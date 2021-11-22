package mwg.wb.model.products;

import com.fasterxml.jackson.annotation.JsonIgnore;
import mwg.wb.common.Utils;

import java.util.Date;

public class ProductErpPriceBO implements Cloneable {
	public int TotalQuantityRelateProvince;
	public String RecordID;
	public int ProvinceId;
	public int StoreID;

	public long ProductId;
	public String ProductCode;

	public double OnlinePrice;
	public double Price;
	public double PriceOrg;
	public double Price402;
	public double HisPrice;

	@JsonIgnore
	public double getPrice() {
		return Price;
	}

	@JsonIgnore
	public float getQuantity() {
		return QuantityNew;
	}

	public double ReturnValue;
	public boolean IsOnlineOnly;
	public boolean IsShowHome;
	public boolean IsWebShow;
	public boolean IsBestSelling;
	public boolean IsPriceConfirmed;

	public String StandardKit;

	// Ton kho
	public int StatusId;
	public int WebStatusId;
	public int WebStatusIdNew;
	public int WebStatusIdOld;

	public Date ArrivalDate;
	public boolean IsForthcoming;

	public int MinStock;
	public int OutOfStock;
	public int Quantity;
	//dùng cho tồn bachhoaxanh
	public float QuantityNew;
	//có phải là sản phẩm cơ sở của bhx ko
	public boolean IsBaseUnit;
	public int TotalQuantity;
	public int CenterQuantity;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	/// <summary>
	/// Có đang chọn không?
	/// </summary>
	public boolean IsSelected;

	/// <summary>
	/// Có chỉnh sữa không?
	/// </summary>
	public boolean IsEdited;

	public int RowNumber;

	public Date ProductArrivalDate;

	public boolean IsProductForthComing;

	public int WebMinStock;

	public int OutOfStockWarningQuantity;

	public double LuckyToPrice;

	public double LuckyFromPrice;

	public double LuckyValue;

	public Date UpdatedPriceDate;

	public int DistrictId;

	public int CategoryId;

	public boolean IsSaleStore;

	public boolean IsShowWeb;

	public boolean IsInputStore;

	public int ProductCodeTotalQuantity;
	public int ProductCodeQuantity;

	/// <summary>
	/// 1: Không giao
	/// 2: Xe máy.
	/// 3: Xe tải
	/// </summary>
	public int DeliveryVehicles;
	public double NetCostPrice;
	public double RefPrice;
	public double MaxPrice;
	public boolean IsLimitProduct;
	/// <summary>
	/// là sp imei?
	/// </summary>
	public boolean IsRequestImei;
	public int VituralQuantity1;
	public int SampleQuantity1;
	public int SampleQuantity;

	public int IsPriceOfStore;

	public int DeliveryType;

	public boolean IsLimit;

	public Date OutOfStockDate;
	public boolean IsBooking;
	public boolean Isonlyonline;
	// don vi (dung cho gia co sở)
	public String QuantityUnit;
	public double StandardSalePrice;
	public double StandardPriceAreaSalePrice;
	public double OnlineAreaSalePrice;
	public Date didxupdateddate;
	public int PriceArea;
	public int OutputType;
	public int CompanyID;
	public int SiteID;
	public String LangID;
	public int TotalSampleQuantity;
	public String RunVersion;

	public boolean IsProductComingForBigphone;

	public int warrantyMonth;
	public int extendWarrantyMonth;
	public int warrantyDays;
	public int extendWarrantyDays;

	public boolean codeIsOnlineOnly;

	public ProductErpPriceBO clone() {
		try {
			return (ProductErpPriceBO) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@JsonIgnore
	public boolean campreorder;

	@JsonIgnore
	public SpecialSaleProgramBO specialSale;

	public String Image;

	@JsonIgnore
	public String getImage() {
		return Image + "";
	}
	@JsonIgnore
	public int hasImage() {
		return Utils.StringIsEmpty(Image) ? 999999 : DisplayOrder;
	}

	public String Bimage;
	public String Mimage;
	public String Simage;
	public int DisplayOrder;
	public int IsUseAvatar;

	@JsonIgnore
	public int maingroupID;
	@JsonIgnore
	public int subgroupID;
	@JsonIgnore
	public boolean is364province;
//	@JsonIgnore
	public int quantityOLOLHub;
	public int manufacturerID;
	public boolean isSetupProduct;
}
