
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Long Trịnh 
/// Created date 	: 9/21/2015 
/// Công ty hãng SX
/// </summary>

import java.util.Date;

public class ProductCompanyBO {

	public ProductCompanyBO() {
	}

	/// <summary>
	/// CompanyID
	///
	/// </summary>
	public int CompanyID;

	/// <summary>
	/// CompanyName
	///
	/// </summary>
	public String CompanyName;

	/// <summary>
	/// NATION
	/// Quốc gia
	/// </summary>
	public String NATION;

	/// <summary>
	/// Logo
	///
	/// </summary>
	public String Logo;

	/// <summary>
	/// ListManuID
	/// List nhà sản xuất liên quan
	/// </summary>
	public String ListManuID;

	/// <summary>
	/// ListWarrantyCenterID
	/// List ID trung tâm bảo hành
	/// </summary>
	public String ListWarrantyCenterID;

	/// <summary>
	/// Description
	///
	/// </summary>
	public String Description;

	/// <summary>
	/// Website
	///
	/// </summary>
	public String Website;

	/// <summary>
	/// HotLine
	///
	/// </summary>
	public String HotLine;

	/// <summary>
	/// RefERLink
	/// Link tham khảo
	/// </summary>
	public String RefERLink;

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

	public String MainOffice;
	public String Url;
	public int HtmlID;
	public String MetaTitle;
	public String MetaDescription;
	public String MetaKeyword;
	public String Keyword;
	public String LanguageID;
	public int SiteID;
	public int TotalRecord;

	public class ProductCompanyGalleryBO {
		public int PictureID;
		public int PictureType;
		public int CompanyID;
		public String PictureName;
		public int DisplayOrder;
		public Date CreatedDate;
		public String CreatedUser;
		public Date UpdatedDate;
		public String UpdatedUser;
		public int IsDeleted;
		public Date DeletedDate;
		public String DeletedUser;
	}
}
