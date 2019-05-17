import javafx.util.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Survive the wrath of Kutulu
 * Coded fearlessly by JohnnyYuge & nmahoude (ok we might have been a bit scared by the old god...but don't say anything)
 **/
class Player {

    static KutuluGrid kutuluGrid;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int width = in.nextInt();
        int height = in.nextInt();
        if (in.hasNextLine()) {
            in.nextLine();
        }

        kutuluGrid = new KutuluGrid(width, height);
        for (int i = 0; i < height; i++) {
            kutuluGrid.formatLine(i, in.nextLine());
        }
        int sanityLossLonely = in.nextInt(); // how much sanity you lose every turn when alone, always 3 until wood 1
        int sanityLossGroup = in.nextInt(); // how much sanity you lose every turn when near another player, always 1 until wood 1
        int wandererSpawnTime = in.nextInt(); // how many turns the wanderer take to spawn, always 3 until wood 1
        int wandererLifeTime = in.nextInt(); // how many turns the wanderer is on map after spawning, always 40 until wood 1

        // game loop
        while (true) {
            Explorer me = null;
            List<Explorer> explorers = new ArrayList<>();
            List<Wanderer> wanderers = new ArrayList<>();
            List<Slasher> slashers = new ArrayList<>();
            List<Monster> monsters = new ArrayList<>();

            int entityCount = in.nextInt(); // the first given entity corresponds to your explorer
            for (int i = 0; i < entityCount; i++) {
                EntityType entityType = EntityType.valueOf(in.next());
                int id = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
                int param0 = in.nextInt();
                int param1 = in.nextInt();
                int param2 = in.nextInt();

                switch (entityType) {
                    case EXPLORER:
                        Explorer newExplorer = new Explorer(id, new Location(x, y), param0, param1, param2);
                        if (me == null) {
                            me = newExplorer;
                        } else {
                            explorers.add(newExplorer);
                        }
                    case WANDERER:
                        Wanderer wanderer = new Wanderer(id, new Location(x, y), MinionStateEnum.stateOf(param1), param0, param2);
                        wanderers.add(wanderer);
                        monsters.add(wanderer);
                    case EFFECT_PLAN:
                        break;
                    case EFFECT_LIGHT:
                        break;
                    case SLASHER:
                        Slasher slasher = new Slasher(id, new Location(x, y), MinionStateEnum.stateOf(param1), param0, param2);
                        slashers.add(slasher);
                        monsters.add(slasher);
                        break;
                    case EFFECT_SHELTER:
                        break;
                    case EFFECT_YELL:
                        break;
                }
            }

            //Process
            Location moveTo = null;
            if (explorers.isEmpty()) {
                Explorer finalMe = me;
                boolean friendsAreClose = explorers.stream().anyMatch(e -> e.isCloseTo(finalMe, 2));
                if (friendsAreClose) {
                    System.err.println("Serve me as shield");
                    moveTo = me.runFromMonsters(monsters, explorers);
                } else {
                    System.err.println("Look for friends");
                    moveTo = me.goInFriendRange(explorers);
                }
            }
            // Write an action using println
            // To debug: Console.err.println("Debug messages...")
            if (moveTo == null) {
                System.out.println("WAIT");
            } else {
                System.out.println("MOVE " + moveTo.x + " " + moveTo.y);
            }
        }
    }
}

abstract class Entity {
    int id;
    Location location;
    DepthGrid depthGrid;

    Entity(int id, Location location) {
        this.id = id;
        this.location = location;

        //FIXME PAS BESOIN DE LE FAIRE SYSTEMATIQUEMENT
        this.depthGrid = new DepthGrid(Player.kutuluGrid.MAX_X, Player.kutuluGrid.MAX_Y, location);
    }

    boolean isCloseTo(Entity entity, int distance) {
        return this.location.distanceWith(entity.location) <= distance;
    }

    /**
     * Location où on doit aller pour être à depth de distance du startPoint par rapport à l'entité en question
     */
    Location runFromHim(Location startPoint, int depth) {
        int startDepth = this.depthGrid.get(startPoint);
        List<Location> res = Collections.singletonList(startPoint);
        for (int i = 0; i < depth; i++) {
            res = res.stream()
                    .flatMap(location -> Arrays.stream(DirectionEnum.values()).map(location::move))
                    .filter(newLocation ->
                            //On garde seulement les cas où l'on s'éloigne
                            startDepth < depthGrid.get(newLocation)
                    ).collect(Collectors.toList());
        }
        return res.isEmpty() ? null : res.get(0);
    }

    List<Location> findPathTo(Location destination) {
        List<Location> res = findPathLoop(Collections.singletonList(destination), depthGrid.get(destination));

        if (!res.isEmpty() && depthGrid.get(res.get(res.size() - 1)) != 0) {
            return Collections.emptyList();
        }
        return res;
    }

