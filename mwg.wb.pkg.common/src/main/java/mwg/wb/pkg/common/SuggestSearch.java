package mwg.wb.pkg.common;

import java.util.Date;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.CategoryHelper;
import mwg.wb.business.NewsHelper;
import mwg.wb.business.ProductHelper;
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
import mwg.wb.model.commonpackage.SuggestSearchBO;
import mwg.wb.model.commonpackage.SuggestSearchSO;
import mwg.wb.model.commonpackage.SuggestSearchTypes;
import mwg.wb.model.commonpackage.TimerSeoBO;
import mwg.wb.model.products.ProductCategoryBO;
import mwg.wb.model.products.ProductManuBO;

public class SuggestSearch implements Ididx {
	private ORThreadLocal factoryRead = null;
	private String currentIndexDB = "ms_suggestsearch";
	private ClientConfig config = null;
	private ObjectMapper mapper = null;
	private static CategoryHelper cateHelper = null;
	private static ProductHelper productHelper = null;
	
	public void InitObject(ObjectTransfer objectTransfer) {
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		config = (ClientConfig) objectTransfer.clientConfig;
		mapper = (ObjectMapper) objectTransfer.mapper;
		if(cateHelper == null)
			cateHelper = new CategoryHelper(factoryRead, config);
		if(productHelper == null)
			productHelper = new ProductHelper(factoryRead, config);
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;
		// if(1==1)return r;
		String strNOTE = message.Note + "";
		boolean isLog = false;
		if (strNOTE.contains("LOG")) {

			isLog = true;
		}
		Logs.getInstance().Log(isLog, strNOTE, "TimerSeoSE", message);

		var messageData = !Utils.StringIsEmpty(message.Identify) ?  message.Identify.split("\\|") : new String[] { };
		if(messageData == null || messageData.length == 0) return r;
		var data = 0;
		var messageType = 0;
		
		messageType = Integer.parseInt(messageData[0]);
		data = Integer.parseInt(messageData[1]);
		
		if (data <= 0) {
			Logs.Log(isLog, strNOTE, r.Message);
			return r;
		}
		try {
			if (message.Action == DataAction.Add || message.Action == DataAction.Update) {

				if (messageType == 1) // keyword suggest khai báo
				{
					var keywordBO = factoryRead.QueryFunction("keyword_suggest_getDetail", SuggestSearchBO[].class,
							false, data);
					if (keywordBO != null && keywordBO.length > 0) {
						var dataKeywordSuggest = keywordBO[0];
						SuggestSearchSO suggestSo = new SuggestSearchSO();
						suggestSo.SuggestSearchID = dataKeywordSuggest.KeywordID;
						suggestSo.SuggestSearchType = SuggestSearchTypes.SuggestSearch.getValue();// 2; // category
						suggestSo.SuggestSearchTypeName = "keywordsuggest ";
						suggestSo.KeywordSuggest = dataKeywordSuggest.KeywordSuggest;
						suggestSo.KeywordSearch = dataKeywordSuggest.KeywordSearch.replace(",", " , ");
						//suggestSo.CategoryName_EN = GenTermTitle(dataKeywordSuggest.KeywordSuggest);

						suggestSo.KeywordSuggest_En = DidxHelper
								.FilterVietkey(DidxHelper.FormatKeywordField(dataKeywordSuggest.KeywordSearch))
								.replace(",", " ");
						suggestSo.KeywordSearch_En = DidxHelper
								.FilterVietkey(DidxHelper.FormatKeywordField(dataKeywordSuggest.KeywordSearch))
								.replace(",", " ");

						suggestSo.UrlDMX = dataKeywordSuggest.UrlDMX;
						suggestSo.UrlTGDD = dataKeywordSuggest.UrlTGDD;
						suggestSo.IsActived = dataKeywordSuggest.IsActived;
						suggestSo.ActivedDate = dataKeywordSuggest.ActivedDate != null ? dataKeywordSuggest.ActivedDate
								: new Date();
						suggestSo.CreatedDate = dataKeywordSuggest.CreatedDate;
						suggestSo.IsDeleted = dataKeywordSuggest.IsDeleted;

						var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST)
								.IndexObject(currentIndexDB, suggestSo, suggestSo.SuggestSearchID + "");
						r.Code = ResultCode.Success;

					}
				} else if (messageType == 2) {
					
					var cateinfos = factoryRead.QueryFunction("keywordredirect_getcategorybyID", ProductCategoryBO[].class, false, data, message.SiteID, "vi-VN");
					ProductCategoryBO cateinfo = null;
					if(cateinfos != null && cateinfos.length > 0) {
						cateinfo = cateinfos[0];
					}
					if(cateinfo == null) return r;
					
					SuggestSearchSO suggestSo = new SuggestSearchSO();
					suggestSo.SuggestSearchID = cateinfo.CategoryID;
					suggestSo.SuggestSearchType = SuggestSearchTypes.Category.getValue();// 2; // category
					suggestSo.SuggestSearchTypeName = "category ";
					suggestSo.KeywordSuggest = cateinfo.CategoryName;
					
					suggestSo.CategoryName_EN = GenTermTitle(cateinfo.CategoryName);
					
					suggestSo.KeywordSearch = cateinfo.CategoryName + " , " + (Utils.StringIsEmpty(cateinfo.KeyWord) ? "" : cateinfo.KeyWord.replace(",", " "));
					suggestSo.KeywordSuggest_En = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField("" + cateinfo.CategoryName));
		            suggestSo.KeywordSearch_En = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField((cateinfo.CategoryName + ", " + cateinfo.KeyWord).replace(",", " ")));
		            suggestSo.UrlDMX = "https://www.dienmayxanh.com/" + (!Utils.StringIsEmpty(cateinfo.URL) ? cateinfo.URL : FormatUrl(cateinfo.CategoryName));
		            suggestSo.UrlTGDD = "https://www.thegioididong.com/" + (!Utils.StringIsEmpty(cateinfo.URL) ? cateinfo.URL : FormatUrl(cateinfo.CategoryName));
		            suggestSo.IsActived = cateinfo.IsActived;
		            suggestSo.ActivedDate = cateinfo.ActivedDate == null ? new Date() : cateinfo.ActivedDate;
		            suggestSo.CreatedDate = cateinfo.CreatedDate == null ? new Date() : cateinfo.CreatedDate;
		            suggestSo.IsDeleted = cateinfo.IsDeleted;
		            suggestSo.SiteID = message.SiteID;
		            
		            var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST)
							.IndexObject(currentIndexDB, suggestSo, suggestSo.SuggestSearchID + "_cate_site_" + message.SiteID);
					r.Code = ResultCode.Success;

					
					var Manu = productHelper.GetManuByCategoryID(data, message.SiteID, "vi-VN");
					for (ProductManuBO item : Manu) {
						String domain = message.SiteID == 1 ? "https://www.thegioididong.com/" : "https://www.dienmayxanh.com/";
						String Url = domain + (!Utils.StringIsEmpty(cateinfo.URL) ? cateinfo.URL : FormatUrl(cateinfo.CategoryName));
	                    Url+=  "-" + (!Utils.StringIsEmpty(item.URL) ? item.URL : FormatUrl(item.ManufacturerName));

	                    //+ "-" + (DidxHelper.FormatKeywordField(item.ManufacturerName)) : "");
	                    SuggestSearchSO suggestManuCateSO = new SuggestSearchSO();
	                    suggestManuCateSO.SuggestSearchID = cateinfo.CategoryID;
	                    suggestManuCateSO.ManufactureID = (int) item.ManufactureID;
	                    suggestManuCateSO.SuggestSearchType = SuggestSearchTypes.CategoryManuFacture.getValue(); //3; // category + ManuFacture
	                    suggestManuCateSO.SuggestSearchTypeName = "categoryManuFacture ";
	                    suggestManuCateSO.KeywordSuggest = cateinfo.CategoryName + " " + item.ManufacturerName;

	                    suggestManuCateSO.KeywordSuggest_En = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(cateinfo.CategoryName + ""));
	                    //suggestManuCateSO.KeywordSuggest_En = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(obj.CategoryName + " " + item.ManufacturerName));

