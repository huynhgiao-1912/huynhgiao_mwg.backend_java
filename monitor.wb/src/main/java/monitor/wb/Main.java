package monitor.wb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FilenameUtils;

import mwg.wb.business.WorkerHelper;
import mwg.wb.business.WorkerPackage;
import mwg.wb.common.FileHelper;
import mwg.wb.common.Logs;
import mwg.wb.common.TypeConvertHelper;
import mwg.wb.common.Utils;
import mwg.wb.common.WebClient;
import mwg.wb.model.api.ClientConfig;

//
//import xeus.jcl.JarClassLoader;
//import xeus.jcl.JclObjectFactory;

public class Main {
//	private static void KillAllProcess() {
//		//String currentIPAddress = GetCurrentIPAddress();
//
//		if (FileHelper.DirectoryExists(g_Path)) {
//			String[] files = FileHelper.DirectoryGetFiles(g_Path);
//			for (String fileName : files) {
//		 
//				int pID = TypeConvertHelper.ToInt32(FileHelper.GetFileNameWithoutExtension(fileName));
//				ProcessHandle aa=ProcessHandle.of(pID).get() ;
//				if(aa.isAlive()) {
//					aa.destroyForcibly();
//					FileHelper.Delete(fileName);
//					 
//
//				}
//
//				FileHelper.Delete(fileName);
//			}
//		}
//	}

	static String g_Path = ConfigHelper._APP_DIRECTORY_ROOT_ + "process/";

	public static Process StartProcessA(String command) {
		System.out.println("Start Process :");
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			process = runtime.exec(command);
			FileHelper.WriteAllText(g_Path + process.pid() + ".txt", command);
			Logs.WriteLine("Start..." + command);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return process;
	}

	public static void StartProcess(String command) {
		System.out.println("Start Process :" + command);
		new Worker(command);

	}

	public static void executeCommand(final String command) throws Throwable {
		System.out.println("Executing command " + command);
		// Make me a Runtime.
		final Runtime r = Runtime.getRuntime();
		// Start the command process.
		final Process p = r.exec(command);

//		try (final BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
//			String line;
//
//			while ((line = b.readLine()) != null) {
//				System.out.println(line);
//			}
//		}
		// Do this AFTER you've piped all the output from the process to System.out
		// Logs.LogFile("monitor.txt", "waiting for "+p.pid());
		System.out.println("waiting for the process");
		p.waitFor();
		System.out.println("waiting done");
		// Logs.LogFile("monitor.txt", "waiting done "+p.pid());
	}

