package mwg.wb.cdc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.business.WorkerHelper;
import mwg.wb.client.OracleClient;
import mwg.wb.client.queue.QueueMesssageEventArgs;
import mwg.wb.client.queue.QueueRabbitMQ;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.Utils;
import mwg.wb.common.notify.LineNotify;
import mwg.wb.model.api.ClientConfig;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class MyThreadInit implements Runnable {
	String name;

	Thread t;

	MyThreadInit() {

		name = "init";
		t = new Thread(this, name);
		t.start();
	}

	public void run() {
		for (int i = 0; i < 10000000; i++) {
			runDo();
		}

	}

	// EXEC TGDD_NEWS.DGRAPH_PUSH(v_ProductID => 198792,v_SiteID => 12,v_LanguageID
	// => 'vi-VN',v_Note =>'manual');

//	v_ProductID   Product.PRODUCTID%TYPE,
//	  v_SiteID      PRODUCT_LANGUAGE.SITEID%TYPE DEFAULT 12,
//	  v_LanguageID  PRODUCT_LANGUAGE.LANGUAGEID%TYPE DEFAULT 'vi-VN',
//	  v_Note        VARCHAR2 DEFAULT NULL
	public boolean DGRAPH_PUSH_STATUS(LineNotify lineNotifyLog, ClientConfig config, int ProductID, int SiteID, String LanguageID, String Note, int DataCenter){
		OracleClient dbclient = null;
		String OracledbURL = config.DB_URL;
		Connection condb = null;
		CallableStatement cs = null;
		try {
			dbclient = new OracleClient(config.DB_CONNECTIONSTRING, config.tns_admin, config.wallet_location,1);
			condb = dbclient.getConnection();

			cs = condb.prepareCall("{call TGDD_NEWS.DGRAPH_PUSH_STATUS(?,?,?,?,?)}");
			cs.setInt(1, ProductID);
			cs.setString(2,Note);
			cs.setInt(3, SiteID);
			cs.setString(4, LanguageID);
			cs.setInt(5, DataCenter);
			cs.execute();
			return true;

		} catch (Throwable e) {
			Logs.WriteLine("SQLException: " + e.getMessage());
			Logs.LogException(e);
			lineNotifyLog.NotifyError("DC:" + config.DATACENTER +  " PUSH_STATUS" + " Throwable :" + e.getMessage());

		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					Logs.WriteLine("SQLException: " + e.getMessage());
				}
			}

			try {
				dbclient.closeConnection();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Logs.LogException(" ERROR : DGRAGH_PUSH_STATUS "); // lỗi quăng vô file ../error/
		return false;
	}
	public boolean DGRAPH_PUSH(LineNotify lineNotifyLog, ClientConfig config, int ProductID, int SiteID,
			String LanguageID, String Note, int DataCenter) {
		OracleClient dbclient = null;
		String OracledbURL = config.DB_URL;
		Connection condb = null;
		CallableStatement cs = null;
		try {
			//dbclient = new OracleClient(OracledbURL, config.DB_USER, config.DB_PASS);
			dbclient = new OracleClient(config.DB_CONNECTIONSTRING, config.tns_admin, config.wallet_location,1);
			// lineNotifyLog.NotifyMessage("DB_CONNECTIONSTRING: " + config.DB_CONNECTIONSTRING);
			condb = dbclient.getConnection();

			cs = condb.prepareCall("{call TGDD_NEWS.DGRAPH_PUSH(?,?,?,?,?)}");

			cs.setInt(1, ProductID);
			cs.setInt(2, SiteID);
			cs.setString(3, LanguageID);
			cs.setString(4, Note);
			cs.setInt(5, DataCenter);
			cs.execute();

			return true;

		} catch (Throwable e) {
			Logs.WriteLine("SQLException: " + e.getMessage());
			Logs.LogException(e);
			lineNotifyLog.NotifyError("DC" + config.DATACENTER + " Throwable :" + e.getMessage());

		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					Logs.WriteLine("SQLException: " + e.getMessage());
				}
			}

			try {
				dbclient.closeConnection();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Logs.WriteLine("null cc");
		return false;

	}

	public void runDo() {
		String backendGroupTokenLog = "e4QuXuo08qZo6CK4wXLoO7djTuB9E9yEhIO2KusK6W9";

		LineNotify lineNotifyLog = new LineNotify("LINE", backendGroupTokenLog);

		ClientConfig config = WorkerHelper.GetWorkerClientConfig();

		int countNull = 0;
		ObjectMapper mapper = null;
		QueueRabbitMQ queueConnect = null;
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper = new ObjectMapper();
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			String queue = "ms.init";
			queueConnect = new QueueRabbitMQ();
			queueConnect.Init(config.SERVER_RABBITMQ_URL);
			queueConnect.Connect();

			Map<String, Object> args = new HashMap<String, Object>();
			// args.put("x-dead-letter-exchange", "ms.direct");
			args.put("x-max-priority", 10);
			queueConnect.StartDequeue("ms.init", args);

			for (int i = 0; i < 10000000; i++) {

				// chỉ 5s chạy 1 lần, và 1 lần chỉ 2 sp thôi
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {

				}

				MessageQueue messageQueue = null;
				QueueMesssageEventArgs msg = null;

				msg = queueConnect.Dequeue(queue);
				if (msg != null) {

					messageQueue = mapper.readValue(msg.Message, MessageQueue.class);

					if (messageQueue != null) {
						int sp = 0;
						String[] listID = messageQueue.Identify.split(",");
						for (String strId : listID) {
							int pid = Utils.toInt(strId.trim());
							if (pid > 0) {
								if(sp>4)break;
								sp++;
								Logs.LogException("messageQueue.Identify:" + messageQueue.Identify);
								// tam thoi bo DIDX_LOG
								if (!Utils.StringIsEmpty(messageQueue.ClassName)
										&& messageQueue.ClassName.toString().equals("NEWS")) {
									DGRAPH_PUSH_NEWS(lineNotifyLog, config, pid, messageQueue.SiteID, messageQueue.Lang,
											"DIDX_TOP DIDX_LOG|" + messageQueue.Identify, messageQueue.DataCenter);
									Logs.WriteLine("DGRAPH_PUSH_NEWS " + pid + " lang " + messageQueue.Lang);

								}else if(!Utils.StringIsEmpty(messageQueue.ClassName) && messageQueue.ClassName.toString().equals("STATUS")){ // push status
									String note = "DIDX_TOP DIDX_LOG|" +messageQueue.Identify;
									DGRAPH_PUSH_STATUS(lineNotifyLog,config,pid,messageQueue.Storeid,messageQueue.Lang,note,messageQueue.DataCenter);
									Logs.WriteLine("DGRAPH_PUSH_STATUS " + pid + " lang " + messageQueue.Lang);
									lineNotifyLog.NotifyInfo("DC:" + config.DATACENTER + " START-INIT:" + note
											+ ",siteid:" + messageQueue.SiteID + ",Lang:" + messageQueue.Lang + " pid:"
											+ pid, config.DATACENTER);
								} else {

									String note =  "DIDX_TOP|" + messageQueue.Identify;
									DGRAPH_PUSH(lineNotifyLog, config, pid, messageQueue.SiteID, messageQueue.Lang,
											note, messageQueue.DataCenter);
									Logs.WriteLine("DGRAPH_PUSH " + pid + " lang " + messageQueue.Lang);
									lineNotifyLog.NotifyInfo("DC" + config.DATACENTER + " START-INIT:" + note
											+ ",siteid:" + messageQueue.SiteID + ",Lang:" + messageQueue.Lang + " pid:"
											+ pid, config.DATACENTER);

								}
							}

							// }
						}

					} else {
						Logs.LogException("ky vay");
					}
					queueConnect.Ack(msg.Tag);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logs.LogException(e);
			lineNotifyLog.NotifyError(config.DATACENTER + ":" + this.name + " BANH BONG, CHECK GAP ");
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

	private boolean DGRAPH_PUSH_NEWS(LineNotify lineNotifyLog, ClientConfig config, int newsID, int siteID, String lang,
			String note, int dataCenter) {
		OracleClient dbclient = null;

		Connection condb = null;
		CallableStatement cs = null;
		try {
			String OracledbURL = config.DB_URL;
			//dbclient = new OracleClient(OracledbURL, config.DB_USER, config.DB_PASS);
			dbclient = new OracleClient(config.DB_CONNECTIONSTRING, config.tns_admin, config.wallet_location,1);
			condb = dbclient.getConnection();

			cs = condb.prepareCall("{call TGDD_NEWS.DGRAPH_PUSH_NEWS(?,?,?)}");

			cs.setInt(1, newsID);
			cs.setString(2, note);
			cs.setInt(3, dataCenter);
			cs.execute();

			return true;

		} catch (Throwable e) {
			Logs.WriteLine("SQLException: " + e.getMessage());
			Logs.LogException(e);
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					Logs.WriteLine("SQLException: " + e.getMessage());
				}
			}
			try {
				dbclient.closeConnection();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Logs.WriteLine("null");
		return false;
	}

}