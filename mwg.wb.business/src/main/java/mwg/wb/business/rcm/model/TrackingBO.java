package mwg.wb.business.rcm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

 
 
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackingBO {
	public Integer getId() {
		return id;
	}
	public Integer id;
	public Integer count;

}
