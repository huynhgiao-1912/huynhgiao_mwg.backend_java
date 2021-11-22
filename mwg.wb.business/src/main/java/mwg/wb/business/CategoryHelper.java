package mwg.wb.business;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.elasticsearch.dataquery.StoreQuery;
import mwg.wb.client.elasticsearch.dataquery.StoreSO;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.cache.CacheObject;
import mwg.wb.model.commonpackage.KeyWordRedirectBO;
import mwg.wb.model.commonpackage.KeyWordRedirectSO;
import mwg.wb.model.commonpackage.TimerSeoBO;
import mwg.wb.model.commonpackage.TimerSeoBOR;
import mwg.wb.model.general.DistrictBO;
import mwg.wb.model.general.ProvinceBO;
import mwg.wb.model.general.ProvinceInfoBO;
import mwg.wb.model.general.WardBO;
import mwg.wb.model.html.InfoBO;
import mwg.wb.model.other.CountWrapper;
import mwg.wb.model.pm.StoreBO;
import mwg.wb.model.products.GroupCategoryBO;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductCategoryBO;
import mwg.wb.model.products.ProductCategoryLangBO;
import mwg.wb.model.products.ProductCategoryQuickLinkBO;
import mwg.wb.model.products.ProductManuBO;
import mwg.wb.model.products.ProductPriceRangeBO;
import mwg.wb.model.products.ProductPropBO;
import mwg.wb.model.products.ProductPropValueBO;
import mwg.wb.model.products.ProductWarrantyCenterBO;
import mwg.wb.model.products.ProductWarrantyManuBO;
import mwg.wb.model.products.RelationShipGroupCategoryBO;
import mwg.wb.model.products.URLGoogleBO;
import mwg.wb.model.searchresult.StoreSR;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortMode;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.search.aggregations.AggregationBuilders.count;
import static org.elasticsearch.search.sort.SortBuilders.geoDistanceSort;

public class CategoryHelper {
	private static ORThreadLocal oclient = null;
	protected ObjectMapper mapper = null;
	public static Map<String, CacheObject> _productManuDictionary = new HashMap<String, CacheObject>();
//	private ClientConfig config = null;
	private String currentKeywordIndexDB = "ms_keywordredirect";
	private String currentUrlGooogleIndexDB = "ms_googleurl";
	private String currentStoreIndexDB = "ms_store";

	protected RestHighLevelClient clientIndex = null;
	private ElasticClient elasticClient = null;

	public boolean IsWorker = false;

	ClientConfig config = null;

	public CategoryHelper(ORThreadLocal afactoryRead, ClientConfig aconfig) {
		oclient = afactoryRead;
		config = aconfig;
		IsWorker = afactoryRead.IsWorker;
//		config = aconfig;
		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatStringNews);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	//		elasticClient =  new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST); 
	elasticClient = ElasticClient.getInstance(aconfig.SERVER_ELASTICSEARCH_READ_HOST);
		clientIndex = elasticClient.getClient();
	}

	static String KEY_CACHED = "CategoryHelper";
	static int KEY_CACHED_TIME_MIN = 20;

	public ProductManuBO[] GetListManuCategory(int[] listManu, int siteID, String langID) {

		// var manu = oclient.QueryFunction("manu_GetByIDList", ProductManuBO[].class,
		// false, listManu,
		// siteID, langID);
//		for (int manuID : listManu) {
//
//		}

		return null;
	}

