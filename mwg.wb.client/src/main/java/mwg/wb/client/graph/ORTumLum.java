package mwg.wb.client.graph;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientechnologies.orient.client.remote.OStorageRemote;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.db.OrientDBConfigBuilder;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.model.products.ProductErpPriceBO;

public class ORTumLum {

	private ObjectMapper mapper = null;
//	
	public ODatabasePool pool = null;

	public ORTumLum() {

		String Url = "remote:172.16.3.71/web";
		String dbname = "web";
		String user = "root";
		String password = "Tgdd2019#@!";
		// String[] lshotWrite= new String[] { "172.16.3.71", "172.16.3.73",
		// "172.16.3.75" };
		// String[] lshotWrite= new String[] { "10.1.5.78", "10.1.5.99","10.1.5.78",
		// "10.1.5.99" };

		OrientDBConfigBuilder poolCfg = OrientDBConfig.builder();
		poolCfg.addConfig(OGlobalConfiguration.DB_POOL_MIN, 1);
		poolCfg.addConfig(OGlobalConfiguration.DB_POOL_MAX, 5);
//		poolCfg.addConfig(OGlobalConfiguration.CLIENT_CONNECTION_STRATEGY,
//				OStorageRemote.CONNECTION_STRATEGY.ROUND_ROBIN_REQUEST);

		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		// this.host= Utils.getRandomElement(lshotWrite);
		// String Url = "remote:"+this.host+"/web";
		pool = new ODatabasePool(Url, user, password, poolCfg.build());
		Logs.WriteLine(Url);
	}

	// public static String Url = "remote:10.1.5.78,10.1.5.99/web";

	public <T> T QueryFunction(String functionname, Map<String, Object> params, Class<T> type) {
		ODatabaseSession session = pool.acquire();
		session.activateOnCurrentThread();
		OResultSet rs = null;
		try {
			rs = session.query(functionname, params);
			if (rs != null)
				while (rs.hasNext()) {
					String js = rs.next().toJSON().replace("{\"rs\": ", "");
					String js2 = js.substring(0, js.length() - 1);
					return mapper.readValue(js2, type);
				}
		} catch (Exception e) {
			Logs.WriteLine(e);
		} finally {
			if (rs != null)
				rs.close();
		}
		return null;
	}

	public boolean Connect() {

		return true;
	}

	public boolean Close() {

		this.pool.close();
		// this.database.close();
		return true;
	}

	public String Command(String sql, Map<String, Object> params) {
		try (ODatabaseSession session = pool.acquire()) {
			session.activateOnCurrentThread();
			long st = System.currentTimeMillis();
			OResultSet rs = null;
			try {
				rs = session.command(sql, params);

				long diff = System.currentTimeMillis() - st;
				Logs.WriteLine(" Command:" + diff);
				return "1";
			} catch (Exception e) {
				Logs.WriteLine(" sql:" + sql + e.getMessage());
				return " sql:" + sql + e.getMessage();

			} finally {
				if (rs != null)
					rs.close();
				session.close();
			}
		}

	}

	public void massDelete(String tbl) {
		System.out.println("Delete all "+tbl);
		try (ODatabaseSession session = pool.acquire()) {
			OResultSet ls = null;
			Map<String, String> params = new HashMap<String, String>();
			try {
				for (int p = 0; p < 100; p++) {
					ls = session.query("select @rid from "+tbl+" skip 100 limit  " + 100 * p, params);

					while (ls.hasNext()) {
						String rid = ls.next().getProperty("@rid").toString();
						session.command("detete from "+tbl+"  where @rid= " + rid, params);

						System.out.print("detete " + rid);

					}
				}

			} finally {
				if (ls != null)
					ls.close();
			}
		}
		System.out.println("Done "+tbl);
	}
	public void massUpdatePrice( ) {
		System.out.println("massUpdatePrice " );
		try (ODatabaseSession session = pool.acquire()) {
			OResultSet ls = null;
			Map<String, String> params = new HashMap<String, String>();
			try {
				for (int p = 0; p < 100000; p++) {
					ls = session.query("select @rid,recordid,siteid,pricearea,outputtype,companyid,provinceid,productcode  from product_price skip "+(100 * p)+" limit  100"   , params);

					while (ls.hasNext()) {
						
						OResult  x= ls.next();
					 int siteid=12;
						String provinceid = x.getProperty("provinceid").toString();
						String productcode = x.getProperty("productcode").toString(); 
						String pricearea = x.getProperty("pricearea").toString();
						String recordidcu = x.getProperty("recordid").toString();
						String outputtype = x.getProperty("outputtype").toString();
					//	String companyid = x.getProperty("companyid").toString();
						String rid = x.getProperty("@rid").toString();
						String recordid = siteid+ "_" + outputtype + "_"   + pricearea + "_"   + provinceid + "_"+ productcode.trim();

						
					
						session.activateOnCurrentThread();
						session.command("update product_price set companyid=0, siteid="+siteid+",recordid='"+recordid+"'   where recordid= '" + recordidcu+"'", params);

						System.out.println("update " + rid);

					}
				}

			} finally {
				if (ls != null)
					ls.close();
			}
		}
		System.out.println("Done " );
	}
	public OResultSet QueryFunction(String functionname, Map<String, Object> params) {
//		OrientGraph graph = factory.getNoTx();
		ODatabaseSession session = pool.acquire();
		try {
			return session.query(functionname, params);
		} catch (Exception ex) {

		} finally {
			session.close();
		}
		return null;

	}

	public OResultSet Query(String sql) {
		ODatabaseSession session = pool.acquire();
		try {

			Map<String, String> params = new HashMap<String, String>();
			return session.query(sql, params);
		} catch (Exception ex) {

		} finally {
			session.close();
		}
		return null;
	}

	public String QueryScalar(String sql, String field) {
		ODatabaseSession session = pool.acquire();
		Map<String, String> params = new HashMap<String, String>();
		OResultSet ls = null;
		try {
			ls = session.query(sql, params);
			while (ls.hasNext()) {
				String productlangRID = ls.next().getProperty(field).toString();
				return productlangRID; // 129
				// return l;// (long) Long.parseLong( productlangRID) ; //.00
			}
		} finally {
			if (ls != null)
				ls.close();
			session.close();
		}

		return "";
	}

}
