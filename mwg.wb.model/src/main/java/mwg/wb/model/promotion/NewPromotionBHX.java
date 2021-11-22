package mwg.wb.model.promotion;

import java.util.Date;

import mwg.wb.common.Utils;

public class NewPromotionBHX {
	/// <summary>
	/// Mã khuyến mãi
	/// </summary>
	public String Id;

	/// <summary>
	/// Thời điểm KM bắt đầu có hiệu lực
	/// </summary>
	public Date BeginDate;

	/// <summary>
	/// Thời điểm KM hết hiệu lực
	/// </summary>
	public Date EndDate;

	/// <summary>
	/// Tên chương trình khuyến mãi
	/// </summary>
	public String Name;

	/// <summary>
	/// Mô tả về khuyến mãi
	/// </summary>
	public String Description;

	/// <summary>
	/// Điều kiện được áp dụng
	/// </summary>
	public String Condition;

	/// <summary>
	/// Thông tin nhóm KM của ERP
	/// </summary>
	public String PromotionListGroupId;

	/// <summary>
	/// Mã chương trình CRM, dùng khi KM có nhiều thứ cần kiểm tra ở CRM
	/// </summary>
	public String CRMProgramId;

	/// <summary>
	/// Số lần khuyến mãi được áp dụng
	/// </summary>
	public int Limit;

	/// <summary>
	/// Kiểm tra khuyến mãi có hiệu lực áp dụng hay không.
	/// <para>Mặc định đã kiểm tra thời gian hiệu lực.</para>
	/// <para>Nếu khuyến mãi có điều kiện áp dụng phức tạp cần override hàm này để
	/// kiểm tra</para>
	/// </summary>
	/// <param name="parameters">Các tham số phục vụ tính toán</param>
	/// <returns></returns>

	public Boolean IsValid(Object... parameters) {
		return BeginDate.compareTo(Utils.GetCurrentDate()) <= 0 && Utils.GetCurrentDate().compareTo(EndDate) < 0;
	}
}
