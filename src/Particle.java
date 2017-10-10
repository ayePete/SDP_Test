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
    public static final int N = 5;

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
        p = computeP_jMax(journalSequence);
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

        for (j = 1; j < N; j++) {
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

        for (j = 1; j < N; j++) {
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
        for (j = 1; j < N; j++) {
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
            int heaviside = (TOTAL_TIME - hSum - j * REVISION_TIME) > 0 ? 1 : 0;

            totalSum += (journals.get(j).getSubToPub() + j * REVISION_TIME)
                    * probabilityTerms * heaviside;
            totalProbability += probabilityTerms;
        }

        System.out.println("totalProbability = " + totalProbability);
        System.out.println("totalSum = " + totalSum);
        //totalProbability = 0.728;
        return totalSum / totalProbability;
    }

    public static double computeP_jMax(ArrayList<Journal> journals){
        System.out.println("-------------- P_JMax -----");
        int jMax = 0;
        double sum = 0;
        for (int j = 0; j < N; j++) {
            sum += journals.get(j).getSubToPub();
            double tDifference = TOTAL_TIME - sum -  j * REVISION_TIME;
            if(tDifference > 0) {
                jMax = j;
            } else
                break;
        }

        System.out.println("jMax = " + jMax);
        double firstSumTerm = 0;
        double jSum = 0;
        double resubmissionRiskProduct = 1;
        double alphaProd = 0;
        for (int j = 0; j <= jMax; j++) {

            double preJSum = 0;
            for (int i = 0; i < j; i++) {
                preJSum += journals.get(i).getSubToPub();
            }

            jSum = preJSum + ((j+1) * REVISION_TIME);

            resubmissionRiskProduct = 1;
            for (int k = 0; k < j; k++) { // using j instead of j-1 since index starts from 0
                resubmissionRiskProduct *= (1 - journals.get(k).getAcceptanceRate()) *
                        Math.pow(1 - SCOOP_RATE, journals.get(k).getSubToPub() + REVISION_TIME);
            }
            firstSumTerm += journals.get(j).getAcceptanceRate() * jSum * resubmissionRiskProduct;
            alphaProd += journals.get(j).getAcceptanceRate() * resubmissionRiskProduct;
        }
        System.out.println("jSum = " + jSum);
        System.out.println("resubmissionRiskProduct = " + resubmissionRiskProduct);
        System.out.println("alphaProd = " + alphaProd);

        System.out.println("firstSumTerm = " + firstSumTerm);

        // Min part
        double minPreJSum = 0;
        for (int i = 0; i <= jMax; i++) {
            minPreJSum += journals.get(i).getSubToPub();
        }
        double minJSum = minPreJSum + ((jMax+1) * REVISION_TIME);


        double minJMaxProdSum = 0;
        for (int j = 0; j <= jMax; j++) {
            double jMaxProd = 1;
            for (int k = 0; k < j; k++) {
                jMaxProd *= (1 - journals.get(k).getAcceptanceRate()) *
                        Math.pow(1 - SCOOP_RATE, journals.get(k).getSubToPub() + REVISION_TIME);
            }
            minJMaxProdSum += journals.get(j).getAcceptanceRate() * jMaxProd;
        }
        System.out.println("minJMaxProdSum = " + (minJMaxProdSum));
        double minProdTerm = minJSum * (1 - minJMaxProdSum);
        System.out.println("minJS = " + minJSum);
        System.out.println("minProdTerm = " + minProdTerm);
        double totalSum = firstSumTerm + Math.min(TOTAL_TIME, minProdTerm);

        System.out.println("totalSum = " + totalSum);

        return totalSum;
    }

    @Override
    public String toString() {
        return position.toString() + ": C = " + getC() + " R = " + getR() + " " + " P = " + getP() +
                " First: " + getJournalSequence().get(0).getName();
    }

}
