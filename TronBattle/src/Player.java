import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player implements Cloneable {

    public int x;
    public int y;
    public DirectionEnum direction;
    public boolean isDead = false;

    public static void main(String args[]) throws CloneNotSupportedException {
        //InitDatas
        Grid grid = new Grid();
        List<Player> listPlayer = new ArrayList<>(4);

        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
            int N = in.nextInt(); // total number of players (2 to 4).
            int P = in.nextInt(); // your player number (0 to 3).
            for (int i = 0; i < N; i++) {
                int X0 = in.nextInt(); // starting X coordinate of lightcycle (or -1)
                int Y0 = in.nextInt(); // starting Y coordinate of lightcycle (or -1)
                int X1 = in.nextInt(); // starting X coordinate of lightcycle (can be the same as X0 if you play before this player)
                int Y1 = in.nextInt(); // starting Y coordinate of lightcycle (can be the same as Y0 if you play before this player)

                Player player;
                // Initialisation au premier tour
                if (listPlayer.size() != N) {
                    player = new Player();
                    listPlayer.add(player);
                    grid.tab[X0][Y0] = i;
                } else {
                    player = listPlayer.get(i);
                }
                //Si un des joueurs est mort
                if (X0 == -1) {
                    //Si c'est la première fois on le supprime de la grille
                    if (!player.isDead) {
                        grid.resetGridForPlayerId(i);
                        player.isDead = true;
                    }
                } else {
                    grid.tab[X1][Y1] = i;
                    player.x = X1;
                    player.y = Y1;
                }
            }

            //Log de la grille
            grid.printGrid();
            grid.makeVoronoiGrid(listPlayer);

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            Player myPlayer = listPlayer.get(P);
            DirectionEnum move = null;
            for (DirectionEnum moveEnum : DirectionEnum.values()) {
                Player tmpNewPlayer = moveEnum.movePlayer(myPlayer.clone());
                if ((myPlayer.direction == null || myPlayer.direction.canMove(moveEnum))
                        && grid.isValidePosition(tmpNewPlayer.x, tmpNewPlayer.y)) {
                    move = moveEnum;
                    break;
                }
            }
            myPlayer.direction = move;
            System.out.println(move); // A single line with UP, DOWN, LEFT or RIGHT
        }
    }


    @Override
    public Player clone() {
        Player player = new Player();
        player.x = this.x;
        player.y = this.y;
        player.direction = this.direction;
        player.isDead = this.isDead;
        return player;
    }
}

class Grid {

    private static final int MAX_X = 30;
    private static final int MAX_Y = 20;

    public Integer[][] tab = new Integer[MAX_X][MAX_Y];

    public boolean isValidePosition(int x, int y) {
        if (x < 0 || y < 0 || x >= MAX_X || y >= MAX_Y || tab[x][y] != null) {
            return false;
        }
        return true;
    }

    public void printGrid() {
        System.err.println("printGrid");
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                Integer playerId = tab[x][y];
                if (playerId != null) {
                    System.err.print(playerId);
                } else {
                    System.err.print("-");
                }
            }
            System.err.println("");
        }
    }

    public Integer[][] reverseGrid() {
        //System.err.println("reverseGrid");
        Integer[][] res = new Integer[MAX_X][MAX_Y];
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (tab[x][y] != null) {
                    res[x][y] = 9;
                    //System.err.print("-");
                } else {
                    //System.err.print("O");
                }
            }
            //System.err.println("");
        }
        return res;
    }

    public void resetGridForPlayerId(int idPlayer) {
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (idPlayer == tab[x][y]) {
                    tab[x][y] = null;
                }
            }
        }
    }

    /**
     * Repond à la question "Est ce que je gagne du territoire en passant par là ?"
     * ATTENTION ! ça ne devrait pas être une notion de distance mais de chemin
     */
    public int makeVoronoiGrid(List<Player> listPlayer) {
        int result = 0;
        Integer[][] voronoiGrid = reverseGrid();
        //init voronoiGrid
        for (int i = 0; i < listPlayer.size(); i++) {
            Player player = listPlayer.get(i);
            voronoiGrid[player.x][player.y] = i;
        }

        /*System.err.println("Init VoronoiGrid");
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                Integer playerId = voronoiGrid[x][y];
                if (playerId == null) {
                    System.err.print("_");
                } else if (playerId == 9) {
                    System.err.print("-");
                } else if (playerId == 8) {
                    System.err.print("X");
                } else {
                    System.err.print(playerId);
                }
            }
            System.err.println("");
        }
*/
        //int nbIterations = 0;
        while (populateVoronoiGrid(voronoiGrid)) {
            /*System.err.println("Processing... " + ++nbIterations);
            if (nbIterations % 10 == 0) {
                for (int y = 0; y < MAX_Y; y++) {
                    for (int x = 0; x < MAX_X; x++) {
                        Integer playerId = voronoiGrid[x][y];
                        if (playerId == null) {
                            System.err.print("_");
                        } else if (playerId == 9) {
                            System.err.print("-");
                        } else if (playerId == 8) {
                            System.err.print("X");
                        } else {
                            System.err.print(playerId);
                        }
                    }
                    System.err.println("");
                }
            }*/
        }
        System.err.println("VoronoiGrid done");
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                Integer playerId = voronoiGrid[x][y];
                if (playerId == 9) {
                    System.err.print("-");
                } else if (playerId == 8) {
                    System.err.print("X");
                } else {
                    System.err.print(playerId);
                }
            }
            System.err.println("");
        }
        return result;
    }

    private boolean populateVoronoiGrid(Integer[][] voronoiGrid) {
        boolean notEnough = false;
        Integer[][] tmpVoronoiGrid = new Integer[MAX_X][MAX_Y];
        for(int i = 0; i< MAX_X; i++){
            tmpVoronoiGrid[i] = Arrays.copyOf(voronoiGrid[i], MAX_Y);
        }
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                if (tmpVoronoiGrid[x][y] == null) {
                    notEnough = true;
                    Integer playerId = null;
                    for (DirectionEnum directionEnum : DirectionEnum.values()) {

                        try {
                            Integer tmpPlayerId = tmpVoronoiGrid[x + directionEnum.x][y + directionEnum.y];
                            if (tmpPlayerId != null &&  tmpPlayerId < 8) {
                                if (playerId == null) {
                                    playerId = tmpPlayerId;
                                } else if (!playerId.equals(tmpPlayerId)) {
                                    playerId = 8;
                                }
                            }
                        } catch (IndexOutOfBoundsException e) {
                        }
                    }
                    voronoiGrid[x][y] = playerId;
                }
            }
        }
        return notEnough;
    }
}

enum DirectionEnum {

    RIGHT(1, 0),
    UP(0, -1),
    LEFT(-1, 0),
    DOWN(0, 1);

    int x;
    int y;

    DirectionEnum(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Player movePlayer(Player player) {
        player.x += this.x;
        player.y += this.y;
        player.direction = this;
        return player;
    }

    public boolean canMove(DirectionEnum directionEnum) {
        switch (this) {
            case RIGHT:
                if (directionEnum == LEFT) {
                    return false;
                }
                break;
            case UP:
                if (directionEnum == DOWN) {
                    return false;
                }
                break;
            case DOWN:
                if (directionEnum == UP) {
                    return false;
                }
                break;
            case LEFT:
                if (directionEnum == RIGHT) {
                    return false;
                }
                break;
        }
        return true;
    }
}