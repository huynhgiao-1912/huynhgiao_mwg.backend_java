package mwg.wb.pkg.upsert;

import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.LogHelper;
import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.client.cache.IgniteClient;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.CodeTimers;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.*;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.notify.LineNotify;
import mwg.wb.model.LogLevel;
import mwg.wb.model.api.ClientConfig;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Upsert implements Ididx {
	private ORThreadLocal factoryWrite = null;
	private ORThreadLocal factoryRead = null;
	private PriceHelper priceHelper = null;
	private ProductHelper productHelper = null;
	private ObjectMapper mapper = null;
	private ErpHelper erpHelper = null;
	private ClientConfig clientConfig = null;
	private LineNotify notifyHelperLog = null;

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {

		factoryWrite = (ORThreadLocal) objectTransfer.factoryWrite;
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		priceHelper = (PriceHelper) objectTransfer.priceHelper;
		productHelper = (ProductHelper) objectTransfer.productHelper;
		mapper = (ObjectMapper) objectTransfer.mapper;
		erpHelper = (ErpHelper) objectTransfer.erpHelper;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		notifyHelperLog = (LineNotify) objectTransfer.notifyHelperLog;
	}

	public static Map<Long, Long> StatusVersion = new HashMap<Long, Long>();

	public boolean RepushTable(MessageQueue messageGoc, Map<String, Object> edgeParam, int datacenter, boolean isLog,
			String strNOTE, String tbl, String cotName, String cotValue) {
		if (messageGoc.RepushCount > 0) {
			// write log,ko push nua
			return true;
		}
		MessageQueue message = new MessageQueue();
		if (tbl.equals("price_default") || tbl.equals("product_promotion") || tbl.equals("product_price")

				|| tbl.equals("productpromotion") || tbl.equals("productprice")) {
			return true;
		}
		message.Action = DataAction.Add;
		message.Note = strNOTE;
		message.Identify = tbl + "|" + cotName + "|" + cotValue + "|";
		// message.ClassName = classname;
		message.CreatedDate = Utils.GetCurrentDate();
		message.ID = 0;
		List<SqlInfo> SqlList = new ArrayList<SqlInfo>();

		SqlInfo sqlinfoEgde2 = new SqlInfo();
		sqlinfoEgde2.Params = edgeParam;
		sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
		SqlList.add(sqlinfoEgde2);

		message.IsCreateEdge = true;

		message.SqlList = SqlList;
		message.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_RECREATE_EDGE;
		message.DataCenter = datacenter;
		String queue = "";
		String queuebk = "";
		String queueBeta = "";
		queue = "gr.reinitdata";
		queuebk = "gr.reinitdata";
		queueBeta = "gr.reinitdata";

		for (int c = 0; c < 100000000; c++) {
			try {
				QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queuebk, queueBeta, message,
						false, strNOTE, datacenter);

				break;
			} catch (Exception e) {

				Utils.Sleep(5000);
			}
		}
		return true;

	}

