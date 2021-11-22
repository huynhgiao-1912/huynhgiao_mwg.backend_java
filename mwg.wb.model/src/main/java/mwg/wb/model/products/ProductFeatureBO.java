
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/11/2012 
/// Product
/// </summary>	

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import mwg.wb.model.api.ProductBOApi;

@JsonInclude(Include.NON_DEFAULT)
public class ProductFeatureBO {

	public ProductBOApi product;

	public int ProductFeatureID;
	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int ProductID;

	/// <summary>
	/// GroupID
	///
	/// </summary>
	public int GroupID;

	/// <summary>
	/// Label
	///
	/// </summary>
	public int LABEL;

	/// <summary>
	/// Displayorder
	///
	/// </summary>
	public int DisplayOrder;

	/// <summary>
	/// IsShowHome
	///
	/// </summary>
	public int IsShowHome;

	/// <summary>
	/// CreatedDate
	///
	/// </summary>
	public Date CreatedDate;

	public String CreatedUser;

	public Date UpdatedDate;

	public String UpdatedUser;

	public boolean IsDeleted;

	public Date DeletedDate;

	public String DeletedUser;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;
	public String ProductName;
	public int SiteID;
	public String LabelStr;
	public int CategoryID;
	
	public int Ispriority;
	public Date FromDate;
	public Date ToDate;
}
