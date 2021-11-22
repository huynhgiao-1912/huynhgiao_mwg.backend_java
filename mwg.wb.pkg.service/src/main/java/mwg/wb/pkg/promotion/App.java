package mwg.wb.pkg.promotion;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.ProductHelper;
import mwg.wb.business.WorkerHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.BhxServiceHelper;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.model.api.ClientConfig;


public class App {

	public static void main(String[] args) throws JsonParseException, IOException {
		// testPromotionBHX();
		// testBhx();
		testPromotionphi();
		// testBillPromotion();
		// testPrice();
	}

	public static void testPrice() throws JsonParseException, IOException {
		var config = WorkerHelper.GetWorkerClientConfig();
		var oread = new ORThreadLocal();
		oread.initRead(config, 0, 1);
		var phelper = new ProductHelper(oread, config);
		var erp = new ErpHelper("http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx",
				"werwerewrw32423!@4#123");
		var bhxServiceHelper = new BhxServiceHelper("http://testservices.bachhoaxanh.com/Web/BHXOnlineSVC.asmx",
				"dsfkjh4uryudhfsljdhsfkljhdsk");
		var mapper = new ObjectMapper();
		var df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		var m = new MessageQueue() {
			{
				Identify = "078895300017|?"; // 0131491001876
				Note = "BHXTEST";
				SiteID = 11;
				Lang = "vi-VN";
				DataCenter = 3;
			}
		};
		var tools = new ObjectTransfer();
		tools.erpHelper = erp;
		tools.mapper = mapper;
		tools.factoryRead = oread;
		tools.clientConfig = config;
		tools.productHelper = phelper;
		tools.bhxServiceHelper = bhxServiceHelper;
		var promo = new Promotion();
		promo.InitObject(tools);
		promo.Refresh(m);
		System.out.print("hello");

		// System.out.
	}

	public static void testPromotionBHX() throws JsonParseException, IOException {

		ClientConfig clientConfig = null;
		/// init config
		clientConfig = WorkerHelper.GetWorkerClientConfig();
//		clientConfig = new ClientConfig();
//		clientConfig.SERVER_ORIENTDB_READ_USER = "admin";
//		clientConfig.SERVER_ORIENTDB_READ_PASS = "EnterW@graph!@#";
//		clientConfig.SERVER_ORIENTDB_WRITE_USER = "admin";
//		clientConfig.SERVER_ORIENTDB_WRITE_PASS = "EnterW@graph!@#";
//		clientConfig.DATACENTER = 3;
//		clientConfig.SERVER_ORIENTDB_WRITE_URL1 = "remote:10.1.5.126:2424/web";
//
//		clientConfig.SERVER_ORIENTDB_READ_URL1 = "remote:10.1.5.126:2424/web";
//
//		clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST = "10.1.6.151";
//		clientConfig.SERVER_ELASTICSEARCH_READ_HOST = "10.1.6.151";
//		clientConfig.SERVER_RABBITMQ_URL = "amqp://beta:beta@10.1.6.139:5672";
//
//		clientConfig.ERP_SERVICES_URL = "http://betaerpsupportservices.thegioididong.com/Web/WSWeb.asmx";
//		clientConfig.ERP_SERVICES_AUTHEN = "ksdfswfrew3ttc!@4#123";
//
//		clientConfig.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";

		var oread = new ORThreadLocal();
		oread.initRead(clientConfig, 0, 1);
		var phelper = new ProductHelper(oread, clientConfig);
		var erp = new ErpHelper("http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx",
				"ksdfswfrew3ttc!@4#123");
		var mapper = new ObjectMapper();
		var df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		var m = new MessageQueue() {
			{
				Identify = "8935012413338|174521";
				Note = "";
				SiteID = 11;
				Lang = "vi-VN";
				DataCenter = 3;
			}
		};
		var bhxServiceHelper = new BhxServiceHelper("http://testservices.bachhoaxanh.com/Web/BHXOnlineSVC.asmx",
				"dsfkjh4uryudhfsljdhsfkljhdsk");
//		var m = new MessageQueue() {
//			{
//				Identify = "X13|0|3041094000718|2";
//				Note = "";
//				SiteID = 2;
//				Lang = "vi-VN";
//				DataCenter = 3;
//			}
//		};
		var objTransfer = new ObjectTransfer();
		objTransfer.erpHelper = erp;
		objTransfer.mapper = mapper;
		objTransfer.factoryRead = oread;
		objTransfer.clientConfig = clientConfig;
		objTransfer.productHelper = phelper;
		objTransfer.bhxServiceHelper = bhxServiceHelper;

		var promo = new PromotionBHXV1();
		// var promo = new PriceV1();
		promo.InitObject(objTransfer);
		promo.Refresh(m);
		System.out.print("hello");

	}

