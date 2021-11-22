package mwg.wb.pkg.productold;

import mwg.wb.business.ProductHelper;
import mwg.wb.business.ProductOldHelper;
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.*;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductOldBO;
import mwg.wb.model.search.ProductOldImeiSO;
import mwg.wb.model.search.ProductSO;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.joda.time.DateTime;

import com.google.common.base.Strings;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/*Note
 * 
 * Đối với sp khong co imei
 * Price2 : lây từ cms (từ graph)
 * 
 * 
 * Đối sp có imei
 * Price2: lây từ erp
 *
 * PriceAfterPromotion: giá sau khuyến mãi (thay thế cho NewRealPrice2)
 * NewDeltaPrice: chenh lech gia truoc va sau km
 * NewDiscountValuePercent: phan tram giam gia'
 * NewDiscountValue: giá trị giảm giá
 * 
 * InventoryStatus = 2 (máy cũ đổi trả)
 * InvenstoryStatus = 7 (máy cũ hàng chưng bày)
 * InvenstoryStatus = 8 (đồng hồ giảm giá mới)
 * 
 * 
 * 
 * */

public class ProductOldTest implements Ididx {

	static final String CurrentIndexDB = "ms_productold";
	static final String CurrentTypeDB = "productold";
	ORThreadLocal factoryRead;
	ErpHelper erpHelper = null;
	private ClientConfig clientConfig = null;
	int DataCenter = 0;
	private ProductHelper productHelper = null;
	private ProductOldHelper oldHelper = null;
	private ProductOldPromotion oldPromotion;


	@Override
	public void InitObject(ObjectTransfer objectTransfer) {
		productHelper = (ProductHelper) objectTransfer.productHelper;
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		erpHelper = (ErpHelper) objectTransfer.erpHelper;
		oldHelper = (ProductOldHelper) objectTransfer.productOldHelper;
		oldPromotion = new ProductOldPromotion();
		oldPromotion.InitObject(objectTransfer);
	}

	public ResultMessage DelOld(int oldid) {
		ResultMessage resultMessage = new ResultMessage();
		resultMessage.Code = ResultCode.Success;
		for (int i = 0; i < 10; i++) {
			try {
				var update = new UpdateRequest(CurrentIndexDB, oldid + "").doc("{\"IsDeleted\":1}", XContentType.JSON)
						.docAsUpsert(true).detectNoop(false);
				var client = ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
				var response = client.update(update, RequestOptions.DEFAULT);
				if (response == null || response.getResult() != Result.UPDATED) {
					Logs.WriteLine("-Index productold   to ES FAILED: " + oldid + ", " + response.getResult().name()
							+ "######################");

					resultMessage.Code = ResultCode.Retry;
					Utils.Sleep(100);

				} else {
					resultMessage.Code = ResultCode.Success;
					return resultMessage;
				}
			} catch (Exception e) {
				Logs.WriteLine("Exception Index productold   to ES FAILED: " + oldid);
				Logs.WriteLine(e);
				Utils.Sleep(100);
				resultMessage.StackTrace = Stream.of(e.getStackTrace()).map(x -> x.toString())
						.collect(Collectors.joining(", "));
				resultMessage.Code = ResultCode.Retry;

			}
		}
		return resultMessage;
	}

