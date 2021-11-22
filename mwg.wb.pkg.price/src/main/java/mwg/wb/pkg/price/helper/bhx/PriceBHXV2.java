package mwg.wb.pkg.price.helper.bhx;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
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
import mwg.wb.client.redis.RedisClient;
import mwg.wb.client.service.BhxServiceHelper;
import mwg.wb.client.service.CodeTimer;
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
import mwg.wb.common.bhx.ProvinceDetailPO;
import mwg.wb.common.bhx.StoreProvinceConfig;
import mwg.wb.common.notify.LineNotify;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.pricestrings.PriceStringBO;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductErpPriceBO;

public class PriceBHXV2 implements Ididx {
	private ORThreadLocal factoryWrite = null;
	private ORThreadLocal factoryRead = null;
	private BHXStoreHelper bhxStoreHelper = null;
	// private BhxPriceHelper bhxPriceHelper = null;
	private ProductHelper productHelper = null;
	private ObjectMapper mapper = null;
	private ErpHelper erpHelper = null;
	private BhxServiceHelper bhxServiceHelper = null;
	
	int DataCenter = 0;
	LineNotify notifyHelperLog = null;

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
		notifyHelperLog = (LineNotify) objectTransfer.notifyHelperLog;
		
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
		DataCenter = 0;// message.DataCenter;
		ResultMessage rsmsg = new ResultMessage();
		rsmsg.Code = ResultCode.Success;

		String strNOTE = message.Note + "";

		boolean isLog = false;
		if (strNOTE.contains("DIDX_TOP_LOG") || strNOTE.contains("DIDX_LOG")) {
			isLog = true;
		}
		if (strNOTE.contains("DIDX_TOP")) {
			notifyHelperLog.NotifyInfo("SERVICE-PRICEBHX:" + message.Identify, DataCenter);
		}
		Logs.Log(isLog, strNOTE, "message " + message.Identify + " site: " + message.SiteID + " lang: " + message.Lang);
		int intCompanyID = 0;
		int SiteID = 0;
		var timer = new CodeTimer("checktime");
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
			if(productId <= 0) {
				Logs.Log(isLog, strNOTE, "productId < 0  ");
				return rsmsg;
			}
			var area = DidxHelper.getDefaultPriceAreaBySiteID(SiteID, Lang);
			// for những kho đã khai báo ròi lấy giá
			int[] stores = bhxStoreHelper.getAllStore();

			var lstPriceERP = new ArrayList<ProductErpPriceBO>();
			timer.reset("getprice");
			List<ProductErpPriceBO> objPrice = null;
			try {
				objPrice = productHelper.GetPriceByProductID((int) productId);
			} catch (Throwable e1) {
				Logs.LogException(e1 );
				rsmsg.Code = ResultCode.Retry;
				return rsmsg;
			}
			Map<Integer, ProductErpPriceBO> prices = objPrice.stream()
					.collect(Collectors.toMap(obj -> obj.StoreID, obj -> obj,(ob1, obj2) -> ob1));
			for (int store : stores) {
				if(store < 1)
					continue;
				
				var price = bhxServiceHelper.GetPriceBHXOnline(strProductCode, store);
				notifyHelperLog.NotifyInfo("SERVICE-PRICEBHX-STORE:" + store, DataCenter);
				Logs.Log(isLog, strNOTE, "Gia " + strProductCode + " tai kho " + store);
				// ko có lỗi thì làm
				if (price != -1) {
					
					var objPriceStore = prices.get(store);
					
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
						// objPriceStore.CategoryId = CategoryID;
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
						// objPriceStore.CategoryId = CategoryID;
						objPriceStore.SiteID = 11;
						objPriceStore.LangID = Lang;
						objPriceStore.OutputType = intOutputTypeID;
						objPriceStore.CompanyID = intCompanyID;
						objPriceStore.PriceArea = area;
					}

					// cập nhật lại ngày thay đổi giá
					var now = Utils.GetCurrentDate();
					objPriceStore.UpdatedPriceDate = now;
					var objPriceNew = BhxPriceHelper.ProcessProductStatus(objPriceStore, false);

					// add object vào trong list
					lstPriceERP.add(objPriceNew);
				}
			}
			timer.end();
			// xử lý giá mặc định
			// for all province
			// lấy ra first price erp của tỉnh đó có giá cao nhất
			// set store = 0 và add vào list price
