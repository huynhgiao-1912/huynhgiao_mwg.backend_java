package mwg.wb.client.elasticsearch.dataquery;

import java.util.List;

public class PromotionQuery {
	public int ProductId;
	public List<Integer> ProductIds;
	public String ProductCode;
	public List<Integer> PromotionIds;
	public int CategoryId;

	public int SiteId;
	public int ProvinceId;
	public int PageIndex;
	public int PageSize;

	public String Keyword;
	
	public OrderType OrderType;
	public OrderValue OrderValue;
}


