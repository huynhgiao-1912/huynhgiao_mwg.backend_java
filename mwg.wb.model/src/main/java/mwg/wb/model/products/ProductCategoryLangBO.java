
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/10/2012 
/// Tên tiếng Việt
/// </summary>	

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT)
public class ProductCategoryLangBO {

	/// <summary>
	/// CategoryID
	///
	/// </summary>
	public int CategoryID;

	/// <summary>
	/// LanguageID
	///
	/// </summary>
	public String LanguageID;

	/// <summary>
	/// CategoryName
	///
	/// </summary>
	public String CategoryName;

	/// <summary>
	/// Description
	///
	/// </summary>
	public String Description;

	/// <summary>
	/// KeyWord
	///
	/// </summary>
	public String KeyWord;

	/// <summary>
	/// MetaKeyWord
	///
	/// </summary>
	public String MetaKeyWord;

	/// <summary>
	/// MetaDescription
	///
	/// </summary>
	public String MetaDescription;

	/// <summary>
	/// MetaTitle
	///
	/// </summary>
	public String MetaTitle;

	/// <summary>
	/// URL
	///
	/// </summary>
	public String URL;

	/// <summary>
	/// TooltipTemplate
	/// Mẫu thiết kế của Tooltip
	/// </summary>
	public String TooltipTemplate;

	/// <summary>
	/// HTMLTemplate
	/// Mẫu chi tiết 1 sản phẩm của ngành hàng
	/// </summary>
	public String HTMLTemplate;

	/// <summary>
	/// GeneralTemplate
	/// Mẫu thông tin tổng quan của ngành hàng
	/// </summary>
	public String GeneralTemplate;

	/// <summary>
	/// TooltipTemplate
	/// Mẫu thông tin tổng quan của ngành hàng
	/// </summary>
	// public String TooltipTemplate;

	/// <summary>
	/// CompareTemplate
	/// Mẫu so sánh các sản phẩm của ngành hàng
	/// </summary>
	public String CompareTemplate;

	/// <summary>
	/// SEOName
	///
	/// </summary>
	public String SEOName;

	/// <summary>
	/// CompareTemplateMobile
	/// Mẫu so sánh các sản phẩm của ngành hàng trên MOBILE
	/// </summary>
	public String CompareTemplateMobile;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public String CompareTpl_6Col;

	public String HtmlMobile;
	 
}
