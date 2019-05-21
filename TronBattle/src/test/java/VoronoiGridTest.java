import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
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
    public void aloneTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/voronoiGrid/alone/given.txt", 6, 3);
        tronGrid.debug();

        MotoCycle motoCycle0 = new MotoCycle(0);
        motoCycle0.position = new Position(0, 0);

        //When
        VoronoiGrid resultGrid = new VoronoiGrid(tronGrid, 0, Collections.singletonList(motoCycle0));
        resultGrid.debug();
        int resultPoints = resultGrid.countPoints(motoCycle0.id);

        //Then
        VoronoiGrid expected = expectedVoronoiGridfromFile("src/test/resources/voronoiGrid/alone/expected.txt", 6, 3);
        Assert.assertEquals(resultGrid, expected);
        Assert.assertEquals(17, resultPoints);
    }

    @Test
    public void opposed2MotoCyclesTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/voronoiGrid/opposed2/given.txt", 5, 3);
        tronGrid.debug();

        MotoCycle motoCycle0 = new MotoCycle(0);
        motoCycle0.position = new Position(0, 0);
        MotoCycle motoCycle1 = new MotoCycle(1);
        motoCycle1.position = new Position(4, 2);
        List<MotoCycle> motoCycles = Arrays.asList(motoCycle0, motoCycle1);

        //MotoCycle 0 begin
        //When
        VoronoiGrid resP0beginGrid = new VoronoiGrid(tronGrid, 0, motoCycles);
        resP0beginGrid.debug();
        int resP0beginPoints0 = resP0beginGrid.countPoints(motoCycle0.id);
        int resP0beginPoints1 = resP0beginGrid.countPoints(motoCycle1.id);

        //Then
        VoronoiGrid expectedP0begin = expectedVoronoiGridfromFile("src/test/resources/voronoiGrid/opposed2/p0begin.txt", 5, 3);
        Assert.assertEquals(resP0beginGrid, expectedP0begin);
        Assert.assertEquals(5, resP0beginPoints0);
        Assert.assertEquals(8, resP0beginPoints1);

        //MotoCycle 1 begin
        //When
        VoronoiGrid resP1beginGrid = new VoronoiGrid(tronGrid, 1, motoCycles);
        resP1beginGrid.debug();
        int resP1beginPoints0 = resP1beginGrid.countPoints(motoCycle0.id);
        int resP1beginPoints1 = resP1beginGrid.countPoints(motoCycle1.id);

        //Then
        VoronoiGrid expectedP1begin = expectedVoronoiGridfromFile("src/test/resources/voronoiGrid/opposed2/p1begin.txt", 5, 3);
        Assert.assertEquals(resP1beginGrid, expectedP1begin);
        Assert.assertEquals(8, resP1beginPoints0);
        Assert.assertEquals(5, resP1beginPoints1);
    }

    @Test
    public void empty2030GridTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/voronoiGrid/empty2030Grid/given.txt", 20, 30);
        tronGrid.debug();

        MotoCycle motoCycle0 = new MotoCycle(0);
        motoCycle0.position = new Position(0, 0);
        MotoCycle motoCycle1 = new MotoCycle(1);
        motoCycle1.position = new Position(19, 29);

        //When
        VoronoiGrid resultGrid = new VoronoiGrid(tronGrid, 0, Arrays.asList(motoCycle0, motoCycle1));
        resultGrid.debug();
        int resultPoints0 = resultGrid.countPoints(motoCycle0.id);
        int resultPoints1 = resultGrid.countPoints(motoCycle1.id);

        //Then
        VoronoiGrid expected = expectedVoronoiGridfromFile("src/test/resources/voronoiGrid/empty2030Grid/expected.txt", 20, 30);
        Assert.assertEquals(resultGrid, expected);
        Assert.assertEquals(289, resultPoints0);
        Assert.assertEquals(309, resultPoints1);
        Assert.assertEquals(tronGrid.MAX_X * tronGrid.MAX_Y - 2, resultPoints0 + resultPoints1);
    }

    @Test
    public void choiceTest() {
        //Given
        TronGrid tronGrid = TronGridBuilder.fromFile("src/test/resources/voronoiGrid/choice/given.txt", 6, 3);
        tronGrid.debug();
        MotoCycle motoCycle0 = new MotoCycle(0);
        motoCycle0.position = new Position(4, 1);

        //When
        VoronoiGrid resultGrid = new VoronoiGrid(tronGrid, 0, Collections.singletonList(motoCycle0));
        int resultPoints = resultGrid.countPoints(0);
        resultGrid.debug();

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
        tronGrid.debug();

        MotoCycle motoCycle0 = new MotoCycle(0);
        motoCycle0.position = new Position(1, 4);

        //When
        VoronoiGrid resultGrid = new VoronoiGrid(tronGrid, 0, Collections.singletonList(motoCycle0));
        int resultPoints = resultGrid.countPoints(0);
        resultGrid.debug();

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
        tronGrid.debug();
        MotoCycle motoCycle0 = new MotoCycle(0);
        motoCycle0.position = new Position(1, 3);

        //When
        VoronoiGrid resultGrid = new VoronoiGrid(tronGrid, 0, Collections.singletonList(motoCycle0));
        int resultPoints = resultGrid.countPoints(0);
        resultGrid.debug();

        //Then
        VoronoiGrid expectedGrid = expectedVoronoiGridfromFile("src/test/resources/voronoiGrid/clashPoints/expected.txt", 7, 6);
        int expectedPoints = 23;
        Assert.assertEquals(resultGrid, expectedGrid);
        Assert.assertEquals(expectedPoints, resultPoints);
    }
}