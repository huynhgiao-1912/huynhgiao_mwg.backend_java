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
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.news.HotTopicBO;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.news.NewsEventBO;
import mwg.wb.model.search.HotTopicSO;
import mwg.wb.model.search.NewsEventSO;

public class NewsTopic implements Ididx {

	private ORThreadLocal factoryRead = null;
	private String currentIndexDB = "ms_newstopic";
	private ClientConfig config = null;

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		config = (ClientConfig) objectTransfer.clientConfig;

	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;
		try {

			int topicID = Integer.parseInt(message.Identify);

			try {
				if (message.Action == DataAction.Add || message.Action == DataAction.Update) {
					var lstTopic = factoryRead.QueryFunction("news_GetHotTopicByID", HotTopicBO[].class, false,
							topicID);

					if (lstTopic == null || lstTopic.length == 0) {
						r.Code = ResultCode.Success;
						r.Message = "Hottopic #" + topicID + " does not exist";
						return r;
					}
					HotTopicBO newsTopic = lstTopic[0];
					HotTopicSO topicSO = new HotTopicSO();
					try {

						topicSO.HotTopicID = newsTopic.HotTopicID;
						topicSO.HotTopicName = newsTopic.HotTopicName;
						topicSO.Description = newsTopic.Description;
						topicSO.IsActived = newsTopic.IsActived;
						topicSO.IsShowHome = newsTopic.IsShowHome;
						topicSO.CreatedDate = newsTopic.CreatedDate;
						topicSO.UpdatedDate = newsTopic.UpdatedDate;
						topicSO.IsDeleted = newsTopic.IsDeleted;
						topicSO.IsExist = newsTopic.IsExist;
						topicSO.IsSelected = newsTopic.IsSelected;
						topicSO.AmountNews = newsTopic.AmountNews;
						topicSO.SiteID = newsTopic.SiteID;
						topicSO.MetaTitle = newsTopic.MetaTitle;
						topicSO.MetaDescription = newsTopic.MetaDescription;
						topicSO.MetaKeyWord = newsTopic.MetaKeyWord;
						topicSO.EventIDList = newsTopic.EventIDList;
						topicSO.ProductIDList = newsTopic.ProductIDList;
						topicSO.ActivedDate = newsTopic.ActivedDate;

						var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST)
								.IndexObject(currentIndexDB, topicSO, topicSO.HotTopicID + "");
						if (!rs) {
							Logs.WriteLine("-Index news HotTopic to ES FAILED: " + newsTopic.HotTopicID
									+ " ######################");

							r.Code = ResultCode.Retry;
						}

					} catch (Throwable e) {

						Logs.WriteLine("Exception index HotTopic to ES FAILED: " + newsTopic.HotTopicID);
						//Logs.WriteLine(e);
						Utils.Sleep(100);
						r.Code = ResultCode.Retry;
					}

				} else if (message.Action == DataAction.Delete) {

					DeleteTopic(topicID, r);

				}
			} catch (Throwable ex) {
				r.Code = ResultCode.Retry;
				r.Message = "Error on Refresh HotTopic: " + topicID + ". Detail error:" + ex;

			}

			return r;
		} catch (Throwable e) {
			r.Message = "Error on Refresh HotTopic. Detail error:" + e;
			r.Code = ResultCode.Retry;
			return r;
		}
	}

	private void DeleteTopic(int topicID, ResultMessage r) {

		var update = new UpdateRequest(currentIndexDB, String.valueOf(topicID))
				.doc("{\"IsDeleted\":1  }", XContentType.JSON).docAsUpsert(true).detectNoop(false);
		var client = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
		UpdateResponse response = null;
		try {
			response = client.update(update, RequestOptions.DEFAULT);
		} catch (IOException e) {

			e.printStackTrace();
		}
		if (response != null && response.getResult() == Result.UPDATED) {
			r.Code = ResultCode.Success;
		} else {

			Logs.WriteLine(
					"-Update isdeleted in HotTopic ES FAILED: " + topicID + ", " + response + "######################");
			Utils.Sleep(100);
			r.Code = ResultCode.Retry;
		}

	}

	@Override
	public ResultMessage RunScheduleTask() {

		return null;
	}

}
