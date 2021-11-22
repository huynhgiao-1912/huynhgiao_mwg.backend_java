package mwg.wb.business;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.boostingQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.search.sort.SortBuilders.scriptSort;

import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.scriptFunction;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mwg.wb.business.webservice.WebserviceHelper;
import mwg.wb.client.elasticsearch.dataquery.NewSearch;
import mwg.wb.model.scs.ResultBO;
import mwg.wb.model.webservice.NewsView;
import org.apache.commons.lang.StringUtils;
import org.eclipse.persistence.internal.helper.StringHelper;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery.ScoreMode;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder.FilterFunctionBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryAction;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequestBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.elasticsearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.sort.ScriptSortBuilder.ScriptSortType;
import org.elasticsearch.search.sort.SortBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Ints;
import com.hazelcast.map.impl.query.Query.QueryBuilder;

import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.elasticsearch.dataquery.NewsEventQuery;
import mwg.wb.client.elasticsearch.dataquery.NewsQuery;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.MessageQueuePushType;
import mwg.wb.common.Utils;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.commonpackage.KeyWordRedirectBO;
import mwg.wb.model.commonpackage.KeyWordRedirectSO;
import mwg.wb.model.news.CustomerUserBO;
import mwg.wb.model.faq.FaqBO;
import mwg.wb.model.news.GameUserBO;
import mwg.wb.model.news.HotTopicBO;
import mwg.wb.model.news.KeywordSuggest;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.news.NewsCategoryBO;
import mwg.wb.model.news.NewsEventBO;
import mwg.wb.model.news.NewsGallery_NewsBO;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.search.FaqSO;
import mwg.wb.model.search.NewsEventSO;
import mwg.wb.model.search.NewsSO;
import mwg.wb.model.searchresult.FaqSR;
import mwg.wb.model.searchresult.NewsBOSR;
import mwg.wb.model.searchresult.NewsSOSR;
import mwg.wb.model.social.SocialNotifyBO;
import mwg.wb.model.social.SocialNotifyBOR;
import mwg.wb.model.social.SocialNotifySO;

public class NewsHelper {
	private static ORThreadLocal oclient = null;
	protected ObjectMapper mapper = null;
	ClientConfig config = null;

	protected RestHighLevelClient clientIndex = null;
	private ElasticClient elasticClient = null;
	protected String CurrentFAQIndexDB = "";
	protected String CurrentNewsIndexDB = "ms_news";

	public NewsHelper(ORThreadLocal afactoryRead, ClientConfig aconfig) {
		CurrentFAQIndexDB = "ms_news"; // aconfig.ELASTICSEARCH_PRODUCT_INDEX;
		oclient = afactoryRead;
		config = aconfig;
//		mapper = new ObjectMapper();
//		DateFormat df = new SimpleDateFormat(GConfig.DateFormatStringNews);
//		mapper.setDateFormat(df);
		mapper = DidxHelper.generateJsonMapper(GConfig.DateFormatStringNews);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		elasticClient = ElasticClient.getInstance(config.SERVER_ELASTICSEARCH_READ_HOST);
		// elasticClient = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
		clientIndex = elasticClient.getClient();

	}

	public NewsBO[] GetNewsDetailByID(int newsID) throws Throwable {
		var data = oclient.QueryFunction("news_GetByID", NewsBO[].class, false, newsID);
		if (data != null && data.length > 0 && data[0].IsActived == 1 && data[0].IsDeleted == 0)
			return data;
		return null;
	}

	public List<NewsBO> GetListNewsByListID(List<Integer> lids) throws Throwable {

		int[] aids = Ints.toArray(lids);
		var listNews = oclient.QueryFunction("news_GetByIDList", NewsBO[].class, false, aids);
		if (listNews != null)
			return Arrays.asList(listNews);
		return new ArrayList<NewsBO>();
	}

	public NewsEventBO GetNewsEventByID(int eventID) throws Throwable {
		var listEvents = oclient.QueryFunctionCached("news_event_GetInfo", NewsEventBO[].class, false, eventID);
		if (listEvents != null && listEvents.length > 0) {
			return listEvents[0];
		}
		return null;
	}

	public List<NewsEventBO> GetNewsEventByListID(String[] lsteventID) throws Throwable {

		var newList = Stream.of(lsteventID).mapToDouble(x -> Double.valueOf(x)).toArray();
		var listEvents = oclient.QueryFunction("news_event_GetInfoByListID", NewsEventBO[].class, false, newList);
		if (listEvents != null)
			return Arrays.asList(listEvents);
		return new ArrayList<NewsEventBO>();
	}

