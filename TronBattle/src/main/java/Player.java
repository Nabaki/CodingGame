import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

//238
class Player implements Cloneable {

    public final int id;
    public Location location;
    public boolean isDead = false;

    public Player(int id) {
        this.id = id;
    }

    public Player move(DirectionEnum directionEnum) {
        location.x += directionEnum.x;
        location.y += directionEnum.y;
        return this;
    }

    /**
     * Pour chaque direction proposée, le joueur va vérifier dans quelle situation il sera et prendre la plus avantageuse
     * Algorithme glouton car on prend juste le meilleur choix sur le coup.
     * TODO Hamiltonian Path to find the better path taking the most nodes
     * TODO Maximum flow ?
     */
    private DirectionEnum choixDirection(TronGrid tronGrid, List<DirectionEnum> directions) {

        int bestScore = -1;
        DirectionEnum bestDirection = null;

        /**
         * TODO détecter des angles
         * Différencier les angles moi-moi, ennemi-moi et ennemi-ennemi
         */
        for (DirectionEnum direction : directions) {
            Location newLocation = new Location(direction.x + location.x, direction.y + location.y);
            int tmpScore = scoreNewPosition(tronGrid, newLocation);
            System.err.println("Choix direction : " + direction + " -> " + tmpScore);
            if (bestDirection == null || tmpScore > bestScore) {
                bestDirection = direction;
                bestScore = tmpScore;
            }
        }
        return bestDirection;
    }

    public DirectionEnum bestMove(TronGrid tronGrid, List<Player> players) {
        //Choix entre l'algo quand on est seul ou quand on doit définir son territoire
        boolean isAlone = new LonelyGrid(tronGrid).isAlone(id, players);
        DirectionEnum finalMove;
        if (isAlone) {
            List<DirectionEnum> directions = new ArrayList<>(3);
            for (DirectionEnum directionEnum : DirectionEnum.values()) {
                Player tmpNewPlayer = clone().move(directionEnum);
                if (tronGrid.isValidePosition(tmpNewPlayer.location.x, tmpNewPlayer.location.y) && tronGrid.isEmpty(tmpNewPlayer.location.x, tmpNewPlayer.location.y)) {
                    directions.add(directionEnum);
                }
            }
            finalMove = choixDirection(tronGrid, directions);
        } else {
            List<DirectionEnum> moves = new ArrayList<>(3);
            Integer bestScore = null;
            for (DirectionEnum directionEnum : DirectionEnum.values()) {
                Player tmpNewPlayer = clone().move(directionEnum);
                if (tronGrid.isValidePosition(tmpNewPlayer.location.x, tmpNewPlayer.location.y) && tronGrid.isEmpty(tmpNewPlayer.location.x, tmpNewPlayer.location.y)) {
                    List<Player> tmpList = new ArrayList<>(players);
                    tmpList.set(id, tmpNewPlayer);
                    VoronoiGrid voronoiGrid = new VoronoiGrid(tronGrid, id, tmpList);
                    //voronoiGrid.printGrid();
                    int tmpScore = voronoiGrid.countPoints(id);
                    System.err.println("Voronoi " + directionEnum.name() + " -> " + tmpScore);
                    if (bestScore == null || bestScore < tmpScore) {
                        bestScore = tmpScore;
                        moves.clear();
                        moves.add(directionEnum);
                    } else if (bestScore == tmpScore) {
                        moves.add(directionEnum);
                    }
                }
            }

            if (moves.size() == 1) {
                finalMove = moves.get(0);
            } else {
                finalMove = choixDirection(tronGrid, moves);
            }
        }
        return finalMove;
    }

