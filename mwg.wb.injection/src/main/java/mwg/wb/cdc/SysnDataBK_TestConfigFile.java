package mwg.wb.cdc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
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

import mwg.wb.business.WorkerHelper;
import mwg.wb.client.OracleClient;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.queue.QueueRabbitMQ;
import mwg.wb.common.ColType;
import mwg.wb.common.EdgeInfo;
import mwg.wb.common.FileHelper;
import mwg.wb.common.GraphDBConstant;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.MessageQueuePushType;
import mwg.wb.common.PushType;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.SqlInfoType;
import mwg.wb.common.TableEdgesInfo;
import mwg.wb.common.Utils;
import mwg.wb.common.notify.LineNotify;
 
import mwg.wb.model.api.ClientConfig;
import oracle.jdbc.OracleTypes;

public class SysnDataBK_TestConfigFile {

	// private static OrientClient _instance = null;

//	public OrientClient() {
//		this("172.16.3.71", 2424, "web", "root", "Tgdd2019#@!");
//	} 
	// ProductHelper productHelper = null;
//	public static ORThreadLocal factoryWrite = null;
//	public static ORThreadLocal factoryRead = null;
	// public static boolean pushStatus = true;
	ClientConfig config = null;
	public static LineNotify lineNotify = null;
	public static String backendGroupToken = "e4QuXuo08qZo6CK4wXLoO7djTuB9E9yEhIO2KusK6W9";
	public static String[] queueBigData = { "gr.dc2.sql.productold", "gr.dc2.sql.productoldpromotion",
			"gr.dc2.sql.promotion", "gr.dc2.sql.stock0", "gr.dc2.sql.stock1", "gr.dc2.sql.stock2", "gr.dc2.sql.stock3",
			"gr.dc2.sql.stock4", "gr.dc2.sql.stockbhx", "gr.dc2.sql.sim", "gr.dc2.sql.stock0", "gr.dc2.sql.stock1",
			"gr.dc2.sql.stock2", "gr.dc2.sql.stock3", "gr.dc2.sql0", "gr.dc2.sql1", "gr.dc2.sql2", "gr.dc2.sql3",
			"gr.dc2.sql4", "gr.dc2.sql5", "gr.dc2.sql6", "gr.dc2.sql7", "gr.dc2.sql8", "gr.dc2.sql9" };

	public SysnDataBK_TestConfigFile(String injectProgramName, String syncMessageStore) {
		SyncMessageStore = syncMessageStore;
		InjectProgramName = injectProgramName;
		// factoryWrite = afactoryWrite;
		// factoryRead = afactoryRead;
		// pushStatus = apushStatus;
		// productHelper = new ProductHelper(factoryRead);
		config = WorkerHelper.GetWorkerClientConfig();
		lineNotify = new LineNotify("LINE", backendGroupToken);

	}

	public String SyncMessageStore;

	public String InjectProgramName;

// 	public ResultSet GetSyncMsgFromDb(Connection condb, long lastid, int rowcount) {
//		Logs.WriteLine("GetSyncMsgFromDb:" + lastid + "-" + rowcount);
//		CallableStatement cs = null;
//		try {
//			cs = condb.prepareCall("{call " + this.SyncMessageStore + "(?,?,?)}");
//			cs.registerOutParameter(1, OracleTypes.CURSOR);
//			cs.setLong(2, lastid);
//			cs.setInt(3, rowcount);
//			cs.execute();
//			// ResultSet rs=cs.executeQuery() ;
//			// ResultSet rs= cs.getResultSet();
//			ResultSet rs = (ResultSet) cs.getObject(1);
//			return rs;
//
//		} catch (SQLException e) {
//			Logs.WriteLine("SQLException: " + e.getMessage());
//		} finally {
//			if (cs != null) {
//				try {
//					cs.close();
//				} catch (SQLException e) {
//					Logs.WriteLine("SQLException: " + e.getMessage());
//				}
//			}
//
//		}
//		Logs.WriteLine("null?");
//		return null;
//
//	}

	public static Map<String, Integer> g_listQuantity = new HashMap<String, Integer>();
	public static Map<String, Long> Hasrocessed = new HashMap<String, Long>();