    private List<Location> findPathLoop(List<Location> destinations, int destinationsDepth) {

        List<Location> res = destinations.stream()
                .flatMap(destination -> Arrays.stream(DirectionEnum.values()).map(destination::move))
                .filter(location ->
                        depthGrid.isValideLocation(location)
                                && depthGrid.get(location) != null
                                && depthGrid.get(location) < destinationsDepth
                ).collect(Collectors.toList());

        if (!res.isEmpty() && destinationsDepth != 0) {
            res.addAll(findPathLoop(res, destinationsDepth - 1));
        }
        return res;
    }
}

class Portal {
}

enum Action {
    WAIT, MOVE
}

enum EntityType {
    EXPLORER,
    WANDERER,
    EFFECT_PLAN,
    EFFECT_LIGHT,
    SLASHER,
    EFFECT_SHELTER,
    EFFECT_YELL
}

/**
 * # : mur
 * w : portail d'invocation
 * . : case vide
 */
enum NodeStateEnum {
    WALL('#'),
    PORTAL('w'),
    EMPTY('.');

    char value;

    NodeStateEnum(char value) {
        this.value = value;
    }
}

/**
 * SPAWNING = 0
 * WANDERING = 1
 */
enum MinionStateEnum {
    SPAWNING(0),
    WANDERING(1),
    STALKING(2),
    RUSHING(3),
    STUNNED(4);

    int value;

    MinionStateEnum(int value) {
        this.value = value;
    }

    static MinionStateEnum stateOf(int state) {
        return Arrays.stream(values()).filter(e -> e.value == state).findFirst().get();
    }
}

abstract class Monster extends Entity {

    MinionStateEnum minionState;
    int timer;
    int targetId;

    Monster(int id, Location location, MinionStateEnum minionState, int timer, int targetId) {
        super(id, location);
        this.timer = timer;
        this.minionState = minionState;
        this.targetId = targetId;
    }
}

class Wanderer extends Monster {

    Wanderer(int id, Location location, MinionStateEnum minionState, int timer, int targetId) {
        super(id, location, minionState, timer, targetId);
    }
}

class Slasher extends Monster {

    Slasher(int id, Location location, MinionStateEnum minionState, int timer, int targetId) {
        super(id, location, minionState, timer, targetId);
    }
}

class Explorer extends Entity {

    int health;
    int param1;
    int param2;

    Explorer(int id, Location location, int health, int param1, int param2) {
        super(id, location);
        this.health = health;
        this.param1 = param1;
        this.param2 = param2;
    }

    Location goInFriendRange(List<Explorer> explorers) {
        List<Location> safeLocations = explorers.stream()
                .flatMap(e -> e.getReallySafeLocations().stream())
                .filter(location ->
                        Player.kutuluGrid.isValideLocation(location)
                                && Player.kutuluGrid.get(location) != KutuluGrid.MotifEnum.MUR
                ).collect(Collectors.toList());

        return this.depthGrid.bestTargets(safeLocations).get(0);
    }

    /**
     * Distance from other explorers to lose less health = 2
     */
    private List<Location> getSafeLocations() {
        List<Location> res = Arrays.asList(
                new Location(location.x + 2, location.y),
                new Location(location.x - 2, location.y),
                new Location(location.x, location.y - 2),
                new Location(location.x, location.y + 2)
        );

        res.addAll(getReallySafeLocations());
        return res;
    }

    /**
     * Distance from other explorers to lose less health = 2
     */
    private List<Location> getReallySafeLocations() {
        return Arrays.asList(
                new Location(location.x + 1, location.y),
                new Location(location.x - 1, location.y),
                new Location(location.x, location.y),
                new Location(location.x, location.y - 1),
                new Location(location.x, location.y + 1)
        );
    }

    List<Location> runBehindAllies(List<Explorer> allies) {
        List<Location> safeLocations = allies.stream()
                .flatMap(allie -> Arrays.stream(DirectionEnum.values()).map(direction -> allie.location.move(direction)))
                .filter(location ->
                        Player.kutuluGrid.isValideLocation(location)
                                && Player.kutuluGrid.get(location) != KutuluGrid.MotifEnum.MUR
                )
                .sorted(Comparator.comparing(p -> depthGrid.get(p)))
                .collect(Collectors.toList());

        return this.depthGrid.bestTargets(safeLocations);
    }

