
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Long Trịnh 
/// Created date 	: 8/5/2015 
/// Tiêu chí đánh giá
/// </summary>

import java.util.Date;

public class ProductRatingCriteriaBO {

	public ProductRatingCriteriaBO() {
	}

	/// <summary>
	/// CriteriaID
	///
	/// </summary>
	public int CriteriaID;

	/// <summary>
	/// CriteriaName
	///
	/// </summary>
	public String CriteriaName;

	/// <summary>
	/// CategoryID
	///
	/// </summary>
	public int CategoryID;

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int ProductID;

	/// <summary>
	/// FromPrice
	///
	/// </summary>
	public double FromPrice;

	/// <summary>
	/// ToPrice
	///
	/// </summary>
	public double ToPrice;

	/// <summary>
	/// DisplayOrder
	///
	/// </summary>
	public int DisplayOrder;

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

	public String ProductName;

	public String CategoryName;
}
