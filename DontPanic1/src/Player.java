/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int nbFloors = in.nextInt(); // number of floors
        int width = in.nextInt(); // width of the area
        int nbRounds = in.nextInt(); // maximum number of rounds
        int exitFloor = in.nextInt(); // floor on which the exit is found
        int exitPos = in.nextInt(); // position of the exit on its floor
        System.err.println("exit " + exitFloor + " " + exitPos);
        int nbTotalClones = in.nextInt(); // number of generated clones
        int nbAdditionalElevators = in.nextInt(); // ignore (always zero)
        int nbElevators = in.nextInt(); // number of elevators
        Map<Integer, Integer> elevatorMap = new HashMap<>(nbElevators);
        for (int i = 0; i < nbElevators; i++) {
            int elevatorFloor = in.nextInt(); // floor on which this elevator is found
            int elevatorPos = in.nextInt(); // position of the elevator on its floor
            System.err.println("elevator - " + elevatorFloor + " " + elevatorPos);
            elevatorMap.put(elevatorFloor, elevatorPos);
        }

        // game loop
        while (true) {
            int cloneFloor = in.nextInt(); // floor of the leading clone
            int clonePos = in.nextInt(); // position of the leading clone on its floor
            String direction = in.next(); // direction of the leading clone: LEFT or RIGHT

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            Integer targetPos = elevatorMap.get(cloneFloor);
            System.err.println("clone " + cloneFloor + " " + clonePos);

            // action: WAIT or BLOCK
            if ((targetPos != null && clonePos > targetPos && direction.equals("RIGHT")) || (cloneFloor == exitFloor && clonePos > exitPos && direction.equals("RIGHT"))) {
                System.out.println("BLOCK");
            } else if ((targetPos != null && clonePos < targetPos && direction.equals("LEFT")) || (cloneFloor == exitFloor && clonePos < exitPos && direction.equals("LEFT"))) {
                System.out.println("BLOCK");
            } else {
                System.out.println("WAIT");
            }
        }
    }
}