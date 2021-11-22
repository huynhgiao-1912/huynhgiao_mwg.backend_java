package mwg.wb.worker;

import java.awt.Desktop.Action;

import mwg.wb.business.WorkerHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.pkg.news.Faq;
import mwg.wb.common.IMessage.DataAction;

public class testfaq {
	
	
	public static void main(String[] args) throws Throwable {
		
		var config = WorkerHelper.GetWorkerClientConfig();
		var oread = new ORThreadLocal();
		oread.initRead(config, 0,1);
		
		
		var m = new MessageQueue() {
			{
				Identify = "662705"; //587927
				Note = "";
				SiteID = 1;
				Lang = "vi-VN";
				DataCenter = 3;
				Action = DataAction.Add;
			}
		};
		Faq faq = new Faq();
		var tools = new ObjectTransfer();

		tools.factoryRead = oread;
		tools.clientConfig = config;
		faq.InitObject(tools);
		faq.Refresh(m);
	}
}
