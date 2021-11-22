package mwg.wb.webapi;

import java.util.HashMap;
import java.util.Map;

import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;

import com.orientechnologies.common.concur.ONeedRetryException;
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.exception.ODatabaseException;

public class OrientClient {
	private String host, dbname, user, password;
	private int port;
	private OrientGraphFactory factory;
	private static Map<String, OrientClient> ls = new HashMap<String, OrientClient>();

	@SuppressWarnings("resource")
	public OrientClient(String host, int port, String dbname, String user, String password) {
		this.host = host;
		this.port = port;
		this.dbname = dbname;
		this.user = user;
		this.password = password;
		//host = "172.16.3.93";
		this.factory = new OrientGraphFactory("remote:" + this.host + ":" + this.port + "/" + this.dbname, this.user,
				this.password).setupPool(1, 50);

	}

	public String command(String sql) {

		OrientGraph graph = factory.getTx();
		graph.begin();
		try {
			graph.executeSql(sql);
			graph.commit();
			return "1";
		} catch (Exception e) {
			graph.database().reload();
			graph.rollback(); 
			return e.toString();
		} finally {
			graph.tx().close();
			graph.close();

		} 
	}

	public String commandccc(String sql) {

		String result = null;
		int nRetries = 10;

		OrientGraph graph = factory.getTx();
		graph.begin();

		for (int i = 0; i < nRetries; i++) {
			try {
				graph.executeSql(sql);
				graph.commit();
				result = "Success";
				break;
			} catch (ONeedRetryException e) {
				if (i == nRetries - 1) {
					return e.toString();
				}
				graph.rollback();
				graph.database().reload();
				graph.begin();
			} catch (Exception e) {
				result = OException.wrapException(new ODatabaseException("Error during tx retry"), e).toString();
			}

		}

		graph.close();
		// factory.close();
		return result;
	}

	// singleton
	public static OrientClient getInstance(String ip) {

		if (ls.containsKey(ip)) {
			return ls.get(ip);
		}
		OrientClient client = new OrientClient(ip, 2424, "web", "root", "Tgdd2019#@!");
		ls.put(ip, client);
		// _instance = new OrientClient(ip, 2424, "test", "root", "Tgdd2012");
		return client;
	}
}
