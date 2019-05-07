import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DfsGridTest {

    private DfsGrid expectedDfsGridfromFile(String fileName, int maxX, int maxY) {
        DfsGrid dfsGrid = new DfsGrid(maxX, maxY);

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int idxY = 0;
            while ((line = br.readLine()) != null) {
                String[] nodes = line.split("\\|");
                for (int idxX = 0; idxX < nodes.length; idxX++) {
                    String[] datas = nodes[idxX].split(",");
                    DfsNode tmpNode = new DfsNode(Integer.parseInt(datas[0]), Integer.parseInt(datas[1]));
                    dfsGrid.set(idxX, idxY, tmpNode);
                }
                idxY++;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(fileName + " is malformed.", e);
        }

        return dfsGrid;
    }

    @Test
    public void fromBlankGrid() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/dfsGrid/emptyGrid/given.txt", 3, 3);
        Player player = new Player(0);
        player.location = new Location(0, 0);
        tronGrid.set(player.location.x, player.location.y, player.id);

        //When
        DfsGrid result = new DfsGrid(tronGrid, player);
        System.err.println("Result grid");
        result.printGrid();

        //Then
        DfsGrid expected = expectedDfsGridfromFile("src/test/resources/dfsGrid/emptyGrid/expected.txt", 3, 3);
        System.err.println("Expected grid");
        expected.printGrid();

        Assert.assertEquals(result, expected);
    }

    @Test
    public void fromSimpleCase() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/dfsGrid/simpleCase/given.txt", 4, 3);
        Player player = new Player(0);
        player.location = new Location(0, 1);
        tronGrid.set(player.location.x, player.location.y, player.id);

        //When
        DfsGrid result = new DfsGrid(tronGrid, player);
        System.err.println("Result grid");
        result.printGrid();

        //Then
        DfsGrid expected = expectedDfsGridfromFile("src/test/resources/dfsGrid/simpleCase/expected.txt", 3, 3);
        System.err.println("Expected grid");
        expected.printGrid();

        Assert.assertEquals(result, expected);
    }
}