//	public ProductCategoryBO GetCategoryByID(int ID, int siteID, String langID) {
//
//		return null;
//	}

	public ProductManuBO getManuSeo(int id, int siteID, String langID) throws Throwable {
		var manu = oclient.queryFunctionCached("product_getManuSeo", ProductManuBO[].class, id, siteID, langID);
		return manu == null || manu.length == 0 ? null : manu[0];
	}

	public ProductManuBO[] getManuSeoByCate(int cateforyID, int siteID, String langID) throws Throwable {
		return oclient.queryFunctionCached("product_getManuSeoByCate", ProductManuBO[].class, cateforyID, siteID,
				langID);
	}

	public ProductPropValueBO getPropValueSeo(int id, int siteID, String langID) throws Throwable {
		var value = oclient.queryFunction("product_GetPropValueSeo", ProductPropValueBO[].class, id, siteID, langID);
		return value == null || value.length == 0 ? null : value[0];
	}

	public ProductPropValueBO[] getPropValueSeoByCate(int categoryID, int siteID, String langID) throws Throwable {
		return oclient.queryFunctionCached("product_getPropValueSeoByCate", ProductPropValueBO[].class, categoryID,
				siteID, langID);
	}

	public ProductPropBO[] GetListYesNoPropertyByCategory(int categoryID, int siteID, String langID) throws Throwable {

		return oclient.QueryFunctionCached("product_getPropYesNoByCate", ProductPropBO[].class, false, categoryID,
				siteID, langID);

	}

	public ProductPropBO[] GetProductPropByCategory(int categoryID, int siteID, String langID, boolean getAddition)
			throws Throwable {
		String func = getAddition ? "product_getPropValueByCateWithAddition" : "product_getPropValueByCate";
		var result = oclient.queryFunctionCached(func, ProductPropBO[].class, categoryID, siteID, langID);
		return result;
	}

	public ProductPriceRangeBO[] GetListProductPriceRangeByCategory(int categoryID, int siteID, String langID)
			throws Throwable {
		var result = oclient.QueryFunctionCached("product_getPriceRangeByCate", ProductPriceRangeBO[].class, true,
				categoryID, siteID, langID);
		return result;
	}

	public ProductCategoryBO[] GetAllCategories(int siteID, String langID) throws Throwable {
		var result = oclient.queryFunctionCached("product_GetCategoryBySiteID", ProductCategoryBO[].class, siteID,
				langID);
		Arrays.sort(result, Comparator.comparingInt(ProductCategoryBO::getDisplayOrder));
		return result;
	}

	public ProductCategoryBO getCategoryByIDFromCached(int categoryID, int siteID, String languageID) throws Throwable {
		String key = KEY_CACHED + "product_GetCategory" + categoryID + "_" + siteID + "" + languageID;
		var rs = (ProductCategoryBO) CacheStaticHelper.GetFromCache(key, 30);
		if (rs == null) {
			rs = getCategoryByID(categoryID, siteID, languageID);
			CacheStaticHelper.AddToCache(key, rs);
		}
		return rs;
	}

	private ProductCategoryBO getCategoryByID(int categoryID, int siteID, String languageID) throws Throwable {
		var result = oclient.queryFunctionCached("product_GetCategory", ProductCategoryBO[].class, categoryID, siteID,
				languageID);
		return result == null || result.length == 0 ? null : result[0];
	}

	public ProductCategoryBO[] getCategoryByIDList(int[] categoryID, int siteID, String languageID) throws Throwable {
		return oclient.queryFunction("product_GetCategoryList", ProductCategoryBO[].class, categoryID, siteID,
				languageID);
	}

	private StoreBO getStoreByID(int storeid) throws Throwable {
		var stores = oclient.QueryFunctionCached("product_GetStoreByID", StoreBO[].class, false, storeid);
		return stores == null || stores.length == 0 ? null : stores[0];
	}

	public StoreBO getStoreByIDFromCached(int storeid) throws Throwable {
		String key = KEY_CACHED + "product_GetStoreByID" + storeid;
		var rs = (StoreBO) CacheStaticHelper.GetFromCache(key, 30);
		if (rs == null) {
			rs = getStoreByID(storeid);
			CacheStaticHelper.AddToCache(key, rs);
		}
		return rs;
	}

	public ProvinceBO[] getAllProvince() throws Throwable {
		// return oclient.QueryFunction("get_all_Gen_Province", ProvinceBO[].class,
		// false);
		return oclient.QueryFunctionCached("get_all_Gen_Province", ProvinceBO[].class, false);

	}

	public ProvinceBO[] getAllProvinceByCountry(int countryID) throws Throwable {
		// return oclient.QueryFunction("get_ProvinceByCountry", ProvinceBO[].class,
		// false, countryID);
		return oclient.QueryFunctionCached("get_ProvinceByCountry", ProvinceBO[].class, false, countryID);
	}

	public ProvinceInfoBO getProvinceInfo(int provinceID, int districtID, int wardID) throws Throwable {
//		var provs = oclient.QueryFunction("province_getInfo", ProvinceInfoBO[].class, false, provinceID, districtID,
//				wardID);

		var provs = oclient.QueryFunctionCached("province_getInfo", ProvinceInfoBO[].class, false, provinceID,
				districtID, wardID);

		return provs == null || provs.length == 0 ? null : provs[0];
	}

	public ProductPropBO[] getPropVByCategoryID(int categoryID, int siteID, String languageID) throws Throwable {
//		return oclient.QueryFunction("ProductPropV_getByCate", ProductPropBO[].class, false, categoryID, siteID,
//				languageID);
		return oclient.QueryFunctionCached("ProductPropV_getByCate", ProductPropBO[].class, false, categoryID, siteID,
				languageID);

	}

	public int getTotalStoreBySite(int siteID) throws Throwable {
		// var result = oclient.QueryFunction("get_totalStoreBySiteID",
		// CountWrapper[].class, false, siteID);
		var result = oclient.QueryFunctionCached("get_totalStoreBySiteID", CountWrapper[].class, false, siteID);

		return result == null || result.length == 0 ? 0 : result[0].sitetotal;
	}

	public StoreBO[] getStoreByProvince(int provinceID, int districtID, int brandID) throws Throwable {
//		return oclient.QueryFunction("product_SearchStoreByProvinceID", StoreBO[].class, false, provinceID, districtID,
//				brandID);

		return oclient.QueryFunctionCached("product_SearchStoreByProvinceID", StoreBO[].class, false, provinceID,
				districtID, brandID);

	}

	public DistrictBO[] getDistrictByProvince(int provinceID) throws Throwable {
		// return oclient.QueryFunction("district_GetByProvinceID", DistrictBO[].class,
		// false, provinceID);
		return oclient.QueryFunctionCached("district_GetByProvinceID", DistrictBO[].class, false, provinceID);

	}

	public WardBO[] getWardByDistrict(int districtID) throws Throwable {
		// return oclient.QueryFunction("ward_GetByDistrictID", WardBO[].class, false,
		// districtID);
		return oclient.QueryFunctionCached("ward_GetByDistrictID", WardBO[].class, false, districtID);

	}

	public KeyWordRedirectSO[] SearchKeywordRedirect(String Keyword, int SiteID) throws Throwable {

		if (Keyword == null || Utils.StringIsEmpty(Keyword))
			return null;
		Keyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(Keyword));
		if (Keyword == null || Utils.StringIsEmpty(Keyword))
			return null;
		Keyword = Keyword.replace(' ', '_');

		KeyWordRedirectBO[] KeyWordBO = null;
		// filter.
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", false));
		q.must(termQuery("KeywordSE", DidxHelper.GenTermKeyWord(Keyword + "")));
		q.must(termQuery("SiteID", SiteID));

		// q.must(rangeQuery("ActivedDate").lte(new Date()));

		sb.from(0).size(10).query(q);

		sb.sort("CreatedDate", SortOrder.DESC);

		var searchRequest = new SearchRequest(currentKeywordIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {
			// System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var data = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					// System.out.println("getSourceAsString" + x.getSourceAsString());
					return mapper.readValue(x.getSourceAsString(), KeyWordRedirectSO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).collect(Collectors.toList());// toArray();

			if (data != null && data.size() > 0) {
				var tmp = data.stream().filter(x -> x.type == 1).limit(1).collect(Collectors.toList());
				if (tmp != null && tmp.size() > 0) {
					return tmp.toArray(KeyWordRedirectSO[]::new);
				} else {
					// nếu không có keywordredirect thì set nghành hàng
					var tmpCate = data.stream().filter(x -> x.type == 2).collect(Collectors.toList());
					if (tmpCate != null && tmpCate.size() == 1) {
						var tmpreturn = tmpCate.stream().findFirst().orElse(null);// .collect(Collectors.toList());
						return new KeyWordRedirectSO[] { tmpreturn };
					} else {
						return null;
					}
				}
			} else {
				return null;
			}

			// System.out.println("idlist: " + idlist.length);
//			if(idlist.length > 0) {
//				KeyWordBO = oclient.QueryFunction("keywordredirect_getByIDList", KeyWordRedirectBO[].class, false, idlist);
//				return KeyWordBO;
//			}else {
//				return null;
//			}

		} catch (Exception e) {

			Logs.LogException(e);
		}

		return null;
	}

	public URLGoogleBO[] GetDataUrlRedirect() throws Throwable {
		// return oclient.QueryFunction("google_GetAll", URLGoogleBO[].class, false);
		return oclient.QueryFunctionCached("google_GetAll", URLGoogleBO[].class, false);

	}

	public URLGoogleBO[] GetDataUrlRedirectBySiteAndProject(String projectName, int siteID, int pageIndex, int pageSize)
			throws Throwable {

		if (pageSize < 0)
			pageSize = 10;
		if (pageSize > 50)
			pageSize = 50;
		if (pageSize * pageIndex > 10000)
			pageIndex = 0;

		if (!Utils.StringIsEmpty(projectName)) {
			projectName = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(projectName));
		}

		URLGoogleBO[] GoogleBO = null;
		// filter.
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", false));

		q.must(termQuery("SiteID", siteID));

		if (!Utils.StringIsEmpty(projectName)) {
			q.must(termQuery("ProjectName", projectName));
		}

		sb.from(pageIndex * pageSize).size(pageSize).query(q);

		sb.sort("CreatedDate", SortOrder.DESC);

		var searchRequest = new SearchRequest(currentUrlGooogleIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {
			// System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var data = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					// System.out.println("getSourceAsString" + x.getSourceAsString());
					return mapper.readValue(x.getSourceAsString(), URLGoogleBO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).toArray(URLGoogleBO[]::new);// collect(Collectors.toList());// toArray();

			return data;

		} catch (Exception e) {

			Logs.LogException(e);
		}

		return null;
	}

	public TimerSeoBOR SearchTimerSeo(int productID, int categoryID, int siteID, int pageIndex, int pageSize)
			throws Throwable {
		if (pageSize < 0)
			pageSize = 10;
		if (pageSize > 50)
			pageSize = 50;
		if (pageSize * pageIndex > 10000)
			pageIndex = 0;

		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", false));
		// q.must(termQuery("IsActived", true));

		q.must(rangeQuery("BeginDate").lte(new Date()));
		q.must(rangeQuery("EndDate").gte(new Date()));

		if (siteID > 0) {
			q.must(termQuery("ListSiteID", siteID));
		}

		if (productID > 0) {
			q.must(termQuery("ProductID", productID));
		}
		if (categoryID > 0) {
			q.must(termQuery("CategoryID", categoryID));
		}

		sb.from(pageIndex * pageSize).size(pageSize).query(q);

		// sb.sort("CreatedDate", SortOrder.DESC);

		var searchRequest = new SearchRequest("ms_timerseo");
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), TimerSeoBO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.TimerID).toArray();

			var timerBOR = new TimerSeoBOR();
			timerBOR.result = oclient.QueryFunction("product_timerseo_GetByIDList", TimerSeoBO[].class, false, idlist);
			timerBOR.total = (int) queryResults.getHits().getTotalHits().value;
			timerBOR.message = "ID: " + Arrays.toString(idlist);

			return timerBOR;
		} catch (Exception e) {

			Logs.LogException(e);
		}

		return null;
	}

	private ObjectMapper storeMapper = null;

	private ObjectMapper getStoreMapper() {
		if (storeMapper == null) {
			storeMapper = DidxHelper.generateNonDefaultJsonMapper(GConfig.DateFormatString);
		}
		return storeMapper;
	}

	public StoreSR searchStoreLatLonBySiteCached(StoreQuery qry) throws Throwable {
		var mapper = getStoreMapper();
		String keyT = "searchStoreLatLonBySiteCached";
		String keyO = mapper.writeValueAsString(qry);
		StoreSR result = CachedObject.getObject(keyT, keyO, 180, StoreSR.class);
		if (result == null) {
			result = searchStoreLatLonBySite(qry);
			CachedObject.putObject(keyT, keyO, result);
		}
		return result;
	}

	public StoreSR searchStoreLatLonBySite(StoreQuery qry) throws Throwable {
		var q = boolQuery();
		if (qry.PageSize > 200) {
			qry.PageSize = 200;
		}
		if (qry.ProvinceId > 0) {
			q.must(termQuery("ProvinceID", qry.ProvinceId));
		}
		if (qry.DistrictId > 0) {
			q.must(termQuery("DistrictID", qry.DistrictId));
		}
		q.must(termQuery("IsShowweb", true));
		if (qry.IsSalesStore > 0) {
			q.must(termQuery("IsSaleStore", true));
		}
		if (!Strings.isNullOrEmpty(qry.siteIDList)) {
			q.must(termsQuery("SiteID", qry.siteIDList.split(",")));

		}
		if (!Strings.isNullOrEmpty(qry.termUrl)) {
			q.must(termQuery("UrlTerm", DidxHelper.GenTerm3(qry.termUrl)));
		}
		if (qry.storeIDs != null && qry.storeIDs.length > 0) {
			q.must(termsQuery("StoreID", qry.storeIDs));
		}
		if (!Strings.isNullOrEmpty(qry.Keyword)) {
			qry.Keyword = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(qry.Keyword)).toLowerCase().trim();
			if (!Strings.isNullOrEmpty(qry.Keyword)) {
				String rsl = "";
				var token = Stream.of(qry.Keyword.split(" ")).filter(x -> !Strings.isNullOrEmpty(x))
						.toArray(String[]::new);
				if (qry.IsSearchLike) {
					if (token.length > 1)
						rsl = "(\"" + qry.Keyword + "\" OR (\"" + qry.Keyword.trim() + "*\") OR ("
								+ String.join(" AND ", token) + ") OR (" + String.join(" AND ", token) + "*)) ";
					else
						rsl = "(" + qry.Keyword + " OR (" + qry.Keyword.trim() + "*)) ";
				} else {
					if (token.length > 1)
						rsl = "(" + qry.Keyword + " OR (" + String.join(" AND ", token) + ")) ";
					else
						rsl = "(" + qry.Keyword + ") ";
				}
				q.must(queryStringQuery(rsl).field("Keyword"));
			}
		}
