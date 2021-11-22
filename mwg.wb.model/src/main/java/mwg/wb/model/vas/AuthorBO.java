
package mwg.wb.model.vas;
/// <summary>

/// Created by 		: Nguyễn Viết Hưng 
/// Created date 	: 12/5/2016
/// Music - Author
/// </summary>	

import java.util.Date;

public class AuthorBO {
	public AuthorBO() {
	}

	// AuthorId
	public int AuthorID;

	// AuthorName - Tên nhạc sĩ

	public String strAuthorName = "";// Tên nhạc sĩ

	/// <summary>
	/// OrderIndex
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

	public String Image;
	public int TotalMusicID;

	public long TotalFileSize;

	// ContentSRH
	public String strContentSRH = "";

	public int TotalRecord;
}
