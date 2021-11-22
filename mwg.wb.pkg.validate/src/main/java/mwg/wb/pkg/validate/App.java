package mwg.wb.pkg.validate;

import java.io.IOException;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.core.JsonParseException;
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

public class App {

	public static void main(String[] args) throws JsonParseException, IOException {
	 var s = new Validate();
	 String vl="{\"gTable\":\"pm_currentinstock\",\"gKey\":\"recordid\",\"gValue\":\"1043190\",\"gSelect\":\"quantity,recordid,brandid,inventorystatusid\",\"dbparams\":{\"recordid\":{\"cot\":\"recordid\",\"sqlType\":2,\"Value\":\"1043190\"},\"inventorystatusid\":{\"cot\":\"inventorystatusid\",\"sqlType\":2,\"Value\":\"3\"},\"quantity\":{\"cot\":\"quantity\",\"sqlType\":2,\"Value\":\"0\"},\"brandid\":{\"cot\":\"brandid\",\"sqlType\":2,\"Value\":\"1\"}},\"Note\":null,\"CreatedDate\":null,\"cCot\":\"quantity\",\"cEdge\":null,\"cEdgeProp\":null}";
	
	 var config = WorkerHelper.GetWorkerClientConfig();
	 config.SERVER_ORIENTDB_READ_URL1 = "remote:172.16.3.71:2424/web";
		config.SERVER_ELASTICSEARCH_WRITE_HOST = "10.1.6.151";
		var oread = new ORThreadLocal();
		oread.initRead(config, 0, 2);
		
		var phelper = new ProductHelper(oread, config);

		var erp = new ErpHelper("http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx",
				"werwerewrw32423!@4#123");
//		var erp = new ErpHelper("http://betaerpsupportservices.thegioididong.com/Web/WSWeb.asmx",
//				"ksdfswfrew3ttc!@4#123");
		var mapper = new ObjectMapper();
		var df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		 
		var tools = new ObjectTransfer();
		tools.erpHelper = erp;
		tools.mapper = mapper;
		 
		tools.productHelper = phelper;
		 
		s.InitObject(tools); ;
		s.Refresh(new MessageQueue() {
			{
			  Identify=vl;
				Note = "";
				Action = DataAction.Update;
			}
		});
		 
//		System.out.println(Stream.of(SimPackageBO.class.getFields()).map(f -> f.getName().toLowerCase())
//				.collect(Collectors.joining(", ")));
	}

	void testRefresh() {
//		var s = new Sim();
//		s.InitObject(null, null, null, null, null, new ErpHelper());
//		s.Refresh(new MessageQueue() {
//			{
//				Identify = "0974356951";
//				Note = "";
//				Action = DataAction.Update;
//			}
//		});
	}
}