package mwg.wb.model.products;

import com.fasterxml.jackson.annotation.JsonIgnore;
import mwg.wb.model.cache.CacheBO;
import mwg.wb.model.installment.InstallmentBO;
import mwg.wb.model.pm.AreaWebBO;
import mwg.wb.model.pm.CurrentInStockDetailBO;
import mwg.wb.model.pm.PriceBO;
import mwg.wb.model.pm.StoreInfoBO;
import mwg.wb.model.pricestrings.PriceStringBO;
import mwg.wb.model.promotion.Promotion;
import mwg.wb.model.promotion.PromotionBHX;
import mwg.wb.model.promotion.PromotionGRBO;
import mwg.wb.model.promotion.PromotionInfoBO;
import mwg.wb.model.promotion.PromotionOldProductBO;
import mwg.wb.model.rs.AccessoryBO;
import mwg.wb.model.search.StoreStockSO;
import mwg.wb.model.vas.CustomerGroupProductBO;

import java.util.Date;
import java.util.List;

public class ProductBO extends CacheBO {
//	public ProductBO(String msg) {
//		message=msg;
//	}
//	public ProductBO( ) {
//		 
//	}
	/**
	 * ProductID
	 *
	 **/
	public int ProductID;

	/**
	 * ParentID
	 *
	 **/
	public int ParentID;

	/**
	 * Quantity
	 *
	 **/
	public int Quantity;

	/**
	 * Số thứ tự Numbers
	 **/
	public int Numbers;

	/**
	 * MaxRow Numbers
	 **/
	public int MaxRow;

	/**
	 * Tỗng record theo Catergori Totalrecord
	 **/
	public int TotalRecord;

	/**
	 * ProductName
	 *
	 **/
	public String ProductName;

	/**
	 * ProductManu
	 *
	 **/
	public ProductManuBO ProductManuBO;

	/**
	 * ProductManuLangBO
	 *
	 **/
	public ProductManuLangBO ProductManuLangBO;

	public ProductPriceRangeBO ProductPriceRangeBO;

	public List<PromotionInfoBO> PromotionInfoList;


	public List<Promotion> Promotion;

	public List<PromotionBHX> PromotionBHX;

	public List<Promotion> PromotionSample;
	public List<Promotion> PromotionAtStore;
	public List<Promotion> PromotionExt;
	public List<Promotion> PromotionInstallment;
	public List<Promotion> PromotionByTimes;
	public List<Promotion> PromotionCheckSalePrice;
	public List<Promotion> PromotionSaving;
	public List<Promotion> PromotionShockPrice;
	public List<Promotion> PromotionTotalValue;
	public List<Promotion> PromotionOnline;
	public List<Promotion> PromotionOnlineOnlyShockPrice;


	public int ProductSoldCount;

	/**
	 * ProductErpPriceBO
	 *
	 **/
	public ProductErpPriceBO ProductErpPriceBO;

	public ProductErpPriceBO[] ProductErpPriceBOList;

	public CurrentInStockDetailBO CurrentInStockDetailBO;

	public StoreInfoBO StoreInfoBO;

	public ProductCategoryLangBO ProductCategoryLangBO;

	public ProductCategoryBO ProductCategoryBO;

	public ProductLanguageBO ProductLanguageBO = new ProductLanguageBO();
	public ProductLanguageBO TGDDProductLanguageBO;

	public ProductSlaveInfo ProductSlaveInfo;

	public PriceBO PriceBO;
	public ProductColorBO ProductColorBO;
	public double Price;

	public AreaWebBO AreaWebBO;

	public AccessoryBO AccessoryBO;

	public List<CustomerGroupProductBO> CustomerGroupProductBOLst;
	/**
	 * CategoryName
	 *
	 **/
	public String CategoryName;

	/**
	 * CurrencyunitName
	 *
	 **/
	public String CurrencyunitName;

	// PackageItemUnit: unit cua san pham co so
	// public String PackageUnitName;
	// SpecialPackageItemUnit: unit lay o db, neu null thi bang unit cua sp co so
	// public String SpecialPackageItemUnit;

