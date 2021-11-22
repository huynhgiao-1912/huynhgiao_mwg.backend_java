package mwg.wb.pkg.promotion;

import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import mwg.wb.business.ProductHelper;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.ErpPromotionHelper;
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
import mwg.wb.model.promotion.Promotion;
import mwg.wb.model.promotion.PromotionGroupGRBO;

public class PromotionSubBrand implements Ididx {

	private ErpPromotionHelper erpPromotionHelper = null;
	private ProductHelper productHelper = null;
	private ObjectMapper mapperndf = null;
	private ClientConfig clientConfig = null;

	@Override
	public void InitObject(ObjectTransfer obj) {
		erpPromotionHelper = (ErpPromotionHelper) obj.erpPromotionHelper;
		productHelper = (ProductHelper) obj.productHelper;
		mapperndf = (ObjectMapper) obj.mapper;
		clientConfig = (ClientConfig) obj.clientConfig;
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;
		int siteID = message.SiteID;
		int provinceID = -1;
		int outputTypeID = 0;
		double salePrice = 0;
		int inventoryStatusID = 1;
		String strNOTE = message.Note + "";
		boolean isLog = strNOTE.contains("LOG");
		Logs.getInstance().Log(isLog, strNOTE, "PromotionSubBrand", message);
		// subgroupID|brandprdID
		try {
			if (Strings.isNullOrEmpty(message.Identify)) {
				message.Identify = "";
			}
			String[] arr = message.Identify.split("\\|");
			int subID = Utils.toInt(arr[0]);
			int manuID = Utils.toInt(arr[1]);
			if (subID <= 0) {
				r.Code = ResultCode.Success;
				return r;
			}
//			if(manuID==0) {
//				r.Code = ResultCode.Success;
//				return r;
//			}
			var promo = erpPromotionHelper.getPromotionSubBrand(subID, manuID, provinceID, outputTypeID, salePrice,
					inventoryStatusID, siteID);
			if (promo == null) {
				// null thì index rỗng
				promo = new mwg.wb.model.promotion.Promotion[0];
			}
			for (var item : promo) {
				if (item != null) {
					String provs = item.provinceIDList;
					if (!Strings.isNullOrEmpty(provs)) {
						item.provinceIDs = Stream.of(provs.split(",")).map(x -> {
							try {
								return Integer.parseInt(x.trim());
							} catch (Exception e) {
								return -1;
							}
						}).filter(x -> x > 0).collect(Collectors.toList());
					}
					productHelper.getHelperBySite(siteID).processPromotion(item);
				}
			}
			String recordid = Stream.of(subID, manuID, provinceID, outputTypeID, salePrice, inventoryStatusID, siteID)
					.map(x -> x.toString()).collect(Collectors.joining("_"));
			var promoGroup = new PromotionGroupGRBO();
			promoGroup.recordID = recordid;
			promoGroup.data = mapperndf.writeValueAsString(promo);

			promoGroup.subGroupID = subID;
			promoGroup.brandIDPrd = manuID;
			promoGroup.provinceID = provinceID;
			promoGroup.outputTypeID = outputTypeID;
			promoGroup.salePrice = salePrice;
			promoGroup.inventoryStatusID = inventoryStatusID;
			promoGroup.siteID = siteID;
			promoGroup.didxUpdatedDate = new Date();

			SqlInfo sqlinfo1 = new SqlInfo();
			RefSql ref1 = new RefSql();
			Utils.BuildSql(isLog, strNOTE, "productpromotiongroup", "recordid", recordid, promoGroup, ref1);
			sqlinfo1.Sql = ref1.Sql;
			sqlinfo1.Params = ref1.params;
			sqlinfo1.tablename = ref1.lTble;
			sqlinfo1.tablekey = ref1.lId;

			MessageQueue mq = new MessageQueue();
			mq.SqlList = new ArrayList<>();
			mq.SqlList.add(sqlinfo1);
			mq.SiteID = siteID;
			mq.Source = "PROMOTION";
			mq.RefIdentify = message.Identify;
			if (salePrice > 0) {
				mq.CachedType = 1;
			}
			mq.Action = DataAction.Update;
			mq.ClassName = "ms.upsert.Upsert";
			mq.CreatedDate = Utils.GetCurrentDate();
			mq.Lang = message.Lang;
			mq.SiteID = siteID;
			mq.Note = strNOTE;
			mq.DataCenter = message.DataCenter;

			Logs.Log(isLog, strNOTE, "productpromotiongroup refresh " + recordid);

			int ieV2 = 0;
			String quV2 = "gr.dc4.sqlpro" + ieV2;
			String qu2V2 = "gr.dc4.sqlpro" + ieV2;
			String qubkV2 = "gr.dc2.sqlpro" + ieV2;
			String qudevV2 = "gr.beta.sqlpro";
			QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(quV2, qu2V2, qubkV2, qudevV2, mq, isLog,
					strNOTE, 0);
			Logs.getInstance().Log(isLog, strNOTE, "messageRepush KM ", mq);

			var bklist = Stream.of(promo).filter(x -> x.GroupID.equalsIgnoreCase("bankem")).toArray(Promotion[]::new);
			if (bklist != null) {
				for (var bk : bklist) {
					MessageQueue nwmsg = new MessageQueue();
					String qu = "gr.dc4.didx.bankem";
					String qubeta = "gr.beta.didx.bankem";
					String qubk = "gr.dc2.didx.bankem";

					nwmsg.Action = DataAction.Update;
					nwmsg.ClassName = "mwg.wb.pkg.promotion.PromotionBanKem";
					nwmsg.CreatedDate = Utils.GetCurrentDate();
					nwmsg.Lang = message.Lang;
					nwmsg.Note = message.Note;
					nwmsg.SiteID = message.SiteID;
					nwmsg.DataCenter = 0;
					nwmsg.Identify = bk.PromotionID + "";
					nwmsg.Data = bk.BeginDate.getTime() + "|" + bk.EndDate.getTime() + "|" + bk.PromotionListGroupID
							+ "|" + bk.PromotionListGroupName;
					nwmsg.Source = "PromotionV1 ";
					QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu, qubk, qubeta, nwmsg, isLog,
							strNOTE, 0);
				}
			}
		} catch (Throwable e) {
			Logs.LogException(e);
			r.Code = ResultCode.Retry;
			r.StackTrace = Utils.stackTraceToString(e);
			Logs.Log(isLog, strNOTE, "push promotion  " + e.getMessage());
		}
		return r;
	}

	@Override
	public ResultMessage RunScheduleTask() {
		return null;
	}

}
