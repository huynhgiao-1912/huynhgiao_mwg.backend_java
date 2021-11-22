package MWG.RabbitMonitor;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;
import com.rabbitmq.http.client.HttpComponentsRestTemplateConfigurator;

import mwg.wb.common.Utils;
import mwg.wb.common.notify.LineNotify;
import mwg.wb.common.notify.NotifyHelper;

public class App {
	static Client client = null;
	static Client clientNew = null;
	static NotifyHelper lineNotify = null;
	static String queueMonitorGroupToken = "npsoO1hXGyCWNfOoL2QnTuIjaxRLKRFyi9mja45hiKx";
	static String test = "ShPSwKt3f5UoUJRvEw6SqyZtwVJyfiJ0xXQLQkjqY2A";
	static String RabbitHost = "http://192.168.2.55:15672/api";
	static String RabbitHostNew = "http://192.168.2.55:35672/api";
	static String RabbitUser = "tgdd";
	static String RabbitPass = "Tgdd2012";

	static String[] ExceptQueue = new String[] { "bhx.deadqueue", "bhx.product", "bhxjava.test",
			"bhxnew.currentinstock", "didx.rcmorder", "didx.gameapp", "dmx.deadqueue", "dmx.productdeliver",
			"dtsr.product", "dtsr.productse", "med.deadqueue", "temp.membershippushall", "dmx.productdelivery",
			"msgbus.bhx", "msgbus.bhxonline", "msgbus.erpusercachecheck", "msgbus.erpmdcheck", "gr.dc2.sql.stock",
			"gr.dc4.sql.stock" };