//	public boolean iSEdgeExistCu(int datacenter, boolean isLog, String strNOTE, String Edge, String fromtbl,
//			String fromName, String fromValue, String totbl, String toName, String toValue) throws Throwable {
////e_product_code:product:productid:999999999:pm_product:recordid:4817
//		if (fromtbl.equals("product")) {
//			long pid = Utils.toLong(fromValue);
//			if (!productHelper.CheckProductID(pid)) {
//				Logs.Log(isLog, strNOTE, "CheckProductID false productid= " + fromValue + " cc " + pid);
//				return true;
//			}
//
//		}
//
//		for (int i = 0; i < 100000000; i++) {
//
//			String fromRID = productHelper.GetRidFromCache(fromtbl, fromName, fromValue);
//			String toRID = productHelper.GetRidFromCache(totbl, toName, toValue);
//			if (Utils.StringIsEmpty(fromRID)) {
//
//				// if (fromtbl.equals("pm_product")) {
//				RepushTable(datacenter, isLog, strNOTE, fromtbl, fromName, fromValue);
//				Logs.WriteLine("wating reinit " + fromtbl);
//				Utils.Sleep(5000);
//				fromRID = productHelper.GetRidFromCache(fromtbl, fromName, fromValue);
//				// }
//			}
//			if (Utils.StringIsEmpty(toRID)) {
//
//				// if (fromtbl.equals("pm_product")) {
//				RepushTable(datacenter, isLog, strNOTE, totbl, toName, toValue);
//				Logs.WriteLine("wating reinit " + totbl);
//				Utils.Sleep(5000);
//				toRID = productHelper.GetRidFromCache(totbl, toName, toValue);
//				// }
//			}
//			if (Utils.StringIsEmpty(fromRID) || Utils.StringIsEmpty(toRID)) {
//				String err = Edge + ":" + fromtbl + ":" + fromName + ":" + fromValue + ":" + totbl + ":" + toName + ":"
//						+ toValue;
//				Logs.WriteLine(err);
//				Logs.Log(isLog, strNOTE, "Utils.StringIsEmpty(fromRID) || Utils.StringIsEmpty(toRID)");
////				if (Edge.equals("e_code_price")) //co the ko co
////				{
////
////					return true;
////				}
//				if (Edge.equals("e_productold_code")) {
//					String Sql = "update product_old_code  set isdeleted=0,productcode='" + toValue
//							+ "' upsert where productcode='" + toValue + "'";
//					Logs.WriteLine("create " + Sql);
//					factoryRead.Command(Sql, null);
//
//				}
//
//				Utils.Sleep(5000);
//				if (i > 3) {
//					Logs.Log(isLog, strNOTE, "ridnotfound");
////					
//					Logs.LogErrorRequireFix("ridnotfound.txt", err);
//					return true;// bo qua , fix sau
//				}
//
//			} else {
//				Logs.Log(isLog, strNOTE, "checkEdgeExist fromRID:" + fromRID + " toRID" + toRID);
//				return productHelper.checkEdgeExist(Edge, fromRID, toRID);
//			}
//		}
//		return false;
//	}

	public boolean iSEdgeExist(MessageQueue message, Map<String, Object> edgeParam, int datacenter, boolean isLog,
			String strNOTE, String Edge, String fromtbl, String fromName, String fromValue, String totbl, String toName,
			String toValue) throws Throwable {
//e_product_code:product:productid:999999999:pm_product:recordid:4817
		if (fromtbl.equals("product")) {
			long pid = Utils.toLong(fromValue);
			if (GConfig.ProductTaoLao.containsKey(pid)) {
				return true;
			}

//			if (!productHelper.CheckProductID(pid)) {
//				if (GConfig.ProductTaoLao.containsKey(pid)) {
//
//				} else {
//					//Logs.LogRefresh("checkproductidfalse", message, "messageRepush:" + message.SqlList.size());
//
//					Logs.Log(isLog, strNOTE, "CheckProductID false productid= " + fromValue + " cc " + pid);
//				}
//				return true;
//			}

		}
		
		var timer = new CodeTimers();
		timer.start("Get_fromRID_toRID");		
		var fromRID = productHelper.GetRidFromCacheWait(fromtbl, fromName, fromValue);
		var toRID = productHelper.GetRidFromCacheWait(totbl, toName, toValue);	
		timer.start("Get_fromRID_toRID");	
		
		long totalTimer = timer.getTimer("Get_fromRID_toRID").getElapsedTime();
		if (totalTimer > 3000) {		
			String messageTimer  = "Get_fromRID_toRID  Timer: " +  totalTimer +" " + Edge + ":" + fromtbl + ":" + fromName + ":" + fromValue + ":" + totbl + ":" + toName + ":"
					+ toValue;
			Logs.Log(isLog, strNOTE, messageTimer);
		}
	
		if (Utils.StringIsEmpty(fromRID)) {

			RepushTable(message, edgeParam, datacenter, isLog, strNOTE, fromtbl, fromName, fromValue);

		}
		if (Utils.StringIsEmpty(toRID)) {
			RepushTable(message, edgeParam, datacenter, isLog, strNOTE, totbl, toName, toValue);

		}
		if (Utils.StringIsEmpty(fromRID) || Utils.StringIsEmpty(toRID)) {
			String err = Edge + ":" + fromtbl + ":" + fromName + ":" + fromValue + ":" + totbl + ":" + toName + ":"
					+ toValue;
			Logs.Log(isLog, strNOTE, "Utils.StringIsEmpty(fromRID) || Utils.StringIsEmpty(toRID)");

//			if (Edge.equals("e_productold_code")) {
//				String Sql = "update product_old_code  set isdeleted=0,productcode='" + toValue
//						+ "' upsert where productcode='" + toValue + "'";
//				Logs.WriteLine("create " + Sql);
//				factoryRead.Command(Sql, null);
//
//			}
			return true; // repush tao lại và bỏ qua

		} else {
			Logs.Log(isLog, strNOTE, "checkEdgeExist fromRID:" + fromRID + " toRID" + toRID);
			return productHelper.checkEdgeExist(Edge, fromRID, toRID);
		}

	}

	public boolean CheckDataExits(String Key, String Hash) throws Exception {

		String data = IgniteClient.GetClient(this.clientConfig).GetString(Key);
		if (Utils.StringIsEmpty(data))
			return false;
		if (data.equals(Hash))
			return true;
		return false;
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage rsmsg = new ResultMessage();
		try {
			int datacenter = message.DataCenter;
			String strNOTE = message.Note + "";
			String refixFilename = "";
			boolean isLog = Utils.IsMessageSaveLog(message.Note);
			// var timer = new CodeTimer("checktime-upsert");
			int siteid = message.SiteID;
			rsmsg.Code = ResultCode.Success;
			// if(strNOTE.equals("DIDX_RENOTI")) return rsmsg;
//			if (datacenter==0) {
//				return rsmsg;
//
//			}
//			if (strNOTE.contains("PackageItemUnit")) {
//				return rsmsg;
//
//			}
			if (strNOTE.contains("DIDX_RENOTI")) {
				refixFilename = "-renoti";
			}
//	if (strNOTE.contains("DIDX_TOP DIDX_LOG|153856")) {
//				return rsmsg;
//			}
			if (message.CompanyID == 8) {

				return rsmsg;
			}

			if (message.BrandID == 3 && message.PushType == 9) {

				return rsmsg;
			}


			String s = message.Source + "";
			Logs.getInstance().Log(isLog, strNOTE, "updatemsg=", message);
			boolean requeireClearcache = false;
			long requeireClearcachePid = 0;
			boolean requireRUNSTATUS = false;
			String source = "" + message.Source;
			if (source.equals("PRICE")) {
				requeireClearcache = true;
				requeireClearcachePid = Utils.toLong(message.Identify);

				Logs.LogRefresh("priceupsert" + refixFilename, message, "" + message.SqlList.size(), siteid);
				// requeireClearcache=true;

			}
			if (source.equals("pm_product")) {
				Logs.LogFactoryMessage("upsert-" + source, mapper.writeValueAsString(message));
				requireRUNSTATUS = true;
				// requeireClearcache=true;

			}

			if (source.equals("PROMOTION")) {
				requeireClearcachePid = Utils.toLong(message.Identify);

				Logs.LogRefresh("promotionupsert" + refixFilename, message, "" + message.SqlList.size(), siteid);

				requeireClearcache = true;
			}

			String sqlType = "";

//			List<SqlInfo> sqlList = new ArrayList<SqlInfo>();
//			if (message.SqlList != null && message.SqlList.size() > 0) {
//				for (final SqlInfo item : message.SqlList) {
//					if (item.Type <= 0) {
//						sqlList.add(item);
//					}
//				}
//			}
//			try {
//				Logs.getInstance().Log(isLog, strNOTE, "upsert", sqlList);
//				var ra = factoryWrite.CommandBash(sqlList); 
//				if (ra.equals("1")) {
//					 
//				}else {
//				 
//					Utils.Sleep(1000);
//					rsmsg.Code = ResultCode.Retry;
//					rsmsg.Message="factoryWrite.CommandBash false";
//					return rsmsg;
//				}
//				
//			} catch (Throwable e) {
//				Logs.LogException(e);
//				rsmsg.Code = ResultCode.Retry;
//				rsmsg.StackTrace=Utils.stackTraceToString(e);
//				return rsmsg;
//			}

			if (message.SqlList != null && message.SqlList.size() > 0) {
				for (final SqlInfo item : message.SqlList) {

					if (item.Type <= 0) {
						if (item.Sql.contains("Upsert where packagestypeid=")) {

							return rsmsg;
						}
						if (item.Sql.contains("gameapp_news_platform")) {

							return rsmsg;
						}

//						if (item.Sql.contains("update product_detail SET ") && strNOTE.contains("PUSHALL")) {
//
//							return rsmsg;
//						}
//						if (item.Sql.contains("update product_gallery SET") && strNOTE.contains("PUSHALL")) {
//
//							return rsmsg;
//						}

						// timer.reset("Command");
						if (item.Sql.contains("pm_currentinstock")) {
							// pm_currentinstock,product_detail
							// if ( item.tablename.equals("pm_currentinstock") ) {
							// :{"quantity":"28485","productid":"3442312000020","warrantydeactivequantity":"0","storechangequantity":"0","storeid":"430","provinceid":"3","scorderlockquantity":"0","recordid":"11982366","inventorystatusid":"1","lockquantity":"0","districtid":"16","pcorderlockquantity":"0","brandid":"1","statuslockquantity":"0"}
							// String productid = String.valueOf(item.Params.get("productid"));
							if (item.Params != null) {
//								String brandid = String.valueOf(item.Params.get("brandid"));
//								int quantity = Utils.toInt(String.valueOf(item.Params.get("quantity")));
//								Logs.LogFactoryMessage("pm_currentinstock_" + brandid,
//										mapper.writeValueAsString(item.Params));
//								if (quantity <= 0) {
//									Logs.LogFactoryMessage("pm_currentinstock0_" + brandid,
//											mapper.writeValueAsString(item.Params));
//
//								}
							}
							// }
						}
						int rsC = Command(item.Sql, item.Params);
						// timer.end();
						// ;
						Logs.Log(isLog, strNOTE, "success");

						// log vào line

						if (rsC == -2) {
							Utils.Sleep(5000);
							rsmsg.Code = ResultCode.ReConnect;
							return rsmsg;
						}
						if (rsC == -3) {
							Utils.Sleep(5000);
							rsmsg.Code = ResultCode.ReConnect;
							return rsmsg;
						}
						if (item.Sql.contains(
								"update product_old_detail SET `note`=:note,`deleteddate`=:deleteddate,`isdeleted`=:isdeleted,`oldid`=:oldid Upsert where oldid=")) {

							sqlType = "P-OLD-DEL";
						}
						if (strNOTE.contains("DIDX_TOP") && !Utils.StringIsEmpty(item.tablename)) {
							// pm_currentinstock,product_detail
//							if (!item.tablename.equals("pm_currentinstock") && !item.tablename.equals("product_detail")
//									&& !item.tablename.equals("payment_category")
//									&& !item.tablename.equals("product_gallery")
//									&& !item.tablename.equals("product_color")) {
//								if (!Utils.StringIsEmpty(item.msg)) {
//									// Update:pricedefault:153856_1_vi-VN br:1 prov:9 st:0,
//									// ProductCode_3:0131491001767 Price_3:2.199E7 WebStatusId_3:4(DIDX_LOG
//									if (item.msg.contains("pricedefault")) {
//										if (item.msg.contains("br:0 prov:0")) {
//											notifyHelperLog.Notify("Update:" + item.tablename + ":" + item.tablekey
//													+ " " + item.msg + "(" + strNOTE + ")");
//										}
//									} else {
//										notifyHelperLog.Notify("Update:" + item.tablename + ":" + item.tablekey + " "
//												+ item.msg + "(" + strNOTE + ")");
//									}
//								} else {
//									notifyHelperLog.Notify(
//											"Update:" + item.tablename + ":" + item.tablekey + " (" + strNOTE + ")");
//								}
//							}
						}
					}
					if (item.Type == SqlInfoType.CREATE_EDGE_BY_EDGE_INFO) {

						String fromNode = String.valueOf(item.Params.get("from"));
						String toNode = String.valueOf(item.Params.get("to"));

						String edge = String.valueOf(item.Params.get("edge"));

						EdgeInfo info = GraphDBConstant.EDGES_LIST.get(edge);

						if (info == null) {
							Logs.LogException("edgenotfound.txt", edge);
						}
						String fromtbl = info.fromtable;
						String totbl = info.totable;
						String fromcol = info.fromcol;
						String tocol = info.tocol;

						if (edge.equals("e_product_prop_value")) {

							if (tocol.equals("recordid")) {
								tocol = "valueid";

							}
						}

						boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE, edge, fromtbl,
								fromcol, fromNode, totbl, tocol, toNode);

						if (x == false) {
							String Sql = "create edge " + edge + " from(select from " + fromtbl + " where " + fromcol
									+ "= '" + fromNode + "') to (select from " + totbl + " where " + tocol + "= '"
									+ toNode + "'  )";

							Logs.Log(isLog, strNOTE, Sql);
							int rsC = Command(Sql, null);
							if (rsC == -2) {
								rsmsg.Code = ResultCode.ReConnect;
								return rsmsg;
							}
							if (edge.equals("e_product_lang")) {
								Logs.Log(true, "DIDX_LOG|e_product_lang", "run " + Sql);
							}

						} else {
							String ms = edge + " Exist " + edge + " fromtbl " + fromtbl + " fromcol " + fromcol
									+ " fromNode " + fromNode + " totbl " + totbl + " tocol " + tocol + " toNode "
									+ toNode;
							if (edge.equals("e_product_lang")) {
								Logs.Log(true, "DIDX_LOG|e_product_lang", "norun " + ms);
							}
							Logs.Log(isLog, strNOTE, ms);
						}
					}
					if (item.Type == SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL) {

						String egde = (String) item.Params.get("edge");
						if (egde.equals("product_old_code")) {
							egde = "e_productold_code";

						}
						String fromNode = String.valueOf(item.Params.get("from"));
						String toNode = String.valueOf(item.Params.get("to"));

						String fromtbl = String.valueOf(item.Params.get("fromtbl"));

						String totbl = String.valueOf(item.Params.get("totbl"));
						String fromcol = String.valueOf(item.Params.get("fromcol"));
						String tocol = String.valueOf(item.Params.get("tocol"));

						if (egde.equals("e_product_prop_value")) {

							if (tocol.equals("recordid")) {
								tocol = "valueid";

							}
						}

						if (egde.equals("e_codeprice")) {

							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE, "e_codeprice",
									"pm_product", "productid", fromNode, "productprice", "recordid", toNode);

							// List<String> lsCOde = productHelper.GetPriceListOfNode(fromNode);
							if (x == false) {
								String Sql = "create edge e_codeprice from(select from pm_product where productid= '"
										+ fromNode + "') to (select from productprice where recordid= '" + toNode
										+ "')";

								int rsC = Command(Sql, null);
								if (rsC == -2) {
									rsmsg.Code = ResultCode.ReConnect;
									return rsmsg;
								}
								Logs.Log(isLog, strNOTE, Sql);
							}
						} else if (egde.equals("e_codepromotion")) {
							// timer.reset("iSEdgeExist");
							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE, "e_codepromotion",
									"pm_product", "productid", fromNode, "productpromotion", "recordid", toNode);
							// timer.end();
							// List<String> lsCOde = productHelper.GetPriceListOfNode(fromNode);
							if (x == false) {
								String Sql = "create edge e_codepromotion from(select from pm_product where productid= '"
										+ fromNode + "') to (select from productpromotion where recordid= '" + toNode
										+ "')";
								// timer.reset("create edge");
								int rsC = Command(Sql, null);
								// timer.end();
								// ;
								if (rsC == -2) {
									rsmsg.Code = ResultCode.ReConnect;
									return rsmsg;
								}
								Logs.Log(isLog, strNOTE, Sql);
							}
						} else {
							if (egde.equals("e_product_lang")) {
								Logs.getInstance().Log(true, "DIDX_LOG|e_product_lang", "create edge ", item);
							}

							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE, egde, fromtbl,
									fromcol, fromNode, totbl, tocol, toNode);
							if (x == false) {
								String Sql = "create edge " + egde + " from(select from " + fromtbl + " where "
										+ fromcol + "= '" + fromNode + "') to (select from " + totbl + " where " + tocol
										+ "= '" + toNode + "'  )";

								if (egde.equals("e_product_lang")) {
									Logs.Log(true, "DIDX_LOG|e_product_lang", "run " + Sql);
								}

								Logs.Log(isLog, strNOTE, Sql);
								int rsC = Command(Sql, null);
								if (rsC == -2) {
									rsmsg.Code = ResultCode.ReConnect;
									return rsmsg;
								}
								Logs.Log(isLog, strNOTE, Sql);
							} else {
								if (egde.equals("e_product_lang")) {
									Logs.Log(true, "DIDX_LOG|e_product_lang", "norun ");
								}
								Logs.Log(isLog, strNOTE,
										egde + " Exist " + egde + " fromtbl " + fromtbl + " fromcol " + fromcol
												+ " fromNode " + fromNode + " totbl " + totbl + " tocol " + tocol
												+ " toNode " + toNode);
							}
						}

					}

					if (item.Type == SqlInfoType.CREATE_EDGE_BY_NODE) {

						String egde = (String) item.Params.get("edge");
						String fromNode = Utils.GetNumberString(String.valueOf(item.Params.get("from")));
						String toNode = Utils.GetNumberString(String.valueOf(item.Params.get("to")));

						String fromtbl = Utils.GetNumberString(String.valueOf(item.Params.get("fromtbl")));
						String totbl = Utils.GetNumberString(String.valueOf(item.Params.get("totbl")));
						String fromcol = Utils.GetNumberString(String.valueOf(item.Params.get("fromcol")));
						String tocol = Utils.GetNumberString(String.valueOf(item.Params.get("tocol")));

						if (Utils.StringIsEmpty(toNode) || Utils.StringIsEmpty(fromNode)) {

							Logs.WriteLine(mapper.writeValueAsString(item.Params));
						}

						if (egde.equals("e_banners_place_cat")) {

							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE, egde, fromtbl,
									fromcol, fromNode, totbl, tocol, toNode);

							if (x == false) {
								String Sql = "create edge " + egde + " from(select from " + fromtbl + " where "
										+ fromcol + "= '" + fromNode + "') to (select from " + totbl + " where " + tocol
										+ "= '" + toNode + "'  )";
								int rsC = Command(Sql, null);
								if (rsC == -2) {
									rsmsg.Code = ResultCode.ReConnect;
									return rsmsg;
								}
							}
						}
						if (egde.equals("e_stock_store")) {
							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE, "e_stock_store",
									"pm_currentinstock", "recordid", fromNode, "pm_store", "storeid", toNode);

							// List<String> lsStore = productHelper.GetStoreListOfNode(fromNode);
							if (x == false) {
								String Sql = "create edge e_stock_store from(select from pm_currentinstock where recordid= "
										+ fromNode + ") to (select from pm_store where storeid= " + toNode + "  )";

								int rsC = Command(Sql, null);
								if (rsC == -2) {
									rsmsg.Code = ResultCode.ReConnect;
									return rsmsg;
								}
								Logs.Log(isLog, strNOTE, Sql);
							}
						}
