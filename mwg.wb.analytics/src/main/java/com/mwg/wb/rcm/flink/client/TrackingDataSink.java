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

import mwg.rcm.model.tracking.StateBO;

public class TrackingDataSink implements ElasticsearchSinkFunction<StateBO>{
	
	public IndexRequest createIndexRequest(StateBO element) {
		Map<String, Object> json = new HashMap<>();
        json.put("products", element.id);
        return null;//Requests.indexRequest()
        	//	.index("product")
              //  .id(element.key)
               // .source(json, XContentType.JSON);
    }
	
	@Override
	public void process(StateBO element, RuntimeContext ctx, RequestIndexer indexer) {
		indexer.add(this.createIndexRequest(element));	
	}
}
