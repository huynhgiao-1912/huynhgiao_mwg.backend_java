
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/11/2012 
/// Product
/// </summary>	

import java.util.Date;

public class ProductHomeShowBO {
	/// <summary>
	/// ProductID
	/// MÃ SẢN PHẨM
	/// </summary>
	public int ProductID;

	/// <summary>
	/// ProductCode
	/// MÃ SẢN PHẨM ERP
	/// </summary>
	public String ProductCode;

	/// <summary>
	/// IsWebHighlight
	/// SẢN PHẨM NỖI BẬT
	/// </summary>
	public boolean IsWebHighlight;

	/// <summary>
	/// DisplayOrderIsWebHightlight
	/// THỨ TỰ HIỂN THỊ CỦA SẢN PHẨM NỔI BẬT
	/// </summary>
	public int DisplayOrderIsWebHightlight;

	/// <summary>
	/// IsBestSelling
	/// SẢN PHẨM BÁN CHẠY
	/// </summary>
	public boolean IsBestSelling;

	/// <summary>
	/// DisplayOrderIsBestSelling
	/// THỨ TỰ HIỂN THỊ SẢN PHẨM BÁN CHẠY
	/// </summary>
	public int DisplayOrderIsBestSelling;

	/// <summary>
	/// UpdatedUser
	/// NGƯỜI CẬP NHẬT CUỐI
	/// </summary>
	public String UpdatedUser;

	/// <summary>
	/// UpdatedDate
	/// NGÀY CẬP NHẬT CUỐI
	/// </summary>
	public Date UpdatedDate;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
