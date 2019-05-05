import java.util.*;


class DfsNode {
    public int depth;
    public int low;
    public Location parent;
    public boolean articulation = false;

    public DfsNode() {
        this.depth = -1;
        this.low = -1;
        this.parent = null;
    }

    public DfsNode(int depth, int low) {
        this.depth = depth;
        this.low = low;
    }

    public DfsNode(Location location) {
        this.parent = location;
    }

    @Override
    public String toString() {
        return "[" + depth + "," + low + ']';
    }

    public static DfsNode fromString(String dfsNode) {
        String[] datas = dfsNode.split(",");
        return new DfsNode(Integer.parseInt(datas[0]), Integer.parseInt(datas[1]));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DfsNode)) return false;

        DfsNode dfsNode = (DfsNode) o;

        if (depth != dfsNode.depth) return false;
        if (low != dfsNode.low) return false;
        if (articulation != dfsNode.articulation) return false;
        return parent != null ? parent.equals(dfsNode.parent) : dfsNode.parent == null;

    }

    @Override
    public int hashCode() {
        int result = depth;
        result = 31 * result + low;
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (articulation ? 1 : 0);
        return result;
    }
}



class DfsGrid extends AbstractGrid<DfsNode> {

    public DfsGrid(int maxX, int maxY) {
        super(maxX, maxY, null);
    }

    public DfsGrid(TronGrid tronGrid, Player player) {
        super(tronGrid.MAX_X, tronGrid.MAX_Y, null);
        init(tronGrid);
        populateDfs(player.location, 0);
    }

