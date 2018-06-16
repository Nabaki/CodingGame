/**
 * Made with love by AntiSquid, Illedan and Wildum.
 * You can help children learn to code while you participate by donating to CoderDojo.
 **/
class Player {

    int id;
    int gold = 0;
    Map<Integer, Unit> units = new HashMap<>();
    Map<HeroEnum, Hero> heroes = new LinkedHashMap<>();
    Tower tower;

    Player(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", gold=" + gold +
                ", units=" + units +
                ", heroes=" + heroes +
                ", tower=" + tower +
                '}';
    }

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);
        int myTeam = in.nextInt();

        Player me = new Player(myTeam);
        Player ennemi = new Player(Math.abs(myTeam - 1));
        Environnement env = new Environnement();

        int bushAndSpawnPointCount = in.nextInt(); // usefrul from wood1, represents the number of bushes and the number of places where neutral units can spawn
        for (int i = 0; i < bushAndSpawnPointCount; i++) {
            String entityType = in.next(); // BUSH, from wood1 it can also be SPAWN
            int x = in.nextInt();
            int y = in.nextInt();
            int radius = in.nextInt();
        }
        int itemCount = in.nextInt(); // useful from wood2
        for (int i = 0; i < itemCount; i++) {
            String itemName = in.next(); // contains keywords such as BRONZE, SILVER and BLADE, BOOTS connected by "_" to help you sort easier
            int itemCost = in.nextInt(); // BRONZE items have lowest cost, the most expensive items are LEGENDARY
            int damage = in.nextInt(); // keyword BLADE is present if the most important item stat is damage
            int health = in.nextInt();
            int maxHealth = in.nextInt();
            int mana = in.nextInt();
            int maxMana = in.nextInt();
            int moveSpeed = in.nextInt(); // keyword BOOTS is present if the most important item stat is moveSpeed
            int manaRegeneration = in.nextInt();
            int isPotion = in.nextInt(); // 0 if it's not instantly consumed
        }

        //code de merde
        boolean switchTurn = true;

        // game loop
        while (true) {
            int gold = in.nextInt();
            int enemyGold = in.nextInt();
            me.gold = gold;
            ennemi.gold = enemyGold;

            int roundType = in.nextInt(); // a positive value will show the number of heroes that await a command
            switchTurn = !switchTurn;

            int entityCount = in.nextInt();
            for (int i = 0; i < entityCount; i++) {
                int unitId = in.nextInt();
                int team = in.nextInt();
                UnitEnum unitType = UnitEnum.valueOf(in.next()); // UNIT, HERO, TOWER, can also be GROOT from wood1
                int x = in.nextInt();
                int y = in.nextInt();
                int attackRange = in.nextInt();
                int health = in.nextInt();
                int maxHealth = in.nextInt();
                int shield = in.nextInt(); // useful in bronze
                int attackDamage = in.nextInt();
                int movementSpeed = in.nextInt();
                int stunDuration = in.nextInt(); // useful in bronze
                int goldValue = in.nextInt();
                int countDown1 = in.nextInt(); // all countDown and mana variables are useful starting in bronze
                int countDown2 = in.nextInt();
                int countDown3 = in.nextInt();
                int mana = in.nextInt();
                int maxMana = in.nextInt();
                int manaRegeneration = in.nextInt();
                String heroType = in.next(); // DEADPOOL, VALKYRIE, DOCTOR_STRANGE, HULK, IRONMAN
                int isVisible = in.nextInt(); // 0 if it isn't
                int itemsOwned = in.nextInt(); // useful from wood1

                Player owner = team == me.id ? me : ennemi;
                Unit tmpUnit = null;
                switch (unitType) {
                    case UNIT:
                        if (owner.units.containsKey(unitId)) {
                            tmpUnit = owner.units.get(unitId);
                        } else {
                            tmpUnit = new Unit(unitId, team, unitType, attackRange, maxHealth, maxMana);
                            owner.units.put(unitId, tmpUnit);
                        }
                        break;
                    case HERO:
                        HeroEnum heroEnum = HeroEnum.valueOf(heroType);
                        if (owner.heroes.containsKey(heroEnum)) {
                            tmpUnit = owner.heroes.get(heroEnum);
                        } else {
                            System.err.println(heroEnum + " is the new Hero !");
                            switch (heroEnum) {
                                case DEADPOOL:
                                    tmpUnit = new DeadPool(unitId, team, unitType, attackRange, maxHealth, maxMana);
                                    break;
                                case DOCTOR_STRANGE:
                                    tmpUnit = new DocteurStrange(unitId, team, unitType, attackRange, maxHealth, maxMana);
                                    break;
                                case HULK:
                                    tmpUnit = new Hulk(unitId, team, unitType, attackRange, maxHealth, maxMana);
                                    break;
                                case IRONMAN:
                                    tmpUnit = new IronMan(unitId, team, unitType, attackRange, maxHealth, maxMana);
                                    break;
                                case VALKYRIE:
                                    tmpUnit = new Valkyrie(unitId, team, unitType, attackRange, maxHealth, maxMana);
                                    break;
                                default:
                                    throw new IllegalArgumentException("Hero not know :" + heroEnum);
                            }
                            owner.heroes.put(heroEnum, (Hero) tmpUnit);
                        }
                        break;
                    case TOWER:
                        if (owner.tower != null) {
                            tmpUnit = owner.tower;
                        } else {
                            tmpUnit = new Tower(unitId, team, unitType, attackRange, maxHealth, maxMana);
                            owner.tower = (Tower) tmpUnit;
                        }
                        break;
                    case GROOT:
                        if (env.groots.containsKey(unitId)) {
                            tmpUnit = env.groots.get(unitId);
                        } else {
                            tmpUnit = new Groot(unitId, team, unitType, attackRange, maxHealth, maxMana);
                            env.groots.put(unitId, (Groot) tmpUnit);
                        }
                }
                tmpUnit.tick(x, y, health, shield, attackDamage, movementSpeed, stunDuration, goldValue, countDown1, countDown2, countDown3, mana, manaRegeneration, isVisible, itemsOwned, switchTurn);
            }
            System.err.println(ennemi.heroes.toString());
            //Supprimer les unités non actives
            me.clearDeadUnits(switchTurn);
            ennemi.clearDeadUnits(switchTurn);
            env.clearDeadUnits(switchTurn);
            System.err.println(ennemi.heroes.toString());

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            // If roundType has a negative value then you need to output a Hero name, such as "DEADPOOL" or "VALKYRIE".
            // Else you need to output roundType number of any valid action, such as "WAIT" or "ATTACK unitId"
            //System.out.println("WAIT");
            if (roundType < 0) {
                System.out.println(initHeroes(roundType));
            } else {
                for (Map.Entry<HeroEnum, Hero> heroEnumHeroEntry : me.heroes.entrySet()) {
                    System.out.println(heroEnumHeroEntry.getValue().doStrategy(me, ennemi, env));
                }
            }
        }
    }

    public boolean isLeftSide() {
        return this.tower.x < 1000;
    }

    public static String initHeroes(int roundType) {

        switch (roundType) {
            case -2:
                return HeroEnum.IRONMAN.name();
            case -1:
                return HeroEnum.DOCTOR_STRANGE.name();
            default:
                throw new IllegalArgumentException("roundType is " + roundType);
        }

    }

    public void clearDeadUnits(boolean switchTurn) {
        for (Iterator<Map.Entry<Integer, Unit>> it = this.units.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Unit> entry = it.next();
            if (entry.getValue().switchTurn != switchTurn) {
                it.remove();
            }
        }

        for (Iterator<Map.Entry<HeroEnum, Hero>> it = this.heroes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<HeroEnum, Hero> entry = it.next();
            if (entry.getValue().switchTurn != switchTurn) {
                it.remove();
            }
        }
    }
}

