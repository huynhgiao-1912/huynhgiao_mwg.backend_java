package mwg.wb.pkg.promotion;

import java.awt.desktop.SystemSleepEvent;
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
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.BhxServiceHelper;
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
import mwg.wb.model.promotion.BillPromotionBHX;
import mwg.wb.model.promotion.BillPromotionBHXBO;
import mwg.wb.model.promotion.GiftBHX;
import mwg.wb.model.promotion.NewGiftBHX;
import mwg.wb.model.promotion.ProductRelevantSO;
import mwg.wb.model.promotion.ProductRelevantType;
import mwg.wb.model.promotion.PromotionBHX;
import mwg.wb.model.promotion.PromotionBHXBO;
import mwg.wb.model.promotion.PromotionTypes;
import mwg.wb.model.promotion.RelevantInfo;
import mwg.wb.model.promotion.URLInfo;
import mwg.wb.model.search.PromotionSO;

public class PromotionBillBHXV1 implements Ididx {
	// private OrientDBFactory factoryDB = null;
//	private PriceHelper priceHelper = null;
	private ProductHelper productHelper = null;
	private ObjectMapper mapper = null;
	private BhxServiceHelper bhxServiceHelper = null;
//	private ORThreadLocal factoryWrite = null;
//	private ORThreadLocal factoryRead = null;
	private ClientConfig clientConfig = null;

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {

//		factoryWrite = (ORThreadLocal) objectTransfer.factoryWrite;
//		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
//		priceHelper = (PriceHelper) objectTransfer.priceHelper;
		productHelper = (ProductHelper) objectTransfer.productHelper;
		mapper = (ObjectMapper) objectTransfer.mapper;
		bhxServiceHelper = (BhxServiceHelper) objectTransfer.bhxServiceHelper;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
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

	public boolean IndexDataES(mwg.wb.model.promotion.BillPromotionBHX billPromotion, boolean isLog, String strNOTE,
			String indexDB, long PromotionID, int SiteID, String LangID, int DataCenter) {
		String esKeyTerm = PromotionID + "_" + SiteID + "_" + (LangID.toLowerCase().replaceAll("-", "_"));

		if (billPromotion == null) {

			billPromotion = new mwg.wb.model.promotion.BillPromotionBHX();
		}
		// var solist = Stream.of(lstBillPromotion).map(x ->
		// x.getSOObject()).toArray(PromotionSO[]::new);
		// if (lstPromotion != null && lstPromotion.length > 0) {
		for (int i = 0; i < 10; i++) {

			try {

				return ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST).IndexObject(indexDB,
						billPromotion, esKeyTerm);

			} catch (Exception e) {
				Logs.WriteLine("Exception Index bill promotion status to ES FAILED: " + PromotionID);
				Logs.WriteLine(e);
				Utils.Sleep(100);
			}
		}

		// }else {}

		return false;
	}

