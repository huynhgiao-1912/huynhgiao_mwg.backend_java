package mwg.wb.business.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import mwg.wb.business.CacheStaticHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.client.elasticsearch.dataquery.ProductPropertySearching;
import mwg.wb.client.elasticsearch.dataquery.ProductQuery;
import mwg.wb.client.elasticsearch.dataquery.SearchFlag;
import mwg.wb.client.elasticsearch.dataquery.SearchOrder;
import mwg.wb.client.service.CodeTimers;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.commonpackage.SuggestSearchSO;
import mwg.wb.model.commonpackage.SuggestSearchTypes;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductDetailBO;
import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.model.products.PromotionListGroupErp;
import mwg.wb.model.promotion.Promotion;
import mwg.wb.model.search.ProductSO;
import mwg.wb.model.searchresult.FaceCategorySR;
import mwg.wb.model.searchresult.FaceManuSR;
import mwg.wb.model.searchresult.FacePropSR;
import mwg.wb.model.searchresult.ProductSOSR;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.TermVectorsRequest;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder.FilterFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.Min;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.scriptFunction;
import static org.elasticsearch.search.aggregations.AggregationBuilders.*;

public class DMXProductHelper extends ISiteProductHelper {

	// private final List<Integer> ApplianceCategories = Arrays.asList(4645, 462,
	// 1922, 1982, 1983, 1984, 1985, 1986, 1987,
//			1988, 1989, 1990, 1991, 1992, 2062, 2063, 2064, 2084, 2222, 2262, 2302, 2322, 2342, 2142, 3305, 5473, 2428,
//			3385, 5105, 7367, 5554, 5475, 7498, 7419, 7278, 7458, 7684);
	private final int[] AccessoryCategory = new int[] { 482, 60, 57, 55, 58, 54, 1662, 1363, 1823, 56, 75, 86, 382, 346,
			2429, 2823, 2824, 2825, 3885, 1622, 5505, 5005, 5025, 4547, 5452, 85, 6858, 6599, 7186 };
	// private final int[] AccsoryCategoryForHomePage = new int[] { 60, 86, 57, 54,
	// 58, 382, 75, 3885, 1662, 4547, 2429,
//			1363, 6858, 56, 55, 7923, 4728, 4727, 1902, 7922, 6858, 6862, 7924, 7925, 6863 };
//	private final int[] SellingProductStatusID = new int[] { 2, 3, 4, 6, 8, 9, 11, 99, 98 };
	private final int[] Appliance2142Categories = new int[] { 4928, 4931, 5205, 4930, 5228, 4927, 5225, 5292, 5227,
			5226, 5230, 5231, 2403, 2402, 5229, 4929, 4932, 5478, 5395, 5354, 6790, 6819, 6790, 3187, 3729, 7075, 346,
			6599, 7305, 6012, 7479, 2528, 7480, 3736, 4946, 7481, 7482, 6553 };

	private final int[] ListCateDungCu = new int[] { 7739, 4697, 7720, 4706, 8119, 8121, 8858, 8860, 8861, 8862, 8980,
			8984, 8979, 9138 };
	private final int[] NewApplianceCategories = new int[] { 4645, 462, 1922, 1982, 1983, 1984, 1985, 1986, 1987, 1988,
			1989, 1990, 1991, 1992, 2062, 2063, 2064, 2084, 2222, 2262, 2302, 2322, 2342, 2142, 3305, 5473, 2428, 3385,
			5105, 7367, 5554, 7498, 7419, 7278, 7458, 7683, 7684, 7685, 7075, 346, 4366, 4706, 366, 365, 8765, 8620,
			7902, 8621, 9008, 324, 9003, 8967, 9000, 7858 };

	private ObjectMapper esmapper = DidxHelper.generateJsonMapper(GConfig.DateFormatStringNews);
	private List<Integer> PromotionTypeAtStore = Stream.of(18, 19).collect(Collectors.toList());

	private final int[] AccsoryCategoryForHomePage = new int[] { 60, 86, 57, 54, 58, 382, 75, 3885, 1662, 4547, 2429,
			6858, 56, 55, 7923, 4728, 4727, 1902, 7922, 6858, 6862, 7924, 7925, 6863 }; // 1363,
	private String[] MainCategory = { "42", "1944", "2002", "1942", "1943", "44", "522",  };
	private String[] SubCategory = { "54", "55", "56", "60", "1662", "6599", "2102", "2429", "7924", "1363", "7925",
			"382", "7691", "5105", "1882", "9058", "7498", "2162",
			/* sbcate chỉ phục vụ search */
			"58", "7978", /* day dong ho */
			"6862" };
	public int[] MainProductCategoryForNewPage = { 42, 44, 522 };

