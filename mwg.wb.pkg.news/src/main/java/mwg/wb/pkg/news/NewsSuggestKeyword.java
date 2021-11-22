package mwg.wb.pkg.news;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.NewsHelper;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.GConfig;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.news.KeywordSuggest;

public class NewsSuggestKeyword implements Ididx {
	private ORThreadLocal factoryRead = null;
	private String currentIndexDB = "ms_newssuggestkeyword";
	private ClientConfig config = null;
	NewsHelper newsHelper = null;
	private ObjectMapper mapper = null;

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		config = (ClientConfig) objectTransfer.clientConfig;

		newsHelper = new NewsHelper(factoryRead, config);
		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatStringNews);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {

		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;

		int keywordID = Integer.parseInt(message.Identify);
		if (message.Action == DataAction.Add || message.Action == DataAction.Update) {
			KeywordSuggest[] keywordIDlist = null;
			try {
				keywordIDlist = factoryRead.QueryFunction("news_KeywordSuggestByID", KeywordSuggest[].class, false,
						keywordID);

				if (keywordIDlist == null || keywordIDlist.length == 0) {
					r.Code = ResultCode.Success;
					r.Message = "keyword #" + keywordID + " does not exist";
					return r;
				}

				IndexNewsKeywordSuggest(keywordIDlist[0], r);
			} catch (Throwable e) {
				Logs.LogException("IndexNewsKeywordSuggest", e);
				e.printStackTrace();
				r.Code = ResultCode.Retry;
				return r;
			}
		}
		return r;
	}

	private void IndexNewsKeywordSuggest(KeywordSuggest keywordSuggest, ResultMessage r) throws Throwable {
		try {
			keywordSuggest.didx_UpdateDate = new Date();
			var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).IndexObject(currentIndexDB,
					keywordSuggest, keywordSuggest.keywordID + "");
			if (!rs) {
				r.Message = "-Index keywordSuggest to ES FAILED: " + keywordSuggest.keywordID
						+ " ######################";
				r.Code = ResultCode.Retry;
			}
		} catch (Throwable e) {
			Logs.LogException("IndexNewsKeywordSuggest ES failed", e);
			r.Message = "Exception index keywordSuggest to ES FAILED: " + keywordSuggest.keywordID;
			r.StackTrace = Logs.GetStacktrace(e);
			r.Code = ResultCode.Retry;
		}
	}

	@Override
	public ResultMessage RunScheduleTask() {
		return null;
	}
}
