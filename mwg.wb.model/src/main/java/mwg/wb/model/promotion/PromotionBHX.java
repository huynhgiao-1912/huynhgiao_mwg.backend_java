package mwg.wb.model.promotion;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PromotionBHX {
	
	///Loại khuyến mãi: 1 KM thường, 2 KM cận date, 3 KM combo, 4 KM flashsale
	public BHXPromotionType BHXPromotionType;
	
	/// <summary>
    /// Mã KM
    /// </summary>
    public int Id;

    /// <summary>
    /// Mô tả
    /// </summary>
    public String Description;

    /// <summary>
    /// Ngày bắt đầu hiệu lực
    /// </summary>
    private Date BeginDate;

    /// <summary>
    /// Ngày hết hiệu lực
    /// </summary>
    private Date EndDate;

    /// <summary>
    /// Giá trị KM
    /// </summary>
    public double Value;

    /// <summary>
    /// Giá trị KM nhỏ nhất. VD: KM từ min VNĐ đến max VNĐ
    /// </summary>
    public double MinValue;

    /// <summary>
    /// Giá trị KM lớn nhất. VD: KM từ min VNĐ đến max VNĐ
    /// </summary>
    public double MaxValue;

    /// <summary>
    /// Loại KM
    /// </summary>
    public PromotionTypes Type;

    /// <summary>
    /// Khi KM là loại giảm giá, nó có phải là kiểu % hay không?
    /// </summary>
    public boolean IsPercent;

    /// <summary>
    /// Danh sách các mã nhóm KM (dùng khi CRM yêu cầu)
    /// </summary>
    public String PromotionListGroupId;

    /// <summary>
    /// Nếu là loại KM quà tặng: TRUE - Chọn tất cả quà. FALSE - Chọn một trong các món quà.
    /// </summary>
    public boolean GiftType;

    /// <summary>
    /// Điều kiện kích hoạt khuyến mãi
    /// <example>VD: Mua 3sp A, tặng 2sp B --> QuantityCondition = 3</example>
    /// </summary>
    public int QuantityCondition;

    /// <summary>
    /// Danh sách các quà tặng
    /// </summary>
    public String Gifts;
    
    public GiftBHX[] GiftBHX;

    /// <summary>
    /// Giá trị thực của khuyến mãi (bao gồm cả giảm tiền & quà tặng) so với sản phẩm
    /// </summary>
    public double PromotionRealDiscountValue;

    /// <summary>
    /// Đếm số lượng đơn hàng có khuyến mãi này
    /// </summary>
    public int LimitedCount;

    /// <summary>
    /// Giới hạn số lượng đơn hàng có khuyến mãi này
    /// </summary>
    public int Limit;

    /// <summary>
    /// Hình đại diện của khuyến mãi
    /// Key: Loại hình, Value: Url hình
    /// </summary>
    public HashMap<String, String> Images;

    /// <summary>
    /// Không áp dụng với trả góp đặt biệt
    /// </summary>
    public boolean IsNotApplyWithSpecialInstallment;

    /// <summary>
    /// Khuyến mãi tốt hơn
    /// </summary>
    public boolean IsBetter;

    /// <summary>
    /// Các khuyến mãi bị loại trừ bởi khuyến mãi này
    /// </summary>
    public List<Integer> Excludes;

    /// <summary>
    /// Sử dụng trong TH là khuyến mãi combo (WEB2x101064)
    /// </summary>
    public String ComboCode;

    /// <summary>
    /// Danh sách các kho được áp dụng KM
    /// </summary>
    public String StoreIds;

    /// <summary>
    /// Giờ bắt đầu KM flashsale
    /// </summary>
    public Date FromTime;

    /// <summary>
    /// Giờ kết thúc KM flashsale
    /// </summary>
    public Date ToTime;
  
    public int ProvinceId;
    
    /// <summary>
    /// chỉ áp dụng cho kho online
    /// </summary>
    public boolean IsOnlineOnly;
  /// <summary>
    /// Số lượng sản phẩm được áp dụng KM trên 1 đơn hàng
    /// </summary>
    public int MaxQuantityOnBill;
	public Date getBeginDate() {
		return BeginDate;
	}
	public void setBeginDate(Date beginDate) {
		BeginDate = beginDate;
	}
	public Date getEndDate() {
		return EndDate;
	}
	public void setEndDate(Date endDate) {
		EndDate = endDate;
	}
}
