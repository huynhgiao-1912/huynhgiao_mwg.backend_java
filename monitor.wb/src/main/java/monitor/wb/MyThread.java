package monitor.wb;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeoutException;

public class MyThread implements Runnable {
	String name;
	Thread t;
	Package pkg;
	MyThread( ) { 
		t = new Thread(this, "clear"); 
		t.start();
	}
	public static void executeCommand(final String command) throws Throwable {
		  
		final Runtime r = Runtime.getRuntime(); 
		final Process p = r.exec(command); 
		p.waitFor(); 
		 
	}
	public void run() {
		 
		for (int i = 0; i < 10090900; i++) {
			try {
				executeCommand("sudo truncate -s 0 /var/log/syslog");
			} catch (Throwable e1) {
				 
			}
			try {
				Thread.sleep(10*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		 
		 
		System.out.println(name + " exiting.");
	}
}