	public static void main(String[] args) {

		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			try {
				List<QueueMonitor> listQueue = new ArrayList<QueueMonitor>();
				if (client == null) {
					client = new Client(new ClientParameters().url(RabbitHost).username(RabbitUser).password(RabbitPass)
							.restTemplateConfigurator(new HttpComponentsRestTemplateConfigurator()));
				}
				if (clientNew == null) {
					clientNew = new Client(
							new ClientParameters().url(RabbitHostNew).username(RabbitUser).password(RabbitPass)
									.restTemplateConfigurator(new HttpComponentsRestTemplateConfigurator()));
				}
				if (lineNotify == null) {
					lineNotify = new LineNotify("LINE", queueMonitorGroupToken);

				}
//				lineNotify.Notify("===START MONITOR:"
//						+ new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() + "===");
				var allQueue = client.getQueues();
				var allQueueNew = clientNew.getQueues();

				var filterQueue = allQueue.stream()
						.filter(x -> !x.getName().contains("led") && !x.getName().startsWith("didx")
								&& !x.getName().contains("dhcd")
								&& !Arrays.stream(ExceptQueue).anyMatch(y -> y.equals(x.getName())))
						.collect(Collectors.toList());

				var filterQueueNew = allQueueNew.stream().filter(x -> x.getName().startsWith("didx"))
						.collect(Collectors.toList());

				for (var queue : filterQueue) {

					var queueInfo = new QueueMonitor();
					queueInfo.NumberMessage = queue.getMessagesReady();
					queueInfo.QueueName = queue.getName();
					queueInfo.TimeCheck = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
					queueInfo.PreviousMonitor = queueInfo.TimeCheck;

					if (queue.getMessageStats() != null && queue.getMessageStats().getAckDetails() != null
							&& queue.getMessagesReady() > 0) {
						queueInfo.Delivery_Get = queue.getMessageStats().getAckDetails().getRate();

						if (queue.getMessagesReady() > 10000) {

							queueInfo.Type = 1;

						}

						else if (queue.getMessageStats().getAckDetails().getRate() <= 0) {

							if ((queue.getName().equals("chat.archive") && queue.getMessagesReady() > 30)
									|| queue.getMessagesReady() > 100) {

								if (queue.getName().contains("gr.") && queue.getMessagesReady() <= 500) {

								} else {
									queueInfo.Type = 2;
								}

							}

						}
					}

					var theLastidl = queue.getIdleSince();
					// !queue.getState().equals("running")
					if (!Utils.StringIsEmpty(theLastidl)) {
						Date lastIdlDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(theLastidl);
						LocalDateTime plusDate = lastIdlDate.toInstant().atZone(ZoneId.systemDefault())
								.toLocalDateTime().plusHours(7);

						queueInfo.Type = 3;
						queueInfo.IdleTime = plusDate;
						queueInfo.IsIdle = true;

					}

					if (queueInfo.Type == 1 || queueInfo.Type == 2 || queueInfo.Type == 3)
						listQueue.add(queueInfo);

				}

				for (var queue : filterQueueNew) {

					var queueInfo = new QueueMonitor();
					queueInfo.NumberMessage = queue.getMessagesReady();
					queueInfo.QueueName = queue.getName();
					queueInfo.TimeCheck = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
					queueInfo.PreviousMonitor = queueInfo.TimeCheck;

					if (queue.getMessageStats() != null && queue.getMessageStats().getAckDetails() != null
							&& queue.getMessagesReady() > 0) {
						queueInfo.Delivery_Get = queue.getMessageStats().getAckDetails().getRate();

						if (queue.getMessagesReady() > 10000) {

							queueInfo.Type = 1;

						}

						else if (queue.getMessageStats().getAckDetails().getRate() <= 0) {

							if ((queue.getName().equals("chat.archive") && queue.getMessagesReady() > 30)
									|| queue.getMessagesReady() > 100) {

								if (queue.getName().contains("gr.") && queue.getMessagesReady() <= 500) {

								} else {
									queueInfo.Type = 2;
								}

							}

						}
					}

					var theLastidl = queue.getIdleSince();
					// !queue.getState().equals("running")
					if (!Utils.StringIsEmpty(theLastidl)) {
						Date lastIdlDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(theLastidl);
						LocalDateTime plusDate = lastIdlDate.toInstant().atZone(ZoneId.systemDefault())
								.toLocalDateTime().plusHours(7);

						queueInfo.Type = 3;
						queueInfo.IdleTime = plusDate;
						queueInfo.IsIdle = true;

					}

					if (queueInfo.Type == 1 || queueInfo.Type == 2 || queueInfo.Type == 3)
						listQueue.add(queueInfo);

				}

				// send notify
				if (listQueue.size() > 0) {
					var listType12 = listQueue.stream().filter(x -> x.Type == 1 || x.Type == 2)
							.collect(Collectors.toList());
					var listType3 = listQueue.stream().filter(x -> x.Type == 3).collect(Collectors.toList());

					if (listType12 != null && listType12.size() > 0) {
						// lineNotify.Notify("****Danh sách queue tồn đọng message*****");
						for (var item : listType12) {
							String message = item.CreateMessage();
							if (!Utils.StringIsEmpty(message)) {

								lineNotify.Notify(message);
							}
						}

					}
					if (listType3 != null && listType3.size() > 0
							&& listType3.stream().anyMatch(x -> !Utils.StringIsEmpty(x.CreateMessage()))) {
						// lineNotify.Notify("********Danh sách queue IDLE*********");
						for (var item : listType3) {
							String message = item.CreateMessage();
							if (!Utils.StringIsEmpty(message)) {

								lineNotify.Notify(message);
							}
						}

					}
				}

				LocalDateTime localDateTime = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

				if (localDateTime.getHour() >= 0 && localDateTime.getHour() <= 5) {
					System.out.println("***Process will continue at " + localDateTime.plusHours(2));
					Thread.sleep(7200000);
				} else {
					System.out.println("***Process will continue at " + localDateTime.plusMinutes(30));
					Thread.sleep(1800000);
				}
			} catch (Throwable e) {

				e.printStackTrace();
				
			}

		}

	}
}
