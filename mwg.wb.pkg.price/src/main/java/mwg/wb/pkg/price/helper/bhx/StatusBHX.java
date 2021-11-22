package mwg.wb.pkg.price.helper.bhx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.business.InStockBO;
import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.helper.BHXStoreHelper;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.redis.RedisCluster;
import mwg.wb.client.service.CodeTimer;
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
import mwg.wb.common.SqlInfoType;
import mwg.wb.common.Utils;
import mwg.wb.common.notify.LineNotify;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.pricedefault.PriceDefaultBO;
import mwg.wb.model.pricedefault.PriceDefaultGRBO;
import mwg.wb.model.pricestrings.PriceStringBO;
import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.model.products.ProductItemExchange;
import mwg.wb.pkg.price.helper.PriceSatusHelper;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class StatusBHX implements Ididx {
	private String indexDB = "";
	private String typeDB = "product";

	private ORThreadLocal factoryWrite = null;
	private ORThreadLocal factoryRead = null;
	private PriceHelper priceHelper = null;
	private ProductHelper productHelper = null;
	private ObjectMapper mapper = null;
	private ErpHelper erpHelper = null;
	private ClientConfig clientConfig = null;
	private int DataCenter = 0;
	private BHXStoreHelper bhxStoreHelper = null;
	private LineNotify notifyHelperLog = null;
	private RedisCluster redisCluster = null;
		
	//private CrmServiceHelper crmServiceHelper = null;

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {

		factoryWrite = (ORThreadLocal) objectTransfer.factoryWrite;
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		priceHelper = (PriceHelper) objectTransfer.priceHelper;
		mapper = (ObjectMapper) objectTransfer.mapper;
		erpHelper = (ErpHelper) objectTransfer.erpHelper;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		productHelper = (ProductHelper) objectTransfer.productHelper;
		indexDB = clientConfig.ELASTICSEARCH_PRODUCT_INDEX;
		bhxStoreHelper = (BHXStoreHelper) objectTransfer.bHXStoreHelper;
		//crmServiceHelper = (CrmServiceHelper) objectTransfer.crmHelper;
		notifyHelperLog = (LineNotify) objectTransfer.notifyHelperLog;
		redisCluster = (RedisCluster)objectTransfer.redisCluster;
	}

	public static Map<String, String> g_listWebStatusId = new HashMap<String, String>();
	public static Map<Long, List<ProductErpPriceBO>> g_listPrice = new HashMap<Long, List<ProductErpPriceBO>>();
	public static Map<String, String> g_listES = new HashMap<String, String>();
	
	
	public ResultMessage Refresh(MessageQueue message) {
			DataCenter = message.DataCenter;
			ResultMessage msg = new ResultMessage();
			msg.Code = ResultCode.Success;
			
			try {
				String strNOTE = message.Note;
				long ProductID = Utils.ToLong(message.Identify);
				
				boolean isLog = strNOTE.contains("DIDX_TOP_LOG") || strNOTE.contains("DIDX_LOG") ? true : false;
				
				var timer = new CodeTimer("checktime");
				
				if (strNOTE.contains("DIDX_TOP")) {
					notifyHelperLog.NotifyInfo("SERVICE-STATUSBHX:" + message.Identify, DataCenter);
				}
				
				Logs.getInstance().Log(isLog, strNOTE, "refresh status BHX", message);
				
				if (!productHelper.CheckProductExistFromCache(ProductID)) {
					Logs.Log(isLog, strNOTE, "CheckProductExistFromCache " + ProductID);
					return msg;
				}
				if ((ProductID <= 0)) {
					Logs.Log(isLog, strNOTE, "ProductID <= 0" + ProductID);
					return msg;
				}


				int siteID = message.SiteID;
				int priceArea = 644;
				String lang = "vi-VN";
				
				if(Utils.StringIsEmpty(message.Lang))
					lang = message.Lang;
				if (siteID != 11 || Utils.StringIsEmpty(lang)) {
					Logs.Log(isLog, strNOTE, "SiteID <= 0 || BrandID<=0  " + ProductID);
					return msg;
				}
				
				//lấy product code
				final var inputProductCode = productHelper.GetCodeByProductID(ProductID);
				if(inputProductCode == null || inputProductCode.equals(""))
				{
					Logs.Log(isLog, strNOTE, "ko co code nao map voi  " + ProductID);
					return msg;
				}
				//lấy code map (hiện tại bhx chưa có vụ 1 product nhiều code)
//				var productCodeMaster = productCode;
				timer.reset("GetItemExchangeByCodeBHX");
				//lấy tỉ lệ đổi theo code sản phẩm
				var lstExchangeRates = priceHelper.GetItemExchangeByCodeBHX(inputProductCode);
				
				timer.end();
				
				
				if(lstExchangeRates == null || lstExchangeRates.isEmpty())
				{
					Logs.Log(isLog, strNOTE, "ko co code nao map voi  " + ProductID);
					return msg;
				}
				
				Logs.Log(isLog, strNOTE, "Refresh Pricestatus..."+ ProductID + " PriceArea " + priceArea + " SiteID" + siteID);			
				
				//-----tính toán tồn của sản phẩm cơ sở
				
				//chỉ xử lý cho kho đó
				final int[] storeids = message.Storeid > 0 ? new int[] {message.Storeid} : bhxStoreHelper.getAllStore(); 

				//lấy code sản phẩm cơ sở
//				var baseCode = lstExchangeRates.get(0).unitproductcode;
				//dictionary để lưu tạm tồn theo kho của sp cơ sở
				HashMap<Integer, Integer> dicBaseStoreStock = new HashMap<Integer, Integer>();
		
				//đẩy base productcode lên trên đầu
				
				lstExchangeRates.sort((o1, o2) -> o1.getExchangequantity().compareTo( o2.getExchangequantity()));	
//				timer.reset("getStockQuantityBhx-");
				var listAllStock = productHelper.getNewListStockQuantityBhx(lstExchangeRates.get(0), storeids, dicBaseStoreStock);				
//				timer.end();
//				System.out.println(timer.getElapsedTime());
				
				String baseCode = lstExchangeRates.get(0).unitproductcode;
				for(ProductItemExchange productItemExchange : lstExchangeRates) {
					var exchangeProductCode = productItemExchange.productcode;
					var productId = productHelper.GetProductIDByProductCodeFromCache(exchangeProductCode);
					if(productId > 0)
					{
						String unit = "";
						if(!exchangeProductCode.equals(baseCode)) {
							unit = productHelper.getUnitQuantity(exchangeProductCode, 6463);
						}
						//lấy list prices
						List<ProductErpPriceBO> lstPrices = priceHelper.getListPriceStrings(productId, priceArea, siteID);
						if (lstPrices == null || lstPrices.isEmpty()) { 
							Logs.Log(isLog, strNOTE, productId + " lstPrices.size <0");
							continue;
						}
						//chỉ lấy giá của kho truyền vào
						lstPrices = lstPrices.stream()
								.filter(priceBO -> contains(storeids, priceBO.StoreID))
								.collect(Collectors.toList());					

//						var listAllStock = productHelper.getNewListStockQuantityBhx(productItemExchange, storeids, dicBaseStoreStock);

						if (strNOTE.contains("DIDX_TOP")) {
							notifyHelperLog.NotifyInfo("SERVICE-STATUSBHX-TIMER:" + timer.getLogs(), DataCenter);
						}
						Logs.Log(isLog, strNOTE, "ProductErpPriceBO item : lstPrices");
						
//						List<InStockBO> listStock = listAllStock.stream()
//								.filter(item -> item.productcode.equals(priceBO.ProductCode.trim()))
//								.collect(Collectors.toList());
						
						int intInstockInCountry = 0;
						if (!listAllStock.isEmpty()) {
							intInstockInCountry = listAllStock.stream().mapToInt(c -> c.quantity).sum();
						}
						
						List<ProductErpPriceBO> listFinalPrices = new ArrayList<ProductErpPriceBO>();
						for (ProductErpPriceBO priceBO : lstPrices) {
							//check lại 1 lần nữa item lấy lên có đúng code trong message
							if (((priceBO != null) && (priceBO.ProductId != productId))) {
								Logs.Log(isLog, strNOTE, "ProductID khong khop");
								continue;
								//return msg;
							}

							//lọc theo code

							InStockBO objInstock = listAllStock.stream()
									.filter(stock -> stock.storeidlist.indexOf(priceBO.StoreID) != -1)
									.limit(1)
									.findFirst()
									.orElse(null);															

							ProductErpPriceBO objPriceNews = priceBO;
							objPriceNews.TotalQuantity = intInstockInCountry;
							objPriceNews.ProductCodeTotalQuantity = intInstockInCountry;
							
							objPriceNews.ProductArrivalDate = new Date(0);
							
							if ((objInstock == null)) {
								objPriceNews.Quantity = 0;
								objPriceNews.QuantityNew = 0;
							} else {
								objPriceNews.WebMinStock = objInstock.webminstock;
								
								// dang la ProductCodeQuantity
								if(!exchangeProductCode.equals(baseCode)) {								
										var stockbase = objInstock.quantitynew;
										if(Objects.nonNull(unit) && !unit.isBlank()) {
											objPriceNews.QuantityUnit = unit;
										}else {
											objPriceNews.QuantityUnit = productHelper.getUnitQuantity(exchangeProductCode, objPriceNews.StoreID);
										}
										objPriceNews.Quantity =(int)(stockbase/productItemExchange.exchangequantity);	
										objPriceNews.QuantityNew =(stockbase/productItemExchange.exchangequantity);
										if(objPriceNews.QuantityUnit != null && objPriceNews.QuantityUnit.equalsIgnoreCase("thùng") 
												&& objInstock.quantitynew > 0) {
											float m = (stockbase % productItemExchange.exchangequantity);
											if(m == 0 ) {
												objPriceNews.Quantity--;
												objPriceNews.QuantityNew--;
											}
										}
										objPriceNews.ProductCodeQuantity = objPriceNews.Quantity;
										
										
								} else {
									objPriceNews.Quantity = objInstock.quantity;
									objPriceNews.QuantityNew = objInstock.quantitynew;
									objPriceNews.ProductCodeQuantity = objInstock.quantity;
									objPriceNews.QuantityUnit = objInstock.quantityUnit;
									objPriceNews.IsBaseUnit = true;
								}
								objPriceNews.ProductCodeTotalQuantity = intInstockInCountry;
								
								objPriceNews.CenterQuantity = objInstock.centerquantity;
								
								objPriceNews.ArrivalDate = objInstock.ExpireddateInStore;
								objPriceNews.ProductArrivalDate = objInstock.ExpiredDate;
							}
										
							objPriceNews = BhxPriceStatusHelper.ProcessProductStatus(objPriceNews, false);
							listFinalPrices.add(objPriceNews);
						}
						
						if ((listFinalPrices.size() <= 0)) {				
							Logs.WriteLine("listFinalPrices.size <0");
							continue;
							//return msg;
						}

						List<ProductErpPriceBO> finalListPriceDefault = new ArrayList<ProductErpPriceBO>();
						
//						List<Integer> store = listFinalPrices.stream().map(e -> e.StoreID).distinct()
//								.collect(Collectors.toList());
						
						String md5 = "";
						Logs.getInstance().Log(isLog, strNOTE, "listFinalPrices", listFinalPrices);
						for (int item : storeids) {
							
							List<ProductErpPriceBO> listPriceByStore = listFinalPrices.stream().filter(x -> x.StoreID == item)
									.collect(Collectors.toList());
//							
//							//lấy giá theo kho
							ProductErpPriceBO priceDefault = PriceSatusHelper.GetPriceDefault(listPriceByStore, siteID, lang);
//							ProductErpPriceBO priceDefault = PriceSatusHelper.GetPriceDefault(listFinalPrices, siteID, lang);
							if ((priceDefault == null)) {
								Logs.Log(isLog, strNOTE, "ProvinceID " + item + " priceDefault == null ");
								// Ng�ng kinh doanh
								priceDefault = new ProductErpPriceBO();
								priceDefault.Price = 0.0;
								priceDefault.WebStatusId = 1;
								priceDefault.ProductId = productId;
								priceDefault.ProvinceId = bhxStoreHelper.getProvinceByStore(item);
								priceDefault.StoreID = item;
							}
							
							finalListPriceDefault.add(priceDefault);
							//thêm storeid
							md5 = md5 + priceDefault.StoreID + "_" + priceDefault.ProvinceId + "_" + priceDefault.Price + "_" + priceDefault.WebStatusId
									+ priceDefault.IsShowHome;
						}
						Logs.Log(isLog, strNOTE, "===============================");
						Logs.getInstance().Log(isLog, strNOTE, "finalListPriceDefault object", finalListPriceDefault);
						boolean isUpdateES = true;
						// test lai
						String esKeyTerm = productId + "_" + siteID + "_" + (lang.toLowerCase().replaceAll("-", "_"));
						synchronized (StatusBHX.class) {

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

						Map<Integer, PriceDefaultBO> pricesListdetaultByProvince = new HashMap<Integer, PriceDefaultBO>();
						Map<String, Object> pricesMap = new HashMap<String, Object>();

						Logs.getInstance().Log(isLog, strNOTE, "finalListPriceDefault", finalListPriceDefault);
						//phải tính toán lại price default cho từng tỉnh
						for (ProductErpPriceBO item : finalListPriceDefault) {
							
//							var item = finalListPriceDefault.get(i);
							
							//index các price
							pricesMap.put("IsShowHome_" + item.ProvinceId + "_" + item.StoreID, item.IsShowHome ? 1 : 0);
							pricesMap.put("WebStatusId_" + item.ProvinceId+ "_" + item.StoreID, item.WebStatusId);
							pricesMap.put("Price_" + item.ProvinceId+ "_" + item.StoreID, item.Price);
							//pricesMap.put("ProductCode_" + item.ProvinceId+ "_" + item.StoreID, item.ProductCode);
							pricesMap.put("Stock_" + item.ProvinceId+ "_" + item.StoreID, item.QuantityNew);
							
							//chưa có key giá mặc định tồn thấp nhất thì tìm
							if(!pricesMap.containsKey("Stock_" + item.ProvinceId+ "_0"))
							{
													
								//tìm ra price theo tỉnh có tồn thấp nhất
								ProductErpPriceBO smallest = finalListPriceDefault
										.stream()
										.filter(s->s.ProvinceId == item.ProvinceId)
										.max(Comparator.comparing(ProductErpPriceBO::getQuantity))
										.orElse(new ProductErpPriceBO());
											
//								if(smallest.StoreID == item.StoreID)
//									defaultstore = true;
								var defaultstore = smallest.StoreID == item.StoreID;
								if(defaultstore) {
									
									//index price defaul theo tỉnh
									pricesMap.put("IsShowHome_" + item.ProvinceId + "_0", item.IsShowHome ? 1 : 0);
									pricesMap.put("WebStatusId_" + item.ProvinceId+ "_0", item.WebStatusId);
									pricesMap.put("Price_" + item.ProvinceId+ "_0", item.Price);
									//pricesMap.put("ProductCode_" + item.ProvinceId+ "_" + item.StoreID, item.ProductCode);
									pricesMap.put("Stock_" + item.ProvinceId+ "_0", item.QuantityNew);
																	
									PriceDefaultBO deF = new PriceDefaultBO();							
									deF.WebStatusId = item.WebStatusId;
									deF.ProductID = productId;
									deF.IsShowHome = item.IsShowHome ? 1 : 0;
									deF.ProductCode = item.ProductCode;
									deF.Price = item.Price;								
									deF.ProvinceId = item.ProvinceId;
									deF.SiteID = siteID;
									deF.RecordID = siteID + "_" + lang + "_" + productId + "_" + item.ProvinceId + "_" + "0";
									deF.LangID = lang;
									Logs.Log(isLog, strNOTE, "deF.ProvinceId " + deF.ProvinceId + "deF.Price " + deF.Price
											+ " deF.WebStatusId" + deF.WebStatusId);
									pricesListdetaultByProvince.put(item.ProvinceId, deF);
								}
							}
													
						}
						isUpdateES = true;
						if (isUpdateES == false) { 
							Logs.Log(isLog, strNOTE, "SKIP IndexDataListPriceDefault order ");
						} else {
							
							//xử lý lại object pricebhx						
							//Date createdate = priceHelper.GetCreatedDateFromCache(productId);
							int order = 0;		
							Logs.getInstance().Log(isLog, strNOTE, "pricesMap", pricesMap);
							boolean rsEs = IndexDataListPriceDefault_ES(message.ProvinceID, isLog, strNOTE, pricesMap, ProductID,
									siteID, lang, order);
							if (rsEs == false) {
								Logs.WriteLine("IndexDataListPriceDefault false");
								msg.Code = ResultCode.Retry;
								Logs.Log(isLog, strNOTE, "IndexDataListPriceDefault false ResultCode.Retry ");
//								continue;
								//return msg;
							}
							synchronized (StatusBHX.class) {
								if (!Utils.StringIsEmpty(md5)) {
									g_listWebStatusId.put(esKeyTerm, md5);
								}
							}
						}
						Logs.getInstance().Log(isLog, strNOTE, "pricesListdetaultByProvince ", pricesListdetaultByProvince);
						boolean rsOr = IndexDataListPriceDefault_OR(message.ProvinceID, isLog, strNOTE, pricesListdetaultByProvince,
								productId, siteID, lang);
						if (rsOr == false) {
							Logs.WriteLine("IndexDataListPriceDefault_OR false");
							msg.Code = ResultCode.Retry;
							Logs.Log(isLog, strNOTE, "IndexDataListPriceDefault_OR false ResultCode.Retry ");
							return msg;
						}
						Logs.Log(isLog, strNOTE, "Push sql default");

						Logs.getInstance().Log(isLog, strNOTE, "pricesListdetaultByProvince ", pricesListdetaultByProvince);
						Logs.Log(isLog, strNOTE, "rsOr = IndexDataListPriceDefault_OR=true");

						MessageQueue messageRepush = new MessageQueue();
						messageRepush.SqlList = new ArrayList<SqlInfo>();

//						for (ProductErpPriceBO item : listFinalPrices) {
//													 
//							ProductErpPriceTMP itemTmp = ConvertToProductErpTMP(item);
//							SqlInfo sqlinfo = new SqlInfo();
//							RefSql ref = new RefSql();
	//
//							Utils.BuildSql(isLog, strNOTE, "product_price", "recordid", item.RecordID, itemTmp, ref);
//							sqlinfo.Sql = ref.Sql;
//							sqlinfo.Params = ref.params;
//							messageRepush.SqlList.add(sqlinfo);
	//
//						}
						var codesMap = listFinalPrices.stream().collect(Collectors.groupingBy(x -> x.RecordID));
						
						//đẩy lên redis
						for(var entry: codesMap.entrySet()) {
							var prices = entry.getValue();
							for(var price : prices) {
								if(price == null)
									continue;
								var province = bhxStoreHelper.getProvinceByStore(price.StoreID);
								var key = redisCluster.createKeyRedisStock(price.ProductCode, province, price.StoreID);
								redisCluster.Set(key, Utils.toString(price.QuantityNew));
							}
						}
											
//						var ckey = "KEY_STOCK_STORE_PRODUCT_3_6463_1012835000287";
//						var t = redisCluster.Get(ckey);
//						System.out.println(t);
						
						
						for (var entry : codesMap.entrySet()) {
							var recordID = entry.getKey();
							var prices = entry.getValue();
							if (prices == null || prices.isEmpty())
								continue;
							
							String dataPrice0 = mapper.writeValueAsString(prices);
							PriceStringBO item0 = new PriceStringBO();
							item0.ProductCode = exchangeProductCode;
							item0.PriceArea = 644;
							item0.OutputType = 0;						
							item0.SiteID = message.SiteID;
							item0.LangID = message.Lang;
							item0.Data = dataPrice0;
							item0.RecordID = recordID;
							item0.didxupdateddate = Utils.GetCurrentDate();

							//var toBeUpserted = PriceStock.fromListPricesBhx(recordID, prices);
							//if (toBeUpserted == null)
							//	continue;
							
							SqlInfo sqlinfo = new SqlInfo();
							RefSql ref = new RefSql();
							Utils.BuildSql(isLog, strNOTE, "productprice", "recordid", recordID, item0, ref);
							sqlinfo.Sql = ref.Sql;
							sqlinfo.Params = ref.params;
							messageRepush.SqlList.add(sqlinfo);
						}
						messageRepush.Source = "STATUS";
//			 
//						String qu = "gr.dc4.sql.price";
//						String qu2 = "gr.dc4.sql.price";
//						String qubk = "gr.dc2.sql.price";
//						String qudev = "gr.beta.sql.price";
						int ie = Utils.GetQueueNum5(ProductID);
						String qu = "gr.dc4.sql" + ie;
						String qu2 = "gr.dc4.sql" + ie;
						String qubk = "gr.dc2.sql" + ie;
						String qudev = "gr.beta.sql"  ;
					
						messageRepush.Identify = message.Identify;
						messageRepush.Action = DataAction.Update;
						//messageRepush.ClassName = "mwg.wb.pkg.upsert.Upsert";
						messageRepush.ClassName = "ms.upsert.Upsert";
						messageRepush.CreatedDate = Utils.GetCurrentDate();
						messageRepush.Type = 0;
						messageRepush.DataCenter = DataCenter;
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepush, isLog,strNOTE,
								DataCenter);
			 
						Logs.Log(isLog, strNOTE, "msg.Code = ResultCode.Success"); 
						msg.Code = ResultCode.Success;
					}
					
					
				}			
			} catch(JSONException ignored) { // skip
			} catch (Throwable e) {
				Logs.LogException("status.txt", e);
				e.printStackTrace();
				msg.Code = ResultCode.Retry;
			}
			return msg;
		}
	


	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

	public synchronized boolean IndexDataListPriceDefault_ES(int provinceid, boolean isLog, String strNOTE,
			Map<String, Object> pricesMap, long ProductID, int SiteID, String LangID, int order) {
		String esKeyTerm = ProductID + "_" + SiteID + "_" + (LangID.toLowerCase().replaceAll("-", "_"));
		
		if (pricesMap.size() > 0) 
		{
			for (int i = 0; i < 10; i++) {

				try {

					var json = mapper.writeValueAsString(pricesMap);
					var update = new UpdateRequest(indexDB, esKeyTerm)
							.doc("{\"Order\":" + order + ", \"PricesBHX\": " + json + "}", XContentType.JSON)
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
					Logs.WriteLine(e);
					Utils.Sleep(100);
				}
			}

		}

		return false;
	}
	
	
	public boolean IndexDataListPriceDefault_ES1() throws Throwable {
		String esKeyTerm = "128701_11_vi_vn";
		var productBo = productHelper.GetProductSO(128701, 11, "vi-VN");
		var id = productBo.CategoryID;
		if (true) {
			for (int i = 0; i < 10; i++) {

				try {					
					var update = new UpdateRequest(indexDB, esKeyTerm)
							.doc(XContentFactory.jsonBuilder()
						            .startObject()
					                .field("Order", "3")
					                .field("PricesBHX.ll", 100)
					            .endObject()
									
									//"{\"Order\":" + 1 + ", \"PricesBHX.Price_3_4100\": " + 2000 + "}", XContentType.JSON
								)
							.docAsUpsert(true).detectNoop(false);
					var client = ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
							.getClient();
					var response = client.update(update, RequestOptions.DEFAULT);
					if (response != null && response.getResult() == Result.UPDATED) {
						// Logs.WriteLine("-Index price status to ES success: " + ProductID);
						// Logs.WriteLine("ES");

						return true;
					} else {

						Logs.WriteLine("-Index price status to ES FAILED: " + 2 + ", "
								+ response.getResult().name() + "######################");
						Utils.Sleep(100);
					}
				} catch (Exception e) {
					Logs.WriteLine("Exception Index price status to ES FAILED: " + 2);
					Logs.WriteLine(e);
					Utils.Sleep(100);
				}
			}

		}

		return false;
	}

	private boolean IndexDataListPriceDefault_OR(int ProvinceID, boolean isLog, String strNOTE,
			Map<Integer, PriceDefaultBO> pricesListdetaultByProvince, long ProductID, int siteID, String langID) throws JsonProcessingException {
		MessageQueue messageRepush = new MessageQueue();
		messageRepush.SqlList = new ArrayList<SqlInfo>();

		var pricegr = PriceDefaultGRBO.fromListDefaultBO(ProductID, siteID, langID,
				pricesListdetaultByProvince.values());
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

		messageRepush.Source = "STATUSV2";

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
	
	private static boolean contains(int[] array, int v) {

        boolean result = false;

        for(int i : array){
            if(i == v){
                result = true;
                break;
            }
        }
        return result;
    }

}
