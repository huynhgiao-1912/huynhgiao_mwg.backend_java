
package mwg.wb.model.pm;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/7/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class CurrentInStockDetailBO {

	public CurrentInStockDetailBO() {
	}

	/// <summary>
	/// CurrentInStockID
	///
	/// </summary>
	public int CurrentInStockID;

	/// <summary>
	/// IMEI
	///
	/// </summary>
	public String IMEI;

	/// <summary>
	/// WARRANTYTIME
	///
	/// </summary>
	public int WarrantyTime;

	/// <summary>
	/// InputVoucherDetailID
	///
	/// </summary>
	public String InputVoucherDetailID;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public Date EndWarrantyDate;

}
