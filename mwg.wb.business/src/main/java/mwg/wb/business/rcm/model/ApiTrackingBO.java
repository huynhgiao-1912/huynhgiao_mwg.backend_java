package  mwg.wb.business.rcm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

 
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiTrackingBO {
	public String id;
	public String key;
	public double score;
	public double getScore() {
		return score;
	}
	public String msg;
	public String manuid;
	public String cateid;
	public String productname;
	public String buycount;
}
