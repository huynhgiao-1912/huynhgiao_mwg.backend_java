package mwg.wb.business;

import mwg.wb.common.DidxHelper;
import mwg.wb.common.Utils;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductCategoryBO;
import mwg.wb.model.products.ProductLanguageBO;
import mwg.wb.model.products.ProductManuBO;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductUrlHelper {

	private final static String seoUrlPattern = "[^a-zA-Z0-9-]";

	private static final char[] SOURCE_CHARACTERS = { 'À', 'Á', 'Â', 'Ã', 'È', 'É', 'Ê', 'Ì', 'Í', 'Ò', 'Ó', 'Ô', 'Õ',
			'Ù', 'Ú', 'Ý', 'à', 'á', 'â', 'ã', 'è', 'é', 'ê', 'ì', 'í', 'ò', 'ó', 'ô', 'õ', 'ù', 'ú', 'ý', 'Ă', 'ă',
			'Đ', 'đ', 'Ĩ', 'ĩ', 'Ũ', 'ũ', 'Ơ', 'ơ', 'Ư', 'ư', 'Ạ', 'ạ', 'Ả', 'ả', 'Ấ', 'ấ', 'Ầ', 'ầ', 'Ẩ', 'ẩ', 'Ẫ',
			'ẫ', 'Ậ', 'ậ', 'Ắ', 'ắ', 'Ằ', 'ằ', 'Ẳ', 'ẳ', 'Ẵ', 'ẵ', 'Ặ', 'ặ', 'Ẹ', 'ẹ', 'Ẻ', 'ẻ', 'Ẽ', 'ẽ', 'Ế', 'ế',
			'Ề', 'ề', 'Ể', 'ể', 'Ễ', 'ễ', 'Ệ', 'ệ', 'Ỉ', 'ỉ', 'Ị', 'ị', 'Ọ', 'ọ', 'Ỏ', 'ỏ', 'Ố', 'ố', 'Ồ', 'ồ', 'Ổ',
			'ổ', 'Ỗ', 'ỗ', 'Ộ', 'ộ', 'Ớ', 'ớ', 'Ờ', 'ờ', 'Ở', 'ở', 'Ỡ', 'ỡ', 'Ợ', 'ợ', 'Ụ', 'ụ', 'Ủ', 'ủ', 'Ứ', 'ứ',
			'Ừ', 'ừ', 'Ử', 'ử', 'Ữ', 'ữ', 'Ự', 'ự', };

	private static final char[] DESTINATION_CHARACTERS = { 'A', 'A', 'A', 'A', 'E', 'E', 'E', 'I', 'I', 'O', 'O', 'O',
			'O', 'U', 'U', 'Y', 'a', 'a', 'a', 'a', 'e', 'e', 'e', 'i', 'i', 'o', 'o', 'o', 'o', 'u', 'u', 'y', 'A',
			'a', 'D', 'd', 'I', 'i', 'U', 'u', 'O', 'o', 'U', 'u', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a',
			'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'E', 'e', 'E', 'e', 'E', 'e', 'E',
			'e', 'E', 'e', 'E', 'e', 'E', 'e', 'E', 'e', 'I', 'i', 'I', 'i', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o',
			'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'U', 'u', 'U', 'u', 'U',
			'u', 'U', 'u', 'U', 'u', 'U', 'u', 'U', 'u', };

	public static char removeAccent(char ch) {
		int index = Arrays.binarySearch(SOURCE_CHARACTERS, ch);
		if (index >= 0) {
			ch = DESTINATION_CHARACTERS[index];
		}
		return ch;
	}

	public static String removeAccent(String str) {
		StringBuilder sb = new StringBuilder(str);
		for (int i = 0; i < sb.length(); i++) {
			sb.setCharAt(i, removeAccent(sb.charAt(i)));
		}
		return sb.toString();
	}

	public static String GenSEOProductUrl(ProductBO objectData) {
		if (objectData == null) {
			return "";
		}
		String ProductName = objectData.ProductName + "";
		String strProductUrl, strCategoryUrl;
		if (objectData.ProductCategoryLangBO != null
				&& !Utils.StringIsEmpty(objectData.ProductCategoryLangBO.CategoryName)) {
			strCategoryUrl = GenSEOUrl(objectData.ProductCategoryLangBO.URL,
					objectData.ProductCategoryLangBO.CategoryName);
		} else if (objectData.ProductCategoryBO != null
				&& !Utils.StringIsEmpty(objectData.ProductCategoryBO.CategoryName)) {
			strCategoryUrl = GenSEOUrl(objectData.ProductCategoryBO.URL, objectData.ProductCategoryBO.CategoryName);
		} else {
			strCategoryUrl = GenSEOUrl(objectData.CategoryName);
		}

		if (strCategoryUrl.equals("may-anh-ky-thuat-so")) {
			strCategoryUrl = "may-anh-so";
		} else if (strCategoryUrl.equals("dien-thoai-di-dong")) {
			strCategoryUrl = "dien-thoai";
		} else if (strCategoryUrl.equals("may-ep")) {
			strCategoryUrl = "may-ep-trai-cay";
		}
		String strColorPattern = "#(.)*";
		if (objectData.ProductLanguageBO != null && (!Utils.StringIsEmpty(objectData.ProductLanguageBO.URL)
				|| !Utils.StringIsEmpty(objectData.ProductLanguageBO.ProductName))) {
			// loại bỏ phần màu trong tên sản phẩm nếu có
			String tmp = "";
			if (!Utils.StringIsEmpty(objectData.ProductLanguageBO.ProductName)) {
				tmp = objectData.ProductLanguageBO.ProductName.contains("#")
						? replaceMatches(objectData.ProductLanguageBO.ProductName, strColorPattern, "")
						: objectData.ProductLanguageBO.ProductName;
			}
			strProductUrl = GenSEOUrl(objectData.ProductLanguageBO.URL, tmp);
		} else {
			// loại bỏ phần màu trong tên sản phẩm nếu có
			String tmp = ProductName.contains("#") ? replaceMatches(ProductName, strColorPattern, "") : ProductName;
			strProductUrl = GenSEOUrl(objectData.URL, tmp);
		}

		return strCategoryUrl + "/" + strProductUrl;
	}

	public static String GenSEOUrl(String checkingField, String defaultField) {
		if (Utils.StringIsEmpty(checkingField)) {
			if (Utils.StringIsEmpty(defaultField)) {
				return "";
			}
			return GenSEOUrl(defaultField);
		}

		return GenSEOUrl(checkingField);
	}

	public static String replaceMatches(String html, String PATTERN, String REPLACEMENT) {
		Pattern COMPILED_PATTERN = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE);
		// Pattern replace = Pattern.compile("\\s+");
		Matcher matcher = COMPILED_PATTERN.matcher(html);
		html = matcher.replaceAll(REPLACEMENT);
		return html;
	}

	public static String GenSEOUrl(String strInput) {
		if (Utils.StringIsEmpty(strInput)) {
			return "";
		}
		strInput = DidxHelper.convertUnicode(strInput);
		// String sNewUrl = Globals.FormatURLText(strInput);
		String sNewUrl = removeAccent(strInput);
		sNewUrl = sNewUrl.replace("_", "").replace(".", "");
		sNewUrl = replaceMatches(sNewUrl, seoUrlPattern, "-");
		sNewUrl = StringUtils.strip(replaceMatches(sNewUrl, "(-)+", "-"), "-").toLowerCase();
		if (sNewUrl.equals("dien-thoai-di-dong")) {
			return "dien-thoai";
		}
		return sNewUrl;
	}

	public static String GenCategoryUrl(ProductCategoryBO category) {
		String categoryUrl = Utils.StringIsEmpty(category.URL) ? GenSEOUrl(category.CategoryName) : category.URL;

		return categoryUrl;
	}

	public static String GenManufacturerUrl(ProductManuBO manufacture) {
		var cateUrl = GenCategoryUrl(manufacture.ProductCategoryBO);
		var manuUrl = GenSEOUrl(Utils.StringIsEmpty(manufacture.URL)
				? manufacture.ManufacturerName
				: manufacture.URL);
		return cateUrl + "-" + manuUrl;
	}

	public static String GenManufacturerUrl(ProductManuBO manufacture, ProductCategoryBO category) {
		var cateUrl = GenCategoryUrl(category);
		var manuUrl = GenSEOUrl(Utils.StringIsEmpty(manufacture.URL)
				? manufacture.ManufacturerName
				: manufacture.URL);
		return cateUrl + "-" + manuUrl;
	}

	public static String GenSEOProductUrlCambodia(ProductBO objectData, ProductCategoryBO CategoryLangEn,
			ProductLanguageBO productLanguageEn) {
		if (objectData == null) {
			return "";
		}
		String strProductUrl, strCategoryUrl;
		if (CategoryLangEn != null && !Utils.StringIsEmpty(CategoryLangEn.CategoryName)) {
			strCategoryUrl = GenSEOUrl(CategoryLangEn.URL, CategoryLangEn.CategoryName);
		} else if (CategoryLangEn != null && !Utils.StringIsEmpty(CategoryLangEn.CategoryName)) {
			strCategoryUrl = GenSEOUrl(CategoryLangEn.URL, CategoryLangEn.CategoryName);
		} else {
			strCategoryUrl = GenSEOUrl(objectData.CategoryName);
		}

		String strColorPattern = "#(.)*";
		if (objectData.ProductLanguageBO != null && !Utils.StringIsEmpty(objectData.ProductLanguageBO.ProductName)) {
			// loại bỏ phần màu trong tên sản phẩm nếu có
			String tmp = productLanguageEn.ProductName.contains("#")
					? replaceMatches(productLanguageEn.ProductName, strColorPattern, "")
					: productLanguageEn.ProductName;
			strProductUrl = GenSEOUrl(productLanguageEn.URL, tmp);
		} else {
			// loại bỏ phần màu trong tên sản phẩm nếu có
			String tmp = objectData.ProductName.contains("#")
					? replaceMatches(objectData.ProductName, strColorPattern, "")
					: objectData.ProductName;
			strProductUrl = GenSEOUrl(objectData.URL, tmp);
		}

		return strCategoryUrl + "/" + strProductUrl;
	}

}
