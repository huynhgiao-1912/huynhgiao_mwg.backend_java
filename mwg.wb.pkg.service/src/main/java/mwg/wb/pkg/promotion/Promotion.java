package mwg.wb.pkg.promotion;

import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.model.api.ClientConfig;

public class Promotion implements Ididx {

	int DataCenter = 0;
	ObjectTransfer objectTransfer = null;
	ClientConfig clientConfig = null;

	@Override
	public void InitObject(ObjectTransfer aobjectTransfer) {

		objectTransfer = aobjectTransfer;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		DataCenter = message.DataCenter;
		String note = message.Note + "";
		if (message.SiteID == 11) {
			if (note.contains("BHXTEST")) {
				try {
					var promotion = new PromotionBHXV1();
					promotion.InitObject(objectTransfer);
					promotion.Refresh(message);
				} catch (Throwable ex) {
					Logs.LogException(ex);
				}
				ResultMessage msg = new ResultMessage();
				msg.Code = ResultCode.Success;
				return msg;
			} else {

				ResultMessage msg = new ResultMessage();
				msg.Code = ResultCode.Success;
				return msg;
			}

		} else {
			var promotion = new PromotionV1();
			promotion.InitObject(objectTransfer);
			return promotion.Refresh(message);
		}

	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
