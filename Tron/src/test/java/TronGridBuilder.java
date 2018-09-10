import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TronGridBuilder {

    public static TronGrid fromFile(String fileName, int maxX, int maxY) {
        TronGrid tronGrid = new TronGrid(maxX, maxY);

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int idxY = 0;
            while ((line = br.readLine()) != null) {
                char[] chars = line.toCharArray();
                for (int idxX = 0; idxX < chars.length; idxX++) {
                    char tmpChar = chars[idxX];
                    if (tmpChar != '.') {
                        tronGrid.set(idxX, idxY, Character.getNumericValue(tmpChar));
                    }
                }
                idxY++;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(fileName + " is malformed.", e);
        }

        return tronGrid;
    }
}