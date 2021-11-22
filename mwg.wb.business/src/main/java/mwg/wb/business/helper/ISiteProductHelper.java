package mwg.wb.business.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import mwg.wb.business.ProductHelper;
import mwg.wb.client.elasticsearch.dataquery.ProductQuery;
import mwg.wb.client.service.CodeTimers;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.Logs;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.commonpackage.SuggestSearchSO;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductDetailBO;
import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.model.promotion.Promotion;
import mwg.wb.model.search.ProductSO;
import mwg.wb.model.searchresult.ProductSOSR;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder.ScriptSortType;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.search.sort.SortBuilders.scriptSort;

public abstract class ISiteProductHelper {

	protected RestHighLevelClient clientIndex = null;
		 
	protected ObjectMapper mapper = null;
	protected ClientConfig config = null;
	protected ProductHelper productHelper = null;

	protected ISiteProductHelper(ClientConfig aconfig, RestHighLevelClient _clientIndex, ObjectMapper _mapper,
			ProductHelper productHelper) {
		clientIndex = _clientIndex;
		mapper = _mapper;
		config = aconfig;
		this.productHelper = productHelper;
		
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		 
	}
 
	public void getPromotions(ProductBO product, ProductErpPriceBO def, int siteID) {
	}

	public void processPromotion(Promotion item, int productID, int siteid, String Lang, int InventoryStatusID,
			ErpHelper erpHelper, List<Promotion> listTempPromotion) throws Throwable {
	}

	public void processPromotion2(Promotion item, ProductBO product, int siteid, String Lang, int InventoryStatusID,
			ErpHelper erpHelper, List<Promotion> listTempPromotion) throws Throwable {
	}

	public AggregationBuilder addAggregation() {
		return AggregationBuilders.nested("FacetPropIdList", "listproperty")
				.subAggregation(AggregationBuilders.terms("name").field("listproperty.propkey").size(1000));
	}

	public abstract ProductSOSR SearchProduct(ProductQuery qry, boolean isGetFacetManu, boolean isGetFacetCate,
			boolean isGetFacetProp, CodeTimers timer) throws Throwable;

	public void processDetail(ProductBO product, int siteID, int provinceID, Integer storeID, String lang,
			CodeTimers timer) throws Throwable {
	}

	/**
	 * Danh cho ham search
	 * 
	 * @param products
	 * @param provinceID
	 * @param siteID
	 * @param languageID
	 */
	public void processSimpleDetails(ProductBO[] products, int provinceID, int siteID, String languageID)
			throws Throwable {
	}

