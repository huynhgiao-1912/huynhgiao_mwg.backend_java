package com.mwg.wb.rcm.flink.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentType;

public class ClickViewProductSink implements ElasticsearchSinkFunction<Tuple2<String, List<Integer>>>{
	
	public IndexRequest createIndexRequest(Tuple2<String, List<Integer>> element) {
		Map<String, Object> json = new HashMap<>();
        json.put("products", element.f1);
        return Requests.indexRequest()
        		.index("be_fresher_streaming_clickview")
                .id(element.f0)
                .source(json, XContentType.JSON);
    }
	
	@Override
	public void process(Tuple2<String, List<Integer>> element, RuntimeContext ctx, RequestIndexer indexer) {
		indexer.add(this.createIndexRequest(element));	
	}
}
