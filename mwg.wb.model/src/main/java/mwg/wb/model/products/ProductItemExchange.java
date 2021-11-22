package mwg.wb.model.products;

public class ProductItemExchange {
	public String productcode;
	public String itemid;
	public int ischeckstockquantity;
	public int quantityunitid;
	public String unitproductcode;
	public float exchangequantity;
	public Float getExchangequantity() {
		return exchangequantity;
	}
	public void setExchangequantity(int exchangequantity) {
		this.exchangequantity = exchangequantity;
	}
	
}
