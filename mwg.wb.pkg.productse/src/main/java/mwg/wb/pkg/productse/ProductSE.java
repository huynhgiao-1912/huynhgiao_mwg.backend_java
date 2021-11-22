package mwg.wb.pkg.productse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.orientechnologies.orient.server.OSystemDatabase;
import mwg.wb.business.CategoryHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.ProductUrlHelper;
import mwg.wb.business.webservice.WebserviceHelper;
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.CodeTimers;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.Utils;
import mwg.wb.common.notify.LineNotify;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.*;
import mwg.wb.model.promotion.ShockPriceBO;
import mwg.wb.model.search.ProductSO;
import mwg.wb.model.search.Prop;
import mwg.wb.model.seo.ProductUrl;
import mwg.wb.pkg.product.SeoUrl;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProductSE implements Ididx {

	private String CurrentIndexDB = "";
	private static ProductHelper productHelper = null;
		private static CategoryHelper categoryHelper = null;
	private int DataCenter = 0;
	private ObjectMapper mapper = null;
//	private ORThreadLocal owriter;
	private ClientConfig clientConfig = null;
	private ErpHelper erpHelper = null;

	private final int[] AllListProvinceID = { 3, 5, 6, 7, 8, 9, 81, 82, 101, 102, 103, 104, 105, 106, 107, 108, 109,
			110, 111, 112, 113, 114, 115, 116, 117, 118, 120, 121, 122, 123, 124, 125, 126, 129, 130, 131, 132, 133,
			134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154,
			155, 156, 127, 128, 167, 168, 169, 170, 171, 172, 173, 176, 177, 178, 179, 181, 182, 186, 187, 185, 163,
			164, 165, 166, 174, 175, 180, 183, 184 };
	private final int[] Appliance2142Categories = new int[]{4928, 4931, 5205, 4930, 5228, 4927, 5225, 5292, 5227,
			5226, 5230, 5231, 2403, 2402, 5229, 4929, 4932, 5478, 5395, 5354, 6790, 6819, 6790, 3187, 3729, 7075, 346,
			6599, 7305, 6012, 7479, 2528, 7480, 3736, 4946, 7481, 7482, 6553};
	private int[] NewAccessoryCategory =
    {
        6858,60,1662,1363
    };
	public String[] AccessoryCategory = { "482", "60", "57", "55", "58", "54", "1662", "1363", "1823", "56", "75", "86", "382", "346", "2429", "2823", "2824", "2825", "3885", "5025", "1622", "5505", "5005" };
	@Override
	public void InitObject(ObjectTransfer objectTransfer) {
		productHelper = (ProductHelper) objectTransfer.productHelper;
		categoryHelper = (CategoryHelper) objectTransfer.categoryHelper;	
		
//		owriter = (ORThreadLocal) objectTransfer.factoryWrite;
		mapper = (ObjectMapper) objectTransfer.mapper;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		CurrentIndexDB = clientConfig.ELASTICSEARCH_PRODUCT_INDEX;
		erpHelper = (ErpHelper) objectTransfer.erpHelper;
		notifyHelperLog = (LineNotify) objectTransfer.notifyHelperLog;
	}

	private LineNotify notifyHelperLog = null;
	private final Lock queueLock = new ReentrantLock();

	public ProductDetailBO[] PushRefresh(MessageQueue message, int ProductID, int siteid, String lang, boolean isLog)
			throws Throwable {

		ProductDetailBO[] detailList = productHelper.getFullProductDetail(ProductID, message.SiteID, message.Lang,
				false , new CodeTimers()).list;

		MessageQueue messageRepushV2 = new MessageQueue();
		messageRepushV2.SqlList = new ArrayList<SqlInfo>();
		messageRepushV2.SiteID = message.SiteID;

		String data = mapper.writeValueAsString(detailList);

		SqlInfo sqlinfo0 = new SqlInfo();
		String recordid = ProductID + "_" + siteid + "_" + DidxHelper.GenTerm(lang);
		sqlinfo0.Sql = "update product_detail_cache set data=:data,langid=:langid ,productid=:productid,recordid=:recordid,siteid=:siteid upsert where recordid='"
				+ recordid + "'";
		sqlinfo0.tablename = "product_detail_cache";
		sqlinfo0.tablekey = "recordid";
		sqlinfo0.Params = new HashMap<String, Object>();
		sqlinfo0.Params.put("recordid", recordid);
		sqlinfo0.Params.put("data", data);
		sqlinfo0.Params.put("langid", lang);
		sqlinfo0.Params.put("productid", ProductID);
		sqlinfo0.Params.put("siteid", siteid);

		messageRepushV2.SqlList.add(sqlinfo0);

//		SqlInfo sqlinfoEgde0 = new SqlInfo();
//		sqlinfoEgde0.Params = new HashMap<String, Object>();
//		sqlinfoEgde0.Type = SqlInfoType.CREATE_EDGE_BY_NODE_GENERAL;
//		sqlinfoEgde0.Params.put("edge", "e_product_product_detail_cache");
//		sqlinfoEgde0.Params.put("from", ProductID);
//		sqlinfoEgde0.Params.put("to", recordid);
//
//		sqlinfoEgde0.Params.put("fromtbl", "product");
//		sqlinfoEgde0.Params.put("totbl", "product_detail_cache");
//
//		sqlinfoEgde0.Params.put("fromcol", "productid");
//		sqlinfoEgde0.Params.put("tocol", "recordid");
//
//		messageRepushV2.SqlList.add(sqlinfoEgde0);

		String qu = "gr.dc4.sql.detail";
		String qu2 = "gr.dc4.sql.detail";
		String qubk = "gr.dc2.sql.detail";
		String qudev = "gr.beta.sql.detail";
//		String qu = "gr.dc4.sql.sysdata0";
//		String qu2 = "gr.dc4.sql.sysdata0";
//		String qubk = "gr.dc2.sql.sysdata0";
//		String qudev = "gr.beta.sql.sysdata";
//		int hsa = Utils.GetQueueNum(ProductID);
//		if (hsa > 0) {
//			qu = "gr.dc4.sql.sysdata" + hsa;
//			qu2 = "gr.dc4.sql.sysdata" + hsa;
//			qubk = "gr.dc2.sql.sysdata" + hsa;
//			qudev = "gr.beta.sql.sysdata";
//			messageRepushV2.Hash = ProductID;
//		} else {
//			qu = "gr.dc4.sql.sysdata0";
//			qu2 = "gr.dc4.sql.sysdata0";
//			qubk = "gr.dc2.sql.sysdata0";
//			qudev = "gr.beta.sql.sysdata";
//
//		}

		messageRepushV2.Action = DataAction.Update;
		messageRepushV2.ClassName = "ms.upsert.Upsert";
		messageRepushV2.CreatedDate = Utils.GetCurrentDate();
		messageRepushV2.Lang = lang;
		messageRepushV2.SiteID = siteid;
		messageRepushV2.Source = "SE";
		messageRepushV2.RefIdentify = message.Identify;
		messageRepushV2.Identify = String.valueOf(ProductID);
		messageRepushV2.Hash = ProductID;
		messageRepushV2.Note = message.Note;

		messageRepushV2.DataCenter = message.DataCenter;
		QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepushV2, isLog,
				message.Note, message.DataCenter); // 0 bom 2 cái
		Logs.Log(isLog, message.Note, "Push product_detail_cache " + ProductID);
		return detailList;
	}

	public ResultMessage Refresh(MessageQueue message) {
		DataCenter = message.DataCenter;
		ResultMessage resultMessage = new ResultMessage();
		resultMessage.Code = ResultCode.Success;

		if (message.SiteID == 8) {
			return resultMessage;
		}

		String strNOTE = message.Note + "";
		if (strNOTE.contains("DIDX_TOP")) {
			notifyHelperLog.NotifyInfo("PRODUCTSE:" + strNOTE +":"+message.Identify,message.DataCenter);
		}
		boolean isLog = false;
		if (strNOTE.contains("LOG")) {

			isLog = true;
		}
		DataAction action = message.Action;

		int ProductID = Utils.toInt(message.Identify);
		int SiteID = message.SiteID;

		if (ProductID <= 0) {
			Logs.WriteLine("ProductID <= 0");
			return resultMessage;
		}
		if (SiteID <= 0) {
			Logs.WriteLine("SiteID <= 0");
			return resultMessage;
		}

		if (Utils.StringIsEmpty(message.Lang)) {
			Logs.WriteLine("message.Lang == null");
			return resultMessage;
		}
		String LangID = message.Lang.toLowerCase().replace("-", "_");

		String esKeyTerm = ProductID + "_" + SiteID + "_" + LangID;
		Logs.Log(isLog, strNOTE, "Refresh ProductSE " + ProductID);

		if (strNOTE.contains("DIDX_DETAIL")) {
			try {
				PushRefresh(message, ProductID, message.SiteID, message.Lang, isLog);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return resultMessage;
		}
		if (action == DataAction.Delete) {
			try {
				Logs.Log(isLog, strNOTE, "delete " + esKeyTerm);

				try {
					queueLock.lock();
					var update = new UpdateRequest(CurrentIndexDB, esKeyTerm)
							.doc("{\"IsDeleted\":1}", XContentType.JSON).docAsUpsert(true).detectNoop(false);
					var client = ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
							.getClient();
					var response = client.update(update, RequestOptions.DEFAULT);
					if (response != null && response.getResult() == Result.UPDATED) {
						resultMessage.Code = ResultCode.Success;
					} else {

						Logs.WriteLine("-Index price status to ES FAILED: " + ProductID + ", "
								+ response.getResult().name() + "######################");
						Utils.Sleep(100);
						resultMessage.Code = ResultCode.Error;
					}
				} catch (Exception e) {
					resultMessage.Code = ResultCode.Error;
					e.printStackTrace();
				} finally {
					queueLock.unlock();
				}
				return resultMessage;

			} catch (Exception e) {
				e.printStackTrace();
				Logs.LogException(e);
			}

		} else {
			if (message.ID == -1) {

			} else {
				try {
					Logs.Log(isLog, strNOTE, "GetProductBOByProductIDSE");
					long d1 = System.currentTimeMillis();
					ProductBO productBO = productHelper.GetProductBOByProductIDSE(ProductID, message.SiteID,
							message.Lang);
					
					long d2 = System.currentTimeMillis();

					Logs.Log(isLog, strNOTE, "GetProductBOByProductIDSE : " + (d2 - d1));

					if (productBO == null || productBO.ProductLanguageBO == null) {
						Logs.WriteLine("  null " + ProductID + " indexing delete...");
//						resultMessage.Code = ResultCode.Success;
//						return resultMessage;
						message.Action = DataAction.Delete;
						return Refresh(message);
					}
					ShockPriceBO sockBO = productHelper.GetShockPriceInfo(ProductID, message.SiteID);
					if(sockBO != null) {
						productBO.ShockPriceType = sockBO.ShockPriceType;
					}
					var catemap = productHelper.productCategoryMapSelByProductID(ProductID, message.SiteID);//nn liên quany
					if(catemap != null && catemap.length > 0) {
						productBO.RelatedCategoryIDList = String.join(",", Stream.of(catemap).mapToInt(x -> x.CategoryID).mapToObj(String::valueOf).toArray(String[]::new));
					}
					
					
					
					// lấy list detail vể có thể làm luôn cái khác
					var _productDetails =
					PushRefresh(message, ProductID, message.SiteID, message.Lang, isLog);

//					ProductDetailBO[] _productDetails = null;
//					try {
//						_productDetails = productHelper.GetListPropProduct(ProductID, message.SiteID, message.Lang,
//								productBO).stream().toArray(ProductDetailBO[]::new);
//					} catch (Throwable e) {
//						e.printStackTrace();
//						Logs.LogException(e);
//						resultMessage.Code = ResultCode.Retry;
//						return resultMessage;
//
//					}
					long d3 = System.currentTimeMillis();
					Logs.Log(isLog, strNOTE, "GetListPropProduct : " + (d3 - d2));

					List<ProductCombo> listCombo = new ArrayList<ProductCombo>();
					// productcombo
					if (SiteID == 2) {
						listCombo = GetListProductComboByID(productBO, message.SiteID, message.Lang);
					}

					resultMessage = IndexData(isLog, strNOTE, ProductID, productBO, _productDetails, listCombo);

					long d4 = System.currentTimeMillis();

					Logs.Log(isLog, strNOTE, "IndexData done " + (d4 - d3));

					if (productBO != null && productBO.ProductManuLangBO != null
							&& productBO.ProductCategoryBO != null) {
						 
						Logs.Log(true, "DIDX_LOG|PRODUCTSEOURL",  ""+ ProductID);

						String urlStr = ProductUrlHelper.GenSEOProductUrl(productBO);
						ProductUrl urlPrd = new ProductUrl() {
							{
								PID = productBO.ProductID;
								MID = (int) productBO.ProductManuLangBO.ManufacturerID;
								CID = (int) productBO.ProductCategoryBO.CategoryID;
							}
						};
						String json = mapper.writeValueAsString(urlPrd);
						SeoUrl.pushUpsert(clientConfig.SERVER_RABBITMQ_URL, json, urlStr, SiteID, message.Lang, isLog,
								strNOTE, DataCenter);
						Logs.Log(true, "DIDX_LOG|PRODUCTSEOURL",  "pushUpsert site id " + SiteID
								+ " url " + urlStr +":"+ json);

					} else {
						Logs.Log(true, "DIDX_LOG|PRODUCTSEOURL",  "productBO == null " +
								"|| productBO.ProductManuLangBO == null || productBO.ProductCategoryBO == null:"+ ProductID);

						Logs.Log(isLog, strNOTE,
								"productBO == null || productBO.ProductManuLangBO == null || productBO.ProductCategoryBO == null ");

					}
					if(Utils.StringIsEmpty(productBO.priceDefaultString)){
						// không có gì thì cập nhật lại status.
						// do ko có giá thì status sẽ ko nhận đc notify
						int h = (int) (productBO.ProductID % 3);
						String queue = "gr.dc4.didx.status" + h;
						String queueDev = "gr.beta.didx.status" + h;
						String queueBK = "gr.dc2.didx.status" + h;


						MessageQueue nwmsg = new MessageQueue();
						nwmsg.Action = DataAction.Update;
						nwmsg.ClassName = "mwg.wb.pkg.price.Status";
						nwmsg.CreatedDate = Utils.GetCurrentDate();
						nwmsg.Lang = "vi-VN";
						nwmsg.SiteID = message.SiteID;
						nwmsg.Type = 0;
						nwmsg.Data = "";
						nwmsg.DataCenter = clientConfig.DATACENTER;// == 3 ? 3 : 4;
						nwmsg.Identify = productBO.ProductID + "";
						//Logs.Log(true, "", " ");
						Logs.LogFile("updatepromotionexpired.txt", nwmsg.Identify + "|" +  nwmsg.SiteID );
						nwmsg.Note = "Update Promotion Expired";
						QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev, nwmsg,
								true, "", nwmsg.DataCenter);
					}

					///

					resultMessage = RefreshPreProduct(ProductID);

					if (resultMessage.Code != ResultCode.Success) {
						return resultMessage;
					}

					// resultMessage.Code = ResultCode.Success;
				} catch (Throwable e) {
					e.printStackTrace();
					Logs.LogException(e,"productse");
					resultMessage.Code = ResultCode.Retry;
					resultMessage.Message = e.getMessage();
//					resultMessage.StackTrace = Stream.of(e.getStackTrace()).map(x -> x.toString())
//							.collect(Collectors.joining(", "));
					//Logs.LogException( "productse","Ms_ProductSE, Product ID: " + message.Identify +""+ resultMessage.Message);
					return resultMessage;

				}

			}
		}

		return resultMessage;

	}

	private List<ProductCombo> GetListProductComboByID(ProductBO product, int siteID, String lang) throws Throwable {
		List<ProductCombo> listCombo = new ArrayList<ProductCombo>();

		if (product.ProductLanguageBO != null && !Utils.StringIsEmpty(product.ProductLanguageBO.comboproductidlist)) {
			listCombo = GetProductComboProductByComboID(product, product.ProductLanguageBO.comboproductidlist, siteID,
					lang);

		}
		return listCombo;
	}

	private boolean UpsertElasticProduct(String esKeyTerm, Map<String, Object> data) throws IOException {
		if (data == null || data.size() <= 0)
			return false;

		String jsonData = "{";
		for (Map.Entry<String, Object> entry : data.entrySet()) {

			if (entry.getValue().toString().contains("{")) {// la chuoi json
				jsonData += "\"" + entry.getKey() + "\":" + entry.getValue() + ",";

			} else {
				jsonData += "\"" + entry.getKey() + "\":" + "\"" + entry.getValue() + "\"" + ",";

			}

		}
		jsonData = StringUtils.strip(jsonData, ",");
		jsonData += "}";

		var update = new UpdateRequest(CurrentIndexDB, esKeyTerm).doc(jsonData, XContentType.JSON).docAsUpsert(true)
				.detectNoop(false);
		var client = ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();

		client.update(update, RequestOptions.DEFAULT);

		return true;

	}

	// thanhphi0401
	private List<ProductCombo> GetProductComboProductByComboID(ProductBO product, String comboproductidlist, int siteID,
			String lang) throws Throwable {

		List<ProductCombo> listProductCombo = new ArrayList<>();
		Map<String, Object> pricesMap = new HashMap<String, Object>();
		Map<String, Object> dataInit = new HashMap<String, Object>();
		String mainEsKeyTerm = product.ProductID + "_" + siteID + "_" + lang.toLowerCase().replace("-", "_");

		ProductCombo[] lstComboProduct = erpHelper.GetProductComboProductByComboID(comboproductidlist);
		if (lstComboProduct != null && lstComboProduct.length > 0) {

			listProductCombo = Arrays.asList(lstComboProduct);

			String listComboProductID = "";
			// B1: cap nhat moi lien he giua combo va sp
			for (var productitem : lstComboProduct) {
				listComboProductID += productitem.ProductIDRef + " ";
				String LangID = lang.toLowerCase().replace("-", "_");
				String esKeyTerm = productitem.ProductIDRef + "_" + siteID + "_" + LangID;

				var tmpproduct = getProductEla(false, "", esKeyTerm);
				dataInit = new HashMap<String, Object>();
				if (tmpproduct != null) {
					if (Utils.StringIsEmpty(tmpproduct.ComboProductIds)) {

						// cap nhat lai elastic sp combo
						dataInit.put("ComboProductIds", product.ProductID);
					} else {
						if (!tmpproduct.ComboProductIds.contains(String.valueOf(product.ProductID))) {

							String newListComboID = tmpproduct.ComboProductIds + " " + product.ProductID;

							// cap nhat lai elastic sp combo
							dataInit.put("ComboProductIds", newListComboID);

						}
					}
					UpsertElasticProduct(esKeyTerm, dataInit);

				}
			}

			// B2: update listproductid combo cua sp hien tai
			dataInit = new HashMap<String, Object>();

			dataInit.put("ComboProductIds", listComboProductID);
			dataInit.put("IsCombo", 1);
			dataInit.put("ComboId", comboproductidlist);

			UpsertElasticProduct(mainEsKeyTerm, dataInit);

			// tinh gia sp combo
			for (var _prov : AllListProvinceID) {

				double comboprice = 0;
				boolean isGetComboPrice = true;

				for (var _combo : lstComboProduct) {
					String LangID = lang.toLowerCase().replace("-", "_");
					String esKeyTerm = _combo.ProductID + "_" + siteID + "_" + LangID;

					var tmpproduct = getProductEla(false, "", esKeyTerm);
					double itemPrice = 0;
					if (tmpproduct != null && tmpproduct.Prices != null && tmpproduct.Prices.size() > 0) {
						itemPrice = Double.valueOf(tmpproduct.Prices.get("Price_" + _prov).toString());

					}

					// 1: giá cố định; 2 giảm theo %; 3: giảm giá theo số tiền
					if (_combo.ComboType == 1) {
						comboprice += _combo.Quantity
								* (Math.round(_combo.Value * (1 + (_combo.VAT * _combo.VATPercent / 10000))));
					} else if (_combo.ComboType == 2) {

						if (itemPrice > 0) {
							comboprice += _combo.Quantity * (itemPrice - (_combo.Value / 100 * itemPrice));
						} else {
							isGetComboPrice = false;
							break;
						}
					} else if (_combo.ComboType == 3) {

						if (itemPrice > 0) {
							var value = _combo.Quantity
									* (Math.round(_combo.Value * (1 + (_combo.VAT * _combo.VATPercent / 10000))));
							comboprice += itemPrice - value;
						} else {
							isGetComboPrice = false;
							break;
						}
					}
				}

				pricesMap.put("IsShowHome_" + _prov, 1);
				pricesMap.put("WebStatusId_" + _prov, isGetComboPrice ? 4 : 0);
				pricesMap.put("Price_" + _prov, comboprice);
				pricesMap.put("ProductCode_" + _prov, comboproductidlist);

			}
			dataInit = new HashMap<String, Object>();
			dataInit.put("Prices", mapper.writeValueAsString(pricesMap));
			UpsertElasticProduct(mainEsKeyTerm, dataInit);

		} else {// xoa moi lien he va gia
//				String listComboProductID = "";
			// B1: xoa moi lien he giua combo va sp
			for (var productitem : lstComboProduct) {
//					listComboProductID += productitem.ProductIDRef + " ";
				String LangID = lang.toLowerCase().replace("-", "_");
				String esKeyTerm = productitem.ProductIDRef + "_" + siteID + "_" + LangID;

				var tmpproduct = getProductEla(false, "", esKeyTerm);
				dataInit = new HashMap<String, Object>();
				if (tmpproduct != null && !Utils.StringIsEmpty(tmpproduct.ComboProductIds)) {

					if (tmpproduct.ComboProductIds.contains(String.valueOf(product.ProductID))) {

						String newListComboID = tmpproduct.ComboProductIds.replace(String.valueOf(product.ProductID),
								"");

						// cap nhat lai elastic sp combo
						dataInit.put("ComboProductIds", newListComboID);

					}

					UpsertElasticProduct(esKeyTerm, dataInit);

				}
			}

			// B2: xoa listproductid combo cua sp hien tai
			dataInit = new HashMap<String, Object>();

			dataInit.put("ComboProductIds", "");
			dataInit.put("IsCombo", 0);
			dataInit.put("ComboId", "");
			UpsertElasticProduct(mainEsKeyTerm, dataInit);

			for (var _prov : AllListProvinceID) {

				pricesMap.put("IsShowHome_" + _prov, 0);
				pricesMap.put("WebStatusId_" + _prov, 1);
				pricesMap.put("Price_" + _prov, 0);
				pricesMap.put("ProductCode_" + _prov, comboproductidlist);

			}
			dataInit = new HashMap<String, Object>();
			dataInit.put("Prices", mapper.writeValueAsString(pricesMap));
			UpsertElasticProduct(mainEsKeyTerm, dataInit);

		}

		return listProductCombo;

	}

	public ProductSO getProductEla(boolean isLog, String StrNote, String ID) throws Throwable {
		Logs.Log(isLog, StrNote, "SERVER_ELASTICSEARCH_READ_HOST:" + clientConfig.SERVER_ELASTICSEARCH_READ_HOST
				+ ",CurrentIndexDB " + CurrentIndexDB + ",ID:" + ID);
		try {
			return ElasticClient.getInstance(clientConfig.SERVER_ELASTICSEARCH_READ_HOST)
					.GetSingleObject(CurrentIndexDB, ID, ProductSO.class);
		} catch (Exception e) {
			Logs.LogException(e);
			Logs.LogException("SERVER_ELASTICSEARCH_READ_HOST:" + clientConfig.SERVER_ELASTICSEARCH_READ_HOST
					+ ",CurrentIndexDB " + CurrentIndexDB + ",ID:" + ID);
			Logs.Log(isLog, "", "SERVER_ELASTICSEARCH_READ_HOST:" + clientConfig.SERVER_ELASTICSEARCH_READ_HOST
					+ ",CurrentIndexDB " + CurrentIndexDB + ",ID:" + ID);
			throw e;
		}

	}

	public String ConvertRfc3339(Date dateTime) throws ParseException {
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dateTime);
	}

	private ResultMessage IndexData(boolean isLog, String StrNote, int ProductID, ProductBO ProductBO,
			ProductDetailBO[] listdetail, List<ProductCombo> listCombo) throws Throwable {
		ResultMessage resultMessage = new ResultMessage();
		resultMessage.Code = ResultCode.Success;
//		var rs = true;

		var ProductLangBO = ProductBO.ProductLanguageBO;
		var ProductManuLangBO = ProductBO.ProductManuLangBO;
		var ProductCateLangBO = ProductBO.ProductCategoryLangBO;

		Logs.Log(true, "DIDX_LOG|log_productse_ProductLangBO", ProductBO.ProductID + "");

		if (ProductLangBO == null) {

			resultMessage.Code = ResultCode.Success;

			resultMessage.Message = "Not exits product_lang for" + ProductID + ". SKIP init...";
			// System.out.println("Not exits product_lang for" + ProductID + ". SKIP
			// init...");
			Logs.Log(isLog, StrNote, "Not exits product_lang for" + ProductID + ". SKIP init...");

			return resultMessage;
		}
		int CategoryID = (int) (ProductBO.ProductCategoryBO != null ? ProductBO.ProductCategoryBO.CategoryID : 0);
		int ManufactureID = (int) (ProductManuLangBO != null ? ProductManuLangBO.ManufacturerID : 0);

		String ProductName = ProductLangBO.ProductName + "";
		int SiteID = (int) ProductLangBO.SiteID;
		String LangID = ProductLangBO.LanguageID.toLowerCase().replace("-", "_");
		String esKeyTerm = ProductID + "_" + SiteID + "_" + LangID;
		var lidbycate = productHelper.GetProductPropByCategory(CategoryID, SiteID, ProductLangBO.LanguageID);
		List<Integer> propIDbyCate = new ArrayList<Integer>();
		if (lidbycate != null && lidbycate.length > 0) {
			propIDbyCate = Arrays.asList(lidbycate).stream().map(v -> v.PropertyID).collect(Collectors.toList());
		}
		ProductSO document = null;

		document = getProductEla(isLog, StrNote, esKeyTerm);

		if (document == null) {
			document = new ProductSO();
			Logs.Log(isLog, StrNote, "document = new ProductSO() " + esKeyTerm);
		}
		document.ProductID = ProductID;
		document.SiteID = SiteID;
		document.Lang = LangID;
		document.IsDeleted = ProductBO.IsDeleted ? 1 : 0;
		document.Scenario = (int) (ProductLangBO.Scenario);

		if (ProductLangBO.ScenarioShowWebFromDate != null) {
			document.ScenarioFromDate = ProductLangBO.ScenarioShowWebFromDate;
		} else {
			document.ScenarioFromDate = null;
		}

		if (ProductLangBO.ScenarioShowWebToDate != null) {
			document.ScenarioToDate = ProductLangBO.ScenarioShowWebToDate; // .scenarioshowwebtodate;
		} else {
			document.ScenarioToDate = null;
		}
		document.IsShowHome = ProductLangBO.IsShowHome == 1 ? true : false;
		document.ShowHomeDisplayOrder = (int) (ProductLangBO.ShowHomeDisplayOrder);
		document.ShowHomeStartDate = ProductLangBO.ShowHomeStartDate;
		document.ShowHomeEndDate = ProductLangBO.ShowHomeEndDate;

		document.ManufacturerName = ProductManuLangBO != null
				? DidxHelper.GenTermKeyWord(ProductManuLangBO.ManufacturerName)
				: "";

		// sold count 14 ngay
		// SL bán chạy của TGDĐ & ĐMX đều là 14 ngày, PM xác nhận 20210204 (DMX - Private)
		long to = System.currentTimeMillis();
		long from = to - 86400000L * 14;
		Integer count = WebserviceHelper.Call(clientConfig.DATACENTER).Get(
				"apiproduct/getsoldproductquantity?siteID=:?&productID=:?&fromDate=:?&toDate=:?",
				Integer.class, SiteID, ProductID, from, to);
		document.ProductSoldCount = count == null ? 0 : count;

		document.didx_updateddate = Utils.GetCurrentDate();
		document.didx_source = "SE";

		String strpro = "";

//		String strproSE = "";
//		String strprovalkeyword = "";
		String strprovalkeywordnew = "";
		String newkeyword = "";
		var words = new ArrayList<String>();

		Map<String, Object> myProp = new HashMap<String, Object>();
		var rangeProp = new HashMap<String, Double>();
		List<Prop> Props = new ArrayList<Prop>();

		int ancYearPropID = productHelper.getAnnouncedYearPropByCateId(CategoryID);
		String DungLuongCate42 = "";
		if (listdetail != null && listdetail.length > 0) {
			for (var row : listdetail) {
				if(row == null) continue;
				if(row.PropertyID == 49 && ProductBO.CategoryID == 42){
					DungLuongCate42 = row.PropValue;
				}
				// Prop_27
				var _propvalKey = "prop_" + row.PropertyID;
				var _propvalValue = row.CompareValue;
				// if (row.IsSearch) { //thuco tinh yes no
				myProp.put(_propvalKey, _propvalValue);
				// }

				if (row.PropValueID > 0) {
					Prop p = new Prop();
					if (propIDbyCate.contains(row.PropertyID)) {
						p.propkey = Utils.toDouble((100000 + row.PropertyID) + "" + row.PropValueID);

						Props.add(p);
					}
					strpro = strpro + " prop" + row.PropertyID + "_" + row.PropValueID;

				}
				//
				if (!Utils.StringIsEmpty(row.Value)) {
					strpro = strpro + " " + "prop" + row.PropertyID + "_" + DidxHelper.GenTerm3(row.Value);
				}
//				if(row.valuesObj != null && row.valuesObj.length > 0){
//					var tmp = row.valuesObj;
//					for (var value: tmp) {
//
//						if (value.IsDataSearch) {
//
//							var kw = !Utils.StringIsEmpty(value.PropValue) ? value.PropValue.replace("_", " ")
//									: "" + " " + (!Utils.StringIsEmpty(value.MetaKeyWord) ? "" : value.MetaKeyWord + " ");
//							strprovalkeywordnew = strprovalkeywordnew + kw + " ";
//						}
//					}
//				}

				if (row.IsDataSearch) {

					var kw = !Utils.StringIsEmpty(row.PropValue) ? row.PropValue.replace("_", " ")
							: "";
					var kw2 = (Utils.StringIsEmpty(row.MetaKeyWord) ? "" : row.MetaKeyWord + " ");
					strprovalkeywordnew = strprovalkeywordnew + kw + " . " + kw2 + " . ";
				}

				if (row.unitTextSE != null && !Utils.StringIsEmpty(row.Value)) {
					String valid = row.Value.replaceAll("[^0-9.-]", "");
					if (!valid.isEmpty()) {
						try {
							rangeProp.put("prop_" + row.PropertyID, Double.parseDouble(valid));
						} catch (NumberFormatException ignored) {
						}
					}
				}

				if (row.PropertyID == ancYearPropID) {
					document.AnnouncedYear = Utils.toInt(row.Value);
				}
			}
		}
		if(!Utils.StringIsEmpty(DungLuongCate42)){
			DungLuongCate42 = DungLuongCate42.replaceAll("\\D","");
			var localsto = Utils.toInt(DungLuongCate42);
			document.Capacity = localsto;
		}


		document.PropStr = strpro;
		document.listproperty = Props;

		if (ProductLangBO != null && ProductCateLangBO != null && ProductManuLangBO != null) {
			newkeyword = ProductLangBO.ProductName + " . " +
	                DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(ProductLangBO.ProductName)) + " . " +
	                DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField3(ProductLangBO.ProductName)) + " . " +
	                ProductCateLangBO.CategoryName + " . " +
	                ProductManuLangBO.ManufacturerName + " . " +
	                ProductBO.SEOName + " . " +
	                 DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(ProductBO.SEOName)) + " . " +
	                  DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField3(ProductBO.SEOName)) + " . ";
			newkeyword = newkeyword.replace("null", "");
