package mwg.wb.business.helper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import mwg.wb.business.CacheStaticHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.SpecialsaleProgram.Pm_ProductBO;
import mwg.wb.client.elasticsearch.dataquery.ProductQuery;
import mwg.wb.client.service.CodeTimers;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.PriceParamsBO;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductDetailBO;
import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.model.products.ProductStickerLabelBO;
import mwg.wb.model.products.PromotionListGroupErp;
import mwg.wb.model.promotion.Promotion;
import mwg.wb.model.search.ProductSO;
import mwg.wb.model.searchresult.ProductSOSR;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder.ScriptSortType;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;
import static org.elasticsearch.search.aggregations.AggregationBuilders.topHits;
import static org.elasticsearch.search.sort.SortBuilders.scriptSort;

public class TGDDProductHelper extends ISiteProductHelper {

	private NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
	private ObjectMapper esmapper = DidxHelper.generateJsonMapper(GConfig.DateFormatStringNews);

	public TGDDProductHelper(ClientConfig aconfig, RestHighLevelClient _clientIndex, ObjectMapper _mapper,
			ProductHelper productHelper) {
		super(aconfig, _clientIndex, _mapper, productHelper);
		PromotionTypeAtStore = Stream.of(18, 19).collect(Collectors.toList());
	}

	private List<Integer> PromotionTypeAtStore;

	@Override
	public AggregationBuilder addAggregation() {
		return AggregationBuilders.nested("FacetPropIdList", "listproperty")
				.subAggregation(AggregationBuilders.terms("name").field("listproperty.propkey").size(1000));
	}

	public PromotionListGroupErp[] GetPromotionListGroupByPromoID(ErpHelper erpHelper, Integer PromotionID)
			throws Throwable {

		String key = "GetPromotionListGroupByPromoID" + PromotionID;
		var rs = (PromotionListGroupErp[]) CacheStaticHelper.GetFromCache(key, 30);
		if (rs == null) {
			rs = erpHelper.GetPromotionListGroupByPromoID(PromotionID, 1);
			CacheStaticHelper.AddToCache(key, rs);

		}
//		var rsa = (PromotionListGroupErp[]) 
		CacheStaticHelper.GetFromCache(key, 30);

		return rs;

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
		double returnvalue2 = 0;
		if (tmp != null && tmp.length > 0) {
			for (var value : tmp) {
				if(value != null) {
					returnvalue2 += Utils.toDouble(value);
				}
			}
		}
		item.ReturnValue2 = returnvalue2;

		item.IsPercentDiscountDisplay = item.IsPercentDiscountNotWeb == 1 ? true : false;
		item.DiscountValueDisplay = item.IsGiftPrdDiscount == 1 ? item.DiscountValueNotWeb : 0;
		item.QuantityLists = item.QuantityList;
		item.ReturnValues = item.ReturnValue;
		item.FromValue = item.MINTOTALPROMOTIONOFFERMONEY;
		item.ToValue = item.MAXTOTALPROMOTIONOFFERMONEY;
		item.ProductIds = item.ProductIDref;
		item.Quantities = item.QuantityList;
		item.QuantityList = "";
		item.IsOnline = item.ISONLINEOUTPUTTYPE == 1;
		item.NotApplyForInstallment = item.IsNotApplyForInterestRate;
		item.newCreatedPercentList = item.createdPercentList;

		var objtmp = listTempPromotion.stream().filter(p -> p.PromotionID == item.PromotionID).findFirst().orElse(null);

//		if (item.GroupID.equals("BanKem") && objtmp == null && InventoryStatusID == 1 && productID > 0) {
//
//			PromotionListGroupErp[] dataDetail = GetPromotionListGroupByPromoID(erpHelper, item.PromotionID);// erpHelper.GetPromotionListGroupByPromoID(item.PromotionID);
//
//			if (dataDetail != null && dataDetail.length > 0) {
//
//				List<Integer> listIDAcc = new ArrayList<Integer>();
//
//				if (isTele) {
//
//					// lay ds pk ban kem
//					listIDAcc = productHelper.GetListProductBanKem(productBO.ProductID);
//
//				}
//				if (isTele && listIDAcc.size() <= 0) {// khong xu ly
//
//				} else {
//					String productidref = "";
//					String percent = "";
//					String promotionPrice = "";
//					String quantities = "";
//
//					for (var sp : dataDetail) {
//
//						if (!listIDAcc.contains(sp.ProductIDRef) && isTele)
//							continue;
//
//						productidref += "|" + sp.ProductIDRef;
//						percent += "|" + sp.IsPercentDiscount;
//						promotionPrice += "|" + (long) sp.PromotionPrice;
//						quantities += "|" + sp.Quantity;
//					}
//					item.ProductIds = StringUtils.strip(productidref, "|");
//					item.ISPERCENTDISCOUNTSALELIST = StringUtils.strip(percent, "|");
//					item.PROMOTIONPRICELIST = StringUtils.strip(promotionPrice, "|");
//					item.Quantities = StringUtils.strip(quantities, "|");
//
//				}
//
//			}
//
//		}
//
//		if (item.GroupID.equals("BanKem") && objtmp != null && InventoryStatusID == 1) {
//			item.ProductIds = objtmp.ProductIds;
//			item.ISPERCENTDISCOUNTSALELIST = objtmp.ISPERCENTDISCOUNTSALELIST;
//			item.PROMOTIONPRICELIST = objtmp.PROMOTIONPRICELIST;
//			item.Quantities = objtmp.Quantities;
//		}

	}

	@Override
	public void processPromotion2(Promotion item, ProductBO productBO, int siteid, String Lang, int InventoryStatusID,
			ErpHelper erpHelper, List<Promotion> listTempPromotion) throws Throwable {
		processPromotion(item);
	}

