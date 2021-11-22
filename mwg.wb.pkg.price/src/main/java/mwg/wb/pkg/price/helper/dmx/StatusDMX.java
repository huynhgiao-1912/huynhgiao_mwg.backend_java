package mwg.wb.pkg.price.helper.dmx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;
import mwg.wb.business.InStockBO;
import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.helper.DmxPriceHelper;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.CodeTimers;
import mwg.wb.client.service.CrmServiceHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.RefSql;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.SqlInfoType;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.pm.StockStore;
import mwg.wb.model.pricedefault.PriceDefaultBO;
import mwg.wb.model.pricedefault.PriceDefaultGRBO;
import mwg.wb.model.products.PriceStock;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductCodeGalleryBO;
import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.model.products.SpecialSaleProgramBO;
import mwg.wb.model.promotion.Promotion;
import mwg.wb.model.promotion.PromotionString;
import mwg.wb.pkg.price.StatusV1;
import mwg.wb.pkg.price.helper.PriceSatusHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatusDMX implements Ididx {
	private String indexDB = "";
//	 private String typeDB = "product";
//
//	private ORThreadLocal factoryWrite = null;
//	private ORThreadLocal factoryRead = null;
	private PriceHelper priceHelper = null;
	private ProductHelper productHelper = null;
	private ObjectMapper mapper = null;
//	private ErpHelper erpHelper = null;
	private ClientConfig clientConfig = null;
	private int DataCenter = 0;
	private CrmServiceHelper crmHelper = null;

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {

//		factoryWrite = (ORThreadLocal) objectTransfer.factoryWrite;
//		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		priceHelper = (PriceHelper) objectTransfer.priceHelper;
		productHelper = (ProductHelper) objectTransfer.productHelper;
		mapper = (ObjectMapper) objectTransfer.mapper;
//		erpHelper = (ErpHelper) objectTransfer.erpHelper;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		indexDB = clientConfig.ELASTICSEARCH_PRODUCT_INDEX;
		crmHelper = (CrmServiceHelper) objectTransfer.crmHelper;
	}

	public static Map<String, String> g_listWebStatusId = new HashMap<String, String>();
	public static Map<Long, List<ProductErpPriceBO>> g_listPrice = new HashMap<Long, List<ProductErpPriceBO>>();
	public static Map<String, String> g_listES = new HashMap<String, String>();
	// public static final int[] arrCategoryGetCenterQuantity = { 4706, 8121, 8120,
	// 8119, 7720, 4697, 5612, 5475, 166,
//			3385, 4645, 2202, 1942, 1943, 1944, 2002, 1962, 2162, 482, 60, 57, 55, 58, 54, 1662, 1363, 1823, 56, 75, 86,
//			382, 346, 2429, 2823, 2824, 2825, 346, 3885, 1882, 5005, 4547, 1982, 3305, 2063, 1987, 7604, 8762 };
	public static final int[] arrCategoryDienMay = { 1942, 1943, 1944, 2002, 1962, 2202, 2022, 3385 };
	public static final int[] arrAccessoryCategory = { 482, 60, 57, 55, 58, 54, 1662, 1363, 1823, 56, 75, 86, 382, 346,
			2429, 2823, 2824, 2825, 346, 3885, 1882, 5005 };
	public static final int[] arrApplianceCategory = { 4645, 462, 1922, 1982, 1983, 1984, 1985, 1986, 1987, 1988, 1989,
			1990, 1991, 1992, 2062, 2063, 2064, 2084, 2222, 2262, 2302, 2322, 2342, 2142, 3305, 5473, 2428, 3385, 5105,
			7367, 5554, 7498 };
	public static final int[] arrSpecialCategory = { 4645 }; // NH xử lý đặc biệt

	private static List<Integer> cate364 = Arrays.asList(5693, 5697, 5698);
//	public static final int[] specproducts = new int[] { 108678, 105217, 88693, 105220, 105221, 105223, 105219, 105225,
//			105222, 105224, 105218, 105213, 105215, 105214, 105216, 105142, 105143, 105151, 105163, 105212, 105145,
//			105226, 105147, 105209, 105202, 105211, 105210, 104363, 104399, 104413, 104871, 104872, 104428, 104875,
//			104877, 104900, 104957, 105027, 105106, 105141 };
//	private static Set<Integer> vienthongGroupID = Set.of(13, 244, 22, 16, 17, 23, 184, 484, 525, 764, 905, 364, 1274,
//			1294);
//	private static Set<Integer> congkenhGroupID = Set.of(484, 305, 304, 1214, 1116);

	public int[] listOLOLStoreHub = { 2534, 173, 3927 };

	public ResultMessage Refresh(MessageQueue message) {
		CodeTimers timer = new CodeTimers();
		timer.start("all");
		DataCenter = message.DataCenter;
		ResultMessage msg = new ResultMessage();
		long ProductID = -1;
		try {

			String strNOTE = message.Note + "";
//			String strSource = message.Source + "";
			msg.Code = ResultCode.Success;
			ProductID = Utils.ToLong(message.Identify);
			timer.setLogging("StatusTimerDMX_" + ProductID);
			timer.start("check-exist");
			if (GConfig.ProductTaoLao.containsKey(ProductID)) {
				return msg;
			}
			
			if (!productHelper.CheckProductExistFromCache(ProductID)) {
				return msg;
			}
			timer.pause("check-exist");
			boolean isLog = false;

			if (strNOTE.contains("DIDX_TOP_LOG") || strNOTE.contains("DIDX_LOG")) {

				isLog = true;
			}

			if ((ProductID <= 0)) {
				Logs.Log(isLog, strNOTE, "ProductID <= 0" + ProductID);
				return msg;
			}

			boolean isGetCenterQuantity = true;

			int SiteID = 2;
			int BrandID = 2;
			int PriceArea = 13;
			String Lang = "vi-VN";
//			boolean v2 = strNOTE.contains("VERSION2");

			PriceArea = DidxHelper.getDefaultPriceAreaBySiteID(SiteID, Lang);

//			if (message.ProvinceID > 0 && message.BrandID > 0) {
//				Logs.Log(isLog, strNOTE, "CRMPushUpdateStockStore " + ProductID);
//				boolean rsP = CRMPushUpdateStockStore(isLog, strNOTE, ProductID, message.ProvinceID, message.BrandID,
//						message.Storeid);
//
//				if (!rsP) {
//					msg.Code = ResultCode.Retry;
//					msg.StackTrace = "PushUpdateStockStore false";
//					return msg;
//				}
//
//			}

			Logs.Log(isLog, strNOTE,
					"Refresh Pricestatus..." + ProductID + " PriceArea " + PriceArea + " SiteID" + SiteID);
//			

			List<ProductErpPriceBO> lstPrices = null;
			timer.start("GetListPriceByBySiteID");
			lstPrices = priceHelper.getListPriceStrings(ProductID, PriceArea, SiteID);
//					: priceHelper.GetListPriceByBySiteID(ProductID, PriceArea, SiteID);
			Logs.getInstance().Log(isLog, strNOTE, "GetListPriceByBySiteID", lstPrices);
			timer.pause("GetListPriceByBySiteID");
			List<ProductCodeGalleryBO> listColor = null;
			// if (DidxHelper.isBeta() || DidxHelper.isLocal()) {
			// productcode_gallery_getbyProductID
			timer.start("getProductCodeGallery");
			listColor = productHelper.getProductCodeGallery((int) ProductID);
			timer.pause("getProductCodeGallery");
			// }

//			if ((SiteID == 2)) {
//				Logs.Log(isLog, strNOTE, "SiteID==2");
//				return msg;
//			}
			// giá cho giá hãng
//			int priceAreaOrg = DidxHelper.getDefaultPriceAreaOrgBySiteID(SiteID, Lang);
//			List<ProductErpPriceBO> priceOrgList = null;

//			if (priceAreaOrg > 0) {

//				priceOrgList = priceHelper.GetListPriceByBySiteID(ProductID, priceAreaOrg, SiteID);

//			}

			if ((lstPrices == null || lstPrices.size() <= 0)) {
				timer.start("noprice");
				var productinfo = productHelper.getProductBOByProductID_PriceStrings((int) ProductID, SiteID, 3, "vi-VN", 0);
				// chỗ này có mục đích hết :D do sp đại diện hãng 42 ko khai báo hình
				if(productinfo != null && (productinfo.CategoryID == 2002 || productinfo.CategoryID == 7264)) {
					var productBO = productHelper.GetProduct_PriceStrings((int) ProductID, SiteID, 3, 0, "vi-VN",
							new mwg.wb.client.service.CodeTimers());
					if (productBO != null && ((productBO.CategoryID == 7264 && productBO.IsCoupleWatch
							&& productBO.ListCoupleWatchBO != null && productBO.ListCoupleWatchBO.length > 0)
							|| (productBO.CategoryID == 2002 && productBO.IsMultiAirConditioner
							&& productBO.MultiAirConditioners != null
							&& productBO.MultiAirConditioners.length >= 3))
						// && (DidxHelper.isBeta() || DidxHelper.isHanh())
					) {
						var pricesMap = RefresPriceRepresent(productBO, message.SiteID);
						if (pricesMap != null && pricesMap.size() > 0) {
							Date createdate = priceHelper.GetCreatedDateFromCache(ProductID);
							int order = PriceSatusHelper.GetOrder(4, createdate, new Date(), SiteID, Lang);
							Logs.Log(isLog, strNOTE, "IndexDataListPriceDefault order " + order);
							// tạm thời để tồn = 0getPriceAfterPromotion
							boolean rsEs = IndexDataListPriceDefault_ES(isLog, strNOTE, pricesMap, ProductID, SiteID, Lang,
									order, 0);
							if (rsEs == false) {
								Logs.WriteLine("IndexDataListPriceDefault false");
								msg.Code = ResultCode.Retry;
								msg.StackTrace = "IndexDataListPriceDefault false";
								Logs.Log(isLog, strNOTE, "IndexDataListPriceDefault false ResultCode.Retry ");
								return msg;
							}
							msg.Code = ResultCode.Success;
							return msg;
						}
					}
				} else if(productinfo != null && productinfo.CategoryID == 42
//						&& productinfo.ManufactureID == 80
				){ //&& (DidxHelper.isBeta() || DidxHelper.isHanh() )
					// điện thoại iphone thì gộp sản phẩm
					RefreshDelPrice(message);
					var pricesMap = productHelper.combinedProduct(productinfo, message.SiteID, message.Lang);
					if (pricesMap != null && pricesMap.size() > 0) {
						Date createdate = priceHelper.GetCreatedDateFromCache(ProductID);
						int order = PriceSatusHelper.GetOrder(4, createdate, new Date(), SiteID, Lang);
						Logs.Log(isLog, strNOTE, "IndexDataListPriceDefault order " + order);
						// tạm thời để tồn = 0
						boolean rsEs = IndexDataListPriceDefault_ES(isLog, strNOTE, pricesMap, ProductID, SiteID, Lang,
								order, 0);
						if (rsEs == false) {
							msg.Code = ResultCode.Retry;
							msg.StackTrace = "combinedProduct false";
							Logs.Log(isLog, strNOTE, "combinedProduct false ResultCode.Retry ");
							return msg;
						}
					}
					msg.Code = ResultCode.Success;
					return msg;
				}

				Logs.Log(isLog, strNOTE, "lstPrices.size <0");
				timer.pause("noprice");
				return RefreshDelPrice(message);
			}

			List<ProductErpPriceBO> listFinalPrices = new ArrayList<ProductErpPriceBO>();

			List<String> listCode = lstPrices.stream().map(e -> e.ProductCode.trim()).distinct()
					.collect(Collectors.toList());

			List<InStockBO> listAllStock = new ArrayList<InStockBO>();
			timer.start("getCategoryID-ByProductID");
			int categoryID = productHelper.GetCategoryIDByProductID(ProductID);
			timer.pause("getCategoryID-ByProductID");
			List<Integer> prov364 = cate364.contains(categoryID) ? Ints.asList(productHelper.getCachedProvince364())
					: new ArrayList<>();
			Logs.Log(isLog, strNOTE, "categoryID = : " + categoryID);

			// get all stock by code
			int brand = BrandID;
			// var mainSubgroup = productHelper.getMainSubGroupIDByProductID(ProductID);
			// int maingroupid = productHelper.getMainGroupIDByProductID(ProductID);
			timer.start("getAllInfo-Stock");
			for (String item : listCode) {
				List<InStockBO> listStock = null;
				boolean fromCodeStock = false;
				// lọc TH đặc biệt lấy cả 2 site tgdđ + đmx: lấy của đmx.
				if (SiteID == 1 || SiteID == 2) {
//					if (mainSubgroup != null) {
//						if ((mainSubgroup.maingroupid == 484 && mainSubgroup.subgroupid == 3799)
//								|| congkenhGroupID.contains(mainSubgroup.maingroupid)
//								|| vienthongGroupID.contains(mainSubgroup.maingroupid)) {
					listStock = priceHelper.getStockByProductCodeNew(item, 2);
					fromCodeStock = true;
//						}
//					}
				}
				// Các TH thông thường: lấy từ pm_currentinstock
				if (!fromCodeStock) {
					listStock = priceHelper.GetStockByProductCode(item, brand);
				}
//				if ((brand == 1 || brand == 2) && vienthongGroupID.contains(maingroupid)) {
//					var clone = listStock;
//					listStock = new ArrayList<>();
//					listStock.addAll(priceHelper.GetStockByProductCode(item, brand == 1 ? 2 : 1));
//					if (clone != null)
//						listStock.addAll(clone);
//				}

				if ((listStock != null)) {
					listAllStock.addAll(
							listStock.stream().filter(x -> x.inventorystatusid == 1).collect(Collectors.toList()));
				}

			}
			timer.pause("getAllInfo-Stock");
			// Giá mặc định: là những code(object giá) trong list giá ưu tiên kd... theo
			// tỉnh thành, (tỉnh thành có nhiều code)
			Logs.Log(isLog, strNOTE, "ProductErpPriceBO item : lstPrices");
			// var productBO = productHelper.GetProductBOByProductID((int) ProductID,
			// SiteID, 3, Lang, 0);
			ProductBO productBO = null;
			try {
				timer.start("GetProductInfoForStatusDMXFromCache");
				productBO = productHelper.GetProductInfoForStatusDMXFromCache(ProductID, SiteID, Lang);
				timer.pause("GetProductInfoForStatusDMXFromCache");
				// productBO = productHelper.GetProductBOByProductIDSE(ProductID, SiteID, Lang);

			} catch (Throwable e2) {
				Logs.LogException(e2);
				msg.Code = ResultCode.Retry;
				return msg;
			}
			if (productBO == null) {

				msg.Code = ResultCode.Success;
				return msg;
			}
			int prCategoryId = productBO.CategoryID;
//			if (Arrays.stream(arrCategoryGetCenterQuantity).anyMatch(x -> x == CategoryID)) {
//				isGetCenterQuantity = true;
//			}
//
//			if (Arrays.stream(specproducts).anyMatch(x -> x == ProductID)) {
//				isGetCenterQuantity = true;
//			}

			timer.start("mainprocessquanque");

			int quantityOLOLHub = 0;
			boolean isGetStockOLOLHub = false;

			Map<String, ProductErpPriceBO> sCpde = new HashMap<String, ProductErpPriceBO>();
			Map<String, ProductErpPriceBO> sCodeListOnly = new HashMap<String, ProductErpPriceBO>();
			for (ProductErpPriceBO objPriceNews : lstPrices) {
				objPriceNews.CategoryId =  categoryID;
				String k = objPriceNews.ProductCode + "-" + objPriceNews.CategoryId;
				if (!sCpde.containsKey(k))
					sCpde.put(k, objPriceNews);

				String k2 = objPriceNews.ProductCode;
				if (!sCodeListOnly.containsKey(k))
					sCodeListOnly.put(k2, objPriceNews);

			}

			Map<String, SpecialSaleProgramBO> rsSpecialSaleProgramBO = new HashMap<String, SpecialSaleProgramBO>();
			// for (String itemProductCode : listCode) {
			for (ProductErpPriceBO itemPrice : sCpde.values()) {
				SpecialSaleProgramBO specialSale = productHelper.getSpecialSaleProgramCU(itemPrice.ProductCode, 1);
				rsSpecialSaleProgramBO.put(itemPrice.ProductCode, specialSale);
				// tonkho onlineonly (chi lay 1 lan theo sieuthi) - thanhphi
				// //yeu cau: https://bit.ly/2US9T6G

				if (!isGetStockOLOLHub && productBO != null && DmxPriceHelper.categoryOlOlHub.contains(prCategoryId)
						&& PriceSatusHelper.isValidProgram(specialSale))// chi lay 1 lan
				{
					// get store olol hub
					try {
						int[] storesOlOlHub = productHelper.GetListStoresOnlineOnly(itemPrice.ProductCode);
						if (storesOlOlHub != null && storesOlOlHub.length > 0) {
							listOLOLStoreHub = storesOlOlHub;
						}

					} catch (Throwable exx) {
						Logs.LogException(exx);
						msg.Code = ResultCode.Retry;
						return msg;
					}

					if (productBO.CategoryID == 5697) {
						StockStore stockStore = null;
						try {
							stockStore = productHelper.GetStockByStore(itemPrice.ProductCode, 463);

						} catch (Throwable e1) {
							Logs.LogException(e1);
							msg.Code = ResultCode.Retry;
							return msg;
						}

						if (stockStore != null) {
							// quantityOLOLHub = quantityOLOLHub + stockStore.quantity;
							itemPrice.quantityOLOLHub += stockStore.quantity;
						}

					} else {

						if (listOLOLStoreHub != null && listOLOLStoreHub.length > 0) {
							StockStore[] stockStore = productHelper.GetStockByListStore(itemPrice.ProductCode,
									listOLOLStoreHub);
							if (stockStore != null && stockStore.length > 0) {
								itemPrice.quantityOLOLHub = stockStore[0].quantity;// Stream.of(stockStore).mapToInt(x
																					// -> x.quantity).sum();//.filter(x
																					// -> (x.provinceid +
																					// "").equals(productcode))
							}
						}
					}
				}
			}

			Date now = new Date();
			for (ProductErpPriceBO item : lstPrices) {

				timer.start("mainprocess-1" + "_" + item.ProductCode + "-p" + item.ProvinceId);
				
				var getpriceHasProcess = sCpde.get(item.ProductCode + "-" + item.CategoryId);
				if (getpriceHasProcess != null) {
					item.codeIsOnlineOnly = getpriceHasProcess.codeIsOnlineOnly;
					item.IsOnlineOnly = getpriceHasProcess.IsOnlineOnly;
					item.quantityOLOLHub = getpriceHasProcess.quantityOLOLHub;
				} else {
					if (rsSpecialSaleProgramBO.containsKey(item.ProductCode)) {
						SpecialSaleProgramBO ssp = rsSpecialSaleProgramBO.get(item.ProductCode);
						item.codeIsOnlineOnly = ssp != null && !Strings.isNullOrEmpty(ssp.SpecialSaleProgramName)
								&& ssp.BeginDate.before(now) && ssp.EndDate.after(now);
						item.IsOnlineOnly = item.codeIsOnlineOnly;
					}
					item.quantityOLOLHub = -1;
				}

				if (listColor != null && listColor.size() > 0) {
					// for (ProductErpPriceBO item : lstPrices) {
					var tmp = listColor.stream().filter(x -> x.ProductCode.equals(item.ProductCode)).findFirst()
							.orElse(null);
					if (tmp != null) {
						item.Image = tmp.Image;
						item.Bimage = tmp.Bimage;
						item.Mimage = tmp.Mimage;
						item.Simage = tmp.Simage;
						item.DisplayOrder = tmp.DisplayOrder;
						item.IsUseAvatar = tmp.IsUseAvatar;
					}
					// }
				}
				timer.pause("mainprocess-1" + "_" + item.ProductCode + "-p" + item.ProvinceId);
				
				timer.start("mainprocess-2" + "_" + item.ProductCode + "-p" + item.ProvinceId);
				List<InStockBO> listStock = listAllStock.stream()
						.filter(x -> x.productcode.trim().equals(item.ProductCode.trim())).collect(Collectors.toList());

				if (((item != null) && (item.ProductId != ProductID))) {
					Logs.Log(isLog, strNOTE, item.RecordID + " ProductID không khớp");
//						return msg;
					continue;
				}
				timer.pause("mainprocess-2" + "_" + item.ProductCode + "-p" + item.ProvinceId);

				timer.start("mainprocess-3" + "_" + item.ProductCode + "-p" + item.ProvinceId);
				
				// chỉ cộng tồn kho trung tâm ở 1 số nghành hàng điện máy và phụ kiện
//				var intInstockCenterInCountry = listAllStock.stream().mapToInt(x -> x.centerquantity).sum();
				var intTotalSampleQuantity = listAllStock.stream().mapToInt(x -> x.samplequantity).filter(x -> x > 0)
						.sum();

				int intInstockInCountry = 0;
				if (isGetCenterQuantity) {
					intInstockInCountry = listAllStock.stream().mapToInt(
							c -> (c.quantity > 0 ? c.quantity : 0) + (c.centerquantity > 0 ? c.centerquantity : 0))
							.sum();
				} else {
					intInstockInCountry = listAllStock.stream().mapToInt(c -> c.quantity).filter(x -> x > 0).sum();
				}

				List<InStockBO> objInstockTmp = new ArrayList<InStockBO>();
				for (InStockBO stock : listStock) {
					if (stock != null && !stock.productcode.isEmpty() && stock.provinceid > 0) {
						if (stock.provinceid == item.ProvinceId
								&& stock.productcode.trim().equals(item.ProductCode.trim())) {
							objInstockTmp.add(stock);
						}
					}
				}
				timer.pause("mainprocess-3" + "_" + item.ProductCode + "-p" + item.ProvinceId);

				timer.start("mainprocess-4" + "_" + item.ProductCode + "-p" + item.ProvinceId);
				int intInstockInCountryByProductCode = 0;
				List<InStockBO> lstInstocksInCountry = listStock.stream()
						.filter(i -> ((i != null) && i.productcode.trim().equals(item.ProductCode.trim())))
						.collect(Collectors.toList());

				if ((CollectionUtils.isNotEmpty(lstInstocksInCountry))) {
					if (isGetCenterQuantity) {
						intInstockInCountryByProductCode = lstInstocksInCountry.stream().mapToInt(
								c -> (c.quantity > 0 ? c.quantity : 0) + (c.centerquantity > 0 ? c.centerquantity : 0))
								.sum();

					} else {
						intInstockInCountryByProductCode = lstInstocksInCountry.stream().mapToInt(c -> c.quantity)
								.filter(x -> x > 0).sum();

					}
				}
				timer.pause("mainprocess-4" + "_" + item.ProductCode + "-p" + item.ProvinceId);
				
				timer.start("mainprocess-5" + "_" + item.ProductCode + "-p" + item.ProvinceId);
				ProductErpPriceBO objPriceNews = item;
				objPriceNews.TotalQuantity = intInstockInCountry;
				objPriceNews.ProductCodeTotalQuantity = intInstockInCountryByProductCode;
				InStockBO objInstock = CollectionUtils.isNotEmpty(objInstockTmp)
						? objInstockTmp.stream().findFirst().orElse(null)
						: null;
				if ((objInstock == null)) {
					objPriceNews.Quantity = 0;
				} else {
//					objPriceNews.WebMinStock = objInstock.webminstock;
					objPriceNews.Quantity = objInstock.quantity;
					// dang la ProductCodeQuantity
					objPriceNews.ProductCodeTotalQuantity = intInstockInCountryByProductCode;
					objPriceNews.ProductCodeQuantity = objInstock.quantity;

					if (isGetCenterQuantity) {
						objPriceNews.CenterQuantity = objInstock.centerquantity;
					} else {
						objPriceNews.CenterQuantity = 0;
					}
					objPriceNews.TotalSampleQuantity = intTotalSampleQuantity;
					objPriceNews.SampleQuantity1 = objInstock.samplequantity;
				}
				timer.pause("mainprocess-5" + "_" + item.ProductCode + "-p" + item.ProvinceId);

//				 lay thong tin olol
//				 var codeInfo =
//				 productHelper.GetPm_ProductFromCache(objPriceNews.ProductCode);
//				 objPriceNews.specialSale = productHelper.getSpecialSaleProgram2(codeInfo,
//				 objPriceNews.ProductCode, 1)
				timer.start("mainprocess-6-1" + "_" + item.ProductCode + "-p" + item.ProvinceId);
				if (rsSpecialSaleProgramBO.containsKey(item.ProductCode)) {
					objPriceNews.specialSale = rsSpecialSaleProgramBO.get(item.ProductCode);
					productBO.SpecialSaleProgram = objPriceNews.specialSale;
				}
				timer.pause("mainprocess-6-1" + "_" + item.ProductCode + "-p" + item.ProvinceId);

				// debug
//                if (objPriceNews.ProvinceId == 128) {
//                    int debug = 1;
//                }

				// lấy tồn kho tại các tỉnh thành liên quan nếu tỉnh thành hiện tại hết hàng
				timer.start("mainprocess-7" + "_" + item.ProductCode + "-p" + item.ProvinceId);
				if ((objPriceNews.TotalQuantity >= objPriceNews.WebMinStock
						|| (objPriceNews.StatusId != 5 && objPriceNews.StatusId != 0 && objPriceNews.StatusId != 6))
						&& objPriceNews.Price > 0 && objPriceNews.Quantity <= 0 && objPriceNews.CenterQuantity <= 0) {
//					&& productBO != null
//							&& (Arrays.stream(arrCategoryDienMay).anyMatch(x -> x == CategoryID)
//									|| Arrays.stream(arrApplianceCategory).anyMatch(x -> x == CategoryID))
//							&& !Arrays.stream(arrSpecialCategory).anyMatch(x -> x == CategoryID)

//					if (DataCenter != 3) {// beta không co cai nay
					var fobjPriceNews = objPriceNews;
					try {
						List<Integer> provincerelate = crmHelper
								.GetListProvinceHasProductInStock(objPriceNews.ProductCode, objPriceNews.ProvinceId);

						if (provincerelate != null && provincerelate.size() > 0) {
							var lstInstocksProvinceRelate = listAllStock.stream()
									.filter(p -> fobjPriceNews.ProductCode.trim().equals(p.productcode.trim())
											&& provincerelate.contains(p.provinceid));

							if (isGetCenterQuantity) {
								objPriceNews.TotalQuantityRelateProvince = lstInstocksProvinceRelate
										.mapToInt(c -> (c.storechangequantity > 0 ? c.storechangequantity : 0)
												+ (c.storechangecenterquantity > 0 ? c.storechangecenterquantity : 0))
										.sum();
							} else {
								objPriceNews.TotalQuantityRelateProvince = lstInstocksProvinceRelate
										.mapToInt(c -> c.storechangequantity).filter(x -> x > 0).sum();
							}
						}
					} catch (JSONException ignored) {
					}
//					}
				}

				objPriceNews.is364province = prov364.contains(objPriceNews.ProvinceId);
				timer.pause("mainprocess-7" + "_" + item.ProductCode + "-p" + item.ProvinceId);

				timer.start("mainprocess-8" + "_" + item.ProductCode + "-p" + item.ProvinceId);
				if (productBO != null) {
					objPriceNews.manufacturerID = (int) productBO.ManufactureID;
					objPriceNews.CategoryId = productBO.CategoryID;
					objPriceNews.isSetupProduct = productBO.isSetupProduct;
				}
				objPriceNews = PriceSatusHelper.ProcessProductStatusDMX(objPriceNews, SiteID,
						productBO == null ? 0 : (int) productBO.CategoryID, objPriceNews.quantityOLOLHub);

				listFinalPrices.add(objPriceNews);
				timer.pause("mainprocess-8" + "_" + item.ProductCode + "-p" + item.ProvinceId);
				System.out.println(item.ProvinceId);
				// timer.pause("mainprocess-"+ item.ProductCode);
			}
			timer.pause("mainprocessquanque");

			if ((listFinalPrices == null || listFinalPrices.size() <= 0)) {
				Logs.WriteLine("listFinalPrices.size <0");
				return RefreshDelPrice(message);
				// return msg;
			}

			timer.start("mainprocess-x");
			List<ProductErpPriceBO> finalListPriceDefault = new ArrayList<ProductErpPriceBO>();

			List<Integer> lstProvinceID = listFinalPrices.stream().filter(e -> e != null).mapToInt(e -> e.ProvinceId)
					.distinct().boxed().collect(Collectors.toList());
			int statusid = 1;
			Date arrival = null;
			String md5 = "";
			Logs.getInstance().Log(isLog, strNOTE, "listFinalPrices", listFinalPrices);

			timer.pause("mainprocess-x");
			timer.start("mainprocess-lstProvinceID");
			for (int item : lstProvinceID) {

				// if (item != 3) continue;//
				List<ProductErpPriceBO> listPriceByProvince = listFinalPrices.stream().filter(x -> x.ProvinceId == item)
						.collect(Collectors.toList());
				ProductErpPriceBO priceDefault = PriceSatusHelper.GetPriceDefault(listPriceByProvince, SiteID, Lang);
				if ((priceDefault == null)) {
					Logs.Log(isLog, strNOTE, "ProvinceID " + item + " priceDefault == null ");
					// Ng�ng kinh doanh
					priceDefault = new ProductErpPriceBO();
					priceDefault.Price = 0.0;
					priceDefault.WebStatusId = 1;
					priceDefault.ProductId = ProductID;
					priceDefault.ProvinceId = item;
				}

				finalListPriceDefault.add(priceDefault);
				md5 = md5 + priceDefault.ProvinceId + "_" + priceDefault.Price + "_" + priceDefault.WebStatusId
						+ priceDefault.IsShowHome;

			}
			timer.pause("mainprocess-lstProvinceID");
			Logs.Log(isLog, strNOTE, "===============================");
			Logs.getInstance().Log(isLog, strNOTE, "finalListPriceDefault object", finalListPriceDefault);
			boolean isUpdateES = true;
			// test lai
			String esKeyTerm = ProductID + "_" + SiteID + "_" + (Lang.toLowerCase().replaceAll("-", "_"));
			timer.start("mainprocess-synchronizedStatusDMX");
			synchronized (StatusDMX.class) {

				if (g_listWebStatusId.containsKey(esKeyTerm)) {

					String k = g_listWebStatusId.get(esKeyTerm);
					if (!Utils.StringIsEmpty(md5)) {
						if (k.equals(md5)) {
							isUpdateES = false;
						} else {

						}
					}
				} else {

				}
			}
			timer.pause("mainprocess-synchronizedStatusDMX");
			Map<String, Object> pricesMap = new HashMap<String, Object>();
			pricesMap.put("didx_updatestatusdate",Utils.GetCurrentDate());
			int defaultCodeQuantity = -1;
			Logs.getInstance().Log(isLog, strNOTE, "finalListPriceDefault", finalListPriceDefault);
			timer.start("forfinalListPriceDefault");

			Map<String, PromotionString[]> PromosionOnly = new HashMap<String, PromotionString[]>();
			for (String peProductCode : sCodeListOnly.keySet()) {
				var promotionstr = productHelper.GetPromotionByCode(peProductCode, message.SiteID, message.Lang);
				if (promotionstr != null && promotionstr.length > 0) {
					PromosionOnly.put(peProductCode, promotionstr);
				}
			}

			Map<Integer, PriceDefaultBO> pricesListdetaultByProvince = new HashMap<>();
			int priceDefaultSize = finalListPriceDefault.size();
			for (int i = 0; i < priceDefaultSize; i++) {
				var item = finalListPriceDefault.get(i);
				PriceDefaultBO deF = new PriceDefaultBO();
				pricesMap.put("IsShowHome_" + item.ProvinceId, item.IsShowHome ? 1 : 0);
				pricesMap.put("WebStatusId_" + item.ProvinceId, item.WebStatusId);
				pricesMap.put("Price_" + item.ProvinceId, item.Price);
				pricesMap.put("ProductCode_" + item.ProvinceId, item.ProductCode);

				deF.WebStatusId = item.WebStatusId;
				deF.ProductID = ProductID;
				deF.IsShowHome = item.IsShowHome ? 1 : 0;
				deF.ProductCode = item.ProductCode;
				deF.Price = item.Price;

				deF.ProvinceId = item.ProvinceId;
				deF.SiteID = SiteID;
				deF.RecordID = SiteID + "_" + Lang + "_" + ProductID + "_" + item.ProvinceId;
				deF.LangID = Lang;

				// if (DidxHelper.isBeta() || DidxHelper.isLocal()) {
				deF.DisplayOrder = item.DisplayOrder;
				deF.Image = item.Image;
				deF.Bimage = item.Bimage;
				deF.Mimage = item.Mimage;
				deF.Simage = item.Simage;
				// }

				if (defaultCodeQuantity < 0) {
					defaultCodeQuantity = item.ProductCodeTotalQuantity;
				}

				pricesListdetaultByProvince.put(item.ProvinceId, deF);

//				if(DidxHelper.isLocal() || DidxHelper.isBeta() || true) {
				// try {
				// init priceafterpromotion
				PromotionString[] promotionstr = PromosionOnly.containsKey(item.ProductCode)
						? PromosionOnly.get(item.ProductCode)
						: null;
				// var promotionstr = productHelper.GetPromotionByCode(item.ProductCode,
				// message.SiteID, message.Lang);
				if (promotionstr != null && promotionstr.length > 0) {
					// var json = mapper.writeValueAsString(pricesMap);
					var promotion = mapper.readValue(promotionstr[0].data, Promotion[].class);

					if (promotion != null && promotion.length > 0) {
						for (Promotion promotion2 : promotion) {
							promotion2.provinceIDList = "," + promotion2.provinceIDList + ",";
						}
						// truyền province = 3
						// var productbomap =
						// productHelper.getProductBOByProductID_PriceStrings((int)ProductID,
						// message.SiteID, 3, message.Lang, 0);
						var productbomap = new ProductBO() {
							{
								CategoryID = item.CategoryId;
								this.ProductID = ProductID;
							}
						};
						// for (var provinceid : lstProvinceID) {
						var provinceid = item.ProvinceId;
						var price = finalListPriceDefault.stream().filter(x -> x.ProvinceId == provinceid).findFirst()
								.orElse(null);
						if (price == null || price.Price <= 0) {
							pricesMap.put("PriceAfterPromotion_" + provinceid + "_" + message.SiteID, 0);
							pricesMap.put("PromotionDiscountPercent_" + provinceid, 0);
							continue;
						}
						double PriceAffterPromotion = 0;
						double newPrice = price.Price;
						double totalDiscount = 0;

						if ((int) price.WebStatusId != 4 && (int) price.WebStatusId != 11
								&& (int) price.WebStatusId != 2 && (int) price.WebStatusId != 8)// khac kinh
																								// doanh,chuyen hang,dat
																								// truoc thi khong gach
																								// gia
						{
							pricesMap.put("PriceAfterPromotion_" + provinceid + "_" + message.SiteID, newPrice);
							pricesMap.put("PromotionDiscountPercent_" + provinceid, 0);
							continue;
						}

						// lay km theo tinh thanh
						var afterLstPromotion = Stream.of(promotion)
								.filter(x -> x.provinceIDList.contains("," + provinceid + ",")
										&& (x.ToPrice <= 0.0 || (x.ToPrice > 0 && x.FromPrice <= newPrice && x.ToPrice >= newPrice)))
								.collect(Collectors.toList());
						if (afterLstPromotion == null || afterLstPromotion.size() == 0) {
							pricesMap.put("PriceAfterPromotion_" + provinceid + "_" + message.SiteID, newPrice);
							pricesMap.put("PromotionDiscountPercent_" + provinceid, 0);
							continue;
						}
						// loại km chọn
						var lstKmChon = afterLstPromotion.stream()
								.collect(Collectors.groupingBy(x -> new PromotionGroupBy(x.PromotionID, x.GroupID)))
								.values().stream().filter(x -> x.size() > 1).collect(Collectors.toList());
						List<Integer> listPromotionIDKmChon = new ArrayList<Integer>();
						if (lstKmChon != null && lstKmChon.size() > 0) {

							for (var kmChon : lstKmChon) {
								if (kmChon.size() > 1) {
									for (var x : kmChon) {
										listPromotionIDKmChon.add(x.PromotionID);
									}
								}
							}
						}
						if (listPromotionIDKmChon.size() > 0) {
							afterLstPromotion = afterLstPromotion.stream()
									.filter(x -> listPromotionIDKmChon.contains(x.PromotionID)
											&& x.FromPrice <= newPrice && x.ToPrice >= newPrice)
									.collect(Collectors.toList());
						}
						var promotionSaving = afterLstPromotion.stream().filter(x -> x.ISONLYFORSPECIALSALEPROGRAM == 0)
								.filter(x -> x.PromotionType != 2).filter(x -> x.IsApplyByTimes == 0)
								.filter(x -> x.PromotionType < 18).filter(x -> x.IsCheckSalePrice == 0)
								.filter(x -> x.SavingProgramOutputCount > 0
										&& x.SavingProgramOutputCount == x.PromotionOutputTypeCount)
//								  day la dk km saving
								.filter(x -> !(x.SpecialOutputTypeCount > 0
										&& x.SpecialOutputTypeCount == x.PromotionOutputTypeCount))
//								 .sorted(Comparator.<Promotion>comparingInt(x ->
//								 Integer.parseInt(x.ReturnValue)))
								.sorted(Comparator.comparingDouble(Promotion::getReturnValue).reversed())
								.collect(Collectors.toList());
						if (promotionSaving != null && promotionSaving.size() > 0) {
							for (Promotion pro : promotionSaving) {
								totalDiscount += CaculateTotalDiscountSimple(pro, newPrice);
							}
						} else {
							// km giảm giá
							afterLstPromotion = afterLstPromotion.stream().filter(x -> IsDiscountPromotionGiaGach(x))
									.collect(Collectors.toList());
							for (Promotion pro : afterLstPromotion) {
								totalDiscount += CaculateTotalDiscountSimple(pro, newPrice);
							}
						}
						if (afterLstPromotion == null || afterLstPromotion.size() == 0) {
							pricesMap.put("PriceAfterPromotion_" + provinceid + "_" + message.SiteID, newPrice);
							pricesMap.put("PromotionDiscountPercent_" + provinceid, 0);
							continue;
						} else {
							pricesMap.put("PriceAfterPromotion_" + provinceid + "_" + message.SiteID,
									newPrice - totalDiscount);
							pricesMap.put("PromotionDiscountPercent_" + provinceid,
									Math.floor(totalDiscount / newPrice * 100));
						}

						// bo rang >60%
//                        if (IsValidDiscount(productbomap, (totalDiscount / newPrice) * 100)) {
//
//                        } else {
//                            pricesMap.put("PriceAfterPromotion_" + provinceid + "_" + message.SiteID, newPrice);
//                            pricesMap.put("PromotionDiscountPercent_" + provinceid, 0);
//                        }
						// pricesMap.put("PriceAfterPromotion_" + provinceid + "_" + message.SiteID,
						// PriceAffterPromotion);
						// }
					} else {
						var price = finalListPriceDefault.stream().filter(x -> x.ProvinceId == item.ProvinceId)
								.findFirst().orElse(null);
						double pricez = price == null ? 0 : price.Price;
						pricesMap.put("PriceAfterPromotion_" + item.ProvinceId + "_" + message.SiteID, pricez);
						pricesMap.put("PromotionDiscountPercent_" + item.ProvinceId, 0);
					}
				} else {
					var price = finalListPriceDefault.stream().filter(x -> x.ProvinceId == item.ProvinceId).findFirst()
							.orElse(null);
					double pricez = price == null ? 0 : price.Price;
					pricesMap.put("PriceAfterPromotion_" + item.ProvinceId + "_" + message.SiteID, pricez);
					pricesMap.put("PromotionDiscountPercent_" + item.ProvinceId, 0);
				}
//				}catch (Exception e) {
//					e.printStackTrace();
//				}

				Logs.Log(isLog, strNOTE, "deF.ProvinceId " + deF.ProvinceId + "deF.Price " + deF.Price
						+ " deF.WebStatusId" + deF.WebStatusId);

			}
			timer.pause("forfinalListPriceDefault");
			timer.start("mainprocess-IndexDataListPriceDefault");
			isUpdateES = true;
			if (isUpdateES == false) {
				Logs.Log(isLog, strNOTE, "SKIP IndexDataListPriceDefault order ");
			} else {
				Date createdate = priceHelper.GetCreatedDateFromCache(ProductID);
				int order = PriceSatusHelper.GetOrder(statusid, createdate, arrival, SiteID, Lang);
				Logs.Log(isLog, strNOTE, "IndexDataListPriceDefault order " + order);
				if (pricesMap != null && pricesMap.size() > 0) {
					boolean rsEs = IndexDataListPriceDefault_ES(message.ProvinceID, isLog, strNOTE, pricesMap,
							ProductID, SiteID, Lang, order, defaultCodeQuantity);
					if (rsEs == false) {
						Logs.WriteLine("IndexDataListPriceDefault false");
						msg.Code = ResultCode.Retry;
						msg.StackTrace = "IndexDataListPriceDefault false";
						Logs.Log(isLog, strNOTE, "IndexDataListPriceDefault false ResultCode.Retry ");
						return msg;
					}
				}

				synchronized (StatusDMX.class) {
					if (!Utils.StringIsEmpty(md5)) {
						g_listWebStatusId.put(esKeyTerm, md5);
					}

				}

			}
			timer.pause("mainprocess-IndexDataListPriceDefault");
			// init trang thai len db
			timer.start("mainprocess- pricesListdetaultByProvince");
			var dbprice = pricesListdetaultByProvince.values().stream()
					.sorted(Comparator.comparingInt(x -> x.WebStatusId == 4 ? 0 : 1)).findFirst().orElse(null);
			if (dbprice != null) {
//				Integer c = WebserviceHelper.Call(clientConfig.DATACENTER).Get(
//						"apiproduct/updateproductstatus?productID=:?&statusID=:?&siteID=:?&languageID=:?&price=:?",
//						Integer.class, ProductID, dbprice.WebStatusId, SiteID, Lang, dbprice.Price);
//				if (c != null && c >= 0) {
//					Logs.Log(isLog, strNOTE, "WebserviceHelper updateproductstatus success");
//				} else {
//					Logs.Log(isLog, strNOTE, "WebserviceHelper updateproductstatus failed");
//				}
				if (message.DataCenter == 4 || message.DataCenter == 3) {
					String qu = "gr.dc4.didx.statusupdate";
					if (message.DataCenter == 3) {
						qu = "gr.beta.didx.statusupdate";
					}
					MessageQueue nwmsg = new MessageQueue();
					nwmsg.Action = DataAction.Update;
					nwmsg.CreatedDate = Utils.GetCurrentDate();
					nwmsg.Lang = Lang;
					nwmsg.SiteID = SiteID;
					nwmsg.DataCenter = message.DataCenter;
					nwmsg.Identify = ProductID + "|" + dbprice.WebStatusId + "|" + dbprice.Price;
					nwmsg.Source = "StatusDMX";
					Logs.Log(isLog, strNOTE, "push => status update ");
					nwmsg.Note = strNOTE;
					try {
						if (isLog) {
							QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushPriority(qu, nwmsg, 10);
						} else {
							QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).Push(qu, nwmsg);
						}
					} catch (Exception e) {
						Logs.LogException(e);
						Logs.Log(isLog, strNOTE, "push => status update " + e.getMessage());
						msg.Code = ResultCode.Retry;
						msg.StackTrace = Arrays.stream(e.getStackTrace()).map(Object::toString)
								.collect(Collectors.joining(", "));
						return msg;
					}
				}
			}
			timer.pause("mainprocess- pricesListdetaultByProvince");

			timer.start("mainprocess- IndexDataListPriceDefault_OR");
			boolean rsOr = IndexDataListPriceDefault_OR(message.ProvinceID, isLog, strNOTE, pricesListdetaultByProvince,
					ProductID);
			if (rsOr == false) {
				Logs.WriteLine("IndexDataListPriceDefault_OR false");
				msg.Code = ResultCode.Retry;
				msg.StackTrace = "IndexDataListPriceDefault false";
				Logs.Log(isLog, strNOTE, "IndexDataListPriceDefault_OR false ResultCode.Retry ");
				return msg;
			}
			timer.pause("mainprocess- IndexDataListPriceDefault_OR");
			Logs.Log(isLog, strNOTE, "Push sql default");

			Logs.getInstance().Log(isLog, strNOTE, "pricesListdetaultByProvince ", pricesListdetaultByProvince);
			Logs.Log(isLog, strNOTE, "rsOr = IndexDataListPriceDefault_OR=true");

			MessageQueue messageRepush = new MessageQueue();
			messageRepush.SqlList = new ArrayList<SqlInfo>();

//			if (v2) {
			// gia VER2

			timer.start("mainprocess- gia VER2");
			Map<String, List<ProductErpPriceBO>> codesMap = new HashMap<>();
			codesMap = listFinalPrices.stream().collect(Collectors.groupingBy(x -> x.RecordID));
			for (var entry : codesMap.entrySet()) {
				if (entry == null)
					continue;
				var recordID = entry.getKey();
				var prices = entry.getValue();
				if (prices == null || prices.isEmpty())
					continue;
				var toBeUpserted = PriceStock.fromListPrices(recordID, prices);
				if (toBeUpserted == null)
					continue;
				SqlInfo sqlinfo = new SqlInfo();
				RefSql ref = new RefSql();
				Utils.BuildSql(isLog, strNOTE, "productprice", "recordid", recordID, toBeUpserted, ref);
				sqlinfo.Sql = ref.Sql;
				sqlinfo.Params = ref.params;
				messageRepush.SqlList.add(sqlinfo);
			}
			timer.pause("mainprocess- gia VER2");
//			} else {
//				for (ProductErpPriceBO item : listFinalPrices) {
//					ProductErpPriceTMP itemTmp = ConvertToProductErpTMP(item);
//					SqlInfo sqlinfo = new SqlInfo();
//					RefSql ref = new RefSql();
//					Utils.BuildSql(isLog, strNOTE, "product_price", "recordid", item.RecordID, itemTmp, ref);
//					sqlinfo.Sql = ref.Sql;
//					sqlinfo.Params = ref.params;
//					messageRepush.SqlList.add(sqlinfo);
//				}
//			}

			if (ProductID == 999999999) {
				msg.Code = ResultCode.Success;
				Logs.Log(isLog, strNOTE, "ProductID == 999999999");
				return msg;
			}
			messageRepush.Source = "STATUS";

//			String qu = "gr.dc4.sql.price";
//			String qu2 = "gr.dc4.sql.price";
//			String qubk = "gr.dc2.sql.price";
//			String qudev = "gr.beta.sql.price";

			int ie = Utils.GetQueueNum5(ProductID);
			String qu = "gr.dc4.sql" + ie;
			String qu2 = "gr.dc4.sql" + ie;
			String qubk = "gr.dc2.sql" + ie;
			String qudev = "gr.beta.sql";

			messageRepush.Identify = message.Identify;
			messageRepush.Action = DataAction.Update;
			messageRepush.ClassName = "mwg.wb.pkg.upsert.Upsert";
			messageRepush.CreatedDate = Utils.GetCurrentDate();
			messageRepush.Type = 0;
			messageRepush.DataCenter = DataCenter;
			timer.start("mainprocess-PushAll");
			QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,
					strNOTE, DataCenter);
			timer.pause("mainprocess-PushAll");
			Logs.Log(isLog, strNOTE, "msg.Code = ResultCode.Success");

			msg.Code = ResultCode.Success;
		} catch (Throwable e) {
			Logs.LogException(message, e);
			msg.StackTrace = Utils.stackTraceToString(e);
			msg.Code = ResultCode.Retry;
		} finally {
//            try {
//                timer.pause("all");
//                String loggedTime = timer.loggedTime();
//                if (loggedTime != null && ProductID > 0) {
//                    Logs.Log(true, "DIDX_LOG|StatusTimer_" + message.SiteID + "_" + ProductID,
//                            "================\r\n"+mapper.writeValueAsString(message)+"\r\n" + loggedTime);
//                }
//            } catch (Exception ignored){}
		}

		return msg;
	}

