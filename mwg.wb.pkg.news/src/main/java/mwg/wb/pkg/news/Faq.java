package mwg.wb.pkg.news;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.notify.LineNotify;
 
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.faq.FaqBO;
import mwg.wb.model.search.FaqSO;

public class Faq implements Ididx {

	private ORThreadLocal factoryRead = null;
	private String currentIndexDB = "ms_faq";
	private ClientConfig config = null;
	// public NotifyHelper lineNotify = null;

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		config = (ClientConfig) objectTransfer.clientConfig;
		// lineNotify = (LineNotify) objectTransfer.notifyHelper;
	}

	private final Lock queueLock = new ReentrantLock();

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;
		try {
			int faqID = Integer.parseInt(message.Identify);
			boolean isLog = false;

			ResultMessage resultMessage = new ResultMessage();
			resultMessage.Code = ResultCode.Success;
			if (message.Action == DataAction.Add || message.Action == DataAction.Update) {

				var faqList = factoryRead.QueryFunction("faq_GetByID", FaqBO[].class, false, faqID);
				if (faqList == null || faqList.length == 0) {
					r.Code = ResultCode.Success;
					r.Message = "Faq ID #" + faqID + " does not exist";
					return r;
				} else {
					try {
						if (faqList[0] == null) {
							r.Code = ResultCode.Success;
							r.Message = "Faq ID #" + faqID + " does not exist";
							return r;

						}
						var faqSO = FaqSO.fromBO(faqList[0]);
						var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST)
								.IndexObject(currentIndexDB, faqSO, faqSO.NewsID + "");
						if (!rs) {
							r.Message = "FAQ #" + faqID + " to ES FAILED";
							r.Code = ResultCode.Retry;

							return r;
						} else {
							r.Code = ResultCode.Success;
						}
					} catch (Throwable e) {

						Logs.LogException("IndexFAQ", e);
						r.Code = ResultCode.Retry;
						r.Message = "Faq #" + faqID + " init FAILED";
						r.StackTrace = Logs.GetStacktrace(e);
						return r;
					}
				}

			} else if (message.Action == DataAction.Delete) {

				r.Code = DeleteFaqSE(faqID, r);
				if (r.Code == ResultCode.Retry) {
					r.Message = "Faq #" + faqID + " init FAILED";
					return r;
				}

			}

		} catch (Throwable e) {
			r.Code = ResultCode.Retry;
			r.Message = "FAQ #" + message.Identify + " update FAILED";
			r.StackTrace = Logs.GetStacktrace(e);
			Logs.LogException("IndexFAQ", e);

		}

		return r;
	}

	private ResultCode DeleteFaqSE(int faqID, ResultMessage r) {

		try {
			var update = new UpdateRequest(currentIndexDB, String.valueOf(faqID))
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

				Logs.WriteLine(
						"-Update isdeleted in FAQ ES FAILED: " + faqID + ", " + response + "######################");

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
