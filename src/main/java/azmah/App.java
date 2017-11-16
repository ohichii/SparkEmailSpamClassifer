package azmah;

import java.util.HashMap;
import java.util.Map;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDFModel;
import org.apache.spark.ml.linalg.SparseVector;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import static spark.Spark.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import maweiming.*;

public class App {
    static long totalTime1;
    static long totalTime2;
    public static void main(String[] args) throws Exception {

        staticFileLocation("/public");
        String layout = "templates/layout.vtl";
        SparkConf conf = new SparkConf().setAppName("EmailClassifier").setMaster("local");
        JavaSparkContext jsc = new JavaSparkContext(conf);
        SparkSession spark = SparkSession.builder().appName("NaiveBayes").master("local")
                  .config("spark.driver.memory", "1073741824")
                  .config("spark.testing.memory", "10073741824")
                  .getOrCreate();

        String trainingFile = "CSDMC2010_SPAM/TRAINING_SUB/";
        String labelsFile = "CSDMC2010_SPAM/SPAMTrain.label";
        String stopWordsFile = "stop_words.txt";
        ArrayList<String> stopWords = new ArrayList<>();
        Scanner in = new Scanner(new File(stopWordsFile));
        while (in.hasNext()) {
            String next = in.nextLine();
            stopWords.add(next);
        }


        get("/hello", (request, response) -> "Hello hamzaaaa!");
        get("/readinput", (request, response) -> {
            HashMap model = new HashMap();
            EmailNormalizer.readAndNormalizeInput(jsc, trainingFile, labelsFile, stopWordsFile);
            model.put("template", "templates/readinput.vtl");
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        get("/makedictionary", (request, response) -> {
          long startTime = System.currentTimeMillis();
            HashMap model = new HashMap();
            // Using my implementation the NaiveBayesModel
            Classifier.computeProbability(jsc);
            Classifier.makeDictionary(jsc);
            long endTime   = System.currentTimeMillis();
            totalTime1 = endTime - startTime;
            // Using NaiveBayesModel MLlib from Spark
            startTime = System.currentTimeMillis();
            SparkMLlibClassifier.train(jsc);
            endTime   = System.currentTimeMillis();
            totalTime2 = endTime - startTime;

            model.put("totalTime1", totalTime1);
            model.put("totalTime2", totalTime2);
            model.put("template", "templates/makedictionary.vtl");
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        get("/testclassification", (request, response) -> {
          //load tf file
          NaiveBayesTest.hashingTF = HashingTF.load("data_class/tfPath");
          //load idf file
          NaiveBayesTest.idfModel = IDFModel.load("data_class/idfPath");
          //load model
          NaiveBayesTest.model = NaiveBayesModel.load(spark.sparkContext(), "data_class/modelPath");
            HashMap model = new HashMap();
            //
            long startTime = System.currentTimeMillis();
            Classifier.computeProbability(jsc);
            Classifier.testClassification(jsc);
            long endTime   = System.currentTimeMillis();
            totalTime1 = endTime - startTime;

            startTime = System.currentTimeMillis();
            NaiveBayesTest.batchTestModel(spark, "data_class/TestingData.json");
            endTime   = System.currentTimeMillis();
            totalTime2 = endTime - startTime;

            model.put("truePos", Classifier.truePos);
            model.put("falsePos", Classifier.falsePos);
            model.put("falseNeg", Classifier.falseNeg);
            model.put("trueNeg", Classifier.trueNeg);
            model.put("accuracy", Classifier.accuracy);
            model.put("precision", Classifier.precision);
            model.put("recall", Classifier.recall);
            model.put("totalTime1", totalTime1);

            model.put("accuracy2", NaiveBayesTest.accuracy);
            model.put("totalTime2", totalTime2);

            model.put("template", "templates/testclassification.vtl");
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        get("/testsingle", (request, response) -> {
            HashMap model = new HashMap();
            model.put("template", "templates/testsingle.vtl");
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        get("/resulttestsingle", (request, response) -> {
            HashMap model = new HashMap();
            String subjectres;
            String subjectres2;
            String subject = request.queryParams("subject");
            String normSubject = EmailNormalizer.normalize(subject, stopWords);
            if (Classifier.isSpam(normSubject)) {
                subjectres = "SPAM";
            } else {
                subjectres = "HAM";
            }
            if (NaiveBayesTest.testModel(spark,normSubject) == 0.0) {
                subjectres2 = "SPAM";
            } else {
                subjectres2 = "HAM";
            }

            model.put("subject", subject);
            model.put("subjectres", subjectres);
            model.put("subjectres2", subjectres2);
            model.put("template", "templates/resulttestsingle.vtl");
            return new ModelAndView(model, "templates/layout.vtl");
        }, new VelocityTemplateEngine());
    }
}