	public static List<SqlInfo> BuildSqlList(MessageQueue messageRepush, String Identify, String table,
			String recordName, String recordValue, String otherColVao, DataAction action, Connection cnndb,
			String strNote, boolean isLog, MessageQueue ref) throws SQLException {
		ResultMessage resultMessage = new ResultMessage();
		// DGRAPH_SYNC_MESSAGE_SEL (ID,TABLENAME,ACTION,IDENTIFY,CREATEDDATE)
		// IDENTIFY TABLENAME|productid|1212|cotkhac,cot khac
		// string connectstring =
		// ms.common.ConfigHelper.Current._ORIENTDB_CONNECTIONSTRING_;
		// Orientclient orientclient = new Orientclient(connectstring);
		// orientclient.Connect();
		List<SqlInfo> SqlList = new ArrayList<SqlInfo>();

		if (table.equals("gen_ward")) {
			return SqlList;
		}

		try {

			resultMessage.Code = ResultCode.Success;
			if (recordValue.isEmpty())
				return SqlList;
			if (action == DataAction.Delete) {
				// crm.crm_productlocking
				// masterdata.gen_store_ward_distance
				// masterdata.pm_store_pm_maingroup

				if (table.equals("pm_specialsaleprogram_invstt") || table.equals("pm_specialsaleprogram_product")
						|| table.equals("pm_specialsaleprogram_subgroup")
						|| table.equals("pm_specialsaleprogram_brand")) {
					String sqlDbDelete = "UPDATE  " + table + " set isdeleted=1 where  " + recordName + "="
							+ recordValue;
					SqlInfo sqlinfo = new SqlInfo();
					Map<String, Object> params = new HashMap<String, Object>();
					sqlinfo.Sql = sqlDbDelete;
					SqlList.add(sqlinfo);
					return SqlList;
				}

				if (table.equals("crm_productlocking")) {
					String sqlDbDelete = "UPDATE  crm_productlocking set isdeleted=1 where  " + recordName + "="
							+ recordValue;
					SqlInfo sqlinfo = new SqlInfo();
					Map<String, Object> params = new HashMap<String, Object>();
					sqlinfo.Sql = sqlDbDelete;
					SqlList.add(sqlinfo);
					return SqlList;
				}
				if (table.equals("gen_store_ward_distance")) {
					String sqlDbDelete = "UPDATE  gen_store_ward_distance set isdeleted=1 where  " + recordName + "="
							+ recordValue;
					SqlInfo sqlinfo = new SqlInfo();
					Map<String, Object> params = new HashMap<String, Object>();
					sqlinfo.Sql = sqlDbDelete;
					SqlList.add(sqlinfo);
					return SqlList;
				}
				if (table.equals("pm_store_pm_maingroup")) {
					String sqlDbDelete = "UPDATE  pm_store_pm_maingroup set isdeleted=1 where  " + recordName + "="
							+ recordValue;
					SqlInfo sqlinfo = new SqlInfo();
					Map<String, Object> params = new HashMap<String, Object>();
					sqlinfo.Sql = sqlDbDelete;
					SqlList.add(sqlinfo);
					return SqlList;
				}
				if (table.equals("pm_currentinstock")) {
					String sqlDbDelete = "UPDATE  pm_currentinstock set isdeleted=1 where  " + recordName + "="
							+ recordValue;
					SqlInfo sqlinfo = new SqlInfo();
					Map<String, Object> params = new HashMap<String, Object>();
					sqlinfo.Sql = sqlDbDelete;
					SqlList.add(sqlinfo);
					return SqlList;
				}
				if (table.equals("pm_currentinstock_bhx")) {
					String sqlDbDelete = "UPDATE  pm_currentinstock_bhx set isdeleted=1 where  " + recordName + "="
							+ recordValue;
					SqlInfo sqlinfo = new SqlInfo();
					Map<String, Object> params = new HashMap<String, Object>();
					sqlinfo.Sql = sqlDbDelete;
					SqlList.add(sqlinfo);
					return SqlList;
				}
			}
			if (action == DataAction.Add || action == DataAction.Update) {
				String tbl = table;

				// Todo: chờ duyệt, đã xóa list khai báo thay bằng 3 dòng này
				TableEdgesInfo tableInfo = GraphDBConstant.TABLE_EDGES_INFO.get(tbl);

				if (tableInfo != null && Utils.StringIsEmpty(tableInfo.dbschema) == false) {

					tbl = tableInfo.dbschema + "." + tableInfo.dbtable;
				}
				
				
				// Upsert
				// product_language|recordid|659311|simage,mimage,bimage,updateddate,updateduser,
				boolean isadd = true;
				String sqlDb = "SELECT *   FROM  " + tbl + " where  " + recordName + "='" + recordValue + "'";
				String otherCol = otherColVao;

				if (!otherCol.isEmpty()) {
					if (!otherCol.contains(recordName)) {
						otherCol = otherCol + "," + recordName;
					}
					sqlDb = "SELECT \"" + otherCol.toUpperCase().replace(",", "\",\"") + "\"   FROM  " + tbl
							+ " where  " + recordName + "='" + recordValue + "'";
					isadd = false;
				}
				if (table.equals("product")) {
					// Logs.WriteLine(sqlDb);
				}

				String collectionname = table;

				String productCode = "";
				Statement cs = cnndb.createStatement();
				ResultSet reader = cs.executeQuery(sqlDb);
				try {
					// ResultSet reader = objDataAccess.executeQuery(sql);

					ResultSetMetaData rsmd = reader.getMetaData();
					int columnsCount = rsmd.getColumnCount();
					while (reader.next()) {

						long st = System.currentTimeMillis();

						// if (table == "pm_currentinstock")
						// {

						// String json = "update " + collectionname + " SET ";
						// build sql

						String sql = "update " + collectionname + " SET ";
						for (int i = 1; i < columnsCount + 1; i++) {

							String cl = rsmd.getColumnName(i).toLowerCase();

							sql = sql + "`" + cl + "`=:" + cl + ",";

						}
						sql = StringUtils.strip(sql, ",");

						Map<String, Object> params = new HashMap<String, Object>();
						for (int i = 1; i < columnsCount + 1; i++) {

							String cl = rsmd.getColumnName(i).toLowerCase();
							int sqlType = rsmd.getColumnType(i);
							if (table.equals("pm_product")) {
								if (cl.equals("productid")) {
									productCode = reader.getString(i).trim();
									ref.Identify = productCode;

								}
							}

							if (table.equals("pm_currentinstock")) {
								if (cl.equals("productid")) {
									productCode = reader.getString(i).trim();
									ref.Identify = productCode;
									ref.PushType = PushType.Currentinstock;
								}
								if (cl.equals("brandid")) {
									int brandid = reader.getInt(i);
									ref.BrandID = brandid;
									ref.PushType = PushType.Currentinstock;
								}
								if (cl.equals("provinceid")) {
									int provinceid = reader.getInt(i);
									ref.ProvinceID = provinceid;
									ref.PushType = PushType.Currentinstock;
								}
							}
							if (table.equals("pm_currentinstock_bhx")) {
								if (cl.equals("productid")) {
									productCode = reader.getString(i).trim();
									ref.Identify = productCode;
									ref.PushType = PushType.CurrentinstockBHX;
								}
								if (cl.equals("storeid")) {
									int storeid = reader.getInt(i);
									ref.Storeid = storeid;
									ref.PushType = PushType.CurrentinstockBHX;
								}
								if (cl.equals("provinceid")) {
									int provinceid = reader.getInt(i);
									ref.ProvinceID = provinceid;
									ref.PushType = PushType.CurrentinstockBHX;
								}
								ref.SiteID = 11;
								ref.Lang = "vi-VN";
							}

							if (cl.equals("pictureid") && table.equals("product_gallery")) {
								long pictureid = reader.getLong(i);
								ref.Identify = String.valueOf(pictureid);
								ref.PushType = PushType.Product_gallery;
							}
							if (cl.equals("oldid") && table.equals("product_old_detail")) {
								long oldid = reader.getLong(i);
								ref.Identify = String.valueOf(oldid);
								ref.PushType = PushType.Product_old_detail;
							}
							if (table.equals("product")) {
								// if (otherColVao.equals("totalreview")) {
								if (cl.equals("totalreview")) {
									int viewcounter = reader.getInt(i);
									ref.Data = String.valueOf(viewcounter);
									// Logs.WriteLine("viewcounter: " + ref.Data);
									ref.PushType = PushType.ProductViewcounter;
								}
								// } else {
								if (cl.equals("productid")) {
									long productid = reader.getLong(i);
									ref.Identify = String.valueOf(productid);

								}
								if (cl.equals("categoryid")) {
									int categoryid = reader.getInt(i);
									ref.CategoryID = categoryid;

								}

								// }
							}
							if (table.equals("news")) {
								// if (otherColVao.equals("viewcounter")) {
								if (cl.equals("viewcounter")) {
									int viewcounter = reader.getInt(i);
									ref.Data = String.valueOf(viewcounter);
									// Logs.WriteLine("viewcounter: " + ref.Data);
									ref.PushType = PushType.NewsViewcounter;
								}
								// } else {

								if (cl.equals("newsid")) {
									long newsid = reader.getLong(i);
									ref.Identify = String.valueOf(newsid);
									ref.PushType = PushType.NewsDetail;
								}
								if (cl.equals("siteid")) {
									int SiteID = reader.getInt(i);
									ref.SiteID = SiteID;
								}

								if (cl.equals("categoryid")) {

									long categoryid = reader.getLong(i);
									// n [1226,1222]
									if (categoryid == 1226 || categoryid == 1222) {
										ref.PushType = PushType.Faq;
									}
								}
								// }
							}

							if (table.equals("cook_dish")) {

								if (cl.equals("viewcount")) {
									int viewcounter = reader.getInt(i);
									ref.Data = String.valueOf(viewcounter);
									// Logs.WriteLine("viewcounter: " + ref.Data);
									ref.PushType = PushType.CookViewcounter;
								}

								if (cl.equals("dishid")) {
									long dishID = reader.getLong(i);
									ref.Identify = String.valueOf(dishID);
									ref.PushType = PushType.DishDetail;
								}
								if (cl.equals("siteid")) {
									int SiteID = reader.getInt(i);
									ref.SiteID = SiteID;
								}

							}

							if (cl.equals("eventid") && table.equals("news_event")) {
								long eventid = reader.getLong(i);
								ref.Identify = String.valueOf(eventid);
								ref.PushType = PushType.News_event;
							}

							if (cl.equals("hottopicid") && table.equals("news_hottopic")) {
								long topicid = reader.getLong(i);
								ref.Identify = String.valueOf(topicid);
								ref.PushType = PushType.News_hottopic;
							}
							if (table.equals("product_language")) {
								if (cl.equals("productid")) {
									long productid = reader.getLong(i);
									ref.Identify = String.valueOf(productid);
									ref.PushType = PushType.Product_language;
								}
								if (cl.equals("siteid")) {

									ref.SiteID = reader.getInt(i);
								}
								if (cl.equals("languageid")) {

									ref.Lang = reader.getString(i);
								}
							}
							if (reader.getObject(i) != null) {
								switch (sqlType) {
								case Types.BIGINT:
								case Types.INTEGER:
								case Types.TINYINT:
								case Types.SMALLINT:

									params.put(cl, reader.getInt(i));

									break;
								case Types.DATE:

									params.put(cl, Utils.FormatDateForGraph(reader.getDate(i)));

									break;
								case Types.TIMESTAMP:
									params.put(cl,
											Utils.FormatDateForGraph(new Date(reader.getTimestamp(i).getTime())));

									break;
								case Types.DOUBLE:
									params.put(cl, reader.getDouble(i));

									break;
								case Types.FLOAT:
									params.put(cl, reader.getFloat(i));

									break;
								case Types.NVARCHAR:
									params.put(cl, reader.getString(i).trim());
									break;
								case Types.VARCHAR:
									params.put(cl, reader.getString(i).trim());

									break;
								case Types.BLOB:
									params.put(cl, Utils.BlobToString(reader.getBlob(i)).trim());

									break;
								case Types.CLOB:
									params.put(cl, Utils.ClobToString(reader.getClob(i)).trim());

									break;
								case Types.NCLOB:
									params.put(cl, Utils.NClobToString(reader.getNClob(i)).trim());
									break;
								default:
									params.put(cl, reader.getString(i).trim());

									break;
								}
							} else {

								params.put(cl, null);
							}

						}
						int rsOr = 0;

						if (table.equals("pm_product")) {
							sql = sql + " Upsert where recordid=" + Utils.toInt(reader.getString("recordid"));
						} else {
							sql = sql + " Upsert where " + recordName + "=" + recordValue;
						}

						Logs.Log(isLog, strNote, sql);
						if (isLog) {
							Logs.getInstance().Log(isLog, strNote, "stock", params);
						}
						SqlInfo sqlinfoUpdate = new SqlInfo();
						sqlinfoUpdate.Sql = sql;
						sqlinfoUpdate.Params = params;
						SqlList.add(sqlinfoUpdate);

						if (action == DataAction.Add) {
							Logs.Log(isLog, strNote, "Create edge");
							try {
								
								// Todo: chờ duyệt, đã xóa hết code type = CREATE_EDGE_BY_NODE_GENERAL & CREATE_EDGE_BY_NODE
								// todo: thay bằng CREATE_EDGE_BY_EDGE_INFO
								SqlInfo sqlinfoEgde2  = new SqlInfo();
								
								if (tableInfo != null && tableInfo.edges != null) {
									
									List<EdgeInfo> edges = tableInfo.edges;
									
									for (EdgeInfo e : edges) {
						
										String fromid = "";
										String toid = "";
										
										/**
										 * Chỉ dùng cột dbfromcol hoặc dbtocol để lấy value của reader
										 * Nếu 1 trong 2 tên cột rỗng thì dùng recordValue
										 */						
										if (Utils.StringIsEmpty(e.dbfromcol) == false) {
											fromid = ReformatIdString(reader.getString(e.dbfromcol), e.fromcoltype);
										}
										else
											fromid = recordValue;
						
										if (Utils.StringIsEmpty(e.dbtocol) == false) {
											toid = ReformatIdString(reader.getString(e.dbtocol), e.tocoltype);
										}
										else
											toid = recordValue;

										/**
										 * Id là số luôn >= 0
										 * Nên chỉ check null or empty
										 */
										if (Utils.StringIsEmpty(fromid) == false
												&& Utils.StringIsEmpty(toid) == false) {
											
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_EDGE_INFO;
											sqlinfoEgde2.Params.put("edge", e.edge);
											sqlinfoEgde2.Params.put("from", fromid);
											sqlinfoEgde2.Params.put("to", toid);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}
						
									}
								}

								if (table.equals("product_category")) {
									// noneed
								}
								if (table.equals("product_manu")) {
									// noneed
								}
								if (table.equals("product_category_lang")) {
									// noneed
									int cateid = Utils.toInt(reader.getString("categoryid"));

									int siteid = Utils.toInt(reader.getString("siteid"));
									String languageid = reader.getString("languageid");
									if (siteid > 0 && !Utils.StringIsEmpty(languageid)) {
										SqlInfo sqlinfoEgde1 = new SqlInfo();
										sqlinfoEgde1.Params = new HashMap<String, Object>();
										sqlinfoEgde1.Type = SqlInfoType.REQUIRE_RUN_SEOURL_CATE;
										sqlinfoEgde1.Params.put("siteid", siteid);
										sqlinfoEgde1.Params.put("languageid", languageid);
										sqlinfoEgde1.Params.put("objectid", cateid);
										SqlList.add(sqlinfoEgde1);
										messageRepush.IsCreateEdge = true;

									}
								}
								if (table.equals("product_manu_lang")) {

									int manufacturerid = Utils.toInt(reader.getString("manufacturerid"));
									int siteid = Utils.toInt(reader.getString("siteid"));
									String languageid = reader.getString("languageid");
									if (siteid > 0 && !Utils.StringIsEmpty(languageid)) {
										SqlInfo sqlinfoEgde1 = new SqlInfo();
										sqlinfoEgde1.Params = new HashMap<String, Object>();
										sqlinfoEgde1.Type = SqlInfoType.REQUIRE_RUN_SEOURL_MANU;
										sqlinfoEgde1.Params.put("siteid", siteid);
										sqlinfoEgde1.Params.put("languageid", languageid);
										sqlinfoEgde1.Params.put("objectid", manufacturerid);
										SqlList.add(sqlinfoEgde1);
										messageRepush.IsCreateEdge = true;
									}

								}
								
								if (table.equals("product_prop_lang")) {
									String propertyid = reader.getString("propertyid");
									SqlInfo sqlinfoEgde1 = new SqlInfo();
									sqlinfoEgde1.Sql = "create edge e_product_prop_lang  from(select from product_prop  where propertyid= "
											+ propertyid + ") to (select from product_prop_lang    where recordid= "
											+ recordValue + " and in('e_product_prop_lang')[propertyid = " + propertyid
											+ "].size() = 0)";

									SqlList.add(sqlinfoEgde1);
									messageRepush.IsCreateEdge = true;
								}
								if (table.equals("product_propgrp_lang")) {
									String groupid = reader.getString("groupid");

									SqlInfo sqlinfoEgde1 = new SqlInfo();
									sqlinfoEgde1.Sql = "create edge e_product_propgrp_lang  from(select from product_propgrp   where groupid= "
											+ groupid + ") to (select from product_propgrp_lang    where recordid= "
											+ recordValue + " and in('e_product_propgrp_lang')[groupid = " + groupid
											+ "].size() = 0)";

									SqlList.add(sqlinfoEgde1);
									messageRepush.IsCreateEdge = true;
								}

								if (table.equals("product_color")) {

								}

							} catch (Exception exx) {
								exx.printStackTrace();
								Logs.Log(isLog, strNote, exx.getMessage());
								Logs.WriteLine(exx.getMessage());
								FileHelper.AppendAllText("sysmessage.txt", exx.toString());
								throw exx;
							}
							// orientclient2.Close();
						}
						// long diff = System.currentTimeMillis() - st;
						// Logs.WriteLine(recordName + ": " + recordValue + " indexed:" + diff);
						// Logs.Log(isLog, strNote, recordName + ": " + recordValue + " indexed:" +
						// diff);
						resultMessage.Code = ResultCode.Success;
					} // while
				} finally {
					cs.close();
					reader.close();
					reader = null;
					// database.Close();

				}

			} else if (action == DataAction.Delete) {

				resultMessage.Code = ResultCode.Success;
			}
		} finally {
			// database.Close();
		}

		return SqlList;
	}

