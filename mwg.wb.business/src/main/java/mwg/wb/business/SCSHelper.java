package mwg.wb.business;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.boostingQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.count;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.directory.SearchResult;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mwg.wb.client.BooleanTypeAdapter;
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.elasticsearch.ElasticClientWriteSCS;
import mwg.wb.client.redis.RedisClient;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.scs.HashtagBO;
import mwg.wb.model.scs.HashtagSO;
import mwg.wb.model.scs.MentionBO;
import mwg.wb.model.scs.MentionSO;
import mwg.wb.model.scs.NLU;
import mwg.wb.model.scs.ResultBO;
import mwg.wb.model.scs.SearchQuery;
import mwg.wb.model.scs.TicketBO;
import mwg.wb.model.scs.TicketSO;
import mwg.wb.model.search.DishSO;
import mwg.wb.model.searchresult.DishSOSR;
import mwg.wb.model.searchresult.FaceObject;

public class SCSHelper {

	protected ObjectMapper mapper = null;
	ClientConfig config = null;

	protected RestHighLevelClient rclientIndex = null;

	private ElasticClient elasticReadClient = null;
	private ElasticClientWriteSCS elasticWriteClient = null;
	private RedisClient redisClient = null;
	protected String TicketIndexDB = "";
	protected String MentionIndexDB = "";
	protected String HastagIndexDB = "";
	protected Gson gson = null;

