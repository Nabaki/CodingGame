import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ConnectionGridTest {

    @Test
    public void fromBlankGrid() {
        //GIVEN
        TronGrid tronGrid = new TronGrid(30, 20);

        Player player = new Player(0);
        player.location = new Location(0, 0);
        tronGrid.set(player.location.x, player.location.y, player.id);

        List<Player> players = new ArrayList<>();
        players.add(player);

        //WHEN
        ConnectionGrid connectionGrid = new ConnectionGrid(tronGrid, players, player.id);

        //THEN
        connectionGrid.printGrid();
    }
}