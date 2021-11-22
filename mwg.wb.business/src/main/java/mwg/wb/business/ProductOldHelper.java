package mwg.wb.business;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.scriptFunction;
import static org.elasticsearch.search.aggregations.AggregationBuilders.cardinality;
import static org.elasticsearch.search.aggregations.AggregationBuilders.count;
import static org.elasticsearch.search.aggregations.AggregationBuilders.filter;
import static org.elasticsearch.search.aggregations.AggregationBuilders.max;
import static org.elasticsearch.search.aggregations.AggregationBuilders.min;
import static org.elasticsearch.search.aggregations.AggregationBuilders.sum;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;
import static org.elasticsearch.search.aggregations.AggregationBuilders.topHits;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.orientechnologies.orient.core.sql.operator.OQueryOperator;
import mwg.wb.model.products.ProductFeatureBO;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder.FilterFunctionBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Cardinality;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.Min;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twelvemonkeys.util.LinkedMap;

import mwg.wb.business.webservice.WebserviceHelper;
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.elasticsearch.dataquery.OrderType;
import mwg.wb.client.elasticsearch.dataquery.PriceFilter;
import mwg.wb.client.elasticsearch.dataquery.ProductOldImeiQuery;
import mwg.wb.client.elasticsearch.dataquery.ProductOldModelQuery;
import mwg.wb.client.elasticsearch.dataquery.PropertyDetail;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductOldBO;
import mwg.wb.model.promotion.PromotionOldProductBO;
import mwg.wb.model.search.ProductOldImeiSO;
import mwg.wb.model.searchresult.FaceCategorySR;
import mwg.wb.model.searchresult.FaceDistrictSR;
import mwg.wb.model.searchresult.FaceManuSR;
import mwg.wb.model.searchresult.FaceObject;
import mwg.wb.model.searchresult.ProductOldModelSR;
import mwg.wb.model.searchresult.ProductOldSR;

public class ProductOldHelper {

    protected String CurrentIndexDB = "";
    //	static final String CurrentTypeDB = "product";
    protected RestHighLevelClient clientIndex = null;
    private ElasticClient elasticClient = null;// ElasticClient.getInstance().getClient();
    protected ObjectMapper mapper = null;
    ORThreadLocal factoryRead = null;
    ClientConfig config = null;
    private static final int MAX_PAGE_SIZE_TGDD = 100;
    private static final int MAX_PAGE_SIZE_DMX = 200;
    
    public static final List<Integer> ACCESSORY_CATEGORY = List.of(482, 60, 57, 55, 58, 54, 1662, 1363, 1823,
            56, 75, 86, 382, 346, 2429, 2823, 2824, 2825, 3885, 1622, 5505, 5005, 5025,
            4547, 5452, 85, 6858, 6599, 7186);

    public ProductOldHelper(ORThreadLocal afactoryRead, ClientConfig aconfig) {
        CurrentIndexDB = aconfig.ELASTICSEARCH_PRODUCT_OLD_INDEX;
        config = aconfig;
        factoryRead = afactoryRead;
        mapper = DidxHelper.generateJsonMapper(GConfig.DateFormatStringNews);
     elasticClient = ElasticClient.getInstance(config.SERVER_ELASTICSEARCH_READ_HOST);
       //    elasticClient =  new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST); 
        clientIndex = elasticClient.getClient();
    }

