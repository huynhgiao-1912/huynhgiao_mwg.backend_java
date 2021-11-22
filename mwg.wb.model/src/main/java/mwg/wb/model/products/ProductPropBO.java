
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Vu Quy Khi 
/// Created date 	: 3/19/2012 
/// Tên tiếng Việt
/// </summary>	

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Date;
import java.util.List;

@JsonInclude(Include.NON_DEFAULT)
public class ProductPropBO {
	/// <summary>
	/// PropertyID
	/// Mã thuộc tính sản phẩm
	/// </summary>
	public List<ProductPropValueBO> ProductPropValueBOLst;

	/// <summary>
	/// PropertyID
	/// Mã thuộc tính sản phẩm
	/// </summary>
	public int PropertyID;

	/// <summary>
	/// GroupID
	/// Nhóm thuộc tính
	/// </summary>
	public int GroupID;

	/// <summary>
	/// GroupOrder
	/// Nhóm thuộc tính
	/// </summary>
	public int GroupOrder;

	/// <summary>
	/// PropertyName
	/// Tên thuộc tính sản phẩm
	/// </summary>
	public String PropertyName;

	/// <summary>
	/// GROUPNAME
	/// Tên nhóm thuộc tính sản phẩm
	/// </summary>
	public String GroupName;

	/// <summary>
	/// Description
	/// Mô tả cho thuộc tính này
	/// </summary>
	public String Description;

	/// <summary>
	/// PropertyType
	/// 0=Textbox; 1=Dropdownlist; 2=Listbox;
	/// </summary>
	public int PropertyType;

	/// <summary>
	/// IsGeneral
	/// Có đưa vào phần General của Product
	/// </summary>
	public boolean IsGeneral;

	/// <summary>
	/// IsTooltip
	/// Có đưa vào phần Tooltip của Product
	/// </summary>
	public boolean IsTooltip;

	/// <summary>
	/// DisplayOrder
	///
	/// </summary>
	public int DisplayOrder;

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
	/// ISGENCOL
	/// Đánh dấu tình trạng sinh cột trong bảng dữ liệu chi tiết thuộc tính
	/// </summary>
	public boolean IsGencol;

	/// <summary>
	/// ISSEARCH
	///
	/// </summary>
	public boolean IsSearch;
	public boolean IsSearchDMX;

	/// <summary>
	/// IsOperator
	/// Có sử dụng toán tử trên tìm kiếm
	/// </summary>
	public boolean IsOperator;

	/// <summary>
	/// ISSELECTED
	///
	/// </summary>
	public boolean IsSelected;

	/// <summary>
	/// ISSPECIAL
	///
	/// </summary>
	public boolean IsSpecial;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	/// <summary>
	/// Giá trị thuộc tính
	/// </summary>
	public String Value;

	public int CategoryID;

	/// <summary>
	/// Ten bai viet huong dan
	/// </summary>
	public String AdvisorText;
	/// <summary>
	/// Link bai viet huong dan
	/// </summary>
	public String AdvisorLink;
	public int PropertyWeight;
	public String MetaKeyWord;
	public boolean IsShowSpecs;
	public boolean IsImportant;
	public boolean isAddUp;
	public boolean IsMenu;
	public String URL;
	public String dragFilter;
	public String dragTooltip;
	public int ISFILTER;

	public boolean isFeaturedCompare;

	public String dmxurl;


	public String unitText;
	/**
	 * Vẫn là unitText nhưng ko hiện ở API
	 */
	public String unitTextSE;

	public String MixStructure;

	public boolean isAdditional;

	public boolean isAndFilter() {
		return ISFILTER == 1 || (ISFILTER == 0 && PropertyType == 2);
	}
}
