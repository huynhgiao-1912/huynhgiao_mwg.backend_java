
package mwg.wb.model.vas;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 03/14/13 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class AlbumBO {
	public AlbumBO() {
	}

	/// <summary>
	/// AlbumID
	///
	/// </summary>
	public int AlbumID;

	public String strAlbumName = "";

	/// <summary>
	/// AlbumName
	///
	/// </summary>

	public String strAlbumNameMap = "";// Tên tiếng việt không có dấu của Album

	/// <summary>
	/// AlbumNameMap
	/// Tên tiếng việt không có dấu của Album
	/// </summary>

	public String strDescription = "";

	/// <summary>
	/// Description
	///
	/// </summary>

	/// <summary>
	/// DisplayOrder
	///
	/// </summary>
	public int DisplayOrder;

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
