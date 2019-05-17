import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class DepthSearchTest {

    @Test
    public void dfs_emptyLab() {
        KutuluGrid given = GridBuilder.kutuluGridFromFile("src/test/resources/depthSearch/emptyLab/given.txt", 6, 5);
        System.out.println("Given");
        given.printGrid();

        Player.kutuluGrid = given;
        DepthGrid depthGrid = new DepthGrid(6, 5, new Location(1, 1));
        System.out.println("When");
        depthGrid.printGrid();

        DepthGrid expected = GridBuilder.depthGridFromFile("src/test/resources/depthSearch/emptyLab/expected.txt", 6, 5);
        System.out.println("Expected");
        expected.printGrid();

        assertThat(depthGrid).isEqualTo(expected);
    }

    @Test
    public void dfs_crossLab() {
        KutuluGrid given = GridBuilder.kutuluGridFromFile("src/test/resources/depthSearch/simpleCross/given.txt", 5, 5);
        System.out.println("Given");
        given.printGrid();

        Player.kutuluGrid = given;
        DepthGrid depthGrid = new DepthGrid(5, 5, new Location(2, 2));
        System.out.println("When");
        depthGrid.printGrid();

        DepthGrid expected = GridBuilder.depthGridFromFile("src/test/resources/depthSearch/simpleCross/expected.txt", 5, 5);
        System.out.println("Expected");
        expected.printGrid();

        assertThat(depthGrid).isEqualTo(expected);
    }

    @Test
    public void dfs_assymLab() {
        KutuluGrid given = GridBuilder.kutuluGridFromFile("src/test/resources/depthSearch/assymLab/given.txt", 9, 7);
        System.out.println("Given");
        given.printGrid();

        Player.kutuluGrid = given;
        DepthGrid depthGrid = new DepthGrid(9, 7, new Location(5, 1));
        System.out.println("When");
        depthGrid.printGrid();

        DepthGrid expected = GridBuilder.depthGridFromFile("src/test/resources/depthSearch/assymLab/expected.txt", 9, 7);
        System.out.println("Expected");
        expected.printGrid();

        assertThat(depthGrid).isEqualTo(expected);
    }
}
