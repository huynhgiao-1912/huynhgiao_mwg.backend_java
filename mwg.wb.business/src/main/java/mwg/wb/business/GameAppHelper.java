package mwg.wb.business;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;
import static org.elasticsearch.search.aggregations.AggregationBuilders.topHits;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder.ScriptSortType;
import org.elasticsearch.search.sort.SortOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.gameapp.GameAppBO;
import mwg.wb.model.gameapp.GameAppCategoryBO;
import mwg.wb.model.gameapp.GameAppDetailPlatformBO;
import mwg.wb.model.gameapp.GameAppFilter;
import mwg.wb.model.gameapp.GameAppSO;
import mwg.wb.model.gameapp.GameAppSR;
import mwg.wb.model.gameapp.PlatformGameAppBO;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.news.NewsSR;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductCategoryBO;
import mwg.wb.model.products.ProductGalleryBO;
import mwg.wb.model.search.NewsSO;

public class GameAppHelper {

	private static ORThreadLocal oclient = null;
	protected ObjectMapper mapper = null;
	ClientConfig config = null;

	protected RestHighLevelClient clientIndex = null;
	private ElasticClient elasticClient = null;
	protected String CurrentGameAppIndexDB = null;
	protected String CurrentGameAppnewIndexDB = null;

	public GameAppHelper(ORThreadLocal afactoryRead, ClientConfig aconfig) {
		CurrentGameAppIndexDB = "ms_gameapp"; // aconfig.ELASTICSEARCH_PRODUCT_INDEX;
		CurrentGameAppnewIndexDB = "ms_news";
		oclient = afactoryRead;
		config = aconfig;
		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatStringNews);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	elasticClient = ElasticClient.getInstance(config.SERVER_ELASTICSEARCH_READ_HOST);
			//elasticClient =  new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST); 
		clientIndex = elasticClient.getClient();
	}

	public ProductBO GetProductBOByProductIDSE(int productID, int siteID, String lang) throws Throwable {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("p", productID);
		params.put("s", siteID);
		params.put("l", lang);

		String functionname = "select product_GetByIdSE(:p,:s,:l) as rs";
		;
		ProductBO[] temp = oclient.QueryFunction( functionname, params, ProductBO[].class, true);
		if (temp != null && temp.length > 0) {
			return temp[0];
		} else {
			return null;
		}
	}

	public PlatformGameAppBO[] GetAllPlatformGameapp(Integer siteID, String langID) throws Throwable {

		var platforms = oclient.QueryFunctionCached("gameapp_getAllGameappPlatform", PlatformGameAppBO[].class, false, siteID,
				langID);
		if (platforms != null)
			return platforms;
		return null;
	}

//	public GameAppSR GameAppFilter(int categoryID, int sortType, int pageIndex, int pageSize,
//			Integer siteID, boolean isTopview, int platformID, GameAppFilter filter) throws IOException {
	public GameAppSR GameAppFilter(GameAppFilter filter) throws Throwable {
		if (filter.pageSize < 0)
			filter.pageSize = 10;
		if (filter.pageSize > 50)
			filter.pageSize = 50;
		if (filter.pageSize * filter.pageIndex > 10000)
			filter.pageIndex = 0;
		GameAppSR gameapp = new GameAppSR();
		// filter.
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("SiteID", filter.siteID));

		q.must(termQuery("IsActived", 1));
		q.must(rangeQuery("ActivedDate").lte(new Date().getTime()));

