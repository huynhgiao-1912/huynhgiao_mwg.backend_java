
package mwg.wb.model.products;
/// <summary>

/// Created by 		:TRUYNN
/// Created date 	: 27/07/2018
/// </summary>	

import java.util.Date;

public class ProductOldLock {
	public int LockId;
	/// <summary>
	/// 1 = Imei
	/// 0 = Không imei
	/// </summary>
	public int IsImei;
	public String Imei;
	public int ProductId;
	public int CategoryId;
	public String ProductCode;
	public int StoreId;
	public String SaleOrderId;
	/// <summary>
	/// 1 = đơn hàng bình thường
	/// </summary>
	public int SaleOrderType;
	public String FullName;
	public String PhoneNumber;
	/// <summary>
	/// Thời gian giao hàng
	/// </summary>
	public Date DeliveryTime;
	public int Quantity;
	public boolean IsOutProduct;
	public int InvenstatusId;
	public boolean IsDeleted;
	public Date DeletedDate;
	public String DeletedUser;
	public boolean IsLocking;
	public Date LockedDate;

	/// <summary>
	/// User hủy đơn hàng: autoupdate hủy tự động
	/// </summary>
	public String UnlockUser;

	public Date UnlockedDate;
}
