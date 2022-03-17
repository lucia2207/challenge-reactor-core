package co.com.sofka.challengereactorcore;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

public class CSVUtilTest {

    @Test
    void converterData(){
        List<Player> list = CsvUtilFile.getPlayers();
        assert list.size() == 18207;
    }

    @Test
    void stream_filtrarJugadoresMayoresA35(){
        List<Player> list = CsvUtilFile.getPlayers();
        Map<String, List<Player>> listFilter = list.parallelStream()
                .filter(player -> player.age >= 35)
                .map(player -> {
                    player.name = player.name.toUpperCase(Locale.ROOT);
                    return player;
                })
                .flatMap(playerA -> list.parallelStream()
                        .filter(playerB -> playerA.club.equals(playerB.club))
                )
                .distinct()
                .collect(Collectors.groupingBy(Player::getClub));

        assert listFilter.size() == 322;
    }


    @Test
    void reactive_filtrarJugadoresMayoresA35(){
        List<Player> list = CsvUtilFile.getPlayers();
        Flux<Player> listFlux = Flux.fromStream(list.parallelStream()).cache();
        Mono<Map<String, Collection<Player>>> listFilter = listFlux
                .filter(player -> player.age >= 35)
                .map(player -> {
                    player.name = player.name.toUpperCase(Locale.ROOT);
                    return player;
                })
                .buffer(100)
                .flatMap(playerA -> listFlux
                         .filter(playerB -> playerA.stream()
                                 .anyMatch(a ->  a.club.equals(playerB.club)))
                )
                .distinct()
                .collectMultimap(Player::getClub);

        assert listFilter.block().size() == 322;
    }
// A partir de una lista de datos de jugadores se debe realizar consultas y/o operaciones que permita filtrar y ordenar los jugadores, se debe consultas los jugadores mayores a 34 años, jugadores filtrados por un club especifico.
    @Test
    void a34filtro(){
        List<Player> list = CsvUtilFile.getPlayers();
        Flux<Player> listFlux = Flux.fromStream(list.parallelStream()).cache();
        Mono<Map<String, Collection<Player>>> listFilter = listFlux
                .filter(player -> player.club.equals("Juventus") && player.age >= 34)
                .distinct()
                .collectMultimap(Player::getClub);
        System.out.println("Equipo: ");
        listFilter.block().forEach((equipo, players) -> {
            System.out.println(equipo);
            players.forEach(player -> {
                System.out.println("Nombre Jugador: " + player.name + "\n" + "Edad Jugador: " + player.age + " años");
                assert player.club.equals("Juventus");
            });
        });
    assert listFilter.block().size() == 1;
}
//Consular las nacionalidades de los jugadores, crear una lista de las nacionalidades y un rancking de los jugadores por cada pais que se tengan en la lista.

    @Test
    void aNacionalidadFiltro() {
        List<Player> list = CsvUtilFile.getPlayers();
        Flux<Player> listFlux = Flux.fromStream(list.parallelStream()).cache();
        Mono<Map<String, Collection<Player>>> listFilter = listFlux
                .buffer(100)
                .flatMap(player1 -> listFlux
                        .filter(player2 -> player1.stream()
                                .anyMatch(a -> a.national.equals(player2.national)))
                ).distinct()
                .sort((k, player) -> player.winners)
                .collectMultimap(Player::getNational);

        System.out.println("Por Nacionalidad: ");
        System.out.println(listFilter.block().size());
        listFilter.block().forEach((pais, players) -> {
            System.out.println("Pais: " + pais + "\n" + "{");
            players.forEach(player -> {
                System.out.println("Nombre Jugador: " + player.name + "\n" + " victorias: " + player.winners);
            });
            System.out.println("}");
        });
    }
}
    //Consular las nacionalidades de los jugadores, crear una lista de las nacionalidades y un rancking de los jugadores por cada pais que se tengan en la lista.

