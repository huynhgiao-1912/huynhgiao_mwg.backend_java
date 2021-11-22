package mwg.wb.webapi.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.primitives.Ints;
import mwg.wb.business.CategoryHelper;
import mwg.wb.business.LogHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.helper.BHXStoreHelper;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.elasticsearch.dataquery.ProductQuery;
import mwg.wb.client.elasticsearch.dataquery.SearchOrder;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.client.service.CodeTimers;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.common.bhx.ProvinceDetailPO;
import mwg.wb.common.bhx.StoreDetailPO;
import mwg.wb.common.bhx.StoreProvinceInfoBO;
import mwg.wb.model.LogLevel;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.general.DistrictBO;
import mwg.wb.model.general.WardBO;
import mwg.wb.model.products.ProductBO;
import mwg.wb.webapi.requests.ProductListRequest;
import mwg.wb.webapi.service.ConfigUtils;
import mwg.wb.webapi.service.HeaderBuilder;
import org.json.JSONObject;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apitest")
public class TestController {
    private static ProductHelper _productHelper = null;
    private static BHXStoreHelper _bhxStoreHelper = null;
    //	private static CommonHelper _commonHelper = null;
//	private static PriceHelper _pricetHelper = null;
    private static ORThreadLocal factoryRead = null;
    //	private static ClientConfig _config = null;
    private static ClientConfig config = null;
    private static CategoryHelper cateHelper = null;

    private static synchronized ProductHelper GetProductClient() {

        // 31, 32

        if (_productHelper == null) {

            // synchronized (ProductApiController.class) {
            // int DataCenterConfig = ConfigUtils.GetDataCenter();
            // int dataCenter = DataCenterHelper.GetDataCenter(DataCenterConfig);

            ClientConfig config = ConfigUtils.GetOnlineClientConfig();
//			_config = config;
            try {
                factoryRead = new ORThreadLocal();
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            factoryRead.initReadAPI(config, 0);
            _productHelper = new ProductHelper(factoryRead, config);

            // }
        }
        // }
        return _productHelper;
    }

    private static synchronized BHXStoreHelper GetStoreHelper() {
        if (_bhxStoreHelper == null) {
            ClientConfig config = ConfigUtils.GetOnlineClientConfig();
            var mapper = DidxHelper.generateJsonMapper(GConfig.DateFormatStringNews);
            _bhxStoreHelper = new BHXStoreHelper(mapper, config);
        }
        return _bhxStoreHelper;
    }

    @RequestMapping(value = "/getproduct", method = RequestMethod.GET)
    public ResponseEntity<ProductBO> GetProduct(int productID, int siteID, Integer storeID, Integer provinceID,
                                                String lang) {
        if (provinceID == null)
            provinceID = 3;
        if (storeID == null)
            storeID = 0;
        if (lang == null || lang == "")
            lang = "vi-VN";
//		var codetimer = new CodeTimer("timer-all");
//		var odbtimer = new CodeTimer("timer-odb");
        var timer = new CodeTimers();
        timer.start("all");
        ProductBO product = null;
        var productHelper = GetProductClient();
        var status = HttpStatus.OK;
        try {
            product = productHelper.GetProduct_PriceStrings(productID, siteID, provinceID, storeID, lang, timer);
            // product = productHelper.GetProductFromCache(productID, siteID, provinceID,
            // storeID, lang, codetimer, odbtimer);

        } catch (Throwable e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            product = new ProductBO() {
                {
                    message = e.getClass().getName() + ": " + e.getMessage();
                    stackTrace = Stream.of(e.getStackTrace()).map(x -> x.toString()).toArray(String[]::new);

                }
            };
            LogHelper.WriteLog(e);
        }
        timer.pause("all");
        var header = HeaderBuilder.buildHeaders(timer);
        return new ResponseEntity<>(product, header, status);

    }

    @RequestMapping(value = "/searchproductquery", method = RequestMethod.POST)
    public ResponseEntity<int[]> SearchProductQuery(@RequestBody ProductQuery productQuery) {

        var timer = new CodeTimers();
        var status = HttpStatus.OK;
        var phelper = GetProductClient();
        int[] ids = null;
        timer.start("all");
        try {
            // query order duplicate check
            if (productQuery.Orders != null && productQuery.Orders.length > 0) {
                productQuery.Orders = Arrays.stream(productQuery.Orders).distinct().toArray(SearchOrder[]::new);
            }

            timer.start("es-all");
            var solist = phelper.Ela_SearchProduct(productQuery, true, true,
                    true, timer);
            timer.pause("es-all");

            List<Integer> lids = new ArrayList<Integer>();
//			List<String> lcodes = new ArrayList<String>();
            if (solist.productList != null) {
                solist.productList.forEach((k, v) -> {
                    lids.add(k);
//					if (v != null && v.Prices != null) {
//						String code = (String) v.Prices.get("ProductCode_" + productQuery.ProvinceId);
//						if (code != null)
//							lcodes.add(code);
//					}
                });
            }
            ids = Ints.toArray(lids);




//			else
//				// Lấy giá từ Elastic
//				for (ProductBO product : result.productList) {
//					var isAccessory = accessories.contains((int) product.CategoryID);
////					phelper.GetDefaultPriceForList(product, isAccessory, true);
//					phelper.GetDefaultPriceAndPromotion(productQuery.SiteId, productQuery.LanguageID, product,
//							isAccessory, false);
//				}



        } catch (Throwable e) {
            String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));

            status = HttpStatus.INTERNAL_SERVER_ERROR;

            var param = phelper.GetJsonFromObject(productQuery);
            LogHelper.WriteLog(e, LogLevel.ERROR, param);

        }
//		result.productList = phelper.GetSimpleProductByID(196963, siteID, provinceID);
        timer.pause("all");
        var header = HeaderBuilder.buildHeaders(timer);

//		try {
//			// header.set("content-length", Long.toString(SizeOf.deepSizeOf(result)));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