	public List<NewsBO> GetNewsByTopicID(int topicID) throws Throwable {
		var listNews = oclient.QueryFunctionCached("news_GetByTopicID", NewsBO[].class, false, topicID);
		if (listNews != null)
			return Arrays.asList(listNews);
		return new ArrayList<NewsBO>();
	}

	public List<NewsBO> GetNewsByListTopicID(String[] lsttopicID) throws Throwable {

		var listNews = oclient.QueryFunction("news_GetByListTopicID", NewsBO[].class, false, lsttopicID);
		if (listNews != null)
			return Arrays.asList(listNews);
		return new ArrayList<NewsBO>();
	}

	public GameUserBO GetUserByID(Integer userID) throws Throwable {
		var listUsers = oclient.QueryFunctionCached("game_user_GetInfo", GameUserBO[].class, false, userID);
		if (listUsers != null && listUsers.length > 0) {
			return listUsers[0];
		}
		return null;

	}

	public List<GameUserBO> GetUserByListID(Integer[] userID) throws Throwable {

		int[] newList = Ints.toArray(Arrays.asList(userID));
		var listUsers = oclient.QueryFunction("game_user_GetInfoByListID", GameUserBO[].class, false, newList);

		if (listUsers != null)
			return Arrays.asList(listUsers);
		return new ArrayList<GameUserBO>();

	}

	public GameUserBO GetUserByIDFromCache(Integer userID) throws Throwable {

		String key = "GetUserByIDFromCache_" + userID;
		var rs = (GameUserBO) CacheStaticHelper.GetGameUserFromCache(key);
		if (rs == null) {
			rs = GetUserByID(userID);
			CacheStaticHelper.AddGameUserToCache(key, rs);

		}

		return rs;

	}

	public CustomerUserBO GetCustomerUserByID(String userID) throws Throwable {
		var listUsers = oclient.QueryFunctionCached("news_GetByCustomerID", CustomerUserBO[].class, false, userID);
		if (listUsers != null && listUsers.length > 0) {
			return listUsers[0];
		}
		return null;

	}

	public List<CustomerUserBO> GetCustomerUserByListID(Double[] userID) throws Throwable {

		var newList = Stream.of(userID).mapToDouble(x -> x).toArray();
		var listUsers = oclient.QueryFunction("news_GetByListCustomerID", CustomerUserBO[].class, false, newList);
		if (listUsers != null) {
			return Arrays.asList(listUsers);
		}
		return new ArrayList<CustomerUserBO>();

	}

	public CustomerUserBO GetCustomerUserByIDFromCache(String userID) throws Throwable {

		String key = "GetCustomerUserByIDFromCache_" + userID;
		var rs = (CustomerUserBO) CacheStaticHelper.GetGameUserFromCache(key);
		if (rs == null) {
			rs = GetCustomerUserByID(userID);
			CacheStaticHelper.AddGameUserToCache(key, rs);

		}

		return rs;
	}

	public List<NewsGallery_NewsBO> GetListPictureNewsGallery(int newsID, int allbumID, int pictureID)
			throws Throwable {
		var listGallery = oclient.QueryFunctionCached("news_gallery_GetInfo", NewsGallery_NewsBO[].class, false, newsID,
				allbumID, pictureID);
		if (listGallery != null)
			return Arrays.asList(listGallery);
		return new ArrayList<NewsGallery_NewsBO>();
	}

	public List<NewsEventBO> GetListNewsEventByListID(List<Integer> lids) throws Throwable {
		int[] aids = Ints.toArray(lids);
		var listNews = oclient.QueryFunction("news_GetEventByIDList", NewsEventBO[].class, false, aids);
		if (listNews != null)
			return Arrays.asList(listNews);
		return new ArrayList<NewsEventBO>();
	}

	public List<NewsCategoryBO> GetCategoriesByParentID(int parentID) throws Throwable {
		var listNewsCate = oclient.QueryFunctionCached("news_GetCategoriesByParentID", NewsCategoryBO[].class, false,
				parentID);
		if (listNewsCate != null)
			return Arrays.asList(listNewsCate);
		return new ArrayList<NewsCategoryBO>();
	}

	// virtual funtion
	public NewsSOSR Ela_SearchNews2020(NewsQuery newsQuery, CodeTimer querytimer, CodeTimer parsetimer)
			throws Throwable {

		return new NewsSOSR();
	}

