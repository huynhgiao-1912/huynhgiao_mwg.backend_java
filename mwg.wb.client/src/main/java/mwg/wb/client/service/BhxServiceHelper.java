package mwg.wb.client.service;

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
import java.io.StringWriter;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import mwg.wb.client.BooleanTypeAdapter;
import mwg.wb.client.resource.XMLResourceBody;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.FileHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.promotion.BillPromotionBHX;
import mwg.wb.model.promotion.BillPromotionBHXBO;
import mwg.wb.model.promotion.PromotionBHXBO;
import mwg.wb.model.sim.SimBO;
import org.w3c.dom.Node;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.json.XMLParserConfiguration;

public class BhxServiceHelper {
	// live
	// private String strAuthen = "werwerewrw32423!@4#123";
	// private String strUrl =
	// "http://bhxservices.bachhoaxanh.com/Web/BHXOnlineSVC.asmx";
	// beta
	private String strAuthen = "werwerewrw32423!@4#123"; //werwerewrw32423!@4#123
	private String strUrl = "http://bhxservices.bachhoaxanh.com/Web/BHXOnlineSVC.asmx";	
	private ObjectMapper mapper = null;
	private String fnServiceLog = "bhxservice.txt";

	public BhxServiceHelper(String url, String authen) {		
		mapper = DidxHelper.generateJsonMapper(GConfig.DateFormatString);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		if (!Utils.StringIsEmpty(url)) {
			strUrl = url;
			strAuthen = authen;
		}
	}

	public BhxServiceHelper() {
		mapper =  DidxHelper.generateJsonMapper(GConfig.DateFormatString);	
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	}
	
	public BhxServiceHelper(ClientConfig aconfig)
	{
		mapper = DidxHelper.generateJsonMapper(GConfig.DateFormatString);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		strUrl = aconfig.BHX_SERVICE_URL;
		if (Utils.StringIsEmpty(strUrl)) {
			strUrl = "http://bhxservices.bachhoaxanh.com/Web/BHXOnlineSVC.asmx";
		}
		if(Utils.StringIsEmpty(aconfig.BHX_SERICE_TOKEN))
		{
			strAuthen = "werwerewrw32423!@4#123";
		}
	}
	
	public static <T extends Node> String toString(T doc) {
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (Exception ex) {
			throw new RuntimeException("Error converting to String", ex);
		}
	}

	public String CallService(String SOAPAction, String body, String _Node) throws Exception {

		StringEntity stringEntity = new StringEntity(body, "UTF-8");
		String strResponse = "";
		stringEntity.setChunked(true);
		HttpPost httpPost = new HttpPost(strUrl);
		httpPost.setEntity(stringEntity);
		httpPost.addHeader("Accept", "text/xml");
		httpPost.addHeader("Content-Type", "text/xml; charset=utf-8");
		httpPost.addHeader("SOAPAction", "http://tempuri.org/" + SOAPAction);
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpResponse response = null;
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				strResponse = EntityUtils.toString(entity);

				SOAPMessage message = MessageFactory.newInstance().createMessage(null,
						new ByteArrayInputStream(strResponse.getBytes()));

				Document document = message.getSOAPBody().extractContentAsDocument();

				// get child Node
				var node = document.getElementsByTagName(_Node);
				if(node.getLength() == 0)
					return null;
				var str = toString(node.item(0));

				// replace date null
				str = str.replace("xmlns=\"http://tempuri.org/\"", "")
						.replace("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"", "");

				// convert xml soap to json
				var objectjson = XML.toJSONObject(str);

				var objectvalue = objectjson.get(_Node);

				return objectvalue.toString();

			}
		} catch (Throwable e) {
			Logs.LogException(fnServiceLog, e);
			Logs.LogException(fnServiceLog, body);
			Logs.LogException(fnServiceLog, "strUrl:" + strUrl); 
			Logs.LogException(fnServiceLog, strResponse); 
			throw e;
		 
		} finally {
			// ghi log
		}
		