	public boolean DeleteDataES(boolean isLog, String strNOTE, String indexDB, long PromotionID, int SiteID,
			String LangID, int DataCenter) {
		String esKeyTerm = PromotionID + "_" + SiteID + "_" + (LangID.toLowerCase().replaceAll("-", "_"));

		for (int i = 0; i < 10; i++) {

			try {

				return ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST).Delete(indexDB,
						indexDB, esKeyTerm);

			} catch (Exception e) {
				Logs.WriteLine("Exception Delete bill promotion status to ES FAILED: " + PromotionID);
				Logs.WriteLine(e);
				Utils.Sleep(100);
			}
		}
		return false;
	}
	
	public ProductRelevantSO GetProductRelevantSOFromES(String indexDB, int id) {
		for (int i = 0; i < 10; i++) {

			try {

				return ElasticClient.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
						.GetSingleObject(indexDB, Utils.toString(id), ProductRelevantSO.class);

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

				return ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
						.IndexObject(index, product, Utils.toString(product.GiftId));

			} catch (Exception e) {
				Logs.WriteLine("Exception IndexProductRelevantSOFromES ES FAILED: " + product.GiftId);
				Logs.WriteLine(e);
				Utils.Sleep(100);
			}
		}

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

			if (!strNOTE.contains("BHXTEST")) {
				return rsmsg;
			}

			boolean isLog = false;
			if (strNOTE.contains("LOG")) {

				isLog = true;
			}

			String promoId = messIdentify[0].trim();
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

			if (message.Action == DataAction.Delete) {
				// TODO:Xóa khỏi ElasticSearch
				DeleteDataES(isLog, strNOTE, "bhx_new_promo_bill", Utils.toLong(promoId), message.SiteID, message.Lang,
						message.DataCenter);
				DeleteDataES(isLog, strNOTE, "bhx_new_promo_qty", Utils.toLong(promoId), message.SiteID, message.Lang,
						message.DataCenter);
				rsmsg.Code = ResultCode.Success;
				return rsmsg;
			}

			// xử lý khuyến mãi bill
			int promotionType = 0;
			var promoBHX = GetPromotion(promoId, siteid, promotionType);

            if (promoBHX == null || promoBHX.Gifts == null || promoBHX.Gifts.stream().allMatch(g -> g.Quantity == 0))
            {
            	DeleteDataES(isLog, strNOTE, "bhx_new_promo_bill", Utils.toLong(promoId), message.SiteID, message.Lang,
						message.DataCenter);
            	DeleteDataES(isLog, strNOTE, "bhx_new_promo_qty", Utils.toLong(promoId), message.SiteID, message.Lang,
						message.DataCenter);

                rsmsg.Code = ResultCode.Success;
                return rsmsg;
            }
			boolean rs = false;
			promotionType = promoBHX.PromotionType;

			// Cập nhật vào Elasticsearch
			if (promotionType == 2) {
				// TODO: UpdateProductRelevant
				UpdateProductRelevant(promoBHX);
				rs = IndexDataES(promoBHX, isLog, strNOTE, "bhx_new_promo_bill", Utils.toInt(promoId), message.SiteID,
						message.Lang, message.DataCenter);
			} else if (promotionType == 4) {
				// TODO: UpdateProductRelevant
				UpdateProductRelevant(promoBHX);
				rs = IndexDataES(promoBHX, isLog, strNOTE, "bhx_new_promo_qty", Utils.toInt(promoId), message.SiteID,
						message.Lang, message.DataCenter);
			}

			if (!rs) {

				rsmsg.Code = ResultCode.Retry;
				Logs.WriteLine("IndexDataES false  ");
				Logs.Log(isLog, strNOTE, "IndexDataES false ");
				return rsmsg;

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

	public BillPromotionBHX GetPromotion(String promoId, int siteId, int promotionType) throws Throwable {

		// lấy khuyến mãi theo các kho, nếu ko đó có thì tạo storeid nói lại
		var stores = StoreProvinceConfig.GetListNormalStore();
		HashMap<Integer, BillPromotionBHXBO[]> lstpromo = new HashMap<Integer, BillPromotionBHXBO[]>();
		for (var item : stores) {
			var tmp = bhxServiceHelper.GetPromotionBHXById(Utils.toInt(promoId), item);
			if (tmp == null || tmp.length < 1) {
				continue;
			}

			lstpromo.put(item, tmp);
		}

		promotionType = 0;

		// get ko có cái nào thì trả về
		if (lstpromo.size() == 0)
			return null;

		var dt = new BillPromotionBHXBO[lstpromo.size()];
		for (BillPromotionBHXBO[] i : lstpromo.values()) {
			dt = i;
			break;
		}

		var firstRow = dt[0];

		if (firstRow.IsDeleted || firstRow.IsStopped ||
		// !firstRow.IsReview ||
				firstRow.PromoStoreType == 1) {
			// Chương trình KM đã bị XÓA hoặc DỪNG hoặc chưa duyệt
			return null;
		}

		promotionType = firstRow.PromotionType;
		if (promotionType != 2 && promotionType != 4) {
			// KHÔNG phải khuyến mãi theo giá trị đơn hàng hoặc khuyến mãi theo số lượng
			return null;
		}

		if (firstRow.MinTotalMoney < 1) {
			// Khai báo sai: Phải có giá trị đơn hàng / số lượng tối thiểu được áp dụng
			return null;
		}

		// Mức áp dụng trần
		double maxtotalmoney = 20000000; // Tối đa là 20 triệu
		var strMaxtotalmoney = firstRow.MaxTotalMoney < 0 ? "" : Utils.toString(firstRow.MaxTotalMoney).trim();
		if (!Utils.StringIsEmpty(strMaxtotalmoney)) {
			maxtotalmoney = firstRow.MaxTotalMoney;
		}

		String desription = "";
		if (Utils.StringIsEmpty(firstRow.HtmlDescription)) {
			if (!Utils.StringIsEmpty(firstRow.Description)) {
				desription = firstRow.Description;
			}
		} else {
			desription = firstRow.HtmlDescription;
		}

		var promotion = new BillPromotionBHX();

		promotion.Id = firstRow.PromotionId;
		promotion.Name = firstRow.PromotionName;
		promotion.Description = desription;
		promotion.BeginDate = firstRow.FromDate;
		promotion.EndDate = firstRow.ToDate;
		promotion.Step = firstRow.MinTotalMoney;
		promotion.Ceiling = maxtotalmoney;
		promotion.Type = firstRow.PromotionGiftType;
		promotion.ChooseType = firstRow.DonateType;
		promotion.ProductCodes = new ArrayList<String>();
		promotion.ProductGroups = new ArrayList<String>();
		promotion.Gifts = new ArrayList<NewGiftBHX>();

		for (var i = 0; i < dt.length; i++) {
			var r = dt[i];

			// Danh sách sản phẩm có khuyến mãi này
			if (!Utils.StringIsEmpty(r.ApplyProductId)) {
				var code = r.ApplyProductId.trim();
				if (!promotion.ProductCodes.contains(code)) {
					promotion.ProductCodes.add(code);
				}
			}

			// Danh sách nhóm sản phẩm (POS) được khuyến mãi này
			if (!Utils.StringIsEmpty(r.ApplyProductId)) {
				var groupId = r.ApplySubgroupId;
				if (!promotion.ProductGroups.contains(groupId)) {
					promotion.ProductGroups.add(groupId);
				}
			}

			// Quà tặng/Mua kèm
			var giftCode = Utils.StringIsEmpty(r.GiftProductId) ? "" : r.GiftProductId.trim();
			if (promotion.Gifts.stream().allMatch(g -> g.ProductCode != giftCode)) // Không được trùng
			{
				var giftId = productHelper.GetProductIDByProductCodeFromCache(giftCode);
				// Quà tặng phải được map code với WEB
				var productBOCheck = productHelper.GetProductBOByProductID(Utils.toInt(giftId), siteId, 3, "vi-VN",
						4100);
				if (productBOCheck != null) {
					var giftQuantity = r.GiftQuantity < 1 ? 0 : r.GiftQuantity;
					if (giftCode != "" && giftQuantity > 0) {
						var gift = new NewGiftBHX();
						gift.Id = productBOCheck.ProductID;
						gift.ProductCode = giftCode;
						gift.Quantity = giftQuantity;
						gift.Discount = r.DiscountPercent < 0 ? "" : Utils.toString(r.DiscountPercent) + "%"; // Giảm x%
																												// khi
																												// bán
																												// bèm
						gift.FixedPrice = r.PromotionPrice < 0 ? 0 : r.PromotionPrice; // Bán kèm với giá cố định

						promotion.Gifts.add(gift);
					}
				}
			}
		}

		// add store áp dụng
		if (promotion.StoreIds == null) {
			promotion.StoreIds = new ArrayList<Integer>();
		}
		for (Integer key : lstpromo.keySet()) {
			promotion.StoreIds.add(key);
		}

		promotion.PromotionType = promotionType;

		return promotion;
	}

	/// <summary>
	/// Cập nhật thông tin có liên quan giữa sp có KM và sp quà tặng
	/// </summary>
	/// <param name="promo">Thông tin khuyến mãi theo bill</param>
	/// <returns></returns>
	public Boolean UpdateProductRelevant(BillPromotionBHX promo) {
		try {
			if (promo == null || promo.Gifts == null)
				return false;

			var id = Utils.toInt(promo.Id);
			var index = "bhx_new_relevantproduct";
			for (var item : promo.Gifts) {
				var tmp = GetProductRelevantSOFromES(index, item.Id);
				if (tmp == null) {
					// Xử lý tạo mới                                                                                                                                                                                      
					tmp = new ProductRelevantSO();
					tmp.GiftId = item.Id;
				}

				// Promotion
				if (tmp.BillPromotionInfos == null || tmp.BillPromotionInfos.length <= 0) {
					var lstPromotionInfo = new ArrayList<RelevantInfo>();
					var relevantInfo = new RelevantInfo();
					relevantInfo.BeginDate = promo.BeginDate;
					relevantInfo.EndDate = promo.EndDate;
					relevantInfo.StoreIds = promo.StoreIds.stream().map(String::valueOf)
							.collect(Collectors.joining(","));
					relevantInfo.Id = id;
					relevantInfo.MainProductId = id;
					relevantInfo.Type = ProductRelevantType.BillPromotion.value();
					lstPromotionInfo.add(relevantInfo);

					tmp.BillPromotionInfos = lstPromotionInfo.stream().toArray(RelevantInfo[]::new);
				} else {
					var lstPromo = Arrays.stream(tmp.BillPromotionInfos).collect(Collectors.toList());
					if (Arrays.stream(tmp.BillPromotionInfos).anyMatch(i -> i.Id == id)) {
						lstPromo.removeIf(i -> i.Id == id);
					}
					var p = new RelevantInfo();
					p.BeginDate = promo.BeginDate;
					p.EndDate = promo.EndDate;
					p.StoreIds = promo.StoreIds.stream().map(String::valueOf).collect(Collectors.joining(","));
					p.Id = id;
					p.MainProductId = id;
					p.Type = ProductRelevantType.BillPromotion.value();
					lstPromo.add(p);
					tmp.BillPromotionInfos = lstPromo.stream().toArray(RelevantInfo[]::new);
				}

				// chỉ lưu lại những KM đang có hiệu lực
				tmp.BillPromotionInfos = Arrays.stream(tmp.BillPromotionInfos)
						.filter(p -> p.BeginDate.compareTo(Utils.GetCurrentDate()) <= 0
								&& p.EndDate.compareTo(Utils.GetCurrentDate()) >= 0)
						.toArray(RelevantInfo[]::new);

				IndexProductRelevantSOFromES(index, tmp);
			}
		} catch (Throwable e) {
		}

		return false;
	}
}