	public void processPromotion2bankem(Promotion item, ProductBO productBO, int siteid, String Lang,
			int InventoryStatusID, ErpHelper erpHelper, List<Promotion> listTempPromotion) throws Throwable {// xu ly
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

//		if (!item.v2 && item.GroupID.equals("BanKem") && objtmp == null && InventoryStatusID == 1) {
//
//			var dataDetail = GetPromotionListGroupByPromoID(erpHelper, item.PromotionID);// erpHelper.GetPromotionListGroupByPromoID(item.PromotionID);
//
//			if (dataDetail != null && dataDetail.length > 0) {
//
//				List<Integer> listIDAcc = new ArrayList<Integer>();
//
//				if (isTele) {
//
//					// lay ds pk ban kem
//					listIDAcc = productHelper.GetListProductBanKem(productBO.ProductID);
//
//				}
//				if (isTele && listIDAcc.size() <= 0) {// khong xu ly
//
//				} else {
//					String productidref = "";
//					String percent = "";
//					String promotionPrice = "";
//					String quantities = "";
////					String stridlist = "";
////					Map<Integer, Integer> listCate = new HashMap<Integer, Integer>();
////
////					for (var sp : dataDetail) {
////
////						if (!listIDAcc.contains(sp.ProductIDRef) && isTele)
////							continue;
////
////						if (item.CategoryID <= 0) {
////							listCate.put(sp.ProductIDRef, 0);
////							stridlist = stridlist + ",";
////
////						} else {
////							listCate.put(sp.ProductIDRef, item.CategoryID);
////						}
////					}
////					if (!Utils.StringIsEmpty(stridlist)) {
////
////						Map<Integer, Integer> listCate2 = productHelper
////								.GetCategoryIDByListProductID("[" + StringUtils.strip(stridlist, ",") + "]");
////						for (Integer pid : listCate2.keySet()) {
////							listCate.put(pid, listCate2.get(pid));
////						}
////					}
//
//					for (var sp : dataDetail) {
//						int categoryID = 0;
//						if (item.CategoryID <= 0 && (categoryID = productHelper.getCategoryID(sp.ProductIDRef)) > 0) {
//							item.CategoryID = categoryID;
//						}
//						if (!listIDAcc.contains(sp.ProductIDRef) && isTele && item.CategoryID != 7264)
//							continue;
//						productidref += "|" + sp.ProductIDRef;
//						percent += "|" + sp.IsPercentDiscount;
//						promotionPrice += "|" + (long) sp.PromotionPrice;
//						quantities += "|" + sp.Quantity;
//
//					}
//					item.ProductIds = StringUtils.strip(productidref, "|");
//					item.ISPERCENTDISCOUNTSALELIST = StringUtils.strip(percent, "|");
//					item.PROMOTIONPRICELIST = StringUtils.strip(promotionPrice, "|");
//					item.Quantities = StringUtils.strip(quantities, "|");
//
//				}
//
//			}
//
//		}
//
//		if (item.GroupID.equals("BanKem") && objtmp != null && InventoryStatusID == 1) {
//			item.ProductIds = objtmp.ProductIds;
//			item.ISPERCENTDISCOUNTSALELIST = objtmp.ISPERCENTDISCOUNTSALELIST;
//			item.PROMOTIONPRICELIST = objtmp.PROMOTIONPRICELIST;
//			item.Quantities = objtmp.Quantities;
//		}

	}

