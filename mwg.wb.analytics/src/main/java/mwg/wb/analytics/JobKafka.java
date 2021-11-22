package mwg.wb.analytics;

 

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.analytics.helper.AppConfig;
import mwg.wb.analytics.helper.Json;
 
 

public class JobKafka {
	private final ObjectMapper objectMapper = new ObjectMapper();

	public static void main1(String[] args) throws Exception {

		// Properties props = new Properties();
		// // props.put("bootstrap.servers", "localhost:9092");
		// props.setProperty("group.id", "flink-streams-consumer");
		// props.setProperty("zookeeper.connect", "10.1.6.70:2181"); // Zookeeper
		// default host:port
		// props.setProperty("bootstrap.servers", "10.1.6.70:9092"); // Broker default
		// host:port
		// props.setProperty("group.id", "flink-streams-consumer");

		Properties props = new Properties();
		props.setProperty("group.id", "flink-streams-consumer");
		props.setProperty("zookeeper.connect", "10.1.6.70:2181"); // Zookeeper default host:port
		props.setProperty("bootstrap.servers", "10.1.6.70:9092"); // Broker default host:port

		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.1.6.70:9092");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

		// KafkaProducer<String, Click> producer = new KafkaProducer<String,
		// Click>(props);
		// com.mwg.wb.rcm.flink.module.viewproduct.JsonSerializer jsonSerializer = new
		// com.mwg.wb.rcm.flink.module.viewproduct.JsonSerializer ();
		// Click fromValue1=new Click();
		//
		// fromValue1.uid= UUID.randomUUID();
		// fromValue1.city="hcm";
		// fromValue1.country="vn";
		// fromValue1.ip="127.0.0.1";
		// fromValue1.campaignId=1;
		// fromValue1.pubId=1;
		// fromValue1.timestamp= LocalDateTime.now();
		// producer.send(new ProducerRecord<String, Click>(KafkaTopics.CLICKS,
		// fromValue1));
		// System.out.println("Message " + fromValue1.toString() + " sent !!");

		// KafkaProducer<byte[], byte[]> producer = new KafkaProducer<>(props);
		//
		// Click clickIterator = new Click ();
		//
		// while (true) {
		// Click fromValue1=new Click();
		// fromValue1.uid= UUID.randomUUID();
		// fromValue1.city="hcm";
		// fromValue1.country="vn";
		// fromValue1.ip="127.0.0.1";
		// fromValue1.campaignId=1;
		// fromValue1.pubId=1;
		// fromValue1.timestamp= LocalDateTime.now();
		// ProducerRecord<byte[], byte[]> record = new ProducerRecord<>("clicks",
		// jsonSerializer.serialize("clicks",fromValue1));
		//
		// System.out.println("Message " + fromValue1.timestamp+ " sent !!");
		// producer.send(record);
		//
		// Thread.sleep(1000);
		// }

		// a.setIp("dasdasd");
		// String astr=jsonSerializer.serialize(a);
		// for (int i = 0; i < 100; i++)
		// producer.send(new ProducerRecord<String,
		// String>(KafkaTopics.AGGREGATED_CLICKS, Integer.toString(i),
		// Integer.toString(i)));

		// producer.close();

		// Serialize the result AggregatedClicksByMinute POJO to JSON
		// val jsonSerializer = new JsonSerializer();
		// val kafkaProducer = new
		// FlinkKafkaProducer08<AggregatedClicksByMinute>(KafkaTopics.AGGREGATED_CLICKS,
		// jsonSerializer, kafkaProperties());
		// kafkaProducer.
		// //Sink
		// aggregatedClicksByMinuteStream.addSink(kafkaProducer);
		//
		// //execute program
		// flinkEnv.execute("Counting clicks in a click stream over a time window");

		// val aggregatedClicksByMinuteStream = clicksWindowedStream
		// .apply(new ClickWindowCountFunction())
		// .name("Count clicks in a Windowed stream");
		//
		// //Serialize the result AggregatedClicksByMinute POJO to JSON
		//
		// val kafkaProducer = new
		// FlinkKafkaProducer08<AggregatedClicksByMinute>(KafkaTopics.AGGREGATED_CLICKS,
		// jsonSerializer, kafkaProperties());
		//
		// //Sink
		// aggregatedClicksByMinuteStream.addSink(kafkaProducer);
		//
		// //execute program
		// flinkEnv.execute("Counting clicks in a click stream over a time window");
	}

