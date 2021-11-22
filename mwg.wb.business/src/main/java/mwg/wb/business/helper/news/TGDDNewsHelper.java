package mwg.wb.business.helper.news;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.boostingQuery;
import static org.elasticsearch.index.query.QueryBuilders.functionScoreQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.scriptFunction;
import static org.elasticsearch.search.aggregations.AggregationBuilders.count;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;
import static org.elasticsearch.search.sort.SortBuilders.scriptSort;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery.ScoreMode;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder.FilterFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder.ScriptSortType;
import org.elasticsearch.search.sort.SortOrder;

import com.sun.xml.fastinfoset.stax.events.Util;

import mwg.wb.business.NewsHelper;
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.elasticsearch.dataquery.NewsEventQuery;
import mwg.wb.client.elasticsearch.dataquery.NewsQuery;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.search.NewsEventSO;
import mwg.wb.model.search.NewsSO;
import mwg.wb.model.searchresult.FaceObject;
import mwg.wb.model.searchresult.NewsSOSR;

public class TGDDNewsHelper extends NewsHelper {

	private static final String CurrentIndexDB = "ms_news";
	private static final String EventCurrentIndexDB = "ms_newsevent";
	private static List<String> FaqCate = Arrays.asList("g1053g", "g1222g", "g1226g", "g1060g", "g1054g", "g1708g");
	ClientConfig config = null;
	public TGDDNewsHelper(ORThreadLocal afactoryRead, ClientConfig aconfig) {
		super(afactoryRead, aconfig);
		config=aconfig;
	}

