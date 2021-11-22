package mwg.wb.pkg.common;

import mwg.wb.common.Ididx;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.model.api.ClientConfig;

public class CommonPackage implements Ididx {

	private ObjectTransfer objectTransfer = null;
	private ClientConfig config = null;

	@Override
	public void InitObject(ObjectTransfer aobjectTransfer) {
		objectTransfer = aobjectTransfer;
		config = (ClientConfig) objectTransfer.clientConfig;
	}

	public void InitConfig(ClientConfig clientConfig) {

		config = clientConfig;
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage resultMessage = new ResultMessage();
		resultMessage.Code = ResultCode.Success;
		if (message.ClassName.equals("mwg.wb.pkg.common.KeywordRedirectSE")) {
			KeywordRedirectSE exec = new KeywordRedirectSE();
			exec.InitObject(objectTransfer);
			return exec.Refresh(message);
		} else if (message.ClassName.equals("mwg.wb.pkg.common.Payment")) {
			Payment exec = new Payment();
			exec.InitObject(objectTransfer);
			return exec.Refresh(message);
		} else if (message.ClassName.equals("mwg.wb.pkg.common.UrlGoogle")) {
			UrlGoogle exec = new UrlGoogle();
			exec.InitObject(objectTransfer);
			return exec.Refresh(message);
		} else if (message.ClassName.equals("mwg.wb.pkg.common.TimerSeoSE")) {
			TimerSeoSE exec = new TimerSeoSE();
			exec.InitObject(objectTransfer);
			return exec.Refresh(message);
		} else if (message.ClassName.equals("mwg.wb.pkg.common.StoreSE")) {
			StoreSE exec = new StoreSE();
			exec.InitObject(objectTransfer);
			return exec.Refresh(message);
		}else if (message.ClassName.equals("mwg.wb.pkg.common.LabelCampaign")) {
			LabelCampaign exec = new LabelCampaign();
			exec.InitObject(objectTransfer);
			return exec.Refresh(message);
		}else if(message.ClassName.equals("mwg.wb.pkg.common.SuggestSearch")) {
			SuggestSearch exec = new SuggestSearch();
			exec.InitObject(objectTransfer);
			return exec.Refresh(message);
		}

		return resultMessage;
	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
