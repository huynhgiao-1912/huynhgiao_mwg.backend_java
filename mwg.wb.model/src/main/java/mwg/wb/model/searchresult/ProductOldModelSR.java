package mwg.wb.model.searchresult;

import mwg.wb.model.products.ProductBO;

import java.util.List;

public class ProductOldModelSR {
	
	public List<ProductBO> list;
	public int totalPromotion;
	public int totalQuantity;
	public List<Integer> categoryList;
	public int totalRecordFilter;
	public List<FaceObject> faceLst;
//	public int[] manufactureList;
//	public int[] districtList;

	public List<FaceCategorySR> faceListCategory;
	public List<FaceManuSR> faceListManu;
	public List<FaceDistrictSR> faceDistrict;
	public String message;

}
