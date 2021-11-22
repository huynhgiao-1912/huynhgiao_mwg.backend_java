package mwg.wb.worker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.WorkerPackage;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.FileHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.JsonConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;

public class MyThreadSchedule implements Runnable {
	String name;
	Thread t;
	WorkerPackage pkg;
	ORThreadLocal factoryWrite = null;
	ORThreadLocal factoryRead = null;
	ObjectMapper mapper = null;

	public Thread GetThread() {
		return t;
	}

	MyThreadSchedule(WorkerPackage apkg, ORThreadLocal afactoryWrite, ORThreadLocal afactoryRead) {
		pkg = apkg;
		name = pkg.PackageId;
		factoryWrite = afactoryWrite;
		factoryRead = afactoryRead;

		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		 
		t = new Thread(this, name);
		t.setPriority(5);
		t.start();

	}

	public void run() {

		for (long i = 0; i < 1000000000; i++) {
 

			try {
				//Logs.Log(true, "|ExecuteSchedule", "RunScheduleTask:" + Utils.GetCurrentDate().toString() +":"+pkg.ScheduleClassName);
				
				int rs = Job.ExecuteSchedule( pkg, factoryWrite, factoryRead);
				 System.out.println(name + " ExecuteSchedule  .");
				  
			} catch (Throwable e) {
				 
				e.printStackTrace();
				Logs.LogException(e);
			}
			Utils.Sleep(5000);
		}	
		

		
	}
}