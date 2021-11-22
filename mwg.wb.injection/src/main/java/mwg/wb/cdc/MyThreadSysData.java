package mwg.wb.cdc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeoutException;

import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.model.api.ClientConfig;

public class MyThreadSysData implements Runnable {
	String name;
	String store;
	String tbl;
	Thread t;
	String queue;
	boolean pushStatus = true;

	MyThreadSysData(String threadname, String _store,String _tbl ,String _que) {

		name = threadname;
		store = _store;
		queue=_que;
		tbl=_tbl;
		t = new Thread(this, name);
		t.start();
	}

	public void run() {

		for (int i = 0; i < 10000000; i++) {

			System.out.println(name + " run...");
			SysnDataBK sysMsg = new SysnDataBK(name, store,tbl,queue);
			try {
				sysMsg.Run();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(name + " exiting.");
	}

}