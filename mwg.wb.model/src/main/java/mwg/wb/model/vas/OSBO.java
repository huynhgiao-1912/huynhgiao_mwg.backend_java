
package mwg.wb.model.vas;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 2/28/2013 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class OSBO {
	public OSBO() {
	}

	/// <summary>
	/// OSID
	/// Mã hệ điều hành (tự phát sinh)
	/// </summary>
	public int OSID;

	public String strOSName = "";// Tên hệ điều hành

	/// <summary>
	/// OSName
	/// Tên hệ điều hành
	/// </summary>

	public String strDescription = "";// Mô tả về nhóm khách hàng

	/// <summary>
	/// Description
	/// Mô tả về nhóm khách hàng
	/// </summary>

	/// <summary>
	/// OrderIndex
	/// Thứ tự hiển thị OS
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

}
