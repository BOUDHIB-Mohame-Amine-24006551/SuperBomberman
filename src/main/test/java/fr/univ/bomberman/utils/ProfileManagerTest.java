// FILE: src/test/java/fr/univ/bomberman/utils/ProfileManagerTest.java
package fr.univ.bomberman.utils;

import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.model.GameSession;
import fr.univ.bomberman.model.PlayerProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class ProfileManagerTest {

    @TempDir
    Path tempDir;

    ProfileManager mgr;

    @BeforeEach
    void setup() throws Exception {
        // Réinitialiser le singleton
        Field inst = ProfileManager.class.getDeclaredField("instance");
        inst.setAccessible(true);
        inst.set(null, null);

        // Créer une nouvelle instance et rediriger vers tempDir
        mgr = ProfileManager.getInstance();
        Field dir = ProfileManager.class.getDeclaredField("profilesDirectory");
        dir.setAccessible(true);
        dir.set(mgr, tempDir);
    }

    @Test
    void saveAndLoadProfile_roundtrip_shouldPreserveData() throws BombermanException {
        PlayerProfile p = new PlayerProfile("TestUser");
        p.setTotalGamesPlayed(5);
        p.setTotalWins(2);
        p.setPreferredTheme("dark");
        p.setSoundEnabled(false);

        mgr.saveProfile(p);
        assertTrue(mgr.profileExists("TestUser"));

        PlayerProfile loaded = mgr.loadProfile("TestUser");
        assertEquals("TestUser",   loaded.getPlayerName());
        assertEquals(5,            loaded.getTotalGamesPlayed());
        assertEquals(2,            loaded.getTotalWins());
        assertEquals("dark",       loaded.getPreferredTheme());
        assertFalse(loaded.isSoundEnabled());
    }

    @Test
    void deleteProfile_existingProfile_shouldRemoveFile() throws BombermanException {
        PlayerProfile p = new PlayerProfile("ToDelete");
        mgr.saveProfile(p);
        assertTrue(mgr.profileExists("ToDelete"));

        boolean deleted = mgr.deleteProfile("ToDelete");
        assertTrue(deleted);
        assertFalse(mgr.profileExists("ToDelete"));
    }

    @Test
    void listProfiles_multipleProfiles_shouldReturnNames() throws BombermanException {
        mgr.saveProfile(new PlayerProfile("UserA"));
        mgr.saveProfile(new PlayerProfile("UserB"));

        List<String> list = mgr.listProfiles();
        assertTrue(list.contains("usera"));
        assertTrue(list.contains("userb"));
        assertEquals(2, list.size());
    }

    @Test
    void recordCustomGameSession_shouldUpdateStats() throws BombermanException {
        PlayerProfile p = new PlayerProfile("PlayerX");
        mgr.saveProfile(p);

        GameSession session = new GameSession();
        session.setWon(true);
        session.setBombsPlaced(4);
        session.setEliminationsDealt(3);
        session.setDeaths(1);
        session.setDurationSeconds(300);

        mgr.recordCustomGameSession("PlayerX", session);

        PlayerProfile updated = mgr.loadProfile("PlayerX");
        assertEquals(1, updated.getTotalGamesPlayed());
        assertEquals(1, updated.getTotalWins());
        assertEquals(0, updated.getTotalLosses());
        assertEquals(4, updated.getTotalBombsPlaced());
        assertEquals(3, updated.getTotalEliminatonsDealt());
        assertEquals(1, updated.getTotalDeaths());
        assertEquals(300, updated.getTotalPlayTimeSeconds());
        assertNotNull(updated.getLastPlayDate());
        assertTrue(updated.getLastPlayDate().isAfter(updated.getCreationDate()));
    }


}