    /**
     * TODO détecter des angles
     * Différencier les angles moi-moi, ennemi-moi et ennemi-ennemi
     */
    private int scoreNewPosition(TronGrid tronGrid, Location newLocation) {
        List<DirectionEnum> connections = new ArrayList<>(3);
        List<DirectionEnum> ennemiWalls = new ArrayList<>(3);
        List<DirectionEnum> myWalls = new ArrayList<>(4);

        for (DirectionEnum directionEnum : DirectionEnum.values()) {
            int tmpX = newLocation.x + directionEnum.x;
            int tmpY = newLocation.y + directionEnum.y;
            boolean isValidePosition = tronGrid.isValidePosition(tmpX, tmpY);

            if (isValidePosition && tronGrid.isEmpty(tmpX, tmpY)) {
                connections.add(directionEnum);
            } else if (!isValidePosition || id == tronGrid.get(tmpX, tmpY)) {
                myWalls.add(directionEnum);
            } else if (tronGrid.get(tmpX, tmpY) != id) {
                ennemiWalls.add(directionEnum);
            } else {
                throw new IllegalStateException("TronGrid can't be in this state : " + tronGrid.get(tmpX, tmpY));
            }
        }

        int tmpScore;
        if (connections.size() == 0) {
            //Mort, pire des cas.
            tmpScore = 0;
        } else if (connections.size() == 1) {
            //Dans un couloir, pas forcément la meilleure idée...
            tmpScore = myWalls.size() - ennemiWalls.size();

                /*//Ennemi walls
                if (ennemiWalls.size() == 2) {
                    if (ennemiWalls.get(0).isAngleWith(ennemiWalls.get(1))) {
                        //Pire des angles
                    } else {
                        //Pire des couloirs
                    }
                }
                if (ennemiWalls.size() == 1) {
                    boolean myAngle = myWalls.get(0).isAngleWith(myWalls.get(1));
                    boolean ennemiAngle1 = myWalls.get(0).isAngleWith(ennemiWalls.get(0));
                    boolean ennemiAngle2 = myWalls.get(1).isAngleWith(ennemiWalls.get(0));

                    if(myAngle && ennemiAngle1){
                        //Mouette-mouette couloir
                    }

                    if (ennemiWalls.get(0).isAngleWith(myWalls.get(0))) {
                        //Mouette-mouette angle
                    } else {
                        //Mouette-mouette couloir
                    }
                }*/
        } else if (connections.size() == 2) {
            //Biconnection, risque de blocage.
            //On privilègie les chemins longeant les murs alliés qu'ennemi pour se débloquer si l'adversaire meurt.
            tmpScore = myWalls.size() - ennemiWalls.size();
        } else {
            //Connection libre, toujours mieux qu'une mort, généralement moins bien qu'un couloir mais risque de bloquer de nouvelles solutions
            tmpScore = myWalls.size();
        }

        System.err.println("Choix direction : " + newLocation + " -> " + tmpScore);
        return tmpScore;
    }

    public Map<DirectionEnum, Integer> scoreMoves(TronGrid tronGrid, List<Player> players) {

        //boolean isAlone = new LonelyGrid(tronGrid).isAlone(id, players);

        Map<DirectionEnum, Integer> scoreByMove = new HashMap<>(3);
        for (DirectionEnum directionEnum : DirectionEnum.values()) {
            Player tmpNewPlayer = clone().move(directionEnum);
            if (tronGrid.isValidePosition(tmpNewPlayer.location.x, tmpNewPlayer.location.y) && tronGrid.isEmpty(tmpNewPlayer.location.x, tmpNewPlayer.location.y)) {
                List<Player> tmpList = new ArrayList<>(players);
                tmpList.set(id, tmpNewPlayer);
                VoronoiGrid voronoiGrid = new VoronoiGrid(tronGrid, id, tmpList);
                //voronoiGrid.printGrid();
                int tmpScore = voronoiGrid.countPoints(id) * 100;
                System.err.println("Voronoi " + directionEnum.name() + " -> " + tmpScore);
                tmpScore += scoreNewPosition(tronGrid, tmpNewPlayer.location);
                scoreByMove.put(directionEnum, tmpScore);
            }
        }

        /*ArrayList<Map.Entry<DirectionEnum, Integer>> entries = new ArrayList<>(scoreByMove.entrySet());
        entries.sort(Map.Entry.comparingByValue((Integer v1, Integer v2) -> -Integer.compare(v1, v2)));*/

        return scoreByMove;
    }

