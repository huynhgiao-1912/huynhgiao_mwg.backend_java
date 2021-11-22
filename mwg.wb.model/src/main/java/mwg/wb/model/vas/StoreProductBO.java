
package mwg.wb.model.vas;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 03/16/13 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class StoreProductBO {
	public StoreProductBO() {
	}

	/// <summary>
	/// StoreID
	///
	/// </summary>
	public int StoreID;

	/// <summary>
	/// ProductD
	///
	/// </summary>
	public int ProductID;

	public String strURL = "";

	/// <summary>
	/// URL
	///
	/// </summary>

	public String strIPCreated = "";

	/// <summary>
	/// IPCreated
	///
	/// </summary>

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

	/// <summary>
	/// IsUpdated
	///
	/// </summary>
	public boolean IsUpdated;

	/// <summary>
	/// DeletedDate
	///
	/// </summary>
	public Date DeletedDate;

	public String strDeletedUser = "";

	/// <summary>
	/// DeletedUser
	///
	/// </summary>

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

	public String ProductName;

	public String FileName;

	public String Path;
}
