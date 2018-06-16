import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    int gold = 100;
    int touchedSite = -1;
    boolean earlyGame = true;

    //Units
    Queen queen;
    List<Knight> knightList = new ArrayList<>();
    List<Archer> archerList = new ArrayList<>();
    List<Giant> giantList = new ArrayList<>();

    //Structures
    List<Integer> knightBarracksId = new ArrayList<>();
    List<Integer> archerBarracksId = new ArrayList<>();
    List<Integer> giantBarracksId = new ArrayList<>();
    List<Integer> towersId = new ArrayList<>();
    List<Integer> minesId = new ArrayList<>();


    public void update() {
        //Units
        knightList.clear();
        archerList.clear();
        giantList.clear();

        //Structures
        knightBarracksId.clear();
        archerBarracksId.clear();
        giantBarracksId.clear();
        towersId.clear();
        minesId.clear();
    }

    public void update(int gold, int touchedSite) {
        update();
        this.gold = gold;
        this.touchedSite = touchedSite;
    }

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);

        Map<Integer, Field> initFields = new HashMap<>();
        int numSites = in.nextInt();
        for (int i = 0; i < numSites; i++) {
            int fieldId = in.nextInt();
            int x = in.nextInt();
            int y = in.nextInt();
            int radius = in.nextInt();
            initFields.put(fieldId, new Field(fieldId, new Area(x, y, radius)));
        }

        final BattleField battleField = new BattleField(initFields);
        final Player me = new Player();
        final Player ennemi = new Player();

        // game loop
        while (true) {
            int gold = in.nextInt();
            int touchedSite = in.nextInt(); // -1 if none
            me.update(gold, touchedSite);
            ennemi.update();

            for (int i = 0; i < numSites; i++) {
                int siteId = in.nextInt();
                int goldLeft = in.nextInt();
                int maxMineSize = in.nextInt();
                StructureType structureType = StructureType.getFromId(in.nextInt()); // -1 = No structureType, 2 = BARRACKS
                Owner owner = Owner.getFromId(in.nextInt()); // -1 = No structureType, 0 = Friendly, 1 = Enemy
                int param1 = in.nextInt();
                int param2 = in.nextInt();

                Player player = owner == Owner.ME ? me : ennemi;
                Field field = battleField.get(siteId);
                field.update(goldLeft, maxMineSize);
                switch (structureType) {
                    case NONE:
                        battleField.get(siteId).structure = null;
                        break;
                    case TOWER:
                        player.towersId.add(siteId);
                        if (field.isEmpty()) {
                            field.set(new Tower(owner, param1, param2));
                        } else {
                            field.structure.update(param1, param2);
                        }
                        break;
                    case BARRACKS:
                        switch (UnitType.getFromId(param2)) {
                            case QUEEN:
                                throw new IllegalArgumentException("Pas de barracks pour des reines.");
                            case KNIGHT:
                                player.knightBarracksId.add(siteId);
                                break;
                            case ARCHER:
                                player.archerBarracksId.add(siteId);
                                break;
                            case GIANT:
                                player.giantBarracksId.add(siteId);
                                break;
                        }
                        if (field.isEmpty()) {
                            field.set(new Barrack(owner, UnitType.getFromId(param1), param2));
                        } else {
                            field.structure.update(param1, param2);
                        }
                        break;
                    case MINE:
                        player.minesId.add(siteId);
                        if (field.isEmpty()) {
                            field.set(new Mine(owner, param1, maxMineSize));
                        } else {
                            field.structure.update(param1, param2);
                        }
                        break;
                }
            }
            int numUnits = in.nextInt();
            for (int i = 0; i < numUnits; i++) {
                int x = in.nextInt();
                int y = in.nextInt();
                Owner owner = Owner.getFromId(in.nextInt());
                UnitType unitType = UnitType.getFromId(in.nextInt()); // -1 = QUEEN, 0 = KNIGHT, 1 = ARCHER
                int health = in.nextInt();

                Player player = owner == Owner.ME ? me : ennemi;
                switch (unitType) {
                    case QUEEN:
                        if (player.queen == null) {
                            player.queen = new Queen(x, y, health, battleField);
                        } else {
                            player.queen.update(x, y, health);
                        }
                        break;
                    case KNIGHT:
                        player.knightList.add(new Knight(x, y, health));
                        break;
                    case ARCHER:
                        player.archerList.add(new Archer(x, y, health));
                        break;
                    case GIANT:
                        player.giantList.add(new Giant(x, y, health));
                        break;
                }
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            System.err.println(me.toString());
            System.err.println(ennemi.toString());
            System.err.println(battleField);

            // First line: A valid queen action
            // Second line: A set of training instructions
            System.out.println(me.action(battleField, ennemi));
            System.out.println(me.train(battleField, ennemi));
        }
    }

    private String action(BattleField battleField, Player ennemi) {

        //Define what to build
        StructureType buildTarget;
        UnitType unitTarget = null;


        if (!ennemi.knightList.isEmpty()) {
            buildTarget = StructureType.TOWER;
        } else if (gold < 200) {
            buildTarget = StructureType.MINE;
        } else if (knightBarracksId.size() < 3) {
            buildTarget = StructureType.BARRACKS;
            unitTarget = UnitType.KNIGHT;
        } else {
            //Dans le but de gagner du temps
            buildTarget = StructureType.TOWER;
        }

        Integer fieldIdTarget = null;
        IStructure touchedStruture = touchedSite != -1 ? battleField.get(touchedSite).structure : null;
        switch (buildTarget) {
            case NONE:
                throw new IllegalArgumentException("Aucune cible de contruction impossible !");
            case MINE:
                if (touchedStruture != null && touchedStruture.getStructureType() == StructureType.MINE && !((Mine) touchedStruture).isMaxed()) {
                    fieldIdTarget = touchedSite;
                } else {
                    fieldIdTarget = queen.getClosestField(buildTarget).id;
                }
                break;
            case TOWER:
                if (touchedStruture != null && touchedStruture.getStructureType() == StructureType.TOWER && ((Tower) touchedStruture).range < 400) {
                    fieldIdTarget = touchedSite;
                } else {
                    fieldIdTarget = queen.getClosestField(buildTarget).id;
                }
                break;
            case BARRACKS:
                fieldIdTarget = queen.getClosestField(buildTarget).id;
                break;
        }

        String result;
        if (buildTarget == StructureType.BARRACKS) {
            result = Action.BUILD + " " + fieldIdTarget + " " + buildTarget + '-' + unitTarget;
        } else {
            result = Action.BUILD + " " + fieldIdTarget + " " + buildTarget;
        }

        return result;

    }

    private String train(BattleField battleField, Player ennemi) {

        List<Integer> trainingList = new ArrayList<>();

        //GIANTS
        if (giantList.size() < 1 && ennemi.towersId.size() > 1) {
            for (Integer id : giantBarracksId) {
                Barrack structure = (Barrack) battleField.get(id).structure;
                if (structure.canTrain(gold)) {
                    gold -= structure.train();
                    trainingList.add(id);
                }
            }
        }

        //KNIGHTS
        if (knightBarracksId.size() * UnitType.KNIGHT.cost < gold) {
            for (Integer id : knightBarracksId) {
                Barrack structure = (Barrack) battleField.get(id).structure;
                if (structure.canTrain(gold)) {
                    gold -= structure.train();
                    trainingList.add(id);
                }
            }
        }

        // Agreggation des Units à train
        StringBuilder train = new StringBuilder("TRAIN");
        for (Integer id : trainingList) {
            train.append(" ");
            train.append(id);
        }
        return train.toString();
    }

    @Override
    public String toString() {
        return "Player{" +
                "gold=" + gold +
                ", touchedSite=" + touchedSite +
                ", queen=" + queen +
                ", knightList=" + knightList +
                ", archerList=" + archerList +
                ", giantList=" + giantList +
                ", knightBarracksId=" + knightBarracksId +
                ", archerBarracksId=" + archerBarracksId +
                ", giantBarracksId=" + giantBarracksId +
                ", towersId=" + towersId +
                ", minesId=" + minesId +
                '}';
    }
}

