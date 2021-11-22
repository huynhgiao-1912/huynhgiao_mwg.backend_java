
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/4/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class PromotionPriceHistoryBO {

	public PromotionPriceHistoryBO() {
	}

	/// <summary>
	/// PricePromotionHistoryID
	///
	/// </summary>
	public String PricePromotionHistoryID;

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public String ProductID;

	/// <summary>
	/// Price
	///
	/// </summary>
	public double Price;

	/// <summary>
	/// IsPriceChange
	///
	/// </summary>
	public boolean IsPriceChange;

	/// <summary>
	/// IsManualEdit
	///
	/// </summary>
	public boolean IsManualEdit;

	/// <summary>
	/// LastUpdate
	///
	/// </summary>
	public Date LastUpdate;

	/// <summary>
	/// UserUpdate
	///
	/// </summary>
	public String UserUpdate;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
