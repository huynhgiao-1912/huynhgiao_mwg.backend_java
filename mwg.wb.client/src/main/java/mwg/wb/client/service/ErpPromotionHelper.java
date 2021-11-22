package mwg.wb.client.service;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import mwg.wb.client.BooleanTypeAdapter;
import mwg.wb.client.resource.XMLResourceBody;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.promotion.Promotion;

public class ErpPromotionHelper {
	private String strAuthen = "werwerewrw32423!@4#123";
	private String strUrl = "http://erpwebsupportservices.thegioididong.com/Web/WSPromotion.asmx";
	private Gson gson = null;
	private ObjectMapper mapper = null;
	private String fnServiceLog = "erpservice.txt";
	private DecimalFormat df;

	public ErpPromotionHelper(String url, String authen) {
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

		this.df = new DecimalFormat("#");
		this.df.setMaximumFractionDigits(0);
	}

	public String CallService(String SOAPAction, String body, String Node) throws Exception {

		StringEntity stringEntity = new StringEntity(body, "UTF-8");

		stringEntity.setChunked(true);
		HttpPost httpPost = new HttpPost(strUrl);
		httpPost.setEntity(stringEntity);
		httpPost.addHeader("Accept", "text/xml");
		httpPost.addHeader("Content-Type", "text/xml; charset=utf-8");
//		httpPost.addHeader("SOAPAction", "http://tempuri.org/" + SOAPAction);
		CloseableHttpClient  httpClient = null;
		CloseableHttpResponse  response = null;
		String strResponse = null;
		try {
			
			
			 httpClient = HttpClients.createDefault();
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				strResponse = EntityUtils.toString(entity);
				if (strResponse
						.contains("<MessageDetail>Oracle.DataAccess.Client.OracleException ORA-01403: no data found")) {
					List<JsonObject> lsa = new ArrayList<JsonObject>();
					return gson.toJson(lsa);
				}
				if (strResponse.contains("<soap:Body><soap:Fault><faultcode>soap:Server</faultcode><faultstring>")  ) {
					throw new Exception("<soap:Body><soap:Fault><faultcode>soap:Server</faultcode><faultstring>");
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
					NodeList listSch = document.getElementsByTagName("xs:sequence").item(0).getChildNodes();
					int l2 = listSch.getLength();
					for (int i = 0; i < l2; i++) {
						NamedNodeMap nodeMap = listSch.item(i).getAttributes();
						String nodeName = nodeMap.getNamedItem("name").getNodeValue().toLowerCase();
						String nodeType = StringUtils.replace(nodeMap.getNamedItem("type").getNodeValue(), "xs:", "");

						lsType.put(nodeName, nodeType);
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

	public Promotion[] getPromotionSubBrand(int subgroupID, int manuID, int provinceID, int outputTypeID,
			double salePrice, int inventoryStatusID, int siteID) throws Exception {
		String body = XMLResourceBody.PROMOTION_WEBGETSUBBRAND.replace("{Authen}", strAuthen)
				.replace("{SubGroupID}", Integer.toString(subgroupID)).replace("{ManuID}", Integer.toString(manuID))
				.replace("{ProvinceID}", Integer.toString(provinceID))
				.replace("{OutputTypeID}", Integer.toString(outputTypeID)).replace("{SalePrice}", df.format(salePrice))
				.replace("{SiteID}", Integer.toString(siteID))
				.replace("{InventoryStatusID}", Integer.toString(inventoryStatusID));
		String SOAPAction = "GetPromotionBySubBrand";
		long now = System.currentTimeMillis();
		String json = CallService(SOAPAction, body, "PM_PROMOTION_WEBGETSUBBRAND");
		long took = System.currentTimeMillis() - now;
		if (took > 1000) {
			Logs.LogFactoryMessage("getPromotionSubBrand-slow",
					SOAPAction + ":" + took + body + "\r\n================\r\n", siteID);
		}
		return gson.fromJson(json, Promotion[].class);
	}

	public String GetPromotionByPrdIDStr(String prdCode, int ProvinceID, int OutputTypeID, int inventoryStatusID,
			double SalePrice, int siteid) throws Exception {

		String body = XMLResourceBody.PROMOTION_WEBGETPRD;

		String SOAPAction = "GetPromotionByPrdID";

		body = body.replace("{Authen}", strAuthen).replace("{ProductID}", prdCode.trim())
				.replace("{ProvinceID}", Integer.toString(ProvinceID))
				.replace("{OutputTypeID}", Integer.toString(OutputTypeID)).replace("{SalePrice}", df.format(SalePrice))
				.replace("{SiteID}", Integer.toString(siteid))
				.replace("{InventoryStatusID}", Integer.toString(inventoryStatusID));

		long t1 = System.currentTimeMillis();
		String a = CallService(SOAPAction, body, "PM_PROMOTION_WEBGETPRD");
		long detal = System.currentTimeMillis() - t1;
		if (detal > 1000) {
			Logs.LogFactoryMessage("GetPromotionByPrdID-slow",
					SOAPAction + ":" + detal + body + "\r\n================\r\n", siteid);
		}
		Logs.LogFactoryMessage("promotionsoap", body + ":" + a + "\r\n================\r\n", siteid);

		return a;

	}

	public Promotion[] GetPromotionByPrdID(String prdCode, int ProvinceID, int OutputTypeID, int inventoryStatusID,
			double SalePrice, int siteid) throws Exception {
		String json = GetPromotionByPrdIDStr(prdCode, ProvinceID, OutputTypeID, inventoryStatusID, SalePrice, siteid);

		return gson.fromJson(json, Promotion[].class);

	}

}
