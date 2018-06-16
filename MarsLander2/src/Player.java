import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static final int MAX_Y = 3000;
    public static final double GRAVITY = 3.711;
    public static final double MAX_V_SPEED_DOWN = -36;

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);
        int surfaceN = in.nextInt(); // the number of points used to draw the surface of Mars.
        for (int i = 0; i < surfaceN; i++) {
            int landX = in.nextInt(); // X coordinate of a surface point. (0 to 6999)
            int landY = in.nextInt(); // Y coordinate of a surface point. By linking all the points together in a sequential fashion, you form the surface of Mars.
        }

        // game loop
        while (true) {
            int X = in.nextInt();
            int Y = in.nextInt();
            int hSpeed = in.nextInt(); // the horizontal speed (in m/s), can be negative.
            int vSpeed = in.nextInt(); // the vertical speed (in m/s), can be negative.
            int fuel = in.nextInt(); // the quantity of remaining fuel in liters.
            int rotate = in.nextInt(); // the rotation angle in degrees (-90 to 90).
            int power = in.nextInt(); // the thrust power (0 to 4).

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            System.err.println(Y + " " + vSpeed + " " + fuel + " " + power);

            // 2 integers: rotate power. rotate is the desired rotation angle (should be 0 for level 1), power is the desired thrust power (0 to 4).
            System.out.println("0 " + descendre(Y, vSpeed, power, 0));
        }
    }

    public static int descendre(int actualY, int actualHSpeed, int actualPower, int targetY) {
        if (actualHSpeed >= MAX_V_SPEED_DOWN && actualHSpeed < 0) {
            System.err.println("Good speed");
            return actualPower;
        } else if (actualHSpeed < MAX_V_SPEED_DOWN) {
            System.err.println("Too fast !");
            return upPower(actualPower);
        } else {
            System.err.println("Too low !");
            return downPower(actualPower);
        }
    }

    public static int upPower(int power) {
        if (power < 4) {
            return ++power;
        } else {
            return power;
        }
    }

    public static int downPower(int power) {
        if (power > 0) {
            return --power;
        } else {
            return power;
        }
    }
}