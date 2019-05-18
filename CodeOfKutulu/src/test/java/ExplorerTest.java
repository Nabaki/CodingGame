import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ExplorerTest {

    @Test
    public void allie_memeCase() {
        Player.kutuluGrid = GridBuilder.kutuluGridFromFile("src/test/resources/depthSearch/emptyLab/given.txt", 6, 6);
        Explorer givenExplorer1 = new Explorer(0, new Location(2, 2), 9999, 0, 0);
        Explorer givenExplorer2 = new Explorer(1, new Location(2, 2), 9999, 0, 0);

        Location friendRange = givenExplorer1.goInFriendRange(Collections.singletonList(givenExplorer2));
        assertThat(friendRange).isEqualTo(new Location(2, 2));
    }

    @Test
    public void allie_uneCase() {
        Player.kutuluGrid = GridBuilder.kutuluGridFromFile("src/test/resources/depthSearch/emptyLab/given.txt", 6, 6);
        Explorer givenExplorer1 = new Explorer(0, new Location(1, 1), 9999, 0, 0);
        Explorer givenExplorer2 = new Explorer(1, new Location(2, 2), 9999, 0, 0);

        Location friendRange = givenExplorer1.goInFriendRange(Collections.singletonList(givenExplorer2));
        assertThat(friendRange).isEqualTo(new Location(1, 1));
    }

    @Test
    public void allie_loin() {
        Player.kutuluGrid = GridBuilder.kutuluGridFromFile("src/test/resources/depthSearch/emptyLab/given.txt", 6, 6);
        Explorer givenExplorer1 = new Explorer(0, new Location(1, 1), 9999, 0, 0);
        Explorer givenExplorer2 = new Explorer(1, new Location(4, 4), 9999, 0, 0);

        Location friendRange = givenExplorer1.goInFriendRange(Collections.singletonList(givenExplorer2));
        assertThat(friendRange).isEqualTo(new Location(2, 4));
    }

    @Test
    public void allie_nonAccessible() {
        Player.kutuluGrid = GridBuilder.kutuluGridFromFile("src/test/resources/depthSearch/simpleCross/given.txt", 5, 5);
        Explorer givenExplorer1 = new Explorer(0, new Location(2, 2), 9999, 0, 0);
        Explorer givenExplorer2 = new Explorer(1, new Location(4, 4), 9999, 0, 0);

        Location friendRange = givenExplorer1.goInFriendRange(Collections.singletonList(givenExplorer2));
        assertThat(friendRange).isNull();
    }
}
