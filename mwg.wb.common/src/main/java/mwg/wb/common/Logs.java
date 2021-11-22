package mwg.wb.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Logs {
	static String g_Path_InitLog = Utils.getCurrentDir() + "log/";
	static String g_Path_ErrorLog = Utils.getCurrentDir() + "error/";

	private static Logs instance;
	static ObjectMapper mapper = null;
	int DataCenter = 0;
	boolean isWorker = false;

	public Logs() {

		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper = new ObjectMapper();
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public Logs(int aDataCenter, boolean aisWorker) {
		DataCenter = aDataCenter;
		isWorker = aisWorker;
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper = new ObjectMapper();
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static synchronized Logs getInstance() {

		if (instance == null) {

			instance = new Logs();

		}

		return instance;
	}

	public static void CheckLogFileSize(String fnPath) {

		double fileL = FileHelper.getFileSizeMegaBytes(fnPath);
		if (fileL > 2000) {

			FileHelper.Delete(fnPath);
		}
	}

	public static void Log(boolean isLog, String note, String msg) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = dateFormat.format(Utils.GetCurrentDate());
			int t = Utils.GetMessageSaveLog(note);
			if (t == MessageLogType.DIDX_LOG || t == MessageLogType.DIDX_TOP_LOG) {
				try {

					String[] arrLog = note.split("\\|");
					String fn = "Default";
					if (arrLog.length > 1)
						fn = arrLog[1].trim();

					String fnPath = g_Path_InitLog + "/" + strDate + "/" + fn + ".txt";
					FileHelper.DirectoryExistsAndCreate(g_Path_InitLog + "/" + strDate + "/");
					CheckLogFileSize(fnPath);
					FileHelper.AppendAllText(fnPath, Utils.GetCurrentDate() + ":" + msg + "\r\n");
				} catch (Throwable e) {
					e.printStackTrace();
					// TODO: handle exception
				}
			}
			if (t == MessageLogType.PUSHALL) {
				try {
					String fnPath = g_Path_InitLog + "/" + strDate + "/PUSHALL.txt";
					FileHelper.DirectoryExistsAndCreate(g_Path_InitLog + "/" + strDate + "/");
					// String fnPath = g_Path_InitLog + "PUSHALL" + ".txt";
					CheckLogFileSize(fnPath);
					FileHelper.AppendAllText(fnPath, Utils.GetCurrentDate() + ":" + msg + "\r\n");
				} catch (Throwable e) {
					e.printStackTrace();
					// TODO: handle exception
				}
			}
			if (DidxHelper.isLocal()) {
				System.out.println(msg);
			}
		} catch (Throwable e) {
			// e.printStackTrace();
		}
	}

	public void Log(boolean isLog, String note, String h1, Object msgObj) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = dateFormat.format(Utils.GetCurrentDate());
			String msg = "";
			try {

				int t = Utils.GetMessageSaveLog(note);
				if (t == MessageLogType.DIDX_LOG || t == MessageLogType.DIDX_TOP_LOG) {

					try {
						msg = mapper.writeValueAsString(msgObj);
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String[] arrLog = note.split("\\|");
					String fn = "Default";
					if (arrLog.length > 1)
						fn = arrLog[1].trim();
					String fnPath = g_Path_InitLog + "/" + strDate + "/" + fn + ".txt";
					CheckLogFileSize(fnPath);

					FileHelper.AppendAllText(fnPath, "\r\n================\r\n" + h1 + "\r\n" + Utils.GetCurrentDate()
							+ ":" + msg + "\r\n===================\r\n");

				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (DidxHelper.isLocal()) {
				System.out.println("\r\n================\r\n" + h1 + "\r\n" + Utils.GetCurrentDate() + ":" + msg
						+ "\r\n===================\r\n");
			}
		} catch (Throwable e) {
			// e.printStackTrace();
		}
	}
	public static void Log(String msg,long timer) {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(Utils.GetCurrentDate());
		String fnPath = g_Path_InitLog + "/" + strDate + "/";
		FileHelper.DirectoryExistsAndCreate(fnPath);
		CheckLogFileSize(fnPath + "timer_searchproduct.txt");
		FileHelper.AppendAllText(fnPath + "timer_searchproduct.txt" ,  Utils.GetCurrentDate() + ":" + msg + " timer :" + timer + "\r\n");
		if (DidxHelper.isLocal()) {
			System.out.println(msg);
		}
	}

	public void LogCMN(Object msgObj) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = dateFormat.format(Utils.GetCurrentDate());
			String msg = "";
			try {
				msg = mapper.writeValueAsString(msgObj);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String fnPath = g_Path_InitLog + "/" + strDate + "/cmn.txt";
			CheckLogFileSize(fnPath);

			FileHelper.AppendAllText(fnPath, " \r\n" + msg + "\r\n===================\r\n");
		} catch (Throwable e) {
			// e.printStackTrace();
		}
	}

	public static List<MessageQueue> ReadLogSysMessage(String fn) {

		List<MessageQueue> ls = new ArrayList<MessageQueue>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(Utils.GetCurrentDate());
		String dir = g_Path_InitLog + "/inject/" + strDate + "/";
		FileHelper.DirectoryExistsAndCreate(dir);
		String fnPath = dir + fn + ".txt";
		if (FileHelper.Exists(fnPath)) {
			List<String> ra = FileHelper.ReadAllLines(fnPath);
			for (String data : ra) {
				MessageQueue msg = null;
				try {
					msg = mapper.readValue(data, MessageQueue.class);
					if (msg != null) {
						ls.add(msg);
					}
				} catch (Throwable e) {

				}
			}

		}
		return ls;
	}

	public void LogSysMessage(String fn, Object msgObj) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = dateFormat.format(Utils.GetCurrentDate());
			String dir = g_Path_InitLog + "/inject/" + strDate + "/";
			FileHelper.DirectoryExistsAndCreate(dir);
			String fnPath = dir + fn + ".txt";
			String msg = "";
			try {
				msg = mapper.writeValueAsString(msgObj);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			FileHelper.AppendAllText(fnPath, msg);
		} catch (Throwable e) {
			// e.printStackTrace();
		}
	}

	public static void LogSysMessage(String ref, String msg) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = dateFormat.format(Utils.GetCurrentDate());
			String dir = g_Path_InitLog + "/inject/" + strDate + "/";
			FileHelper.DirectoryExistsAndCreate(dir);
			String fnPath = dir + ref + ".txt";

			FileHelper.AppendAllText(fnPath, msg);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void DelOLdLogSysMessage() {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			for (int i = 10; i < 15; i++) {
				Date fromdate = Utils.AddDay(Utils.GetCurrentDate(), 0 - i);
				String dir = g_Path_InitLog + "/inject/" + dateFormat.format(fromdate) + "/";
				FileHelper.DeleteDirectory(new File(dir));
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void LogSysMessage(String ref, String queue, MessageQueue msg) {
		try {
			String ms = Utils.GetCurrentDate() + ":push " + queue + " LangID: " + msg.Lang + ", site: " + msg.SiteID
					+ ", Identify: " + msg.Identify + ", NOTE:" + msg.Note + ",classname:" + msg.ClassName + ",date:"
					+ msg.CreatedDate.toString() + ",dc" + msg.DataCenter;

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = dateFormat.format(Utils.GetCurrentDate());
			String dir = g_Path_InitLog + "/inject/" + strDate + "/";
			FileHelper.DirectoryExistsAndCreate(dir);

			String fnPath = dir + ref + ".txt";
			CheckLogFileSize(fnPath);
			FileHelper.AppendAllText(fnPath, ms);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void LogFactoryInfo(String msg, String fn) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = dateFormat.format(Utils.GetCurrentDate());
			String dir = g_Path_InitLog + "/factory/" + strDate + "/info/";
			FileHelper.DirectoryExistsAndCreate(dir);

			String fnPath = dir + fn + ".txt";
			CheckLogFileSize(fnPath);
			FileHelper.AppendAllText(fnPath, "\r\n" + Utils.GetCurrentDate() + ":" + msg);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void LogFactorySlowMessage(String msg, long time) {
		try {

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = dateFormat.format(Utils.GetCurrentDate());
			String dir = g_Path_InitLog + "/factory/" + strDate + "/slow/";
			FileHelper.DirectoryExistsAndCreate(dir);

			String fnPath = dir + strDate + ".txt";
			CheckLogFileSize(fnPath);
			FileHelper.AppendAllText(fnPath,
					"\r\n=======" + Utils.GetCurrentDate() + "|" + time + "=========\r\n" + msg);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void LogFactorySlowMessage2(String fn, String msg, long time) {
		try {

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = dateFormat.format(Utils.GetCurrentDate());
			String dir = g_Path_InitLog + "/factory/" ;
			FileHelper.DirectoryExistsAndCreate(dir);

			String fnPath = dir + "slow-" + fn + ".txt";
			CheckLogFileSize(fnPath);
			FileHelper.AppendAllText(fnPath,
					"\r\n=======" + Utils.GetCurrentDate() + "|" + time + "=========\r\n" + msg);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void LogRefresh(String fn, MessageQueue message, String msgLog) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
			String strDate = dateFormat.format(Utils.GetCurrentDate());
			Logs.LogFactoryMessage(fn, strDate + ":" + message.SiteID + "-" + message.Lang + ":" + message.Identify
					+ ":" + message.RefIdentify + ":" + msgLog);
		} catch (Throwable e) {
			// e.printStackTrace();
		}
	}

	public static void LogRefresh(String fn, MessageQueue message, String msgLog, int siteid) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
			String strDate = dateFormat.format(Utils.GetCurrentDate());
			if (siteid > 0) {
				Logs.LogFactoryMessage(fn, strDate + ":" + message.SiteID + "-" + message.Lang + ":" + message.Identify
						+ ":" + message.RefIdentify + ":" + msgLog, siteid);
			} else {
				Logs.LogFactoryMessage(fn, strDate + ":" + message.SiteID + "-" + message.Lang + ":" + message.Identify
						+ ":" + message.RefIdentify + ":" + msgLog);
			}
		} catch (Throwable e) {
			// e.printStackTrace();
		}
	}

	public static void LogFactoryMessage(String fn, String msg) {
		try {

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = dateFormat.format(Utils.GetCurrentDate());
			String dir = g_Path_InitLog + "/factory/" + strDate + "/";
			FileHelper.DirectoryExistsAndCreate(dir);

			String fnPath = dir + fn + ".txt";
			// CheckLogFileSize(fnPath);
			FileHelper.AppendAllText(fnPath, "\r\n" + Utils.GetCurrentDate() + ":" + msg);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void LogFactoryMessageDate(String fn, String msg) {
		try {

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = dateFormat.format(Utils.GetCurrentDate());
			String dir = g_Path_InitLog + "/factory/" + strDate + "/";
			FileHelper.DirectoryExistsAndCreate(dir);

			String fnPath = dir + fn + ".txt";
			// CheckLogFileSize(fnPath);
			FileHelper.AppendAllText(fnPath, "\r\n" + Utils.GetCurrentDate() + ":" + msg);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void LogFactoryMessageDateXX(String fn, String msg) {
		try {

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = dateFormat.format(Utils.GetCurrentDate());
			String dir = "/home/thanhphi/worker/log/factory/" + strDate + "/";
			FileHelper.DirectoryExistsAndCreate(dir);

			String fnPath = dir + fn + ".txt";
			// CheckLogFileSize(fnPath);
			FileHelper.AppendAllText(fnPath, "\r\n" + Utils.GetCurrentDate() + ":" + msg);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void LogFactoryMessage(String fn, String msg, int siteid) {
		try {

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = dateFormat.format(Utils.GetCurrentDate());
			String dir = g_Path_InitLog + "/factory/" + strDate + "/" + siteid + "/";
			FileHelper.DirectoryExistsAndCreate(dir);

			String fnPath = dir + fn + "" + siteid + ".txt";
			// CheckLogFileSize(fnPath);
			FileHelper.AppendAllText(fnPath, msg);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void DelOLdLogFactoryMessage() {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			for (int i = 10; i < 15; i++) {
				Date fromdate = Utils.AddDay(Utils.GetCurrentDate(), 0 - i);
				String dir = g_Path_InitLog + "/factory/" + dateFormat.format(fromdate) + "/";
				FileHelper.DeleteDirectory(new File(dir));
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void LogTemp(String fn, String msg) {
		if (fn.contains("sqlpro")) {
			FileHelper.AppendAllText(fn + ".txt", msg);
		}
	}

	public static void LogTemp2(String fn, String msg) {

		FileHelper.AppendAllText(fn + ".txt", msg);
	}

	public static void LogErrorRequireFix(String msg) {
		try {
//			FileHelper.AppendAllText(g_Path_ErrorLog + "ErrorRequireFix.txt",
//					Utils.GetCurrentDate().toString() + ":" + msg + "\r\n");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String date = dateFormat.format(Utils.GetCurrentDate());
			String dirError = g_Path_ErrorLog + date + "/";
			FileHelper.DirectoryExistsAndCreate(dirError);
			CheckLogFileSize(dirError + "ErrorRequireFix.txt");
			FileHelper.AppendAllText(dirError + "ErrorRequireFix.txt",
					Utils.GetCurrentDate().toString() + ":" + msg + "\r\n");
		} catch (Throwable e) {
			// e.printStackTrace();
		}

	}

	public static void LogErrorRequireFix(String fn, String msg) {
		try {
//			FileHelper.AppendAllText(g_Path_ErrorLog + fn + "ErrorRequireFix.txt",
//					Utils.GetCurrentDate().toString() + ":" + msg + "\r\n");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String date = dateFormat.format(Utils.GetCurrentDate());
			String dirError = g_Path_ErrorLog + date + "/";
			FileHelper.DirectoryExistsAndCreate(dirError);
			CheckLogFileSize(dirError + fn + "ErrorRequireFix.txt");
			FileHelper.AppendAllText(dirError + fn + "ErrorRequireFix.txt",
					Utils.GetCurrentDate().toString() + ":" + msg + "\r\n");


		} catch (Throwable e) {
			// e.printStackTrace();
		}

	}

	public static void WriteLine(String msg) {
		// mo cái này, chạy đầy disk vì ghi log
		// System.out.println(msg);
		// Thread currentThread = Thread.currentThread();
		// CurrentProcessInfo.ThreadID=currentThread.getId();
		// String ThreadName=currentThread.getName() ;
		// FileHelper.AppendAllText("ErrorRequireFix.txt",
		// Utils.GetCurrentDate().toString() + ":" + msg + "\r\n");

	}

	public static void WriteLine(Throwable e) {
		// mo cái này, chạy đầy disk vì ghi log
		// Thread currentThread = Thread.currentThread();
		// CurrentProcessInfo.ThreadID=currentThread.getId();
		// String ThreadName=currentThread.getName() ;
		// FileHelper.AppendAllText("ErrorRequireFix.txt",
		// Utils.GetCurrentDate().toString() + ":" + msg + "\r\n");

	}

	public static void WriteLine(String msg, String note) {
		// System.out.println(msg);
	}

	public static void Write(String msg) {
		// System.out.print(msg);
	}

	public static void LogException(Exception ex) {
		try {
			String stacktrace =  ExceptionUtils.getStackTrace(ex);
			if(NotLogException(stacktrace) ) return ;

			// ex.printStackTrace();
			 
//		CheckLogFileSize(g_Path_ErrorLog + "Exception.txt");
//		FileHelper.AppendAllText(g_Path_ErrorLog + "Exception.txt",
//				Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");
			/**
			 * Tuan Vu - chỉnh path log theo ngày
			 */
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String date = dateFormat.format(Utils.GetCurrentDate());
			String dirError = g_Path_ErrorLog + date + "/";
			FileHelper.DirectoryExistsAndCreate(dirError);
			CheckLogFileSize(dirError + "Exceptions.txt");
			FileHelper.AppendAllText(dirError + "Exceptions.txt",
					Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");
		} catch (Throwable e) {
			// e.printStackTrace();
		}
	}

	public static void LogException(String ex) {
		if(ex.contains("You have reached maximum pool size for given partition ")) return;
		
		try {
//		String stacktrace = Thread.currentThread().getName() + ":" + ex;
//		// ex.printStackTrace();
//		// System.out.println(stacktrace);
//		CheckLogFileSize(g_Path_ErrorLog + "Exception.txt");
//		FileHelper.AppendAllText(g_Path_ErrorLog + "Exception.txt",
//				Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");

			/**
			 * Tuan Vu -chỉnh lại path log file error worker
			 */
			String stacktrace =   ex;
			// ex.printStackTrace(); 
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String date = dateFormat.format(Utils.GetCurrentDate());
			String dirError = g_Path_ErrorLog + date + "/";
			FileHelper.DirectoryExistsAndCreate(dirError);
			CheckLogFileSize(dirError + "Exceptions.txt");
			FileHelper.AppendAllText(dirError + "Exceptions.txt",
					Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");
		} catch (Throwable e) {
			// e.printStackTrace();
		}
	}

	public static void LogTrace(String ex) {
		if(ex.contains("You have reached maximum pool size for given partition ")) return;

		try {
			String stacktrace =  ex;
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat dateFormatLog = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

			String date = dateFormat.format(Utils.GetCurrentDate());
			String strDate = dateFormatLog.format(Calendar.getInstance().getTime());
			stacktrace = strDate + ": \n" + stacktrace;
			String dirError = g_Path_ErrorLog + date + "/";
			FileHelper.DirectoryExistsAndCreate(dirError);
			CheckLogFileSize(dirError + "LogTrace.txt");
			FileHelper.AppendAllText(dirError + "LogTrace.txt",
					Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void LogExceptionExit(String ex) {
		if(ex.contains("You have reached maximum pool size for given partition ")) return;
		String stacktrace =   ex;
		// ex.printStackTrace();
		// System.out.println(stacktrace);
		CheckLogFileSize(g_Path_ErrorLog + "exitexception.txt");
		FileHelper.AppendAllText(g_Path_ErrorLog + "exitexception.txt",
				Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");
	}

	public static void LogExceptionExit(Throwable ex) {
		String stacktrace =  ExceptionUtils.getStackTrace(ex);
	 // ex.printStackTrace();
		if(NotLogException(stacktrace) ) return ;
		CheckLogFileSize(g_Path_ErrorLog + "exitexception.txt");
		FileHelper.AppendAllText(g_Path_ErrorLog + "exitexception.txt",
				Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");

		if (DidxHelper.isLocal()) {
			ex.printStackTrace();
		}
	}

	public static void LogException(Throwable ex) {
		try {

			String stacktrace =  ExceptionUtils.getStackTrace(ex);
			if(NotLogException(stacktrace) ) return ;
			// ex.printStackTrace();

//		CheckLogFileSize(g_Path_ErrorLog + "throwable.txt");
//		FileHelper.AppendAllText(g_Path_ErrorLog + "throwable.txt",
//				Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");
			/**
			 * Tuan Vu - chỉnh lại path worker error theo ngày
			 */
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String date = dateFormat.format(Utils.GetCurrentDate());
			String dirError = g_Path_ErrorLog + date + "/";
			FileHelper.DirectoryExistsAndCreate(dirError);
			CheckLogFileSize(dirError + "Exceptions.txt");
			FileHelper.AppendAllText(dirError + "Exceptions.txt",
					Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");

			if (DidxHelper.isLocal()) {
				ex.printStackTrace();
			}
		} catch (Throwable e) {
			// e.printStackTrace();
		}
	}
	
	public static void LogException(MessageQueue msg,   Throwable ex) {
		try {
			 
			String stacktrace  = Utils.stackTraceToString(ex);
			if(NotLogException(stacktrace) ) return ;
			// ex.printStackTrace();

//		CheckLogFileSize(g_Path_ErrorLog + "throwable.txt");
//		FileHelper.AppendAllText(g_Path_ErrorLog + "throwable.txt",
//				Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");
			/**
			 * Tuan Vu - chỉnh lại path worker error theo ngày
			 */
			
		String msgstr=	mapper.writeValueAsString(msg);
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String date = dateFormat.format(Utils.GetCurrentDate());
			String dirError = g_Path_ErrorLog + date + "/";
			FileHelper.DirectoryExistsAndCreate(dirError);
			CheckLogFileSize(dirError +msg.ClassName+ "-throwable.txt");
			FileHelper.AppendAllText(dirError + msg.ClassName+"-throwable.txt",
					Utils.GetCurrentDate().toString() + ": " + msgstr + "\r\n " + stacktrace + "\r\n");

			if (DidxHelper.isLocal()) {
				ex.printStackTrace();
			}
		} catch (Throwable e) {
			// e.printStackTrace();
		}
	}
	
	
	public static void LogException( Throwable ex,String queue) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String date = dateFormat.format(Utils.GetCurrentDate());
		String dirError = g_Path_ErrorLog + date + "/";
		try {

			String stacktrace =  ExceptionUtils.getStackTrace(ex);
			if(NotLogException(stacktrace) ) return ;
			 
			/**
			 * Tuan Vu - chỉnh lại path worker error theo ngày
			 */
			
			 
			FileHelper.DirectoryExistsAndCreate(dirError);
			CheckLogFileSize(dirError + queue+"-throwable.txt");
			FileHelper.AppendAllText(dirError + queue+"-throwable.txt",
					Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");

			if (DidxHelper.isLocal()) {
				ex.printStackTrace();
			}
		} catch (Throwable e) {
			FileHelper.AppendAllText(dirError + queue+"-writelog.txt",
					Utils.GetCurrentDate().toString() + ":" + e.getMessage() + "\r\n");
		}
	}
	//Logs.LogException(  e,query +"\n\r"+mapper.writeValueAsString(params));
	public static void LogExceptionParam( Throwable ex , String param) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String date = dateFormat.format(Utils.GetCurrentDate());
		String dirError = g_Path_ErrorLog + date + "/";
		try {

			String stacktrace =  ExceptionUtils.getStackTrace(ex);
			if(NotLogException(stacktrace) ) return ;
			 
			/**
			 * Tuan Vu - chỉnh lại path worker error theo ngày
			 */
			
			 
			FileHelper.DirectoryExistsAndCreate(dirError);
			CheckLogFileSize(dirError + "Exceptions.txt");
			FileHelper.AppendAllText(dirError + "Exceptions.txt",
					Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n Param:  "+param);

			if (DidxHelper.isLocal()) {
				ex.printStackTrace();
			}
		} catch (Throwable e) {
			 
		}
	}
	
	
	public static void LogException(int dataCenter, boolean isWorker, String stacktrace) {
		if(NotLogException(stacktrace) ) return ;
		if (isWorker == true) {
			LogException(stacktrace);
		} else {// api
			try {

				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String strDate = dateFormat.format(Utils.GetCurrentDate());
				String dir = "/home/thanhphi/logs/" + strDate + "/";
				if (dataCenter == 2 || dataCenter == 4) {
					dir = "/home/phanvankhanh/logs/" + strDate + "/";

				}
				FileHelper.DirectoryExistsAndCreate(dir);

				String fnPath = dir + "throwable.txt";
				// CheckLogFileSize(fnPath);
				FileHelper.AppendAllText(fnPath, Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public static void LogException(int dataCenter, boolean isWorker, Throwable ex) {
		try {
			String stacktrace =   ExceptionUtils.getStackTrace(ex);
			if(NotLogException(stacktrace) ) return ;
			LogException(dataCenter, isWorker, stacktrace);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

//	public static void LogException(Throwable ex,int siteid) {
//		String stacktrace = Thread.currentThread().getName() + ":" + ExceptionUtils.getStackTrace(ex);
//		// ex.printStackTrace();
//
//		CheckLogFileSize(g_Path_ErrorLog + "throwable"+siteid+".txt");
//		FileHelper.AppendAllText(g_Path_ErrorLog + "throwable"+siteid+".txt",
//				Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");
//	}
	public static void LogException(String title, Throwable ex) {
		try {
		 	String stacktrace = ExceptionUtils.getStackTrace(ex);
			if(NotLogException(stacktrace) ) return ;
			// ex.printStackTrace();

//			CheckLogFileSize(g_Path_ErrorLog + "Throwable.txt");
//			FileHelper.AppendAllText(g_Path_ErrorLog + "Throwable.txt",
//					Utils.GetCurrentDate().toString() + ":" + title + "\r\n" + stacktrace + "\r\n");

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String date = dateFormat.format(Utils.GetCurrentDate());
			String dirError = g_Path_ErrorLog + date + "/";
			FileHelper.DirectoryExistsAndCreate(dirError);
			CheckLogFileSize(dirError + "Exceptions.txt");
			FileHelper.AppendAllText(dirError + "Exceptions.txt",
					Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");
		} catch (Throwable e) {
			// e.printStackTrace();
		}
	}

	public static String GetStacktrace(Throwable ex) {
		try {
		String stacktrace = ExceptionUtils.getStackTrace(ex);
		return stacktrace;
		// System.out.println(stacktrace);
		} catch (Throwable e) {
			// e.printStackTrace();
		}
		return "xxx";
	}

	public static void Printline(Throwable ex) {
		try {
			String stacktrace = Thread.currentThread().getName() + ":" + ExceptionUtils.getStackTrace(ex);
			// ex.printStackTrace();
			// System.out.println(stacktrace);
		} catch (Throwable e) {
			// e.printStackTrace();
		}

	}

	public static void LogException(String fn, String ex) {
		try {
			String stacktrace =  ex;
			if(NotLogException(stacktrace) ) return ;
			// ex.printStackTrace();
			// System.out.println(stacktrace);
//			CheckLogFileSize(g_Path_ErrorLog + fn + "_exception.txt");
//			FileHelper.AppendAllText(g_Path_ErrorLog + fn + "_exception.txt",
//					Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String date = dateFormat.format(Utils.GetCurrentDate());
			String dirError = g_Path_ErrorLog + date + "/";
			FileHelper.DirectoryExistsAndCreate(dirError);
			CheckLogFileSize(dirError + fn + "_exception.txt");
			FileHelper.AppendAllText(dirError + fn + "_exception.txt",
					Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");
		} catch (Throwable e) {
			// e.printStackTrace();
		}
	}

	public static void LogFile(String fn, String msg) {
		try {
			CheckLogFileSize(fn);
			FileHelper.AppendAllText(fn, Utils.GetCurrentDate().toString() + ":" + msg + "\r\n");
		} catch (Throwable e) {
			// e.printStackTrace();
		}
	}
	public static boolean NotLogException(String stacktrace) {
		 
			if(stacktrace.contains("You have reached maximum pool size for given partition")) return true;
			if(stacktrace.contains("version_conflict_engine_exception")) return true;
			return false;
	}
	public static void LogException(String fn, Exception ex) {
		try {
			String stacktrace =  ExceptionUtils.getStackTrace(ex);
			if(NotLogException(stacktrace) ) return ;
			// ex.printStackTrace();
			// System.out.println(stacktrace);
//			CheckLogFileSize(g_Path_ErrorLog + fn + "_exception.txt");
//			FileHelper.AppendAllText(g_Path_ErrorLog + fn + "_exception.txt",
//					Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String date = dateFormat.format(Utils.GetCurrentDate());
			String dirError = g_Path_ErrorLog + date + "/";
			FileHelper.DirectoryExistsAndCreate(dirError);
			CheckLogFileSize(dirError + fn + "_exception.txt");
			FileHelper.AppendAllText(dirError + fn + "_exception.txt",
					Utils.GetCurrentDate().toString() + ":" + stacktrace + "\r\n");
		} catch (Throwable e) {
			// e.printStackTrace();
		}
	}

	public static void WriteLine(Exception ex) {
		// ex.printStackTrace();
	}

}
