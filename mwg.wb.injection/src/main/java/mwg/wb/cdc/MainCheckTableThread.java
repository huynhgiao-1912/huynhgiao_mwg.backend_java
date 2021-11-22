package mwg.wb.cdc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.transform.tailrec.HasRecursiveCalls;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import mwg.wb.business.ProductHelper;
import mwg.wb.business.WorkerHelper;
import mwg.wb.client.OracleClient;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.queue.QueueRabbitMQ;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.EdgeInfo;
import mwg.wb.common.FileHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.GraphDBConstant;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.MessageQueuePushType;
import mwg.wb.common.MessageValidate;
import mwg.wb.common.PushType;
import mwg.wb.common.RefSql;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.SSObject;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.SqlInfoType;
import mwg.wb.common.TableEdgesInfo;
import mwg.wb.common.Utils;
import mwg.wb.common.notify.LineNotify;

import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.pricestrings.PriceStringBO;
import oracle.jdbc.OracleTypes;

public class MainCheckTableThread implements Runnable { 

	ClientConfig config = null;
	public static LineNotify lineNotify = null;
	public static String backendGroupToken = "e4QuXuo08qZo6CK4wXLoO7djTuB9E9yEhIO2KusK6W9";

	ProductHelper productHelper = null;

	ObjectMapper mapper = new ObjectMapper();

	String name;
int conItemFromID=0;
int  conItemToID=0;
	Thread t;
	ConfigValidateBO g_conItem=null;
	
	public MainCheckTableThread(ClientConfig aconfig,ConfigValidateBO gconItem,String aname,int aconItemFromID ,int agconItemToID) {
		System.out.println(gconItem.dbTable+ ":" +aconItemFromID +"-"+agconItemToID);
		conItemFromID=aconItemFromID;
		 conItemToID=agconItemToID;
		name=aname;
		config = aconfig;
		g_conItem=gconItem;
		lineNotify = new LineNotify("LINE", backendGroupToken);
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper = new ObjectMapper();
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		t = new Thread(this,name);
		t.start();
	}

	public boolean ISSame(Map<String, SSObject> paramsdb, String gValidate, Map<String, SSObject> paramsgr) {
		if (paramsgr.size() == 0)
			return false;

		SSObject db = paramsdb.get(gValidate);
		SSObject gr = paramsgr.get(gValidate);
//		if (db == null || gr == null)
//			return false;
		String dbs = paramsdb.get(gValidate).Value;
		String grs = paramsgr.get(gValidate).Value;
		if (dbs == null && grs != null)
			return false;
		if (dbs != null && grs == null)
			return false;
		if (dbs == null || grs == null)
			return true;
		if (dbs.toString().equals(grs.toString()))
			return true;
		return false;
	}

