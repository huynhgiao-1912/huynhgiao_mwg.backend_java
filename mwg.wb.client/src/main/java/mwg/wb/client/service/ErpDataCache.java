package mwg.wb.client.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
import mwg.wb.common.FileHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.sim.SimPackageErpBO;

public class ErpDataCache {
//	private static final String strAuthen = "werwerewrw32423!@4#123";
	private Gson gson = null;
	private ObjectMapper mapper = null;

	public ErpDataCache() {
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper = new ObjectMapper();
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		gson = new GsonBuilder().serializeNulls().setFieldNamingStrategy(f -> f.getName().toLowerCase())
				.setDateFormat(GConfig.DateFormatString).registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
				.create();
	}

	public String CallService(String SOAPAction, String body, String Node) {

		StringEntity stringEntity = new StringEntity(body, "UTF-8");

		stringEntity.setChunked(true);
		HttpPost httpPost = new HttpPost("http://erpwebsupportservices.thegioididong.com/DataCache/WSDataCache.asmx");
		httpPost.setEntity(stringEntity);
		httpPost.addHeader("Accept", "text/xml");
		httpPost.addHeader("Content-Type", "text/xml; charset=utf-8");
		httpPost.addHeader("SOAPAction", "http://tempuri.org/" + SOAPAction);
		CloseableHttpClient  httpClient = null;
		CloseableHttpResponse  response = null;
		try {
			httpClient = HttpClientBuilder.create().build();
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			String strResponse = null;
			if (entity != null) {
				strResponse = EntityUtils.toString(entity);
				// System.out.print(strResponse);
//				StringReader sr = new StringReader(strResponse);
				// String xml = strResponse;
				// String example = strResponse;
				SOAPMessage message = MessageFactory.newInstance().createMessage(null,
						new ByteArrayInputStream(strResponse.getBytes()));

				Document document = message.getSOAPBody().extractContentAsDocument();
				NodeList list = document.getElementsByTagName(Node);
				NodeList listSch = document.getElementsByTagName("xs:sequence").item(0).getChildNodes();
				int l2 = listSch.getLength();
				Map<String, String> lsType = new HashMap<String, String>();

				for (int i = 0; i < l2; i++) {
					NamedNodeMap nodeMap = listSch.item(i).getAttributes();
					String nodeName = nodeMap.getNamedItem("name").getNodeValue().toLowerCase();
					String nodeType = StringUtils.replace(nodeMap.getNamedItem("type").getNodeValue(), "xs:", "");

					lsType.put(nodeName, nodeType);
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

						} else if (stype.equals("decimal") || stype.equals("long")) {
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
			Logs.LogException(e);
		} finally {
		}
		return "";
	}

	public SimPackageErpBO[] GetSimPackages() {
		String body = "";
		try {
			body = FileHelper.ReadAllText(Utils.getCurrentDir() + "soapxml/GetPackagesTypeCache.xml");
		} catch (IOException e1) {
			Logs.LogException(e1);
			/// e1.printStackTrace();
		}
		String SOAPAction = "GetPackagesTypeCache";
		return gson.fromJson(CallService(SOAPAction, body, "GetPackagesTypeCache"), SimPackageErpBO[].class);
	}
}
