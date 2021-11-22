package mwg.wb.model.installment;

import java.util.Date;

public class InstallmentException {
	public int ExceptionId;
	public String ExceptionName;
	/// <summary>
	/// Loại exception
	/// 1 = Trả góp 0% ngân lượng
	/// 2 = Chặn hiển thị trả góp 0%
	/// </summary>
	public int ExceptionType;
	public String ExceptionNote;
	public int Productid;
	public Date FromDate;
	public Date ToDate;
	public int ErpProgramID;
}
