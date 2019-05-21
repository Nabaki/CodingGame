import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class LonelyGridTest {

    @Test
    public void alonePlayerTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/lonelyGrid/alonePlayer.txt", 3, 3);
        tronGrid.debug();
        Player player = new Player(0);
        player.position = new Position(0, 0);

        //When
        LonelyGrid result = new LonelyGrid(tronGrid);
        boolean isAlone = result.isAlone(0, Collections.singletonList(player));
        result.debug();

        //Then
        Assert.assertTrue(isAlone);
    }

    @Test
    public void notAlonePlayerTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/lonelyGrid/notAlonePlayer.txt", 3, 3);
        tronGrid.debug();

        Player player0 = new Player(0);
        player0.position = new Position(0, 0);
        Player player1 = new Player(1);
        player1.position = new Position(2, 2);

        //When
        LonelyGrid result = new LonelyGrid(tronGrid);
        boolean isAlone = result.isAlone(0, Arrays.asList(player0, player1));
        result.debug();

        //Then
        Assert.assertFalse(isAlone);
    }

    @Test
    public void trappedPlayerTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/lonelyGrid/trappedPlayer.txt", 3, 3);
        tronGrid.debug();
        Player player0 = new Player(0);
        player0.position = new Position(0, 0);
        Player player1 = new Player(1);
        player1.position = new Position(2, 2);

        //When
        LonelyGrid result = new LonelyGrid(tronGrid);
        result.debug();
        boolean isAlone = result.isAlone(0, Arrays.asList(player0, player1));
        result.debug();

        //Then
        Assert.assertTrue(isAlone);
    }
}