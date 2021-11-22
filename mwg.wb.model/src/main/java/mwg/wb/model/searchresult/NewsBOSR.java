package mwg.wb.model.searchresult;

import java.util.ArrayList;
import java.util.List;

import mwg.wb.model.api.ProductBOApi;
import mwg.wb.model.news.NewsBO;

public class NewsBOSR {

	public String message;
	public int total;
	public List<FaceObject> facetList;
	public List<NewsBO> newsList;
	public List<NewsBO> result;

	public NewsBOSR() {
		message = "success";
		total = 0;
		facetList = new ArrayList<FaceObject>();
		newsList = new ArrayList<NewsBO>();
		result = new ArrayList<NewsBO>();// site 2
	}
}
