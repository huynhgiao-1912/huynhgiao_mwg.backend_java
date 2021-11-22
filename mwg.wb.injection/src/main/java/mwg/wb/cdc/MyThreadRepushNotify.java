package mwg.wb.cdc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.WorkerHelper;
import mwg.wb.client.OracleClient;
import mwg.wb.client.graph.OrientDBFactory;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.queue.QueueMesssageEventArgs;
import mwg.wb.client.queue.QueueRabbitMQ;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.Utils;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.notify.LineNotify;
import mwg.wb.model.api.ClientConfig;
import oracle.jdbc.OracleTypes;

public class MyThreadRepushNotify implements Runnable {
	String name;

	Thread t;

	MyThreadRepushNotify() {

		name = "MyThreadRepushNotify";
		t = new Thread(this, name);
		t.start();
	}

	public void run() {
		String backendGroupTokenLog = "e4QuXuo08qZo6CK4wXLoO7djTuB9E9yEhIO2KusK6W9";
//Map<String,Integer> mapHash=new  HashMap<String,Integer>();  

		LineNotify lineNotifyLog = new LineNotify("LINE", backendGroupTokenLog);
		try {

			for (int i = 0; i < 10000000; i++) {

				List<MessageQueue> ra = Logs.ReadLogSysMessage("message-promotion");
				if (ra.size() > 0) {
					lineNotifyLog.NotifyInfo("<START REPUSH " + ra.size() + " NOTIFY PROMOTION !>");
					String queue = "gr.dc4.didx.promotion";
					String queuebk = "gr.dc2.didx.promotion";
					String queueBeta = "gr.beta.didx.promotion";

					for (MessageQueue messageQueue : ra) {
						try {
							messageQueue.Note = "DIDX_RENOTI";
							QueueHelper.Current("amqp://tgdd:Tgdd2012@192.168.2.55:5672").PushAll(queue, queue, queuebk,
									queueBeta, messageQueue, false, "", 4); // 0 bom 2 cái

						} catch (Exception e) {
							Logs.LogException(e);

						}

					}
					lineNotifyLog.NotifyInfo("</END REPUSH " + ra.size() + " NOTIFY PROMOTION !>");
				}

				List<MessageQueue> raPrice = Logs.ReadLogSysMessage("message-price");
				if (raPrice.size() > 0) {
					lineNotifyLog.NotifyInfo("<START REPUSH " + ra.size() + " NOTIFY PRICE !>");
					String queue = "gr.dc4.didx.price";
					String queuebk = "gr.dc2.didx.price";
					String queueBeta = "gr.beta.didx.price";

					for (MessageQueue messageQueue : ra) {
						try {
							messageQueue.Note = "DIDX_RENOTI";
							QueueHelper.Current("amqp://tgdd:Tgdd2012@192.168.2.55:5672").PushAll(queue, queue, queuebk,
									queueBeta, messageQueue, false, "", 4); // 0 bom 2 cái

						} catch (Exception e) {
							Logs.LogException(e);

						}

					}
					lineNotifyLog.NotifyInfo("</END REPUSH " + ra.size() + " NOTIFY PRICE !>");
				}
				Utils.Sleep(60 * 60 * 1000);// 60 phút
			}
		} catch (Exception e) {
			lineNotifyLog.NotifyError("TOOL RENOTI BANH BONG !CHECK GAP");
			Logs.LogException(e);
		}
	}

}