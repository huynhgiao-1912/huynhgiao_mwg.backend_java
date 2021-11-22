package mwg.wb.business;

import mwg.wb.model.cache.CacheDataBO;
import mwg.wb.model.gameapp.GameAppBO;

import java.util.HashMap;
import java.util.Map;

public class CacheStaticHelper {
	public static Map<String, CacheDataBO> g_listCacheData = new HashMap<String, CacheDataBO>();
	public static Map<String, CacheDataBO> g_listGameUserCacheData = new HashMap<String, CacheDataBO>();

	private static CacheStaticHelper instance;

	public synchronized static CacheStaticHelper getInstance() {
		if (instance == null) {
			instance = new CacheStaticHelper();
		}
		return instance;
	}

	public CacheStaticHelper() {

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

	//	synchronized (CacheStaticHelper.class) {

			CacheDataBO d = new CacheDataBO();
			d.Data = data;
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