	@Override
	public NewsSOSR Ela_SearchNews2020(NewsQuery newsQuery, CodeTimer queryTimer, CodeTimer parseTimer)
			throws Throwable {
		
		
		
		NewsSOSR result = new NewsSOSR();

		SearchSourceBuilder sb = new SearchSourceBuilder();
//			sb.fetchSource(new String[] { "_score", "NewsID", "ListTreeCategory", "TermEventsID", "TermTopicID" },
//					null);

		var query = boolQuery();

		LocalDateTime localDateTime = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		query.must(rangeQuery("ActivedDate")
				.lte(Date.from(localDateTime.plusHours(7).atZone(ZoneId.systemDefault()).toInstant())));
		query.must(termQuery("IsDeleted", 0));

		if (newsQuery.CategoryId == 1123)// bai tin mxh
		{
			if (newsQuery.IsActive > 0) {
				query.must(termQuery("IsActived", newsQuery.IsActive));

			}
		} else {
			query.must(termQuery("IsActived", 1));

		}

		// a Thai yêu cau bo cate 1744 => cac bai tin thuong hieu bhx
		query.mustNot(termQuery("ListTreeCategory", "g1744g"));
		query.mustNot(termQuery("ListTreeCategory", "g1264g"));
		query.mustNot(termQuery("ListTreeCategory", "g2002g"));// bai tin gameapp
		// loai bai faq

		query.mustNot(termsQuery("ListTreeCategory", FaqCate.toArray()));

		if (newsQuery.ExtensionObject > 0) {
			query.must(termQuery("NewsID", newsQuery.ExtensionObject));

		} else {
			if (newsQuery.ExtensionObject == -1)// bai tin moi trong 7 ngay, view >10k, comment >10
			{
				var filter = boolQuery();
				filter.should(rangeQuery("CommentCount").gte(10));
				filter.should(rangeQuery("ViewCounter").gte(10000));
				query.must(filter);

				query.must(rangeQuery("ActivedDate").gte(
						Date.from(localDateTime.plusHours(7).minusDays(7).atZone(ZoneId.systemDefault()).toInstant())));
			}
		}
		if (newsQuery.UserID > 0) {
			query.must(termQuery("UserID", newsQuery.UserID));

			if (newsQuery.CategoryId != 1708) {
				query.mustNot(termQuery("ListTreeCategory", "g1708g"));
			}
		}
		if (newsQuery.StoreID > 0) {
			query.must(termQuery("StoreID", newsQuery.StoreID));

		}
		if (newsQuery.CustomerID > 0) {
			query.must(termQuery("CustomerID", newsQuery.CustomerID));

		}
		if (newsQuery.CategoryId > 0) {
			if (newsQuery.CategoryId == 1733) {
				if (newsQuery.StoreID == 0) {
					query.mustNot(termQuery("ListTreeCategory", "g1264g"));

				}
			}
			query.must(termQuery("ListTreeCategory", "g" + newsQuery.CategoryId + "g"));

		}
		if (newsQuery.CateIds != null && newsQuery.CateIds.length > 0) {

			var lstCate = new ArrayList<String>();
			for (var item : newsQuery.CateIds) {
				lstCate.add("g" + item + "g");
			}

			query.must(termsQuery("ListTreeCategory", lstCate.toArray()));

		}
		if (newsQuery.CategoryId == -31) {
			query.must(termQuery("ListTreeCategory", "g31g"));
			query.mustNot(termQuery("ListTreeCategory", "g1716g"));

		}
		if (newsQuery.HotTopicId > 0) {
			query.must(termQuery("TermTopicID", String.valueOf(newsQuery.HotTopicId)));

		}

		if (newsQuery.RelatedNewsId > 0) {
			query.must(termQuery("RelatedNews", "g" + newsQuery.RelatedNewsId + "g"));

		}
		if (newsQuery.IsViewMore == true) {
			query.must(rangeQuery("UpdatedDate").lte(newsQuery.FromDate));

		}

		if (newsQuery.CategoryId != 1822 && newsQuery.CategoryId != 1849) {
			if (newsQuery.FromDate != null) {
				long diffInMillies = Math.abs(new Date().getTime() - newsQuery.FromDate.getTime());
				long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

				if (newsQuery.FromDate != null && diff < 10) {
					query.must(rangeQuery("ActivedDate").gte(newsQuery.FromDate));

				}
			}

		}
		if (!Utils.StringIsEmpty(newsQuery.EventsIDList)) {
			query.must(termsQuery("TermEventsID", newsQuery.EventsIDList.split(",")));

		}
		if (!Utils.StringIsEmpty(newsQuery.ProductIDList)) {
			query.must(termsQuery("ProductIDList", newsQuery.ProductIDList.split(",")));

		}

		SearchResponse queryResults = null;
		SearchRequest searchRequest = new SearchRequest(CurrentIndexDB);
		String rsl = "";
		// Thanh Phi 0401
		if (!Utils.StringIsEmpty(newsQuery.Keyword)) {
			if (newsQuery.CategoryId == 1708) {
				String keyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(newsQuery.Keyword));

				query.must(boostingQuery(
						queryStringQuery(keyword).field("Keyword").boost(5f).defaultOperator(Operator.AND),
						QueryBuilders.matchAllQuery()).negativeBoost(0.2f));

			} else if (newsQuery.CategoryId == 1733 || newsQuery.CategoryId == 1752) {

				query.mustNot(termQuery("ListTreeCategory", "g1744g"));

				if (newsQuery.Keyword.contains("|")) {
					String[] arrMultipleKeywords = newsQuery.Keyword.split("\\s+");
					var keywordQuery = boolQuery();

					for (var item : arrMultipleKeywords) {
						String keyword_vn = DidxHelper.FormatKeywordSearchField(item);

						QueryBuilder queryBuilder = QueryBuilders.boostingQuery(
								QueryBuilders.multiMatchQuery(keyword_vn, "SETIitleVN^8", "SETag^3", "SEContent^2")
										.operator(Operator.AND),
								QueryBuilders.matchAllQuery()).negativeBoost(0.2f);

						keywordQuery.should(queryBuilder);

					}
					for (var item : arrMultipleKeywords) {
						String keyword_en = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(item));
						QueryBuilder queryBuilder = QueryBuilders.boostingQuery(
								QueryBuilders.multiMatchQuery(keyword_en, "SETIitle^8", "SETag^3", "SEContent^2")
										.operator(Operator.AND),
								QueryBuilders.matchAllQuery()).negativeBoost(0.2f);

						keywordQuery.should(queryBuilder);

					}
					query.must(keywordQuery);

				} else {
					var keywordQuery = boolQuery();
					String keyword_vn = DidxHelper.FormatKeywordSearchField((newsQuery.Keyword));
					String keyword_en = DidxHelper
							.FormatKeywordSearchField(DidxHelper.FilterVietkey(newsQuery.Keyword));

					QueryBuilder queryBuildervn = QueryBuilders.boostingQuery(
							QueryBuilders.multiMatchQuery(keyword_vn, "SETIitleVN^8", "SETag^3", "SEContent^2")
									.operator(Operator.AND),
							QueryBuilders.matchAllQuery()).negativeBoost(0.2f);

					keywordQuery.should(queryBuildervn);

					QueryBuilder queryBuilderen = QueryBuilders.boostingQuery(QueryBuilders
							.multiMatchQuery(keyword_en, "SETIitle^8", "SETag^3", "SEContent^2").operator(Operator.AND),
							QueryBuilders.matchAllQuery()).negativeBoost(0.2f);
					keywordQuery.should(queryBuilderen);

					query.must(keywordQuery);

				}

			} else if (newsQuery.CategoryId == 1822 || newsQuery.CategoryId == 1849) {

				String keyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(newsQuery.Keyword));
				query.must(boostingQuery(
						queryStringQuery(keyword).field("Keyword").boost(5f).defaultOperator(Operator.AND),
						QueryBuilders.matchAllQuery()).negativeBoost(0.2f));

			} else {
				String keyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(newsQuery.Keyword));
				query.must(boostingQuery(
						queryStringQuery(keyword).field("Keyword").boost(5f).defaultOperator(Operator.AND),
						QueryBuilders.matchAllQuery()).negativeBoost(0.2f));

				String[] arrCate = { "g1169g", "g1141g", "g31g", "g196g", "g210g", "g1036g", "g94g", "g1104g", "g1110g",
						"g1112g", "g1111g" };
				query.must(termsQuery("ListTreeCategory", arrCate));

			}
		}

		if (!Utils.StringIsEmpty(newsQuery.Tag)) {
			query.must(termQuery("TagTerm", DidxHelper.GenTerm3(newsQuery.Tag)));

		}

		if (newsQuery.OrderType == 1) {

			sb.sort(scriptSort(new Script(
					"doc.containsKey('activedDate')?(Math.ceil((Math.abs(new Date(doc['activedDate'].value).getTime() - new Date().getTime())) / (1000 * 3600 * 24 * 3))):999999999"),
					ScriptSortType.NUMBER).order(SortOrder.ASC))
					.sort(scriptSort(new Script(
							" doc.containsKey('commentCount')?(doc['commentCount'].value + doc['likeCounter'].value - doc['disLikeCount'].value):0"),
							ScriptSortType.NUMBER).order(SortOrder.DESC));

		} else {
			if (newsQuery.CategoryId == 1733 || newsQuery.CategoryId == 1752) {
				query.mustNot(termQuery("ListTreeCategory", "g1744g"));

			}

			switch (newsQuery.OrderType) {
			case 2:
				sb.sort("ActivedDate", SortOrder.DESC);
				break;
			case 3:
				sb.sort("ViewCounter", SortOrder.DESC);
				break;
			case 4:
				sb.sort("_score", SortOrder.DESC);
				break;

			default:
				sb.sort("ActivedDate", SortOrder.DESC);
				break;
			}
		}
		sb.from(newsQuery.PageIndex * newsQuery.PageSize).size(newsQuery.PageSize);
		sb.query(query);
		sb.aggregation(terms("FacetTermListTreeCategory").field("ListTreeCategory").size(5000)
				.subAggregation(count("Sum").field("NewsID")));

		sb.aggregation(
				terms("FacetTermTopicID").field("TermTopicID").size(5000).subAggregation(count("Sum").field("NewsID")));

		searchRequest.source(sb);
		if (queryTimer != null)
			queryTimer.reset();

