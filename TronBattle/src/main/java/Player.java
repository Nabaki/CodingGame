import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Player {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Game game = new Game();

        // game loop
        while (true) {

            game.update(in);

            Instant start = Instant.now();
            DirectionEnum finalMove = game.buildOutput();

            //Timeout
            //First turn 1000ms
            //Other turns 100ms
            System.err.println("Time used : " + Duration.between(start, Instant.now()).toMillis() + "ms");

            System.out.println(finalMove.name()); // A single line with UP, DOWN, LEFT or RIGHT
        }
    }
}

class Game {

    private int myId; // your motocycle number (0 to 3).
    private int nbMotocycles; // total number of motocycles (2 to 4).
    private boolean isInit = false;
    private TronGrid tronGrid = new TronGrid(30, 20);
    private List<Motocycle> motocycles = null;

    void update(Scanner in) {
        nbMotocycles = in.nextInt();
        myId = in.nextInt();

        if (!isInit) {
            motocycles = new ArrayList<>(nbMotocycles);
            for (int i = 0; i < nbMotocycles; i++) {
                motocycles.add(new Motocycle(i));
            }
            isInit = true;
        }

        for (int i = 0; i < nbMotocycles; i++) {
            int X0 = in.nextInt(); // starting X coordinate of lightcycle (or -1)
            int Y0 = in.nextInt(); // starting Y coordinate of lightcycle (or -1)
            int X1 = in.nextInt(); // starting X coordinate of lightcycle (can be the same as X0 if you play before this motocycle)
            int Y1 = in.nextInt(); // starting Y coordinate of lightcycle (can be the same as Y0 if you play before this motocycle)

            Motocycle motocycle = motocycles.get(i);

            //Si un des joueurs est mort
            if (X0 == -1) {
                //Si c'est la première fois on le supprime de la grille
                if (!motocycle.isDead) {
                    tronGrid.resetGridForMotocycleId(i);
                    motocycle.isDead = true;
                }
            } else {
                tronGrid.set(X0, Y0, i);
                tronGrid.set(X1, Y1, i);
                motocycle.position = new Position(X1, Y1);
            }
        }
    }

    DirectionEnum buildOutput() {
        Motocycle myMotocycle = motocycles.get(myId);
        return myMotocycle.bestMove(tronGrid, motocycles);
    }
}

class Motocycle implements Cloneable {

    final int id;
    Position position;
    boolean isDead = false;

    Motocycle(int id) {
        this.id = id;
    }