//		if(filter.parentID > 0) {
//			q.must(termQuery("CategoryID", filter.parentID));
//		}
		if (filter.parentID > 0) {
			q.must(termQuery("ParentID", filter.parentID));
		}
		if (filter.platformID > 0) {
			String[] listPL = Integer.toString(filter.platformID).split(",");
			q.must(termsQuery("PlatformIDList", listPL));
		}

		sb.from(filter.pageIndex * filter.pageSize).size(filter.pageSize).query(q);

		if (filter.sortType == javax.swing.SortOrder.ASCENDING) {
			sb.sort("ActivedDate", SortOrder.ASC);
			if (filter.isTopView) {
				sb.sort("ViewCount", SortOrder.ASC);
			}
		}

		if (filter.sortType == javax.swing.SortOrder.DESCENDING) {
			sb.sort("ActivedDate", SortOrder.DESC);
			if (filter.isTopView) {
				sb.sort("ViewCount", SortOrder.DESC);
			}
		}

		var searchRequest = new SearchRequest(CurrentGameAppIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {
			// System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), GameAppSO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.ProductID).toArray();

			// System.out.println("idlist: " + idlist.length);

			gameapp.result = oclient.QueryFunction("gameapp_GetByIdList", GameAppBO[].class, false, idlist,
					filter.siteID, filter.langID);
			gameapp.total = (int) queryResults.getHits().getTotalHits().value;
			gameapp.message = "";

		} catch (Exception e) {

			Logs.LogException(e);
		}

		return gameapp;
	}

	public GameAppBO GetGameappByID(int gameAppID, Integer siteID) throws Throwable {
		var gameapp = oclient.QueryFunctionCached("gameapp_GetById", GameAppBO[].class, false, gameAppID, siteID, "vi-VN");
		if (gameapp != null && gameapp.length > 0) {
			if (gameapp[0].ProductLanguageBO != null && gameapp[0].ProductLanguageBO.CategoryIDList != null
					&& gameapp[0].ProductLanguageBO.CategoryIDList.length() > 0) {
				var listID = gameapp[0].ProductLanguageBO.CategoryIDList.split(",");
				gameapp[0].ProductCategorys = oclient.QueryFunction("gameapp_getByListCategoryID",
						ProductCategoryBO[].class, false, listID);
			}
			return gameapp[0];
		}

		return null;
	}

	public GameAppBO ReviewGameappByID(int gameAppID, Integer siteID) throws Throwable {
		var gameapp = oclient.QueryFunctionCached("gameapp_ReViewById", GameAppBO[].class, false, gameAppID, siteID, "vi-VN");
		if (gameapp != null && gameapp.length > 0) {
			if (gameapp[0].ProductLanguageBO != null && gameapp[0].ProductLanguageBO.CategoryIDList != null
					&& gameapp[0].ProductLanguageBO.CategoryIDList.length() > 0) {
				var listID = gameapp[0].ProductLanguageBO.CategoryIDList.split(",");
				gameapp[0].ProductCategorys = oclient.QueryFunction("gameapp_getByListCategoryID",
						ProductCategoryBO[].class, false, listID);
			}
			return gameapp[0];
		}

		return null;
	}

	public GameAppSR SearchGameApp2020(mwg.wb.model.gameapp.GameAppFilter filter) throws Throwable {

		if (filter.pageSize < 0)
			filter.pageSize = 10;
		if (filter.pageSize > 50)
			filter.pageSize = 50;
		if (filter.pageSize * filter.pageIndex > 10000)
			filter.pageIndex = 0;
		GameAppSR gameapp = new GameAppSR();
		// filter.
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("SiteID", filter.siteID));
		q.must(termQuery("IsActived", 1));
		q.must(rangeQuery("ActivedDate").lte(new Date().getTime()));
		// Utils.GetDefaultDate().getTime()

		if (filter.isFeature > 0) {//
			q.must(termQuery("IsFeature", 1));
			q.must(rangeQuery("FeatureDate").lte(new Date().getTime()));
		}
		if (filter.categoryID > 0) {
			q.must(termQuery("CategoryID", filter.categoryID));
		}
		if (filter.parentID > 0) {
			q.must(termQuery("ParentID", filter.parentID));
		}
		if (filter.platformID > 0) {
			// String[] listPL = Integer.toString(filter.platformID).split(",");
			q.must(termsQuery("PlatformIDList", Integer.toString(filter.platformID)));
		}
		if (filter.platformIDList != null && filter.platformIDList.length > 0) {
			q.must(termsQuery("PlatformIDList", filter.platformIDList));
		}
		if (filter.categoryIDList != null  && filter.categoryIDList.length > 0) { // termS
			q.must(termsQuery("CategoryIDList", filter.categoryIDList));
		}
		if (filter.isShowHome) {
			q.must(termQuery("IsShowHome", 1));
		}

		if (!Utils.StringIsEmpty(filter.keyWord)) {
			// var keyword = DidxHelper.FilterVietkey(filter.keyWord);
			var keyword = DidxHelper.FormatKeywordAZ(DidxHelper.FilterVietkey(filter.keyWord)).toLowerCase();
			if (!Utils.StringIsEmpty(keyword)) {

				String rsl = "";
				String[] keywords = keyword.split(" ");
				if (keywords.length == 1) {
					rsl = "(\"" + keyword + "\")";
					// rsltitle = "(*" + keyword + "* OR (ProductName:" + keyword + ") OR
					// (ProductName:\"" + keyword + "\")) ";
				} else {
					rsl = "(" + String.join(" AND ", keywords) + ")";
				}
				q.must(queryStringQuery(rsl).field("ProductName").boost(10.0f).field("KeyWord").boost(2.0f)
						.field("CategoryNameList").boost(1.0f)); // CategoryNameList

			}

		}
		//

		sb.from(filter.pageIndex * filter.pageSize).size(filter.pageSize).query(q);
		if (!Utils.StringIsEmpty(filter.keyWord)) {
			sb.sort("_score", SortOrder.DESC);
		} else {
			if (filter.sortType == javax.swing.SortOrder.ASCENDING) {

				if (filter.isTopView) {
					sb.sort("ViewCount", SortOrder.ASC);
				} else {
					sb.sort("ActivedDate", SortOrder.ASC);
				}
			}

			if (filter.sortType == javax.swing.SortOrder.DESCENDING) {

				if (filter.isTopView) {
					sb.sort("ViewCount", SortOrder.DESC);
				} else {
					sb.sort("ActivedDate", SortOrder.DESC);
				}
			}
		}
		try {
			var searchRequest = new SearchRequest(CurrentGameAppIndexDB);
			searchRequest.source(sb);
			var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
	
			// System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), GameAppSO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.ProductID).toArray();
	
			// System.out.println("idlist: " + idlist.length);
	
			gameapp.result = oclient.QueryFunction("gameapp_GetByIdList", GameAppBO[].class, false, idlist, filter.siteID,
					filter.langID);
			gameapp.total = (int) queryResults.getHits().getTotalHits().value;
			gameapp.message = "ID: " + Arrays.toString(idlist);
	
			if (filter.isGetNews > 0) {
				if (gameapp.result != null && gameapp.result.length > 0) {
					// int[] gameappidlist =
					// Stream.of(gameapp.result).collect(Collectors.toList()).stream().mapToInt(x->x.ProductID).toArray();
					// var ListNews = GetNewsByProductListID(gameappidlist,2,1);
	
					for (var item : gameapp.result) {
						// item.NewsBO =
						// Stream.of(ListNews).filter(x->x.ProductIDList.contains(Integer.toString(item.ProductID))).limit(2).toArray(NewsBO[]::new);
						item.NewsBO = GetNewsByProductID(item.ProductID, filter.isGetNews, filter.siteID);
					}
				}
			}
		} catch (Exception e) {
			
			Logs.LogException(e);
		}
		return gameapp;
	}

	private NewsBO[] GetNewsByProductID(int productID, int limit, int siteID) throws Throwable {

		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("SiteID", siteID));

		// q.must(termsQuery("ListTreeCategory", "g2002g")); // 2002 là parent news của
		// gameapp
		// g2002g

		q.must(termQuery("IsActived", 1));
		q.must(rangeQuery("ActivedDate").lte(new Date()));

		if (productID > 0) {
			String[] listPL = Integer.toString(productID).split(",");
			q.must(termsQuery("ProductIDList", listPL));
		}

		sb.from(0).size(limit).query(q).sort("ActivedDate", SortOrder.DESC);

		var searchRequest = new SearchRequest(CurrentGameAppnewIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {

			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), NewsBO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();

			// System.out.println("idlist: " + idlist.length);

			var result = oclient.QueryFunction("news_GetByIDList", NewsBO[].class, false, idlist);
			return result;

		} catch (Exception e) {
			Logs.LogException(e);
		}
		return null;
	}

	public NewsBO[] GetNewsByProductListID(int[] productID, int limit, int siteID) throws Throwable {

		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("SiteID", siteID));

		// q.must(termsQuery("ListTreeCategory", "g2002g")); // 2002 là parent news của
		// gameapp
		// g2002g

		q.must(termQuery("IsActived", 1));
		q.must(rangeQuery("ActivedDate").lte(new Date()));

		if (productID.length > 0) {
			// String[] listPL = Integer.toString(productID).split(",");
			q.must(termsQuery("ProductIDList", productID));
		}

		sb.from(0).size(limit).query(q).sort("ActivedDate", SortOrder.DESC);

		sb.aggregation(terms("ProductIDList").field("ProductIDList").size(productID.length)
				.subAggregation(topHits("topProductIDListHits").size(limit).sort("ActivedDate", SortOrder.DESC)));

		var searchRequest = new SearchRequest(CurrentGameAppnewIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		var aggrs = queryResults.getAggregations();
		ParsedStringTerms bucket = (ParsedStringTerms) aggrs.get("ProductIDList");
		if (bucket == null || bucket.getBuckets().size() == 0)
			return null;

		try {
			var idlist = new ArrayList<Integer>();
			bucket.getBuckets().stream().map(v -> (TopHits) v.getAggregations().get("topProductIDListHits"))
					.forEach(h -> h.getHits().forEach(v -> {
						try {
							var so = mapper.readValue(v.getSourceAsString(), NewsBO.class);
							idlist.add(so.NewsID);
						} catch (IOException e) {
							Logs.LogException(e);
						}
					}));

//			idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
//				try {
//					return mapper.readValue(x.getSourceAsString(), NewsBO.class);
//				} catch (IOException e1) {
//					return null;
//				}
//			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();

			// System.out.println("idlist: " + idlist.length);

			var result = oclient.QueryFunction("news_GetByIDList", NewsBO[].class, false, idlist);
			return result;

		} catch (Exception e) {
			Logs.LogException(e);
		}
		return null;
	}
	public List<Integer> GetNewsByProductListID2(int productID, int limit, int siteID) throws Throwable {

		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("SiteID", siteID));
		q.must(termQuery("IsActived", 1));
		q.must(rangeQuery("ActivedDate").lte(new Date()));
		q.must(termQuery("ProductIDList", productID));

		sb.from(0).size(limit).query(q).sort("ActivedDate", SortOrder.DESC);

		var searchRequest = new SearchRequest(CurrentGameAppnewIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		var idlist = new ArrayList<Integer>();
		try {
		 Stream.of(queryResults.getHits().getHits()).forEach(x -> {
				try {
					var so =  mapper.readValue(x.getSourceAsString(), NewsBO.class);
					idlist.add(so.NewsID);
				} catch (IOException e1) {
				}
			});
		} catch (Exception e) {
			Logs.LogException(e);
		}
		return idlist;
	}

	public GameAppCategoryBO[] GetCateggoryByPlatformID(int parentID, int platformID) throws Throwable {
		var gameappcat = oclient.QueryFunctionCached("gameapp_GetCategoriesByPlatformID", GameAppCategoryBO[].class, false,
				parentID, platformID);
		if (gameappcat != null)
			return gameappcat;
		return null;
	}

	public GameAppCategoryBO[] GetCateggoryByParentID(int parentID) throws Throwable {
		var gameappcat = oclient.QueryFunctionCached("gameapp_GetCategoriesByParentID", GameAppCategoryBO[].class, false,
				parentID);
		if (gameappcat != null)
			return gameappcat;
		return null;
	}

	public GameAppSR GetGameAppRelatedByGameAppID(int gameAppID, int siteID, int pageIndex, int pageSize)
			throws Throwable {
		if (pageSize < 0)
			pageSize = 10;
		if (pageSize > 50)
			pageSize = 50;
		if (pageSize * pageIndex > 10000)
			pageIndex = 0;
		GameAppSR gameapp = new GameAppSR();
		GameAppBO gameBO = null;

		var gameDetail = oclient.QueryFunctionCached("gameapp_GetByIdSE", GameAppBO[].class, false, gameAppID, siteID,
				"vi-VN");
		if (gameDetail == null || gameDetail.length == 0) {
			return gameapp;
		} else {
			gameBO = gameDetail[0];
		}

		// filter.
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("SiteID", siteID));
		q.must(termQuery("IsActived", 1));

		q.must(rangeQuery("ActivedDate").lte(new Date().getTime()));

		q.mustNot(termQuery("ProductID", gameAppID));

		if (gameBO != null && gameBO.ProductLanguageBO != null && gameBO.ProductLanguageBO.CategoryIDList != null
				&& gameAppID > 0) {

			q.must(termsQuery("CategoryIDList", gameBO.ProductLanguageBO.CategoryIDList.split(",")));

		}

		sb.from(pageIndex * pageSize).size(pageSize).query(q);

		sb.sort("ActivedDate", SortOrder.DESC);

		var searchRequest = new SearchRequest(CurrentGameAppIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {
			// System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), GameAppSO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.ProductID).toArray();

			// System.out.println("idlist: " + idlist.length);

			gameapp.result = oclient.QueryFunction("gameapp_GetByIdList", GameAppBO[].class, false, idlist, siteID,
					"vi-VN");
			gameapp.total = (int) queryResults.getHits().getTotalHits().value;
			gameapp.message = "";

		} catch (Exception e) {

			Logs.LogException(e);
		}

		return gameapp;
	}

	public PlatformGameAppBO[] GetPlatFormByID(int platformID, Integer siteID, String langID) throws Throwable {
		var platform = oclient.QueryFunctionCached("gameapp_GameappPlatformByID", PlatformGameAppBO[].class, false,
				platformID, siteID, langID);
		if (platform != null && platform.length > 0)
			return platform;
		return null;
	}

	public PlatformGameAppBO GetPlatFormByURL(String url, Integer siteID, String langID) throws Throwable {
		var platforms = oclient.QueryFunctionCached("gameapp_getGameappPlatformbyurl", PlatformGameAppBO[].class, false, url);
		if (platforms != null && platforms.length > 0)
			return platforms[0];
		return null;
	}

	public GameAppCategoryBO GetCateggoryByID(int categoryID) throws Throwable {
		var platforms = oclient.QueryFunctionCached("gameapp_GetCategoriesByID", GameAppCategoryBO[].class, false,
				categoryID);
		if (platforms != null && platforms.length > 0)
			return platforms[0];
		return null;
	}

	public GameAppDetailPlatformBO[] GetGameappPlatform(String[] platformIdList, int gameAppID) throws Throwable {
		var platforms = oclient.QueryFunction("gameapp_GetProPlatformByID", GameAppDetailPlatformBO[].class, false,
				platformIdList, gameAppID);
		if (platforms != null)
			return platforms;
		return null;
	}

	public GameAppDetailPlatformBO[] GetGameappPlatformByID(int gameAppID) throws Throwable {
		var platforms = oclient.QueryFunctionCached("gameapp_getproductPlatformbyID", GameAppDetailPlatformBO[].class, false,
				gameAppID);
		if (platforms != null) {
			var allplatform = oclient.QueryFunctionCached("gameapp_getAllGameappPlatform", PlatformGameAppBO[].class, false,
					1, "vi-VN");

			for (GameAppDetailPlatformBO item : platforms) {
				var find = Stream.of(allplatform).filter(x -> x.platformID == item.platformID).findFirst();
				if (find.isPresent() && find.get() != null)
					item.platformName = find.get().platformName;
			}

			return platforms;
		}

		return null;
	}

	public ProductGalleryBO[] GetGalleryByProductID(int gameAppID, int platformID) throws Throwable {
		var gallery = oclient.QueryFunctionCached("gameapp_GetGalleryByProductID", ProductGalleryBO[].class, false, gameAppID,
				1, platformID);
		if (gallery != null)
			return gallery;
		return null;
	}

	public ProductGalleryBO[] GetGalleryByProductIDTest(int gameAppID) throws Throwable {
		var gallery = oclient.QueryFunctionCached("gameapp_GetGalleryByProductIDTest", ProductGalleryBO[].class, false,
				gameAppID, 1);
		if (gallery != null)
			return gallery;
		return null;
	}

	public ProductGalleryBO[] GetGalleryByProductIDTest2(int gameAppID) throws Throwable {
		var gallery = oclient.QueryFunction("gameapp_GetGalleryByProductIDTest111", ProductGalleryBO[].class, false,
				gameAppID, 1);
		if (gallery != null)
			return gallery;
		return null;
	}

	public GameAppBO[] GetGameappFeatureInterlace() throws Throwable {
		// TODO Auto-generated method stub
		// region m
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("SiteID", 1));
		q.must(termQuery("IsActived", 1));
		q.must(termQuery("IsShowHome", 1));
		q.must(rangeQuery("ActivedDate").lte(new Date().getTime()));
		// endregion

		// lấy gameapp feature
		q.must(termQuery("IsFeature", 1));
		q.must(rangeQuery("FeatureDate").lte(new Date().getTime()));

		sb.from(0).size(0).query(q);

		sb.sort("ActivedDate", SortOrder.DESC);
		sb.aggregation(terms("ParentID").field("ParentID").size(2).subAggregation(topHits("topParentIDHits").size(2)
				.sort(new ScriptSortBuilder(new Script("Math.random()"), ScriptSortType.NUMBER).order(SortOrder.ASC))

		));

		var searchRequest = new SearchRequest(CurrentGameAppIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		var aggrs = queryResults.getAggregations();
		ParsedLongTerms bucket = (ParsedLongTerms) aggrs.get("ParentID");
		if (bucket == null || bucket.getBuckets().size() == 0)
			return null;

		List<Integer> arrlistGame = new ArrayList<Integer>();
		List<Integer> arrlistApp = new ArrayList<Integer>();
		List<Integer> arrlist = new ArrayList<Integer>();
		bucket.getBuckets().stream().map(v -> (TopHits) v.getAggregations().get("topParentIDHits"))
				// .collect(Collectors.toList())
				.forEach(h -> h.getHits().forEach(v -> {
					try {
						var so = mapper.readValue(v.getSourceAsString(), GameAppSO.class);
						if (so.ParentID == 8232) // game
						{
							arrlistGame.add(so.ProductID);
						} else { // ngược lại thì là 8233
							arrlistApp.add(so.ProductID);
						}

					} catch (IOException e) {
						Logs.LogException(e);
					}
				}));
		// xử ký sắp xếp lại theo game - app - game - app
		var index = 0;
		for (Integer gameid : arrlistGame) {
			arrlist.add(gameid);
			try {
				if (arrlistApp.get(index) != null)
					arrlist.add(arrlistApp.get(index));
			} catch (Exception e) {
				// list arrlistApp ko có phần từ từ index này trở đi.
				// xử lý ngoại lệ nếu list 2 ko có thì nối list 1 vô
				for (int i = index + 1; i < arrlistGame.size(); i++) {
					arrlist.add(arrlistGame.get(i));
				}
				Logs.LogException(e);
				break;
			}
			index++;
		}

		if (arrlist != null && arrlist.size() > 0) {
			var result = oclient.QueryFunction("gameapp_GetByIdList", GameAppBO[].class, false, arrlist.toArray(), 1,
					"vi-VN");
			if (result != null || result.length > 0) {
				int[] gameappidlist = Stream.of(result).collect(Collectors.toList()).stream().mapToInt(x -> x.ProductID)
						.toArray();
				
				NewsBO[] ListNews =null;
				if( gameappidlist.length > 0) {
					
					var idsNews = new ArrayList<Integer>();
					for(int i : gameappidlist) {
					   var idNews =	GetNewsByProductListID2(i, 2, 1);
					   idsNews.addAll(idNews);
					}
					ListNews  = oclient.QueryFunction("news_GetByIDList", NewsBO[].class, false, idsNews);
				} else {
					
					 ListNews = GetNewsByProductListID(gameappidlist, 2, 1);
				}
				for (var item : result) {
					item.NewsBO = Stream.of(ListNews)
							.filter(x -> x.ProductIDList.contains(Integer.toString(item.ProductID))).limit(2)
							.toArray(NewsBO[]::new);
					// GetNewsByProductID(item.ProductID,2, 1); //collect(Collectors.toList()).
				}
				return result;
			}
		}

		return null;
	}

	public GameAppBO[] GetGameAppInterlaceFromCache(String key) throws Throwable {

		// String key = key;
		GameAppBO[] rs = (GameAppBO[]) CacheStaticHelper.GetGameAppInterlace(key);
		if (rs == null) {
			rs = GetGameappFeatureInterlace();
			CacheStaticHelper.AddGameAppInterlaceToCache(key, rs);
		}

		return rs;

	}

	public GameAppSR GetGameAppByDate(Date fromDate, Date toDate, int categoryID, int pageSize, int pageIndex)
			throws Throwable {
		if (pageSize < 0)
			pageSize = 10;
		if (pageSize > 500)
			pageSize = 500;
		if (pageSize * pageIndex > 10000)
			pageIndex = 0;
		GameAppSR gameappSR = new GameAppSR();

		var sb = new SearchSourceBuilder();

		var q = boolQuery();

		if (categoryID > 0) {
			q.must(termQuery("CategoryID", categoryID));
		}
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("IsActived", 1));
		q.must(rangeQuery("ActivedDate").lte(new Date().getTime()));

		q.must(rangeQuery("ActivedDate").gte(fromDate.getTime()));
		q.must(rangeQuery("ActivedDate").lte(toDate.getTime()));
	
		sb.from(pageIndex * pageSize).size(pageSize).query(q);

		var searchRequest = new SearchRequest(CurrentGameAppIndexDB);
		searchRequest.source(sb);

		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), GameAppSO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.ProductID).toArray();

			if(idlist != null && idlist.length > 0) {
				gameappSR.result = oclient.QueryFunction("gameapp_GetByIdList", GameAppBO[].class, false, idlist, 1, "vi-VN");
				gameappSR.total = (int) queryResults.getHits().getTotalHits().value;
			}
			return gameappSR;

		} catch (Exception e) {

			Logs.LogException(e);
		}
		return null;
	}


	

	public String ObjectToString(Object t) {
		try {
			return mapper.writeValueAsString(t);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

}
