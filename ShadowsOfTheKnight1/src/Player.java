import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static int minW = 0;
    public static int maxW;
    public static int minH = 0;
    public static int maxH;

    enum DirectionEnum {
        U,
        UR,
        R,
        DR,
        D,
        DL,
        L,
        UL
    }

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);
        maxW = in.nextInt(); // width of the building.
        maxH = in.nextInt(); // height of the building.
        int N = in.nextInt(); // maximum number of turns before game over.
        int X0 = in.nextInt();
        int Y0 = in.nextInt();
        int[] position = new int[]{X0, Y0};
        System.err.println(Arrays.toString(position));

        // game loop
        while (true) {
            String bombDir = in.next(); // the direction of the bombs from batman's current location (U, UR, R, DR, D, DL, L or UL)
            move(position, DirectionEnum.valueOf(bombDir));

            System.err.println(minW + " " + minH + " " + maxW + " " + maxH);

            // the location of the next window Batman should jump to.
            System.out.println(position[0] + " " + position[1]);
        }
    }

    public static void move(int[] position, DirectionEnum directionEnum) {

        switch (directionEnum) {
            case U:
                maxH = position[1] - 1;
                position[1] = maxH - (maxH - minH) / 2;
                break;
            case UR:
                maxH = position[1] - 1;
                position[1] = maxH - (maxH - minH) / 2;
                minW = position[0] + 1;
                position[0] = minW + (maxW - minW) / 2;
                break;
            case R:
                minW = position[0] + 1;
                position[0] = minW + (maxW - minW) / 2;
                break;
            case DR:
                minH = position[1] + 1;
                position[1] = minH + (maxH - minH) / 2;
                minW = position[0] + 1;
                position[0] = minW + (maxW - minW) / 2;
                break;
            case D:
                minH = position[1] + 1;
                position[1] = minH + (maxH - minH) / 2;
                break;
            case DL:
                minH = position[1] + 1;
                position[1] = minH + (maxH - minH) / 2;
                maxW = position[0] - 1;
                position[0] = maxW - (maxW - minW) / 2;
                break;
            case L:
                maxW = position[0] - 1;
                position[0] = maxW - (maxW - minW) / 2;
                break;
            case UL:
                maxH = position[1] - 1;
                position[1] = maxH - (maxH - minH) / 2;
                maxW = position[0] - 1;
                position[0] = maxW - (maxW - minW) / 2;
                break;
        }
    }
}