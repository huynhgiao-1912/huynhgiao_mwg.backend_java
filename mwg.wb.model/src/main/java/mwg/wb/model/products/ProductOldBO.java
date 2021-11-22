
package mwg.wb.model.products;
/// <summary>

/// Created by 		:NGUYEN DUC HIEU
/// Created date 	: 18/10/2013
/// </summary>	

import java.util.Date;
import java.util.List;

import mwg.wb.model.pm.StoreInfoBO;
import mwg.wb.model.promotion.PromotionOldProductBO;

public class ProductOldBO {
	/// <summary>
	/// OLDID
	/// Mã sản phẩm
	/// </summary>
	public int OLDID;

	/// <summary>
	/// IMEI
	/// Số IMEL(khóa chính)
	/// </summary>
	public String IMEI;

	/// <summary>
	/// ProductID
	/// Mã sản phẩm
	/// </summary>
	public long ProductID;

	/// <summary>
	/// MinPrice
	///
	/// </summary>
	public double MinPrice;

	/// <summary>
	/// Rownum
	///
	/// </summary>
	public int Rownum;

	/// <summary>
	/// CountProduct
	/// Đếm sản phẩm
	/// </summary>
	public int CountProduct;

	/// <summary>
	/// ProductName
	/// Tên sản phẩm
	/// </summary>
	public String ProductName;

	/// <summary>
	/// StoreID
	/// Được bán tại siêu thi
	/// </summary>
	public int StoreID;

	/// <summary>
	/// Price
	///
	/// </summary>
	public double Price;

	public double Price2;

	/// <summary>
	/// OldPrice
	///
	/// </summary>
	public double OldPrice;

	/// <summary>
	/// NewPrice
	///
	/// </summary>
	public double NewPrice;
	/// <summary>
	/// WarrantyTime
	/// Thời gian bảo hành
	/// </summary>
	public int WarrantyTime;

	/// <summary>
	/// Loại bảo hành
	/// 1: chính hãng
	/// 2: TGDD
	/// </summary>
	public int WarrantyType;

	/// <summary>
	/// WarrantyAddress
	/// Địa chỉ nơi bảo hành
	/// </summary>
	public String WarrantyAddress;

	/// <summary>
	/// WarrantyStatus
	/// Ghi chú bảo hành: thegioididong hay chính hãng
	/// </summary>
	public String WarrantyStatus;

	/// <summary>
	/// Accessories
	/// Phụ kiện kèm theo
	/// </summary>
	public String AccessoryList;

	/// <summary>
	/// DateInput
	///
	/// </summary>
	public Date DateInput;

	/// <summary>
	/// PriceBuy
	///
	/// </summary>
	public int PriceBuy;

	/// <summary>
	/// BuyDate
	///
	/// </summary>
	public Date BuyDate;

	/// <summary>
	/// PriceSell
	///
	/// </summary>
	public int PriceSell;

	/// <summary>
	/// DateIsSell
	///
	/// </summary>
	public Date DateIsSell;

	/// <summary>
	/// Seller
	///
	/// </summary>
	public String Seller;

	/// <summary>
	/// DateOutput
	///
	/// </summary>
	public Date DateOutput;

	/// <summary>
	/// Status
	///
	/// </summary>
	public String Status;

	/// <summary>
	/// HideReason
	/// Lý do ẩn
	/// </summary>
	public String HideReason;

	/// <summary>
	/// StatusProduct
	/// Tình trạng máy (%; 0: còn đẹp)
	/// </summary>
	public String StatusProduct;

	/// <summary>
	/// RateCount
	/// </summary>
	public int RateCount;

	/// <summary>
	/// RateScore
	/// </summary>
	public double RateScore;

	/// <summary>
	/// RePairFee
	/// Phí sửa chữa
	/// </summary>
	public int RePairFee;

	/// <summary>
	/// RePairFeeNote
	/// Ghi chú phí sửa chữa
	/// </summary>
	public String RePairFeeNote;

	/// <summary>
	/// ProductBO
	///
	/// </summary>
	public ProductBO ProductBO;

	/// <summary>
	/// ProductCategoryLangBO
	///
	/// </summary>
	public ProductCategoryLangBO ProductCategoryLangBO;

	/// <summary>
	/// ProductCategoryLangBO
	///
	/// </summary>
	public ProductCategoryBO ProductCategoryBO;

	/// <summary>
	/// ProductManuLangBO
	///
	/// </summary>
	public ProductManuLangBO ProductManuLangBO;

	/// <summary>
	/// ProductManuLangBO
	///
	/// </summary>
	public StoreInfoBO StoreInfoBO;

	/// <summary>
	/// IsDeleted
	///
	/// </summary>
	public int IsDeleted;

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
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public String ProductCode;

	public int CommentCount;

	public String URL;

	public String Description;

	public String SImage;

	public String MImage;

	public String ToolTip;

	public String SeoName;

	public int CategoryID;

	public int ManufacturerID;

	public String CategoryURL;

	public String ManuURL;

	public String CurrencyUnitName;

	public String CategoryName;

	public boolean IsEvent;

	public boolean IsSpecial;

	public boolean IsNew;

	public boolean IsHot;

	public String ManufacturerName;

	public Date ActivedDate;

	public String StoreName;

	public String StoreAddress;

	public String WebStoreName;

	public String StorePhoneNum;

	public int ProvinceID;

	public String ProvinceName;

	public int Districtid;

	public String Districtname;

	public double Lat;

	public double Lng;

	public String Imagemaplarge;

	public String Imagemapsmall;

	public int TotalRecord;

	public int Quantity;

	public int TotalQuantity;

	public int PromotionCount;

	public String REPRESENTIMAGE;

	/// <summary>
	/// THOI GIAN CAP NHAT HINH LAN CUOI
	/// </summary>
	public Date LASTIMAGEUPDATEDDATE;

	public List<ProductOldImageBO> lstProductOldImageBO;

	/// <summary>
	/// Danh dau san pham co KM hay khong
	/// </summary>
	public boolean IsPromotion;

	/// <summary>
	/// Trang thai may doi tra
	/// 2: Đã sử dụng
	/// 4: Cũ ( thu mua)
	/// </summary>
	public int InventoryStatusID;

	/// <summary>
	/// Phan tram giam gia
	/// </summary>
	public int DiscountValue;
	public List<PromotionOldProductBO> lstPromotionBO;
	public boolean IsImei;
	public int ColorID;
	public String Caption;
	public int siteId;
	public boolean IsErpLock;
	public Date UnlockDate;

	public int Subgroupid;
	public int Maingroupid;
	public int BrandID;
	public String LanguagueID;
}