	public void run() {
		 
		FileHelper.AppendAllText(name+".txt",  Utils.GetCurrentDate() + ":" + g_conItem.dbTable + ": "
				+ conItemFromID + "-" + conItemToID);
		

		ResultSet reader = null;

		String OracledbURL = config.DB_URL;
		String udb = config.DB_USER;
		String passdb = config.DB_PASS;
		String rabitUrl = config.SERVER_RABBITMQ_URL;
 
		OracleClient dbclient = null;
		String checkTable = g_conItem.gTable;
		Connection condb = null;
		//System.out.println("OracledbURL:" + OracledbURL);
		try {
			//dbclient = new OracleClient(OracledbURL, udb, passdb);
			dbclient = new OracleClient(config.DB_CONNECTIONSTRING, config.tns_admin, config.wallet_location,1);

			condb = dbclient.getConnection();
			int fromid = conItemFromID;
			int toid = 0;
			int tong = 0;
			int pagesize = 5000;
			for (int j = 0; j < 10000000; j++) {

				toid = fromid + pagesize;
				String sql = "";
				Statement csSQL = null;
				sql = "SELECT  " + g_conItem.gSelect + "  FROM  " + g_conItem.dbTable + " where " + g_conItem.gKey + ">"
						+ fromid + " AND " + g_conItem.gKey + " <=" + toid + " Order by " + g_conItem.gKey + " asc ";
				FileHelper.AppendAllText(name+".txt", sql);
				
				csSQL = condb.createStatement();
				csSQL.setFetchSize(pagesize);
				reader = csSQL.executeQuery(sql);

				int size = reader.getFetchSize();
				// Logs.WriteLine("GetSyncMsgFromDb:" + this.gTable + ":" + size +
				// ",tempLastID:" + tempLastID);
				int dem = 0;
				if (reader != null) {
					ResultSetMetaData rsmd = reader.getMetaData();
					int columnsCount = rsmd.getColumnCount();
					while (reader.next()) {
						Map<String, SSObject> params = new HashMap<String, SSObject>();
						int id = 0;
						long st = System.currentTimeMillis();
						int gCotValue = reader.getInt(g_conItem.gKey);

						if (checkTable.equals("stock")) {
//int brandid =  reader.getInt("brandid"); 
//int Site = DidxHelper.getSitebyBrandID( reader.getInt("brandid")); 
							// quantity,recordid,brandid
							int inventorystatusid = reader.getInt("inventorystatusid");
							int brandid = 0;// reader.getInt("brandid"); long quantity =0;//
							// reader.getInt("quantity");
							try {
								brandid = reader.getInt("brandid");
								// = reader.getLong("quantity");

							} catch (Exception exx) {
								// System.out.println(gCotValue); exx.printStackTrace(); throw exx;
							} finally {

							}

							// if(quantity<=0) continue; //may moi 1 và 3
							if (inventorystatusid > 0 && inventorystatusid != 1 && inventorystatusid != 3)
								continue;

							if (brandid > 0 && brandid != 1 && brandid != 2 && brandid != 6 && brandid != 11)
								continue;

							// if(brandid<=0) {
//							   System.out.println("");
//							   
//						   } 
//if(Site<=0 ) continue;

						}

						if (checkTable.equals("pm_product_productstatus")) {
							int companyid = reader.getInt("companyid");

							if (companyid > 0 && companyid != 1 && companyid != 2)
								continue;

						}
						for (int i = 1; i < columnsCount + 1; i++) {

							String cl = rsmd.getColumnName(i).toLowerCase();
							int sqlType = rsmd.getColumnType(i);
							SSObject data = new SSObject();
							data.cot = cl;
							data.sqlType = sqlType;

							// boolean isdeleted= reader.getBoolean("isdeleted") ;
							if (reader.getObject(i) != null) {
								switch (sqlType) {
								case Types.BIGINT:
								case Types.INTEGER:
								case Types.TINYINT:
								case Types.SMALLINT:
									data.Value = String.valueOf(reader.getInt(i));
									params.put(cl, data);

									break;
								case Types.DATE:

									if (reader.getDate(i) == null) {

									} else {
										data.Value = String.valueOf(Utils.FormatDateForGraph(reader.getDate(i)));
										params.put(cl, data);
									}
									break;
								case Types.TIMESTAMP:

									data.Value = String.valueOf(
											Utils.FormatDateForGraph(new Date(reader.getTimestamp(i).getTime())));
									params.put(cl, data);
									break;
								case Types.DOUBLE:

									data.Value = String.valueOf(reader.getDouble(i)).replace(".0", "");
									params.put(cl, data);
									break;
								case Types.FLOAT:
									// params.put(cl, reader.getFloat(i));
									data.Value = String.valueOf(reader.getFloat(i)).replace(".0", "");
									params.put(cl, data);
									break;
								case Types.NVARCHAR:

									data.Value = reader.getString(i).trim();
									params.put(cl, data);
									break;
								case Types.VARCHAR:
									data.Value = reader.getString(i).trim();
									params.put(cl, data);
									break;
								case Types.BLOB:

									data.Value = Utils.BlobToString(reader.getBlob(i)).trim();
									params.put(cl, data);
									break;
								case Types.CLOB:

									data.Value = Utils.ClobToString(reader.getClob(i)).trim();
									params.put(cl, data);
									break;
								case Types.NCLOB:

									data.Value = Utils.NClobToString(reader.getNClob(i)).trim();
									params.put(cl, data);
									break;
								default:

									data.Value = reader.getString(i).trim();
									params.put(cl, data);
									break;
								}
							} else {
								data.Value = null;
								params.put(cl, data);

							}

						}

						MessageQueue messageRepushV2 = new MessageQueue();

						messageRepushV2.Action = DataAction.Update;
						messageRepushV2.ClassName = "mwg.wb.pkg.validate.Validate";
						messageRepushV2.CreatedDate = Utils.GetCurrentDate();
						messageRepushV2.Lang = "vi-VN";
						messageRepushV2.SiteID = 1;
						messageRepushV2.Source = "CHECKTABLE";

						MessageValidate msgValidate = new MessageValidate();
						// MainCheckTable r = new MainCheckTable(config, "pm_currentinstock",
						// "pm_currentinstock", "recordid", "quantity",
						// "quantity,recordid");

						msgValidate.gKey = g_conItem.gKey;
						msgValidate.gTable = g_conItem.gTable;
						msgValidate.gValue = String.valueOf(gCotValue);

						msgValidate.cEdge = g_conItem.cEdge;
						msgValidate.gSelect = g_conItem.gSelect;
						msgValidate.cCot = g_conItem.cCot;
						msgValidate.cEdgeProp = g_conItem.cEdgeProp;

						msgValidate.dbparams = params;
						messageRepushV2.Identify = mapper.writeValueAsString(msgValidate);
						messageRepushV2.Hash = gCotValue;
						messageRepushV2.Note = "INJECT_VALIDATE";
						messageRepushV2.DataCenter = 0;

						int hsa = Utils.GetQueueNum5(gCotValue);

						String quV2 = "gr.dc4.validate" + hsa;
						String qu2V2 = "gr.dc4.validate" + hsa;
						String qubkV2 = "gr.dc2.validate" + hsa;
						String qudevV2 = "gr.beta.validate";

//						qu = "gr.dc4.sql.sysdata" + hsa;
//						qu2 = "gr.dc4.sql.sysdata" + hsa;
//						qubk = "gr.dc2.sql.sysdata" + hsa;
//						qudev = "gr.beta.sql.sysdata";

						try {
							tong++;
							QueueHelper.Current("amqp://tgdd:Tgdd2012@192.168.2.55:5672").PushAll(quV2, qu2V2, qubkV2,
									qudevV2, messageRepushV2, false, "", 0); // 0 bom 2 cái
							//System.out.println(gCotValue);

						} catch (Exception e) {
							e.printStackTrace();

						}

					}

					// LastId = tempLastID + "";

					if (reader != null) {
						reader.close();
					}
					if (csSQL != null) {
						csSQL.close();
					}

					dem = 0;
				}
				fromid = toid;
				// Utils.Sleep(6000 );
				// System.out.println("nghi chut");
				if (tong % 1000 == 0) {
					System.out.println("nghi chut");
					Utils.Sleep(3000);
				}
				FileHelper.AppendAllText(name+".txt", fromid + "");
				// System.out.println(id);
				if (fromid >= conItemToID) {

					String message = "CHECK DB DONE:" + Utils.GetCurrentDate() + ":" + g_conItem.dbTable + ": "
							+ conItemFromID + "-" + conItemToID;
					lineNotify.NotifyInfo(message, 0);
					System.out.println(message);
					// Utils.Sleep(100000000);
					return ;
				}
				System.out.println(name+  "-fromid " + fromid);
			}

		} catch (Throwable eee) {
			eee.printStackTrace();
		} finally {

			if (dbclient != null)
				try {
					dbclient.closeConnection();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (condb != null)
				try {
					condb.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}

	}

	 

}
