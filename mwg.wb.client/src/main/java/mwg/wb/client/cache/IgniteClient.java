package mwg.wb.client.cache;

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwg.kvclient.KVClient;
import com.mwg.kvclient.KVResponse;

import mwg.wb.common.DidxHelper;
import mwg.wb.model.api.ClientConfig;

public class IgniteClient {

	private static String URL = null;// "http://10.1.5.68/KVProject/kvp";
	private static String APP = null;// "web";
	private static String USER = null;// "web";
	private static String PASS = null;// "123456";
	static Long keyVersion = 10L;
	//static Long lifeTimeInMillis = Duration.ofMinutes(15).toMillis();
	static Long lifeTimeInMillis = Duration.ofDays(3600).toMillis();
	private static KVClient kvClient = null;
	private static IgniteClient ignite = null;

	public IgniteClient(ClientConfig config) { // String host
		if (config.DATACENTER == 3) {
			IgniteClient.URL = "http://10.1.5.68/KVProject/kvp";
			IgniteClient.APP = "web";
			IgniteClient.USER = "web";
			IgniteClient.PASS = "123456";
		} else {
			IgniteClient.URL = config.IGNITE_URL;
			IgniteClient.APP = config.IGNITE_APP;
			IgniteClient.USER = config.IGNITE_USER;
			IgniteClient.PASS = config.IGNITE_PASS;
		}
		if(kvClient == null)
			kvClient = new KVClient(URL);
	}

	public static IgniteClient GetClient(ClientConfig config) throws Exception {
		if(ignite == null)
			ignite = new IgniteClient(config);
		
		return ignite;
	}

	public boolean SetKey(String Key, Object object) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(object);
		KVResponse response = kvClient.setKey(APP, Key, USER, PASS, jsonString, keyVersion, lifeTimeInMillis);
		// System.out.println(String.format("SET ResponseCode: %d",
		// response.getErrorCode()));
		if (response.getErrorCode() == 0)
			return true;
		return false;
	}

	@SuppressWarnings("hiding")
	public <T> T GetKey(String Key, Class<T> object) throws Exception {

		var response = kvClient.getKey(APP, Key, USER, PASS);
		if (response.getValue() != null) {
			ObjectMapper mapper = DidxHelper.generateJsonMapper("yyyy-MM-dd'T'HH:mm:ss");
			var result = mapper.readValue(response.getValue(), object);
			if (result != null)
				return result;
		}
		return null;
	}
	public String   GetString(String Key ) throws Exception {

		var response = kvClient.getKey(APP, Key, USER, PASS);
		return response.getValue() ;
	}
	
	public boolean SetString(String Key, String data) throws Exception {
 
		KVResponse response = kvClient.setKey(APP, Key, USER, PASS, data, keyVersion, lifeTimeInMillis);
		 
		if (response.getErrorCode() == 0)
			return true;
		return false;
	}
	public boolean RemoveKey(String Key) throws Exception {

		var response = kvClient.remKey(APP, Key, USER, PASS);
		System.out.println(String.format("REMOVE ResponseCode: %d", response.getErrorCode()));
		if (response.getErrorCode() == 0)
			return true;
		return false;
	}

}
