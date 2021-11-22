
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/10/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ProductPriceRangeBO {
	/// <summary>
	/// RangeID
	///
	/// </summary>
	public int RangeID;

	/// <summary>
	/// CountProduct
	///
	/// </summary>
	public int CountProduct;
	/// <summary>
	/// CategoryID
	///
	/// </summary>
	public int CategoryID;

	/// <summary>
	/// RangeName
	///
	/// </summary>
	public String RangeName;

	/// <summary>
	/// From
	///
	/// </summary>
	public int From;

	/// <summary>
	/// To
	///
	/// </summary>
	public int To;

	/// <summary>
	// CountSim
	///
	/// </summary>
	public int CountSim;
	/// <summary>
	/// DisplayOrder
	///
	/// </summary>
	public int DisplayOrder;

	@JsonIgnore
	public int getDisplayOrder() {
		return DisplayOrder == 0 ? 99 : DisplayOrder;
	}

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
	/// ProductCount
	///
	/// </summary>
	public int ProductCount;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;
	public String MetaTitle;
	public String MetaDescription;
	public String DisplayName;
}
