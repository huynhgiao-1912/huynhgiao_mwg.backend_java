
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Vu Quy Khi 
/// Created date 	: 5/12/2012 
/// Chương trình tổng hợp khuyến mãi
/// </summary>	

import java.util.Date;

public class PromotionGeneralBO {

	public PromotionGeneralBO() {
	}

	/// <summary>
	/// PromotionID
	/// MÃ CHƯƠNG TRÌNH
	/// </summary>
	public int PromotionID;

	/// <summary>
	/// PromotionName
	/// TÊN CHƯƠNG TRÌNH
	/// </summary>
	public String PromotionName;

	/// <summary>
	/// BeginDate
	/// NGÀY BẮT ĐẦU
	/// </summary>
	public Date BeginDate;

	/// <summary>
	/// EndDate
	/// NGÀY KẾT THÚC
	/// </summary>
	public Date EndDate;

	/// <summary>
	/// SiteID
	/// SITE ÁP DỤNG : 1:TGDD,3:DIENMAY
	/// </summary>
	public int SiteID;

	/// <summary>
	/// TypeApply
	/// HÌNH THỨC ÁP DỤNG:1: SIÊU THỊ, 2:ONLINE, 0:CẢ 2
	/// </summary>
	public int TypeApply;

	/// <summary>
	/// IsUNLimitED
	/// CHƯƠNG TRÌNH LÀ KHÔNG BỊ GIỚI HẠN
	/// </summary>
	public boolean IsUnLimited;

	/// <summary>
	/// URL
	/// ĐƯỜNG DẪN ĐẾN TRANG KHUYẾN MÃI ĐÓ
	/// </summary>
	public String URL;

	/// <summary>
	/// IsHot
	/// LÀ CHƯƠNG TRÌNH KHUYẾN MÃI HOT
	/// </summary>
	public boolean IsHot;

	/// <summary>
	/// IsActived
	/// ĐÃ ĐƯỢC KÍCH HOẠT
	/// </summary>
	public boolean IsActived;

	/// <summary>
	/// Image
	/// HÌNH ẢNH CỦA CHƯƠNG TRÌNH
	/// </summary>
	public String Image;

	/// <summary>
	/// IsDeleted
	/// ĐÃ XÓA
	/// </summary>
	public boolean IsDeleted;

	/// <summary>
	/// CreateUser
	/// NGƯỜI TẠO
	/// </summary>
	public String CreateUser;

	/// <summary>
	/// UpdateUser
	/// NGƯỜI UPDATE
	/// </summary>
	public String UpdateUser;

	/// <summary>
	/// DeletedUser
	/// NGƯỜI XÓA
	/// </summary>
	public String DeletedUser;

	/// <summary>
	/// Description
	/// MÔ TẢ CHƯƠNG TRÌNH
	/// </summary>
	public String Description;

	/// <summary>
	/// CreatedDate
	///
	/// </summary>
	public Date CreatedDate;

	/// <summary>
	/// Updatedate
	///
	/// </summary>
	public Date Updatedate;

	/// <summary>
	/// Deletedate
	///
	/// </summary>
	public Date Deletedate;

	/// <summary>
	/// OrderIndex
	///
	/// </summary>
	public int OrderIndex;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
