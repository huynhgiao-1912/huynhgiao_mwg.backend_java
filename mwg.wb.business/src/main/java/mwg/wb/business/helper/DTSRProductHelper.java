package mwg.wb.business.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.business.ProductHelper;
import mwg.wb.client.elasticsearch.dataquery.OrderType;
import mwg.wb.client.elasticsearch.dataquery.ProductQuery;
import mwg.wb.client.elasticsearch.dataquery.ProductQuery.SearchType;
import mwg.wb.client.service.CodeTimers;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.search.ProductSO;
import mwg.wb.model.searchresult.FaceCategorySR;
import mwg.wb.model.searchresult.FaceManuSR;
import mwg.wb.model.searchresult.FacePropSR;
import mwg.wb.model.searchresult.ProductSOSR;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery.ScoreMode;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder.FilterFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder.ScriptSortType;
import org.elasticsearch.search.sort.SortOrder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.scriptFunction;
import static org.elasticsearch.search.aggregations.AggregationBuilders.*;
import static org.elasticsearch.search.sort.SortBuilders.scriptSort;

public class DTSRProductHelper extends ISiteProductHelper {

	public DTSRProductHelper(ClientConfig aconfig, RestHighLevelClient _clientIndex, ObjectMapper _mapper,
			ProductHelper productHelper) {
		super(aconfig, _clientIndex, _mapper, productHelper);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ProductSOSR SearchProduct(ProductQuery qry, boolean isGetFacetManu, boolean isGetFacetCate,
									 boolean isGetFacetProp, CodeTimers timers) {
		ProductSOSR result = new ProductSOSR();
		SearchSourceBuilder sb = new SearchSourceBuilder();
		sb.fetchSource(new String[] { "_score", "ProductID", "Prices.WebStatusId_" + qry.ProvinceId,
				"Prices.IsShowHome_" + qry.ProvinceId, "Prices.Price_" + qry.ProvinceId,
				"Prices.ProductCode_" + qry.ProvinceId }, null);

//		String strCateid = qry.CategoryId + "";
		boolean isAccessory = ProductHelper.AccessoryCategory.contains(qry.CategoryId);
//				Arrays.stream(ProductHelper.AccessoryCategory).anyMatch(x -> x == qry.CategoryId);

		var query = boolQuery();

		var cateids = qry.CategoryIdList;
		if (cateids != null && cateids.length > 0)
			query.must(termsQuery("CategoryID", cateids));
		else if (qry.CategoryId > 0) {
			if (qry.CategoryId == 2422)
				query.must(termsQuery("CategoryID", "60", "1662"));
			else if (qry.CategoryId == 1825)
				query.must(termsQuery("CategoryID", "57", "56"));
			else if (qry.CategoryId == 1862)
				query.must(termsQuery("CategoryID", "58", "346"));
			else if (qry.CategoryId == 1622)
				query.must(termsQuery("CategoryID", "1363", "1823"));
			else if (qry.CategoryId == 44)
				query.must(termsQuery("CategoryID", "44", "5698"));
			else if (qry.CategoryId == 382) // loa, loa kéo
				query.must(termsQuery("CategoryID", "382", "2162"));
			else {
				query.must(boolQuery().should(termQuery("CategoryID", qry.CategoryId))
						.should(termQuery("CategoryMap", "c" + qry.CategoryId + "c")));
			}

//			if (qry.CategoryId == 44 || qry.CategoryId == 42 || qry.CategoryId == 522) {
//				// Đối với ngành hàng chính, chỉ lấy sản phẩm có slider đầy đủ
////                   if (qry.WebStatus != 0)
////                   {
////                       filter &= !Query<ProductSO>.Term(k => k.SliderStatus, 2);
////                   }
//			} else // doi voi pk siêu rẻ chỉ lấy giá 402>0
//			{
//				query.must(rangeQuery("Prices.Price402_" + qry.ProvinceId).gte(1));
//			}
//			if (qry.CategoryId != 44 && qry.CategoryId != 42 && qry.CategoryId != 522) {
//				// doi voi pk siêu rẻ chỉ lấy giá 402>0
//				query.must(rangeQuery("Prices.Price402_" + qry.ProvinceId).gt(0));
//			}
//			else if (qry.WebStatus != 0)
//				// Đối với ngành hàng chính, chỉ lấy sản phẩm có slider đầy đủ
//				query.must(boolQuery().mustNot(termQuery("SliderStatus", 2)));
		}
		if (qry.SiteId > 0)
			query.must(termQuery("SiteID", qry.SiteId));
		if (qry.LanguageID != null && !qry.LanguageID.isBlank())
			query.must(termQuery("Lang", DidxHelper.GenTerm3(qry.LanguageID)));
		if (qry.ManufacturerIdList != null && qry.ManufacturerIdList.length > 0)
			query.must(termsQuery("ManufactureID", qry.ManufacturerIdList));
		else if (qry.ManufacturerId > 0)
			query.must(termQuery("ManufactureID", qry.ManufacturerId));
		if (qry.HasPromotion > 0)
			query.must(termQuery("HasPromotion", 1));

		if (qry.CategoryId == -1) {
			// bo nhung thang phu kien co gia 402=0
			query.mustNot(boolQuery().must(termQuery("Prices.Price402_" + qry.ProvinceId, 0))
					.must(termsQuery("CategoryID", ProductHelper.AccessoryCategory)));
		}
		if (qry.CategoryId == -4) {

			// Search chỉ ngành hàng chính
			query.must(termsQuery("CategoryID", ProductHelper.MainProductCategory));

		} else if (qry.CategoryId == -3) { // Khi search phụ kiện, bỏ qua các con collection, chỉ lấy detail
			query.must(termQuery("IsCollection", 0));
			query.must(termsQuery("CategoryID", ProductHelper.AccessoryCategory));
			query.must(boolQuery()
					.should(termsQuery("Prices.WebStatusId_" + qry.ProvinceId,
							new String[] { "2", "3", "4", "5", "6", "8", "9" }))
					.should(termQuery("IsReferAccessory", 1)));

			query.must(rangeQuery("Prices.Price402_" + qry.ProvinceId).gte(1));

		} else if (qry.SearchType != null) {
			if (qry.SearchType == SearchType.SEARCH)
				query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, "4", "5", "1"));
			else if (qry.SearchType == SearchType.CATE)
				query.must(termQuery("Prices.WebStatusId_" + qry.ProvinceId, 4));
		} else {
			if (qry.WebStatus > 0)
				query.must(termQuery("Prices.WebStatusId_" + qry.ProvinceId, qry.WebStatus));
			else if (qry.WebStatus == -2) {
				query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, new String[] { "4", "5" }));
			} else if (qry.WebStatus == -1) {
				query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, new String[] { "4", "5" }));
				query.must(termsQuery("Prices.IsShowHome_" + qry.ProvinceId, "1"));
			}
		}
		if (qry.WebStatus == 0)// trang search
		{
			query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, new String[] { "4", "5" }));

			query.mustNot(termQuery("CategoryID", 2102));
		}

