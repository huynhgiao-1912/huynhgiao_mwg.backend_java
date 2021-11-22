package mwg.wb.client.graph;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;
import org.apache.tinkerpop.gremlin.orientdb.executor.OGremlinResultSet;
import org.apache.tinkerpop.gremlin.structure.Graph;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientechnologies.orient.client.remote.OStorageRemote;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.ODatabase.ATTRIBUTES;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.jdbc.OrientJdbcConnection;

import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.MyException;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;

public class ORJdbc {

	 
	ObjectMapper mapper = null;
	// JsonConfig jsonConfig = null;
	private String url;
	int dataCenter = 0;
	String user = "";
	String password = "";
	public boolean IsWorker = false;
	public int PoolNumber = 3;
	 Connection conn =null;
	public ORJdbc( ClientConfig config) throws JsonParseException, IOException, SQLException {
		// jsonConfig = new JsonConfig();
		// OGlobalConfiguration.SQL_GRAPH_CONSISTENCY_MODE.setValue("notx_sync_repair");
		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		 Properties info = new Properties();
		 user = "root";
		 password = "Tgdd2012";
		 info.put("user", user);
		 info.put("password", password);

		 info.put("db.usePool", "true"); // USE THE POOL
		 info.put("db.pool.min", "3");   // MINIMUM POOL SIZE
		 info.put("db.pool.max", "300");  // MAXIMUM POOL SIZE

		   conn = (OrientJdbcConnection) DriverManager.getConnection("jdbc:orient:remote:10.1.5.82,10.1.5.84,10.1.5.86,10.1.5.87,10.1.5.90,10.1.5.94/web", info);
	

	}
	private static ORJdbc instance;
	public static   ORJdbc getInstance( ClientConfig config) throws JsonParseException, IOException, SQLException {
		 
		if (instance == null) {
			 synchronized (ORJdbc.class) {
			instance = new ORJdbc(config);
			 }
		}
	 
		return instance;
	}
 
   public void executeQuery ( ) throws SQLException {
	   
	   Statement stmt = conn.createStatement();

	   ResultSet rs = stmt.executeQuery("SELECT stringKey, intKey, text, length, date FROM Item");

   }

	public void Close ( ) {
		try { 
			conn.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public   Connection getConnection() { 
		return conn;
	}
 
 
}
