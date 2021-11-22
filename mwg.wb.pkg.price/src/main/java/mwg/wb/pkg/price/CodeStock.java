package mwg.wb.pkg.price;

import mwg.wb.business.ProductHelper;
import mwg.wb.business.ResultStockByMainSubGroup;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.MessageQueuePushType;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.ProductErpPriceBO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeStock implements Ididx {

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
	public static Integer[] List_MainGroupid2 = { 484, 305, 304, 1214, 1116, 384 };
	public static Integer[] List_MainGroupid3 = { 13, 244, 22, 16, 17, 23, 184, 244, 484, 525, 764, 905, 1274, 1294,
			364 };

	public static Integer[] ListSubGroup364 = { 1273, 3563, 1131, 1034 };

	public ResultMessage Refresh(MessageQueue message) {
		DataCenter = message.DataCenter;
		ResultMessage msg = new ResultMessage();
		
		String strNOTE = message.Note + "";
//		có 4 nhóm nhu cầu tồn kho tgdd và dmx:
//			1-hàng điện gia dụng - quạt điều hòa quạt sửi: maingroupid(484) subgroupid(3799): lấy tồn kho tgdd + dmx(showweb) + tồn kho ngoài(kho trung tâm) ---sử dụng hàm: stock_getByProvince1
//			2-tồn hàng cồng kềnh maingroupid(484,305,304,1214,1116) site dmx: tồn theo từng site(showweb) + tồn kho ngoài(kho trung tâm)  ---sử dụng hàm: stock_getByProvince2
//			3-tồn kho hàng viễn thông maingroupid(13,244,22,16,17,23,184,244,484,525,764,905,1274,1294): lấy tồn kho tgdd + dmx(showweb) + kho 1488 ---sử dụng hàm: stock_getByProvince3
//			4-còn lại siteid in (1,2): tồn theo từng site(showweb) + tồn kho 1488 ---sử dụng hàm: stock_getByProvince4
//			
		msg.Code = ResultCode.Success;
		String productCode = message.Identify;
		int provinceID = message.ProvinceID;
		int brandid = message.BrandID;

		int siteid = DidxHelper.getSitebyBrandID(brandid);
		String lang = DidxHelper.getLangByBrandID(brandid);
		if (brandid <= 0)
			siteid = message.SiteID;

		boolean isLog = false;

		if (strNOTE.contains("DIDX_TOP_LOG") || strNOTE.contains("DIDX_LOG")) {

			isLog = true;
		}

		Logs.getInstance().Log(isLog, strNOTE, "refresh codestock", message);
		if (siteid != 1 && siteid != 2 && siteid != 6 && siteid != 12) {
			Logs.Log(isLog, strNOTE, "siteid != 1 && siteid != 2 && siteid != 6 && siteid != 12 brand=" + brandid);
			return msg;
		}

		var mainSubGroup = productHelper.GetMainSubGroupByProductCode(productCode);
		if (mainSubGroup == null) {
			Logs.Log(isLog, strNOTE, "mainSubGroup == null");

			return msg;
		}

		if (!productHelper.CheckProductExistFromCache(mainSubGroup.productidref)) {
			Logs.Log(isLog, strNOTE, "CheckProductExistFromCache == false " + mainSubGroup.productidref);

			return msg;
		}
		ResultStockByMainSubGroup rs = null;
		String recordid = "";
		String functionName = "";
//m 244,s 931 p202705

		if (provinceID > 0 && brandid > 0) {
			if (siteid == 1 || siteid == 2) {

				// 364-tồn kho hàng maingroupid = 364 và subgroupid in (1273,3563,1131,1034):
				// lấy tồn kho tgdd + dmx(showweb) + kho 1488 ---sử dụng hàm:
				// stock_getByProvince364
				if (mainSubGroup.maingroupid == 364
						&& Arrays.stream(ListSubGroup364).anyMatch(x -> x == mainSubGroup.subgroupid)) {
					siteid = 2;
					recordid = productCode + "_" + provinceID + "_" + siteid;
					functionName = "stock_getByProvince364";
				} else if (mainSubGroup.maingroupid == 484 && mainSubGroup.subgroupid == 3799) {
					// 1-hàng điện gia dụng - quạt điều hòa quạt sửi: maingroupid(484)
					// subgroupid(3799): lấy tồn kho tgdd + dmx(showweb) + tồn kho ngoài(kho trung
					// tâm) ---sử dụng hàm: stock_getByProvince1
					siteid = 2;
					recordid = productCode + "_" + provinceID + "_" + siteid;
					functionName = "stock_getByProvince1";
				} else if (mainSubGroup.subgroupid == 4324 
						|| Arrays.stream(List_MainGroupid2).anyMatch(x -> x == mainSubGroup.maingroupid)) {
//				2-tồn hàng cồng kềnh maingroupid(484,305,304,1214,1116) site dmx: tồn theo từng site(showweb) + tồn kho ngoài(kho trung tâm)  ---sử dụng hàm: stock_getByProvince2
					siteid = 2;
					recordid = productCode + "_" + provinceID + "_" + siteid;
					functionName = "stock_getByProvince2";
				} else if (Arrays.stream(List_MainGroupid3).anyMatch(x -> x == mainSubGroup.maingroupid)) {
//				3-tồn kho hàng viễn thông maingroupid(13,244,22,16,17,23,184,244,484,525,764,905,1274,1294,364): lấy tồn kho tgdd + dmx(showweb) + kho 1488 ---sử dụng hàm: stock_getByProvince3
					siteid = 2;
					functionName = "stock_getByProvince3";
					recordid = productCode + "_" + provinceID + "_" + siteid;
				} else {
//				4-còn lại siteid in (1,2): tồn theo từng site(showweb) + tồn kho 1488 ---sử dụng hàm: stock_getByProvince4
					recordid = productCode + "_" + provinceID + "_" + siteid;
					functionName = "stock_getByProvince4";
				}
			} else {
				recordid = productCode + "_" + provinceID + "_" + siteid;
				functionName = "stock_getByProvince5";

			}
			try {
				long s1=System.currentTimeMillis();
				Logs.Log(isLog, strNOTE, "GetStockMainSubGroupByProductCode ==   " + functionName + " productCode "
						+ productCode + " siteid" + siteid + " provinceID " + provinceID);
				rs = productHelper.GetStockMainSubGroupByProductCode(functionName, productCode, siteid, provinceID);
				long time=System.currentTimeMillis()-s1;
				if (time > 1000) {
				 
						try {
							Logs.LogFactorySlowMessage2("codestock", "\r\n" + "GetStockMainSubGroupByProductCode ==   " + functionName + " productCode "
									+ productCode + " siteid" + siteid + " provinceID " + provinceID, time);
						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					 
				}
			} catch (Throwable e) {
				Logs.LogException(e);
				msg.Code = ResultCode.Retry;
				return msg;
			}

			if (rs == null) {
				Logs.Log(isLog, strNOTE, " GetStockMainSubGroupByProductCode null ");
				rs = new ResultStockByMainSubGroup();
//				return msg;
			}
			MessageQueue messageRepush = new MessageQueue();
			messageRepush.SqlList = new ArrayList<SqlInfo>();
			SqlInfo sqlinfo = new SqlInfo();

			sqlinfo.Sql = "update codestock  set samplequantity=:samplequantity,  productidref=:productidref,  " +
					"quantity=:quantity, vituralquantity=:vituralquantity,  centerquantity=:centerquantity, " +
					"storechangequantity=:storechangequantity, storechangecenterquantity=:storechangecenterquantity, " +
					"siteid=:siteid,  productcode=:productcode, provinceid=:provinceid,recordid=:recordid   " +
					"upsert where recordid=:recordid  ";
			sqlinfo.Params = new HashMap<String, Object>();

			sqlinfo.Params.put("productcode", productCode);
			sqlinfo.Params.put("provinceid", provinceID);
			sqlinfo.Params.put("recordid", recordid);
			sqlinfo.Params.put("siteid", siteid);
			sqlinfo.Params.put("centerquantity", rs.centerquantity);
			sqlinfo.Params.put("quantity", rs.quantity);
			sqlinfo.Params.put("vituralquantity", rs.vituralquantity);
			sqlinfo.Params.put("productidref", mainSubGroup.productidref);
			sqlinfo.Params.put("samplequantity", rs.samplequantity);
			sqlinfo.Params.put("storechangequantity", rs.storechangequantity);
			sqlinfo.Params.put("storechangecenterquantity", rs.storechangecenterquantity);
			messageRepush.SqlList.add(sqlinfo);

			messageRepush.Source = "CODESTOCK";
			String qu = "gr.dc4.sql.codestock";
			String qu2 = "gr.dc4.sql.codestock";
			String qubk = "gr.dc2.sql.codestock";
			String qudev = "gr.beta.sql.codestock";
			messageRepush.Identify = String.valueOf(mainSubGroup.productidref);
			messageRepush.Action = DataAction.Update;
			messageRepush.ClassName = "mwg.wb.pkg.upsert.Upsert";
			messageRepush.CreatedDate = Utils.GetCurrentDate();
			// messageRepush.SiteID = siteid; site này sai nha dieu gan la 1
			messageRepush.Lang = lang;
			messageRepush.BrandID = brandid;

			messageRepush.ProvinceID = provinceID;
			messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_CODESTOCK;
			messageRepush.DataCenter = DataCenter;
			messageRepush.Note = strNOTE;
			Logs.Log(isLog, strNOTE, " push sql codestock");
			Logs.getInstance().Log(isLog, strNOTE, " push sql codestock", messageRepush);
			try {
				QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush,
						isLog, strNOTE, DataCenter);
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				msg.Code = ResultCode.Retry;
				return msg;
			}

			// th phan mem store 979 20201120
			if (message.Storeid == 979 && siteid == 1) {
				siteid = 2;
				recordid = productCode + "_" + provinceID + "_" + siteid;
				sqlinfo.Params.put("recordid", recordid);
				sqlinfo.Params.put("siteid", siteid);
				messageRepush.BrandID = 2;
				Logs.Log(isLog, strNOTE, " push sql codestock store 979");
				Logs.getInstance().Log(isLog, strNOTE, " push sql codestock 979", messageRepush);
				try {
					QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush,
							isLog, strNOTE, DataCenter);
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					msg.Code = ResultCode.Retry;
					return msg;
				}
			}
		}
		return msg;

	}

	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
