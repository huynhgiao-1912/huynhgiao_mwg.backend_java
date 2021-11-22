package monitor.wb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.ivy.plugins.repository.ssh.Scp.FileInfo;

import com.google.gson.Gson;

import mwg.wb.common.FileHelper;

public class PackageHelper {

	private static PackageHelper single_instance = null;
	private Gson gson = null;
	// variable of type String
	public String s;

	// private constructor restricted to this class itself
	private PackageHelper() {

	}

	// static method to create instance of Singleton class
	public static PackageHelper getInstance() {
		if (single_instance == null)
			single_instance = new PackageHelper();

		return single_instance;
	}

	public List<FileVersion> GetNewVersionFiles() {
		// TODO Auto-generated method stub
		return null;
	}
	public List<FileVersion> GetVersionFiles(String saveFile, String dirASM) {
		// TODO Auto-generated method stub
		File folder = new File(dirASM);
		File[] listOfFiles = folder.listFiles();
		List<FileVersion> lstfile = new ArrayList<FileVersion>();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			//if (listOfFiles[i].isFile()) {
//				String Filename = listOfFiles[i].getName();
//				FileInfo fileInfo = new FileInfo(Filename);
//				fileInfo.setFilename(listOfFiles[i].getName());
			String filename = listOfFiles[i].getName();
				lstfile.add(new FileVersion()
                {{
					FileName = filename;
                    Version = "";//fileInfo.CreationTime.ToString();
                    DownloadUrl = "http://172.16.2.24:5005/ms/root/assembly/" + filename;

                }});
			//}
		}
		
		return lstfile;
	}
	
	public Map<String, Package> ParsePackagefile(String json)
    {
		// HashMap
		Map<String, Package> rs = new HashMap<String, Package>();
		if (StringUtils.isNotEmpty(json)) return rs;
        if (!StringUtils.isNotEmpty(json))
        {
        	//var ra = Newtonsoft.Json.JsonConvert.DeserializeObject<List<Package>>(json);
        	var ra = gson.fromJson(json, Package[].class);
        	if (ra != null)
            {	
        		for(int i = 0; i < ra.length; i++)
                {
                    if (ra != null && !rs.containsKey(ra[i].PackageId))
                    {
                        if (ra[i].IsActive <= 0) continue;
                        rs.put(ra[i].PackageId, ra[i]);
                    }
                }
            }
        }
		
		return rs;
    }
	public Map<String, Package> LoadCurrentPackagefile(String fn) throws IOException //for app call
	{
		File tempFile = new File(fn);
		if (tempFile.exists())
        {
            String json = FileHelper.ReadAllText(fn);
            return ParsePackagefile(json);
        }
        return new HashMap<String, Package>();
		
	}
	
	public Package GetPackageByID(String PackageID)
    {
        Map<String, Package> lsASM;
		try {
			lsASM = LoadCurrentPackagefile(ConfigHelper._APP_DIRECTORY_ROOT_ + "/packages.json");
	        if (lsASM.containsKey(PackageID)) return lsASM.get(PackageID);
	        return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
	
//	public void InitSavePackageList(String SaveFIle) //FOR WEB CALL
//    {
//        List<Package> lst = new ArrayList<Package>();
//        lst.add(new Package()
//        {{
//            Assembly = "ms.product.dll";
//            DownloadUrl = "http://ms.com/microservicefactory/productv2.dll";
//            PackageId = "product";
//            Version = "2";
//            ScheduleClassName = "ms.product.Schedule";
//            IsActiveProcess = 1;
//            IsActiveSchedule = 1;
//        }});
//        PrintWriter writer;
//		try {
//			writer = new PrintWriter(SaveFIle, "UTF-8");
//			writer.println(gson.toJson(lst).toString());
//	        writer.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
       
//	public static List<FileVersion> GetNewVersionFiles()
//      {
//          List<FileVersion> fileVersionsOnline = null;
//          try
//          {
//              fileVersionsOnline = FilesVersionHelper.getInstance().GetOnlineAllFilesVersion();
//          }
//          catch (Exception ex)
//          {
//              Logs.WriteLine("GetOnlineAllFilesVersion  " + ex.getMessage() );
//              return null;
//          }
//
//
//          if (fileVersionsOnline == null)
//          {
//              Logs.WriteLine("newPackagefile == null ");
//              return null;
//          }
//          if (fileVersionsOnline.size() <= 0)
//          {
//        	  Logs.WriteLine("Count == 0 ");
//              return null;
//          }
//          WebClient webClient = new WebClient();
//          List<FileVersion> requireUpdateList = new ArrayList<FileVersion>();
//          List<FileVersion> currentFiles = FilesVersionHelper.getInstance().LoadCurrentFilesVersion();
//
//          if (currentFiles == null || currentFiles.size() <= 0)
//          {
//
//        	  Logs.WriteLine("CURRENT FILES VERSION : not found");
//          }
//
//      
//		for  (FileVersion item : fileVersionsOnline)
//          {
//              if (currentFiles != null)
//              {
//
//            	  FileVersion find = currentFiles.stream().Where(c -> c.FileName== item.FileName.ToLower()).FirstOrDefault();
//                  if (find != null)
//                  {
//                      if (find.Version != item.Version)
//                      {
//
//                          requireUpdateList.add(item);
//                      }
//                  }
//                  else
//                  {
//                      requireUpdateList.add(item);
//
//                  }
//              }
//
//          }
//          return requireUpdateList;
//          //if (requireUpdateList != null && requireUpdateList.Any())
//          //{
//
//          //}
//      }

}