	@Override
	public void getPromotions(ProductBO product, ProductErpPriceBO def, int siteID) {
		if (product.Promotion == null || product.Promotion.size() == 0)
			return;
		double price = def == null ? 0 : def.Price;

		var counter = product.Promotion.stream()
				.collect(Collectors.groupingBy(x -> x.GroupID, Collectors.counting()));

		Map<Integer, Double> discountMap = new HashMap<>();

		var iter = product.Promotion.iterator();
		while(iter.hasNext()) {
			var x = iter.next();
			// bo cac field lien quan den ban kem
			// anh Nam yeu cau 20201228
			if (x.GroupID.equalsIgnoreCase("bankem")) {
				x.ISPERCENTDISCOUNTSALELIST = "";
				x.PROMOTIONPRICELIST = "";
				x.ProductIds = "";
				x.Quantities = "";
			}
			var groupid = x.GroupID;
			var count = counter.getOrDefault(groupid, 0L);
			x.kmChon = count > 1 && x.GroupID.matches("[0-9]+-[0-9]+");

			// gắn groupid = tặng cho site tgdđ
			if (siteID == 1 && !groupid.equalsIgnoreCase("tặng")
					&& !groupid.equalsIgnoreCase("webnote")
					&& !groupid.equalsIgnoreCase("muakem")
					&& !groupid.equalsIgnoreCase("bankem")
					&& !x.kmChon) {
				x.GroupID = "Tặng";
			}

			x.discountValue(price);
			if (x.kmChon) {
				// Loại KM chọn có  IsGiftPrdDiscount=1
				// A Lâm yêu cầu 20210323
				if ((x.DiscountValue > 0 && x.IsGiftPrdDiscount == 1) || x.ProductName == null) {
					iter.remove();
					continue;
				}
				if (x.ProductId != null && x.ProductId.equalsIgnoreCase("discount") && x.ToValue > 0) {
					discountMap.put(x.PromotionID, x.ToValue);
				}
			}

			// anh Định yêu cầu 20210429
			if(!Strings.isNullOrEmpty(x.PARTNERINSTALLMENTIDLIST)) {
				x.PARTNERINSTALLMENTIDLIST = Arrays.stream(x.PARTNERINSTALLMENTIDLIST.split("\\|")).distinct()
						.collect(Collectors.joining(","));
			}
		}

//		đi thêm 1 vòng nữa để set tovalue cho km chọn
		for (var x : product.Promotion) {
			if (x.kmChon && discountMap.containsKey(x.PromotionID) && (x.ToValue == 0 || x.ProductId == null
					|| !x.ProductId.equalsIgnoreCase("discount"))) {
				x.ToValue = discountMap.get(x.PromotionID);
			}
		}

		product.PromotionAtStore = product.Promotion.stream()
				.filter(x -> !x.GroupID.equalsIgnoreCase("bankem") && PromotionTypeAtStore.contains(x.PromotionType)
						&& x.PromotionType != 2 && x.ISONLYFORSPECIALSALEPROGRAM == 0 && x.SpecialOutputTypeCount <= 0
						&& x.SpecialOutputTypeCount != x.PromotionOutputTypeCount && x.IsApplyByTimes == 0
						&& x.IsCheckSalePrice == 0
						&& !(x.SavingProgramOutputCount == x.PromotionOutputTypeCount && x.SavingProgramOutputCount > 0)
						// vh 04/12/2020
						&& !(x.SPECIALOUTPUTTYECOUNT > 0 && x.SPECIALOUTPUTTYECOUNT == x.PromotionOutputTypeCount))
				.collect(Collectors.toList());
		product.PromotionInstallment = product.Promotion.stream()
				.filter(x -> !x.GroupID.equalsIgnoreCase("bankem") && !PromotionTypeAtStore.contains(x.PromotionType)
						&& x.PromotionType != 2 && x.ISONLYFORSPECIALSALEPROGRAM == 1 && x.SpecialOutputTypeCount <= 0
						&& x.SpecialOutputTypeCount != x.PromotionOutputTypeCount && x.IsApplyByTimes == 0
						&& x.IsCheckSalePrice == 0
						&& !(x.SavingProgramOutputCount == x.PromotionOutputTypeCount && x.SavingProgramOutputCount > 0)
						// vh 04/12/2020
						&& !(x.SPECIALOUTPUTTYECOUNT > 0 && x.SPECIALOUTPUTTYECOUNT == x.PromotionOutputTypeCount))
				.collect(Collectors.toList());
		product.PromotionShockPrice = product.Promotion.stream()
				.filter(x -> !PromotionTypeAtStore.contains(x.PromotionType) && x.PromotionType != 2
						&& x.ISONLYFORSPECIALSALEPROGRAM == 0 && x.SpecialOutputTypeCount > 0
						&& x.GroupID.equalsIgnoreCase("bankem") == false && x.IsApplyByTimes == 0
						&& x.IsCheckSalePrice == 0
						&& !(x.SavingProgramOutputCount == x.PromotionOutputTypeCount && x.SavingProgramOutputCount > 0)
						// vh 04/12/2020
						&& !(x.SPECIALOUTPUTTYECOUNT > 0 && x.SPECIALOUTPUTTYECOUNT == x.PromotionOutputTypeCount))
				.collect(Collectors.toList());
		product.PromotionByTimes = product.Promotion.stream()
				.filter(x -> !x.GroupID.equalsIgnoreCase("bankem") && !PromotionTypeAtStore.contains(x.PromotionType)
						&& x.PromotionType != 2 && x.ISONLYFORSPECIALSALEPROGRAM == 0
						&& (x.SpecialOutputTypeCount <= 0 || x.SpecialOutputTypeCount != x.PromotionOutputTypeCount)
						&& x.IsApplyByTimes == 1 && x.IsCheckSalePrice == 0
						// vh 04/12/2020
						&& !(x.SPECIALOUTPUTTYECOUNT > 0 && x.SPECIALOUTPUTTYECOUNT == x.PromotionOutputTypeCount))
				.collect(Collectors.toList());
		product.PromotionCheckSalePrice = product.Promotion.stream()
				.filter(x -> !x.GroupID.equalsIgnoreCase("bankem") && !PromotionTypeAtStore.contains(x.PromotionType)
						&& x.PromotionType != 2 && x.ISONLYFORSPECIALSALEPROGRAM == 0
						&& (x.SpecialOutputTypeCount <= 0 || x.SpecialOutputTypeCount != x.PromotionOutputTypeCount)
						&& x.IsApplyByTimes == 0 && x.IsCheckSalePrice == 1
						&& !(x.SavingProgramOutputCount == x.PromotionOutputTypeCount && x.SavingProgramOutputCount > 0)
						// vh 04/12/2020
						&& !(x.SPECIALOUTPUTTYECOUNT > 0 && x.SPECIALOUTPUTTYECOUNT == x.PromotionOutputTypeCount))
				.collect(Collectors.toList());
		product.PromotionSaving = product.Promotion.stream()
				.filter(x -> !x.GroupID.equalsIgnoreCase("bankem") && !PromotionTypeAtStore.contains(x.PromotionType)
						&& x.PromotionType != 2 && x.ISONLYFORSPECIALSALEPROGRAM == 0
						&& (x.SpecialOutputTypeCount <= 0 || x.SpecialOutputTypeCount != x.PromotionOutputTypeCount)
						&& x.IsApplyByTimes == 0 && x.IsCheckSalePrice == 0
						&& x.SavingProgramOutputCount == x.PromotionOutputTypeCount && x.SavingProgramOutputCount > 0
						// vh 04/12/2020
						&& !(x.SPECIALOUTPUTTYECOUNT > 0 && x.SPECIALOUTPUTTYECOUNT == x.PromotionOutputTypeCount))
//				.map(x -> {
////					x = x.clone();
////					x.ProductName = x.PromotionListGroupName;
//					// bo sung 1/8/2020
//					String formatted = nf.format(x.DiscountValue);
//					if (Strings.isNullOrEmpty(x.ProductName) && x.DiscountValue > 0) {
//						String d = x.IsPercentDiscount ? (formatted + "%") : (formatted + "₫");
//						x.ProductName = "Giảm " + d;
//					}
//					return x;
//				})
				.collect(Collectors.toList());
		// 20201201
		// https://docs.google.com/document/d/1WYL2x8nGlrF8ItqDXTMquZoDybxuyif0Eo7R9N9Sw0E/edit#
		product.PromotionTotalValue = product.Promotion.stream()
				.filter(x -> !x.GroupID.equalsIgnoreCase("bankem") && x.PromotionType == 2 && x.IsPercentDiscount
				// vh 04/12/2020
						&& !(x.SPECIALOUTPUTTYECOUNT > 0 && x.SPECIALOUTPUTTYECOUNT == x.PromotionOutputTypeCount))
				.collect(Collectors.toList());

		// https://docs.google.com/document/d/1pYI6-9CZjC9V_Qt4DQNC72PtgrcpKAKIZHQd8UHwmKs/edit#
		// 04/12/2020
		product.PromotionOnline = product.Promotion.stream().filter(
				x -> x.SPECIALOUTPUTTYECOUNT > 0 && x.PromotionType == 1 && !x.GroupID.equalsIgnoreCase("bankem"))
				.collect(Collectors.toList());

		product.PromotionOnlineOnlyShockPrice = product.Promotion.stream()
				.filter(x -> {
					if (x.hasOneOfOutputType(2683, 2684)) {
						x.isExcludeFromMainPromotion = x.hasOnlyOneOfOutputType(2683, 2684);
						return true;
					}
					return false;
				})
				.collect(Collectors.toList());

		if (product.PromotionSaving != null && !product.PromotionSaving.isEmpty()) {
			product.PromotionSaving.addAll(product.Promotion.stream()
					.filter(x -> !product.PromotionSaving.contains(x) && x.SavingProgramOutputCount > 0
							&& x.PromotionType != 2)
//					.map(x -> {
////						x = x.clone();
//						// bo sung 1/8/2020
//						String formatted = nf.format(x.DiscountValue);
//						if (Strings.isNullOrEmpty(x.ProductName) && x.DiscountValue > 0) {
//							String d = x.IsPercentDiscount ? (formatted + "%") : (formatted + "₫");
//							x.ProductName = "Giảm " + d;
//						}
//						return x;
//					})
					.collect(Collectors.toList()));
			// PromotionSaving bỏ mặc định gán giảm giá xxx nếu như KM không được khai báo ProductName và vẫn trả KM
			// không có productName
			// Anh Lâm yêu cầu 20210323
			product.PromotionSaving = product.PromotionSaving.stream().map(x -> {
				x = x.clone();
				// bo sung 1/8/2020
				String formatted = nf.format(x.DiscountValue);
				if (Strings.isNullOrEmpty(x.ProductName) && x.DiscountValue > 0) {
					String d = x.IsPercentDiscount ? (formatted + "%") : (formatted + "₫");
					x.ProductName = "Giảm " + d;
				}
				return x;
			}).collect(Collectors.toList());
		}
		if (product.PromotionShockPrice != null && !product.PromotionShockPrice.isEmpty()) {
			product.PromotionShockPrice = product.PromotionShockPrice.stream().map(x -> {
				x = x.clone();
				if (x != null) {
					x.IsOnline = false;
				}
				return x;
			}).filter(x -> x != null).collect(Collectors.toList());
		}
		product.Promotion = product.Promotion.stream().filter(x -> !PromotionTypeAtStore.contains(x.PromotionType)
				&& x.ISONLYFORSPECIALSALEPROGRAM == 0
				&& (x.SpecialOutputTypeCount <= 0 || x.SpecialOutputTypeCount != x.PromotionOutputTypeCount)
				&& x.IsApplyByTimes == 0 && x.IsCheckSalePrice == 0
				&& !((x.SavingProgramOutputCount == x.PromotionOutputTypeCount) && (x.SavingProgramOutputCount > 0))
				// 20201201
				&& x.PromotionType != 2
				// vh 04/12/2020
				&& !(x.SPECIALOUTPUTTYECOUNT > 0 && x.SPECIALOUTPUTTYECOUNT == x.PromotionOutputTypeCount)
				// loai bo promotion shockprice 20201218
				&& !(x.SpecialOutputTypeCount > 0 && !x.GroupID.equalsIgnoreCase("bankem")
						&& x.SpecialOutputTypeCount == x.PromotionOutputTypeCount)
				&& !x.isExcludeFromMainPromotion
				&& !x.hasOnlyOneOfOutputType(222, 1441, 2163, 2183, 2203, 2204, 2683, 2684)
				)
				.sorted(Comparator.comparingDouble(x -> -x.discountValueNonAssign(price))).collect(Collectors.toList());
//		product.Promotion.sort(Comparator.<Promotion>comparingDouble(x -> x.discountValue(price)).reversed());
	}