//		 	if (lstPriceERP != null && lstPriceERP.size() > 0) {
//			 	var lstProvince = bhxStoreHelper.getAllProvince();
//			 	if(lstProvince != null && lstProvince.size() >0) {
//			 		for (ProvinceDetailPO provinceDetailPO : lstProvince) {
//						var prov = provinceDetailPO.getProvinceId();
//						var priceProvince = lstPriceERP.stream()
//							.filter(x -> x.ProvinceId == prov)
//							.max(Comparator.comparingDouble(ProductErpPriceBO::getPrice))
//							.get();
//						//store là 0; tương đương với mặc định
//						priceProvince.StoreID = 0;
//						
//						lstPriceERP.add(priceProvince);						
//					}
//			 	}
//		 	}
			if (strNOTE.contains("DIDX_TOP")) {
				notifyHelperLog.NotifyInfo("SERVICE-PRICEBHX-TIMER:" + timer.getLogs(), DataCenter);
			}
			
			if (lstPriceERP != null  ) {
				// String exStrV2 = ",dataex,".toLowerCase();
				Logs.Log(isLog, strNOTE, "service price v2");
				String picekeyV2 = SiteID + "_" + intOutputTypeID + "_" + area + "_" + strProductCode;
				MessageQueue messageRepushV2 = new MessageQueue();
				messageRepushV2.SqlList = new ArrayList<SqlInfo>();
				messageRepushV2.SiteID = SiteID;

				Logs.Log(isLog, strNOTE, "pricev2  " + area + ":" + lstPriceERP.size());
				Logs.LogRefresh("pricerefreshV2", message, "lstPriceERP:" + lstPriceERP.size(),SiteID );
				String dataPrice0 = mapper.writeValueAsString(lstPriceERP);
				PriceStringBO item0 = new PriceStringBO();
				item0.ProductCode = strProductCode;
				item0.PriceArea = area;
				item0.OutputType = intOutputTypeID;
				item0.CompanyID = intCompanyID;
				item0.SiteID = SiteID;
				item0.LangID = Lang;
				item0.Data = dataPrice0;
				item0.RecordID = picekeyV2;
				item0.didxupdateddate = Utils.GetCurrentDate();

				SqlInfo sqlinfo0 = new SqlInfo();
				RefSql ref0 = new RefSql();
				Utils.BuildSql(isLog, strNOTE, "productprice", "recordid", picekeyV2, item0, "", ref0);
				sqlinfo0.Sql = ref0.Sql;
				sqlinfo0.Params = ref0.params;
				sqlinfo0.tablename = ref0.lTble;
				sqlinfo0.tablekey = ref0.lId;
				messageRepushV2.SqlList.add(sqlinfo0);

				SqlInfo sqlinfoEgde0 = new SqlInfo();
				sqlinfoEgde0.Params = new HashMap<String, Object>();
				sqlinfoEgde0.Type = SqlInfoType.CREATE_EDGE_BY_NODE;
				sqlinfoEgde0.Params.put("edge", "e_codeprice");
				sqlinfoEgde0.Params.put("from", strProductCode.trim());
				sqlinfoEgde0.Params.put("to", item0.RecordID);
				messageRepushV2.SqlList.add(sqlinfoEgde0);

				int ieV2 = Utils.GetQueueNum(productId);
				String quV2 = "gr.dc4.sqlpri" + ieV2;
				String qu2V2 = "gr.dc4.sqlpri" + ieV2;
				String qubkV2 = "gr.dc2.sqlpri" + ieV2;
				String qudevV2 = "gr.beta.sqlpri";

				messageRepushV2.Action = DataAction.Update;
				messageRepushV2.ClassName = "ms.upsert.Upsert";
				messageRepushV2.CreatedDate = Utils.GetCurrentDate();
				messageRepushV2.Lang = Lang;
				messageRepushV2.SiteID = SiteID;
				messageRepushV2.Source = "PRICE";
				messageRepushV2.RefIdentify = message.Identify;
				messageRepushV2.Identify = String.valueOf(productId);
				messageRepushV2.Hash = productId;
				messageRepushV2.Note = strNOTE;
				messageRepushV2.CachedType = 1;
				messageRepushV2.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_PRICE;
				messageRepushV2.DataCenter = DataCenter;
				Logs.getInstance().Log(isLog, strNOTE, "PriceBHX Push messageRepush ", messageRepushV2);
				// Logs.LogRefresh("pricepushupsertV2", message, "messageRepush:" +
				// messageRepushV2.SqlList.size());
				try {
					QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(quV2, qu2V2, qubkV2, qudevV2,
							messageRepushV2, isLog, strNOTE, 0); // 0 bom 2 cái
					Logs.Log(isLog, strNOTE, "Push V2 BHX " + productId);
				} catch (Exception e) {

					Logs.LogException(e );
					rsmsg.Code = ResultCode.Retry;
					rsmsg.StackTrace = Logs.GetStacktrace(e);
					Logs.Log(isLog, strNOTE, "push price=> upsert BHX v2 " + e.getMessage());
					return rsmsg;
				}
			}

			rsmsg.Code = ResultCode.Success;
		} catch (Throwable e) {
			// Logs.LogException("service_price.txt", e);
			Logs.LogException(e );
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
