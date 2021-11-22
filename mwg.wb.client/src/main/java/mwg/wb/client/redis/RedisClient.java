package mwg.wb.client.redis;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
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
import org.elasticsearch.search.SearchHits;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
//import org.springframework.context.annotation.Configuration;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.SortOrder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.common.JsonConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import redis.clients.jedis.Jedis;

//@Configuration
//@RefreshScope
public class RedisClient {
	Jedis client = null;// new Jedis("localhost");
	private static RedisClient instance;

	static ObjectMapper mapper = null;

	private static String SERVER_REDIS_READ_HOST = "";

	JsonConfig jsonConfig = null;

	public RedisClient(String host) {
		client = new Jedis(host);
		SERVER_REDIS_READ_HOST = host;
	}

//	public RedisClient(ClientConfig config)
//	{
//		SERVER_REDIS_READ_HOST = config.REDIS_CONNECTION_STRING;
//		if(Utils.StringIsEmpty(SERVER_REDIS_READ_HOST))
//			SERVER_REDIS_READ_HOST = "192.168.2.90:6379,192.168.2.91:6379,192.168.2.92:6379,192.168.2.93:6379,192.168.2.94:6379,192.168.2.95:6379,192.168.2.96:6379,192.168.2.97:6379,192.168.2.98:6379,192.168.2.99:6379,allowAdmin=true,defaultDatabase=0,abortConnect=false,syncTimeout=3000";
//		client = new Jedis(SERVER_REDIS_READ_HOST);
//		
//	}

	public Jedis getClient() {
		return client;
	}

	public void choiceDB(int dbNumber) {
		client.select(dbNumber);
	}

//	public Jedis incr(String key) {
//		return this.client.incr(key);
//	}
	public static synchronized RedisClient getInstance(String host) {

		if (instance == null) {
			instance = new RedisClient(host);
		}
		return instance;
	}

	public String Set(String key, String value) throws Throwable {
		return client.set(key, value);
	}

	public String Get(String key) {
		return client.get(key);
	}

	public Long incrBy(String key, int increment) {
		return client.incrBy(key, increment);
	}

	public List<String> mget(final String... keys) {
		return client.mget(keys);
	}

}