	public NewsSOSR ElaSearchRelateNews(NewsBO newsBO, int pageIndex, int pageSize, int rootNewsID, int siteID,
			CodeTimer querytimer, CodeTimer parsetimer) {
		// TODO Auto-generated method stub
		return new NewsSOSR();
	}

	public List<NewsEventSO> Ela_SearchNewsEvent2020FromCache(NewsEventQuery newsEventQuery, CodeTimer querytimer,
			CodeTimer parsetimer) {
		String keyT = "Ela_SearchNewsEvent2020FromCache";
		String keyO = newsEventQuery.siteID + "_" + newsEventQuery.pageIndex + "_" + newsEventQuery.pageSize + "_"
				+ ((newsEventQuery.lstEventID == null || newsEventQuery.lstEventID.size() == 0) ? ""
						: StringUtils.join(newsEventQuery.lstEventID, "_"))
				+ "_" + newsEventQuery.isFollow + "_" + newsEventQuery.parentEventID + "_" + newsEventQuery.orderType
				+ "_" + newsEventQuery.keyword;

		if (newsEventQuery.fromDate != null) {
			keyO += newsEventQuery.fromDate.getTime();
		}
		if (newsEventQuery.toDate != null) {
			keyO += "_" + newsEventQuery.toDate.getTime();
		}
		List<NewsEventSO> result = new ArrayList<>();
		if ((result = CachedObject.getObject(keyT, keyO, 15, List.class)) == null) {
			result = Ela_SearchNewsEvent2020(newsEventQuery, querytimer, parsetimer);
			CachedObject.putObject(keyT, keyO, result);
		}
		return result;
	}

	public List<NewsEventSO> Ela_SearchNewsEvent2020(NewsEventQuery newsEventQuery, CodeTimer querytimer,
			CodeTimer parsetimer) {
		// TODO Auto-generated method stub
		return new ArrayList<NewsEventSO>();
	}

	public NewsSOSR GetNewsListByCommentCount(int categoryID, int pagesize, int pageIndex, int siteID,
			CodeTimer querytimer, CodeTimer parsetimer) {
		// TODO Auto-generated method stub
		return new NewsSOSR();
	}

	public List<HotTopicBO> GetAllHotTopic() throws Throwable {
		var listNewsHotTopic = oclient.QueryFunctionCached("news_GetAllHotTopic", HotTopicBO[].class, false);
		if (listNewsHotTopic != null)
			return Arrays.asList(listNewsHotTopic);
		return new ArrayList<HotTopicBO>();
	}

	public NewsCategoryBO GetNewsCateByID(int categoryId) throws Throwable {
		var newsCates = oclient.QueryFunctionCached("news_GetNewsCateByID", NewsCategoryBO[].class, false, categoryId);
		if (newsCates != null && newsCates.length > 0)
			return newsCates[0];
		return null;
	}

	// virtual funtion
	public List<NewsBO> GetNewsRelativeByQuery(NewsQuery newsQuery, CodeTimer queryTimer, CodeTimer parseTimer) {
		return null;
	}

	public List<NewsBO> GetTopNews(CodeTimer queryTimer, CodeTimer parseTimer) throws Throwable {
		// lấy 6 tin
		// GetTopNews 1 : mặc định trả về 1 tin
		// 2,3: trả về 6 tin

		final var NUMBER_TOP = 7;

		// get top view
		var topView = GetTopNews(1);
		var topCmtNews = GetTopNews(2);
		var rs = new ArrayList<NewsBO>();
		if (topView != null && topView.size() > 0) {
			rs.add(topView.get(0));
		}
		if (topCmtNews != null && topCmtNews.size() > 0) {
			rs.addAll(topCmtNews);
		}

		if (rs.size() < NUMBER_TOP) {
			var tmp = GetTopNews(3);
			if (tmp != null && tmp.size() > 0) {
				rs.addAll(tmp);
			}
		}

		return rs.size() > NUMBER_TOP ? rs.subList(0, NUMBER_TOP) : rs;
	}

	private List<NewsBO> GetTopNews(int type) throws Throwable {
		var news = oclient.QueryFunctionCached("bhx_news_getTop", NewsBO[].class, false, type);
		if (news != null)
			return Arrays.asList(news);
		return null;
	}

	public CustomerUserBO GetUserByUserName(String createdUser) throws Throwable {
		var listUsers = oclient.QueryFunctionCached("news_getByUsername", CustomerUserBO[].class, false, createdUser);
		if (listUsers != null && listUsers.length > 0) {
			return listUsers[0];
		}
		return null;
	}

