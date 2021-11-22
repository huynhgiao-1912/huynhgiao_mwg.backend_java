package mwg.wb.client.graph;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientechnologies.common.concur.ONeedRetryException;
import com.orientechnologies.orient.client.remote.OStorageRemote;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import mwg.wb.client.CachedGraphDBHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.MyException;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;
import org.apache.tinkerpop.gremlin.orientdb.executor.OGremlinResultSet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ORThreadLocal {

	private OrientGraphFactory factory;
	// ThreadLocal<OrientGraph> graphThreadLocal = new ThreadLocal<OrientGraph>();
	ThreadLocal<OrientGraph> graphThreadLocalNoTx = new ThreadLocal<OrientGraph>();
	ObjectMapper mapper = null;
	// JsonConfig jsonConfig = null;
	private String url;
	public int dataCenter = 0;
	String user = "";
	String password = "";
	public boolean IsWorker = false;
	public int PoolNumber = 30;
	public int KEY_CACHED_TIME = 5;

	// Logs logs=null;
	public ORThreadLocal() throws JsonParseException, IOException {
		// jsonConfig = new JsonConfig();
		// OGlobalConfiguration.SQL_GRAPH_CONSISTENCY_MODE.setValue("notx_sync_repair");
		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		factory.declareIntent(new OIntentMassiveInsert());
//		factory.setThreadMode(THREAD_MODE.MANUAL);
//		factory.setAutoStartTx(false);
//		factory.setKeepInMemoryReferences(false);
//		factory.setRequireTransaction(false);
//		factory.setUseLog(false);
//		 factory.setThreadMode(THREAD_MODE.ALWAYS_AUTOSET);
//		OGlobalConfiguration.DB_POOL_MAX.setValue(20);
//		OGlobalConfiguration.tx

	}
	// static final String[] lshotWrite= new String[] { "172.16.3.71",
	// "172.16.3.73", "172.16.3.75" };
	// static final String[] lshotRead= new String[] { "172.16.3.72",
	// "172.16.3.74","172.16.3.76" };

	// static int port = 24242;
	// public OrientGraph orientGraphNoTx = null;

	public void InitNoTx() {
		// orientGraphNoTx = factory.getNoTx();
	}

	public void initRead(ClientConfig config, int type, int poolNumber) {
		dataCenter = config.DATACENTER;
		PoolNumber = poolNumber;
		// logs=new Logs(config.DATACENTER,this.IsWorker);
		String SERVER_ORIENTDB_READ_USER = config.SERVER_ORIENTDB_READ_USER;
		String SERVER_ORIENTDB_READ_PASS = config.SERVER_ORIENTDB_READ_PASS;

		String SERVER_ORIENTDB_READ_URL1 = "";
		String SERVER_ORIENTDB_READ_URL2 = "";
		String SERVER_ORIENTDB_READ_URL3 = "";
		String SERVER_ORIENTDB_READ_URL4 = "";
		String SERVER_ORIENTDB_READ_URL5 = "";
		String SERVER_ORIENTDB_READ_URL6 = "";
		user = SERVER_ORIENTDB_READ_USER;
		password = SERVER_ORIENTDB_READ_PASS;
		// OStorageRemote.CONNECTION_STRATEGY strategy =
		// OStorageRemote.CONNECTION_STRATEGY.ROUND_ROBIN_REQUEST;
		// OGlobalConfiguration.CLIENT_CONNECTION_STRATEGY.setValue(strategy);
		if (dataCenter == 4) {

			SERVER_ORIENTDB_READ_URL1 = config.SERVER_ORIENTDB_READ_URL1;
			SERVER_ORIENTDB_READ_URL2 = config.SERVER_ORIENTDB_READ_URL2;
			SERVER_ORIENTDB_READ_URL3 = config.SERVER_ORIENTDB_READ_URL3;
			SERVER_ORIENTDB_READ_URL4 = config.SERVER_ORIENTDB_READ_URL4;
			SERVER_ORIENTDB_READ_URL5 = config.SERVER_ORIENTDB_READ_URL5;
			SERVER_ORIENTDB_READ_URL6 = config.SERVER_ORIENTDB_READ_URL6;
			if (type == 0) {

				url = SERVER_ORIENTDB_READ_URL1;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 1) {

				url = SERVER_ORIENTDB_READ_URL2;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 2) {

				url = SERVER_ORIENTDB_READ_URL3;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 3) {

				url = SERVER_ORIENTDB_READ_URL4;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 4) {

				url = SERVER_ORIENTDB_READ_URL5;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 5) {

				url = SERVER_ORIENTDB_READ_URL6;
				factory = new OrientGraphFactory(url, user, password);

			}
		}

		if (dataCenter == 2) {
			SERVER_ORIENTDB_READ_URL1 = config.SERVER_ORIENTDB_READ_URL1;
			SERVER_ORIENTDB_READ_URL2 = config.SERVER_ORIENTDB_READ_URL2;
			SERVER_ORIENTDB_READ_URL3 = config.SERVER_ORIENTDB_READ_URL3;
			SERVER_ORIENTDB_READ_URL4 = config.SERVER_ORIENTDB_READ_URL4;
			SERVER_ORIENTDB_READ_URL5 = config.SERVER_ORIENTDB_READ_URL5;
			SERVER_ORIENTDB_READ_URL6 = config.SERVER_ORIENTDB_READ_URL6;

			if (type == 0) {

				url = SERVER_ORIENTDB_READ_URL1;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 1) {

				url = SERVER_ORIENTDB_READ_URL2;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 2) {

				url = SERVER_ORIENTDB_READ_URL3;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 3) {

				url = SERVER_ORIENTDB_READ_URL4;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 4) {

				url = SERVER_ORIENTDB_READ_URL5;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 5) {

				url = SERVER_ORIENTDB_READ_URL6;
				factory = new OrientGraphFactory(url, user, password);

			}
		}

		if (dataCenter == 3) {

			SERVER_ORIENTDB_READ_URL1 = config.SERVER_ORIENTDB_READ_URL1;
			OStorageRemote.CONNECTION_STRATEGY strategy = OStorageRemote.CONNECTION_STRATEGY.ROUND_ROBIN_REQUEST;
			OGlobalConfiguration.CLIENT_CONNECTION_STRATEGY.setValue(strategy);
			url = SERVER_ORIENTDB_READ_URL1;

			factory = new OrientGraphFactory(url, user, password);
		}
		factory.setupPool(PoolNumber);

	}

	public void initRead(ClientConfig config, String Url, int poolNumber) {

		OGlobalConfiguration.CLIENT_CHANNEL_MAX_POOL.setValue(poolNumber);
		OGlobalConfiguration.CLIENT_CONNECT_POOL_WAIT_TIMEOUT.setValue(180000);
		OGlobalConfiguration.NETWORK_BINARY_MAX_CONTENT_LENGTH.setValue(32768);

		dataCenter = config.DATACENTER;
		PoolNumber = poolNumber;
		String SERVER_ORIENTDB_READ_USER = config.SERVER_ORIENTDB_READ_USER;
		String SERVER_ORIENTDB_READ_PASS = config.SERVER_ORIENTDB_READ_PASS;

		user = SERVER_ORIENTDB_READ_USER;
		password = SERVER_ORIENTDB_READ_PASS;

		url = Url;
		factory = new OrientGraphFactory(url, user, password);
		factory.setupPool(poolNumber);

	}

	public void initReadRoundRobin(ClientConfig config, int type, int poolNumber) {
		dataCenter = config.DATACENTER;
		PoolNumber = poolNumber;
		String SERVER_ORIENTDB_READ_USER = config.SERVER_ORIENTDB_READ_USER;
		String SERVER_ORIENTDB_READ_PASS = config.SERVER_ORIENTDB_READ_PASS;

		String SERVER_ORIENTDB_READ_URL1 = "";
		String SERVER_ORIENTDB_READ_URL2 = "";
		String SERVER_ORIENTDB_READ_URL3 = "";
		user = SERVER_ORIENTDB_READ_USER;
		password = SERVER_ORIENTDB_READ_PASS;
		OStorageRemote.CONNECTION_STRATEGY strategy = OStorageRemote.CONNECTION_STRATEGY.ROUND_ROBIN_REQUEST;
		OGlobalConfiguration.CLIENT_CONNECTION_STRATEGY.setValue(strategy);

		OGlobalConfiguration.CLIENT_CHANNEL_MAX_POOL.setValue(poolNumber);
		OGlobalConfiguration.CLIENT_CONNECT_POOL_WAIT_TIMEOUT.setValue(180000);
		OGlobalConfiguration.NETWORK_BINARY_MAX_CONTENT_LENGTH.setValue(32768);

		if (dataCenter == 4) {

			SERVER_ORIENTDB_READ_URL1 = config.SERVER_ORIENTDB_READ_URL1;
			SERVER_ORIENTDB_READ_URL2 = config.SERVER_ORIENTDB_READ_URL2;
			SERVER_ORIENTDB_READ_URL3 = config.SERVER_ORIENTDB_READ_URL3;

			if (type == 0) {

				url = SERVER_ORIENTDB_READ_URL1;
				factory = new OrientGraphFactory(url, user, password);

			} else if (type == 1) {

				url = SERVER_ORIENTDB_READ_URL2;
				factory = new OrientGraphFactory(url, user, password);

			} else if (type == 2) {

				url = SERVER_ORIENTDB_READ_URL3;
				factory = new OrientGraphFactory(url, user, password);

			} else {

				url = SERVER_ORIENTDB_READ_URL1;
				factory = new OrientGraphFactory(url, user, password);

			}
		}

		if (dataCenter == 2) {
			SERVER_ORIENTDB_READ_URL1 = config.SERVER_ORIENTDB_READ_URL1;
			SERVER_ORIENTDB_READ_URL2 = config.SERVER_ORIENTDB_READ_URL2;
			SERVER_ORIENTDB_READ_URL3 = config.SERVER_ORIENTDB_READ_URL3;

			if (type == 0) {

				url = SERVER_ORIENTDB_READ_URL1;
				factory = new OrientGraphFactory(url, user, password);

			} else if (type == 1) {

				url = SERVER_ORIENTDB_READ_URL2;
				factory = new OrientGraphFactory(url, user, password);

			} else if (type == 2) {

				url = SERVER_ORIENTDB_READ_URL3;
				factory = new OrientGraphFactory(url, user, password);

			} else {

				url = SERVER_ORIENTDB_READ_URL1;
				factory = new OrientGraphFactory(url, user, password);

			}
		}

		if (dataCenter == 3) {

			SERVER_ORIENTDB_READ_URL1 = config.SERVER_ORIENTDB_READ_URL1;

			url = SERVER_ORIENTDB_READ_URL1;

			factory = new OrientGraphFactory(url, user, password);

		}
		factory.setupPool(PoolNumber);

	}

	public void initReadRoundRobinV2(ClientConfig config, int type, int poolNumber) {
		dataCenter = config.DATACENTER;
		PoolNumber = poolNumber;
		String SERVER_ORIENTDB_READ_USER = config.SERVER_ORIENTDB_READ_USER;
		String SERVER_ORIENTDB_READ_PASS = config.SERVER_ORIENTDB_READ_PASS;

		String SERVER_ORIENTDB_READ_URL1 = "";
		String SERVER_ORIENTDB_READ_URL2 = "";
		String SERVER_ORIENTDB_READ_URL3 = "";
		user = SERVER_ORIENTDB_READ_USER;
		password = SERVER_ORIENTDB_READ_PASS;
		// OStorageRemote.CONNECTION_STRATEGY strategy =
		// OStorageRemote.PARAM_CONNECTION_STRATEGY.CONNECTION_STRATEGY.ROUND_ROBIN_REQUEST;
		// OGlobalConfiguration.CLIENT_CONNECTION_STRATEGY.setValue(strategy);

		// OrientDBConfig.builder().addConfig(OGlobalConfiguration.CLIENT_CONNECTION_STRATEGY,
		// "ROUND_ROBIN_CONNECT").build());

		SERVER_ORIENTDB_READ_URL1 = config.SERVER_ORIENTDB_READ_URL1;

		url = SERVER_ORIENTDB_READ_URL1;
		factory = new OrientGraphFactory(url, user, password);

		factory.setupPool(PoolNumber);

	}

	public void initWriteRoundRobinV2(ClientConfig config, int type, int poolNumber) {
		dataCenter = config.DATACENTER;
		PoolNumber = poolNumber;
		// OGlobalConfiguration.RID_BAG_EMBEDDED_TO_SBTREEBONSAI_THRESHOLD.setValue(-1);

		String SERVER_ORIENTDB_WRITE_USER = config.SERVER_ORIENTDB_WRITE_USER;
		String SERVER_ORIENTDB_WRITE_PASS = config.SERVER_ORIENTDB_WRITE_PASS;
		String SERVER_ORIENTDB_WRITE_URL1 = "";
		String SERVER_ORIENTDB_WRITE_URL2 = "";
		String SERVER_ORIENTDB_WRITE_URL3 = "";
		String SERVER_ORIENTDB_WRITE_URL4 = "";
		String SERVER_ORIENTDB_WRITE_URL5 = "";
		String SERVER_ORIENTDB_WRITE_URL6 = "";
		user = SERVER_ORIENTDB_WRITE_USER;
		password = SERVER_ORIENTDB_WRITE_PASS;

		SERVER_ORIENTDB_WRITE_URL1 = config.SERVER_ORIENTDB_WRITE_URL1;
		SERVER_ORIENTDB_WRITE_URL2 = config.SERVER_ORIENTDB_WRITE_URL2;
		SERVER_ORIENTDB_WRITE_URL3 = config.SERVER_ORIENTDB_WRITE_URL3;
		SERVER_ORIENTDB_WRITE_URL4 = config.SERVER_ORIENTDB_WRITE_URL4;
		SERVER_ORIENTDB_WRITE_URL5 = config.SERVER_ORIENTDB_WRITE_URL5;
		SERVER_ORIENTDB_WRITE_URL6 = config.SERVER_ORIENTDB_WRITE_URL6;

		SERVER_ORIENTDB_WRITE_URL1 = config.SERVER_ORIENTDB_WRITE_URL1;
		url = SERVER_ORIENTDB_WRITE_URL1;

		// OStorageRemote.CONNECTION_STRATEGY strategy =
		// OStorageRemote.CONNECTION_STRATEGY.ROUND_ROBIN_REQUEST;
		// OGlobalConfiguration.CLIENT_CONNECTION_STRATEGY.setValue(strategy);

		factory = new OrientGraphFactory(url, user, password);

		factory.setupPool(PoolNumber);

	}

	public void initReadAPI(ClientConfig centralConfig, int type) {
		dataCenter = centralConfig.DATACENTER;
		user = centralConfig.API_SERVER_ORIENTDB_READ_USER;
		password = centralConfig.API_SERVER_ORIENTDB_READ_PASS;
		url = centralConfig.API_SERVER_ORIENTDB_READ_URL;
		OStorageRemote.CONNECTION_STRATEGY strategy = OStorageRemote.CONNECTION_STRATEGY.ROUND_ROBIN_REQUEST;
		OGlobalConfiguration.CLIENT_CONNECTION_STRATEGY.setValue(strategy);

		OGlobalConfiguration.CLIENT_CHANNEL_MAX_POOL.setValue(20000);
		OGlobalConfiguration.CLIENT_CONNECT_POOL_WAIT_TIMEOUT.setValue(180000);
		OGlobalConfiguration.NETWORK_BINARY_MAX_CONTENT_LENGTH.setValue(32768);

		factory = new OrientGraphFactory(url, user, password);
		factory.setupPool(20000);
	}

	public void initWrite(ClientConfig config, int type, int poolNumber) {
		PoolNumber = poolNumber;
		dataCenter = config.DATACENTER;
		System.out.println("dataCenter:" + dataCenter);
		String SERVER_ORIENTDB_WRITE_USER = config.SERVER_ORIENTDB_WRITE_USER;
		String SERVER_ORIENTDB_WRITE_PASS = config.SERVER_ORIENTDB_WRITE_PASS;
		String SERVER_ORIENTDB_WRITE_URL1 = "";
		String SERVER_ORIENTDB_WRITE_URL2 = "";
		String SERVER_ORIENTDB_WRITE_URL3 = "";
		String SERVER_ORIENTDB_WRITE_URL4 = "";
		String SERVER_ORIENTDB_WRITE_URL5 = "";
		String SERVER_ORIENTDB_WRITE_URL6 = "";
		user = SERVER_ORIENTDB_WRITE_USER;
		password = SERVER_ORIENTDB_WRITE_PASS;
		if (dataCenter == 4) {

			SERVER_ORIENTDB_WRITE_URL1 = config.SERVER_ORIENTDB_WRITE_URL1;
			SERVER_ORIENTDB_WRITE_URL2 = config.SERVER_ORIENTDB_WRITE_URL2;
			SERVER_ORIENTDB_WRITE_URL3 = config.SERVER_ORIENTDB_WRITE_URL3;
			SERVER_ORIENTDB_WRITE_URL4 = config.SERVER_ORIENTDB_WRITE_URL4;
			SERVER_ORIENTDB_WRITE_URL5 = config.SERVER_ORIENTDB_WRITE_URL5;
			SERVER_ORIENTDB_WRITE_URL6 = config.SERVER_ORIENTDB_WRITE_URL6;

			if (type == 0) {

				url = SERVER_ORIENTDB_WRITE_URL1;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 1) {

				url = SERVER_ORIENTDB_WRITE_URL2;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 2) {

				url = SERVER_ORIENTDB_WRITE_URL3;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 3) {

				url = SERVER_ORIENTDB_WRITE_URL4;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 4) {

				url = SERVER_ORIENTDB_WRITE_URL5;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 5) {

				url = SERVER_ORIENTDB_WRITE_URL6;
				factory = new OrientGraphFactory(url, user, password);

			}
		}

		if (dataCenter == 2) {

			SERVER_ORIENTDB_WRITE_URL1 = config.SERVER_ORIENTDB_WRITE_URL1;
			SERVER_ORIENTDB_WRITE_URL2 = config.SERVER_ORIENTDB_WRITE_URL2;
			SERVER_ORIENTDB_WRITE_URL3 = config.SERVER_ORIENTDB_WRITE_URL3;
			SERVER_ORIENTDB_WRITE_URL4 = config.SERVER_ORIENTDB_WRITE_URL4;
			SERVER_ORIENTDB_WRITE_URL5 = config.SERVER_ORIENTDB_WRITE_URL5;
			SERVER_ORIENTDB_WRITE_URL6 = config.SERVER_ORIENTDB_WRITE_URL6;

			if (type == 0) {

				url = SERVER_ORIENTDB_WRITE_URL1;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 1) {

				url = SERVER_ORIENTDB_WRITE_URL2;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 2) {

				url = SERVER_ORIENTDB_WRITE_URL3;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 3) {

				url = SERVER_ORIENTDB_WRITE_URL4;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 4) {

				url = SERVER_ORIENTDB_WRITE_URL5;
				factory = new OrientGraphFactory(url, user, password);

			}
			if (type == 5) {

				url = SERVER_ORIENTDB_WRITE_URL6;
				factory = new OrientGraphFactory(url, user, password);

			}
		}

		if (dataCenter == 3) {
			SERVER_ORIENTDB_WRITE_URL1 = config.SERVER_ORIENTDB_WRITE_URL1;
			// OStorageRemote.CONNECTION_STRATEGY strategy =
			// OStorageRemote.CONNECTION_STRATEGY.ROUND_ROBIN_REQUEST;
			// OGlobalConfiguration.CLIENT_CONNECTION_STRATEGY.setValue(strategy);
			// SERVER_ORIENTDB_WRITE_URL1 = config.SERVER_ORIENTDB_WRITE_URL1;

			url = SERVER_ORIENTDB_WRITE_URL1;
			factory = new OrientGraphFactory(url, user, password);
			// System.out.println(url);
		}
		factory.setupPool(poolNumber);
	}

	public void reInit() {

		if (factory != null) {

			if (factory.isOpen())
				factory.close();
			factory = null;
		}
		Logs.LogExceptionExit("reInit(): PoolNumber=" + PoolNumber);
		factory = new OrientGraphFactory(url, user, password);
		factory.setupPool(PoolNumber);
	}

//	public OrientGraph getGraph() {
//
//		OrientGraph graph = graphThreadLocal.get();
//		if (graph == null) {
//			graph = factory.getTx();
//			graphThreadLocal.set(graph);
//		}
//
//		return graph;
//	}

	public void CloseNoTx() {

		try {
			OrientGraph graph = graphThreadLocalNoTx.get();
			if (graph == null) {
				return;
			}

			graph.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void CloseAll() {

		try {

			if (factory == null) {
				return;
			}

			factory.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public synchronized OrientGraph getGraphNoTx() {

		OrientGraph graph = graphThreadLocalNoTx.get();
		if (graph == null) {
			graph = factory.getNoTx();
			// graph = factory.getTx();
			if (graph == null) {

				new MyException("getGraphNoTx : graph==null");
			}
			graphThreadLocalNoTx.set(graph);

		}
		// graph.makeActive();
		return graph;
	}

	public synchronized OrientGraph getGraphTx() {

		OrientGraph graph = graphThreadLocalNoTx.get();
		if (graph == null) {
			graph = factory.getTx();
			// graph = factory.getTx();
			if (graph == null) {

				new MyException("getGraphNoTx : graph==null");
			}
			graphThreadLocalNoTx.set(graph);

		}
		// graph.makeActive();
		return graph;
	}
//	public OrientGraph getGraphtNoTx() {
//		return factory.getNoTx();
//	}

//	public OrientGraph getTx() {
//		return factory.getTx();
//	}
//
//	public void unsetDb() {
//		graphThreadLocal.set(null);
//	}

	/////////////// them

	public OResultSet Query(String sql) {

		Map<String, String> params = new HashMap<String, String>();
		OrientGraph orG = factory.getNoTx();
		OResultSet ls = null;
		try {
			ls = orG.querySql(sql, params).getRawResultSet();

		} catch (Throwable ex) {
			Logs.LogException(ex + sql);
			throw ex;
		} finally {

			CloseOrientGraph(orG);
		}
		return ls;

	}

	public OResultSet QueryLOIquai(String sql) {

		// OrientGraph orG = factory.getNoTx();
		try {

			Map<String, String> params = new HashMap<String, String>();

			var t = this.getGraphNoTx().querySql(sql, params);
			if (t == null)
				return null;

			return t.getRawResultSet();
		} catch (Throwable ex) {
			Logs.LogException(ex);
			throw ex;
		} finally {

			// orG.close();
		}

	}

	public String QueryScalar(String sql, String field) {
		Map<String, String> params = new HashMap<String, String>();
		OrientGraph orG = factory.getNoTx();
		OResultSet ls = null;
		try {
			ls = orG.querySql(sql, params).getRawResultSet();
			while (ls.hasNext()) {
				var f = ls.next();
				if (f != null && f.getProperty(field) != null) {
					return f.getProperty(field).toString();
				}
				// return l;// (long) Long.parseLong( productlangRID) ; //.00
			}
		} catch (Throwable ex) {
			Logs.LogException(ex);
			throw ex;
		} finally {
			CloseResultSet(ls);
			CloseOrientGraph(orG);
		}

		return "";
	}

	public List<String> QueryRid(String sql) {
		List<String> rs = new ArrayList<String>();
		Map<String, String> params = new HashMap<String, String>();
		OrientGraph orG = factory.getNoTx();
		OResultSet ls = null;
		try {
			ls = orG.querySql(sql, params).getRawResultSet();
			while (ls.hasNext()) {
				var f = ls.next();
				if (f != null && f.getProperty("rid") != null) {
					rs.add(f.getProperty("rid").toString());
				}
				// return l;// (long) Long.parseLong( productlangRID) ; //.00
			}
		} catch (Throwable ex) {
			Logs.LogException(ex);
			throw ex;
		} finally {
			CloseResultSet(ls);
			CloseOrientGraph(orG);
		}

		return rs;
	}

	public Boolean GetPoductid(long productid) {

		Map<String, String> params = new HashMap<String, String>();
		OrientGraph orG = factory.getNoTx();
		String sql = "select productid  from  product where productid=" + productid + " and  isdeleted=0   limit 1";
		OResultSet ls = null;
		try {
			ls = orG.querySql(sql, params).getRawResultSet();
			while (ls.hasNext()) {
				var f = ls.next();
				if (f != null && f.getProperty("productid") != null) {
					return true;

				}
			}
		} catch (Throwable ex) {
			Logs.LogException(ex + sql);
			throw ex;
		} finally {
			CloseResultSet(ls);
			CloseOrientGraph(orG);
		}
		return false;
	}

	public String GetRid(String table, String keyname, String keyvalue) {
		String rs = "";
		Map<String, String> params = new HashMap<String, String>();
		OrientGraph orG = factory.getNoTx();
		String sql = "select @rid as rid from " + table + " where " + keyname + "='" + keyvalue + "'";
		OResultSet ls = null;
		try {
			ls = orG.querySql(sql, params).getRawResultSet();
			while (ls.hasNext()) {
				var f = ls.next();
				if (f != null && f.getProperty("rid") != null) {
					rs = f.getProperty("rid").toString();
					if (Utils.StringIsEmpty(rs)) {
						Logs.WriteLine(sql);
					}
				}
			}
		} catch (Throwable ex) {
			Logs.LogException(ex + sql + "/n" + " Keyname: " + keyname + " table: " + table + ": keyvalue" + keyvalue);
			throw ex;
		} finally {
			CloseResultSet(ls);
			CloseOrientGraph(orG);
		}

		return rs;
	}

	public boolean checkEdgeExist(String edge, String inout, String table, String keyname, String keyvalue) {

		List<String> rlist = QueryRid("select  " + inout + "('" + edge + "').@rid as rid    from " + table + " where "
				+ keyname + "=" + keyvalue);
		if (rlist != null && rlist.size() > 0)
			return true;
		return false;
	}

	public Date QueryScalarDate(String sql, String field) {
		Map<String, String> params = new HashMap<String, String>();

		OrientGraph orG = null;
		OResultSet ls = null;

		try {
			synchronized (ORThreadLocal.class) {
				orG = factory.getNoTx();
				ls = orG.querySql(sql, params).getRawResultSet();
			}
			while (ls.hasNext()) {
				var f = ls.next();
				if (f != null) {
					Date xx = f.getProperty(field);
					return xx;
				}
			}
		} catch (Throwable ex) {
			Logs.LogException(ex);
			throw ex;
		} finally {
			CloseResultSet(ls);
			CloseOrientGraph(orG);

		}

		return Utils.GetDefaultDate();
	}

	public OResultSet QueryFunction(String query, Map<String, Object> params) throws Throwable {

		Throwable gE = null;
		OrientGraph orG = null;
		OGremlinResultSet ORS = null;
		try {
			orG = factory.getNoTx();
			long t1 = System.currentTimeMillis();
			ORS = orG.execute("sql", query, params);
			// ORS = orG.querySql( query, params);==> sai param ham se ngum
			LogsOB(query, params, System.currentTimeMillis() - t1);
			return ORS.getRawResultSet();

			// throws OCommandSQLParsingException, OCommandExecutionException
		} catch (Throwable e) {
			// System.out.println(e);
			gE = e;
			Logs.LogException(e);
			Logs.LogException(query);

		} finally {
			CloseOGremlinResultSet(ORS);
			CloseOrientGraph(orG);

		}
		if (gE != null) {
			throw gE;
		}
		return null;

	}

	public OResultSet QueryFunctionaaa(String query, Map<String, Object> params) {

		OrientGraph orG = null;

		try {
			orG = factory.getNoTx();

			long t1 = System.currentTimeMillis();
			var g = orG.querySql(query, params);
			LogsOB(query, params, System.currentTimeMillis() - t1);

			if (g != null) {
				return g.getRawResultSet();
			}
		} catch (Throwable ex) {
			String msg = Utils.stackTraceToString(ex);
			if (msg.contains("No function with name")) {

			}
			System.out.println(msg);
			// Logs.LogException(ex);
		} finally {

			CloseOrientGraph(orG);

		}

		return null;

	}

	public void LogsOB(String sql, Map<String, Object> params, long time) {
		if (this.dataCenter == 3) {
			String f = "worker";
			if (IsWorker == false) {
				f = "api";
			}
			// if (IsWorker == true) {
			try {
				Logs.LogFactoryMessageDateXX("function-" + f, "\r\n" + sql + mapper.writeValueAsString(params));
			} catch (Throwable e) {

			}
			// }
		}
		if (time > 300) {
			if (IsWorker == true) {
				try {
					Logs.LogFactorySlowMessage("\r\n" + sql + mapper.writeValueAsString(params), time);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void Logs(String sql, Map<String, String> params, long time) {
		if (time > 300) {
			if (IsWorker == true) {
				try {
					Logs.LogFactorySlowMessage("\r\n" + sql + mapper.writeValueAsString(params), time);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

//	public <T> T SelectAs_rs(String command, Class<T> type) throws Throwable {
//		OGremlinResultSet g = null;
//		OResultSet rs = null;
//		Map<String, String> params = new HashMap<String, String>();
//		OrientGraph orG = null;
//		Throwable gE = null;
//		try {
//			orG = factory.getNoTx();
//			long t1 = System.currentTimeMillis();
//			g = orG.querySql(command, params);
//			Logs(command, params, System.currentTimeMillis() - t1);
//
//			if (g != null) {
//				rs = g.getRawResultSet();
//				if (rs != null)
//					while (rs.hasNext()) {
//						var f = rs.next();
//						if (f != null) {
//							String js = f.toJSON().replace("{\"rs\": ", "");
//							String js2 = js.substring(0, js.length() - 1);
//							return mapper.readValue(js2, type);
//						}
//					}
//			}
//		} catch (Throwable e) {
//			Logs.LogException(e);
//			gE = e;
//			// Logs.WriteLine(e);
//		} finally {
//			CloseResultSet(rs);
//			CloseGremlinResultSet(g);
//			CloseOrientGraph(orG);
//		}
//		if (gE != null) {
//			throw gE;
//		}
//		return null;
//	}

	/**
	 * select AS rs
	 * 
	 * @param <T>
	 * @param command
	 * @param type
	 * @param multiThread
	 * @return
	 * @throws Throwable
	 */
//	public <T> T SelectAs_rs(String command, Class<T> type, boolean multiThread) throws Throwable {
//
//		return SelectAs_rs(command, type);
//
//	}

//	public <T> T QueryFunction(String query, Map<String, Object> params, Class<T> type) throws Throwable {
//
//		OResultSet rs = null;
//		Throwable gE = null;
//		OrientGraph orG = null;
//		ODatabaseDocument db=null;
//		
//		 
//			orG = factory.getNoTx();
//			try {
//				try (final OGremlinResultSet result = orG.querySql(query, params) ) {
//					
//				}
//			} finally {
//				 
//			} 
//			orG.close();
//			 
//		 
//		if (gE != null) {
//			throw gE;
//		}
//		return null;
//	}

	public <T> T QueryFunction_xxxx(String query, Map<String, Object> params, Class<T> type) throws Throwable {
		OResultSet rs = null;
		Throwable gE = null;
		OrientGraph orG = null;
		OGremlinResultSet ORS = null;
		try {
			if (this.IsWorker == true) {
				orG = getGraphNoTx();// factory.getNoTx();
			} else {
				orG = factory.getNoTx();
			}
			long t1 = System.currentTimeMillis();
			ORS = orG.execute("sql", query, params);
			if (ORS != null)
				rs = ORS.getRawResultSet();
			LogsOB(query, params, System.currentTimeMillis() - t1);
			if (rs != null)
				while (rs.hasNext()) {
					var f = rs.next();
					if (f != null) {
						String js = f.toJSON().replace("{\"rs\": ", "");
						String js2 = js.substring(0, js.length() - 1);
						return mapper.readValue(js2, type);
					}
				}
			// throws OCommandSQLParsingException, OCommandExecutionException
		} catch (Throwable e) {
			// System.out.println(e);
			gE = e;

			Logs.LogExceptionParam(e, query + "\n\r" + mapper.writeValueAsString(params));

//			throw e;

		} finally {

			CloseResultSet(rs);
			if (this.IsWorker == true) {

			} else {
				CloseOrientGraph(orG);
			}
			CloseOGremlinResultSet(ORS);
		}
		if (gE != null) {
			throw gE;
		}
		return null;
	}

	public String QueryFunctionJsonString(String query, Map<String, Object> params) throws Throwable {

		OResultSet rs = null;
		Throwable gE = null;
		OrientGraph orG = null;
		OGremlinResultSet ORS = null;
		try {
			if (this.IsWorker == true) {
				orG = getGraphNoTx();// factory.getNoTx();
			} else {
				orG = factory.getNoTx();
			}
			long t1 = System.currentTimeMillis();
			ORS = orG.execute("sql", query, params);
			if (ORS != null)
				rs = ORS.getRawResultSet();
			LogsOB(query, params, System.currentTimeMillis() - t1);
			if (rs != null)
				while (rs.hasNext()) {
					var f = rs.next();
					if (f != null) {
						String js = f.toJSON().replace("{\"rs\": ", "");
						String js2 = js.substring(0, js.length() - 1);
						return js2;
					}
				}
			// throws OCommandSQLParsingException, OCommandExecutionException
		} catch (Throwable e) {
			// System.out.println(e);
			gE = e;

			Logs.LogExceptionParam(e, query + "\n\r" + mapper.writeValueAsString(params));

//			throw e;

		} finally {

			CloseResultSet(rs);
			if (this.IsWorker == true) {

			} else {
				CloseOrientGraph(orG);
			}
			CloseOGremlinResultSet(ORS);
		}
		if (gE != null) {
			throw gE;
		}
		return null;
	}

	public <T> T QueryFunction2021(int cacheTime, String query, Map<String, Object> params, Class<T> type)
			throws Throwable {

		// "select " + functionname + "(" + paramsstring + ") as rs";
		String k = Utils.MD5( query);//.replaceAll("select ", "").replaceAll(") as rs", "").replaceAll("(", "");
		for (var _element : params.entrySet()) {
			k += "_" + _element.getKey() + "_" + String.valueOf(_element.getValue());
		}

		String KEY_CACHED = k;

		if (IsWorker == false && cacheTime > 0) {
			var rsJson = CachedGraphDBHelper.GetFromCache(KEY_CACHED, cacheTime);
			if (rsJson == null || Utils.StringIsEmpty(String.valueOf(rsJson))) {
				rsJson = QueryFunctionJsonString(query, params);
				CachedGraphDBHelper.AddToCache(KEY_CACHED, rsJson);
			}

			return mapper.readValue(String.valueOf(rsJson), type);
		} else {
			String rsJson = QueryFunctionJsonString(query, params);
			return mapper.readValue(rsJson, type);
		}

	}

//public <T> T QueryFunction(String query, Map<String, Object> params, Class<T> type) {
//	return QueryFunction2021(0,   query,   params,   type);
//}
//	public <T> T QueryFunction(String query, Map<String, Object> params, Class<T> type) {
//
//		OResultSet rs = null;
//		// OGremlinResultSet rsGlem = null;
//		OrientGraph orG = null;
//		// try {
//		orG = factory.getNoTx();
//		long t1 = System.currentTimeMillis();
//		try (OGremlinResultSet rsGlem = orG.querySql(query, params)) {
//			rs = rsGlem.getRawResultSet();
//			LogsOB(query, params, System.currentTimeMillis() - t1);
//
//			if (rs != null)
//				while (rs.hasNext()) {
//					var f = rs.next();
//					if (f != null) {
//						String js = f.toJSON().replace("{\"rs\": ", "");
//						String js2 = js.substring(0, js.length() - 1);
//						try {
//							return mapper.readValue(js2, type);
//						} catch (JsonParseException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						} catch (JsonMappingException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				}
//
//		}catch(Exception x){
//			
//			
//		}
//
//		CloseResultSet(rs);
//
//		CloseOrientGraph(orG);
//
//		return null;
//	}
//	OrientGraph orG = null;
//	OGremlinResultSet ORS = null;
//	try {
//		orG = factory.getNoTx();
//		long t1 = System.currentTimeMillis();
//		ORS = orG.execute("sql", query, params); 
//		if (ORS != null)
//			rs = ORS.getRawResultSet();
	public String QueryFunctionToString(String query, Map<String, Object> params) {
		// this.getGraphNoTx()
		OResultSet rs = null;
		OrientGraph orG = null;
		try {
			orG = factory.getNoTx();

			long t1 = System.currentTimeMillis();
			rs = orG.querySql(query, params).getRawResultSet();
			LogsOB(query, params, System.currentTimeMillis() - t1);

			if (rs != null)
				while (rs.hasNext()) {
					var f = rs.next();
					if (f != null) {
						String js = f.toJSON().replace("{\"rs\": ", "");
						String js2 = js.substring(0, js.length() - 1);
						return js2;
					}
				}
		} catch (Throwable e) {
			Logs.LogException(e);
			// Logs.WriteLine(e);
		} finally {

			CloseResultSet(rs);
			CloseOrientGraph(orG);

		}

		return "";
	}

	public void CloseResultSet(OResultSet vao) {

		try {
			vao.close();
		} catch (Throwable e) {

		}

	}

	public void CloseGremlinResultSet(OGremlinResultSet vao) {

		try {
			vao.close();
		} catch (Throwable e) {

		}

	}

	public void CloseOGremlinResultSet(OGremlinResultSet vao) {

		try {
			if (vao != null)
				vao.close();
		} catch (Throwable e) {

		}

	}

	public void CloseOrientGraph(OrientGraph orG) {
		for (int i = 0; i < 1000; i++) {
			try {
				if (i > 1) {
					Utils.Sleep(500);
				}
				if (orG != null) {
					if (orG.isClosed()) {
						return;
					} else {
						// System.out.println("not isClosed");
						orG.close();
					}
				}
			} catch (Throwable e) {
				Logs.LogException(e);
			}
		}

	}

	public <T> T QueryFunction(String query, Map<String, Object> params, Class<T> type, boolean multiThread)
			throws Throwable {
		return QueryFunction(0, query, params, type, multiThread);
	}

	public <T> T QueryFunction(int timeCached, String query, Map<String, Object> params, Class<T> type,
			boolean multiThread) throws Throwable {
		if (this.IsWorker == true) {
			for (int i = 0; i < 8; i++) {
				try {
					return QueryFunction2021(0, query, params, type);
				} catch (Throwable e) {
					if (i >= 4) {
						throw e;
					} else {
						Utils.Sleep(1000);
					}
				}
			}
			return null;
		} else {
			for (int i = 0; i < 5; i++) {
				try {
					return QueryFunction2021(timeCached, query, params, type);
				} catch (Throwable e) {
					if (i >= 2) {
						throw e;
					} else {
						Utils.Sleep(500);
					}
				}
			}
			return null;
		}
	}

	public <T> T QueryFunction(String functionname, Class<T> type, boolean multithread, Object... params)
			throws Throwable {
		Map<String, Object> paramsmap = new HashMap<String, Object>();
		String paramsstring = "";
		for (int i = 0; i < params.length; i++) {
			paramsmap.put("p" + i, params[i]);
			paramsstring += ":p" + i + ",";
		}
		paramsstring = paramsstring.replaceAll(",$", "");
		String query = "select " + functionname + "(" + paramsstring + ") as rs";
		return QueryFunction(0, query, paramsmap, type, multithread);
	}

//	public static <T> T convertInstanceOfObject(Object o, Class<T> clazz) {
//		try {
//			return clazz.cast(o);
//		} catch (ClassCastException e) {
//			return null;
//		}
//	}

	public <T> T QueryFunctionCachedTime(int minit, String functionname, Class<T> type, boolean multithread,
			Object... params) throws Throwable {

		Map<String, Object> paramsmap = new HashMap<String, Object>();
		String paramsstring = "";

		for (int i = 0; i < params.length; i++) {
			paramsmap.put("p" + i, params[i]);
			paramsstring += ":p" + i + ",";

		}
		paramsstring = paramsstring.replaceAll(",$", "");
		String query = "select " + functionname + "(" + paramsstring + ") as rs";
		return QueryFunction(minit, query, paramsmap, type, multithread);

		// return QueryFunction(functionname, type, multithread, params);
	}

	public <T> T QueryFunctionCached(String functionname, Class<T> type, boolean multithread, Object... params)
			throws Throwable {

		Map<String, Object> paramsmap = new HashMap<String, Object>();
		String paramsstring = "";
		String k = "";
		for (int i = 0; i < params.length; i++) {
			paramsmap.put("p" + i, params[i]);
			paramsstring += ":p" + i + ",";
			k += "_p" + i + "" + String.valueOf(params[i]);
		}
		paramsstring = paramsstring.replaceAll(",$", "");
		String query = "select " + functionname + "(" + paramsstring + ") as rs";

		return QueryFunction(KEY_CACHED_TIME, query, paramsmap, type, multithread);

	}

	// ambiguous nen phai dat ten ham khac
	public <T> T queryFunction(String functionname, Class<T> type, Object... params) throws Throwable {
		Map<String, Object> paramsmap = new HashMap<String, Object>();
		String paramsstring = "";
		for (int i = 0; i < params.length; i++) {
			paramsmap.put("p" + i, params[i]);
			paramsstring += ":p" + i + ",";
		}
		paramsstring = paramsstring.replaceAll(",$", "");
		String query = "select " + functionname + "(" + paramsstring + ") as rs";
		return QueryFunction(0, query, paramsmap, type, false);
	}

	public <T> T queryFunctionCached(String functionname, Class<T> type, Object... params) throws Throwable {
		Map<String, Object> paramsmap = new HashMap<String, Object>();
		String paramsstring = "";
		for (int i = 0; i < params.length; i++) {
			paramsmap.put("p" + i, params[i]);
			paramsstring += ":p" + i + ",";
		}
		paramsstring = paramsstring.replaceAll(",$", "");
		String query = "select " + functionname + "(" + paramsstring + ") as rs";
		return QueryFunction(KEY_CACHED_TIME, query, paramsmap, type, false);
	}

	public <T> T queryFunctionTimeCached(int timeCachedMinute, String functionname, Class<T> type, Object... params)
			throws Throwable {
		Map<String, Object> paramsmap = new HashMap<String, Object>();
		String paramsstring = "";
		for (int i = 0; i < params.length; i++) {
			paramsmap.put("p" + i, params[i]);
			paramsstring += ":p" + i + ",";

		}

		paramsstring = paramsstring.replaceAll(",$", "");
		String query = "select " + functionname + "(" + paramsstring + ") as rs";

		return QueryFunction(timeCachedMinute, query, paramsmap, type, false);

	}
	// WRITE

	public int CommandCommitEgde(boolean isLog, String note, String sql) throws Throwable {
		// Logs.WriteLine(sql);
		Logs.Log(isLog, note, sql);
		return CommandCommitEgde(sql);
	}

	public int CommandCommitEgde(String sql) throws Throwable {
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
			} else if (rsw.contains("ODistributedRecordLockedException")) {
				return -3;// lam lai
			}

			else if (rsw.contains("OConcurrentModificationException")) {
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
				return -3;

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

	public int CommandCommit(boolean isLog, String note, String sql, Map<String, Object> params) throws Throwable {
		Logs.Log(isLog, note, sql);
		return CommandCommit(sql, params);
	}

	public int CommandCommitTran(boolean isLog, String note, String sql, Map<String, Object> params) {
		Logs.Log(isLog, note, sql);
		return CommandCommitTran(sql, params);
	}

	public int CommandCommitTran(String sql, Map<String, Object> params) {
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

		} catch (Throwable ex) {
			Logs.WriteLine("webai:" + sql + ex.getMessage());
			// throw ex;
		}
		return 0;
	}

	public int CommandCommit(String sql, Map<String, Object> params) throws Throwable {
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
			Utils.Sleep(500);
			Logs.WriteLine("webai:" + sql + ex.getMessage());
			// throw ex;
		}
		return 0;
	}

	public String UpsertODatabaseJsonEx(boolean isLog, String note, String VertexClasses, String recordName,
			String recordValue, Object obj, String ex) throws Throwable {

		Field[] fields = obj.getClass().getDeclaredFields();

		String sql = "update " + VertexClasses + " SET ";
		for (final Field _item : fields) {
			String cl = _item.getName().toLowerCase();
			if (ex.contains("," + cl + ","))
				continue;
			sql = sql + cl + "=:" + cl + ",";

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
			String recordValue, Object obj) throws Throwable {

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

//public String MassUpsertODatabaseJson(OrientGraph graphtNoTx,boolean isLog, String note, String VertexClasses, String recordName,
//			String recordValue, Object obj) throws IllegalArgumentException, IllegalAccessException {
//
//		Field[] fields = obj.getClass().getDeclaredFields();
//
//		String sql = "update " + VertexClasses + " SET ";
//		for (final Field _item : fields) {
//			String cl = _item.getName().toLowerCase();
//			sql = sql + cl + "=:" + cl + ",";
//
//		}
//		sql = StringUtils.strip(sql, ",");
//		Map<String, Object> params = new HashMap<String, Object>();
//
//		for (final Field _item : fields) {
//			Object cc = _item.get(obj);
//			String o = _item.getName().toLowerCase();
//			if (cc != null) {
//				String value = String.valueOf(cc);
//				// Logs.WriteLine(o+"="+value);
//				Type f = _item.getType();
//				if (f == Date.class) {
//					params.put(o, Utils.FormatDateForGraph((Date) cc));
//				} else if (f == boolean.class) {
//
//					params.put(o, Boolean.valueOf(value) == true ? 1 : 0);
//
//				} else if (f == byte.class) {
//					params.put(o, Byte.valueOf(value));
//				} else if (f == char.class) {
//					params.put(o, value.charAt(0));
//				} else if (f == short.class) {
//					params.put(o, Short.valueOf(value));
//				} else if (f == int.class) {
//					params.put(o, Integer.valueOf(value));
//				} else if (f == long.class) {
//					params.put(o, Long.valueOf(value));
//				} else if (f == float.class) {
//					params.put(o, Float.valueOf(value));
//				} else if (f == double.class) {
//					params.put(o, Double.valueOf(value));
//
//				} else if (f == String.class) {
//					params.put(o, String.valueOf(value));
//				} else {
//					params.put(o, String.valueOf(value));
//
//				}
//
//			} else {
//
//				params.put(o, null);
//			}
//
//		}
//		sql = sql + " Upsert where " + recordName + "='" + recordValue + "'";
//		Logs.Log(isLog, note, sql);
//		return MassCommand(graphtNoTx,sql, params);
//
//	}
//	public   String Command(String sql, Map<String, Object> params) {
//
//	 
//		// Logs.WriteLine(Url);
//		int nRetries = 1000;
//		String result = "0";
//		if (nRetries < 1) {
//			throw new IllegalArgumentException("invalid number of retries: " + nRetries);
//		}
//		OTransaction tx =  factory.getTx()..getTransaction();
//		boolean txActive = tx.isActive();
//		if (txActive) {
//			if (tx.getEntryCount() > 0) {
//				// throw new IllegalStateException( "executeWithRetry() cannot be used within a
//				// pending (dirty) transaction. Please commit or rollback before invoking it");
//				return "executeWithRetry() cannot be used within a pending (dirty) transaction. Please commit or rollback before invoking it";
//
//			}
//		}
//		if (!txActive) {
//			database.begin();
//		}
//		for (int i = 0; i < nRetries; i++) {
//			try {
//				Date st = Utils.GetCurrentDate();
//
//				database.command(sql, params);
//				database.commit();
//				result = "1";
//				long diff = Utils.GetCurrentDate().getTime() - st.getTime();
//				// Logs.WriteLine("Command:" + diff);
//				Logs.Write("." + diff);
//				break;
//			} catch (ONeedRetryException e) {
//				e.printStackTrace();
//				if (i == nRetries - 1) {
//					// throw e;
//					// database.rollback();
//					return "maxretry";
//				}
//				Logs.WriteLine("retry:" + i + sql);
//				try {
//					Thread.sleep(500);
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				database.rollback();
//				database.begin();
//			} catch (Exception e) {
//				// database.rollback();
//				result = "0";
//				// Logs.WriteLine(" sql:" + sql+e.getMessage());
//				return " sql:" + sql + e.getMessage();
//				// throw OException.wrapException(new ODatabaseException("Error during tx
//				// retry"), e);
//			}
//		}
//
//		if (txActive) {
//			database.begin();
//		}
//
//		return result;
//	}
//	public OResultSet Command(String sql, Map<String, String> params) {
//		OrientGraph orG = factory.getNoTx();
//		try {
//
//			return orG.querySql(sql, params).getRawResultSet();
//		} catch (Exception ex) {
//
//		} finally {
//			orG.close();
//		}
//		return null;
//	}
	public void Command2(String sql, Map<String, Object> params) {

		// com.orientechnologies.common.concur.lock.OLockException

		try {
			long t1 = System.currentTimeMillis();
			getGraphNoTx().executeSql(sql, params);
			// getGraphNoTx().wait(100000000);

		} catch (Throwable e) {
			e.printStackTrace();
		} finally {

		}

	}

	private final Lock queueLock = new ReentrantLock();

	public String CommandCU(String sql, Map<String, Object> params) {

		OGremlinResultSet rss = null;
		try {

			long t1 = System.currentTimeMillis();

			rss = getGraphNoTx().executeSql(sql, params);

			long ra = System.currentTimeMillis() - t1;

			if (ra > 300) {
				if (IsWorker == true) {
					String msg = sql + "\r\n" + mapper.writeValueAsString(params);
					Logs.LogFactorySlowMessage(msg, ra);
				}

			}
			return "1";

		} catch (OConcurrentModificationException cme) {

			return cme.getMessage();

		} catch (Throwable e) {

			String pram = "";
			try {
				pram = mapper.writeValueAsString(params);
			} catch (JsonProcessingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String stacktrace = e.getMessage() + "" + ExceptionUtils.getStackTrace(e) + sql + pram;

			Logs.LogException(stacktrace);
			return stacktrace;
		} finally {

			CloseOGremlinResultSet(rss);
		}

	}
//	protected Boolean execute(ODatabaseDocument db) {
//	    int maxRetries = 50;
//	    OCommandSQL command = new OCommandSQL("update "+document.getIdentity()+" "+changeCommand);
//	    int retry = 0;					
//	    while(true){
//	      try {
//	        command.execute(value);
//	        break;
//	      } catch (OConcurrentModificationException  e) {
//	        retry++;
//	        try { Thread.sleep((long) (Math.random()*150));} catch (InterruptedException e1) {}
//	        if (retry>=maxRetries){
//	          throw e;//if all retries failed
//	        }
//	      }
//	    }
//	    document.reload();
//	    return true;
//	  }
//	}.execute();

	public String Command_cuLIve(String sql, Map<String, Object> params) throws Throwable {
		OrientGraph graph = this.factory.getTx();
		int maxRetries = 10999;
		try {
			long t1 = System.currentTimeMillis();
			for (int i = 0; i < maxRetries; i++) {
				OGremlinResultSet rsG = null;
				try {
					graph.begin();
					rsG = graph.execute("sql", sql, params);
					graph.commit();
					break;
				} catch (ONeedRetryException e) {
					Utils.Sleep(3000);
					if (i == maxRetries - 1) {
						Logs.LogException((Exception) e);
						graph.rollback();
						throw e;
					}
				} catch (Throwable e) {
					Logs.LogException(e);
					graph.rollback();
					throw e;
				} finally {
					CloseGremlinResultSet(rsG);
				}
			}
			long ra = System.currentTimeMillis() - t1;
			if (ra > 300 && this.IsWorker) {
				String msg = String.valueOf(sql) + "\r\n" + this.mapper.writeValueAsString(params);
				Logs.LogFactorySlowMessage(msg, ra);
			}
			return "1";
		} catch (Throwable ez) {
			String pram = "";
			try {
				pram = this.mapper.writeValueAsString(params);
			} catch (JsonProcessingException jsonProcessingException) {
			}
			String stacktrace = String.valueOf(ez.getMessage()) + ExceptionUtils.getStackTrace(ez) + sql + pram;
			Logs.LogException(stacktrace);
			Utils.Sleep(15000);
			throw ez;
		} finally {
			graph.close();
		}
	}

	public String CommandTestMoiB(String sql, Map<String, Object> params) throws Throwable {

		// com.orientechnologies.common.concur.lock.OLockException
		OGremlinResultSet rsG = null;//
		try {
			rsG = getGraphNoTx().executeSql(sql, params);
		} finally {
			CloseGremlinResultSet(rsG);
		}

		// getGraphNoTx().wait(100000000);

		return "1";

	}

	public String CommandBash(List<SqlInfo> sqlList) throws Throwable {
		var graph = factory.getTx();
		// graph.getRawDatabase().reload();

		int maxRetries = 500;
		try {

			long t1 = System.currentTimeMillis();
			for (int i = 0; i < maxRetries; i++) {
				// System.out.println("try " + i + "" + sql);
				OGremlinResultSet rsG = null;//
				try {
					// System.out.print("0");
					graph.begin();
					// System.out.print("-1");
					for (SqlInfo sqlItem : sqlList) {
						rsG = graph.execute("sql", sqlItem.Sql, sqlItem.Params);
					}

					// System.out.print("-2");
					graph.commit();
					// System.out.print("-3");
					break;
				} catch (ONeedRetryException e) {

					// graph.begin();
					// System.out.println("try " + i + "ONeedRetryException");
					// e.printStackTrace();
//					try {
//						// graph.rollback();
//					} catch (Exception ex1) {
//						System.out.println("rollback exception! " + ex1);
//					}
					int randomNum = ThreadLocalRandom.current().nextInt(1, 200);
					Utils.Sleep(randomNum * 10);
					if (i == maxRetries - 1) {
						Logs.LogException((Exception) e);
						throw e;
					}
				} catch (Throwable e) {
					Logs.LogException(e);
					// e.printStackTrace();
//					try {
//						// graph.rollback();
//					} catch (Exception ex1) {
//						System.out.println("rollback exception! " + ex1);
//					}
					int randomNum = ThreadLocalRandom.current().nextInt(1, 500);
					Utils.Sleep(randomNum * 10);
					// Utils.Sleep(2500);
					throw e;
				} finally {
					CloseGremlinResultSet(rsG);
				}
			}

			long ra = System.currentTimeMillis() - t1;

			if (ra > 300) {
				if (IsWorker == true) {
					// String msg = sql + "\r\n" + mapper.writeValueAsString(params);
					// Logs.LogFactorySlowMessage(msg, ra);
				}

			}
			return "1";

		} catch (Throwable ez) {
			// graph.rollback();
			String pram = "";
//			try {
//				pram = mapper.writeValueAsString(params);
//			} catch (JsonProcessingException e1) {
//				// TODO Auto-generated catch block
//				// e1.printStackTrace();
//			}
			// String stacktrace = ez.getMessage() + "" + ExceptionUtils.getStackTrace(ez) +
			// sql + pram;
			// ez.printStackTrace();
			// Logs.LogException(stacktrace);
			// System.out.println("BANHHHHHHHHHH" );

			throw ez;
			// return stacktrace;
		} finally {
			// t.rollback();
			graph.close();
		}

	}

	public String Command(String sql, Map<String, Object> params) throws Throwable {
		var graph = factory.getTx();
		// graph.getRawDatabase().reload();

		int maxRetries = 500;
		try {

			long t1 = System.currentTimeMillis();
			for (int i = 0; i < maxRetries; i++) {
				// System.out.println("try " + i + "" + sql);
				OGremlinResultSet rsG = null;//
				try {
					// System.out.print("0");
					graph.begin();
					// System.out.print("-1");
					rsG = graph.execute("sql", sql, params);
					// System.out.print("-2");
					graph.commit();
					// System.out.print("-3");
					break;
				} catch (ONeedRetryException e) {

					// graph.begin();
					// System.out.println("try " + i + "ONeedRetryException");
					// e.printStackTrace();
//					try {
//						// graph.rollback();
//					} catch (Exception ex1) {
//						System.out.println("rollback exception! " + ex1);
//					}
					int randomNum = ThreadLocalRandom.current().nextInt(1, 200);
					Utils.Sleep(randomNum * 10);
					if (i == maxRetries - 1) {
						Logs.LogException((Exception) e);
						throw e;
					}
				} catch (Throwable e) {
					Logs.LogException(e + sql + "/n" + " params: " + params);
					// e.printStackTrace();
//					try {
//						// graph.rollback();
//					} catch (Exception ex1) {
//						System.out.println("rollback exception! " + ex1);
//					}
					int randomNum = ThreadLocalRandom.current().nextInt(1, 500);
					Utils.Sleep(randomNum * 10);
					// Utils.Sleep(2500);
					throw e;
				} finally {
					CloseGremlinResultSet(rsG);
				}
			}

			long ra = System.currentTimeMillis() - t1;

			if (ra > 300) {
				if (IsWorker == true) {
					String msg = sql + "\r\n" + mapper.writeValueAsString(params);
					Logs.LogFactorySlowMessage(msg, ra);
				}

			}
			return "1";

		} catch (Throwable ez) {
			// graph.rollback();
			String pram = "";
			try {
				pram = mapper.writeValueAsString(params);
			} catch (JsonProcessingException e1) {
				// TODO Auto-generated catch block
				// e1.printStackTrace();
			}
			String stacktrace = "Command:" + ez.getMessage() + "" + ExceptionUtils.getStackTrace(ez) + sql + pram;
			// ez.printStackTrace();
			Logs.LogException(stacktrace);
			// System.out.println("BANHHHHHHHHHH" );

			throw ez;
			// return stacktrace;
		} finally {
			// t.rollback();
			graph.close();
		}

	}

	public String CommandTest(String sql, Map<String, Object> params) throws Throwable {
		var graph = getGraphTx();

		int maxRetries = 10999;
		try {

			long t1 = System.currentTimeMillis();
			// var graph = getGraphTx();

			for (int i = 0; i < maxRetries; i++) {
				// System.out.println("try "+i +""+sql);
				try {
					graph.begin();
					graph.execute("sql", sql, params);
					graph.commit();
					break;
				} catch (ONeedRetryException e) {
					System.out.println("try " + i + "ONeedRetryException");
					e.printStackTrace();
					// graph.getRawDatabase().reload();;
					graph.rollback();
					Utils.Sleep(5000);

				} catch (Throwable e) {
					Logs.LogException(e);
					e.printStackTrace();
					graph.rollback();
					Utils.Sleep(5000);
				} finally {

				}
			}

			long ra = System.currentTimeMillis() - t1;

			if (ra > 300) {
				if (IsWorker == true) {
					String msg = sql + "\r\n" + mapper.writeValueAsString(params);
					Logs.LogFactorySlowMessage(msg, ra);
				}

			}
			return "1";

		} catch (Throwable ez) {

			String pram = "";
			try {
				pram = mapper.writeValueAsString(params);
			} catch (JsonProcessingException e1) {
				// TODO Auto-generated catch block
				// e1.printStackTrace();
			}
			String stacktrace = ez.getMessage() + "" + ExceptionUtils.getStackTrace(ez) + sql + pram;
			ez.printStackTrace();
			Logs.LogException(stacktrace);
			// System.out.println("BANHHHHHHHHHH" );
			Utils.Sleep(15000);
			throw ez;
			// return stacktrace;
		} finally {
			// t.rollback();
			// graph.close();
			// queueLock.unlock();
		}

	}

	public String CommandA(String sql, Map<String, Object> params) {

		// com.orientechnologies.common.concur.lock.OLockException

		for (int j = 0; j < 1000000; j++) {
			OrientGraph Tx = null;
			try {
				Tx = factory.getTx();
				Tx.begin();
				long t1 = System.currentTimeMillis();
				// Tx.executeWithRetry(5, function).executeSql(sql, params);
				Tx.executeSql(sql, params);
				Tx.commit();
				long r = (System.currentTimeMillis() - t1);
				System.out.print("." + r);
				if (r > 1000) {
					System.out.print(sql);
				}
				return "1";

			} catch (Exception e) {
				Tx.rollback();
				Logs.LogException(e);
				return e.getMessage();
			} finally {
				Tx.close();
			}
		}
		return "0";
	}

	public String getURL() {
		return url;
	}

//	public String MassCommand(OrientGraph graphtNoTx, String sql, Map<String, Object> params) {
//
//		long t1 = System.currentTimeMillis();
//		graphtNoTx.executeSql(sql, params);
//		System.out.print("." + (System.currentTimeMillis() - t1));
//		return "1";
//
//	}
}