	@Override
	public ProductSOSR SearchProduct(ProductQuery qry, boolean isGetFacetManu, boolean isGetFacetCate,
									 boolean isGetFacetProp, CodeTimers timer) throws Throwable {
	    return productHelper.getHelperBySite(2).SearchProduct(qry, isGetFacetManu, isGetFacetCate, isGetFacetProp,
				timer);
	}

	@Override
	public void processDetail(ProductBO product, int siteID, int provinceID, Integer storeID, String lang,
			CodeTimers timer) throws Throwable {
		int productID = product.ProductID;
		Pm_ProductBO codeInfo = null;
		// cho nay lay dc từ graph product->pm_product, mất hết 40ms
		if (!Utils.StringIsEmpty(product.ProductCode)) {
			codeInfo = productHelper.GetPm_ProductFromCache(product.ProductCode);
		}
		// dong ho cap
		if (product.CategoryID == 7264) {
			var coupleList = productHelper.getListCoupleWatch(productID, siteID, provinceID, lang);
			if (coupleList != null && coupleList.length > 1) {
				product.IsCoupleWatch = true;
				product.ListCoupleWatchProductID = Stream.of(coupleList).mapToInt(x -> x.ProductID).toArray();
				product.ListCoupleWatchBO = coupleList;
			}
		}

		// advantage , thêm table cache_product_detail , update khi chạy productse
		timer.start("propdetail");
		var details = productHelper.getCachedProductDetail(productID, siteID, lang, true, 0, timer);
		timer.pause("propdetail");
		product.Advantage = null;
		int cateid = product.CategoryID;
		if (ProductHelper.advantageMap.containsKey(cateid)) {
			var compare = Stream.of(details.list)
					.filter(x -> x != null && ProductHelper.advantageMap.get(cateid).contains(x.PropertyID))
					.sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder)
							.thenComparingInt(x -> x.CompareValue))
					.collect(Collectors.toList());
			if (compare != null && compare.size() > 0) {
				try {
					product.Advantage = mapper.writeValueAsString(compare);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			}
		}

		// disadvantage
		var useSearch = Stream.of(details.list)
				.filter(x -> x != null && x.IsImportant && (x.IsSearch || (siteID == 2 && x.IsSearchDMX)))
				.collect(Collectors.toList());
		// test
//		var tmpResultPropValuex = useSearch.stream()
//				.sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder))
//				.collect(Collectors.groupingBy(x -> x.PropertyID, Collectors.toList())).values()
//				.stream().map(x -> x.get(0)).map(xx -> xx.PropertyID)
//				.toArray(Integer[]::new);
		// var ccx = tmpResultPropValuexx.toArray(ProductDetailBO[]::new);

		ProductDetailBO[] resultPropValue = null;
		if (useSearch != null && useSearch.size() > 0) {
			List<ProductDetailBO> tmp = null;
			var groupProperty = useSearch.stream()
					.collect(Collectors.groupingBy(x -> x.PropertyID, Collectors.toList())).values();
			if (groupProperty != null && groupProperty.size() > 0) {
				int totalProp = groupProperty.size();

				switch (totalProp) {
				case 1:
					resultPropValue = useSearch.stream()
							.sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder)
									.thenComparingInt(x -> x.ValueDisplayOrder))
							.limit(2).toArray(ProductDetailBO[]::new);
					break;
				case 2:
					resultPropValue = useSearch.stream()
							.sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder)
									.thenComparingInt(x -> x.ValueDisplayOrder))
							.collect(Collectors.groupingBy(x -> x.PropertyID, Collectors.toList())).values().stream()
							.map(x -> x.get(0)).toArray(ProductDetailBO[]::new);
					break;
				default:
					var a = 1;
					var tmpResultPropValue = useSearch.stream()
							.sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder))
							.collect(Collectors.groupingBy(x -> x.PropertyID, Collectors.toList())).values().stream()
							.map(x -> x.get(0)).map(xx -> xx.PropertyID).limit(2).toArray(Integer[]::new);
					// var propIds = Stream.of(tmpResultPropValue).limit(2).toArray(Integer[]::new);
					var tmpProps = useSearch.stream()
							.filter(x -> Arrays.asList(tmpResultPropValue).contains(x.PropertyID))
							.toArray(ProductDetailBO[]::new);

					resultPropValue = Stream.of(tmpProps)
							.sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder)
									.thenComparingInt(x -> x.ValueDisplayOrder))
							.collect(Collectors.groupingBy(x -> x.PropertyID, Collectors.toList())).values().stream()
							.map(x -> x.get(0)).toArray(ProductDetailBO[]::new);
					break;

				}
			}

//			if (product.IsCoupleWatch) {
//				tmp = useSearch.stream().sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder)
//						.thenComparingInt(x -> x.ValueDisplayOrder)).collect(Collectors.toList());
//			} else {
//				tmp = useSearch.stream().collect(Collectors.groupingBy(x -> x.PropertyID, Collectors.toList())).values()
//						.stream().filter(x -> x != null && x.size() > 0).map(x -> x.get(0))
//						.sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder)
//								.thenComparingInt(x -> x.ValueDisplayOrder))
//						.collect(Collectors.toList());
//			}
			if (resultPropValue != null && resultPropValue.length > 0) {
				for (var prop : resultPropValue) {
					prop.PropValue = prop.Value;
				}
				product.DisAdvantage = mapper.writeValueAsString(resultPropValue);
			}
		} else {
			var tmp = Stream.of(details.list).filter(x -> x != null && x.IsFeatureProp)
					.sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder)
							.thenComparingInt(x -> x.ValueDisplayOrder))
					.collect(Collectors.groupingBy(x -> x.PropertyID, Collectors.toList())).entrySet().stream()
					.map(x -> x.getValue()).findFirst().orElse(null);
			if (tmp != null) {
//				try {
				product.DisAdvantage = mapper.writeValueAsString(tmp);
//				} catch (JsonProcessingException e) {
//					e.printStackTrace();
//				}
			} else {
				product.DisAdvantage = "";
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
				product.ProductLanguageBO.PercentInstallment = installment.PercentInstallment;
			}
			product.IsPayment = installment.IsPayment == 1;
			product.Paymentfromdate = installment.FromDate;
			product.Paymenttodate = installment.ToDate;
			product.PercentInstallment = installment.PercentInstallment;
		}

		// capacity
		try {
			product.Capacity = Stream.of(details.list).filter(x -> x != null && x.isAddUp).map(x -> x.Value).findFirst().orElse("");
		} catch (NullPointerException e) {
			product.Capacity = "";
		}

		List<PriceParamsBO> lspriceparams = null;
		if (codeInfo != null) {
			lspriceparams = productHelper.GetPriceParams(codeInfo.maingroupid);
			if (lspriceparams != null) {
				var priceparams = lspriceparams.stream()
						.filter(x -> x != null && x.companyid == DidxHelper.getCompanyID(siteID)).findFirst()
						.orElse(null);

				if (priceparams != null) {
					product.Numbers = priceparams.roundlevel;
					product.RoundLevel = priceparams.roundlevel;
					product.RoundLevelValue = 0;
				}
			}
		}
		// lam tron gia
