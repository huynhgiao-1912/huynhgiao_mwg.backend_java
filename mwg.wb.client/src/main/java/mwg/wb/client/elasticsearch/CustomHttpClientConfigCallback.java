package mwg.wb.client.elasticsearch;

import java.io.IOException;

import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.elasticsearch.client.RestClientBuilder;

import mwg.wb.common.Logs;

public class CustomHttpClientConfigCallback implements RestClientBuilder.HttpClientConfigCallback {
	@Override
	  public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {

	    // Add custom exception handler.
	    // - https://hc.apache.org/httpcomponents-core-ga/tutorial/html/nio.html#d5e601
	    // - This always handles the exception and just logs a warning.
	    try {
	      DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
	      ioReactor.setExceptionHandler(new IOReactorExceptionHandler() {
	        @Override
	        public boolean handle(IOException e) {
	          Logs.LogException("System may be unstable: IOReactor encountered a checked exception : " + e.getMessage(), e);
	          return true; // Return true to note this exception as handled, it will not be re-thrown
	        }

	        @Override
	        public boolean handle(RuntimeException e) {
	        	Logs.LogException("System may be unstable: IOReactor encountered a runtime exception : " + e.getMessage(), e);
	          return true; // Return true to note this exception as handled, it will not be re-thrown
	        }
	      });

	      httpClientBuilder.setConnectionManager(new PoolingNHttpClientConnectionManager(ioReactor));
	    } catch (IOReactorException e) {
	      throw new RuntimeException(e);
	    }

	    return httpClientBuilder;
	  }
}
