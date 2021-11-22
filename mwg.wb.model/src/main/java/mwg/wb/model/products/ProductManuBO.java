package mwg.wb.model.products;
/// <summary>

/// Created by 		: Vu Quy Khi 
/// Created date 	: 3/19/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_DEFAULT)
public class ProductManuBO {
	/// <summary>
	/// ManufactureID
	///
	/// </summary>
//	@JsonProperty("manufacturerid")
	public double ManufactureID;

	/// <summary>
	/// ManufacturerName
	///
	/// </summary>
	public String ManufacturerName;

	/// <summary>
	/// ClassifiedCount
	/// </summary>

	public double ClassifiedCount;
	/// <summary>
	/// CategoryID
	///
	/// </summary>
	public double CategoryID;

	/// <summary>
	/// Description
	///
	/// </summary>
	public String Description;

	/// <summary>
	/// KeyWord
	///
	/// </summary>
	public String KeyWord;

	/// <summary>
	/// MetaKeyWord
	///
	/// </summary>
	public String MetaKeyWord;

	/// <summary>
	/// MetaDescription
	///
	/// </summary>
	public String MetaDescription;

	/// <summary>
	/// MetaTitle
	///
	/// </summary>
	public String MetaTitle;

	/// <summary>
	/// URL
	///
	/// </summary>
	public String URL;

	/// <summary>
	/// DisplayOrder
	///
	/// </summary>
	public double DisplayOrder;

	@JsonIgnore
	public double getDisplayOrder() {
		return DisplayOrder == 0 ? 99d : DisplayOrder;
	}

	/// <summary>
	/// DisplayTEMPLATE
	///
	/// </summary>
	public int DisplayTemplate;

	/// <summary>
	/// SmallLogo
	///
	/// </summary>
	public String SmallLogo;

	/// <summary>
	/// CharSearch
	///
	/// </summary>
	public String CharSearch;

	/// <summary>
	/// BigLogo
	///
	/// </summary>
	public String BigLogo;

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
	/// BrandURL
	///
	/// </summary>
	public String BrandURL;

	/// <summary>
	/// ServiceCenter
	///
	/// </summary>
	public String ServiceCenter;

	/// <summary>
	/// SEOName
	///
	/// </summary>
	public String SEOName;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public ProductCategoryBO ProductCategoryBO;

	@JsonProperty("countproduct")
	public int ProductCount;

	public int ProductCountAccess;

	public int CategoryParentID;
	public String TimerMetaDescription;
	public String TimerMetaKeyWord;
	public String TimerMetaTitle;
	public Date TimerFromDate;
	public Date TimerToDate;
	public String OldMetaTitle;
	public String OldMetaDescription;
	public String Languageid;

	public String Countclassified;
	public String IntroDescription;
	public int PenWarranty;
	public int IsNotSuggestSearch;
	
}
