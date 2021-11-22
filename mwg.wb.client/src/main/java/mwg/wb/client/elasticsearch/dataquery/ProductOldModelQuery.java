package mwg.wb.client.elasticsearch.dataquery;

import java.util.List;

public class ProductOldModelQuery {
	public int ProductID;
	public int ImeiStatus;
	public int CategoryID;
	public String PropertyNameURL;
	public String FeatureNameURL;
	public int ManufactureID;
	public double MaxPrice;
	public double MinPrice;
	public OrderType PriceOrdertype;
	public String Keyword;
	public int PageIndex;
	public int PageSize;
	public int[] ProvinceIDList;
	public int DistrictID;
	public int StoreID;
	public boolean IsHavePromotion;
	public OrderType DiscountOrdertype;
	public String[] ListInventoryStatusID;
	public long[] ListProductID;
	public int promotionType;
	public int ExtensionObject;
	public String CategoryIdList;
	public int[] ManufactureIds;
	public int DiscountTo;
	public int DiscountFrom;
	public int[] listCategory;
	public PriceFilter[] PriceFilters;
	public List<PropertyDetail> propertyDetailFilters;
	public int SiteID;

}
