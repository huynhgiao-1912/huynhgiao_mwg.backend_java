
package mwg.wb.model.pm;
/// <summary>

import java.util.Date;

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/7/2012 
/// Tên tiếng Việt
/// </summary>	

public class StoreInfoBO {

	public StoreInfoBO() {
	}

	/// <summary>
	/// StoreID
	///
	/// </summary>
	public int StoreID;

	/// <summary>
	/// StoreName
	///
	/// </summary>
	public String StoreName;

	/// <summary>
	/// StoreFullName
	///
	/// </summary>
	public String StoreFullName;

	/// <summary>
	/// StoreWebsite
	///
	/// </summary>
	public StoreWebsiteBO objStoreWebsiteBO = new StoreWebsiteBO();

	/// <summary>
	/// AreaID
	///
	/// </summary>
	public int AreaID;

	/// <summary>
	/// DistrictID
	///
	/// </summary>
	public int DistrictID;

	/// <summary>
	/// StoreAddress
	///
	/// </summary>
	public String StoreAddress;

	/// <summary>
	/// Director
	///
	/// </summary>
	public String Director;

	/// <summary>
	/// Phone
	///
	/// </summary>
	public String Phone;

	/// <summary>
	/// Fax
	///
	/// </summary>
	public String Fax;

	/// <summary>
	/// Email
	///
	/// </summary>
	public String Email;

	/// <summary>
	/// OpenHour
	///
	/// </summary>
	public String OpenHour;

	/// <summary>
	/// Description
	///
	/// </summary>
	public String Description;

	/// <summary>
	/// ImageMapSmall
	///
	/// </summary>
	public String ImageMapSmall;

	/// <summary>
	/// Mobile ImageMapSmall
	///
	/// </summary>
	public String Mobile_ImageMapSmall;

	/// <summary>
	/// ImageMapLarge
	///
	/// </summary>
	public String ImageMapLarge;

	/// <summary>
	/// Mobile ImageMapLarge
	///
	/// </summary>
	public String Mobile_ImageMapLarge;

	/// <summary>
	/// RANK
	///
	/// </summary>
	public int Rank;

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
	/// IsDefault
	///
	/// </summary>
	public boolean IsDefault;

	/// <summary>
	/// IsOnlineStore
	/// Có phải là kho online (để đặt PreOrder)
	/// </summary>
	public boolean IsOnlineStore;

	/// <summary>
	/// StoreOnline
	///
	/// </summary>
	public boolean StoreOnline;

	/// <summary>
	/// LAT
	///
	/// </summary>
	public float LAT;

	/// <summary>
	/// LNG
	///
	/// </summary>
	public float LNG;

	/// <summary>
	/// IsRepay
	///
	/// </summary>
	public int IsRepay;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public int SiteID;

	public int ProvinceID;

	public String ProvinceName;

	public String WebStoreImage;

	public String WebAddress;
}
