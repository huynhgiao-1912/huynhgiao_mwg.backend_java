package mwg.wb.model.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import mwg.wb.model.products.ProductCombo;

import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSO extends ISO {
//	public List<PromotionInfoBO> ListPromotionBO;
//	public List<ProductDetailBO> ListProductDetailsBO;
//	public List<ProductColorBO> ListColorBO;
//	public List<ProductGalleryBO> ListProductGalleryBO;
//	public List<ProductGallery360BO> ListProductGallery360BO;
//	public List<ProductVideoBO> ListProductVideoBO;
//	public List<ProductVideoShowBO> ListProductVideoShowBO;
//	public List<ProductSliderBO> ListSlideBO;
//	public List<CMSPromotion> ListCmsPromotionsBO;
//	public Map<String, ProductErpPriceBO> ListProductErpPriceBO;
//	public ProductLanguageBO ProductLanguageBO;
//	public ProductBO ProductBO;

	public PromotionSO[] PromotionSoList;
	public ProductCombo[] ListProductCombo;
	public int ProductID;
	// SO data

	public String RD_KEY;

	public String Keyword_VN;
	public String ProductName;
//public double _score;
	public int ManufactureID;
	public String ManufactureCode;
	public int CategoryID;
	public String CategoryMap;
	public int intCategoryMap;
	public String CategoryName;
	public String NewMainKeyword;
	public String NewSubKeyword;
	
	public int ViewCount;
	public int ViewCountLast7Days;
	public int HasPromotion;
	public Map<String, Object> Prices;
	public Map<String, Object> PricesBHX;
	public Map<String, Object> PropVal;
	public Map<String, Double> rangeProp;
	public Date DateCreated;
	public String ProductIDList;
	public String ListSiteID;
	public int IsFeature;
	public int Order;
	public int HasBimage;
	public int IsCollection;
	public int CollectionID;
	public int ProductSoldCount;
	public String ListSizeGroupID;

	public int CollectionProductCount;
	public int AccessoryCount;
	public int IsGeneralKeyword;
	public int IsShortDescription;
	public String GeneralKeyword;
	public String PropStr;
	public List<Prop> listproperty;

	public String PropStrSe;
	public Date PaymentFromDate;
	public Date PaymentToDate;
	public int IsPayment;
	public double PercentInstallment;

	public Date ShowHomeStartDate;
	public Date ShowHomeEndDate;
	public int IsDetailImage;

	public Map<String, Object> PromotionCallcenterList;
	public String ProductCode;
	public List<String> ListProductCode;

	public Date ScenarioFromDate;
	public Date ScenarioToDate;
	public int Scenario;
	public int IsRepresentProduct;
	public int PresentProductID;
	public String ComboId;
	public String ComboProductIds;
	public int IsCombo;
	public String MainKeyword;

	public boolean NoImage;
	public boolean NoDescription;
	public int DisplayOrder;
	public int ShockPriceType;
	public int AnnouncedYear;
	public String NewKeyword;
	public boolean isMonoPoly; // này ko sài nữa
	public boolean IsMonopolyCms; //này là ismartphone
	public boolean IsMonopolyLabel;// này là sp độc quyền thường dùng chocate ict

	public boolean IsShowHome;
	public int IsShowWeb;
	public int ShowHomeDisplayOrder;

	public int ProductType;

	public Date FeatureStartDate;
	public Date FeatureExpireDate;

	public int SliderStatus;
	public int IsReferAccessory;

	public String DocumentKeyTerm;
	public Object ReferAccessory;
	public boolean IsOnlineOnly;

	public Date OnlineOnlyFromDate;
	public Date OnlineOnlyToDate;

	public boolean IsCoupleWatch;
	public String CoupleWatchIDs;

	public int PromotionDiscountPercent;
	public double PriceAfterPromotion;

	public String StickerLabel;

	public String ManufacturerName;
	public int ProductCodeTotalQuantity;

	// multi
	public boolean IsMultiAirConditioner;
	public String MultiAirConditionerIds;

	public boolean Appliance2142Categories;
	
	
	@JsonIgnore
	public double discountValue(int provinceID) {
		if (Prices == null || Prices.get("Price_" + provinceID) == null || Prices.get("Price402_" + provinceID) == null)
			return 0d;
		return 1 - ((double) Prices.get("Price_" + provinceID) / (double) Prices.get("Price402_" + provinceID));
	}
	
	public int DeliveryVehicles;
//	public String ManufactureName;
	public Date AccessoriesStartDate;
	public Date AccessoriesEndDate;
	public boolean AccessoriesIsShowHome;
	public int AccessoriesDisplayOrder;
	
	public String ParentIdList;
	
	public int IsPreOrder;
	public int CmsProductStatus;
	public int IsHearSay;
	public int ProductSlider;
	
	public long nextpromotionupdate;
	public Date strnextpromotionupdate;

	public long nextseupdate;
	public Date strnextseupdate;

	public int Capacity;
}