//		var priceparams = Stream
//				.of(productHelper.getOrientClient().QueryFunction("priceparameter_getByProductcode",
//						PriceParamsBO[].class, false, product.ProductCode))
//				.filter(x -> x != null
//						&& x.companyid == DidxHelper.getBrandBySite(siteID, siteID == 6 ? "km-KH" : "vi-VN"))
//				.findFirst().orElse(null);
//		if (priceparams != null) {
//			product.Numbers = priceparams.psychologicaladjustlevel > 0 ? priceparams.psychologicaladjustlevel
//					: priceparams.roundlevel;
//		}

		// sticker label
		int stickerProp = productHelper.getStickerLabelPropByCateID(product.CategoryID);;
		var icon = Stream.of(details.list).filter(x -> x != null && x.PropertyID == stickerProp)
				.collect(Collectors.toList());
		if (icon.size() > 0) {
			var iconFile = icon.stream().map(x -> x.Icon).collect(Collectors.joining("|"));
			if (product.CategoryID == 44) {
				product.SpecialFeature = iconFile;// cai nay cu, kh biet con dung khog?
			}
			var item = icon.stream().sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.CompareValue).reversed())
					.findFirst().orElse(null);
			if (item != null && item.Icon != null)
				product.StickerLabel = item.Icon + "|" + item.PropValueID;
		}

		// special sale program
		if (codeInfo != null && !Utils.StringIsEmpty(product.ProductCode)) {
			product.SpecialSaleProgram = productHelper.getSpecialSaleProgram2(codeInfo, product.ProductCode, 1);
		}

		// btu của máy lạnh
		if (product.CategoryID == 2002) {
			var btup = Stream.of(details.list).filter(x -> x != null && x.PropertyID == 8480).findFirst().orElse(null);
			if (btup != null) {
				product.BTU = btup.Value;
			}
		}

		// thuộc top bán chạy ngành hàng
		// anh Trùy yêu cầu 20210325
		var topList = productHelper.getTopSoldCountFromCache(product.CategoryID, siteID, provinceID, lang, 3);
		product.IsBestSelling = topList != null ? topList.contains(productID) : false;
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

		Map<String, Promotion[]> promoGroup = recordid == null || recordid.length == 0 ? new HashMap<>()
				: productHelper.getPromotionGroup(recordid);
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
						product.Numbers = priceparams.roundlevel;
						product.RoundLevel = priceparams.roundlevel;
						product.RoundLevelValue = 0;
					}
				}
			}

			// sticker label
			ProductStickerLabelBO sticker = product.productStickerLabelBO;
			if (sticker != null && sticker.icon != null) {
				product.StickerLabel = sticker.icon + "|" + sticker.valueid;
			}

			// gia du kien
			LocalDateTime after60Days = product.CreatedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
					.plusDays(60);
			boolean validCate = product.ProductCategoryBO != null && product.ProductCategoryBO.CategoryID == 2182;
			if (product.ExpectedPrice > 0 && product.CreatedDate != null
					&& (Date.from(after60Days.atZone(ZoneId.systemDefault()).toInstant()).after(new Date())
					|| validCate)) {
				if (product.ProductErpPriceBO == null || product.ProductErpPriceBO.WebStatusId == 1) {
					product.ProductErpPriceBO = new ProductErpPriceBO();
					product.ProductErpPriceBO.Price = product.ExpectedPrice;
					product.ProductErpPriceBO.IsShowHome = true;
					product.ProductErpPriceBO.IsWebShow = true;
					product.ProductErpPriceBO.WebStatusId = 9;
//					if ((Date.from(after60Days.atZone(ZoneId.systemDefault()).toInstant()).before(new Date()))
//							&& validCate) {
//						product.ProductErpPriceBO.Price = 0;
//						product.ProductErpPriceBO.WebStatusId = 1;
//					}
				}

			}
			if(product.ProductLanguageBO != null && product.ProductErpPriceBO != null && Utils.StringIsEmpty(product.ProductErpPriceBO.Image)){

				if(!Utils.StringIsEmpty(product.ProductLanguageBO.simage)){
					var img = product.ProductLanguageBO.simage;
					product.ProductLanguageBO.simageurl = productHelper.GenProductImageUrl(product.CategoryID, product.ProductID, img);
				}
				if(!Utils.StringIsEmpty(product.ProductLanguageBO.bimage)){
					var img = product.ProductLanguageBO.bimage;
					product.ProductLanguageBO.bimageurl = productHelper.GenProductImageUrl(product.CategoryID, product.ProductID, img);
				}
				if(!Utils.StringIsEmpty(product.ProductLanguageBO.mimage)){
					var img = product.ProductLanguageBO.mimage;
					product.ProductLanguageBO.mimageurl = productHelper.GenProductImageUrl(product.CategoryID, product.ProductID, img);
				}

			}


			// WEBSTATUS HARDCODE
			productHelper.hardCodedWebStatus(product, siteID, provinceID);
		}
	}

	@Override
	public void processPromotion(Promotion item) throws Throwable {
		var tmp = !Utils.StringIsEmpty(item.ReturnValue) ? item.ReturnValue.split("\\|") : null;
		double returnvalue2 = 0;
		if (tmp != null && tmp.length > 0) {
			for (var value : tmp) {
				if(value != null) {
						returnvalue2 += Utils.toDouble(value);
				}
			}
		}
		item.ReturnValue2 = returnvalue2;

		item.IsPercentDiscountDisplay = item.IsPercentDiscountNotWeb == 1;
		item.DiscountValueDisplay = item.IsGiftPrdDiscount == 1 ? item.DiscountValueNotWeb : 0;
		item.QuantityLists = item.QuantityList;
		item.ReturnValues = item.ReturnValue;
		item.FromValue = item.MINTOTALPROMOTIONOFFERMONEY;
		item.ToValue = item.MAXTOTALPROMOTIONOFFERMONEY;
		item.ProductIds = item.ProductIDref;
		item.Quantities = item.QuantityList;
		item.QuantityList = "";
		item.IsOnline = item.ISONLINEOUTPUTTYPE == 1;
		item.NotApplyForInstallment = item.IsNotApplyForInterestRate;
		item.newCreatedPercentList = item.createdPercentList;

		item.ExcludeInstallmentProgramID = item.SALEPROGRAMIDLIST;
		item.ApplyStartTime = item.StartTime;
		item.ApplyEndTime = item.EndTime;
		item.ProductApplyType = item.LoaiChon == null ? -1 : item.LoaiChon;
		item.LoaiChon = item.LoaiChon == null ? 0 : item.LoaiChon;
		item.SpecialOutputTypeCount = item.SPECIALOUTPUTTYECOUNT;
		item.ExcludePromotion = item.EXCLUDEPROMOTIONID;
		item.IsRequestImeiWeb = !Strings.isNullOrEmpty(item.ISREQUESTIMEI);
	}

	@Override
	public int[] GetHomePageProduct2020(int category, int pageSize, int provinceID) {
		SearchSourceBuilder sb = new SearchSourceBuilder();

		// filter.
		var day = new Date();
		var query = boolQuery();
		query.must(termQuery("ProductType", 1));// 1. MÁY MỚI 2. máy cũ
		query.must(termQuery("IsCollection", 0));
		if (category > 0) {
//			if (category == 44 || category == 522) {
//				query.must(boolQuery().should(termsQuery("CategoryID", new int[] { 44, 522 })));
//			} else
			query.must(termQuery("CategoryID", category));
			query.must(termQuery("IsShowHome", false));
		} else {
			query.must(termQuery("IsShowHome", true));

//			query.must(rangeQuery("ShowHomeStartDate").gte(day));
//			query.must(rangeQuery("ShowHomeEndDate").lte(day));

			query.must(rangeQuery("ShowHomeStartDate").lte(day));
			query.must(rangeQuery("ShowHomeEndDate").gte(day));
		}
		query.must(boolQuery().should(termsQuery("prices.WebStatusId_3", new int[] { 4, 3, 2 }))
				.should(termQuery("IsRepresentProduct", 1))); // .should(termQuery("Order", 40))
		SearchResponse response = null;
		if (category < 0) {
			sb.from(0).size(100).sort("ShowHomeDisplayOrder", SortOrder.ASC);
			var searchRequest = new SearchRequest(config.ELASTICSEARCH_PRODUCT_INDEX);
			searchRequest.source(sb.query(query));
			
//			var elasticClient1 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST); 
//			var clientIndex1 = elasticClient1.getClient();
 		try {
				response = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
 		} catch (Throwable e) { 
 			Logs.LogException(e);
//				e.printStackTrace();
 			} finally {
//				//elasticClient1.close();
 			}
			
			
			 
		 
		} else {
			sb.from(0).size(pageSize)
					.sort(scriptSort(new Script("doc['DisplayOrder'].value > 0 ? doc['DisplayOrder'].value : 9999"),
							ScriptSortType.NUMBER).order(SortOrder.ASC))
					.sort("ViewCount", SortOrder.DESC);
			var searchRequest = new SearchRequest(config.ELASTICSEARCH_PRODUCT_INDEX);
			searchRequest.source(sb.query(query));
			
			
//			var elasticClient1 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST); 
//			var clientIndex1 = elasticClient1.getClient();
 			try {
				response = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
				
			} catch (Throwable e) { 
				Logs.LogException(e);
				e.printStackTrace();
			} finally {
			//	elasticClient1.close();
			}
//			
			
//			try {
//				response = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}

		}
		List<Integer> soListIDProduct = new ArrayList<Integer>();
		var searchHit = response.getHits().getHits();
		for (SearchHit searchHit2 : searchHit) {
			ProductSO so;
			try {
				so = esmapper.readValue(searchHit2.getSourceAsString(), ProductSO.class);
				soListIDProduct.add(so.ProductID);
				// System.out.println("test productId" + so.ProductID);
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if ((int) response.getHits().getTotalHits().value == 0 || response.getHits().EMPTY == null)
			return null;
		return soListIDProduct.stream().mapToInt(x -> x).toArray();
	}

	@Override
	public int[] GetAccessoryHomePageProductNew(int manuID, int countPerCat, int provinceID) {
		var listCategory = Arrays.asList(new int[] { 57, 382, 58, 55, 54, 75, 86, 60, 3885, 1882, 7077, 7264 });
		// var tmpListCate = Arrays.asList(listCategory);
		SearchSourceBuilder sb = new SearchSourceBuilder();

		// filter.
		var day = new Date();
		var query = boolQuery();
		SearchResponse response = null;

		if (countPerCat < 0) {
			if (manuID > 0) {
				query.must(termQuery("ManufactureID", manuID));
			}
			query.must(termQuery("ProductType", 1));
			query.must(termQuery("IsCollection", 0));
			query.must(termsQuery("CategoryID", ProductHelper.AccessoryCategory));
			query.must(termsQuery("Prices.WebStatusId_" + provinceID, new int[] { 4, 3 }));
			query.must(termQuery("Prices.IsShowHome_" + provinceID, 1));

			query.must(rangeQuery("FeatureStartDate").lte(day));
			query.must(rangeQuery("FeatureExpireDate").gte(day));

			query.must(rangeQuery("Prices.Price_" + provinceID).gte(2));

			sb.from(0).size(-countPerCat).fetchSource(new String[] { "ProductID", "CategoryID" }, null)
					.sort("DisplayOrder", SortOrder.ASC).sort("IsFeature", SortOrder.DESC)
					.sort("Scenario", SortOrder.DESC).sort("Prices.Price_" + provinceID, SortOrder.ASC)
					.sort("DateCreated", SortOrder.DESC);

			var searchRequest = new SearchRequest(config.ELASTICSEARCH_PRODUCT_INDEX);

			searchRequest.source(sb.query(query));
			try {
				
				SearchResponse queryResults = null; 
//				var elasticClient1 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST); 
//				var clientIndex1 = elasticClient1.getClient();
//				try {
					  queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
//				} catch (Throwable e) { 
//					Logs.LogException(e);
//					e.printStackTrace();
//				} finally {
//					elasticClient1.close();
//				}
				
				
				

				var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
					try {
						return mapper.readValue(x.getSourceAsString(), ProductSO.class);
					} catch (IOException e1) {
						return null;
					}
				}).filter(x -> x != null).collect(Collectors.toList()); // .mapToInt(x -> x.ProductID).toArray();

				var tmp = idlist.stream()
						.sorted(Comparator.<ProductSO>comparingDouble(
								x -> listCategory.contains(x.CategoryID) ? 999 : listCategory.indexOf(x.CategoryID)))
						.collect(Collectors.toList());
				return tmp != null ? tmp.stream().mapToInt(x -> x.ProductID).toArray() : null;

			} catch (Throwable e) {
				e.printStackTrace();
				Logs.LogException(e);
			}

		} else {

			if (manuID > 0) {
				query.must(termQuery("ManufactureID", manuID));
			}
			query.must(termQuery("ProductType", 1));
			query.must(termQuery("IsCollection", 0));
			query.mustNot(termsQuery("CategoryID", new int[] { 85, 56, 1823, 7978, 5697 }));
			query.must(termsQuery("CategoryID", ProductHelper.AccessoryCategory));

			query.must(termsQuery("Prices.WebStatusId_" + provinceID, new int[] { 4, 3 }));
			query.must(termQuery("Prices.IsShowHome_" + provinceID, 1));

//			query.must(rangeQuery("ShowHomeStartDate").lte(day));
//			query.must(rangeQuery("ShowHomeEndDate").gte(day));

			query.must(rangeQuery("Prices.Price_" + provinceID).gte(2));

			sb.from(0).size(1).aggregation(terms("categoryID").field("CategoryID").size(10)
					.subAggregation(topHits("topCategoryHits")
							.fetchSource(new String[] { "ProductID", "CategoryID" }, null).size(countPerCat)
							.sort("DisplayOrder", SortOrder.ASC).sort("Scenario", SortOrder.DESC)
							.sort("Prices.Price_" + provinceID, SortOrder.ASC).sort("DateCreated", SortOrder.DESC)));
			;
			var searchRequest = new SearchRequest(config.ELASTICSEARCH_PRODUCT_INDEX);

			searchRequest.source(sb.query(query));
			try {
				

				
//				var elasticClient1 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST); 
//				var clientIndex1 = elasticClient1.getClient();
//				try {
					response = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

//				} catch (Throwable e) { 
//					Logs.LogException(e);
//					e.printStackTrace();
//				} finally {
//					elasticClient1.close();
//				}
				
				
			
				var aggrs = response.getAggregations();
				ParsedLongTerms bucket = (ParsedLongTerms) aggrs.get("categoryID");
				if (bucket == null || bucket.getBuckets().size() == 0)
					return null;
				var somap = new ArrayList<ProductSO>();
				bucket.getBuckets().stream().map(v -> (TopHits) v.getAggregations().get("topCategoryHits"))
						// .collect(Collectors.toList())
						.forEach(h -> h.getHits().forEach(v -> {
							try {
								var so = mapper.readValue(v.getSourceAsString(), ProductSO.class);
								somap.add(so);
							} catch (IOException e) {
								Logs.LogException(e);
							}
						}));

				var tmp = somap.stream()
						.sorted(Comparator.<ProductSO>comparingDouble(
								x -> listCategory.contains(x.CategoryID) ? 999 : listCategory.indexOf(x.CategoryID)))
						.collect(Collectors.toList());
				return tmp != null ? tmp.stream().mapToInt(x -> x.ProductID).toArray() : null;
				// return somap.stream().mapToInt(Integer::intValue).toArray();

			} catch (Throwable e) {
				e.printStackTrace();
				Logs.LogException(e);
			}

		}
		return null;

	}

	@Override
	public int[] GetAccessoryHomePageProductApple2021(int provinceID) {
		int[] listCategory = new int[] { 54, 55, 58, 60, 86, 1662, 1882, 2429 };
		int[] manufacture = new int[] { 2660, 2392, 2371, 2380, 5290, 8501, 1977, 5399 };

		SearchSourceBuilder sb = new SearchSourceBuilder();

		// filter.
		var day = new Date();
		var query = boolQuery();
		var query2 = boolQuery();
		SearchResponse response = null;
		query.must(termQuery("SiteID", 1));
		
		query.must(termQuery("ProductType", 1));
		query.must(termQuery("IsCollection", 0));

		query.must(termsQuery("ManufactureID", manufacture));
		query.must(termsQuery("Prices.WebStatusId_" + provinceID, new int[] { 4, 3 }));
		query.must(termQuery("Prices.IsShowHome_" + provinceID, 1));
		query.must(rangeQuery("Prices.Price_" + provinceID).gte(2));
		query2 = query;

		sb.from(0).size(5).fetchSource(new String[] { "ProductID" }, null);
//				.sort("DisplayOrder", SortOrder.ASC).sort("IsFeature", SortOrder.DESC).sort("Scenario", SortOrder.DESC)
//				.sort("prices.Price_" + provinceID, SortOrder.ASC).sort("DateCreated", SortOrder.DESC);

		var searchRequest = new SearchRequest(config.ELASTICSEARCH_PRODUCT_INDEX);

		searchRequest.source(sb.query(query));
		try {
			 
			SearchResponse queryResults=null;
//			var elasticClient1 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST);
//				var clientIndex1 = elasticClient1.getClient();
//				try {

					queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

					

//				} catch (Throwable e) {
//
//					Logs.LogException(e);
//					throw e;
//				} finally {
//					elasticClient1.close();
//				}
				
				
				
				
			var idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), ProductSO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).mapToInt(x -> x.ProductID).toArray();

			if (idlist.length > 0) {
				query2.mustNot(termsQuery("ProductID", idlist));
			}
			sb = new SearchSourceBuilder();
			sb.from(0).size(1).aggregation(terms("categoryID").field("CategoryID").size(20)
					.subAggregation(topHits("topCategoryHits").fetchSource(new String[] { "ProductID" }, null).size(1)
							.sort("DisplayOrder", SortOrder.ASC).sort("Scenario", SortOrder.DESC)
							.sort("Prices.Price_" + provinceID, SortOrder.ASC).sort("DateCreated", SortOrder.DESC)));
			searchRequest = new SearchRequest(config.ELASTICSEARCH_PRODUCT_INDEX);
			searchRequest.source(sb.query(query2));

			
