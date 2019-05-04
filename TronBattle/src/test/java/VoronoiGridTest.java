import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class VoronoiGridTest {

    private VoronoiGrid expectedVoronoiGridfromFile(String fileName, int maxX, int maxY) {
        VoronoiGrid voronoiGrid = new VoronoiGrid(maxX, maxY);

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int idxY = 0;
            while ((line = br.readLine()) != null) {
                char[] chars = line.toCharArray();
                for (int idxX = 0; idxX < chars.length; idxX++) {
                    char tmpChar = chars[idxX];
                    if (tmpChar == 'X') {
                        voronoiGrid.set(idxX, idxY, -2);
                    } else if (tmpChar == '-') {
                        voronoiGrid.set(idxX, idxY, -1);
                    } else if (tmpChar != '.') {
                        voronoiGrid.set(idxX, idxY, Character.getNumericValue(tmpChar));
                    }
                }
                idxY++;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(fileName + " is malformed.", e);
        }

        return voronoiGrid;
    }

    @Test
    public void alonePlayerTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/voronoiGrid/alonePlayer/given.txt", 6, 3);
        tronGrid.printGrid();
        List<Player> players = Arrays.asList(new Player(0));
        players.get(0).location = new Location(0, 0);

        //When
        VoronoiGrid result = new VoronoiGrid(tronGrid, 0, players);
        result.printGrid();

        //Then
        VoronoiGrid expected = expectedVoronoiGridfromFile("src/test/resources/voronoiGrid/alonePlayer/expected.txt", 6, 3);
        Assert.assertEquals(result, expected);
    }

    @Test
    public void opposed2PlayersTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/voronoiGrid/opposed2Players/given.txt", 5, 3);
        tronGrid.printGrid();
        List<Player> players = Arrays.asList(new Player(0),new Player(1));
        players.get(0).location = new Location(0, 0);
        players.get(1).location = new Location(4, 2);

        //Player 0 begin
        //When
        VoronoiGrid resP0begin = new VoronoiGrid(tronGrid, 0, players);
        resP0begin.printGrid();

        //Then
        VoronoiGrid expectedP0begin = expectedVoronoiGridfromFile("src/test/resources/voronoiGrid/opposed2Players/p0begin.txt", 5, 3);
        Assert.assertEquals(resP0begin, expectedP0begin);

        //Player 1 begin
        //When
        VoronoiGrid resP1begin = new VoronoiGrid(tronGrid, 1, players);
        resP1begin.printGrid();

        //Then
        VoronoiGrid expectedP1begin = expectedVoronoiGridfromFile("src/test/resources/voronoiGrid/opposed2Players/p1begin.txt", 5, 3);
        Assert.assertEquals(resP1begin, expectedP1begin);
    }

    @Test
    public void empty2030GridTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/voronoiGrid/empty2030Grid/given.txt", 20, 30);
        tronGrid.printGrid();
        List<Player> players = Arrays.asList(new Player(0),new Player(1));
        players.get(0).location = new Location(0, 0);
        players.get(1).location = new Location(19, 29);

        //When
        VoronoiGrid result = new VoronoiGrid(tronGrid, 0, players);
        result.printGrid();

        //Then
        VoronoiGrid expected = expectedVoronoiGridfromFile("src/test/resources/voronoiGrid/empty2030Grid/expected.txt", 20, 30);
        Assert.assertEquals(result, expected);
    }

    @Test
    public void choiceTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/voronoiGrid/choice/given.txt", 6, 3);
        tronGrid.printGrid();
        List<Player> players = Arrays.asList(new Player(0));
        players.get(0).location = new Location(4, 1);

        //When
        VoronoiGrid resultGrid = new VoronoiGrid(tronGrid, 0, players);
        int resultPoints = resultGrid.countPoints(0);
        resultGrid.printGrid();

        //Then
        VoronoiGrid expectedGrid = expectedVoronoiGridfromFile("src/test/resources/voronoiGrid/choice/expected.txt", 6, 3);
        int expectedPoints = 5;
        Assert.assertEquals(resultGrid, expectedGrid);
        Assert.assertEquals(expectedPoints, resultPoints);
    }

    @Test
    public void revertChoiceTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/voronoiGrid/revertChoice/given.txt", 3, 6);
        tronGrid.printGrid();
        List<Player> players = Arrays.asList(new Player(0));
        players.get(0).location = new Location(1, 4);

        //When
        VoronoiGrid resultGrid = new VoronoiGrid(tronGrid, 0, players);
        int resultPoints = resultGrid.countPoints(0);
        resultGrid.printGrid();

        //Then
        VoronoiGrid expectedGrid = expectedVoronoiGridfromFile("src/test/resources/voronoiGrid/revertChoice/expected.txt", 3, 6);
        int expectedPoints = 5;
        Assert.assertEquals(resultGrid, expectedGrid);
        Assert.assertEquals(expectedPoints, resultPoints);
    }

    @Test
    public void clashPointsTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/voronoiGrid/clashPoints/given.txt", 7, 6);
        tronGrid.printGrid();
        List<Player> players = Arrays.asList(new Player(0));
        players.get(0).location = new Location(1, 3);

        //When
        VoronoiGrid resultGrid = new VoronoiGrid(tronGrid, 0, players);
        int resultPoints = resultGrid.countPoints(0);
        resultGrid.printGrid();

        //Then
        VoronoiGrid expectedGrid = expectedVoronoiGridfromFile("src/test/resources/voronoiGrid/clashPoints/expected.txt", 7, 6);
        int expectedPoints = 23;
        Assert.assertEquals(resultGrid, expectedGrid);
        Assert.assertEquals(expectedPoints, resultPoints);
    }
}