package mwg.wb.model.products;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/11/2012 
/// Product
/// </summary>	

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_DEFAULT)
public class ProductManuLangBO {

	/// <summary>
	/// ManufacturerID
	///
	/// </summary>
	public double ManufacturerID;
	public double ClassifiedCount;

	/// <summary>
	/// CountProduct
	///
	/// </summary>
	public double CountProduct;

	/// <summary>
	/// LanguageID
	///
	/// </summary>
	public String LanguageID;

	/// <summary>
	/// ManufacturerName
	///
	/// </summary>
	public String ManufacturerName;

	/// <summary>
	/// Description
	///
	/// </summary>

	@JsonProperty("manudescription")
	public String Description;

	/// <summary>
	/// KeyWord
	///
	/// </summary>
	@JsonProperty("manukeyword")
	public String KeyWord;

	/// <summary>
	/// MetaKeyWord
	///
	/// </summary>
	@JsonProperty("manumetakeyword")
	public String MetaKeyWord;

	/// <summary>
	/// MetaDescription
	///
	/// </summary>
	@JsonProperty("manumetadescription")
	public String MetaDescription;

	/// <summary>
	/// MetaTitle
	///
	/// </summary>
	@JsonProperty("manumetatitle")
	public String MetaTitle;

	/// <summary>
	/// URL
	///
	/// </summary>
	@JsonProperty("manuurl")
	public String URL;

	/// <summary>
	/// SEOName
	///
	/// </summary>
	@JsonProperty("manuseoname")
	public String SEOName;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	/// <summary>
	/// Đếm số lượng tin rao vặt
	/// </summary>
	public int CountClassified;

	public float DisplayOrder;
	public String TimerMetaDescription;
	public String TimerMetaKeyWord;
	public String TimerMetaTitle;
	public Date TimerFromDate;
	public Date TimerToDate;
	public String SmallLogo;
	public String ManufacturerSmallLogo;
	public String ManufacturerBigLogo;
	public int Pinwarranty;
	public int warranty;
	public int compressorswarranty;
	public int refrigerationwarranty;
	public String companyidlist;
	public String manucountryidlist;
	public String manucountrynamelist;
	public String newslink;
	
	public String saleInfo;
	
	public String ReturnPolicy;
	public String  OldReturnPolicy;
	public Date OldReturnPolicyDate;
	public String DescriptionDMX;

}