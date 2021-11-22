
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Trịnh Văn Long 
/// Created date 	: 24/12/2013 
/// Landing page khuyến mãi
/// </summary>

import java.util.Date;

public class PRMPromotionProgramBO {

	public PRMPromotionProgramBO() {
	}

	/// <summary>
	/// PromotionID
	///
	/// </summary>
	public int PromotionID;

	/// <summary>
	/// CategoryID
	///
	/// </summary>
	public int CategoryID;

	/// <summary>
	/// PromotionName
	///
	/// </summary>
	public String PromotionName;

	/// <summary>
	/// BeginDate
	///
	/// </summary>
	public Date BeginDate;

	/// <summary>
	/// EndDate
	///
	/// </summary>
	public Date EndDate;

	/// <summary>
	/// Description
	///
	/// </summary>
	public String Description;

	/// <summary>
	/// BackgroundImage
	///
	/// </summary>
	public String BackgroundImage;

	/// <summary>
	/// BackgroundColor
	///
	/// </summary>
	public String BackgroundColor;

	/// <summary>
	/// Banner
	///
	/// </summary>
	public String Banner;

	/// <summary>
	/// ProductIDList
	///
	/// </summary>
	public String ProductIDList;

	/// <summary>
	/// Image
	///
	/// </summary>
	public String Image;

	/// <summary>
	/// BannerMobile
	///
	/// </summary>
	public String BannerMobile;

	/// <summary>
	/// SEOTitle
	///
	/// </summary>
	public String SEOTitle;

	/// <summary>
	/// SEODescription
	///
	/// </summary>
	public String SEODescription;

	/// <summary>
	/// SEOKeyWord
	///
	/// </summary>
	public String SEOKeyWord;

	/// <summary>
	/// FacebookURL
	///
	/// </summary>
	public String FacebookURL;

	/// <summary>
	/// FacebookImage
	///
	/// </summary>
	public String FacebookImage;

	/// <summary>
	/// FacebookTitle
	///
	/// </summary>
	public String FacebookTitle;

	/// <summary>
	/// FacebookDescription
	///
	/// </summary>
	public String FacebookDescription;

	/// <summary>
	/// Content
	/// Nội dung bài viết
	/// </summary>
	public String Content;

	/// <summary>
	/// CreatedUser
	///
	/// </summary>
	public String CreatedUser;

	/// <summary>
	/// CreatedDate
	///
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
	/// ContentSRH
	///
	/// </summary>
	public String ContentSRH;

	public int TotalRecord;
	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public int CommentType;

	public String CommentNote;
}