enum HeroEnum {
    DEADPOOL,
    DOCTOR_STRANGE,
    HULK,
    IRONMAN,
    VALKYRIE
}

enum ActionEnum {
    WAIT,
    MOVE,
    ATTACK,
    ATTACK_NEAREST,
    MOVE_ATTACK,
    BUY,
    SELL
}

enum UnitEnum {
    UNIT, HERO, TOWER, GROOT
}

abstract class Hero extends Unit {

    public Hero(int id, int teamId, UnitEnum unitEnum, int attackRange, int maxHealth, int maxMana) {
        super(id, teamId, unitEnum, attackRange, maxHealth, maxMana);
    }

    abstract String doStrategy(Player me, Player ennemi, Environnement env);

    public String runAndHit(Player me, Unit ennemi) {
        boolean isLeftSide = me.isLeftSide();
        int maxYranged = ennemi.y - this.y;
        int maxXranged = (int) Math.sqrt(this.attackRange * this.attackRange - maxYranged * maxYranged);
        int moveX = isLeftSide ? ennemi.x - maxXranged : ennemi.x + maxXranged;

        if (isLeftSide) {
            moveX = moveX < 0 ? 0 : moveX;
        } else {
            moveX = moveX > 1920 ? 1920 : moveX;
        }

        return ActionEnum.MOVE_ATTACK + " " + moveX + " " + this.y + " " + ennemi.id;
    }

