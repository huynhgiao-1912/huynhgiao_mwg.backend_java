
package mwg.wb.model.vas;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 03/14/13 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class TopBO {
	public TopBO() {
	}

	/// <summary>
	/// ToPID
	///
	/// </summary>
	public int TopID;

	public String strTopName = "";

	/// <summary>
	/// ToPName
	///
	/// </summary>

	public String strDescription = "";// Mô tả về loại top

	/// <summary>
	/// Description
	/// Mô tả về loại top
	/// </summary>

	/// <summary>
	/// DisplayOrder
	/// Thứ tự hiển thị
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
