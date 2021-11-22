
package mwg.wb.model.pm;

import java.util.Date;

/// <summary>
/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/7/2012 
/// Tên tiếng Việt
/// </summary>	

public class CurrencyUnitBO {

	public CurrencyUnitBO() {
	}

	/// <summary>
	/// CurrencyUnitID
	///
	/// </summary>
	public int CurrencyUnitID;

	/// <summary>
	/// CurrencyUnitName
	///
	/// </summary>
	public String CurrencyUnitName;

	/// <summary>
	/// CurrencyExchange
	///
	/// </summary>
	public float CurrencyExchange;

	/// <summary>
	/// IsDefault
	///
	/// </summary>
	public boolean IsDefault;

	/// <summary>
	/// IsDelete
	///
	/// </summary>
	public boolean IsDelete;

	/// <summary>
	/// UserDelete
	///
	/// </summary>
	public String UserDelete;

	/// <summary>
	/// DateDelete
	///
	/// </summary>
	public Date DateDelete;

	/// <summary>
	/// AccountID
	///
	/// </summary>
	public int AccountID;

	/// <summary>
	/// OrderIndex
	/// Thứ tự
	/// </summary>
	public int OrderIndex;

	/// <summary>
	/// IsSystem
	///
	/// </summary>
	public boolean IsSystem;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