//		if (Strings.isNullOrEmpty(qry.Keyword)) {
//			qry.SearchType = 2;
//		}
		SearchSourceBuilder sb = new SearchSourceBuilder().fetchSource(new String[] { "StoreID" }, null);
		var srq = new SearchRequest(currentStoreIndexDB).source(sb);
		SearchResponse sr = null;
		switch (qry.SearchType) {
		case 1: // distance
				q.must(geoDistanceQuery("Location").distance(qry.Distance, DistanceUnit.KILOMETERS).point(qry.Lat,
						qry.Lon));
			sb.query(q).sort(geoDistanceSort("Location", qry.Lat, qry.Lon).order(SortOrder.ASC).sortMode(SortMode.MIN));
			break;
		case 2:
			sb.sort("_score", SortOrder.DESC);
			break;
		case 3:
			q.must(boolQuery().should(rangeQuery("OpeningDay").gt("now-10d/d").lte("now/d"))
					.should(rangeQuery("ReOpenDate").gt("now-10d/d").lte("now/d")));
			sb.sort("OpeningDay", SortOrder.DESC);
			break;
		case 4:// sap khai truong
			q.must(boolQuery().should(rangeQuery("ReOpenDate").gt("now/d").lt("now+7d/d"))
					.should(rangeQuery("OpeningDay").gt("now/d").lt("now+7d/d")));
			sb.sort("ReOpenDate", SortOrder.ASC);
			break;
		case 5:// lay st moi
			sb.sort("OpeningDay", SortOrder.DESC);
			break;
		case 6: // sap & moi khai truong
			q.must(rangeQuery("OpeningDay").gt("now-10d/d").lte("now+10d/d"));
			sb.sort("OpeningDay", SortOrder.DESC);
			break;
		default:
			if (qry.SearchType == -1) {
				q.must(termQuery("SiteID", 1));
			} else if (qry.SearchType == -2) {
				q.must(termQuery("SiteID", 2));
			}
			sb.sort("SiteID", SortOrder.DESC);
			break;

		}
		sb.from(qry.PageIndex * qry.PageSize).size(qry.PageSize).query(q)
				.aggregation(count("store_count").field("StoreID"));

