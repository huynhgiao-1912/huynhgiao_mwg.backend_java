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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mwg.wb.client.redis.RedisClient;
import mwg.wb.client.redis.RedisPoolConnection;
import mwg.wb.model.news.NewsCount;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
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

import mwg.wb.business.NewsHelper;
import mwg.wb.client.elasticsearch.dataquery.NewsEventQuery;
import mwg.wb.client.elasticsearch.dataquery.NewsQuery;
import mwg.wb.client.elasticsearch.dataquery.NewsQuery.DateOrder;
import mwg.wb.client.elasticsearch.dataquery.NewsQuery.Int64Order;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.news.NewsCategoryBO;
import mwg.wb.model.search.NewsEventSO;
import mwg.wb.model.search.NewsSO;
import mwg.wb.model.searchresult.FaceObject;
import mwg.wb.model.searchresult.NewsSOSR;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

public class BHXNewsHelper extends NewsHelper{
	private static final String CurrentIndexDB = "ms_news";
//	private RedisClient redisClientBHX = null;
	private JedisPool redisPoolBHX =  null;
	private Jedis redisClientPoolBHX = null;
	private RedisPoolConnection rdmClient = null;
	private static int jedisCP = 1;
	public BHXNewsHelper(ORThreadLocal afactoryRead, ClientConfig aconfig) {
		super(afactoryRead, aconfig);
//		redisClientBHX = RedisClient.getInstance("192.168.2.209");
//		redisClientBHX.choiceDB(11);
//		//aconfig.REDIS_HOST_NLU);

		if(DidxHelper.isLive()) {
			if(redisPoolBHX == null){
				RedisPoolConnection.initializeSettings(aconfig.REDIS_CHAT_DIDX);
				redisPoolBHX = RedisPoolConnection.getPoolInstance();
				redisClientPoolBHX = redisPoolBHX.getResource();
				redisClientPoolBHX.select(aconfig.REDIS_CHAT_DIDX_NUMBERDB);
			}
			
			rdmClient = new RedisPoolConnection();
			rdmClient.initializeSettings(aconfig.REDIS_CHAT_DIDX);
			rdmClient.getPoolInstance();
		}

	}
	
	
	@Override
	public NewsSOSR Ela_SearchNews2020(NewsQuery newsQuery, CodeTimer queryTimer, CodeTimer parseTimer) {
		
		/*
		 * todo Kiểm tra từ strTag có nằm trong LP thương hiệu hay ko?
		 * code cũ check tại đây
		 * code java mới phải truyền vô cho api
		 * client phải xử lý thêm chỗ này
		 * 
		 * newsQuery.IsHasYouTube = false;
		*/		
		
		NewsSOSR result = new NewsSOSR();
		try {
			SearchSourceBuilder sb = new SearchSourceBuilder();
			var query = boolQuery();
			
			//chỉ lấy tin có cate 1733 (cate knh bhx) 
			query.must(termQuery("ListTreeCategory", "g1733g"));
			
			if(newsQuery.CategoryId > 0) {
				query.must(termQuery("ListTreeCategory", "g" + newsQuery.CategoryId + "g"));
			}
			
			if(newsQuery.RelatedNewsId > 0) {
				query.must(termQuery("RelatedNews", "g" + newsQuery.RelatedNewsId + "g"));
			}
			
			if(newsQuery.NewsId > 0) {
				query.mustNot(termQuery("NewsId", newsQuery.NewsId));
			}
			
			if(newsQuery.FromDate != null ) {
				query.must(rangeQuery("CreatedDate").gte(newsQuery.FromDate));
			}
			
			if(newsQuery.IsViewMore) {
				query.must(rangeQuery("UpdatedDate").lte(newsQuery.FromDate));
			}
			
			query.must(rangeQuery("ActivedDate").lte(Utils.AddHour(new Date(), 7)));
			query.must(termQuery("IsActived", 1));
			query.must(termQuery("IsDeleted", 0));
			
			if (!Utils.StringIsEmpty(newsQuery.Keyword)) {
					String keyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(newsQuery.Keyword));

					query.must(queryStringQuery(keyword).field("Keyword").defaultOperator(Operator.AND));
			}
			
			//to do check chỗ này
			//chỉ dùng cho hàm getlistbyQuery
			if(!Utils.StringIsEmpty(newsQuery.Tag)) {
				query.must(termQuery("KeywordTerm", DidxHelper.GenTerm(newsQuery.Tag)));
			}
			
			if(newsQuery.LoadedNewsIds != null) {
				for(var id: newsQuery.LoadedNewsIds) {
					query.mustNot(termQuery("NewsID", id));
				}
			}
			
			if(newsQuery.ExcludeIdsCate != null) {
				for(var cateId : newsQuery.ExcludeIdsCate) {
					 query.mustNot(termQuery("ListTreeCategory", "g" + cateId + "g"));
				}
			}
			
			//check lại title
			if(!Utils.StringIsEmpty(newsQuery.Title))
			{
				String titleVN = DidxHelper.FormatKeywordSearchField(newsQuery.Title);
				String title = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(newsQuery.Title));
				
				if (!newsQuery.IsSuggestSearch) {
					query.must(boolQuery()
						.should(QueryBuilders.matchPhraseQuery("SETIitleVN", titleVN).boost(10))
						.should(queryStringQuery(titleVN).field("SETIitleVN").boost(10).defaultOperator(Operator.AND))
						.should(queryStringQuery(title).field("SETIitle").boost(5).defaultOperator(Operator.AND))
						.should(termQuery("TagTerm", DidxHelper.GenTerm(newsQuery.Title)))
						.should(termQuery("KeywordTerm", DidxHelper.GenTerm(newsQuery.Title)))
						);
				}
				else {
					query.must(boolQuery()
						.should(queryStringQuery(title).field("SETIitle").boost(10).defaultOperator(Operator.AND))
						.should(termQuery("KeywordTerm", DidxHelper.GenTerm(newsQuery.Title)))
						);
				}
			}
			
