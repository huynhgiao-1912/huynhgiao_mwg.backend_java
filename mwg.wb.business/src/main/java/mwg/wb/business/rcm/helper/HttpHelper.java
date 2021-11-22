package  mwg.wb.business.rcm.helper;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpHelper {
	private static final CloseableHttpClient httpClient = HttpClients.createDefault();
	public static CloseableHttpResponse sendGetRequest(HttpGet httpGet) throws ClientProtocolException, IOException {
		return httpClient.execute(httpGet);
	}
	
	public CloseableHttpResponse sendPostRequest(HttpPost httpPost) throws ClientProtocolException, IOException {
		return httpClient.execute(httpPost);
	}
}
