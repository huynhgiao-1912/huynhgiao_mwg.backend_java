package mwg.wb.worker;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.analysis.CharArrayMap.EntrySet;

import mwg.wb.business.WorkerHelper;
import mwg.wb.business.WorkerPackage;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.FileHelper;
import mwg.wb.common.GraphDBConstant;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.common.notify.LineNotify;
 
import mwg.wb.model.api.ClientConfig;

public class Main {
	public static ORThreadLocal factoryWrite = null;
	public static ORThreadLocal factoryRead = null;
	public static LineNotify lineNotifyLog = null;
	public static String backendGroupTokenLog = "e4QuXuo08qZo6CK4wXLoO7djTuB9E9yEhIO2KusK6W9";

//NEW

	public static void LoadPackage(ClientConfig config, WorkerPackage pkg) {

		if (pkg.MaxProcessNumber <= 0)
			pkg.MaxProcessNumber = 1;

		for (int i = 0; i < pkg.MaxProcessNumber; i++) {
			new MyThread(pkg, factoryWrite, factoryRead);
		}

	}

	public static void LoadPackageShedule(ClientConfig config, WorkerPackage pkg) {

		new MyThreadSchedule(pkg, factoryWrite, factoryRead);
	}
	public static void mainbanh    (String[] args) throws Throwable {

		try {
/*
 * un 16 19:54:28 ograph01-5-71 server.sh[20786]: 2020-06-16 19:53:58:564 WARNI [ograph01-5-71] Timeout (30003ms) on waiting for synchronous responses from nodes=[ograph02-5-72, ograph05-5-75, ograph01-5-71, ograph04-5-74, ograph03-5-73, ograph06-5-76] responsesSoFar=[ograph01-5-71, ograph04-5-74, ograph06-5-76, ograph05-5-75, ograph02-5-72, ograph03-5-73] request=(id=0.5238554 task=TxPhase1 user=#69:0) [ODistributedDatabaseImpl]

 * */

			Options options = new Options();
			Option input = new Option("n", "run", true, "input package");
			input.setRequired(true);
			options.addOption(input);

			CommandLineParser parser = new DefaultParser();
			HelpFormatter formatter = new HelpFormatter();
			CommandLine cmd = null;

			try {
				cmd = parser.parse(options, args);
			} catch (ParseException e) {
				System.out.println(e.getMessage());
				formatter.printHelp("utility-name", options);

				System.exit(1);
			}
			int n =Utils.toInt(  cmd.getOptionValue("run"));

			System.out.println("run server num:" + n);
			

			factoryWrite = new ORThreadLocal();
			factoryRead = new ORThreadLocal();
			factoryWrite.IsWorker = true;
			factoryRead.IsWorker = true;
			ClientConfig config = WorkerHelper.GetWorkerClientConfig();
			System.out.println("runversion:" + config.RUN_VERSION);
			Map<String, WorkerPackage> packlist = WorkerHelper.GetPackageList( );
			String[] hostArr=config.SERVER_ORIENTDB_READ_URL1.replace("/web", "").replace("remote:", "").split("\\,");
			int p = (int) (n % hostArr.length);
			String host= hostArr[p];
			factoryRead.initRead(config,"remote:"+ host+"/web",100);
			 
			factoryWrite.initWrite(config,n,100); 
		 	for (Entry<String,WorkerPackage> item : packlist.entrySet()) {
		 		var pack=item.getValue();
		 		if(pack.ServerNumberWrite==n) {
		 		LoadPackage(config, pack );
				if (pack.IsActiveSchedule > 0) {
					Logs.Log(true, "DIDX_LOG|LoadPackageShedule", "RunScheduleTask:" + Utils.GetCurrentDate().toString() +":"+pack.ScheduleClassName);
					
					LoadPackageShedule(config, pack);
				}
		 		}
			}
		 
			
			
			new MyThreadLogs();
		} catch (Throwable e) {
			Logs.LogException(e);
		}
		try {
			Thread.sleep(60 * 60 * 60 * 1000);
		} catch (InterruptedException e) {
			System.out.println("Main thread Interrupted");
		}
		System.out.println("Mainfactory thread exiting.");
	}
	public static void main      (String[] args) throws Throwable {

		try {
			
			//khởi tạo constant edge
			try {
				GraphDBConstant.initEdgesList();
			} catch (Exception e) {
				Logs.LogException( e );
			}

			Options options = new Options();
			Option input = new Option("i", "run", true, "input package");
			input.setRequired(true);
			options.addOption(input);

			CommandLineParser parser = new DefaultParser();
			HelpFormatter formatter = new HelpFormatter();
			CommandLine cmd = null;

			try {
				cmd = parser.parse(options, args);
			} catch (ParseException e) {
				System.out.println(e.getMessage());
				formatter.printHelp("utility-name", options);

				System.exit(1);
			}
			String runCMD = cmd.getOptionValue("run");

			System.out.println("run:" + runCMD);

			factoryWrite = new ORThreadLocal();
			factoryRead = new ORThreadLocal();
			factoryWrite.IsWorker = true;
			factoryRead.IsWorker = true;
			ClientConfig config = WorkerHelper.GetWorkerClientConfig();
			System.out.println("runversion:" + config.RUN_VERSION);
			WorkerPackage pack = WorkerHelper.GetWorkerPackageByID(runCMD);
			lineNotifyLog = new LineNotify("LINE", backendGroupTokenLog);	
			if (pack == null) {
				Logs.LogException("pack == null "+runCMD);
				System.out.println("package==null:" + runCMD);
				return;
			}
			int pool=2;
//			if(pack.DataCenterConfig>0) {
//				config = WorkerHelper.GetWorkerClientConfig(pack.DataCenterConfig);
//			}
			if(pack.MaxProcessNumber>0) {
				pool=pack.MaxProcessNumber+55;
			}
			
			String message = "Start Worker: id "
			+pack.PackageId 
			+",dc " +pack.DataCenter 
					+",cf "+pack.DataCenterConfig
					+ ",or "+config.SERVER_ORIENTDB_READ_URL1
					+ ",es "+config.SERVER_ELASTICSEARCH_WRITE_HOST
					;
			//lineNotifyLog.Notify(message);
			
			
			if(pack.Queue.contains("didx")) { 
				factoryRead.initReadRoundRobin(config, pack.ServerNumber,pool);
			}else {
				//#66:-1 
				//remote:172.16.5.72,172.16.5.73,172.16.5.74,172.16.5.75,172.16.5.76/web
				String[] hostArr=config.SERVER_ORIENTDB_READ_URL1.replace("/web", "").replace("remote:", "").split("\\,");
				int p = (int) (pack.ServerNumber % hostArr.length);
				String host= hostArr[p];
				factoryRead.initRead(config,"remote:"+ host+"/web",pool);
				FileHelper.AppendAllText("logsworker.txt", pack.PackageId + ":host"+host+"\r\n");

			}
//		 if(pack.Queue.contains("stock")) {
//			 factoryWrite.initWrite(config,1,pool);
//		 }else {
			 factoryWrite.initWrite(config,0,pool);
		// }
			//cho 0 luon con 71
			LoadPackage(config, pack);
			if (pack.IsActiveSchedule > 0) {
				Logs.Log(true, "DIDX_LOG|LoadPackageShedule", "RunScheduleTask:" + Utils.GetCurrentDate().toString() +":"+pack.ScheduleClassName);
				
				LoadPackageShedule(config, pack);
			}
			new MyThreadLogs();
		} catch (Throwable e) {
			Logs.LogException(e);
		}
		try {
			Thread.sleep(60 * 60 * 60 * 1000);
		} catch (InterruptedException e) {
			System.out.println("Main thread Interrupted");
		}
		System.out.println("Mainfactory thread exiting.");
	}
	
	
	public static void main1     (String[] args) throws Throwable {

		try {
			
			//khởi tạo constant edge
			try {
				GraphDBConstant.initEdgesList();
			} catch (Exception e) {
				Logs.LogException( e );
			}

		 
		 

			String runCMD="sqlsysdata01";  

			factoryWrite = new ORThreadLocal();
			factoryRead = new ORThreadLocal();
			factoryWrite.IsWorker = true;
			factoryRead.IsWorker = true;
			ClientConfig config = WorkerHelper.GetWorkerClientConfig();
			System.out.println("runversion:" + config.RUN_VERSION);
			WorkerPackage pack = WorkerHelper.GetWorkerPackageByID(runCMD);
			lineNotifyLog = new LineNotify("LINE", backendGroupTokenLog);	
			if (pack == null) {
				Logs.LogException("pack == null "+runCMD);
				System.out.println("package==null:" + runCMD);
				return;
			}
			int pool=2;
//			if(pack.DataCenterConfig>0) {
//				config = WorkerHelper.GetWorkerClientConfig(pack.DataCenterConfig);
//			}
			if(pack.MaxProcessNumber>0) {
				pool=pack.MaxProcessNumber+55;
			}
			
			String message = "Start Worker: id "
			+pack.PackageId 
			+",dc " +pack.DataCenter 
					+",cf "+pack.DataCenterConfig
					+ ",or "+config.SERVER_ORIENTDB_READ_URL1
					+ ",es "+config.SERVER_ELASTICSEARCH_WRITE_HOST
					;
		 
			
			if(pack.Queue.contains("didx")) { 
				factoryRead.initReadRoundRobin(config, pack.ServerNumber,pool);
			}else {
				//#66:-1 
				//remote:172.16.5.72,172.16.5.73,172.16.5.74,172.16.5.75,172.16.5.76/web
				String[] hostArr=config.SERVER_ORIENTDB_READ_URL1.replace("/web", "").replace("remote:", "").split("\\,");
				int p = (int) (pack.ServerNumber % hostArr.length);
				String host= hostArr[p];
				factoryRead.initRead(config,"remote:"+ host+"/web",pool);
				FileHelper.AppendAllText("logsworker.txt", pack.PackageId + ":host"+host+"\r\n");

			}
//		 
			 factoryWrite.initWrite(config,0,pool);
			 LoadPackage(config, pack);
		} catch (Throwable e) {
			Logs.LogException(e);
		}
		try {
			Thread.sleep(60 * 60 * 60 * 1000);
		} catch (InterruptedException e) {
			System.out.println("Main thread Interrupted");
		}
		System.out.println("Mainfactory thread exiting.");
	}
	public static void mainaa (String[] args) throws Throwable {

		try {
 

		 
			 

			factoryWrite = new ORThreadLocal();
		//	factoryRead = new ORThreadLocal();
			factoryWrite.IsWorker = true;
			//factoryRead.IsWorker = true;
			ClientConfig config = WorkerHelper.GetWorkerClientConfig();
			System.out.println("runversion:" + config.RUN_VERSION);
			Map<String, WorkerPackage> packlist = WorkerHelper.GetPackageList( );
			//factoryRead.initReadRoundRobinV2(config, 0,200);
			factoryWrite.initWriteRoundRobinV2(config, 0,200);//cho 0 luon con 71
			
			
			
			
			    
			    
			for (Entry<String, WorkerPackage> packItem : packlist.entrySet()) {
				 
				WorkerPackage pack=packItem.getValue(); 
			 
				LoadPackage(config, pack);
				if (pack.IsActiveSchedule > 0) {
					Logs.Log(true, "DIDX_LOG|LoadPackageShedule", "RunScheduleTask:" + Utils.GetCurrentDate().toString() +":"+pack.ScheduleClassName);
					
					LoadPackageShedule(config, pack);
				}
			}
			 
			
			new MyThreadLogs();
		} catch (Throwable e) {
			Logs.LogException(e);
		}
		try {
			Thread.sleep(60 * 60 * 60 * 1000);
		} catch (InterruptedException e) {
			System.out.println("Main thread Interrupted");
		}
		System.out.println("Mainfactory thread exiting.");
	}
}
