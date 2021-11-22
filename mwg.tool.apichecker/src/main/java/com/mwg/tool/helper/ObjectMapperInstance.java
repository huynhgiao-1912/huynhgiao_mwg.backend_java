package com.mwg.tool.helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperInstance {
	private static ObjectMapper mapper;
	private static final ReentrantLock reLock = new ReentrantLock(true);
	
	private static void initObject() {
		mapper = new ObjectMapper();
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
	}
	
	public static void setDateFormat(String... dateFormat) {
		DateFormat df = null;
		if(dateFormat.length == 0) {
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		}else {
			df = new SimpleDateFormat(dateFormat[0]);
		}
		mapper.setDateFormat(df);
	}
	
	public static ObjectMapper getInstance() {
		if(mapper == null) {
			reLock.lock();
			if(mapper == null) {
				initObject();
			}
			reLock.unlock();
		}
			
		return mapper;
	}

	public static ObjectMapper getInstance(ObjectMapper object) {
		if(mapper == null) {
			reLock.lock();
			if(mapper == null) {
				mapper = object.copy();
			}
			reLock.unlock();
		}
			
		return mapper;
	}
}
