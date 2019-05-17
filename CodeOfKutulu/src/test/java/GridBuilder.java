import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class GridBuilder {

    public static KutuluGrid kutuluGridFromFile(String fileName, int maxX, int maxY) {
        KutuluGrid kutuluGrid = new KutuluGrid(maxX, maxY);

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int idxY = 0;
            while ((line = br.readLine()) != null) {
                kutuluGrid.formatLine(idxY++, line);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(fileName + " is malformed.", e);
        }

        return kutuluGrid;
    }

    public static DepthGrid depthGridFromFile(String fileName, int maxX, int maxY) {
        DepthGrid depthGrid = new DepthGrid(maxX, maxY);

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int idxY = 0;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                for (int x = 0; x < maxX; x++) {
                    Integer value = Integer.valueOf(split[x]);
                    if (value != -1) {
                        depthGrid.set(x, idxY, value);
                    }
                }
                idxY++;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(fileName + " is malformed.", e);
        }

        return depthGrid;
    }
}