package mwg.wb.client.elasticsearch.dataquery;

import java.util.Date;

public class DishQuery {

	
	public String CategoryIdList;
	public String DishIDList;
	public Date FromDate;
	public Date ToDate;
	public int IsActive;
	public int IsFeature;
	public int OrderType;// 0 (default): theo score, 1: ngay active ,2: luot view
	public OrderType OrderValue;

	public String Keyword;
	public int PageIndex;
	public int PageSize;

	public int ExtensionObject;
	public int SiteID;
	
	public String Tag;
	public int IsVideo;


}
