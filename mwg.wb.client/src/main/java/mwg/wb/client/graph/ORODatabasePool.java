package mwg.wb.client.graph;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.db.OrientDBConfigBuilder;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import mwg.wb.common.Logs;
import mwg.wb.common.ServerType;
import mwg.wb.common.Utils;

public class ORODatabasePool {
	private String host, dbname, user, password;
	private int port;
	private ODatabasePool databasePool;
	// private ODatabaseSession db ;

	public ORODatabasePool(String host, String dbname, String userName,
			String Password, int port) {

		this.port = port;
		this.dbname = dbname;
		this.user = userName;
		this.password = Password;
		this.host = host;
		OrientDBConfigBuilder poolCfg = OrientDBConfig.builder();
		poolCfg.addConfig(OGlobalConfiguration.DB_POOL_MIN, 5);
		poolCfg.addConfig(OGlobalConfiguration.DB_POOL_MAX, 10);
		this.databasePool = new ODatabasePool("remote:" + this.host + ":" + this.port + "/" + this.dbname, this.user,
				this.password, OrientDBConfig.defaultConfig());
	}

	public boolean Connect() {

		return true;
	}

	public boolean Close() {
		this.databasePool.close();

		return true;
	}

	public String Command(String sql, Map<String, Object> params) {
		Date st = Utils.GetCurrentDate();

		try (ODatabaseSession db = databasePool.acquire()) {
			 
			db.activateOnCurrentThread();
			db.begin();
			db.command(sql, params);
			db.commit();
			long diff = Utils.GetCurrentDate().getTime() - st.getTime();
			Logs.WriteLine(" Command:" + diff);
			db.close();
			return "1";
		} catch (Exception e) {

			Logs.WriteLine(" sql:" + sql + e.getMessage());
			return e.toString();
		}

	}

	public OResultSet Query(String sql) {
		ODatabaseSession db = databasePool.acquire();
		db.activateOnCurrentThread();
		OResultSet rs= null;
		try {

			Map<String, String> params = new HashMap<String, String>();
			
			  rs= db.query(sql, params);
		} catch (Exception ex) {
ex.printStackTrace();
		} finally {
			db.rollback();
			db.close();
			 
		}
		return null;
	}

}
