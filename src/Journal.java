public class Journal implements Comparable<Journal>{
    private double acceptanceRate;
    private double expectedNumOfCitations;
    private double subToPub;
    private double v;
    private String name;

    public double getV() {
        return v;
    }


    public double getAcceptanceRate() {
        return acceptanceRate;
    }


    public double getExpectedNumOfCitations() {
        return expectedNumOfCitations;
    }

    public double getSubToPub() {
        return subToPub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Journal(){
        this(0, 0, 0);
    }

    public Journal(double acceptanceRate, double expectedNumofPublications, double subToPub){
        this.acceptanceRate = acceptanceRate;
        this.expectedNumOfCitations = expectedNumofPublications;
        this.subToPub = subToPub;
        computeV();
    }

    public void computeV(){
        double num = acceptanceRate * expectedNumOfCitations*(1 - subToPub/ Particle.TOTAL_TIME);
        double den = 1 - (1 - subToPub/Particle.TOTAL_TIME - Particle.REVISION_TIME / Particle.TOTAL_TIME)
                * (1 - acceptanceRate) * Math.pow(1 - Particle.SCOOP_RATE, Particle.REVISION_TIME + subToPub);
        v = num/den;
    }

    @Override
    public int compareTo(Journal j) {
        if(v > j.getV())
            return 1;
        else if(v < j.getV())
            return -1;
        else
            return 0;
    }

    public String toString(){
        return name + " (" + acceptanceRate + ", " + subToPub + ", " + expectedNumOfCitations + ")";
    }

    public boolean equals(Object j){
        if(j instanceof Journal) {
            Journal journal = (Journal) j;
            return name.equals(journal.getName());
        }
        else
            return false;
    }
}
