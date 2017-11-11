package azmah;

import java.util.HashMap;
import java.util.Map;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public class App {

    public static void main(String[] args) throws Exception {

        staticFileLocation("/public");
        String layout = "templates/layout.vtl";

        SparkConf conf = new SparkConf().setAppName("EmailClassifier").setMaster("local");
        JavaSparkContext jsc = new JavaSparkContext(conf);

        String trainingFile = "CSDMC2010_SPAM/TRAINING_SUB/";
        String labelsFile = "CSDMC2010_SPAM/SPAMTrain.label";
        String stopWordsFile = "stop_words.txt";
        get("/hello", (request, response) -> "Hello hamzaaaa!");
        get("/readinput", (request, response) -> {
            HashMap model = new HashMap();
            EmailNormalizer.readAndNormalizeInput(jsc, trainingFile, labelsFile, stopWordsFile);
            model.put("template", "templates/readinput.vtl");
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        get("/makedictionary", (request, response) -> {
            HashMap model = new HashMap();
            Classifier.computeProbability(jsc);
            Classifier.makeDictionary(jsc);
            model.put("template", "templates/makedictionary.vtl");
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        get("/testclassification", (request, response) -> {
            HashMap model = new HashMap();
            Classifier.computeProbability(jsc);
            Classifier.testClassification(jsc);

            model.put("truePos", Classifier.truePos);
            model.put("falsePos", Classifier.falsePos);
            model.put("falseNeg", Classifier.falseNeg);
            model.put("trueNeg", Classifier.trueNeg);
            model.put("accuracy", Classifier.accuracy);
            model.put("precision", Classifier.precision);
            model.put("recall", Classifier.recall);

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
            String subject = request.queryParams("subject");
            if (Classifier.isSpam(subject)) {
                subjectres = "SPAM";
            } else {
                subjectres = "HAM";
            }
            model.put("subject", subject);
            model.put("subjectres", subjectres);
            model.put("template", "templates/resulttestsingle.vtl");
            return new ModelAndView(model, "templates/layout.vtl");
        }, new VelocityTemplateEngine());
    }
}
