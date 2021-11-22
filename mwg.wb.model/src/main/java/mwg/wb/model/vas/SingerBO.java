
package mwg.wb.model.vas;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 1/29/2013 
/// Music
/// </summary>	

import java.util.Date;

public class SingerBO {
	public SingerBO() {
	}

	/// <summary>
	/// SingerID
	///
	/// </summary>
	public int SingerID;

	public String strSingerName = "";// Tên ca sĩ

	/// <summary>
	/// SingerName
	/// Tên ca sĩ
	/// </summary>

	public String strDescription = "";

	/// <summary>
	/// Description
	///
	/// </summary>

	/// <summary>
	/// OrderIndex
	///
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

	public String SingerNameMap;
}
