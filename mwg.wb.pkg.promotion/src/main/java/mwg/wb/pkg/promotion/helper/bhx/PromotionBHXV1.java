package mwg.wb.pkg.promotion.helper.bhx;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.helper.BHXStoreHelper;
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.BhxServiceHelper;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.client.service.ErpHelper;
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
import mwg.wb.common.notify.LineNotify;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.promotion.BHXPromotionType;
import mwg.wb.model.promotion.GiftBHX;
import mwg.wb.model.promotion.ProductRelevantSO;
import mwg.wb.model.promotion.ProductRelevantType;
import mwg.wb.model.promotion.PromotionBHX;
import mwg.wb.model.promotion.PromotionBHXBO;
import mwg.wb.model.promotion.PromotionGRBO;
import mwg.wb.model.promotion.PromotionTypes;
import mwg.wb.model.promotion.RelevantInfo;
import mwg.wb.model.promotion.URLInfo;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PromotionBHXV1 implements Ididx {
	// private OrientDBFactory factoryDB = null;
	private PriceHelper priceHelper = null;
	private ProductHelper productHelper = null;
	private BhxServiceHelper bhxServiceHelper = null;
	private ErpHelper erpHelper = null;
//	private ORThreadLocal factoryWrite = null;
//	private ORThreadLocal factoryRead = null;
	private ClientConfig clientConfig = null;
	BHXStoreHelper bhxStoreHelper = null;
	private ObjectMapper mapper = null, esMapper = null;

	String indexDB = "";
	LineNotify notifyHelperLog = null;

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {

//		factoryWrite = (ORThreadLocal) objectTransfer.factoryWrite;
//		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		priceHelper = (PriceHelper) objectTransfer.priceHelper;
		productHelper = (ProductHelper) objectTransfer.productHelper;
		mapper = (ObjectMapper) objectTransfer.mapper;
		bhxServiceHelper = (BhxServiceHelper) objectTransfer.bhxServiceHelper;
		erpHelper = (ErpHelper) objectTransfer.erpHelper;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		indexDB = clientConfig.ELASTICSEARCH_PRODUCT_INDEX;
		bhxStoreHelper = (BHXStoreHelper) objectTransfer.bHXStoreHelper;
		esMapper = DidxHelper.generateJsonMapper(GConfig.DateFormatStringNews);
		notifyHelperLog = (LineNotify) objectTransfer.notifyHelperLog;
	}

	public void UpsertODatabaseJson(boolean isLog, String note, String VertexClasses, String recordName,
			String recordValue, Object obj) throws IllegalArgumentException, IllegalAccessException {

		Field[] fields = obj.getClass().getFields();

		String sql = "update " + VertexClasses + " SET ";
		for (final Field _item : fields) {
			String cl = _item.getName().toLowerCase();
			sql = sql + cl + "=:" + cl + ",";

		}
		sql = StringUtils.strip(sql, ",");
		Map<String, Object> params = new HashMap<String, Object>();

		for (final Field _item : fields) {
			Object cc = _item.get(obj);
			String o = _item.getName().toLowerCase();
			if (cc != null) {
				String value = String.valueOf(cc);
				// Logs.WriteLine(o+"="+value);
				Type f = _item.getType();
				if (f == Date.class) {
					params.put(o, Utils.FormatDateForGraph((Date) cc));
				} else if (f == boolean.class) {

					params.put(o, Boolean.valueOf(value) == true ? 1 : 0);

				} else if (f == byte.class) {
					params.put(o, Byte.valueOf(value));
				} else if (f == char.class) {
					params.put(o, value.charAt(0));
				} else if (f == short.class) {
					params.put(o, Short.valueOf(value));
				} else if (f == int.class) {
					params.put(o, Integer.valueOf(value));
				} else if (f == long.class) {
					params.put(o, Long.valueOf(value));
				} else if (f == float.class) {
					params.put(o, Float.valueOf(value));
				} else if (f == double.class) {
					params.put(o, Double.valueOf(value));

				} else if (f == String.class) {
					params.put(o, String.valueOf(value));
				} else {
					params.put(o, String.valueOf(value));

				}

			} else {

				params.put(o, null);
			}

		}
		sql = sql + " Upsert where " + recordName + "='" + recordValue + "'";
		Logs.Log(isLog, note, sql);
		PushSysData(isLog, note, sql, params);

	}

	public int PushSysData(boolean isLog, String Note, String sql, Map<String, Object> params) {

		return 1;
	}

	public int PushSysData(String sql) {

		return 1;
	}

	public int PushSysData(boolean isLog, String Note, String sql) {

		return 1;
	}

	public boolean IndexDataES(mwg.wb.model.promotion.PromotionBHX[] lstPromotion, long ProductID, int SiteID,
			String LangID, int DataCenter, String field) {
		String esKeyTerm = ProductID + "_" + SiteID + "_" + (LangID.toLowerCase().replaceAll("-", "_"));

		if (lstPromotion == null) {

			lstPromotion = new mwg.wb.model.promotion.PromotionBHX[] {};
		}
		// var solist = Stream.of(lstPromotion).map(x ->
		// x.getSOObject()).toArray(PromotionSO[]::new);
		// int percent = 0;
		// if (lstPromotion != null && lstPromotion.length > 0) {
		for (int i = 0; i < 5000000; i++) {

			try {
//				var promoJson = esMapper.writeValueAsString(solist);
//				String json = "{\"PromotionSoList\":" + promoJson + ", \"PromotionDiscountPercent\": " + percent + "}";
//				return ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)//
//						.updateObject(indexDB, esKeyTerm, json);

				return ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
						.UpdateFieldOBject(indexDB, esKeyTerm, field, lstPromotion);

			} catch (Exception e) {
				Logs.WriteLine("Exception Index promotion status to ES FAILED: " + ProductID);
				Logs.WriteLine(e);
				Utils.Sleep(300);
			}
		}

		// }else {}

		return false;
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage rsmsg = new ResultMessage();
		rsmsg.Code = ResultCode.Success;

		// productcode|
		String[] messIdentify = message.Identify.split("\\|");
		if (messIdentify.length < 2) {
			Logs.WriteLine(messIdentify + ":" + messIdentify.length);
			return rsmsg;
		}
		String strNOTE = message.Note + "";

		boolean isLog = false;
		if (strNOTE.contains("DIDX_TOP_LOG") || strNOTE.contains("DIDX_LOG")) {
			isLog = true;
		}
		if (strNOTE.contains("DIDX_TOP")) {
			 notifyHelperLog.NotifyInfo("SERVICE-PROMOTION_BHX:" + message.Identify, message.DataCenter);
		}
		
		// ko phải site bhx thì return
		if (message.SiteID != 11) {
			return rsmsg;
		}
		
		String productCode = messIdentify[0].trim();
		int SalePrice = -1;
		int OutputTypeID = 0;
//		int ProvinceID = -1;
		int InventoryStatusID = 1;
		final int siteid = message.SiteID;
		final String Lang = Utils.StringIsEmpty(message.Lang) ? "vi-VN" : message.Lang;
		int BrandID = 3;
		
		try {
			MessageQueue messageRepush = new MessageQueue();
			messageRepush.SqlList = new ArrayList<SqlInfo>();
			messageRepush.SiteID = siteid;

//			mwg.wb.model.promotion.Promotion[] lstpromotion = new mwg.wb.model.promotion.Promotion[] {};

			// xu ly khuyen mai BHX
			
			String arecordid = siteid + "_" + BrandID + "_" + "_" + SalePrice + "_" + OutputTypeID + "_"
					+ InventoryStatusID;

			// arecordid = siteid + "_" + SalePrice + "_" + OutputTypeID + "_" +
			// InventoryStatusID;
			//String recordidcommon = siteid + "_" + BrandID + "_" + SalePrice + "_" + OutputTypeID + "_"
				//	+ InventoryStatusID + "_" + productCode;
//			SqlInfo sqlinfoDelBHX = new SqlInfo();
//			sqlinfoDelBHX.Sql = "update product_promotion set isdeleted=1 where recordidcommon='" + recordidcommon
//					+ "'";
//			messageRepush.SqlList.add(sqlinfoDelBHX);
			var timer = new CodeTimer("checktime");
			Logs.Log(isLog, strNOTE, "GetPromotionBHXByProductID " + arecordid);

			long ProductID = productHelper.GetProductIDByProductCodeFromCache(productCode);
			Logs.Log(isLog, strNOTE, "BHX Product ID " + ProductID);
			if (ProductID > 0) {
//				String dfcode = productHelper.getDefaultCodeFromCache(ProductID, message.SiteID,
//						message.SiteID == 6 ? 163 : 3);
//				if (dfcode != null && dfcode.equals(productCode)) {
					
					timer.reset("get promotion");
					var promoBHX = processPromotionBHX(message, productCode);
//					if(Arrays.asList(promoBHX).contains(140614))
//					{
//						System.out.println(" co pro");
//					}
					timer.end();

					if (promoBHX != null) {
						Logs.Log(isLog, strNOTE, "BHX promoBHX != null ");
						MessageQueue messageRepushV2 = new MessageQueue();
						messageRepushV2.SqlList = new ArrayList<SqlInfo>();
						messageRepushV2.SiteID = message.SiteID;
						String recordidcommonV2 = siteid + "_" + BrandID + "_0_" + OutputTypeID + "_"
								+ InventoryStatusID + "_" + productCode;
						String Data = mapper.writeValueAsString(promoBHX);
						PromotionGRBO item1 = new PromotionGRBO();
						item1.Data = Data;
						item1.OutputTypeID = OutputTypeID;
						item1.SalePrice = SalePrice;
						item1.SiteID = siteid;
						item1.ProductCode = productCode;
						item1.recordid = recordidcommonV2;
						item1.BrandID = BrandID;
						item1.didxupdateddate = Utils.GetCurrentDate();
						item1.IsDeleted = 0;
						item1.LangID = Lang;
						item1.didxupdateddate = Utils.GetCurrentDate();
						item1.InventoryStatusID = InventoryStatusID;
						SqlInfo sqlinfo1 = new SqlInfo();
						RefSql ref1 = new RefSql();
						Utils.BuildSql(isLog, strNOTE, "productpromotion", "recordid", recordidcommonV2, item1, ref1);
						sqlinfo1.Sql = ref1.Sql;
						sqlinfo1.Params = ref1.params;

						sqlinfo1.tablename = ref1.lTble;
						sqlinfo1.tablekey = ref1.lId;
						
						messageRepushV2.SqlList.add(sqlinfo1);

						SqlInfo sqlinfoEgde1 = new SqlInfo();
						sqlinfoEgde1.Params = new HashMap<String, Object>();
						sqlinfoEgde1.Type = SqlInfoType.CREATE_EDGE_BY_NODE;
						sqlinfoEgde1.Params.put("edge", "e_codepromotion");
						sqlinfoEgde1.Params.put("from", productCode.trim());
						sqlinfoEgde1.Params.put("to", recordidcommonV2);
						messageRepushV2.SqlList.add(sqlinfoEgde1);

						messageRepushV2.Source = "PROMOTION";
						messageRepushV2.RefIdentify = message.Identify;
						messageRepushV2.Action = DataAction.Update;
						messageRepushV2.ClassName = "ms.upsert.Upsert";
						messageRepushV2.CreatedDate = Utils.GetCurrentDate();
						messageRepushV2.Lang = message.Lang;
						messageRepushV2.SiteID = siteid;
						messageRepushV2.Identify = String.valueOf(ProductID);
						messageRepushV2.Hash = ProductID;
						messageRepushV2.CachedType=1;//cần xoa cache
						messageRepushV2.Note = strNOTE;
						messageRepushV2.DataCenter = message.DataCenter;
						Logs.LogRefresh("promotionrefresh", message,
								  messageRepushV2.SqlList.size()+"",siteid);
						try {
							int ieV2 = Utils.GetQueueNum(ProductID);
							String quV2 = "gr.dc4.sqlpro" + ieV2;
							String qu2V2 = "gr.dc4.sqlpro" + ieV2;
							String qubkV2 = "gr.dc2.sqlpro" + ieV2;
							String qudevV2 = "gr.beta.sqlpro";
							QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(quV2, qu2V2, qubkV2, qudevV2,
									messageRepushV2, isLog, strNOTE, 0);
							Logs.getInstance().Log(isLog, strNOTE, "messageRepush KM ", messageRepushV2);
						} catch (Exception e) {
							Logs.LogException(e );
							rsmsg.Code = ResultCode.Retry;
							rsmsg.StackTrace = Utils.stackTraceToString(e);
							Logs.Log(isLog, strNOTE, "push promotion  " + e.getMessage());
							return rsmsg;
						}
//					}
				}

			}
			
			if (strNOTE.contains("DIDX_TOP")) {
				 notifyHelperLog.NotifyInfo("SERVICE-PROMOTION_BHX-TIMER:" + timer.getLogs(), message.SiteID);
			}
			rsmsg.Code = ResultCode.Success;
		} catch (Throwable e) {

			rsmsg.Code = ResultCode.Retry;
			rsmsg.StackTrace = Utils.stackTraceToString(e);
			Logs.LogException(e );
		}
		
		return rsmsg;

	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unlikely-arg-type")
	public PromotionBHX[] processPromotionBHX(MessageQueue message, String productCode) throws Throwable {

		var lstNormalStore = bhxStoreHelper.getListNormalStore();
		var lstOutStore = bhxStoreHelper.getListOutStockStore();
		var lstpromotions = new ArrayList<PromotionBHXBO>();
		var clearStockPromotions = new ArrayList<PromotionBHXBO>();
		ArrayList<Integer> dicAllStore = new ArrayList<Integer>();

		// kho thuong
		// suy nghi cho nay viết storeids là 1 list, nếu km áp dụng all kho thì add hết
		// list store vào đó để giảm data promo
		for (var store : lstNormalStore) {
			var tmp = bhxServiceHelper.GetPromotionBHXByPrdID(productCode, 1000, store, true, 60);
			if (tmp != null && tmp.length > 0) {
				for (var prom : tmp) {
					// ap dung cho all kho (co km offline)
					if (prom.STOREID == 0) {
						dicAllStore.add(prom.PromotionID);
					}
					prom.STOREID = store;
					lstpromotions.add(prom);
				}
			}
		}

		// Lấy KM clear tồn lên
		for (var item : lstNormalStore) {
			var tmp = bhxServiceHelper.GetPromotionByProductClearStock(productCode, item, "1000", 0,
					Utils.FormatDateForGraph(Utils.GetCurrentDate()));

			if (tmp != null && tmp.length > 0) {
				for (var row : tmp) {
					var clearPromo = new PromotionBHXBO();
					clearPromo.STOREID = item;
					clearPromo.PromotionID = row.PromotionID;
					clearPromo.PromotionName = row.PromotionName;
					clearPromo.Description = row.Description;
					clearPromo.PromotionType = row.PromotionType;
					clearPromo.APPLYSUBGROUPID = row.APPLYSUBGROUPID;
					clearPromo.FromDate = row.FromDate;
					clearPromo.ToDate = row.ToDate;
					clearPromo.DiscountType = row.DiscountType;
					clearPromo.DiscountValue = row.DiscountValue;
					clearPromo.PromotionGiftType = row.PromotionGiftType;
					clearPromo.ApplyProductID = row.ApplyProductID;
					clearPromo.ApplyProductIDRef = row.ApplyProductIDRef;
					clearPromo.CorrespondingMonney = row.CorrespondingMonney;
					clearPromo.LimitQuantity = row.LimitQuantity;
					clearPromo.MAXQUANTITY = row.MAXQUANTITY;
					clearPromo.RequestQuantity = row.RequestQuantity;
					clearPromo.DonateType = row.DonateType;
					clearStockPromotions.add(clearPromo);
				}
			}
		}

		if (clearStockPromotions != null && clearStockPromotions.size() > 0) {
			lstpromotions.addAll(clearStockPromotions);
		}

		var promotions = new PromotionBHXBO[lstpromotions.size()];
		promotions = lstpromotions.toArray(promotions);

		// kho can date
		var lstOutStorePromo = new ArrayList<PromotionBHXBO>();
		for (var store : lstOutStore) {
			if(store <1)
				continue;
			
			var tmp = bhxServiceHelper.GetPromotionBHXByPrdID(productCode, 1000, store, true, 60);
			if (tmp != null && tmp.length > 0) {
				for (var prom : tmp) {
					// ap dung cho all kho (co km offline)
					if (prom.STOREID > 0) {
						prom.STOREID = store;
						lstOutStorePromo.add(prom);
					}
				}
			}
		}

		var exdPromotions = new PromotionBHXBO[lstOutStorePromo.size()];
		exdPromotions = lstOutStorePromo.toArray(exdPromotions);

		PromotionBHX[] curPromotions = null;

//		PromotionBHX[] curComboPromotions = null;
//		if (message.Action == DataAction.Update) {
//			var result = mwg.wb.business.APIOrientClient.GetOrientClient(clientConfig).QueryFunction(
//					"product_GetPromoByProductID", Promotion[].class, false, productCode, message.SiteID, message.Lang);
//			if (result != null && result.length > 0) {
//				curPromotions = new PromotionBHX[result.length];
//				for (var i = 0; i < result.length; i++) {
//					var promo = new PromotionBHX();
//					promo.setBeginDate(result[i].BeginDate);
//					// item.ComboCode = promo.ComboCode;
//					promo.Description = result[i].Description;
//					promo.setEndDate(result[i].EndDate);
//					promo.Excludes = Arrays.asList(result[i].ExcludePromotion.split(",", -1)).stream()
//							.map(s -> Utils.toInt(s)).collect(Collectors.toList());
//					// item.FromTime = promo.FromTime;
//					promo.Gifts = result[i].Gifts;
//					
//					//promo.GiftBHX = 
//					
//					promo.GiftType = result[i].GiftType;
//					promo.Id = result[i].PromotionID;
//					// item.Images = promo.Images;
//					// promo.IsBetter = promo.IsBetter;
//					// promo.IsNotApplyWithSpecialInstallment =
//					// promo.IsNotApplyWithSpecialInstallment;
//					promo.IsOnlineOnly = result[i].IsOnline;
//					promo.IsPercent = result[i].IsPercentDiscount;
//					promo.Limit = result[i].Quantity;
//					promo.LimitedCount = result[i].LimitQuantity;
//					promo.MaxValue = result[i].FromValue;
//					promo.MinValue = result[i].ToValue;
//					promo.PromotionListGroupId = result[i].PromotionListGroupName;
//					promo.PromotionRealDiscountValue = (int) result[i].SalePrice;
//					promo.QuantityCondition = result[i].RequestQuantity;
//					promo.StoreIds = promo.StoreIds;
//					// item.STOREID = promo.StoreIds;
//					// item.ToTime = promo.ToTime;
//					switch (result[i].PromotionType) {
//					case 0:
//						promo.Type = PromotionTypes.Discount;
//						break;
//					case 1:
//						promo.Type = PromotionTypes.Gift;
//						break;
//					case 2:
//						promo.Type = PromotionTypes.Undefined;
//						break;
//					case 3:
//						promo.Type = PromotionTypes.AndDiscountGift;
//						break;
//					case 4:
//						promo.Type = PromotionTypes.OrDiscountGift;
//						break;
//					case 5:
//						promo.Type = PromotionTypes.Combo;
//						break;
//					case 6:
//						promo.Type = PromotionTypes.Text;
//						break;
//					}
//					promo.Value = result[i].DiscountValue;
//					promo.MaxQuantityOnBill = promo.MaxQuantityOnBill;
//
//					// add vào curPromotions
//					curPromotions[i] = promo;
//				}
//			}
//		}		

		var lstNewPromos = new ArrayList<PromotionBHX>();
		var comboPromotions = new ArrayList<PromotionBHX>();
		var flashSalePromotions = new ArrayList<PromotionBHX>();
		var productId = productHelper.GetProductIDByProductCodeFromCache(productCode);
		if(productId <= 0)
		{
			throw new Throwable("Product Not Found");
		}
		//var product = productHelper.getProductBOByProductID_PriceStrings((int) productId, message.SiteID, 3,
		//		message.Lang, 644);
		// KM hang can date
		var expiredPromotions = new ArrayList<PromotionBHX>();

		if (promotions != null && promotions.length > 0) {
			// loai tru khuyen mai khai truong
			promotions = Arrays.stream(promotions).filter(p -> p.PromoStoreType != 1).toArray(PromotionBHXBO[]::new);
			for (var row : promotions) {
				// Khuyen mai dich danh nhung khai bao theo nhom
				// Cung nhom thi hien nhien ap dung cho san pham
				row.ApplyProductIDRef = Utils.toString(productId);
				if (Utils.StringIsEmpty(row.ApplyProductID) 
						//&& row.APPLYSUBGROUPID == product.IsPartnerProduct
						) {
					row.ApplyProductID = productCode;
					row.ApplyProductIDRef = Utils.toString(productId);
				}

//				if (!row.ApplyProductID.equals(null) || Utils.toInt(row.ApplyProductIDRef) < 1
//						|| !row.ApplyProductID.trim().equals(productCode)
//						|| !row.ApplyProductIDRef.equals(Utils.toString(product.ProductID))) {
//					continue;
//				}
			}

			Date maxExpiredate = Utils.AddDay(Utils.GetCurrentDate(), -1);
			var lstPromo = Arrays.asList(promotions);
			// bỏ những khuyến mãi áp dụng cho 1 kho trùng nhau

			// var distinctPromotion =
			// lstPromo.stream().distinct().toArray(PromotionBHXBO[]::new);

			var distinctPromotion = lstPromo.stream()
					.filter(distinctByKeys(PromotionBHXBO::getPromotionID, PromotionBHXBO::getSTOREID))
					.toArray(PromotionBHXBO[]::new);
			for (var item : distinctPromotion) {
				System.out.println(" Store: " + item.STOREID);
				// tam thoi chi cho KM tang qua va KM giam gia duoc phep hien thi web
				if (item.PromotionGiftType != 4 && item.PromotionGiftType != 3)
					continue;

				if (!item.ApplyProductID.equals(null) && !item.ApplyProductID.trim().equals(productCode)
						|| !item.ApplyProductIDRef.equals(Utils.toString(productId))) {
					continue;
				}

				// if (item.PromotionGiftType == 3 && item.RequestQuantity > 1)
				// {
				// continue;
				// }

				PromotionBHX promo = new PromotionBHX();
				if (item.FromDate != null) {
					Date dateFr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.FromDate);
					promo.setBeginDate(dateFr);
				}
				if (item.ToDate != null) {
					Date dateTo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.ToDate);
					promo.setEndDate(dateTo);
				}
				promo.BHXPromotionType = BHXPromotionType.NORMAL;
				promo.StoreIds = Utils.toString(item.STOREID);
				promo.ProvinceId = bhxStoreHelper.getProvinceByStore(item.STOREID);
				promo.Description = item.Description;
				promo.Id = item.PromotionID * -1;
				promo.MinValue = item.CorrespondingMonney;
				promo.MaxValue = item.CorrespondingMonney;
				promo.Limit = item.LimitQuantity;
				promo.IsOnlineOnly = !dicAllStore.contains(item.PromotionID);
				promo.MaxQuantityOnBill = (int) item.MAXQUANTITY;
				// khuyen mai tu BHX thi them promotionlistgroupid = 0
				promo.PromotionListGroupId = "0";
				if (Utils.StringIsEmpty(item.getProductID()) && item.DiscountValue > 0) {
					// Khuyen mai giam tien
					promo.Type = PromotionTypes.Discount;
					promo.IsPercent = item.DiscountType == 1;
					promo.Value = item.DiscountValue;
					promo.QuantityCondition = item.RequestQuantity; // So san pham phai mua de duoc giam gia

					lstNewPromos.add(promo);
				} else {
					// Khuyen mai tang qua
					promo.Type = PromotionTypes.Gift;

					// "donatetype" IS ' 1: chi tang 1, 2: Tang theo tong so luong; 3: Tang tat ca';
					promo.GiftType = item.DonateType == 3;

					promo.QuantityCondition = 9999;
					List<PromotionBHXBO> gifts = Arrays.stream(promotions)
							.filter(p -> p.PromotionID == item.PromotionID)
							.filter(distinctByKeys(PromotionBHXBO::getProductID)).collect(Collectors.toList());// .collect(Collectors.groupingBy(PromotionBHXBO::getProductId));
					// List<PromotionBHXBO> gifts = promotions.Where(p -> p.PromotionID ==
					// item.PromotionID).GroupBy(g -> g.ProductID).Select(p -> p.First()).ToList();

					if (gifts.size() > 0) {
						var tmp = new GiftBHX[gifts.size()];
						int i = 0;
						for (var g : gifts) {
							if (g.DefineQuantity < 1 || g.RequestQuantity < 1 || Utils.toInt(g.ProductIDRef) < 1) {
								continue;
							}
							var gTmp = new GiftBHX();
							gTmp.AppliedQuantity = g.DefineQuantity;
							gTmp.setQuantityCondition(g.RequestQuantity);
							gTmp.ProductID = Utils.toInt(g.ProductIDRef);
							gTmp.ProductCode = g.getProductID();
							gTmp.Name = g.ProductName;
							// Tạm thời giữ lại để tránh lỗi, sẽ bỏ đi sau khi cập nhật xong
							var productInfo = new ProductBO();
							productInfo.ProductID = Utils.toInt(g.ProductIDRef);
							productInfo.ProductCode = g.getProductID();
							productInfo.ProductName = g.ProductName;
							gTmp.ProductInfo = productInfo;
							tmp[i] = gTmp;
							i++;
						}
						tmp = Arrays.stream(tmp).filter(p -> p != null).toArray(GiftBHX[]::new);
						promo.Gifts = mapper.writeValueAsString(tmp);
						promo.GiftBHX = tmp;				

						if (promo.Gifts.length() > 0) {
							// Cap nhat lai thong tin qua tang de toi uu xu ly tren WEB
							var giftObj = mapper.readValue(promo.Gifts, GiftBHX[].class);
							var giftPrdId = new int[giftObj.length];
							if (giftObj != null && giftObj.length > 0) {
								for (var obj = 0; obj < giftObj.length; obj++) {
									giftPrdId[obj] = giftObj[obj].ProductID;
								}
							}
							var provineid = bhxStoreHelper.getProvinceByStore(item.STOREID);
							var giftsBO = productHelper.GetProductBOByListID_PriceStrings(giftPrdId, 11, provineid, message.Lang);
							if (giftsBO != null && giftsBO.length > 0) {
								for (var g : giftsBO) {
									if (g == null)
										continue;
									var gift = Arrays.stream(giftObj)
											.filter(f -> f.ProductInfo.ProductID == g.ProductID).findFirst().get();
									if (gift == null)
										continue;									
									
									//lấy price của store hiện tại để lấy unit, quantity
									if(g.ProductErpPriceBOList == null || g.ProductErpPriceBOList.length == 0)
									{
										continue;
									}
									var price = Arrays.stream(g.ProductErpPriceBOList).filter(x->x.StoreID == item.STOREID).findFirst().get();
									if (price == null)
									{
										continue;
									}
									if(!Utils.StringIsEmpty(price.QuantityUnit)) {
										gift.Unit = price.QuantityUnit.toLowerCase().replace("hủ", "hũ");
									}
									//if(gift.Unit.equals("hủ"))
									//	gift.Unit = "hũ";
									gift.setStockQuantity(price.Quantity);

									gift.Name = g.ProductName;
									// Cap nhat lai hinh dai dien
									if (g.ProductLanguageBO != null && g.ProductLanguageBO.bimage != null && g.ProductLanguageBO.bimage.contains("300x300")) {
										gift.Avatar = GenImgUrl(g.CategoryID, g.ProductID, g.ProductLanguageBO.bimage.replace("-300x300", ""));
									} else if (g.ProductLanguageBO != null && g.ProductLanguageBO.mimage != null) {
										gift.Avatar = GenImgUrl(g.CategoryID, g.ProductID,
												g.ProductLanguageBO.mimage.replace("_resize", "_300x300"));
									}

									// Url qua tang
									gift.Url = URLInfo.GenUrl(g);
								}
							}

							if (giftObj.length > 0) {
								Comparator<GiftBHX> comparator = Comparator.comparing(GiftBHX::getQuantityCondition);
								promo.QuantityCondition = Arrays.stream(giftObj).min(comparator).get()
										.getQuantityCondition();
							}

							if (message.ID != -999) {
								// TODO check san pham qua tang neu chua duoc khai bao gia thi tao object gia
								// sau do push notification qua ton de cho san pham KM duoc len
								for (var g : giftObj) {
									if (g == null)
										continue;
									// ko tim thay san pham qua tang thi noti giá defaut de tao sp
								}

								// Chi luon lay toi da 10 qua tang co nhieu ton nhat
								// Tranh object khuyen mai qua lon
								if (giftObj.length > 0) {
									var giftTmp = Arrays.stream(giftObj)
											.sorted(Comparator.comparingInt(GiftBHX::getStockQuantity)).limit(10)
											.toArray(GiftBHX[]::new);
									promo.Gifts = mapper.writeValueAsString(giftTmp);
									promo.GiftBHX = giftTmp;
								}
							}
						}
					}

					if (promo.Gifts != null && promo.Gifts.length() > 0)
						lstNewPromos.add(promo);
				}

				if (promo.getEndDate() != null) {
					if (promo.getEndDate().compareTo(maxExpiredate) > 0) {
						maxExpiredate = promo.getEndDate();
					}
				}

				// cập nhật thông tin khuyến mãi cho sp liên quan
				if (promo.getBeginDate().compareTo(Utils.GetCurrentDate()) <= 0
						&& Utils.GetCurrentDate().compareTo(promo.getEndDate()) <= 0) {
					UpdateProductRelevant(productId, promo);
					UpdateComboRelevant(productId);
				}
			}

			// Cap nhat gia tri KM thuc cho danh sach KM moi
			for (var item : lstNewPromos) {
				var tmp = new ArrayList<PromotionBHX>();
				tmp.add(item);
				// TODO: sua ham nay
				var provineId = bhxStoreHelper.getProvinceByStore(Utils.toInt(item.StoreIds));
				var pricePO = priceHelper.getDefaultPriceStrings((int) productId, message.SiteID, provineId, 644,
						message.Lang);
				double price = 0;
				if (pricePO != null)
					price = pricePO.Price;
				var realDiscountValue = PromotionBHXHelper.CalculateRealDiscountValue(tmp, productId, productCode,
						message.SiteID, provineId, price, Utils.toInt(item.StoreIds), priceHelper);
				item.PromotionRealDiscountValue = realDiscountValue;
			}

			// Lay ra khuyen mai hien tai trong REDIS da cap nhat lai bo dem
//			if (curPromotions != null) {
//				for (var curPromotion : curPromotions) {
//					var promo = lstNewPromos.stream().filter(p -> p.Id == curPromotion.Id).collect(Collectors.toList());
//
//					if (promo != null) {
//						for (var cpro : promo) {
//							cpro.LimitedCount = curPromotion.LimitedCount;
//						}
//
//					}
//				}
//			}
//			if (curComboPromotions != null) {
//				for (var curPromotion : curComboPromotions) {
//					var promo = lstNewPromos.stream().filter(p -> p.Id == curPromotion.Id).limit(1)
//							.collect(Collectors.toList());
//					if (promo != null) {
//						promo.get(0).LimitedCount = curPromotion.LimitedCount;
//					}
//				}
//			}

			// Trich ra danh sach KM giam tien nhung dieu kien ap dung lon hon 1
			// Luu tru vao mot key REDIS khac
			comboPromotions = (ArrayList<PromotionBHX>) lstNewPromos.stream()
					.filter(p -> p.Type == PromotionTypes.Discount && p.QuantityCondition > 1)
					.collect(Collectors.toList());

			if (comboPromotions != null && !comboPromotions.isEmpty()) {
				for (PromotionBHX promotionBHX : comboPromotions) {
					promotionBHX.BHXPromotionType = BHXPromotionType.COMBO;
				}
			}

			// Looi bo khoi danh sach ap dung tuc thi
			lstNewPromos.removeIf(p -> p.Type == PromotionTypes.Discount && p.QuantityCondition > 1);

		}

		// xử lý KM cận date
		for (var _store : lstOutStore) {
			if(_store<1)
				continue;
			
			System.out.println(" ex Store: " + _store);
			// var province = StoreProvinceConfig.GetProvinceByStore(_store);
			if (exdPromotions != null && exdPromotions.length > 0) {
				// chỉ lấy những KM cho kho 4472 và ko phải KM khai trương (PromoStoreType = 1)
				// lên
				var allExpirePromotion = Arrays.stream(exdPromotions)
						.filter(p -> p.STOREID > 0 && p.STOREID == _store && p.PromoStoreType != 1)
						.toArray(PromotionBHXBO[]::new);
				for (var row : allExpirePromotion) {
					// Khuyen mai dich danh nhung khai bao theo nhom
					// Cung nhom thi hien nhien ap dung cho san pham
					row.ApplyProductIDRef = Utils.toString(productId);
					if (Utils.StringIsEmpty(row.ApplyProductID) 
							//&& row.APPLYSUBGROUPID == product.IsPartnerProduct
							) {
						row.ApplyProductID = productCode;
						row.ApplyProductIDRef = Utils.toString(productId);
					}		

//					if (row.ApplyProductID == null 
//							|| Utils.toInt(row.ApplyProductIDRef) < 1
//							|| !row.ApplyProductID.trim().equals(productCode)
//							|| !row.ApplyProductIDRef.equals(Utils.toString(product.ProductID))) {
//						continue;
//					}

				}

				Date maxExpiredate = Utils.AddDay(Utils.GetCurrentDate(), -1);
				var lstPromo = Arrays.asList(allExpirePromotion);
				var distinctPromotion = lstPromo.stream().distinct().toArray(PromotionBHXBO[]::new);
				for (var item : distinctPromotion) {
					// tam thoi chi cho KM tang qua va KM giam gia duoc phep hien thi web
					if (item.PromotionGiftType != 4 && item.PromotionGiftType != 3)
						continue;
					
					if (!item.ApplyProductID.equals(null) && !item.ApplyProductID.trim().equals(productCode)
							|| !item.ApplyProductIDRef.equals(Utils.toString(productId))) {
						continue;
					}
					PromotionBHX promo = new PromotionBHX();
					if (item.FromDate != null) {
						Date dateFr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.FromDate);
						promo.setBeginDate(dateFr);
					}
					if (item.ToDate != null) {
						Date dateTo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.ToDate);
						promo.setEndDate(dateTo);
					}
					promo.BHXPromotionType = BHXPromotionType.EXPDATE;
					promo.StoreIds = Utils.toString(item.STOREID);
					promo.ProvinceId = bhxStoreHelper.getProvinceByStore(item.STOREID);
					promo.Description = item.Description;
					promo.Id = item.PromotionID * -1;
					promo.MinValue = item.CorrespondingMonney;
					promo.MaxValue = item.CorrespondingMonney;
					promo.Limit = item.LimitQuantity;
					promo.IsOnlineOnly = !dicAllStore.contains(item.PromotionID);
					promo.MaxQuantityOnBill = (int) item.MAXQUANTITY;
					// khuyen mai tu BHX thi them promotionlistgroupid = 0
					promo.PromotionListGroupId = "0";
					if (Utils.StringIsEmpty(item.getProductID()) && item.DiscountValue > 0) {
						// Khuyen mai giam tien
						promo.Type = PromotionTypes.Discount;
						promo.IsPercent = item.DiscountType == 1;
						promo.Value = item.DiscountValue;
						promo.QuantityCondition = item.RequestQuantity; // So san pham phai mua de duoc giam gia

						expiredPromotions.add(promo);
					} else {
						// Khuyen mai tang qua
						promo.Type = PromotionTypes.Gift;

						// "donatetype" IS ' 1: chi tang 1, 2: Tang theo tong so luong; 3: Tang tat ca';
						promo.GiftType = item.DonateType == 3;

						promo.QuantityCondition = 9999;
						List<PromotionBHXBO> gifts = Arrays.stream(promotions)
								.filter(p -> p.PromotionID == item.PromotionID).collect(Collectors.toList());// .collect(Collectors.groupingBy(PromotionBHXBO::getProductId));

						if (gifts.size() > 0) {
							var tmp = new GiftBHX[gifts.size()];
							int i = 0;
							for (var g : gifts) {
								if (g.DefineQuantity < 1 || g.RequestQuantity < 1 || Utils.toInt(g.ProductIDRef) < 1) {
									continue;
								}
								var gTmp = new GiftBHX();
								gTmp.AppliedQuantity = g.DefineQuantity;
								gTmp.setQuantityCondition(g.RequestQuantity);
								gTmp.ProductID = Utils.toInt(g.ProductIDRef);
								gTmp.ProductCode = g.getProductID();
								gTmp.Name = g.ProductName;
								// Tạm thời giữ lại để tránh lỗi, sẽ bỏ đi sau khi cập nhật xong
								var productInfo = new ProductBO();
								productInfo.ProductID = Utils.toInt(g.ProductIDRef);
								productInfo.ProductCode = g.getProductID();
								productInfo.ProductName = g.ProductName;
								gTmp.ProductInfo = productInfo;
								tmp[i] = gTmp;
								i++;
							}
							tmp = Arrays.stream(tmp).filter(p -> p != null).toArray(GiftBHX[]::new);
							promo.Gifts = mapper.writeValueAsString(tmp);

							if (promo.Gifts.length() > 0) {
								// Cap nhat lai thong tin qua tang de toi uu xu ly tren WEB
								var giftObj = mapper.readValue(promo.Gifts, GiftBHX[].class);
								var giftPrdId = new int[giftObj.length];
								if (giftObj != null && giftObj.length > 0) {
									for (var obj = 0; obj < giftObj.length; obj++) {
										giftPrdId[obj] = giftObj[obj].ProductID;
									}
								}
								var provineid = bhxStoreHelper.getProvinceByStore(item.STOREID);
								var giftsBO = productHelper.GetProductBOByListID_PriceStrings(giftPrdId, 11, provineid, message.Lang); 
										//.GetProductBOByListID(giftPrdId, message.SiteID, message.ProvinceID, message.Lang);
								if (giftsBO != null) {
									for (var g : giftsBO) {
										if (g == null)
											continue;
										var gift = Arrays.stream(giftObj)
												.filter(f -> f.ProductInfo.ProductID == g.ProductID).findFirst().get();
										if (gift == null)
											continue;

										//lấy price của store hiện tại để lấy unit, quantity
										if(g.ProductErpPriceBOList == null || g.ProductErpPriceBOList.length == 0)
										{
											continue;
										}
										var price = Arrays.stream(g.ProductErpPriceBOList).filter(x->x.StoreID == item.STOREID).findFirst().get();
										if (price == null)
										{
											continue;
										}
										if(!Utils.StringIsEmpty(price.QuantityUnit)) {
											gift.Unit = price.QuantityUnit.toLowerCase().replace("hủ", "hũ");
										}
										gift.setStockQuantity(price.Quantity);

										gift.Name = g.ProductName;
										// Cap nhat lai hinh dai dien
										if (g.ProductLanguageBO != null && g.ProductLanguageBO.bimage != null && g.ProductLanguageBO.bimage.contains("300x300")) {
											gift.Avatar = GenImgUrl(g.CategoryID, g.ProductID, g.ProductLanguageBO.bimage.replace("-300x300", ""));
										} else if (g.ProductLanguageBO != null && g.ProductLanguageBO.mimage != null) {
											gift.Avatar = GenImgUrl(g.CategoryID, g.ProductID,
													g.ProductLanguageBO.mimage.replace("_resize", "_300x300"));
										}

										// Url qua tang
										gift.Url = URLInfo.GenUrl(g);
									}
								}

								// So luong san pham chinh phai mua
								Comparator<GiftBHX> comparator = Comparator.comparing(GiftBHX::getQuantityCondition);
								promo.QuantityCondition = Arrays.stream(giftObj).min(comparator).get()
										.getQuantityCondition();

								if (message.ID != -999) {
									// TODO check san pham qua tang neu chua duoc khai bao gia thi tao object gia
									// sau do push notification qua ton de cho san pham KM duoc len
									for (var g : giftObj) {
										if (g == null)
											continue;
									}

									// Chi luon lay toi da 10 qua tang co nhieu ton nhat
									// Tranh object khuyen mai qua lon
									var giftTmp = Arrays.stream(giftObj)
											.sorted(Comparator.comparingInt(GiftBHX::getStockQuantity)).limit(10)
											.toArray(GiftBHX[]::new);
									promo.Gifts = mapper.writeValueAsString(giftTmp);
									promo.GiftBHX = giftTmp;
								}
							}
						}

						if (promo.Gifts != null && promo.Gifts.length() > 0)
							expiredPromotions.add(promo);
					}

					if (promo.getEndDate() != null) {
						if (promo.getEndDate().compareTo(maxExpiredate) > 0) {
							maxExpiredate = promo.getEndDate();
						}
					}
				}

				// Cap nhat gia tri KM thuc cho danh sach KM moi
				for (var item : expiredPromotions) {
					var tmp = new ArrayList<PromotionBHX>();
					tmp.add(item);
					// TODO: sua ham nay
					var provineId = bhxStoreHelper.getProvinceByStore(Utils.toInt(item.StoreIds));
					var pricePO = priceHelper.getDefaultPriceStrings((int) productId, message.SiteID, provineId, 644,
							message.Lang);
					double price = 0;
					if (pricePO != null)
						price = pricePO.Price;
					var realDiscountValue = PromotionBHXHelper.CalculateRealDiscountValue(tmp, productId, productCode,
							message.SiteID, provineId, price, Utils.toInt(item.StoreIds), priceHelper);
					item.PromotionRealDiscountValue = realDiscountValue;
				}

			}
		}

		// TODO: Cap nhat Elasticsearch
		var rs = AddUpdatePromotion(productId, productCode, lstNewPromos, comboPromotions, flashSalePromotions,
				expiredPromotions, message);
		if (!rs) {
			throw new Throwable("Index ES fail");
		}

		var finalpromo = new ArrayList<PromotionBHX>();
		finalpromo.addAll(lstNewPromos);
		finalpromo.addAll(expiredPromotions);
		finalpromo.addAll(comboPromotions);

		return finalpromo.toArray(PromotionBHX[]::new);
	}
	
	public PromotionBHX[] processNewPromotionBHX(MessageQueue message, String productCode) throws Throwable {

		var lstNormalStore = bhxStoreHelper.getListNormalStore(); //kho thường
		var lstOutStore = bhxStoreHelper.getListOutStockStore(); //kho cận date
		
		var lstpromotions = new ArrayList<PromotionBHXBO>();
		var dicAllStore = new ArrayList<Integer>();
		var clearStockPromotions = new ArrayList<PromotionBHXBO>();
		
		var productId = productHelper.GetProductIDByProductCodeFromCache(productCode);
		var product = productHelper.getProductBOByProductID_PriceStrings((int) productId, message.SiteID, 3,
				message.Lang, 644);
		
		for (var storeid : lstNormalStore) {
			if(storeid < 1)
				continue;
			var promotionsOfProduct = bhxServiceHelper.GetPromotionBHXByPrdID(productCode, 1000, storeid, true, 60);
			if (promotionsOfProduct != null && promotionsOfProduct.length > 0) {
				for (var prom : promotionsOfProduct) {
					//storeid = 0 áp dụng cho tất cả các store (có KM offline)
					if (prom.STOREID == 0) {
						dicAllStore.add(prom.PromotionID);
					}
					// loai tru khuyen mai khai truong
					if(prom.PromoStoreType == 1) continue;
					prom.STOREID = storeid;
					prom.ApplyProductIDRef = Utils.toString(product.ProductID);
					if (Utils.StringIsEmpty(prom.ApplyProductID)) {
						prom.ApplyProductID = productCode;
						prom.ApplyProductIDRef = Utils.toString(product.ProductID);
					}

					lstpromotions.add(prom);
				}
			}
			
			// Lấy KM clear tồn 
			var promotionsOfClearStock = bhxServiceHelper.GetPromotionByProductClearStock(productCode, storeid, "1000", 0,
					Utils.FormatDateForGraph(Utils.GetCurrentDate()));
			if(promotionsOfClearStock == null) continue;
			List<PromotionBHXBO> temp = Arrays.stream(promotionsOfClearStock)
				.filter(p -> p.PromoStoreType != 1)
				.map(p ->{
					p.STOREID = storeid;
					p.ApplyProductIDRef = Utils.toString(product.ProductID);
					if (Utils.StringIsEmpty(p.ApplyProductID)) {
						p.ApplyProductID = productCode;
						p.ApplyProductIDRef = Utils.toString(product.ProductID);
					}
					return p;
				})
				.collect(Collectors.toList());
			clearStockPromotions.addAll(temp);
		}
		
		if (clearStockPromotions != null && clearStockPromotions.size() > 0) {
			lstpromotions.addAll(clearStockPromotions);
		}

		var distinctPromotion = lstpromotions.stream()
				.filter(distinctByKeys(PromotionBHXBO::getPromotionID, PromotionBHXBO::getSTOREID))
				.toArray(PromotionBHXBO[]::new);

		var lstNewPromos = new ArrayList<PromotionBHX>();
		var comboPromotions = new ArrayList<PromotionBHX>();
		var flashSalePromotions = new ArrayList<PromotionBHX>();
		
		// KM hang can date
		var expiredPromotions = new ArrayList<PromotionBHX>();

		if (distinctPromotion != null) {

			Date maxExpiredate = Utils.AddDay(Utils.GetCurrentDate(), -1);
//			var lstPromo = Arrays.asList(promotions);
			// bỏ những khuyến mãi áp dụng cho 1 kho trùng nhau

			for (var item : distinctPromotion) {
				System.out.println(" Store: " + item.STOREID);
				// tam thoi chi cho KM tang qua va KM giam gia duoc phep hien thi web
				if (item.PromotionGiftType != 4 && item.PromotionGiftType != 3)
					continue;

				if (!item.ApplyProductID.equals(null) && !item.ApplyProductID.trim().equals(productCode)
						|| !item.ApplyProductIDRef.equals(Utils.toString(product.ProductID))) {
					continue;
				}

				// if (item.PromotionGiftType == 3 && item.RequestQuantity > 1)
				// {
				// continue;
				// }

				PromotionBHX promo = new PromotionBHX();
				if (item.FromDate != null) {
					Date dateFr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.FromDate);
					promo.setBeginDate(dateFr);
				}
				if (item.ToDate != null) {
					Date dateTo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.ToDate);
					promo.setEndDate(dateTo);
				}
				promo.BHXPromotionType = BHXPromotionType.NORMAL;
				promo.StoreIds = Utils.toString(item.STOREID);
				promo.ProvinceId = bhxStoreHelper.getProvinceByStore(item.STOREID);
				promo.Description = item.Description;
				promo.Id = item.PromotionID * -1;
				promo.MinValue = item.CorrespondingMonney;
				promo.MaxValue = item.CorrespondingMonney;
				promo.Limit = item.LimitQuantity;
				promo.IsOnlineOnly = !dicAllStore.contains(item.PromotionID);
				promo.MaxQuantityOnBill = (int) item.MAXQUANTITY;
				// khuyen mai tu BHX thi them promotionlistgroupid = 0
				promo.PromotionListGroupId = "0";
				if (Utils.StringIsEmpty(item.getProductID()) && item.DiscountValue > 0) {
					// Khuyen mai giam tien
					promo.Type = PromotionTypes.Discount;
					promo.IsPercent = item.DiscountType == 1;
					promo.Value = item.DiscountValue;
					promo.QuantityCondition = item.RequestQuantity; // So san pham phai mua de duoc giam gia

					lstNewPromos.add(promo);
				} else {
					// Khuyen mai tang qua
					promo.Type = PromotionTypes.Gift;

					// "donatetype" IS ' 1: chi tang 1, 2: Tang theo tong so luong; 3: Tang tat ca';
					promo.GiftType = item.DonateType == 3;

					promo.QuantityCondition = 9999;
					List<PromotionBHXBO> gifts = Arrays.stream(distinctPromotion)
							.filter(p -> p.PromotionID == item.PromotionID)
							.filter(distinctByKeys(PromotionBHXBO::getProductID)).collect(Collectors.toList());// .collect(Collectors.groupingBy(PromotionBHXBO::getProductId));
					// List<PromotionBHXBO> gifts = promotions.Where(p -> p.PromotionID ==
					// item.PromotionID).GroupBy(g -> g.ProductID).Select(p -> p.First()).ToList();

					if (gifts.size() > 0) {
						var tmp = new GiftBHX[gifts.size()];
						int i = 0;
						for (var g : gifts) {
							if (g.DefineQuantity < 1 || g.RequestQuantity < 1 || Utils.toInt(g.ProductIDRef) < 1) {
								continue;
							}
							var gTmp = new GiftBHX();
							gTmp.AppliedQuantity = g.DefineQuantity;
							gTmp.setQuantityCondition(g.RequestQuantity);
							gTmp.ProductID = Utils.toInt(g.ProductIDRef);
							gTmp.ProductCode = g.getProductID();
							// Tạm thời giữ lại để tránh lỗi, sẽ bỏ đi sau khi cập nhật xong
							var productInfo = new ProductBO();
							productInfo.ProductID = Utils.toInt(g.ProductIDRef);
							productInfo.ProductCode = g.getProductID();
							productInfo.ProductName = g.ProductName;
							gTmp.ProductInfo = productInfo;
							tmp[i] = gTmp;
							i++;
						}
						tmp = Arrays.stream(tmp).filter(p -> p != null).toArray(GiftBHX[]::new);
						promo.Gifts = mapper.writeValueAsString(tmp);
						promo.GiftBHX = tmp;				

						if (promo.Gifts.length() > 0) {
							// Cap nhat lai thong tin qua tang de toi uu xu ly tren WEB
							var giftObj = mapper.readValue(promo.Gifts, GiftBHX[].class);
							var giftPrdId = new int[giftObj.length];
							if (giftObj != null && giftObj.length > 0) {
								for (var obj = 0; obj < giftObj.length; obj++) {
									giftPrdId[obj] = giftObj[obj].ProductID;
								}
							}
							var provineid = bhxStoreHelper.getProvinceByStore(item.STOREID);
							var giftsBO = productHelper.GetProductBOByListID_PriceStrings(giftPrdId, 11, provineid, message.Lang);
							if (giftsBO != null) {
								for (var g : giftsBO) {
									if (g == null)
										continue;
									var gift = Arrays.stream(giftObj)
											.filter(f -> f.ProductInfo.ProductID == g.ProductID).findFirst().get();
									if (gift == null)
										continue;									
									
									//lấy price của store hiện tại để lấy unit, quantity
									if(g.ProductErpPriceBOList == null || g.ProductErpPriceBOList.length == 0)
									{
										continue;
									}
									var price = Arrays.stream(g.ProductErpPriceBOList).filter(x->x.StoreID == item.STOREID).findFirst().get();
									if (price == null)
									{
										continue;
									}
									gift.Unit = price.QuantityUnit.toLowerCase().replace("hủ", "hũ"); 
									//if(gift.Unit.equals("hủ"))
									//	gift.Unit = "hũ";
									gift.setStockQuantity(price.Quantity);

									gift.Name = g.ProductName;
									// Cap nhat lai hinh dai dien
									if (g.ProductLanguageBO != null && g.ProductLanguageBO.bimage != null && g.ProductLanguageBO.bimage.contains("300x300")) {
										gift.Avatar = GenImgUrl(g.CategoryID, g.ProductID, g.ProductLanguageBO.bimage.replace("-300x300", ""));
									} else if (g.ProductLanguageBO != null && g.ProductLanguageBO.mimage != null) {
										gift.Avatar = GenImgUrl(g.CategoryID, g.ProductID,
												g.ProductLanguageBO.mimage.replace("_resize", "_300x300"));
									}

									// Url qua tang
									gift.Url = URLInfo.GenUrl(g);
								}
							}

							if (giftObj.length > 0) {
								Comparator<GiftBHX> comparator = Comparator.comparing(GiftBHX::getQuantityCondition);
								promo.QuantityCondition = Arrays.stream(giftObj).min(comparator).get()
										.getQuantityCondition();
							}

							if (message.ID != -999) {
								// TODO check san pham qua tang neu chua duoc khai bao gia thi tao object gia
								// sau do push notification qua ton de cho san pham KM duoc len
								for (var g : giftObj) {
									if (g == null)
										continue;
									// ko tim thay san pham qua tang thi noti giá defaut de tao sp
								}

								// Chi luon lay toi da 10 qua tang co nhieu ton nhat
								// Tranh object khuyen mai qua lon
								if (giftObj.length > 0) {
									var giftTmp = Arrays.stream(giftObj)
											.sorted(Comparator.comparingInt(GiftBHX::getStockQuantity)).limit(10)
											.toArray(GiftBHX[]::new);
									promo.Gifts = mapper.writeValueAsString(giftTmp);
									promo.GiftBHX = giftTmp;
								}
							}
						}
					}

					if (promo.Gifts != null && promo.Gifts.length() > 0)
						lstNewPromos.add(promo);
				}

				if (promo.getEndDate() != null) {
					if (promo.getEndDate().compareTo(maxExpiredate) > 0) {
						maxExpiredate = promo.getEndDate();
					}
				}

				// cập nhật thông tin khuyến mãi cho sp liên quan
				if (promo.getBeginDate().compareTo(Utils.GetCurrentDate()) <= 0
						&& Utils.GetCurrentDate().compareTo(promo.getEndDate()) <= 0) {
					UpdateProductRelevant(productId, promo);
					UpdateComboRelevant(productId);
				}
			}

			// Cap nhat gia tri KM thuc cho danh sach KM moi
			for (var item : lstNewPromos) {
				var tmp = new ArrayList<PromotionBHX>();
				tmp.add(item);
				// TODO: sua ham nay
				var provineId = bhxStoreHelper.getProvinceByStore(Utils.toInt(item.StoreIds));
				var pricePO = priceHelper.getDefaultPriceStrings((int) productId, message.SiteID, provineId, 644,
						message.Lang);
				double price = 0;
				if (pricePO != null)
					price = pricePO.Price;
				var realDiscountValue = PromotionBHXHelper.CalculateRealDiscountValue(tmp, productId, productCode,
						message.SiteID, provineId, price, Utils.toInt(item.StoreIds), priceHelper);
				item.PromotionRealDiscountValue = realDiscountValue;
			}

			// Lay ra khuyen mai hien tai trong REDIS da cap nhat lai bo dem
