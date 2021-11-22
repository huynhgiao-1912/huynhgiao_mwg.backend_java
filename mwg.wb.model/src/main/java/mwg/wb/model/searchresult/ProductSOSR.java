package mwg.wb.model.searchresult;

import mwg.wb.model.search.ProductSO;

import java.util.LinkedHashMap;

public class ProductSOSR {
	public String message = "Success";
	public boolean isNotSelling = false;
	public int rowCount;
	public FaceCategorySR[] faceListCategory;
	public FaceManuSR[] faceListManu;
	public FacePropSR[] faceListProp;
	public LinkedHashMap<Integer, ProductSO> productList;
	public double priceMin ;
	public double priceMax ;
}
