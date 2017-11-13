/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maweiming;

import java.io.IOException;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDFModel;
import org.apache.spark.ml.linalg.SparseVector;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Row;

/**
 *
 * @author azmah
 */
public class SparkMLlibClassifier {

    public static void train(JavaSparkContext jsc) throws IOException {
      String path = "data_class/TrainingData.json";

      SparkSession spark = SparkSession.builder().appName("NaiveBayes").master("local")
              .config("spark.driver.memory", "1073741824")
              .config("spark.testing.memory", "10073741824")
              .getOrCreate();

      Dataset<Row> train = spark.read().json(path);

      //word frequency count
      HashingTF hashingTF = new HashingTF().setNumFeatures(500000).setInputCol("subject").setOutputCol("rawFeatures");
      Dataset<Row> featurizedData = hashingTF.transform(train);

      //count tf-idf
      IDF idf = new IDF().setInputCol("rawFeatures").setOutputCol("features");
      IDFModel idfModel = idf.fit(featurizedData);
      Dataset<Row> rescaledData = idfModel.transform(featurizedData);

      JavaRDD<LabeledPoint> trainDataRdd = rescaledData.select("label", "features").javaRDD().map(v1 -> {
          Long label = v1.getAs("label");
          SparseVector features = v1.getAs("features");
          Vector featuresVector = Vectors.dense(features.toArray());
          return new LabeledPoint(Long.valueOf(label), featuresVector);
      });

      System.out.println("Start training...");
      NaiveBayesModel model = NaiveBayes.train(trainDataRdd.rdd());
      model.save(spark.sparkContext(), "data_class/modelPath");//save model
      hashingTF.save("data_class/tfPath");//save tf
      idfModel.save("data_class/idfPath");//save idf

      System.out.println("train successfully !");
      System.out.println("=======================================================");
      System.out.println("modelPath:" + DataFactory.MODEL_PATH);
      System.out.println("tfPath:" + DataFactory.TF_PATH);
      System.out.println("idfPath:" + DataFactory.IDF_PATH);
      System.out.println("=======================================================");

    }

    public static void main(String[] args)  {


    }
}
