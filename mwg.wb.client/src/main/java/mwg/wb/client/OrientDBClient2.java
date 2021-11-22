package mwg.wb.client;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDBConfig;
//import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
//import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;
//import org.apache.tinkerpop.gremlin.orientdb.executor.OGremlinResultSet;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.json.JSONException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import mwg.wb.common.Logs;
import mwg.wb.common.OrientDBType;
import mwg.wb.common.Utils;

public class OrientDBClient2 {
	private String host, dbname, user, password;
	private int port;
	private ODatabasePool factory;
	// private OrientGraph graph;
	private int poolsize;

	public OrientDBClient2(String host, int port, String dbname, String user, String password, int poolSize) {
		this.host = host;
		this.port = port;
		this.dbname = dbname;
		this.user = user;
		this.password = password;
		this.poolsize = poolSize;
	}

	public OrientDBClient2(int type) {
		if (type == OrientDBType.READ) {
			this.host = "172.16.3.94";
		}
		if (type == OrientDBType.WRITE) {
			this.host = "172.16.3.71";
		}

		this.port = 2424;
		this.dbname = "web";
		this.user = "root";
		this.password = "Tgdd2019#@!";
		this.poolsize = 10;
	}

	@SuppressWarnings("resource")
	public boolean Connect() {

		this.factory = new ODatabasePool("remote:" + this.host + ":" + this.port + "/" + this.dbname, this.user,
				this.password, OrientDBConfig.defaultConfig());
		return true;
	}

	public boolean Close() {
		this.factory.close();
		return true;
	}

	public OResultSet Query(String sql) {
		ODatabaseSession db = factory.acquire();
		try {

			Map<String, String> params = new HashMap<String, String>();
			return db.query(sql, params);
		} catch (Exception ex) {

		} finally {
			db.close();
		}
		return null;
	}

//	public OResultSet Function(String sql) {
//		ODatabaseDocument db = factory.getDatabase(false, true); 
//		Map<String, Object> params = new HashMap<>();
//		return db.(sql, params); 
//	}

	public int CommandCommitEgde(String sql) {
		// try
		// {
		String rsw = Command(sql);
		if (rsw == "1") {

			return 1;

		} else {

			if (rsw.contains("OConcurrentModificationException")) {
				return 0;
			} else if (rsw.contains("ORecordDuplicatedException")) {
				if (rsw.contains("Cannot index record")) {
					return 2;// lam lai
				}

				return 1;
			} else if (rsw.contains("No edge has been created because ")) {
				return 1;
			} // ODistributedRecordLockedExceptionUpdate
			else if (rsw.contains("ODistributedRecordLockedException")) {
				return 0;

			} else if (rsw.contains("{\"result\":")) {
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

	public int CommandCommit(String sql) {
		try {

			for (int i = 0; i < 2; i++) {

				String rsw = Command(sql);
				if (rsw == "1") {

					return 1;

				} else {

					Logs.WriteLine("rsw:" + rsw+"sql="+sql);
					if (rsw.contains("OConcurrentModificationException")) {
						// return 0;
						Logs.WriteLine("OConcurrentModificationException retry " + i);
						Thread.sleep(200);

					} else if (rsw.contains("ORecordDuplicatedException")) {
						if (rsw.contains("Cannot index record")) {
							return 2;// lam lai
						}

						return 1;
					} else if (rsw.contains("No edge has been created because ")) {
						return 1;
					} // ODistributedRecordLockedExceptionUpdate
					else if (rsw.contains("ODistributedRecordLockedException")) {
						Logs.WriteLine("ODistributedRecordLockedException retry " + i);
						Thread.sleep(200);

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
			Logs.WriteLine("webai:" + sql);
			// throw ex;
		}
		return 0;
	}
 
 public int CommandCommitPR(String sql,Map<String, Object> params) {
		try {

			for (int i = 0; i < 2; i++) {

				String rsw = CommandPR(sql,params);
				if (rsw == "1") {

					return 1;

				} else {

					Logs.WriteLine("rsw:" + rsw+"sql="+sql);
					if (rsw.contains("OConcurrentModificationException")) {
						// return 0;
						Logs.WriteLine("OConcurrentModificationException retry " + i);
						Thread.sleep(200);

					} else if (rsw.contains("ORecordDuplicatedException")) {
						if (rsw.contains("Cannot index record")) {
							return 2;// lam lai
						}

						return 1;
					} else if (rsw.contains("No edge has been created because ")) {
						return 1;
					} // ODistributedRecordLockedExceptionUpdate
					else if (rsw.contains("ODistributedRecordLockedException")) {
						Logs.WriteLine("ODistributedRecordLockedException retry " + i);
						Thread.sleep(200);

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
			Logs.WriteLine("webai:" + sql);
			// throw ex;
		}
		return 0;
	}
 public String CommandPR(String sql, Map<String, Object> params) {
		Date st = Utils.GetCurrentDate();

		ODatabaseSession db = factory.acquire();

		db.begin();
		try {

			 
			db.command(sql, params);
			db.commit();
			long diff = Utils.GetCurrentDate().getTime() - st.getTime();
			Logs.WriteLine(" Command:" + diff);
			return "1";
		} catch (Exception e) {
			db.reload();
			db.rollback();
			Logs.WriteLine(" sql:" + sql);
			return e.toString();
		} finally {

			db.close();
			

		}

	}
	public String Command(String sql) {
		Date st = Utils.GetCurrentDate();

		ODatabaseSession db = factory.acquire();

		db.begin();
		try {

			Map<String, String> params = new HashMap<String, String>();
			db.command(sql, params);
			db.commit();
			long diff = Utils.GetCurrentDate().getTime() - st.getTime();
			Logs.WriteLine(" Command:" + diff);
			return "1";
		} catch (Exception e) {
			db.reload();
			db.rollback();
			Logs.WriteLine(" sql:" + sql);
			return e.toString();
		} finally {

			db.close();
			

		}

	}
}
