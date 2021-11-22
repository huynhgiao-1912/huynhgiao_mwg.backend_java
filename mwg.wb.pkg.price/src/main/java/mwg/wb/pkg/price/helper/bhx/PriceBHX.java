package mwg.wb.pkg.price.helper.bhx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.helper.BHXProductHelper;
import mwg.wb.business.helper.BHXStoreHelper;
import mwg.wb.business.helper.BhxPriceHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.BhxServiceHelper;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.client.service.ProductServiceHelper;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.MessageQueuePushType;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.RefSql;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.SqlInfoType;
import mwg.wb.common.Utils;
import mwg.wb.common.bhx.StoreProvinceBO;
import mwg.wb.common.bhx.StoreProvinceConfig;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductErpPriceBO;

public class PriceBHX implements Ididx {
	private ORThreadLocal factoryWrite = null;
	private ORThreadLocal factoryRead = null;
	// private BhxPriceHelper bhxPriceHelper = null;
	private ProductHelper productHelper = null;
	private ObjectMapper mapper = null;
	private ErpHelper erpHelper = null;
	private BhxServiceHelper bhxServiceHelper = null;
	private BHXStoreHelper bhxStoreHelper = null;
	int DataCenter = 0;

	private ClientConfig clientConfig = null;

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {
		factoryWrite = (ORThreadLocal) objectTransfer.factoryWrite;
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		// bhxPriceHelper = (BhxPriceHelper) objectTransfer.bhxPriceHelper;
		productHelper = (ProductHelper) objectTransfer.productHelper;
		mapper = (ObjectMapper) objectTransfer.mapper;
		erpHelper = (ErpHelper) objectTransfer.erpHelper;
		bhxServiceHelper = (BhxServiceHelper) objectTransfer.bhxServiceHelper;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		bhxStoreHelper = (BHXStoreHelper) objectTransfer.bHXStoreHelper;
	}

	public int PushSysData(boolean isLog, String Note, String sql, Map<String, Object> params) {

		return 1;
	}

