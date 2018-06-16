/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Solution {

    public static final int NUM_ALPHA = 26;
    public static final int FIRST_ASCII = 'a';

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int L = in.nextInt();
        int H = in.nextInt();

        if (in.hasNextLine()) {
            in.nextLine();
        }
        String T = in.nextLine();
        System.err.println("String to print : " + T);

        //Récupérer l'alphabet en ascii art
        Map<String, List<String>> alphaMap = new HashMap<>();
        for (int i = 0; i < H; i++) {
            String ROW = in.nextLine();
            System.err.println(ROW);
            for (int j = 0; j < NUM_ALPHA + 1; j++) {
                if (alphaMap.containsKey(Character.toString((char) (FIRST_ASCII + j)))) {
                    String asciiLetter = ROW.substring(L * j, L * (j + 1));
                    String letter = Character.toString((char) (FIRST_ASCII + j));
                    System.err.println(letter + " -> '" + asciiLetter + "'");
                    alphaMap.get(letter).add(asciiLetter);
                } else {
                    String asciiLetter = ROW.substring(L * j, L * (j + 1));
                    String letter = Character.toString((char) (FIRST_ASCII + j));
                    System.err.println(letter + " -> '" + asciiLetter + "'");
                    alphaMap.put(letter, new ArrayList<>(Arrays.asList(asciiLetter)));
                }
            }
        }

        //Verifier que la chaine de caracteres est une chaine de lettres
        String clearT = T.replaceAll("(?![a-zA-Z]).", "{");
        System.err.println("T -> " + T + ", clearT -> " + clearT);
        //Ecrire la chaine de caractères donnée
        StringBuilder asciiAnswer;
        for (int i = 0; i < H; i++) {
            asciiAnswer = new StringBuilder();

            for (char c : clearT.toCharArray()) {
                asciiAnswer.append(alphaMap.get(String.valueOf(c).toLowerCase()).get(i));
            }
            System.out.println(asciiAnswer.toString());
        }
    }
}