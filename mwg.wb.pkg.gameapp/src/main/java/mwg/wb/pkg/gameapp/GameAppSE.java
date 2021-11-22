package mwg.wb.pkg.gameapp;

import java.util.Date;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.MessageQueuePushType;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.Utils;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.notify.LineNotify;
 
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.gameapp.GameAppBO;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.search.FaqSO;

public class GameAppSE implements Ididx {

	private ORThreadLocal factoryRead = null;
	private String currentIndexDB = "ms_gameapp";
	private ClientConfig config = null;
	// public NotifyHelper lineNotify = null;
	private ObjectMapper mapper = null;

	public void InitObject(ObjectTransfer objectTransfer) {
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		config = (ClientConfig) objectTransfer.clientConfig;
		// lineNotify = (LineNotify) objectTransfer.notifyHelper;
		mapper = (ObjectMapper) objectTransfer.mapper;
	}

	public long GetViewCount(boolean isLog, String strNOTE, double productID) {
		OResultSet ls = null;
		long viewcount = 0;
		try {
			  ls = factoryRead.Query("select totalreview from product where productid = " + productID);

			while (ls.hasNext()) {
				// String productlangRID = ls.next().getProperty("productidref").toString();
				viewcount = (long) Double.parseDouble(ls.next().getProperty("totalreview").toString());
				return viewcount;
			}
		}catch (Throwable e) {
			Logs.LogException(e);
			Logs.Log(isLog, strNOTE, "GetViewCount:"+e.getMessage());
			Logs.LogFile("gameappse.txt", "1:" +  e.toString() );
			
		}
		finally {
			if (ls != null)
				ls.close();
		}

		return -1;
	}

	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;
		String strNOTE = message.Note + "";

		boolean isLog = Utils.IsMessageSaveLog(message.Note);

		// if (strNOTE.contains("DIDX_NOTIFY_GAMEAPP_VIEWCOUNT")) {

		// isUpdateViewCount = true;
		// }

		Logs.getInstance().Log(isLog, strNOTE, "gameappse", message);
		;

		// int productID = message.Identify == null ? 0 :
		// Integer.parseInt(message.Identify);
		long productID = Utils.toLong(message.Identify);//
		int siteID = 1;// message.SiteID;

