package mwg.wb.common.notify;

import java.util.HashMap;
import java.util.Map;

import mwg.wb.common.MessageQueue;
import mwg.wb.common.ResultMessage;

public abstract class NotifyHelper1 {

	public String notifyName;
	
	public String token;

	public int DataCenter;
	
	public abstract void Notify(String message);
	public abstract void Notify(String message, int DataCenter);
	public void PushLineNotify(MessageQueue messageQueue, ResultMessage rsCode, String pkName, String processClassName) {
		// TODO Auto-generated method stub
		
	}
}
