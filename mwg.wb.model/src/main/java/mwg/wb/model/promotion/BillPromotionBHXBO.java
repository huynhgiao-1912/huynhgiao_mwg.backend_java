package mwg.wb.model.promotion;

import java.util.Date;

public class BillPromotionBHXBO {
	public String PromotionId;
	public Boolean IsDeleted;
	public Boolean IsStopped;
	public Boolean IsReview;
	public String PromotionName;
	public String Description;
	public Date FromDate;
	public Date ToDate;
	public String HtmlDescription;
	public int PromotionType;
	public double MinTotalMoney;
	public double MaxTotalMoney;
	public String ApplyProductId;
	public String ApplySubgroupId;
	public String GiftProductId;
	public int GiftQuantity;
	public int PromotionGiftType;
	public int DonateType;
	public int PromoStoreType;
	public int DiscountPercent;
	public double PromotionPrice;
}
