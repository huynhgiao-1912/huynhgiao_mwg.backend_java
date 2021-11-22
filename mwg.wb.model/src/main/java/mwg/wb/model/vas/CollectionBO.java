
package mwg.wb.model.vas;
/// <summary>

/// Created by 		: Trịnh Văn Long 
/// Created date 	: 5/9/2013 
/// Bộ sưu tập
/// </summary>	

import java.util.Date;

public class CollectionBO {

	public CollectionBO() {
	}

	/// <summary>
	/// CollectionID
	/// Mã bộ sưu tập game
	/// </summary>
	public int CollectionID;

	/// <summary>
	/// CollectionName
	/// Tên bộ sưu tập
	/// </summary>
	public String CollectionName;

	/// <summary>
	/// OSID
	/// Hệ điều hành (1: Ios, 2: Android, 3: Window phone)
	/// </summary>
	public int OSID;

	/// <summary>
	/// AVATARImage
	/// Hình đại diện
	/// </summary>
	public String AVATARImage;

	/// <summary>
	/// DetailImage
	/// Hình chi tiết
	/// </summary>
	public String DetailImage;

	/// <summary>
	/// IsActived
	/// Đã kích hoạt
	/// </summary>
	public boolean IsActived;

	/// <summary>
	/// ActivedDate
	///
	/// </summary>
	public Date ActivedDate;

	/// <summary>
	/// ActivedUser
	///
	/// </summary>
	public String ActivedUser;

	/// <summary>
	/// UpdatedDate
	///
	/// </summary>
	public Date UpdatedDate;

	/// <summary>
	/// UpdatedUser
	///
	/// </summary>
	public String UpdatedUser;

	/// <summary>
	/// CreatedUser
	///
	/// </summary>
	public String CreatedUser;

	/// <summary>
	/// CreatedDate
	/// Ngày tham gia
	/// </summary>
	public Date CreatedDate;

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

	/// <summary>
	/// DeletedUser
	///
	/// </summary>
	public String DeletedUser;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public int TotalRecord;

}