//			var elasticClient2 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST); 
//			var clientIndex2 = elasticClient1.getClient();
			try {
				response = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

			} catch (Throwable e) { 
				Logs.LogException(e);
				e.printStackTrace();
			} finally {
				//elasticClient2.close();
			}
			
			
			
			
			var aggrs = response.getAggregations();
			ParsedLongTerms bucket = (ParsedLongTerms) aggrs.get("categoryID");
			if (bucket == null || bucket.getBuckets().size() == 0)
				return null;
			var somap = new ArrayList<Integer>();
			bucket.getBuckets().stream().map(v -> (TopHits) v.getAggregations().get("topCategoryHits"))
					// .collect(Collectors.toList())
					.forEach(h -> h.getHits().forEach(v -> {
						try {
							var so = mapper.readValue(v.getSourceAsString(), ProductSO.class);
							// somap.put(so.ProductID, so);
							somap.add(so.ProductID);
						} catch (IOException e) {
							Logs.LogException(e);
						}
					}));
			var list2 = somap.stream().mapToInt(Integer::intValue).toArray();
			return org.apache.commons.lang.ArrayUtils.addAll(idlist, list2);
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
		}

		return new int[0];
	}

	@Override
	public int[] GetGenuineAccessoryHomePage(String[] ListManuName, int provinceID) {
		SearchSourceBuilder sb = new SearchSourceBuilder();

		// filter.
		var day = new Date();
		var query = boolQuery();
		SearchResponse response = null;

		query.must(termQuery("SiteID", 1));
		
		query.must(termQuery("ProductType", 1));
		query.must(termQuery("IsCollection", 0));
		query.must(termsQuery("CategoryID", ProductHelper.AccessoryCategory));
		query.must(termsQuery("Prices.WebStatusId_" + provinceID, new int[] { 4, 3 }));
		query.must(termQuery("Prices.IsShowHome_" + provinceID, 1));

		query.must(rangeQuery("AccessoriesStartDate").lte(day));
		query.must(rangeQuery("AccessoriesEndDate").gte(day));

		query.must(rangeQuery("AccessoriesDisplayOrder").gte(1).lte(15));

		sb.from(0).size(15).fetchSource(new String[] { "ProductID", "CategoryID" }, null)
				.sort("AccessoriesDisplayOrder", SortOrder.ASC);

		var searchRequest = new SearchRequest(config.ELASTICSEARCH_PRODUCT_INDEX);

		searchRequest.source(sb.query(query));
		List<ProductSO> idlist = null;
		try {
			
			
			SearchResponse queryResults=null;
			
//			var elasticClient1 =  new ElasticClient( config.SERVER_ELASTICSEARCH_READ_HOST);
//				var clientIndex1 = elasticClient1.getClient();
//				try {

					  queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);

//
//				} catch (Throwable e) {
//
//					Logs.LogException(e);
//					throw e;
//				} finally {
//					elasticClient1.close();
//				}
//			
			
			
			idlist = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), ProductSO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).collect(Collectors.toList()); // .mapToInt(x -> x.ProductID).toArray();

			if (idlist != null && idlist.size() >= 5) {
				return idlist.stream().mapToInt(x -> x.ProductID).toArray();
			}

			query = boolQuery();
			query.must(termQuery("ProductType", 1));
			query.must(termQuery("IsCollection", 0));
			query.must(termsQuery("Prices.WebStatusId_" + provinceID, new int[] { 4, 3 }));
			if (ListManuName.length > 0) {
				var tmpListManu = new ArrayList<String>();
				for (String item : ListManuName) {
					var tmp = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordSearchField(item));
					tmpListManu.add(tmp);
				}
				query.must(termsQuery("ManufacturerName", tmpListManu.toArray()) );
			}
			var listcate = new int[] { 54, 55, 56, 57, 58, 75, 85, 86, 1363, 1622, 1882, 1902, 2162, 3885, 4547, 4727,
					4728, 6858, 6859, 6862, 6863, 7922, 7923, 7924, 7925 };
			query.must(boolQuery().should(boolQuery().must(termsQuery("CategoryID", listcate)))
					.should(boolQuery().must(termQuery("CategoryID", 60)).must(termQuery("PropStr", "prop14609_84641")))
					.should(boolQuery().must(termQuery("CategoryID", 1662))
							.must(termQuery("PropStr", "prop24738_168651"))));
			if (idlist != null && idlist.size() > 0) {
				query.mustNot(termsQuery("ProductID", idlist.stream().mapToInt(x -> x.ProductID).toArray()));
			}

			sb.from(0).size(5).fetchSource(new String[] { "ProductID", "CategoryID" }, null);

			searchRequest = new SearchRequest(config.ELASTICSEARCH_PRODUCT_INDEX);
			searchRequest.source(sb.query(query));
			
			 
			 

				queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
			 
			
			
			
			var tmp = Stream.of(queryResults.getHits().getHits()).map(x -> {
				try {
					return mapper.readValue(x.getSourceAsString(), ProductSO.class);
				} catch (IOException e1) {
					return null;
				}
			}).filter(x -> x != null).collect(Collectors.toList()); // 
			if(tmp != null && tmp.size() > 0) {
				idlist.addAll(tmp);
			}
			
			return idlist.stream().mapToInt(x->x.ProductID).toArray();
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
		}
		return null;
	}

	@Override
	public String getDisadvantage(ProductDetailBO[] detail, int siteID) throws JsonProcessingException {
		var useSearch = Stream.of(detail)
				.filter(x -> x != null && x.IsImportant && (x.IsSearch || (siteID == 2 && x.IsSearchDMX)))
				.collect(Collectors.toList());
		// test
//		var tmpResultPropValuex = useSearch.stream()
//				.sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder))
//				.collect(Collectors.groupingBy(x -> x.PropertyID, Collectors.toList())).values()
//				.stream().map(x -> x.get(0)).map(xx -> xx.PropertyID)
//				.toArray(Integer[]::new);
		// var ccx = tmpResultPropValuexx.toArray(ProductDetailBO[]::new);

		ProductDetailBO[] resultPropValue = null;
		if (useSearch != null && useSearch.size() > 0) {
			List<ProductDetailBO> tmp = null;
			var groupProperty = useSearch.stream()
					.collect(Collectors.groupingBy(x -> x.PropertyID, Collectors.toList())).values();
			if (groupProperty != null && groupProperty.size() > 0) {
				int totalProp = groupProperty.size();

				switch (totalProp) {
					case 1:
						resultPropValue = useSearch.stream()
								.sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder)
										.thenComparingInt(x -> x.ValueDisplayOrder))
								.limit(2).toArray(ProductDetailBO[]::new);
						break;
					case 2:
						resultPropValue = useSearch.stream()
								.sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder)
										.thenComparingInt(x -> x.ValueDisplayOrder))
								.collect(Collectors.groupingBy(x -> x.PropertyID, Collectors.toList())).values().stream()
								.map(x -> x.get(0)).toArray(ProductDetailBO[]::new);
						break;
					default:
						var a = 1;
						var tmpResultPropValue = useSearch.stream()
								.sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder))
								.collect(Collectors.groupingBy(x -> x.PropertyID, Collectors.toList())).values().stream()
								.map(x -> x.get(0)).map(xx -> xx.PropertyID).limit(2).toArray(Integer[]::new);
						// var propIds = Stream.of(tmpResultPropValue).limit(2).toArray(Integer[]::new);
						var tmpProps = useSearch.stream()
								.filter(x -> Arrays.asList(tmpResultPropValue).contains(x.PropertyID))
								.toArray(ProductDetailBO[]::new);

						resultPropValue = Stream.of(tmpProps)
								.sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder)
										.thenComparingInt(x -> x.ValueDisplayOrder))
								.collect(Collectors.groupingBy(x -> x.PropertyID, Collectors.toList())).values().stream()
								.map(x -> x.get(0)).toArray(ProductDetailBO[]::new);
						break;

				}
			}

