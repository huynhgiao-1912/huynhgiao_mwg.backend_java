
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 8/16/2012 
/// Tình trạng máy cũ
/// </summary>	

import java.util.Date;

public class StatusConfigBO {

	public StatusConfigBO() {
	}

	/// <summary>
	/// StatusConfigID
	/// ID Status
	/// </summary>
	public int StatusConfigID;

	/// <summary>
	/// StatusName
	/// Tên trạng thái
	/// </summary>
	public String StatusName;

	/// <summary>
	/// StatusValue
	/// Value ID tương ứng
	/// </summary>
	public String StatusValue;

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

}
