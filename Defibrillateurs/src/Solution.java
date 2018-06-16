/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Solution {

    public static class Defibrilateur {

        String nom;
        String adresse;
        String telephone;
        double longitude;
        double latitude;

        public Defibrilateur(String nom, String adresse, String telephone, double longitude, double latitude) {
            this.nom = nom;
            this.adresse = adresse;
            this.telephone = telephone;
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public double getDistanceFrom(double targetLong, double targetLat) {
            double abscisses = (this.longitude - targetLong) * Math.cos((this.latitude + targetLat) / 2);
            double ordonnees = (this.latitude - targetLat);
            return Math.sqrt(Math.pow(abscisses, 2) + Math.pow(ordonnees, 2)) * 6371;
        }
    }

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        double LON = Double.parseDouble(in.next().replace(",", "."));
        double LAT = Double.parseDouble(in.next().replace(",", "."));
        int N = in.nextInt();
        if (in.hasNextLine()) {
            in.nextLine();
        }

        Map<Long, Defibrilateur> defibMap = new HashMap<>();
        for (int i = 0; i < N; i++) {
            String[] DEFIB = in.nextLine().split(";");
            System.err.println(Arrays.toString(DEFIB));
            defibMap.put(
                    Long.parseLong(DEFIB[0]),
                    new Defibrilateur(
                            DEFIB[1],
                            DEFIB[2],
                            DEFIB[3],
                            Double.parseDouble(DEFIB[4].replace(",", ".")),
                            Double.parseDouble(DEFIB[5].replace(",", "."))
                    )
            );
        }

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");
        Long closerId = null;
        Double bestDistance = null;
        for (Long aLong : defibMap.keySet()) {
            Double tmpDistance = defibMap.get(aLong).getDistanceFrom(LON, LAT);
            if (bestDistance == null || tmpDistance < bestDistance) {
                bestDistance = tmpDistance;
                closerId = aLong;
            }
        }

        System.out.println(defibMap.get(closerId).nom);
    }
}