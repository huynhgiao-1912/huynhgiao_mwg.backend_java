package mwg.wb.pkg.tracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.ErpDataCache;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.RefSql;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.common.Upsertable;
import mwg.wb.model.search.SimSO;
import mwg.wb.model.sim.SimBO;
import mwg.wb.model.sim.SimPackageBO;

public class Tracking implements Ididx {

   
	private ClientConfig clientConfig = null;

	public Tracking() {
		 
	}

	 
	@Override
	public void InitObject(ObjectTransfer objectTransfer) {

		 
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
	}

	public ResultMessage Refresh(MessageQueue message) {
		 
		ResultMessage resultMessage = new ResultMessage();
		resultMessage.Code=ResultCode.Success;
		return resultMessage;

	}

	@Override
	public ResultMessage RunScheduleTask() {
		ResultMessage resultMessage = new ResultMessage();
		resultMessage.Code=ResultCode.Success;
		return resultMessage;
	}
}
