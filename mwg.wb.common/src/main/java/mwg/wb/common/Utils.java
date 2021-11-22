package mwg.wb.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {

	/** Date helper **/
	// 2019-8-22.8.48. 25. 0
	public static Date StringToDate(String strdate) throws ParseException {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(strdate);
	}

	public static Date GetDefaultDate() {
		Date date = new Date();
		date.setTime(1);
		return date;
	}

	public static String GetNumberStringDouble(double vao) {
		return GetNumberString(String.valueOf(vao));
	}

	public static String GetNumberString(String vao) {

		return vao.replace(".0", "");
	}

	public static Date GetCurrentDate() {
		long millis = System.currentTimeMillis();
		return new java.util.Date(millis);
	}

	public static Date AddHour(Date from, int hour) {
		Calendar c = Calendar.getInstance();
		c.setTime(from);
		c.add(Calendar.HOUR, hour);
		return c.getTime();
	}

	public static Date AddMinute(Date from, int minute) {
		Calendar c = Calendar.getInstance();
		c.setTime(from);
		c.add(Calendar.MINUTE, minute);
		return c.getTime();
	}

	public static Date AddSecond(Date from, int second) {
		Calendar c = Calendar.getInstance();
		c.setTime(from);
		c.add(Calendar.SECOND, second);
		return c.getTime();
	}

	public static Date AddDay(Date from, int day) {
		Calendar c = Calendar.getInstance();
		c.setTime(from);
		c.add(Calendar.DATE, day);
		return c.getTime();
	}

	public static Date AddMonth(Date from, int month) {
		Calendar c = Calendar.getInstance();
		c.setTime(from);
		c.add(Calendar.MONTH, month);
		return c.getTime();
	}

	public static Date AddYear(Date from, int year) {
		Calendar c = Calendar.getInstance();
		c.setTime(from);
		c.add(Calendar.YEAR, year);
		return c.getTime();
	}
	/* end Date helper */

	public static int GetQueueNum(long ProductID) {
		return (int) ProductID % 3;
	}

	public static int GetQueueNum(long Hash, long id) {
		if (Hash > 0) {
			return (int) Hash % 3;
		} else {
			return (int) id % 3;
		}
	}

	public static int GetQueueNum5(long hash) {

		return (int) (hash % 5);
	}

	public static int GetQueueNum10(long hash) {

		return (int) (hash % 10);
	}

	public static int GetQueueNum30(long hash) {

		return (int) (hash % 30);
	}

	public static long toLong(String str) {
		try {
			return Long.parseLong(str.replace(".0", ""));
		} catch (Exception e) {
			// TODO: handle exception
		}
		return 0;
	}

	public static Double toDouble(String str) {
		try {
			return Double.parseDouble(str);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return 0.0;
	}

	public static Float toFloat(String str) {
		try {
			return Float.parseFloat(str);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return 0.0f;
	}

	public static int toInt(String str) {
		try {
			return Integer.parseInt(str.replace(".0", ""));
		} catch (Exception e) {
			// TODO: handle exception
		}
		return 0;
	}

	public static int toInt(double d) {
		try {
			return (int) d;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return 0;
	}

	public static String toString(int intValue) {
		try {
			return Integer.toString(intValue);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "";
	}

	public static String toString(Object objectValue) {
		try {
			return String.valueOf(objectValue);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "";
	}

	public static String getRandomElement(String[] list) {
		Random rand = new Random();
		return list[rand.nextInt(list.length)];
	}

	public static boolean StringIsEmpty(String vao) {
		if (vao == null || vao.isBlank() || vao.isEmpty() || vao.trim().length() <= 0) {
			return true;
		}
		return false;
	}

	public static String MD5(String data) throws NoSuchAlgorithmException {
		if (StringIsEmpty(data))
			return "";
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(data.getBytes());
		byte[] digest = md.digest();
		return DatatypeConverter.printHexBinary(digest).toUpperCase();

	}

	public static int GetMessageSaveLog(String note) {
		if (Utils.StringIsEmpty(note))
			return MessageLogType.NONE;
		String strNOTE = note + "";
		if (strNOTE.contains("DIDX_TOP") && strNOTE.contains("DIDX_LOG")) {
			return MessageLogType.DIDX_TOP_LOG;
		}
		if (strNOTE.contains("DIDX_TOP")) {
			return MessageLogType.DIDX_TOP;
		}
		if (strNOTE.contains("DIDX_TOP_LOG")) {
			return MessageLogType.DIDX_TOP_LOG;
		}
		if (strNOTE.contains("DIDX_LOG")) {
			return MessageLogType.DIDX_LOG;
		}
		if (strNOTE.contains("PUSHALL")) {
			return MessageLogType.PUSHALL;
		}
		return MessageLogType.NONE;
	}

	public static boolean IsMessageSaveLog(String note) {
		if (Utils.StringIsEmpty(note))
			return false;
		String strNOTE = note + "";// (DIDX_LOG DIDX_TOP|227628)
		if (strNOTE.contains("DIDX_TOP_LOG") || strNOTE.contains("DIDX_LOG")) {

			return true;
		}
		return false;
	}

	public static String getCurrentDir() {

		// co1 / o cuoi
//		String path = FileHelper
//				.GetDirectoryName(Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath());
//		String decodedPath = "";
//		try {
//			decodedPath = URLDecoder.decode(path, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		 
//		return decodedPath;

		File directory = new File("/");
		try {
			String current = new java.io.File(".").getCanonicalPath();
			return current + "/";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "/";
	}

	public static int GetRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	public static void WriteAllText(String path, String content) throws IOException {
		Logs.WriteLine("WriteAllText " + content);
		File file = new File(path);
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content);
		bw.close(); // Be sure to close BufferedWriter
		fw.close();

	}

	public static boolean FileExists(String path) {

		File tempFile = new File(path);
		return tempFile.exists();

	}

	public static String ReadAllText(String path) throws IOException {

		String content = "";

		try {
			content = new String(Files.readAllBytes(Paths.get(path)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;

	}

	public static final long YEAR = 365 * 24 * 3600 * 1000;

	public static String FormatDateForGraph(Date dateTime) {
		if (dateTime == null) {
			dateTime = new Date(GetCurrentDate().getTime() - 100 * YEAR);
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(dateTime);
	}

	public static String CheckSQl(String value) {
		// return value.replaceAll("\\", "\\\\").replaceAll("\r",
		// "\\r").replaceAll("\n", "\\n").replaceAll("'", "\\'");
		value = StringUtils.replace(value, "\\", "\\\\");
		value = StringUtils.replace(value, "\r", "\\r");
		value = StringUtils.replace(value, "\n", "\\n");
		value = StringUtils.replace(value, "'", "\\'");
		return value;
	}

	public static String stackTraceToString(Throwable e) {
		return e.getMessage() + "\n"
				+ Stream.of(e.getStackTrace()).map(x -> x.toString()).collect(Collectors.joining("\n"));
	}

	public static long ToLong(String obg) {
		try {
			return (new Double(obg)).longValue();
		} catch (Exception e) {
			return 0;
		}

	}

	public static String ClobToString(Clob clob) {
		if (clob == null)
			return "";
		StringBuilder sb = new StringBuilder();
		try {
			Reader reader = clob.getCharacterStream();
			BufferedReader br = new BufferedReader(reader);

			String line;
			while (null != (line = br.readLine())) {
				sb.append(line);
			}
			br.close();
		} catch (SQLException e) {
			Logs.WriteLine(e.toString());
		} catch (IOException e) {
			Logs.WriteLine(e.toString());
		}

		return sb.toString();
	}

	public static String BlobToString(Blob blob) throws SQLException {
		if (blob == null)
			return "";
		return new String(blob.getBytes(1l, (int) blob.length()));

	}

	public static void Sleep(long num) {
		try {
			Thread.sleep(num);
		} catch (InterruptedException e1) {

		}

	}

	public static String NClobToString(NClob nclob) {
		if (nclob == null)
			return "";
		StringBuilder sb = new StringBuilder();
		try {
			Reader reader = nclob.getCharacterStream();
			BufferedReader br = new BufferedReader(reader);

			String line;
			while (null != (line = br.readLine())) {
				sb.append(line);
			}
			br.close();
		} catch (SQLException e) {
			Logs.WriteLine(e.toString());
		} catch (IOException e) {
			Logs.WriteLine(e.toString());
		}

		return sb.toString();
	}

	public static int getDayOfMonth(Date aDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(aDate);
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	private static ObjectMapper jsonMapper = null;

	public static ObjectMapper getJsonMapper() {
		if (jsonMapper == null)
			jsonMapper = new ObjectMapper().setDateFormat(new SimpleDateFormat(GConfig.DateFormatString))
					.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return jsonMapper;
	}

	public static void BuildSql(boolean isLog, String note, String VertexClasses, String recordName, String recordValue,
			Object obj, String exclude, RefSql ref) throws NoSuchAlgorithmException {
		Map<String, Object> params = new HashMap<String, Object>();
		Field[] fields = obj.getClass().getDeclaredFields();
//String hash="";
		ref.lTble = VertexClasses;
		ref.lId = recordValue;
		String sql = "update " + VertexClasses + " SET ";
		for (final Field _item : fields) {
			String cl = _item.getName().toLowerCase();
			if (exclude.contains("," + cl + ","))
				continue;
			sql = sql + "`" + cl + "`=:" + cl + ",";

		}
		sql = StringUtils.strip(sql, ",");
		// hash=sql;
		for (final Field _item : fields) {
			Object cc = null;
			try {
				cc = _item.get(obj);
			} catch (IllegalArgumentException e) {

			} catch (IllegalAccessException e) {

			}
			String o = _item.getName().toLowerCase();
			if (exclude.contains("," + o + ","))
				continue;
			if (cc != null) {
				String value = String.valueOf(cc);
				// hash=hash+"_"+value+"_"+String.valueOf(value);
				// Logs.WriteLine(o+"="+value);
				Type f = _item.getType();
				if (f == Date.class) {
					params.put(o, Utils.FormatDateForGraph((Date) cc));
				} else if (f == boolean.class) {

					params.put(o, Boolean.valueOf(value) == true ? 1 : 0);

				} else if (f == byte.class) {
					params.put(o, Byte.valueOf(value));
				} else if (f == char.class) {
					params.put(o, value.charAt(0));
				} else if (f == short.class) {
					params.put(o, Short.valueOf(value));
				} else if (f == int.class) {
					params.put(o, Integer.valueOf(value));
				} else if (f == long.class) {
					params.put(o, Long.valueOf(value));
				} else if (f == float.class) {
					params.put(o, Float.valueOf(value));
				} else if (f == double.class) {
					params.put(o, Double.valueOf(value));

				} else if (f == String.class) {
					params.put(o, String.valueOf(value));
				} else {
					params.put(o, String.valueOf(value));

				}

			} else {

				params.put(o, null);
			}

		}
		sql = sql + " Upsert where " + recordName + "='" + recordValue + "'";
		ref.Sql = sql;
		ref.params = params;
		// ref.Hash=Utils.MD5(hash);
	}

	public static void BuildSql(boolean isLog, String note, String VertexClasses, String recordName, String recordValue,
			Object obj, RefSql ref) {
		ref.lTble = VertexClasses;
		ref.lId = recordValue;
		// Field[] fields = obj.getClass().getDeclaredFields();
		Field[] fields = obj.getClass().getFields();
		Map<String, Object> params = new HashMap<String, Object>();
		String sql = "update " + VertexClasses + " SET ";
		for (final Field _item : fields) {
			String cl = _item.getName().toLowerCase();

			sql = sql + "`" + cl + "`=:" + cl + ",";

		}
		sql = StringUtils.strip(sql, ",");

		for (final Field _item : fields) {
			Object cc = null;
			try {
				cc = _item.get(obj);
			} catch (IllegalArgumentException e) {

			} catch (IllegalAccessException e) {

			}
			String o = _item.getName().toLowerCase();

			if (cc != null) {
				String value = String.valueOf(cc);
				// Logs.WriteLine(o+"="+value);
				Type f = _item.getType();
				if (f == Date.class) {
					params.put(o, Utils.FormatDateForGraph((Date) cc));
				} else if (f == boolean.class) {

					params.put(o, Boolean.valueOf(value) == true ? 1 : 0);

				} else if (f == byte.class) {
					params.put(o, Byte.valueOf(value));
				} else if (f == char.class) {
					params.put(o, value.charAt(0));
				} else if (f == short.class) {
					params.put(o, Short.valueOf(value));
				} else if (f == int.class) {
					params.put(o, Integer.valueOf(value));
				} else if (f == long.class) {
					params.put(o, Long.valueOf(value));
				} else if (f == float.class) {
					params.put(o, Float.valueOf(value));
				} else if (f == double.class) {
					params.put(o, Double.valueOf(value));

				} else if (f == String.class) {
					params.put(o, String.valueOf(value));
				} else {
					params.put(o, String.valueOf(value));

				}

			} else {

				params.put(o, null);
			}

		}
		sql = sql + " Upsert where " + recordName + "='" + recordValue + "'";
		ref.Sql = sql;
		ref.params = params;

	}

	public static <T> T[] pushArray(T[] arr, T item) {
		T[] tmp = Arrays.copyOf(arr, arr.length + 1);
		tmp[tmp.length - 1] = item;
		return tmp;
	}

	public static <T> T[] pushArray(T[] arr, T[] item) {
		for (int i = 0; i < item.length; i++) {
			arr = pushArray(arr, item[i]);
		}
		return arr;
	}

	public static <T> T[] popArray(T[] arr) {
		T[] tmp = Arrays.copyOf(arr, arr.length - 1);
		return tmp;
	}
	public static int[] removeTheElementArray(int[] arr, int index)
	{
		if (arr == null
				|| index < 0
				|| index >= arr.length) {

			return arr;
		}
		int[] anotherArray = new int[arr.length - 1];
		for (int i = 0, k = 0; i < arr.length; i++) {
			if (i == index) {
				continue;
			}
			anotherArray[k++] = arr[i];
		}

		return anotherArray;
	}



	public static <T> T concatenate(T a, T b) {
		if (!a.getClass().isArray() || !b.getClass().isArray()) {
			throw new IllegalArgumentException();
		}

		Class<?> resCompType;
		Class<?> aCompType = a.getClass().getComponentType();
		Class<?> bCompType = b.getClass().getComponentType();

		if (aCompType.isAssignableFrom(bCompType)) {
			resCompType = aCompType;
		} else if (bCompType.isAssignableFrom(aCompType)) {
			resCompType = bCompType;
		} else {
			throw new IllegalArgumentException();
		}

		int aLen = Array.getLength(a);
		int bLen = Array.getLength(b);

		@SuppressWarnings("unchecked")
		T result = (T) Array.newInstance(resCompType, aLen + bLen);
		System.arraycopy(a, 0, result, 0, aLen);
		System.arraycopy(b, 0, result, aLen, bLen);

		return result;
	}

	public static boolean isNumeInt(String strNum) {
		if (strNum == null || StringIsEmpty(strNum)) {
			return false;
		}
		try {
			var d = Integer.parseInt(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static boolean isProductInvalid(int productID) {
		if (productID >= 999999)
			return false;
		if (productID < 1000)
			return false;
		var blackList = new String[] { "9999", "1111", "999", "99" };
		if (Arrays.asList(blackList).contains(productID + ""))
			return false;
		return true;
	}

	public static boolean isProductInvalid(String strProductID) {
		if (!isNumeInt(strProductID))
			return false;
		var productID = Integer.parseInt(strProductID);
		return isProductInvalid(productID);
	}

	public static Date addHoursToJavaUtilDate(Date date, int hours) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.HOUR_OF_DAY, hours);
		return calendar.getTime();
	}
	public static  boolean isNullOrEmptyMap(Map< ? , ?> map){
		return  (map==null || map.isEmpty());
	}

}
