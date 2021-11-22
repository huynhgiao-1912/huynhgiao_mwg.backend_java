package mwg.wb.business.rcm.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

 
 
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResultBO { 
	 
	public List<ApiTrackingBO> listproduct;
	public List<TimeTrackingBO> tk_buy_manu_count_list; 
}
