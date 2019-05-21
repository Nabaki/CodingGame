import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Function;

class Player {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        Game g = new Game();
        g.init(in);

        // game loop
        while (true) {
            g.update(in);
            g.debug();
            g.buildOutput();
            g.output();
        }
    }
}

class Game {

    List<Unit> units;
    List<Building> buildings;
    List<Command> output;
    int gold;
    int income;
    BattleField battleField = new BattleField(12, 12);

    Game() {
        units = new ArrayList<>();
        buildings = new ArrayList<>();
        output = new ArrayList<>();
    }

    // not useful in Wood 3
    void init(Scanner in) {
        int numberMineSpots = in.nextInt();
        for (int i = 0; i < numberMineSpots; i++) {
            int x = in.nextInt();
            int y = in.nextInt();
        }
    }

    void update(Scanner in) {

        units.clear();
        buildings.clear();
        output.clear();

        // READ TURN INPUT
        gold = in.nextInt();
        int income = in.nextInt();
        int opponentGold = in.nextInt();
        int opponentIncome = in.nextInt();
        for (int i = 0; i < 12; i++) {
            String line = in.next();
            battleField.initLine(i, line);
            System.err.println(line);
        }
        int buildingCount = in.nextInt();
        for (int i = 0; i < buildingCount; i++) {
            OwnerEnum owner = OwnerEnum.get(in.nextInt());
            int buildingType = in.nextInt();
            int x = in.nextInt();
            int y = in.nextInt();
            buildings.add(new Building(x, y, buildingType, owner));
        }
        int unitCount = in.nextInt();
        for (int i = 0; i < unitCount; i++) {
            OwnerEnum owner = OwnerEnum.get(in.nextInt());
            int unitId = in.nextInt();
            int level = in.nextInt();
            int x = in.nextInt();
            int y = in.nextInt();
            units.add(new Unit(x, y, unitId, level, owner));
        }
    }

    void buildOutput() {
        // @TODO "core" of the AI
        trainUnits();

        moveUnits();
    }

    private void trainUnits() {
        Position trainingPosition = findTrainingPosition();
        if (gold > 30) {
            output.add(new Command(CommandType.TRAIN, 1, trainingPosition));
        }
    }

    // move to the center
    private void moveUnits() {
        Position center = new Position(5, 5);
        units.stream()
                .filter(u -> u.owner == OwnerEnum.ME)
                .forEach(myUnit -> output.add(new Command(CommandType.MOVE, myUnit.id, center)));
    }

    // train near the HQ for now
    private Position findTrainingPosition() {
        Building HQ = getHQ();
        if (HQ.position.x == 0) {
            return new Position(0, 1);
        }
        return new Position(11, 10);
    }

    void output() {
        StringBuilder s = new StringBuilder("WAIT;");
        output.forEach(c -> s.append(c.get()));
        System.out.println(s);
    }

    public void debug() {
        battleField.debug();
        units.forEach(Unit::debug);
        buildings.forEach(Building::debug);
    }

    private Building getHQ() {
        return buildings.stream().filter(b -> b.type == BuildingTypeEnum.HQ && b.owner == OwnerEnum.ME).findFirst().get();
    }

    private Building getOpponentHQ() {
        return buildings.stream().filter(b -> b.type == BuildingTypeEnum.HQ && b.owner == OwnerEnum.ADV).findFirst().get();
    }
}

class Command {

    CommandType t;
    Position p;
    int idOrLevel;

    Command(CommandType t, int idOrLevel, Position p) {
        this.t = t;
        this.p = p;
        this.idOrLevel = idOrLevel;
    }

    String get() {
        return t.toString() + " " + idOrLevel + " " + p.x + " " + p.y + ";";
    }
}

class Unit {

    int id;
    int level;
    Position position;
    OwnerEnum owner;

    Unit(int x, int y, int id, int level, OwnerEnum owner) {
        this.position = new Position(x, y);
        this.id = id;
        this.level = level;
        this.owner = owner;
    }

    void debug() {
        System.err.println("Unit of level " + level + " at " + position.x + " " + position.y + " owned by " + owner);
    }
}

class Building {

    Position position;
    BuildingTypeEnum type;
    OwnerEnum owner;

    Building(int x, int y, int type, OwnerEnum owner) {
        this.position = new Position(x, y);
        this.type = BuildingTypeEnum.get(type);
        this.owner = owner;
    }

    void debug() {
        System.err.println(type + " at " + position.x + " " + position.y + " owned by " + owner);
    }
}

enum BuildingTypeEnum {

    HQ(0);

    int value;

    BuildingTypeEnum(int value) {
        this.value = value;
    }

    public static BuildingTypeEnum get(int value) {
        switch (value) {
            case 0:
                return HQ;
            default:
                throw new IllegalArgumentException(value + " not expected.");
        }
    }
}

enum CommandType {
    MOVE,
    TRAIN
}


class BattleField extends AbstractGrid<FieldEnum> {

    protected BattleField(int maxX, int maxY) {
        super(maxX, maxY);
    }

    void initLine(int y, String line) {
        for (int x = 0; x < MAX_X; x++) {
            set(x, y, FieldEnum.getFromMotif(line.charAt(x)));
        }
    }

    @Override
    public void debug() {
        debug(fieldEnum -> String.valueOf(fieldEnum.motif));
    }
}

enum FieldEnum {
    NEANT('#'),
    NEUTRE('.'),
    CAPTUREE('O'),
    INACTIF('o'),
    ADV_CATUREE('X'),
    ADV_INACTIF('x');

    char motif;

    FieldEnum(char motif) {
        this.motif = motif;
    }

    public static FieldEnum getFromMotif(char motif) {
        switch (motif) {
            case '#':
                return NEANT;
            case '.':
                return NEUTRE;
            case 'O':
                return CAPTUREE;
            case 'o':
                return INACTIF;
            case 'X':
                return ADV_CATUREE;
            case 'x':
                return ADV_INACTIF;
            default:
                throw new IllegalArgumentException(motif + " not expected.");
        }
    }
}

enum OwnerEnum {
    ME(0),
    ADV(1);

    int value;

    OwnerEnum(int value) {
        this.value = value;
    }

    public static OwnerEnum get(int value) {
        switch (value) {
            case 0:
                return ME;
            case 1:
                return ADV;
            default:
                throw new IllegalArgumentException(value + " not expected.");
        }
    }

    @Override
    public String toString() {
        return name();
    }
}

/*
 *  GLOBAL CODING GAME CLASS !
 */

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

    int distanceWith(Position position) {
        return Math.abs(x - position.x) + Math.abs(y - position.y);
    }

    @Override
    public String toString() {
        return "Position[" + x + ", " + y + ']';
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

    boolean isValidePosition(Position position) {
        return isValidePosition(position.x, position.y);
    }

    boolean isValidePosition(int x, int y) {
        return x >= 0 && y >= 0 && x < MAX_X && y < MAX_Y;
    }

    protected void debug(Function<T, String> printNodeFunction) {
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