	public DMXProductHelper(ClientConfig aconfig, RestHighLevelClient _clientIndex, ObjectMapper _mapper,
			ProductHelper productHelper) {
		super(aconfig, _clientIndex, _mapper, productHelper);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processPromotion(Promotion item, int productID, int siteid, String Lang, int InventoryStatusID,
			ErpHelper erpHelper, List<Promotion> listTempPromotion) throws Throwable {// xu ly
		// nghiep
		// vu km
		// cho
		// site
		// 2
		// //thanhphi0401

		var productBO = productHelper.GetProductBOByProductIDSE(productID, siteid, Lang);
		var isTele = false;
		if (productBO != null && productBO.ProductCategoryBO != null) {
			int productCate = (int) productBO.ProductCategoryBO.CategoryID;
			isTele = productCate == 42 || productCate == 522 ? true : false;
		}
		var tmp = !Utils.StringIsEmpty(item.ReturnValue) ? item.ReturnValue.split("\\|") : null;
		double returnvalue = 0;
		if (tmp != null && tmp.length > 0) {
			for (var value : tmp) {
				returnvalue += Double.valueOf(value);
			}
		}
		item.ReturnValue2 = returnvalue;

		item.IsPercentDiscountDisplay = item.IsPercentDiscountNotWeb == 1 ? true : false;
		item.DiscountValueDisplay = item.IsGiftPrdDiscount == 1 ? item.DiscountValueNotWeb : 0;
		item.QuantityLists = item.QuantityList;
		item.QuantityList = "";
		item.ReturnValues = item.ReturnValue;
		item.FromValue = item.MINTOTALPROMOTIONOFFERMONEY;
		item.ToValue = item.MAXTOTALPROMOTIONOFFERMONEY;
		item.ProductCodes = item.ProductId;
		item.NotApplyForInstallment = item.IsNotApplyForInterestRate;

		item.ExcludeInstallmentProgramID = item.SALEPROGRAMIDLIST;
		item.ApplyStartTime = item.StartTime;
		item.ApplyEndTime = item.EndTime;
		item.ProductApplyType = item.LoaiChon == null ? -1 : item.LoaiChon;
		item.LoaiChon = item.LoaiChon == null ? 0 : item.LoaiChon;
		item.SpecialOutputTypeCount = item.SPECIALOUTPUTTYECOUNT;

		var objtmp = listTempPromotion.stream().filter(p -> p.PromotionID == item.PromotionID).findFirst().orElse(null);

//        if (item.GroupID.equals("BanKem") && objtmp == null && InventoryStatusID == 1 && productID > 0) {
//
//            var dataDetail = erpHelper.GetPromotionListGroupByPromoID(item.PromotionID, 2);
//
//            if (dataDetail != null && dataDetail.length > 0) {
//
//                List<Integer> listIDAcc = new ArrayList<Integer>();
//
//                if (isTele) {
//
//                    // lay ds pk ban kem
//                    listIDAcc = productHelper.GetListProductBanKem(productBO.ProductID);
//
//                }
//                if (isTele && listIDAcc.size() <= 0) {// khong xu ly
//
//                } else {
//                    String productidref = "";
//                    String percent = "";
//                    String promotionPrice = "";
//                    String quantities = "";
//
//                    for (var sp : dataDetail) {
//
//                        if (!listIDAcc.contains(sp.ProductIDRef) && isTele)
//                            continue;
//
//                        productidref += "|" + sp.ProductIDRef;
//                        percent += "|" + sp.IsPercentDiscount;
//                        promotionPrice += "|" + sp.PromotionPrice;
//                        quantities += "|" + sp.Quantity;
//                        int categoryID;
//                        if (item.CategoryID <= 0 && (categoryID = productHelper.getCategoryID(sp.ProductIDRef)) > 0) {
//                            item.CategoryID = categoryID;
//                        }
//                    }
//                    item.ProductIds = StringUtils.strip(productidref, "|");
//                    item.ISPERCENTDISCOUNTSALELIST = StringUtils.strip(percent, "|");
//                    item.PROMOTIONPRICELIST = StringUtils.strip(promotionPrice, "|");
//                    item.Quantities = StringUtils.strip(quantities, "|");
//
//                }
//
//            }
//
//        }
//
//        if (item.GroupID.equals("BanKem") && objtmp != null && InventoryStatusID == 1) {
//            item.ProductIds = objtmp.ProductIds;
//            item.ISPERCENTDISCOUNTSALELIST = objtmp.ISPERCENTDISCOUNTSALELIST;
//            item.PROMOTIONPRICELIST = objtmp.PROMOTIONPRICELIST;
//            item.Quantities = objtmp.Quantities;
//        }

	}

	public PromotionListGroupErp[] GetPromotionListGroupByPromoID(ErpHelper erpHelper, Integer PromotionID)
			throws Throwable {

		String key = "GetPromotionListGroupByPromoID" + PromotionID;
		var rs = (PromotionListGroupErp[]) CacheStaticHelper.GetFromCache(key, 30);
		if (rs == null) {
			rs = erpHelper.GetPromotionListGroupByPromoID(PromotionID, 2);
			CacheStaticHelper.AddToCache(key, rs);

		}
//		var rsa = (PromotionListGroupErp[])
		CacheStaticHelper.GetFromCache(key, 30);

		return rs;

	}

	@Override
	public void processPromotion2(Promotion item, ProductBO productBO, int siteid, String Lang, int InventoryStatusID,
			ErpHelper erpHelper, List<Promotion> listTempPromotion) throws Throwable {// xu ly
		// nghiep
		// vu km
		// cho
		// site
		// 2
		// //thanhphi0401

		var isTele = false;
		if (productBO != null && productBO.ProductCategoryBO != null) {
			int productCate = (int) productBO.ProductCategoryBO.CategoryID;
			isTele = productCate == 42 || productCate == 522 ? true : false;
		}

		processPromotion(item);

		var objtmp = listTempPromotion.stream().filter(p -> p.PromotionID == item.PromotionID).findFirst().orElse(null);

//        if (!item.v2 && item.GroupID.equals("BanKem") && objtmp == null && InventoryStatusID == 1) {
//
//            var dataDetail = GetPromotionListGroupByPromoID(erpHelper, item.PromotionID);// erpHelper.GetPromotionListGroupByPromoID(item.PromotionID);
//
//            if (dataDetail != null && dataDetail.length > 0) {
//
//                List<Integer> listIDAcc = new ArrayList<Integer>();
//
//                if (isTele) {
//
//                    // lay ds pk ban kem
//                    listIDAcc = productHelper.GetListProductBanKem(productBO.ProductID);
//
//                }
//                if (isTele && listIDAcc.size() <= 0) {// khong xu ly
//
//                } else {
//                    String productidref = "";
//                    String percent = "";
//                    String promotionPrice = "";
//                    String quantities = "";
//
//                    for (var sp : dataDetail) {
//
//                        int categoryID;
//                        if (item.CategoryID <= 0 && (categoryID = productHelper.getCategoryID(sp.ProductIDRef)) > 0) {
//                            item.CategoryID = categoryID;
//                        }
//                        if (!listIDAcc.contains(sp.ProductIDRef) && isTele && item.CategoryID != 7264)
//                            continue;
//
//                        productidref += "|" + sp.ProductIDRef;
//                        percent += "|" + sp.IsPercentDiscount;
//                        promotionPrice += "|" + sp.PromotionPrice;
//                        quantities += "|" + sp.Quantity;
//                    }
//                    item.ProductIds = StringUtils.strip(productidref, "|");
//                    item.ISPERCENTDISCOUNTSALELIST = StringUtils.strip(percent, "|");
//                    item.PROMOTIONPRICELIST = StringUtils.strip(promotionPrice, "|");
//                    item.Quantities = StringUtils.strip(quantities, "|");
//
//                }
//
//            }
//
//        }
//
//        if (item.GroupID.equals("BanKem") && objtmp != null && InventoryStatusID == 1) {
//            item.ProductIds = objtmp.ProductIds;
//            item.ISPERCENTDISCOUNTSALELIST = objtmp.ISPERCENTDISCOUNTSALELIST;
//            item.PROMOTIONPRICELIST = objtmp.PROMOTIONPRICELIST;
//            item.Quantities = objtmp.Quantities;
//        }
//
	}

	@Override
	public void getPromotions(ProductBO product, ProductErpPriceBO def, int siteID) {
		productHelper.getHelperBySite(1).getPromotions(product, def, siteID);
	}

	@Override
	public AggregationBuilder addAggregation() {
		return AggregationBuilders.nested("FacetPropIdList", "listproperty")
				.subAggregation(AggregationBuilders.terms("name").field("listproperty.propkey").size(1000));
	}

	@Override
	public ProductSOSR SearchProduct(ProductQuery qry, boolean isGetFacetManu, boolean isGetFacetCate,
			boolean isGetFacetProp, CodeTimers timer) throws Throwable {

		// null checks
		if (qry.SiteId == 0) {
			return new ProductSOSR();
		}

		if (qry.SearchFlags == null) {
			qry.SearchFlags = new HashSet<>();
		}

		if (qry.LanguageID == null) {
			qry.LanguageID = DidxHelper.getLangBySiteID(qry.SiteId);
		}

		// start
		SimpleDateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		ProductSOSR result = new ProductSOSR();
		SearchSourceBuilder sb = new SearchSourceBuilder();
		sb.fetchSource(new String[] { "_score", "ProductID", "Prices.WebStatusId_" + qry.ProvinceId,
				"Prices.IsShowHome_" + qry.ProvinceId, "Prices.Price_" + qry.ProvinceId,
				"Prices.ProductCode_" + qry.ProvinceId, "StickerLabel", "PaymentFromDate", "PaymentToDate", "IsPayment",
				"PercentInstallment", "CategoryID", "PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId,
				"CmsProductStatus" }, null);

//		String strCateid = qry.CategoryId + "";
//		boolean isAccessory = ProductHelper.AccessoryCategory.contains(qry.CategoryId);
//				Arrays.stream(ProductHelper.AccessoryCategory).anyMatch(x -> x == qry.CategoryId);

		var query = boolQuery();

		var now = new Date();
		long nowepoch = System.currentTimeMillis();

		var cateids = qry.CategoryIdList;
		if (cateids != null && cateids.length > 0)
			query.must(termsQuery("CategoryID", cateids));

//		 loại bỏ trạng thái 2,5 dưới cms
		if (Utils.StringIsEmpty(qry.Keyword) && qry.CategoryId > 0 ) {
			query.mustNot(termQuery("CmsProductStatus", 5));
			// ict không loại 2
			query.mustNot(boolQuery().must(termQuery("CmsProductStatus", 2))
					.mustNot(termsQuery("CategoryID", new int[] { 42, 44, 522 })));
		}

		// cate loại trừ
		var excludeCate = new int[] { 2102 };
		query.mustNot(termsQuery("CategoryID", excludeCate));

		query.must(termQuery("IsDeleted", 0));

		if (qry.CategoryId > 0) {

			if (qry.CategoryId == 2142) {
				query.must(termsQuery("CategoryID", "4929", "4927", "4928", "4931", "4932", "4930"));
			}

			else if (qry.CategoryId == 2823) {
				query.must(termsQuery("CategoryID", "2824", "5005", "2825", "346", "5452", "6599", "7186"));
			}

			else if (qry.CategoryId == 5025) {
				query.must(termsQuery("CategoryID", "55", "75"));
			}

			else if (qry.CategoryId == 1622) {
				query.must(termsQuery("CategoryID", "1363", "1823"));
			}

			else if (qry.CategoryId == 2429) { // phụ kiện
				query.must(termsQuery("CategoryID", "2429", "1363", "1823"));
			} else if (qry.CategoryId > 0) {
				query.must(boolQuery()//
						.should(termQuery("CategoryID", qry.CategoryId))//
						.should(termQuery("CategoryMap", qry.CategoryId))//
						.should(termQuery("GroupId", qry.CategoryId))//
						.should(termQuery("ParentIdList", qry.CategoryId))//
				);
			}
//			Bên dotnet không có filter này
//			else if (qry.CategoryId == 7482) {
//				query.must(termsQuery("CategoryID", "7482", "6599", "2825", "8060"));
//			}

		} else if (qry.CategoryId == -3) { // phụ kiện
			query.must(termsQuery("CategoryID", AccessoryCategory));
		} else if (qry.CategoryId == -2) { // loại bỏ phụ kiện
			query.mustNot(termsQuery("CategoryID", AccessoryCategory));
		} else if (qry.CategoryId == -5) {
			// trang phu kien noi bat
			query.must(termQuery("CategoryID", AccsoryCategoryForHomePage));
			query.mustNot(termQuery("CategoryID", 2162));
		} else {
//            var validCates = productHelper.getValidCategoryIDsFromCache(qry.SiteId, qry.LanguageID);
//            if (validCates != null && validCates.length > 0) {
//                query.must(termsQuery("CategoryID", validCates));
//            }
		}
//      Nghia: Chuyen Sang enum ISSEARCHONLINEONLY
//		if (qry.IsSearchOnlineOnly) {
//			// lay sp online only
//			query.must(termQuery("IsOnlineOnly", true));
//		}
		if (qry.ListManufacturename != null && qry.ListManufacturename.length > 0) {
			var listManu = Arrays.stream(qry.ListManufacturename).map(x -> DidxHelper.GenTermKeyWord(x))
					.toArray(String[]::new);
//			for (String item : qry.ListManufacturename) {
//				var tmp = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordSearchField(item.trim()))
//                        .replace(" ", "_");
//				Utils.pushArray(listManu, tmp);
//			}
			query.must(termsQuery("ManufacturerName", listManu));
		}

//		if (qry.CategoryId <= 0) {
//			query.must(termsQuery("CategoryID",
//					new int[] { 48, 662, 43, 1825, 1322, 1782, 1824, 61, 1784, 1862, 41, 2122, 2042, 45, 1822, 6862,
//							1902, 2423, 67, 2084, 2362, 1802, 1783, 2082, 2242, 6864, 6859, 6866, 5474, 5493, 5025,
//							5472, 5492, 6863, 6861, 6860 }));
//		}

		query.must(boolQuery()//
				.should(boolQuery().mustNot(termsQuery("CategoryID", Appliance2142Categories)))// cate gia dụng
				.should(boolQuery()//
						.must(termsQuery("CategoryID", Appliance2142Categories))//
						.mustNot(termQuery("Prices.WebStatusId_" + qry.ProvinceId, 1))//
				)//
		);

//		query.must(boolQuery()//
//				.should(boolQuery().mustNot(termQuery("Appliance2142Categories", 1)))// cate gia dụng
//				.should(boolQuery()//
//						.must(termQuery("Appliance2142Categories", 1))//
//						.mustNot(termQuery("Prices.WebStatusId_" + qry.ProvinceId, 1))//
//				)//
//		);

//        query.must(boolQuery()//
//				.should(boolQuery().must(termsQuery("Appliance2142Categories", false)))// cate gia dụng
//				.should(boolQuery()//
//						.must(termsQuery("Appliance2142Categories", true))//
//						.mustNot(termQuery("Prices.WebStatusId_" + qry.ProvinceId, 1))//
//				)//
//		);

//		if (qry.CategoryId == 2002 && (DidxHelper.isBeta() || DidxHelper.isLocal()) )
//        {
//			query.must(rangeQuery("PresentProductID").gt(0));
//        }

		if (Arrays.stream(AccessoryCategory).anyMatch(x -> x == qry.CategoryId)
				&& qry.SearchFlags.contains(SearchFlag.ACCESSORY_SCENARIO)) {
			query.must(termQuery("Scenario", 1)).must(rangeQuery("ScenarioFromDate").lte(now))
					.must(rangeQuery("ScenarioToDate").gte(now));
		}

//       Nghia: chuyen sang enum
//        if (qry.FillterType == 4) {
//
//		//query.must(scriptQuery( new Script("(doc['Prices.Price_3'].value - doc['totalPromotionDiscount'].value)  > 1200000")));
//			query.must(scriptQuery( new Script("(doc['Prices.PriceAfterPromotion_'"+ qry.ProductID +"_2].value)  > 1200000")));
//			query.must(termQuery("IsPayment", 1));
//			query.must(rangeQuery("PaymentFromDate").lte(new Date()));
//			query.must(rangeQuery("PaymentToDate").gte(new Date()));
//			query.must(termQuery("IsOnlineOnly", false));
//
//		}
		/**
		 * - bỏ thay bằng check trong List<SearchFlag> có chưa
		 * ORDER_COUPLE_WATCH,PROMOTION_DISCOUNTPERCENT_GT10 hay k
		 */
//        if (qry.ExtensionObject == SearchFlag.ORDER_COUPLE_WATCH) { // ko lay dh cap
//            query.must(termQuery("IsCoupleWatch", false));
//        }

		// multi máy lạnh luôn luôn truyền
		if (!qry.SearchFlags.contains(SearchFlag.ISMULTIAIRCONDITIONER)) {
			SearchFlag.ISMULTIAIRCONDITIONER.query(query, qry);
		}

		for (var search : qry.SearchFlags) {
			search.query(query, qry);
		}

		// flag theo nhóm: những flag cùng một nhóm sẽ được thao tác OR, một số flags ko
		// áp dụng được cho nhóm
		if (qry.SearchFlagGroups != null) {
			for (var set : qry.SearchFlagGroups) {
				var should = boolQuery();
				for (var search : set) {
					var inner = boolQuery();
					search.query(inner, qry);
					should.should(inner);
				}
				query.must(should);
			}
		}


		// search khoảng giá trị của prop
		if (qry.propRanges != null) {
			for (var prop : qry.propRanges) {
				if (prop.propertyID <= 0)
					continue;
				var range = rangeQuery("rangeProp.prop_" + prop.propertyID).gte(prop.from);
				if (prop.to >= prop.from)
					range.lte(prop.to);
				query.must(range);
			}
		}

		// Nghia: chuyen sang enum SEARCH_2020
//		if(qry.SearchType == SearchType.SEARCH2020 || qry.SearchNumberType == SearchNumberType.E_2020) {
//			if(Arrays.asList(Appliance2142Categories).contains(qry.CategoryId)) {
//				query.must(boolQuery()
//						.should(termQuery("Scenario", 1))
//						.should(rangeQuery("PromotionDiscountPercent_"+qry.ProvinceId).gte(30))
//						);
//			}else if(Arrays.asList(ListCateDungCu).contains(qry.CategoryId) || qry.CategoryId == 2162){
//				query.must(boolQuery()
//						.should(termQuery("Scenario", 1))
//						.should(rangeQuery("PromotionDiscountPercent_"+qry.ProvinceId).gte(10))
//						);
//			}else {
//
//				query.must(boolQuery()
//						.should(termQuery("Scenario", 1))
//						.should(rangeQuery("PromotionDiscountPercent").gte(10))
//						.should(boolQuery().must(termQuery("CategoryID", NewApplianceCategories))
//								.must(termQuery("HasPromotion", 1)))
//						);
//			}
//			query.must(scriptQuery( new Script(" (doc['Prices.Price_" + qry.ProvinceId + "'].value * 0.91) >= doc['Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId + "'].value ")));
//			//loại bỏ sp km > 60%
//			query.mustNot(rangeQuery("PromotionDiscountPercent").gte(60));
//		}
		// chuyen sang enum
//		if(qry.IsMonoPoly == 1) {
//			query.must(termQuery("IsMonopolyLabel", true));
//		}
//		if (qry.SearchFlags != null && qry.SearchFlags.size() > 0
//				&& qry.SearchFlags.contains(SearchFlag.PROMOTION_DISCOUNTPERCENT_GT10)) { // lay san pham co km thoa
//																							// dieu kien
//			query.must(boolQuery()//
//					.should(termQuery("Scenario", 1))//
//					.should(rangeQuery("PromotionDiscountPercent_"+qry.ProvinceId).gt(10))//
//			);
//		}

		// Nghia: chuyen sang enum SCENARIO
//		if (Arrays.asList(AccessoryCategory).contains(qry.CategoryId) )
//        {
//        	if(qry.SearchNumberType == SearchNumberType.SCENARIO){
//				query.must(termQuery("Scenario", 1));
//				query.must(rangeQuery("ScenarioFromDate").lte(now));
//				query.must(rangeQuery("ScenarioToDate").gte(now));
//			}
//        }

		// loai sp ko co anh
		// filter &= !Query<ProductSO>.Term(k => k.NoImage, true);
		// loai sp gop
		// filter &= !Query<ProductSO>.Term("IsRepresentProduct", 1);

		// loai bo sp ko kinh doanh truoc 1/1/2015
		query.must(boolQuery()//
				.should(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, new int[] { 2, 3, 4, 6, 8, 9, 11, 99, 98 }))//
				.should(boolQuery()//
						.mustNot(termsQuery("Prices.WebStatusId_" + qry.ProvinceId,
								new int[] { 2, 3, 4, 6, 8, 9, 11, 99, 98 }))//
						.must(rangeQuery("DateCreated").gte(new Date(1420045200000l))))//
		);

		// sp gộp ko quan tâm hình đại diện
		//if (!qry.SearchFlags.contains(SearchFlag.COMBINEDPRODUCT)) {
		//	query.must(termQuery("HasBimage", 1));
		//}
		///////////

		if (qry.SiteId > 0)
			query.must(termQuery("SiteID", qry.SiteId));

		if(qry.CreatedDateFrom !=null){
			query.must(rangeQuery("DateCreated").gte(qry.CreatedDateFrom));
		}
		if(qry.CreatedDateTo!=null){
			query.must(rangeQuery("DateCreated").lte(qry.CreatedDateTo));
		}

		if (qry.LanguageID != null && !qry.LanguageID.isBlank())
			query.must(termQuery("Lang", DidxHelper.GenTerm3(qry.LanguageID)));

		if (qry.ManufacturerIdList != null && qry.ManufacturerIdList.length > 0) {
			timer.start("es-manuidlist");
			var mq = boolQuery();
			var names = productHelper.getManuNames(qry.ManufacturerIdList, qry.SiteId, qry.LanguageID);
			if (names != null && names.length > 0) {
				mq.should(termsQuery("ManufacturerName", names));
			}
			mq.should(termsQuery("ManufactureID", qry.ManufacturerIdList));
			query.must(mq);
			timer.pause("es-manuidlist");
		} else if (qry.ManufacturerId > 0) {
			query.must(termQuery("ManufactureID", qry.ManufacturerId));
		}

//		Nghia: chuyen sang enum searchFlag
//		if(qry.IsFeature == 1) {
//			query.must(termQuery("IsFeature", 1));
//		}

//		if (qry.HasPromotion > 0) {
//			if (qry.HasPromotion == 2) { // văn hạnh 24/09/2019
//				if (qry.CategoryId == 57 || qry.CategoryId == 58 || qry.CategoryId == 54 || qry.CategoryId == 2162) {
//					// 12/05/2020 văn hạnh
//					query.must(boolQuery()
//							.should(boolQuery().must(
//									scriptQuery(new Script(" 100 - (doc['Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId + "'].value / doc['Prices.Price_" + qry.ProvinceId + "'].value) * 100  >= 20") )
//									))//
//							.should(termQuery("IsPromotionSaving", true))//
//							.should(boolQuery()//
//									.must(termQuery("Scenario", 1))//
//									.must(rangeQuery("ScenarioFromDate").lte(now)) //
//									.must(rangeQuery("ScenarioToDate").gte(now)) )
//							);
//
//					//query.must(scriptQuery( new Script(" (doc['Prices.Price_" + qry.ProvinceId + "'].value * 0.91) >= doc['Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId + "'].value ")));
//				}
//			} else {
//				query.must(termQuery("HasPromotion", 1));
//			}
//		}

//		if (qry.CategoryId == -4) {
//
//			// Search chỉ ngành hàng chính
//			query.must(termsQuery("CategoryID", ProductHelper.MainProductCategory));
//
//		} else if (qry.CategoryId == -3) { // Khi search phụ kiện, bỏ qua các con collection, chỉ lấy detail
//			query.must(termQuery("IsCollection", 0));
//			query.must(termsQuery("CategoryID", ProductHelper.AccessoryCategory));
//			query.must(boolQuery().should(termsQuery("Prices.WebStatusId_" + qry.ProvinceId,
////							new String[] { "2", "3", "4", "5", "6", "8", "9" }
//					new int[] { 2, 3, 4, 5, 6, 8, 9 } //
//			)).should(termQuery("IsReferAccessory", 1)));
//
//		} else if (qry.SearchType != null) {
//			if (qry.SearchType == SearchType.SEARCH)
//				query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, new int[] { 4, 5, 1 }));
//			else if (qry.SearchType == SearchType.CATE)
//				query.must(termQuery("Prices.WebStatusId_" + qry.ProvinceId, 4));
//		} else {
//			if (qry.WebStatus > 0)
//				query.must(termQuery("Prices.WebStatusId_" + qry.ProvinceId, qry.WebStatus));
//			else if (qry.WebStatus == -2) {
//				query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, new int[] { 4, 5 }));
//			} else if (qry.WebStatus == -1) {
//				query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, new int[] { 4, 5 }));
//				query.must(termsQuery("Prices.IsShowHome_" + qry.ProvinceId, "1"));
//			}
//		}

		var statusQ = boolQuery();
		statusQ.should(termQuery("CmsProductStatus", 1));
		// lấy sp hàng sắp về (bỏ)
//		statusQ.should(boolQuery().must(rangeQuery("DateCreated").gte("now-60d/d").lte("now/d"))
//            .must(termQuery("Prices.Price_" + qry.ProvinceId, 0)));
		if (qry.WebStatusIDList != null && qry.WebStatusIDList.length > 0) {
			statusQ.should(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, qry.WebStatusIDList));
		} else if (qry.WebStatus > 0) {
			query.must(termQuery("Prices.WebStatusId_" + qry.ProvinceId, qry.WebStatus));
		} else if (qry.WebStatus < 0) {
			if (qry.WebStatus == -5) {
				query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, new int[] { 3, 4, 11 }));
			} else {

				// Dieu kien WebStatus < 0 ben DotNet
				List<Integer> eleccate = Arrays.asList(1942, 1943, 1944, 2002, 1962, 2202);// hang dien tu
				List<Integer> telecate = Arrays.asList(42, 44);// hang vien thong -- dien thoai, -- laptop

				if (eleccate.contains(qry.CategoryId)) {
					query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, new int[] { 2, 3, 4, 6, 8, 9, 11 } //
					));
				} else if (telecate.contains(qry.CategoryId)) {
					query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, new int[] { 2, 3, 4, 6, 8, 9, 11, 99 } //
					));