	public int[] GetRelativeProductIDs(ProductBO product, int provinceID, int siteID, String lang, int propertyID,
			int propertyValueID, int property2ID, int property2ValueID, double price, boolean status4)
			throws Throwable {

		SearchSourceBuilder sb = new SearchSourceBuilder().fetchSource(new String[] { "ProductID" }, null);

		SearchRequest searchRequest = new SearchRequest(productHelper.CurrentIndexDB);
		SearchResponse queryResults = null;

		String pricef = "Prices.Price_" + provinceID;

		var query = boolQuery().must(termQuery("SiteID", siteID))
				.must(termQuery("Lang", lang.toLowerCase().replace("-", "_")))
				.must(termQuery("CategoryID", product.CategoryID)).must(termQuery("IsCollection", 0))
					.mustNot(termQuery("ProductID", product.ProductID));

		query.must(termQuery("HasBimage",1));
		query.mustNot(termQuery("CmsProductStatus", 2));
		if(product.IsMultiAirConditioner == true) {
			query.must(termQuery("IsMultiAirConditioner",true));
		}
		if (status4)
			query.must(termQuery("Prices.WebStatusId_" + provinceID, 4));
		else
			query.must(termsQuery("Prices.WebStatusId_" + provinceID, new int[] { 2, 3, 4, 5, 6, 8, 9 }));
		if(product.ProductCategoryBO != null && product.ProductCategoryBO.IsSuggestManu == 1){

			query.must(termQuery("ManufactureID", product.ProductManuBO.ManufactureID));
//			HashMap<String, Object> scriptParams = new HashMap<>();
//			// query.must(termQuery("ManufactureID", product.ProductManuBO.ManufactureID));
//			// ưu tiên sp cùng hãng lên trước
//			query.must(QueryBuilders.functionScoreQuery(
//					new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
//
//							new FunctionScoreQueryBuilder.FilterFunctionBuilder(scriptFunction(new Script(
//									ScriptType.INLINE, "painless",
//									"if(doc['ManufactureID'].value == "+product.ProductManuBO.ManufactureID+"){ 1000 }else{ 20000 } ",
//									scriptParams)))
//					})
//					.scoreMode(FunctionScoreQuery.ScoreMode.SUM).boostMode(CombineFunction.SUM));
		}
		if(product.ProductErpPriceBO != null && product.ProductErpPriceBO.Price > 0){
//			query.must(scriptQuery(new Script(" doc['Prices.Price_" + provinceID + "'].value >= " + product.ProductErpPriceBO.Price * 0.8 + " ")));
//			query.must(scriptQuery(new Script(" doc['Prices.Price_" + provinceID + "'].value <= " + product.ProductErpPriceBO.Price * 0.8 + " ")));
			query.must(rangeQuery("Prices.Price_" + provinceID ).gte(product.ProductErpPriceBO.Price * 0.8));
			query.must(rangeQuery("Prices.Price_" + provinceID ).lte(product.ProductErpPriceBO.Price * 1.2));
		}


		if (propertyID > 0 && propertyValueID > 0)
			query.must(termQuery("PropStr", "prop" + propertyID + "_" + propertyValueID));
		if (property2ID > 0 && property2ValueID > 0)
			query.must(termQuery("PropStr", "prop" + property2ID + "_" + property2ValueID));

//		var scripts = new FilterFunctionBuilder[] {
//				new FilterFunctionBuilder(scriptFunction(" if(doc['ManufactureID'].value != "+product.ProductManuBO.ManufactureID+"){ Math.abs(" + price + "- doc['" + pricef + "'].value) }")) };
//		query.must(
//				functionScoreQuery(matchAllQuery(), scripts).scoreMode(ScoreMode.SUM).boostMode(CombineFunction.SUM));

		sb.from(0).size(10);
		
		sb.sort(scriptSort(new Script(
				"if(doc['Prices.Price_" + provinceID + "'].size() > 0 ){ Math.abs(" + price + " - doc['Prices.Price_" + provinceID + "'].value) }"),
				ScriptSortType.NUMBER).order(SortOrder.ASC));		

		
		sb.query(query);
		searchRequest.source(sb.query(query));
		try {
			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		var finalResult = queryResults.getHits();

		return Stream.of(finalResult.getHits()).mapToInt(x -> {
			try {
				return mapper.readValue(x.getSourceAsString(), ProductSO.class).ProductID;
			} catch (IOException e) {
				Logs.LogException(e);
				return -1;
			}
		}).filter(x -> x > 0).toArray();
	}

	/**
	 * Dùng để xử lý promotion sub brand
	 */
	public void processPromotion(Promotion item) throws Throwable {
	}
	
	public int[] GetHomePageProduct2020(int category,int pageSize,int provinceID) {
		return new int[0];
	}
	
	public int[] GetAccessoryHomePageProductNew(int manuID,int countPerCat,int provinceID) {
		return new int[0];
	}
	public int[] GetAccessoryHomePageProductApple2021(int provinceID) {
		return new int[0];
	}
	
	public int[] GetGenuineAccessoryHomePage(String[] ListManuName, int provinceID) {
		return new int[0];
	}
	
	public SuggestSearchSO[] GetSuggestSearch(String keyword, int siteID) throws Throwable {
		return null;
	}

	public String getDisadvantage(ProductDetailBO[] detail, int siteID) throws JsonProcessingException {
		return null;
	}
}
