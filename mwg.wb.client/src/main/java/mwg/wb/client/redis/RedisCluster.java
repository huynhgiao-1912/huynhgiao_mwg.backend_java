package mwg.wb.client.redis;

import java.util.HashSet;
import java.util.Set;

import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class RedisCluster {
	JedisCluster client = null;
	private static String SERVER_REDIS_READ_HOST = "";
	
	public RedisCluster(ClientConfig config) throws Throwable
	{
		SERVER_REDIS_READ_HOST = config.REDIS_CONNECTION_STRING;
//		SERVER_REDIS_READ_HOST = "";
		if(Utils.StringIsEmpty(SERVER_REDIS_READ_HOST))
			SERVER_REDIS_READ_HOST = "192.168.2.90:6379,192.168.2.91:6379,192.168.2.92:6379,192.168.2.93:6379,192.168.2.94:6379,192.168.2.95:6379,192.168.2.96:6379,192.168.2.97:6379,192.168.2.99:6379";
		//client = new JedisCluster(SERVER_REDIS_READ_HOST);
		if(Utils.StringIsEmpty(SERVER_REDIS_READ_HOST))
			throw new Throwable("Redis host wrong");
		
		Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
		var lstHost = SERVER_REDIS_READ_HOST.split(",");
		for (String string : lstHost) {
			var ipport = string.split(":");
			if(ipport.length == 2)
			{
				jedisClusterNodes.add(new HostAndPort(ipport[0],Utils.toInt(ipport[1])));
			}
		}
		client = new JedisCluster(jedisClusterNodes);
		
	}
	
	public String Set(String key, String value) throws Throwable {
		
		return client.set(key, value);

	}
	
	public String Get(String key ) {
		return client.get(key);
	}
	
	public String createKeyRedisStock(String productCode,int provinceId, int storeid)
	{
		return "KEY_STOCK_STORE_PRODUCT_" + provinceId + "_" + storeid + "_" + productCode;
	}
}
