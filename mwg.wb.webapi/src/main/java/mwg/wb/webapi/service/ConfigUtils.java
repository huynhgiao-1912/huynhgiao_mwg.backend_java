
package mwg.wb.webapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.business.DataCenterHelper;
import mwg.wb.common.DataCenterType;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.webapi.config.CentralConfig;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public final class ConfigUtils {
	private static CentralConfig myConfig;
	// private static ClientConfig config;

	public static void setMyConfig(CentralConfig myConfig) {
		ConfigUtils.myConfig = myConfig;
	}

	private ConfigUtils() {

	}

	public static int GetDataCenter() {
		return myConfig.GetDataCenter();

	}

	public static String GetOnlineClientConfigStr() {
		ObjectMapper mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		var configc = GetOnlineClientConfig();
		try {
			return mapper.writeValueAsString(configc);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "_";
	}

	public static synchronized ClientConfig GetOnlineClientConfig() {

		ClientConfig config = new ClientConfig();
		config.ISAPI = false;
		config.API_VERSION = "28-05-2020";
		config.CreatedDate = System.currentTimeMillis();
		config.DATACENTER = DataCenterHelper.GetDataCenter(myConfig.GetDataCenter());
		if (config.DATACENTER == 3) {
			config.API_SERVER_ORIENTDB_READ_USER = "onlyread";
			config.API_SERVER_ORIENTDB_READ_PASS = "EnterR@graph!@#";
			config.API_SERVER_ORIENTDB_READ_URL = "remote:10.1.5.126:2424/web";
			config.SERVER_ELASTICSEARCH_READ_HOST = "10.1.6.151";
			config.SERVER_ELASTICSEARCH_WRITE_HOST = "10.1.6.151";
//			config.SERVER_RABBITMQ_URL = "amqp://tgdd:Tgdd2012@192.168.2.55:5672";
			config.SERVER_RABBITMQ_URL = "amqp://beta:beta@10.1.6.139:5672";
			config.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";
			config.ELASTICSEARCH_PRODUCT_GALLERY_INDEX = "ms_gallery";
			config.ELASTICSEARCH_PRODUCT_OLD_INDEX = "ms_productold";
			config.REDIS_CONNECTION_STRING = "10.1.6.95:6379,10.1.6.96:6379,10.1.6.97:6379,10.1.6.98:6379,10.1.6.99:6379,allowAdmin=true,defaultDatabase=0,abortConnect=false,syncTimeout=3000";
//			config.DB_URL = "10.1.12.138:1521/pdbbeta";
//			config.DB_USER = "tgdd_news";
//			config.DB_PASS = "44662288";

			config.IGNITE_URL = "http://10.1.5.68/KVProject/kvp";
			config.IGNITE_APP = "web";
			config.IGNITE_USER = "web";
			config.IGNITE_PASS = "123456";

			config.CRM_SERVIVES_URL = "http://betacrmservices.thegioididong.com/CRMTGDD/CRMTGDDService.asmx";
			config.REDIS_HOST_NLU = "172.16.3.123";

			config.BHX_SERVICE_URL = "http://testbhxservices.bachhoaxanh.com/Web/BHXOnlineSVC.asmx";
			config.BHX_SERICE_TOKEN = "werwerewrw32423!@4#123";

			config.RCM_ES_HOST = "10.1.6.151";
			config.RCM_ES_PORT = 9200;
			config.RCM_ES_PROTOCOL = "http";
			config.RCM_PIO_ACCESS_KEY = "bbjfxxi5tf1AYn_EpkNrOC4EvS-NQ2h6PSZy3IrVv2KXqFr1oVPH57gilQ4Em9I5";
			config.RCM_PIO_APP_URL = "http://10.1.6.70:7070";
			config.RCM_PIO_ENGINE_URL = "http://10.1.6.70:8000";

			config.REDIS_CHAT_DIDX = "10.1.6.107";
			config.REDIS_CHAT_DIDX_NUMBERDB = 11;

		} else if (config.DATACENTER == 4) {
			// removed

		} else if (config.DATACENTER == 2) {
			// removed
		} else if (config.DATACENTER == 6) {
			config.API_SERVER_ORIENTDB_READ_USER = "onlyread";
			config.API_SERVER_ORIENTDB_READ_PASS = "EnterR@graph!@#";
			config.API_SERVER_ORIENTDB_READ_URL = "remote:10.1.5.126:2424/web";
			config.SERVER_ELASTICSEARCH_READ_HOST = "10.1.6.151";
			config.SERVER_ELASTICSEARCH_WRITE_HOST = "10.1.6.151";
			config.SERVER_RABBITMQ_URL = "amqp://beta:beta@10.1.6.139:5672";
			config.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";
			config.ELASTICSEARCH_PRODUCT_GALLERY_INDEX = "ms_gallery";
			config.ELASTICSEARCH_PRODUCT_OLD_INDEX = "ms_productold";
			config.IGNITE_URL = "http://10.1.5.68/KVProject/kvp";
			config.IGNITE_APP = "web";
			config.IGNITE_USER = "web";
			config.IGNITE_PASS = "123456";
			config.CRM_SERVIVES_URL = "http://betacrmservices.thegioididong.com/CRMTGDD/CRMTGDDService.asmx";
			config.REDIS_HOST_NLU = "172.16.3.123";
			config.BHX_SERVICE_URL = "http://testbhxservices.bachhoaxanh.com/Web/BHXOnlineSVC.asmx";
			config.BHX_SERICE_TOKEN = "werwerewrw32423!@4#123";
			config.RCM_ES_HOST = "10.1.6.151";
			config.RCM_ES_PORT = 9200;
			config.RCM_ES_PROTOCOL = "http";
			config.RCM_PIO_ACCESS_KEY = "bbjfxxi5tf1AYn_EpkNrOC4EvS-NQ2h6PSZy3IrVv2KXqFr1oVPH57gilQ4Em9I5";
			config.RCM_PIO_APP_URL = "http://10.1.6.70:7070";
			config.RCM_PIO_ENGINE_URL = "http://10.1.6.70:8000";
			config.REDIS_CHAT_DIDX = "10.1.6.107";
			config.REDIS_CHAT_DIDX_NUMBERDB = 11;
		} else if (config.DATACENTER == DataCenterType.Staging) {
			// removed
		}
		return config;
	}

	public static synchronized ClientConfig GetOnlineClientConfigCU() {

		ClientConfig config = new ClientConfig();
		config.CreatedDate = System.currentTimeMillis();
		config.DATACENTER = DataCenterHelper.GetDataCenter(myConfig.GetDataCenter());
		if (config.DATACENTER == 3) {
			config.API_SERVER_ORIENTDB_READ_USER = "admin";
			config.API_SERVER_ORIENTDB_READ_PASS = "EnterW@graph!@#";
			config.API_SERVER_ORIENTDB_READ_URL = "remote:10.1.5.126:2424/web";
			config.SERVER_ELASTICSEARCH_READ_HOST = "10.1.6.151";
			config.SERVER_RABBITMQ_URL = "amqp://tgdd:Tgdd2012@192.168.2.55:5672";
			config.SERVER_RABBITMQ_URL = "amqp://beta:beta@10.1.6.139:5672";
			config.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";
			config.ELASTICSEARCH_PRODUCT_GALLERY_INDEX = "ms_gallery";

		} else {
			config.API_SERVER_ORIENTDB_READ_USER = myConfig.getAPI_SERVER_ORIENTDB_READ_USER() + "";
			if (!config.API_SERVER_ORIENTDB_READ_USER.equals("none")) {

				// config.API_SERVER_ORIENTDB_READ_USER =
				// myConfig.getAPI_SERVER_ORIENTDB_READ_USER();
				// config.API_SERVER_ORIENTDB_READ_PASS =
				// myConfig.getAPI_SERVER_ORIENTDB_READ_PASS();
				// config.API_SERVER_ORIENTDB_READ_URL =
				// myConfig.getAPI_SERVER_ORIENTDB_READ_URL();
				// config.SERVER_ELASTICSEARCH_READ_HOST =
				// myConfig.getAPI_SERVER_ELASTICSEARCH_READ_HOST();
				// config.SERVER_RABBITMQ_URL = myConfig.getSERVER_RABBITMQ_URL();
				if (config.DATACENTER == 4) {
					config.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";
					config.ELASTICSEARCH_PRODUCT_GALLERY_INDEX = "ms_gallery";
				}
				if (config.DATACENTER == 2) {
					config.ELASTICSEARCH_PRODUCT_INDEX = "msbk_product";
					config.ELASTICSEARCH_PRODUCT_GALLERY_INDEX = "msbk_gallery";
				}
			} else {
				if (config.DATACENTER == 4) {
					// removed
				}
				if (config.DATACENTER == 2) {
					// removed
				}
			}

		}

		return config;
	}

}