    private Motocycle move(DirectionEnum directionEnum) {
        position.x += directionEnum.x;
        position.y += directionEnum.y;
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
            Position newPosition = new Position(direction.x + position.x, direction.y + position.y);
            int tmpScore = scoreTypeNode(tronGrid, newPosition);
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
    DirectionEnum bestMove(TronGrid tronGrid, List<Motocycle> motocycles) {
        boolean isAlone = new LonelyGrid(tronGrid).isAlone(id, motocycles);
        System.err.println("isAlone : " + isAlone);

        //Liste des directions possibles
        List<DirectionEnum> directions = Arrays.stream(DirectionEnum.values())
                .filter(d -> {
                    Position newPosition = this.position.move(d);
                    return tronGrid.isValidePosition(newPosition) && tronGrid.get(newPosition) == null;
                })
                .collect(Collectors.toList());

        System.err.println("Directions valides : " + directions);

        // Si l'on est seul on saute l'étape voronoi qui n'a pas trop d'intéret si on est seul
        if (!isAlone) {
            // Algorithme de guerre de territoire (Voronoi)
            TreeMap<Integer, List<DirectionEnum>> collect = directions.stream()
                    .collect(Collectors.groupingBy(
                            d -> useVoronoiInThisDirection(tronGrid, motocycles, id, d),
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
     * On regarde si les cases autour de newPosition sont des murs alliés/ennemis, des cases vides
     */
    private int scoreTypeNode(TronGrid tronGrid, Position newPosition) {
        List<DirectionEnum> connections = new ArrayList<>(3);
        List<DirectionEnum> ennemiWalls = new ArrayList<>(3);
        List<DirectionEnum> myWalls = new ArrayList<>(4);

        for (DirectionEnum directionEnum : DirectionEnum.values()) {
            Position tmpPosition = newPosition.move(directionEnum);

            if (!tronGrid.isValidePosition(tmpPosition) || tronGrid.get(tmpPosition) == id) {
                myWalls.add(directionEnum);
            } else if (tronGrid.get(tmpPosition) == null) {
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

    private int useVoronoiInThisDirection(TronGrid tronGrid, List<Motocycle> motocycles, int myId, DirectionEnum direction) {
        //La liste des joueurs et de leur position est mise à jour.
        List<Motocycle> newMotocycles = new ArrayList<>(motocycles);
        newMotocycles.set(myId, clone().move(direction));

        int voronoiScore = new VoronoiGrid(tronGrid, myId, newMotocycles).countPoints(myId);
        System.err.println("Voronoi " + direction.name() + " -> " + voronoiScore);
        return voronoiScore;
    }

    @Override
    public Motocycle clone() {
        Motocycle motocycle = new Motocycle(id);
        motocycle.position = new Position(position);
        motocycle.isDead = this.isDead;
        return motocycle;
    }

    @Override
    public String toString() {
        return "Motocycle{" + position +
                ", isDead=" + isDead +
                ", id=" + id +
                '}';
    }
}

class TronGrid extends AbstractGrid<Integer> implements Cloneable {

    TronGrid(int maxX, int maxY) {
        super(maxX, maxY);
    }

    void resetGridForMotocycleId(int idMotocycle) {
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (get(x, y) == idMotocycle) {
                    set(x, y, null);
                }
            }
        }
    }

    @Override
    public void debug() {
        debug(n -> n == null ? "-" : String.valueOf(n));
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

/**
 * Permet de déterminer si un motocycle est seul.
 * Une case est 'true' si elle est occupée ou déjà visitée, sinon 'false'.
 * Chaque case 'true' est visitée en BFS à partir du motocycle ciblé puis devient 'false'.
 * Si une case visitée est celle d'un ennemi alors on arrète, le motocycle n'est pas seul.
 */
class LonelyGrid extends AbstractGrid<Boolean> {

    LonelyGrid(TronGrid tronGrid) {
        super(tronGrid.MAX_X, tronGrid.MAX_Y);

        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                set(x, y, tronGrid.get(x, y) == null);
            }
        }
    }

    boolean isAlone(int targetedId, List<Motocycle> motocycles) {
        List<Position> ennemisPosition = motocycles.stream()
                .filter(motocycle -> motocycle.id != targetedId)
                .map(motocycle -> motocycle.position)
                .collect(Collectors.toList());

        Position basePosition = motocycles.get(targetedId).position;
        set(basePosition, false);
        return isAloneLoop(ennemisPosition, Collections.singletonList(basePosition));
    }

    private boolean isAloneLoop(List<Position> ennemisPositions, List<Position> targetedPositions) {
        List<Position> newPositions = new ArrayList<>(50);
        for (Position position : targetedPositions) {
            for (DirectionEnum directionEnum : DirectionEnum.values()) {
                Position newPosition = position.move(directionEnum);
                if (ennemisPositions.contains(newPosition)) {
                    return false;
                } else if (isValidePosition(newPosition) && get(newPosition)) {
                    set(newPosition, false);
                    newPositions.add(newPosition);
                }
            }
        }

        if (newPositions.isEmpty()) {
            return true;
        } else {
            return isAloneLoop(ennemisPositions, newPositions);
        }
    }

    @Override
    public void debug() {
        debug(n -> n ? "1" : "0");
    }
}

class VoronoiGrid extends AbstractGrid<Integer> {

    private static final int WALL = 8;

    /**
     * ATTENTION SEULEMENT POUR LES TESTS !
     */
    VoronoiGrid(int maxX, int maxY) {
        super(maxX, maxY);
    }

    /**
     * Voronoi
     * Algorithme récursif qui trouve les positions potentielles des joueurs atteintes à chaque tour+1.
     * Chaque récursion simule un tour de jeu.
     * A la fin la carte est séparée entre les murs (WALL), les zones atteintes en premier par tel ou tel joueur (0,1,2,3) ou des zones qui ne peuvent être atteintes (null)
     */
    VoronoiGrid(TronGrid tronGrid, int lastMotocycleToMove, List<Motocycle> motocycles) {
        super(tronGrid.MAX_X, tronGrid.MAX_Y);

        //Init walls. If tronGrid is not empty then mark the position as 8 (wall)
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (tronGrid.get(x, y) != null) {
                    set(x, y, WALL);
                }
            }
        }

        //Init motocycles position.
        Map<Motocycle, List<Position>> positionsByMotocycle = new HashMap<>(motocycles.size());
        motocycles.forEach(motocycle -> {
            List<Position> positions;
            if (!motocycle.isDead) {
                set(motocycle.position, WALL);
                positions = Collections.singletonList(motocycle.position);
            } else {
                positions = Collections.emptyList();
            }
            positionsByMotocycle.put(motocycle, positions);
        });

        int idMotocycleTurn = lastMotocycleToMove;
        while (!positionsByMotocycle.values().stream().allMatch(List::isEmpty)) {
            idMotocycleTurn = (idMotocycleTurn + 1) % motocycles.size();
            Motocycle motocycleTurn = motocycles.get(idMotocycleTurn);
            positionsByMotocycle.put(motocycleTurn, voronoiLoop(motocycleTurn, positionsByMotocycle.get(motocycleTurn)));
        }

        //debug();
    }

    /**
     * Fait avancer le jeu d'un tick pour le joueur en question
     *
     * @param motocycle le joueur pour lequel c'est le tour
     * @param positions les emplacements où le joueur pourrait être à ce tour
     * @return les futurs emplacements où le joueur pourra être à son prochain tour ce joueurs
     */
    private List<Position> voronoiLoop(Motocycle motocycle, List<Position> positions) {
        return positions.stream()
                .flatMap(l -> Arrays.stream(DirectionEnum.values()).map(l::move))
                .filter(l -> isValidePosition(l) && get(l) == null)
                .peek(l -> set(l, motocycle.id))
                .collect(Collectors.toList());
    }

    public void debug() {
        debug(n -> n == null ? "." : String.valueOf(n));
    }

    /**
     * Counting maximum possible points which can be scored by a motocycle.
     * Done by using connected-component labeling only for nodes owned by by a targeted motocycle.
     * @param targetMotocycleId the id of the targeted motocycle.
     * @return maximum points which can be scored by targeted motocycle.
     */
    int countPoints(int targetMotocycleId) {
        VoronoiGrid regionGrid = new VoronoiGrid(MAX_X, MAX_Y);
        List<List<Integer>> links = new ArrayList<>(10);
        int regionCounter = 0;

        //First pass
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (get(x, y) == targetMotocycleId) {
                    Position upPosition = new Position(x + DirectionEnum.UP.x, y + DirectionEnum.UP.y);
                    Position leftPosition = new Position(x + DirectionEnum.LEFT.x, y + DirectionEnum.LEFT.y);
                    Integer upRegionValue = null;
                    Integer leftRegionValue = null;
                    boolean upInRange = false;
                    boolean leftInRange = false;

                    if (isValidePosition(upPosition)) {
                        upInRange = get(upPosition) == targetMotocycleId;
                        upRegionValue = regionGrid.get(upPosition);
                    }

                    if (isValidePosition(leftPosition)) {
                        leftInRange = get(leftPosition) == targetMotocycleId;
                        leftRegionValue = regionGrid.get(leftPosition);
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
                if (regionGrid.get(x, y) != null) {
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

class BiconnectedComponentLabelingGrid extends AbstractGrid<Integer>{

    private static final int WALL = 0;

    BiconnectedComponentLabelingGrid(TronGrid tronGrid, List<Motocycle> motocycles) {
        super(tronGrid.MAX_X, tronGrid.MAX_Y);

        //Init walls. If tronGrid is not empty then mark the position as WALL
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (tronGrid.get(x, y) != null) {
                    set(x, y, WALL);
                }
            }
        }

        //Les positions des motocles ne sont pas considérées comme occupées
        motocycles.forEach(motocycle ->
            set(motocycle.position, null)
        );
    }


    @Override
    public void debug() {
        debug(n -> String.format("%02d ", n));
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
        switch (this) {
            case RIGHT:
                return LEFT;
            case UP:
                return DOWN;
            case LEFT:
                return RIGHT;
            case DOWN:
                return UP;
            default:
                throw new IllegalStateException("Can't be anything else ?! " + this);
        }
    }
}

class Position {
    int x;
    int y;

    Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Position(Position position) {
        this.x = position.x;
        this.y = position.y;
    }

    Position move(DirectionEnum directionEnum) {
        return new Position(x + directionEnum.x, y + directionEnum.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;

        Position position = (Position) o;

        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "Position[" + x + ", " + y + ']';
    }
}

abstract class AbstractGrid<T> {

    private final List<List<T>> tab;
    final int MAX_X;
    final int MAX_Y;

    AbstractGrid(int maxX, int maxY) {
        MAX_X = maxX;
        MAX_Y = maxY;

        tab = new ArrayList<>(MAX_X);
        for (int x = 0; x < MAX_X; x++) {
            List<T> column = new ArrayList<>(MAX_Y);
            for (int y = 0; y < MAX_Y; y++) {
                column.add(null);
            }
            tab.add(column);
        }
    }

    boolean isValidePosition(Position position) {
        return isValidePosition(position.x, position.y);
    }

    boolean isValidePosition(int x, int y) {
        return x >= 0 && y >= 0 && x < MAX_X && y < MAX_Y;
    }

    void debug(Function<T, String> printNodeFunction) {
        System.err.println(this.getClass().getSimpleName());
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                T node = get(x, y);
                System.err.print(printNodeFunction.apply(node));
            }
            System.err.println();
        }
        System.err.println();
    }

    T get(int x, int y) {
        return tab.get(x).get(y);
    }

    T get(Position position) {
        return get(position.x, position.y);
    }

    void set(int x, int y, T value) {
        tab.get(x).set(y, value);
    }

    void set(Position position, T value) {
        set(position.x, position.y, value);
    }

    public abstract void debug();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractGrid<?> that = (AbstractGrid<?>) o;
        return MAX_X == that.MAX_X &&
                MAX_Y == that.MAX_Y &&
                Objects.equals(tab, that.tab);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tab, MAX_X, MAX_Y);
    }
}