    private void init(TronGrid tronGrid) {
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (!tronGrid.isEmpty(x, y)) {
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
    private void populateDfs(Location parentLocation, int depth) {
        DfsNode parent = get(parentLocation.x, parentLocation.y);
        parent.depth = depth;
        parent.low = depth;

        //printGrid();

        int childCount = 0;
        boolean isArticulation = false;
        for (DirectionEnum direction : DirectionEnum.values()) {
            int childX = parentLocation.x + direction.x;
            int childY = parentLocation.y + direction.y;

            if (isValidePosition(childX, childY)) {
                if (isEmpty(childX, childY)) {
                    DfsNode child = new DfsNode(parentLocation);
                    set(childX, childY, child);
                    childCount++;

                    populateDfs(new Location(childX, childY), depth + 1);
                    if (child.low >= parent.depth) {
                        isArticulation = true;
                    }
                    parent.low = Math.min(child.low, parent.low);

                } else if (get(childX, childY).depth != -1 && !parentLocation.equals(get(childX, childY).parent)) {
                    parent.low = Math.min(get(childX, childY).depth, parent.low);
                }
            }
        }

        if ((parent.parent != null && isArticulation) || (parent.parent == null && childCount > 1)) {
            parent.articulation = true;
        }

        printGrid();
    }

    @Override
    public void printGrid() {
        printGrid("  X  ");
    }
}

class ConnectionNode {
    public Set<DirectionEnum> openedConnections = new HashSet<>();
    public Set<DirectionEnum> closedConnections = new HashSet<>();

    public ConnectionNode(boolean wall) {
        if (wall) {
            closedConnections.addAll(Arrays.asList(DirectionEnum.values()));
        }
    }

    public boolean isWall() {
        return closedConnections.size() == DirectionEnum.values().length;
    }

    public boolean isProcessed() {
        return closedConnections.size() + openedConnections.size() == DirectionEnum.values().length;
    }

    @Override
    public String toString() {
        return String.valueOf(openedConnections.size());
    }
}

//https://fr.wikipedia.org/wiki/Algorithme_de_Kosaraju
class ConnectionGrid extends AbstractGrid<ConnectionNode> {

    public ConnectionGrid(TronGrid tronGrid, List<Player> players, int myId) {
        super(tronGrid.MAX_X, tronGrid.MAX_Y, null);
        init(tronGrid, players);

        // Compute all connections
        //computeConnections1();

        //computes connections only from my Location
        computeConnections2(initComputeConnections2(players));
    }

    private void init(TronGrid tronGrid, List<Player> players) {
        players.stream().filter(player -> !player.isDead).forEach(player ->
                set(player.location.x, player.location.y, new ConnectionNode(true))
        );

        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (isEmpty(x, y)) {
                    if (tronGrid.isEmpty(x, y)) {
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

    private Set<Location> initComputeConnections2(List<Player> players) {
        Set<Location> res = new HashSet<>();
        for (Player player : players) {
            for (DirectionEnum directionEnum : DirectionEnum.values()) {
                Location tmpLocation = new Location(player.location.x + directionEnum.x, player.location.y + directionEnum.y);
                if (isValidePosition(tmpLocation) && !get(tmpLocation).isWall()) {
                    res.add(tmpLocation);
                }
            }
        }
        return res;
    }

    private void computeConnections2(Set<Location> locations) {
        Set<Location> newLocations = new HashSet<>();
        locations.stream().filter(location -> !get(location).isWall()).forEach(location -> {
            for (DirectionEnum directionEnum : DirectionEnum.values()) {
                Location newLocation = new Location(location.x + directionEnum.x, location.y + directionEnum.y);
                if (isValidePosition(newLocation) && !get(location).isWall()) {
                    get(location).openedConnections.add(directionEnum);
                    if (!get(newLocation).isProcessed()) {
                        newLocations.add(newLocation);
                    }
                } else {
                    get(location).closedConnections.add(directionEnum);
                }
            }
        });

        if (!newLocations.isEmpty()) {
            computeConnections2(newLocations);
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
    public void printGrid() {
        printGrid("X");
    }
}

class BiconnectedComponents {
    // No. of vertices & Edges respectively
    private int size;
    // Adjacency List
    private LinkedList[] adj;

    // Count is number of biconnected components. time is
    // used to find discovery times
    static int count = 0, time = 0;

    class Edge {
        int u;
        int v;

        Edge(int u, int v) {
            this.u = u;
            this.v = v;
        }
    }

    //Constructor
    BiconnectedComponents(int v) {
        size = v;
        adj = new LinkedList[v];
        for (int i = 0; i < v; ++i)
            adj[i] = new LinkedList();
    }

    //Function to add an edge into the graph
    void addEdge(int v, int w) {
        adj[v].add(w);
    }

    // A recursive function that finds and prints strongly connected
    // components using DFS traversal
    // u --> The vertex to be visited next
    // disc[] --> Stores discovery times of visited vertices
    // low[] -- >> earliest visited vertex (the vertex with minimum
    //             discovery time) that can be reached from subtree
    //             rooted with current vertex
    // *st -- >> To store visited edges
    void BCCUtil(int u, int disc[], int low[], LinkedList<Edge> st, int parent[]) {

        // Initialize discovery time and low value
        disc[u] = low[u] = ++time;
        int children = 0;

        // Go through all vertices adjacent to this
        Iterator<Integer> it = adj[u].iterator();
        while (it.hasNext()) {
            // v is current adjacent of 'u'
            int v = it.next();

            // If v is not visited yet, then recur for it
            if (disc[v] == -1) {
                children++;
                parent[v] = u;

                // store the edge in stack
                st.add(new Edge(u, v));
                BCCUtil(v, disc, low, st, parent);

                // Check if the subtree rooted with 'v' has a
                // connection to one of the ancestors of 'u'
                // Case 1 -- per Strongly Connected Components Article
                if (low[u] > low[v])
                    low[u] = low[v];

                // If u is an articulation point,
                // pop all edges from stack till u -- v
                if ((disc[u] == 1 && children > 1) ||
                        (disc[u] > 1 && low[v] >= disc[u])) {
                    while (st.getLast().u != u || st.getLast().v != v) {
                        System.out.print(st.getLast().u + "--" +
                                st.getLast().v + " ");
                        st.removeLast();
                    }
                    System.out.println(st.getLast().u + "--" +
                            st.getLast().v + " ");
                    st.removeLast();

                    count++;
                }
            }

            // Update low value of 'u' only of 'v' is still in stack
            // (i.e. it's a back edge, not cross edge).
            // Case 2 -- per Strongly Connected Components Article
            else if (v != parent[u] && disc[v] < low[u]) {
                if (low[u] > disc[v])
                    low[u] = disc[v];
                st.add(new Edge(u, v));
            }
        }
    }

    // The function to do DFS traversal. It uses BCCUtil()
    void BCC() {
        int disc[] = new int[size];
        int low[] = new int[size];
        int parent[] = new int[size];
        LinkedList<Edge> st = new LinkedList<>();

        // Initialize disc and low, and parent arrays
        for (int i = 0; i < size; i++) {
            disc[i] = -1;
            low[i] = -1;
            parent[i] = -1;
        }

        for (int i = 0; i < size; i++) {
            if (disc[i] == -1)
                BCCUtil(i, disc, low, st, parent);

            int j = 0;

            // If stack is not empty, pop all edges from stack
            while (st.size() > 0) {
                j = 1;
                System.out.print(st.getLast().u + "--" +
                        st.getLast().v + " ");
                st.removeLast();
            }
            if (j == 1) {
                System.out.println();
                count++;
            }
        }
    }

    public static void main(String args[]) {
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

        g.BCC();

        System.out.println("Above are " + g.count +
                " biconnected components in graph");
    }
}