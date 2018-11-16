package prediction

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{StructField, _}
import training.Constants._

/**
  * Created by dipesh.patel on 31/10/18.
  */
object Driver {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder
      .appName("XGBoost-Streaming-Prediction")
      .getOrCreate()

    val schema = StructType(Array(
      StructField(TimeStamp, StringType),
      StructField(FeatureA, DoubleType),
      StructField(FeatureB, DoubleType),
      StructField(FeatureC, DoubleType),
      StructField(FeatureD, DoubleType),
      StructField(FeatureE, DoubleType)
    ))

    val streamingTestDF = spark.readStream.schema(schema).option("header","true").option("inferSchema","true").csv(args(0))

    import spark.implicits._
    val dfWithTimestamp = streamingTestDF.withColumn("unixTS", unix_timestamp($"Timestamp", TSFormat)).drop(TimeStamp)


    //TODO Kafka integration is disabled due to issues which kafka setup. Complete It.
//    dfWithTimestamp.selectExpr("to_json(struct(*)) AS value").
//      writeStream
//      .format("kafka")
//      .option("topic", "test_stream")
//      .option("kafka.bootstrap.servers", "localhost:9092")
//      .option("checkpointLocation", args(2)+"/kafka_checkpoint/")
//      .start()

//    import spark.implicits._
//    val df1 = dfWithTimestamp.selectExpr("CAST(value AS STRING)").as[String]
//      .select(from_json($"value", schema).as("data"))
//      .select("data.*")


    dfWithTimestamp.writeStream
      .format("prediction.XGBoostMLSinkProvider")
      .queryName("XGBoostQuery")
        .option("basePath",args(1))
      .option("checkpointLocation", args(1)+"/output_checkpoint/")
      .start()

    spark.streams.awaitAnyTermination()
  }
}


