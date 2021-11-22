package mwg.wb.client.queue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;

public class QueueLogsHelper {

	public static QueueLogsHelper instance = null;

	public static synchronized QueueLogsHelper Current() {

		if (instance == null) {
			instance = new QueueLogsHelper();
			return instance;
		} else {
			return instance;

		}
	}

	public QueueRabbitMQ queueConnect = null;
	public Gson gSon = new GsonBuilder().setDateFormat(GConfig.DateFormatString).create();

	public QueueLogsHelper() {
		queueConnect = new QueueRabbitMQ();
		queueConnect.Init("amqp://tgdd:Tgdd2012@192.168.2.55:5672");
		queueConnect.Connect();
	}
	public boolean PushExchange(String exchangeName, String json) throws Exception {
 
		return queueConnect.SendMessageExchange(exchangeName, json);

	}
	 

	public boolean PushExchange(String exchangeName, MessageQueue message) throws Exception {

		String json = gSon.toJson(message, message.getClass());
		return queueConnect.SendMessageExchange(exchangeName, json);

	}

	 

}
