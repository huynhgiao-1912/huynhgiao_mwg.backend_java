package mwg.wb.pkg.common;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.Utils;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.api.ProductCategoryBOApi;
import mwg.wb.model.commonpackage.KeyWordRedirectBO;
import mwg.wb.model.commonpackage.KeyWordRedirectSO;
import mwg.wb.model.products.URLGoogleBO;


public class UrlGoogle implements Ididx {

	private ORThreadLocal factoryRead = null;
	private String currentIndexDB = "ms_googleurl";
	private ClientConfig config = null;
	//public NotifyHelper lineNotify = null;
	private ObjectMapper mapper = null;
	public void InitObject(ObjectTransfer objectTransfer) {
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		config = (ClientConfig) objectTransfer.clientConfig;
		mapper = (ObjectMapper) objectTransfer.mapper;
	}

	public ResultMessage Refresh(MessageQueue message) {
		// TODO Auto-generated method stub
		
		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;
		//if(1==1)return  r;
		String strNOTE = message.Note + "";
		boolean isLog = false;
		if (strNOTE.contains("LOG")) {

			isLog = true;
		}
		Logs.getInstance().Log(isLog, strNOTE, "UrlGoogle", message);
		
		//int KeywordID = Utils.toInt(message.Identify);//
		//int siteID = message.SiteID;

		if(Utils.StringIsEmpty(message.Identify)) {
			r.Code = ResultCode.Success;
			return r;
		}
		
		var UrlID = Integer.parseInt(message.Identify);
        if (UrlID == 0)
        {
        	Logs.Log(isLog, strNOTE, r.Message);
            return r;
        }
		if (message.Action == DataAction.Add || message.Action == DataAction.Update) {
			//if(data > 0) {
				Logs.Log(isLog, strNOTE, message.Identify);
				try {
					
					var googleBO = factoryRead.QueryFunction("google_GetByID", URLGoogleBO[].class, false, UrlID);
					if(googleBO != null && googleBO.length > 0) {
						var tmpURL = googleBO[0];
						
						tmpURL.ProjectName = !Utils.StringIsEmpty(googleBO[0].ProjectName) ? DidxHelper.FormatKeywordAZ(DidxHelper.FilterVietkey(googleBO[0].ProjectName.replace(",", " "))) : "";    
						//DidxHelper.FormatKeywordAZ(DidxHelper.FilterVietkey(field)).toLowerCase()
						
						var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST)
								.IndexObject(currentIndexDB, tmpURL, tmpURL.URLID + "_" + tmpURL.SiteID);
					}
					
					
				}catch (Throwable e) {
					//Logs.Log(true, strNOTE, e.toString());
					Logs.WriteLine("KeywordRedirect: "+  e.toString());
					r.Code = ResultCode.Retry;
					Logs.LogException(e);
					return r;
				}
			
		}else {
			r.Code = DeleteUrlGooogle(message, r);
		}
		
		return r;
	}

	private ResultCode DeleteUrlGooogle(MessageQueue message, ResultMessage r) {

		try {
			var UrlID = Integer.parseInt(message.Identify);
	        if (UrlID == 0)
	        {
	        	r.Code = ResultCode.Success;
	            return r.Code;
	        }
	        var KeywordID = message.Identify + "_" + message.SiteID;
	        
	        /// xử lý tính toán gen ra KeywordID
			var update = new UpdateRequest(currentIndexDB, String.valueOf(KeywordID))
					.doc("{\"IsDeleted\":true  }", XContentType.JSON).docAsUpsert(true).detectNoop(false);
			var client = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
			UpdateResponse response = null;
			try {
				response = client.update(update, RequestOptions.DEFAULT);
			} catch (Exception e) {
				r.Code = ResultCode.Retry;
				e.printStackTrace();
			}
			if (response != null && response.getResult() == Result.UPDATED) {
				r.Code = ResultCode.Success;
			} else {

				Logs.WriteLine("-Update isdeleted in UrlGooogle ES FAILED: KeywordID:#" + KeywordID + ", " + response
						+ "######################");

				r.Code = ResultCode.Retry;
			}
		} catch (Exception e) {
			Logs.WriteLine("Update isdeleted in ERRER, " + e);
			r.Code = ResultCode.Retry;

		}
		return r.Code;
	}

	
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
