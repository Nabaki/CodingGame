import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static Map<Integer, Node> nodeMap;

    public static class Node {

        List<Integer> voisins = new ArrayList<>();
        boolean isExit = false;

        @Override
        public String toString() {
            return "Node{" +
                    "voisins=" + voisins +
                    ", isExit=" + isExit +
                    '}';
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int N = in.nextInt(); // the total number of nodes in the level, including the gateways
        int L = in.nextInt(); // the number of links
        int E = in.nextInt(); // the number of exit gateways
        nodeMap = new HashMap<>(N);

        for (int i = 0; i < L; i++) {
            int N1 = in.nextInt(); // N1 and N2 defines a link between these nodes
            int N2 = in.nextInt();

            //Ajout des noeds
            if (!nodeMap.containsKey(N1)) {
                nodeMap.put(N1, new Node());
            }
            if (!nodeMap.containsKey(N2)) {
                nodeMap.put(N2, new Node());
            }
            //Ajout des liens entre nodes
            nodeMap.get(N1).voisins.add(N2);
            nodeMap.get(N2).voisins.add(N1);
        }
        for (int i = 0; i < E; i++) {
            int EI = in.nextInt(); // the index of a gateway node
            nodeMap.get(EI).isExit = true;
        }

        System.err.println("nodeMap");
        for (Integer integer : nodeMap.keySet()) {
            System.err.println(integer + "->" + nodeMap.get(integer).toString());
        }
        System.err.println("----");

        // game loop
        while (true) {
            int SI = in.nextInt(); // The index of the node on which the Skynet agent is positioned this turn
            System.err.println("SI " + SI);
            List<List<Integer>> cheminList = initChemins(SI);
            printChemins(cheminList);

            // Example: 0 1 are the indices of the nodes you wish to sever the link between
            System.out.println(getWorseNode(SI, cheminList));
        }
    }

    public static List<List<Integer>> initChemins(Integer depart) {
        List<List<Integer>> cheminList = new ArrayList<>();
        List<Integer> blackList = new ArrayList<>();

        List<Integer> voisinList = nodeMap.get(depart).voisins;
        for (Integer voisin : voisinList) {
            if (blackList.contains(voisin)) {
                continue;
            } else {
                blackList.add(voisin);
            }
            if (nodeMap.get(voisin).isExit) {
                System.err.println(voisin + " is an exit");
                cheminList.add(Arrays.asList(voisin));
                break;
            } else {
                cheminList.addAll(etoffeChemins(Arrays.asList(voisin), new ArrayList<>(blackList)));
            }
        }

        return cheminList;
    }

    private static List<List<Integer>> etoffeChemins(List<Integer> chemin, List<Integer> blackList) {
        System.err.println("etoffeChemins " + Arrays.toString(chemin.toArray()));
        List<List<Integer>> cheminList = new ArrayList<>();

        Node lastNode = nodeMap.get(chemin.get(chemin.size() - 1));
        for (Integer voisin : lastNode.voisins) {
            if (blackList.contains(voisin)) {
                continue;
            } else {
                blackList.add(voisin);
            }
            List<Integer> tmpChemin = new ArrayList<>(chemin);
            if (nodeMap.get(voisin).isExit) {
                System.err.println(voisin + " is an exit");
                tmpChemin.add(voisin);
                cheminList.add(tmpChemin);
                break;
            } else {
                cheminList.addAll(etoffeChemins(tmpChemin, new ArrayList<>(blackList)));
            }
        }

        return cheminList;
    }

    public static void printChemins(List<List<Integer>> chemins) {
        System.err.println("printChemins");
        for (List<Integer> chemin : chemins) {
            System.err.println(Arrays.toString(chemin.toArray()));
        }
    }

    public static String getWorseNode(Integer depart, List<List<Integer>> chemins) {

        Integer worseSize = null;
        Integer worseNode = null;
        for (List<Integer> chemin : chemins) {
            Integer tmpSize = chemin.size();
            if (worseSize == null || worseSize > tmpSize) {
                worseSize = tmpSize;
                worseNode = chemin.get(0);
            }
        }
        return depart + " " + worseNode;
    }

}