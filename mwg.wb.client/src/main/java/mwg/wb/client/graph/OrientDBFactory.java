package mwg.wb.client.graph;

import java.util.HashMap;
import java.util.Map;
//
public class OrientDBFactory  {
	public Map<String, OrientDBClient2> LsConnect = null;
	final String host = "172.16.3.93";
	//final int[] lpOrt = new int[] { 24241, 24242, 24243 };
	final int[] lpOrt = new int[] { 24241  };

	public void InitConnect() {
		synchronized (OrientDBFactory.class) {
			if (LsConnect != null)
				return;

			LsConnect = new HashMap<String, OrientDBClient2>();
			for (int port : lpOrt) {
				OrientDBClient2 obj = new OrientDBClient2(host, port);
				obj.Connect();
				LsConnect.put(String.valueOf(port), obj);
			}
		}
	}

	public void Close() {
		synchronized (OrientDBFactory.class) {
			try {
				for (int port : lpOrt) {
					OrientDBClient2 obj = LsConnect.get(String.valueOf(port));
					if (obj != null && obj.isOpen()) {
						obj.Close();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public OrientDBClient2 GetConnect(String tablename) {
//		String tb = tablename.toLowerCase();
//		if (tb.startsWith("pm_currentinstock")) {
//			return LsConnect.get("24241");
//		}
//		if (tb.startsWith("pm_product")) {
//			return LsConnect.get("24241");
//		}
//
//		if (tb.startsWith("pm_store")) {
//			return LsConnect.get("24241");
//		}
//
//		if (tb.startsWith("product_promotion") || tb.startsWith("promotionupsert")
//
//		) {
//			return LsConnect.get("24242");
//		}
//		if (tb.startsWith("product_price") || tb.startsWith("priceupsert") || tb.startsWith("pricestatus")) {
//			return LsConnect.get("24243");
//		}
//		if (tb.startsWith("read")) {
//			return LsConnect.get("24241");
//		}
//		 
//		if (tb.startsWith("product")) {
//			return LsConnect.get("24241");
//		}

		return LsConnect.get("24241");
	}
}

//
//public class OrientDBFactory {
//	public Map<String, OrientDBClient2> LsConnect = null;
//	final String host = "172.16.3.71";
//	//final int[] lpOrt = new int[] { 24241, 24242, 24243 };
//	final int[] lpOrt = new int[] { 2424   };
//
//	public void InitConnect() {
//		synchronized (OrientDBFactory.class) {
//			if (LsConnect != null)
//				return;
//
//			LsConnect = new HashMap<String, OrientDBClient2>();
//			for (int port : lpOrt) {
//				OrientDBClient2 obj = new OrientDBClient2(host, port);
//				obj.Connect();
//				LsConnect.put(String.valueOf(port), obj);
//			}
//		}
//	}
//
//	public void Close() {
//		synchronized (OrientDBFactory.class) {
//			try {
//				for (int port : lpOrt) {
//					OrientDBClient2 obj = LsConnect.get(String.valueOf(port));
//					if (obj != null && obj.isOpen()) {
//						obj.Close();
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//	}
//
//	public OrientDBClient2 GetConnect(String tablename) {
//		String tb = tablename.toLowerCase();
//		return LsConnect.get("2424");
//		  
//	}
//}
