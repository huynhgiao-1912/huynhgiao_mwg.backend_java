package mwg.wb.business;

import java.util.HashMap;
import java.util.stream.Stream;

public class CachedObject<T> {
	private static HashMap<String, HashMap<String, CachedObject<?>>> cachedMaps = null;

	private static HashMap<String, HashMap<String, CachedObject<?>>> getMap() {
		if (cachedMaps == null) {
			cachedMaps = new HashMap<>();
		}
		return cachedMaps;
	}

	public static <T> void putObject(String typeKey, String objectKey, T object) {
		var map = getMap().get(typeKey);
		if (map == null) {
			map = new HashMap<String, CachedObject<?>>();
			getMap().put(typeKey, map);
		}
		var cachedObject = new CachedObject<>(objectKey, object);
		map.put(objectKey, cachedObject);
	}

	public static <T> T getObject(String typeKey, String objectKey, int expiryMinutes, Class<T> type) {
		var map = getMap().get(typeKey);
		if (map == null)
			return null;
		var object = map.get(objectKey);
		if (object == null || object.isExpired(expiryMinutes))
			return null;
		return type.cast(object.getObject());
	}

	public static String[] expiredKeys(String typeKey, String[] objectKeys, int expiryMinutes) {
		var map = getMap().get(typeKey);
		if (map == null) {
			return objectKeys;
		}
		return Stream.of(objectKeys).filter(x -> {
			var y = map.get(x);
			return y == null || y.isExpired(expiryMinutes);
		}).toArray(String[]::new);
	}

	public static void clear(String typeKey, String objectKey) {
		if (typeKey == null) {
			return;
		}
		if (objectKey != null) {
			var x = getMap().get(typeKey);
			if (x != null) {
				x.remove(objectKey);
			}
		} else {
			getMap().remove(typeKey);
		}
	}

	private T object;
	private long lastUpdated;
	private String key;

	public CachedObject(String key, T object) {
		this.key = key;
		this.object = object;
		lastUpdated = System.currentTimeMillis();
	}

	public String getKey() {
		return key;
	}

	public T getObject() {
		return object;
	}

	public boolean isExpired(int expiryMinutes) {
		return System.currentTimeMillis() - lastUpdated >= 60000L * expiryMinutes;
	}
}
