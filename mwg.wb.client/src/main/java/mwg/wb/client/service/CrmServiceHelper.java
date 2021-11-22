package mwg.wb.client.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.json.JSONArray;
import org.json.XML;

import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.client.resource.XMLResourceBody;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.FileHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.common.notify.LineNotify;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.ProductInventory;

public class CrmServiceHelper {
	private String strUrl = "http://crm-services.thegioididong.com/NEW-CRMTGDD/CRMTGDDService.asmx";
	// private String strUrl =
	// "http://betacrmservices.thegioididong.com/CRMTGDD/CRMTGDDService.asmx";
	private ObjectMapper mapper = null;
	private String fnServiceLog = "crmservice.txt";
	// LineNotify notifyHelperLog = null;
	int datacenter = 0;

	public CrmServiceHelper() {
		mapper = DidxHelper.generateJsonMapper(GConfig.DateFormatStringNews);
//		gson = new GsonBuilder().serializeNulls().setFieldNamingStrategy(f -> f.getName().toLowerCase())
//				.setDateFormat(GConfig.DateFormatString).registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
//				.create();

	}

	public CrmServiceHelper(String url) {
		mapper = DidxHelper.generateJsonMapper(GConfig.DateFormatStringNews);
//		gson = new GsonBuilder().serializeNulls().setFieldNamingStrategy(f -> f.getName().toLowerCase())
//				.setDateFormat(GConfig.DateFormatString).registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
//				.create();
		strUrl = url;

	}

	public CrmServiceHelper(ClientConfig aconfig) {
		mapper = DidxHelper.generateJsonMapper(GConfig.DateFormatStringNews);
		strUrl = aconfig.CRM_SERVIVES_URL;
		if (Utils.StringIsEmpty(strUrl)) {
			strUrl = "http://crm-services.thegioididong.com/NEW-CRMTGDD/CRMTGDDService.asmx";
		}
		datacenter = aconfig.DATACENTER;
		// notifyHelperLog = new
		// LineNotify("LINE","e4QuXuo08qZo6CK4wXLoO7djTuB9E9yEhIO2KusK6W9");
		// notifyHelperLog.DataCenter = 3;
	}

	public org.json.JSONObject CallService(String SOAPAction, String body, String NodeResponse, String NodeResult)
			throws Exception {

		StringEntity stringEntity = new StringEntity(body, "UTF-8");

		stringEntity.setChunked(true);
		HttpPost httpPost = new HttpPost(strUrl);
		httpPost.setEntity(stringEntity);
		httpPost.addHeader("Accept", "text/xml");
		httpPost.addHeader("Content-Type", "text/xml; charset=utf-8");
		httpPost.addHeader("SOAPAction", "http://tempuri.org/" + SOAPAction);
		CloseableHttpClient  httpClient = null;
		CloseableHttpResponse  response = null;
		String strResponse = null;
		try {
			  httpClient = HttpClients.createDefault();
			//httpClient = new DefaultHttpClient();
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				strResponse = EntityUtils.toString(entity);
				// System.out.print(strResponse);
				// StringReader sr = new StringReader(strResponse);
				// String xml = strResponse;
				// String example = strResponse;
				// SOAPMessage message = MessageFactory.newInstance().createMessage(null,
				// new ByteArrayInputStream(strResponse.getBytes()));

				// convert xml soap to json
				var objectjson = XML.toJSONObject(strResponse);
				if (!objectjson.has(NodeResult)) {
					// Logs.LogException(fnServiceLog, "objectjson.has(NodeResult):strResponse:" +
					// strResponse);
					// return null;
				}
				if (strResponse.contains("<" + NodeResult + " />"))
					return null;
				var data = objectjson.getJSONObject("soap:Envelope").getJSONObject("soap:Body")
						.getJSONObject(NodeResponse).getJSONObject(NodeResult);

				return data;

			}
		} catch (Throwable e) {
			// notifyHelperLog.Notify(body);
			// notifyHelperLog.Notify(strUrl + strResponse + e.getMessage());
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
		return null;
	}

