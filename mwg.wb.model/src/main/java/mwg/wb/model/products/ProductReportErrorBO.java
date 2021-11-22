package mwg.wb.model.products;
/// <summary>

/// Created by 		: Long Trịnh 
/// Created date 	: 10/24/2014 
/// Báo lỗi, góp ý trang chi tiết sản phẩm
/// </summary>

import java.util.Date;

public class ProductReportErrorBO {

	public ProductReportErrorBO() {
	}

	/// <summary>
	/// ReportErrorID
	///
	/// </summary>
	public int ReportErrorID;

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int ProductID;

	/// <summary>
	/// FullName
	///
	/// </summary>
	public String FullName;

	/// <summary>
	/// Email
	///
	/// </summary>
	public String Email;

	/// <summary>
	/// PhoneNumber
	///
	/// </summary>
	public String PhoneNumber;

	/// <summary>
	/// ErrorContent
	///
	/// </summary>
	public String ErrorContent;

	/// <summary>
	/// IPCreator
	///
	/// </summary>
	public String IPCreator;

	/// <summary>
	/// CreatedDate
	///
	/// </summary>
	public Date CreatedDate;

	/// <summary>
	/// CreatedDateKey
	///
	/// </summary>
	public int CreatedDateKey;

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
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
