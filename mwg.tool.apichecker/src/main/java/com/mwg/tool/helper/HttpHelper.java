package com.mwg.tool.helper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HttpHelper {
	 
	 public static CompletableFuture<HttpResponse<String>> sendAsyc(HttpRequest request) {
		 HttpClient client = HttpClient.newBuilder()
					.version(Version.HTTP_1_1)
//					.connectTimeout(Duration.ofSeconds(5))
//					.authenticator(Authenticator.getDefault())
					.build();
			
		 CompletableFuture<HttpResponse<String>> response =  client.sendAsync(request, BodyHandlers.ofString());
		 return response;
		 
	 }
	 
	 public static HttpResponse<String> sendSync(HttpRequest request) throws IOException, InterruptedException {
		 HttpClient client = HttpClient.newBuilder()
					.version(Version.HTTP_1_1)
//					.connectTimeout(Duration.ofSeconds(5))
//					.authenticator(Authenticator.getDefault())
					.build();
			
		HttpResponse<String> response =  client.send(request, BodyHandlers.ofString());
		 return response;
		 
	 }
	 
	 public static String sendSyncGetRequest(String url, String... headers) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException, IOException {
		 HttpRequest.Builder builder = HttpRequest
					.newBuilder(new URI(url))
//					.headers(headers)
					.timeout(Duration.ofSeconds(10));
		 
		 if(!Objects.isNull(headers) && headers.length > 0) builder.headers(headers);

		 HttpResponse<String> response = HttpHelper.sendSync(builder.build()); // .sendGetRequest(url, builder);//
		 //var vkl = HttpHelper.sendGetRequest(url,headers);
		 
		 if(Objects.isNull(response)) return null;
		 
		 return response.body();
	 }
	 
	 public static String sendSyncPostRequest(String url, String body, String... headers) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException, IOException {
		 HttpRequest.Builder builder = HttpRequest
					.newBuilder(new URI(url))
					.timeout(Duration.ofSeconds(10))
					.POST(HttpRequest.BodyPublishers.ofString(body));
		 
		if(!Objects.isNull(headers) && headers.length > 0) builder.headers(headers);
		
		HttpResponse<String> response = HttpHelper.sendSync(builder.build());
		
		if(Objects.isNull(response)) return null;
		
		return response.body();
	 }
	 
	 public static String sendAyncGetRequest(String url, String... headers) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {
		 HttpRequest.Builder builder = HttpRequest
					.newBuilder(new URI(url))
//					.headers(headers)
					.timeout(Duration.ofSeconds(10));
		 
		 if(!Objects.isNull(headers) && headers.length > 0) builder.headers(headers);

		 CompletableFuture<HttpResponse<String>> responseFuture = HttpHelper.sendAsyc(builder.build()); // .sendGetRequest(url, builder);//
		 //var vkl = HttpHelper.sendGetRequest(url,headers);
		 
		 if(Objects.isNull(responseFuture)) return null;
		 
		 return responseFuture.get(10, TimeUnit.SECONDS).body();
	 }
	 
	 public static String sendAyncPostRequest(String url, String body, String... headers) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {
		 HttpRequest.Builder builder = HttpRequest
					.newBuilder(new URI(url))
					.timeout(Duration.ofSeconds(10))
					.POST(HttpRequest.BodyPublishers.ofString(body));
		 
		if(!Objects.isNull(headers) && headers.length > 0) builder.headers(headers);
		
		CompletableFuture<HttpResponse<String>> responseFuture = HttpHelper.sendAsyc(builder.build());
		
		if(Objects.isNull(responseFuture)) return null;
		
		return responseFuture.get(5, TimeUnit.SECONDS).body();
	 }
}
