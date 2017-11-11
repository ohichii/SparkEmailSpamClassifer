/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maweiming;

/**
 *
 * @edited by azmah
 */

import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDFModel;
import org.apache.spark.ml.linalg.SparseVector;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.*;

import java.text.DecimalFormat;
import java.util.*;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;

/**
 * 3、the third step
 * Created by Coder-Ma on 2017/6/26.
 */
public class NaiveBayesTest {

    private static HashingTF hashingTF;

    private static IDFModel idfModel;

    private static NaiveBayesModel model;

    private static Map<Integer,String> labels = new HashMap<>();

    public static void main(String[] args) {

      SparkSession spark = SparkSession.builder().appName("NaiveBayes").master("local")
                .config("spark.driver.memory", "1073741824")
                .config("spark.testing.memory", "10073741824")
                .getOrCreate();

        //load tf file
        hashingTF = HashingTF.load("data_class/tfPath");
        //load idf file
        idfModel = IDFModel.load("data_class/idfPath");
        //load model
        model = NaiveBayesModel.load(spark.sparkContext(), "data_class/modelPath");

        //batch test
        batchTestModel(spark, "data_class/TestingData.json");

        //test a single
        testModel(spark,"lance knobel davos newbies does quick review tony blairs case agai");

    }

    public static void batchTestModel(SparkSession sparkSession, String testPath) {

        Dataset<Row> test = sparkSession.read().json(testPath);
        //word frequency count
        Dataset<Row> featurizedData = hashingTF.transform(test);
        //count tf-idf
        Dataset<Row> rescaledData = idfModel.transform(featurizedData);

        List<Row> rowList = rescaledData.select("label", "features").javaRDD().collect();

        List<Result> dataResults = new ArrayList<>();
        for (Row row : rowList) {
            Long label = row.getAs("label");
            SparseVector sparseVector = row.getAs("features");
            Vector features = Vectors.dense(sparseVector.toArray());
            long predict = (new Double(model.predict(features))).longValue();
            dataResults.add(new Result(label, predict));
        }
        Integer successNum = 0;
        Integer errorNum = 0;

        for (Result result : dataResults) {
            if (result.isCorrect()) {
                successNum++;
            } else {
                errorNum++;
            }
        }

        JavaRDD<LabeledPoint> testDataRdd = rescaledData.select("label", "features").javaRDD().map(v1 -> {
            Long label = v1.getAs("label");
            SparseVector features = v1.getAs("features");
            Vector featuresVector = Vectors.dense(features.toArray());
            return new LabeledPoint(Long.valueOf(label), featuresVector);
        });
        JavaPairRDD<Double, Double> predictionAndLabel
                = testDataRdd.mapToPair((LabeledPoint p) -> new Tuple2<>(model.predict(p.features()), p.label()));
        double accuracy = predictionAndLabel.filter((Tuple2<Double, Double> pl) -> pl._1().equals(pl._2())).count() / (double) test.count();

        DecimalFormat df = new DecimalFormat("######0.0000");
        Double result = (Double.valueOf(successNum) / Double.valueOf(dataResults.size())) * 100;

        System.out.println("batch test");
        System.out.println("=======================================================");
        System.out.println("Summary");
        System.out.println("-------------------------------------------------------");
        System.out.println("ACCURACY : " + accuracy);
        System.out.println(String.format("Correctly Classified Instances          :      %s\t   %s%%",successNum,df.format(result)));
        System.out.println(String.format("Incorrectly Classified Instances        :       %s\t    %s%%",errorNum,df.format(100-result)));
        System.out.println(String.format("Total Classified Instances              :      %s",dataResults.size()));
        System.out.println("===================================");

    }

    public static void testModel(SparkSession sparkSession, String content){
        List<Row> data = Arrays.asList(
                RowFactory.create(AnsjUtils.participle(content))
        );
        StructType schema = new StructType(new StructField[]{
                new StructField("subject", new ArrayType(DataTypes.StringType, false), false, Metadata.empty())
        });

        Dataset<Row> testData = sparkSession.createDataFrame(data, schema);
        //word frequency count
        Dataset<Row> transform = hashingTF.transform(testData);
        //count tf-idf
        Dataset<Row> rescaledData = idfModel.transform(transform);

        Row row =rescaledData.select("features").first();
        SparseVector sparseVector = row.getAs("features");
        Vector features = Vectors.dense(sparseVector.toArray());
        Double predict = model.predict(features);

        System.out.println("test a single");
        System.out.println("=======================================================");
        System.out.println("Result");
        System.out.println("-------------------------------------------------------");
        System.out.println(predict);
        System.out.println("===================================");
    }


}
