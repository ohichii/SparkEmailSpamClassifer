/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package azmah;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.jsoup.Jsoup;
import scala.Tuple2;

/**
 *
 * @author azmah
 */
public class EmailNormalizer {

    static JavaRDD<String> test;
    static JavaRDD<String> training;
    static JavaRDD<String> inputHam;
    static JavaRDD<String> inputSpam;

    public static String normalizeCurrencySymbol(String s) {
        return s.replaceAll("[\\$\\€\\£]", " normalizedcurrency");
    }

    public static String normalizeNumber(String s) {
        return s.replaceAll("\\b\\d+\\b", " normalizednumber");
    }

    public static String normalizeEmoticon(String s) {
        return s.replaceAll("\\p{InEmoticons}", " normalizedemoji").replaceAll("((?::|;|=)(?:-)?(?:\\)|D|P))", " normalizedemoji");

    }

    public static String normalizeURL(String s) {
        return s.replaceAll("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", " normalizedurl");
    }

    public static String normalizeEmailAddress(String s) {
        return s.replaceAll("([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))$", " normalizedemail");
    }

    public static String removePunctuationAndSpecialChar(String s) {
        return s.replaceAll("[^a-zA-Z ]", "");
    }

    public static String removeStopWords(String s, ArrayList<String> stopWords) {
        s = s.replaceAll("\\s+", " ");
        String[] words = s.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if(stopWords.contains(word))
                words[i] = "";
        }
        return String.join(" ", words);
    }

    public static String normalize(String s, ArrayList<String> stopWords) {
        return removeStopWords(
                removePunctuationAndSpecialChar(
                normalizeNumber(
                normalizeCurrencySymbol(
                normalizeEmoticon(
                normalizeURL(
                normalizeEmailAddress(
                Jsoup.parse(s).text()
                )))))).toLowerCase(), stopWords)
                .replaceAll("\\s+", " ");
    }

    public static JavaRDD<String> readAndNormalizeInput(JavaSparkContext sc, String trainingFile, String labelsFile, String stopWordsFile) throws FileNotFoundException {

        ArrayList<String> stopWords = new ArrayList<>();
        Scanner in = new Scanner(new File("stop_words.txt"));
        while (in.hasNext()) {
            String next = in.nextLine();
            stopWords.add(next);
        }

        JavaRDD<String> labels = sc.textFile("CSDMC2010_SPAM/SPAMTrain.label");
        JavaPairRDD<String, String> filesRdd = sc.wholeTextFiles("CSDMC2010_SPAM/TRAINING_SUB/");

        JavaPairRDD<String, Integer> labelPairs = labels.mapToPair(line -> {
            String[] parts = line.split(" ");
            return new Tuple2<>(parts[1], Integer.parseInt(parts[0]));
        });
        // Filter Spam and Ham records from the input
        List<String> spamList = labels.filter((String s) -> s.startsWith("0"))
                .map((String s) -> s.replaceAll("0 ", ""))
                .collect();//SPAM
        List<String> hamList = labels.filter((String s) -> s.startsWith("1"))
                .map((String s) -> s.replaceAll("1 ", ""))
                .collect();//HAM

        Tuple2<String, Integer> labelSample = labelPairs.first();
        int keyLength = labelSample._1.length();

        JavaRDD<String> normalizedInput = filesRdd
                .mapValues((String s) -> EmailNormalizer.normalize(s, stopWords))
                .map((Tuple2<String, String> pair) -> {
                    String nameFile = pair._1.substring(pair._1.length() - keyLength);
                    if (spamList.contains(nameFile)) {
                        return nameFile + ";" + "spam" + "; " + pair._2;
                    }
                    return nameFile + ";" + "ham" + "; " + pair._2;
                });
        /* Split data as testing and training */
        // 70% is training, 30% is testing data

        JavaRDD<String>[] tmp = normalizedInput.randomSplit(new double[]{0.7, 0.3});
        training = tmp[0]; // training set
JavaRDD<String> testTmp = tmp[1]; // test set
        test = testTmp.map((line) -> line.split(";")[1] + ", " + line.split(";")[2]);
        training.cache();
        test.cache();

        inputSpam = training.filter((line) -> spamList.contains(line.split(";")[0]))
                .map((line) -> line.split(";")[2]);
        inputHam = training.filter((line) -> hamList.contains(line.split(";")[0]))
                .map((line) -> line.split(";")[2]);

        inputSpam.saveAsTextFile("data_class/inputSpam/");
        inputHam.saveAsTextFile("data_class/inputHam/");
        test.saveAsTextFile("data_class/test/");
        training.map((line) -> line.split(";")[1] + ", " + line.split(";")[2]).saveAsTextFile("data_class/training/");

        return normalizedInput;
    }

}
