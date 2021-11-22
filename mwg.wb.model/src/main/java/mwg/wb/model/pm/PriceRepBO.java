
package mwg.wb.model.pm;

import java.util.Date;

/// <summary>
/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/7/2012 
/// Tên tiếng Việt
/// </summary>	

public class PriceRepBO {

	public PriceRepBO() {
	}

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public String ProductID;

	/// <summary>
	/// AreaID
	///
	/// </summary>
	public int AreaID;

	/// <summary>
	/// IMEI
	///
	/// </summary>
	public String IMEI;

	/// <summary>
	/// Price
	/// Giá bán
	/// </summary>
	public float Price;

	/// <summary>
	/// Updatedate
	///
	/// </summary>
	public Date Updatedate;

	/// <summary>
	/// StoreID
	/// Kho hiện tại của những SP có IMEI
	/// </summary>
	public int StoreID;

	/// <summary>
	/// WebPrice
	///
	/// </summary>
	public float WebPrice;

	/// <summary>
	/// RefPrice
	/// Giá tham khảo (giá do người dùng nhập vd: giá VienThongA)
	/// </summary>
	public float RefPrice;

	/// <summary>
	/// BuyPrice
	///
	/// </summary>
	public float BuyPrice;

	/// <summary>
	/// AdjustLevel
	///
	/// </summary>
	public float AdjustLevel;

	/// <summary>
	/// PromotionCost
	///
	/// </summary>
	public float PromotionCost;

	/// <summary>
	/// Note
	///
	/// </summary>
	public String Note;

	/// <summary>
	/// Promotion
	///
	/// </summary>
	public String Promotion;

	/// <summary>
	/// CurrencyUnitID
	///
	/// </summary>
	public int CurrencyUnitID;

	/// <summary>
	/// IsConfirm
	///
	/// </summary>
	public boolean IsConfirm;

	/// <summary>
	/// AccessoryIncludes
	///
	/// </summary>
	public String AccessoryIncludes;

	/// <summary>
	/// DefaultPromotion
	///
	/// </summary>
	public String DefaultPromotion;

	/// <summary>
	/// IsPrint
	/// Có in trong bảng giá hay không
	/// </summary>
	public boolean IsPrint;

	/// <summary>
	/// IsWebShow
	/// Có hiển thị trên web hay không?
	/// </summary>
	public boolean IsWebShow;

	/// <summary>
	/// PromotionPrice
	/// Giá KM
	/// </summary>
	public int PromotionPrice;

	/// <summary>
	/// IsHightLight
	/// Có in nổi bật ở bảng giá không?
	/// </summary>
	public boolean IsHightLight;

	/// <summary>
	/// IsNewProduct
	/// Có hiển thị biểu tượng mới ở bảng giá in hay ko?
	/// </summary>
	public boolean IsNewProduct;

	/// <summary>
	/// OutputTypeID
	/// Hình thức xuất
	/// </summary>
	public int OutputTypeID;

	/// <summary>
	/// FormatedIMEI
	/// IMEI đã được định dạng
	/// </summary>
	public String FormatedIMEI;

	/// <summary>
	/// CrossMarginPercent
	/// Tỷ lệ lãi gộp
	/// </summary>
	public double CrossMarginPercent;

	/// <summary>
	/// IsDifferentStandard
	/// Khác với giá chuẩn
	/// </summary>
	public boolean IsDifferentStandard;

	/// <summary>
	/// IsUpdatePriceByArea
	/// Có làm giá theo khu vực hay không?
	/// </summary>
	public boolean IsUpdatePriceByArea;

	/// <summary>
	/// VAT
	/// Thuế VAT
	/// </summary>
	public int VAT;

	/// <summary>
	/// IsNew
	/// Là giá của sản phẩm mới
	/// </summary>
	public boolean IsNew;

	/// <summary>
	/// MainGroupID
	/// Mã ngành hàng của ERP
	/// </summary>
	public int MainGroupID;

	/// <summary>
	/// ProductIDRef
	/// Mã sản phẩm tham chiếu
	/// </summary>
	public int ProductIDRef;

	/// <summary>
	/// IsOnlineOnly
	/// Chỉ bán online
	/// </summary>
	public boolean IsOnlineOnly;

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
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
