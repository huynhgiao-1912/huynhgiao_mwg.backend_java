package mwg.wb.client.elasticsearch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
//import org.springframework.context.annotation.Configuration;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.SortOrder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.common.DidxHelper;
import mwg.wb.common.JsonConfig;
import mwg.wb.common.Logs;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.scs.HashtagBO;
import mwg.wb.model.scs.HashtagSO;

//@Configuration
//@RefreshScope
public class ElasticClient {

	private static ElasticClient instance;
	private RestHighLevelClient client = null;
	private final Lock queueLock = new ReentrantLock();
//	static Gson gson = new Gson();
	static ObjectMapper mapper = null;

	// @Value("${ELASTICSEARCH_CONNECTIONSTRING:172.16.3.151}")
	// private String ElasticHost = "172.16.3.60";
	// private String ElasticHost = "172.16.3.23"; //120,125,126
	// private static String ElasticHost = "172.16.3.120"; //120,125,126
	// private static String ElasticHostDev = "10.1.6.151";
	private static String SERVER_ELASTICSEARCH_READ_HOST = "";

	JsonConfig jsonConfig = null;

//http://172.16.3.60
//	@SuppressWarnings("resource")
	public ElasticClient(String host) {

		SERVER_ELASTICSEARCH_READ_HOST = host;

		mapper = DidxHelper.generateJsonMapper("yyyy-MM-dd'T'HH:mm:ss");
		CustomHttpClientConfigCallback configurationCallback = new CustomHttpClientConfigCallback();
		RestClientBuilder builder = RestClient.builder(new HttpHost(SERVER_ELASTICSEARCH_READ_HOST, 9200))
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
		builder.setHttpClientConfigCallback(configurationCallback);
		client = new RestHighLevelClient(builder);
		// RestClient.setMaxRetryTimeoutMillis(200000)
	}
	public void close( ) {
		 
			try {
				client.close();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	  finally {
				 
			}
	
	}
//	public ElasticClient(String host) {
//		mapper = new ObjectMapper();
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//		mapper.setDateFormat(df);
//
//		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
//		RestClientBuilder builder = RestClient.builder(
//			    new HttpHost(host, 9200))
//			    .setRequestConfigCallback(
//			        new RestClientBuilder.RequestConfigCallback() {
//			            @Override
//			            public RequestConfig.Builder customizeRequestConfig(
//			                    RequestConfig.Builder requestConfigBuilder) {
//			                return requestConfigBuilder
//			                    .setConnectTimeout(5000)
//			                    .setSocketTimeout(60000);
//			            }
//			        });
//		//client = new RestHighLevelClient(RestClient.builder(new HttpHost(ElasticHost, 9200, "http")));
//		client = new RestHighLevelClient(builder);
//		  
//	}
//http://172.16.3.60:9222/ write
	// read 172.16.3.60
	public RestHighLevelClient getClient() {
		return client;
	}

	public static synchronized ElasticClient getInstance(String host) {
		// synchronized (ElasticClient.class) {
		if (instance == null) {
			// if(DataCenter==3) {
			// instance = new ElasticClient (ElasticHostDev);
			// }else {
			instance = new ElasticClient(host);

			// }

		}
		// }
		return instance;
	}

	// thanhphi0401-29082019
//	public <T> boolean IndexObject(String index, String type, T data, String id) throws Exception {
//		 queueLock.lock();
//
//		// build json
//		String json = mapper.writeValueAsString(data);
//		// String json = gson.toJson(data);
//		IndexRequest indexRequest = new IndexRequest(index).id(id).source(json, XContentType.JSON);
//
//		try {
//			IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
//			
//			if (response != null) {
//				if (response.status() == RestStatus.OK || response.status() == RestStatus.CREATED
//						|| response.status() == RestStatus.ACCEPTED) {
//					return true;
//				} else {
//					throw new Exception("ElasticIndexer::AddUpdateObject::" + response.status().toString() + " "
//							+ response.getResult().toString());
//				}
//			}
//			return false;
//		} catch (Exception e) {
//			 
//			throw e;
//		}finally {
//			 queueLock.unlock();
//		}
//	}

	// thanhphi0401-29082019
//	public Boolean Delete(String index, String type, String itemKey) throws Exception {
//		 queueLock.lock();
//		DeleteRequest request = new DeleteRequest(index, itemKey);
//
//		try {
//			DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
//			 
//			if (deleteResponse != null) {
//				if (deleteResponse.status() == RestStatus.OK) {
//					return true;
//				}
//
//				throw new Exception("ElasticIndexer::DeleteObject::" + deleteResponse.status().toString() + " "
//						+ deleteResponse.getResult().toString());
//			}
//			return false;
//		} catch (Exception e) {
//			 
//			throw e;
//		}finally {
//			 queueLock.unlock();
//		}
//
//	}

	public void Search() {

		SearchRequest request = new SearchRequest("ms_product");
		// request.types("product");
		// ????????
		// SearchSourceBuilder builder = new SearchSourceBuilder();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		// ??????

//	        searchSourceBuilder.query(QueryBuilders.matchAllQuery()); //????
		// builder.query(QueryBuilders.matchAllQuery());
		searchSourceBuilder.query(QueryBuilders.termQuery("Keyword", "bap")); // term??
//	        builder.query(QueryBuilders.termQuery("title","??"));
//	        searchSourceBuilder.query(QueryBuilders.matchQuery("title","????")); //????
//	        builder.query(QueryBuilders.matchQuery("title","??"));
//	        searchSourceBuilder.query(QueryBuilders.rangeQuery("price").gte(1000).lte(10000)); //????
//	        builder.query(QueryBuilders.rangeQuery("price").gte(1000).lte(10000));
//	        searchSourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("title","??")).must(QueryBuilders.rangeQuery("price").gte(3000).lte(4000))); //????
//	        builder.query(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("title","??")).must(QueryBuilders.rangeQuery("price").gte(1000).lte(100000)));
//	        searchSourceBuilder.query(QueryBuilders.fuzzyQuery("title","??").fuzziness(Fuzziness.ONE)); //????
//	        builder.query(QueryBuilders.fuzzyQuery("title","??").fuzziness(Fuzziness.ONE));
//	        searchSourceBuilder.query(QueryBuilders.wildcardQuery("title","*?*")); //?????
//	        builder.query(QueryBuilders.wildcardQuery("title","*?*"));

		// ????
		// ????
//	        builder.query(QueryBuilders.matchAllQuery());
		// searchSourceBuilder.fetchSource(new String[]{"ProductID","keyword"},null);
		// ?????
		// builder.query(QueryBuilders.matchQuery("title","????"));
//	        builder.postFilter(QueryBuilders.boolQuery().
//	                must(QueryBuilders.rangeQuery("price").gte(1000).lte(4000)).
//	                must(QueryBuilders.matchQuery("brand","??")));

		searchSourceBuilder.from(0); // ????
		searchSourceBuilder.size(2); // ?????
		searchSourceBuilder.sort("ProductID", SortOrder.DESC);

		// ??????

//	        builder.query(QueryBuilders.matchQuery("title","??")); //????
//	        HighlightBuilder highlightBuilder = new HighlightBuilder();
//	        highlightBuilder.preTags("<span style='color:red'>");
//	        highlightBuilder.postTags("</span>");
//	        highlightBuilder.field("title");
//	        builder.highlighter(highlightBuilder);

		// 1????????
//	        builder.query(QueryBuilders.matchAllQuery());
//	        AvgAggregationBuilder aggregationBuilder = AggregationBuilders.avg("avgPrice").field("price");
//	        builder.aggregation(aggregationBuilder);

//	        ???????searchRequest
		request.source(searchSourceBuilder);
//	        ???? ??searchResponse
		SearchResponse search = null;
		try {
			search = client.search(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SearchHits hits = search.getHits();
		System.out.println(" ada" + hits.getTotalHits());

	}

	public <T> T GetFieldsByObject(String currentindexdb, String iD, Class<T> valueType, String[] includes) {
		try {
			GetRequest getRequest = new GetRequest(currentindexdb, iD);

			String[] excludes = Strings.EMPTY_ARRAY;
			FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
			getRequest.fetchSourceContext(fetchSourceContext);
			GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
			if (getResponse.isExists()) {
				String sourceAsString = getResponse.getSourceAsString();
				return mapper.readValue(sourceAsString, valueType);
			}
		} catch (Exception e) {
			Logs.WriteLine(e);
		}
		return null;
	}

	public <T> T GetSingleObject(String currentindexdb, String iD, Class<T> valueType) throws Throwable{
		 
			GetRequest getRequest = new GetRequest(currentindexdb, iD);
			GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
			if (getResponse.isExists()) {
				String sourceAsString = getResponse.getSourceAsString();
				var value = mapper.readValue(sourceAsString, valueType);
				return value;
			}
			return null;
	 
	}
	
	public  SearchResponse searchRequest(SearchSourceBuilder builder, final String... indices) throws IOException {
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(indices);
		if(builder != null )
			searchRequest.source(builder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		return searchResponse;
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
//						LOGGER.error(ex);
						throw new RuntimeException(ex);
					}
				})
				.collect(Collectors.toList());
		return results;		
	}
	public  List<String> getIds(SearchResponse searchResponse){
		if(searchResponse.getHits() == null 
				|| searchResponse.getHits().getHits() == null
				|| searchResponse.getHits().getHits().length == 0) return List.of();
		SearchHit[] hits = searchResponse.getHits().getHits();
		List<String> results = (List<String>) Arrays.stream(hits)
				.map(SearchHit::getId)
				.collect(Collectors.toList());
		return results;		
	}
	public <T> T getSingleSource(SearchResponse searchResponse, Class<T> c) throws JsonParseException, JsonMappingException, UnsupportedEncodingException, IOException {
		if(searchResponse.getHits() == null 
				|| searchResponse.getHits().getHits() == null
				|| searchResponse.getHits().getHits().length == 0) return null;
		SearchHit[] hits = searchResponse.getHits().getHits();
		if(hits[0] != null) {
			var t = mapper.readValue(hits[0].getSourceAsString().getBytes("utf-8"), c);
			return t;
		}
		return null;
	}

	//thanhphi
	@SuppressWarnings("unchecked")
	public <T> T GetSingleObjectNew(String currentindexdb, String iD, Class<T> valueType) {
		try {
			GetRequest getRequest = new GetRequest(currentindexdb, iD);
			GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
			if (getResponse.isExists()) {
				String sourceAsString = getResponse.getSourceAsString();
				var value = mapper.readValue(sourceAsString, valueType);

				var allSuperProperty = value.getClass().getSuperclass().getDeclaredFields();
				var allChildProperty = value.getClass().getDeclaredFields();

				for (Field f : allSuperProperty) {		
					if (f.getName().equals("Version")) {
						f.set(value, getResponse.getVersion());
					}
				}

				for (Field f : allChildProperty) {			
					if (f.getName().equals("Version")) {
						f.set(value, getResponse.getVersion());
					}
				}

				return value;
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
		}
		return null;
	}


	public SearchHit[] searchObject(String currentIndexDB, SearchSourceBuilder searchSourceBuilder) {
		SearchRequest request = new SearchRequest();
		request.indices(currentIndexDB);
		request.source(searchSourceBuilder);
		try {
			SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
			if (searchResponse.getHits() == null
					|| searchResponse.getHits().getHits() == null
					|| searchResponse.getHits().getHits().length == 0){
				return null;
			}
			SearchHit[] hits = searchResponse.getHits().getHits();
			return hits;
		} catch (Throwable e) {
			throw new ElasticsearchException("Error while searching for request: " + request.toString(), e);
		}
	}
}
