package mwg.wb.common;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DidxHelper {

	public static final String regexEmoji = "[^\\p{L}\\p{N}\\p{P}\\p{Z}]";
	// SiteID BrandID CompanyID LanguageID BrandName
	// 1 1 1 vi-VN tgdd
	// 2 2 1 vi-VN dmx
	// 6 6 6 km-KH bigphone
	// 11 3 2 vi-VN bhx
	// 12 10 1 vi-VN dienthoaisieure
	// 10 8 8 vi-VN ankhang
	// Brandid 4: tgdđ + đmx
	public static int getSitebyBrandID(int BrandID) {
		switch (BrandID) {
		case 1: // TGDD
		case 2: // DMX
			return BrandID; // siteid = brandid
		case 10: // dtsr
			return 12;
		case 3: // bhx
			return 11;
		case 6: // bigphone
		case 11: // bigphone plus
			return 6;
		default:
			return 0;
		}
	}

	public static int getCompanyBySiteID(int siteID) {
		switch (siteID) {
		case 1: // TGDD
		case 2: // DMX
			return 1;
		case 6: // bigphone
			return 6;
		case 11: // bhx
			return 2;
		case 10:
			return 8;
		default:
			return 0;
		}
	}
	 
	public static String getLangByBrandID(int BrandID) {
		switch (BrandID) {
		case 11:
		case 6:
			return "km-KH";
		default:
			return "vi-VN";
		}
	}

	public static String getLangBySiteID(int siteID) {
		switch (siteID) {
		case 11:
		case 6:
			return "km-KH";
		default:
			return "vi-VN";
		}
	}

	public static int getPriceAreaBySiteID(int SiteID, String Lang) {
		switch (SiteID) {
		case 1: // tgdd or bigphone
			return Lang.equals("km-KH") ? 647 : 13;
		case 2: // dmx
		case 12: // dtsr
			return 13;
		case 11: // bhx?
			return 644;
		case 6: // bigphone
			return 647;
		default:
			return 0;
		}
	}

	public static int getDefaultPriceAreaBySiteID(int SiteID, String Lang) {
		// khu vuc gia de tinh trang thai kinh doanh/con hang
		switch (SiteID) {
		case 1: // tgdd or bigphone
			return Lang.equals("km-KH") ? 647 : 13;
		case 2: // dmx
			return 13;
		case 12: // dtsr
			return 678;
		case 11: // bhx?
			return 644;
		case 6: // bigphone
			return 647;
		default:
			return 0;
		}
	}

	public static int getDefaultProvinceIDBySiteID(int siteID) {
		switch (siteID) {
		case 6:
			return 163;
		default:
			return 3;
		}
	}

	public static int getDefaultPriceAreaOrgBySiteID(int SiteID, String Lang) {

		if (SiteID == 12 && Lang.equals("vi-VN")) { // DTSR
			return 13;
		}

		return 0;
	}

	public static int getBrandBySite(int SiteID, String Lang) {
		switch (SiteID) {
		case 1: // tgdd or bigphone
			return Lang.equals("km-KH") ? 11 : 1;
		case 2: // dmx
			return 2;
		case 12: // dtsr
			return 10;
		case 11: // bhx?
			return 3;
		case 6: // bigphone
			return 11;// 6;
		default:
			return 0;
		}
	}

	public static int getCompanyID(int siteID) {
		switch (siteID) {
		case 1:
		case 2:
		case 12:
			return 1;
		case 11:
			return 2;
		case 10:
			return 8;
		case 6:
			return siteID;
		default:
			return 0;
		}
	}

	public static int getCountryBySiteID(int siteID) {
		switch (siteID) {
			case 6: // bluetronics
				return 0;
			default:
				return 2;
		}
	}

	public static String genObjectTerm(int SiteID, String Lang, String ObjectID) throws Exception {
		if (SiteID <= 0)
			SiteID = 1;

		if (Lang.isEmpty())
			Lang = "vi-VN";

		return SiteID + "_" + ObjectID + "_" + Lang.toLowerCase().replace("-", "_").replace(" ", "_");
	}

	public static String genKeyBySite(String Prefix, int SiteID, String Lang) throws Exception {
		if (SiteID <= 0)
			SiteID = 1;

		if (Lang.isEmpty())
			Lang = "vi-VN";

		return Prefix + "_" + SiteID + "_" + Lang.toLowerCase().replace("-", "_").replace(" ", "_");
	}

	public static String fotmatKeywordIndexField(String input) throws Exception {
		if (Utils.StringIsEmpty(input))
			return null;
		// String rsl =
		// StringEscapeUtils.unescapeHtml(StringEscapeUtils.unescapeHtml(input));
		input = "__SERD__ " + input;
		String rsl = Jsoup.parse(input).text();
		if (Utils.StringIsEmpty(rsl))
			return null;
		return rsl.replaceAll("[@#,;\\\"!`\n\t“”()/.\\[\\]\\:]+", " ").replaceAll("[-]+", " ").replaceAll("\\+", "plus")
				.toLowerCase().trim();
	}

	public static String FormatKeywordField(String input) {
		if (Utils.StringIsEmpty(input))
			return null;
		// String rsl =
		// StringEscapeUtils.unescapeHtml(StringEscapeUtils.unescapeHtml(input));
		String rsl = Jsoup.parse(input).text();
		if (Utils.StringIsEmpty(rsl))
			return null;
		return rsl.replaceAll("[@#,;\\\"!`\n\t“”()/.\\[\\]\\:]+", " ").replaceAll("[-]+", " ").replaceAll("\\+", "plus")
				.toLowerCase().trim();
	}
	public static String FormatKeywordFieldForSE(String input) {
		if (Utils.StringIsEmpty(input))
			return null;
		// String rsl =
		// StringEscapeUtils.unescapeHtml(StringEscapeUtils.unescapeHtml(input));
		String rsl = Jsoup.parse(input).text();
		if (Utils.StringIsEmpty(rsl))
			return null;
		return rsl.replaceAll("[@#;\\\"!`\n\t“”()/\\[\\]\\:]+", " ").replaceAll("[-]+", " ").replaceAll("\\+", "plus")
				.toLowerCase().trim();
	}
	public static String FormatKeywordField3(String input) {
		if (Utils.StringIsEmpty(input))
			return null;
		// String rsl =
		// StringEscapeUtils.unescapeHtml(StringEscapeUtils.unescapeHtml(input));
		String rsl = Jsoup.parse(input).text();
		if (Utils.StringIsEmpty(rsl))
			return null;
		return rsl.replaceAll("[@#,;\\\"!`\n\t“”()/.\\[\\]\\:]+", "").replaceAll("[-]+", "").replaceAll("\\+", "plus")
				.toLowerCase().trim();
	}

	public static String FormatKeywordAZ(String input) {
		if (Utils.StringIsEmpty(input))
			return null;
		// String rsl =
		// StringEscapeUtils.unescapeHtml(StringEscapeUtils.unescapeHtml(input));
		String rsl = Jsoup.parse(input).text();
		if (Utils.StringIsEmpty(rsl))
			return null;
		return rsl.replaceAll("[^a-zA-Z0-9\\. ]+", "").toLowerCase().trim();

	}

	public static String FormatKeywordSearchField(String input) {
		if (input == null)
			return null;
		// String rsl =
		// StringEscapeUtils.unescapeHtml(StringEscapeUtils.unescapeHtml(input));
		String rsl = Jsoup.parse(input).text();
		rsl =FormatKeywordEmoji(rsl);
//		return rsl.replaceAll("[@#,;\\\"!`\n\t“”()/.\\[\\]]+", " ").replaceAll("[-]+", " ").replaceAll("\\+", "plus")
//				.toLowerCase().trim();
		return rsl.replaceAll("\\+", "plus").replaceAll(
				"[^.a-z0-9A-Z_àáãạảăắằẳẵặâấầẩẫậèéẹẻẽêềếểễệđìíĩỉịòóõọỏôốồổỗộơớờởỡợùúũụủưứừửữựỳỵỷỹýÀÁÃẠẢĂẮẰẲẴẶÂẤẦẨẪẬÈÉẸẺẼÊỀẾỂỄỆĐÌÍĨỈỊÒÓÕỌỎÔỐỒỔỖỘƠỚỜỞỠỢÙÚŨỤỦƯỨỪỬỮỰỲỴỶỸÝ]+",
				" ").toLowerCase().trim();
	}
	public static String FormatKeywordEmoji(String input){
		return  input.replaceAll(regexEmoji, "").trim();
	}

	public static String FormatKeywordSearchFieldCam(String input) {
		if (input == null)
			return null;
		// String rsl =
		// StringEscapeUtils.unescapeHtml(StringEscapeUtils.unescapeHtml(input));
		String rsl = Jsoup.parse(input).text();
		return rsl.replaceAll("[:@#,;\\\"!`\n\t“”()/.\\[\\]]+", " ").replaceAll("[-]+", " ").replaceAll("\\+", "plus")
				.toLowerCase().trim();
//		return rsl.replaceAll("\\+", "plus").replaceAll(
//				"[^.a-z0-9A-Z_àáãạảăắằẳẵặâấầẩẫậèéẹẻẽêềếểễệđìíĩỉịòóõọỏôốồổỗộơớờởỡợùúũụủưứừửữựỳỵỷỹýÀÁÃẠẢĂẮẰẲẴẶÂẤẦẨẪẬÈÉẸẺẼÊỀẾỂỄỆĐÌÍĨỈỊÒÓÕỌỎÔỐỒỔỖỘƠỚỜỞỠỢÙÚŨỤỦƯỨỪỬỮỰỲỴỶỸÝ]+",
//				" ").toLowerCase().trim();
	}

	public static String ConvertToUnsign3(String str) {
		String temp = Normalizer.normalize(str, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
	}

	public static String FilterVietkey(String strSource) {
		if (Utils.StringIsEmpty(strSource))
			return null;

		strSource = convertISOToUnicode(strSource);
		strSource = strSource.replace("\0", "");
		if (strSource.strip().length() == 0)
			return "";

		return ConvertToUnsign3(strSource);// bo dau
	}

	public static String convertISOToUnicode(String strSource) {
		String strUni = "á à ả ã ạ Á À Ả Ã Ạ ă ắ ằ ẳ ẵ ặ Ă Ắ Ằ Ẳ Ẵ Ặ â ấ ầ ẩ ẫ ậ Â Ấ Ầ Ẩ Ẫ Ậ đ Đ é è ẻ ẽ ẹ É È Ẻ Ẽ Ẹ ê ế ề ể ễ ệ Ê Ế Ề Ể Ễ Ệ í ì ỉ ĩ ị Í Ì Ỉ Ĩ Ị ó ò ỏ õ ọ Ó Ò Ỏ Õ Ọ ô ố ồ ổ ỗ ộ Ô Ố Ồ Ổ Ỗ Ộ ơ ớ ờ ở ỡ ợ Ơ Ớ Ờ Ở Ỡ Ợ ú ù ủ ũ ụ Ú Ù Ủ Ũ Ụ ư ứ ừ ử ữ ự Ư Ứ Ừ Ử Ữ Ự ý ỳ ỷ ỹ ỵ Ý Ỳ Ỷ Ỹ Ỵ";
		String strISO = "á à &#7843; ã &#7841; Á À &#7842; Ã &#7840; &#259; &#7855; &#7857; &#7859; &#7861; &#7863; &#258; &#7854; &#7856; &#7858; &#7860; &#7862; â &#7845; &#7847; &#7849; &#7851; &#7853; Â &#7844; &#7846; &#7848; &#7850; &#7852; &#273; &#272; é è &#7867; "
				+ "&#7869; &#7865; É È &#7866; &#7868; &#7864; ê &#7871; &#7873; &#7875; &#7877; &#7879; Ê &#7870; &#7872; &#7874; &#7876; &#7878; í ì &#7881; &#297; &#7883; Í Ì &#7880; &#296; &#7882; ó ò &#7887; õ &#7885; Ó Ò &#7886; Õ &#7884; ô "
				+ "&#7889; &#7891; &#7893; &#7895; &#7897; Ô &#7888; &#7890; &#7892; &#7894; &#7896; &#417; &#7899; &#7901; &#7903; &#7905; &#7907; &#416; &#7898; &#7900; &#7902; &#7904; &#7906; ú ù &#7911; &#361; &#7909; Ú Ù &#7910; &#360; &#7908; &#432; &#7913; &#7915; &#7917; &#7919; &#7921; &#431; "
				+ "&#7912; &#7914; &#7916; &#7918; &#7920; ý &#7923; &#7927; &#7929; &#7925; Ý &#7922; &#7926; &#7928; &#7924;";
		String[] arrCharUni = strUni.split(" ");
		String[] arrCharISO = strISO.split(" ");
		String strResult = strSource;
		for (int i = 0; i < arrCharUni.length; i++)
			strResult = strResult.replace(arrCharISO[i], arrCharUni[i]);
		strUni = "À Á Â Ã Ä Å Æ Ç È É Ê Ë Ì Í Î Ï Ð Ñ Ò Ó Ô Õ Ö Ø Ù Ú Û Ü Ý Þ ß à á â ã ä å æ ç è é ê ë ì í î ï ð ñ ò ó ô õ ö ø ù ú û ü ý þ ÿ";
		strISO = "&#192; &#193; &#194; &#195; &#196; &#197; &#198; &#199; &#200; &#201; &#202; &#203; &#204; &#205; &#206; "
				+ "&#207; &#208; &#209; &#210; &#211; &#212; &#213; &#214; &#216; &#217; &#218; &#219; &#220; &#221; &#222; "
				+ "&#223; &#224; &#225; &#226; &#227; &#228; &#229; &#230; &#231; &#232; &#233; &#234; &#235; &#236; &#237; &#238; &#239; "
				+ "&#240; &#241; &#242; &#243; &#244; &#245; &#246; &#248; &#249; &#250; &#251; &#252; &#253; &#254; &#255;";
		String[] arrCharUni1 = strUni.split(" ");
		String[] arrCharISO1 = strISO.split(" ");
		for (int i = 0; i < arrCharUni1.length; i++)
			strResult = strResult.replace(arrCharISO1[i], arrCharUni1[i]);
		strResult = strResult.replace("\0", "");
		return strResult;
	}

	public static String GenTerm3(String strInput) {
		try {
			if (strInput == null || strInput.isBlank()) {
				return "";
			}
			strInput = FilterVietkey(strInput);
			// String seoUrlPattern = @"[^a-zA-Z0-9-]";
			// String sNewUrl = Globals.FormatURLText(strInput);
			strInput = strInput.replace("_", "").replaceAll("[^a-zA-Z0-9_-]", "").replace("-", "_");

			return strInput.toLowerCase();
		} catch (Exception e) {
			return "";
		}
	}

	public static String GenTerm(String strInput) {
		try {
			if (strInput == null || strInput.isBlank()) {
				return "";
			}
			strInput = FilterVietkey(strInput);
			// String seoUrlPattern = @"[^a-zA-Z0-9-]";
			// String sNewUrl = Globals.FormatURLText(strInput);
			strInput = strInput.replace("_", "").replaceAll("[^a-zA-Z0-9_-]", "-").replace("-", "_");

			return strInput.toLowerCase();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Lấy Elastic key của SP
	 * 
	 * @param productID
	 * @param siteID
	 * @param lang
	 * @return
	 */
	public static String GetElasticKey(int productID, int siteID, String lang) {
		return productID + "_" + siteID + "_" + lang.toLowerCase().replace("-", "_");
	}

	public static boolean isLive() {
		return !isStaging() && !isLocal() && !isBeta();
	}

	public static boolean isBeta() {
		try {
			// return InetAddress.getLocalHost().getHostName().equals("stagingweb-api15");
			return InetAddress.getLocalHost().getHostName().equals("itdev-microserivces-4-123");
		} catch (UnknownHostException e) {
			return false;
		}
	}

	public static boolean isLocal() {
		try {
			// return InetAddress.getLocalHost().getHostName().equals("stagingweb-api15");
			String a = InetAddress.getLocalHost().getHostName();
			return a.equals("95838-DHAN") || a.equals("90538-CHHANH") || a.equalsIgnoreCase("142990-NTNghia") || a.equals("142791-TUANVU");
		} catch (UnknownHostException e) {
			return false;
		}
	}

	public static boolean isHanh() {
		try {
			// return InetAddress.getLocalHost().getHostName().equals("stagingweb-api15");
			String a = InetAddress.getLocalHost().getHostName();
			return a.equals("90538-CHHANH");
		} catch (UnknownHostException e) {
			return false;
		}
	}
	public static boolean isNghia() {
		try {
			// return InetAddress.getLocalHost().getHostName().equals("stagingweb-api15");
			String a = InetAddress.getLocalHost().getHostName();
			return a.equals("142990-NTNghia");
		} catch (UnknownHostException e) {
			return false;
		}
	}
	public static boolean isVu() {
		try {
			String a = InetAddress.getLocalHost().getHostName();
			return a.equals("142791-TUANVU");
		} catch (UnknownHostException e) {
			return false;
		}
	}

	public static boolean hostNameIs(String name) {
		try {
			String a = InetAddress.getLocalHost().getHostName();
			return a.equals(name);
		} catch (UnknownHostException e) {
			return false;
		}
	}

	public static boolean isLoc() {
		try {
			String a = InetAddress.getLocalHost().getHostName();
			return a.equals("LAPTOP-75278AIR");
		} catch (UnknownHostException e) {
			return false;
		}
	}

	public static boolean isPhi() {
		try {
			// return InetAddress.getLocalHost().getHostName().equals("stagingweb-api15");
			String a = InetAddress.getLocalHost().getHostName();
			return a.equals("61814-phi");
		} catch (UnknownHostException e) {
			return false;
		}
	}

	public static boolean isDat() {
		try {
			// return InetAddress.getLocalHost().getHostName().equals("stagingweb-api15");
			String a = InetAddress.getLocalHost().getHostName();
			return a.equals("NgoDat-58205");
		} catch (UnknownHostException e) {
			return false;
		}
	}

	public static boolean isStaging() {
		try {
			return InetAddress.getLocalHost().getHostName().equals("stagingweb-api198");

		} catch (UnknownHostException e) {
			return false;
		}
	}

	public static String ConvertToTagsTerm(String tags) {
		if (Utils.StringIsEmpty(tags))
			return "";
		String res = "";
		String[] tagsArray = tags.split(",");
		for (var itemtag : tagsArray) {
			if (!Utils.StringIsEmpty(itemtag)) {
				String TagName = GenTerm(itemtag.trim());
				res = res + " " + TagName;
			}

		}
		res = StringUtils.strip(res.trim(), ",").toLowerCase();
		return res;
	}
//
//    public static String genTerm(String strInput) throws Exception {
//        if (String.IsNullOrEmpty(strInput))
//        {
//            return String.Empty;
//        }
//         
//        strInput = FilterVietkey(strInput);
//        String seoUrlPattern = "[^a-zA-Z0-9-]";
//        String sNewUrl = Globals.FormatURLText(strInput);
//        sNewUrl = sNewUrl.Replace("_", "");
//        sNewUrl = Regex.Replace(sNewUrl, seoUrlPattern, "");
//        sNewUrl = Regex.Replace(sNewUrl, "(-)+", "-").Trim('-').ToLower();
//        sNewUrl = sNewUrl.Replace("-", "x");
//        return sNewUrl.ToLower();
//    }
//
//    public static String genSEOUrl(String strInput) throws Exception {
//        String seoUrlPattern = "[^a-zA-Z0-9-]";
//        if (String.IsNullOrEmpty(strInput))
//        {
//            return String.Empty;
//        }
//         
//        String sNewUrl = Globals.FormatURLText(strInput);
//        sNewUrl = sNewUrl.Replace("_", "");
//        sNewUrl = Regex.Replace(sNewUrl, seoUrlPattern, "-");
//        sNewUrl = Regex.Replace(sNewUrl, "(-)+", "-").Trim('-').ToLower();
//        if (StringSupport.equals(sNewUrl, "dien-thoai-di-dong"))
//        {
//            return "dtdd";
//        }
//         
//        return sNewUrl;
//    }
//
//    public static String formatIndexKeywordField(String input) throws Exception {
//        input = "__SERD__ " + input;
//        Regex rgx = new Regex("<[^>]+>", RegexOptions.IgnoreCase);
//        String rsl = rgx.Replace(WebUtility.HtmlDecode(input), "");
//        rgx = new Regex("[,;\"!`\\n\\t“”]+");
//        return rgx.Replace(rsl, " , ").ToLower();
//    }
//
//    public static String genTerm2(String strInput) throws Exception {
//        if (String.IsNullOrEmpty(strInput))
//        {
//            return String.Empty;
//        }
//         
//        strInput = FilterVietkey(strInput);
//        String seoUrlPattern = "[^a-zA-Z0-9-]";
//        String sNewUrl = Globals.FormatURLText(strInput);
//        sNewUrl = sNewUrl.Replace("_", "");
//        sNewUrl = Regex.Replace(sNewUrl, seoUrlPattern, "");
//        sNewUrl = Regex.Replace(sNewUrl, "(-)+", "-").Trim('-').ToLower();
//        sNewUrl = sNewUrl.Replace("-", "nqh");
//        return sNewUrl.ToLower();
//    }

	public static String GenTermVN(String strInput) {

		try {
			if (strInput == null || strInput.isBlank()) {
				return "";
			}

			strInput = StringUtils.strip(strInput.replace("_", "").replaceAll("(-)+", "-").toLowerCase(), "-")
					.replace("-", "_");

			return strInput.toLowerCase();
		} catch (Exception e) {
			return "";
		}
	}

	public static String ConvertToTagsTermVN(String keyWord) {
		// TODO Auto-generated method stub
		return null;
	}

	public static ObjectMapper generateJsonMapper(String dateFormat) {
		var mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(dateFormat);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}

	public static ObjectMapper generateNonNullJsonMapper(String dateFormat) {
		var mapper = generateJsonMapper(dateFormat);
		mapper.setSerializationInclusion(Include.NON_NULL);
		return mapper;
	}

	public static ObjectMapper generateNonDefaultJsonMapper(String dateFormat) {
		var mapper = generateJsonMapper(dateFormat);
		mapper.setSerializationInclusion(Include.NON_DEFAULT);
		return mapper;
	}

	public static String GenTermKeyWord(String keyword) {// hàm này để lấy ra keyword term es
		// input : văn hạnh van hạnh
		// output: van_hanh vanh_hanh
		if (keyword == null || keyword.isBlank())
			return "";
		var arrString = keyword.split(",");
		if (arrString == null || arrString.length == 0)
			return "";
		String tmpString = "";
		for (String item : arrString) {
			if (!Utils.StringIsEmpty(item)) {
				var tmp = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(item.trim()));
				if (!Utils.StringIsEmpty(tmp))
					tmp = tmp.replace(' ', '_') .replace("&","_")+ " ";
				tmpString += tmp;
			}

		}

		return tmpString.trim();
	}

	/**
	 * Cat stream thanh nhieu phan
	 * 
	 * @param <T>
	 * @param stream
	 * @param partitionSize
	 * @return
	 */
	public static <T> Stream<Stream<T>> partition(Stream<T> stream, int partitionSize) {
		return StreamSupport
				.stream(new PartitioningSpliterator<>(stream.spliterator(), partitionSize), stream.isParallel())
				.map(sp -> StreamSupport.stream(sp, stream.isParallel()));
	}

	public static String getStackTrace(Throwable e) {
		try {
			return Arrays.stream(e.getStackTrace()).map(x -> x.toString()).collect(Collectors.joining(", "));
		} catch (Exception ex) {
			return "";
		}
	}

	public static String convertUnicode(String unicode_str) {
		unicode_str = unicode_str.replace("\u0065\u0309", "\u1EBB")    ;// ẻ
		unicode_str = unicode_str.replace("\u0065\u0301", "\u00E9")    ;// é
		unicode_str = unicode_str.replace("\u0065\u0300", "\u00E8")    ;// è
		unicode_str = unicode_str.replace("\u0065\u0323", "\u1EB9")    ;// ẹ
		unicode_str = unicode_str.replace("\u0065\u0303", "\u1EBD")    ;// ẽ
		unicode_str = unicode_str.replace("\u00EA\u0309", "\u1EC3")    ;// ể
		unicode_str = unicode_str.replace("\u00EA\u0301", "\u1EBF")    ;// ế
		unicode_str = unicode_str.replace("\u00EA\u0300", "\u1EC1")    ;// ề
		unicode_str = unicode_str.replace("\u00EA\u0323", "\u1EC7")    ;// ệ
		unicode_str = unicode_str.replace("\u00EA\u0303", "\u1EC5")    ;// ễ
		unicode_str = unicode_str.replace("\u0079\u0309", "\u1EF7")    ;// ỷ
		unicode_str = unicode_str.replace("\u0079\u0301", "\u00FD")    ;// ý
		unicode_str = unicode_str.replace("\u0079\u0300", "\u1EF3")    ;// ỳ
		unicode_str = unicode_str.replace("\u0079\u0323", "\u1EF5")    ;// ỵ
		unicode_str = unicode_str.replace("\u0079\u0303", "\u1EF9")    ;// ỹ
		unicode_str = unicode_str.replace("\u0075\u0309", "\u1EE7")    ;// ủ
		unicode_str = unicode_str.replace("\u0075\u0301", "\u00FA")    ;// ú
		unicode_str = unicode_str.replace("\u0075\u0300", "\u00F9")    ;// ù
		unicode_str = unicode_str.replace("\u0075\u0323", "\u1EE5")    ;// ụ
		unicode_str = unicode_str.replace("\u0075\u0303", "\u0169")    ;// ũ
		unicode_str = unicode_str.replace("\u01B0\u0309", "\u1EED")    ;// ử
		unicode_str = unicode_str.replace("\u01B0\u0301", "\u1EE9")    ;// ứ
		unicode_str = unicode_str.replace("\u01B0\u0300", "\u1EEB")    ;// ừ
		unicode_str = unicode_str.replace("\u01B0\u0323", "\u1EF1")    ;// ự
		unicode_str = unicode_str.replace("\u01B0\u0303", "\u1EEF")    ;// ữ
		unicode_str = unicode_str.replace("\u0069\u0309", "\u1EC9")    ;// ỉ
		unicode_str = unicode_str.replace("\u0069\u0301", "\u00ED")    ;// í
		unicode_str = unicode_str.replace("\u0069\u0300", "\u00EC")    ;// ì
		unicode_str = unicode_str.replace("\u0069\u0323", "\u1ECB")    ;// ị
		unicode_str = unicode_str.replace("\u0069\u0303", "\u0129")    ;// ĩ
		unicode_str = unicode_str.replace("\u006F\u0309", "\u1ECF")    ;// ỏ
		unicode_str = unicode_str.replace("\u006F\u0301", "\u00F3")    ;// ó
		unicode_str = unicode_str.replace("\u006F\u0300", "\u00F2")    ;// ò
		unicode_str = unicode_str.replace("\u006F\u0323", "\u1ECD")    ;// ọ
		unicode_str = unicode_str.replace("\u006F\u0303", "\u00F5")    ;// õ
		unicode_str = unicode_str.replace("\u01A1\u0309", "\u1EDF")    ;// ở
		unicode_str = unicode_str.replace("\u01A1\u0301", "\u1EDB")    ;// ớ
		unicode_str = unicode_str.replace("\u01A1\u0300", "\u1EDD")    ;// ờ
		unicode_str = unicode_str.replace("\u01A1\u0323", "\u1EE3")    ;// ợ
		unicode_str = unicode_str.replace("\u01A1\u0303", "\u1EE1")    ;// ỡ
		unicode_str = unicode_str.replace("\u00F4\u0309", "\u1ED5")    ;// ổ
		unicode_str = unicode_str.replace("\u00F4\u0301", "\u1ED1")    ;// ố
		unicode_str = unicode_str.replace("\u00F4\u0300", "\u1ED3")    ;// ồ
		unicode_str = unicode_str.replace("\u00F4\u0323", "\u1ED9")    ;// ộ
		unicode_str = unicode_str.replace("\u00F4\u0303", "\u1ED7")    ;// ỗ
		unicode_str = unicode_str.replace("\u0061\u0309", "\u1EA3")    ;// ả
		unicode_str = unicode_str.replace("\u0061\u0301", "\u00E1")    ;// á
		unicode_str = unicode_str.replace("\u0061\u0300", "\u00E0")    ;// à
		unicode_str = unicode_str.replace("\u0061\u0323", "\u1EA1")    ;// ạ
		unicode_str = unicode_str.replace("\u0061\u0303", "\u00E3")    ;// ã
		unicode_str = unicode_str.replace("\u0103\u0309", "\u1EB3")    ;// ẳ
		unicode_str = unicode_str.replace("\u0103\u0301", "\u1EAF")    ;// ắ
		unicode_str = unicode_str.replace("\u0103\u0300", "\u1EB1")    ;// ằ
		unicode_str = unicode_str.replace("\u0103\u0323", "\u1EB7")    ;// ặ
		unicode_str = unicode_str.replace("\u0103\u0303", "\u1EB5")    ;// ẵ
		unicode_str = unicode_str.replace("\u00E2\u0309", "\u1EA9")    ;// ẩ
		unicode_str = unicode_str.replace("\u00E2\u0301", "\u1EA5")    ;// ấ
		unicode_str = unicode_str.replace("\u00E2\u0300", "\u1EA7")    ;// ầ
		unicode_str = unicode_str.replace("\u00E2\u0323", "\u1EAD")    ;// ậ
		unicode_str = unicode_str.replace("\u00E2\u0303", "\u1EAB")    ;// ẫ
		unicode_str = unicode_str.replace("\u0045\u0309", "\u1EBA")    ;// Ẻ
		unicode_str = unicode_str.replace("\u0045\u0301", "\u00C9")    ;// É
		unicode_str = unicode_str.replace("\u0045\u0300", "\u00C8")    ;// È
		unicode_str = unicode_str.replace("\u0045\u0323", "\u1EB8")    ;// Ẹ
		unicode_str = unicode_str.replace("\u0045\u0303", "\u1EBC")    ;// Ẽ
		unicode_str = unicode_str.replace("\u00CA\u0309", "\u1EC2")    ;// Ể
		unicode_str = unicode_str.replace("\u00CA\u0301", "\u1EBE")    ;// Ế
		unicode_str = unicode_str.replace("\u00CA\u0300", "\u1EC0")    ;// Ề
		unicode_str = unicode_str.replace("\u00CA\u0323", "\u1EC6")    ;// Ệ
		unicode_str = unicode_str.replace("\u00CA\u0303", "\u1EC4")    ;// Ễ
		unicode_str = unicode_str.replace("\u0059\u0309", "\u1EF6")    ;// Ỷ
		unicode_str = unicode_str.replace("\u0059\u0301", "\u00DD")    ;// Ý
		unicode_str = unicode_str.replace("\u0059\u0300", "\u1EF2")    ;// Ỳ
		unicode_str = unicode_str.replace("\u0059\u0323", "\u1EF4")    ;// Ỵ
		unicode_str = unicode_str.replace("\u0059\u0303", "\u1EF8")    ;// Ỹ
		unicode_str = unicode_str.replace("\u0055\u0309", "\u1EE6")    ;// Ủ
		unicode_str = unicode_str.replace("\u0055\u0301", "\u00DA")    ;// Ú
		unicode_str = unicode_str.replace("\u0055\u0300", "\u00D9")    ;// Ù
		unicode_str = unicode_str.replace("\u0055\u0323", "\u1EE4")    ;// Ụ
		unicode_str = unicode_str.replace("\u0055\u0303", "\u0168")    ;// Ũ
		unicode_str = unicode_str.replace("\u01AF\u0309", "\u1EEC")    ;// Ử
		unicode_str = unicode_str.replace("\u01AF\u0301", "\u1EE8")    ;// Ứ
		unicode_str = unicode_str.replace("\u01AF\u0300", "\u1EEA")    ;// Ừ
		unicode_str = unicode_str.replace("\u01AF\u0323", "\u1EF0")    ;// Ự
		unicode_str = unicode_str.replace("\u01AF\u0303", "\u1EEE")    ;// Ữ
		unicode_str = unicode_str.replace("\u0049\u0309", "\u1EC8")    ;// Ỉ
		unicode_str = unicode_str.replace("\u0049\u0301", "\u00CD")    ;// Í
		unicode_str = unicode_str.replace("\u0049\u0300", "\u00CC")    ;// Ì
		unicode_str = unicode_str.replace("\u0049\u0323", "\u1ECA")    ;// Ị
		unicode_str = unicode_str.replace("\u0049\u0303", "\u0128")    ;// Ĩ
		unicode_str = unicode_str.replace("\u004F\u0309", "\u1ECE")    ;// Ỏ
		unicode_str = unicode_str.replace("\u004F\u0301", "\u00D3")    ;// Ó
		unicode_str = unicode_str.replace("\u004F\u0300", "\u00D2")    ;// Ò
		unicode_str = unicode_str.replace("\u004F\u0323", "\u1ECC")    ;// Ọ
		unicode_str = unicode_str.replace("\u004F\u0303", "\u00D5")    ;// Õ
		unicode_str = unicode_str.replace("\u01A0\u0309", "\u1EDE")    ;// Ở
		unicode_str = unicode_str.replace("\u01A0\u0301", "\u1EDA")    ;// Ớ
		unicode_str = unicode_str.replace("\u01A0\u0300", "\u1EDC")    ;// Ờ
		unicode_str = unicode_str.replace("\u01A0\u0323", "\u1EE2")    ;// Ợ
		unicode_str = unicode_str.replace("\u01A0\u0303", "\u1EE0")    ;// Ỡ
		unicode_str = unicode_str.replace("\u00D4\u0309", "\u1ED4")    ;// Ổ
		unicode_str = unicode_str.replace("\u00D4\u0301", "\u1ED0")    ;// Ố
		unicode_str = unicode_str.replace("\u00D4\u0300", "\u1ED2")    ;// Ồ
		unicode_str = unicode_str.replace("\u00D4\u0323", "\u1ED8")    ;// Ộ
		unicode_str = unicode_str.replace("\u00D4\u0303", "\u1ED6")    ;// Ỗ
		unicode_str = unicode_str.replace("\u0041\u0309", "\u1EA2")    ;// Ả
		unicode_str = unicode_str.replace("\u0041\u0301", "\u00C1")    ;// Á
		unicode_str = unicode_str.replace("\u0041\u0300", "\u00C0")    ;// À
		unicode_str = unicode_str.replace("\u0041\u0323", "\u1EA0")    ;// Ạ
		unicode_str = unicode_str.replace("\u0041\u0303", "\u00C3")    ;// Ã
		unicode_str = unicode_str.replace("\u0102\u0309", "\u1EB2")    ;// Ẳ
		unicode_str = unicode_str.replace("\u0102\u0301", "\u1EAE")    ;// Ắ
		unicode_str = unicode_str.replace("\u0102\u0300", "\u1EB0")    ;// Ằ
		unicode_str = unicode_str.replace("\u0102\u0323", "\u1EB6")    ;// Ặ
		unicode_str = unicode_str.replace("\u0102\u0303", "\u1EB4")    ;// Ẵ
		unicode_str = unicode_str.replace("\u00C2\u0309", "\u1EA8")    ;// Ẩ
		unicode_str = unicode_str.replace("\u00C2\u0301", "\u1EA4")    ;// Ấ
		unicode_str = unicode_str.replace("\u00C2\u0300", "\u1EA6")    ;// Ầ
		unicode_str = unicode_str.replace("\u00C2\u0323", "\u1EAC")    ;// Ậ
		unicode_str = unicode_str.replace("\u00C2\u0303", "\u1EAA")    ;// Ẫ
		return unicode_str;
	}
}
