package mwg.wb.client.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.StringWriter;
 
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys; 
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient; 
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mwg.wb.client.BooleanTypeAdapter;
import mwg.wb.common.FileHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Utils;
import mwg.wb.model.products.ProductBO;
import org.w3c.dom.Node;
import org.json.XML;

public class ProductServiceHelper {
	//private String strAuthen = "werwerewrw32423!@4#123";
	private String strUrl = "http://webservice.bachhoaxanh.com/productsvc.asmx";
	private Gson gson = null;
	private ObjectMapper mapper = null;
	private String fnServiceLog = "productservice.txt";

	public ProductServiceHelper(String url) {
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper = new ObjectMapper();
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);		
	    mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);        
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); 
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);        
        mapper.setSerializationInclusion(Include.NON_NULL); 
		
		gson = new GsonBuilder()
				.serializeNulls()
				.setFieldNamingStrategy(f -> f.getName().toLowerCase())
				.setDateFormat(GConfig.DateFormatString)
				.registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
				.create();
		if(!Utils.StringIsEmpty(url))
			strUrl=url;
		//strAuthen=authen;
	}

	private static <T extends Node> String toString(T doc) {
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
	
	protected String CallService(String SOAPAction, String body, String _Node) throws Throwable {

		StringEntity stringEntity = new StringEntity(body, "UTF-8");

		stringEntity.setChunked(true);
		HttpPost httpPost = new HttpPost(strUrl);
		httpPost.setEntity(stringEntity);
		httpPost.addHeader("Accept", "text/xml");
		httpPost.addHeader("Content-Type", "text/xml; charset=utf-8");
		httpPost.addHeader("SOAPAction", "http://tempuri.org/" + SOAPAction);		
		CloseableHttpClient  httpClient = null;
		CloseableHttpResponse  response = null;
		
		try {
			
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5 *60 * 1000).build();
		  httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
			
			 
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			String strResponse = null;
			if (entity != null) {
				strResponse = EntityUtils.toString(entity);
				if (strResponse.contains("<soap:Body><soap:Fault><faultcode>soap:Server</faultcode><faultstring>")  ) {
					throw new Exception("<soap:Body><soap:Fault><faultcode>soap:Server</faultcode><faultstring>");
				}
				
				//System.out.println(strResponse);
				
				SOAPMessage message = MessageFactory.newInstance().createMessage(null,
						new ByteArrayInputStream(strResponse.getBytes()));

				Document document = message.getSOAPBody().extractContentAsDocument();	
				
				//get child Node
				var node = document.getElementsByTagName(_Node);
					
				var str = toString(node.item(0));
				
				//replace date null
				str = str.replace("xmlns=\"http://tempuri.org/\"", "")
						.replace("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"", "");
				
				//convert xml soap to json
				var objectjson = XML.toJSONObject(str);				
				
				var objectvalue = objectjson.get(_Node);
				
				var ret = objectvalue.toString();
				//System.out.println(ret);
				return ret;

			}
		} catch (Exception e) {
			//log
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
		//return null;
		return "_ERR_";
	}

	public ProductBO GetProductBOByProductCode(String strProductCode, String strLangID, int intAreaID, int intSiteID ) throws Throwable {
		String body = "";
		try {
			body = FileHelper.ReadAllText(Utils.getCurrentDir() + "soapxml/GetProductBOByProductCode.xml");
		} catch (IOException e1) {
			e1.printStackTrace();
			throw e1;
		}

		if (Utils.StringIsEmpty(body)) {
			
		}
		String SOAPAction = "GetProductBOByProductCode";

		body = body
				.replace("{strProductCode}", strProductCode.trim())
				.replace("{strLangID}", strLangID.trim())
				.replace("{intAreaID}", Integer.toString(intAreaID))
				.replace("{intSiteID}", Integer.toString(intSiteID));
		String txt = CallService(SOAPAction, body, "GetProductBOByProductCodeResult");		
		//{"GetPriceBHXOnlineResult":10500}		
		try {			
			//return gson.fromJson(txt, ProductBO.class);
			return mapper.readValue(txt, ProductBO.class);
		} 
		catch (NumberFormatException e) {
			return null;
		}
		

	}
	
}
