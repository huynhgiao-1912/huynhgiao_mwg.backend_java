package mwg.wb.client.elasticsearch.dataquery;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class ProductQuery {
	public int CategoryId;
	public int[] CategoryIdList;
	public int[] WebStatusIDList;
	public int ProvinceId;
	public int SiteId;
	public int startingFrom;
	public int PageSize;
	public int PageIndex;
	public int[] ManufacturerIdList;
	public int HasPromotion;
	public String Keyword;
	public String LanguageID;
	public SearchOrder[] Orders;
	public Set<SearchFlag> SearchFlags;
	public Set<SearchFlag>[] SearchFlagGroups;
	public String[] ListManufacturename;
	public int[] ProductIdEnumerable;// sp loại trừ
	public int ProductID; // loại trừ?
	public List<Integer> propValueIDs;
	public int TopSoldProduct; // số lượng sp bán chạy trong NH
	public PriceRangeQuery[] PriceRanges;
	public int[] promotionBKIDs; // danh sach promotionid ban kem
	public PropRangeQuery[] propRanges;
	public int sortingPropertyID;

	// Cũ bỏ, không dùng
	public int PriceFrom;
	public int PriceTo;
	public OrderType OrderByPrice;
	public List<PropertyDetail> PropertyDetailFilters;
	public ProductPropertySearching[] PropSearch;

//	public List<Integer> ListProductID;
	public int WebStatus;
	public int ManufacturerId;
	public boolean IsSearchLike;
	public boolean IsNew;
	public boolean IsSearchOnlineOnly;
	public SearchType SearchType;
	public int SearchNumberType;
	public int FeatureType;
	public int IsMonoPoly;


	public Date CreatedDateFrom;
	public Date CreatedDateTo;

	public int FillterType;
	public int IsFeature;
	public int ExtensionObject;
	public List<Integer> productIDList;
	public static enum SearchType {

		NONE,
		/**
		 * TRANG SEARCH
		 */
		SEARCH,
		/**
		 * TRANG NGÀNH HÀNG
		 */
		CATE,
		
		/*
		 * filter giá sốc
		 * */
		SEARCH2020, // 2020,
		
		SEARCH12, // = 1
		
		NUM_MINUS_1
		
	}
}