	public static void testBhx() throws JsonParseException, IOException {
		var config = WorkerHelper.GetWorkerClientConfig();
		var oread = new ORThreadLocal();
		oread.initRead(config, 0, 1);
		var phelper = new ProductHelper(oread, config);
		var erp = new ErpHelper("", "");
		var bhxServiceHelper = new BhxServiceHelper();
		var mapper = new ObjectMapper();
		var df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	
		var m = new MessageQueue() {
			{
				Identify = "X13|0|1053057000006|?"; // 0131491001876
				Note = "";
				SiteID = 11;
				Lang = "vi-VN";
				DataCenter = 3;
				Action = DataAction.Update;
			}
		};
		var tools = new ObjectTransfer();
		tools.erpHelper = erp;
		tools.mapper = mapper;
		tools.factoryRead = oread;
		tools.factoryWrite = oread;
		tools.clientConfig = config;
		tools.productHelper = phelper;
		tools.bhxServiceHelper = bhxServiceHelper;
		

		//var promo = new PriceBHX();
		//promo.InitObject(tools);
		//promo.Refresh(m);
		System.out.print("hello");
	}

	public static void testPromotionphi() throws JsonParseException, IOException {

		ClientConfig clientConfig = null;
		/// init config
		// clientConfig = WorkerHelper.GetWorkerClientConfig();
		clientConfig = new ClientConfig();
		clientConfig.SERVER_ORIENTDB_READ_USER = "admin";
		clientConfig.SERVER_ORIENTDB_READ_PASS = "EnterW@graph!@#";
		clientConfig.SERVER_ORIENTDB_WRITE_USER = "admin";
		clientConfig.SERVER_ORIENTDB_WRITE_PASS = "EnterW@graph!@#";
		clientConfig.DATACENTER = 3;
		clientConfig.SERVER_ORIENTDB_WRITE_URL1 = "remote:10.1.5.126:2424/web";

		clientConfig.SERVER_ORIENTDB_READ_URL1 = "remote:10.1.5.126:2424/web";

		clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST = "10.1.6.151";
		clientConfig.SERVER_ELASTICSEARCH_READ_HOST = "10.1.6.151";
		clientConfig.SERVER_RABBITMQ_URL = "amqp://beta:beta@10.1.6.139:5672";

		clientConfig.ERP_SERVICES_URL = "http://betaerpsupportservices.thegioididong.com/Web/WSWeb.asmx";
		clientConfig.ERP_SERVICES_AUTHEN = "ksdfswfrew3ttc!@4#123";

		clientConfig.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";

//		clientConfig.SERVER_ORIENTDB_READ_USER = "onlyread";
//		clientConfig.SERVER_ORIENTDB_READ_PASS = "EnterR@graph!@#";
//		clientConfig.SERVER_ORIENTDB_WRITE_USER = "onlyread";
//		clientConfig.SERVER_ORIENTDB_WRITE_PASS = "EnterR@graph!@#";
//		clientConfig.DATACENTER = 2;
//		clientConfig.SERVER_ORIENTDB_WRITE_URL1 = "remote:172.16.3.71:2424/web";
//
//		clientConfig.SERVER_ORIENTDB_READ_URL1 = "remote:172.16.3.71:2424/web";
//
//		clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST = "172.16.3.23";
//		clientConfig.SERVER_ELASTICSEARCH_READ_HOST = "172.16.3.23";
//		clientConfig.SERVER_RABBITMQ_URL = "amqp://tgdd:Tgdd2012@192.168.2.55:5672";
//
//		clientConfig.ERP_SERVICES_URL = "http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx";
//		clientConfig.ERP_SERVICES_AUTHEN = "werwerewrw32423!@4#123";
//
//		clientConfig.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";

		var oread = new ORThreadLocal();
		oread.initRead(clientConfig, 0, 10);
		var phelper = new ProductHelper(oread, clientConfig);
		var erp = new ErpHelper(clientConfig.ERP_SERVICES_URL, clientConfig.ERP_SERVICES_AUTHEN);
		var mapper = new ObjectMapper();
		var df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		var listProduct = new HashMap<String, String>();

		// listProduct.put("0131491000392", "74110");
		listProduct.put("4843799000185", "199117");
//		listProduct.put("3041094000718", "201363");
//		listProduct.put("3051097000084", "88627");
//		listProduct.put("3051097000563", "156181");
//		listProduct.put("3041094000754", "203369");
//		listProduct.put("3041094000679", "200429");
//		listProduct.put("0160871000373", "201290");
//		listProduct.put("0131491001872", "204651");
//		listProduct.put("0131491001873", "204651");
//		listProduct.put("0232391000943", "199541");
//		listProduct.put("0232391001048", "199497");
//		listProduct.put("0233999000055", "200902");
//		listProduct.put("1274059000084", "203908");
//		listProduct.put("0233999000554", "201620");
//		listProduct.put("1274059001193", "209960");

//		listProduct.put("0220042001019", "210081");
//		listProduct.put("3050911000372", "201501");
//		listProduct.put("0131491001811", "210441");
//		listProduct.put("0131491001810", "210441");

		var objTransfer = new ObjectTransfer();
		objTransfer.erpHelper = erp;
		objTransfer.mapper = mapper;
		objTransfer.factoryRead = oread;
		objTransfer.clientConfig = clientConfig;
		objTransfer.productHelper = phelper;

		var promo = new Promotion();
		promo.InitObject(objTransfer);
		//var price = new PriceV1();
		//price.InitObject(objTransfer);

		for (String i : listProduct.keySet()) {
			System.out.println("*code: " + i + " - product: " + listProduct.get(i));

			var proMe = new MessageQueue() {
				{
					Identify = i + "|" + listProduct.get(i);
					Note = "";
					SiteID = 2;
					Lang = "vi-VN";
					DataCenter = 3;
				}
			};
			
			promo.Refresh(proMe);
			//price.Refresh(priceMe);

		}

		System.out.print("Compeleted!");

	}