			if(newsQuery.IsHasYouTube) {
				if(newsQuery.IsHasTagBrand) {
					query.must(QueryBuilders.matchPhrasePrefixQuery("SeTags", "bachhoaxanh youtube shopinshop " + newsQuery.Tag));
				}
				else {
					query.must(QueryBuilders.matchPhrasePrefixQuery("SeTags", "bachhoaxanh youtube"));
					query.mustNot(QueryBuilders.matchPhrasePrefixQuery("SeTags", "shopinshop"));
				}
			}
			if (newsQuery.ScoreOrder != null && newsQuery.ScoreOrder != Int64Order.NORMAL) {
				switch (newsQuery.ScoreOrder) {
					case LARGEST :
						sb.sort("_score", SortOrder.DESC);
						break;
					case SMALLEST:
						sb.sort("_score", SortOrder.ASC);
						break;
				default:
					break;
				}
			} else if (newsQuery.Order != null && newsQuery.Order != DateOrder.NORMAL) {
				switch (newsQuery.Order) {
					case LASTEST:
						sb.sort("ActivedDate", SortOrder.DESC);
						break;
					case OLDEST:
						sb.sort("ActivedDate", SortOrder.ASC);
						break;
				default:
					break;
				}
			}
			else if (newsQuery.OrderByCreatedDate != null && newsQuery.OrderByCreatedDate != DateOrder.NORMAL)
            {
                switch (newsQuery.OrderByCreatedDate)
                {
                    case LASTEST:
                    	sb.sort("CreatedDate", SortOrder.DESC);
						break;
                    case OLDEST:
                    	sb.sort("CreatedDate", SortOrder.ASC);
                    	break;
                    default:
                    	break;
                }
            }
			else if (newsQuery.OrderByUpdatedDate != null && newsQuery.OrderByUpdatedDate != DateOrder.NORMAL)
            {
                switch (newsQuery.OrderByUpdatedDate)
                {
	                case LASTEST:
	                	sb.sort("UpdatedDate", SortOrder.DESC);
						break;
	                case OLDEST:
	                	sb.sort("UpdatedDate", SortOrder.ASC);
	                	break;
	                default:
                    	break;
                }
            }
			else if (newsQuery.OrderByCommentCounter != null && newsQuery.OrderByCommentCounter != Int64Order.NORMAL)
            {
                switch (newsQuery.OrderByCommentCounter)
                {
                case LARGEST:
                	sb.sort("CommentCount", SortOrder.DESC);
					break;
                case SMALLEST:
                	sb.sort("CommentCount", SortOrder.ASC);
                	break;
                default:
                	break;
                }
            }
			else if (newsQuery.OrderByViewCounter != null && newsQuery.OrderByViewCounter != Int64Order.NORMAL)
            {
                switch (newsQuery.OrderByViewCounter)
                {
                case LARGEST:
                	sb.sort("ViewCounter", SortOrder.DESC);
					break;
                case SMALLEST:
                	sb.sort("ViewCounter", SortOrder.ASC);
                	break;
                default:
                	break;
                }
            }
			else if (newsQuery.OrderByLikeCounter != null && newsQuery.OrderByLikeCounter != Int64Order.NORMAL)
            {
                switch (newsQuery.OrderByLikeCounter)
                {
                case LARGEST:
                	sb.sort("LikeCounter", SortOrder.DESC);
					break;
                case SMALLEST:
                	sb.sort("LikeCounter", SortOrder.ASC);
                	break;
                default:
                	break;
                }
            }
			 else {
				 sb.sort("ActivedDate", SortOrder.DESC);
			 }
			
