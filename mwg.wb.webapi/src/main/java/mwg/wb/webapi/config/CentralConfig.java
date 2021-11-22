package mwg.wb.webapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class CentralConfig {
	@Value("${DATACENTER:0}")
	private int DataCenter;

	public int GetDataCenter() {
		return DataCenter;
	}

	@Value("${API_SERVER_ORIENTDB_READ_USER:none}")
	private String API_SERVER_ORIENTDB_READ_USER;

	public String getAPI_SERVER_ORIENTDB_READ_USER() {
		return API_SERVER_ORIENTDB_READ_USER;
	}

	@Value("${API_SERVER_ORIENTDB_READ_PASS:none}")
	private String API_SERVER_ORIENTDB_READ_PASS;

	public String getAPI_SERVER_ORIENTDB_READ_PASS() {
		return API_SERVER_ORIENTDB_READ_PASS;
	}

	@Value("${API_SERVER_ORIENTDB_READ_URL:none}")
	private String API_SERVER_ORIENTDB_READ_URL;

	public String getAPI_SERVER_ORIENTDB_READ_URL() {
		return API_SERVER_ORIENTDB_READ_URL;
	}
	@Value("${API_SERVER_ELASTICSEARCH_READ_HOST:none}")
	private String API_SERVER_ELASTICSEARCH_READ_HOST;

	public String getAPI_SERVER_ELASTICSEARCH_READ_HOST() {
		return API_SERVER_ELASTICSEARCH_READ_HOST;
	}
	
	
	@Value("${SERVER_RABBITMQ_URL:none}")
	private String SERVER_RABBITMQ_URL;

	public String getSERVER_RABBITMQ_URL() {
		return SERVER_RABBITMQ_URL;
	}
	
	
	
	
	
	
	
	
}
