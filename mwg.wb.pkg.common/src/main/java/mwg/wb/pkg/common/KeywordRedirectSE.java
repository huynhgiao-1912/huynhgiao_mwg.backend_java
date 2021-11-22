package mwg.wb.pkg.common;

import mwg.wb.model.products.ProductCategoryBO;
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


public class KeywordRedirectSE implements Ididx {

	private ORThreadLocal factoryRead = null;
	private String currentIndexDB = "ms_keywordredirect";
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
		Logs.getInstance().Log(isLog, strNOTE, "keywordredirect", message);
		
		//int KeywordID = Utils.toInt(message.Identify);//
		//int siteID = message.SiteID;

		var data = message.Identify != null ? message.Identify.split("\\|") : new String[] { };
        if (data == null || data.length <= 1)
        {
        	Logs.Log(isLog, strNOTE, r.Message);
            return r;
        }
		if (message.Action == DataAction.Add || message.Action == DataAction.Update) {
			//if(data > 0) {
				Logs.Log(isLog, strNOTE, message.Identify);
				try {
					
					if (data[0].equals("1"))
                    {
						var KeywordID = Integer.parseInt(data[1]);//data[1];
						var keywordredirectBO = factoryRead.QueryFunction("keywordredirect_getByID", KeyWordRedirectBO[].class, false, KeywordID);
						if(keywordredirectBO != null && keywordredirectBO.length > 0) {
							var dataSE = KeyWordRedirectBO.toKeyWordRedirectSO(keywordredirectBO[0]);
							
							var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST)
									.IndexObject(currentIndexDB, dataSE, "1_" + dataSE.KeywordID);
							if (!rs) {
								r.Message = "KeywordRedirect #" + KeywordID + " init to ES FAILED";
								r.Code = ResultCode.Retry;
								Logs.Log(isLog, strNOTE, r.Message);
								return r;
							} else {
								r.Code = ResultCode.Success;
							}
							
							
						}else {
							r.Code = ResultCode.Success;
							r.Message = "KeywordRedirect #" + KeywordID + " does not exist";
							Logs.Log(isLog, strNOTE, r.Message);
							return r;
						}
                    }else if (data[0].equals("2")) { // category - Nghành hàng
                    	int categoryID = Integer.parseInt(data[1]);

                    	var categoryBO = factoryRead.QueryFunction("keywordredirect_getcategorybyID", ProductCategoryBO[].class, false, categoryID, message.SiteID, "vi-VN");
                    	if(categoryBO != null && categoryBO.length > 0) {
                    		// xử lý cate theo, mỗi site sẽ có cách lấy url khác nhau.
                    		KeyWordRedirectSO kwse = new KeyWordRedirectSO();
                    		kwse.KeywordID = categoryBO[0].CategoryID;
                    		if(message.SiteID == 1) { // tgdd
                    			
                    			String CateName = categoryBO[0].CategoryName.replace(",","").replace(".", "").replace("-", "").replace("\\", "").replace("\\/", "").replace("  ", " ").replace(" - ", " ");
                    			//categoryBO[0].URL = "";
                    			categoryBO[0].URL = !Utils.StringIsEmpty(categoryBO[0].URL) ? categoryBO[0].URL : DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(CateName)).replace(" ", "-");
                    			kwse.UrlTGDD = "https://www.thegioididong.com/" + categoryBO[0].URL;
                        		kwse.UrlDMX = "";
                        		
                    		}else { // dmx
                    			kwse.UrlTGDD = "";
                    			if(Utils.StringIsEmpty(categoryBO[0].URL)) {// nếu url null
                    				String CateName = categoryBO[0].CategoryName.replace(",","").replace(".", "").replace("-", "").replace("\\", "").replace("\\/", "").replace("  ", " ").replace(" - ", " ");
                            		kwse.UrlDMX = "https://www.dienmayxanh.com/" + DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(CateName)).replace(" ", "-");  
                    			}else {
                    				kwse.UrlDMX = categoryBO[0].URL;
                    			}
                    		}
                    		
                    		kwse.Keyword = categoryBO[0].KeywordList;
                    		categoryBO[0].CategoryName = categoryBO[0].CategoryName.replace(",","").replace(".", "").replace("-", "").replace("\\", "").replace("\\/", "").replace("  ", " ").replace(" - ", " ");
                    		kwse.KeywordSE = DidxHelper.GenTermKeyWord(categoryBO[0].CategoryName + "," + (categoryBO[0].KeywordList != null ? categoryBO[0].KeywordList : ""));
                    		kwse.SiteID = message.SiteID + "";
                    		kwse.CreatedDate = categoryBO[0].CreatedDate != null ? categoryBO[0].CreatedDate : Utils.GetDefaultDate();
                    		kwse.IsDeleted = !categoryBO[0].IsActived || categoryBO[0].IsDeleted ? true : false;
                    		kwse.type = 2;
                    		
                    		var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST)
									.IndexObject(currentIndexDB, kwse, "2_" + kwse.KeywordID + "_" + message.SiteID);
                    		
                    	} else {
                    		Logs.WriteLine("KeywordRedirect: categoryID #"+  categoryID + " does not exist ");
                    	}
                    	
                    	
                    }else {
                    	Logs.Log(isLog, strNOTE, message.Identify);
                    	return r;
                    }
					
				}catch (Throwable e) {
					//Logs.Log(true, strNOTE, e.toString());
					Logs.WriteLine("KeywordRedirect: "+  e.toString());
				}
				
//			}else
//			{ // keywordid <= 0
//				r.Code = ResultCode.Success; //
//				Logs.WriteLine("KeywordRedirect: KeywordID < 0, KeywordID: "+  KeywordID);
//				return r;
//			}
			
		}else {
			r.Code = DeleteKeywordRedirect(message, r);
		}
		
		return r;
	}

	private ResultCode DeleteKeywordRedirect(MessageQueue message, ResultMessage r) {

		try {
			var data = message.Identify != null ? message.Identify.split("\\|") : new String[] { };
	        if (data == null || data.length <= 1)
	        {
	        	r.Code = ResultCode.Success;
	        	return r.Code;
	        }
	        var KeywordID = "";
	        if(data[0].equals("1")) {
	        	KeywordID = "1_" + data[1];
	        }else {
	        	KeywordID = "1_" + data[1] + "_" + message.SiteID;
	        }
	        
	        /// xử lý tính toán gen ra KeywordID
			var update = new UpdateRequest(currentIndexDB, String.valueOf(KeywordID))
					.doc("{\"IsDeleted\":1  }", XContentType.JSON).docAsUpsert(true).detectNoop(false);
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

				Logs.WriteLine("-Update isdeleted in KeywordRedirect ES FAILED: KeywordID:#" + KeywordID + ", " + response
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