//			if (curPromotions != null) {
//				for (var curPromotion : curPromotions) {
//					var promo = lstNewPromos.stream().filter(p -> p.Id == curPromotion.Id).collect(Collectors.toList());
//
//					if (promo != null) {
//						for (var cpro : promo) {
//							cpro.LimitedCount = curPromotion.LimitedCount;
//						}
//
//					}
//				}
//			}
//			if (curComboPromotions != null) {
//				for (var curPromotion : curComboPromotions) {
//					var promo = lstNewPromos.stream().filter(p -> p.Id == curPromotion.Id).limit(1)
//							.collect(Collectors.toList());
//					if (promo != null) {
//						promo.get(0).LimitedCount = curPromotion.LimitedCount;
//					}
//				}
//			}

			// Trich ra danh sach KM giam tien nhung dieu kien ap dung lon hon 1
			// Luu tru vao mot key REDIS khac
			comboPromotions = (ArrayList<PromotionBHX>) lstNewPromos.stream()
					.filter(p -> p.Type == PromotionTypes.Discount && p.QuantityCondition > 1)
					.collect(Collectors.toList());

			if (comboPromotions != null && !comboPromotions.isEmpty()) {
				for (PromotionBHX promotionBHX : comboPromotions) {
					promotionBHX.BHXPromotionType = BHXPromotionType.COMBO;
				}
			}

			// Looi bo khoi danh sach ap dung tuc thi
			lstNewPromos.removeIf(p -> p.Type == PromotionTypes.Discount && p.QuantityCondition > 1);

		}
		
		//-------------------lấy promotions theo kho can date
		
		var lstOutStorePromo = new ArrayList<PromotionBHXBO>();
		for (var store : lstOutStore) {
			if(store < 1)
				continue;
			
			var tmp = bhxServiceHelper.GetPromotionBHXByPrdID(productCode, 1000, store, true, 60);
			if (tmp != null && tmp.length > 0) {
				for (var prom : tmp) {
					// ap dung cho all kho (co km offline)
					if (prom.STOREID > 0 && prom.PromoStoreType != 1) {
						prom.STOREID = store;
						lstOutStorePromo.add(prom);
					}
				}
			}
		}
		var exdPromotions = lstOutStorePromo.toArray(PromotionBHXBO[]::new);
		var mapPromotions = lstOutStorePromo.stream()
				.map(p-> {
					p.ApplyProductIDRef = Utils.toString(product.ProductID);
					if (Utils.StringIsEmpty(p.ApplyProductID) 
							//&& row.APPLYSUBGROUPID == product.IsPartnerProduct
							) {
						p.ApplyProductID = productCode;
						p.ApplyProductIDRef = Utils.toString(product.ProductID);
					}		
					return p;
				})
				.collect(Collectors.groupingBy(p -> p.STOREID, Collectors.mapping(p->p, Collectors.toList())));
		
		// xử lý KM cận date
		for (var _store : lstOutStore) {
			if(_store < 1)
				continue;
			System.out.println(" ex Store: " + _store);
			// var province = StoreProvinceConfig.GetProvinceByStore(_store);
			if (exdPromotions != null && exdPromotions.length > 0) {
				// chỉ lấy những KM cho kho 4472 và ko phải KM khai trương (PromoStoreType = 1)
				// lên
//				var allExpirePromotion = Arrays.stream(exdPromotions)
//						.filter(p -> p.STOREID > 0 && p.STOREID == _store )
//						.toArray(PromotionBHXBO[]::new);
//				var allExpirePromotion = mapPromotions.get(_store);
				

				Date maxExpiredate = Utils.AddDay(Utils.GetCurrentDate(), -1);
				var lstPromo = mapPromotions.get(_store);
				var Promotions = lstPromo.stream().distinct().toArray(PromotionBHXBO[]::new);
				for (var item : Promotions) {
					// tam thoi chi cho KM tang qua va KM giam gia duoc phep hien thi web
					if (item.PromotionGiftType != 4 && item.PromotionGiftType != 3)
						continue;
					
					if (!item.ApplyProductID.equals(null) && !item.ApplyProductID.trim().equals(productCode)
							|| !item.ApplyProductIDRef.equals(Utils.toString(product.ProductID))) {
						continue;
					}
					PromotionBHX promo = new PromotionBHX();
					if (item.FromDate != null) {
						Date dateFr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.FromDate);
						promo.setBeginDate(dateFr);
					}
					if (item.ToDate != null) {
						Date dateTo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.ToDate);
						promo.setEndDate(dateTo);
					}
					promo.BHXPromotionType = BHXPromotionType.EXPDATE;
					promo.StoreIds = Utils.toString(item.STOREID);
					promo.ProvinceId = bhxStoreHelper.getProvinceByStore(item.STOREID);
					promo.Description = item.Description;
					promo.Id = item.PromotionID * -1;
					promo.MinValue = item.CorrespondingMonney;
					promo.MaxValue = item.CorrespondingMonney;
					promo.Limit = item.LimitQuantity;
					promo.IsOnlineOnly = !dicAllStore.contains(item.PromotionID);
					promo.MaxQuantityOnBill = (int) item.MAXQUANTITY;
					// khuyen mai tu BHX thi them promotionlistgroupid = 0
					promo.PromotionListGroupId = "0";
					if (Utils.StringIsEmpty(item.getProductID()) && item.DiscountValue > 0) {
						// Khuyen mai giam tien
						promo.Type = PromotionTypes.Discount;
						promo.IsPercent = item.DiscountType == 1;
						promo.Value = item.DiscountValue;
						promo.QuantityCondition = item.RequestQuantity; // So san pham phai mua de duoc giam gia

						expiredPromotions.add(promo);
					} else {
						// Khuyen mai tang qua
						promo.Type = PromotionTypes.Gift;

						// "donatetype" IS ' 1: chi tang 1, 2: Tang theo tong so luong; 3: Tang tat ca';
						promo.GiftType = item.DonateType == 3;

						promo.QuantityCondition = 9999;
						List<PromotionBHXBO> gifts = Arrays.stream(distinctPromotion)
								.filter(p -> p.PromotionID == item.PromotionID).collect(Collectors.toList());// .collect(Collectors.groupingBy(PromotionBHXBO::getProductId));

						if (gifts.size() > 0) {
							var tmp = new GiftBHX[gifts.size()];
							int i = 0;
							for (var g : gifts) {
								if (g.DefineQuantity < 1 || g.RequestQuantity < 1 || Utils.toInt(g.ProductIDRef) < 1) {
									continue;
								}
								var gTmp = new GiftBHX();
								gTmp.AppliedQuantity = g.DefineQuantity;
								gTmp.setQuantityCondition(g.RequestQuantity);
								gTmp.ProductID = Utils.toInt(g.ProductIDRef);
								gTmp.ProductCode = g.getProductID();
								// Tạm thời giữ lại để tránh lỗi, sẽ bỏ đi sau khi cập nhật xong
								var productInfo = new ProductBO();
								productInfo.ProductID = Utils.toInt(g.ProductIDRef);
								productInfo.ProductCode = g.getProductID();
								productInfo.ProductName = g.ProductName;
								gTmp.ProductInfo = productInfo;
								tmp[i] = gTmp;
								i++;
							}
							tmp = Arrays.stream(tmp).filter(p -> p != null).toArray(GiftBHX[]::new);
							promo.Gifts = mapper.writeValueAsString(tmp);

							if (promo.Gifts.length() > 0) {
								// Cap nhat lai thong tin qua tang de toi uu xu ly tren WEB
								var giftObj = mapper.readValue(promo.Gifts, GiftBHX[].class);
								var giftPrdId = new int[giftObj.length];
								if (giftObj != null && giftObj.length > 0) {
									for (var obj = 0; obj < giftObj.length; obj++) {
										giftPrdId[obj] = giftObj[obj].ProductID;
									}
								}
								var provineid = bhxStoreHelper.getProvinceByStore(item.STOREID);
								var giftsBO = productHelper.GetProductBOByListID_PriceStrings(giftPrdId, 11, provineid, message.Lang); 
										//.GetProductBOByListID(giftPrdId, message.SiteID, message.ProvinceID, message.Lang);
								if (giftsBO != null) {
									for (var g : giftsBO) {
										if (g == null)
											continue;
										var gift = Arrays.stream(giftObj)
												.filter(f -> f.ProductInfo.ProductID == g.ProductID).findFirst().get();
										if (gift == null)
											continue;

										//lấy price của store hiện tại để lấy unit, quantity
										if(g.ProductErpPriceBOList == null || g.ProductErpPriceBOList.length == 0)
										{
											continue;
										}
										var price = Arrays.stream(g.ProductErpPriceBOList).filter(x->x.StoreID == item.STOREID).findFirst().get();
										if (price == null)
										{
											continue;
										}
										gift.Unit = price.QuantityUnit.toLowerCase().replace("hủ", "hũ");									
										gift.setStockQuantity(price.Quantity);

										gift.Name = g.ProductName;
										// Cap nhat lai hinh dai dien
										if (g.ProductLanguageBO != null && g.ProductLanguageBO.bimage != null && g.ProductLanguageBO.bimage.contains("300x300")) {
											gift.Avatar = GenImgUrl(g.CategoryID, g.ProductID, g.ProductLanguageBO.bimage.replace("-300x300", ""));
										} else if (g.ProductLanguageBO != null && g.ProductLanguageBO.mimage != null) {
											gift.Avatar = GenImgUrl(g.CategoryID, g.ProductID,
													g.ProductLanguageBO.mimage.replace("_resize", "_300x300"));
										}

										// Url qua tang
										gift.Url = URLInfo.GenUrl(g);
									}
								}

								// So luong san pham chinh phai mua
								Comparator<GiftBHX> comparator = Comparator.comparing(GiftBHX::getQuantityCondition);
								promo.QuantityCondition = Arrays.stream(giftObj).min(comparator).get()
										.getQuantityCondition();

								if (message.ID != -999) {
									// TODO check san pham qua tang neu chua duoc khai bao gia thi tao object gia
									// sau do push notification qua ton de cho san pham KM duoc len
									for (var g : giftObj) {
										if (g == null)
											continue;
									}

									// Chi luon lay toi da 10 qua tang co nhieu ton nhat
									// Tranh object khuyen mai qua lon
									var giftTmp = Arrays.stream(giftObj)
											.sorted(Comparator.comparingInt(GiftBHX::getStockQuantity)).limit(10)
											.toArray(GiftBHX[]::new);
									promo.Gifts = mapper.writeValueAsString(giftTmp);
									promo.GiftBHX = giftTmp;
								}
							}
						}

						if (promo.Gifts != null && promo.Gifts.length() > 0)
							expiredPromotions.add(promo);
					}

					if (promo.getEndDate() != null) {
						if (promo.getEndDate().compareTo(maxExpiredate) > 0) {
							maxExpiredate = promo.getEndDate();
						}
					}
				}

				// Cap nhat gia tri KM thuc cho danh sach KM moi
				for (var item : expiredPromotions) {
					var tmp = new ArrayList<PromotionBHX>();
					tmp.add(item);
					// TODO: sua ham nay
					var provineId = bhxStoreHelper.getProvinceByStore(Utils.toInt(item.StoreIds));
					var pricePO = priceHelper.getDefaultPriceStrings((int) productId, message.SiteID, provineId, 644,
							message.Lang);
					double price = 0;
					if (pricePO != null)
						price = pricePO.Price;
					var realDiscountValue = PromotionBHXHelper.CalculateRealDiscountValue(tmp, productId, productCode,
							message.SiteID, provineId, price, Utils.toInt(item.StoreIds), priceHelper);
					item.PromotionRealDiscountValue = realDiscountValue;
				}

			}
		}

		// TODO: Cap nhat Elasticsearch
		var rs = AddUpdatePromotion(productId, productCode, lstNewPromos, comboPromotions, flashSalePromotions,
				expiredPromotions, message);
		if (!rs) {
			throw new Throwable("Index ES fail");
		}

		var finalpromo = new ArrayList<PromotionBHX>();
		finalpromo.addAll(lstNewPromos);
		finalpromo.addAll(expiredPromotions);
		finalpromo.addAll(comboPromotions);

		return finalpromo.toArray(PromotionBHX[]::new);
	}

	private static String GenImgUrl(double categoryId, int productId, String imageFileName) {
		if (Utils.StringIsEmpty(imageFileName))
			return "";
		if (imageFileName.startsWith("http://"))
			return imageFileName.replace("http://", "https://");
		return "https://cdn.tgdd.vn/Products/Images/" + Utils.toInt(categoryId) + "/" + productId + "/bhx/"
				+ imageFileName;
	}

	public Boolean AddUpdatePromotion(long productId, String productCode, ArrayList<PromotionBHX> lstPromotion,
			ArrayList<PromotionBHX> lstComboPromotions, ArrayList<PromotionBHX> lstFlashSalePromotions,
			ArrayList<PromotionBHX> lstExpiredPromotions, MessageQueue message)
			throws JsonParseException, JsonMappingException, IOException {

		int[] excludes = null;
		// TODO: chua xu ly
		var excludesData = "";// iCached.Get<string>("BHX_CONFIG_EXCLUDEPROMOID"); //chuyen sang lay stactic
		if (!Utils.StringIsEmpty(excludesData)) {
			var excludesTmp = excludesData.split(",");
			var tmp = Arrays.stream(excludesTmp).filter(x -> !x.equals("0"))
					.collect(Collectors.toCollection(ArrayList::new));
			excludes = new int[tmp.size()];
			for (var i = 0; i < tmp.size(); i++) {
				excludes[i] = Utils.toInt(tmp.get(i));
			}
		}

		var hiddenPromotions = new ArrayList<PromotionBHX>();

		// Sắp xếp lại theo hiệu lực và loại trừ KM ẩn trước khi cập nhật vào
		// Elasticsearch
		var promotions = new ArrayList<PromotionBHX>();
		if (lstPromotion != null) {
			for (var l : lstPromotion) {
				var px = l;
				if (px.Gifts != null && px.Gifts.length() > 0) {
					var pxTmp = mapper.readValue(px.Gifts, GiftBHX[].class);
					for (var g : pxTmp) {
						g.ProductInfo = null;
					}
					px.Gifts = mapper.writeValueAsString(pxTmp);
					px.GiftBHX = pxTmp;
				}

				if (excludes != null && Arrays.stream(excludes).anyMatch(p -> p == px.Id)) {
					hiddenPromotions.add(px);
				} else {
					promotions.add(px);
				}
				// promotions.add(px);
			}
		}

		// Tương tự cho KM combo
		var comboPromotions = new ArrayList<PromotionBHX>();
		if (lstComboPromotions != null) {
			for (var l : lstComboPromotions) {
				var px = l;
				if (px.Gifts != null && px.Gifts.length() > 0) {
					var pxTmp = mapper.readValue(px.Gifts, GiftBHX[].class);
					for (var g : pxTmp) {
						g.ProductInfo = null;
					}
					px.Gifts = mapper.writeValueAsString(pxTmp);
					px.GiftBHX = pxTmp;
				}

				if (excludes != null && Arrays.stream(excludes).anyMatch(p -> p == px.Id)) {
					hiddenPromotions.add(px);
				} else {
					comboPromotions.add(px);
				}
			}
		}

		var flashSalePromotions = new ArrayList<PromotionBHX>();
		if (lstFlashSalePromotions != null) {
			for (var l : lstFlashSalePromotions) {
				var px = l;
				if (px.Gifts != null && px.Gifts.length() > 0) {
					var pxTmp = mapper.readValue(px.Gifts, GiftBHX[].class);
					for (var g : pxTmp) {
						g.ProductInfo = null;
					}
					px.Gifts = mapper.writeValueAsString(pxTmp);
					px.GiftBHX = pxTmp;
				}
				flashSalePromotions.add(px);
			}
		}

		// KM hàng cận date
		var expiredPromotions = new ArrayList<PromotionBHX>();
		if (lstExpiredPromotions != null) {
			for (var l : lstExpiredPromotions) {
				var px = l;
				if (px.Gifts != null && px.Gifts.length() > 0) {
					var pxTmp = mapper.readValue(px.Gifts, GiftBHX[].class);
					for (var g : pxTmp) {
						g.ProductInfo = null;
					}
					px.Gifts = mapper.writeValueAsString(pxTmp);
					px.GiftBHX = pxTmp;
				}
				expiredPromotions.add(px);
			}
		}

		promotions = promotions.stream().sorted(Comparator.comparing(PromotionBHX::getBeginDate))
				.collect(Collectors.toCollection(ArrayList::new));

		comboPromotions = comboPromotions.stream().sorted(Comparator.comparing(PromotionBHX::getBeginDate))
				.collect(Collectors.toCollection(ArrayList::new));

		hiddenPromotions = hiddenPromotions.stream().sorted(Comparator.comparing(PromotionBHX::getBeginDate))
				.collect(Collectors.toCollection(ArrayList::new));

		flashSalePromotions = flashSalePromotions.stream().sorted(Comparator.comparing(PromotionBHX::getBeginDate))
				.collect(Collectors.toCollection(ArrayList::new));

		expiredPromotions = expiredPromotions.stream().sorted(Comparator.comparing(PromotionBHX::getBeginDate))
				.collect(Collectors.toCollection(ArrayList::new));

		// tạo ra 2 cụm km tren es: km thường mà km cận date
		var normalpromo = new ArrayList<PromotionBHX>();
		if (promotions != null && !promotions.isEmpty()) {
			normalpromo.addAll(promotions);
		}

		var result = true;
		//index km thường
		result = IndexDataES(normalpromo.toArray(PromotionBHX[]::new), productId, message.SiteID, message.Lang,
				message.DataCenter, "PromotionBHXList");
		
		//index km combo
		result = IndexDataES(comboPromotions.toArray(PromotionBHX[]::new), productId, message.SiteID,
				message.Lang, message.DataCenter, "PromotionBHXComboList");
		
		//index km cận date thường		
		result = IndexDataES(expiredPromotions.toArray(PromotionBHX[]::new), productId, message.SiteID,
				message.Lang, message.DataCenter, "PromotionBHXEpxList");
		

		return result;
	}

	public ProductRelevantSO GetProductRelevantSOFromES(String indexDB, int id) throws Throwable {
		 

			 
				return ElasticClient.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST).GetSingleObject(indexDB,
						Utils.toString(id), ProductRelevantSO.class);

			 
	}

	private <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
		final Map<List<?>, Boolean> seen = new ConcurrentHashMap<>();

		return t -> {
			final List<?> keys = Arrays.stream(keyExtractors).map(ke -> ke.apply(t)).collect(Collectors.toList());

			return seen.putIfAbsent(keys, Boolean.TRUE) == null;
		};
	}

	public boolean IndexProductRelevantSOFromES(String index, ProductRelevantSO product) {
		for (int i = 0; i < 1000000; i++) {

			try {

				return ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST).IndexObject(index,
						product, Utils.toString(product.GiftId));

			} catch (Exception e) {
				Logs.WriteLine("Exception IndexProductRelevantSOFromES ES FAILED: " + product.GiftId);
				Logs.WriteLine(e);
				Utils.Sleep(300);
			}
		}

		return false;
	}

	/// <summary>
	/// Cập nhật thông tin có liên quan giữa sp có KM và sp quà tặng
	/// </summary>
	/// <param name="productId">Id sản phẩm có KM</param>
	/// <param name="promo">Thông tin khuyến mãi có liên quan</param>
	/// <returns></returns>
	public boolean UpdateProductRelevant(long productId, PromotionBHX promo) throws Throwable {
		 
			if (promo == null || promo.Gifts == null || promo.Gifts.length() <= 0)
				return false;

			var index = "bhx_new_relevantproduct";
			var promoGifts = mapper.readValue(promo.Gifts, GiftBHX[].class);
			for (var item : promoGifts) {
				var tmp = GetProductRelevantSOFromES(index, item.ProductID);
				if (tmp == null) {
					// Xử lý tạo mới
					tmp = new ProductRelevantSO();
					tmp.GiftId = item.ProductID;
				}

				// Promotion
				if (tmp.PromotionInfos == null || tmp.PromotionInfos.length == 0) {
					var lstPromotionInfo = new ArrayList<RelevantInfo>();
					var relevantInfo = new RelevantInfo();
					relevantInfo.BeginDate = promo.getBeginDate();
					relevantInfo.EndDate = promo.getEndDate();
					relevantInfo.StoreIds = promo.StoreIds;
					relevantInfo.Id = promo.Id;
					relevantInfo.MainProductId = (int) productId;
					relevantInfo.MainProductCode = item.ProductCode;
					relevantInfo.Type = ProductRelevantType.Promotion.value();
					lstPromotionInfo.add(relevantInfo);
					tmp.PromotionInfos = lstPromotionInfo.stream().toArray(RelevantInfo[]::new);
				} else {					
					var lstPromo = Arrays.stream(tmp.PromotionInfos).collect(Collectors.toList());
					if (Arrays.stream(tmp.PromotionInfos).anyMatch(i -> i.Id == promo.Id && i.StoreIds == promo.StoreIds)) {
						lstPromo.removeIf(i -> i.Id == promo.Id && i.StoreIds == promo.StoreIds);
					}
					var relevantInfo = new RelevantInfo();
					relevantInfo.BeginDate = promo.getBeginDate();
					relevantInfo.EndDate = promo.getEndDate();
					relevantInfo.StoreIds = promo.StoreIds;
					relevantInfo.Id = promo.Id;
					relevantInfo.MainProductId = (int) productId;
					relevantInfo.MainProductCode = item.ProductCode;
					relevantInfo.Type = (int) ProductRelevantType.Promotion.value();
					lstPromo.add(relevantInfo);
					tmp.PromotionInfos = lstPromo.stream().toArray(RelevantInfo[]::new);
				}

				// chỉ lưu lại những KM đang có hiệu lực
				tmp.PromotionInfos = Arrays.stream(tmp.PromotionInfos)
						.filter(p -> p.BeginDate.compareTo(Utils.GetCurrentDate()) <= 0
								&& p.EndDate.compareTo(Utils.GetCurrentDate()) >= 0)
						.toArray(RelevantInfo[]::new);

				IndexProductRelevantSOFromES(index, tmp);
			}
		 
		return false;
	}

	/// <summary>
	/// Cập nhật thêm trạng thái cho sản phẩm combo
	/// </summary>
	/// <param name="productId"></param>
	/// <returns></returns>
	public boolean UpdateComboRelevant(long productId) throws Throwable {
		var index = "bhx_new_relevantproduct";

		var tmp = GetProductRelevantSOFromES(index, (int) productId);
		if (tmp != null && tmp.ComboInfos != null && tmp.ComboInfos.length > 0) {
			for (var combo : tmp.ComboInfos) {
				// TODO: chua xu ly
//                Queue.Current.Push("bhxnew.productse", new MessageQueue
//                {
//                    ID = -20191103L,
//                    Action = DataAction.Update,
//                    ClassName = AsmClass.Didx_Productse_ProductSE,
//                    Identify = combo.MainProductId,
//                    SiteID = 11,
//                    Lang = "vi-VN"
//                });
			}
		}

		return false;
	}
}