	public List<Integer> GetListProvinceHasProductInStock(String ProductCode, int ProvinceID) throws Exception {

		if (Utils.StringIsEmpty(ProductCode) || ProvinceID <= 0) {
			return new ArrayList<Integer>();
		}
		String body = "";
		body = XMLResourceBody.CRM_GetListProvinceHasProductInStock;
		List<Integer> listdata = new ArrayList<Integer>();
//		try {
//			var path = Utils.getCurrentDir() + "soapxml/GetListProvinceHasProductInStock.xml";
////			if(datacenter == 3) {
////				path = "/home/thanhphi/worker/soapxml/GetListProvinceHasProductInStock.xml";
////			}
//			body = FileHelper.ReadAllText(path);
//
//		} catch (IOException e1) {
//			Logs.LogException(e1);
//			throw e1;
//		}
		String SOAPAction = "GetListProvinceHasProductInStock";
		body = body.replace("{strProductCode}", ProductCode).replace("{intToProvinceID}", String.valueOf(ProvinceID));
		var data = CallService(SOAPAction, body, "GetListProvinceHasProductInStockResponse",
				"GetListProvinceHasProductInStockResult");

		if (data != null) {
			try {
				JSONArray jArray = (JSONArray) data.getJSONArray("int");
				if (jArray != null) {
					for (int i = 0; i < jArray.length(); i++) {
						listdata.add(jArray.getInt(i));
					}
				}
			} catch (Throwable e1) {
				Logs.LogException(data.toString());
				throw e1;
			}

		}

		return listdata;
	}

	public ProductInventory GetCurrentInStocksBHXOnlByStore(String productCode, int storeId) throws Exception {
		String body = "";
		body = XMLResourceBody.CRM_GetCurrentInStocksBHXOnlByStore;
//		try {
//			var path = Utils.getCurrentDir() + "soapxml/GetCurrentInStocksBHXOnlByStore.xml";
//			if(datacenter == 3) {
//				path = "/home/thanhphi/worker/soapxml/GetCurrentInStocksBHXOnlByStore.xml";
//			}
		// notifyHelperLog.Notify(path);
//			body = FileHelper.ReadAllText(path);

//		} catch (IOException e1) {
		// notifyHelperLog.Notify(e1.getMessage());
//			Logs.LogException(e1);
//		}
		String SOAPAction = "GetCurrentInStocksBHXOnlByStore";
		body = body.replace("{productID}", productCode).replace("{storeID}", String.valueOf(storeId));
		var data = CallService(SOAPAction, body, "GetCurrentInStocksBHXOnlByStoreResponse",
				"GetCurrentInStocksBHXOnlByStoreResult");

		if (data != null) {
			// trường hợp trả về mảng
//			var x = data.getJSONArray("ProductInventory");
//			if(x!= null)
//			{
//				var json = x.toString();
//				var result = mapper.readValue(json, ProductInventory[].class);
//				return result[0];
//			}

			data = data.getJSONObject("ProductInventory");
			if (data != null) {
				var json = data.toString();
				// notifyHelperLog.Notify("GetCurrentInStocksBHXOnlByStore: " + json);
				var result = mapper.readValue(json, ProductInventory.class);
				return result;
			}
		}
		// notifyHelperLog.Notify("GetCurrentInStocksBHXOnlByStore: Empty response from
		// CRM");
		// Logs.LogException(new Exception("GetCurrentInStocksBHXOnlByStore: Empty
		// response from CRM"));
		return null;
	}

	public ProductInventory[] GetListCurrentInStocksBHXOnl(String productCode) throws Exception {
		String body = "";
		body = XMLResourceBody.CRM_GetCurrentInStocksBHXOnl;
//		try {
//			body = FileHelper.ReadAllText(Utils.getCurrentDir() + "soapxml/GetCurrentInStocksBHXOnl.xml");
//		} catch (IOException e1) {
//			Logs.LogException(e1);
//		}
		String SOAPAction = "GetCurrentInStocksBHXOnl";
		body = body.replace("{productID}", productCode);
		var data = CallService(SOAPAction, body, "GetCurrentInStocksBHXOnlResponse", "GetCurrentInStocksBHXOnlResult");

		if (data != null) {
			var _data = data.getJSONArray("ProductInventory"); // .getJSONObject("ProductInventory");
			if (_data != null) {
				// StringBuilder json = new StringBuilder();
				// for (var tmp : _data) {
				// json.append(tmp.toString());
				// }
				var json = _data.toString();
				// notifyHelperLog.Notify("GetCurrentInStocksBHXOnlByStore: " + json);
				var result = mapper.readValue(json, ProductInventory[].class);
				return result;
			}
		}
		// notifyHelperLog.Notify("GetListCurrentInStocksBHXOnl: Empty response from
		// CRM");
		// Logs.LogException(new Exception("GetListCurrentInStocksBHXOnl: Empty response
		// from CRM"));

		return null;
	}
}
