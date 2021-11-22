package mwg.wb.pkg.price;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.InStockBO;
import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.helper.BigPhonePriceHelper;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.common.DidxHelper;
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
import mwg.wb.model.other.CamPreorder;
import mwg.wb.model.products.PriceStock;
import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.pkg.price.helper.PriceSatusHelper;
import mwg.wb.pkg.price.helper.ProductErpPriceDefault;
import mwg.wb.pkg.price.helper.ProductErpPriceTMP;

public class StatusV1backup implements Ididx {
	private String indexDB = "";
//	private String typeDB = "product";
//
//	private ORThreadLocal factoryWrite = null;
//	private ORThreadLocal factoryRead = null;
	private PriceHelper priceHelper = null;
	private ProductHelper productHelper = null;
	private ObjectMapper mapper = null;
//	private ErpHelper erpHelper = null;
	private ClientConfig clientConfig = null;
	private int DataCenter = 0;
//	private CrmServiceHelper crmHelper = null;

	// có 4 nhóm nhu cầu tồn kho tgdd và dmx:
	// 1-hàng điện gia dụng - quạt điều hòa quạt sửi: maingroupid(484)
	// subgroupid(3799): lấy tồn kho tgdd + dmx(showweb) + tồn kho ngoài(kho trung
	// tâm) ---sử dụng hàm: stock_getByProvince1

	// 2-tồn hàng cồng kềnh maingroupid(484,305,304,1214,1116) site dmx: tồn theo
	// từng site(showweb) + tồn kho ngoài(kho trung tâm) ---sử dụng hàm:
	// stock_getByProvince2

	// 3-tồn kho hàng viễn thông
	// maingroupid(13,244,22,16,17,23,184,244,484,525,764,905,364,1274,1294): lấy
	// tồn kho tgdd + dmx(showweb) + kho 1488 ---sử dụng hàm: stock_getByProvince3

	// 4-còn lại siteid in (1,2): tồn theo từng site(showweb) + tồn kho 1488 ---sử
	// dụng hàm: stock_getByProvince4

	private static Set<Integer> vienthongGroupID = Set.of(13, 244, 22, 16, 17, 23, 184, 484, 525, 764, 905, 364, 1274,
			1294);
	private static Set<Integer> congkenhGroupID = Set.of(484, 305, 304, 1214, 1116);

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
//		crmHelper = (CrmServiceHelper) objectTransfer.crmHelper;
	}

	public static Map<String, String> g_listWebStatusId = new HashMap<String, String>();
	public static Map<Long, List<ProductErpPriceBO>> g_listPrice = new HashMap<Long, List<ProductErpPriceBO>>();
	public static Map<String, String> g_listES = new HashMap<String, String>();
