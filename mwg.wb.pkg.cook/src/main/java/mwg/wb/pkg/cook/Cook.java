package mwg.wb.pkg.cook;

import mwg.wb.common.Ididx;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.model.api.ClientConfig;


public class Cook implements Ididx{

	private ObjectTransfer objectTransfer = null;
	private ClientConfig config = null;
	@Override
	public void InitObject(ObjectTransfer inObjectTransfer) {
		objectTransfer = inObjectTransfer;
		config = (ClientConfig) objectTransfer.clientConfig;
		
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage resultMessage = new ResultMessage();
		resultMessage.Code = ResultCode.Success;
		
		CookSE exec = new CookSE();
		exec.InitObject(objectTransfer);
		return exec.Refresh(message);
		

	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
