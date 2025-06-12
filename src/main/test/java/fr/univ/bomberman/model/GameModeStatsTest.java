// FILE: src/test/java/fr/univ/bomberman/model/GameModeStatsTest.java
package fr.univ.bomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameModeStatsTest {
    private GameModeStats stats;

    @BeforeEach
    void setUp() {
        stats = new GameModeStats();
    }

    @Test
    void testAddGame() {
        stats.addGame(true);
        assertEquals(1, stats.getGamesPlayed());
        assertEquals(1, stats.getWins());

        stats.addGame(false);
        assertEquals(2, stats.getGamesPlayed());
        // on s'assure que losses a été incrémenté via recordGame implicitement
    }

    @Test
    void testWinRatio() {
        assertEquals(0.0, stats.getWinRatio());
        stats.setGamesPlayed(4);
        stats.setWins(1);
        assertEquals(25.0, stats.getWinRatio(), 1e-6);
    }

}
