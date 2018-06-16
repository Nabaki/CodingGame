import java.util.*;
import java.io.*;
import java.math.*;

/**
 * The while loop represents the game.
 * Each iteration represents a turn of the game
 * where you are given inputs (the heights of the mountains)
 * and where you have to print an output (the index of the mountain to fire on)
 * The inputs you are given are automatically updated according to your last actions.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        List mountains = new ArrayList<>();
        // game loop
        while (true) {
            mountains.clear();
            for (int i = 0; i < 8; i++) {
                mountains.add(in.nextInt()); // represents the height of one mountain.
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            int bestIndex = -1;
            int bestHeight = -1;
            for (int i = 0; i < mountains.size(); i++) {
                int tmpHeight = (int) mountains.get(i);
                if (tmpHeight > bestHeight) {
                    bestHeight = tmpHeight;
                    bestIndex = i;
                }
            }
            System.out.println(bestIndex); // The index of the mountain to fire on.
        }
    }
}