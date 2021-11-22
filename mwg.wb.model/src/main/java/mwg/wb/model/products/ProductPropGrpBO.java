
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Vu Quy Khi 
/// Created date 	: 3/19/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;
import java.util.List;

public class ProductPropGrpBO {

	public List<ProductPropBO> ProductPropBOLst;

	/// <summary>
	/// GroupID
	///
	/// </summary>
	public int GroupID;

	/// <summary>
	/// GroupName
	///
	/// </summary>
	public String GroupName;

	/// <summary>
	/// CategoryID
	/// Nhóm thuộc tính này thuộc loại sản phẩm nào
	/// </summary>
	public int CategoryID;

	/// <summary>
	/// CountProduct
	/// Nhóm thuộc tính này thuộc loại sản phẩm nào
	/// </summary>
	public int CountProduct;

	/// <summary>
	/// Description
	///
	/// </summary>
	public String Description;

	/// <summary>
	/// DisplayOrder
	///
	/// </summary>
	public int DisplayOrder;

	/// <summary>
	/// IsSpecial
	/// Nhóm đặc biệt (để ánh xạ với thuộc tính khác)
	/// </summary>
	public int IsSpecial;

	/// <summary>
	/// IsActived
	///
	/// </summary>
	public int IsActived;

	/// <summary>
	/// ActivedDate
	///
	/// </summary>
	public Date ActivedDate;

	/// <summary>
	/// ActivedUser
	///
	/// </summary>
	public String ActivedUser;

	/// <summary>
	/// IsDeleted
	///
	/// </summary>
	public boolean IsDeleted;

	/// <summary>
	/// DeletedDate
	///
	/// </summary>
	public Date DeletedDate;

	/// <summary>
	/// DeletedUser
	///
	/// </summary>
	public String DeletedUser;

	/// <summary>
	/// CreatedDate
	///
	/// </summary>
	public Date CreatedDate;

	/// <summary>
	/// CreatedUser
	///
	/// </summary>
	public String CreatedUser;

	/// <summary>
	/// UpdatedDate
	///
	/// </summary>
	public Date UpdatedDate;

	/// <summary>
	/// UpdatedUser
	///
	/// </summary>
	public String UpdatedUser;

	public  ProductPropGrpLangBO objProductPropGrpLangBO = new ProductPropGrpLangBO();

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public String LanguageID;

	public int IsCompact;
}
