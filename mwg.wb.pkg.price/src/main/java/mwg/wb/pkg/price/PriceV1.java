package mwg.wb.pkg.price;

import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.business.ProductHelper;
import mwg.wb.client.cache.IgniteClient;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.IMessage.DataAction;
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
import mwg.wb.common.notify.LineNotify;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.pricestrings.PriceStringBO;
import mwg.wb.model.products.ProductErpPriceBO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PriceV1 implements Ididx {
//	private ORThreadLocal factoryWrite = null;
//	private ORThreadLocal factoryRead = null;
//	private PriceHelper priceHelper = null;
	private ProductHelper productHelper = null;
	private ObjectMapper mapper = null;
	private ErpHelper erpHelper = null;
	int DataCenter = 0;
//	private static List<Integer> MainProductCate = List.of(42, 44, 522, 7077, 7264);
	private ClientConfig clientConfig = null;
	LineNotify notifyHelperLog = null;

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {
//		factoryWrite = (ORThreadLocal) objectTransfer.factoryWrite;
//		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
//		priceHelper = (PriceHelper) objectTransfer.priceHelper;
		productHelper = (ProductHelper) objectTransfer.productHelper;
		mapper = (ObjectMapper) objectTransfer.mapper;
		erpHelper = (ErpHelper) objectTransfer.erpHelper;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		notifyHelperLog = (LineNotify) objectTransfer.notifyHelperLog;
	}

	public int PushSysData(boolean isLog, String Note, String sql, Map<String, Object> params) {

		return 1;
	}

	public boolean CheckDataExits(String Key, String Hash) throws Exception {

		String data = IgniteClient.GetClient(this.clientConfig).GetString(Key);
		if (Utils.StringIsEmpty(data))
			return false;
		if (data.equals(Hash))
			return true;
		return false;
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
		// DC 2 service ngưng, DC 4 lấy và bom qua 2 ben

		try {
			String strNOTE = message.Note + "";

			boolean isLog = false;
			if (strNOTE.contains("DIDX_TOP_LOG") || strNOTE.contains("DIDX_LOG")) {

				isLog = true;
			}
			if (strNOTE.contains("DIDX_TOP")) {

				notifyHelperLog.NotifyInfo("SERVICE-PRICEV1:" + message.Identify, DataCenter);
			}
			Logs.Log(isLog, strNOTE, "message " + message.SiteID + " lang " + message.Lang);
			int intCompanyID = 0;
			int SiteID = 0;

			String Lang = message.Lang;
			SiteID = message.SiteID;
			if (Utils.StringIsEmpty(Lang))
				Lang = "vi-VN";

			if ((SiteID <= 0 || Utils.StringIsEmpty(Lang))) {
				Logs.Log(isLog, strNOTE, "SiteID <= 0  ");
				Logs.LogRefresh("pricerefresh", message, "SiteID <= 0");
				return rsmsg;
			}
//			int intPriceAreaID = DidxHelper.getDefaultPriceAreaBySiteID (SiteID, Lang);
//
//			if ((intPriceAreaID <= 0)) {
//				Logs.Log(isLog, strNOTE, "intPriceAreaID <= 0  ");
//				return rsmsg;
//			}
			if (SiteID == 12) { // dtsr
				intCompanyID = 10;
			}
			if (SiteID == 1 && Lang.equals("km-KH")) {// tgdd
				intCompanyID = 6;

			}
			if (SiteID == 6 && Lang.equals("km-KH")) {// tgdd
				intCompanyID = 6;

			}
			if (SiteID == 1 && Lang.equals("vi-VN")) {// tgdd
				intCompanyID = 1;

			}
			if (SiteID == 2) {// dmx
				intCompanyID = 2;

			}
			if (intCompanyID <= 0) {
				return rsmsg;
			}
			String strIdentify = message.Identify;
			if (strIdentify.startsWith("X"))
				strIdentify = strIdentify.substring(1);
			String[] messIdentify = strIdentify.split("\\|");
			if (messIdentify.length < 4) {
				Logs.WriteLine("param: " + strIdentify);
				return rsmsg;
			}

			if (Utils.StringIsEmpty(Lang)) {
				Lang = "vi-VN";

			}
			String strProductCode = messIdentify[2].trim();
			int intOutputTypeID = Integer.valueOf(messIdentify[1]);
			int intPriceAreaID = DidxHelper.getDefaultPriceAreaBySiteID(SiteID, Lang);

			// String exStr =
			// ",RunVersion,PriceOrg,Price402,WebStatusId,TotalQuantity,ProductCodeTotalQuantity,Quantity,WebMinStock,ProductCodeQuantity,CenterQuantity,"
			// .toLowerCase();
			Logs.Log(isLog, strNOTE, "service price");
//			String picekey = SiteID + "_" + intOutputTypeID + "_" + intPriceAreaID;

//			MessageQueue messageRepush = new MessageQueue();
//			messageRepush.SqlList = new ArrayList<SqlInfo>();
//			messageRepush.SiteID = SiteID;

			ProductErpPriceBO[] lstPriceERP = null;
			try {
				lstPriceERP = erpHelper.GetPriceByProductCode(intPriceAreaID, strProductCode, intOutputTypeID,
						intCompanyID, SiteID);
			} catch (Throwable e)  {
				loimeroi: {
					rsmsg.Message = "ERP returned null";
					rsmsg.StackTrace = Utils.stackTraceToString(e);
					Logs.LogException(e);
					if (message.DataCenter == 3) {
						lstPriceERP = new ProductErpPriceBO[0];
						break loimeroi;
					} else {
						rsmsg.Code = ResultCode.Retry;
					}
					Logs.LogRefresh("pricerefresh", message, "ERP ERR", SiteID);
					return rsmsg;
				}
			}
			Logs.Log(isLog, strNOTE,
					"GetPriceByProductCode param  intPriceAreaID:" + intPriceAreaID + ",strProductCode:"
							+ strProductCode + ",intOutputTypeID: " + intOutputTypeID + ",intCompanyID:"
							+ intCompanyID);
			long ProductID = productHelper.GetProductIDByProductCodeFromCache(strProductCode);

//			if (lstPriceERP != null) {
//				Logs.Log(isLog, strNOTE, "price  " + intPriceAreaID + ":" + lstPriceERP.length);
//				Logs.LogRefresh("pricerefresh", message, "lstPriceERP:" + lstPriceERP.length);
//				for (ProductErpPriceBO item : lstPriceERP) {
//					item.PriceArea = intPriceAreaID;
//					item.OutputType = intOutputTypeID;
//					item.CompanyID = intCompanyID;
//					item.SiteID = SiteID;
//					item.LangID = Lang;
//
//					item.RecordID = picekey + "_" + item.ProvinceId + "_" + item.ProductCode.trim();
//					item.didxupdateddate = Utils.GetCurrentDate();
//					SqlInfo sqlinfo = new SqlInfo();
//
//					RefSql ref = new RefSql();
//					Utils.BuildSql(isLog, strNOTE, "product_price", "recordid", item.RecordID, item, exStr, ref);
//
//					sqlinfo.Sql = ref.Sql;
//					sqlinfo.Params = ref.params;
//					if (isLog) {
//						try {
//							Logs.Log(isLog, strNOTE,
//									item.RecordID + "" + ref.Sql + "" + mapper.writeValueAsString(sqlinfo));
//						} catch (JsonProcessingException e) {
//							//Auto-generated catch block
//							Logs.LogException(e);
//						}
//					}
//					messageRepush.SqlList.add(sqlinfo);
////					String cmd2 = "create edge e_code_price from (select from pm_product where productid='"
////							+ item.ProductCode.trim() + "')   to(select from  product_price where recordid ='"
////							+ item.RecordID + "'   and in('e_code_price')[productid = '" + item.ProductCode.trim()
////							+ "'].size() = 0)";
////					SqlInfo sqlinfoEdge = new SqlInfo();
////					sqlinfoEdge.Sql = cmd2;
////					messageRepush.SqlList.add(sqlinfoEdge);
//
//					SqlInfo sqlinfoEgde1 = new SqlInfo();
//					sqlinfoEgde1.Params = new HashMap<String, Object>();
//					sqlinfoEgde1.Type = SqlInfoType.CREATE_EDGE_BY_NODE;
//					sqlinfoEgde1.Params.put("edge", "e_code_price");
//					sqlinfoEgde1.Params.put("from", item.ProductCode.trim());
//					sqlinfoEgde1.Params.put("to", item.RecordID);
//					messageRepush.SqlList.add(sqlinfoEgde1);
//
//				}
//
//				// double ProductID1 = productHelper.GetProductIDByProductCode(strProductCode);
//				if (SiteID == 12) {// th company=1: tgdd hoac dtsr
//
//					int CategoryID = 0;
//					try {
//						CategoryID = productHelper.GetProductCategoryIDFromCode(strProductCode);
//					} catch (Throwable e1) {
//						Logs.LogException(e1);
//						rsmsg.Code = ResultCode.Retry;
//						Logs.LogRefresh("pricerefresh", message, "GetProductCategoryIDFromCode ERR");
//						return rsmsg;
//					}
// 
//					if (CategoryID > 0 && !MainProductCate.contains(CategoryID)) {
//						// de nguyen khuc nay cho data cu dung
//						intPriceAreaID = 402;
//						intCompanyID = 10;
//
//						picekey = SiteID + "_" + intOutputTypeID + "_" + intPriceAreaID;
//
//						// Logs.WriteLine("intPriceAreaID " + intPriceAreaID);
//						ProductErpPriceBO[] lstPriceERP2 = null;
//						try {
//							lstPriceERP2 = erpHelper.GetPriceByProductCode(intPriceAreaID, strProductCode,
//									intOutputTypeID, intCompanyID);
//						} catch (Throwable e1) {
//							rsmsg.Code = ResultCode.Retry;
//							Logs.LogException(e1);
//							Logs.LogRefresh("pricerefresh", message, "GetPriceByProductCode ERR");
//							return rsmsg;
//						}
//						if (lstPriceERP2 != null) {
//							Logs.Log(isLog, strNOTE, picekey + "price 402:" + lstPriceERP2.length);
//							Logs.LogRefresh("pricerefresh", message, "price 402:" + lstPriceERP2.length);
//							for (ProductErpPriceBO item2 : lstPriceERP2) {
//								item2.PriceArea = intPriceAreaID;
//								item2.OutputType = intOutputTypeID;
//								item2.CompanyID = intCompanyID;
//								item2.SiteID = SiteID;
//								item2.LangID = Lang;
//								item2.RecordID = picekey + "_" + item2.ProvinceId + "_" + item2.ProductCode.trim();
//								item2.didxupdateddate = Utils.GetCurrentDate();
//								SqlInfo sqlinfo = new SqlInfo();
//								RefSql ref = new RefSql();
//
//								Utils.BuildSql(isLog, strNOTE, "product_price", "recordid", item2.RecordID, item2,
//										exStr, ref);
//								// Logs.Log(isLog, strNOTE,item.RecordID+""+ ref.Sql);
//
//								sqlinfo.Sql = ref.Sql;
//								sqlinfo.Params = ref.params;
//								if (isLog) {
//									try {
//										Logs.Log(isLog, strNOTE, item2.RecordID + "" + ref.Sql + ""
//												+ mapper.writeValueAsString(sqlinfo));
//									} catch (JsonProcessingException e) {
//										//  Auto-generated catch block
//										// e.printStackTrace();
//									}
//								}
//								messageRepush.SqlList.add(sqlinfo);
//
//								SqlInfo sqlinfoEgde1 = new SqlInfo();
//								sqlinfoEgde1.Params = new HashMap<String, Object>();
//								sqlinfoEgde1.Type = SqlInfoType.CREATE_EDGE_BY_NODE;
//								sqlinfoEgde1.Params.put("edge", "e_code_price");
//								sqlinfoEgde1.Params.put("from", item2.ProductCode.trim());
//								sqlinfoEgde1.Params.put("to", item2.RecordID);
//
//								messageRepush.SqlList.add(sqlinfoEgde1);
//								messageRepush.IsCheckHash = true;
//							}
//						}
//					}
//					/// lay data cho khu vuc gia 13 chuan bi cho data moi
//					// if (DataCenter==3) {
//					// lay thêm giá hãng cho site sieu re
//					intPriceAreaID = 13;
//					intCompanyID = 1;
//					picekey = SiteID + "_" + intOutputTypeID + "_" + intPriceAreaID;
//					// Logs.WriteLine("intPriceAreaID " + intPriceAreaID);
//					ProductErpPriceBO[] lstPriceERP13 = null;
//					try {
//						lstPriceERP13 = erpHelper.GetPriceByProductCode(intPriceAreaID, strProductCode, intOutputTypeID,
//								intCompanyID);
//					} catch (Throwable e1) {
//						rsmsg.Code = ResultCode.Retry;
//						Logs.LogException(e1);
//						Logs.LogRefresh("pricerefresh", message, "GetPriceByProductCode ERR");
//						return rsmsg;
//					}
//					if (lstPriceERP13 != null) {
//						Logs.Log(isLog, strNOTE, picekey + "price  13:" + lstPriceERP13.length);
//						Logs.LogRefresh("pricerefresh", message, "price  13:" + lstPriceERP13.length);
//
//						for (ProductErpPriceBO item2 : lstPriceERP13) {
//							if (item2.Price == 0)
//								continue;// ko cap nhat giá này về 0
//							item2.PriceArea = intPriceAreaID;
//							item2.OutputType = intOutputTypeID;
//							item2.CompanyID = intCompanyID;
//							item2.SiteID = SiteID;
//							item2.LangID = Lang;
//							item2.RecordID = picekey + "_" + item2.ProvinceId + "_" + item2.ProductCode.trim();
//							item2.didxupdateddate = Utils.GetCurrentDate();
//							SqlInfo sqlinfo = new SqlInfo();
//							RefSql ref = new RefSql();
//
//							Utils.BuildSql(isLog, strNOTE, "product_price", "recordid", item2.RecordID, item2, exStr,
//									ref);
//							// Logs.Log(isLog, strNOTE,item.RecordID+""+ ref.Sql);
//							// sqlinfo.Hash = ref.Hash;
//							sqlinfo.Sql = ref.Sql;
//							sqlinfo.Params = ref.params;
//							if (isLog) {
//								try {
//									Logs.Log(isLog, strNOTE,
//											item2.RecordID + "" + ref.Sql + "" + mapper.writeValueAsString(sqlinfo));
//								} catch (JsonProcessingException e) {
//									//  Auto-generated catch block
//									// e.printStackTrace();
//								}
//							}
//							messageRepush.SqlList.add(sqlinfo);
//
//							SqlInfo sqlinfoEgde1 = new SqlInfo();
//							sqlinfoEgde1.Params = new HashMap<String, Object>();
//							sqlinfoEgde1.Type = SqlInfoType.CREATE_EDGE_BY_NODE;
//							sqlinfoEgde1.Params.put("edge", "e_code_price");
//							sqlinfoEgde1.Params.put("from", item2.ProductCode.trim());
//							sqlinfoEgde1.Params.put("to", item2.RecordID);
//
//							messageRepush.SqlList.add(sqlinfoEgde1);
//							// messageRepush.IsCheckHash = true;
//						}
//					}
//
//				}
//			}
//
//			// }
//			// }
//
////			String qu = "gr.dc4.sql.price";
////			String qu2 = "gr.dc4.sql.price";
////			String qubk = "gr.dc2.sql.price";
////			String qudev = "gr.beta.sql.price";
//			int ie = Utils.GetQueueNum10(ProductID);
//			String qu = "gr.dc4.sql" + ie;
//			String qu2 = "gr.dc4.sql" + ie;
//			String qubk = "gr.dc2.sql" + ie;
//			String qudev = "gr.beta.sql";
//
//			messageRepush.Action = DataAction.Update;
//			messageRepush.ClassName = "ms.upsert.Upsert";
//			messageRepush.CreatedDate = Utils.GetCurrentDate();
//			messageRepush.Lang = Lang;
//			messageRepush.SiteID = SiteID;
//			messageRepush.Source = "PRICE";
//			messageRepush.RefIdentify = message.Identify;
//			messageRepush.Identify = String.valueOf(ProductID);
//			messageRepush.Hash = ProductID;
//			messageRepush.Note = strNOTE;
//			messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_PRICE;
//			messageRepush.DataCenter = DataCenter;
//			Logs.LogRefresh("pricerefresh", message, "messageRepush:" + messageRepush.SqlList.size());
//			try {
//				QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush,
//						isLog, strNOTE, 0);
//				Logs.Log(isLog, strNOTE, "Push " + ProductID);
//			} catch (Exception e) {
//
//				Logs.LogException(e);
//				rsmsg.Code = ResultCode.Retry;
//				rsmsg.StackTrace = Logs.GetStacktrace(e);
//				Logs.Log(isLog, strNOTE, "push price=> status " + e.getMessage());
//				return rsmsg;
//			}

			rsmsg.Code = ResultCode.Success;

			// code V2
			if (lstPriceERP != null) {
//				String exStrV2 = ",dataex,".toLowerCase();
				Logs.Log(isLog, strNOTE, "service price v2");
				String picekeyV2 = SiteID + "_" + intOutputTypeID + "_" + intPriceAreaID + "_" + strProductCode;
				MessageQueue messageRepushV2 = new MessageQueue();
				messageRepushV2.SqlList = new ArrayList<SqlInfo>();
				messageRepushV2.SiteID = SiteID;

				Logs.Log(isLog, strNOTE, "pricev2  " + intPriceAreaID + ":" + lstPriceERP.length);
				Logs.LogRefresh("pricerefresh", message, "lstPriceERP:" + lstPriceERP.length, SiteID);
				String DataPrice0 = mapper.writeValueAsString(lstPriceERP);
				PriceStringBO item0 = new PriceStringBO();
				item0.ProductCode = strProductCode;
				item0.PriceArea = intPriceAreaID;
				item0.OutputType = intOutputTypeID;
				item0.CompanyID = intCompanyID;
				item0.SiteID = SiteID;
				item0.LangID = Lang;
				item0.Data = DataPrice0;
				item0.RecordID = picekeyV2;
				item0.didxupdateddate = Utils.GetCurrentDate();

				SqlInfo sqlinfo0 = new SqlInfo();
				RefSql ref0 = new RefSql();
				Utils.BuildSql(isLog, strNOTE, "productprice", "recordid", picekeyV2, item0, "", ref0);
				sqlinfo0.Sql = ref0.Sql;
				sqlinfo0.tablename = ref0.lTble;
				sqlinfo0.tablekey = ref0.lId;
				sqlinfo0.Params = ref0.params;

				messageRepushV2.SqlList.add(sqlinfo0);

				SqlInfo sqlinfoEgde0 = new SqlInfo();
				sqlinfoEgde0.Params = new HashMap<String, Object>();
				sqlinfoEgde0.Type = SqlInfoType.CREATE_EDGE_BY_NODE;
				sqlinfoEgde0.Params.put("edge", "e_codeprice");
				sqlinfoEgde0.Params.put("from", strProductCode.trim());
				sqlinfoEgde0.Params.put("to", item0.RecordID);
				messageRepushV2.SqlList.add(sqlinfoEgde0);

				int ieV2 = Utils.GetQueueNum(ProductID);
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
				messageRepushV2.Identify = String.valueOf(ProductID);
				messageRepushV2.Hash = ProductID;
				messageRepushV2.CachedType = 1;
				messageRepushV2.Note = strNOTE;
				messageRepushV2.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_PRICE;
				messageRepushV2.DataCenter = DataCenter;
				Logs.getInstance().Log(isLog, strNOTE, "PriceV2 Push messageRepush ", messageRepushV2);
				Logs.LogRefresh("pricepushupsert", message, "" + messageRepushV2.SqlList.size(), SiteID);
				try {
					QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(quV2, qu2V2, qubkV2, qudevV2,
							messageRepushV2, isLog, strNOTE, 0); // 0 bom 2 cái
					Logs.Log(isLog, strNOTE, "Push V2 " + ProductID);
				} catch (Exception e) {

					Logs.LogException(e);
					rsmsg.Code = ResultCode.Retry;
					rsmsg.StackTrace = Logs.GetStacktrace(e);
					Logs.Log(isLog, strNOTE, "push price=> upsertv2 " + e.getMessage());
					return rsmsg;
				}
			} else {
				Logs.Log(isLog, strNOTE, "service price v2 ERP RONG");

			}
		} catch (Exception e) {
			Logs.LogException(e);
			rsmsg.StackTrace = Logs.GetStacktrace(e);
			rsmsg.Code = ResultCode.Retry;
		}
		return rsmsg;

	}

	@Override
	public ResultMessage RunScheduleTask() {
		// Auto-generated method stub
		return null;
	}

}

//  class ProductErpPriceNotDefaultBO extends ProductErpPriceBO {
//	
//}