	public void Run() throws Exception {
		ResultSet rs = null;

		 
		String OracledbURL = config.DB_URL;
		String udb = config.DB_USER;
		String passdb = config.DB_PASS;
		String rabitUrl = config.SERVER_RABBITMQ_URL;

		 
		String LastId = "0";
		String LasIdFileName = this.InjectProgramName + ".txt";
		if (Utils.FileExists(LasIdFileName)) {
			LastId = Utils.ReadAllText(LasIdFileName);
		}
		if (LastId.isEmpty())
			LastId = "0";

		long tempLastID = Long.parseLong(LastId);
		if (tempLastID <= 0) {
			String message = "Injection:" + Utils.GetCurrentDate() + ":" + this.InjectProgramName + ":Lastid =0???";
			lineNotify.NotifyInfo(message);

			Logs.WriteLine("tempLastID<=0 " + this.InjectProgramName);
			Utils.Sleep(100000000);
		}
		OracleClient dbclient = null;
		QueueRabbitMQ queueConnect = null;
		Connection condb = null;
		int ErrorCount = 0;
		try {
			//dbclient = new OracleClient(OracledbURL, udb, passdb);
			dbclient = new OracleClient(config.DB_CONNECTIONSTRING, config.tns_admin, config.wallet_location,1);
			queueConnect = new QueueRabbitMQ();
			queueConnect.Init(rabitUrl);
			queueConnect.Connect();
			condb = dbclient.getConnection();
			// factoryWrite.InitNoTx();
			for (int j = 0; j < 1000000; j++) {
				Logs.WriteLine("GetSyncMsgFromDb ");
				//long ToID = tempLastID + 1000;
				//new MyThreadSysData("data", "DGRAPH_SYNC_MESSAGE_SEL");
//				new MyThreadSysData("dataprice", "DGRAPH_SYNC_MESSAGE_PRICE_SEL");
//				new MyThreadSysData("datastock", "DGRAPH_SYNC_MESSAGE_SELSTOCK");
//				new MyThreadSysData("datastockbhx", "DGRAPH_SYNC_SELSTOCKBHX");
//				new MyThreadSysData("data-reinit", "DGRAPH_SYNC_REPUSH_SEL");
				String sql = "";
				Statement csSQL =null;
				CallableStatement csStore =null;
				if (InjectProgramName.equals("data")) {
					sql="SELECT\r\n" + 
							"  *\r\n" + 
							"FROM\r\n" + 
							"(\r\n" + 
							"  SELECT\r\n" + 
							"    ROWNUM STT,\r\n" + 
							"    datacenter ,ID,TABLENAME,ACTION,IDENTIFY,CREATEDDATE,CHANGEDVERSION,ClassName,SiteID,NOTE, \r\n" + 
							"    LANGUAGEID\r\n" + 
							"  FROM\r\n" + 
							"  (\r\n" + 
							"    SELECT \r\n" + 
							"      0 as datacenter ,ID,TABLENAME,ACTION,IDENTIFY,CREATEDDATE,CHANGEDVERSION,ClassName,SiteID,NOTE, \r\n" + 
							"      LANGUAGEID \r\n" + 
							"    FROM  DGRAPH_SYNC_MESSAGE where ID > "+tempLastID+"\r\n" + 
							"    Order by ID ASC\r\n" + 
							"  )\r\n" + 
							")\r\n" + 
							"WHERE STT < 1001 ORDER BY id";
					//sql = "SELECT 0 as datacenter ,ID,TABLENAME,ACTION,IDENTIFY,CREATEDDATE,CHANGEDVERSION,ClassName,SiteID,NOTE, LANGUAGEID FROM  DGRAPH_SYNC_MESSAGE where ID>"
					//		+ tempLastID + " AND  ROWNUM <=500   Order by ID asc ";

					csSQL = condb.createStatement();
					rs = csSQL.executeQuery(sql);

				} else {

					csStore = condb.prepareCall("{call " + this.SyncMessageStore + "(?,?,?)}");
					csStore.registerOutParameter(1, OracleTypes.CURSOR);
					csStore.setLong(2, tempLastID);
					csStore.setInt(3, 1000 );
					csStore.execute();
					rs = (ResultSet) csStore.getObject(1);
				}
//				if (InjectProgramName.equals("data-reinit")) {
//					sql = "SELECT ID,TABLENAME,ACTION,IDENTIFY,CREATEDDATE,CHANGEDVERSION,ClassName,SiteID,NOTE, LANGUAGEID,DATACENTER FROM  DGRAPH_SYNC_MESSAGE_REPUSH where ID>"
//							+ tempLastID + " AND ID<=" + ToID + " Order by ID asc ";
//
//				}
//				if (InjectProgramName.equals("data-reinit")) {
//					sql = "SELECT ID,TABLENAME,ACTION,IDENTIFY,CREATEDDATE,CHANGEDVERSION,ClassName,SiteID,NOTE, LANGUAGEID,DATACENTER FROM  DGRAPH_SYNC_MESSAGE_REPUSH where ID>"
//							+ tempLastID + " AND ID<=" + ToID + " Order by ID asc ";
//
//				}

				int size = rs.getFetchSize();
				Logs.WriteLine("GetSyncMsgFromDb:" + this.InjectProgramName + ":" + size + ",tempLastID:" + tempLastID);
				int dem = 0;
				if (rs != null) {

					while (rs.next()) {
						dem++;
						size++;
						// Logs.WriteLine("\r\npush" + size );
						String strIdentify = rs.getString("IDENTIFY");
						String strACTION = rs.getString("ACTION");
							String TABLENAME = rs.getString("TABLENAME");
							
						String strNOTE = rs.getString("NOTE") + "";

						if (strACTION.equals("insert"))
							strACTION = "Add";
						if (strACTION.equals("update"))
							strACTION = "Update";
						if (strACTION.equals("delete"))
							strACTION = "Delete";

						boolean isLog = false;
						if (strNOTE.contains("DIDX_LOG")) {

							isLog = true;
						}

						DataAction action = DataAction.Add;
						try {
							action = Enum.valueOf(DataAction.class, strACTION);
						} catch (Exception e1) {
							Logs.LogException(e1);
							Logs.LogException("DataAction" + strACTION + " strIdentify=" + strIdentify);
						}

						String classname = StringUtils.replace(rs.getString("classname"), "_", ".");
						long ID = rs.getLong("ID");
						long CHANGEDVERSION = rs.getLong("CHANGEDVERSION");
						int siteID = rs.getInt("siteID");
						int datacenter = 0;
						// if (InjectProgramName.equals("data-reinit")) {
						datacenter = rs.getInt("datacenter");
						// }
//						if(TABLENAME.equals("pm_currentinstock") || TABLENAME.equals("pm_currentinstock_bhx"))
//						 {
//							if (ID > tempLastID) {
//								tempLastID = ID;
//
//							}
//							continue;
//						 }
						
						Date date = new Date(rs.getTimestamp("CREATEDDATE").getTime());
						String LangID = rs.getString("LanguageID");
						if (Utils.StringIsEmpty(LangID))
							LangID = "vi-VN";
						boolean logit = false;
						String logfile = "log";
						Logs.Log(isLog, strNOTE, "inject " + strIdentify + " ID:" + ID);
						if(  TABLENAME.equals("product") 
							||	TABLENAME.equals("product_language") 
								)
							 {
							if(!strIdentify.endsWith("totalreview,")) {
							Logs.LogSysMessage("injectlog", Utils.GetCurrentDate() + ":" + strIdentify + " ID:" + ID +":"+strACTION);
							}
							 }
						
						
						
						if (classname.equals("ms.price.Status") || classname.equals("ms.price.Price")
								|| classname.equals("ms.promotion.Promotion")
								// || classname.equals("ms.productse.ProductSE")
								|| classname.equals("ms.currentinstock.Currentinstock")
								// || classname.equals("ms.gameapp.GameAppSE")
								|| classname.equals("ms.sim.Sim") || classname.equals("ms.productold.Promotion")
								|| classname.equals("ms.price.StockStore") || classname.equals("ms.price.CodeStock")

						) {
							// if (pushStatus) {
							Logs.WriteLine("\r\npush" + classname + " - LangID: " + LangID);
							MessageQueue message = new MessageQueue();

							message.Action = action;
							message.Note = strNOTE + "|" + this.InjectProgramName.toUpperCase();
							message.Identify = strIdentify;
							message.ClassName = classname;
							message.CreatedDate = date;
							message.ID = ID;
							message.SiteID = siteID;
							message.Lang = LangID;
							message.Version = CHANGEDVERSION;
							message.DataCenter = datacenter;
							String queue = "";
							String queuebk = "";
							String queueBeta = "";
							boolean isSend = true;
							if (classname.equals("ms.price.StockStore")) {
								// ProductID|provinceID

								String[] messIdentifyArr = strIdentify.split("\\|");

								if (messIdentifyArr.length == 2) {
									long productID = Utils.toLong(messIdentifyArr[0]);
									int provinceID = Utils.toInt(messIdentifyArr[1]);
									message.Identify = String.valueOf(productID);
									message.ProvinceID = provinceID;
									queue = "gr.dc4.didx.stockstore";
									queuebk = "gr.dc2.didx.stockstore";
									queueBeta = "gr.beta.didx.stockstore";
									isSend = true;

								} else {
									isSend = false;

								}
								Logs.Log(isLog, "DIDX_LOG|stockstore",
										strIdentify + "," + messIdentifyArr.length + " isSend=" + isSend);

							}
							if (classname.equals("ms.price.CodeStock")) {
								// ms_price_CodeStock productCode|provinceID|brandid

								String[] messIdentifyArr = strIdentify.split("\\|");

								if (messIdentifyArr.length == 3) {
									String productCode = messIdentifyArr[0];
									int provinceID = Utils.toInt(messIdentifyArr[1]);
									int brandid = Utils.toInt(messIdentifyArr[2]);
									message.Identify = productCode;
									message.ProvinceID = provinceID;
									message.BrandID = brandid;

									queue = "gr.dc4.didx.codestock";
									queuebk = "gr.dc2.didx.codestock";
									queueBeta = "gr.beta.didx.codestock";
									isSend = true;

								} else {
									isSend = false;

								}
								Logs.Log(isLog, "DIDX_LOG|codestock",
										strIdentify + "," + messIdentifyArr.length + " isSend=" + isSend);

							}

							if (classname.equals("ms.price.Status")) {
								// ms_price_Status ProductID|provinceID

								String[] messIdentifyArr = strIdentify.split("\\|");

								if (messIdentifyArr.length == 2) {
									long productID = Utils.toLong(messIdentifyArr[0]);
									int provinceID = Utils.toInt(messIdentifyArr[1]);
									message.Identify = String.valueOf(productID);
									message.ProvinceID = provinceID;

									int h = (int) (productID % 3);

									queue = "gr.dc4.didx.status" + h;
									queueBeta = "gr.beta.didx.status" + h;
									queuebk = "gr.dc2.didx.status" + h;

									message.Action = DataAction.Update;
									message.ClassName = "mwg.wb.pkg.price.Status";

									isSend = true;

								} else {
									isSend = false;

								}
								Logs.Log(isLog, "DIDX_LOG|status",
										strIdentify + "," + messIdentifyArr.length + " isSend=" + isSend);

							}

							if (classname.equals("ms.price.Price")) {
								logit = true;
								logfile = "price";
								queue = "gr.dc4.didx.price";
								queuebk = "gr.dc2.didx.price";
								queueBeta = "gr.beta.didx.price";
								isSend = true;
							}
							if (classname.equals("ms.sim.Sim")) {

								queue = "gr.dc4.didx.sim";
								queuebk = "gr.dc2.didx.sim";
								queueBeta = "gr.beta.didx.sim";
								isSend = true;
							}
//							if (classname.equals("ms.gameapp.GameAppSE")) {
//
//								queue = "gr.dc4.didx.gameapp";
//								queuebk = "gr.dc2.didx.gameapp";
//								queueBeta = "gr.beta.didx.gameapp";
//
//							}
							if (classname.equals("ms.promotion.Promotion")) {
								queue = "gr.dc4.didx.promotion";
								queuebk = "gr.dc2.didx.promotion";
								queueBeta = "gr.beta.didx.promotion";
								logit = true;
								logfile = "promotion";
								isSend = true;
							}
							if (classname.equals("ms.productold.Promotion")) {
								queue = "gr.dc4.didx.productoldpromotion";
								queuebk = "gr.dc2.didx.productoldpromotion";
								queueBeta = "gr.beta.didx.productoldpromotion";
								logit = true;
								logfile = "productoldpromotion";
								isSend = true;
							}
							if (classname.equals("ms.productse.ProductSE")) {
								queue = "gr.dc4.didx.productse";
								queuebk = "gr.dc2.didx.productse";
								queueBeta = "gr.beta.didx.productse";
								isSend = true;
							}

							if (isSend == true) {
								for (int c = 0; c < 100000000; c++) {
									try {
										message.ID= ID;
										boolean rsPush = QueueHelper.Current(rabitUrl).PushAll(queue, queue, queuebk,
												queueBeta, message, isLog, strNOTE, datacenter);

										if (logit) {
											Logs.LogSysMessage(logfile,
													Utils.GetCurrentDate() + ":push " + queue + " LangID: " + LangID
															+ ", site: " + siteID + ", Identify: " + strIdentify
															+ ", NOTE:" + strNOTE + ",classname:" + classname + ",date:"
															+ date.toString() + ",dc" + datacenter);
										}
										Logs.WriteLine("\r\npush " + queue);
										Logs.Log(isLog, message.Note,
												"push " + queue + " LangID: " + LangID + ", site: " + siteID);
										if (rsPush) {
											break;

										} else {

											if (c % 5 == 0 && c > 2) {
												String messageSL = "Inject error ( " + c + " times ), PushAll: false";
												lineNotify.NotifyInfo(messageSL);
											}
											Thread.sleep(3000 * c);
										}

									} catch (Throwable e) {
										// e.printStackTrace();
										String errorMessage = Logs.GetStacktrace(e);
										// Logs.WriteLine("\r\npush fail " + queue);
										Logs.LogException(e);
										if (c % 5 == 0 && c > 2) {
											String messageSL = "Inject error ( " + c + " times ), PushAll:"
													+ errorMessage;
											lineNotify.NotifyInfo(messageSL);
										}
										Thread.sleep(3000 * c);

									}
								}

							}

							message = null;
							// }

						} else {
							// ms.gameapp.GameAppSE
							if (classname.equals("ms.gameapp.GameAppSE")) {

								MessageQueue messageRepush = new MessageQueue();
								String qu = "gr.dc4.sql.sysdata";
								String qu2 = "gr.dc4.sql.sysdata";
								String qubk = "gr.dc2.sql.sysdata";
								String qudev = "gr.beta.sql.sysdata";
								messageRepush.Action = DataAction.Update;
								messageRepush.ClassName = "mwg.wb.pkg.upsert";
								messageRepush.CreatedDate = date;
								messageRepush.Note = strNOTE;
								messageRepush.Lang = LangID;
								messageRepush.Identify = strIdentify;
								messageRepush.SiteID = siteID;
									messageRepush.ID = ID;
								messageRepush.DataCenter = datacenter;
								messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_GAMEAPPSE_FROM_INJECT;
								QueueHelper.Current(rabitUrl).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,
										strNOTE, datacenter);

							} else if (classname.equals("ms.common.KeywordRedirect")) {

								MessageQueue messageRepush = new MessageQueue();
								String qu = "gr.dc4.sql.sysdata";
								String qu2 = "gr.dc4.sql.sysdata";
								String qubk = "gr.dc2.sql.sysdata";
								String qudev = "gr.beta.sql.sysdata";
								// messageRepush.Action = DataAction.Update;
								// messageRepush.ClassName = "mwg.wb.pkg.upsert";
								// messageRepush.CreatedDate = Utils.GetCurrentDate();
								// messageRepush.Note = strNOTE;
								// messageRepush.Lang = LangID;
								// messageRepush.Identify = strIdentify;
								// messageRepush.SiteID = siteID;
								// messageRepush.DataCenter = datacenter;
								// messageRepush.Type =
								// MessageQueuePushType.REQUIRE_PUSH_RUN_GAMEAPPSE_FROM_INJECT;

								messageRepush.DataCenter = datacenter;
								messageRepush.Action = DataAction.Update;
								messageRepush.ClassName = "mwg.wb.pkg.upsert";
								messageRepush.Identify = strIdentify;// bang khoa chinh luon
								// messageRepush.Source=table;
								messageRepush.RepushClassName = "mwg.wb.pkg.common.KeywordRedirectSE";
								messageRepush.RepushQueue = "didx.common";
								messageRepush.CreatedDate = date;
								messageRepush.Note = strNOTE;
								messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_COMMON;
								messageRepush.SiteID = siteID;
								messageRepush.ID = ID;
								QueueHelper.Current(rabitUrl).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,
										strNOTE, datacenter);

							} else if (classname.equals("ms.productse.ProductSE")) {

								MessageQueue messageRepush = new MessageQueue();
								String qu = "gr.dc4.sql.sysdata";
								String qu2 = "gr.dc4.sql.sysdata";
								String qubk = "gr.dc2.sql.sysdata";
								String qudev = "gr.beta.sql.sysdata";
								messageRepush.Action = DataAction.Update;
								messageRepush.ClassName = "mwg.wb.pkg.upsert";
								messageRepush.CreatedDate = date;
								messageRepush.Note = strNOTE;
								messageRepush.Lang = LangID;
								messageRepush.Identify = strIdentify;
								messageRepush.SiteID = siteID;
								messageRepush.DataCenter = datacenter;
								messageRepush.ID = ID;
								messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_PRODUCTSE_FROM_INJECT;
								QueueHelper.Current(rabitUrl).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,
										strNOTE, datacenter);
								Logs.Log(isLog, strNOTE, "Upsert " + strIdentify + " ID:" + ID);
							} else if (classname.equals("ms.cook.CookSE")) {

								MessageQueue messageRepush = new MessageQueue();
								String qu = "gr.dc4.sql.sysdata";
								String qu2 = "gr.dc4.sql.sysdata";
								String qubk = "gr.dc2.sql.sysdata";
								String qudev = "gr.beta.sql.sysdata";
								messageRepush.Action = DataAction.Update;
								messageRepush.ClassName = "mwg.wb.pkg.upsert";
								messageRepush.CreatedDate = date;
								messageRepush.Note = strNOTE;
								messageRepush.Lang = LangID;
								messageRepush.Identify = strIdentify;
								messageRepush.SiteID = siteID;
								messageRepush.DataCenter = datacenter;
								messageRepush.ID = ID;
								messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_COOKSE_FROM_INJECT;
								QueueHelper.Current(rabitUrl).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,
										strNOTE, datacenter);
								Logs.Log(isLog, strNOTE, "Upsert " + strIdentify + " ID:" + ID);
							} else {
								// Logs.WriteLine("Upsert " + strIdentify + " ID:" + ID);
								Logs.Log(isLog, strNOTE, "Upsert " + strIdentify + " ID:" + ID);
								MessageQueue messageRepush = new MessageQueue();

								String[] arr = (strIdentify + "|").split("\\|");
								if (arr.length >= 3) {

									String table = arr[0];
									String recordName = arr[1];
									String recordValue = arr[2];

									String otherCol = arr.length >= 4 ? StringUtils.strip(arr[3], ",") : "";
//								if (table.equals("news")) {
//									logit = true;
//									logfile = "news";
//								}
									if (table.equals("product_language")) {
										Logs.Log(true, "DIDX_LOG|inject_e_product_lang",
												strIdentify + " action:" + strACTION);
									}
									boolean push = true;
									if (otherCol.equals("viewcounter")) {
										logit = false;

										String key = "viewcounter_" + table + "_" + recordValue;

										if (Hasrocessed.containsKey(key)) {

											long dt = Hasrocessed.get(key);
											if (date.getTime() < dt) {
												Logs.WriteLine("skip " + key);
												push = false;
											}
										}

									}

									String key1 = strIdentify + "_" + strACTION;
									var lastUpdatedw = Hasrocessed.get(key1);
									long lastUpdated = lastUpdatedw == null ? 0 : lastUpdatedw;
									if (date.getTime() < lastUpdated) {
										String d1 = Utils.FormatDateForGraph(date);
										String d2 = Utils.FormatDateForGraph(new Date(lastUpdated));
										Logs.Log(true, "DIDX_LOG|skipinject", "Skip:" + key1 + ": " + d1 + ":" + d2);
										push = false;
									}

									if (push) {
										messageRepush.SqlList = BuildSqlList(messageRepush, strIdentify, table,
												recordName, recordValue, otherCol, action, condb, strNOTE, isLog,
												messageRepush);
//news|newsid|815287|viewcounter

										if (otherCol.equals("viewcounter")) {
											String key = "viewcounter_" + table + "_" + recordValue;

											long dt2 = System.currentTimeMillis();
											Hasrocessed.put(key, dt2);

										}

										String qu = "";
										String qu2 = "";
										String qubk = "";
										String qudev = "";

										if (table.equals("pm_currentinstock")) {

											long rid = Long.valueOf(recordValue);
											Date dateC = new Date();
											Calendar calendar = Calendar.getInstance(); // calendar instance
											calendar.setTime(dateC);
											int d = calendar.get(Calendar.HOUR_OF_DAY);

											if (d > 19 && d <= 24 || d < 6) {

												int idx = (int) (rid % queueBigData.length);
												qu = queueBigData[idx].replace("gr.dc2.", "gr.dc4.");
												qu2 = qu;
												qubk = queueBigData[idx];
												qudev = "gr.beta.sql.stock";
												messageRepush.Note = "BIGDATA";// strNOTE;
											} else {

												int h = Utils.GetQueueNum5(rid);
												qu = "gr.dc4.sql.stock" + h;
												qu2 = "gr.dc4.sql.stock" + h;
												qubk = "gr.dc2.sql.stock" + h;
												qudev = "gr.beta.sql.stock";
											}

											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.CreatedDate = date;
											messageRepush.DataCenter = datacenter;

											messageRepush.Hash = rid;

											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_STOCK;

										} else if (table.equals("pm_currentinstock_bhx")) {

											long rid = Long.valueOf(recordValue);
											qu = "gr.dc4.sql.stockbhx";
											qu2 = "gr.dc4.sql.stockbhx";
											qubk = "gr.dc2.sql.stockbhx";
											qudev = "gr.beta.sql.stockbhx";
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.CreatedDate = date;
											messageRepush.DataCenter = datacenter;

											messageRepush.Hash = rid;
											messageRepush.Note = strNOTE;
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_STOCK;

										} else if (table.equals("news") && otherCol.equals("viewcounter")) {
											// chi viewcount
											qu = "gr.dc4.sql.sysnews";
											qu2 = "gr.dc4.sql.sysnews";
											qubk = "gr.dc2.sql.sysnews";
											qudev = "gr.beta.sql.sysnews";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_VIEWCOUNT;

										} else if (table.equals("cook_dish") && otherCol.equals("viewcount")) {
											// chi viewcount

											qu = "gr.dc4.sql.sysdata";
											qu2 = "gr.dc4.sql.sysdata";
											qubk = "gr.dc2.sql.sysdata";
											qudev = "gr.beta.sql.sysdata";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_COOK_VIEWCOUNT;

										} else if (table.equals("news") && otherCol.equals("categoryid,viewcounter")) {
											// chi viewcount
											qu = "gr.dc4.sql.sysnews";
											qu2 = "gr.dc4.sql.sysnews";
											qubk = "gr.dc2.sql.sysnews";
											qudev = "gr.beta.sql.sysnews";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_VIEWCOUNT;

										} else if (table.equals("product")
												&& otherCol.equals("categoryid,totalreview")) {
											// chi viewcount
											qu = "gr.dc4.sql.sysdata";
											qu2 = "gr.dc4.sql.sysdata";
											qubk = "gr.dc2.sql.sysdata";
											qudev = "gr.beta.sql.sysdata";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											if (messageRepush.CategoryID == 8232 || messageRepush.CategoryID == 8233) {
												messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_GAMEAPP_VIEWCOUNT;
											}

										} else if (table.equals("news")) {

											if (messageRepush.PushType == PushType.Faq) {
												qu = "gr.dc4.sql.sysnews";
												qu2 = "gr.dc4.sql.sysnews";
												qubk = "gr.dc2.sql.sysnews";
												qudev = "gr.beta.sql.sysnews";
												messageRepush.DataCenter = datacenter;
												messageRepush.Action = DataAction.Update;
												messageRepush.ClassName = "mwg.wb.pkg.upsert";
												messageRepush.CreatedDate = date;
												messageRepush.Note = strNOTE;
												messageRepush.ID = ID;
												messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_FAQ_FROM_INJECT;
											} else {
												qu = "gr.dc4.sql.sysnews";
												qu2 = "gr.dc4.sql.sysnews";
												qubk = "gr.dc2.sql.sysnews";
												qudev = "gr.beta.sql.sysnews";
												messageRepush.DataCenter = datacenter;
												messageRepush.Action = DataAction.Update;
												messageRepush.ClassName = "mwg.wb.pkg.upsert";
												messageRepush.CreatedDate = date;
												messageRepush.Note = strNOTE;
												messageRepush.ID = ID;
												messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_NEWSSE_FROM_INJECT;
											}

										} else if (table.equals("news_event")) {
											qu = "gr.dc4.sql.sysnews";
											qu2 = "gr.dc4.sql.sysnews";
											qubk = "gr.dc2.sql.sysnews";
											qudev = "gr.beta.sql.sysnews";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											messageRepush.ID = ID;
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_NEWSEVENT_FROM_INJECT;

										} else if (table.equals("news_hottopic")) {
											qu = "gr.dc4.sql.sysnews";
											qu2 = "gr.dc4.sql.sysnews";
											qubk = "gr.dc2.sql.sysnews";
											qudev = "gr.beta.sql.sysnews";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											messageRepush.ID = ID;
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_NEWSTOPIC_FROM_INJECT;

										} else if (table.equals("product_gallery")) {
											qu = "gr.dc4.sql.sysdata";
											qu2 = "gr.dc4.sql.sysdata";
											qubk = "gr.dc2.sql.sysdata";
											qudev = "gr.beta.sql.sysdata";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											messageRepush.ID = ID;
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_USER_GALLERY_FROM_INJECT;

										} else if (table.equals("product_old_detail")) {
											qu = "gr.dc4.sql.productold";
											qu2 = "gr.dc4.sql.productold";
											qubk = "gr.dc2.sql.productold";
											qudev = "gr.beta.sql.productold";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											messageRepush.ID = ID;
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_PRODUCT_OLD_FROM_INJECT;

										} else if (table.equals("payment_category")) {
											qu = "gr.dc4.sql.sysdata";
											qu2 = "gr.dc4.sql.sysdata";
											qubk = "gr.dc2.sql.sysdata";
											qudev = "gr.beta.sql.sysdata";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.Identify = recordValue;// bang khoa chinh luon
											messageRepush.Source = table;
											messageRepush.RepushClassName = "mwg.wb.pkg.common.Payment";
											messageRepush.RepushQueue = "didx.common";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_COMMON;
											messageRepush.ID = ID;
										}
//										else if (table.equals("keywordredirect")) {
//											qu = "gr.dc4.sql.sysdata";
//											qu2 = "gr.dc4.sql.sysdata";
//											qubk = "gr.dc2.sql.sysdata";
//											qudev = "gr.beta.sql.sysdata";
//											messageRepush.DataCenter = datacenter;
//											messageRepush.Action = DataAction.Update;
//											messageRepush.ClassName = "mwg.wb.pkg.upsert";
//											messageRepush.Identify=recordValue;//bang khoa chinh luon
//											messageRepush.Source=table;
//											messageRepush.RepushClassName="mwg.wb.pkg.common.KeywordRedirectSE";
//											messageRepush.RepushQueue="didx.common";
//											messageRepush.CreatedDate = Utils.GetCurrentDate();
//											messageRepush.Note = strNOTE;
//											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_COMMON;
//
//										}
										else {
											if (messageRepush.IsCreateEdge == true) {
												qu = "gr.dc4.sql.sysdataedge";
												qu2 = "gr.dc4.sql.sysdataedge";
												qubk = "gr.dc2.sql.sysdataedge";
												qudev = "gr.beta.sql.sysdataedge";
												messageRepush.DataCenter = datacenter;
												messageRepush.Action = DataAction.Update;
												messageRepush.ClassName = "mwg.wb.pkg.upsert";
												messageRepush.CreatedDate = date;
												messageRepush.Note = strNOTE;
												messageRepush.Type = 0;
												messageRepush.ID = ID;
											} else {
												qu = "gr.dc4.sql.sysdata";
												qu2 = "gr.dc4.sql.sysdata";
												qubk = "gr.dc2.sql.sysdata";
												qudev = "gr.beta.sql.sysdata";
												messageRepush.DataCenter = datacenter;
												messageRepush.Action = DataAction.Update;
												messageRepush.ClassName = "mwg.wb.pkg.upsert";
												messageRepush.CreatedDate = date;
												messageRepush.Note = strNOTE;
												messageRepush.Type = 0;
												messageRepush.ID = ID;
											}
										}
										if (table.equals("pm_product")) {

											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_INJECT;

										}
										if (messageRepush.PushType == PushType.Currentinstock
												&& messageRepush.BrandID == 3) {
											// bo qua

										} else {
											messageRepush.ID= ID;
											for (int c = 0; c < 100000000; c++) {
												try {

													QueueHelper.Current(rabitUrl).PushAll(qu, qu2, qubk, qudev,
															messageRepush, isLog, strNOTE, datacenter);
													if (logit) {

														Logs.LogSysMessage(logfile,
																Utils.GetCurrentDate() + ":push " + qu + " LangID: "
																		+ LangID + ", site: " + siteID + ", Identify: "
																		+ strIdentify + ", NOTE:" + strNOTE
																		+ ",classname:" + classname + ",date:"
																		+ date.toString() + ",dc" + datacenter);

													}
													Logs.WriteLine("\r\npush " + qu);
													Logs.Log(isLog, messageRepush.Note,
															"push " + qu + " LangID: " + LangID + ", site: " + siteID);
													if (action == DataAction.Update) {
														Hasrocessed.put(key1, System.currentTimeMillis());
													}
													break;
												} catch (Throwable e) {
													Logs.LogException(e);
													String errorMessage = Logs.GetStacktrace(e);
													Logs.WriteLine("\r\npush fail " + qu);
													e.printStackTrace();
													if (c % 5 == 0 && c > 2) {
														String messageSL = "Inject error ( " + c + " times ), PushAll:"
																+ errorMessage;
														lineNotify.NotifyInfo(messageSL);
													}
													Thread.sleep(3000 * c);

												}
											}
										}

										// System.out.println( strIdentify +",datacenter:"+datacenter);

									}
								}
							}
						}

						if (ID > tempLastID) {
							tempLastID = ID;

						}

					} // while
						// NoTX.database().declareIntent(null);
					 
