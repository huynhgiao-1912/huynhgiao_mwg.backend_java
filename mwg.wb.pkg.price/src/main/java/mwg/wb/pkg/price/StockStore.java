package mwg.wb.pkg.price;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.InStockBO;
import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.RefSql;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.SqlInfoType;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.ProductErpPriceBO;

public class StockStore implements Ididx {

	private ProductHelper productHelper = null;

	private ClientConfig clientConfig = null;
	private int DataCenter = 0;

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {

		productHelper = (ProductHelper) objectTransfer.productHelper;

		clientConfig = (ClientConfig) objectTransfer.clientConfig;

	}

	public static Map<String, String> g_listWebStatusId = new HashMap<String, String>();
	public static Map<Long, List<ProductErpPriceBO>> g_listPrice = new HashMap<Long, List<ProductErpPriceBO>>();
	public static Map<String, String> g_listES = new HashMap<String, String>();

	public ResultMessage Refresh(MessageQueue message) {
		DataCenter = message.DataCenter;
		ResultMessage msg = new ResultMessage();

		String strNOTE = message.Note + "";

		msg.Code = ResultCode.Success;
		long productID = Utils.toLong(message.Identify);
		int provinceID = message.ProvinceID;
		//int brandid = message.BrandID;
		
		boolean isLog = false;

		if (strNOTE.contains("DIDX_TOP_LOG") || strNOTE.contains("DIDX_LOG")) {

			isLog = true;
		}
		Logs.Log(isLog, strNOTE, "Stockstore:Refresh" ); 
		//CRMPushUpdateStockStore(isLog, strNOTE, productID);
		if (provinceID > 0 && productID > 0) {
			Logs.Log(isLog, strNOTE, "PushUpdateStockStoreByProvince" ); 
			PushUpdateStockStoreByProvince(isLog, strNOTE, productID, provinceID);

		}else {
			Logs.Log(isLog, strNOTE, "provinceID > 0 && productID > 0" ); 
			
		}
		return msg;
	}

	private boolean PushUpdateStockStoreByProvince(boolean isLog, String strNOTE, long Productid, int provinceID) {
	
		String storelist = productHelper.GetStockStoreByProductID(Productid, provinceID);
		Logs.Log(isLog, strNOTE, "GetStockStoreByProductID "+Productid+" ="  + storelist);
		MessageQueue messageRepush = new MessageQueue();
		messageRepush.SqlList = new ArrayList<SqlInfo>();
		SqlInfo sqlinfo = new SqlInfo();
		String recordValue = Productid + "_" + provinceID;
		sqlinfo.Sql = "update crm_product_store_prov set  productid=:productid, provinceid=:provinceid,storelist=:storelist   upsert where recordid=:recordid  ";
		sqlinfo.Params = new HashMap<String, Object>();

		sqlinfo.Params.put("productid", Productid);
		sqlinfo.Params.put("provinceid", provinceID);
		sqlinfo.Params.put("storelist", storelist);
		sqlinfo.Params.put("recordid", recordValue);

		messageRepush.SqlList.add(sqlinfo);

		messageRepush.Source = "STOCKSTORE";
		String qu = "gr.dc4.sql.stockstore";
		String qu2 = "gr.dc4.sql.stockstore";
		String qubk = "gr.dc2.sql.stockstore";
		String qudev = "gr.beta.sql.stockstore";
		messageRepush.Identify = String.valueOf(Productid);
		messageRepush.Action = DataAction.Update;
		messageRepush.ClassName = "mwg.wb.pkg.upsert.Upsert";
		messageRepush.CreatedDate = Utils.GetCurrentDate();
		messageRepush.Type = 0;
		messageRepush.DataCenter = DataCenter;
		try {
			QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,strNOTE,
					DataCenter);
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			return false;
		}

		Logs.Log(isLog, strNOTE, "PushUpdateStockStore");
		return true;
	}

//	private boolean CRMPushUpdateStockStore(boolean isLog, String strNOTE, long Productid) {
//
//		String storelist = "";
//		synchronized (StockStore.class) {
//			storelist = productHelper.GetAllStockStoreByProductID(Productid);
//		}
//		MessageQueue messageRepush = new MessageQueue();
//		messageRepush.SqlList = new ArrayList<SqlInfo>();
//		SqlInfo sqlinfo = new SqlInfo();
//
//		sqlinfo.Sql = "update crm_product_store set  productid=:productid,storelist=:storelist   upsert where productid=:productid  ";
//		sqlinfo.Params = new HashMap<String, Object>();
//		sqlinfo.Params.put("productid", Productid);
//		sqlinfo.Params.put("storelist", storelist);
//		messageRepush.SqlList.add(sqlinfo);
//
//		messageRepush.Source = "StockStore";
//		String qu = "gr.dc4.sql.stockstore";
//		String qu2 = "gr.dc4.sql.stockstore";
//		String qubk = "gr.dc2.sql.stockstore";
//		String qudev = "gr.beta.sql.stockstore";
//		messageRepush.Identify = String.valueOf(Productid);
//		messageRepush.Action = DataAction.Update;
//		messageRepush.ClassName = "mwg.wb.pkg.upsert.Upsert";
//		messageRepush.CreatedDate = Utils.GetCurrentDate();
//		messageRepush.Type = 0;
//		messageRepush.DataCenter = DataCenter;
//		try {
//			QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,
//					strNOTE,	DataCenter);
//		} catch (Exception ex) {
//			// TODO Auto-generated catch block
//			return false;
//		}
//
//		Logs.Log(isLog, strNOTE, "PushUpdateStockStore");
//		return true;
//	}

	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
