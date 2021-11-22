package mwg.wb.model.search;

import java.util.Date;

import mwg.wb.model.promotion.PromotionOldProductBO;

public class PromotionOldSO {
	public String promotionlistgroupname;
	public int promotionid, promotiontype;
	public int discountvalue;
	public boolean ispercentdiscount, isdiscount, isimeipromotion;
	public long begindate, enddate;
	public double fromvalue, tovalue;
	public int storeid;
	public int promotionlistgroupid;
	public Date strenddate, strbegindate;

	public static PromotionOldSO fromBO(PromotionOldProductBO bo, int storeID) {
		return new PromotionOldSO() {
			{
				discountvalue = bo.Discountvalue;
				fromvalue = bo.FromValue;
				tovalue = bo.ToValue;
				promotiontype = bo.PromotionType;
				strbegindate = bo.BeginDate;
				strenddate = bo.EndDate;
				begindate = bo.BeginDate.getTime();
				enddate = bo.EndDate.getTime();
				promotionlistgroupname = bo.Promotionlistgroupname;
				promotionlistgroupid = bo.ListPromotionListGroupID;
				storeid = storeID;
				ispercentdiscount = bo.IsPercentDiscount;
				isimeipromotion = bo.IsImeiPromotion;
				isdiscount = bo.Isdiscount;
				promotionid = bo.PromotionID;
			}
		};
	}

	public PromotionOldProductBO toBO() {
		return new PromotionOldProductBO() {
			{
				Discountvalue = discountvalue;
				FromValue = fromvalue;
				ToValue = tovalue;
				PromotionType = promotiontype;
				BeginDate = strbegindate;
				EndDate = strenddate;
				Promotionlistgroupname = promotionlistgroupname;
				ListPromotionListGroupID = promotionlistgroupid;
				IsPercentDiscount = ispercentdiscount;
				IsImeiPromotion = isimeipromotion;
				Isdiscount = isdiscount;
				PromotionID = promotionid;
			}
		};
	}
}
