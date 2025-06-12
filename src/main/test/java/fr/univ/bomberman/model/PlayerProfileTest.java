// FILE: src/test/java/fr/univ/bomberman/model/PlayerProfileTest.java
package fr.univ.bomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PlayerProfileTest {
    private PlayerProfile profile;

    @BeforeEach
    void setUp() {
        profile = new PlayerProfile("TestUser");
        // figer les dates pour les tests de format
        profile.setCreationDate(LocalDateTime.of(2025,6,12,14,30));
        profile.setLastPlayDate(LocalDateTime.of(2025,6,12,15,45));
    }

    @Test
    void testWinRatioAndKillDeathRatio() {
        // 0 games → ratio 0
        assertEquals(0.0, profile.getWinRatio());
        assertEquals(0.0, profile.getKillDeathRatio());

        profile.setTotalGamesPlayed(10);
        profile.setTotalWins(3);
        profile.setTotalEliminatonsDealt(8);
        profile.setTotalDeaths(2);

        assertEquals(30.0, profile.getWinRatio(), 1e-6);
        assertEquals(4.0, profile.getKillDeathRatio(), 1e-6);
    }

    @Test
    void testFormattedDatesAndPlayTime() {
        assertEquals("12/06/2025 14:30", profile.getFormattedCreationDate());
        assertEquals("12/06/2025 15:45", profile.getFormattedLastPlayDate());

        profile.setTotalPlayTimeSeconds(3665); // 1h 1m 5s
        assertEquals("1h 1m", profile.getFormattedTotalPlayTime());
    }

    @Test
    void testStatsForMode() {
        assertSame(profile.getClassicModeStats(), profile.getStatsForMode(GameMode.REAL_TIME));
        assertSame(profile.getClassicModeStats(), profile.getStatsForMode(GameMode.TURN_BASED));
        assertSame(profile.getBattleRoyaleStats(), profile.getStatsForMode(GameMode.BATTLE_ROYALE));
        assertSame(profile.getBotModeStats(), profile.getStatsForMode(GameMode.BOT_GAME));
        assertSame(profile.getCtfModeStats(), profile.getStatsForMode(GameMode.CAPTURE_THE_FLAG));
    }
}
