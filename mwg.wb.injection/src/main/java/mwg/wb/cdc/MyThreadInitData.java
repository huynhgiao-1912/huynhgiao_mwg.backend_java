package mwg.wb.cdc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.WorkerHelper;
import mwg.wb.client.OracleClient;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.graph.OrientDBFactory;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.queue.QueueMesssageEventArgs;
import mwg.wb.client.queue.QueueRabbitMQ;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.notify.LineNotify;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.MessageQueuePushType;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import oracle.jdbc.OracleTypes;

public class MyThreadInitData implements Runnable {
	String name;
	 
	Thread t;

	MyThreadInitData() {

		name = "reintidata";
		t = new Thread(this, name);
		t.start();
	}

	public void run() {
		for (int i = 0; i < 10000000; i++) {
			lineNotifyLog.NotifyInfo( this.name +" run...." );
			runDo();
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	public Map<String, Long> Hasrocessed = new HashMap<String, Long>();
String backendGroupTokenLog = "e4QuXuo08qZo6CK4wXLoO7djTuB9E9yEhIO2KusK6W9";

		LineNotify lineNotifyLog = new LineNotify("LINE", backendGroupTokenLog);

	public void runDo() {
		
		ClientConfig config = WorkerHelper.GetWorkerClientConfig();
		OracleClient dbclient = null;
		Connection condb = null;
		String OracledbURL = config.DB_URL;
		int countNull = 0;
		ObjectMapper mapper = null;
		QueueRabbitMQ queueConnect = null;
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper = new ObjectMapper();
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			String queue = "gr.reinitdata";

			queueConnect = new QueueRabbitMQ();
			queueConnect.Init(config.SERVER_RABBITMQ_URL);
			queueConnect.Connect();
			//dbclient = new OracleClient(OracledbURL, config.DB_USER, config.DB_PASS);
			dbclient = new OracleClient(config.DB_CONNECTIONSTRING, config.tns_admin, config.wallet_location,1);

			condb = dbclient.getConnection();
			 
			Map<String, Object> args = new HashMap<String, Object>();
			// args.put("x-dead-letter-exchange", "ms.direct");
			args.put("x-max-priority", 10);
			queueConnect.StartDequeue(queue, args);
			 
			for (int i = 0; i < 10000000; i++) {

				MessageQueue messageQueue = null;
				QueueMesssageEventArgs msg = null;

				msg = queueConnect.Dequeue(queue);
				if (msg != null) {

					messageQueue = mapper.readValue(msg.Message, MessageQueue.class);

					if (messageQueue != null) {
						
						
						if(condb==null || condb!=null &&  condb.isClosed()) { 
							 Utils.Sleep(15000);
							 Logs.LogException("condb==null || condb!=null &&  condb.isClosed()");
							 condb = dbclient.openConnection(OracledbURL, config.DB_USER, config.DB_PASS);
							 
						}
						boolean push=true;
						String strIdentify = messageQueue.Identify;
						 String source = messageQueue.Source+"";
						Date date = messageQueue.CreatedDate;
						String key  = strIdentify ;
						var lastUpdatedw = Hasrocessed.get(key );
						long lastUpdated = lastUpdatedw == null ? 0 : lastUpdatedw;
						if (date.getTime() < lastUpdated) {
						 push = false;
						}
						 
						if(push) {
							
						// pm_product|productid|1274062000934|
						String[] arr = (strIdentify + "|").split("\\|");
						String table = arr[0];
						String recordName = arr[1];
						String recordValue = arr[2];
						String otherCol = arr.length >= 4 ? StringUtils.strip(arr[3], ",") : "";
						if (table.equals("productpromotion")
								|| table.equals("productprice")
								|| table.equals("pricedefault")
									|| table.equals("product_detail_cache")
								|| table.equals("product_old_code")
								
								) {
							queueConnect.Ack(msg.Tag);
						} else {
							MessageQueue message = new MessageQueue();
							Logs.WriteLine("table " + table + "otherCol " + otherCol);
							message.SqlList = SysnDataBK.BuildSqlList(message, strIdentify, table, recordName,
									recordValue, otherCol, DataAction.Update, condb, "", false, message);

							Logs.WriteLine("SqlList:" + message.SqlList.size());
							String qu = "gr.dc4.sql.priority.sysdata";
							String qu2 = "gr.dc4.sql.priority.sysdata";
							String qubk = "gr.dc2.sql.priority.sysdata";
							String qudev = "gr.beta.sql.priority.sysdata";
							if(table.equals("pm_currentinstock")) {
								long rid = Long.valueOf(recordValue); 
								int h = Utils.GetQueueNum5(rid);
								qu = "gr.dc4.sql.stock" + h;
								qu2 = "gr.dc4.sql.stock" + h;
								qubk = "gr.dc2.sql.stock" + h;
								qudev = "gr.beta.sql.stock";
								message.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_STOCK;
								
							}
							if( source.equals("Validate") && table.equals("product_gallery")) {
								 
								message.Identify=recordValue;
								message.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_USER_GALLERY_FROM_INJECT;
								
							}
							message.DataCenter = messageQueue.DataCenter;
							message.Action = DataAction.Update;
							message.ClassName = "mwg.wb.pkg.upsert";
							message.CreatedDate = Utils.GetCurrentDate();
							//message.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_RECREATE_EDGE;
							message.RepushCount = 1;
							message.Note="DIDX_LOG|validate-"+table;
							message.IsCreateEdge = true;
							if (messageQueue.SqlList != null) {
								for (SqlInfo item : messageQueue.SqlList) {
									message.SqlList.add(item);
								}
							}

							try {
								// send co uu tien
								QueueHelper.Current(config.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, message,
										false, "", messageQueue.DataCenter);
								queueConnect.Ack(msg.Tag);
								long dt2 = System.currentTimeMillis();
								Hasrocessed.put(key, dt2);
							} catch (Exception ex) {
								// TODO Auto-generated catch block
								Logs.LogException(ex);
							}
						}
						}else {
							
							queueConnect.Ack(msg.Tag);
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
			lineNotifyLog.NotifyInfo(config.DATACENTER+":"+ this.name +":"+e.getMessage());
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

			try {
			 
				dbclient.closeConnection();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println(name + " exiting.");
	}

}