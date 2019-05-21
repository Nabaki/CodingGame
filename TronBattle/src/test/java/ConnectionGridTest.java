import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ConnectionGridTest {

    @Test
    public void fromBlankGrid() {
        //GIVEN
        TronGrid tronGrid = new TronGrid(30, 20);

        Motocycle motocycle = new Motocycle(0);
        motocycle.position = new Position(0, 0);
        tronGrid.set(motocycle.position.x, motocycle.position.y, motocycle.id);

        List<Motocycle> motocycles = new ArrayList<>();
        motocycles.add(motocycle);

        //WHEN
        ConnectionGrid connectionGrid = new ConnectionGrid(tronGrid, motocycles);

        //THEN
        connectionGrid.debug();
    }
}