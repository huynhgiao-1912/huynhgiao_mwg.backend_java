
package mwg.wb.model.pm;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/13/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class StoreBO {
	public Date CreatedDate;
	public Date OpeningDay;
	public Date ReOpenDate;
	public Date ClosingDay;
	/// <summary>
	/// StoreID
	///
	/// </summary>
	public int StoreID;

	/// <summary>
	/// AreaID
	///
	/// </summary>
	public int AreaID;

	/// <summary>
	/// StoreName
	///
	/// </summary>
	public String StoreName;

	/// <summary>
	/// StoreAddress
	///
	/// </summary>
	public String StoreAddress;

	/// <summary>
	/// StoreManager
	///
	/// </summary>
	public String StoreManager;

	/// <summary>
	/// StorePhoneNum
	///
	/// </summary>
	public String StorePhoneNum;

	/// <summary>
	/// StoreShortName
	///
	/// </summary>
	public String StoreShortName;

	/// <summary>
	/// ProvinceID
	///
	/// </summary>
	public int ProvinceID;

	/// <summary>
	/// StoreFax
	///
	/// </summary>
	public String StoreFax;

	/// <summary>
	/// IsCenterStore
	///
	/// </summary>
	public boolean IsCenterStore;

	/// <summary>
	/// TaxCode
	///
	/// </summary>
	public String TaxCode;

	/// <summary>
	/// IsDelete
	///
	/// </summary>
	public boolean IsDelete;

	/// <summary>
	/// UserDelete
	///
	/// </summary>
	public String UserDelete;

	/// <summary>
	/// DateDelete
	///
	/// </summary>
	public Date DateDelete;

	/// <summary>
	/// IsRealStore
	///
	/// </summary>
	public boolean IsRealStore;

	/// <summary>
	/// IsSaleStore
	///
	/// </summary>
	public boolean IsSaleStore;

	/// <summary>
	/// IsInputStore
	///
	/// </summary>
	public boolean IsInputStore;

	/// <summary>
	/// CompanyNamePrefix
	///
	/// </summary>
	public String CompanyNamePrefix;

	/// <summary>
	/// StoreTypeID
	///
	/// </summary>
	public int StoreTypeID;

	/// <summary>
	/// OrderIndex
	///
	/// </summary>
	public int OrderIndex;

	/// <summary>
	/// StoreGroupID
	///
	/// </summary>
	public int StoreGroupID;

	/// <summary>
	/// IsWarrantyStore
	///
	/// </summary>
	public boolean IsWarrantyStore;

	/// <summary>
	/// IsActive
	///
	/// </summary>
	public boolean IsActive;

	//
	//
	//
	public int WardID;
	/// <summary>
	/// Note
	///
	/// </summary>
	public String Note;

	/// <summary>
	/// IsAutoStoreChange
	///
	/// </summary>
	public boolean IsAutoStoreChange;

	/// <summary>
	/// StoreCode
	///
	/// </summary>
	public String StoreCode;

	/// <summary>
	/// TaxAddress
	/// Địa chỉ khai báo thuế
	/// </summary>
	public String TaxAddress;

	/// <summary>
	/// PriceAreaID
	///
	/// </summary>
	public int PriceAreaID;

	/// <summary>
	/// IsSystem
	///
	/// </summary>
	public boolean IsSystem;

	/// <summary>
	/// OpenHour
	/// giờ mở cửa
	/// </summary>
	public String OpenHour;

	/// <summary>
	/// LAT
	///
	/// </summary>
	public double LAT;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public double LNG;

	public String StoreEmail;

	public String StoreFullName;

	public String ImageMapSmall;

	public String ImageMapLarge;

	public int SiteID;

	public String WebStoreName;

	public int DistrictID;

	public int CompanyID;

	public String CompanyTitle;

	public String ProvinceName;

	public String DistrictName;

	public int DistrictIndex;

	public int DisplayOrder;

	public int Rank;

	public boolean IsShowweb;

	public String WebStoreImage;

	public String WebAddress; // vu.nguyenhoang2 - 20140326
	/// <summary>
	/// Số lượng sản phẩm tồn tại siêu thị
	/// </summary>
	public int Quantity;
	public int CenterQuantity;

	public String BCNBStoreName;
	public double BCNBAreaID;
	public double BCNBProvinceID;
	public boolean IsShowBCNB;
	public int SampleQuantity;
	public int OldSampleQuantity;
	public int ReplacePrdQuantity;
	public String ProductCode;
	public boolean isStockAvailable;
	public int brandID;

	public String PartnerinstallmentIDList;

	public int TypeOff;
	public Date OffEndDate;
	public Date OffBeginDate;
	public int InventoryStatusID;
	public double range; // khoảng cách 2 latlon

	public String replacedproductid;
}
