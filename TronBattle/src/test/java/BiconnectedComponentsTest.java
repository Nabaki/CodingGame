import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class BiconnectedComponentsTest {

    @Test
    public void fromBlankGrid() {
        //GIVEN
        BiconnectedComponents g = new BiconnectedComponents(12);
        g.addEdge(0, 1);
        g.addEdge(0, 3);
        g.addEdge(1, 0);
        g.addEdge(1, 2);
        g.addEdge(1, 4);
        g.addEdge(2, 1);
        g.addEdge(2, 5);
        g.addEdge(3, 0);
        g.addEdge(3, 4);
        g.addEdge(3, 6);
        g.addEdge(4, 1);
        g.addEdge(4, 3);
        g.addEdge(4, 5);
        g.addEdge(4, 7);
        g.addEdge(5, 2);
        g.addEdge(5, 4);
        g.addEdge(5, 8);
        g.addEdge(6, 3);
        g.addEdge(6, 7);
        g.addEdge(7, 6);
        g.addEdge(7, 4);
        g.addEdge(7, 8);
        g.addEdge(8, 7);
        g.addEdge(8, 5);

        //WHEN
        g.findBiconnectedComponents();

        //THEN
        assertThat(g.count).isEqualTo(1);
        assertThat(g.biconnectedComponents).hasSize(1);
        assertThat(g.biconnectedComponents.get(0))
                .containsExactly(
                        new BiconnectedComponents.Edge(0, 1),
                        new BiconnectedComponents.Edge(1, 2),
                        new BiconnectedComponents.Edge(2, 5),
                        new BiconnectedComponents.Edge(5, 4),
                        new BiconnectedComponents.Edge(4, 1),
                        new BiconnectedComponents.Edge(4, 3),
                        new BiconnectedComponents.Edge(3, 0),
                        new BiconnectedComponents.Edge(3, 6),
                        new BiconnectedComponents.Edge(6, 7),
                        new BiconnectedComponents.Edge(7, 4),
                        new BiconnectedComponents.Edge(7, 8),
                        new BiconnectedComponents.Edge(8, 5)
                );
    }

    @Test
    public void simpleCaseGrid() {
        //GIVEN
        BiconnectedComponents g = new BiconnectedComponents(15);
        g.addEdge(0, 1);
        g.addEdge(0, 3);
        g.addEdge(1, 0);
        g.addEdge(1, 2);
        g.addEdge(1, 4);
        g.addEdge(2, 1);
        g.addEdge(2, 5);
        g.addEdge(3, 0);
        g.addEdge(3, 4);
        g.addEdge(4, 1);
        g.addEdge(4, 3);
        g.addEdge(4, 5);
        g.addEdge(5, 2);
        g.addEdge(5, 4);
        g.addEdge(5, 8);
        g.addEdge(8, 5);
        g.addEdge(8, 11);
        g.addEdge(9, 10);
        g.addEdge(10, 9);
        g.addEdge(10, 11);
        g.addEdge(11, 8);
        g.addEdge(11, 10);

        //WHEN
        g.findBiconnectedComponents();

        //THEN
        assertThat(g.count).isEqualTo(5);
        assertThat(g.biconnectedComponents)
                .containsExactly(
                        Collections.singletonList(new BiconnectedComponents.Edge(10, 9)),
                        Collections.singletonList(new BiconnectedComponents.Edge(11, 10)),
                        Collections.singletonList(new BiconnectedComponents.Edge(8, 11)),
                        Collections.singletonList(new BiconnectedComponents.Edge(5, 8)),
                        Arrays.asList(
                                new BiconnectedComponents.Edge(0, 1),
                                new BiconnectedComponents.Edge(1, 2),
                                new BiconnectedComponents.Edge(2, 5),
                                new BiconnectedComponents.Edge(5, 4),
                                new BiconnectedComponents.Edge(4, 1),
                                new BiconnectedComponents.Edge(4, 3),
                                new BiconnectedComponents.Edge(3, 0)
                        )
                );
    }

}