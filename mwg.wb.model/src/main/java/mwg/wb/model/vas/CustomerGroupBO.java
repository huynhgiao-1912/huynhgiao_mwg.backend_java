
package mwg.wb.model.vas;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 2/28/2013 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class CustomerGroupBO {
	public CustomerGroupBO() {
	}

	/// <summary>
	/// CustomerGroupID
	///
	/// </summary>
	public int CustomerGroupID;

	public String strCustomerGroupName = "";// Danh sách nhóm khách hàng

	/// <summary>
	/// CustomerGroupName
	/// Danh sách nhóm khách hàng
	/// </summary>

	public String strDescription = "";// Mô tả về nhóm khách hàng

	/// <summary>
	/// Description
	/// Mô tả về nhóm khách hàng
	/// </summary>

	/// <summary>
	/// OrderIndex
	/// Thứ tự hiển thị nhóm khách hàng
	/// </summary>
	public int OrderIndex;

	public String strCreatedUser = "";

	/// <summary>
	/// CreatedUser
	///
	/// </summary>

	/// <summary>
	/// CreatedDate
	///
	/// </summary>
	public Date CreatedDate;

	public String strUpdatedUser = "";

	/// <summary>
	/// UpdatedUser
	///
	/// </summary>

	/// <summary>
	/// UpdatedDate
	///
	/// </summary>
	public Date UpdatedDate;

	/// <summary>
	/// IsDeleted
	///
	/// </summary>
	public boolean IsDeleted;

	public String strDeletedUser = "";

	/// <summary>
	/// DeletedUser
	///
	/// </summary>

	/// <summary>
	/// DeletedDate
	///
	/// </summary>
	public Date DeletedDate;

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

	public String ActivedUser;

	public boolean IsActived;
}
