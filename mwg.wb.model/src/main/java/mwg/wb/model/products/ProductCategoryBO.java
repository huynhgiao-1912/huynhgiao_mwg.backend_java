
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Vu Quy Khi 
/// Created date 	: 3/19/2012 
/// Tên tiếng Việt
/// </summary>	

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import mwg.wb.model.system.KeyWordBO;

import java.util.Date;
import java.util.List;

public class ProductCategoryBO {
	/// <summary>
	/// CategoryID
	///
	/// </summary>
	public int CategoryID;

	public List<ProductCategoryBO> SubProductCategoryLst;

	/// <summary>
	/// CategoryName
	///
	/// </summary>
	public String CategoryName;

	/// <summary>
	/// Description
	///
	/// </summary>
	public String Description;
	public String DMDescription;

	/// <summary>
	/// KeyWord
	///
	/// </summary>

//	@JsonProperty("catkeyword")
	public String KeyWord;

	/// <summary>
	/// MetaKeyWord
	///
	/// </summary>
//	@JsonProperty("catmetakeyword")
	public String MetaKeyWord;

	/// <summary>
	/// MetaDescription
	///
	/// </summary>
//	@JsonProperty("catmetadescription")
	public String MetaDescription;

	/// <summary>
	/// MetaTitle
	///
	/// </summary>
//	@JsonProperty("catmetatitle")
	public String MetaTitle;

	/// <summary>
	/// URL
	///
	/// </summary>
	@JsonProperty("categoryurl")
	public String URL;

	/// <summary>
	/// TGDD_CompareTemplate
	///
	/// </summary>
	public String TGDD_CompareTemplate;

	/// <summary>
	/// ParentID
	///
	/// </summary>
	public int ParentID;

	/// <summary>
	/// NodeTree
	///
	/// </summary>
	public String NodeTree;

	/// <summary>
	/// DisplayOrder
	///
	/// </summary>
	@JsonProperty("categorydisplayorder")
	public int DisplayOrder;

	@JsonIgnore
	public int getDisplayOrder() {
		return DisplayOrder == 0 ? 99 : DisplayOrder;
	}

	/// <summary>
	/// ClassifiedCount
	///
	/// </summary>
	public int ClassifiedCount;

	/// <summary>
	/// DisplayTEMPLATE
	///
	/// </summary>
	public int DisplayTemplate;

	/// <summary>
	/// SpecialCategory
	/// Danh mục đặc biệt (SIM, Máy cũ)
	/// </summary>
	public boolean SpecialCategory;

	/// <summary>
	/// CategoryLink
	/// Liên kết tới trang ngành hàng đặc biệt
	/// </summary>
	public String CategoryLink;

	/// <summary>
	/// TooltipTEMPLATE
	/// Mẫu thiết kế của Tooltip
	/// </summary>
	public String TooltipTemplate;

	/// <summary>
	/// ICON
	/// Icon cỡ nhỏ
	/// </summary>
	public String Icon;

	/// <summary>
	/// IsActived
	///
	/// </summary>
	public boolean IsActived;
	
	public String OldListPropertyFilter;
	
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
	/// Oldcount
	///
	/// </summary>
	public int Oldcount;

	/// <summary>
	/// Sooncount
	///
	/// </summary>
	public int Sooncount;

	/// <summary>
	/// Promotioncount
	///
	/// </summary>
	public int Promotioncount;

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
	/// HTMLTEMPLATE
	/// Mẫu chi tiết 1 sản phẩm của ngành hàng
	/// </summary>
	public String HTMLTemplate;

	/// <summary>
	/// GeneralTEMPLATE
	/// Mẫu thông tin tổng quan của ngành hàng
	/// </summary>
	public String GeneralTemplate;

	/// <summary>
	/// COMPARETEMPLATE
	/// Mẫu so sánh các sản phẩm của ngành hàng
	/// </summary>
	public String CompareTemplate;

	/// <summary>
	/// ISProductCategory
	/// Là ngành hàng có bán sản phẩm
	/// </summary>
	public boolean IsProductCategory;

	/// <summary>
	/// SImage
	///
	/// </summary>
	public String SImage;

