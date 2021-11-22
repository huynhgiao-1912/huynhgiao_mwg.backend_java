
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/4/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class PromotionProgramHistoryBO {

	public PromotionProgramHistoryBO() {
	}

	/// <summary>
	/// PromotionHistoryID
	///
	/// </summary>
	public int PromotionHistoryID;

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
	/// UpdateDate
	///
	/// </summary>
	public Date UpdateDate;

	/// <summary>
	/// ShortDescription
	///
	/// </summary>
	public String ShortDescription;

	/// <summary>
	/// IsShowVATInVoiceMessage
	///
	/// </summary>
	public boolean IsShowVATInVoiceMessage;

	/// <summary>
	/// VATInVoiceMessage
	///
	/// </summary>
	public String VATInVoiceMessage;

	/// <summary>
	/// IsNewType
	///
	/// </summary>
	public int IsNewType;

	/// <summary>
	/// IsShowProductType
	///
	/// </summary>
	public int IsShowProductType;

	/// <summary>
	/// ShowPriority
	/// Độ ưu tiên hiển thị trên hóa đơn VAT
	/// </summary>
	public int ShowPriority;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
