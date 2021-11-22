package mwg.wb.common;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConfig {
	private ObjectMapper mapper;
	private JsonNode root;

	/**
	 * Lấy config theo file
	 * 
	 * @param file
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public JsonConfig(String fn) {
		File file = new File(fn);
		mapper = Utils.getJsonMapper();
		try {
			root = mapper.readTree(file);
		} catch (Throwable e) {
			Logs.LogException(e);
		}
	}

//	public JsonConfig(String filename) throws JsonParseException, IOException {
//		this(new File(filename));
//	}
	public JsonConfig() throws JsonParseException, IOException {
		this("config.json");
	}

	public int getInt(String field) {
		var node = root.get(field);
		if (node != null) {
			return root.get(field).asInt();
		}
		return 0;
	}

	public double getDouble(String field) {
		return root.get(field).asDouble(0);
	}

	public boolean getBoolean(String field) {
		return root.get(field).asBoolean();
	}

	public String getString(String field) {
		var node = root.get(field);
		if (node != null) {
			return root.get(field).asText();
		}
		return "";
	}

	public String getStringWithException(String field) {
		String s = root.get(field).asText();
		if (Utils.StringIsEmpty(s)) {
			// new MyException(field + ":config empty");
			Logs.WriteLine(field + ":config empty");
			Logs.LogException(field + ":config empty");
		}

		return s;
	}

	public long getLong(String field) {
		return root.get(field).asLong();
	}

	public <T> T getObject(String field, Class<T> type) {
		return mapper.convertValue(root.get(field), type);
	}
}
