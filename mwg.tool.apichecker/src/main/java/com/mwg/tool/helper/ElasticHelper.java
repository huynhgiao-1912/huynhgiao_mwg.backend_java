package com.mwg.tool.helper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;


public class ElasticHelper {
	private RestHighLevelClient restClient;	
	private static ElasticHelper _helper;
	private static final ReentrantLock reLock = new ReentrantLock(true);
	
	public static ElasticHelper getInstance(String host) {
		if(_helper == null) {
			reLock.lock();
			if(_helper == null) {
				_helper = new ElasticHelper(host);
			}
			reLock.unlock();
		}
		return _helper;
	}
	
	private ElasticHelper(String host) {		 
		RestClientBuilder builder = RestClient.builder(new HttpHost(host, 9200))
				.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
					@Override
					public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
						return requestConfigBuilder.setConnectTimeout(5000).setSocketTimeout(10000);
					}

				});
		restClient = new RestHighLevelClient(builder);
	}
	
	
	
	public boolean indexRequest(final String index, String id, String json) {
		try {
			IndexRequest indexRequest = new IndexRequest(index)
										.id(id)
										.source(json, XContentType.JSON);
			
			IndexResponse indexResponse = restClient.index(indexRequest, RequestOptions.DEFAULT);
			
			if(	indexResponse != null ) { 
				if(indexResponse.status() == RestStatus.CREATED || indexResponse.status() == RestStatus.OK) {
					return true;
				}
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void indexRequestAsync(final String index, String id, String json, ActionListener<IndexResponse> listener) {
			IndexRequest indexRequest = new IndexRequest(index)
										.id(id)
										.source(json, XContentType.JSON);
			
			restClient.indexAsync(indexRequest, RequestOptions.DEFAULT, listener);
	}
	
	public void upsertRequestAsync(final String index, String id, String json, boolean isUpsert, ActionListener<UpdateResponse> listener){
		UpdateRequest updateRequest = new UpdateRequest(index, id)
				.doc(json, XContentType.JSON)
				.docAsUpsert(isUpsert);

		restClient.updateAsync(updateRequest, RequestOptions.DEFAULT, listener);

	}
	
	public BulkResponse bulkRequest(final String globalIndex, List<Object> requests) throws IOException {
		BulkRequest bulkRequest = new BulkRequest(globalIndex);
		for(Object request : requests) {
			if(request instanceof IndexRequest) {
				bulkRequest.add((IndexRequest)request);
			}else if(request instanceof UpdateRequest) {
				bulkRequest.add((UpdateRequest)request);
			}else if(request instanceof DeleteRequest) {
				bulkRequest.add((DeleteRequest)request);
			}
		}
		bulkRequest.timeout("10s");
		BulkResponse bulkResponse = restClient.bulk(bulkRequest, RequestOptions.DEFAULT);
		return bulkResponse;
			
	}
	
	public  SearchResponse searchRequest(SearchSourceBuilder sourceBuilder, final String... indices) throws IOException {
		if(sourceBuilder == null) throw new RuntimeException("[ElasticHelper:searchRequest] SearchSourceBuilder is null");
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(indices);
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
		
		return searchResponse;
	}
	
	public void searchRequestAsync(SearchSourceBuilder sourceBuilder, ActionListener<SearchResponse> listener, final String... indices) {
		if(sourceBuilder == null) throw new RuntimeException("[ElasticHelper:searchRequestAsync] SearchSourceBuilder is null");
		
		SearchRequest searchRequest = new SearchRequest(indices);
		searchRequest.source(sourceBuilder);
		
		restClient.searchAsync(searchRequest, RequestOptions.DEFAULT, listener);
		
	}
	
	public UpdateResponse upsertRequest(final String index, String id, String json, boolean isUpsert) throws IOException {
		UpdateRequest updateRequest = new UpdateRequest(index, id)
				.doc(json, XContentType.JSON)
				.docAsUpsert(isUpsert);
		UpdateResponse updateResponse =null;
		for (int i = 0; i < 5; i++) {
			try {
				updateResponse = restClient.update(updateRequest, RequestOptions.DEFAULT);
				 break;
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println(String.format("try %d times after 3s", i));
				try {
					Thread.sleep(3000);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
		return updateResponse;
	}

	public boolean isExistsRequest(String id, String index) throws IOException{
		GetRequest request = new GetRequest(index, id);
		request.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);
		request.storedFields("_none_");
		boolean isExist = restClient.exists(request, RequestOptions.DEFAULT);;
		return isExist;
	}
	
	public boolean deleteRequest(String id, String index) throws IOException {
		DeleteRequest deleteRequest = new DeleteRequest(index, id);
		DeleteResponse deleteResponse = restClient.delete(deleteRequest, RequestOptions.DEFAULT);
		if(deleteResponse != null && deleteResponse.status() == RestStatus.OK)
			return true;
		return false;
	}
	
	public CountResponse countRequest(QueryBuilder queryBuilder, final String... indices) throws IOException {
		CountRequest countRequest = new CountRequest(indices);
		countRequest.query(queryBuilder);
		CountResponse countResponse = restClient.count(countRequest, RequestOptions.DEFAULT);
		return countResponse;
	}
	
	/**
	 * 
	 * @param id
	 * @param index
	 * @return json
	 * @throws IOException
	 */
	public String getSourceById(final String index, String id) throws IOException {
		
		GetRequest getRequest = new GetRequest(index, id);
		GetResponse getResponse;
		try {
			getResponse = restClient.get(getRequest, RequestOptions.DEFAULT);
			if(getResponse.isExists()) {
				String data = getResponse.getSourceAsString();
				return  data;
			}
		}catch(ElasticsearchException ex) {
			ex.printStackTrace();		
		}
		return null;
	}
	
	public MultiGetResponse getMultiSourceById(final String index, FetchSourceContext sourceContext, String...ids) throws IOException {
		if(ids.length == 0) return null;
		MultiGetRequest multiRequest = new MultiGetRequest();
		for(String id : ids) {
			multiRequest.add(new MultiGetRequest.Item(index, id)
									.fetchSourceContext(sourceContext));
		}
		MultiGetResponse multiResponse = restClient.mget(multiRequest, RequestOptions.DEFAULT);
		return multiResponse;
	}
	
	public MultiSearchResponse multiSearchRequest(final String index, SearchSourceBuilder... sourceBuilders) throws IOException {
		if(sourceBuilders.length == 0 ) return null;
		MultiSearchRequest multiRequest = new MultiSearchRequest();
		for (SearchSourceBuilder searchSourceBuilder : sourceBuilders) {
			SearchRequest searchRequest = new SearchRequest(index);
			searchRequest.source(searchSourceBuilder);
			multiRequest.add(searchRequest);
		}
		MultiSearchResponse multiResponse = restClient.msearch(multiRequest, RequestOptions.DEFAULT);
		return multiResponse;
	}
	
	/**
	 * Pagination 
	 * 
	 * @param builder
	 * @param indices
	 * @return
	 * @throws IOException
	 */
	public  SearchResponse scrollRequest(SearchSourceBuilder builder, final String... indices) throws IOException {		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(indices);
		searchRequest.scroll("10s");
		if(builder != null )
			searchRequest.source(builder);

		SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
		return searchResponse;
	}
	
	public SearchResponse scrollDocument(String scrollId) throws IOException {
		SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId) ;
		scrollRequest.scroll("10s");
		SearchResponse scrResponse = restClient.scroll(scrollRequest, RequestOptions.DEFAULT);
//		String _ScrollId = sResponse.getScrollId();
		return scrResponse;
	}
	
	public boolean clearScrollRequest(String scrollId) throws IOException {
		ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
		clearScrollRequest.addScrollId(scrollId);
		ClearScrollResponse clearScrollResponse = restClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
		return clearScrollResponse.isSucceeded();
	}
	
	/**
	 * update a part of document
	 * 
	 * @param index
	 * @param id
	 * @param part
	 * @param t
	 * @return
	 * @throws IOException
	 */
	
	public boolean updateDocPart(final String index, String id, String fieldName, Map<String, Object> object) throws IOException {
		String script  = String.format("ctx._source.%s.add(params.part)", fieldName);
//		String script  = String.format("ctx._source.%s = []", fieldName); //Empty array object

		Script inline = new Script(ScriptType.INLINE, 
									"painless", 
									script, 
									Map.of("part", object));
		
		UpdateRequest updateRequest = new UpdateRequest(index, id)
										.script(inline);
//										.scriptedUpsert(true);
		UpdateResponse updateResponse =  restClient.update(updateRequest, RequestOptions.DEFAULT);
		
		if(updateResponse.getResult() == Result.UPDATED)
			return true;
		return false;
	}
	
	public void close() throws IOException {
		if(!Objects.isNull(restClient)) restClient.close();
	}
}