//						if (egde.equals("e_code_price")) {
//
//							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE, "e_code_price",
//									"pm_product", "productid", fromNode, "product_price", "recordid", toNode);
//
//							// List<String> lsCOde = productHelper.GetPriceListOfNode(fromNode);
//							if (x == false) {
//								String Sql = "create edge e_code_price from(select from pm_product where productid= '"
//										+ fromNode + "') to (select from product_price where recordid= '" + toNode
//										+ "')";
//
//								int rsC = Command(Sql, null);
//								if (rsC == -2) {
//									rsmsg.Code = ResultCode.ReConnect;
//									return rsmsg;
//								}
//								Logs.Log(isLog, strNOTE, Sql);
//							}
//						}
//						if (egde.equals("e_code_promotion")) {
//
//							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE,
//									"e_code_promotion", "pm_product", "productid", fromNode, "product_promotion",
//									"recordid", toNode);
//
//							// List<String> lsCOde = productHelper.GetPriceListOfNode(fromNode);
//							if (x == false) {
//								String Sql = "create edge e_code_promotion from(select from pm_product where productid= '"
//										+ fromNode + "') to (select from product_promotion where recordid= '" + toNode
//										+ "')";
//
//								int rsC = Command(Sql, null);
//								if (rsC == -2) {
//									rsmsg.Code = ResultCode.ReConnect;
//									return rsmsg;
//								}
//								Logs.Log(isLog, strNOTE, Sql);
//							}
//						}
						// V2
						if (egde.equals("e_codeprice")) {

							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE, "e_codeprice",
									"pm_product", "productid", fromNode, "productprice", "recordid", toNode);

							// List<String> lsCOde = productHelper.GetPriceListOfNode(fromNode);
							if (x == false) {
								String Sql = "create edge e_codeprice from(select from pm_product where productid= '"
										+ fromNode + "') to (select from productprice where recordid= '" + toNode
										+ "')";

								int rsC = Command(Sql, null);
								if (rsC == -2) {
									rsmsg.Code = ResultCode.ReConnect;
									return rsmsg;
								}
								Logs.Log(isLog, strNOTE, Sql);
							}
						}
						if (egde.equals("e_codepromotion")) {
							// timer.reset("e_codepromotion");
							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE, "e_codepromotion",
									"pm_product", "productid", fromNode, "productpromotion", "recordid", toNode);
							// timer.end();
							// List<String> lsCOde = productHelper.GetPriceListOfNode(fromNode);
							if (x == false) {
								String Sql = "create edge e_codepromotion from(select from pm_product where productid= '"
										+ fromNode + "') to (select from productpromotion where recordid= '" + toNode
										+ "')";
								// timer.reset("create edge");
								int rsC = Command(Sql, null);
								// timer.end();
								// ;
								if (rsC == -2) {
									rsmsg.Code = ResultCode.ReConnect;
									return rsmsg;
								}
								Logs.Log(isLog, strNOTE, Sql);
							}
						}

						// end V2
						if (egde.equals("e_productold_code")) {
							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE, egde,
									"product_old_detail", "oldid", fromNode, "product_old_code", "productcode", toNode);
							if (x == false) {
								String Sql = "create edge " + egde
										+ " from(select from product_old_detail where oldid=  " + fromNode
										+ " ) to (select from product_old_code where productcode= '" + toNode + "')";
								int rsC = Command(Sql, null);
								if (rsC == -2) {
									rsmsg.Code = ResultCode.ReConnect;
									return rsmsg;
								}
								Logs.Log(isLog, strNOTE, Sql);
							}
						}
						if (egde.equals("e_productold_code_promotion")) {
							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE, egde,
									"product_old_code", "productcode", fromNode, "product_old_promotion", "recordid",
									toNode);
							if (x == false) {
								String Sql = "create edge " + egde
										+ " from(select from product_old_code where productcode=  '" + fromNode
										+ "' ) to (select from product_old_promotion where recordid= '" + toNode + "')";
								int rsC = Command(Sql, null);
								if (rsC == -2) {
									rsmsg.Code = ResultCode.ReConnect;
									return rsmsg;
								}
								Logs.Log(isLog, strNOTE, Sql);
							}
						}

						if (egde.equals("e_productold_imei_promotion")) {
							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE, egde,
									"product_old_detail", "oldid", fromNode, "product_old_promotion", "recordid",
									toNode);
							if (x == false) {
								String Sql = "create edge  " + egde
										+ " from(select from product_old_detail where oldid=  " + fromNode
										+ " ) to (select from product_old_promotion where recordid= '" + toNode + "')";
								int rsC = Command(Sql, null);
								if (rsC == -2) {
									rsmsg.Code = ResultCode.ReConnect;
									return rsmsg;
								}
								Logs.Log(isLog, strNOTE, Sql);
							}
						}

						if (egde.equals("e_product_pricedefault")) {
							// List<String> lsProductID = productHelper.GetPriceDefaultListOfNode(fromNode);

							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE,
									"e_product_pricedefault", "product", "productid", fromNode, "price_default",
									"recordid", toNode);
							if (x == false) {
								String Sql = "create edge e_product_pricedefault from(select from product where productid=  "
										+ fromNode + " ) to (select from price_default where recordid= '" + toNode
										+ "')";
								int rsC = Command(Sql, null);
								if (rsC == -2) {
									rsmsg.Code = ResultCode.ReConnect;
									return rsmsg;
								}
								Logs.Log(isLog, strNOTE, Sql);
							}
						}
						if (egde.equals("e_pricedefault")) {

							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE, "e_pricedefault",
									"product", "productid", fromNode, "pricedefault", "recordid", toNode);
							if (x == false) {
								String Sql = "create edge e_pricedefault from(select from product where productid=  "
										+ fromNode + " ) to (select from pricedefault where recordid= '" + toNode
										+ "')";
								int rsC = Command(Sql, null);
								if (rsC == -2) {
									rsmsg.Code = ResultCode.ReConnect;
									return rsmsg;
								}
								Logs.Log(isLog, strNOTE, Sql);
							}
						}
						if (egde.equals("e_product_manu")) {

							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE, "e_product_manu",
									"product", "productid", fromNode, "product_manu", "manufacturerid", toNode);
							if (x == false) {
								String Sql = "create edge e_product_manu from(select from product where productid= "
										+ fromNode + ") to (select from product_manu where manufacturerid= " + toNode
										+ "  )";
								int rsC = Command(Sql, null);
								if (rsC == -2) {
									rsmsg.Code = ResultCode.ReConnect;
									return rsmsg;
								}
								Logs.Log(isLog, strNOTE, Sql);
							}
							// List<String> lsManu = productHelper.GetManuListOfNode(fromNode);

