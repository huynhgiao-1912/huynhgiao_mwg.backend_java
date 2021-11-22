package mwg.wb.pkg.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.commonpackage.LabelCampaignBO;
import mwg.wb.model.commonpackage.LabelCampaignSO;

import java.util.Date;

public class LabelCampaign implements Ididx {

	private ORThreadLocal factoryRead = null;
	private String currentIndexDB = "ms_label_campaign";
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

		int LabelID = Utils.toInt(message.Identify);
		if (LabelID <= 0) {
			return r;
		}
		try {
//			LabelCampaignBO
			if (message.Action == DataAction.Add || message.Action == DataAction.Update) {
				var LabelCampaign = factoryRead.QueryFunction("product_GetLabelCampaignByID",
						LabelCampaignBO[].class, false, LabelID);

				if (LabelCampaign == null || LabelCampaign.length == 0) {
					r.Code = ResultCode.Success;
					return r;
				} else {
					/// ///
					LabelCampaignSO labelSO = new LabelCampaignSO();
					labelSO.LabelID = LabelCampaign[0].LabelID;
					labelSO.LabelName = LabelCampaign[0].LabelName;
					labelSO.Icon = LabelCampaign[0].Icon;
					labelSO.ColorLeft = LabelCampaign[0].ColorLeft;
					labelSO.ColorRight = LabelCampaign[0].ColorRight;
					labelSO.CreatedDate = LabelCampaign[0].CreatedDate == null
							? new Date()
							: LabelCampaign[0].CreatedDate;
					labelSO.ActivedDate = LabelCampaign[0].ActivedDate == null
							? new Date()
							: LabelCampaign[0].ActivedDate;
					labelSO.ProDuctIDList = !Utils.StringIsEmpty(LabelCampaign[0].ProDuctIDList)
							? LabelCampaign[0].ProDuctIDList.replace(",", " ")
							: "";
					labelSO.BeginDate = LabelCampaign[0].BeginDate != null
							? LabelCampaign[0].BeginDate
							: Utils.GetDefaultDate();
					labelSO.EndDate = LabelCampaign[0].EndDate != null
							? LabelCampaign[0].EndDate
							: Utils.GetDefaultDate();
					labelSO.SampleType = LabelCampaign[0].SampleType;
					labelSO.IsActived  = LabelCampaign[0].IsActived;
					labelSO.IsDeleted = LabelCampaign[0].IsDeleted;
					labelSO.SiteIDList = !Utils.StringIsEmpty(LabelCampaign[0].SiteIDList)
							? LabelCampaign[0].SiteIDList.replace(" ", "")
							.replace(",", " ")
							: "";
					
					var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST)
							.IndexObject(currentIndexDB, labelSO, labelSO.LabelID + "");
					r.Code = ResultCode.Success;

				}
			} else if (message.Action == DataAction.Delete) {

				/// xoá
				r.Code = ResultCode.Success;
				Logs.LogException("LabelCampaign: sao vo DELETE");
			}

		} catch (Throwable e) {

			r.Code = ResultCode.Retry;
			Logs.LogException( e);
			return r;
		}

		return r;
	}
	
//	private ResultCode DeleteLabelCampaign(int LabelID, ResultMessage r) {
//
//		try {
//			var update = new UpdateRequest(currentIndexDB, String.valueOf(LabelID + ""))
//					.doc("{\"IsDeleted\":1  }", XContentType.JSON).docAsUpsert(true).detectNoop(false);
//			var client = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
//			UpdateResponse response = null;
//			try {
//				response = client.update(update, RequestOptions.DEFAULT);
//			} catch (Exception e) {
//				r.Code = ResultCode.Retry;
//				e.printStackTrace();
//			}
//			if (response != null && response.getResult() == Result.UPDATED) {
//				r.Code = ResultCode.Success;
//			} else {
//
//				Logs.WriteLine("-Update isdeleted in LabelCampaign ES FAILED: LabelID:#" + LabelID + ", " + response
//						+ "######################");
//
//				r.Code = ResultCode.Retry;
//			}
//		} catch (Exception e) {
//			Logs.WriteLine("Update isdeleted in ERRER, " + e);
//			r.Code = ResultCode.Retry;
//
//		}
//		return r.Code;
//	}
	
	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
