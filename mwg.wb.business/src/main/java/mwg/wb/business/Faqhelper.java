package mwg.wb.business;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.boostingQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;
import static org.elasticsearch.search.aggregations.AggregationBuilders.topHits;

import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.internal.InternalSearchResponse;
import org.elasticsearch.search.sort.SortOrder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.elasticsearch.dataquery.NewsEventQuery.Int64Order;
import mwg.wb.client.elasticsearch.dataquery.OrderType;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.faq.FAQStatSO;
import mwg.wb.model.faq.FaqBO;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.search.FaqCategorySO;
import mwg.wb.model.search.FaqSO;
import mwg.wb.model.searchresult.FaqSR;
import mwg.wb.model.system.ObjectSearch;

public class Faqhelper {
	private static ORThreadLocal oclient = null;
	protected ObjectMapper mapper = null;
	ClientConfig config = null;

	protected RestHighLevelClient clientIndex = null;
	private ElasticClient elasticClient = null;
	protected String CurrentFAQIndexDB = "";

	public Faqhelper(ORThreadLocal afactoryRead, ClientConfig aconfig) {
		CurrentFAQIndexDB = "ms_faq"; // aconfig.ELASTICSEARCH_PRODUCT_INDEX;
		oclient = afactoryRead;
		config = aconfig;
		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatStringNews);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		elasticClient = ElasticClient.getInstance(config.SERVER_ELASTICSEARCH_READ_HOST);

		clientIndex = elasticClient.getClient();

	}

	public FaqSR Ela_GetListActiveQuestion(long UserID, int pageIndex, int pageSize, Integer siteID)
			throws Throwable {
		if (pageSize < 0)
			pageSize = 10;
		if (pageSize > 50)
			pageSize = 50;
		if (pageSize * pageIndex > 10000)
			pageIndex = 0;
		FaqSR faq = new FaqSR();
		faq.total = 0;
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("Siteid", siteID));

		sb.from(pageIndex * pageSize).size(pageSize).query(q)
				// .sort(scriptSort(new Script("doc['ActivedDate'].value)"),
				// ScriptSortType.NUMBER).order(SortOrder.ASC))
				.sort("ActivedDate", SortOrder.DESC);

		var searchRequest = new SearchRequest(CurrentFAQIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {

			var faqlist = new ArrayList<FaqBO>();
			System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), FaqSO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();

			System.out.println("idlist: " + idlist.length);

			faq.result = oclient.QueryFunction("faq_GetByIDList", FaqBO[].class, false, idlist);
			faq.total = (int) queryResults.getHits().getTotalHits().value;
			faq.message = "";

		} catch (Exception e) {

			Logs.LogException(e);
		}

		return faq;
	}

	public FaqBO[] GetListQuestionByListQuestionID(String[] listID) throws Throwable {
		// int[] aids = Ints.toArray(listID);
		var listNews = oclient.QueryFunction("faq_GetByIDList", FaqBO[].class, false, listID);
		if (listNews != null)
			return listNews;
		return null;

	}

	public FaqSR Ela_GetListQuestionByTag(long UserID, String Tag, int pageIndex, int pageSize, Integer siteID)
			throws Throwable {
		if (pageSize < 0)
			pageSize = 10;
		if (pageSize > 100)
			pageSize = 100;
		if (pageSize * pageIndex > 10000)
			pageIndex = 0;
		FaqSR faq = new FaqSR();
		faq.total = 0;
		var sb = new SearchSourceBuilder();
		// QueryBuilder qb = queryStringQuery("Email OR @gmail.com
		// @yahoo.com").defaultField("file");
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("Siteid", siteID));
		if (UserID > 0) {
			q.must(termQuery("Userid", UserID));
		}
		// Tag = Tag.replace(",", " OR ");
		q.must(boostingQuery(queryStringQuery(Tag.replace(",", " OR ")).field("Keyword").boost(5f), null)
				.negativeBoost(0.2f));