	public ProductBO GetSimpleProductInfo(int productID, int siteID) throws Throwable {

		var lstProducts = oclient.QueryFunctionCached("product_GetSimpleInfoByID", ProductBO[].class, false, productID,
				siteID, siteID != 6 ? "vi-VN" : "km-KH");
		if (lstProducts != null && lstProducts.length > 0) {
			return lstProducts[0];
		}
		return null;
	}

	public NewsSOSR GetRelatedNewsByProductInfo(ProductBO productInfo, int newsCount, int siteID, CodeTimer querytimer,
			CodeTimer parsetimer) {

		return null;
	}

	public GameUserBO GetUserByUserIdOrCustomerId(String createdCustomerID, String createdUser) throws Throwable {
		var listUsers = oclient.QueryFunctionCached("game_user_GetByUserOrCustomerID", GameUserBO[].class, false,
				!Utils.StringIsEmpty(createdUser) ? createdUser : "null",
				!Utils.StringIsEmpty(createdCustomerID) ? createdCustomerID : "null");
		if (listUsers != null && listUsers.length > 0) {
			return listUsers[0];
		}
		return null;
	}

	public List<GameUserBO> GetListUserByUserIdOrCustomerId(Double[] createdCustomerID, String[] createdUser)
			throws Throwable {

		var newListCustomerID = Stream.of(createdCustomerID).mapToDouble(x -> x).toArray();
		var listUsers = oclient.QueryFunction("game_user_GetByListUserOrCustomerID", GameUserBO[].class, false,
				createdUser, newListCustomerID);
		if (listUsers != null) {
			return Arrays.asList(listUsers);
		}
		return new ArrayList<GameUserBO>();
	}

	public GameUserBO GetUserByUserIdOrCustomerIdFromCache(String createdCustomerID, String createdUser)
			throws Throwable {

		String key = "GetUserByUserIdOrCustomerIdFromCache" + createdCustomerID + "_" + createdUser;
		var rs = (GameUserBO) CacheStaticHelper.GetGameUserFromCache(key);
		if (rs == null) {
			rs = GetUserByUserIdOrCustomerId(createdCustomerID, createdUser);
			CacheStaticHelper.AddGameUserToCache(key, rs);

		}

		return rs;

	}

	public NewsSOSR Ela_GetRelatedNewsByProductID(ProductBO productBO, int siteID) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<NewsSO> ElaSearchNews(int pageSize, String keyword, String[] cateList, int type, int productID)
			throws Exception {
		return null;
	}

	public ProductBO GetProductBO(int productID) throws Throwable {
		var listUsers = oclient.QueryFunctionCached("gameapp_GetByIdSE", ProductBO[].class, false, productID, 1,
				"vi-VN");
		if (listUsers != null && listUsers.length > 0) {
			return listUsers[0];
		}
		return null;

	}

	public NewsSO GetViewNewsES(int newsID) {
		try {
//			var sb = new SearchSourceBuilder();
//			var q = boolQuery();
//			q.must(termQuery("NewsID", newsID));
//			sb.from(0).size(1).query(q);
//			
//			
//			var searchRequest = new SearchRequest("ms_news");
//			searchRequest.source(sb);
			// var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

			GetRequest getRequest = new GetRequest("ms_news", Integer.toString(newsID));
			var queryResults = clientIndex.get(getRequest, RequestOptions.DEFAULT);
			var aa = queryResults.getSourceAsString();
			var bb = queryResults.getSource();

			var newitem = mapper.readValue(queryResults.getSourceAsString(), NewsSO.class);

			if (newitem == null)
				return null;

			return newitem;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean UpdateResetViewCount_Test() throws IOException {

		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("IsActived", 1));
		q.must(rangeQuery("ActivedDate").lte(new Date()));

		q.must(termsQuery("ListTreeCategory", "g2002g")); // 2002 là parent news của gameapp

		sb.from(0).size(300).query(q);

		var searchRequest = new SearchRequest("ms_news");
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
				String queue = "gr.dc4.didx.newsviewcount";
				String queueBK = "gr.dc2.didx.newsviewcount";
				String queueDev = "gr.beta.didx.newsviewcount";

				for (int i : idlist) {

					MessageQueue nwmsg = new MessageQueue();

					nwmsg.Action = DataAction.Update;
					nwmsg.ClassName = "mwg.wb.pkg.news.NewSE";
					nwmsg.CreatedDate = Utils.GetCurrentDate();
					nwmsg.Lang = "vi-VN";
					nwmsg.SiteID = 1;
					nwmsg.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_VIEWCOUNT_RESETVIEW_7DAY;
					nwmsg.Data = "";
					nwmsg.DataCenter = config.DATACENTER;
					nwmsg.Identify = String.valueOf(i);
					Logs.Log(true, "", "push upsert=> news viewcounter 7day gameapp ");

					nwmsg.Note = "UpdateResetViewCount2";
					QueueHelper.Current(config.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev, nwmsg,
							true, "", nwmsg.DataCenter);
				}

			}

		} catch (Exception e) {

			Logs.LogException(e);
		}

