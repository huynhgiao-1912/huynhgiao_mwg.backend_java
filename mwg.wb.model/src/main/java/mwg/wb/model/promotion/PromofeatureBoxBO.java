
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Long Trịnh 
/// Created date 	: 11/18/2014 
/// Feature box Promotion
/// </summary>

import java.util.Date;

public class PromofeatureBoxBO {

	public PromofeatureBoxBO() {
	}

	/// <summary>
	/// FeatureBoxID
	///
	/// </summary>
	public int FeatureBoxID;

	/// <summary>
	/// PromoProgramID
	///
	/// </summary>
	public int PromoProgramID;

	/// <summary>
	/// FeatureBoxType
	/// 1: Box SP giá sốc, 2: Banner
	/// </summary>
	public int FeatureBoxType;

	/// <summary>
	/// IsShowWeb
	///
	/// </summary>
	public boolean IsShowWeb;

	/// <summary>
	/// EndDate
	///
	/// </summary>
	public Date EndDate;

	/// <summary>
	/// DisplayOrder
	///
	/// </summary>
	public int DisplayOrder;

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int ProductID;

	/// <summary>
	/// BannerImage
	///
	/// </summary>
	public String BannerImage;

	/// <summary>
	/// BannerURL
	///
	/// </summary>
	public String BannerURL;

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
	/// DeletedUser
	///
	/// </summary>
	public String DeletedUser;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