	public ResultMessage Refresh(MessageQueue message) {

//		checkSiteID();

		DataCenter = message.DataCenter;
		ResultMessage resultMessage = new ResultMessage();
		try {

			int oldid = 0;
			try {
				oldid = Integer.parseInt(message.Identify);
			} catch (NumberFormatException e) {
				Logs.WriteLine("Invalid OLDID");
				resultMessage.Code = ResultCode.Success;
				return resultMessage;
			}

			if (oldid <= 0) {
				resultMessage.Code = ResultCode.Success;
				return resultMessage;
			}
			if (clientConfig.IS_NOT_UPDATE_ES == 1) {
				resultMessage.Code = ResultCode.Success;
				return resultMessage;
			}

			switch (message.Action) {
			case Delete:
				return DelOld(oldid);

			case Add:
			case Update:
				ProductOldBO[] x = null;
				try {
					x = factoryRead.QueryFunction("product_GetByOldID", ProductOldBO[].class, false, oldid);
				} catch (Throwable e2) {
					Logs.LogException(e2);
					resultMessage.Code = ResultCode.Retry;
					resultMessage.StackTrace = Stream.of(e2.getStackTrace()).map(z -> z.toString())
							.collect(Collectors.joining(", "));
					return resultMessage;
				}
				if (x == null || x.length == 0) {
					return DelOld(oldid);

				}

				ProductOldBO oldbo = Stream.of(x).findFirst().orElse(null);
				if (oldbo == null) {
					return DelOld(oldid);

				} else {

					if (GConfig.ProductTaoLao.containsKey(oldbo.ProductID)) {
						resultMessage.Code = ResultCode.Success;
						return resultMessage;

					}
					int SiteID = DidxHelper.getSitebyBrandID(oldbo.BrandID);
					String lang = DidxHelper.getLangByBrandID(oldbo.BrandID);
					String langTerm = DidxHelper.GenTerm3(lang);
//					String modelterm = oldbo.ProductID + "_" + SiteID + "_" + langTerm;
					String oldTerm = String.valueOf(oldbo.OLDID);
					String[] includes = new String[] { "Keyword" };
					String modelKeyword = "";
//				var products = factoryRead.QueryFunction("product_GetByIdWithPriceDef", ProductBO[].class, false,
//						oldbo.ProductID, SiteID, 3, lang);

					ProductBO model = null;
					try {
						model = productHelper.GetProductOldModelFromCache(oldbo.ProductID, 3, SiteID, lang);
					} catch (Throwable e1) {
						Logs.LogException(e1);
						resultMessage.Code = ResultCode.Retry;
						resultMessage.StackTrace = Stream.of(e1.getStackTrace()).map(z -> z.toString())
								.collect(Collectors.joining(", "));
						return resultMessage;
					}
//					ProductSO productso = ElasticClient.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
//							.GetFieldsByObject(clientConfig.ELASTICSEARCH_PRODUCT_INDEX, modelterm, ProductSO.class,
//									includes);
//					if (productso != null && productso.ProductID > 0) {
//
//						modelKeyword = productso.Keyword;
//					}

					if (oldbo != null && oldbo.OLDID > 0) {
						
						List<ProductOldImeiSO> reList = null;
						ProductOldImeiSO oldso = ElasticClient.getInstance(clientConfig.SERVER_ELASTICSEARCH_READ_HOST)
								.GetSingleObject("ms_productold", oldTerm, ProductOldImeiSO.class);
						if (oldso == null) {
							oldso = new ProductOldImeiSO();
						} else {
							reList = oldHelper.getProductOldByCode(oldbo.ProductCode.trim(),
									new String[] { oldbo.InventoryStatusID + "" }, oldbo.StoreID, SiteID);
							
							
						}
						// cap nhat price2 cho sp

						double price2 = 0;


						oldso.Quantity = oldbo.IsImei ? 1 : oldbo.Quantity;

						if (reList != null && reList.size() > 0) {
							price2 = erpHelper.GetRefSalePriceOfProduct(oldbo.ProductCode, 1, oldbo.InventoryStatusID,
									SiteID);
						} else if (oldso.Quantity > 0 && oldbo.InventoryStatusID > 0
								&& !Strings.isNullOrEmpty(oldbo.ProductCode)) {
							price2 = erpHelper.GetRefSalePriceOfProduct(oldbo.ProductCode, 1, oldbo.InventoryStatusID,
									SiteID);

						}

						oldso.OLDID = oldbo.OLDID;
						oldso.ProductID = oldbo.ProductID;
						oldso.CategoryID = oldbo.CategoryID;
						oldso.ManufacturerID = oldbo.ManufacturerID;
						if (!Utils.StringIsEmpty(oldbo.IMEI)) {
							oldso.SEIMEI = "IMEI" + oldbo.IMEI.trim();
							oldso.IMEI = oldbo.IMEI.trim();
						} else {
							oldso.SEIMEI = "";
							oldso.IMEI = "";
						}
						oldso.ProductCode = oldbo.ProductCode.trim();
						oldso.Keyword = DidxHelper.FormatKeywordField(
								DidxHelper.FilterVietkey(oldbo.ProductCode + " " + oldbo.ProductName)) + modelKeyword;
						oldso.BrandID = oldbo.BrandID;
						oldso.LanguageID = langTerm;
						oldso.ProvinceID = oldbo.ProvinceID;
						oldso.DistrictID = oldbo.Districtid;
						oldso.Price = (long) oldbo.Price;
						oldso.SiteID = SiteID;
						oldso.IsDeleted = oldbo.IsDeleted;

						oldso.RealPrice = (long) oldbo.Price;
						oldso.Price2 = (long) price2;
						oldso.RealPrice2 = (long) price2;
						
						oldso.ColorID = oldbo.ColorID;
						oldso.IsImei = oldbo.IsImei ? 1 : 0;
						oldso.UnlockDate = oldbo.UnlockDate != null ? oldbo.UnlockDate
								: new DateTime().minusMonths(12).toDate();
						oldso.StoreID = oldbo.StoreID;
						oldso.CreatedDate = oldbo.CreatedDate;
						// productOLdso.IsPromotion = oldbo.IsPromotion ? 1 : 0;
						// productOLdso.DiscountValue = oldbo.DiscountValue;
						oldso.ManufacturerID = oldbo.ManufacturerID;
						oldso.didx_updateddate = Utils.GetCurrentDate();
						oldso.didx_source = "se";
						// productOLdso.IsDeleted = 0;
						oldso.InventoryStatusID = oldbo.InventoryStatusID;
						if (model != null) {
							oldso.ProductPrice = model.ProductErpPriceBOList != null
									&& model.ProductErpPriceBOList.length > 0
											? (long) model.ProductErpPriceBOList[0].Price
											: 0;
						}
						oldso.PricePercent = oldso.ProductPrice > 0
								? 100 - ((double) oldso.Price / oldso.ProductPrice) * 100d
								: 0;
//							Promotion = promos.length > 0 ? promos[0] : null;
//								PromotionListByCode = Arrays.stream(promos).filter(p -> p.PromotionType == 2)
//										.collect(Collectors.toList());
//								PromotionListByImei = Arrays.stream(promos).filter(p -> p.PromotionType == 1)
//										.collect(Collectors.toList());
//							}
//						};
						for (int i = 0; i < 10; i++) {
							try {
								if (!ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
										.IndexObject2(CurrentIndexDB, CurrentIndexDB, oldso, oldTerm)) {
									Logs.WriteLine(
											"-Index productold  to ES FAILED: " + oldTerm + " ######################");
									Utils.Sleep(100);

								} else {
									
									
									//chạy promotion luôn
									// khúc này phải đẩy lên queue nhưng chưa rành nên làm chay test trước
									
									var productCode = oldbo.ProductCode;
									var siteId =oldbo.siteId;
									var imei = oldbo.IMEI;
									var productid = oldbo.ProductID;
									var storeId = oldbo.StoreID;
									
									var m = new MessageQueue() {
										{
											// productcode|imei|productID|storeid
//											Identify = "3051098001006|AAANZ7E090000K720447|198105|1760";
											Identify = productCode+"|"+imei+"|"+productid+"|"+storeId;
											Note = "";
											SiteID = siteId;
											Lang = "vi-VN";
											DataCenter = 3;
											Action = DataAction.Add;
										}
									};
									oldPromotion.RefreshNew(m);
									
									resultMessage.Code = ResultCode.Success;
									return resultMessage;
								}
							} catch (Throwable e) {
								Utils.Sleep(100);
								Logs.WriteLine(
										"-Index productold  to ES FAILED: " + oldTerm + " ######################");

								resultMessage.Code = ResultCode.Retry;
								resultMessage.StackTrace = Stream.of(e.getStackTrace()).map(z -> z.toString())
										.collect(Collectors.joining(", "));
								Logs.LogException(e);
							}
						}
						resultMessage.Code = ResultCode.Retry;
						return resultMessage;

					}

				}

				break;
			default:
				break;
			}
			resultMessage.Code = ResultCode.Success;
			return resultMessage;
		} catch (Throwable exx) {
			Logs.LogException(exx);
			resultMessage.Code = ResultCode.Retry;
			return resultMessage;
		}
	}

	private void checkSiteID() {
		ProductOldBO[] x = null;
		int id[] = new int[] {};
		String thegioididong = "";
		String dienmayxanh = "";

		for (int i : id) {
			try {
				x = factoryRead.QueryFunction("product_GetByOldID", ProductOldBO[].class, false, i);

				if (x != null && x.length > 0) {
					if (x[0].siteId == 1) {
						thegioididong += i + ",";
					} else if (x[0].siteId == 2) {
						dienmayxanh += i + ",";
					}
				}
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println(thegioididong);
		System.out.println(dienmayxanh);

	}

	@Override
	public ResultMessage RunScheduleTask() {
		return null;
	}
}
