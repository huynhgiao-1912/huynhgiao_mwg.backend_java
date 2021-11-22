package mwg.wb.pkg.promotion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import mwg.wb.business.CacheStaticHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.MessageQueuePushType;
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
import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.model.promotion.Promotion;
import mwg.wb.model.promotion.PromotionGRBO;
import mwg.wb.model.search.PromotionSO;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PromotionV1 implements Ididx {
	// private OrientDBFactory factoryDB = null;
//	private PriceHelper priceHelper = null;
	private ProductHelper productHelper = null;
	private ObjectMapper esMapper = null, mapper = null;
	private ErpHelper erpHelper = null;
//	private ErpPromotionHelper erpPromotionHelper = null;
//	private ORThreadLocal factoryWrite = null;
//	private ORThreadLocal factoryRead = null;
	private ClientConfig clientConfig = null;
//	private ObjectTransfer objTrans = null;
	LineNotify notifyHelperLog = null;
	String indexDB = "";
	int DataCenter = 0;
//	private Map<String, Long> processedBK = new HashMap<>();

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {

//		factoryWrite = (ORThreadLocal) objectTransfer.factoryWrite;
//		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
//		priceHelper = (PriceHelper) objectTransfer.priceHelper;
		productHelper = (ProductHelper) objectTransfer.productHelper;
		mapper = (ObjectMapper) objectTransfer.mapper;
		erpHelper = (ErpHelper) objectTransfer.erpHelper;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		indexDB = clientConfig.ELASTICSEARCH_PRODUCT_INDEX;
		esMapper = DidxHelper.generateJsonMapper(GConfig.DateFormatStringNews);
		notifyHelperLog = (LineNotify) objectTransfer.notifyHelperLog;
//		erpPromotionHelper = (ErpPromotionHelper) objectTransfer.erpPromotionHelper;
//		objTrans = objectTransfer;
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
			long ProductID, int SiteID, String LangID, int DataCenter, double price) {
		String esKeyTerm = ProductID + "_" + SiteID + "_" + (LangID.toLowerCase().replaceAll("-", "_"));

		if (lstPromotion == null) {

			lstPromotion = new mwg.wb.model.promotion.Promotion[] {};
		}
//		lstPromotion = Stream.of(lstPromotion).filter(x -> x.provinceIDApplied(SiteID == 6 ? 163 : 3))
//				.toArray(Promotion[]::new);

		String bankemIDs = Stream.of(lstPromotion).filter(x -> x.GroupID.equalsIgnoreCase("bankem"))
				.map(x -> x.PromotionID + "").collect(Collectors.joining(" "));

		int percent = 0;
		double priceAfterPromotion = 0;
		if (price > 0) {
			var totalDiscount = Stream.of(lstPromotion).mapToDouble(x -> x.discountValueNonAssign(price)).sum();
			percent = (int) Math.floor((totalDiscount / price) * 100);
			priceAfterPromotion = price - totalDiscount;
		}
		var solist = Stream.of(lstPromotion).filter(x -> x.ProductCode != null).map(x -> x.getSOObject())
//				.collect(Collectors.toMap(x -> x.codekey, x -> x, (x, y) -> x)).values()
				.toArray(PromotionSO[]::new);
		// if (lstPromotion != null && lstPromotion.length > 0) {
		for (int i = 0; i < 5000000; i++) {

			try {
				Date endDate = null;
				Date now = new Date();
				if(solist != null && solist.length > 0) {
					// lấy ra km có ngày hết hạn gần nhất
					for (PromotionSO promotionSO : solist) {
						if(endDate == null || endDate.after(promotionSO.strenddate)) {
							endDate = promotionSO.strenddate;
						}
					}
				}
				
				var promoJson = esMapper.writeValueAsString(solist);
				
				String json = "{\"nextpromotionupdate\": " + (endDate != null ? endDate.getTime() : 0) + ", \"PromotionSoList\":" + promoJson + ", \"PromotionDiscountPercent\": " + percent
						+ ", \"PriceAfterPromotion\": " + priceAfterPromotion + ", \"PromotionIdsBankem\": \""
						+ bankemIDs + "\"}";
				
				if(endDate != null) {
					var formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");  
				    String strDate = formatter.format(endDate);  
				    
				    json = "{\"strnextpromotionupdate\": \""+strDate+"\", \"nextpromotionupdate\": " + endDate.getTime() + ", \"PromotionSoList\":" + promoJson + ", \"PromotionDiscountPercent\": " + percent
							+ ", \"PriceAfterPromotion\": " + priceAfterPromotion + ", \"PromotionIdsBankem\": \""
							+ bankemIDs + "\"}";
				}
				
				return ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)//
						.updateObject(indexDB, esKeyTerm, json);
//						.UpdateFieldOBject(indexDB, esKeyTerm, "PromotionSoList", solist);

			} catch (Exception e) {
				Logs.WriteLine("Exception Index promotion status to ES FAILED: " + ProductID);
				e.printStackTrace();
				Utils.Sleep(500);
			}
		}

		// }else {}

		return false;
	}

	public ProductErpPriceBO GetDefaultPriceFromCache(int productID, int siteid, int ProvinceID, int priceArea,
			String Lang) throws Throwable {

		String key = "XXGetDefaultPriceFromCache" + productID + "_" + ProvinceID + "_" + siteid + "" + priceArea + "_"
				+ Lang;

		var rs = (ProductErpPriceBO) CacheStaticHelper.GetFromCache(key, 15);
		if (rs == null) {
			rs = productHelper.getPriceHelper().getDefaultPriceStrings((int) productID, siteid, ProvinceID, priceArea,
					Lang);
			CacheStaticHelper.AddToCache(key, rs);
		}

		return rs;

	}

