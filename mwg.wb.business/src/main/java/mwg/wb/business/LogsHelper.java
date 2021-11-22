package mwg.wb.business;

import mwg.wb.client.queue.QueueLogsHelper;

public class LogsHelper {
	public static String ExchangeName = "gr.exlogs";

	public static void SendLog(Exception ex) {
		// ex.printStackTrace();
	}

	public static void SendLog(String msg) {

		try {
			QueueLogsHelper.Current().PushExchange(ExchangeName, msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Thread currentThread = Thread.currentThread();
		// CurrentProcessInfo.ThreadID=currentThread.getId();
		// CurrentProcessInfo.ThreadName=currentThread.getName() ;
	}
}
