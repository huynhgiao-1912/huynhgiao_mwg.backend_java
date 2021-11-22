
package mwg.wb.model.products;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Strings;

import java.util.stream.Stream;

/// <summary>

/// Created by 		: Vu Quy Khi 
/// Created date 	: 3/19/2012 
/// Tên tiếng Việt
/// </summary>	

@JsonInclude(Include.NON_DEFAULT)
public class ProductDetailBO implements Cloneable {
	/// <summary>
	/// Value
	///
	/// </summary>
	public String Value;

	/// <summary>
	/// PropertyID
	///
	/// </summary>
	public int PropertyID;

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public long ProductID;
//	public double ProductID;

	/// <summary>
	/// LanguageID
	///
	/// </summary>
	public String LanguageID;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public String PropertyName;

	public int GroupID;
	public String GroupName;

	public int PropertyType;

	public int PropValueID;

	public String PropValue;

	public boolean IsShowSpecs;

	public String ProductName;

	public int CompareValue;
	public double dCompareValue;

	public boolean IsSearch;
	public boolean IsSearchDMX;

	public String ContentHelp;

	public int PropertyWeight;

	public int ValueDisplayOrder;
	public String PropValueLink;
	public String Icon;
	public String IconUrl;
	public int SiteId;
	public boolean Isexistpro;
	public boolean PropIssearch;
	public boolean IsFeatureProp;
	public int PropertyDisplayOrder;
	public boolean IsImportant;
	public String MetaKeyWord;
	public boolean IsDataSearch;

	public boolean IsInitSearch;
	public String[] Values;
	public boolean isAddUp;

	public String labelName;
	
	public String shortName;

	@JsonIgnore
	public ProductDetailBO[] valuesObj;

    public String PropUrl;

    public String dragFilter;
    public String dragToolTip;

    public boolean isFeaturedCompare;

    public boolean isSpecial;
    public String unitText;
	/**
	 * Vẫn là unitText nhưng ko hiện ở API
	 */
	public String unitTextSE;

	public String MixStructure;

    @JsonIgnore
	public boolean isInvalid;

    public Stream<ProductDetailBO> expandValues() {
		if (valuesObj == null)
			return Stream.of(clone(null));
		return Stream.of(valuesObj).map(x -> clone(x));
	}

	public ProductDetailBO clone(ProductDetailBO value) {
		try {
			var x = (ProductDetailBO) super.clone();
			if (value != null) {
				x.Value = value.Value;
				x.PropValue = value.Value;
//				x.PropertyDisplayOrder = value.PropertyDisplayOrder;
				x.PropertyID = value.PropertyID;
				x.ValueDisplayOrder = value.ValueDisplayOrder;
				x.PropValueLink = value.PropValueLink;
				x.PropValueID = value.PropValueID;
				x.CompareValue = (int) (value.dCompareValue * 100);
				x.IsImportant = value.IsImportant;
				x.Icon = value.Icon;
				x.labelName = value.labelName;
				x.shortName = value.shortName;
				x.dragToolTip = value.dragToolTip;
				x.IsDataSearch = value.IsDataSearch;
				x.IsInitSearch = value.IsInitSearch;
				x.MetaKeyWord = value.MetaKeyWord;
			}
			x.Values = null;
			x.valuesObj = null;
			// nếu có đơn vị thì value chỉ chừa lại chữ số
			if (!Strings.isNullOrEmpty(x.unitTextSE)
					&& (Strings.isNullOrEmpty(x.Value) || x.Value.matches("[0-9.-]+"))) {
				x.unitText = x.unitTextSE;
			}
			return x;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
