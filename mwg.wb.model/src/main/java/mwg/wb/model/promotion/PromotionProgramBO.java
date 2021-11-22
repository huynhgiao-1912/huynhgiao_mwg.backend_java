
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/4/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class PromotionProgramBO {

	public PromotionProgramBO() {
	}

	/// <summary>
	/// PromotionID
	///
	/// </summary>
	public int PromotionID;

	/// <summary>
	/// PromotionName
	///
	/// </summary>
	public String PromotionName;

	/// <summary>
	/// BeginDate
	///
	/// </summary>
	public Date BeginDate;

	/// <summary>
	/// EndDate
	///
	/// </summary>
	public Date EndDate;

	/// <summary>
	/// UserName
	///
	/// </summary>
	public String UserName;

	/// <summary>
	/// InputTime
	///
	/// </summary>
	public Date InputTime;

	/// <summary>
	/// Description
	///
	/// </summary>
	public String Description;

	/// <summary>
	/// IsPercentDiscount
	///
	/// </summary>
	public boolean IsPercentDiscount;

	/// <summary>
	/// DiscountValue
	///
	/// </summary>
	public int DiscountValue;

	/// <summary>
	/// IsConditionDiscont
	///
	/// </summary>
	public boolean IsConditionDiscont;

	/// <summary>
	/// ConditionContent
	///
	/// </summary>
	public String ConditionContent;

	/// <summary>
	/// IsActive
	///
	/// </summary>
	public boolean IsActive;

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
	/// UserActive
	///
	/// </summary>
	public String UserActive;

	/// <summary>
	/// DateActive
	///
	/// </summary>
	public Date DateActive;

	/// <summary>
	/// UserUpdate
	///
	/// </summary>
	public String UserUpdate;

	/// <summary>
	/// DateUpdate
	///
	/// </summary>
	public Date DateUpdate;

	/// <summary>
	/// ShortDescription
	/// Mô tả ngắn
	/// </summary>
	public String ShortDescription;

	/// <summary>
	/// IsShowVATInVoiceMessage
	/// Có hiển thị thông báo trên HĐ VAT hay không?
	/// </summary>
	public boolean IsShowVATInVoiceMessage;

	/// <summary>
	/// VATInVoiceMessage
	/// Nội dung câu thông báo trên HĐ VAT
	/// </summary>
	public String VATInVoiceMessage;

	/// <summary>
	/// IsNewType
	/// Loại mới cũ; -1: Tất cả, 0: Cũ, 1: Mới
	/// </summary>
	public int IsNewType;

	/// <summary>
	/// IsShowProductType
	/// Loại hàng trưng bày; -1: tất cả, 0: Không là hàng trưng bày, 1: Hàng trưng
	/// bày
	/// </summary>
	public int IsShowProductType;

	/// <summary>
	/// ShowPriority
	/// Độ ưu tiên hiển thị trên hóa đơn VAT
	/// </summary>
	public int ShowPriority;

	/// <summary>
	/// FromPrice
	///
	/// </summary>
	public int FromPrice;

	/// <summary>
	/// IsPromotionByPrice
	///
	/// </summary>
	public boolean IsPromotionByPrice;

	/// <summary>
	/// IsPromotionByTotalMoney
	///
	/// </summary>
	public boolean IsPromotionByTotalMoney;

	/// <summary>
	/// IsPromotionByTotalQuantity
	///
	/// </summary>
	public int IsPromotionByTotalQuantity;

	/// <summary>
	/// MaxPromotionTotalMoney
	///
	/// </summary>
	public int MaxPromotionTotalMoney;

	/// <summary>
	/// MaxPromotionTotalQuantity
	///
	/// </summary>
	public int MaxPromotionTotalQuantity;

	/// <summary>
	/// MinPromotionTotalMoney
	///
	/// </summary>
	public int MinPromotionTotalMoney;

	/// <summary>
	/// MinPromotionTotalQuantity
	///
	/// </summary>
	public int MinPromotionTotalQuantity;

	/// <summary>
	/// ToPrice
	///
	/// </summary>
	public int ToPrice;

	/// <summary>
	/// ProgramImage
	///
	/// </summary>
	public String ProgramImage;

	/// <summary>
	/// ProgramURL
	///
	/// </summary>
	public String ProgramURL;

	/// <summary>
	/// DisplayOrder
	///
	/// </summary>
	public int DisplayOrder;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