//				}else if (qry.CategoryId == 5697)//nếu sp là online only thì lấy luôn trạng thái 98
//				{
//					query.must(boolQuery().should(boolQuery().must(termQuery("IsOnlineOnly", true))
//							.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId,
//									new int[] { 2, 3, 4, 6, 8, 9, 11, 99, 98 })))
//							.should(boolQuery().must(termQuery("IsOnlineOnly", false)).must(termQuery(
//									"Prices.WebStatusId_" + qry.ProvinceId, new int[] { 2, 3, 4, 6, 8, 9, 11, 99 }))));

				} else {// Neu sp online only thi lay luon trang thai 98

//					query.must(boolQuery()
//							.should(boolQuery()
//									.must(termQuery("IsOnlineOnly", true))
//									.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId,
//									new int[] { 2, 3, 4, 6, 8, 9, 11, 99, 98 }))
//									)
//							.should(boolQuery().must(termQuery("IsOnlineOnly", false))
//										.must(termQuery("Prices.WebStatusId_" + qry.ProvinceId, new int[] { 2, 3, 4, 6, 8, 9, 11, 99 }))
//										));

					query.must(boolQuery()
							.should(boolQuery().must(termQuery("IsOnlineOnly", true))
									.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId + "",
											new int[] { 2, 3, 4, 6, 8, 9, 11, 99, 98 })))
							.should(boolQuery().must(termQuery("IsOnlineOnly", false))
									.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId + "",
											new int[] { 2, 3, 4, 6, 8, 9, 11, 99 }))));

				}

			}

		}

		query.must(statusQ);

//		if (qry.WebStatus == 0 && !Utils.StringIsEmpty(qry.Keyword))// trang search
//        {
//
//            if ((qry.PropertyDetailFilters != null && qry.PropertyDetailFilters.size() > 0) ||
//                    (qry.PropSearch != null && qry.PropSearch.length > 0)) {
//                List<Integer> eleccate = Arrays.asList(1942, 1943, 1944, 2002, 1962, 2202);// hang dien tu
//                if (eleccate.contains(qry.CategoryId)) {
//                    query.must(termQuery("Prices.WebStatusId_" + qry.ProvinceId,
//                            new int[]{2, 3, 4, 6, 8, 9, 11} //
//                    ));
//
//                } else {
//                    query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId,
//                            new int[]{2, 3, 4, 6, 8, 9, 11, 99} //
//                    ));
//
//                }
//            }
//        }

//		if (qry.WebStatus == 0)
//			filter.filter(boolQuery().mustNot(termQuery("CategoryID", 2102)));

//				new String[] { "1784", "1783" }
		query.mustNot(termQuery("CategoryID", 2182)); // Khong lay nganh hang vat tu ???
		query.mustNot(termsQuery("CategoryID", new int[] { 8233, 8232, 0 })); // loại gameapp
		query.mustNot(termQuery("ManufactureID", 0));

		query.mustNot(termQuery("IsCollection", 1)); // Khong lay collections ???
		if (qry.WebStatus == 0)
			query.mustNot(termQuery("CategoryID", 1802)); // khong lay thang nay???? code moi

		if (qry.ProductIdEnumerable == null || qry.ProductIdEnumerable.length == 0) {
			if (qry.ProductID > 0)
				query.mustNot(termQuery("ProductID", qry.ProductID));
		} else {
			query.mustNot(termsQuery("ProductID", qry.ProductIdEnumerable));
		}

		query.must(termQuery("ProductType", 1));
		query.must(termQuery("IsRepresentProduct", 0));
		// ẩn sản phẩm tham khảo khỏi trang kq search
		// yêu cầu vonhatnam 3/7/2019
		query.must(termQuery("IsReferAccessory", 0));
		query.must(boolQuery().mustNot(termsQuery("CategoryID",
//				new String[] { "1784", "1783" }
				new int[] { 1784, 1783 } //
		)));

		// PROP (cũ bỏ)
		var propStr = new ArrayList<String>();
		var lstPropsChainWatch = new ArrayList<String>();
		if (qry.PropSearch != null && qry.PropSearch.length > 0) {
			// vanhanh update 19/02/2021
			if (qry.CategoryId != 0) {
				var tmplstProp = Stream.of(qry.PropSearch).filter(x -> x.CompareValue.equals("-99"))
						.collect(Collectors.toList());
				// getManuSeo
				if (tmplstProp != null && tmplstProp.size() > 0) {
					List<String> listManu = new ArrayList<String>();
					for (ProductPropertySearching productPropertySearching : tmplstProp) {
						var manu = productHelper.categoryHelper.getManuSeo(productPropertySearching.PropertyId,
								qry.SiteId, qry.LanguageID);
						if (manu != null) {
							listManu.add(DidxHelper.GenTerm3(manu.ManufacturerName));
						}
					}
					if (listManu.size() > 0) {
						query.must(termsQuery("ManufactureCode", listManu.toArray()));
					}

				}

			} else {
				// qry.CategoryId == 0 //
				// if (qry.PropSearch != null && qry.PropSearch.Any() && qry.CategoryId == 0)
				var tmp = Stream.of(qry.PropSearch).mapToInt(x -> x.PropertyId).toArray();
				query.must(termsQuery("ManufactureID", tmp));
			}
		}

		// PROP
		productHelper.buildPropQuery(qry, query);

		// PRICES
		query.must(rangeQuery("Prices.Price_" + qry.ProvinceId).gte(0));
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
			} else if (qry.PriceFrom > 0 && qry.PriceTo == 0)
				query.must(rangeQuery("Prices.Price_" + qry.ProvinceId).gte(qry.PriceFrom));
		}

		// PROMOTION BÁN KÈM
		if (qry.promotionBKIDs != null && qry.promotionBKIDs.length > 0) {
			query.must(termsQuery("PromotionIdsBankem", qry.promotionBKIDs));
		}

//		if (qry.PropertyFilters != null && qry.PropertyFilters.Count() > 0) {
//			chắc ko sài nữa :D
//		}

//        if (qry.ExtensionObject == -1 || qry.ExtensionObject == -3) {
//            //IsPayment
////            query.must(termQuery("IsPayment", 1));
////            query.must(rangeQuery("PaymentFromDate").lte(new Date()));
////            query.must(rangeQuery("PaymentToDate").gte(new Date()));
//            SearchFlag.ISPAYMENT.query(query,qry);
//        }
//        if (qry.ExtensionObject == -2 || qry.ExtensionObject == -3) {
//            int[] cate_vt = new int[]{42, 44, 522};
//            if (Arrays.asList(AccessoryCategory).contains(qry.CategoryId) || Arrays.asList(cate_vt).contains(qry.CategoryId)) {
//
//                Calendar c = Calendar.getInstance();
//                c.setTime(new Date());
//                c.add(Calendar.DATE, -60);
//                query.must(rangeQuery("DateCreated").gte(c.getTime()));
//            } else {
//                var listCate2020 = new int[]{2002, 1942, 1944, 1943, 3385};
//                var z = Calendar.getInstance();
//                z.setTime(new Date());
//                if (Arrays.asList(listCate2020).contains(qry.CategoryId)) {
//                    query.must(termQuery("AnnouncedYear", z.get(Calendar.YEAR)));
//                } else {
//                    query.must(termQuery("AnnouncedYear", z.get(Calendar.YEAR - 1)));
//                }
//            }
//            //formatterDate
//        }

