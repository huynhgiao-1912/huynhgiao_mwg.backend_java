package mwg.wb.business.webservice;

import mwg.wb.model.system.SystemConfigBO;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.tinkerpop.gremlin.structure.T;

import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.common.DidxHelper;
import mwg.wb.model.gameapp.GameAppBO;
import mwg.wb.model.webservice.NewsView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
import org.apache.http.client.HttpClient;
 
public class WebserviceHelper {

	// one instance, reuse
	private final CloseableHttpClient httpClient = HttpClients.createDefault();
	private int DATACENTER = 3;
	private static WebserviceHelper webservice = null;

	public WebserviceHelper(int DATACENTER) {
		this.DATACENTER = DATACENTER;
	}

	public static void main(String[] args) throws Exception {
		//var aa = WebserviceHelper.Call(3).Get("apinews/gettopview7days", NewsView[].class);
		var data = WebserviceHelper.Call(3).Get("apisystem/getallsystemconfig", SystemConfigBO[].class);
		System.out.println(data.toString());
	}

	public static WebserviceHelper Call(int DataCenter) {
		if(webservice == null) 
			webservice = new WebserviceHelper(DataCenter);
		else
			webservice.DATACENTER = DataCenter;;
		
		return webservice;
	}

	private String GetServer() {
		if (DATACENTER == 3) {
			return "http://10.1.4.123:2061/";
		}
		else {
			return "http://172.16.5.54:2060/";
		}
	}

	private void close() throws IOException {
		httpClient.close();
	}

	public <T> T Get(String url, Class<T> object, Object... params) throws Exception {
		// url => apigameapp/getgallerybyproductid?gameAppID=:?
		// url => apigameapp/getgameapptbyid?gameAppID=:?&siteID=:?

		for (int i = 0; i < params.length; i++) {
			url = url.replaceFirst(":\\?", params[i].toString().replace(" ", "%20"));
		}

		String StrUrl = GetServer() + url;

		HttpGet request = new HttpGet(StrUrl);
		
	 
		 
	 
		// add request headers
		request.addHeader("Content-Type", "application/json");
		request.addHeader(HttpHeaders.USER_AGENT, "MWG-Client");
		CloseableHttpResponse response = null;
		try {
			
			response = httpClient.execute(request);
					
			// Get HttpResponse Status
			//System.out.println(response.getStatusLine().toString());
			if (response.getStatusLine().getStatusCode() == 200) {

				HttpEntity entity = response.getEntity();
				Header headers = entity.getContentType();
				 
				if (entity != null) {
					String result = EntityUtils.toString(entity);
					System.out.println(result);
					ObjectMapper mapper = DidxHelper.generateJsonMapper("yyyy-MM-dd'T'HH:mm:ss");
					var resultObject = mapper.readValue(result, object);
					if (resultObject != null)
						return resultObject;
				}
			}

		} catch (Throwable e) {
			// TODO: handle exception
			e.printStackTrace();
			throw e;
		} finally {
			//this.close();
			if(response != null)
				response.close();
		}
		return null;

	}

	private <T> T Post(String url, Class<T> object, Map<String, String> map) throws Exception {
		
		String StrUrl = GetServer() + url;
		HttpPost post = new HttpPost(StrUrl);
		
		
		// add request parameter, form parameters
		List<NameValuePair> urlParameters = new ArrayList<>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		
		post.setEntity(new UrlEncodedFormEntity(urlParameters));

		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(post)) {

			System.out.println(EntityUtils.toString(response.getEntity()));
			
			ObjectMapper mapper = DidxHelper.generateJsonMapper("yyyy-MM-dd'T'HH:mm:ss");
			var resultObject = mapper.readValue(EntityUtils.toString(response.getEntity()), object);
			if (resultObject != null)
				return resultObject;
			
		}catch (Throwable e) {
			 
			throw e;
		}finally {
			this.close();
		}
		return null;

	}
}
