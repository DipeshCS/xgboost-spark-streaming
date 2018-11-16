# xgboost-spark-streaming

This repository contains codebase for ML pipeline based on XGBoost, Spark Streaming and Kafka. Please find below instructions to run.

```shell
cd xgboost-on-spark
mvn clean install
```

Once you are able to compile and have JAR file ready, You can follow instruction below to run Training Pipeline and Prediction Pipeline. Note that, Same JAR contains code for training as well as prediction pipeline in different packages.

---
### Training Pipeline
```shell
spark-submit --master local[*] --deploy-mode client --class training.Driver {JAR location} {Path to Training Data} {Path to store model}
```
For ex.:
```shell
nohup spark-submit --master local[*] --deploy-mode client --class training.Driver xgboost-on-spark-1.0-SNAPSHOT-jar-with-dependencies.jar file:///mnt/xgboost/train.csv file:///mnt/dipesh/xgboost-model > xgboost_train.log &
```

### Prediction Pipeline
```shell
spark-submit --master local[*] --deploy-mode client --class prediction.Driver {JAR location} {Base Path to Test Data} {Base Path for Model}
```
For ex.:
```shell
nohup spark-submit --master local[*] --deploy-mode client --class prediction.Driver xgboost-on-spark-1.0-SNAPSHOT-jar-with-dependencies.jar file:///mnt/xgboost/test_data file:///mnt/xgboost/ > xgboost_test.log &
```
For prediction pipeline, Path to test data should be directory, since it is used as streaming source. Base path for training model will be used to store output and checkpoint information in respective directories.


---
1) Briefly describe the conceptual approach you chose! What are the trade-offs?

Training pipeline utilizes XGboost on spark to perform multiclass classification on training data. Accuracy is used as performance measure. 
Prediction pipeline is based on spark streaming. It reads test data from directory as streaming source. Kafka as streaming source is implemented but not tested due to difficulties in kakfa cluster setup. Prediction pipeline reads data streaming source, performs classification on pre trained XGBoost model and writes results to directory as streaming sink.

2) What's the model performance?

Accuracy: 0.1445

3) What's the runtime performance? 

Streaming model is able to process 1 record every 10 seconds.

4) If you had more time, what improvements would you make, and in what order of priority?

 - Perform hyper-parameter optimization on model and improve model's accuracy.
 - Integrate Kafka as streaming source for spark.
 - Perform Unit and Integration Testing on codebase.
