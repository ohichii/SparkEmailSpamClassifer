# SparkEmailClassifier

This project present the implementation of email spam classification using Spark and Java,
the classification is based on Naive Bayes’ approach, so in a first time I wrote the implementation from scratch
using only simple methods from spark, then I used "NaiveBayesModel" from MLlib.

For this classification I used the the emails subject, because when using the whole body of the email the accuracy get too low.

## Structure

Under directory "SparkEmailSpamClassifer/src/main/java/"

```
.
├── azmah
│   ├── App.java
│   ├── Classifier.java
│   ├── EmailNormalizer.java
│   └── WordDetails.java
└── maweiming
    ├── DataFactory.java
    ├── NaiveBayesTest.java
    └── SparkMLlibClassifier.java
 ```

The main source code is written under these two directories, "azmah/" (which contains the implementation of the Naive Bayes Model from scratch) and "maweiming/" (which contains the NaiveBayesModel classifier from MLlib)

### azmah/

Under this package we find four principal classes :

* EmailNormalizer: This class takes care of one of the most important tasks before starting the classification, which is normalization of data so that it will be easy to manage, this class contains different methods to read the dataset, normalize it (removing HTML tags, lowercasing, unify some text like "URLs, emails..." using regular expresions...), change it to an appropriate format (csv or json in my case) and saving everything.
* WordDetails: This class present some statistics about each word from the training data, and for each word it provide the following information.

```
// The word
private String word;
// Number of times the word appears in Spam messages
private int cntSpam;
// Number of times the word appears in Ham messages
private int cntHam;
// Probability that the word is Spam
// (1 + cntSpam) / [cntTotalSpam + (cntTotalSpam + cntTotalHam)]
private float pSpam;
// Probability that the word is Ham
// (1 + cntHam) / [cntTotalHam + (cntTotalSpam + cntTotalHam)]
private float pHam;
 ```
* Classifier: the class that represent the classification, it contains three main methods:
* makeDictionary() this method construct a dictionary or a model which is a map that contains all the words from the dataset linked to their WordDetails object.
* testClassification() test the classifier on the whole testing dataset and it compute the accuracy of the results.
* isSpam() checks whether an email is a Spam or not, using the model constructed by makeDictionary(), it's the same method used in testClassification().
* App : it is the point entry to the classifier and to the whole application, from this class we call and execute all different methods and the other classes.


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

This project requires Gradle and a Java JDK or JRE to be installed, version 7 or higher (to check, use java -version). Gradle ships with its own Groovy library, therefore Groovy does not need to be installed. Any existing Groovy installation is ignored by Gradle.

Gradle uses whatever JDK it finds in your path. Alternatively, you can set the JAVA_HOME environment variable to point to the installation directory of the desired JDK.

## Running the tests

Just put the project in your machine and rub the following command :

```
gradle run
```
* And then open your browser and go to http://localhost:4567/readinput, here program starts reading the data and saving it after it's normalized by calling the methods from EmailNormalizer class.
* then click on the button "Go to make dictionary",this will lead you to the second link http://localhost:4567/makedictionary. Here we start the training process which makes the two models that our classifiers will be based on, here we call the method makeDictionary() from Classifier class and train() from SparkMLlibClassifier.
* then click on the button "Test Classification",this will lead you to the third link http://localhost:4567/testclassification. here we test both classifiers on the same dataset and we can see the difference between their results.
* then click on the button "test single",this will lead you to this link http://localhost:4567/testsingle. and here you can test the Classifiers on a single email by entring its subject.

**PS. the order is very important, and the first two links are supposed to called only once, because they create some files, so may get some errors if you try enter to one of them a second time, after the first execution you can go directly to http://localhost:4567/testclassification to make tests.**

## Deployment

Add additional notes about how to deploy this on a live system

## Built With

* [Java](https://www.java.com) - The Programming language used
* [Spark](https://spark.apache.org/) - Used to handle the big quantity of data.
* [Gradle](http://gradle.org/) -  build automation system
* [Maven](https://maven.apache.org/) - Dependency Management


## Authors

* **Hamza Ouhaichi** - *Initial work* - [GitHub](https://github.com/ohichii)
* **Prof. Diego Reforgiato Recupero** [Supervisor](http://people.unica.it/diegoreforgiato/en/).


## References

* [Spark Programming Guide](https://spark.apache.org/docs/latest/rdd-programming-guide.html)
* [How To Build a Simple Spam-Detecting Machine Learning Classifier](https://hackernoon.com/how-to-build-a-simple-spam-detecting-machine-learning-classifier-4471fe6b816e)
* [Spam classification using Spark’s DataFrames, ML and Zeppelin](https://blog.codecentric.de/en/2016/06/spam-classification-using-sparks-dataframes-ml-zeppelin-part-1/)
* [Spark Video Tutorials](http://sparkjava.com/tutorials/video-tutorials)
* [robinsonraju/cs286-spark-nb-email-classification](https://github.com/robinsonraju/cs286-spark-nb-email-classification)
* [Maweiming/SparkTextClassifier](https://github.com/Maweiming/SparkTextClassifier)
