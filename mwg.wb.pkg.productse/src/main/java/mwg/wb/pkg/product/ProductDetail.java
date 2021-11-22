package mwg.wb.pkg.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import mwg.wb.business.ProductHelper;
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
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductCombo;
import mwg.wb.model.products.ProductDetailBO;
import mwg.wb.model.search.ProductSO;
import mwg.wb.model.search.Prop;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProductDetail implements Ididx {

	private String CurrentIndexDB = "";
	private static ProductHelper productHelper = null;
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

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {
		productHelper = (ProductHelper) objectTransfer.productHelper;
//		owriter = (ORThreadLocal) objectTransfer.factoryWrite;
		mapper = (ObjectMapper) objectTransfer.mapper;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		CurrentIndexDB = clientConfig.ELASTICSEARCH_PRODUCT_INDEX;
		erpHelper = (ErpHelper) objectTransfer.erpHelper;
		notifyHelperLog = (LineNotify) objectTransfer.notifyHelperLog;
	}

	private LineNotify notifyHelperLog = null;
	private final Lock queueLock = new ReentrantLock();

	public void PushRefresh(MessageQueue message, int ProductID, int siteid, String lang, boolean isLog)
			throws Throwable {


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
			notifyHelperLog.NotifyInfo("PRODUCTSE:" + strNOTE);
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
		try {
		ProductDetailBO[] detailList =productHelper.getFullProductDetail(ProductID, message.SiteID, message.Lang,
				false , new CodeTimers()).list;

		MessageQueue messageRepushV2 = new MessageQueue();
		messageRepushV2.SqlList = new ArrayList<SqlInfo>();
		messageRepushV2.SiteID = message.SiteID;

		String data = mapper.writeValueAsString(detailList);

		SqlInfo sqlinfo0 = new SqlInfo();
		String recordid = ProductID + "_" + SiteID + "_" + LangID;
		sqlinfo0.Sql = "update product_detail_cache set data=:data,langid=:langid ,productid=:productid,recordid=:recordid,siteid=:siteid upsert where recordid='"
				+ recordid + "'";
		sqlinfo0.tablename = "product_detail_cache";
		sqlinfo0.tablekey = "recordid";
		sqlinfo0.Params = new HashMap<String, Object>();
		sqlinfo0.Params.put("recordid", recordid);
		sqlinfo0.Params.put("data", data);
		sqlinfo0.Params.put("langid", message.Lang);
		sqlinfo0.Params.put("productid", ProductID);
		sqlinfo0.Params.put("siteid", SiteID);

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

		String qu = "gr.dc4.sql.sysdata0";
		String qu2 = "gr.dc4.sql.sysdata0";
		String qubk = "gr.dc2.sql.sysdata0";
		String qudev = "gr.beta.sql.sysdata";
		int hsa = Utils.GetQueueNum(ProductID);
		if (hsa > 0) {
			qu = "gr.dc4.sql.sysdata" + hsa;
			qu2 = "gr.dc4.sql.sysdata" + hsa;
			qubk = "gr.dc2.sql.sysdata" + hsa;
			qudev = "gr.beta.sql.sysdata";
			messageRepushV2.Hash = ProductID;
		} else {
			qu = "gr.dc4.sql.sysdata0";
			qu2 = "gr.dc4.sql.sysdata0";
			qubk = "gr.dc2.sql.sysdata0";
			qudev = "gr.beta.sql.sysdata";

		}

		messageRepushV2.Action = DataAction.Update;
		messageRepushV2.ClassName = "ms.upsert.Upsert";
		messageRepushV2.CreatedDate = Utils.GetCurrentDate();
		messageRepushV2.Lang = message.Lang;
		messageRepushV2.SiteID = SiteID;
		messageRepushV2.Source = "didx_Product_Detail";
		messageRepushV2.RefIdentify = message.Identify;
		messageRepushV2.Identify = String.valueOf(ProductID);
		messageRepushV2.Hash = ProductID;
		messageRepushV2.Note = message.Note;

		messageRepushV2.DataCenter = message.DataCenter;
		QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(qu, qu2, qubk, qudev, messageRepushV2, isLog,
				message.Note, message.DataCenter);  
		Logs.Log(isLog, message.Note, "Push product_detail_cache " + ProductID);
		resultMessage.Code = ResultCode.Success;
		} catch (Throwable e) {
			Logs.LogException(e);
			resultMessage.Code = ResultCode.Retry;
			resultMessage.Message = e.getMessage();
			resultMessage.StackTrace = Stream.of(e.getStackTrace()).map(x -> x.toString())
					.collect(Collectors.joining(", "));
			return resultMessage;

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
			List<ProductDetailBO> listdetail, List<ProductCombo> listCombo) throws Throwable {
		ResultMessage resultMessage = new ResultMessage();
//		var rs = true;

		var ProductLangBO = ProductBO.ProductLanguageBO;
		var ProductManuLangBO = ProductBO.ProductManuLangBO;
		var ProductCateLangBO = ProductBO.ProductCategoryLangBO;

		if (ProductLangBO == null) {
			resultMessage.Code = ResultCode.NotFound;
			resultMessage.Message = "Not exits product_lang for" + ProductID + ". SKIP init...";
			System.out.println("Not exits product_lang for" + ProductID + ". SKIP init...");
			Logs.Log(isLog, StrNote, "Not exits product_lang for" + ProductID + ". SKIP init...");

			return resultMessage;
		}
		int CategoryID = (int) (ProductBO.ProductCategoryBO != null ? ProductBO.ProductCategoryBO.CategoryID : 0);
		int ManufactureID = (int) (ProductManuLangBO != null ? ProductManuLangBO.ManufacturerID : 0);

		String ProductName = ProductLangBO.ProductName;
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
		document.IsDeleted = 0;
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

		// sold count
		long to = System.currentTimeMillis();
		long from = to - 86400000L * 7;
		Integer count = WebserviceHelper.Call(clientConfig.DATACENTER).Get(
				"apiproduct/getsoldproductquantity?siteID=:?&productID=:?&fromDate=:?&toDate=:?", Integer.class, SiteID,
				ProductID, from, to);
		document.ProductSoldCount = count == null ? 0 : count;

		document.didx_updateddate = Utils.GetCurrentDate();
		document.didx_source = "SE";

		String strpro = "";

//		String strproSE = "";
//		String strprovalkeyword = "";
		String strprovalkeywordnew = "";
		String newkeyword = "";

		Map<String, Object> myProp = new HashMap<String, Object>();
		List<Prop> Props = new ArrayList<Prop>();

		if (listdetail != null && !listdetail.isEmpty()) {
			for (var row : listdetail) {
				// Prop_27
				var _propvalKey = "prop_" + row.PropertyID;
				var _propvalValue = row.CompareValue;
				// if (row.IsSearch) { //thuco tinh yes no
				myProp.put(_propvalKey, _propvalValue);
				// }

				if (row.PropValueID > 0 && row.IsSearch == true) {
					Prop p = new Prop();
					if (propIDbyCate.contains(row.PropertyID)) {
						p.propkey = Utils.toDouble((100000 + row.PropertyID) + "" + row.PropValueID);

						Props.add(p);
					}
					strpro = strpro + " prop" + row.PropertyID + "_" + row.PropValueID;

				}
				//
				if (!Utils.StringIsEmpty(row.PropValue)) {
					strpro = strpro + " " + "prop" + row.PropertyID + "_" + DidxHelper.GenTerm3(row.PropValue);
				}

				if (row.PropIssearch) {

					var kw = !Utils.StringIsEmpty(row.PropValue) ? row.PropValue.replace("_", " ")
							: "" + " " + (!Utils.StringIsEmpty(row.MetaKeyWord) ? "" : row.MetaKeyWord + " ");
					strprovalkeywordnew = strprovalkeywordnew + kw;
				}

			}
		}
		document.PropStr = strpro;
		document.listproperty = Props;

		newkeyword += (ProductCateLangBO != null ? ProductCateLangBO.CategoryName : "") + " "
				+ (ProductManuLangBO != null ? ProductManuLangBO.ManufacturerName : "") + " " + ProductName;

		if (!Utils.StringIsEmpty(ProductLangBO.KeyWord)) {

			newkeyword += " " + ProductLangBO.KeyWord;
			newkeyword += " " + ProductLangBO.MetaKeyWord;
		}

		document.NewKeyword = DidxHelper
				.FilterVietkey(DidxHelper.FormatKeywordSearchField(newkeyword + " " + strprovalkeywordnew));

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
		if (!Utils.StringIsEmpty(ProductLangBO.ShortName)) {
			if ((CategoryID != 42 && CategoryID != 44 && CategoryID != 522)
					|| !ProductLangBO.ShortName.toLowerCase().contains("thẻ nhớ")
							&& !ProductLangBO.ShortName.toLowerCase().contains("sạc dự phòng")) {
				keyword += ProductLangBO.ShortName;
			}
		}
		if (!Utils.StringIsEmpty(ProductLangBO.SEOName)) {
			keyword += " " + ProductLangBO.SEOName;
		}
		if (!Utils.StringIsEmpty(ProductLangBO.KeyWord))
			keyword += " , " + ProductLangBO.KeyWord + " , ";

		// keyword tskt
		if (listdetail != null && !listdetail.isEmpty()) {
			for (var element : listdetail) {
				var kw = "";
				if (!Utils.StringIsEmpty(element.PropValue))
					kw = element.PropValue.replace("_", " ") + " ";
				if (!Utils.StringIsEmpty(element.MetaKeyWord))
					kw += element.MetaKeyWord + " ";
				keyword += kw + " ";
			}
		}

		// từ khóa sai chính ta hãng
		if (ProductManuLangBO != null && !Utils.StringIsEmpty(ProductManuLangBO.MetaKeyWord)) {
			// if (objPPL.Product.ProductManuBO.IsActived == true)
			{
				keyword += " " + ProductManuLangBO.MetaKeyWord;
			}

		}

		String keywordRemoveSpace = DidxHelper.FormatKeywordSearchField(ProductName);
		keyword = SiteID == 6 ? DidxHelper.FormatKeywordSearchFieldCam(keyword)
				: DidxHelper.FormatKeywordSearchField(keyword);

		document.Keyword = DidxHelper.FilterVietkey(keyword) + " " + keywordRemoveSpace;

		document.ManufactureID = ManufactureID;

		document.CategoryID = CategoryID;

		var sticker = productHelper.getStickerLabel(ProductID, CategoryID, SiteID, ProductLangBO.LanguageID);
		if (sticker != null && sticker.icon != null) {
			document.StickerLabel = sticker.icon + "|" + sticker.valueid;
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

		// int SliderStatus = 0;
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

		if (ProductName != null) {
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
		if (ProductBO.ProductCode != null) {
			var sale = productHelper.getSpecialSaleProgram(ProductBO.ProductCode, 1);
			if (sale != null && !Strings.isNullOrEmpty(sale.SpecialSaleProgramName) && sale.BeginDate.before(now)
					&& sale.EndDate.after(now)) {
				document.IsOnlineOnly = true;
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

		// tra gop 0%
		var installment = productHelper.GetInstallmentByProduct(ProductID, SiteID, ProductLangBO.LanguageID,
				ProductBO.CategoryID, (int) ProductBO.ManufactureID);
		if (installment != null) {
			document.IsPayment = installment.IsPayment;
			document.PaymentFromDate = installment.FromDate;
			document.PaymentToDate = installment.ToDate;
			document.PercentInstallment = installment.PercentInstallment;
		}

		var result = ElasticClientWrite.getInstance(clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST)
				.IndexObject(CurrentIndexDB, document, esKeyTerm);
		if (result == false) {
			resultMessage.Code = ResultCode.Retry;
		}
		return resultMessage;

	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}
}