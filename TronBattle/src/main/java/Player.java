

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Player implements Cloneable {

    final int id;
    Location location;
    boolean isDead = false;

    Player(int id) {
        this.id = id;
    }

    private Player move(DirectionEnum directionEnum) {
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

        Integer bestScore = null;
        DirectionEnum bestDirection = null;

        for (DirectionEnum direction : directions) {
            Location newLocation = new Location(direction.x + location.x, direction.y + location.y);
            int tmpScore = scoreTypeNode(tronGrid, newLocation);
            System.err.println("Score type de noeud : " + direction + " -> " + tmpScore);
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
    private DirectionEnum bestMove(TronGrid tronGrid, List<Player> players) {
        boolean isAlone = new LonelyGrid(tronGrid).isAlone(id, players);
        System.err.println("isAlone : " + isAlone);

        //Liste des directions possibles
        List<DirectionEnum> directions = Arrays.stream(DirectionEnum.values())
                .filter(d -> {
                    Location newLocation = this.location.move(d);
                    return tronGrid.isValidePosition(newLocation) && tronGrid.isEmpty(newLocation);
                })
                .collect(Collectors.toList());

        System.err.println("Directions valides : " + directions);

        // Si l'on est seul on saute l'étape voronoi qui n'a pas trop d'intéret si on est seul
        if (!isAlone) {
            // Algorithme de guerre de territoire (Voronoi)
            TreeMap<Integer, List<DirectionEnum>> collect = directions.stream()
                    .collect(Collectors.groupingBy(
                            d -> useVoronoiInThisDirection(tronGrid, players, id, d),
                            TreeMap::new,
                            Collectors.toList()
                    ));

            directions = collect.lastEntry().getValue();
        }

        if (directions.size() == 1) {
            return directions.get(0);
        }
        return choixDirection(tronGrid, directions);
    }

    /**
     * Différencier les angles moi-moi, ennemi-moi et ennemi-ennemi.
     * On regarde si les cases autour de newLocation sont des murs alliés/ennemis, des cases vides
     */
    private int scoreTypeNode(TronGrid tronGrid, Location newLocation) {
        List<DirectionEnum> connections = new ArrayList<>(3);
        List<DirectionEnum> ennemiWalls = new ArrayList<>(3);
        List<DirectionEnum> myWalls = new ArrayList<>(4);

        for (DirectionEnum directionEnum : DirectionEnum.values()) {
            Location tmpLocation = newLocation.move(directionEnum);

            if (!tronGrid.isValidePosition(tmpLocation) || tronGrid.get(tmpLocation) == id) {
                myWalls.add(directionEnum);
            } else if (tronGrid.isEmpty(tmpLocation)) {
                connections.add(directionEnum);
            } else {
                ennemiWalls.add(directionEnum);
            }
        }

        int tmpScore;
        if (connections.size() == 0) {
            //Mort, pire des cas.
            tmpScore = 0;
        } else if (connections.size() == 1) {
            //Dans un couloir, pas forcément la meilleure idée...
            //Score = [-1, ..., 3]
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
            //Biconnection (dans un angle), risque de blocage.
            //On privilègie les chemins longeant les murs alliés qu'ennemi pour se débloquer si l'adversaire meurt.
            //Score = [0, 1].
            tmpScore = myWalls.size() - ennemiWalls.size();
        } else {
            //Connection libre, toujours mieux qu'une mort, généralement moins bien qu'un couloir mais risque de bloquer de nouvelles solutions
            //Score = 1
            tmpScore = myWalls.size();
        }

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

    int useVoronoiInThisDirection(TronGrid tronGrid, List<Player> players, int myId, DirectionEnum direction) {
        //La liste des joueurs et de leur position est mise à jour.
        List<Player> newPlayers = new ArrayList<>(players);
        newPlayers.set(myId, clone().move(direction));

        int voronoiScore = new VoronoiGrid(tronGrid, myId, newPlayers).countPoints(myId);
        System.err.println("Voronoi " + direction.name() + " -> " + voronoiScore);
        return voronoiScore;
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
    private final List<List<T>> tab;
    final int MAX_X;
    final int MAX_Y;

    AbstractGrid(int maxX, int maxY, T emptyNode) {
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

    boolean isValidePosition(Location location) {
        return isValidePosition(location.x, location.y);
    }

    boolean isValidePosition(int x, int y) {
        return x >= 0 && y >= 0 && x < MAX_X && y < MAX_Y;
    }

    boolean isEmpty(Location location) {
        return isEmpty(location.x, location.y);
    }

    boolean isEmpty(int x, int y) {
        return get(x, y) == EMPTY_NODE;
    }

    void printGrid(String defaultString) {
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
        System.err.println();
    }

    T get(int x, int y) {
        return tab.get(x).get(y);
    }

    T get(Location location) {
        return get(location.x, location.y);
    }

    void set(int x, int y, T value) {
        tab.get(x).set(y, value);
    }

    void set(Location location, T value) {
        set(location.x, location.y, value);
    }

    void setEmpty(int x, int y) {
        set(x, y, EMPTY_NODE);
    }

    void setEmpty(Location location) {
        setEmpty(location.x, location.y);
    }

    public abstract void printGrid();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractGrid<?> that = (AbstractGrid<?>) o;
        return MAX_X == that.MAX_X &&
                MAX_Y == that.MAX_Y &&
                Objects.equals(EMPTY_NODE, that.EMPTY_NODE) &&
                Objects.equals(tab, that.tab);
    }

    @Override
    public int hashCode() {
        return Objects.hash(EMPTY_NODE, tab, MAX_X, MAX_Y);
    }
}

class TronGrid extends AbstractGrid<Integer> implements Cloneable {

    TronGrid(int maxX, int maxY) {
        super(maxX, maxY, -1);
    }

    @Override
    public void printGrid() {
        printGrid("-");
    }

    void resetGridForPlayerId(int idPlayer) {
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (get(x, y) == idPlayer) {
                    setEmpty(x, y);
                }
            }
        }
    }

    @Override
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

    LonelyGrid(TronGrid tronGrid) {
        super(tronGrid.MAX_X, tronGrid.MAX_Y, true);

        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (!tronGrid.isEmpty(x, y)) {
                    set(x, y, false);
                }
            }
        }
    }

    boolean isAlone(int id, List<Player> players) {
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

    private static final int WALL = 8;

    /**
     * ATTENTION SEULEMENT POUR LES TESTS !
     */
    VoronoiGrid(int maxX, int maxY) {
        super(maxX, maxY, -1);
    }

    /**
     * Voronoi
     * Algorithme récursif qui trouve les positions potentielles des joueurs atteintes à chaque tour+1.
     * Chaque récursion simule un tour de jeu.
     * A la fin la carte est séparée entre les murs (WALL), les zones atteintes en premier par tel ou tel joueur (0,1,2,3) ou des zones qui ne peuvent être atteintes (null)
     */
    VoronoiGrid(TronGrid tronGrid, int lastPlayerToMove, List<Player> players) {
        super(tronGrid.MAX_X, tronGrid.MAX_Y, -1);

        //Init walls. If tronGrid is not empty then mark the location as 8 (wall)
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (!tronGrid.isEmpty(x, y)) {
                    set(x, y, WALL);
                }
            }
        }

        //Init players position.
        Map<Player, List<Location>> locationsByPlayer = new HashMap<>(players.size());
        players.forEach(p -> {
            List<Location> locations;
            if (!p.isDead) {
                set(p.location, WALL);
                locations = Collections.singletonList(p.location);
            } else {
                locations = Collections.emptyList();
            }
            locationsByPlayer.put(p, locations);
        });

        int idPlayerTurn = lastPlayerToMove;
        while (!locationsByPlayer.values().stream().allMatch(List::isEmpty)) {
            idPlayerTurn = (idPlayerTurn + 1) % players.size();
            Player playerTurn = players.get(idPlayerTurn);
            locationsByPlayer.put(playerTurn, voronoiLoop(playerTurn, locationsByPlayer.get(playerTurn)));
        }

        //printGrid();
    }

    /**
     * Fait avancer le jeu d'un tick pour le joueur en question
     *
     * @param player    le joueur pour lequel c'est le tour
     * @param locations les emplacements où le joueur pourrait être à ce tour
     * @return les futurs emplacements où le joueur pourra être à son prochain tour ce joueurs
     */
    private List<Location> voronoiLoop(Player player, List<Location> locations) {
        return locations.stream()
                .flatMap(l -> Arrays.stream(DirectionEnum.values()).map(l::move))
                .filter(l -> isValidePosition(l) && isEmpty(l))
                .peek(l -> set(l, player.id))
                .collect(Collectors.toList());
    }

    public void printGrid() {
        printGrid(".");
    }

    //Connected-component_labeling
    int countPoints(int targetPlayerId) {
        VoronoiGrid regionGrid = new VoronoiGrid(MAX_X, MAX_Y);
        List<List<Integer>> links = new ArrayList<>(10);
        int regionCounter = 0;

        //First pass
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (get(x, y) == targetPlayerId) {
                    Location upLocation = new Location(x + DirectionEnum.UP.x, y + DirectionEnum.UP.y);
                    Location leftLocation = new Location(x + DirectionEnum.LEFT.x, y + DirectionEnum.LEFT.y);
                    Integer upRegionValue = null;
                    Integer leftRegionValue = null;
                    boolean upInRange = false;
                    boolean leftInRange = false;

                    if (isValidePosition(upLocation)) {
                        upInRange = get(upLocation) == targetPlayerId;
                        upRegionValue = regionGrid.get(upLocation);
                    }

                    if (isValidePosition(leftLocation)) {
                        leftInRange = get(leftLocation) == targetPlayerId;
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
                    } else if (upInRange) { // && !leftInRange
                        regionGrid.set(x, y, upRegionValue);
                    } else { // leftInRange && !upInRange
                        regionGrid.set(x, y, leftRegionValue);
                    }
                }
            }
        }

        //Second pass
        List<Integer> points = Stream.generate(() -> 0).limit(links.size()).collect(Collectors.toList());

        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (!regionGrid.isEmpty(x, y)) {
                    for (int i = 0; i < links.size(); i++) {
                        if (links.get(i).contains(regionGrid.get(x, y))) {
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
    int x;
    int y;

    Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Location(Location location) {
        this.x = location.x;
        this.y = location.y;
    }

    Location move(DirectionEnum directionEnum) {
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