package mwg.wb.analytics.helper;

public class AppConfig {
	public static   String KAFKA_ZOOKEEPER = "10.1.6.70:2181";
	public static   String KAFKA_BOOTSTRAP = "10.1.6.70:9092";
	//public static   String KAFKA_ZOOKEEPER = "localhost:2181";
	//public static   String KAFKA_BOOTSTRAP = "localhost:9092";
	public static   String KAFKA_TOPIC_RCM_TRACKING = "rcm_tracking";
	//public static   String KAFKA_TOPIC_RCM_TRACKING_ADDTOCART = "rcm_tracking_addtocart";
	
	//public static   String KAFKA_TOPIC_RCM_AGGREGATED = "rcm_aggr";
	public static   String KAFKA_GROUP = "flink-streams-consumer";

}
