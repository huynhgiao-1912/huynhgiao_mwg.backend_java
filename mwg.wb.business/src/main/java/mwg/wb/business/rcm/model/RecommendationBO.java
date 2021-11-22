package mwg.wb.business.rcm.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendationBO {
	
	public List<TrackingBO> tk_viewed_count_list;
	
	//Rule 3
	public List<TrackingBO> tk_buy_count_list;
	
	public List<TimeTrackingBO> tk_viewed_product_count_list;
	
	
	//Rule 2
	public List<TimeTrackingBO> tk_buy_product_count_list;
	
	//Rule 9
	public List<TimeTrackingBO> tk_buy_manu_count_list;
	
	//Rule 6
	public List<TimeTrackingBO> tk_buy_product_inmonth_count_list;
	
	//Rule 8
	public List<TimeTrackingBO> tk_viewed_product_time_list;
	
	//Rule1
	public List<TimeTrackingBO> tk_buy_productcategory_count_list;
	
	
	//Rule 7
	public List<TimeTrackingBO> tk_product_addtocart_notbuy_count_list;
	
}
