package mwg.wb.client.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mwg.wb.common.DataCenterType;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageLogType;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.notify.LineNotify;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.MyException;
import mwg.wb.common.RefSql;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.Utils;
import mwg.wb.model.common.Upsertable;

public class QueueHelper {

	public static QueueHelper instance = null;
	public volatile static String rabbitUrl = "";

//	public static synchronized QueueHelper Current() {
//
//		if (instance == null) {
//			
//			instance = new QueueHelper();
//			return instance;
//		} else {
//			return instance;
//
//		}
//	}
	public static synchronized QueueHelper Current(String url) {

		if (instance == null) {

			instance = new QueueHelper(url);
			return instance;
		} else {
			return instance;

		}
	}

	public QueueRabbitMQ queueConnect = null;
	public Gson gSon = new GsonBuilder().setDateFormat(GConfig.DateFormatString).create();

	public QueueHelper(String url) {
		rabbitUrl = url;
		queueConnect = new QueueRabbitMQ();
		queueConnect.Init(url);
		queueConnect.Connect();
	}

//	public QueueHelper() {
//		queueConnect = new QueueRabbitMQ();
//		queueConnect.Init("amqp://tgdd:Tgdd2012@192.168.2.55:5672");
//		queueConnect.Connect();
//	}

	public boolean Push(String queue, MessageQueue message) throws Exception {

		String json = gSon.toJson(message, message.getClass());

		return queueConnect.SendMessage(queue, json);

	}

//	public boolean PushExchange(String exchangeName, MessageQueue message) throws Exception {
//
//		String json = gSon.toJson(message, message.getClass());
//		return queueConnect.SendMessageExchange(exchangeName, json);
//
//	}

	public boolean PushPriority(String queue, MessageQueue message, int pro) throws Exception {

		String json = gSon.toJson(message, message.getClass());

		return queueConnect.SendMessagePriority(queue, json, pro);

	}

