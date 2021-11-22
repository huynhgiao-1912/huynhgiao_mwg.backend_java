package mwg.wb.webapi.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.primitives.Ints;
import com.hazelcast.core.HazelcastInstance;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import mwg.wb.business.CategoryHelper;
import mwg.wb.business.CommonHelper;
import mwg.wb.business.InStockBO;
import mwg.wb.business.LogHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.helper.APIPriceHelper;
import mwg.wb.client.elasticsearch.dataquery.*;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.client.service.CodeTimers;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.LogLevel;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.api.ProductGalleryBOApi;
import mwg.wb.model.campagain.PreOrderProgramBO;
import mwg.wb.model.commonpackage.LabelCampaignBO;
import mwg.wb.model.commonpackage.LabelCampaignSO;
import mwg.wb.model.commonpackage.SuggestSearchSO;
import mwg.wb.model.general.DistrictBO;
import mwg.wb.model.general.ProvinceBO;
import mwg.wb.model.installment.InstallmentBO;
import mwg.wb.model.installment.InstallmentException;
import mwg.wb.model.other.BooleanWrapper;
import mwg.wb.model.pm.StockStore;
import mwg.wb.model.pm.StoreBO;
import mwg.wb.model.products.GroupCategoryBO;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductColorBO;
import mwg.wb.model.products.ProductDetailBO;
import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.model.products.ProductGallery360BO;
import mwg.wb.model.products.ProductGalleryBO;
import mwg.wb.model.products.ProductInstallment;
import mwg.wb.model.products.ProductManuBO;
import mwg.wb.model.products.ProductPropGrpBO;
import mwg.wb.model.products.ProductPropUseManualBO;
import mwg.wb.model.products.ProductSliderBO;
import mwg.wb.model.products.ProductVideoShowBO;
import mwg.wb.model.products.SpecTemplateBO;
import mwg.wb.model.products.SpecialSaleProgramBO;
import mwg.wb.model.products.StockBO;
import mwg.wb.model.products.TemplateRating;
import mwg.wb.model.products.URLGoogleBO;
import mwg.wb.model.promotion.CMSPromotion;
import mwg.wb.model.promotion.PromotionProductBanKemBO;
import mwg.wb.model.promotion.ShockPriceBO;
import mwg.wb.model.search.AccesoriesResult;
import mwg.wb.model.search.ProductPriceSO;
import mwg.wb.model.search.ProductSO;
import mwg.wb.model.searchresult.FaceManuSR;
import mwg.wb.model.searchresult.GallerySR;
import mwg.wb.model.searchresult.ProductBOSR;
import mwg.wb.model.searchresult.ProductSOSR;
import mwg.wb.model.seo.ProductUrl;
import mwg.wb.model.system.CachedDetails;
import mwg.wb.model.system.DeliveryTime;
import mwg.wb.webapi.requests.ProductListRequest;
import mwg.wb.webapi.service.ConfigUtils;
import mwg.wb.webapi.service.HeaderBuilder;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//@PropertySource("classpath:/bootstrap.properties")
@RestController
@Configuration
@RefreshScope
@RequestMapping("/apiproduct")
public class ProductApiController {

	@Autowired
	private HttpServletRequest request;
	private static ProductHelper _productHelper = null;
	private static CategoryHelper _categoryHelper = null;
	private static CommonHelper _commonHelper = new CommonHelper();
//	private static PriceHelper _pricetHelper = null;
	private static ORThreadLocal factoryRead = null;
//	private static ClientConfig _config = null;
//	int dc = 0;
	public static ClientConfig currentConfig = null;

	public static Optional<HttpServletRequest> getCurrentHttpRequest() {
		return Optional.ofNullable(RequestContextHolder.getRequestAttributes()).filter(
				requestAttributes -> ServletRequestAttributes.class.isAssignableFrom(requestAttributes.getClass()))
				.map(requestAttributes -> ((ServletRequestAttributes) requestAttributes))
				.map(ServletRequestAttributes::getRequest);
	}

	private static synchronized ProductHelper GetProductClient() {

		// 31, 32

		if (_productHelper == null) {

			// synchronized (ProductApiController.class) {
			// int DataCenterConfig = ConfigUtils.GetDataCenter();
			// int dataCenter = DataCenterHelper.GetDataCenter(DataCenterConfig);
			OGlobalConfiguration.NETWORK_BINARY_MAX_CONTENT_LENGTH.setValue(32768);
			ClientConfig config = ConfigUtils.GetOnlineClientConfig();
			currentConfig = config;
//			_config = config;
			try {
				factoryRead = new ORThreadLocal();
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			factoryRead.initReadAPI(config, 0);
			_productHelper = new ProductHelper(factoryRead, config);

			// }
		}
		// }
		return _productHelper;
	}

	private static synchronized CategoryHelper getCategoryHelper() {

		// 31, 32

		if (_categoryHelper == null) {

			// synchronized (ProductApiController.class) {
			// int DataCenterConfig = ConfigUtils.GetDataCenter();
			// int dataCenter = DataCenterHelper.GetDataCenter(DataCenterConfig);
			OGlobalConfiguration.NETWORK_BINARY_MAX_CONTENT_LENGTH.setValue(32768);
			ClientConfig config = ConfigUtils.GetOnlineClientConfig();
			currentConfig = config;
//			_config = config;
			try {
				factoryRead = new ORThreadLocal();
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			factoryRead.initReadAPI(config, 0);
			_categoryHelper = new CategoryHelper(factoryRead, config);

			// }
		}
		// }
		return _categoryHelper;
	}

//	private static synchronized CommonHelper GetCommonHelper() {
//		// synchronized (ProductApiController.class) {
//		if (_commonHelper == null)
//			_commonHelper = new CommonHelper();
//		// }
//		return _commonHelper;
//	}

//	private static synchronized PriceHelper GetPriceClient() {
//		if (_pricetHelper == null)
//			_pricetHelper = new PriceHelper();
//		return _pricetHelper;
//	}

	@RequestMapping(value = "/testfailrequest", method = RequestMethod.GET)
	public ResponseEntity<String> TestFaiLRequest() {
		var status = HttpStatus.OK;

		try {
			var productHelper = GetProductClient();
			productHelper.TestFaiLRequest();
		} catch (Throwable e) {

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}

		return new ResponseEntity<>("test", status);

	}

	@RequestMapping(value = "/getproducttestnewprice", method = RequestMethod.GET)
	public ResponseEntity<ProductBO> GetProductTest(int productID, int siteID, Integer storeID, Integer provinceID,
			String lang) {
		if (provinceID == null)
			provinceID = 3;
		if (storeID == null)
			storeID = 0;
		if (lang == null || lang == "")
			lang = "vi-VN";
//		var codetimer = new CodeTimer("timer-all");
//		var odbtimer = new CodeTimer("timer-odb");
		var timer = new CodeTimers();
		ProductBO product = null;
		var productHelper = GetProductClient();
		var status = HttpStatus.OK;
		try {
			product = productHelper.GetProduct_PriceStrings(productID, siteID, provinceID, storeID, lang, timer);
			// product = productHelper.GetProductFromCache(productID, siteID, provinceID,
			// storeID, lang, codetimer, odbtimer);

		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			product = new ProductBO() {
				{
					message = e.getClass().getName() + ": " + e.getMessage();
					stackTrace = Stream.of(e.getStackTrace()).map(x -> x.toString()).toArray(String[]::new);

				}
			};
			LogHelper.WriteLog(e);
		}

		var header = HeaderBuilder.buildHeaders(timer);
		return new ResponseEntity<>(product, header, status);

	}

	@RequestMapping(value = "/getproduct", method = RequestMethod.GET)
	// @HystrixCommand(fallbackMethod = "GetDefaultProduct")
	public ResponseEntity<ProductBO> GetProduct(int productID, int siteID, Integer storeID, Integer provinceID,
			String lang, String productCode) {
		if (provinceID == null)
			provinceID = 3;
		if (storeID == null)
			storeID = 0;
		if (lang == null || lang == "")
			lang = "vi-VN";
		var timer = new CodeTimers();


//		var codetimer = new CodeTimer("timer-all");
//		var odbtimer = new CodeTimer("timer-odb");
		timer.start("all");
		ProductBO product = null;
		var productHelper = GetProductClient();
		var status = HttpStatus.OK;
		if (!GConfig.ProductTaoLao.containsKey((long) productID)) {

			try {

//				String KEY_CACHED = "product_getproduct" + productID + "_" + siteID + "_" + provinceID + "_" + storeID + "_"
//						+ lang ;
//				if(!Utils.StringIsEmpty(productCode))
//					KEY_CACHED += "_" + productCode;
//
//				product = (ProductBO) CachedGraphDBHelper.GetFromCache(KEY_CACHED, 2);
//				if (product == null) {
					product = productHelper.GetProduct_PriceStrings(productID, siteID, provinceID, storeID, lang,
							timer,productCode);
			// CachedGraphDBHelper.AddToCache(KEY_CACHED, product);
				//}

//				 product = productHelper.GetProductFromCache(productID, siteID, provinceID,
//				 storeID, lang, codetimer, odbtimer);

			} catch (Throwable e) {
//				e.printStackTrace();
				e.printStackTrace();
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				product = new ProductBO() {
					{
						// message = e.getClass().getName() + ": " + e.getMessage();
						// stackTrace = Stream.of(e.getStackTrace()).map(x ->
						// x.toString()).toArray(String[]::new);

					}
				};
				product = new ProductBO();
				// (Utils.stackTraceToString(e));
				LogHelper.WriteLog(e, LogLevel.ERROR, request);
			}
		} else {

			product = new ProductBO() {
				{
					message = "Product rac";
				}
			};
		}
		timer.pause("all");

		var header = HeaderBuilder.buildHeaders(timer);
		return new ResponseEntity<ProductBO>(product, header, status);

	}

	@RequestMapping(value = "/getspecialsaleprogram", method = RequestMethod.GET)
	public ResponseEntity<SpecialSaleProgramBO> GetSpecialSaleProgram(String ProductCode) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		SpecialSaleProgramBO SpecialSaleProgram = null;
		var productHelper = GetProductClient();
		var status = HttpStatus.OK;
		try {
			SpecialSaleProgram = productHelper.getSpecialSaleProgram(ProductCode, 1);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);
		return new ResponseEntity<>(SpecialSaleProgram, header, status);
	}

	@RequestMapping(value = "/clearcache_specialsaleprogram", method = RequestMethod.GET)
	public ResponseEntity<String> ClearCacheSpecialSaleProgram(String table) {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		var productHelper = GetProductClient();
		var status = HttpStatus.OK;
		try {
			productHelper.ClearCacheSpecialSaleProgram(table);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);
		return new ResponseEntity<>("OK", header, status);

	}

	@RequestMapping(value = "/getinstallmentinfo", method = RequestMethod.GET)
	public ResponseEntity<ProductInstallment> GetInstallmentInfo(int productID, int siteID, String lang, int cateID) {

		if (Utils.StringIsEmpty(lang))
			lang = "vi-VN";
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		ProductInstallment productInstallment = null;
		var productHelper = GetProductClient();
		var status = HttpStatus.OK;
		try {
			productInstallment = productHelper.GetInstallmentByProduct(productID, siteID, lang, Math.round(cateID));
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			LogHelper.WriteLog(e);
		}

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);
		return new ResponseEntity<>(productInstallment, header, status);

	}