    public static void main(String args[]) {
        //InitDatas
        TronGrid tronGrid = new TronGrid(30, 20);
        TronGrid tronBack = new TronGrid(tronGrid.MAX_X, tronGrid.MAX_Y);
        List<Player> players = null;
        boolean isInit = false;

        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {

            int nbPlayers = in.nextInt(); // total number of players (2 to 4).
            int myId = in.nextInt(); // your player number (0 to 3).

            if (!isInit) {
                players = new ArrayList<>(nbPlayers);
                for (int i = 0; i < nbPlayers; i++) {
                    players.add(new Player(i));
                }
                isInit = true;
            }

            for (int i = 0; i < nbPlayers; i++) {
                int X0 = in.nextInt(); // starting X coordinate of lightcycle (or -1)
                int Y0 = in.nextInt(); // starting Y coordinate of lightcycle (or -1)
                int X1 = in.nextInt(); // starting X coordinate of lightcycle (can be the same as X0 if you play before this player)
                int Y1 = in.nextInt(); // starting Y coordinate of lightcycle (can be the same as Y0 if you play before this player)

                Player player = players.get(i);

                //Si un des joueurs est mort
                if (X0 == -1) {
                    //Si c'est la première fois on le supprime de la grille
                    if (!player.isDead) {
                        tronGrid.resetGridForPlayerId(i);
                        player.isDead = true;
                    }
                } else {
                    tronGrid.set(X0, Y0, i);
                    tronGrid.set(X1, Y1, i);
                    player.location = new Location(X1, Y1);
                }
            }

            Instant start = Instant.now();

            Player myPlayer = players.get(myId);
            DirectionEnum finalMove = myPlayer.bestMove(tronGrid, players);
            System.err.println("---");

            //Keep safe
            tronBack.copy(tronGrid);

            Map<DirectionEnum, Integer> scoreMoves = players.get(myId).scoreMoves(tronGrid, players);
            while (Duration.between(start, Instant.now()).toMillis() < 70) {
                //Min

                //Max
                scoreMoves = players.get(myId).scoreMoves(tronGrid, players);
            }
            Optional<DirectionEnum> finalMoveBis =
                    scoreMoves.entrySet().stream()
                    .sorted((e1, e2) -> -e1.getValue().compareTo(e2.getValue()))
                    .findFirst()
                    .map(Map.Entry::getKey);

            //Timeout
            //First turn 1000ms
            //Other turns 100ms
            System.err.println("Time used : " + Duration.between(start, Instant.now()).toMillis() + "ms");

            System.out.println(finalMove); // A single line with UP, DOWN, LEFT or RIGHT
        }
    }

    @Override
    public Player clone() {
        Player player = new Player(id);
        player.location = new Location(location);
        player.isDead = this.isDead;
        return player;
    }

    @Override
    public String toString() {
        return "Player{" + location +
                ", isDead=" + isDead +
                ", id=" + id +
                '}';
    }
}

abstract class AbstractGrid<T> {

    private final T DEFAULT_VALUE;
    protected final int MAX_X;
    protected final int MAX_Y;

    protected List<List<T>> tab;

    protected AbstractGrid(int maxX, int maxY, T defaultValue) {
        MAX_X = maxX;
        MAX_Y = maxY;
        DEFAULT_VALUE = defaultValue;

        tab = new ArrayList<>(MAX_X);
        for (int x = 0; x < MAX_X; x++) {
            tab.add(new ArrayList<>(MAX_Y));
            for (int y = 0; y < MAX_Y; y++) {
                tab.get(x).add(DEFAULT_VALUE);
            }
        }
    }

    protected boolean isValidePosition(Location location) {
        return isValidePosition(location.x, location.y);
    }

    protected boolean isValidePosition(int x, int y) {
        return x >= 0 && y >= 0 && x < MAX_X && y < MAX_Y;
    }

    protected boolean isEmpty(Location location) {
        return isEmpty(location.x, location.y);
    }

    protected boolean isEmpty(int x, int y) {
        return get(x, y) == DEFAULT_VALUE;
    }

    protected void printGrid(String defaultString) {
        System.err.println("Print grid");
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                T node = get(x, y);
                if (!node.equals(DEFAULT_VALUE)) {
                    System.err.print(node.toString());
                } else {
                    System.err.print(defaultString);
                }
            }
            System.err.println("");
        }
    }

    protected T get(int x, int y) {
        return tab.get(x).get(y);
    }

    protected T get(Location location) {
        return get(location.x, location.y);
    }

    protected T set(int x, int y, T value) {
        return tab.get(x).set(y, value);
    }

    protected T set(Location location, T value) {
        return tab.get(location.x).set(location.y, value);
    }

    public abstract void printGrid();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractGrid)) return false;

        AbstractGrid<?> that = (AbstractGrid<?>) o;

        if (MAX_X != that.MAX_X) return false;
        if (MAX_Y != that.MAX_Y) return false;
        return tab != null ? tab.equals(that.tab) : that.tab == null;

    }

    @Override
    public int hashCode() {
        int result = MAX_X;
        result = 31 * result + MAX_Y;
        result = 31 * result + (tab != null ? tab.hashCode() : 0);
        return result;
    }
}

class TronGrid extends AbstractGrid<Integer> {

    public TronGrid(int maxX, int maxY) {
        super(maxX, maxY, -1);
    }

    public void printGrid() {
        printGrid("-");
    }

    public void resetGridForPlayerId(int idPlayer) {
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (!isEmpty(x, y) && get(x, y) == idPlayer) {
                    set(x, y, null);
                }
            }
        }
    }

    public void copy(TronGrid tronGrid) {
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                set(x, y, tronGrid.get(x, y));
            }
        }
    }
}