//		var elasticClient1 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			sr = clientIndex.search(srq, RequestOptions.DEFAULT);

//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}

		// sr = clientIndex.search(srq, RequestOptions.DEFAULT);
		var idlist = Stream.of(sr.getHits().getHits()).map(source -> {
			try {
				return mapper.readValue(source.getSourceAsString(), StoreSO.class);
			} catch (IOException ex) {
				return null;
			}
		}).filter(obj -> obj != null).mapToInt(storeso -> storeso.StoreID).toArray();
		StoreSR result = new StoreSR();
		if (!Strings.isNullOrEmpty(qry.siteIDList) || qry.storeIDs.length >0) {
			ValueCount count = sr.getAggregations().get("store_count");
			result.total = (int) count.getValue();
		} else {
			result.total = (int) sr.getHits().getTotalHits().value;
		}
		result.stores = oclient.queryFunction("product_GetStoreByListID", StoreBO[].class, idlist);
//		if(qry.SearchType == 1) { // tính khoảng cách 2 lat lon
//			double lat = qry.Lat;
//			double lon = qry.Lon;
//			double distanceCustomer = qry.Distance;
//			result.stores = Stream.of(result.stores).filter(x -> x != null).map(x -> {
//				x.range = calculatorDistanceBetweenPlaces(lat, lon, x.LAT, x.LNG);
//				return x;
//			}).toArray(StoreBO[]::new);
//		}
 		return result;
	}
	public double calculatorDistanceBetweenPlaces(double lat1,double lon1,double lat2,double lon2){
		 double  R = 6371;

		double sLat1 = Math.sin(calculatorRadians(lat1));
		double sLat2 = Math.sin(calculatorRadians(lat2));
		double cLat1 = Math.cos(calculatorRadians(lat1));
		double cLat2 = Math.cos(calculatorRadians(lat2));
		double cLon = Math.cos(calculatorRadians(lon1) - calculatorRadians(lon2));
		double cosD = sLat1 * sLat2 + cLat1 * cLat2 * cLon;
		double d = Math.acos(cosD);
		double range = R * d;

//		var dLat = calculatorRadians(lat2-lat1);  // deg2rad below
//		var dLon = calculatorRadians(lon2-lon1);
//		var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
//				Math.cos(calculatorRadians(lat1)) * Math.cos(calculatorRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
//		var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//		var range = R * c; // Distance in km
		return range;

	}
	public double calculatorRadians(double rank){
		//Radian để đo góc phẳng
		return  rank*(Math.PI/180);
	}

	public ProvinceBO[] getProvinceByListID(int[] ids) throws Throwable {
		return oclient.queryFunction("province_GetByListID", ProvinceBO[].class, ids);
	}

	public ProductManuBO[] GetProductManuByListCategoryID(String listCategoryID, int siteID, String LangID)
			throws Throwable {
		return oclient.queryFunction("manu_GetByListCateID", ProductManuBO[].class, listCategoryID, siteID, LangID);
	}

	public ProductWarrantyManuBO[] productWarrantyManuSelect(int warrantymanuID, int categoryID, int siteID)
			throws Throwable {
		return oclient.queryFunction("warranty_manu_GetDetail", ProductWarrantyManuBO[].class, warrantymanuID,
				categoryID, siteID);
	}

	public ProductWarrantyCenterBO[] getProductWarrantyCenterByManufacturerAndProvince(int manuID, int provinceID,
			int categoryID) throws Throwable {
		return oclient.queryFunction("warranty_center_GetByManuID", ProductWarrantyCenterBO[].class, manuID, provinceID,
				categoryID);
	}

	public GroupCategoryBO[] getAllGroupCateFromCate(int siteID, String langID, Integer clearcache) throws Throwable {

		String keyT = "product_groupcate";
		String keyO = "GROUPCATE_ALL" + siteID + "_" + langID;
		GroupCategoryBO[] cached = null;
		if (clearcache == 1 || (cached = CachedObject.getObject(keyT, keyO, 30, GroupCategoryBO[].class)) == null) {
			cached = getAllGroupCate(siteID, langID);
			CachedObject.putObject(keyT, keyO, cached);
		}
		return cached;
	}

	public ProductCategoryBO[] getAllProductCate(int siteID, String langID) throws Throwable {
		// var result = oclient.queryFunction("product_categroup_getAll",
		// ProductCategoryBO[].class, siteID, langID);

		var result = oclient.queryFunctionCached("product_categroup_getAll", ProductCategoryBO[].class, siteID, langID);

		return result;

	}

	public RelationShipGroupCategoryBO[] getAllRelationShipGroupCate() throws Throwable {
		// var result = oclient.queryFunction("product_categroup_linkGetAll",
		// RelationShipGroupCategoryBO[].class);
		var result = oclient.queryFunctionCached("product_categroup_linkGetAll", RelationShipGroupCategoryBO[].class);

		return result;
	}

	public GroupCategoryBO[] getAllGroupCate(int siteID, String langID) throws Throwable {

		var productCate = getAllProductCate(siteID, langID);
		var relationShipGroupCate = getAllRelationShipGroupCate();

		var mapPro = Arrays.stream(productCate).collect(Collectors.groupingBy(x -> x.GroupID, Collectors.toList()));

		var mapRelation = Arrays.stream(relationShipGroupCate)
				.collect(Collectors.groupingBy(x -> x.GroupID, Collectors.toList()));

		List<GroupCategoryBO> lstgr = new ArrayList<GroupCategoryBO>();

		mapPro.entrySet().stream().forEach(p -> {
			GroupCategoryBO gr = new GroupCategoryBO();
			gr.ListCategory = p.getValue();
			mapRelation.entrySet().stream().forEach(r -> {
				if (r.getKey() == p.getKey()) {
					gr.RelationShipGroupCategoryBO = r.getValue();
				}
			});
			lstgr.add(gr);
		});
		return lstgr.toArray(GroupCategoryBO[]::new);
	}

	public GroupCategoryBO[] getGroupCateByParentID(int parentID, int siteID, String langID) throws Throwable {

//		var result = oclient.queryFunction("product_categroup_getByPrarentCate", GroupCategoryBO[].class, parentID,
//				langID, siteID);
		var result = oclient.queryFunctionCached("product_categroup_getByPrarentCate", GroupCategoryBO[].class,
				parentID, langID, siteID);

		if (result != null) {
			result = Arrays.stream(result).filter(x -> x.ParentCateID == parentID).filter(x -> x.IsDeleted == false)
					.toArray(GroupCategoryBO[]::new);
			for (var item : result) {
				item.ListCategory = getListCategoryByGroupID(item.GroupID, siteID, langID);
			}

		}
		return result;
	}

	public GroupCategoryBO[] getGroupCateByGroupID(int groupID, int siteID, String langID) throws Throwable {

//		var result = oclient.queryFunction("product_categroup_getByGroupID", GroupCategoryBO[].class, groupID, siteID,
//				langID);
		var result = oclient.queryFunctionCached("product_categroup_getByGroupID", GroupCategoryBO[].class, groupID,
				siteID, langID);

		if (result != null) {
			for (var item : result) {
				item.ListCategory = getListCategoryByGroupID(groupID, siteID, langID);
			}

		}
		return result;
	}

	public List<ProductCategoryBO> getListCategoryByGroupID(int groupID, int siteID, String langID) throws Throwable {

		var resultListLink = oclient.queryFunctionCached("product_categroup_linkbygroupid",
				RelationShipGroupCategoryBO[].class, groupID);

//		
//		var resultListLink = oclient.queryFunction("product_categroup_linkbygroupid",
//				RelationShipGroupCategoryBO[].class, groupID);	

		var listCate = new int[] {};
		if (resultListLink != null && resultListLink.length > 0) {
			listCate = Arrays.stream(resultListLink).mapToInt(x -> x.CategoryID).toArray();
		}
		if (listCate != null && listCate.length > 0) {
			var tmp = getCategoryByIDList(listCate, siteID, langID);
			var tmplistcate = new ArrayList<ProductCategoryBO>();
			if (tmp != null && tmp.length > 0) {
				for (var catelink : resultListLink) {
					if (catelink.CategoryID > 0) {
						var t = Arrays.stream(tmp).filter(x -> x.CategoryID == catelink.CategoryID).findFirst()
								.orElse(null);
						if (t != null) {
							t.productCategoryQuickLinkBO = Arrays.stream(t.productCategoryQuickLinkBO)
									.sorted(Comparator.comparingInt(z -> z.displayOrder))
									.toArray(ProductCategoryQuickLinkBO[]::new);
							tmplistcate.add(t);
						}

					} else {
						var linktoCate = new ProductCategoryBO();
						linktoCate.CategoryName = catelink.linkname;
						linktoCate.BImage = catelink.avatarimage;
						linktoCate.Icon = catelink.avatarimage;
						linktoCate.CategoryLink = catelink.url;
						linktoCate.URL = catelink.url;
						linktoCate.DisplayOrder = catelink.DisplayOrder;

						tmplistcate.add(linktoCate);
					}
				}
//				for ( var c :tmp ) {
//					if(c == null) continue;
//					c.productCategoryQuickLinkBO = Arrays.stream(c.productCategoryQuickLinkBO).sorted(Comparator.comparingInt(z -> z.displayOrder)).toArray(ProductCategoryQuickLinkBO[]::new);
//				}

			}
			return tmplistcate;// Arrays.asList(tmp.clone());
		}
		return null;
	}

	public int getTotalStoreBySiteID(int siteID) throws Throwable {
		var qry = boolQuery();
		SearchSourceBuilder sb = new SearchSourceBuilder();
		if (siteID > 0) {
			qry.must(termQuery("SiteID", siteID));

		} else {
			qry.must(termsQuery("SiteID", new String[] { "1", "2" }));
		}
		qry.must(termQuery("IsShowweb", true));
		qry.must(termQuery("IsSaleStore", true));

		sb.from(0).size(1).query(qry).aggregation(count("store_count").field("StoreID"));

		var queryResult = clientIndex.search(new SearchRequest(currentStoreIndexDB).source(sb), RequestOptions.DEFAULT);

		ValueCount count = queryResult.getAggregations().get("store_count");
		return (int) count.getValue();

	}
	// MOVE QUA

