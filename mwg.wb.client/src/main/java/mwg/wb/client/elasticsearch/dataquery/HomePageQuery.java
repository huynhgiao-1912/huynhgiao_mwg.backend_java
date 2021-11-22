package mwg.wb.client.elasticsearch.dataquery;

import java.util.Map;

import mwg.wb.client.elasticsearch.dataquery.ProductQuery.SearchType;

public class HomePageQuery {
	public int siteID;
	public int category;
	public int pageSize;
	public int pageIndex;
	public int provinceID;
	public int manuID;
	public int countPerCat;
	public String[] ListManuName;
	public SearchType homeType;
	public enum SearchType {
		/**
		 * lấy sản phẩm theo cate => áp dụng cho dt, tablet, laptop ở trang chủ
		 */
		PRODUCT,
		/**
		 * GetAccessoryHomePageProductNew lấy phụ kiện ở trang chủ
		 */
		ACCESSORY,
		/*
		 * GetAccessoryHomePageProductApple2021
		 * */
		ACCESSORYAPPLE,
		/**
		 * GetGenuineAccessoryHomePage lấy phụ kiện chính hãng ở trang chủ
		 */
		GENUINEACCESSORY,
		
		/**
		 * GetFeatureProductByCategories
		 */
		FEATUREPRODUCTDMX
		
		
	}
}
