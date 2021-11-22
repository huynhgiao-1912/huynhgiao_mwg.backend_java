package mwg.wb.pkg.price;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.WorkerHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CrmServiceHelper;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.pkg.price.helper.bhx.StatusBHX;
import mwg.wb.pkg.price.helper.dmx.StatusDMX;

public class App {

	public static void main(String[] args) throws Throwable {

		testPricePhi();

	}

	public static void testPricePhi() throws Throwable {
		System.out.println("Welcome test price Phi....");
		ObjectTransfer objTransfer = new ObjectTransfer();
		ORThreadLocal factoryRead = null;
		try {
			factoryRead = new ORThreadLocal();
			factoryRead.IsWorker = true;
		} catch (IOException e) {

			e.printStackTrace();
		}
		ClientConfig clientConfig = null;
		clientConfig = new ClientConfig();

		
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

//		var erp = new ErpHelper("http://betaerpsupportservices.thegioididong.com/Web/WSWeb.asmx",
//				"ksdfswfrew3ttc!@4#123");

		clientConfig.SERVER_ORIENTDB_READ_USER = "onlyread";
		clientConfig.SERVER_ORIENTDB_READ_PASS = "EnterR@graph!@#";
		clientConfig.SERVER_ORIENTDB_WRITE_USER = "onlyread";
		clientConfig.SERVER_ORIENTDB_WRITE_PASS = "EnterR@graph!@#";
		clientConfig.DATACENTER = 2;
		clientConfig.SERVER_ORIENTDB_WRITE_URL1 = "remote:172.16.3.71:2424/web";

		clientConfig.SERVER_ORIENTDB_READ_URL1 = "remote:172.16.3.71:2424/web";

		clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST = "172.16.3.23";
		clientConfig.SERVER_ELASTICSEARCH_READ_HOST = "172.16.3.23";
		clientConfig.SERVER_RABBITMQ_URL = "amqp://tgdd:Tgdd2012@192.168.2.55:5672";

		clientConfig.ERP_SERVICES_URL = "http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx";
		clientConfig.ERP_SERVICES_AUTHEN = "werwerewrw32423!@4#123";

		clientConfig.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";

		factoryRead.initRead(clientConfig, 0, 10);
		var priceHelper = new PriceHelper(factoryRead, clientConfig);
		var mapper = new ObjectMapper();
		var df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		var crm = new CrmServiceHelper();

		var erp = new ErpHelper("http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx",
			"werwerewrw32423!@4#123");

		objTransfer.clientConfig = clientConfig;
		objTransfer.factoryRead = factoryRead;
		objTransfer.priceHelper = priceHelper;

		ProductHelper phelp = new ProductHelper(factoryRead, clientConfig);

		objTransfer.mapper = mapper;
		objTransfer.productHelper = phelp;
		objTransfer.erpHelper = erp;
		objTransfer.crmHelper = crm;

		var listProduct = new HashMap<String, String>();

		listProduct.put("0131491002300", "241291");
//		listProduct.put("3051098000349", "153850");
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
//
//		listProduct.put("0220042001019", "210081");
//		listProduct.put("3050911000372", "201501");
//		listProduct.put("0131491001811", "210441");
//		listProduct.put("0131491001810", "210441");
		var status = new StatusV1();
		//var status = new StatusDMX();
		status.InitObject(objTransfer);

		for (String i : listProduct.keySet()) {
			System.out.println("*code: " + i + " - product: " + listProduct.get(i));

			var m = new MessageQueue() {
				{
					Identify = listProduct.get(i);
					Note = "";
					SiteID = 1;
					Lang = "vi-VN";
					DataCenter = 2;
				}
			};
			status.Refresh(m);

		}

		System.out.print("ok");
	}

}