	public static void testPromotionAn() throws JsonParseException, IOException {

		ClientConfig clientConfig = null;
		/// init config
		clientConfig = WorkerHelper.GetWorkerClientConfig();
//		clientConfig = new ClientConfig();
//		clientConfig.SERVER_ORIENTDB_READ_USER = "admin";
//		clientConfig.SERVER_ORIENTDB_READ_PASS = "EnterW@graph!@#";
		clientConfig.SERVER_ORIENTDB_WRITE_USER = "admin";
		clientConfig.SERVER_ORIENTDB_WRITE_PASS = "EnterW@graph!@#";
//		clientConfig.DATACENTER = 3;
		clientConfig.SERVER_ORIENTDB_WRITE_URL1 = "remote:10.1.5.126:2424/web";
//
//		clientConfig.SERVER_ORIENTDB_READ_URL1 = "remote:10.1.5.126:2424/web";
//
//		clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST = "10.1.6.151";
//		clientConfig.SERVER_ELASTICSEARCH_READ_HOST = "10.1.6.151";
//		clientConfig.SERVER_RABBITMQ_URL = "amqp://beta:beta@10.1.6.139:5672";
//
//		clientConfig.ERP_SERVICES_URL = "http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx";
//		clientConfig.ERP_SERVICES_AUTHEN = "werwerewrw32423!@4#123";
//
//		clientConfig.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";

		var oread = new ORThreadLocal();
		oread.initRead(clientConfig, 0, 1);
		var phelper = new ProductHelper(oread, clientConfig);
		var erp = new ErpHelper("http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx",
				"werwerewrw32423!@4#123");
		var mapper = new ObjectMapper();
		var df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		var m = new MessageQueue() {
			{
				Identify = "0131491001817|209535";
				Note = "";
				SiteID = 1;
				Lang = "vi-VN";
				DataCenter = 3;
			}
		};

//		var m = new MessageQueue() {
//			{
//				Identify = "X13|0|3041094000718|2";
//				Note = "";
//				SiteID = 2;
//				Lang = "vi-VN";
//				DataCenter = 3;
//			}
//		};
		var objTransfer = new ObjectTransfer();
		objTransfer.erpHelper = erp;
		objTransfer.mapper = mapper;
		objTransfer.factoryRead = oread;
		objTransfer.clientConfig = clientConfig;
		objTransfer.productHelper = phelper;

		var promo = new Promotion();
		// var promo = new PriceV1();
		promo.InitObject(objTransfer);
		promo.Refresh(m);
		System.out.print("hello");

	}

	public static void testBillPromotion() throws JsonParseException, IOException {
		var config = WorkerHelper.GetWorkerClientConfig();
		var oread = new ORThreadLocal();
		oread.initRead(config, 0, 1);
		var phelper = new ProductHelper(oread, config);
		var erp = new ErpHelper("http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx",
				"werwerewrw32423!@4#123");
		var bhxServiceHelper = new BhxServiceHelper("http://testservices.bachhoaxanh.com/Web/BHXOnlineSVC.asmx",
				"dsfkjh4uryudhfsljdhsfkljhdsk");
		var mapper = new ObjectMapper();
		var df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		var m = new MessageQueue() {
			{
				Identify = "101477|?"; // 0131491001876
				Note = "BHXTEST";
				SiteID = 11;
				Lang = "vi-VN";
				DataCenter = 3;
			}
		};
		var tools = new ObjectTransfer();
		tools.erpHelper = erp;
		tools.mapper = mapper;
		tools.factoryRead = oread;
		tools.clientConfig = config;
		tools.productHelper = phelper;
		tools.bhxServiceHelper = bhxServiceHelper;
		var promo = new PromotionBillBHXV1();
		promo.InitObject(tools);
		promo.Refresh(m);
		System.out.print("hello");

		// System.out.
	}

}