    Location runFromMonsters(List<Monster> monsters, List<Explorer> allies) {

        //Tri des monsters par proximité
        List<Monster> closerWanderers = monsters.stream()
                .sorted(Comparator.comparing(w -> depthGrid.get(w.location)))
                .collect(Collectors.toList());

        System.err.println("closerWanderer : " + closerWanderers);

        //Récupération du chemin que devront parcourir les monsters
        Map<Monster, List<Location>> paths = closerWanderers.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        w -> findPathTo(w.location)
                ));

        System.err.println("paths : " + paths);

        //Récupération des alliés qui feront bouclier
        Map<Monster, List<Explorer>> shieldsByWanderer = paths.entrySet().stream()
                .map(e -> {
                    List<Location> path = e.getValue();
                    List<Explorer> shieds = allies.stream().filter(a -> path.contains(a.location)).collect(Collectors.toList());
                    return new Pair<>(e.getKey(), shieds);
                }).collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        System.err.println("shieldsByWanderer : " + shieldsByWanderer);

        //S'il existe des wanderer
        if (!closerWanderers.isEmpty()) {
            Monster closerMonster = closerWanderers.get(0);
            List<Explorer> shields = shieldsByWanderer.get(closerMonster);
            //S'il n'existe aucun bouclier à une distance de 2 ou - de nous, on s'éloigne du wanderer
            if (!shields.isEmpty() && shields.stream().anyMatch(s -> depthGrid.get(s.location) >= 2)) {
                Explorer closerAllie = shields.stream()
                        .min(Comparator.comparing(s -> depthGrid.get(s.location)))
                        .get();

                System.err.println("closerAllie : " + closerAllie);

                //Go 2 depths behind the allie
                Location res = closerMonster.runFromHim(closerAllie.location, 2);
                System.err.println("runFromHim shield : " + res);
                return res;
            } else {
                List<Location> locations = runBehindAllies(allies);
                System.err.println("runBehindAllies no shield : " + locations);
                return locations.get(0);
            }
        } else {
            return null;
        }
    }
}

class DepthGrid extends AbstractGrid<Integer> {

    DepthGrid(int maxX, int maxY, Location startLocation) {
        super(maxX, maxY);
        depthSearchLoop(Collections.singletonList(startLocation), new ArrayList<>(), 0);
    }

    /**
     * BFS
     */
    private void depthSearchLoop(List<Location> toVisit, List<Location> visited, int depth) {
        if (toVisit.isEmpty()) {
            return;
        }

        toVisit.forEach(location -> set(location, depth));

        List<Location> newToVisit = toVisit.stream()
                .flatMap(location -> Arrays.stream(DirectionEnum.values()).map(location::move))
                .filter(location ->
                        isValideLocation(location)
                                && Player.kutuluGrid.get(location) != KutuluGrid.MotifEnum.MUR
                                && !visited.contains(location)
                ).collect(Collectors.toList());

        visited.addAll(toVisit);
        depthSearchLoop(newToVisit, visited, depth + 1);
    }

    /**
     * A partir du tableau des distances on cré une liste des cibles les plus proches.
     */
    List<Location> bestTargets(List<Location> targets) {
        List<Location> bestTargets = new ArrayList<>();

        int bestDepth = Integer.MAX_VALUE;
        for (Location target : targets) {
            Integer tmpDepth = get(target);

            if (bestTargets.isEmpty() || tmpDepth < bestDepth) {
                bestTargets.clear();
                bestTargets.add(target);
                bestDepth = tmpDepth;
            } else if (tmpDepth.equals(bestDepth)) {
                bestTargets.add(target);
            }
        }

        return bestTargets;
    }

    @Override
    public void printGrid() {
        printGrid(".");
    }
}

class KutuluGrid extends AbstractGrid<KutuluGrid.MotifEnum> implements Cloneable {

    KutuluGrid(int maxX, int maxY) {
        super(maxX, maxY);
    }

    void formatLine(int y, String line) {
        for (int x = 0; x < MAX_X; x++) {
            set(x, y, MotifEnum.fromChar(line.charAt(x)));
        }
    }

    @Override
    public void printGrid() {
        printGrid("");
    }

    @Override
    public KutuluGrid clone() {
        KutuluGrid clone = new KutuluGrid(this.MAX_X, this.MAX_Y);
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                clone.set(x, y, this.get(x, y));
            }
        }
        return clone;
    }

    enum MotifEnum {
        MUR('#'),
        PORTAIL('w'),
        ABRI('U'),
        VIDE('.');

        char motif;

        MotifEnum(char motif) {
            this.motif = motif;
        }

        static MotifEnum fromChar(char motif) {
            return Stream.of(values()).filter(e -> e.motif == motif).findFirst().get();
        }
    }
}

/*
 *  GLOBAL CODING GAME CLASS !
 */

abstract class AbstractGrid<T> {

    private final List<List<T>> tab;
    final int MAX_X;
    final int MAX_Y;

    protected AbstractGrid(int maxX, int maxY) {
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

    boolean isValideLocation(Location location) {
        return isValideLocation(location.x, location.y);
    }

    boolean isValideLocation(int x, int y) {
        return x >= 0 && y >= 0 && x < MAX_X && y < MAX_Y;
    }

    void printGrid(String nullString) {
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                T node = get(x, y);
                if (node == null) {
                    System.err.print(nullString);
                } else {
                    System.err.print(node.toString());
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

    public abstract void printGrid();

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

    int distanceWith(Location location) {
        return Math.abs(x - location.x) + Math.abs(y - location.y);
    }

    @Override
    public String toString() {
        return "Location[" + x + ", " + y + ']';
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
}