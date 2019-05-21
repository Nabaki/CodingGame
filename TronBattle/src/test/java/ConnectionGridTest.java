import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ConnectionGridTest {

    @Test
    public void fromBlankGrid() {
        //GIVEN
        TronGrid tronGrid = new TronGrid(30, 20);

        Player player = new Player(0);
        player.position = new Position(0, 0);
        tronGrid.set(player.position.x, player.position.y, player.id);

        List<Player> players = new ArrayList<>();
        players.add(player);

        //WHEN
        ConnectionGrid connectionGrid = new ConnectionGrid(tronGrid, players);

        //THEN
        connectionGrid.debug();
    }
}