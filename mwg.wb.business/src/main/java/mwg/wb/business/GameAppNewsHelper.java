package mwg.wb.business;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.scriptFunction;
import static org.elasticsearch.search.sort.SortBuilders.scriptSort;
import static org.elasticsearch.index.query.QueryBuilders.boostingQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.index.query.QueryBuilders.scriptQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Stream;

import javax.mail.search.NotTerm;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.ScriptQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.client.cache.IgniteClient;
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.gameapp.GameAppBO;
import mwg.wb.model.gameapp.GameAppCategoryBO;
import mwg.wb.model.gameapp.GameAppSO;
import mwg.wb.model.gameapp.GameAppSR;
import mwg.wb.model.gameapp.NewsGameAppFilter;
import mwg.wb.model.gameapp.PlatformGameAppBO;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.news.NewsSR;
import mwg.wb.model.search.NewsSO;
import mwg.wb.model.webservice.NewsView;

public class GameAppNewsHelper {
	private static ORThreadLocal oclient = null;
	protected ObjectMapper mapper = null;
	ClientConfig config = null;

	protected RestHighLevelClient clientIndex = null;
	private ElasticClient elasticClient = null;
	protected String CurrentGameAppNewsIndexDB = "";

	public GameAppNewsHelper(ORThreadLocal afactoryRead, ClientConfig aconfig) {
		CurrentGameAppNewsIndexDB = "ms_news"; // aconfig.ELASTICSEARCH_PRODUCT_INDEX;
		oclient = afactoryRead;
		config = aconfig;
		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatStringNews);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		elasticClient = ElasticClient.getInstance(config.SERVER_ELASTICSEARCH_READ_HOST);
	//	elasticClient=  new ElasticClient(aconfig.SERVER_ELASTICSEARCH_READ_HOST); 

