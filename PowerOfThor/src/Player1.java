import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 * ---
 * Hint: You can use the debug stream to print initialTX and initialTY, if Thor seems not follow your orders.
 **/
class Player {

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

        public static DirectionEnum getDirectionEnum(int x, int y) {
            int normX = 0;
            int normY = 0;

            if (Math.abs(x) > Math.abs(y)) {
                normX = x / Math.abs(x);
            } else if (Math.abs(x) < Math.abs(y)) {
                normY = y / Math.abs(y);
            } else {
                normX = x / Math.abs(x);
                normY = y / Math.abs(y);
            }

            for (DirectionEnum directionEnum : DirectionEnum.values()) {
                if (directionEnum.x == normX && directionEnum.y == normY) {
                    return directionEnum;
                }
            }
            return null;
        }
    }

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int lightX = in.nextInt(); // the X position of the light of power
        int lightY = in.nextInt(); // the Y position of the light of power
        int initialTX = in.nextInt(); // Thor's starting X position
        int initialTY = in.nextInt(); // Thor's starting Y position

        int actualTX = initialTX;
        int actualTY = initialTY;

        // game loop
        while (true) {
            int remainingTurns = in.nextInt(); // The remaining amount of turns Thor can move. Do not remove this line.

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            DirectionEnum hasToGo = DirectionEnum.getDirectionEnum(lightX - actualTX, lightY - actualTY);
            actualTX += hasToGo.x;
            actualTY += hasToGo.y;
            // A single line providing the move to be made: N NE E SE S SW W or NW
            System.out.println(hasToGo);
        }
    }
}