		return "_ERR_";
	}

	public String CallServicePromotion(String SOAPAction, String body, String _Node) throws Exception {

		StringEntity stringEntity = new StringEntity(body, "UTF-8");
		String strResponse = "";
		stringEntity.setChunked(true);
		HttpPost httpPost = new HttpPost(strUrl);
		httpPost.setEntity(stringEntity);
		httpPost.addHeader("Accept", "text/xml");
		httpPost.addHeader("Content-Type", "text/xml; charset=utf-8");
		httpPost.addHeader("SOAPAction", "http://tempuri.org/" + SOAPAction);
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpResponse response = null;
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				strResponse = EntityUtils.toString(entity);

				SOAPMessage message = MessageFactory.newInstance().createMessage(null,
						new ByteArrayInputStream(strResponse.getBytes()));

				Document document = message.getSOAPBody().extractContentAsDocument();
				
				// get child Node
				var node = document.getElementsByTagName(_Node);
				
			
				if(node.getLength() == 0)
					return null;
				var str = toString(node.item(0));
	
				// replace date null
				str = str.replace("xmlns=\"http://tempuri.org/\"", "")
						.replace("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"", "");

				// convert xml soap to json
				var objectjson = XML.toJSONObject(str);
//				System.out.println(objectjson);
				

				String objectvalue = objectjson.get(_Node).toString();	
				if(objectvalue.length() == 0) return null;
				
				JSONObject js = new JSONObject(objectvalue);
				if(!js.has("PromotionBO")) return null;
//				System.out.println(js.getJSONArray("PromotionBO"));
				String result = null;
				try {
					result =  js.getJSONArray("PromotionBO").toString();
					return result;
				}catch(JSONException jex) {
					result = js.getJSONObject("PromotionBO").toString();
				}
//				return js.getJSONArray("PromotionBO").toString();
				return result;
			}
			
		} catch (Throwable e) {
			Logs.LogException(fnServiceLog, e);
			Logs.LogException(fnServiceLog, body);
			Logs.LogException(fnServiceLog, "strUrl:" + strUrl); 
			Logs.LogException(fnServiceLog, strResponse); 
		 		
			throw e;
		} finally {
			// ghi log
		}
		// return null;
		return "_ERR_";
	}
	
	public String CallServiceBillPromo(String SOAPAction, String body, String _Node) throws Exception {

		StringEntity stringEntity = new StringEntity(body, "UTF-8");

		stringEntity.setChunked(true);
		HttpPost httpPost = new HttpPost(strUrl);
		httpPost.setEntity(stringEntity);
		httpPost.addHeader("Accept", "text/xml");
		httpPost.addHeader("Content-Type", "text/xml; charset=utf-8");
		httpPost.addHeader("SOAPAction", "http://tempuri.org/" + SOAPAction);
		HttpResponse response = null;
		String strResponse = null;
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				strResponse = EntityUtils.toString(entity);

				SOAPMessage message = MessageFactory.newInstance().createMessage(null,
						new ByteArrayInputStream(strResponse.getBytes()));

				Document document = message.getSOAPBody().extractContentAsDocument();
				
				// get child Node
				var node = document.getElementsByTagName(_Node);
				if(node.getLength() == 0)
					return null;
				var str = toString(node.item(0));

				// replace date null
				str = str.replace("xmlns=\"http://tempuri.org/\"", "")
						.replace("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"", "");

				// convert xml soap to json
				var objectjson = XML.toJSONObject(str);

				var objectvalue = objectjson.get(_Node);				

				return objectvalue.toString();

			}
		} catch (Exception e) {
			Logs.LogException(fnServiceLog, e);
			Logs.LogException(fnServiceLog, body);
			throw e;
		} finally {
		}
		return "_ERR_";
	}

	public double GetPriceBHXOnline(String strProductCode, int intStoreId) throws Exception {
		String body = "";
		body = XMLResourceBody.BHX_GetPriceBHXOnline;
		if (Utils.StringIsEmpty(body)) {

		}
		String SOAPAction = "GetPriceBHXOnline";

		body = body.replace("{AuthenData}", strAuthen).replace("{productId}", strProductCode.trim())
				.replace("{intStoreId}", Integer.toString(intStoreId));
		String txt = CallService(SOAPAction, body, "GetPriceBHXOnlineResponse");
		// {"GetPriceBHXOnlineResult":10500}
		double result = 0;
		try {
			txt = txt.replace("{\"GetPriceBHXOnlineResult\":", "").replace("}", "");
			result = Double.parseDouble(txt);
			// System.out.print(result);
		} catch (NumberFormatException e) {
			Logs.LogException(e);
			result = -1;
			//throw new Exception(e);
		}
		return result;

	}

	public PromotionBHXBO[] GetPromotionBHXByPrdID(String strProductID, int intQuantity, int intStoreId,
			Boolean bolIsGetStock, int intOldDays) throws Throwable {
		String json = GetPromotionBHXByProductID(strProductID, intQuantity, intStoreId, bolIsGetStock, intOldDays);
		if (json == null) {
			return null;
		}
		
			return mapper.readValue(json, PromotionBHXBO[].class);
	}

	public String GetPromotionBHXByProductID(String strProductID, int intQuantity, int intStoreId,
			Boolean bolIsGetStock, int intOldDays) throws Exception {
		String body = "";
		body = XMLResourceBody.BHX_GetPromotionBHXByProductID;
		if (Utils.StringIsEmpty(body)) {

		}
		String SOAPAction = "GetPromotionBHXByProductID";

		body = body.replace("{AuthenData}", strAuthen).replace("{strProductID}", strProductID.trim())
				.replace("{intQuantity}", Integer.toString(intQuantity))
				.replace("{intStoreID}", Integer.toString(intStoreId))
				.replace("{bolIsGetStock}", Boolean.toString(bolIsGetStock).toLowerCase())
				.replace("{intOldDays}", Integer.toString(intOldDays));
		//sao lay node PromotionBO
		return CallServicePromotion(SOAPAction, body, "GetPromotionBHXByProductIDResult");
	}
	
	public BillPromotionBHXBO[] GetPromotionBHXById(int intPromoId, int intStoreId) throws Exception {
		String json = GetPromotionBHXByPromotionId(intPromoId, intStoreId);
		if (json == null)
			return null;
		try {
			return mapper.readValue(json, BillPromotionBHXBO[].class);
		} catch (Exception e) {
			Logs.LogException(fnServiceLog, e);
			Logs.LogException(fnServiceLog, json);
			//throw e;
			return null;
		}
	}

	public String GetPromotionBHXByPromotionId(int intPromoId, int intStoreId) throws Exception {
		String body = "";
		body = XMLResourceBody.BHX_GetPromotionBHXByPromotionId;
		if (Utils.StringIsEmpty(body)) {

		}
		String SOAPAction = "GetPromotionById";

		body = body.replace("{AuthenData}", strAuthen)
				.replace("{intPromoId}", Integer.toString(intPromoId))
				.replace("{intStoreId}", Integer.toString(intStoreId));
		return CallServiceBillPromo(SOAPAction, body, "bhx_masterdata.pm_promotion_getforonline");
	}

	public PromotionBHXBO[] GetPromotionByProductClearStock(String strProductID, int intStoreID,
			String strQuantity, int intOldDays, String dtmDateApply) throws Exception {
		String json = GetPromotionByProductClearStockByProductCode(strProductID, intStoreID,
				strQuantity, intOldDays, dtmDateApply);
		if (json == null)
			return null;
		try {
			return mapper.readValue(json, PromotionBHXBO[].class);
		} catch (Exception e) {
			Logs.LogException(fnServiceLog, e);
			Logs.LogException(fnServiceLog, json);
			//throw e;
			return null;
		}
	}

	public String GetPromotionByProductClearStockByProductCode(String strProductID, int intStoreID,
			String strQuantity, int intOldDays, String dtmDateApply) throws Exception {
		String body = "";
		body = XMLResourceBody.BHX_GetPromotionByProductClearStock;
		if (Utils.StringIsEmpty(body)) {

		}
		String SOAPAction = "GetPromotionByProductClearStock";

		body = body.replace("{AuthenData}", strAuthen)
				.replace("{strProductID}", strProductID)
				.replace("{intStoreID}", Utils.toString(intStoreID))
				.replace("{strQuantity}", strQuantity)
				.replace("{intOldDays}", Utils.toString(intOldDays))
				.replace("{dtmDateApply}", dtmDateApply.replace(" ", "T") + "+07:00");
		return CallServiceBillPromo(SOAPAction, body, "tbdPromotionByProductClearStock");
	}

}
