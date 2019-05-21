import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class LonelyGridTest {

    @Test
    public void aloneMotoCycleTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/lonelyGrid/alone.txt", 3, 3);
        tronGrid.debug();
        MotoCycle motoCycle = new MotoCycle(0);
        motoCycle.position = new Position(0, 0);

        //When
        LonelyGrid result = new LonelyGrid(tronGrid);
        boolean isAlone = result.isAlone(0, Collections.singletonList(motoCycle));
        result.debug();

        //Then
        Assert.assertTrue(isAlone);
    }

    @Test
    public void notAloneMotoCycleTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/lonelyGrid/notAlone.txt", 3, 3);
        tronGrid.debug();

        MotoCycle motoCycle0 = new MotoCycle(0);
        motoCycle0.position = new Position(0, 0);
        MotoCycle motoCycle1 = new MotoCycle(1);
        motoCycle1.position = new Position(2, 2);

        //When
        LonelyGrid result = new LonelyGrid(tronGrid);
        boolean isAlone = result.isAlone(0, Arrays.asList(motoCycle0, motoCycle1));
        result.debug();

        //Then
        Assert.assertFalse(isAlone);
    }

    @Test
    public void trappedMotoCycleTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/lonelyGrid/trapped.txt", 3, 3);
        tronGrid.debug();
        MotoCycle motoCycle0 = new MotoCycle(0);
        motoCycle0.position = new Position(0, 0);
        MotoCycle motoCycle1 = new MotoCycle(1);
        motoCycle1.position = new Position(2, 2);

        //When
        LonelyGrid result = new LonelyGrid(tronGrid);
        result.debug();
        boolean isAlone = result.isAlone(0, Arrays.asList(motoCycle0, motoCycle1));
        result.debug();

        //Then
        Assert.assertTrue(isAlone);
    }
}