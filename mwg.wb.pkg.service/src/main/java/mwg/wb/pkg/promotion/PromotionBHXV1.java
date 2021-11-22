package mwg.wb.pkg.promotion;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.ProductHelper;
import mwg.wb.business.helper.BHXStoreHelper;
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.BhxServiceHelper;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.RefSql;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.Utils;
import mwg.wb.common.bhx.StoreProvinceConfig;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.model.promotion.ComboBHX;
import mwg.wb.model.promotion.GiftBHX;
import mwg.wb.model.promotion.ProductRelevantSO;
import mwg.wb.model.promotion.ProductRelevantType;
import mwg.wb.model.promotion.Promotion;
import mwg.wb.model.promotion.PromotionBHX;
import mwg.wb.model.promotion.PromotionBHXBO;
import mwg.wb.model.promotion.PromotionTypes;
import mwg.wb.model.promotion.RelevantInfo;
import mwg.wb.model.promotion.URLInfo;
import mwg.wb.model.search.PromotionSO;

public class PromotionBHXV1 implements Ididx {
	// private OrientDBFactory factoryDB = null;
//	private PriceHelper priceHelper = null;
	private ProductHelper productHelper = null;
	private ObjectMapper mapper = null;
	private BhxServiceHelper bhxServiceHelper = null;
	private ErpHelper erpHelper = null;
//	private ORThreadLocal factoryWrite = null;
//	private ORThreadLocal factoryRead = null;
	private ClientConfig clientConfig = null;
	String indexDB = "";

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {

//		factoryWrite = (ORThreadLocal) objectTransfer.factoryWrite;
//		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
//		priceHelper = (PriceHelper) objectTransfer.priceHelper;
		productHelper = (ProductHelper) objectTransfer.productHelper;
		mapper = (ObjectMapper) objectTransfer.mapper;
		bhxServiceHelper = (BhxServiceHelper) objectTransfer.bhxServiceHelper;
		erpHelper = (ErpHelper) objectTransfer.erpHelper;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		indexDB = clientConfig.ELASTICSEARCH_PRODUCT_INDEX;
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

	public boolean IndexDataES(mwg.wb.model.promotion.Promotion[] lstPromotion, boolean isLog, String strNOTE,
			long ProductID, int SiteID, String LangID, int DataCenter) {
		String esKeyTerm = ProductID + "_" + SiteID + "_" + (LangID.toLowerCase().replaceAll("-", "_"));

		if (lstPromotion == null) {

			lstPromotion = new mwg.wb.model.promotion.Promotion[] {};
		}
		var solist = Stream.of(lstPromotion).map(x -> x.getSOObject()).toArray(PromotionSO[]::new);
		// if (lstPromotion != null && lstPromotion.length > 0) {
		for (int i = 0; i < 5000000; i++) {

			try {

				return ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
						.UpdateFieldOBject(indexDB, esKeyTerm, "PromotionSoList", solist);

			} catch (Exception e) {
				Logs.WriteLine("Exception Index promotion status to ES FAILED: " + ProductID);
				Logs.WriteLine(e);
				Utils.Sleep(100);
			}
		}

		// }else {}

		return false;
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage rsmsg = new ResultMessage();
		rsmsg.Code = ResultCode.Success;
		try {
			// productcode|
			String[] messIdentify = message.Identify.split("\\|");
			if (messIdentify.length < 2) {
				Logs.WriteLine(messIdentify + ":" + messIdentify.length);
				return rsmsg;
			}
			String strNOTE = message.Note + "";

			boolean isLog = false;
			if (strNOTE.contains("LOG")) {

				isLog = true;
			}

			String productCode = messIdentify[0].trim();
			int SalePrice = -1;
			int OutputTypeID = 0;
			int ProvinceID = -1;
			int InventoryStatusID = 1;
			int siteid = message.SiteID;
			String Lang = message.Lang;
			if (Utils.StringIsEmpty(Lang)) {
				Lang = "vi-VN";

			}
			// ko phải site bhx thì return
			if (siteid != 11) {
				return rsmsg;
			}
			MessageQueue messageRepush = new MessageQueue();
			messageRepush.SqlList = new ArrayList<SqlInfo>();
			messageRepush.SiteID = message.SiteID;

			mwg.wb.model.promotion.Promotion[] lstpromotionBHX = new mwg.wb.model.promotion.Promotion[] {};

			// xu ly khuyen mai BHX
			int BrandID = 3;
			String arecordid = siteid + "_" + BrandID + "_" + "_" + SalePrice + "_" + OutputTypeID + "_"
					+ InventoryStatusID;

			// arecordid = siteid + "_" + SalePrice + "_" + OutputTypeID + "_" +
			// InventoryStatusID;
			String recordidcommon = siteid + "_" + BrandID + "_" + SalePrice + "_" + OutputTypeID + "_"
					+ InventoryStatusID + "_" + productCode;
			SqlInfo sqlinfoDelBHX = new SqlInfo();
			sqlinfoDelBHX.Sql = "update product_promotion set isdeleted=1 where recordidcommon='" + recordidcommon
					+ "'";
			messageRepush.SqlList.add(sqlinfoDelBHX);

			Logs.Log(isLog, strNOTE, "GetPromotionBHXByProductID " + arecordid);

			var promoBHX = ProcessPromotionBHX(message, productCode, siteid);
			// xu ly them voi GraphDB
			if (promoBHX != null && promoBHX.length > 0) {

				for (mwg.wb.model.promotion.PromotionBHX promo : promoBHX) {
					if (promo != null) {
						var item = new mwg.wb.model.promotion.Promotion();
						String recordid = arecordid + "_" + promo.StoreIds + "_" + productCode + "_" + promo.Id;
						item.OutputTypeID = OutputTypeID;
						item.SalePrice = SalePrice;
						item.SiteID = siteid;
						item.ProductCode = productCode;
						item.recordid = recordid;
						item.BrandID = BrandID;
						item.didxupdateddate = Utils.GetCurrentDate();
						item.IsDeleted = 0;
						item.LangID = Lang;
						item.recordidcommon = recordidcommon;
						item.ProvinceId = promo.ProvinceId;
						// bhx
						item.BeginDate = promo.getBeginDate();
						// item.ComboCode = promo.ComboCode;
						item.Description = promo.Description;
						item.EndDate = promo.getEndDate();
						item.ExcludePromotion = StringUtils.join(promo.Excludes, ",");
						// item.FromTime = promo.FromTime;
						item.Gifts = promo.Gifts;
						item.GiftType = promo.GiftType;
						// item.Images = promo.Images;
						// promo.IsBetter = promo.IsBetter;
						// promo.IsNotApplyWithSpecialInstallment =
						// promo.IsNotApplyWithSpecialInstallment;
						item.IsOnline = promo.IsOnlineOnly;
						item.IsPercentDiscount = promo.IsPercent;
						item.Quantity = promo.Limit;
						item.LimitQuantity = promo.LimitedCount;
						item.FromValue = promo.MaxValue;
						item.ToValue = promo.MinValue;
						item.PromotionListGroupName = promo.PromotionListGroupId;
						item.SalePrice = (int) promo.PromotionRealDiscountValue;
						item.RequestQuantity = promo.QuantityCondition;
						item.StoreIds = promo.StoreIds;
						// item.STOREID = promo.StoreIds;
						// item.ToTime = promo.ToTime;
						item.PromotionType = promo.Type.value();
						item.DiscountValue = promo.Value;
						item.MaxQuantityOnBill = promo.MaxQuantityOnBill;
						item.PromotionID = promo.Id;

						Logs.Log(isLog, strNOTE, "UpsertODatabaseJson : " + item.recordid);

						SqlInfo sqlinfo = new SqlInfo();

						RefSql ref = new RefSql();
						Utils.BuildSql(isLog, strNOTE, "product_promotion", "recordid", item.recordid, item, ref);
						sqlinfo.Sql = ref.Sql;
						sqlinfo.Params = ref.params;
						messageRepush.SqlList.add(sqlinfo);

						var code = item.ProductCode.trim();
						String cmd2 = "create edge e_code_promotion  from (select from pm_product where productid='"
								+ code + "')   to(select from  product_promotion where recordid ='" + item.recordid
								+ "' and in('e_code_promotion')[productid='" + code + "'].size() = 0)";
						SqlInfo sqlinfoEdge = new SqlInfo();
						sqlinfoEdge.Sql = cmd2;
						messageRepush.SqlList.add(sqlinfoEdge);

						// add item to list promotion bhx
						lstpromotionBHX = (mwg.wb.model.promotion.Promotion[]) ArrayUtils.add(lstpromotionBHX, item);
					}

				}

			} else {
				Logs.WriteLine("\nKM BHX stpromotionbhx.length<=0");

			}

			long ProductID = productHelper.GetProductIDByProductCodeFromCache(productCode);
			// long ProductID = productHelper.GetProductIDByProductCode(productCode);
			if (ProductID > 0) {
				String dfcode = productHelper.getDefaultCodeFromCache(ProductID, message.SiteID,
						message.SiteID == 6 ? 163 : 3);
				if (dfcode != null && dfcode.equals(productCode) || message.SiteID == 11) {
					boolean rs = IndexDataES(lstpromotionBHX, isLog, strNOTE, ProductID, message.SiteID, message.Lang,
							message.DataCenter);

					if (!rs) {

						rsmsg.Code = ResultCode.Retry;
						Logs.WriteLine("IndexDataES false  ");
						Logs.Log(isLog, strNOTE, "IndexDataES false ");
						return rsmsg;

					}
				}
				// int ie = Utils.GetQueueNum(ProductID);
				String qu = "gr.dc4.sql.promotion";
				String qu2 = "gr.dc4.sql.promotion";
				String qubk = "gr.dc2.sql.promotion";
				String qudev = "gr.beta.sql.promotion";
				messageRepush.Source = "PROMOTION";
				messageRepush.Action = DataAction.Update;
				messageRepush.ClassName = "ms.upsert.Upsert";
				messageRepush.CreatedDate = Utils.GetCurrentDate();
				messageRepush.Lang = message.Lang;
				messageRepush.SiteID = message.SiteID;
				messageRepush.Identify = String.valueOf(ProductID);
				messageRepush.Note = strNOTE;
				messageRepush.DataCenter = message.DataCenter;
				try {
					QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush,
							isLog, strNOTE,message.DataCenter);
				} catch (Exception e) {

					Logs.LogException(e);
					rsmsg.Code = ResultCode.Retry;
					Logs.Log(isLog, strNOTE, "push promotion  " + e.getMessage());
					return rsmsg;
				}

			}
			rsmsg.Code = ResultCode.Success;
		} catch (Throwable e) {

			rsmsg.Code = ResultCode.Retry;
			rsmsg.StackTrace = Utils.stackTraceToString(e);
			Logs.LogException(e);
		}
		return rsmsg;

	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unlikely-arg-type")
	public PromotionBHX[] ProcessPromotionBHX(MessageQueue message, String productCode, int siteid) throws Throwable {
		BHXStoreHelper helper = new BHXStoreHelper("10.1.6.151");
		var lstNormalStore = helper.getListNormalStore();
		var lstOutStore = StoreProvinceConfig.GetListOutStockStore();
		var lstpromotions = new ArrayList<PromotionBHXBO>();
		var clearStockPromotions = new ArrayList<PromotionBHXBO>();
		ArrayList<Long> dicAllStore = new ArrayList<Long>();

		// kho binh thuong
		for (var store : lstNormalStore) {
			var tmp = bhxServiceHelper.GetPromotionBHXByPrdID(productCode, 1000, store, true, 60);
			if (tmp != null && tmp.length > 0) {
				for (var prom : tmp) {
					// ap dung cho all kho (co km offline)
					if (prom.STOREID != 0) {
						dicAllStore.add((long) prom.PromotionID);
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

		var lstOutStorePromo = new ArrayList<PromotionBHXBO>();
		for (var store : lstOutStore) {
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
		PromotionBHX[] curComboPromotions = null;

		if (message.Action == DataAction.Update) {
			var result = mwg.wb.business.APIOrientClient.GetOrientClient(clientConfig).QueryFunction(
					"product_GetPromoByProductID", Promotion[].class, false, productCode, siteid, message.Lang);
			if (result != null && result.length > 0) {
				curPromotions = new PromotionBHX[result.length];
				for (var i = 0; i < result.length; i++) {
					var promo = new PromotionBHX();
					promo.setBeginDate(result[i].BeginDate);
					// item.ComboCode = promo.ComboCode;
					promo.Description = result[i].Description;
					promo.setEndDate(result[i].EndDate);
					promo.Excludes = Arrays.asList(result[i].ExcludePromotion.split(",", -1)).stream().map(s -> Utils.toInt(s)).collect(Collectors.toList());
					// item.FromTime = promo.FromTime;
					promo.Gifts = result[i].Gifts;
					promo.GiftType = result[i].GiftType;
					promo.Id = result[i].PromotionID;
					// item.Images = promo.Images;
					// promo.IsBetter = promo.IsBetter;
					// promo.IsNotApplyWithSpecialInstallment =
					// promo.IsNotApplyWithSpecialInstallment;
					promo.IsOnlineOnly = result[i].IsOnline;
					promo.IsPercent = result[i].IsPercentDiscount;
					promo.Limit = result[i].Quantity;
					promo.LimitedCount = result[i].LimitQuantity;
					promo.MaxValue = result[i].FromValue;
					promo.MinValue = result[i].ToValue;
					promo.PromotionListGroupId = result[i].PromotionListGroupName;
					promo.PromotionRealDiscountValue = (int) result[i].SalePrice;
					promo.QuantityCondition = result[i].RequestQuantity;
					promo.StoreIds = promo.StoreIds;
					// item.STOREID = promo.StoreIds;
					// item.ToTime = promo.ToTime;
					switch (result[i].PromotionType) {
					case 0:
						promo.Type = PromotionTypes.Discount;
						break;
					case 1:
						promo.Type = PromotionTypes.Gift;
						break;
					case 2:
						promo.Type = PromotionTypes.Undefined;
						break;
					case 3:
						promo.Type = PromotionTypes.AndDiscountGift;
						break;
					case 4:
						promo.Type = PromotionTypes.OrDiscountGift;
						break;
					case 5:
						promo.Type = PromotionTypes.Combo;
						break;
					case 6:
						promo.Type = PromotionTypes.Text;
						break;
					}
					promo.Value = result[i].DiscountValue;
					promo.MaxQuantityOnBill = promo.MaxQuantityOnBill;

					// add vào curPromotions
					curPromotions[i] = promo;
				}
			}
		}

		// Xóa dữ liệu hiện tại
		// iCached.Remove(newPromotionKey);
		// iCached.Remove(flashsalePromotionKey);

		// TODO: Xoa khuyen mai combo
		// InitComboPromotion(iCached, productId, new List<Promotion>(),
		// message.SiteID);

		var lstNewPromos = new ArrayList<PromotionBHX>();
		var comboPromotions = new ArrayList<PromotionBHX>();
		var flashSalePromotions = new ArrayList<PromotionBHX>();
		var productId = productHelper.GetProductIDByProductCodeFromCache(productCode);
		var product = productHelper.GetProductBOByProductID((int) productId, siteid, 3, message.Lang, 0);
		// KM hang can date
		var expiredPromotions = new ArrayList<PromotionBHX>();

		if (promotions != null && promotions.length > 0) {
			// loai tru khuyen mai khai truong
			promotions = Arrays.stream(promotions).filter(p -> p.PromoStoreType != 1).toArray(PromotionBHXBO[]::new);
			for (var row : promotions) {
				// Khuyen mai dich danh nhung khai bao theo nhom
				// Cung nhom thi hien nhien ap dung cho san pham
				if (Utils.StringIsEmpty(row.ApplyProductID) && row.APPLYSUBGROUPID == product.IsPartnerProduct) {
					row.ApplyProductID = productCode;
					row.ApplyProductIDRef = Utils.toString(product.ProductID);
				}

				if (!row.ApplyProductID.equals(null) || Utils.toInt(row.ApplyProductIDRef) < 1
						|| !row.ApplyProductID.trim().equals(productCode)
						|| !row.ApplyProductIDRef.equals(Utils.toString(product.ProductID))) {
					continue;
				}
			}

			Date maxExpiredate = Utils.AddDay(Utils.GetCurrentDate(), -1);
			var lstPromo = Arrays.asList(promotions);
			var distinctPromotion = lstPromo.stream().distinct().toArray(PromotionBHXBO[]::new);
			for (var item : distinctPromotion) {
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
				promo.StoreIds = Utils.toString(item.STOREID);
				promo.ProvinceId = StoreProvinceConfig.GetProvinceByStore(item.STOREID);
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
							.filter(p -> p.PromotionID == item.PromotionID).collect(Collectors.toList());// .collect(Collectors.groupingBy(PromotionBHXBO::getProductId));
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

						if (promo.Gifts.length() > 0) {
							// Cap nhat lai thong tin qua tang de toi uu xu ly tren WEB
							var giftObj = mapper.readValue(promo.Gifts, GiftBHX[].class);
							var giftPrdId = new int[giftObj.length];
							if (giftObj != null && giftObj.length > 0) {
								for (var obj = 0; obj < giftObj.length; obj++) {
									giftPrdId[obj] = giftObj[obj].ProductID;
								}
							}
							var giftsBO = productHelper.GetProductBOByListID(giftPrdId, siteid, message.ProvinceID,
									message.Lang);
							if (giftsBO != null) {
								for (var g : giftsBO) {
									if (g == null)
										continue;
									var gift = Arrays.stream(giftObj)
											.filter(f -> f.ProductInfo.ProductID == g.ProductID).findFirst().get();
									if (gift == null)
										continue;

									// lay fullname cua san pham (18/12/2019)
									gift.Name = g.ProductName;

									// Cap nhat lai hinh dai dien
									if (g.Bimage != null && g.Bimage.contains("300x300")) {
										gift.Avatar = GenImgUrl(g.CategoryID, g.ProductID, g.Bimage);
									} else if (g.Mimage != null) {
										gift.Avatar = GenImgUrl(g.CategoryID, g.ProductID,
												g.Mimage.replace("_resize", "_300x300"));
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

									// kiem tra object gia
									var province = StoreProvinceConfig.GetProvinceByStore(item.STOREID);
//                                    string priceKey = string.Format(DataKey.ProductModule.PCI.PRICE_DETAIL_BYSITE,
//                                        ProvinceStoreConfig.GetProvinceByStore(item.STOREID.Value), g.ProductInfo.ProductID, g.ProductInfo.ProductCode, message.SiteID);

									// notify chay ton kho
//                                    Queue.Current.Push("bhxnew.currentinstockbhx", new MessageQueue
//                                    {
//                                        Action = DataAction.Update,
//                                        ClassName = AsmClass.Didx_CurrentInStockBHX_CurrentInStockBHX,
//                                        Identify = g.ProductInfo.ProductID,
//                                        SiteID = message.SiteID
//                                    });
								}

								// Chi luon lay toi da 10 qua tang co nhieu ton nhat
								// Tranh object khuyen mai qua lon
								var giftTmp = Arrays.stream(giftObj)
										.sorted(Comparator.comparingInt(GiftBHX::getStockQuantity)).limit(10)
										.toArray(GiftBHX[]::new);
								promo.Gifts = mapper.writeValueAsString(giftTmp);
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
				// var realDiscountValue = PromotionHelper.CalculateRealDiscountValue(tmp,
				// productId, productCode, message.SiteID, 3, iCached);
				// item.PromotionRealDiscountValue = realDiscountValue;
			}

			// Lay ra khuyen mai hien tai trong REDIS da cap nhat lai bo dem
			if (curPromotions != null) {
				for (var curPromotion : curPromotions) {
					var promo = lstNewPromos.stream().filter(p -> p.Id == curPromotion.Id).collect(Collectors.toList());

					if (promo != null) {
						for (var cpro : promo) {
							cpro.LimitedCount = curPromotion.LimitedCount;
						}

					}
				}
			}
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

			// TODO: init KM combo len elastic
			// InitComboPromotion(iCached, productId, comboPromotions, message.SiteID);

			// Looi bo khoi danh sach ap dung tuc thi
			lstNewPromos.removeIf(p -> p.Type == PromotionTypes.Discount && p.QuantityCondition > 1);

		}

		// xử lý KM cận date
		for (var _store : lstOutStore) {
			//var province = StoreProvinceConfig.GetProvinceByStore(_store);
			if (exdPromotions != null && exdPromotions.length > 0) {
				// chỉ lấy những KM cho kho 4472 và ko phải KM khai trương (PromoStoreType = 1)
				// lên
				var allExpirePromotion = Arrays.stream(exdPromotions)
						.filter(p -> p.STOREID > 0 && p.STOREID == _store && p.PromoStoreType != 1)
						.toArray(PromotionBHXBO[]::new);
				for (var row : allExpirePromotion) {
					// Khuyen mai dich danh nhung khai bao theo nhom
					// Cung nhom thi hien nhien ap dung cho san pham
					if (Utils.StringIsEmpty(row.ApplyProductID) && row.APPLYSUBGROUPID == product.IsPartnerProduct) {
						row.ApplyProductID = productCode;
						row.ApplyProductIDRef = Utils.toString(product.ProductID);
					}

					if (!row.ApplyProductID.equals(null) || Utils.toInt(row.ApplyProductIDRef) < 1
							|| !row.ApplyProductID.trim().equals(productCode)
							|| !row.ApplyProductIDRef.equals(Utils.toString(product.ProductID))) {
						continue;
					}

					if (row.STOREID <= 0)
						continue;
				}

				Date maxExpiredate = Utils.AddDay(Utils.GetCurrentDate(), -1);
				var lstPromo = Arrays.asList(exdPromotions);
				var distinctPromotion = lstPromo.stream().distinct().toArray(PromotionBHXBO[]::new);
				for (var item : distinctPromotion) {
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
					promo.StoreIds = Utils.toString(item.STOREID);
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
								.filter(p -> p.PromotionID == item.PromotionID).collect(Collectors.toList());// .collect(Collectors.groupingBy(PromotionBHXBO::getProductId));
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

							if (promo.Gifts.length() > 0) {
								// Cap nhat lai thong tin qua tang de toi uu xu ly tren WEB
								var giftObj = mapper.readValue(promo.Gifts, GiftBHX[].class);
								var giftPrdId = new int[giftObj.length];
								if (giftObj != null && giftObj.length > 0) {
									for (var obj = 0; obj < giftObj.length; obj++) {
										giftPrdId[obj] = giftObj[obj].ProductID;
									}
								}
								var giftsBO = productHelper.GetProductBOByListID(giftPrdId, siteid, message.ProvinceID,
										message.Lang);
								if (giftsBO != null) {
									for (var g : giftsBO) {
										if (g == null)
											continue;
										var gift = Arrays.stream(giftObj)
												.filter(f -> f.ProductInfo.ProductID == g.ProductID).findFirst().get();
										if (gift == null)
											continue;

										// lay fullname cua san pham (18/12/2019)
										gift.Name = g.ProductName;

										// Cap nhat lai hinh dai dien
										if (g.Bimage != null && g.Bimage.contains("300x300")) {
											gift.Avatar = GenImgUrl(g.CategoryID, g.ProductID, g.Bimage);
										} else if (g.Mimage != null) {
											gift.Avatar = GenImgUrl(g.CategoryID, g.ProductID,
													g.Mimage.replace("_resize", "_300x300"));
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

										// kiem tra object gia
//                                    string priceKey = string.Format(DataKey.ProductModule.PCI.PRICE_DETAIL_BYSITE,
//                                        ProvinceStoreConfig.GetProvinceByStore(item.STOREID.Value), g.ProductInfo.ProductID, g.ProductInfo.ProductCode, message.SiteID);

										// notify chay ton kho
//                                    Queue.Current.Push("bhxnew.currentinstockbhx", new MessageQueue
//                                    {
//                                        Action = DataAction.Update,
//                                        ClassName = AsmClass.Didx_CurrentInStockBHX_CurrentInStockBHX,
//                                        Identify = g.ProductInfo.ProductID,
//                                        SiteID = message.SiteID
//                                    });
									}

									// Chi luon lay toi da 10 qua tang co nhieu ton nhat
									// Tranh object khuyen mai qua lon
									var giftTmp = Arrays.stream(giftObj)
											.sorted(Comparator.comparingInt(GiftBHX::getStockQuantity)).limit(10)
											.toArray(GiftBHX[]::new);
									promo.Gifts = mapper.writeValueAsString(giftTmp);
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
					// var realDiscountValue = PromotionHelper.CalculateRealDiscountValue(tmp,
					// productId, productCode, message.SiteID, 3, iCached);
					// item.PromotionRealDiscountValue = realDiscountValue;
				}

			}
		}

		// TODO: Cap nhat Elasticsearch
		AddUpdatePromotion(productId, productCode, lstNewPromos, comboPromotions, flashSalePromotions,
				expiredPromotions);
//        Queue.Current.Push("bhxnew.productse", new MessageQueue
//        {
//            Action = DataAction.Update,
//            ClassName = AsmClass.Didx_Productse_ProductSE,
//            Identify = productId,
//            SiteID = message.SiteID,
//            Lang = message.Lang,
//            ID = -999L
//        });

		return lstNewPromos.toArray(PromotionBHX[]::new);
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
			ArrayList<PromotionBHX> lstExpiredPromotions) throws JsonParseException, JsonMappingException, IOException {

		int[] excludes = null;
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
				}

				if (excludes != null && Arrays.stream(excludes).anyMatch(p -> p == px.Id)) {
					hiddenPromotions.add(px);
				} else {
					promotions.add(px);
				}
				promotions.add(px);
			}
		}
		promotions = promotions.stream().sorted(Comparator.comparing(PromotionBHX::getBeginDate))
				.collect(Collectors.toCollection(ArrayList::new));

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
				}
				expiredPromotions.add(px);
			}
		}
		comboPromotions = comboPromotions.stream().sorted(Comparator.comparing(PromotionBHX::getBeginDate))
				.collect(Collectors.toCollection(ArrayList::new));

		hiddenPromotions = hiddenPromotions.stream().sorted(Comparator.comparing(PromotionBHX::getBeginDate))
				.collect(Collectors.toCollection(ArrayList::new));

		flashSalePromotions = flashSalePromotions.stream().sorted(Comparator.comparing(PromotionBHX::getBeginDate))
				.collect(Collectors.toCollection(ArrayList::new));

		expiredPromotions = expiredPromotions.stream().sorted(Comparator.comparing(PromotionBHX::getBeginDate))
				.collect(Collectors.toCollection(ArrayList::new));

		return true;
	}

	public ProductRelevantSO GetProductRelevantSOFromES(String indexDB, int id) {
		for (int i = 0; i < 10; i++) {

			try {

				return ElasticClient.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST).GetSingleObject(indexDB,
						Utils.toString(id), ProductRelevantSO.class);

			} catch (Exception e) {
				Logs.WriteLine("Exception GetProductRelevantSOFromES ES FAILED: " + id);
				Logs.WriteLine(e);
				Utils.Sleep(100);
			}
		}

		return null;
	}

	public boolean IndexProductRelevantSOFromES(String index, ProductRelevantSO product) {
		for (int i = 0; i < 10; i++) {

			try {

				return ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST).IndexObject(index,
						product, Utils.toString(product.GiftId));

			} catch (Exception e) {
				Logs.WriteLine("Exception IndexProductRelevantSOFromES ES FAILED: " + product.GiftId);
				Logs.WriteLine(e);
				Utils.Sleep(100);
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
	public boolean UpdateProductRelevant(long productId, PromotionBHX promo) {
		try {
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
				if (tmp.PromotionInfos == null || tmp.PromotionInfos.length <= 0) {
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
					var streamPromo = Arrays.stream(tmp.PromotionInfos);
					var lstPromo = streamPromo.collect(Collectors.toList());
					if (streamPromo.anyMatch(i -> i.Id == promo.Id && i.StoreIds == promo.StoreIds)) {
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
		} catch (Exception e) {
			Logs.WriteLine("Exception UpdateProductRelevant: " + productId);
			Logs.WriteLine(e);
			Utils.Sleep(100);
		}

		return false;
	}

	/// <summary>
	/// Cập nhật thêm trạng thái cho sản phẩm combo
	/// </summary>
	/// <param name="productId"></param>
	/// <returns></returns>
	public boolean UpdateComboRelevant(long productId) {
		var index = "bhx_new_relevantproduct";

		var tmp = GetProductRelevantSOFromES(index, (int) productId);
		if (tmp != null && tmp.ComboInfos != null && tmp.ComboInfos.length > 0) {
			for (var combo : tmp.ComboInfos) {
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