	/**
	 * PromotionDetail
	 *
	 **/
	public String PromotionDetail;

	/**
	 * HisPrice
	 *
	 **/
	public int HisPrice;

	/**
	 * IsShared
	 *
	 **/
	public boolean IsShared;
	public boolean HasBimage;
	/**
	 * CurrencyunitID
	 *
	 **/
	public int CurrencyunitID;

	/**
	 * PROMOTIONINFO
	 *
	 **/
	public String PromotionInfo;
	/**
	 * Description
	 *
	 **/
	public String Description;

	/**
	 * CategoryID
	 *
	 **/
	public int CategoryID;

	/**
	 * AREAID
	 *
	 **/
	public int AreaID;

	/**
	 * PRODUCTSTATUSID
	 *
	 **/
	public int ProductStatusID;

	/**
	 * Comments
	 *
	 **/
	public int Comments;

	/**
	 * RatingScore
	 *
	 **/
	public float RatingScore;

	/**
	 * RowNum
	 *
	 **/
	public int RowNum;

	/**
	 * ManufactureID
	 *
	 **/
	public double ManufactureID;

	/**
	 * KeyWord
	 *
	 **/
	public String KeyWord;

	/**
	 * MetaKeyWord
	 *
	 **/
	public String MetaKeyWord;

	/**
	 * MetaDescription
	 *
	 **/
	public String MetaDescription;

	/**
	 * MetaTitle
	 *
	 **/
	public String MetaTitle;

	/**
	 * URL
	 *
	 **/
	public String URL;

	/**
	 * DisplayOrder
	 *
	 **/
	public int DisplayOrder;

	/**
	 * HTML
	 *
	 **/
	public String HTML;

	/**
	 * HTMLDESCRIPTION
	 *
	 **/
	public String HTMLDescription;

	/**
	 * Tooltip
	 *
	 **/
	public String Tooltip;

	/**
	 * General
	 *
	 **/
	public String General;

	/**
	 * IsHot Sản phẩm được chú ý
	 **/
	public boolean IsHot;

	/**
	 * IsNew Sản phẩm mới
	 **/
	public boolean IsNew;

	/**
	 * IsSpecial Sản phẩm đặc biệt
	 **/
	public boolean IsSpecial;

	/**
	 * IsOnlineOnly Chỉ bán online
	 **/
	public boolean IsOnlineOnly;

	/**
	 * LastestGenerate Lần xuất HTML cuối cùng
	 **/

	public Date LastestGenerate;

	/**
	 * GroupID
	 *
	 **/
	public int GroupID;

	/**
	 * IsOutStore Không còn được bán nữa (nhưng vẫn hiển thị để tham khảo)
	 **/
	public boolean IsOutStore;

	/**
	 * Simage
	 *
	 **/
	public String Simage;

	/**
	 * DetailImage
	 *
	 **/
	public String DetailImage;

	/**
	 * SimageUrl
	 *
	 **/
	public String SimageUrl;

	/**
	 * Mimage
	 *
	 **/
	public String Mimage;

	/**
	 * Mimage
	 *
	 **/
	public String MimageUrl;

	/**
	 * Bimage
	 *
	 **/
	public String Bimage;

	/**
	 * Bimage
	 *
	 **/
	public String BimageUrl;

	/**
	 * IsActived
	 *
	 **/
	public boolean IsActived;
	@JsonIgnore
	public boolean isActived() {
		return IsActived ;
	}
	/**
	 * ActivedDate
	 *
	 **/
	public Date ActivedDate;

	/**
	 * ActivedUser
	 *
	 **/
	public String ActivedUser;

	public int UpdatedPartnerUserID;

	/**
	 * IsDeleted
	 *
	 **/
	public boolean IsDeleted;

	/**
	 * DeletedDate
	 *
	 **/
	public Date DeletedDate;

	/**
	 * DeletedUser
	 *
	 **/
	public String DeletedUser;

	/**
	 * CreatedDate
	 *
	 **/
	public Date CreatedDate;

	/**
	 * CreatedUser
	 *
	 **/
	public String CreatedUser;