	/// <summary>
	/// MImage
	///
	/// </summary>
	public String MImage;

	/// <summary>
	/// BImage
	///
	/// </summary>
	public String BImage;

	/// <summary>
	/// ISDisplayGroup
	///
	/// </summary>
	public boolean IsDisplayGroup;

	/// <summary>
	/// SEOName
	///
	/// </summary>
	@JsonProperty("categoryseoname")
	public String SEOName;

	/// <summary>
	/// IsAccessory
	///
	/// </summary>
	public boolean IsAccessory;

	/// <summary>
	/// IsParentGroup
	///
	/// </summary>
	public boolean IsParentGroup;

	/// <summary>
	/// COMPARETEMPLATEMobile
	/// Mẫu so sánh các sản phẩm của ngành hàng trên Mobile
	/// </summary>
	public String CompareTemplateMobile;

	/// <summary>
	/// CompareTemplate_6col
	/// Mẫu so sánh các sản phẩm trên site TGDD
	/// </summary>
	public String CompareTemplate_6col;

	/// <summary>
	/// ProductKey
	///
	/// </summary>
	public int ProductKey;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	/// <summary>
	/// Đếm số lượng tin rao vặt
	/// </summary>
	public int CountClassified;

	public int ProductCount;

	public String HTMLMobile;

	public String LanguageID;

	public String CategorySEOName;

	public String ListProperty;

	public String KeyValueImage;
	/// <summary>
	/// Ngưỡng giá trị khuyến mãi để hiển thị label trên web
	/// </summary>
	public int MinPromotionPercent;
	/// <summary>
	/// Ngưỡng giá trị chênh lệch giá để hiển thị label trên web
	/// </summary>
	public int MinDiscountPercent;
	/// <summary>
	/// Danh sách ProductId của 1 ngành hàng hiển thị trang home
	/// </summary>
	public String FeaturedProductidList;

	public String ListPropertyFilter;

	/// <summary>
	/// Thuộc tính feature dùng để hiển thị giá trị nổi bật trên các ds sp
	/// </summary>
	public int FeaturePropertyID;
	/// <summary>
	/// Danh sách nhóm thuộc tính
	/// </summary>
	public List<ProductPropGrpBO> ListProductGroup;

	public List<KeyWordBO> ListKeywordBO;

	public List<ImageTypeBO> ListImageType;
	/// <summary>
	/// Text tu van chon hang
	/// </summary>
	public String ManuAdvisorText;
	/// <summary>
	/// Link tu van chon hang
	/// </summary>
	public String ManuAdvisorLink;
	/// <summary>
	/// Text tu van chon gia
	/// </summary>
	public String PriceAdvisorText;
	/// <summary>
	/// Link tu van chon gia
	/// </summary>
	public String PriceAdvisorLink;
	public String FEATURECOLORID;
	public String TimerMetaTile;
	public String TimerMetaDescription;
	public String TimerMetaKeyword;
	public Date TimerFromDate;
	public Date TimerToDate;
	public String KeywordList;
	public String OldMetaDescription;
	public String OldMetaTitle;
	public String ReturnPolicy;
	public String ParentIdList;
	public int[] RelatedIds;
	public String ListRelateCategoryID;
	public String ListPropertyTicket;
	public Integer IsRefund;
	
	public String PlatformIdList;
	
	public String autoMetaTitle;
	public String autoMetaDescription;
	public String videourl;
	public String virtualCateIdList;
	public int newFilter;
	public int propNewFilter; 
	public String ShortCodeName;
	public int relatepropertyid;
	public int productType;
	public int comparePropertyID;
	
	public String valueIDFilterList;
	public String valueIDFilterNameList;
	
	//có phải là cate con sau cùng ko
	public boolean isLeaf;

	public ProductCategoryQuickLinkBO[] productCategoryQuickLinkBO;
	
	public int GroupID;
	public String GroupName;
	public String desktopimage;
    public String mobileimage;
    public int parentcateid;
    public int IsSuggestManu;
    public int quickFilterID;
    public int additionalProperty;
    // yêu cầu a Quyền thêm
	public boolean  isManuQuickFilter;
}
