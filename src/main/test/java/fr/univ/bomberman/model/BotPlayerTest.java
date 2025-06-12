// FILE: src/test/java/fr/univ/bomberman/model/BotPlayerTest.java
package fr.univ.bomberman.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BotPlayerTest {

    @Test
    void testDifficultyClamping() {
        Position pos = new Position(0,0);
        BotPlayer b0 = new BotPlayer("B0", pos, 0);
        assertEquals(1, b0.getDifficulty());
        BotPlayer b1 = new BotPlayer("B1", pos, 1);
        assertEquals(1, b1.getDifficulty());
        BotPlayer b3 = new BotPlayer("B3", pos, 3);
        assertEquals(3, b3.getDifficulty());
        BotPlayer b4 = new BotPlayer("B4", pos, 4);
        assertEquals(3, b4.getDifficulty());
    }

    @Test
    void decideAction_whenEliminated_returnsNone() {
        BotPlayer bot = new BotPlayer("TestBot", new Position(1,1), 2);
        bot.setEliminated(true);
        // on peut passer null car isEliminated est vérifié en premier
        assertEquals(BotAction.NONE, bot.decideAction(null));
    }
}
