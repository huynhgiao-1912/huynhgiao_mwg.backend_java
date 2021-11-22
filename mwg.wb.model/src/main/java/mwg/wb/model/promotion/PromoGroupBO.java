
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Long Trịnh 
/// Created date 	: 11/18/2014 
/// Promotion Group
/// </summary>

import java.util.Date;

public class PromoGroupBO {

	public PromoGroupBO() {
	}

	/// <summary>
	/// PromoGroupID
	///
	/// </summary>
	public int PromoGroupID;

	/// <summary>
	/// ParentGroupID
	/// 1: Tổng hợp, 2: Điện thoại, 3: Máy tính bảng, 4:
	/// </summary>
	public int ParentGroupID;

	/// <summary>
	/// GroupTitle
	///
	/// </summary>
	public String GroupTitle;

	/// <summary>
	/// GroupListProductID
	///
	/// </summary>
	public String GroupListProductID;

	/// <summary>
	/// AnchorText
	///
	/// </summary>
	public String AnchorText;

	/// <summary>
	/// TextLink
	///
	/// </summary>
	public String TextLink;

	/// <summary>
	/// URLTextLink
	///
	/// </summary>
	public String URLTextLink;

	/// <summary>
	/// ProductShowForm
	/// 1: 2 cột(lớn), 2: 3 cột(trung bình), 3: 4 cột(nhỏ)
	/// </summary>
	public int ProductShowForm;

	/// <summary>
	/// DisplayOrder
	///
	/// </summary>
	public int DisplayOrder;

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
	/// DeletedUser
	///
	/// </summary>
	public String DeletedUser;

	/// <summary>
	/// PromoProgramID
	///
	/// </summary>
	public int PromoProgramID;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public String Icon;

	public String LabelImage;

	public String LabelText;

	public Date LabelDate;

	public String PromotionText;

	/// <summary>
	/// 1 ( Mặc định: Hiển thị tổng giá trị khuyến mãi), 2 (Hiển thị dòng đầu tiên
	/// từ box KM), 3 (Khai báo bằng Text)
	/// </summary>
	public int PromotionType;

	public Date PromotionDate;

	/// <summary>
	/// Hiển thị số lượng tồn kho của sản phẩm
	/// </summary>
	public boolean IsShowInventory;

	public boolean IsShowConfig;

	public int PriceOrder;

	public Date StartDate;

	public Date EndDate;

	public boolean IsShowPromotion;
}
