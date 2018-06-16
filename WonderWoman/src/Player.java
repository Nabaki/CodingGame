import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    // Global datas
    public static LevelEnum[][] grid;
    public static List<Unit> myList;
    public static List<Unit> ennemiList;
    //Max number of actions is 9 per unit (max 2)
    public static List<Action> legalActionsList = new ArrayList<>(18);

    //Debug mode
    public static boolean debug = false;

    //Estimations
    public static LevelEnum[][] oldGrid;
    public static List<Unit> myOldList;
    public static List<Unit> ennemiOldList;
    public static int[] myOldBuild;
    public static int[] oldEnnemiBuild;
    public static int[] ennemiBuild;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int size = in.nextInt();
        int unitsPerPlayer = in.nextInt();

        //Init datas
        grid = new LevelEnum[size][size];
        myList = new ArrayList<>(unitsPerPlayer);
        ennemiList = new ArrayList<>(unitsPerPlayer);
        myOldList = new ArrayList<>(unitsPerPlayer);
        ennemiOldList = new ArrayList<>(unitsPerPlayer);

        // game loop
        while (true) {
            initLoop();

            for (int y = 0; y < size; y++) {
                String row = in.next();
                for (int x = 0; x < size; x++) {
                    grid[x][y] = LevelEnum.getLevelEnumFromLvl(
                            Character.getNumericValue(
                                    row.replace(".", "4")
                                            .charAt(x)
                            )
                    );
                }
            }
            for (int i = 0; i < unitsPerPlayer; i++) {
                int[] myTmpList = new int[2];
                myTmpList[0] = in.nextInt(); //X
                myTmpList[1] = in.nextInt();//Y
                myList.add(new Unit(myTmpList));
            }
            for (int i = 0; i < unitsPerPlayer; i++) {
                int[] otherTmpList = new int[2];
                otherTmpList[0] = in.nextInt(); //X
                otherTmpList[1] = in.nextInt();//Y
                //Si l'ennemi n'est pas vu alors on ne l'ajoute pas à la liste
                if (otherTmpList[0] != -1) {
                    ennemiList.add(new Unit(otherTmpList));
                }
            }
            int legalActions = in.nextInt();
            for (int i = 0; i < legalActions; i++) {
                String atype = in.next();
                int index = in.nextInt();
                String dir1 = in.next();
                String dir2 = in.next();
                legalActionsList.add(new Action(ActionEnum.getTypeActionFromAction(atype), index, DirectionEnum.valueOf(dir1), DirectionEnum.valueOf(dir2)));
            }

            Action bestAction;
            if (!myOldList.isEmpty()) {
                haveBeenPushed(); //TODO, à refaire
            }
            if (oldGrid != null) {
                findEnnemiBuild();
                findUnit();
                //TODO trouver les positions potentielles des ennemis
            }

            for (Action action : legalActionsList) {
                countPointsForAction(action);
            }
            bestAction = findBestScore();
            doAction(bestAction);
        }
    }


    public static void initLoop() {
        System.err.println("--- New turn ---");
        myList.clear();
        ennemiOldList.clear();
        ennemiOldList.addAll(ennemiList);
        for (Unit ennemiPosition : ennemiOldList) {
            ennemiPosition.setIsConcernedByBuild(false);
        }
        ennemiList.clear();
        legalActionsList.clear();

        if (grid[0][0] != null) {
            oldGrid = copyGrid();
        }
    }

    public static LevelEnum[][] copyGrid() {
        LevelEnum[][] tmpGrid;
        tmpGrid = new LevelEnum[grid.length][grid.length];
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid.length; y++) {
                tmpGrid[x][y] = grid[x][y];
            }
        }

        return tmpGrid;
    }

    /**
     * Donne des points en fonction de la case de destination
     * ATTENTION on ne vérifie pas la case de départ (mouvement impossible donc il ne devrait pas être proposé)
     */
    private static void countPointsForAction(Action action) {

        Unit unit = myList.get(action.getPionId());
        int[] newPosition = getNextPosition(unit.getPosition(), action.getDir1());

        int points;
        if (action.getAtype().equals("PUSH&BUILD")) {
            points = countAttackPoints(newPosition, action.getDir2());
        } else {
            int movePoints = countMovePoints(action); //countVoronoiPoints(action); //
            int buildPoints = countBuildPoints(action);

            if (movePoints < 0 || buildPoints < 0) {
                points = -(Math.abs(movePoints) + Math.abs(buildPoints));
            } else {
                points = movePoints + buildPoints;
            }
        }

        //algo de gestion des points
        action.setPoints(points);
    }


    /**
     * Priorités :
     * Ne pas mourir TODO priorité aux pions qui sont enfermés
     * Atteindre un lvl 3
     * Détruire un lvl 3 TODO meme si on tombe ?
     * Si l'ennemi peut être bloqué, le tuer
     * Monter le plus haut possible
     */
    private static int countMovePoints(Action action) {
        int points = 0;
        Unit actualPosition = myList.get(action.getPionId());
        LevelEnum actualLevel = getLvl(actualPosition.getPosition());
        int[] newPosition = getNextPosition(myList.get(action.getPionId()).getPosition(), action.getDir1());
        LevelEnum newLevel = getLvl(newPosition);
        int[] buildPosition = getNextPosition(newPosition, action.getDir2());
        LevelEnum buildLevel = getLvl(buildPosition);
        List<int[]> actualPositionsAccessiblesListe = getPositionsAccessibles(actualPosition.getPosition());
        List<int[]> newPositionsAccessiblesListe = getPositionsAccessibles(newPosition);

        //TODO On donne de l'importance aux pions en danger de mort
        if (newLevel == LevelEnum.LVL_3) {
            points += 100;
        } else if (buildLevel == LevelEnum.LVL_3 && isEnnemyClose(buildPosition)) {
            //Si un ennemi est proche d'une case à 3 on la ferme
            points += 90;
        } else if (newLevel == LevelEnum.LVL_2) {
            points += 70;
        } else if (newLevel == LevelEnum.LVL_1) {
            points += 60;
        } else if (newLevel == LevelEnum.LVL_0) {
            points += 50;
        } else {
            System.err.println("You killed me ! " + action.toString() + " new Lvl= " + newLevel.level);
            System.exit(-1);
        }
        return points;
    }

    /**
     * Priorités :
     * //positif
     * Construire lvl 3 atteignable TODO sauf par ennemi
     * Construire lvl 4 si adversaire proche de nous ou pas atteignable
     * Gagner 1 lvl à un lvl 2 TODO priorité entre eux ?
     * Gagner 1 lvl à un lvl 1 TODO priorité entre eux ?
     * //négatif
     * Construire lvl 3 pas atteignable ni ennemi TODO Tout autre mouvement est meilleur, même en étant sur un lvl plus bas
     * Construire lvl 2 ou moins si ennemi proche (no help)
     * Construire lvl 4 si les ennemis sont loins (réutilisation)
     * Construire lvl 3 si adversaire proche de nous
     */
    private static int countBuildPoints(Action action) {
        Unit actualPosition = myList.get(action.getPionId());
        int[] newPosition = getNextPosition(actualPosition.getPosition(), action.getDir1());

        int points;
        LevelEnum newLevel = getLvl(newPosition);
        int[] buildPosition = getNextPosition(newPosition, action.getDir2());
        LevelEnum buildLevel = getLvl(buildPosition);

        //Fermer une zone
        if (buildLevel == LevelEnum.LVL_3) {
            if (newLevel == LevelEnum.LVL_3 || newLevel == LevelEnum.LVL_2) {
                logRule("countBuildPoints", 9, "garder pour réutilisation");
                points = -1;
            } else {
                logRule("countBuildPoints", 2, "peut pas atteindre ni ennemi"); //neutre
                points = 0;
            }
            //Mettre une zone lvl 3
        } else if (buildLevel == LevelEnum.LVL_2) {
            if (isEnnemyClose(buildPosition)) {
                logRule("countBuildPoints", 10, "EnnemyClose - ne pas construire"); //négatif
                points = -3;
            } else if (newLevel == LevelEnum.LVL_3 || newLevel == LevelEnum.LVL_2) {
                logRule("countBuildPoints", 1, "Construire zone 3 atteignable");
                points = 9;
            } else {
                logRule("countBuildPoints", 7, "Ne pas construire zone 3 si pas atteignable"); //négatif neutre (on pourrait donner la zone)
                points = -2;
            }
        } else if (buildLevel == LevelEnum.LVL_1) {
            points = 2;
        } else if (buildLevel == LevelEnum.LVL_0) {
            points = 1;
        } else {
            throw new IllegalArgumentException("countBuildPoints - " + buildLevel);
        }
        return points;
    }

    /**
     * Priorités :
     * Si on le fait tomber
     * Si on le pousse dans une impasse //a développer
     * S'il est sur un lvl 3
     */
    private static int countAttackPoints(int[] ennemyPosition, DirectionEnum directionPushed) {
        int points;
        LevelEnum ennemyLvl = getLvl(ennemyPosition);
        int[] nextEnnemyPosition = getNextPosition(ennemyPosition, directionPushed);
        LevelEnum nextEnnemyLvl = getLvl(nextEnnemyPosition);
        List<int[]> positionsAccessibles = getPositionsAccessibles(nextEnnemyPosition);

        if (nextEnnemyLvl.level < ennemyLvl.level || positionsAccessibles.size() <= 1 || ennemyLvl == LevelEnum.LVL_3) {
            //Il peut y avoir 8 cases autour, et ce mouvement est prioritaire sur tous les autres
            points = 9 - positionsAccessibles.size();
            logRule("countAttackPoints", 1, "Ennemy wil fall ! " + points);
        } else {
            //Si l'ennemi peu remonter sur la plateforme aprés l'avoir poussé, celà n'a pas d'intéret
            points = 0;
            logRule("countAttackPoints", 2, "Ennemy can't fall ");
        }
        return points * 1000;
    }


    private static int[] getNextPosition(int[] position, DirectionEnum directionEnum) {
        int[] nextPosition = new int[]{position[0] + directionEnum.x, position[1] + directionEnum.y};
        //System.err.println("getNextPosition - "  + directionEnum.name() + " -> " + nextPosition[0] + ", " + nextPosition[1]);
        return nextPosition;
    }

    public static LevelEnum getLvl(int[] position) {
        LevelEnum level;
        level = grid[position[0]][position[1]];
        return level;
    }

    public static Action findBestScore() {
        Collections.sort(legalActionsList);
        System.err.println("legalActionsList");

        Action bestAction = null;
        if (legalActionsList != null && !legalActionsList.isEmpty()) {
            bestAction = legalActionsList.get(0);
            float bestScore = bestAction.getPoints();
            int bestVoronoiPoints = 0;

            for (Action action : legalActionsList) {
                if (action.getPoints() != bestScore) {
                    break;
                }

                System.err.println(action);/*
                int tmpVoronoiPoints = countVoronoiPoints(action);
                System.err.println(" -> VoronoiPoints " + tmpVoronoiPoints);
                if (tmpVoronoiPoints > bestVoronoiPoints) {
                    bestVoronoiPoints = tmpVoronoiPoints;
                    bestAction = action;
                }*/
            }
        }

        return legalActionsList.get(0);
    }


    public static void doAction(Action action) {
        if (action != null) {
            myOldList.clear();
            myOldList.addAll(myList);

            int[] posPion = new int[]{myList.get(action.getPionId()).getPosition()[0], myList.get(action.getPionId()).getPosition()[1]};
            if (action.getAtype().equals("MOVE&BUILD")) {
                myOldBuild = getNextPosition(getNextPosition(posPion, action.getDir1()), action.getDir2());
                myOldList.set(action.getPionId(), new Unit(getNextPosition(posPion, action.getDir1())));

            } else {
                myOldBuild = getNextPosition(posPion, action.getDir1());
                ennemiBuild = null;
                System.err.println("PUSHED !");

                //Si on pousse l'ennemi, il change de position
                int[] ennemiPosition = getNextPosition(myList.get(action.getPionId()).getPosition(), action.getDir1());
                for (int i = 0; i < ennemiList.size(); i++) {
                    if (ennemiList.get(i).estimatePosition()[0] == ennemiPosition[0] && ennemiList.get(i).estimatePosition()[1] == ennemiPosition[1]) {
                        ennemiList.remove(i);
                        break;
                    }
                }
                ArrayList<int[]> tmpList = new ArrayList<>();
                tmpList.add(ennemiPosition);
                tmpList.add(getNextPosition(ennemiPosition, action.getDir2()));
                ennemiList.add(new Unit(tmpList));
            }
            StringBuilder iSeeU = new StringBuilder();
            for (Unit ennemiPosition : ennemiList) {
                iSeeU.append(Arrays.toString(ennemiPosition.estimatePosition()));
                iSeeU.append(" ");
            }

            action.doIt();
        }
    }

    public static int distanceBetween(int[] position1, int[] position2) {
        int distanceX = Math.abs(position1[0] - position2[0]);
        int distanceY = Math.abs(position1[1] - position2[1]);
        int result = distanceX > distanceY ? distanceX : distanceY;

        return result;
    }

    //Donne si un ennemi peut atteindre la position au tour d'aprés
    public static boolean isEnnemyClose(int[] position) {
        LevelEnum positionLvl = getLvl(position);
        boolean res = false;
        for (Unit ennemiPosition : ennemiList) {
            int[] ennemi = ennemiPosition.estimatePosition();
            if (ennemi != null) {
                LevelEnum ennemiLevel = getLvl(ennemi);
                if (distanceBetween(position, ennemi) == 1 && isDeltaClose(positionLvl, ennemiLevel)) {
                    res = true;
                }
            }
        }
        return res;
    }

    public static boolean isEnnemyDistanceClose(int[] position) {
        LevelEnum positionLvl = getLvl(position);
        boolean res = false;
        for (Unit ennemiPosition : ennemiList) {
            int[] ennemi = ennemiPosition.estimatePosition();
            if (ennemi != null) {
                LevelEnum ennemiLevel = getLvl(ennemi);
                if (distanceBetween(position, ennemi) == 1) {
                    res = true;
                }
            }
        }
        return res;
    }

    /**
     * L'ennemi est proche s'il est capable de monter sur la cible
     */
    public static boolean isDeltaClose(LevelEnum target, LevelEnum ennemi) {
        int delta = target.level - ennemi.level;
        boolean res = true;
        if (delta > 1) {
            res = false;
        }
        return res;
    }

    public static void logRule(String method, int ruleNb, String comment) {
        if (debug) {
            System.err.println(method + " - regle " + ruleNb + " - " + comment);
        }
    }

    /**
     * Donne un nombre de points basé sur l'algo de voronoi qui permet de savoir qui contrôle le mieux la map
     */
    public static int countVoronoiPoints(Action action) {

        //make changes on grid with this action
        ArrayList<Unit> myCloneList = new ArrayList<>(myList);
        if (action.getAtype().equals("MOVE&BUILD")) {
            myCloneList.set(action.getPionId(), new Unit(getNextPosition(myList.get(action.getPionId()).getPosition(), action.getDir1())));
        }

        int myPoints = 0;
        Integer[][] voronoiMap = new Integer[grid.length][grid.length];
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid.length; y++) {
                if (getLvl(new int[]{x, y}) != LevelEnum.LVL_4) {
                    int tmpDist = grid.length + 1;
                    for (Unit ennemiPosition : ennemiList) {
                        if (tmpDist > distanceBetween(new int[]{x, y}, ennemiPosition.estimatePosition())) {
                            tmpDist = distanceBetween(new int[]{x, y}, ennemiPosition.estimatePosition());
                        }
                    }
                    for (Unit ints : myCloneList) {
                        if (tmpDist > distanceBetween(new int[]{x, y}, ints.getPosition())) {
                            myPoints++;
                            break;
                        }
                    }
                }
            }
        }
        return myPoints;
    }

    /**
     * Trouve le build de l'ennemi
     */
    public static void findEnnemiBuild() {
        oldEnnemiBuild = ennemiBuild;
        //Trouver la nouvelle construction
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid.length; y++) {
                int deltaLvl = grid[x][y].level - oldGrid[x][y].level;
                if ((deltaLvl == 1 && !(x == myOldBuild[0] && y == myOldBuild[1])) || deltaLvl == 2) {
                    ennemiBuild = new int[]{x, y};
                    if (oldEnnemiBuild != null) {
                        System.err.println("oldEnnemiBuild " + oldEnnemiBuild[0] + "," + oldEnnemiBuild[1]);
                    }
                    System.err.println("ennemiBuild " + ennemiBuild[0] + "," + ennemiBuild[1]);
                    return;
                }
            }
        }
    }

    public static void findUnit() {

        //Corrélation avec les ennemis vus
        //Le but est de supprimer les ennemis de ennemiOldList qui sont maintenant vus
        ArrayList<Unit> cloneUnitList = new ArrayList<>(ennemiList);
        for (Iterator<Unit> it = ennemiOldList.iterator(); it.hasNext(); ) {
            Unit oldEnnemi = it.next();
            int[] oldUnit = oldEnnemi.getPosition();
            List<int[]> oldPotentialPositions = oldEnnemi.getPotentialPositions();
            // Pour chaque ennemi vu
            for (Iterator<Unit> it1 = cloneUnitList.iterator(); it1.hasNext(); ) {
                Unit ennemi = it1.next();
                int[] ennemiPosition = ennemi.getPosition();

                //On vérifie si on le voyait pas avant
                if (oldUnit != null) {
                    boolean isDeltaClose = isDeltaClose(getLvl(ennemiPosition), getLvl(oldUnit));
                    boolean isDistanceClose = distanceBetween(ennemiPosition, oldUnit) <= 1;

                    if (isDeltaClose && isDistanceClose) {
                        //System.err.println("OldEnnemi [" + oldUnit[0] + "," + oldUnit[1] + "] moved to ennemi [" + ennemiPosition[0] + "," + ennemiPosition[1] + "]");
                        it.remove();
                        it1.remove();
                        break;
                    } else {
                        //System.err.println("OldEnnemi [" + oldUnit[0] + "," + oldUnit[1] + "] is not new ennemi [" + ennemiPosition[0] + "," + ennemiPosition[1] + "] cause of isDeltaClose " + isDeltaClose + " and isDistanceClose " + isDistanceClose);
                    }
                    //On vérifie les estimations d'avant étaient pour lui
                } else if (!oldPotentialPositions.isEmpty() && ennemiPosition != null) {
                    boolean isBreak = false;
                    for (int[] oldPotentialPosition : oldPotentialPositions) {
                        boolean isDeltaClose = isDeltaClose(getLvl(ennemiPosition), getLvl(oldPotentialPosition));
                        boolean isDistanceClose = distanceBetween(ennemiPosition, oldPotentialPosition) <= 1;

                        if (isDeltaClose && isDistanceClose) {
                            //System.err.println("oldPotentialPosition [" + oldPotentialPosition[0] + "," + oldPotentialPosition[1] + "] moved to ennemi [" + ennemiPosition[0] + "," + ennemiPosition[1] + "]");
                            it.remove();
                            it1.remove();
                            isBreak = true;
                            break;
                        } else {
                            //System.err.println("oldPotentialPosition [" + oldPotentialPosition[0] + "," + oldPotentialPosition[1] + "] is not new ennemi [" + ennemiPosition[0] + "," + ennemiPosition[1] + "] cause of isDeltaClose " + isDeltaClose + " and isDistanceClose " + isDistanceClose);
                        }
                    }
                    if (isBreak) {
                        break;
                    }
                }
            }

        }

            /*System.err.println("ennemiOldList");
            for (Unit ennemiPosition : ennemiOldList) {
                System.err.println(ennemiPosition.toString());
            }
            System.err.println("ennemiList");
            for (Unit ennemiPosition : ennemiList) {
                System.err.println(ennemiPosition.toString());
            }*/

        //Si on connait déjà tous les ennemis, on s'arrete là
        if (ennemiList.size() == myList.size()) {
            System.err.println("La position des 2 ennemis est déjà connue !");
            return;
        }

        //Trouver les positions potentielles en fonction du build
        if (ennemiBuild != null) {
            ArrayList<int[]> potentialUnitList = new ArrayList<>();
            for (DirectionEnum directionEnum : DirectionEnum.values()) {
                LevelEnum tmpLvl;
                int[] tmpPosition = getNextPosition(ennemiBuild, directionEnum);
                try {
                    tmpLvl = getLvl(tmpPosition);
                    for (Unit ennemiPosition : ennemiList) {
                        if (Arrays.equals(ennemiPosition.getPosition(), tmpPosition)) {
                            //System.err.println("La position de l'ennemi est déjà connue !");
                            ennemiList.addAll(ennemiOldList);
                            return;
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    tmpLvl = LevelEnum.LVL_4;
                }

                //Test de si c'est une position potentielle pour un ennemi
                //Si la zone est pas 4
                if (tmpLvl == LevelEnum.LVL_4) {
                    //System.err.println("[" + tmpPosition[0] + "," + tmpPosition[1] + "] is lvl 4");
                    continue;
                }

                //Si la zone est vue par un allié ou si c'est celle d'un allié
                boolean isPotentialUnit = true;
                for (Unit ints : myList) {
                    if (distanceBetween(ints.getPosition(), tmpPosition) <= 1) {
                        //System.err.println("[" + tmpPosition[0] + "," + tmpPosition[1] + "] is ally or seen by ally");
                        isPotentialUnit = false;
                        break;
                    }
                }
                if (!isPotentialUnit) {
                    continue;
                }
    /*
                    //Si la zone est trop loin de l'ancien build
                    if (oldEnnemiBuild != null && distanceBetween(oldEnnemiBuild, tmpPosition) > 2) {
                        System.err.println("[" + tmpPosition[0] + "," + tmpPosition[1] + "] is too far from old build [" + oldEnnemiBuild[0] + "," + oldEnnemiBuild[1] + "]");
                        continue;
                    }*/

                //Si on connaissait la position d'un oldEnnemi, on regarde s'il a pu bouger jusque là
                isPotentialUnit = false;
                for (Unit oldEnnemi : ennemiOldList) {
                    int[] ennemiPosition = oldEnnemi.getPosition();
                    if (ennemiPosition != null) {
                        boolean isDeltaClose = isDeltaClose(getLvl(tmpPosition), getLvl(ennemiPosition));
                        boolean isDistanceClose = distanceBetween(ennemiPosition, tmpPosition) <= 1;
                        if (isDeltaClose && isDistanceClose) {
                            //System.err.println("[" + tmpPosition[0] + "," + tmpPosition[1] + "] is a potential position of the old ennemi [" + ennemiPosition[0] + "," + ennemiPosition[1] + "]");
                            oldEnnemi.setIsConcernedByBuild(true);
                            isPotentialUnit = true;
                        } else {
                            //System.err.println("[" + tmpPosition[0] + "," + tmpPosition[1] + "] is too far from old ennemi [" + ennemiPosition[0] + "," + ennemiPosition[1] + "]");
                        }
                    } else {
                        for (int[] potentialPosition : oldEnnemi.getPotentialPositions()) {
                            if (distanceBetween(potentialPosition, tmpPosition) <= 1 && isDeltaClose(getLvl(tmpPosition), getLvl(potentialPosition))) {
                                //System.err.println("[" + tmpPosition[0] + "," + tmpPosition[1] + "] is a potential position of the old Potential ennemi position [" + potentialPosition[0] + "," + potentialPosition[1] + "]");
                                oldEnnemi.setIsConcernedByBuild(true);
                                isPotentialUnit = true;
                            }
                        }
                    }
                }
                if (!isPotentialUnit) {
                    continue;
                }

                potentialUnitList.add(tmpPosition);
            }
/*
                System.err.println("potentialUnitList");
                for (int[] ints : potentialUnitList) {
                    System.err.println(Arrays.toString(ints) + " ");
                }
*/
            //On sait qui a bougé et qui n'a pas bougé si l'ennemi était proche du build
            boolean haveToClear = false;
            for (Iterator<Unit> it = ennemiOldList.iterator(); it.hasNext(); ) {
                Unit oldEnnemi = it.next();
                if (!oldEnnemi.isConcernedByBuild()) {
                    //System.err.println(oldEnnemi.toString() + " n'a pas bougé");
                    ennemiList.add(oldEnnemi);
                    it.remove();
                } else {
                    //System.err.println(oldEnnemi.toString() + " a bougé");
                    ArrayList<int[]> potentialUnitListClone = (ArrayList<int[]>) potentialUnitList.clone();
                    if (oldEnnemi.getPosition() != null) {
                        for (Iterator<int[]> it1 = potentialUnitListClone.iterator(); it1.hasNext(); ) {
                            int[] potentialUnit = it1.next();
                            if (Arrays.equals(potentialUnit, oldEnnemi.getPosition())) {
                                //System.err.println(oldEnnemi.toString() + " ne peut pas rester à son ancienne position");
                                it1.remove();
                            }
                        }
                    } else if (oldEnnemi.getPotentialPositions() != null) {
                        correlationWithOldPositions(oldEnnemi, potentialUnitListClone);
                    }
                    ennemiList.add(new Unit(potentialUnitListClone));
                    haveToClear = true;
                }
            }
            if (haveToClear) {
                potentialUnitList.clear();
            }

            //Si rien ne correspond à la liste de mouvements potentiels, alors c'est un nouveau pion
            if (!potentialUnitList.isEmpty()) {
                ennemiList.add(new Unit(potentialUnitList));
            }
/*
                System.err.println("ennemiOldList");
                for (Unit ennemiPosition : ennemiOldList) {
                    System.err.println(ennemiPosition.toString());
                }
                System.err.println("ennemiList");
                for (Unit ennemiPosition : ennemiList) {
                    System.err.println(ennemiPosition.toString());
                }*/
        } else {
            ennemiList.addAll(ennemiOldList); //TODO plus complexe
        }

        //On fait le ménage
        System.err.println("EnnemiList = ");
        for (Iterator<Unit> it = ennemiList.iterator(); it.hasNext(); ) {
            Unit ennemiPosition = it.next();
            List<int[]> tmpPotentialUnitList = ennemiPosition.getPotentialPositions();
            if (ennemiPosition.getPosition() != null) {
                System.err.println("Position évidente : " + ennemiPosition.toString());
            } else if (tmpPotentialUnitList.isEmpty()) {
                System.err.println("Liste potentielle perdue, les 2 enemis sont trop proches pour savoir lequel fait quoi !");
                it.remove();
            } else if (tmpPotentialUnitList.size() == 1) {
                System.err.println("Une seule position potentielle !");
                ennemiPosition.setPosition(tmpPotentialUnitList.get(0));
                tmpPotentialUnitList.clear();
                System.err.println(ennemiPosition.toString());
            } else {
                System.err.println("Trop de positions potentielles : " + ennemiPosition.toString());
            }
        }
    }

    //Correlation avec les anciennes positions potentielles
    public static void correlationWithOldPositions(Unit oldEnnemi, List<int[]> potentialNewPositions) {
        for (Iterator<int[]> it = potentialNewPositions.iterator(); it.hasNext(); ) {
            int[] potentialUnit = it.next();
            boolean isLinked = false;
            for (int[] oldPotentialPosition : oldEnnemi.getPotentialPositions()) {
                boolean isDeltaClose = isDeltaClose(getLvl(potentialUnit), getLvl(oldPotentialPosition));
                boolean isDistanceClose = distanceBetween(potentialUnit, oldPotentialPosition) <= 1;

                if (isDeltaClose && isDistanceClose) {
                    //System.err.println("oldPotentialPosition " + Arrays.toString(oldPotentialPosition) + " is linked to " + Arrays.toString(potentialUnit));
                    isLinked = true;
                }
            }
            if (!isLinked) {
                //System.err.println("potentialPosition " + Arrays.toString(potentialUnit) + " is not linked to oldPositions");
                it.remove();
            }
        }
        if (potentialNewPositions.isEmpty()) {
            System.err.println("There is no link between oldPotentialPositions and newPotentialPositions, the other ennemi moved");
            System.exit(-1);
        }
    }

    //TODO ajouter le build
    //TODO penser à ses alliés / adversaires
    public static List<int[]> getPositionsAccessibles(int[] position) {
        List<int[]> res = new ArrayList<>();
        LevelEnum lvl = getLvl(position);
        for (DirectionEnum directionEnum : DirectionEnum.values()) {
            LevelEnum newLvl;
            int[] newPosition = getNextPosition(position, directionEnum);
            try {
                newLvl = getLvl(newPosition);
            } catch (IndexOutOfBoundsException e) {
                newLvl = LevelEnum.LVL_4;
            }
            if (newLvl.level - lvl.level <= 1 && newLvl != LevelEnum.LVL_4) {
                res.add(newPosition);
            }
        }

        return res;
    }

    public static void printGrid(LevelEnum[][] grid) {
        if (grid[0][0] == null) {
            System.err.println("printGrid - grid not initialized !");
        } else {
            for (int y = 0; y < grid.length; y++) {
                for (int x = 0; x < grid.length; x++) {
                    System.err.print(grid[x][y].level);
                }
                System.err.println("");
            }
        }
    }

    public static void haveBeenPushed() {
        for (int i = 0; i < myList.size(); i++) {
            if (!Arrays.equals(myList.get(i).getPosition(), myOldList.get(i).getPosition())) {
                //System.err.println(Arrays.toString(myList.get(i)) + " have been pushed !");
                int[] push = new int[]{myList.get(i).getPosition()[0] - myOldList.get(i).getPosition()[0], myList.get(i).getPosition()[1] - myOldList.get(i).getPosition()[1]};
                ArrayList<int[]> potentialPusher = getPushers(push, myOldList.get(i).getPosition());

                for (Iterator<Unit> it = ennemiOldList.iterator(); it.hasNext(); ) {
                    Unit ennemiPosition = it.next();
                    for (int[] ints : potentialPusher) {
                        if (Arrays.equals(ints, ennemiPosition.getPosition())) {
                            System.err.println("That's you ! " + ennemiPosition);
                            ennemiList.add(ennemiPosition);
                            it.remove();
                            break;
                        } else {
                            boolean needToBreak = false;
                            for (int[] ints1 : ennemiPosition.getPotentialPositions()) {
                                if (Arrays.equals(ints, ints1)) {
                                    System.err.println("That's you ! " + ennemiPosition);
                                    correlationWithOldPositions(ennemiPosition, potentialPusher);
                                    ennemiList.add(new Unit(potentialPusher));
                                    needToBreak = true;
                                    it.remove();
                                    break;
                                }
                            }
                            if (needToBreak) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public static ArrayList<int[]> getPushers(int[] push, int[] pushed) {
        //System.err.println("push " + Arrays.toString(push));
        //System.err.println("pushed " + Arrays.toString(pushed));
        ArrayList<int[]> potentialPushers = new ArrayList<>();
        int[] obviousPush = new int[]{pushed[0] - push[0], pushed[1] - push[1]};
        potentialPushers.add(obviousPush);

        if (Math.abs(push[0]) + Math.abs(push[1]) == 2) {
            potentialPushers.add(new int[]{obviousPush[0], obviousPush[1] + push[1]});
            potentialPushers.add(new int[]{obviousPush[0] + push[0], obviousPush[1]});
        } else {
            if (push[0] != 0) {
                potentialPushers.add(new int[]{obviousPush[0] + push[0], obviousPush[1] - 1});
                potentialPushers.add(new int[]{obviousPush[0] + push[0], obviousPush[1] + 1});
            } else {
                potentialPushers.add(new int[]{obviousPush[0] - 1, obviousPush[1] + push[0]});
                potentialPushers.add(new int[]{obviousPush[0] + 1, obviousPush[1] + push[0]});
            }
        }
        //System.err.print("potentialPushers -> ");
        for (Iterator<int[]> it = potentialPushers.iterator(); it.hasNext(); ) {
            int[] potentialPusher = it.next();
            try {
                if (getLvl(potentialPusher) == LevelEnum.LVL_4 || Arrays.equals(myList.get(0).getPosition(), potentialPusher) || Arrays.equals(myList.get(1).getPosition(), potentialPusher)) {
                    it.remove();
                }

                //System.err.print(Arrays.toString(potentialPusher) + " ");
            } catch (IndexOutOfBoundsException e) {
                it.remove();
            }
        }
        //System.err.println("");
        return potentialPushers;
    }

    /**
     * 1/ Comparer l'ancienne map à la nouvelle (soustraction des levels)
     * 2/ Supprimer les points ajoutés par les alliés
     * 3/ En déduire les positions potentielles de l'ennemi
     * 4/ Corréler avec les positions potentielles de l'ennemi au tour d'avant
     * 5/ Corréler avec la position d'un ennemi déjà vu
     */
    //TODO INSTINCT DE SURVIE, pousser mieux et aller moins dans les impasses
}

class Action implements Comparable<Action> {
    private ActionEnum atype;
    private int pionId;
    private DirectionEnum dir1;
    private DirectionEnum dir2;
    private int points = 0;

    Action(ActionEnum atype, int pionId, DirectionEnum dir1, DirectionEnum dir2) {
        this.atype = atype;
        this.pionId = pionId;
        this.dir1 = dir1;
        this.dir2 = dir2;
    }

    public void doIt() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.atype.action);
        sb.append(" ");
        sb.append(this.pionId);
        sb.append(" ");
        sb.append(this.dir1);
        sb.append(" ");
        sb.append(this.dir2);
        System.out.println(sb.toString());
    }

    public ActionEnum getAtype() {
        return atype;
    }

    public int getPionId() {
        return pionId;
    }

    public int getPoints() {
        return points;
    }

    public DirectionEnum getDir1() {
        return dir1;
    }

    public DirectionEnum getDir2() {
        return dir2;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public int compareTo(Action o) {
        if (this.getPoints() > o.getPoints()) {
            return -1;
        } else if (this.getPoints() > o.getPoints()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "Action{" +
                "atype=" + atype +
                ", pionId=" + pionId +
                ", dir1=" + dir1 +
                ", dir2=" + dir2 +
                ", points=" + points +
                '}';
    }
}

enum ActionEnum {
    MOVE("MOVE&BUILD"),
    PUSH("PUSH&BUILD");

    String action;

    ActionEnum(String action) {
        this.action = action;
    }

    static ActionEnum getTypeActionFromAction(String action) {
        for (ActionEnum actionEnum : ActionEnum.values()) {
            if (actionEnum.action.equals(action)) {
                return actionEnum;
            }
        }
        throw new IllegalArgumentException("Action inconnue " + action);
    }
}

enum DirectionEnum {
    N(0, -1),
    NE(1, -1),
    E(1, 0),
    SE(1, 1),
    S(0, 1),
    SW(-1, 1),
    W(-1, 0),
    NW(-1, -1);

    int x;
    int y;

    DirectionEnum(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

enum LevelEnum {
    LVL_0(0),
    LVL_1(1),
    LVL_2(2),
    LVL_3(3),
    LVL_4(4);

    public int level;

    LevelEnum(int level) {
        this.level = level;
    }

    public LevelEnum levelUp(LevelEnum levelEnum) {
        return getLevelEnumFromLvl(levelEnum.level + 1);
    }

    public static LevelEnum getLevelEnumFromLvl(int level) {
        for (LevelEnum levelEnum : LevelEnum.values()) {
            if (levelEnum.level == level) {
                return levelEnum;
            }
        }
        throw new IllegalArgumentException("getLevelEnumFromLvl - Pas de LevelEnum associé au level : " + level);
    }
}

class Unit implements Cloneable {
    private int[] position;
    private ArrayList<int[]> potentialPositions;
    private boolean isConcernedByBuild = false;

    public Unit(int[] position) {
        this.position = position;
        this.potentialPositions = new ArrayList<>();
    }

    private Unit(int[] position, ArrayList<int[]> potentialPositions) {
        this.position = position;
        this.potentialPositions = potentialPositions;
    }

    public Unit(ArrayList<int[]> potentialPositions) {
        if (potentialPositions.size() == 1) {
            this.position = potentialPositions.get(0);
        } else {
            this.potentialPositions = potentialPositions;
        }
    }

    public int[] estimatePosition() {
        if (position == null && !potentialPositions.isEmpty()) {
            return getBestPotentialPosition();
        }
        return position;
    }

    public int[] getPosition() {
        return position;
    }

    public ArrayList<int[]> getPotentialPositions() {
        return potentialPositions;
    }

    public boolean isConcernedByBuild() {
        return isConcernedByBuild;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }

    public void setPotentialPositions(ArrayList<int[]> potentialPositions) {
        this.potentialPositions = potentialPositions;
    }

    @Override
    public String toString() {
        StringBuilder potentialPositionsString = new StringBuilder();
        for (int[] potentialPosition : potentialPositions) {
            potentialPositionsString.append(Arrays.toString(potentialPosition));
            potentialPositionsString.append(" ");
        }

        return "Unit{" +
                "position=" + Arrays.toString(position) +
                ", potentialPositions=" + potentialPositionsString.toString() +
                '}';
    }

    public Unit clone() {
        return new Unit(position.clone(), (ArrayList<int[]>) potentialPositions.clone());
    }

    private int[] getBestPotentialPosition() {
        int bestLvl = -1;
        int[] bestPotentialPositions = null;
        for (int[] potentialPosition : potentialPositions) {
            int tmpLvl = Player.getLvl(potentialPosition).level;
            if (bestLvl < tmpLvl) {
                bestLvl = tmpLvl;
                bestPotentialPositions = potentialPosition;
            }
        }
        return bestPotentialPositions;
    }

    public void setIsConcernedByBuild(boolean isConcernedByBuild) {
        this.isConcernedByBuild = isConcernedByBuild;
    }
}
