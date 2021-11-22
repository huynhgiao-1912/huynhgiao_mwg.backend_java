package mwg.wb.client;
 
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.model.cache.CacheDataBO;
import mwg.wb.model.gameapp.GameAppBO;

public class CachedGraphDBHelper {
	public static Map<String, CacheDataBO> g_listCacheData = new HashMap<String, CacheDataBO>();
	public static Map<String, CacheDataBO> g_listGameUserCacheData = new HashMap<String, CacheDataBO>();

	private static CachedGraphDBHelper instance;
	public ObjectMapper mapper = null, esmapper = null;
	public static CachedGraphDBHelper getInstance() {

		if (instance == null) {
			synchronized (CachedGraphDBHelper.class) {
				instance = new CachedGraphDBHelper();
			}

		}

		return instance;
	}

	public CachedGraphDBHelper() {

	}

	
		// gameapp
		public static boolean AddGameAppInterlaceToCache(String key, GameAppBO[] data) throws Throwable {
			CacheDataBO d = new CacheDataBO();
			d.Data = data;
			d.api_MemCacheDate = System.currentTimeMillis();
			d.api_MemCacheSource = "api";
			g_listGameUserCacheData.put(key, d);
			return true;
		}

		public static GameAppBO[] GetGameAppInterlace(String key) throws Throwable {

			if (g_listGameUserCacheData.containsKey(key)) {
				var ra = g_listGameUserCacheData.get(key);
				if (ra != null) {
					if (System.currentTimeMillis() - ra.api_MemCacheDate > 60 * 30 * 1000) {// 30 phut
						return null;
					} else {
						return (GameAppBO[]) ra.Data;
					}
				}

			}
			return null;
		}
		
	
	// gameuser
	public static boolean AddGameUserToCache(String key, Object data) throws Throwable {
		CacheDataBO d = new CacheDataBO();
		d.Data = data;
		d.api_MemCacheDate = System.currentTimeMillis();
		d.api_MemCacheSource = "api";
		g_listGameUserCacheData.put(key, d);

		return true;
	}

	public static Object GetGameUserFromCache(String key) throws Throwable {

		if (g_listGameUserCacheData.containsKey(key)) {
			var ra = g_listGameUserCacheData.get(key);
			if (ra != null) {
				if (System.currentTimeMillis() - ra.api_MemCacheDate > 60 * 60 * 1000) {// 60 phut
					return null;
				} else {
					return ra.Data;
				}
			}

		}
		return null;
	}

	//////////////////
	public static boolean AddToCache(String key, Object data) throws Throwable {

		//synchronized (CachedGraphDBHelper.class) {

			CacheDataBO d = new CacheDataBO();
			d.Data=  data;
			d.api_MemCacheDate = System.currentTimeMillis();
			d.api_MemCacheSource = "api";
			g_listCacheData.put(key, d);
		//}
		return true;
	}

	public static Object GetFromCache(String key) throws Throwable {

		if (g_listCacheData.containsKey(key)) {
			var ra = g_listCacheData.get(key);
			if (ra != null) {
				if (System.currentTimeMillis() - ra.api_MemCacheDate > 10 * 60 * 1000) {// 10 phut
					return null;
				} else {
					return ra.Data;
				}
			}

		}
		return null;
	}
	public static long GetCount( ) throws Throwable {

		 return g_listCacheData.values().size();
	}
	
	 
	public static Object GetFromCache(String key, int minute) throws Throwable {

		if (g_listCacheData.containsKey(key)) {
			var ra = g_listCacheData.get(key);
			if (ra != null) {
				if (System.currentTimeMillis() - ra.api_MemCacheDate > minute * 60 * 1000) {// 10 phut
					return null;
				} else {
					return ra.Data;
				}
			}

		}
		return null;
	}
}