		if (message.Action == DataAction.Add || message.Action == DataAction.Update) {

			if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_GAMEAPP_VIEWCOUNT) {
				// cho site 1 luon
				if (productID > 0) {
					 long ViewCounter = GetViewCount(isLog, strNOTE, productID); 
					 Logs.Log(isLog, strNOTE, "ViewCounter:"+ViewCounter);
	 
						
					var update = new UpdateRequest(currentIndexDB, String.valueOf(productID))
							.doc("{\"ViewCount\":" + ViewCounter + " ,\"DidxSource\":\"viewcount\" }",
									XContentType.JSON)
							.docAsUpsert(true).detectNoop(false);
					var client = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
					UpdateResponse response = null;
					try {
						response = client.update(update, RequestOptions.DEFAULT);

					} catch (Throwable e) {
						r.Code = ResultCode.Retry;
						//e.printStackTrace();
						r.Message = "GameApp #" + productID + " update viewcount: " + e;
						Logs.Log(isLog, strNOTE, r.Message);
						Logs.LogFile("gameappse.txt", "2:" + e.toString());
						Logs.LogException(e);
						return r;
					}
					if (response != null && response.getResult() == Result.UPDATED) {
						r.Code = ResultCode.Success;
						//Logs.Log(isLog, strNOTE,"Success ViewCounter="+ViewCounter);
					} else {

						Logs.Log(isLog, strNOTE, "-Update viewcount in GameApp ES FAILED: ProductID:#" + productID
								+ ", " + response + "######################");
						r.Code = ResultCode.Retry;
						
					}
				}else {
					r.Code = ResultCode.Success;
					r.Message = "ProductID <= 0";
					Logs.Log(isLog, strNOTE, r.Message);
					return r;
				}

			} else {

				Logs.Log(isLog, strNOTE, "productID " + productID);
				if (siteID <= 0) {
					//Logs.WriteLine("SiteID <= 0");
					Logs.Log(isLog, strNOTE, "SiteID <= 0");
					return r;
				}
				if (productID > 0) {
					try {
						var listProduct = factoryRead.QueryFunction("gameapp_GetByIdSE", GameAppBO[].class, false,
								productID, siteID, message.Lang);

						if (listProduct == null || listProduct.length == 0) {
							Logs.LogFile("gameappse.txt", productID + ":listProduct == null");
							r.Code = ResultCode.Success;
							r.Message = "GameApp #" + productID + " does not exist";
							Logs.Log(isLog, strNOTE, r.Message);
							return r;
						}
						Logs.Log(isLog, strNOTE, "GameAppBO.fromBOToSO");
						var product = GameAppBO.fromBOToSO(listProduct[0]);
						product.DidxSource = "IDXALL";
						product.DidxDate = Utils.GetCurrentDate();

						Logs.getInstance().Log(isLog, strNOTE, "bo", product);
						;
						var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST)
								.IndexObject(currentIndexDB, product, product.ProductID + "");
						if (!rs) {
							r.Message = "GameApp #" + productID + " init to ES FAILED";
							r.Code = ResultCode.Retry;
							Logs.Log(isLog, strNOTE, r.Message);
							return r;
						} else {
							Logs.Log(isLog, strNOTE, productID + ":ESOK");
							Logs.LogFile("gameappse.txt", productID + ":ESOK");
							r.Code = ResultCode.Success;
						}
					} catch (Throwable e) {

						r.Code = ResultCode.Retry;
						r.Message = "GameApp #" + productID + " init to ES FAILED";
						r.StackTrace = Logs.GetStacktrace(e);
						Logs.Log(isLog, strNOTE, r.Message);
						Logs.LogFile("gameappse.txt", "3:" + e.toString());
						Logs.LogException(e);
						return r;
					}
				} else {
					r.Code = ResultCode.Success;
					r.Message = "GameApp #" + productID + " does not exist";
					Logs.Log(isLog, strNOTE, r.Message);
					Logs.LogFile("gameappse.txt", productID + ":not exist");
					return r;
				}
			}

		} else if (message.Action == DataAction.Delete) {

			/// xoá
			r.Code = DeleteProductGameApp(productID, r);

		}
		return r;
	}

	private ResultCode DeleteProductGameApp(double productID, ResultMessage r) {

		try {
			var update = new UpdateRequest(currentIndexDB, String.valueOf(productID))
					.doc("{\"IsDeleted\":1  }", XContentType.JSON).docAsUpsert(true).detectNoop(false);
			var client = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
			UpdateResponse response = null;
			try {
				response = client.update(update, RequestOptions.DEFAULT);
			} catch (Throwable e) {
				r.Code = ResultCode.Retry;
				// e.printStackTrace();
				Logs.LogFile("gameappse.txt", "4: " + e.toString());
				Logs.LogException(e);
			}
			if (response != null && response.getResult() == Result.UPDATED) {
				r.Code = ResultCode.Success;
			} else {

				Logs.WriteLine("-Update isdeleted in GameApp ES FAILED: ProductID:#" + productID + ", " + response
						+ "######################");
				Logs.LogFile("gameappse.txt", "5: "+  "-Update isdeleted in GameApp ES FAILED: ProductID:#" + productID + ", ");
				r.Code = ResultCode.Retry;
			}
		} catch (Throwable e) {
			Logs.WriteLine("Update isdeleted in ERRER, " + e);
			r.Code = ResultCode.Retry;
			
			Logs.LogFile("gameappse.txt", "6: "+  e.toString() );
			Logs.LogException(e);

		}
		return r.Code;
	}

	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
