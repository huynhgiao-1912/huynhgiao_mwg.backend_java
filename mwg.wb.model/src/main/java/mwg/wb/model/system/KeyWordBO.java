
package mwg.wb.model.system;

import java.util.Date;

/// <summary>
/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 7/25/2012 
/// Từ khóa
/// </summary>
public class KeyWordBO {

	public KeyWordBO() {
	}
	/// <summary>
	/// Ðôi tuong message
	/// </summary>

	/// <summary>
	/// KeyWordID
	///
	/// </summary>
	public int KeyWordID;

	/// <summary>
	/// CategoryID
	///
	/// </summary>
	public int CategoryID;

	/// <summary>
	/// SiteID
	///
	/// </summary>
	public int SiteID;

	/// <summary>
	/// KeyWord
	///
	/// </summary>
	public String KeyWord;

	/// <summary>
	/// Categoryname
	///
	/// </summary>
	public String Categoryname;

	/// <summary>
	/// SiteURL
	///
	/// </summary>
	public String SiteURL;

	/// <summary>
	/// URL
	///
	/// </summary>
	public String URL;

	/// <summary>
	/// CreatedUser
	///
	/// </summary>
	public String CreatedUser;

	/// <summary>
	/// CreatedDate
	///
	/// </summary>
	public Date CreatedDate;

	/// <summary>
	/// UpdatedUser
	///
	/// </summary>
	public String UpdatedUser;

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
	/// DeletedUser
	///
	/// </summary>
	public String DeletedUser;

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

	public int RowNumber;

	public int DisplayOrder;
}
