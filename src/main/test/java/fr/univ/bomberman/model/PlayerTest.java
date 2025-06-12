// FILE: src/test/java/fr/univ/bomberman/model/PlayerTest.java
package fr.univ.bomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Alice", new Position(1, 2));
    }

    @Test
    void testNameAndPosition() {
        assertEquals("Alice", player.getName());
        assertEquals(1, player.getX());
        assertEquals(2, player.getY());

        player.setName("Bob");
        player.setPosition(new Position(5, 6));
        assertEquals("Bob", player.getName());
        assertEquals(5, player.getX());
        assertEquals(6, player.getY());
    }

    @Test
    void testBombCooldownAndPlacement() {
        // Au départ, lastBombTime = 0 → pas de cooldown
        assertFalse(player.isOnBombCooldown());
        assertEquals(0, player.getCooldownPercentage());
        assertTrue(player.canPlaceBomb());

        player.bombPlaced(); // pose une bombe → démarre le cooldown
        assertTrue(player.isOnBombCooldown());
        assertTrue(player.getRemainingCooldown() > 0);
        assertTrue(player.getCooldownPercentage() > 0);

        player.resetBombCooldown();
        assertFalse(player.isOnBombCooldown());
        assertEquals(0, player.getCooldownPercentage());
    }

    @Test
    void testCanPlaceBombWhenEliminatedFlag() {
        player.setEliminated(true);
        // par défaut, canPlaceBombWhenEliminated == false
        assertFalse(player.canPlaceBomb());

        player.setCanPlaceBombWhenEliminated(true);
        assertTrue(player.canPlaceBomb());
    }

    @Test
    void testCapturedFlagsManagement() {
        assertEquals(0, player.getCapturedFlagsCount());

        player.addCapturedFlag("flag1");
        player.addCapturedFlag("flag2");
        player.addCapturedFlag("flag1"); // doublon
        assertEquals(2, player.getCapturedFlagsCount());

        player.removeCapturedFlag("flag1");
        assertEquals(1, player.getCapturedFlagsCount());

        player.clearCapturedFlags();
        assertEquals(0, player.getCapturedFlagsCount());
    }
}
