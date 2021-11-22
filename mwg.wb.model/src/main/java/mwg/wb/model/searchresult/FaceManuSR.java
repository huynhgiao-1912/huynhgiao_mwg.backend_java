package mwg.wb.model.searchresult;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FaceManuSR {
	public int manufacturerID;
	public String manufacturerName;
	public String manufacturerUrl;
	public String manufacturerLogo;
	public int productCount;
	public int displayorder;
	
	
//	public int getDispalyOrder() {
//		return DispalyOrder;
//	}


	@JsonIgnore
	public int getID() {
		return manufacturerID;
	}
}