			sb.from(newsQuery.PageIndex * newsQuery.PageSize).size(newsQuery.PageSize);
			sb.query(query);
			
			SearchResponse queryResults = null;
			SearchRequest searchRequest = new SearchRequest(CurrentIndexDB);
			searchRequest.source(sb);
			
			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
			if (queryTimer != null)
				queryTimer.end();

			var listNewsSO = new LinkedHashMap<Integer, NewsSO>();

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
			int rowcount = (int) queryResults.getHits().getTotalHits().value;
			result.total = rowcount;
			result.newsSOList = listNewsSO;
		}
		catch(Exception e){
			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.message = "Failed on Ela_SearchNews2020: " + e.toString() + ": " + e.getMessage() + " - "

					+ trace;
			Logs.LogException(e);
		}
		
		return result;
	}
	
	public List<NewsBO> GetNewsRelativeByQuery(NewsQuery newsQuery, CodeTimer queryTimer, CodeTimer parseTimer) {
		var result = new ArrayList<NewsBO>();
		try {
			SearchSourceBuilder sb = new SearchSourceBuilder();
			var query = boolQuery();
			var boost = 1000;
			var bQ = new BoolQueryBuilder();
			
			//chỉ lấy tin có cate 1733 (cate knh bhx)
			query.must(termQuery("ListTreeCategory", "g1733g"));
			
			if(newsQuery.NewsIds != null && newsQuery.NewsIds.length > 0) {
				for(var newsId : newsQuery.NewsIds) {
						 bQ.should(termQuery("NewsID", newsId).boost(boost));
				}
			}
			
			if(newsQuery.RelatedTags != null && newsQuery.RelatedTags.length > 0) {
				int step = boost / (newsQuery.RelatedTags.length > 0 ? newsQuery.RelatedTags.length : 1);
				for(var tag : newsQuery.RelatedTags) {
					tag = DidxHelper.GenTerm(tag);
					 boost -= step;
						 bQ.should(termQuery("TagTerm", tag).boost(boost));
				}
			}
			
			if(newsQuery.LoadedNewsIds != null) {
				for(var newsId : newsQuery.LoadedNewsIds) {
					 query.mustNot(termQuery("NewsID", newsId));
				}
			}
			
			if(newsQuery.ExcludeIdsCate != null) {
				for(var cateId : newsQuery.ExcludeIdsCate) {
					 query.mustNot(termQuery("ListTreeCategory", "g" + cateId + "g"));
				}
			}
			
			if(newsQuery.FromDate != null) {
				query.must(rangeQuery("CreatedDate").gte(newsQuery.FromDate));
			}
			
			query.must(rangeQuery("ActivedDate").lte(Utils.AddHour(new Date(), 7)));
			query.must(termQuery("IsActived", 1));
			query.must(termQuery("IsDeleted", 0));
			query.must(bQ);
			  
			sb.from(newsQuery.PageIndex * newsQuery.PageSize).size(newsQuery.PageSize);
			sb.query(query);
			
			SearchResponse queryResults = null;
			SearchRequest searchRequest = new SearchRequest(CurrentIndexDB);
			searchRequest.source(sb);
			
			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
			if (queryTimer != null)
				queryTimer.end();

			if (parseTimer != null)
				parseTimer.end();

			var finalResult = queryResults.getHits();
			finalResult.forEach(h -> {
				try {
					var so = mapper.readValue(h.getSourceAsString(), NewsBO.class);
					result.add(so);
				} catch (Exception e) {
					Logs.LogException(e);
				}
			});		
		}
		catch(Exception e){
			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			//result.message = "Failed on Ela_SearchNews2020: " + e.toString() + ": " + e.getMessage() + " - " + trace;
			Logs.LogException(e);
		}
		
		return result;
	}
	
	/**
	 * Get views by ID and currentDay
	 *
	 * @param NewsidList
	 * @return collection.List with type is {@code NewsCount(id,views)}
	 */
	public List<NewsCount> GetNewsViewsCountByIdList(long[] NewsidList) {

		if(jedisCP == 0){
			return null;
		}
		// Key = idViews, Value = countViews
		List<NewsCount> result = new LinkedList<NewsCount>();
		// Format date by "yyyy-MM-dd"
		Date myDate = new Date();
		SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy_MM_dd");
		String dmy = dmyFormat.format(myDate);

		// ID format PV_Tracker-NewsDetail-{id}-{yyyy_MM_dd}
		List<String> ids = Arrays.stream(NewsidList).mapToObj(idView -> "PV_Tracker-NewsDetail-" + idView + "-" + dmy)
				.collect(Collectors.toList());
		// Call database
		List<String> lstViewsCount = new ArrayList<String>();
		if(jedisCP == 1){
			try(Jedis jedis = redisPoolBHX.getResource()){
				//Pipeline p = jedis.pipelined();
				jedis.select(11);
				lstViewsCount = jedis.mget(ids.toArray(new String[ids.size()]));
			}
		}else{
			lstViewsCount = rdmClient.getResource(11).mget(ids.toArray(new String[ids.size()]));
		}

		// lstViewsCount = rdmClient.getResource(11).mget(ids.toArray(new String[ids.size()]));

//		if(!DidxHelper.isLive()){
//			lstViewsCount = redisClientPoolBHX.mget(ids.toArray(new String[ids.size()]));
//		}else{
//			lstViewsCount = redisClientBHX.mget(ids.toArray(new String[ids.size()]));
//		}

		//lstViewsCount.stream().collect(Collectors.toMap(x -> x.indexOf(0), x -> x));
		var i = 0;
		for (String value : lstViewsCount) {
			if (Utils.StringIsEmpty(value)) {
				value = "0";
			}
			NewsCount viewsCount = new NewsCount();
			try {

				viewsCount.id = NewsidList[i];
				viewsCount.count = Utils.toLong(value);
			} catch (Throwable e) {
				e.printStackTrace();
				return null;
			}
			result.add(viewsCount);

			i++;
		}
		return result;
	}

	public void OffRedisNewcount(int onCP){
		jedisCP = onCP;
	}


}
