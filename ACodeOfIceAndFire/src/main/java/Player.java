import java.util.*;
import java.io.*;
import java.math.*;
import java.util.function.Function;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int numberMineSpots = in.nextInt();
        for (int i = 0; i < numberMineSpots; i++) {
            int x = in.nextInt();
            int y = in.nextInt();
        }

        // game loop
        while (true) {
            int gold = in.nextInt();
            int income = in.nextInt();
            int opponentGold = in.nextInt();
            int opponentIncome = in.nextInt();
            for (int i = 0; i < 12; i++) {
                String line = in.next();
            }
            int buildingCount = in.nextInt();
            for (int i = 0; i < buildingCount; i++) {
                int owner = in.nextInt();
                int buildingType = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
            }
            int unitCount = in.nextInt();
            for (int i = 0; i < unitCount; i++) {
                int owner = in.nextInt();
                int unitId = in.nextInt();
                int level = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            System.out.println("WAIT");
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

    protected void printGrid(String nullString, Function<T, String> printNodeFunction) {
        for (int y = 0; y < MAX_Y; y++) {
            for (int x = 0; x < MAX_X; x++) {
                T node = get(x, y);
                if (node == null) {
                    System.err.print(nullString);
                } else {
                    System.err.print(printNodeFunction.apply(node));
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