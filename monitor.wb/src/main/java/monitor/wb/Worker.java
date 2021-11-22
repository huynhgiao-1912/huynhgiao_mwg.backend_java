package monitor.wb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import mwg.wb.common.FileHelper;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;

public class Worker implements Runnable {

	public String command;
	Thread t;

	Worker(String acommand) {
		this.command = acommand;
		t = new Thread(this, command);
		t.start();

	}

	public Thread GetThread() {
		return t;
	}

	static String g_Path = ConfigHelper._APP_DIRECTORY_ROOT_ + "process/";

	public static void executeCommand(final String command) throws Throwable {
		System.out.println("Executing command " + command);
		// Make me a Runtime.
		final Runtime r = Runtime.getRuntime();
		// Start the command process.
		final Process p = r.exec(command);
		FileHelper.WriteAllText(g_Path + p.pid() + ".txt", command);
		// Pipe it's output to System.out.
		try (final BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			String line;

			while ((line = b.readLine()) != null) {
				System.out.println(line);
			}
		}
		// Do this AFTER you've piped all the output from the process to System.out
		Logs.LogFile("monitor.txt", "waiting for " + p.pid());
		System.out.println("waiting for the process");
		p.waitFor();
		System.out.println("waiting done");
		Logs.LogFile("monitor.txt", "waiting done " + p.pid());
	}

	public void run() {
		//for (int i = 0; i < 10; i++) {

			try {
				executeCommand(command);
			} catch (Throwable e) {
				Logs.LogException(e);
				System.out.println("waiting done");
				e.printStackTrace();
			}
			Utils.Sleep(5000);
		//}
	}

}