	@RequestMapping(value = "/getproductbylistid", method = RequestMethod.POST)
	public ResponseEntity<ProductBO[]> GetProductByListID(@RequestBody ProductListRequest query) {
//		System.out.print(query.storeId);

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		var phelper = GetProductClient();
		var status = HttpStatus.OK;
		odbtimer.reset();
		ProductBO[] result = null;
		try {

			result = phelper.GetSimpleProductListByListID_PriceStrings_soMap(query.listProductID, query.siteID,
					query.provinceID, query.lang);

//			result = phelper.GetProductBOByListID_PriceStrings(query.listProductID, query.siteID, query.provinceID,
//					query.lang);

		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();

			var param = phelper.GetJsonFromObject(query);
			LogHelper.WriteLog(e, LogLevel.ERROR, param);
		}
		odbtimer.end();
		// Lấy giá từ Elastic
		var accessories = phelper.getAccessoryCategory();
		for (ProductBO product : result) {
//			phelper.GetDefaultPriceForList(product, isAccessory, true);

			if (query.siteID == 11) {

				try {
					phelper.GetDefaultPriceAndPromotionBHX(query.lang, product, false, query.storeId, query.provinceID);
				} catch (Throwable e) {
					status = HttpStatus.INTERNAL_SERVER_ERROR;

					var param = phelper.GetJsonFromObject(query);
					LogHelper.WriteLog(e, LogLevel.ERROR, param);
				}
			} else {
//				var isAccessory = accessories.contains((int) product.CategoryID);
//				try {
//					phelper.GetDefaultPriceAndPromotion(query.siteID, query.lang, product, isAccessory, false, null);
//				} catch (Throwable e) {
//					status = HttpStatus.INTERNAL_SERVER_ERROR;
//
//					var param = phelper.GetJsonFromObject(query);
//					LogHelper.WriteLog(e, LogLevel.ERROR, param);
//				}
			}
		}
		codetimer.end();
		return new ResponseEntity<ProductBO[]>(result, HeaderBuilder.buildHeaders(codetimer, odbtimer), status);
	}

