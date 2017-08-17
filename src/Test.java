import java.io.*;
import java.util.*;

public class Test {
    public static ArrayList<Journal> journals;

    public static void main(String[] args){
        readData();
        ArrayList<Integer> testSchedule = new ArrayList<>(Arrays.asList(13, 56, 25, 15, 45, 51, 42, 30, 28, 32));
        Particle p = new Particle(testSchedule);
        System.out.println(p);
    }

    public static void readData(){
        journals = new ArrayList<>();
        double acceptanceRate;
        double subToPub;
        double expectedNumOfCitations;

        FileReader fr;
        try {
            File f = new File("res\\journal_data.txt");
            fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String s;
            while (br.ready()) {
                s = br.readLine();
                String[] sHolder = s.split("\\s+");
                acceptanceRate = Double.parseDouble(sHolder[0]);
                subToPub = Double.parseDouble(sHolder[1]);
                expectedNumOfCitations = Double.parseDouble(sHolder[2]);

                Journal j = new Journal(acceptanceRate, expectedNumOfCitations, subToPub);
                journals.add(j);
            }

            f = new File("res\\journal_names.txt");
            br = new BufferedReader(new FileReader(f));
            int i = 0;
            while (br.ready()) {
                s = br.readLine();
                journals.get(i).setName(s);
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
