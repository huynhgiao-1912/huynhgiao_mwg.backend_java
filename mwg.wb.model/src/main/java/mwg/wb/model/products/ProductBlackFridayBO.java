
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Lê Văn Thông
/// Created date 	: 11/06/2014
/// Product
/// </summary>	

import java.util.Date;

public class ProductBlackFridayBO {
	public int STT;

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int ProductID;

	/// <summary>
	/// ProductCode
	///
	/// </summary>
	public String ProductCode;

	/// <summary>
	/// Quantity
	///
	/// </summary>
	public int Quantity;

	/// <summary>
	/// Discount
	///
	/// </summary>
	public int Discount;

	/// <summary>
	/// ProductType
	///
	/// </summary>
	public int ProductType;

	/// <summary>
	/// CreatedDate
	///
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

	public int TotalRecord;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;
}
