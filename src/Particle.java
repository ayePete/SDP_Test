import java.util.ArrayList;
import java.util.Random;

public class Particle {

    private ArrayList<Integer> position;
    private Random rand = new Random();
    private double c;
    private double r;
    private double p;
    private ArrayList<Journal> journalSequence;
    public static final double REVISION_TIME = 30;
    public static final double SCOOP_RATE = 0.001;
    public static final int TOTAL_TIME = 1095;
    public static final int N = 61;

    public double getP() {
        return p;
    }

    public double getR() {
        return r;
    }

    public double getC() {
        return c;
    }


    public ArrayList<Journal> getJournalSequence() {
        return journalSequence;
    }

    public Particle(ArrayList<Integer> position) {
        while (position.size() < N) {
            int toAdd = rand.nextInt(N);
            if (!position.contains(toAdd))
                position.add(toAdd);
        }
        this.position = position;
        decodeSequence();
        computeC();
        computeR();
        computeP();
    }

    private static ArrayList<Journal> decodeSequence(ArrayList<Integer> position) {
        ArrayList<Journal> toReturn = new ArrayList<>();
        for (int i = 0; i < position.size(); i++) {
            toReturn.add(Test.journals.get(position.get(i)));
        }
        return toReturn;
    }

    private void decodeSequence() {
        journalSequence = decodeSequence(position);
    }

    private void computeC() {
        c = computeC(journalSequence);
    }

    private void computeR() {
        r = computeR(journalSequence);
    }

    private void computeP() {
        p = computeP(journalSequence);
    }

    public static double computeC(ArrayList<Journal> journals) {
        double remainingTimeSum;
        double timeDifference;
        double resubmissionRiskProduct;

        int j = 0; // Address case where product term is zero for first journal in sequence.
        double probabilityTerms = journals.get(j).getAcceptanceRate();
        double totalSum = (journals.get(j).getExpectedNumOfCitations() / 365.25)
                * Math.max(TOTAL_TIME - journals.get(j).getSubToPub(), 0) * probabilityTerms;
        double totalProbability = probabilityTerms; // for normalization constant, q;

        for (j = 1; j < journals.size(); j++) {
            remainingTimeSum = 0;
            for (int k = 0; k < j; k++) {
                remainingTimeSum += journals.get(k).getSubToPub();  // using j instead of j-1
                // since index starts from 0, not 1
            }
            timeDifference = TOTAL_TIME - remainingTimeSum - j * REVISION_TIME;
            timeDifference = timeDifference < 0 ? 0 : timeDifference;
            resubmissionRiskProduct = 1;

            for (int k = 0; k < j; k++) {
                resubmissionRiskProduct *= (1 - journals.get(k).getAcceptanceRate()) *
                        Math.pow(1 - SCOOP_RATE, journals.get(k).getSubToPub() + REVISION_TIME);
            }
            probabilityTerms = resubmissionRiskProduct * journals.get(j).getAcceptanceRate();
            totalProbability += probabilityTerms;
            totalSum += (journals.get(j).getExpectedNumOfCitations() / 365.25) * timeDifference * probabilityTerms;
        }
        System.out.println("totalProbability = " + totalProbability);
        System.out.println("totalSum = " + totalSum);
        return totalSum / totalProbability;
    }

    public static double computeR(ArrayList<Journal> journals) {
        int j = 0; // Address case where product term is zero for first journal in sequence.
        double probabilityTerms = journals.get(j).getAcceptanceRate();
        double totalSum = (j + 1) * (TOTAL_TIME - journals.get(j).getSubToPub() > 0 ? 1 : 0) * probabilityTerms;
        double totalProbability = probabilityTerms;

        for (j = 1; j < journals.size(); j++) {
            double resubmissionRiskProduct = 1;
            for (int i = 0; i < j; i++) { // using j instead of j-1 since index starts from 0
                resubmissionRiskProduct *= (1 - journals.get(i).getAcceptanceRate()) *
                        Math.pow(1 - SCOOP_RATE, journals.get(i).getSubToPub() + REVISION_TIME);
            }
            probabilityTerms = journals.get(j).getAcceptanceRate() * resubmissionRiskProduct;
            // for scooping and acceptance

            double hSum = 0;
            for (int k = 0; k < j; k++) {
                hSum += journals.get(k).getSubToPub();
            }
            totalSum += (j + 1) * probabilityTerms * (TOTAL_TIME - hSum - j * REVISION_TIME > 0 ? 1 : 0);
            totalProbability += probabilityTerms;
        }
        System.out.println("-------------- R -----");
        System.out.println("totalProbability = " + totalProbability);
        System.out.println("totalSum = " + totalSum);
        return totalSum / totalProbability;
    }

    public static double computeP(ArrayList<Journal> journals) {
        System.out.println("-------------- P -----");
        int j = 0; // Address case where product term is zero for first journal in sequence.
        double probabilityTerms = journals.get(j).getAcceptanceRate();
        double totalSum = journals.get(j).getSubToPub() *
                (TOTAL_TIME - journals.get(j).getSubToPub() > 0 ? 1 : 0) * probabilityTerms;
        double totalProbability = probabilityTerms;
        //int count = 0;
        for (j = 1; j < journals.size(); j++) {
            double resubmissionRiskProduct = 1;
            for (int i = 0; i < j; i++) { // using j instead of j-1 since index starts from 0
                resubmissionRiskProduct *= (1 - journals.get(i).getAcceptanceRate()) *
                        Math.pow(1 - SCOOP_RATE, journals.get(i).getSubToPub() + REVISION_TIME);
                //++count;
            }
            //System.out.println("count = " + count);
            //count = 0;
            probabilityTerms = journals.get(j).getAcceptanceRate() * resubmissionRiskProduct;
            // for scooping and acceptance
            double hSum = 0;
            for (int k = 0; k < j; k++) {
                hSum += journals.get(k).getSubToPub();
            }
            int heaviside = (TOTAL_TIME - hSum - j * REVISION_TIME) > 0 ? 1 : 0;

            double subPub = journals.get(j).getSubToPub();
            double dum = (subPub + j * REVISION_TIME);
            System.out.println("subPub = " + subPub);
            System.out.println("dum = " + dum);
            System.out.println("heaviside = " + heaviside);
            System.out.println("j = " + j);
            System.out.println("probabilityTerms = " + probabilityTerms);


            totalSum += (journals.get(j).getSubToPub() + j * REVISION_TIME)
                    * probabilityTerms * heaviside;
            totalProbability += probabilityTerms;
        }

        System.out.println("totalProbability = " + totalProbability);
        System.out.println("totalSum = " + totalSum);
        //totalProbability = 0.728;
        return totalSum / totalProbability;
    }

    @Override
    public String toString() {
        return position.toString() + ": C = " + getC() + " R = " + getR() + " " + " P = " + getP() +
                " First: " + getJournalSequence().get(0).getName();
    }

}