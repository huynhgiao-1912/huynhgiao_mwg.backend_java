
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/11/2012 
/// Product
/// </summary>	

import java.util.Date;

public class ProductHelpBO {
	/// <summary>
	/// ContentID
	/// Mã nội dung (Mã thuộc tính, Mã nhóm ttính, ...)
	/// </summary>
	public int ContentID;

	/// <summary>
	/// ContentType
	/// Loại nội dung (1=Group, 2=Property, 3=xxx)
	/// </summary>
	public int ContentType;

	/// <summary>
	/// Title
	/// Title của help
	/// </summary>
	public String Title;

	/// <summary>
	/// Description
	/// Mô tả hiển thị trên balloontip khi mouseover
	/// </summary>
	public String Description;

	/// <summary>
	/// Content
	/// Nội dung hiển thị trên trang help khi click popup window
	/// </summary>
	public String Content;

	/// <summary>
	/// IsActived
	///
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
	/// CreatedDate
	///
	/// </summary>
	public Date CreatedDate;

	/// <summary>
	/// CreatedUser
	///
	/// </summary>
	public String CreatedUser;

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
	/// LanguageID
	///
	/// </summary>
	public String LanguageID;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
