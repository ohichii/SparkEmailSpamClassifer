/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maweiming;

//import com.maweiming.spark.mllib.utils.FileUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import org.json4s.jackson.Json;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;

/**
 *
 * @author azmah
 */
public class DataFactory {

    //public static final String CLASS_PATH = FileUtils.getClassPath();
    public static final String NEWS_DATA_PATH = "/data/NewsData";

    public static final String DATA_TRAIN_PATH = "/data/data-train.txt";

    public static final String DATA_TEST_PATH = "/data/data-test.txt";

    public static final String MODELS = "/data/models";

    public static final String MODEL_PATH = "/data/models/category";

    public static final String LABEL_PATH = "/data/models/labels.txt";

    public static final String TF_PATH = "/data/models/tf";

    public static final String IDF_PATH = "/data/models/idf";

    public static void main(String[] args) throws FileNotFoundException, IOException {
        Scanner in = new Scanner(new File("TTDATA2/part-00000"));
        Writer out = new FileWriter(new File("TestingData.json"));
        in.nextLine();
        while (in.hasNext()) {
            String next = in.nextLine();
            String[] parts = next.split(",");
            if (parts.length > 1) {
                String[] subjWords = parts[1].split(" ");
                if (subjWords.length != 0) {
                    out.write("{ \"label\" : " + parts[0]
                            + ", \"subject\" : [ ");
                    for (String subjWord : subjWords) {
                        if (subjWord.length() > 0) {
                            out.write("\"" + subjWord + "\", ");
                        }
                    }
                    out.write(" ] } \n");
                }

            }
        }
        in.close();
        out.close();
    }
}

public class Result {

    private Long label;//text label

    private Long predict;//predict label

    public boolean isCorrect(){
        return label.equals(predict);
    }

    public Result() {
    }

    public Result(Long label, Long predict) {
        this.label = label;
        this.predict = predict;
    }

    public Long getLabel() {
        return label;
    }

    public void setLabel(Long label) {
        this.label = label;
    }

    public Long getPredict() {
        return predict;
    }

    public void setPredict(Long predict) {
        this.predict = predict;
}
}

public class AnsjUtils {

    public static List<String> participle(String text){
        List<String> wordList = new ArrayList<>();
        Result result = NlpAnalysis.parse(text);
        List<Term> terms = result.getTerms();
        for(Term term:terms){
            String name = term.getName();
            wordList.add(name);
        }
        return wordList;
    }

    public static String participleText(String text) {
        StringBuilder wordText = new StringBuilder();
        Result result = NlpAnalysis.parse(text);
        List<Term> terms = result.getTerms();

        for (int i = 0; i < terms.size(); i++) {
            Term term = terms.get(i);
            String word = term.getName();
            if(i!=0){
                wordText.append(" ");
            }
            wordText.append(word);
        }

        return wordText.toString();
    }
}