    public List<ProductOldImeiSO> getProductOldByCode(String code, String[] inventoryStatusID, int storeID, int siteID)
            throws IOException {
        var q = boolQuery().must(rangeQuery("Quantity").gt(0)).must(termsQuery("InventoryStatusID", inventoryStatusID))
                .must(termQuery("ProductCode", code));
        if (storeID > 0)
            q.must(termQuery("StoreID", storeID));
        if (siteID > 0)
            q.must(termQuery("SiteID", siteID));
        var sb = new SearchSourceBuilder().query(q).from(0).size(100);
        var sr = new SearchRequest().indices(CurrentIndexDB).source(sb);
        var qr = clientIndex.search(sr, RequestOptions.DEFAULT);
        return Stream.of(qr.getHits().getHits()).map(x -> {
            try {
                return mapper.readValue(x.getSourceAsString(), ProductOldImeiSO.class);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(x -> x != null).collect(Collectors.toList());
    }

    public PromotionOldProductBO[] getPromotion(int siteID, int storeID, int inventoryStatus, String productCode,
                                                String imei) throws Throwable {
        return factoryRead.QueryFunction("product_getOldPromotion", PromotionOldProductBO[].class, false, siteID,
                storeID, inventoryStatus, productCode, imei);
    }

    public ProductOldBO getProductOldImei(int OLDID) throws Throwable {
        var old = factoryRead.QueryFunction("product_GetByOldID", ProductOldBO[].class, false, OLDID);
        return old == null || old.length == 0 ? null : old[0];

    }

    public ProductOldModelSR getProductOldModel() {
        return null;
    }

    /**
     * Search máy cũ, dựa trên ĐMX, Hàm cũ: GetProductOldImei2018
     *
     * @param query
     * @return
     * @throws Throwable
     */
    public ProductOldSR searchProductOldImei(ProductOldImeiQuery query) throws Throwable {
//		if (query.pagesize <= 0 || query.pagesize > maxPageSize)
//			query.pagesize = maxPageSize;
        if (query.pagesize <= 0) query.pagesize = 10;
        if (query.pageindex < 0) query.pageindex = 0;
        var q = boolQuery();
//		if (query.iserplock == 1) {
//			q.must(termQuery("IsErpLock", 1));
//			q.must(rangeQuery("UnlockDate").gte(new Date()));
//		}
//		if (query.iserplock == 2) {
//			q.must(boolQuery().should(termQuery("IsErpLock", 0)).should(termQuery("IsErpLock", 1)))
//					.must(rangeQuery("UnlockDate").lt(new Date()));
//		}
        q.must(boolQuery().should(termQuery("IsErpLock", 0)).should(termQuery("IsErpLock", 1)))
                .must(rangeQuery("UnlockDate").lt(new Date()));

        if (query.categoryid > 0)
            q.must(termQuery("CategoryID", query.categoryid));
        if (query.productid > 0)
            q.must(termQuery("ProductID", query.productid));

        if (query.colorid > 0)
            q.must(termQuery("ColorID", query.colorid));

        if (query.manufactureid > 0)
            q.must(termQuery("ManufactureID", query.manufactureid));

        if (query.provinceidlist != null && query.provinceidlist.length > 0)
            q.must(termsQuery("ProvinceID", query.provinceidlist));

        if (query.provinceID > 0)
            q.must(termQuery("ProvinceID", query.provinceID));


        if (query.districtid > 0)
            q.must(termQuery("DistrictID", query.districtid));

        if (query.storeid > 0) {
            q.must(termQuery("StoreID", query.storeid));
        }


//		if (query.storeid > 0) {
//			if (!query.isgetimeiinotherstore)
//				q.must(termQuery("StoreID", query.storeid));
//			else
//				q.mustNot(termQuery("StoreID", query.storeid));
//		}
        if (query.minprice > 0) {
            q.must(rangeQuery("Price2").lte(query.minprice));
        }
        if (query.maxprice > 0)
            q.must(rangeQuery("Price2").gte(query.minprice));
        if (query.isgettotal) {
            q.must(rangeQuery("NewDiscountValue").gte(0));
        } else {
            if (query.havepromotion) {
                q.must(rangeQuery("NewDiscountValue").gte(1000));
            } else {
                q.must(rangeQuery("NewDiscountValue").lte(0));
            }
        }


        q.must(rangeQuery("PriceAfterPromotion").gte(10000));


        if (query.issearchimei)
            q.must(termQuery("IsImei", 1));


//        if (query.inventorystatusidlist != null && query.inventorystatusidlist.length > 0)
//            q.must(termsQuery("InventoryStatusID", query.inventorystatusidlist));

//        if (query.imei != null && !query.imei.isEmpty())
//            q.must(wildcardQuery("IMEI", "*" + query.imei + "*"));
//        q.must(rangeQuery("Quantity").gt(0));
        q.must(boolQuery().should(termQuery("IsImei", 1))
                .should(termQuery("IsImei", 0)).must(rangeQuery("Quantity").gte(1)));


        // => nếu invstatus = 7, lấy oldid nào có promo còn giảm giá > 0?
//        long now = System.currentTimeMillis();
//        q.must(boolQuery().should(termsQuery("InventoryStatusID", new int[]{2, 4}))
//                .should(boolQuery().must(termQuery("InventoryStatusID", 7)).must(rangeQuery("DiscountValue").gt(0))
//                        .must(rangeQuery("DiscountValueCode").gt(0))
//                        .must(rangeQuery("PromotionsListCode.begindate").lt(now))
//                        .must(rangeQuery("PromotionsListCode.enddate").gt(now))));
//
//        q.must(rangeQuery("Price").gt(query.minprice > 0 ? query.minprice : 0));
//        if (query.maxprice > 0 && query.maxprice > query.minprice)
//            q.must(rangeQuery("Price").lte(query.maxprice));


        //Promtion chi cap nhap khuyen mai chô Inventory 2 4 nen khong can filter nay nua

//        if (query.havepromotion) {
//            q.mustNot(termQuery("InventoryStatusID", 7));
//            q.must(boolQuery()
//                    .should(boolQuery().must(rangeQuery("DiscountValueIMEI").gt(0))
//                            .must(rangeQuery("PromotionsListImei.begindate").lt(now))
//                            .must(rangeQuery("PromotionsListImei.enddate").gt(now)))
//                    .should(boolQuery().must(rangeQuery("DiscountValueCode").gt(0))
//                            .must(rangeQuery("PromotionsListCode.begindate").lt(now))
//                            .must(rangeQuery("PromotionsListCode.enddate").gt(now)))
//                    .should(rangeQuery("PricePercent").gt(60)));
//
//        }
        SearchSourceBuilder sb = new SearchSourceBuilder();
        SearchRequest sr = new SearchRequest();
        sb.from(query.pageindex * query.pagesize).size(query.pagesize).query(q).fetchSource(new String[]{"OLDID"},
                null);

        // Aggregations
        if(query.inventorystatusidlist !=null){
            {
                sb.aggregation(filter("filtered_aggs", termsQuery("InventoryStatusID", query.inventorystatusidlist))
                        .subAggregation(terms("ByMin").field("ProductID").size(MAX_PAGE_SIZE_TGDD)
                                .subAggregation(sum("mBySum").field("Quantity"))))
                        .aggregation(terms("FacetTermColorID").field("ColorID").size(MAX_PAGE_SIZE_TGDD)
                                .subAggregation(sum("Sum").field("OLDID")))
                        .aggregation(terms("FaceTermStoreID").field("StoreID").size(MAX_PAGE_SIZE_TGDD)
                                .subAggregation(sum("Sum").field("OLDID")))
                        .aggregation(terms("FacetTermDistrictID").field("DistrictID").size(MAX_PAGE_SIZE_TGDD)
                                .subAggregation(sum("Sum").field("OLDID")))
                        .aggregation(terms("FacetTermProvinceID").field("ProvinceID").size(MAX_PAGE_SIZE_TGDD)
                                .subAggregation(sum("Sum").field("OLDID")));
            }}

        // Sorts
        // sap xep theo phan tram khuyen mai
        if (query.discountordertype == OrderType.DESCENDING)
            sb.sort("NewDiscountValuePercent", SortOrder.DESC);
        else if (query.discountordertype == OrderType.ASCENDING)
            sb.sort("NewDiscountValuePercent", SortOrder.ASC);

            //NewRealPrice2 == PriceAfterPromotion -- Gia sau khuyen mai #warning
        else if (query.priceordertype == OrderType.DESCENDING)
            sb.sort("PriceAfterPromotion", SortOrder.DESC);
        else if (query.priceordertype == OrderType.ASCENDING)
            sb.sort("PriceAfterPromotion", SortOrder.ASC);


        sr.indices(CurrentIndexDB).source(sb);
        ProductOldSR result = new ProductOldSR() {
            {
                message = "Unknown error";
            }
        };
        try {
            var qr = clientIndex.search(sr, RequestOptions.DEFAULT);
            result.message = "Success";
            int[] ids = Stream.of(qr.getHits().getHits()).mapToInt(x -> {
                try {
                    return mapper.readValue(x.getSourceAsString(), ProductOldImeiSO.class).OLDID;
                } catch (IOException e) {
                    Logs.LogException("Can not parse: " + x.getSourceAsString());
                    Logs.LogException(e);
                    return -1;
                }
            }).filter(x -> x > 0).toArray();
            result.list = factoryRead.QueryFunction("product_GetByOldIDList", ProductOldBO[].class, false, ids);
            var aggrs = qr.getAggregations() == null ? null : qr.getAggregations().asMap();
            if(query.inventorystatusidlist !=null){
                var terms = (Terms) ((ParsedFilter) aggrs.get("filtered_aggs")).getAggregations().get("ByMin");
                result.totalQuantity = (int) terms.getBuckets().stream()
                        .mapToDouble(x -> ((Sum) x.getAggregations().get("mBySum")).getValue()).sum();
                result.colorList = ((Terms) aggrs.get("FacetTermColorID")).getBuckets().stream()
                        .mapToInt(x -> x.getKeyAsNumber().intValue()).toArray();
                result.districtList = ((Terms) aggrs.get("FacetTermDistrictID")).getBuckets().stream()
                        .mapToInt(x -> x.getKeyAsNumber().intValue()).toArray();
                result.provinceList = ((Terms) aggrs.get("FacetTermProvinceID")).getBuckets().stream()
                        .mapToInt(x -> x.getKeyAsNumber().intValue()).toArray();
            }


            result.total = qr.getHits().getTotalHits().value;
        } catch (Exception e) {
            Logs.WriteLine("ProductOldHelper - Search error");
            Logs.LogException(e);
            result.message = e.getMessage();
            result.stacktrace = Stream.of(e.getStackTrace()).map(x -> x.toString()).toArray(String[]::new);
        }
        return result;
    }

    /**
     * Search máy cũ, dựa trên ĐMX, Hàm cũ: GetProductOldImei2020
     *
     * @param query
     * @return
     * @throws Throwable
     */
    public ProductOldSR searchProductOldImeiBySiteDMX(ProductOldImeiQuery query) throws Throwable {

        if (query == null) {
            return null;
        }

        Date date = new Date();
        // Get page size
        if (query.pagesize <= 0 || query.pagesize > 50) {
            query.pagesize = 50;
        }
        if (query.pageindex < 0) {
            query.pageindex = 0;
        }

        BoolQueryBuilder q = boolQuery();
        if (query.iserplock == 1) {
            q.must(termQuery("IsErpLock", 1));
            q.must(rangeQuery("UnlockDate").gte(date));
        }

        if (query.iserplock == 2) {
            q.must(boolQuery().should(termQuery("IsErpLock", 0)).should(termQuery("IsErpLock", 1)))
                    .must(rangeQuery("UnlockDate").lt(date));
        }

        if (query.storeid > 0) {
            if (!query.isgetimeiinotherstore)
                q.must(termQuery("StoreID", query.storeid));
            else
                q.mustNot(termQuery("StoreID", query.storeid));
        }

        if (query.issearchimei) {
            q.must(termQuery("IsImei", 1));
        }

        if (query.categoryid > 0) {
            q.must(termQuery("CategoryID", query.categoryid));
        }

        if (query.productid > 0) {
            q.must(termQuery("ProductID", query.productid));
        }

        if (query.colorid > 0) {
            q.must(termQuery("ColorID", query.colorid));
        }

        if (query.manufactureid > 0) {
            q.must(termQuery("ManufactureID", query.manufactureid));
        }

        if (query.provinceidlist != null && query.provinceidlist.length > 0) {
            q.must(termsQuery("ProvinceID", query.provinceidlist));
        }

        if (query.inventorystatusidlist != null && query.inventorystatusidlist.length > 0) {
            q.must(termsQuery("InventoryStatusID", query.inventorystatusidlist));
        }

        if (query.districtid > 0) {
            q.must(termQuery("DistrictID", query.districtid));
        }

        if (query.storeid > 0) {
            q.must(termQuery("StoreID", query.storeid));
        }

        if (query.imei != null && !query.imei.isEmpty()) {
            q.must(wildcardQuery("IMEI", "*" + query.imei + "*"));
        }

        q.must(rangeQuery("Quantity").gte(1));
        q.must(rangeQuery("Price2").gt(0));

        if (query.minprice > 0) {
            q.must(rangeQuery("NewRealPrice2").lte(query.minprice));
        }

        if (query.maxprice > 0) {
            q.must(rangeQuery("NewRealPrice2").gte(query.maxprice));
        }

        if (query.havepromotion == true) {
            q.must(boolQuery().should(boolQuery().must(rangeQuery("NewDiscountValue").gte(1))
                    .must(rangeQuery("DeltaPercent_ProductPrice_RealPrice").gte(61))));
        }

        SearchSourceBuilder sb = new SearchSourceBuilder();
        SearchRequest sr = new SearchRequest();
        sb.from(query.pageindex * query.pagesize).size(query.pagesize).query(q)
                .fetchSource(new String[]{"OLDID"}, null);

        // Aggregations
		if (query.inventorystatusidlist != null) {
			sb.aggregation(filter("filtered_aggs", termsQuery("InventoryStatusID", query.inventorystatusidlist))
					.subAggregation(terms("ByMin").field("ProductID").size(MAX_PAGE_SIZE_DMX)
							.subAggregation(sum("mBySum").field("Quantity"))))

					.aggregation(terms("FacetTermColorID").field("ColorID").size(MAX_PAGE_SIZE_DMX)
							.subAggregation(sum("Sum").field("OLDID")))
					.aggregation(terms("FacetTermDistrictID").field("DistrictID").size(MAX_PAGE_SIZE_DMX)
							.subAggregation(sum("Sum").field("OLDID")))
					.aggregation(terms("FacetTermProvinceID").field("ProvinceID").size(MAX_PAGE_SIZE_DMX)
							.subAggregation(sum("Sum").field("OLDID")));
		}

        // Sorts
        // sap xep theo phan tram khuyen mai #warning
        if (query.discountordertype == OrderType.DESCENDING)
            sb.sort("NewDiscountValuePercent", SortOrder.DESC);
        else if (query.discountordertype == OrderType.ASCENDING)
            sb.sort("NewDiscountValuePercent", SortOrder.ASC);

            // NewRealPrice2 == PriceAfterPromotion -- Gia sau khuyen mai #warning
        else if (query.priceordertype == OrderType.DESCENDING)
            sb.sort("PriceAfterPromotion", SortOrder.DESC);
        else if (query.priceordertype == OrderType.ASCENDING)
            sb.sort("PriceAfterPromotion", SortOrder.ASC);

        // Create new a ProductOldSR
        ProductOldSR result = new ProductOldSR() {
            {
                message = "Unknown error";
            }
        };
        try {
            // Query from orientDB
            sr.indices(CurrentIndexDB).source(sb);
            var qr = clientIndex.search(sr, RequestOptions.DEFAULT);
            result.message = "Success";

            // Get list OLDID
            int[] ids = Stream.of(qr.getHits().getHits()).mapToInt(x -> {
                try {
                    return mapper.readValue(x.getSourceAsString(), ProductOldImeiSO.class).OLDID;
                } catch (IOException e) {
                    Logs.LogException("Can not parse: " + x.getSourceAsString());
                    Logs.LogException(e);
                    return -1;
                }
            }).filter(x -> x > 0).toArray();

            if (ids.length > 0) {
                result.list = factoryRead.QueryFunction("product_GetByOldIDList", ProductOldBO[].class, false, ids);
            }

            // Get aggregations keyed by aggregation name.
            var aggrs = qr.getAggregations() == null ? null : qr.getAggregations().asMap();
			if (query.inventorystatusidlist != null) {
				// Get aggregations have name is ByMin
				var terms = (Terms) ((ParsedFilter) aggrs.get("filtered_aggs")).getAggregations().get("ByMin");

				result.totalQuantity = (int) terms.getBuckets().stream()
						.mapToDouble(x -> ((Sum) x.getAggregations().get("mBySum")).getValue()).sum();

				result.colorList = ((Terms) aggrs.get("FacetTermColorID")).getBuckets().stream()
						.mapToInt(x -> x.getKeyAsNumber().intValue()).toArray();

				result.districtList = ((Terms) aggrs.get("FacetTermDistrictID")).getBuckets().stream()
						.mapToInt(x -> x.getKeyAsNumber().intValue()).toArray();

				result.provinceList = ((Terms) aggrs.get("FacetTermProvinceID")).getBuckets().stream()
						.mapToInt(x -> x.getKeyAsNumber().intValue()).toArray();
			}
            result.total = qr.getHits().getTotalHits().value;

        } catch (Exception e) {
            Logs.WriteLine("ProductOldHelper - Search error");
            Logs.LogException(e);
            result.message = e.getMessage();
            result.stacktrace = Stream.of(e.getStackTrace()).map(x -> x.toString()).toArray(String[]::new);
        }
        return result;
    }

    public ProductOldModelSR searchProductOldModel(ProductOldModelQuery qry) throws Throwable {
        var result = new ProductOldModelSR();
        List<ProductBO> modelList = new ArrayList<>();
        long intRowCount = 0;
        if (qry.PageSize <= 1) qry.PageSize = 10;
        if (qry.PageIndex < 0) qry.PageIndex = 0;
        var query = boolQuery();


        //filter
        genQueryProductOldModel(query, qry);

        var sb = new SearchSourceBuilder();
        var sr = new SearchRequest();
        sb.from(qry.PageIndex * qry.PageSize).size(qry.PageSize).query(query)
                .fetchSource(new String[]{"OLDID"}, null);

        // Aggregations


        sb.aggregation(filter("FacetDiscountValue", rangeQuery("NewDiscountValue").gte(1))// agg khuyen mai
                .subAggregation(terms("byMin3").field("ProductID").size(500)
                        .order(BucketOrder.aggregation("MinPrice3", false))
                        .subAggregation(min("MinPrice3").field("NewDiscountValue"))//discount nho nhat
                        .subAggregation(min("MinDiscountValuePercent").field("NewDiscountValuePercent"))//tinh ra % discount nho nhat
                        .subAggregation(sum("SumHasPromotion").field("Quantity"))//tong sp có km là bn (lay theo quantity vì hang khong imei có quantity có the >1)
                ));

        sb.aggregation(filter("FacetNoDiscountValue", rangeQuery("NewDiscountValue").lte(1))// agg khong khuyen mai
                .subAggregation(terms("byMin4").field("ProductID").size(500)
                        .order(BucketOrder.aggregation("MinPrice4", false))
                        .subAggregation(min("MinPrice4").field("NewDiscountValue"))
                        .subAggregation(sum("SumNoPromotion").field("Quantity"))));
//
        sb.aggregation(terms("ByProductID").field("ProductID").size(500).order(BucketOrder.aggregation("MinPrice", false)) //agg productid
                .subAggregation(min("MinPrice").field("Price2")) // minimun price
                .subAggregation(sum("bySum").field("Quantity"))
                .subAggregation(min("MinPriceAfterPromo").field("PriceAfterPromotion")) // min price promotion
                .subAggregation(min("MinProvince").field("ProvinceID"))
                .subAggregation(cardinality("mByCountProvince").field("ProvinceID"))
                .subAggregation(min("MinStoreID").field("StoreID"))
//                .subAggregation(count("mbyCountImei").field("IMEI"))
                .subAggregation(max("MaxDiscountValue").field("NewDiscountValue")) // tinh ra gia tri khuyen mai lon nhat
                .subAggregation(max("MaxDiscountValuePercent").field("NewDiscountValuePercent")) //tinh ra % discount nho nhat
                .subAggregation(min("MinDiscountValuePercent").field("NewDiscountValuePercent"))
                .subAggregation(sum("mByImei").field("IsImei")) //tong so luong isImei
                .subAggregation(min("MinIsPromotion").field("NewIsPromotion"))

        );
        sb.aggregation(terms("termByModelMinPrice").field("ProductID").size(500) // get 1 product with the lowest price
                .subAggregation(topHits("topMinPriceHit").size(1).sort("Price2", SortOrder.ASC)));
//
        sb.aggregation(terms("termByModelMinPriceAfterPromotion").field("ProductID").size(500) // get 1 product with the lowest price after promtion
                .subAggregation(topHits("topMinPriceAfterPromotionHit").size(1).sort("PriceAfterPromotion", SortOrder.ASC)));

        sb.aggregation(terms("FacetTermCategoryID").field("CategoryID").size(20) // agg product by categoryid
                .subAggregation(count("Sum").field("OlDID")));
//
        sb.aggregation(terms("FacetTermManufactureID").field("ManufacturerID").size(50) // agg product by ManufactureID
                .subAggregation(count("Sum").field("OlDID")));
//
        sb.aggregation(terms("FacetTermDistrictID").field("DistrictID").size(500) // agg product by DistrictID
                .subAggregation(count("Sum").field("OLDID")));


        sr.indices(CurrentIndexDB).source(sb);

        try {
            var qr = clientIndex.search(sr, RequestOptions.DEFAULT);
            result.totalRecordFilter = (int) qr.getHits().getTotalHits().value;
//            int[] ids = Stream.of(qr.getHits().getHits()).mapToInt(x -> {
//                try {
//                    return mapper.readValue(x.getSourceAsString(), ProductOldImeiSO.class).OLDID;
//                } catch (IOException e) {
//                    Logs.LogException("Can not parse: " + x.getSourceAsString());
//                    Logs.LogException(e);
//                    return -1;
//                }
//            }).filter(x -> x > 0).toArray();
//
//            if (ids.length > 0) {
//                result.list = factoryRead.QueryFunction("product_GetByOldIDList", ProductOldBO[].class, false, ids);
//            }
            var aggrs = qr.getAggregations() == null ? null : qr.getAggregations().asMap();
            var aggDiscounts = (Terms) ((ParsedFilter) aggrs.get("FacetDiscountValue")).getAggregations().get("byMin3"); //agg discount
            var aggNoDiscounts   = (Terms) ((ParsedFilter) aggrs.get("FacetNoDiscountValue")).getAggregations().get("byMin4"); // agg no discount
            var aggProductId = ((Terms) aggrs.get("ByProductID")); // agg productid
            var aggMinPrice = ((Terms) aggrs.get("termByModelMinPrice")); // agg min price
            var aggMinPriceAfterPromotion = ((Terms)  aggrs.get("termByModelMinPriceAfterPromotion")); // agg min price after promotion
            var aggCategoryID = ((Terms) aggrs.get("FacetTermCategoryID")); // agg CategoryID
            var aggManufactureID = ((Terms) aggrs.get("FacetTermManufactureID")); // agg ManufactureID
            var aggDistrictID = ((Terms) aggrs.get("FacetTermDistrictID")); // agg DistrictID


            var mMinPriceHit = new HashMap<Double,ProductOldImeiSO>();
            var mMinPriceAfterPromotionHit = new HashMap<Double,ProductOldImeiSO>();

            aggMinPrice.getBuckets().stream().map(v -> (TopHits) v.getAggregations().get("topMinPriceHit"))
                    .forEach(h -> h.getHits().forEach(v -> {
                        try {
                            var so = mapper.readValue(v.getSourceAsString(), ProductOldImeiSO.class);
                            mMinPriceHit.put(so.ProductID, so);
                        } catch (IOException e) {
                            Logs.LogException(e);
                        }
                    }));

            aggMinPriceAfterPromotion.getBuckets().stream().map(v -> (TopHits) v.getAggregations().get("topMinPriceAfterPromotionHit"))
                    .forEach(h -> h.getHits().forEach(v -> {
                        try {
                            var so = mapper.readValue(v.getSourceAsString(), ProductOldImeiSO.class);
                            mMinPriceAfterPromotionHit.put(so.ProductID, so);
                        } catch (IOException e) {
                            Logs.LogException(e);
                        }
                    }));


            for(var item : aggProductId.getBuckets()){
                        String extendInfo = "";
                        int productId = Utils.toInt(item.getKeyAsString());

//
//                        var minPriceSO = mMinPriceHit.get(productId);
//                        var minPriceAfterPromo = mMinPriceAfterPromotionHit.get(productId);

//                        if (minPriceSO == null && minPriceAfterPromo == null) continue;


                        double isImei = ((Sum) item.getAggregations().get("mByImei")).getValue();       //tong so luong imei

                        int quantityCount = (int) ((Sum) item.getAggregations().get("bySum")).getValue();//tong so luong quantity (theo ton kho)

                        long mByCountProvince = ((Cardinality) item.getAggregations().get("mByCountProvince")).getValue(); //tong province thoa man

                        double maxDiscountValue = ((Max) item.getAggregations().get("MaxDiscountValue")).getValue(); // max discount

                        int maxDiscountValuePercent = (int) ((Max) item.getAggregations().get("MaxDiscountValuePercent")).getValue(); // max discount value percent

                        int minDiscountValuePercent = (int) ((Min) item.getAggregations().get("MinDiscountValuePercent")).getValue(); // min discount value percent

                        double minIsPromotion = ((Min) item.getAggregations().get("MinIsPromotion")).getValue(); // neu khong co khuyen mai thì min = 0

                        double minPrice = ((Min) item.getAggregations().get("MinPrice")).getValue(); // min price

                        double minPriceAfterPromotion = ((Min) item.getAggregations().get("MinPriceAfterPromo")).getValue(); // min price after promotion


//                        extendInfo += extendInfo + "<MinRealPrice>" + minPriceAfterPromo + "</MinRealPrice><minPrice>"
//                                + minPrice + "</minPrice><IsSameOldID>" + (minPriceSO.OLDID == minPriceAfterPromo.OLDID ? 1 : 0) + "</IsSameOldID>" +
//                                "<IsHasPromotionInSameOldID>" + (minPriceSO.NewDiscountValue > 0 ? 1 : 0) + "</IsHasPromotionInSameOldID>";

                        double discountValueMin = 0;
                        int quantityHasPro = 0;
                        int quantityNoPro = 0;

                        if (aggDiscounts != null) {

                         var aggDiscount =    aggDiscounts.getBuckets().stream().filter(x ->  Utils.toInt(x.getKeyAsString()) == productId).findFirst();

                            discountValueMin = aggDiscount.map(x -> {
                                return ((Min) x.getAggregations().get("MinPrice3")).value();
                            }).get();
                            quantityHasPro=  aggDiscount.map(x -> {
                                return ((Sum) x.getAggregations().get("SumHasPromotion")).value();
                            }).get().intValue();


                        }



                        if (aggNoDiscounts != null) {

                            var aggNoDiscount =    aggDiscounts.getBuckets().stream().filter(x ->  Utils.toInt(x.getKeyAsString()) == productId).findFirst();

                            quantityHasPro=  aggNoDiscount.map(x -> {
                                return ((Sum) x.getAggregations().get("SumHasPromotion")).value();
                            }).get().intValue();


                        }

                        extendInfo = extendInfo + "<DiscountValueMin>" + discountValueMin + "</DiscountValueMin>";

                        extendInfo = extendInfo + "<IsHasMultiDiscount>" + (minDiscountValuePercent != maxDiscountValuePercent ? 1 : 0) + "</IsHasMultiDiscount>";
                        if (qry.IsHavePromotion) {
                            quantityCount = quantityHasPro;
                        } else {
                            quantityCount = quantityNoPro;
                        }

                        if (productId != 999999999) {
                            String finalExtendInfo = extendInfo;
                            var product = new ProductBO() {
                                {
                                    ProductID=productId;
                                    ProductOldCount = item.getDocCount();
                                    Price = minPriceAfterPromotion;
                                    MaxDiscountValue = maxDiscountValuePercent;
                                    ProvinceCount = (int) mByCountProvince;
                                    ExtendInfo = finalExtendInfo;
                                    IsHavePromotion = minIsPromotion > 0 ? true : false;
                                    CountLike = (int) isImei;
                                    UpdatedPartnerUserID = minDiscountValuePercent;
                                }
                            };
                            if (minPriceAfterPromotion >= 10000 && quantityCount > 0) {
                                modelList.add(product);
                            }

                        }

                    }

//
//                    result.districtList = ((Terms) aggrs.get("FacetTermDistrictID")).getBuckets().stream()
//                    .mapToInt(x -> x.getKeyAsNumber().intValue()).toArray();
//
//            result.provinceList = ((Terms) aggrs.get("FacetTermProvinceID")).getBuckets().stream()
//                    .mapToInt(x -> x.getKeyAsNumber().intValue()).toArray();
            List<FaceManuSR> manulist = new ArrayList<FaceManuSR>();
            aggManufactureID.getBuckets().forEach(x -> manulist.add(new FaceManuSR(){{
                manufacturerID = x.getKeyAsNumber().intValue();
                productCount = (int)((ValueCount)x.getAggregations().get("Sum")).value();

            }}));

            List<FaceCategorySR> cateList = new ArrayList<>();
            aggCategoryID.getBuckets().stream().filter(Objects::nonNull).forEach(x -> cateList.add(new FaceCategorySR(){
                {
                    categoryID = x.getKeyAsNumber().intValue();
                    productCount = (int)((ValueCount) x.getAggregations().get("Sum")).value();
                }
            }));

            List<FaceDistrictSR> districtList = new ArrayList<>();
            aggDistrictID.getBuckets().forEach(x -> districtList.add(new FaceDistrictSR(){
                {
                    districtID = x.getKeyAsNumber().intValue();
                    productCount = (int) ((ValueCount) x.getAggregations().get("Sum")).value();
                }
            }));

        /*    if (qry.PropertyDetailFilters != null && qry.PropertyDetailFilters.Any())
            {
                IEnumerable<int> lProductIdTmp = SearchProductProperty2018(qry);
                if (lProductIdTmp != null)
                {
                    modelList = modelList.Where(p1 => lProductIdTmp.Contains(p1.Key)).ToDictionary(t => t.Key, t => t.Value);
                }

            }*/
            if (qry.ExtensionObject != 2020 && qry.ExtensionObject != 9999)//th khong phai lay all
            {
                if (qry.IsHavePromotion == true)
                {
                    modelList = modelList.stream().filter(x -> x !=null && x.MaxDiscountValue >= 1).collect(Collectors.toList());
                }

            }

            if(qry.MinPrice >0){
                modelList = modelList.stream().filter(x -> x!=null && x.Price <= qry.MinPrice).collect(Collectors.toList());
            }
            if(qry.MaxPrice > 0){
                modelList = modelList.stream().filter(x -> x!=null && x.Price >= qry.MaxPrice).collect(Collectors.toList());
            }
            //order
            if(qry.IsHavePromotion){
                if (qry.DiscountOrdertype == OrderType.DESCENDING){
                    modelList = modelList.stream().sorted(Comparator.comparing(x -> -x.MaxDiscountValue)).collect(Collectors.toList());
                }
                if(qry.DiscountOrdertype == OrderType.ASCENDING){
                    modelList = modelList.stream().sorted(Comparator.comparing(x -> x.MaxDiscountValue)).collect(Collectors.toList());
                }
                if(qry.PriceOrdertype == OrderType.ASCENDING){
                    modelList = modelList.stream().sorted(Comparator.comparing(x -> x.Price)).collect(Collectors.toList());

                }
                if(qry.PriceOrdertype == OrderType.DESCENDING){
                    modelList = modelList.stream().sorted(Comparator.comparing(x -> -x.Price)).collect(Collectors.toList());

                }


            }else{
                if (qry.DiscountOrdertype == OrderType.DESCENDING){
                    modelList = modelList.stream().sorted(Comparator.comparing(x -> -x.MaxDiscountValue)).collect(Collectors.toList());
                }
                if(qry.DiscountOrdertype == OrderType.ASCENDING){
                    modelList = modelList.stream().sorted(Comparator.comparing(x -> -x.MaxDiscountValue)).collect(Collectors.toList());
                }
                if(qry.PriceOrdertype == OrderType.ASCENDING){
                    modelList = modelList.stream().sorted(Comparator.comparing(x -> x.Price)).collect(Collectors.toList());

                }
                if(qry.PriceOrdertype == OrderType.DESCENDING){
                    modelList = modelList.stream().sorted(Comparator.comparing(x -> -x.Price)).collect(Collectors.toList());

                }}
                if(modelList !=null && modelList.size() > 0){
                    result.totalPromotion = (int) modelList.stream().filter(x-> x.MaxDiscountValue >0).count();
                }
                var reList = modelList.stream().skip(qry.PageIndex*qry.PageSize).limit(qry.PageSize).collect(Collectors.toList());
                List<ProductBO> listProduct = new ArrayList<>();
                if(reList!=null){

                    for(var item : reList){
                        ProductBO productBO = WebserviceHelper.Call(4).Get(
                                "apiproduct/getproduct?productID=:?&siteID=:?&provinceID=:?&lang=:?",
                                ProductBO.class, item.ProductID, 2, 3, "vi-VN");
                        var modelFilter = modelList.stream().filter(x->x.ProductID == item.ProductID);
                        if(productBO !=null){
                            boolean IsPromotion = false;
                            productBO.IsHavePromotion = IsPromotion;
                        modelFilter.forEach(x -> {
                                productBO.MaxDiscountValue = x.MaxDiscountValue;
                                productBO.lstPromotionBO = x.lstPromotionBO !=null ? x.lstPromotionBO : new ArrayList<>();
                                productBO.ProductOldPriceFrom = x.Price;
                                productBO.ProductOldCount = x.ProductOldCount;
                                productBO.ProvinceCount = x.ProvinceCount;
                                productBO.ExtendInfo = x.ExtendInfo;
                                productBO.CountLike = x.CountLike;
                            });
                        }
                        listProduct.add(productBO);


                    }
                }
                result.faceListCategory = cateList;
                result.faceListManu = manulist;
                result.faceDistrict = districtList;
                result.list = listProduct;

        } catch (IOException ioException) {
            ioException.printStackTrace();
            Logs.LogException(ioException);
            throw new Exception("ESIOException-RERTRY");
        }


        return result;


    }

    private void genQueryProductOldModel(BoolQueryBuilder query, ProductOldModelQuery qry) {

        var keyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(qry.Keyword));

        if (qry.ProductID > 0)
            query.must(termQuery("ProductID", qry.ProductID));
        if (qry.ExtensionObject != 9999 && !Strings.isNullOrEmpty(qry.CategoryIdList))
            query.must(termsQuery("CategoryID", qry.CategoryIdList.split(",")));
        if (qry.ExtensionObject == 9999 && !Strings.isNullOrEmpty(qry.CategoryIdList)) //goi tu trang feature, khong them field moi duoc nen dung do field nay
            query.must(termsQuery("ProductID", qry.CategoryIdList.split(",")));
        if (qry.ManufactureID > 0)
            query.must(termQuery("ManufacturerID", qry.ManufactureID));
        if (qry.ManufactureIds != null && qry.ManufactureIds.length > 0)
            query.must(termsQuery("ManufactureID", qry.ManufactureIds));
        if (qry.ProvinceIDList != null && qry.ProvinceIDList.length > 0)
            query.must(termsQuery("ProvinceID", qry.ProvinceIDList));
        if (qry.DistrictID > 0)
            query.must(termQuery("DistrictID", qry.DistrictID));
        if (qry.StoreID > 0) {
            query.must(termQuery("StoreID", qry.StoreID));
        }
        if (qry.ExtensionObject == 2020 || qry.ExtensionObject == 9999) {
            query.must(rangeQuery("NewDiscountValue").gte(0));
        } else {
            if (qry.IsHavePromotion) {
                query.must(rangeQuery("NewDiscountValue").gte(5));

            } else {
                query.must(rangeQuery("NewDiscountValue").lte(0));

            }
            if (!Strings.isNullOrEmpty(keyword)) {
                query.must(boolQuery()
                        .should(queryStringQuery(keyword).field("Keyword").defaultOperator(Operator.AND))
                        .should(termQuery("SEIMEI", "*" + keyword + "*")));


            }

            query.must(rangeQuery("PriceAfterPromotion").gte(10000));

            query.must(boolQuery().should(termQuery("IsImei", 1))
                    .should(termQuery("IsImei", 0)).must(rangeQuery("Quantity").gte(1)));

            query.must(boolQuery().should(termQuery("IsErpLock", 0)).should(termQuery("IsErpLock", 1)))
                    .must(rangeQuery("UnlockDate").lt(new Date()));


            //lock
            if (qry.DiscountTo > 0) {
                query.must(rangeQuery("NewDiscountValue").lte(qry.DiscountTo));
            }
            if (qry.DiscountFrom > 0) {
                query.must(rangeQuery("NewDiscountValue").gte(qry.DiscountFrom));
            }

            var sb = new SearchSourceBuilder();
            var sr = new SearchRequest();

            sb.from(qry.PageIndex * qry.PageSize).size(qry.PageSize).query(query)
                    .fetchSource(new String[]{"OLDID"}, null);
            sr.indices(CurrentIndexDB).source(sb);
            try {
                var qr = clientIndex.search(sr, RequestOptions.DEFAULT);
            } catch (IOException e) {
                LogHelper.WriteLog(e);

                e.printStackTrace();
            }


        }


    }

	/**
	 * Search máy cũ, dựa trên ĐMX, Hàm cũ: GetProductOldModelList2020S
	 * 
	 * @param qry
	 * @return
	 * @throws Exception
	 */
	public ProductOldModelSR searchProductOldModeDMX(ProductOldModelQuery qry) throws Exception {

		// Attributes return
		int totalPromotion = 0;
		int totalQuantity = 0;
		long intRowCount = 0;
		List<Integer> categoryList = new LinkedList<Integer>();
		int totalRecordFilter = 0;
		List<FaceObject> faceLst = new LinkedList<FaceObject>();
		
		if (qry.PageSize < 1 || qry.PageSize > MAX_PAGE_SIZE_DMX) {
			qry.PageSize = MAX_PAGE_SIZE_DMX;
		}
		
		// create a productOldModelSR(result)
		ProductOldModelSR result = new ProductOldModelSR() {
			{
				message = "Unknown error";
			}
		};
		
		int maxPageSize = 1000;
		// Create query
		SearchSourceBuilder sb = searchProductOldModeDMXQuery(qry, maxPageSize);
		SearchRequest sr = new SearchRequest();
		sr.indices(CurrentIndexDB).source(sb);
		try {
			// Query to elasticsearchDB
			SearchResponse qr = clientIndex.search(sr, RequestOptions.DEFAULT);
			long rowCount = qr.getHits().getTotalHits().value;
			result.message = "Success";


//			intRowCount = queryResults.Total;
//			var tmp2 = queryResults.Documents;
//			if (tmp2 == null)
//				return null;

			// Create a collection.map contain model
			Map<Integer, ProductBO> modelList = new HashMap<>();
			// Get aggregations keyed by aggregation name.
			Map<String, Aggregation> aggrs = qr.getAggregations() == null ? null : qr.getAggregations().asMap();
			// agg tong hop het cac loai may cu
			var terms = (Terms) ((ParsedFilter) aggrs.get("filtered_aggs")).getAggregations().get("ByMin");
			// agg may cu 2 4
			var terms2 = (Terms) ((ParsedFilter) aggrs.get("filtered_aggs2")).getAggregations().get("ByMin2");
			// agg may cu HTB
			var terms3 = (Terms) ((ParsedFilter) aggrs.get("filtered_aggs3")).getAggregations().get("ByMin3");
			// agg may co km
			var terms5 = (Terms) ((ParsedFilter) aggrs.get("filtered_aggs5")).getAggregations().get("ByMin5");

			for (Terms.Bucket bucket : terms.getBuckets()) {

				double minval2 = 0;
				double minval3 = 0;
				int mBySum3 = 0;
				int mBySum2 = 0;
				int mBySum5 = 0;

				if (terms2 != null) {
					Bucket term3 = terms2.getBucketByKey((String) bucket.getKey());
					if (term3 != null) {
						// gia tu hang TB
						minval3 = ((Min) term3.getAggregations().get("MinPrice3")).getValue();
						mBySum3 = (int) ((Sum) term3.getAggregations().get("mBySum3")).getValue();
					}
				}

				if (terms2 != null) {
					Bucket term2 = terms2.getBucketByKey((String) bucket.getKey());
					if (term2 != null) {
						// gia tu hang 2 4
						minval3 = ((Min) term2.getAggregations().get("MinPrice2")).getValue();
						mBySum3 = (int) ((Sum) term2.getAggregations().get("mBySum2")).getValue();
					}
				}

				long valueCount = ((ValueCount) bucket.getAggregations().get("mByCount")).getValue();
				double quantityCount = ((Sum) bucket.getAggregations().get("mBySum")).getValue();
				double isImei = ((Sum) bucket.getAggregations().get("mByImei")).getValue();
				double minInventoryStatusID = ((Min) bucket.getAggregations().get("MinInventoryStatusID")).getValue();
				double minIsPromotion = ((Min) bucket.getAggregations().get("MinIsPromotion")).getValue();
				// gia goc nho nhat
				double min = ((Min) bucket.getAggregations().get("MinPrice")).getValue();
				// gia goc lon nhat
				double max = ((Max) bucket.getAggregations().get("MaxPrice")).getValue();
				double cateID = ((Min) bucket.getAggregations().get("CategoryID")).getValue();
				// gia oldid nho nhat sau km
				double minOldPrice = ((Min) bucket.getAggregations().get("MinOldPrice")).getValue();

				double maxOldPrice = ((Max) bucket.getAggregations().get("MaxOldPrice")).getValue();
				// max discount percent
				double discountValuePercent = ((Max) bucket.getAggregations().get("DiscountValuePercent")).getValue();
				// max discount percent
				double minDiscountValuePercent = ((Min) bucket.getAggregations().get("DiscountValuePercentMin"))
						.getValue();
//				double maxDiscountValue = ((Min) bucket.getAggregations().get("MaxDiscountValue")).getValue();
				double maxDeltaPercent = ((Max) bucket.getAggregations().get("MaxDeltaPercent")).getValue();

				double maxProductPrice = ((Min) bucket.getAggregations().get("MaxProductPrice")).getValue();
				int pid = (int) bucket.getKey();

				var mByCountProvince = ((Cardinality) bucket.getAggregations().get("mByCountProvince"));
//				double MinProvinceID = ((Min) bucket.getAggregations().get("MinProvinceID")).getValue();
//				double MinStoreID = ((Min) bucket.getAggregations().get("MinStoreID")).getValue();
				double minOldID = ((Min) bucket.getAggregations().get("MinOldID")).getValue();
				int pcount = (int) mByCountProvince.getValue();

				// Create a StringBuffer
				StringBuffer sbExtendInfo = new StringBuffer();
				sbExtendInfo.append("<MinPrice2>" + minval2 + "</MinPrice2> <MinPrice3>" + minval3
						+ "</MinPrice3><InventoryStatusID>" + minInventoryStatusID + "</InventoryStatusID>");

				sbExtendInfo.append("<MaxProductPrice>" + maxProductPrice + "</MaxProductPrice>");
				sbExtendInfo.append("<MaxOldPrice>" + maxOldPrice + "</MaxOldPrice>");
				// gia gach nho nhat (oldid nho nhat)
				sbExtendInfo.append("<MinOldPrice>" + minOldPrice + "</MinOldPrice>");

				if (valueCount == 1) {
					sbExtendInfo.append("<OldID>" + minOldID + "</OldID>");
				}
				//
				sbExtendInfo.append("<CountImei>" + valueCount + "</CountImei>");

				double discountValueMin = 0;
				double discountValueMax = 0;

				if (terms5 != null) {
					Bucket term5 = terms5.getBucketByKey((String) bucket.getKey());
					if (term5 != null) {
						discountValueMin = ((Min) term5.getAggregations().get("MinPrice5")).getValue();
						mBySum5 = (int) ((Sum) term5.getAggregations().get("mBySum5")).getValue();
						discountValueMax = ((Min) term5.getAggregations().get("MaxPrice5")).getValue();
					}
				}

				sbExtendInfo.append("<DiscountValueMin>" + discountValueMin + "</DiscountValueMin>");
				sbExtendInfo.append("<DiscountValueMax>" + discountValueMax + "</DiscountValueMax>");
				sbExtendInfo.append("<MaxPrice>" + max + "</MaxPrice>");
				sbExtendInfo.append("<SumQuantity7>" + mBySum3 + "</SumQuantity7>");
				sbExtendInfo.append("<SumQuantity2>" + mBySum2 + "</SumQuantity2>");

				// Create new a ProductBO
				ProductBO pbo = new ProductBO() {
					{
						ProductID = pid;
						// Total quantity
						ProductOldCount = (long) quantityCount;
						Price = min;
						CountLike = (long) isImei;
						ProvinceCount = pcount;
						ExtendInfo = sbExtendInfo.toString();
						MaxDiscountValue = (int) discountValuePercent;
						Warranty = (int) minDiscountValuePercent;
						// tam thoi lay filed nay thay the
						PercentInstallment = maxDeltaPercent;
						IsHavePromotion = (int) minIsPromotion > 0 ? true : false;
						CategoryID = (int) cateID;
					}
				};
				modelList.put(pid, pbo);

			}

			categoryList = new LinkedList<>();

			// Chua convert qua duoc ???
//			CategoryList = new List<int>();
//            var yearFacetItems = queryResults.FacetItems<FacetItem>(p => p.CategoryID);
//            foreach (var item in yearFacetItems)
//            {
//                var termItem = item as TermItem;
//                CategoryList.Add(Convert.ToInt32(termItem.Term));
//            }

			//
			Terms facetTermCategoryID = (Terms) aggrs.get("FacetTermCategoryID");
			for (Terms.Bucket term : facetTermCategoryID.getBuckets()) {
				categoryList.add((Integer) term.getKeyAsNumber());
				// Add aggregations facetTerms
				faceLst.add(new FaceObject() {
					{
						Type = 1;
						Key = (term.getKeyAsString());
						Count = (int) term.getDocCount();
					}
				});
			}

			//
			Terms facetTermManufactureID = (Terms) aggrs.get("FacetTermManufactureID");
			for (Terms.Bucket term : facetTermManufactureID.getBuckets()) {
				faceLst.add(new FaceObject() {
					{
						Type = 2;
						Key = (term.getKeyAsString());
						Count = (int) term.getDocCount();
					}
				});
			}

			// Get total Quantity in modelList
			totalQuantity = modelList.entrySet().stream().filter(x -> x.getKey() != null)
					.mapToInt(x -> (int) x.getValue().ProductOldCount).sum();

			List<ProductBO> listProduct = new LinkedList<>();

			if (modelList != null && modelList.size() > 0) {
				totalRecordFilter = modelList.size();

				// Filter modelList
				var promolist = modelList.entrySet().stream()
						.filter(x -> x.getValue() != null && x.getValue().IsHavePromotion == true)
						.collect(Collectors.toList());
				if (promolist != null) {
					totalPromotion = promolist.size();
				}
				
				if (qry.PageSize * qry.PageIndex >= totalRecordFilter) {
					return null;
				}

				// Sorting
				if (qry.IsHavePromotion == true) {

					if (qry.DiscountOrdertype == OrderType.LARGEST) {
						// Sort by MaxDiscountValue descending
						modelList = modelList.entrySet().stream()
								.sorted((e1, e2) -> Integer.compare(e2.getValue().MaxDiscountValue,
										e1.getValue().MaxDiscountValue))
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
										(oldValue, newValue) -> oldValue, LinkedMap::new));

					} else if (qry.DiscountOrdertype == OrderType.SMALLEST) {
						// Sort by MaxDiscountValue ascending
						modelList = modelList.entrySet().stream()
								.sorted((e1, e2) -> Integer.compare(e1.getValue().MaxDiscountValue,
										e2.getValue().MaxDiscountValue))
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
										(oldValue, newValue) -> oldValue, LinkedMap::new));
					}

				}
				if (qry.PriceOrdertype == OrderType.LARGEST) {
					// Sort by Price descending
					modelList = modelList.entrySet().stream()
							.sorted((e1, e2) -> Double.compare(e2.getValue().Price, e1.getValue().Price))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
									(oldValue, newValue) -> oldValue, LinkedMap::new));

				} else if (qry.PriceOrdertype == OrderType.SMALLEST) {
					// Sort by Price ascending
					modelList = modelList.entrySet().stream()
							.sorted((e1, e2) -> Double.compare(e1.getValue().Price, e2.getValue().Price))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
									(oldValue, newValue) -> oldValue, LinkedMap::new));

				} else if (qry.PriceOrdertype == OrderType.NORMAL) {
					// mac dinh sap xep theo % chenh lech may cu /may moi va so luong ton kho
					modelList = modelList.entrySet().stream()
							.sorted((e1, e2) -> compareProductBO(e2.getValue(), e1.getValue()))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
									(oldValue, newValue) -> oldValue, LinkedMap::new));
				}
				
				
				List<ProductBO> reList = new LinkedList<ProductBO>();
				// Top HomePage
				if (qry.ExtensionObject == 1) {
					reList = getTopHomePage(modelList, qry.PageSize, qry.listCategory.length);
				} else {
					reList = modelList.values().stream().skip(qry.PageIndex * qry.PageSize).limit(qry.PageSize)
							.collect(Collectors.toList());
				}
				
				if (reList != null) {

					for (var item : reList) {
						// Call api apiproduct/getproduct
						ProductBO productBO = WebserviceHelper.Call(0).Get(
								"apiproduct/getproduct?productID=:?&siteID=:?&provinceID=:?&lang=:?", ProductBO.class,
								item.ProductID, 2, 3, "vi-VN");

						Stream<ProductBO> modelFilter = modelList.values().stream()
								.filter(x -> x.ProductID == item.ProductID);
						if (productBO != null) {
							// Set value for productBO
							boolean IsPromotion = false;
							productBO.IsHavePromotion = IsPromotion;
							productBO.MaxDiscountValue = modelFilter.mapToInt(x -> x.MaxDiscountValue).findFirst()
									.orElse(0);
							productBO.lstPromotionBO = modelFilter.map(x -> x.lstPromotionBO).findFirst()
									.orElse(new ArrayList<>());
							// contain realprice
							productBO.ProductOldPriceFrom = modelFilter.map(x -> x.Price).findFirst().orElse(0.0);
							productBO.ProductOldCount = modelFilter.map(x -> x.ProductOldCount).findFirst().orElse(0L);
							// isimei
							productBO.CountLike = modelFilter.map(x -> x.CountLike).findFirst().orElse(0L);
							productBO.ProvinceCount = modelFilter.map(x -> x.ProvinceCount).findFirst().orElse(0);
							productBO.ExtendInfo = modelFilter.map(x -> x.ExtendInfo).findFirst().orElse("");
							productBO.ProvinceCount = modelFilter.mapToInt(x -> x.ProvinceCount).findFirst().orElse(0);
							// Tam thoi lay field nay thay the
							productBO.PercentInstallment = modelFilter.mapToDouble(x -> x.PercentInstallment)
									.findFirst().orElse(0.0);
							productBO.MaxDiscountValue = modelFilter.mapToInt(x -> x.MaxDiscountValue).findFirst()
									.orElse(0);
							productBO.Warranty = modelFilter.map(x -> x.Warranty).findFirst().orElse(0);
						}
						listProduct.add(productBO);
					}
				}
				
				// Add data return
				result.totalPromotion = totalPromotion;
				result.totalQuantity = totalQuantity;
				result.categoryList = categoryList;
				result.totalRecordFilter = totalRecordFilter;
				result.faceLst = faceLst;
                result.list = listProduct;
			}

		} catch (IOException e) {
			e.printStackTrace();
			Logs.LogException("ProductOldHelper - Search error");
			Logs.LogException(e);
			result.message = e.getMessage();
		}
		return result;
	}

	/**
	 * Create query to elasticsearchDB
	 * 
	 * @param qry
	 * @param maxPageSize
	 * @return
	 */
	private SearchSourceBuilder searchProductOldModeDMXQuery(ProductOldModelQuery qry, int maxPageSize) {
		// Filter
		BoolQueryBuilder query = searchProductOldModeDMXFilter(qry);
		
		// Request query
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.from(qry.PageIndex * qry.PageSize).size(qry.PageSize).query(query);
		
		// Aggregations
		searchProductOldModeDMXAggregations(maxPageSize, searchSourceBuilder);
		return searchSourceBuilder;
	}
	
	/**
	 * Filter query for method searchProductOldModeDMX
	 * 
	 * @param productOldModel
	 * @return {@code BoolQueryBuilder}
	 */
	private BoolQueryBuilder searchProductOldModeDMXFilter(ProductOldModelQuery productOldModel) {
		BoolQueryBuilder query = boolQuery();

		if (productOldModel.CategoryID > 0) {
			// 2823: Phụ kiện điện máy
			if (productOldModel.CategoryID == 2823) {
				query.must(termsQuery("CategoryID", new int[] { 2824, 2825, 5452, 5005, 7186 }));
			} else {
				query.must(termQuery("categoryID", productOldModel.CategoryID));
			}
		}

		if (productOldModel.listCategory != null && productOldModel.listCategory.length > 0) {
			query.must(termsQuery("CategoryID", productOldModel.listCategory));
		}

		if (productOldModel.ProductID > 0) {
			query.must(termQuery("ProductID", productOldModel.ProductID));
		}

		if (productOldModel.ManufactureID > 0) {
			query.must(termQuery("manufactureID", productOldModel.ManufactureID));
		}

		if (productOldModel.ManufactureIds != null && productOldModel.ManufactureIds.length > 0) {
			query.must(termsQuery("ManufactureID", productOldModel.ManufactureIds));
		}

		// Don't by list prodcutID (khong lay theo list productid)
		if (productOldModel.ListProductID == null || productOldModel.ListProductID.length <= 0) {
			if (productOldModel.ImeiStatus == 0) {
				query.must(termQuery("IsImei", 0));
			}
			if (productOldModel.ImeiStatus == 1) {
				query.must(termQuery("IsImei", 1));
			}
		}

		if (productOldModel.ProvinceIDList != null && productOldModel.ProvinceIDList.length > 0) {
			query.must(termsQuery("ProvinceID", productOldModel.ProvinceIDList));
		}

		if (productOldModel.DistrictID > 0) {
			query.must(termQuery("DistrictID", productOldModel.DistrictID));
		}

		if (productOldModel.StoreID > 0) {
			query.must(termQuery("StoreID", productOldModel.StoreID));
		}

		if (productOldModel.ListProductID != null && productOldModel.ListProductID.length > 0) {
			query.must(termsQuery("ProductID", productOldModel.ListProductID));
		}

		// Remove product-lock
		query.must(boolQuery().should(termQuery("IsErpLock", 0)).should(termQuery("IsErpLock", 1)))
				.must(rangeQuery("UnlockDate").lt(new Date()));

		if (productOldModel.IsHavePromotion == true) {
			query.must(boolQuery().should(boolQuery().must(rangeQuery("NewDiscountValue").gte(1))
					.must(rangeQuery("DeltaPercent_ProductPrice_RealPrice").gte(61))));
		}

		// Loai sp vien thong laptop.. o box gia soc
		if (productOldModel.promotionType == 9) {
			query.mustNot(termQuery("CategoryID", ACCESSORY_CATEGORY));
			query.mustNot(termQuery("CategoryID", new String[] { "42", "522", "7077", "44", "5698", "5697", "5693" }));
		}

		if (productOldModel.ListInventoryStatusID != null && productOldModel.ListInventoryStatusID.length > 0) {
			query.must(termsQuery("InventoryStatusID", productOldModel.ListInventoryStatusID));
		}

		query.must(rangeQuery("NewRealPrice2").gte(1));
		query.must(rangeQuery("Price2").gte(1));
		query.must(rangeQuery("Quantity").gte(1));

		if (productOldModel.MinPrice == 0 && productOldModel.MaxPrice > 0) {
			query.must(rangeQuery("NewRealPrice2").gte((long) productOldModel.MinPrice + 1));
			query.must(rangeQuery("NewRealPrice2").lte((long) productOldModel.MaxPrice));
		} else if (productOldModel.MaxPrice >= productOldModel.MinPrice && productOldModel.MinPrice > 0) {
			query.must(rangeQuery("NewRealPrice2").gte((long) productOldModel.MinPrice));
			query.must(rangeQuery("NewRealPrice2").lte((long) productOldModel.MaxPrice));
		} else if (productOldModel.MinPrice > 0 && productOldModel.MaxPrice == 0) {
			query.must(rangeQuery("NewRealPrice2").gte((long) productOldModel.MinPrice));
		}

		// Filter prices (filter nhieu gia)
		if (productOldModel.PriceFilters != null && productOldModel.PriceFilters.length > 0) {
			BoolQueryBuilder subQueryBuider = new BoolQueryBuilder();
			for (PriceFilter priceFilter : productOldModel.PriceFilters) {
				subQueryBuider.must(rangeQuery("NewRealPrice2").gte(priceFilter.minPrice));
				subQueryBuider.must(rangeQuery("NewRealPrice2").lte(priceFilter.maxPrice));

			}
			query.must(boolQuery().should(subQueryBuider));
		}

		// filter nhieu thuoc tinh and or
		if (productOldModel.propertyDetailFilters != null && productOldModel.propertyDetailFilters.size() > 0) {
			StringBuffer sbProp = new StringBuffer();
			Map<Integer, List<PropertyDetail>> map = productOldModel.propertyDetailFilters.stream()
					.collect(Collectors.groupingBy(p -> p.propertyID));
			for (Map.Entry<Integer, List<PropertyDetail>> entry : map.entrySet()) {
				List<String> propQueryString = entry.getValue().stream()
						.map(prop -> ("prop" + prop.propertyID + "_" + prop.propValueID)).collect(Collectors.toList());
				if (propQueryString != null && propQueryString.size() > 0) {
					sbProp.append("( ");
					sbProp.append(String.join(" OR ", propQueryString));
					sbProp.append(" ) AND ");
				}
			}
			// Get size of the list sbProp
			int sbPropLength = sbProp.length();
			if (sbPropLength > 0) {
				// remove the last element("AND") in the list "sbProp"
				String propQuery = sbProp.substring(0, sbPropLength - 4);
				query.must(queryStringQuery(propQuery).field("PropStr"));
			}
		}

		// #Region parameter (region thuộc tính)
		if (productOldModel.PropertyNameURL != null && productOldModel.PropertyNameURL.length() > 0) {
			productOldModel.PropertyNameURL = productOldModel.PropertyNameURL.trim().replace("-", "_");
			String[] nameUrls = productOldModel.PropertyNameURL.split(",");
			if (nameUrls != null && nameUrls.length > 0) {
				for (String nameUrl : nameUrls) {
					if (nameUrl != null) {
						query.must(termQuery("PropStr", nameUrl));
					}
				}
			}

		}
		// Khong conver duoc, vi ko tim thay ham thay cho cache
//      if (!string.IsNullOrEmpty(qry.FeatureNameURL))
//      {
//          //prop6718_co
//          var list = qry.FeatureNameURL.Split(new char[] { ',' }, StringSplitOptions.RemoveEmptyEntries);
//          List<string> newlist = new List<string>();
//          if (list != null && list.Length > 0)
//          {
//              foreach (var item in list)
//              {
//
//                  var key = string.Format(DataKey.ProductModule.Product.PROPV_LST_BY_CAT_SITE, qry.CategoryID, 2);
//                  var feature = iCached.Get<ProductRef.ProductPropBO[]>(key);
//                  if (feature != null)
//                  {
//                      var obj = feature.FirstOrDefault(p => p.PropertyID == Convert.ToInt32(item));
//                      if (obj != null)
//                          newlist.Add(string.Format("prop{0}_{1}", item, GenSEOUrl(obj.ProductPropValueBOLst.First().Value).Replace("-", "_")));
//                  }
//
//              }
//              querym &= Query<ProductOldImeiSO>.Terms(f => f.PropStr, newlist.ToArray());
//          }
//      }

		if (productOldModel.FeatureNameURL != null && productOldModel.FeatureNameURL.length() > 0) {
			List<String> nameUrls = Arrays.asList(productOldModel.FeatureNameURL.split(",")).stream()
					.filter(f -> f != null).collect(Collectors.toList());
			if (nameUrls != null && nameUrls.size() > 0) {
				for (String nameUrl : nameUrls) {
					String key = String.format("MD_1_PROPV_LST_BY_CAT_SITE_%d_%d", productOldModel.CategoryID, 2);

				}
			}
		}

		if (productOldModel.Keyword != null && productOldModel.Keyword.length() > 0) {
			String keyword = DidxHelper
					.FormatKeywordSearchField(DidxHelper.convertISOToUnicode(productOldModel.Keyword));
			if (keyword != null && keyword.length() > 0) {
				// Loc nghanh hang muon kinh doanh may cu tren web khi tim kiem
				query.must(termsQuery("CategoryID",
						new int[] { 1942, 1943, 1944, 2002, 42, 44, 522, 7077, 1962, 2222, 3385, 2162, 2022, 1983, 1982,
								3305, 2063, 1987, 1986, 1984, 1922, 1989, 2064, 2062, 1985, 1988, 1990, 2342, 2202,
								5473, 4645, 1991, 1992, 57, 55, 382, 58, 54, 75, 86, 1882, 7498, 8762 }));
			}

			// #region search bỏ phụ kiện
			query.mustNot(termsQuery("categoryID", ACCESSORY_CATEGORY));

			List<String> keywords = Arrays.asList(keyword.split(" ")).stream().filter(k -> k != null)
					.collect(Collectors.toList());

			String rsl = "";
			if (keywords.size() == 1) {
				rsl = "(*" + keyword + "* OR (keyword:" + keyword + ") OR (keyword:\"" + keyword + "\")) ";

			} else {
				rsl = "((keyword:\"" + keyword + "\") OR (keyword:\"" + keyword + "*\") OR ("
						+ String.join(" AND ", keywords) + "*) OR (keyword:" + String.join(" AND ", keywords) + ") ) ";
			}
			query.must(
					QueryBuilders
							.functionScoreQuery(
									QueryBuilders.boolQuery().must(queryStringQuery(
											rsl).field("Keyword")),
									new FilterFunctionBuilder[] { new FilterFunctionBuilder(
											termsQuery("CategoryID",
													new String[] { "42", "44", "522", "1942", "1943", "1944", "2002" }),
											scriptFunction("1000000   ")) })
							.scoreMode(FunctionScoreQuery.ScoreMode.SUM).boostMode(CombineFunction.SUM));
		}
		
		return query;
	}

	/**
	 * Aggregation after query to elasticsearchDB
	 * 
	 * @param maxPageSize
	 * @param searchSourceBuilder
	 */
	private void searchProductOldModeDMXAggregations(int maxPageSize, SearchSourceBuilder searchSourceBuilder) {
		// Loc theo may cu [2, 4]
		FilterAggregationBuilder filteredAggs2 = AggregationBuilders
				// Agg may cu 2 4
				.filter("filtered_aggs2", termsQuery("InventoryStatusID", new int[] { 2, 4 }))
				.subAggregation(terms("ByMin2").field("ProductID").size(maxPageSize)
						// Order theo gia sau km giam gian
						.order(BucketOrder.aggregation("MinPrice2", false))
						// Min gia sau km (gia tu)
						.subAggregation(min("MinPrice2").field("NewRealPrice2"))
						// Sum so luong ton kho cua tat ca oldid
						.subAggregation(sum("mBySum2").field("Quantity")));

		// Loc theo hang trung bay
		FilterAggregationBuilder filteredAggs3 = AggregationBuilders
				.filter("filtered_aggs3", termQuery("InventoryStatusID", 7))
				.subAggregation(terms("ByMin3").field("ProductID").size(maxPageSize)
						// Order theo gia sau km giam gian
						.order(BucketOrder.aggregation("MinPrice3", false))
						// Min gia sau km (gia tu)
						.subAggregation(min("MinPrice3").field("NewRealPrice2"))
						// Sum so luong ton kho cua tat ca oldid
						.subAggregation(sum("mBySum3").field("Quantity")));

		// Loc theo model co km
		FilterAggregationBuilder filtered_aggs5 = AggregationBuilders
				.filter("filtered_aggs5", rangeQuery("NewDiscountValue").gte(1))
				.subAggregation(terms("ByMin5").field("ProductID").size(maxPageSize)
						.order(BucketOrder.aggregation("MinPrice5", false))
						// gia tri km nho nhat
						.subAggregation(min("MinPrice5").field("NewDiscountValue"))
						// gia tri km nho nhat
						.subAggregation(min("MinDiscountValue5").field("NewDiscountValuePercent"))
						// gia tri km lon nhat
						.subAggregation(max("MaxPrice5").field("NewDiscountValue"))
						// tong so luong ton kho cua tat ca oldid
						.subAggregation(sum("mBySum5").field("Quantity")));

		// Loc theo may cu [2, 4] va hang trung bay [7]
		FilterAggregationBuilder filtered_aggs = AggregationBuilders
				// Agg tong hop tat ca
				.filter("filtered_aggs", termsQuery("InventoryStatusID", new int[] { 2, 4, 7 }))
				// Cung productid lay min
				.subAggregation(terms("ByMin").field("ProductID").size(maxPageSize)
						.order(BucketOrder.aggregation("MinPrice", false))
						// Gia sau km nho nhat
						.subAggregation(min("MinPrice").field("NewRealPrice2"))
						// Gia sau km lon nhat
						.subAggregation(max("MaxPrice").field("NewRealPrice2"))
						// Gia tri km lon nhat (so tien)
						.subAggregation(max("MaxDiscountValue").field("NewDiscountValue"))
						// Tinh ra % discount lonnhat
						.subAggregation(max("DiscountValuePercent").field("NewDiscountValuePercent"))
						// Tinh ra % discount min
						.subAggregation(min("DiscountValuePercentMin").field("NewDiscountValuePercent"))
						// Chenh lech gia may cu may moi (gia truoc km)
						.subAggregation(max("MaxDeltaPercent").field("DeltaPercent_ProductPrice_RealPrice"))
						// Gia may moi
						.subAggregation(max("MaxProductPrice").field("ProductPrice"))
						// Gia oldid lon nhat
						.subAggregation(max("MaxOldPrice").field("Price2"))
						// Gia old nho nhat
						.subAggregation(min("MinOldPrice").field("Price2"))
						// Tong so luong ton kho cua cac oldid
						.subAggregation(sum("mBySum").field("Quantity"))
						// Tong so luong oldid
						.subAggregation(count("mByCount").field("OLDID"))
						// Tong so luong IsImei
						.subAggregation(sum("mByImei").field("IsImei"))
						// Lay province nho nhat
						.subAggregation(min("MinProvinceID").field("ProvinceID"))
						// Lay InventoryStatusID nho nhat
						.subAggregation(min("MinInventoryStatusID").field("InventoryStatusID"))
						//
						.subAggregation(cardinality("mByCountProvince").field("ProvinceID"))
						// Lay storeID nho nhat
						.subAggregation(min("MinStoreID").field("StoreID"))
						// Lay OLDID nho nhat
						.subAggregation(min("MinOldID").field("OLDID"))
						// Lay IsPromotion nho nhat
						.subAggregation(min("MinIsPromotion").field("IsPromotion"))
						// Lay CategoryID nho nhat
						.subAggregation(min("CategoryID").field("CategoryID")));

		//
		TermsAggregationBuilder termFacetTermCategoryID = AggregationBuilders.terms("FacetTermCategoryID")
				.field("CategoryID").size(maxPageSize).subAggregation(sum("Sum").field("ProductID"));

		TermsAggregationBuilder termFacetTermManufactureID = AggregationBuilders.terms("FacetTermManufactureID")
				.field("ManufactureID").size(maxPageSize).subAggregation(sum("Sum").field("ProductID"));

		// Loc theo may cu [2, 4]
		searchSourceBuilder.aggregation(filteredAggs2);
		// Loc theo hang trung bay
		searchSourceBuilder.aggregation(filteredAggs3);
		// Loc theo model co km
		searchSourceBuilder.aggregation(filtered_aggs5);
		//
		searchSourceBuilder.aggregation(filtered_aggs);
		//
		searchSourceBuilder.aggregation(termFacetTermCategoryID);
		//
		searchSourceBuilder.aggregation(termFacetTermManufactureID);
	}
	
	
	/**
	 * Compare two {@code productBO} by parameter is 'PercentInstallment'
	 * <p>
	 * if {@code d1.PercentInstallment} == {@code d2.PercentInstallment} then
	 * continue compare with parameter {@code ProductOldCount}
	 * 
	 * @param x
	 * @param y
	 * @return the value {@code 0} if {@code x == y}; a value less than {@code 0} if
	 *         {@code x < y}; and a value greater than {@code 0} if {@code x > y}
	 */
	private int compareProductBO(ProductBO x, ProductBO y) {

		if (x.PercentInstallment < y.PercentInstallment)
			return -1; // Neither val is NaN, thisVal is smaller
		if (x.PercentInstallment > y.PercentInstallment)
			return 1; // Neither val is NaN, thisVal is larger

		// Cannot use doubleToRawLongBits because of possibility of NaNs.
		long thisBits = Double.doubleToLongBits(x.PercentInstallment);
		long anotherBits = Double.doubleToLongBits(y.PercentInstallment);
		return (thisBits == anotherBits ? Long.compare(x.ProductOldCount, y.ProductOldCount)
				: (thisBits < anotherBits ? -1 : 1));
	}
	
	/**
	 * Get top home page
	 * 
	 * @param modelList
	 * @param pageSize
	 * @param listCategorySize
	 * @return a Collection.Map group by {@code CategoryID} have key is
	 *         {@code CategoryID} and value {@code List<ProductBO>}
	 */
	private List<ProductBO> getTopHomePage(Map<Integer, ProductBO> modelList, int pageSize, int listCategorySize) {
		Map<Integer, List<ProductBO>> result = new HashMap<>();
		int countPerCate = pageSize / listCategorySize;

		for (ProductBO productBO : modelList.values()) {
			if (result.containsKey(productBO.CategoryID)) {
				if (result.get(productBO.CategoryID).size() <= countPerCate) {
					result.get(productBO.CategoryID).add(productBO);
				}
			} else {
				result.put(productBO.CategoryID, Arrays.asList(productBO));
			}

		}
		return result.values().stream().flatMap(List::stream).collect(Collectors.toList());
		
		
	}

	/**
	 * Get feature for product old
	 * 
	 * @param isHome
	 * @param categoryID
	 * @param provinceID
	 * @param siteID
	 * @return Return {@code List<ProductBO>}
	 * @throws Throwable
	 */
	public List<ProductBO> getFeatureProductOld(int isHome, int categoryID, int provinceID, int siteID)
			throws Throwable {
		var listFinal = new ArrayList<ProductBO>();

		// Call to OrientDB with function 'productfeature_getBySite'
		var listFeature = factoryRead.QueryFunction("productfeature_getBySite", ProductFeatureBO[].class, false, isHome,
				categoryID, siteID, 0);

		if (listFeature == null || listFeature.length <= 0) {
			return null;
		}

		// Filter by fromDate and toDate
		Date now = new Date();
		List<ProductFeatureBO> listFeatureProducts = Stream.of(listFeature)
				.filter(x -> x.FromDate != null && x.FromDate.before(now) && x.ToDate != null && x.ToDate.after(now))
				.collect(Collectors.toList());

		long totalPromotion = 0;

		// Create a ProductOldModelQuery
		ProductOldModelQuery productOldModelQuery = new ProductOldModelQuery() {
			{
				PageSize = listFeatureProducts.size();
				PageIndex = 0;
				ProvinceIDList = provinceID <= 0 ? null : new int[] { provinceID };
				// Set parameter for TGDD
				if (siteID == 1) {
					DistrictID = -1;
					DiscountOrdertype = OrderType.DESCENDING;
					ExtensionObject = 9999;
					CategoryIdList = listFeatureProducts.stream().map(x -> x.ProductID + "")
							.collect(Collectors.joining(","));
				}
				// Set parameter for DMX
				if (siteID == 2) {
					ListProductID = listFeatureProducts.stream().mapToLong(x -> x.ProductID).toArray();
					ImeiStatus = -1;
					promotionType = 10;
					IsHavePromotion = false;
				}
			}
		};

		// Call to method searchProductOldModeDMX
		ProductOldModelSR lstProductOldModel = searchProductOldModel(productOldModelQuery);

		List<ProductBO> lstProduct = lstProductOldModel.list;

		// bo vao display order
		if (lstProduct != null && lstProduct.size() > 0) {
			for (var item : lstProduct) {
				item.DisplayOrder = listFeatureProducts.stream().filter(x -> x.ProductID == item.ProductID)
						.map(x -> x.DisplayOrder).findFirst().orElse(0);
			}
		}

		// Sap xep thep ti le
		Map<List<Integer>, Integer> keyValue = new HashMap<>();

		// Dien Thoai(Phone) 8: 42
		keyValue.put(List.of(42), 8);
		// Tablet 8: 522
		keyValue.put(List.of(522), 8);
		// Laptop(Laptop + May tinh bo(Computer) + Mang hinh(Screen) + May in(Printer))
		keyValue.put(List.of(44, 5698, 5697, 5693), 4);
		// Dong ho(Smartwatch) 3: 7077
		keyValue.put(List.of(7077), 3);
		// Phu kien(Accessory) 3: List accessory
		keyValue.put(ACCESSORY_CATEGORY, 3);
		keyValue.entrySet().stream().forEach(item -> {
			listFinal.addAll(lstProduct.stream().filter(x -> item.getKey().contains(x.CategoryID))
					.sorted(Comparator.comparing(x -> x.DisplayOrder)).limit(item.getValue())
					.collect(Collectors.toList()));
		});

		return listFinal;
	}
	
}

