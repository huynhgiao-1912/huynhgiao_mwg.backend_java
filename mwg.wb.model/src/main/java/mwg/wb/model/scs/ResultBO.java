package mwg.wb.model.scs;

import java.util.List;

import mwg.wb.model.searchresult.FaceObject;

public class ResultBO<T> {

	public int StatusCode;
	public int Total;
	public String Message;
	public T Result;
	public List<FaceObject> Facets;
	
	
}
