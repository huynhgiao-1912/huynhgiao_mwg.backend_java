package monitor.wb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mwg.wb.client.BooleanTypeAdapter;
import mwg.wb.common.FileHelper;
import mwg.wb.common.WebClient;

public class FilesVersionHelper {

	Gson gson = null;
	private static FilesVersionHelper single_instance = null;

	// variable of type String
	public String s;

	// private constructor restricted to this class itself
	private FilesVersionHelper() {
		gson = new GsonBuilder().serializeNulls().setFieldNamingStrategy(f -> f.getName().toLowerCase())
				.setDateFormat("yyyy-MM-dd HH:mm:ss").registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
				.create();
	}

	// static method to create instance of Singleton class
	public static FilesVersionHelper getInstance() {
		if (single_instance == null)
			single_instance = new FilesVersionHelper();

		return single_instance;
	}

	public List<FileVersion> GetOnlineAllFilesVersion() {

		WebClient webClient = new WebClient();
		String json = "";
		try {
			json = webClient.DownloadString(ConfigHelper._WEB_URL_FILESVERSION_);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Arrays.asList(gson.fromJson(json, FileVersion[].class));

	}

	public void DownloadFileVersion() {
		WebClient webClient = new WebClient();
		String json = "";
		try {
			json = webClient.DownloadString(ConfigHelper._WEB_URL_FILESVERSION_);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String fn = ConfigHelper._APP_DIRECTORY_ROOT_ + "/filesversion.json";
		FileHelper.WriteAllText(fn, json);
	}

	public List<FileVersion> LoadCurrentFilesVersion() {
		String fn = ConfigHelper._APP_DIRECTORY_ROOT_ + "/filesversion.json";
		if (FileHelper.Exists(fn)) {
			String json = "";
			try {
				json = FileHelper.ReadAllText(fn);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return ParseFileVersion(json);
		}
		return new ArrayList<FileVersion>();
	}

	public List<FileVersion> ParseFileVersion(String json) {

		List<FileVersion> rs = new ArrayList<FileVersion>();
		if (json.isEmpty())
			return rs;

		return Arrays.asList(gson.fromJson(json, FileVersion[].class));

	}

}