	/**
	 * UpdatedDate
	 *
	 **/
	public Date UpdatedDate;

	/**
	 * UpdatedUser
	 *
	 **/
	public String UpdatedUser;

	/**
	 * ProductCode
	 *
	 **/
	public String ProductCode;

	/**
	 * RateCount
	 *
	 **/
	public double RateCount;

	/**
	 * Rating
	 *
	 **/
	public int Rating;
	/**
	 * RateScore
	 *
	 **/
	public double RateScore;

	/**
	 * StandardKIT
	 *
	 **/
	public String StandardKIT;

	/**
	 * WarrantyInfo
	 *
	 **/
	public String WarrantyInfo;

	/**
	 * TotalReview
	 *
	 **/
	public double TotalReview;

	/**
	 * UserGuide
	 *
	 **/
	public String UserGuide;

	/**
	 * IsVent
	 *
	 **/
	public boolean IsVent;

	/**
	 * UserGuidePDF
	 *
	 **/
	public String UserGuidePDF;

	/**
	 * SEOName
	 *
	 **/
	public String SEOName;

	/**
	 * PriceStatus
	 *
	 **/
	public String PriceStatus;

	/**
	 * ExpiredPreOrder
	 *
	 **/
	public Date ExpiredPreOrder;

	/**
	 * StrGallery
	 *
	 **/
	public String StrGallery;

	/**
	 * StrGalleryAllColor
	 *
	 **/
	public String StrGalleryAllColor;

	/**
	 * HTMLMobile
	 *
	 **/
	public String HTMLMobile;

	/**
	 * MobileSimage
	 *
	 **/
	public String MobileSimage;

	/**
	 * MobileMimage
	 *
	 **/
	public String MobileMimage;

	/**
	 * MobileBimage
	 *
	 **/
	public String MobileBimage;

	/**
	 * mobilestrgallery
	 *
	 **/
	public String MobileStrgallery;

	/**
	 * htmlwarranty
	 *
	 **/
	public String HTMLWarranty;

	/**
	 * HTMLSHIPPING
	 *
	 **/
	public String HTMLShipping;

	/**
	 * IMPORTEDDATE
	 *
	 **/
	public Date ImportedDate;

	/**
	 * ProductArrivalDate
	 *
	 **/
	public Date ProductArrivalDate;
	/**
	 * DisscountPrice
	 *
	 **/
	public double DisscountPrice;

	/**
	 * PreOrderPrice
	 *
	 **/
	public double PreOrderPrice;

	/**
	 * Price
	 *
	 **/
	public double pstore;

	/**
	 * Type
	 *
	 **/
	public int Type;

	/**
	 * Có tồn tại không
	 **/
	public boolean IsExist;

	public int StatusProduct;

	/**
	 * sản phẩm có giảm giá
	 **/
	public boolean IsDesPrice;

	/**
	 * sản phẩm có quà tặng
	 **/
	public boolean IsGift;

	public String LanguageID;

	/**
	 * IsFullWidth
	 **/
	public boolean IsFullWidth;

	public String AreaName;

	public String Specs;

	public int DisplayOrderIsBestSelling;

	public int RankByAll;

	public int DisplayOrderIsWebHightLight;

	public int SiteID;

	public String Canonical;

	public boolean IsWebShow;

	public boolean IsWebShowForOnline;

	public String ManufacturerName;

	public String ManuURL;

	public String CategoryURL;

	public double CommentCount;

	public boolean IsEvent;

	public boolean IsChecked;

	public String TemplateAlias;

	public boolean IsFeature;

	public String FeatureImage;

	public int Scenario;

	public String UrlDownload;

	// public boolean IsFree ;

	public String Image;

	public double LikeCount;

	public double DownloadCount;

	public String CustomerGroupName;

	public int CustomerGroupID;

	public String ValueName;

	public int ValueID;

	public String FileName;

	public int OSID;

	public double FileSize;

	public String ListGenre;

	public String FileVersion;

	public String OSName;

	public String VasUrl;

	public String Path;

	public int OrderIndex;

	public String StatusName;

	public int PropertyID;

	public String PropertyName;

	public String ValueVOLT;