    public String runInRange(Player me, Unit ennemi) {
        boolean isLeftSide = me.isLeftSide();
        int maxYranged = ennemi.y - this.y;
        int maxXranged = (int) Math.sqrt(this.attackRange * this.attackRange - maxYranged * maxYranged);
        int moveX = isLeftSide ? ennemi.x - maxXranged : ennemi.x + maxXranged;

        if (isLeftSide) {
            moveX = moveX < 0 ? 0 : moveX;
        } else {
            moveX = moveX > 1920 ? 1920 : moveX;
        }

        return ActionEnum.MOVE + " " + moveX + " " + this.y;
    }

    public boolean isBehindUnits(Tower camp, Map<Integer, Unit> units) {

        boolean isLeftSide = camp.x < 1000;
        boolean res = false;
        for (Map.Entry<Integer, Unit> unitSet : units.entrySet()) {
            if (isLeftSide) {
                if (unitSet.getValue().x > this.x) {
                    res = true;
                }
            } else {
                if (unitSet.getValue().x < this.x) {
                    res = true;
                }
            }
        }
        return res;
    }
}

class Tower extends Unit {

    public Tower(int id, int teamId, UnitEnum unitEnum, int attackRange, int maxHealth, int maxMana) {
        super(id, teamId, unitEnum, attackRange, maxHealth, maxMana);
    }
}

class Unit {
    //Perma
    int id;
    int teamId;
    UnitEnum unitType;
    int attackRange;
    int maxHealth;
    int maxMana;

    //Change
    int x;
    int y;
    int health;
    int previousHealth;
    int shield;
    int attackDamage;
    int movementSpeed;
    int stunDuration;
    int goldValue;
    int countDown1;
    int countDown2;
    int countDown3;
    int mana;
    int manaRegeneration;
    int isVisible;
    int itemsOwned;
    boolean switchTurn;

    public Unit(int id, int teamId, UnitEnum unitType, int attackRange, int maxHealth, int maxMana) {
        this.id = id;
        this.teamId = teamId;
        this.unitType = unitType;
        this.attackRange = attackRange;
        this.maxHealth = maxHealth;
        this.maxMana = maxMana;
    }

    public void tick(int x, int y, int health, int shield, int attackDamage, int movementSpeed, int stunDuration, int goldValue, int countDown1, int countDown2, int countDown3, int mana, int manaRegeneration, int isVisible, int itemsOwned, boolean switchTurn) {

        this.previousHealth = this.health;
        this.x = x;
        this.y = y;
        this.health = health;
        this.shield = shield;
        this.attackDamage = attackDamage;
        this.movementSpeed = movementSpeed;
        this.stunDuration = stunDuration;
        this.goldValue = goldValue;
        this.countDown1 = countDown1;
        this.countDown2 = countDown2;
        this.countDown3 = countDown3;
        this.mana = mana;
        this.manaRegeneration = manaRegeneration;
        this.isVisible = isVisible;
        this.itemsOwned = itemsOwned;
        this.switchTurn = switchTurn;
    }