//	private boolean CRMPushUpdateStockStore(boolean isLog, String strNOTE, long Productid, int ProvinceID, int BrandID,
//			int Storeid) {
//		MessageQueue messageRepush = new MessageQueue();
//		String qu = "gr.dc4.didx.stockstore";
//		String qu2 = "gr.dc4.didx.stockstore";
//		String qubk = "gr.dc2.didx.stockstore";
//		String qudev = "gr.beta.didx.stockstore";
//		messageRepush.Identify = String.valueOf(Productid);
//		messageRepush.Action = DataAction.Update;
//		messageRepush.ClassName = "mwg.wb.pkg.price.StockStore";
//		messageRepush.CreatedDate = Utils.GetCurrentDate();
//		messageRepush.Type = 0;
//		messageRepush.DataCenter = DataCenter;
//		messageRepush.ProvinceID = ProvinceID;
//		messageRepush.BrandID = BrandID;
//		messageRepush.Storeid = Storeid;
//		try {
//			QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,
//					strNOTE, DataCenter);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return true;
//	}

//	private boolean CRMPushUpdateStockStore(boolean isLog, String strNOTE, long Productid) {
//
//		String storelist = productHelper.GetAllStockStoreByProductID(Productid);
//		MessageQueue messageRepush = new MessageQueue();
//		messageRepush.SqlList = new ArrayList<SqlInfo>();
//		SqlInfo sqlinfo = new SqlInfo();
//
//		sqlinfo.Sql = "update crm_product_store set  productid=:productid,storelist=:storelist   upsert where productid=:productid  ";
//		sqlinfo.Params = new HashMap<String, Object>();
//		sqlinfo.Params.put("productid", Productid);
//		sqlinfo.Params.put("storelist", storelist);
//		messageRepush.SqlList.add(sqlinfo);
//
//		messageRepush.Source = "STATUS";
//		String qu = "gr.dc4.sql.stockstore";
//		String qu2 = "gr.dc4.sql.stockstore";
//		String qubk = "gr.dc2.sql.stockstore";
//		String qudev = "gr.beta.sql.stockstore";
//		messageRepush.Identify = String.valueOf(Productid);
//		messageRepush.Action = DataAction.Update;
//		messageRepush.ClassName = "mwg.wb.pkg.upsert.Upsert";
//		messageRepush.CreatedDate = Utils.GetCurrentDate();
//		messageRepush.Type = 0;
//		messageRepush.DataCenter = DataCenter;
//		try {
//			QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,
//					DataCenter);
//		} catch (Exception ex) {
//			// TODO Auto-generated catch block
//			return false;
//		}
//
//		Logs.Log(isLog, strNOTE, "PushUpdateStockStore");
//		return true;
//	}

