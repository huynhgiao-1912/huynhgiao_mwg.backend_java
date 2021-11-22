
package mwg.wb.model.products;
/// <summary>

import java.util.Date;

public class PPromotionProgramBO {

	public PPromotionProgramBO() {
	}

	/// <summary>
	/// ProgramID
	///
	/// </summary>
	public int ProgramID;

	/// <summary>
	/// ProgramName
	///
	/// </summary>
	public String ProgramName;

	/// <summary>
	/// Description
	/// Nội dung khuyến mãi
	/// </summary>
	public String Description;

	/// <summary>
	/// DetailLink
	/// Link chi tiết
	/// </summary>
	public String DetailLink;

	/// <summary>
	/// CategoryID
	///
	/// </summary>
	public int CategoryID;

	/// <summary>
	/// ManufacturerID
	///
	/// </summary>
	public int ManufacturerID;

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int ProductID;

	/// <summary>
	/// FromDate
	///
	/// </summary>
	public Date FromDate;

	/// <summary>
	/// ToDate
	///
	/// </summary>
	public Date ToDate;

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
	/// ContentSRH
	///
	/// </summary>
	public String ContentSRH;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public int TotalRecord;

	public String CategoryName;

	public String ManufacturerName;

	public String DMDetailLink;
}