//			strprovalkeywordnew += ProductCateLangBO.CategoryName + " . " + ProductManuLangBO.ManufacturerName + " . ";
		}
		
		
                  
		if(ProductCateLangBO != null ) {
			if(ProductManuLangBO != null) {
				String[] arrCateKeyword = {};
				if(!Utils.StringIsEmpty(ProductCateLangBO.KeyWord)) {
					arrCateKeyword = ProductCateLangBO.KeyWord.split(",");
				}
				
				//1. ten hang + tu khoa NH
				var tmp = Stream.of(arrCateKeyword).filter(x -> !Utils.StringIsEmpty(x) && x.trim().split(" ") != null && x.trim().split(" ").length < 4).toArray(String[]::new);
				var finalJoin = String.join(" " + ProductManuLangBO.ManufacturerName + " . ", tmp );
				newkeyword += " . " + finalJoin + " " + ProductManuLangBO.ManufacturerName + " . ";//cong thang cuoi vao cho khoi thieu
				
				//2. ten hang  + ten NH
	            newkeyword += ProductCateLangBO.CategoryName + " " + ProductManuLangBO.ManufacturerName + " . ";
	            
	            if (!Utils.StringIsEmpty(ProductManuLangBO.MetaKeyWord))
	            {
	
	                //3. ten NH + metakw hang
	            	String[] metaKeywordSplit = !Utils.StringIsEmpty(ProductManuLangBO.MetaKeyWord) ? ProductManuLangBO.MetaKeyWord.split(",") : new String[]{};
	
	                if(metaKeywordSplit != null && metaKeywordSplit.length > 0) {
	                	for (String item : metaKeywordSplit) {
	                		newkeyword += ProductCateLangBO.CategoryName + " " + item + " . ";
						}
		                //4. kw hang + kw nh
		                for (String kCate : arrCateKeyword) {
		                	for (String kManu : metaKeywordSplit) {
		                		String phrase = kCate + " " + kManu;
	                            newkeyword += " " + phrase + " . ";
		    				}
						}
	                }
	            }
			}else {
				newkeyword += " . " + ProductCateLangBO.KeyWord + " . ";
			}
		}
		
		