		clientIndex = elasticClient.getClient();
	}

	public NewsSR GetGameAppNews(int siteID, int pageIndex, int pageSize, int sortType) throws Throwable {

		if (pageSize < 0)
			pageSize = 10;
		if (pageSize > 50)
			pageSize = 50;
		if (pageSize * pageIndex > 10000)
			pageIndex = 0;
		NewsSR gameAppNews = new NewsSR();
		// filter.
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("SiteID", siteID));
		q.must(termQuery("IsActived", 1));
		q.must(termsQuery("ListTreeCategory", "g2002g")); // 2002 là parent news của gameapp

		q.must(rangeQuery("ActivedDate").lte(new Date()));

		sb.from(pageIndex * pageSize).size(pageSize).query(q);

		if (sortType == 1) {
			sb.sort("ActivedDate", SortOrder.ASC);
		}
		if (sortType == 2) {
			sb.sort("ActivedDate", SortOrder.DESC);
		}

		var searchRequest = new SearchRequest(CurrentGameAppNewsIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {
			// System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), NewsBO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();

			// System.out.println("idlist: " + idlist.length);

			gameAppNews.result = oclient.QueryFunction("gameapp_GetNewsByListNewsID", NewsBO[].class, false, idlist,
					siteID, "vi-VN");
			gameAppNews.total = (int) queryResults.getHits().getTotalHits().value;
			gameAppNews.message = "sortType: 1 = ASC, 2 = DESC";

		} catch (Exception e) {

			Logs.LogException(e);
		}

		return gameAppNews;
	}

	public NewsSR GetListNews(int IsGame, int siteID, int pageIndex, int pageSize, int sortType) throws Throwable {

		if (pageSize < 0)
			pageSize = 10;
		if (pageSize > 50)
			pageSize = 50;
		if (pageSize * pageIndex > 10000)
			pageIndex = 0;
		NewsSR gameAppNews = new NewsSR();
		// filter.
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("SiteID", siteID));
		q.must(termQuery("IsActived", 1));
		q.must(termsQuery("ListTreeCategory", "g2002g")); // g2002g là parent news của gameapp
		q.must(rangeQuery("ActivedDate").lte(new Date()));

		sb.from(pageIndex * pageSize).size(pageSize).query(q);

		if (sortType == 1) {
			sb.sort("ActivedDate", SortOrder.ASC);
		}
		if (sortType == 2) {
			sb.sort("ActivedDate", SortOrder.DESC);
		}
		if (IsGame >= 0) {
			q.must(termQuery("IsGame", IsGame));
		}
		var searchRequest = new SearchRequest(CurrentGameAppNewsIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {
			// System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), NewsBO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();

			// System.out.println("idlist: " + idlist.length);

			gameAppNews.result = oclient.QueryFunction("gameapp_GetNewsByListNewsID", NewsBO[].class, false, idlist,
					siteID, "vi-VN");
			gameAppNews.total = (int) queryResults.getHits().getTotalHits().value;
			gameAppNews.message = "sortType: 1 = ASC, 2 = DESC, SORT BY ActivedDate";

		} catch (Exception e) {

			Logs.LogException(e);
		}

		return gameAppNews;
	}

	public NewsSR GetNewTopView7Day(int siteID, int pageIndex, int pageSize) throws Throwable {

		NewsSR gameAppNews = new NewsSR();
		// filter.
		try {
			// IgniteClient ignite = new IgniteClient(config);
			var getata = IgniteClient.GetClient(config).GetKey("MWG_News_TopView7D", NewsView[].class);
			if (getata != null && getata.length > 0) {
				var idlist = Stream.of(getata).filter(x -> x != null).skip(pageIndex * pageSize).limit(pageSize)
						.mapToInt(x -> x.NewsID).toArray();
				// System.out.println("idlist: " + idlist.length);

				gameAppNews.result = oclient.QueryFunction("gameapp_GetNewsByListNewsID", NewsBO[].class, false, idlist,
						siteID, "vi-VN");
				gameAppNews.total = (int) getata.length;
				gameAppNews.message = "SORT BY ViewCounter7Days";
			} else {
				gameAppNews.message = "IgniteClient null";
			}

		} catch (Exception e) {
			gameAppNews.message = Utils.stackTraceToString(e);
			Logs.LogException(e);
		}

		return gameAppNews;
	}

	public NewsSR GetNewsRelatedByGameAppID(int newsId, int gameAppID, int siteID, int pageIndex, int pageSize)
			throws Throwable {
		if (pageSize < 0)
			pageSize = 10;
		if (pageSize > 50)
			pageSize = 50;
		if (pageSize * pageIndex > 10000)
			pageIndex = 0;
		NewsSR gameAppNews = new NewsSR();
		// filter.
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("IsActived", 1));
		q.must(rangeQuery("ActivedDate").lte(new Date()));
		// q.must(termQuery("SiteID", siteID));
		q.must(termsQuery("ListTreeCategory", "g2002g")); // 2002 là parent news của gameapp

		if (gameAppID > 0) {
			q.must(termsQuery("ProductIDList", Integer.toString(gameAppID)));
			q.must(termQuery("CountProductIDList", "1"));
			// q.must(rangeQuery("CountProductIDList").gte(2));
		}
		if (newsId > 0) {
			q.mustNot(termQuery("NewsID", Integer.toString(newsId)));
		}
		q.must(termQuery("SiteID", siteID));
		sb.from(pageIndex * pageSize).size(pageSize).query(q);

		sb.sort("ActivedDate", SortOrder.DESC);

		var searchRequest = new SearchRequest(CurrentGameAppNewsIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {
			// System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), NewsBO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();

			// System.out.println("idlist: " + idlist.length);

			gameAppNews.result = oclient.QueryFunction("gameapp_GetNewsByListNewsID", NewsBO[].class, false, idlist,
					siteID, "vi-VN");
			gameAppNews.total = (int) queryResults.getHits().getTotalHits().value;
			gameAppNews.message = "";

		} catch (Exception e) {

			Logs.LogException(e);
		}

		return gameAppNews;
	}

	public NewsSR SearchNews(String keyword, int siteID, int pageIndex, int pageSize) throws Throwable {

		if (pageSize < 0)
			pageSize = 10;
		if (pageSize > 50)
			pageSize = 50;
		if (pageSize * pageIndex > 10000)
			pageIndex = 0;
		NewsSR gameAppNews = new NewsSR();
		String rsl = "";
		String rsltitle = "";

		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("IsActived", 1));
		q.must(termsQuery("ListTreeCategory", "g2002g")); // 2002 là parent news của gameappƯ
		q.must(rangeQuery("ActivedDate").lte(new Date()));

		if (!Utils.StringIsEmpty(keyword)) {
			keyword = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(keyword));
			keyword = keyword.replaceAll("[\\{|\\}]+", "");
			// keyword = keyword.replace(".", " ").replace(":", "").replace("-", "").trim();
			String[] keywords = keyword.split("\\s+");
			if (keywords.length == 1) {
				rsl = "(\"" + keyword + "\")";
				// rsltitle = "(*" + keyword + "* OR (ProductName:" + keyword + ") OR
				// (ProductName:\"" + keyword + "\")) ";
			} else {
				rsl = "(" + String.join(" AND ", keywords) + ")";
//				rsl = "(*" + keyword.replace(" ", "") + "* OR (keyword:\"" + keyword + "\")  OR (keyword:\"" + keyword
//						+ "*\") OR (" + String.join(" AND ", keywords) + "*) OR (keyword:"
//						+ String.join(" AND ", keywords) + ") OR  (SETIitle:\"" + keyword + "\") " + " OR ("
//						+ String.join(" AND ", keywords) + "*) OR (SETIitle:" + String.join(" AND ", keywords)
//						+ ")  ) ";

//				rsltitle = "(*" + keyword.replace(" ", "") + "* OR (SETIitle:\"" + keyword + "\")  OR (SETIitle:\"" + keyword
//						+ "*\") OR (" + String.join(" AND ", keywords) + "*) OR (SETIitle:"
//						+ String.join(" AND ", keywords) + ")  ) ";
			}
		}

		// q.must(boostingQuery(queryStringQuery(rsl).field("ProductName").boost(5f),
		// null).negativeBoost(0.8f));

		// q.must(boostingQuery(queryStringQuery(rsl).field("KeyWord").boost(5f),
		// null).negativeBoost(0.2f));
		// q.must(boostingQuery(queryStringQuery(rsltitle).field("SETIitle").boost(5f),
		// null).negativeBoost(0.2f));
		q.must(queryStringQuery(rsl).field("SETIitle").boost(1.0f)); // field("KeyWord").boost(0.2f)

		// q.must(queryStringQuery(rsl).field("KeyWord"));
		// q.must(queryStringQuery(queryString))
		sb.from(pageIndex * pageSize).size(pageSize).query(q);

		sb.sort("ActivedDate", SortOrder.DESC);

		var searchRequest = new SearchRequest(CurrentGameAppNewsIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {
			// System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), NewsBO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();

			// System.out.println("idlist: " + idlist.length);

			gameAppNews.result = oclient.QueryFunction("gameapp_GetNewsByListNewsID", NewsBO[].class, false, idlist,
					siteID, "vi-VN");
			gameAppNews.total = (int) queryResults.getHits().getTotalHits().value;
			gameAppNews.message = "";

		} catch (Exception e) {

			Logs.LogException(e);
		}
		return gameAppNews;
	}

	public NewsBO GetNewsDetail(int newsID, int siteID) throws Throwable {
		// gameapp_getNewsByNewsID
		var result = oclient.QueryFunction("gameapp_getNewsByNewsID", NewsBO[].class, false, newsID, siteID, "vi-VN");
		if (result != null && result.length > 0)
			return result[0];
		return null;
	}

	public NewsBO ReviewNewsDetail(int newsID, int siteID) throws Throwable {
		// gameapp_getNewsByNewsID
		var result = oclient.QueryFunction("gameapp_ReViewNewsByNewsID", NewsBO[].class, false, newsID, siteID,
				"vi-VN");
		if (result != null && result.length > 0)
			return result[0];
		return null;
	}

	public NewsSR GetNewsByPlatformID(int platformID, int categoryID, int isGame, int isLatest, int isMostView,
			int pageIndex, int pageSize, int siteID) throws Throwable {

		if (pageSize < 0)
			pageSize = 10;
		if (pageSize > 50)
			pageSize = 50;
		if (pageSize * pageIndex > 10000)
			pageIndex = 0;
		NewsSR news = new NewsSR();
		// filter.
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("IsActived", 1));

		if (categoryID > 0) {
			q.must(termQuery("ProductCategoryIDList", categoryID));
		}

		q.must(termQuery("SiteID", siteID));
		q.must(rangeQuery("ActivedDate").lte(new Date()));

		// lay bai tin game?
		q.must(termsQuery("ListTreeCategory", "g2002g"));

		if (isGame >= 0) {
			q.must(termQuery("IsGame", isGame));
		}
		if (platformID > 0) {
//			String[] listPL = Integer.toString(platformID).split(",");
			q.must(termsQuery("PlatformIDList", Integer.toString(platformID)));
		}

		sb.from(pageIndex * pageSize).size(pageSize).query(q);
		if (isLatest == 1) {
			sb.sort("ActivedDate", SortOrder.DESC);
			sb.sort("CreatedDate", SortOrder.DESC);///H

		}
		if (isMostView == 1) {
			sb.sort("ViewCounter", SortOrder.DESC);
		}
		if (isLatest == 0 && isMostView == 0) {
			sb.sort("ActivedDate", SortOrder.DESC);
			sb.sort("CreatedDate", SortOrder.DESC);////H
		}
		var searchRequest = new SearchRequest(CurrentGameAppNewsIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {
			// System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), NewsSO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();

			// System.out.println("idlist: " + idlist.length);

			news.result = oclient.QueryFunction("gameapp_GetNewsByListNewsID", NewsBO[].class, false, idlist, siteID,
					"vi-VN");
			news.total = (int) queryResults.getHits().getTotalHits().value;
			news.message = "ID: " + Arrays.toString(idlist);

		} catch (Exception e) {
			Logs.LogException(e);
		}

		return news;
	}

	public NewsSR GetNewsRelatedByNewsID(NewsGameAppFilter filter) throws Throwable {
		// Tin tức liên quan(trang news)
		if (filter.PageSize < 0)
			filter.PageSize = 10;
		if (filter.PageSize > 50)
			filter.PageSize = 50;
		if (filter.PageSize * filter.PageIndex > 10000)
			filter.PageIndex = 0;

		// rule mới ngày 23/09/2020
		if (filter.ListGameApp.length == 0) {
			return null;
		}

		NewsSR gameAppNews = new NewsSR();
		// filter.
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("IsActived", 1));
		q.must(rangeQuery("ActivedDate").lte(new Date()));
		q.mustNot(termQuery("NewsID", filter.NewsID));

		q.must(termsQuery("ListTreeCategory", "g2002g")); // 2002 là parent news của gameapp

		/* chia case theo rule moi 11/05/2020 - VanHanh */