//		var	elasticClient1 = new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST);
//		var	clientIndex1 = elasticClient1.getClient();
//		try {
			 
			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
//			
//			} catch (Throwable e) {
//
//				Logs.LogException(e);
//				throw e ;
//			}finally {
//				elasticClient1.close();
//			}
		
		if (queryTimer != null)
			queryTimer.end();

		var listNewsSO = new LinkedHashMap<Integer, NewsSO>();
		List<FaceObject> faceList = new ArrayList<FaceObject>();

		if (parseTimer != null)
			parseTimer.reset();

		var finalResult = queryResults.getHits();
		finalResult.forEach(h -> {
			try {

				var so = mapper.readValue(h.getSourceAsString(), NewsSO.class);
				listNewsSO.put(so.NewsID, so);
			} catch (Exception e) {
				Logs.LogException(e);
			}
		});
		if (parseTimer != null)
			parseTimer.end();
		int rowcount = (int) queryResults.getHits().getTotalHits().value;
		var _aggrs = queryResults.getAggregations();
		Map<String, Aggregation> aggrs = null;
		if (_aggrs != null) {
			aggrs = _aggrs.asMap();
		}

		if (aggrs != null) {

			ParsedStringTerms treeCateBucket = (ParsedStringTerms) aggrs.get("FacetTermListTreeCategory");
			ParsedStringTerms topicBucket = (ParsedStringTerms) aggrs.get("FacetTermTopicID");

			for (var term : treeCateBucket.getBuckets()) {
				var faceObject = new FaceObject();
				faceObject.Type = 1;
				faceObject.Key = term.getKeyAsString().replace("g", "");
				faceObject.Count = (int) term.getDocCount();
				faceList.add(faceObject);
			}
			for (var term : topicBucket.getBuckets()) {
				var faceObject = new FaceObject();
				faceObject.Type = 2;
				faceObject.Key = term.getKeyAsString().replace("g", "");
				faceObject.Count = (int) term.getDocCount();
				faceList.add(faceObject);
			}

		}

		result = new NewsSOSR();
		result.total = rowcount;
		result.faceList = faceList;
		result.newsSOList = listNewsSO;

		return result;

	}

	@Override
	public List<NewsEventSO> Ela_SearchNewsEvent2020(NewsEventQuery newsEventQuery, CodeTimer queryTimer,
			CodeTimer parseTimer) {

		SearchSourceBuilder sb = new SearchSourceBuilder();
//		sb.fetchSource(new String[] { "_score", "NewsID", "ListTreeCategory", "TermEventsID", "TermTopicID" },
//				null);

		var query = boolQuery();
		if (!Utils.StringIsEmpty(newsEventQuery.keyword)) {
			String keyword = DidxHelper.FormatKeywordField(DidxHelper.FilterVietkey(newsEventQuery.keyword));
			query.must(boostingQuery(queryStringQuery(keyword).field("keyword").boost(5f).defaultOperator(Operator.AND),
					QueryBuilders.matchAllQuery()).negativeBoost(0.2f));
			LocalDateTime localDateTime = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

			localDateTime = localDateTime.plusDays(-7);

			query.must(rangeQuery("toDate").gte(localDateTime));

		}
		if (newsEventQuery.lstEventID != null && newsEventQuery.lstEventID.size() > 0) {

			query.must(termsQuery("eventID", newsEventQuery.lstEventID));

		} else {
			if (newsEventQuery.fromDate == null && newsEventQuery.toDate == null) {
				query.must(rangeQuery("toDate").gte(new Date()));

			}
			if (newsEventQuery.fromDate != null) {
				query.must(rangeQuery("toDate").gte(new Date()));

			}
			if (newsEventQuery.toDate != null) {
				query.must(rangeQuery("toDate").lte(new Date()));

			}
		}

		if (newsEventQuery.isFollow == 1) {
			query.must(termQuery("isFollow", 1));

		} else if (newsEventQuery.isFollow == -1) {
			query.must(termQuery("isFollow", 0));

		}
		if (newsEventQuery.parentEventID > 0) {
			query.must(termQuery("parentEventID", newsEventQuery.parentEventID));

		}

		sb.from(newsEventQuery.pageIndex * newsEventQuery.pageSize).size(newsEventQuery.pageSize);
		sb.query(query);
		if (newsEventQuery.orderType == 1) {
			if (newsEventQuery.toDate != null) {
				sb.sort("toDate", SortOrder.DESC);
			}
			sb.sort("fromDate", SortOrder.DESC);

		} else if (newsEventQuery.toDate != null) {
			sb.sort("toDate", SortOrder.DESC);
		} else {
			sb.sort("fromDate", SortOrder.ASC);
		}

		SearchRequest searchRequest = new SearchRequest(EventCurrentIndexDB);
		searchRequest.source(sb);
		if (queryTimer != null)
			queryTimer.reset();

		SearchResponse queryResults = null;
		
		
//		var elasticClient1 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
 		try {

			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

 	} catch (Throwable e) {
//
 			Logs.LogException(e);
//			 
 		} finally {
//			elasticClient1.close();
 		}
		
		
		 
		if (queryTimer != null)
			queryTimer.end();
		if (queryResults == null)
			return new ArrayList<NewsEventSO>();

		var listNewsEventSO = new ArrayList<NewsEventSO>();
		List<FaceObject> faceList = new ArrayList<FaceObject>();

		if (parseTimer != null)
			parseTimer.reset();

		var finalResult = queryResults.getHits();
		finalResult.forEach(h -> {
			try {

				var so = mapper.readValue(h.getSourceAsString(), NewsEventSO.class);
				listNewsEventSO.add(so);
			} catch (Exception e) {
				Logs.LogException(e);
			}
		});
		if (parseTimer != null)
			parseTimer.end();

		return listNewsEventSO;
	}

	@Override
	public NewsSOSR ElaSearchRelateNews(NewsBO news, int pageIndex, int pageSize, int rootNewsID, int siteID,
			CodeTimer queryTimer, CodeTimer parseTimer) {
		NewsSOSR result = new NewsSOSR();
		SearchSourceBuilder sb = new SearchSourceBuilder();

		var query = boolQuery();

		query.mustNot(termQuery("NewsID", news.NewsID));
		query.must(rangeQuery("ActivedDate").lte(new Date()));

		if (rootNewsID == 0) {
			String[] arrCate = { "g1169g", "g1141g", "g31g", "g196g", "g210g", "g1036g", "g94g", "g1104g", "g1110g",
					"g1112g", "g1111g" };

			query.must(termsQuery("ListTreeCategory", arrCate));
		} else {
			int catid = 0;
			String[] arrCate = { "g" + rootNewsID + "g" };
			String[] lstcate = news.ListCategoryID.split("\\*");
			if (lstcate.length > 0) {
				try {
					catid = Integer.valueOf((lstcate[0].trim()));

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			query.must(termsQuery("ListTreeCategory", arrCate));

			if (catid > 0) {
				var scripts = new FilterFunctionBuilder[] { new FilterFunctionBuilder(
						termQuery("ListTreeCategory", "g" + catid + "g"), scriptFunction(" 1000 ")), };
				query.must(functionScoreQuery(matchAllQuery(), scripts).scoreMode(ScoreMode.SUM)
						.boostMode(CombineFunction.SUM));

			}

		}
		if (!Utils.StringIsEmpty(news.KeyWord)) {
			String[] term = DidxHelper.ConvertToTagsTerm(news.KeyWord).split("\\s+");

			query.must(termsQuery("KeywordTerm", term));
		}

		sb.from(pageIndex * pageSize).size(pageSize);
		sb.query(query);

		if (rootNewsID == 0)

			sb.sort("ActivedDate", SortOrder.DESC);

		SearchRequest searchRequest = new SearchRequest(CurrentIndexDB);
		searchRequest.source(sb);

		if (queryTimer != null)
			queryTimer.reset();

		SearchResponse queryResults = null;
		try {
			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
		if (queryTimer != null)
			queryTimer.end();
		if (queryResults == null)
			return result;
		var finalResult = queryResults.getHits();
		var listNewsSO = new LinkedHashMap<Integer, NewsSO>();
		finalResult.forEach(h -> {
			try {

				var so = mapper.readValue(h.getSourceAsString(), NewsSO.class);
				listNewsSO.put(so.NewsID, so);
			} catch (Exception e) {
				Logs.LogException(e);
			}
		});
		if (parseTimer != null)
			parseTimer.end();
		int rowcount = (int) queryResults.getHits().getTotalHits().value;

		result = new NewsSOSR();
		result.newsSOList = listNewsSO;
		result.total = rowcount;

		return result;
	}

	@Override
	public NewsSOSR GetNewsListByCommentCount(int categoryID, int pageSize, int pageIndex, int siteID,
			CodeTimer queryTimer, CodeTimer parseTimer) {
		NewsSOSR result = new NewsSOSR();
		SearchSourceBuilder sb = new SearchSourceBuilder();

		var query = boolQuery();

		if (categoryID > 0) {
			query.must(termQuery("ListTreeCategory", "g" + categoryID + "g"));

		}
		if (categoryID == 999) {
			query.mustNot(termQuery("ListTreeCategory", "g31g"));
			//1053,1222,1226,1060 loại bài tin faq
			query.mustNot(termsQuery("ListTreeCategory", new String[] { "g1053g", "g1222g", "g1226g", "g1060g" }));

		}

		LocalDateTime localDateTime = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		localDateTime = localDateTime.plusDays(-7);

		var fromDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
		query.must(rangeQuery("ActivedDate").gte(fromDate));
		query.must(rangeQuery("ActivedDate").lte(new Date()));

		sb.from(pageIndex * pageSize).size(pageSize);
		sb.query(query);

		sb.sort("CommentCount", SortOrder.DESC);

		SearchRequest searchRequest = new SearchRequest(CurrentIndexDB);
		searchRequest.source(sb);

		if (queryTimer != null)
			queryTimer.reset();

		SearchResponse queryResults = null;
		try {
			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
		if (queryTimer != null)
			queryTimer.end();
		if (queryResults == null)
			return result;
		var finalResult = queryResults.getHits();
		var listNewsSO = new LinkedHashMap<Integer, NewsSO>();
		finalResult.forEach(h -> {
			try {

				var so = mapper.readValue(h.getSourceAsString(), NewsSO.class);
				listNewsSO.put(so.NewsID, so);
			} catch (Exception e) {
				Logs.LogException(e);
			}
		});
		if (parseTimer != null)
			parseTimer.end();
		int rowcount = (int) queryResults.getHits().getTotalHits().value;

		result = new NewsSOSR();
		result.newsSOList = listNewsSO;
		result.total = rowcount;

		return result;

	}

	@Override
	public NewsSOSR Ela_GetRelatedNewsByProductID(ProductBO productBO, int siteID) throws Exception {

		var listNewsSO = new NewsSOSR();

		String sKeyword = productBO.ProductLanguageBO == null ? productBO.KeyWord
				: (productBO.ProductLanguageBO.KeyWord != null ? productBO.KeyWord : "");
		int introw = 0;
		var listNewSO = new ArrayList<NewsSO>();
		if (Util.isEmptyString(sKeyword)) {
			sKeyword = productBO.ProductLanguageBO != null ? productBO.ProductLanguageBO.ProductName : "";

		}

		if (productBO.CategoryID != 42) {
			// sKeyword = productBO.ProductLanguageBO.ProductName + "," +
			// productBO.ProductCategoryBO.CategoryName + " " +
			// productBO.ProductManuBO.ManufacturerName + "," +
			// productBO.ProductCategoryBO.CategoryName;

			var datanews = ElaSearchNews(2, "", new String[] { "g210g", "g1036g", "g1141g" }, 1, productBO.ProductID);
			listNewSO.addAll(datanews);
		}
		if (!Util.isEmptyString(sKeyword)) {
			String[] arrTags = sKeyword.split(",");
			for (String string : arrTags) {
				if (introw < 1) {
					// String tmpStr =
					// DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(string));
					if(Utils.StringIsEmpty(string)) continue;
					var datanews = ElaSearchNews(1, string, new String[] { "g210g", "g1036g", "g1141g" }, 1, 0);
					listNewSO.addAll(datanews);
					listNewSO = (ArrayList<NewsSO>) listNewSO.stream().distinct().collect(Collectors.toList());
					introw += listNewSO.size();
				} else
					break;
			}
			for (String string : arrTags) {
				if (introw < 4) {
					// String tmpStr =
					// DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(string));
					var datanews = ElaSearchNews(10, string, new String[] { "g999g" }, 0, 0);
					listNewSO.addAll(datanews);
					listNewSO = (ArrayList<NewsSO>) listNewSO.stream().distinct().collect(Collectors.toList());
					introw += listNewSO.size();
				}
			}
		}

		var listMapNewsSO = new LinkedHashMap<Integer, NewsSO>();
		for (NewsSO newsSO : listNewSO) {
			listMapNewsSO.put((Integer) newsSO.NewsID, newsSO);
		}

		listNewsSO.newsSOList = listMapNewsSO;
		listNewsSO.total = (int) listNewSO.size();

		return listNewsSO;
	}

	@Override
	public List<NewsSO> ElaSearchNews(int pageSize, String keyword, String[] cateList, int type, int productID)
			throws Exception {
		SearchSourceBuilder sb = new SearchSourceBuilder();

		var query = boolQuery();

		query.must(termQuery("IsDeleted", 0));
		query.must(termQuery("IsActived", 1));
		query.must(termQuery("SiteID", 1));

		String[] arrCate = { "g1169g", "g1141g", "g31g", "g196g", "g210g", "g1036g", "g94g", "g1104g", "g1110g",
				"g1112g", "g1111g" };

		query.must(termsQuery("ListTreeCategory", arrCate));

		if (cateList.length > 0) {
			query.must(termsQuery("ListTreeCategory", cateList));
		}

		query.must(rangeQuery("ActivedDate").lte(new Date()));

		if (productID > 0) {
			query.must(termQuery("ProductIDList", productID));

		} else if (!Utils.StringIsEmpty(keyword)) {
			keyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(keyword));
			if (type == 1) {
				query.must(queryStringQuery(keyword).field("SETIitle").boost(1.0f).defaultOperator(Operator.AND));
			} else {
				query.must(queryStringQuery(keyword).field("Keyword").boost(1.0f).defaultOperator(Operator.AND));
			}
		}

		sb.from(0).size(pageSize);
		sb.query(query);
		sb.sort("ActivedDate", SortOrder.DESC);

		SearchRequest searchRequest = new SearchRequest(CurrentIndexDB);
		searchRequest.source(sb);

		SearchResponse queryResults = null;
		try {
			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e1) {

			e1.printStackTrace();
		}

		if (queryResults == null)
			return new ArrayList<NewsSO>();

		var listNewsEventSO = new NewsSOSR();
		var listNewsSO = new ArrayList<NewsSO>();

		var finalResult = queryResults.getHits();
		finalResult.forEach(h -> {
			try {

				var so = mapper.readValue(h.getSourceAsString(), NewsSO.class);
				listNewsSO.add(so);
			} catch (Exception e) {
				Logs.LogException(e);
			}
		});
		return listNewsSO;
	}

	@Override
	public NewsSOSR GetNewsListByCategoryID(NewsQuery newsQuery, CodeTimer querytimer, CodeTimer parsetimer)
			throws Throwable {

		NewsSOSR result = new NewsSOSR();

		SearchSourceBuilder sb = new SearchSourceBuilder();
//			sb.fetchSource(new String[] { "_score", "NewsID", "ListTreeCategory", "TermEventsID", "TermTopicID" },
//					null);

		var query = boolQuery();

		query.must(termQuery("IsDeleted", 0));
		query.must(termQuery("IsActived", 1));
		query.mustNot(termQuery("ListTreeCategory", "g1708g"));
		query.mustNot(termQuery("ListTreeCategory", "g2002g"));// bai tin gameapp
		
		if (newsQuery.CategoryId > 0) {
			if (newsQuery.CategoryId == 999) {
				query.mustNot(termQuery("ListTreeCategory", "g31g"));
			}
			query.must(termQuery("ListTreeCategory", "g" + newsQuery.CategoryId + "g"));
		}
		query.must(rangeQuery("ActivedDate").lte(new Date() ));
		
		sb.from(newsQuery.PageIndex * newsQuery.PageSize).size(newsQuery.PageSize).query(query)
		.sort("ActivedDate", SortOrder.DESC);
		
		
		SearchRequest searchRequest = new SearchRequest(CurrentIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
		
		String rsl = "";
		

		var listNewsSO = new LinkedHashMap<Integer, NewsSO>();

		

		var finalResult = queryResults.getHits();
		finalResult.forEach(h -> {
			try {

				var so = mapper.readValue(h.getSourceAsString(), NewsSO.class);
				listNewsSO.put(so.NewsID, so);
			} catch (Exception e) {
				Logs.LogException(e);
			}
		});
		
		int rowcount = (int) queryResults.getHits().getTotalHits().value;

		

		result = new NewsSOSR();
		result.total = rowcount;
		result.faceList = null;
		result.newsSOList = listNewsSO;

		return result;

	}
	
}