	public int PropertyType;

	public String CombineValue;

	public String Models;

	public String Capacity;

	public int CapacityStorage;

	public String Voltage;

	public Date FeatureExpireDate;

	public String Tag;

	public String Advantage;

	public String DisAdvantage;

	public String ShortDescription;

	public String ReleaseDate;

	public String CategoryMap;

	public double ExpectedPrice;

	public boolean IsShowExpectedPrice;

	public String PurchasingInfo;

	public String ListValue;

	public String ListValueid;

	public String OsRequirement;

	public Date HotEndDate;

	public Date NewEndDate;

	public Date FeatureStartDate;

	public boolean IsHearSay;

	public boolean IsShowExpectedText;

	public String HtmlSlider;

	public String Kitimagesmall;

	public String Kitimagelarge;

	public String Specificationimage;

	public boolean IsNewsDetailVersion;

	public String TemplateFeature;

	public boolean IsMarketLink;

	public boolean IsCheckConfirmConfig;

	public String HtmlSliderMobile;

	/**
	 * ListSideID: phuc vu cho mo hinh nhieu cong ty
	 **/
	public String ListSiteID;
	/**
	 * Đặc điểm nổi bậc của sản phẩm
	 **/
	public String SpecialFeature;
	/**
	 * Lý do sản phẩm hot
	 **/
	public String HotReason;
	/**
	 * Số lượt thích sản phẩm
	 **/
	public long CountLike;

	/**
	 * Gets or sets the first photo in gallery.
	 **/
	public ProductGalleryBO FirstProductGallery;

	/**
	 * Giá trị thuộc tính nổi bật
	 **/
	public String FeaturePropertyValue;
	public int FeaturePropertyValueId;
	public int FeaturePropertyCompareValue;

	public double InstallmentPayPerMonth;
	public double RealInterestRate;
	public int SpecialInterestInstallment;
	public InstallmentBO SpecialInterestInstallmentInfo;
	public List<InstallmentBO> lstSpecialInstallmentBO;
	public boolean IsRequestImei;

	public Date ScenarioStartDate;
	public Date ScenarioEndDate;
	public Date ScenarioShowWebFromDate;
	public Date ScenarioShowWebToDate;
	public Date ScenarioFromDate;
	public Date ScenarioToDate;
	public ProductBO[] BonusProducts;

	public Date GiftPromotionBeginDate;
	public Date GiftPromotionEndDate;

	public double PackageWeight;

	public String AnnouncedYear;

	public boolean Isfree;
	public long ProductOldCount;
	/**
	 * Giá thấp nhất từ
	 **/
	public double ProductOldPriceFrom;
	/**
	 * Giá cao nhất
	 **/
	public double ProductOldPriceTo;

	/**
	 * for redis store
	 **/
	public boolean IsHavePromotion;
	/**
	*
	**/
	public List<PromotionOldProductBO> lstPromotionBO;
	/**
	*
	**/
	public int MaxDiscountValue;

	public int PinWarranty;

	public int Warranty;
	public int Refrigerationwarranty;
	public int Compressorswarranty;

	public String ListNewsId;
	public String ListProductId;

	public int RelatePropertyID;
	public int ComparePropertyID;
	public String BTU;
	public int RelativeVersion;

	public String ReplaceProducts;

	public boolean IsEasyDeal;
	public boolean IsSmartPhone;

	public boolean IsCollection;

	public int CollectionID;

	public int CollectionProductCount;

	public String BRepresentImage;
	public String MRepresentImage;
	public String SRepresentImage;

	public String LuckyDrawAccompanyingPromotions;

	public String ExtendInfo;

	public int ProvinceCount;

	/**
	 * 1: Không giao 2: Xe máy 3: Xe tải
	 **/
	public int DeliveryVehicles;

