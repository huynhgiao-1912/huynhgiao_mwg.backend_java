package mwg.wb.client.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mwg.wb.client.BooleanTypeAdapter;
import mwg.wb.common.FileHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.products.ProductCombo;
import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.model.products.PromotionListGroupErp;
import mwg.wb.model.promotion.Promotion;
import mwg.wb.model.promotion.PromotionOldProductBO;
import mwg.wb.model.sim.SimBO;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
 
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErpHelper {
	private String strAuthen = "werwerewrw32423!@4#123";
	private String strUrl = "http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx";
	private Gson gson = null;
	private ObjectMapper mapper = null;
	private String fnServiceLog = "erpservice.txt";

	public ErpHelper(String url, String authen) {
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper = new ObjectMapper();
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		gson = new GsonBuilder().serializeNulls().setFieldNamingStrategy(f -> f.getName().toLowerCase())
				.setDateFormat(GConfig.DateFormatString).registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
				.create();
		if (!Utils.StringIsEmpty(url)) {
			strUrl = url;
			strAuthen = authen;
		}
	}

	public String CallService(String SOAPAction, String body, String Node, int siteid) throws Exception {

		StringEntity stringEntity = new StringEntity(body, "UTF-8");

		stringEntity.setChunked(true);
		HttpPost httpPost = new HttpPost(strUrl);
		httpPost.setEntity(stringEntity);
		httpPost.addHeader("Accept", "text/xml");
		httpPost.addHeader("Content-Type", "text/xml; charset=utf-8");
		httpPost.addHeader("SOAPAction", "http://tempuri.org/" + SOAPAction);
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
//		HttpClient httpClient = null;
//		HttpResponse response = null;
		String strResponse = null;
		try {

			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5 * 60 * 1000).build();
			httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

			// httpClient = new DefaultHttpClient();

			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				strResponse = EntityUtils.toString(entity);
				//loi SOAP, sai file,sai nodename <?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema"><soap:Body><soap:Fault><faultcode>soap:Server</faultcode><faultstring>Server was unable to process request. ---&gt; Object reference not set to an instance of an object.</faultstring><detail /></soap:Fault></soap:Body></soap:Envelope>
				
				Logs.LogFactoryMessage("SOAP-" + Node, "\r\n========" + Utils.GetCurrentDate() + "========\r\n" + body
						+ ":" + strResponse + "\r\n================\r\n", siteid);
				if (strResponse
						.contains("<MessageDetail>Oracle.DataAccess.Client.OracleException ORA-01403: no data found")) {
					List<JsonObject> lsa = new ArrayList<JsonObject>();
					return gson.toJson(lsa);
				}
				if (strResponse.contains("<soap:Body><soap:Fault><faultcode>soap:Server</faultcode><faultstring>")  ) {
					throw new Exception("<soap:Body><soap:Fault><faultcode>soap:Server</faultcode><faultstring>");
				}
				
				if (strResponse.contains("<IsError>true</IsError>")) {

					Logs.LogException(fnServiceLog, strResponse);
					Logs.LogException(fnServiceLog, body);
					throw new Exception("Exception GET RS=ISERROR PROMOTION ERP-RERTRY");
				}
				// System.out.print(strResponse);
				StringReader sr = new StringReader(strResponse);
				// String xml = strResponse;
				// String example = strResponse;
				SOAPMessage message = MessageFactory.newInstance().createMessage(null,
						new ByteArrayInputStream(strResponse.getBytes()));

				Document document = message.getSOAPBody().extractContentAsDocument();
				NodeList list = document.getElementsByTagName(Node);

				Map<String, String> lsType = new HashMap<String, String>();
				try {
					if (document != null && document.getElementsByTagName("xs:sequence") != null
							&& document.getElementsByTagName("xs:sequence").getLength() > 0) {
						NodeList listSch = document.getElementsByTagName("xs:sequence").item(0).getChildNodes();

						int l2 = listSch != null ? listSch.getLength() : 0;
						for (int i = 0; i < l2; i++) {
							NamedNodeMap nodeMap = listSch.item(i).getAttributes();
							String nodeName = nodeMap.getNamedItem("name").getNodeValue().toLowerCase();
							String nodeType = StringUtils.replace(nodeMap.getNamedItem("type").getNodeValue(), "xs:",
									"");

							lsType.put(nodeName, nodeType);
						}
					}

				} catch (Exception ex) {
					ex.printStackTrace();
					Logs.LogException(fnServiceLog, ex);
					Logs.LogException(fnServiceLog, body);
					throw ex;
				}
				int l = list.getLength();
				// xs:decimal xs:long xs:short xs:longxs:stringxs:shortxs:decimal xs:dateTime
				// xs:dateTime
				// xs:stringxs:stringxs:stringxs:stringxs:stringxs:shortxs:stringxs:decimalxs:longxs:shortxs:decimalxs:decimalxs:shortxs:stringxs:shortxs:stringxs:shortxs:decimalxs:decimalxs:decimalxs:stringxs:decimalxs:decimal
				// xs:stringxs:shortxs:stringxs:stringxs:decimalxs:decimalxs:shortxs:stringxs:intxs:stringxs:decimalxs:shortxs:decimalxs:decimalxs:string
				List<JsonObject> ls = new ArrayList<JsonObject>();
				for (int i = 0; i < l; i++) {

					NodeList listc = list.item(i).getChildNodes();
					int lc = listc.getLength();
					JsonObject json = new JsonObject();
					// JSONObject
					for (int j = 0; j < lc; j++) {
						// String a = listc.item(j).getAttributes("type").to
						String cl = listc.item(j).getNodeName().toLowerCase();
						String ct = listc.item(j).getTextContent();
						String stype = "";
						if (lsType.containsKey(cl)) {
							stype = lsType.get(cl);
						}
						if (stype.equals("short")) {
							var vl = Integer.valueOf(ct);
							json.addProperty(cl, vl);

						} else if (stype.equals("decimal")) {
							var vl = Double.valueOf(ct);
							json.addProperty(cl, vl);

						} else if (stype.equals("long")) {
							var vl = Double.valueOf(ct).longValue();
							json.addProperty(cl, vl);

						} else if (stype.equals("dateTime")) {

							// yyyy-MM-dd HH:mm:ss

							Date date1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(ct);
//							if(date1 == null) {
//								//2013-07-31T11:59:32+07:00
//								//2016-08-18T18:52:16.513805+07:00
//								var reformatTime = ct.split(".")[0] + "+07:00";
//								date1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(reformatTime);
//							}
							var vl = Utils.FormatDateForGraph(date1);
							json.addProperty(cl, vl);

						} else if (stype.equals("string")) {

							json.addProperty(cl, ct);

						} else {
							json.addProperty(cl, ct);

						}
						// s = s + "\"" + listc.item(j).getNodeName().toLowerCase() + "\":\"" +
						// listc.item(j).getTextContent() + "\",";
					}
					ls.add(json);

					// s = payload + " ";
				}
				// s = StringUtils.strip(s, ",") + "]";
				return gson.toJson(ls);

			}
		} catch (Exception e) {
			Logs.LogException(fnServiceLog, e);
			Logs.LogException(fnServiceLog, body);
			Logs.LogException(fnServiceLog, "strUrl:" + strUrl);
			// Logs.LogException(fnServiceLog,"sr:" +str);
			Logs.LogException(fnServiceLog, strResponse);
			throw e;
		} finally {

			try {
				response.close();
			} finally {

			}

			try {
				httpClient.close();
			} finally {

			}
		}
		return "_ERR_";
	}

	public String GetPriceByProductCodeStr(int intPriceAreaID, String strProductCode, int intOutputTypeID,
			int intCompanyID, int siteid) throws Exception {
		String body = "";
		try {
			body = FileHelper.ReadAllText(Utils.getCurrentDir() + "soapxml/GetPriceByProductID.xml");
		} catch (IOException e1) {
			e1.printStackTrace();
			throw e1;
		}
//		try {
//			body = FileHelper.ReadAllText("D:\\TheGioiDiDong\\_GITMicroservice\\mwg.backend-java\\mwg.wb.client\\SoapRequestToErpService.xml");
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
		if (Utils.StringIsEmpty(body)) {
//	throw    Exception("");

		}
		String SOAPAction = "GetPriceByProductID";

		body = body.replace("{strAuthen}", strAuthen).replace("{intPriceAreaID}", Integer.toString(intPriceAreaID))
				.replace("{strProductCode}", strProductCode.trim())
				.replace("{intOutputTypeID}", Integer.toString(intOutputTypeID))
				.replace("{intCompanyID}", Integer.toString(intCompanyID));
		String a = CallService(SOAPAction, body, "WEB_GETPRICEBYPRODUCTID", siteid);
		Logs.LogFactoryMessage("pricesoap", body + ":" + a + "\r\n================\r\n", siteid);

		return a;

	}

	public ProductErpPriceBO[] GetPriceByProductCode(int intPriceAreaID, String strProductCode, int intOutputTypeID,
			int intCompanyID, int siteid) throws Throwable {
		String json = GetPriceByProductCodeStr(intPriceAreaID, strProductCode, intOutputTypeID, intCompanyID, siteid);

		return gson.fromJson(json, ProductErpPriceBO[].class);

	}

	public String GetPromotionByPrdIDStr(String prdCode, int ProvinceID, int OutputTypeID, double SalePrice, int siteid)
			throws Exception {

		String body = "";

		body = FileHelper.ReadAllText(Utils.getCurrentDir() + "soapxml/GetPromotionByPrdID.xml");

		String SOAPAction = "GetPromotionByPrdID";

		body = body.replace("{strAuthen}", strAuthen).replace("{ProductID}", prdCode.trim())
				.replace("{ProvinceID}", Integer.toString(ProvinceID))
				.replace("{OutputTypeID}", Integer.toString(OutputTypeID))
				.replace("{SalePrice}", Double.toString(SalePrice)).replace("{SiteID}", Integer.toString(siteid));

		long t1 = System.currentTimeMillis();
		String a = CallService(SOAPAction, body, "PM_PROMOTION_WEBGET", siteid);
		long detal = System.currentTimeMillis() - t1;
		if (detal > 1000) {
			Logs.LogFactoryMessage("GetPromotionByPrdID-slow",
					SOAPAction + ":" + detal + body + "\r\n================\r\n", siteid);
		}
		Logs.LogFactoryMessage("promotionsoap",
				"\r\n========" + Utils.GetCurrentDate() + "========\r\n" + body + ":" + a + "\r\n================\r\n",
				siteid);

		return a;

	}

	public SimBO GetInfoSIMByIMEI(String SimID) throws JsonSyntaxException, Exception {
		String body = "";

		body = FileHelper.ReadAllText(Utils.getCurrentDir() + "soapxml/GetInfoSIMByIMEI.xml");

		String SOAPAction = "GetInfoSIMByIMEI";
		body = body.replace("{strAuthen}", strAuthen).replace("{strSIMID}", SimID);
		return Arrays.stream(gson.fromJson(CallService(SOAPAction, body, "Web_SIM_SELECT", 999), SimBO[].class))
				.findFirst().orElse(null);
	}

	public PromotionOldProductBO[] GetProductOldPromotionByImei(String productCode, String IMEI, int storeID,
			int inventoryStatusID, int siteID) throws JsonSyntaxException, Exception {
		String body = "";

		body = FileHelper.ReadAllText(Utils.getCurrentDir() + "soapxml/GetPromotionByCodeDMX.xml");

		String SOAPAction = "GetPromotionByCodeDMX";
		body = body.replace("{strAuthen}", strAuthen).replace("{strProductID}", productCode.trim())
				.replace("{strIMEI}", IMEI).replace("{intStoreID}", storeID + "")
				.replace("{intInventoryStatusID}", inventoryStatusID + "").replace("{intSiteID}", siteID + "");
		String a = CallService(SOAPAction, body, "WEBOLD_GETPROMOBYCODE", siteID);
		if (!Strings.isNullOrEmpty(a)) {
			return gson.fromJson(a, PromotionOldProductBO[].class);
		}
		return null;
	}

	public PromotionOldProductBO[] GetProductOldPromotionByCode(String productCode, String IMEI, int storeID,
			int inventoryStatusID, int siteID) throws JsonSyntaxException, Exception {
		String body = "";
		try {
			body = FileHelper.ReadAllText(Utils.getCurrentDir() + "soapxml/GetPromotionByCode.xml");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String SOAPAction = "GetPromotionByCode";
		body = body.replace("{strAuthen}", strAuthen).replace("{ProductID}", productCode.trim()).replace("{Imei}", IMEI)
				.replace("{StoreID}", storeID + "").replace("{InventoryStatusID}", inventoryStatusID + "")
				.replace("{SiteID}", siteID + "");
		return gson.fromJson(CallService(SOAPAction, body, "WEBOLD_GETPROMOTIONBYCODE", siteID),
				PromotionOldProductBO[].class);
	}

	public Promotion[] GetPromotionByPrdID(String prdCode, int ProvinceID, int OutputTypeID, double SalePrice,
			int siteid) throws Exception {
		String json = GetPromotionByPrdIDStr(prdCode, ProvinceID, OutputTypeID, SalePrice, siteid);

		return gson.fromJson(json, Promotion[].class);

	}

//	public PromotionErpBO[] ErpGetPromotionByPrdID(String prdCode, int ProvinceID, int OutputTypeID, double SalePrice,
//			int siteid) throws Exception {
//		String json = GetPromotionByPrdIDStr(prdCode, ProvinceID, OutputTypeID, SalePrice, siteid);
//		List< mwg.wb.model.promotion.Promotion> ra = new ArrayList< mwg.wb.model.promotion.Promotion>();
//		
//		 mwg.wb.model.promotion.PromotionErpBO[] allList= gson.fromJson(json, PromotionErpBO[].class);
//		for (PromotionErpBO promotionErpBO : allList) {
//			mwg.wb.model.promotion.Promotion n=new mwg.wb.model.promotion.Promotion();
//			n.a
//			ra.add(e);
//		}
//
//	}
	public ProductCombo[] GetProductComboProductByComboID(String comboproductidlist)
			throws JsonSyntaxException, Exception {

		String body = "";

		body = FileHelper.ReadAllText(Utils.getCurrentDir() + "soapxml/GetProductComboProductByComboID.xml");

		String SOAPAction = "GetProductComboProductByComboID";
		body = body.replace("{strAuthen}", strAuthen).replace("{strProductComboID}", comboproductidlist);
		return (gson.fromJson(CallService(SOAPAction, body, "ProductComboProduct", 99), ProductCombo[].class));

	}

	public PromotionListGroupErp[] GetPromotionListGroupByPromoID(int promotionID, int siteid)
			throws JsonSyntaxException, Exception {

		String body = "";

		body = FileHelper.ReadAllText(Utils.getCurrentDir() + "soapxml/GetPromotionListGroupByPromoID.xml");

		String SOAPAction = "GetPromotionListGroupByPromoID";
		body = body.replace("{strAuthen}", strAuthen).replace("{intPromotionID}", String.valueOf(promotionID));
		long t1 = System.currentTimeMillis();
		String a = CallService(SOAPAction, body, "ERP.WEB_PROMOTIONLIST_GETPROMOID", siteid);
		long detal = System.currentTimeMillis() - t1;
		if (detal > 800) {
			Logs.LogFactoryMessage("PromotionListGroupErp", "pro " + promotionID + ":" + detal + "\r\n", siteid);
		}

		return (gson.fromJson(a, PromotionListGroupErp[].class));

	}

	public PromotionOldProductBO[] GetPromotionByPrdIDNew(String prdCode, String imei, int storeID, int ProvinceID,
			int siteid) throws Exception {

		String body = "";
		if (siteid <= 0) {
			siteid = 1;
		}

		body = FileHelper.ReadAllText(Utils.getCurrentDir() + "soapxml/GetPromotionByPrdIDNew.xml");
//		body = GetPromotionByPrdIDNew;

		String SOAPAction = "GetPromotionByPrdIDNew";

		body = body.replace("{strAuthen}", strAuthen).replace("{ProductID}", prdCode.trim())
				.replace("{ProvinceID}", ProvinceID + "").replace("{StoreID}", storeID + "").replace("{Imei}", imei)
				.replace("{SiteID}", Integer.toString(siteid));

		long t1 = System.currentTimeMillis();
		String a = CallService(SOAPAction, body, "PM_PROMOTION_WEBGETNEW", siteid);
		long detal = System.currentTimeMillis() - t1;
		if (detal > 1000) {
			Logs.LogFactoryMessage("GetPromotionByPrdIDNew-slow",
					SOAPAction + ":" + detal + body + "\r\n================\r\n", siteid);
		}
		Logs.LogFactoryMessage("promotionsoap",
				"\r\n========" + Utils.GetCurrentDate() + "========\r\n" + body + ":" + a + "\r\n================\r\n",
				siteid);

		return gson.fromJson(a, PromotionOldProductBO[].class);

	}

	public double GetRefSalePriceOfProduct(String productCode, int intBrandID, int inventoryStatusID, int siteid)
			throws Exception {

		double gia = -1;

		String body = "";
		body = FileHelper.ReadAllText(Utils.getCurrentDir() + "soapxml/GetRefSalePriceOfProduct.xml");
//		body = GetRefSalePriceOfProduct;
		String SOAPAction = "GetRefSalePriceOfProduct";

		body = body.replace("{strAuthen}", strAuthen).replace("{strProductID}", productCode.trim())
				.replace("{intBrandID}", intBrandID + "").replace("{intInventoryStatusID}", inventoryStatusID + "");

		long t1 = System.currentTimeMillis();
		String a = CallService(SOAPAction, body, "GetRefSalePriceOfProductResult", siteid);
		if (a.contains("[{\"#text\":\"")) {
			a = a.replace("[{\"#text\":\"", "");
			a = a.substring(0, a.length() - 3);
		}
		gia = Utils.toDouble(a.trim());
		long detal = System.currentTimeMillis() - t1;
		if (detal > 1000) {
			Logs.LogFactoryMessage("GetPromotionByPrdIDNew-slow",
					SOAPAction + ":" + detal + body + "\r\n================\r\n", siteid);
		}
		Logs.LogFactoryMessage("promotionsoap",
				"\r\n========" + Utils.GetCurrentDate() + "========\r\n" + body + ":" + a + "\r\n================\r\n",
				siteid);

		System.out.println(a);

		return gia == 0 ? -1 : gia;

	}

	public static String GetPromotionByPrdIDNew = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
			+ "<soap:Envelope\r\n" + "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
			+ "	xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\r\n"
			+ "	xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" + "	<soap:Body>\r\n"
			+ "		<GetPromotionByPrdIDNew\r\n" + "			xmlns=\"http://tempuri.org/\">\r\n"
			+ "			<strAuthen>{strAuthen}</strAuthen>\r\n" + "			<objResultMessage>\r\n"
			+ "				<IsError>false</IsError>\r\n" + "				<ErrorType>GetData</ErrorType>\r\n"
			+ "				<Message>string</Message>\r\n" + "				<MessageDetail>string</MessageDetail>\r\n"
			+ "				<ErrorCode>0</ErrorCode>\r\n" + "				<Parram>string</Parram>\r\n"
			+ "			</objResultMessage>\r\n" + "			<objKeywords>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">@ProductID</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">{ProductID}</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">@ProvinceID</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">{ProvinceID}</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">@Imei</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">{Imei}</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">@SiteID</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">{SiteID}</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">@StoreID</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">{StoreID}</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">@OutputTypeID</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">0</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">@SalePrice</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">0</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">@Inventorystatusid</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">2</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">@Top</anyType>\r\n"
			+ "				<anyType xsi:type=\"xsd:string\">50</anyType>\r\n" + "			</objKeywords>\r\n"
			+ "		</GetPromotionByPrdIDNew>\r\n" + "	</soap:Body>\r\n" + "</soap:Envelope>";

	public static String GetRefSalePriceOfProduct = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
			+ "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n"
			+ "  <soap:Body>\r\n" + "    <GetRefSalePriceOfProduct xmlns=\"http://tempuri.org/\">\r\n"
			+ "      <objResultMessage>\r\n" + "        <IsError>false</IsError>\r\n"
			+ "        <ErrorType>GetData</ErrorType>\r\n" + "        <Message>string</Message>\r\n"
			+ "        <MessageDetail>string</MessageDetail>\r\n" + "        <ErrorCode>0</ErrorCode>\r\n"
			+ "        <Parram>string</Parram>\r\n" + "      </objResultMessage>\r\n"
			+ "      <strProductID>{strProductID}</strProductID>\r\n"
			+ "      <intBrandID>{intBrandID}</intBrandID>\r\n"
			+ "      <intInventoryStatusID>{intInventoryStatusID}</intInventoryStatusID>\r\n"
			+ "    </GetRefSalePriceOfProduct>\r\n" + "  </soap:Body>\r\n" + "</soap:Envelope>";
}
