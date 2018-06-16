import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public Unit reaper;
    public Unit destroyer;
    public Unit doof;

    public Player() {
    }

    @Override
    public String toString() {
        return "Reaper=" + reaper +
                "\nDestroyer=" + destroyer;
    }

    public static void main(String args[]) {

        //datas
        int myScore;
        int enemyScore1;
        int enemyScore2;
        int myRage;
        int enemyRage1;
        int enemyRage2;
        Map<PlayerTypeEnum, Player> playerMap;
        Environnement environnement;

        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
            myScore = in.nextInt();
            enemyScore1 = in.nextInt();
            enemyScore2 = in.nextInt();
            myRage = in.nextInt();
            enemyRage1 = in.nextInt();
            enemyRage2 = in.nextInt();
            int unitCount = in.nextInt();

            //reinit
            playerMap = new HashMap<>();
            playerMap.put(PlayerTypeEnum.ME, new Player());
            playerMap.put(PlayerTypeEnum.ENNEMI_1, new Player());
            playerMap.put(PlayerTypeEnum.ENNEMI_2, new Player());

            environnement = new Environnement();

            for (int i = 0; i < unitCount; i++) {
                //data read
                int unitId = in.nextInt();
                int unitType = in.nextInt();
                int player = in.nextInt();
                float mass = in.nextFloat();
                int radius = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
                int vx = in.nextInt();
                int vy = in.nextInt();
                int extra = in.nextInt();
                int extra2 = in.nextInt();

                //Data process
                PlayerTypeEnum actualPlayer = PlayerTypeEnum.getPlayerTypeEnumFromId(player);
                UnitTypeEnum actualUnitType = UnitTypeEnum.getUnitTypeEnumFromId(unitType);
                Unit newUnit = new Unit(unitId, mass, radius, x, y, vx, vy, extra, extra2);

                if (actualPlayer == PlayerTypeEnum.ENV) {
                    switch (actualUnitType) {
                        case TANKER:
                            environnement.tankerSquad.add(newUnit);
                            break;
                        case WATER:
                            environnement.waterPoints.add(newUnit);
                            break;
                        case REAPER:
                        case DESTROYER:
                        case DOOF:
                            throw new IllegalArgumentException("Environnement ne peut posseder de telles unités : " + actualUnitType);
                    }
                } else {
                    switch (actualUnitType) {
                        case REAPER:
                            playerMap.get(actualPlayer).reaper = newUnit;
                            break;
                        case DESTROYER:
                            playerMap.get(actualPlayer).destroyer = newUnit;
                            break;
                        case DOOF:
                            playerMap.get(actualPlayer).doof = newUnit;
                            break;
                        case TANKER:
                        case WATER:
                            throw new IllegalArgumentException("Player ne peut posseder de telles unités : " + actualUnitType);
                    }
                }
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            System.err.println("--- " + PlayerTypeEnum.ENV);
            System.err.println("------ " + UnitTypeEnum.WATER);
            for (Unit waterPoint : environnement.waterPoints) {
                System.err.println(waterPoint);
            }
            System.err.println("------ " + UnitTypeEnum.TANKER);
            for (Unit unit : environnement.tankerSquad) {
                System.err.println(unit);
            }

            for (Map.Entry<PlayerTypeEnum, Player> playerTypeEnumPlayerEntry : playerMap.entrySet()) {
                System.err.println("--- " + playerTypeEnumPlayerEntry.getKey());
                System.err.println(playerTypeEnumPlayerEntry.getValue().toString());
            }


            /************************************************************/
            double closestDistance = -1;

            System.err.println("--- Get the closest water Point from REAPER");
            Unit closestWaterPoint = null;
            Unit myReaper = playerMap.get(PlayerTypeEnum.ME).reaper;
            for (Unit unit : environnement.waterPoints) {
                double tmpDistance = MathUtils.getDistance(unit, myReaper);
                if (closestWaterPoint == null || tmpDistance < closestDistance) {
                    closestWaterPoint = unit;
                    closestDistance = tmpDistance;
                }
            }
            System.err.println("Closest waterPoint is " + closestWaterPoint);

            if (closestWaterPoint != null) {
                System.out.println(closestWaterPoint.x + " " + closestWaterPoint.y + " " + 300);
            } else {
                System.out.println("WAIT");
            }

            /************************************************************/

            /*if (myRage > 60) {
                //FIXME pb de portée
                Unit target;
                if (enemyScore1 > enemyScore2) {
                    target = playerMap.get(PlayerTypeEnum.ENNEMI_1).reaper;
                } else {
                    target = playerMap.get(PlayerTypeEnum.ENNEMI_2).reaper;
                }
                System.out.println("SKILL " + target.x + " " + target.y );
            } else {*/
            System.err.println("--- Get the closest tanker from DESTROYER");
            Unit closestTanker = null;
            Unit myDestroyer = playerMap.get(PlayerTypeEnum.ME).destroyer;
            for (Unit unit : environnement.tankerSquad) {
                double tmpDistance = MathUtils.getDistance(unit, myDestroyer);
                if (closestTanker == null || tmpDistance < closestDistance) {
                    closestTanker = unit;
                    closestDistance = tmpDistance;
                }
            }
            System.err.println("Closest tanker is " + closestTanker);

            if (closestTanker != null) {
                System.out.println(closestTanker.x + " " + closestTanker.y + " " + 300);
            } else {
                System.out.println("WAIT");
            }
            //}

            /************************************************************/

            System.err.println("--- Get the best reaper ennemi from DOOF");
            Unit target;
            if (enemyScore1 > enemyScore2) {
                target = playerMap.get(PlayerTypeEnum.ENNEMI_1).reaper;
            } else {
                target = playerMap.get(PlayerTypeEnum.ENNEMI_2).reaper;
            }
            System.err.println("Best ennemi is " + target);

            if (target != null) {
                System.out.println(target.x + " " + target.y + " " + 300);
            } else {
                System.out.println("WAIT");
            }

            /************************************************************/
        }
    }
}

