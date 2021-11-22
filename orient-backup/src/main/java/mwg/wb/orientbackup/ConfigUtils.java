package mwg.wb.orientbackup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.common.DataCenterType;
import mwg.wb.common.GConfig;
import mwg.wb.model.api.ClientConfig;

public final class ConfigUtils {

	public static String GetOnlineClientConfigStr(int dataCenter) {
		ObjectMapper mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		var configc = GetOnlineClientConfig(dataCenter);
		try {
			return mapper.writeValueAsString(configc);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "_";
	}

	public static synchronized ClientConfig GetOnlineClientConfig(int dataCenter) {

		ClientConfig config = new ClientConfig();
		config.CreatedDate = System.currentTimeMillis();
		config.DATACENTER = dataCenter;
		if (config.DATACENTER == 3) {
			config.API_SERVER_ORIENTDB_READ_USER = "admin";
			config.API_SERVER_ORIENTDB_READ_PASS = "EnterW@graph!@#";
			config.API_SERVER_ORIENTDB_READ_URL = "remote:10.1.5.126:2424/web";
			config.SERVER_ELASTICSEARCH_READ_HOST = "10.1.6.151";
			config.SERVER_RABBITMQ_URL = "amqp://tgdd:Tgdd2012@192.168.2.55:5672";
			config.SERVER_RABBITMQ_URL = "amqp://beta:beta@10.1.6.139:5672";
			config.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";
			config.ELASTICSEARCH_PRODUCT_GALLERY_INDEX = "ms_gallery";

		} else if (config.DATACENTER == 4) {
			config.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";
			config.ELASTICSEARCH_PRODUCT_GALLERY_INDEX = "ms_gallery";
			config.API_SERVER_ORIENTDB_READ_USER = "onlyread";
			config.API_SERVER_ORIENTDB_READ_PASS = "EnterR@graph!@#";
			config.API_SERVER_ORIENTDB_READ_URL = "remote:172.16.5.74,172.16.5.75,172.16.5.76/web";
			config.SERVER_ELASTICSEARCH_READ_HOST = "172.16.3.120";
			config.SERVER_RABBITMQ_URL = "amqp://tgdd:Tgdd2012@192.168.2.55:5672";
		} else if (config.DATACENTER == 2) {
			// config.ELASTICSEARCH_PRODUCT_INDEX = "msbk_product";
			// config.ELASTICSEARCH_PRODUCT_GALLERY_INDEX = "msbk_gallery";

			config.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";
			config.ELASTICSEARCH_PRODUCT_GALLERY_INDEX = "ms_gallery";
			config.API_SERVER_ORIENTDB_READ_USER = "onlyread";
			config.API_SERVER_ORIENTDB_READ_PASS = "EnterR@graph!@#";
			config.API_SERVER_ORIENTDB_READ_URL = "remote:172.16.3.74,172.16.3.75,172.16.3.76/web";
			config.SERVER_ELASTICSEARCH_READ_HOST = "172.16.3.120";
			config.SERVER_RABBITMQ_URL = "amqp://tgdd:Tgdd2012@192.168.2.55:5672";
		} else if (config.DATACENTER == 6) {
			config.API_SERVER_ORIENTDB_READ_USER = "admin";
			config.API_SERVER_ORIENTDB_READ_PASS = "EnterW@graph!@#";
			config.API_SERVER_ORIENTDB_READ_URL = "remote:10.1.5.127:2424/web";
			config.SERVER_ELASTICSEARCH_READ_HOST = "10.1.6.151";
			config.SERVER_RABBITMQ_URL = "amqp://tgdd:Tgdd2012@192.168.2.55:5672";
			config.SERVER_RABBITMQ_URL = "amqp://beta:beta@10.1.6.139:5672";
			config.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";
			config.ELASTICSEARCH_PRODUCT_GALLERY_INDEX = "ms_gallery";
		}

		else if (config.DATACENTER == DataCenterType.Staging) {
			config.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";
			config.ELASTICSEARCH_PRODUCT_GALLERY_INDEX = "ms_gallery";
			config.API_SERVER_ORIENTDB_READ_USER = "onlyread";
			config.API_SERVER_ORIENTDB_READ_PASS = "EnterR@graph!@#";
			config.API_SERVER_ORIENTDB_READ_URL = "remote:172.16.3.74,172.16.3.75,172.16.3.76/web";
			config.SERVER_ELASTICSEARCH_READ_HOST = "172.16.3.120";
			config.SERVER_RABBITMQ_URL = "amqp://tgdd:Tgdd2012@192.168.2.55:5672";
		}
		return config;
	}
}