//		newkeyword += (ProductCateLangBO != null ? ProductCateLangBO.CategoryName : "") + " "
//				+ (ProductManuLangBO != null ? ProductManuLangBO.ManufacturerName : "") + " " + ProductName;
//		if (!Utils.StringIsEmpty(ProductLangBO.KeyWord)) {
//
//			newkeyword += " " + ProductLangBO.KeyWord;
//			newkeyword += " " + ProductLangBO.MetaKeyWord;
//			if(!Utils.StringIsEmpty(ProductBO.Tag)) {
//				newkeyword += " " + ProductBO.Tag;
//			}
//		}
		//document.MainKeyword = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(keyword));
		document.NewKeyword = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordSearchField(newkeyword + " " + strprovalkeywordnew));

		String keyword = "";
		if (ProductCateLangBO != null && ProductManuLangBO != null) {
			if (ManufactureID == 203 || ManufactureID == 10387) {
				keyword += ProductCateLangBO.CategoryName + " ";
			} else {
				keyword += ProductCateLangBO.CategoryName + " " + ProductManuLangBO.ManufacturerName + " ";
			}
		}
		if ((CategoryID != 42 && CategoryID != 44 && CategoryID != 522)
				|| !ProductName.toLowerCase().contains("thẻ nhớ")
						&& !ProductName.toLowerCase().contains("sạc dự phòng")) {
			keyword += ProductName + " ";
		}
		if(SiteID == 2 && ProductCateLangBO != null) {
			if(!Arrays.asList(AccessoryCategory).contains(ProductCateLangBO.CategoryID+"")) {
				keyword += " " + ProductCateLangBO.CategoryName + " " + ProductLangBO.SEOName + " , " + ProductLangBO.KeyWord + " , ";
			}
		}else{
			if (!Utils.StringIsEmpty(ProductLangBO.SEOName)) {
				keyword += " " + ProductLangBO.SEOName;
			}
			if (!Utils.StringIsEmpty(ProductLangBO.KeyWord))
				keyword += " , " + ProductLangBO.KeyWord + " , ";
		}
		// keyword tskt
		if (listdetail != null && listdetail.length > 0) {
			for (var element : listdetail) {
				if(element!=null) {
				var kw = "";
				if (!Utils.StringIsEmpty( element.PropValue))
					kw = element.PropValue.replace("_", " ") + " ";
				if (!Utils.StringIsEmpty(element.MetaKeyWord))
					kw += element.MetaKeyWord + " ";
				keyword += kw + " ";
				}
			}
		}

		// từ khóa sai chính ta hãng
		if (ProductManuLangBO != null && !Utils.StringIsEmpty(ProductManuLangBO.MetaKeyWord)) {
			// if (objPPL.Product.ProductManuBO.IsActived == true)
			{
				keyword += " " + ProductManuLangBO.MetaKeyWord;
			}

		}

		

		
		if (!Utils.StringIsEmpty(ProductLangBO.ShortName)) {
			if ((CategoryID != 42 && CategoryID != 44 && CategoryID != 522)
					|| !ProductLangBO.ShortName.toLowerCase().contains("thẻ nhớ")
							&& !ProductLangBO.ShortName.toLowerCase().contains("sạc dự phòng")) {
				keyword += ProductLangBO.ShortName;
			}
			if(ProductLangBO != null && !Utils.StringIsEmpty(ProductLangBO.ShortName)) {
				var input = ProductLangBO.ShortName.trim();
                String shortNameAfter = "";
                List<String> listKeyword = new ArrayList<String>();
                shortNameAfter = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField3(input));
                listKeyword = BuildKeywordBaseOnShortName(shortNameAfter);
                if(listKeyword != null && listKeyword.size() > 0)
                	words.addAll(listKeyword);
                
                //special rule base on experience
	            listKeyword = BuildKeywordSpecialRule(shortNameAfter);
				if(listKeyword != null && listKeyword.size() > 0)
					words.addAll(listKeyword);
	            
	            shortNameAfter = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField3(ProductLangBO.ShortName));
	            String[] shortNameSplit = !Utils.StringIsEmpty(shortNameAfter) ? shortNameAfter.split(" ") : new String[] {};
	            if(shortNameSplit != null && shortNameSplit.length > 0) {
	            	for (String item : shortNameSplit)
	                {
	                    if (item.length() < 4) continue;
	                    listKeyword = BuildKeywordBaseOnShortName(item);
						if(listKeyword != null && listKeyword.size() > 0)
							words.addAll(listKeyword);

	                    listKeyword = BuildKeywordSpecialRule(item);
						if(listKeyword != null && listKeyword.size() > 0)
							words.addAll(listKeyword);
	                }
	            }
	            
