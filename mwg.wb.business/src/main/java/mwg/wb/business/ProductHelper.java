package mwg.wb.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import mwg.wb.business.SpecialsaleProgram.Pm_ProductBO;
import mwg.wb.business.SpecialsaleProgram.PriceParameterHelper;
import mwg.wb.business.SpecialsaleProgram.SpecialsaleProgramHelper;
import mwg.wb.business.helper.APIPriceHelper;
import mwg.wb.business.helper.BHXProductHelper;
import mwg.wb.business.helper.BHXStoreHelper;
import mwg.wb.business.helper.BhxPriceHelper;
import mwg.wb.business.helper.BigPhoneProductHelper;
import mwg.wb.business.helper.DMXProductHelper;
import mwg.wb.business.helper.DTSRProductHelper;
import mwg.wb.business.helper.ISiteProductHelper;
import mwg.wb.business.helper.TGDDProductHelper;
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.elasticsearch.dataquery.*;
import mwg.wb.client.elasticsearch.dataquery.HomePageQuery.SearchType;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.redis.RedisCluster;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.client.service.CodeTimers;
import mwg.wb.client.service.CrmServiceHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.SSObject;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.api.ProductBOApi;
import mwg.wb.model.api.ProductErpPriceBOApi;
import mwg.wb.model.api.ProductGalleryBOApi;
import mwg.wb.model.campagain.PreOrderProgramBO;
import mwg.wb.model.commonpackage.LabelCampaignBO;
import mwg.wb.model.commonpackage.LabelCampaignSO;
import mwg.wb.model.commonpackage.SuggestSearchSO;
import mwg.wb.model.general.DistrictBO;
import mwg.wb.model.general.ProvinceBO;
import mwg.wb.model.general.StockDistrictBO;
import mwg.wb.model.general.StockProvinceBO;
import mwg.wb.model.html.InfoBO;
import mwg.wb.model.installment.CompanyInstallmentBO;
import mwg.wb.model.installment.InstallmentBO;
import mwg.wb.model.installment.InstallmentException;
import mwg.wb.model.installment.PaymentTypeBO;
import mwg.wb.model.other.BooleanWrapper;
import mwg.wb.model.other.ORIntArrWrapper;
import mwg.wb.model.other.ORIntWrapper;
import mwg.wb.model.other.StringWrapper;
import mwg.wb.model.pm.StockStore;
import mwg.wb.model.pm.StoreBO;
import mwg.wb.model.pricestrings.PriceStringBO;
import mwg.wb.model.products.*;
import mwg.wb.model.promotion.BHXPromotionType;
import mwg.wb.model.promotion.CMSPromotion;
import mwg.wb.model.promotion.Promotion;
import mwg.wb.model.promotion.PromotionBHX;
import mwg.wb.model.promotion.PromotionBanKemBO;
import mwg.wb.model.promotion.PromotionProductBanKemBO;
import mwg.wb.model.promotion.PromotionString;
import mwg.wb.model.promotion.PromotionSubBrandBO;
import mwg.wb.model.promotion.ShockPriceBO;
import mwg.wb.model.promotion.ShockPriceDiscountBO;
import mwg.wb.model.prop.PropValueID;
import mwg.wb.model.search.GallerySO;
import mwg.wb.model.search.ProductPriceSO;
import mwg.wb.model.search.ProductSO;
import mwg.wb.model.searchresult.FaceCategorySR;
import mwg.wb.model.searchresult.FaceManuSR;
import mwg.wb.model.searchresult.GallerySR;
import mwg.wb.model.searchresult.ProductBOSR;
import mwg.wb.model.searchresult.ProductSOSR;
import mwg.wb.model.seo.ProductUrl;
import mwg.wb.model.seo.ProductUrlUpsert;
import mwg.wb.model.system.CachedDetails;
import mwg.wb.model.system.DeliveryTime;
import mwg.wb.model.system.IntStringKVObject;
import mwg.wb.model.system.KeyWordBO;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.Min;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder.ScriptSortType;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.scriptFunction;
import static org.elasticsearch.search.aggregations.AggregationBuilders.*;
import static org.elasticsearch.search.sort.SortBuilders.scriptSort;

public class ProductHelper {
	// ORTumLum database2;
	// OrientDBClient2 database;
	public String CurrentIndexDB = "", GalleryIndex = "", PromotionIndex = "";
//	static final String CurrentTypeDB = "product";

	public static final List<Integer> AccessoryCategory = Arrays.asList(60, 57, 55, 58, 54, 1662, 1363, 1823, 56, 75,
			86, 382, 2429, 1882, 3885, 6858, 85, 2162, 4547, 7924);
	public static final List<Integer> teleCategory = List.of(42, 44, 522, 7077, 7264, 7978);
	public static final List<Integer> itCategory = List.of(5693, 5697, 5698, 1262);
	public static String[] MainProductCategory = { "42", "44", "522", "1882", "5698", "7077", "7264", "5693", "7678" };
	public static int[] AllCategory = { 60, 57, 55, 58, 54, 1662, 1363, 1823, 56, 75, 86, 382, 2429, 1882, 3885, 6858,
			85, 2162, 4547, 5697, 4727, 4728, 42, 44, 522, 1882, 5698, 7077, 7264, 5693, 7678, 1902, 7921, 7922, 7923,
			6859, 6862, 7924, 7925, 6863, 7978, 1262 };
	private List<Integer> bigCE = Arrays.asList(1943, 1944, 2002, 5475, 2202);
	public static int[] MainProductCategoryForNewPage = { 42, 44, 522 };
	private static List<Integer> dmxCateIDList = Arrays.asList(1942, 1943, 2002, 1944, 462, 1962, 2162, 2022);
	public static int[] SellingProductStatusID = { 2, 3, 4, 6, 8, 9, 11, 99, 98 };

	protected RestHighLevelClient clientIndex = null;
	private ElasticClient elasticClient = null;// ElasticClient.getInstance().getClient();
	public ObjectMapper mapper = null, esmapper = null;
	ORThreadLocal factoryRead = null;

	ClientConfig config = null;
	ISiteProductHelper bigphone, dtsr, tgdd, bhx, dmx;
	PriceHelper priceHelper;
	String function_GetProductByListID = "product_GetByListIDCodeSimpleWithPriceDefV2";
	public static final Map<Integer, List<Integer>> advantageMap = new HashMap<>();
	private Map<String, CachedDetails> cachedDetails;
	BHXStoreHelper bhxStoreHelper = null;
	CrmServiceHelper crmHelper = null;
	RedisCluster redisCluster = null;
	// private BhxServiceHelper bhxServiceHelper = null;
	Logs logs = null;
	public CategoryHelper categoryHelper = null;

	public List<Integer> getAccessoryCategory() {
		return AccessoryCategory;
	}

	public boolean IsWorker = false;
	public int KEY_CACHED_TIME = 3;
	public ProductHelper(ORThreadLocal afactoryRead, ClientConfig aconfig) {
		IsWorker = afactoryRead.IsWorker;
		CurrentIndexDB = aconfig.ELASTICSEARCH_PRODUCT_INDEX;
		GalleryIndex = aconfig.ELASTICSEARCH_PRODUCT_GALLERY_INDEX;
		PromotionIndex = aconfig.ELASTICSEARCH_PROMOTION_INDEX;
		config = aconfig;
		factoryRead = afactoryRead;
		logs = new Logs(aconfig.DATACENTER, afactoryRead.IsWorker);
		mapper = DidxHelper.generateJsonMapper(GConfig.DateFormatString);
		esmapper = DidxHelper.generateJsonMapper(GConfig.DateFormatStringNews);

//		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
//		mapper.setDateFormat(df);
//		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
//		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
			//elasticClient =  new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST); 
	elasticClient = ElasticClient.getInstance(config.SERVER_ELASTICSEARCH_READ_HOST);
		
		
		clientIndex = elasticClient.getClient();
		advantageMap.put(42, Arrays.asList(79, 84, 27));
		advantageMap.put(44, Arrays.asList(92, 93, 146, 228));
		advantageMap.put(522, Arrays.asList(2440, 2445, 7839));
		cachedDetails = new HashMap<>();

		bigphone = new BigPhoneProductHelper(config, clientIndex, mapper, this);
		dtsr = new DTSRProductHelper(config, clientIndex, mapper, this);
		tgdd = new TGDDProductHelper(config, clientIndex, mapper, this);
		bhx = new BHXProductHelper(config, clientIndex, mapper, this);
		dmx = new DMXProductHelper(config, clientIndex, mapper, this);
		priceHelper = new PriceHelper(factoryRead, config);
		bhxStoreHelper = new BHXStoreHelper(esmapper, config);
		crmHelper = new CrmServiceHelper(config);
		categoryHelper = new CategoryHelper(factoryRead, config);
		try {
			// redisCluster = new RedisCluster(config);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// bhxServiceHelper = new BhxServiceHelper(); //objectTransfer.bhxServiceHelper;
	}

	// public void Close() {
//		try {
//			if (database.isOpen())
//				database.Close();
//		} catch (Exception e) {
//			Logs.WriteLine(e);
//		}
//
//	}
	public static Map<String, Long> g_listProductID = new ConcurrentHashMap<String, Long>();
	public static Map<String, String> g_listCacheRID = new HashMap<String, String>();
	public static Map<String, String> g__listDefaultcode = new HashMap<String, String>();
	public static Map<Long, String> g_ProductIDProductCode = new HashMap<Long, String>();
	public static Map<Long, Integer> g_CateIDProductID = new HashMap<Long, Integer>();
	public static Map<String, ProductBO> g_listProduct = new ConcurrentHashMap<String, ProductBO>();
	public static Map<String, ProductBO[]> g_listProducts = new HashMap<String, ProductBO[]>();
	public static Map<String, ProductBO> g_listProductOLdModel = new ConcurrentHashMap<String, ProductBO>();
	public static Map<Long, Boolean> g_listDBProductID = new ConcurrentHashMap<Long, Boolean>();
	public static Map<String, ProductBO> g_listProductDMX = new ConcurrentHashMap<String, ProductBO>();
	public static long DateExpireCache_GetProductIDByProductCodeFromCache = System.currentTimeMillis() + 10 * 60 * 1000; // 10
	// phut
	public static Map<String, MainSubGroupBO> g_listMainSubGroup = new ConcurrentHashMap<String, MainSubGroupBO>();

	public boolean CheckProductExistFromCache(long productid) {
//		if (GConfig.ProductTaoLao.containsKey(productid))
//			return false;

		if (DateExpireCache_GetProductIDByProductCodeFromCache < System.currentTimeMillis()) {
			g_listDBProductID.clear();
			DateExpireCache_GetProductIDByProductCodeFromCache = System.currentTimeMillis() + 10 * 60 * 1000;
		}
		if (g_listDBProductID.containsKey(productid)) {
			return g_listDBProductID.get(productid);

		}
		var rs = CheckProductID(productid);
		g_listDBProductID.put(productid, rs);
		return rs;
	}

	public boolean CheckProductID(long productid) {

		return factoryRead.GetPoductid(productid);

	}
//	public boolean CheckProductID(long productid) {
//
//		OResultSet ls = null;
//		try {
//			ls = factoryRead.Query(
//					"select productid  from  product where productid=" + productid + " and  isdeleted=0   limit 1");
//			if (ls != null) {
//				while (ls.hasNext()) {
//
//					var f = ls.next();
//					if (f != null) {
//						return true;
//					}
//
//				}
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//			Logs.LogException(e);
//			throw e;
//			// TODO: handle exception
//		} finally {
//			CloseOResultSet(ls);
//		}
//		Logs.LogFactoryInfo(String.valueOf(productid), "producttaolao");
//
//		return false;
//	}

	public Map<String, SSObject> GetResultMap(String sql, Map<String, SSObject> cotlist, String gEdgePropToCheck)
			throws Throwable {
		Map<String, SSObject> params = new HashMap<String, SSObject>();
		OResultSet ls = null;
		try {
			String pattern = "yyyy-MM-dd MM:mm:ss";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

			ls = factoryRead.Query(sql);
			while (ls.hasNext()) {
				var f = ls.next();
				if (f != null) {
					if (!Utils.StringIsEmpty(gEdgePropToCheck)) {
						if (f.getProperty("proptocheck") != null) {
							String productid = f.getProperty("proptocheck").toString().replace(".0", "");
							SSObject data = new SSObject();
							data.cot = "proptocheck";
							data.Value = productid;
							params.put("proptocheck", data);
						}
					}
					for (String cl : cotlist.keySet()) {
						SSObject data = new SSObject();
						var cc = cotlist.get(cl);

						data.cot = cl;
						if (cc != null && f.getProperty(cl) != null) {
							switch (cc.sqlType) {
							case Types.BIGINT:
							case Types.INTEGER:
							case Types.TINYINT:
							case Types.SMALLINT:

								data.Value = f.getProperty(cl).toString();
								params.put(cl, data);

								break;
							case Types.DATE:

								SimpleDateFormat parserSDF = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy",
										Locale.ENGLISH);
								Date date = parserSDF.parse(f.getProperty(cl).toString());

								// Date d=DateFormat.getInstance()..parse( f.getProperty(cl).toString()) ;
								String dates = simpleDateFormat.format(date);
								data.Value = dates;
								params.put(cl, data);

								break;
							case Types.TIMESTAMP:
//							 if(f.getProperty(cl)!=null) {
//							data.Value=  f.getProperty(cl).toString() ;
//							params.put(cl, data);
//							 }

								SimpleDateFormat parserSDF1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy",
										Locale.ENGLISH);
								Date date1 = parserSDF1.parse(f.getProperty(cl).toString());

								// Date d=DateFormat.getInstance()..parse( f.getProperty(cl).toString()) ;
								String dates1 = simpleDateFormat.format(date1);
								data.Value = dates1;
								params.put(cl, data);

								break;
							case Types.DOUBLE:

								data.Value = f.getProperty(cl).toString().replace(".0", "");
								params.put(cl, data);

								break;
							case Types.FLOAT:
								// params.put(cl, reader.getFloat(i));

								data.Value = f.getProperty(cl).toString().replace(".0", "");
								params.put(cl, data);

								break;
							case Types.NVARCHAR:

								data.Value = f.getProperty(cl).toString();
								params.put(cl, data);

								break;
							case Types.VARCHAR:

								data.Value = f.getProperty(cl).toString();
								params.put(cl, data);

								break;
							case Types.BLOB:

								data.Value = f.getProperty(cl).toString();
								params.put(cl, data);

								break;
							case Types.CLOB:

								data.Value = f.getProperty(cl).toString();
								params.put(cl, data);

								break;
							case Types.NCLOB:
								data.Value = f.getProperty(cl).toString();
								params.put(cl, data);

								break;
							default:
								data.Value = f.getProperty(cl).toString();
								params.put(cl, data);

								break;
							}
						} else {
							params.put(cl, data);
						}

					}

				}

			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			throw e;

		} finally {
			CloseOResultSet(ls);
		}

		return params;
	}

	public String GetRidFromCache(String table, String keyname, String keyvalue) {
		String key = table + "_" + keyvalue;
		if (g_listCacheRID.containsKey(key)) {
			return g_listCacheRID.get(key);

		}
		String rs = "";

		rs = factoryRead.GetRid(table, keyname, keyvalue);
		if (Utils.StringIsEmpty(rs))
			return "";

		g_listCacheRID.put(key, rs);

		return rs;

	}

	public String GetRidFromCacheWait(String table, String keyname, String keyvalue) {
		String key = table + "_" + keyvalue;
		if (g_listCacheRID.containsKey(key)) {
			return g_listCacheRID.get(key);

		}

		String rs = "";
		if (!Utils.StringIsEmpty(table) && !Utils.StringIsEmpty(keyname) && !Utils.StringIsEmpty(keyvalue)
				&& !table.equalsIgnoreCase("null") && !keyname.equalsIgnoreCase("null")
				&& !keyvalue.equalsIgnoreCase("null")) {
			for (int i = 0; i < 5; i++) {
				rs = factoryRead.GetRid(table, keyname, keyvalue);
				if (!Utils.StringIsEmpty(rs)) {
					break;
				} else {
					Utils.Sleep(500);
				}

			}
			if (Utils.StringIsEmpty(rs))
				return "";

			g_listCacheRID.put(key, rs);
		}
		return rs;

	}

	public String getDefaultCodeFromCache(long productID, int siteID, int provinceID) {
		String key = productID + "_" + siteID + "_" + provinceID;
		String code = g__listDefaultcode.get(key);
		if (code == null) {
			code = getDefaultCode(productID, siteID, provinceID);
			if (code != null && !code.isEmpty())
				g__listDefaultcode.put(key, code);
		}
		return code;
	}

	public ISiteProductHelper getHelperBySite(int siteID) {
		switch (siteID) {
		case 12:
			return dtsr;
		case 6:
			return bigphone;
		case 1:
			return tgdd;
		case 11:
			return bhx;
		case 2:
			return dmx;
		default:
			return tgdd;
		}
	}

	public boolean checkEdgeExist(String Edge, String rFrom, String rTO) {
		OResultSet ls = null;
		String sql = "select  from  index:" + Edge + ".in_out where key=[" + rTO + "," + rFrom + "]";
		try {

			ls = factoryRead.Query(sql);
			if (ls == null)
				return false;
			while (ls.hasNext()) {
				// String productlangRID = ls.next().getProperty("productidref").toString();
				return true;
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			throw e;
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}
		return false;
	}

	public List<String> GetManuListOfNode(String productid) {
		OResultSet ls = null;
		List<String> list = new ArrayList<String>();
		try {
			ls = factoryRead.Query(
					"select set(out('e_product_manu').manufacturerid) as manufacturerid from product where productid="
							+ productid);

			while (ls.hasNext()) {
				// [9126]
				var f = ls.next();
				if (f != null && f.getProperty("manufacturerid") != null) {
					list = List.of(f.getProperty("manufacturerid").toString().replace(".0", "").replace("[", "")
							.replace("]", "").split("\\,"));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			throw e;
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}
		return list;
	}

	public List<String> GetStoreListOfNode(String recordid) {
		OResultSet ls = null;
		List<String> list = new ArrayList<String>();
		try {
			ls = factoryRead
					.Query("select set(out('e_stock_store').storeid) as storeid from pm_currentinstock where recordid="
							+ recordid);

			while (ls.hasNext()) {
				// [9126]
				var f = ls.next();
				if (f != null && f.getProperty("storeid") != null) {
					list = List.of(f.getProperty("storeid").toString().replace(".0", "").replace("[", "")
							.replace("]", "").split("\\,"));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			throw e;
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}
		return list;
	}

	public List<String> GetPriceListOfNode(String recordid) {

		OResultSet ls = null;
		List<String> list = new ArrayList<String>();
		try {
			ls = factoryRead.Query(
					"select set(out('e_code_price').productcode) as productcode from pm_product where productid='"
							+ recordid + "'");

			while (ls.hasNext()) {
				// [9126]
				var f = ls.next();
				if (f != null && f.getProperty("productcode") != null) {
					list = List.of(f.getProperty("productcode").toString().replace(".0", "").replace("[", "")
							.replace("]", "").split("\\,"));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			throw e;
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}
		return list;
	}
	public ProductColorBO[] GetColorByProductID(int productID, String langID) throws Throwable {
		return GetColorByProductID2(productID,langID,2,3);
	}

	public ProductColorBO[] GetColorByProductID2(int productID, String langID, int siteID,int provinceID) throws Throwable {
		// return factoryRead.QueryFunction("product_GetColorByID",
		// ProductColorBO[].class, false, productID, langID);

		var colorCode = factoryRead.QueryFunction("product_GetColorByID", ProductColorBO[].class, false, productID,
				langID);

		if (colorCode != null && colorCode.length > 0) {

			var CodeGallery = factoryRead.QueryFunction("productcode_gallery_getbyProductID",
					ProductCodeGalleryBO[].class, false, productID);
			var codeprice = factoryRead.QueryFunction("product_getPriceStrings", PriceStringBO[].class, false, productID,
					siteID,13);
			var listPrices = getPriceHelper().processQuantities(siteID, codeprice, provinceID);

			colorCode =  Arrays.stream(colorCode).map(x -> {
				var tmp = listPrices.stream()
						.filter(z -> z.ProductCode.equals(x.ProductCode)
						 && z.ProvinceId == provinceID).findFirst().orElse(null);
				if(tmp != null)
					x.WebstatusID = tmp.WebStatusId;
				return x;
			}).toArray(ProductColorBO[]::new);

			if (CodeGallery != null && CodeGallery.length > 0) {
				// hình thumb màu
				for (ProductColorBO x : colorCode) {
					var tmp = Stream.of(CodeGallery).filter(c -> c.ProductCode.equals(x.ProductCode)).findFirst()
							.orElse(null);
					if (tmp != null) {
						x.Image = tmp.Image;
						x.Bimage = tmp.Bimage;
						x.Mimage = tmp.Mimage;
						x.Simage = tmp.Simage;
						x.DisplayOrder = tmp.DisplayOrder;
					}
				}

			}
			colorCode = Arrays.stream(colorCode).sorted(Comparator.<ProductColorBO>comparingInt(x -> x.WebstatusID == 4 ? 0 : x.WebstatusID == 11 ? 1 : 2)
			.thenComparingInt(x -> x.GetImage())).toArray(ProductColorBO[]::new);

		}
		return colorCode;

	}

	public ProductErpPriceBO[] GetPriceByProductID(int productID, int siteID, String langID, int provinceID)
			throws Throwable {
		if (provinceID > 0)
			return factoryRead.QueryFunction("product_GetPriceByProductIDAndProvinceID", ProductErpPriceBO[].class,
					false, productID, siteID, provinceID, langID);
		return factoryRead.QueryFunction("product_GetPriceByProductID", ProductErpPriceBO[].class, false, productID,
				siteID, langID);
	}

	public ProductManuBO[] GetManuByCategoryID(int categoryID, int siteID, String langID) throws Throwable {

		var manu = factoryRead.QueryFunctionCached("manu_GetByCateID", ProductManuBO[].class, false, categoryID, siteID,
				langID);
		Arrays.sort(manu, Comparator.comparingDouble(ProductManuBO::getDisplayOrder));
		return manu;
	}

	public ProductManuLangBO GetMainManuByID(Double manufacturerid, int siteID, String langID) throws Throwable {

		var manu = factoryRead.QueryFunctionCached("manu_GetMainManuByID", ProductManuLangBO[].class, false,
				manufacturerid, siteID, langID);
		if (manu == null || manu.length == 0)
			return null;
		return manu[0];
	}

	public ProductErpPriceBO GetPriceByProductCode(String productCode, int siteID, int provinceID, String langID)
			throws Throwable {

		var price = factoryRead.QueryFunction("product_GetPriceByProductCode", ProductErpPriceBO[].class, false,
				productCode, siteID, provinceID, langID);

		return price == null || price.length == 0 ? null : price[0];
	}

	public List<String> GetPriceDefaultListOfNode(String ProductID) {
//	String egde = "e_product_pricedefault";
//	String cmd2 = "create edge " + egde + " from (select from  product where productid=" + ProductID
//			+ ")   to (select from  price_default   where recordid ='" + item.RecordID + "'   and in('" + egde
//			+ "')[productid = " + ProductID + "].size() = 0)";
		OResultSet ls = null;
		List<String> list = new ArrayList<String>();
		try {
			ls = factoryRead.Query(
					"select set(out('e_product_pricedefault').productid) as productid from product where productid= "
							+ ProductID + " ");

			while (ls.hasNext()) {
				// [9126]
				var f = ls.next();
				if (f != null && f.getProperty("productid") != null) {

					list = List.of(f.getProperty("productid").toString().replace(".0", "").replace("[", "")
							.replace("]", "").split("\\,"));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			;
			throw e;
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}
		return list;
	}

	public List<String> GetCateListOfNode(String productid) {
		OResultSet ls = null;
		List<String> list = new ArrayList<String>();
		try {
			ls = factoryRead.Query(
					"select set(out('e_product_category').categoryid) as categoryid from product where productid="
							+ productid);

			while (ls.hasNext()) {
				// [9126]
				var f = ls.next();
				if (f != null && f.getProperty("categoryid") != null) {
					list = List.of(f.getProperty("categoryid").toString().replace(".0", "").replace("[", "")
							.replace("]", "").split("\\,"));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			throw e;
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}
		return list;
	}

	public static long DateExpireCache = System.currentTimeMillis() + 10 * 60 * 1000; // 10 phut

	public long GetProductIDByProductCodeFromCache(String productCode) {
		long rs = 0;
		if(!Utils.StringIsEmpty(productCode)){
		if (DateExpireCache < System.currentTimeMillis()) {
			g_listProductID.clear();
			DateExpireCache = System.currentTimeMillis() + 10 * 60 * 1000;
		}
		if (g_listProductID.containsKey(productCode)) {
			return g_listProductID.get(productCode);

		}
		rs = GetProductIDByProductCode(productCode);
		if (rs == 0)
			return 0;
		if (!CheckProductExistFromCache(rs)) {
			return 0;
		} else {
			g_listProductID.put(productCode, rs);
		}
		}
		return rs;

	}

	// lấy productcode by productid
//	public List<String> GetListCodeBySiteID_BO(long ProductID, int PriceArea, int SiteID) {
//		if (GConfig.ProductTaoLao.containsKey(ProductID)) {
//			return null;
//		}
//		List<String> list = new ArrayList<String>();
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("p", ProductID);
//		params.put("m", PriceArea);
//		params.put("s", SiteID);
//		String functionname = "select product_GetListCodeBySite(:p,:m,:s) as rs";
//		var ls = factoryRead.QueryFunction(functionname, params);
//		String strJson = "";
//		if (ls != null) {
//			while (ls.hasNext()) {
//				var f = ls.next();
//				if (f != null) {
//					strJson = f.toJSON();
//					strJson = strJson.replace("{\"rs\": ", "").replace("\"productcode\":", "").replace("{", "")
//							.replace("}", "").replace("[", "").replace("]", "");
//					var hlist = new HashSet<>(Arrays.asList(strJson.split(",")));
//					list = new ArrayList<>(hlist);
//				}
//			}
//		}
//
//		return list;
//	}

	// lấy MainSubGroupBO
	public MainSubGroupBO GetMainSubGroupByProductCode(String ProductCode) {
		if (g_listMainSubGroup.containsKey(ProductCode))
			return g_listMainSubGroup.get(ProductCode);

		OResultSet ls = null;
		String sql = "";
		try {
			sql = "SELECT maingroupid,subgroupid,productidref FROM pm_product WHERE productid = '" + ProductCode
					+ "' limit 1";
			ls = factoryRead.Query(sql);
			if (ls != null) {
				while (ls.hasNext()) {
					var f = ls.next();
					if (f != null) {
						MainSubGroupBO obj = new MainSubGroupBO();
						if (f.getProperty("productidref") == null)
							return null;
						obj.maingroupid = Utils.toInt(f.getProperty("maingroupid").toString());
						obj.subgroupid = Utils.toInt(f.getProperty("subgroupid").toString());
						obj.productidref = Utils.toLong(f.getProperty("productidref").toString());
						g_listMainSubGroup.put(ProductCode, obj);
						return obj;

					}

					return null;
					// return l;// (long) Long.parseLong( productlangRID) ; //.00
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(ProductCode);
			Logs.LogException(e);
			throw e;
			// TODO: handle exception
		} finally {

			CloseOResultSet(ls);
		}
		return null;
	}

	// lấy productcode by productid
	public String GetCodeByProductID(long ProductID) {

		if (GConfig.ProductTaoLao.containsKey(ProductID)) {
			return null;
		}

		if (g_ProductIDProductCode.containsKey(ProductID))
			return g_ProductIDProductCode.get(ProductID);

		OResultSet ls = null;
		String sql = "";
		try {
			sql = "select productid from pm_product where productidref = " + ProductID + " limit 1";
			ls = factoryRead.Query(sql);
			if (ls != null) {
				while (ls.hasNext()) {

					var f = ls.next();
					if (f != null) {
						if (f.getProperty("productid") != null) {
							var result = f.getProperty("productid").toString();
							g_ProductIDProductCode.put(ProductID, result);
							return result;
						} else {
							return "";
						}
					}

					return "";
					// return l;// (long) Long.parseLong( productlangRID) ; //.00
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			throw e;
			// TODO: handle exception
		} finally {

			CloseOResultSet(ls);
		}
		return "";
	}

	public List<Integer> getAllProvinceByCountry(int countryID) throws Throwable {
		var r = factoryRead.QueryFunctionCached("get_ProvinceByCountry", ProvinceBO[].class, false, countryID);
		if (r == null || r.length == 0)
			return List.of(countryID == 0 ? 163 : 3);
		return Arrays.stream(r).map(x -> x.ProvinceID).collect(Collectors.toList());
	}

	public int GetCategoryIDByProductID(long ProductID) {

		if (g_CateIDProductID.containsKey(ProductID))
			return g_CateIDProductID.get(ProductID);

		OResultSet ls = null;
		String sql = "";
		try {
			sql = " select categoryid.asInteger() as categoryid from  product where  productid =  " + ProductID
					+ " limit 1";
			ls = factoryRead.Query(sql);
			if (ls != null) {
				while (ls.hasNext()) {

					var f = ls.next();
					if (f != null) {
						if (f.getProperty("categoryid") != null) {
							var result = Integer.valueOf(f.getProperty("categoryid").toString());
							if (result > 0) {
								g_CateIDProductID.put(ProductID, result);
							}
							return result;
						} else {
							return 0;
						}
					}

					return 0;
					// return l;// (long) Long.parseLong( productlangRID) ; //.00
				}
			}
		} catch (Throwable e) {
			// e.printStackTrace();
			Logs.LogException(e);
			throw e;
			// TODO: handle exception
		} finally {

			CloseOResultSet(ls);
		}
		return 0;
	}

	public Map<Integer, Integer> GetCategoryIDByListProductID(String stridlist) {
		Map<Integer, Integer> listCate = new HashMap<Integer, Integer>();

		OResultSet ls = null;
		String sql = "";
		try {
			sql = " select categoryid.asInteger() as categoryid ,productid from  product where  productid in["
					+ stridlist + "]";
			ls = factoryRead.Query(sql);
			if (ls != null) {
				while (ls.hasNext()) {

					var f = ls.next();
					if (f != null) {
						if (f.getProperty("categoryid") != null) {
							var categoryid = Integer.valueOf(f.getProperty("categoryid").toString());
							var productid = Integer.valueOf(f.getProperty("productid").toString());
							listCate.put(productid, categoryid);

						}
					}

				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			throw e;
			// TODO: handle exception
		} finally {

			CloseOResultSet(ls);
		}
		return listCate;
	}

	public int getMainGroupIDByProductID(long productID) throws Throwable {
		// var rs = factoryRead.queryFunction("product_GetMainGroupIDByProductID",
		// ORIntWrapper[].class, productID);
		var rs = factoryRead.queryFunctionCached("product_GetMainGroupIDByProductID", ORIntWrapper[].class, productID);

		return rs == null || rs.length == 0 ? -1 : rs[0].intresult;
	}

	public int getSubscribedCustomerListID(int productID) throws Throwable {
		// var rs = factoryRead.queryFunction("product_GetCustomerList",
		// ORIntWrapper[].class, productID);

		var rs = factoryRead.queryFunctionCached("product_GetCustomerList", ORIntWrapper[].class, productID);

		return rs == null || rs.length == 0 ? 0 : rs[0].intresult;
	}

	public int getReplatePropCached(int categoryID, int siteID, String langID) throws Throwable {

		var rs = factoryRead.queryFunctionCached("product_GetRelatePropertyID", ORIntWrapper[].class, categoryID,
				siteID, langID);

		return rs == null || rs.length == 0 ? 0 : rs[0].intresult;
	}

	public int getReplateProp(int categoryID, int siteID, String langID) throws Throwable {
		var rs = factoryRead.queryFunction("product_GetRelatePropertyID", ORIntWrapper[].class, categoryID, siteID,
				langID);

		return rs == null || rs.length == 0 ? 0 : rs[0].intresult;
	}

	public int[] getReplatePropList(int categoryID, int siteID, String langID) throws Throwable {
//		var rs = factoryRead.queryFunction("product_GetRelatePropertyIDList", StringWrapper[].class, categoryID, siteID,
//				langID);

		var rs = factoryRead.queryFunctionCached("product_GetRelatePropertyIDList", StringWrapper[].class, categoryID,
				siteID, langID);

		if (rs != null && rs.length > 0) {
			String[] props = rs[0].intresult.split(",");
			int[] reVal = new int[props.length];
			for (int i = 0; i < props.length; i++) {
				try {
					reVal[i] = Integer.valueOf(props[i]);
				} catch (Exception ex) {
				}
			}
			return reVal;
		}
		return new int[] {};
	}

	public List<Integer> getReplatePropList(StringWrapper[] rs) {
		if (rs != null && rs.length > 0) {
			String[] props = rs[0].intresult.split(",");
			return Stream.of(props).map(x -> {
				return Utils.toInt(x);
			}).collect(Collectors.toList());
		}
		return null;
	}

	public ProductPropDetailBO getProductPropDetailBO(int productID, int siteID, String langID, int cateiD)
			throws Throwable {
		var propDetail = factoryRead.QueryFunction("product_getrelativeproperty", ProductPropDetailBO[].class, false,
				productID, siteID, langID, cateiD);
		return propDetail != null && propDetail.length > 0 ? propDetail[0] : null;
	}

	public MainSubGroupBO getMainSubGroupIDByProductID(long productID) throws Throwable {
		var key = productID + "";
		var rs = g_listMainSubGroup.get(key);
		if (rs == null) {
			var ars = factoryRead.queryFunction("product_GetMainSubGroupIDByProductID", MainSubGroupBO[].class,
					productID);
			rs = ars == null || ars.length == 0 ? null : ars[0];
			g_listMainSubGroup.put(key, rs);
		}
		return rs;
	}

	public ProductPropBO[] GetProductPropByCategory(int categoryID, int siteID, String langID) throws Throwable {
		var result = factoryRead.QueryFunction("product_getPropValueByCate", ProductPropBO[].class, false, categoryID,
				siteID, langID);

		return result;
	}

	public ProductPropBO[] GetProductPropByCategoryByListID(int[] idlist, int siteID, String langID) throws Throwable {

		var result = factoryRead.QueryFunction("product_getPropValueByCateByListID", ProductPropBO[].class, false,
				siteID, langID, idlist);
		return result;
	}

	public String getDefaultCode(long productID, int siteID, int provinceID) {
		OResultSet ls = null;
		String sql = "";
		try {
			sql = "select productcode from price_default where productid = " + productID + " and provinceid = "
					+ provinceID + " and siteid = " + siteID + " limit 1";
			ls = factoryRead.Query(sql);
			if (ls != null) {
				while (ls.hasNext()) {

					var f = ls.next();
					if (f != null) {
						if (f.getProperty("productcode") != null) {
							return f.getProperty("productcode").toString();
						} else {
							return "";
						}
					}

					return "";
					// return l;// (long) Long.parseLong( productlangRID) ; //.00
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			throw e;
			// TODO: handle exception
		} finally {

			CloseOResultSet(ls);
		}
		return "";
	}

	public long GetProductIDByProductCode(String productCode) {
		if (Utils.StringIsEmpty((productCode)))
			return 0;
		OResultSet ls = null;
		try {
			ls = factoryRead.Query("select productidref from pm_product where productid='" + productCode + "' limit 1");
			if (ls != null) {
				while (ls.hasNext()) {

					var f = ls.next();
					if (f != null) {
						if (f.getProperty("productidref") != null) {
							String productlangRID = f.getProperty("productidref").toString();
							return Double.valueOf(productlangRID).longValue();
						} else {
							return 0;
						}
					}

				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			throw e;
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}
		return 0;
	}

	public void CloseOResultSet(OResultSet vao) {

		try {
			if (vao != null)
				vao.close();
		} catch (Exception e) {

		}

	}

	public int GetProductCategoryIDFromCode(String productCode) throws Throwable {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("code", productCode);

		String functionname = "select product_GetProductCategoryFromCode(:code) as rs";

		try {

			var result = factoryRead.QueryFunction(  functionname, params, ProductCate[].class, false)[0];
			if (result != null && result.product != null) {
				return (int) Math.round(result.product.categoryid);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			throw e;
			// TODO: handle exception
		}

		return 0;
	}

//	public void Dispose() {
//		database.Close();
//	}
//	public String GetProductBOByProductIDTest(int productID, int siteID, String lang) {
//
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("p", productID);
//		params.put("s", siteID);
//		params.put("l", lang);
//
//		String functionname = "select product_GetById(:p,:s,:l) as rs";
//		OResultSet ls = null;
//		try {
//
//			ls = factoryRead.QueryFunction(functionname, params);
//			if (ls != null) {
//
//				while (ls.hasNext()) {
//					var f = ls.next();
//					if (f != null)
//						return f.toJSON();
//
//					// return Arrays.asList(gson.fromJson(js2, ProductBO[].class)).get(0);
//				}
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//			Logs.LogException(e);
//			;
//			// TODO: handle exception
//		} finally {
//			if (ls != null)
//				ls.close();
//		}
//
//		return null;
//	}

	public ProductBO[] GetProductBOByListID(int[] productID, int siteID, int provinceID, String lang) throws Throwable {
//		if (siteID == 11)
//			return factoryRead.QueryFunction("product_GetByListIDCodeSimpleWithPriceDefV2BHX", ProductBO[].class, false,
//					productID, siteID, provinceID, lang, 1);

//		if (siteID == 1 || siteID == 2)
//			return getPro
//		else
//			return factoryRead.QueryFunction(function_GetProductByListID, ProductBOApi[].class, false, productID,
//					siteID, provinceID, lang, 1);
		return GetProductBOByListID_PriceStrings(productID, siteID, provinceID, lang);
	}

	public ProductBO[] GetProductBOByListID_PriceStrings(int[] productID, int siteID, int provinceID, String lang)
			throws Throwable {
		var list = factoryRead.QueryFunction("product_GetByIdListWithPriceStrings", ProductBO[].class, false, productID,
				siteID, provinceID, lang, DidxHelper.getPriceAreaBySiteID(siteID, lang));
		for (var item : list) {
			processSimplePriceStrings(item, siteID, provinceID);
		}
		return list;
	}

	public ProductBO[] GetProductBOSimpleByListID_PriceStrings_INVALID(int[] productID, int siteID, int provinceID,
			String lang) throws Throwable {
		var list = factoryRead.QueryFunction("product_GetByListIDCodeSimpleWithPriceStrings", ProductBO[].class, false,
				productID, siteID, provinceID, lang, DidxHelper.getPriceAreaBySiteID(siteID, lang));
		for (var item : list) {
			processSimplePriceStrings(item, siteID, provinceID);
		}
		return list;
	}

	public ProductBO[] GetProductByListIDtest(int[] productID, String[] productCode, int siteID, int provinceID,
			String lang) throws Throwable {
		return factoryRead.QueryFunction("product_GetByListIDCodeSimple2", ProductBO[].class, false, productID, siteID,
				provinceID, lang, 1);
	}

	public ProductBO GetProductBOByProductID(int productID, int siteID, int provinceID, String lang, int storeID)
			throws Throwable {
		ProductBO[] product = null;

		if (siteID == 1 || siteID == 2) {
			product = factoryRead.QueryFunction("product_GetByIdWithPriceDef", ProductBO[].class, false, productID,
					siteID, provinceID, lang);
		} else if (siteID == 11) {
			product = factoryRead.QueryFunction("product_GetByIdWithPriceDef_BHX", ProductBO[].class, false, productID,
					11, provinceID, lang, storeID);
		} else {
			product = factoryRead.QueryFunction("product_GetByIdWithPriceDef", ProductBOApi[].class, false, productID,
					siteID, provinceID, lang);
		}
		return product == null || product.length == 0 ? null : product[0];
	}
	public ProductBO getProductBOByProductID_PriceStrings(int productID, int siteID, int provinceID, String lang,
														  int storeID) throws Throwable {
		return  getProductBOByProductID_PriceStrings(productID,siteID,provinceID,lang,storeID, null);
	}

	public ProductBO getProductBOByProductID_PriceStrings(int productID, int siteID, int provinceID, String lang,
			int storeID,String productCode) throws Throwable {
		// TODO: hàm get này lấy ko lên
		// if (siteID == 11)
		// return GetProductBOByProductID(productID, siteID, provinceID, lang, storeID);
//		var products = factoryRead.queryFunctionTimeCached(5, "product_GetByIdWithPriceStrings", ProductBO[].class, productID,
//				siteID, provinceID, lang, DidxHelper.getDefaultPriceAreaBySiteID(siteID, lang));
//		var products = !Utils.StringIsEmpty(productCode) ?
//				factoryRead.queryFunction("product_priceStringBOByProductCode", ProductBO[].class, productID, siteID, provinceID, lang, DidxHelper.getDefaultPriceAreaBySiteID(siteID, lang),productCode)
//				: factoryRead.queryFunction("product_GetByIdWithPriceStrings", ProductBO[].class, productID, siteID, provinceID, lang, DidxHelper.getDefaultPriceAreaBySiteID(siteID, lang));
        String func = "product_GetByIdWithPriceStrings";
		var products = factoryRead.queryFunction(func, ProductBO[].class, productID,
				siteID, provinceID, lang, DidxHelper.getDefaultPriceAreaBySiteID(siteID, lang));
		return products == null || products.length == 0 ? null : products[0];
	}

	public StockStore GetStockByStore(String productCode, int storeID) throws Throwable {

		var result = factoryRead.QueryFunction("product_GetStockQuantityByStoreID", StockStore[].class, false,
				productCode, storeID);
		if (result != null && result.length > 0) {
			return result[0];
		}
		return null;
	}

	public StockStore[] GetStockByListStore(String productCode, int[] storeIDlist) throws Throwable {

		var result = factoryRead.QueryFunction("product_GetStockQuantityByStoreIDList", StockStore[].class, false,
				productCode, storeIDlist);
		return result;
	}

	public int[] GetListStoresOnlineOnly(String productCode) throws Throwable {

		var result = factoryRead.QueryFunction("product_getOnlyStoreList", ORIntArrWrapper[].class, false, productCode);
		if (result != null && result.length > 0) {
			return result[0].intresult;
		}
		return new int[0];
	}

	// overload
//	public ProductBO GetProductBOByProductID(int productID, int siteID, int provinceID, String lang, String defaultCode,
//			boolean priceList, HeaderBO headerBO) {
//
//		headerBO.OrientDBStartTime = System.currentTimeMillis();
//
//		var result = database.QueryFunction(priceList ? "product_GetByIdWithPriceList" : "product_GetById",
//				ProductBO[].class, false, productID, siteID, provinceID, lang, defaultCode)[0];
//		headerBO.OrientDBEndTime = System.currentTimeMillis();
//
//		return result;
//	}

	public List<ProductErpPriceBO> GetPriceByProductID(long productID) throws Throwable {
		if (GConfig.ProductTaoLao.containsKey(productID)) {
			return null;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", productID);
		String functionname = "select product_GetPriceByID(:p) as rs";
		return Arrays.asList(factoryRead.QueryFunction( functionname, params, ProductErpPriceBO[].class, true));
	}

	public ProductGalleryBOApi[] GetGalleryByProductID(int productID, int siteID, int imageType) throws Throwable {
		if (imageType <= 0)
			return factoryRead.QueryFunctionCached("product_GetGalleryByProductID", ProductGalleryBOApi[].class, false,
					productID, siteID);
		else
			return factoryRead.QueryFunctionCached("product_GetGalleryByProductIDAndIType", ProductGalleryBOApi[].class,
					false, productID, siteID, imageType);
	}

	public OResult GetProductBOByProductIDOResult(int productID, int siteID, String lang) throws Throwable {
		if (GConfig.ProductTaoLao.containsKey((long) productID)) {
			return null;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", productID);
		params.put("s", siteID);
		params.put("l", lang);

		String functionname = "select product_GetById(:p,:s,:l) as rs";
		OResultSet ls = null;
		try {

			ls = factoryRead.QueryFunction(functionname, params);
			if (ls != null) {
				while (ls.hasNext()) {

					return ls.next();
				}
			}
		} catch (Throwable ex) {
			Logs.LogException(ex);
			throw ex;
		} finally {
			if (ls != null)
				ls.close();
		}

		return null;
	}

	public ProductBO GetProductOldModelFromCache(long productID, int provinceID, int siteID, String lang)
			throws Throwable {
		if (GConfig.ProductTaoLao.containsKey(productID))
			return null;

		String key = "" + productID + "-" + siteID + "-" + lang;
		ProductBO rs = null;
		if (g_listProductOLdModel.containsKey(key)) {
			rs = g_listProductOLdModel.get(key);
			// lay data nhanh qua xin phep cache lai, sai ke me
			if (rs != null && System.currentTimeMillis() - rs.api_MemCacheDate > 30 * 1000) {
				g_listProductOLdModel.remove(key);
			} else {
				rs.api_MemCacheSource = "Memcached";
				return rs;

			}
		}
		rs = GetProductOldModel(productID, provinceID, siteID, lang);
		if (rs == null)
			return rs;
		rs.api_MemCacheDate = System.currentTimeMillis();

		g_listProductOLdModel.put(key, rs);
		rs.api_MemCacheSource = "ORDB";
		return rs;

	}

	public ProductBO GetProductOldModel(long productID, int provinceID, int siteID, String lang) throws Throwable {
		if (GConfig.ProductTaoLao.containsKey(productID))
			return null;
		var temp = factoryRead.QueryFunction("product_GetByIdWithPriceDef", ProductBO[].class, false, productID, siteID,
				provinceID, lang);
		if (temp != null && temp.length > 0) {
			return temp[0];
		} else {
			return null;
		}
	}

	public ProductBO GetProductBOByProductIDSEFromCache(long productID, int siteID, String lang) throws Throwable {

		if (GConfig.ProductTaoLao.containsKey(productID))
			return null;

		String key = "" + productID + "-" + siteID + "-" + lang;
		ProductBO rs = null;
		if (g_listProduct.containsKey(key)) {
			rs = g_listProduct.get(key);
			// lay data nhanh qua xin phep cache lai, sai ke me
			if (System.currentTimeMillis() - rs.api_MemCacheDate > 30 * 1000) {
				g_listProduct.remove(key);
			} else {
				rs.api_MemCacheSource = "Memcached";
				return rs;

			}
		}
		rs = GetProductBOByProductIDSE(productID, siteID, lang);
		if (rs == null)
			return null;
		rs.api_MemCacheDate = System.currentTimeMillis();
		g_listProduct.put(key, rs);
		rs.api_MemCacheSource = "ORDB";
		return rs;

	}

	public ProductBO GetProductBOByProductIDSE(long productID, int siteID, String lang) throws Throwable {
		if (GConfig.ProductTaoLao.containsKey(productID))
			return null;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", productID);
		params.put("s", siteID);
		params.put("l", lang);

		String functionname = "select product_GetByIdSE(:p,:s,:l) as rs";
		;
		ProductBO[] temp = factoryRead.QueryFunction( functionname, params, ProductBO[].class, true);
		if (temp != null && temp.length > 0) {
			return temp[0];
		} else {
			return null;
		}
	}

	public RootObjectProductdetail GetPropProductDetail(long ProductID, int siteID, String LangID) throws Throwable {
		if (GConfig.ProductTaoLao.containsKey(ProductID)) {
			return null;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", ProductID);
		params.put("s", siteID);
		params.put("l", LangID);
		String functionname = "select product_GetPropProductDetail(:p,:s,:l) as rs";
		RootObjectProductdetail[] temp = factoryRead.QueryFunction( functionname, params,
				RootObjectProductdetail[].class, true);
		if (temp != null && temp.length > 0) {
			return temp[0];
		} else {
			return null;
		}

	}

	public RootObjectProductdetail GetPropProductDetailDidx(long ProductID, int siteID, String LangID)
			throws Throwable {
		if (GConfig.ProductTaoLao.containsKey(ProductID)) {
			return null;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", ProductID);
		params.put("s", siteID);
		params.put("l", LangID);
		String functionname = "select product_GetPropProductDetail_V1(:p,:s,:l) as rs";
		return factoryRead.QueryFunction( functionname, params, RootObjectProductdetail[].class, true)[0];

	}

	public List<StoreBO> GetStoreByProvinceID(int provinceID, int districtID, int brandID) throws Throwable {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", provinceID);
		params.put("d", districtID);
		params.put("b", brandID);
		String functionname = "select product_SearchStoreByProvinceID(:p,:d,:b) as rs";
		return Arrays.asList(factoryRead.QueryFunction( functionname, params, StoreBO[].class, true));
	}

	public StoreBO[] getStoreByProvinceIDList(int[] provinceIDs, int brandID, boolean it364) throws Throwable {
		String funcname = it364 ? "product_SearchStoreByProvinceIDList364" : "product_SearchStoreByProvinceIDList";
		return factoryRead.queryFunction(funcname, StoreBO[].class, provinceIDs, brandID);
	}

	public StoreBO[] getStoreByProvinceID(int provinceID, int districtID, int brandID, boolean it364) throws Throwable {
		String funcname = it364 ? "product_SearchStoreByProvinceID364" : "product_SearchStoreByProvinceID";
		return factoryRead.queryFunction(funcname, StoreBO[].class, provinceID, districtID, brandID);
	}

	public ResultStockByMainSubGroup GetStockMainSubGroupByProductCode(String FunctionName, String ProductCode,
			int siteID, int provinceID) throws Throwable {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", ProductCode);
		params.put("s", siteID);
		params.put("l", provinceID);
		String functionname = "select " + FunctionName + "(:p,:s,:l) as rs";
		var x = factoryRead.QueryFunction( functionname, params, ResultStockByMainSubGroup[].class, true);
		if (x != null && x.length > 0)
			return x[0];
		return null;

	}

	public String GetStockStoreByProductID(long Productid, int provinceID) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", Productid);

		params.put("b", provinceID);
		String functionname = "select product_GetStockStoreByProductID(:p,:b) as rs";
		String ra = factoryRead.QueryFunctionToString(functionname, params);

		// [{"storelist":[50,5770

		// ra = ra.replaceAll(".0", "");
		ra = ra.replace("[{\"storelist\": [", "");// .replaceAll("[{\"storelist\": [", "");
		ra = ra.replaceAll("]}]", "");
		return ra;
		// [{"storelist": [1098.0, 384.0, 415.0, 975.0, 975.0, 415.0, 975.0, 1098.0,
		// 1098.0, 384.0]}]}
	}

	// CRM
//	public String GetAllStockStoreByProductID(long Productid) {
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("p", Productid);
//		String functionname = "select product_GetAllStockStoreByProductID(:p ) as rs";
//		String ra = factoryRead.QueryFunctionToString(functionname, params);
//		//ra = ra.replaceAll(".0", "");
//		ra = ra.replace("[{\"storelist\": [", "");// .replaceAll("[{\"storelist\": [", "");
//		ra = ra.replaceAll("]}]", "");
//		return ra;
//	}

//	public String GetStockStoreByProductCode(String ProductCode, int brandid, int provinceID) {
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("p", ProductCode);
//		params.put("d", brandid);
//		params.put("b", provinceID);
//		String functionname = "select product_GetStockStoreByProductCode(:p,:d,:b) as rs";
//		String ra = factoryRead.QueryFunctionToString(functionname, params);
//		//ra = ra.replaceAll(".0", "");
//		ra = ra.replace("[{\"storelist\": [", "");// .replaceAll("[{\"storelist\": [", "");
//		ra = ra.replaceAll("]}]", "");
//		return ra;
//		// [{"storelist": [1098.0, 384.0, 415.0, 975.0, 975.0, 415.0, 975.0, 1098.0,
//		// 1098.0, 384.0]}]}
//	}

	public List<ProductDetailBO> GetListPropProduct(int productID, int siteID, String lang, ProductBO productBO)
			throws Throwable {
		if (GConfig.ProductTaoLao.containsKey((long) productID)) {
			return null;
		}
		List<ProductDetailBO> iListProductDetails = new ArrayList<ProductDetailBO>();

		var productSE = GetPropProductDetail(productID, siteID, lang);

		if (productBO != null && productSE != null) {

			for (var e : productSE.productdetail) {
				int propertyid = (int) e.PropertyID;// int.Parse(e.GetValue("propertyid").ToString());
				int propertytype = e.PropertyType; // 0 text, 1 1 id, 2 nhieu cach dau ,

//				if (propertyid == 19879 || propertyid==88  || propertyid==6240) {
//					try {
//						System.out.println(mapper.writeValueAsString(productSE.productdetail));
//					} catch (JsonProcessingException e1) {
//						e1.printStackTrace();
//					}
//				}
				String languageid = e.LanguageID;// e.GetValue("languageid").ToString();
				String oldvalue = StringUtils.strip(e.Value, ",");
				int isfeatureprop = e.IsFeatureProp ? 1 : 0;// int.Parse(e.GetValue("isfeatureprop").ToString());
				var isSplit = oldvalue != null && !oldvalue.isBlank() ? oldvalue.split(",") : new String[0];

//				if (ArrayUtils.contains(isSplit, String.valueOf(56824))) {
//
//					System.out.println("dsd");
//				}
//			  Set<String> listValueid = new HashSet<String>(Arrays.asList(
//						  isSplit
//						));

				if (propertytype <= 0) {
					ProductDetailBO productDetails = new ProductDetailBO();
					productDetails.PropertyID = propertyid;
					productDetails.LanguageID = languageid;
					productDetails.IsFeatureProp = isfeatureprop == 1 ? true : false;
					productDetails.PropValueID = 0;
					productDetails.PropValue = e.Value;

					iListProductDetails.add(productDetails);
				}
				if (propertytype >= 1) {
					// value la araay id list
					var PropValueDetail = getPropValueDetail(propertyid, siteID, lang);
					if (PropValueDetail != null && PropValueDetail.value.size() > 0) {
//						for (var ele : PropValueDetail.value.get(0).prop) {
//
//						}
//if (isNumeric(oldvalue) && valueid == Integer.parseInt(oldvalue)) {
//					if (PropValueDetail != null && PropValueDetail.value.size() > 0
//							&& PropValueDetail.value.get(0).prop != null && PropValueDetail.value.get(0).prop.size() > 0
//							&& PropValueDetail.value.get(0).prop.get(0).propertytype != 0) {
						for (var ele : PropValueDetail.value.get(0).propvalue) {
							// JObject vl = JObject.Parse(ele);

							var propvaluelang = ele.propvaluelang;// ele.GetValue("propvaluelang");
							if (propvaluelang != null && propvaluelang.size() > 0) {
								for (int i = 0; i < propvaluelang.size(); i++) {

									int valueid = (int) propvaluelang.get(i).Valueid;
									// if (propvaluelang.get(i).Isinitsearch == 1) {
									if (ArrayUtils.contains(isSplit, String.valueOf(valueid))) {
										// Do some stuff.

										ProductDetailBO productDetails = new ProductDetailBO();
										productDetails.PropertyID = propertyid;
										productDetails.LanguageID = languageid;
										productDetails.IsFeatureProp = isfeatureprop == 1 ? true : false;
										productDetails.PropValueID = valueid;
										productDetails.PropValue = propvaluelang.get(0).Value;
										productDetails.PropIssearch = propvaluelang.get(0).Issearch == 1 ? true : false;
										productDetails.IsSearch = propvaluelang.get(0).Issearch == 1 ? true : false;

										productDetails.CompareValue = (int) propvaluelang.get(0).Comparevalue;

										iListProductDetails.add(productDetails);
									}
								}

								// }
							}
						}
						// }
					}
				}
			}

			return iListProductDetails;

		} else {

			return iListProductDetails;
		}
	}

	public static boolean isNumeric(String strNum) {
		try {
			Double.parseDouble(strNum);
		} catch (NumberFormatException | NullPointerException nfe) {
			return false;
		}
		return true;
	}

	public RootObjectPropvalue getPropValueDetail(int propertyid, int siteID, String LangID) throws Throwable {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", propertyid);
		params.put("s", siteID);
		params.put("l", LangID);
		String functionname = "select product_GetPropValueDetail(:p,:s,:l) as rs";
		RootObjectPropvalue[] ra = factoryRead.QueryFunction( functionname, params, RootObjectPropvalue[].class, false);
		if (ra != null && ra.length > 0) {
			return ra[0];
		}
		return null;
	}

	public int[] getPropValueId(int propertyID, int siteID, String LangID) throws Throwable {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", propertyID);
		params.put("s", siteID);
		params.put("l", LangID);
		String functionname = "select product_getPropValueId(:p,:s,:l) as rs";
		var propvalue = factoryRead.QueryFunction( functionname, params, PropValueID[].class, false);
		int[] rs = Stream.of(propvalue).flatMapToInt(valueids -> Arrays.stream(valueids.valueid)).toArray();
		return rs.length > 0 ? rs : new int[0];
	}

	/**
	 * Lấy giá mặc định cho sản phẩm
	 *
	 * @param product
	 * @param priceHelper
	 */
//	public void GetDefaultPrice(ProductBO product, int provinceID, PriceHelper priceHelper) {
//		String key = DidxHelper.GetElasticKey(product.ProductID, (int) product.ProductLanguageBO.SiteID,
//				product.ProductLanguageBO.LanguageID);
//		var so = ElasticClient.getInstance().GetSingleObject(CurrentIndexDB, CurrentTypeDB, key, ProductSO.class);
//		var pricebo = new ProductErpPriceBO();
//		String code = "";
//		if (so != null && so.Prices != null) {
//			pricebo.Price = (double) so.Prices.get("Price_" + provinceID);
//			pricebo.WebStatusId = (int) so.Prices.get("WebStatusId_" + provinceID);
//			pricebo.IsShowHome = (int) so.Prices.get("IsShowHome_" + provinceID) == 1;
//			code = (String) so.Prices.get("ProductCode_" + provinceID);
//		}
//		// get default price
//		String fcode = code;
//		// var priceDefault = priceHelper.GetPriceByProductCode(fcode, provinceID);
//		if (product.CategoryID != 42 && product.ProductLanguageBO != null && product.ProductLanguageBO.SiteID == 12) {// get
//																														// gia
//																														// pk
//																														// 402
//			// var priceDefault = priceHelper.product_GetPriceByArea(fcode, provinceID);
//		}
//		product.ProductErpPriceBO = pricebo;
//
//		product.Promotion = product.Promotion.stream().filter(p -> p.ProductCode.equals(fcode))
//				.collect(Collectors.toList());
//	}

	/**
	 * Lấy ProductSO để lấy code, giá mặc định (dùng cho hàm detail)
	 *
	 * @param productID
	 * @param siteID
	 * @param lang
	 * @return
	 * @throws Throwable
	 */
	public ProductSO GetProductSO(int productID, int siteID, String lang) throws Throwable {

		String key = DidxHelper.GetElasticKey(productID, siteID, lang);
		return ElasticClient.getInstance(config.SERVER_ELASTICSEARCH_READ_HOST).GetSingleObject(CurrentIndexDB, key,
				ProductSO.class);
	}

	// overload
//	public ProductSO GetProductSO(int productID, int siteID, String lang, HeaderBO headerBO) {
//		String key = DidxHelper.GetElasticKey(productID, siteID, lang);
//		headerBO.ESStartTime = System.currentTimeMillis();
//		var result = ElasticClient.getInstance().GetSingleObject(CurrentIndexDB, CurrentTypeDB, key, ProductSO.class);
//		headerBO.ESEndTime = System.currentTimeMillis();
//
//		return result;
//	}

	public ProductBO[] SearchProduct(ProductQuery productQuery) throws Throwable {
		var timer = new CodeTimers();
		var solist = Ela_SearchProduct(productQuery, false, false, false, timer);
		var aids = solist.productList.keySet().stream().mapToInt(x -> x).toArray();
		return GetSimpleProductListByListID_PriceStrings_soMap(aids, productQuery.SiteId, productQuery.ProvinceId,
				productQuery.CategoryId, productQuery.LanguageID, timer);
	}

	public ProductColorBO[] GetProductColorByProductID(int productID) throws Throwable {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", productID);
		String functionname = "select product_GetProductColorByProductID(:p) as rs";
		var colorCode = factoryRead.QueryFunction(functionname, params, ProductColorBO[].class, false);

		if (colorCode != null && colorCode.length > 0) {
			var CodeGallery = factoryRead.QueryFunction("productcode_gallery_getbyProductID",
					ProductCodeGalleryBO[].class, false, productID);
			if (CodeGallery != null && CodeGallery.length > 0) {

				for (ProductColorBO x : colorCode) {
					var tmp = Stream.of(CodeGallery).filter(c -> c.ProductCode.equals(x.ProductCode)).findFirst()
							.orElse(null);
					if (tmp != null) {
						x.Image = tmp.Image;
						x.Bimage = tmp.Bimage;
						x.Mimage = tmp.Mimage;
						x.Simage = tmp.Simage;
						x.DisplayOrder = tmp.DisplayOrder;
					}
				}

			}

		}
		return colorCode;

		// return null;
	}

	public ProductColorBO[] GetProductColorByProductIDLang(int productID, String lang) throws Throwable {

//		return factoryRead.QueryFunction("product_GetColorByID", ProductColorBO[].class, false, productID, lang);
		var colorCode = factoryRead.QueryFunction("product_GetColorByID", ProductColorBO[].class, false, productID,
				lang);

		if (colorCode != null && colorCode.length > 0) {
			var CodeGallery = factoryRead.QueryFunction("productcode_gallery_getbyProductID",
					ProductCodeGalleryBO[].class, false, productID);
			if (CodeGallery != null && CodeGallery.length > 0) {

				for (ProductColorBO x : colorCode) {
					var tmp = Stream.of(CodeGallery).filter(c -> c.ProductCode.equals(x.ProductCode)).findFirst()
							.orElse(null);
					if (tmp != null) {
						x.Image = tmp.Image;
						x.Bimage = tmp.Bimage;
						x.Mimage = tmp.Mimage;
						x.Simage = tmp.Simage;
						x.DisplayOrder = tmp.DisplayOrder;
					}
				}

			}

		}
		return colorCode;

	}

	// nghia test
	public ProductErpPriceBO[] GetListPricesByProductIDSiteProvinceLang(int productID, int provinceID, int siteID,
			String lang) throws Throwable {
		return factoryRead.QueryFunction("product_GetListPriceByProductProvinceSite", ProductErpPriceBO[].class, false,
				productID, siteID, lang, provinceID);

	}

	public ProductGeneral[] getAllInfoColorByProductID(int productID, int siteID, String langID, int priceArea)
			throws Throwable {
		return factoryRead.QueryFunction("product_getcolorbyproductidandprovince", ProductGeneral[].class, false,
				productID, siteID, langID, priceArea);

	}

	public ProductBO[] GetSimpleProductListByListID(int[] ids, int siteID, int provinceID, String langID)
			throws Throwable {
		return factoryRead.QueryFunction("product_GetByListIDSimpleV1", ProductBO[].class, false, ids, siteID,
				provinceID, langID);
	}

	public ProductBO[] GetSimpleProductListByListIDCode(int[] ids, int siteID, int provinceID, String langID)
			throws Throwable {
		if (siteID == 1 || siteID == 2)
			return factoryRead.QueryFunction(function_GetProductByListID, ProductBO[].class, false, ids, siteID,
					provinceID, langID);
		else
			return factoryRead.QueryFunction(function_GetProductByListID, ProductBOApi[].class, false, ids, siteID,
					provinceID, langID);

	}

	public ProductBO[] GetSimpleProductListByListID_PriceStrings(int[] ids, int siteID, int provinceID, String langID)
			throws Throwable {
//		var products = factoryRead.queryFunction("product_GetByIdWithPriceStrings", ProductBO[].class, productID,
//				siteID, provinceID, lang, DidxHelper.getDefaultPriceAreaBySiteID(siteID, lang));
//		return products == null || products.length == 0 ? null : products[0];
		ProductBO[] list = new ProductBO[0];
		if (ids != null && ids.length > 0) {
			list = factoryRead.queryFunction("product_GetByListIDCodeSimpleWithPriceStrings", ProductBO[].class, ids,
					siteID, provinceID, langID, DidxHelper.getPriceAreaBySiteID(siteID, langID));
			for (var item : list) {
				if (item.ProductCategoryBO != null && !item.ProductCategoryBO.IsActived) {
					// nếu cate ko actived
					item.ProductCategoryBO = null;
					item.ProductCategoryLangBO = null;
				}
				processSimplePriceStrings(item, siteID, provinceID);
			}
			getHelperBySite(siteID).processSimpleDetails(list, provinceID, siteID, langID);
		}
		return list;
	}

	/**
	 * Hàm này trả ra giống <b>GetSimpleProductListByListID_PriceStrings</b> nhưng
	 * có thêm thông tin khác trên elastic (trả góp, stickerlabel, v.v...)
	 */
	public ProductBO[] GetSimpleProductListByListID_PriceStrings_soMap(int[] ids, int siteID, int provinceID,
			int categoryID, String langID, CodeTimers timer) throws Throwable {
		if (timer == null) {
			timer = new CodeTimers();
		}
		if (ids.length == 0) {
			return new ProductBO[0];
		}
		// flag de khong lay dong ho cap
		timer.start("get-product-odb-cwatch-get");
		Map<Integer, List<ProductBO>> cMap = null;

		/*
		 * 7264: đồng hồ thông minh 7007 đồng hồ thời trnag 42: DT 2002 máy lạnh
		 */
		if (ids[ids.length - 1] != -9999 && (categoryID == 7264 || categoryID == 7077 || categoryID == 42
				|| categoryID == 2002 || categoryID <= 0)) {
			cMap = getListCoupleWatch(ids, siteID, provinceID, langID);

		} else {
			cMap = new HashMap<>();
		}
		timer.pause("get-product-odb-cwatch-get");
		timer.start("get-product-odb-query");
		ProductBO[] result = GetSimpleProductListByListID_PriceStrings(ids, siteID, provinceID, langID);
		timer.pause("get-product-odb-query");
		timer.start("get-product-odb-somap");
		var somap = ids.length > 0 ? getSOMap(ids, siteID, langID) : null;
		timer.pause("get-product-odb-somap");
		timer.start("get-product-odb-details");
		var detailmap = getODBCachedDetail(ids, siteID, langID);
		timer.pause("get-product-odb-details");
		Map<Integer, ProductDetailBO[]> detailmapsPresent = null;
		if (categoryID == 42 && cMap.size() > 0) {
			timer.start("get-product-odb-details-Present");
			var idlist = cMap.values().stream().flatMap(l -> l.stream()).mapToInt(x -> x.ProductID).toArray();
			detailmapsPresent = getODBCachedDetail(idlist, siteID, langID);
			timer.pause("get-product-odb-details-Present");
		}

		var helper = getHelperBySite(siteID);
		if (somap != null) {
			timer.start("get-product-odb-parse");
			for (var p : result) {
				var so = somap.get(p.ProductID);
				if (so != null) {
					p.IsPayment = so.IsPayment == 1;
					p.Paymentfromdate = so.PaymentFromDate;
					p.Paymenttodate = so.PaymentToDate;
					p.PercentInstallment = so.PercentInstallment;
//					if (!Strings.isNullOrEmpty(so.StickerLabel)) {
//						p.StickerLabel = so.StickerLabel;
//					}
					p.ProductSoldCount = so.ProductSoldCount;
				}
				var detail = detailmap.get(p.ProductID);
				if (detail != null && detail.length > 0) {
					// disadvantage
					p.DisAdvantage = helper.getDisadvantage(detail, siteID);
					String shortName = processShortName(p, detail).replace("  ", " ");
					if (p.ProductLanguageBO != null) {
						p.ProductLanguageBO.shortNameProcessed = shortName.trim();
					}

					//
					var apid = getAnnouncedYearPropByCateId(p.CategoryID);
					var stickerpid = getStickerLabelPropByCateID(p.CategoryID);
					var lastStickerComp = -9999999;
					for (var x : detail) {
						if (x == null)
							continue;
						// announcedyear
						if (apid == x.PropertyID) {
							p.AnnouncedYear = x.Value;
						}

						// shortname prop
						if (x.PropertyID == 26618) {
							p.shortNameProp = x.Value;
						}

						// feature prop
						if (p.ProductCategoryBO != null && x.PropertyID == p.ProductCategoryBO.FeaturePropertyID) {
							p.FeaturePropertyName = x.PropertyName;
							p.FeaturePropertyValueId = x.PropValueID;
							p.FeaturePropertyValue = x.Value;
							p.FeaturePropertyCompareValue = x.CompareValue;
						}

						// stickerlabel
						if (x.PropertyID == stickerpid && x.Icon != null && x.CompareValue > lastStickerComp) {
							p.StickerLabel = x.Icon + "|" + x.PropValueID;
						}
					}
					if (p.AnnouncedYear == null) {
						p.AnnouncedYear = "0";
					}
				} else { // trường hợp k lấy đc ProductDetailBO -? xử lí shortname
					String shortName = processShortName(p).replace("  ", " ");
					if (p.ProductLanguageBO != null) {
						p.ProductLanguageBO.shortNameProcessed = shortName.trim();
					}
				}

				// specialsaleprogram
				if (!Strings.isNullOrEmpty(p.ProductCode)) {
					Pm_ProductBO codeinfo = new Pm_ProductBO() {
						{
							maingroupid = p.MainGroupID;
							subgroupid = p.SubGroupID;
							issetupproduct = p.isSetupProduct ? 1 : 0;
						}
					};
					p.SpecialSaleProgram = getSpecialSaleProgram2(codeinfo, p.ProductCode, 1);
				}
				categoryID = p.ProductCategoryBO == null ? -1 : p.ProductCategoryBO.CategoryID;
				if (categoryID == 2002) {
					var list = cMap.get(p.ProductID);
					if (list != null && list.size() > 0) {
						// list = list.stream().filter(x -> x.ProductErpPriceBO != null &&
						// x.ProductErpPriceBO.WebStatusId == 8).collect(Collectors.toList());
						processMultiAirConditions(p, list.toArray(ProductBO[]::new));
					}
				} else if (categoryID == 7264) {
					// dong ho cap
					var list = cMap.get(p.ProductID);
					if (list != null) {
						for (ProductBO dh : list) {
							dh.PromotionAtStore = null;
							dh.PromotionTotalValue = null;
							dh.PromotionByTimes = null;
							dh.PromotionCheckSalePrice = null;
							dh.PromotionExt = null;
							dh.PromotionShockPrice = null;
							dh.PromotionInstallment = null;
							dh.PromotionOnline = null;
							dh.PromotionSample = null;
							dh.PromotionSaving = null;
						}
						p.IsCoupleWatch = true;
						p.ListCoupleWatchBO = list.stream().toArray(ProductBO[]::new);
						p.ListCoupleWatchProductID = list.stream().mapToInt(x -> x.ProductID).toArray();
					}
				} else if (categoryID == 42 && p.ProductManuBO != null && p.ProductManuBO.ManufactureID == 80 ) {
					var list = cMap.get(p.ProductID);
					if (list != null) {
						processcombinedProduct(p, list, detailmapsPresent);

					}
				}
			}
			timer.pause("get-product-odb-parse");
		}
		return result;
	}

	public int getStickerLabelPropByCateID(int categoryID) {
		switch (categoryID) {
		case 42:
			return 21375;
		case 44:
			return 12040;
		case 522:
			return 21376;
		case 7077:
			return 21373;
		case 7264:
			return 22233;
		case 4728:// camera giám sát - hành tình
			return 26500;
		case 5697:
			return 27244;
		case 5698:
			return 27245;
		case 5693:
			return 27246;
		case 58: // Sạc, cáp
			return 26521;
		case 57: // Pin sạc dự phòng
			return 26520;
		case 54: // tai nghe
			return 26522;
		case 2162:// loa
			return 26094;
		case 55: // the nho
			return 26523;
		case 75: // USB
			return 26524;
		case 1902: // ổ cứng di động
			return 26525;
		case 86: // Chuột máy tính
			return 26526;
		case 4547: // Bàn phím
			return 26527;
		case 60: // Ôp ứng điện thoại
			return 26528;
		case 1363: // Miếng dán màn hình
			return 26530;
		case 7923: // Balo , túi chống sốc
			return 26531;
		case 4727: // Thiết bị mạng
			return 26532;
		case 9320: // Cảm biến áp suất lốp
			return 26533;
		case 9321: // Thiết bị định vị ô tô
			return 26534;
		case 9298: // Màn hình hiển thị thông tin lái xe
			return 26535;
		case 9398: // Máy tính cầm tay
			return 26536;
		case 9458: // Thiết bị nhà thông minh
			return 26519;
		case 9118: // Android TV Box
			return 26537;
		case 9119: // Bút trình chiếu - 9119, thuộc tính label hình thumb - 26538
			return 26538;
		case 7922: // Quạt mini - 7922, thuộc tính label hình thumb - 26539
			return 26539;
		case 7924: // Đế, móc điện thoại - 7924, thuộc tính label hình thumb - 26540
			return 26540;
		case 7925: // Túi đựng AirPods - 7925, thuộc tính label hình thumb - 26541
			return 26541;
		case 6862: // Giá đỡ laptop, điện thoại - 6862, thuộc tính label hình thumb - 26542
			return 26542;
		case 56: // Pin tiểu, pin điện thoại - 56, thuộc tính label hình thumb - 26543
			return 26543;
		case 6859: // Túi chống nước - 6859, thuộc tính label hình thumb - 26544
			return 26544;
		case 3885: // Gậy tự sướng - 3885, thuộc tính label hình thumb - 26545
			return 26545;
		case 6858: // Miếng lót bàn phím - 6858, thuộc tính label hình thumb - 26546
			return 26546;
		case 1882: // Phụ kiện iPad - 1882, thuộc tính label hình thumb - 26547
			return 26547;
		default:
			return 0;
		}
	}

	public int getAnnouncedYearPropByCateId(int categoryID) {
		switch (categoryID) {
		case 1942:
			return 6989;
		case 1943:
			return 6588;
		case 1944:
			return 7042;
		case 2162:
			return 9087;
		case 2202:
			return 8645;
		case 1962:
			return 16696;
		case 166:
			return 20888;
		case 2002:
			return 6990;
		default:
			return -1;

		}
	}

	public ProductBO[] GetSimpleProductListByListID_PriceStrings_soMap(int[] ids, int siteID, int provinceID,
			String langID) throws Throwable {
		return GetSimpleProductListByListID_PriceStrings_soMap(ids, siteID, provinceID, 0, langID, null);
	}

	public ProductBO[] GetSimpleProductByID(int id, int siteID, int provinceID) throws Throwable {
		return factoryRead.QueryFunction("product_GetByIDSimple", ProductBO[].class, false, id, siteID, provinceID);
	}

	public ProductBO[] GetFeatureLaptophtml_info_GetByHtmlID(int[] id, int siteID, int provinceID) throws Throwable {
		return factoryRead.QueryFunction("product_GetByIDSimple", ProductBO[].class, false, id, siteID, provinceID);
	}

//	public void Ela_SearchProduct(ProductQuery qry, ProductIDSR pidsr) {
//		if (pidsr == null)
//			return;
//		pidsr.rowCount = 2;
//		pidsr.faceListCategory = new FaceCategorySR[] { new FaceCategorySR() {
//			{
//				categoryID = 42;
//				productCount = 2;
//				score = 150;
//				hasProductInStock = true;
//			}
//		} };
//		pidsr.faceListManu = new FaceManuSR[] { new FaceManuSR() {
//			{
//				manufacturerID = 2;
//				productCount = 1;
//			}
//		}, new FaceManuSR() {
//			{
//				manufacturerID = 80;
//				productCount = 1;
//			}
//		} };
//		pidsr.productList = new int[] { 196963, 114115 };
//	}

	public Map<Integer, ProductSO> GetProductSOByListID(int[] ids, int provinceID, int siteID) throws Throwable {
		var sb = new SearchSourceBuilder();
		sb.fetchSource(new String[] { "ProductID", "Prices.WebStatusId_" + provinceID,
				"Prices.IsShowHome_" + provinceID, "Prices.Price_" + provinceID, "Prices.ProductCode_" + provinceID },
				null);

		sb.query(boolQuery().must(termsQuery("ProductID", ids)).must(termQuery("SiteID", siteID)));
		var searchRequest = new SearchRequest(CurrentIndexDB);
		searchRequest.source(sb);
		Map<Integer, ProductSO> somap = new LinkedHashMap<Integer, ProductSO>();

		SearchResponse queryResults = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		queryResults.getHits().forEach(h -> {
			try {
				var so = mapper.readValue(h.getSourceAsString(), ProductSO.class);
				somap.put(so.ProductID, so);
			} catch (Exception e) {
				Logs.LogException(e);
			}
		});
		return somap;
	}

//	public List<Integer> GetAllProductIDbySIte(int SiteId, String LanguageID) throws Exception {
//
//		SearchSourceBuilder sb = new SearchSourceBuilder();
//		sb.fetchSource(new String[] { "ProductID" }, null);
//
//		var query = boolQuery();
//
//		if (SiteId > 0)
//			query.must(termQuery("SiteID", SiteId));
//		if (LanguageID != null && !LanguageID.isBlank())
//			query.must(termQuery("Lang", DidxHelper.GenTerm3(LanguageID)));
//
//		query.must(termQuery("ProductType", 1));
//		query.must(termQuery("IsRepresentProduct", 0));
//		query.must(termQuery("IsReferAccessory", 0));
//		query.must(boolQuery().mustNot(termsQuery("CategoryID", new String[] { "1784", "1783" })));
//		// query.must(termQuery("HasBimage", 1));
//
//		sb.from(0).size(10000);
//		SearchResponse queryResults = null;
//		SearchRequest searchRequest = new SearchRequest(CurrentIndexDB);// .types(CurrentTypeDB);
////		String rsl = "";
//		try {
//
//			var idlist = new ArrayList<Integer>();
//			searchRequest.source(sb.query(query));
//			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
//			queryResults.getHits().forEach(h -> {
//				try {
//					var so = mapper.readValue(h.getSourceAsString(), ProductSO.class);
//					idlist.add(so.ProductID);
//				} catch (Exception e) {
//					// ignored
//					Logs.LogException(e);
//				}
//			});
//
//			return idlist;
//		} catch (Exception e) {
//
//			Logs.LogException(e);
//			throw e;
//		}
//		// return null;
//	}

//	public List<Integer> GetAllProductIDbySIteScan(int SiteId, String LanguageID) throws Exception {
//
//		SearchSourceBuilder sb = new SearchSourceBuilder();
//		sb.fetchSource(new String[] { "ProductID" }, null);
//
//		var query = boolQuery();
//
//		if (SiteId > 0)
//			query.must(termQuery("SiteID", SiteId));
//		if (LanguageID != null && !LanguageID.isBlank())
//			query.must(termQuery("Lang", DidxHelper.GenTerm3(LanguageID)));
//
//		query.must(termQuery("ProductType", 1));
//		query.must(termQuery("IsRepresentProduct", 0));
//		query.must(termQuery("IsReferAccessory", 0));
//		query.must(boolQuery().mustNot(termsQuery("CategoryID", new String[] { "1784", "1783", "8232", "8233" })));
//		// query.must(termQuery("HasBimage", 1));
//
//		var idlist = new ArrayList<Integer>();
//
//		final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
//		SearchRequest searchRequest = new SearchRequest(CurrentIndexDB);
//		searchRequest.scroll(scroll);
//		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//		searchSourceBuilder.query(query);
//		searchRequest.source(searchSourceBuilder);
//		searchSourceBuilder.fetchSource(new String[] { "ProductID" }, null);
//		searchSourceBuilder.size(5000);
//		SearchResponse searchResponse = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
//		String scrollId = searchResponse.getScrollId();
//		org.elasticsearch.search.SearchHit[] searchHits = searchResponse.getHits().getHits();
//
//		while (searchHits != null && searchHits.length > 0) {
//
//			SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
//			scrollRequest.scroll(scroll);
//			searchResponse = clientIndex.scroll(scrollRequest, RequestOptions.DEFAULT);
//			scrollId = searchResponse.getScrollId();
//			searchHits = searchResponse.getHits().getHits();
//			searchResponse.getHits().forEach(h -> {
//				try {
//					var so = mapper.readValue(h.getSourceAsString(), ProductSO.class);
//					idlist.add(so.ProductID);
//				} catch (Exception e) {
//					// ignored
//					Logs.LogException(e);
//				}
//			});
//		}
//
//		ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
//		clearScrollRequest.addScrollId(scrollId);
////		ClearScrollResponse clearScrollResponse = clientIndex.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
////		boolean succeeded = clearScrollResponse.isSucceeded();
//
//		return idlist;
//
//	}

	public Map<Integer, ProductSO> GetFeatureAccessory(int countPerCate, int provinceID) throws IOException {
		SearchSourceBuilder sb = new SearchSourceBuilder();
		sb.fetchSource(new String[] { "ProductID", "Prices.WebStatusId_" + provinceID,
				"Prices.IsShowHome_" + provinceID, "Prices.Price_" + provinceID, "Prices.ProductCode_" + provinceID },
				null);
		var query = boolQuery();

		query.must(termQuery("ProductType", 1)).must(termQuery("IsCollection", 0))
				.must(termsQuery("CategoryID", new int[] { 57, 58, 54, 382, 55, 1363, 75, 2429, 3885 }))
				.must(termQuery("Prices.WebStatusId_" + provinceID, 4))
				.must(termQuery("Prices.IsShowHome_" + provinceID, 1))
				.must(rangeQuery("Prices.Price402_" + provinceID).gte(1)).must(termQuery("HasBimage", 1));

		sb.from(0).size(0).query(query)
				.aggregation(terms("categoryID").field("CategoryID").size(100)
						.subAggregation(topHits("topCategoryHits")
								.fetchSource(new String[] { "ProductID", "Prices.WebStatusId_" + provinceID,
										"Prices.IsShowHome_" + provinceID, "Prices.Price_" + provinceID,
										"Prices.ProductCode_" + provinceID }, null)
								.size(countPerCate).sort("ProductSoldCount", SortOrder.DESC)
								.sort("Prices.Price_" + provinceID, SortOrder.ASC)
//								.sort(scriptSort(new Script("doc['Prices.Price_" + provinceID
//										+ "'].value/doc['Prices.Price_402" + provinceID + "'].value"),
//										ScriptSortType.NUMBER).order(SortOrder.DESC))
						));

		SearchResponse queryResults = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			queryResults = clientIndex.search(new SearchRequest(CurrentIndexDB).source(sb), RequestOptions.DEFAULT);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		var aggrs = queryResults.getAggregations();
		ParsedLongTerms bucket = (ParsedLongTerms) aggrs.get("categoryID");
		if (bucket == null || bucket.getBuckets().size() == 0)
			return null;
		Map<Integer, ProductSO> somap = new LinkedHashMap<Integer, ProductSO>();
		bucket.getBuckets().stream().map(v -> (TopHits) v.getAggregations().get("topCategoryHits"))
				// .collect(Collectors.toList())
				.forEach(h -> h.getHits().forEach(v -> {
					try {
						var so = mapper.readValue(v.getSourceAsString(), ProductSO.class);
						somap.put(so.ProductID, so);
					} catch (IOException e) {
						Logs.LogException(e);
					}
				}));
		return somap;
	}

	public AggregationBuilder addAggregation() {
		return AggregationBuilders.nested("FacetPropIdList", "listproperty")
				.subAggregation(AggregationBuilders.terms("name").field("listproperty.propkey"));
	}

	public ProductSOSR Ela_SearchProduct(ProductQuery qry, boolean isGetFacetManu, boolean isGetFacetCate,
			boolean isGetFacetProp, CodeTimers timer) throws Throwable {
		return getHelperBySite(qry.SiteId).SearchProduct(qry, isGetFacetManu, isGetFacetCate, isGetFacetProp, timer);

	}

	public ProductDefaultPriceBO[] getDefaultPriceByListID(int[] productID, int siteID, int provinceID, String langID)
			throws Throwable {
		return factoryRead.QueryFunction("product_GetDefaultPriceByListID", ProductDefaultPriceBO[].class, false,
				productID, siteID, provinceID, langID);
	}

	public ProductDefaultPriceBO getDefaultPriceByID(int productID, int siteID, int provinceID, String langID)
			throws Throwable {
		return factoryRead.QueryFunction("product_GetDefaultPriceByListID", ProductDefaultPriceBO[].class, false,
				siteID + "_" + langID + "_" + productID + "_" + provinceID)[0];
	}

//	public List<Integer> getAccessoryCategory() {
//		return Arrays.asList(AccessoryCategory);
//
//	}

	// public void GetDefaultPrice(ProductBO product, ProductSO so, int ProvinceID,
	// boolean isAccessory) {
//		var pricelist = product.ProductErpPriceBOList;
//		ProductErpPriceBO price402 = null, pricebo = null;
//		if (pricelist != null && pricelist.length > 0) {
//			for (var price : pricelist)
//				if (price.PriceArea == 402)
//					price402 = price.clone();
//				else if (price.PriceArea == 13)
//					pricebo = price.clone();
//			if (pricebo == null)
//				pricebo = pricelist[0].clone();
//		} else
//			pricebo = new ProductErpPriceBO();
//		if (so != null && so.Prices != null) {
//			pricebo.Price = !isAccessory || price402 == null ? (double) so.Prices.get("Price_" + ProvinceID)
//					: price402.Price;
//			pricebo.WebStatusId = (int) so.Prices.get("WebStatusId_" + ProvinceID);
//			pricebo.IsShowHome = (int) so.Prices.get("IsShowHome_" + ProvinceID) == 1;
//			product.ProductCode = (String) so.Prices.get("ProductCode_" + ProvinceID);
//		}
//		product.ProductErpPriceBO = pricebo;
//	}
	public void GetDefaultPriceOld(int siteID, String LangID, ProductBOApi product, boolean isAccessory,
			boolean clearPriceList) {
		var def = product.ProductErpPriceBO;
		if (def == null)
			return;
		product.Promotion = product.Promotion.stream().filter(x -> x.ProductCode.equals(def.ProductCode))
				.collect(Collectors.toList());
		if (product.ProductErpPriceBOList == null)
			return;

		if (isAccessory && siteID == 12) {

			int priceArea = 402;

			product.ProductErpPriceBOList = Arrays.stream(product.ProductErpPriceBOList)
					.filter(x -> x.ProductCode.equals(def.ProductCode) && x.PriceArea == priceArea)
					.toArray(ProductErpPriceBOApi[]::new);
			if (product.ProductErpPriceBOList.length == 0)
				return;
			var current = product.ProductErpPriceBOList[0].clone();
			if (current == null)
				return;
			current.Price = def.Price402 == 0 ? def.Price : def.Price402; // 402
			current.PriceOrg = def.Price;// 13
			current.IsShowHome = def.IsShowHome;
			current.WebStatusId = def.WebStatusId;
			product.ProductCode = def.ProductCode;
			product.ProductErpPriceBO = current;

		} else {

			int priceArea = 13;
			product.ProductErpPriceBOList = Arrays.stream(product.ProductErpPriceBOList)
					.filter(x -> x.ProductCode.equals(def.ProductCode) && x.PriceArea == priceArea)
					.toArray(ProductErpPriceBOApi[]::new);
			if (product.ProductErpPriceBOList.length == 0)
				return;
			var current = product.ProductErpPriceBOList[0].clone();
			if (current == null)
				return;
			current.Price = def.Price;
			current.IsShowHome = def.IsShowHome;
			current.WebStatusId = def.WebStatusId;
			current.RunVersion = "BETA";
			product.ProductCode = def.ProductCode;
			product.ProductErpPriceBO = current;
		}
		if (clearPriceList)
			product.ProductErpPriceBOList = null;
	}

	public void GetDefaultPriceAndPromotion(int siteID, String LangID, ProductBO product, boolean isAccessory,
			boolean clearPriceList, Promotion[] promoGroup) throws Throwable {
		if (product instanceof ProductBOApi)
			getDefaultPriceLive(siteID, LangID, (ProductBOApi) product, isAccessory, clearPriceList);
		else // TGDD//DMX
			getDefaultPriceAndPromotion(siteID, LangID, product, isAccessory, clearPriceList, promoGroup);// cac site 1
		// 2 11
	}

	public void getDefaultPriceAndPromotion(int siteID, String LangID, ProductBO product, boolean isAccessory,
			boolean clearPriceList, Promotion[] promoGroup) {
		var def = product.ProductErpPriceBO;
		if (def == null)
			return;
		if (product.ProductErpPriceBO != null && product.ProductErpPriceBO.WebStatusId != 9
				&& product.ProductLanguageBO != null && product.ProductLanguageBO.CategoryID != 2182) {
			product.ProductErpPriceBO = null;
		}
		// product.ProductErpPriceBO = null; ??????????????

		if (product.ProductErpPriceBOList == null)
			return;
		String productCode = product.ProductCode;
		if(Utils.StringIsEmpty(productCode)) { // trường hợp không truyền productCode => lấy theo productcode mặc định , có truyền thì bỏ qua
			int priceArea = DidxHelper.getDefaultPriceAreaBySiteID(siteID, LangID);
			product.ProductErpPriceBOList = Arrays.stream(product.ProductErpPriceBOList)
					.filter(x -> (x.ProductCode.equals(def.ProductCode) || def.ProductCode == null)
							&& (x.PriceArea == priceArea || x.PriceArea == 0))
					.toArray(ProductErpPriceBO[]::new);
		}
		if (product.ProductErpPriceBOList.length == 0)
			return;
		var current = product.ProductErpPriceBOList[0].clone(); // đã xử lí nếu truyền productCode , chưa truyền thì lấy default
		if (current == null)
			return;
		if (def.Price > 0) {
			current.Price = def.Price;
		}
		current.PriceOrg = def.PriceOrg;
		current.IsShowHome = def.IsShowHome;
		current.WebStatusId = def.WebStatusId;
		current.WebStatusIdNew = def.WebStatusIdNew;
		current.WebStatusIdOld = def.WebStatusIdOld;

		product.ProductCode = Utils.StringIsEmpty(productCode) ? def.ProductCode : productCode; // ko có productCode => lấy productCode default
		current.DeliveryType = current.DeliveryVehicles;
		current.ArrivalDate = current.ProductArrivalDate;
		current.CategoryId = product.CategoryID;

		current.DisplayOrder = def.DisplayOrder;
		current.Mimage = def.Mimage;
		current.Simage = def.Simage;
		current.Bimage = def.Bimage;
		current.Image = def.Image;

		// Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		long nowstamp = System.currentTimeMillis() / 1000;// timestamp.getTime() / 1000;
		// https://timestamp.online/
		// 2/11/2021, 00:00:00 AM = 1612976400
		// 2/15/2021, 00:00:00 AM = 1613322000
		if (nowstamp >= 1612976400 && nowstamp <= 1613322000) {
			current.OnlineAreaSalePrice = 0;
		} else {
			if (current.OnlineAreaSalePrice >= current.Price) {
				current.OnlineAreaSalePrice = 0;
			}
		}

//		if (current.StandardKit != null)
//			current.StandardKit = current.StandardKit.trim();

		if (product.pmProductBOList != null) {
			var code = Stream.of(product.pmProductBOList).filter(x -> x.productID.trim().equals(current.ProductCode))
					.findFirst().orElse(null);
			if (code != null) {
				if (code.accessoriesincludes != null) {
					current.StandardKit = code.accessoriesincludes.trim();
				}
				product.MainGroupID = code.mainGroupID;
				product.SubGroupID = code.subGroupID;
				product.isSetupProduct = code.isSetupProduct;
				product.IsRequestImei = code.isRequestImei;
				product.erpBrandID = code.brandID;
			}
		}

		// vanhanh 24/11/2020
		if (product.ProductLanguageBO != null) {
			product.Bimage = Utils.StringIsEmpty(product.Bimage) ? product.ProductLanguageBO.bimage : product.Bimage;
			product.Simage = Utils.StringIsEmpty(product.Simage) ? product.ProductLanguageBO.simage : product.Simage;
			product.Mimage = Utils.StringIsEmpty(product.Mimage) ? product.ProductLanguageBO.mimage : product.Mimage;
		}

		product.ProductErpPriceBO = current;
		if (product.ProductLanguageBO == null)
			product.ProductLanguageBO = new ProductLanguageBO();

		// vanhanh
		if (product.ProductErpPriceBO != null) {
			if (!Utils.StringIsEmpty(product.ProductErpPriceBO.Image)
					&& !product.ProductErpPriceBO.Image.equals("null")) {

				// GenProductImageUrl(product.CategoryID, product.ProductID,
				// product.ProductErpPriceBO.Bimage)
				var bimage = !Utils.StringIsEmpty(product.ProductErpPriceBO.Bimage) ? product.ProductErpPriceBO.Bimage
						: product.Bimage + "";
				var mimage = !Utils.StringIsEmpty(product.ProductErpPriceBO.Bimage) ? product.ProductErpPriceBO.Bimage
						: product.Mimage + "";
				var simage = !Utils.StringIsEmpty(product.ProductErpPriceBO.Simage) ? product.ProductErpPriceBO.Simage
						: product.Simage + "";

				product.Bimage = bimage;
				product.BimageUrl = GenProductImageUrl(product.CategoryID, product.ProductID, bimage);
				product.ProductLanguageBO.bimage = bimage;
				product.ProductLanguageBO.bimageurl = product.BimageUrl;// GenProductImageUrl(product.CategoryID,
				// product.ProductID, bimage);

				product.Mimage = mimage;
				product.MimageUrl = GenProductImageUrl(product.CategoryID, product.ProductID, mimage);
				product.ProductLanguageBO.mimage = mimage;
				product.ProductLanguageBO.mimageurl = product.MimageUrl;// GenProductImageUrl(product.CategoryID,
				// product.ProductID, mimage);

				product.Simage = simage;
				product.SimageUrl = GenProductImageUrl(product.CategoryID, product.ProductID, simage);
				product.ProductLanguageBO.simage = simage;
				product.ProductLanguageBO.simageurl = product.SimageUrl;// GenProductImageUrl(product.CategoryID,
				// product.ProductID, simage);

			}
		}
		if (product.Promotion == null) {
			product.Promotion = new ArrayList<>();
		}

		// lay km subbrand
		if (promoGroup != null) {
			product.Promotion.addAll(Stream.of(promoGroup).filter(x -> Strings.isNullOrEmpty(x.EXCLUDEPRODUCTIDLIST)
					|| !Stream.of(x.EXCLUDEPRODUCTIDLIST.split("\\|")).anyMatch(y -> product.ProductCode.equals(y)))
					.map(y -> {
						y.ProductCode = product.ProductCode;
						return y;
					}).collect(Collectors.toList()));
		}

		List<Integer> excludes = null;

		if (product.getSubBrandPromotion) {
			excludes = Stream.of(product.Promotion.stream().map(x -> x.EXCLUDEPROMOTIONID).filter(x -> x != null)
					.collect(Collectors.joining("|")).split("\\|")).map(x -> {
						try {
							return Integer.parseInt(x);
						} catch (NumberFormatException e) {
							return -1;
						}
					}).filter(x -> x > 0).collect(Collectors.toList());
		}
 
		Date now = new Date();
		var fExcludes = excludes;
		double price = product.ProductErpPriceBO == null ? 0 : product.ProductErpPriceBO.Price;
		if(Utils.StringIsEmpty(productCode)) {
			product.Promotion = product.Promotion.stream()
					.filter(x -> x.ProductCode != null && x.ProductCode.equals(def.ProductCode) && x.BeginDate != null
							&& x.BeginDate.before(now) && x.EndDate != null && x.EndDate.after(now)
							&& (fExcludes == null || !fExcludes.contains(x.PromotionID))
							&& x.FromPrice <= price && (x.ToPrice >= price || x.ToPrice == 0))
					.collect(Collectors.toList());
		}
		// sau tat ca, neu gia = 0 thì loại hết promotion
		if (product.ProductErpPriceBO != null && product.ProductErpPriceBO.Price <= 0) {
			product.Promotion = new ArrayList<Promotion>();
		}
		getHelperBySite(siteID).getPromotions(product, def, siteID);
		if (clearPriceList) {
			product.ProductErpPriceBOList = null;
		}
	}

	public void getDefaultPriceLive(int siteID, String LangID, ProductBOApi product, boolean isAccessory,
			boolean clearPriceList) throws Throwable {
		var def = product.ProductErpPriceBO;
		if (def == null)
			return;
		String productCode = product.ProductCode;
		if(Utils.StringIsEmpty(productCode)) {// k truyền productCode => lấy productCode default
			product.Promotion = product.Promotion.stream().filter(x -> x.ProductCode.equals(def.ProductCode))
					.collect(Collectors.toList());
		}
		getHelperBySite(siteID).getPromotions(product, def, siteID);

		if (product.ProductErpPriceBOList == null || product.ProductErpPriceBOList.length == 0) {
			product.ProductErpPriceBO = null;
			return;
		}
        if(Utils.StringIsEmpty(productCode)) { // không truyền productCode
			int priceArea = DidxHelper.getDefaultPriceAreaBySiteID(siteID, LangID);
			product.ProductErpPriceBOList = Arrays.stream(product.ProductErpPriceBOList)
					.filter(x -> x.ProductCode.equals(def.ProductCode) && x.PriceArea == priceArea)
					.toArray(ProductErpPriceBOApi[]::new);
		}
		if (product.ProductErpPriceBOList.length == 0)
			return;
		var current = product.ProductErpPriceBOList[0].clone();
		if (current == null)
			return;
		current.Price = def.Price;
		current.PriceOrg = def.PriceOrg;
		current.IsShowHome = def.IsShowHome;
		current.WebStatusId = def.WebStatusId;

		product.ProductCode = Utils.StringIsEmpty(productCode) ? def.ProductCode : productCode;
		product.ProductErpPriceBO = current;

		if (clearPriceList)
			product.ProductErpPriceBOList = null;
	}

	public void GetDefaultPriceAndPromotionBHX(String LangID, ProductBO product, boolean clearPriceList, int storeID,
			int provinceID) throws Throwable {
		if (product.ProductLanguageBO != null && !Utils.StringIsEmpty(product.ProductLanguageBO.comboproductidlist)) {
			processComboBhx(product, provinceID, storeID);
		}

		if (product.ProductErpPriceBOList == null || product.ProductErpPriceBOList.length == 0)
			return;

		ProductErpPriceBO def = new ProductErpPriceBO();
		if (storeID > 0) {
			// -1 là giá của combo
			var option = Arrays.stream(product.ProductErpPriceBOList).filter(x -> x.StoreID == storeID)
					.map(Optional::ofNullable).findFirst().orElse(null);
			if (option != null && !option.isEmpty())
				def = option.get();
		} else {
			// lay giá thấp nhất ngoại trừ kho cận date
			var lsext = Arrays.stream(bhxStoreHelper.getListNormalStore()).boxed().collect(Collectors.toList());
			def = Arrays.stream(product.ProductErpPriceBOList).filter(y -> lsext.contains(y.StoreID))
					.max(Comparator.comparing(ProductErpPriceBO::getPrice)).get();
		}
		if (def == null || def.ProductId == 0)
			return;
//		try {
//			if (product.PromotionBHX != null) {
//				var code = def.ProductCode;
//				product.PromotionBHX = product.Promotion.stream().filter(x -> x.ProductCode.equals(code))
//						.collect(Collectors.toList());
//
//				// xu ly nghiep vu khuyen mai
//				getHelperBySite(siteID).getPromotions(product, def, siteID);
//			}
//
//		} catch (Exception e) {
//			// TODO: handle exception
//			Logs.LogException(e);
//		}

		if (product.ProductErpPriceBOList == null)
			return;
		if (product.ProductErpPriceBOList.length == 0)
			return;
		var current = def.clone();
		if (current == null)
			return;

		// lấy tồn kho bảo vào object này luôn
		var quantity = redisCluster
				.Get(redisCluster.createKeyRedisStock(current.ProductCode, current.ProvinceId, current.StoreID));
//		var quantity = "10";
		if (Utils.StringIsEmpty(quantity)) {
			current.Quantity = 0;
			current.QuantityNew = 0;
		} else {
			current.QuantityNew = Utils.toFloat(quantity);
			current.Quantity = (int) current.QuantityNew;
		}

//		if (listStock == null || listStock.isEmpty()) {
//			current.Quantity = 0;
//			// current = priceHelper.ProcessProductStatus(current, false);
//		} else {
//			current.Quantity = listStock.get(0).quantity;
//		}

		current = BhxPriceHelper.ProcessProductStatus(current, false);

		product.ProductCode = def.ProductCode;
		product.ProductErpPriceBO = current;

		if (clearPriceList)
			product.ProductErpPriceBOList = null;

		var tmpstoreID = current.StoreID;
		// promotion
		// lấy promtion của kho hiện tại và kho cận date theo cặp
		var expStore = bhxStoreHelper.getStoreDetailByStore(tmpstoreID);
		if (expStore != null) {
			if (product.PromotionBHX != null && !product.PromotionBHX.isEmpty()) {
				var tmppromo = product.PromotionBHX.stream().filter(x -> x.BHXPromotionType != BHXPromotionType.COMBO
						&& (x.StoreIds.equals(Utils.toString(tmpstoreID)) || x.StoreIds.equals(expStore.getExpStore())))
						.collect(Collectors.toList());
				product.PromotionBHX = tmppromo;
			}
		}

		// lọc lại chỉ lấy giá chỉ cho kho cận date
		if (expStore != null) {
			product.ProductErpPriceBOList = Arrays.stream(product.ProductErpPriceBOList)
					.filter(x -> x.StoreID == Utils.toInt(expStore.getExpStore())).toArray(ProductErpPriceBO[]::new);
		} else {
			product.ProductErpPriceBOList = null;
		}
	}

	public void GetDefaultPriceCu(ProductBO product, boolean isAccessory, boolean clearPriceList) {
		var def = product.ProductErpPriceBO;
		if (def == null)
			return;
		product.Promotion = product.Promotion.stream().filter(x -> x.ProductCode.equals(def.ProductCode))
				.collect(Collectors.toList());
		if (product.ProductErpPriceBOList == null)
			return;
		boolean get402 = isAccessory && def.SiteID == 12;
		int priceArea = get402 ? 402
				: DidxHelper.isBeta() ? DidxHelper.getDefaultPriceAreaBySiteID(def.SiteID, def.LangID) : 13;
		product.ProductErpPriceBOList = Arrays.stream(product.ProductErpPriceBOList)
				.filter(x -> x.ProductCode.equals(def.ProductCode) && x.PriceArea == priceArea)
				.toArray(ProductErpPriceBO[]::new);
		if (product.ProductErpPriceBOList.length == 0)
			return;
		var current = product.ProductErpPriceBOList[0].clone();
		if (current == null)
			return;
		current.Price = !get402 || def.Price402 == 0 ? def.Price : def.Price402;
		current.IsShowHome = def.IsShowHome;
		current.WebStatusId = def.WebStatusId;
		current.PriceOrg = get402 ? def.Price : def.PriceOrg;
		product.ProductCode = def.ProductCode;
		product.ProductErpPriceBO = current;
		if (clearPriceList)
			product.ProductErpPriceBOList = null;
	}

//	public void GetDefaultPriceForList(ProductBO product, boolean isAccessory, boolean clearPriceList) {
//		var def = product.ProductErpPriceBO;
//		if (def == null)
//			return;
//		if (product.Promotion != null) {
//			product.Promotion = product.Promotion.stream().filter(x -> x.ProductCode.equals(def.ProductCode))
//					.collect(Collectors.toList());
//		}
//	}

//    Mã nhóm hàng WEB :
//    Màn hình, máy tính để bàn : 5697
//    Máy tính nguyên bộ : 5698
//    Máy in, Fax : 5693
//    Mực in : 1262
//		Thay đổi 20201119: Sử dụng hàm 364 đối với các categoryid trên

	private static List<Integer> cate364 = Arrays.asList(1262, 5693, 5697, 5698);

	public int[] SearchStoreByProductID(int productID, int brandID, int provinceID, int districtID,
			BooleanWrapper it364) throws Throwable {
		int categoryID = categoryHelper.getCategoryIDByProductIDFromCache(productID);
		it364.value = cate364.contains(categoryID);
		String funcname = it364.value ? "product_SearchStoreByProductID364" : "product_SearchStoreByProductID";
		var wrap = factoryRead.QueryFunction(funcname, StoreIDList[].class, false, productID, brandID, provinceID,
				districtID);
		return wrap == null || wrap.length <= 0 ? new int[0] : wrap[0].storeidlist;
	}

	public int[] SearchStoreByProductCode(String productCode, int brandID, int provinceID, int districtID,
			BooleanWrapper it364) throws Throwable {
		int categoryID = categoryHelper.getCategoryIDFromCache(productCode);
		it364.value = cate364.contains(categoryID);
		String funcname = it364.value ? "product_SearchStoreByProductCode364" : "product_SearchStoreByProductCode";
		var wrap = factoryRead.QueryFunctionCachedTime(5, funcname, StoreIDList[].class, false, productCode, brandID,
				provinceID, districtID);
		return wrap == null || wrap.length <= 0 ? new int[0] : wrap[0].storeidlist;
	}

	public StoreBO[] SearchStoreStockByProductCode(String productCode, int brandID, int provinceID, int districtID,
			BooleanWrapper it364) throws Throwable {
		int categoryID = categoryHelper.getCategoryIDFromCache(productCode);
		it364.value = cate364.contains(categoryID);
		String funcname = "product_SearchStoreStockByProductCode";

		var wrap = factoryRead.QueryFunctionCachedTime(5, funcname, StoreBO[].class, false, productCode, provinceID,
				districtID, brandID, it364.value ? 1 : 0);
		return wrap;
	}

	public StoreBO[] SearchStoreStock_ReplacedProductid(String productCode) throws Throwable {
		if (DidxHelper.isBeta()) {
			String funcname = "product_getReplacedProductid";
			var wrap = factoryRead.QueryFunctionCachedTime(5, funcname, StoreBO[].class, false, productCode);
			return wrap;
		}
		return null;
	}

	public int[] searchStoreByProvinceList(String productCode, int brandID, int[] provinceIDs, BooleanWrapper it364)
			throws Throwable {
		int categoryID = categoryHelper.getCategoryID(productCode);
		it364.value = cate364.contains(categoryID);
		String funcname = it364.value ? "product_SearchStoreByProductCode364Provs"
				: "product_SearchStoreByProductCodeProvs";
		var wrap = factoryRead.QueryFunctionCached(funcname, StoreIDList[].class, false, productCode, brandID,
				provinceIDs);
		return wrap == null || wrap.length <= 0 ? new int[0] : wrap[0].storeidlist;
	}

	public StoreBO[] getStoreByIDList(int[] ids) throws Throwable {
		return factoryRead.QueryFunctionCached("product_GetStoreByListID", StoreBO[].class, false, ids);
	}

	public StockBO[] getStockQuantity(String[] productCodes, int[] storeIDs) throws Throwable {
		return factoryRead.QueryFunction("product_SearchStoreQuantityByProductCode", StockBO[].class, false,
				productCodes, storeIDs);
	}

	public InStockBO getStockQuantityBhx(int productId, int storeid, String pcode) throws Throwable {
		var code = pcode;
		if (Utils.StringIsEmpty(code))
			code = GetCodeByProductID(productId);
		if (code == null || code.equals("")) {
			throw new Throwable("Không tìm thấy sản phẩm");
		}
		int[] tmp = new int[1];
		tmp[0] = storeid;
		var listStock = priceHelper.GetStockByProductCodeBHX(code, tmp);
		if (listStock != null && !listStock.isEmpty()) {
			var result = listStock.get(0);
//			// TODO: set gia trị min stock từ config
//			if (result.quantity >= 2) {
//				return result;
//			}
			// lấy tồn từ crm
			var stock = crmHelper.GetCurrentInStocksBHXOnlByStore(code, storeid);
			if (stock != null) {
//				var t = stock;
				result.quantity = (int) stock.CurrentQuantity;
				result.quantitynew = (float) stock.CurrentQuantity;
				result.ExpiredDate = stock.ExpiredDate;
				result.ExpireddateInStore = stock.ExpireddateInStore;
				result.ExpireQuantity = stock.ExpireQuantity;
				result.quantityUnit = stock.QuantityUnit;
			}
			return result;
		}
		// get rỗng thì tạo object và get từ crm
		else {
			InStockBO newstock = new InStockBO();
			newstock.quantity = 0;
			newstock.productcode = code;
			var storeidlist = new ArrayList<Integer>();
			storeidlist.add(storeid);
			newstock.storeidlist = storeidlist;
			var stock = crmHelper.GetCurrentInStocksBHXOnlByStore(code, storeid);
			if (stock != null) {
				newstock.quantity = (int) stock.CurrentQuantity;
				newstock.quantitynew = (float) stock.CurrentQuantity;
				newstock.ExpiredDate = stock.ExpiredDate;
				newstock.ExpireddateInStore = stock.ExpireddateInStore;
				newstock.ExpireQuantity = stock.ExpireQuantity;
				newstock.quantityUnit = stock.QuantityUnit;
			}

			return newstock;
		}
		// return null;
	}

	private static boolean contains(int[] array, int v) {
		for (int i : array) {
			if (i == v)
				return true;
		}
		return false;
	}

	public String getUnitQuantity(String productcode, int storeid) throws Exception {
		var stock = crmHelper.GetCurrentInStocksBHXOnlByStore(productcode, storeid);
		return stock.QuantityUnit;
	}

	public List<InStockBO> getNewListStockQuantityBhx(ProductItemExchange itemExchange, int[] storeids,
			HashMap<Integer, Integer> listStockBase) throws Throwable {
//		List<Integer> boxedStoreIds = Arrays.stream(storeids).boxed().collect(Collectors.toList());
		String exchangeProductCode = itemExchange.productcode;
		String baseProductCode = itemExchange.unitproductcode;
		Map<Integer, InStockBO> result = new HashMap<>();

		var listStock = priceHelper.GetNewStockByProductCodeBHX(exchangeProductCode, storeids);

		if (Objects.isNull(listStock) || listStock.isEmpty()) {
			var crmStock = crmHelper.GetListCurrentInStocksBHXOnl(exchangeProductCode);

			Map<Integer, ProductInventory> mapStock = Arrays.stream(crmStock)
					.filter(inventory -> contains(storeids, inventory.StoreID))
//				.filter(inventory -> boxedStoreIds.contains(inventory.StoreID))
					.collect(Collectors.toMap(inventory -> inventory.StoreID, inventory -> inventory));

			result = mapStock.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> {
				ProductInventory pi = entry.getValue();
				InStockBO newstock = new InStockBO();
				newstock.quantity = 0;
				newstock.quantitynew = 0;
				newstock.productcode = exchangeProductCode.trim();
				var storeidlist = new ArrayList<Integer>();
				storeidlist.add(entry.getKey());
				newstock.storeidlist = storeidlist;
				newstock.quantity = pi.CurrentQuantity > 0 ? (int) pi.CurrentQuantity : 0;
				newstock.quantitynew = pi.CurrentQuantity > 0 ? (float) pi.CurrentQuantity : 0;
				newstock.ExpiredDate = pi.ExpiredDate;
				newstock.ExpireddateInStore = pi.ExpireddateInStore;
				newstock.ExpireQuantity = pi.ExpireQuantity;
				newstock.quantityUnit = pi.QuantityUnit;
				if (exchangeProductCode.equals(baseProductCode)) {
					listStockBase.put(entry.getKey(), newstock.quantity);
				}

				return newstock;
			}));

		} else {
			for (int storeid : storeids) {
				System.out.println(storeid);
				var lstStock = priceHelper.GetStockByProductCodeBHX(exchangeProductCode, new int[] { storeid });
				if (lstStock != null && !lstStock.isEmpty()) {
					var temp = listStock.get(0);

					var stock = crmHelper.GetCurrentInStocksBHXOnlByStore(exchangeProductCode, storeid);
					if (stock != null) {
						temp.quantity = stock.CurrentQuantity > 0 ? (int) stock.CurrentQuantity : 0;
						temp.quantitynew = stock.CurrentQuantity > 0 ? (float) stock.CurrentQuantity : 0;
						temp.ExpiredDate = stock.ExpiredDate;
						temp.ExpireddateInStore = stock.ExpireddateInStore;
						temp.ExpireQuantity = stock.ExpireQuantity;
						temp.quantityUnit = stock.QuantityUnit;
					}
					result.put(storeid, temp);
				}
			}
		}
		if (exchangeProductCode != baseProductCode && itemExchange.exchangequantity > 1) {

			for (Map.Entry<Integer, InStockBO> entry : result.entrySet()) {
				InStockBO newstock = entry.getValue();
				var stockbase = listStockBase.get(entry.getKey());
				if (stockbase == null || stockbase < 0) {
					stockbase = 0;
				}
				newstock.quantity = (int) (stockbase / itemExchange.exchangequantity);
				newstock.quantitynew = (stockbase / itemExchange.exchangequantity);

				if (newstock.quantityUnit.equalsIgnoreCase("thùng") && newstock.quantitynew > 0) {
					float m = (stockbase % itemExchange.exchangequantity);
					if (m == 0) {
						newstock.quantity--;
						newstock.quantitynew--;
					}
				}

			}
		}

		List<InStockBO> lstStock = result.entrySet().stream().map(entry -> entry.getValue())
				.collect(Collectors.toList());
		return lstStock;
	}

	public List<InStockBO> getListStockQuantityBhx(int productId) throws Throwable {
		var code = GetCodeByProductID(productId);
		if (code == null || code.equals("")) {
			throw new Throwable("Không tìm thấy sản phẩm");
		}
		var tmp = bhxStoreHelper.getAllStore();
		var listStock = priceHelper.GetStockByProductCodeBHX(code, tmp);
		if ((listStock != null)) {
			var result = listStock;
//			// TODO: set gia trị min stock từ config
//			if (result.quantity >= 2) {
//				return result;
//			}
			// lấy tồn từ crm
			var stock = crmHelper.GetListCurrentInStocksBHXOnl(code);
			if (stock != null && stock.length > 0) {

				// lọc lại với mấy kho ở trên
				var lstStockAvarible = listStock.stream().filter(e -> e.getStoreidlist() != null)
						.map(InStockBO::getStoreidlist)
						// .collect(Collectors.toList())
						// .stream()
						.flatMap(List::stream).collect(Collectors.toList());

				// System.out.print(lstStockAvarible.getClass());
				stock = Stream.of(stock).filter(x -> lstStockAvarible.contains(x.StoreID))
						.toArray(ProductInventory[]::new);

				for (int i = 0; i < stock.length; i++) {
					var t = stock[0];
					result.get(i).quantity = t.CurrentQuantity > 0 ? (int) t.CurrentQuantity : 0;
					result.get(i).quantitynew = t.CurrentQuantity > 0 ? (float) t.CurrentQuantity : 0;
					result.get(i).ExpiredDate = t.ExpiredDate;
					result.get(i).ExpireddateInStore = t.ExpireddateInStore;
					result.get(i).ExpireQuantity = t.ExpireQuantity;
				}
			}
			return result;
		}
		return null;
	}

	public ORThreadLocal getOrientClient() {
		return factoryRead;
	}

	public String[] getManuNames(int[] manuIDs, int siteID, String languageID) throws Throwable {
		var names = factoryRead.QueryFunction("search_GetCateManuName", FaceManuCateList[].class, false, new int[0],
				manuIDs, siteID, languageID)[0];
		return names.manu.stream().map(x -> DidxHelper.GenTermKeyWord(x.manufacturerName)).toArray(String[]::new);
	}

	public void getCateManuName(ProductBOSR sr, int siteID, String langID) throws Throwable {
		if (sr != null && sr.faceListManu != null && sr.faceListCategory != null) {
			var manuids = Stream.of(sr.faceListManu).map(FaceManuSR::getID).collect(Collectors.toList());
			var cateids = Stream.of(sr.faceListCategory).map(FaceCategorySR::getID).collect(Collectors.toList());
			var names = factoryRead.QueryFunction("search_GetCateManuName", FaceManuCateList[].class, false, cateids,
					manuids, siteID, langID)[0];
			if (names != null) {
				if (names.manu != null && names.manu.size() > 0) {
					var manumap = names.manu.stream().filter(x -> x != null && x.manufacturerName != null)
							.collect(Collectors.toMap(FaceManuName::getID, v -> v));
					for (var manu : sr.faceListManu) {
						var vmanu = manumap.get(manu.manufacturerID);
						if (vmanu != null) {
							manu.manufacturerName = vmanu.manufacturerName;
							manu.manufacturerUrl = vmanu.url;
							manu.manufacturerLogo = vmanu.smallLogo;
							manu.displayorder = vmanu.displayorder;
						}
					}
				}
				if (names.cate != null && names.cate.size() > 0) {
					var catemap = names.cate.stream().filter(x -> x != null && x.categoryName != null)
							.collect(Collectors.toMap(FaceCateName::getID, v -> v.categoryName));
					for (var cate : sr.faceListCategory) {
						cate.categoryName = catemap.get(cate.categoryID);
					}
				}

			}

		}
	}

	public void getlistManu(ProductBOSR sr, int siteID, String langID) throws Throwable {
		if (sr != null && sr.faceListManu != null) {
			var manuids = Stream.of(sr.faceListManu).map(FaceManuSR::getID).collect(Collectors.toList());
			var names = factoryRead.QueryFunction("search_GetCateManuName", FaceManuCateList[].class, false, new int[0],
					manuids, siteID, langID)[0];
			if (names != null) {
				if (names.manu != null && names.manu.size() > 0) {
					var manumap = names.manu.stream().filter(x -> x != null && x.manufacturerName != null)
							.collect(Collectors.toMap(FaceManuName::getID, v -> v));
					for (var manu : sr.faceListManu) {
						var vmanu = manumap.get(manu.manufacturerID);
						if (vmanu != null) {
							manu.manufacturerName = vmanu.manufacturerName;
							manu.manufacturerUrl = vmanu.url;
							manu.manufacturerLogo = vmanu.smallLogo;
							manu.displayorder = vmanu.displayorder;
						}
					}
				}
			}

		}
	}

	public void getCateName(ProductBOSR sr, int siteID, String langID) throws Throwable {
		if (sr != null && sr.faceListCategory != null) {
			var cateids = Stream.of(sr.faceListCategory).map(FaceCategorySR::getID).collect(Collectors.toList());
			var names = factoryRead.QueryFunction("search_GetCateManuName", FaceManuCateList[].class, false, cateids,
					new int[] {}, siteID, langID)[0];
			if (names != null) {
				if (names.cate != null && names.cate.size() > 0) {
					var catemap = names.cate.stream().filter(x -> x != null && x.categoryName != null)
							.collect(Collectors.toMap(FaceCateName::getID, v -> v.categoryName));
					for (var cate : sr.faceListCategory) {
						cate.categoryName = catemap.get(cate.categoryID);
					}
				}

			}

		}
	}

	public void getCateManuName(ProductSOSR sr, int siteID, String langID) throws Throwable {
		if (sr != null && sr.faceListManu != null && sr.faceListCategory != null) {
			var manuids = Stream.of(sr.faceListManu).map(FaceManuSR::getID).collect(Collectors.toList());
			var cateids = Stream.of(sr.faceListCategory).map(FaceCategorySR::getID).collect(Collectors.toList());
			var names = factoryRead.QueryFunction("search_GetCateManuName", FaceManuCateList[].class, false, cateids,
					manuids, siteID, langID)[0];
			if (names != null) {
				if (names.manu != null && names.manu.size() > 0) {
					var manumap = names.manu.stream().filter(x -> x != null && x.manufacturerName != null)
							.collect(Collectors.toMap(FaceManuName::getID, v -> v));
					for (var manu : sr.faceListManu) {
						var vmanu = manumap.get(manu.manufacturerID);
						if (vmanu != null) {
							manu.manufacturerName = vmanu.manufacturerName;
							manu.manufacturerUrl = vmanu.url;
							manu.manufacturerLogo = vmanu.smallLogo;
						}
					}
				}
				if (names.cate != null && names.cate.size() > 0) {
					var catemap = names.cate.stream().filter(x -> x != null && x.categoryName != null)
							.collect(Collectors.toMap(FaceCateName::getID, v -> v.categoryName));
					for (var cate : sr.faceListCategory) {
						cate.categoryName = catemap.get(cate.categoryID);
					}
				}

			}

		}
	}

	public ProductBO[] getSamePriceProduct(ProductBO product, int siteID, int provinceID, String langID)
			throws Throwable {
		if (product == null || product.getPrice() == null)
			return new ProductBO[0];
		var q = boolQuery();
		q.mustNot(termQuery("ProductID", product.ProductID));
		q.must(termQuery("CategoryID", product.CategoryID));
		q.must(termsQuery("Prices.WebStatusId_" + provinceID, new int[] { 2, 3, 4, 5, 6, 8, 9 }));
		q.must(termQuery("SiteID", siteID));
		q.must(termQuery("Lang", DidxHelper.GenTerm3(langID)));
		q.must(termQuery("IsCollection", 0));
		var ssb = new SearchSourceBuilder().query(q).from(0).size(5).fetchSource(new String[] { "ProductID" }, null)
				.sort(scriptSort(new Script(
						"Math.abs(" + product.getPrice().Price + " - doc['Prices.Price_" + provinceID + "'].value)"),
						ScriptSortType.NUMBER).order(SortOrder.ASC));
		var sr = new SearchRequest(CurrentIndexDB).source(ssb);
		try {

			SearchResponse qr = null;
//			var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//			var clientIndex1 = elasticClient1.getClient();
//			try {

				qr = clientIndex.search(sr, RequestOptions.DEFAULT);
//			} catch (Throwable e) {
//
//				Logs.LogException(e);
//				throw e;
//			} finally {
//				elasticClient1.close();
//			}

			var ids = Arrays.stream(qr.getHits().getHits()).map(h -> {
				try {
					return mapper.readValue(h.getSourceAsString(), ProductSO.class);
				} catch (Exception e) {
					return null;
				}
			}).filter(p -> p != null).mapToInt(p -> p.ProductID).toArray();
			return GetProductBOByListID(ids, siteID, provinceID, langID);
		} catch (Exception e) {
			Logs.LogException(e);
			throw e;
		}
	}

	public ProductUrl getProductInfoByURL(int siteID, String url, String langID) throws Throwable {
		var recordid = siteID + "_" + DidxHelper.GenTerm3(langID) + "_" + url;
		var list = factoryRead.QueryFunctionCached("producturl_GetByRecordID", ProductUrlUpsert[].class, false,
				recordid);
		if (list == null || list.length == 0)
			return null;
		try {
			var o = mapper.readValue(list[0].json, ProductUrl.class);
			if (o.CID > 0 && !Arrays.stream(getValidCategoryIDs(siteID, langID)).anyMatch(x -> x == o.CID)) {
				return null;
			}
			return o;
		} catch (Exception e) {
			Logs.LogException(e);
			return null;
		}
	}

	public GallerySR getListProductUserGallery(int siteID, int categoryID, int productID, int pictureID,
			int sizeGroupID, int pictureType, int imageType, long userID, int pageIndex, int pageSize, int orderBy,
			OrderType orderType) throws Throwable {
		var result = new GallerySR();
		var q = boolQuery();
		if (pictureID > 0)
			q.must(termQuery("pictureid", pictureID));
		if (productID > 0)
			q.must(termQuery("productid", productID));
		if (categoryID > 0)
			q.must(termQuery("categoryid", categoryID));
		if (sizeGroupID > 0)
			q.must(termQuery("sizegroupid", sizeGroupID));
		if (userID > 0)
			q.must(termQuery("userid", userID));
		if (pictureType <= 0)
			q.must(boolQuery().should(boolQuery().mustNot(termQuery("picturetype", 3)))
					.should(termQuery("isshowhome", 1)));
		else if (pictureType == 3)
			q.must(termQuery("isshowhome", 1));
		if (imageType > 0)
			q.must(termQuery("imagetype", imageType));
		if (siteID > 0)
			q.must(termQuery("siteid", siteID));
		q.must(termQuery("isdeleted", 0));
		q.must(termQuery("isactived", 1));
		var sb = new SearchSourceBuilder();
		sb.fetchSource(
				new String[] { "pictureid", "width", "widthlarge", "widthorg", "height", "heightlarge", "heightorg" },
				null).from(pageIndex * pageSize).size(pageSize).query(q);
		var sr = new SearchRequest(GalleryIndex).source(sb);
		try {

			SearchResponse qr = null;
//			var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//			var clientIndex1 = elasticClient1.getClient();
//			try {

				qr = clientIndex.search(sr, RequestOptions.DEFAULT);

//			} catch (Throwable e) {
//
//				Logs.LogException(e);
//				throw e;
//			} finally {
//				elasticClient1.close();
//			}

			var somap = Stream.of(qr.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), GallerySO.class);
				} catch (IOException e) {
					Logs.LogException(e);
					return null;
				}
			}).filter(x -> x != null).collect(Collectors.toMap(x -> x.pictureid, x -> x));
//			var ids = somap.values().stream().mapToInt(x -> x.pictureid).toArray();
			var glist = factoryRead.QueryFunction("product_GetGalleryByIDList", ProductGalleryBOApi[].class, false,
					somap.keySet());
			for (var g : glist) {
				var so = somap.get(g.PictureID);
				if (so != null) {
					g.Width = so.width;
					g.WidthLarge = so.widthlarge;
					g.WidthOrg = so.widthorg;
					g.Height = so.height;
					g.HeightLarge = so.heightlarge;
					g.HeightOrg = so.heightorg;
				}
			}
			result.message = "Success";
			result.result = glist;
			result.total = (int) qr.getHits().getTotalHits().value;
		} catch (IOException e) {
			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.message = "Failed: " + e.toString() + ": " + e.getMessage() + " - " + trace;
			Logs.LogException(e);
			throw e;
		}
		return result;
	}

//	public ProductBOApi[] getFeatureProducts(int isShowHome, int categoryID, int siteID, boolean isPriority) {
//		var list = Stream.of(factoryRead.QueryFunction("productfeature_getBySite", ProductFeatureBO[].class, false,
//				isShowHome, categoryID, siteID, isPriority)).map(f -> f.ProductID).collect(Collectors.toList());
//		if (list.size() < 5) {
//		}
//		var khmer = siteID == 6;
//		var products = factoryRead.QueryFunction(function_GetProductByListID, ProductBOApi[].class,
//				false, list, siteID, khmer ? 163 : 3, khmer ? "km-KH" : "vi-VN");
//		return products;
//	}

	public ProductBO[] getFeatureProductsByListCate(int isShowHome, int[] categoryID, int siteID, boolean isPriority)
			throws Throwable {

//		var elasticClient = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex = elasticClient.getClient();

		// get feature
		var feature = factoryRead.QueryFunctionCached("productfeature_getByListCate", ProductFeatureBO[].class, false,
				isShowHome, categoryID, siteID, isPriority);
		// id list
		var list = Stream.of(feature).map(f -> f.ProductID).collect(Collectors.toList());
		// ket qua tra ve phan loai theo cat: map(cat id, set productid)
		var fullmap = Stream.of(feature).collect(
				Collectors.groupingBy(f -> f.CategoryID, Collectors.mapping(f -> f.ProductID, Collectors.toSet())));
		// cat ko du 5 sp: map(cat id, set productid)
		var map = fullmap.entrySet().stream().filter(e -> e.getValue().size() < 5)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		// them cat ko co trong map
		Arrays.stream(categoryID).filter(x -> !fullmap.containsKey(x)).forEach(x -> map.put(x, new HashSet<Integer>()));
		// cat id không đủ 5 sp
		var cateids = map.keySet().stream().mapToInt(Number::intValue).toArray();
		var khmer = siteID == 6;
		int prov = khmer ? 163 : 3;
		String lang = khmer ? "km-KH" : "vi-VN";
		if (cateids.length > 0)
			query: {
				var q = boolQuery().must(termQuery("SiteID", siteID)).must(termQuery("HasBimage", 1))
						.must(termsQuery("CategoryID", cateids)).must(termQuery("Prices.WebStatusId_" + prov, 4))
						.mustNot(termsQuery("ProductID", list)).must(termQuery("Lang", DidxHelper.GenTerm3(lang)));
				var script = "int prov = " + prov + ";\r\n"
						+ "				  if(doc[\"Prices.Price_\" + prov].size() == 0 || doc[\"Prices.Price_\" + prov].value <=0)\r\n"
						+ "					return 0;\r\n"
						+ "                  long now = System.currentTimeMillis();\r\n"
						+ "                  int total = 0;\r\n" + "				  double percent = 0;\r\n"
						+ "                  for(int i = 0; i < doc[\"PromotionSoList.discountvalue\"].size(); i++) {\r\n"
						+ "					if(now > doc[\"PromotionSoList.begindate\"][i] && now < doc[\"PromotionSoList.enddate\"][i]) {\r\n"
						+ "						if(doc[\"PromotionSoList.discountvalue\"][i] > 0){"
						+ "							if(!doc[\"PromotionSoList.ispercentdiscount\"][i])\r\n"
						+ "								total += doc[\"PromotionSoList.discountvalue\"][i];\r\n"
						+ "							else\r\n"
						+ "								percent += doc[\"PromotionSoList.discountvalue\"][i]/100;\r\n"
						+ " 					}else{"
						+ "							if(!doc[\"PromotionSoList.ispercentdiscount\"][i])\r\n"
						+ "								total += doc[\"PromotionSoList.tovalue\"][i];\r\n"
						+ "							else\r\n"
						+ "								percent += doc[\"PromotionSoList.tovalue\"][i]/100;\r\n"
						+ "						}" + "					}\r\n" + "                  }\r\n"
						+ "				  percent += (double) total/doc[\"Prices.Price_\" + prov].value;\r\n"
						+ "				  return percent;";
				SearchSourceBuilder sb = new SearchSourceBuilder();
				sb.fetchSource(new String[] { "ProductID", "CategoryID" }, null).from(0).size(0).query(q);
				sb.aggregation(terms("categoryID").field("CategoryID").size(100)
						.subAggregation(topHits("topCategoryHits")//
								.fetchSource(new String[] { "ProductID", "CategoryID" }, null).size(5)//
								.sort(scriptSort(new Script(script), ScriptSortType.NUMBER).order(SortOrder.DESC))//
						));
				try {

					SearchResponse queryResults = null;
//					var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//					var clientIndex1 = elasticClient1.getClient();
//					try {

						queryResults = clientIndex.search(new SearchRequest(CurrentIndexDB).source(sb),
								RequestOptions.DEFAULT);
//					} catch (Throwable e) {
//
//						Logs.LogException(e);
//						throw e;
//					} finally {
//						elasticClient1.close();
//					}

					var aggrs = queryResults.getAggregations();
					ParsedLongTerms bucket = (ParsedLongTerms) aggrs.get("categoryID");
					if (bucket == null || bucket.getBuckets().size() == 0)
						break query;
					var mapmore = bucket.getBuckets().stream()
							.flatMap(v -> Stream
									.of(((TopHits) v.getAggregations().get("topCategoryHits")).getHits().getHits()))
							.map(v -> {
								try {
									return mapper.readValue(v.getSourceAsString(), ProductSO.class);
								} catch (IOException e1) {
									Logs.LogException(e1);
									return null;
								}
							}).filter(x -> x != null).collect(Collectors.groupingBy(x -> x.CategoryID,
									Collectors.mapping(x -> x.ProductID, Collectors.toList())));

					for (var x : map.entrySet()) {
						int cat = x.getKey();
						var ids = x.getValue();
						var more = mapmore.get(cat);
						if (more != null && more.size() > 0) {
							for (var add : more) {
								if (ids.size() >= 5)
									break;
								ids.add(add);
								list.add(add);
							}
						}
					}
				} catch (Exception e) {
					Logs.LogException(e);
				} finally {
					//elasticClient.close();

				}
			}
		int[] ids = list.stream().mapToInt(x -> x).toArray();
		var products = ids.length > 0 ?
//						(siteID == 6 && (DidxHelper.isBeta() || DidxHelper.isStaging())
				GetSimpleProductListByListID_PriceStrings_soMap(list.stream().mapToInt(x -> x).toArray(), siteID, prov,
						lang)
//						: //
//						factoryRead.QueryFunction(function_GetProductByListID, ProductBO[].class, false, list, siteID,
//								prov, khmer ? "km-KH" : "vi-VN")) //
				: new ProductBO[0];
		return products;

	}

	public Map<Integer, ProductSO> getSOMap(int[] productIDs, int siteID, String langID) throws IOException {
		var q = boolQuery().must(termQuery("SiteID", siteID)).must(termsQuery("ProductID", productIDs))
				.must(termQuery("Lang", DidxHelper.GenTerm3(langID)));
		var sb = new SearchSourceBuilder()
				.fetchSource(new String[] { "ProductID", "CategoryID", "PaymentFromDate", "PaymentToDate", "IsPayment",
						"StickerLabel", "PercentInstallment", "ProductSoldCount" }, null)
				.from(0).size(productIDs.length).query(q);
		SearchResponse queryResults = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			queryResults = clientIndex.search(new SearchRequest(CurrentIndexDB).source(sb), RequestOptions.DEFAULT);

//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		return Stream.of(queryResults.getHits().getHits()).map(x -> {
			try {
				return esmapper.readValue(x.getSourceAsString(), ProductSO.class);
			} catch (IOException e) {
//				e.printStackTrace();
				return null;
			}
		}).collect(Collectors.toMap(x -> x.ProductID, x -> x));
	}

	public List<Integer> getTopSoldCountFromCache(int categoryID, int siteID, int provinceID, String langID, int amount)
			throws Throwable {
		var topSoldCount = getCachedTopProductSelling(siteID, provinceID, langID);
		if (topSoldCount != null && topSoldCount.size() > 0) {
			return topSoldCount.get(categoryID);
		} else {
			String key = "getTopSoldCountFromCache_" + categoryID + "_" + siteID + "_" + provinceID + "_" + langID + "_"
					+ amount;
			var rs = (List<Integer>) CacheStaticHelper.GetFromCache(key, 30);
			if (rs == null) {
				rs = getTopSoldCount(categoryID, siteID, provinceID, langID, amount);
				CacheStaticHelper.AddToCache(key, rs);
			}
			return rs;
		}

	}

	public List<Integer> getTopSoldCount(int categoryID, int siteID, int provinceID, String langID, int amount)
			throws IOException {

		var q = boolQuery().must(termQuery("SiteID", siteID))
				.must(boolQuery().should(termQuery("CategoryID", categoryID))
						.should(termQuery("CategoryMap", categoryID)).should(termQuery("GroupId", categoryID))
						.should(termQuery("ParentIdList", categoryID)))
				.must(termQuery("Lang", DidxHelper.GenTerm3(langID))).must(termQuery("HasBimage", 1))
				.must(termQuery("Prices.WebStatusId_" + provinceID, 4));
		var sb = new SearchSourceBuilder().fetchSource(new String[] { "ProductID" }, null).from(0).size(amount).query(q)
				.sort("ProductSoldCount", SortOrder.DESC);
		SearchResponse queryResults = null;

//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			queryResults = clientIndex.search(new SearchRequest(CurrentIndexDB).source(sb), RequestOptions.DEFAULT);

//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		return Stream.of(queryResults.getHits().getHits()).map(x -> {
			try {
				return esmapper.readValue(x.getSourceAsString(), ProductSO.class).ProductID;
			} catch (IOException e) {
//				e.printStackTrace();
				return -1;
			}
		}).filter(x -> x > 0).collect(Collectors.toList());

	}

	// top 3 agg
	public Map<Integer, List<Integer>> getTop3SoldCount(List<Integer> categoryID, int siteID, int provinceID,
			String langID) throws IOException {
		var q = boolQuery().must(termQuery("SiteID", siteID))
				.must(boolQuery().should(termsQuery("CategoryID", categoryID))
						.should(termsQuery("CategoryMap", categoryID)).should(termsQuery("GroupId", categoryID))
						.should(termsQuery("ParentIdList", categoryID)))
				.must(termQuery("Lang", DidxHelper.GenTerm3(langID))).must(termQuery("HasBimage", 1))
				.must(termsQuery("Prices.WebStatusId_" + provinceID, new int[] { 4, 11 }));
		var sb = new SearchSourceBuilder().fetchSource(new String[] { "ProductID" }, null).from(0).size(0).query(q)
				.aggregation(terms("cate").field("CategoryID").size(categoryID.size())
						.subAggregation(topHits("topSale").sort("ProductSoldCount", SortOrder.DESC).size(3)));

		SearchResponse queryResults = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			queryResults = clientIndex.search(new SearchRequest(CurrentIndexDB).source(sb), RequestOptions.DEFAULT);

//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		var result2 = new HashMap<Integer, List<Integer>>();
		Terms agg = queryResults.getAggregations().get("cate");

		for (Terms.Bucket entry : agg.getBuckets()) {
			String key = entry.getKey().toString();
			int cate = Utils.toInt(key);
			TopHits topHits = entry.getAggregations().get("topSale");
			var productids = Stream.of(topHits.getHits().getHits()).map(x -> {
				try {
					return esmapper.readValue(x.getSourceAsString(), ProductSO.class).ProductID;
				} catch (IOException e) {
					return -1;
				}
			}).filter(x -> x > 0).collect(Collectors.toList());
			result2.put(cate, productids);
		}
		return result2;
	}

	public void processDetail(ProductBO product, int siteID, int provinceID, Integer storeID, String lang,
			CodeTimers timer) throws Throwable {
//		int productID = product.ProductID;
		if (siteID == 11) {

			// isleaf
			if (product.ProductCategoryBO != null && product.ProductCategoryBO.CategoryID > 0) {
				product.ProductCategoryBO.isLeaf = checkCategoryIsLeaf(product.ProductCategoryBO.CategoryID);
//				product.ProductCategoryBO.RelatedIds = product.ProductCategoryBO.ListRelateCategoryID.split(",").;
			}
			String newslink = "";
			if (product.ProductManuLangBO != null) {
				newslink = product.ProductManuLangBO.newslink;
			}
			product.ProductManuLangBO = GetMainManuByID(product.ManufactureID, 11, "vi-VN");
			if (product.ProductManuLangBO != null) {
				product.ProductManuLangBO.newslink = newslink;
			}

			// neu storeid > 0: lay gia cua store do, nguoc lai lay gia min cua tinh do
			GetDefaultPriceAndPromotionBHX(lang, product, false, storeID, provinceID);
//			codetimer.end();
			return;

		} else {

			if (siteID == 12) {
				if (!product.HasBimage) {
					product = new ProductBO();
					return;
				}
			}
			if (product.ProductLanguageBO != null) {
				product.BimageUrl = GenProductImageUrl(product.CategoryID, product.ProductID,
						product.ProductLanguageBO.bimage);
				product.MimageUrl = GenProductImageUrl(product.CategoryID, product.ProductID,
						product.ProductLanguageBO.mimage);
				product.SimageUrl = GenProductImageUrl(product.CategoryID, product.ProductID,
						product.ProductLanguageBO.simage);
				product.ProductLanguageBO.bimageurl = product.BimageUrl;
				product.ProductLanguageBO.mimageurl = product.BimageUrl;
				product.ProductLanguageBO.simageurl = product.SimageUrl;
			}
//			if (DidxHelper.isBeta() || DidxHelper.isHanh() || DidxHelper.isStaging()) {
			var listPropManul = GetPropUseManualByProductID(product.ProductID, siteID, lang);
			if (listPropManul != null && listPropManul.length > 0) {
				product.PropManual = listPropManul;
			}
//			}

			var isAccessory = AccessoryCategory.contains((int) product.CategoryID);

			Promotion[] promoGroup = null;
			String productCode = product.ProductCode;

			if (product.getSubBrandPromotion && product.pmProductBOList != null && product.pmProductBOList.length > 0) {
				int provinceIDpromo = -1;
				int outputTypeID = 0;
				double salePrice = 0;
				int inventoryStatusID = 1;
				if(Utils.StringIsEmpty(productCode)) { // trường hợp k có product = default
					var code = product.pmProductBOList[0];
					String recordid = Stream.of(code.subGroupID, code.brandID, provinceIDpromo, outputTypeID, salePrice,
							inventoryStatusID, siteID).map(x -> x.toString()).collect(Collectors.joining("_"));
					promoGroup = getPromotionGroup(recordid);
				}else {
					var code = Stream.of(product.pmProductBOList).filter(x ->x!= null && x.productID.equals(productCode)).findFirst().orElse(null);
					String recordid = Stream.of(code.subGroupID, code.brandID, provinceIDpromo, outputTypeID, salePrice,
							inventoryStatusID, siteID).map(x -> x.toString()).collect(Collectors.joining("_"));
					promoGroup = getPromotionGroup(recordid);
				}
			}

			GetDefaultPriceAndPromotion(siteID, lang, product, isAccessory, false, promoGroup); // đã  xử lí kiểm tra thông tin truyền productCode trước đó

			getHelperBySite(siteID).processDetail(product, siteID, provinceID, storeID, lang, timer);

			// thong tin bao hanh
			String code;
			ProductErpPriceBO price;
			if ((price = product.getPrice()) != null && (code = price.ProductCode) != null) {
				int company = DidxHelper.getCompanyBySiteID(siteID);
				if (company > 0) {
					var w = getWarrantyMonthByProductCode(code, 1, company);
					if (w != null) {
						price.extendWarrantyDays = w.extendWarrantyDays;
						price.extendWarrantyMonth = w.extendWarrantyMonth;
						price.warrantyMonth = w.warrantyMonth;
						price.warrantyDays = w.warrantyDays;
					}
				}
			}

			// gia du kien
			LocalDateTime after60Days = product.CreatedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
					.plusDays(60);
			boolean validCate = product.ProductCategoryBO != null && product.ProductCategoryBO.CategoryID == 2182;
			if (product.ExpectedPrice > 0 && product.CreatedDate != null
					&& (Date.from(after60Days.atZone(ZoneId.systemDefault()).toInstant()).after(new Date())
							|| validCate)) {
				if (product.ProductErpPriceBO == null || product.ProductErpPriceBO.WebStatusId == 1) {
					product.ProductErpPriceBO = new ProductErpPriceBO();
					product.ProductErpPriceBO.Price = product.ExpectedPrice;
					product.ProductErpPriceBO.IsShowHome = true;
					product.ProductErpPriceBO.IsWebShow = true;
					product.ProductErpPriceBO.WebStatusId = 9;
//					if ((Date.from(after60Days.atZone(ZoneId.systemDefault()).toInstant()).before(new Date()))
//							&& validCate) {
//						product.ProductErpPriceBO.Price = 0;
//						product.ProductErpPriceBO.WebStatusId = 1;
//					}
				}

			}

			// WEBSTATUS HARDCODE
			hardCodedWebStatus(product, siteID, provinceID);

			// off tra gop 0% tu 10/2 -> 14/2
			// ap dung site2
			long now = System.currentTimeMillis();
			if ((siteID == 1 || siteID == 2) && now >= 1612890000000L && now < 1613322000000L) {
				product.IsPayment = false;
				if (product.ProductLanguageBO != null) {
					product.ProductLanguageBO.IsPayment = 0;
				}
			}
			// set gia = 0 neu nkd (bỏ)
			if (product.ProductErpPriceBO != null) {
				if (
//						(product.ProductErpPriceBO.WebStatusId == 1 && siteID == 1)
				// set gia = 0 neu sp co an gia (anh Nam yeu cau 20201006)
//						||
				(siteID == 1 && product.ProductLanguageBO != null && product.ProductLanguageBO.isHiddenPrice)) {
					product.ProductErpPriceBO.Price = 0;
					product.ProductErpPriceBO.StandardPriceAreaSalePrice = 0;
					product.ProductErpPriceBO.StandardSalePrice = 0;

				}
			} else {
				// trả ra websatusid mặc định = 1 nếu giá = null (20201109)
//				int productID = product.ProductID;
//				product.ProductErpPriceBO = new ProductErpPriceBO() {
//					{
//						ProductId = productID;
//						WebStatusId = 1;
//					}
//				};
			}

//			return product;
		}

	}

	// hardCoded vars
	private final List<Integer> chuyenhangStatusID = Arrays.asList(8, 11, 99);

	public void hardCodedWebStatus(ProductBO product, int siteID, int provinceID) {
		if (product.ProductErpPriceBO != null) {
			// HIỂN THỊ SẢN PHẨM Ở TUỲ KHU VỰC KINH DOANH
			// https://docs.google.com/document/d/1GG_g1z_UGHSBZc9Egv5fvCgCfSVICVVrZdlTFCS2GeI/edit
			// 20201026
			List<Integer> provs = Arrays.asList(104, 112, 114, 117, 123, 128, 131, 137, 145, 148, 153, 155, 156, 103,
					106, 121, 101, 124, 130, 142, 147, 118, 120, 133, 134, 135, 147, 149, 5);
			List<String> codes = Arrays.asList("3051098001262", "3051098001263", "3051098001264", "3051098001265");

			if (codes.contains(product.ProductCode) && !provs.contains(provinceID)) {
				product.ProductErpPriceBO.WebStatusId = 1;
				product.CreatedDate = new Date(0);
			}

//			00:00 24/01 → 00:00 18/02, sản phẩm thuoc cate BigCE trạng thái PREORDER và là sản phẩm LẮP ĐẶT thì
//			đưa nó về TẠM HẾT HÀNG
//			Ds cate BigCE 1943 1944 2002 5475 2202
//			SP lap dat: IsSetupProduct
//			20210120
			long now = System.currentTimeMillis(), from = 1611421200000L, to = 1613581200000L;
			if (siteID == 2 && now > from && now < to && product.isSetupProduct
					&& product.ProductErpPriceBO.WebStatusId == 8 && bigCE.contains(product.CategoryID)) {
				product.ProductErpPriceBO.WebStatusId = 5;
			}

			// off cate phan mem tu 10/2 -> 14/2
			// ap dung site2
			if (siteID == 2 && now >= 1612890000000L && now < 1613322000000L && product.CategoryID == 85) {
				product.ProductErpPriceBO.WebStatusId = 5;
			}

			// chuyển trạng thái 11 thành 4, 7 thành 1 (TGDĐ)
			// bỏ sau khi up live web mới
			// 20210311
//			if (siteID == 1 && !DidxHelper.isBeta() && !DidxHelper.isStaging() && !DidxHelper.isLocal()) {
//				if (product.ProductErpPriceBO.WebStatusId == 11) {
//					product.ProductErpPriceBO.WebStatusId = 4;
//				}
//				if (product.ProductErpPriceBO.WebStatusId == 7) {
//					product.ProductErpPriceBO.WebStatusId = 1;
//				}
//			}

//			// STATUS TGDĐ THEO ĐMX CHO WEB MỚI
//			// bỏ ràng chỗ này khi up live web mới
//			if (siteID == 1 && DidxHelper.isLive()) {
//				product.ProductErpPriceBO.WebStatusId = product.ProductErpPriceBO.WebStatusIdOld;
//			}
		}
	}

	public ProductBO GetProductFromCache(int productID, int siteID, Integer provinceID, Integer storeID, String lang,
			CodeTimers timer) throws Throwable {
		timer.start("odb");
		String key = "GetProduct" + productID + "_" + siteID + "_" + provinceID + "_" + storeID + "_" + lang;
		var rs = (ProductBO) CacheStaticHelper.GetFromCache(key, 5);
		if (rs == null) {
			rs = GetProduct(productID, siteID, provinceID, storeID, lang, timer);
			CacheStaticHelper.AddToCache(key, rs);

		}
		timer.pause("odb");
		return rs;

	}

	public ProductBO GetProduct(int productID, int siteID, Integer provinceID, Integer storeID, String lang,
			CodeTimers timer) throws Throwable {

		if (provinceID == null)
			provinceID = 3;
		if (Utils.StringIsEmpty(lang))
			lang = "vi-VN";
		ProductBO product = null;
		timer.start("odb");
		product = GetProductBOByProductID(productID, siteID, provinceID, lang, storeID);
		timer.pause("odb");
		if (product != null) {
			processDetail(product, siteID, provinceID, storeID, lang, timer);
		}
		return product;
	}

	public ProductBO[] getFeautureLaptop() {
		return null;
	}


	public ProductBO GetProduct_PriceStrings(int productID, int siteID, Integer provinceID, Integer storeID,
											 String lang, CodeTimers timer) throws Throwable {
		return  GetProduct_PriceStrings(productID,siteID,provinceID,storeID,lang,timer,null);
	}


	public ProductBO GetProduct_PriceStrings(int productID, int siteID, Integer provinceID, Integer storeID,
			String lang, CodeTimers timer,String productCode) throws Throwable {

		if (provinceID == null)
			provinceID = 3;
		if (Utils.StringIsEmpty(lang))
			lang = "vi-VN";
		ProductBO product = null;
		timer.start("odb");
		product = getProductBOByProductID_PriceStrings(productID, siteID, provinceID, lang, storeID,productCode);
		timer.pause("odb");

		if (product != null) {
//			if(Strings.isNullOrEmpty(product.Simage) && Strings.isNullOrEmpty(product.Mimage) && Strings.isNullOrEmpty(product.Bimage) && Strings.isNullOrEmpty(product.Image)){
//				return null;
//			}
			if (product.ProductLanguageBO == null
					|| ((Strings.isNullOrEmpty(product.ProductLanguageBO.bimage) && productID != 201159)
							&& Strings.isNullOrEmpty(product.ProductLanguageBO.mimage)
							&& Strings.isNullOrEmpty(product.ProductLanguageBO.simage))) {
				return null;
			}
			// TODO: xử lý chỗ này khác nhau cho site 11
			timer.start("pricestringsprocess");
			processPriceStrings(product, provinceID, siteID, storeID,productCode);
			timer.pause("pricestringsprocess");
			timer.start("processDetail");
			processDetail(product, siteID, provinceID, storeID, lang, timer);
			timer.pause("processDetail");

//			if (product.ProductErpPriceBO != null) {
//				if (!Utils.StringIsEmpty(product.ProductErpPriceBO.Image)) {
//
//					//GenProductImageUrl(product.CategoryID, product.ProductID, product.ProductErpPriceBO.Bimage)
//					var bimage = !Utils.StringIsEmpty(product.ProductErpPriceBO.Bimage) ? product.ProductErpPriceBO.Bimage : product.Bimage + "";
//					var mimage = !Utils.StringIsEmpty(product.ProductErpPriceBO.Bimage) ? product.ProductErpPriceBO.Bimage : product.Mimage + "";
//					var simage = !Utils.StringIsEmpty(product.ProductErpPriceBO.Simage) ? product.ProductErpPriceBO.Simage : product.Simage + "";
//
//					product.Bimage = bimage;
//					product.BimageUrl = GenProductImageUrl(product.CategoryID, product.ProductID, bimage);
//
//					product.Mimage = mimage;
//					product.MimageUrl = GenProductImageUrl(product.CategoryID, product.ProductID, mimage);
//
//					product.Simage = simage;
//					product.SimageUrl = GenProductImageUrl(product.CategoryID, product.ProductID, simage);
//
//
//				}
//			}
		}

		return product;
	}

	public int GetLenthProduct(ProductBO product) throws JsonProcessingException {
		if (product != null) {
			return mapper.writeValueAsString(product).length();
		}
		return 0;

	}

	public String GetJsonProduct(ProductBO product) throws JsonProcessingException {
		if (product != null) {
			return mapper.writeValueAsString(product);
		}
		return "";

	}

	private void proccessPriceStock(ProductErpPriceBO price, Map<String, PriceStockObject> quantities) {
		if (quantities != null && price != null) {

			for (var a : quantities.entrySet()) {
				if (a.getValue() != null) {

					if (a.getValue().quantities != null)
						for (var q : a.getValue().quantities.entrySet()) {
							price.TotalQuantity += q.getValue();
							if (q.getKey() == price.ProvinceId) {
								price.Quantity += q.getValue();
								if (a.getKey().equals(price.RecordID)) {
									price.ProductCodeQuantity += q.getValue();
								}
							}
							if (a.getKey().equals(price.RecordID)) {
								price.ProductCodeTotalQuantity += q.getValue();
							}
						}

					if (a.getValue().sampleQuantities != null) {
						for (var q : a.getValue().sampleQuantities.entrySet()) {
							price.TotalSampleQuantity += q.getValue();
							if (q.getKey() == price.ProvinceId) {
								price.SampleQuantity += q.getValue();
							}
						}
					}

					if (a.getValue().relateQuantities != null) {
						for (var q : a.getValue().relateQuantities.entrySet()) {
							if (q.getKey() == price.ProvinceId && a.getKey().equals(price.RecordID)) {
								price.TotalQuantityRelateProvince += q.getValue();
							}
						}
					}

					if (a.getValue().centerQuantities != null) {
						Integer d = a.getValue().centerQuantities.get(price.ProvinceId);
						price.CenterQuantity = d != null ? d : 0;
					}
				}
			}
		}
	}
	public void processPriceStrings(ProductBO product, int provinceID, int siteID, int storeID) throws Throwable {
		processPriceStrings(product,provinceID,siteID,storeID,null);
	}

	/**
	 * - Hàm xử lí nếu có truyền thêm productCode
	 *
	 * */
	public void processPriceStrings(ProductBO product, int provinceID, int siteID, int storeID,String productCode) throws Throwable {
		if (product == null) {
			return;
		}

		// xử lý combo bhx
		if (siteID == 11 && product.ProductLanguageBO != null
				&& !Utils.StringIsEmpty(product.ProductLanguageBO.comboproductidlist)) {
			// processComboBhx(product, provinceID, storeID);
			return;
		}

		if (!Strings.isNullOrEmpty(product.priceDefaultString)) {
			var prideDefaults = mapper.readValue(product.priceDefaultString, ProductErpPriceBO[].class);
			if (prideDefaults != null && prideDefaults.length > 0) {
				product.ProductErpPriceBO = Stream.of(prideDefaults)
//						.filter(x ->
//								(siteID == 1 && x.ProvinceId == 3) || 	// ← bỏ rule lấy tỉnh thành 3 với site 1 20210311
//																		// Thêm lại 20210317
//								(siteID != 1 && x.ProvinceId == provinceID))
						.filter(x -> x.ProvinceId == provinceID).findFirst().orElse(null);
				product.priceDefaultString = null;
//				product.message = "price default string applied";
			}
		}
		if (product.priceStrings!=null && product.priceStrings.length>0) {
			if(Utils.StringIsEmpty(productCode)) {
				var quantities = Stream.of(product.priceStrings).collect(Collectors.toMap(x -> x.RecordID, x -> {
					try {
						return x.DataEx != null ? mapper.readValue(x.DataEx, PriceStockObject.class)
								: new PriceStockObject();
					} catch (IOException e1) {
//					e1.printStackTrace();
						return new PriceStockObject();
					}
				}));

				product.ProductErpPriceBOList = Stream.of(product.priceStrings).flatMap(x -> {
					try {
						var list = mapper.readValue(x.Data, ProductErpPriceBO[].class);
						for (var item : list)
							item.RecordID = x.RecordID;
						return Stream.of(list);
					} catch (Exception e) {
//					e.printStackTrace();
						return Stream.empty();
					}
				})
//					.filter(x -> x != null
//					&& (
//						(siteID == 1 && x.ProvinceId == 3)  || 	// ← bỏ rule lấy tỉnh thành 3 với site 1 20210311
//																// Thêm lại 20210317
//						(siteID != 1 && x.ProvinceId == provinceID) || x.ProvinceId == 0
//					)) //
						.filter(x -> x != null && x.ProvinceId == provinceID)
						// quantity tinh thanh & ca nuoc
						.map(x -> {
							proccessPriceStock(x, quantities);
							x.IsExist = true;
							// sua lai provinceid theo tinh thanh truyen vao 20201208
							x.ProvinceId = provinceID;
							return x;
						})//
						.toArray(ProductErpPriceBO[]::new);
				product.priceStrings = null;
			}else {// trường hợp có truyền productCode
				//filter lọc lấy tồn theo productcode
//				var quantities = Stream.of(product.priceStrings).filter(x -> x != null && x.RecordID.contains(productCode))
//						.collect(Collectors.toMap(x -> x.RecordID, x -> {
//							try {
//								return x.DataEx != null ? mapper.readValue(x.DataEx, PriceStockObject.class) :
//										new PriceStockObject();
//							} catch (IOException e) {
//								return new PriceStockObject();
//							}
//						}));
//				var productErpPriceBOS = Stream.of(product.priceStrings).filter(x -> x != null && x.RecordID.contains(productCode)) // filter lấy ra productcode
//						.flatMap(x -> {
//							try {
//								var productERP = mapper.readValue(x.Data, ProductErpPriceBO[].class);
//								for (ProductErpPriceBO p : productERP) {
//									p.RecordID = x.RecordID;
//								}
//								return Stream.of(productERP);
//
//							} catch (IOException e) {
//								e.printStackTrace();
//								return Stream.empty();
//							}
//						})
//						.filter(x -> x != null && x.ProvinceId == provinceID) // filter theo tỉnh thành
//						.map(x -> { // quantity tinh thanh
//							proccessPriceStock(x, quantities);
//							x.IsExist = true;
//							// sua lai provinceid theo tinh thanh truyen vao 20201208
//							x.ProvinceId = provinceID;
//							return x;
//						})//
//						.toArray(ProductErpPriceBO[]::new);
//				product.priceStrings = null;
//				product.ProductErpPriceBOList = productErpPriceBOS;
//				var priceStringBO = product.pricebyproductcode;
//				List<ProductErpPriceBO> priceBO = getPriceHelper().processQuantities(siteID,priceStringBO,provinceID);
//				product.ProductErpPriceBO = productErpPriceBOS[0].clone();
//				if(product.ProductErpPriceBO != null){
//					var a = product.SpecialSaleProgram;
//					product.ProductErpPriceBO.specialSale = getSpecialSaleProgram2(GetPm_ProductFromCache(product.ProductErpPriceBO.ProductCode), product.ProductErpPriceBO.ProductCode, 1);
//					product.ProductErpPriceBO.CategoryId = product.ProductCategoryBO == null ? 0 : product.ProductCategoryBO.CategoryID;
//					APIPriceHelper.getHelperBySite(1).ProcessProductStatus(product.ProductErpPriceBO);
//				}
				var priceString = Stream.of(product.priceStrings).filter(x ->x!=null && x.RecordID.contains(productCode)).toArray(PriceStringBO[]::new);
				if(priceString!= null && priceString.length >0){
					List<ProductErpPriceBO> priceBO =getPriceHelper().processQuantities(siteID,priceString,provinceID);
					if(priceBO != null && priceBO.size() >0) {
						product.ProductErpPriceBOList = priceBO.toArray(ProductErpPriceBO[]::new);
						product.ProductErpPriceBO = priceBO.get(0);
					}
				}
				product.ProductCode = productCode; // gán productCode
				product.priceStrings = null;
			}
		}
		// site bhx
		if (siteID == 11) {
			if (product.promotionStrings != null) {
				// product.PromotionBHX =
				var tmp = Stream.of(product.promotionStrings).flatMap(x -> {
					try {
						return Stream.of(mapper.readValue(x.Data, PromotionBHX[].class));
					} catch (IOException e) {
//						e.printStackTrace();
						return Stream.empty();
					}
				});

//				var t = tmp.filter(x -> x.ProvinceId == provinceID)
////						.map(x -> {
////					x.p .ProductCodes = x.ProductId;
////					x.ReturnValues = x.ReturnValue;
////							return x;
////						})
//						.collect(Collectors.toList());
				// lấy km theo tỉnh và loại trừ km combo
				product.PromotionBHX = tmp.filter(x -> x.ProvinceId == provinceID).collect(Collectors.toList());
				product.promotionStrings = null;

				// xử lý tồn của sản phẩm khuyến mãi
			}
		} else {
			if (product.promotionStrings != null) {
				if (Utils.StringIsEmpty(productCode)) {
					product.Promotion = Stream.of(product.promotionStrings).filter(x -> x.SiteID != 6 || x.BrandID == 11)
							.flatMap(x -> {
								try {
									return Stream.of(mapper.readValue(x.Data, Promotion[].class));
								} catch (IOException e) {
//								e.printStackTrace();
									return Stream.empty();
								}
							}).filter(x -> x.provinceIDApplied(provinceID)).map(x -> {
								x.ProductCodes = x.ProductId;
								x.ReturnValues = x.ReturnValue;
								// tra ra ReturnValue = 0 cho site tgdd
								if (siteID == 1) {
									x.ReturnValue = "0";
								}
								return x;
							}).collect(Collectors.toList());
					product.promotionStrings = null;
				}else{ // xử lí khuyến mãi theo productCode
					product.Promotion =	Stream.of(product.promotionStrings).filter(x ->x!= null && (x.SiteID != 6 || x.BrandID ==11) && x.recordid.contains(productCode))
							.flatMap(x -> {
								try {
									Promotion[] promotion = mapper.readValue(x.Data,Promotion[].class);
									return  Stream.of(promotion);
								} catch (IOException e) {
									e.printStackTrace();
									return  Stream.empty();
								}
							}).filter(x -> x.provinceIDApplied(provinceID)).map(x -> {
								x.ProductCodes = x.ProductId; // productCode KM
								x.ReturnValues = x.ReturnValue;
								// tra ra ReturnValue = 0 cho site tgdd
								if (siteID == 1) {
									x.ReturnValue = "0";
								}
								return x;
							}).collect(Collectors.toList());
					product.promotionStrings = null;
				}
			}
		}
	}

	public void processSimplePriceStrings(ProductBO product, int siteID, int provinceID)
			throws JsonParseException, JsonMappingException, IOException {
		if (!Strings.isNullOrEmpty(product.priceDefaultString)) {
			var prideDefaults = mapper.readValue(product.priceDefaultString, ProductErpPriceBO[].class);
			if (prideDefaults != null && prideDefaults.length > 0) {
				product.ProductErpPriceBO = Stream.of(prideDefaults)
//						.filter(x ->
//								(siteID == 1 && x.ProvinceId == 3) || 	// ← bỏ rule lấy tỉnh thành 3 với site 1 20210311
//																		// Thêm lại 20210317
//								(siteID != 1 && x.ProvinceId == provinceID))
						.filter(x -> x.ProvinceId == provinceID).findFirst().orElse(null);
				product.priceDefaultString = null;
//				product.message = "price default string applied";
			}

		}
		if ((product.ProductErpPriceBO == null || Utils.StringIsEmpty(product.ProductErpPriceBO.ProductCode))
				&& product.ProductLanguageBO != null && product.ProductLanguageBO.expectedprice != null
				&& product.ProductLanguageBO.expectedprice > 0 && product.CreatedDate != null) {
			// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.setTime(product.CreatedDate);
			c.add(Calendar.DAY_OF_YEAR, 60);
			if (product.ProductCategoryLangBO != null && product.ProductCategoryLangBO.CategoryID == 2182
					|| c.after(Calendar.getInstance())) {
				var price = new ProductErpPriceBO();
				price.Price = product.ProductLanguageBO.expectedprice;
				price.WebStatusId = 9;
				price.IsShowHome = true;
				price.IsWebShow = true;
				price.ProductId = product.ProductID;
				product.ProductErpPriceBO = price;
			}

		}
		// (product.CreatedDate)
		if (product.priceStrings != null) {
			var quantities = Stream.of(product.priceStrings).collect(Collectors.toMap(x -> x.RecordID, x -> {
				try {
					return x.DataEx != null ? mapper.readValue(x.DataEx, PriceStockObject.class)
							: new PriceStockObject();
				} catch (IOException e1) {
//					e1.printStackTrace();
					return new PriceStockObject();
				}
			}));
			product.ProductErpPriceBOList = Stream.of(product.priceStrings).flatMap(x -> {
				try {
					var list = mapper.readValue(x.Data, ProductErpPriceBO[].class);
					for (var item : list)
						item.RecordID = x.RecordID;
					return Stream.of(list);
				} catch (Exception e) {
					// e.printStackTrace();
					return Stream.empty();
				}
			})
//					.filter(x -> x != null
//					&& (
//							(siteID == 1 && x.ProvinceId == 3) || 	// ← bỏ rule lấy tỉnh thành 3 với site 1 20210311
//																	// Thêm lại 20210317
//							(siteID != 1 && x.ProvinceId == provinceID)|| x.ProvinceId == 0)) //
					.filter(x -> x != null && x.ProvinceId == provinceID).map(x -> {
						proccessPriceStock(x, quantities);
						// sua lai provinceid theo tinh thanh truyen vao 20201208
						x.ProvinceId = provinceID;
						return x;
					}).toArray(ProductErpPriceBO[]::new);
			product.priceStrings = null;
		}

		if (product.promotionStrings != null) {
			// if(siteID == 11) {

			// } else {
			product.Promotion = Stream.of(product.promotionStrings).flatMap(x -> {
				try {
					return Stream.of(mapper.readValue(x.Data, Promotion[].class));
				} catch (IOException e) {
					// e.printStackTrace();
					return Stream.empty();
				}
			}).filter(x -> x.provinceIDApplied(provinceID)).map(x -> {
				x.ProductCodes = x.ProductId;
				x.ReturnValues = x.ReturnValue;
				// tra ra ReturnValue = 0 cho site tgdd
				if (product.SiteID == 1) {
					x.ReturnValue = "0";
				}
				return x;
			}).collect(Collectors.toList());
			product.promotionStrings = null;
			// }
		}
	}

	public void processComboBhx(ProductBO product, int provinceID, int storeId) throws Throwable {
		// kiểm tra hợp lệ sp combo: ngày bắt đầu kết thúc, ip sản phẩm lẻ
		var now = Utils.GetCurrentDate();
		if (product.ProductLanguageBO.combofromdate.compareTo(now) > 0
				|| product.ProductLanguageBO.combotodate.compareTo(now) < 0) {
			return;
		}

		if (product.ProductLanguageBO.comboproductidlist.toLowerCase().startsWith("cb")) {
			// combo erp
			return;
		}
		if (product.ProductLanguageBO.comboproductidlist.toLowerCase().startsWith("pos")) {
			// combo pos
			return;
		}

		// tìm con sản phẩm lẻ
		List<ComboDetailBO> lscombodetail = new ArrayList<ComboDetailBO>();
		List<PromotionBHX> promotiondetail = new ArrayList<PromotionBHX>();
		Date expiredDate = new Date();
		var firstDate = true;
		Date expiredDateDisplay = new Date();
		var lstidstring = product.ProductLanguageBO.comboproductidlist.replace(" ", "").split(",");
		for (String str : lstidstring) {
			var comboDetail = new ComboDetailBO();
			var tmp = str.replace(" ", "").split("x");
			if (tmp.length == 1) {
				comboDetail.ProductId = Utils.toInt(tmp[0]);
				comboDetail.Quantity = 1;
			} else {
				comboDetail.ProductId = Utils.toInt(tmp[1]);
				comboDetail.Quantity = Utils.toInt(tmp[0]);
			}

			var productdetail = getProductBOByProductID_PriceStrings(comboDetail.ProductId, 11, provinceID, "vi-VN",
					storeId);
			processPriceStrings(productdetail, provinceID, 11, storeId);

			var price = productdetail.ProductErpPriceBOList;
			ProductErpPriceBO priceprovince = new ProductErpPriceBO();
			if (price == null || price.length == 0) {
				// ghi log lai, thong tin detail null
				// Logs.LogSysMessage("", lstidstring);
				break;
			}
			if (storeId > 0) {
				priceprovince = Arrays.stream(price).filter(x -> x.ProvinceId == provinceID).findFirst().get();
			} else {
				var lsext = Arrays.stream(bhxStoreHelper.getListNormalStore()).boxed().collect(Collectors.toList());
				priceprovince = Arrays.stream(price).filter(y -> lsext.contains(y.StoreID))
						.max(Comparator.comparing(ProductErpPriceBO::getPrice)).get();
			}

			if (priceprovince == null) {
				break;
			}
			var quantitystr = redisCluster.Get(redisCluster.createKeyRedisStock(priceprovince.ProductCode,
					priceprovince.ProvinceId, priceprovince.StoreID));
			float quantity = 0;
			if (Utils.StringIsEmpty(quantitystr)) {
				quantity = 0;
			} else {
				quantity = Utils.toFloat(quantitystr);

			}
			comboDetail.ComboPrice = priceprovince.Price;
			comboDetail.ComboType = 1;
			comboDetail.Price = priceprovince.Price;
			comboDetail.ProductCode = priceprovince.ProductCode;
			comboDetail.StockQuantity = Utils.toInt(quantity) / comboDetail.Quantity;

			if (firstDate) {
				expiredDate = priceprovince.ArrivalDate;
				expiredDateDisplay = priceprovince.ProductArrivalDate;
				firstDate = false;
			}
			expiredDate = expiredDate.compareTo(priceprovince.ArrivalDate) > 0 ? priceprovince.ArrivalDate
					: expiredDate;
			expiredDateDisplay = expiredDateDisplay.compareTo(priceprovince.ProductArrivalDate) > 0
					? priceprovince.ProductArrivalDate
					: expiredDateDisplay;
			lscombodetail.add(comboDetail);

			// promtion
			if (productdetail.PromotionBHX == null || productdetail.PromotionBHX.size() == 0) {
				continue;
			}
			var promos = productdetail.PromotionBHX.stream().filter(x -> x.BHXPromotionType == BHXPromotionType.COMBO)
					.collect(Collectors.toList());
			if (promos == null || promos.isEmpty()) {
				continue;
			}
			var tmpstoreID = priceprovince.StoreID;
			var expStore = bhxStoreHelper.getStoreDetailByStore(tmpstoreID);
			if (expStore != null) {
				promos = promos.stream().filter(
						x -> x.StoreIds.equals(Utils.toString(tmpstoreID)) || x.StoreIds.equals(expStore.getExpStore()))
						.collect(Collectors.toList());
			}
			if (promos == null || promos.isEmpty()) {
				continue;
			}
			for (PromotionBHX promo : promos) {
				if (promo.QuantityCondition == comboDetail.Quantity) {
					promotiondetail.add(promo);
				}
			}
		}
		ProductErpPriceBO objectprice = new ProductErpPriceBO();
		if (lscombodetail != null && !lscombodetail.isEmpty()) {
			// tạo object giá con sp combo dựa trên sản phẩm lẻ
			var stockQuantity = lscombodetail.stream().min(Comparator.comparing(ComboDetailBO::getStockQuantity)).get()
					.getStockQuantity();
			objectprice.ProductCode = "WEB" + product.ProductLanguageBO.comboproductidlist;
			objectprice.WebStatusId = stockQuantity > 0 ? 4 : 5;
			objectprice.Price = lscombodetail.stream().mapToDouble(i -> i.getPrice()).sum();
			objectprice.HisPrice = 0;
			objectprice.ProductCodeTotalQuantity = stockQuantity;
			objectprice.Quantity = stockQuantity;
			objectprice.TotalQuantity = stockQuantity;
			objectprice.IsShowHome = true;
			objectprice.IsShowWeb = true;
			objectprice.ArrivalDate = expiredDate;
			objectprice.ProductArrivalDate = expiredDateDisplay;
		}
		product.ProductErpPriceBO = objectprice;

		// lấy km con lẻ
		if (promotiondetail != null && !promotiondetail.isEmpty()) {
			product.PromotionBHX = promotiondetail;
		}
		// set lại code
		product.ProductCode = "WEB" + product.ProductLanguageBO.comboproductidlist.replace(" ", "");

	}

	public ProductInstallment GetInstallmentByProduct(int productID, int siteID, String lang, long categoryID)
			throws Throwable {
		var data = factoryRead.QueryFunctionCached("installment_getByProductID", ProductInstallment[].class, false,
				productID, lang, siteID, (int) categoryID);
		return data != null && data.length > 0 ? data[0] : null;
	}

	public ProductInstallment GetInstallmentByProduct(int productID, int siteID, String lang, long categoryID,
			int manuID) throws Throwable {
		var data = factoryRead.QueryFunctionCached("installment_getByProductID_v2",
				ProductInstallment[].class, false, productID, siteID, lang, (int) categoryID, manuID);
		return Stream.of(data)
				.sorted(Comparator.<ProductInstallment>comparingInt(x -> -x.ProductID)
						.thenComparingInt(x -> Strings.isNullOrEmpty(x.ManuIDList) || x.ManuIDList.equals("0") ? 0 : 1))
				.findFirst().orElse(null);
	}

	public List<ProductInstallment> GetInstallmentByProductSE(int productID, int siteID, String lang, long categoryID,
													  int manuID) throws Throwable {
		var data = factoryRead.QueryFunctionCached("installment_getByProductID_se",
				ProductInstallment[].class, false, productID, siteID, lang, (int) categoryID, manuID);
		return Stream.of(data)
				.sorted(Comparator.<ProductInstallment>comparingInt(x -> -x.ProductID)
						.thenComparingInt(x -> Strings.isNullOrEmpty(x.ManuIDList) || x.ManuIDList.equals("0") ? 0 : 1))
				.collect(Collectors.toList());
//				.findFirst().orElse(null);
	}

	public ProductInstallment[] GetInstallmentByProduct(int[] productID, int siteID, String lang) throws Throwable {
		var data = factoryRead.QueryFunction("installment_getByProductIDList", ProductInstallment[].class, false,
				productID, lang, siteID);
		return data;
	}

	public String GenProductImageUrl(int categoryID, int productID, String filename) {

		String productImageCdn = "https://cdn.tgdd.vn/Products/Images";

		return productImageCdn + "/" + categoryID + "/" + productID + "/" + filename;
	}

	public ProductBO[] getListCoupleWatch(int productID, int siteID, int provinceID, String langID) throws Throwable {
		var q = boolQuery().must(termQuery("SiteID", siteID)).must(termQuery("PresentProductID", productID));
		var sb = new SearchSourceBuilder().query(q).from(0).size(2).fetchSource(new String[] { "ProductID" }, null);
		var sr = new SearchRequest().indices(CurrentIndexDB).source(sb);
		try {

			SearchResponse result = null;
//			var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//			var clientIndex1 = elasticClient1.getClient();
//			try {

				result = clientIndex.search(sr, RequestOptions.DEFAULT);
//			} catch (Throwable e) {
//
//				Logs.LogException(e);
//				throw e;
//			} finally {
//				elasticClient1.close();
//			}

			var ids = Stream.of(result.getHits().getHits()).mapToInt(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), ProductSO.class).ProductID;
				} catch (Exception e) {
					Logs.LogException(e);
					return -1;
				}
			}).filter(x -> x > 0).toArray();
			if (ids.length > 0) {
//				return IntStream.of(ids)
//						.mapToObj(
//								x -> GetProduct(x, siteID, provinceID, 0, langID, new CodeTimer(""), new CodeTimer("")))
//						.toArray(ProductBO[]::new);

				var list = GetSimpleProductListByListID_PriceStrings_soMap(ids, siteID, provinceID, langID);
				return list;
			} else
				return new ProductBO[0];
		} catch (IOException e) {
			Logs.LogException(e);
			return null;
		}
	}

	public Map<Integer, List<ProductBO>> getListCoupleWatch(int[] productIDs, int siteID, int provinceID, String langID)
			throws Throwable {
		var q = boolQuery().must(termQuery("SiteID", siteID)).must(termsQuery("PresentProductID", productIDs));
		var sb = new SearchSourceBuilder().query(q).size(1000).fetchSource(new String[] { "ProductID" }, null);
		var sr = new SearchRequest().indices(CurrentIndexDB).source(sb);
		try {

			SearchResponse result = null;
//			var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//			var clientIndex1 = elasticClient1.getClient();
//			try {

				result = clientIndex.search(sr, RequestOptions.DEFAULT);
//			} catch (Throwable e) {
//
//				Logs.LogException(e);
//				throw e;
//			} finally {
//				elasticClient1.close();
//			}

			var ids = Stream.of(result.getHits().getHits()).mapToInt(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), ProductSO.class).ProductID;
				} catch (Exception e) {
					Logs.LogException(e);
					return -1;
				}
			}).filter(x -> x > 0).toArray();
			if (ids.length > 0) {
//				return IntStream.of(ids)
//						.mapToObj(
//								x -> GetProduct(x, siteID, provinceID, 0, langID, new CodeTimer(""), new CodeTimer("")))
//						.toArray(ProductBO[]::new);
				ids = Arrays.copyOf(ids, ids.length + 1);
				// flag de khi goi ham somap, khong lay dong ho cap
				ids[ids.length - 1] = -9999;
				var arr = GetSimpleProductListByListID_PriceStrings_soMap(ids, siteID, provinceID, langID);
				var map = new HashMap<Integer, List<ProductBO>>();
				for (var p : arr) {
					var list = map.get(p.RepresentProductID);
					if (list == null) {
						list = new ArrayList<ProductBO>();
						map.put(p.RepresentProductID, list);
					}
					list.add(p);
				}
				return map;
			}
		} catch (IOException e) {
			Logs.LogException(e);
		}
		return new HashMap<>();
	}

	public ProductBO[] getListPreResentProduct(int productID, int siteID, int provinceID, String langID)
			throws Throwable {
		// hàm này chỉ dùng cho mỗi multi máy lạnh được thôi, bị dính rule
		// Multil product cate 2002 - máy lạnh
		// https://docs.google.com/document/d/1R6jBJXJNiT7LPFsQEc_69Uo5hJSijgJIY3JNj3rcsw8/edit#
		var q = boolQuery().must(termQuery("SiteID", siteID)).must(termQuery("PresentProductID", productID));
		// q.must(termQuery("Prices.WebStatusId_" + provinceID,8));
		var sb = new SearchSourceBuilder().query(q).from(0).size(20).fetchSource(new String[] { "ProductID" }, null);
		var sr = new SearchRequest().indices(CurrentIndexDB).source(sb);
		try {
			SearchResponse result = null;
//			var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//			var clientIndex1 = elasticClient1.getClient();
//			try {

				result = clientIndex.search(sr, RequestOptions.DEFAULT);
//			} catch (Throwable e) {
//
//				Logs.LogException(e);
//				throw e;
//			} finally {
//				elasticClient1.close();
//			}

			var ids = Stream.of(result.getHits().getHits()).mapToInt(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), ProductSO.class).ProductID;
				} catch (Exception e) {
					Logs.LogException(e);
					return -1;
				}
			}).filter(x -> x > 0).toArray();
			if (ids != null && ids.length > 0) {
				var list = GetSimpleProductListByListID_PriceStrings_soMap(ids, siteID, provinceID, langID);
				return list;
			} else
				return new ProductBO[0];
		} catch (IOException e) {
			Logs.LogException(e);
			return null;
		}
	}

	public ProductPropGrpBO[] GetListProductGroupByCategoryIDFromCach(long categoryID, int isGetAll, String lang,
			int siteID) throws Throwable {
		String key = "GetListProductGroupByCategoryIDFromCach" + siteID + "_" + categoryID + "_" + lang + "_"
				+ isGetAll;
		var rs = (ProductPropGrpBO[]) CacheStaticHelper.GetFromCache(key, 30);
		if (rs == null) {
			rs = GetListProductGroupByCategoryID(categoryID, isGetAll, lang, siteID);
			CacheStaticHelper.AddToCache(key, rs);
		}
		return rs;

	}

	public ProductPropGrpBO[] GetListProductGroupByCategoryID(long categoryID, int isGetAll, String lang, int siteID)
			throws Throwable {
		return factoryRead.QueryFunctionCached("product_propgrp_getByCateID", ProductPropGrpBO[].class, false,
				(int) categoryID, isGetAll, lang, siteID);

	}

	public void getComboPrice(ProductBO product, int siteID, String lang, int provinceID) {

		var q = boolQuery().must(termQuery("SiteID", siteID))
				.must(termQuery("Lang", lang.toLowerCase().replace("-", "_")))
				.must(termQuery("ProductID", product.ProductID));
		var sb = new SearchSourceBuilder().query(q).from(0).size(1).fetchSource(new String[] { "ProductID", "Prices" },
				null);
		var sr = new SearchRequest().indices(CurrentIndexDB).source(sb);
		try {

			SearchResponse result = null;
//			var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//			var clientIndex1 = elasticClient1.getClient();
//			try {

				result = clientIndex.search(sr, RequestOptions.DEFAULT);
//			} catch (Throwable e) {
//
//				Logs.LogException(e);
//				throw e;
//			} finally {
//				elasticClient1.close();
//			}

			var finalResult = result.getHits();
			var listProductSO = new ArrayList<ProductSO>();
			finalResult.forEach(h -> {
				try {

					var so = mapper.readValue(h.getSourceAsString(), ProductSO.class);
					listProductSO.add(so);

				} catch (Exception e) {
					Logs.LogException(e);
				}
			});
			if (listProductSO.size() > 0) {

				var prices = listProductSO.get(0).Prices;
				if (prices != null) {
					ProductErpPriceBO productErpPrice = new ProductErpPriceBO();
					productErpPrice.WebStatusId = Integer.valueOf(prices.get("WebStatusId_" + provinceID).toString());
					productErpPrice.Price = Double.valueOf(prices.get("Price_" + provinceID).toString());
					productErpPrice.IsShowHome = Integer.valueOf(prices.get("IsShowHome_" + provinceID).toString()) == 1
							? true
							: false;
					productErpPrice.ProductCode = String.valueOf(prices.get("ProductCode_" + provinceID).toString());

					product.ProductErpPriceBO = productErpPrice;

				}

			}

		} catch (IOException e) {
			Logs.LogException(e);

		}

	}

	public List<Integer> GetListProductBanKem(int productID) throws Throwable {
		List<Integer> listProductID = new ArrayList<>();

		var result = factoryRead.QueryFunction("accessory_getByProductID", ProductBO[].class, false, productID);

		if (result != null) {

			for (var item : result) {
				listProductID.add(item.ProductID);
			}
		}
		return listProductID;
	}

	public int[] getCachedProductBankem(int productID) throws Throwable {
		String typeKey = "productaccbankem";
		String objKey = String.valueOf(productID);
		int[] r = CachedObject.getObject(typeKey, objKey, 10, int[].class);
		if (r == null) {
			var result = factoryRead.queryFunction("accessory_getByProductID", ProductBO[].class, productID);
			r = Stream.of(result).mapToInt(x -> x.ProductID).filter(x -> x > 0).toArray();
			CachedObject.putObject(typeKey, objKey, r == null ? new int[0] : r);
		}
		return r;
	}

	// methods cho class con
//	public ProductSOSR SearchProduct(ProductQuery qry, boolean isGetFacetManu, boolean isGetFacetCate,
//			boolean isGetFacetProp, CodeTimer queryTimer, CodeTimer parseTimer) {
//		return null;
//	}

//	public void getPromotions(ProductBO product, ProductErpPriceBO def, int siteID) {
//	}

	public KeyWordBO[] GetListKeyWordByCateFromCache(long categoryID, int siteID) throws Throwable {
		String key = "GetListKeyWordByCateFromCache" + siteID + "_" + categoryID;
		var rs = (KeyWordBO[]) CacheStaticHelper.GetFromCache(key, 30);
		if (rs == null) {
			rs = GetListKeyWordByCate(categoryID, siteID);
			CacheStaticHelper.AddToCache(key, rs);
		}

		return rs;

	}

	public KeyWordBO[] GetListKeyWordByCate(long categoryID, int siteID) throws Throwable {
		return factoryRead.QueryFunction("keyword_getByCate", KeyWordBO[].class, false, (int) categoryID, siteID);
	}

	public ProductManuBO[] getManuByIDList(int[] idlist, int siteID, String langID) throws Throwable {
		return factoryRead.QueryFunction("manu_GetByIDList", ProductManuBO[].class, false, idlist, siteID, langID);
	}

	public ProductSliderBO[] getProductSlider(int productID, int siteID, String langID) throws Throwable {
		return factoryRead.QueryFunction("product_getSliderByProductID", ProductSliderBO[].class, false, productID,
				siteID, langID);
	}

	public ProductGallery360BO[] getProductGallery360(int productID, int siteID) throws Throwable {
		var r = factoryRead.QueryFunction("product_getGallery360", ProductGallery360BO[].class, false, productID,
				siteID);
		var pattern = Pattern.compile("^[a-z0-9-]+-([0-9]+).jpg");
		r = Arrays.stream(r).map(x -> {
			try {
				var m = pattern.matcher(x.Picture);
				if (m.find()) {
					x.number = Integer.parseInt(m.group(1));
				}
			} catch (Exception ignored) {
			}
			return x;
		}).sorted(Comparator.comparingInt(x -> x.number)).toArray(ProductGallery360BO[]::new);
		return r;
	}

	public PropList[] getProductDetail(int productID, int siteID, String langID) throws Throwable {
		return factoryRead.QueryFunction("product_GetProductDetail_v3", PropList[].class, false, productID, siteID,
				langID);
	}

	public Map<Long, ProductDetailBO[]> getProductDetailMap(int[] productIDs, int siteID, String langID)
			throws Throwable {
		var list = factoryRead.QueryFunction("product_GetProductDetailByListID", PropList[].class, false, productIDs,
				siteID, langID);
		if (list == null || list.length == 0)
			return new HashMap<>();
		var map = Stream.of(list).collect(Collectors.toMap(x -> x.productID, x -> x.productdetails));
		return map;
	}

	public ProductDetailBO[] getPropValue(int[] valueids, int siteID, String langID) throws Throwable {
		return factoryRead.QueryFunction("prop_GetValue", ProductDetailBO[].class, false, valueids, siteID, langID);
	}

	public ProductPropBO[] getPropTypeAndName(Set<Integer> propids, String langID) throws Throwable {
		return factoryRead.QueryFunction("prop_getTypeAndName", ProductPropBO[].class, false, propids, langID);
	}

	public SpecTemplateBO[] getListSpecTemplate(int siteID, String langID) throws Throwable {
		return factoryRead.QueryFunctionCached("spec_template_GetAll", SpecTemplateBO[].class, false, siteID, langID);
	}

	public TemplateRating[] getProductRating(int productID, int price, int siteID) throws Throwable {
		return factoryRead.QueryFunction("product_ratingGetByProductdid", TemplateRating[].class, false, productID,
				price, siteID);
	}

	public int[] getNextAndPrevGenerationOfProduct(int productID) throws Throwable {
		var result = factoryRead.QueryFunctionCached("product_getPrenext", ProductBO[].class, false, productID);
		return Stream.of(result).mapToInt(x -> x.ProductID).toArray();
	}

	public DeliveryTime GetDeliverytimeProvinceByProvinceID(int provinceID) throws Throwable {
		var result = factoryRead.QueryFunction("deliverytimeProvinceGetByID", DeliveryTime[].class, false, provinceID);
		return result == null || result.length == 0 ? null : result[0];
	}

	public ProductBO[] getListAllAccessory(int collectionID, int siteID) throws Throwable {
		var result = factoryRead.QueryFunctionCached("product_langGetByCollectionID", ProductBO[].class, false,
				collectionID, siteID);
//		var productItem = GetProductBOByProductID(collectionID, siteID, siteID == 6 ? 163 : 3,
//				siteID == 6 ? "km-KH" : "vi-VN", -1);
		if (result != null && result.length > 0) {
			var ids = Stream.of(result).mapToInt(x -> x.ProductID).toArray();
			var list = Stream.of(getProductsByIDList(ids, siteID)).filter(x -> {
				var y = x.ProductErpPriceBO;
				return y != null && y.WebStatusId != 1 && y.WebStatusId != 7 && y.WebStatusId != 0 && y.Price > 0;
			}).toArray(ProductBO[]::new);
			for (var product : list) {
				var colors = Stream.of(GetColorByProductID(product.ProductID, siteID == 6 ? "km-KH" : "vi-VN"))
						.filter(x -> x != null).map(x -> x.ColorCode).collect(Collectors.joining(","));
				product.ProductColorBO = new ProductColorBO() {
					{
						ColorName = colors;
					}
				};
			}
			return list;
		}
		return new ProductBO[0];
	}

	public ProductBO[] getFullAccessory(int productID, int siteID, int provinceID) throws Throwable {
		var ids = Stream
				.of(factoryRead.QueryFunctionCached("accessory_getByProductID", ProductBO[].class, false, productID))
				.mapToInt(x -> x.ProductID).toArray();
		ids = getSellingProducts(ids, siteID, provinceID);
		return Stream.of(getProductsByIDList_PriceStrings(ids, siteID, provinceID, siteID == 6 ? "km-KH" : "vi-VN"))
				.filter(x -> isProductSelling(x)).toArray(ProductBO[]::new);
	}

	/**
	 * Lọc ra những productID còn kinh doanh
	 *
	 * @throws IOException
	 */
	public int[] getSellingProducts(int[] productIDs, int siteID, int provinceID) throws IOException {
		var q = boolQuery()//
				.must(termsQuery("ProductID", productIDs))//
				.must(termQuery("SiteID", siteID))//
				.must(rangeQuery("Prices.Price_" + provinceID).gt(0))//
				.mustNot(termQuery("Prices.WebStatusId_" + provinceID, 7))//
				.must(rangeQuery("Prices.WebStatusId_" + provinceID).gt(1));
		var sb = new SearchSourceBuilder().fetchSource(new String[] { "_score", "ProductID" }, null).query(q);
		var sr = new SearchRequest().indices(CurrentIndexDB).source(sb);

		SearchResponse r = null;
// 	var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
// 	var clientIndex1 = elasticClient1.getClient();
//		try {

			r = clientIndex.search(sr, RequestOptions.DEFAULT);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		return Stream.of(r.getHits().getHits()).mapToInt(x -> {
			try {
				return esmapper.readValue(x.getSourceAsString(), ProductSO.class).ProductID;
			} catch (IOException e) {
				return -1;
			}
		}).filter(x -> x > 0).toArray();
	}

	public ProductBO[] getAccessory(int productID, int siteID, int provinceID) throws Throwable {
		var products = Stream.of(getFullAccessory(productID, siteID, provinceID)).filter(x -> {
			var price = x.ProductErpPriceBO;
			return price != null && price.WebStatusId != 1 && price.Price > 0;
		}).toArray(ProductBO[]::new);
		if (products.length < 4)
			return products;
		products = Stream.of(products).collect(Collectors.groupingBy(x -> x.CategoryID)).values().stream().findFirst()
				.orElse(new ArrayList<ProductBO>()).toArray(ProductBO[]::new);
		if (products.length > 4)
			products = Stream.of(products).limit(4).toArray(ProductBO[]::new);
		return products;
	}

	public boolean isProductSelling(ProductBO product) {
		var price = product.ProductErpPriceBO;
		return price != null && price.Price > 0 && price.WebStatusId > 1 && price.WebStatusId != 7;
	}

	public ProductBO[] getProductsByIDList(int[] ids, int siteID) throws Throwable {
		return factoryRead.QueryFunction("product_GetByIdListWithPriceDef", ProductBO[].class, false, ids, siteID,
				siteID == 6 ? 163 : 3, siteID == 6 ? "vi-VN" : "km-KH");
	}

	public ProductVideoShowBO[] GetProductVideoShowByProductID(int productID, String lang) throws Throwable {
		return factoryRead.QueryFunctionCached("product_getVideoshowByProductid", ProductVideoShowBO[].class, false,
				productID, lang);
	}

	public ProductBO[] getProductsByIDList(int[] ids, int siteID, int provinceID, String langID) throws Throwable {
		return factoryRead.QueryFunction("product_GetByIdListWithPriceDef", ProductBO[].class, false, ids, siteID,
				provinceID, langID);
	}

	public ProductBO[] getProductsByIDList_PriceStrings(int[] ids, int siteID, int provinceID, String langID)
			throws Throwable {
		var products = factoryRead.QueryFunction("product_GetByIdListWithPriceStrings", ProductBO[].class, false, ids,
				siteID, provinceID, langID, DidxHelper.getPriceAreaBySiteID(siteID, siteID == 6 ? "km-KH" : "vi-VN"));
		for (var x : products) {
			processPriceStrings(x, provinceID, siteID, 0);
		}
		return products;
	}

	public CachedDetails getFullProductDetailFromORCache(int productID, int siteID, String langID, boolean expand,
			int clearcache, CodeTimers timer) throws Throwable {

		timer.start("propdetail-odb");
		var master = getProductDetail(productID, siteID, langID);
		timer.pause("propdetail-odb");
		// ProductDetailBO[] list = getFullProductDetailList(productID, siteID, langID);
		ProductDetailBO[] list = getFullProductDetailListFromORDB(productID, siteID, langID);

		list = Stream.of(list).filter(x -> x.PropertyName != null).toArray(ProductDetailBO[]::new);
		if (siteID == 12 || siteID == 1 || expand)
			list = Stream.of(list).flatMap(ProductDetailBO::expandValues).toArray(ProductDetailBO[]::new);
		return new CachedDetails(list, master);

	}

	public InfoBO getHTMLInfo(int htmlID, int siteID, String langID) throws Throwable {
		var info = factoryRead.queryFunctionCached("html_info_GetByHtmlID", InfoBO[].class, htmlID, langID, siteID);
		return info != null && info.length > 0 ? info[0] : null;
	}

	public CachedDetails getFullProductDetail(int productID, int siteID, String langID, boolean expand,
			CodeTimers timer) throws Throwable {
		timer.start("propdetail-uncached");
		timer.start("propdetail-odb");
		var master = getProductDetail(productID, siteID, langID);
		timer.pause("propdetail-odb");
		List<ProductDetailBO> list;
		Map<Integer, ProductPropGrpBO> groups = null;
		if (master == null || master.length == 0) {
			list = new ArrayList<>();
			groups = new HashMap<>();
		} else {
			list = new ArrayList<>(Arrays.asList(master[0].productdetails));
			groups = master[0].groups == null ? null
					: master[0].groups.stream().collect(Collectors.toMap(x -> x.GroupID, x -> x));
			if (groups == null) {
				groups = new HashMap<>();
			}
			if (list == null) {
				list = new ArrayList<>();
			}
		}
		var propids = list.stream().map(x -> x.PropertyID).distinct().collect(Collectors.toSet());
		var mixStructure = getMixStructureProp(productID, siteID, langID);
		if (mixStructure != null) {
			for (int id : mixStructure) {
				propids.add(id);
				if (!list.stream().anyMatch(x -> x.PropertyID == id)) {
					var add = new ProductDetailBO();
					add.PropertyID = id;
					list.add(add);
				}
			}
		}
		var namesdb = getPropTypeAndName(propids, langID);
		var types = Arrays.stream(namesdb).collect(Collectors.toMap(x -> x.PropertyID, x -> x.PropertyType));
		var valueids = list.stream().filter(x -> x.Value != null && types.getOrDefault(x.PropertyID, 0) != 0)
				.flatMap(x -> Stream.of(x.Value.split(","))).filter(x -> x != null).mapToInt(x -> {
					try {
						return Integer.parseInt(x);
					} catch (Exception e) {
						return -1;
					}
				}).filter(x -> x > 0).toArray();
		timer.start("propdetail-odb");
		var td = getPropValue(valueids, siteID, langID);
		timer.pause("propdetail-odb");
		if (td != null) {
			var values = Stream.of(td).filter(x -> x.Value != null).collect(Collectors.groupingBy(x -> x.PropertyID));
			if (namesdb != null) {
				var names = Stream.of(namesdb).collect(Collectors.toMap(x -> x.PropertyID, x -> x));
				for (var x : list) {
					var name = names.get(x.PropertyID);
					names.remove(x.PropertyID);
					if (name != null && groups.containsKey(name.GroupID)) {
						x.PropertyType = name.PropertyType;
						x.PropertyName = name.PropertyName;
						x.IsShowSpecs = name.IsShowSpecs;
						x.PropertyDisplayOrder = name.DisplayOrder;
						x.PropIssearch = name.IsSearch;
						x.isAddUp = name.isAddUp;
						x.IsSearch = name.IsSearch;
						x.IsSearchDMX = name.IsSearchDMX;
						x.GroupID = name.GroupID;
						x.unitTextSE = name.unitTextSE;
						var group = groups.get(x.GroupID);
						if (group != null) {
							x.GroupName = group.GroupName;
							x.isSpecial = group.IsSpecial == 1;
						}
						x.dragFilter = name.dragFilter;
						x.isFeaturedCompare = name.isFeaturedCompare;
						x.PropUrl = siteID == 2 ? name.dmxurl : name.URL;
						x.MixStructure = name.MixStructure;
					} else
						x.PropertyName = null;

					var value = values.get(x.PropertyID);
					if (value != null) {
						x.Values = value.stream().filter(z -> z.Value != null).map(z -> z.Value).toArray(String[]::new);
						x.valuesObj = value.toArray(ProductDetailBO[]::new);
					} else
						x.Values = new String[] {};

					if (x.PropertyType == 2 && (x.Value == null || !(x.Value.startsWith(",") && x.Value.endsWith(","))))
						x.isInvalid = true;
				}
			}
		}
		var arr = list.stream().filter(x -> !x.isInvalid
				&& ((x.Values != null && x.Values.length > 0) || x.PropertyType == 0) && x.PropertyName != null)
				.toArray(ProductDetailBO[]::new);
		if (siteID != 6 || expand)
			arr = Stream.of(arr).flatMap(ProductDetailBO::expandValues).toArray(ProductDetailBO[]::new);
		CachedDetails cached = new CachedDetails(arr, master);
		timer.pause("propdetail-uncached");
		return cached;
	}

	public int[] getMixStructureProp(int productID, int siteID, String languageID) throws Throwable {
		var or = factoryRead.queryFunctionCached("prop_getMixStructureProp", ORIntArrWrapper[].class, productID, siteID,
				languageID);
		int[] result = or == null || or.length == 0 || or[0].intresult == null ? new int[0] : or[0].intresult;
		return result;
	}

	public CachedDetails getCachedProductDetail(int productID, int siteID, String langID, boolean expand,
			int clearcache, CodeTimers timer) throws Throwable {
		String keyT = "product_detail";
		String keyO = productID + "_" + siteID + "_" + langID + "_" + expand;
		CachedDetails cached = null;
		if (clearcache == 1 || (cached = CachedObject.getObject(keyT, keyO, 10, CachedDetails.class)) == null) {
			cached = getFullProductDetail(productID, siteID, langID, expand, timer);
			CachedObject.putObject(keyT, keyO, cached);
		}
		return cached;
	}

	public Map<Integer, ProductDetailBO[]> getODBCachedDetail(int[] productID, int siteID, String langID)
			throws Throwable {
		String[] recordID = Arrays.stream(productID).mapToObj(x -> x + "_" + siteID + "_" + DidxHelper.GenTerm(langID))
				.toArray(String[]::new);
		IntStringKVObject[] r1 = factoryRead.queryFunction("product_GetCachedDetails", IntStringKVObject[].class,
				recordID, 0);
		return Arrays.stream(r1).collect(Collectors.toMap(x -> x.key, x -> {
			try {
				return Arrays.stream(mapper.readValue(x.value, ProductDetailBO[].class)).filter(y -> y != null)
						.toArray(ProductDetailBO[]::new);
			} catch (IOException e) {
				return null;
			}
		}));
	}

	public ProductDetailBO[] getFullProductDetailListFromORDB(int productID, int siteID, String langID)
			throws Throwable {
		String recordid = productID + "_" + siteID + "_" + DidxHelper.GenTerm(langID);
		String data = factoryRead.QueryScalar("select data from product_detail_cache where recordid='" + recordid + "'",
				"data");
		ProductDetailBO[] ra = mapper.readValue(data, ProductDetailBO[].class);
		return ra;
	}

//	public ProductDetailBO[] parseDetail(ProductBO productBO, String languageID, boolean expand, CodeTimers timer)
//			throws Throwable {
//		if (productBO == null) {
//			return new ProductDetailBO[0];
//		}
//		int productID = productBO.ProductID;
//		int siteID = productBO.SiteID;
//
//		if (productID <= 0 || siteID <= 0) {
//			return new ProductDetailBO[0];
//		}
//
//		if (productBO.cachedDetail == null || Strings.isNullOrEmpty(productBO.cachedDetail.data)) {
//			var cached = getFullProductDetail(productID, siteID, languageID, expand, 0, timer);
//			return cached.list;
//		}
//
//		var list = new ProductDetailBO[0];
//
//		timer.start("propdetail-parse");
//		list = mapper.readValue(productBO.cachedDetail.data, ProductDetailBO[].class);
//		timer.pause("propdetail-parse");
//
//		return list;
//	}

	public int[] GetListRepresentProduct(int representID, int provinceID, int siteID, String langID) {

		int[] listProductID = null;
		var q = boolQuery().must(termQuery("SiteID", siteID))
				.must(termQuery("Lang", langID.toLowerCase().replace("-", "_")))
				.must(termQuery("PresentProductID", representID))
				.must(termQuery("HasBimage", 1))
				.must(termQuery("IsDeleted", 0))
				// .must(termsQuery("Prices.WebStatusId_3", new int[] { 2, 3, 4, 6, 8, 9, 11,
				// 99, 98 }))
				.must(boolQuery().should(
						termsQuery("Prices.WebStatusId_" + provinceID, new int[] { 2, 3, 4, 6, 8, 9, 11, 99, 98 }))
						.should(termQuery("CmsProductStatus", 1)));
		try {
			var sb = new SearchSourceBuilder().query(q).from(0).size(10)
					.fetchSource(new String[] { "ProductID", "Prices" }, null);
			var sr = new SearchRequest().indices(CurrentIndexDB).source(sb);

			SearchResponse result = null;
//			var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//			var clientIndex1 = elasticClient1.getClient();
//			try {

				result = clientIndex.search(sr, RequestOptions.DEFAULT);

//			} catch (Throwable e) {
//
//				Logs.LogException(e);
//				throw e;
//			} finally {
//				elasticClient1.close();
//			}

			listProductID = Stream.of(result.getHits().getHits()).mapToInt(x -> {
				try {
					return esmapper.readValue(x.getSourceAsString(), ProductSO.class).ProductID;
				} catch (Exception e) {
					Logs.LogException(e);
					return -1;
				}
			}).filter(x -> x > 0).toArray();
		} catch (Throwable e) {
			Logs.LogException(e);
		}
		return listProductID;
	}

	public int IsRepresentProduct(int productID, int siteID, String langID) throws Throwable {
		List<ProductSO> listProductID = null;
		var q = boolQuery().must(termQuery("SiteID", siteID))
				.must(termQuery("Lang", langID.toLowerCase().replace("-", "_")))
				.must(termQuery("ProductID", productID));
		q.must(termQuery("IsCollection", 0));

		q.must(scriptQuery(new Script(
				" (doc['CategoryID'].value == 42 && doc['ManufactureID'].value == 80 && doc['HasBimage'].value >= 0) || ( doc['HasBimage'].value > 0) ")));
		// q.must(termQuery("HasBimage", 1));
		var sb = new SearchSourceBuilder().query(q).from(0).size(1)
				.fetchSource(new String[] { "ProductID", "PresentProductID" }, null);
		var sr = new SearchRequest().indices(CurrentIndexDB).source(sb);
		try {

			SearchResponse result = null;
//			var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//			var clientIndex1 = elasticClient1.getClient();
//			try {

				result = clientIndex.search(sr, RequestOptions.DEFAULT);

//			} catch (Throwable e) {
//
//				Logs.LogException(e);
//				throw e;
//			} finally {
//				elasticClient1.close();
//			}

			listProductID = Stream.of(result.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), ProductSO.class);
				} catch (IOException e1) {
					return null;
				}
			}).collect(Collectors.toList());

		} catch (Throwable e) {
			Logs.LogException(e);
			throw e;
		}
		return listProductID != null && listProductID.size() > 0 ? listProductID.get(0).PresentProductID : 0;
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}

	public List<CMSPromotion> GetCMSPromotions(int categoryID, int manufactureID, int productID, int siteID,
			CodeTimer codetimer, CodeTimer odbtimer) throws Throwable {

		int defaultCateType = 1;
		int defaultManuType = 2;
		int defaultProductType = 3;
		var promotionByCate = factoryRead.QueryFunction("p_PromoprogramProGetByType1", CMSPromotion[].class, false,
				defaultCateType, categoryID);
		if (promotionByCate == null) {
			promotionByCate = new CMSPromotion[0];
		}
		var promotionByManu = factoryRead.QueryFunction("p_PromoprogramProGetByType2", CMSPromotion[].class, false,
				defaultManuType, manufactureID);
		if (promotionByManu == null) {
			promotionByManu = new CMSPromotion[0];
		}
		var promotionByProduct = factoryRead.QueryFunction("p_PromoprogramProGetByType3", CMSPromotion[].class, false,
				defaultProductType, productID);

		if (promotionByProduct == null) {
			promotionByProduct = new CMSPromotion[0];
		}

		List<CMSPromotion> allPromotion = new ArrayList<>();

		allPromotion.addAll(Arrays.asList(promotionByCate));
		allPromotion.addAll(Arrays.asList(promotionByManu));
		allPromotion.addAll(Arrays.asList(promotionByProduct));
		return allPromotion.stream().filter(distinctByKey(CMSPromotion::getProgramID)).collect(Collectors.toList());

	}

	public ProductBO[] GetRelativeProduct(ProductBO product, int provinceID, int siteID, String lang, int propertyID,
			int propertyValueID, int property2ID, int property2ValueID, double price) throws Throwable {

		var listID = getHelperBySite(siteID).GetRelativeProductIDs(product, provinceID, siteID, lang, propertyID,
				propertyValueID, property2ID, property2ValueID, price, true);

		// trong trường hợp ko đủ 6 sp
		if (listID.length < 6 && product.ProductCategoryBO != null && product.ProductCategoryBO.IsSuggestManu == 1) {
			product.ProductCategoryBO.IsSuggestManu = 0;
			if (AccessoryCategory.contains(product.CategoryID)) {
				propertyID = 0;
				propertyValueID = 0;
			}
			listID = ArrayUtils.addAll(listID, getHelperBySite(siteID).GetRelativeProductIDs(product, provinceID,
					siteID, lang, propertyID, propertyValueID, property2ID, property2ValueID, price, true));
		}

		ProductBO[] result = null;

		if (listID.length > 0) {
			result = GetSimpleProductListByListID_PriceStrings_soMap(listID, siteID, provinceID, lang);
		}

		return result;
	}

	public ShockPriceBO GetShockPriceInfo(int productID, int siteID) throws Throwable {

		var shockPrie = factoryRead.QueryFunction("shockprice_getByProdID", ShockPriceBO[].class, false, productID,
				siteID, -1, 1);

		if (shockPrie != null && shockPrie.length > 0) {
			return shockPrie[0];
		}
		return null;
	}

	public List<ShockPriceDiscountBO> GetListShockPriceDiscount(int shockPriceID) throws Throwable {
		var shockPrice = factoryRead.QueryFunction("shockprice_getByShockPriceID", ShockPriceDiscountBO[].class, false,
				shockPriceID);
		if (shockPrice != null && shockPrice.length > 0) {
			return Arrays.asList(shockPrice);
		}
		return null;
	}

	public ProductBO GetProductInfoForStatusDMXFromCache(long productID, int siteID, String lang) throws Throwable {
		if (GConfig.ProductTaoLao.containsKey(productID))
			return null;

		String key = "" + productID + "-" + siteID + "-" + lang;
		ProductBO rs = null;
		if (g_listProductDMX.containsKey(key)) {
			rs = g_listProductDMX.get(key);

			if (System.currentTimeMillis() - rs.api_MemCacheDate > 30 * 1000) {
				g_listProductDMX.remove(key);
			} else {
				rs.api_MemCacheSource = "Memcached";
				return rs;

			}
		}
		rs = GetProductInfoForStatusDMX(productID, siteID, lang);
		if (rs == null)
			return rs;
		rs.api_MemCacheDate = System.currentTimeMillis();
		g_listProductDMX.put(key, rs);
		rs.api_MemCacheSource = "ORDB";
		return rs;

	}

	public ProductBO GetProductInfoForStatusDMX(long productID, int siteID, String lang) throws Throwable {

		// g_listProductDMX
		var product = factoryRead.QueryFunctionCached("product_GetProductInfoForStatusDMX", ProductBO[].class, false,
				productID, siteID, lang);

		if (product != null && product.length > 0) {
			return product[0];

		}
		return null;
	}

	public ProductBO[] getPricePromoByID(int productID, int siteID, int provinceID, String langID) throws Throwable {
		var products = factoryRead.QueryFunction("product_getPricePromoByID", ProductBO[].class, false, productID,
				siteID, provinceID, langID);
		if (products == null)
			return new ProductBO[0];
		for (ProductBO product : products) {
			var isAccessory = AccessoryCategory.contains((int) product.CategoryID);
			getDefaultPriceAndPromotion(siteID, langID, product, isAccessory, true, null);
		}
		return products;
	}

	public Pm_ProductBO GetPm_ProductBO(String prductCode) throws Throwable {
		Pm_ProductBO[] rs = factoryRead.QueryFunction("pm_product_getInfo", Pm_ProductBO[].class, false,
				"" + prductCode);
		if (rs != null && rs.length > 0) {
			return rs[0];
		}
		return null;
	}

	public Pm_ProductBO GetPm_ProductFromCache(String prductCode) throws Throwable {
		String key = "pm_product_" + prductCode;
		var rs = (Pm_ProductBO) CacheStaticHelper.GetFromCache(prductCode, 100);
		if (rs == null) {
			rs = GetPm_ProductBO(prductCode);
			CacheStaticHelper.AddToCache(key, rs);
		}
		return rs;
	}

	public SpecialSaleProgramBO getSpecialSaleProgram(String productCode, int inventorystatusid) throws Throwable {
		Pm_ProductBO codeInfo = GetPm_ProductFromCache(productCode);
		if (codeInfo != null) {
			SpecialSaleProgramBO sale = SpecialsaleProgramHelper.getInstance(factoryRead, config)
					.GetSpecialsaleProgram(productCode, codeInfo.brandid, inventorystatusid, codeInfo.subgroupid);
			if (sale != null) {

				sale.IsSetupProduct = codeInfo.issetupproduct > 0 ? true : false;
				return sale;
			}

		}
		return null;
	}

	public SpecialSaleProgramBO getSpecialSaleProgram2(Pm_ProductBO codeInfo, String productCode, int inventorystatusid)
			throws Throwable {
		if (codeInfo != null) {
			SpecialSaleProgramBO sale = SpecialsaleProgramHelper.getInstance(factoryRead, config)
					.GetSpecialsaleProgram(productCode, codeInfo.brandid, inventorystatusid, codeInfo.subgroupid);
			if (sale != null) {
				sale.IsSetupProduct = codeInfo.issetupproduct > 0 ? true : false;
				return sale;
			}

		}
		return null;
	}

	public List<PriceParamsBO> GetPriceParams(int maingroupid) throws Throwable {

		return PriceParameterHelper.getInstance(factoryRead, config).GetPriceParams(maingroupid);

	}

	public boolean ClearCacheSpecialSaleProgram(String table) throws Throwable {

		SpecialsaleProgramHelper.getInstance(factoryRead, config).LoadAll(table);
		;
		return true;
	}

	public SpecialSaleProgramBO getSpecialSaleProgramCU(String productCode, int inventoryStatusID) throws Throwable {
		var sale = factoryRead.QueryFunction("GetSpecialsaleprogram", SpecialSaleProgramBO[].class, false, productCode,
				inventoryStatusID);
		return sale == null || sale.length == 0 ? null : sale[0];
	}

	public DistrictBO[] getDistrictByStock(String productCode, int provinceID, int siteID) throws Throwable {
		var stocks = factoryRead.QueryFunction("stock_getDisctrictByProvince", StockDistrictBO[].class, false,
				productCode, provinceID, siteID);
		int[] ids = Stream.of(stocks).filter(x -> x.quantity + x.sampleQuantity > 0).mapToInt(x -> x.districtID)
				.toArray();
		var districts = factoryRead.QueryFunction("district_GetByListID", DistrictBO[].class, false, ids);
		return districts == null ? new DistrictBO[0] : districts;
	}

	public ProvinceBO[] getProvinceByStock(String productCode, int siteID) throws Throwable {
		var stocks = factoryRead.QueryFunction("stock_getProvinceByCode", StockProvinceBO[].class, false, productCode,
				siteID);
		int[] ids = Stream.of(stocks).filter(x -> x.quantity + x.sampleQuantity > 0).mapToInt(x -> x.provinceID)
				.toArray();
		var provinces = factoryRead.QueryFunction("province_GetByListID", ProvinceBO[].class, false, ids);
		return provinces == null ? new ProvinceBO[0] : provinces;
	}

	public int[] getProvinceIDByStockDMX(String productCode) throws Throwable {
		var stocks = factoryRead.queryFunction("stock_getProvinceByCode", StockProvinceBO[].class,
				productCode, 2);
		return Stream.of(stocks).filter(x -> x.quantity > 0 || x.centerQuantity > 0).mapToInt(x -> x.provinceID)
				.toArray();
	}

	public int[] getProvinceIDByStockCam(String productCode, boolean sample) throws Throwable {
		var stocks = factoryRead.queryFunction("stock_getProvinceByCode", StockProvinceBO[].class,
				productCode, 6);
		return Stream.of(stocks).filter(x -> x.quantity > 0 || (sample && (x.centerQuantity > 0 || x.sampleQuantity > 0
				|| x.oldSamplequantity > 0))).mapToInt(x -> x.provinceID).toArray();
	}

	public ProductDetailBO[] getDetailsByProductIDs(long[] productIDs, String languageID) throws Throwable {
		return factoryRead.QueryFunction("detail_ValuesByProductIDList", ProductDetailBO[].class, false, productIDs,
				languageID);
	}

	public Map<Long, String> getCapacities(long[] productIDs, int siteID, String languageID) throws Throwable {
		var details = getDetailsByProductIDs(productIDs, languageID);
		var propids = Stream.of(details).mapToInt(x -> x.PropertyID).toArray();
		var addupdetails = Stream.of(
				factoryRead.QueryFunction("detail_FilterIsAddUp", ProductDetailBO[].class, false, propids, languageID))
				.collect(Collectors.toMap(x -> x.PropertyID, x -> x.PropertyType));
		var valueids = new ArrayList<Integer>();
		details = Stream.<ProductDetailBO>of(details).filter(x -> addupdetails.containsKey(x.PropertyID)).map(x -> {
			var y = addupdetails.get(x.PropertyID);
			x.isAddUp = true;
			x.PropertyType = y;
			if (y != 0)
				valueids.addAll(Stream.of(StringUtils.strip(x.Value, ",").split(",")).map(z -> {
					try {
						return Integer.parseInt(z);
					} catch (NumberFormatException e) {
						return -1;
					}
				}).distinct().filter(z -> z > 0).collect(Collectors.toList()));
			return x;
		}).toArray(ProductDetailBO[]::new);
		var values = valueids.size() > 0
				? Stream.of(getPropValue(valueids.stream().mapToInt(x -> x).toArray(), siteID, languageID)).collect(
						Collectors.groupingBy(x -> x.PropertyID))
				: null;
		return Stream.of(details).map(x -> {
			if (values != null && values.containsKey(x.PropertyID))
				x.Value = values.get(x.PropertyID).get(0).Value;
			return x;
		}).collect(Collectors.toMap(x -> x.ProductID, x -> Optional.ofNullable(x.Value).orElse("")));
	}

	public double getExactPrice(String productCode, int siteID, int provinceID, String languageID) throws Throwable {
		var prices = factoryRead.QueryFunction("product_GetExactPriceByProductCode", ProductErpPriceBO[].class, false,
				productCode, siteID, provinceID, languageID);
		return prices != null && prices.length > 0 ? prices[0].Price : -1f;
	}

	public ProductErpPriceBO getExactPrice(long productID, int siteID, int provinceID, String languageID)
			throws Throwable {
		var prices = factoryRead.queryFunction("product_getExactPriceByProductID", ProductErpPriceBO[].class, productID,
				siteID, provinceID, languageID);
		return prices != null && prices.length > 0 ? prices[0] : null;
	}

	public InstallmentBO getPaymentByID(int paymentID) throws Throwable {
		var payments = factoryRead.queryFunctionCached("installment_getByPaymentID", InstallmentBO[].class, paymentID);
		var payment = payments == null || payments.length <= 0 ? null : payments[0];
		if (payment != null) {
			var paymentType = factoryRead.queryFunctionCached("installment_getPaymenttype", PaymentTypeBO[].class,
					payment.paymentType);
			if (paymentType != null && paymentType.length > 0) {
				payment.Note = paymentType[0].moreDescription;
				payment.Approvedtime = paymentType[0].approvedTime;
			}
			if (payment.listProvinceIDStr != null) {
				payment.ListProvinceID = Stream.of(payment.listProvinceIDStr.split(",")).map(x -> x.trim())
						.filter(x -> !Strings.isNullOrEmpty(x)).collect(Collectors.toList());
			}
			if (payment.SiteIDList != null) {
				payment.siteIDListArr = Stream.of(payment.SiteIDList.split(",")).mapToInt(x -> {
					try {
						return Integer.parseInt(x.trim());
					} catch (NumberFormatException e) {
						return -1;
					}
				}).filter(x -> x > 0).toArray();
			}
		}
		return payment;
	}

	public List<InstallmentBO> getListNormalInstallment(int siteID, int categoryID, double price, int companyID,
			int percent, int month, int briefID, int productID, int inventoryStatusID, int isDefault, String languageID)
			throws Throwable {
		var query = boolQuery();
		if (siteID > 0)
			query.must(termQuery("siteIDListArr", siteID));
		if (categoryID > 0)
			query.must(termQuery("CategoryID", categoryID));
		if (price > 0) {
			query.must(rangeQuery("PriceFrom").lte(price));
			query.must(boolQuery()//
					.should(rangeQuery("PriceTo").gte(price))//
					.should(rangeQuery("fromSimPrice").gte(price))//
					.should(termQuery("PriceTo", -1))//
			);
		}
		if (companyID > 0)
			query.must(termQuery("paymentType", companyID));
		if (percent > 0) {
			query.must(rangeQuery("PaymentPercentFrom").lte(percent));
			query.must(rangeQuery("PaymentPercentTo").gte(percent));
		}
		if (month > 0)
			query.must(termQuery("PaymentMonth", month));
		if (briefID > 0)
			query.must(termQuery("BriefId", briefID));
		if (productID > 0)
			query.must(termQuery("ProductId", productID));
		if (isDefault > 0)
			query.must(termQuery("isDefault", isDefault));
		Date now = new Date();
		query.must(rangeQuery("FromDate").lte(now));
		query.must(rangeQuery("ToDate").gte(now));
		query.must(termQuery("isApplyForCate", 0));
		query.must(termQuery("isDeleted", false));
		var sb = new SearchSourceBuilder().from(0).size(100).query(query);
		var request = new SearchRequest("ms_payment").source(sb);

		SearchResponse result = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			result = clientIndex.search(request, RequestOptions.DEFAULT);

//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		return Stream.of(result.getHits().getHits()).map(x -> {
			try {
				return esmapper.readValue(x.getSourceAsString(), InstallmentBO.class);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}).filter(x -> x != null).collect(Collectors.toList());
	}

	public List<InstallmentBO> getAllZeroFeatureInstallment(int siteID, int productID, int isDefaultPackage,
			int inventoryStatusID) throws Throwable {
		var list = getListNormalInstallment(siteID, -1, -1, -1, -1, -1, -1, productID, inventoryStatusID,
				isDefaultPackage, siteID == 6 ? "km-KH" : "vi-VN");
		if (list == null || list.size() == 0)
			return null;
		return list.stream().filter(x -> x.ToDate.after(new Date()) && x.paymentType != 6).collect(Collectors.toList());
	}

	// ham1
	public InstallmentBO getInstallmentResult(int siteID, int CategoryId, double Price, int CompanyId, int Percent,
			int Month, int BriefId, String ListDealId, int ProductId, double CollectionFee, boolean isDefault)
			throws Throwable {
		if (Month <= 0 || Percent < 0)
			return null;

		int iCategoryIDGetData = !Strings.isNullOrEmpty(ListDealId) ? CategoryId : -1;

		InstallmentBO objInstall = null;
		double decInstallmentMoney = 0;
//        #region Số tiền trả trước và khoản vay
		double decPrepaid = 0;
		int iZeroPercent = 0;
		if (ProductId > 0) {
//            #region Nếu trả góp 0% thì lấy số tiền trả trước tại đây -> Tính ra % mới

			var tmpInstallList = getListNormalInstallment(siteID, iCategoryIDGetData, 0, CompanyId, 0, 0, 0, 0, 0, 0,
					"vi-VN");
			InstallmentBO TmpinstallmentBO = null;
			if (tmpInstallList != null && tmpInstallList.size() > 0)
				TmpinstallmentBO = tmpInstallList.stream().filter(x -> x.ProductId == ProductId).findFirst()
						.orElse(null);
			if (TmpinstallmentBO != null && TmpinstallmentBO.PrepaymentAmount > 0) {
				decPrepaid = TmpinstallmentBO.PrepaymentAmount;
				Percent = 0; // Truong hop nay tra 0%
				var tmpPercent = (decPrepaid * 100.0) / Price;
				if (tmpPercent > 0)
					iZeroPercent = (int) tmpPercent;
			} else {
				iZeroPercent = Percent;
				decPrepaid = Price * (iZeroPercent / 100.0);
			}
//            #endregion
		} else {
			decPrepaid = Price * (Percent / 100.0);
		}

		double decLoan = Price - decPrepaid; // so tien vay thuc te

		int cateIDGetQuiData = iCategoryIDGetData;
		if (ProductId <= 0 && iCategoryIDGetData == -1) {
			// lay data truong hop thuong tu database:
			cateIDGetQuiData = -2;
		}

		// Xu cho truong hop trả trước 0đ bình thường***
		List<Integer> vtl = Arrays.asList(42, 44, 522, 1882);// vien thong
		if (ProductId <= 0 && Percent == 0 && !vtl.contains(CategoryId)) {
			iCategoryIDGetData = -3;// dung loc data
		}
//        #endregion

		List<InstallmentBO> lstInstallment = null;
		// Lấy list có brief 1 và 2 nếu là HC và chọn CMND + HK
		if (CompanyId == 1 && BriefId == 1) {
			// rieng cho HC:
			lstInstallment = getListNormalInstallment(siteID, cateIDGetQuiData, Price, CompanyId, Percent, Month, -1,
					ProductId, 0, 0, "vi-VN");
			if (lstInstallment != null && lstInstallment.size() > 0)
				lstInstallment = lstInstallment.stream().filter(p -> p.BriefId == 1 || p.BriefId == 2)
						.collect(Collectors.toList());
		} else if (CompanyId == 1 && BriefId == 4) {
			// HC chuyển giấy tờ 4 thành 6
			lstInstallment = getListNormalInstallment(siteID, cateIDGetQuiData, Price, CompanyId, Percent, Month, -1,
					ProductId, 0, 0, "vi-VN");
			if (lstInstallment != null && lstInstallment.size() > 0)
				lstInstallment = lstInstallment.stream().filter(p -> p.BriefId == 6).collect(Collectors.toList());
		} else {
			lstInstallment = getListNormalInstallment(siteID, cateIDGetQuiData, Price, CompanyId, Percent, Month,
					BriefId, ProductId, 0, 0, "vi-VN");
		}

		if (lstInstallment == null || lstInstallment.size() == 0)
			return null;
//		objResultMessageBO = objInstallmentBLL.ResultMessageBO;

		// xe truong hop binh thuong hoac lãi suất đặc biệt 0%, 1%:

		final int percentf = Percent;
		final int categoryIDf = iCategoryIDGetData;
		if (ProductId > 0) {
			// truong hợp đặc biệt:
			if (CompanyId == 1 && BriefId == 1) {
				// rieng cho HC
				lstInstallment = lstInstallment.stream()
						.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 1 || p.BriefId == 2)
								&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
								&& (percentf >= p.PaymentPercentFrom && percentf <= p.PaymentPercentTo)
								&& Month == p.PaymentMonth && p.ProductId > 0)
						.collect(Collectors.toList());

				if (lstInstallment != null && lstInstallment.size() > 0)
					lstInstallment = lstInstallment.stream().sorted(Comparator.comparingInt(x -> x.BriefId))
							.collect(Collectors.toList());
			} else if (CompanyId == 1 && BriefId == 4) {
				// HC chuyển giấy tờ 4 thành 6
				lstInstallment = lstInstallment.stream()
						.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 6)
								&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
								&& (percentf >= p.PaymentPercentFrom && percentf <= p.PaymentPercentTo)
								&& Month == p.PaymentMonth && p.ProductId > 0)
						.collect(Collectors.toList());
			} else {
				lstInstallment = lstInstallment.stream()
						.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && p.BriefId == BriefId
								&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
								&& (percentf >= p.PaymentPercentFrom && percentf <= p.PaymentPercentTo)
								&& Month == p.PaymentMonth && p.ProductId > 0)
						.collect(Collectors.toList());
			}
		} else {
			// trường hợp thường
			if (isDefault) {
				if (CompanyId == 1 && BriefId == 1) {
					// rieng cho HC:
					lstInstallment = lstInstallment.stream().filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo)
							&& (p.BriefId == 1 || p.BriefId == 2) && (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
							&& (percentf >= p.PaymentPercentFrom && percentf <= p.PaymentPercentTo)
							&& Month == p.PaymentMonth && p.ProductId <= 0 && p.CategoryID == categoryIDf
							&& p.IsDefaultPackage == 1).collect(Collectors.toList());

					if (lstInstallment != null && lstInstallment.size() > 0)
						lstInstallment = lstInstallment.stream().sorted(Comparator.comparingInt(x -> x.BriefId))
								.collect(Collectors.toList());
				} else if (CompanyId == 1 && BriefId == 4) {
					// HC chuyển giấy tờ 4 thành 6
					lstInstallment = lstInstallment.stream()
							.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 6)
									&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
									&& (percentf >= p.PaymentPercentFrom && percentf <= p.PaymentPercentTo)
									&& Month == p.PaymentMonth && p.ProductId <= 0 && p.CategoryID == categoryIDf
									&& p.IsDefaultPackage == 1)
							.collect(Collectors.toList());
				} else {
					lstInstallment = lstInstallment.stream()
							.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && p.BriefId == BriefId
									&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
									&& (percentf >= p.PaymentPercentFrom && percentf <= p.PaymentPercentTo)
									&& Month == p.PaymentMonth && p.ProductId <= 0 && p.CategoryID == categoryIDf
									&& p.IsDefaultPackage == 1)
							.collect(Collectors.toList());
				}
			} else {
				if (CompanyId == 1 && BriefId == 1) {
					// rieng cho HC:
					lstInstallment = lstInstallment.stream().filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo)
							&& (p.BriefId == 1 || p.BriefId == 2) && (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
							&& (percentf >= p.PaymentPercentFrom && percentf <= p.PaymentPercentTo)
							&& Month == p.PaymentMonth && p.ProductId <= 0 && p.CategoryID == categoryIDf)
							.collect(Collectors.toList());
					if (lstInstallment != null && lstInstallment.size() > 0)
						lstInstallment = lstInstallment.stream().sorted(Comparator.comparingInt(x -> x.BriefId))
								.collect(Collectors.toList());
				} else if (CompanyId == 1 && BriefId == 4) {
					// HC chuyển giấy tờ 4 thành 6
					lstInstallment = lstInstallment.stream()
							.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 6)
									&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
									&& (percentf >= p.PaymentPercentFrom && percentf <= p.PaymentPercentTo)
									&& Month == p.PaymentMonth && p.ProductId <= 0 && p.CategoryID == categoryIDf)
							.collect(Collectors.toList());

				} else {
					lstInstallment = lstInstallment.stream()
							.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && p.BriefId == BriefId
									&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
									&& (percentf >= p.PaymentPercentFrom && percentf <= p.PaymentPercentTo)
									&& Month == p.PaymentMonth && p.ProductId <= 0 && p.CategoryID == categoryIDf)
							.collect(Collectors.toList());

				}
			}

		}

		if (lstInstallment == null || lstInstallment.size() == 0)
			return null;

		try {
			for (InstallmentBO item : lstInstallment) {
				if (item != null) {
					objInstall = new InstallmentBO();
					if (item.paymentType == 1) {
//                        #region Tính lãi suất HomeCredit
						double decInsuranceFee = 0;
						double decEfficient = 0;
						if (decLoan >= item.LoanFrom && decLoan <= item.LoanTo) {
							decEfficient = item.Co_efficient; // cho tat ca cac truong hop

							List<Double> inSpecialPercent = Arrays.asList(0d, 1d);// cho truong hop dac biet
							if (item.ProductId > 0 && inSpecialPercent.contains(item.PercentInstallment)) {
//                                #region Trường hợp đặc biệt

								if (decEfficient > 0) {
									decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12 * Month)
											/ ((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient);
									// phí bảo hiểm mỗi tháng cũng tính như bình thường
								} else {
									decInsuranceFee = (item.InsuranceCoefficient * decLoan) / 12;
								}

								if (item.PercentInstallment == 0) {
//                                    #region Lãi suất trong khoản 0%
									if (decEfficient == 0)
										// decInstallmentMoney = (Price - (Price * Convert.ToDecimal(iZeroPercent /
										// 100.0))) / Month;
										decInstallmentMoney = decLoan / Month;
									else
										// decInstallmentMoney = (Price - (Price * Convert.ToDecimal(iZeroPercent /
										// 100.0))) * decEfficient;
										decInstallmentMoney = decLoan * decEfficient;
									// decInsuranceFee = (item.InsuranceCoefficient * decLoan) / Month;
//                                    #endregion
								} else if (item.PercentInstallment == 1) {
//                                    #region Lãi suất 1%
									// decInstallmentMoney = Price * decEfficient;
									if (decEfficient > 0)
										decInstallmentMoney = decLoan
												/ ((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient);
									else
										decInstallmentMoney = 0;

									if (decEfficient > 0)
										decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12 * Month)
												/ ((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient);
									else
										decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12);
//                                    #endregion
								}

								// #region cong thuc mới V2 chua live
								// decInsuranceFee = item.InsuranceCoefficient * Price;//tien bao hiem
								// if (item.PercentInstallment == 1)
								// {
								// decInstallmentMoney = decEfficient * Price;//tra hang thang
								// }
								// else if(item.PercentInstallment == 0)
								// {
								// //trả hàng tháng = ( giá sản phẩm - trả trước )/kỳ hạn
								// decInstallmentMoney = decLoan / Month;//tra hang thang
								// }
								// #endregion

//                                #endregion
							} else {
//                                #region Công thức mới
								if (decEfficient > 0) {
									decInstallmentMoney = decLoan
											/ ((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient);

									decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12 * Month)
											/ ((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient);

								} else {
									decInstallmentMoney = 0;
									decInsuranceFee = 0;
								}
//                                #endregion

								// #region cong thuc mới V2 chua live
								// if (decEfficient > 0)
								// {
								// decInstallmentMoney = decEfficient * Price;//tra hang thang
								// decInsuranceFee = item.InsuranceCoefficient * Price;//tien bao hiem
								// }
								// else
								// {
								// decInstallmentMoney = 0;//tra hang thang
								// decInsuranceFee = 0;//tien bao hiem
								// }
								// #endregion
							}

							objInstall = item;
							objInstall.MoneyPayPerMonth = decInstallmentMoney;
							if (decInsuranceFee > 0)
								objInstall.InsuranceFee = decInsuranceFee;
							objInstall.TotalPay = (decInstallmentMoney * Month) + (decInsuranceFee * Month)
									+ (CollectionFee * Month) + decPrepaid;
						}
//                        #endregion
					} else if (item.paymentType == 3)// || item.CompanyID == 6
					{
//                        #region Tính lãi suất FECredit - HDSaison
						double decEfficient = 0;
						if (decLoan >= item.LoanFrom && decLoan <= item.LoanTo) {
							decEfficient = item.Co_efficient; // cho tat ca cac truong hop
							double decInsuranceFee = 0;
							if (item.InsuranceCoefficient > 0) {
								if (item.paymentType == 3) {
									if (decEfficient > 0)
										decInsuranceFee = (decLoan + (item.InsuranceCoefficient * decLoan))
												/ (((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient))
												- (decLoan / ((1 - Math.pow((1 + (double) decEfficient), -Month))
														/ decEfficient));
									else
										decInsuranceFee = (item.InsuranceCoefficient * decLoan) / (double) Month;
								} else
									decInsuranceFee = (item.InsuranceCoefficient * decLoan) / 12;// HD mặc định chia 12
								// tháng
							}

							List<Double> inSpecialPercent = Arrays.asList(0d, 1d);// cho truong hop dac biet
							if (item.ProductId > 0 && inSpecialPercent.contains(item.PercentInstallment)) {
//                                #region TH đặc biệt

								if (item.PercentInstallment == 0) {
//                                    #region Trả góp 0%

									if (decEfficient == 0)
										decInstallmentMoney = decLoan / Month;// (Price - (Price *
									// Convert.ToDecimal(iZeroPercent /
									// 100.0))) / Month;
									else
										decInstallmentMoney = decLoan * decEfficient; // (Price - (Price *
									// Convert.ToDecimal(iZeroPercent
									// / 100.0))) * decEfficient;
//                                    #endregion
								} else if (item.PercentInstallment == 1) {
//                                    #region Trả góp 1% cong thuc cũ
									// decInstallmentMoney = (Price - (Price * Convert.ToDecimal(Percent / 100.0)))
									// * decEfficient;
//                                    #endregion

//                                    #region Trả góp 1% -  Sử dụng công thức tính như bình thường
									if (decEfficient > 0)
										decInstallmentMoney = decLoan
												/ ((1 - (Math.pow((1 + (decEfficient)), -(item.PaymentMonth))))
														/ decEfficient);
									else
										decInstallmentMoney = 0;
//                                    #endregion
								}
//                                #endregion
							} else {
								// Công thức mới
								if (item.paymentType == 6) {
									if (decEfficient > 0)
										decInstallmentMoney = decLoan
												/ ((1 - (Math.pow((1 + (decEfficient)), -(item.PaymentMonth))))
														/ decEfficient);
									else
										decInstallmentMoney = 0;
								} else if (item.paymentType == 3) // decLoan = decLoan + phis baor hiểm
								{
									if (decEfficient > 0)
										decInstallmentMoney = decLoan
												/ ((1 - (Math.pow((1 + (decEfficient)), -(item.PaymentMonth))))
														/ decEfficient);
									else
										decInstallmentMoney = 0;
								}
							}
							objInstall = item;
							objInstall.MoneyPayPerMonth = decInstallmentMoney;
							if (decInsuranceFee > 0)
								objInstall.InsuranceFee = decInsuranceFee;
							objInstall.TotalPay = (decInstallmentMoney * Month) + (decInsuranceFee * Month)
									+ (CollectionFee * Month) + decPrepaid;
						}
						// #endregion
					} else if (item.paymentType == 2) {
						// #region Tính lãi suất ACS
						// if (item.BriefId != 2) //Không áp dụng cho CMND + GPLX
						// {
						decInstallmentMoney = (Price * ((1 - (Percent / 100.0))))
								* ((1 / (item.PaymentMonth)) + item.Co_efficient);

						double decInsuranceFee = 0;
						if (item.InsuranceCoefficient > 0)
							decInsuranceFee = (item.InsuranceCoefficient * decLoan) / (double) Month;
						objInstall = item;
						objInstall.MoneyPayPerMonth = decInstallmentMoney;
						if (decInsuranceFee > 0)
							objInstall.InsuranceFee = decInsuranceFee;
						objInstall.TotalPay = (decInstallmentMoney * Month) + (decInsuranceFee * Month)
								+ (CollectionFee * Month) + decPrepaid;
						// }
						// #endregion
					}
				}
			}
		} catch (Exception ex) {
			throw ex;
		}
		return objInstall;
	}

	public void TestFaiLRequest() throws Throwable {

		throw new IOException();
	}

	public boolean isAppliances(int cateId) {
		var ApplianceCategories = Arrays.asList(4645, 462, 1922, 1982, 1983, 1984, 1985, 1986, 1987, 1988, 1989, 1990,
				1991, 1992, 2062, 2063, 2064, 2084, 2222, 2262, 2302, 2322, 2342, 2142, 3305, 5473, 2428, 3385, 5105,
				7367, 5554);
		var Appliance2142Categories = Arrays.asList(4928, 4931, 5205, 4930, 5228, 4927, 5225, 5292, 5227, 5226, 5230,
				5231, 2403, 2402, 5229, 4929, 4932, 5478, 5395, 5354, 6790, 6819, 3187, 3729, 7075, 346, 6599, 7305,
				6012);
		if (Appliance2142Categories.contains(cateId) || ApplianceCategories.contains(cateId))
			return true;
		return false;
	}

	public List<InstallmentBO> getListFeatureInstallmentByCateProduct(int siteID, int CompanyId, int CategoryId,
			int ProductId, int Percent, int Month, double MoneyLoan, int FeatureType, int IsDefaultPackage,
			int InventStatusId, String languageId) throws Throwable {
		// Cách mới: Lấy toàn bộ danh sách theo FeatureType, sau đó lọc lại theo % và
		// tháng truyền vào
		List<InstallmentBO> lstInstall = getListNormalInstallment(siteID, CategoryId, -1, CompanyId, Percent, Month, 0,
				ProductId, InventStatusId, 0, languageId);
		if (lstInstall == null || lstInstall.size() == 0)
			return null;
		lstInstall = lstInstall.stream().filter(x -> x.ToDate.after(new Date())).collect(Collectors.toList());
		if (lstInstall == null || lstInstall.size() == 0)
			return null;
		if (Percent > 0 && Month > 0)
			lstInstall = lstInstall.stream().filter(p -> p.PaymentPercentFrom == Percent && p.PaymentMonth == Month)
					.collect(Collectors.toList());
		else
			lstInstall = lstInstall.stream().sorted(Comparator.comparingInt(x -> x.PaymentPercentFrom))
					.collect(Collectors.toList());

		if (lstInstall == null || lstInstall.size() == 0)
			return null;
		if (Percent == 0 && MoneyLoan > 0)
			lstInstall = lstInstall.stream().filter(p -> MoneyLoan >= p.PriceFrom && MoneyLoan <= p.PriceTo)
					.collect(Collectors.toList());
		else if (MoneyLoan > 0)
			lstInstall = lstInstall.stream().filter(p -> MoneyLoan >= p.LoanFrom && MoneyLoan <= p.LoanTo)
					.collect(Collectors.toList());
		if (lstInstall == null || lstInstall.size() == 0)
			return null;
		lstInstall = lstInstall.stream().sorted(Comparator.comparingDouble(x -> x.PercentInstallment))
				.collect(Collectors.toList());
		if (lstInstall == null || lstInstall.size() == 0)
			return null;
		return lstInstall;
	}

	public CompanyInstallmentBO getCompanyPercentAndMonth(int insType, double price, int categoryId, int manuId,
			int productid, int companyid, int percent, int month) {
		if (price <= 0) {
			percent = -1;
			month = 0;
			companyid = 0;
			return new CompanyInstallmentBO(companyid, percent, month);
		}

		if (insType == 2) {
//            #region Lãi suất thấp
			if (categoryId == 42 || categoryId == 1882) {
				// dien thoai va dong ho
				if (manuId == 80) {
					// dien thoai apple:
					if (price >= 4000000 && price <= 21300000) {
						percent = 30;
						month = 12;
						companyid = 3;
					} else if (price > 21300000 && price <= 25000000) {
						percent = 40;
						month = 12;
						companyid = 3;
					} else if (price > 25000000 && price <= 30000000) {
						percent = 50;
						month = 12;
						companyid = 1;
					} else if (price > 30000000) {
						percent = 50;
						month = 15;
						companyid = 2;
					}
				} else if (price >= 2500000 && price < 3700000) {
					percent = 20;
					month = 12;
					companyid = 1;
				} else if (price >= 3700000 && price < 4000000) {
					percent = 30;
					month = 12;
					companyid = 1;
				} else if (price >= 4000000 && price <= 21300000) {
					percent = 30;
					month = 12;
					companyid = 3;
				} else if (price >= 21300000 && price <= 25000000) {
					percent = 40;
					month = 12;
					companyid = 3;
				} else if (price > 25000000 && price <= 30000000) {
					percent = 50;
					month = 12;
					companyid = 1;
				} else if (price > 30000000) {
					percent = 50;
					month = 15;
					companyid = 2;
				} else {
					percent = -1;
					month = 0;
					companyid = -1;
				}
			} else if (categoryId == 522) {
				// may tinh bảng
				if (manuId == 1028) {
					// apple:
					if (price >= 4000000 && price <= 21300000) {
						percent = 30;
						month = 12;
						companyid = 3;
					} else if (price > 21300000 && price <= 25000000) {
						percent = 40;
						month = 12;
						companyid = 3;
					} else if (price > 25000000 && price <= 30000000) {
						percent = 50;
						month = 12;
						companyid = 1;
					} else if (price > 30000000) {
						percent = 50;
						month = 15;
						companyid = 2;
					}
				} else if (price >= 2500000 && price < 3700000) {
					percent = 20;
					month = 12;
					companyid = 1;
				} else if (price >= 3700000 && price < 4000000) {
					percent = 30;
					month = 12;
					companyid = 1;
				} else if (price >= 4000000 && price <= 21300000) {
					percent = 30;
					month = 12;
					companyid = 3;
				} else if (price >= 21300000 && price <= 25000000) {
					percent = 40;
					month = 12;
					companyid = 3;
				} else if (price > 25000000 && price <= 30000000) {
					percent = 50;
					month = 12;
					companyid = 1;
				} else if (price > 30000000) {
					percent = 50;
					month = 12;
					companyid = 2;
				} else {
					percent = -1;
					month = 0;
					companyid = -1;
				}
			} else if (categoryId == 44) {
				// lap top
				if (manuId == 203) {
					// apple
					if (price >= 4000000 && price <= 21300000) {
						percent = 30;
						month = 12;
						companyid = 3;
					} else if (price > 21300000 && price <= 25000000) {
						percent = 40;
						month = 12;
						companyid = 3;
					} else if (price > 25000000 && price <= 40000000) {
						percent = 50;
						month = 12;
						companyid = 1;
					} else if (price > 40000000 && price <= 50000000) {
						percent = 50;
						month = 15;
						companyid = 2;
					} else if (price > 50000000) {
						percent = 50;
						month = 18;
						companyid = 2;
					}
				} else if (price >= 2500000 && price < 3700000) {
					percent = 20;
					month = 12;
					companyid = 1;
				} else if (price >= 3700000 && price < 4000000) {
					percent = 30;
					month = 12;
					companyid = 1;
				} else if (price >= 4000000 && price <= 21300000) {
					percent = 30;
					month = 12;
					companyid = 3;
				} else if (price > 21300000 && price <= 25000000) {
					percent = 40;
					month = 12;
					companyid = 3;
				} else if (price > 25000000 && price <= 40000000) {
					percent = 50;
					month = 12;
					companyid = 1;
				} else if (price > 40000000 && price <= 50000000) {
					percent = 50;
					month = 15;
					companyid = 2;
				} else if (price > 50000000) {
					percent = 50;
					month = 18;
					companyid = 2;
				} else {
					percent = -1;
					month = 0;
					companyid = -1;
				}
			} else if (dmxCateIDList.contains(categoryId)) {
				// tivi: 1942; tu lanh: 1943; may lanh: 2002; may giat: 1944; gia dụng: 462
				if (price >= 2500000 && price < 3700000) {
					percent = 20;
					month = 12;
					companyid = 1;
				} else if (price >= 3700000 && price < 4000000) {
					percent = 30;
					month = 12;
					companyid = 1;
				} else if (price >= 4000000 && price <= 21300000) {
					percent = 30;
					month = 12;
					companyid = 3;

				} else if (price > 21300000 && price <= 25000000) {
					percent = 40;
					month = 12;
					companyid = 3;
				} else if (price > 25000000 && price <= 40000000) {
					percent = 50;
					month = 12;
					companyid = 1;

				} else if (price > 40000000 && price <= 50000000) {
					percent = 50;
					month = 15;
					companyid = 2;
				} else if (price > 50000000) {
					percent = 50;
					month = 18;
					companyid = 2;
				} else {
					percent = -1;
					month = 0;
					companyid = -1;
				}
			}
//            #endregion
			return new CompanyInstallmentBO(companyid, percent, month);
		} else if (insType == 3) {
//            #region Trả trước 0đ
			if (price >= 1200000 && price < 2000000) {
				percent = 0;
				month = 3;
				companyid = 3;
			} else if (price >= 2000000 && price <= 10000000) {
				percent = 0;
				month = 12;
				companyid = 3;
			} else {
				percent = -1;
				month = 0;
				companyid = -1;
			}
//            #endregion
			return new CompanyInstallmentBO(companyid, percent, month);
		} else if (insType == 4) {
//            #region Thủ tục đơn giản
			if (categoryId == 42 || categoryId == 1882) {
//                #region Điện thoại
				if (manuId == 80) {
					// apple
					if (price >= 2500000 && price <= 14200000) {
						percent = 30;
						month = 12;
						companyid = 2;
					} else if (price > 14200000 && price <= 16600000) {
						percent = 40;
						month = 12;
						companyid = 2;
					} else if (price > 16600000 && price <= 20000000) {
						percent = 50;
						month = 12;
						companyid = 2;
					}
				} else if (price >= 1200000 && price < 10000000) {
					percent = 20;
					month = 12;
					companyid = 1;
				} else if (price >= 10000000 && price <= 14200000) {
					percent = 30;
					month = 12;
					companyid = 1;
				} else if (price > 14200000 && price <= 16600000) {
					percent = 40;
					month = 12;
					companyid = 2;
				} else if (price > 16600000 && price <= 20000000) {
					percent = 50;
					month = 12;
					companyid = 2;
				} else if (price > 20000000 && price <= 25000000) {
					percent = 40;
					month = 12;
					companyid = 1;
				} else {
					percent = -1;
					month = 0;
					companyid = -1;
				}
//                #endregion
			} else if (categoryId == 522) {
//                #region Máy tính bảng
				if (manuId == 1028) {
					// apple
					if (price >= 2500000 && price <= 14200000) {
						percent = 30;
						month = 12;
						companyid = 2;
					} else if (price > 14200000 && price <= 16600000) {
						percent = 40;
						month = 12;
						companyid = 2;
					} else if (price > 16600000 && price <= 20000000) {
						percent = 50;
						month = 12;
						companyid = 2;
					}
				} else if (price >= 1200000 && price < 10000000) {
					percent = 20;
					month = 12;
					companyid = 1;
				} else if (price >= 10000000 && price <= 14200000) {
					percent = 30;
					month = 12;
					companyid = 1;
				} else if (price > 14200000 && price <= 16600000) {
					percent = 40;
					month = 12;
					companyid = 2;
				} else if (price > 16600000 && price <= 20000000) {
					percent = 50;
					month = 12;
					companyid = 2;
				} else if (price > 20000000 && price <= 25000000) {
					percent = 40;
					month = 12;
					companyid = 1;
				} else {
					percent = -1;
					month = 0;
					companyid = -1;
				}
//                #endregion
			} else if (categoryId == 44) {
//                #region Laptop
				if (manuId == 203) {
					// apple
					if (price >= 2500000 && price <= 14200000) {
						percent = 30;
						month = 12;
						companyid = 2;
					} else if (price > 14200000 && price <= 16600000) {
						percent = 40;
						month = 12;
						companyid = 2;
					} else if (price > 16600000 && price <= 20000000) {
						percent = 50;
						month = 12;
						companyid = 2;
					}
				} else if (price >= 1200000 && price < 10000000) {
					percent = 20;
					month = 12;
					companyid = 1;
				} else if (price >= 10000000 && price <= 14200000) {
					percent = 30;
					month = 12;
					companyid = 1;
				} else if (price > 14200000 && price <= 16600000) {
					percent = 40;
					month = 12;
					companyid = 2;
				} else if (price > 16600000 && price <= 20000000) {
					percent = 50;
					month = 12;
					companyid = 2;
				} else if (price > 20000000 && price <= 33000000) {
					percent = 40;
					month = 12;
					companyid = 1;
				} else {
					percent = -1;
					month = 0;
					companyid = -1;
				}
//                #endregion
			} else if (dmxCateIDList.contains(categoryId)) {
				// tivi: 1942; tu lanh: 1943; may lanh: 2002; may giat: 1944; gia dụng: 462
				if (price >= 1200000 && price < 10000000) {
					percent = 20;
					month = 12;
					companyid = 1;
				} else if (price >= 10000000 && price <= 14200000) {
					percent = 30;
					month = 12;
					companyid = 1;
				} else if (price > 14200000 && price <= 16600000) {
					percent = 40;
					month = 12;
					companyid = 2;
				} else if (price > 16600000 && price <= 20000000) {
					percent = 50;
					month = 12;
					companyid = 2;
				} else if (price > 20000000 && price <= 33000000) {
					percent = 40;
					month = 12;
					companyid = 1;
				} else {
					percent = -1;
					month = 0;
					companyid = -1;
				}
			}
//            #endregion
		}
		return new CompanyInstallmentBO(companyid, percent, month);
	}

	public InstallmentBO getInstallmentResult_Old(int siteID, int CategoryId, double Price, int CompanyId, int Percent,
			int Month, int BriefId, String ListDealId, int ProductId, double CollectionFee, boolean isDefault,
			int InvenStatusId) throws Throwable {
		if (Month <= 0 || Percent < 0)
			return null;

		int iCategoryIDGetData = !Strings.isNullOrEmpty(ListDealId) ? CategoryId : -1;

		InstallmentBO objInstall = null;
		double decInstallmentMoney = 0;
//        #region Số tiền trả trước và khoản vay
		double decPrepaid = 0;
		int iZeroPercent = 0;
		if (ProductId > 0) {
//            #region Nếu trả góp 0% thì lấy số tiền trả trước tại đây -> Tính ra % mới

			var tmpInstallList = getListNormalInstallment(siteID, iCategoryIDGetData, -1, CompanyId, -1, -1, -1, -1,
					InvenStatusId == 2 ? 2 : -1, 0, siteID == 6 ? "km-KH" : "vi-VN");
//            		GetListInstallmentByCategory_Old(iCategoryIDGetData, CompanyId, InvenStatusId);
			InstallmentBO TmpinstallmentBO = null;
			if (tmpInstallList != null && tmpInstallList.size() > 0)
				TmpinstallmentBO = tmpInstallList.stream().filter(p -> p.ProductId == ProductId).findFirst()
						.orElse(null);
			if (TmpinstallmentBO != null && TmpinstallmentBO.PrepaymentAmount > 0) {
				decPrepaid = TmpinstallmentBO.PrepaymentAmount;
				Percent = 0; // Truong hop nay tra 0%
				var tmpPercent = (decPrepaid * 100.0d) / Price;
				if (tmpPercent > 0)
					iZeroPercent = (int) (tmpPercent);
			} else {
				iZeroPercent = Percent;
				decPrepaid = Price * (iZeroPercent / 100d);
			}
//            #endregion
		} else
			decPrepaid = Price * (Percent / 100d);
		double decLoan = Price - decPrepaid; // so tien vay thuc te

		int cateIDGetQuiData = iCategoryIDGetData;
		if (ProductId <= 0 && iCategoryIDGetData == -1) {
			// lay data truong hop thuong tu database:
			cateIDGetQuiData = -2;
		}

		// Xu cho truong hop trả trước 0đ bình thường***
		List<Integer> vtl = Arrays.asList(42, 44, 522, 1882);// vien thong
		if (ProductId <= 0 && Percent == 0 && !vtl.contains(CategoryId)) {
			iCategoryIDGetData = -3;// dung loc data
		}
//        #endregion

		List<InstallmentBO> lstInstallment = null;
		// Lấy list có brief 1 và 2 nếu là HC và chọn CMND + HK
		if (CompanyId == 1 && BriefId == 1) {
			// rieng cho HC:
			lstInstallment = getListNormalInstallment(siteID, cateIDGetQuiData, Price, CompanyId, Percent, Month, -1,
					ProductId, InvenStatusId, 0, siteID == 6 ? "km-Kh" : "vi-VN");
//            		objInstallmentBLL.GetListNormalInstallment_Old(cateIDGetQuiData, Price, CompanyId, Percent, Month, -1, ProductId, InvenStatusId);
			if (lstInstallment != null && lstInstallment.size() > 0)
				lstInstallment = lstInstallment.stream().filter(p -> p.BriefId == 1 || p.BriefId == 2)
						.collect(Collectors.toList());
//                lstInstallment.FindAll(p => p.BriefId == 1 || p.BriefId == 2);
		} else if (CompanyId == 1 && BriefId == 4) {
			// HC chuyển giấy tờ 4 thành 6
			lstInstallment = getListNormalInstallment(siteID, cateIDGetQuiData, Price, CompanyId, Percent, Month, -1,
					ProductId, InvenStatusId, 0, siteID == 6 ? "km-Kh" : "vi-VN");
			if (lstInstallment != null && lstInstallment.size() > 0)
				lstInstallment = lstInstallment.stream().filter(p -> p.BriefId == 6).collect(Collectors.toList());
//                lstInstallment.FindAll(p => p.BriefId == 6);
		} else {
			lstInstallment = getListNormalInstallment(siteID, cateIDGetQuiData, Price, CompanyId, Percent, Month, -1,
					ProductId, InvenStatusId, 0, siteID == 6 ? "km-Kh" : "vi-VN");
		}

		if (lstInstallment == null || lstInstallment.size() == 0)
			return null;
//        objResultMessageBO = objInstallmentBLL.ResultMessageBO;

		// xe truong hop binh thuong hoac lãi suất đặc biệt 0%, 1%:
		int Percentf = Percent;
		int iCategoryIDGetDataf = iCategoryIDGetData;
		if (ProductId > 0) {
			// truong hợp đặc biệt:
			if (CompanyId == 1 && BriefId == 1) {
				// rieng cho HC
				lstInstallment = lstInstallment.stream()
						.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 1 || p.BriefId == 2)
								&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
								&& (Percentf >= p.PaymentPercentFrom && Percentf <= p.PaymentPercentTo)
								&& Month == p.PaymentMonth && p.ProductId > 0)
						.collect(Collectors.toList());
//                		lstInstallment.FindAll(p => (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 1 || p.BriefId == 2) && (decLoan >= p.LoanFrom && decLoan <= p.LoanTo) && (Percent >= p.PaymentPercentFrom && Percent <= p.PaymentPercentTo) && Month == p.PaymentMonth && p.ProductId > 0);

				if (lstInstallment != null && lstInstallment.size() > 0)
					lstInstallment = lstInstallment.stream().sorted(Comparator.comparingInt(p -> p.BriefId))
							.collect(Collectors.toList());
//                    lstInstallment.OrderBy(p => p.BriefId).ToList();
			} else if (CompanyId == 1 && BriefId == 4) {
				// HC chuyển giấy tờ 4 thành 6
				lstInstallment = lstInstallment.stream()
						.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 6)
								&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
								&& (Percentf >= p.PaymentPercentFrom && Percentf <= p.PaymentPercentTo)
								&& Month == p.PaymentMonth && p.ProductId > 0)
						.collect(Collectors.toList());
//                		lstInstallment.FindAll(p => (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 6) && (decLoan >= p.LoanFrom && decLoan <= p.LoanTo) && (Percent >= p.PaymentPercentFrom && Percent <= p.PaymentPercentTo) && Month == p.PaymentMonth && p.ProductId > 0);
			} else {
				lstInstallment = lstInstallment.stream()
						.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && p.BriefId == BriefId
								&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
								&& (Percentf >= p.PaymentPercentFrom && Percentf <= p.PaymentPercentTo)
								&& Month == p.PaymentMonth && p.ProductId > 0)
						.collect(Collectors.toList());
//                		lstInstallment.FindAll(p => (Price >= p.PriceFrom && Price <= p.PriceTo) && p.BriefId == BriefId && (decLoan >= p.LoanFrom && decLoan <= p.LoanTo) && (Percent >= p.PaymentPercentFrom && Percent <= p.PaymentPercentTo) && Month == p.PaymentMonth && p.ProductId > 0);
			}
		} else {
			// trường hợp thường
			if (isDefault) {
				if (CompanyId == 1 && BriefId == 1) {
					// rieng cho HC:
					lstInstallment = lstInstallment.stream().filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo)
							&& (p.BriefId == 1 || p.BriefId == 2) && (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
							&& (Percentf >= p.PaymentPercentFrom && Percentf <= p.PaymentPercentTo)
							&& Month == p.PaymentMonth && p.ProductId <= 0 && p.CategoryID == iCategoryIDGetDataf
							&& p.IsDefaultPackage == 1).collect(Collectors.toList());
//                    		lstInstallment.FindAll(p => (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 1 || p.BriefId == 2) && (decLoan >= p.LoanFrom && decLoan <= p.LoanTo) && (Percent >= p.PaymentPercentFrom && Percent <= p.PaymentPercentTo) && Month == p.PaymentMonth && p.ProductId <= 0 && p.CategoryID == iCategoryIDGetData && p.IsDefaultPackage == 1);

					if (lstInstallment != null && lstInstallment.size() > 0)
						lstInstallment = lstInstallment.stream().sorted(Comparator.comparingInt(p -> p.BriefId))
								.collect(Collectors.toList());
//                        lstInstallment.OrderBy(p => p.BriefId).ToList();
				} else if (CompanyId == 1 && BriefId == 4) {
					// HC chuyển giấy tờ 4 thành 6
					lstInstallment = lstInstallment.stream()
							.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 6)
									&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
									&& (Percentf >= p.PaymentPercentFrom && Percentf <= p.PaymentPercentTo)
									&& Month == p.PaymentMonth && p.ProductId <= 0
									&& p.CategoryID == iCategoryIDGetDataf && p.IsDefaultPackage == 1)
							.collect(Collectors.toList());
//                    		lstInstallment.FindAll(p => (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 6) && (decLoan >= p.LoanFrom && decLoan <= p.LoanTo) && (Percent >= p.PaymentPercentFrom && Percent <= p.PaymentPercentTo) && Month == p.PaymentMonth && p.ProductId <= 0 && p.CategoryID == iCategoryIDGetData && p.IsDefaultPackage == 1);
				} else {
					lstInstallment = lstInstallment.stream()
							.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && p.BriefId == BriefId
									&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
									&& (Percentf >= p.PaymentPercentFrom && Percentf <= p.PaymentPercentTo)
									&& Month == p.PaymentMonth && p.ProductId <= 0
									&& p.CategoryID == iCategoryIDGetDataf && p.IsDefaultPackage == 1)
							.collect(Collectors.toList());
//                    		lstInstallment.FindAll(p => (Price >= p.PriceFrom && Price <= p.PriceTo) && p.BriefId == BriefId && (decLoan >= p.LoanFrom && decLoan <= p.LoanTo) && (Percent >= p.PaymentPercentFrom && Percent <= p.PaymentPercentTo) && Month == p.PaymentMonth && p.ProductId <= 0 && p.CategoryID == iCategoryIDGetData && p.IsDefaultPackage == 1);
				}
			} else {
				if (CompanyId == 1 && BriefId == 1) {
					// rieng cho HC:
					lstInstallment = lstInstallment.stream().filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo)
							&& (p.BriefId == 1 || p.BriefId == 2) && (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
							&& (Percentf >= p.PaymentPercentFrom && Percentf <= p.PaymentPercentTo)
							&& Month == p.PaymentMonth && p.ProductId <= 0 && p.CategoryID == iCategoryIDGetDataf)
							.collect(Collectors.toList());
//                    		lstInstallment.FindAll(p => (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 1 || p.BriefId == 2) && (decLoan >= p.LoanFrom && decLoan <= p.LoanTo) && (Percent >= p.PaymentPercentFrom && Percent <= p.PaymentPercentTo) && Month == p.PaymentMonth && p.ProductId <= 0 && p.CategoryID == iCategoryIDGetData);
					if (lstInstallment != null && lstInstallment.size() > 0)
						lstInstallment = lstInstallment.stream().sorted(Comparator.comparingInt(p -> p.BriefId))
								.collect(Collectors.toList());
				} else if (CompanyId == 1 && BriefId == 4) {
					// HC chuyển giấy tờ 4 thành 6
					lstInstallment = lstInstallment.stream()
							.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 6)
									&& (Percentf >= p.PaymentPercentFrom && Percentf <= p.PaymentPercentTo)
									&& Month == p.PaymentMonth && p.ProductId <= 0
									&& p.CategoryID == iCategoryIDGetDataf)
							.collect(Collectors.toList());
//                    		lstInstallment.FindAll(p => (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 6) && (decLoan >= p.LoanFrom && decLoan <= p.LoanTo) && (Percent >= p.PaymentPercentFrom && Percent <= p.PaymentPercentTo) && Month == p.PaymentMonth && p.ProductId <= 0 && p.CategoryID == iCategoryIDGetData);
				} else {
					lstInstallment = lstInstallment.stream()
							.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && p.BriefId == BriefId
									&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
									&& (Percentf >= p.PaymentPercentFrom && Percentf <= p.PaymentPercentTo)
									&& Month == p.PaymentMonth && p.ProductId <= 0
									&& p.CategoryID == iCategoryIDGetDataf)
							.collect(Collectors.toList());
//                    		lstInstallment.FindAll(p => (Price >= p.PriceFrom && Price <= p.PriceTo) && p.BriefId == BriefId && (decLoan >= p.LoanFrom && decLoan <= p.LoanTo) && (Percent >= p.PaymentPercentFrom && Percent <= p.PaymentPercentTo) && Month == p.PaymentMonth && p.ProductId <= 0 && p.CategoryID == iCategoryIDGetData);
				}
			}

		}

		if (lstInstallment == null || lstInstallment.size() == 0)
			return null;

		try {
			for (InstallmentBO item : lstInstallment) {
				if (item != null) {
					objInstall = new InstallmentBO();
					if (item.paymentType == 1) {
//                        #region Tính lãi suất HomeCredit
						double decInsuranceFee = 0;
						double decEfficient = 0;
						if (decLoan >= item.LoanFrom && decLoan <= item.LoanTo) {
							decEfficient = item.Co_efficient; // cho tat ca cac truong hop

							List<Double> inSpecialPercent = Arrays.asList(0d, 1d);// cho truong hop dac biet
							if (item.ProductId > 0 && inSpecialPercent.contains(item.PercentInstallment)) {
//                                #region Trường hợp đặc biệt

								if (decEfficient > 0) {
									decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12 * Month)
											/ ((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient);// phí
									// bảo
									// hiểm
									// mỗi
									// tháng
									// cũng
									// tính
									// như
									// bình
									// thường
								} else {
									decInsuranceFee = (item.InsuranceCoefficient * decLoan) / 12;
								}

								if (item.PercentInstallment == 0) {
//                                    #region Lãi suất trong khoản 0%
									if (decEfficient == 0)
										// decInstallmentMoney = (Price - (Price * Convert.ToDecimal(iZeroPercent /
										// 100.0))) / Month;
										decInstallmentMoney = decLoan / Month;
									else
										// decInstallmentMoney = (Price - (Price * Convert.ToDecimal(iZeroPercent /
										// 100.0))) * decEfficient;
										decInstallmentMoney = decLoan * decEfficient;
									// decInsuranceFee = (item.InsuranceCoefficient * decLoan) / Month;
//                                    #endregion
								} else if (item.PercentInstallment == 1) {
//                                    #region Lãi suất 1%
									// decInstallmentMoney = Price * decEfficient;
									if (decEfficient > 0)
										decInstallmentMoney = decLoan
												/ ((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient);
									else
										decInstallmentMoney = 0;

									if (decEfficient > 0)
										decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12 * Month)
												/ ((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient);
									else
										decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12);
//                                    #endregion
								}

								// #region cong thuc mới V2 chua live
								// decInsuranceFee = item.InsuranceCoefficient * Price;//tien bao hiem
								// if (item.PercentInstallment == 1)
								// {
								// decInstallmentMoney = decEfficient * Price;//tra hang thang
								// }
								// else if(item.PercentInstallment == 0)
								// {
								// //trả hàng tháng = ( giá sản phẩm - trả trước )/kỳ hạn
								// decInstallmentMoney = decLoan / Month;//tra hang thang
								// }
								// #endregion

//                                #endregion
							} else {
//                                #region Công thức mới
								if (decEfficient > 0) {
									decInstallmentMoney = decLoan
											/ ((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient);

									decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12 * Month)
											/ ((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient);

								} else {
									decInstallmentMoney = 0;
									decInsuranceFee = 0;
								}
//                                #endregion

								// #region cong thuc mới V2 chua live
								// if (decEfficient > 0)
								// {
								// decInstallmentMoney = decEfficient * Price;//tra hang thang
								// decInsuranceFee = item.InsuranceCoefficient * Price;//tien bao hiem
								// }
								// else
								// {
								// decInstallmentMoney = 0;//tra hang thang
								// decInsuranceFee = 0;//tien bao hiem
								// }
								// #endregion
							}

							objInstall = item;
							objInstall.MoneyPayPerMonth = decInstallmentMoney;
							if (decInsuranceFee > 0)
								objInstall.InsuranceFee = decInsuranceFee;
							objInstall.TotalPay = (decInstallmentMoney * Month) + (decInsuranceFee * Month)
									+ (CollectionFee * Month) + decPrepaid;
						}
//                        #endregion
					} else if (item.paymentType == 3)// || item.CompanyID == 6
					{
//                        #region Tính lãi suất FECredit - HDSaison
						double decEfficient = 0;
						if (decLoan >= item.LoanFrom && decLoan <= item.LoanTo) {
							decEfficient = item.Co_efficient; // cho tat ca cac truong hop
							double decInsuranceFee = 0;
							if (item.InsuranceCoefficient > 0) {
								if (item.paymentType == 3) {
									if (decEfficient > 0)
										decInsuranceFee = (decLoan + (item.InsuranceCoefficient * decLoan))
												/ (((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient))
												- (decLoan / ((1 - Math.pow((1 + (double) decEfficient), -Month))
														/ decEfficient));
									else
										decInsuranceFee = (item.InsuranceCoefficient * decLoan) / (double) Month;
								} else
									decInsuranceFee = (item.InsuranceCoefficient * decLoan) / 12;// HD mặc định chia 12
								// tháng
							}

							List<Double> inSpecialPercent = Arrays.asList(0d, 1d);// cho truong hop dac biet
							if (item.ProductId > 0 && inSpecialPercent.contains(item.PercentInstallment)) {
//                                #region TH đặc biệt

								if (item.PercentInstallment == 0) {
//                                    #region Trả góp 0%

									if (decEfficient == 0)
										decInstallmentMoney = decLoan / Month;// (Price - (Price *
									// Convert.ToDecimal(iZeroPercent /
									// 100.0))) / Month;
									else
										decInstallmentMoney = decLoan * decEfficient; // (Price - (Price *
									// Convert.ToDecimal(iZeroPercent
									// / 100.0))) * decEfficient;
//                                    #endregion
								} else if (item.PercentInstallment == 1) {
//                                    #region Trả góp 1% cong thuc cũ
									// decInstallmentMoney = (Price - (Price * Convert.ToDecimal(Percent / 100.0)))
									// * decEfficient;
//                                    #endregion

//                                    #region Trả góp 1% -  Sử dụng công thức tính như bình thường
									if (decEfficient > 0)
										decInstallmentMoney = decLoan
												/ ((1 - (Math.pow((1 + (decEfficient)), -(double) (item.PaymentMonth))))
														/ decEfficient);
									else
										decInstallmentMoney = 0;
//                                    #endregion
								}
//                                #endregion
							} else {
								// Công thức mới
								if (item.paymentType == 6) {
									if (decEfficient > 0)
										decInstallmentMoney = decLoan
												/ ((1 - (Math.pow((1 + (decEfficient)), -(item.PaymentMonth))))
														/ decEfficient);
									else
										decInstallmentMoney = 0;
								} else if (item.paymentType == 3) // decLoan = decLoan + phis baor hiểm
								{
									if (decEfficient > 0)
										decInstallmentMoney = decLoan
												/ ((1 - (Math.pow((1 + (decEfficient)), -(double) (item.PaymentMonth))))
														/ decEfficient);
									else
										decInstallmentMoney = 0;
								}
							}
							objInstall = item;
							objInstall.MoneyPayPerMonth = decInstallmentMoney;
							if (decInsuranceFee > 0)
								objInstall.InsuranceFee = decInsuranceFee;
							objInstall.TotalPay = (decInstallmentMoney * Month) + (decInsuranceFee * Month)
									+ (CollectionFee * Month) + decPrepaid;
						}
//                        #endregion
					} else if (item.paymentType == 2) {
//                        #region Tính lãi suất ACS
						// if (item.BriefId != 2) //Không áp dụng cho CMND + GPLX
						// {
						decInstallmentMoney = (Price * ((1 - (Percent / 100.0))))
								* ((1 / (double) (item.PaymentMonth)) + item.Co_efficient);

						double decInsuranceFee = 0;
						if (item.InsuranceCoefficient > 0)
							decInsuranceFee = (item.InsuranceCoefficient * decLoan) / (double) Month;
						objInstall = item;
						objInstall.MoneyPayPerMonth = decInstallmentMoney;
						if (decInsuranceFee > 0)
							objInstall.InsuranceFee = decInsuranceFee;
						objInstall.TotalPay = (decInstallmentMoney * Month) + (decInsuranceFee * Month)
								+ (CollectionFee * Month) + decPrepaid;
						// }
//                        #endregion
					}
				}
			}
		} catch (Exception ex) {
			throw ex;
		}
		return objInstall;
	}

	public InstallmentBO getInstallmentResult2020(int siteID, int CategoryId, double Price, int CompanyId, int Percent,
			int Month, int BriefId, int ProductId, double CollectionFee, int InventStatusId) throws Throwable {
		InstallmentBO objInstall = null;
		double decInstallmentMoney = 0;
		List<InstallmentBO> lstInstallment = getListNormalInstallment(siteID, CategoryId, Price, CompanyId, Percent,
				Month, -1, ProductId, InventStatusId, 0, siteID == 6 ? "km-KH" : "vi-VN");
		if (lstInstallment == null || lstInstallment.size() == 0)
			return null;
//        #region Phân biệt điều kiện để loại bỏ dòng dữ liệu dư thừa
		if (lstInstallment.size() > 1) // nếu có hơn 1 dữ liệu trả ra sẽ lọc.
		{
			if (ProductId > 0 && CategoryId <= 0) // Lấy theo sản phẩm
				lstInstallment = lstInstallment.stream()
						.filter(p -> p != null && p.ProductId == ProductId && p.isApplyForCate != 1)
						.collect(Collectors.toList());
			else if (ProductId > 0 && CategoryId > 0)
				lstInstallment = lstInstallment.stream()
						.filter(p -> p != null && p.CategoryID == CategoryId && p.isApplyForCate == 1)
						.collect(Collectors.toList());
		}
//        #endregion

//        #region Số tiền trả trước và khoản vay
		double decPrepaid = 0;
		int iZeroPercent = 0;
		if (ProductId > 0) {
//            #region Nếu trả góp 0% thì lấy số tiền trả trước tại đây -> Tính ra % mới

			InstallmentBO TmpinstallmentBO = lstInstallment.stream().filter(p -> p.ProductId == ProductId).findFirst()
					.orElse(null);
			if (TmpinstallmentBO == null && CategoryId > 0)
				TmpinstallmentBO = lstInstallment.stream()
						.filter(p -> p.CategoryID == CategoryId && p.isApplyForCate == 1).findFirst().orElse(null);
			if (TmpinstallmentBO == null) {
				// nếu không truyền cat nhưng không tìm thấy gói theo cat
				// trường hợp trả product = 0 ,IsApplyForCate và ko truyền cat.
				TmpinstallmentBO = lstInstallment.get(0);
			}
			// hanh set
			// lstInstallment = new List<InstallmentBO> { TmpinstallmentBO }; // laasy 1 goi
			// rox rangf

			if (TmpinstallmentBO != null && TmpinstallmentBO.PrepaymentAmount > 0) {
				decPrepaid = TmpinstallmentBO.PrepaymentAmount;
				Percent = 0; // Truong hop nay tra 0%
				var tmpPercent = (decPrepaid * 100d) / Price;
				if (tmpPercent > 0)
					iZeroPercent = (int) tmpPercent;
			} else {
				iZeroPercent = Percent;
				decPrepaid = Price * (iZeroPercent / 100d);
			}
//            #endregion
		} else
			decPrepaid = Price * (Percent / 100d);
		double decLoan = Price - decPrepaid; // so tien vay thuc te
//        #endregion

		// Lấy list có brief 1 và 2 nếu là HC và chọn CMND + HK
		if (CompanyId == 1 && (BriefId == 1 || BriefId == 2))
			lstInstallment = lstInstallment.stream().filter(p -> p.BriefId == 1 || p.BriefId == 2)
					.collect(Collectors.toList());
		else if (CompanyId == 1 && BriefId == 4) // CMND + HK + HDD
			lstInstallment = lstInstallment.stream().filter(p -> p.BriefId == 4 || p.BriefId == 6)
					.collect(Collectors.toList());
		if (lstInstallment == null || lstInstallment.size() == 0)
			return null;
		var Percentf = Percent;
		if (CompanyId == 1 && (BriefId == 1 || BriefId == 2)) {
//            #region HC => CMND + HK => Lấy cả CMND + HK và CMND + BLX, ngược lại chỉ lấy CMND + BLX
			lstInstallment = lstInstallment.stream()
					.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 1 || p.BriefId == 2)
							&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
							&& (Percentf >= p.PaymentPercentFrom && Percentf <= p.PaymentPercentTo)
							&& Month == p.PaymentMonth)
					.collect(Collectors.toList());
			if (lstInstallment != null && lstInstallment.size() > 0)
				lstInstallment = lstInstallment.stream().sorted(Comparator.comparingInt(p -> p.BriefId))
						.collect(Collectors.toList());
//            #endregion
		} else if (CompanyId == 1 && BriefId == 4) {
//            #region  HC => CMND + HK + HDĐ => Lấy cả CMND + HK + HDĐ và CMND + BLX + HDĐ
			lstInstallment = lstInstallment.stream()
					.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 4 || p.BriefId == 6)
							&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
							&& (Percentf >= p.PaymentPercentFrom && Percentf <= p.PaymentPercentTo)
							&& Month == p.PaymentMonth)
					.collect(Collectors.toList());
//            		lstInstallment.FindAll(p => (Price >= p.PriceFrom && Price <= p.PriceTo) && (p.BriefId == 4 || p.BriefId == 6) && (decLoan >= p.LoanFrom && decLoan <= p.LoanTo) && (Percent >= p.PaymentPercentFrom && Percent <= p.PaymentPercentTo) && Month == p.PaymentMonth);
			if (lstInstallment != null && lstInstallment.size() > 0)
				lstInstallment = lstInstallment.stream().sorted(Comparator.comparingInt(p -> p.BriefId))
						.collect(Collectors.toList());
//            #endregion
		} else
			lstInstallment = lstInstallment.stream()
					.filter(p -> (Price >= p.PriceFrom && Price <= p.PriceTo) && p.BriefId == BriefId
							&& (decLoan >= p.LoanFrom && decLoan <= p.LoanTo)
							&& (Percentf >= p.PaymentPercentFrom && Percentf <= p.PaymentPercentTo)
							&& Month == p.PaymentMonth)
					.collect(Collectors.toList());
		if (lstInstallment == null || lstInstallment.size() == 0)
			return null;
		try {
			for (InstallmentBO item : lstInstallment) {
				if (item != null) {
					boolean bIsValid = decLoan >= item.LoanFrom && decLoan <= item.LoanTo ? true : false;

					objInstall = new InstallmentBO();
					// home credit
					if (item.paymentType == 1) {
//                        #region Tính lãi suất Home Credit
						double decInsuranceFee = 0;
						double decEfficient = 0;
						if (bIsValid) {
							decEfficient = item.Co_efficient; // cho tat ca cac truong hop
							if (item.ProductId > 0 || (item.CategoryID > 0 && item.isApplyForCate == 1)) // TH đặc biệt
							{
//                                #region Trường hợp đặc biệt
								if (item.PercentInstallment == 0) {
//                                    #region Lãi suất 0%
									if (decEfficient == 0) {
										decInstallmentMoney = decLoan / Month;
										decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12);
									} else {
										decInstallmentMoney = decLoan * decEfficient;
										decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12 * Month)
												/ ((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient);
									}
//                                    #endregion
								} else if (item.PercentInstallment > 0) {
//                                    #region Lãi suất 1%
									decInstallmentMoney = decLoan
											/ ((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient);
									if (decEfficient > 0)
										decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12 * Month)
												/ ((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient);
									else
										decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12);
//                                    #endregion
								}
//                                #endregion
							} else {
//                                #region Trường hợp lãi suất thường
								if (decEfficient > 0) {
									decInstallmentMoney = decLoan
											/ ((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient);
									decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12 * Month)
											/ ((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient);
								} else {
									decInstallmentMoney = 0;
									decInsuranceFee = 0;
								}
//                                #endregion
							}
							// List<decimal> inSpecialPercent = new List<decimal> { 0, 1 };//cho truong hop
							// dac biet
							// if (item.ProductId > 0 || (item.CategoryID > 0 && item.IsApplyForCate ==
							// true)) //&& inSpecialPercent.Contains(item.PercentInstallment)
							// {
							// #region Trường hợp đặc biệt

							// if (decEfficient > 0)
							// {
							// decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12 * Month) /
							// (Convert.ToDecimal(1 - Math.Pow((1 + (double)decEfficient), -Month)) /
							// decEfficient);//phí bảo hiểm mỗi tháng cũng tính như bình thường
							// }
							// else
							// {
							// decInsuranceFee = (item.InsuranceCoefficient * decLoan) / 12;
							// }

							// if (item.PercentInstallment == 0)
							// {
							// #region Lãi suất trong khoản 0%
							// if (decEfficient == 0)
							// //decInstallmentMoney = (Price - (Price * Convert.ToDecimal(iZeroPercent /
							// 100.0))) / Month;
							// decInstallmentMoney = decLoan / Month;
							// else
							// //decInstallmentMoney = (Price - (Price * Convert.ToDecimal(iZeroPercent /
							// 100.0))) * decEfficient;
							// decInstallmentMoney = decLoan * decEfficient;
							// //decInsuranceFee = (item.InsuranceCoefficient * decLoan) / Month;
							// #endregion
							// }
							// else if (item.PercentInstallment > 0)
							// {
							// #region Lãi suất 1%
							// //decInstallmentMoney = Price * decEfficient;
							// if (decEfficient > 0)
							// decInstallmentMoney = decLoan / (Convert.ToDecimal(1 - Math.Pow((1 +
							// (double)decEfficient), -Month)) / decEfficient);
							// else
							// decInstallmentMoney = 0;

							// if (decEfficient > 0)
							// decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12 * Month) /
							// (Convert.ToDecimal(1 - Math.Pow((1 + (double)decEfficient), -Month)) /
							// decEfficient);
							// else
							// decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12);
							// #endregion
							// }

							// //#region cong thuc mới V2 chua live
							// //decInsuranceFee = item.InsuranceCoefficient * Price;//tien bao hiem
							// //if (item.PercentInstallment == 1)
							// //{
							// // decInstallmentMoney = decEfficient * Price;//tra hang thang
							// //}
							// //else if(item.PercentInstallment == 0)
							// //{
							// // //trả hàng tháng = ( giá sản phẩm - trả trước )/kỳ hạn
							// // decInstallmentMoney = decLoan / Month;//tra hang thang
							// //}
							// //#endregion

							// #endregion
							// }
							// else
							// {
							// #region Công thức mới
							// if (decEfficient > 0)
							// {
							// decInstallmentMoney = decLoan / (Convert.ToDecimal(1 - Math.Pow((1 +
							// (double)decEfficient), -Month)) / decEfficient);

							// decInsuranceFee = (item.InsuranceCoefficient * decLoan / 12 * Month) /
							// (Convert.ToDecimal(1 - Math.Pow((1 + (double)decEfficient), -Month)) /
							// decEfficient);

							// }
							// else
							// {
							// decInstallmentMoney = 0;
							// decInsuranceFee = 0;
							// }
							// #endregion
							// }

							objInstall = item;
							objInstall.MoneyPayPerMonth = decInstallmentMoney;
							if (decInsuranceFee > 0)
								objInstall.InsuranceFee = decInsuranceFee;
							objInstall.TotalPay = (decInstallmentMoney * Month) + (decInsuranceFee * Month)
									+ (CollectionFee * Month) + decLoan;
						}
//                        #endregion
					} else if (item.paymentType == 3 || item.paymentType == 6)// 2 ACS gói đặc biệt // FE && HDsaigon
					{
//                        #region Tính lãi suất FECredit - HDSaison
						double decEfficient = 0;
						if (bIsValid) {
							decEfficient = item.Co_efficient; // cho tat ca cac truong hop
							double decInsuranceFee = 0;
							if (item.InsuranceCoefficient > 0) {
								if (item.paymentType == 3) {
									if (decEfficient > 0)
										decInsuranceFee = (decLoan + (item.InsuranceCoefficient * decLoan))
												/ (((1 - Math.pow((1 + (double) decEfficient), -Month)) / decEfficient))
												- (decLoan / ((1 - Math.pow((1 + (double) decEfficient), -Month))
														/ decEfficient));
									else
										decInsuranceFee = (item.InsuranceCoefficient * decLoan) / (double) Month;
								} else
									decInsuranceFee = (item.InsuranceCoefficient * decLoan) / 12;// HD mặc định chia 12
								// tháng
							}
							if (item.ProductId > 0 || (item.CategoryID > 0 && item.isApplyForCate == 1)) {
//                                #region TH đặc biệt

								if (item.PercentInstallment == 0) {
//                                    #region Trả góp 0%

									if (decEfficient == 0)
										decInstallmentMoney = decLoan / Month;
									else
										decInstallmentMoney = decLoan * decEfficient;
//                                    #endregion
								} else if (item.PercentInstallment == 1) {
//                                    #region Trả góp 1% -  Sử dụng công thức tính như bình thường
									if (decEfficient > 0)
										decInstallmentMoney = decLoan
												/ ((1 - (Math.pow((1 + (decEfficient)), -((double) item.PaymentMonth))))
														/ decEfficient);
									else
										decInstallmentMoney = 0;
//                                    #endregion
								} else {
//                                    #region Các trường hợp khác, như fix cứng cho hãng, sản phẩm
									if (decEfficient > 0)
										decInstallmentMoney = decLoan
												/ ((1 - (Math.pow((1 + (decEfficient)), -(double) (item.PaymentMonth))))
														/ decEfficient);
									else
										decInstallmentMoney = 0;
//                                    #endregion
								}
//                                #endregion
							} else {
								// Công thức mới
								if (item.paymentType == 6) {
									if (decEfficient > 0)
										decInstallmentMoney = decLoan
												/ ((1 - (Math.pow((1 + (decEfficient)), -(double) (item.PaymentMonth))))
														/ decEfficient);
									else
										decInstallmentMoney = 0;
								} else if (item.paymentType == 3) // decLoan = decLoan + phis baor hiểm
								{
									if (decEfficient > 0)
										decInstallmentMoney = decLoan
												/ ((1 - (Math.pow((1 + (decEfficient)), -(double) (item.PaymentMonth))))
														/ decEfficient);
									else
										decInstallmentMoney = 0;
								}
							}
							objInstall = item;
							objInstall.MoneyPayPerMonth = decInstallmentMoney;
							if (decInsuranceFee > 0)
								objInstall.InsuranceFee = decInsuranceFee;
							objInstall.TotalPay = (decInstallmentMoney * Month) + (decInsuranceFee * Month)
									+ (CollectionFee * Month) + decPrepaid;
						}
//                        #endregion
					} else if (item.paymentType == 2) {
//                        #region Tính lãi suất ACS
						// if (item.BriefId != 2) //Không áp dụng cho CMND + GPLX
						// {
						decInstallmentMoney = (Price * ((1 - (Percent / 100.0))))
								* ((1 / (double) (item.PaymentMonth)) + item.Co_efficient);

						double decInsuranceFee = 0; // bảo hiểm
						if (item.InsuranceCoefficient > 0)
							decInsuranceFee = (item.InsuranceCoefficient * decLoan) / (double) Month;
						objInstall = item;
						objInstall.MoneyPayPerMonth = decInstallmentMoney;
						if (decInsuranceFee > 0)
							objInstall.InsuranceFee = decInsuranceFee;
						objInstall.TotalPay = (decInstallmentMoney * Month) + (decInsuranceFee * Month)
								+ (CollectionFee * Month) + decPrepaid;
						// }
//                        #endregion
					}
				}
			}
		} catch (Exception ex) {
			throw ex;
		}
		return objInstall;
	}

	// ham2
	public InstallmentBO getInstallmentResult(int siteID, int CategoryId, double Price, int CompanyId, int Percent,
			int Month, int BriefId, int ProductId, double CollectionFee, int ManuID, boolean isDefault,
			int InvenStatusId, int ProvinceId) throws Throwable {
		if (Month <= 0 || Percent < 0) {
			return null;
		}

		if (CategoryId == 1882 || CategoryId == 7077)
			CategoryId = 42;// dong ho se thanh dien thoai

		InstallmentBO obj = null;
		String ListDealId = "1";
		int FCateID = CategoryId;

		List<Integer> cateVienThongL = Arrays.asList(42, 44, 522, 1882, 7077, 7264);
		List<Integer> manuIdAppleL = Arrays.asList(1028, 203, 80);
		if (!cateVienThongL.contains(CategoryId) && Percent > 0) {
			FCateID = -1;
			ListDealId = "";
		} else if (isAppliances(CategoryId) && Percent == 0 && ProductId <= 0) {
			FCateID = 462;// cateID gia dung
		}

		// cho tra trươc 0đ hang dien may truong hop thường:
		if (!cateVienThongL.contains(CategoryId) && Percent == 0 && ProductId <= 0) {
			ListDealId = "";
		}

		if (manuIdAppleL.contains(ManuID) && CompanyId == 1 && ProductId <= 0 && !isAppliances(CategoryId)) {
			// truong hop của apple HC không có lãi 0%, 1%
			// FCateID = 9000;
		}
		// xet rieng cho IPhone
		// if (FCateID == 9000 && ManuID == 80 && Month > 12)
		// return null;
		// đoi voi FE va lai thường, nếu trả trước tu 10% thi BLX thay bang HK:
		// if(ProductId <= 0 && CompanyId == 3 && BriefId ==2 && Percent >= 10)
		// {
		// BriefId = 1;//chuyen thanh HK
		// }
		// chi danh cho FE
		// if (ProductId <= 0 && CompanyId == 3 && BriefId == 6 && Percent >= 10)
		// {
		// BriefId = 4;//chi danh cho FE
		// }
		var installforcate = getListFeatureInstallmentByCateProduct(siteID, CompanyId, -1, ProductId, Percent, Month,
				-1, 1, 1, 1, "vi-VN");

		boolean isApplyForCate = false;
		if (installforcate != null) {
			var crrObj = installforcate.stream().filter(c -> c.paymentType == CompanyId && c.PriceFrom <= Price
					&& c.PriceTo >= Price && c.isApplyForCate != 1).findFirst().orElse(null);
			if (crrObj == null) {
				crrObj = installforcate.stream().filter(c -> c.paymentType == CompanyId && c.PriceFrom <= Price
						&& c.PriceTo >= Price && c.isApplyForCate == 1).findFirst().orElse(null);
//                		installforcate.Where(c => c.CompanyID == CompanyId && c.PriceFrom <= Price && c.PriceTo >= Price && c.IsApplyForCate == true).FirstOrDefault();
			}
			if (crrObj != null) {
				isApplyForCate = crrObj.isApplyForCate == 1;
			}
		}

		if (InvenStatusId == 2) {
			obj = getInstallmentResult_Old(siteID, FCateID, Price, CompanyId, Percent, Month, BriefId, ListDealId,
					ProductId, CollectionFee, isDefault, InvenStatusId);
//			g_ProductSvc.GetInstallmentResult_Old(FCateID, Price, CompanyId, Percent, Month, BriefId, ListDealId, ProductId, CollectionFee, isDefault, InvenStatusId);
			// TODO: GetInstallmentResult_Old
		} else {
			// obj = _productSvc.GetInstallmentResult2020(isApplyForCate ? -1 : FCateID,
			// Price, CompanyId, Percent, Month, BriefId, isApplyForCate && product != null
			// ? product.Id : ProductId, CollectionFee, InvenStatusId);
			if (isApplyForCate)
				obj = getInstallmentResult2020(siteID, CategoryId, Price, CompanyId, Percent, Month, BriefId, ProductId,
						CollectionFee, InvenStatusId);
			else if (ProductId > 0 && isApplyForCate == false)
				obj = getInstallmentResult2020(siteID, -1, Price, CompanyId, Percent, Month, BriefId, ProductId,
						CollectionFee, InvenStatusId);
			else
				obj = getInstallmentResult(siteID, CategoryId, Price, CompanyId, Percent, Month, BriefId, ListDealId,
						-1, CollectionFee, isDefault);
		}

		if (obj == null || obj.MoneyPayPerMonth <= 0)
			return null;

		// xet dac biet theo tinh thanh
		if (ProductId > 0) {
			// tra gop dac biet:
			if (obj.ListProvinceID == null || obj.ListProvinceID.size() == 0
					|| obj.ListProvinceID.contains(ProvinceId + "")) {
				return obj;
			} else {
				return null;
			}
		}

		return obj;
	}

	public InstallmentBO GetFeatureInstallment(int siteID, int CompanyId, int CategoryId, int ProductId, int Percent,
			int Month, double MoneyLoan, int FeatureType, int IsDefaultPackage, int RecordsID) throws Throwable {
		List<Integer> vtl = Arrays.asList(42, 44, 522, 1882);
		int cateGetData = CategoryId;
		if (!vtl.contains(CategoryId) && Percent > 0) {
			cateGetData = -1;
		}

		int cateGetQuiData = cateGetData;
		if ((ProductId <= 0 && cateGetData == -1) || (ProductId <= 0 && Percent == 0 && !vtl.contains(CategoryId))) {
			cateGetQuiData = -2;// chi lay data hang dien may
		}

		// danh cho trả trước 0đ trường hợp thường***
		if (ProductId <= 0 && Percent == 0 && !vtl.contains(CategoryId)) {
			cateGetData = -3;
		}

//         TGDD.Web.Thegioididong.BusinessLogic.Installment.IInstallmentBLL objInstallmentBLL = ThegioididongFactoryBLL.CreateInstallmentBLL();
		// List<TGDD.Web.BusinessObjects.Installment.InstallmentBO> lstInstall =
		// objInstallmentBLL.GetListFeatureInstallment(CompanyId, cateGetQuiData,
		// ProductId, Percent, Month, MoneyLoan, FeatureType, IsDefaultPackage);
		List<InstallmentBO> lstInstall = getListNormalInstallment(siteID, cateGetQuiData, -1, CompanyId, Percent, Month,
				-1, ProductId, -1, IsDefaultPackage, siteID == 6 ? "km-KH" : "vi-VN");
//        		 objInstallmentBLL.GetListFeatureInstallment(CompanyId, cateGetQuiData, ProductId, Percent, Month, -1, FeatureType, IsDefaultPackage);

		if (lstInstall == null)
			return null;
		lstInstall = lstInstall.stream().filter(x -> x.InstallmentType == FeatureType).collect(Collectors.toList());
		if (lstInstall.isEmpty())
			return null;

		int cateGetDataf = cateGetData;
		if (Percent == 0 && MoneyLoan > 0)
			lstInstall = lstInstall.stream()
					.filter(p -> MoneyLoan >= p.PriceFrom && MoneyLoan <= p.PriceTo && p.CategoryID == cateGetDataf)
					.collect(Collectors.toList());
//             .FindAll(p => MoneyLoan >= p.PriceFrom && MoneyLoan <= p.PriceTo && p.CategoryID == cateGetData);
		else if (MoneyLoan > 0)
			lstInstall = lstInstall.stream()
					.filter(p -> MoneyLoan >= p.LoanFrom && MoneyLoan <= p.LoanTo && p.CategoryID == cateGetDataf)
					.collect(Collectors.toList());
//             lstInstall = lstInstall.FindAll(p => MoneyLoan >= p.LoanFrom && MoneyLoan <= p.LoanTo && p.CategoryID == cateGetData);
		else if (MoneyLoan < 0 && ProductId <= 0)
			lstInstall = lstInstall.stream().filter(p -> p.CategoryID == cateGetDataf).collect(Collectors.toList());
//             lstInstall = lstInstall.FindAll(p => p.CategoryID == cateGetData);

		if (lstInstall == null || lstInstall.isEmpty())
			return null;

		InstallmentBO fobj = null;
		if (RecordsID > 0) {
			fobj = lstInstall.stream().filter(x -> x.BriefId == RecordsID).findFirst().orElse(null);
		}
		if (fobj == null)
			fobj = lstInstall.get(0);

		return fobj;
	}

	public List<ProductCodeGalleryBO> getProductCodeGallery(int ProductID) throws Throwable {
		var tmp = factoryRead.QueryFunctionCached("productcode_gallery_getbyProductID", ProductCodeGalleryBO[].class,
				false, ProductID);
		return tmp == null ? null : Arrays.asList(tmp);
	}

	public double getFeeByCompany(int companyID) {
		switch (companyID) {
		case 1:
			return 11000;
		case 3:
		case 6:
			return 12000;
		default:
			return 0;
		}
	}

	public WarrantyMonthBO getWarrantyMonthByProductCode(String productCode, int inventoryStatus, int companyID)
			throws Throwable {
		var w = factoryRead.queryFunctionCached("warrantymonth_getbyProductCode", WarrantyMonthBO[].class, productCode,
				inventoryStatus, companyID);
		return w == null || w.length == 0 ? null : w[0];
	}

	public PriceHelper getPriceHelper() {
		return priceHelper;
	}

	public URLGoogleBO[] getDataUrlRedirect() throws Throwable {
		return factoryRead.queryFunctionCached("google_GetAll", URLGoogleBO[].class);
	}

	public URLGoogleBO[] getCachedDataUrlRedirect() throws Throwable {
		String TKey = "urlredirect";
		String OKey = "cúc cu";
		URLGoogleBO[] cached = CachedObject.getObject(TKey, OKey, 60, URLGoogleBO[].class);
		if (cached == null) {
			cached = getDataUrlRedirect();
			CachedObject.putObject(TKey, OKey, cached);
		}
		return cached;
	}

	public InstallmentException[] getListInstallmentException(int productID) throws Throwable {
		return factoryRead.queryFunction("exception_GetByProductid", InstallmentException[].class, productID);
	}

	public PreOrderProgramBO[] getListPromotionCMS(int productID) throws Throwable {
		return factoryRead.queryFunction("p_Promoprogram_GetByID", PreOrderProgramBO[].class, productID);
	}

	public PreOrderProgramBO[] preOrderProgramGetByProductId(int productID) throws Throwable {
		return factoryRead.queryFunction("preorderprogram_GetByProductID", PreOrderProgramBO[].class, productID);
	}

	public ProductStickerLabelBO getStickerLabel(int productID, int categoryID, int siteID, String languageID)
			throws Throwable {
		var rs = factoryRead.queryFunctionCached("product_getStickerProp", ProductStickerLabelBO[].class, productID,
				categoryID, siteID, languageID);
		return rs == null || rs.length == 0 ? null : rs[0];
	}

	public PrenextProduct[] GetPreProduct(int PreProductID) throws Throwable {
		var PreProduct = factoryRead.QueryFunctionCached("product_PreNextGetbyID", PrenextProduct[].class, false,
				PreProductID);
		return PreProduct;
	}

//	public void getCategoryQuantity(int[] productID, int siteID) throws Throwable {
//		var list = factoryRead.QueryFunction("product_GetCategoryQuantity", ProductBO[].class, false, productID,
//				siteID);
//		for (var p : list) {
//
//		}
//	}

	public ProductBO[] GetnextVersionProductFromCache(int productID, int siteID, String langID, int provinceID)
			throws Throwable {

		String key = "GetnextVersionProductFromCache" + productID + "_" + siteID + "_" + langID + "_" + provinceID;
		var rs = (ProductBO[]) CacheStaticHelper.GetFromCache(key, 60);
		if (rs == null) {
			rs = GetnextVersionProduct(productID, siteID, langID, provinceID);
			CacheStaticHelper.AddToCache(key, rs);
		}
		return rs;

	}

	/*
	 * LOCTRAN
	 */

	public ProductBO[] GetnextVersionProduct(int productID, int siteID, String langID, int provinceID)
			throws Throwable {

		var Manu = factoryRead.QueryFunctionCached("product_getCategoryByProductID", ProductBO[].class, false,
				productID);
		int ManuID = 0;
		int cateID = 0;

		if (Manu != null && Manu.length > 0) {
			ManuID = (int) Manu[0].ManufactureID;
			cateID = Manu[0].CategoryID;
		}
		/*
		 * var searchBuilder = new SearchSourceBuilder(); var query = boolQuery();
		 *
		 * query.must(termQuery("ManuID", ManuID));
		 * searchBuilder.from(0).size(200).query(query);
		 *
		 * var searchRequest = new SearchRequest("ms_prenextproduct");
		 * searchRequest.source(searchBuilder); var queryResults =
		 * clientIndex.search(searchRequest, RequestOptions.DEFAULT);%
		 */

		final int max = 10;
//		Object[] prenextIds = new Object[] {};
		List<ProductSO> result = new ArrayList<ProductSO>();

		var sourceBuilder = new SearchSourceBuilder();

		var boolQuery = boolQuery();

		// 4-kinh doanh, 8-Cho đặt trước, 11-Chuyển hàng
		boolQuery.must(termQuery("SiteID", siteID)).must(termQuery("CategoryID", cateID))
				.must(termsQuery("Prices.WebStatusId_" + provinceID, "4", "8"));

		boolQuery.mustNot(termQuery("ProductID", productID));
		boolQuery.mustNot(termQuery("ShockPriceType", "14"));
		boolQuery.must(termQuery("HasBimage", 1));
//		var subQuery = boolQuery();
		// process prenext if cate = ICT
		var timer = new CodeTimers();
		timer.start("all");
		if (cateID == 42 || cateID == 44 || cateID == 522) {
			boolQuery.must(QueryBuilders.termQuery("ManufactureID", ManuID).boost(1f));
			var getRequest = new GetRequest("ms_prenextproduct", productID + "");

			var getResponse = clientIndex.get(getRequest, RequestOptions.DEFAULT);
//			var nextProducts = new HashMap<Integer, String>();
			int level = 1;
			final float point = 20;

			if (!Objects.isNull(getResponse) && getResponse.isExists() && !getResponse.isSourceEmpty()) {
				var product = mapper.readValue(getResponse.getSourceAsString(), PrenextProductSO.class);
				timer.start("es");
				if (!Objects.isNull(product)) {
					var nextProduct = product.NextProduct;

					if (!Utils.StringIsEmpty(nextProduct)) {
//						nextProducts.put(++level, nextProduct);
						level++;
						boolQuery.should(termsQuery("ProductID", Stream.of(nextProduct.split(" ")).toArray())
								.boost(point * level));

						for (int i = 1; i < 3; i++) {
							var searchBuilder = new SearchSourceBuilder();
							var searchRequest = new SearchRequest("ms_prenextproduct");
							var query = boolQuery();
							searchBuilder.from(0).size(20).query(query);
							query.must(matchQuery("previousProduct", nextProduct).operator(Operator.OR));

							searchRequest.source(searchBuilder);

							SearchResponse queryResults = null;
//							var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//							var clientIndex1 = elasticClient1.getClient();
//							try {

								queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

//							} catch (Throwable e) {
//
//								Logs.LogException(e);
//								throw e;
//							} finally {
//								elasticClient1.close();
//							}

							var data = Stream.of(queryResults.getHits().getHits()).map(x -> {
								try {
									return mapper.readValue(x.getSourceAsString(), PrenextProductSO.class);
								} catch (IOException e1) {
									return null;
								}
							}).filter(p -> !Objects.isNull(p)).collect(Collectors.toList());

							if (data.isEmpty())
								break;

							// All next products
							var listNextProducts = data.stream().map(p -> p.productID + " ").reduce("", String::concat)
									.strip();
//							nextProducts.put(++level, listNextProducts);
							level++;
							boolQuery.should(termsQuery("ProductID", Stream.of(listNextProducts.split(" ")).toArray())
									.boost(point * level));
							// contain all ID next product for search next products
							var listSearchNextProducts = data.stream()
									.filter(p -> !Objects.isNull(p.NextProduct) && !p.NextProduct.isBlank())
									.map(p -> p.NextProduct + " ").reduce("", String::concat).strip();

							nextProduct = listSearchNextProducts;
						}
					}

				}
				timer.pause("es");
			}
			if (ManuID > 0)
				boolQuery.should(QueryBuilders.termQuery("ManufactureID", ManuID).boost(1f));

		}
//		else {
//			boolQuery.should(termQuery("CategoryID", cateID).boost(1.0f));
//		}

		// other rules

		// Process relative property
		String strProp = "";
		PropList[] lstProp = null;
		StringWrapper[] wrappers = null;
		ProductPropDetailBO propDetail = this.getProductPropDetailBO(productID, siteID, langID, cateID);
		lstProp = propDetail.getProductDetailV3;
		wrappers = propDetail.getRelatePropertyIDList;
		if (lstProp != null && lstProp.length > 0) {
			PropList prop = lstProp[0];
			ProductDetailBO[] arrProductDetail = prop.productdetails;
			List<Integer> relateProps = this.getReplatePropList(wrappers);
			if (relateProps != null && relateProps.size() > 0) {
				strProp = Stream.of(arrProductDetail)
						.filter(x -> x != null && relateProps.contains(x.PropertyID) && x.Value != null).map(x -> {
							return "prop" + x.PropertyID + "_" + x.Value;
						}).collect(Collectors.joining(" "));
			}
		}

//		if (relateProp > 0) {
//			PropList[] lstProp = this.getProductDetail(productID, siteID, langID);
//			if (lstProp != null && lstProp.length > 0) {
//				PropList prop = lstProp[0];
//				ProductDetailBO[] arrProductDetail = prop.productdetails;
//				if (arrProductDetail != null && arrProductDetail.length > 0) {
//					for (ProductDetailBO product : arrProductDetail) {
//						if (product.PropertyID == relateProp && product.Value != null) {
//							strProp += "prop" + relateProp + "_" + product.Value;
//
//							break;
//						}
//					}
//
//				}
//			}
//		}

		// Process price in range
		ProductSO productso = this.getProductSOById(productID, siteID);
		double price = 0;
		if (productso != null && productso.Prices != null) {
			Object objPrice = productso.Prices.get("Price_" + provinceID);
			if (objPrice != null) {
				price = Double.valueOf(objPrice.toString());
			}
		}

		if (!Utils.StringIsEmpty(strProp)) {
//			boolQuery.should(termQuery("PropStr", strProp).boost(5.0f));
			String[] props = strProp.trim().split(" ");
//			var subQuery = boolQuery();
//			if ((cateID == 42 || cateID == 44 || cateID == 522) && ManuID > 0) {
//				boolQuery.must(QueryBuilders.termQuery("ManufactureID", ManuID).boost(1f));
//			}

//				subQuery.must(termQuery("PropStr", prop).boost(5.0f));
//				subQuery.should(termQuery("PropStr", prop).boost(5.0f));
			boolQuery.should(termsQuery("PropStr", props).boost(5.0f));

//			subQuery.must(matchQuery("PropStr", strProp.trim()).operator(Operator.OR).boost(10.0f));
//			subQuery.minimumShouldMatch(1);
//			boolQuery.should(subQuery);

//			boolQuery.should(termQuery("PropStr", strProp).boost(3.0f));
		}

		if (price > 0) {
			if (cateID == 42 || cateID == 44 || cateID == 522) {
				var subQuery = boolQuery();
				if (ManuID > 0)
					subQuery.must(QueryBuilders.termQuery("ManufactureID", ManuID).boost(1f));
				subQuery.must(rangeQuery("Prices.Price_" + provinceID).gte(price * 0.8).lte(price * 1.2).boost(3.0f));
				boolQuery.should(subQuery);
			} else {
				boolQuery
						.should(rangeQuery("Prices.Price_" + provinceID).gte(price * 0.8).lte(price * 1.2).boost(3.0f));
			}

			Map<String, Object> parameters = Map.of("price", price);
			Script inline = new Script(ScriptType.INLINE, "painless",
					"Math.abs(doc['Prices.Price_3'].value - params.price)", parameters);
			sourceBuilder.sort(SortBuilders.scriptSort(inline, ScriptSortType.NUMBER).order(SortOrder.ASC));
		}
		if (cateID == 42 || cateID == 44 || cateID == 522) {
			boolQuery.minimumShouldMatch(1);
		}

//		boolQuery.must(subQuery);

//		System.out.println(boolQuery);

//		if(prenextIds.length > 0) {
//			boolQuery.should(termsQuery("ProductID", prenextIds).boost(10.0f));
//		}
		sourceBuilder.sort("_score", SortOrder.DESC);
		sourceBuilder.from(0).size(max).query(boolQuery);
//		System.out.println(sourceBuilder);

		SearchResponse searchResponse = elasticClient.searchRequest(sourceBuilder, "ms_product");
		if (!Objects.isNull(searchResponse)) {
			result = elasticClient.getSource(searchResponse, ProductSO.class);

			int[] listID = result.stream().mapToInt(x -> x.ProductID).toArray();
			if (listID != null && listID.length > 0) {
				timer.start("somap");
				ProductBO[] products = GetSimpleProductListByListID_PriceStrings_soMap(listID, siteID, provinceID,
						langID);
				timer.pause("somap");
				if (cateID == 2002 && siteID == 2) {
					ProductBO[] r = Arrays.stream(products).map(p -> {
						try {
							processMultiAirCondition(p, siteID, provinceID);
							return p;
						} catch (Throwable t) {
							return p;
						}

					}).toArray(ProductBO[]::new);
					return r;
				}
				timer.pause("all");
				long timeall = timer.getTimer("all").getElapsedTime();
				if (timeall > 5000) {
					Logs.Write("all:" + timeall + " Es time :" + timer.getTimer("es").getElapsedTime() + "\n Somap: "
							+ timer.getTimer("somap").getElapsedTime());
				}

				return products;
			}
		}

		return null;
	}

	private static List<String> StatusMultiAirCondition = List.of("8"); // 2, 4, 11

	public void processMultiAirConditions(ProductBO product, ProductBO[] MultiAirConditioner) throws Throwable {
		// var product = products[0];
		if (product.ProductErpPriceBO != null && product.ProductErpPriceBO.Price > 0)
			return;

		// if (DidxHelper.isBeta() || DidxHelper.isLocal() || DidxHelper.isStaging()) {
		// máy lạnh multi
		if (product.CategoryID == 2002) {
			if (MultiAirConditioner != null && MultiAirConditioner.length > 0) {
				var check = Arrays.stream(MultiAirConditioner).filter(x -> x.ProductErpPriceBO == null
						|| !StatusMultiAirCondition.contains(x.ProductErpPriceBO.WebStatusId + "")).count();
				if (check > 0)
					return;
				product.IsMultiAirConditioner = true;
				// filed này để dữ lại các product cho đễ ktr
				product.MultiAirConditionerIdsAllTime = Stream.of(MultiAirConditioner).map(x -> x.ProductID + "")
						.collect(Collectors.joining(","));

//					MultiAirConditioner = Stream.of(MultiAirConditioner)
//							.filter(x -> x.ProductErpPriceBO != null)
//							.filter(x -> StatusMultiAirCondition.contains(x.ProductErpPriceBO.WebStatusId + ""))
//							.toArray(ProductBO[]::new);

//					for (ProductBO productBO : MultiAirConditioner) {
//						productBO.IsMultiAirConditioner = true;
//						//productBO.ProductErpPriceBO.WebStatusId = 8;
//					}
				var tmp = Stream.of(MultiAirConditioner).map(x -> x.ProductID + "").collect(Collectors.joining(","));
				product.MultiAirConditionerIds = Utils.StringIsEmpty(tmp) ? null : tmp.split(",");
				product.MultiAirConditioners = MultiAirConditioner;
			}

		}
		// }
	}

	public void processMultiAirCondition(ProductBO product, int siteId, int provinceId) throws Throwable {

		if (product.ProductErpPriceBO != null && product.ProductErpPriceBO.Price > 0)
			return;

		// if (DidxHelper.isBeta() || DidxHelper.isLocal() || DidxHelper.isStaging()) {
		// máy lạnh multi
		if (product.CategoryID == 2002) {

			var MultiAirConditioner = getListPreResentProduct(product.ProductID, siteId, provinceId, "vi-VN");
			processMultiAirConditions(product, MultiAirConditioner);

//				if (MultiAirConditioner != null && MultiAirConditioner.length == 3) {
//					product.IsMultiAirConditioner = true;
//					// filed này để dữ lại các product cho đễ ktr
//					product.MultiAirConditionerIdsAllTime = Stream.of(MultiAirConditioner).map(x -> x.ProductID + "").collect(Collectors.joining(","));
//
//					MultiAirConditioner = Stream.of(MultiAirConditioner)
//							.filter(x -> x.ProductErpPriceBO != null)
//							.filter(x -> StatusMultiAirCondition.contains(x.ProductErpPriceBO.WebStatusId + "")).toArray(ProductBO[]::new);
//
////					Stream.of(MultiAirConditioner).forEach(x ->
////							x.IsMultiAirConditioner = true
////					);
//
//					for (ProductBO productBO : MultiAirConditioner) {
//						productBO.IsMultiAirConditioner = true;
//						//productBO.ProductErpPriceBO.WebStatusId = 8;
//					}
//
//					var tmp = Stream.of(MultiAirConditioner).map(x -> x.ProductID + "")
//							.collect(Collectors.joining(","));
//					product.MultiAirConditionerIds = Utils.StringIsEmpty(tmp) ? null : tmp.split(",");
//					product.MultiAirConditioners = MultiAirConditioner;
//				}

		}
		// }

	}

	public ProductBO[] getProductsByProductBanKem(PromotionQuery query) throws Throwable {
		final String PROMOTION_INDEX = "ms_promotion";
		final String PRODUCT_INDEX = "ms_product";

		List<String> promotionIds = null;

		var sourceBuilder = new SearchSourceBuilder();
		var boolPromotionQuery = boolQuery();

		int pageSize = query.PageSize > 0 ? query.PageSize : 100;
		sourceBuilder.from(Math.abs(query.PageIndex) * query.PageSize).size(pageSize);

		sourceBuilder.fetchSource(false);
		boolPromotionQuery.must(termsQuery("listProductBanKem.ProductID", query.ProductIds));
		boolPromotionQuery.must(rangeQuery("BeginDate").lte("now/d").timeZone("+07:00"));
		boolPromotionQuery.must(rangeQuery("EndDate").gte("now/d").timeZone("+07:00"));

		sourceBuilder.query(boolPromotionQuery);

		SearchResponse searchPromotionResponse = elasticClient.searchRequest(sourceBuilder, PROMOTION_INDEX);

//		SearchResponse searchPromotionResponse = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		try {
//
//			searchPromotionResponse = elasticClient1.searchRequest(sourceBuilder, PROMOTION_INDEX);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		if (!Objects.isNull(searchPromotionResponse)) {
			promotionIds = elasticClient.getIds(searchPromotionResponse);
			if (promotionIds.isEmpty())
				return null;
		}

		ProductQuery qry = new ProductQuery() {
			{
//			promotionBKIDs = promotionIds.stream().mapToInt(x -> Integer.parseInt(x)).toArray();
				SiteId = query.SiteId;
				Keyword = query.Keyword;
				CategoryId = query.CategoryId;
				WebStatusIDList = new int[] { 4 };
				PageSize = query.PageSize;
				ProvinceId = query.ProvinceId;
			}
		};
		qry.promotionBKIDs = promotionIds.stream().mapToInt(x -> Integer.parseInt(x)).toArray();
		return SearchProduct(qry);
//
//
//		var boolProductQuery = boolQuery();
//		sourceBuilder.fetchSource(true);
//		boolProductQuery.must(termsQuery("PromotionIdsBankem", promotionIds));
//		boolProductQuery.must(termQuery("CategoryID", query.CategoryId));
//		boolProductQuery.must(termsQuery("Prices.WebStatusId_" + query.ProvinceId, "4"));
//		if (!Utils.StringIsEmpty(query.Keyword)) {
//			String rsl = "";// không phân rã
//			String rsl2 = "";// phân rã
//			String formattedKeyword = DidxHelper.FilterVietkey(query.Keyword);
//			String[] keywords = formattedKeyword.trim().split("\\s+");
//
//			if (keywords.length <= 2) {
//				rsl = rsl2 = "(\"" + formattedKeyword + "\") ";
//			} else {
//				rsl = "(\"" + formattedKeyword + "\") ";
//				rsl2 = "(" + String.join(" AND ", keywords) + ")";
//			}
//			var k = boolQuery();
//			k.should(termQuery("Keyword", rsl).boost(500));
//			k.should(termQuery("Keyword", rsl2).boost(50));
//			k.should(termQuery("Keyword", rsl).boost(10));
//			k.should(termQuery("Keyword", rsl2).boost(0.5f));
//			var scripts = new FilterFunctionBuilder[] { new FilterFunctionBuilder(scriptFunction(" 10000 ")) };
//			boolProductQuery
//					.must(functionScoreQuery(k, scripts).scoreMode(ScoreMode.SUM).boostMode(CombineFunction.SUM));
//		}
//
//		sourceBuilder.query(boolProductQuery);
//		if (query.OrderType == OrderType.PRICE) {
//			if (query.OrderValue == OrderValue.DESC)
//				sourceBuilder.sort("Prices.Price_" + query.ProvinceId, SortOrder.DESC);
//			else
//				sourceBuilder.sort("Prices.Price_" + query.ProvinceId, SortOrder.ASC);
//		}
//
//		SearchResponse searchProductnResponse = elasticClient.searchRequest(sourceBuilder, PRODUCT_INDEX);
//		var ids = Stream.of(searchProductnResponse.getHits().getHits()).mapToInt(x -> {
//			try {
//				return esmapper.readValue(x.getSourceAsString(), ProductSO.class).ProductID;
//			} catch (Exception ignored) {
//				return 0;
//			}
//		}).filter(x -> x > 0).toArray();
//		return GetSimpleProductListByListID_PriceStrings_soMap(ids, query.SiteId, query.ProvinceId,
//				DidxHelper.getLangBySiteID(query.SiteId));
	}

	// kiểm tra cate là con sau cùng
	public boolean checkCategoryIsLeaf(int cate) throws Throwable {

		OResultSet ls = null;
		try {
			ls = factoryRead.Query("select 1 from product_category_lang where parentidlist =" + cate
					+ " and siteid = 11 and languageid='vi-VN'");
			if (ls != null) {
				while (ls.hasNext()) {
					var f = ls.next();
					if (f != null) {
						return false;
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			throw e;
			// TODO: handle exception
		} finally {
			CloseOResultSet(ls);
		}
		return true;
	}

	public LabelCampaignBO[] GetProductLabelCampaign(int productID) throws Throwable {

		var sb = new SearchSourceBuilder();
		var q = boolQuery();

//		q.filter(termQuery("previousProduct", productID));
//		q.filter(termQuery("isDeleted", 0));
		q.must(termQuery("ProDuctIDList", productID));
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("IsActived", 1));

		q.must(rangeQuery("BeginDate").lte(new Date())); // BeginDate < Date
		q.must(rangeQuery("EndDate").gte(new Date())); // EndDate > Date
		sb.from(0).size(10).query(q);

		var searchRequest = new SearchRequest("ms_label_campaign");
		searchRequest.source(sb);

		SearchResponse queryResults = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
			try {
				// System.out.println("ccc:" + x.getSourceAsString());
				return esmapper.readValue(x.getSourceAsString(), LabelCampaignSO.class);
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
		}).filter(x -> x != null).mapToInt(x -> x.LabelID).toArray();
		if (idlist != null && idlist.length > 0) {
			// var Label = factoryRead.QueryFunction("product_GetLableCampaignByIDList",
			// LabelCampaignBO[].class, false, idlist);
			LabelCampaignBO[] labelcp = factoryRead.QueryFunction("product_GetLableCampaignByIDList",
					LabelCampaignBO[].class, false, idlist);

			if (labelcp != null && labelcp.length > 0) {
				labelcp = Stream.of(labelcp).sorted((o1, o2) -> o1.ActivedDate.compareTo(o2.ActivedDate)).skip(1)
						.toArray(LabelCampaignBO[]::new);
			}

			return labelcp;
		} else
			return null;
	}

	public LabelCampaignSO[] GetAllProductLabelCampaign(int siteId) throws Throwable {

		var sb = new SearchSourceBuilder();

		// sb.query(QueryBuilders.matchAllQuery());

		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("IsActived", 1));
		q.must(termQuery("SiteIDList", siteId));
		var now = new Date();
		now = Utils.addHoursToJavaUtilDate(now, 7);

		q.must(rangeQuery("BeginDate").timeZone("+07:00").lte(now)); // BeginDate < Date
		q.must(rangeQuery("EndDate").timeZone("+07:00").gte(now)); // EndDate > Date
		sb.from(0).size(1000).query(q);
		sb.sort("ActivedDate", SortOrder.DESC);

		var searchRequest = new SearchRequest("ms_label_campaign");
		searchRequest.source(sb);

		SearchResponse queryResults = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		var listSO = Stream.of(queryResults.getHits().getHits()).map(x -> {
			try {
				// System.out.println("ccc:" + x.getSourceAsString());
				var tmp = esmapper.readValue(x.getSourceAsString(), LabelCampaignSO.class);
				if (tmp != null) {
					tmp.ProDuctIDList = !Utils.StringIsEmpty(tmp.ProDuctIDList)
							? "," + tmp.ProDuctIDList.replace("  ", " ").replace(" ", ",") + ","
							: "";
				}
				return tmp;
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
		}).filter(x -> x != null).toArray(LabelCampaignSO[]::new);

		return listSO;
	}

	public ProductSO getProductSOById(int productId, int siteId) throws IOException {

		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("ProductID", productId));
		q.must(termQuery("SiteID", siteId));
		sb.from(0).size(1).query(q);

		var searchRequest = new SearchRequest("ms_product");
		searchRequest.source(sb);

		SearchResponse queryResults = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		var idlist2 = Stream.of(queryResults.getHits().getHits()).map(x -> {
			try {
				return esmapper.readValue(x.getSourceAsString(), ProductSO.class);
			} catch (IOException e1) {
				return null;
			}
		}).filter(x -> x != null).toArray(ProductSO[]::new);

		if (idlist2 != null && idlist2.length > 0) {
			return idlist2[0];
		} else
			return null;

		// org.elasticsearch.search.SearchHit[] searchHits =
		// searchResponse.getHits().getHits();

//		SearchSourceBuilder searchBuilder = new SearchSourceBuilder();
//		QueryBuilder boolQuery = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("ProductID", productId))
//				.must(QueryBuilders.matchQuery("SiteID", siteId));
//		searchBuilder.query(boolQuery);
//		SearchResponse searchResponse = elasticClient.searchRequest(searchBuilder, "ms_product");
//		ProductSO product = elasticClient.getSingleSource(searchResponse, ProductSO.class);
//		return product;
	}

//	public int[] getListrProductFromPreProduct(int PreProductID) throws Throwable{
//		if(config.DATACENTER == 3 || config.DATACENTER == 6) {
//			var products = factoryRead.queryFunction("product_PreNextGetbyID", PrenextProduct[].class, PreProductID);
//			if(products != null && products.length > 0) {
//				//var aaa = Stream.of(products).flatMapToInt(x -> x.listproductidpre).toArray();
//				//return Stream.of(products).flatMapToInt(x -> x.listproductidpre).toArray();
//			}
//			return null;
//		}
//		return null;
//	}

	public int[] searchProvinceByProductCode(String productCode, int brandID) throws Throwable {
		int[] storeIDs = SearchStoreByProductCode(productCode, brandID, 0, 0, new BooleanWrapper());
		var w = factoryRead.queryFunctionCached("store_getValidProvince", ORIntArrWrapper[].class, storeIDs);
		return w == null || w.length == 0 ? new int[0] : w[0].intresult;
	}

	public int[] searchDistrictByProductCode(String productCode, int provinceID, int brandID) throws Throwable {
		int[] storeIDs = SearchStoreByProductCode(productCode, brandID, provinceID, 0, new BooleanWrapper());
		var w = factoryRead.queryFunction("store_getValidDistrict", ORIntArrWrapper[].class, storeIDs);
		return w == null || w.length == 0 ? new int[0] : w[0].intresult;
	}

	public List<ProductSO> getProductNames(int[] productIDs, int siteID, String languageID) throws Throwable {
		var q = boolQuery().must(termsQuery("ProductID", productIDs)).must(termQuery("SiteID", siteID))
				.must(termQuery("Lang", DidxHelper.GenTerm3(languageID)));
		var sb = new SearchSourceBuilder().size(500).fetchSource(
				new String[] { "ProductID", "CategoryID", "ProductCodeTotalQuantity", "ProductName" }, null).query(q);
		var sr = new SearchRequest(CurrentIndexDB).source(sb);

		SearchResponse r = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			r = clientIndex.search(sr, RequestOptions.DEFAULT);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		return Stream.of(r.getHits().getHits()).map(x -> {
			try {
				return mapper.readValue(x.getSourceAsString(), ProductSO.class);
			} catch (IOException e) {
				return null;
			}
		}).filter(x -> x != null).collect(Collectors.toList());
	}

	public Map<Integer, ProductSO> getProductNamesMap(int[] productIDs, int siteID, String languageID)
			throws Throwable {
		var q = boolQuery().must(termsQuery("ProductID", productIDs)).must(termQuery("SiteID", siteID))
				.must(termQuery("Lang", DidxHelper.GenTerm3(languageID)));
		var sb = new SearchSourceBuilder().size(500).fetchSource(
				new String[] { "ProductID", "CategoryID", "ProductCodeTotalQuantity", "ProductName" }, null).query(q);
		var sr = new SearchRequest(CurrentIndexDB).source(sb);

		SearchResponse r = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			r = clientIndex.search(sr, RequestOptions.DEFAULT);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		return Stream.of(r.getHits().getHits()).map(x -> {
			try {
				return mapper.readValue(x.getSourceAsString(), ProductSO.class);
			} catch (IOException e) {
				return null;
			}
		}).filter(x -> x != null).collect(Collectors.toMap(x -> x.ProductID, x -> x));
	}

	public Map<Integer, ProductSO> getSellingProductNamesMap(int[] productIDs, int siteID, String languageID)
			throws Throwable {
		var q = boolQuery().must(termsQuery("ProductID", productIDs)).must(termQuery("SiteID", siteID))
				.must(termQuery("Lang", DidxHelper.GenTerm3(languageID))).must(termsQuery(
						"Prices.WebStatusId_" + DidxHelper.getDefaultProvinceIDBySiteID(siteID), new int[] { 4, 5 }));
		var sb = new SearchSourceBuilder().size(10000).fetchSource(
				new String[] { "ProductID", "CategoryID", "ProductCodeTotalQuantity", "ProductName" }, null).query(q);
		var sr = new SearchRequest(CurrentIndexDB).source(sb);

		SearchResponse r = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			r = clientIndex.search(sr, RequestOptions.DEFAULT);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		return Stream.of(r.getHits().getHits()).map(x -> {
			try {
				return mapper.readValue(x.getSourceAsString(), ProductSO.class);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}).filter(x -> x != null).collect(Collectors.toMap(x -> x.ProductID, x -> x));
	}

	public int[] Ela_getHomePageProduct2020(HomePageQuery qry) throws Throwable {
		if (qry.homeType == SearchType.PRODUCT) {
			// lấy sản phẩm theo cate => áp dụng cho dt, tablet, laptop ở trang chủ
			return getHelperBySite(qry.siteID).GetHomePageProduct2020(qry.category, qry.pageSize, qry.provinceID);
		}
		if (qry.homeType == SearchType.ACCESSORY) {
			// GetAccessoryHomePageProductNew lấy phụ kiện ở trang chủ
			return getHelperBySite(qry.siteID).GetAccessoryHomePageProductNew(qry.manuID, qry.countPerCat,
					qry.provinceID);
		}
		if (qry.homeType == SearchType.ACCESSORYAPPLE) {
			return getHelperBySite(qry.siteID).GetAccessoryHomePageProductApple2021(qry.provinceID);
		}

		if (qry.homeType == SearchType.GENUINEACCESSORY) {
			// phụ kiện chính hãng
			return getHelperBySite(qry.siteID).GetGenuineAccessoryHomePage(qry.ListManuName, qry.provinceID);
		}

		return null;
	}

	public int[] Elas_GetApplianceHasOnlinePrice2019(int provinceID, int pageIndex, int pageSize, int[] excludeProducts,
			long total, int[] cateID) {
		var dateTime = new Date();
		SearchSourceBuilder sb = new SearchSourceBuilder();
		var query = boolQuery();
		query.must(termQuery("prices.WebStatusId_" + provinceID + "_2", new String[] { "4" }));
		if (cateID != null && cateID.length > 0) {
			query.must(termQuery("CategoryID", cateID)); // .net conver sang String
		} else {
			query.must(termQuery("CategoryID",
					new int[] { 4645, 462, 1922, 1982, 1983, 1984, 1985, 1986, 1987, 1988, 1989, 1990, 1991, 1992, 2062,
							2063, 2064, 2084, 2222, 2262, 2302, 2322, 2342, 2142, 3305, 5473, 2428, 3385, 5105, 7367,
							5554, 5475, 7498, 7419, 7278, 7458, 7684 }));
		}
		if (excludeProducts != null) {
			query.must(termQuery("ProductID", excludeProducts));
		}
//      filter &= Filter<ProductSO>.Range(r => r.Greater(0).OnField("prices.PriceOnline_" + provinceId + "_2"));
//
//      //thay the script. chi lay san pham co gia online < gia thuong
//      filter &= Filter<ProductSO>.Range(r => r.Greater(0).OnField("prices.Diff_Price_PriceOnline_" + provinceId + "_2"));

		sb.from(pageSize * pageIndex).size(pageSize)
				.sort(scriptSort(
						new Script("doc['prices.Diff_Price_PriceOnline_" + provinceID
								+ "_2'].value*100/doc['prices.Price_" + provinceID + "_2'].value"),
						ScriptSortType.NUMBER).order(SortOrder.DESC));
		sb.query(query);
		var searchRequest = new SearchRequest(config.ELASTICSEARCH_PRODUCT_INDEX);
		searchRequest.source(sb);
		SearchResponse response = null;
		int[] mangID;
		try {

//			var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//			var clientIndex1 = elasticClient1.getClient();
//			try {

				response = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

//			} catch (Throwable e) {
//
//				Logs.LogException(e);
//				throw e;
//			} finally {
//				elasticClient1.close();
//			}

			response.getHits().forEach(h -> {
				try {
					var productSO = mapper.readValue(h.getSourceAsString(), ProductSO.class);
				} catch (Exception e) {
					Logs.LogException(e);
				}
			});
		} catch (Exception e) {
			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			Logs.LogException(e);
		}

//         total = queryResults.Total;
//         var result = GetListProductByListRedisKey(2, provinceId, queryResults.Documents.Select(s => s.RD_KEY), "GetProduct");
//         #region timer
//
//         TimeSpan tsTimeCall = DateTime.Now - dts;
//         if (tsTimeCall.Seconds >= 15)
//             _objSystemSvc.WriteLogError("GetApplianceHasOnlinePrice2019: " + tsTimeCall,
//                $"ProductSvc -> GetApplianceHasOnlinePrice2019({provinceId} {PageIndex} {PageSize} { ExcludeProducts} { CateID})", "API",
//                 $"Hàm xử lấy dữ liệu quá lâu: {tsTimeCall.Seconds}s");
//
//         #endregion
//         return result;

		return null;
	}

	/*
	 * */

	public ProductBOSR getListProductPromotionBanKem(PromotionQuery query) throws Throwable {
		Date now = new Date();

		// 1 - lay cac sp ban kem
		var q1 = boolQuery().must(termsQuery("PromotionID", query.PromotionIds)).must(rangeQuery("EndDate").gte(now))
				.must(rangeQuery("BeginDate").lte(now));
		var sb1 = new SearchSourceBuilder().size(100).query(q1).fetchSource(new String[] { "listProductBanKem" }, null);
		var sr1 = new SearchRequest(PromotionIndex).source(sb1);

		SearchResponse r1 = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			r1 = clientIndex.search(sr1, RequestOptions.DEFAULT);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		var productBK = Stream.of(r1.getHits().getHits()).flatMap(x -> {
			try {
				return Stream.of(esmapper.readValue(x.getSourceAsString(), PromotionBanKemBO.class).listProductBanKem);
			} catch (Exception e) {
				return Stream.empty();
			}
		}).collect(Collectors.toList());

		List<Integer> bkAcc = null;

		// 2 - khi truyen product
		int categoryID = 0;
		if (query.ProductId > 0) {
			categoryID = categoryHelper.getCategoryIDByProductIDFromCache(query.ProductId);
			// 2.1 - sort ton kho giam dan doi voi camera giam sat
			// thật ra chỗ này ko còn ý nghĩa gì nên comment
//			if (categoryID == 4728) {
//				productBK.sort(Comparator.comparingInt(x -> -x.ProductCodeTotalQuantity));
//			}
			// 2.2 - voi cate 42, 522, loai sp khong nam trong danh sach phu kien mua kem
			if (categoryID == 42 || categoryID == 522 || categoryID == 4728) {
				bkAcc = IntStream.of(getCachedProductBankem(query.ProductId)).boxed().collect(Collectors.toList());
			}
		}

		// 3 - loc lai ds san pham
		int[] listIDBK = productBK.stream().mapToInt(x -> x.ProductID).distinct().toArray();

		int[] loaitru = null;
		if (bkAcc != null) {
			var fbkAcc = bkAcc;
			loaitru = IntStream.of(listIDBK).filter(x -> !fbkAcc.contains(x)).toArray();
		}
		var q2 = boolQuery().must(termsQuery("ProductID", listIDBK))
				.must(termsQuery("Prices.WebStatusId_" + query.ProvinceId, SellingProductStatusID))
				.must(termQuery("SiteID", query.SiteId))
				.must(termQuery("Lang", DidxHelper.GenTerm3(DidxHelper.getLangBySiteID(query.SiteId))));

		if (loaitru != null) {
			// loại bỏ sp ko phải đồng hồ hoặc ko nằm trong danh sách
			q2.must(boolQuery() //
					.should(termQuery("CategoryID", 7264)) //
					.should(boolQuery().mustNot(termsQuery("ProductID", loaitru))) //
			);
		}

		// loại bỏ sp olol
		q2.must(boolQuery() //
				.should(termQuery("IsOnlineOnly", false)) //
				.should(rangeQuery("OnlineOnlyFromDate").gt("now/d")) // ctr chua bat dau
				.should(rangeQuery("OnlineOnlyToDate").lt("now/d")) // ctr da ket thuc
		);

		// loại bỏ: ID ngành hàng 1363 (miếng dán màn hình) hoặc
		// objPrice.DeliveryVehicles == 1
		q2.mustNot(termQuery("CategoryID", 1363));

		// sp chính thuộc cate 4728 (camera) thì chỉ lấy sp mua kèm thuộc 2 cate 9499
		// (adapter sạc), 55 (thẻ nhớ)
		if (categoryID == 4728) {
			q2.must(termsQuery("CategoryID", new int[] { 9499, 55 }));
		}

		var sb2 = new SearchSourceBuilder().query(q2);
		var sr2 = new SearchRequest(CurrentIndexDB).source(sb2);
		var finalResult = new ProductBOSR();
		int[] ids = null;
		List<Integer> idsList = null;
		if (query.CategoryId <= 0) {
			// trang tong hop khuyen mai ban kem theo tieu chi
			sb2.size(0).aggregation(terms("CategoryID").field("CategoryID").size(100)
					.subAggregation(topHits("top1ProductHits").size(1).sort("ProductCodeTotalQuantity", SortOrder.DESC)
							.fetchSource(new String[] { "ProductID", "CategoryID", "ProductCodeTotalQuantity" },
									null)));

			SearchResponse r2 = null;
//			var elasticClient2 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//			var clientIndex2 = elasticClient2.getClient();
//			try {

				r2 = clientIndex.search(sr2, RequestOptions.DEFAULT);
//			} catch (Throwable e) {
//
//				Logs.LogException(e);
//				throw e;
//			} finally {
//				elasticClient2.close();
//			}

			List<FaceCategorySR> faceCatL = new ArrayList<>();
			idsList = ((ParsedLongTerms) r2.getAggregations().get("CategoryID")).getBuckets().stream().flatMap(x -> {
				faceCatL.add(new FaceCategorySR() {
					{
						categoryID = x.getKeyAsNumber().intValue();
						productCount = (int) x.getDocCount();
					}
				});
				return Stream.of(((TopHits) x.getAggregations().get("top1ProductHits")).getHits().getHits()).map(y -> {
					try {
						return esmapper.readValue(y.getSourceAsString(), ProductSO.class).ProductID;
					} catch (IOException e) {
						return -1;
					}
				}).filter(y -> y > 0);
			}).collect(Collectors.toList());
			finalResult.faceListCategory = faceCatL.toArray(FaceCategorySR[]::new);
			finalResult.rowCount = (int) r2.getHits().getTotalHits().value;
		} else {
			q2.must(termQuery("CategoryID", query.CategoryId));
			sb2.from(query.PageIndex).size(query.PageSize).sort("Prices.Price_" + query.ProvinceId, SortOrder.DESC);

			SearchResponse r2 = null;
//			var elasticClient2 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//			var clientIndex2 = elasticClient2.getClient();
//			try {

				r2 = clientIndex.search(sr2, RequestOptions.DEFAULT);
//			} catch (Throwable e) {
//
//				Logs.LogException(e);
//				throw e;
//			} finally {
//				elasticClient1.close();
//			}

			// var r2 = clientIndex.search(sr2, RequestOptions.DEFAULT);
			idsList = Stream.of(r2.getHits().getHits()).map(x -> {
				try {
					return esmapper.readValue(x.getSourceAsString(), ProductSO.class).ProductID;
				} catch (IOException e) {
					return -1;
				}
			}).filter(x -> x > 0).collect(Collectors.toList());
			finalResult.rowCount = (int) r2.getHits().getTotalHits().value;
		}
		// 4 - loc array id ban dau
		final var fIdsList = idsList;
		ids = IntStream.of(listIDBK).filter(x -> fIdsList.contains(x)).toArray();

		// 5 - lay thong tin sp
		finalResult.productList = GetSimpleProductListByListID_PriceStrings_soMap(ids, query.SiteId, query.ProvinceId,
				DidxHelper.getLangBySiteID(query.SiteId));

		// 6 - sort theo ton kho nhieu nhat
		Arrays.sort(finalResult.productList,
				Comparator.comparingInt(x -> x.ProductErpPriceBO == null ? 0 : -x.ProductErpPriceBO.Quantity));

		return finalResult;
	}

	public PromotionProductBanKemBO[] getPromotionsBanKemInfo(PromotionQuery query, CodeTimers timer) throws Throwable {
		if (query.PromotionIds == null || query.PromotionIds.isEmpty() || query.ProductIds == null
				|| query.ProductIds.size() == 0) {
			return null;
		}

		timer.start("accessory");
		List<Integer> bkAcc = null;

		// khi truyen product
		if (query.ProductId > 0) {
			int categoryID = categoryHelper.getCategoryIDByProductIDFromCache(query.ProductId);
			// voi cate 42, 522, loai sp khong nam trong danh sach phu kien mua kem
			if (categoryID == 42 || categoryID == 522) {
				bkAcc = IntStream.of(getCachedProductBankem(query.ProductId)).boxed().collect(Collectors.toList());
			}
		}

		var fbkAcc = bkAcc;
		timer.pause("accessory");
		timer.start("query");
		var q1 = boolQuery().must(termsQuery("PromotionID", query.PromotionIds))
				.must(rangeQuery("EndDate").gte("now/d")).must(rangeQuery("BeginDate").lte("now/d"));
		var sb1 = new SearchSourceBuilder().size(100).query(q1);
		var sr1 = new SearchRequest(PromotionIndex).source(sb1);
		timer.pause("query");
		timer.start("ela");

		SearchResponse r1 = null;
//		var elasticClient2 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex2 = elasticClient2.getClient();
//		try {

			r1 = clientIndex.search(sr1, RequestOptions.DEFAULT);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient2.close();
//		}

		timer.pause("ela");
//		var lstPromoIDs = Arrays.stream(query.PromotionIds).boxed().collect(Collectors.toList());
		timer.start("parse");
		var r = Stream.of(r1.getHits().getHits()).flatMap(x -> {
			try {
				var item = esmapper.readValue(x.getSourceAsString(), PromotionBanKemBO.class);
				return Stream.of(item.listProductBanKem).filter(y -> {
					if (y.CategoryID == 2182) {
						return true;
					}
					if (query.ProductIds.contains(y.ProductID)
							&& (query.CategoryId == 0 || y.CategoryID == query.CategoryId)
							&& (fbkAcc == null || y.CategoryID == 7264 || fbkAcc.contains(y.ProductID))) {
						y.PromotionID = item.PromotionID;
						y.PromotionListGroupID = item.PromotionListGroupID;
						return true;
					}
					return false;
				});
			} catch (Exception e) {
				return Stream.empty();
			}
		}).sorted(Comparator.comparingInt(x -> query.PromotionIds.indexOf(x.PromotionID)))
				.filter(distinctByKey(x -> x.ProductID)).toArray(PromotionProductBanKemBO[]::new);
		timer.pause("parse");
		return r;
	}

	public String GetJsonFromObject(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	public Promotion[] getPromotionGroup(String recordID) throws Throwable {
		var promo = factoryRead.queryFunction("promotiongroup_getByrecordId", PromotionSubBrandBO[].class, recordID);
		if (promo == null || promo.length == 0) {
			return null;
		}
		promo[0].parse(mapper);
		return promo[0].parsed;
	}

	public Map<String, Promotion[]> getPromotionGroup(String[] recordID) throws Throwable {
		var promo = factoryRead.queryFunction("promotiongroup_getByrecordId", PromotionSubBrandBO[].class, recordID, 0);
		if (promo == null || promo.length == 0) {
			return new HashMap<>();
		}
		return Stream.of(promo).collect(Collectors.toMap(x -> x.recordid, x -> {
			x.parse(mapper);
			return x.parsed;
		}));
	}

	public int[] getProvince364() throws Throwable {
		var r = factoryRead.queryFunction("product_getProvince364", ORIntArrWrapper[].class);
		return r == null || r.length == 0 ? new int[0] : r[0].intresult;
	}

	public int[] getCachedProvince364() throws Throwable {
		String key = "getProvince364";
		String oKey = "cúc cu";
		var i = CachedObject.getObject(key, oKey, 10, int[].class);
		if (i == null) {
			i = getProvince364();
			CachedObject.putObject(key, oKey, i);
		}
		return i;
	}

	public ProductFeatureBO[] GetProductFeatureByListCate(int isShowHome, int[] ListCate, int siteID, int isPriority)
			throws Throwable {

		return factoryRead.QueryFunction("productfeature_getByListCate", ProductFeatureBO[].class, false, isShowHome,
				ListCate, siteID, isPriority);
	}

//	public ProductBOSR GetFeatureProductByCategoriesFromCache(Boolean homeOrCate, int[] lstcateIds, int provinceID,
//			int pageSize, int pageIndex, Boolean isMobile, int siteID, String langID, AsArray[] lstChangePosition)
//			throws Throwable {
//
////		if (GConfig.ProductTaoLao.containsKey(productID))
////			return null;
//		var keyLisstCate = "_";
//		if (lstcateIds != null && lstcateIds.length > 0) {
//			for (int item : lstcateIds) {
//				keyLisstCate += item + "_";
//			}
//		}
//		String key = "" + (homeOrCate ? "1" : "0") + "-" + keyLisstCate + "-" + provinceID + "-"
//				+ (isMobile ? "1" : "0") + "-" + siteID + "-" + langID;
//		ProductBOSR rs = null;
//		if (g_listProduct.containsKey(key)) {
//			rs = g_listProducts.get(key);
//			// lay data nhanh qua xin phep cache lai, sai ke me
//			if (System.currentTimeMillis() - Stream.of(rs.productList).findFirst().orElse(null).api_MemCacheDate > 30 * 1000) {
//				synchronized (ProductHelper.class) {
//					g_listProduct.remove(key);
//				}
//
//			} else {
//				Stream.of(rs).findFirst().orElse(null).api_MemCacheSource = "Memcached";
//				return rs;
//
//			}
//		}
//		rs = GetFeatureProductByCategories(homeOrCate, lstcateIds, provinceID, pageSize, pageIndex, isMobile, siteID,
//				langID, lstChangePosition);
//		if (rs == null)
//			return null;
//		Stream.of(rs).findFirst().orElse(null).api_MemCacheDate = System.currentTimeMillis();
//
//		synchronized (ProductHelper.class) {
//			g_listProducts.put(key, rs);
//		}
//		Stream.of(rs).findFirst().orElse(null).api_MemCacheSource = "ORDB";
//		return rs;
//
//	}

	public ProductBOSR GetFeatureProductByCategories(Boolean homeOrCate, int[] lstcateIds, int provinceID, int pageSize,
			int pageIndex, Boolean isMobile, int siteID, String langID, AsArray[] lstChangePosition) throws Throwable {

		ProductFeatureBO[] featureProducts = null;
		ProductBOSR productResult = new ProductBOSR();
		List<ProductFeatureBO> lstNewfeatureProducts = new ArrayList<ProductFeatureBO>();

		if (homeOrCate) {

			// featureProducts = factoryRead.QueryFunction("productfeature_getByListCate",
			// ProductFeatureBO[].class, false, 1, null, siteID, 1);
			featureProducts = GetProductFeatureByListCate(1, null, siteID, 1);

			for (ProductFeatureBO item : featureProducts) {
				if (item.Ispriority == 1) {
					if (item.FromDate != null && item.ToDate != null) {
						if (new Date().after(item.FromDate) && new Date().before(item.ToDate)) {
							lstNewfeatureProducts.add(item);
						}
					} else {
						lstNewfeatureProducts.add(item);
					}
				}
			}
			featureProducts = lstNewfeatureProducts.toArray(ProductFeatureBO[]::new);
			if (isMobile) {
				var newfeatureProducts = featureProducts;
				if (lstChangePosition != null && lstChangePosition.length > 0) {
					for (var item : lstChangePosition) {
						if (item.key <= newfeatureProducts.length && item.value <= newfeatureProducts.length) {
							featureProducts[item.value - 1] = newfeatureProducts[item.key - 1];

						}
					}
				}
			}
			// get list productID
		} else if (lstcateIds != null && lstcateIds.length > 0) {
//			featureProducts = factoryRead.QueryFunction("productfeature_getByListCate", ProductFeatureBO[].class, false,
//					0, lstcateIds, siteID, 1);
			featureProducts = GetProductFeatureByListCate(1, lstcateIds, siteID, 0);
		}
		if (featureProducts != null && featureProducts.length > 0) {
			var Product = new ArrayList<ProductBO>();

			var tmp = Stream.of(featureProducts).filter(x -> x != null).filter(x -> x.ProductID > 0)
					.mapToInt(x -> x.ProductID).skip(pageIndex * pageSize).limit(pageSize).toArray();
			if (pageSize > 30) {
				int page = (int) Math.ceil(tmp.length / 30);
				for (int i = 0; i < page; i++) {
					var listid = Stream.of(tmp).skip(i * 30).limit(30).toArray();
				}
			}
			productResult.productList = GetSimpleProductListByListID_PriceStrings_soMap(tmp, siteID, provinceID,
					langID);
			productResult.rowCount = featureProducts.length;

			return productResult;
		}
		return productResult;
	}

	public SuggestSearchSO[] GetSuggestSearch(String keyword, int siteID) throws Throwable {

		// getHelperBySite(siteID).processSimpleDetails(list, provinceID, siteID,
		// langID);
		return getHelperBySite(2).GetSuggestSearch(keyword, siteID);
	}

	public ProductPropUseManualBO[] GetPropUseManualByProductID(int productID, int siteID, String langID)
			throws Throwable {
		var result = factoryRead.queryFunctionCached("product_prop_usermanual_getByProductID",
				ProductPropUseManualBO[].class, productID, siteID, langID);
		return result;
	}

	public ProductBO[] productCategoryMapSelByProductID(int productID, int siteID) throws Throwable {
		// product_categoryMapSelByProductid
		var catemap = factoryRead.queryFunctionCached("product_categoryMapSelByProductid", ProductBO[].class, productID,
				siteID);
		if (catemap == null || catemap.length == 0) {
			return null;
		}
		return catemap;
	}

	public PromotionString[] GetPromotionByCode(String productCode, int siteID, String languageID) throws Throwable {
		var prices = factoryRead.queryFunction("product_GetPromobyProductCode", PromotionString[].class, productCode,
				siteID, languageID);
		return prices;
	}

	public String GetMonopolyLabel(ProductBO product, ProductDetailBO[] lstProductDetail) {

		try {
			if (lstProductDetail == null || lstProductDetail.length == 0)
				return "";
			int PropertyID = 0;
			int PropertyValueID = 0;
			switch (product.CategoryID) {
			case 1942: // tivi
				PropertyID = 26057;
				PropertyValueID = 182614;
				break;
			case 1943: // tủ lạnh
				PropertyID = 26089;
				PropertyValueID = 180862;
				break;
			case 166: // tủ đông
				PropertyID = 26090;
				PropertyValueID = 182880;
				break;
			case 1944: // máy giặt
				PropertyID = 26091;
				PropertyValueID = 180861;
				break;
			case 2202: // máy sấy
				PropertyID = 26092;
				PropertyValueID = 182882;
				break;
			case 2002: // máy lạnh
				PropertyID = 26093;
				PropertyValueID = 182877;
				break;
			case 2162: // loa
				PropertyID = 26094;
				PropertyValueID = 182878;
				break;
			case 1962: // máy nước nóng
				PropertyID = 26095;
				PropertyValueID = 182885;
				break;
			case 5475: // Máy rửa chén
				PropertyID = 26658;
				PropertyValueID = 182887;
				break;
			case 7264: // đồng hồ thời trang
				PropertyID = 21427;
				PropertyValueID = 182616;
				break;
			default:
				break;
			}
			var propid = PropertyID;
			var propvalue = PropertyValueID;
			ProductDetailBO prop = null;
			for (var x : lstProductDetail) {
				if (x != null && x.PropertyID == propid && x.PropValueID == propvalue) {
					prop = x;
				}
			}
//            var prop = lstProductDetail.stream().filter(x -> x.PropertyID == propid && x.PropValueID == propvalue).findFirst().orElse(null);
			if (prop != null) {
				product.MonopolyLabel = prop.PropertyID + "|" + prop.IconUrl;
			} else {
				product.MonopolyLabel = "";
			}

		} catch (Throwable e) {
			logs.LogException(e);
//			e.printStackTrace();
		}
		return product.MonopolyLabel;

	}

	public String GetMonopolyLabel2(ProductBO product) {

		try {
			var lstProductDetail = GetListPropProduct(product.ProductID, product.SiteID,
					product.ProductLanguageBO != null ? product.ProductLanguageBO.LanguageID : "vi-VN", product);
			int PropertyID = 0;
			int PropertyValueID = 0;
			switch (product.CategoryID) {
			case 1942: // tivi
				PropertyID = 26057;
				PropertyValueID = 182614;
				break;
			case 1943: // tủ lạnh
				PropertyID = 26089;
				PropertyValueID = 180862;
				break;
			case 166: // tủ đông
				PropertyID = 26090;
				PropertyValueID = 182880;
				break;
			case 1944: // máy giặt
				PropertyID = 26091;
				PropertyValueID = 180861;
				break;
			case 2202: // máy sấy
				PropertyID = 26092;
				PropertyValueID = 182882;
				break;
			case 2002: // máy lạnh
				PropertyID = 26093;
				PropertyValueID = 182877;
				break;
			case 2162: // loa
				PropertyID = 26094;
				PropertyValueID = 182878;
				break;
			case 1962: // máy nước nóng
				PropertyID = 26095;
				PropertyValueID = 182885;
				break;
			case 5475: // Máy rửa chén
				PropertyID = 26658;
				PropertyValueID = 182887;
				break;
			default:
				break;
			}
			var propid = PropertyID;
			var propvalue = PropertyValueID;
			var prop = lstProductDetail.stream().filter(x -> x.PropertyID == propid && x.PropValueID == propvalue)
					.findFirst().orElse(null);
			if (prop != null) {
				product.MonopolyLabel = prop.PropertyID + "|" + prop.IconUrl;
			} else {
				product.MonopolyLabel = "";
			}

		} catch (Throwable e) {
			logs.LogException(e);
//			e.printStackTrace();
		}
		return product.MonopolyLabel;

	}

	public InstallmentException[] getInstallmentException(int productID) throws Throwable {
		var result = factoryRead.queryFunctionCached("installment_exe_getByProID", InstallmentException[].class,
				productID);
		return result;
	}

	public void buildPropQuery(ProductQuery qry, BoolQueryBuilder query) throws Throwable {
		if (qry.CategoryId > 0 && qry.propValueIDs != null && qry.propValueIDs.size() > 0) {
			// prop & value cua cate
			var cateProps = GetProductPropByCategory(qry.CategoryId, qry.SiteId, qry.LanguageID);

			if (qry.CategoryId == 60) {
				// hardcode ốp lưng
				String prop = qry.propValueIDs.stream().map(x -> "*" + x + "*").collect(Collectors.joining(" OR "));
				query.must(queryStringQuery(prop).field("PropStr").defaultOperator(Operator.AND));
				return;
			}

			// build queries
			var qStrs = Arrays.stream(cateProps).map(p -> {
				String operator = p.isAndFilter() ? " AND " : " OR ";
				return p.ProductPropValueBOLst.stream().filter(v -> qry.propValueIDs.contains(v.ValueID))
						.map(v -> "prop" + p.PropertyID + "_" + v.ValueID).collect(Collectors.joining(operator));
			}).filter(s -> !Strings.isNullOrEmpty(s)).map(s -> "(" + s + ")").collect(Collectors.joining(" AND "));
			if (!Strings.isNullOrEmpty(qStrs)) {
				query.must(queryStringQuery(qStrs).field("PropStr").defaultOperator(Operator.AND));
			}
		}
	}

	public ProductColorBO[] getColorInfor(String colorID, String langID) throws Throwable {
		var result = factoryRead.queryFunctionCached("product_color_getDetail", ProductColorBO[].class, colorID,
				langID);
		return result;
	}

	public ProductBOSR GetFullAccessoryByProductIDDetail(int productID, int provinceID, int siteID, String lang,
			int categoryID, int pageIndex, int pageSize, CodeTimers timer) {
		ProductBOSR result = new ProductBOSR();
		try {
			timer.start("db");
			var getlistProductID = factoryRead.queryFunctionCached("accessory_getByProductID", ProductBO[].class,
					productID);
			timer.pause("db");

			if (getlistProductID != null && getlistProductID.length > 0) {
				var listID = Arrays.stream(getlistProductID).mapToInt(x -> x.ProductID).toArray();
				if (listID == null && listID.length == 0)
					return null;

				int per = 1;
				int totalcate = 10;
				var from = 0;

				var q = boolQuery();
				q.must(termsQuery("ProductID", listID));
				q.must(termQuery("ProductType", 1));
				q.must(termQuery("SiteID", siteID));
				q.must(termQuery("IsCollection", 0));
				q.must(termQuery("Lang", DidxHelper.GenTerm3(lang)));
				q.must(termsQuery("Prices.WebStatusId_" + provinceID, new int[] { 3, 4, 11 }));
				q.must(termQuery("Prices.IsShowHome_" + provinceID, 1));
				q.must(termQuery("HasBimage", 1));

				if (categoryID > 0) {
					q.must(termQuery("CategoryID", categoryID));
					totalcate = 1;
					per = pageSize;
					from = pageIndex * pageSize;
				} else {
					if (categoryID == 0) {
						per = 10; // tối đa lấy 8 sp mỗi cate
						totalcate = 20;// tối đa lấy 20 cate
					}
					// lấy phụ kiện mua kèm
					// categoryID = -1 lấy mỗi cate 1 sp tối đa 10 sp.
					// categoryid = 0 lấy mỗi cate 30 sp, tối đa 20 cate
					// category > 0 lấy theo cate, tối đa 30 sp.
				}

				var sb = new SearchSourceBuilder()
						.query(q).from(
								0)
						.size(0).fetchSource(new String[] { "ProductID" }, null)
						.aggregation(terms("categoryID").field("CategoryID").size(totalcate).subAggregation(
								topHits("topCategoryHits").fetchSource(new String[] { "ProductID" }, null).from(from)
										.size(per).sort("Scenario", SortOrder.DESC)
										.sort("Prices.Price_" + provinceID, SortOrder.ASC)
										.sort("DateCreated", SortOrder.DESC)

						)).aggregation(terms("FacetTermCategoryID").field("CategoryID").size(100)
								.subAggregation(count("Sum").field("CategoryID")));

				SearchResponse queryResults = null;
//				var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//				var clientIndex1 = elasticClient1.getClient();
//				try {

					queryResults = clientIndex.search(new SearchRequest(CurrentIndexDB).source(sb),
							RequestOptions.DEFAULT);

//				} catch (Throwable e) {
//
//					Logs.LogException(e);
//					throw e;
//				} finally {
//					elasticClient1.close();
//				}

				var aggrs = queryResults.getAggregations();
				ParsedLongTerms bucket = (ParsedLongTerms) aggrs.get("categoryID");
				ParsedLongTerms catebucket = (ParsedLongTerms) aggrs.get("FacetTermCategoryID");
				if (bucket == null || bucket.getBuckets().size() == 0)
					return null;
				var listIDResult = new ArrayList<Integer>();
				List<FaceCategorySR> catelist = new ArrayList<FaceCategorySR>();

				bucket.getBuckets().stream().map(v -> (TopHits) v.getAggregations().get("topCategoryHits"))
						// .collect(Collectors.toList())
						.forEach(h -> h.getHits().forEach(v -> {
							try {

								var so = mapper.readValue(v.getSourceAsString(), ProductSO.class);
								listIDResult.add(so.ProductID);
							} catch (IOException e) {
								Logs.LogException(e);
							}
						}));

				bucket.getBuckets().forEach(b -> {
					catelist.add(new FaceCategorySR() {
						{
							categoryID = b.getKeyAsNumber().intValue();
							productCount = (int) b.getDocCount();
							score = 0;// (int) catescore;
							hasProductInStock = true;// hasproductinstock == 1;

						}
					});
				});
//				if(catebucket != null && catebucket.getBuckets() != null && catebucket.getBuckets().size() > 0) {
//					catebucket.getBuckets().forEach(b -> {
//	                    catelist.add(new FaceCategorySR() {
//	                        {
//	                            categoryID = b.getKeyAsNumber().intValue();
//	                            productCount = (int) b.getDocCount();
//	                            score = 0;//(int) catescore;
//	                            hasProductInStock = true;//hasproductinstock == 1;
//	
//	                        }
//	                    });
//	                });
//				}
				if (listIDResult.size() > 0) {
					var listpros = GetSimpleProductListByListID_PriceStrings_soMap(
							listIDResult.stream().mapToInt(Integer::intValue).toArray(), siteID, provinceID, lang);
					result.productList = listpros;
				}
				result.faceListCategory = (FaceCategorySR[]) catelist.toArray(FaceCategorySR[]::new);
				if (result.faceListCategory != null && result.faceListCategory.length > 0) {
					var sum = Arrays.stream(result.faceListCategory).map(item -> item.productCount).reduce(0,
							(a, b) -> a + b);
					result.rowCount = sum != null ? sum : 0;
				}

			}
		} catch (Throwable e) {
			logs.LogException(e);
			result.message = e.toString();
		}
		return result;
	}

	public ProductBOSR GetFullAccessoryByProductIDDetail2(int productID, int provinceID, int siteID, String lang,
			int categoryID, int pageIndex, int pageSize, CodeTimers timer) throws Throwable {

		ProductBOSR result = new ProductBOSR();
		String typeKey = "accessory_getByProductIDDetail2";
		String objKey = String.valueOf("accessory" + "_" + productID + "_" + provinceID + "_" + lang + "_" + categoryID
				+ "_" + pageIndex + "_" + pageSize);
		result = CachedObject.getObject(typeKey, objKey, 1440, ProductBOSR.class);

		if (result == null) {

			result = new ProductBOSR();
			var product = GetSimpleProductListByListID_PriceStrings_soMap(new int[] { productID }, siteID, provinceID,
					lang);
			if (product == null || product.length == 0) {
				return null;
			}
			try {
				timer.start("db");
				var getlistProductID = factoryRead.queryFunction("accessory_getByProductID", ProductBO[].class,
						productID);
				timer.pause("db");
				if (getlistProductID != null && getlistProductID.length > 0) {
					var listID = Arrays.stream(getlistProductID).mapToInt(x -> x.ProductID).toArray();
					if (listID == null && listID.length == 0)
						return null;

//					ObjectMapper mapper = new ObjectMapper();
//					var xx = Arrays.asList(mapper.readValue(Paths.get("json.json").toFile(), xxxx[].class));
//					var listIDx = xx.stream().mapToInt(x -> x.productid).toArray();	
					int per = 1;
					int totalcate = 10;
					var from = 0;
					var q = boolQuery();
					q.must(termsQuery("ProductID", listID));
					q.must(termQuery("ProductType", 1));
					q.must(termQuery("SiteID", siteID));
					q.must(termQuery("IsCollection", 0));
					q.must(termQuery("Lang", DidxHelper.GenTerm3(lang)));
					q.must(termsQuery("Prices.WebStatusId_" + provinceID, new int[] { 3, 4, 11 }));
					q.must(termQuery("Prices.IsShowHome_" + provinceID, 1));
					q.must(termQuery("HasBimage", 1));
					if (categoryID > 0) {
						q.must(termQuery("CategoryID", categoryID));
						totalcate = 1;
						per = pageSize;
						from = pageIndex * pageSize;
					} else {
						if (categoryID == 0) {
							per = 10; // tối đa lấy 8 sp mỗi cate
							totalcate = 20;// tối đa lấy 20 cate
						}
						// lấy phụ kiện mua kèm
						// categoryID = -1 lấy mỗi cate 1 sp tối đa 10 sp.
						// categoryid = 0 lấy mỗi cate 30 sp, tối đa 20 cate
						// category > 0 lấy theo cate, tối đa 30 sp.
					}
					var sb = new SearchSourceBuilder();

					if (categoryID < 0) {
						String manuName = "";
						manuName = product[0].ProductManuBO != null
								? DidxHelper.GenTermKeyWord(product[0].ProductManuBO.ManufacturerName)
								: "";

						HashMap<String, Object> scriptParams = new HashMap<>();
						scriptParams.put("Manus", new String[] { manuName });
						scriptParams.put("manusIphone", null);

						if (manuName.contains("iphone")) {
							scriptParams.put("manusIphone",
									new String[] { manuName, "iphone", "iphone__apple", "apple" });
						}
						q.must(QueryBuilders.functionScoreQuery(new FunctionScoreQueryBuilder.FilterFunctionBuilder[] {
								new FunctionScoreQueryBuilder.FilterFunctionBuilder(scriptFunction(new Script(
										ScriptType.INLINE, "painless",
										"if(doc['Prices.PromotionDiscountPercent_" + provinceID
												+ "'].length > 0 && doc['Prices.PromotionDiscountPercent_" + provinceID
												+ "'].value > 0)  doc['Prices.PromotionDiscountPercent_" + provinceID
												+ "'].value; ",
										scriptParams))),
								new FunctionScoreQueryBuilder.FilterFunctionBuilder(scriptFunction(new Script(
										ScriptType.INLINE, "painless",
										"if(params.Manus.contains(doc['ManufacturerName'].value + \"\")){ 1000 } ",
										scriptParams))),
								new FunctionScoreQueryBuilder.FilterFunctionBuilder(scriptFunction(new Script(
										ScriptType.INLINE, "painless",
										"if((params.manusIphone != null) && (params.manusIphone.contains(doc['ManufacturerName'].value + \"\"))){ 1000 } ",
										scriptParams)))

						}).scoreMode(FunctionScoreQuery.ScoreMode.SUM).boostMode(CombineFunction.SUM));
						sb.query(q).from(0).size(100).fetchSource(new String[] { "ProductID", "CategoryID" }, null);
					} else {
						sb.query(q).from(
								0).size(
										0)
								.fetchSource(new String[] { "ProductID" }, null)
								.aggregation(terms("categoryID").field("CategoryID").size(totalcate)
										.subAggregation(topHits("topCategoryHits")
												.fetchSource(new String[] { "ProductID" }, null).from(from).size(per)
												.sort("Scenario", SortOrder.DESC)
												.sort("Prices.Price_" + provinceID, SortOrder.ASC)
												.sort("DateCreated", SortOrder.DESC)))
								.aggregation(terms("FacetTermCategoryID").field("CategoryID").size(100)
										.subAggregation(count("Sum").field("CategoryID")));
					}

					SearchResponse queryResults = null;
//					var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//					var clientIndex1 = elasticClient1.getClient();
//					try {

						queryResults = clientIndex.search(new SearchRequest(CurrentIndexDB).source(sb),
								RequestOptions.DEFAULT);

//					} catch (Throwable e) {
//
//						Logs.LogException(e);
//						throw e;
//					} finally {
//						elasticClient1.close();
//					}

					var listIDResult = new ArrayList<Integer>();
					List<FaceCategorySR> catelist = new ArrayList<FaceCategorySR>();
					if (categoryID >= 0) {
						var aggrs = queryResults.getAggregations();
						ParsedLongTerms bucket = (ParsedLongTerms) aggrs.get("categoryID");
						if (bucket == null || bucket.getBuckets().size() == 0)
							return null;

						bucket.getBuckets().stream().map(v -> (TopHits) v.getAggregations().get("topCategoryHits"))
								.forEach(h -> h.getHits().forEach(v -> {
									try {

										var so = mapper.readValue(v.getSourceAsString(), ProductSO.class);
										listIDResult.add(so.ProductID);
									} catch (IOException e) {
										Logs.LogException(e);
									}
								}));

						bucket.getBuckets().forEach(b -> {
							catelist.add(new FaceCategorySR() {
								{
									categoryID = b.getKeyAsNumber().intValue();
									productCount = (int) b.getDocCount();
									score = 0;// (int) catescore;
									hasProductInStock = true;// hasproductinstock == 1;

								}
							});
						});

					} else {
						var map = new ArrayList<Integer>();
						queryResults.getHits().forEach(h -> {
							if (map.size() <= 10) {
								try {
									var so = esmapper.readValue(h.getSourceAsString(), ProductSO.class);
									if (map.isEmpty()) {
										map.add(so.CategoryID);
										listIDResult.add(so.ProductID);
									} else {
										var check = map.stream().filter(i -> i == so.CategoryID).toArray();
										if (check.length == 0) {
											map.add(so.CategoryID);
											listIDResult.add(so.ProductID);
										}
									}
								} catch (Exception e) {
									// result.message = e.toString();
								}
							}
						});
						int total = (int) queryResults.getHits().getTotalHits().value;
						result.rowCount = total;
					}

					if (listIDResult.size() > 0) {
						var listpros = GetSimpleProductListByListID_PriceStrings_soMap(
								listIDResult.stream().mapToInt(Integer::intValue).toArray(), siteID, provinceID, lang);
						result.productList = listpros;
					}
					result.faceListCategory = (FaceCategorySR[]) catelist.toArray(FaceCategorySR[]::new);
					if (result.faceListCategory != null && result.faceListCategory.length > 0) {
						var sum = Arrays.stream(result.faceListCategory).map(item -> item.productCount).reduce(0,
								(a, b) -> a + b);
						result.rowCount = sum != null ? sum : 0;
					}
				}

				CachedObject.putObject(typeKey, objKey, result);

			} catch (Throwable e) {
				Logs.LogException(e);
				result.message = e.toString();
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	public int[] getValidCategoryIDsFromCache(int siteID, String langID) throws Throwable {
		String key = "getValidCategoryIDs" + siteID + "_" + langID;
		var rs = (int[]) CacheStaticHelper.GetFromCache(key, 120);
		if (rs == null) {
			rs = getValidCategoryIDs(siteID, langID);
			CacheStaticHelper.AddToCache(key, rs);
		}
		return rs;

	}

	public int[] getValidCategoryIDs(int siteID, String langID) throws Throwable {
		var r = factoryRead.queryFunctionCached("product_GetCategoryIDsBySiteID", ORIntArrWrapper[].class, siteID,
				langID);
		return r == null || r.length == 0 ? null : r[0].intresult;
	}

	public Map<String, Double> getPriceAfterPromotion(int[] listproductID, int[] listprovinceID, int siteID,
			String langID) throws IOException {

		Map<String, Double> result = new HashMap<String, Double>();

		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termsQuery("ProductID", listproductID));
		q.must(termQuery("SiteID", siteID));
		q.must(termQuery("Lang", DidxHelper.GenTerm3(langID)));

		sb.from(0).size(100).query(q);

		var searchRequest = new SearchRequest(CurrentIndexDB).source(sb);

		SearchResponse searchResponse = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {
			searchResponse = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		List<ProductSO> productSO = Stream.of(searchResponse.getHits().getHits()).map(x -> {

			try {
				var test = esmapper.readValue(x.getSourceAsString(), ProductSO.class);
				return test;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}).collect(Collectors.toList());

		if (productSO != null) {
			for (var provinceid : listprovinceID) {
				productSO.stream().forEach(x -> {

					if (x.Prices != null) {
						var PriceAfterPromotion = x.Prices.get("PriceAfterPromotion_" + provinceid + "_" + siteID);
						if (PriceAfterPromotion == null
								|| (PriceAfterPromotion instanceof Integer && (Integer) PriceAfterPromotion == 0)) {
							PriceAfterPromotion = 0.0;
						}
						var xxx = x.Prices.get("PromotionDiscountPercent_" + provinceid);
						if (xxx == null || xxx instanceof Integer) {
							xxx = 0d;
						}
						var PromotionDiscountPercent = (Double) xxx;
						result.put(x.ProductID + "_" + provinceid, (Double) PriceAfterPromotion);
						result.put(x.ProductID + "_" + provinceid + "_PromotionDiscountPercent",
								PromotionDiscountPercent == 0 ? 0.0 : PromotionDiscountPercent.doubleValue());
					}
				});
			}

		}
		return result;
	}

	public String processShortName(ProductBO product, ProductDetailBO[] detail) {
		String result = "";
		// var listProperty = Arrays.stream(detail).map(x ->
		// x.PropertyID).distinct().collect(Collectors.toList());
		var lstProperty = Arrays.stream(detail).filter(x -> x != null)
//				.filter(distinctByKey(x -> x.PropertyID))
				.collect(Collectors.groupingBy(x -> x.PropertyID));

		String shortCodeName = product.ProductCategoryBO != null ? product.ProductCategoryBO.ShortCodeName : "";
		if (shortCodeName != null && shortCodeName.length() > 0) {
			result = Arrays.stream(shortCodeName.split("\\|")).filter(x -> x != null && x.length() > 0).map(x -> {
				if (x.equals("[hang]")) {
					x = product.ProductManuBO != null && !Strings.isNullOrEmpty(product.ProductManuBO.ManufacturerName)
							? product.ProductManuBO.ManufacturerName
							: "";
				} else if (x.toLowerCase().equals("[shortname]")) {
					x = product.ProductLanguageBO != null && !Strings.isNullOrEmpty(product.ProductLanguageBO.ShortName)
							? product.ProductLanguageBO.ShortName
							: "";
				} else {
					x = x.replaceAll("[^0-9,-]", "");
					List<Integer> listProByShortCode = null;
					if (x.contains(",")) {
						listProByShortCode = Arrays.stream(x.split(",")).map(k -> Utils.toInt(k))
								.collect(Collectors.toList());
					} else {
						var pid = Utils.toInt(x);
						if (lstProperty.containsKey(pid)) {
							listProByShortCode = List.of(pid);
						}
					}
					x = listProByShortCode != null && listProByShortCode.size() > 0
							? processProperty(product, detail, listProByShortCode)
							: "";
				}
				return x;
			}).collect(Collectors.joining(" "));
		} else {
			if (product.ProductLanguageBO != null && !Strings.isNullOrEmpty(product.ProductLanguageBO.ShortName)) {
				return result = product.ProductLanguageBO.ShortName;
			}
			if (!Strings.isNullOrEmpty(product.SEOName)) {
				return result = product.SEOName;
			}
			if (!Strings.isNullOrEmpty(product.ProductName)) {
				return result = product.ProductName;
			}
		}
		return result;
	}

	public String processShortName(ProductBO product) {
		String result = "";
		String shortCodeName = product.ProductCategoryBO != null ? product.ProductCategoryBO.ShortCodeName : "";
		if (shortCodeName != null && shortCodeName.length() > 0) {
			shortCodeName = shortCodeName.replace("][", "]|[");
			result = Arrays.stream(shortCodeName.split("\\|")).filter(x -> x != null && x.length() > 0).map(x -> {
				if (x.equals("[hang]")) {
					x = product.ProductManuBO != null && !Strings.isNullOrEmpty(product.ProductManuBO.ManufacturerName)
							? product.ProductManuBO.ManufacturerName
							: "";
				}
				if (x.toLowerCase().equals("[shortname]")) {
					x = product.ProductLanguageBO != null && !Strings.isNullOrEmpty(product.ProductLanguageBO.ShortName)
							? product.ProductLanguageBO.ShortName
							: "";
				}
				return x;
			}).collect(Collectors.joining(" "));
		} else {
			if (product.ProductLanguageBO != null && !Strings.isNullOrEmpty(product.ProductLanguageBO.ShortName)) {
				return result = product.ProductLanguageBO.ShortName;
			}
			if (!Strings.isNullOrEmpty(product.SEOName)) {
				return result = product.SEOName;
			}
			if (!Strings.isNullOrEmpty(product.ProductName)) {
				return result = product.ProductName;
			}
		}
		return result;

	}

	private List<Integer> listProperty(String shortCodeName) {
		String value = shortCodeName.replace(",", "|");
		var arrString = value.split("[|]");
		String strLstProp[] = null;
		List<Integer> lstPropByShortCode = new ArrayList<>();
		if (arrString != null && arrString.length > 0) {
			for (String _item : arrString) {
				if (Strings.isNullOrEmpty(_item))
					continue;
				String code = _item.trim().toLowerCase();
				if (code.equals("[hang]") || code.equals("[shortname]"))
					continue;
				code = code.replace("[", "").replace("]", ""); // lấy chuỗi lấy mảng propertyID
				lstPropByShortCode.add(Utils.toInt(code));
			}
		}
		return lstPropByShortCode;
	}

	private String processProperty(ProductBO product, ProductDetailBO[] detail, List<Integer> listProp) {
		String provinceIdS = "131,156,114,123,128,145,117,104,130,153,148,137,103,142,106,118,5,121,101,124,133,135,147,155,149,120,139,134,143,150,9";
		List<Integer> lstProvince = Arrays.stream(provinceIdS.split(",")).map(x -> Utils.toInt(x))
				.collect(Collectors.toList());
		String result = "";
		String regex = "[0-9.]+";
		var values = Arrays.stream(detail).map(x -> x.PropValueID).collect(Collectors.toList());
		// map tất cả PropertyId - Value
		var lstProperty = Arrays.stream(detail).filter(distinctByKey(x -> x.PropertyID))
				.collect(Collectors.toMap(x -> x.PropertyID, x -> Optional.ofNullable(x.Value).orElse("")));
		// filter PropertyId trong ShortCodeName
		var lstPropertyIds = lstProperty.entrySet().stream().filter(x -> listProp.contains(x.getKey()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		if (!Utils.isNullOrEmptyMap(lstPropertyIds)) {
			for (var checkMap : lstPropertyIds.entrySet()) {
				if (!checkMap.getValue().matches(regex))
					continue;
				var mapUnitTex = Arrays.stream(detail).filter(distinctByKey(x -> x.PropertyID))
						.collect(Collectors.toMap(x -> x.PropertyID, x -> Optional.ofNullable(x.unitText).orElse("")));
				String value = checkMap.getValue() + " " + mapUnitTex.get(checkMap.getKey()); // nếu value chỉ là số or
																								// dấu chấm thì gán thêm
																								// unitTex;
				lstPropertyIds.put(checkMap.getKey(), value);
			}
		}
		List<String> arrIgnore = Arrays.asList("không", "không có", "hãng không công bố", "đang cập nhật");
		switch (product.CategoryID) {
		case 1942: // tivi
			String type = ""; // loaitv
			String doPhanGiai = ""; // hãng tv
			String soInch = ""; // số inh
			String android = "";
			if (lstPropertyIds != null && lstPropertyIds.containsKey(7320)) {
//					type = android = values.contains(84816) ? "Android TV " : "Smart TV ";
//					if (values.contains(63136))
//						type = android + "QLED";
//					else if (values.contains(185049))
//						type = android + "Neo QLED";
//					else if (values.contains(45855))
//						type = android + "OLED";
//					else if (values.contains(60988))
//						type = "Internet TV";
//					if (values.contains(168548))
//						type = android + " NanoCell";
//					// lấy độ phân giải
//					if (values.contains(43376))
//						doPhanGiai = "4K ";
//					else if (values.contains(138328))
//						doPhanGiai = "8K ";
				if (values.contains(63136) && values.contains(84816))
					type = "Android TV QLED";
				else if (values.contains(185049))
					type = "Smart TV Neo QLED";
				else if (values.contains(63136))
					type = "Smart TV QLED";
				else if (values.contains(45855) && values.contains(84816))
					type = "Android TV OLED";
				else if (values.contains(45855))
					type = "Smart TV OLED";
				else if (values.contains(84816))
					type = "Android TV";
				else if (values.contains(45849))
					type = "Smart TV";
				else if (values.contains(60988))
					type = "Internet TV";
				if (values.contains(168548))
					type = type + " NanoCell";
			}
			if (lstPropertyIds.containsKey(6541)) {
				// lấy độ phân giải
				if (values.contains(43376))
					doPhanGiai = "4K ";
				else if (values.contains(138328))
					doPhanGiai = "8K ";
			}
			// Kích thước được khai báo trong CMS sản phẩm với Id thuộc tính : 6540
			if (lstPropertyIds.containsKey(6540)) {
				soInch = lstPropertyIds.get(6540).replace(" inch", "&quot;");
			}

			if (!Strings.isNullOrEmpty(type))
				result += " " + type;
			if (!Strings.isNullOrEmpty(doPhanGiai))
				result += " " + doPhanGiai;
			if (!Strings.isNullOrEmpty(soInch) && !arrIgnore.contains(soInch.toLowerCase()))
				result += " " + soInch;

			if (lstPropertyIds != null && lstPropertyIds.size() > 0) {
				var _tmpProp = lstPropertyIds.keySet().stream().filter(x -> x != 7320 && x != 6541 && x != 6540)
						.collect(Collectors.toList());
				if (_tmpProp != null && _tmpProp.size() > 0) {
					for (Integer keyProId : _tmpProp) {
						String propDetailValue = lstPropertyIds.get(keyProId);
						if (Strings.isNullOrEmpty(propDetailValue) || arrIgnore.contains(propDetailValue.toLowerCase()))
							continue;
						result += " " + propDetailValue;
					}
				}
			}
			break;
		case 1944: // Ngành hàng máy giăt
			String Inverter = "";
			String khoiLuongGiat = "";
			// Inverter của sản phẩm máy giặt
			if (lstPropertyIds != null && lstPropertyIds.size() > 0) {
				if (lstPropertyIds.containsKey(7987) && values.contains(49317) || values.contains(152413)) {
					Inverter = "Inverter";
				}
				// Khối lượng giặt của sp máy giặt
				if (lstPropertyIds.containsKey(6591)) {
					khoiLuongGiat = lstPropertyIds.get(6591);
				}
			}

			if (!Strings.isNullOrEmpty(Inverter))
				result += " " + Inverter;
			if (!Strings.isNullOrEmpty(khoiLuongGiat) && !arrIgnore.contains(khoiLuongGiat.toLowerCase()))
				result += " " + khoiLuongGiat;
			/**
			 * - Trường hợp lstPropertyID không chứa đồng thời cả 2 Inverter , khoi luong
			 * giat
			 */
			if (lstPropertyIds != null && lstPropertyIds.size() > 0) {
				var _tmpLstProp = lstPropertyIds.keySet().stream().filter(x -> x != 7987 && x != 6591)
						.collect(Collectors.toList());
				if (_tmpLstProp != null && _tmpLstProp.size() > 0) {
					for (Integer keyProPId : _tmpLstProp) {
						if (Strings.isNullOrEmpty(lstPropertyIds.get(keyProPId))
								|| arrIgnore.contains(lstPropertyIds.get(keyProPId).toLowerCase()))
							continue;
						String propDetailValue = lstPropertyIds.get(keyProPId);
						result += " " + propDetailValue;
					}
				}

			}
			break;
		case 1943: // Ngành hàng tủ lạnh
			String Inverters = "";
			String dungTich = "";
			if (lstPropertyIds != null && lstPropertyIds.size() > 0) {
				// Inverters của tủ lạnh khai báo CMS với ID thuộc tính : 8384
				if (lstPropertyIds.containsKey(7339) && values.contains(46227)) {
					Inverters = "Inverter";
				}
				// Dung tích của tủ lạnh khai báo CMS với ID thuộc tính : 8384
				if (lstPropertyIds.containsKey(8384)) {
					String name = lstPropertyIds.get(8384);
					dungTich = name;
				}
			}

			if (!Strings.isNullOrEmpty(Inverters))
				result += " " + Inverters;
			if (!Strings.isNullOrEmpty(dungTich) && !arrIgnore.contains(dungTich.toLowerCase()))
				result += " " + dungTich;
			if (lstPropertyIds != null && lstPropertyIds.size() > 0) {
				List<Integer> _tmpPropIds = lstPropertyIds.keySet().stream().filter(x -> x != 7339 && x != 8384)
						.collect(Collectors.toList());
				if (_tmpPropIds != null && _tmpPropIds.size() > 0)
					for (Integer item : _tmpPropIds) {
						String propDetailValue = lstPropertyIds.get(item);
						if (Strings.isNullOrEmpty(propDetailValue) || arrIgnore.contains(propDetailValue.toLowerCase()))
							continue;
						result += " " + propDetailValue;
					}
			}
			// product.shortNameProcessed = product.ProductManuBO.ManufacturerName + " " +
			// result + " " + product.ProductLanguageBO.ShortName;
			break;
		case 2002: // Ngành hàng máy lạnh
			String InventerMayLanh = "";
			String congSuatLamLanh = "";
			if (lstPropertyIds != null && lstPropertyIds.size() > 0) {
				// Inverters của masy lanh khai báo CMS với ID thuộc tính : 8384
				if (lstPropertyIds.containsKey(6820) && values != null && values.contains(43700)) {
					InventerMayLanh = "Inverter";
				}
				// Công suất làm lạnh của sản phẩm máy lạnh check trong CMS sản phẩm với thuộc
				// tính Id : 6819
				if (lstPropertyIds.containsKey(6819)) {
					congSuatLamLanh = lstPropertyIds.get(6819);
				}
			}

			if (!Strings.isNullOrEmpty(InventerMayLanh))
				result += " " + InventerMayLanh;
			if (!Strings.isNullOrEmpty(congSuatLamLanh)  && !arrIgnore.contains(congSuatLamLanh.toLowerCase()))
				result += " " + congSuatLamLanh;
			if (lstPropertyIds != null && lstPropertyIds.size() > 0) {
				List<Integer> _tmpPropIds = lstPropertyIds.keySet().stream().filter(x -> x != 6820 && x != 6819)
						.collect(Collectors.toList());
				if (_tmpPropIds != null && _tmpPropIds.size() > 0)
					for (Integer item : _tmpPropIds) {
						String propDetailValue = lstPropertyIds.get(item);
						if (Strings.isNullOrEmpty(propDetailValue) || arrIgnore.contains(propDetailValue.toLowerCase()))
							continue;
						var arr = propDetailValue.split("-");
						if (arr != null && arr.length >= 2) {
							if (lstProvince.contains(product.ProvinceID)) {
								result += " " + arr[1] != null && arr[1].length() > 0 ? arr[1] : "";
							} else {
								result += " " + arr[0] != null && arr[0].length() > 0 ? arr[0] : "";
							}
						} else
							result += " " + arr[0];
					}
			}
			// product.shortNameProcessed = product.ProductManuBO.ManufacturerName + " " +
			// result + " " + product.ProductLanguageBO.ShortName;
			break;
		case 166: // Ngành hàng tủ đông
			String InventerTuDong = "";
			String congSuatLamLanhTuDong = "";
			if (lstPropertyIds != null && lstPropertyIds.size() > 0) {
				if (lstPropertyIds.containsKey(15128) && values.contains(87241)) {
					InventerTuDong = "Inverter";
				}
				// Công suất làm lạnh của sản phẩm máy lạnh check trong CMS sản phẩm với thuộc
				// tính Id : 944
				if (lstPropertyIds.containsKey(944)) {
					congSuatLamLanhTuDong = lstPropertyIds.get(944);
				}

			}

			if (!Strings.isNullOrEmpty(InventerTuDong))
				result += " " + InventerTuDong;
			if (!Strings.isNullOrEmpty(congSuatLamLanhTuDong) && !arrIgnore.contains(congSuatLamLanhTuDong.toLowerCase()))
				result += " " + congSuatLamLanhTuDong;
			if (lstPropertyIds != null && lstPropertyIds.size() > 0) {
				List<Integer> _tmpPropIds = lstPropertyIds.keySet().stream().filter(x -> x != 15128 && x != 944)
						.collect(Collectors.toList());
				if (_tmpPropIds != null && _tmpPropIds.size() > 0)
					for (Integer item : _tmpPropIds) {
						String propDetailValue = lstPropertyIds.get(item);
						if (Strings.isNullOrEmpty(propDetailValue) || arrIgnore.contains(propDetailValue.toLowerCase()))
							continue;
						result += " " + propDetailValue;
					}
			}
			// product.shortNameProcessed = product.ProductManuBO.ManufacturerName + " " +
			// result + " " + product.ProductLanguageBO.ShortName;
			break;
		default:
			if (lstPropertyIds != null && lstPropertyIds.size() > 0) {
				for (Integer value : lstPropertyIds.keySet()) {
					String propDetailValue = lstPropertyIds.get(value);
					if (Strings.isNullOrEmpty(propDetailValue) || arrIgnore.contains(propDetailValue.toLowerCase()))
						continue;
					result += " " + propDetailValue;
				}
			}
			break;
		}
		return result;
	}

	public Map<String, Double> GetListCoupleWatch(int siteID) throws IOException {

		Map<String, Double> result = new HashMap<String, Double>();

		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		// q.must(termsQuery("ProductID", listproductID));
		q.must(termQuery("SiteID", siteID));
		q.must(termQuery("Lang", "vi_vn"));
		// q.must(termQuery("IsMultiAirConditioner", true));
		q.must(termQuery("IsCollection", 0));
		q.must(termQuery("HasBimage", 1));
		q.mustNot(termsQuery("CategoryID", new int[] { 8233, 8232 }));
		// q.must(termQuery("Prices", null));
		q.mustNot(existsQuery("Prices"));

		sb.from(0).size(1500).query(q);

		var searchRequest = new SearchRequest(CurrentIndexDB).source(sb);

		SearchResponse searchResponse = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {
			searchResponse = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		String idlist = "";
		List<ProductSO> productSO = Stream.of(searchResponse.getHits().getHits()).map(x -> {

			try {
				var test = esmapper.readValue(x.getSourceAsString(), ProductSO.class);

				return test;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}).collect(Collectors.toList());
		for (ProductSO productSO2 : productSO) {
			idlist += productSO2.ProductID + ",";
		}
		System.out.println(idlist);

		return result;
	}

	public List<ProductSO> getListProductPromotionExpired() throws Throwable {

		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		// q.must(termsQuery("ProductID", listproductID));
		q.must(termsQuery("SiteID", new int[] { 1, 2 }));
		q.must(termQuery("Lang", "vi_vn"));

		q.must(existsQuery("nextpromotionupdate"));
		q.must(rangeQuery("nextpromotionupdate").gt(0));
		q.must(rangeQuery("nextpromotionupdate").lte(new Date().getTime()));
		q.must(termQuery("IsCollection", 0));
		q.must(termQuery("HasBimage", 1));

		sb.from(0).size(1000).query(q);

		var searchRequest = new SearchRequest(CurrentIndexDB).source(sb);
		var searchResponse = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
		List<ProductSO> productSO = Stream.of(searchResponse.getHits().getHits()).map(x -> {

			try {
				var tmp = esmapper.readValue(x.getSourceAsString(), ProductSO.class);
				return tmp;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}).collect(Collectors.toList());
		return productSO;
	}

	public ProductPriceSO getPriceMinMax(int cateId, int siteId, String langId, int provinceId, String manu,
			Boolean round) throws Throwable {
		if (Strings.isNullOrEmpty(langId)) {
			langId = DidxHelper.getLangBySiteID(siteId);
		}
		String[] lstManu = null;
		var query = boolQuery();
		query.must(termQuery("CategoryID", cateId));
		query.must(termQuery("SiteID", siteId));
		query.must(termQuery("Lang", DidxHelper.GenTerm3(langId)));
		query.must(termsQuery("Prices.WebStatusId_" + provinceId, new int[] { 2, 4, 8, 11 }));
		query.must(rangeQuery("Prices.PriceAfterPromotion_" + provinceId + "_" + siteId).gt(0));
		if (manu != null && manu.length() > 0) {
			lstManu = Arrays.stream(manu.split(",")).map(x -> DidxHelper.GenTermKeyWord(x)).toArray(String[]::new);
			query.must(termsQuery("ManufacturerName", lstManu));

		}
		var sb = new SearchSourceBuilder();
		sb.fetchSource("PriceAfterPromotion_" + provinceId + "_" + siteId, null);
		// sb.from(0).size(1).query(query).aggregation(stats("priceMinAndMax").field("Prices.PriceAfterPromotion_"
		// + provinceId + "_" + siteId));
		sb.from(0).size(1).query(query)
				.aggregation(min("priceMin").field("Prices.PriceAfterPromotion_" + provinceId + "_" + siteId))
				.aggregation(max("priceMax").field("Prices.PriceAfterPromotion_" + provinceId + "_" + siteId));

		SearchResponse queryResults = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {
			queryResults = clientIndex.search(new SearchRequest(CurrentIndexDB).source(sb), RequestOptions.DEFAULT);

//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		// var aggrs = queryResults.getAggregations();
		Min start = queryResults.getAggregations().get("priceMin");
		Max end = queryResults.getAggregations().get("priceMax");
		ProductPriceSO priceSO = new ProductPriceSO();
		String endValue = end.getValueAsString();
		if (null != endValue && !endValue.toLowerCase().contains("infinity")) {
			priceSO.priceMax = Double.parseDouble(endValue);
		}

		String startValue = start.getValueAsString();
		if (null != startValue && !startValue.toLowerCase().contains("infinity")) {
			priceSO.priceMin = Double.parseDouble(startValue);
		}
		// Stats stats = aggrs.get("priceMinAndMax");

//		if((int) stats.getCount() >0) {
//			priceSO.priceMax = Double.valueOf(stats.getMax());
//			priceSO.priceMin = Double.valueOf(stats.getMin());
//		}

		if (round != null) {
			if (round == true) { // nếu muốn làm tròn giá min max
				double rod = 1000000d;
				priceSO.priceMin -= priceSO.priceMin % rod;
				priceSO.priceMax += rod - (priceSO.priceMax % rod);
			} else {
				priceSO.priceMin = Double.parseDouble(startValue);
				priceSO.priceMax = Double.parseDouble(endValue);
			}
		}

		return priceSO;
	}

	public int[] getPricesNull(int siteId) throws Throwable {
		var query = boolQuery();
		query.must(termQuery("SiteID", siteId));
		query.must(termQuery("HasBimage", 1));
		query.must(termQuery("IsCollection", 0));
		query.must(termQuery("IsDeleted", 0));
		query.mustNot(existsQuery("Prices"));

		var sb = new SearchSourceBuilder();
		sb.fetchSource("ProductID", null);
		// sb.from(0).size(1).query(query).aggregation(stats("priceMinAndMax").field("Prices.PriceAfterPromotion_"
		// + provinceId + "_" + siteId));
		sb.from(0).size(1000).query(query);

		var queryResults = clientIndex.search(new SearchRequest(CurrentIndexDB).source(sb), RequestOptions.DEFAULT);

		var idList = Stream.of(queryResults.getHits().getHits()).map(source -> {
			try {
				return mapper.readValue(source.getSourceAsString(), ProductSO.class);
			} catch (IOException e) {
				return null;
			}
		}).filter(obj -> obj != null).mapToInt(productid -> productid.ProductID).toArray();
		return idList;
	}

	public ProductSOSR getProductsByProductIDList2021New(ProductQuery qry) throws IOException {
		if (qry.ProvinceId == 0) {
			qry.ProvinceId = 3;
		}
		int newPageIndex = qry.PageIndex;
		ProductSOSR rs = null;
		int nextPageIndex = qry.PageIndex;
		int totalProduct = qry.productIDList.stream().distinct().collect(Collectors.toList()).size();
		if (qry.productIDList == null || qry.productIDList.size() == 0)
			return null;

		if (qry.ManufacturerId == 0 && qry.PageIndex == 0)// trang chu , 10 con dau tien lay theo thu tu nhu trong cms
		{
			qry.productIDList = qry.productIDList.stream().limit(qry.PageSize).collect(Collectors.toList());
		}
		if (qry.ManufacturerId == 0 && qry.PageIndex > 0)// page tiep theo skip may thang dau, ha pageindex
		{
			qry.productIDList = qry.productIDList.stream().skip(qry.PageSize).collect(Collectors.toList());
			newPageIndex = qry.PageIndex - 1;
		}
		if (qry.ManufacturerId > 0) {
			qry.productIDList = qry.productIDList.stream().skip(qry.PageSize).collect(Collectors.toList()); // trang
																											// hang thi
																											// skip 10
																											// con dau
																											// (do anh
																											// huong thu
																											// tu
																											// khaibao
																											// trong
																											// cms)
		}

		if (qry.PageSize < 0 || qry.PageSize > 50)
			qry.PageSize = 50;
		if (qry.PageIndex < 0)
			qry.PageIndex = 0;
		var query = boolQuery();
		query.must(termsQuery("ProductID", qry.productIDList));
		query.must(termsQuery("Prices.WebStatusId_3", new int[] { 2, 3, 4 }));

		query.must(termQuery("ProductType", 1));
		query.must(termQuery("IsCollection", 0));
		query.must(termQuery("IsRepresentProduct", 0));
		query.must(termQuery("IsReferAccessory", 0));
		if (qry.ManufacturerId > 0) {
			query.must(termQuery("ManufactureID", qry.ManufacturerId));
		}

		var sb = new SearchSourceBuilder();
		sb.from(newPageIndex * qry.PageIndex).size(qry.PageSize).query(query);
		sb.aggregation(terms("FacetTermCategoryID").field("CategoryID").size(100));
		sb.aggregation(terms("FacetTermManufactureID").field("CategoryID").size(100)
				.subAggregation(count("Sum").field("ProductID")));
		for (SearchOrder order : qry.Orders) {
			order.sort(sb, qry);
		}
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.source(sb);

		SearchResponse queryResults = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		var somap = new LinkedHashMap<Integer, ProductSO>();
		queryResults.getHits().forEach(h -> {
			try {

				var so = esmapper.readValue(h.getSourceAsString(), ProductSO.class);
				somap.put(so.ProductID, so);
			} catch (Exception e) {
				Logs.LogException(e);
			}
		});

		int total = (int) queryResults.getHits().getTotalHits().value;

		var _aggrs = queryResults.getAggregations();
		Map<String, Aggregation> aggrs = null;
		if (_aggrs != null) {
			aggrs = _aggrs.asMap();
		}

		List<FaceManuSR> manulist = new ArrayList<FaceManuSR>();

		ParsedLongTerms manubucket = (ParsedLongTerms) aggrs.get("FacetTermManufactureID");

		manubucket.getBuckets().forEach(b -> manulist.add(new FaceManuSR() {
			{
				manufacturerID = b.getKeyAsNumber().intValue();
				productCount = (int) b.getDocCount();
			}
		}));

		rs = new ProductSOSR() {
			{
				faceListManu = manulist.stream().toArray(FaceManuSR[]::new);
				productList = somap;
				rowCount = total;
			}

		};

		return rs;
	}

	public Map<Integer, List<Integer>> getCachedTopProductSelling(int siteID, int provinceID, String lang)
			throws Throwable {
		String keyT = "product_topselling";
		String keyO = siteID + "_" + lang + "_" + provinceID;
		Map<Integer, List<Integer>> result = new HashMap<>();
		if ((result = CachedObject.getObject(keyT, keyO, 180, Map.class)) == null) {
			result = getProductTopSelling(siteID, lang, provinceID);
			CachedObject.putObject(keyT, keyO, result);
		}
		return result;
	}

	public Map<Integer, List<Integer>> getProductTopSelling(int siteID, String lang, int ProvinceID) throws Throwable {
		var listcate = categoryHelper.GetAllCategories(siteID, lang);
		Map map = new HashMap<Integer, List<Integer>>();
		if (listcate != null && listcate.length > 0) {
			var idsCate = Arrays.stream(listcate).filter(Objects::nonNull).map(x -> x.CategoryID)
					.collect(Collectors.toList());
			map = getTop3SoldCount(idsCate, siteID, ProvinceID, lang);
		}
		return map;
	}

	public List<ProductSO> getPresentProductSE(int productID, int siteID, String langID) throws IOException {

		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("PresentProductID", productID));
		q.must(termQuery("SiteID", siteID));
		q.must(termQuery("IsDeleted", 0));
		// int[] status = new int[] { 2, 3, 4, 8, 9, 11 };

		// q.must(termQuery("HasBimage", 1));
		// sản phẩm gộp không check image
		q.must(termQuery("Lang", DidxHelper.GenTerm3(langID)));
		sb.from(0).size(100).query(q);
		var searchRequest = new SearchRequest(CurrentIndexDB).source(sb);

		SearchResponse searchResponse = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			searchResponse = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		List<ProductSO> productSO = Stream.of(searchResponse.getHits().getHits()).map(x -> {

			try {
				var t = esmapper.readValue(x.getSourceAsString(), ProductSO.class);
				return t;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}).collect(Collectors.toList());

		if (productSO != null) {
			return productSO;
		}
		return null;
	}

	public Map<String, Object> combinedProduct(ProductBO productBO, int siteID, String langID) throws Throwable {
		// xử lý gộp sp
//		if(DidxHelper.isLive()){
//			return null;
//		}
		Map<String, Object> pricesMap = new HashMap<String, Object>();
		if (productBO.CategoryID == 42) {
			// điện thoại iphone thì gộp sản phẩm
			var lstPresent = getPresentProductSE(productBO.ProductID, siteID, langID);
			List<Integer> lstProvinceID = getAllProvinceByCountry(DidxHelper.getCountryBySiteID(siteID));
			// int[] status = new int[] { 2, 3, 4, 8, 9, 11 };
			String[] status = new String[] { "2", "3", "4", "8", "9", "11" };
			Double[] statusd = new Double[] { 2.0, 3.0, 4.0, 8.0, 9.0, 11.0 };
			for (var provinceID : lstProvinceID) {
//				var PriceAfterPromotion = x.Prices.get("PriceAfterPromotion_" + provinceid + "_" + siteID);
//				if(PriceAfterPromotion == null || (PriceAfterPromotion instanceof Integer  && (Integer)PriceAfterPromotion == 0)) {
//					PriceAfterPromotion = 0.0;
//				}
//				var xx = lstPresent.get(0);
//				var test = xx.Prices.get("WebStatusId_" + provinceID) instanceof Integer;
//				String in = xx.Prices.get("WebStatusId_" + provinceID + "") + "";
//				var test2 = Arrays.asList(status).contains(xx.Prices.get("WebStatusId_" + provinceID + "") + "");

				var p = lstPresent.stream().filter(x -> x.Prices != null
						&& x.Prices.get("WebStatusId_" + provinceID) != null
						&& ((x.Prices.get("WebStatusId_" + provinceID) instanceof Integer
								&& (Integer) x.Prices.get("WebStatusId_" + provinceID) > 0
								&& Arrays.asList(status).contains(x.Prices.get("WebStatusId_" + provinceID + "") + ""))
								|| (!(x.Prices.get("WebStatusId_" + provinceID) instanceof Integer)
										&& (Double) x.Prices.get("WebStatusId_" + provinceID) > 0
										&& Arrays.asList(statusd)
												.contains(Utils.toDouble(x.Prices.get("WebStatusId_" + provinceID).toString())))))
						.collect(Collectors.toList());
				if (lstPresent.size() > 0) {
					var pp = lstPresent.stream()
							.filter(x -> x.Prices != null && x.Prices.get("Price_" + provinceID) != null
									&& x.Prices.get("PriceAfterPromotion_" + provinceID + "_" + siteID) !=null
									&& ((x.Prices.get("Price_" + provinceID) instanceof Integer
											&& (Integer) x.Prices.get("Price_" + provinceID) > 0)
											|| ((Double) x.Prices.get("Price_" + provinceID) > 0)))
							.mapToDouble(x -> Utils.toDouble(x.Prices.get("PriceAfterPromotion_" + provinceID + "_" + siteID).toString()))
							.toArray();

//					var pp = plist.toArray(Double[]::new);

					pricesMap.put("PresentStatus_" + provinceID, p.size());
					if (p.size() > 0) {
						var first = p.stream().sorted(Comparator.comparingInt(x -> x.Capacity)).findFirst()
								.orElse(null);
						if (first != null && first.Prices != null) {
							String key = "PriceAfterPromotion_" + provinceID + "_" + siteID;
							String keyPrice = "Price_" + provinceID;
							if (first.Prices.get(key) != null) {
								pricesMap.put(key, first.Prices.get(key));
							}
							if (first.Prices.get(keyPrice) != null) {
								pricesMap.put(keyPrice, first.Prices.get(keyPrice));
							}

						}
					}

					if (pp != null && pp.length > 0) {
						pricesMap.put("PresentPriceMin" + provinceID, Arrays.stream(pp).min().getAsDouble());
						pricesMap.put("PresentPriceMax" + provinceID, Arrays.stream(pp).max().getAsDouble());
					} else {
						pricesMap.put("PresentPriceMin" + provinceID, 0);
						pricesMap.put("PresentPriceMax" + provinceID, 0);
					}

				} else {
					pricesMap.put("PresentStatus_" + provinceID, 0);
					pricesMap.put("PresentPriceMin" + provinceID, 0);
					pricesMap.put("PresentPriceMax" + provinceID, 0);

				}
			}

		}
		return pricesMap;
	}

	public void processcombinedProduct(ProductBO product, List<ProductBO> combinedProduct,
			Map<Integer, ProductDetailBO[]> detailmapsPresent) throws Throwable {

		if (product.CategoryID == 42 && product.ProductManuBO != null) {

			if (combinedProduct != null && combinedProduct.size() > 0 && detailmapsPresent != null) {

				for (var item : combinedProduct) {
					// DungLuongCate42
					if (item != null) {
						var propDetail = detailmapsPresent.get(item.ProductID);
						if (propDetail != null) {
							var propDungLuong = Arrays.stream(propDetail).filter(x -> x.PropertyID == 49).findFirst()
									.orElse(null);
							if (propDungLuong != null) {
								var DungLuongCate42 = propDungLuong.PropValue;
								if (!Utils.StringIsEmpty(DungLuongCate42)) {
									DungLuongCate42 = DungLuongCate42.replaceAll("\\D", "");
									var localsto = Utils.toInt(DungLuongCate42);
									item.CapacityStorage = localsto;
								}
							}
							//
						}
					} else {
						item.CapacityStorage = 0;
					}
				}

				String[] status = new String[] { "2", "3", "4", "8", "9", "11" };
				var tmp = combinedProduct.stream()
						.filter(x -> x.RepresentProductID == product.ProductID && x.ProductErpPriceBO != null
								&& Arrays.asList(status).contains(x.ProductErpPriceBO.WebStatusId + ""))

						.sorted(Comparator.<ProductBO>comparingInt(x -> x.ProductSlaveInfo != null &&
								x.ProductSlaveInfo.ActivedDefaultFormDate != null
								&& x.ProductSlaveInfo.ActivedDefaultFormDate.before(new Date())
								&& x.ProductSlaveInfo.ActivedDefaultToDate != null
								&& x.ProductSlaveInfo.ActivedDefaultToDate.after(new Date()) ? -x.ProductSlaveInfo.IsActivedDefault : 0)
								.thenComparingInt(x -> x.CapacityStorage))
						//.sorted(Comparator.comparingInt(x -> x.CapacityStorage))
						.toArray(ProductBO[]::new);
				product.CombinedProducts = tmp;
			}

		}
		// }
	}

	private static final Comparator<ProductBO> sortByFeaturePropertyValue = (p1, p2) -> {
		if (Objects.isNull(p1.FeaturePropertyValue)) {
			return -1;
		} else if (Objects.isNull(p2.FeaturePropertyValue)) {
			return 1;
		}
		return Utils.toInt(p1.FeaturePropertyValue.replace("GB", "").trim()) > Utils
				.toInt(p2.FeaturePropertyValue.replace("GB", "").trim()) ? 1 : -1; // sort ASC
	};

	public ProductBO[] processCombinedModel(ProductBO[] product) {
		/*
		 * Hàm này đảo vị trí của sp, lấy sp đầu tiền trong ds combined lên để actived
		 * sp cha thì đẩy vô RepresentProductBO
		 */
		if (product == null || product.length == 0) {
			return null;
		}
		var tmpProduct = new ProductBO[] {};
		for (var p : product) {
			if (p.CombinedProducts != null && p.CombinedProducts.length > 0) {
				var c = p.CombinedProducts;
				var p2 = p;
				p2.CombinedProducts = null;
				p = c[0];
				p.CombinedProducts = Arrays.stream(c).skip(1).toArray(ProductBO[]::new);
				p.RepresentProductBO = p2;
			}
			tmpProduct = Utils.pushArray(tmpProduct, p);
		}
		return tmpProduct;
	}

	public int[] getCachedRelateCategories(int categoryID, int siteID, String langID) throws IOException {
		String keyT = "getCachedRelateCategories";
		String keyO = categoryID + "_" + siteID + "_" + langID;
		int[] result;
		if ((result = CachedObject.getObject(keyT, keyO, 60, int[].class)) == null) {
			result = getRelateCategories(categoryID, siteID, langID);
			CachedObject.putObject(keyT, keyO, result);
		}
		return result;
	}

	public int[] getRelateCategories(int categoryID, int siteID, String langID) throws IOException {
		var q = boolQuery().must(termQuery("SiteID", siteID)).must(termQuery("Lang", DidxHelper.GenTerm3(langID)))
				.must(termQuery("CategoryID", categoryID)).must(termQuery("IsDeleted", 0))
				.must(termQuery("HasBimage", 1));
		var sb = new SearchSourceBuilder().query(q).from(0).size(0)
				.aggregation(terms("relateCate").field("intCategoryMap").size(100));
		var sr = new SearchRequest(config.ELASTICSEARCH_PRODUCT_INDEX).source(sb);

		SearchResponse r = null;
//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			r = clientIndex.search(sr, RequestOptions.DEFAULT);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		var agg = (ParsedLongTerms) r.getAggregations().get("relateCate");
		return agg.getBuckets().stream().mapToInt(x -> x.getKeyAsNumber().intValue()).toArray();
	}

	public String getProductIdByHasBimageAndSite(int siteId) throws IOException {

		var q = boolQuery();
		q.must(termQuery("HasBimage", 0));
		q.must(termQuery("SiteID", siteId));
		q.must(termQuery("IsDeleted", 0));
		q.must(rangeQuery("ProductID").gt(150000));

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.fetchSource(new String[] { "ProductID" }, null);
		searchSourceBuilder.query(q).size(5000);

		SearchHit[] hit = null;
		SearchResponse r = null;
	//	var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);

		//try {

			hit = elasticClient.searchObject(config.ELASTICSEARCH_PRODUCT_INDEX, searchSourceBuilder);
		//} catch (Throwable e) {

		//	Logs.LogException(e);
		//	throw e;
		//} finally {
		//	elasticClient1.close();
		//}

		List<String> results = new ArrayList<>();
		if (hit != null && hit.length > 0) {
			results = Arrays.stream(hit).map(h -> {
				try {
					return esmapper.readValue(h.getSourceAsString(), ProductSO.class);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}).map(x -> x.ProductID + "").collect(Collectors.toList());
		}
		return String.join(",", results);

	}

	public ProductColorBO[] GetProductColorByProductIDLang2(ProductGeneral all) {
		{

			var colorCode = all.GetColorByID != null ? all.GetColorByID : null;

			if (colorCode != null && colorCode.length > 0) {
				var CodeGallery = all.GalleryByProductID != null ? all.GalleryByProductID : null;
				if (CodeGallery != null && CodeGallery.length > 0) {

					for (ProductColorBO x : colorCode) {
						var tmp = Stream.of(CodeGallery).filter(c -> c.ProductCode.equals(x.ProductCode)).findFirst()
								.orElse(null);
						if (tmp != null) {
							x.Image = tmp.Image;
							x.Bimage = tmp.Bimage;
							x.Mimage = tmp.Mimage;
							x.Simage = tmp.Simage;
							x.DisplayOrder = tmp.DisplayOrder;
						}
					}

				}

			}
			return colorCode;

		}
	}

	public ProductBOSR GetProductListingByIDFromCache(ProductListing query) throws Throwable {

		var stridlist = Arrays.toString(query.listProductID);
		String key = "GetProductListingByIDFromCache" + stridlist + "_" + query.ManufactureId
				+ "_" + query.ProvinceID+ "_" + query.OrderType+ "_" + query.PageIndex
				+ "_" + query.PageSize+ "_" + query.SiteID + "_" + query.Lang;
		var rs = (ProductBOSR) CacheStaticHelper.GetFromCache(key, 5);
		if (rs == null) {
			rs = GetProductListingByID(query);
			CacheStaticHelper.AddToCache(key, rs);
		}
		return rs;
	}
	public ProductBOSR GetProductListingByID(ProductListing query) throws Throwable {
	// hàm cũ GetProductsByProductIDList2021New
		int newPageIndex = query.PageIndex;
		int totalProduct = (int) Arrays.stream(query.listProductID).distinct().count();
		var ProductIDListTotal =  Arrays.stream(query.listProductID).distinct().toArray();
		if (query.PageSize < 0 || query.PageSize > 50) query.PageSize = 50;
		if (query.PageIndex < 0) query.PageIndex = 0;
		if (query.OrderType == 0 && query.ManufactureId == 0)
		{
			query.listProductID = Arrays.stream(query.listProductID).skip(query.PageSize * query.PageIndex).limit(query.PageSize).toArray();
		}
		var q = boolQuery();
		var q2 = boolQuery();

		q.must(termsQuery("Prices.WebStatusId_" + query.ProvinceID, new String[] { "2", "3", "4" }));
		q.must(termQuery("HasBimage", 1));
		q.must(termQuery("SiteID", query.SiteID));
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("ProductType", 1));
		// q.must(termQuery("IsCollection", 1));
		q2 = q;

		if (query.OrderType == 0 && query.ManufactureId == 0) {
			q2.must(termsQuery("ProductID", ProductIDListTotal));
		}
		q.must(termsQuery("ProductID", query.listProductID));
		if (query.ManufactureId>0) {
			q.must(termQuery("ManufactureID", query.ManufactureId));
		}
		SearchSourceBuilder sb = new SearchSourceBuilder();
		sb.fetchSource(new String[] { "ProductID" }, null);
		sb.from(query.PageIndex * query.PageSize).size(query.PageSize);

		sb.aggregation(terms("FacetTermCategoryID").field("CategoryID").size(100));
		sb.aggregation(terms("FacetTermManufactureID").field("ManufactureID").size(100)
		.subAggregation(count("Sum").field("ProductID")));

		if (query.OrderType == 1){
			sb.sort(scriptSort(new Script("return (doc['Prices.Price_3'].value/doc['Prices.PriceAfterPromotion_" + query.ProvinceID + "'].value)*100"), ScriptSortType.NUMBER).order(SortOrder.DESC));
		}
		if (query.OrderType == 2){
			sb.sort(scriptSort(new Script("return doc['Prices.Price_3'].value"), ScriptSortType.NUMBER).order(SortOrder.DESC));
		}else{
			//
		}


		// SearchHit[] hit = null;
		SearchResponse r = null;
		SearchRequest searchRequest = new SearchRequest(config.ELASTICSEARCH_PRODUCT_INDEX);
		searchRequest.source(sb.query(q));
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
		// hit = queryResults.getHits();

		int TotalProduct = (int) queryResults.getHits().getTotalHits().value;
		var _aggrs = queryResults.getAggregations();
		Map<String, Aggregation> aggrs = null;
		if (_aggrs != null) {
			aggrs = _aggrs.asMap();
		}
		if (query.OrderType == 0 && query.ManufactureId == 0 )
		{
			var req = clientIndex.search(new SearchRequest(config.ELASTICSEARCH_PRODUCT_INDEX).source(sb.query(q2)), RequestOptions.DEFAULT);
			TotalProduct = (int) req.getHits().getTotalHits().value;
		}

		ParsedLongTerms manubucket = (ParsedLongTerms) aggrs.get("FacetTermManufactureID");
		List<FaceManuSR> manulist = new ArrayList<FaceManuSR>();
		manubucket.getBuckets().forEach(b -> manulist.add(new FaceManuSR() {
			{
				manufacturerID = b.getKeyAsNumber().intValue();
				productCount = (int) b.getDocCount();
			}
		}));

		List<FaceCategorySR> catelist = new ArrayList<FaceCategorySR>();
		ParsedLongTerms catebucket = (ParsedLongTerms) aggrs.get("FacetTermCategoryID");
		catebucket.getBuckets().forEach(b -> {

			catelist.add(new FaceCategorySR() {
				{
					categoryID = b.getKeyAsNumber().intValue();
					productCount = (int) b.getDocCount();
					score = 0;// (int) catescore;
					hasProductInStock = true;// hasproductinstock == 1;
					productCountNoStock = -1;

				}
			});
		});

		//List<Integer> results = new ArrayList<>();
		var idlist = new int[]{};

		var listSO = Stream.of(queryResults.getHits().getHits()).map(x -> {
			try {
				return mapper.readValue(x.getSourceAsString(), ProductSO.class);
			} catch (IOException e1) {
				return null;
			}
		}).filter(x -> x != null).collect(Collectors.toList());
		//.sorted(Comparator.comparingInt(x -> listproduct.indexOf(x.ProductID))).mapToInt(x -> x.ProductID).toArray();
		if (query.OrderType == 0 && query.ManufactureId == 0 ){
			var listproduct = Arrays.asList(query.listProductID);
			idlist = listSO.stream().sorted(Comparator.comparingInt(x -> listproduct.indexOf(x.ProductID))).mapToInt(x -> x.ProductID).toArray();
		}else{
			idlist = listSO.stream().mapToInt(x -> x.ProductID).toArray();
		}
		var productlist = GetSimpleProductListByListID_PriceStrings_soMap(idlist, query.SiteID, query.ProvinceID,0,query.Lang, null);
		var productsr = new ProductBOSR();
		productsr.productList  = productlist;
		productsr.faceListCategory = catelist.toArray(FaceCategorySR[]::new);
		productsr.faceListManu = manulist.toArray(FaceManuSR[]::new);
		productsr.rowCount = TotalProduct;
		getCateManuName(productsr, query.SiteID, query.Lang);

		return productsr;
	}
	
	public StoreBO[] getStorebyWardID(int wardID) throws Throwable {
		return factoryRead.QueryFunction("product_SearchStoreByWardid", StoreBO[].class, false, wardID);
	}

	public List<ProductSO> getListProductUpdate() throws Throwable {
			var sb = new SearchSourceBuilder();
			var q = boolQuery();
			// q.must(termsQuery("ProductID", listproductID));
			q.must(termsQuery("SiteID", new int[] { 1, 2 }));
			q.must(termQuery("Lang", "vi_vn"));

			q.must(existsQuery("nextseupdate"));
			q.must(existsQuery("strnextseupdate"));
			q.must(rangeQuery("nextseupdate").gt(0));
			// dung ngay update thi lay khong thi thoi :|
			q.must(rangeQuery("strnextseupdate").gte("now/d").lte("now+1d/d"));
			q.must(termQuery("IsCollection", 0));
			q.must(termQuery("HasBimage", 1));

			sb.from(0).size(1000).query(q);

			var searchRequest = new SearchRequest(CurrentIndexDB).source(sb);
			var searchResponse = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
			List<ProductSO> productSO = Stream.of(searchResponse.getHits().getHits()).map(x -> {

				try {
					var tmp = esmapper.readValue(x.getSourceAsString(), ProductSO.class);
					return tmp;
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}).collect(Collectors.toList());
			return productSO;
	}

	public StoreBO[] hgGetListStore(String ProductCode,int SiteID) throws Throwable {
		// productcode: 3051097001685
		// siteid : 2
		 var sb = new SearchSourceBuilder();
		var b = boolQuery();
		b.must(termQuery("SiteID",SiteID));
		sb.from(0).size(1000).query(b);
		//.fetchSource(new String[]{"StoreID","ProvinceID"},null);
		var searchRequest = new SearchRequest("ms_store");
		searchRequest.source(sb);
		List<Integer> lstID = new ArrayList<>();
		StoreBO[] bo = null;
		try{
			SearchResponse result = null;
			result = clientIndex.search(searchRequest,RequestOptions.DEFAULT);
			var finalResult = result.getHits();
			finalResult.forEach(h->{
				StoreSO so = null;
				try {
					so = esmapper.readValue(h.getSourceAsString(), StoreSO.class);
					lstID.add(so.StoreID);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}catch (Exception e) {
			e.printStackTrace();
		}
		List<Integer> listFinalID = new ArrayList<>();
		int[] ids = lstID.stream().mapToInt(i->i).toArray();
		try {
			bo = factoryRead.QueryFunction("fresher_getStoreByProductCode", StoreBO[].class, false,ProductCode,ids);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		for(StoreBO i : bo)
		{
			listFinalID.add(i.StoreID);
		}
		StoreBO[] finalBO = null;
		int[] finalids = listFinalID.stream().mapToInt(i->i).toArray();
		finalBO = factoryRead.QueryFunction("product_GetStoreByListID", StoreBO[].class, false,finalids);
		return finalBO;
	}

	public StoreBO[] hgTest(String ProductCode) throws Throwable {
		// productcode: 3051097001685
		// siteid : 2
		 /* var sb = new SearchSourceBuilder();
		var b = boolQuery();
		b.must(termQuery("SiteID",SiteID));
		sb.from(0).size(1000).query(b);
		//.fetchSource(new String[]{"StoreID","ProvinceID"},null);
		var searchRequest = new SearchRequest("ms_store");
		searchRequest.source(sb);
		List<Integer> lstID = new ArrayList<>();
		StoreBO[] bo = null;
		try{
			SearchResponse result = null;
			result = clientIndex.search(searchRequest,RequestOptions.DEFAULT);
			var finalResult = result.getHits();
			finalResult.forEach(h->{
				StoreSO so = null;
				try {
					so = esmapper.readValue(h.getSourceAsString(), StoreSO.class);
					lstID.add(so.StoreID);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}catch (Exception e) {
			e.printStackTrace();
		}
		int[] ids = lstID.stream().mapToInt(i->i).toArray();
		  */
		StoreBO[] bo = null;
		try {
			bo = factoryRead.QueryFunction("fresher_test", StoreBO[].class, false,ProductCode);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return bo;
	}
}

//class PrenextProduct{
//	public String listproductidpre;
//	public int productID;
//	public int isdeleted;
//}

class FaceManuName {
	public int manufacturerID;
	public String manufacturerName;
	public String url;
	public String bigLogo;
	public String smallLogo;
	public int displayorder;

	@JsonIgnore
	public int getID() {
		return manufacturerID;
	}
}

class FaceCateName {
	public int categoryID;
	public String categoryName;

	@JsonIgnore
	public int getID() {
		return categoryID;
	}
}

class FaceManuCateList {
	public List<FaceManuName> manu;
	public List<FaceCateName> cate;
}

class StoreIDList {
	public int[] storeidlist;
}

class RootObjectPropvalue {
	public List<Value> value;
}

class Value {
	public List<Prop> prop;
	public List<Propvalue> propvalue;
}

class Prop {
	public int propertytype;
	public int propertyid;

	public List<Proplang> proplang;

}

class Propvalue {
	public int propertyid;

	public List<Propvaluelang> propvaluelang;

}

class Proplang {
	public String propertyname;

	public long isshowspecs;

	public long displayorder;

	public long propertyweight;

	public int propissearchbhx;

	public int propissearchtgdd;

	public int propissearchdmx;

	public int propissearchvuivui;

	public int propissearch;

	public int issuggestaccessoryprop;

	public int isaddup;

	public int isforceimport;
}

class Propvaluelang {
	public String Value;

	public long Valueid;

	public int Issearch;

	public long Comparevalue;

	public String Icon;

	public long Isexistpro;

	public long Displayorder;

	public long Iconscale;

	public String Metatitle;

	public int Isimportant;

	public String Shortname;

	public String Smoothurl;

	public String Metakeyword;

	public int Isinitsearch;
}

class Productdetail {

	public int isfeatureprop;
	public long productid;
	public String languageid;
	public long propertyid;
	public String value;
	public int propertytype;
}

class ProductCate {
	public ProductCateChild product;
}

class xxxx {
	public int productid;
}

class ProductCateChild {
	public long productid;
	public long categoryid;
}

class WarppedNumbers {
	int[] result;
}
