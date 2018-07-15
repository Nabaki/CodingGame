package golf;

import java.util.Scanner;

class Player {
    enum D {
        N(0, -1), NE(1, -1), E(1, 0), SE(1, 1), S(0, 1), SW(-1, 1), W(-1, 0), NW(-1, -1);
        int x, y;

        D(int _x, int _y) {
            x = _x;
            y = _y;
        }
    }

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        int lX = s.nextInt(), lY = s.nextInt(), dX = lX - s.nextInt(), dY = lY - s.nextInt();

        while (true) {
            s.nextInt();

            for (D d : D.values()) {
                if (d.x == (dX == 0 ? 0 : dX / Math.abs(dX)) && d.y == (dY == 0 ? 0 : dY / Math.abs(dY))) {
                    dX -= d.x;
                    dY -= d.y;
                    System.out.println(d);
                    break;
                }
            }
        }
    }
}