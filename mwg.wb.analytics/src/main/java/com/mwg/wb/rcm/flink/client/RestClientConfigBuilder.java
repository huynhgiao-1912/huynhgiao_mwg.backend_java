package com.mwg.wb.rcm.flink.client;

import java.util.List;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch7.ElasticsearchSink;
import org.apache.flink.streaming.connectors.elasticsearch7.RestClientFactory;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback;





public class RestClientConfigBuilder {
	private static RestClientFactory factory;
	public static RestClientFactory getRestClientFactory() {
		if(factory == null) {
			factory = new RestClientFactory() {
				@Override
				public void configureRestClientBuilder(RestClientBuilder restClientBuilder) {
					restClientBuilder.setDefaultHeaders(new Header[] {
							new BasicHeader("accept","application/json"),
							new BasicHeader("content-type","application/json")
					});
					restClientBuilder.setRequestConfigCallback(new RequestConfigCallback() {
						@Override
						public Builder customizeRequestConfig(Builder requestConfigBuilder) {
							return requestConfigBuilder.setConnectTimeout(5000)
									.setSocketTimeout(5000);
						}
					});
					
				}
			};
		}
		return factory;
	}
	public static ElasticsearchSink.Builder<Tuple2<String, List<Integer>>> getElasticSinkBuilder(){
//		List<HttpHost> hosts = List.of(new HttpHost("10.1.6.151", 
//						9200, "http"));
//		ElasticsearchSink.Builder<Tuple2<String, List<Integer>>> esSinkBuilder = 
//				new ElasticsearchSink.Builder<Tuple2<String, List<Integer>>>(hosts,new ClickViewProductSink());
//		esSinkBuilder.setBulkFlushMaxActions(1);
//		esSinkBuilder.setRestClientFactory(restClientBuilder -> {
//			restClientBuilder.setDefaultHeaders(new Header[] {
//							new BasicHeader("accept","application/json"),
//							new BasicHeader("content-type","application/json")
//					});
//			restClientBuilder.setRequestConfigCallback(new RequestConfigCallback() {
//				
//				@Override
//				public Builder customizeRequestConfig(Builder requestConfigBuilder) {
//					return requestConfigBuilder.setConnectTimeout(5000)
//							.setSocketTimeout(5000);
//				}
//			});
//		});
//		return esSinkBuilder;
		return null;
	}
	
	
}
