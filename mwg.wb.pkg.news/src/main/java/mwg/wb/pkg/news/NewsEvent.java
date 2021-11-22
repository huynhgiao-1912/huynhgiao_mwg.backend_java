package mwg.wb.pkg.news;

import java.io.IOException;
import java.util.Date;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

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
import mwg.wb.common.notify.LineNotify;
 
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.news.NewsEventBO;
import mwg.wb.model.search.NewsEventSO;

public class NewsEvent implements Ididx {

	private ORThreadLocal factoryRead = null;
	private String currentIndexDB = "ms_newsevent";
	private ClientConfig config = null;
	public LineNotify lineNotify = null;

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		config = (ClientConfig) objectTransfer.clientConfig;
		lineNotify = (LineNotify) objectTransfer.notifyHelper;

	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {

		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;
		try {

			int newsEventID = Integer.parseInt(message.Identify);

			// String strNOTE = message.Note + "";
			boolean isLog = false;

//			if (strNOTE.contains("LOG")) {
//
//				isLog = true;
//			}

			// Logs.Log(isLog, strNOTE, "start newsEvent... " + newsEventID + ",action: " +
			// message.Action.toString());

			try {
				if (message.Action == DataAction.Add || message.Action == DataAction.Update) {
					var newsEventList = factoryRead.QueryFunction("news_event_GetInfo", NewsEventBO[].class, false,
							newsEventID);
					if (newsEventList == null || newsEventList.length == 0) {
						r.Code = ResultCode.Success;
						r.Message = "News Event #" + newsEventID + " does not exist";
						// Logs.Log(isLog, strNOTE, r.Message);
						return r;
					}
					NewsEventBO newsEvent = newsEventList[0];
					NewsEventSO newsEventSO = new NewsEventSO();
					try {
						newsEventSO.eventID = newsEvent.Eventid;
						newsEventSO.isExist = newsEvent.IsExist;

						newsEventSO.eventName = newsEvent.Eventname;
						newsEventSO.image = newsEvent.Image;
						newsEventSO.fromDate = newsEvent.Fromdate;
						newsEventSO.toDate = newsEvent.Todate;
						newsEventSO.metaTitle = newsEvent.Metatitle;
						newsEventSO.address = newsEvent.Address;
						newsEventSO.shortDescription = newsEvent.Shortdescription;
						newsEventSO.embedLiveLog = newsEvent.Embedlivelog;
						newsEventSO.embedGoogleMap = newsEvent.Embedgooglemap;
						newsEventSO.isActived = newsEvent.Isactived;
						newsEventSO.activedDate = newsEvent.Activeddate;
						newsEventSO.isdeleted = newsEvent.Isdeleted;
						newsEventSO.keyword = DidxHelper
								.FilterVietkey(DidxHelper.FormatKeywordField(newsEvent.Eventname));
						newsEventSO.liveStreamVideoLink = newsEvent.LiveStreamVideoLink;
						newsEventSO.liveStreamFromDate = newsEvent.LiveStreamFromDate;
						newsEventSO.liveStreamToDate = newsEvent.LiveStreamToDate;
						newsEventSO.isFollow = newsEvent.IsFollow;
						newsEventSO.isParent = newsEvent.IsParent;
						newsEventSO.parentEventID = newsEvent.ParentEventID;

						var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST)
								.IndexObject(currentIndexDB, newsEventSO, newsEventSO.eventID + "");
						if (!rs) {
							Logs.WriteLine("-Index newsEvent to ES FAILED: " + newsEventSO.eventID
									+ " ######################");

//							Logs.Log(isLog, strNOTE, "-Index newsEvent to ES FAILED: " + newsEventSO.eventID
//									+ " ######################");
							r.Code = ResultCode.Retry;
						}

					} catch (Exception e) {

						// Logs.Log(isLog, strNOTE, "Exception index newsEvent to ES FAILED: " +
						// newsEvent.Eventid);
						Logs.WriteLine(e);

						r.Code = ResultCode.Retry;
					}

				} else if (message.Action == DataAction.Delete) {

					DeleteEvent(newsEventID, r);
					// Logs.Log(isLog, strNOTE, "Delete event " + newsEventID);
				}
			} catch (Exception ex) {
				r.Code = ResultCode.Retry;
				r.Message = "Error on Refresh NewsEvent: " + newsEventID + ". Detail error:" + ex;
				// Logs.Log(isLog, strNOTE, r.Message);
			}
		} catch (Throwable e) {
			r.Code = ResultCode.Success;
		 

		}

		return r;
	}

	private void DeleteEvent(int newsEventID, ResultMessage r) {

		var update = new UpdateRequest(currentIndexDB, String.valueOf(newsEventID))
				.doc("{\"isdeleted\":1  }", XContentType.JSON).docAsUpsert(true).detectNoop(false);
		var client = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
		UpdateResponse response = null;
		try {
			response = client.update(update, RequestOptions.DEFAULT);
		} catch (Exception e) {

			e.printStackTrace();
		}
		if (response != null && response.getResult() == Result.UPDATED) {
			r.Code = ResultCode.Success;
		} else {

			Logs.WriteLine("-Update isdeleted in NewsEvent ES FAILED: " + newsEventID + ", " + response
					+ "######################");

			r.Code = ResultCode.Retry;
		}

	}

	@Override
	public ResultMessage RunScheduleTask() {

		return null;
	}

}
