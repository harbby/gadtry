package com.github.harbby.gadtry.collection;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PlayerIteratorPlusTest {
    @Test
    public void should_get_number_of_players() {
        PlayerIteratorPlusTest.PlayerIterator playerIterator = createPlayerIterator();
        assertEquals(4L, playerIterator.size());
    }

    @Test
    public void should_get_number_of_saber() {
        long count = createPlayerIterator()
                .flatMap(player -> player.getAchievement().iterator())
                .filter(s -> s.equals("saber"))
                .size();
        assertEquals(2L, count);
    }

    @Test
    public void should_return_team2_member() {
        IteratorPlus<Player> iterator = createPlayerIterator()
                .filter(player -> player.getTeamName().equals("team2"));
        assertEquals("player2", iterator.next().getName());
        assertEquals("player3", iterator.next().getName());
    }

    public static class Player {
        private final long id;

        private final String name;

        private final String teamName;

        private final List<String> achievement;

        public Player(long id, String name, String teamName, List<String> achievement) {
            this.id = id;
            this.name = name;
            this.teamName = teamName;
            this.achievement = achievement;
        }

        public String getName() {
            return name;
        }

        public String getTeamName() {
            return teamName;
        }

        public List<String> getAchievement() {
            return achievement;
        }
    }

    public static class PlayerIterator implements IteratorPlus<Player> {
        private final Iterator<Player> iterator;

        public PlayerIterator(List<Player> players) {
            this.iterator = players.listIterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Player next() {
            return iterator.next();
        }
    }

    private PlayerIteratorPlusTest.PlayerIterator createPlayerIterator() {
        PlayerIteratorPlusTest.Player player1 = new PlayerIteratorPlusTest.Player(1L, "player1", "team1", Arrays.asList("king", "saber"));
        PlayerIteratorPlusTest.Player player2 = new PlayerIteratorPlusTest.Player(2L, "player2", "team2", Arrays.asList("lancer", "champion"));
        PlayerIteratorPlusTest.Player player3 = new PlayerIteratorPlusTest.Player(3L, "player3", "team2", Arrays.asList("killer", "master"));
        PlayerIteratorPlusTest.Player player4 = new PlayerIteratorPlusTest.Player(4L, "player4", "team3", Arrays.asList("archer", "saber"));
        List<PlayerIteratorPlusTest.Player> players = Arrays.asList(player1, player2, player3, player4);
        return new PlayerIteratorPlusTest.PlayerIterator(players);
    }
}