//	int[] arrCategoryGetCenterQuantity = { 4706, 8121, 8120, 8119, 7720, 4697, 5612, 5475, 166, 3385, 4645, 2202, 1942,
//			1943, 1944, 2002, 1962, 2162, 482, 60, 57, 55, 58, 54, 1662, 1363, 1823, 56, 75, 86, 382, 346, 2429, 2823,
//			2824, 2825, 346, 3885, 1882, 5005, 4547, 1982, 3305, 2063, 1987, 7604 };
//	int[] arrCategoryDienMay = { 1942, 1943, 1944, 2002, 1962, 2202, 2022, 3385 };
//	int[] arrAccessoryCategory = { 482, 60, 57, 55, 58, 54, 1662, 1363, 1823, 56, 75, 86, 382, 346, 2429, 2823, 2824,
//			2825, 346, 3885, 1882, 5005 };
//	int[] arrApplianceCategory = { 4645, 462, 1922, 1982, 1983, 1984, 1985, 1986, 1987, 1988, 1989, 1990, 1991, 1992,
//			2062, 2063, 2064, 2084, 2222, 2262, 2302, 2322, 2342, 2142, 3305, 5473, 2428, 3385, 5105, 7367, 5554,
//			7498 };
//	int[] arrSpecialCategory = { 4645 }; // NH xử lý đặc biệt

	public ResultMessage Refresh(MessageQueue message) {
		DataCenter = message.DataCenter;
		ResultMessage msg = new ResultMessage();
		try {

			String strNOTE = message.Note + "";
//			String strSource = message.Source + "";
			msg.Code = ResultCode.Success;
			long ProductID = Utils.ToLong(message.Identify);

			if (!productHelper.CheckProductExistFromCache(ProductID)) {
				return msg;
			}

			boolean isLog = false;

			if (strNOTE.contains("DIDX_TOP_LOG") || strNOTE.contains("DIDX_LOG")) {

				isLog = true;
			}

			if ((ProductID <= 0)) {
				Logs.Log(isLog, strNOTE, "ProductID <= 0" + ProductID);
				return msg;
			}
//			if (message.ProvinceID > 0 && message.BrandID > 0) {
//				// boolean rsP = PushUpdateStockStore(isLog, strNOTE, ProductID,
//				// message.ProvinceID, message.BrandID,
//				// SiteID, Lang);
//				boolean rsP = CRMPushUpdateStockStore(isLog, strNOTE, ProductID, message.ProvinceID, message.BrandID,
//						message.Storeid);
//				Logs.Log(isLog, strNOTE, "CRMPushUpdateStockStore" );
//				if (!rsP) {
//					msg.Code = ResultCode.Retry;
//					msg.StackTrace = "PushUpdateStockStore false";
//					return msg;
//				}
//
//			}
			int SiteID = 0;
			int BrandID = 0;
			int PriceArea = 13;
			String Lang = "";
			boolean v2 = strNOTE.contains("VERSION2");
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
			if ((SiteID == 2)) {

				return msg;
			}
			PriceArea = DidxHelper.getDefaultPriceAreaBySiteID(SiteID, Lang);
			Logs.Log(isLog, strNOTE,
					"Refresh Pricestatus..." + ProductID + " PriceArea " + PriceArea + " SiteID" + SiteID);
//			

			List<ProductErpPriceBO> lstPrices = null;
			lstPrices = v2 ? priceHelper.getListPriceStrings(ProductID, PriceArea, SiteID)
					: priceHelper.GetListPriceByBySiteID(ProductID, PriceArea, SiteID);

			Logs.getInstance().Log(isLog, strNOTE, "GetListPriceByBySiteID", lstPrices);
//			if ((SiteID == 2)) {
//				Logs.Log(isLog, strNOTE, "SiteID==2");
//				return msg;
//			}
			// giá cho giá hãng
			int priceAreaOrg = DidxHelper.getDefaultPriceAreaOrgBySiteID(SiteID, Lang);
			List<ProductErpPriceBO> priceOrgList = null;
//			List<ProductErpPriceBO> lstPrices402 = null;
			if (priceAreaOrg > 0) {

				priceOrgList = priceHelper.GetListPriceByBySiteID(ProductID, priceAreaOrg, SiteID);

			}
//			if (SiteID == 12) {
//				lstPrices402 = priceHelper.GetListPriceByBySiteID(ProductID, 402, SiteID);
//			}

			if (lstPrices == null) {
				Logs.Log(isLog, strNOTE, "lstPrices.size <0");
				return msg;
			}

			if ((lstPrices.size() <= 0)) {

				return RefreshDelPrice(message);
			}
			List<ProductErpPriceBO> listFinalPrices = new ArrayList<ProductErpPriceBO>();

			List<String> listCode = lstPrices.stream().map(e -> e.ProductCode.trim()).distinct()
					.collect(Collectors.toList());

			List<InStockBO> listAllStock = new ArrayList<InStockBO>();
//			int sampleQuantity = 0;

			int categoryID = productHelper.GetCategoryIDByProductID(ProductID);
			Logs.Log(isLog, strNOTE, "categoryID = : " + categoryID);
			// get all stock by code
			int brand = BrandID;
			var mainSubgroup = productHelper.getMainSubGroupIDByProductID(ProductID);
//			int maingroupid = productHelper.getMainGroupIDByProductID(ProductID);
			for (String item : listCode) {
				List<InStockBO> listStock = null;
				boolean fromCodeStock = false;
				// lọc TH đặc biệt lấy cả 2 site tgdđ + đmx: lấy của đmx.
				if (SiteID == 1 || SiteID == 2) {
					if (mainSubgroup != null && SiteID == 1) {
						if ((mainSubgroup.maingroupid == 484 && mainSubgroup.subgroupid == 3799)
								|| congkenhGroupID.contains(mainSubgroup.maingroupid)
								|| vienthongGroupID.contains(mainSubgroup.maingroupid)) {
							listStock = priceHelper.getStockByProductCodeNew(item, 2);
							fromCodeStock = true;
						}
					}
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
					listAllStock.addAll(listStock.stream().filter(
							x -> x.inventorystatusid == 1 || (x.inventorystatusid == 3 && (brand == 6 || brand == 11)
									&& BigPhonePriceHelper.trungBay.contains(categoryID)))
							.collect(Collectors.toList()));
				}
			}

			// Giá mặc định: là những code(object giá) trong list giá ưu tiên kd... theo
			// tỉnh thành, (tỉnh thành có nhiều code)
			Logs.Log(isLog, strNOTE, "ProductErpPriceBO item : lstPrices");

			for (ProductErpPriceBO item : lstPrices) {

				List<InStockBO> listStock = listAllStock.stream()
						.filter(x -> x.productcode.trim().equals(item.ProductCode.trim())).collect(Collectors.toList());

				if (((item != null) && (item.ProductId != ProductID))) {
					Logs.Log(isLog, strNOTE, "ProductID không khớp");
					return msg;
				}

				var intTotalSampleQuantity = listAllStock.stream().mapToInt(x -> x.samplequantity).sum();

				int intInstockInCountry = listStock.stream().mapToInt(c -> c.quantity).sum();

				List<InStockBO> objInstockTmp = new ArrayList<InStockBO>();
				for (var stock : listStock) {
					if (stock != null && !stock.productcode.isEmpty() && stock.provinceid > 0) {
						if (stock.provinceid == item.ProvinceId
								&& stock.productcode.trim().equals(item.ProductCode.trim())) {
							objInstockTmp.add(stock);
						}
					}
				}

				InStockBO objInstock = null;
				if (((objInstockTmp != null) && (objInstockTmp.size() > 0))) {
					objInstock = objInstockTmp.stream().findFirst().get();
				}

				int intInstockInCountryByProductCode = 0;
				List<InStockBO> lstInstocksInCountry = listStock.stream()
						.filter(i -> ((i != null) && i.productcode.trim().equals(item.ProductCode.trim())))
						.collect(Collectors.toList());
				if ((lstInstocksInCountry == null)) {
					intInstockInCountryByProductCode = 0;
				} else {

					intInstockInCountryByProductCode = lstInstocksInCountry.stream().mapToInt(c -> c.quantity).sum();
				}

				ProductErpPriceBO objPriceNews = item;
				objPriceNews.TotalQuantity = intInstockInCountry;
				objPriceNews.ProductCodeTotalQuantity = intInstockInCountryByProductCode;
				if ((objInstock == null)) {
					objPriceNews.Quantity = 0;
					objPriceNews.WebMinStock = 0;
					objPriceNews.Quantity = 0;
					// dang la ProductCodeQuantity
					objPriceNews.ProductCodeTotalQuantity = intInstockInCountryByProductCode;
					objPriceNews.ProductCodeQuantity = 0;
					objPriceNews.CenterQuantity = 0;

					objPriceNews.TotalSampleQuantity = 0;
					objPriceNews.SampleQuantity1 = 0;
				} else {
					objPriceNews.WebMinStock = objInstock.webminstock;
					objPriceNews.Quantity = objInstock.quantity;
					// dang la ProductCodeQuantity
					objPriceNews.ProductCodeTotalQuantity = intInstockInCountryByProductCode;
					objPriceNews.ProductCodeQuantity = objInstock.quantity;
					objPriceNews.CenterQuantity = objInstock.centerquantity;

					objPriceNews.TotalSampleQuantity = intTotalSampleQuantity;
					objPriceNews.SampleQuantity1 = objInstock.samplequantity;
				}
				if (SiteID == 6) {
					CamPreorder campreorder = null;
					try {
						campreorder = priceHelper.getCamPreorder(ProductID);
					} catch (Throwable e1) {
						Logs.LogException(e1);
						msg.Code = ResultCode.Retry;
						return msg;
					}
					if (campreorder != null) {
						Date now = new Date();
						objPriceNews.campreorder = campreorder.ispreordercam && campreorder.preordercamfromdate != null
								&& campreorder.preordercamfromdate.before(now) && campreorder.preordercamtodate != null
								&& campreorder.preordercamtodate.after(now);
					}
				}

//				objPriceNews.TotalSampleQuantity = sampleQuantity;
				objPriceNews.CategoryId = categoryID;

				objPriceNews = PriceSatusHelper.ProcessProductStatus(objPriceNews, SiteID, Lang);

				listFinalPrices.add(objPriceNews);
			}
			if ((listFinalPrices == null || listFinalPrices.size() <= 0)) {
				// Logs.WriteLine("lstPrices.size <0");
				Logs.WriteLine("listFinalPrices.size <0");
				return msg;
			}

			List<ProductErpPriceBO> finalListPriceDefault = new ArrayList<ProductErpPriceBO>();
//
			List<Integer> lstProvinceID = listFinalPrices.stream().map(e -> e.ProvinceId).distinct()
					.collect(Collectors.toList());
			int statusid = 1;
			Date arrival = null;
			String md5 = "";
			Logs.getInstance().Log(isLog, strNOTE, "listFinalPrices", listFinalPrices);
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
				if (item == 3 || item == 163) {
					statusid = priceDefault.WebStatusId;
					arrival = priceDefault.ProductArrivalDate;
					Logs.Log(isLog, strNOTE, "ProvinceID " + item + " WebStatusId " + statusid);
				}
				finalListPriceDefault.add(priceDefault);
				md5 = md5 + priceDefault.ProvinceId + "_" + priceDefault.Price + "_" + priceDefault.WebStatusId
						+ priceDefault.IsShowHome;

			}
			Logs.Log(isLog, strNOTE, "===============================");
			Logs.getInstance().Log(isLog, strNOTE, "finalListPriceDefault object", finalListPriceDefault);
			boolean isUpdateES = true;
			// test lai
			String esKeyTerm = ProductID + "_" + SiteID + "_" + (Lang.toLowerCase().replaceAll("-", "_"));
			synchronized (StatusV1backup.class) {

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

			Map<Integer, ProductErpPriceDefault> pricesListdefaultByProvince = new HashMap<Integer, ProductErpPriceDefault>();
			Map<String, Object> pricesMap = new HashMap<String, Object>();

			Logs.getInstance().Log(isLog, strNOTE, "finalListPriceDefault", finalListPriceDefault);
			int finalListPriceDefaultSize = finalListPriceDefault.size();
			for (int i = 0; i < finalListPriceDefaultSize; i++) {
				var item = finalListPriceDefault.get(i);
				pricesMap.put("IsShowHome_" + item.ProvinceId, item.IsShowHome ? 1 : 0);
				pricesMap.put("WebStatusId_" + item.ProvinceId, item.WebStatusId);
				pricesMap.put("Price_" + item.ProvinceId, item.Price);
				pricesMap.put("ProductCode_" + item.ProvinceId, item.ProductCode);
//				double Price402 = 0;
				double PriceOrg = 0;
				if (SiteID == 12) {
					// code cho phu kien
//					Price402 = PriceSatusHelper.GetPriceByCodeFromList(isLog, strNOTE, lstPrices402, item.ProductCode,
//							item.ProvinceId);

					// gia hang
					PriceOrg = PriceSatusHelper.GetPriceByCodeFromList(isLog, strNOTE, priceOrgList, item.ProductCode,
							item.ProvinceId);
					Logs.Log(isLog, strNOTE, "SiteID == 12,PriceOrg= " + PriceOrg);
				}
				ProductErpPriceDefault deF = new ProductErpPriceDefault();
//				deF.Price402 = Price402;
//				pricesMap.put("Price402_" + item.ProvinceId, Price402);
				deF.PriceOrg = PriceOrg;
				pricesMap.put("PriceOrg_" + item.ProvinceId, PriceOrg);

				deF.WebStatusId = item.WebStatusId;
				deF.ProductID = ProductID;
				deF.IsShowHome = item.IsShowHome ? 1 : 0;
				deF.ProductCode = item.ProductCode;
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
				boolean rsEs = IndexDataListPriceDefault_ES(isLog, strNOTE, pricesMap, ProductID, SiteID, Lang, order);
				if (rsEs == false) {
					Logs.WriteLine("IndexDataListPriceDefault false");
					msg.Code = ResultCode.Retry;
					msg.StackTrace = "IndexDataListPriceDefault false";
					Logs.Log(isLog, strNOTE, "IndexDataListPriceDefault false ResultCode.Retry ");
					return msg;
				}

				synchronized (StatusV1backup.class) {
					if (!Utils.StringIsEmpty(md5)) {
						g_listWebStatusId.put(esKeyTerm, md5);
					}

				}

			}

			boolean rsOr = IndexDataListPriceDefault_OR(message.ProvinceID, isLog, strNOTE, pricesListdefaultByProvince,
					ProductID);
			if (rsOr == false) {
				Logs.WriteLine("IndexDataListPriceDefault_OR false");
				msg.Code = ResultCode.Retry;
				msg.StackTrace = "IndexDataListPriceDefault false";
				Logs.Log(isLog, strNOTE, "IndexDataListPriceDefault_OR false ResultCode.Retry ");
				return msg;
			}
			Logs.Log(isLog, strNOTE, "Push sql default");

			Logs.getInstance().Log(isLog, strNOTE, "pricesListdetaultByProvince ", pricesListdefaultByProvince);
			Logs.Log(isLog, strNOTE, "rsOr = IndexDataListPriceDefault_OR=true");

			MessageQueue messageRepush = new MessageQueue();
			messageRepush.SqlList = new ArrayList<SqlInfo>();

			if (v2) {
				// gia VER2
				var codesMap = listFinalPrices.stream().collect(Collectors.groupingBy(x -> x.RecordID));
				for (var entry : codesMap.entrySet()) {
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
			} else {
				for (ProductErpPriceBO item : listFinalPrices) {
					// check ko update
					if (message.ProvinceID > 0) {
						if (item.ProvinceId != message.ProvinceID) {
							if (item.ProvinceId != 3 && item.ProvinceId != 163) {
								continue;
							}

						}
					}

					ProductErpPriceTMP itemTmp = ConvertToProductErpTMP(item);
					SqlInfo sqlinfo = new SqlInfo();
					RefSql ref = new RefSql();

					Utils.BuildSql(isLog, strNOTE, "product_price", "recordid", item.RecordID, itemTmp, ref);
					sqlinfo.Sql = ref.Sql;
					sqlinfo.Params = ref.params;
					messageRepush.SqlList.add(sqlinfo);

				}
			}
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
			int ie = Utils.GetQueueNum10(ProductID);
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
			QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,
					strNOTE, DataCenter);

			Logs.Log(isLog, strNOTE, "msg.Code = ResultCode.Success");

			msg.Code = ResultCode.Success;
		} catch (Throwable e) {
			Logs.LogException("status.txt", e);
			msg.StackTrace = Utils.stackTraceToString(e);
			msg.Code = ResultCode.Retry;
		}
		return msg;
	}

	public ResultMessage RefreshDelPrice(MessageQueue message) {
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
			if ((SiteID == 2)) {

				return msg;
			}
			PriceArea = DidxHelper.getDefaultPriceAreaBySiteID(SiteID, Lang);
			Logs.Log(isLog, strNOTE,
					"Refresh Pricestatus..." + ProductID + " PriceArea " + PriceArea + " SiteID" + SiteID);
//			

			List<ProductErpPriceBO> lstPrices = null;
			lstPrices = priceHelper.GetListPriceByBySiteID(ProductID, PriceArea, SiteID);
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

			List<Integer> lstProvinceID = List.of(3);
			if (SiteID == 6) {
				lstProvinceID = List.of(163);
			}
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
			synchronized (StatusV1backup.class) {

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

			Map<Integer, ProductErpPriceDefault> pricesListdefaultByProvince = new HashMap<Integer, ProductErpPriceDefault>();
			Map<String, Object> pricesMap = new HashMap<String, Object>();

			Logs.getInstance().Log(isLog, strNOTE, "finalListPriceDefault", finalListPriceDefault);
			int finalListPriceDefaultSize = finalListPriceDefault.size();
			for (int i = 0; i < finalListPriceDefaultSize; i++) {
				var item = finalListPriceDefault.get(i);
				ProductErpPriceDefault deF = new ProductErpPriceDefault();
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
				boolean rsEs = IndexDataListPriceDefault_ES(isLog, strNOTE, pricesMap, ProductID, SiteID, Lang, order);
				if (rsEs == false) {
					Logs.WriteLine("IndexDataListPriceDefault false");
					msg.Code = ResultCode.Retry;
					msg.StackTrace = "IndexDataListPriceDefault false";
					Logs.Log(isLog, strNOTE, "IndexDataListPriceDefault false ResultCode.Retry ");
					return msg;
				}

				synchronized (StatusV1backup.class) {
					if (!Utils.StringIsEmpty(md5)) {
						g_listWebStatusId.put(esKeyTerm, md5);
					}

				}

			}

			boolean rsOr = IndexDataListPriceDefault_OR(0, isLog, strNOTE, pricesListdefaultByProvince, ProductID);
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
//			e.printStackTrace();
//		}
//		return true;
//	}

	private ProductErpPriceTMP ConvertToProductErpTMP(ProductErpPriceBO item) {
		ProductErpPriceTMP ErpPrice = new ProductErpPriceTMP();
		ErpPrice.TotalQuantity = item.TotalQuantity;
		ErpPrice.ProductCodeTotalQuantity = item.ProductCodeTotalQuantity;
		ErpPrice.Quantity = item.Quantity;
		ErpPrice.WebMinStock = item.WebMinStock;
		ErpPrice.ProductCodeQuantity = item.ProductCodeQuantity;
		ErpPrice.CenterQuantity = item.CenterQuantity;
		ErpPrice.WebStatusId = item.WebStatusId;
		return ErpPrice;
	}

	public ResultMessage RunScheduleTask() {
		return null;
	}

	private final Lock queueLock = new ReentrantLock();

	public synchronized boolean IndexDataListPriceDefault_ES(boolean isLog, String strNOTE,
			Map<String, Object> pricesMap, long ProductID, int SiteID, String LangID, int order) {
		String esKeyTerm = ProductID + "_" + SiteID + "_" + (LangID.toLowerCase().replaceAll("-", "_"));

		if (pricesMap.size() > 0) {
			for (int i = 0; i < 10; i++) {

				try {
					queueLock.lock();
					// synchronized (StatusV1.class) {

					var json = mapper.writeValueAsString(pricesMap);
					var update = new UpdateRequest(indexDB, esKeyTerm)
							.doc("{\"Order\":" + order + ", \"Prices\": " + json + "}", XContentType.JSON)
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

	private boolean IndexDataListPriceDefault_OR(int ProvinceID, boolean isLog, String strNOTE,
			Map<Integer, ProductErpPriceDefault> pricesListdetaultByProvince, long ProductID) {
		MessageQueue messageRepush = new MessageQueue();
		messageRepush.SqlList = new ArrayList<SqlInfo>();

		for (ProductErpPriceDefault item : pricesListdetaultByProvince.values()) {
			if (ProvinceID > 0) {
				if (item.ProvinceId != ProvinceID) {
					if (item.ProvinceId != 3 && item.ProvinceId != 163) {
						continue;
					}

				}
			}
			SqlInfo sqlinfo = new SqlInfo();
			RefSql ref = new RefSql();

			Utils.BuildSql(isLog, strNOTE, "price_default", "recordid", item.RecordID, item, ref);
			sqlinfo.Sql = ref.Sql;
			sqlinfo.Params = ref.params;
			messageRepush.SqlList.add(sqlinfo);

//			String egde = "e_product_pricedefault";
//			String cmd2 = "create edge " + egde + " from (select from  product where productid=" + ProductID
//					+ ")   to (select from  price_default   where recordid ='" + item.RecordID + "'   and in('" + egde
//					+ "')[productid = " + ProductID + "].size() = 0)";
//			SqlInfo sqlinfoEdge = new SqlInfo();
//			sqlinfoEdge.Sql = cmd2;
//			messageRepush.SqlList.add(sqlinfoEdge);

			SqlInfo sqlinfoEgde1 = new SqlInfo();
			sqlinfoEgde1.Params = new HashMap<String, Object>();
			sqlinfoEgde1.Type = SqlInfoType.CREATE_EDGE_BY_NODE;
			sqlinfoEgde1.Params.put("edge", "e_product_pricedefault");
			sqlinfoEgde1.Params.put("from", ProductID);
			sqlinfoEgde1.Params.put("to", item.RecordID);

			messageRepush.SqlList.add(sqlinfoEgde1);

		}
//
		messageRepush.Source = "STATUS";

		// Logs.WriteLine("push status sql " + ProductID);
		// int ie = Utils.GetQueueNum(ProductID);
		int ie = Utils.GetQueueNum10(ProductID);
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