	public String ListCateFeatureID;
	public String TopImage;
	public Date ShowHomeEndDate;
	public String PropValueInName;
	public String PropIDInName;
	public String Shortname;
	public String UtilityValueIDList;
	public List<ProductDetailBO> ProductDetailUtilityList;
	public boolean IsLandingPageOnDetail;
	public String RedirectURL;
	public Date RedirectFromDate;
	public Date RedirectToDate;
	public boolean IsPayment;
	public Date Paymentfromdate;
	public Date Paymenttodate;
	public double PercentInstallment;
	// may cu
	public boolean OldIsPayment;
	public Date OldPaymentfromdate;
	public Date OldPaymenttodate;
	public double OldPercentInstallment;

	public String TGDDKitimagesmall;
	public String TGDDKitimagelarge;
	public String TimerMetaTile;
	public String TimerMetaDescription;
	public String TimerMetaKeyword;
	public Date TimerFromDate;
	public Date TimerToDate;
	public Date ShowhomeStartDate;
	public String ProvinceIDList;
	public int IsPartnerProduct;
	public String Comboproductidlist;
	public int RepresentProductID;
	public ProductBO RepresentProductBO;
	public String RepresentProductName;
	public boolean IsRepresentProduct;
	public boolean IsKey;
	public String FeaturePropertyName;
	public String RelatedCategoryIDList;
	public String DetailSuggestDescription;
	public String SearchSuggestDescription;
	public Date SuggestFromDate;
	public Date SuggestToDate;
	public List<String> ListProductIdCombo;
	public double RoundLevel;
	public double RoundLevelValue;
	public Date RoundLevelLastUpdate;

	public boolean IsWarrantyHome;
	public int MainGroupID;
	public int SubGroupID;
	public int WebStatusID;
	public String TimerThumbImage;
	public Date TimerThumbImageFromDate;
	public Date TimerThumbImageToDate;
	public String ProductUrl;
	public String ClassifiedCount;
	public int AreadID;
	public String Configuration;
	public int IsOnlineOnlyprice;
	public int IsMainPrice;
	public String Mobile_Simage;
	public String Mobile_Mimage;
	public String Mobile_Bimage;
	public String Product_Code;
	public double OnlinerePaymentPrice;
	public String VideoTag;
	public int IsAccessory;
	public String GeneralKeyword;
	public boolean isoriginalaccessories;
	public int orgmanuid;
	public int ShockPriceType;

	// public String urldownload;
	// public int isoutstore;

	public SpecialSaleProgramBO SpecialSaleProgram;

	// dong ho cap
	public boolean IsCoupleWatch;
	public ProductBO[] ListCoupleWatchBO;
	public int[] ListCoupleWatchProductID;

	public int productType;

	// ham dung chung voi api object
	@JsonIgnore
	public ProductErpPriceBO getPrice() {
		return ProductErpPriceBO;
	}

	@JsonIgnore
	public ProductErpPriceBO[] getPriceList() {
		return ProductErpPriceBOList;
	}

	public List<StoreStockSO> StoreList;

	public String StickerLabel;

	public boolean isSetupProduct;

	public PriceStringBO[] priceStrings;

	public String priceDefaultString;

	public PromotionGRBO[] promotionStrings;

//	public ProductDetailGRBO cachedDetail;

	// Error reporting
	public String message;
	public String[] stackTrace;

	// public Date ComboToDate;
	// public Date ComboFromDate;

	public ProductStickerLabelBO productStickerLabelBO;

	public PMProductBO[] pmProductBOList;

	public ProductDetailGRBO cachedDetail;

	public int erpBrandID;

	@JsonIgnore
	public String promoGroupRecordID;

	@JsonIgnore
	public boolean getSubBrandPromotion;

	// multi
	public boolean IsMultiAirConditioner;
	public String MultiAirConditionerIdsAllTime;
	public String[] MultiAirConditionerIds;
	// public String[] MultiAirConditionerIdList;
	public ProductBO[] MultiAirConditioners;
	public ProductBO[] CombinedProducts;

	public int ProvinceID;
	
	public String MonopolyLabel;
	
	public ProductPropUseManualBO[] PropManual;

	public boolean IsBestSelling;
	public String shortNameProcessed;
	public String shortNameProp;
	public PriceStringBO[] pricebyproductcode;
	
	
	public int getDisplayOrder() {
		return DisplayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		DisplayOrder = displayOrder;
	}
}
