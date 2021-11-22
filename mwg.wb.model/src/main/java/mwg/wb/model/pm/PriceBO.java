
package mwg.wb.model.pm;

import java.util.Date;

/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/7/2012 
/// Tên tiếng Việt
/// </summary>	

public class PriceBO {

	public PriceBO() {
	}

	/// <summary>
	/// PriceID
	///
	/// </summary>
	public int PriceID;

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int ProductID;

	/// <summary>
	/// ProductCode
	/// Mã sản phẩm ERP
	/// </summary>
	public String ProductCode;

	/// <summary>
	/// Product_Code
	/// Mã sản phẩm ERP
	/// </summary>
	public String Product_Code;

	/// <summary>
	/// AreaID
	/// Khu vực ERP
	/// </summary>
	public int AreaID;

	/// <summary>
	/// ColorID
	///
	/// </summary>
	public int ColorID;

	/// <summary>
	/// Price
	///
	/// </summary>
	public double Price;

	/// <summary>
	/// Image
	///
	/// </summary>
	public String Image;

	/// <summary>
	/// PreOrderPrice
	///
	/// </summary>
	public double PreOrderPrice;

	/// <summary>
	/// DESPrice
	///
	/// </summary>
	public double DESPrice;

	/// <summary>
	/// CurrencyUnitID
	///
	/// </summary>
	public int CurrencyUnitID;

	/// <summary>
	/// Instock
	///
	/// </summary>
	public int Instock;

	/// <summary>
	/// CurrencyUnitName
	///
	/// </summary>
	public String CurrencyUnitName;

	/// <summary>
	/// Configuration
	///
	/// </summary>
	public String Configuration;

	/// <summary>
	/// ISONLINEONLYPRICE
	///
	/// </summary>
	public boolean IsOnlineOnlyPrice;
	/// <summary>
	/// ISMAINPRICE
	///
	/// </summary>
	public boolean IsMainPrice;
	/// <summary>
	/// StandardKIT
	///
	/// </summary>
	public String StandardKIT;

	/// <summary>
	/// IsShowHome
	/// Hiển thị ở trang chủ
	/// </summary>
	public boolean IsShowHome;

	/// <summary>
	/// IsOnlineOnly
	/// Chỉ bán online
	/// </summary>
	public boolean IsOnlineOnly;

	/// <summary>
	/// IsWebShow
	/// Hiển thị web
	/// </summary>
	public boolean IsWebShow;

	/// <summary>
	/// IsWebShowHomePage
	/// Hiển thị trang chủ web
	/// </summary>
	public boolean IsWebShowHomePage;

	/// <summary>
	/// IsWebShowForOnline
	/// Hiển thị web của giá theo hình thức xuất
	/// </summary>
	public boolean IsWebShowForOnline;

	/// <summary>
	/// IsWebHighlight
	/// Sản phẩm nổi bật trên web
	/// </summary>
	public boolean IsWebHighlight;

	/// <summary>
	/// IsBestSelling
	/// Sản phẩm bán chạy
	/// </summary>
	public boolean IsBestSelling;

	/// <summary>
	/// InputDate
	///
	/// </summary>
	public Date InputDate;

	/// <summary>
	/// UpdatedDate
	///
	/// </summary>
	public Date UpdatedDate;

	/// <summary>
	/// OnlineRepaymentPrice
	///
	/// </summary>
	public double OnlineRepaymentPrice;

	/// <summary>
	/// RepaymentPrice
	///
	/// </summary>
	public double RepaymentPrice;

	/// <summary>
	/// ProductStatusID
	/// Mã trạng thái sản phẩm (ERP.PM_ProductStatus)
	/// </summary>
	public int ProductStatusID;

	/// <summary>
	/// WeekSaledCount
	/// Số lượng xuất bán trong tuần
	/// </summary>
	public int WeekSaledCount;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public double NewPrice;

	public double OldPrice;

	public String AccessoryIncludes;

	public int WebMinStock;

	public int OutOfStockWarningQuantity;

	public boolean IsProductForthComing;

	public Date ArrivalDate;

	public int OutputType;

	public boolean IsPriceConfirmed;

	public double PromotionPrice;

	public double WebPrice;

	public String PriceAreaName;
}
