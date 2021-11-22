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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery.ScoreMode;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder.FilterFunctionBuilder;
import org.elasticsearch.index.search.MultiMatchQuery;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.sort.ScriptSortBuilder.ScriptSortType;

import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import mwg.wb.business.NewsHelper;
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.elasticsearch.dataquery.NewsEventQuery;
import mwg.wb.client.elasticsearch.dataquery.NewsQuery;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.redis.RedisClient;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.news.NewsCount;
import mwg.wb.model.news.NewsSR;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.search.NewsEventSO;
import mwg.wb.model.search.NewsSO;
import mwg.wb.model.searchresult.FaceObject;
import mwg.wb.model.searchresult.NewsBOSR;
import mwg.wb.model.searchresult.NewsSOSR;

public class DMXNewsHelper extends NewsHelper {

	private static final String CurrentIndexDB = "ms_news";
	private static final String EventCurrentIndexDB = "ms_newsevent";
	ClientConfig config = null;
	private RedisClient redisClient = null;
	
	public DMXNewsHelper(ORThreadLocal afactoryRead, ClientConfig aconfig) {
		super(afactoryRead, aconfig);
		config = aconfig;
		redisClient = RedisClient.getInstance(config.REDIS_HOST_NLU);
	}

	@Override
	public NewsSOSR GetRelatedNewsByProductInfo(ProductBO productInfo, int newsCount, int siteID, CodeTimer queryTimer,
			CodeTimer parseTimer) {
		NewsSOSR result = new NewsSOSR();
		SearchSourceBuilder sb1 = new SearchSourceBuilder();
		SearchSourceBuilder sb2 = new SearchSourceBuilder();

		var query = boolQuery();
		var listNewsSO = new LinkedHashMap<Integer, NewsSO>();

		LocalDateTime localDateTime = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		
			localDateTime = localDateTime.plusHours(7);

		

		var toDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

		
		String keyword = Utils.StringIsEmpty(productInfo.ProductLanguageBO.KeyWord)
				? productInfo.ProductLanguageBO.MetaKeyWord
				: productInfo.ProductLanguageBO.KeyWord;
		if (Utils.StringIsEmpty(keyword)) {
			keyword = "";
		}

		String catename = productInfo.ProductCategoryBO != null ? productInfo.ProductCategoryBO.CategoryName : "";

		query.mustNot(termQuery("ListTreeCategory", "g1061"));// bai tin km
		query.must(rangeQuery("ActivedDate").lte(toDate));
		query.must(termQuery("SiteID", siteID));

		String[] arrMultipleKeywords = StringUtils.strip(keyword.toLowerCase(), ",").split(",");
		var keywordQuery = boolQuery();

		for (var item : arrMultipleKeywords) {
			if (Utils.StringIsEmpty(item))
				continue;

			String keyword_vn = item;
			try {
				keyword_vn = DidxHelper.FormatKeywordSearchField(item);
			} catch (Exception e) {

				e.printStackTrace();
			}

//			QueryBuilder queryBuilder = QueryBuilders
//					.boostingQuery(
//							QueryBuilders.multiMatchQuery(keyword_vn, "SETIitleVN")
//									.operator(Operator.AND),
//							QueryBuilders.matchAllQuery())
//					.negativeBoost(0.2f);

			QueryBuilder _queryBuilder = QueryBuilders.multiMatchQuery(keyword_vn).field("SETIitleVN", 8)
					.field("SETIitle", 3).boost(5.0f).operator(Operator.AND);
			keywordQuery.should(_queryBuilder);
		}
		query.must(keywordQuery);

		sb1.from(0).size(newsCount);
		sb1.query(query);

		SearchRequest searchRequest = new SearchRequest(CurrentIndexDB);
		searchRequest.source(sb1);

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
		
		
		 

		if (queryResults == null)
			return result;
		var finalResult = queryResults.getHits();

		finalResult.forEach(h -> {
			try {

				var so = mapper.readValue(h.getSourceAsString(), NewsSO.class);
				listNewsSO.put(so.NewsID, so);
			} catch (Exception e) {
				Logs.LogException(e);
			}
		});

		if (listNewsSO.size() < newsCount && !Utils.StringIsEmpty(catename)) {// lay them theo cate va sort theo ngay
																				// active
			var newquery = boolQuery();
			newquery.mustNot(termQuery("ListTreeCategory", "g1061"));// bai tin km
			newquery.must(rangeQuery("ActivedDate").lte(toDate));

			newquery.mustNot(termsQuery("NewsID", listNewsSO.keySet().toArray()));
			newquery.must(termQuery("SiteID", siteID));
			newquery.must(boostingQuery(queryStringQuery(catename.toLowerCase()).field("SETIitleVN").boost(5f)
					.defaultOperator(Operator.AND), QueryBuilders.matchAllQuery()).negativeBoost(0.1f));

			sb2.sort("ActivedDate", SortOrder.DESC);
			sb2.from(0).size(newsCount - listNewsSO.size());
			sb2.query(newquery);

			searchRequest = new SearchRequest(CurrentIndexDB);
			searchRequest.source(sb2);

			
		//	var elasticClient2 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST);
			//var clientIndex2 = elasticClient2.getClient();
			try {

				queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
			} catch (Throwable e) {

				Logs.LogException(e);
				 
			} finally {
				//elasticClient2.close();
			}
			
			 

			finalResult = queryResults.getHits();

			finalResult.forEach(h -> {
				try {

					var so = mapper.readValue(h.getSourceAsString(), NewsSO.class);
					listNewsSO.put(so.NewsID, so);
				} catch (Exception e) {
					Logs.LogException(e);
				}
			});

		}

		result = new NewsSOSR();
		result.newsSOList = listNewsSO;
		result.total = listNewsSO.size();

		return result;
	}
	
	
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

