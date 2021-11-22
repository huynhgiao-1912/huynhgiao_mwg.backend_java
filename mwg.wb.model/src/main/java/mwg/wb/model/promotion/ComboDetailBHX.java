package mwg.wb.model.promotion;

public class ComboDetailBHX {
	public int ProductId;
    public String ProductCode;
    public double Value;
    public int Quantity;
    public double Vat;
    public double Price;
    private int StockQuantity;
    public double ComboPrice;
    public int ComboType;
    public int PosSubGroupId;
    public double DiscountByQuantityPromotion;
	public int getStockQuantity() {
		return StockQuantity;
	}
	public void setStockQuantity(int stockQuantity) {
		StockQuantity = stockQuantity;
	}
}
