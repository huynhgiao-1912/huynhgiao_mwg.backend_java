
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Vu Quy Khi 
/// Created date 	: 3/19/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class ProductCommentBO {

	/// <summary>
	/// CommentID
	///
	/// </summary>
	public int CommentID;

	/// <summary>
	/// ROWINDEX
	///
	/// </summary>
	public int RowIndex;

	/// <summary>
	/// Title
	///
	/// </summary>
	public String Title;

	/// <summary>
	/// Content
	///
	/// </summary>
	public String Content;

	/// <summary>
	/// Email
	///
	/// </summary>
	public String Email;

	/// <summary>
	/// FullName
	///
	/// </summary>
	public String FullName;

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
	/// CategoryID
	/// Dua danh muc nganh hang vao tim cho nhanh
	/// </summary>
	public int CategoryID;

	/// <summary>
	/// ProductID
	/// san pham duoc Comment
	/// </summary>
	public int ProductID;

	/// <summary>
	/// RATING
	/// Danh gia theo thang diem tu 1->5
	/// </summary>
	public int Rating;

	/// <summary>
	/// CreatedBy
	///
	/// </summary>
	public String CreatedBy;

	/// <summary>
	/// DEALID
	/// Mã Deal
	/// </summary>
	public int DealID;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public int Vote;

	public int TotalRecord;
}
