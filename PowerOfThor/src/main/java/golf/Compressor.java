package golf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Compressor {
    private static final String FILENAME = "PowerOfThor\\src\\main\\java\\golf\\Player.java";

    public static void main(String args[]) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILENAME))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                if(!sCurrentLine.contains("package")) {
                    System.out.print(
                            sCurrentLine
                                    .replaceAll("^ +", "")
                                    .replaceAll(" +", " ")
                                    .replaceAll(" \\{", "{")
                                    .replaceAll("\\{ ", "{")
                                    .replaceAll("} ", "}")
                                    .replaceAll(" }", "}")
                                    .replaceAll(" \\(", "(")
                                    .replaceAll("\\( ", "(")
                                    .replaceAll("\\) ", ")")
                                    .replaceAll(" \\)", ")")
                                    .replaceAll(", ", ",")
                                    .replaceAll(" = ", "=")
                                    .replaceAll(" - ", "-")
                                    .replaceAll(" + ", "+")
                                    .replaceAll(" == ", "==")
                                    .replaceAll(" : ", ":")
                                    .replaceAll(" / ", "/")
                                    .replaceAll(" \\? ", "?")
                                    .replaceAll("; ", ";")
                    );
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