final class DistanceUtils {

    private DistanceUtils() {
    }

    static int distanceBetween(Area c1, Area c2) {
        int x1 = c1.location.x + c1.radius;
        int y1 = c1.location.y + c1.radius;
        int x2 = c2.location.x + c2.radius;
        int y2 = c2.location.y + c2.radius;

        return (int) Math.sqrt(
                Math.abs((x1 - x2) * (x1 - x2))
                        + Math.abs((y1 - y2) * (y1 - y2))
        );
    }

    private static int isInRange(Unit unit1, Unit unit2) {
        int count = 0;
        while (distanceBetween(unit1.area, unit2.area) > unit1.unitType.range + unit2.unitType.range + count * (unit1.unitType.moveSpeed + unit2.unitType.moveSpeed)) {
            count++;
        }
        return count;
    }

    private static int isInRange(Field fieldWithTower, Unit unit) {
        int count = 0;
        while (distanceBetween(fieldWithTower.area, unit.area) > Tower.BASE_RANGE + unit.unitType.range + count * unit.unitType.moveSpeed) {
            count++;
        }
        return count;
    }

    private static Unit closestUnit(Unit unit, List<? extends Unit> units) {
        Unit closestUnit = null;
        int closestDistance = -1;
        for (Unit tmpUnit : units) {
            int tmpDistance = distanceBetween(unit.area, tmpUnit.area);
            if (closestUnit == null || tmpDistance < closestDistance) {
                closestDistance = tmpDistance;
                closestUnit = tmpUnit;
            }
        }
        return closestUnit;
    }

