import javafx.util.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
     * Algorithme glouton car on prend juste le meilleur choix sur le coup sans jouer plusieurs coups en avance.
     * TODO détecter des angles, différencier les angles moi-moi, ennemi-moi et ennemi-ennemi
     * TODO Hamiltonian Path to find the better path taking the most nodes
     * TODO Maximum flow ?
     */
    private DirectionEnum choixDirection(TronGrid tronGrid, List<DirectionEnum> directions) {

        int bestScore = -1;
        DirectionEnum bestDirection = null;

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

    /**
     * Choix de la meilleur direction à prendre en fonction de si l'on est en mode "survie seul" ou "guerre de territoire"
     */
    public DirectionEnum bestMove(TronGrid tronGrid, List<Player> players) {
        //boolean isAlone = new LonelyGrid(voronoiGrid).isAlone(id, players);
        //System.err.println("isAlone : " + isAlone);
        boolean isAlone = false;

        DirectionEnum finalMove;
        if (isAlone) {
            //TODO à vérifier si c'est pertinant
            // Algorithme de survie seul -> Toutes les positions sont vérifiées
            List<DirectionEnum> directions = Arrays.stream(DirectionEnum.values())
                    .map(d -> new Pair<>(d, this.location.move(d)))
                    .filter(p -> tronGrid.isValidePosition(p.getKey().x, p.getKey().y) && tronGrid.isEmpty(p.getKey().x, p.getKey().y))
                    .map(Pair::getKey)
                    .collect(Collectors.toList());

            finalMove = choixDirection(tronGrid, directions);
        } else {
            // Algorithme de guerre de territoire
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

    public static void main(String[] args) {
        //InitDatas
        TronGrid tronGrid = new TronGrid(30, 20);
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

    private final T EMPTY_NODE;
    protected final int MAX_X;
    protected final int MAX_Y;

    protected List<List<T>> tab;

    protected AbstractGrid(int maxX, int maxY, T emptyNode) {
        MAX_X = maxX;
        MAX_Y = maxY;
        EMPTY_NODE = emptyNode;

        tab = new ArrayList<>(MAX_X);
        for (int x = 0; x < MAX_X; x++) {
            tab.add(new ArrayList<>(MAX_Y));
            for (int y = 0; y < MAX_Y; y++) {
                tab.get(x).add(EMPTY_NODE);
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
        return get(x, y) == EMPTY_NODE;
    }

    protected void printGrid(String defaultString) {
        System.err.println("Print grid");
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                T node = get(x, y);
                if (!node.equals(EMPTY_NODE)) {
                    System.err.print(node.toString());
                } else {
                    System.err.print(defaultString);
                }
            }
            System.err.println();
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
        return set(location.x, location.y, value);
    }

    protected T setEmpty(int x, int y) {
        return set(x, y, EMPTY_NODE);
    }

    protected T setEmpty(Location location) {
        return setEmpty(location.x, location.y);
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

    @Override
    public void printGrid() {
        printGrid("-");
    }

    public void resetGridForPlayerId(int idPlayer) {
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (get(x, y) == idPlayer) {
                    setEmpty(x, y);
                }
            }
        }
    }

    public TronGrid clone() {
        TronGrid clone = new TronGrid(this.MAX_X, this.MAX_Y);
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                clone.set(x, y, this.get(x, y));
            }
        }
        return clone;
    }
}

class LonelyGrid extends AbstractGrid<Boolean> {

    //FIXME Utiliser VoronoiGrid plutôt ?
    public LonelyGrid(VoronoiGrid voronoiGrid) {
        super(voronoiGrid.MAX_X, voronoiGrid.MAX_Y, true);

        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if(voronoiGrid.get(x, y) == VoronoiGrid.WALL){
                    set(x, y, false);
                }
            }
        }
    }

    public boolean isAlone(int id, List<Player> players) {
        List<Location> ennemisLocation = players.stream()
                .filter(player -> player.id != id)
                .map(player -> player.location)
                .collect(Collectors.toList());

        Location myLocation = players.get(id).location;
        set(myLocation, false);
        return isAloneLoop(ennemisLocation, Collections.singletonList(myLocation));
    }

    private boolean isAloneLoop(List<Location> ennemisLocations, List<Location> myLocations) {
        List<Location> newLocations = new ArrayList<>(50);
        for (Location location : myLocations) {
            for (DirectionEnum directionEnum : DirectionEnum.values()) {
                Location newLocation = location.move(directionEnum);
                if (ennemisLocations.contains(newLocation)) {
                    return false;
                } else if (isValidePosition(newLocation) && isEmpty(newLocation)) {
                    set(newLocation, false);
                    newLocations.add(newLocation);
                }
            }
        }

        if (newLocations.isEmpty()) {
            return true;
        } else {
            return isAloneLoop(ennemisLocations, newLocations);
        }
    }

    @Override
    public void printGrid() {
        printGrid(Boolean.TRUE.toString());
    }
}

class VoronoiGrid extends AbstractGrid<Integer> {

    public static final int WALL = 8;

    /**
     * ATTENTION SEULEMENT POUR LES TESTS !
     */
    public VoronoiGrid(int maxX, int maxY) {
        super(maxX, maxY, -1);
    }

    /**
     * Voronoi
     * Algorithme récursif qui trouve les positions potentielles des joueurs atteintes à chaque tour+1.
     * Chaque récursion simule un tour de jeu.
     * A la fin la carte est séparée entre les murs (WALL), les zones atteintes en premier par tel ou tel joueur (0,1,2,3) ou des zones qui ne peuvent être atteintes (null)
     */
    public VoronoiGrid(TronGrid tronGrid, int firstPlayerToMove, List<Player> players) {
        super(tronGrid.MAX_X, tronGrid.MAX_Y, -1);

        //Init walls. If tronGrid is not empty then mark the location as 8 (wall)
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (!tronGrid.isEmpty(x, y)) {
                    set(x, y, WALL);
                }
            }
        }
        //Init player position.
        List<List<Location>> locationsByPlayer = new ArrayList<>(4);
        for (Player player : players) {
            List<Location> locations = new ArrayList<>(1);
            if (!player.isDead) {
                set(player.location, WALL);
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
                        links.add(new ArrayList<>(Collections.singletonList(regionCounter)));
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
            return Collections.singletonList(regionToLink);
        } else {
            return tmpList;
        }
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