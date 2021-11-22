package mwg.wb.model.searchresult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import mwg.wb.model.search.DishSO;


public class DishSOSR {

	public String message;
	public int total;
	public List<FaceObject> faceList;
	public LinkedHashMap<Integer, DishSO> dishesSO;

	public DishSOSR() {
		message = "Success";
		faceList = new ArrayList<FaceObject>();
		dishesSO = new LinkedHashMap<Integer, DishSO>();
	}

}