    //On peut attaquer qqun si on peut se déplacer (0.9 secondes maximum) puis attaquer (0.1 seconde minimum)
    public boolean canAttack(Unit unit, boolean debug) {
        double distance = distanceWith(unit);
        if (debug) {
            System.err.println(distance);
            System.err.println(this.attackRange);
        }
        boolean res = distance - this.movementSpeed * 0.9 <= this.attackRange;

        /*if (res) {
            System.err.println(this.unitType + " can attack !");
            System.err.println(distance + " <= " + this.attackRange);
            System.err.println("this " + this.x + " " + this.y);
            System.err.println("unit " + unit.x + " " + unit.y);
        }*/
        return res;
    }

    public double distanceWith(Unit unit) {
        return Math.sqrt((this.x - unit.x) * (this.x - unit.x) + (this.y - unit.y) * (this.y - unit.y));
    }
}

class Environnement {
    Map<Integer, Groot> groots = new HashMap<>();

    public void clearDeadUnits(boolean switchTurn) {
        for (Iterator<Map.Entry<Integer, Groot>> it = this.groots.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Groot> entry = it.next();
            if (entry.getValue().switchTurn != switchTurn) {
                it.remove();
            }
        }
    }
}

class Groot extends Unit {

    public Groot(int id, int teamId, UnitEnum unitType, int attackRange, int maxHealth, int maxMana) {
        super(id, teamId, unitType, attackRange, maxHealth, maxMana);
    }
}


abstract class RangedHero extends Hero {

    public RangedHero(int id, int teamId, UnitEnum unitEnum, int attackRange, int maxHealth, int maxMana) {
        super(id, teamId, unitEnum, attackRange, maxHealth, maxMana);
    }

    public String doStrategy(Player me, Player ennemi, Environnement env) {
        //Calcul de range
        boolean outOfHero = true;
        for (Map.Entry<HeroEnum, Hero> heroSet : ennemi.heroes.entrySet()) {
            outOfHero &= !heroSet.getValue().canAttack(this, false);
        }
        boolean outOfTower = !ennemi.tower.canAttack(this, false);
        boolean outOfUnits = true;
        for (Map.Entry<Integer, Unit> integerUnitEntry : ennemi.units.entrySet()) {
            outOfUnits &= !integerUnitEntry.getValue().canAttack(this, false);
        }
        boolean outOfAllRange = outOfHero && outOfTower && outOfUnits;

        //Calcul de distances
        Double distanceWithHeroes = null;
        HeroEnum heroToFocus = null;
        for (Map.Entry<HeroEnum, Hero> heroSet : ennemi.heroes.entrySet()) {
            double tmpDistance = heroSet.getValue().distanceWith(this);
            if (distanceWithHeroes == null || distanceWithHeroes > tmpDistance) {
                distanceWithHeroes = tmpDistance;
                heroToFocus = heroSet.getKey();
            }
        }

        Double distanceWithUnits = null;
        Integer unitId = null;
        for (Map.Entry<Integer, Unit> unitSet : ennemi.units.entrySet()) {
            double tmpDistance = unitSet.getValue().distanceWith(this);
            if (distanceWithUnits == null || distanceWithUnits > tmpDistance) {
                distanceWithUnits = tmpDistance;
                unitId = unitSet.getValue().id;
            }
        }


        Integer targetToKill = null;
        for (Map.Entry<Integer, Unit> integerUnitEntry : ennemi.units.entrySet()) {
            if (integerUnitEntry.getValue().health < this.attackDamage) {
                targetToKill = integerUnitEntry.getKey();
            }
        }

        Integer targetToDeny = null;
        for (Map.Entry<Integer, Unit> integerUnitEntry : me.units.entrySet()) {
            if (integerUnitEntry.getValue().health < this.attackDamage) {
                targetToDeny = integerUnitEntry.getKey();
            }
        }

        boolean notFocused = this.previousHealth == this.health;

        /** Actions à faire
         - Rester hors range de la tour, des sbires et des heros
         - Rester derrière les sbires
         - Reculer quand on se fait attaquer
         - Attaquer le heroes advairse
         - Attaquer les sbires
         */
        String action;
        if (outOfTower && this.isBehindUnits(me.tower, me.units) && notFocused) {
            //Trouver la cible la plus proche
            //distanceWithHeroes == null si les unités sont cachées
            /*if (distanceWithUnits != null && distanceWithHeroes!= null &&  distanceWithHeroes > distanceWithUnits && targetToKill == null) {
                System.err.println("Run in range of unit " + unitId);
                action = this.runInRange(me, ennemi.units.get(unitId));
            } else */
            if (distanceWithUnits != null && distanceWithHeroes != null && distanceWithHeroes > distanceWithUnits) {
                System.err.println("Last hit unit " + unitId);
                action = this.runAndHit(me, ennemi.units.get(unitId));
            } else if (targetToDeny == null) {
                System.err.println("Attack hero " + heroToFocus);
                Unit unitToFocus = ennemi.heroes.get(heroToFocus);
                action = this.runAndHit(me, unitToFocus);
            } else {
                System.err.println("Deny unit " + targetToDeny);
                action = this.runAndHit(me, me.units.get(targetToDeny));
            }
        } else {
            System.err.println(this.getClass() + " " + outOfTower + " " + this.isBehindUnits(me.tower, me.units) + " " + this.previousHealth + " > " + this.health);
            action = "MOVE " + me.tower.x + " " + me.tower.y;
        }

        return action;
    }
}

