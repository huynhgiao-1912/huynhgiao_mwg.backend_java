package mwg.wb.business;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Ints;
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.elasticsearch.dataquery.DishQuery;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.LogLevel;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.comment.RatingStaticBO;
import mwg.wb.model.cook.CookCategory;
import mwg.wb.model.cook.CookDish;
import mwg.wb.model.cook.CookGallery;
import mwg.wb.model.cook.CookIngredient;
import mwg.wb.model.cook.CookRecipe;
import mwg.wb.model.cook.CookStep;
import mwg.wb.model.search.DishSO;
import mwg.wb.model.searchresult.DishSOSR;
import mwg.wb.model.searchresult.FaceObject;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.search.aggregations.AggregationBuilders.count;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;

public class CookHelper {
	private static ORThreadLocal oclient = null;
	protected ObjectMapper mapper = null;
	ClientConfig config = null;

	protected RestHighLevelClient clientIndex = null;
	private ElasticClient elasticClient = null;
	protected String CurrentIndexDB = "";

	final int CateMeoVat = 3;

	public CookHelper(ORThreadLocal afactoryRead, ClientConfig aconfig) {
		CurrentIndexDB = "ms_cook";
		oclient = afactoryRead;
		config = aconfig;
		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatStringNews);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	elasticClient = ElasticClient.getInstance(config.SERVER_ELASTICSEARCH_READ_HOST);
			//elasticClient =  new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST); 
		clientIndex = elasticClient.getClient();

	}

	public CookDish GetDishByID(long dishID) throws Throwable {
		var dishList = oclient.QueryFunction("cook_GetDishDetail", CookDish[].class, false, dishID);

		if (dishList != null && dishList.length > 0) {
			return dishList[0];
		}
		return null;

	}

	public List<CookCategory> GetAllCategory() throws Throwable {
		var cateList = oclient.QueryFunctionCached("cook_GetListCategory", CookCategory[].class, false);

		if (cateList != null && cateList.length > 0) {
			return Arrays.asList(cateList);
		}
		return null;
	}

	public List<CookRecipe> GetListRecipeByDishId(int dishID) throws Throwable {
		var recipes = oclient.QueryFunctionCached("cook_GetListRecipeByDishID", CookRecipe[].class, false, dishID);

		if (recipes != null && recipes.length > 0) {
			return Arrays.asList(recipes);
		}
		return null;
	}

	public DishSOSR SearchDishes(DishQuery dishQuery, CodeTimer esquerytimer) {
		DishSOSR result = new DishSOSR();
		try {

			SearchSourceBuilder sb = new SearchSourceBuilder();
//			sb.fetchSource(new String[] { "_score", "NewsID", "ListTreeCategory", "TermEventsID", "TermTopicID" },
//					null);

			var query = boolQuery();
			if (!Utils.StringIsEmpty(dishQuery.CategoryIdList)) {
				query.must(termsQuery("CategoryIdList", dishQuery.CategoryIdList.split(",")));

			}
			if (!Utils.StringIsEmpty(dishQuery.DishIDList)) {
				if (dishQuery.ExtensionObject == 1 || dishQuery.ExtensionObject == 2) {// lay loai tru
					query.mustNot(termsQuery("DishID", dishQuery.DishIDList.split(",")));
				}

			}
			query.must(termQuery("IsDeleted", 0));
			query.must(termQuery("IsDraft", 0));

			if (dishQuery.IsVideo > 0) {
				query.must(termQuery("IsHasVideo", 1));
			}
			if (dishQuery.IsActive > 0) {
				query.must(termQuery("IsActived", 1));
				LocalDateTime localDateTime = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				query.must(rangeQuery("ActivedDate")
						.lte(Date.from(localDateTime.plusHours(7).atZone(ZoneId.systemDefault()).toInstant())));

			}
			if (dishQuery.IsFeature > 0) {
				query.must(termQuery("IsFeatured", 1));
				// không lay bai tick duy nhat cate mẹo vặt :3
				query.must(termQuery("IsMeoVatOnly", 0));

			}

			if (!Utils.StringIsEmpty(dishQuery.Keyword)) {
				String keyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(dishQuery.Keyword));

				var kwSplit = keyword.trim().split("\\s+");
				String queryStr = "( (\"" + keyword + "\") OR (" + String.join(" AND ", kwSplit) + ") )";
				
				String queryStr1 = "( \"" + keyword + "\" )";
				String queryStr2 = "( " + String.join(" AND ", kwSplit) + " )";

				var keywordQuery = boolQuery();
//
//				var queryBuilder1 = QueryBuilders.matchPhraseQuery("Title", keyword).boost(10000f);
//				 
//				keywordQuery.should(queryBuilder1);
//
//				var queryBuilder2 = QueryBuilders.queryStringQuery(queryStr2).field("Keyword").defaultOperator(Operator.AND).defaultField("Keyword");
//				
//				
//				keywordQuery.should(queryBuilder2);
				
				var queryBuilder = QueryBuilders.boostingQuery(
						QueryBuilders.matchPhraseQuery("Title", keyword),
						QueryBuilders.queryStringQuery(queryStr2).defaultOperator(Operator.AND).defaultField("Keyword")).negativeBoost(0.2f);

				keywordQuery.should(queryBuilder);

				query.must(keywordQuery);
			}

			if (!Utils.StringIsEmpty(dishQuery.Tag)) {// lay mon an lien quan
				query.must(termQuery("Tag", DidxHelper.ConvertToTagsTerm(dishQuery.Tag)));

			}
			if (dishQuery.ExtensionObject > 0) {
				if (dishQuery.ExtensionObject == 2)// moi nhat
				{

					query.must(termQuery("IsMeoVatOnly", 0));
				}
				if (dishQuery.ExtensionObject == 3)// chi lay tin tick duy nhat cate meo vat
				{
					query.must(termQuery("IsMeoVatOnly", 1));
				}
			}
			SearchResponse queryResults = null;
			SearchRequest searchRequest = new SearchRequest(CurrentIndexDB);

			switch (dishQuery.OrderType) {
			case 1:
				sb.sort("ActivedDate", SortOrder.DESC);
				break;
			case 2:
				sb.sort("ViewCount", SortOrder.DESC);
				break;

			default:
				sb.sort("_score", SortOrder.DESC);
				break;
			}

			sb.from(dishQuery.PageIndex * dishQuery.PageSize).size(dishQuery.PageSize);
			sb.query(query);
			sb.aggregation(terms("FacetCategory").field("CategoryIdList").size(100)
					.subAggregation(count("Sum").field("DishID")));

			searchRequest.source(sb);
			if (esquerytimer != null)
				esquerytimer.reset();

			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
			if (esquerytimer != null)
				esquerytimer.end();

			var listDishO = new LinkedHashMap<Integer, DishSO>();
			List<FaceObject> faceList = new ArrayList<FaceObject>();

			var finalResult = queryResults.getHits();
			finalResult.forEach(h -> {
				try {

					var so = mapper.readValue(h.getSourceAsString(), DishSO.class);
					listDishO.put(so.DishID, so);
				} catch (Exception e) {
					Logs.LogException(e);
				}
			});

			int rowcount = (int) queryResults.getHits().getTotalHits().value;
			var _aggrs = queryResults.getAggregations();
			Map<String, Aggregation> aggrs = null;
			if (_aggrs != null) {
				aggrs = _aggrs.asMap();
			}

			result = new DishSOSR();
			result.total = rowcount;
			result.faceList = faceList;
			result.dishesSO = listDishO;

		} catch (Exception e) {

			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.message = "Failed on SearchDishes: " + e.toString() + ": " + e.getMessage() + " - "

					+ trace;
			Logs.LogException(e);

		}
		return result;
	}

	public List<CookDish> GetListDishes(List<Integer> lids) throws Throwable {
		int[] aids = Ints.toArray(lids);
		var dishes = oclient.QueryFunction("cook_GetListDish", CookDish[].class, false, aids);

		if (dishes != null && dishes.length > 0) {
			return Arrays.asList(dishes);
		}
		return null;
	}

	public List<CookStep> GetListStepByDishId(int dishID) throws Throwable {

		var stepes = oclient.QueryFunctionCached("cook_GetStepsByDishID", CookStep[].class, false, dishID);

		if (stepes != null && stepes.length > 0) {
			return Arrays.asList(stepes);
		}
		return new ArrayList<CookStep>();
	}

	public List<CookIngredient> GetListIngredientByDishId(int dishID) throws Throwable {
		var ingredients = oclient.QueryFunctionCached("cook_GetIngredientsByDishID", CookIngredient[].class, false, dishID);

		if (ingredients != null && ingredients.length > 0) {
			return Arrays.asList(ingredients);
		}
		return new ArrayList<CookIngredient>();
	}

	public DishSOSR GetRelatedDish(String cateIDList, String tag, int size, CodeTimer esquerytimer) {
		DishSOSR result = new DishSOSR();
		try {

			SearchSourceBuilder sb = new SearchSourceBuilder();
			var finalSO = new LinkedHashMap<Integer, DishSO>();
			sb.fetchSource(new String[] { "_score", "NewsID", "ListTreeCategory", "TermEventsID", "TermTopicID" },
					null);

			var orgiginQuery = boolQuery();
			var query1 = boolQuery();
			var query2 = boolQuery();

			orgiginQuery.must(termQuery("IsDeleted", 0));
			orgiginQuery.must(termQuery("IsDraft", 0));
			orgiginQuery.must(termQuery("IsActived", 1));
			LocalDateTime localDateTime = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			orgiginQuery.must(rangeQuery("ActivedDate")
					.lte(Date.from(localDateTime.plusHours(7).atZone(ZoneId.systemDefault()).toInstant())));

			SearchResponse queryResults = null;
			SearchRequest searchRequest = new SearchRequest(CurrentIndexDB);
			sb.sort("ActivedDate", SortOrder.DESC);
			sb.from(0).size(size);
			query1 = orgiginQuery;
			if (!Utils.StringIsEmpty(tag)) {

				query1.must(termQuery("Tag", DidxHelper.ConvertToTagsTerm(tag)));

			}

			sb.query(query1);
			searchRequest.source(sb);
			esquerytimer.reset();
			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
			var listByTag = GetListDishSOFromResult(queryResults.getHits());
			finalSO.putAll(listByTag);

			int rowcount = (int) queryResults.getHits().getTotalHits().value;
			if (rowcount < size) {
				query2 = orgiginQuery;
				if (!Utils.StringIsEmpty(cateIDList)) {

					query2.must(termsQuery("CategoryIdList", cateIDList.split(",")));
				}
				sb.from(0).size(size - rowcount);
				sb.query(query2);
				searchRequest.source(sb);

				queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

				var listByCate = GetListDishSOFromResult(queryResults.getHits());
				finalSO.putAll(listByCate);
			}

			esquerytimer.end();
			result = new DishSOSR();
			result.total = rowcount;
			result.dishesSO = finalSO;

		} catch (Exception e) {

			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.message = "Failed on GetRelatedDish: " + e.toString() + ": " + e.getMessage() + " - "

					+ trace;
			Logs.LogException(e);

		}
		return result;
	}

	// mo ta moi 25-09-2020
	// https://docs.google.com/document/d/132_-KcJU0k_ZOrGbN-fiFrTNg5rfg4wdqMDX08fm4mI/edit
	public DishSOSR GetRelatedDishNew(String cateIDList, int size, int dishID, CodeTimer esquerytimer) {
		DishSOSR result = new DishSOSR();
		try {

			SearchSourceBuilder sb = new SearchSourceBuilder();
			var finalSO = new LinkedHashMap<Integer, DishSO>();

			var orgiginQuery = boolQuery();

			orgiginQuery.must(termQuery("IsDeleted", 0));
			orgiginQuery.must(termQuery("IsDraft", 0));
			orgiginQuery.must(termQuery("IsActived", 1));
			LocalDateTime localDateTime = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			orgiginQuery.must(rangeQuery("ActivedDate")
					.lte(Date.from(localDateTime.plusHours(7).atZone(ZoneId.systemDefault()).toInstant())));

			orgiginQuery.must(termsQuery("CategoryIdList", cateIDList.split(",")));
			orgiginQuery.mustNot(termQuery("DishID", dishID));

			SearchResponse queryResults = null;
			SearchRequest searchRequest = new SearchRequest(CurrentIndexDB);
			sb.sort("ActivedDate", SortOrder.DESC);
			sb.from(0).size(size);

			sb.query(orgiginQuery);
			searchRequest.source(sb);
			esquerytimer.reset();
			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
			var listResult = GetListDishSOFromResult(queryResults.getHits());
			finalSO.putAll(listResult);

			int rowcount = (int) queryResults.getHits().getTotalHits().value;

			esquerytimer.end();
			result = new DishSOSR();
			result.total = rowcount;
			result.dishesSO = finalSO;

		} catch (Exception e) {

			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.message = "Failed on GetRelatedDish: " + e.toString() + ": " + e.getMessage() + " - "

					+ trace;
			Logs.LogException(e);

		}
		return result;
	}

	public HashMap<Integer, DishSO> GetListDishSOFromResult(SearchHits finalResult) {
		var listDishO = new LinkedHashMap<Integer, DishSO>();
		if (finalResult == null)
			return listDishO;
		finalResult.forEach(h -> {
			try {

				var so = mapper.readValue(h.getSourceAsString(), DishSO.class);
				listDishO.put(so.DishID, so);
			} catch (Exception e) {
				Logs.LogException(e);
			}
		});
		return listDishO;

	}

	public CookCategory GetCateInfoByUrl(String url) throws Throwable {

		var cate = oclient.QueryFunctionCached("cook_GetCategoryByURL", CookCategory[].class, false, url);

		if (cate != null && cate.length > 0) {
			return cate[0];
		}
		return null;
	}

	public List<CookGallery> GetGalleryBySteps(List<Integer> listRecipeID, List<Integer> listStepID) throws Throwable {

		int[] reids = Ints.toArray(listRecipeID);
		int[] seids = Ints.toArray(listStepID);

		var lstGallery = oclient.QueryFunction("cook_GetGalleryByListStep", CookGallery[].class, false, seids, reids);

		if (lstGallery != null && lstGallery.length > 0) {
			return Arrays.asList(lstGallery);
		}

		return new ArrayList<CookGallery>();
	}

	public RatingStaticBO GetRatingStatic(int objectID, int objectType, int siteID) throws Throwable {
		var rating = oclient.QueryFunctionCached("comment_getTotalRating", RatingStaticBO[].class, false, objectID,
				objectType, siteID);

		if (rating != null && rating.length > 0) {
			return rating[0];
		}
		return null;
	}

	public CookDish GetDishByNewsId(int newsID) throws Throwable {
		var dish = oclient.QueryFunctionCached("cook_GetDishByNewsID", CookDish[].class, false, newsID);

		if (dish != null && dish.length > 0) {
			return dish[0];
		}
		return null;
	}

	public List<CookDish> GetDishByRecipeID(int recipeID) throws Throwable {
		var stepes = oclient.QueryFunctionCached("cook_GetDishByRecipeID", CookDish[].class, false, recipeID);

		if (stepes != null && stepes.length > 0) {
			return Arrays.asList(stepes);
		}
		return new ArrayList<CookDish>();
	}

	public CookRecipe GetRecipeInfoByRecipeId(int recipeID) throws Throwable {
		var recipe = oclient.QueryFunctionCached("cook_GetRecipeByID", CookRecipe[].class, false, recipeID);

		if (recipe != null && recipe.length > 0) {
			return recipe[0];
		}
		return null;
	}
	public CookDish[] getListByCookdish(int pageSize) throws Throwable {
		if(pageSize>100)
			pageSize  = 100;
		CookDish[] cookDishes = null;
		var sb = new SearchSourceBuilder();
		sb.fetchSource("DishID",null);
		var query = boolQuery();
		query.must(termQuery("IsDeleted", 0));
		query.must(termQuery("IsDraft", 0));
		query.must(termQuery("IsActived",1));
//		LocalDateTime localDateTime = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//		query.must(rangeQuery("ActivedDate")
//				.lte(Date.from(localDateTime.plusHours(7).atZone(ZoneId.systemDefault()).toInstant())));
		query.must(rangeQuery("ActivedDate").lte(new Date()));
		sb.from(0).size(pageSize).query(query);
		sb.sort("ActivedDate",SortOrder.DESC);
		SearchRequest request = new SearchRequest(CurrentIndexDB);
		SearchResponse response = null;
		request.source(sb);
		try {
			response = clientIndex.search(request, RequestOptions.DEFAULT);
			var ids = Stream.of(response.getHits().getHits()).map( x -> {
				try {
					return mapper.readValue(x.getSourceAsString(),DishSO.class);
				} catch (IOException e) {
					e.printStackTrace();
					return  null;
				}
			}).filter(x -> x!= null).map(x -> x.DishID).collect(Collectors.toList());
			cookDishes = GetListDishes(ids).stream().toArray(CookDish[]::new);

		}catch (IOException e){
			LogHelper.WriteLog(e, LogLevel.ERROR);
			throw new ElasticsearchException("Error while searching for request: " + request.toString(), e);
		}
		return cookDishes;


	}

}
