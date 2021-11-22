package  mwg.wb.business.rcm.helper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
 

public class HttpRequestHelper {
	//private static final Logger LOGGER = Logger.getLogger(HttpRequestHelper.class);
//	private static HttpClient httpClient;
//	
//	 static {
//		 httpClient = HttpClient.newBuilder()
//					.version(Version.HTTP_1_1)
//					.connectTimeout(Duration.ofSeconds(5))
////					.authenticator(Authenticator.getDefault())
//					.build();
//			
//	 }

	 public static HttpResponse<String> sendAsycRequest(HttpRequest request) {
		 HttpClient client = HttpClient.newBuilder()
					.version(Version.HTTP_1_1)
					.connectTimeout(Duration.ofSeconds(5))
//					.authenticator(Authenticator.getDefault())
					.build();
			
		 CompletableFuture<HttpResponse<String>> response =  client.sendAsync(request, BodyHandlers.ofString())
			        												.thenApply(res -> res);
		 try {
			HttpResponse<String> result = response.get(5, TimeUnit.SECONDS);
			return result;
		} catch (InterruptedException | ExecutionException | TimeoutException  e) {
			e.printStackTrace();
			//LOGGER.error(e.getMessage());
		} 
			return null;
	 }
	 
	 public static CompletableFuture<HttpResponse<String>> sendAsyc(HttpRequest request) {
		 HttpClient client = HttpClient.newBuilder()
					.version(Version.HTTP_1_1)
					.connectTimeout(Duration.ofSeconds(5))
//					.authenticator(Authenticator.getDefault())
					.build();
			
		 CompletableFuture<HttpResponse<String>> response =  client.sendAsync(request, BodyHandlers.ofString());
		 return response;
		 
	 }
}