		if (newsQuery.CategoryId == 1123 || newsQuery.CategoryId == 1992
				||newsQuery.CategoryId == 2009)// bai tin mxh, có the lay nhung bai khong active
		{
			
				query.must(termQuery("IsActived", newsQuery.IsActive));

			
		} else {
			query.must(termQuery("IsActived", 1));

		}

		// a Thai yêu cau bo cate 1744 => cac bai tin thuong hieu bhx
		query.mustNot(termQuery("ListTreeCategory", "g1744g"));
		query.mustNot(termQuery("ListTreeCategory", "g1264g"));
		query.mustNot(termQuery("ListTreeCategory", "g2002g"));// bai tin gameapp

		if (newsQuery.ExtensionObject > 0) {
			query.must(termQuery("NewsID", newsQuery.ExtensionObject));

		} else {
			if (newsQuery.ExtensionObject == -1)// bai tin moi trong 7 ngay, view >10k, comment >10
			{
				var filter = boolQuery();
				filter.should(rangeQuery("CommentCount").gte(10));
				filter.should(rangeQuery("ViewCounter").gte(10000));
				query.must(filter);

				query.must(rangeQuery("ActivedDate")
						.gte(Date.from(localDateTime.plusHours(7).minusDays(7).atZone(ZoneId.systemDefault()).toInstant())));
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
					String[] arrMultipleKeywords = newsQuery.Keyword.split("|\\s");
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

		
//		var elasticClient1 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
 	try {

			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		} catch (Throwable e) {

			Logs.LogException(e);
			throw e;
		} finally {
			//elasticClient1.close();
		}
		
		
		
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
	public NewsBOSR GetNewsVideoByCategoryID(int categoryID, int siteID, int pageIndex, int pageSize) throws Throwable {
			
		if (pageSize < 0)
			pageSize = 10;
		if (pageSize > 50)
			pageSize = 50;
		if (pageSize * pageIndex > 10000)
			pageIndex = 0;
		NewsBOSR gameAppNews = new NewsBOSR();
		String rsl = "";
		String rsltitle = "";

		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("IsActived", 1));
		q.must(rangeQuery("ActivedDate").lte(new Date()));
		q.must(termQuery("SiteID", siteID));
		
		if(categoryID == 9999) {
			q.must(termQuery("PostType", 2));
		}else {
			q.must(termQuery("IsNewsVideo", 1));// bài tin video
			if (categoryID > 0) {
				q.must(termQuery("ListTreeCategory", "g" + categoryID + "g"));
			}
		}
		

		// a Thai yêu cau bo cate 1744 => cac bai tin thuong hieu bhx
		q.mustNot(termQuery("ListTreeCategory", "g1744g"));
		q.mustNot(termQuery("ListTreeCategory", "g1264g"));
		q.mustNot(termQuery("ListTreeCategory", "g2002g"));// bai tin gameapp
		
		// q.mustNot(termsQuery("ListTreeCategory", new String[] { "g1053g","g1222g","g1226g","g1060g"} ));
		

		

		sb.from(pageIndex * pageSize).size(pageSize).query(q);

		sb.sort("ActivedDate", SortOrder.DESC);

		var searchRequest = new SearchRequest(CurrentIndexDB);
		searchRequest.source(sb);
		
		
		 
		
		
		 SearchResponse queryResults=null;
		//var elasticClient1 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST);
		//var clientIndex1 = elasticClient1.getClient();
		try {

			  queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);


		} catch (Throwable e) {

			Logs.LogException(e);
			throw e;
		} finally {
			//elasticClient1.close();
		}
		
		
		try {
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), NewsBO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();


			gameAppNews.result = GetListNewsByListID(Arrays.stream(idlist).boxed().collect(Collectors.toList()));
			gameAppNews.total = (int) queryResults.getHits().getTotalHits().value;
			gameAppNews.message = "";

		} catch (Exception e) {

			Logs.LogException(e);
		}
		return gameAppNews;
	}
	
}