//							if (lsManu.size() <= 0) {
//								String Sql = "create edge e_product_manu from(select from product where productid= "
//										+ fromNode + ") to (select from product_manu where manufacturerid= " + toNode
//										+ "  )";
//								int rsC = Command(Sql, null);
//								if (rsC == -2) {
//									rsmsg.Code = ResultCode.ReConnect;
//									return rsmsg;
//								}
//								Logs.Log(isLog, strNOTE, Sql);
//							} else if (lsManu.size() > 0) {
//								if (!lsManu.contains(toNode)) {
//									String delSql = "delete EDGE e_product_manu where out IN (select from product where productid="
//											+ fromNode + ")    ";
//									int rsC = Command(delSql, null);
//									if (rsC == -2) {
//										rsmsg.Code = ResultCode.ReConnect;
//										return rsmsg;
//									}
//									String Sql = "create edge e_product_manu from(select from product where productid= "
//											+ fromNode + ") to (select from product_manu where manufacturerid= "
//											+ toNode + "  )";
//									rsC = Command(Sql, null);
//									if (rsC == -2) {
//										rsmsg.Code = ResultCode.ReConnect;
//										return rsmsg;
//									}
//									Logs.Log(isLog, strNOTE, Sql);
//								}
//								
//							}
						}
						if (egde.equals("e_product_category")) {

							boolean x = iSEdgeExist(message, item.Params, datacenter, isLog, strNOTE,
									"e_product_category", "product", "productid", fromNode, "product_category",
									"categoryid", toNode);
							if (x == false) {
								String Sql = "create edge e_product_category from(select from product where productid= "
										+ fromNode + ") to (select from product_category where categoryid= " + toNode
										+ "  )";
								int rsC = Command(Sql, null);
								if (rsC == -2) {
									rsmsg.Code = ResultCode.ReConnect;
									return rsmsg;
								}
								Logs.Log(isLog, strNOTE, Sql);
							}
//							List<String> lsManu = productHelper.GetCateListOfNode(fromNode);
//
//							if (lsManu.size() <= 0) {
//								String Sql = "create edge e_product_category from(select from product where productid= "
//										+ fromNode + ") to (select from product_category where categoryid= " + toNode
//										+ "  )";
//								int rsC = Command(Sql, null);
//								if (rsC == -2) {
//									rsmsg.Code = ResultCode.ReConnect;
//									return rsmsg;
//								}
//								Logs.Log(isLog, strNOTE, Sql);
//							} else if (lsManu.size() > 0) {
//								if (!lsManu.contains(toNode)) {
//									String delSql = "delete EDGE e_product_category where out IN (select from product where productid="
//											+ fromNode + ")    ";
//									int rsC = Command(delSql, null);
//									if (rsC == -2) {
//										rsmsg.Code = ResultCode.ReConnect;
//										return rsmsg;
//									}
//									String Sql = "create edge e_product_category from(select from product where productid= "
//											+ fromNode + ") to (select from product_category where categoryid= "
//											+ toNode + "  )";
//									rsC = Command(Sql, null);
//									if (rsC == -2) {
//										rsmsg.Code = ResultCode.ReConnect;
//										return rsmsg;
//									}
//									Logs.Log(isLog, strNOTE, Sql);
//								}
//							}

						}

					}
					if (message.DataCenter <= 1 || message.DataCenter == 2 || message.DataCenter == 3
							|| message.DataCenter == 4) {
						if (item.Type == SqlInfoType.REQUIRE_RUN_SEOURL_CATE
								|| item.Type == SqlInfoType.REQUIRE_RUN_SEOURL_MANU
								|| item.Type == SqlInfoType.REQUIRE_RUN_SEOURL_PROP_VALUE) {
							// if (item.Type ==99) {
							String olanguageid = (String) item.Params.get("languageid");
							int ositeid = Integer
									.valueOf(Utils.GetNumberString(String.valueOf(item.Params.get("siteid"))));
							int oid = Integer
									.valueOf(Utils.GetNumberString(String.valueOf(item.Params.get("objectid"))));

							if (oid > 0) {
//								String qu = "gr.dc4.didx.product";
//								if (message.DataCenter == 3) {
//									qu = "gr.beta.didx.product";
//								}

								String queue = "gr.dc4.didx.product";
								String queueBK = "gr.dc2.didx.product";
								String queueDev = "gr.beta.didx.product";

								MessageQueue nwmsg = new MessageQueue();
								nwmsg.Action = DataAction.Update;
								nwmsg.ClassName = "mwg.wb.pkg.product.SeoUrl";
								nwmsg.CreatedDate = Utils.GetCurrentDate();
								nwmsg.Lang = olanguageid;
								nwmsg.SiteID = ositeid;
								nwmsg.DataCenter = message.DataCenter;
								nwmsg.Identify = String.valueOf(oid);
								nwmsg.Type = item.Type;
								nwmsg.Source = item.Type == SqlInfoType.REQUIRE_RUN_SEOURL_CATE
										? "product_category_lang"
										: item.Type == SqlInfoType.REQUIRE_RUN_SEOURL_MANU ? "product_manu_lang"
												: "product_propvalue_lang";
								Logs.Log(isLog, strNOTE, "push upsert=> seourl ");

								nwmsg.Note = strNOTE;
								try {

									QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK,
											queueDev, nwmsg, isLog, strNOTE, nwmsg.DataCenter);

//									if (isLog) {
//										QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushPriority(qu, nwmsg,
//												10);
//									} else {
//										QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).Push(qu, nwmsg);
//									}

								} catch (Exception e) {
									Logs.LogException(e);
									rsmsg.Code = ResultCode.Retry;
									Logs.Log(isLog, strNOTE, "push upsert=> seourl " + e.getMessage());
									return rsmsg;
								}
							}
						}
					}
				}
			}
			if (message.DataCenter <= 1 || message.DataCenter == 2 || message.DataCenter == 3
					|| message.DataCenter == 4) {

				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_PRODUCTSE_FROM_INJECT) {
					String queue = "gr.dc4.didx.productse";
					String queueBK = "gr.dc2.didx.productse";
					String queueDev = "gr.beta.didx.productse";

					MessageQueue messageSe = new MessageQueue();
					if (Utils.StringIsEmpty(message.Identify)) {
						rsmsg.Code = ResultCode.Success;
						return rsmsg;
					}
					long pid = Utils.toLong(message.Identify);
					if (pid > 0) {

						int h = Utils.GetQueueNum(message.Hash, pid);
						queue = "gr.dc4.didx.product" + h;
						queueBK = "gr.dc2.didx.product" + h;
						queueDev = "gr.beta.didx.product";

						messageSe.Action = DataAction.Update;
						messageSe.Note = strNOTE;
						messageSe.Identify = message.Identify;
						messageSe.ClassName = "ms.productse.ProductSE";
						messageSe.CreatedDate = Utils.GetCurrentDate();
						messageSe.ID = 0;
						messageSe.SiteID = message.SiteID;
						messageSe.Lang = message.Lang;
						messageSe.DataCenter = message.DataCenter;

						Logs.Log(isLog, strNOTE, "push upsert=> productse ");
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev,
								messageSe, isLog, strNOTE, messageSe.DataCenter);

					}
				}

				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_COMMON) {
					String queue = "gr.dc4." + message.RepushQueue;
					String queueBK = "gr.dc2." + message.RepushQueue;
					String queueDev = "gr.beta." + message.RepushQueue;

					MessageQueue messageSe = new MessageQueue();

					messageSe.Action = DataAction.Update;
					messageSe.Note = strNOTE;
					messageSe.Identify = message.Identify;
					messageSe.ClassName = message.RepushClassName;
					messageSe.CreatedDate = Utils.GetCurrentDate();
					messageSe.ID = message.ID;
					messageSe.SiteID = message.SiteID;
					messageSe.Lang = message.Lang;
					messageSe.DataCenter = message.DataCenter;

					Logs.Log(isLog, strNOTE, "push upsert=>  " + message.RepushQueue);
					QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev,
							messageSe, isLog, strNOTE, messageSe.DataCenter);

				}
				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_GAMEAPPSE_FROM_INJECT) {
					String queue = "gr.dc4.didx.gameapp";
					String queueBK = "gr.dc2.didx.gameapp";
					String queueDev = "gr.beta.didx.gameapp";

					MessageQueue messageSe = new MessageQueue();
					if (Utils.StringIsEmpty(message.Identify)) {
						rsmsg.Code = ResultCode.Success;
						return rsmsg;
					}
					long pid = Utils.toLong(message.Identify);
					if (pid > 0) {
						messageSe.Action = DataAction.Update;
						messageSe.Note = strNOTE;
						messageSe.Identify = message.Identify;
						messageSe.ClassName = "ms.gameapp.GameAppSE";
						messageSe.CreatedDate = Utils.GetCurrentDate();
						messageSe.ID = 0;
						messageSe.SiteID = message.SiteID;
						messageSe.Lang = message.Lang;
						messageSe.DataCenter = message.DataCenter;

						Logs.Log(isLog, strNOTE, "push upsert=> GameAppSE ");
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev,
								messageSe, isLog, strNOTE, messageSe.DataCenter);

					}
				}

				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_COOKSE_FROM_INJECT) {
					String queue = "gr.dc4.didx.cook";
					String queueBK = "gr.dc2.didx.cook";
					String queueDev = "gr.beta.didx.cook";

					MessageQueue messageSe = new MessageQueue();
					if (Utils.StringIsEmpty(message.Identify)) {
						rsmsg.Code = ResultCode.Success;
						return rsmsg;
					}
					long pid = Utils.toLong(message.Identify);
					if (pid > 0) {
						messageSe.Action = DataAction.Update;
						messageSe.Note = strNOTE;
						messageSe.Identify = message.Identify;
						messageSe.ClassName = "ms.cook.CookSE";
						messageSe.CreatedDate = Utils.GetCurrentDate();
						messageSe.ID = 0;
						messageSe.SiteID = message.SiteID;
						messageSe.Lang = message.Lang;
						messageSe.DataCenter = message.DataCenter;

						Logs.Log(isLog, strNOTE, "push upsert=> CookSE ");
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev,
								messageSe, isLog, strNOTE, messageSe.DataCenter);

					}
				}

				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_PRODUCT_OLD_FROM_INJECT) {
					long oldid = 0;
					MessageQueue nwmsg = new MessageQueue();
					oldid = Utils.toLong(message.Identify);

					if (oldid > 0) {

						String queue = "gr.dc4.didx.productold";
						String queueBK = "gr.dc2.didx.productold";
						String queueDev = "gr.beta.didx.productold";
						if (sqlType.equals("P-OLD-DEL")) {
							nwmsg.Action = DataAction.Delete;

						} else {
							nwmsg.Action = DataAction.Update;
						}

						nwmsg.ClassName = "mwg.wb.pkg.productold.ProductOld";
						nwmsg.CreatedDate = Utils.GetCurrentDate();
						nwmsg.Lang = message.Lang;
						nwmsg.SiteID = message.SiteID;

						nwmsg.DataCenter = message.DataCenter;
						nwmsg.Identify = String.valueOf(oldid);
						Logs.Log(isLog, strNOTE, "push upsert=> ProductOld ");

						nwmsg.Note = strNOTE;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev,
								nwmsg, isLog, strNOTE, nwmsg.DataCenter);

					}
				}
				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_USER_GALLERY_FROM_INJECT) {
					long picid = 0;
					MessageQueue nwmsg = new MessageQueue();
					picid = Utils.toLong(message.Identify);

					if (picid > 0) {

						String queue = "gr.dc4.didx.product";
						String queueBK = "gr.dc2.didx.product";
						String queueDev = "gr.beta.didx.product";

						nwmsg.Action = DataAction.Update;
						nwmsg.ClassName = "mwg.wb.pkg.product.ProductUserGallery";
						nwmsg.CreatedDate = Utils.GetCurrentDate();
						nwmsg.Lang = message.Lang;
						nwmsg.SiteID = message.SiteID;

						nwmsg.DataCenter = message.DataCenter;
						nwmsg.Identify = String.valueOf(picid);
						Logs.Log(isLog, strNOTE, "push upsert=> ProductUserGallery ");

						nwmsg.Note = strNOTE;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev,
								nwmsg, isLog, strNOTE, nwmsg.DataCenter);

					}
				}
				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_GAMEAPP_VIEWCOUNT) {

					if (Utils.StringIsEmpty(message.Identify)) {
						rsmsg.Code = ResultCode.Success;
						return rsmsg;
					}

					long picid = 0;
					MessageQueue nwmsg = new MessageQueue();
					picid = Utils.toLong(message.Identify);

					if (picid > 0) {

						String queue = "gr.dc4.didx.gameapp";
						String queueBK = "gr.dc2.didx.gameapp";
						String queueDev = "gr.beta.didx.gameapp";
						nwmsg.Action = DataAction.Update;
						nwmsg.ClassName = "ms.gameapp.GameAppSE";
						nwmsg.CreatedDate = Utils.GetCurrentDate();
						nwmsg.Lang = message.Lang;
						nwmsg.SiteID = message.SiteID;
						nwmsg.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_GAMEAPP_VIEWCOUNT;
						nwmsg.Data = message.Data;//
						nwmsg.DataCenter = message.DataCenter;
						nwmsg.Identify = String.valueOf(picid);
						Logs.Log(isLog, strNOTE, "push upsert=> news REQUIRE_PUSH_RUN_UPDATE_GAMEAPP_VIEWCOUNT ");
						// Logs.WriteLine( "push upsert=> news viewcounter to se ");
						nwmsg.Note = strNOTE;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev,
								nwmsg, isLog, strNOTE, nwmsg.DataCenter);

					}
				}
				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_COOK_VIEWCOUNT) {

					if (Utils.StringIsEmpty(message.Identify)) {
						rsmsg.Code = ResultCode.Success;
						return rsmsg;
					}

					long picid = 0;
					MessageQueue nwmsg = new MessageQueue();
					picid = Utils.toLong(message.Identify);

					if (picid > 0) {

						String queue = "gr.dc4.didx.cook";
						String queueBK = "gr.dc2.didx.cook";
						String queueDev = "gr.beta.didx.cook";
						nwmsg.Action = DataAction.Update;
						nwmsg.ClassName = "ms.cook.CookSE";
						nwmsg.CreatedDate = Utils.GetCurrentDate();
						nwmsg.Lang = message.Lang;
						nwmsg.SiteID = message.SiteID;
						nwmsg.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_COOK_VIEWCOUNT;
						nwmsg.Data = message.Data;//
						nwmsg.DataCenter = message.DataCenter;
						nwmsg.Identify = String.valueOf(picid);
						Logs.Log(isLog, strNOTE, "push upsert=> news REQUIRE_PUSH_RUN_UPDATE_COOK_VIEWCOUNT ");

						nwmsg.Note = strNOTE;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev,
								nwmsg, isLog, strNOTE, nwmsg.DataCenter);

					}
				}
				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_VIEWCOUNT) {
					long picid = 0;
					MessageQueue nwmsg = new MessageQueue();
					picid = Utils.toLong(message.Identify);

					if (picid > 0) {

						String queue = "gr.dc4.didx.newsviewcount";
						String queueBK = "gr.dc2.didx.newsviewcount";
						String queueDev = "gr.beta.didx.newsviewcount";

						nwmsg.Action = DataAction.Update;
						nwmsg.ClassName = "mwg.wb.pkg.news.NewSE";
						nwmsg.CreatedDate = Utils.GetCurrentDate();
						nwmsg.Lang = message.Lang;
						nwmsg.SiteID = message.SiteID;
						nwmsg.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_VIEWCOUNT;
						nwmsg.Data = message.Data;// chưa viewcounter
						nwmsg.DataCenter = message.DataCenter;
						nwmsg.Identify = String.valueOf(picid);
						Logs.Log(isLog, strNOTE, "push upsert=> REQUIRE_PUSH_RUN_UPDATE_VIEWCOUNT");
						// Logs.WriteLine( "push upsert=> news viewcounter to se ");
						nwmsg.Note = strNOTE;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev,
								nwmsg, isLog, strNOTE, nwmsg.DataCenter);

					}
				}
				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_NEWSSE_FROM_INJECT) {
					long picid = 0;
					MessageQueue nwmsg = new MessageQueue();
					picid = Utils.toLong(message.Identify);

					if (picid > 0) {

						String queue = "gr.dc4.didx.news";
						String queueBK = "gr.dc2.didx.news";
						String queueDev = "gr.beta.didx.news";

						nwmsg.Action = DataAction.Update;
						nwmsg.ClassName = "mwg.wb.pkg.news.NewSE";
						nwmsg.CreatedDate = Utils.GetCurrentDate();
						nwmsg.Lang = message.Lang;
						nwmsg.SiteID = message.SiteID;

						nwmsg.DataCenter = message.DataCenter;
						nwmsg.Identify = String.valueOf(picid);
						Logs.Log(isLog, strNOTE, "push upsert=> REQUIRE_PUSH_RUN_NEWSSE_FROM_INJECT ");

						nwmsg.Note = strNOTE;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev,
								nwmsg, isLog, strNOTE, nwmsg.DataCenter);

					}
				}

				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_KEYWORDSUGGESTNEW_FROM_INJECT) {
					long picid = 0;
					MessageQueue nwmsg = new MessageQueue();
					picid = Utils.toLong(message.Identify);

					if (picid > 0) {

						String queue = "gr.dc4.didx.news";
						String queueBK = "gr.dc2.didx.news";
						String queueDev = "gr.beta.didx.news";

						nwmsg.Action = DataAction.Update;
						nwmsg.ClassName = "mwg.wb.pkg.news.NewsSuggestKeyword";
						nwmsg.CreatedDate = Utils.GetCurrentDate();
						nwmsg.Lang = message.Lang;
						nwmsg.SiteID = message.SiteID;

						nwmsg.DataCenter = message.DataCenter;
						nwmsg.Identify = String.valueOf(picid);
						Logs.Log(isLog, strNOTE, "push upsert=> REQUIRE_PUSH_RUN_KEYWORDSUGGESTNEW_FROM_INJECT ");

						nwmsg.Note = strNOTE;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev,
								nwmsg, isLog, strNOTE, nwmsg.DataCenter);

					}
				}


				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_FAQ_FROM_INJECT) {
					long picid = 0;
					MessageQueue nwmsg = new MessageQueue();
					picid = Utils.toLong(message.Identify);

					if (picid > 0) {

						String queue = "gr.dc4.didx.faq";
						String queueBK = "gr.dc2.didx.faq";
						String queueDev = "gr.beta.didx.faq";

						nwmsg.Action = DataAction.Update;
						nwmsg.ClassName = "mwg.wb.pkg.news.Faq";
						nwmsg.CreatedDate = Utils.GetCurrentDate();
						nwmsg.Lang = message.Lang;
						nwmsg.SiteID = message.SiteID;

						nwmsg.DataCenter = message.DataCenter;
						nwmsg.Identify = String.valueOf(picid);
						Logs.Log(isLog, strNOTE, "push upsert=> REQUIRE_PUSH_RUN_FAQ_FROM_INJECT ");

						nwmsg.Note = strNOTE;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev,
								nwmsg, isLog, strNOTE, nwmsg.DataCenter);

					}
				}
				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_NEWSEVENT_FROM_INJECT) {
					long picid = 0;
					MessageQueue nwmsg = new MessageQueue();
					picid = Utils.toLong(message.Identify);

					if (picid > 0) {

						String queue = "gr.dc4.didx.news";
						String queueBK = "gr.dc2.didx.news";
						String queueDev = "gr.beta.didx.news";

						nwmsg.Action = DataAction.Update;
						nwmsg.ClassName = "mwg.wb.pkg.news.NewsEvent";
						nwmsg.CreatedDate = Utils.GetCurrentDate();
						nwmsg.Lang = message.Lang;
						nwmsg.SiteID = message.SiteID;

						nwmsg.DataCenter = message.DataCenter;
						nwmsg.Identify = String.valueOf(picid);
						Logs.Log(isLog, strNOTE, "push upsert=> REQUIRE_PUSH_RUN_NEWSEVENT_FROM_INJECT ");

						nwmsg.Note = strNOTE;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev,
								nwmsg, isLog, strNOTE, nwmsg.DataCenter);

					}
				}

				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_NEWSTOPIC_FROM_INJECT) {
					long picid = 0;
					MessageQueue nwmsg = new MessageQueue();
					picid = Utils.toLong(message.Identify);

					if (picid > 0) {

						String queue = "gr.dc4.didx.news";
						String queueBK = "gr.dc2.didx.news";
						String queueDev = "gr.beta.didx.news";

						nwmsg.Action = DataAction.Update;
						nwmsg.ClassName = "mwg.wb.pkg.news.NewTopic";
						nwmsg.CreatedDate = Utils.GetCurrentDate();
						nwmsg.Lang = message.Lang;
						nwmsg.SiteID = message.SiteID;

						nwmsg.DataCenter = message.DataCenter;
						nwmsg.Identify = String.valueOf(picid);
						Logs.Log(isLog, strNOTE, "push upsert=> REQUIRE_PUSH_RUN_NEWSTOPIC_FROM_INJECT ");

						nwmsg.Note = strNOTE;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev,
								nwmsg, isLog, strNOTE, nwmsg.DataCenter);

					}
				}

