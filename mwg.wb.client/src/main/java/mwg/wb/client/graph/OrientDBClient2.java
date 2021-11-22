package mwg.wb.client.graph;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

//import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
//import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;
//import org.apache.tinkerpop.gremlin.orientdb.executor.OGremlinResultSet;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.json.JSONException;

import mwg.wb.common.Logs;
import mwg.wb.common.Utils;

public class OrientDBClient2 extends OROrientDB {
	// live

//	static final String[] lshotWrite= new String[] { "172.16.3.71", "172.16.3.73", "172.16.3.75" };
//	static final String[] lshotRead= new String[] { "172.16.3.72", "172.16.3.74","172.16.3.76"  };

	// static final String[] lshotWrite = new String[] { "172.16.3.93" };
	// static final String[] lshotRead = new String[] { "172.16.3.93" };

	// static final String[] lshotWrite= new String[] { "172.16.3.71",
	// "172.16.3.73", "172.16.3.75" };
	// static final String[] lshotRead= new String[] { "172.16.3.72",
	// "172.16.3.74","172.16.3.76" };

	// static int port = 24242;
	static String dbname = "web";
	static String user = "root";
	static String password = "Tgdd2019#@!";

	// beta

//	static final String[] lshotWrite = new String[] { "10.1.5.87", "10.1.5.87" };
//	static final String[] lshotRead = new String[] { "10.1.5.87" };

//	static final String[] lshotWrite = new String[] { "10.1.5.87", "10.1.5.99" };
//	static final String[] lshotRead = new String[] { "10.1.5.104" };

//	static int port = 2424;
//	st	aatic String dbname = "web";
//	static String user = "root";
//	static String password = "Tgdd2012";

//	public OrientDBClient2(int type) {
//
//	//	super(type, lshotWrite, lshotRead, dbname, user, password, port);
//		// TODO Auto-generated constructor stub
//	}

	public OrientDBClient2(String host, int port) {

		super(host, port, dbname, user, password);
		// TODO Auto-generated constructor stub
	}

	public int CommandCommitEgde(boolean isLog, String note, String sql) {
		//Logs.WriteLine(sql);
		Logs.Log(isLog, note, sql);
		return CommandCommitEgde(sql);
	}

	public int CommandCommitEgde(String sql) {
		Map<String, Object> params = new HashMap<String, Object>();
		// try
		// {
		String rsw = Command(sql, params);
		if (rsw == "1") {

			return 1;

		} else {

			if (rsw.contains("maxretry")) {
				return 2;// lam lai
			} else if (rsw.contains("executeWithRetry")) {
				return 2;// lam lai
			} else if (rsw.contains("OConcurrentModificationException")) {
				return 2;// lam lai
			} else if (rsw.contains("OConcurrentModificationException")) {
				return 2;// lam lai
			} else if (rsw.contains("because the version is not the latest")) {

				return 2;
			} else if (rsw.contains("No edge has been created because ")) {
				return 1;
			}

			else if (rsw.contains("found duplicated key")) { // found duplicated key 'OCompositeKey
				return 1;
			}

			// ODistributedRecordLockedExceptionUpdate
			else if (rsw.contains("ODistributedRecordLockedException")) {
				return 0;

			} else if (rsw.contains("{\"result\":")) {
				return 1;

			} else if (rsw.contains("found duplicated key")) {
				return 1;

			} else {

				Logs.WriteLine("rsw:" + rsw + "" + sql);
				// File.AppendAllText("loiapi.txt", "\r\nrsw" + rsw + "" + sql);
				return 0;
			}

		}

//          }
//          catch (Exception ex)
//          {
//        	  Logs.WriteLine("webai:" + sql);
//             // throw  ;
//          }

	}

	public int CommandCommit(boolean isLog, String note, String sql, Map<String, Object> params) {
		Logs.Log(isLog, note, sql);
		return CommandCommit(sql, params);
	}

	public int CommandCommit(String sql, Map<String, Object> params) {
		try {

			for (int i = 0; i < 200000000; i++) {

				String rsw = Command(sql, params);
				if (rsw == "1") {

					return 1;

				} else {

					Logs.WriteLine("rsw:" + rsw + "sql=" + sql);

					if (rsw.contains("OConcurrentModificationException")) {
						// return 0;
						Logs.WriteLine("OConcurrentModificationException retry " + i);
						Thread.sleep(200);

					} else if (rsw.contains("version is not the latest")) {
						Logs.WriteLine("Thread.sleep(1000) version is not the latest retry " + i);
						Thread.sleep(1000);
						// return 2;
					} else if (rsw.contains("ORecordDuplicatedException")) {
						Logs.WriteLine(rsw);
						if (rsw.contains("Cannot index record")) {
							return 2;// lam lai
						}

						return 1;
					} else if (rsw.contains("No edge has been created because ")) {
						return 1;
					} // ODistributedRecordLockedExceptionUpdate
					else if (rsw.contains("ODistributedRecordLockedException")) {
						Logs.WriteLine("Thread.sleep(2000) ODistributedRecordLockedException retry " + i);
						Thread.sleep(2000);

					}

					else if (rsw.contains("quorum not reached")) {

						Logs.WriteLine(rsw + i);
						Thread.sleep(200);
						Logs.WriteLine("rsw:" + rsw + "" + sql);
						// File.AppendAllText("loiapi.txt", "\r\nrsw" + rsw + "" + sql);
						throw new Exception(rsw);
					}

					else {
						Logs.WriteLine(rsw + i);
						Thread.sleep(200);
						Logs.WriteLine("rsw:" + rsw + "" + sql);
						// File.AppendAllText("loiapi.txt", "\r\nrsw" + rsw + "" + sql);
						return 0;
					}

				}
			}

		} catch (Exception ex) {
			Logs.WriteLine("webai:" + sql + ex.getMessage());
			// throw ex;
		}
		return 0;
	}

