
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Vu Quy Khi 
/// Created date 	: 3/19/2012 
/// Tên tiếng Việt
/// </summary>	

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Date;

@JsonInclude(Include.NON_DEFAULT)
public class ProductPropValueBO {
	/// <summary>
	/// ValueID
	///
	/// </summary>
	public int ValueID;

	/// <summary>
	/// PropertyID
	///
	/// </summary>
	public int PropertyID;

	/// <summary>
	/// Value
	///
	/// </summary>
	public String Value;

	/// <summary>
	/// DisplayOrder
	///
	/// </summary>
	public int DisplayOrder;

	/// <summary>
	/// CountProduct
	///
	/// </summary>
	public int CountProduct;

	/// <summary>
	/// ISSEARCH
	/// Có cho phép tìm kiếm trên giá trị thuộc tính này không
	/// </summary>
	public boolean IsSearch;

	/// <summary>
	/// COMPAREValue
	/// Giá trị để so sánh
	/// </summary>
	public int CompareValue;

	/// <summary>
	/// ICON
	/// Biểu tượng của giá trị
	/// </summary>
	public String Icon;

	/// <summary>
	/// IsActived
	///
	/// </summary>
	public boolean IsActived;

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

	/// <summary>
	/// ISEXISTPRO
	/// đánh dấu thuộc tính có của SP (=1: SP có thuộc tính này)
	/// </summary>
	public boolean IsExistPro;

	/// <summary>
	/// ISSELECTED
	///
	/// </summary>
	public boolean IsSelected;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public float ValueData;

	public String IconUrl;
	public String Description;
	public String ShortTitle;
	public String MetaTitle;
	public String MetaDescription;
	public String DmUrl;
	public String MetaKeyWord;
	public String SmoothUrl;
	public String shortName;
	
	public String ManuName;
	public int ManuId;
	public String ManuNameList;
	public String manuTitle;
	public String manuDescription;
	public ProductCategoryBO productCategoryBO;
	public String dragFilter;
	public String dragTooltip;
	public String urlFine;
}