	private static Properties kafkaProperties() {
		Properties properties = new Properties();
		// Each key in Kafka is String
		// properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
		// StringSerializer.class );
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		// Each value is a byte[] (Each value is a JSON string encoded as bytes)
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

		properties.setProperty("zookeeper.connect", "10.1.6.70:2181"); // Zookeeper default host:port
		properties.setProperty("bootstrap.servers", "10.1.6.70:9092"); // Broker default host:port
		properties.setProperty("group.id", "flink-streams-consumer");
		return properties;
	}

	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		// env.getConfig().disableSysoutLogging();
		env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000));
		// very simple data generator
		DataStream< DataTrackingBO> messageStream = env
				.addSource(new SourceFunction< DataTrackingBO>() {

					private static final long serialVersionUID = 6369260445318862378L;

					public boolean running = true;

					@Override
					public void run(SourceContext< DataTrackingBO> ctx)
							throws Exception {
						int i = 0;
						while (this.running) {
							i++;
							// ctx..collect("Element - " + i++);
//							 DataTrackingBO fromValue1 = new  DataTrackingBO();
//							fromValue1.username = "user1";
//							 
//							fromValue1.cmd = "VIEWED_PRODUCT";
//							 ViewProductBO bo = new  ViewProductBO( );
//							 
//									 
//							bo.productid=i;
//							bo.timeview=i;
//							fromValue1.data = Json.toJson(bo);
//							fromValue1.timestamp = LocalDateTime.now();
//							//bo.timestamp=LocalDateTime.now();
//							ctx.collect(fromValue1);
//							System.out.println("View " + fromValue1.timestamp + " sent  " +bo.productid +" time "+ bo.timeview);
//							
//							//buy
//							
//							
//							 DataTrackingBO fromValue2 = new  DataTrackingBO();
//							fromValue2.username = "user1";
//							 
//							fromValue2.cmd = "ORDER_PRODUCT";
//							 OrderBO bo2 = new  OrderBO();
//							bo2.orderid=String.valueOf(i);
//							bo2.timestamp=LocalDateTime.now();
//							bo2.productlist=new ArrayList<OrderItemBO>()  ;
//							
//							//bo2.productlist.add(new OrderItemBO(i,i+2,44));
//							//bo2.productlist.add(new OrderItemBO(i+1,2,42));
//							//fromValue2.data = Json.toJson(bo2);
//							fromValue2.timestamp = LocalDateTime.now();
//						
//							ctx.collect(fromValue2);
//							System.out.println("Buy1 " + bo2.timestamp + " sent  " +bo2.orderid );
//							
//							Thread.sleep(500);
//							if (i > 10)
//								i = 0;
						}
					}

					@Override
					public void cancel() {
						running = false;
					}
				});
		AppConfig.KAFKA_ZOOKEEPER = "10.1.6.70:2181";
		AppConfig.KAFKA_BOOTSTRAP = "10.1.6.70:9092";
		AppConfig.KAFKA_TOPIC_RCM_TRACKING = "rcm_tracking";
	 
		AppConfig.KAFKA_GROUP = "flink-streams-consumer";

		Properties props = new Properties();
		props.setProperty("group.id", AppConfig.KAFKA_GROUP);
		props.setProperty("zookeeper.connect", AppConfig.KAFKA_ZOOKEEPER);
		props.setProperty("bootstrap.servers", AppConfig.KAFKA_BOOTSTRAP);

		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

		//val jsonSerializerView = new com.mwg.wb.rcm.flink.module.persionalize.JsonSerializer();

		//val kafkaProducer = new FlinkKafkaProducer< DataTrackingBO>(
		//		AppConfig.KAFKA_TOPIC_RCM_TRACKING, jsonSerializerView, props);

		//messageStream.addSink(kafkaProducer);
		env.execute("Write into Kafka example");
	}
}