	@RequestMapping(value = "/getproductbyrepresentid", method = RequestMethod.GET)
	public ResponseEntity<ProductBO[]> GetProductByRepresentID(int representID, int provinceID, int siteID,
			String langID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		var status = HttpStatus.OK;
		var phelper = GetProductClient();

		int[] idlist = null;
		ProductBO[] result = null;
		try {
			// get danh sach san pham dai dien
			idlist = phelper.GetListRepresentProduct(representID, provinceID, siteID, langID);
			if (idlist != null && idlist.length > 0) {
				result = phelper.GetSimpleProductListByListID_PriceStrings_soMap(idlist, siteID, provinceID, langID);
				Arrays.sort(result, Comparator.<ProductBO>comparingInt(x -> x.FeaturePropertyCompareValue));
			}
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();
		return new ResponseEntity<ProductBO[]>(result, HeaderBuilder.buildHeaders(codetimer, odbtimer), status);
	}

//	@RequestMapping(value = "/getproductbylistid", method = RequestMethod.POST)
//	public ResponseEntity<ProductBO[]> GetProductByListIDtest(@RequestBody ProductListRequest query) {
//		var codetimer = new CodeTimer("timer-all");
//		var estimer = new CodeTimer("timer-es");
//		var odbtimer = new CodeTimer("timer-odb");
//		var phelper = GetProductClient();
// 
//		odbtimer.reset();
//		var result = phelper.GetProductBOByListID(query.listProductID, codearray, query.siteID, query.provinceID,
//				query.lang);
//		odbtimer.end();
//		// Lấy giá từ Elastic
//		var accessories = phelper.getAccessoryCategory();
//		for (ProductBO product : result) {
//			var isAccessory = accessories.contains((int) product.CategoryID);
//			var so = somap.get(product.ProductID);
//			phelper.GetDefaultPrice(product, so, query.provinceID, isAccessory);
//		}
//		codetimer.end();
//		return new ResponseEntity<ProductBO[]>(result, HeaderBuilder.buildHeaders(codetimer,   odbtimer),
//				HttpStatus.OK);
//	}

	@RequestMapping(value = "/getpricebyproductid", method = RequestMethod.GET)
	public ResponseEntity<List<ProductErpPriceBO>> GetPriceByProductID(int productID, int siteID, String langID,
			Integer storeID, Integer provinceID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		List<ProductErpPriceBO> price = null;
		ProductHelper helper;
		int priProv = provinceID == null ? -1 : provinceID;
		try {
			helper = GetProductClient();
			price = helper.getPriceHelper().getListPriceStringsWithQuantities(productID,
					DidxHelper.getPriceAreaBySiteID(siteID, langID), siteID, priProv, langID);
			for (var x : price) {
				var dummy = new ProductBO();
				dummy.ProductErpPriceBO = x;
				helper.hardCodedWebStatus(dummy, siteID, priProv);
			}
//			price = GetProductClient().GetPriceByProductID(productID, siteID, langID,
//					provinceID == null ? -1 : provinceID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			LogHelper.WriteLog(e);
		}
		if (siteID == 11 && storeID != null && storeID > 0) {
			price = price.stream().filter(x -> x.StoreID == storeID).collect(Collectors.toList()); // .toArray(v -> new
																									// ProductErpPriceBO[v]);
		}
		timer.end();
		return new ResponseEntity<>(price, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getcmspromotions", method = RequestMethod.GET)
	public ResponseEntity<List<CMSPromotion>> GetCMSPromotions(int categoryID, int manufactureID, int productID,
			int siteID) {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		var status = HttpStatus.OK;
		List<CMSPromotion> cmsPromotions = null;
		var productHelper = GetProductClient();

		try {
			cmsPromotions = productHelper.GetCMSPromotions(categoryID, manufactureID, productID, siteID, codetimer,
					odbtimer);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}

		if (cmsPromotions.size() > 0) {
			cmsPromotions = cmsPromotions.stream()
					.filter(r -> r.FromDate.before(new Date()) && new Date().before(r.ToDate))
					.sorted(Comparator.comparing(CMSPromotion::getToDate)).collect(Collectors.toList());
		}
		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);
		return new ResponseEntity<>(cmsPromotions, header, status);

	}

	@RequestMapping(value = "/getpricebyproductcode", method = RequestMethod.GET)
	public ResponseEntity<ProductErpPriceBO> GetPriceByProductCode(String productCode, int siteID, int provinceID,
			String langID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductErpPriceBO price = null;
		try {
			var list = GetProductClient().getPriceHelper().getListPriceStringsWithQuantities(productCode,
					DidxHelper.getDefaultPriceAreaBySiteID(siteID, langID), siteID, provinceID);
			price = list != null && list.size() > 0 ? list.get(0) : null;
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<ProductErpPriceBO>(price, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getcolorbyproductid", method = RequestMethod.GET)
	public ResponseEntity<ProductColorBO[]> GetColorByProductID(int productID, String langID,
	@RequestParam(name = "siteId", defaultValue = "2", required = true) int siteID,
	@RequestParam(name = "provinceID", defaultValue = "3", required = true) int provinceID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductColorBO[] colors = null;
		try {
			colors = GetProductClient().GetColorByProductID2(productID, langID, siteID, provinceID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<ProductColorBO[]>(colors, HeaderBuilder.buildHeaders(timer), status);
	}

	private static final List<Integer> arrCategoryGetCenterQuantity = Arrays.asList(4706, 8121, 8120, 8119, 7720, 4697,
			5612, 5475, 166, 3385, 4645, 2202, 1942, 1943, 1944, 2002, 1962, 2162, 482, 60, 57, 55, 58, 54, 1662, 1363,
			1823, 56, 75, 86, 382, 346, 2429, 2823, 2824, 2825, 346, 3885, 1882, 5005, 4547, 1982, 3305, 2063, 1987,
			7604);
	private static final List<Integer> specproducts = Arrays.asList(108678, 105217, 88693, 105220, 105221, 105223,
			105219, 105225, 105222, 105224, 105218, 105213, 105215, 105214, 105216, 105142, 105143, 105151, 105163,
			105212, 105145, 105226, 105147, 105209, 105202, 105211, 105210, 104363, 104399, 104413, 104871, 104872,
			104428, 104875, 104877, 104900, 104957, 105027, 105106, 105141);

	@RequestMapping(value = "/getcolorbyproductidandprovincenew", method = RequestMethod.GET)
	public ResponseEntity<List<ProductColorBO>> GetColorByProductIDAndProvincenew(int productID, String langID, int siteID,
			int provinceID) {

		try {
			var codetimer = new CodeTimer("timer-all");
			var odbtimer = new CodeTimer("timer-odb");

			var productHelper = GetProductClient();

			odbtimer.reset();

			ProductColorBO[] productColors = null;
			List<ProductErpPriceBO> listPrices = new ArrayList<>();
			boolean isGetCenterQuantity = false;
			try {

				var allInfor = productHelper.getAllInfoColorByProductID(productID, siteID, langID,
						DidxHelper.getPriceAreaBySiteID(siteID, langID));


				if (allInfor != null && allInfor.length > 0) {
					productColors = productHelper.GetProductColorByProductIDLang2(allInfor[0]);
					if (productColors != null && productColors.length > 0) {
						var prices = allInfor[0].GetPriceStrings;
						if (prices != null) {
							listPrices = productHelper.getPriceHelper().processQuantities(siteID, prices, provinceID);
						}
					}

				} else {
					return new ResponseEntity<>(null, HttpStatus.OK);
				}
			} catch (Throwable e) {
				Logs.LogException(e);
				return null;
			}
			odbtimer.end();

			var colorMap = Stream.of(productColors != null ? productColors : new ProductColorBO[0])
					.filter(Objects::nonNull).filter(x -> x.ColorID > 0)
					.collect(Collectors.toMap(x -> x.ProductCode, x -> x));

			var validColors = listPrices.stream().filter(x -> x != null).map(x -> {
				try {
					//var xSpecialSale =  productHelper.getSpecialSaleProgram(x.ProductCode, 1);
					x.IsOnlineOnly = x.specialSale != null;
					//x.specialSale = xSpecialSale;
					//x = APIPriceHelper.getHelperBySite(1).ProcessProductStatus(x);
				} catch (Throwable ignored) {
				}
				return x;
			}).filter(tmp -> tmp != null
					&& (tmp.WebStatusId == 4 || tmp.WebStatusId == 11))
					.sorted(Comparator
							.comparingInt(x -> x.WebStatusId)) /// 4 -> 11/
					.map(x -> {
						var t = colorMap.get(x.ProductCode);
						t.WebstatusID = x.WebStatusId;
						t.Price = x.Price;
						return t;
					}).filter(x -> x != null)
					.sorted(Comparator
							.<ProductColorBO>comparingInt(x -> x.WebstatusID)
							.thenComparingInt(x2 -> x2.DisplayOrder)
							.thenComparingDouble(x -> x.Price))
					.collect(Collectors.toList());

			codetimer.end();

			var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

			return new ResponseEntity<>(validColors, header, HttpStatus.OK);

		} catch (Exception e) {
			Logs.LogException(e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}
	@RequestMapping(value = "/getcolorbyproductidandprovince", method = RequestMethod.GET)
	public ResponseEntity<List<ProductColorBO>> GetColorByProductIDAndProvince(int productID, String langID, int siteID,
																			   int provinceID, boolean isAppliance) {
		if(!DidxHelper.isLive()){
			return GetColorByProductIDAndProvincenew(productID,langID,siteID, provinceID);
		}
		try {
			var codetimer = new CodeTimer("timer-all");
			var odbtimer = new CodeTimer("timer-odb");

//			var finalListProductColors = new ArrayList<ProductColorBO>();

			var productHelper = GetProductClient();

			odbtimer.reset();

			ProductColorBO[] productColors = null;
			List<ProductErpPriceBO> listPrices = new ArrayList<>();
			boolean isGetCenterQuantity = false;
			try {

				var allInfor = productHelper.getAllInfoColorByProductID(productID, siteID, langID,
						DidxHelper.getPriceAreaBySiteID(siteID, langID));

//				int cate = productHelper.getCategoryID(productID);
//				productColors = productHelper.GetProductColorByProductIDLang(productID, langID);

				if (allInfor != null && allInfor.length > 0) {
					productColors = productHelper.GetProductColorByProductIDLang2(allInfor[0]);
					if (productColors != null && productColors.length > 0) {
						int cate = productColors[0].CategoryID;
						isGetCenterQuantity = arrCategoryGetCenterQuantity.contains(cate)
								|| specproducts.contains(productID);

						var prices = allInfor[0].GetPriceStrings;
						if (prices != null) {
							listPrices = productHelper.getPriceHelper().processQuantities(siteID, prices, provinceID);
						}
					}

				} else {
					return new ResponseEntity<>(null, HttpStatus.OK);
				}
//				productColors = productHelper.GetProductColorByProductIDLang2(all[0]);
//				int cate = productColors[0].CategoryID;
//				isGetCenterQuantity = arrCategoryGetCenterQuantity.contains(cate) || specproducts.contains(productID);
//
//				listPrices = productHelper.getPriceHelper().getListPriceStringsWithQuantities(productID,
//				DidxHelper.getPriceAreaBySiteID(siteID, langID), siteID, provinceID, langID);
			} catch (Throwable e) {
				Logs.LogException(e);
				return null;
			}
			odbtimer.end();

			var center = isGetCenterQuantity;

			var colorMap = Stream.of(productColors != null ? productColors : new ProductColorBO[0])
					.filter(Objects::nonNull).filter(x -> x.ColorID > 0)
					.collect(Collectors.toMap(x -> x.ProductCode, x -> x));

			var validColors = listPrices.stream().filter(x -> x != null).map(x -> {
				try {
					x.IsOnlineOnly = productHelper.getSpecialSaleProgram(x.ProductCode, 1) != null;
				} catch (Throwable ignored) {
				}
				return x;
			}).filter(tmp -> tmp != null
					&& ((isAppliance ? tmp.ProductCodeQuantity : tmp.ProductCodeTotalQuantity) > 0
					|| (center && tmp.CenterQuantity > 0) || tmp.IsOnlineOnly)
					&& tmp.IsWebShow && tmp.Price > 0)
					.sorted(Comparator
							.comparingInt(x -> siteID == 2 ? -x.ProductCodeQuantity : -x.ProductCodeTotalQuantity))
					.map(x -> {
						return colorMap.get(x.ProductCode);
					}).filter(x -> x != null).collect(Collectors.toList());

//			if (productColors != null && productColors.length > 0) {
//				for (var item : productColors) {
//					if (listPrices != null && listPrices.length > 0) {
//						var tmp = Arrays.asList(listPrices).stream()
//								.filter(x -> x != null && x.ProductCode.equals(item.ProductCode)).findFirst()
//								.orElse(null);
//						if (tmp != null && (isAppliance ? tmp.ProductCodeQuantity : tmp.ProductCodeTotalQuantity) > 0
//								&& tmp.IsWebShow && tmp.Price > 0 && item.ColorID > 0) {
//
//							finalListProductColors.add(item);
//						}
//					}
//				}
//			}
			codetimer.end();

			var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

			return new ResponseEntity<>(validColors, header, HttpStatus.OK);

		} catch (Exception e) {
			Logs.LogException(e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

	@RequestMapping(value = "/getgallerybyproductid", method = RequestMethod.GET)
	public ResponseEntity<ProductGalleryBOApi[]> GetGalleryByProductID(int productID, int siteID, Integer imageType) {
		var timer = new CodeTimer("timer-all");
		var phelper = GetProductClient();
		int type = imageType == null ? 0 : imageType;
		ProductGalleryBOApi[] gallery = null;
		var status = HttpStatus.OK;
		try {
			gallery = phelper.GetGalleryByProductID(productID, siteID, type);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			LogHelper.WriteLog(e);
		}
		timer.end();
		return new ResponseEntity<>(gallery, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getstorebyprovinceid", method = RequestMethod.GET)
	public ResponseEntity<List<StoreBO>> GetStoreByProvinceID(int provinceID, int districtID, int brandID) {
		var timer = new CodeTimer("timer-all");
		List<StoreBO> list = null;
		var phelper = GetProductClient();
		var status = HttpStatus.OK;
		try {
			list = phelper.GetStoreByProvinceID(provinceID, districtID, brandID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		timer.end();
		return new ResponseEntity<List<StoreBO>>(list, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getstoreandproductcode", method = RequestMethod.GET)
	public ResponseEntity<List<StoreBO>> GetStoreAndProductCode(int provinceID, int districtID, int brandID,
			int productID, String langID) {
		if (Utils.StringIsEmpty(langID)) {
			langID = "vi-VN";
			// throw new MyException("langID null");
		}
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		List<StoreBO> list = null;
		var phelper = GetProductClient();
		try {
			list = phelper.GetStoreByProvinceID(provinceID, districtID, brandID);
			var so = phelper.GetProductSO(productID, DidxHelper.getSitebyBrandID(brandID), langID);
			if (so != null && so.Prices != null)
				list.forEach(s -> s.ProductCode = (String) so.Prices.get("ProductCode_" + provinceID));
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		timer.end();
		return new ResponseEntity<List<StoreBO>>(list, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getproductcolorbyproductid", method = RequestMethod.GET)
	public ResponseEntity<ProductColorBO[]> GetProductColorByProductID(int productID) {
		return GetColorByProductID(productID, "vi-VN",2,3);
	}

	@RequestMapping(value = "/getmanubycategoryid", method = RequestMethod.GET)
	public ResponseEntity<ProductManuBO[]> GetManuByCategoryID(int categoryID, int siteID, String langID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		var phelper = GetProductClient();
		ProductManuBO[] manu = null;
		try {
			manu = phelper.GetManuByCategoryID(categoryID, siteID, langID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<ProductManuBO[]>(manu, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getactivemanubycategoryid", method = RequestMethod.GET)
	public ResponseEntity<ProductManuBO[]> GetActiveManuByCategoryID(int categoryID, int siteID, String langID,
			String keyword) {
		var status = HttpStatus.OK;
		var timer = new CodeTimers();
		timer.start("all");
		ProductManuBO[] manu = null;
		try {
			var phelper = GetProductClient();
			timer.start("elastic");
			var qry = new ProductQuery() {
				{
					CategoryId = categoryID;
					LanguageID = langID;
					PageIndex = 0;
					PageSize = 1;
					ProvinceId = siteID == 6 ? 163 : 3;
					SiteId = siteID;
					Keyword = keyword;
//					WebStatus = 4;
					WebStatusIDList = new int[] { 2, 4, 5, 11 };
				}
			};
			ProductSOSR search = phelper.Ela_SearchProduct(qry, true, false, false, timer);
			timer.pause("elastic");
			timer.start("odb");
			var idlist = Arrays.asList(search.faceListManu).stream().map(FaceManuSR::getID)
					.collect(Collectors.toList());
			manu = phelper.getOrientClient().QueryFunction("manu_GetByIDList", ProductManuBO[].class, false, idlist,
					siteID, langID);
			timer.pause("odb");
			timer.start("grouping");
			List<ProductManuBO> manulist = new ArrayList<>(Arrays.asList(manu));
			var grouped = manulist.stream().filter(x -> x.URL != null).collect(Collectors.groupingBy(x -> x.URL));
			for (var list : grouped.values()) {
				ProductManuBO max = null;
				for (var m : list) {
					manulist.remove(m);
					if (max == null || max.ManufactureID < m.ManufactureID) {
						max = m;
					}
				}
				manulist.add(max);
			}
			manu = manulist.stream().sorted(Comparator.comparingDouble(ProductManuBO::getDisplayOrder))
					.toArray(ProductManuBO[]::new);
			timer.pause("grouping");
			timer.pause("all");
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<ProductManuBO[]>(manu, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/searchproductquery", method = RequestMethod.POST)
	public ResponseEntity<ProductBOSR> SearchProductQuery(@RequestBody ProductQuery productQuery) {

		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		var phelper = GetProductClient();
		ProductBOSR result = new ProductBOSR();
		timer.start("all");
		Throwable error = null;
		try {
			// query order duplicate check
			if (productQuery.Orders != null && productQuery.Orders.length > 0) {
				productQuery.Orders = Arrays.stream(productQuery.Orders).distinct().toArray(SearchOrder[]::new);
			}

			timer.start("es-all");
			// nghia 1
			var solist = phelper.Ela_SearchProduct(productQuery, true, true, true, timer);
			timer.pause("es-all");
			result.message = solist.message;
			result.rowCount = solist.rowCount;
			result.faceListCategory = solist.faceListCategory;
			result.faceListManu = solist.faceListManu;
			result.faceListProp = solist.faceListProp;
			result.isNotSelling = solist.isNotSelling;
			result.priceMin = solist.priceMin;
			result.priceMax = solist.priceMax;
			List<Integer> lids = new ArrayList<Integer>();
//			List<String> lcodes = new ArrayList<String>();
			if (solist.productList != null) {
				solist.productList.forEach((k, v) -> {
					lids.add(k);
//					if (v != null && v.Prices != null) {
//						String code = (String) v.Prices.get("ProductCode_" + productQuery.ProvinceId);
//						if (code != null)
//							lcodes.add(code);
//					}
				});
			}
			int[] aids = Ints.toArray(lids);
			timer.start("get-product-odb-all");
			result.productList = phelper.GetSimpleProductListByListID_PriceStrings_soMap(aids, productQuery.SiteId,
					productQuery.ProvinceId, productQuery.CategoryId, productQuery.LanguageID, timer);
			timer.pause("get-product-odb-all");

			timer.start("get-product-processcombined");

			if (productQuery.SearchFlags != null && productQuery.SearchFlags.contains(SearchFlag.COMBINEDPRODUCT)) {
				result.productList = phelper.processCombinedModel(result.productList);
			}

			timer.start("get-product-processcombined");

//			List<Integer> banchay = new ArrayList<>();
			Map<Integer, List<Integer>> bestSelling = null;
			// lay label ban chay
			// productQuery.CategoryId > 0 && // ko truyền cate cũng lấy IsBestSelling //
			// a Thái TGDD yêu cầu
			timer.start("ES-banchay");
			if (productQuery.TopSoldProduct > 0) {
				if (productQuery.CategoryId == 0 && result.faceListCategory != null
						&& result.faceListCategory.length == 1) {
					productQuery.CategoryId = result.faceListCategory[0].categoryID;
				}
//				if (productQuery.CategoryId > 0) {
//					banchay = phelper.getTopSoldCountFromCache(productQuery.CategoryId, productQuery.SiteId,
//							productQuery.ProvinceId, productQuery.LanguageID, productQuery.TopSoldProduct);
//				}

				bestSelling = phelper.getCachedTopProductSelling(productQuery.SiteId, productQuery.ProvinceId,
						productQuery.LanguageID);

			}
			timer.pause("ES-banchay");

			if (result.productList != null && bestSelling != null
			) {
				for (var product : result.productList) {
					var list = bestSelling.get(product.CategoryID);
					product.IsBestSelling = list == null ? false : list.contains(product.ProductID);
				}
			}

//			var accessories = phelper.getAccessoryCategory();

			if (result.productList == null)
				result.message = "Failed: Lay detail tu OrientDB that bai";
//			else
//				// Lấy giá từ Elastic
//				for (ProductBO product : result.productList) {
//					var isAccessory = accessories.contains((int) product.CategoryID);
////					phelper.GetDefaultPriceForList(product, isAccessory, true);
//					phelper.GetDefaultPriceAndPromotion(productQuery.SiteId, productQuery.LanguageID, product,
//							isAccessory, false);
//				}
			timer.start("cate-manu-names");
			phelper.getCateManuName(result, productQuery.SiteId, productQuery.LanguageID);
			timer.pause("cate-manu-names");

			// manu nganh hang cha
			// grouping
			if (result.faceListManu != null) {
				var manulist = new ArrayList<>(Arrays.asList(result.faceListManu));
				var grouped = manulist.stream().filter(x -> x.manufacturerName != null)
						.collect(Collectors.groupingBy(x -> x.manufacturerName));
				// find max & sum products
				for (var sublist : grouped.values()) {
					int sum = 0;
					FaceManuSR max = null;
					for (var manu : sublist) {
						manulist.remove(manu);
						sum += manu.productCount;
						if (max == null
								|| (manu.manufacturerLogo != null
										&& (max.manufacturerLogo == null || max.manufacturerID < manu.manufacturerID))
								|| (max.manufacturerLogo == null && manu.manufacturerLogo == null
										&& max.manufacturerID < manu.manufacturerID)) {
							max = manu;
						}
					}
					max.productCount = sum;
					manulist.add(max);
				}

				result.faceListManu = manulist.stream().sorted(Comparator.<FaceManuSR>comparingInt(x -> x.displayorder))
						.toArray(FaceManuSR[]::new);
			}
		} catch (Throwable e) {
			String trace = DidxHelper.getStackTrace(e);
			result.message = "Failed: " + e.toString() + ": " + e.getMessage() + " - " + trace;
			String m = "com.orientechnologies.orient.client.remote.OStorageRemote.networkOperationRetryTimeout";
			if ((trace != null && trace.contains(m)) || (e.getMessage() != null && e.getMessage().contains(m))) {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
			}

			var param = phelper.GetJsonFromObject(productQuery);
			LogHelper.WriteLog(e, LogLevel.ERROR, param);
			error = e;
		}
//		result.productList = phelper.GetSimpleProductByID(196963, siteID, provinceID);
		timer.pause("all");
		long total = timer.getTimer("all").getElapsedTime();
		if (total > 5000 || DidxHelper.isHanh()) {// hàm chạy lớn hơn 10s thì ghi log
			String params = phelper.GetJsonFromObject(productQuery);
//			long timeES = timer.getTimer("es-all").getElapsedTime();
//			long timeODB =  timer.getTimer("get-product-odb-all").getElapsedTime();
			String message = "JAVA_API : /searchproductquery - Tổng request time :" + timer.GetAllTime();
			LogHelper.WriteLog(" TIMER_LOG :" + message, LogLevel.LOGTRACE, params);
		}
		var header = HeaderBuilder.buildHeaders(error, timer);

//		try {
//			// header.set("content-length", Long.toString(SizeOf.deepSizeOf(result)));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		return new ResponseEntity<ProductBOSR>(result, header, status);

	}

	@RequestMapping(value = "/searchstorebyproductcode", method = RequestMethod.GET)
	public ResponseEntity<int[]> SearchStoreByProductCode(String productCode, int brandID, int provinceID,
			int districtID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		int[] result = null;
		try {
			result = GetProductClient().SearchStoreByProductCode(productCode, brandID, provinceID, districtID,
					new BooleanWrapper());
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		timer.end();
		return new ResponseEntity<int[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/searchstorebyproductid", method = RequestMethod.GET)
	public ResponseEntity<int[]> SearchStoreByProductID(int productID, int brandID, int provinceID, int districtID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		int[] result = null;
		var helper = GetProductClient();
		try {
			result = helper.SearchStoreByProductID(productID, brandID, provinceID, districtID, new BooleanWrapper());
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<int[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/searchavailablestorebyproductid", method = RequestMethod.GET)
	public ResponseEntity<List<StoreBO>> SearchAvailableStoreByProductID(int productID, int brandID, int provinceID,
			int districtID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		List<StoreBO> stores = null;
		var helper = GetProductClient();
		try {
			var it364 = new BooleanWrapper();
			Set<Integer> result = Arrays
					.stream(helper.SearchStoreByProductID(productID, brandID, provinceID, districtID, it364)).boxed()
					.collect(Collectors.toSet());
			stores = Stream.of(helper.getStoreByProvinceID(provinceID, districtID, brandID, it364.value)).map(x -> {
				if (result.contains((int) x.StoreID))
					x.isStockAvailable = true;
				return x;
			}).sorted(Comparator.comparingInt(x -> x.isStockAvailable ? 0 : 1)).collect(Collectors.toList());
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		timer.end();
		return new ResponseEntity<>(stores, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/searchavailablestorebyproductcode", method = RequestMethod.GET)
	public ResponseEntity<List<StoreBO>> SearchAvailableStoreByProductCode(String productCode, int brandID,
			int provinceID, int districtID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		List<StoreBO> stores = null;
		var helper = GetProductClient();
		try {
			;
			var it364 = new BooleanWrapper();

			StoreBO[] tmpstore = helper.SearchStoreStockByProductCode(productCode, brandID, provinceID, districtID,
					it364);
			var mappedStores = Arrays.stream(tmpstore).collect(Collectors.groupingBy(x -> x.StoreID));

//			Set<Integer> result = Arrays
//					.stream(helper.SearchStoreByProductCode(productCode, brandID, provinceID, districtID, it364))
//					.boxed().collect(Collectors.toSet());
			stores = Stream.of(helper.getStoreByProvinceID(provinceID, districtID, brandID, it364.value)).map(x -> {
//				if (result.contains((int) x.StoreID))
//					x.isStockAvailable = true;
				List<StoreBO> tmpStock = null;
				if (mappedStores != null && (tmpStock = mappedStores.get(x.StoreID)) != null) {
					if (tmpStock.size() > 0) {
						for (var y : tmpStock) {
							if (y.ProductCode.equals(productCode)) {
								switch (y.InventoryStatusID) {
									case 7:
										x.OldSampleQuantity += y.Quantity;
										break;
									case 3:
										x.SampleQuantity += y.Quantity;
										break;
									case 1:
										x.Quantity += y.Quantity;
										if (!x.isStockAvailable && y.Quantity > 0) {
											x.isStockAvailable = true;
										}
										break;
									default:
										break;
								}
							} else if (y.InventoryStatusID == 3) {
								x.ReplacePrdQuantity += y.Quantity;
							}
						}
					}
				}
				return x;
			}).sorted(Comparator.comparingInt(x -> x.isStockAvailable ? 0 : 1)).collect(Collectors.toList());
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<>(stores, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/searchavailableprovincebyproductcode", method = RequestMethod.GET)
	public ResponseEntity<int[]> SearchAvailableProvinceByProductCode(String productCode, int brandID, Boolean getSample) {
		int[] result = null;
		var status = HttpStatus.OK;
		boolean sample = getSample == null ? false : getSample;
		try {
			if (brandID == 6 || brandID == 11) {
				result = GetProductClient().getProvinceIDByStockCam(productCode, sample);
			} else {
				result = GetProductClient().getProvinceIDByStockDMX(productCode);
			}
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/searchavailabledistrictbyproductcode", method = RequestMethod.GET)
	public ResponseEntity<int[]> SearchAvailableDistrictByProductCode(String productCode, int provinceID, int brandID) {
		int[] result = null;
		var status = HttpStatus.OK;
		try {
			result = GetProductClient().searchDistrictByProductCode(productCode, provinceID, brandID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/searchavailablestorebyprovinceidlist", method = RequestMethod.GET)
	public ResponseEntity<List<StoreBO>> searchAvailableStoreByProvinceIDList(String productCode, int brandID,
			String provinceIDs, boolean isStock) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		List<StoreBO> stores = null;
		var helper = GetProductClient();
		try {
			int[] arrProvinceIDs = Stream.of(provinceIDs.split(",")).mapToInt(x -> Utils.toInt(x.trim())).distinct()
					.filter(x -> x > 0).toArray();
			var it364 = new BooleanWrapper();
			int[] storeIDs = helper.searchStoreByProvinceList(productCode, brandID, arrProvinceIDs, it364);
			if (isStock) {
				stores = Arrays.asList(helper.getStoreByIDList(storeIDs));
				for (var x : stores) {
					x.isStockAvailable = true;
				}
			} else {
				Set<Integer> result = IntStream.of(storeIDs).boxed().distinct().collect(Collectors.toSet());
				stores = Stream.of(helper.getStoreByProvinceIDList(arrProvinceIDs, brandID, it364.value)).map(x -> {
					if (result.contains((int) x.StoreID))
						x.isStockAvailable = true;
					return x;
				}).sorted(Comparator.comparingInt(x -> x.isStockAvailable ? 0 : 1)).collect(Collectors.toList());
			}
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<>(stores, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/get364provinces", method = RequestMethod.GET)
	public ResponseEntity<int[]> get364Provinces(int brandID) {
		int[] result = null;
		var status = HttpStatus.OK;
		ProductHelper helper = null;
		try {
			helper = GetProductClient();
			var stores = helper.getStoreByProvinceID(0, 0, brandID, true);
			result = Stream.of(stores).mapToInt(x -> x.ProvinceID).distinct().toArray();
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/searchstorebyproductcodefull", method = RequestMethod.GET)
	public ResponseEntity<StoreBO[]> SearchStoreByProductCodeFull(String productCode, int brandID, int provinceID,
			int districtID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		int[] result = null;
		StoreBO[] stores = null;
		var helper = GetProductClient();
		try {
			result = helper.SearchStoreByProductCode(productCode, brandID, provinceID, districtID,
					new BooleanWrapper());
			stores = helper.getStoreByIDList(result);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<StoreBO[]>(stores, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getproductslider", method = RequestMethod.GET)
	public ResponseEntity<ProductSliderBO[]> GetProductSlider(int productID, int siteID, String langID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductSliderBO[] result = null;
		try {
			result = GetProductClient().getProductSlider(productID, siteID, langID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<ProductSliderBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getfeatureaccessory", method = RequestMethod.GET)
	public ResponseEntity<ProductBO[]> GetFeatureAccessory(int countByCate, int provinceID, int siteID, String langID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		var estimer = new CodeTimer("timer-es");
		var status = HttpStatus.OK;
		var phelper = GetProductClient();
		try {
			estimer.reset();
			var somap = phelper.GetFeatureAccessory(countByCate, provinceID);
			estimer.end();
			var idlist = somap.values().stream()
					.sorted((o1, o2) -> (int) ((o2.discountValue(provinceID) - o1.discountValue(provinceID)) * 100))
					.map(so -> so.ProductID).collect(Collectors.toList());
//			var idlist = new ArrayList<Integer>();
//			somap.forEach((k, v) -> {
//				idlist.add(k);
//			});
			int[] idarray = Ints.toArray(idlist);
			odbtimer.reset();
			var result = GetProductClient().GetProductBOByListID(idarray, siteID, provinceID, langID);
			odbtimer.end();
			// Lấy giá từ Elastic

			var accessories = phelper.getAccessoryCategory();
			for (var product : result) {
				var isAccessory = accessories.contains((int) product.CategoryID);
//				phelper.GetDefaultPriceForList(product, isAccessory, false);
				phelper.GetDefaultPriceAndPromotion(siteID, langID, product, isAccessory, false, null);
			}
//			Arrays.sort(result, Comparator.comparingDouble(ProductBO::discountPercent).reversed());
			codetimer.end();
			return new ResponseEntity<ProductBO[]>(result, HeaderBuilder.buildHeaders(codetimer, odbtimer, estimer),
					status);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<ProductBO[]>(new ProductBO[0], status);
	}

	@RequestMapping(value = "/getsamepriceproduct", method = RequestMethod.GET)
	public ResponseEntity<ProductBO[]> GetSamePriceProduct(int productID, int siteID, int provinceID, String langID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		var phelper = GetProductClient();
		ProductBO product = null;
		ProductBO[] products = null;
		try {
			product = phelper.GetProductBOByProductID(productID, siteID, provinceID, langID, 0);
			products = phelper.getSamePriceProduct(product, siteID, provinceID, langID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		var accessories = phelper.getAccessoryCategory();

		Arrays.stream(products).forEach(p -> {
			try {
				phelper.GetDefaultPriceAndPromotion(siteID, langID, p, accessories.contains((int) p.CategoryID), true,
						null);
			} catch (Throwable e) {
				LogHelper.WriteLog(e);

			}
		});
		timer.end();
		return new ResponseEntity<>(products, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getproductgallery360", method = RequestMethod.GET)
	public ResponseEntity<ProductGallery360BO[]> GetProductGallery360(int productID, int siteID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductGallery360BO[] gallery = null;
		try {
			gallery = GetProductClient().getProductGallery360(productID, siteID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		timer.end();
		return new ResponseEntity<>(gallery, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getproductdetail", method = RequestMethod.GET)
	public ResponseEntity<ProductDetailBO[]> getProductDetail(int productID, int siteID, String langID,
			Integer clearcache, Boolean getExcluded) {
		int clear = clearcache == null ? 0 : clearcache;
		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		var phelper = GetProductClient();
		CachedDetails cached = null;
		try {
			cached = phelper.getCachedProductDetail(productID, siteID, langID, false, clear, timer);
			if (getExcluded == null || !getExcluded) {
				// excludes
				var excInfo = phelper.getHTMLInfo(1514, 1, "vi-VN");
				if (excInfo != null && excInfo.InfoLangBO != null && excInfo.InfoLangBO.Content != null) {
					var excludes = Arrays.asList(excInfo.InfoLangBO.Content.split(","));
					cached.list = Stream.of(cached.list).filter(x -> x.Value == null || !excludes.contains(x.Value))
							.toArray(ProductDetailBO[]::new);
				}
			}
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		if (cached != null) {
			return new ResponseEntity<>(cached.list, HeaderBuilder.buildHeaders(timer), status);
		} else {
			return new ResponseEntity<>(null, HeaderBuilder.buildHeaders(timer), status);
		}
	}

	@RequestMapping(value = "/getproductinfobyurl", method = RequestMethod.GET)
	public ResponseEntity<ProductUrl> getProductInfoByURL(int siteID, String url, String langID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductUrl urlPrd = null;
		try {
			urlPrd = GetProductClient().getProductInfoByURL(siteID, url, langID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<>(urlPrd, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getlistspectemplate", method = RequestMethod.GET)
	public ResponseEntity<SpecTemplateBO[]> getListSpecTemplate(int siteID, String langID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		SpecTemplateBO[] templates = null;
		try {
			templates = GetProductClient().getListSpecTemplate(siteID, langID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<>(templates, HeaderBuilder.buildHeaders(timer), status);
	}

	/**
	 * 
	 * @param categoryID  j
	 * @param productID
	 * @param pictureID
	 * @param sizeGroupID
	 * @param pictureType
	 * @param imageType
	 * @param userID
	 * @param pageIndex
	 * @param pageSize
	 * @param orderBy
	 * @param orderValue  0 => normal, 1 => desc, 2 => asc
	 * @return
	 */
	@RequestMapping(value = "/getlistproductusergallery", method = RequestMethod.GET)
	public ResponseEntity<ProductGalleryBO[]> getListProductUserGallery(int categoryID, int productID, int pictureID,
			int sizeGroupID, int pictureType, int imageType, long userID, int pageIndex, int pageSize, int orderBy,
			int orderValue, Integer siteID) {

		long startTime = System.currentTimeMillis();
		int siteid = siteID == null ? 0 : siteID;
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		if (orderValue < 0 || orderValue > 2)
			orderValue = 0;
		OrderType orderType = OrderType.values()[orderValue];
		var phelper = GetProductClient();
		GallerySR result = null;
		try {
			result = phelper.getListProductUserGallery(siteid, categoryID, productID, pictureID, sizeGroupID,
					pictureType, imageType, userID, pageIndex, pageSize, orderBy, orderType);
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();

		long endTime = System.currentTimeMillis();
		if (endTime - startTime > 2500) {
			var message = String.format("getlistproductusergallery - Total request time: %s",
					String.valueOf(endTime - startTime));

			LogHelper.WriteLogTimer(message, LogLevel.WARNING, request);

		}
		if (result != null) {
			return new ResponseEntity<>(result.result, HeaderBuilder.buildHeaders(timer), status);
		} else {
			return new ResponseEntity<>(null, HeaderBuilder.buildHeaders(timer), status);
		}

	}

	@RequestMapping(value = "/getlistproductusergallerytest", method = RequestMethod.GET)
	public ResponseEntity<GallerySR> getListProductUserGalleryTest(int categoryID, int productID, int pictureID,
			int sizeGroupID, int pictureType, int imageType, long userID, int pageIndex, int pageSize, int orderBy,
			int orderValue, Integer siteID) {
		int siteid = siteID == null ? 0 : siteID;
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		if (orderValue < 0 || orderValue > 2)
			orderValue = 0;
		OrderType orderType = OrderType.values()[orderValue];
		var phelper = GetProductClient();
		GallerySR result = null;
		try {
			result = phelper.getListProductUserGallery(siteid, categoryID, productID, pictureID, sizeGroupID,
					pictureType, imageType, userID, pageIndex, pageSize, orderBy, orderType);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getfeatureproduct", method = RequestMethod.GET)
	public ResponseEntity<ProductBO[]> getFeatureProduct(int isShowHome, String categoryIDList, int siteID,
			boolean isPriority) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		var catelist = categoryIDList == null || categoryIDList.isEmpty() ? null
				: Stream.of(categoryIDList.split(",")).mapToInt(x -> Integer.parseInt(x.trim())).toArray();
		ProductBO[] products = null;
		try {
			products = GetProductClient().getFeatureProductsByListCate(isShowHome, catelist, siteID, isPriority);
		} catch (Throwable e) {
//			e.printStackTrace();
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<>(products, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getproductrating", method = RequestMethod.GET)
	public ResponseEntity<TemplateRating[]> getProductRating(int productID, int price, int siteID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		TemplateRating[] result = null;
		try {
			result = GetProductClient().getProductRating(productID, price, siteID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getprenextproducts", method = RequestMethod.GET)
	public ResponseEntity<int[]> getNextAndPrevGenerationOfProduct(int productID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		int[] result = null;
		try {
			result = GetProductClient().getNextAndPrevGenerationOfProduct(productID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<int[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getdeliverytime", method = RequestMethod.GET)
	public ResponseEntity<DeliveryTime> get(int provinceID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		DeliveryTime result = null;
		try {
			result = GetProductClient().GetDeliverytimeProvinceByProvinceID(provinceID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getlistallacessory", method = RequestMethod.GET)
	public ResponseEntity<ProductBO[]> getListAllAccessory(int collectionID, int siteID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductBO[] result = null;
		try {
			result = GetProductClient().getListAllAccessory(collectionID, siteID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getfullaccessory", method = RequestMethod.GET)
	public ResponseEntity<ProductBO[]> getFullAccessory(int productID, int siteID, int provinceID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductBO[] result = null;
		try {
			result = GetProductClient().getFullAccessory(productID, siteID, provinceID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			LogHelper.WriteLog(e);
		}
		timer.end();
		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getaccessory", method = RequestMethod.GET)
	public ResponseEntity<ProductBO[]> getAccessory(int productID, int siteID, int provinceID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductBO[] result = null;
		try {
			result = GetProductClient().getAccessory(productID, siteID, provinceID);
		} catch (Throwable e) {
//			e.printStackTrace();
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		timer.end();
		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getshockpriceinfo", method = RequestMethod.GET)
	public ResponseEntity<ShockPriceBO> GetShockPriceInfo(int productID, int siteID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ShockPriceBO shockPrice = new ShockPriceBO();

		try {
			shockPrice = GetProductClient().GetShockPriceInfo(productID, siteID);
			if (shockPrice != null && shockPrice.ShockPriceID > 0) {
				shockPrice.ListShockPriceDiscountBO = GetProductClient()
						.GetListShockPriceDiscount(shockPrice.ShockPriceID);
				shockPrice.IsExist = 1;
			}
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<>(shockPrice, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getstockquantity", method = RequestMethod.GET)
	public ResponseEntity<StockBO[]> getStockQuantity(String productCodeList, String storeIDList) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		String[] codes = productCodeList.split(",");
		int[] storeids = Stream.of(storeIDList.split(",")).mapToInt(x -> Integer.parseInt(x)).toArray();
		StockBO[] result = null;
		try {
			result = GetProductClient().getStockQuantity(codes, storeids);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		timer.end();
		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getstockquantitybhx", method = RequestMethod.GET)
	public ResponseEntity<InStockBO> getStockQuantityBhx(int productId, int storedId) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		InStockBO result = new InStockBO();
		try {
			result = GetProductClient().getStockQuantityBhx(productId, storedId, null);
		} catch (Throwable e) {
			result = null;
			e.printStackTrace();
		}
		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getliststockquantitybhx", method = RequestMethod.GET)
	public ResponseEntity<List<InStockBO>> getListStockQuantityBhx(int productId) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		List<InStockBO> result = null;
		try {
			result = GetProductClient().getListStockQuantityBhx(productId);
		} catch (Throwable e) {
			result = null;
			e.printStackTrace();
		}
		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getproductvideoshowbyproductid", method = RequestMethod.GET)
	public ResponseEntity<ProductVideoShowBO[]> GetProductVideoShowByProductID(int productID, String langID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductVideoShowBO[] result = null;
		try {
			result = GetProductClient().GetProductVideoShowByProductID(productID, langID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		timer.end();
		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getaccesoriesbycollection", method = RequestMethod.GET)
	public ResponseEntity<AccesoriesResult> GetAccesoriesByCollection(int provinceID, int categoryID, int productID,
			int siteID, int pageIndex, int pageSize) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		ProductBO[] result = null;
		var accesoriesResult = new AccesoriesResult();
		try {
			result = GetProductClient().getFullAccessory(productID, siteID, provinceID);
			accesoriesResult.total = result.length;
			if (categoryID > 0) {
				accesoriesResult.result = Arrays.asList(result).stream().filter(x -> x.CategoryID == categoryID)
						.skip(pageIndex * pageSize).limit(pageSize).collect(Collectors.toCollection(ArrayList::new));
			} else {
				accesoriesResult.result = Arrays.asList(result).stream().skip(pageIndex * pageSize).limit(pageSize)
						.collect(Collectors.toCollection(ArrayList::new));
			}

			timer.end();
		} catch (Throwable e) {

			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(accesoriesResult, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getrelativeproductsbypriceproperty", method = RequestMethod.GET)
	public ResponseEntity<ProductBO[]> GetRelativeProductsByPrice_Property(int productID, int provinceID, int siteID,
			String langID, int propertyID, int propertyValueID, int property2ID, int property2ValueID) {

		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		ProductBO product = null;
		var productHelper = GetProductClient();
		ProductBO[] listRelativeProduct = null;
		try {
			product = productHelper.GetProduct_PriceStrings(productID, siteID, provinceID, 0, langID, timer);

			if (product == null)
				return null;
			double price = 0;
			if (product.ProductErpPriceBO != null) {
				price = product.ProductErpPriceBO.Price;
			}

			listRelativeProduct = productHelper.GetRelativeProduct(product, provinceID, siteID, langID, propertyID,
					propertyValueID, property2ID, property2ValueID, price);
		} catch (Throwable e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			LogHelper.WriteLog(e);
		}

		var header = HeaderBuilder.buildHeaders(timer);
		return new ResponseEntity<>(listRelativeProduct, header, status);

	}

	@RequestMapping(value = "/getpricepromobyid", method = RequestMethod.GET)
	public ResponseEntity<ProductBO[]> getPricePromoByID(int productID, int siteID, int provinceID, String langID) {
		try {
			return new ResponseEntity<>(GetProductClient().getPricePromoByID(productID, siteID, provinceID, langID),
					HttpStatus.OK);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			return null;
		}
	}

	@RequestMapping(value = "/getdistrictbystock", method = RequestMethod.GET)
	public ResponseEntity<DistrictBO[]> getDistrictByStock(String productCode, int provinceID, int siteID) {
		DistrictBO[] result = null;
		var status = HttpStatus.OK;
		try {
			result = GetProductClient().getDistrictByStock(productCode, provinceID, siteID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/getprovincebystock", method = RequestMethod.GET)
	public ResponseEntity<ProvinceBO[]> getProvinceByStock(String productCode, int siteID) {
		ProvinceBO[] result = null;
		var status = HttpStatus.OK;
		try {
			result = GetProductClient().getProvinceByStock(productCode, siteID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	/**
	 * GetSubscribedCustomerListID
	 */
	@RequestMapping(value = "/getcustomerlist", method = RequestMethod.GET)
	public ResponseEntity<Integer> getCustomerList(int productID) {
		int result = 0;
		var status = HttpStatus.OK;
		try {
			result = GetProductClient().getSubscribedCustomerListID(productID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/getdataurlredirect", method = RequestMethod.GET)
	public ResponseEntity<URLGoogleBO[]> getDataUrlRedirect() {
		URLGoogleBO[] result = null;
		var status = HttpStatus.OK;
		try {
			result = GetProductClient().getCachedDataUrlRedirect();
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/getdataurlredirectbysiteid", method = RequestMethod.GET)
	public ResponseEntity<URLGoogleBO[]> getDataUrlRedirectBySiteID(int siteID) {
		URLGoogleBO[] result = null;
		var status = HttpStatus.OK;
		try {
			result = GetProductClient().getCachedDataUrlRedirect();
			if (siteID > 0) {
				result = Arrays.stream(result).filter(x -> x.SiteID == siteID).toArray(URLGoogleBO[]::new);
			}
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/getlistnormalinstallment", method = RequestMethod.GET)
	public ResponseEntity<List<InstallmentBO>> getListNormalInstallment(int categoryID, double price, int companyID,
			int percent, int month, int briefID, int productID, int siteID, String languageID) {
		List<InstallmentBO> result = null;
		var status = HttpStatus.OK;
		try {
			result = GetProductClient().getListNormalInstallment(siteID, categoryID, price, companyID, percent, month,
					briefID, productID, -1, 0, languageID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/getlistinstallmentexception", method = RequestMethod.GET)
	public ResponseEntity<InstallmentException[]> getListInstallmentException(int productID) {
		InstallmentException[] result = null;
		var status = HttpStatus.OK;

		try {
			result = GetProductClient().getListInstallmentException(productID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/getlistpromotioncms", method = RequestMethod.GET)
	public ResponseEntity<PreOrderProgramBO[]> getListPromotionCMS(int productID) {
		PreOrderProgramBO[] result = null;
		var status = HttpStatus.OK;

		try {
			result = GetProductClient().getListPromotionCMS(productID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/preorderprogramgetbyproductid", method = RequestMethod.GET)
	public ResponseEntity<PreOrderProgramBO[]> preOrderProgramGetByProductId(int productID) {
		PreOrderProgramBO[] result = null;
		var status = HttpStatus.OK;

		try {
			result = GetProductClient().preOrderProgramGetByProductId(productID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	/*
	 * LOCTRAN
	 */
	@RequestMapping(value = "/getnextversionproduct", method = RequestMethod.GET)
	public ResponseEntity<ProductBO[]> GetnextVersionProduct(int productID, int siteID, String langID, int provinceID) {
		ProductBO[] result = null;
		var status = HttpStatus.OK;
		var timer = new CodeTimers();
		timer.start("all");
		try {
			result = GetProductClient().GetnextVersionProductFromCache(productID, siteID, langID, provinceID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
		}
		timer.pause("all");
		long totalTimer = timer.getTimer("all").getElapsedTime();
		if (totalTimer > 10000) {
			String message = "JAVA_API : /getnextversionproduct - Tổng request time :" + totalTimer;
			LogHelper.WriteLog(" TIMER_LOG :" + message, LogLevel.LOGTRACE, request);
		}
		return new ResponseEntity<>(result, status);
	}

	@PostMapping(value = "/getproductsbyproductbankem")
	public ResponseEntity<ProductBO[]> getProductsByProductBanKem(@RequestBody PromotionQuery query) {
		if (Objects.isNull(query.ProductIds) || query.ProductIds.isEmpty())
			return null;
		if (query.PageSize <= 0)
			query.PageSize = 1;
		if (query.PageIndex < 0)
			query.PageIndex = 0;
		ProductBO[] result = null;
		var status = HttpStatus.OK;
		try {
			result = GetProductClient().getProductsByProductBanKem(query);
		} catch (Throwable ex) {
			ex.printStackTrace();
			LogHelper.WriteLog(ex, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(result, status);
	}

	// -------------

	@RequestMapping(value = "/getproductlabelcampaign", method = RequestMethod.GET)
	public ResponseEntity<LabelCampaignBO[]> GetProductLabelCampaign(int productID) {
		LabelCampaignBO[] result = null;
		var status = HttpStatus.OK;

		try {
			result = GetProductClient().GetProductLabelCampaign(productID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/getallproductlabelcampaign", method = RequestMethod.GET)
	public ResponseEntity<LabelCampaignSO[]> GetAllProductLabelCampaign(int siteId) {
		LabelCampaignSO[] result = null;
		var status = HttpStatus.OK;
		try {
			result = GetProductClient().GetAllProductLabelCampaign(siteId);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/getlistproductpromotionbankem", method = RequestMethod.POST)
	public ResponseEntity<ProductBOSR> getListProductPromotionBanKem(@RequestBody PromotionQuery promotionQuery) {
		ProductBOSR result = null;
		var status = HttpStatus.OK;
		try {
			result = GetProductClient().getListProductPromotionBanKem(promotionQuery);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			var param = GetProductClient().GetJsonFromObject(promotionQuery);
			LogHelper.WriteLog(e, LogLevel.ERROR, param);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/getpromotionsbankeminfo", method = RequestMethod.POST)
	public ResponseEntity<PromotionProductBanKemBO[]> getPromotionsBanKemInfo(
			@RequestBody PromotionQuery promotionQuery) {
		PromotionProductBanKemBO[] result = null;
		var status = HttpStatus.OK;
		var timer = new CodeTimers();
		timer.start("all");
		try {
			result = GetProductClient().getPromotionsBanKemInfo(promotionQuery, timer);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			var param = GetProductClient().GetJsonFromObject(promotionQuery);
			LogHelper.WriteLog(e, LogLevel.ERROR, param);
		}
		timer.pause("all");
		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getlistproductgroupbycategoryid", method = RequestMethod.GET)
	public ResponseEntity<ProductPropGrpBO[]> getListProductGroupByCategoryID(long categoryID, int siteID,
			String languageID, int isGetAll) {
		ProductPropGrpBO[] result = null;
		var status = HttpStatus.OK;
		try {
			result = GetProductClient().GetListProductGroupByCategoryID(categoryID, isGetAll, languageID, siteID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	public ResponseEntity<ProductBO> GetDefaultProduct(int productID, int siteID, Integer storeID, Integer provinceID,
			String lang) {
		return new ResponseEntity<ProductBO>(new ProductBO(), HttpStatus.INTERNAL_SERVER_ERROR);

	}

	public ResponseEntity<String> GetDefaultProduct2(int productID, int siteID, Integer storeID, Integer provinceID,
			String lang) {
		return new ResponseEntity<String>("", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public ResponseEntity<ProductBOSR> GetDefaultSearchProductQuery(@RequestBody ProductQuery productQuery) {
		var result = new ProductBOSR();
		result.message = "error on SearchProductQuery with params:"
				+ _commonHelper.ConvertObjectToJsonString(productQuery);
		return new ResponseEntity<ProductBOSR>(result, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/getdatacenterconfig", method = RequestMethod.GET)
	public ResponseEntity<String> GetDataCenterConfig() {
		var dataCenter = ConfigUtils.GetDataCenter();
		return new ResponseEntity<String>(Integer.toString(dataCenter), HttpStatus.OK);
	}

	@RequestMapping(value = "/getproductwithcategoryfilters", method = RequestMethod.POST)
	public ResponseEntity<ProductBOSR> GetProductWithCategoryFilters(@RequestBody ProductQuery productQuery) {

		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es-all");
		var odbtimer = new CodeTimer("timer-odb");
		var nametimer = new CodeTimer("timer-names");
		var parsetimer = new CodeTimer("timer-es-parser");//
		var querytimer = new CodeTimer("timer-es-query");//
		var status = HttpStatus.OK;
		var phelper = GetProductClient();
		ProductBOSR result = new ProductBOSR();
		try {
			estimer.reset();
			var solist = phelper.Ela_SearchProduct(productQuery, true, true, true, null);
			estimer.end();

			result.message = solist.message;
			result.rowCount = solist.rowCount;
			result.faceListCategory = solist.faceListCategory;
			result.faceListManu = solist.faceListManu;
			result.faceListProp = solist.faceListProp;
			List<Integer> lids = new ArrayList<Integer>();
			List<String> lcodes = new ArrayList<String>();
			if (solist.productList != null) {
				solist.productList.forEach((k, v) -> {
					lids.add(k);
					if (v != null && v.Prices != null) {
						String code = (String) v.Prices.get("ProductCode_" + productQuery.ProvinceId);
						if (code != null)
							lcodes.add(code);
					}
				});
			}
			int[] aids = Ints.toArray(lids);

			odbtimer.reset();
			result.productList = phelper.GetSimpleProductListByListID_PriceStrings(aids, productQuery.SiteId,
					productQuery.ProvinceId, productQuery.LanguageID);
			odbtimer.end();

			for (var product : result.productList) {
				var so = solist.productList.get(product.ProductID);
				if (so != null) {
					product.IsPayment = so.IsPayment == 1;
					product.Paymentfromdate = so.PaymentFromDate;
					product.Paymenttodate = so.PaymentToDate;
					product.PercentInstallment = so.PercentInstallment;
					product.StickerLabel = so.StickerLabel;
				}
			}

		} catch (Throwable e) {
			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.message = "Failed: " + e.toString() + ": " + e.getMessage() + " - " + trace;
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			var param = phelper.GetJsonFromObject(productQuery);
			LogHelper.WriteLog(e, LogLevel.ERROR, param);

		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(codetimer, estimer, odbtimer, nametimer, parsetimer, querytimer);

		return new ResponseEntity<ProductBOSR>(result, header, status);
	}

	/*
	 * - Tuấn Vũ 142791 7/1/2021
	 */
	@RequestMapping(value = "/gethomepageproduct2020", method = RequestMethod.POST)
	public ResponseEntity<ProductBO[]> GetHomePageProduct(@RequestBody HomePageQuery qry) {
		if (qry.provinceID == 0)
			qry.provinceID = 3;
		var time = new CodeTimers();
		time.start("all");
		var estimer = new CodeTimer("timer-es-all");
		var productHelper = GetProductClient();
		var status = HttpStatus.OK;
		estimer.reset();
		ProductBO[] productBO = null;
		try {
			var listID = productHelper.Ela_getHomePageProduct2020(qry);
			if (listID != null && listID.length > 0)
				productBO = GetProductClient().GetSimpleProductListByListID_PriceStrings_soMap(listID, qry.siteID,
						qry.provinceID, "vi-VN");
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();
		}
		estimer.end();
		return new ResponseEntity<>(productBO, HeaderBuilder.buildHeaders(time), status);
	}

	@GetMapping(value = "/getfeaturelaptop")
	public ResponseEntity<ProductBO[]> getFeatureLaptop(int siteID, Integer provinceID, String lang) {
		if (siteID == 0)
			siteID = 1;
		if (provinceID == null)
			provinceID = 3;
		if (lang == null || lang == "")
			lang = "vi-VN";
		ProductBO[] listFeautureLaptop = null;
		List<Integer> listContenID = new ArrayList<>();
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		try {
			var htmlInfo = SystemController.GetSystemClient().getHTMLInfo(3122, 1, lang);
			if (htmlInfo != null && !htmlInfo.InfoLangBO.Content.isEmpty()) {
				String[] chuoi = htmlInfo.InfoLangBO.Content.split(",");
				for (String tachChuoi : chuoi) {
					// System.out.println("test Chuoi" + Integer.parseInt(tachChuoi.trim()));
					listContenID.add(Integer.parseInt(tachChuoi.trim()));
				}
			}

			listFeautureLaptop = GetProductClient().GetSimpleProductListByListID_PriceStrings(
					listContenID.stream().mapToInt(x -> x).toArray(), siteID, provinceID, lang);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<>(listFeautureLaptop, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getappliancehasonlineprice2019", method = RequestMethod.GET)
	public ResponseEntity<ProductBO[]> getApplianceHasOnlinePrice2019(int provinceID, int pageIndex, int pageSize,
			int[] excludeProducts, long total, int[] cateID) {
		return null;

	}

	@RequestMapping(value = "/getproductlist", method = RequestMethod.GET)
	public ResponseEntity<ProductBO[]> getProductList(String productids, int siteID, int provinceID, String langID) {
		if (Strings.isNullOrEmpty(productids)) {
			return null;
		}
		var timer = new CodeTimers();
		timer.start("all");
		ProductBO[] result = null;
		var status = HttpStatus.OK;
		// List<Integer> listID = new ArrayList<>();
		ProductHelper helper;
		try {
			helper = GetProductClient();
			timer.start("getTopProductSelling");
			var bessSelling = helper.getCachedTopProductSelling(siteID, provinceID, langID);
			timer.pause("getTopProductSelling");
			String[] mangID = productids.split(",");
			if (mangID.length > 100)
				mangID = Stream.of(mangID).limit(100).toArray(String[]::new);
			int[] ids = Stream.of(mangID).mapToInt(x -> {
				return Utils.toInt(x);
			}).filter(x -> x > 0).toArray();

			if (ids.length > 0) {
				result = helper.GetSimpleProductListByListID_PriceStrings_soMap(ids, siteID, provinceID, 0, langID,
						timer);
				for (var x : result) {
					var y = bessSelling.get(x.CategoryID);
					x.IsBestSelling = y != null && y.contains(x.ProductID);
				}
			}
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
		}
		timer.pause("all");
		long time = timer.getTimer("all").getElapsedTime();
		if (time > 5000) {
			String message = String.format(" JAVA_API : /getproductlist - Tổng request time : %s ",
					String.valueOf(time));
			LogHelper.WriteLog(" TIMER_LOG :" + message, LogLevel.LOGTRACE, request);
		}
		var header = HeaderBuilder.buildHeaders(timer);
		return new ResponseEntity<ProductBO[]>(result, header, status);
	}

	/*
	 * - Văn Long 142785 13/1/2021
	 */

	@RequestMapping(value = "/searchproductsuggest", method = RequestMethod.POST)
	public ResponseEntity<ProductBOSR> searchProductSuggest(@RequestBody ProductQuery query) {
		var parsetimer = new CodeTimer("timer-es-parser");
		var querytimer = new CodeTimer("timer-es-query");
		var status = HttpStatus.OK;
		var phelper = GetProductClient();
		ProductBOSR result = new ProductBOSR();
		if (query != null) {
			try {
				// String priceKeyLst[] = null;
				// List<String> categoryLst = null;

				query.IsSearchLike = false;// search uu tien
				if (query.SearchFlags != null && query.SearchFlags.contains(SearchFlag.ISSEARCHLIKE)) {
					query.SearchFlags.remove(SearchFlag.ISSEARCHLIKE);
				}

				// query.SearchType = SearchType.SEARCH;
//                 if (query.SearchType. <= 0){
//                     query.SearchType = -3;
//                 }
				var productBOSR = phelper.Ela_SearchProduct(query, true, true, true, null);
				result.message = productBOSR.message;
				result.rowCount = productBOSR.rowCount;
				result.faceListCategory = productBOSR.faceListCategory;
				result.faceListManu = productBOSR.faceListManu;
				result.faceListProp = productBOSR.faceListProp;

//				FacePropSR[] facet1 = (FacePropSR[]) Stream.of(result.faceListProp).filter(x -> x.propID == 1)
//						.toArray();
//				var cateKey = Stream.of(facet1).map(x -> x.propValueID).toArray();
//				// var lstcate = iCached.GetAll<ProductCategoryBO>(cateKey); chưa hiểu :)
//				if (lstcate != null) {
//					int idx = 0;
//					for (var item : lstcate) {
//						if (result.faceListCategory == null)
//							result.faceListCategory = (FaceCategorySR[]) new ArrayList<FaceCategorySR>().toArray();
//						if (item != null) {
//							if (facet1.length >= idx && facet1[idx].propValueID == item.categoryID) {
//								item.productCount = facet1[idx].count;
//							}
//							productBOSR.faceListCategory[productBOSR.faceListCategory.length] = item;
//						}
//						idx++;
//					}
//				}    else {
//              var cates = SearchCategory(query.Keyword);
//				if (cates != null)
//                    {
//                       // lstcate = iCached.GetAll<ProductCategoryBO>(cates.Select(p -> p.CategoryId));
//                        if (lstcate != null)
//                        {
//                            var lst = lstcate.stream().filter(p -> p != null).map(p -> p);
//                            result.faceListCategory = lst.OrderByDescending(p -> GetGroupInCategory(p.CategoryID) == GetGroupInCategory(lst.First().CategoryID)).ToList();
//                        }
//                    }
//                }
//				List<ProductManuBO> lstmanu = null;// iCached.GetAll<ProductManuBO>(FacetLst.stream().filter(p -> p.Type == 2).map(p -> p.Key));
//				if (lstmanu != null) {
//					for (var item : lstmanu) {
//						if (ListManufacture == null)
//							ListManufacture = new ArrayList<ProductManuBO>();
//						if (item != null)
//							ListManufacture.add(item);
//					}
//				}
//                positionError = 1;

				if (productBOSR.productList != null) {

					Set<Integer> keySet = productBOSR.productList.keySet();
					List<Integer> idslst = new ArrayList<Integer>();
					for (Integer key : keySet) {
						var x = productBOSR.productList.get(key);
						idslst.add(x.ProductID);
					}

					var lstProductResult = phelper.GetSimpleProductListByListID_PriceStrings_soMap(Ints.toArray(idslst),
							query.SiteId, query.ProvinceId, query.LanguageID);
					try {
						if (lstProductResult != null
								&& Stream.of(lstProductResult).anyMatch(x -> x.CategoryID == 7264 && x.IsCoupleWatch)) {
							var lstCoupleKey = new ArrayList<Integer>();
							for (var item : lstProductResult) {
								if (item.CategoryID == 7264 && item.IsCoupleWatch
										&& item.ListCoupleWatchProductID != null) {
									for (var productid : item.ListCoupleWatchProductID) {
										lstCoupleKey.add(productid);

									}
								}
							}
							var listCouple = phelper.GetSimpleProductListByListID_PriceStrings_soMap(
									Ints.toArray(lstCoupleKey), query.SiteId, query.ProvinceId, query.LanguageID);
							for (var item : lstProductResult) {
								var getCouple = Stream.of(listCouple)
										.filter(x -> x.RepresentProductID == item.ProductID).toArray();
								item.ListCoupleWatchBO = (ProductBO[]) getCouple;
							}
						}

						if (lstProductResult != null && Stream.of(lstProductResult)
								.anyMatch(x -> x.CategoryID == 2002 && x.IsMultiAirConditioner)) {
							for (var item : lstProductResult) {
								if (item.IsMultiAirConditioner && item.MultiAirConditionerIds != null) {
									var lstMultiKey = new ArrayList<Integer>();
									for (var productid : item.MultiAirConditionerIds) {
										lstMultiKey.add(Integer.parseInt(productid));
									}
									var products = phelper.GetSimpleProductListByListID_PriceStrings_soMap(
											Ints.toArray(lstMultiKey), query.SiteId, query.ProvinceId,
											query.LanguageID);
									item.MultiAirConditioners = products;
								}
							}
						}
					} catch (Throwable e) {
						result = null;
					}
					result.productList = lstProductResult;
				}
			} catch (Throwable e) {
				String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
				result.message = "Failed: " + e.toString() + ": " + e.getMessage() + " - " + trace;
				status = HttpStatus.INTERNAL_SERVER_ERROR;

				var param = phelper.GetJsonFromObject(query);
				LogHelper.WriteLog(e, LogLevel.ERROR, param);

			}
		}
		var header = HeaderBuilder.buildHeaders(parsetimer, querytimer);
		return new ResponseEntity<ProductBOSR>(result, header, status);
	}

	@RequestMapping(value = "/getfeatureproductbycategories", method = RequestMethod.POST)
	public ResponseEntity<ProductBOSR> GetFeatureProductByCategories(@RequestBody QueryProductFeature query) {

		if (query.provinceID == 0)
			query.provinceID = 3;
		var time = new CodeTimers();
		time.start("all");
		var estimer = new CodeTimer("timer-es-all");
		var productHelper = GetProductClient();
		var status = HttpStatus.OK;
		estimer.reset();
		ProductBOSR productBO = null;
		try {
			productBO = productHelper.GetFeatureProductByCategories(query.homeOrCate, query.lstcateIds,
					query.provinceID, query.pageSize, query.pageIndex, query.isMobile, query.siteID, query.langID,
					query.lstChangePosition);

		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();
		}
		estimer.end();
		return new ResponseEntity<>(productBO, HeaderBuilder.buildHeaders(time), status);
	}

	@RequestMapping(value = "/getsuggestsearch", method = RequestMethod.GET)
	public ResponseEntity<SuggestSearchSO[]> GetSuggestSearch(String keyword, int siteID) {
		if (Strings.isNullOrEmpty(keyword)) {
			return null;
		}
		SuggestSearchSO[] result = null;
		var status = HttpStatus.OK;
		// List<Integer> listID = new ArrayList<>();

		try {

			result = GetProductClient().GetSuggestSearch(keyword, siteID);

		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<>(result, status);
	}

	@RequestMapping(value = "/getpropusemanualbyproduct", method = RequestMethod.GET)
	public ResponseEntity<ProductPropUseManualBO[]> GetPropUseManualByProductID(int productID, int siteID,
			String langID) {
		ProductPropUseManualBO[] result = null;
		var status = HttpStatus.OK;
		try {
			// tạm thời chưa sài tới, nhưng cứ để đó
			result = GetProductClient().GetPropUseManualByProductID(productID, siteID, langID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();
			LogHelper.WriteLog(e, LogLevel.ERROR);
		}
		return new ResponseEntity<>(result, status);
	}

//	@RequestMapping(value = "/getfullaccessorybyproductid", method = RequestMethod.GET)
//	public ResponseEntity<ProductBOSR> GetFullAccessoryByProductID(int productID, int provinceID, int siteID, Integer categoryID, String langID, Integer pageIndex, Integer pageSize ) {	
//		//Api cũ: GetFullAccessoryByProductIDDetail
//		var timer = new CodeTimers();
//		ProductBOSR result = null;
//		var status = HttpStatus.OK;
//		if(categoryID == null) categoryID = -1;
//		if(langID == null) langID = "vi-VN";
//		if(pageIndex == null) pageIndex = 0;
//		if(pageSize == null || pageSize > 30) pageSize = pageSize == null ? 10 : 30;
//			// tạm thời chưa sài tới, nhưng cứ để đó
//			result = GetProductClient().GetFullAccessoryByProductIDDetail(productID, provinceID, siteID, langID, categoryID, pageIndex, pageSize, timer);
//		try {
//			GetProductClient().getCateName(result,siteID,langID);
//		} catch (Throwable e) {
//			status = HttpStatus.INTERNAL_SERVER_ERROR;
//			e.printStackTrace();
//			LogHelper.WriteLog(e, LogLevel.ERROR);
//		}
//		var header = HeaderBuilder.buildHeaders(timer);
//		return new ResponseEntity<>(result, header, status);
//	}

	@RequestMapping(value = "/getfullaccessorybyproductid", method = RequestMethod.GET)
	public ResponseEntity<ProductBOSR> GetFullAccessoryByProductIDDetail(int productID, int provinceID, int siteID,
			Integer categoryID, String langID, Integer pageIndex, Integer pageSize) {
		// Api cũ: GetFullAccessoryByProductIDDetail
		var timer = new CodeTimers();
		ProductBOSR result = null;
		var status = HttpStatus.OK;
		if (categoryID == null)
			categoryID = -1;
		if (langID == null)
			langID = "vi-VN";
		if (pageIndex == null)
			pageIndex = 0;
		if (pageSize == null || pageSize > 30)
			pageSize = pageSize == null ? 10 : 30;
		// tạm thời chưa sài tới, nhưng cứ để đó
		try {
			result = GetProductClient().GetFullAccessoryByProductIDDetail2(productID, provinceID, siteID, langID,
					categoryID, pageIndex, pageSize, timer);

			GetProductClient().getCateName(result, siteID, langID);
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();
			LogHelper.WriteLog(e, LogLevel.ERROR);
		}
		var header = HeaderBuilder.buildHeaders(timer);
		return new ResponseEntity<>(result, header, status);
	}

	@RequestMapping(value = "/getallgroupcate", method = RequestMethod.GET)
	public ResponseEntity<GroupCategoryBO[]> getAllGroupCate(int siteID, String langID, Integer clearcache) {
		int clear = clearcache == null ? 0 : clearcache;
		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		var phelper = GetProductClient();
		GroupCategoryBO[] cached = null;
		try {
			// cached = phelper.getAllGroupCateFromCate(siteID,langID,clear);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(cached, HeaderBuilder.buildHeaders(timer), status);
	}

	@PostMapping(value = "/getmanufactureonsales")
	public ResponseEntity<ProductManuBO[]> getManufactureOnSales(@RequestBody ProductQuery productQuery) {
		var timers = new CodeTimers();
		var langID = productQuery.LanguageID;
		var status = HttpStatus.OK;
		if (Strings.isNullOrEmpty(langID)) {
			langID = "vi-VN";
		}
		ProductManuBO[] result = null;
		var phelper = GetProductClient();

		timers.start("all");

		try {
			productQuery.WebStatus = -5;
			var productSR = phelper.Ela_SearchProduct(productQuery, true, false, false, null);
			var listIdManu = Stream.of(productSR.faceListManu).mapToInt(x -> x.manufacturerID).toArray();
			if (listIdManu != null && listIdManu.length > 0) {
				result = phelper.getOrientClient().QueryFunction("manu_GetByIDList", ProductManuBO[].class, false,
						listIdManu, productQuery.SiteId, productQuery.LanguageID);
			}

		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timers.pause("all");
		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timers), status);

	}

	@GetMapping(value = "/getactiveaccessorymanu")
	public ResponseEntity<ProductManuBO[]> getActiveAccessoryManu(int siteID, String langID, String keyword) {
		var status = HttpStatus.OK;
		var timer = new CodeTimers();
		timer.start("all");
		ProductManuBO[] manu = null;
		try {
			var phelper = GetProductClient();
			timer.start("elastic");
			var qry = new ProductQuery() {
				{
					CategoryId = 0;
					LanguageID = langID;
					PageIndex = 0;
					PageSize = 1;
					ProvinceId = siteID == 6 ? 163 : 3;
					SiteId = siteID;
					SearchFlags = new HashSet<>();
					Keyword = keyword;
//					WebStatus = 4;
					WebStatusIDList = new int[] { 2, 4, 5, 11 };
				}
			};
			qry.SearchFlags.add(SearchFlag.ACCESSORY_ONLY);

			ProductSOSR search = phelper.Ela_SearchProduct(qry, true, false, false, timer);
			timer.pause("elastic");
			timer.start("odb");
			var idlist = Arrays.asList(search.faceListManu).stream().map(FaceManuSR::getID)
					.collect(Collectors.toList());
			manu = phelper.getOrientClient().QueryFunction("manu_GetByIDList", ProductManuBO[].class, false, idlist,
					siteID, langID);
			timer.pause("odb");
			timer.start("grouping");
			// manu nganh hang cha
			List<ProductManuBO> manulist = new ArrayList<>(Arrays.asList(manu));
			var grouped = manulist.stream().filter(x -> x.ManufacturerName != null)
					.collect(Collectors.groupingBy(x -> x.ManufacturerName));
			for (var list : grouped.values()) {
				ProductManuBO max = null;
				for (var m : list) {
					manulist.remove(m);
					if (max == null
							|| (m.BigLogo != null && (max.BigLogo == null || max.ManufactureID < m.ManufactureID))
							|| (max.BigLogo == null && m.BigLogo == null && max.ManufactureID < m.ManufactureID)) {
						max = m;
					}
				}
				manulist.add(max);
			}
			manu = manulist.stream().sorted(Comparator.comparingDouble(ProductManuBO::getDisplayOrder))
					.toArray(ProductManuBO[]::new);
			timer.pause("grouping");
			timer.pause("all");
		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<ProductManuBO[]>(manu, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/testt", method = RequestMethod.GET)
	public ResponseEntity<ProductBO> testt(int siteID) {

		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		var phelper = GetProductClient();
		ProductBO cached = null;
		try {
			var _cached = phelper.GetListCoupleWatch(siteID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(cached, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getListProductPromotionExpired", method = RequestMethod.GET)
	public ResponseEntity<List<ProductSO>> getListProductPromotionExpired() {

		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		var phelper = GetProductClient();
		List<ProductSO> cached = null;
		try {
			var _cached = phelper.getListProductPromotionExpired();
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(cached, HeaderBuilder.buildHeaders(timer), status);
	}

	@GetMapping(value = "/getpriceminmaxbycategory")
	public ResponseEntity<ProductPriceSO> getPriceMinAndMaxByCategory(int categoryId, int siteId, String langId,
			int provinceId, String manu, Boolean round) {
		ProductPriceSO produtPrice = null;
		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		try {
			produtPrice = GetProductClient().getPriceMinMax(categoryId, siteId, langId, provinceId, manu, round);
		} catch (Throwable throwable) {
			LogHelper.WriteLog(throwable);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<ProductPriceSO>(produtPrice, HeaderBuilder.buildHeaders(timer), status);
	}

	@GetMapping(value = "/getproducthaspricesempty")
	public ResponseEntity<int[]> getProductHasPricesEmpty(int siteId) {
		int[] productPriceNull = null;
		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		try {
			productPriceNull = GetProductClient().getPricesNull(siteId);
		} catch (Throwable throwable) {
			LogHelper.WriteLog(throwable);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(productPriceNull, HeaderBuilder.buildHeaders(timer), status);
	}

	@PostMapping(value = "/getproductbyproductidlist2021new")
	public ResponseEntity<ProductBOSR> getProductsByProductIDList2021New(@RequestBody ProductQuery qry) {

		CodeTimers timers = new CodeTimers();
		var status = HttpStatus.OK;
		ProductBOSR result = null;
		ProductSOSR solist = null;

		try {
			solist = GetProductClient().getProductsByProductIDList2021New(qry);
			if (solist != null) {

				result.message = solist.message;
				result.rowCount = solist.rowCount;
				result.faceListManu = solist.faceListManu;
				result.isNotSelling = solist.isNotSelling;

				List<Integer> lids = new ArrayList<Integer>();
				if (solist.productList != null) {
					solist.productList.forEach((k, v) -> {
						lids.add(k);

					});
				}
				timers.start("get-product-odb-all");
				int[] aids = lids.stream().sorted(Comparator.comparingInt(x -> x)).mapToInt(Integer::byteValue)
						.toArray();

				result.productList = GetProductClient().GetSimpleProductListByListID_PriceStrings_soMap(aids,
						qry.SiteId, qry.ProvinceId, qry.CategoryId, qry.LanguageID, timers);
				GetProductClient().getlistManu(result, qry.SiteId, qry.LanguageID);
				if (result.productList == null)
					result.message = "Failed: Lay detail tu OrientDB that bai";
			}
		} catch (Throwable e) {
			Logs.LogException(e);
		}
		return new ResponseEntity<ProductBOSR>(result, HeaderBuilder.buildHeaders(timers), status);
	}

	@GetMapping(value = "getpropvalueid")
	public ResponseEntity<int[]> getPropValueID(int propertyID, int siteID, String langID) {

		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		int[] result = null;

		try {

			result = GetProductClient().getPropValueId(propertyID, siteID, langID);

		} catch (Throwable e) {
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<int[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@GetMapping(value = "getlistproductidbysiteid")
	public ResponseEntity<String> getProductIDsBySiteId(int siteID) {

		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		String result = null;

		try {
			result = GetProductClient().getProductIdByHasBimageAndSite(siteID);
		} catch (Throwable e) {
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<String>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "getproductlistingbyid",  method = RequestMethod.POST)
	public ResponseEntity<ProductBOSR> GetProductListingByID(@RequestBody ProductListing query) {

		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		ProductBOSR result = null;

		try {
			if(query.listProductID != null && query.listProductID.length > 0 ){
				result = GetProductClient().GetProductListingByIDFromCache(query);
			}
			// result = GetProductClient().getProductIdByHasBimageAndSite(0);
		} catch (Throwable e) {
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<ProductBOSR>(result, HeaderBuilder.buildHeaders(timer), status);
	}
	
	@RequestMapping(value = "getstorebywardid",  method = RequestMethod.GET)
	public ResponseEntity<StoreBO[]> getStorebyWardID(int wardID) {
		var timer = new CodeTimers();
		var status = HttpStatus.OK;
		StoreBO [] result = null;
		try {
				result = GetProductClient().getStorebyWardID( wardID);
		} catch (Throwable e) {
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<StoreBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
	}



//	@RequestMapping(value = "/get", method = RequestMethod.GET)
//	public ResponseEntity<> get() {
//		 result = null;
//		var status = HttpStatus.OK;
//		
//		try {
//		} catch (Throwable e) {
//			status = HttpStatus.INTERNAL_SERVER_ERROR;
//			LogHelper.WriteLog(e);
//		}
//		return new ResponseEntity<>(result, status);
//	}

	@RequestMapping(value = "hgGetListStore", method =  RequestMethod.GET)
	public ResponseEntity<StoreBO[]> hgGetListStore (String ProductCode, int SiteID)
	{
		var timer = new CodeTimers();
		var phelper = GetProductClient();
		var status = HttpStatus.OK;
		StoreBO[] result = null;
		try {
			result =  phelper.hgGetListStore(ProductCode,SiteID);
		} catch (Throwable e) {
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return  new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "hgTest", method =  RequestMethod.GET)
	public ResponseEntity<StoreBO[]> hgTest (String ProductCode)
	{
		var timer = new CodeTimers();
		var phelper = GetProductClient();
		var status = HttpStatus.OK;
		StoreBO[] result = null;
		try {
			result =  phelper.hgTest(ProductCode);
		} catch (Throwable e) {
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return  new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
	}
}