class LonelyGrid extends AbstractGrid<Boolean> {

    public LonelyGrid(TronGrid tronGrid) {
        super(tronGrid.MAX_X, tronGrid.MAX_Y, true);
    }

    public boolean isAlone(int id, List<Player> players) {
        List<Location> playersLocation = new ArrayList<>(50);
        players.stream()
                .filter(player -> player.id != id)
                .forEach(player -> playersLocation.add(player.location));

        return isAloneLoop(playersLocation, Arrays.asList(players.get(id).location));
    }

    private boolean isAloneLoop(List<Location> playersLocation, List<Location> locations) {
        List<Location> newLocations = new ArrayList<>(50);
        for (Location location : locations) {
            for (DirectionEnum directionEnum : DirectionEnum.values()) {
                Location newLocation = location.move(directionEnum);
                if (playersLocation.contains(newLocation)) {
                    return false;
                } else if (isValidePosition(newLocation) && isEmpty(newLocation)) {
                    set(newLocation, true);
                    newLocations.add(newLocation);
                }
            }
        }

        if (newLocations.isEmpty()) {
            return true;
        } else {
            return isAloneLoop(playersLocation, newLocations);
        }
    }

    @Override
    public void printGrid() {
        printGrid(Boolean.TRUE.toString());
    }
}

class VoronoiGrid extends AbstractGrid<Integer> {

    public VoronoiGrid(int maxX, int maxY) {
        super(maxX, maxY, -1);
    }

    /**
     * Voronoi
     * Algorithme récursif qui trouve les positions potentielles des joueurs atteintes à chaque tour+1.
     * Chaque récursion simule un tour de jeu.
     * A la fin la carte est séparée entre les murs(8), les zones atteintes en premier par tel ou tel joueur (0,1,2,3) ou des zones qui ne peuvent être atteintes (null)
     */
    public VoronoiGrid(TronGrid tronGrid, int firstPlayerToMove, List<Player> players) {
        super(tronGrid.MAX_X, tronGrid.MAX_Y, -1);

        //Init grid, walls become -1 on the grid
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (!tronGrid.isEmpty(x, y)) {
                    set(x, y, 8);
                }
            }
        }

        //Init player position
        List<List<Location>> locationsByPlayer = new ArrayList<>(4);
        for (Player player : players) {
            List<Location> locations = new ArrayList<>(1);
            if (!player.isDead) {
                set(player.location, 8);
                locations.add(player.location);
            }
            locationsByPlayer.add(locations);
        }

        int playerTurn = firstPlayerToMove;
        while (!locationsByPlayer.stream().allMatch(List::isEmpty)) {
            playerTurn = (playerTurn + 1) % players.size();
            locationsByPlayer.set(playerTurn, voronoiLoop(players.get(playerTurn), locationsByPlayer.get(playerTurn)));
        }
    }

    /**
     * Fait avancer le jeu d'un tick pour le joueur en question
     *
     * @param player    le joueur pour lequel c'est le tour
     * @param locations les emplacements où le joueur pourrait être à ce tour
     * @return les futurs emplacements où le joueur pourra être à son prochain tour ce joueurs
     */
    private List<Location> voronoiLoop(Player player, List<Location> locations) {
        if (locations.isEmpty()) {
            return locations;
        }

        List<Location> newLocations = new ArrayList<>(100);
        for (Location location : locations) {
            for (DirectionEnum directionEnum : DirectionEnum.values()) {
                Location newLocation = new Location(location.x + directionEnum.x, location.y + directionEnum.y);
                if (isValidePosition(newLocation) && isEmpty(newLocation)) {
                    set(newLocation, player.id);
                    newLocations.add(newLocation);
                }
            }
        }

        return newLocations;
    }

    public void printGrid() {
        printGrid(".");
    }

    //Connected-component_labeling
    public int countPoints(int targetPlayerId) {
        VoronoiGrid regionGrid = new VoronoiGrid(MAX_X, MAX_Y);
        List<List<Integer>> links = new ArrayList<>(10);
        int regionCounter = 0;

        //First pass
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (get(x, y) != null && get(x, y) == targetPlayerId) {
                    Location upLocation = new Location(x + DirectionEnum.UP.x, y + DirectionEnum.UP.y);
                    Location leftLocation = new Location(x + DirectionEnum.LEFT.x, y + DirectionEnum.LEFT.y);
                    Integer upRegionValue = null, leftRegionValue = null;
                    boolean upInRange = false, leftInRange = false;

                    if (isValidePosition(upLocation)) {
                        upInRange = get(upLocation) != null && get(upLocation).equals(targetPlayerId);
                        upRegionValue = regionGrid.get(upLocation);
                    }

                    if (isValidePosition(leftLocation)) {
                        leftInRange = get(leftLocation) != null && get(leftLocation).equals(targetPlayerId);
                        leftRegionValue = regionGrid.get(leftLocation);
                    }

                    if (!upInRange && !leftInRange) {
                        links.add(new ArrayList<>(Arrays.asList(regionCounter)));
                        regionGrid.set(x, y, regionCounter++);
                    } else if (upInRange && leftInRange) {
                        if (upRegionValue.equals(leftRegionValue)) {
                            regionGrid.set(x, y, upRegionValue);
                        } else {
                            boolean found = false;
                            for (List<Integer> link : links) {
                                if (link.contains(upRegionValue) && link.contains(leftRegionValue)) {
                                    regionGrid.set(x, y, leftRegionValue);
                                    found = true;
                                    break;
                                } else if (link.contains(upRegionValue)) {
                                    link.addAll(suppressRegion(links, leftRegionValue));
                                    regionGrid.set(x, y, upRegionValue);
                                    found = true;
                                    break;
                                } else if (link.contains(leftRegionValue)) {
                                    link.addAll(suppressRegion(links, upRegionValue));
                                    regionGrid.set(x, y, leftRegionValue);
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                regionGrid.set(x, y, leftRegionValue);
                                links.add(new ArrayList<>(Arrays.asList(upRegionValue, leftRegionValue)));
                            }
                        }
                    } else if (upInRange && !leftInRange) {
                        regionGrid.set(x, y, upRegionValue);
                    } else if (leftInRange && !upInRange) {
                        regionGrid.set(x, y, leftRegionValue);
                    }
                }
            }
        }

        //Second pass
        List<Integer> points = new ArrayList<Integer>(links.size()) {{
            for (int i = 0; i < links.size(); i++) {
                add(0);
            }
        }};

        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                Integer region = regionGrid.get(x, y);
                if (region != null) {
                    for (int i = 0; i < links.size(); i++) {
                        if (links.get(i).contains(region)) {
                            points.set(i, points.get(i) + 1);
                        }
                    }
                }
            }
        }

        if (points.isEmpty()) {
            return -1;
        } else {
            return new TreeSet<>(points).last();
        }
    }

    /**
     * @return the regionlinks suppressed
     */
    private List<Integer> suppressRegion(List<List<Integer>> regionsLinks, int regionToLink) {
        boolean linked = false;
        Iterator<List<Integer>> iterator = regionsLinks.iterator();
        List<Integer> tmpList = null;
        while (iterator.hasNext()) {
            tmpList = iterator.next();
            if (tmpList.contains(regionToLink)) {
                iterator.remove();
                linked = true;
                break;
            }
        }

        if (!linked) {
            return Arrays.asList(regionToLink);
        } else {
            return tmpList;
        }
    }
}

