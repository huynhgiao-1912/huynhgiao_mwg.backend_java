package mwg.wb.worker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.WorkerPackage;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;

public class MyThread implements Runnable {
	String name;
	Thread t;
	WorkerPackage pkg;
	ORThreadLocal factoryWrite = null;
	ORThreadLocal factoryRead = null;
	ObjectMapper mapper = null;

	public Thread GetThread() {
		return t;
	}

	MyThread(WorkerPackage apkg, ORThreadLocal afactoryWrite, ORThreadLocal afactoryRead) {
		pkg = apkg;
		name = pkg.PackageId;
		factoryWrite = afactoryWrite;
		factoryRead = afactoryRead;

		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		// apkg.IsActiveProcess = 1;
		// apkg.ProcessClassName=pkg.PackageId;
		// apkg.MaxProcessNumber = 1;
//		try {
//			FileHelper.AppendAllText("package.json", mapper.writeValueAsString(apkg));
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		t = new Thread(this, name);
		t.setPriority(5);
		t.start();

	}

	public void run() {

		for (long i = 0; i < 1000000000; i++) {
		 	//JobAll jobAll=new JobAll();
			try {

				 int rs = Job.ExecuteProcessQueue(pkg, factoryWrite, factoryRead);
			//int rs = jobAll.ExecuteProcessQueue(pkg, factoryWrite, factoryRead);
				if (rs == -2) {
					Utils.Sleep(5000);
					factoryWrite.reInit();

				}

			} catch (Throwable e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				e.printStackTrace();
				Logs.LogException(e);
			}finally {
				//jobAll.
			}

		}

		System.out.println(name + " exiting.");
	}
}