					LastId = tempLastID + "";

					if (!Utils.StringIsEmpty(LastId) && !LastId.equals("0")) {
						for (int i = 0; i < 10000000; i++) {
							try {
								Utils.WriteAllText(LasIdFileName, LastId);
								break;
							} catch (Throwable eF) {
								Logs.LogException(eF);
								Utils.Sleep(i * 5000);
								if (i % 5 == 0 && i > 2) {
									String message = "Injection:" + Utils.GetCurrentDate()
											+ ":WriteAllText:LasIdFileName:" + Utils.stackTraceToString(eF);
									lineNotify.NotifyInfo(message);
								}
							}
						}

					}

				}

				if (dem <= 0) {
					Logs.WriteLine("Nothing found, sleep in 5 seconds");
					Thread.sleep(5 * 1000);
				}
				if (rs != null) {
					rs.close();
				}
				if (csSQL != null) {
					csSQL.close();
				}
				if (csStore != null) {
					csStore.close();
				}
				
				dem = 0;
			}
			ErrorCount = 0;
		} catch (

		Throwable exx) {
			ErrorCount++;
			Logs.LogException(exx);
			if (ErrorCount % 5 == 0 && ErrorCount > 2) {
				String message = "Injection:" + Utils.GetCurrentDate() + ":" + Utils.stackTraceToString(exx);
				lineNotify.NotifyInfo(message);
			}
			Thread.sleep(5000 * ErrorCount);
		} finally {
			if (queueConnect != null)
				queueConnect.Close();
			if (dbclient != null)
				dbclient.closeConnection();
			if (condb != null)
				condb.close();
			// factoryWrite.CloseNoTx();
//			if (factory != null)
//				factory.Close();
		}

	}
	
	public static String ReformatIdString(String id, ColType type) {

		switch (type) {

		case INTEGER:
		case DOUBLE:
		case LONG:
			return Utils.GetNumberString(id);

		case STRING:
		default:
			return id.trim();

		}
	}

}
