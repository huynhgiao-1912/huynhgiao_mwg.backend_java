
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Vũ Quý Khi 
/// Created date 	: 3/29/2012 
/// Tư vấn qua điện thoại
/// </summary>	

import java.util.Date;

public class ProductCategoryPhoneLineBO {

	/// <summary>
	/// SITE
	/// Trang (0: thegioididong.com, 1: dienmay.com)
	/// </summary>
	public String Site;

	/// <summary>
	/// CategoryID
	/// Mã ngành hàng
	/// </summary>
	public int CategoryID;

	/// <summary>
	/// MASTERLine
	/// SĐT chính
	/// </summary>
	public String MasterLine;

	/// <summary>
	/// SUBLineBuy
	/// SĐT con (nhánh mua hàng)
	/// </summary>
	public String SubLineBuy;

	/// <summary>
	/// DESKLine
	/// SĐT bàn (bổ sung sau nếu có)
	/// </summary>
	public String DeskLine;

	/// <summary>
	/// SUBLineSUPPORT
	/// SĐT con (nhánh hỗ trợ kĩ thuật)
	/// </summary>
	public String SubLineSupport;

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
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
