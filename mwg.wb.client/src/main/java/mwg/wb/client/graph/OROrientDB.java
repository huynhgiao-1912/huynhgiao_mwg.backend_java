package mwg.wb.client.graph;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientechnologies.common.concur.ONeedRetryException;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.tx.OTransaction;

import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;

public class OROrientDB {
	public String host, dbname, user, password;
	private int port;

//	
	private OrientDB orientDb;
	private ODatabaseDocument database;
	private ObjectMapper mapper = null;
	private ReentrantLock locker = new ReentrantLock();

	public OROrientDB(String host, int port, String dbname, String userName, String Password) {
		this.host = host;
		this.port = port;
		this.dbname = dbname;
		this.user = userName;
		this.password = Password;
		// Logs.WriteLine(this.host);
		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

//	public OROrientDB(int type, String dbname,String userName,String Password,int port) {
//		if (type == OrientDBType.READ) {
//			this.host = Utils.getRandomElement(lshotRead);
//		}
//		if (type == OrientDBType.WRITE) {
//
//			this.host = Utils.getRandomElement(lshotWrite);
//
//		}
//
//		this.port = port;
//		this.dbname = dbname;
//		this.user = userName;
//		this.password = Password;
//		 Logs.WriteLine(this.host );
//	}
//	public OROrientDB(int type, String[] lshotWrite,String[] lshotRead, String dbname,String userName,String Password,int port) {
//		if (type == OrientDBType.READ) {
//			this.host = Utils.getRandomElement(lshotRead);
//		}
//		if (type == OrientDBType.WRITE) {
//
//			this.host = Utils.getRandomElement(lshotWrite);
//
//		}
//
//		this.port = port;
//		this.dbname = dbname;
//		this.user = userName;
//		this.password = Password;
//		 Logs.WriteLine(this.host );
//	}

	public String GetIP() {
		return this.host;
	}

	public boolean Connect() {

		orientDb = new OrientDB("remote:" + this.host + ":" + this.port + "/" + this.dbname, this.user, this.password,
				OrientDBConfig.defaultConfig());
		database = orientDb.open(this.dbname, this.user, this.password);

		return true;
	}

//	public static String Url="remote:10.1.5.81,10.1.5.99/web";
//	public boolean ConnectUrl() {
//		//remote:{ip_1},{ip_2},{ip_3}/{database_name}
//				//String Url="remote:172.16.3.71,172.16.3.73,172.16.3.75/web";
//				 
//		 	    orientDb = new OrientDB(Url, this.user,
//		 					this.password, OrientDBConfig.defaultConfig()); 
//		 		    database = orientDb.open( this.dbname, this.user, this.password);
//
//				 Logs.WriteLine(Url);
//				return true;
//			}
	public boolean Close() {
		database.activateOnCurrentThread();
		if (!database.isClosed())
			this.database.close();
		if (this.orientDb.isOpen())
			this.orientDb.close();

		return true;
	}

	public boolean isOpen() {

		return this.orientDb.isOpen();
	}

	public synchronized String Command(String sql, Map<String, Object> params) {

		database.activateOnCurrentThread();
		// Logs.WriteLine(Url);
		int nRetries = 1000;
		String result = "0";
		if (nRetries < 1) {
			throw new IllegalArgumentException("invalid number of retries: " + nRetries);
		}
		OTransaction tx = database.getTransaction();
		boolean txActive = tx.isActive();
		if (txActive) {
			if (tx.getEntryCount() > 0) {
				// throw new IllegalStateException( "executeWithRetry() cannot be used within a
				// pending (dirty) transaction. Please commit or rollback before invoking it");
				return "executeWithRetry() cannot be used within a pending (dirty) transaction. Please commit or rollback before invoking it";

			}
		}
		if (!txActive) {
			database.begin();
		}
		for (int i = 0; i < nRetries; i++) {
			try {
				Date st = Utils.GetCurrentDate();

				database.command(sql, params);
				database.commit();
				result = "1";
				long diff = Utils.GetCurrentDate().getTime() - st.getTime();
				// Logs.WriteLine("Command:" + diff);
				Logs.Write("." + diff);
				break;
			} catch (ONeedRetryException e) {
				e.printStackTrace();
				if (i == nRetries - 1) {
					// throw e;
					// database.rollback();
					return "maxretry";
				}
				Logs.WriteLine("retry:" + i + sql);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				database.rollback();
				database.begin();
			} catch (Exception e) {
				// database.rollback();
				result = "0";
				// Logs.WriteLine(" sql:" + sql+e.getMessage());
				return " sql:" + sql + e.getMessage();
				// throw OException.wrapException(new ODatabaseException("Error during tx
				// retry"), e);
			}
		}

		if (txActive) {
			database.begin();
		}

		return result;
	}

	public String CommandA(String sql, Map<String, Object> params) {
		database.activateOnCurrentThread();
		long st = System.currentTimeMillis();
		// String r="";
		OResultSet rs = null;
		try {
			rs = database.command(sql, params);
			long diff = System.currentTimeMillis() - st;
			Logs.Write(" " + diff + " ");
			return "1";
		} finally {
			if (rs != null)
				rs.close();

		}

	}

	public String Commanda(String sql, Map<String, Object> params) {
		Date st = Utils.GetCurrentDate();

		database.activateOnCurrentThread();

		// database.begin();
		try {

			database.command(sql, params);
			database.commit();
			long diff = Utils.GetCurrentDate().getTime() - st.getTime();
			Logs.WriteLine(" Command:" + diff);
			return "1";
		} catch (Exception e) {
			// db.reload();
			database.rollback();
			Logs.WriteLine(" sql:" + sql + e.getMessage());
			return e.toString();
		} finally {

			// database.close();

		}

	}

	public OResultSet Query(String sql) {

		try {

			Map<String, String> params = new HashMap<String, String>();
			database.activateOnCurrentThread();
			return database.query(sql, params);
		} catch (Exception ex) {

		} finally {

		}
		return null;
	}

	public String QueryScalar(String sql, String field) {
		Map<String, String> params = new HashMap<String, String>();
		database.activateOnCurrentThread();
		OResultSet ls = database.query(sql, params);
		try {
			while (ls.hasNext()) {
				return ls.next().getProperty(field).toString();

				// return l;// (long) Long.parseLong( productlangRID) ; //.00
			}
		} finally {
			ls.close();
		}

		return "";
	}

	public Date QueryScalarDate(String sql, String field) {
		Map<String, String> params = new HashMap<String, String>();
		database.activateOnCurrentThread();
		OResultSet ls = database.query(sql, params);
		try {
			while (ls.hasNext()) {

				return ls.next().getProperty(field);
			}
		} finally {
			ls.close();
		}

		return Utils.GetDefaultDate();
	}

	public OResultSet QueryFunction(String query, Map<String, Object> params) {
//		OrientGraph graph = factory.getNoTx();
		database.activateOnCurrentThread();
		return database.query(query, params);
//			return graph.execute("sql", functionname, params).getRawResultSet();

	}

//	public OResultSet QueryFunctionTest() {
//		Map<String, Object> params = new HashMap<String, Object>();
//		return database.query("select product_GetByListIDSimpleV2('196963a144115',12,3,'vi-VN') as rs", params);
//		// new OCommandFunction("functionName").execute()
//		// return this.database.execute(language, script, args).command(new
//		// OCommandFunction("findSubscriptions")).execute(params);
//	}

	public <T> T SelectAs_rs(String command, Class<T> type) {
		OResultSet rs = null;
		try {
			database.activateOnCurrentThread();
			rs = database.query(command);
			if (rs != null)
				while (rs.hasNext()) {
					String js = rs.next().toJSON().replace("{\"rs\": ", "");
					String js2 = js.substring(0, js.length() - 1);
					return mapper.readValue(js2, type);
				}
		} catch (Exception e) {
			Logs.WriteLine(e.getMessage());
			Logs.WriteLine(e);
		} finally {
			if (rs != null)
				rs.close();
		}
		return null;
	}

	/**
	 * select AS rs
	 * 
	 * @param <T>
	 * @param command
	 * @param type
	 * @param multiThread
	 * @return
	 */
	public <T> T SelectAs_rs(String command, Class<T> type, boolean multiThread) {
		try {
			if (multiThread) {
				locker.lock();
				database.activateOnCurrentThread();
			}
			return SelectAs_rs(command, type);
		} catch (Exception e) {
			Logs.WriteLine(e);
		} finally {
			if (multiThread)
				locker.unlock();
		}
		return null;
	}

	public <T> T QueryFunction(String query, Map<String, Object> params, Class<T> type) {
		OResultSet rs = null;
		try {
			database.activateOnCurrentThread();
			// select product_GetByListIDSimpleV1([196963,196963],12,3,'vi-VN')
			rs = database.query(query, params);
			if (rs != null)
				while (rs.hasNext()) {
					String js = rs.next().toJSON().replace("{\"rs\": ", "");
					String js2 = js.substring(0, js.length() - 1);
//					System.out.println(js2);
					return mapper.readValue(js2, type);
				}
		} catch (Exception e) {
			Logs.WriteLine(e.getMessage());
			Logs.WriteLine(e);
		} finally {
			if (rs != null)
				rs.close();
		}
		return null;
	}

	public <T> T QueryFunction(String query, Map<String, Object> params, Class<T> type, boolean multiThread) {
		try {
			database.activateOnCurrentThread();
			if (multiThread) {
				locker.lock();
			}
			return QueryFunction(query, params, type);
		} catch (Exception e) {
			Logs.WriteLine(e);
		} finally {
			if (multiThread)
				locker.unlock();
		}
		return null;
	}

	public <T> T QueryFunction(String functionname, Class<T> type, boolean multithread, Object... params) {
		Map<String, Object> paramsmap = new HashMap<String, Object>();
		String paramsstring = "";
		for (int i = 0; i < params.length; i++) {
			paramsmap.put("p" + i, params[i]);
			paramsstring += ":p" + i + ",";
		}
		paramsstring = paramsstring.replaceAll(",$", "");
		String query = "select " + functionname + "(" + paramsstring + ") as rs";
		return QueryFunction(query, paramsmap, type, multithread);
	}

//	public <T> T QueryFunction(String functionname, Class<T> type, Object... params) {
//		return QueryFunction(functionname, type, false, params);
//	}
}
