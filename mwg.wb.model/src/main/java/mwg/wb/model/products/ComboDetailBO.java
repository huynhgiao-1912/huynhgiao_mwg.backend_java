package mwg.wb.model.products;
import java.util.List;

import mwg.wb.model.promotion.PromotionBHX;


public class ComboDetailBO {
	public int ProductId;
    public String ProductCode ;
    public double Value ;
    public int Quantity ;
    public double Vat;
    public double Price ;
    public int StockQuantity ;
    public double ComboPrice ;
    public int ComboType ;
    public int PosSubGroupId;
    public double DiscountByQuantityPromotion;
    
    public List<PromotionBHX> Promotions;
    
	public int getQuantity() {
		return Quantity;
	}
	public void setQuantity(int quantity) {
		Quantity = quantity;
	}
	public double getPrice() {
		return Price;
	}
	public void setPrice(double price) {
		Price = price;
	}
	public int getStockQuantity() {
		return StockQuantity;
	}
	public void setStockQuantity(int stockQuantity) {
		StockQuantity = stockQuantity;
	}
    
}
