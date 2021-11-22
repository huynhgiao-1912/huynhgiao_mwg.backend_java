package mwg.wb.model.searchresult;

import mwg.wb.model.products.ProductBO;

public class ProductBOSR {
	public String message = "Success";
	public boolean isNotSelling;
	public int rowCount;
	public FaceCategorySR[] faceListCategory;
	public FaceManuSR[] faceListManu;
	public FacePropSR[] faceListProp;
	public ProductBO[] productList;
	public double priceMin ;
	public double priceMax ;
	
}
