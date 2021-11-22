package mwg.wb.pkg.common;

import java.util.Date;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import mwg.wb.business.ProductHelper;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;

public class Payment implements Ididx {
	private ProductHelper productHelper = null;
	private ClientConfig clientConfig = null;
	static final String currentIndexDB = "ms_payment";

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {
		productHelper = (ProductHelper) objectTransfer.productHelper;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
//		currentIndexDB = clientConfig.ELASTICSEARCH_PAYMENT_INDEX;
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage r = new ResultMessage() {
			{
				Code = ResultCode.Success;
			}
		};
		String strNOTE = message.Note + "";
		boolean isLog = false;
		if (strNOTE.contains("DIDX_TOP_LOG") || strNOTE.contains("DIDX_LOG")) {
			isLog = true;
		}
		try {
			int paymentID = Integer.parseInt(message.Identify);
			if (message.Action != DataAction.Delete) {
				var payment = productHelper.getPaymentByID(paymentID);
				if (payment == null) {
					r.Message = "PaymentID " + paymentID + " not found, indexing delete...";
					Logs.Log(isLog, strNOTE, r.Message);
					doDelete(paymentID, isLog, strNOTE);
					return r;
				}
				payment.didx_updateddate = new Date();
				if (!ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
						.IndexObject2(currentIndexDB, "", payment, paymentID + "")) {
					String m = "-Index payment to ES FAILED: " + paymentID + " ######################";
					Logs.Log(isLog, strNOTE, m);
					throw new Exception(m);
				} else {
					Logs.Log(isLog, strNOTE, "Index payment to ES success: " + paymentID);
				}
			} else {
				doDelete(paymentID, isLog, strNOTE);
			}
		} catch (Throwable e) {
//			e.printStackTrace();
			Logs.LogException("status.txt", e);
			r.StackTrace = Utils.stackTraceToString(e);
			r.Code = ResultCode.Retry;
		}
		return r;
	}

	private void doDelete(int paymentID, boolean isLog, String strNOTE) throws Exception {
		var update = new UpdateRequest(currentIndexDB, paymentID + "").doc("{\"isDeleted\":true}", XContentType.JSON)
				.docAsUpsert(true).detectNoop(false);
		var client = ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
		var response = client.update(update, RequestOptions.DEFAULT);
		if (response == null || (response.getResult() != Result.UPDATED && response.getResult() != Result.CREATED)) {
			String m = "-Index payment to ES FAILED: " + paymentID + ", " + response.getResult().name()
					+ "######################";
			Logs.Log(isLog, strNOTE, m);
			throw new Exception(m);
		} else {
			Logs.Log(isLog, strNOTE, "Deleted payment from ES success: " + paymentID);
		}
	}

	@Override
	public ResultMessage RunScheduleTask() {
		return null;
	}

}
