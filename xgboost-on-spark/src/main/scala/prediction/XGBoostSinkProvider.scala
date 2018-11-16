package prediction

import org.apache.spark.sql.execution.streaming.Sink
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.sql.sources.StreamSinkProvider
import org.apache.spark.sql.streaming.OutputMode

/**
  * Created by dipesh.patel on 01/11/18.
  */
abstract class MLSinkProvider extends StreamSinkProvider {
  def process(df: DataFrame): Unit
  var basePath : String = null
  def createSink(
                  sqlContext: SQLContext,
                  parameters: Map[String, String],
                  partitionColumns: Seq[String],
                  outputMode: OutputMode): MLSink = {
    basePath = parameters("basePath")
    new MLSink(process)
  }
}

case class MLSink(process: (DataFrame) => Unit) extends Sink {
  override def addBatch(batchId: Long, data: DataFrame): Unit = {
    process(data)
  }
}
class XGBoostMLSinkProvider extends MLSinkProvider {
  override def process(df: DataFrame) {
    XGBoostModel(basePath).transform(df)
  }
}