    public static Field closestEmptyField(Unit unit, List<Field> fields) {
        Field closestUnit = null;
        int closestDistance = -1;
        for (Field tmpField : fields) {
            int tmpDistance = distanceBetween(unit.area, tmpField.area);
            if ((closestUnit == null || tmpDistance < closestDistance) && tmpField.structure == null) {
                closestDistance = tmpDistance;
                closestUnit = tmpField;
            }
        }
        return closestUnit;
    }

    private static Unit closestUnit(Field fieldWithTower, List<? extends Unit> units) {
        Unit closestUnit = null;
        int closestDistance = -1;
        for (Unit tmpUnit : units) {
            int tmpDistance = distanceBetween(fieldWithTower.area, tmpUnit.area);
            if (closestUnit == null || tmpDistance < closestDistance) {
                closestDistance = tmpDistance;
                closestUnit = tmpUnit;
            }
        }
        return closestUnit;
    }

    //0 if not or the number of turns before being in range
    private static int areInRange(Unit unit, List<? extends Unit> units) {
        Unit closestUnit = closestUnit(unit, units);
        return isInRange(unit, closestUnit);
    }

    private static int areInRange(Field fieldWithTower, List<? extends Unit> units) {
        Unit closestUnit = closestUnit(fieldWithTower, units);
        return isInRange(fieldWithTower, closestUnit);
    }

    public static boolean doesNeedTower(Queen queen, BattleField battleField, List<Knight> knights) {
        if (knights.isEmpty()) {
            return false;
        }

        //Trouver l'endroit où on devrait créer une tour
        Integer fieldWithTowerId = queen.nextFieldIdTarget(battleField);
        Field fieldWithTower = battleField.get(fieldWithTowerId);
        Field nextFieldWithTower = battleField.get(queen.orderedFieldIds.get(queen.orderedFieldIds.indexOf(fieldWithTowerId) + 1));
        // Le nombre de tours pour qu'elle soit construite
        // +1 pour le tour de construction de la tour
        int towerTurns = isInRange(fieldWithTower, queen) + 1;
        int nextTowerTurns = isInRange(nextFieldWithTower, queen) + 1;
        // Le nombre de tours pour que les chevaliers atteignent sa portée
        int knightsTurns = areInRange(fieldWithTower, knights);
        int nextKnightsTurns = areInRange(nextFieldWithTower, knights);
        // si Q < K dans un tour de + (2 par sécurité ?) alors oui

        System.err.println(towerTurns + "<=" + knightsTurns + " mais " + nextTowerTurns + "<=" + nextKnightsTurns);
        return towerTurns >= knightsTurns || (towerTurns < knightsTurns && nextTowerTurns >= nextKnightsTurns);
    }
}

class Location {
    int x;
    int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "[" + x + ',' + y + ']';
    }
}

class BattleField extends HashMap<Integer, Field> {
    public static final int MAX_X = 1920;
    public static final int MAX_Y = 1000;

    BattleField(Map<Integer, Field> map) {
        super(map);
    }
}

enum StructureType {
    NONE(-1),
    MINE(0),
    TOWER(1),
    BARRACKS(2);

    public final int id;

    StructureType(int id) {
        this.id = id;
    }

