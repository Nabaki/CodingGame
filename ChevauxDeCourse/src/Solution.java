/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Solution {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        //List<Integer> inList = Arrays.asList(1,5,7,2,3,8,4);
        List<Integer> listHorses = new ArrayList<>();

        int N = in.nextInt();
        for (int i = 0; i < N; i++) {
            int pi = in.nextInt();
            listHorses.add(pi);
        }
/*
        for (Integer integer : inList) {
            listHorses.add(integer);
        }

        /*System.err.println("Set");
        listHorses.forEach( nb -> System.err.println(nb));
        System.err.println("delta");*/
        Collections.sort(listHorses);
        Integer oldInt = null;
        Integer bestDelta = null;

        for (Integer integer : listHorses) {
            if (oldInt != null) {
                int tmpDelta = Math.abs(oldInt - integer);
                System.err.println(tmpDelta);
                if (bestDelta == null || tmpDelta < bestDelta) {
                    bestDelta = tmpDelta;
                }
            }
            oldInt = integer;
        }

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");
        System.out.println(bestDelta);
    }
}