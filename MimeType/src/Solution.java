import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Solution {

    public static void main(String args[]) {

        Map<String, String> mimeMap = new HashMap<>();

        Scanner in = new Scanner(System.in);
        int N = in.nextInt(); // Number of elements which make up the association table.
        int Q = in.nextInt(); // Number Q of file names to be analyzed.
        for (int i = 0; i < N; i++) {
            String EXT = in.next(); // file extension
            String MT = in.next(); // MIME type.
            System.err.println(EXT + " -> " + MT);
            mimeMap.put(EXT.toLowerCase(), MT);
        }
        in.nextLine();
        for (int i = 0; i < Q; i++) {
            String FNAME = in.nextLine(); // One file name per line.
            System.err.print(FNAME);
            String result = null;
            if (FNAME.matches("[^\\.]*(\\.[^\\.]*)+")) {
                String[] splitFileName = FNAME.toLowerCase().split("\\.", -1);
                if (splitFileName.length > 0) {
                    result = mimeMap.get(splitFileName[splitFileName.length - 1]);
                }
            }
            if (result == null) {
                result = "UNKNOWN";
            }
            System.err.println(" -> " + result);
            System.out.println(result);
        }

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");


        // For each of the Q filenames, display on a line the corresponding MIME type. If there is no corresponding type, then display UNKNOWN.
        //System.out.println("UNKNOWN");
    }
}