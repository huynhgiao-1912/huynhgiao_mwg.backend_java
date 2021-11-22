
package mwg.wb.model.pm;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/7/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class AreaBO {

	public AreaBO() {
	}

	/// <summary>
	/// AreaID
	/// Mã khu vực
	/// </summary>
	public int AreaID;

	/// <summary>
	/// AreaName
	/// Tên khu vực
	/// </summary>
	public String AreaName;

	/// <summary>
	/// IsDelete
	///
	/// </summary>
	public boolean IsDelete;

	/// <summary>
	/// UserDelete
	///
	/// </summary>
	public String UserDelete;

	/// <summary>
	/// DateDelete
	///
	/// </summary>
	public Date DateDelete;

	/// <summary>
	/// OrderIndex
	/// Thứ tự
	/// </summary>
	public int OrderIndex;

	/// <summary>
	/// IsSystem
	///
	/// </summary>
	public boolean IsSystem;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
