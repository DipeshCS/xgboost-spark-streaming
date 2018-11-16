package prediction

import org.apache.spark.ml.tuning.TrainValidationSplitModel
import org.apache.spark.sql.DataFrame

/**
  * Created by dipesh.patel on 01/11/18.
  */
case class XGBoostModel(path: String) {

  private val model = TrainValidationSplitModel.load(path+"/xgboost-model")

  def transform(df: DataFrame) = {

    // run the model on new data
    val result = model.transform(df)
    //print and verify the results.
    result.drop("features","rawPrediction","probability").show(10,false)

    // write the results
    result.drop("features","rawPrediction","probability").write.csv(path+"/xgboost-output")
  }

}
