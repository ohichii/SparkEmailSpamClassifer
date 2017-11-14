/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package azmah;

/**
 *
 * @author azmah
 */
import java.util.List;
import java.util.Map;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;
import java.util.Arrays;
import org.apache.spark.api.java.Optional;

public class Classifier {

    static Map<String, WordDetails> model;
    static JavaPairRDD<String, Integer> spamCounts;
    static JavaPairRDD<String, Integer> hamCounts;
    static float pSpam;
    static float pHam;
    static float accuracy;
    static float precision;
    static float recall;
    static int truePos = 0;
    static int falsePos = 0;
    static int falseNeg = 0;
    static int trueNeg = 0;

    /*
	 * Takes an input and outputs count of each word
     */
    public static JavaPairRDD<String, Integer> countWords(
            JavaRDD<String> input) throws Exception {

        /* Map *///(String x) -> Arrays.asList(x.split(" "))
        // Split each line to multiple words
        JavaRDD<String> words = input.flatMap((s) -> Arrays.asList(s.split(" ")).iterator());

        // Turn the words into (word, 1) pairs
        JavaPairRDD<String, Integer> wordOnePairs = words.mapToPair((String x) -> new Tuple2<>(x, 1));

        /* Reduce */
        // Add the pairs by key to produce counts
        JavaPairRDD<String, Integer> counts = wordOnePairs.reduceByKey((Integer x, Integer y) -> x + y);

        return counts;
    }

    /**
     *
     * @param countRDD (RDD with wordcount)
     * @return Integer
     */
    public static Integer addCounts(JavaPairRDD<String, Integer> countRDD) {
        return countRDD.values().reduce((Integer x, Integer y) -> x + y);
    }



    public static void computeProbability(JavaSparkContext jsc) throws Exception {
        // Calculate overall probability of Spam and Ham
        JavaRDD<String> spamRdd = jsc.textFile("data_class/inputSpam/");
        JavaRDD<String> hamRdd = jsc.textFile("data_class/inputHam/");
        long cntSpamRecords = spamRdd.count();
        long cntHamRecords = hamRdd.count();
        pSpam = (float) cntSpamRecords / (float) (cntSpamRecords + cntHamRecords);
        pHam = (float) cntHamRecords / (float) (cntSpamRecords + cntHamRecords);
        // Call WordCount to compute frequency of each word
        spamCounts = countWords(spamRdd);
        hamCounts = countWords(hamRdd);
    }

    public static void makeDictionary(JavaSparkContext jsc) throws Exception {
      File f1 = new File("data_class/dictionary/");
      if(f1.exists()) return;
        final Broadcast<Integer> cntWordsSpam = jsc.broadcast(addCounts(spamCounts)); // Total unique words in spam dataset
        final Broadcast<Integer> cntWordsHam = jsc.broadcast(addCounts(hamCounts)); // Total unique words in ham dataset

        JavaPairRDD<String, WordDetails> one = spamCounts.mapValues((Integer s) -> {
            WordDetails wordDet = new WordDetails();
            wordDet.setCntSpam(s);
            return wordDet;
        });
        JavaPairRDD<String, WordDetails> two = hamCounts.mapValues((Integer s) -> {
            WordDetails wordFreq = new WordDetails();
            wordFreq.setCntHam(s);
            return wordFreq;
        });
        JavaPairRDD<String, Tuple2<Optional<WordDetails>, Optional<WordDetails>>> mergedRDD = one.fullOuterJoin(two);

        // Calculate Spam and Ham probabilities for each word
        @SuppressWarnings("serial")
        JavaPairRDD<String, WordDetails> dictionary = mergedRDD.mapValues((Tuple2<Optional<WordDetails>, Optional<WordDetails>> v1) -> {
            WordDetails word = new WordDetails();
            if (v1._1.isPresent()) {
                word.setCntSpam(v1._1.get().getCntSpam());
            }

            if (v1._2.isPresent()) {
                word.setCntHam(v1._2.get().getCntHam());
            }
            word.updateProbabilities(cntWordsSpam.value(), cntWordsHam.value());
            return word;
        });
        //dictionary.cache();

        // Create a Map from the RDD

        dictionary.saveAsObjectFile("data_class/dictionary/");
    }

    /**
     *
     * @param text
     * @return
     */
    public static boolean isSpam(String text) {

        String[] tokens = text.split(" ");

        float spamProbability = 1.0f;
        float hamProbability = 1.0f;

        for (String token : tokens) {
            if (model.containsKey(token)) {
                spamProbability *= model.get(token).getpSpam();
                hamProbability *= model.get(token).getpHam();
            }
        }

        spamProbability = spamProbability * pSpam;
        hamProbability = hamProbability * pHam;

        return spamProbability > hamProbability;
    }

    public static void testClassification(JavaSparkContext jsc) {
        JavaRDD<Tuple2<String, WordDetails>> modelRdd = jsc.objectFile("data_class/dictionary/");
        model = modelRdd.mapToPair(t -> new Tuple2<>(t._1, t._2)).collectAsMap();
        /* Classification */
        JavaRDD<String> testRdd = jsc.textFile("data_class/test/");
        JavaRDD<String> cleanedTestData = testRdd.filter((String s) -> {
            boolean goodRec = true;

            if (s == null || s.trim() == "") {
                goodRec = false;
            }
            String[] parts = s.split(",");
            if (parts.length != 2) {
                goodRec = false;
            }

            return goodRec;
        });
        List<String> testingData = cleanedTestData.collect();
        boolean[] testingDataResult = new boolean[testingData.size()];
        boolean[] classifierResult = new boolean[testingData.size()];

        truePos = 0;
        falsePos = 0;
        falseNeg = 0;
        trueNeg = 0;

        for (int i = 0; i < testingData.size(); i++) {
            String[] parts = testingData.get(i).split(",");

            testingDataResult[i] = "spam".equals(parts[0]);
            classifierResult[i] = isSpam(parts[1]);

            if (testingDataResult[i]) { // Actual data - Spam
                if (classifierResult[i]) {
                    truePos++; // Spam Spam
                } else {
                    falsePos++; // Spam Ham
                }
            } else {
                if (classifierResult[i]) {
                    falseNeg++; // Ham Spam
                } else {
                    trueNeg++; // Ham Ham
                }
            }
        }

        //System.out.println("Training data size : " + EmailNormalizer.training.count());
        System.out.println("Testing data size : " + testingData.size());

        System.out.println("True Positive : " + truePos);
        System.out.println("False Positive : " + falsePos);
        System.out.println("False Negative : " + falseNeg);
        System.out.println("True Negative : " + trueNeg);

        // Accuracy = TP + TN / Total
        accuracy = (float) (truePos + trueNeg) / (float) (truePos + falsePos + falseNeg + trueNeg);

        // Precision = TP / (TP + FP)
        precision = (float) truePos / (float) (truePos + falsePos);

        // Recall = TP / (TP + FN)
        recall = (float) truePos / (float) (truePos + falseNeg);

        System.out.println("Accuracy : " + accuracy);
        System.out.println("Precision : " + precision);
        System.out.println("Recall : " + recall);
    }

}