//		if (qry.WebStatus == 0)
//			filter.filter(boolQuery().mustNot(termQuery("CategoryID", 2102)));
		query.must(termQuery("ProductType", 1));
		query.must(termQuery("IsRepresentProduct", 0));
		// ẩn sản phẩm tham khảo khỏi trang kq search
		// yêu cầu vonhatnam 3/7/2019
		query.must(termQuery("IsReferAccessory", 0));
		query.must(boolQuery().mustNot(termsQuery("CategoryID", new String[] { "1784", "1783" })));
		query.must(termQuery("HasBimage", 1));
		// query.must(rangeQuery("Prices.Price_" + qry.ProvinceId).lte(8000000)); yeu
		// cau bo 11/3/2020

		var propStr = new ArrayList<String>();
		if (qry.PropSearch != null && qry.PropSearch.length > 0) {
			for (var item : qry.PropSearch) {
				switch (item.Operator) {
				case EQUAL: // yes no
					query.must(termQuery("PropVal.prop_" + item.PropertyId, item.CompareValue));
					break;
				case GREATER: // dùng để filter manu nhưng đã thay thế
					// query.must(rangeQuery("PropVal.prop_" +
					// item.PropertyId).gte(item.CompareValue));
					break;
				case SMALLER: // propStr
					propStr.add("prop" + item.PropertyId + "_" + item.CompareValue);
//					query.must(rangeQuery("PropVal.prop_" + item.PropertyId).lte(item.CompareValue));
					break;
				}
			}
		}

		if (propStr.size() > 0)
			query.must(termsQuery("PropStr", propStr.toArray()));

		if (qry.PriceFrom == 0 && qry.PriceTo > 0) {
			query.must(rangeQuery("Prices.Price_" + qry.ProvinceId).gte(qry.PriceFrom + 1));
			query.must(rangeQuery("Prices.Price_" + qry.ProvinceId).lte(qry.PriceTo));
		} else if (qry.PriceTo >= qry.PriceFrom && qry.PriceFrom > 0) {
			query.must(rangeQuery("Prices.Price_" + qry.ProvinceId).gte(qry.PriceFrom));
			query.must(rangeQuery("Prices.Price_" + qry.ProvinceId).lte(qry.PriceTo));
		} else if (qry.PriceFrom > 0 && qry.PriceTo == 0)
			query.must(rangeQuery("Prices.Price_" + qry.ProvinceId).gte(qry.PriceFrom));

		// ko co gia tri nay, da loc ben tren
		// query.should(
		// functionScoreQuery(termQuery( "IsReferAccessory",1), scriptFunction("
		// -10000000 ")));

		// uu tien co gia

		// ko can vi chi co 42,60 nam noi khac
		// query.should(functionScoreQuery(termsQuery("CategoryID",
		// MainProductCategory), scriptFunction(" 100000 ")));

		SearchResponse queryResults = null;
		SearchRequest searchRequest = new SearchRequest(config.ELASTICSEARCH_PRODUCT_INDEX);// .types(CurrentTypeDB);
		String rsl = "";
		try {
			String dstatus = "doc['Prices.WebStatusId_" + qry.ProvinceId + "']";
			String keyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(qry.Keyword));

			if (isGetFacetCate) {

				sb.aggregation(terms("FacetTermCategoryID").field("CategoryID").size(100)
						.order(BucketOrder.aggregation("hasproductinstock", false))
						.subAggregation(count("Sum").field("ProductID"))
						.subAggregation(max("hasproductinstock")
								.script(scriptFunction(dstatus + ".size() > 0 && " + dstatus + ".value == 4 ? 1 : 0")
										.getScript()))
						.subAggregation(max("maincatescore").script(scriptFunction(
								"doc['CategoryID'].value == 42 ? 150 : doc['CategoryID'].value == 44 ? 125 : doc['CategoryID'].value == 522 ? 100 : doc['CategoryID'].value == 7077 ? 80 : doc['CategoryID'].value == 7264 ? 60 : 40")
										.getScript())));
			}
			if (isGetFacetManu) {

				sb.aggregation(terms("FacetTermManufactureID").field("ManufactureID").size(100)
						.subAggregation(count("Sum").field("ProductID")));
			}
			if (isGetFacetProp) {
				sb.aggregation(addAggregation());

			}

			sb.from(qry.PageIndex * qry.PageSize).size(qry.PageSize);

			if (!Utils.StringIsEmpty(keyword)) {
				String[] keywords = keyword.split("\\s+");
				if (keywords.length == 1)
					rsl = "(*" + keyword + "* OR (keyword:" + keyword + ") OR (keyword:\"" + keyword + "\")) ";
				else {
					if (qry.IsSearchLike) {
						rsl = "(*" + keyword.replace(" ", "") + "* OR (keyword:\"" + keyword + "\")  OR (keyword:\""
								+ keyword + "*\") OR (" + String.join(" AND ", keywords) + "*) OR (keyword:"
								+ String.join(" AND ", keywords) + ")  ) ";
					} else {
						rsl = "(*" + keyword.replace(" ", "") + "* OR    (keyword:\"" + keyword + "\")   OR (keyword:\""
								+ keyword + "*\") OR (" + String.join(" AND ", keywords) + "*) OR (keyword:"
								+ String.join(" AND ", keywords) + ") OR (" + keyword + ")  ) ";
					}
				}

				if (qry.CategoryId == -3) { // phu kien
					if (qry.OrderByPrice == OrderType.NORMAL) {
						var scripts = new FilterFunctionBuilder[] {
								new FilterFunctionBuilder(scriptFunction("_score+ 1000000 * doc['Order'].value    ")),
								new FilterFunctionBuilder(
										scriptFunction(" 1000*_score + doc['ViewCountLast7Days'].value  ")) };
						query.must(functionScoreQuery(queryStringQuery(rsl).field("Keyword"), scripts)
								.scoreMode(ScoreMode.SUM).boostMode(CombineFunction.SUM));

					} else {

						query.must(boostingQuery(queryStringQuery(rsl).field("Keyword").boost(5f), null)
								.negativeBoost(0.2f));

//						if (qry.OrderByPrice == OrderType.NORMAL) // should not happen??
//							sb.sort("_score", SortOrder.DESC);
//						else
						sb.sort(scriptSort(
								new Script("doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 4 ? 1 : 0"),
								ScriptSortType.NUMBER).order(SortOrder.DESC))
								.sort("Prices.Price" + (isAccessory ? "402" : "") + "_" + qry.ProvinceId,
										qry.OrderByPrice == OrderType.ASCENDING ? SortOrder.ASC : SortOrder.DESC);
					}
				} else if (qry.CategoryId == -5) {
					query.must(
							boostingQuery(queryStringQuery(rsl).field("Keyword").boost(5f), null).negativeBoost(0.2f));
				} else {
					var scripts = new FilterFunctionBuilder[] {
							new FilterFunctionBuilder(termsQuery("CategoryID", ProductHelper.MainProductCategory),
									scriptFunction("  1000000 ")),

							// new FilterFunctionBuilder(termQuery("IsReferAccessory", "1"),
							// scriptFunction(" -10000000 ")),
							new FilterFunctionBuilder(scriptFunction("_score+ 10000000 * doc['Order'].value   ")),
							new FilterFunctionBuilder(
									scriptFunction(" 1000*_score + doc['ViewCountLast7Days'].value ")) };

					query.must(functionScoreQuery(queryStringQuery(rsl).field("Keyword"), scripts)
							.scoreMode(ScoreMode.SUM).boostMode(CombineFunction.SUM));
					if (qry.OrderByPrice != OrderType.NORMAL) {
						sb.sort(scriptSort(
								new Script("doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 4 ? 1 : 0"),
								ScriptSortType.NUMBER).order(SortOrder.DESC))
								.sort("Prices.Price" + (isAccessory ? "402" : "") + "_" + qry.ProvinceId,
										qry.OrderByPrice == OrderType.ASCENDING ? SortOrder.ASC : SortOrder.DESC);
					} else {
						sb.sort(new ScoreSortBuilder().order(SortOrder.DESC));
						// sb.sort("_score", SortOrder.DESC);
					}
				}
			} else { // else keyword
				if (qry.OrderByPrice != OrderType.NORMAL) {
					if (qry.OrderByPrice == OrderType.PROMOTION_PERCENT) {
						var script = "if(doc[\"Prices.Price_3\"].size() == 0 || doc[\"Prices.Price_" + qry.ProvinceId
								+ "\"].value <=0)\r\n" + "					return 0;\r\n"
								+ "                  long now = System.currentTimeMillis();\r\n"
								+ "                  int total = 0;\r\n" + "				  double percent = 0;\r\n"
								+ "                  for(int i = 0; i< doc[\"PromotionList.promotiontype\"].size(); i++) {\r\n"
								+ "					if(doc[\"PromotionList.productcode\"].get(i).equals(doc[\"Prices.ProductCode_"
								+ qry.ProvinceId
								+ "\"].value) && now > doc[\"PromotionList.begindate\"].get(i) && now < doc[\"PromotionList.enddate\"].get(i)) {\r\n"
								+ "						if(!doc[\"PromotionList.ispercentdiscount\"].get(i))\r\n"
								+ "							total += doc[\"PromotionList.discountvalue\"].get(i);\r\n"
								+ "						else\r\n"
								+ "							percent += doc[\"PromotionList.discountvalue\"].get(i)/100;\r\n"
								+ "					}\r\n" + "                  }\r\n"
								+ "				  percent += total/doc[\"Prices.Price_" + qry.ProvinceId
								+ "\"].value;\r\n" + "                  return percent;";
						sb.sort(scriptSort(new Script(script), ScriptSortType.NUMBER).order(SortOrder.DESC));
					} else if (qry.OrderByPrice == OrderType.DISCOUNT_VALUE) {
						var script = "if(doc[\"Prices.Price_" + qry.ProvinceId
								+ "\"].size() == 0 || doc[\"Prices.Price_" + qry.ProvinceId
								+ "\"].value <=0 || doc[\"Prices.PriceOrg_" + qry.ProvinceId
								+ "\"].size() == 0 || doc[\"Prices.PriceOrg_" + qry.ProvinceId + "\"].value <=0)\n"
								+ "					return 0;\n" + "                  return doc[\"Prices.PriceOrg_"
								+ qry.ProvinceId + "\"].value - doc[\"Prices.Price_" + qry.ProvinceId + "\"].value;";
						sb.sort(scriptSort(new Script(script), ScriptSortType.NUMBER).order(SortOrder.DESC));
					} else if (qry.OrderByPrice == OrderType.DESCENDING) {
						sb.sort("Prices.Price_3", SortOrder.DESC);

					} else if (qry.OrderByPrice == OrderType.ASCENDING) {
						sb.sort("Prices.Price_3", SortOrder.ASC);
					} else {
						sb.sort("_score", SortOrder.DESC);

					}
				} else {
					var scripts = new FilterFunctionBuilder[] {

							new FilterFunctionBuilder(scriptFunction(
									"_score+ 1000000 * doc['Order'].value  + doc['ProductID'].value   ")) };
					// new FilterFunctionBuilder(scriptFunction(
					// "_score+ 1000000 ")) };

					query.must(functionScoreQuery(matchAllQuery(), scripts).scoreMode(ScoreMode.SUM)
							.boostMode(CombineFunction.SUM));

//					sb.sort("Order", SortOrder.DESC)
//					.sort(new ScoreSortBuilder().order(SortOrder.DESC))
//					.sort("ViewCountLast7Days", SortOrder.DESC)
//					.sort("DateCreated", SortOrder.DESC);
					sb.sort(new ScoreSortBuilder().order(SortOrder.DESC));
				}

			}
			searchRequest.source(sb.query(query));

			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

