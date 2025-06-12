// FILE: src/test/java/fr/univ/bomberman/utils/JsonUtilsTest.java
package fr.univ.bomberman.utils;

import fr.univ.bomberman.model.Cell;
import fr.univ.bomberman.model.CellType;
import fr.univ.bomberman.model.Position;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void readLevelFile_validJson_shouldReturnJsonObject() throws IOException {
        String json = "{ \"width\":2, \"height\":1, \"autoFill\":false, \"grid\":[[1,3]] }";
        Path file = tempDir.resolve("level.json");
        Files.write(file, json.getBytes());

        JSONObject obj = JsonUtils.readLevelFile(file.toString());
        assertEquals(2, obj.getInt("width"));
        assertEquals(1, obj.getInt("height"));
        assertFalse(obj.getBoolean("autoFill"));
    }

    @Test
    void parseLevelGrid_noAutoFill_shouldMapValuesCorrectly() {
        /*
         * width=3, height=2
         * grid:
         * [1,2,0]
         * [3,0,2]
         * autoFill=false
         */
        JSONObject level = new JSONObject()
                .put("width", 3)
                .put("height", 2)
                .put("autoFill", false)
                .put("grid", new org.json.JSONArray()
                        .put(new org.json.JSONArray(new int[]{1,2,0}))
                        .put(new org.json.JSONArray(new int[]{3,0,2}))
                );

        Cell[][] cells = JsonUtils.parseLevelGrid(level);
        assertEquals(2, cells.length);
        assertEquals(3, cells[0].length);

        // ligne 0
        assertEquals(CellType.INDESTRUCTIBLE_WALL, cells[0][0].getType());
        assertEquals(CellType.DESTRUCTIBLE_BRICK,   cells[0][1].getType());
        assertEquals(CellType.EMPTY,                cells[0][2].getType());

        // ligne 1
        assertEquals(CellType.EMPTY,                cells[1][0].getType());
        assertEquals(CellType.EMPTY,                cells[1][1].getType());
        assertEquals(CellType.DESTRUCTIBLE_BRICK,   cells[1][2].getType());

        // positions
        assertEquals(new Position(0,0), cells[0][0].getPosition());
        assertEquals(new Position(2,1), cells[1][2].getPosition());
    }
}
