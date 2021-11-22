package mwg.wb.pkg.product;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.business.CategoryHelper;
import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.ProductUrlHelper;
import mwg.wb.business.WorkerHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.graph.OrientDBFactory;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.CrmServiceHelper;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.IMessage;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.SqlInfoType;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.ProductCategoryBO;
import mwg.wb.model.products.ProductManuBO;
import mwg.wb.model.seo.ProductUrl;
import mwg.wb.model.seo.ProductUrlUpsert;
import org.apache.commons.lang3.ArrayUtils;
import org.elasticsearch.common.Strings;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SeoUrl implements Ididx {

	private OrientDBFactory factoryDB = null;
	private static CategoryHelper categoryHelper = null;
	private int DataCenter = 0;
	private ObjectMapper mapper = null;
	private ClientConfig clientConfig = null;
	private ProductHelper productHelper;

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {
		categoryHelper = (CategoryHelper) objectTransfer.categoryHelper;
		productHelper = (ProductHelper) objectTransfer.productHelper;
		mapper = (ObjectMapper) objectTransfer.mapper;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
	}

	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;
		//String strNOTE = message.Note + "";
		
		String strNOTE = message.Note + "DIDX_LOG|SEOURL";
		
		boolean isLog = strNOTE.contains("LOG");
		Logs.getInstance().Log(isLog, strNOTE, "SeoUrl", message);
//		String source = message.Source + "";
		int type = message.Type;
		int id = Utils.toInt(message.Identify);
		int siteID = message.SiteID;
		String langID = message.Lang;
		try {
			if (type == SqlInfoType.REQUIRE_RUN_SEOURL_CATE) {
				var cate = categoryHelper.getCategoryByIDFromCached(id, siteID, langID);
				if(cate == null) {
					r.Message = "CategoryID " + id + " not found";
					Logs.Log(isLog, strNOTE, r.Message);
					return r;
				}
				String url = ProductUrlHelper.GenSEOUrl(cate.URL, cate.CategoryName);
				var urlObj = new ProductUrl() {
					{
						CID = id;
					}
				};
				String json = mapper.writeValueAsString(urlObj);
				pushUpsert(clientConfig.SERVER_RABBITMQ_URL, json, url, siteID, message.Lang, isLog,
						strNOTE, DataCenter);
				Logs.Log(isLog, strNOTE, "CategoryID " + id + " url " + url + " value: " + json);
			}
			if (type == SqlInfoType.REQUIRE_RUN_SEOURL_MANU) {
				var manu = categoryHelper.getManuSeo(id, siteID, langID);
				if(manu == null || manu.ProductCategoryBO == null || manu.BigLogo == null) {
					r.Message = "ManufacturerID " + id + " not found";
					Logs.Log(isLog, strNOTE, r.Message);
					return r;
				}
				String url = ProductUrlHelper.GenManufacturerUrl(manu);

				// previous check
				var previousUrl = productHelper.getProductInfoByURL(siteID, url, langID);
				if (previousUrl != null && previousUrl.MID > 0) {
					var previousM = categoryHelper.getManuSeo(previousUrl.MID, siteID, langID);
					var previousparents = previousM.ProductCategoryBO != null
							&& previousM.ProductCategoryBO.ParentIdList != null
							? Arrays.stream(previousM.ProductCategoryBO.ParentIdList.split(","))
								.map(x -> Utils.toInt(x)).filter(x -> x > 0).collect(Collectors.toList())
							: null;
					if (previousM != null && previousUrl.MID > id
							&& ProductUrlHelper.GenManufacturerUrl(previousM).equals(url) && previousparents != null
							&& previousparents.contains(manu.ProductCategoryBO.CategoryID)) {
						r.Message = "ManufacturerID " + id + " duplicate url, skip...";
						Logs.Log(isLog, strNOTE, r.Message);
						return r;
					}
				}

				int categoryID = (int) manu.CategoryID;
				var urlObj = new ProductUrl() {
					{
						MID = id;
						CID = categoryID;
					}
				};
				String json = mapper.writeValueAsString(urlObj);
				pushUpsert(clientConfig.SERVER_RABBITMQ_URL, json, url, siteID, message.Lang, isLog,
						strNOTE, DataCenter);
				Logs.Log(isLog, strNOTE, "ManufacturerID " + id + " url " + url + " value: " + json);

				// parents
				String parent = manu.ProductCategoryBO.ParentIdList;
				parenturl: {
					int [] ids = new int[0];
					if (!Strings.isNullOrEmpty(parent)) {
						ids = Arrays.stream(parent.split(",")).mapToInt(x -> {
							try {
								return Integer.parseInt(x.trim());
							} catch (NumberFormatException ignored) {
								return -1;
							}
						}).toArray();
					}

					// them nganh hang lien quan vao
					var relate = productHelper.getCachedRelateCategories(categoryID, siteID, langID);
					ids = ArrayUtils.addAll(ids, relate);

					ProductCategoryBO[] parentcat;
					if (ids == null || ids.length == 0
							|| (parentcat = categoryHelper.getCategoryByIDList(ids, siteID, langID)) == null
							|| parentcat.length == 0) {
						break parenturl;
					}
					for (var cat : parentcat) {
						String urlP = ProductUrlHelper.GenManufacturerUrl(manu, cat);
						var currentMURL = productHelper.getProductInfoByURL(siteID, urlP, langID);
						ProductManuBO currentM = null;
						if (currentMURL == null || currentMURL.MID < id
								|| (currentM = categoryHelper.getManuSeo(currentMURL.MID, siteID, langID)) == null
								|| currentM.BigLogo == null || currentM.ProductCategoryBO == null
								|| (currentM.ProductCategoryBO.CategoryID != cat.CategoryID && currentMURL.MID == id)) {
							var urlObjP = new ProductUrl() {
								{
									MID = id;
									CID = (int) cat.CategoryID;
								}
							};
							String jsonP = mapper.writeValueAsString(urlObjP);
							pushUpsert(clientConfig.SERVER_RABBITMQ_URL, jsonP, urlP, siteID, message.Lang, isLog,
									strNOTE, DataCenter);
							Logs.Log(isLog, strNOTE, "ManufacturerID " + id + " url " + urlP + " value: "
									+ jsonP);
						}
					}
				}

				// prop
				var values = categoryHelper.getPropValueSeoByCate(categoryID, siteID, langID);
				if (values != null) {
					for (var value : values) {
						if (value.SmoothUrl == null || !value.IsSearch) continue;
						var urlObjV = new ProductUrl() {{
							PROPID = value.PropertyID;
							PROPVALUEID = value.ValueID;
							CID = categoryID;
							MID = id;
						}};
						String jsonV = mapper.writeValueAsString(urlObjV);
						String urlV = (url + "-" + value.SmoothUrl).toLowerCase();
						pushUpsert(clientConfig.SERVER_RABBITMQ_URL, jsonV, urlV, siteID, message.Lang, isLog,
								strNOTE, DataCenter);
						Logs.Log(isLog, strNOTE, "PropValueID " + value.ValueID + " url " + urlV
								+ " value: " + jsonV);
					}
				}
			}
			if (type == SqlInfoType.REQUIRE_RUN_SEOURL_PROP_VALUE) {
				var prop = categoryHelper.getPropValueSeo(id, siteID, langID);
				if (prop == null || (Strings.isNullOrEmpty(prop.SmoothUrl) && Strings.isNullOrEmpty(prop.urlFine))
						|| prop.productCategoryBO == null || !prop.IsSearch) {
					r.Message = "PropValueID " + id + " not found or null URL";
					Logs.Log(isLog, strNOTE, r.Message);
					return r;
				}

				var listProp = Strings.isNullOrEmpty(prop.productCategoryBO.ListProperty) ? null :
						Arrays.stream(prop.productCategoryBO.ListProperty.split(",")).map(x -> Utils.toInt(x))
								.filter(x -> x > 0).collect(Collectors.toList());

				if (listProp != null && !listProp.contains(prop.PropertyID)) {
					r.Message = "PropValueID " + id + " not in category proplist";
					Logs.Log(isLog, strNOTE, r.Message);
					return r;
				}

				String cateUrl = ProductUrlHelper.GenCategoryUrl(prop.productCategoryBO);
				String url = "";

				//xu li uu tien urlfine cho propvalue
				String vurl = prop.SmoothUrl; // !Utils.StringIsEmpty(prop.urlFine) ? prop.urlFine : prop.SmoothUrl;
				url = (cateUrl + "-" + vurl).toLowerCase();

				var urlObj = new ProductUrl() {
					{
						PROPID = prop.PropertyID;
						PROPVALUEID = prop.ValueID;
						CID = prop.productCategoryBO.CategoryID;
					}
				};
				String json = mapper.writeValueAsString(urlObj);
				pushUpsert(clientConfig.SERVER_RABBITMQ_URL, json, url, siteID, message.Lang, isLog,
						strNOTE, DataCenter);
				Logs.Log(isLog, strNOTE, "PropValueID " + id + " url " + url + " value: " + json);

				// manu
				var manus = categoryHelper.getManuSeoByCate(prop.productCategoryBO.CategoryID, siteID,
						langID);
				if (manus != null) {
					for (var manufacture : manus) {
						if (manufacture.BigLogo == null) continue;
						String manuUrl = ProductUrlHelper.GenSEOUrl(Utils.StringIsEmpty(manufacture.URL)
								? manufacture.ManufacturerName
								: manufacture.URL);
						String urlM = (cateUrl + "-" + manuUrl + "-" + vurl).toLowerCase();
						var urlObjM = new ProductUrl() {
							{
								PROPID = prop.PropertyID;
								PROPVALUEID = prop.ValueID;
								CID = prop.productCategoryBO.CategoryID;
								MID = (int) manufacture.ManufactureID;
							}
						};
						String jsonM = mapper.writeValueAsString(urlObjM);
						pushUpsert(clientConfig.SERVER_RABBITMQ_URL, jsonM, urlM, siteID, message.Lang, isLog,
								strNOTE, DataCenter);
						Logs.Log(isLog, strNOTE, "PropValueID " + id + " url " + urlM + " value: " + jsonM);
					}
				}
			}
		} catch(Throwable e) {
			e.printStackTrace();
			Logs.LogException(e);
			r.Code = ResultCode.Retry;
			r.Message = e.getMessage();
			r.StackTrace = Stream.of(e.getStackTrace()).map(x -> x.toString())
					.collect(Collectors.joining(", "));
			return r;
		}
		return r;

	}

	public static boolean pushUpsert(String rabiturl, String jsonOBject, String urlStr, int SiteID, String Lang,
			boolean isLog, String strNOTE, int DataCenter) throws Exception {

		ProductUrlUpsert rootObject = new ProductUrlUpsert() {
			{
				isdeleted = false;
				json = jsonOBject;
				url = urlStr;
				langid = Lang;
				siteid = SiteID;
				recordid = SiteID + "_" + DidxHelper.GenTerm3(Lang) + "_" + urlStr;
			}
		};

		String qu = "gr.dc4.sql.sysdata0";
		String qu2 = "gr.dc4.sql.sysdata0";
		String qubk = "gr.dc2.sql.sysdata0";
		String qudev = "gr.beta.sql.sysdata";
		QueueHelper.Current(rabiturl).pushUpsertODBObjects("hashurl", "recordid", qu, qu2, qubk, qudev,
				Arrays.asList(rootObject), isLog, strNOTE, DataCenter);
		Logs.Log(isLog, strNOTE, "Upsert url done: " + rootObject.url);
		return true;

	}

	@Override
	public ResultMessage RunScheduleTask() {
		return null;
	}

	// test
	public static void main(String[] args) throws Throwable {
		testPush();
//		testProcess();
	}

	public static void testPush() throws Throwable {
		var config = WorkerHelper.GetWorkerClientConfig();

		// dl live
		config.SERVER_RABBITMQ_URL = "amqp://tgdd:Tgdd2012@192.168.2.55:5672";
		config.SERVER_ORIENTDB_READ_URL1 = "remote:172.16.3.71:2424/web";
		config.ERP_SERVICES_URL = "http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx";
		config.ERP_SERVICES_AUTHEN = "werwerewrw32423!@4#123";
		config.CRM_SERVIVES_URL = "http://crm-services.thegioididong.com/NEW-CRMTGDD/CRMTGDDService.asmx";
		config.SERVER_ELASTICSEARCH_WRITE_HOST = "172.16.3.23";

		var oread = new ORThreadLocal();
		oread.initRead(config, 0, 2);
		var chelper = new CategoryHelper(oread, config);
		var phelper = new ProductHelper(oread, config);

		int siteid = 2;

		var props = Arrays.stream(chelper.GetProductPropByCategory(44, siteid, "vi-VN", true))
				.flatMapToInt(x -> x.ProductPropValueBOLst.stream().mapToInt(y -> y.ValueID)).toArray();

		var manu = Arrays.stream(phelper.GetManuByCategoryID(3305, 2, "vi-VN"))
				.mapToInt(x -> (int) x.ManufactureID).toArray();

		for (var id : manu) {
//			int prop = 1558;
			MessageQueue nwmsg = new MessageQueue();
			nwmsg.Action = IMessage.DataAction.Update;
			nwmsg.ClassName = "mwg.wb.pkg.product.SeoUrl";
			nwmsg.CreatedDate = Utils.GetCurrentDate();
			nwmsg.Lang = "vi-VN";
			nwmsg.SiteID = siteid;
			nwmsg.DataCenter = 2;
			nwmsg.Identify = id + "";
			nwmsg.Type = SqlInfoType.REQUIRE_RUN_SEOURL_MANU;
			nwmsg.Source = "product_manu_lang";
			nwmsg.Note = "DIDX_LOG|testurl|";

			// gr.dc4.didx.product
			// gr.beta.didx.product
			QueueHelper.Current(config.SERVER_RABBITMQ_URL).PushPriority("gr.dc" + nwmsg.DataCenter
							+ ".didx.product2", nwmsg, 10);
			System.out.println("done: " + id);
		}

		System.out.print("cuc cu");
	}

	public static void testProcess() throws IOException {
		var config = WorkerHelper.GetWorkerClientConfig();

		// dl live
		config.SERVER_RABBITMQ_URL = "amqp://tgdd:Tgdd2012@192.168.2.55:5672";
		config.SERVER_ORIENTDB_READ_URL1 = "remote:172.16.3.71:2424/web";
		config.ERP_SERVICES_URL = "http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx";
		config.ERP_SERVICES_AUTHEN = "werwerewrw32423!@4#123";
		config.CRM_SERVIVES_URL = "http://crm-services.thegioididong.com/NEW-CRMTGDD/CRMTGDDService.asmx";
		config.SERVER_ELASTICSEARCH_WRITE_HOST = "172.16.3.23";
		config.SERVER_ELASTICSEARCH_READ_HOST = "172.16.3.23";

		var oread = new ORThreadLocal();
		oread.initRead(config, 0, 2);
		var priceHelper = new PriceHelper(oread, config);
		var phelper = new ProductHelper(oread, config);
		var erp = new ErpHelper(config.ERP_SERVICES_URL, config.ERP_SERVICES_AUTHEN);
		var mapper = new ObjectMapper();
		var df = new SimpleDateFormat(GConfig.DateFormatString);
		var cate = new CategoryHelper(oread, config);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		var m = new MessageQueue() {
			{
				// X13|0|0160015000771|1
				Identify = "7583";
				Note = "";
				SiteID = 2;
				Lang = "vi-VN";
				DataCenter = 2;
//				Source = "product_propvalue_lang";
				Type = SqlInfoType.REQUIRE_RUN_SEOURL_MANU;
//				BrandID = 1;
//				Note = "VERSION2";
			}
		};
		var tools = new ObjectTransfer();
		tools.erpHelper = erp;
		tools.mapper = mapper;
		tools.factoryRead = oread;
		tools.clientConfig = config;
		tools.productHelper = phelper;
		tools.priceHelper = priceHelper;
		tools.crmHelper = new CrmServiceHelper(config);
		tools.categoryHelper = cate;
		var pkg = new SeoUrl();
		pkg.InitObject(tools);
		pkg.Refresh(m);
		System.out.print("cuc cu");
	}
}
