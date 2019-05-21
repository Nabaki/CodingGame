import java.util.*;


class DfsNode {
    Integer depth;
    Integer low;
    Position parent;
    boolean articulation = false;
    boolean wall = false;

    DfsNode() {
        this.wall = true;
    }

    DfsNode(int depth, int low) {
        this.depth = depth;
        this.low = low;
    }

    DfsNode(Position parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "[" + depth + "," + low + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DfsNode dfsNode = (DfsNode) o;
        return articulation == dfsNode.articulation &&
                Objects.equals(depth, dfsNode.depth) &&
                Objects.equals(low, dfsNode.low) &&
                Objects.equals(parent, dfsNode.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(depth, low, parent, articulation);
    }
}

class DfsGrid extends AbstractGrid<DfsNode> {

    DfsGrid(int maxX, int maxY) {
        super(maxX, maxY);
    }

    DfsGrid(TronGrid tronGrid, MotoCycle motoCycle) {
        super(tronGrid.MAX_X, tronGrid.MAX_Y);
        init(tronGrid);
        get(motoCycle.position).wall = false;
        populateDfs(motoCycle.position, 0);
    }

    private void init(TronGrid tronGrid) {
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (tronGrid.get(x, y) != null) {
                    set(x, y, new DfsNode());
                }
            }
        }
    }

    /**
     * Depth-First Search
     * https://en.wikipedia.org/wiki/Biconnected_component
     * Biconnected components
     */
    private void populateDfs(Position parentPosition, int depth) {
        DfsNode parent = get(parentPosition.x, parentPosition.y);
        parent.depth = depth;
        parent.low = depth;

        int childCount = 0;
        boolean isArticulation = false;
        for (DirectionEnum direction : DirectionEnum.values()) {
            Position childPosition = parentPosition.move(direction);

            if (isValidePosition(childPosition)) {
                DfsNode child = get(childPosition);
                if (child == null) { //Child is Empty
                    child = new DfsNode(parentPosition);
                    set(childPosition, child);
                    childCount++;

                    populateDfs(childPosition, depth + 1);
                    if (child.low >= parent.depth) {
                        isArticulation = true;
                    }
                    parent.low = Math.min(parent.low, child.low);

                } else if (!child.wall && (parent.parent == null || !child.equals(get(parent.parent)))) {
                    parent.low = Math.min(parent.low, child.depth);
                }
            }
        }

        if ((parent.parent != null && isArticulation) || (parent.parent == null && childCount > 1)) {
            parent.articulation = true;
        }
    }

    @Override
    public void debug() {
        debug(n -> n == null ? "  X  " : n.toString());
    }
}

class ConnectionNode {
    Set<DirectionEnum> openedConnections = new HashSet<>();
    Set<DirectionEnum> closedConnections = new HashSet<>();

    ConnectionNode(boolean wall) {
        if (wall) {
            closedConnections.addAll(Arrays.asList(DirectionEnum.values()));
        }
    }

    boolean isWall() {
        return closedConnections.size() == DirectionEnum.values().length;
    }

    boolean isProcessed() {
        return closedConnections.size() + openedConnections.size() == DirectionEnum.values().length;
    }

    @Override
    public String toString() {
        return String.valueOf(openedConnections.size());
    }
}

//https://fr.wikipedia.org/wiki/Algorithme_de_Kosaraju
class ConnectionGrid extends AbstractGrid<ConnectionNode> {

    ConnectionGrid(TronGrid tronGrid, List<MotoCycle> motoCycles) {
        super(tronGrid.MAX_X, tronGrid.MAX_Y);
        init(tronGrid, motoCycles);

        // Compute all connections
        //computeConnections1();

        //computes connections only from my Position
        computeConnections2(initComputeConnections2(motoCycles));
    }