//			q.must(matchQuery("Tags",Tag));

		sb.from(pageIndex * pageSize).size(pageSize).query(q)
				// .sort(scriptSort(new Script("doc['ActivedDate'].value)"),
				// ScriptSortType.NUMBER).order(SortOrder.ASC))
				.sort("ActivedDate", SortOrder.DESC);

		var searchRequest = new SearchRequest(CurrentFAQIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {

			var faqlist = new ArrayList<FaqBO>();
			System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), FaqSO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();

			System.out.println("idlist: " + idlist.length);

			faq.result = oclient.QueryFunction("faq_GetByIDList", FaqBO[].class, false, idlist);
			faq.total = (int) queryResults.getHits().getTotalHits().value;
			faq.message = "";

		} catch (Exception e) {

			Logs.LogException(e);
		}

		return faq;
	}

	public FaqBO GetFaqCategoryByID(int cateID) throws Throwable {
		var faqcat = oclient.QueryFunction("faq_GetFaqCateByID", FaqBO[].class, false, cateID);
		if (faqcat != null)
			return faqcat[0];
		return null;
	}

	public FaqBO GetQuestion(int faqID) throws Throwable {
		var faqquestion = oclient.QueryFunction("faq_GetByID", FaqBO[].class, false, faqID);
		if (faqquestion != null)
			return faqquestion[0];
		return null;
	}

	public FaqBO[] GetHotQuestion(String[] listCategoryID, int countPerCat, Integer siteID) throws Throwable {

		// var sb = new SearchSourceBuilder();
		SearchSourceBuilder sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0)).must(termQuery("Siteid", siteID))
				.must(termsQuery("ListParentThreadID", listCategoryID))
				.must(rangeQuery("ActivedDate").lte(Utils.GetCurrentDate().getTime())).must(termQuery("IsPrivate", 0));
		q.mustNot(termQuery("ListCategoryID", 1708));

		// q.must(rangeQuery("Listcategoryid"));
		// Listcategoryid != 1708.

		sb.from(0).size(5).query(q).aggregation(terms("listParentThreadID").field("ListParentThreadID").size(100)
				.subAggregation(topHits("topCategoryHits").size(countPerCat).sort("ActivedDate", SortOrder.DESC)));

		var queryResults = clientIndex.search(new SearchRequest(CurrentFAQIndexDB).source(sb), RequestOptions.DEFAULT);
		var a = queryResults.getHits().getHits();
		var aggrs = queryResults.getAggregations();