	public int PushSysData(boolean isLog, String Note, String sql) {

		return 1;
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		// X13|0|0160015000771|1
		DataCenter = 0;
		ResultMessage rsmsg = new ResultMessage();
		rsmsg.Code = ResultCode.Success;
		// DC 2 service ngưng, DC 4 lấy và bom qua 2 ben

		String strNOTE = message.Note + "";

		boolean isLog = false;
		if (strNOTE.contains("DIDX_TOP_LOG") || strNOTE.contains("DIDX_LOG")) {

			isLog = true;
		}
		Logs.Log(isLog, strNOTE, "message " + message.Identify + " site: " + message.SiteID + " lang: " + message.Lang);
		int intCompanyID = 0;
		int SiteID = 0;

		String Lang = message.Lang;
		SiteID = message.SiteID;
		// ko phải site bhx thì return
		if (SiteID != 11) {
			return rsmsg;
		}
		if (Utils.StringIsEmpty(Lang))
			Lang = "vi-VN";

		if (SiteID <= 0) {
			Logs.Log(isLog, strNOTE, "SiteID <= 0  ");
			return rsmsg;
		}
		intCompanyID = 3;
		if (intCompanyID <= 0) {
			return rsmsg;
		}
		try {
			String strIdentify = message.Identify;
			if (strIdentify.startsWith("X"))
				strIdentify = strIdentify.substring(1);
			String[] messIdentify = strIdentify.split("\\|");

			if (messIdentify.length < 3) {
				Logs.WriteLine("param: " + strIdentify);
				return rsmsg;
			}

			String strProductCode = messIdentify[2].trim();
			int intOutputTypeID = Integer.valueOf(messIdentify[1]);

			var productId = productHelper.GetProductIDByProductCodeFromCache(strProductCode);

			String exStr = ",RunVersion,PriceOrg,Price402,WebStatusId,TotalQuantity,ProductCodeTotalQuantity,Quantity,WebMinStock,ProductCodeQuantity,CenterQuantity,"
					.toLowerCase();
			MessageQueue messageRepush = new MessageQueue();
			messageRepush.SqlList = new ArrayList<SqlInfo>();
			messageRepush.SiteID = SiteID;

			var area = DidxHelper.getDefaultPriceAreaBySiteID(SiteID, Lang);
			// Action Delete
//		 	if(message.Action == DataAction.Delete)
//		 	{
//		 		//xóa key giá
////		 		var lstPrice = productHelper.GetPriceByProductID(productId);
////		 		if(lstPrice != null)
////		 		{0
////		 			for (ProductErpPriceBO productErpPriceBO : lstPrice) {
////		 				
////		 				SqlInfo sqlinfoDel = new SqlInfo();
////		 				sqlinfoDel.Sql = "update product_price set isdeleted=1 where recordid='" + productErpPriceBO.RecordID + "'";
////		 				messageRepush.SqlList.add(sqlinfoDel);
////					}
////		 		}
//		 		//update product_price set isdeleted=0 where productcode= 8935012413338 AND siteid=11
//		 		SqlInfo sqlinfoDel = new SqlInfo();
// 				sqlinfoDel.Sql = "update product_price set isdeleted=1 where productcode='" + strProductCode + "' and siteid='" + SiteID + "'";
// 				messageRepush.SqlList.add(sqlinfoDel);
// 				Logs.Log(isLog, strNOTE, "Xoa key gia " + strProductCode);
//		 		
//		 	}
//		 	else 
//		 	{			
			// for những kho đã khai báo ròi lấy giá
			// BHXStoreHelper bhxStoreHelper = new BHXStoreHelper("10.1.6.151");
			int[] stores = bhxStoreHelper.getAllStore();

			int CategoryID = 0;
			try {
				CategoryID = productHelper.GetProductCategoryIDFromCode(strProductCode);
			} catch (Throwable e2) {
				Logs.LogException(e2 );
				rsmsg.Code = ResultCode.Retry;
				return rsmsg;
			}
			for (int store : stores) {
				var price = bhxServiceHelper.GetPriceBHXOnline(strProductCode, store);
				Logs.Log(isLog, strNOTE, "Gia " + strProductCode + " tai kho " + store);
				// ko có lỗi thì làm
				if (price != -1) {
					List<ProductErpPriceBO> objPrice = null;
					try {
						objPrice = productHelper.GetPriceByProductID((int) productId);
					} catch (Throwable e1) {
						Logs.LogException(e1);
						rsmsg.Code = ResultCode.Retry;
						return rsmsg;
					}
					var objPriceStore = objPrice.stream().filter(x -> x.StoreID == store).findAny().orElse(null);

					var current_province = bhxStoreHelper.getProvinceByStore(store);
					if (objPriceStore == null) {
						objPriceStore = new ProductErpPriceBO();
						objPriceStore.Price = price;
						objPriceStore.ProductId = productId;
						objPriceStore.ProductCode = strProductCode;
						objPriceStore.ProvinceId = current_province;
						objPriceStore.IsPriceConfirmed = price > 0;
						objPriceStore.IsShowHome = price > 0;
						objPriceStore.IsShowWeb = price > 0;
						objPriceStore.IsWebShow = price > 0;
						objPriceStore.StatusId = 3;
						objPriceStore.StoreID = store;
						objPriceStore.CategoryId = CategoryID;
						objPriceStore.WebStatusId = 1;
						objPriceStore.ProductCodeQuantity = 0;
						objPriceStore.Quantity = 0;
						objPriceStore.ProductCodeTotalQuantity = 0;
						objPriceStore.TotalQuantity = 0;
						objPriceStore.SiteID = 11;
						objPriceStore.LangID = Lang;
						objPriceStore.OutputType = intOutputTypeID;
						objPriceStore.CompanyID = intCompanyID;
						objPriceStore.PriceArea = area;
					} else {
						objPriceStore.Price = price;
						objPriceStore.ProductId = productId;
						objPriceStore.ProductCode = strProductCode;
						objPriceStore.ProvinceId = current_province;
						objPriceStore.IsPriceConfirmed = price > 0;
						objPriceStore.IsShowHome = price > 0;
						objPriceStore.IsWebShow = price > 0;
						objPriceStore.IsShowWeb = price > 0;
						objPriceStore.StatusId = 3;
						objPriceStore.StoreID = store;
						objPriceStore.CategoryId = CategoryID;
						objPriceStore.SiteID = 11;
						objPriceStore.LangID = Lang;
						objPriceStore.OutputType = intOutputTypeID;
						objPriceStore.CompanyID = intCompanyID;
						objPriceStore.PriceArea = area;
					}

					// cập nhật lại ngày thay đổi giá
					var now = Utils.GetCurrentDate();
					// objPriceStore.ProductArrivalDate = now;
					objPriceStore.UpdatedPriceDate = now;
					var objPriceNew = BhxPriceHelper.ProcessProductStatus(objPriceStore, false);

					String picekey = SiteID + "_" + intOutputTypeID + "_" + store;
					objPriceNew.RecordID = picekey + "_" + current_province + "_" + strProductCode;
					objPriceNew.didxupdateddate = now;

					SqlInfo sqlinfo = new SqlInfo();

					RefSql ref = new RefSql();
					Utils.BuildSql(isLog, strNOTE, "product_price", "recordid", objPriceNew.RecordID, objPriceNew,
							exStr, ref);

					sqlinfo.Sql = ref.Sql;
					sqlinfo.Params = ref.params;
					try {
						Logs.Log(isLog, strNOTE,
								objPriceNew.RecordID + "" + ref.Sql + "" + mapper.writeValueAsString(sqlinfo));
					} catch (JsonProcessingException e) {
						Logs.LogException(e );
					}

					messageRepush.SqlList.add(sqlinfo);

					SqlInfo sqlinfoEgde1 = new SqlInfo();
					sqlinfoEgde1.Params = new HashMap<String, Object>();
					sqlinfoEgde1.Type = SqlInfoType.CREATE_EDGE_BY_NODE;
					sqlinfoEgde1.Params.put("edge", "e_code_price");
					sqlinfoEgde1.Params.put("from", objPriceNew.ProductCode.trim());
					sqlinfoEgde1.Params.put("to", objPriceNew.RecordID);

					messageRepush.SqlList.add(sqlinfoEgde1);

				} else {
					// get giá bị lỗi thì xóa key giá đó luôn cho an toàn
					SqlInfo sqlinfoDel = new SqlInfo();
					sqlinfoDel.Sql = "update product_price set isdeleted=1 where productcode='" + strProductCode
							+ "' and storeid='" + store + "'";
					messageRepush.SqlList.add(sqlinfoDel);
				}

			}
//		 	}

			// có query thì mới push queue
			if (messageRepush.SqlList != null && messageRepush.SqlList.size() > 0) {
//			 	String qu = "gr.dc4.sql.price"  ;
//				String qu2 = "gr.dc4.sql.price"  ;
//				String qubk = "gr.dc2.sql.price";
//				String qudev = "gr.beta.sql.price";
				int ie = Utils.GetQueueNum10(productId);
				String qu = "gr.dc4.sql" + ie;
				String qu2 = "gr.dc4.sql" + ie;
				String qubk = "gr.dc2.sql" + ie;
				String qudev = "gr.beta.sql";

				messageRepush.Action = DataAction.Update;
				messageRepush.ClassName = "ms.upsert.Upsert";
				messageRepush.CreatedDate = Utils.GetCurrentDate();
				messageRepush.Lang = Lang;
				messageRepush.SiteID = SiteID;
				messageRepush.Source = "PRICE";
				messageRepush.RefIdentify = message.Identify;
				messageRepush.Identify = String.valueOf(productId);
				messageRepush.Note = strNOTE;
				messageRepush.Hash = productId;
				messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_PRICE;
				messageRepush.DataCenter = DataCenter;
				try {
					QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush,
							isLog, strNOTE, message.DataCenter);
					Logs.Log(isLog, strNOTE, "Push " + productId);
				} catch (Exception e) {

					Logs.LogException(e);
					rsmsg.Code = ResultCode.Retry;
					rsmsg.StackTrace = Utils.stackTraceToString(e);
					Logs.Log(isLog, strNOTE, "push price=> status " + e.getMessage());
					return rsmsg;
				}
			}

			rsmsg.Code = ResultCode.Success;
		} catch (Throwable e) {
			Logs.LogException("service_price.txt", e);
			rsmsg.StackTrace = Utils.stackTraceToString(e);
			rsmsg.Code = ResultCode.Retry;
		}
		return rsmsg;

	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