class IronMan extends RangedHero {

    public IronMan(int id, int teamId, UnitEnum unitEnum, int attackRange, int maxHealth, int maxMana) {
        super(id, teamId, unitEnum, attackRange, maxHealth, maxMana);
    }
}

class Hulk extends Hero {

    public Hulk(int id, int teamId, UnitEnum unitEnum, int attackRange, int maxHealth, int maxMana) {
        super(id, teamId, unitEnum, attackRange, maxHealth, maxMana);
    }

    public String doStrategy(Player me, Player ennemi, Environnement env) {
        return "WAIT";
    }
}

class DeadPool extends Hero {

    public DeadPool(int id, int teamId, UnitEnum unitEnum, int attackRange, int maxHealth, int maxMana) {
        super(id, teamId, unitEnum, attackRange, maxHealth, maxMana);
    }

    //Farm and protect
    public String doStrategy(Player me, Player ennemi, Environnement env) {
        return jungle(me, ennemi, env);
    }

    /**
     * Farm les groots, va défendre le deuxième hero et deny les creeps alliés
     */
    private String jungle(Player me, Player ennemi, Environnement env) {
/*
        String action;
        if (!env.groots.isEmpty()) {
            //Si déjà en combat
            if(this.previousHealth > this.health){
                action = farmStrategie();
            }else{
                //Go to GROOT
            }

        } else {

            Integer targetToKill = null;
            for (Map.Entry<Integer, Unit> integerUnitEntry : ennemi.units.entrySet()) {
                System.err.println("unit attackRange " + integerUnitEntry.getValue().attackRange);
                if(integerUnitEntry.getValue().health < this.attackDamage){
                    targetToKill = integerUnitEntry.getKey();
                }
            }

            Integer targetToDeny = null;
            for (Map.Entry<Integer, Unit> integerUnitEntry : me.units.entrySet()) {
                if(integerUnitEntry.getValue().health < this.attackDamage){
                    targetToDeny = integerUnitEntry.getKey();
                }
            }

            if (targetToKill != null) {
                action = this.runAndHit(me, ennemi.units.get(targetToKill));
            } else if (targetToDeny != null) {
                action = this.runAndHit(me, me.units.get(targetToDeny));
            } else {
                //Rester à portée d'un MOVE_ATTACK des sbires cac
                action = this.runInRange();
            }
        }

        return action;*/

        return "";
    }

    private String farmStrategie() {
        return "";
    }
}

class DocteurStrange extends RangedHero {

    public DocteurStrange(int id, int teamId, UnitEnum unitEnum, int attackRange, int maxHealth, int maxMana) {
        super(id, teamId, unitEnum, attackRange, maxHealth, maxMana);
    }
}

class Valkyrie extends Hero {

    public Valkyrie(int id, int teamId, UnitEnum unitEnum, int attackRange, int maxHealth, int maxMana) {
        super(id, teamId, unitEnum, attackRange, maxHealth, maxMana);
    }

    public String doStrategy(Player me, Player ennemi, Environnement env) {
        return "WAIT";
    }
}
