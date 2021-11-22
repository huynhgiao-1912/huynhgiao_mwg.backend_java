package mwg.wb.cdc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import mwg.wb.client.OracleClient;
import mwg.wb.common.FileHelper;
import mwg.wb.common.JsonConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.common.notify.LineNotify;

public class MyThreadLogs implements Runnable {
	
	String name;
	Thread t;
	String backendGroupTokenLog = "e4QuXuo08qZo6CK4wXLoO7djTuB9E9yEhIO2KusK6W9";
	LineNotify lineNotifyLog = new LineNotify("LINE", backendGroupTokenLog);

	MyThreadLogs() {
		name = "init";
		t = new Thread(this, name);
		t.start();
	}

	
	public void run() {
		for (int i = 0; i < 10000000; i++) {
			runDo();
			checkMessageQueueOverload();
			Utils.Sleep(60 * 1000 * 60 * 3);// 3 gio xoa mot lan
		}
	}

	
	public void runDo() {
		try {
			Logs.DelOLdLogSysMessage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(name + " exiting.");
	}
	
	/**
	 * Check message queue overload than 100.000
	 * <P> Repeat after 60 * 1000 * 60 * 3 milliseconds
	 */
	public void checkMessageQueueOverload() {
		try {
			
			String pathFile = "data.txt";
			if (FileHelper.Exists(pathFile)) {
				// Get lastID from file data.txt
				Long lastID = Long.parseLong(FileHelper.ReadAllText(pathFile).trim());
				// Get topID from table sysnc_message
				Long topID = getTopID();
				if (lastID != null && topID != null) {
					// Check message queue overload
					if ((topID - lastID) > 100000) {
						// Send WARNING to line chat group 
						lineNotifyLog.NotifyInfo("[Warning]: MESSAGE QUEUE OVERLOAD, stack queue more than 100.000 in 3 house");
					}
				}
			}

		} catch (Exception e) {
			Logs.LogException(e);
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Get topID from table TGDD_NEWS.DGRAPH_SYNC_MESSAGE of oracleDB
	 * 
	 * @return topID
	 */
	public Long getTopID() {

		OracleClient dbclient = null;
		Long topID = null;
		ResultSet rs = null;
		try {
			// Get information to connect database oracle from file config.json
			JsonConfig jsonConfig = new JsonConfig();

			// Create a connecting with oracleDB
//			dbclient = new OracleClient(jsonConfig.getString("DB_URL"), jsonConfig.getString("DB_USER"),
//					jsonConfig.getString("DB_PASS"));
			dbclient = new OracleClient(jsonConfig.getString("DB_CONNECTIONSTRING"), jsonConfig.getString("tns_admin"),
					jsonConfig.getString("wallet_location"),1);

			// Execute query to OracleDB
			 rs = dbclient.executeQuery("SELECT MAX(ID) AS ID FROM TGDD_NEWS.DGRAPH_SYNC_MESSAGE");
			if (rs.next()) {
				topID = rs.getLong("ID");
			}

		} catch (IOException | ClassNotFoundException | SQLException e) {
			Logs.LogException(e);
			e.printStackTrace();

		} finally {
			try {
				if(rs != null) {
					rs.close();
				}
				dbclient.closeConnection();
			} catch (SQLException e) {
				Logs.LogException(e);
				e.printStackTrace();
			}
		}
		return topID;
	}
	

}