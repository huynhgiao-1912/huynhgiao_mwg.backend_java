package mwg.wb.pkg.common;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.ProductUrlHelper;
import mwg.wb.business.WorkerHelper;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.elasticsearch.dataquery.StoreSO;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StoreSE implements Ididx {

	private ProductHelper productHelper = null;
	private String currentIndexDB = "ms_store";
	private ClientConfig clientConfig = null;

	@Override
	public void InitObject(ObjectTransfer o) {
		productHelper = (ProductHelper) o.productHelper;
		clientConfig = (ClientConfig) o.clientConfig;
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;
		String strNOTE = message.Note + "";
		boolean isLog = strNOTE.contains("LOG");
		Logs.getInstance().Log(isLog, strNOTE, "StoreSE", message);
		int storeID = Utils.toInt(message.Identify);
		if (storeID <= 0) {
			return r;
		}
		try {
			if (message.Action == DataAction.Delete) {
				doDelete(storeID, isLog, strNOTE);
				return r;
			}
			var arr = productHelper.getStoreByIDList(new int[] { storeID });
			if (arr == null || arr.length == 0) {
				r.Message = "StoreID " + storeID + " not found, indexing delete...";
				Logs.Log(isLog, strNOTE, r.Message);
				doDelete(storeID, isLog, strNOTE);
				return r;
			}
			var store = arr[0];
			String url = DidxHelper.GenTerm3(
					ProductUrlHelper.GenSEOUrl(store.WebAddress != null ? store.WebAddress : store.StoreAddress));
			var keyword = Stream.of(store.StoreID + "", formatStoreKeyword(store.ProvinceName),
					formatStoreKeyword(store.StoreName), formatStoreKeyword(store.StoreShortName),
					formatStoreKeyword(store.WebAddress != null ? store.WebAddress : store.StoreAddress),
					formatStoreKeyword(store.StorePhoneNum)).collect(Collectors.joining(" , "));
			keyword = DidxHelper.FilterVietkey(DidxHelper.fotmatKeywordIndexField(keyword));
			var so = new StoreSO() {
				{
					LAT = store.LAT;
					LNG = store.LNG;
					Location = new GeoPoint(LAT <= 90 && LAT >= -90 ? LAT : 0, LNG);
					StoreID = store.StoreID;
					SiteID = store.SiteID;
					CompanyID = store.CompanyID;
					OpeningDay = store.OpeningDay;
					ReOpenDate = store.ReOpenDate;
					PartnerinstallmentIDList = store.PartnerinstallmentIDList;
					ProvinceID = store.ProvinceID;
					DistrictID = store.DistrictID;
					IsShowweb = store.IsShowweb;
					IsSaleStore = store.IsSaleStore;
					ClosingDay = store.ClosingDay != null && store.ClosingDay.getTime() > 315532800000L
							? store.ClosingDay
							: new Date(System.currentTimeMillis() + 6307200000000L);
					UrlTerm = url;
					didx_updateddate = new Date();
				}
			};
			so.Keyword = keyword;
			so.Keyword_us = keyword;
			if (!ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
					.IndexObject2(currentIndexDB, "", so, storeID + "")) {
				String m = "-Index StoreSE to ES FAILED: " + storeID + " ######################";
				Logs.Log(isLog, strNOTE, m);
				 throw new Exception(m);
			} else {
				Logs.Log(isLog, strNOTE, "Index StoreSE to ES success: " + storeID);
			}
		} catch (Throwable e) {
//			e.printStackTrace();
			Logs.LogException(  e);
			r.StackTrace = Utils.stackTraceToString(e);
			r.Code = ResultCode.Retry;
		}
		return r;
	}

	private void doDelete(int storeID, boolean isLog, String strNOTE) throws Exception {
		var update = new UpdateRequest(currentIndexDB, storeID + "").doc("{\"isDeleted\":true}", XContentType.JSON)
				.docAsUpsert(true).detectNoop(false);
		var client = ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
		var response = client.update(update, RequestOptions.DEFAULT);
		if (response == null || (response.getResult() != Result.UPDATED && response.getResult() != Result.CREATED)) {
			String m = "-Index store to ES FAILED: " + storeID + ", " + response.getResult().name()
					+ "######################";
			Logs.Log(isLog, strNOTE, m);
			throw new Exception(m);
		} else {
			Logs.Log(isLog, strNOTE, "Deleted store from ES success: " + storeID);
		}
	}

	private String formatStoreKeyword(String input) {
		return input == null ? "" : input.replace(".", " . ");
	}

	@Override
	public ResultMessage RunScheduleTask() {
		return null;
	}

	public static void main(String[] args) throws JsonParseException, IOException {

		var config = WorkerHelper.GetWorkerClientConfig();
		var oread = new ORThreadLocal();
		oread.initRead(config, 0, 2);
		var priceHelper = new PriceHelper(oread, config);
		var phelper = new ProductHelper(oread, config);

		// dl live
		config.SERVER_RABBITMQ_URL = "amqp://tgdd:Tgdd2012@192.168.2.55:5672";
		config.SERVER_ORIENTDB_READ_URL1 = "remote:172.16.3.71:2424/web";
		config.ERP_SERVICES_URL = "http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx";
		config.ERP_SERVICES_AUTHEN = "werwerewrw32423!@4#123";
		config.CRM_SERVIVES_URL = "http://crm-services.thegioididong.com/NEW-CRMTGDD/CRMTGDDService.asmx";
		config.SERVER_ELASTICSEARCH_WRITE_HOST = "172.16.3.23";

		var erp = new ErpHelper(config.ERP_SERVICES_URL, config.ERP_SERVICES_AUTHEN);
		var mapper = new ObjectMapper();
		var df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		int[] mn = {2010,1405,1386,297,1133,4737,4253,3902,1814,2100,4575,7177};
		for (int k : mn){
			var m = new MessageQueue() {
				{
					Identify = k+"";
					Note = "";
					SiteID = 1;
					Lang = "vi-VN";
					DataCenter = 0;
				}
			};
			var tools = new ObjectTransfer();
			tools.erpHelper = erp;
			tools.mapper = mapper;
			tools.factoryRead = oread;
			tools.clientConfig = config;
			tools.productHelper = phelper;
			tools.priceHelper = priceHelper;
			var status = new StoreSE();
			status.InitObject(tools);
			status.Refresh(m);
		}

		System.out.print("hello");

	}

}
