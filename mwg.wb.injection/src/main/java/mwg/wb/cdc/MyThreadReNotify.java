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

public class MyThreadReNotify implements Runnable {
	String name;

	Thread t;
	String backendGroupTokenLog = "e4QuXuo08qZo6CK4wXLoO7djTuB9E9yEhIO2KusK6W9";

	LineNotify lineNotifyLog = new LineNotify("LINE", backendGroupTokenLog);

	MyThreadReNotify() {

		name = "gr.didx.notify ";
		t = new Thread(this, name);
		t.start();
	}

	public void run() {
		for (int i = 0; i < 10000000; i++) {
			lineNotifyLog.NotifyInfo( this.name +" run...." );
			runDo();
			Utils.Sleep(5*60*1000);
		}

	}

	public void runDo() {
		
		ClientConfig config = WorkerHelper.GetWorkerClientConfig();

		int countNull = 0;
		ObjectMapper mapper = null;
		QueueRabbitMQ queueConnect = null;
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper = new ObjectMapper();
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			String G_queue = "gr.didx.notify";
			queueConnect = new QueueRabbitMQ();
			queueConnect.Init(config.SERVER_RABBITMQ_URL);
			queueConnect.Connect();

			Map<String, Object> args = new HashMap<String, Object>();
			// args.put("x-dead-letter-exchange", "ms.direct");
			args.put("x-max-priority", 10);
			queueConnect.StartDequeue(G_queue, args);
			String ls = "";
			int cachdo = 60 * 60 * 1000;// 15 phut chay lai
			for (int i = 0; i < 10000000; i++) {

				MessageQueue messageQueue = null;
				QueueMesssageEventArgs msg = null;

				msg = queueConnect.Dequeue(G_queue);
				if (msg != null) {

					messageQueue = mapper.readValue(msg.Message, MessageQueue.class);

					if (messageQueue != null) {
						if (System.currentTimeMillis() - messageQueue.CreatedDate.getTime() > cachdo) {
							 

								if (messageQueue.Note.contains("message-promotion")) {
									String queue = "gr.dc4.didx.promotionbk";
									String queuebk = "gr.dc2.didx.promotionbk";
									String queueBeta = "gr.beta.didx.promotionbk";
									messageQueue.Note = "DIDX_RENOTI";
									QueueHelper.Current("amqp://tgdd:Tgdd2012@192.168.2.55:5672").PushAll(queue, queue,
											queuebk, queueBeta, messageQueue, false, "", 4);
									queueConnect.Ack(msg.Tag);
								}
								if (messageQueue.Note.contains("message-price")) {
									String queue = "gr.dc4.didx.pricebk";
									String queuebk = "gr.dc2.didx.pricebk";
									String queueBeta = "gr.beta.didx.pricebk";
									messageQueue.Note = "DIDX_RENOTI";
									QueueHelper.Current("amqp://tgdd:Tgdd2012@192.168.2.55:5672").PushAll(queue, queue,
											queuebk, queueBeta, messageQueue, false, "", 4);
									queueConnect.Ack(msg.Tag);
								}
							 

						}else { //chua toi gio xử lý
							 queueConnect.Nack2( msg.Tag);
							Utils.Sleep(60*1000);
						}
					}

				} else {
					countNull++;
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}
		} catch (Throwable e) {
			lineNotifyLog.NotifyInfo(config.DATACENTER+":"+this.name +" BANH BONG,CHECK GAP" );
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logs.LogException(e);
		} finally {
			try {
				queueConnect.Close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println(name + " exiting.");
	}

}