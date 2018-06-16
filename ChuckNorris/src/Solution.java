import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Solution {

    public static void main(String args[]) throws UnsupportedEncodingException {
        Scanner in = new Scanner(System.in);
        String MESSAGE = in.nextLine();

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        StringBuilder binaryMessage = new StringBuilder();
        for (byte b : MESSAGE.getBytes(StandardCharsets.US_ASCII)) {
            binaryMessage.append(Integer.toBinaryString((1 << 7) | b).substring(1));
        }

        char oldChar = '2';
        StringBuilder result = new StringBuilder();
        for (char c : binaryMessage.toString().toCharArray()) {
            if (oldChar != c) {
                if (c == '0') {
                    result.append(" 00 ");
                } else if (c == '1') {
                    result.append(" 0 ");
                } else {
                    throw new IllegalArgumentException("wtf oldChar is " + oldChar);
                }
            }
            result.append(0);
            oldChar = c;
        }
        System.out.println(result.toString().substring(1));
    }
}