	public String UpsertODatabaseJsonEx(boolean isLog, String note, String VertexClasses, String recordName,
			String recordValue, Object obj, String ex) throws IllegalArgumentException, IllegalAccessException {

		Field[] fields = obj.getClass().getDeclaredFields();

		String sql = "update " + VertexClasses + " SET ";
		for (final Field _item : fields) {
			String cl = _item.getName().toLowerCase();
			sql = sql + cl + "=:" + cl + ",";
			if (ex.contains("," +cl + ","))
				continue;

		}
		sql = StringUtils.strip(sql, ",");
		Map<String, Object> params = new HashMap<String, Object>();

		for (final Field _item : fields) {
			Object cc = _item.get(obj);
			String o = _item.getName().toLowerCase();
			if (ex.contains("," + o + ","))
				continue;
			if (cc != null) {
				String value = String.valueOf(cc);
				// Logs.WriteLine(o+"="+value);
				Type f = _item.getType();
				if (f == Date.class) {
					params.put(o, Utils.FormatDateForGraph((Date) cc));
				} else if (f == boolean.class) {

					params.put(o, Boolean.valueOf(value) == true ? 1 : 0);

				} else if (f == byte.class) {
					params.put(o, Byte.valueOf(value));
				} else if (f == char.class) {
					params.put(o, value.charAt(0));
				} else if (f == short.class) {
					params.put(o, Short.valueOf(value));
				} else if (f == int.class) {
					params.put(o, Integer.valueOf(value));
				} else if (f == long.class) {
					params.put(o, Long.valueOf(value));
				} else if (f == float.class) {
					params.put(o, Float.valueOf(value));
				} else if (f == double.class) {
					params.put(o, Double.valueOf(value));

				} else if (f == String.class) {
					params.put(o, String.valueOf(value));
				} else {
					params.put(o, String.valueOf(value));

				}

			} else {

				params.put(o, null);
			}

		}
		sql = sql + " Upsert where " + recordName + "='" + recordValue + "'";
		Logs.Log(isLog, note, sql);
		return Command(sql, params);

	}

	public String UpsertODatabaseJson(boolean isLog, String note, String VertexClasses, String recordName,
			String recordValue, Object obj) throws IllegalArgumentException, IllegalAccessException {

		Field[] fields = obj.getClass().getDeclaredFields();

		String sql = "update " + VertexClasses + " SET ";
		for (final Field _item : fields) {
			String cl = _item.getName().toLowerCase();
			sql = sql + cl + "=:" + cl + ",";

		}
		sql = StringUtils.strip(sql, ",");
		Map<String, Object> params = new HashMap<String, Object>();

		for (final Field _item : fields) {
			Object cc = _item.get(obj);
			String o = _item.getName().toLowerCase();
			if (cc != null) {
				String value = String.valueOf(cc);
				// Logs.WriteLine(o+"="+value);
				Type f = _item.getType();
				if (f == Date.class) {
					params.put(o, Utils.FormatDateForGraph((Date) cc));
				} else if (f == boolean.class) {

					params.put(o, Boolean.valueOf(value) == true ? 1 : 0);

				} else if (f == byte.class) {
					params.put(o, Byte.valueOf(value));
				} else if (f == char.class) {
					params.put(o, value.charAt(0));
				} else if (f == short.class) {
					params.put(o, Short.valueOf(value));
				} else if (f == int.class) {
					params.put(o, Integer.valueOf(value));
				} else if (f == long.class) {
					params.put(o, Long.valueOf(value));
				} else if (f == float.class) {
					params.put(o, Float.valueOf(value));
				} else if (f == double.class) {
					params.put(o, Double.valueOf(value));

				} else if (f == String.class) {
					params.put(o, String.valueOf(value));
				} else {
					params.put(o, String.valueOf(value));

				}

			} else {

				params.put(o, null);
			}

		}
		sql = sql + " Upsert where " + recordName + "='" + recordValue + "'";
		Logs.Log(isLog, note, sql);
		return Command(sql, params);

	}
}
