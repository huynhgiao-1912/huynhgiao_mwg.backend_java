
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/4/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class PromotionListBO {

	public PromotionListBO() {
	}

	/// <summary>
	/// PromotionListGroupID
	///
	/// </summary>
	public int PromotionListGroupID;

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public String ProductID;

	/// <summary>
	/// Quantity
	///
	/// </summary>
	public int Quantity;

	/// <summary>
	/// InputTime
	///
	/// </summary>
	public Date InputTime;

	/// <summary>
	/// IsCombo
	///
	/// </summary>
	public boolean IsCombo;

	/// <summary>
	/// IsPercentCalc
	///
	/// </summary>
	public boolean IsPercentCalc;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
