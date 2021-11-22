package mwg.wb.pkg.promotion;

import mwg.wb.business.CacheStaticHelper;
import mwg.wb.business.CategoryHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.PromotionListGroupErp;
import mwg.wb.model.promotion.PromotionBanKemBO;
import mwg.wb.model.promotion.PromotionProductBanKemBO;
import mwg.wb.model.search.ProductSO;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PromotionBanKem implements Ididx {

//	private ObjectMapper mapper = null;
//	private ORThreadLocal factoryRead = null;
	private ErpHelper erpHelper = null;
	private ProductHelper productHelper = null;
	private ClientConfig clientConfig = null;
	private CategoryHelper categoryHelper = null;
	private static final String promotionIndex = "ms_promotion";
	private Map<String, Long> processedBK = new HashMap<>();

	@Override
	public void InitObject(ObjectTransfer o) {
//		mapper = (ObjectMapper) o.mapper;
//		factoryRead = (ORThreadLocal) o.factoryRead;
		erpHelper = (ErpHelper) o.erpHelper;
		productHelper = (ProductHelper) o.productHelper;
		categoryHelper = (CategoryHelper) o.categoryHelper;
		
		clientConfig = (ClientConfig) o.clientConfig;
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;
		String strNOTE = message.Note + "";
		int siteID = message.SiteID;
		String languageID = message.Lang;
		boolean isLog = strNOTE.contains("LOG");
		Logs.getInstance().Log(isLog, strNOTE, "PromotionBanKem", message);
		try {
			// itdentify: promotionid|productid?
			// data: listpromotionid, begin/end date/manu/cate?

//			var iarr = message.Identify.split("\\|");

			Long processed = processedBK.get(message.Identify);
			if (clientConfig.DATACENTER != 3 && processed != null
					&& System.currentTimeMillis() - processed < 21600000L) {
				return r;
			}

			int promotionID = Integer.parseInt(message.Identify);
//			int productID = iarr.length > 1 ? Utils.toInt(iarr[1]) : 0;

			var darr = message.Data.split("\\|");
			Date begin = new Date(Long.parseLong(darr[0]));
			Date end = new Date(Long.parseLong(darr[1]));
			int groupID = 0;
			String groupName = null;
			if (darr.length > 2) {
				groupID = Utils.toInt(darr[2]);
				groupName = darr[3];
			}
//			int pmanuID = Integer.parseInt(darr[2]);
//			int pcateID = Integer.parseInt(darr[3]);

//			if (Strings.isNullOrEmpty(message.Data) || productID <= 0) {
//				Logs.Log(isLog, strNOTE, "message.Data null || productID <= 0 || productCode null");
//				return r;
//			}
			// [{promotion}]
			// var promos = mapper.readValue(message.Data,
			// mwg.wb.model.promotion.Promotion[].class);
			// categoryid,manuid
//			var Manu = factoryRead.QueryFunction("product_getCategoryByProductID", ProductBO[].class, false, productID);
//			int cateID = 0;
//			if (Manu != null && Manu.length > 0) {
//				cateID = Manu[0].CategoryID;
//			}
//			int cateID = productID > 0 ? productHelper.getCategoryID(productID) : 0;
//			var isTele = cateID == 42 || cateID == 522;

//			List<PromotionBanKemBO> allList = new ArrayList<>();

			PromotionListGroupErp[] bkList = getPromotionListGroupByPromoID(erpHelper, promotionID);
			if (bkList == null || bkList.length == 0) {
				return r;
			}

//			List<Integer> listIDAcc = isTele
//					? IntStream.of(productHelper.getCachedProductBankem(productID)).boxed().collect(Collectors.toList())
//					: new ArrayList<>();
//			if (isTele && listIDAcc.size() <= 0) {
//				return r;
//			}
//				CachedObject o;

			int bkCateID = categoryHelper.getCategoryIDByProductIDFromCache( bkList[0].ProductIDRef);

//			int[] ids = Stream.of(bkList)
////		.filter(x -> listIDAcc.contains(x.ProductIDRef))
//					.mapToInt(x -> x.ProductIDRef).toArray();

			Map<Integer, ProductSO> somap = null; // productHelper.getSellingProductNamesMap(ids, siteID, languageID);
//			if (bkCateID == 4728) {
			if (bkList.length <= 500) {
				int[] ids = Stream.of(bkList)
//						.filter(x -> listIDAcc.contains(x.ProductIDRef))
						.mapToInt(x -> x.ProductIDRef).toArray();

				somap = productHelper.getProductNamesMap(ids, siteID, languageID);
			} else {
				somap = DidxHelper.partition(Arrays.stream(bkList).map(x -> x.ProductIDRef).distinct(), 500)
						.flatMap(x -> {
							try {
								return productHelper.getProductNames(x.mapToInt(y -> y).toArray(), siteID, languageID)
										.stream();
							} catch (Throwable e) {
								return Stream.empty();
							}
						}).filter(x -> x != null).collect(Collectors.toMap(x -> x.ProductID, x -> x));
			}
//			}
			var fsomap = somap;

			int fgroupID = groupID;
			String fgroupName = groupName;

			PromotionProductBanKemBO[] lstBanKem = Arrays.stream(bkList)
//					.filter(promotionerp -> !isTele || bkCateID == 7264 || listIDAcc.contains(promotionerp.ProductIDRef))
					.map(erp -> {
						ProductSO so = fsomap != null ? fsomap.get(erp.ProductIDRef) : null;
//						if ((so == null || so.CategoryID != 7264) && isTele && !listIDAcc.contains(erp.ProductIDRef)) {
//							return null;
//						}
						return new PromotionProductBanKemBO() {
							{
								ProductID = erp.ProductIDRef;
								IsPercentDiscount = erp.IsPercentDiscount;
								Quantity = erp.Quantity;
								PromotionPrice = erp.PromotionPrice;
//								CategoryID = bkCateID;

								if (so != null) {
									CategoryID = so.CategoryID;
									ProductCodeTotalQuantity = so.ProductCodeTotalQuantity;
									ProductName = so.ProductName;
								} else if (bkCateID > 0) {
									CategoryID = bkCateID;
								}
							}
						};
					}).filter(x -> x != null).toArray(PromotionProductBanKemBO[]::new);

			PromotionBanKemBO promotionbk = new PromotionBanKemBO() {
				{
					listProductBanKem = lstBanKem;
					TotalProduct = lstBanKem.length;
					BeginDate = begin;
					EndDate = end;
					PromotionID = promotionID;
					IndexDate = new Date();
					PromotionListGroupID = fgroupID;
					PromotionListGroupName = fgroupName;
				}
			};

			ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST).IndexObject(promotionIndex,
					promotionbk, promotionbk.PromotionID + "");
			processedBK.put(message.Identify, System.currentTimeMillis());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			Logs.LogException(e);
			r.StackTrace = Utils.stackTraceToString(e);
			r.Code = ResultCode.Error;
		} catch (Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			r.StackTrace = Utils.stackTraceToString(e);
			r.Code = ResultCode.Retry;
		}
		return r;
	}

	@Override
	public ResultMessage RunScheduleTask() {
		return null;
	}

	public PromotionListGroupErp[] getPromotionListGroupByPromoID(ErpHelper erpHelper, Integer PromotionID)
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

