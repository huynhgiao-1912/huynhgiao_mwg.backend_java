package mwg.wb.business;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.prefixQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.api.SimBOApi;
import mwg.wb.model.general.DistrictBO;
import mwg.wb.model.general.ProvinceBO;
import mwg.wb.model.pm.StoreBO;
import mwg.wb.model.search.SimSO;
import mwg.wb.model.sim.GroupBO;
import mwg.wb.model.sim.NetworkBO;
import mwg.wb.model.sim.SimBO;
import mwg.wb.model.sim.SimBOListSR;
import mwg.wb.model.sim.SimPackageBO;
import mwg.wb.model.sim.SimPackageErpBO;
import mwg.wb.model.sim.SimQuery;
import mwg.wb.model.sim.SimStoreListSR;

public class SimHelper {
	private ORThreadLocal oclient;
	private RestHighLevelClient clientIndex = null;
	private final static String CurrentIndexDB = "ms_sim";
	private ObjectMapper mapper = null;

	public SimHelper(ClientConfig aconfig) {

		oclient = APIOrientClient.GetOrientClient(aconfig);
		// var elasticClient = new
		// ElasticClient(aconfig.SERVER_ELASTICSEARCH_READ_HOST);
		clientIndex = ElasticClient.getInstance(aconfig.SERVER_ELASTICSEARCH_READ_HOST).getClient();

		// clientIndex = elasticClient.getClient();

		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public SimBO getSimDetail(String imei) throws Throwable {
		var timer = new CodeTimer("timer-all");
		var sim = oclient.QueryFunction("sim_GetByImei", SimBO[].class, false, imei)[0];
		timer.end();
		return sim;
	}

	public SimBOListSR SearchSim(SimQuery simquery, CodeTimer timer, CodeTimer es, CodeTimer odb) throws Throwable {
		var simso = simquery.query;
		var q = boolQuery();
		if (!Strings.isEmpty(simso.ProductNO))
			q.must(termQuery("ProductNO", simso.ProductNO));
		if (!Strings.isEmpty(simso.GroupId))
			q.must(termQuery("GroupId", "g" + simso.GroupId + "g"));
		if (simso.NetworkId > 0)
			q.must(termQuery("NetworkId", simso.NetworkId));
		if (!Strings.isEmpty(simso.StoreID))
			q.must(termQuery("StoreID", "t" + simso.StoreID + "t"));
		if (simso.SubgroupId > 0)
			q.must(termQuery("SubgroupId", simso.SubgroupId));
		if (simso.MainGoupID > 0)
			q.must(termQuery("MainGoupID", simso.MainGoupID));
		if (simso.SimLength > 0)
			q.must(termQuery("SimLength", simso.SimLength));
		q.must(termQuery("StoreID", "t25t"));
		q.must(termQuery("IsDeleted", 0));
		q.must(termQuery("LanguageID", "vi_vn"));
		// = 1 -> lẤY SIM 3G
		// = 2 -> Sim thường
		// <=0 -> lay het
		if (simso.Is3G > 0)
			q.must(termQuery("Is3G", simso.Is3G));
		if (!Strings.isEmpty(simso.Keyword_us)) { // 01632, 01636, 01664, 01633, 01639
			var q2 = boolQuery();
			Arrays.stream(simso.Keyword_us.split(",")).filter(s -> !Strings.isEmpty(s))
					.forEach(s -> q2.should(prefixQuery("SimNo", s.trim())));
			q.must(q2);
		}
		q.must(rangeQuery("PriceByPackage.backage_" + simquery.packageID).gt(1));
		if (simso.PriceFrom == 0 && simso.PriceTo > 0) // gia nho hon
			q.must(rangeQuery("Price").lte(simso.PriceTo));
		else if (simso.PriceFrom > 0 && simso.PriceTo >= simso.PriceFrom) // trong khoang
			q.must(rangeQuery("Price").gte(simso.PriceFrom).lte(simso.PriceTo));
		else if (simso.PriceFrom > 0 && simso.PriceTo == 0)
			q.must(rangeQuery("Price").gte(simso.PriceFrom));
		if (!Strings.isEmpty(simso.Keyword))
			q.must(wildcardQuery("Keyword", simso.Keyword));
		SearchSourceBuilder sb = new SearchSourceBuilder();
		sb.query(q).from(simquery.pageSize * simquery.pageIndex).size(simquery.pageSize)
				.sort("MainGoupID", SortOrder.DESC).fetchSource("SimNo", null);
		if (simquery.sortBy == 5)
			sb.sort("PriceByPackage.backage_" + simquery.packageID, SortOrder.ASC);
		else if (simquery.sortBy == 1 && simquery.sortType == 1)
			sb.sort("Price", SortOrder.DESC);
		else if (simquery.sortBy == 1 && simquery.sortType == 2)
			sb.sort("Price", SortOrder.ASC);
		else if (simquery.sortBy == 2 && simquery.sortType == 2)
			sb.sort("PriceByPackage.backage_" + simquery.packageID, SortOrder.ASC);
		else
			sb.sort("PriceByPackage.backage_" + simquery.packageID, SortOrder.DESC);
		SearchRequest sr = new SearchRequest(CurrentIndexDB).source(sb);
		var result = new SimBOListSR();
		List<String> ids;
		try {
			es.reset();
			var qr = clientIndex.search(sr, RequestOptions.DEFAULT);
			es.end();
			ids = Arrays.stream(qr.getHits().getHits()).map(h -> {
				try {
					return mapper.readValue(h.getSourceAsString(), SimSO.class).SimNo;
				} catch (IOException e) {
					return null;
				}
			}).collect(Collectors.toList());
			odb.reset();
			result.sim = oclient.QueryFunction("sim_GetByImeiList", SimBOApi[].class, false, ids);
			odb.end();
			result.message = "Success";
			result.total = qr.getHits().getTotalHits().value;
		} catch (Exception e) {
			result.message = e.getMessage();
		}
		timer.end();
		return result;
	}

	public SimPackageBO[] GetSimPackage(int groupID) throws Throwable {
		return oclient.queryFunction("sim_GetAll", SimPackageBO[].class, groupID);
	}

	public SimPackageErpBO[] GetSimPackageErp(int brandID) throws Throwable {
		SimPackageErpBO[] list;
		if (brandID > 0)
			list = oclient.QueryFunction("sim_GetPackagesErpByBrandID", SimPackageErpBO[].class, false, brandID);
		else
			list = oclient.QueryFunction("sim_GetPackagesErp", SimPackageErpBO[].class, false);
		return list;
	}

	public SimBOListSR SearchCam(final SimQuery simquery, CodeTimer timer, CodeTimer es, CodeTimer odb)
			throws Throwable {
		SimSO simso = simquery.query;
		SimBOListSR result = new SimBOListSR();

		if (simquery.pageSize < 1)
			simquery.pageSize = 10;

		final int pageSize = simquery.pageSize;
		final int pageIndex = simquery.pageIndex;

		String keyword = DidxHelper.FormatKeywordSearchFieldCam(simso.Keyword);

		if (pageIndex * pageSize > 10000)
			return result;

		var boolSearchQuery = boolQuery();

		if (!Strings.isNullOrEmpty(simso.GroupId)) {
			boolSearchQuery.must(termQuery("GroupId", "g" + simso.GroupId + "g"));
		}
		if (simso.NetworkId >= 0) {
			boolSearchQuery.must(termQuery("NetworkId", simso.NetworkId));
		}
		if (!Strings.isNullOrEmpty(simso.StoreID)) {
			boolSearchQuery.must(termQuery("StoreID", "t" + simso.StoreID + "t"));
		}
		if (simso.SubgroupId > 0) {
			boolSearchQuery.must(termQuery("SubgroupId", simso.SubgroupId));
		}
		if (simso.MainGoupID > 0) {
			boolSearchQuery.must(termQuery("MainGoupID", simso.MainGoupID));
		}
		if (simso.SimLength > 0) {
			boolSearchQuery.must(termQuery("SimLength", simso.SimLength));
		}

		boolSearchQuery.must(termQuery("LanguageID", "km_kh"));

		// = 1 -> lẤY SIM 3G
		// = 2 -> Sim thường
		// <=0 -> lay het
		if (simso.Is3G > 0) {
			boolSearchQuery.must(termQuery("Is3G", simso.Is3G));
		}
		if (!Strings.isNullOrEmpty(simso.Keyword_us)) // 01632, 01636, 01664, 01633, 01639
		{
			var boolSecondSearchQuery = boolQuery();
			String[] ds = simso.Keyword_us.replace(",", " ").trim().split("\\s+");
			for (String item : ds) {
				boolSecondSearchQuery.should(prefixQuery("SimNo", item));
			}
			boolSearchQuery.must(boolSecondSearchQuery);
		}
		if (simso.PriceFrom == 0 && simso.PriceTo > 0) {
			boolSearchQuery.must(rangeQuery("Price").lte(simso.PriceTo));
		} else if (simso.PriceTo >= simso.PriceFrom && simso.PriceFrom > 0)// trong khoan
		{
			boolSearchQuery.must(rangeQuery("Price").gte(simso.PriceFrom).lte(simso.PriceTo));
		} else if (simso.PriceFrom > 0 && simso.PriceTo == 0) // lon hon
		{
			boolSearchQuery.must(rangeQuery("Price").gte(simso.PriceFrom));
		}

		if (!Strings.isNullOrEmpty(keyword)) {
			boolSearchQuery.must(wildcardQuery("Keyword", keyword));
		}
		SearchSourceBuilder searchBuilder = new SearchSourceBuilder();
		searchBuilder.query(boolSearchQuery).from(pageSize * pageIndex).size(pageSize)
				.sort("MainGoupID", SortOrder.DESC).fetchSource("SimNo", null);
		if (simquery.sortBy == 5)
			searchBuilder.sort("Price", SortOrder.ASC);
		else if (simquery.sortBy == 1 && simquery.sortType == 1)
			searchBuilder.sort("Price", SortOrder.DESC);
		else if (simquery.sortBy == 1 && simquery.sortType == 2)
			searchBuilder.sort("Price", SortOrder.ASC);
		else if (simquery.sortBy == 2 && simquery.sortType == 2)
			searchBuilder.sort("Price", SortOrder.ASC);
		else
			searchBuilder.sort("Price", SortOrder.DESC);

		// sortBy =1 theo gia ,2 update date
		// sortType=1 giam dan, 2 tang dan
		SearchRequest searchRequest = new SearchRequest(CurrentIndexDB).source(searchBuilder);

		es.reset();
		SearchResponse searchResponse = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
		es.end();
		var hits = searchResponse.getHits();
		List<String> ids = Arrays.stream(hits.getHits()).map(source -> {
			try {
				String _simno = mapper.convertValue(mapper.readTree(source.getSourceAsString()).path("SimNo"),
						String.class);
				return _simno;
			} catch (IOException e) {
				e.printStackTrace();
				return "";
			}
		}).filter(simno -> !Strings.isNullOrEmpty(simno)).collect(Collectors.toList());

		odb.reset();
		result.sim = oclient.QueryFunction("sim_GetByImeiList", SimBOApi[].class, false, ids);
		odb.end();
		result.message = "Success";
		result.total = hits.getTotalHits().value;

		timer.end();
		return result;

	}

	public NetworkBO[] getAllSimNetwork(CodeTimer odb) throws Throwable {
		odb.reset();
		var result = oclient.QueryFunction("sim_GetNetwork", NetworkBO[].class, false);
		odb.end();
		return result;
	}

	public GroupBO[] getAllSimGroup() throws Throwable {
		return oclient.QueryFunction("sim_GetGroup", GroupBO[].class, false);
	}

	public SimStoreListSR SearchSimStore(SimQuery simquery, CodeTimer timer, CodeTimer es, CodeTimer odb)
			throws Throwable {

		var simStoreListSR = new SimStoreListSR();
		var q = boolQuery();
		var sb = new SearchSourceBuilder();

		if (simquery.provinceId > 0) {
			q.must(termQuery("ProvinceId", simquery.provinceId));
		}
		if (simquery.districtId > 0) {
			q.must(termQuery("DistrictId", simquery.districtId));
		}
		sb.query(q).from(simquery.pageSize * simquery.pageIndex).size(simquery.pageSize)
				.fetchSource(new String[] { "StoreID" }, null).sort("_score", SortOrder.DESC);
		sb.aggregation(terms("ProvinceId").field("ProvinceId").size(100))
				.aggregation(terms("DistrictId").field("DistrictId").size(500));

		SearchRequest searchRequest = new SearchRequest(CurrentIndexDB).source(sb);

		es.reset();
		var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
		es.end();

		List<Integer> somap = new ArrayList<Integer>();
		queryResults.getHits().forEach(h -> {
			try {
				var so = mapper.readValue(h.getSourceAsString(), SimSO.class);
				if (!Utils.StringIsEmpty(so.StoreID)) {
					var StoreID = Integer.valueOf(so.StoreID.replaceAll("T", "").trim());
					somap.add(StoreID);
				}
			} catch (Exception e) {
				Logs.LogException(e);
			}
		});

		simStoreListSR.rowCount = (int) queryResults.getHits().getTotalHits().value;

		if (somap != null) {

			if (simquery.provinceId > 0) {
				List<Integer> provincesID = new ArrayList<Integer>();
				ParsedLongTerms bucketprovinceID = queryResults.getAggregations().get("ProvinceId");
				bucketprovinceID.getBuckets().forEach(b -> {
					provincesID.add(b.getKeyAsNumber().intValue());
				});
				simStoreListSR.provinceBO = oclient.QueryFunction("province_GetByListID", ProvinceBO[].class, false,
						provincesID);
			}

			if (simquery.districtId > 0) {
				List<Integer> districtsID = new ArrayList<Integer>();
				ParsedLongTerms bucketdistrictID = queryResults.getAggregations().get("DistrictId");
				bucketdistrictID.getBuckets().forEach(b -> {
					districtsID.add(b.getKeyAsNumber().intValue());
				});
				simStoreListSR.districtBO = oclient.QueryFunction("district_GetByListID", DistrictBO[].class, false,
						districtsID);
			}
			simStoreListSR.storeBO = oclient.QueryFunction("product_GetStoreByListID", StoreBO[].class, false, somap);
		}
		return simStoreListSR;
	}

	public SimBOListSR Search2018(final SimQuery simquery, CodeTimer timer, CodeTimer es, CodeTimer odb)
			throws Throwable {
		SimSO simso = simquery.query;
		SimBOListSR result = new SimBOListSR();

		if (simquery.pageSize < 1)
			simquery.pageSize = 10;

		final int pageSize = simquery.pageSize;
		final int pageIndex = simquery.pageIndex;

		String keyword = DidxHelper.FormatKeywordSearchField(simso.Keyword);

		if (pageIndex * pageSize > 10000)
			return result;

		var boolSearchQuery = boolQuery();

		if (!Strings.isNullOrEmpty(simso.ProductNO)) {
			boolSearchQuery.must(termQuery("ProductNO", simso.ProductNO));
		}
		if (!Strings.isNullOrEmpty(simso.GroupId)) {
			boolSearchQuery.must(termQuery("GroupId", "g" + simso.GroupId + "g"));
		}
		if (simso.NetworkId >= 0) {
			boolSearchQuery.must(termQuery("NetworkId", simso.NetworkId));
		}
		if (!Strings.isNullOrEmpty(simso.StoreID)) {
			boolSearchQuery.must(termQuery("StoreID", "t" + simso.StoreID + "t"));
		}
		if (simso.SubgroupId > 0) {
			boolSearchQuery.must(termQuery("SubgroupId", simso.SubgroupId));
		}
		if (simso.MainGoupID > 0) {
			boolSearchQuery.must(termQuery("MainGoupID", simso.MainGoupID));
		}
		if (simso.SimLength > 0) {
			boolSearchQuery.must(termQuery("SimLength", simso.SimLength));
		}

		boolSearchQuery.must(termQuery("StoreID", "t25t"));

		// = 1 -> lẤY SIM 3G
		// = 2 -> Sim thường
		// <=0 -> lay het
		if (simso.Is3G > 0) {
			boolSearchQuery.must(termQuery("Is3G", simso.Is3G));
		}
		if (!Strings.isNullOrEmpty(simso.StoreID)) {
			boolSearchQuery.must(termQuery("StoreID", "t" + simso.StoreID + "t"));
		}
		if (!Strings.isNullOrEmpty(simso.Keyword_us)) // 01632, 01636, 01664, 01633, 01639
		{
			var boolSecondSearchQuery = boolQuery();
			String[] ds = simso.Keyword_us.replace(",", " ").trim().split("\\s+");
			for (String item : ds) {
				boolSecondSearchQuery.should(prefixQuery("SimNo", item));
			}
			boolSearchQuery.must(boolSecondSearchQuery);
		}
		
		boolSearchQuery.must(rangeQuery("PriceByPackage.backage_" + simquery.packageID).gt(1));
		
		if (simso.PriceFrom == 0 && simso.PriceTo > 0) {
			boolSearchQuery.must(rangeQuery("Price").lte(simso.PriceTo));
		} else if (simso.PriceTo >= simso.PriceFrom && simso.PriceFrom > 0)// trong khoan
		{
			boolSearchQuery.must(rangeQuery("Price").gte(simso.PriceFrom).lte(simso.PriceTo));
		} else if (simso.PriceFrom > 0 && simso.PriceTo == 0) // lon hon
		{
			boolSearchQuery.must(rangeQuery("Price").gte(simso.PriceFrom));
		}

		if (!Strings.isNullOrEmpty(keyword)) {
			boolSearchQuery.must(wildcardQuery("Keyword", keyword));
		}
		SearchSourceBuilder searchBuilder = new SearchSourceBuilder();
		searchBuilder.query(boolSearchQuery).from(pageSize * pageIndex).size(pageSize)
				.sort("MainGoupID", SortOrder.DESC).fetchSource("SimNo", null);
		if (simquery.sortBy == 5)
			searchBuilder.sort("PriceByPackage.backage_" + simquery.packageID, SortOrder.ASC);
		else if (simquery.sortBy == 1 && simquery.sortType == 1)
			searchBuilder.sort("Price", SortOrder.DESC);
		else if (simquery.sortBy == 1 && simquery.sortType == 2)
			searchBuilder.sort("Price", SortOrder.ASC);
		else if (simquery.sortBy == 2 && simquery.sortType == 1)
			searchBuilder.sort("PriceByPackage.backage_" + simquery.packageID, SortOrder.DESC);
		else if (simquery.sortBy == 2 && simquery.sortType == 2)
			searchBuilder.sort("PriceByPackage.backage_" + simquery.packageID, SortOrder.ASC);
		else
			searchBuilder.sort("PriceByPackage.backage_" + simquery.packageID, SortOrder.DESC);

		// sortBy =1 theo gia ,2 update date
		// sortType=1 giam dan, 2 tang dan
		SearchRequest searchRequest = new SearchRequest(CurrentIndexDB).source(searchBuilder);

		es.reset();
		SearchResponse searchResponse = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
		es.end();
		var hits = searchResponse.getHits();
		List<String> ids = Arrays.stream(hits.getHits()).map(source -> {
			try {
				String _simno = mapper.convertValue(mapper.readTree(source.getSourceAsString()).path("SimNo"),
						String.class);
				return _simno;
			} catch (IOException e) {
				e.printStackTrace();
				return "";
			}
		}).filter(simno -> !Strings.isNullOrEmpty(simno)).collect(Collectors.toList());

		odb.reset();
		result.sim = oclient.QueryFunction("sim_GetByImeiList", SimBOApi[].class, false, ids);
		odb.end();
		result.message = "Success";
		result.total = hits.getTotalHits().value;

		timer.end();
		return result;

	}

}
