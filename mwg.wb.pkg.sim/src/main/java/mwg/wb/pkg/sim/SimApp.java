package mwg.wb.pkg.sim;

import java.io.IOException;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.WorkerHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;

public class SimApp {

	public static void main(String[] args) {
//		System.out.println(Stream.of(SimPackageBO.class.getFields()).map(f -> f.getName().toLowerCase())
//				.collect(Collectors.joining(", ")));
		try {
			testRefresh();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void testRefresh() throws JsonParseException, IOException {
		var config = WorkerHelper.GetWorkerClientConfig();

		// dl live
		config.SERVER_RABBITMQ_URL = "amqp://tgdd:Tgdd2012@192.168.2.55:5672";
		config.SERVER_ORIENTDB_READ_URL1 = "remote:172.16.3.71:2424/web";
		config.ERP_SERVICES_URL = "http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx";
		config.ERP_SERVICES_AUTHEN = "werwerewrw32423!@4#123";
		config.CRM_SERVIVES_URL = "http://crm-services.thegioididong.com/NEW-CRMTGDD/CRMTGDDService.asmx";
		config.SERVER_ELASTICSEARCH_WRITE_HOST = "172.16.3.23";
		
		var erp = new ErpHelper(config.ERP_SERVICES_URL, config.ERP_SERVICES_AUTHEN);

		var oread = new ORThreadLocal();
		oread.initRead(config, 0, 2);
		
		var mapper = new ObjectMapper();
		var df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		var objTransfer = new ObjectTransfer();
		objTransfer.clientConfig = config;
//		objTransfer.factoryRead = oread;
		objTransfer.erpHelper = erp;
//		objTransfer.mapper = mapper;
		var s = new Sim();
		s.InitObject(objTransfer);
		
		MessageQueue message = new MessageQueue() {
			{
				Identify = "0128590844";
//				Note = "20201215";
				Action = DataAction.Add;
			}
		};		

		s.Refresh(message);
	}
}