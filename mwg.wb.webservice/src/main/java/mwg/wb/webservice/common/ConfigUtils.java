package mwg.wb.webservice.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.Properties;

import mwg.wb.business.DataCenterHelper;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.webservice.config.CentralConfig;

public final class ConfigUtils {
	private static CentralConfig myConfig;
	// private static ClientConfig config;

	public static void setMyConfig(CentralConfig myConfig) {
		ConfigUtils.myConfig = myConfig;
	}

	private ConfigUtils() {

	}

	public static synchronized ClientConfig GetOnlineClientConfig() {

		ClientConfig config = new ClientConfig();
		config.CreatedDate = System.currentTimeMillis();

		config.DATACENTER = DataCenterHelper.GetDataCenter(myConfig.getDataCenter());
		if (config.DATACENTER == 3) {// beta
			config.DB_CONNECTIONSTRING = "jdbc:oracle:thin:/@BETA_TGDD_NEWS";
			config.DB_URL = "jdbc:oracle:thin:tgdd_news/44662288@10.1.12.138:1521:pdbbeta";
			config.DB_USER = "tgdd_news";
			config.DB_PASS = "44662288";
			config.tns_admin = "/home/thanhphi/DBwallet/admin";
			config.wallet_location = "/home/thanhphi/DBwallet/wallets";

		} else if (config.DATACENTER == 6) {
			config.DB_CONNECTIONSTRING = "jdbc:oracle:thin:/@BETA_TGDD_NEWS";
			config.DB_URL = "jdbc:oracle:thin:tgdd_news/44662288@10.1.12.138:1521:pdbbeta";
			config.DB_USER = "tgdd_news";
			config.DB_PASS = "44662288";
			config.tns_admin = "E:\\MWG\\BANGOC\\mwg.backend-java\\DBwallet\\admin";
			config.wallet_location = "E:\\MWG\\BANGOC\\mwg.backend-java\\DBwallet\\wallets";
			try {
				var abc = Paths.get("../DBwallet\\admin").toUri().toURL();
//				config.tns_admin = Paths.get("../DBwallet\\admin").toUri().toURL().getPath();
//				config.wallet_location = Paths.get("../DBwallet\\wallets").toUri().toURL().getPath();
				var ccc =1;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} else {
			String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
			String appConfigPath = rootPath + "bootstrap.properties";
			
			Properties appProps = new Properties();
			try {
				appProps.load(new FileInputStream(appConfigPath));
//				config.DB_URL = myConfig.getCONNECTION_STRING();
//				config.DB_USER = myConfig.getDB_USER();
//				config.DB_PASS = myConfig.getDB_PASS();
				config.DB_URL  = appProps.getProperty("dbip");
				config.DB_USER = appProps.getProperty("dbuser");
				config.DB_PASS = appProps.getProperty("dbpass");

				// config wallets
				config.DB_CONNECTIONSTRING = appProps.getProperty("dbconnectionstring");
				config.tns_admin =  appProps.getProperty("dbtnsname");
				config.wallet_location =  appProps.getProperty("dbwalletlocation");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}

		return config;
	}

}
