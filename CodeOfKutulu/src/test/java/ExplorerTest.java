import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ExplorerTest {

    @Test
    public void allie_memeCase() {
        Player.kutuluGrid = GridBuilder.kutuluGridFromFile("src/test/resources/depthSearch/emptyLab/given.txt", 6, 5);
        Explorer givenExplorer1 = new Explorer(0, new Location(2, 2), 9999, 0, 0);
        Explorer givenExplorer2 = new Explorer(1, new Location(2, 2), 9999, 0, 0);

        List<Location> closerFriendLocations = givenExplorer1.closerFriendLocations(Collections.singletonList(givenExplorer2));
        assertThat(closerFriendLocations).isEqualTo(Collections.singletonList(new Location(2, 2)));
    }

    @Test
    public void allie_uneCase() {
        Player.kutuluGrid = GridBuilder.kutuluGridFromFile("src/test/resources/depthSearch/emptyLab/given.txt", 6, 5);
        Explorer givenExplorer1 = new Explorer(0, new Location(1, 1), 9999, 0, 0);
        Explorer givenExplorer2 = new Explorer(1, new Location(2, 2), 9999, 0, 0);

        List<Location> closerFriendLocations = givenExplorer1.closerFriendLocations(Collections.singletonList(givenExplorer2));
        assertThat(closerFriendLocations).isEqualTo(Arrays.asList(new Location(1, 2), new Location(2, 1)));
    }

    @Test
    public void allie_loin() {
        Player.kutuluGrid = GridBuilder.kutuluGridFromFile("src/test/resources/depthSearch/emptyLab/given.txt", 6, 5);
        Explorer givenExplorer1 = new Explorer(0, new Location(1, 1), 9999, 0, 0);
        Explorer givenExplorer2 = new Explorer(1, new Location(4, 3), 9999, 0, 0);

        List<Location> closerFriendLocations = givenExplorer1.closerFriendLocations(Collections.singletonList(givenExplorer2));
        assertThat(closerFriendLocations).isEqualTo(Arrays.asList(new Location(3, 3), new Location(4, 2)));
    }

    @Test
    public void allie_nonAccessible() {
        Player.kutuluGrid = GridBuilder.kutuluGridFromFile("src/test/resources/depthSearch/simpleCross/given.txt", 5, 5);
        Explorer givenExplorer1 = new Explorer(0, new Location(2, 2), 9999, 0, 0);
        Explorer givenExplorer2 = new Explorer(1, new Location(4, 4), 9999, 0, 0);

        List<Location> closerFriendLocations = givenExplorer1.closerFriendLocations(Collections.singletonList(givenExplorer2));
        assertThat(closerFriendLocations).isEmpty();
    }
}
