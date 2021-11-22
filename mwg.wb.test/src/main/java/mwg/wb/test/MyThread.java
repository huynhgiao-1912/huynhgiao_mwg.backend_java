package mwg.wb.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;

 
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
 
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import mwg.wb.business.PriceHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.graph.OrientDBClient2;
import mwg.wb.common.ServerType;
import mwg.wb.common.Utils;

public class MyThread implements Runnable {
	String name;
	Thread t;
	ORThreadLocal factory = null;
	String table;
	String data="";
	MyThread(String aname, String atable, ORThreadLocal afactory) {
		name = aname;
		table = atable;
		factory = afactory;
		 for (int i = 1; i < 1000 ; i++) {
			data=data+"_sdaaaaaaaaAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAaaaaaaaaa";
		 }
		t = new Thread(this, name);
		t.start();
	}

	public long PushOR(int id, String table, String Name) {
		int i = 0;
		Map<String, String> params = new HashMap<String, String>();
		// com.orientechnologies.common.concur.lock.OLockException
		while (true) {
			OrientGraph graphtNoTx = null;

			try {
				graphtNoTx =null;// factory.getGraphtNoTx(); 
				long t1 = System.currentTimeMillis();
				graphtNoTx.executeSql(
						"update   " + table + " set id=" + id + ", name='" + Name + "' upsert where id=" + id +" LOCK  default ", params);
				return System.currentTimeMillis() - t1;

			} catch (OConcurrentModificationException cme) {
				graphtNoTx.getRawDatabase().reload(); 
				System.out.print("retry " + i);
				// cme.printStackTrace();
				i++;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			} catch (Exception e) {
				graphtNoTx.getRawDatabase().reload(); 
				// e.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException d) {
					// TODO Auto-generated catch block
					d.printStackTrace();
				}
				// System.out.print(e.getMessage());
				return -1;
			} finally {
				graphtNoTx.close();
				graphtNoTx.tx().readWrite();
			}
		}

	}

	public void run() {
		 
		Map<String, Object> params = new HashMap<String, Object>();
	 
		 		params.put("name", data);
String sql="update b set name=:name ,id=2 upsert where id=2 ";

 if(table.equals("test2")) {
 //sql = " select from b where id=1 ";
 }
		for (int i = 1; i < 10000000; i++) {

			String ra = null;
			try {
				 if(table.equals("test2")) {
				  ra = factory.Command  (  sql, params);
				 }else {
					 //  sql="update b set name=:name ,id=1 upsert where id=1 ";

			   sql = "delete edge e_ab from (select from a where id=1) to (select from b where id= 2   )";
			  ra = factory.Command  (  sql, params);
			   sql = "create edge e_ab from(select from a where id=1) to (select from b where id= 2   )";

			 	ra = factory.Command    (  sql, params);
				 }
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Utils.Sleep(300);
			System.out.println(" up " + table + "=" + i);
		}

		System.out.println(name + " exiting.");
	}

	// mass insert multi ip =OK
	public void runXXX() {

		try {
			Map<String, Object> params = new HashMap<String, Object>();

			String[] lshotWrite = new String[] { "172.16.3.71", "172.16.3.73", "172.16.3.75" };
			String host = Utils.getRandomElement(lshotWrite);
			System.out.println(host + " run.");
			OrientDBClient2 orientdb = new OrientDBClient2(host, 2424);
			orientdb.Connect();
			for (int i = 1; i < 10000000; i++) {

				int d = i;
				orientdb.Command(
						"update    cluster:price_71 set name='name" + d + "' ,id=" + d + " upsert where id=" + d,
						params);
				System.out.println(orientdb.GetIP() + " up " + i);
			}

			orientdb.Close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(name + " exiting.");
	}
//	public void run() { //banh
//
//		try {
//			Map<String, Object> params = new HashMap<String, Object>();
//
//			String[] lshotWrite = new String[] { "172.16.3.71", "172.16.3.73", "172.16.3.75" };
//			String host = Utils.getRandomElement(lshotWrite);
//			System.out.println(host + " run.");
//			OrientDBClient2 orientdb = new OrientDBClient2(host, 2424);
//			orientdb.Connect();
//			for (int i = 0; i < 10000000; i++) {
//
//				int d = i;
//
//				orientdb.CommandCommit("update test1   set name='name" + d + "' ,id=" + d + "  upsert  where id=" + d,
//						params);
//				System.out.println(orientdb.GetIP() + " up " + i);
//			}
//
//			orientdb.Close();
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		System.out.println(name + " exiting.");
//	}

//	public void run() {
//
//		try {
//			Map<String, Object> params = new HashMap<String, Object>();
//
//			String[] lshotWrite = new String[] { "172.16.3.71", "172.16.3.73", "172.16.3.75" };
//			String host = Utils.getRandomElement(lshotWrite);
//			System.out.println(host + " run.");
//			OrientDBClient2 orientdb = new OrientDBClient2(host, 2424);
//			orientdb.Connect();
//			for (int i = 0; i < 10000000; i++) {
//
//				int d = i;
//
//				orientdb.CommandCommit("update test1   set name='name" + d + "' ,id=" + d + "  upsert  where id=" + d,
//						params);
//				System.out.println(orientdb.GetIP() + " up " + i);
//			}
//
//			orientdb.Close();
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		System.out.println(name + " exiting.");
//	}
}