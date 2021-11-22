package mwg.wb.cdc;

import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.GraphDBConstant;
import mwg.wb.common.Logs;

public class Main {

	public static void server() {

	}

	public static ORThreadLocal factoryWrite = null;
	public static ORThreadLocal factoryRead = null;

	public static void main(String[] args) {

		// secret
//		try {
//			OracleClient dbclient = null;
//			
//			Connection condb = null;
//			dbclient = new OracleClient("jdbc:oracle:thin:tgdd_news/44662288@10.1.12.138:1521:pdbbeta",
//					"tgdd_news", "44662288");
//			condb = dbclient.getConnection();
//			MessageQueue messageRepush = new MessageQueue();
//			
//			@SuppressWarnings("static-access")
//			List<SqlInfo> result=new SysnData("", "").BuildSqlList("news|newsid|1174033", "news", "newsid", "1174033",
//					"", DataAction.Add, condb, "LOG|1174033", true, messageRepush);
//			
//		} catch (Exception e) {
//			// TODO: handle exception
//		}

		// end

		// factoryRead.initRead(1 ,0);
		// factoryWrite.initWrite(1, 0);
		
		// khởi tạo constant của table vs edge
		try {
			GraphDBConstant.initTableEdgesInfo();
		} catch (Exception e) {
			Logs.LogException("errorInitEdgeList", ""+e.getStackTrace());
			e.printStackTrace();
		}
		System.out.println("Let go...");
		try {
			factoryWrite = new ORThreadLocal();
			factoryRead = new ORThreadLocal();
		
			//new MyThreadSysData("datastock", "DGRAPH_SYNC_MESSAGE_SELSTOCK");
			// new MyThreadSysData("datastockbhx", "DGRAPH_SYNC_SELSTOCKBHX");
		
		  new MyThreadSysData("data", "TGDD_NEWS.DGRAPH_SYNC_MESSAGE_SEL","","");
		  new MyThreadSysData("data-pm_currentinstock", "TGDD_NEWS.DGRAPH_SYNC_MESSAGE_SEL","pm_currentinstock","");
		   new MyThreadSysData("data-pm_saleorder_locking", "TGDD_NEWS.DGRAPH_SYNC_MESSAGE_SEL","pm_saleorder_locking","pm_saleorder_locking");
		    new MyThreadSysData("data-crm_productlocking", "TGDD_NEWS.DGRAPH_SYNC_MESSAGE_SEL","crm_productlocking","crm_productlocking");
		 		    new MyThreadSysData("data-news", "TGDD_NEWS.DGRAPH_SYNC_MESSAGE_SEL","news","news");
		        new MyThreadSysData("data-pm_store", "TGDD_NEWS.DGRAPH_SYNC_MESSAGE_SEL","pm_store","pm_store");
		            new MyThreadSysData("data-accessory_product_tmp", "TGDD_NEWS.DGRAPH_SYNC_MESSAGE_SEL","accessory_product_tmp","accessory_product_tmp");
		              new MyThreadSysData("data-gen_store_ward_distance", "TGDD_NEWS.DGRAPH_SYNC_MESSAGE_SEL","gen_store_ward_distance","gen_store_ward_distance");

 		  new MyThreadSysData("dataprice", "TGDD_NEWS.DGRAPH_SYNC_MESSAGE_PRICE_SEL","","");

			new MyThreadSysData("data-reinit", "TGDD_NEWS.DGRAPH_SYNC_REPUSH_SEL","","");

			new MyThreadInit();// repush via api
		  new MyThreadRepushall();
		  new MyThreadReNotify();
			new MyThreadInitData();// repush db

//			 new MyThreadInitData();
			new MyThreadLogs();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				System.out.println("Main thread Interrupted");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		

	}

}
