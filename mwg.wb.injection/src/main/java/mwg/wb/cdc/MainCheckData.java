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
import mwg.wb.common.EdgeInfo;
import mwg.wb.common.FileHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.GraphDBConstant;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.MessageQueuePushType;
import mwg.wb.common.PushType;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.SSObject;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.SqlInfoType;
import mwg.wb.common.TableEdgesInfo;
import mwg.wb.common.Utils;
import mwg.wb.common.notify.LineNotify;
 
import mwg.wb.model.api.ClientConfig;
import oracle.jdbc.OracleTypes;

public class MainCheckData {

	ClientConfig config = null;
	public static LineNotify lineNotify = null;
	public static String backendGroupToken = "e4QuXuo08qZo6CK4wXLoO7djTuB9E9yEhIO2KusK6W9";

	ProductHelper productHelper = null;

	public static void main(String[] args) {

		System.out.println("Let go...");
		try {

			DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);

			ClientConfig config = new ClientConfig();
			config.DB_PASS = "4 _ar3";
			config.DB_URL = "jdbc: oradc3";
			config.DB_USER = "webtgdd";

			 
			config.SERVER_ORIENTDB_READ_USER = "onlyread";
			config.SERVER_ORIENTDB_READ_PASS = "EnterR@graph!@#";
			config.SERVER_ORIENTDB_READ_URL1 = "remote:172.16.3.74,172.16.3.75,172.16.3.76/web";
			config.DATACENTER = 2;
			config.SERVER_ELASTICSEARCH_READ_HOST = "172.16.3.120";
			// config.SERVER_RABBITMQ_URL = "amqp://beta:beta@10.1.6.139:5672";
			config.ELASTICSEARCH_PRODUCT_INDEX = "ms_product";
			config.ELASTICSEARCH_PRODUCT_GALLERY_INDEX = "ms_gallery";
			config.ELASTICSEARCH_PRODUCT_OLD_INDEX = "ms_productold";
//			MainCheckData r = new MainCheckData(config, "product_gallery",
//					"product_gallery", "pictureid",
//					"picture","picture,pictureid");
MainCheckData r = new MainCheckData(config, "product_detail",
					"product_detail", "recordid",
					"value","value,recordid");
			
			
			r.Run();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				System.out.println("Main thread Interrupted");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	public MainCheckData(ClientConfig aconfig, String tableNamw, String aTableGraph, String aCotKey,
			String aupdateddate,String aselect) {
		gCotKey = aCotKey;
		gTableGraph = aTableGraph;
		gupdateddate = aupdateddate;
		gTable = tableNamw;
		config = aconfig;
		gselect = aselect;
		lineNotify = new LineNotify("LINE", backendGroupToken);
	}
	public String gselect;
	public String gTable;
	public String gTableGraph;
	public String gCotKey;
	public String gupdateddate;

	public boolean ISSame(Map<String, SSObject> paramsdb, Map<String, SSObject> paramsgr) {
		if (paramsgr.size() ==0)
			return false;
		 
		SSObject db = paramsdb.get(gupdateddate);
		SSObject gr = paramsgr.get(gupdateddate);
//		if (db == null || gr == null)
//			return false;
		String dbs = paramsdb.get(gupdateddate).Value;
		String grs = paramsgr.get(gupdateddate).Value;
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

	public void Run() throws Exception {

		ORThreadLocal oclient = new ORThreadLocal();
		oclient.initRead(config, 0, 1);
		productHelper = new ProductHelper(oclient, config);

		ResultSet reader = null;

		String OracledbURL = config.DB_URL;
		String udb = config.DB_USER;
		String passdb = config.DB_PASS;
		String rabitUrl = config.SERVER_RABBITMQ_URL;

//		String LastId = "0";
//		String LasIdFileName = gTable + ".txt";
//		if (Utils.FileExists(LasIdFileName)) {
//			LastId = Utils.ReadAllText(LasIdFileName);
//		}
//		if (LastId.isEmpty())
//			LastId = "0";

//		long tempLastID = Long.parseLong(LastId);
//		if (tempLastID <= 0) {
//			String message = "Injection:" + Utils.GetCurrentDate() + ":" + this.gTable + ":Lastid =0???";
//			lineNotify.Notify(message);
//
//			Logs.WriteLine("tempLastID<=0 " + this.gTable);
//			Utils.Sleep(100000000);
//		}
		OracleClient dbclient = null;

		Connection condb = null;

		try {
			//dbclient = new OracleClient(OracledbURL, udb, passdb);
			dbclient = new OracleClient(config.DB_CONNECTIONSTRING, config.tns_admin, config.wallet_location,1);

			condb = dbclient.getConnection();
			int fromid = 0;
			int toid = 0;

			for (int j = 0; j < 1000000; j++) {

				toid = fromid + 1000;
				String sql = "";
				Statement csSQL = null;
				sql = "SELECT  "+gselect+"  FROM " + gTable + " where " + gCotKey + ">" + fromid + " AND " + gCotKey + " <="
						+ toid + " Order by " + gCotKey + " asc ";

				csSQL = condb.createStatement();
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
						for (int i = 1; i < columnsCount + 1; i++) {

							String cl = rsmd.getColumnName(i).toLowerCase();
							int sqlType = rsmd.getColumnType(i);
							SSObject data = new SSObject();
							data.cot = cl;
							data.sqlType = sqlType;

							// boolean isactived= reader.getBoolean("isactived") ;
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
						if (params.get(gCotKey) != null && params.get(gCotKey).Value != null) {
							id = Integer.valueOf(params.get(gCotKey).Value.toString());
						}

						//select in('e_product_gallery')[0].productid as productid,picture from `product_gallery` order by pictureid desc limit 10
						
						Map<String, SSObject> paramsGrap = productHelper
								.GetResultMap("select "+gselect+",in('e_product_detail')[0].productid as productid from " + gTableGraph + " where " + gCotKey + "=" + id, params,"productid");
						 
						boolean ss = ISSame(params, paramsGrap);
						if (ss == false) {
							try {

								String fnPath = gTableGraph + "-notsame.txt";
								FileHelper.AppendAllText(fnPath, id + "");
								System.out.println(id);
							} catch (Throwable e) {
								e.printStackTrace();
								// TODO: handle exception
							}

						} else {
							
							 if(paramsGrap.get("productid")!=null && paramsGrap.get("productid").Value!=null) {
								 long pid=Utils.toLong(paramsGrap.get("productid").Value.toString());
								 if(pid<=0) {
									 try {

											String fnPath = gTableGraph + "-notsame.txt";
											FileHelper.AppendAllText(fnPath, id + "");
											System.out.println(id);
										} catch (Throwable e) {
											e.printStackTrace();
											// TODO: handle exception
										}
								 }
							 }

							 
							try {

								//String fnPath = gTableGraph + "-same.txt";
								//FileHelper.AppendAllText(fnPath, id + "");
								// System.out.println(id);
							} catch (Throwable e) {
								e.printStackTrace();
								// TODO: handle exception
							}
						}

						// fromid = id;
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
				 
				 FileHelper.AppendAllText("fromid", fromid + "");
				// System.out.println(id);
				if (fromid > 3200043) {

					String message = "CHECK DB DONE:" + Utils.GetCurrentDate() + ":" + this.gTable + " ";
					lineNotify.NotifyInfo(message,0);

					Utils.Sleep(100000000);
				}
				System.out.println("fromid " + fromid);
			}

		} catch (Throwable eee) {
			eee.printStackTrace();
		} finally {

			if (dbclient != null)
				dbclient.closeConnection();
			if (condb != null)
				condb.close();

		}

	}

}
