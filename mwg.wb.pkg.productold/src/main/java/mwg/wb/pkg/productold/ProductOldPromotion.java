package mwg.wb.pkg.productold;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mwg.wb.common.*;
import mwg.wb.model.products.ProductOldBO;

import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateAction;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentType;

import mwg.wb.business.ProductOldHelper;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.promotion.PromotionOldProductBO;
import mwg.wb.model.search.ProductOldImeiSO;
import mwg.wb.model.search.PromotionOldSO;

public class ProductOldPromotion implements Ididx {

	static String CurrentIndexDB = "ms_productold";
	static String CurrentTypeDB = "productold";
//	private ORThreadLocal factoryRead;
	private ErpHelper erpHelper = null;
	private ClientConfig clientConfig = null;
	private ProductOldHelper oldHelper = null;

	int DataCenter = 0;

	// listcate check rang
	public static List<Integer> MobileCate = List.of(42);
	public static List<Integer> LaptopCate = List.of(44);
	public static List<Integer> TabletCate = List.of(522);
	public static List<Integer> ElecCate = List.of(1942, 2162, 2165);
	public static List<Integer> ICTCate = List.of(1943, 1944, 166, 2002, 2202, 1962, 7604, 5475, 7739);
	public static List<Integer> AccessoryCate = List.of(57, 58, 54, 2162, 55, 75, 86, 4728, 4727);

	public static List<Integer> ApplianceCate = List.of(1922, 7498, 3385, 1983, 3305, 1982, 1985, 2062, 2322, 2262,
			7684, 7685, 2063, 1987, 1989, 2064, 4645, 1992, 2428, 2222, 1988, 1990, 5473, 7367, 2302, 7173, 7075, 1062,
			7901, 7899, 1991, 346, 366, 365, 8579);

	public static List<Integer> SmartWatchCate = List.of(7077);

