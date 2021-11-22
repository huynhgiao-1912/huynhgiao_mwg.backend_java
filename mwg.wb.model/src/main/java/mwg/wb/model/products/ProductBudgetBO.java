
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/11/2012 
/// Product
/// </summary>	

import java.util.Date;

public class ProductBudgetBO {
	/// <summary>
	/// BudgetID
	/// ID budget
	/// </summary>
	public int BudgetID;

	/// <summary>
	/// BeginDate
	/// Ngày bắt đầu
	/// </summary>
	public Date BeginDate;

	/// <summary>
	/// EndDate
	/// Ngày kết thúc
	/// </summary>
	public Date EndDate;

	/// <summary>
	/// BudgetValue
	/// Yêu cầu phải đạt
	/// </summary>
	public int BudgetValue;

	/// <summary>
	/// CategoryID
	/// Ngành hàng
	/// </summary>
	public int CategoryID;

	/// <summary>
	/// RealValue
	/// Thực tế đạt được
	/// </summary>
	public int RealValue;

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

}
