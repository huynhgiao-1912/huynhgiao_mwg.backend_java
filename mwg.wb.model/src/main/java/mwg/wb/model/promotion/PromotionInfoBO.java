
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/4/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class PromotionInfoBO {

	public PromotionInfoBO() {
	}
	/// <summary>

	/// <summary>
	/// PromotionInfoID
	///
	/// </summary>
	public String PromotionInfoID;

	/// <summary>
	/// AreaID
	///
	/// </summary>
	public int AreaID;

	/// <summary>
	/// Mã sản phẩm ERP - Product Code bên WEB
	/// </summary>
	public String ProductID;

	/// <summary>
	/// Product ID WEB
	/// </summary>
	public int ProductIDRef;

	/// <summary>
	/// PromotionID
	///
	/// </summary>
	public int PromotionID;

	/// <summary>
	/// Description
	///
	/// </summary>
	public String Description;

	/// <summary>
	/// InputTime
	///
	/// </summary>
	public Date InputTime;

	/// <summary>
	/// ShortDescription
	/// Mô tả ngắn
	/// </summary>
	public String ShortDescription;

	/// <summary>
	/// IsShowHome
	///
	/// </summary>
	public boolean IsShowHome;

	/// <summary>
	/// OrderIndex
	///
	/// </summary>
	public int OrderIndex;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public String AreaName;

	public PromotionProgramBO PromotionProgramBO;

	public double ToValue;
	public double FromValue;

	public boolean IsGift;

	public int PromotionType;

	public double PromotionValue;
	/// <summary>
	///
	/// </summary>
	public String PromotionlistgroupID;
	public boolean IsLimitPromotionTimes;
	public int ProvinceId;
	public boolean IsBetterPromotion;
	public double FromPrice;
	public double ToPrice;
	public boolean IsDiscountPercent;

	
}