    public static StructureType getFromId(int id) {
        for (StructureType structure : values()) {
            if (structure.id == id) {
                return structure;
            }
        }
        throw new IllegalArgumentException(StructureType.class + " ne connait pas l'id " + id);
    }
}

class Field {
    final Integer id;
    final Area area;
    final int tag;

    IStructure structure;
    int goldLeft;
    int maxMineSize;

    public Field(int id, Area area) {
        this.id = id;
        this.area = area;

        int xLimit = BattleField.MAX_X / 3;
        if (area.location.x < xLimit) {
            this.tag = 0;
        } else if (area.location.x < xLimit * 2) {
            this.tag = 1;
        } else {
            this.tag = 2;
        }
    }

    public void set(IStructure structure) {
        this.structure = structure;
    }

    void update(int goldLeft, int maxMineSize) {
        this.goldLeft = goldLeft;
        this.maxMineSize = maxMineSize;
    }

    boolean isEmpty() {
        return structure == null;
    }

    @Override
    public String toString() {
        return "Field{" +
                area +
                ", " + structure +
                ", " + tag +
                '}';
    }
}

interface IStructure {
    StructureType getStructureType();

    Owner getOwner();

    void update(int param1, int param2);
}

abstract class Structure implements IStructure {
    final Owner owner;

    public Structure(Owner owner) {
        this.owner = owner;
    }

    public Owner getOwner() {
        return owner;
    }
}

enum Owner {
    ME(0),
    ENNEMI(1),
    NONE(-1);

    public final int id;

    Owner(int id) {
        this.id = id;
    }

    public static Owner getFromId(int id) {
        for (Owner owner : values()) {
            if (owner.id == id) {
                return owner;
            }
        }
        throw new IllegalArgumentException(Owner.class + "ne connait pas l'id " + id);
    }
}

class Area {
    Location location;
    int radius;

    public Area(Location location, int radius) {
        this.location = location;
        this.radius = radius;
    }

    public Area(int x, int y, int radius) {
        this.location = new Location(x, y);
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "Area{" +
                "area=" + location +
                ", radius=" + radius +
                '}';
    }
}

abstract class Unit {
    final UnitType unitType;
    Area area;
    int health;

    Unit(UnitType unitType, Area area, int health) {
        this.unitType = unitType;
        this.area = area;
        this.health = health;
    }

    @Override
    public String toString() {
        return unitType + "{" + area + ", " + health + '}';
    }
}

class Queen extends Unit {
    final List<Integer> orderedFieldIds;

    final List<Field> mines = new ArrayList<>();
    final List<Field> others = new ArrayList<>();

    public Queen(int x, int y, int health, BattleField battleField) {
        super(UnitType.QUEEN, new Area(x, y, UnitType.QUEEN.radius), health);
        this.orderedFieldIds = orderTargetFields(battleField);


        int xLimit = BattleField.MAX_X / 3;
        int mineTag;
        if (area.location.x < xLimit) {
            mineTag = 0;
        } else {
            mineTag = 2;
        }

        for (Field field : battleField.values()) {
            if (field.tag == 1) {
                others.add(field);
            } else if (field.tag == mineTag) {
                mines.add(field);
            }
        }
    }

    Queen update(int x, int y, int health) {
        this.area.location.x = x;
        this.area.location.y = y;
        this.health = health;
        return this;
    }

    private List<Integer> orderTargetFields(BattleField battleField) {
        Map<Integer, Integer> distanceFielMap = new HashMap<>();
        for (Map.Entry<Integer, Field> idFieldEntry : battleField.entrySet()) {
            distanceFielMap.put(idFieldEntry.getKey(), DistanceUtils.distanceBetween(area, idFieldEntry.getValue().area));
        }
        return new ArrayList<>(sortByValues(distanceFielMap).keySet());
    }

    private <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator =
                (k1, k2) -> map.get(k1).compareTo(map.get(k2));
        Map<K, V> sortedByValues = new TreeMap<>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    public Integer nextFieldIdTarget(BattleField battleField) {
        for (Integer orderedFieldId : orderedFieldIds) {
            if (battleField.get(orderedFieldId).structure == null) {
                return orderedFieldId;
            }
        }
        return null;
    }

    private Field getClosestMine() {
        return DistanceUtils.closestEmptyField(this, mines);
    }

