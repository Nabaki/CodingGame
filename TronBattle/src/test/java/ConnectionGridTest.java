import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ConnectionGridTest {

    @Test
    public void fromBlankGrid() {
        //GIVEN
        TronGrid tronGrid = new TronGrid(30, 20);

        MotoCycle motoCycle = new MotoCycle(0);
        motoCycle.position = new Position(0, 0);
        tronGrid.set(motoCycle.position.x, motoCycle.position.y, motoCycle.id);

        List<MotoCycle> motoCycles = new ArrayList<>();
        motoCycles.add(motoCycle);

        //WHEN
        ConnectionGrid connectionGrid = new ConnectionGrid(tronGrid, motoCycles);

        //THEN
        connectionGrid.debug();
    }
}