//	            words.RemoveAll(x => x.Length < 4);
//                words = words.Distinct().ToList();
	            words = new ArrayList<String>(words.stream().filter(x -> x.length() >= 4).distinct().collect(Collectors.toList()));
	            
			}
			if(words.size() > 0) {
				document.MainKeyword = document.MainKeyword + " " + String.join(" ", words);
	            document.NewKeyword = document.NewKeyword + " " + String.join(" ", words);
			}
			
		}
		
		String keywordRemoveSpace = DidxHelper.FormatKeywordSearchField(ProductName);
		keyword = SiteID == 6 ? DidxHelper.FormatKeywordSearchFieldCam(keyword)
				: DidxHelper.FormatKeywordSearchField(keyword);
		document.MainKeyword = DidxHelper.FilterVietkey(keyword);
		document.Keyword = document.MainKeyword + " " + keywordRemoveSpace;
		
		document.ManufactureID = ManufactureID;

		document.CategoryID = CategoryID;
		document.Appliance2142Categories =  Arrays.asList(Appliance2142Categories).contains(CategoryID);
		//document.Appliance2142Categories =  Arrays.asList(Appliance2142Categories).contains(CategoryID) == true ? 1 : 0;

		// init bộ keyword search
		document.CategoryName = (ProductCateLangBO != null ? DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(ProductCateLangBO.CategoryName)) : "");
		String tmpkeyword = "";
		try {
			String ProductLangBOKeyWord  = ProductCateLangBO != null && ProductLangBO != null && !Utils.StringIsEmpty(ProductLangBO.KeyWord) && !Arrays.asList(NewAccessoryCategory).contains(ProductCateLangBO.CategoryID) //kh phai la phu kien
	                ? 
	                    	!Utils.StringIsEmpty(ProductLangBO.KeyWord) ? ProductLangBO.KeyWord.replace(",", " . ") : "" : "";
			tmpkeyword = newkeyword + " " + ProductLangBOKeyWord
                + " . " + (words != null && words.size() > 0 ? String.join(" . ", words) : "");
		}catch(Exception e) {
			Logs.LogException("Ms_Productse: " + ProductBO.ProductID);
			Logs.LogException(e);
		}
                
		document.NewMainKeyword = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordFieldForSE(tmpkeyword));
		document.NewSubKeyword = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordFieldForSE(
				CategoryID == 2002
						? (tmpkeyword + " . " + strprovalkeywordnew)
						: strprovalkeywordnew
		));
		
		
		var sticker = productHelper.getStickerLabel(ProductID, CategoryID, SiteID, ProductLangBO.LanguageID);
		if (sticker != null && sticker.icon != null) {
			document.StickerLabel = sticker.icon + "|" + sticker.valueid;
		} else {
			document.StickerLabel = "";
		}
		// document.ViewCount = 0;

