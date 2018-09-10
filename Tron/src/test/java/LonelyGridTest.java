import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class LonelyGridTest {

    @Test
    public void alonePlayerTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/lonelyGrid/alonePlayer.txt", 3, 3);
        tronGrid.printGrid();
        List<Player> players = Arrays.asList(new Player(0));
        players.get(0).location = new Location(0, 0);

        //When
        LonelyGrid result = new LonelyGrid(tronGrid);
        result.printGrid();

        //Then
        Assert.assertEquals(true, result.isAlone(0, players));
    }

    @Test
    public void notAlonePlayerTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/lonelyGrid/notAlonePlayer.txt", 3, 3);
        tronGrid.printGrid();
        List<Player> players = Arrays.asList(new Player(0), new Player(1));
        players.get(0).location = new Location(0, 0);
        players.get(1).location = new Location(2, 2);

        //When
        LonelyGrid result = new LonelyGrid(tronGrid);
        result.printGrid();

        //Then
        Assert.assertEquals(false, result.isAlone(0, players));
    }

    @Test
    public void trappedPlayerTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/lonelyGrid/trappedPlayer.txt", 3, 3);
        tronGrid.printGrid();
        List<Player> players = Arrays.asList(new Player(0), new Player(1));
        players.get(0).location = new Location(0, 0);
        players.get(1).location = new Location(2, 2);

        //When
        LonelyGrid result = new LonelyGrid(tronGrid);
        result.printGrid();

        //Then
        Assert.assertEquals(true, result.isAlone(0, players));
    }
}