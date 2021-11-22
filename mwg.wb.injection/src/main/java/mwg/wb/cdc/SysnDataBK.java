package mwg.wb.cdc;

import com.google.common.base.Strings;
import mwg.wb.business.WorkerHelper;
import mwg.wb.client.OracleClient;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.queue.QueueRabbitMQ;
import mwg.wb.common.DidxHelper;
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
import org.apache.commons.lang.StringUtils;

import java.net.InetAddress;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SysnDataBK {

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

	public static String backendGroupToken = "sizZI5Yq5AiyBXtz1tEw9hrTqjKKKh6yPszn7qi1BaU";
	public static String[] queueBigData = { "gr.dc2.sql.productold", "gr.dc2.sql.productoldpromotion",
			"gr.dc2.sql.promotion"

			, "gr.dc2.sql.stock0", "gr.dc2.sql.stock1", "gr.dc2.sql.stock2", "gr.dc2.sql.stock3", "gr.dc2.sql.stock4"

			, "gr.dc2.sql.stockbhx", "gr.dc2.sql.sim"

			, "gr.dc2.sql0", "gr.dc2.sql1", "gr.dc2.sql2", "gr.dc2.sql3", "gr.dc2.sql4"

			, "gr.dc2.sqlpri0", "gr.dc2.sqlpri1", "gr.dc2.sqlpri2"

			, "gr.dc2.sqlpro0", "gr.dc2.sqlpro1", "gr.dc2.sqlpro2" };

	public SysnDataBK(String injectProgramName, String syncMessageStore, String tbl, String Queue) {
		SyncMessageStore = syncMessageStore;
		InjectProgramName = injectProgramName;
		SyncTableName = tbl;
		SyncQueue = Queue;
		// factoryWrite = afactoryWrite;
		// factoryRead = afactoryRead;
		// pushStatus = apushStatus;
		// productHelper = new ProductHelper(factoryRead);
		config = WorkerHelper.GetWorkerClientConfig();
		lineNotify = new LineNotify("LINE", backendGroupToken);

	}

	public String SyncMessageStore;
	public String SyncTableName;
	public String InjectProgramName;
	public String SyncQueue;
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
	public Map<String, Long> Hasrocessed = new HashMap<String, Long>();

	public static void GetBrandIDBYStoreid(Connection cnndb, int storeid, MessageQueue msg) throws SQLException {

		String sqlDb = "SELECT brandid,provinceid,districtid   FROM   masterdata.pm_store where storeid=" + storeid
				+ "";

		Statement cs = cnndb.createStatement();
		ResultSet reader = cs.executeQuery(sqlDb);
		try {

			ResultSetMetaData rsmd = reader.getMetaData();
			int columnsCount = rsmd.getColumnCount();
			while (reader.next()) {

				for (int i = 1; i < columnsCount + 1; i++) {

					String cl = rsmd.getColumnName(i).toLowerCase();

					if (cl.equals("brandid")) {
						msg.BrandID = reader.getInt(i);
					}
					if (cl.equals("provinceid")) {
						msg.ProvinceID = reader.getInt(i);
					}
					if (cl.equals("districtid")) {
						msg.DistrictID = reader.getInt(i);
					}

				}
				return;

			} // while
		} finally {
			cs.close();
			reader.close();
			reader = null;
			// database.Close();

		}

	}

	public static List<SqlInfo> BuildSqlList(MessageQueue messageRepush, String Identify, String table,
			String recordName, String recordValue, String otherColVao, DataAction action, Connection cnndb,
			String strNote, boolean isLog, MessageQueue ref) throws SQLException {
		ResultMessage resultMessage = new ResultMessage();
		// DGRAPH_SYNC_MESSAGE_SEL (ID,TABLENAME,ACTION,IDENTIFY,CREATEDDATE)
		// IDENTIFY TABLENAME|productid|1212|cotkhac,cot khac
		
		//product_pricerange|rangeid|30
		
		// string connectstring =
		// ms.common.ConfigHelper.Current._ORIENTDB_CONNECTIONSTRING_;
		// Orientclient orientclient = new Orientclient(connectstring);
		// orientclient.Connect();
		List<SqlInfo> SqlList = new ArrayList<SqlInfo>();

//		if (table.equals("gen_ward")) {
//			return SqlList;
//		}

		try {

			resultMessage.Code = ResultCode.Success;
			if (recordValue.isEmpty())
				return SqlList;
			if (action == DataAction.Delete) {
				// crm.crm_productlocking
				// masterdata.gen_store_ward_distance
				// masterdata.pm_store_pm_maingroup
				// pm_warrantymonthbyproduct
				// pm_warrantymonthbysubgroup
				if (table.equals("pm_specialsaleprogram_invstt") || table.equals("pm_specialsaleprogram_product")
						|| table.equals("pm_specialsaleprogram_subgroup") || table.equals("pm_specialsaleprogram_brand")
						|| table.equals("pm_warrantymonthbyproduct") || table.equals("pm_warrantymonthbysubgroup")

				) {
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
				if (table.equals("pm_saleorder_locking")) {
					String sqlDbDelete = "UPDATE  pm_saleorder_locking set isdeleted=1 where  " + recordName + "="
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
				String dbSchema = "";
				TableEdgesInfo tableInfo = GraphDBConstant.TABLE_EDGES_INFO.get(tbl);

				if (tableInfo != null && Utils.StringIsEmpty(tableInfo.dbschema) == false) {
					dbSchema = tableInfo.dbschema;
					if (table.equals("cook_dish") || table.equals("cook_dish_category")
							|| table.equals("cook_dish_recipe") || table.equals("cook_recipe")
							|| table.equals("cook_recipe_ingredients") || table.equals("cook_recipe_steps")
							|| table.equals("cook_recipe_steps_gallery")) {
						dbSchema = "WEBCAP";
					}
						if (table.equals("product") || table.equals("product_language")
								|| table.equals("gameapp_product_platfrom") || table.equals("product_gallery")
								|| table.equals("news")) {
							dbSchema = "WEBCAP";
						}
					tbl = dbSchema + "." + tableInfo.dbtable;
				}
//				if (table.equals("pm_currentinstock")) {
//					tbl = "erp.pm_currentinstock";
//				}
//				if (table.equals("pm_currentinstock_bhx")) {
//					tbl = "webcap.pm_currentinstock";
//					// Logs.WriteLine(tbl);
//					// return SqlList;
//				}
//				if (table.equals("cmt_comment_sta")) {
//					tbl = "webcap.cmt_comment_sta";
//					// Logs.WriteLine(tbl);
//					// return SqlList;
//				}
//
//				if (table.equals("pm_saleorder_locking")) {
//					tbl = "erp.pm_saleorder_locking";
//
//				}
//				if (
//
//				table.equals("crm_productlocking")
//
//				) {
//					tbl = "crm." + table;
//				}
//
//				if (table.equals("pm_store") || table.equals("pm_product") || table.equals("pm_item")
//						|| table.equals("pm_item_exchangequantityunit") || table.equals("pm_quantityunit")
//						|| table.equals("pm_product_productstatus") || table.equals("gen_district")
//						|| table.equals("pm_store_pm_maingroup") || table.equals("system_user")
//						|| table.equals("gen_ward") || table.equals("gen_store_ward_distance")
//						|| table.equals("pr_priceparameter") || table.equals("gen_province")) {
//					tbl = "masterdata." + table;
//				}
//
//				if (table.equals("pm_specialsaleprogram") 
//						|| table.equals("pm_specialsaleprogram_invstt")
//						|| table.equals("pm_specialsaleprogram_product")
//						|| table.equals("pm_specialsaleprogram_subgroup")
//						|| table.equals("pm_specialsaleprogram_brand")
//							|| table.equals("pm_warrantymonthbyproduct")
//							||  table.equals("pm_warrantymonthbysubgroup")
//						
//						) {
//					tbl = "erp." + table;
//				}

				// Upsert
				// product_language|recordid|659311|simage,mimage,bimage,updateddate,updateduser,

				if (table.equals("cook_dish") || table.equals("cook_dish_category") || table.equals("cook_dish_recipe")
						|| table.equals("cook_recipe") || table.equals("cook_recipe_ingredients")
						|| table.equals("cook_recipe_steps") || table.equals("cook_recipe_steps_gallery")) {
					dbSchema = "WEBCAP";
					tbl = dbSchema + "." + table;
				}
					if (table.equals("product") || table.equals("product_language")
							|| table.equals("gameapp_product_platfrom") || table.equals("product_gallery")
							|| table.equals("news")) {
						dbSchema = "WEBCAP";
						tbl = dbSchema + "." + table;
					}
				boolean isadd = true;
				String sqlDb = "SELECT *   FROM  " + tbl + " where  " + recordName + "='" + recordValue + "'";
				String otherCol = otherColVao;

				if (!otherCol.isEmpty()) {
					if (!otherCol.contains(recordName)) {
						otherCol = otherCol + "," + recordName;
					}
					if (tbl.equals("product_propvalue_lang")) {
						if (!otherCol.contains("valueid")) {
							otherCol += ",valueid";
						}
						if (!otherCol.matches(".*(^|,)siteid($|,).*")) {
							otherCol += ",siteid";
						}
						if (!otherCol.contains("languageid")) {
							otherCol += ",languageid";
						}
					}
					if (tbl.equals("product_manu_lang") && !otherCol.contains("manufacturerid")) {
						otherCol += ",manufacturerid";
					}
					if (tbl.equals("product_category_lang")) {
						if (!otherCol.contains("categoryid")) {
							otherCol += ",categoryid";
						}
						if (!otherCol.matches(".*(^|,)siteid($|,).*")) {
							otherCol += ",siteid";
						}
					}
					sqlDb = "SELECT \"" + otherCol.toUpperCase().replace(",", "\",\"") + "\"   FROM  " + tbl
							+ " where  " + recordName + "='" + recordValue + "'";
					isadd = false;
				}
				if (table.equals("product")) {
					// Logs.WriteLine(sqlDb);
				}
//				if (table.equals("pm_currentinstock")) {
//					messageRepush.Logs = messageRepush.Logs + " id:" + recordValue;
//				}
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
							if (table.equals("pm_specialsaleprogram_product")) {
								if (cl.equals("productid")) {
									productCode = reader.getString(i).trim();
									ref.Identify = productCode;

								}
								if (cl.equals("productidref")) {
									String productidref = (reader.getString(i) + "").trim();
									ref.RefIdentify = productidref;
								}
//								if (cl.equals("brandid")) { ko phai brandid cty
//									int brandid = reader.getInt(i);
//									ref.BrandID = brandid;
//
//								}
							}
							if (table.equals("pm_product_productstatus")) {
								if (cl.equals("productid")) {
									productCode = reader.getString(i).trim();
									ref.Identify = productCode;

								}
								if (cl.equals("companyid")) {
									ref.CompanyID = reader.getInt(i);

								}
							}

							if (table.equals("pm_product")) {

								if (cl.equals("productid")) {
									productCode = reader.getString(i).trim();
									ref.Identify = productCode;

								}
								if (cl.equals("productidref")) {
									String productidref = (reader.getString(i) + "").trim();
									ref.RefIdentify = productidref;
								}
//								if (cl.equals("brandid")) { ko phai brandid cty
//									int brandid = reader.getInt(i);
//									ref.BrandID = brandid;
//
//								}
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
								if (cl.equals("storeid")) {
									int storeid = reader.getInt(i);
									ref.Storeid = storeid;
									ref.PushType = PushType.Currentinstock;

								}

//								if (cl.equals("quantity")) {
//									int quantity = reader.getInt(i);
//									ref.Quantity = quantity;
//									ref.PushType = PushType.Currentinstock;
//									messageRepush.Logs = messageRepush.Logs + " qty:" + quantity;
//								}
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
								ref.BrandID = 3;// BHX
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
//							if (cl.equals("keywordid") && table.equals("keyword_suggest_news")) {
//								long keywordid = reader.getLong(i);
//								ref.Identify = String.valueOf(keywordid);
//								ref.PushType = PushType.Keyword_Suggest_News;
//
//							}
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
//								case Types.NUMERIC:
//									if (cl.equals("lat") || cl.equals("lng")) {
//										params.put(cl, reader.getDouble(i));
//										break;
//									}
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
								case Types.NUMERIC:
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
									
									 try {
									params.put(cl, reader.getString(i).trim());
									 }catch (Throwable e) {
										 Logs.LogException("reader.getString(i).trim() cot data sai kieu du lieu: " +table+" " +cl+" sqlType="+sqlType );
										Logs.LogException(e);
										 throw e;
									}
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
							Logs.getInstance().Log(isLog, strNote, "buildsql", params);
						}
						SqlInfo sqlinfoUpdate = new SqlInfo();

						if (table.equals("pm_currentinstock")) {

							if (messageRepush.BrandID <= 0) {
								if (messageRepush.Storeid > 0) {
									GetBrandIDBYStoreid(cnndb, messageRepush.Storeid, messageRepush);
									params.put("brandid", messageRepush.BrandID);
									params.put("provinceid", messageRepush.ProvinceID);
									params.put("districtid", messageRepush.DistrictID);
									Logs.LogSysMessage("fix-brandid0", recordValue + ":st:" + messageRepush.Storeid
											+ ",pr:" + messageRepush.ProvinceID + ",br:" + messageRepush.BrandID);
									if (messageRepush.BrandID < 0 || messageRepush.ProvinceID < 0) {
										Logs.LogSysMessage("stock-error_brandid0",
												recordValue + ":st:" + messageRepush.Storeid + ",pr:"
														+ messageRepush.ProvinceID + ",br:" + messageRepush.BrandID);

									}
								}
							}
						}

						sqlinfoUpdate.Sql = sql;
						sqlinfoUpdate.Params = params;
						if (strNote.contains("DIDX_TOP")) {
							sqlinfoUpdate.tablename = table;
							sqlinfoUpdate.tablekey = recordValue;
						}
						SqlList.add(sqlinfoUpdate);

						if (action == DataAction.Add) {
							Logs.Log(isLog, strNote, "Create edge");
							try {
								// banner
								if (table.equals("banners_place_cat")) {
									// banners(bannerid)
									// banners_place_cat(bannerid,placeid,categoryid)
									// banners_place_cat_manu(bannerid,placeid,categoryid,manufacturerid)
									// banners_place_province(bannerid,placeid,provinceid)
									int bannerid = Utils.toInt(reader.getString("bannerid"));
									int placeid = Utils.toInt(reader.getString("placeid"));
									if (bannerid > 0) {

										// tableInfo.edges.

//										SqlInfo sqlinfoEgde1 = new SqlInfo();
										String e = "e_banners_place_cat";

										// NEW

										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", bannerid);
											sqlinfoEgde.Params.put("to", recordValue);
											// them

											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", "e_banners_place_cat chua dinh nghia");

											Logs.Log(isLog, strNote, "create edge " + e);
											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "banners");
											sqlinfoEgde2.Params.put("totbl", "banners_place_cat");
											sqlinfoEgde2.Params.put("fromcol", "bannerid");
											sqlinfoEgde2.Params.put("tocol", "recordid");
											sqlinfoEgde2.Params.put("from", bannerid);
											sqlinfoEgde2.Params.put("to", recordValue);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}

									}
									if (placeid > 0) {

										// SqlInfo sqlinfoEgde1 = new SqlInfo();
										String e = "e_bannerplaces_place_cat";

										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", placeid);
											sqlinfoEgde.Params.put("to", recordValue);

											// hthem
											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e + " chua dinh nghia");

											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "banner_places");
											sqlinfoEgde2.Params.put("fromcol", "bannerplaceid");
											sqlinfoEgde2.Params.put("from", placeid);

											sqlinfoEgde2.Params.put("totbl", "banners_place_cat");
											sqlinfoEgde2.Params.put("tocol", "recordid");
											sqlinfoEgde2.Params.put("to", recordValue);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}

									}

								}
								if (table.equals("banners_place_cat_manu")) {
									int bannerid = Utils.toInt(reader.getString("bannerid"));
									int placeid = Utils.toInt(reader.getString("placeid"));
									if (bannerid > 0) {

										String e = "e_banners_place_cat_manu";

										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", bannerid);
											sqlinfoEgde.Params.put("to", recordValue);
											// them
											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e + " chua dinh nghia");

											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "banners");
											sqlinfoEgde2.Params.put("fromcol", "bannerid");
											sqlinfoEgde2.Params.put("from", bannerid);

											sqlinfoEgde2.Params.put("totbl", "banners_place_cat_manu");
											sqlinfoEgde2.Params.put("tocol", "recordid");
											sqlinfoEgde2.Params.put("to", recordValue);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}

									}
									if (placeid > 0) {

										// SqlInfo sqlinfoEgde1 = new SqlInfo();
										String e = "e_bannerplaces_place_cat_manu";

										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", placeid);
											sqlinfoEgde.Params.put("to", recordValue);
											// them
											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e + " chua dinh nghia");

											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "banner_places");
											sqlinfoEgde2.Params.put("fromcol", "bannerplaceid");
											sqlinfoEgde2.Params.put("from", placeid);

											sqlinfoEgde2.Params.put("totbl", "banners_place_cat_manu");
											sqlinfoEgde2.Params.put("tocol", "recordid");
											sqlinfoEgde2.Params.put("to", recordValue);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}

									}
								}
								if (table.equals("banners_place_province")) {
									int bannerid = Utils.toInt(reader.getString("bannerid"));
									int placeid = Utils.toInt(reader.getString("placeid"));
									if (bannerid > 0) {
										// SqlInfo sqlinfoEgde1 = new SqlInfo();
										String e = "e_banners_place_province";

										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", bannerid);
											sqlinfoEgde.Params.put("to", recordValue);

											// them
											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e + " chua dinh nghia");

											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "banners");
											sqlinfoEgde2.Params.put("fromcol", "bannerid");
											sqlinfoEgde2.Params.put("from", bannerid);

											sqlinfoEgde2.Params.put("totbl", "banners_place_province");
											sqlinfoEgde2.Params.put("tocol", "recordid");
											sqlinfoEgde2.Params.put("to", recordValue);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}

									}

									if (placeid > 0) {

										String e = "e_bannerplaces_place_province";
										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", placeid);
											sqlinfoEgde.Params.put("to", recordValue);

											// them
											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e + " chua dinh nghia");

											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "banner_places");
											sqlinfoEgde2.Params.put("fromcol", "bannerplaceid");
											sqlinfoEgde2.Params.put("from", placeid);

											sqlinfoEgde2.Params.put("totbl", "banners_place_province");
											sqlinfoEgde2.Params.put("tocol", "recordid");
											sqlinfoEgde2.Params.put("to", recordValue);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}

									}

								}

								if (table.equals("pm_store")) {

									int districtid = Utils.toInt(reader.getString("districtid"));
									int provinceid = Utils.toInt(reader.getString("provinceid"));
									if (districtid > 0) {

										String e = "e_store_district";
										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", recordValue);
											sqlinfoEgde.Params.put("to", districtid);

											// them
											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e);

											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "pm_store");
											sqlinfoEgde2.Params.put("fromcol", "storeid");
											sqlinfoEgde2.Params.put("from", recordValue);

											sqlinfoEgde2.Params.put("totbl", "gen_district");
											sqlinfoEgde2.Params.put("tocol", "districtid");
											sqlinfoEgde2.Params.put("to", districtid);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}

									}
									if (provinceid > 0) {

										String e = "e_store_province";
										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", recordValue);
											sqlinfoEgde.Params.put("to", provinceid);

											// thenm

											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e);

											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "pm_store");
											sqlinfoEgde2.Params.put("fromcol", "storeid");
											sqlinfoEgde2.Params.put("from", recordValue);

											sqlinfoEgde2.Params.put("totbl", "gen_province");
											sqlinfoEgde2.Params.put("tocol", "provinceid");
											sqlinfoEgde2.Params.put("to", provinceid);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}

									}

								}

								if (table.equals("pm_store_info")) {

									int storeid = Utils.toInt(reader.getString("storeid"));

									if (storeid > 0) {

										String e = "e_pm_store_info";
										Logs.Log(isLog, strNote, "create edge " + e);

										SqlInfo sqlinfoEgde2 = new SqlInfo();
										sqlinfoEgde2.Params = new HashMap<String, Object>();
										sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde2.Params.put("edge", e);
										sqlinfoEgde2.Params.put("fromtbl", "pm_store");
										sqlinfoEgde2.Params.put("fromcol", "storeid");
										sqlinfoEgde2.Params.put("from", recordValue);

										sqlinfoEgde2.Params.put("totbl", "pm_store_info");
										sqlinfoEgde2.Params.put("tocol", "storeid");
										sqlinfoEgde2.Params.put("to", storeid);
										SqlList.add(sqlinfoEgde2);
										messageRepush.IsCreateEdge = true;

									}

								}
								// edge
								if (table.equals("pm_product")) {

									long productidref = Utils.toLong(reader.getString("productidref"));
									int productcolorid = Utils.toInt(reader.getString("productcolorid"));
									if (productidref > 0) {

										String e = "e_product_code";
										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", productidref);
											sqlinfoEgde.Params.put("to", recordValue);

											// them

											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e + " chua dinh nghia");

											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "product");
											sqlinfoEgde2.Params.put("fromcol", "productid");
											sqlinfoEgde2.Params.put("from", productidref);

											sqlinfoEgde2.Params.put("totbl", "pm_product");
											sqlinfoEgde2.Params.put("tocol", "recordid");
											sqlinfoEgde2.Params.put("to", recordValue);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}

									}
									if (productcolorid > 0) {

										String e = "e_code_color";
										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", recordValue);
											sqlinfoEgde.Params.put("to", productcolorid);

											// them

											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e + " chua dinh nghia");

											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "pm_product");
											sqlinfoEgde2.Params.put("fromcol", "recordid");
											sqlinfoEgde2.Params.put("from", recordValue);

											sqlinfoEgde2.Params.put("totbl", "product_color");
											sqlinfoEgde2.Params.put("tocol", "colorid");
											sqlinfoEgde2.Params.put("to", productcolorid);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}

									}

								}
								if (table.equals("pm_currentinstock")) {
									String code = reader.getString("productid");

									int storeid = Utils.toInt(reader.getString("storeid"));
									if (code != null && !code.isBlank()) {

										String e = "e_code_stock";
//										Logs.Log(isLog, strNote, "create edge " + e);
//										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
//												.findFirst().orElse(null);
//										if (edgeInfo != null) {
//											SqlInfo sqlinfoEgde = new SqlInfo();
//											sqlinfoEgde.Params = new HashMap<String, Object>();
//											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
//											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
//											sqlinfoEgde.Params.put("from", code.trim());
//											sqlinfoEgde.Params.put("to", recordValue);
//											//them
//											
//											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
//											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
//											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
//											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);
//											
//											SqlList.add(sqlinfoEgde);
//											messageRepush.IsCreateEdge = true;
//										} else {
//											Logs.LogErrorRequireFix("edge", e  );
//
//											SqlInfo sqlinfoEgde2 = new SqlInfo();
//											sqlinfoEgde2.Params = new HashMap<String, Object>();
//											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
//											sqlinfoEgde2.Params.put("edge", e);
//											sqlinfoEgde2.Params.put("fromtbl", "pm_product");
//											sqlinfoEgde2.Params.put("fromcol", "productid");
//											sqlinfoEgde2.Params.put("from", code.trim());
//
//											sqlinfoEgde2.Params.put("totbl", "pm_currentinstock");
//											sqlinfoEgde2.Params.put("tocol", "recordid");
//											sqlinfoEgde2.Params.put("to", recordValue);
//											SqlList.add(sqlinfoEgde2);
//											messageRepush.IsCreateEdge = true;
//										}

									}
									if (storeid > 0) {

										String e = "e_stock_store";
										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", recordValue);
											sqlinfoEgde.Params.put("to", storeid);

											// them

											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e);

											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "pm_currentinstock");
											sqlinfoEgde2.Params.put("fromcol", "recordid");
											sqlinfoEgde2.Params.put("from", recordValue);

											sqlinfoEgde2.Params.put("totbl", "pm_store");
											sqlinfoEgde2.Params.put("tocol", "storeid");
											sqlinfoEgde2.Params.put("to", storeid);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}

									}

									// Logs.WriteLine("done");

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
									if (cateid > 0) {

										String e = "e_category_lang";
										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", cateid);
											sqlinfoEgde.Params.put("to", recordValue);

											// them
											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e);

											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "product_category");
											sqlinfoEgde2.Params.put("fromcol", "categoryid");
											sqlinfoEgde2.Params.put("from", cateid);

											sqlinfoEgde2.Params.put("totbl", "product_category_lang");
											sqlinfoEgde2.Params.put("tocol", "recordid");
											sqlinfoEgde2.Params.put("to", recordValue);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}

									}
								}
								if (table.equals("product_manu_lang")) {

									int manufacturerid = Utils.toInt(reader.getString("manufacturerid"));
									if (manufacturerid > 0) {

										String e = "e_manu_lang";
										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", manufacturerid);
											sqlinfoEgde.Params.put("to", recordValue);

											// them
											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e);

											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "product_manu");
											sqlinfoEgde2.Params.put("fromcol", "manufacturerid");
											sqlinfoEgde2.Params.put("from", manufacturerid);

											sqlinfoEgde2.Params.put("totbl", "product_manu_lang");
											sqlinfoEgde2.Params.put("tocol", "recordid");
											sqlinfoEgde2.Params.put("to", recordValue);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}
									}
								}
								if (table.equals("product_detail")) {
									long productid = Utils.toLong(reader.getString("productid"));
									if (productid > 0) {
										messageRepush.RefIdentify = String.valueOf(productid);
										// messageRepush.Type=MessageQueuePushType.REQUIRE_PUSH_RUN_PRODUCTSE_FROM_INJECT;
										String e = "e_product_detail";
										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", productid);
											sqlinfoEgde.Params.put("to", recordValue);

											// them

											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e);

											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "product");
											sqlinfoEgde2.Params.put("fromcol", "productid");
											sqlinfoEgde2.Params.put("from", productid);

											sqlinfoEgde2.Params.put("totbl", "product_detail");
											sqlinfoEgde2.Params.put("tocol", "recordid");
											sqlinfoEgde2.Params.put("to", recordValue);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}

									}
								}
								if (table.equals("product_old_detail")) {
									String productcode = reader.getString("productcode");
									if (!Utils.StringIsEmpty(productcode)) {
										productcode = productcode.trim();
										SqlInfo sqlinfoEgde0 = new SqlInfo();
										sqlinfoEgde0.Params = new HashMap<String, Object>();
										sqlinfoEgde0.Sql = "update product_old_code  set isdeleted=0,productcode='"
												+ productcode + "' upsert where productcode='" + productcode + "'";
										SqlList.add(sqlinfoEgde0);

										String e = "e_productold_code";
										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", recordValue);
											sqlinfoEgde.Params.put("to", productcode);

											// them

											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e);
											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "product_old_detail");
											sqlinfoEgde2.Params.put("fromcol", "oldid");
											sqlinfoEgde2.Params.put("from", recordValue);

											sqlinfoEgde2.Params.put("totbl", "product_old_code");
											sqlinfoEgde2.Params.put("tocol", "productcode");
											sqlinfoEgde2.Params.put("to", productcode);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}

									}
								}
								if (table.equals("product_language")) {
									Logs.Log(true, "DIDX_LOG|e_product_lang", "<recordValue:" + recordValue);

									long productid = Utils.toLong(reader.getString("productid"));
									if (productid > 0) {

										messageRepush.RefIdentify = String.valueOf(productid);

										String e = "e_product_lang";

										SqlInfo sqlinfoEgde2 = new SqlInfo();
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", productid);
											sqlinfoEgde.Params.put("to", recordValue);

											// them
											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e);
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "product");
											sqlinfoEgde2.Params.put("fromcol", "productid");
											sqlinfoEgde2.Params.put("from", productid);

											sqlinfoEgde2.Params.put("totbl", "product_language");
											sqlinfoEgde2.Params.put("tocol", "recordid");
											sqlinfoEgde2.Params.put("to", recordValue);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;
										}
										Logs.getInstance().Log(true, "DIDX_LOG|e_product_lang", "create edge ",
												sqlinfoEgde2);

									} else {
										Logs.Log(true, "DIDX_LOG|e_product_lang", "productid<=0 ");
									}
									Logs.getInstance().Log(true, "DIDX_LOG|e_product_lang", "messageRepush ",
											messageRepush);
									Logs.Log(true, "DIDX_LOG|e_product_lang", "/recordValue>");
								}

								if (table.equals("product")) {
									int manufacturerid = 0;
									int categoryid = 0;
									try {

										manufacturerid = Utils.toInt(reader.getString("manufacturerid"));
										categoryid = Utils.toInt(reader.getString("categoryid"));
									} catch (Exception e) {
										// TODO: handle exception
									}
									if (manufacturerid > 0) {
										String e = "e_product_manu";
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", Utils.GetNumberString(recordValue));
											sqlinfoEgde.Params.put("to", manufacturerid);

											// them
											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e);
											SqlInfo sqlinfoEgde1 = new SqlInfo();
											sqlinfoEgde1.Params = new HashMap<String, Object>();
											sqlinfoEgde1.Type = SqlInfoType.CREATE_EDGE_BY_NODE;
											sqlinfoEgde1.Params.put("edge", "e_product_manu");
											sqlinfoEgde1.Params.put("from", Utils.GetNumberString(recordValue));
											sqlinfoEgde1.Params.put("to", manufacturerid);

											SqlList.add(sqlinfoEgde1);
											messageRepush.IsCreateEdge = true;
										}
									}
									if (categoryid > 0) {
										String e = "e_product_category";
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", Utils.GetNumberString(recordValue));
											sqlinfoEgde.Params.put("to", categoryid);
											// them
											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e);
											SqlInfo sqlinfoEgde1 = new SqlInfo();

											sqlinfoEgde1.Params = new HashMap<String, Object>();
											sqlinfoEgde1.Type = SqlInfoType.CREATE_EDGE_BY_NODE;
											sqlinfoEgde1.Params.put("edge", "e_product_category");
											sqlinfoEgde1.Params.put("from", Utils.GetNumberString(recordValue));
											sqlinfoEgde1.Params.put("to", categoryid);

											SqlList.add(sqlinfoEgde1);
											messageRepush.IsCreateEdge = true;
										}
									}

								}

								if (table.equals("product_prop_lang")) {
									String propertyid = reader.getString("propertyid");
									String e = "e_product_prop_lang";
									EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
											.findFirst().orElse(null);
									if (edgeInfo != null) {
										SqlInfo sqlinfoEgde = new SqlInfo();
										sqlinfoEgde.Params = new HashMap<String, Object>();
										sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde.Params.put("edge", edgeInfo.edge);
										sqlinfoEgde.Params.put("from", propertyid);
										sqlinfoEgde.Params.put("to", recordValue);
										// them
										sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
										sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
										sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
										sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

										SqlList.add(sqlinfoEgde);
										messageRepush.IsCreateEdge = true;
									} else {
										Logs.LogErrorRequireFix("edge", e);
										SqlInfo sqlinfoEgde1 = new SqlInfo();
										sqlinfoEgde1.Sql = "create edge e_product_prop_lang  from(select from product_prop  where propertyid= "
												+ propertyid + ") to (select from product_prop_lang    where recordid= "
												+ recordValue + " and in('e_product_prop_lang')[propertyid = "
												+ propertyid + "].size() = 0)";

										SqlList.add(sqlinfoEgde1);
										messageRepush.IsCreateEdge = true;
									}
								}
								if (table.equals("product_propgrp_lang")) {
									String groupid = reader.getString("groupid");
									String e = "e_product_propgrp_lang";
									EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
											.findFirst().orElse(null);
									if (edgeInfo != null) {
										SqlInfo sqlinfoEgde = new SqlInfo();
										sqlinfoEgde.Params = new HashMap<String, Object>();
										sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde.Params.put("edge", edgeInfo.edge);
										sqlinfoEgde.Params.put("from", groupid);
										sqlinfoEgde.Params.put("to", recordValue);
										// them
										sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
										sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
										sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
										sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

										SqlList.add(sqlinfoEgde);
										messageRepush.IsCreateEdge = true;
									} else {
										Logs.LogErrorRequireFix("edge", e);
										SqlInfo sqlinfoEgde1 = new SqlInfo();
										sqlinfoEgde1.Sql = "create edge e_product_propgrp_lang  from(select from product_propgrp   where groupid= "
												+ groupid + ") to (select from product_propgrp_lang    where recordid= "
												+ recordValue + " and in('e_product_propgrp_lang')[groupid = " + groupid
												+ "].size() = 0)";

										SqlList.add(sqlinfoEgde1);
										messageRepush.IsCreateEdge = true;
									}
								}
								if (table.equals("product_propvalue")) {
									String propertyid = reader.getString("propertyid");

									String e = "e_product_prop_value";
									Logs.Log(isLog, strNote, "create edge " + e);
									EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
											.findFirst().orElse(null);
									if (edgeInfo != null) {
										SqlInfo sqlinfoEgde = new SqlInfo();
										sqlinfoEgde.Params = new HashMap<String, Object>();
										sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde.Params.put("edge", edgeInfo.edge);
										sqlinfoEgde.Params.put("from", propertyid);
										sqlinfoEgde.Params.put("to", recordValue);

										// them
										sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
										sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
										sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
										sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

										SqlList.add(sqlinfoEgde);
										messageRepush.IsCreateEdge = true;
									} else {
										Logs.LogErrorRequireFix("edge", e);
										SqlInfo sqlinfoEgde2 = new SqlInfo();
										sqlinfoEgde2.Params = new HashMap<String, Object>();
										sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde2.Params.put("edge", e);
										sqlinfoEgde2.Params.put("fromtbl", "product_prop");
										sqlinfoEgde2.Params.put("fromcol", "propertyid");
										sqlinfoEgde2.Params.put("from", propertyid);

										sqlinfoEgde2.Params.put("totbl", "product_propvalue");
										sqlinfoEgde2.Params.put("tocol", "valueid");
										sqlinfoEgde2.Params.put("to", recordValue);
										SqlList.add(sqlinfoEgde2);
										messageRepush.IsCreateEdge = true;
									}

								}
								if (table.equals("product_propvalue_lang")) {
									String valueid = reader.getString("valueid");

									String e = "e_product_propvalue_lang";
									Logs.Log(isLog, strNote, "create edge " + e);
									EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
											.findFirst().orElse(null);
									if (edgeInfo != null) {
										SqlInfo sqlinfoEgde = new SqlInfo();
										sqlinfoEgde.Params = new HashMap<String, Object>();
										sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde.Params.put("edge", edgeInfo.edge);
										sqlinfoEgde.Params.put("from", valueid);
										sqlinfoEgde.Params.put("to", recordValue);
										sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
										sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
										sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
										sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);
										SqlList.add(sqlinfoEgde);
										messageRepush.IsCreateEdge = true;
									} else {
										Logs.LogErrorRequireFix("edge", e);
										SqlInfo sqlinfoEgde2 = new SqlInfo();
										sqlinfoEgde2.Params = new HashMap<String, Object>();
										sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde2.Params.put("edge", e);
										sqlinfoEgde2.Params.put("fromtbl", "product_propvalue");
										sqlinfoEgde2.Params.put("fromcol", "valueid");
										sqlinfoEgde2.Params.put("from", valueid);

										sqlinfoEgde2.Params.put("totbl", "product_propvalue_lang");
										sqlinfoEgde2.Params.put("tocol", "recordid");
										sqlinfoEgde2.Params.put("to", recordValue);
										SqlList.add(sqlinfoEgde2);
										messageRepush.IsCreateEdge = true;
									}
								}

								if (table.equals("product_pricerange_lang")) {
									String rangeid = reader.getString("rangeid");

									String e = "e_pricerange_lang";
									Logs.Log(isLog, strNote, "create edge " + e);
									EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
											.findFirst().orElse(null);
									if (edgeInfo != null) {
										SqlInfo sqlinfoEgde = new SqlInfo();
										sqlinfoEgde.Params = new HashMap<String, Object>();
										sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde.Params.put("edge", edgeInfo.edge);
										sqlinfoEgde.Params.put("from", rangeid);
										sqlinfoEgde.Params.put("to", recordValue);
										sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
										sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
										sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
										sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

										SqlList.add(sqlinfoEgde);
										messageRepush.IsCreateEdge = true;
									} else {
										Logs.LogErrorRequireFix("edge", e);
										SqlInfo sqlinfoEgde2 = new SqlInfo();
										sqlinfoEgde2.Params = new HashMap<String, Object>();
										sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde2.Params.put("edge", e);
										sqlinfoEgde2.Params.put("fromtbl", "product_pricerange");
										sqlinfoEgde2.Params.put("fromcol", "rangeid");
										sqlinfoEgde2.Params.put("from", rangeid);

										sqlinfoEgde2.Params.put("totbl", "product_pricerange_lang");
										sqlinfoEgde2.Params.put("tocol", "recordid");
										sqlinfoEgde2.Params.put("to", recordValue);
										SqlList.add(sqlinfoEgde2);
										messageRepush.IsCreateEdge = true;
									}

								}

								if (table.equals("product_color")) {

								}
								if (table.equals("product_color_lang")) {
									double productcolorid = reader.getDouble("colorid");

									if (productcolorid > 0) {

										String e = "e_product_color_lang";
										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", Utils.GetNumberStringDouble(productcolorid));
											sqlinfoEgde.Params.put("to", recordValue);

											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;

										} else {
											Logs.LogErrorRequireFix("edge", e);

											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "product_color");
											sqlinfoEgde2.Params.put("fromcol", "colorid");
											sqlinfoEgde2.Params.put("from",
													Utils.GetNumberStringDouble(productcolorid));

											sqlinfoEgde2.Params.put("totbl", "product_color_lang");
											sqlinfoEgde2.Params.put("tocol", "recordid");
											sqlinfoEgde2.Params.put("to", recordValue);
											SqlList.add(sqlinfoEgde2);
											messageRepush.IsCreateEdge = true;

										}

									}
								}

								if (table.equals("product_gallery")) {
									// double productid = reader.getDouble("productid");
									String productid = Utils.GetNumberStringDouble(reader.getDouble("productid"));
									String e = "e_product_gallery";
									Logs.Log(isLog, strNote, "create edge " + e);
									EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
											.findFirst().orElse(null);
									if (edgeInfo != null) {
										SqlInfo sqlinfoEgde = new SqlInfo();
										sqlinfoEgde.Params = new HashMap<String, Object>();
										sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde.Params.put("edge", edgeInfo.edge);
										sqlinfoEgde.Params.put("from", productid);
										sqlinfoEgde.Params.put("to", recordValue);

										sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
										sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
										sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
										sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

										SqlList.add(sqlinfoEgde);
										messageRepush.IsCreateEdge = true;
									} else {
										Logs.LogErrorRequireFix("edge", e);
										SqlInfo sqlinfoEgde2 = new SqlInfo();
										sqlinfoEgde2.Params = new HashMap<String, Object>();
										sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde2.Params.put("edge", e);
										sqlinfoEgde2.Params.put("fromtbl", "product");
										sqlinfoEgde2.Params.put("fromcol", "productid");
										sqlinfoEgde2.Params.put("from", productid);

										sqlinfoEgde2.Params.put("totbl", "product_gallery");
										sqlinfoEgde2.Params.put("tocol", "pictureid");
										sqlinfoEgde2.Params.put("to", recordValue);
										SqlList.add(sqlinfoEgde2);
										messageRepush.IsCreateEdge = true;
									}

								}
								// e_product_slider
								// e_slider_slidervideo
								// e_slidervideo_video

								if (table.equals("product_slider")) {
									// double productid = reader.getDouble("productid");
									String productid = Utils.GetNumberStringDouble(reader.getDouble("productid"));
									String e = "e_product_slider";
									Logs.Log(isLog, strNote, "create edge " + e);
									EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
											.findFirst().orElse(null);
									if (edgeInfo != null) {
										SqlInfo sqlinfoEgde = new SqlInfo();
										sqlinfoEgde.Params = new HashMap<String, Object>();
										sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde.Params.put("edge", edgeInfo.edge);
										sqlinfoEgde.Params.put("from", productid);
										sqlinfoEgde.Params.put("to", recordValue);

										sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
										sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
										sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
										sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

										SqlList.add(sqlinfoEgde);
										messageRepush.IsCreateEdge = true;
									} else {
										Logs.LogErrorRequireFix("edge", e);
										SqlInfo sqlinfoEgde2 = new SqlInfo();
										sqlinfoEgde2.Params = new HashMap<String, Object>();
										sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde2.Params.put("edge", e);
										sqlinfoEgde2.Params.put("fromtbl", "product");
										sqlinfoEgde2.Params.put("fromcol", "productid");
										sqlinfoEgde2.Params.put("from", productid);

										sqlinfoEgde2.Params.put("totbl", "product_slider");
										sqlinfoEgde2.Params.put("tocol", "sliderid");
										sqlinfoEgde2.Params.put("to", recordValue);
										SqlList.add(sqlinfoEgde2);
										messageRepush.IsCreateEdge = true;
									}

								}

								if (table.equals("html_info_lang")) {
									// double htmlid = reader.getDouble("htmlid");
									String htmlid = Utils.GetNumberStringDouble(reader.getDouble("htmlid"));
									String e = "e_htmlinfo_lang";
									Logs.Log(isLog, strNote, "create edge " + e);
									EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
											.findFirst().orElse(null);
									if (edgeInfo != null) {
										SqlInfo sqlinfoEgde = new SqlInfo();
										sqlinfoEgde.Params = new HashMap<String, Object>();
										sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde.Params.put("edge", edgeInfo.edge);
										sqlinfoEgde.Params.put("from", htmlid);
										sqlinfoEgde.Params.put("to", recordValue);

										sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
										sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
										sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
										sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

										SqlList.add(sqlinfoEgde);
										messageRepush.IsCreateEdge = true;
									} else {
										Logs.LogErrorRequireFix("edge", e);
										SqlInfo sqlinfoEgde2 = new SqlInfo();
										sqlinfoEgde2.Params = new HashMap<String, Object>();
										sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde2.Params.put("edge", e);
										sqlinfoEgde2.Params.put("fromtbl", "html_info");
										sqlinfoEgde2.Params.put("fromcol", "htmlid");
										sqlinfoEgde2.Params.put("from", htmlid);

										sqlinfoEgde2.Params.put("totbl", "html_info_lang");
										sqlinfoEgde2.Params.put("tocol", "recordid");
										sqlinfoEgde2.Params.put("to", recordValue);
										SqlList.add(sqlinfoEgde2);
										messageRepush.IsCreateEdge = true;
									}
								}
								if (table.equals("product_gallery_360")) {
									// double productid = reader.getDouble("productid");
									String productid = Utils.GetNumberStringDouble(reader.getDouble("productid"));

									String e = "e_productgallery_360";
									Logs.Log(isLog, strNote, "create edge " + e);
									EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
											.findFirst().orElse(null);
									if (edgeInfo != null) {
										SqlInfo sqlinfoEgde = new SqlInfo();
										sqlinfoEgde.Params = new HashMap<String, Object>();
										sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde.Params.put("edge", edgeInfo.edge);
										sqlinfoEgde.Params.put("from", productid);
										sqlinfoEgde.Params.put("to", recordValue);

										sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
										sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
										sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
										sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

										SqlList.add(sqlinfoEgde);
										messageRepush.IsCreateEdge = true;
									} else {
										Logs.LogErrorRequireFix("edge", e);
										SqlInfo sqlinfoEgde2 = new SqlInfo();
										sqlinfoEgde2.Params = new HashMap<String, Object>();
										sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
										sqlinfoEgde2.Params.put("edge", e);
										sqlinfoEgde2.Params.put("fromtbl", "product");
										sqlinfoEgde2.Params.put("fromcol", "productid");
										sqlinfoEgde2.Params.put("from", productid);

										sqlinfoEgde2.Params.put("totbl", "product_gallery_360");
										sqlinfoEgde2.Params.put("tocol", "pictureid");
										sqlinfoEgde2.Params.put("to", recordValue);
										SqlList.add(sqlinfoEgde2);
										messageRepush.IsCreateEdge = true;
									}

								}

								if (table.equals("product_slider_videoslide")) {
									double sliderid = reader.getDouble("sliderid");

									if (sliderid > 0) {
										String e = "e_slider_slidervideo";
										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", Utils.GetNumberStringDouble(sliderid));
											sqlinfoEgde.Params.put("to", recordValue);

											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e);
											SqlInfo sqlinfoEgde2 = new SqlInfo();
											sqlinfoEgde2.Params = new HashMap<String, Object>();
											sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde2.Params.put("edge", e);
											sqlinfoEgde2.Params.put("fromtbl", "product_slider");
											sqlinfoEgde2.Params.put("fromcol", "sliderid");
											sqlinfoEgde2.Params.put("from", Utils.GetNumberStringDouble(sliderid));

											sqlinfoEgde2.Params.put("totbl", "product_slider_videoslide");
											sqlinfoEgde2.Params.put("tocol", "recordid");
											sqlinfoEgde2.Params.put("to", recordValue);
											SqlList.add(sqlinfoEgde2);
										}
									}
									double videoid = reader.getDouble("videoid");
									if (videoid > 0) {
										String e = "e_slidervideo_video";
										Logs.Log(isLog, strNote, "create edge " + e);
										EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
												.findFirst().orElse(null);
										if (edgeInfo != null) {
											SqlInfo sqlinfoEgde = new SqlInfo();
											sqlinfoEgde.Params = new HashMap<String, Object>();
											sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde.Params.put("edge", edgeInfo.edge);
											sqlinfoEgde.Params.put("from", recordValue);
											sqlinfoEgde.Params.put("to", videoid);

											sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
											sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
											sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
											sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

											SqlList.add(sqlinfoEgde);
											messageRepush.IsCreateEdge = true;
										} else {
											Logs.LogErrorRequireFix("edge", e);
											SqlInfo sqlinfoEgde3 = new SqlInfo();
											sqlinfoEgde3.Params = new HashMap<String, Object>();
											sqlinfoEgde3.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
											sqlinfoEgde3.Params.put("edge", e);
											sqlinfoEgde3.Params.put("fromtbl", "product_slider_videoslide");
											sqlinfoEgde3.Params.put("fromcol", "recordid");
											sqlinfoEgde3.Params.put("from", recordValue);

											sqlinfoEgde3.Params.put("totbl", "product_videoslide");
											sqlinfoEgde3.Params.put("tocol", "videoid");
											sqlinfoEgde3.Params.put("to", videoid);
											SqlList.add(sqlinfoEgde3);
											messageRepush.IsCreateEdge = true;
										}
									}
								}

								if (table.equals("product_category_quicklink"))
									quicklinkedge: {
										if (Strings.isNullOrEmpty(Identify))
											break quicklinkedge;
										String[] arr = Identify.split("\\|");
										if (arr.length < 5)
											break quicklinkedge;
										double caterid = Utils.toDouble(arr[4]);
										if (caterid > 0) {
											String e = "e_category_quicklink";
											Logs.Log(isLog, strNote, "create edge " + e);
											EdgeInfo edgeInfo = tableInfo.edges.stream().filter(x -> x.edge.equals(e))
													.findFirst().orElse(null);
											if (edgeInfo != null) {
												SqlInfo sqlinfoEgde = new SqlInfo();
												sqlinfoEgde.Params = new HashMap<String, Object>();
												sqlinfoEgde.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
												sqlinfoEgde.Params.put("edge", edgeInfo.edge);
												sqlinfoEgde.Params.put("from", Utils.GetNumberStringDouble(caterid));
												sqlinfoEgde.Params.put("to", recordValue);

												sqlinfoEgde.Params.put("fromtbl", edgeInfo.fromtable);
												sqlinfoEgde.Params.put("totbl", edgeInfo.totable);
												sqlinfoEgde.Params.put("fromcol", edgeInfo.fromcol);
												sqlinfoEgde.Params.put("tocol", edgeInfo.tocol);

												SqlList.add(sqlinfoEgde);
												messageRepush.IsCreateEdge = true;

											} else {
												Logs.LogErrorRequireFix("edge", e);

												SqlInfo sqlinfoEgde2 = new SqlInfo();
												sqlinfoEgde2.Params = new HashMap<String, Object>();
												sqlinfoEgde2.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
												sqlinfoEgde2.Params.put("edge", e);
												sqlinfoEgde2.Params.put("fromtbl", "product_category_lang");
												sqlinfoEgde2.Params.put("fromcol", "reccordid");
												sqlinfoEgde2.Params.put("from", Utils.GetNumberStringDouble(caterid));

												sqlinfoEgde2.Params.put("totbl", "product_category_quicklink");
												sqlinfoEgde2.Params.put("tocol", "linkid");
												sqlinfoEgde2.Params.put("to", recordValue);
												SqlList.add(sqlinfoEgde2);
												messageRepush.IsCreateEdge = true;
											}
										}
									}

							} catch (Throwable exx) {
								exx.printStackTrace();
								Logs.Log(isLog, strNote, exx.getMessage());
								Logs.WriteLine(exx.getMessage());
								FileHelper.AppendAllText("sysmessage.txt", exx.toString());
								FileHelper.AppendAllText("valueeror.txt", "table: " + table + "-identify: " + Identify
										+ "-recordName: " + recordName + "-recordValue: " + recordValue);
								throw exx;
							}
							// orientclient2.Close();
						}

						// seourl

						if (table.equals("product_category_lang")) {
							int cateid = Utils.toInt(reader.getString("categoryid"));
							if (cateid > 0) {
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
						}

						if (table.equals("product_manu_lang")) {
							int manufacturerid = Utils.toInt(reader.getString("manufacturerid"));
							if (manufacturerid > 0) {
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
						}

						if (table.equals("product_propvalue_lang")) {
							String valueid = reader.getString("valueid");
							int siteid = Utils.toInt(reader.getString("siteid"));
							String languageid = reader.getString("languageid");
							if (siteid > 0 && !Utils.StringIsEmpty(languageid)) {
								SqlInfo sqlinfoEgde1 = new SqlInfo();
								sqlinfoEgde1.Params = new HashMap<String, Object>();
								sqlinfoEgde1.Type = SqlInfoType.REQUIRE_RUN_SEOURL_PROP_VALUE;
								sqlinfoEgde1.Params.put("siteid", siteid);
								sqlinfoEgde1.Params.put("languageid", languageid);
								sqlinfoEgde1.Params.put("objectid", valueid);
								SqlList.add(sqlinfoEgde1);
								messageRepush.IsCreateEdge = true;
							}
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

	public boolean CheckData(Map<String, Object> paraJson, Map<String, Object> paracu) {
		String fromtbl = (String) paraJson.get("fromtbl");
		String fromcol = (String) paraJson.get("fromcol");

		String totbl = (String) paraJson.get("totbl");
		String tocol = (String) paraJson.get("tocol");

		String fromtbl2 = (String) paracu.get("fromtbl");
		String fromcol2 = (String) paracu.get("fromcol");

		String totbl2 = (String) paracu.get("totbl");
		String tocol2 = (String) paracu.get("tocol");

		if (!fromtbl.equals(fromtbl2))
			return false;
		if (!fromcol.equals(fromcol2))
			return false;
		if (!totbl.equals(totbl2))
			return false;
		if (!tocol.equals(tocol2))
			return false;

		return true;
	}

	public void Run() throws Exception {
		ResultSet rs = null;

		// String udb = "tgdd_news";
		// String passdb = "44662288";
		// String rabitUrl = "amqp://beta:beta@10.1.6.139:5672";

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
			String message = "Injection:" + Utils.GetCurrentDate() + ":" + this.InjectProgramName + ":Lastid =0??? in " + InetAddress.getLocalHost().getHostName();
			lineNotify.NotifyError(message);

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
			for (int j = 0; j < 100000000; j++) {
				Logs.WriteLine("GetSyncMsgFromDb ");
				// long ToID = tempLastID + 1000;
				// new MyThreadSysData("data", "DGRAPH_SYNC_MESSAGE_SEL");
//				new MyThreadSysData("dataprice", "DGRAPH_SYNC_MESSAGE_PRICE_SEL");
//				new MyThreadSysData("datastock", "DGRAPH_SYNC_MESSAGE_SELSTOCK");
//				new MyThreadSysData("datastockbhx", "DGRAPH_SYNC_SELSTOCKBHX");
//				new MyThreadSysData("data-reinit", "DGRAPH_SYNC_REPUSH_SEL");
				String sql = "";
				Statement csSQL = null;
				CallableStatement csStore = null;
				// int fetchSize = 500;
				int pageSize = 15000;
				var t1 = System.currentTimeMillis();
				boolean printlog = false;
				if (InjectProgramName.equals("data")) {
					printlog = true;

				}
				if (InjectProgramName.equals("data") || InjectProgramName.equals("data-pm_currentinstock")
						|| InjectProgramName.equals("data-pm_saleorder_locking")
						|| InjectProgramName.equals("data-crm_productlocking") || InjectProgramName.equals("data-news")
						|| InjectProgramName.equals("data-pm_store")
						|| InjectProgramName.equals("data-accessory_product_tmp")
						|| InjectProgramName.equals("data-gen_store_ward_distance")) {

					sql = "SELECT     0 as datacenter ,ID,TABLENAME,ACTION,IDENTIFY,CREATEDDATE,CHANGEDVERSION,ClassName,SiteID,NOTE, \r\n"
							+ "      LANGUAGEID \r\n" + "    FROM  DGRAPH_SYNC_MESSAGE where ID > " + tempLastID
							+ " AND ID <=  " + (tempLastID + pageSize) + "\r\n" + "    Order by ID ASC ";
					if (tempLastID > 1933428181) {
						sql = "SELECT\r\n" + "  *\r\n" + "FROM\r\n" + "(\r\n" + "  SELECT\r\n" + "    ROWNUM STT,\r\n"
								+ "    datacenter ,ID,TABLENAME,ACTION,IDENTIFY,CREATEDDATE,CHANGEDVERSION,ClassName,SiteID,NOTE, \r\n"
								+ "    LANGUAGEID\r\n" + "  FROM\r\n" + "  (\r\n" + "    SELECT \r\n"
								+ "      0 as datacenter ,ID,TABLENAME,ACTION,IDENTIFY,CREATEDDATE,CHANGEDVERSION,ClassName,SiteID,NOTE, \r\n"
								+ "      LANGUAGEID \r\n" + "    FROM  DGRAPH_SYNC_MESSAGE where ID > " + tempLastID
								+ "\r\n" + "    Order by ID ASC\r\n" + "  )\r\n" + ")\r\n" + "WHERE STT < " + pageSize
								+ " ORDER BY id";

					}

					csSQL = condb.createStatement();
					csSQL.setFetchSize(pageSize);
					rs = csSQL.executeQuery(sql);
//					 if (printlog)
//						System.out.println( InjectProgramName+"::::" +sql);
//					csStore = condb.prepareCall("{call " + this.SyncMessageStore + "(?,?,?)}");
//					csStore.registerOutParameter(1, OracleTypes.CURSOR);
//					csStore.setLong(2, tempLastID);
//					csStore.setInt(3, pageSize);
//					csStore.setFetchSize(pageSize);
//					csStore.execute();
//					rs = (ResultSet) csStore.getObject(1);

				} else if (InjectProgramName.equals("data-reinit")) {

 				sql = "SELECT     0 as datacenter ,ID,TABLENAME,ACTION,IDENTIFY,CREATEDDATE,CHANGEDVERSION,ClassName,SiteID,NOTE, \r\n"
 							+ "      LANGUAGEID \r\n" + "    FROM  DGRAPH_SYNC_MESSAGE_REPUSH where ID > " + tempLastID
 							+ " AND ID <=  " + (tempLastID + pageSize) + "\r\n" + "    Order by ID ASC ";
 				if (tempLastID > 135045627) {
					sql = "SELECT\r\n" + "  *\r\n" + "FROM\r\n" + "(\r\n" + "  SELECT\r\n" + "    ROWNUM STT,\r\n"
							+ "    datacenter ,ID,TABLENAME,ACTION,IDENTIFY,CREATEDDATE,CHANGEDVERSION,ClassName,SiteID,NOTE, \r\n"
							+ "    LANGUAGEID\r\n" + "  FROM\r\n" + "  (\r\n" + "    SELECT \r\n"
							+ "      0 as datacenter ,ID,TABLENAME,ACTION,IDENTIFY,CREATEDDATE,CHANGEDVERSION,ClassName,SiteID,NOTE, \r\n"
							+ "      LANGUAGEID \r\n" + "    FROM  DGRAPH_SYNC_MESSAGE_REPUSH where ID > " + tempLastID
							+ "\r\n" + "    Order by ID ASC\r\n" + "  )\r\n" + ")\r\n" + "WHERE STT < " + pageSize
							+ " ORDER BY id";

					  }

					csSQL = condb.createStatement();
					csSQL.setFetchSize(pageSize);
					rs = csSQL.executeQuery(sql);
					// if (printlog)
					// System.out.println(sql);

				} else {

					csStore = condb.prepareCall("{call " + this.SyncMessageStore + "(?,?,?)}");
					csStore.registerOutParameter(1, OracleTypes.CURSOR);
					csStore.setLong(2, tempLastID);
					csStore.setInt(3, pageSize);
					csStore.setFetchSize(pageSize);
					csStore.execute();
					rs = (ResultSet) csStore.getObject(1);
				}

				var t2 = System.currentTimeMillis() - t1;
				if (printlog)
					System.out.println(t2);
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
				// /app/spark/tmp
				int size = rs.getFetchSize();
//				Logs.WriteLine("GetSyncMsgFromDb:" + this.InjectProgramName + ":" + size + ",tempLastID:" + tempLastID);
//
				if (printlog)
					System.out.println(
							"GetSyncMsgFromDb:" + this.InjectProgramName + ":" + size + ",tempLastID:" + tempLastID);
				int dem = 0;
				if (rs != null) {

					while (rs.next()) {
						dem++;
						size++;
						// Logs.WriteLine("\r\npush" + size );
						String strIdentify = rs.getString("IDENTIFY");
						String strACTION = rs.getString("ACTION");
						String TABLENAME = rs.getString("TABLENAME");
						if (TABLENAME.equals("pricedefault")) {

						}

						String strNOTE = rs.getString("NOTE") + "";

						if (strACTION.equals("insert"))
							strACTION = "Add";
						if (strACTION.equals("update"))
							strACTION = "Update";
						if (strACTION.equals("delete"))
							strACTION = "Delete";
						boolean isLogAll = false;
						boolean isLog = false;
						if (strNOTE.contains("DIDX_LOG")) {

							isLog = true;
						}
						if (strNOTE.contains("WEBCAP.DGRAPH_PRODUCT_UPD")) {

							isLogAll = true;
						}

						if (TABLENAME.contains("pm_currentinstock")) {
							isLog = true;

						}

						if (TABLENAME.contains("product_detail") && !strNOTE.contains("DIDX_LOG")) {
							// strNOTE="DIDX_LOG|product_detail";
							// isLog = true;

						}

//						product_prop
//						product_prop_lang
//						product_propvalue
//						product_propvalue_lang

//						if ((TABLENAME.equals("product_prop") || TABLENAME.equals("product_prop_lang")
//								|| TABLENAME.equals("product_propvalue") || TABLENAME.equals("product_propvalue_lang"))
//								&& !strNOTE.contains("DIDX_LOG")
//
//						) {
//							strNOTE = "DIDX_LOG|" + TABLENAME;
//							isLog = true;
//
//						}
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
						// System.out.println(SyncTableName+" ss "+TABLENAME );
						// rieng
						if (SyncTableName.length() > 0 && !TABLENAME.equals(SyncTableName)

						) {

							if (ID > tempLastID) {
								tempLastID = ID;

							}
							// System.out.println(SyncTableName+" skip "+TABLENAME );
							continue;
						}
						// chung bo 2 cai nay
						if (InjectProgramName.equals("data") && SyncTableName.length() <= 0 && TABLENAME.length() > 0) {
							if (TABLENAME.equals("pm_saleorder_locking") || TABLENAME.equals("pm_currentinstock")
									|| TABLENAME.equals("crm_productlocking") || TABLENAME.equals("news")
									|| TABLENAME.equals("pm_store") || TABLENAME.equals("accessory_product_tmp")
									|| TABLENAME.equals("gen_store_ward_distance")) {

								if (ID > tempLastID) {
									tempLastID = ID;

								}
								// System.out.println("skipall "+TABLENAME);
								continue;
							}

						}

//						if ( // TABLENAME.equals("gen_store_ward_distance") || 
//								
//								TABLENAME.equals("product_old_detail") || TABLENAME.equals("pm_currentinstock_bhx")
//
//						) {
//
//							if (ID > tempLastID) {
//								tempLastID = ID;
//
//							}
//							// System.out.println("skip pm_currentinstock_bhx");
//							continue;
//						}
//
//						// news|newsid|816129|categoryid,viewcounter,
//						if (TABLENAME.equals("news") && strIdentify.contains("|categoryid,viewcounter,")
//
//						) {
//							if (ID > tempLastID) {
//								tempLastID = ID;
//
//							}
//							// System.out.println("skipviewcounter");
//							continue;
//						}
//						if (TABLENAME.equals("product") && strIdentify.contains("|categoryid,totalreview")
//
//						) {
//							if (ID > tempLastID) {
//								tempLastID = ID;
//
//							}
//							// System.out.println("skipviewcounter");
//							continue;
//						}
//						if (TABLENAME.equals("product") && strIdentify.contains("|totalreview")
//
//						) {
//							if (ID > tempLastID) {
//								tempLastID = ID;
//
//							}
//							// System.out.println("skipviewcounter");
//							continue;
//						}

//						if(TABLENAME.equals("pm_currentinstock") || TABLENAME.equals("pm_currentinstock_bhx"))
//						 {
//							if (ID > tempLastID) {
//								tempLastID = ID;
//
//							}
//							continue;
//						 }

//						if (classname.equals("ms.productse.ProductSE")) {
//							strNOTE="DIDX_LOG|productse";
//							isLog = true;
//
//						}

						Date date = new Date(rs.getTimestamp("CREATEDDATE").getTime());
						String LangID = rs.getString("LanguageID");
						if (Utils.StringIsEmpty(LangID))
							LangID = "vi-VN";
						boolean logit = false;
						String logfile = "log";
						boolean logRecheck = false;
						String logRecheckfile = "log";

						Logs.Log(isLog, strNOTE, "inject " + strIdentify + " ID:" + ID);

						if (TABLENAME.contains("product_prop") || TABLENAME.contains("banners")
								|| TABLENAME.equals("pm_currentinstock") || TABLENAME.equals("pm_product")
								|| TABLENAME.equals("product") || TABLENAME.equals("product_language")

						) {
							if (!strIdentify.endsWith("totalreview,")) {
								Logs.LogSysMessage("logmsg-" + TABLENAME,
										strIdentify + "|" + classname + "|" + siteID + "|" + strACTION);
							}
						}

						if (isLogAll) {

							Logs.LogSysMessage("pushall",
									Utils.GetCurrentDate() + ":" + strIdentify + " ID:" + ID + ":" + strACTION);

						}
						if (classname.equals("ms.price.Status") || classname.equals("ms.price.Price")
								|| classname.equals("ms.promotion.Promotion")
								|| classname.equals("ms.promotion.PromotionByCode")
								|| classname.equals("ms.promotion.PromotionGroup")
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
							message.Source = "INJECT 1";
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
									message.Hash = provinceID;
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
									message.Hash = provinceID;
									queue = "gr.dc4.didx.codestock";
									queuebk = "gr.dc2.didx.codestock";
									queueBeta = "gr.beta.didx.codestock";
									isSend = true;

								} else {
									isSend = false;

								}
//								Logs.Log(isLog, "DIDX_LOG|codestock",
//										strIdentify + "," + messIdentifyArr.length + " isSend=" + isSend);

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
									message.Hash = productID;
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
								// service 1 ben send qua 2 ben
								datacenter = 4;
								if (strNOTE.contains("MSGBUS_ERPNOTIFY_RCV")) {
									logRecheck = true;
									logRecheckfile = "message-price";
								}
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
								datacenter = 4;
								if (strNOTE.contains("MSGBUS_ERPNOTIFY_RCV")) {
									logRecheck = true;
									// String timeStr=Utils.GetCurrentDate().toString();
									logRecheckfile = "message-promotion";
								}
							}
//							if (classname.equals("ms.promotion.PromotionByCode")) {
//								queue = "gr.dc4.didx.promotionbycode";
//								queuebk = "gr.dc2.didx.promotionbycode";
//								queueBeta = "gr.beta.didx.promotionbycode";
//								logit = true;
//								logfile = "promotionbycode";
//								isSend = true;
//								datacenter = 4;
//								if (strNOTE.contains("MSGBUS_ERPNOTIFY_RCV")) {
//									logRecheck = true;
//									// String timeStr=Utils.GetCurrentDate().toString();
//									logRecheckfile = "message-promotion-bycode";
//								}
//							}
//							if (classname.equals("ms.promotion.PromotionGroup")) {
//								queue = "gr.dc4.didx.promotiongroup";
//								queuebk = "gr.dc2.didx.promotiongroup";
//								queueBeta = "gr.beta.didx.promotiongroup";
//								logit = true;
//								logfile = "promotiongroup";
//								isSend = true;
//								datacenter = 4;
//								if (strNOTE.contains("MSGBUS_ERPNOTIFY_RCV")) {
//									logRecheck = true;
//									// String timeStr=Utils.GetCurrentDate().toString();
//									logRecheckfile = "message-promotion-group";
//								}
//							}

							if (classname.equals("ms.productold.Promotion")) {
								queue = "gr.dc4.didx.productoldpromotion";
								queuebk = "gr.dc2.didx.productoldpromotion";
								queueBeta = "gr.beta.didx.productoldpromotion";
								logit = true;
								logfile = "productoldpromotion";
								isSend = true;
							}
							if (classname.equals("ms.productse.ProductSE")) {
								int h = Utils.GetQueueNum(message.Hash);
								queue = "gr.dc4.didx.product" + h;
								queuebk = "gr.dc2.didx.product" + h;
								queueBeta = "gr.beta.didx.product";
								isSend = true;
							}

							if (isSend == true && !Utils.StringIsEmpty(queue)) {
								for (int c = 0; c < 100000000; c++) {
									try {
										message.ID = ID;
										boolean rsPush = QueueHelper.Current(rabitUrl).PushAll(queue, queue, queuebk,
												queueBeta, message, isLog, strNOTE, datacenter);

										if (logit) {
											Logs.LogSysMessage(logfile,
													Utils.GetCurrentDate() + ":push " + queue + " LangID: " + LangID
															+ ", site: " + siteID + ", Identify: " + strIdentify
															+ ", NOTE:" + strNOTE + ",classname:" + classname + ",date:"
															+ date.toString() + ",dc" + datacenter);
										}

										Logs.Log(isLog, message.Note,
												"apush " + queue + ",NOTE:" + strNOTE + "classname:" + classname
														+ "Identify: " + strIdentify + " LangID: " + LangID + ", site: "
														+ siteID + ", dc: " + datacenter);

										if (logRecheck) {
											Logs.getInstance().LogSysMessage(logRecheckfile, message);
											queue = "gr.didx.notify";
											queuebk = "gr.didx.notify";
											queueBeta = "gr.didx.notify";
											message.Note = logRecheckfile;
											message.CreatedDate = Utils.GetCurrentDate();
											boolean rsPushBK = QueueHelper.Current(rabitUrl).PushAll(queue, queue,
													queuebk, queueBeta, message, false, "", 4);// send 4 thoi

										}

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
								String qu = "gr.dc4.sql.sysdata0";
								String qu2 = "gr.dc4.sql.sysdata0";
								String qubk = "gr.dc2.sql.sysdata0";
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
								String qu = "gr.dc4.sql.sysdata0";
								String qu2 = "gr.dc4.sql.sysdata0";
								String qubk = "gr.dc2.sql.sysdata0";
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
								String qu = "gr.dc4.sql.sysdata0";
								String qu2 = "gr.dc4.sql.sysdata0";
								String qubk = "gr.dc2.sql.sysdata0";
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
								messageRepush.Hash = Utils.toInt(strIdentify);
								messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_PRODUCTSE_FROM_INJECT;
								QueueHelper.Current(rabitUrl).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,
										strNOTE, datacenter);
								// Logs.Log(isLog, strNOTE, "Upsert " + strIdentify + " ID:" + ID);
							} else if (classname.equals("ms.cook.CookSE")) {

								MessageQueue messageRepush = new MessageQueue();
								String qu = "gr.dc4.sql.sysdata0";
								String qu2 = "gr.dc4.sql.sysdata0";
								String qubk = "gr.dc2.sql.sysdata0";
								String qudev = "gr.beta.sql.sysdata";
								messageRepush.Action = DataAction.Update;
								messageRepush.ClassName = "mwg.wb.pkg.upsert";
								messageRepush.CreatedDate = date;
								messageRepush.Note = strNOTE;
								messageRepush.Lang = LangID;
								messageRepush.Identify = strIdentify;
								messageRepush.Hash = Utils.toInt(strIdentify);
								messageRepush.SiteID = siteID;
								messageRepush.DataCenter = datacenter;
								messageRepush.ID = ID;
								messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_COOKSE_FROM_INJECT;
								QueueHelper.Current(rabitUrl).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,
										strNOTE, datacenter);
								// Logs.Log(isLog, strNOTE, "Upsert " + strIdentify + " ID:" + ID);
							} else if (classname.equals("ms.common.SuggestSearch")) {

								MessageQueue messageRepush = new MessageQueue();
								String qu = "gr.dc4.sql.sysdata0";
								String qu2 = "gr.dc4.sql.sysdata0";
								String qubk = "gr.dc2.sql.sysdata0";
								String qudev = "gr.beta.sql.sysdata";
								messageRepush.Action = DataAction.Update;
								messageRepush.ClassName = "mwg.wb.pkg.upsert";
								messageRepush.RepushQueue = "didx.common";
								messageRepush.RepushClassName = "mwg.wb.pkg.common.SuggestSearch";
								messageRepush.CreatedDate = date;
								messageRepush.Note = strNOTE;
								messageRepush.Lang = LangID;
								messageRepush.Identify = strIdentify;
								messageRepush.Hash = Utils.toInt(strIdentify);
								messageRepush.SiteID = siteID;
								messageRepush.ID = ID;
								messageRepush.DataCenter = datacenter;
								messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_COMMON;// REQUIRE_PUSH_RUN_SUGGESTSEARCH_FROM_INJECT;
								QueueHelper.Current(rabitUrl).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,
										strNOTE, datacenter);

							} else {
								// Logs.WriteLine("Upsert " + strIdentify + " ID:" + ID);
								// Logs.Log(isLog, strNOTE, "Upsert " + strIdentify + " ID:" + ID);
								MessageQueue messageRepush = new MessageQueue();
//product_pricerange|rangeid|30
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
//									if (table.equals("product_language")) {
//										Logs.Log(true, "DIDX_LOG|inject_product_lang",
//												strIdentify + " action:" + strACTION);
//									}
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
									long recordHash = Utils.toLong(recordValue);
									if (recordValue.contains(",")) {
										push = false;
									}
//									if (table.contains("product_pricerange")) {
//										push = false;
//									}
									
									if (push) {
										try {
											messageRepush.SqlList = BuildSqlList(messageRepush, strIdentify, table,
													recordName, recordValue, otherCol, action, condb, strNOTE, isLog,
													messageRepush);
											messageRepush.Hash = recordHash;

										} catch (Throwable er
												
												) {
											Logs.LogException(er);
											
										//	141057292|product_pricerange|rangeid|30|ms.product.ProductData|1|Add
											Logs.LogException(ID + " ,  " + strIdentify + " ,  " + classname + " ,  " + siteID
													+ "   , " + strACTION);

											String message = "DC" + config.DATACENTER + ":" + this.InjectProgramName
													+ ":BANH BONG:" + Utils.GetCurrentDate() + ":"
													+ Utils.stackTraceToString(er);
											lineNotify.NotifyInfo(message);
											if(er.getMessage().contains("java.lang.IllegalArgumentException: Invalid Input Number")) { 
												push=false;
											}else {
											
											  throw er;
											}
										}

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

//											Date dateC = new Date();
//											Calendar calendar = Calendar.getInstance(); // calendar instance
//											calendar.setTime(dateC);
//											int d = calendar.get(Calendar.HOUR_OF_DAY);
//
//											if (d > 19 && d <= 24 || d < 6) {
//
//												int idx = (int) (rid % queueBigData.length);
//												qu = queueBigData[idx].replace("gr.dc2.", "gr.dc4.");
//												qu2 = qu;
//												qubk = queueBigData[idx];
//												qudev = "gr.beta.sql.stock";
//												messageRepush.Note = "BIGDATA";// strNOTE;
//											} else {

											int h = Utils.GetQueueNum5(recordHash);
											qu = "gr.dc4.sql.stock" + h;
											qu2 = "gr.dc4.sql.stock" + h;
											qubk = "gr.dc2.sql.stock" + h;
											qudev = "gr.beta.sql.stock";
											// }

											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.CreatedDate = date;
											messageRepush.DataCenter = datacenter;
											messageRepush.Note = strNOTE;
											messageRepush.Hash = recordHash;
											messageRepush.Source = "INJECT";
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_STOCK;

										} else if (table.equals("pm_currentinstock_bhx")) {

											qu = "gr.dc4.sql.stockbhx";
											qu2 = "gr.dc4.sql.stockbhx";
											qubk = "gr.dc2.sql.stockbhx";
											qudev = "gr.beta.sql.stockbhx";
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.CreatedDate = date;
											messageRepush.DataCenter = datacenter;
											messageRepush.Source = "INJECT BHX";
											messageRepush.Hash = recordHash;
											messageRepush.Note = strNOTE;
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_STOCK_BHX;

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

											qu = "gr.dc4.sql.sysdata0";
											qu2 = "gr.dc4.sql.sysdata0";
											qubk = "gr.dc2.sql.sysdata0";
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
											qu = "gr.dc4.sql.sysdata0";
											qu2 = "gr.dc4.sql.sysdata0";
											qubk = "gr.dc2.sql.sysdata0";
											qudev = "gr.beta.sql.sysdata";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											if (messageRepush.CategoryID == 8232 || messageRepush.CategoryID == 8233) {
												messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_GAMEAPP_VIEWCOUNT;
											}

											// Logs.LogSysMessage("totalreview",
											// Utils.GetCurrentDate() + ":" + strIdentify + " ID:" + ID + ":"
											// + strACTION + " cate " + messageRepush.CategoryID);

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
										} else if(table.equals("keyword_suggest_news")){
											qu = "gr.dc4.sql.sysnews";
											qu2 = "gr.dc4.sql.sysnews";
											qubk = "gr.dc2.sql.sysnews";
											qudev = "gr.beta.sql.sysnews";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.Identify = recordValue;
											messageRepush.RepushClassName = "mwg.wb.pkg.news.News";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											messageRepush.ID = ID;
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_KEYWORDSUGGESTNEW_FROM_INJECT;

										} else if (table.equals("product_gallery")) {
											qu = "gr.dc4.sql.sysdata0";
											qu2 = "gr.dc4.sql.sysdata0";
											qubk = "gr.dc2.sql.sysdata0";
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
											qu = "gr.dc4.sql.sysdata0";
											qu2 = "gr.dc4.sql.sysdata0";
											qubk = "gr.dc2.sql.sysdata0";
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
										} else if (table.equals("pm_store")) {
											qu = "gr.dc4.sql.sysdata0";
											qu2 = "gr.dc4.sql.sysdata0";
											qubk = "gr.dc2.sql.sysdata0";
											qudev = "gr.beta.sql.sysdata";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.Identify = recordValue;// bang khoa chinh luon
											messageRepush.Source = table;
											messageRepush.RepushClassName = "mwg.wb.pkg.common.StoreSE";
											messageRepush.RepushQueue = "didx.common";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_COMMON;
											messageRepush.ID = ID;
										} else if (table.equals("url_google")) {
											qu = "gr.dc4.sql.sysdata0";
											qu2 = "gr.dc4.sql.sysdata0";
											qubk = "gr.dc2.sql.sysdata0";
											qudev = "gr.beta.sql.sysdata";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.Identify = recordValue;// bang khoa chinh luon
											messageRepush.Source = table;
											messageRepush.RepushClassName = "mwg.wb.pkg.common.UrlGoogle";
											messageRepush.RepushQueue = "didx.common";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_COMMON;
											messageRepush.ID = ID;
										} else if (table.equals("product_label_campaign")) {
											qu = "gr.dc4.sql.sysdata0";
											qu2 = "gr.dc4.sql.sysdata0";
											qubk = "gr.dc2.sql.sysdata0";
											qudev = "gr.beta.sql.sysdata";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.Identify = recordValue;// bang khoa chinh luon
											messageRepush.Source = table;
											messageRepush.RepushClassName = "mwg.wb.pkg.common.LabelCampaign";
											messageRepush.RepushQueue = "didx.common";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_COMMON;
											messageRepush.ID = ID;
										} else if (table.equals("product_detail") && strNOTE.contains("20210516")) {
											qu = "gr.dc4.sql.sysdata10";
											qu2 = "gr.dc4.sql.sysdata10";
											qubk = "gr.dc2.sql.sysdata10";
											qudev = "gr.beta.sql.sysdata10";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											messageRepush.ID = ID;
										} else if (table.equals("product_timerseo")) {
											qu = "gr.dc4.sql.sysdata0";
											qu2 = "gr.dc4.sql.sysdata0";
											qubk = "gr.dc2.sql.sysdata0";
											qudev = "gr.beta.sql.sysdata";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.Identify = recordValue;// bang khoa chinh luon
											messageRepush.Source = table;
											messageRepush.RepushClassName = "mwg.wb.pkg.common.TimerSeoSE";
											messageRepush.RepushQueue = "didx.common";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_COMMON;
											messageRepush.ID = ID;
										}

										else {
//											if (messageRepush.IsCreateEdge == true) {
//												qu = "gr.dc4.sql.sysdataedge";
//												qu2 = "gr.dc4.sql.sysdataedge";
//												qubk = "gr.dc2.sql.sysdataedge";
//												qudev = "gr.beta.sql.sysdataedge";
//												messageRepush.DataCenter = datacenter;
//												messageRepush.Action = DataAction.Update;
//												messageRepush.ClassName = "mwg.wb.pkg.upsert";
//												messageRepush.CreatedDate = date;
//												messageRepush.Note = strNOTE;
//												messageRepush.Type = 0;
//												messageRepush.ID = ID;
//											} else {
											qu = "gr.dc4.sql.sysdata0";
											qu2 = "gr.dc4.sql.sysdata0";
											qubk = "gr.dc2.sql.sysdata0";
											qudev = "gr.beta.sql.sysdata";
											messageRepush.DataCenter = datacenter;
											messageRepush.Action = DataAction.Update;
											messageRepush.ClassName = "mwg.wb.pkg.upsert";
											messageRepush.CreatedDate = date;
											messageRepush.Note = strNOTE;
											messageRepush.Type = 0;
											messageRepush.ID = ID;
											// }
										}
										if (table.equals("pm_product")) {

											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_INJECT;
											messageRepush.Source = "pm_product";
										}
										if (table.equals("pm_product_productstatus")) {

											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_PRODUCTSTATUS_FROM_INJECT;
											messageRepush.Source = "pm_product_productstatus";
										}

										if (table.equals("pm_specialsaleprogram_product")) {

											messageRepush.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_SPECIALSALEPROGRAM_FROM_INJECT;
											messageRepush.Source = "pm_specialsaleprogram_product";
										}

										messageRepush.Source = table;
										if (messageRepush.PushType == PushType.Currentinstock
												&& messageRepush.BrandID == 3) {
											// bo qua

										} else {
											messageRepush.ID = ID;

											if (qu.endsWith("sql.sysdata0")) {
												// cho vao mot queue, trnah ong tao truoc , ong tạo sau
												if (action == DataAction.Add) {
													qu = "gr.dc4.sql.sysdata0";
													qu2 = "gr.dc4.sql.sysdata0";
													qubk = "gr.dc2.sql.sysdata0";
													qudev = "gr.beta.sql.sysdata";
												} else { // chi danh cho update
													int hsa = Utils.GetQueueNum(recordHash);
													qu = "gr.dc4.sql.sysdata" + hsa;
													qu2 = "gr.dc4.sql.sysdata" + hsa;
													qubk = "gr.dc2.sql.sysdata" + hsa;
													qudev = "gr.beta.sql.sysdata";

												}

											}
											if (SyncQueue.length() > 0) {
												qu = "gr.dc4.sql.sysdat." + SyncQueue;
												qu2 = "gr.dc4.sql.sysdata." + SyncQueue;
												qubk = "gr.dc2.sql.sysdata." + SyncQueue;
												qudev = "gr.beta.sql.sysdata." + SyncQueue;

											}

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
													if (table.equals("pm_product")) {
//truogn hop thay doi pm_product anh huogn stock, KM,price,codestock, phai repush lai 
//ko push chỗ này dc, vì chưa update data trên graph, phải gắn vào type
														if (Utils.toLong(messageRepush.RefIdentify) > 0) {
															messageRepush.Type = 0;// MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_INJECT;
															messageRepush.Source = "pm_product";
															String queue = "gr.repushall";
															String queuebk = "gr.repushall";
															String queueBeta = "gr.repushall";
															messageRepush.Note = "PUSHALL";// chua productid
															messageRepush.Identify = messageRepush.RefIdentify;
															messageRepush.CreatedDate = Utils.GetCurrentDate();

//															messageRepush.SiteID = DidxHelper
//																	.getSitebyBrandID(messageRepush.BrandID);
//															messageRepush.Lang = DidxHelper
//																	.getLangByBrandID(messageRepush.BrandID);

//															boolean rsPushBK = QueueHelper.Current(rabitUrl).PushAll(
//																	queue, queue, queuebk, queueBeta, messageRepush,
//																	false, "", 4);// send 4 thoi
//															Logs.getInstance().Log(isLog, strNOTE, "repush-pm-product",
//																	messageRepush);
														}
													}

													Logs.getInstance().Log(isLog, strNOTE, "injectpush" + table,
															messageRepush);

													Logs.Log(isLog, strNOTE,

															Utils.GetCurrentDate() + ":push " + qu + " LangID: "
																	+ LangID + ", site: " + siteID + ", Identify: "
																	+ strIdentify + ", NOTE:" + strNOTE + ",classname:"
																	+ classname + ",date:" + date.toString() + ",dc"
																	+ datacenter);

													if (action == DataAction.Update) {
														Hasrocessed.put(key1, System.currentTimeMillis());
													}
													break;
												} catch (Throwable e) {
													Logs.LogException(e);
													String errorMessage = Logs.GetStacktrace(e);
													Logs.WriteLine("\r\npush fail " + qu);
													e.printStackTrace();
													if (c % 5 == 0) {
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
							// System.out.println( InjectProgramName +",ID = "+ID);
						}
						// System.out.println( ID);
					} // while
						// NoTX.database().declareIntent(null);

					LastId = tempLastID + "";
					// System.out.println(InjectProgramName + ",tempLastID = " + LastId);
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
			// if (ErrorCount % 5 == 0 && ErrorCount > 2) {
			String message = "DC" + config.DATACENTER + ":" + this.InjectProgramName + ":BANH BONG:"
					+ Utils.GetCurrentDate() + ":" + Utils.stackTraceToString(exx);
			lineNotify.NotifyInfo(message);
			// }
			Thread.sleep(15000 * ErrorCount);
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

}