//			var idlist = new ArrayList<Integer>();
//			var solist = new ArrayList<ProductSO>();
			var somap = new LinkedHashMap<Integer, ProductSO>();

			queryResults.getHits().forEach(h -> {
				try {
					// String aaa = new BigDecimal((Float) h.getScore()).toPlainString();
					// System.out.println(aaa);
					var so = mapper.readValue(h.getSourceAsString(), ProductSO.class);
					somap.put(so.ProductID, so);
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

			List<FaceManuSR> manulist = new ArrayList<FaceManuSR>();
			if (isGetFacetManu) {
				ParsedLongTerms manubucket = (ParsedLongTerms) aggrs.get("FacetTermManufactureID");
				manubucket.getBuckets().forEach(b -> manulist.add(new FaceManuSR() {
					{
						manufacturerID = b.getKeyAsNumber().intValue();
						productCount = (int) b.getDocCount();
					}
				}));
			}
			List<FacePropSR> Proplist = new ArrayList<FacePropSR>();
			if (isGetFacetProp) {

				Nested agg = queryResults.getAggregations().get("FacetPropIdList");
				Terms name = agg.getAggregations().get("name");
				for (Terms.Bucket bucket : name.getBuckets()) {
					// ReverseNested resellerToProduct =
					// bucket.getAggregations().get("reseller_to_product");
					// resellerToProduct.getDocCount(); // Doc count
					// 100000+ row.PropertyID) + "" + row.PropValueID
					// 10776048748 →
					FacePropSR a = new FacePropSR();
					String aaa = new BigDecimal((Double) bucket.getKey()).toPlainString();
					a.propID = Integer.parseInt(aaa.substring(0, 6)) - 100000;
					a.propValueID = Integer.parseInt(aaa.substring(6));
					a.count = (int) bucket.getDocCount();
					Proplist.add(a);

				}
			}
			List<FaceCategorySR> catelist = new ArrayList<FaceCategorySR>();
			if (isGetFacetCate) {
				ParsedLongTerms catebucket = (ParsedLongTerms) aggrs.get("FacetTermCategoryID");
				catebucket.getBuckets().forEach(b -> {
					var catescore = ((Max) b.getAggregations().get("maincatescore")).getValue();
					var hasproductinstock = ((Max) b.getAggregations().get("hasproductinstock")).getValue();
					catelist.add(new FaceCategorySR() {
						{
							categoryID = b.getKeyAsNumber().intValue();
							productCount = (int) b.getDocCount();
							score = (int) catescore;
							hasProductInStock = hasproductinstock == 1;
						}
					});
				});

				catelist.sort(Comparator.comparingInt(FaceCategorySR::getScore).reversed());
			}
			result = new ProductSOSR() {
				{
					faceListCategory = catelist.stream().toArray(FaceCategorySR[]::new);
					faceListManu = manulist.stream().toArray(FaceManuSR[]::new);
					faceListProp = Proplist.stream().toArray(FacePropSR[]::new);
					rowCount = rowcount;
					productList = somap;
				}
			};
		} catch (Exception e) {
			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.message = "Failed: " + e.toString() + ": " + e.getMessage() + " - " + trace;
			Logs.LogException(e);
		}
		return result;
	}

	@Override
	public void processDetail(ProductBO product, int siteID, int provinceID, Integer storeID, String lang,
			CodeTimers timer) throws Throwable {
		int productID = product.ProductID;

		var details = productHelper.getCachedProductDetail(productID, siteID, lang, true, 0, timer);

		// capacity
		try {
			product.Capacity = Stream.of(details.list).filter(x -> x.isAddUp).map(x -> x.Value).findFirst().orElse("");
		} catch (NullPointerException e) {
			product.Capacity = "";
		}
	}

	@Override
	public void processSimpleDetails(ProductBO[] products, int provinceID, int siteID, String lang) throws Throwable {

		if (products == null || products.length == 0)
			return;

		// capacities
		var ids = Stream.of(products).mapToLong(x -> x.ProductID).toArray();
		var capacities = productHelper.getCapacities(ids, siteID, lang);
		for (var product : products)
			product.Capacity = capacities.get((long) product.ProductID);
	}

	@Override
	public int[] GetHomePageProduct2020(int category, int pageSize, int provinceID) {
		// TODO Auto-generated method stub
		return null;
	}
}
