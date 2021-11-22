package mwg.wb.business.rcm.helper;


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
 
public class ElasticService {
	
	private ObjectMapper mapper;
	private RestHighLevelClient restClient;	
	private static ElasticService elasticService;
	private static final ReentrantLock reLock = new ReentrantLock(true);
	public ElasticService(String host) {		 
		mapper = DidxHelper.generateJsonMapper("yyyy-MM-dd'T'HH:mm:ss");
		RestClientBuilder builder = RestClient.builder(new HttpHost(host, 9200))
				.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
					@Override
					public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
						return requestConfigBuilder.setConnectTimeout(5000).setSocketTimeout(60000);
					}
//					public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
//						return requestConfigBuilder.setConnectTimeout(200)
//								.setSocketTimeout(200);
//					}
				});
		// client = new RestHighLevelClient(RestClient.builder(new HttpHost(ElasticHost,
		// 9200, "http")));
		restClient = new RestHighLevelClient(builder);
		// RestClient.setMaxRetryTimeoutMillis(200000)
	}
//	private ElasticService(RestHighLevelClient client) {
//		this.restClient = client;
//		this.mapper = new ObjectMapper();
//		//mapper.registerModules(new JavaTimeModule());
//		DateFormat df = new SimpleDateFormat(GConfig.DateFormatStringNews);
//		mapper.setDateFormat(df);
//		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
//		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//	
//		mapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
//	}
	
	public static ElasticService getInstance(String host) {
		if(elasticService == null) {
			reLock.lock();
			if(elasticService == null) {
				elasticService = new ElasticService(host);
			}
			reLock.unlock();
		}
		return elasticService;
	}
	
	public <T> boolean indexRequest(final String index, Object id, T t) {
	 	String _Id = String.valueOf(id);  
		String json;
		try {
			json = mapper.writeValueAsString(t);
			 
			IndexRequest indexRequest = new IndexRequest(index)
										.id(_Id)
										.source(json, XContentType.JSON);
			
			IndexResponse indexResponse = restClient.index(indexRequest, RequestOptions.DEFAULT);
			
			if(	indexResponse != null ) {
				if(indexResponse.status() == RestStatus.CREATED) {
					 
					return true;
				}
				else if(indexResponse.status() == RestStatus.OK ) {
					 
					return true;
				}
			}	
		} catch (IOException e) {
			e.printStackTrace();
		 
		}
		
		return false;
	}
	
	public <T> boolean updateDocPart(final String index, String id, String part, T t) throws IOException {
		String script  = String.format("ctx._source.%s.add(params.part)", part);
//		String script  = String.format("ctx._source.%s = []", fieldName); //Empty array object

		Script inline = new Script(ScriptType.INLINE, 
									"painless", 
									script, 
									Map.of("part", mapper.convertValue(t, Map.class)));
		
		UpdateRequest updateRequest = new UpdateRequest(index, id)
										.script(inline);
//										.scriptedUpsert(true);
		UpdateResponse updateResponse =  restClient.update(updateRequest, RequestOptions.DEFAULT);
		
		if(updateResponse.getResult() == Result.UPDATED)
			return true;
		return false;
	}

	
	public <T> boolean updateRequest(final String index, String id, T t) throws IOException {
		String json = mapper.writeValueAsString(t);
		UpdateRequest updateRequest = new UpdateRequest(index, id)
				.doc(json, XContentType.JSON);
//				.docAsUpsert(true); 
		
		UpdateResponse updateResponse = restClient.update(updateRequest, RequestOptions.DEFAULT);
		if(updateResponse != null && updateResponse.status() == RestStatus.OK) {
			return true;
		}
		return false;
	}
	
	public <T> UpdateResponse upsertRequest(final String index, String id, T t) throws IOException {
	 	String json = mapper.writeValueAsString(t);
		UpdateRequest updateRequest = new UpdateRequest(index, id)
				.doc(json, XContentType.JSON)
				.docAsUpsert(true);
		UpdateResponse uResponse =null;
		for (int i = 0; i < 100; i++) {
			try {
				 uResponse = restClient.update(updateRequest, RequestOptions.DEFAULT);
				 break;
			} catch (Exception e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			 
			
		}
		return uResponse;
	}
	
	public  SearchResponse searchRequest(SearchSourceBuilder builder, final String... indices) throws IOException {
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(indices);
		if(builder != null )
			searchRequest.source(builder);

		SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
		return searchResponse;
	}
	
	public  SearchResponse scrollRequest(SearchSourceBuilder builder, final String... indices) throws IOException {
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(indices);
		searchRequest.scroll("10s");
		if(builder != null )
			searchRequest.source(builder);

		SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
		return searchResponse;
	}
	
	public SearchResponse scrollDoc(String scrollId) throws IOException {
		SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId) ;
		scrollRequest.scroll("10s");
		SearchResponse scrResponse = restClient.scroll(scrollRequest, RequestOptions.DEFAULT);
//		String _ScrollId = sResponse.getScrollId();
		return scrResponse;
	}
	
	public boolean clearScrollReq(String scrollId) throws IOException {
		ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
		clearScrollRequest.addScrollId(scrollId);
		ClearScrollResponse clearScrollResponse = restClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
		return clearScrollResponse.isSucceeded();
	}
	
	public <T> List<T> getSource(SearchResponse searchResponse, Class<T> c){
		if(searchResponse.getHits() == null 
				|| searchResponse.getHits().getHits() == null
				|| searchResponse.getHits().getHits().length == 0) return List.of();
		SearchHit[] hits = searchResponse.getHits().getHits();
		List<T> results = (List<T>) Arrays.stream(hits)
				.map( hit ->{ 
					try {
						return mapper.readValue(hit.getSourceAsString().getBytes("utf-8"), c);
					}catch(Exception ex) {
						 
						throw new RuntimeException(ex);
					}
				})
				.collect(Collectors.toList());
		return results;		
	}
	
	
	public boolean isExistsRequest(String id, String index) throws IOException{
		GetRequest req = new GetRequest(index, id);
		req.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);
		req.storedFields("_none_");
		boolean isExist = restClient.exists(req, RequestOptions.DEFAULT);;
		return isExist;
	}
	
	public <T> T getSourceById(String id, String index, Class<T> valueType) throws IOException {
	
		GetRequest getRequest = new GetRequest(index, id);
		GetResponse getResponse;
		try {
			getResponse = restClient.get(getRequest, RequestOptions.DEFAULT);
			if(getResponse.isExists()) {
				String data = getResponse.getSourceAsString();
				return  mapper.readValue(data, valueType);
			}
		}catch(ElasticsearchException ex) {
			if(ex.status() == RestStatus.NOT_FOUND)
				return null;
			 
			ex.printStackTrace();		
		}
		return null;
	}
	
	public boolean deleteRequest(String id, String index) throws IOException {
		DeleteRequest deleteRequest = new DeleteRequest(index, id);
		DeleteResponse deleteResponse = restClient.delete(deleteRequest, RequestOptions.DEFAULT);
		if(deleteResponse != null && deleteResponse.status() == RestStatus.OK)
			return true;
		return false;
	}
	
}