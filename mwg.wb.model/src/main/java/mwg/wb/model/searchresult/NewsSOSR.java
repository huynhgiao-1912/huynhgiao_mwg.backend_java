package mwg.wb.model.searchresult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import mwg.wb.model.search.NewsSO;

public class NewsSOSR {
	public String message;
	public int total;
	public List<FaceObject> faceList;
	public LinkedHashMap<Integer, NewsSO> newsSOList;

	public NewsSOSR() {
		message = "Success";
		faceList = new ArrayList<FaceObject>();
		newsSOList = new LinkedHashMap<Integer, NewsSO>();
	}
}
