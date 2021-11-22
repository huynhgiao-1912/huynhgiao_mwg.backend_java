
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/4/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class PromotionPriceBO {

	public PromotionPriceBO() {
	}

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
	/// IsManualEdit
	///
	/// </summary>
	public boolean IsManualEdit;

	/// <summary>
	/// IsPriceChange
	///
	/// </summary>
	public boolean IsPriceChange;

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