		return false;
	}

	public boolean UpdateResetViewCount2() throws IOException {

		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("IsActived", 1));
		q.must(rangeQuery("ActivedDate").lte(new Date()));

		q.must(termsQuery("ListTreeCategory", "g2002g")); // 2002 là parent news của gameapp

		sb.from(0).size(9999).query(q);

		var searchRequest = new SearchRequest("ms_news");
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
				String queue = "gr.dc4.didx.newsviewcount";
				String queueBK = "gr.dc2.didx.newsviewcount";
				String queueDev = "gr.beta.didx.newsviewcount";

				for (int i : idlist) {

					MessageQueue nwmsg = new MessageQueue();

					nwmsg.Action = DataAction.Update;
					nwmsg.ClassName = "mwg.wb.pkg.news.NewSE";
					nwmsg.CreatedDate = Utils.GetCurrentDate();
					nwmsg.Lang = "vi-VN";
					nwmsg.SiteID = 1;
					nwmsg.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_VIEWCOUNT_RESETVIEW_7DAY;
					nwmsg.Data = "";
					nwmsg.DataCenter = config.DATACENTER;
					nwmsg.Identify = String.valueOf(i);
					Logs.Log(true, "UpdateResetViewCount2", "push upsert=> news viewcounter 7day gameapp ");

					nwmsg.Note = "UpdateResetViewCount2";
					QueueHelper.Current(config.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev, nwmsg,
							true, "", nwmsg.DataCenter);
				}

			}

		} catch (Exception e) {

			Logs.LogException(e);
		}

		return true;
	}

	public boolean UpdateResetViewCount() {
		try {
			// clientIndex || elasticClient

			Calendar calendar = Calendar.getInstance();
			int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
			int indexDays = (dayOfYear % 7) + 1;
//gr.dc2.didx.newsviewcount
//			UpdateByQueryRequestBuilder updateByQuery = new UpdateByQueryRequestBuilder((ElasticsearchClient) clientIndex,
//					UpdateByQueryAction.INSTANCE);
//			updateByQuery.source("ms_news")
//			.filter(QueryBuilders.termQuery("ListTreeCategory", "g2002g"))
//			.filter(QueryBuilders.termQuery("IsDeleted", 0))
//			.filter(QueryBuilders.termQuery("IsActived", 1))
//			.maxDocs(99999)
//			.script(new Script(ScriptType.INLINE, "painless", "ctx._source.lstView7Day.ViewCounter7Days"+indexDays+" = 0;",
//							Collections.emptyMap()));
//			
//			
//			
//			BulkByScrollResponse response = updateByQuery.get();

			Map<String, Object> listView = new HashMap<>();
			listView.put("ViewCounter7Days1", 0);
			listView.put("ViewCounter7Days2", 0);
			listView.put("ViewCounter7Days3", 0);
			listView.put("ViewCounter7Days4", 0);
			listView.put("ViewCounter7Days5", 0);
			listView.put("ViewCounter7Days6", 0);
			listView.put("ViewCounter7Days7", 0);

			HashMap<String, Object> scriptParams = new HashMap<>();
			try {
				scriptParams.put("lstView7Day", listView);// mapper.writeValueAsString()
			} catch (Throwable e) {
				// TODO: handle exception
				Logs.LogException("news_view7d", e);
				// System.out.println("mapper.writeValueAsString");
			}

			UpdateByQueryRequest request = new UpdateByQueryRequest("ms_news");
			request.setQuery(new TermQueryBuilder("ListTreeCategory", "g2002g"));
			request.setQuery(new TermQueryBuilder("IsDeleted", 0));
			request.setQuery(new TermQueryBuilder("IsActived", 1));

			request.setMaxDocs(999999);
			request.setScript(new Script(ScriptType.INLINE, "painless",
					"if(ctx._source.RelatedCategory != null && ctx._source.RelatedCategory.contains(\"G2002G\")){"
							+ "if(!ctx._source.containsKey(\"lstView7Day\") || ctx._source.lstView7Day == null || ctx._source.lstView7Day.length == 0){"
							+ "	ctx._source.lstView7Day = params.lstView7Day;" + "}else{"
							+ "ctx._source.lstView7Day.ViewCounter7Days" + indexDays + " = 0;" + "}" + "}",
					scriptParams));
			var bulkResponse = clientIndex.updateByQuery(request, RequestOptions.DEFAULT);
			boolean timedOut = bulkResponse.isTimedOut();
			long totalDocs = bulkResponse.getTotal();
			long updatedDocs = bulkResponse.getUpdated();
			long deletedDocs = bulkResponse.getDeleted();
			long batches = bulkResponse.getBatches();
			long noops = bulkResponse.getNoops();
			long versionConflicts = bulkResponse.getVersionConflicts();
			long bulkRetries = bulkResponse.getBulkRetries();
			// long searchRetries = bulkResponse.getSearchRetries();
			String strlog = "";
			if (timedOut) {
				strlog += "UpdateResetViewCount timeout \n";
			}
			strlog += "update total " + updatedDocs + " docs. \n";

			Logs.Log(true, "Daily_UpdateResetViewCount", strlog);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public long AddSocialNotify(SocialNotifyBO Info) throws IOException {

		try {
			if (Info != null) {
				if (Info.Type != 1 && Info.Type != 2 && Info.Type != 3 && Info.Type != 5)
					return -1;

				if (Info.ToUserB == null && Info.FromUserA == null) {
					return -2;
				}
				if (Info.SiteID <= 0)
					return -3;

				Date dateC = new Date();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dateC);

				if (Info.Type == 3)// userfollowtopic
				{
					Info.NotifyID = Info.ToUserB.UserID + Info.TopicObj.TopicID;
				} else if (Info.Type == 5)// user follow user
				{

					int Month = calendar.get(Calendar.MONTH);
					int day = calendar.get(Calendar.DAY_OF_MONTH);
					int Year = calendar.get(Calendar.YEAR);

					Info.NotifyID = Info.FromUserA.UserID + Info.ToUserB.UserID + Year + day + Month;
				} else {

					Date date = calendar.getTime();
					long millis = date.getTime();
					Info.NotifyID = Info.CommentID + Info.NewsObj.NewsID + millis;// tao key nay tránh trùng
				}

				var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).IndexObject("ms_notify",
						Info, Info.NotifyID + "");
				if (!rs) {
					return -1;
				}
				return Info.NotifyID;

			}

		} catch (Exception e) {

			Logs.LogException(e);
		}

		return -1;
	}

	public long GetAggFollowByTopicID(int topicID, Integer siteID) throws Throwable {

		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("SiteID", siteID));
		q.must(termQuery("TopicObj.TopicID", topicID));

		sb.from(0).size(1).query(q);
		sb.aggregation(AggregationBuilders.count("notify_count").field("NotifyID"));

		var searchRequest = new SearchRequest("ms_notify");
		searchRequest.source(sb);
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

		// int rowcount = (int) queryResults.getHits().getTotalHits().value;
		ValueCount agg = queryResults.getAggregations().get("notify_count");
		Long value = agg.getValue();
		return value;
	}

	public SocialNotifyBOR SearchNotify(SocialNotifySO qry) throws Throwable {

		if (qry.PageSize < 0)
			qry.PageSize = 10;
		if (qry.PageSize > 100)
			qry.PageSize = 100;
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		q.must(termQuery("SiteID", qry.SiteID));

		if (qry.NotifyType > 0) {
			q.must(termQuery("Type", qry.NotifyType));
		} else {
			q.must(termsQuery("Type", new String[] { "1", "2", "4" }));
		}
		if (qry.UserID > 0) {
			q.must(termQuery("ToUserB.UserID", qry.UserID));
		}

		if (qry.FromUserID > 0) {
			q.must(termQuery("FromUserA.UserID", qry.FromUserID));
		}

		if (qry.IsRead > 0) {
			if (qry.IsRead == 1)// lây notify da doc
			{
				q.must(termQuery("IsRead", true));
			} else if (qry.IsRead == 2)// lây notify chua doc
			{
				q.must(termQuery("IsRead", false));
			}

		}
		if (qry.TopicID > 0) {
			q.must(termQuery("TopicObj.TopicID", qry.TopicID));
		}

		sb.from(qry.PageIndex * qry.PageSize).size(qry.PageSize).query(q);
		if (qry.orderBy == 1) {
			sb.sort("Createddate", SortOrder.ASC);
		} else if (qry.orderBy == 2) {
			sb.sort("Createddate", SortOrder.DESC);
		}
		// orderBy 1 => asc
		var searchRequest = new SearchRequest("ms_notify");
		searchRequest.source(sb);

		SearchResponse queryResults = null;

//		var elasticClient1 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST); 
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
		SocialNotifyBOR result = new SocialNotifyBOR();

		int rowcount = (int) queryResults.getHits().getTotalHits().value;
		if (rowcount > 0) {

			var ListNotify = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), SocialNotifyBO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).collect(Collectors.toList());

			result.total = rowcount;
			result.message = "";
			result.result = ListNotify.toArray(new SocialNotifyBO[0]); // (SocialNotifyBO[])
		}

		return result;
	}

	public Boolean UserUnfollowUser(int FromUserID, int toUserID, Integer siteID) throws Throwable {

		var lstNotify = SearchNotify(new SocialNotifySO() {
			{
				PageIndex = 0;
				PageSize = 100;
				UserID = toUserID;
				FromUserID = FromUserID;
				NotifyType = 5;
			}
		});
		if (lstNotify != null && lstNotify.result != null && lstNotify.result.length > 0 && lstNotify.total > 0) {

			// BulkProcessor bulkProcessor = new BulkProcessor(null, null, null, siteID,
			// siteID, null, null, null, null, null);
			for (var item : lstNotify.result) {

				try {
					DeleteRequest request = new DeleteRequest("ms_notify", Long.toString(item.NotifyID));
					var queryResults = clientIndex.delete(request, RequestOptions.DEFAULT);
					if (queryResults.getResult() != null && queryResults.getResult() == Result.NOT_FOUND) {
						/// xoá ko được.
					}
				} catch (Throwable e) {
					// TODO: handle exception
					Logs.LogException("API_MXH_UserUnfollowUser", e);
				}
			}

		}

		return true;
	}

	public Boolean UserUnfollowAllTopic(int userID, Integer siteID) throws Throwable {

		if (userID <= 0)
			return false;

		var lstNotify = SearchNotify(new SocialNotifySO() {
			{
				PageIndex = 0;
				PageSize = 100;
				UserID = userID;
				NotifyType = 3;
			}
		});

		if (lstNotify != null && lstNotify.result != null && lstNotify.result.length > 0 && lstNotify.total > 0) {

			for (var item : lstNotify.result) {
				try {
					DeleteRequest request = new DeleteRequest("ms_notify", Long.toString(item.NotifyID));
					var queryResults = clientIndex.delete(request, RequestOptions.DEFAULT);
					if (queryResults.getResult() != null && queryResults.getResult() == Result.NOT_FOUND) {
						/// xoá ko được.
					}
					return true;
				} catch (Throwable e) {

					Logs.LogException("API_MXH_UserUnfollowAllTopic", e);
				}
			}

		}
		return false;
	}

	public Boolean TickIsReadNotify(long notifyID, Integer siteID) throws Throwable {
		var update = new UpdateRequest("ms_notify", String.valueOf(notifyID))
				.doc("{\"IsRead\":" + true + " }", XContentType.JSON).docAsUpsert(true).detectNoop(false);

		var response = clientIndex.update(update, RequestOptions.DEFAULT);
		if (response.getResult() != null && response.getResult() != Result.NOT_FOUND) {
			return true;
		}
		return false;

	}

	public String GetJsonFromObject(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	public NewsSOSR GetNewsListByCategoryID(NewsQuery newsQuery, CodeTimer querytimer, CodeTimer parsetimer)
			throws Throwable {

		return new NewsSOSR();
	}

	public NewsBOSR GetNewsVideoByCategoryID(int categoryID, int siteID, int pageIndex, int pageSize) throws Throwable {

		return new NewsBOSR();
	}

	public NewsBOSR GetNewsByStore(int storeID, int siteID, int pageIndex, int pageSize) throws Throwable {

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
		if (siteID > 0) {
			q.must(termQuery("SiteID", siteID));
		}
		if (storeID > 0) {
			q.must(termQuery("StoreID", storeID));
		}

		// objNewsBO.ListCategoryID.Contains("1022") ||
		// objNewsBO.ListCategoryID.Contains("1675")

		// a Thai yêu cau bo cate 1744 => cac bai tin thuong hieu bhx
		q.mustNot(termQuery("ListTreeCategory", "g1744g"));
		// q.mustNot(termQuery("ListTreeCategory", "g1264g")); // chủ đề liên quan
		// bachhoaxanh
		q.mustNot(termQuery("ListTreeCategory", "g2002g"));// bai tin gameapp

		sb.from(pageIndex * pageSize).size(pageSize).query(q);
		sb.sort("ActivedDate", SortOrder.DESC);

		var searchRequest = new SearchRequest(CurrentNewsIndexDB);
		searchRequest.source(sb);

//	var	elasticClient1 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST);
//	var	clientIndex1 = elasticClient1.getClient();
		try {
			var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

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

		} catch (Throwable e) {

			Logs.LogException(e);
		} finally {
			// elasticClient1.close();
		}
		return gameAppNews;
	}
	public NewsBO[] getNewsByCategoryidParent(NewSearch search, int siteID, int pageSize){

		var sb = new SearchSourceBuilder();
		sb.fetchSource("NewsID",null);
		var q = boolQuery();
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("IsActived", 1));
		if(siteID > 0) {
			q.must(termQuery("SiteID", siteID));
		}
		if(search!= null){
			search.queryMust(q);
		}
		q.must(rangeQuery("ActivedDate").lte(new Date()));

		sb.from(0).size(pageSize).query(q);
		sb.sort("ActivedDate",SortOrder.DESC);

		var searchRequest = new SearchRequest(CurrentNewsIndexDB);
		searchRequest.source(sb);
		NewsBO[] news = null;


//	var	elasticClient1 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST);
//	var	clientIndex1 = elasticClient1.getClient();
		try {
			var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);


			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), NewsBO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.NewsID).toArray();


			 news = GetListNewsByListID(Arrays.stream(idlist).boxed().collect(Collectors.toList())).stream().toArray(NewsBO[]::new);

		} catch (Throwable e) {

			Logs.LogException(e);
		}finally {
			//	elasticClient1.close();
		}
		return news;
	}



	public KeywordSuggest[] GetkeywordSuggestNewsFromCache(String keyword,Integer clearcache) throws Throwable {

		String key = "GetkeywordSuggestNewsFromCache_" + keyword;
		KeywordSuggest[] rs = (KeywordSuggest[]) CacheStaticHelper.GetFromCache(key, Utils.StringIsEmpty(keyword) ? 15 : 2);
		if (rs == null || clearcache == 1) {
			rs = GetkeywordSuggestNews(keyword);
			CacheStaticHelper.AddToCache(key, rs);
		}
		return rs;
	}
	public KeywordSuggest[] GetkeywordSuggestNews(String keyword) throws Throwable {

		
		List<KeywordSuggest> result = new ArrayList<KeywordSuggest>();
		var sb = new SearchSourceBuilder();
		var q = boolQuery();
		String rsl = "";
		String keyword_en = "";
		String[] keywords_en = new String[] {};
		int pagesize = 100;
		if (!Utils.StringIsEmpty(keyword)) {
			keyword_en = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(keyword)).replace("-", "");
			keywords_en = Utils.StringIsEmpty(keyword_en) ? null : keyword_en.split("\\s");

			if (Utils.StringIsEmpty(keyword_en)) {
				return null;
			}
			var formatKeyword = Arrays.stream(keywords_en).map(x->{
				return x + "*";
			}).toArray(String[]::new);
			if (keywords_en.length == 1)
				rsl = "(" + keyword_en + "*)";
			else {
				rsl = "( " + String.join(" AND ", formatKeyword) + " )";
			}
			if (keywords_en.length > 0) {
				q.must(queryStringQuery(rsl).field("unkeywordSuggest"));
				pagesize = 10;
			}

		} else {
			q.must(wildcardQuery("unkeywordSuggest", "*"));
		}
		sb.from(0).size(pagesize).query(q);

		var searchRequest = new SearchRequest("ms_newssuggestkeyword");
		searchRequest.source(sb);
		try {
			var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
			
			Stream.of(queryResults.getHits().getHits()).forEach(x -> {
				try {		
					var so =  mapper.readValue(x.getSourceAsString(), KeywordSuggest.class);
					result.add(so);		
				} catch (Throwable e) {
					e.printStackTrace();
					Logs.LogException(e);
				}
			});
		} catch (Throwable e) {
			Logs.LogException(e);
		}
		return result.toArray(KeywordSuggest[]::new);
	}

	public ResultBO addKeywordSuggestForNews(String keyWordSuggest) throws Throwable {
		//ResultBO
		if(DidxHelper.isHanh()){
			config.DATACENTER = 4;
		}
		var data = WebserviceHelper.Call(config.DATACENTER).Get("apinews/addkeywordsuggestfornews?keyWordSuggest=:?", ResultBO.class,keyWordSuggest);
		return data;
	}

}