    private void init(TronGrid tronGrid, List<MotoCycle> motoCycles) {
        motoCycles.stream().filter(motoCycle -> !motoCycle.isDead).forEach(motoCycle ->
                set(motoCycle.position.x, motoCycle.position.y, new ConnectionNode(true))
        );

        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (get(x, y) == null) {
                    if (tronGrid.get(x, y) == null) {
                        set(x, y, new ConnectionNode(false));
                    } else {
                        set(x, y, new ConnectionNode(true));
                    }
                }
            }
        }
    }

    private void computeConnections1() {
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (!get(x, y).isWall()) {
                    //See on right Side
                    computeConnection(x, y, DirectionEnum.RIGHT);

                    //See on down Side
                    computeConnection(x, y, DirectionEnum.DOWN);
                }
            }
        }
    }

    private Set<Position> initComputeConnections2(List<MotoCycle> motoCycles) {
        Set<Position> res = new HashSet<>();
        for (MotoCycle motoCycle : motoCycles) {
            for (DirectionEnum directionEnum : DirectionEnum.values()) {
                Position tmpPosition = new Position(motoCycle.position.x + directionEnum.x, motoCycle.position.y + directionEnum.y);
                if (isValidePosition(tmpPosition) && !get(tmpPosition).isWall()) {
                    res.add(tmpPosition);
                }
            }
        }
        return res;
    }

    private void computeConnections2(Set<Position> positions) {
        Set<Position> newPositions = new HashSet<>();
        positions.stream().filter(position -> !get(position).isWall()).forEach(position -> {
            for (DirectionEnum directionEnum : DirectionEnum.values()) {
                Position newPosition = new Position(position.x + directionEnum.x, position.y + directionEnum.y);
                if (isValidePosition(newPosition) && !get(position).isWall()) {
                    get(position).openedConnections.add(directionEnum);
                    if (!get(newPosition).isProcessed()) {
                        newPositions.add(newPosition);
                    }
                } else {
                    get(position).closedConnections.add(directionEnum);
                }
            }
        });

        if (!newPositions.isEmpty()) {
            computeConnections2(newPositions);
        }
    }

    private void computeConnection(int x, int y, DirectionEnum directionEnum) {
        int connectionX = x + directionEnum.x;
        int connectionY = y + directionEnum.y;
        if (isValidePosition(connectionX, connectionY)) {
            if (get(connectionX, connectionY).isWall()) {
                get(x, y).closedConnections.add(directionEnum);
                get(connectionX, connectionY).closedConnections.add(directionEnum.opposite());
            } else {
                get(x, y).openedConnections.add(directionEnum);
                get(connectionX, connectionY).openedConnections.add(directionEnum.opposite());
            }
        }
    }

    @Override
    public void debug() {
        debug(n -> n == null ? "X" : n.toString());
    }
}

class BiconnectedComponents {
    // Number of vertices & Edges respectively
    private final int size;
    // Adjacency List
    private final List<LinkedList<Integer>> adj;
    // Time is used to find discovery times
    private int time = 0;
    // Count is the number of biconnected components.
    int count = 0;

    List<List<Edge>> biconnectedComponents = new ArrayList<>();

    static class Edge {
        int u;
        int v;

        Edge(int u, int v) {
            this.u = u;
            this.v = v;
        }

        @Override
        public String toString() {
            return u + "--" + v;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return u == edge.u &&
                    v == edge.v;
        }

        @Override
        public int hashCode() {
            return Objects.hash(u, v);
        }
    }

    BiconnectedComponents(int size) {
        this.size = size;
        this.adj = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            adj.add(new LinkedList<>());
        }
    }

    //Function to add an edge into the graph
    void addEdge(int v, int w) {
        adj.get(v).add(w);
    }

    /**
     * A recursive function that finds and prints strongly connected components using DFS traversal.
     *
     * @param u      The vertex to be visited next
     * @param disc   Stores discovery times of visited vertices
     * @param low    Earliest visited vertex (the vertex with minimum discovery time) that can be reached from subtree rooted with current vertex
     * @param st     To store visited edges
     * @param parent
     */
    private void biconnectedComponentsLoop(int u, int[] disc, int[] low, LinkedList<Edge> st, int[] parent) {

        // Initialize discovery time and low value
        disc[u] = low[u] = ++time;
        int children = 0;

        // Go through all vertices adjacent to this
        for (int v : adj.get(u)) {
            // v is current adjacent of 'u'
            // If v is not visited yet, then recur for it
            if (disc[v] == -1) {
                children++;
                parent[v] = u;

                // store the edge in stack
                st.add(new Edge(u, v));
                biconnectedComponentsLoop(v, disc, low, st, parent);

                // Check if the subtree rooted with 'v' has a
                // connection to one of the ancestors of 'u'
                // Case 1 -- per Strongly Connected Components Article
                if (low[u] > low[v]) {
                    low[u] = low[v];
                }

                // If u is an articulation point, pop all edges from stack till u -- v
                if ((disc[u] == 1 && children > 1) || (disc[u] > 1 && low[v] >= disc[u])) {
                    List<Edge> edgeList = new LinkedList<>();
                    while (st.getLast().u != u || st.getLast().v != v) {
                        edgeList.add(st.removeLast());
                    }
                    edgeList.add(st.removeLast());

                    biconnectedComponents.add(edgeList);
                    count++;
                }
            }

            // Update low value of 'u' only of 'v' is still in stack
            // (i.e. it's a back edge, not cross edge).
            // Case 2 -- per Strongly Connected Components Article
            else if (v != parent[u] && disc[v] < low[u]) {
                if (low[u] > disc[v]) {
                    low[u] = disc[v];
                }
                st.add(new Edge(u, v));
            }
        }
    }

    void findBiconnectedComponents() {
        time = 0;
        count = 0;
        biconnectedComponents.clear();

        int[] disc = new int[size];
        int[] low = new int[size];
        int[] parent = new int[size];
        LinkedList<Edge> st = new LinkedList<>();

        // Initialize disc and low, and parent arrays
        for (int i = 0; i < size; i++) {
            disc[i] = -1;
            low[i] = -1;
            parent[i] = -1;
        }

        for (int i = 0; i < size; i++) {
            if (disc[i] == -1) {
                biconnectedComponentsLoop(i, disc, low, st, parent);
            }

            if (!st.isEmpty()) {
                biconnectedComponents.add(st);
                st = new LinkedList<>();
                count++;
            }
        }
    }
}