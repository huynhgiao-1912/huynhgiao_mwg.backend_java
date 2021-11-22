
package mwg.wb.model.vas;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 2/28/2013 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class ShoppingCardBO {
	public ShoppingCardBO() {
	}

	public String strShoppingCardID = "";// Mã tự gen theo yêu cầu

	/// <summary>
	/// SHOPPINGCardID
	/// Mã tự gen theo yêu cầu
	/// </summary>

	/// <summary>
	/// CreatedCustomerID
	/// Mã khách hàng tạo
	/// </summary>
	public long CreatedCustomerID;

	/// <summary>
	/// CreatedDate
	///
	/// </summary>
	public Date CreatedDate;

	/// <summary>
	/// TotalPrice
	/// Giá tổng
	/// </summary>
	public int TotalPrice;

	public String strIPCreated = "";// IP người tạo

	/// <summary>
	/// IPCreated
	/// IP người tạo
	/// </summary>

	public String strFullName = "";// Tên khách hàng

	/// <summary>
	/// FullName
	/// Tên khách hàng
	/// </summary>

	public String strPhoneNumber = "";// Số điện thoại của khách hàng

	/// <summary>
	/// PhoneNumber
	/// Số điện thoại của khách hàng
	/// </summary>

	/// <summary>
	/// Gender
	/// Giới tính 0: Nữ, 1: Nam
	/// </summary>
	public boolean Gender;

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