@Deprecated
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

@Deprecated
class DfsGrid extends AbstractGrid<DfsNode> {

    public DfsGrid(int maxX, int maxY) {
        super(maxX, maxY, null);
    }

    public DfsGrid(TronGrid tronGrid, Player player) {
        super(tronGrid.MAX_X, tronGrid.MAX_Y, null);
        init(tronGrid);
        populateDfs(player.location, 0);
        printGrid();
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

@Deprecated
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
@Deprecated
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

enum DirectionEnum {

    RIGHT(1, 0),
    UP(0, -1),
    LEFT(-1, 0),
    DOWN(0, 1);

    public int x;
    public int y;

    DirectionEnum(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isAngleWith(DirectionEnum directionEnum) {
        return (Math.abs(this.x) + Math.abs(directionEnum.x)) == 1 && Math.abs(this.y) + Math.abs(directionEnum.y) == 1;
    }

    public DirectionEnum opposite() {
        DirectionEnum res = null;
        switch (this) {
            case RIGHT:
                res = LEFT;
                break;
            case UP:
                res = DOWN;
                break;
            case LEFT:
                res = RIGHT;
                break;
            case DOWN:
                res = UP;
                break;
        }
        return res;
    }
}

class Location {
    public int x;
    public int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Location(Location location) {
        this.x = location.x;
        this.y = location.y;
    }

    public Location move(DirectionEnum directionEnum) {
        return new Location(x + directionEnum.x, y + directionEnum.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;

        Location location = (Location) o;

        return x == location.x && y == location.y;

    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "Location[" + x + ", " + y + ']';
    }
}

@Deprecated
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