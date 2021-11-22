package mwg.wb.client.elasticsearch;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.rest.RestStatus;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.common.JsonConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;

//@Configuration
//@RefreshScope
public class ElasticClientWriteSCS {

	private static ElasticClientWriteSCS instance;
	private RestHighLevelClient client = null;
	//private final Lock queueLock = new ReentrantLock();

	static ObjectMapper mapper = null;

	// 172.16.3.60:9222 write
	// 172.16.3.60:9200 read
	// http://172.16.3.120:9200/
	// private String ElasticHost = "172.16.3.60";
	// private int ElasticPort = 9222;
	// private static String ElasticHost = "172.16.3.23";
	// private static String ElasticHostDev = "10.1.6.151";
	private static String SERVER_ELASTICSEARCH_WRITE_HOST = "";

	JsonConfig jsonConfig = null;
	private int ElasticPort = 9200;

//	@SuppressWarnings("resource")
	public ElasticClientWriteSCS(String host) {

		SERVER_ELASTICSEARCH_WRITE_HOST = host;

		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		mapper.setDateFormat(df);

		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		RestClientBuilder builder = RestClient.builder(new HttpHost(SERVER_ELASTICSEARCH_WRITE_HOST, 9200))
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
		// client = new RestHighLevelClient(
		// RestClient.builder(new HttpHost(SERVER_ELASTICSEARCH_WRITE_HOST, ElasticPort,
		// "http")));
		client = new RestHighLevelClient(builder);
	}

	public RestHighLevelClient getClient() {
		return client;
	}

	public static synchronized ElasticClientWriteSCS getInstance(String host) {
		// synchronized (ElasticClient.class) {
		if (instance == null) {

			instance = new ElasticClientWriteSCS(host);
		}
		// }
		return instance;
	}

	public <T> boolean IndexObject2(String index, String type, T data, String id) throws Exception {
		
		// build json
		String json = mapper.writeValueAsString(data);
		// String json = gson.toJson(data);
		IndexRequest indexRequest = new IndexRequest(index).id(id).source(json, XContentType.JSON);

		try {
			IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
			
			if (response != null) {
				if (response.status() == RestStatus.OK || response.status() == RestStatus.CREATED
						|| response.status() == RestStatus.ACCEPTED) {
					return true;
				} else {
					throw new Exception("ElasticIndexer::AddUpdateObject::" + response.status().toString() + " "
							+ response.getResult().toString());
				}
			}
			return false;
		} catch (Exception e) {

			throw e;
		} finally {

		}
	}

	// thanhphi0401-29082019
	public <T> boolean IndexObject(String index, T data, String id) throws Exception {
		

		// build json
		String json = mapper.writeValueAsString(data);
		// String json = gson.toJson(data);
		IndexRequest indexRequest = new IndexRequest(index).id(id).source(json, XContentType.JSON);

		try {
			IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
			
			if (response != null) {
				if (response.status() == RestStatus.OK || response.status() == RestStatus.CREATED
						|| response.status() == RestStatus.ACCEPTED) {
					return true;
				} else {
					throw new Exception("ElasticIndexer::AddUpdateObject::" + response.status().toString() + " "
							+ response.getResult().toString());
				}
			}
			return false;
		} catch (Exception e) {
			throw e;
		}
	}

	public <T> long IndexObjectNew(String index, T data, String id) throws Throwable {
		

		// build json
		String json = mapper.writeValueAsString(data);
		// String json = gson.toJson(data);
		IndexRequest indexRequest = new IndexRequest(index).id(id).source(json, XContentType.JSON)
				.versionType(VersionType.EXTERNAL).version(1);

		IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
		
		if (response != null) {
			if (response.status() == RestStatus.OK || response.status() == RestStatus.CREATED
					|| response.status() == RestStatus.ACCEPTED) {

				return response.getVersion();
			} else {
				throw new Exception("ElasticIndexer::AddUpdateObject::" + response.status().toString() + " "
						+ response.getResult().toString());
			}
		}
		return -1;

	}

	public <T> long UpdateObjectNew(String index, T data, String id) throws Throwable {
		

		// build json
		String json = mapper.writeValueAsString(data);

		var update = new UpdateRequest(index, id).doc(json, XContentType.JSON).docAsUpsert(true).detectNoop(false);

		var response = client.update(update, RequestOptions.DEFAULT);
	
		if (response != null) {
			if (response != null && response.getResult() == Result.UPDATED) {

				return response.getVersion();
			}
			return -1;
		}
		return -1;

	}

	// thanhphi0401-29082019
	public Boolean Delete(String index, String type, String itemKey) throws Exception {
		
		DeleteRequest request = new DeleteRequest(index, itemKey);

		try {
			DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
			

			if (deleteResponse != null) {
				if (deleteResponse.status() == RestStatus.OK) {
					return true;
				}

				throw new Exception("ElasticIndexer::DeleteObject::" + deleteResponse.status().toString() + " "
						+ deleteResponse.getResult().toString());
			}
			return false;
		} catch (Exception e) {
			throw e;
		}

	}

	public Boolean UpdateFieldOBject(String indexDB, String esKeyTerm, String field, Object solist) throws Exception {
		
		var json = mapper.writeValueAsString(solist);
		var update = new UpdateRequest(indexDB, esKeyTerm).doc("{\"" + field + "\":" + json + "}", XContentType.JSON)
				.docAsUpsert(true).detectNoop(false);
		var client = ElasticClientWrite.getInstance(SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
		var response = client.update(update, RequestOptions.DEFAULT);
		
		if (response != null && response.getResult() == Result.UPDATED) {

			return true;
		} else {

			Logs.WriteLine(
					"-Index   FAILED: " + esKeyTerm + ", " + response.getResult().name() + "######################");
			Utils.Sleep(100);
		}
		return false;
	}
}