//        String SoldProductKey = string.Format(DataKey.ProductModule.Product.SOLD_TRACKING, ProductID);
//        //int soldcount = iCached.Get<int>(SoldProductKey);
//		int soldcount = 0;
//		document.ProductSoldCount = soldcount;

		document.IsCollection = ProductLangBO.Iscollection;
		document.IsShowWeb = ProductLangBO.IsshowWeb;
		document.CollectionID = (int) ProductLangBO.Collectionid;
		document.CollectionProductCount = (int) (ProductLangBO.CollectionProductCount);
		document.ProductType = 1;
		// String ProductCode = GetDefautProductCode(objPPL.Product.ProductID, iCached,
		// 3, 1).Trim();
//		String ProductCode = "";
		document.ProductCode = ProductBO.ProductCode;
		// bancu ko thay loc, du 2 sp hasbimage=0
		if (Utils.StringIsEmpty(ProductLangBO.bimage)) {
			document.HasBimage = 0;
		} else {

			document.HasBimage = 1;
		}
//        //if (!string.IsNullOrEmpty(objPPL.Product.ProductCode))
//        //{
//        //    document.HasPromotion = IsHasPromotion(objPPL.Product.ProductCode, objPPL.Product.Scenario.ToString());
//        //}
		document.HasPromotion = 0;

		document.PropVal = myProp;
		document.rangeProp = rangeProp;
		document.DateCreated = ProductBO.CreatedDate;

		document.GeneralKeyword = ProductLangBO.GeneralKeyWord;

		document.IsFeature = ProductBO.IsFeature ? 1 : 0;

		if (ProductLangBO.FeatureStartDate != null) {
			document.FeatureStartDate = ProductLangBO.FeatureStartDate;
		} else {
			document.FeatureStartDate = new Date(0);
		}
		if (ProductLangBO.FeatureExpireDate != null) {
			document.FeatureExpireDate = ProductLangBO.FeatureExpireDate;
		} else {
			document.FeatureExpireDate = new Date(1000);
		}
		if (ProductLangBO.GeneralKeyWord != null && !Utils.StringIsEmpty(ProductLangBO.GeneralKeyWord)) {
			document.IsGeneralKeyword = 1;
		}

		// int SliderStatus = 0;239339
		// var rslSlide = iCached.Get<List<ProductSliderBO>>(proSlideKey);
		// if (rslSlide != null)
		// {
		// if (rslSlide.Count > 0)
		// {
		// SliderStatus = 1;
		// }
		// else
		// {
		// SliderStatus = 2;

		// }
		// }
		// document.SliderStatus = SliderStatus;
		document.didx_updateddate = Utils.GetCurrentDate();
		document.SliderStatus = 0;

		document.IsRepresentProduct = ProductLangBO.IsRepResentProduct;
		document.PresentProductID = (int) ProductLangBO.RepresentProductID;
		document.IsReferAccessory = ProductLangBO.IsreferAccessory;
		document.DisplayOrder = ProductBO.DisplayOrder <= 0 ? 9999 : ProductBO.DisplayOrder;

		if (!Utils.StringIsEmpty(ProductName)) {
			String productNameSE = DidxHelper
					.FilterVietkey(ProductName.toLowerCase().replace("/", " ").replace("(", " ").replace(")", " "));
			document.ProductName = ProductName.toLowerCase().replace("/", " ").replace("(", " ").replace(")", " ")
					.replace("+", " plus ") + " " + productNameSE.replace("+", " plus ") + " "
					+ productNameSE.replace("+", "plus");
		}

		document.DocumentKeyTerm = esKeyTerm + " " + DidxHelper.GenTerm3(esKeyTerm) + " " + ProductID + " "
				+ document.SiteID + " " + LangID;
		document.ListProductCombo = new ProductCombo[listCombo.size()];

		document.ListProductCombo = listCombo != null && listCombo.size() > 0
				? listCombo.toArray(document.ListProductCombo)
				: null;

		Date now = new Date();

		// san pham online only
		document.IsOnlineOnly = false;
		document.OnlineOnlyFromDate = null;
		document.OnlineOnlyToDate = null;
		if (ProductBO.ProductCode != null) {
			var sale = productHelper.getSpecialSaleProgram(ProductBO.ProductCode, 1);
			if (sale != null && !Strings.isNullOrEmpty(sale.SpecialSaleProgramName) && sale.BeginDate.before(now)
					&& sale.EndDate.after(now)) {
				document.IsOnlineOnly = true;
				document.OnlineOnlyFromDate = sale.BeginDate;
				document.OnlineOnlyToDate = sale.EndDate;
			}
		}

		// dong ho cap
		if (ProductBO.CategoryID == 7264) {
			var coupleList = productHelper.getListCoupleWatch(ProductID, SiteID, SiteID == 6 ? 163 : 3, LangID);
			if (coupleList != null && coupleList.length > 1) {
				document.IsCoupleWatch = true;
				document.CoupleWatchIDs = Stream.of(coupleList).map(x -> x.ProductID + "")
						.collect(Collectors.joining(" "));
			}
		}
		//if (DidxHelper.isBeta() || DidxHelper.isLocal()) {
		// máy lạnh multi
		if (ProductBO.CategoryID == 2002) {
			var MultiAirConditioner = productHelper.getListPreResentProduct(ProductID, SiteID,
					SiteID == 6 ? 163 : 3, LangID);

			var checkSPKHL = Arrays.stream(MultiAirConditioner).filter(x -> x.ProductErpPriceBO == null || x.ProductErpPriceBO.WebStatusId == 8 ).count();
			if (MultiAirConditioner != null && MultiAirConditioner.length > 1 && checkSPKHL == 0) {
				document.IsMultiAirConditioner = true;
				document.MultiAirConditionerIds = Stream.of(MultiAirConditioner).map(x -> x.ProductID + "")
						.collect(Collectors.joining(" "));
			}

		}
		//}

		// tra gop 0%
		var lstInstallments = productHelper.GetInstallmentByProductSE(ProductID, SiteID, ProductLangBO.LanguageID,
				ProductBO.CategoryID, (int) ProductBO.ManufactureID);

		//lay ra thoi gian bat dau kich ban "sap co hieu luc" gan nhat (effective time coming soon)
		var effectTimeCS = lstInstallments.stream().filter(x -> x.FromDate.after(now)).map(x -> x.FromDate)
				.sorted().findFirst().orElse(null);

		// update nextseupdate vào se
		document.nextseupdate = effectTimeCS!=null? effectTimeCS.getTime(): 0L;
		document.strnextseupdate = effectTimeCS!=null? effectTimeCS : new Date(0);


		// lay kich ban tra gop dang co' hieu luc gan nhat
		var installment = lstInstallments.stream().filter(x -> x.FromDate.before(now))
				.min(Comparator.comparingLong(x -> Math.abs(x.FromDate.getTime() - now.getTime())))
				.orElse(null);
		if (installment != null ) {

			document.IsPayment = installment.IsPayment;
			document.PaymentFromDate = installment.FromDate;
			document.PaymentToDate = installment.ToDate;
			document.PercentInstallment = SiteID == 6 ? installment.PaymentValue : installment.PercentInstallment;
		} else {
			document.IsPayment = 0;
			document.PaymentFromDate = new Date(0L);
			document.PaymentToDate = new Date(86400L);
			document.PercentInstallment = 0;
		}
