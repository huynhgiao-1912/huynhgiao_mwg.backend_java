
package mwg.wb.model.vas;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 03/16/13 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class StoreMusicBO {
	public StoreMusicBO() {
	}

	/// <summary>
	/// StoreID
	///
	/// </summary>
	public int StoreID;

	/// <summary>
	/// MusicID
	///
	/// </summary>
	public int MusicID;

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
	/// Có đang chọn không?
	/// </summary>
	public boolean IsUpdated;

	/// <summary>
	/// Có chỉnh sữa không?
	/// </summary>
	public boolean IsEdited;

	public String MusicFileName;

	public String MusicName;

	public String Path;
}
