import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class LonelyGridTest {

    @Test
    public void aloneMotocycleTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/lonelyGrid/alone.txt", 3, 3);
        tronGrid.debug();
        Motocycle motocycle = new Motocycle(0);
        motocycle.position = new Position(0, 0);

        //When
        LonelyGrid result = new LonelyGrid(tronGrid);
        boolean isAlone = result.isAlone(0, Collections.singletonList(motocycle));
        result.debug();

        //Then
        Assert.assertTrue(isAlone);
    }

    @Test
    public void notAloneMotocycleTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/lonelyGrid/notAlone.txt", 3, 3);
        tronGrid.debug();

        Motocycle motocycle0 = new Motocycle(0);
        motocycle0.position = new Position(0, 0);
        Motocycle motocycle1 = new Motocycle(1);
        motocycle1.position = new Position(2, 2);

        //When
        LonelyGrid result = new LonelyGrid(tronGrid);
        boolean isAlone = result.isAlone(0, Arrays.asList(motocycle0, motocycle1));
        result.debug();

        //Then
        Assert.assertFalse(isAlone);
    }

    @Test
    public void trappedMotocycleTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/lonelyGrid/trapped.txt", 3, 3);
        tronGrid.debug();
        Motocycle motocycle0 = new Motocycle(0);
        motocycle0.position = new Position(0, 0);
        Motocycle motocycle1 = new Motocycle(1);
        motocycle1.position = new Position(2, 2);

        //When
        LonelyGrid result = new LonelyGrid(tronGrid);
        result.debug();
        boolean isAlone = result.isAlone(0, Arrays.asList(motocycle0, motocycle1));
        result.debug();

        //Then
        Assert.assertTrue(isAlone);
    }
}