//		ParsedLongTerms bucket = (ParsedLongTerms) aggrs.get("listParentThreadID");
		var bucket = (ParsedStringTerms) aggrs.get("listParentThreadID");
		if (bucket == null || bucket.getBuckets().size() == 0)
			return null;
		List<Integer> listnewID = new ArrayList<Integer>();

		bucket.getBuckets().stream().map(v -> (TopHits) v.getAggregations().get("topCategoryHits"))
				.forEach(h -> h.getHits().forEach(v -> {
					try {
						var so = mapper.readValue(v.getSourceAsString(), FaqSO.class);
						listnewID.add(so.NewsID);
					} catch (IOException e) {
						Logs.LogException(e);
					}
				}));

		return oclient.QueryFunction("faq_GetByIDList", FaqBO[].class, false, listnewID);
	}

	public FaqCategorySO[] SearchCategory(int categoryID, Integer siteID) throws Throwable {
		var listNews = oclient.QueryFunction("faq_SearchCategory", FaqCategorySO[].class, false, categoryID, siteID);
		if (listNews != null)
			return listNews;
		return null;
	}

	public NewsBO AddQuestion(NewsBO newbo, Integer siteID) {
		return null;
	}

	public FaqBO[] GetFAQFeature(Integer siteID) throws Throwable {
		// TODO Auto-generated method stub
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("Siteid", siteID));
		q.must(termQuery("IsFeature", 1));

		sb.from(0).size(5).query(q)
				// .sort(scriptSort(new Script("doc['ActivedDate'].value)"),
				// ScriptSortType.NUMBER).order(SortOrder.ASC))
				.sort("ActivedDate", SortOrder.DESC);

		var searchRequest = new SearchRequest(CurrentFAQIndexDB);
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		try {

			var faqlist = new ArrayList<FaqBO[]>();
			System.out.println("hits: " + queryResults.getHits().getTotalHits().value);
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), FaqSO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();

			System.out.println("idlist: " + idlist.length);

			var result = oclient.QueryFunction("faq_GetByIDList", FaqBO[].class, false, idlist);

			return result;

		} catch (Exception e) {
			Logs.LogException(e);

		}

		return null;

	}

	public NewsBO FnSearchQuestion(int searchType, LocalDateTime plusDays, Date currentDay, NewsBO qry, int orderType,
			Int64Order orderBy, int pageIndex, int pageSize) throws Exception {

		// Variables
		var q = QueryBuilders.boolQuery();

		// Execute && Codition
		if (pageSize < 0) {
			pageSize = 10;
		} else if (pageSize > 50) {
			pageSize = 50;
		} else if (pageSize * pageIndex > 10000) {
			pageIndex = 0;
		}

		if (qry.ManuID == 1) {
			// TODO Filtered
		} else {
			if (qry.ListParentThreadID != null) {
				String[] sl = qry.ListParentThreadID.split(CurrentFAQIndexDB, ',');
				q.must(termsQuery(qry.ListParentThreadID, sl));
			}

			if (qry.ProductIDList != null) {
				String[] sl = qry.ProductIDList.split(CurrentFAQIndexDB, ',');
				q.must(termsQuery(qry.ProductIDList, sl));
			}

			if (qry.ListChildThreadID != null) {
				String[] sl = qry.ListChildThreadID.split(CurrentFAQIndexDB, ',');
				q.must(termsQuery(qry.ListChildThreadID, sl));
			}
		}

		switch (qry.IsPrivate) {
		case 1:
			q.must(termQuery("", 1));
			break;
		case 2:
			q.must(termQuery("", 2));
			break;
		}

		q.must(termsQuery("listCategoryID", new String[] { "1708" }));

		if (qry.CustomerID > 0) {
			q.must(termQuery("CustomerID", qry.CustomerID));
		}

		if (qry.PostType > 0) {
			if (qry.PostType == 5) {
				q.must(termQuery("PostType", new int[] { 2, 3 }));
			} else {
				q.must(termQuery("PostType", qry.PostType));
			}
		}

		if (qry.CategoryID == 1) {
			// TODO
		} else {
			// TODO
		}

		if (qry.AmountComment > 0) {
			q.must(termQuery("AmountComment", qry.AmountComment));
		}

		if (qry.AmountComment == 0 && qry.IsAdminAnswer <= 0) // Non-reply
		{
			q.must(termQuery("PostType", 1));

			q.must(rangeQuery(" ")); // TODO
			q.must(boolQuery()); // TODO
		}

		if (qry.Device > -1) {
			q.must(termQuery("Device", qry.Device));
		}

		if (searchType == 1) // ngay
		{
			// TODO
		}

		if (qry.IsDraft == true) {
			String keyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(qry.SEKeyword));
			q.must(queryStringQuery(null)); // TODO
		} else {
			if (qry.SEKeyword != null) {
				String keyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(qry.SEKeyword));

//				q.must(boostingQuery(positiveQuery, negativeQuery)); // TODO
			}
		}

		if (orderType == 4) {
			q.must(rangeQuery(null)); // TODO
		}

//		InternalSearchResponse<NewsBO> queryResults = null;

//		var queryResults = clientIndex.Search<NewsBO>(); 

		return null;
	}

}
