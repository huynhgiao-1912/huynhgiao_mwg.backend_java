
package mwg.wb.model.products;
/// <summary>

import java.util.Date;

public class GCProgramBO {

	public GCProgramBO() {
	}

	/// <summary>
	/// ProgramSTR
	///
	/// </summary>
	public String ProgramSTR;

	/// <summary>
	/// StartDate
	///
	/// </summary>
	public Date StartDate;

	/// <summary>
	/// EndDate
	///
	/// </summary>
	public Date EndDate;

	/// <summary>
	/// MaxCount
	/// Tổng số giảm tối đa
	/// </summary>
	public int MaxCount;

	/// <summary>
	/// DiscountValue
	/// Giá trị giảm
	/// </summary>
	public int DiscountValue;

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
	/// UpdatedUser
	///
	/// </summary>
	public String UpdatedUser;

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

	/// <summary>
	/// DeletedUser
	///
	/// </summary>
	public String DeletedUser;

	/// <summary>
	/// DeletedDate
	///
	/// </summary>
	public Date DeletedDate;

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int ProductID;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public int GCType;

	public int DiscountPercent;
}
