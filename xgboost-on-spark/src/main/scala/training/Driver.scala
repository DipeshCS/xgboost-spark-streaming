package training

import ml.dmlc.xgboost4j.scala.spark.XGBoostClassifier
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.ml.feature.{Imputer, StandardScaler, VectorAssembler}
import org.apache.spark.ml.tuning.{ParamGridBuilder, TrainValidationSplit}
import Constants._
/**
  * Created by dipesh.patel on 30/10/18.
  */
object Driver {

  def main(args: Array[String]): Unit = {

    val spark  = SparkSession.builder()
      .appName("Spark XGBOOST Training")
      .getOrCreate()

    val df_raw = spark.read.option("header", "true").option("inferSchema","true").csv(args(0))

    import spark.implicits._
    val dfWithTimestamp = df_raw.withColumn("unixTS", unix_timestamp($"Timestamp", TSFormat))
      .drop(TimeStamp)
      .withColumnRenamed("Label","label")

    //All the null values are replaced with mean for that column. Since less knowledge of data.
      val imputer = new Imputer()
        .setInputCols(Array(FeatureA, FeatureB, FeatureC, FeatureD, FeatureE))
        .setOutputCols(Array(FeatureA, FeatureB, FeatureC, FeatureD, FeatureE))

    val vectorAssembler = new VectorAssembler()
      .setInputCols(Array("unixTS", FeatureA, FeatureB, FeatureC, FeatureD, FeatureE))
      .setOutputCol("features")

    val xgbParam = Map("eta" -> 0.01f,
      "max_depth" -> 12,
      "objective" -> "multi:softmax",
      "num_class" -> 5,
      "num_round" -> 50,
      "num_workers" -> 4)

    val xgbEstimator = new XGBoostClassifier(xgbParam)
      .setFeaturesCol("features")
      .setLabelCol("label")

    val pipeline = new Pipeline().setStages(Array(imputer, vectorAssembler, xgbEstimator))

    // Build parameter grid
    val paramGrid = new ParamGridBuilder()
      .addGrid(xgbEstimator.maxDepth, Array(10))
      .addGrid(xgbEstimator.eta, Array(0.1, 0.05, 0.01, 0.001))
      .addGrid(xgbEstimator.numRound, Array(100))
      .build()

    val evaluator = new MulticlassClassificationEvaluator()
      .setMetricName("accuracy")

    // Establish TrainValidationSplit()
    //TODO Do Split based on Timestamp instead of random split. Write Custom Splitter for that.
    val trainValidationSplit = new TrainValidationSplit()
      .setEstimator(pipeline)
      .setEvaluator(evaluator)
      .setEstimatorParamMaps(paramGrid)
      // 85% of the data will be used for training and the remaining 15% for validation.
      .setTrainRatio(0.85)

    // Run cross-validation, and choose the best set of parameters.
    val cvModel = trainValidationSplit.fit(dfWithTimestamp)
    cvModel.validationMetrics.foreach(println)
    cvModel.save(args(1))

  }

}
