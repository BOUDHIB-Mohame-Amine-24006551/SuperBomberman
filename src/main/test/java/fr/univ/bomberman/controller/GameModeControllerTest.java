// FILE: src/test/java/fr/univ/bomberman/controller/GameModeControllerTest.java
package fr.univ.bomberman.controller;

import fr.univ.bomberman.model.PlayerProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class GameModeControllerTest {

    private GameModeController controller;

    @BeforeEach
    void setUp() {
        controller = new GameModeController();
    }

    @Test
    void testGetDetailedBotInfo_easy() {
        String info = invokeDetailedBotInfo(1);
        assertTrue(info.contains("😊 IA Débutante"));
        assertTrue(info.contains("Pose des bombes aléatoirement"));
    }

    @Test
    void testGetDetailedBotInfo_medium() {
        String info = invokeDetailedBotInfo(2);
        assertTrue(info.contains("😐 IA Équilibrée"));
        assertTrue(info.contains("Évite les dangers principaux"));
    }

    @Test
    void testGetDetailedBotInfo_hard() {
        String info = invokeDetailedBotInfo(3);
        assertTrue(info.contains("😈 IA Redoutable"));
        assertTrue(info.contains("Réagit rapidement aux menaces"));
    }



    @Test
    void testGetRelativePath_insideResources() throws Exception {
        File file = new File("src/main/resources/fr/univ/bomberman/level/level1.json");
        String expected = file.getAbsolutePath();
        String rel = invokeRelativePath(String.valueOf(file));
        assertEquals(expected, rel);
    }

    @Test
    void testGetRelativePath_outsideResources() throws Exception {
        File file = new File("/tmp/otherdir/level2.json");
        String expected = file.getAbsolutePath();
        String rel = invokeRelativePath(String.valueOf(file));
        assertEquals(expected, rel);
    }

    @Test
    void testStaticCurrentGameProfile() {
        // par défaut null
        assertNull(GameModeController.getCurrentGameProfile());
        PlayerProfile p = new PlayerProfile("Test");
        GameModeController.setCurrentGameProfile(p);
        assertSame(p, GameModeController.getCurrentGameProfile());
    }

    /** Helpers pour appeler les méthodes privées */
    private String invokeDetailedBotInfo(int diff) {
        try {
            var m = GameModeController.class.getDeclaredMethod("getDetailedBotInfo", int.class);
            m.setAccessible(true);
            return (String)m.invoke(controller, diff);
        } catch (Exception e) {
            fail("Erreur reflection getDetailedBotInfo: " + e.getMessage());
            return null;
        }
    }

    private String invokeRelativePath(String path) {
        try {
            var m = GameModeController.class.getDeclaredMethod("getRelativePath", File.class);
            m.setAccessible(true);
            return (String)m.invoke(controller, new File(path));
        } catch (Exception e) {
            fail("Erreur reflection getRelativePath: " + e.getMessage());
            return null;
        }
    }
}
