package mwg.wb.pkg.validate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.ErpDataCache;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.FileHelper;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.MessageValidate;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.RefSql;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.SSObject;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.notify.LineNotify;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.search.SimSO;
import mwg.wb.model.sim.SimBO;
import mwg.wb.model.sim.SimPackageErpBO;

public class Validate implements Ididx {

	private ProductHelper productHelper = null;
	private ObjectMapper mapper = null;
	private ClientConfig clientConfig = null;

	public Validate() {

	}

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {

		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		productHelper = (ProductHelper) objectTransfer.productHelper;
		mapper = (ObjectMapper) objectTransfer.mapper;

	}

	public boolean ISSame(String cotTockech, Map<String, SSObject> paramsdb, Map<String, SSObject> paramsgr) {
		if (paramsgr.size() == 0)
			return false;

		SSObject db = paramsdb.get(cotTockech);
		SSObject gr = paramsgr.get(cotTockech);
//		if (db == null || gr == null)
//			return false;
		String dbs = paramsdb.get(cotTockech).Value;
		String grs = paramsgr.get(cotTockech).Value;
		if (dbs == null && grs != null)
			return false;
		if (dbs != null && grs == null)
			return false;
		if (dbs == null || grs == null)
			return true;
		if (dbs.toString().equals(grs.toString().replace(".0", "")))
			return true;
		return false;
	}

	public ResultMessage Refresh(MessageQueue message) {
		// DataCenter = message.DataCenter;
		ResultMessage resultMessage = new ResultMessage();
		resultMessage.Code = ResultCode.Success;
		String note = message.Note;

		boolean isLog = Utils.IsMessageSaveLog(message.Note);
		MessageValidate messageValidate = null;
		try {
			messageValidate = mapper.readValue(message.Identify, MessageValidate.class);
		} catch (Throwable e1) {
			// loi thi thoi
			resultMessage.Code = ResultCode.Success;
			return resultMessage;
		}
		if (messageValidate == null)
			return resultMessage;

		// gRecordID\":null,\"gTable\":\"product_detail\",\"gKey\":\"recordid\",\"gValue\":\"1\",\"gSelect\":\"value,recordid\",\"dbparams\":{\"recordid\":{\"cot\":\"recordid\",\"sqlType\":2,\"Value\":\"1\"},\"value\":{\"cot\":\"value\",\"sqlType\":-9,\"Value\":\"86\"}},\"Note\":null,\"CreatedDate\":null,\"cCot\":\"value\",\"cEdge\":\"e_product_detail\",\"cEdgeProp\":\"productid\"}",
		String gCotKey = messageValidate.gKey;
		String gCotValue = messageValidate.gValue;
		String gTable = messageValidate.gTable;
		String gselect = messageValidate.gSelect;

		String gCottoCheck = messageValidate.cCot;
		String gEdgeName = messageValidate.cEdge;
		String gEdgePropToCheck = messageValidate.cEdgeProp;
		Map<String, SSObject> paramsDb = messageValidate.dbparams;
		Map<String, SSObject> paramsGrap = null;
		try {
			if (!Utils.StringIsEmpty(gEdgePropToCheck) && !Utils.StringIsEmpty(gEdgeName)) {
				paramsGrap = productHelper.GetResultMap(
						"select " + gselect + ",in('" + gEdgeName + "')[0]." + gEdgePropToCheck
								+ " as proptocheck from " + gTable + " where " + gCotKey + "=" + gCotValue,
						paramsDb, gEdgePropToCheck);
			} else {
				paramsGrap = productHelper.GetResultMap(
						"select " + gselect + "  from " + gTable + " where " + gCotKey + "=" + gCotValue, paramsDb,
						gEdgePropToCheck);
			}
		} catch (Throwable e) {
			resultMessage.Code = ResultCode.Retry;
			return resultMessage;
		}
		boolean Send = false;

		boolean ss = ISSame(gCottoCheck, paramsDb, paramsGrap);
		if (ss == false) {
			Send = true;
			Logs.LogFactoryMessage("is-same-false" , message.Identify);
		} else {
			if (!Utils.StringIsEmpty(gEdgePropToCheck) && !Utils.StringIsEmpty(gEdgeName)) {

				if (paramsGrap.get("proptocheck") != null && paramsGrap.get("proptocheck").Value != null) {
					long pid = Utils.toLong(paramsGrap.get("proptocheck").Value.toString());
					if (pid <= 0) {
						Send = true;
					}
				}
			}

		}

		if (Send == true) {
			MessageQueue messageNew = new MessageQueue();
			messageNew.Action = DataAction.Add;
			messageNew.Note = "PUSHALL";
			messageNew.Identify = gTable + "|" + gCotKey + "|" + gCotValue + "|";
			messageNew.ClassName = "ALL";
			messageNew.CreatedDate = Utils.GetCurrentDate();
			messageNew.ID = 0;
			messageNew.SiteID = 1;
			messageNew.Lang = "vi-VN";
			messageNew.Version = 0;
			messageNew.DataCenter = 0;
			messageNew.Source = "Validate";
			try {
				QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).Push("gr.reinitdata", messageNew);
			} catch (Exception e) {
				resultMessage.Code = ResultCode.Retry;
				return resultMessage;
			}
			Logs.LogFactoryMessage("require-repush-" + gTable, messageNew.Identify);
		}
		return resultMessage;

	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}
}
