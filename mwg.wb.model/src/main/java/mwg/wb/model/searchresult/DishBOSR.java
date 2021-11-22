package mwg.wb.model.searchresult;

import java.util.ArrayList;
import java.util.List;

import mwg.wb.model.cook.CookDish;

public class DishBOSR {

	public String message;
	public int total;
	public List<FaceObject> facetList;
	public List<CookDish> result;

	public DishBOSR() {
		message = "success";
		total = 0;
		facetList = new ArrayList<FaceObject>();
		result = new ArrayList<CookDish>();
	}
}
