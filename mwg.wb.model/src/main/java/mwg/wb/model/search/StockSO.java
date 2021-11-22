package mwg.wb.model.search;

public class StockSO extends ISO {
	public String ProductCode;
	public int BrandID;
	public int ProvinceID;
	public int Quantity;
	public int[] StoreIDList;

	/**
	 * ID: productcode_provinceid_brandid
	 */
	public void generateID() {
		ID = ProductCode + "_" + ProvinceID + "_" + BrandID;
	}
}
