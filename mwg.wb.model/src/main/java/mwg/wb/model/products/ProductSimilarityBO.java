
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/11/2012 
/// Product
/// </summary>	

import java.util.Date;

public class ProductSimilarityBO {

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int ProductID;

	/// <summary>
	/// SIMProductID
	///
	/// </summary>
	public int SIMProductID;

	/// <summary>
	/// Distance
	///
	/// </summary>
	public int Distance;

	/// <summary>
	/// LastRUN
	///
	/// </summary>
	public Date LastRUN;

	/// <summary>
	/// CategoryID
	///
	/// </summary>
	public int CategoryID;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