	public SCSHelper(ClientConfig aconfig) {
		TicketIndexDB = "scs_ticket";
		MentionIndexDB = "scs_mention";
		HastagIndexDB = "scs_hashtag";

		config = aconfig;
		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatStringNews);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		elasticReadClient = ElasticClient.getInstance(config.SERVER_ELASTICSEARCH_READ_HOST);
		elasticWriteClient = ElasticClientWriteSCS.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST);

		rclientIndex = elasticReadClient.getClient();
		redisClient = RedisClient.getInstance(config.REDIS_HOST_NLU);
		gson = new GsonBuilder().serializeNulls().setFieldNamingStrategy(f -> f.getName().toLowerCase())
				.setDateFormat(GConfig.DateFormatString).registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
				.create();
	}

	public ResultBO<Long> InsertHashTag(HashtagBO hastag) throws Throwable {
		var result = new ResultBO<Long>();

		result.Message = "success";
		result.StatusCode = 200;
		if (hastag == null || hastag.Id <= 0) {
			result.StatusCode = 400;
			result.Message = "data input wrong";
			return result;
		}
		// check if is exist
		var preHastag = elasticReadClient.GetSingleObjectNew(HastagIndexDB, String.valueOf(hastag.Id), HashtagSO.class);
		if (preHastag != null && preHastag.Id > 0) {
			result.StatusCode = 400;
			result.Message = "hastag already exist, please use update method";
			return result;
		}

		HashtagSO _hashtag = new HashtagSO();
		_hashtag.Id = hastag.Id;
		_hashtag.Description = hastag.Description;
		_hashtag.IsDeleted = hastag.IsDeleted;
		_hashtag.Object = hastag.Object;
		_hashtag.ObjectId = hastag.ObjectId;
		_hashtag.LastedUpdate = new Date();

		_hashtag.Type = hastag.Type;
		_hashtag.Hashtag = hastag.Hashtag;
		_hashtag.IsActived = 1;
		if (!Utils.StringIsEmpty(hastag.Hashtag)) {
			_hashtag.HashtagTerm = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(hastag.Hashtag));
		}

		_hashtag.SearchText = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(hastag.SearchText));

		var rs = elasticWriteClient.IndexObjectNew(HastagIndexDB, _hashtag, _hashtag.Id + "");
		if (rs <= 0) {
			result.Message = "failed";
			result.StatusCode = 500;
		}
		result.Result = rs;// result version document
		return result;
	}

	public ResultBO<Long> UpdateHashTag(HashtagBO hastag, long id) throws Throwable {
		var result = new ResultBO<Long>();

		result.Message = "success";
		result.StatusCode = 200;
		if (hastag == null || id <= 0) {
			result.StatusCode = 400;
			result.Message = "data input wrong";
			return result;
		}
		// check if is exist
		var preHastag = elasticReadClient.GetSingleObjectNew(HastagIndexDB, String.valueOf(id), HashtagSO.class);
		if (preHastag == null) {
			result.StatusCode = 400;
			result.Message = "hastag not exist, please insert first";
			return result;
		}
//		if (preHastag.Version != hastag.Version) {
//			result.StatusCode = 400;
//			result.Message = "version document conflict";
//			return result;
//		}

		preHastag.Description = hastag.Description;
		preHastag.IsDeleted = hastag.IsDeleted;
		preHastag.Object = hastag.Object;
		preHastag.ObjectId = hastag.ObjectId;
		preHastag.LastedUpdate = new Date();

		preHastag.Type = hastag.Type;
		preHastag.Hashtag = hastag.Hashtag;
		if (!Utils.StringIsEmpty(hastag.Hashtag)) {
			preHastag.HashtagTerm = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(hastag.Hashtag));
		}

		preHastag.SearchText = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(hastag.SearchText));

		var rs = elasticWriteClient.UpdateObjectNew(HastagIndexDB, preHastag, id + "");
		if (rs <= 0) {
			result.Message = "failed";
			result.StatusCode = 500;
		}
		result.Result = rs;// result version document
		return result;
	}

	public ResultBO<HashtagBO> GetHashTag(long id) {
		var result = new ResultBO<HashtagBO>();
		var preHastag = elasticReadClient.GetSingleObjectNew(HastagIndexDB, String.valueOf(id), HashtagBO.class);

		if (preHastag != null && preHastag.IsDeleted == 1)
			preHastag = null;
		result.Result = preHastag;
		result.StatusCode = 200;
		result.Message = "success";

		return result;
	}

	public ResultBO<Long> InsertMention(MentionBO mention) throws Throwable {
		var result = new ResultBO<Long>();

		result.Message = "success";
		result.StatusCode = 200;
		if (mention == null || mention.Id <= 0) {
			result.StatusCode = 400;
			result.Message = "data input wrong";
			return result;
		}
		// check if is exist
		var preMention = elasticReadClient.GetSingleObjectNew(MentionIndexDB, String.valueOf(mention.Id),
				MentionSO.class);
		if (preMention != null && preMention.Id > 0) {
			result.StatusCode = 400;
			result.Message = "mention already exist, please use update method";
			return result;
		}

		MentionSO _mention = new MentionSO();
		_mention.Id = mention.Id;
		_mention.Description = mention.Description;
		_mention.Hashtags = mention.Hashtags;
		_mention.IsActived = 1;
		_mention.LastedUpdate = new Date();
		_mention.Mention = mention.Mention;
		_mention.ObjectId = mention.ObjectId;
		_mention.Object = mention.Object;
		_mention.IsDeleted = mention.IsDeleted;
		if (!Utils.StringIsEmpty(mention.Mention)) {
			_mention.MentionTerm = DidxHelper
					.GenTerm(DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(mention.Mention)));

		}

		if (!Utils.StringIsEmpty(mention.Hashtags)) {
			_mention.HashtagTerm = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(mention.Hashtags));
		}

		_mention.SearchText = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(mention.SearchText));

		var rs = elasticWriteClient.IndexObjectNew(MentionIndexDB, _mention, _mention.Id + "");
		if (rs <= 0) {
			result.Message = "failed";
			result.StatusCode = 500;
		}
		result.Result = rs;// result version document
		return result;
	}

	public ResultBO<Long> UpdateMention(MentionBO mention, long id) throws Throwable {
		var result = new ResultBO<Long>();

		result.Message = "success";
		result.StatusCode = 200;
		if (mention == null || id <= 0) {
			result.StatusCode = 400;
			result.Message = "data input wrong";
			return result;
		}
		// check if is exist
		var preMention = elasticReadClient.GetSingleObjectNew(MentionIndexDB, String.valueOf(id), MentionSO.class);
		if (preMention == null) {
			result.StatusCode = 400;
			result.Message = "mention not exist, please insert first";
			return result;
		}
//		if (preMention.Version != mention.Version) {
//			result.StatusCode = 400;
//			result.Message = "version document conflict";
//			return result;
//		}

		preMention.Description = mention.Description;
		preMention.IsDeleted = mention.IsDeleted;
		preMention.Object = mention.Object;
		preMention.ObjectId = mention.ObjectId;
		preMention.LastedUpdate = new Date();
		preMention.Type = mention.Type;
		preMention.Hashtags = mention.Hashtags;
		preMention.Mention = mention.Mention;

		if (!Utils.StringIsEmpty(mention.Hashtags)) {
			preMention.HashtagTerm = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(mention.Hashtags));
		}
		if (!Utils.StringIsEmpty(mention.Mention)) {
			preMention.MentionTerm = DidxHelper
					.GenTerm(DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(mention.Mention)));
		}

		preMention.SearchText = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(mention.SearchText));

		var rs = elasticWriteClient.UpdateObjectNew(MentionIndexDB, preMention, id + "");
		if (rs <= 0) {
			result.Message = "failed";
			result.StatusCode = 500;
		}
		result.Result = rs;// result version document
		return result;
	}

	public ResultBO<MentionBO> GetMention(long id) {
		var result = new ResultBO<MentionBO>();
		var mention = elasticReadClient.GetSingleObjectNew(MentionIndexDB, String.valueOf(id), MentionBO.class);

		if (mention != null && mention.IsDeleted == 1)
			mention = null;
		result.Result = mention;
		result.StatusCode = 200;
		result.Message = "success";

		return result;
	}

	public ResultBO<Long> UpdateHashtagInMention(String[] hashtags, long id) throws Throwable {
		var result = new ResultBO<Long>();

		result.Message = "success";
		result.StatusCode = 200;
		if (id <= 0) {
			result.StatusCode = 400;
			result.Message = "id not valid";
			return result;
		}
		// check if is exist
		var preMention = elasticReadClient.GetSingleObjectNew(MentionIndexDB, String.valueOf(id), MentionSO.class);
		if (preMention == null) {
			result.StatusCode = 400;
			result.Message = "mention not exist, please insert first";
			return result;
		}
		if (hashtags != null && hashtags.length > 0) {
			preMention.Hashtags = String.join(" ", hashtags);
			preMention.HashtagTerm = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(preMention.Hashtags));

		} else {
			preMention.Hashtags = "";
			preMention.HashtagTerm = "";
		}
		preMention.LastedUpdate = new Date();

		var rs = elasticWriteClient.UpdateObjectNew(MentionIndexDB, preMention, id + "");
		if (rs <= 0) {
			result.Message = "failed";
			result.StatusCode = 500;
		}
		result.Result = rs;// result version document
		return result;
	}

	public ResultBO<List<MentionBO>> SearchMentions(SearchQuery searchQuery, CodeTimer estimer) {

		var result = new ResultBO<List<MentionBO>>();
		try {

			SearchSourceBuilder sb = new SearchSourceBuilder();

			var query = boolQuery();

			query.must(termQuery("IsDeleted", 0));
			if (searchQuery.Ids != null && searchQuery.Ids.length > 0) {
				query.must(termsQuery("Id", searchQuery.Ids));
			}
			if (searchQuery.Type > 0) {
				query.must(termQuery("Type", searchQuery.Type));
			}
			if (searchQuery.ObjectId > 0) {
				query.must(termQuery("ObjectId", searchQuery.ObjectId));
			}

			if (!Utils.StringIsEmpty(searchQuery.Keyword)) {
				String keyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(searchQuery.Keyword));
				query.must(boostingQuery(
						queryStringQuery(keyword).field("SearchText").boost(5f).defaultOperator(Operator.AND),
						QueryBuilders.matchAllQuery()).negativeBoost(0.2f));
			}

			if (searchQuery.Hashtags != null && searchQuery.Hashtags.length > 0) {
				var listTag = new ArrayList<String>();
				for (var tag : searchQuery.Hashtags) {
					listTag.add(DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(tag)));
				}
				query.must(termsQuery("HashtagTerm", listTag));

			}

			if (searchQuery.Mentions != null && searchQuery.Mentions.length > 0) {
				var listMention = new ArrayList<String>();
				for (var metion : searchQuery.Mentions) {
					listMention.add(DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(metion)));
				}
				query.must(termsQuery("MentionTerm", listMention));

			}

			SearchResponse queryResults = null;
			SearchRequest searchRequest = new SearchRequest(MentionIndexDB);

			sb.sort("_score", SortOrder.DESC);

			sb.from(searchQuery.PageIndex * searchQuery.PageSize).size(searchQuery.PageSize);
			sb.query(query);

			searchRequest.source(sb);
			estimer.reset();

			queryResults = rclientIndex.search(searchRequest, RequestOptions.DEFAULT);
			estimer.end();

			var searchResult = new ArrayList<MentionBO>();

			var finalResult = queryResults.getHits();
			finalResult.forEach(h -> {
				try {

					var so = mapper.readValue(h.getSourceAsString(), MentionBO.class);
					so.Version = h.getVersion();
					searchResult.add(so);
				} catch (Throwable e) {
					Logs.LogException(e);
				}
			});

			int rowcount = (int) queryResults.getHits().getTotalHits().value;

			result.Total = rowcount;
			result.Result = searchResult;
			result.StatusCode = 200;
			result.Message = "success";

		} catch (Exception e) {

			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.Message = "Failed on SearchMentions: " + e.toString() + ": " + e.getMessage() + " - "

					+ trace;
			Logs.LogException(e);

		}
		return result;
	}

	public ResultBO<Long> InsertTicket(TicketBO ticket) throws Throwable {

		var result = new ResultBO<Long>();

		result.Message = "success";
		result.StatusCode = 200;
		if (ticket == null || ticket.Id <= 0) {
			result.StatusCode = 400;
			result.Message = "data input wrong";
			return result;
		}
		// check if is exist
		var preTicket = elasticReadClient.GetSingleObjectNew(TicketIndexDB, String.valueOf(ticket.Id), TicketBO.class);
		if (preTicket != null && preTicket.Id > 0) {
			result.StatusCode = 400;
			result.Message = "ticket already exist, please use update method";
			return result;
		}

		TicketSO _ticket = new TicketSO();
		_ticket.Id = ticket.Id;
		_ticket.Content = ticket.Content;
		_ticket.IsDeleted = ticket.IsDeleted;
		_ticket.CreatedDate = ticket.CreatedDate;
		_ticket.LastedUpdate = new Date();

		_ticket.CreatedBy = ticket.CreatedBy;
		if (!Utils.StringIsEmpty(_ticket.Content)) {
			_ticket.Keyword = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(ticket.Content));
		}

		if (ticket.HashTags != null && ticket.HashTags.length > 0) {
			_ticket.HashtagTerm = Arrays.asList(ticket.HashTags).stream().map(x -> String.valueOf(x.Id))
					.collect(Collectors.joining(" "));

			_ticket.HashTags = GetListHashTagByTerm(_ticket.HashtagTerm);

		}
		if (ticket.Mentions != null && ticket.Mentions.length > 0) {
			_ticket.MentionTerm = Arrays.asList(ticket.Mentions).stream().map(x -> String.valueOf(x.Id))
					.collect(Collectors.joining(" "));

			_ticket.Mentions = GetListMentionsByTerm(_ticket.MentionTerm);
		}

		_ticket.IsActived = 1;

		var rs = elasticWriteClient.IndexObjectNew(TicketIndexDB, _ticket, _ticket.Id + "");
		if (rs <= 0) {
			result.Message = "failed";
			result.StatusCode = 500;
		}
		result.Result = rs;// result version document
		return result;

	}

	private HashtagBO[] GetListHashTagByTerm(String term) throws Throwable {

		var query = boolQuery();

		query.must(termsQuery("Id", term.split(" ")));
		query.must(termQuery("IsDeleted", 0));
		SearchSourceBuilder sb = new SearchSourceBuilder();
		sb.sort("_score", SortOrder.DESC);

		sb.from(0).size(term.split(" ").length);
		sb.query(query);
		var searchRequest = new SearchRequest(HastagIndexDB);
		searchRequest.source(sb);

		var queryResults = rclientIndex.search(searchRequest, RequestOptions.DEFAULT);

		var searchResult = new ArrayList<HashtagBO>();

		var finalResult = queryResults.getHits();
		finalResult.forEach(h -> {
			try {

				var so = mapper.readValue(h.getSourceAsString(), HashtagBO.class);
				so.Version = h.getVersion();
				searchResult.add(so);

			} catch (Throwable e) {
				System.out.println(e.toString() + "-" + e.getMessage());
			}
		});

		return searchResult.toArray(new HashtagBO[searchResult.size()]);

	}

	private MentionBO[] GetListMentionsByTerm(String term) throws Throwable {

		var query = boolQuery();

		query.must(termsQuery("Id", term.split(" ")));
		query.must(termQuery("IsDeleted", 0));
		SearchSourceBuilder sb = new SearchSourceBuilder();
		sb.sort("_score", SortOrder.DESC);

		sb.from(0).size(term.split(" ").length);
		sb.query(query);
		var searchRequest = new SearchRequest(MentionIndexDB);
		searchRequest.source(sb);

		var queryResults = rclientIndex.search(searchRequest, RequestOptions.DEFAULT);

		var searchResult = new ArrayList<MentionBO>();

		var finalResult = queryResults.getHits();
		finalResult.forEach(h -> {
			try {

				var so = mapper.readValue(h.getSourceAsString(), MentionBO.class);
				so.Version = h.getVersion();
				searchResult.add(so);
			} catch (Throwable e) {
				System.out.println(e.toString() + "-" + e.getMessage());
			}
		});

		return searchResult.toArray(new MentionBO[searchResult.size()]);

	}

	public ResultBO<TicketBO> GetTicket(long id) throws Throwable {
		var result = new ResultBO<TicketBO>();
		var ticket = elasticReadClient.GetSingleObjectNew(TicketIndexDB, String.valueOf(id), TicketSO.class);

		if (ticket != null && ticket.IsDeleted == 1)
			ticket = null;
		result.Result = ticket;
		result.StatusCode = 200;
		result.Message = "success";

		return result;
	}

	public ResultBO<Long> UpdateTicket(TicketBO ticket, long id) throws Throwable {

		var result = new ResultBO<Long>();

		result.Message = "success";
		result.StatusCode = 200;
		if (ticket == null || id <= 0) {
			result.StatusCode = 400;
			result.Message = "data input wrong";
			return result;
		}
		// check if is exist
		var preTicket = elasticReadClient.GetSingleObjectNew(TicketIndexDB, String.valueOf(id), TicketSO.class);
		if (preTicket == null) {
			result.StatusCode = 400;
			result.Message = "mention not exist, please insert first";
			return result;
		}
//		if (preTicket.Version != ticket.Version) {
//			result.StatusCode = 400;
//			result.Message = "version document conflict";
//			return result;
//		}

		preTicket.Content = ticket.Content;
		preTicket.IsDeleted = ticket.IsDeleted;
		preTicket.CreatedDate = ticket.CreatedDate;
		preTicket.LastedUpdate = new Date();

		preTicket.CreatedBy = ticket.CreatedBy;
		if (!Utils.StringIsEmpty(ticket.Content)) {
			preTicket.Keyword = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(ticket.Content));
		}

		if (ticket.HashTags != null && ticket.HashTags.length > 0) {
			preTicket.HashtagTerm = Arrays.asList(ticket.HashTags).stream().map(x -> String.valueOf(x.Id))
					.collect(Collectors.joining(" "));
			preTicket.HashTags = GetListHashTagByTerm(preTicket.HashtagTerm);

		} else {
			preTicket.HashtagTerm = null;
		}
		if (ticket.Mentions != null && ticket.Mentions.length > 0) {
			preTicket.MentionTerm = Arrays.asList(ticket.Mentions).stream().map(x -> String.valueOf(x.Id))
					.collect(Collectors.joining(" "));

			preTicket.Mentions = GetListMentionsByTerm(preTicket.MentionTerm);

		} else {
			preTicket.MentionTerm = null;
		}

		var rs = elasticWriteClient.UpdateObjectNew(TicketIndexDB, preTicket, id + "");
		if (rs <= 0) {
			result.Message = "failed";
			result.StatusCode = 500;
		}
		result.Result = rs;// result version document
		return result;
	}

	public ResultBO<Long> UpdateTicketHashtags(HashtagBO[] hashtag, long id) throws Throwable {

		var result = new ResultBO<Long>();

		result.Message = "success";
		result.StatusCode = 200;
		if (id <= 0) {
			result.StatusCode = 400;
			result.Message = "id not valid";
			return result;
		}
		// check if is exist
		var preTicket = elasticReadClient.GetSingleObjectNew(TicketIndexDB, String.valueOf(id), TicketSO.class);
		if (preTicket == null) {
			result.StatusCode = 400;
			result.Message = "ticket not exist, please insert first";
			return result;
		}
		if (hashtag != null && hashtag.length > 0) {
			preTicket.HashtagTerm = Arrays.asList(hashtag).stream().map(x -> String.valueOf(x.Id))
					.collect(Collectors.joining(" "));

			preTicket.HashTags = GetListHashTagByTerm(preTicket.HashtagTerm);

		} else {
			preTicket.HashtagTerm = "";
		}

		preTicket.LastedUpdate = new Date();

		var rs = elasticWriteClient.UpdateObjectNew(TicketIndexDB, preTicket, id + "");
		if (rs <= 0) {
			result.Message = "failed";
			result.StatusCode = 500;
		}
		result.Result = rs;// result version document
		return result;
	}

	public ResultBO<Long> UpdateTicketMentions(MentionBO[] mentions, long id) throws Throwable {
		var result = new ResultBO<Long>();

		result.Message = "success";
		result.StatusCode = 200;
		if (id <= 0) {
			result.StatusCode = 400;
			result.Message = "id not valid";
			return result;
		}
		// check if is exist
		var preTicket = elasticReadClient.GetSingleObjectNew(TicketIndexDB, String.valueOf(id), TicketSO.class);
		if (preTicket == null) {
			result.StatusCode = 400;
			result.Message = "ticket not exist, please insert first";
			return result;
		}
		if (mentions != null && mentions.length > 0) {
			preTicket.MentionTerm = Arrays.asList(mentions).stream().map(x -> String.valueOf(x.Id))
					.collect(Collectors.joining(" "));
			preTicket.Mentions = GetListMentionsByTerm(preTicket.MentionTerm);

		} else {
			preTicket.MentionTerm = "";
		}

		preTicket.LastedUpdate = new Date();

		var rs = elasticWriteClient.UpdateObjectNew(TicketIndexDB, preTicket, id + "");
		if (rs <= 0) {
			result.Message = "failed";
			result.StatusCode = 500;
		}
		result.Result = rs;// result version document
		return result;
	}

	public ResultBO<List<TicketBO>> SearchTickets(SearchQuery searchQuery, CodeTimer estimer) {

		var result = new ResultBO<List<TicketBO>>();
		try {

			SearchSourceBuilder sb = new SearchSourceBuilder();

			var query = boolQuery();
			query.must(termQuery("IsDeleted", 0));
			if (searchQuery.Ids != null && searchQuery.Ids.length > 0) {
				query.must(termsQuery("Id", searchQuery.Ids));
			}
			if (searchQuery.Type > 0) {
				query.must(termQuery("Type", searchQuery.Type));
			}
			if (searchQuery.ObjectId > 0) {
				query.must(termQuery("ObjectId", searchQuery.ObjectId));
			}

			if (!Utils.StringIsEmpty(searchQuery.Keyword)) {
				String keyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(searchQuery.Keyword));
				query.must(boostingQuery(
						queryStringQuery(keyword).field("Keyword").boost(5f).defaultOperator(Operator.AND),
						QueryBuilders.matchAllQuery()).negativeBoost(0.2f));
			}

			if (searchQuery.Hashtags != null && searchQuery.Hashtags.length > 0) {

				query.must(termsQuery("HashtagTerm", searchQuery.Hashtags));

			}

			if (searchQuery.Mentions != null && searchQuery.Mentions.length > 0) {

				query.must(termsQuery("MentionTerm", searchQuery.Mentions));

			}

			SearchResponse queryResults = null;
			SearchRequest searchRequest = new SearchRequest(TicketIndexDB);

			sb.sort("_score", SortOrder.DESC);

			sb.from(searchQuery.PageIndex * searchQuery.PageSize).size(searchQuery.PageSize);
			sb.query(query);

			searchRequest.source(sb);
			estimer.reset();

			queryResults = rclientIndex.search(searchRequest, RequestOptions.DEFAULT);
			estimer.end();

			var searchResult = new ArrayList<TicketBO>();

			var finalResult = queryResults.getHits();
			finalResult.forEach(h -> {
				try {

					var so = mapper.readValue(h.getSourceAsString(), TicketBO.class);
					so.Version = h.getVersion();
					searchResult.add(so);
				} catch (Throwable e) {
					Logs.LogException(e);
				}
			});

			int rowcount = (int) queryResults.getHits().getTotalHits().value;

			result.Total = rowcount;
			result.Result = searchResult;
			result.StatusCode = 200;
			result.Message = "success";

		} catch (Exception e) {

			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.Message = "Failed on SearchTickets: " + e.toString() + ": " + e.getMessage() + " - "

					+ trace;
			Logs.LogException(e);

		}
		return result;
	}

	public ResultBO<Long> InsertNLUData(String value, String intent) throws Throwable {
		ResultBO resultBO = new ResultBO<Long>();

		if (Utils.StringIsEmpty(intent) || Utils.StringIsEmpty(value)) {
			resultBO.Message = "wrong input data";
			resultBO.StatusCode = 400;
			resultBO.Result = -1;
			return resultBO;
		}
		String key = "REDIS_LOGS_KEY_" + new Date().getTime();
		NLU nlu = new NLU();

		nlu.intent = DidxHelper.FormatKeywordField(intent);
		nlu.key = key;
		nlu.sourcefrom = "SCS";
		nlu.text = value;

		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(tz);

		nlu.time = df.format(new Date());
		nlu.status = 3;

		String nluJSON = gson.toJson(nlu);
		redisClient.choiceDB(9);
		String result = redisClient.Set(key, nluJSON);

		if (result.equals("OK")) {
			resultBO.Message = "success";
			resultBO.StatusCode = 200;
			resultBO.Result = 1;
		}
		return resultBO;

	}

	public ResultBO<List<HashtagBO>> SearchHashTags(SearchQuery searchQuery, CodeTimer estimer) {

		var result = new ResultBO<List<HashtagBO>>();
		try {

			SearchSourceBuilder sb = new SearchSourceBuilder();
			var query = boolQuery();

			query.must(termQuery("IsDeleted", 0));
			if (searchQuery.Ids != null && searchQuery.Ids.length > 0) {
				query.must(termsQuery("Id", searchQuery.Ids));
			}
			if (searchQuery.Type > 0) {
				query.must(termQuery("Type", searchQuery.Type));
			}
			if (searchQuery.ObjectId > 0) {
				query.must(termQuery("ObjectId", searchQuery.ObjectId));
			}

			if (!Utils.StringIsEmpty(searchQuery.Keyword)) {
				String keyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(searchQuery.Keyword));
				query.must(boostingQuery(
						queryStringQuery(keyword).field("SearchText").boost(5f).defaultOperator(Operator.AND),
						QueryBuilders.matchAllQuery()).negativeBoost(0.2f));
			}

			if (searchQuery.Hashtags != null && searchQuery.Hashtags.length > 0) {
				var listTag = new ArrayList<String>();
				for (var tag : searchQuery.Hashtags) {
					listTag.add(DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(tag)));
				}
				query.must(termsQuery("HashtagTerm", listTag));

			}

			SearchResponse queryResults = null;
			SearchRequest searchRequest = new SearchRequest(HastagIndexDB);
			if (!Utils.StringIsEmpty(searchQuery.Keyword)) {
				sb.sort("_score", SortOrder.DESC);
			}
			else {
				sb.sort("Id", SortOrder.ASC);
			}

			sb.from(searchQuery.PageIndex * searchQuery.PageSize).size(searchQuery.PageSize);
			sb.query(query);

			searchRequest.source(sb);
			estimer.reset();

			queryResults = rclientIndex.search(searchRequest, RequestOptions.DEFAULT);
			estimer.end();

			var searchResult = new ArrayList<HashtagBO>();

			var finalResult = queryResults.getHits();
			finalResult.forEach(h -> {
				try {

					var so = mapper.readValue(h.getSourceAsString(), HashtagBO.class);
					so.Version = h.getVersion();
					searchResult.add(so);
				} catch (Throwable e) {
					Logs.LogException(e);
				}
			});

			int rowcount = (int) queryResults.getHits().getTotalHits().value;

			result.Total = rowcount;
			result.Result = searchResult;
			result.StatusCode = 200;
			result.Message = "success";

		} catch (Exception e) {

			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.Message = "Failed on SearchHashTags: " + e.toString() + ": " + e.getMessage() + " - "

					+ trace;
			Logs.LogException(e);

		}
		return result;
	}
}
