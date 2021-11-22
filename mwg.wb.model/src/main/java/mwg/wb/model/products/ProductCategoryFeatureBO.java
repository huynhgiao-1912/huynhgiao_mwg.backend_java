
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Long Trịnh 
/// Created date 	: 1/11/2016 
/// Feature sản phẩm
/// </summary>

import java.util.Date;

public class ProductCategoryFeatureBO {

	public ProductCategoryFeatureBO() {
	}

	/// <summary>
	/// FeatureID
	///
	/// </summary>
	public int FeatureID;

	/// <summary>
	/// CategoryID
	///
	/// </summary>
	public int CategoryID;

	/// <summary>
	/// FeatureName
	///
	/// </summary>
	public String FeatureName;

	/// <summary>
	/// Image
	///
	/// </summary>
	public String Image;

	/// <summary>
	/// FeatureContent
	///
	/// </summary>
	public String FeatureContent;

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
