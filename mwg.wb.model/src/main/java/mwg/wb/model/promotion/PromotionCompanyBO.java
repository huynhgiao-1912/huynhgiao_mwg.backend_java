
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/4/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class PromotionCompanyBO {

	public PromotionCompanyBO() {
	}

	/// <summary>
	/// ProductID
	/// Mã sản phẩm được khuyến mãi giảm giá
	/// </summary>
	public String ProductID;

	/// <summary>
	/// DiscountPrice
	/// Số tiền giảm cho sản phẩm này
	/// </summary>
	public String DiscountPrice;

	/// <summary>
	/// BeginDate
	/// Ngày bắt đầu có hiệu lực khuyến mãi
	/// </summary>
	public Date BeginDate;

	/// <summary>
	/// EndDate
	/// Ngày hết hiệu lực khuyến mãi
	/// </summary>
	public Date EndDate;

	/// <summary>
	/// Description
	/// Mô tả ngắn
	/// </summary>
	public String Description;

	/// <summary>
	/// CreatedUser
	///
	/// </summary>
	public String CreatedUser;

	/// <summary>
	/// CreatedDate
	///
	/// </summary>
	public Date CreatedDate;

	/// <summary>
	/// UpdatedUser
	///
	/// </summary>
	public String UpdatedUser;

	/// <summary>
	/// UpdatedDate
	///
	/// </summary>
	public Date UpdatedDate;

	/// <summary>
	/// DeletedUser
	///
	/// </summary>
	public String DeletedUser;

	/// <summary>
	/// DeletedDate
	///
	/// </summary>
	public Date DeletedDate;

	/// <summary>
	/// IsDeleted
	///
	/// </summary>
	public boolean IsDeleted;

	/// <summary>
	/// DispalyOrder
	/// Thứ tự hiển thị
	/// </summary>
	public int DispalyOrder;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
