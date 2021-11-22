package mwg.wb.analytics;


import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.StateBackendLoader;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.ProcessingTimeTrigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.streaming.connectors.elasticsearch7.ElasticsearchSink;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mwg.wb.rcm.flink.module.persionalize.TrackingProcessFunction;

import akka.remote.WireFormats.TimeUnit;
import mwg.wb.analytics.helper.AppConfig;
import mwg.wb.analytics.helper.Json;
import mwg.wb.analytics.serializer.EventDeserializer;
import mwg.wb.analytics.streaming.ClickViewProductStreaming;
import mwg.wb.analytics.streaming.PromotionStreaming;
 
 

public class StreamingJob {
	static ObjectMapper mapper;
	 
		 
	public static void main(String[] args) throws Exception {
		mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		 nqhtest(1);
		// TrackingViewProduct1(args);
	}
	
	 
	public static void nqhtest(int ver ) throws Exception {
	 
			 Properties kafkaLive=	kafkaPropertiesLive();
			 
			// AppConfig.KAFKA_ZOOKEEPER = "10.1.6.70:2181";
	 		//	AppConfig.KAFKA_BOOTSTRAP = "10.1.6.70:9092"; 
			// AppConfig.KAFKA_ZOOKEEPER = "10.1.6.70:2181";
	 
	 			
				AppConfig.KAFKA_TOPIC_RCM_TRACKING = "rcm_tracking"; 
				AppConfig.KAFKA_GROUP = "flink-streams-consumer";
				StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
			 
		//
				// start a checkpoint every 1000 ms
				env.enableCheckpointing(10*60*1000);//10 phut

				// advanced options:

				// set mode to exactly-once (this is the default)
				env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
				CheckpointConfig config = env.getCheckpointConfig();
				config.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
				
	 env.setRestartStrategy(RestartStrategies.fixedDelayRestart(1000, 5000));//ms
				
				
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime); 
		com.mwg.wb.rcm.flink.module.persionalize.JsonDeserializer jsonDeserializer =  new  com.mwg.wb.rcm.flink.module.persionalize.JsonDeserializer();  
		FlinkKafkaConsumer<DataTrackingBO> flinkKafkaConsumer = new FlinkKafkaConsumer<DataTrackingBO>(AppConfig.KAFKA_TOPIC_RCM_TRACKING, jsonDeserializer,kafkaLive );
		//flinkKafkaConsumer.setStartFromEarliest();
	 
		DataStreamSource<DataTrackingBO> kafkaStream = env.addSource(flinkKafkaConsumer);  
		DataStreamSource windowStream = kafkaStream
				.keyBy(DataTrackingBO::getUsername)
				.timeWindow(Time.seconds(5))
				.trigger(ProcessingTimeTrigger.create())
				.process(new   TrackingProcessFunction());
		windowStream.print();
		
		//http://172.16.3.39:9200/
 		//List<HttpHost> hosts = List.of(new HttpHost("10.1.6.151", 9200, "http"));	
 		List<HttpHost> hosts = new ArrayList<HttpHost>();	
 		hosts.add( new HttpHost("172.16.3.39", 9200, "http"));
	        final ElasticsearchSink.Builder<StateUpdateBO> esSinkBuilder = new ElasticsearchSink.Builder<StateUpdateBO>(
	        		hosts,
	                new ElasticsearchSinkFunction<StateUpdateBO>() {
	                    private static final long serialVersionUID = 6337634330353081773L;

	                    @Override
	                    public void process(StateUpdateBO element, RuntimeContext ctx, RequestIndexer indexer) {
	                      
	                         if(element!=null && element.statelist!=null && !element.statelist.isEmpty()) {
	                        	 
	                        String promoJson="[]";
							//try {
								try {
									promoJson = Json.toJson(element.statelist);
								} catch (Throwable e) {
								 	System.out.println("-------err 1 json statelist " +e.getMessage() ); 
								}
							//} catch (Throwable e) {
								// System.out.println("err json statelist " +e.getMessage()); 
								//e.printStackTrace();
							//}
								 
	        				String json = "{\"username\":\"" + element.username + "\",\""+element.propertyname+"\":" + promoJson + "}"; 
	        				UpdateRequest update = new UpdateRequest("rcm_customer", element.username ).doc(json, XContentType.JSON).docAsUpsert(true)
	        							.detectNoop(false);
	        				  indexer.add(update); 
	                       // System.out.println("sink ok 4 "+element.username + "-" + element.statelist.size()); 
	                         }else {
	                        	 
	                        	 System.out.println("se:err 3 statelist empty " ); 
	                         }
	                        
	                    }
	                });

	        esSinkBuilder.setBulkFlushInterval(300);
	      //  esSinkBuilder.setBulkFlushMaxActions(10); 
	        windowStream.addSink(esSinkBuilder.build());
	       
		env.execute("rcm streaming");
			
	        
 		 
	}
 

	private static Properties kafkaProperties(){
		Properties properties = new Properties(); 
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); 
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		
 
		properties.setProperty("group.id", AppConfig.KAFKA_GROUP);
		properties.setProperty("zookeeper.connect",  AppConfig.KAFKA_ZOOKEEPER);  
		properties.setProperty("bootstrap.servers", AppConfig.KAFKA_BOOTSTRAP);  
//		properties.setProperty("AUTO_OFFSET_RESET_CONFIG", "earliest");    
		  
		return properties;
	}
	private static Properties kafkaPropertiesLive(){
		Properties properties = new Properties(); 
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); 
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		
 
		properties.setProperty("group.id", AppConfig.KAFKA_GROUP);
		properties.setProperty("zookeeper.connect",  AppConfig.KAFKA_ZOOKEEPER);  
		properties.setProperty("bootstrap.servers", AppConfig.KAFKA_BOOTSTRAP);  
		
		properties.setProperty("zookeeper.connect", "172.16.3.46:2181"); // Zookeeper default host:port
		properties.setProperty("bootstrap.servers", "172.16.3.46:9092"); // Broker def
		
//		properties.setProperty("AUTO_OFFSET_RESET_CONFIG", "earliest");    
		  
		return properties;
	}

}