//				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_PRICE
//						|| message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_STOCK
//						|| message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_INJECT
//						|| message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_STOCK_BHX
//						|| message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_PROMOTION
//						|| message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_CODESTOCK
//
//				) {
				long ProductID = 0;

				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_PRICE
						|| message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_PROMOTION
						|| message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_CODESTOCK) {
					ProductID = Utils.toLong(message.Identify);
				}
				if (message.Type == 2) {
					if(Utils.StringIsEmpty(message.Identify)) {
						return   rsmsg;
					}
					ProductID = productHelper.GetProductIDByProductCodeFromCache(message.Identify);

				}
				if (message.Type == 11) {
					ProductID = productHelper.GetProductIDByProductCode(message.Identify);

				}
				if (requireRUNSTATUS) {
					ProductID = Utils.toLong(message.RefIdentify);

				}
				if (ProductID == 99999 || ProductID == 9999 || ProductID == 999999999) {
					// day la nhung code map tam
					return rsmsg;
				}

				if (requireRUNSTATUS || message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_PRICE
						|| message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_STOCK_BHX
						|| message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_PROMOTION
						|| message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_CODESTOCK) {
					if (ProductID > 0) {
						String qu = "gr.dc4.didx.status";
						String qubeta = "gr.beta.didx.status";
						String qubk = "gr.dc2.didx.status";

						int h = (int) (ProductID % 3);

						qu = "gr.dc4.didx.status" + h;
						qubeta = "gr.beta.didx.status" + h;
						qubk = "gr.dc2.didx.status" + h;
						MessageQueue nwmsg = new MessageQueue();
						nwmsg.Action = DataAction.Update;
						nwmsg.ClassName = "mwg.wb.pkg.price.Status";
						nwmsg.CreatedDate = Utils.GetCurrentDate();
						nwmsg.Lang = message.Lang;
						// sao bỏ nhỉ
						nwmsg.SiteID = message.SiteID;
						nwmsg.Source = "upsert";
						nwmsg.BrandID = message.BrandID;
						nwmsg.ProvinceID = message.ProvinceID;
						nwmsg.Storeid = message.Storeid;
						nwmsg.DataCenter = message.DataCenter;
						nwmsg.Identify = String.valueOf(ProductID);
						Logs.Log(isLog, strNOTE, "push price=> status ");
						// Logs.WriteLine("push " + ProductID + " " + qu);
						nwmsg.Note = strNOTE;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu, qubk, qubeta, nwmsg,
								isLog, strNOTE, message.DataCenter);
					}
				}
				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_SPECIALSALEPROGRAM_FROM_INJECT) {
					if(!Utils.StringIsEmpty(message.Identify)) {
					long ProductID1 = productHelper.GetProductIDByProductCodeFromCache(message.Identify);

					if (ProductID1 > 0) {
						String qu = "gr.dc4.didx.status";
						String qubeta = "gr.beta.didx.status";
						String qubk = "gr.dc2.didx.status";
						int h = (int) (ProductID1 % 3);
						qu = "gr.dc4.didx.status" + h;
						qubeta = "gr.beta.didx.status" + h;
						qubk = "gr.dc2.didx.status" + h;
						MessageQueue nwmsg = new MessageQueue();
						nwmsg.Action = DataAction.Update;
						nwmsg.ClassName = "mwg.wb.pkg.price.Status";
						nwmsg.CreatedDate = Utils.GetCurrentDate();
						nwmsg.Lang = "vi-VN";

						nwmsg.Source = "upsert";
						nwmsg.DataCenter = message.DataCenter;
						nwmsg.Identify = String.valueOf(ProductID1);

						Logs.Log(isLog, strNOTE, "push inject=> REQUIRE_PUSH_RUN_STATUS_SPECIALSALEPROGRAM_FROM_INJECT "
								+ nwmsg.Identify);

						nwmsg.Note = strNOTE;

						nwmsg.SiteID = 1;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu, qubk, qubeta, nwmsg,
								isLog, strNOTE, message.DataCenter);
						nwmsg.SiteID = 2;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu, qubk, qubeta, nwmsg,
								isLog, strNOTE, message.DataCenter);
					}
					}
				}
				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_PRODUCTSTATUS_FROM_INJECT) {
					long ProductID2 = productHelper.GetProductIDByProductCodeFromCache(message.Identify);

					if (ProductID2 > 0 && message.CompanyID <= 2) {// TGDD,DMX truoc
						String qu = "gr.dc4.didx.status";
						String qubeta = "gr.beta.didx.status";
						String qubk = "gr.dc2.didx.status";
						int h = (int) (ProductID2 % 3);
						qu = "gr.dc4.didx.status" + h;
						qubeta = "gr.beta.didx.status" + h;
						qubk = "gr.dc2.didx.status" + h;
						MessageQueue nwmsg = new MessageQueue();
						nwmsg.Action = DataAction.Update;
						nwmsg.ClassName = "mwg.wb.pkg.price.Status";
						nwmsg.CreatedDate = Utils.GetCurrentDate();
						nwmsg.Lang = "vi-VN";

						nwmsg.Source = "upsert";
						nwmsg.DataCenter = message.DataCenter;
						nwmsg.Identify = String.valueOf(ProductID2);
						Logs.Log(isLog, strNOTE,
								"push inject=> REQUIRE_PUSH_RUN_STATUS_PRODUCTSTATUS_FROM_INJECT " + nwmsg.Identify);

						nwmsg.Note = strNOTE;
						// tam thời chạy 2 cái
						nwmsg.SiteID = 1;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu, qubk, qubeta, nwmsg,
								isLog, strNOTE, message.DataCenter);
						nwmsg.SiteID = 2;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu, qubk, qubeta, nwmsg,
								isLog, strNOTE, message.DataCenter);

					} else {
						Logs.Log(isLog, strNOTE,
								"push inject=> GetProductIDByProductCodeFromCache   " + message.Identify);

					}
				}

				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_STOCK
						|| message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_INJECT) {
					MessageQueue messageRepush = new MessageQueue();
					String store_qu = "gr.dc4.didx.stockstore";
					String store_qu2 = "gr.dc4.didx.stockstore";
					String store_qubk = "gr.dc2.didx.stockstore";
					String store_qudev = "gr.beta.didx.stockstore";
					messageRepush.Identify = String.valueOf(ProductID);
					messageRepush.Action = DataAction.Update;
					messageRepush.ClassName = "mwg.wb.pkg.price.StockStore";
					messageRepush.CreatedDate = Utils.GetCurrentDate();
					messageRepush.Type = 0;
					messageRepush.DataCenter = message.DataCenter;
					messageRepush.ProvinceID = message.ProvinceID;
					messageRepush.BrandID = message.BrandID;
					messageRepush.Storeid = message.Storeid;
					messageRepush.Note = message.Note;
					messageRepush.Lang = message.Lang;
					if (message.ProvinceID > 0) {
						Logs.getInstance().Log(isLog, strNOTE, "upsert=>stockstore ", messageRepush);

						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(store_qu, store_qu2, store_qubk,
								store_qudev, messageRepush, isLog, strNOTE, message.DataCenter);
					} else {
						Logs.getInstance().Log(isLog, strNOTE, "upsert=>NOt stockstore  Provice<0", messageRepush);

					}
				}

				// push ngay neu có làm giá

				// chi chay tu stock
				if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_STOCK
						|| message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_INJECT) {
					MessageQueue nwmsg = new MessageQueue();
					String qu = "gr.dc4.didx.codestock";
					String qubeta = "gr.beta.didx.codestock";
					String qubk = "gr.dc2.didx.codestock";

					nwmsg.Action = DataAction.Update;
					nwmsg.ClassName = "mwg.wb.pkg.price.CodeStock";
					nwmsg.CreatedDate = Utils.GetCurrentDate();
					nwmsg.Lang = message.Lang;
					nwmsg.Note = message.Note;
					nwmsg.SiteID = message.SiteID;
					nwmsg.BrandID = message.BrandID;
					nwmsg.ProvinceID = message.ProvinceID;
					nwmsg.Storeid = message.Storeid;
					nwmsg.DataCenter = message.DataCenter;
					nwmsg.Identify = message.Identify;
					nwmsg.Source = message.Source + " Upsert ";

					Logs.getInstance().Log(isLog, strNOTE, "push upsert mesage goc=>  ", message);
					Logs.getInstance().Log(isLog, strNOTE, "push upsert=> codestock", nwmsg);
					if (message.ProvinceID > 0) {
						nwmsg.Note = strNOTE;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu, qubk, qubeta, nwmsg,
								isLog, strNOTE, message.DataCenter);
					} else {
						Logs.getInstance().Log(isLog, strNOTE, "upsert=>NOt stockstore  Provice<0", message);

					}

				}

