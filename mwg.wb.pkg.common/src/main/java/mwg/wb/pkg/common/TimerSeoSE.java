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
import mwg.wb.model.commonpackage.KeyWordRedirectBO;
import mwg.wb.model.commonpackage.TimerSeoBO;
import mwg.wb.model.commonpackage.TimerSeoSO;

public class TimerSeoSE implements Ididx {

	private ORThreadLocal factoryRead = null;
	private String currentIndexDB = "ms_timerseo";
	private ClientConfig config = null;

	private ObjectMapper mapper = null;

	public void InitObject(ObjectTransfer objectTransfer) {
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		config = (ClientConfig) objectTransfer.clientConfig;
		mapper = (ObjectMapper) objectTransfer.mapper;
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

		long TimerID = Utils.toInt(message.Identify);
		if (TimerID <= 0) {
			return r;
		}
		try {
			if (message.Action == DataAction.Add || message.Action == DataAction.Update) {
				var TimerSeo = factoryRead.QueryFunction("product_timerseo_GetByID", TimerSeoBO[].class, false,
						TimerID);

				if (TimerSeo == null || TimerSeo.length == 0) {
					r.Code = ResultCode.Success;
					return r;
				} else {
					/// ///
					TimerSeoSO seoSE = new TimerSeoSO();
					seoSE.TimerID = TimerSeo[0].TimerID;
					seoSE.Title = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(TimerSeo[0].Title + " "));
					seoSE.ProductID = TimerSeo[0].ProductID;
					seoSE.CategoryID = TimerSeo[0].CategoryID;
					seoSE.ListSiteID = (TimerSeo[0].ListSiteID + ",").replace(",", " ");
					seoSE.BeginDate = TimerSeo[0].BeginDate;
					seoSE.EndDate = TimerSeo[0].EndDate;
					seoSE.IsActived = TimerSeo[0].IsActived;
					seoSE.IsDeleted = TimerSeo[0].IsDeleted;
					seoSE.CreatedDate = TimerSeo[0].CreatedDate;

					var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST)
							.IndexObject("ms_timerseo", seoSE, seoSE.TimerID + "_" + seoSE.ProductID);
					r.Code = ResultCode.Success;

				}
			} else if (message.Action == DataAction.Delete) {

				/// xoá
				var TimerSeo = factoryRead.QueryFunction("product_timerseo_GetByID", TimerSeoBO[].class, false,
						TimerID);

				if (TimerSeo != null || TimerSeo.length > 0) {
					r.Code = DeleteTimerSeo(TimerSeo[0], r);
				}

			}

		} catch (Throwable e) {

			r.Code = ResultCode.Retry;
			Logs.WriteLine("TimerSeoSE_trycatch: " + e.toString());
			return r;
		}

		return r;
	}

	private ResultCode DeleteTimerSeo(TimerSeoBO Timerseo, ResultMessage r) {

		try {
			var update = new UpdateRequest(currentIndexDB, String.valueOf(Timerseo.TimerID + "_" + Timerseo.ProductID))
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

				Logs.WriteLine("-Update isdeleted in Timerseo ES FAILED: Timerseo:#" + Timerseo + ", " + response
						+ "######################");

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

}