//        if (qry.ExtensionObject == -4) {
//            var arrproductid = new int[]
//                    {
//                            178983, 137190, 155046, 155058, 155067, 156188, 164505, 156389, 164517, 156396, 152361, 152364, 158070, 159666, 158618, 156079, 156081, 159814, 158279, 70308, 158277, 70307, 158846, 157629, 158749, 72726, 163585, 72778, 158007, 163603, 157643, 70309, 75194, 75195, 167613, 152506, 153033, 152506, 152512, 153100, 153100, 75256, 97718, 75259, 156063, 156067, 165020, 165019, 153222, 156071, 153924, 153921, 154735, 154735, 153923, 156073, 153925, 156074, 153978, 92014, 156068, 153922, 153982, 97262, 148874, 148862, 158619, 148852, 148839, 148894, 148849, 92131, 148807, 71017, 92127, 148895, 148850, 148832, 92126, 148796, 149428, 73950, 148820, 148924, 149483, 149431, 156168, 149448, 114959, 148823, 99732, 149479, 72949, 75236, 75241, 85217, 72862, 71192, 88588, 71191, 153825, 153852, 91124, 153900, 153850, 91125, 91806, 153832, 153901, 133252, 153853, 153806, 91882, 153839, 91881, 75257, 154018, 92129, 73683, 75469, 153819, 73682, 92105, 75412, 92128, 92123, 98061, 92124, 92132, 92121, 91884, 91883, 91880, 100307, 75408, 92122, 91740, 92133, 154023, 91801, 71533, 153855, 153822, 153840, 75468, 151042, 91805, 91887, 162441, 153737, 153870, 100312, 72861, 71693, 75588, 75185, 91847, 88600, 91668, 71256, 75563, 71252, 92230, 133275, 154202, 114569, 97258, 114568, 153871, 154262, 154261, 88249, 148851, 85168, 88346, 154263, 76988, 92018, 75224, 114973, 114980, 154264, 75263, 154650, 154667, 173990, 153740, 153767, 167753, 153671, 153778, 91916, 67766, 167873, 92135, 88008, 101675, 91858, 74889, 91859, 91852, 92930, 154150, 109184, 109358, 109157, 109201, 109202, 73927, 74603, 74622, 74574, 74575, 71257, 74617, 91857, 91856, 92009, 153787, 145456, 106489, 93394, 145587, 96816, 106533, 114404, 114397, 148626, 145975, 93395, 156181, 108366, 156182, 75327, 88627, 158471, 100626, 100650, 156175, 156173, 106489, 158471, 111432, 106487, 108387, 103903, 148973, 104109, 158734, 136377, 143441, 158740, 103919, 92886, 154985, 154998, 110587, 136377, 155202, 92886, 154998, 154985, 92846, 155202, 77721, 154994, 71099, 71104, 149232, 149233, 149174, 154994, 109132, 109134, 149182, 154991, 155200
//                    };
//            query.must(termsQuery("ProductID", arrproductid));
//        }

		HashMap<String, Object> scriptParams = new HashMap<>();
		HashMap<String, Object> scriptParamsNull = new HashMap<>();
		// scriptParams.put("AccessoryCategory", AccessoryCategory);
		scriptParams.put("WebStatusId", new String[] { "2", "4", "8" });
		scriptParams.put("SubCategory", SubCategory); // ưu tiên thấp, còn lại ko ưu tiên
		scriptParams.put("MediumCategory", new String[] { "202", "3385", "7498", "7264", "7077" }); // ưu tiên trung bình
		scriptParams.put("MainCategory", MainCategory); // ưu tiên cao
		scriptParams.put("Combined", qry.SearchFlags != null && qry.SearchFlags.size() > 0
				&& qry.SearchFlags.contains(SearchFlag.COMBINEDPRODUCT) ? 1 : 0); // ưu tiên cao
		if (!Utils.StringIsEmpty(qry.Keyword)) {
			String filterVietKey = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordSearchField(qry.Keyword));
			qry.Keyword = filterVietKey != null ? filterVietKey : "";
			String[] keywords = qry.Keyword.split("\\s+");
			String rsl = "";// không phân rãf
			String rsl2 = "";// phân rã
			String rsl3 = "";

			if (keywords.length <= 2) {
				rsl = rsl2 = "(\"" + qry.Keyword + "\")";
			} else {
				rsl = "(\"" + qry.Keyword + "\")";
				rsl2 = "(" + String.join(" AND ", keywords) + ")";

				// ten cate map it nhat 3 tu trong kw la duoc
				if (keywords.length > 3) {
					rsl3 = "(";
					boolean isEnd = false;

					for (int i = 0; i < keywords.length; i++) {
						var kw = new ArrayList<String>();
						for (int j = i; j < i + 3; j++) {
							if (j > keywords.length - 1) {
								isEnd = true;
								break;
							}
							kw.add(keywords[j]);
						}
						if (isEnd)
							break;
						rsl3 += " ( " + String.join(" AND ", kw) + " ) OR ";

					}
					rsl3 = rsl3.substring(0, rsl3.length() - 4);
					rsl3 += ")";
				} else {
					rsl3 = rsl2;// khoi bi loi
				}
			}

			BoolQueryBuilder queryFunction = null;

			if (qry.SearchFlags.contains(SearchFlag.MAINKEYWORD)) {
				queryFunction = QueryBuilders.boolQuery().should(queryStringQuery(rsl).field("ProductName").boost(800f))
						.should(queryStringQuery(rsl).field("MainKeyword").boost(80f));
			} else if (qry.SearchFlags.contains(SearchFlag.MAINKEYWORD_KEYWORD)) {

				queryFunction = QueryBuilders.boolQuery().should(queryStringQuery(rsl).field("ProductName").boost(800f))
						.should(queryStringQuery(rsl).field("MainKeyword").boost(80f))
						.should(queryStringQuery(rsl).field("Keyword").boost(8f));
			} else {
				queryFunction = QueryBuilders.boolQuery()
						.should(queryStringQuery(rsl3).field("CategoryName").boost(50f))
						.should(queryStringQuery(rsl).field("NewMainKeyword").boost(50f))
						.should(queryStringQuery(rsl).field("NewSubKeyword").boost(10f))
						.should(queryStringQuery(rsl2).field("NewMainKeyword").boost(50f))
						.should(queryStringQuery(rsl2).field("NewSubKeyword").boost(5f))

						//.should(queryStringQuery(rsl3).field("NewSubKeyword").boost(1f))
				// .should(queryStringQuery(rsl).field("NewKeyword").boost(10f))
				// .should(queryStringQuery(rsl2).field("NewKeyword").boost(10f))
				// .should(queryStringQuery(rsl2).field("NewKeyword").boost(1f))
				;
			}
			if (qry.CategoryId <= 0) {
				// suggestsearch
				query.must(QueryBuilders.functionScoreQuery(QueryBuilders.boolQuery()
//									.should(queryStringQuery(rsl).field("CategoryName").boost(100000f))
//									.should(queryStringQuery(rsl2).field("CategoryName").boost(50000f))
//									.should(queryStringQuery(rsl3).field("CategoryName").boost(10000f))
//									.should(queryStringQuery(rsl).field("NewMainKeyword").boost(5000f))
//									.should(queryStringQuery(rsl).field("NewSubKeyword").boost(500f))
//									.should(queryStringQuery(rsl2).field("NewMainKeyword").boost(100f))
//									.should(queryStringQuery(rsl2).field("NewSubKeyword").boost(5f))

						.should(queryStringQuery(rsl).field("CategoryName").boost(1000f))
						.should(queryStringQuery(rsl2).field("CategoryName").boost(500f))
						.should(queryStringQuery(rsl3).field("CategoryName").boost(100f))
						.should(queryStringQuery(rsl).field("NewMainKeyword",1.0f).boost(3000f))
						.should(queryStringQuery(rsl).field("NewSubKeyword").boost(500f))
						.should(queryStringQuery(rsl2).field("NewMainKeyword",1.0f).boost(300f))
						.should(queryStringQuery(rsl2).field("NewSubKeyword").boost(5f))

//									.should(queryStringQuery(rsl).field("NewKeyword").boost(10f))
//			                        .should(queryStringQuery(rsl2).field("NewKeyword").boost(10f))
						,

						new FilterFunctionBuilder[] {
//                            new FilterFunctionBuilder(scriptFunction(new Script(
//									ScriptType.INLINE, "painless",
//									"if( doc['Prices.WebStatusId_" + qry.ProvinceId + "'].size() > 0 && params.WebStatusId.contains(doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value) ) 20000  ",
//									scriptParams))),
								new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
										" if(doc['Prices.WebStatusId_" + qry.ProvinceId + "'].size() > 0 && "
												+ "(doc['Prices.WebStatusId_" + qry.ProvinceId
												+ "'].value == 2 || doc['Prices.WebStatusId_" + qry.ProvinceId
												+ "'].value == 4 || doc['Prices.WebStatusId_" + qry.ProvinceId
												+ "'].value == 8)" + ") 20000 ",
										scriptParamsNull))),
								new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
										"if( doc['Prices.WebStatusId_" + qry.ProvinceId
												+ "'].size() > 0 && doc['Prices.WebStatusId_" + qry.ProvinceId
												+ "'].value == 11 ) 18000  ",
										scriptParams))),
								new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
										"if( doc['Prices.WebStatusId_" + qry.ProvinceId
												+ "'].size() > 0 && doc['Prices.WebStatusId_" + qry.ProvinceId
												+ "'].value == 99 ) 18000 ",
										scriptParams))),
								new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
										"if( doc['Prices.WebStatusId_" + qry.ProvinceId
												+ "'].size() > 0 && doc['Prices.WebStatusId_" + qry.ProvinceId
												+ "'].value == 5 ) 15000 ",
										scriptParams))),
								new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
										"if( doc['Prices.WebStatusId_" + qry.ProvinceId
												+ "'].size() > 0 && ( doc['Prices.WebStatusId_" + qry.ProvinceId
												+ "'].value == 1 || doc['Prices.WebStatusId_" + qry.ProvinceId
												+ "'].value == 7) ) 10 ",
										scriptParams))),
								new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
										"if( params.SubCategory.contains(doc['CategoryID'].value + \"\")){ 100 } else if( params.MainCategory.contains(doc['CategoryID'].value + \"\")){ 22000  } else if( params.MediumCategory.contains(doc['CategoryID'].value + \"\")) { 7000 } else { 1 }", // else
																																																																									// 10000;
										scriptParams))),
								// đè subcate xuống đái :v
								new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
										"if( params.SubCategory.contains(doc['CategoryID'].value + \"\")){ 1 } else  { 30000 }", // else
										// 10000;
										scriptParams))),})
						.scoreMode(ScoreMode.SUM).boostMode(CombineFunction.SUM));

			} else {
				// seach cate

				var ListICTCate = new int[] { 42, 522, 44, 7077 };

				if (Arrays.asList(ListICTCate).contains(qry.CategoryId)) {
					// nghành hàng ict
					query.must(QueryBuilders.functionScoreQuery(queryFunction,

							// - Trạng thái ưu tiên: ICT
							// +Pre order, hàng sắp về
							// + Đang kinh doanh,
							// + Chuyen hang
							// + Tạm hết hàng
							// + Mới ra mắt
							// + Tin đồn
							// + Không kinh doanh, ngừng kinh doanh
							new FilterFunctionBuilder[] {

									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].size() > 0 && ( doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 2 || doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 9)) 20000 ",
											scriptParamsNull))),
									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['IsPreOrder'].size() > 0 && doc['IsPreOrder'] == 1 ) 20000 ",
											scriptParamsNull))),
									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['CmsProductStatus'].size() > 0 && doc['CmsProductStatus'] == 1 ) 20000 ",
											scriptParamsNull))),
									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['Prices.WebStatusId_" + qry.ProvinceId + "'].size() > 0 && "
													+ "(doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 4 || doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 8)" + ") 12000 ",
											scriptParamsNull))),
									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['Prices.WebStatusId_" + qry.ProvinceId + "'].size() > 0 && "
													+ "(doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 11 || doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 99)" + ") 9000 ",
											scriptParamsNull))),
									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['Prices.WebStatusId_" + qry.ProvinceId + "'].size() > 0 && "
													+ "(doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 5 || doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 98)" + ") 7000 ",
											scriptParamsNull))),
									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].size() > 0 && ( doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 1 || doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 7)) 10 ",
											scriptParamsNull))),
									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['IsHearSay'].size() > 0 && doc['IsHearSay'] == 1 ) 2000 ",
											scriptParamsNull))),

							}).scoreMode(ScoreMode.SUM).boostMode(CombineFunction.SUM));
				} else {
					//// - Trạng thái ưu tiên CE:
					// +Đang kinh doanh
					// + CHUYỂN HÀNG
					// + Pre order, hàng sắp về
					// + Tạm hết hàng
					// + Mới ra mắt
					// + Tin đồn
					// + Không kinh doanh, ngừng kinh doanh

					query.must(QueryBuilders.functionScoreQuery(queryFunction,
							// ưu tiên CE
							new FilterFunctionBuilder[] {

									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['Prices.WebStatusId_" + qry.ProvinceId + "'].size() > 0 && "
													+ "(doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 4 )"
													+ ") 20000 ",
											scriptParamsNull))),
									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['Prices.WebStatusId_" + qry.ProvinceId + "'].size() > 0 && "
													+ "(doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 11 || doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 99)" + ") 15000 ",
											scriptParamsNull))),
									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['Prices.WebStatusId_" + qry.ProvinceId + "'].size() > 0 && "
													+ "(doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 2 || doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 8 || doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 9)" + ") 10000 ",
											scriptParamsNull))),
									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['IsPreOrder'].size() > 0 && doc['IsPreOrder'] == 1 ) 10000 ",
											scriptParamsNull))),

									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['CmsProductStatus'].size() > 0 && doc['CmsProductStatus'] == 1 ) 10000 ",
											scriptParamsNull))),

									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['Prices.WebStatusId_" + qry.ProvinceId + "'].size() > 0 && "
													+ "(doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 5 || doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 98)" + ") 5000 ",
											scriptParamsNull))),
									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['IsHearSay'].size() > 0 && doc['IsHearSay'] == 1 ) 2000 ",
											scriptParamsNull))),
									new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
											" if(doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].size() > 0 && ( doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 1 || doc['Prices.WebStatusId_" + qry.ProvinceId
													+ "'].value == 7)) 10 ",
											scriptParamsNull))),

							}).scoreMode(ScoreMode.SUM).boostMode(CombineFunction.SUM));
				}

			}
			// if(qry.OrderByPrice == OrderType.ASCENDING) {
			if (qry.Orders != null && qry.Orders.length > 0
					&& Arrays.asList(qry.Orders).containsAll(Arrays.asList(SearchOrder.PRICE_ASC))) {
				query.must(rangeQuery("Prices.Price_" + qry.ProvinceId).gt(0));
				query.mustNot(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, new int[] { 1, 5, 7, 98, 99 }));
			} else
			// if(qry.OrderByPrice == OrderType.DESCENDING) {
			if (qry.Orders != null && qry.Orders.length > 0
					&& Arrays.asList(qry.Orders).containsAll(Arrays.asList(SearchOrder.PRICE_DESC))) {
				query.must(rangeQuery("Prices.Price_" + qry.ProvinceId).gt(0));
				query.mustNot(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, new int[] { 1, 5, 7, 98, 99 }));
			}
		} // end nếu có truyền keyword
		else {
			if (qry.SearchFlags.contains(SearchFlag.GOOD_EXPERIENCE_PAGE)) { // trang KNH ; ExtensionObject = 2

				query.must(QueryBuilders.functionScoreQuery(new FunctionScoreQueryBuilder.FilterFunctionBuilder[] {

						new FunctionScoreQueryBuilder.FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE,
								"painless",
								"if(doc['IsDetailImage'].value >= 1){ if(  (new Date(doc['PaymentToDate'].value).getTime()   >= new Date().getTime()) && (new Date().getTime()>=   new Date(doc['PaymentFromDate'].value).getTime() ) ){  100000  }else {1}  } ",
								scriptParams))),
						new FunctionScoreQueryBuilder.FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE,
								"painless",
								"if(doc['IsPayment'].value >= 1){ if(  (new Date(doc['PaymentToDate'].value).getTime()   >= new Date().getTime()) && (new Date().getTime()>=   new Date(doc['PaymentFromDate'].value).getTime() ) ){  10000   }else {1}  } ",
								scriptParams))),
						// new FilterFunctionBuilder(scriptQuery(new
						// Script("if(doc['IsDetailImage'].value >= 1){ if( (new
						// Date(doc['PaymentToDate'].value).getTime() >= new Date().getTime()) && (new
						// Date().getTime()>= new Date(doc['PaymentFromDate'].value).getTime() ) ){
						// 100000 }else {1} } ")), ScoreFunctionBuilders.weightFactorFunction(10000)),
						// new FilterFunctionBuilder(scriptQuery(new Script("if(doc['IsPayment'].value
						// >= 1){ if( (new Date(doc['PaymentToDate'].value).getTime() >= new
						// Date().getTime()) && (new Date().getTime()>= new
						// Date(doc['PaymentFromDate'].value).getTime() ) ){ 10000 }else {1} } ")),
						// ScoreFunctionBuilders.weightFactorFunction(10000)),
						new FunctionScoreQueryBuilder.FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE,
								"painless", "if( doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 2 ) 5000 ",
								scriptParams))),
						new FunctionScoreQueryBuilder.FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE,
								"painless", "if( doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 8 ) 4000 ",
								scriptParams))),
						new FunctionScoreQueryBuilder.FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE,
								"painless", "if( doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 4 ) 3000 ",
								scriptParams))),
						new FunctionScoreQueryBuilder.FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE,
								"painless", "if( doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 11 ) 2000 ",
								scriptParams))),
						new FunctionScoreQueryBuilder.FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE,
								"painless", "if( doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 99 ) 1000 ",
								scriptParams))), })
						.scoreMode(FunctionScoreQuery.ScoreMode.SUM).boostMode(CombineFunction.SUM));

			} else {
				if (Arrays.asList(MainProductCategoryForNewPage).contains(qry.CategoryId)) {
					query.must(QueryBuilders.functionScoreQuery(new FilterFunctionBuilder[] {
							new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
									" if (doc['DisplayOrder'].value >= 2 && doc['DisplayOrder'].value <= 98) "
											+ "                            {"
											+ "                                if (doc['FeatureStartDate'].value != null && new Date(doc['FeatureStartDate'].value).before(new Date())) "
											+ "                                {"
											+ "                                    if(doc['CategoryID'].value==44 && doc['FeatureExpireDate'].value != null && new Date(doc['FeatureExpireDate'].value).after(new Date())) "
											+ "                                        return 150000*(100-doc['DisplayOrder'].value);"
											+ "                                    else"
											+ "                                       {"
											+ "                                            if(doc['CategoryID'].value!=44)  return 150000*(100-doc['DisplayOrder'].value);"
											+ "                                             else return doc['ProductSoldCount'].value; "
											+ "                                       }"
											+ "                                }"
											+ "                                else"
											+ "                                {"
											+ "                                    if(doc['CategoryID'].value==44) return doc['ProductSoldCount'].value;"
											+ "                                     else return doc['Prices.Price_3'].value * 0.0001;"
											+ "                                }" + "                        }"
											+ "                        else" + "                        {"
											+ "                              if(doc[CategoryID'].value==44) return doc['ProductSoldCount'].value;"
											+ "                              else return doc['Prices.Price_3'].value * 0.0001;"
											+ "                        } ",
									scriptParams))), })
							.scoreMode(ScoreMode.SUM).boostMode(CombineFunction.SUM));
				} else {
//					if (qry.SearchNumberType != 2020 && qry.SearchType != SearchType.SEARCH2020) {
//						query.must(QueryBuilders.functionScoreQuery(
//								new FilterFunctionBuilder[] {
//										new FilterFunctionBuilder(scriptFunction(new Script(
//												ScriptType.INLINE, "painless",
//												" doc['IsDetailImage'].value ",
//												scriptParams))),
//										new FilterFunctionBuilder(scriptFunction(new Script(
//												ScriptType.INLINE, "painless",
//												"if( doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 4 ) 100000 ",
//												scriptParams))),
//										new FilterFunctionBuilder(scriptFunction(new Script(
//												ScriptType.INLINE, "painless",
//												"if( doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 11 ) 90000 ",
//												scriptParams))),
//										new FilterFunctionBuilder(scriptFunction(new Script(
//												ScriptType.INLINE, "painless",
//												"if( doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 99 ) 80000 ",
//												scriptParams))),
////										new FilterFunctionBuilder(scriptQuery(new Script(" doc['IsDetailImage'].value ")), null),
////										new FilterFunctionBuilder(scriptQuery(new Script("if( doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 4 ) 100000 ")), ScoreFunctionBuilders.weightFactorFunction(0)),
////										new FilterFunctionBuilder(scriptQuery(new Script("if( doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 11 ) 90000 ")), ScoreFunctionBuilders.weightFactorFunction(0)),
////										new FilterFunctionBuilder(scriptQuery(new Script("if( doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 99 ) 80000 ")), ScoreFunctionBuilders.weightFactorFunction(0))
//										})
//								.scoreMode(ScoreMode.SUM).boostMode(CombineFunction.SUM));
//					}
					if (!qry.SearchFlags.contains(SearchFlag.SEARCH_2020)) {
						query.must(
								QueryBuilders.functionScoreQuery(new FunctionScoreQueryBuilder.FilterFunctionBuilder[] {

										new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE,
												"painless",
												"if( params.Combined == 1 && doc['Prices.PresentStatus_"
														+ qry.ProvinceId + "'].length > 0 && doc['Prices.PresentStatus_"
														+ qry.ProvinceId + "'].value> 0 ){ 100000 } ", // else 10000;
												scriptParams))),
										new FunctionScoreQueryBuilder.FilterFunctionBuilder(
												scriptFunction(new Script(ScriptType.INLINE, "painless",
														" doc['IsDetailImage'].value ", scriptParams))),
										new FunctionScoreQueryBuilder.FilterFunctionBuilder(
												scriptFunction(new Script(ScriptType.INLINE, "painless",
														"if( doc['Prices.WebStatusId_" + qry.ProvinceId
																+ "'].value == 4 ) 100000 ",
														scriptParams))),
										new FunctionScoreQueryBuilder.FilterFunctionBuilder(
												scriptFunction(new Script(ScriptType.INLINE, "painless",
														"if( doc['Prices.WebStatusId_" + qry.ProvinceId
																+ "'].value == 11 ) 90000 ",
														scriptParams))),
										new FunctionScoreQueryBuilder.FilterFunctionBuilder(
												scriptFunction(new Script(
														ScriptType.INLINE, "painless", "if( doc['Prices.WebStatusId_"
																+ qry.ProvinceId + "'].value == 99 ) 80000 ",
														scriptParams))),
//										new FilterFunctionBuilder(scriptQuery(new Script(" doc['IsDetailImage'].value ")), null),
//										new FilterFunctionBuilder(scriptQuery(new Script("if( doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 4 ) 100000 ")), ScoreFunctionBuilders.weightFactorFunction(0)),
//										new FilterFunctionBuilder(scriptQuery(new Script("if( doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 11 ) 90000 ")), ScoreFunctionBuilders.weightFactorFunction(0)),
//										new FilterFunctionBuilder(scriptQuery(new Script("if( doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 99 ) 80000 ")), ScoreFunctionBuilders.weightFactorFunction(0))
								}).scoreMode(FunctionScoreQuery.ScoreMode.SUM).boostMode(CombineFunction.SUM));
					}

				}
			}
		}
		SearchResponse queryResults = null;
		SearchRequest searchRequest = new SearchRequest(config.ELASTICSEARCH_PRODUCT_INDEX);// .types(CurrentTypeDB);
		String rsl = "";
		String dstatus = "doc['Prices.WebStatusId_" + qry.ProvinceId + "']";
		String keyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(qry.Keyword));
		if (isGetFacetCate) {

//                sb.aggregation(terms("FacetTermCategoryID2").field("CategoryID").size(100)
//                        .subAggregation(
//                        		count("Sum").field("CategoryID")
//                        		)
////                        .subAggregation(
////                        		termQuery("Prices.WebStatusId_"+qry.ProvinceId, 4)
////                        		)
//                        );
			sb.aggregation(terms("FacetTermCategoryID").field("CategoryID").size(100)
					// .order(BucketOrder.aggregation("stock", false))
					.subAggregation(count("Sum").field("CategoryID") // CategoryID
					)
					.subAggregation(sum("notstock").script(scriptFunction("(doc['Prices.WebStatusId_" + qry.ProvinceId
							+ "'].size() > 0 && " + "( doc['Prices.WebStatusId_" + qry.ProvinceId
							+ "'].value == 1 || doc['Prices.WebStatusId_" + qry.ProvinceId + "'].value == 7)) ? 1 : 0 ")
									.getScript()))
//                        .subAggregation(sum("stock")
//                        		.script(scriptFunction("doc['Prices.WebStatusId_" + qry.ProvinceId + "'].size() > 0 && doc['Prices.WebStatusId_"+qry.ProvinceId+"'].value == 4 ? 1 : 0 " ).getScript()))

//                        		.script(new Script(ScriptType.INLINE, "painless",
//                        				//"params.MainCategory.contains(doc['CategoryID'].value) ?" +
//                        				" doc['Prices.WebStatusId_" + qry.ProvinceId + "'].size() > 0 && doc['Prices.WebStatusId_"+qry.ProvinceId+"'].value == 4) ? 1 : 0 "
//                        				//+ " : "
//                        				//+ "params.SubCategory.contains(doc['CategoryID'].value) == false && doc['Prices.WebStatusId_" + qry.ProvinceId + "'].size() > 0 && doc['Prices.WebStatusId_"+qry.ProvinceId+"'].value == 4 ? 1 : 0 "
//                        				,scriptParams ) ))

//		                        .script(new Script(ScriptType.INLINE, "painless",
//		                				"if(params.MainCategory.contains(doc['CategoryID'].value)){"
//		                				+ " doc['Prices.WebStatusId_" + qry.ProvinceId + "'].size() > 0 && doc['Prices.WebStatusId_"+qry.ProvinceId+"'].value == 4 ? 3 : 1 "
//		                				+ "} else {"
//		                				+ "doc['Prices.WebStatusId_" + qry.ProvinceId + "'].size() > 0 && doc['Prices.WebStatusId_"+qry.ProvinceId+"'].value == 4 ? 1 : 0 "
//		                				+ "} ",scriptParams ) ))
//                        		.script(new Script(ScriptType.INLINE, "painless",
//                                        "params.SubCategory.contains(doc['CategoryID'].value) ? 0 : 1 ",
//                                        scriptParams))
			);
			// termQuery("Prices.WebStatusId_"+qry.ProvinceId, 4).fieldName("CategoryID")

			// sb.aggregation(terms("FacetTermCategoryID").field("CategoryID").size(100)
			// .order(BucketOrder.aggregation("hasproductinstock", false))
			// .subAggregation(count("Sum").field("ProductID"))
			// .subAggregation(max("hasproductinstock")
			// .script(scriptFunction(dstatus + ".size() > 0 && " + dstatus + ".value == 4 ?
			// 1 : 0")
			// .getScript()))
			// .subAggregation(max("maincatescore").script(scriptFunction(
			// "doc['CategoryID'].value == 42 ? 150 : doc['CategoryID'].value == 44 ? 125 :
			// doc['CategoryID'].value == 522 ? 100 : doc['CategoryID'].value == 7077 ? 80 :
			// doc['CategoryID'].value == 7264 ? 60 : 40")
			// .getScript())));

		}
		if (isGetFacetManu) {
			sb.aggregation(terms("FacetTermManufactureID").field("ManufactureID").size(500)
					.subAggregation(count("Sum").field("ProductID")));
		}
		if (isGetFacetProp) {
			sb.aggregation(addAggregation());
		}

		int from = qry.PageIndex == 0 ? qry.startingFrom : qry.PageIndex * qry.PageSize;

		sb.from(from).size(qry.PageSize);
		/**
		 * thêm yêu cầu lấy giá min max theo fillter 20/5/2021
		 */
		sb.aggregation(min("priceMin").field("Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId))
				.aggregation(max("priceMax").field("Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId));

		if (qry.Orders != null && qry.Orders.length > 0) {

			for (var order : qry.Orders) {
//                    if(Arrays.asList (qry.Orders).containsAll(Arrays.asList(new SearchOrder[] { SearchOrder.PRICE_ASC,SearchOrder.PRICE_DESC }.clone() )) ){
//                        query.must(rangeQuery("Prices.Price_" + qry.ProvinceId + "").gt(0));
//                    }

				order.sort(sb, qry);
			}

		}

		if (Utils.StringIsEmpty(keyword)) { // keyword null, trang NN trang nghành hàng

//				if(qry.SearchNumberType == 2020 || qry.SearchType == SearchType.SEARCH2020) {
//					if(qry.ExtensionObject == 30) {
////						if(qry.Orders.length > 0) {
////							sb.sort("PromotionDiscountPercent", SortOrder.ASC);
////						}else {
////							sb.sort("PromotionDiscountPercent", SortOrder.DESC);
////						}
//
//						if (qry.Orders != null && qry.Orders.length > 0 && Arrays.asList(qry.Orders).containsAll(Arrays.asList(SearchOrder.DISCOUNT_PERCENT_ASC, SearchOrder.DISCOUNT_PERCENT_DESC))) {
//							for (var order : qry.Orders)
//								order.sort(sb, qry.ProvinceId);
//						}
//						query.must(scriptQuery(new Script("return (100 - (doc['Prices.PriceAfterPromotion_3_2'].value / doc['prices.Price_3_2'].value * 100)) * 100000")));
//					}else {
//						if(DidxHelper.isBeta() || DidxHelper.isStaging() || DidxHelper.isLoc()) {
//							if(qry.OrderByPrice == OrderType.ASCENDING) {
//								sb.sort("Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId + "", SortOrder.ASC);
//							}else if(qry.OrderByPrice == OrderType.DESCENDING){
//								sb.sort("Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId + "", SortOrder.DESC);
//							}else {
//								sb.sort("ProductSoldCount", SortOrder.DESC);
//							}
//						}else {
//
//							if (qry.Orders != null && qry.Orders.length > 0) {
//								for (var order : qry.Orders)
//									order.sort(sb, qry.ProvinceId,qry);
//							}
//
////							if(qry.OrderByPrice == OrderType.ASCENDING) {
////								sb.sort("Prices.Price_" + qry.ProvinceId + "", SortOrder.ASC);
////							}else if(qry.OrderByPrice == OrderType.DESCENDING){
////								sb.sort("Prices.Price_" + qry.ProvinceId + "", SortOrder.DESC);
////							}else {
////								sb.sort("ProductSoldCount", SortOrder.DESC);
////							}
//						}
//					}
//				}

//				else {
//					if(qry.OrderByPrice == OrderType.ASCENDING) {
//						sb.sort("Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId + "", SortOrder.ASC);
//					}else if(qry.OrderByPrice == OrderType.DESCENDING){
//						sb.sort("Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId + "", SortOrder.DESC);
//					}else if(qry.CategoryId == 7264){
//						sb.sort("DisplayOrder", SortOrder.ASC);
//					}else {
//						sb.sort("_score", SortOrder.DESC);
//					}
//				}

		} else { // trang tim kiếm trang search
//				if(qry.OrderByPrice == OrderType.DESCENDING) {
//					sb.sort("Prices.Price_" + qry.ProvinceId + "", SortOrder.DESC);
//				}else if(qry.OrderByPrice == OrderType.ASCENDING) {
//					sb.sort("Prices.Price_" + qry.ProvinceId + "", SortOrder.ASC);
//				}else {
//					sb.sort("_score", SortOrder.DESC);
//				}
//
		}

		// thong ke webstatus
		sb.aggregation(terms("FaceWebstatusID").field("Prices.WebStatusId_" + qry.ProvinceId).size(100)
				.subAggregation(count("Sum").field("Prices.WebStatusId_" + qry.ProvinceId)));
		sb.aggregation(terms("FaceCmsWebstatusID").field("CmsProductStatus").size(100)
				.subAggregation(count("Sum").field("CmsProductStatus")));
		searchRequest.source(sb.query(query));
		if (timer == null) {
			timer = new CodeTimers();
		}
		if (DidxHelper.isLocal()) {
			System.out.println(sb);
		}


//		var elasticClient1 = new ElasticClient(config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {
//			timer.start("es-query");
			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
			timer.pause("es-query");
//
//		} catch (Throwable e) {
//			Logs.LogException(e);
//			e.printStackTrace();
//		} finally {
//			elasticClient1.close();
//		}

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

		// List<FaceWebstatus> listwwebstatus = new ArrayList<FaceWebstatus>();

		ParsedLongTerms statusbucket = (ParsedLongTerms) aggrs.get("FaceWebstatusID");
		var nkd = List.of(1, 7, 5);// isNotSelling thêm trạng thái 5, c Phương yêu cầu.
		var statuses = statusbucket.getBuckets().stream().map(x -> x.getKeyAsNumber().intValue())
				.collect(Collectors.toList());

		ParsedLongTerms cmsstatusbucket = (ParsedLongTerms) aggrs.get("FaceCmsWebstatusID");
		var cmsnkd = List.of(2);
		var cmsStatuses = cmsstatusbucket.getBuckets().stream().map(x -> x.getKeyAsNumber().intValue())
				.collect(Collectors.toList());
		//// ???????
//            listwwebstatus = statusbucket.getBuckets().stream().map( x -> {
//            	var tmp = new FaceWebstatus();
//            	tmp.WebstatusID = x.getKeyAsNumber().intValue();;
//            	tmp.ProductCount = (int) x.getDocCount();
//            	return tmp;
//            }).collect(Collectors.toList());

		boolean isnkd = nkd.containsAll(statuses) && cmsnkd.containsAll(cmsStatuses);

		List<FaceManuSR> manulist = new ArrayList<FaceManuSR>();

		if (isGetFacetManu) {
			ParsedLongTerms manubucket = (ParsedLongTerms) aggrs.get("FacetTermManufactureID");

			manubucket.getBuckets().forEach(b -> manulist.add(new FaceManuSR() {
				{
					manufacturerID = b.getKeyAsNumber().intValue();
					productCount = (int) b.getDocCount();
				}
			}));

			// FaceWebstatus
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
			// ParsedLongTerms stock = (ParsedLongTerms) aggrs.get("notstock");
			// var cc = catebucket.getBuckets();
			catebucket.getBuckets().forEach(b -> {
				// var catescore = ((Max) b.getAggregations().get("maincatescore")).getValue();
				// var hasproductinstock = ((Max)
				// b.getAggregations().get("hasproductinstock")).getValue();
				// var tmpp = b.getAggregations();
				ParsedSum stock = b.getAggregations().get("notstock");
				var count = stock.getValue();
				catelist.add(new FaceCategorySR() {
					{
						categoryID = b.getKeyAsNumber().intValue();
						productCount = (int) b.getDocCount();
						score = 0;// (int) catescore;
						hasProductInStock = true;// hasproductinstock == 1;
						productCountNoStock = (int) count;

					}
				});
			});

			// catelist.sort(Comparator.comparingInt(FaceCategorySR::getScore).reversed());
			catelist.sort(Comparator.comparingInt(FaceCategorySR::getSort).reversed());
		}

		/**
		 * - lấy ra min max theo aggration
		 */
		Min start = queryResults.getAggregations().get("priceMin");
		Max end = queryResults.getAggregations().get("priceMax");
//            ProductPriceSO priceSO = new ProductPriceSO();
		String maxValue = end.getValueAsString();
		double priceMin = 0, priceMax = 0;
		if (null != maxValue && !maxValue.toLowerCase().contains("infinity")) {
			priceMax = Double.parseDouble(maxValue);
		}

		String minValue = start.getValueAsString();
		if (null != minValue && !minValue.toLowerCase().contains("infinity")) {
			priceMin = Double.parseDouble(minValue);
		}

		result = new ProductSOSR() {
			{
				faceListCategory = catelist.stream().toArray(FaceCategorySR[]::new);
				faceListManu = manulist.stream().toArray(FaceManuSR[]::new);
				faceListProp = Proplist.stream().toArray(FacePropSR[]::new);
				rowCount = rowcount;
				productList = somap;
				isNotSelling = isnkd;
			}
		};
		result.priceMax = priceMax;
		result.priceMin = priceMin;

		if (result != null && result.productList != null && result.productList.size() > 0) {
			var tmpfistobj = result.productList.values().stream().findFirst().orElse(null);
			int firstCate = 0;
			if (tmpfistobj != null) {
				firstCate = tmpfistobj.CategoryID;
			}
			if (firstCate > 0 && result.faceListCategory != null && result.faceListCategory.length > 0
					&& result.faceListCategory[0].categoryID != firstCate && Arrays.stream(result.faceListCategory)
							.map(x -> x.categoryID).collect(Collectors.toList()).contains(firstCate)) {
				var faceListCategory = Arrays.asList(result.faceListCategory);
				var tmpFirtCate = firstCate;
				var cateFirstFaceObj = faceListCategory.stream().filter(x -> x.categoryID == tmpFirtCate).findFirst()
						.orElse(null);
				var cateLastFaceObj = faceListCategory.stream().filter(x -> x.categoryID != tmpFirtCate)
						.toArray(FaceCategorySR[]::new);
				cateLastFaceObj = Arrays.stream(cateLastFaceObj)
						.sorted(Comparator.comparingInt(FaceCategorySR::getSort).reversed())
						.toArray(FaceCategorySR[]::new);
//            		var tempt = faceListCategory.get(0);
//            		result.faceListCategory[0] = result.faceListCategory[index];
//            		result.faceListCategory[index] = tempt;
				faceListCategory.sort(Comparator.comparingInt(FaceCategorySR::getSort).reversed());
				result.faceListCategory = new FaceCategorySR[] { cateFirstFaceObj };
				result.faceListCategory = Utils.pushArray(result.faceListCategory, cateLastFaceObj);
			}
		}

		return result;
	}

	public List<Promotion> GetPromotionsNewOfProductByProvince(List<Promotion> lstPromotion, double price,
			int provinceId, String productCode, int siteId, int type) {

		List<Promotion> result = null;
		try {

			if (lstPromotion != null && lstPromotion.size() > 0) {
				result = lstPromotion.stream()
						.filter(c -> c != null && c.provinceIDApplied(provinceId) && c.BeginDate != null
								&& c.BeginDate.before(new Date()) && c.EndDate != null && c.EndDate.after(new Date()))
						.collect(Collectors.toList());

				var temp = new ArrayList<Promotion>();
				for (var p : result) {
					if (p.FromPrice > 0 && p.ToPrice > p.FromPrice) {
						if (p.FromPrice <= price && price <= p.ToPrice) {
							temp.add(p);
						}
					} else if (p.FromPrice > 0 && p.ToPrice == 0) {
						if (p.FromPrice <= price) {
							temp.add(p);
						}
					} else if (p.FromPrice == 0 && p.ToPrice > 0) {
						if (price <= p.ToPrice)
							temp.add(p);
					} else {
						temp.add(p);
					}
				}

				/*
				 *
				 * ISONLYFORSPECIALSALEPROGRAM: DK KM sinh nhat dac biet IsApplyByTimes: DK km
				 * flash sale IsCheckSalePrice: DK km checksale price p.SavingProgramOutputCount
				 * == p.PromotionOutputTypeCount && p.SavingProgramOutputCount > 0: DK km saving
				 * tiet kiem online PromotionType=18,19: KM offline
				 *
				 */
				if (type == -1)// km tra gop
				{
					result = temp.stream().filter(p -> p.ISONLYFORSPECIALSALEPROGRAM == 1 && p.IsApplyByTimes == 0
							&& p.IsCheckSalePrice == 0 && !(p.SavingProgramOutputCount == p.PromotionOutputTypeCount
									&& p.SavingProgramOutputCount > 0)
							// vh 04/12/2020
							&& !(p.SPECIALOUTPUTTYECOUNT > 0 && p.SPECIALOUTPUTTYECOUNT == p.PromotionOutputTypeCount)
							&& p.PromotionType != 2 && !p.GroupID.equalsIgnoreCase("bankem")
							&& !PromotionTypeAtStore.contains(p.PromotionType))
							.sorted(Comparator.comparingDouble(Promotion::getReturnValue).reversed())
							.collect(Collectors.toList());
				} else if (type == -2)// km flashsale : kh can check loai dk saving vi no cung la km saving
				{
					result = temp.stream().filter(p -> p.ISONLYFORSPECIALSALEPROGRAM == 0 && p.IsApplyByTimes == 1
							&& p.PromotionType < 18 && p.PromotionType != 2 && p.IsCheckSalePrice == 0
							// vh 04/12/2020
							&& !(p.SPECIALOUTPUTTYECOUNT > 0 && p.SPECIALOUTPUTTYECOUNT == p.PromotionOutputTypeCount)
							&& !p.GroupID.equalsIgnoreCase("bankem") && !PromotionTypeAtStore.contains(p.PromotionType))
							.sorted(Comparator.comparingDouble(Promotion::getReturnValue).reversed())
							.collect(Collectors.toList());
				} else if (type == -3)// km check saleprice
				{
					result = temp.stream()
							.filter(p -> p.ISONLYFORSPECIALSALEPROGRAM == 0 && p.IsApplyByTimes == 0
									&& p.PromotionType < 18 && p.IsCheckSalePrice == 1
									&& !(p.SavingProgramOutputCount == p.PromotionOutputTypeCount
											&& p.SavingProgramOutputCount > 0)
									&& p.PromotionType != 2
									// vh 04/12/2020
									&& !(p.SPECIALOUTPUTTYECOUNT > 0
											&& p.SPECIALOUTPUTTYECOUNT == p.PromotionOutputTypeCount)
									&& !p.GroupID.equalsIgnoreCase("bankem")
									&& !PromotionTypeAtStore.contains(p.PromotionType))
							.sorted(Comparator.comparingDouble(Promotion::getReturnValue).reversed())
							.collect(Collectors.toList());
				} else if (type == -4)// km saving tiet kiem
				{
					result = temp.stream().filter(p -> p.ISONLYFORSPECIALSALEPROGRAM == 0 && p.IsApplyByTimes == 0
							&& p.PromotionType < 18 && p.IsCheckSalePrice == 0
							&& (p.SavingProgramOutputCount > 0 && p.PromotionType != 2
									&& p.SavingProgramOutputCount == p.PromotionOutputTypeCount)/*
																								 * day la dk km saving
																								 */
							// vh 04/12/2020
							&& !(p.SPECIALOUTPUTTYECOUNT > 0 && p.SPECIALOUTPUTTYECOUNT == p.PromotionOutputTypeCount)
							&& !p.GroupID.equalsIgnoreCase("bankem") && !PromotionTypeAtStore.contains(p.PromotionType))
							.sorted(Comparator.comparingDouble(Promotion::getReturnValue).reversed())
							.collect(Collectors.toList());
				} else if (type >= 18) // km store 18,19
				{
					result = temp.stream()
							.filter(p -> p.PromotionType == type && p.ISONLYFORSPECIALSALEPROGRAM == 0
									&& p.IsApplyByTimes == 0 && p.IsCheckSalePrice == 0
									&& !(p.SavingProgramOutputCount == p.PromotionOutputTypeCount
											&& p.SavingProgramOutputCount > 0)
									// vh 04/12/2020
									&& !(p.SPECIALOUTPUTTYECOUNT > 0
											&& p.SPECIALOUTPUTTYECOUNT == p.PromotionOutputTypeCount)
									&& p.PromotionType != 2 && !p.GroupID.equalsIgnoreCase("bankem")
									&& PromotionTypeAtStore.contains(p.PromotionType))
							.sorted(Comparator.comparingDouble(Promotion::getReturnValue).reversed())
							.collect(Collectors.toList());
				} else // km thuong
				{
					result = temp.stream().filter(p -> p.PromotionType < 18 && p.PromotionType != 2
							&& p.ISONLYFORSPECIALSALEPROGRAM == 0 && p.IsApplyByTimes == 0 && p.IsCheckSalePrice == 0
							&& !(p.SavingProgramOutputCount == p.PromotionOutputTypeCount
									&& p.SavingProgramOutputCount > 0)
							// vh 04/12/2020
							&& !(p.SPECIALOUTPUTTYECOUNT > 0 && p.SPECIALOUTPUTTYECOUNT == p.PromotionOutputTypeCount))
							.sorted(Comparator.comparingDouble(Promotion::getReturnValue).reversed())
							.collect(Collectors.toList());
				}

			}

		} catch (Exception objEx) {

		}

		return result;
	}

	@Override
	public void processSimpleDetails(ProductBO[] products, int provinceID, int siteID, String lang) throws Throwable {
		productHelper.getHelperBySite(1).processSimpleDetails(products, provinceID, siteID, lang);
	}

	@Override
	public void processDetail(ProductBO product, int siteID, int provinceID, Integer storeID, String lang,
			CodeTimers timer) throws Throwable {
		timer.start("dmx_processdetail_1");
		int productID = product.ProductID;
		if (product.ProductLanguageBO != null) {
			if (product.ProductLanguageBO.ispreordercam <= 0 || product.ProductLanguageBO.preordercamfromdate == null
					|| product.ProductLanguageBO.preordercamtodate == null
					|| product.ProductLanguageBO.preordercamfromdate.after(new Date())
					|| product.ProductLanguageBO.preordercamtodate.before(new Date())) {
				product.ProductLanguageBO.preordercaminfo = "";

			}

		}
		int isGetAll = 1;
		// cache lai
		var listProductGroup = productHelper.GetListProductGroupByCategoryIDFromCach(product.CategoryID, isGetAll, lang,
				siteID);
		// cache lại
		var listKeywordBO = productHelper.GetListKeyWordByCateFromCache(product.CategoryID, siteID);
		if (product.ProductCategoryBO != null) {
			if (listProductGroup != null)
				product.ProductCategoryBO.ListProductGroup = Arrays.asList(listProductGroup);
			if (listKeywordBO != null)
				product.ProductCategoryBO.ListKeywordBO = Arrays.asList(listKeywordBO);
		}

		// lay gia sp combo tu elastic
		if (product.ProductLanguageBO != null && !Utils.StringIsEmpty(product.ProductLanguageBO.comboproductidlist)) {
			productHelper.getComboPrice(product, siteID, lang, provinceID);

		}
		var productDetailFull = productHelper.getCachedProductDetail(productID, siteID, lang, true, 0, timer);
		// get feature property
		if (product.ProductCategoryBO != null && product.ProductCategoryBO.FeaturePropertyID > 0) {

			var productDetail = productDetailFull.PropList;// productHelper.getProductDetail(productID, siteID, lang);
			int FeaturePropertyID = product.ProductCategoryBO.FeaturePropertyID;
			ProductDetailBO[] listProperty;

			if (productDetail != null && productDetail.length > 0) {
				listProperty = productDetail[0].productdetails;
				if (listProperty != null && listProperty.length > 0) {

					var property = Arrays.asList(listProperty).stream().filter(x -> x.PropertyID == FeaturePropertyID)
							.findFirst().orElse(null);

					Set<Integer> setID = new HashSet<Integer>();
					setID.add(FeaturePropertyID);
					var prop = productHelper.getPropTypeAndName(setID, lang);
					if (prop != null && prop.length > 0) {
						product.FeaturePropertyName = prop[0].PropertyName;

						if (property != null) {
							property.Value = StringUtils.strip(property.Value, ",");
							if (prop[0].PropertyType != 0 && !Utils.StringIsEmpty(property.Value)) {// 0 text, 1 1 id, 2
								// nhieu cach
								// dau ,
								int[] lstPropValueID = Stream.of(property.Value.split(",")).mapToInt(x -> {
									try {
										return Integer.parseInt(x);
									} catch (Throwable e) {
										return -1;
									}
								}).filter(x -> x > 0).toArray();
								var propValue = productHelper.getPropValue(lstPropValueID, siteID, lang);
								if (propValue != null && propValue.length > 0) {

									var firstValue = propValue[0];

									product.FeaturePropertyValue = firstValue.Value;
									product.FeaturePropertyCompareValue = firstValue.CompareValue;
									product.FeaturePropertyValueId = firstValue.PropValueID;
								}
							} else {
								product.FeaturePropertyValue = property.Value;
							}
						}

					}
				}
			}
		}

//		if (DidxHelper.isBeta() || DidxHelper.isLocal()) {
//			// máy lạnh multi
//			if (product.CategoryID == 2002) {
//				var MultiAirConditioner = productHelper.getListPreResentProduct(productID, siteID, provinceID, lang);
//				if (MultiAirConditioner != null && MultiAirConditioner.length > 1) {
//					product.IsMultiAirConditioner = true;
//					// filed này để dữ lại các product cho đễ ktr
//					product.MultiAirConditionerIdsAllTime = Stream.of(MultiAirConditioner).map(x -> x.ProductID + "").collect(Collectors.joining(","));
//
//					MultiAirConditioner = Stream.of(MultiAirConditioner)
//							.filter(x -> x.ProductErpPriceBO != null)
//							.filter(x -> Arrays.asList(new String [] { "2", "4", "11" }).contains(x.ProductErpPriceBO.WebStatusId + "")).toArray(ProductBO[]::new);
//
//					Stream.of(MultiAirConditioner).forEach(x -> x.IsMultiAirConditioner = true);
//					var tmp = Stream.of(MultiAirConditioner).map(x -> x.ProductID + "")
//							.collect(Collectors.joining(","));
//					product.MultiAirConditionerIds = Utils.StringIsEmpty(tmp) ? null : tmp.split(",");
//					product.MultiAirConditioners = MultiAirConditioner;
//				}
//
//			}
//		}

		// máy lạnh multi
		productHelper.processMultiAirCondition(product, siteID, provinceID);

		var relative = productHelper.getNextAndPrevGenerationOfProduct(productID);
		if (relative != null && relative.length > 0)
			product.RelativeVersion = relative[0];

		// lay detail tu tgdd

		productHelper.getHelperBySite(1).processDetail(product, siteID, provinceID, storeID, lang, timer);
		timer.pause("dmx_processdetail_1");

		// bo tra gop dmx 20201123

		// tgddkitimage
		if (product.TGDDProductLanguageBO != null) {
			product.TGDDKitimagelarge = product.TGDDProductLanguageBO.kitimagelarge;
			product.TGDDKitimagesmall = product.TGDDProductLanguageBO.kitimagesmall;
		}
	}

	@Override
	public void processPromotion(Promotion item) throws Throwable {
		var tmp = !Utils.StringIsEmpty(item.ReturnValue) ? item.ReturnValue.split("\\|") : null;
		double returnvalue = 0;
		if (tmp != null && tmp.length > 0) {
			for (var value : tmp) {
				returnvalue += Double.valueOf(value);
			}
		}
		item.ReturnValue2 = returnvalue;

		item.IsPercentDiscountDisplay = item.IsPercentDiscountNotWeb == 1;
		item.DiscountValueDisplay = item.IsGiftPrdDiscount == 1 ? item.DiscountValueNotWeb : 0;
		item.QuantityLists = item.QuantityList;
		item.QuantityList = "";
		item.ReturnValues = item.ReturnValue;
		item.FromValue = item.MINTOTALPROMOTIONOFFERMONEY;
		item.ToValue = item.MAXTOTALPROMOTIONOFFERMONEY;
		item.ProductCodes = item.ProductId;
		item.IsOnline = item.ISONLINEOUTPUTTYPE == 1;
		item.newCreatedPercentList = item.createdPercentList;

		item.ExcludeInstallmentProgramID = item.SALEPROGRAMIDLIST;
		item.ApplyStartTime = item.StartTime;
		item.ApplyEndTime = item.EndTime;
		item.ProductApplyType = item.LoaiChon == null ? -1 : item.LoaiChon;
		item.NotApplyForInstallment = item.IsNotApplyForInterestRate;
		item.IsCheapPrice = item.IsSpecialOutputTye;
		item.SpecialOutputTypeCount = item.SPECIALOUTPUTTYECOUNT;
		item.ExcludePromotion = item.EXCLUDEPROMOTIONID;
		item.IsRequestImeiWeb = !Strings.isNullOrEmpty(item.ISREQUESTIMEI);
		item.ProductIds = item.ProductIDref == null ? "0" : item.ProductIDref;

		if (item.createdPercentList == null) {
			item.createdPercentList = "";
		}

	}

	@Override
	public int[] GetHomePageProduct2020(int category, int pageSize, int provinceID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] GetAccessoryHomePageProductNew(int manuID, int countPerCat, int provinceID) {
		try {
			ProductQuery qry = new ProductQuery();

			SearchProduct(null, false, false, false, null);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public SuggestSearchSO[] GetSuggestSearch(String keyword, int siteID) throws Throwable {
		SearchSourceBuilder sb = new SearchSourceBuilder();

		// filter.
		var day = new Date();
		var query = boolQuery();
		var queryBegin = boolQuery();
		var queryProduct = boolQuery();
		var result = new ArrayList<SuggestSearchSO>();

		String rsl = "";
		String keyword_en = "";
		String[] keywords = new String[] {};
		String[] keywords_en = new String[] {};
		if (!Utils.StringIsEmpty(keyword)) {
			keyword_en = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(keyword)).replace("-", "");
			keywords = keyword.split("\\s");
			keywords_en = Utils.StringIsEmpty(keyword_en) ? null : keyword_en.split("\\s");

		}
		if (Utils.StringIsEmpty(keyword_en)) {
			return null;
		}
		if (keywords_en.length == 1)
			rsl = "(" + keyword_en + ")";
		else {
			rsl = "( " + String.join(" AND ", keywords_en) + " )";

		}
		if (keywords_en.length > 0) {
			query.must(termQuery("IsActived", true));
			query.must(termQuery("IsDeleted", false));
			HashMap<String, Object> scriptParams = new HashMap<>();

			scriptParams.put("MainCategory", MainCategory);
			scriptParams.put("SubCategory", SubCategory);
			var cc = Arrays.asList(MainCategory);
			query.must(QueryBuilders.functionScoreQuery(
					QueryBuilders.boolQuery().should(queryStringQuery(rsl).field("KeywordSuggest_En").boost(10000f))
							.should(queryStringQuery(rsl).field("KeywordSearch_En").boost(1000f)),

					new FilterFunctionBuilder[] {
							new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
									"(params.MainCategory.contains(doc['SuggestSearchID'].value + \"\")) ? 50000 : 0 ",
									scriptParams))),
							new FilterFunctionBuilder(scriptFunction(new Script(ScriptType.INLINE, "painless",
									"(params.SubCategory.contains(doc['SuggestSearchID'].value+ \"\")) ? 5000 : 10000  ",
									scriptParams))) })
					.scoreMode(ScoreMode.SUM).boostMode(CombineFunction.SUM));
			var uniKeyword = DidxHelper.FormatKeywordSearchField(DidxHelper.FilterVietkey(keyword)).replace(' ', '_');

			queryBegin.must(queryStringQuery(uniKeyword).field("CategoryName_EN"));
			queryBegin.must(termQuery("IsActived", true));
			queryBegin.must(termQuery("IsDeleted", false));
			queryBegin.must(termsQuery("SiteID", new int[] { 0, siteID }));

//            query.must(boolQuery()//
//                    .should(termQuery("CategoryID", qry.CategoryId))//
//                    .should(termQuery("CategoryMap", qry.CategoryId))//
//                    .should(termQuery("GroupId", qry.CategoryId))//
//                    .should(termQuery("ParentIdList", qry.CategoryId))//
//            );

			sb.from(0).size(1);
			var searchRequest = new SearchRequest("ms_suggestsearch");
			searchRequest.source(sb.query(queryBegin));

			//var queryCateUni = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

			
			
			SearchResponse queryCateUni=null;
//			var elasticClient1 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST);
//				var clientIndex1 = elasticClient1.getClient();
//				try {

					queryCateUni = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

//				} catch (Throwable e) {
//
//					Logs.LogException(e);
//					throw e;
//				} finally {
//					elasticClient1.close();
//				}
//				
				
				
			var numQueryUni = (int) queryCateUni.getHits().getTotalHits().value;
			var dataQueryUni = new ArrayList<SuggestSearchSO>();
			queryCateUni.getHits().forEach(h -> {
				try {
					var so = esmapper.readValue(h.getSourceAsString(), SuggestSearchSO.class);
					dataQueryUni.add(so);
				} catch (Exception e) {
					Logs.LogException(e);
				}
			});
			query.must(termsQuery("SiteID", new int[] { 0, siteID }));
			if (numQueryUni > 0) {
				// trường hợp 1 - hiện 1 cate và 4 gợi ý hoặc cate + manu
				// query.must(termQuery("SuggestSearchID",
				// dataQueryUni.stream().findFirst().orElse(null).SuggestSearchID));
				sb = new SearchSourceBuilder();
				sb.from(0).size(1).aggregation(terms("suggestSearchType").field("SuggestSearchType").size(3)
						.subAggregation(topHits("topSuggestSearchTypeHits").size(50)));
				searchRequest = new SearchRequest("ms_suggestsearch");
				searchRequest.source(sb.query(query));
				
				SearchResponse queryResults=null;
//				var elasticClient2 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST);
//					var clientIndex2 = elasticClient2.getClient();
//					try {

						queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

//					} catch (Throwable e) {
//
//						Logs.LogException(e);
//						throw e;
//					} finally {
//						elasticClient2.close();
//					}
//				
				 

				var aggrs = queryResults.getAggregations();
				ParsedLongTerms bucket = (ParsedLongTerms) aggrs.get("suggestSearchType");
				if (bucket == null || bucket.getBuckets().size() == 0)
					return null;
				var lstSO = new ArrayList<SuggestSearchSO>();
				bucket.getBuckets().stream().map(v -> (TopHits) v.getAggregations().get("topSuggestSearchTypeHits"))
						// .collect(Collectors.toList())
						.forEach(h -> h.getHits().forEach(v -> {
							try {
								var so = esmapper.readValue(v.getSourceAsString(), SuggestSearchSO.class);

								// somap.put(so.ProductID, so);
								lstSO.add(so);
							} catch (IOException e) {
								Logs.LogException(e);
							}
						}));

				if (lstSO == null || lstSO.size() == 0)
					return null;

				var listManuStock = new ArrayList<Integer>();
				try {
//						listManuStock.addAll( getManuStock(lstSO.stream().filter(x -> x.SuggestSearchType == 3)
//								.mapToInt(s -> s.ManufactureID).toArray(), siteID) );
				} catch (Throwable e) {
					// TODO: handle exception
				}
				// xử lý lấy NN - lấy 1 cate thôi =))
				var tmpNN = lstSO.stream().filter(
						x -> x.SuggestSearchID == dataQueryUni.stream().findFirst().orElse(null).SuggestSearchID)
						.filter(x -> x.SuggestSearchType == SuggestSearchTypes.Category.getValue()).findFirst()
						.orElse(null);
				if (tmpNN != null) {
					result.add(tmpNN);
				}

				// xử lý lấy NN
				var tmpKS = lstSO.stream()
						.filter(x -> x.SuggestSearchType == SuggestSearchTypes.SuggestSearch.getValue()).limit(4)
						.collect(Collectors.toList());
				if (tmpKS != null && tmpKS.size() > 0) {
					result.addAll(tmpKS);
				}

				// xử lý lấy cate + NN
				if (result.size() < 5) {
					if (tmpNN != null) // lấy theo cate
					{
						var tmplstSO = lstSO.stream().filter(x -> x.SuggestSearchID == tmpNN.SuggestSearchID)
								.filter(x -> x.IsNotSuggestSearch == false)
								.filter(x -> x.SuggestSearchType == SuggestSearchTypes.CategoryManuFacture.getValue())
								.sorted(Comparator.<SuggestSearchSO>comparingInt(x -> x.DisplayOrder))
								.collect(Collectors.toList());

						var test = tmplstSO.stream().mapToInt(x -> x.ManufactureID).toArray();
						try {
							var stock = getManuStock(test, siteID);
							if (stock != null) {
								listManuStock.addAll(stock);
							}
						} catch (Throwable e) {
							e.printStackTrace();
						}
						if (tmplstSO != null && tmplstSO.size() > 0) {
							if (listManuStock != null && listManuStock.size() > 0) {

								var tmpresult = tmplstSO.stream().filter(x -> listManuStock.contains(x.ManufactureID))
										.limit(5 - result.size()).collect(Collectors.toList());

								result.addAll(tmpresult);
							}
						}
					} else {
						var tmplstSO = lstSO.stream().filter(x -> x.IsNotSuggestSearch == false)
								.filter(x -> x.SuggestSearchType == SuggestSearchTypes.CategoryManuFacture.getValue())
								.sorted(Comparator.<SuggestSearchSO>comparingInt(x -> x.DisplayOrder))
								.collect(Collectors.toList());

						if (tmplstSO != null && tmplstSO.size() > 0) {
							result.addAll(tmplstSO.stream().limit(5 - result.size()).collect(Collectors.toList()));
						}
					}
				}

			} else { // count query uni
				String strDomain = siteID == 1 ? "*thegioididong.com*" : "*dienmayxanh.com*";
				String strField = siteID == 1 ? "UrlTGDD" : "UrlDMX";
				query.must(boolQuery()
						.should(boolQuery().must(termQuery("SiteID", 0))
								.must(queryStringQuery(strDomain).field(strField)))
						.should(termQuery("SiteID", siteID)));

				sb = new SearchSourceBuilder();
				sb.from(0).size(0).aggregation(terms("suggestSearchType").field("SuggestSearchType").size(3)
						.subAggregation(topHits("topSuggestSearchTypeHits").size(50)));

				searchRequest.source(sb.query(query));
				
				SearchResponse queryResultsManu=null;
//				var elasticClient2 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST);
//				var clientIndex2 = elasticClient2.getClient();
//				try {

					  queryResultsManu = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
//				} catch (Throwable e) {
//
//					Logs.LogException(e);
//					throw e;
//				} finally {
//					elasticClient2.close();
//				}
				
				
			
				var lstSO = new ArrayList<SuggestSearchSO>();

				var aggrs = queryResultsManu.getAggregations();
				var bucket = (ParsedLongTerms) aggrs.get("suggestSearchType");
				if (bucket == null || bucket.getBuckets().size() == 0)
					return null;
				bucket.getBuckets().stream().map(v -> (TopHits) v.getAggregations().get("topSuggestSearchTypeHits"))
						.forEach(h -> h.getHits().forEach(v -> {
							try {
								var so = esmapper.readValue(v.getSourceAsString(), SuggestSearchSO.class);
								lstSO.add(so);
							} catch (IOException e) {
								Logs.LogException(e);
							}
						}));

				if (lstSO == null || lstSO.size() == 0)
					return null;

				result.addAll(lstSO.stream().filter(x -> x.SuggestSearchType == SuggestSearchTypes.Category.getValue())
						.limit(5 - result.size())
						// .sorted(Comparator.comparingInt(x -> x.DisplayOrder))
						.collect(Collectors.toList()));
				if (result.size() < 5) {
					result.addAll(lstSO.stream()
							.filter(x -> x.SuggestSearchType == SuggestSearchTypes.SuggestSearch.getValue())
							.limit(5 - result.size()).collect(Collectors.toList()));
				}

				if (result.size() < 5) {

					var listManuStock = new ArrayList<Integer>();

					IntStream lstStock = lstSO.stream()
							.filter(x -> x.SuggestSearchType == SuggestSearchTypes.CategoryManuFacture.getValue())
							.mapToInt(s -> s.ManufactureID);

//                    	if(lstStock != null) {
//                    		int[] arrStock =  lstStock.toArray();
//                    		if(arrStock.length > 0) {
//                    			List<Integer> lstManuStock = getManuStock(arrStock, siteID);
//                    			if(lstManuStock != null)
//                    				listManuStock.addAll(lstManuStock);
//                    		}
//                    	}

					List<Integer> lstManuStock = getManuStock(lstSO.stream()
							.filter(x -> x.SuggestSearchType == SuggestSearchTypes.CategoryManuFacture.getValue())
							.mapToInt(s -> s.ManufactureID).toArray(), siteID);

					if (lstManuStock != null)
						listManuStock.addAll(lstManuStock);
//                        listManuStock.addAll(getManuStock(lstSO.stream().filter(x -> x.SuggestSearchType == SuggestSearchTypes.CategoryManuFacture.getValue())
//                                .mapToInt(s -> s.ManufactureID).toArray(), siteID));

//                        var ccc = lstSO.stream().filter(x -> x.SuggestSearchType == SuggestSearchTypes.CategoryManuFacture.getValue())
//                                .filter(x -> listManuStock.contains(x.ManufactureID))
//                                .sorted(Comparator.comparingInt(x -> x.DisplayOrder))
//                                .collect(Collectors.toList());

					result.addAll(lstSO.stream()
							.filter(x -> x.SuggestSearchType == SuggestSearchTypes.CategoryManuFacture.getValue())
							.filter(x -> listManuStock.contains(x.ManufactureID))
							// .sorted(Comparator.<SuggestSearchSO>comparingInt(x -> x.DisplayOrder))
							.limit(5 - result.size()).collect(Collectors.toList()));
				}

			}

			return result.toArray(SuggestSearchSO[]::new);

		} // end if

		return null;
	}

	public List<Integer> getManuStock(int[] litsManu, int siteID) throws Throwable {

		// loc cate nghành hàng nào ko kinh doanh
		var queryProduct = boolQuery();
		queryProduct.must(termsQuery("Prices.WebStatusId_3", new int[] { 4, 3 }));
		queryProduct.must(termQuery("SiteID", siteID));
		queryProduct.must(termsQuery("ManufactureID", litsManu));

		var sb = new SearchSourceBuilder();
		sb.from(0).size(0).aggregation(terms("manufactureID").field("ManufactureID").size(40)
				.subAggregation(topHits("topManufactureIDHits").size(1)));
		var searchRequest = new SearchRequest("ms_product");
		searchRequest.source(sb.query(queryProduct));

		SearchResponse queryResultsManu=null;
//		var elasticClient1 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST);
//		var clientIndex1 = elasticClient1.getClient();
//		try {

			  queryResultsManu = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
//		} catch (Throwable e) {
//
//			Logs.LogException(e);
//			throw e;
//		} finally {
//			elasticClient1.close();
//		}
		
		
	
		var listManuStock = new ArrayList<Integer>();

		var aggrs = queryResultsManu.getAggregations();
		var bucket = (ParsedLongTerms) aggrs.get("manufactureID");
		if (bucket == null || bucket.getBuckets().size() == 0)
			return null;
		bucket.getBuckets().stream().map(v -> (TopHits) v.getAggregations().get("topManufactureIDHits"))
				.forEach(h -> h.getHits().forEach(v -> {
					try {
						var so = esmapper.readValue(v.getSourceAsString(), ProductSO.class);
						// somap.put(so.ProductID, so);
						listManuStock.add(so.ManufactureID);
					} catch (IOException e) {
						Logs.LogException(e);
					}
				}));

		return listManuStock;
	}

	@Override
	public String getDisadvantage(ProductDetailBO[] detail, int siteID) throws JsonProcessingException {
		return productHelper.getHelperBySite(1).getDisadvantage(detail, siteID);
	}
}