//	public int getCategoryIDFromCache(int productID) throws Throwable {
//		var rs = oclient.queryFunction("product_getCategoryByProductID", ProductBO[].class, productID);
//		return rs == null || rs.length == 0 ? -1 : rs[0].CategoryID;
//	}

	public int getCategoryIDByProductID(int productID) throws Throwable {
		var rs = oclient.queryFunction("product_getCategoryByProductID", ProductBO[].class, productID);
		return rs == null || rs.length == 0 ? -1 : rs[0].CategoryID;
	}

	public int getCategoryIDByProductIDFromCache(int productID) throws Throwable {
		String key = KEY_CACHED + "product_getCategoryByProductID" + productID + "_CategoryID";
		var rs = (Integer) CacheStaticHelper.GetFromCache(key, 30);
		if (rs == null) {
			rs = getCategoryIDByProductID(productID);
			CacheStaticHelper.AddToCache(key, rs);
		}
		return rs;
	}

	public int getCategoryID(String productCode) throws Throwable {
		var rs = oclient.queryFunction("product_getCategoryByProductCode", ProductBO[].class, productCode);
		return rs == null || rs.length == 0 ? -1 : rs[0].CategoryID;
	}

	public int getCategoryIDFromCache(String productCode) throws Throwable {
		String key = KEY_CACHED + "product_getCategoryByProductCode_" + productCode;
		var rs = (Integer) CacheStaticHelper.GetFromCache(key, 30);
		if (rs == null) {
			rs = getCategoryID(productCode);
			CacheStaticHelper.AddToCache(key, rs);
		}
		return rs;
	}

	public int getManufactureID(int productID) throws Throwable {
		var rs = oclient.queryFunctionCached("product_getCategoryByProductID", ProductBO[].class, productID);
		return (int) (rs == null || rs.length == 0 ? 0 : rs[0].ManufactureID);
	}

	public int getManufactureIDFromCache(int productID) throws Throwable {
		String key = KEY_CACHED + "product_getCategoryByProductID_" + productID + "_ManufactureID";
		var rs = (Integer) CacheStaticHelper.GetFromCache(key, 30);
		if (rs == null) {
			rs = getManufactureID(productID);
			CacheStaticHelper.AddToCache(key, rs);
		}
		return rs;
	}

	public Map<Integer, Integer> getCategoryMap(int[] productID) throws Throwable {
		var products = oclient.queryFunction("product_getCategoryByProductIDList", ProductBO[].class, productID);
		return products == null || products.length == 0 ? new HashMap<>()
				: Stream.of(products).collect(Collectors.toMap(x -> x.ProductID, x -> x.CategoryID));
	}

	public Map<Integer, Integer> getCategoryMapFromCache(int[] productID) throws Throwable {

		String str = Arrays.toString(productID).replaceAll("[^0-9,-]", "").trim();
		String key = KEY_CACHED + "product_getCategoryByProductIDList_" + str;
		var rs = (Map<Integer, Integer>) CacheStaticHelper.GetFromCache(key, 30);
		if (rs == null) {
			rs = getCategoryMap(productID);
			CacheStaticHelper.AddToCache(key, rs);
		}
		return rs;
	}

	public Integer[] getCateIDInactivebyUrl(String url, int siteID, String lang) throws Throwable {
		var rs = oclient.queryFunctionCached("product_getCateInActivedByUrl", ProductCategoryLangBO[].class, url,
				siteID, lang);

		return Stream.of(rs).filter(Objects::nonNull).map(x -> x.CategoryID).toArray(Integer[]::new);
	}
	/**
	 *  -Task lấy list store thoe html id
	 * */
	public InfoBO getStoreByHtmlID(int htmlId) throws Throwable {
		var info = oclient.queryFunctionCached("html_info_GetByHtmlID", InfoBO[].class,
				htmlId, "vi-VN", 1);
		return info != null && info.length > 0 ? info[0] : null;
	}

	public StoreBO[] getStoreByHtmlIDCached(int[] stores) throws Throwable {
//		var mapper = getStoreMapper();
//		String keyT = "getStoreByHtmlIDCached";
//		String key0bject = mapper.writeValueAsString(stores);
//		StoreBO[] result = CachedObject.getObject(keyT,key0bject,180,StoreBO[].class);
//		if(result == null){
			return oclient.queryFunction("product_GetStoreByListID", StoreBO[].class, stores);
//			CachedObject.putObject(keyT,key0bject,result);
//		}
//		return result;
	}
}