//	public ProductErpPriceBO GetDefaultPriceFromCache(int productID, int siteid, int ProvinceID, int priceArea,
//			String Lang) throws Throwable {
//
//		String key = "XXGetDefaultPriceFromCache" + productID + "_" + ProvinceID + "_" + siteid + "" + priceArea + "_"
//				+ Lang;
//
//		var rs = (ProductErpPriceBO) CacheStaticHelper.GetFromCache(key, 15);
//		if (rs == null) {
//			rs = productHelper.getPriceHelper().getDefaultPriceStrings((int) productID, siteid, ProvinceID, priceArea,
//					Lang);
//			CacheStaticHelper.AddToCache(key, rs);
//		}
//
//		return rs;
//
//	}
//
//	public PromotionListGroupErp[] GetPromotionListGroupByPromoID(ErpHelper erpHelper, Integer PromotionID)
//			throws Throwable {
//
//		String key = "GetPromotionListGroupByPromoID" + PromotionID;
//		var rs = (PromotionListGroupErp[]) CacheStaticHelper.GetFromCache(key, 30);
//		if (rs == null) {
//			rs = erpHelper.GetPromotionListGroupByPromoID(PromotionID, 1);
//			CacheStaticHelper.AddToCache(key, rs);
//
//		}
//
//		CacheStaticHelper.GetFromCache(key, 30);
//
//		return rs;
//
//	}
//
//	@Override
//	public ResultMessage Refresh(MessageQueue message) {
//		ResultMessage rsmsg = new ResultMessage();
//		rsmsg.Code = ResultCode.Success;
//		DataCenter = 0;// message.DataCenter;
//		 
//		try {
//
//			 
//			int promotionID = Utils.toInt(message.Identify);
//			 	if (promotionID < 0) {
//
//				return rsmsg;
//			}
//
//			String strNOTE = message.Note + "";
//
//			boolean isLog = false;
//			if (strNOTE.contains("DIDX_TOP_LOG") || strNOTE.contains("DIDX_LOG")) {
//
//				isLog = true;
//			}
//			if (strNOTE.contains("DIDX_TOP")) {
//
//				notifyHelperLog.Notify("SERVICE-PROMOTION BANKEM:" + message.Identify, DataCenter);
//			}
//			 
//
//			String allLog = "";
//			var timer = new CodeTimer("checktime");
//			timer.reset("checkexist");
//
//			int siteid = message.SiteID;
//			String Lang = message.Lang;
//			if (Utils.StringIsEmpty(Lang)) {
//				Lang = DidxHelper.getLangBySiteID(siteid);
//
//			}
//
//			var dataDetail = GetPromotionListGroupByPromoID(erpHelper, promotionID);// erpHelper.GetPromotionListGroupByPromoID(item.PromotionID);
//
//			if (dataDetail != null  ) {
//				 
//					 String data=mapper.writeValueAsString(dataDetail);
//					MessageQueue messageRepushV2 = new MessageQueue();
//					messageRepushV2.SqlList = new ArrayList<SqlInfo>();
//					messageRepushV2.SiteID = message.SiteID;
//					SqlInfo sqlinfo0 = new SqlInfo();
//					sqlinfo0.Sql = "update promotionbankem  set data=:data  upsert where promotionid=" + promotionID + "";
//
//					sqlinfo0.tablename = "promotionbankem";
//					sqlinfo0.tablekey = "promotionid";
//					sqlinfo0.Params = new HashMap<String, Object>();
//					sqlinfo0.Params.put("promotionid", promotionID);
//					sqlinfo0.Params.put("data", data); 
//					messageRepushV2.SqlList.add(sqlinfo0);
//
//					int ieV2 = 0;
//					String quV2 = "gr.dc4.sqlpro" + ieV2;
//					String qu2V2 = "gr.dc4.sqlpro" + ieV2;
//					String qubkV2 = "gr.dc2.sqlpro" + ieV2;
//					String qudevV2 = "gr.beta.sqlpro";
//
//					messageRepushV2.Action = DataAction.Update;
//					messageRepushV2.ClassName = "ms.upsert.Upsert";
//					messageRepushV2.CreatedDate = Utils.GetCurrentDate();
//					messageRepushV2.Lang = message.Lang;
//					messageRepushV2.SiteID = message.SiteID;
//					messageRepushV2.Source = "PRO";
//					messageRepushV2.RefIdentify = message.Identify;
//					messageRepushV2.Identify = String.valueOf(promotionID);
//					messageRepushV2.Hash = 0;
//					messageRepushV2.Note = message.Note;
//
//					messageRepushV2.DataCenter = message.DataCenter;
//					QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(quV2, qu2V2, qubkV2, qudevV2,
//							messageRepushV2, isLog, message.Note, 0); // 0 bom 2 cái
//					Logs.Log(isLog, message.Note, "Push promotionbankem " + promotionID);
//				}
//
//			 
//
//			rsmsg.Code = ResultCode.Success;
//		} catch (Throwable e) {
//			rsmsg.StackTrace = Utils.stackTraceToString(e);
//			rsmsg.Code = ResultCode.Retry;
//			Logs.LogException(e);
//		}
//		return rsmsg;
//
//	}
}
