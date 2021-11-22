
package mwg.wb.model.products;
/// <summary>

import java.util.Date;
import java.util.List;

public class ProductLanguageBO {

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int ProductID;

	/// <summary>
	/// LanguageID
	///
	/// </summary>
	public String LanguageID;

	/// <summary>
	/// Description
	///
	/// </summary>
	public String Description;

	/// <summary>
	/// CategoryID
	///
	/// </summary>
	public int CategoryID;

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
	/// HTML
	///
	/// </summary>
	public String HTML;

	/// <summary>
	/// Tooltip
	///
	/// </summary>
	public String Tooltip;

	/// <summary>
	/// General
	///
	/// </summary>
	public String General;

	/// <summary>
	/// UserGuide
	///
	/// </summary>
	public String UserGuide;

	/// <summary>
	/// ProductName
	///
	/// </summary>
	public String ProductName;

	/// <summary>
	/// SEOName
	///
	/// </summary>
	public String SEOName;

	/// <summary>
	/// CANONICal
	///
	/// </summary>
	public String CANONICal;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public String HTMLMobile;

	public double SiteID;

	public String HTMLDescription;

	public String HotReason; // vu.nguyenhoang2 - 20140215

	public long CountLike; // vu.nguyenhoang2 - 20140215

	public Date ScenarioShowWebFromDate;
	public double Scenario;
	public Date ScenarioShowWebToDate;
	public int IsShowHome;
	public int PackageQuantity;

	///////////
	///
	public int IsHearSay;
	public String HtmlSliderMobile;
	public double ShowHomeDisplayOrder;
	public Date ShowHomeStartDate;
	public Date ShowHomeEndDate;
	public String ShortName;
	public int Iscollection;
	public int IsshowWeb;
	public double Collectionid;
	public double CollectionProductCount;
	public String GeneralKeyWord;
	public int IsRepreentProduct;
	public double RepresentProductID;
	public int IsreferAccessory;
	public Date FeatureStartDate;
	public Date FeatureExpireDate;
	public int IsRepResentProduct;
	public double PepResentProductID;
	public String ListProductID;
	
	
	public class Productcategorylangbo {
		public String metakeyword;
		public String metadescription;
		public String metatitle;
		public String url;
		public String seoname;
		public boolean isexist;
		public String categoryname;
		public String keyword;
	}

	public class Productcategorybo {
		public double categoryid;
		public boolean isexist;
	}

	public class Productmanubo {
		public double manufacturerid;
		public double categoryid;
	}

	public class Productmanulangbo {
		public String manufacturername;
		public String smalllogo;
		public double manudisplayorder;
		public String keyword;
		public String metadescription;
		public String metakeyword;
		public String metatitle;
		public String seoname;
		public String url;
		public double manufacturerid;
	}

	public class Result {
		public double productid;
		public Object producturl;
		public String productcode;
		public int isonlineonly;
		public int isdeleted;
		public String userguidepdf;
		public String createduser;
		public String strgallery;
		public String strgalleryallcolor;
		public String mobilesimage;
		public String mobilemimage;
		public String mobilebimage;
		public String mobilestrgallery;
		public String htmlwarranty;
		public String htmlshipping;
		public String currencyunitname;
		public String promotioninfo;
		public int displayorder;
		public double categoryid;
		public double ratecount;
		public String lastestgenerate;
		public String createddate;
		public String updateddate;
		public double totalreview;
		public double ratescore;
		public String importeddate;
		public double ratingscore;
		public int quantity;
		public double comments;
		public int isfeature;
		public int isfullwidth;
		public int osid;
		public String osname;
		public double likecount;
		public String specs;
		public String releasedate;
		public List<ProductLanguageBO> productlanguagebo;
		public List<Productcategorylangbo> productcategorylangbo;
		public List<Productcategorybo> productcategorybo;
		public List<Productmanubo> productmanubo;
		public List<Productmanulangbo> productmanulangbo;

	}

	public class RootObjectProductLanguageBO {
		public List<Result> result;
	}

	public String simage;
	public String mimage;
	public String bimage;
	public String bimageurl;
	public String picture;
	public String warrantyinfo;
	public String featureimage;
	public int ishot;
	public int isnew;
	public String templatealias;
	public int isevent;
	public String detailimage;
	public String shortdescription;
	public Double expectedprice;
	public String comboproductidlist;
	public String advantage;
	public String disadvantage;
	public int isshowexpectedprice;
	public String purchasinginfo;

	public int isshowexpectedtext;
	public String htmlslider;
	public String kitimagesmall;
	public String kitimagelarge;
	public String specificationimage;
	public int isnewsdetailversion;
	public String templatefeature;
	public int ischeckconfirmconfig;

	public String topimage;
	public Date scenariostartdate;
	public Date scenarioenddate;
	

	public String listrelatednewsid;
	public String listrelatedvideoid;
	public String brepresentimage;
	public String mrepresentimage;
	public String srepresentimage;
	public int issmartphone;
	public String redirecturl;
	public Date redirectfromdate;
	public Date redirecttodate;
	public int badgetype;
	public Date badgestartdate;
	public Date badgeenddate;

	public int iseasydeal;
	public int islandingpageondetail;
	public int imagesize;
	public String timermetatile;
	public String timermetatitle;
	public String timermetadescription;
	public String timermetakeyword;
	public Date timerfromdate;
	public Date timertodate;	
	public String instocksiteidlist;
	public String dusermanual;
	public String musermanual;
	public int isnotsale;
	public String notdeliverydistrictidlist;
	
	
	public String provinceidlist;
	public String detailshortname;
	public String northernproductname;
	public String detailsuggestdescription;
	public Date suggestfromdate;
	public Date suggesttodate;
	public int combodiscountpercent;
	public String combopromotionidlist;
	public int isshowfullstock;

	public String desktopfeatureimage;
	public String listnewsid;
	public int ispreordercam;
	public Date preordercamfromdate;
	public Date preordercamtodate;
	public String preordercaminfo;
	public String timerthumbmeta;
	public String timerfeatureimage;
	public Date timerfeaturefromdate;
	public Date timerfeaturetodate;
	public String timerfeatureimagedesktop;
	public String iswarrantyhome;	
	public int labelposition;
	public String labelcolor;
	public int webstatusid;
	public String timerthumbimage;
	public Date timerthumbimagefromdate;
	public Date timerthumbimagetodate;
	public Date combofromdate;
	public Date combotodate;
	public int quantitypromotionid;
	public String searchsuggestdescription;
	
	
	public int IsPayment;
	public Date PaymentFromDate;
	public Date PaymentToDate;
	public double PercentInstallment;

	public String mimageurl;

	public String simageurl;
	
	public boolean isHiddenPrice;
	
	public String AvatarByStore;
	public String AvatarStoreIds ;
	public Date AvatarByStoreEndDate;
	public Date AvatarByStoreStartDate;
	
	public int AccessoriesDisplayOrder;
	public Date AccessoriesStartDate;
	public Date AccessoriesEndDate;
	public int AccessoriesIsShowHome;
	public String shortNameProcessed;
	//public String AdvertiseImageUrl;

}
