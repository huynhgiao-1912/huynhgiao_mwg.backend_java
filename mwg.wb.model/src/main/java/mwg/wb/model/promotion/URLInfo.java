package mwg.wb.model.promotion;

import java.text.Normalizer;
import java.util.regex.Pattern;

import mwg.wb.common.Utils;
import mwg.wb.model.products.ProductBO;

public class URLInfo {
	public int PID;
    public int MID;
    public int CID;
    public String Properties;

    public static String GenUrl(ProductBO productBO)
    {
        if (productBO == null)
        {
            return "";
        }
        String productUrl, cateUrl;
        if (productBO.ProductCategoryLangBO != null)
        {
            cateUrl = Utils.StringIsEmpty(productBO.ProductCategoryLangBO.URL)
                ? productBO.ProductCategoryLangBO.CategoryName
                : productBO.ProductCategoryLangBO.URL;
        }
        else if (productBO.ProductCategoryBO != null)
        {
            cateUrl = Utils.StringIsEmpty(productBO.ProductCategoryBO.URL)
                ? productBO.ProductCategoryBO.CategoryName
                : productBO.ProductCategoryBO.URL;
        }
        else
        {
            cateUrl = productBO.CategoryName;
        }

        if (productBO.ProductLanguageBO != null)
        {
            productUrl = Utils.StringIsEmpty(productBO.ProductLanguageBO.URL)
                ? productBO.ProductLanguageBO.ProductName
                : productBO.ProductLanguageBO.URL;
        }
        else
        {
            productUrl = Utils.StringIsEmpty(productBO.URL)
                ? productBO.ProductName
                : productBO.URL;
        }

        if (Utils.StringIsEmpty(cateUrl) || Utils.StringIsEmpty(productUrl))
            return "";
        var url = ToURL(productUrl);
        url = (url.startsWith("/") ? "" : "/") + url;
        return ToURL(cateUrl) + url;
    }

//    public static string GenCategoryUrl(ProductCategoryBO category)
//    {
//        return ToURL(string.IsNullOrEmpty(category.URL)
//            ? (string.IsNullOrEmpty(category.CategoryName) ? string.Empty : category.CategoryName)
//            : category.URL);
//    }

//    public static string GenManufacturerUrl(ProductCategoryBO category, ProductManuBO manufacture)
//    {
//        var cateUrl = GenCategoryUrl(category);
//        var manuUrl = string.IsNullOrEmpty(manufacture.URL)
//            ? (string.IsNullOrEmpty(manufacture.ManufacturerName)
//                ? string.Empty
//                : manufacture.ManufacturerName)
//            : manufacture.URL;
//        if (string.IsNullOrEmpty(manuUrl))
//            return string.Empty;
//        return ToURL(cateUrl + "-" + manuUrl);
//    }

//    public static string GenPropertyUrl(ProductCategoryBO category, ProductPropValueBO propVal)
//    {
//        var categoryUrl = GenCategoryUrl(category);
//        if (string.IsNullOrEmpty(categoryUrl))
//            return string.Empty;
//
//        var propUrl = (categoryUrl + "-" + (string.IsNullOrEmpty(propVal.URL)
//                           ? propVal.Value
//                           : propVal.URL));
//        return ToURL(propUrl);
//    }

//    public static string GenPropertyUrl(ProductCategoryBO category, ProductManuBO manufacture, ProductPropValueBO propVal)
//    {
//        var manuUrl = GenManufacturerUrl(category, manufacture);
//        if (string.IsNullOrEmpty(manuUrl))
//            return string.Empty;
//        var propUrl = (manuUrl + "-" + (string.IsNullOrEmpty(propVal.URL)
//                           ? propVal.Value
//                           : propVal.URL));
//        return ToURL(propUrl);
//    }

    /// <summary>
    /// L???y d???ng URL c???a m???t chu???i b???t k???
    /// </summary>
    /// <param name="phrase">Chu???i c???n chuy???n th??nh URl</param>
    /// <returns>Chu???i d???ng URL</returns>
    private static String ToURL(String phrase)
    {
        String str = RemoveAccent(ToUnsignedVietnamese(phrase.replace("&", "-").replace(",", " "))).toLowerCase();

//        str = Regex.Replace(str, @"[^a-z0-9\s-/?:]", ""); // invalid chars           
//        str = Regex.Replace(str, @"\s+", " ").Trim(); // convert multiple spaces into one space   
        str = str.substring(0, str.length() <= 150 ? str.length() : 150).trim(); // cut and trim it   
//        str = Regex.Replace(str, @"\s", "-"); // hyphens   
//        str = str.Replace("?", "");

        return str;
    }

    /// <summary>
    /// Chuy???n m??i v??? chu???n ASCII ????? ?????m b???o lo???i t???t c??? c??c d???u ?????t bi???t
    /// </summary>
    /// <param name="txt">Chu???i c???n chuy???n</param>
    /// <returns>Chu???i ???? m?? h??a</returns>
    private static String RemoveAccent(String txt)
    {
    	return Normalizer
				.normalize(txt, Normalizer.Form.NFD)
				.replaceAll("[^\\p{ASCII}]", "");
    }

    /// <summary>
    /// B??? d???u ti???ng Vi???t c???a m???t chu???i b???t k???
    /// </summary>
    /// <param name="vietnamese">Chu???i ti???ng Vi???t c???n b??? d???u</param>
    /// <returns>Chu???i ???? kh??? d???u</returns>
    private static String ToUnsignedVietnamese(String vietnamese)
    {
        if (vietnamese == null) return "";
        String nfdNormalizedString = Normalizer.normalize(vietnamese, Normalizer.Form.NFD); 
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }
}