//					if(message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_SUGGESTSEARCH_FROM_INJECT) {
//						MessageQueue nwmss = new MessageQueue();
//						String qu = "gr.dc4.didx.suggestsearch";
//						String qubeta = "gr.beta.didx.suggestsearch";
//						String qubk = "gr.dc2.didx.suggestsearch";
//
//						MessageQueue messageSe = new MessageQueue();
//						if (Utils.StringIsEmpty(message.Identify)) {
//							rsmsg.Code = ResultCode.Success;
//							return rsmsg;
//						}
//						if (!Utils.StringIsEmpty(message.Identify)) {
//							messageSe.Action = DataAction.Update;
//							messageSe.Note = strNOTE;
//							messageSe.Identify = message.Identify;
//							messageSe.ClassName = "ms.common.SuggestSearch";
//							messageSe.CreatedDate = Utils.GetCurrentDate();
//							messageSe.ID = 0;
//							messageSe.SiteID = message.SiteID;
//							messageSe.Lang = message.Lang;
//							messageSe.DataCenter = message.DataCenter;
//
//							Logs.Log(isLog, strNOTE, "push upsert=> GameAppSE ");
//							QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu, qubk, qubeta,
//									messageSe, isLog, strNOTE, messageSe.DataCenter);
//
//						}
//
//					}
			}

			// }
			if (strNOTE.contains("DIDX_TOP") && source.equals("PROMOTION")) {

				// notifyHelperLog.Notify("Ussert PRO:" + message.DataCenter + ":" +
				// timer.getLogs(), 0);

			}

			if (message.CachedType == 1 && requeireClearcachePid > 0) {
				MessageQueue nwmsg = new MessageQueue();
				String qu = "gr.dc4.didx.clearcache";
				String qubeta = "gr.beta.didx.clearcache";
				String qubk = "gr.dc2.didx.clearcache";

				nwmsg.Action = DataAction.Update;
				nwmsg.ClassName = "mwg.wb.pkg.validate.ClearCache";
				nwmsg.CreatedDate = Utils.GetCurrentDate();
				nwmsg.Lang = message.Lang;
				nwmsg.CachedType = message.CachedType;
				nwmsg.Note = message.Note;
				nwmsg.SiteID = message.SiteID;
				nwmsg.DataCenter = message.DataCenter;
				nwmsg.Identify = String.valueOf(requeireClearcachePid);
				nwmsg.RefIdentify = message.RefIdentify;
				nwmsg.Source = message.Source + " Upsert ";
				// timer.reset("push clearcache");
				Logs.getInstance().Log(isLog, strNOTE, "upsert=>push clearcache", nwmsg);

				QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu, qubk, qubeta, nwmsg, isLog,
						strNOTE, 4);// chi gui 1 cho
				// timer.end();

			}

		} catch (Throwable ex) {
			Logs.LogException(ex);
			rsmsg.Code = ResultCode.Retry;
//			if (ex.getMessage().contains("NETWORK_BINARY_MAX_CONTENT_LENGTH ")) {
//				Logs.getInstance().Log(true, "DIDX_LOG|NETWORK_BINARY_MAX_CONTENT_LENGTH",
//						"NETWORK_BINARY_MAX_CONTENT_LENGTH", message);
//
//				rsmsg.Code = ResultCode.Success;
//			}

		}
		return rsmsg;

	}

	public int Command(String Sql, Map<String, Object> Params) throws Throwable {

		String rs = "0";
		if (Params == null)
			Params = new HashMap<String, Object>();
		if (Sql.contains("e_code_stock")) {
			return 1;

		}

		if (Sql.contains("e_product_productold")) {
			return 1;

		}
//		if (Sql.contains("product_language_slave")||Sql.contains("recordid=847016")) {
//			return 1;
//
//		}
		if (Sql.contains("SET [")) {
			Sql = StringUtils.replace(Sql, "SET [", "SET `");
			Sql = StringUtils.replace(Sql, "]=:", "`=:");
			Sql = StringUtils.replace(Sql, ",[", ",`");// ,[
		}
		rs = factoryWrite.Command(Sql, Params);
		if (rs.equals("1")) {
			return 1;
		} else {
//			if (Sql.contains("update productpromotion SET")) {
//				Logs.getInstance().Log(true, "DIDX_LOG|productpromotion", Sql + "rs", Params);
//			}
			if (rs.contains("maxretry")) {

			} else if (rs.contains("executeWithRetry")) {

			} else if (rs.contains("OConcurrentModificationException")) {

			} else if (rs.contains("OConcurrentModificationException")) {

			} else if (rs.contains("because the version is not the latest")) {

			} else if (rs.contains("No edge has been created because ")) {
				if (Sql.contains("e_product_lang")) {
					Logs.getInstance().Log(true, "DIDX_LOG|error_e_product_lang", Sql + "rs", Params);
				}
				return 1;
			} else if (rs.contains("NETWORK_BINARY_MAX_CONTENT_LENGTH ")) {
				Logs.getInstance().Log(true, "DIDX_LOG|NETWORK_BINARY_MAX_CONTENT_LENGTH", Sql + "rs", Params);

				// return 1;
			}

			else if (rs.contains("found duplicated key")) { // found duplicated key found duplicated key
//com.orientechnologies.orient.core.storage.ORecordDuplicatedException: Cannot index record
				if (Sql.contains("e_product_lang")) {
					Logs.getInstance().Log(true, "DIDX_LOG|error_e_product_lang", Sql + "rs", Params);
				}
//				if(Sql.contains("cook_recipe_steps")) {//xu ly tam
//					
//					
//					return 1;
//				}
				// return 1;
			} else if (rs.contains("OCommandSQLParsingException")) { // found duplicated key 'OCompositeKey
				return 1;
			} else if (rs.contains("Error parsing query")) { // found duplicated key 'OCompositeKey
				return 1;
			}

			// ODistributedRecordLockedExceptionUpdate
			else if (rs.contains("ODistributedRecordLockedException")) {
				FileHelper.AppendAllText("LockedException.txt", rs);
				Logs.WriteLine("ODistributedRecordLockedException");
				Utils.Sleep(5000);
				return -3;
			} else if (rs.contains("ODistributedRecordLockedException")) {
				FileHelper.AppendAllText("LockedException.txt", rs);
				Logs.WriteLine("ODistributedRecordLockedException");
				Utils.Sleep(5000);
				return -3;
			}

			else if (rs.contains("{\"result\":")) {
				if (Sql.contains("e_product_lang")) {
					Logs.getInstance().Log(true, "DIDX_LOG|error_e_product_lang", Sql + "rs", Params);
				}
				return 1;

			} else if (rs.contains("found duplicated key")) {
				if (Sql.contains("e_product_lang")) {
					Logs.getInstance().Log(true, "DIDX_LOG|error_e_product_lang", Sql + "rs", Params);
				}
				// return 1;

			} else if (rs.contains("ORecordDuplicatedException")) {
				// return 1;

			} else if (rs.contains("quorum not reached")) {
				Logs.WriteLine("---sql error quorum retry   " + rs + Sql);
				FileHelper.AppendAllText("errorquorum.txt", rs + Sql);
				Utils.Sleep(5000);
				return -3;

			} else if (rs.contains("Socket closed")) // com.orientechnologies.common.io.OIOException: Socket closed
			{
				Logs.WriteLine(rs + " " + rs + Sql);
				FileHelper.AppendAllText("Socket.txt", rs + Sql);
				return -2;

			} else if (rs.contains("is closed")) // com.orientechnologies.common.io.OIOException: Socket closed
			{
				Logs.WriteLine(rs + " " + rs + Sql);
				FileHelper.AppendAllText("Socket.txt", rs + Sql);
				Utils.Sleep(1300);
				return -2;

			} else {
				Logs.WriteLine("---sql error retry  " + rs + Sql);
				FileHelper.AppendAllText("errorcommit.txt", rs + Sql);
				Utils.Sleep(1300);
			}

			Logs.WriteLine("---sql error retry   " + rs);
			Utils.Sleep(300);
			Logs.LogException("---sql error retry   " + rs);

		}

		return -1;
	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
