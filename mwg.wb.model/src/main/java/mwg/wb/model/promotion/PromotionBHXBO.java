package mwg.wb.model.promotion;

public class PromotionBHXBO {
	public int PromotionID;
	public int getPromotionID() {
		return PromotionID;
	}
	public void setPromotionID(int promotionID) {
		PromotionID = promotionID;
	}
	public int getSTOREID() {
		return STOREID;
	}
	public void setSTOREID(int sTOREID) {
		STOREID = sTOREID;
	}
	public int CorrespondingMonney;
	public String PromotionName;
	public int PromotionType;
	private String ProductID;
	public String ProductName;
	public int StockQuantity;
	public double VAT;
	public int DiscountType;
	public int DiscountValue;
	public int Quantity;
	public int DefineQuantity;
	public int QuantityUnitID;
	public int RequestQuantity;
	public String QuantityUnit;
	public boolean IsDonateAll;
	public String Description;
	public String ProductIDRef;
	public String FromDate;
	public String ToDate;
	public String ApplyProductID;
	public int STOREID;
	public String ApplyProductIDRef;
	public int LimitQuantity;
	public int ProductApplyQuantity;
	public int PromotionGiftType;
	public int APPLYSUBGROUPID;
	public int DonateType;
	public int PromoStoreType;
	public boolean ISAPPLYALLSTORE;
	public double MAXQUANTITY;
	public String getProductID() {
		return ProductID;
	}
	public void setProductID(String productID) {
		ProductID = productID;
	}
}
