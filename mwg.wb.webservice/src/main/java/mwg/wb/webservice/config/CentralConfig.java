package mwg.wb.webservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class CentralConfig {
	
	@Value("${DATACENTER:0}")
	private int DataCenter;

	public int getDataCenter() {
		return DataCenter;
	}
	
	@Value("${DB_CONNECTION_STRING:none}")
	private String DB_CONNECTION_STRING;
	@Value("${DB_USER:none}")
	private String DB_USER;
	@Value("${DB_PASS:none}")
	private String DB_PASS;

	public String getCONNECTION_STRING() {
		return DB_CONNECTION_STRING;
	}
	public String getDB_USER() {
		return DB_USER;
	}
	public String getDB_PASS() {
		return DB_PASS;
	}
	
	
}
