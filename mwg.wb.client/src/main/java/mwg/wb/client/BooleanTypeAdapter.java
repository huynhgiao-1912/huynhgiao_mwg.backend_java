package mwg.wb.client;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class BooleanTypeAdapter implements JsonDeserializer<Boolean> {
	public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		try {
			if(json == null || json.isJsonNull()) return false;
			int code = json.getAsInt();
			return code == 1 ? true : false;
		} catch (NumberFormatException e) {
			return json.getAsBoolean();
		}
	}
}
