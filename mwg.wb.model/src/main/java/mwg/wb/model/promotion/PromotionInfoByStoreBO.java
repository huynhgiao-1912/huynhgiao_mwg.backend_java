
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/4/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class PromotionInfoByStoreBO {

	public PromotionInfoByStoreBO() {
	}

	/// <summary>
	/// PromotionInfoByStoreID
	///
	/// </summary>
	public String PromotionInfoByStoreID;

	/// <summary>
	/// StoreID
	///
	/// </summary>
	public int StoreID;

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public String ProductID;

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
	/// Mô t? ng?n
	/// </summary>
	public String ShortDescription;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
