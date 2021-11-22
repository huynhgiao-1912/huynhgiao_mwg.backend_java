package com.mwg.tool.model;


public interface DiffComparable {
	
	String diff(Object object) throws IllegalArgumentException, IllegalAccessException ; 
	
}
