import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LonelyGridTest {

    @Test
    public void alonePlayerTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/lonelyGrid/alonePlayer.txt", 3, 3);
        tronGrid.printGrid();
        Player player = new Player(0);
        player.location = new Location(0, 0);

        //When
        LonelyGrid result = new LonelyGrid(tronGrid);
        boolean isAlone = result.isAlone(0, Collections.singletonList(player));
        result.printGrid();

        //Then
        Assert.assertTrue(isAlone);
    }

    @Test
    public void notAlonePlayerTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/lonelyGrid/notAlonePlayer.txt", 3, 3);
        tronGrid.printGrid();

        Player player0 = new Player(0);
        player0.location = new Location(0, 0);
        Player player1 = new Player(1);
        player1.location = new Location(2, 2);

        //When
        LonelyGrid result = new LonelyGrid(tronGrid);
        boolean isAlone = result.isAlone(0, Arrays.asList(player0, player1));
        result.printGrid();

        //Then
        Assert.assertFalse(isAlone);
    }

    @Test
    public void trappedPlayerTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/lonelyGrid/trappedPlayer.txt", 3, 3);
        tronGrid.printGrid();
        Player player0 = new Player(0);
        player0.location = new Location(0, 0);
        Player player1 = new Player(1);
        player1.location = new Location(2, 2);

        //When
        LonelyGrid result = new LonelyGrid(tronGrid);
        result.printGrid();
        boolean isAlone = result.isAlone(0, Arrays.asList(player0, player1));
        result.printGrid();

        //Then
        Assert.assertTrue(isAlone);
    }
}