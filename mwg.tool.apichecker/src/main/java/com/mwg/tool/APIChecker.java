package com.mwg.tool;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.xml.soap.SOAPException;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mwg.tool.config.GlobalConfig;
import com.mwg.tool.helper.ElasticHelper;
import com.mwg.tool.helper.HttpHelper;
import com.mwg.tool.model.SimBO;

public class APIChecker {
	final private static int RETRY = 10; 
	final private static int MAX_RETRY = 5;
	final private static String FILE_NAME = "Result.txt";
	final private static String IMEI_FILE_NAME = "Imeis.txt";
	final private static String APP_NAME = APIChecker.class.getSimpleName();
	final private static String[] DEAFAULT_HEADER = new String[]{"Content-Type", "text/xml; charset=utf-8"};
	
	private static XmlMapper xmlMapper;
	private static ObjectMapper jsonMapper;
	private static ElasticHelper elasticHelper;
	private static Path target;
	private static Path imeiTarget;
	private static AtomicInteger count;
	
	public static void main(String[] args) throws InterruptedException {
		xmlMapper = new XmlMapper();
		jsonMapper = new ObjectMapper();
		elasticHelper = ElasticHelper.getInstance(GlobalConfig.ELASTIC_HOST);
		
		xmlMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		jsonMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		target = Paths.get("./", FILE_NAME);
		imeiTarget = Paths.get("./", IMEI_FILE_NAME);
		
//		target = Paths.get("/home/webtgddteam/ApiChecker/Result", FILE_NAME);
//		imeiTarget = Paths.get("/home/webtgddteam/ApiChecker/Result", IMEI_FILE_NAME);
		
		count = new AtomicInteger(0);
	
		try {
			long startTime = System.currentTimeMillis();
			run();
			long endTime = System.currentTimeMillis();
			System.out.println("Total time: " + (endTime - startTime) + "ms");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				elasticHelper.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void run() throws IOException, IllegalArgumentException, IllegalAccessException, URISyntaxException, InterruptedException, ExecutionException, SOAPException {
		final String index = "ms_sim";
//		final String[] includes = new String[] {"_id"};
//		final String[] excludes = new String[] {};
		SearchSourceBuilder queryBuilder = new SearchSourceBuilder();
		
		queryBuilder.fetchSource(false);
		queryBuilder.query(QueryBuilders.termQuery("IsDeleted", 0));
//		queryBuilder.query(QueryBuilders.matchAllQuery());
		queryBuilder.size(500);

		String scrollId;
		SearchResponse searchResponse = null;
		searchResponse = elasticHelper.scrollRequest(queryBuilder, index);
		scrollId = searchResponse.getScrollId();
		
		int pageIndex = 1;
	
//		ForkJoinPool customThreadPool = new ForkJoinPool(4);
		SearchHit[] searchHits = searchResponse.getHits().getHits();
		while(searchHits != null && searchHits.length > 0) {
			
			//if(pageIndex == 10) break;
			
			System.out.println("pageIndex: " + pageIndex);
//			res = service.scroll(scrollId);
			List<String> simnos = getIds(searchResponse);
		
			
			simnos.parallelStream()
						.forEach(simno -> {
							try {
								System.out.println("Check SimNo: " + simno);
								for(int j = 1; j<=MAX_RETRY; j++) {
									try {
										checkSimAPI(simno);
										count.incrementAndGet();
										break;
								
									} catch (TimeoutException e) {
										System.out.println("Server is timeout.");
										System.out.println("Retry After " + RETRY * j + "s");
										TimeUnit.SECONDS.sleep(RETRY*j);
										e.printStackTrace();
									}
								}
							} catch (IllegalArgumentException | IllegalAccessException | URISyntaxException | InterruptedException
									| ExecutionException | IOException | SOAPException e) {
								e.printStackTrace();
							}
						});
		
			
			
	/*
			for(SimBO sim: sims) {
	
					System.out.println("Check SimNo: " + sim.SimNo);
					for(int j = 1; j<=MAX_RETRY; j++) {
						try {
							checkSimAPI(sim.SimNo);
							count++;
							break;
					
						} catch (TimeoutException e) {
							System.out.println("Server is timeout.");
							System.out.println("Retry After " + RETRY * j + "s");
							TimeUnit.SECONDS.sleep(RETRY*j);
							e.printStackTrace();
						}
					}
			}
			*/
			searchResponse = elasticHelper.scrollDocument(scrollId);
			searchHits = searchResponse.getHits().getHits();
			scrollId = searchResponse.getScrollId();
			pageIndex++;
			
			
		}
//		customThreadPool.shutdownNow();
		elasticHelper.clearScrollRequest(scrollId);
		
		System.out.println("Total Pages: " + pageIndex);
		System.out.println("Total Records: " + count.get());
	}
	
	public static String processPlaceHolder(final String template, Object... params) {
		if(Objects.isNull(template) 
				|| Objects.isNull(params) 
				|| params.length == 0) return template;
		String result = String.copyValueOf(template.toCharArray());
		for(int i = 0;i < params.length; i++) {
			String placeHolder = String.format("{%d}", i);
			result = result.replace(placeHolder, String.valueOf(params[i]));
		}
		return result;
	}

	public static void checkSimAPI(final String imei) throws URISyntaxException, InterruptedException, ExecutionException, IOException, SOAPException, TimeoutException, IllegalArgumentException, IllegalAccessException {
		
		SimBO oldSim = getSimFromOldAPI(imei);
//			boolean isSame = oldSim.equals(newSim);
		if(!Objects.isNull(oldSim)) {
			SimBO newSim = getSimBOFromNewAPI(imei);
			
			//System.out.println("oldSim: " + jsonMapper.writeValueAsString(oldSim));
			//System.out.println("newSim " + jsonMapper.writeValueAsString(newSim));
//			boolean isSame = Objects.deepEquals(oldSim, newSim);
//			if(!isSame) System.out.println("imei=" + imei);
			
			String diffs = oldSim.diff(newSim);
			if(!diffs.isBlank()) {
				String imeis = String.format("'%s',\n", imei);
				Files.writeString(imeiTarget, imeis, StandardCharsets.UTF_8,
		              	StandardOpenOption.CREATE, 
		              	StandardOpenOption.APPEND);

				String record = String.format("%s\t\t\t%s\n\n", imei, diffs);
				Files.writeString(target, record, StandardCharsets.UTF_8,
								              	StandardOpenOption.CREATE, 
								              	StandardOpenOption.APPEND);
			}
		}
		
		
	}
	
	//Get SimBO from new api
	//http://10.1.4.123:2060/apisim/getsimdetail?imei=0326098189
	public static SimBO getSimBOFromNewAPI(final String imei) throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException, IOException {
		String template =  GlobalConfig.API_JAVA_HOST + "/apisim/getsimdetail?imei={0}";
//			String[] headers = new String[] {"Content-Type", "text/xml;Charset=utf-8"}; 
		
		final String url = processPlaceHolder(template, imei);
		
//		String strResponse  = HttpHelper.sendAyncGetRequest(url);
		String strResponse  = HttpHelper.sendSyncGetRequest(url, DEAFAULT_HEADER);
		
		if(strResponse.isBlank()) return null;
		SimBO sim = jsonMapper.readValue(strResponse, SimBO.class);
		if(!Objects.isNull(sim.ProductNo)) sim.ProductNo = sim.ProductNo.trim();
		if(!Objects.isNull(sim.SimNoDisplay)) sim.SimNoDisplay = sim.SimNoDisplay.trim();

		return sim;
	}
	
	//Get SimBO from old api by Get Http
	public static SimBO getSimBOFromOldAPI(final String imei) throws URISyntaxException, InterruptedException, ExecutionException, IOException, TimeoutException {
		String templateURL = GlobalConfig.WEBSERVICE_HOST + "SIMSvc.asmx/LoadSimInfo?strIMEI={0}";
//		String[] headers = new String[] {"Content-Type", "text/xml;Charset=utf-8"}; 
		final String url = processPlaceHolder(templateURL, imei);
		
//		String strResponse  = HttpHelper.sendAyncGetRequest(url, DEAFAULT_HEADER);
		String strResponse  = HttpHelper.sendSyncGetRequest(url, DEAFAULT_HEADER);
		JsonNode node = xmlMapper.readTree(strResponse.getBytes());
		
//		System.out.println(networkBO.toPrettyString());
//		System.out.println(node.toPrettyString());
		SimBO sim = xmlMapper.readValue(strResponse, SimBO.class);
		
		String logo = node.path("NetworkBO").path("logoField").asText();
		int SubGroupID = node.path("NetworkBO").path("subGroupIDField").asInt();
		String SubGroupName = node.path("NetworkBO").path("subBroupNameField").asText();
		String SIMNetworkName = node.path("NetworkBO").path("sIMNetworkNameField").asText();
		sim.Logo = logo;
		sim.SubGroupID = SubGroupID;
		sim.SubGroupName = SubGroupName;
		sim.SIMNetworkName = SIMNetworkName;

		return sim;
	}
	
	//Get SimBO from old api By Soap 1.1
	public static SimBO getSimFromOldAPI(String imei) throws URISyntaxException, InterruptedException, ExecutionException, IOException, TimeoutException {
//		final String auth = "ksdfswfrew3ttc!@4#123";
		String[] headers = new String[] {"Content-Type", "text/xml;Charset=utf-8", "SOAPAction", "http://tempuri.org/ISIMSvc/GetSimInfo"}; 
//		String templateBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
//				+ "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n"
//				+ "  <soap:Body>\r\n"
//				+ "    <GetInfoSIMByIMEI xmlns=\"http://tempuri.org/\">\r\n"
//				+ "      <strAuthen>{0}</strAuthen>\r\n"
//				+ "      <strSIMID>{1}</strSIMID>\r\n"
//				+ "    </GetInfoSIMByIMEI>\r\n"
//				+ "  </soap:Body>\r\n"
//				+ "</soap:Envelope>";
		
		String templateBody = "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n"
				+ "    <Body>\r\n"
				+ "        <GetSimInfo xmlns=\"http://tempuri.org/\">\r\n"
				+ "            <SimNo>{0}</SimNo>\r\n"
				+ "        </GetSimInfo>\r\n"
				+ "    </Body>\r\n"
				+ "</Envelope>";
		
//		final String url = processPlaceHolder(templateURL, imei);
		final String body = processPlaceHolder(templateBody, imei);
//		final String pathNode = "GetInfoSIMByIMEIResponse.GetInfoSIMByIMEIResult.diffgram.DocumentElement.Web_SIM_SELECT";
		final String pathNode = "GetSimInfoResponse.GetSimInfoResult";
//		System.out.println(url );
//		System.out.println(body);
		
//		String strResponse  = HttpHelper.sendAyncPostRequest(GlobalConfig.WEBSERVICE_HOST, body, headers);
		String strResponse  = HttpHelper.sendSyncPostRequest(GlobalConfig.WEBSERVICE_HOST, body, headers);
//		System.out.println(strResponse);
		JsonNode node = xmlMapper.readTree(strResponse.getBytes());

	
		JsonNode result = node.path("Body");
//		System.out.println(result.toPrettyString());
		if(pathNode != null && !pathNode.isEmpty()) {
			String[] paths = pathNode.trim().split("\\.");
			for (String path : paths) {
				result = result.path(path);
			}
		}
		
//		System.out.println(networkBO.toPrettyString());
//		System.out.println(result.toPrettyString());
		SimBO sim = jsonMapper.readValue(result.toString(), SimBO.class);

		if(!Objects.isNull(sim)) {
			String logo = result.path("networkBOField").path("logoField").asText();
			int SubGroupID = result.path("networkBOField").path("subGroupIDField").asInt();
			String SubGroupName = result.path("networkBOField").path("subBroupNameField").asText();
			String SIMNetworkName = result.path("networkBOField").path("sIMNetworkNameField").asText();
			sim.Logo = logo;
			sim.SubGroupID = SubGroupID;
			sim.SubGroupName = SubGroupName;
			sim.SIMNetworkName = SIMNetworkName;
		}
		
//		System.out.println(sim);
		return sim;
	}
	
	public static List<String> getIds(SearchResponse searchResponse){
		if(searchResponse.getHits() == null 
				|| searchResponse.getHits().getHits() == null
				|| searchResponse.getHits().getHits().length == 0) return List.of();
		SearchHit[] hits = searchResponse.getHits().getHits();
		List<String> results = (List<String>) Arrays.stream(hits)
				.map( hit ->{ 
					try {
						return hit.getId();
					}catch(Exception ex) {
						throw new RuntimeException(ex);
					}
				})
				.collect(Collectors.toList());
		return results;		
	}
	
	public static <T> List<T> getSource(SearchResponse searchResponse, Class<T> c){
		if(searchResponse.getHits() == null 
				|| searchResponse.getHits().getHits() == null
				|| searchResponse.getHits().getHits().length == 0) return List.of();
		SearchHit[] hits = searchResponse.getHits().getHits();
		List<T> results = (List<T>) Arrays.stream(hits)
				.map( hit ->{ 
					try {
						return jsonMapper.readValue(hit.getSourceAsString().getBytes("utf-8"), c);
					}catch(Exception ex) {
						throw new RuntimeException(ex);
					}
				})
				.collect(Collectors.toList());
		return results;		
	}
}