	                    String KeywordSearch = (item.MetaKeyWord + "").replace(","," ") + " " + item.ManufacturerName + " " + (cateinfo.CategoryName + ", " + cateinfo.KeyWord).replace(",", " ");
	                    suggestManuCateSO.KeywordSearch = KeywordSearch;
	                    suggestManuCateSO.KeywordSearch_En = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(KeywordSearch));

	                    suggestManuCateSO.UrlDMX = Url;// "https://www.dienmayxanh.com/" + (!string.IsNullOrEmpty(obj.URL) ? obj.URL + "-" + (DidxHelper.FormatKeywordField(item.ManufacturerName)) : url);
	                    suggestManuCateSO.UrlTGDD = Url;// "https://www.dienmayxanh.com/" + (!string.IsNullOrEmpty(obj.URL) ? obj.URL + "-" + DidxHelper.FormatKeywordField(item.ManufacturerName) : url);
	                    suggestManuCateSO.IsActived = cateinfo.IsActived && item.IsActived;
	                    suggestManuCateSO.ActivedDate = cateinfo.ActivedDate == null ? new Date() : cateinfo.ActivedDate;
	                    suggestManuCateSO.CreatedDate = cateinfo.CreatedDate == null ? new Date() : cateinfo.CreatedDate;
	                    suggestManuCateSO.IsDeleted = cateinfo.IsDeleted || item.IsDeleted;
	                    suggestManuCateSO.DisplayOrder = (int) item.DisplayOrder;// chỉ dùng cho cate + manu
	                    suggestManuCateSO.IsNotSuggestSearch = item.IsNotSuggestSearch == 1 ? true : false;
	                    suggestManuCateSO.SiteID = message.SiteID;
	                    
	                    var rsManu = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST)
								.IndexObject(currentIndexDB, suggestManuCateSO, suggestManuCateSO.SuggestSearchID + "_cate_" + (int)item.ManufactureID + "_manu_site_" + message.SiteID);
	                    if(rsManu)
	                    	r.Code = ResultCode.Success;
	                    
					}
				}

				r.Code = ResultCode.Success;

			} else if (message.Action == DataAction.Delete) {

				/// xoá
				if (message.SiteID > 0 && !Utils.StringIsEmpty(message.Identify)) {
					r.Code = DeleteSuggestSearch(message, r);
				}

			}

		} catch (Throwable e) {

			r.Code = ResultCode.Retry;
			Logs.LogException(e);
			return r;
		}

		return r;
	}

	private ResultCode DeleteSuggestSearch(MessageQueue messega, ResultMessage r) {

		try {
			var update = new UpdateRequest(currentIndexDB, String.valueOf(messega.SiteID + "_" + messega.Identify))
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

				Logs.WriteLine("-Update isdeleted in SuggestSearch ES FAILED: SuggestSearch:#" + messega.Identify + ", "
						+ response + "######################");

				r.Code = ResultCode.Retry;
			}
		} catch (Exception e) {
			Logs.WriteLine("Update isdeleted in ERRER, " + e);
			r.Code = ResultCode.Retry;

		}
		return r.Code;
	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String FormatUrl(String Url)
    {
		if(Utils.StringIsEmpty(Url)) return "";
        Url = Url.replace(",", "").replace(".", "").replace("  ", " ").replace(" - ", " ");
        Url = Url.replace(":", " ").replace("  ", " ").replace("---", " ").replace("--", " ");
        Url = Url.replace(",", "").replace(".", "").replace("-", "").replace("\\", "").replace("\\/", "").replace("  ", " ");
        Url = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(Url)).replace(' ', '-').replace("--", "-");
        return Url;
    }
	private String GenTermTitle(String keyword)
    {
		if(Utils.StringIsEmpty(keyword)) return "";
        var arrString = keyword.replace("(", " ").replace(")", " ").replace("|", "").replace(" - ", " ").replace("  ", " ");
        String tmpString = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(arrString.trim())).replace(' ', '_') + " ";
        return tmpString.trim();
    }
}
