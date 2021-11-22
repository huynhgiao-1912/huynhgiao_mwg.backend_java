package mwg.wb.business.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.SpecialsaleProgram.Pm_ProductBO;
import mwg.wb.client.elasticsearch.dataquery.OrderType;
import mwg.wb.client.elasticsearch.dataquery.ProductQuery;
import mwg.wb.client.elasticsearch.dataquery.ProductQuery.SearchType;
import mwg.wb.client.service.CodeTimers;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.PriceParamsBO;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.promotion.Promotion;
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

public class BigPhoneProductHelper extends ISiteProductHelper {

	private ProductHelper productHelper;
	private ObjectMapper esmapper = DidxHelper.generateJsonMapper(GConfig.DateFormatStringNews);

	public BigPhoneProductHelper(ClientConfig aconfig, RestHighLevelClient _clientIndex, ObjectMapper _mapper,
			ProductHelper productHelper) {
		super(aconfig, _clientIndex, _mapper, productHelper);
		this.productHelper = productHelper;
	}

	@Override
	public ProductSOSR SearchProduct(ProductQuery qry, boolean isGetFacetManu, boolean isGetFacetCate,
									 boolean isGetFacetProp, CodeTimers timer) {
		ProductSOSR result = new ProductSOSR();
		SearchSourceBuilder sb = new SearchSourceBuilder();
		sb.fetchSource(new String[] { "_score", "ProductID", "Prices.WebStatusId_" + qry.ProvinceId,
				"Prices.IsShowHome_" + qry.ProvinceId, "Prices.Price_" + qry.ProvinceId,
				"Prices.ProductCode_" + qry.ProvinceId, "StickerLabel", "PaymentFromDate", "PaymentToDate", "IsPayment",
				"PercentInstallment" }, null);

//		String strCateid = qry.CategoryId + "";
//		boolean isAccessory = Arrays.stream(ProductHelper.AccessoryCategory).anyMatch(x -> x == qry.CategoryId);

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
//			else if (qry.CategoryId == 382) // loa, loa kéo
//				query.must(termsQuery("CategoryID", "382", "2162"));
			else {
				query.must(boolQuery().should(termQuery("CategoryID", qry.CategoryId))
						.should(termQuery("CategoryMap", "c" + qry.CategoryId + "c")));
			}
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
//
//		if (qry.CategoryId == -1) {
//			// bo nhung thang phu kien co gia 402=0
//			query.mustNot(boolQuery().must(termQuery("Prices.Price402_" + qry.ProvinceId, 0))
//					.must(termsQuery("CategoryID", (Object[]) ProductHelper.AccessoryCategory)));
//		}
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

//			query.must(rangeQuery("Prices.Price402_" + qry.ProvinceId).gte(1));

		} else if (qry.SearchType != null) {
			if (qry.SearchType == SearchType.SEARCH)
				query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, "0", "1", "2", "3", "4", "5", "6"));
			else if (qry.SearchType == SearchType.CATE)
				query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, "2", "4", "6"));
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
		if (qry.PriceRanges != null && qry.PriceRanges.length > 0) {
			var priceQ = boolQuery();
			for (var pr : qry.PriceRanges) {
				if (pr.PriceTo <= 0) {
					pr.PriceTo = Integer.MAX_VALUE;
				}
				priceQ.should(rangeQuery("Prices.Price_" + qry.ProvinceId).gte(pr.PriceFrom).lte(pr.PriceTo));
			}
			query.must(priceQ);
		} else {
			if (qry.PriceFrom == 0 && qry.PriceTo > 0) {
				query.must(rangeQuery("Prices.Price_" + qry.ProvinceId).gte(qry.PriceFrom + 1));
				query.must(rangeQuery("Prices.Price_" + qry.ProvinceId).lte(qry.PriceTo));
			} else if (qry.PriceTo >= qry.PriceFrom && qry.PriceFrom > 0) {
				query.must(rangeQuery("Prices.Price_" + qry.ProvinceId).gte(qry.PriceFrom));
				query.must(rangeQuery("Prices.Price_" + qry.ProvinceId).lte(qry.PriceTo));
			} else if (qry.PriceFrom > 0 && qry.PriceTo <= 0)
				query.must(rangeQuery("Prices.Price_" + qry.ProvinceId).gte(qry.PriceFrom));
		}

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
			String keyword = DidxHelper.FormatKeywordSearchFieldCam(DidxHelper.FilterVietkey(qry.Keyword));

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
						sb.sort(scriptSort(new Script("doc['Prices.WebStatusId_" + qry.ProvinceId
								+ "'].value == 4 || doc['Prices.WebStatusId_" + qry.ProvinceId
								+ "'].value == 6 ? 1 : 0"), ScriptSortType.NUMBER).order(SortOrder.DESC))
								.sort("Prices.Price_" + qry.ProvinceId,
										qry.OrderByPrice == OrderType.ASCENDING ? SortOrder.ASC : SortOrder.DESC);
					}
				} else if (qry.CategoryId == -5) {
					query.must(
							boostingQuery(queryStringQuery(rsl).field("Keyword").boost(5f), null).negativeBoost(0.2f));
				} else {
					var scripts = new FilterFunctionBuilder[] {
//							new FilterFunctionBuilder(termQuery("ReferAccessory", "1"), scriptFunction(" -10000000 ")),
							new FilterFunctionBuilder(scriptFunction("_score+ 1000000 * doc['Order'].value   ")),
							new FilterFunctionBuilder(
									scriptFunction(" 1000*_score + doc['ViewCountLast7Days'].value ")) };
					query.must(functionScoreQuery(queryStringQuery(rsl).field("Keyword"), scripts)
							.scoreMode(ScoreMode.SUM).boostMode(CombineFunction.SUM));
					if (qry.OrderByPrice != OrderType.NORMAL) {
						sb.sort(scriptSort(new Script("doc['Prices.WebStatusId_" + qry.ProvinceId
								+ "'].value == 4 || doc['Prices.WebStatusId_" + qry.ProvinceId
								+ "'].value == 6 ? 1 : 0"), ScriptSortType.NUMBER).order(SortOrder.DESC))
								.sort("Prices.Price_" + qry.ProvinceId,
										qry.OrderByPrice == OrderType.ASCENDING ? SortOrder.ASC : SortOrder.DESC);
					} else {
						// đưa ngành hàng chính & webstatus 4 lên đầu, tránh trường hợp phụ kiện nằm đầu
						// kết quả tìm kiếm
						// 20200812: đưa sp preorder lên đầu
						sb.sort(scriptSort(new Script("!" + dstatus + ".empty ? (" + dstatus + ".value == 6 ? 2 : "
								+ dstatus + ".value == 6 ? 1 : 0) : 0"), ScriptSortType.NUMBER).order(SortOrder.DESC))
								.sort(scriptSort(new Script(
										"doc['CategoryID'].value == 42 || doc['CategoryID'].value == 44 || doc['CategoryID'].value == 522 || doc['CategoryID'].value == 7077 ? 1 : 0"),
										ScriptSortType.NUMBER).order(SortOrder.DESC));

						sb.sort(new ScoreSortBuilder().order(SortOrder.DESC));
						// sb.sort("_score", SortOrder.DESC);
					}
				}
			} else { // else keyword
				if (qry.OrderByPrice != OrderType.NORMAL) {
					if (qry.OrderByPrice == OrderType.PROMOTION_PERCENT) {
						var script = "				  int prov = 163;\n"
								+ "				  if(doc[\"Prices.Price_\" + prov].size() == 0 || doc[\"Prices.Price_\" + prov].value <=0)\n"
								+ "					return 0;\n"
								+ "                  long now = System.currentTimeMillis();\n"
								+ "                  int total = 0;\n" + "				  double percent = 0;\n"
								+ "                  for(int i = 0; i < doc[\"PromotionList.discountvalue\"].size(); i++) {\n"
								+ "					if(now > doc[\"PromotionList.begindate\"][i] && now < doc[\"PromotionList.enddate\"][i]) {\n"
								+ "						if(!doc[\"PromotionList.ispercentdiscount\"][i])\n"
								+ "							total += doc[\"PromotionList.discountvalue\"][i];\n"
								+ "						else\n"
								+ "							percent += doc[\"PromotionList.discountvalue\"][i]/100;\n"
								+ "					}\n" + "                  }\n"
								+ "				  percent += (double) total/doc[\"Prices.Price_\" + prov].value;\n"
								+ "				  return percent;";
						sb.sort(scriptSort(new Script(script), ScriptSortType.NUMBER).order(SortOrder.DESC));
					} else if (qry.OrderByPrice == OrderType.PROMOTION_VALUE) {

					} else if (qry.OrderByPrice == OrderType.DESCENDING) {
						sb.sort("Prices.Price_" + qry.ProvinceId, SortOrder.DESC);

					} else if (qry.OrderByPrice == OrderType.ASCENDING) {
						sb.sort("Prices.Price_" + qry.ProvinceId, SortOrder.ASC);
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
			if (timer == null) {
				timer = new CodeTimers();
			}
			timer.start("es-query");
			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
			timer.pause("es-query");
//			var idlist = new ArrayList<Integer>();
//			var solist = new ArrayList<ProductSO>();
			var somap = new LinkedHashMap<Integer, ProductSO>();
			timer.start("es-parse");
			queryResults.getHits().forEach(h -> {
				try {
					// String aaa = new BigDecimal((Float) h.getScore()).toPlainString();
					// System.out.println(aaa);
					var so = esmapper.readValue(h.getSourceAsString(), ProductSO.class);
					somap.put(so.ProductID, so);
				} catch (Exception e) {
//					e.printStackTrace();
					Logs.LogException(e);
				}
			});
			timer.pause("es-parse");
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

		}
		return result;
	}

	@Override
	public void processPromotion(Promotion item, int productID, int siteid, String Lang, int InventoryStatusID,
			ErpHelper erpHelper, List<Promotion> listTempPromotion) throws Throwable {
		productHelper.getHelperBySite(1).processPromotion(item, productID, siteid, Lang, InventoryStatusID, erpHelper,
				listTempPromotion);
	}

	@Override
	public void processPromotion2(Promotion item, ProductBO productBO, int siteid, String Lang, int InventoryStatusID,
			ErpHelper erpHelper, List<Promotion> listTempPromotion) throws Throwable {
		productHelper.getHelperBySite(1).processPromotion2(item, productBO, siteid, Lang, InventoryStatusID, erpHelper,
				listTempPromotion);
	}

	@Override
	public void processDetail(ProductBO product, int siteID, int provinceID, Integer storeID, String lang,
			CodeTimers timer) throws Throwable {
//		int productID = product.ProductID;
		Pm_ProductBO codeInfo = null;
		if (!Utils.StringIsEmpty(product.ProductCode)) {
			codeInfo = productHelper.GetPm_ProductFromCache(product.ProductCode);
		}

		List<PriceParamsBO> lspriceparams = null;
		if (codeInfo != null) {
			lspriceparams = productHelper.GetPriceParams(codeInfo.maingroupid);
			if (lspriceparams != null) {
				var priceparams = lspriceparams.stream()
						.filter(x -> x != null && x.companyid == DidxHelper.getCompanyID(siteID)).findFirst()
						.orElse(null);

				if (priceparams != null) {
					product.Numbers = priceparams.psychologicaladjustlevel > 0 ? priceparams.psychologicaladjustlevel
							: priceparams.roundlevel;
				}
			}
		}
		// lay thong tin tra gop 0%

		var installment = productHelper.GetInstallmentByProduct(product.ProductID, siteID, lang, product.CategoryID,
				(int) product.ManufactureID);

		if (installment != null) {
			if (product.ProductLanguageBO != null) {
				product.ProductLanguageBO.IsPayment = installment.IsPayment;
				product.ProductLanguageBO.PaymentFromDate = installment.FromDate;
				product.ProductLanguageBO.PaymentToDate = installment.ToDate;
				product.ProductLanguageBO.PercentInstallment = installment.PaymentValue;
			}
			product.IsPayment = installment.IsPayment == 1;
			product.Paymentfromdate = installment.FromDate;
			product.Paymenttodate = installment.ToDate;
			product.PercentInstallment = installment.PaymentValue;
		}
	}

	@Override
	public void processSimpleDetails(ProductBO[] products, int provinceID, int siteID, String languageID)
			throws Throwable {
		// lay km subbrand
		int provinceIDpromo = -1;
		int outputTypeID = 0;
		double salePrice = 0;
		int inventoryStatusID = 1;
		String[] recordid = Stream.of(products)
				.filter(x -> x.getSubBrandPromotion && x.pmProductBOList != null && x.pmProductBOList.length > 0)
				.map(x -> {
					var code = x.pmProductBOList[0];
					x.promoGroupRecordID = Stream.of(code.subGroupID, code.brandID, provinceIDpromo, outputTypeID,
							salePrice, inventoryStatusID, siteID).map(y -> y.toString())
							.collect(Collectors.joining("_"));
					return x.promoGroupRecordID;
				}).toArray(String[]::new);

		var promoGroup = productHelper.getPromotionGroup(recordid);

		for (var product : products) {
			var promoG = product.getSubBrandPromotion && product.promoGroupRecordID != null
					? promoGroup.get(product.promoGroupRecordID)
					: null;
			productHelper.GetDefaultPriceAndPromotion(siteID, languageID, product, false, true, promoG);
			// lam tron gia
			List<PriceParamsBO> lspriceparams = null;
			if (product.MainGroupID > 0) {
				lspriceparams = productHelper.GetPriceParams(product.MainGroupID);
				if (lspriceparams != null) {
					var priceparams = lspriceparams.stream()
							.filter(x -> x != null && x.companyid == DidxHelper.getCompanyID(siteID)).findFirst()
							.orElse(null);

					if (priceparams != null) {
						product.Numbers = priceparams.psychologicaladjustlevel > 0
								? priceparams.psychologicaladjustlevel
								: priceparams.roundlevel;
					}
				}
			}
		}
	}

	@Override
	public int[] GetHomePageProduct2020(int category, int pageSize, int provinceID) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public void getPromotions(ProductBO product, ProductErpPriceBO def, int siteID) {
//		if (product.Promotion == null || product.Promotion.isEmpty())
//			return;
//		product.Promotion = product.Promotion.stream().filter(x -> x.BrandID == 11)
//				.collect(Collectors.toMap(x -> x.recordid, x -> x)).values().stream().collect(Collectors.toList());
//	}
}