	public static List<Integer> AllListCate = List.of(42, 44, 522, 1942, 2162, 2165, 1943, 1944, 166, 2002, 2202, 1962,
			7604, 5475, 7739, 57, 58, 54, 2162, 55, 75, 86, 4728, 4727, 1922, 7498, 3385, 1983, 3305, 1982, 1985, 2062,
			2322, 2262, 7684, 7685, 2063, 1987, 1989, 2064, 4645, 1992, 2428, 2222, 1988, 1990, 5473, 7367, 2302, 7173,
			7075, 1062, 7901, 7899, 1991, 346, 366, 365, 8579, 7077);

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
//		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		erpHelper = (ErpHelper) new ErpHelper("http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx",
				"werwerewrw32423!@4#123");
		oldHelper = (ProductOldHelper) objectTransfer.productOldHelper;
		CurrentIndexDB = "ms_productold";// clientConfig.ELASTICSEARCH_PRODUCT_OLD_INDEX;

	}

	public ResultMessage Refresh(MessageQueue message) {
		DataCenter = message.DataCenter;
		String note = message.Note;
		boolean isLog = note != null && note.contains("LOG");

		String qu = "gr.dc4.sql.productoldpromotion";
		String qu2 = "gr.dc4.sql.productoldpromotion";
		String qubk = "gr.dc2.sql.productoldpromotion";
		String qudev = "gr.beta.sql.productoldpromotion";

		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;
		String strIdentify = message.Identify;
		if (strIdentify.startsWith("X"))
			strIdentify = strIdentify.substring(1);
		if(strIdentify.startsWith("NEW")){
			try {
				return RefreshNew(message);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
		String[] messIdentify = strIdentify.split("\\|");
		if (messIdentify.length < 5) {
			Logs.WriteLine("param: " + strIdentify);
			return r;
		}
		// productcode|imei|productid|Inventorystatusid|storeid
		String ProductCode = messIdentify[0].trim();
		String Imei = messIdentify[1].trim();
//		int productid = Integer.valueOf(messIdentify[2]);
		int Inventorystatusid = Integer.valueOf(messIdentify[3]);
		int storeid = Integer.valueOf(messIdentify[4]);
		int SiteID = message.SiteID;
//		String Lang = message.Lang;
		boolean isimei = !Imei.equals("0");

		switch (message.Action) {

		case Add:
		case Update:
			PromotionOldProductBO[] promos = null;
			try {
				if (isimei)
					promos = erpHelper.GetProductOldPromotionByImei(ProductCode, Imei, storeid, Inventorystatusid,
							SiteID);
				else
					promos = erpHelper.GetProductOldPromotionByCode(ProductCode, Imei, storeid, Inventorystatusid,
							SiteID);
			} catch (Exception e) {
				Logs.WriteLine("get promotion from ERP failed: " + message.Identify);
				Logs.LogException(e);
				r.StackTrace = Logs.GetStacktrace(e);
				r.Message = "get promotion from ERP failed: " + message.Identify + ", " + e.getMessage();
				r.Code = ResultCode.Retry;
				return r;
			}
			if (promos == null)
				promos = new PromotionOldProductBO[] {};

			// var Promotion = promos.length > 0 ? promos[0] : null;
			var all = Arrays.asList(promos);

			try {
				List<ProductOldImeiSO> plist;
				if (Inventorystatusid == 3 || Inventorystatusid == 7)
					plist = oldHelper.getProductOldByCode(ProductCode, new String[] { Inventorystatusid + "" }, storeid,
							SiteID);
				else
					plist = oldHelper.getProductOldByCode(ProductCode, new String[] { "2", "4" }, storeid, SiteID);
				var storeidlist = plist.stream().collect(Collectors.toMap(x -> x.StoreID, x -> x, (x, y) -> x))
						.values();
				var eclient = ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST);
				for (var old : storeidlist) {
					var id = old.StoreID;
					Logs.WriteLine("update promotion by store " + id);
					if (promos != null) {
						List<PromotionOldProductBO> storefiltered = null;
						if (!isimei)
							storefiltered = all.stream().filter(x -> x.StoreID == id || x.StoreID == -1)
									.collect(Collectors.toList());
						else
							storefiltered = all;
						// imei recordid: imei_<siteid>_<imei>_<promoid>
						// code recordid: code_<siteid>_<productcode>_<invstatusid>_<storeid>_<promoid>
						// addon: addon_<siteid>_<productcode>_<promoid>
						var commonids = List.of("imei_" + SiteID + "_" + ProductCode + "_" + Imei,
								"code_" + SiteID + "_" + ProductCode + "_" + Inventorystatusid + "_" + id,
								"addon_" + SiteID + "_" + ProductCode);
						for (var x : storefiltered) {
							var recordid = commonids.get(x.PromotionType - 1);
							x.RecordIDCommon = recordid;
							x.RecordID = recordid + "_" + x.PromotionID;
							x.SiteID = SiteID;
							x.Imei = x.PromotionType == 1 ? Imei : "0";
							x.ProductCode = x.ProductCode.trim();
							x.StoreID = id;
						}
						try {
							QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).pushUpsertODBObjects(
									"product_old_promotion", "recordid", qu, qu2, qubk, qudev, storefiltered,
									"recordidcommon", commonids, isLog, note, DataCenter);
							Logs.WriteLine("pushed storeid: " + id);
						} catch (Exception e) {
							Logs.WriteLine("Exception Index promotion status to graph FAILED: " + message.Identify);
							Logs.LogException(e);
							r.StackTrace = Logs.GetStacktrace(e);
							r.Message = "Exception Index promotion status to graph FAILED: " + message.Identify + ", "
									+ e.getMessage();
							r.Code = ResultCode.Retry;
							return r;
						}
						try {
							long priceAfterPromos = old.Price;
							int discountValueCode = 0, discountValueIMEI = 0;
							Date now = new Date();
							for (var promo : storefiltered)
								if (promo.EndDate.after(now) && promo.BeginDate.before(now)
										&& (promo.PromotionType == 1 || promo.PromotionType == 2)) {
									if (promo.IsPercentDiscount) {
										priceAfterPromos -= priceAfterPromos * (promo.Discountvalue / 100f);
										if (promo.PromotionType == 1)
											discountValueIMEI += promo.Discountvalue;
										else
											discountValueCode += promo.Discountvalue;
									} else
										priceAfterPromos -= promo.Discountvalue;
								}
							var map = storefiltered.stream().map(x -> PromotionOldSO.fromBO(x, id))
									.collect(Collectors.groupingBy(x -> x.promotiontype));
							var lstcode = map.get(2);
							var lstimei = map.get(1);
//							var lstaddon = map.get(3);
							String oldid = old.OLDID + "";
							String update = "{\"DiscountValueCode\": " + discountValueCode + ", \"DiscountValueIMEI\": "
									+ discountValueIMEI + ", \"PriceAfterPromotion\": " + priceAfterPromos
									+ ", \"DiscountValue\": " + (discountValueCode + discountValueIMEI) + "}";
							var req = new UpdateRequest(CurrentIndexDB, oldid + "").doc(update, XContentType.JSON)
									.docAsUpsert(true).detectNoop(false);
							eclient.getClient().update(req, RequestOptions.DEFAULT);
							eclient.UpdateFieldOBject(CurrentIndexDB, oldid, "PromotionsListCode",
									lstcode != null ? lstcode : new ArrayList<PromotionOldSO>());
							eclient.UpdateFieldOBject(CurrentIndexDB, oldid, "PromotionsListImei",
									lstimei != null ? lstimei : new ArrayList<PromotionOldSO>());
//							if (lstaddon != null)
//								eclient.UpdateFieldOBject(CurrentIndexDB, oldid, "PromotionsListAddon", lstaddon);
							Logs.WriteLine("indexed to elastic");
						} catch (Exception e) {
							Logs.WriteLine("Exception Index promotion status to ES FAILED: " + message.Identify);
							Logs.LogException(e);
							r.StackTrace = Logs.GetStacktrace(e);
							r.Message = "Exception Index promotion status to ES FAILED: " + message.Identify + ", "
									+ e.getMessage();
							r.Code = ResultCode.Retry;
							return r;
						}
					}
				}
			} catch (Exception e) {
				Logs.WriteLine("Exception Index promotion status FAILED: " + message.Identify);
				Logs.LogException(e);
				r.StackTrace = Logs.GetStacktrace(e);
				r.Message = "Exception Index promotion status FAILED: " + message.Identify + ", " + e.getMessage();
				r.Code = ResultCode.Retry;
				return r;
//				e.printStackTrace();
			}

			r.Code = ResultCode.Success;
			break;
		default:
			break;
		}
		return r;

	}

	public ResultMessage RefreshNew(MessageQueue message) throws IOException {
		DataCenter = message.DataCenter;
		String note = message.Note;
		boolean isLog = note != null && note.contains("LOG");

		String qu = "gr.dc4.sql.productoldpromotion";
		String qu2 = "gr.dc4.sql.productoldpromotion";
		String qubk = "gr.dc2.sql.productoldpromotion";
		String qudev = "gr.beta.sql.productoldpromotion";

		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;
		String strIdentify = message.Identify;
		if (strIdentify.startsWith("X"))
			strIdentify = strIdentify.substring(1);
		if(strIdentify.startsWith("NEW"))
			strIdentify = strIdentify.replace("NEW","");

		String[] messIdentify = strIdentify.split("\\|");
		if (messIdentify.length < 3) {
			Logs.WriteLine("param: " + strIdentify);
			return r;
		}
		// productcode|imei|productID|storeid
		String ProductCode = messIdentify[0].trim();
		String Imei = messIdentify[1].trim();
		int productID = Utils.toInt(messIdentify[2]);
		int storeid = 0;
//		int Inventorystatusid = Integer.valueOf(messIdentify[3]);
		if (messIdentify.length > 3) {
			storeid = Integer.valueOf(messIdentify[3]);
		}
		int SiteID = message.SiteID;
//		String Lang = message.Lang;
		boolean isimei = !Imei.equals("0");
//		if (Imei.equals("0")) {
//			Logs.Write("Imei equal 0 : " + Imei);
//			return r;
//		}

		switch (message.Action) {

		case Add:
		case Update:
			if (Strings.isNullOrEmpty(ProductCode)) {
				Logs.Write("ProductCode is : " + ProductCode);
				return r;
			}
			if (productID <= 0) {
				Logs.Write("ProductID <= 0");
				return r;
			}
			PromotionOldProductBO[] promos = null;
			int provinceID = 3;

			var lstProductOld = oldHelper.getProductOldByCode(ProductCode, new String[] { "2", "4" }, storeid, SiteID);
			if (lstProductOld != null && lstProductOld.size() > 0) {

				provinceID = lstProductOld.stream().findFirst().get().ProvinceID;
				int oldID = lstProductOld.stream().findFirst().get().OLDID;
//					ProductOldBO productOldInfo = null;
//					if (oldID > 0) {
//						productOldInfo = oldHelper.getProductOldImei(oldID);
//						if (storeid <= 0 && productOldInfo != null) {
//							storeid = productOldInfo.StoreID;
//						}
//					}
				try {
					promos = erpHelper.GetPromotionByPrdIDNew(ProductCode, Imei, storeid, provinceID, SiteID);

				} catch (Throwable e) {
					Logs.WriteLine("get promotion from ERP failed: " + message.Identify);
					Logs.LogException(e);
					r.StackTrace = Logs.GetStacktrace(e);
					r.Message = "get promotion from ERP failed: " + message.Identify + ", " + e.getMessage();
					r.Code = ResultCode.Retry;
					return r;
				}
				var listPromotion = List.of(promos);
				if (listPromotion != null) {

					// tam thoi loai bo km qua tang
					listPromotion = listPromotion.stream().filter(x -> x != null && x.Discountvalue > 0)
							.collect(Collectors.toList());

					// loai bo km bankem
					listPromotion = listPromotion.stream()
							.filter(x -> x != null && !x.GroupID.equalsIgnoreCase("bankem"))
							.collect(Collectors.toList());

					// loai bo km chon . may cu khong co km chon
					var countRepeatPromotion = listPromotion.stream().collect(Collectors.groupingBy(x -> x.PromotionID))
							.values().stream().filter(z -> z.size() > 1).map(y -> new Counter() {
								{
									ID = y.get(0).PromotionID;
									Counter = y.size();
								}
							}).collect(Collectors.toList());

					for (Counter item : countRepeatPromotion) {
						if (item.Counter > 1) {
							listPromotion = listPromotion.stream().filter(x -> x != null && x.PromotionID != item.ID)
									.collect(Collectors.toList());

						}

					}

					// loai bo km có nhiều code quà

					listPromotion = listPromotion.stream()
							.filter(x -> !Strings.isNullOrEmpty(x.ProductID) && !x.ProductID.contains("|"))
							.collect(Collectors.toList());

					// loai bo km khong hop le theo rule
					// lay km
					// 1. Neu list km khong co cai nao ISONLYAPPYFORIMEI =1 thì lay all km
					// 2. Neu list km có ít nhât 1 cái ISONLYAPPYFORIMEI =1 thì lay thang dau tien

					var isHasIsOnlyApplyForImei = listPromotion.stream().anyMatch(x -> x.IsOnlyAppyForImei == 1);
					if (isHasIsOnlyApplyForImei) {
						var tempPromotion = listPromotion.stream().filter(x -> x.IsOnlyAppyForImei == 1).findFirst()
								.orElse(new PromotionOldProductBO());
						listPromotion = listPromotion.stream().filter(x -> x.Discountvalue <= 0)
								.collect(Collectors.toList());// loai bo het km giam tien
						listPromotion.add(tempPromotion);// them thang dau tien hop le vao
					}

				} else {
					listPromotion = new ArrayList<PromotionOldProductBO>();
				}
				if (listPromotion.size() > 0) {
					System.out.println("Có " + listPromotion.size() + " khuyến mãi ");
				} else {
					System.out.println("Không có khuyến mãi");
				}

				var eclient = ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST);

				r = UpdateProductOldPromotion(listPromotion, lstProductOld, r, eclient, SiteID);
			}
			r.Code = ResultCode.Success;
			break;
		default:
			break;
		}
		return r;

	}

	private ResultMessage UpdateProductOldPromotion(List<PromotionOldProductBO> listPromotion,
			List<ProductOldImeiSO> lstProductOld, ResultMessage r, ElasticClientWrite eclient, int siteID) {

		if (lstProductOld != null && lstProductOld.size() > 0) {
			for (var item : lstProductOld) {

				if (item != null) {
					// lay truoc price2 start
					// price2
					if (item.IsImei <= 0) { // sp imei lay gia db

						try {
							var x = oldHelper.getProductOldImei(item.OLDID);
							if (x != null) {
								item.Price2 = (long) x.Price;
							}
						} catch (Throwable e1) {
							r.StackTrace = Logs.GetStacktrace(e1);
							r.Message = "get price from ERP failed: " + item.ProductCode + ", " + e1.getMessage();
							r.Code = ResultCode.Retry;
							e1.printStackTrace();
							return r;

						}

						// con khong co gia nua thi lay tu erp luon
						if (item.Price2 <= 0) {
							try {
								var erpPrice = erpHelper.GetRefSalePriceOfProduct(item.ProductCode, 1,
										item.InventoryStatusID, siteID);

								item.Price2 = (long) erpPrice;

							} catch (Exception e) {
								Logs.LogException(e);
								Logs.WriteLine("Exception Index price status FAILED: " + item.ProductCode);
								r.StackTrace = Logs.GetStacktrace(e);
								r.Message = "get price from ERP failed: " + item.ProductCode + ", " + e.getMessage();
								r.Code = ResultCode.Retry;
								e.printStackTrace();
								return r;
							}
						}
					}

					// lay truoc price2 end
					try {

						double newDiscountValue2 = 0;// tinh theo Price2
						double newDiscountValuePercent2 = 0;
						int reCheckDiscountValue = 0;
						int isPromotion = 0;

						long priceAfterPromotion = 0;
						long price2 = 0;
						var finalListPromotion = checkPromotion(listPromotion);
						if(finalListPromotion==null) finalListPromotion = new ArrayList<>();

					 

						// tinh gia tri khuyen mai
						for (var promotion : finalListPromotion) {
							if (promotion.Discountvalue > 0) {

								if (promotion.IsPercentDiscount) {

									float percent = ((float) promotion.Discountvalue) / 100;
									System.out.println("sp " + item.OLDID + " : giảm: " + percent * 100 + " %");
									newDiscountValue2 += item.Price2 * percent;
								} else {
									newDiscountValue2 += promotion.Discountvalue;
								}
							}
						}

						reCheckDiscountValue = 1;

						// check rang buoc
						if (item.Price2 > 0) {
							newDiscountValuePercent2 = Math.floor(((float) newDiscountValue2 / item.Price2) * 100);

						}
						if (newDiscountValue2 > 0 && item.Price2 > 0) {

							if ((MobileCate.contains(item.CategoryID)
									&& (newDiscountValuePercent2 < 3 || newDiscountValuePercent2 > 70))
									|| TabletCate.contains(item.CategoryID)
											&& (newDiscountValuePercent2 < 3 || newDiscountValuePercent2 > 70)
									|| LaptopCate.contains(item.CategoryID)
											&& (newDiscountValuePercent2 < 5 || newDiscountValuePercent2 > 50)
									|| ElecCate.contains(item.CategoryID)
											&& (newDiscountValuePercent2 < 5 || newDiscountValuePercent2 > 70)
									|| ICTCate.contains(item.CategoryID)
											&& (newDiscountValuePercent2 < 5 || newDiscountValuePercent2 > 70)
									|| AccessoryCate.contains(item.CategoryID)
											&& (newDiscountValuePercent2 < 5 || newDiscountValuePercent2 > 85)
									|| ApplianceCate.contains(item.CategoryID)
											&& (newDiscountValuePercent2 < 5 || newDiscountValuePercent2 > 85)
									|| SmartWatchCate.contains(item.CategoryID)
											&& (newDiscountValuePercent2 < 5 || newDiscountValuePercent2 > 70)
									|| !AllListCate.contains(item.CategoryID)
											&& (newDiscountValuePercent2 < 5 || newDiscountValuePercent2 > 50)) {

								newDiscountValue2 = 0;
								newDiscountValuePercent2 = 0;
							}

						}

						// price2
						price2 = item.Price2;
						priceAfterPromotion = item.Price2 > 0? item.Price2 - (long) newDiscountValue2:0;

						if (newDiscountValue2 > 0) {
							isPromotion = 1;
						} else {
							isPromotion = 0;

						}
						String oldid = item.OLDID + "";
						SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						String now = dateformat.format(new Date());

						String update = "{\"Price2\": " + price2 + ", \"NewIsPromotion\": " + isPromotion
								+ ", \"didx_updateddate\": \"" + now + "\", \"PriceAfterPromotion\": "
								+ priceAfterPromotion + ", \"NewDiscountValue\": " + newDiscountValue2
								+ ", \"ReCheckDiscountValue\": " + reCheckDiscountValue
								+ ", \"NewDiscountValuePercent\": " + newDiscountValuePercent2 + "}";

						var req = new UpdateRequest(CurrentIndexDB, oldid + "").doc(update, XContentType.JSON)
								.docAsUpsert(true).detectNoop(false);

						eclient.getClient().update(req, RequestOptions.DEFAULT);
						Logs.WriteLine("indexed to elastic");
					} catch (Exception e) {
						Logs.WriteLine("Exception Index promotion status to ES FAILED: " + item.OLDID);
						Logs.LogException(e);
						r.StackTrace = Logs.GetStacktrace(e);
						r.Message = "Exception Index promotion status to ES FAILED: " + item.OLDID + ", "
								+ e.getMessage();
						r.Code = ResultCode.Retry;
						return r;
					}

				}

			}
		}
		return r;

	}

	private List<PromotionOldProductBO> checkPromotion(List<PromotionOldProductBO> listPromotion) {
		var now = new Date();
		if (listPromotion != null && listPromotion.size() > 0) {
			listPromotion = listPromotion.stream()
					.filter(itemc -> itemc.BeginDate.before(now) && itemc.EndDate.after(now))
					.collect(Collectors.toList());
			return listPromotion;
		}
		return null;
	}

	@Override
	public ResultMessage RunScheduleTask() {
		return null;
	}

	class Counter {
		int ID;
		int Counter;
	}

}
