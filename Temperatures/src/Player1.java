import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Solution {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(); // the number of temperatures to analyse
        if (in.hasNextLine()) {
            in.nextLine();
        }
        String temps = in.nextLine(); // the n temperatures expressed as integers ranging from -273 to 5526
        System.err.println(temps);

        if (temps != null && !temps.isEmpty()) {

            Integer bestTemp = null;
            for (String s : temps.split(" ")) {
                int tmpTemp = Integer.valueOf(s);

                if (bestTemp == null || Math.abs(bestTemp) > Math.abs(tmpTemp)) {
                    bestTemp = tmpTemp;
                } else if (Math.abs(bestTemp) == Math.abs(tmpTemp) && tmpTemp > 0) {
                    bestTemp = tmpTemp;
                }
            }

            System.out.println(bestTemp);
        } else {
            System.out.println(0);
        }
    }
}