class Environnement {
    List<Unit> waterPoints = new ArrayList<>();
    List<Unit> tankerSquad = new ArrayList<>();

    public Environnement() {
    }
}

class Unit {
    public int unitId;
    public float mass;
    public int radius;
    public int x;
    public int y;
    public int vx;
    public int vy;
    public int extra;
    public int extra2;

    public Unit(int unitId, float mass, int radius, int x, int y, int vx, int vy, int extra, int extra2) {
        this.unitId = unitId;
        this.mass = mass;
        this.radius = radius;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.extra = extra;
        this.extra2 = extra2;
    }

    @Override
    public String toString() {
        return "Unit{" +
                "unitId=" + unitId +
                ", mass=" + mass +
                ", radius=" + radius +
                ", x=" + x +
                ", y=" + y +
                ", vx=" + vx +
                ", vy=" + vy +
                ", extra=" + extra +
                ", extra2=" + extra2 +
                '}';
    }
}

enum UnitTypeEnum {
    REAPER(0),
    DESTROYER(1),
    DOOF(2),
    TANKER(3),
    WATER(4);

    public int id;

    UnitTypeEnum(int id) {
        this.id = id;
    }

    static UnitTypeEnum getUnitTypeEnumFromId(int id) {
        for (UnitTypeEnum unitTypeEnum : UnitTypeEnum.values()) {
            if (id == unitTypeEnum.id) {
                return unitTypeEnum;
            }
        }
        throw new IllegalArgumentException("No UnitTypeEnum found from id " + id);
    }
}

enum PlayerTypeEnum {
    ENV(-1),
    ME(0),
    ENNEMI_1(1),
    ENNEMI_2(2);

    public int id;

    PlayerTypeEnum(int id) {
        this.id = id;
    }

    static PlayerTypeEnum getPlayerTypeEnumFromId(int id) {
        for (PlayerTypeEnum playerTypeEnum : PlayerTypeEnum.values()) {
            if (id == playerTypeEnum.id) {
                return playerTypeEnum;
            }
        }
        throw new IllegalArgumentException("No PlayerTypeEnum found from id " + id);
    }
}

class MathUtils {

    public static double getDistance(Unit unit1, Unit unit2) {
        double distanceX;
        double distanceY;

        if ((unit1.x > 0 && unit2.x > 0) || (unit1.x < 0 && unit2.x < 0)) {
            distanceX = Math.abs(unit1.x) - Math.abs(unit2.x);
        } else {
            distanceX = Math.abs(unit1.x) + Math.abs(unit2.x);
        }
        if ((unit1.y > 0 && unit2.y > 0) || (unit1.y < 0 && unit2.y < 0)) {
            distanceY = Math.abs(unit1.y) - Math.abs(unit2.y);
        } else {
            distanceY = Math.abs(unit1.y) + Math.abs(unit2.y);
        }
        return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
    }
}