
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Thạch Thị Kim Xuyến 
/// Created date 	: 31/05/2013 
/// PRODUCT_HISUPDATE
/// </summary>	

import java.util.Date;

public class ProductHisUpdateBO {

	public ProductHisUpdateBO() {
	}

	/// <summary>
	/// HisUpdateID
	///
	/// </summary>
	public int HisUpdateID;

	/// <summary>
	/// ScriptID
	///
	/// </summary>
	public int ScriptID;

	public String strFullName = "";

	/// <summary>
	/// FullName
	///
	/// </summary>

	public String strCreatedUser = "";

	/// <summary>
	/// CreatedUser
	///
	/// </summary>

	/// <summary>
	/// CreatedDate
	/// Ngày sửa đổi
	/// </summary>
	public Date CreatedDate;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	/// <summary>
	/// Có đang chọn không?
	/// </summary>
	public boolean IsSelected;

	/// <summary>
	/// Có chỉnh sữa không?
	/// </summary>
	public boolean IsEdited;

	/// <summary>
	/// ProductID
	/// </summary>
	public int ProductID;

	public int TotalRecord;
}
