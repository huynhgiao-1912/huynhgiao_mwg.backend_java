package mwg.wb.model.cache;

import java.util.HashMap;
import java.util.Map;

public class CacheObject {
	public String Key;
	public Long ExpireDate;
	public String Data;
	public static Map<String, String> listCache = new HashMap<String, String>();

	public CacheObject(String Key, String Data ) {
		listCache.put(Key, Data);
	}
 
}
