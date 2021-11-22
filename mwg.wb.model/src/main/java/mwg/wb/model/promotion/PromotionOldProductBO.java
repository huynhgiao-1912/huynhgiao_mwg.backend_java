
package mwg.wb.model.promotion;
/// <summary>

/// Creator: Đức Định
/// Date: 13/02/2014
/// Khuyến mãi cho máy đổi trả
/// </summary>

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import mwg.wb.model.common.Upsertable;

@JsonInclude(Include.NON_DEFAULT)
public class PromotionOldProductBO implements Upsertable {
	public int PromotionID;

	public String PromotionName;

	public Date BeginDate;

	public Date EndDate;

	public int ListPromotionListGroupID;

	public String Promotionlistgroupname;

	public boolean Isdiscount;

	public boolean IsImeiPromotion;

	public int Discountvalue;

	public boolean IsPercentDiscount;

	public double FromValue;

	public double ToValue;

	public int PromotionType;

	public String ProductCode, Imei;

	public int InventoryStatusID;
	

	public int StoreID;

	public int SiteID;

	public boolean IsDeleted;
	public String GroupID;
	public int IsOnlyAppyForImei;
	public String RecordID, RecordIDCommon;
	
	public String ProductID;

	@Override
	public String indexValue() {
		return RecordID;
	}
}
