package mwg.wb.worker;

import mwg.wb.common.Logs;
import mwg.wb.common.Utils;

public class MyThreadLogs implements Runnable {
	String name;

	Thread t;

	MyThreadLogs() {

		name = "init";
		t = new Thread(this, name);
		t.start();
	}

	public void run() {
		for (int i = 0; i < 10000000; i++) {
			runDo();
			Utils.Sleep(60 * 1000 * 60 * 3);// 3 gio xoa mot lan
		}

	}

	public void runDo() {
		try {
			Logs.DelOLdLogFactoryMessage();  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(name + " exiting.");
	}

}