	public boolean PushAll_(String queue, String queue2, String queueBK, String queueDev, MessageQueue messageRepush,
			boolean isLog, int DataCenter) throws Exception {
		if (DataCenter != 3) {

			if (isLog) {
				PushPriority(queue2, messageRepush, 10);
			} else {

				Push(queue2, messageRepush);
			}
			Push(queueBK, messageRepush);

		} else {
			try {
				Push(queueDev, messageRepush);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return true;

	}

	public boolean PushAll(String queue, String queue2, String queueBK, String queueDev, MessageQueue messageRepush,
			boolean isLog, String strNOTE, int DataCenter, LineNotify lineNotify) throws Exception {
		for (int c = 0; c < 100000000; c++) {
			try {

				boolean rsPush = PushAll(queue, queue, queueBK, queueDev, messageRepush, isLog, strNOTE, DataCenter);
				if (rsPush) {
					return true;

				} else {
					Thread.sleep(3000 * c);
				}

			} catch (Throwable e) {

				String errorMessage = Logs.GetStacktrace(e);
				Logs.LogException(e);
				if (c % 5 == 0 && c > 2) {
					String messageSL = "Inject error ( " + c + " times ), PushAll:" + errorMessage;
					lineNotify.NotifyError(messageSL);
				}
				Thread.sleep(3000 * c);

			}
		}
		return false;
	}

	public boolean PushAll(String queue, String queue2, String queueBK, String queueDev, MessageQueue messageRepush,
			boolean isLog, String strNOTE, int DataCenter) throws Exception {
		int t = Utils.GetMessageSaveLog(strNOTE);

		if (DataCenter > 0) {
			messageRepush.DataCenter=DataCenter;
			if (rabbitUrl.contains("10.1.6.139")) {
				messageRepush.DataCenter=3;
				//if (DataCenter == DataCenterType.Beta) {
					
					if (t == MessageLogType.DIDX_TOP || t == MessageLogType.DIDX_TOP_LOG) {
						boolean x = PushPriority(queueDev, messageRepush, 10);
						if (x == false)
							new MyException("PushAll fail");
					} else {

						boolean x = Push(queueDev, messageRepush);
						if (x == false)
							new MyException("PushAll fail");
					}

				//}
			} else {
				if (DataCenter == DataCenterType.Main) {
					
					if (t == MessageLogType.DIDX_TOP || t == MessageLogType.DIDX_TOP_LOG) {
						boolean x = PushPriority(queue2, messageRepush, 10);
						if (x == false)
							new MyException("PushAll fail");
					} else {

						boolean x = Push(queue2, messageRepush);
						if (x == false)
							new MyException("PushAll fail");
					}
				}
				if (DataCenter == DataCenterType.Backup) {
					if (t == MessageLogType.DIDX_TOP || t == MessageLogType.DIDX_TOP_LOG) {
						boolean x = PushPriority(queueBK, messageRepush, 10);
						if (x == false)
							new MyException("PushAll fail");
					} else {

						boolean x = Push(queueBK, messageRepush);
						if (x == false)
							new MyException("PushAll fail");
					}

				}
//				if (DataCenter == DataCenterType.Beta) {
//					if (isLog) {
//						PushPriority(queueDev, messageRepush, 10);
//					} else {
//
//						Push(queueDev, messageRepush);
//					}
//
//				}
			}

		} else {

			// kh push leen rabit beta voi queue live
			if (!rabbitUrl.contains("10.1.6.139")) {
				if (t == MessageLogType.DIDX_TOP || t == MessageLogType.DIDX_TOP_LOG) {
//					String qu = "gr.dc4.sql" + ie;
//					String qu2 = "gr.dc4.sql" + ie;
//					String qubk = "gr.dc2.sql" + ie;
//					String qudev = "gr.beta.sql";
					//String queue, String queue2, String queueBK, String queueDev
					messageRepush.DataCenter=4;
					boolean x = PushPriority(queue2, messageRepush, 10);
					if (x == false)
						new MyException("PushAll fail");
				} else {
					messageRepush.DataCenter=4;
					boolean x = Push(queue2, messageRepush);
					if (x == false)
						new MyException("PushAll fail");
				}
				if (t == MessageLogType.DIDX_TOP || t == MessageLogType.DIDX_TOP_LOG) {
					messageRepush.DataCenter=2;
					boolean x = PushPriority(queueBK, messageRepush, 10);
					if (x == false)
						new MyException("PushAll fail");
				} else {
					messageRepush.DataCenter=2;
					boolean x = Push(queueBK, messageRepush);
					if (x == false)
						new MyException("PushAll fail");
				}
			} else {
				if (t == MessageLogType.DIDX_TOP || t == MessageLogType.DIDX_TOP_LOG) {
					messageRepush.DataCenter=3;
					boolean x = PushPriority(queueDev, messageRepush, 10);
					if (x == false)
						new MyException("PushAll fail");
				} else {
					messageRepush.DataCenter=3;
					boolean x = Push(queueDev, messageRepush);
					if (x == false)
						new MyException("PushAll fail");
				}
			}

		}

		return true;

	}

	public boolean InjectPushAll(String queue, String queue2, String queueBK, String queueDev,
			MessageQueue messageRepush, boolean isLog, int DataCenter) throws Exception {
		if (DataCenter == 3) {

			Push(queueDev, messageRepush);
			return true;
		}
		if (isLog) {
			PushPriority(queue2, messageRepush, 10);
			PushPriority(queueBK, messageRepush, 10);
			PushPriority(queueDev, messageRepush, 10);
		} else {

			Push(queue2, messageRepush);
			Push(queueBK, messageRepush);
			Push(queueDev, messageRepush);
		}

		return true;

	}

	public <T extends Upsertable> void pushUpsertODBObjects(String classname, String index, String queue, String queue2,
			String queueBK, String queueDev, List<T> objects, boolean isLog, String note, int DataCenter)
			throws Exception {
		pushUpsertODBObjects(classname, index, queue, queue2, queueBK, queueDev, objects, null, null, isLog, note,
				DataCenter);
	}

	public <T extends Upsertable> void pushUpsertODBObjects(String classname, String index, String queue, String queue2,
			String queueBK, String queueDev, List<T> objects, String deleteindex, List<String> deletevalues,
			boolean isLog, String note, int DataCenter) throws Exception {
		MessageQueue messageRepush = new MessageQueue();
		messageRepush.SqlList = new ArrayList<SqlInfo>();
		if (deleteindex != null && deletevalues != null) {
			messageRepush.SqlList.addAll(deletevalues.stream().map(x -> {
				SqlInfo sqlinfo = new SqlInfo();
				sqlinfo.Sql = "update " + classname + " set isdeleted=1 where " + deleteindex + "='" + x + "'";
				return sqlinfo;
			}).collect(Collectors.toList()));
		}
		messageRepush.SqlList.addAll(objects.stream().map(p -> {
			SqlInfo sqlinfo = new SqlInfo();
			RefSql ref = new RefSql();
			Utils.BuildSql(isLog, note, classname, index, p.indexValue(), p, ref);
			sqlinfo.Sql = ref.Sql;
			sqlinfo.Params = ref.params;
			return sqlinfo;
		}).collect(Collectors.toList()));
		messageRepush.Action = DataAction.Update;
		messageRepush.ClassName = "mwg.wb.pkg.upsert.Upsert";
		messageRepush.CreatedDate = Utils.GetCurrentDate();
		messageRepush.Type = 0;

		PushAll(queue, queue2, queueBK, queueDev, messageRepush, isLog, note, DataCenter);
	}

	public <T extends Upsertable> void pushUpsertODBObjects_old(String classname, String index, String queuename,
			String queuenamebk, List<T> objects, boolean log, String note) throws Exception {
		MessageQueue messageRepush = new MessageQueue();
		messageRepush.SqlList = new ArrayList<SqlInfo>();
		objects.forEach(p -> {
			SqlInfo sqlinfo = new SqlInfo();
			RefSql ref = new RefSql();
			Utils.BuildSql(log, note, classname, index, p.indexValue(), p, ref);
			sqlinfo.Sql = ref.Sql;
			sqlinfo.Params = ref.params;
			messageRepush.SqlList.add(sqlinfo);
		});
		messageRepush.Action = DataAction.Update;
		messageRepush.ClassName = "mwg.wb.pkg.upsert.Upsert";
		messageRepush.CreatedDate = Utils.GetCurrentDate();
		messageRepush.Type = 0;

		if (log) {
			PushPriority(queuename, messageRepush, 10);
		} else {
			Push(queuename, messageRepush);
		}
		Push(queuenamebk, messageRepush);
	}
}