	private static void CheckAndReStartAllProcess(boolean IsRestart) {
		Logs.LogFile("monitor.txt", "Stop...");
		try {
			//executeCommand("pkill -f factory");
		} catch (Throwable e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// List<String> factory_cmdList = new ArrayList<String>();//(/"javac -jar
		// mwg.wb.factory.jar";
		// List<String> factory_cmdList = FileHelper.ReadAllLines("cmdlist.txt");

		// ClientConfig config = WorkerHelper.GetWorkerClientConfig();
		Map<String, WorkerPackage> packList = WorkerHelper.GetPackageList();

		/// String currentIPAddress = GetCurrentIPAddress();
//		ProcessHandle.allProcesses().map(ProcessHandle::info).filter(p -> p.command().isPresent())
//		.forEach(System.out::println);
		// String pathProcess =Utils.getCurrentDir()+"process/";
		List<String> files = FileHelper.DirectoryGetFiles(g_Path);
		if (files != null && files.size() >= 1) {
			for (String str3 : files) {

				for (int i = 0; i < 1000000000; i++) {

					long pHandle = TypeConvertHelper.ToInt32(str3.replace(".txt", ""));
					if (pHandle <= 0) {

						Logs.WriteLine("pHandle<0..." + str3);
						FileHelper.Delete(g_Path + str3);
						Logs.LogFile("monitor.txt", "pHandle<0 " + pHandle);
						break;
					}
					ProcessHandle aa = ProcessHandle.of(pHandle).orElse(null);
					if (aa == null) {
						Logs.WriteLine("is stoped..." + str3);
						FileHelper.Delete(g_Path + str3);
						Logs.LogFile("monitor.txt", "stoped " + pHandle);
						break;
					} else {
						try {
							aa.destroyForcibly();
						} catch (Exception e) {
							// TODO: handle exception
						}
						Logs.LogFile("monitor.txt", "kill... " + pHandle);
						String cmd = "taskkill /F /PID " + pHandle;
						try {
							Runtime.getRuntime().exec(cmd);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("Utils.Sleep(5000)");
						Utils.Sleep(5000);
					
						// FileHelper.Delete(g_Path + str3);
						// Logs.WriteLine("Stop..." + str3);
					}
				}

			}

		} else {
			Logs.WriteLine("list process not found..." + g_Path);
		}
		if (IsRestart) {
			Logs.LogFile("monitor.txt", "restart...");
			Logs.WriteLine("packList " + packList.size());
			for (Map.Entry<String, WorkerPackage> entry : packList.entrySet()) {
				var pkg = entry.getValue();
				if (pkg.IsActiveProcess == 1) {
					if(pkg.PackageId.contains("news")
							|| pkg.PackageId.contains("sim")
							|| pkg.PackageId.contains("game")
							) {
					String cmd = "java -Xmx5G   -jar mwg.wb.factory.jar -i " + pkg.PackageId;
					StartProcess(cmd);
					}else {
						String cmd = "java -Xmx5G   -jar mwg.wb.factory.jar -i " + pkg.PackageId;
						StartProcess(cmd);
					}
					 
				}
			}
		}

	}

	private static String GetCurrentIPAddress() {
		// TODO Auto-generated method stub
		return null;
	}

//	private static void Start() {
//
//		for (int k = 0; k < 11111111; k++) {
//
//			List<FileVersion> UpdateList = PackageHelper.getInstance().GetNewVersionFiles();
//			Logs.WriteLine("CHECK VERSION :");
//			if (UpdateList == null || UpdateList.size() <= 0) {
//
//				 CheckAndStartAllProcess();
//				Logs.WriteLine("Monitor....");
//
//				try {
//					Thread.sleep(10000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//			} else {
//
//				// KillProcess();
//				WebClient webClient = new WebClient();
//
//				for (FileVersion item : UpdateList) {
//					// if (FILENAME_DOWNLOAD_EXCLUDE.Contains(item.FileName.ToLower()))
//					// {
//					// Logs("only update =tay " + item.FileName);
//					// continue;
//					// }
//					String pkdir = "";
//					String tempPath = "";
//					String localPackFile = ConfigHelper._APP_DIRECTORY_ROOT_ + "/" + item.FileName;
//					String tempdir = FileHelper.GetDirectoryName(tempPath);
//					FileHelper.CreateDirectory(tempdir);
//					String localdir = FileHelper.GetDirectoryName(localPackFile);
//					FileHelper.CreateDirectory(localdir);
//					// String pkdir = FileHelper.GetDirectoryName(backupPack);
//					FileHelper.CreateDirectory(pkdir);
//					String DownloadUrl = item.DownloadUrl.replace("\\", "/");
//					try {
//						if (FileHelper.Exists(tempPath)) {
//							FileHelper.Delete(tempPath);
//						}
//						Logs.WriteLine("Download : " + DownloadUrl + " save to " + tempPath);
//						webClient.DownloadFile(DownloadUrl, tempPath);
//
//					} catch (Exception ex) {
//
//						Logs.WriteLine("DownloadFile error " + ex.getMessage());
//						continue;
//					}
//
//					if (FileHelper.Exists(tempPath)) {
//						if (FileHelper.Exists(localPackFile)) {
//							FileHelper.Delete(localPackFile);
//						}
//						Logs.WriteLine("Copy to " + localPackFile);
//						FileHelper.Copy(tempPath, localPackFile, true);
//
//					}
//
//				}
//				FilesVersionHelper.getInstance().DownloadFileVersion();
//				CheckAndStartAllProcess();
//
//			}
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//	}
	public static boolean CheckHasNewVersion() {

		return true;
	}

	public static void DownloadNewFile() {

//		WebClient webClient = new WebClient();
//
//		for (FileVersion item : UpdateList) {
//			// if (FILENAME_DOWNLOAD_EXCLUDE.Contains(item.FileName.ToLower()))
//			// {
//			// Logs("only update =tay " + item.FileName);
//			// continue;
//			// }
//			String pkdir = "";
//			String tempPath = "";
//			String localPackFile = ConfigHelper._APP_DIRECTORY_ROOT_ + "/" + item.FileName;
//			String tempdir = FileHelper.GetDirectoryName(tempPath);
//			FileHelper.CreateDirectory(tempdir);
//			String localdir = FileHelper.GetDirectoryName(localPackFile);
//			FileHelper.CreateDirectory(localdir);
//			// String pkdir = FileHelper.GetDirectoryName(backupPack);
//			FileHelper.CreateDirectory(pkdir);
//			String DownloadUrl = item.DownloadUrl.replace("\\", "/");
//			try {
//				if (FileHelper.Exists(tempPath)) {
//					FileHelper.Delete(tempPath);
//				}
//				Logs.WriteLine("Download : " + DownloadUrl + " save to " + tempPath);
//				webClient.DownloadFile(DownloadUrl, tempPath);
//
//			} catch (Exception ex) {
//
//				Logs.WriteLine("DownloadFile error " + ex.getMessage());
//				continue;
//			}
//
//			if (FileHelper.Exists(tempPath)) {
//				if (FileHelper.Exists(localPackFile)) {
//					FileHelper.Delete(localPackFile);
//				}
//				Logs.WriteLine("Copy to " + localPackFile);
//				FileHelper.Copy(tempPath, localPackFile, true);
//
//			}
//
//		}
//		FilesVersionHelper.getInstance().DownloadFileVersion();
	}

	public static void main(String[] args) throws Exception {
	   new 	MyThread();
		int time = 2 * 60 * 12;
		for (int i = 0; i < 10090900; i++) {
			try {
				CheckAndReStartAllProcess(true);
				for (int j = 0; j < time; i++) {
					Thread.sleep(5000);
					//if (CheckHasNewVersion()) { 
					//	CheckAndReStartAllProcess(false);
						//DownloadNewFile();
					//}
				}
				
			} catch (Throwable e) {
				e.printStackTrace();
			}

		}
		for (int i = 0; i < 10090900; i++) {
			Thread.sleep(1000);

		}
		System.out.println("MainMonitor thread exiting.");
	}
}
