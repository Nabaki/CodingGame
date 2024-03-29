import org.junit.Assert;
import org.junit.Ignore;
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
    @Ignore(value = "Not used and work in progress")
    public void fromBlankGrid() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/dfsGrid/emptyGrid/given.txt", 3, 3);
        Motocycle motocycle = new Motocycle(0);
        motocycle.position = new Position(0, 0);
        tronGrid.set(motocycle.position.x, motocycle.position.y, motocycle.id);

        //When
        DfsGrid result = new DfsGrid(tronGrid, motocycle);
        System.err.println("Result grid");
        result.debug();

        //Then
        DfsGrid expected = expectedDfsGridfromFile("src/test/resources/dfsGrid/emptyGrid/expected.txt", 3, 3);
        System.err.println("Expected grid");
        expected.debug();

        Assert.assertEquals(result, expected);
    }

    @Test
    @Ignore(value = "Not used and work in progress")
    public void fromSimpleCase() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/dfsGrid/simpleCase/given.txt", 4, 3);
        Motocycle motocycle = new Motocycle(0);
        motocycle.position = new Position(0, 1);
        tronGrid.set(motocycle.position.x, motocycle.position.y, motocycle.id);

        //When
        DfsGrid result = new DfsGrid(tronGrid, motocycle);
        System.err.println("Result grid");
        result.debug();

        //Then
        DfsGrid expected = expectedDfsGridfromFile("src/test/resources/dfsGrid/simpleCase/expected.txt", 3, 3);
        System.err.println("Expected grid");
        expected.debug();

        Assert.assertEquals(result, expected);
    }
}