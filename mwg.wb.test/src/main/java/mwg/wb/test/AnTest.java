package mwg.wb.test;

import mwg.wb.business.ProductUrlHelper;
import mwg.wb.business.WorkerHelper;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.MessageQueue;

public class AnTest {
	public static void main(String[] args) throws Exception {
		String x = "Quạt";
		System.out.println(ProductUrlHelper.GenSEOUrl(x));
	}

	public static void main2(String[] args) throws Exception {
		var config = WorkerHelper.GetWorkerClientConfig();
		var mapper = DidxHelper.generateNonDefaultJsonMapper(GConfig.DateFormatString);
		String msgStr = "{\"ID\":142819522,\"Action\":\"Update\",\"PushType\":0,\"IsCreateEdge\":true,\"Identify\":null," +
				"\"Data\":null,\"Source\":\"product_detail\",\"RefIdentify\":\"242921\"," +
				"\"Note\":\"DIDX_TOP DIDX_LOG|242921\",\"SiteID\":0,\"IsCheckHash\":false,\"CategoryID\":0," +
				"\"BrandID\":0,\"ProvinceID\":0,\"DistrictID\":0,\"Storeid\":0,\"Type\":0,\"CachedType\":0," +
				"\"DataCenter\":2,\"RepushCount\":0,\"Hash\":5225707,\"CompanyID\":0,\"Lang\":null,\"Term\":null," +
				"\"CreatedDate\":\"2021-06-22 21:29:36\",\"Version\":0,\"SqlList\":[{\"Type\":0,\"Order\":0," +
				"\"Hash\":null,\"Sql\":\"update product_detail SET `value`=:value,`propertyid`=:propertyid," +
				"`productid`=:productid,`languageid`=:languageid,`isfeatureprop`=:isfeatureprop,`recordid`=:recordid," +
				"`newmodelvalue`=:newmodelvalue Upsert where recordid=5225707\",\"msg\":null," +
				"\"tablename\":\"product_detail\",\"tablekey\":\"5225707\",\"Params\":{\"isfeatureprop\":0," +
				"\"recordid\":5225707,\"productid\":242921,\"languageid\":\"vi-VN\",\"value\":\"193889\"," +
				"\"propertyid\":8901}},{\"Type\":6,\"Order\":0,\"Hash\":null,\"Sql\":null,\"msg\":null," +
				"\"tablename\":null,\"tablekey\":null,\"Params\":{\"edge\":\"e_product_detail\"," +
				"\"tocol\":\"recordid\",\"totbl\":\"product_detail\",\"fromcol\":\"productid\",\"fromtbl\":\"product\"," +
				"\"from\":242921,\"to\":\"5225707\"}}],\"ClassName\":\"mwg.wb.pkg.upsert\",\"RepushClassName\":null," +
				"\"RepushQueue\":null,\"Processid\":null}";
		var msg = mapper.readValue(msgStr, MessageQueue.class);
		QueueHelper.Current(config.SERVER_RABBITMQ_URL).PushPriority("gr.test.sql.sysdata", msg,
				10);
		System.out.println("hello");

	}

	public static void mainStr(String[] args) throws Throwable {
		String a = "Đang cập nhật";
		String b = "Đang cập nhật";
		System.out.println(a.equals(b));
		String c = "Thế giới di động";
		String d = "Thế giới di động";
		System.out.println(c.equals(d));
	}
}