//		if(DataCenter == 3 || DataCenter == 6) {
//			var PreProduct = productHelper.getListrProductFromPreProduct(ProductID);
//			if(PreProduct != null && PreProduct.length > 0) {
//				//PrenextProduct[]
//				
//			}
//			
//		}
		
		document.ShockPriceType = ProductBO.ShockPriceType;
		if(ProductLangBO != null) {
			if(ProductLangBO.ispreordercam == 1 &&
					ProductLangBO.preordercamfromdate != null && now.after(ProductLangBO.preordercamfromdate) &&
					ProductLangBO.preordercamtodate != null && now.before(ProductLangBO.preordercamtodate) 
					//ProductLangBO.preordercamtodate.getTime() > 0
					) {
				document.IsPreOrder = 1;
			}
			document.CmsProductStatus = ProductLangBO.webstatusid;
			document.IsHearSay = ProductLangBO.IsHearSay;
			document.ProductSlider = ProductLangBO.isnewsdetailversion;
			
		}
		
		
//		if(ProductBO.ProductManuLangBO != null) {
//			document.ManufactureName = DidxHelper.GenTermKeyWord(ProductBO.ProductManuLangBO.ManufacturerName);
//		}
		if(ProductBO.ProductLanguageBO != null) {
			
			document.AccessoriesDisplayOrder = ProductBO.ProductLanguageBO.AccessoriesDisplayOrder;
			document.AccessoriesStartDate = ProductBO.ProductLanguageBO.AccessoriesStartDate;
			document.AccessoriesEndDate = ProductBO.ProductLanguageBO.AccessoriesEndDate;
		}
		
		// sản phẩm độc quyền // listdetail
		document.IsMonopolyLabel = Utils.StringIsEmpty(productHelper.GetMonopolyLabel(ProductBO,listdetail)) ? false : true;

		document.IsMonopolyCms = ProductLangBO != null && ProductLangBO.issmartphone == 1;
		document.CategoryMap = !Utils.StringIsEmpty(ProductBO.RelatedCategoryIDList) ? ProductBO.RelatedCategoryIDList.replace(",", " ") : "";
		if (document.CategoryMap != null) {
			document.intCategoryMap = Utils.toInt(document.CategoryMap);
		}
		document.ParentIdList = ProductBO.ProductCategoryBO != null && !Utils.StringIsEmpty(ProductBO.ProductCategoryBO.ParentIdList) ? ProductBO.ProductCategoryBO.ParentIdList.replace(",", " ") : "";//ss
		
		
		document.ManufactureCode = ProductBO.ProductManuBO != null && !Utils.StringIsEmpty(ProductBO.ProductManuBO.ManufacturerName) 
				? DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(ProductBO.ProductManuBO.ManufacturerName.trim())).replace(" ", "_") : "";
		
		var result = ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
				.IndexObject(CurrentIndexDB, document, esKeyTerm);
		if (result == false) {
			resultMessage.Code = ResultCode.Retry;
		}
		return resultMessage;

	}

	public ResultMessage RefreshPreProduct(int PreProductID) {

		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;

		if (PreProductID <= 0) {
			return r;
		}
		try {
			var PreProduct = productHelper.GetPreProduct(PreProductID);
//						factoryRead.QueryFunction("product_PreNextGetbyID", PrenextProduct[].class, false,
//						PreProductID);

			if (PreProduct == null || PreProduct.length == 0) {
				r.Code = ResultCode.Success;
				return r;
			} else {
				/// //
				var PreProductList = "";
				var NextProductList = "";
				// int NextProductSize = 0;
				int isDeleted = 0;
				int ProductID = 0;
				var listPreProductIDNotParent = new ArrayList<String>();

				for (PrenextProduct product : PreProduct) {

					if (product.productID == PreProductID) {
						// update 16/11/2020
						if (!Utils.StringIsEmpty(product.listproductidpre)) {
							String[] tmpProduct = product.listproductidpre.split(",");
							for (var item : tmpProduct) {
								if (Utils.StringIsEmpty(item))
									continue;
								int itemToInt = Utils.toInt(item);
								if (!Stream.of(PreProduct).anyMatch(x -> x.productID == itemToInt)) {

									var PrenextES = new PrenextProductSO();
									PrenextES.productID = itemToInt;
									PrenextES.NextProduct = Utils.toString(product.productID);
									PrenextES.ManuID = categoryHelper.getManufactureIDFromCache(itemToInt);
									PrenextES.isDeleted = 0;
									// tính webstatus
									ProductSO productso = productHelper.getProductSOById(ProductID, 2);
									if (productso != null) {

										if (productso.Prices == null || productso.Prices.isEmpty()) {
											PrenextES.webstatus = 0;
										} else {
											Object webstatus = productso.Prices.get("WebStatusId_3");
											if (webstatus == null) {
												PrenextES.webstatus = 0;
											} else {
												PrenextES.webstatus = Integer.valueOf(webstatus.toString());
											}
										}

									}
									var rs = ElasticClientWrite
											.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
											.IndexObject("ms_prenextproduct", PrenextES, PrenextES.productID + "");
								} /// end if stream

							}
						}

						// đây là sản phẩm đời trước
						// tìm xem đời sau là ai?
						PreProductList = product.listproductidpre + " ";
						PreProductList = PreProductList.replace(",", " ");
						isDeleted = product.isDeleted;
						ProductID = PreProductID;

					} else {
						// ngược lại sản phẩm đời sau.
						NextProductList += product.productID + " ";
						// NextProductSize++;
					}
				}

				if (ProductID > 0) {

					var ManuID = categoryHelper.getManufactureIDFromCache(PreProductID);
					// factoryRead.QueryFunction("product_getCategoryByProductID",
					// ProductBO[].class, false,PreProductID);

					PrenextProductSO PreSo = new PrenextProductSO();
					PreSo.previousProduct = PreProductList;
					PreSo.NextProduct = (NextProductList + " ");// .replace(",", " ");
					PreSo.isDeleted = isDeleted;
					PreSo.productID = ProductID;
					PreSo.ManuID = ManuID;
					ProductSO productso = productHelper.getProductSOById(ProductID, 2);

					if (productso != null) {

						if (productso.Prices == null || productso.Prices.isEmpty()) {
							PreSo.webstatus = 0;
						} else {
							Object webstatus = productso.Prices.get("WebStatusId_3");
							if (webstatus == null) {
								PreSo.webstatus = 0;
							} else {
								PreSo.webstatus = Integer.valueOf(webstatus.toString());
							}
						}

					}
					var rs = ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
							.IndexObject("ms_prenextproduct", PreSo, PreSo.productID + "");
				}
				r.Code = ResultCode.Success;
			}

		} catch (Throwable e) {
			// e.getStackTrace();
			e.printStackTrace();
			r.Code = ResultCode.Retry;
			Logs.LogException(e);
			return r;
		}
		return r;
	}

	private ResultCode DeletePreProduct(String PreProductID, ResultMessage r) {

		try {
			var data = PreProductID;
			if (data == null) {
				r.Code = ResultCode.Success;
				return r.Code;
			}

			var update = new UpdateRequest("ms_prenextproduct", String.valueOf(data))
					.doc("{\"IsDeleted\":1  }", XContentType.JSON).docAsUpsert(true).detectNoop(false);
			var client = ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
			UpdateResponse response = null;
			try {
				response = client.update(update, RequestOptions.DEFAULT);
			} catch (Exception e) {
				r.Code = ResultCode.Retry;
				e.printStackTrace();
			}
			if (response != null && response.getResult() == Result.UPDATED) {
				r.Code = ResultCode.Success;
			} else {

				// Logs.WriteLine("-Update isdeleted in PrenextProduct ES FAILED: PreProduct:#"
				// + data + ", " + response
				// + "######################");
				r.Code = ResultCode.Retry;
			}
		} catch (Exception e) {
			// Logs.WriteLine("Update isdeleted in ERRER, " + e);
			r.Code = ResultCode.Retry;

		}
		return r.Code;
	}


	private static int _DayLastUpdate = -1;


	@Override
	public ResultMessage RunScheduleTask() {
		Date now = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(now);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int day = calendar.get(Calendar.DAY_OF_WEEK );
		System.out.println("Oh yeah Oh yeah ");
		System.out.println("DoW: "+ day);
		System.out.println("hour: "+hour);

		if(DidxHelper.isNghia() ||  hour == 1 && (_DayLastUpdate < day || _DayLastUpdate == 7)) {
			// _View7DayLastUpdate tức là ngày cuối tuần thì hôm nay this.getday == 1
			_DayLastUpdate = day;
			// xử lý ở đây

			try {
				String queue = "";
				String queueBK = "";
				String queueDev = "";

				var listproductSO = productHelper.getListProductUpdate();
				if(listproductSO != null && listproductSO.size() > 0) {
					for (ProductSO productSO : listproductSO) {

						int h = Utils.GetQueueNum(productSO.ProductID);

						queue = "gr.dc4.didx.product" + h;
						queueBK = "gr.dc2.didx.product" + h;
						queueDev = "gr.beta.didx.product";



						MessageQueue nwmsg = new MessageQueue();
						String ProductCode = productSO.Prices != null && productSO.Prices.size() > 0 ? productSO.Prices.get("ProductCode_3") + "" : null;
						if(!Utils.StringIsEmpty(ProductCode)) {

							nwmsg.Action = DataAction.Update;
							nwmsg.ClassName = "ms.productse.ProductSE";
							nwmsg.CreatedDate = Utils.GetCurrentDate();
							nwmsg.Lang = "vi-VN";
							nwmsg.SiteID = productSO.SiteID;
							nwmsg.Type = 0;
							nwmsg.Data = "";
							nwmsg.DataCenter = clientConfig.DATACENTER;// == 3 ? 3 : 4;
							nwmsg.Identify = productSO.ProductID + "";
							//Logs.Log(true, "", " ");
							Logs.LogFile("updateproductse_schedule.txt", nwmsg.Identify + "|" +  nwmsg.SiteID );
							nwmsg.Note = "Update ProductSE Expired";
							QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev, nwmsg,
									true, "", nwmsg.DataCenter);

						}

					}
				}



			} catch (Throwable e) {
				e.printStackTrace();
			}


		}

		return null;
	}




	private List<String> BuildKeywordBaseOnShortName(String input)
    {
		if(Utils.StringIsEmpty(input)) return null;
        var words = new ArrayList<String>();
        //0. giu lai nguyen goc
        words.add(input);//0-0
                         //1. got 1, 2 ky tự đau
        if (input.length() > 4)
        {
            words.add(input.substring(0, input.length() - 1));//0-1
            words.add(input.substring(0, input.length() - 2));//0-2

            words.add(input.substring(1));//1-0
			words.add(input.substring(1, input.length() - 1));//1-1
            words.add(input.substring(1, input.length() - 2));//1-1
            
            if ((input.length() - 3) >= 1 ) {
            	words.add(input.substring(1, input.length() - 3));//1-2
            }

            words.add(input.substring(2));//2-0
            
            if ((input.length() - 3) >= 2 ) {
				words.add(input.substring(0, input.length() - 3));//0-1
				words.add(input.substring(1, input.length() - 3));//1-1
            	words.add(input.substring(2, input.length() - 3));//2-1

            }
            if ((input.length() - 4) >= 2 ) {
				words.add(input.substring(0, input.length() - 4));
				words.add(input.substring(1, input.length() - 4));
            	words.add(input.substring(2, input.length() - 4));//2-2
            }
            
        }


        //2.neu có 4 số => tách ra 4 số
        int numberDigit = (int) input.chars()
                .filter(Character::isDigit)
                .count();
        //.Count(x => Char.IsDigit(x));
        String temptPhraseDigit = "";
        if (numberDigit >= 4)
        {

            for (int i = 0; i < input.length(); i++)
            {
                if (Character.isDigit(input.charAt(i)))
                {
                    temptPhraseDigit += input.charAt(i);
                }
                else
                {
                    if (temptPhraseDigit.length() >= 4)
                    {
                        words.add(temptPhraseDigit);
                    }
                    temptPhraseDigit = "";
                }
            }

        }
        if (temptPhraseDigit.length() >= 4)//th no chi có số hoặc số ở cuối thì không rơi vào else ở trên
        {
            words.add(temptPhraseDigit);
        }
        return words;
    }
    private List<String> BuildKeywordSpecialRule(String input)
    {
    	if(Utils.StringIsEmpty(input)) return null;
        var words = new ArrayList<String>();
        if (input.length() == 4)
        {

            words.add(input.substring(0, 4));
        }
        else if (input.length() == 5)
        {

            words.add(input.substring(0, 4));
            words.add(input.substring(0, 5));

        }
        else if (input.length() >= 6)
        {

            words.add(input.substring(0, 4));
            words.add(input.substring(0, 5));
            words.add(input.substring(0, 6));
        }
        return words;
    }

}