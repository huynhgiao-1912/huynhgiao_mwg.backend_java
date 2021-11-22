package mwg.wb.pkg.cook;

import java.util.ArrayList;
import java.util.Date;

import mwg.wb.business.WorkerHelper;
import mwg.wb.client.cache.IgniteClient;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.search.NewsSO;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {

		System.out.println("All news in your brain...");

//		lineNotify = new LineNotify("LINE", backendGroupToken);
//
//		lineNotify.Notify("Refresh error");

		ObjectTransfer objTransfer = new ObjectTransfer();
		ORThreadLocal factoryRead = null;
		try {
			factoryRead = new ORThreadLocal();
			factoryRead.IsWorker = true;
		} catch (Exception e) {

			e.printStackTrace();
		}
		ClientConfig clientConfig = null;

		clientConfig = new ClientConfig();

		clientConfig.SERVER_ORIENTDB_READ_USER = "admin";
		clientConfig.SERVER_ORIENTDB_READ_PASS = "EnterW@graph!@#";
		clientConfig.SERVER_ORIENTDB_WRITE_USER = "admin";
		clientConfig.SERVER_ORIENTDB_WRITE_PASS = "EnterW@graph!@#";
		clientConfig.DATACENTER = 1;
		clientConfig.SERVER_ORIENTDB_WRITE_URL1 = "remote:10.1.5.126:2424/web";

		clientConfig.SERVER_ORIENTDB_READ_URL1 = "remote:10.1.5.126:2424/web";

		clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST = "10.1.6.151";
		clientConfig.SERVER_ELASTICSEARCH_READ_HOST = "10.1.6.151";
		clientConfig.SERVER_RABBITMQ_URL = "amqp://beta:beta@10.1.6.139:5672";

		clientConfig.ERP_SERVICES_URL = "http://betaerpsupportservices.thegioididong.com/Web/WSWeb.asmx";
		clientConfig.ERP_SERVICES_AUTHEN = "ksdfswfrew3ttc!@4#123";
		clientConfig.DATACENTER = 3;
		factoryRead.initRead(clientConfig, 0, 10);

		objTransfer.clientConfig = clientConfig;
		objTransfer.factoryRead = factoryRead;

		Cook cook = new Cook();
		cook.InitObject(objTransfer);
		MessageQueue message = new MessageQueue();
		message.SiteID = 1;
		message.Lang = "vi-VN";
		message.Action = DataAction.Update;

		// message.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_VIEWCOUNT;
		// message.Data = "2184";
		var listID = new ArrayList<Integer>();
		listID.add(3);
		

		for (var item : listID) {
			message.Identify = String.valueOf(item);
			try {
				cook.Refresh(message);
			} catch (Exception e) {
				e.getStackTrace();

			}
		}

		System.out.println("Refresh completed!");

	}
}