//	public void PushBanKem(PromotionBanKemBO item ,MessageQueue  message,boolean isLog)
//			throws Throwable {
//
//		 
//		MessageQueue messageRepushV2 = new MessageQueue();
//		messageRepushV2.SqlList = new ArrayList<SqlInfo>();
//		messageRepushV2.SiteID = message.SiteID;  
//		SqlInfo sqlinfo0 = new SqlInfo();
//		 sqlinfo0.Sql = "update promotionbankem  set ispercentdiscountsalelist=:ispercentdiscountsalelist,productids=:productids ,promotionid=:promotionid,promotionpricelist=:promotionpricelist,quantities=:quantities upsert where promotionid="
//				+ item.PromotionID + "";
//		 
//		 
//		 
//		sqlinfo0.tablename = "promotionbankem";
//		sqlinfo0.tablekey = "promotionid";
//		sqlinfo0.Params = new HashMap<String, Object>();
//		sqlinfo0.Params.put("promotionid", item.PromotionID);
//		sqlinfo0.Params.put("productids", item.ProductIds);
//		sqlinfo0.Params.put("promotionpricelist", item.PROMOTIONPRICELIST);
//		sqlinfo0.Params.put("quantities", item.Quantities);
//		 
//
//		messageRepushV2.SqlList.add(sqlinfo0);
//
//		int ieV2 =0;
//		String quV2 = "gr.dc4.sqlpro" + ieV2;
//		String qu2V2 = "gr.dc4.sqlpro" + ieV2;
//		String qubkV2 = "gr.dc2.sqlpro" + ieV2;
//		String qudevV2 = "gr.beta.sqlpro";
//		 
//
//		messageRepushV2.Action = DataAction.Update;
//		messageRepushV2.ClassName = "ms.upsert.Upsert";
//		messageRepushV2.CreatedDate = Utils.GetCurrentDate();
//		messageRepushV2.Lang = message.Lang;
//		messageRepushV2.SiteID = message.SiteID;
//		messageRepushV2.Source = "PRO";
//		messageRepushV2.RefIdentify = message.Identify;
//		messageRepushV2.Identify =String.valueOf( item.PromotionID);
//		messageRepushV2.Hash = 0;
//		messageRepushV2.Note = message.Note;
//
//		messageRepushV2.DataCenter = message.DataCenter;
//		QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(quV2, qu2V2, qubkV2, qudevV2, messageRepushV2, isLog,
//				message.Note, 0); // 0 bom 2 cái
//		Logs.Log(isLog, message.Note, "Push promotionbankem " + item.PromotionID);
//	}
	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage rsmsg = new ResultMessage();
		rsmsg.Code = ResultCode.Success;
		DataCenter = 0;// message.DataCenter;
		String refixFilename = "";

		try {
			String[] messIdentify = message.Identify.split("\\|");
			if (messIdentify.length < 2) {
				Logs.WriteLine(messIdentify + ":" + messIdentify.length);
				return rsmsg;
			}
//DC 2 ngung, DC 4 lay va bom adta qua 2 ben
//			if (message.DataCenter == 2) {
//				Logs.LogRefresh("promotionreturndc", message, "message.DataCenter == 2");
//				return rsmsg;
//			}
			String strNOTE = message.Note + "";

			boolean isLog = false;
			if (strNOTE.contains("DIDX_TOP_LOG") || strNOTE.contains("DIDX_LOG")) {

				isLog = true;
			}
			if (strNOTE.contains("DIDX_TOP")) {

			 	notifyHelperLog.NotifyInfo("SERVICE-PROMOTIONV1:" + message.Identify, DataCenter);
			}
			if (strNOTE.contains("DIDX_RENOTI")) {
				refixFilename = "-renoti";
			}

//			if (strNOTE.contains("VERSION2")) {
//				return RefreshV2(message);
//			}
//			try {
//				  RefreshV2(message);
//			} catch (Throwable e) {
//				Logs.LogException(e);
//			}

			String productCode = messIdentify[0].trim();
			long productID = Utils.toLong(messIdentify[1]);
			if (productID == 9999)
				return rsmsg;
			String allLog = "";
			var timer = new CodeTimer("checktime");
			timer.reset("checkexist");
			if (!productHelper.CheckProductExistFromCache(productID)) {
				Logs.LogRefresh("promotionrefresh" + refixFilename, message, "CheckProductExistFromCache false");
				return rsmsg;
			}
			timer.end();

			int OutputTypeID = 0;
			int ProvinceID = -1;
			int InventoryStatusID = 1;
			int siteid = message.SiteID;
			String Lang = message.Lang;
//			int SalePrice = -1;// productHelper.getExactPrice(productCode, siteid, siteid == 6 ? 163 : 3,
			// Lang);

			if (Utils.StringIsEmpty(Lang)) {
				Lang = DidxHelper.getLangBySiteID(siteid);

			}

			// chỉ lấy saleprice cho các site tgdđ. bigphone
//			var price = productHelper.getExactPrice(productID, siteid, siteid == 6 ? 163 : 3, Lang);
			timer.reset("price");
			int priceArea = DidxHelper.getDefaultPriceAreaBySiteID(siteid, Lang);
			// var price = productHelper.getPriceHelper().getDefaultPriceStrings((int)
			// productID, siteid,
			// DidxHelper.getDefaultProvinceIDBySiteID(siteid), priceArea, Lang);
			var price = GetDefaultPriceFromCache((int) productID, siteid,
					DidxHelper.getDefaultProvinceIDBySiteID(siteid), priceArea, Lang);

			double SalePrice = price != null ? price.Price : -1d;
			String defaultCode = price != null ? price.ProductCode : null;

			int BrandID = DidxHelper.getBrandBySite(message.SiteID, Lang);

			timer.end();

			if (SalePrice <= 0) {
//				return rsmsg;// khong co gia thi khong can lam gi
//				
//			
				Logs.LogRefresh("promotionrefresh" + refixFilename, message, "SalePrice <= 0  ");

			}
			// String arecordid = siteid + "_" + SalePrice + "_" + OutputTypeID + "_" +
			// InventoryStatusID;

			String arecordid = siteid + "_" + BrandID + "_" + "_-1_" + OutputTypeID + "_" + InventoryStatusID;
			// -1 la saleprice nhung gio ko de saleprice vao recordid nua

//			String arecordid11 = siteid + "_11_" + "_-1_" + OutputTypeID + "_" + InventoryStatusID;

			Logs.Log(isLog, strNOTE, "GetPromotionByPrdID " + arecordid);

//			MessageQueue messageRepush = new MessageQueue();
//			messageRepush.SqlList = new ArrayList<SqlInfo>();
//			messageRepush.SiteID = message.SiteID;
			// String recordidcommon = BrandID + "_-1_" + OutputTypeID + "_" +
			// InventoryStatusID + "_" + productCode;
			timer.reset("getproERP");
			mwg.wb.model.promotion.Promotion[] allList = new mwg.wb.model.promotion.Promotion[] {};
			mwg.wb.model.promotion.Promotion[] lstpromotion = null;
			try {
				lstpromotion = erpHelper.GetPromotionByPrdID(productCode, ProvinceID, OutputTypeID, SalePrice,
						siteid == 6 ? 11 : BrandID);
				for (var x : lstpromotion) {
					x.recordid = arecordid;
					x.BrandID = BrandID;
					
					// lấy hình quà tặng
					if(!x.GroupID.equalsIgnoreCase("bankem") && ( !Utils.StringIsEmpty(x.ProductIDref) || !Utils.StringIsEmpty(x.ProductIDRefList) )
							/* &&  (productID == 234613 || DidxHelper.isBeta() || DidxHelper.isLocal()) */
					)
					{
						
						if(!x.GroupID.equalsIgnoreCase("webnote") && Utils.isProductInvalid(x.ProductIDref) && Integer.parseInt(x.ProductIDref) > 0 ) 
						{
							var cc = x.ProductIDref;

							var producttmp = productHelper.GetSimpleProductListByListID_PriceStrings_soMap(new int[] { Integer.parseInt(x.ProductIDref) }, siteid, 3, Lang);
							
							if(producttmp != null && producttmp.length > 0)
							{
								if(producttmp.length > 1){
									producttmp = Stream.of(producttmp).filter(z -> z!= null).sorted((f1,f2) -> Integer.compare(f2.ProductErpPriceBO.TotalQuantity ,f1.ProductErpPriceBO.TotalQuantity)).toArray(ProductBO[]::new);
									//producttmp = Stream.of(producttmp).filter(z -> z!= null).sorted(Comparator.comparingInt(zz -> -zz.ProductErpPriceBO.Quantity)).toArray(ProductBO[]::new);
								}
								var tmp = producttmp[0];
								if(tmp != null) {
									var listImage = new ArrayList<ProductImagePromotion>();
									listImage.add(new ProductImagePromotion() {{ 
										ProductID = tmp.ProductID+""; 
										ImageUrl = tmp.ProductLanguageBO != null && !Utils.StringIsEmpty(tmp.ProductLanguageBO.bimage) ? tmp.ProductLanguageBO.bimageurl : "";
										ProductName = tmp.ProductName;
									}});

									if(listImage != null && listImage.size() > 0) {
										x.ProductImage = mapper.writeValueAsString(listImage);
									}
								}
							}
						} else {
//							if(x.PromotionID == 417217) {
//								var debug = 1;
//							}
							String[] lstproduct  = new String[] {};
							if(x.GroupID.equalsIgnoreCase("webnote"))
							{
								if(!Utils.StringIsEmpty(x.ProductIDRefList))
								{
									lstproduct = x.ProductIDRefList.split(",");
								}
								
							} else {
								if(!Utils.StringIsEmpty(x.ProductIDref))
								{
									lstproduct = x.ProductIDref.split("\\|");
								}
								
							}
							if(lstproduct != null && lstproduct.length > 0) {
								var tmplistID = new ArrayList<Integer>();
								for (String item : lstproduct) {
									if(Utils.isProductInvalid(item) && Integer.parseInt(item) > 0) {
										tmplistID.add(Integer.parseInt(item));
									}
								}
								if(tmplistID != null && tmplistID.size() > 0) {
									
									var producttmp = productHelper.GetSimpleProductListByListID_PriceStrings_soMap(tmplistID.stream().mapToInt(Integer::intValue).toArray(), siteid, 3, Lang);
									if(producttmp != null && producttmp.length > 0) {
										producttmp = Stream.of(producttmp).sorted(Comparator.comparing(z -> -(z.ProductErpPriceBO != null ? z.ProductErpPriceBO.TotalQuantity : 0) ) )
												//.collect(Collectors.toList()).toArray();
												
												.toArray(ProductBO[]::new);
										
										var listImage = new ArrayList<ProductImagePromotion>();
										
										for (var tmp : producttmp) {
											if(tmp != null) {
												listImage.add(new ProductImagePromotion() {{ 
													ProductID = tmp.ProductID+""; 
													ImageUrl = tmp.ProductLanguageBO != null && !Utils.StringIsEmpty(tmp.ProductLanguageBO.bimage) ? tmp.ProductLanguageBO.bimageurl : "";
													ProductName = tmp.ProductName;
													}});
												//listImage.put(tmp.ProductID, tmp.ProductLanguageBO != null ? tmp.ProductLanguageBO.bimageurl : "");
											}
										}
										if(listImage != null && listImage.size() > 0) {
											x.ProductImage = mapper.writeValueAsString(listImage);
										}
									}
								}// tmplistID
							}
						}
						
					}
				}
				// promotion v2
//				Logs.Log(isLog, strNOTE, "v2: get from WSPromotion " + arecordid);
//				var promov2 = erpPromotionHelper.GetPromotionByPrdID(productCode, ProvinceID, OutputTypeID, 1,
//						SalePrice, BrandID);
//				for (var x : promov2) {
//					x.recordid = arecordid;
//					x.BrandID = BrandID;
//					x.v2 = true;
//				}
//				lstpromotion = (Promotion[]) ArrayUtils.addAll(lstpromotion, promov2);

//				List<Promotion> filtered = new ArrayList<>();
//
//				var promap = Stream.of(lstpromotion).collect(Collectors
//						.groupingBy(x -> x.PromotionID + "_" + x.GroupID + "_" + x.ProductName, Collectors.toList()));
//
//				for (var x : promap.values()) {
//					if (!x.isEmpty()) {
//
//						var y = x.get(0);
//						y.provinceIDs = x.stream().map(z -> z.ProvinceId).collect(Collectors.toList());
//						filtered.add(y);
//					}
//				}
//
//				lstpromotion = filtered.toArray(Promotion[]::new);

			} catch (Throwable e) {
				rsmsg.Message = "ERP returned null";
				rsmsg.StackTrace = Utils.stackTraceToString(e);
//				e.printStackTrace();
				Logs.LogException(e);
				if (message.DataCenter == 3) {
					rsmsg.Code = ResultCode.Success;
				} else {
					rsmsg.Code = ResultCode.Retry;
				}
				Logs.LogRefresh("promotionrefresh" + refixFilename, message, "ERP returned null", siteid);
				return rsmsg;
			}
			timer.end();

			// filter km ban kem
			timer.reset();
			for (var item : lstpromotion) {
				if (item != null) {
					String provs = item.provinceIDList;
					if (!Strings.isNullOrEmpty(provs)) {
						item.provinceIDs = Stream.of(provs.split(",")).map(x -> {
							try {
								return Integer.parseInt(x.trim());
							} catch (Exception e) {
								return -1;
							}
						}).filter(x -> x > 0).collect(Collectors.toList());
					}
				}
			}
			if (lstpromotion != null && lstpromotion.length > 0) {
				if (lstpromotion.length > 25) {
					Logs.LogRefresh("promotionerplength" + refixFilename, message, "" + lstpromotion.length, siteid);
				}
//				if (siteid == 1 || siteid == 2) {
//					var listProExceptBanKem = Arrays.asList(lstpromotion).stream()
//							.filter(x -> x != null && !x.GroupID.toLowerCase().equals("bankem"))
//							.collect(Collectors.toList());
//
//					var listProBanKemHcm = Arrays.asList(lstpromotion).stream().filter(
//							x -> x != null && x.GroupID.toLowerCase().equals("bankem") && x.provinceIDApplied(3))
//							.collect(Collectors.toList());
//
//					listProExceptBanKem.addAll(listProBanKemHcm);
//					lstpromotion = listProExceptBanKem.toArray(Promotion[]::new);
//
//				}
			}
			timer.end();
			allLog = allLog + ",bankem:" + timer.getElapsedTime();
			int ProductIDX = (int) productHelper.GetProductIDByProductCodeFromCache(productCode);
//			if (ProductIDX <= 0) {
//				Logs.LogException("promotion_productnofoundfromcode.txt", message.Identify + "," + productCode);
//				rsmsg.Code = ResultCode.Success;
//				return rsmsg;
//
//			}
			if (ProductIDX <= 0) {
				Logs.LogRefresh("promotionrefresh" + refixFilename, message, "ProductIDX <= 0", siteid);
			}
			ProductBO productBO = null;// productHelper.GetProductBOByProductIDSEFromCache(ProductIDX, siteid, Lang);
			List<mwg.wb.model.promotion.Promotion> listTempPromotion = new ArrayList<mwg.wb.model.promotion.Promotion>();

			if (lstpromotion != null && lstpromotion.length > 0) {
				Logs.LogRefresh("promotionrefresh" + refixFilename, message,
						"lstpromotion.length:" + lstpromotion.length);
				for (mwg.wb.model.promotion.Promotion item : lstpromotion) {
					if (item != null) {
						// double pid = Utils.toDouble(item.ProductIDref);
						// if(GConfig.ProductTaoLao.containsKey(pid)) continue;
						if (siteid == 6

								&& !item.provinceIDList.contains("163") && !item.provinceIDList.contains("164")
								&& !item.provinceIDList.contains("165")) {
							continue;
						}
						String recordid = item.recordid + "_" + item.ProvinceId + "_" + productCode + "_"
								+ item.PromotionID;
						// System.out.println("process promotion" + recordid + "->" +
						// item.PromotionListGroupName);
						if (item.ProductId != null)
							recordid += "_" + item.ProductId.trim();// th km vua giam tien, vua tang qua (productcodes)

						item.OutputTypeID = OutputTypeID;
						item.SalePrice = (int) SalePrice;
						item.SiteID = siteid;
						item.ProductCode = productCode;
						item.recordid = recordid;
//						item.BrandID = BrandID;
						item.didxupdateddate = Utils.GetCurrentDate();
						item.IsDeleted = 0;
						item.LangID = Lang;
//						item.recordidcommon = recordidcommon;

						if (siteid != 12) {

							if (productBO == null) {
								timer.reset("getproductboidse");
								productBO = productHelper.GetProductBOByProductIDSEFromCache(ProductIDX, siteid, Lang);
								timer.end();

							}
							if (productBO != null) {
								long t1 = System.currentTimeMillis();
								timer.reset("processpromotion2");
								productHelper.getHelperBySite(siteid).processPromotion2(item, productBO, siteid, Lang,
										InventoryStatusID, erpHelper, listTempPromotion);
								timer.end();
//Tong hop lai databan kem luu cho khac
								if (!Utils.StringIsEmpty(item.Quantities)) {
//								item.ProductIds = StringUtils.strip(productidref, "|");
//								item.ISPERCENTDISCOUNTSALELIST = StringUtils.strip(percent, "|");
//								item.PROMOTIONPRICELIST = StringUtils.strip(promotionPrice, "|");
//								item.Quantities = StringUtils.strip(quantities, "|");
								}

								long deta = System.currentTimeMillis() - t1;
								if (deta > 200) {

									String msg = "\r\nprocessPromotion2 siteid=" + siteid + ",ProductIDX=" + ProductIDX
											+ ",productCode=" + productCode;
									Logs.LogFactorySlowMessage(msg, deta);

								}
							}
						}

//						SqlInfo sqlinfo = new SqlInfo();
//
//						RefSql ref = new RefSql();
//						Utils.BuildSql(isLog, strNOTE, "product_promotion", "recordid", item.recordid, item, ref);
//						sqlinfo.Sql = ref.Sql;
//						sqlinfo.Params = ref.params;
//						messageRepush.SqlList.add(sqlinfo);

//						var code = item.ProductCode.trim();
						/*
						 * String cmd2 =
						 * "create edge e_code_promotion  from (select from pm_product where productid='"
						 * + code + "')   to(select from  product_promotion where recordid ='" +
						 * item.recordid + "' and in('e_code_promotion')[productid='" + code +
						 * "'].size() = 0)"; SqlInfo sqlinfoEdge = new SqlInfo(); sqlinfoEdge.Sql =
						 * cmd2; messageRepush.SqlList.add(sqlinfoEdge);
						 */

//						SqlInfo sqlinfoEgde1 = new SqlInfo();
//						sqlinfoEgde1.Params = new HashMap<String, Object>();
//						sqlinfoEgde1.Type = SqlInfoType.CREATE_EDGE_BY_NODE;
//						sqlinfoEgde1.Params.put("edge", "e_code_promotion");
//						sqlinfoEgde1.Params.put("from", code);
//						sqlinfoEgde1.Params.put("to", item.recordid);
//						messageRepush.SqlList.add(sqlinfoEgde1);

						listTempPromotion.add(item);

					}

				}

			} else {
				Logs.WriteLine("n stpromotion.length<=0");
				Logs.LogRefresh("promotionrefresh" + refixFilename, message, "stpromotion.length<=0", siteid);
			}
			if (strNOTE.contains("DIDX_TOP")) {

				 notifyHelperLog.NotifyInfo("SERVICE-PROMOTIONV1-TIMER:" + timer.getLogs(), DataCenter);
			}

			// chỉ index promotion cũ lên ES
			allList = Stream.of(lstpromotion).filter(x -> !x.v2).toArray(Promotion[]::new);

			long ProductID = productHelper.GetProductIDByProductCodeFromCache(productCode);
			if (ProductID > 0) {
//				String dfcode = productHelper.getDefaultCodeFromCache(ProductID, message.SiteID,
//						message.SiteID == 6 ? 163 : 3);
				if (defaultCode != null && defaultCode.equals(productCode)) {
					boolean rs = IndexDataES(allList, isLog, strNOTE, ProductID, message.SiteID, message.Lang,
							message.DataCenter, SalePrice);

					if (!rs) {

						rsmsg.Code = ResultCode.Retry;
						Logs.WriteLine("IndexDataES false  ");
						Logs.Log(isLog, strNOTE, "IndexDataES false ");
						return rsmsg;

					}
				}
				// int ie = Utils.GetQueueNum(ProductID);
//				String qu = "gr.dc4.sql.promotion";
//				String qu2 = "gr.dc4.sql.promotion";
//				String qubk = "gr.dc2.sql.promotion";
//				String qudev = "gr.beta.sql.promotion";

//				int ie = Utils.GetQueueNum10(ProductID);
//				String qu = "gr.dc4.sql" + ie;
//				String qu2 = "gr.dc4.sql" + ie;
//				String qubk = "gr.dc2.sql" + ie;
//				String qudev = "gr.beta.sql";

//				messageRepush.Source = "PROMOTION";
//				messageRepush.RefIdentify = message.Identify;
//				messageRepush.Action = DataAction.Update;
//				messageRepush.ClassName = "ms.upsert.Upsert";
//				messageRepush.CreatedDate = Utils.GetCurrentDate();
//				messageRepush.Lang = message.Lang;
//				messageRepush.SiteID = message.SiteID;
//				messageRepush.Identify = String.valueOf(ProductID);
//				messageRepush.Hash = ProductID;
//				messageRepush.Note = strNOTE;
//				messageRepush.DataCenter = message.DataCenter;
//				Logs.LogRefresh("promotionrefresh", message, "messageRepush" + messageRepush.SqlList.size());
//				try {
//					QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush,
//							isLog, strNOTE, 0);
//				} catch (Exception e) {
//					Logs.LogException(e);
//					rsmsg.Code = ResultCode.Retry;
//					rsmsg.StackTrace = Utils.stackTraceToString(e);
//					Logs.Log(isLog, strNOTE, "push promotion  " + e.getMessage());
//					return rsmsg;
//				}

				// VER2

				// tách ra làm 2
//				Map<String, List<Promotion>> versionMap = listTempPromotion.stream()
//						.collect(Collectors.groupingBy(x -> x.v2 ? "v2" : "v1"));
//				var v1 = versionMap.containsKey("v1") ? versionMap.get("v1") : new ArrayList<Promotion>();
//				var v2 = versionMap.containsKey("v2") ? versionMap.get("v2") : new ArrayList<Promotion>();

//				String lstPromoJson = mapper.writeValueAsString(listTempPromotion);
//				String lstPromoJsonv2 = mapper.writeValueAsString(v2);
//				if (Strings.isNullOrEmpty(lstPromoJsonv2)) {
//					lstPromoJsonv2 = "[]";
//				}
				if (listTempPromotion != null) {
					MessageQueue messageRepushV2 = new MessageQueue();
					messageRepushV2.SqlList = new ArrayList<SqlInfo>();
					messageRepushV2.SiteID = message.SiteID;
					String recordidcommonV2 = siteid + "_" + BrandID + "_0_" + OutputTypeID + "_" + InventoryStatusID
							+ "_" + productCode;
					String Data = mapper.writeValueAsString(listTempPromotion);
					PromotionGRBO item1 = new PromotionGRBO();
					item1.Data = Data;
//					item1.Datav2 = lstPromoJsonv2;
					item1.OutputTypeID = OutputTypeID;
					item1.SalePrice = SalePrice;
					item1.SiteID = siteid;
					item1.ProductCode = productCode;
					item1.recordid = recordidcommonV2;
					item1.BrandID = BrandID;
					item1.didxupdateddate = Utils.GetCurrentDate();
					item1.IsDeleted = 0;
					item1.LangID = Lang;
					item1.InventoryStatusID = InventoryStatusID;
					SqlInfo sqlinfo1 = new SqlInfo();
					RefSql ref1 = new RefSql();
					Utils.BuildSql(isLog, strNOTE, "productpromotion", "recordid", recordidcommonV2, item1, ",datav2,",
							ref1);
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

					if(messIdentify != null && messIdentify.length > 0){
						messageRepushV2.RefIdentify = messIdentify[0];
					}else{
						messageRepushV2.RefIdentify = message.Identify;
					}

					if (SalePrice > 0) {
						messageRepushV2.CachedType = 1;
					}
					messageRepushV2.Action = DataAction.Update;
					messageRepushV2.ClassName = "ms.upsert.Upsert";
					messageRepushV2.CreatedDate = Utils.GetCurrentDate();
					messageRepushV2.Lang = message.Lang;
					messageRepushV2.SiteID = siteid;
					messageRepushV2.Identify = String.valueOf(ProductID);
					messageRepushV2.Hash = ProductID;
					messageRepushV2.Note = strNOTE;
					messageRepushV2.Type = MessageQueuePushType.REQUIRE_PUSH_RUN_STATUS_FROM_PROMOTION;
					messageRepushV2.DataCenter = message.DataCenter;
					Logs.LogRefresh("promotionrefresh" + refixFilename, message, " " + messageRepushV2.SqlList.size(),
							siteid);
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
						Logs.LogException(e);
						rsmsg.Code = ResultCode.Retry;
						rsmsg.StackTrace = Utils.stackTraceToString(e);
						Logs.Log(isLog, strNOTE, "push promotion  " + e.getMessage());
						return rsmsg;
					}

				}
				// if (clientConfig.DATACENTER == 3) {
				var bklist = listTempPromotion.stream().filter(x -> x.GroupID
						.equalsIgnoreCase("bankem"))
						.toArray(Promotion[]::new);
				if (bklist != null) {
					for (var bk : bklist) {
						String bkID = bk.PromotionID + "";
//						Long processed = processedBK.get(bkID);
//						if (clientConfig.DATACENTER != 3 && processed != null
//								&& System.currentTimeMillis() - processed < 21600000L) {
//							continue;
//						}

						MessageQueue nwmsg = new MessageQueue();
						String qu = "gr.dc4.didx.bankem";
						String qubeta = "gr.beta.didx.bankem";
						String qubk = "gr.dc2.didx.bankem";

						nwmsg.Action = DataAction.Update;
						nwmsg.ClassName = "mwg.wb.pkg.promotion.PromotionBanKem";
						nwmsg.CreatedDate = Utils.GetCurrentDate();
						nwmsg.Lang = message.Lang;
						nwmsg.Note = message.Note;
						nwmsg.SiteID = message.SiteID;
						nwmsg.DataCenter = 0;
						nwmsg.Identify = bk.PromotionID + "";
						nwmsg.Data = bk.BeginDate.getTime() + "|" + bk.EndDate.getTime() + "|" + bk.PromotionListGroupID
								+ "|" + bk.PromotionListGroupName;
						nwmsg.Source = "PromotionV2 ";
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu, qubk, qubeta, nwmsg,
								isLog, strNOTE, 0);
//						processedBK.put(bkID, System.currentTimeMillis());
					}
				}
				// }
			}
			rsmsg.Code = ResultCode.Success;
		} catch (Throwable e) {
			rsmsg.StackTrace = Utils.stackTraceToString(e);
			rsmsg.Code = ResultCode.Retry;
			Logs.LogException(e);
		}
		return rsmsg;

	}

	@Override
	public ResultMessage RunScheduleTask() {
		return null;
	}

}

class ProductImagePromotion{
	public String ProductID;
	public String ImageUrl;
	public String ProductName;
}