        return new ResponseEntity<int[]>(ids, header, status);

    }
    
    @RequestMapping("getDataCenter")
    public ResponseEntity<Integer> getDataCenter(){
		System.out.println(ConfigUtils.GetDataCenter());
		return new ResponseEntity<Integer>(ConfigUtils.GetDataCenter(), HttpStatus.OK);
    }

    @RequestMapping("getConfigUtils")
    public ResponseEntity<ClientConfig> getConfigUtils(){
        System.out.println(ConfigUtils.GetDataCenter());
        if(DidxHelper.isBeta()){
            return new ResponseEntity<ClientConfig>(ConfigUtils.GetOnlineClientConfig(), HttpStatus.OK);
        }
        return new ResponseEntity<ClientConfig>(new ClientConfig(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getproductbylistid", method = RequestMethod.POST)
    public ResponseEntity<ProductBO[]> GetProductByListID(@RequestBody ProductListRequest query) {
        System.out.print(query.storeId);

        var codetimer = new CodeTimer("timer-all");
        var odbtimer = new CodeTimer("timer-odb");
        var phelper = GetProductClient();
        var status = HttpStatus.OK;
        odbtimer.reset();
        ProductBO[] result = null;
        try {
            result = phelper.GetProductBOByListID_PriceStrings(query.listProductID, query.siteID, query.provinceID,
                    query.lang);
            for (var product : result) {
                phelper.processPriceStrings(product, query.provinceID, query.siteID, 0);
            }
        } catch (Throwable e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;

            LogHelper.WriteLog(e);
        }
        odbtimer.end();
        // Lấy giá từ Elastic
        var accessories = phelper.getAccessoryCategory();
        for (ProductBO product : result) {
//			phelper.GetDefaultPriceForList(product, isAccessory, true);

            if (query.siteID == 11) {

                try {
                    phelper.GetDefaultPriceAndPromotionBHX(query.lang, product, false, query.storeId, query.provinceID);
                } catch (Throwable e) {
                    status = HttpStatus.INTERNAL_SERVER_ERROR;

                    LogHelper.WriteLog(e);
                }
            } else {
                var isAccessory = accessories.contains((int) product.CategoryID);
                try {
                    phelper.GetDefaultPriceAndPromotion(query.siteID, query.lang, product, isAccessory, false, null);
                } catch (Throwable e) {
                    status = HttpStatus.INTERNAL_SERVER_ERROR;

                    LogHelper.WriteLog(e);
                }
            }
        }
        codetimer.end();
        return new ResponseEntity<ProductBO[]>(result, HeaderBuilder.buildHeaders(codetimer, odbtimer), status);
    }

    public void updateDataElasticsearch() throws JsonMappingException, JsonProcessingException {
        var mapper = DidxHelper.generateJsonMapper("dd/MM/yyyy");
        String url = "https://logisticapi.thegioididong.com/apisso/api//InsiteDelivery/GetAllStoreToProvince";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer b2c552254ed9bd26c575806f38b30a97");
        JSONObject body = new JSONObject();
        body.put("ProvinceId", -1);

        HttpEntity<String> entity = new HttpEntity<String>(body.toString(), headers);
        Object data = restTemplate.postForObject(url, entity, Object.class);
        LinkedHashMap linkedHashMap = (LinkedHashMap) data;

        if (!linkedHashMap.get("Status").equals("Success")) {
            return;
        }

        List<LinkedHashMap> linkedHashMaps = (List<LinkedHashMap>) linkedHashMap.get("Data");
        Map<Integer, List<StoreProvinceInfoBO>> mapStoreToProvince = new HashMap<>();
        Map<Integer, List<Integer>> mapDistrictApply = new HashMap<Integer, List<Integer>>();
        Map<String, List<Integer>> mapWardDeliveryInDistrict = new HashMap<String, List<Integer>>();

        if (linkedHashMaps != null && linkedHashMaps.size() > 0) {
            for (LinkedHashMap item : linkedHashMaps) {
                String json = new JSONObject(item).toString();
                StoreProvinceInfoBO storeProvince = mapper.readValue(json, StoreProvinceInfoBO.class);
                Integer provinceId = storeProvince.getProvinceId();

                if (!mapStoreToProvince.containsKey(provinceId)) {
                    mapStoreToProvince.put(provinceId, new ArrayList<>());
                }

                if (!mapDistrictApply.containsKey(provinceId)) {
                    mapDistrictApply.put(provinceId, new ArrayList<>());
                }

//                if(!mapWardDeliveryInDistrict.containsKey(provinceId + "_" + storeProvince.getDistrictId())) {
//                	mapWardDeliveryInDistrict.put(provinceId + "_" + storeProvince.getDistrictId(), new ArrayList<>());
//                }
                if (!mapWardDeliveryInDistrict.containsKey(storeProvince.getDistrictId().toString())) {
                    mapWardDeliveryInDistrict.put(storeProvince.getDistrictId().toString(), new ArrayList<>());
                }
                if (storeIsRunning(storeProvince)) {
                    // mapStoreToProvince.get(provinceId).add(storeProvince);
                    if (!checkExistStoreInMap(mapStoreToProvince, provinceId, storeProvince.getStoreId())) {
                        mapStoreToProvince.get(provinceId).add(storeProvince);
                    }

                    if (!mapDistrictApply.get(provinceId).contains(storeProvince.getDistrictId())) {
                        mapDistrictApply.get(provinceId).add(storeProvince.getDistrictId());
                    }

//                	if(!mapWardDeliveryInDistrict.get(provinceId + "_" + storeProvince.getDistrictId()).contains(storeProvince.getWardId())) {
//                		mapWardDeliveryInDistrict.get(provinceId + "_" + storeProvince.getDistrictId()).add(storeProvince.getWardId());
//                    }
                    if (!mapWardDeliveryInDistrict.get(storeProvince.getDistrictId().toString())
                            .contains(storeProvince.getWardId())) {
                        mapWardDeliveryInDistrict.get(storeProvince.getDistrictId().toString())
                                .add(storeProvince.getWardId());
                    }
                }
            }
        }
        List<ProvinceDetailPO> listDetail = new ArrayList<>();
        for (Map.Entry<Integer, List<StoreProvinceInfoBO>> entry : mapStoreToProvince.entrySet()) {
            ProvinceDetailPO bo = new ProvinceDetailPO();
            if (entry.getValue() == null || entry.getValue().size() == 0) {
                continue;
            }
            StoreProvinceInfoBO defaultstore = entry.getValue().get(0);
            if (entry.getValue().size() > 0) {
                List<StoreDetailPO> listStoreDetail = new ArrayList<>();
                for (StoreProvinceInfoBO item : entry.getValue()) {
                    StoreDetailPO storeDetailPO = new StoreDetailPO();
                    storeDetailPO.setStoreId(item.getStoreId());
                    storeDetailPO.setCloseDate(item.getClosingDay());
                    storeDetailPO.setExpStore(item.getExpireDateStore());
                    storeDetailPO.setFullName(item.getStoreName());
                    storeDetailPO.setOpenDate(item.getOpeningDay());
                    storeDetailPO.setDistrictId(item.getDistrictId());
                    storeDetailPO.setWardId(item.getWardId());
                    listStoreDetail.add(storeDetailPO);
                }
                bo.setStoreIds(listStoreDetail);
            }
            Integer provinceId = entry.getKey();
            bo.setProvinceId(provinceId);
            bo.setOpenDate(defaultstore.getOpeningDay());
            bo.setDefaultStore(defaultstore.getStoreId());
            bo.setProvinceFullName(defaultstore.getProvinceName());
            bo.setProvinceName(defaultstore.getProvinceName());
            bo.setProvinceShortName(defaultstore.getProvinceShortName());
            bo.setDistrictApply(mapDistrictApply.get(provinceId));
            try {
                bo = getDeliveryInWard(bo, mapWardDeliveryInDistrict);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            listDetail.add(bo);
        }
        saveIndex(listDetail);
    }

    private Boolean checkExistStoreInMap(Map<Integer, List<StoreProvinceInfoBO>> mapData, Integer provinceId,
                                         Integer storeId) {
        if (mapData.get(provinceId) == null || mapData.get(provinceId).size() == 0) {
            return Boolean.FALSE;
        }
        List<StoreProvinceInfoBO> listStore = mapData.get(provinceId);
        var status = listStore.stream().anyMatch(x -> x.getStoreId().equals(storeId));
        return status;
    }

    private ProvinceDetailPO getDeliveryInWard(ProvinceDetailPO provinceDetail, Map<String, List<Integer>> mapData)
            throws Throwable {
        GetCategoryClient();
        DistrictBO[] allDistrictInProvince = cateHelper.getDistrictByProvince(provinceDetail.getProvinceId());
        Stream<DistrictBO> stream = Arrays.stream(allDistrictInProvince);
        List<Integer> listDistrictId = stream.map(x -> x.DistrictID).collect(Collectors.toList());

        Map<Integer, List<Integer>> wardNotDeliveryInDistrict = new HashMap<Integer, List<Integer>>();
        Map<Integer, List<Integer>> wardDeliveryInDistrict = new HashMap<Integer, List<Integer>>();
        List<Integer> wardDeliverys = new ArrayList<Integer>();

        for (Integer districtId : listDistrictId) {
            WardBO[] listWard = cateHelper.getWardByDistrict(districtId);
            for (int i = 0; i < listWard.length; i++) {
                WardBO ward = listWard[i];
                if (mapData.get(districtId) != null && mapData.get(districtId).contains(ward.WardID)) {
                    if (!wardDeliveryInDistrict.containsKey(districtId)) {
                        wardDeliveryInDistrict.put(districtId, new ArrayList<Integer>());
                    }
                    wardDeliveryInDistrict.get(districtId).add(ward.WardID);
                    wardDeliverys.add(ward.WardID);
                } else {
                    if (!wardNotDeliveryInDistrict.containsKey(districtId)) {
                        wardNotDeliveryInDistrict.put(districtId, new ArrayList<Integer>());
                    }
                    wardNotDeliveryInDistrict.get(districtId).add(ward.WardID);
                }
            }
        }
        provinceDetail.setWardNotDeliveryByDistrict(wardNotDeliveryInDistrict);
        provinceDetail.setWardDeliveryByDistrict(wardDeliveryInDistrict);
        provinceDetail.setWardDeliverys(wardDeliverys);
        return provinceDetail;
    }

    private static synchronized CategoryHelper GetCategoryClient() {
        if (config == null) {
            ClientConfig _config = ConfigUtils.GetOnlineClientConfig();
            config = _config;
        }
        if (cateHelper == null)
            cateHelper = new CategoryHelper(GetProductClient().getOrientClient(), config);
        return cateHelper;
    }

    private Boolean storeIsRunning(StoreProvinceInfoBO data) {
        Timestamp today = new Timestamp(System.currentTimeMillis());
        if (data.getOpeningDay().before(today) && today.before(data.getClosingDay())) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private Boolean saveIndex(List<ProvinceDetailPO> listDetail) {
        try {
            // ClientConfig _config = ConfigUtils.GetOnlineClientConfig();
            for (ProvinceDetailPO data4Push : listDetail) {
                String esKeyTerm = data4Push.getProvinceId().toString();
                ElasticClientWrite.getInstance("172.16.3.23").IndexObject("bhx_storeprovinceinfo", data4Push,
                        esKeyTerm);
                // ElasticClientWrite.getInstance("10.1.6.151").IndexObject("bhx_storeprovinceinfo",
                // data4Push, esKeyTerm);
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    @RequestMapping(value = "/getData", method = RequestMethod.GET)
    public Object findByProvinceId(int provinceId) {
        try {
            updateDataElasticsearch();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        try {
            var helper = GetStoreHelper();
            return helper.getStoreIdByProvinceDistrictWard(3, 30, 10311);
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//		List<ProvinceDetailPO> result = GetStoreHelper().bHXFindStoreProvinceById(provinceId);
//		return result;
        return null;
    }

    @RequestMapping(value = "/getstoreconfig", method = RequestMethod.GET)
    public Object getStoreConfig(int provinceId) {
        try {
            var helper = GetStoreHelper();
            return helper.getAllProvince();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//		List<ProvinceDetailPO> result = GetStoreHelper().bHXFindStoreProvinceById(provinceId);
//		return result;
        return null;
    }

    @RequestMapping(value = "/testlisting", method = RequestMethod.GET)
    public ResponseEntity<ProductBO> testListing(int productID, int siteID, int provinceID, String langID) {
        ProductBO result = null;
        var status = HttpStatus.OK;
        try {
            var helper = GetProductClient();
            var ids = new int[]{productID};
            var list = helper.GetSimpleProductListByListID_PriceStrings_soMap(ids, siteID, provinceID, langID);
            if (list != null && list.length > 0) {
                result = list[0];
            }
        } catch (Throwable e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            LogHelper.WriteLog(e);
        }
        return new ResponseEntity<>(result, status);
    }
    
    @RequestMapping(value = "/testlog", method = RequestMethod.GET)
    public ResponseEntity<String> testLogs(int index) {
    	try {
    		if(index == 1) {
	    		String p1 = System.getProperty("user.dir");
	    		Logs.LogException(new NullPointerException("test log"));
	    		return new ResponseEntity<>(p1, HttpStatus.OK);
    		}else if(index == 2) {
    			Path currentRelativePath = Paths.get("");
    			String p1 = currentRelativePath.toUri().getPath();
//	    		Logs.LogException(new NullPointerException("test log"));
	    		return new ResponseEntity<>(p1, HttpStatus.OK);
    		}else {
    			String path = Utils.getCurrentDir();
//    			Logs.LogException(new NullPointerException("test log"));
	    		return new ResponseEntity<>(path, HttpStatus.OK);
    		}
    	}catch(Exception ex) {
    		return new ResponseEntity<>(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(ex), HttpStatus.OK);
    	}
    }

    @RequestMapping(value = "/gettopsold", method = RequestMethod.GET)
    public ResponseEntity<Map<Integer, List<Integer>>> getTopSold(int siteID, int provinceID, String languageID) {
        var status = HttpStatus.OK;
        Map<Integer, List<Integer>> result = null;
        try {
            result = GetProductClient().getCachedTopProductSelling(siteID, provinceID, languageID);
        } catch (Throwable e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            LogHelper.WriteLog(e);
        }
        return new ResponseEntity<Map<Integer, List<Integer>>>(result, status);
    }


}
