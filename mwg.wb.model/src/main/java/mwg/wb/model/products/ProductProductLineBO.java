
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Long Trịnh 
/// Created date 	: 9/15/2017 
/// Dòng sản phẩm
/// </summary>

import java.util.Date;

public class ProductProductLineBO {

	public ProductProductLineBO() {
	}

	/// <summary>
	/// ProductLineID
	///
	/// </summary>
	public int ProductLineID;

	/// <summary>
	/// ProductLineName
	///
	/// </summary>
	public String ProductLineName;

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
