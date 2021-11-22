
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Nguyen Hoang Vu 
/// Created date 	: 2014-05-13 
/// 
/// </summary>

import java.util.Date;

public class ProductWarrantyManuBO {

	public ProductWarrantyManuBO() {
	}

	/// <summary>
	/// WarrantyManuID
	///
	/// </summary>
	public int WarrantyManuID;

	/// <summary>
	/// WarrantyManuName
	///
	/// </summary>
	public String WarrantyManuName;

	/// <summary>
	/// LogoImage
	///
	/// </summary>
	public String LogoImage;

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
	public String WebsiteLink;
}
