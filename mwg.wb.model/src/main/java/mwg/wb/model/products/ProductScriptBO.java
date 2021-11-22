
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Thạch Thị Kim Xuyến 
/// Created date 	: 31/05/2013 
/// PRODUCT_SCRIPT
/// </summary>	

import java.util.Date;

public class ProductScriptBO {

	public ProductScriptBO() {
	}

	/// <summary>
	/// ScriptID
	///
	/// </summary>
	public int ScriptID;

	public String strScriptName = "";// Khu vực được cập nhật

	/// <summary>
	/// ScriptName
	/// Khu vực được cập nhật
	/// </summary>

	public String strCreatedUser = "";

	/// <summary>
	/// CreatedUser
	///
	/// </summary>

	/// <summary>
	/// CreatedDate
	/// Ngày tham gia
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