//		if(filter.ListGameApp == null || filter.ListGameApp.length == 0) // Trường hợp 1 - bài tin không thuộc game/app liên quan
//		{
//			q.mustNot(existsQuery("ProductIDList"));
//			// lấy những bài tin không thuốc gì cả.
//		}else if(filter.ListGameApp != null && filter.ListGameApp.length == 1) {
//			
//			q.must(termsQuery("ProductIDList", filter.ListGameApp));
//			q.must(termQuery("CountProductIDList", 1));
//			//
//		}else{
//			
//
//			q.must(termsQuery("ProductIDList", filter.ListGameApp));
//		}
		// q.must( boolQuery(Script(scriptFunction(""))) );

		// cập nhật rule mới ngày 23/09/2020
		if (filter.ListGameApp.length == 1) {
			q.must(termsQuery("ProductIDList", filter.ListGameApp));
			q.must(rangeQuery("CountProductIDList").gte(2));
		} else if (filter.ListGameApp.length > 1) {
			q.must(termsQuery("ProductIDList", filter.ListGameApp));
		}

		if (filter.ListCategory.length > 0) {
			q.must(termsQuery("ProductCategoryIDList", filter.ListCategory));

		}

		q.must(termQuery("SiteID", filter.SiteID));
		sb.from(filter.PageIndex * filter.PageSize).size(filter.PageSize).query(q);

		sb.sort("ActivedDate", SortOrder.DESC);

		var searchRequest = new SearchRequest(CurrentGameAppNewsIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {
			// System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), NewsBO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();

			// System.out.println("idlist: " + idlist.length);

			gameAppNews.result = oclient.QueryFunction("gameapp_GetNewsByListNewsID", NewsBO[].class, false, idlist,
					filter.SiteID, "vi-VN");
			gameAppNews.total = (int) queryResults.getHits().getTotalHits().value;
			gameAppNews.message = "";

		} catch (Exception e) {

			Logs.LogException(e);
		}

		return gameAppNews;
	}

	public NewsSR GetNewsTopView7DayByPlatFormOrCategory(int platformID, int categoryID, int pageIndex, int pageSize,
			int siteID) throws Throwable {

		if (pageSize < 0)
			pageSize = 10;
		if (pageSize > 50)
			pageSize = 50;
		if (pageSize * pageIndex > 10000)
			pageIndex = 0;
		NewsSR news = new NewsSR();
		// filter.
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("IsActived", 1));
		q.must(rangeQuery("ActivedDate").lte(new Date()));

		q.must(termQuery("SiteID", siteID));
		q.must(rangeQuery("ActivedDate").lte(new Date()));

		// lay bai tin game?
		q.must(termsQuery("ListTreeCategory", "g2002g"));

		if (platformID > 0) {
			q.must(termsQuery("PlatformIDList", Integer.toString(platformID)));
		}
		if (categoryID > 0) {
			q.must(termsQuery("ProductCategoryIDList", Integer.toString(categoryID)));
		}

		sb.from(pageIndex * pageSize).size(pageSize).query(q);

		sb.sort("ViewCounter7Days", SortOrder.DESC);

		var searchRequest = new SearchRequest(CurrentGameAppNewsIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {
			// System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), NewsSO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();

			news.result = oclient.QueryFunction("gameapp_GetNewsByListNewsID", NewsBO[].class, false, idlist, siteID,
					"vi-VN");
			news.total = (int) queryResults.getHits().getTotalHits().value;
			news.message = "ID: " + Arrays.toString(idlist);

		} catch (Exception e) {

			Logs.LogException(e);
		}

		return news;

	}

	public NewsSR GetGameAppNewsByDate(int platformID, int IsGame, int CategoryId, Date FromDate, Date ToDate,
			int pageSize, int pageIndex) throws Throwable {
		if (pageSize < 0)
			pageSize = 10;
		if (pageSize > 200)
			pageSize = 200;
		if (pageSize * pageIndex > 10000)
			pageIndex = 0;

		NewsSR newsSR = new NewsSR();
		var sb = new SearchSourceBuilder();

		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("IsActived", 1));
		q.must(rangeQuery("ActivedDate").lte(new Date()));
		q.must(termsQuery("ListTreeCategory", "g2002g")); // 2002 là parent news của gameapp

		q.must(rangeQuery("ActivedDate").gte(FromDate));
		q.must(rangeQuery("ActivedDate").lte(ToDate));

		if (CategoryId > 0) {
			q.must(termQuery("CategoryID", CategoryId));
		}
		if (platformID > 0) {
			q.must(termsQuery("PlatformIDList", Integer.toString(platformID)));
		}
		if (IsGame >= 0) {
			q.must(termQuery("IsGame", IsGame));
		}

		sb.from(pageIndex * pageSize).size(pageSize).query(q);

		var searchRequest = new SearchRequest(CurrentGameAppNewsIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), NewsSO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();
			if (idlist != null && idlist.length > 0) {
				newsSR.result = oclient.QueryFunction("news_GetByIDList", NewsBO[].class, false, idlist);
				newsSR.total = (int) queryResults.getHits().getTotalHits().value;
				newsSR.message = "";
			}
			return newsSR;

		} catch (Exception e) {

			Logs.LogException(e);
			return null;
		}

	}

	public NewsSR GetListNewsByCustomerID(String customerID, int pageIndex, int pageSize) throws Throwable{
		
		if (pageSize < 0)
			pageSize = 10;
		if (pageSize > 50)
			pageSize = 50;
		if (pageSize * pageIndex > 10000)
			pageIndex = 0;
		NewsSR gameAppNews = new NewsSR();
		// filter.
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("SiteID", 1));
		q.must(termQuery("IsActived", 1));
		q.must(termsQuery("ListTreeCategory", "g2002g")); // g2002g là parent news của gameapp
		q.must(rangeQuery("ActivedDate").lte(new Date()));
		
		q.must(termQuery("CreatedCustomerID", customerID));

		sb.from(pageIndex * pageSize).size(pageSize).query(q);

		sb.sort("ActivedDate", SortOrder.DESC);
		
		var searchRequest = new SearchRequest(CurrentGameAppNewsIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {
			// System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), NewsBO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();

			// System.out.println("idlist: " + idlist.length);

			gameAppNews.result = oclient.QueryFunction("gameapp_GetNewsByListNewsID", NewsBO[].class, false, idlist,
					1, "vi-VN");
			gameAppNews.total = (int) queryResults.getHits().getTotalHits().value;
			gameAppNews.message = "sortType:  DESC, SORT BY ActivedDate, CreatedCustomerID = " + customerID;

		} catch (Exception e) {

			Logs.LogException(e);
		}

		return gameAppNews;
	}
	
}