//	private ProductErpPriceTMP ConvertToProductErpTMP(ProductErpPriceBO item) {
//		ProductErpPriceTMP ErpPrice = new ProductErpPriceTMP();
//		ErpPrice.TotalQuantity = item.TotalQuantity;
//		ErpPrice.ProductCodeTotalQuantity = item.ProductCodeTotalQuantity;
//		ErpPrice.Quantity = item.Quantity;
//		ErpPrice.WebMinStock = item.WebMinStock;
//		ErpPrice.ProductCodeQuantity = item.ProductCodeQuantity;
//		ErpPrice.CenterQuantity = item.CenterQuantity;
//		ErpPrice.WebStatusId = item.WebStatusId;
//		return ErpPrice;
//	}

	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

	public synchronized boolean IndexDataListPriceDefault_ES(int provinceid, boolean isLog, String strNOTE,
			Map<String, Object> pricesMap, long ProductID, int SiteID, String LangID, int order,
			int productCodeTotalQuantity) {
		String esKeyTerm = ProductID + "_" + SiteID + "_" + (LangID.toLowerCase().replaceAll("-", "_"));

		if (productCodeTotalQuantity < 0) {
			productCodeTotalQuantity = 0;
		}

		if (pricesMap.size() > 0) {
			for (int i = 0; i < 10; i++) {

				try {

					var json = mapper.writeValueAsString(pricesMap);
					var update = new UpdateRequest(indexDB, esKeyTerm)
							.doc("{\"Order\":" + order + ", \"Prices\": " + json + ", \"ProductCodeTotalQuantity\": "
									+ productCodeTotalQuantity + "}", XContentType.JSON)
							.docAsUpsert(true).detectNoop(false);
					var client = ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
							.getClient();
					var response = client.update(update, RequestOptions.DEFAULT);
					if (response != null && response.getResult() == Result.UPDATED) {
						// Logs.WriteLine("-Index price status to ES success: " + ProductID);
						// Logs.WriteLine("ES");

						return true;
					} else {

						Logs.WriteLine("-Index price status to ES FAILED: " + ProductID + ", "
								+ response.getResult().name() + "######################");
						Utils.Sleep(100);
					}
				} catch (Exception e) {
					Logs.WriteLine("Exception Index price status to ES FAILED: " + ProductID);
					Logs.LogException(e);
					Utils.Sleep(100);
				}
			}

		}

		return false;
	}

	private boolean IndexDataListPriceDefault_OR(int ProvinceID, boolean isLog, String strNOTE,
			Map<Integer, PriceDefaultBO> pricesListdetaultByProvince, long ProductID) throws JsonProcessingException {
		MessageQueue messageRepush = new MessageQueue();
		messageRepush.SqlList = new ArrayList<SqlInfo>();

//		for (PriceDefaultBO item : pricesListdetaultByProvince.values()) {
//			if (ProvinceID > 0) {
//				if (item.ProvinceId != ProvinceID) {
//					if (item.ProvinceId != 3 && item.ProvinceId != 163) {
//						continue;
//					}
//
//				}
//			}
//			SqlInfo sqlinfo = new SqlInfo();
//			RefSql ref = new RefSql();
//
//			Utils.BuildSql(isLog, strNOTE, "price_default", "recordid", item.RecordID, item, ref);
//			sqlinfo.Sql = ref.Sql;
//			sqlinfo.Params = ref.params;
//			messageRepush.SqlList.add(sqlinfo);
//
////			String egde = "e_product_pricedefault";
////			String cmd2 = "create edge " + egde + " from (select from  product where productid=" + ProductID
////					+ ")   to (select from  price_default   where recordid ='" + item.RecordID + "'   and in('" + egde
////					+ "')[productid = " + ProductID + "].size() = 0)";
////			SqlInfo sqlinfoEdge = new SqlInfo();
////			sqlinfoEdge.Sql = cmd2;
////			messageRepush.SqlList.add(sqlinfoEdge);
//
//			SqlInfo sqlinfoEgde1 = new SqlInfo();
//			sqlinfoEgde1.Params = new HashMap<String, Object>();
//			sqlinfoEgde1.Type = SqlInfoType.CREATE_EDGE_BY_NODE;
//			sqlinfoEgde1.Params.put("edge", "e_product_pricedefault");
//			sqlinfoEgde1.Params.put("from", ProductID);
//			sqlinfoEgde1.Params.put("to", item.RecordID);
//
//			messageRepush.SqlList.add(sqlinfoEgde1);
//
//		}

		// ver2
		var pricegr = PriceDefaultGRBO.fromListDefaultBO(ProductID, 2, "vi-VN", pricesListdetaultByProvince.values());
		SqlInfo sqlinfo = new SqlInfo();
		RefSql ref = new RefSql();
		Utils.BuildSql(isLog, strNOTE, "pricedefault", "recordid", pricegr.recordID, pricegr, ref);
		sqlinfo.Sql = ref.Sql;
		sqlinfo.Params = ref.params;
		messageRepush.SqlList.add(sqlinfo);

		SqlInfo sqlinfoEgde1 = new SqlInfo();
		sqlinfoEgde1.Params = new HashMap<String, Object>();
		sqlinfoEgde1.Type = SqlInfoType.CREATE_EDGE_BY_NODE;
		sqlinfoEgde1.Params.put("edge", "e_pricedefault");
		sqlinfoEgde1.Params.put("from", ProductID);
		sqlinfoEgde1.Params.put("to", pricegr.recordID);
		messageRepush.SqlList.add(sqlinfoEgde1);

		messageRepush.Source = "STATUS";
		// Logs.WriteLine("push status sql " + ProductID);
		// int ie = Utils.GetQueueNum(ProductID);

//		String qu = "gr.dc4.sql.pricestatus";
//		String qu2 = "gr.dc4.sql.pricestatus";
//		String qubk = "gr.dc2.sql.pricestatus";
//		String qudev = "gr.beta.sql.pricestatus";
		int ie = Utils.GetQueueNum5(ProductID);
		String qu = "gr.dc4.sql" + ie;
		String qu2 = "gr.dc4.sql" + ie;
		String qubk = "gr.dc2.sql" + ie;
		String qudev = "gr.beta.sql";

		messageRepush.Identify = String.valueOf(ProductID);
		messageRepush.Action = DataAction.Update;
		messageRepush.ClassName = "mwg.wb.pkg.upsert.Upsert";
		messageRepush.CreatedDate = Utils.GetCurrentDate();
		messageRepush.Type = 0;
		messageRepush.DataCenter = DataCenter;
		// nwmsg.Note = strNOTE;
		try {
			QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,
					strNOTE, DataCenter);
			// Logs.WriteLine("push status sql " + DataCenter);
		} catch (Exception e) {
			Logs.LogException(e);
			return false;
		}

		return true;
	}

	private double CaculateTotalDiscountSimple(Promotion pro, double price) {
		double tempTotalPromotionDiscount = 0;
		if (pro.IsPercentDiscount) {
			tempTotalPromotionDiscount = pro.DiscountValue / 100 * price;
		} else {
			tempTotalPromotionDiscount = pro.DiscountValue;
		}
		return tempTotalPromotionDiscount;
	}

	private boolean IsDiscountPromotionGiaGach(Promotion item) {
//		var a = !Utils.StringIsEmpty(item.ProductId);
//		var b = item.ProductId.toLowerCase().equals("discount");
//		var c = ((item.DiscountValue >= 1000 && !item.IsPercentDiscount) || (item.DiscountValue > 0 && item.DiscountValue < 100 && item.IsPercentDiscount));
//		var d = (item.isApplyInPriceReport == 2 || item.isApplyInPriceReport == 3);

		return !Utils.StringIsEmpty(item.ProductId) && item.ProductId.toLowerCase().equals("discount")
				&& ((item.DiscountValue >= 1000 && !item.IsPercentDiscount)
						|| (item.DiscountValue > 0 && item.DiscountValue < 100 && item.IsPercentDiscount))
				&& (item.isApplyInPriceReport == 2 || item.isApplyInPriceReport == 3);
	}

	private boolean IsValidDiscount(ProductBO product, double discountPercent) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		// Date date = formatter.parse("31/09/2017");
		var now = new Date();

		var maximumPercentAllow = 60;
		try {
			if (product.CategoryID == 7264 && now.after(formatter.parse("01/10/2020 00:00:00"))
					&& now.before(formatter.parse("31/01/2021 23:59:59"))) {
				maximumPercentAllow = 75;
			} else if (Arrays.asList(new int[] { 6858, 6862, 7924, 6863, 3885, 6858, 4706 })
					.contains(product.CategoryID) && now.after(formatter.parse("07/12/2020 00:00:00"))
					&& now.before(formatter.parse("28/02/2021 23:59:59"))) {
				maximumPercentAllow = 75;
			} else if (Arrays.asList(new int[] { 60, 1662 }).contains(product.CategoryID)
					&& now.after(formatter.parse("16/11/2020 00:00:00"))
					&& now.before(formatter.parse("31/12/2021 23:59:59"))) {
				maximumPercentAllow = 75;
			} else if (now.after(formatter.parse("21/12/2020 00:00:00"))
					&& now.before(formatter.parse("28/02/2021 23:59:59"))
					&& (product.ProductID == 208853 || product.ProductID == 208854)) {
				maximumPercentAllow = 75;
			} else if (now.after(formatter.parse("21/12/2020 00:00:00"))
					&& now.before(formatter.parse("21/01/2021 23:59:59"))
					&& Arrays.asList(new int[] { 202264, 202263, 202265, 202728 }).contains(product.ProductID)) {
				maximumPercentAllow = 70;
			} else if (now.after(formatter.parse("21/12/2020 00:00:00"))
					&& now.before(formatter.parse("09/01/2021 23:59:59"))
					&& Arrays.asList(new int[] { 223478, 225986, 225990 }).contains(product.ProductID)) {
				maximumPercentAllow = 80;
			}
			if (discountPercent > maximumPercentAllow)
				return false;
		} catch (ParseException e) {
			Logs.LogException(e);
		}
		return true;
	}

	public ResultMessage RefreshDelPrice(MessageQueue message) throws Throwable {
		DataCenter = message.DataCenter;
		ResultMessage msg = new ResultMessage();
		try {

			String strNOTE = message.Note + "";

			msg.Code = ResultCode.Success;
			long ProductID = Utils.ToLong(message.Identify);

			boolean isLog = false;

			if (strNOTE.contains("DIDX_TOP_LOG") || strNOTE.contains("DIDX_LOG")) {

				isLog = true;
			}

			if ((ProductID <= 0)) {
				Logs.Log(isLog, strNOTE, "ProductID <= 0" + ProductID);
				return msg;
			}

			int SiteID = 0;
			int BrandID = 0;
			int PriceArea = 13;
			String Lang = "";
			if (message.BrandID > 0) {
				BrandID = message.BrandID;
				Lang = DidxHelper.getLangByBrandID(BrandID);
				SiteID = DidxHelper.getSitebyBrandID(BrandID);
			} else {
				BrandID = DidxHelper.getBrandBySite(message.SiteID, message.Lang);
				SiteID = message.SiteID;
				Lang = message.Lang;
			}
			if ((SiteID <= 0 || BrandID <= 0 || Utils.StringIsEmpty(Lang))) {
				Logs.Log(isLog, strNOTE, "SiteID <= 0 || BrandID<=0  " + ProductID);
				return msg;
			}
//			if ((SiteID == 2)) {
//
//				return msg;
//			}
			PriceArea = DidxHelper.getDefaultPriceAreaBySiteID(SiteID, Lang);
			Logs.Log(isLog, strNOTE,
					"Refresh Pricestatus..." + ProductID + " PriceArea " + PriceArea + " SiteID" + SiteID);
//

			List<ProductErpPriceBO> lstPrices = null;
			lstPrices = priceHelper.getListPriceStrings(ProductID, PriceArea, SiteID);
			Logs.getInstance().Log(isLog, strNOTE, "GetListPriceByBySiteID", lstPrices);

			// giá cho giá hãng
//			int priceAreaOrg = DidxHelper.getDefaultPriceAreaOrgBySiteID(SiteID, Lang);
//			List<ProductErpPriceBO> priceOrgList = null;
//			List<ProductErpPriceBO> lstPrices402 = null;
//			if (priceAreaOrg > 0) {
//
//				priceOrgList = null;// priceHelper.GetListPriceByBySiteID(ProductID, priceAreaOrg, SiteID);
//
//			}
//			if (SiteID == 12) {
//				lstPrices402 = null;// priceHelper.GetListPriceByBySiteID(ProductID, 402, SiteID);
//			}

			if (lstPrices == null) {
				Logs.Log(isLog, strNOTE, "lstPrices.size =null");
				return msg;
			}

			List<ProductErpPriceBO> listFinalPrices = new ArrayList<ProductErpPriceBO>();

			int categoryID = productHelper.GetCategoryIDByProductID(ProductID);
			Logs.Log(isLog, strNOTE, "categoryID = : " + categoryID);

			Logs.Log(isLog, strNOTE, "ProductErpPriceBO item : lstPrices");

			List<ProductErpPriceBO> finalListPriceDefault = new ArrayList<ProductErpPriceBO>();

			List<Integer> lstProvinceID = productHelper.getAllProvinceByCountry(DidxHelper.getCountryBySiteID(SiteID));
			int statusid = 1;
			Date arrival = null;
			String md5 = "";
			Logs.getInstance().Log(isLog, strNOTE, "listFinalPrices", listFinalPrices);
			for (int item : lstProvinceID) {

				ProductErpPriceBO priceDefault = null;
				priceDefault = new ProductErpPriceBO();
				priceDefault.Price = 0.0;
				priceDefault.ProductCode = "";
				priceDefault.WebStatusId = 1;
				priceDefault.ProductId = ProductID;
				priceDefault.ProvinceId = item;
				finalListPriceDefault.add(priceDefault);

			}
			Logs.Log(isLog, strNOTE, "===============================");
			Logs.getInstance().Log(isLog, strNOTE, "finalListPriceDefault object", finalListPriceDefault);
			boolean isUpdateES = true;
			// test lai
			String esKeyTerm = ProductID + "_" + SiteID + "_" + (Lang.toLowerCase().replaceAll("-", "_"));
			synchronized (StatusV1.class) {

				if (g_listWebStatusId.containsKey(esKeyTerm)) {

					String k = g_listWebStatusId.get(esKeyTerm);
					if (!Utils.StringIsEmpty(md5)) {
						if (k.equals(md5)) {
							isUpdateES = false;
						} else {

						}
					}
				} else {

				}
			}

			Map<Integer, PriceDefaultBO> pricesListdefaultByProvince = new HashMap<>();
			Map<String, Object> pricesMap = new HashMap<String, Object>();

			Logs.getInstance().Log(isLog, strNOTE, "finalListPriceDefault", finalListPriceDefault);
			int finalListPriceDefaultSize = finalListPriceDefault.size();
			for (int i = 0; i < finalListPriceDefaultSize; i++) {
				var item = finalListPriceDefault.get(i);
				PriceDefaultBO deF = new PriceDefaultBO();
				pricesMap.put("IsShowHome_" + item.ProvinceId, item.IsShowHome ? 1 : 0);
				pricesMap.put("WebStatusId_" + item.ProvinceId, item.WebStatusId);
				pricesMap.put("Price_" + item.ProvinceId, item.Price);
				pricesMap.put("ProductCode_" + item.ProvinceId, "");
				double Price402 = 0;
				double PriceOrg = 0;

				deF.Price402 = Price402;
				pricesMap.put("Price402_" + item.ProvinceId, Price402);
				deF.PriceOrg = PriceOrg;
				pricesMap.put("PriceOrg_" + item.ProvinceId, PriceOrg);

				deF.WebStatusId = item.WebStatusId;
				deF.WebStatusIdNew = item.WebStatusIdNew;
				deF.WebStatusIdOld = item.WebStatusIdOld;
				deF.ProductID = ProductID;
				deF.IsShowHome = item.IsShowHome ? 1 : 0;
				deF.ProductCode = "";
				deF.Price = item.Price;

				deF.ProvinceId = item.ProvinceId;
				deF.SiteID = SiteID;
				deF.RecordID = SiteID + "_" + Lang + "_" + ProductID + "_" + item.ProvinceId;
				deF.LangID = Lang;
				Logs.Log(isLog, strNOTE, "deF.ProvinceId " + deF.ProvinceId + "deF.Price " + deF.Price
						+ " deF.WebStatusId" + deF.WebStatusId);
				pricesListdefaultByProvince.put(item.ProvinceId, deF);
			}
			isUpdateES = true;
			if (isUpdateES == false) {
				Logs.Log(isLog, strNOTE, "SKIP IndexDataListPriceDefault order ");
			} else {
				Date createdate = priceHelper.GetCreatedDateFromCache(ProductID);
				int order = PriceSatusHelper.GetOrder(statusid, createdate, arrival, SiteID, Lang);
				Logs.Log(isLog, strNOTE, "IndexDataListPriceDefault order " + order);
				boolean rsEs = IndexDataListPriceDefault_ES(isLog, strNOTE, pricesMap, ProductID, SiteID, Lang, order,
						0);
				if (rsEs == false) {
					Logs.WriteLine("IndexDataListPriceDefault false");
					msg.Code = ResultCode.Retry;
					msg.StackTrace = "IndexDataListPriceDefault false";
					Logs.Log(isLog, strNOTE, "IndexDataListPriceDefault false ResultCode.Retry ");
					return msg;
				}

				synchronized (StatusDMX.class) {
					if (!Utils.StringIsEmpty(md5)) {
						g_listWebStatusId.put(esKeyTerm, md5);
					}

				}

			}

			boolean rsOr = IndexDataListPriceDefault_OR("Del", message, isLog, strNOTE, pricesListdefaultByProvince,
					ProductID, SiteID, Lang);
			if (rsOr == false) {
				Logs.WriteLine("IndexDataListPriceDefault_OR false");
				msg.Code = ResultCode.Retry;
				msg.StackTrace = "IndexDataListPriceDefault false";
				Logs.Log(isLog, strNOTE, "IndexDataListPriceDefault_OR false ResultCode.Retry ");
				return msg;
			}

			Logs.Log(isLog, strNOTE, "msg.Code = ResultCode.Success");

			msg.Code = ResultCode.Success;
		} catch (Exception e) {
			Logs.LogException("status.txt", e);
			msg.StackTrace = Utils.stackTraceToString(e);
			msg.Code = ResultCode.Retry;
		}
		return msg;
	}

	public Map<String, Object> RefresPriceRepresent(ProductBO productBO, int SiteID) throws Throwable {

		Map<String, Object> pricesMap = new HashMap<String, Object>();
		Map<String, Object> listpricesMap = new HashMap<String, Object>();
		if ((productBO != null && productBO.CategoryID == 7264 && productBO.IsCoupleWatch
				&& productBO.ListCoupleWatchBO != null && productBO.ListCoupleWatchBO.length > 0)
				|| (productBO.CategoryID == 2002 && productBO.IsMultiAirConditioner
						&& productBO.MultiAirConditioners != null && productBO.MultiAirConditioners.length >= 3)) {
			int[] productIDListChild = productBO.CategoryID == 7264 ? productBO.ListCoupleWatchProductID
					: Arrays.stream(productBO.MultiAirConditionerIds).mapToInt(Integer::parseInt).toArray();
			var getlistprice = priceHelper.getListPriceStringsByListID(productIDListChild, SiteID);

			if (getlistprice != null && getlistprice.size() > 0) {
				List<Integer> lstProvinceID = productHelper
						.getAllProvinceByCountry(DidxHelper.getCountryBySiteID(SiteID));
				var PriceAfterPromotion = productHelper.getPriceAfterPromotion(productIDListChild,
						lstProvinceID.stream().mapToInt(Integer::intValue).toArray(), SiteID, "vi-VN");

				for (var provinceID : lstProvinceID) {
					double totalPrice = 0;
					int webStatusID = 0;
					int isShowHome = 0;
					var listPriceBO = new ArrayList<ProductErpPriceBO>();

					for (var price : getlistprice) {
						if (price == null)
							continue;
						ProductErpPriceBO pricesing = Stream.of(price).filter(x -> x.ProvinceId == provinceID)
								.findFirst().orElse(null);
						if (pricesing != null) {
							// totalPrice += pricesing.Price;
							listPriceBO.add(pricesing);
						}
					}

					if (listPriceBO.size() == 0 || (productBO.CategoryID == 7264
							&& productBO.ListCoupleWatchBO.length != listPriceBO.size())) {
						// có > 1 con không có giá
						webStatusID = 1;
						totalPrice = 0;
						isShowHome = 0;
					} else if (productBO.CategoryID == 2002 && listPriceBO.size() < 3) {
						webStatusID = 1;
						totalPrice = 0;
						isShowHome = 0;
					} else {
						if (productBO.CategoryID == 2002) {
							if (listPriceBO.stream().filter(x -> x.WebStatusId != 8).findFirst().orElse(null) != null) {
								webStatusID = 1;
								totalPrice = 0;
							} else {
								webStatusID = 4;
								totalPrice = listPriceBO.stream().map(x -> x.getPrice()).reduce((double) 0,
										(a, b) -> a + b);
							}
						} else {
							if (listPriceBO.stream()
									.filter(x -> x.WebStatusId != 4 && x.WebStatusId != 3 && x.WebStatusId != 11)
									.findFirst().orElse(null) != null) {
								webStatusID = 1;
								totalPrice = 0;
							} else {
								webStatusID = 4;
								totalPrice = listPriceBO.stream().map(x -> x.getPrice()).reduce((double) 0,
										(a, b) -> a + b);
							}
						}

						if (listPriceBO.stream().filter(x -> x.IsShowHome).findFirst().orElse(null) != null) {
							isShowHome = 1;
						} else {
							isShowHome = 0;
						}
					}

					pricesMap.put("IsShowHome_" + provinceID, isShowHome);
					pricesMap.put("WebStatusId_" + provinceID, webStatusID);
					pricesMap.put("Price_" + provinceID, totalPrice);
					pricesMap.put("ProductCode_" + provinceID,
							productBO.ProductErpPriceBO != null ? productBO.ProductErpPriceBO.ProductCode : "");

					// giá sau km của đồng hồ cặp và multi máy lạnh

//					PriceAfterPromotion
					double totalPriceAfterPromotion = 0; // chỗ này cần tối ưu sau
					double totalPromotionDiscountPercent = 0;
					for (int itemproductID : productIDListChild) {
						var tmp = PriceAfterPromotion.get(itemproductID + "_" + provinceID);
						if (tmp != null) {
							totalPriceAfterPromotion += tmp;
						}
						tmp = PriceAfterPromotion.get(itemproductID + "_" + provinceID + "_PromotionDiscountPercent");
						if (tmp != null) {
							totalPromotionDiscountPercent += tmp;
						}
					}
					pricesMap.put("PriceAfterPromotion_" + provinceID + "_" + SiteID, totalPriceAfterPromotion);

				} // end for lstProvinceID
			}
		}
		return pricesMap;

	}

	private final Lock queueLock = new ReentrantLock();

	public synchronized boolean IndexDataListPriceDefault_ES(boolean isLog, String strNOTE,
			Map<String, Object> pricesMap, long ProductID, int SiteID, String LangID, int order,
			int productCodeTotalQuantity) {
		String esKeyTerm = ProductID + "_" + SiteID + "_" + (LangID.toLowerCase().replaceAll("-", "_"));
		if (productCodeTotalQuantity < 0) {
			productCodeTotalQuantity = 0;
		}
		if (pricesMap.size() > 0) {
			for (int i = 0; i < 10; i++) {

				try {
					queueLock.lock();
					// synchronized (StatusV1.class) {

					var json = mapper.writeValueAsString(pricesMap);
					var update = new UpdateRequest(indexDB, esKeyTerm)
							.doc("{\"Order\":" + order + ", \"Prices\": " + json + ", \"ProductCodeTotalQuantity\": "
									+ productCodeTotalQuantity + "}", XContentType.JSON)
							.docAsUpsert(true).detectNoop(false);
					var client = ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
							.getClient();
					var response = client.update(update, RequestOptions.DEFAULT);
					if (response != null && response.getResult() == Result.UPDATED) {
						// Logs.WriteLine("-Index price status to ES success: " + ProductID);
						// Logs.WriteLine("ES");

						return true;
					} else {

						Logs.WriteLine("-Index price status to ES FAILED: " + ProductID + ", "
								+ response.getResult().name() + "######################");
						Utils.Sleep(100);
					}
					// }
				} catch (Exception e) {
					Logs.WriteLine("Exception Index price status to ES FAILED: " + ProductID);
					Logs.LogException(e);
					Utils.Sleep(100);
				} finally {
					queueLock.unlock();
				}
			}

		}

		return false;
	}

	private boolean IndexDataListPriceDefault_OR(String noteLog, MessageQueue messageGoc, boolean isLog, String strNOTE,
			Map<Integer, PriceDefaultBO> pricesListdetaultByProvince, long ProductID, int siteID, String langID)
			throws JsonProcessingException {
		MessageQueue messageRepush = new MessageQueue();
		messageRepush.SqlList = new ArrayList<SqlInfo>();
//
//		for (PriceDefaultBO item : pricesListdetaultByProvince.values()) {
//			if (ProvinceID > 0) {
//				if (item.ProvinceId != ProvinceID) {
//					if (item.ProvinceId != 3 && item.ProvinceId != 163) {
//						continue;
//					}
//
//				}
//			}
//			SqlInfo sqlinfo = new SqlInfo();
//			RefSql ref = new RefSql();
//
//			Utils.BuildSql(isLog, strNOTE, "price_default", "recordid", item.RecordID, item, ref);
//			sqlinfo.Sql = ref.Sql;
//			sqlinfo.Params = ref.params;
//			messageRepush.SqlList.add(sqlinfo);
//
////			String egde = "e_product_pricedefault";
////			String cmd2 = "create edge " + egde + " from (select from  product where productid=" + ProductID
////					+ ")   to (select from  price_default   where recordid ='" + item.RecordID + "'   and in('" + egde
////					+ "')[productid = " + ProductID + "].size() = 0)";
////			SqlInfo sqlinfoEdge = new SqlInfo();
////			sqlinfoEdge.Sql = cmd2;
////			messageRepush.SqlList.add(sqlinfoEdge);
//
//			SqlInfo sqlinfoEgde1 = new SqlInfo();
//			sqlinfoEgde1.Params = new HashMap<String, Object>();
//			sqlinfoEgde1.Type = SqlInfoType.CREATE_EDGE_BY_NODE;
//			sqlinfoEgde1.Params.put("edge", "e_product_pricedefault");
//			sqlinfoEgde1.Params.put("from", ProductID);
//			sqlinfoEgde1.Params.put("to", item.RecordID);
//
//			messageRepush.SqlList.add(sqlinfoEgde1);
//
//		}

		// ver2
		var pricegr = PriceDefaultGRBO.fromListDefaultBO(ProductID, siteID, langID,
				pricesListdetaultByProvince.values());
		SqlInfo sqlinfo = new SqlInfo();
		RefSql ref = new RefSql();
		Utils.BuildSql(isLog, strNOTE, "pricedefault", "recordid", pricegr.recordID, pricegr, ref);
		sqlinfo.Sql = ref.Sql;
		sqlinfo.Params = ref.params;
		sqlinfo.tablename = ref.lTble;
		sqlinfo.tablekey = ref.lId;
		sqlinfo.msg = " br:" + messageGoc.BrandID + " prov:" + messageGoc.ProvinceID + " st:" + messageGoc.Storeid + ","
				+ noteLog;

		// sqlinfo.msg="prov:"+ProvinceID;
		messageRepush.SqlList.add(sqlinfo);

		SqlInfo sqlinfoEgde1 = new SqlInfo();
		sqlinfoEgde1.Params = new HashMap<String, Object>();
		sqlinfoEgde1.Type = SqlInfoType.CREATE_EDGE_BY_NODE;
		sqlinfoEgde1.Params.put("edge", "e_pricedefault");
		sqlinfoEgde1.Params.put("from", ProductID);
		sqlinfoEgde1.Params.put("to", pricegr.recordID);
		messageRepush.SqlList.add(sqlinfoEgde1);

		messageRepush.Source = "STATUSV2";

		// Logs.WriteLine("push status sql " + ProductID);
		// int ie = Utils.GetQueueNum(ProductID);
		int ie = Utils.GetQueueNum5(ProductID);
		String qu = "gr.dc4.sql" + ie;
		String qu2 = "gr.dc4.sql" + ie;
		String qubk = "gr.dc2.sql" + ie;
		String qudev = "gr.beta.sql";

//		String qu = "gr.dc4.sql.pricestatus";
//		String qu2 = "gr.dc4.sql.pricestatus";
//		String qubk = "gr.dc2.sql.pricestatus";
//		String qudev = "gr.beta.sql.pricestatus";
		messageRepush.Identify = String.valueOf(ProductID);
		messageRepush.Action = DataAction.Update;
		messageRepush.ClassName = "mwg.wb.pkg.upsert.Upsert";
		messageRepush.CreatedDate = Utils.GetCurrentDate();
		messageRepush.Type = 0;
		messageRepush.Note = strNOTE;
		messageRepush.DataCenter = DataCenter;
		// nwmsg.Note = strNOTE;
		try {
			QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,
					strNOTE, DataCenter);
			// Logs.WriteLine("push status sql " + DataCenter);
		} catch (Exception e) {
			Logs.LogException(e);
			return false;
		}
		Logs.getInstance().Log(isLog, strNOTE, "IndexDataListPriceDefault_OR ", messageRepush);

		return true;
	}

}

class PromotionGroupBy {
	public PromotionGroupBy(int promotionID2, String groupID2) {
		PromotionID = promotionID2;
		GroupID = groupID2;
	}

	public int PromotionID;
	public String GroupID;
}
