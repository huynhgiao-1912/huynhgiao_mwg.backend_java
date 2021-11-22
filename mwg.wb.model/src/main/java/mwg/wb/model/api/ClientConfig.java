package mwg.wb.model.api;

 
public class ClientConfig {
	public String REDIS_HOST_NLU;
	public int RUN_VERSION;
	public int DATACENTER;
	// public int ISWRITE_ES;
	public int ISWRITE_LOG;
	public boolean ISAPI;
	public int SERVER_ORDER;
	public int IS_NOT_PROCESS_LOG;
	public int IS_NOT_UPDATE_ES;

	// rabitmq
	public String SERVER_RABBITMQ_URL;
	public long CreatedDate;
	// DB
	public String tns_admin;
	public String wallet_location;
	public String DB_CONNECTIONSTRING;
	public String DB_URL;
	public String DB_USER;
	public String DB_PASS;

	// ERP http://betaerpsupportservices.thegioididong.com/Web/WSWeb.asmx
	public String ERP_SERVICES_URL;
	// ERP PROMO http://erpwebsupportservices.thegioididong.com/Web/WSPromotion.asmx
	public String ERP_SERVICES_PROMOTION_URL;
	public String ERP_SERVICES_AUTHEN;
	
	//crm http://crm-services.thegioididong.com/NEW-CRMTGDD/CRMTGDDService.asmx
	public String CRM_SERVIVES_URL;
	
	//POS BHX http://crm-services.thegioididong.com/NEW-CRMTGDD/CRMTGDDService.asmx	
	public String BHX_SERVICE_URL;
	public String BHX_SERICE_TOKEN;

	// API
	public String API_SERVER_ORIENTDB_READ_USER;
	public String API_SERVER_ORIENTDB_READ_PASS;
	public String API_SERVER_ORIENTDB_READ_URL;

	// ES
	public String SERVER_ELASTICSEARCH_WRITE_HOST;
	public String SERVER_ELASTICSEARCH_READ_HOST;
	public String SERVER_ELASTICSEARCH_FB_READ_HOST;
	public String ELASTICSEARCH_PRODUCT_INDEX;
	public String ELASTICSEARCH_PRODUCT_GALLERY_INDEX;
	public String ELASTICSEARCH_PRODUCT_OLD_INDEX;
	public String ELASTICSEARCH_PAYMENT_INDEX;
	public String ELASTICSEARCH_PROMOTION_INDEX = "ms_promotion";

	// JOB READ
	public String SERVER_ORIENTDB_READ_USER;
	public String SERVER_ORIENTDB_READ_PASS;

	public String SERVER_ORIENTDB_READ_URL1;
	public String SERVER_ORIENTDB_READ_URL2;
	public String SERVER_ORIENTDB_READ_URL3;

	public String SERVER_ORIENTDB_READ_URL4;
	public String SERVER_ORIENTDB_READ_URL5;
	public String SERVER_ORIENTDB_READ_URL6;

	// JOB WRITE
	public String SERVER_ORIENTDB_WRITE_USER;
	public String SERVER_ORIENTDB_WRITE_PASS;
	public String SERVER_ORIENTDB_WRITE_URL1;
	public String SERVER_ORIENTDB_WRITE_URL2;
	public String SERVER_ORIENTDB_WRITE_URL3;

	public String SERVER_ORIENTDB_WRITE_URL4;
	public String SERVER_ORIENTDB_WRITE_URL5;
	public String SERVER_ORIENTDB_WRITE_URL6;
	// KV
	public String IGNITE_URL;
	public String IGNITE_APP;
	public String IGNITE_USER;
	public String IGNITE_PASS;

	public String API_VERSION;
	
	public String RCM_PIO_ACCESS_KEY;
	public String RCM_PIO_APP_URL; 
	public String RCM_PIO_ENGINE_URL;
	public String RCM_ES_HOST;
	public int RCM_ES_PORT; 
	public String RCM_ES_PROTOCOL;
	  
	//redis
	public String REDIS_CONNECTION_STRING;
	public String REDIS_CHAT_DIDX;
	public int REDIS_CHAT_DIDX_NUMBERDB;

}
