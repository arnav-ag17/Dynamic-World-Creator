import core.AutograderBuddy;
import core.Engine;
import edu.princeton.cs.algs4.StdDraw;
import org.junit.jupiter.api.Test;
import tileengine.TERenderer;
import tileengine.TETile;

import static com.google.common.truth.Truth.assertThat;

public class WorldGenTests {
    @Test
    public void basicTest() {
        // put different seeds here to test different worlds
        TETile[][] tiles = AutograderBuddy.getWorldFromInput("n1234567890123456789s");

        TERenderer ter = new TERenderer();
        ter.initialize(tiles.length, tiles[0].length);
        ter.renderFrame(tiles);
        StdDraw.pause(5000); // pause for 5 seconds so you can see the output
    }

    @Test
    public void basicInteractivityTest() {
        // TODO: write a test that uses an input like "n123swasdwasd"
        String inputSeed = "n123swasdwasd";
        Engine engine = new Engine();

        TETile[][] world = engine.interactWithInputString(inputSeed);
        System.out.println(TETile.toString(world));
    }

    @Test
    public void basicSaveTest() {
        TETile[][] expected = AutograderBuddy.getWorldFromInput("n1392967723524655428sddsaawwsaddw");
        TETile[][] actual = AutograderBuddy.getWorldFromInput("n1392967723524655428sddsaawws:qladdw");

        assertThat(actual).isEqualTo(expected);
        // TODO: write a test that calls getWorldFromInput twice, with "n123swasd:q" and with "lwasd"
    }
}
