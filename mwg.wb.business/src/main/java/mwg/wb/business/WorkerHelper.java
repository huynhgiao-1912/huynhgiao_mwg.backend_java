package mwg.wb.business;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.common.FileHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.JsonConfig;
import mwg.wb.model.api.ClientConfig;

public class WorkerHelper {
	private static Map<String, WorkerPackage> PackageList;
	private static ClientConfig config;
	static ObjectMapper mapper = null;

	public static synchronized Map<String, WorkerPackage> GetPackageList() {
		if (PackageList == null) {
			PackageList = new HashMap<String, WorkerPackage>();
			mapper = new ObjectMapper();
			DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
			mapper.setDateFormat(df);
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			String da = "";
			try {
				da = FileHelper.ReadAllText("package.json");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				var ls = Arrays.asList(mapper.readValue(da, WorkerPackage[].class));
				for (WorkerPackage workerPackage : ls) {
					PackageList.put(workerPackage.PackageId, workerPackage);
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return PackageList;

	}

	public static WorkerPackage GetWorkerPackageByID(String id) {
		Map<String, WorkerPackage> ls = GetPackageList();
		if (ls.containsKey(id))
			return ls.get(id);
		return null;
	}

	public static synchronized ClientConfig GetWorkerClientConfig_(int dc) {
		// if (config == null) {
		config = new ClientConfig();
		JsonConfig jsonConfig = null;
		jsonConfig = new JsonConfig("config" + dc + ".json");

		// config.ISWRITE_ES = jsonConfig.getInt("ISWRITE_ES");
		config.IS_NOT_PROCESS_LOG = jsonConfig.getInt("IS_NOT_PROCESS_LOG");
		config.IS_NOT_UPDATE_ES = jsonConfig.getInt("IS_NOT_UPDATE_ES");

		config.DATACENTER = jsonConfig.getInt("DATACENTER");
		config.RUN_VERSION = jsonConfig.getInt("RUN_VERSION");
		config.SERVER_RABBITMQ_URL = jsonConfig.getString("SERVER_RABBITMQ_URL");
		config.SERVER_ELASTICSEARCH_WRITE_HOST = jsonConfig.getString("SERVER_ELASTICSEARCH_WRITE_HOST");
		config.SERVER_ELASTICSEARCH_READ_HOST = jsonConfig.getString("SERVER_ELASTICSEARCH_READ_HOST");
		config.ELASTICSEARCH_PRODUCT_GALLERY_INDEX = jsonConfig.getString("ELASTICSEARCH_PRODUCT_GALLERY_INDEX");
		config.ELASTICSEARCH_PRODUCT_INDEX = jsonConfig.getString("ELASTICSEARCH_PRODUCT_INDEX");
		config.ELASTICSEARCH_PRODUCT_OLD_INDEX = jsonConfig.getString("ELASTICSEARCH_PRODUCT_OLD_INDEX");

		config.ERP_SERVICES_AUTHEN = jsonConfig.getString("ERP_SERVICES_AUTHEN");
		config.ERP_SERVICES_URL = jsonConfig.getString("ERP_SERVICES_URL");
		config.ERP_SERVICES_PROMOTION_URL = jsonConfig.getString("ERP_SERVICES_PROMOTION_URL");

		config.CRM_SERVIVES_URL = jsonConfig.getString("CRM_SERVIVES_URL");
		config.BHX_SERVICE_URL = jsonConfig.getString("BHX_SERVICE_URL");
		config.BHX_SERICE_TOKEN = jsonConfig.getString("BHX_SERICE_TOKEN");
		// DB
		config.DB_URL = jsonConfig.getString("DB_URL");
		config.DB_USER = jsonConfig.getString("DB_USER");
		config.DB_PASS = jsonConfig.getString("DB_PASS");

		config.REDIS_CONNECTION_STRING = jsonConfig.getString("REDIS_CONNECTION_STRING");

		config.IGNITE_APP = jsonConfig.getString("IGNITE_APP");
		config.IGNITE_PASS = jsonConfig.getString("IGNITE_PASS");
		config.IGNITE_URL = jsonConfig.getString("IGNITE_URL");
		config.IGNITE_USER = jsonConfig.getString("IGNITE_USER");

		config.SERVER_ORIENTDB_READ_USER = jsonConfig.getString("SERVER_ORIENTDB_READ_USER");
		config.SERVER_ORIENTDB_READ_PASS = jsonConfig.getString("SERVER_ORIENTDB_READ_PASS");
		config.SERVER_ORIENTDB_READ_URL1 = jsonConfig.getString("SERVER_ORIENTDB_READ_URL1");
		config.SERVER_ORIENTDB_READ_URL2 = jsonConfig.getString("SERVER_ORIENTDB_READ_URL2");
		config.SERVER_ORIENTDB_READ_URL3 = jsonConfig.getString("SERVER_ORIENTDB_READ_URL3");

		config.SERVER_ORIENTDB_WRITE_USER = jsonConfig.getString("SERVER_ORIENTDB_WRITE_USER");
		config.SERVER_ORIENTDB_WRITE_PASS = jsonConfig.getString("SERVER_ORIENTDB_WRITE_PASS");
		config.SERVER_ORIENTDB_WRITE_URL1 = jsonConfig.getString("SERVER_ORIENTDB_WRITE_URL1");
		config.SERVER_ORIENTDB_WRITE_URL2 = jsonConfig.getString("SERVER_ORIENTDB_WRITE_URL2");
		config.SERVER_ORIENTDB_WRITE_URL3 = jsonConfig.getString("SERVER_ORIENTDB_WRITE_URL3");

		// }
		return config;

	}

	public static synchronized ClientConfig GetWorkerClientConfig() {
		if (config == null) {
			config = new ClientConfig();
			JsonConfig jsonConfig = null;
			try {
				jsonConfig = new JsonConfig();
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// config.ISWRITE_ES = jsonConfig.getInt("ISWRITE_ES");
			config.IS_NOT_PROCESS_LOG = jsonConfig.getInt("IS_NOT_PROCESS_LOG");
			config.IS_NOT_UPDATE_ES = jsonConfig.getInt("IS_NOT_UPDATE_ES");

			config.DATACENTER = jsonConfig.getInt("DATACENTER");
			config.RUN_VERSION = jsonConfig.getInt("RUN_VERSION");
			config.SERVER_RABBITMQ_URL = jsonConfig.getString("SERVER_RABBITMQ_URL");
			config.SERVER_ELASTICSEARCH_WRITE_HOST = jsonConfig.getString("SERVER_ELASTICSEARCH_WRITE_HOST");
			config.SERVER_ELASTICSEARCH_READ_HOST = jsonConfig.getString("SERVER_ELASTICSEARCH_READ_HOST");
			config.ELASTICSEARCH_PRODUCT_GALLERY_INDEX = jsonConfig.getString("ELASTICSEARCH_PRODUCT_GALLERY_INDEX");
			config.ELASTICSEARCH_PRODUCT_INDEX = jsonConfig.getString("ELASTICSEARCH_PRODUCT_INDEX");
			config.ELASTICSEARCH_PRODUCT_OLD_INDEX = jsonConfig.getString("ELASTICSEARCH_PRODUCT_OLD_INDEX");

			config.ERP_SERVICES_AUTHEN = jsonConfig.getString("ERP_SERVICES_AUTHEN");
			config.ERP_SERVICES_URL = jsonConfig.getString("ERP_SERVICES_URL");
			config.ERP_SERVICES_PROMOTION_URL = jsonConfig.getString("ERP_SERVICES_PROMOTION_URL");

			config.CRM_SERVIVES_URL = jsonConfig.getString("CRM_SERVIVES_URL");
			config.BHX_SERVICE_URL = jsonConfig.getString("BHX_SERVICE_URL");
			config.BHX_SERICE_TOKEN = jsonConfig.getString("BHX_SERICE_TOKEN");
			// DB
			config.DB_URL = jsonConfig.getString("DB_URL");
			config.DB_USER = jsonConfig.getString("DB_USER");
			config.DB_PASS = jsonConfig.getString("DB_PASS");
			config.DB_CONNECTIONSTRING = jsonConfig.getString("DB_CONNECTIONSTRING");
			config.tns_admin = jsonConfig.getString("tns_admin");
			config.wallet_location = jsonConfig.getString("wallet_location");

			config.REDIS_CONNECTION_STRING = jsonConfig.getString("REDIS_CONNECTION_STRING");

			config.IGNITE_APP = jsonConfig.getString("IGNITE_APP");
			config.IGNITE_PASS = jsonConfig.getString("IGNITE_PASS");
			config.IGNITE_URL = jsonConfig.getString("IGNITE_URL");
			config.IGNITE_USER = jsonConfig.getString("IGNITE_USER");

			config.SERVER_ORIENTDB_READ_USER = jsonConfig.getString("SERVER_ORIENTDB_READ_USER");
			config.SERVER_ORIENTDB_READ_PASS = jsonConfig.getString("SERVER_ORIENTDB_READ_PASS");
			config.SERVER_ORIENTDB_READ_URL1 = jsonConfig.getString("SERVER_ORIENTDB_READ_URL1");
			config.SERVER_ORIENTDB_READ_URL2 = jsonConfig.getString("SERVER_ORIENTDB_READ_URL2");
			config.SERVER_ORIENTDB_READ_URL3 = jsonConfig.getString("SERVER_ORIENTDB_READ_URL3");

			config.SERVER_ORIENTDB_WRITE_USER = jsonConfig.getString("SERVER_ORIENTDB_WRITE_USER");
			config.SERVER_ORIENTDB_WRITE_PASS = jsonConfig.getString("SERVER_ORIENTDB_WRITE_PASS");
			config.SERVER_ORIENTDB_WRITE_URL1 = jsonConfig.getString("SERVER_ORIENTDB_WRITE_URL1");
			config.SERVER_ORIENTDB_WRITE_URL2 = jsonConfig.getString("SERVER_ORIENTDB_WRITE_URL2");
			config.SERVER_ORIENTDB_WRITE_URL3 = jsonConfig.getString("SERVER_ORIENTDB_WRITE_URL3");

		}
		return config;

	}
}