//			if (product.IsCoupleWatch) {
//				tmp = useSearch.stream().sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder)
//						.thenComparingInt(x -> x.ValueDisplayOrder)).collect(Collectors.toList());
//			} else {
//				tmp = useSearch.stream().collect(Collectors.groupingBy(x -> x.PropertyID, Collectors.toList())).values()
//						.stream().filter(x -> x != null && x.size() > 0).map(x -> x.get(0))
//						.sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder)
//								.thenComparingInt(x -> x.ValueDisplayOrder))
//						.collect(Collectors.toList());
//			}
			if (resultPropValue != null && resultPropValue.length > 0) {
				for (var prop : resultPropValue) {
					
				if(prop!=null)	prop.PropValue = prop.Value;
				}
				return mapper.writeValueAsString(resultPropValue);
			}
		} else {
			var tmp = Arrays.stream(detail).filter(x -> x != null && x.IsFeatureProp)
					.sorted(Comparator.<ProductDetailBO>comparingInt(x -> x.PropertyDisplayOrder)
							.thenComparingInt(x -> x.ValueDisplayOrder))
					.collect(Collectors.groupingBy(x -> x.PropertyID, Collectors.toList())).entrySet().stream()
					.map(x -> x.getValue()).findFirst().orElse(null);
			if (tmp != null) {
//				try {
				return mapper.writeValueAsString(tmp);
//				} catch (JsonProcessingException e) {
//					e.printStackTrace();
//				}
			} else {
				return "";
			}
		}
		return null;
	}
}