    private Field getClosestOther() {
        return DistanceUtils.closestEmptyField(this, others);
    }

    public Field getClosestField(StructureType structureType) {
        if (structureType == StructureType.MINE) {
            return getClosestMine();
        } else {
            return getClosestOther();
        }
    }

    public StructureType getStructureToBuild(Field field) {
        if (mines.contains(field)) {
            return StructureType.MINE;
        } else {
            return StructureType.TOWER;
        }
    }

    @Override
    public String toString() {
        return "Queen{" +
                "mines=" + mines +
                ", others=" + others +
                '}';
    }
}

class Knight extends Unit {
    Knight(int x, int y, int health) {
        super(UnitType.KNIGHT, new Area(x, y, UnitType.KNIGHT.radius), health);
    }
}

class Archer extends Unit {
    Archer(int x, int y, int health) {
        super(UnitType.ARCHER, new Area(x, y, UnitType.ARCHER.radius), health);
    }
}

class Giant extends Unit {
    Giant(int x, int y, int health) {
        super(UnitType.GIANT, new Area(x, y, UnitType.GIANT.radius), health);
    }
}

enum Action {
    MOVE,
    BUILD,
    WAIT
}

enum UnitType {
    QUEEN(-1, 0, 60, 30, 0),
    KNIGHT(0, 80, 100, 20, 0),
    ARCHER(1, 100, 75, 25, 200),
    GIANT(2, 140, 50, 40, 0);

    final int id;
    final int cost;
    final int moveSpeed;
    final int radius;
    final int range;

    UnitType(int id, int cost, int moveSpeed, int radius, int range) {
        this.id = id;
        this.cost = cost;
        this.moveSpeed = moveSpeed;
        this.radius = radius;
        this.range = range;
    }

    public static UnitType getFromId(int id) {
        for (UnitType unitType : values()) {
            if (unitType.id == id) {
                return unitType;
            }
        }
        throw new IllegalArgumentException(UnitType.class + "ne connait pas l'id " + id);
    }
}

class Barrack extends Structure {
    UnitType unitType;
    int timer;

    public Barrack(Owner owner, UnitType unitType, int timer) {
        super(owner);
        this.unitType = unitType;
        this.timer = timer;
    }

    private boolean isTraining() {
        return timer > 0;
    }

    public boolean canTrain(int gold) {
        return gold >= unitType.cost && !isTraining();
    }

    public int train() {
        return unitType.cost;
    }

    @Override
    public StructureType getStructureType() {
        return StructureType.BARRACKS;
    }

    @Override
    public Owner getOwner() {
        return owner;
    }

    @Override
    public void update(int param1, int param2) {
        timer = param1;
        unitType = UnitType.getFromId(param2);
    }

    @Override
    public String toString() {
        return "Barrack{" +
                "timer=" + timer +
                ", unitType=" + unitType +
                '}';
    }
}

class Tower extends Structure {
    static final int BASE_HEALTH = 200;
    static final int MAX_HEALTH = 800;
    static final int BASE_RANGE = 250; // sqrt((pv * 1000 + aireSite) / PI) sans considérer la aireSite

    int health;
    int range;

    Tower(Owner owner, int health, int range) {
        super(owner);
        this.health = health;
        this.range = range;
    }

    @Override
    public String toString() {
        return "TOWER{" +
                "health=" + health +
                ", range=" + range +
                '}';
    }

    @Override
    public StructureType getStructureType() {
        return StructureType.TOWER;
    }

    @Override
    public Owner getOwner() {
        return owner;
    }

    @Override
    public void update(int param1, int param2) {
        health = param1;
        range = param2;
    }
}

class Mine extends Structure {
    final int maxMineSize;
    int actualMineSize;

    Mine(Owner owner, int actualMineSize, int maxMineSize) {
        super(owner);
        this.maxMineSize = maxMineSize;
        this.actualMineSize = actualMineSize;
    }

    @Override
    public String toString() {
        return "Mine{" +
                "actualMineSize=" + actualMineSize +
                '}';
    }

    @Override
    public StructureType getStructureType() {
        return StructureType.MINE;
    }

    @Override
    public Owner getOwner() {
        return owner;
    }

    @Override
    public void update(int param1, int param2) {
        actualMineSize = param1;
    }

    public boolean isMaxed() {
        return actualMineSize == maxMineSize;
    }
}