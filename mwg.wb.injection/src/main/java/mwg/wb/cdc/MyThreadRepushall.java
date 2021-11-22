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

public class MyThreadRepushall implements Runnable {
	String name;

	Thread t;

	MyThreadRepushall() {

		name = "initall";
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
	public boolean DGRAPH_PUSH(LineNotify lineNotifyLog, ClientConfig config, int ProductID, int SiteID,
			String LanguageID, String Note, int DataCenter) {
		OracleClient dbclient = null;
		String OracledbURL = config.DB_URL;
		Connection condb = null;
		CallableStatement cs = null;
		try {
			//dbclient = new OracleClient(OracledbURL, config.DB_USER, config.DB_PASS);
			dbclient = new OracleClient(config.DB_CONNECTIONSTRING, config.tns_admin, config.wallet_location,1);
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
			if (lineNotifyLog != null)
				lineNotifyLog.NotifyInfo("DC" + config.DATACENTER + " Throwable :" + e.getMessage());

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
			String queue = "gr.repushall";
			queueConnect = new QueueRabbitMQ();
			queueConnect.Init(config.SERVER_RABBITMQ_URL);
			queueConnect.Connect();

			Map<String, Object> args = new HashMap<String, Object>();
			// args.put("x-dead-letter-exchange", "ms.direct");
			args.put("x-max-priority", 10);
			queueConnect.StartDequeue("gr.repushall", args);
			int cachdo = 15 * 60 * 1000;
			for (int i = 0; i < 10000000; i++) {

				MessageQueue messageQueue = null;
				QueueMesssageEventArgs msg = null;

				msg = queueConnect.Dequeue(queue);
				if (msg != null) {

					messageQueue = mapper.readValue(msg.Message, MessageQueue.class);

					if (messageQueue != null) {
						if (System.currentTimeMillis() - messageQueue.CreatedDate.getTime() > cachdo) {

							int pid = Utils.toInt(messageQueue.Identify);
							if (pid > 0) {

								 
								String note = "XXX|PUSHALL";
								DGRAPH_PUSH(null, config, pid, messageQueue.SiteID, messageQueue.Lang, note,
										messageQueue.DataCenter);

							}
							queueConnect.Ack(msg.Tag);
						}else { 
							//chua toi gio xử lý
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