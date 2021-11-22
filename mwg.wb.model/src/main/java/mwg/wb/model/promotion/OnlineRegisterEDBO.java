
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 12/14/2012 
/// Coupon
/// </summary>	

import java.util.Date;

public class OnlineRegisterEDBO {
	public OnlineRegisterEDBO() {
	}

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int ProductID;

	public String strPhoneNumber = "";

	/// <summary>
	/// DateCreated
	///
	/// </summary>
	public Date DateCreated;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	/// <summary>
	/// Có đang chọn không?
	/// </summary>
	public boolean IsSelected;

	/// <summary>
	/// Có chỉnh sữa không?
	/// </summary>
	public boolean IsEdited;

}
