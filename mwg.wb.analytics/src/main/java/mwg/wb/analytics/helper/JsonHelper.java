package mwg.wb.analytics.helper;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonHelper {
	private final static ObjectMapper mapper = new ObjectMapper();
	
	static {
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
		
		mapper.registerModule(new JavaTimeModule());
	}
	
	public static <T> T readJsonAsObject(String json, Class<T> t) throws JsonParseException, JsonMappingException, IOException {
		return (T) mapper.readValue(json.getBytes("utf-8"), t);
	}
	
	public static <T> T readAtNode(String root, String node, Class<T> t) throws JsonParseException, JsonMappingException, IOException {
		JsonNode json = mapper.readTree(root).path(node);
		return (T) mapper.convertValue(json, t);
	}
	
	public static String writeAsString(Object object) throws JsonProcessingException {
		return mapper.writeValueAsString(object);
	}
	
	public static ObjectMapper getMapperInstance() {
		return mapper;
	}
}

