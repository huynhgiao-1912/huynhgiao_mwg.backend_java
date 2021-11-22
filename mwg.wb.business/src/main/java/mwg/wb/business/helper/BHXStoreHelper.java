package mwg.wb.business.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import mwg.wb.business.CacheStaticHelper;
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.Utils;
import mwg.wb.common.bhx.ProvinceDetailPO;
import mwg.wb.common.bhx.StoreDetailPO;
import mwg.wb.model.api.ClientConfig;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

public class BHXStoreHelper {

    public static RestHighLevelClient clientIndex = null;
    public ElasticClient elasticClient = null;
    public ObjectMapper mapper = null;
    public ORThreadLocal factoryRead = null;
    public ClientConfig config = null;
    
    private List<ProvinceDetailPO> listProvince = null;
	 
    public BHXStoreHelper(ObjectMapper _mapper, ClientConfig config) {
    	String readHost = config.SERVER_ELASTICSEARCH_READ_HOST;//"10.1.6.151";
        elasticClient = ElasticClient.getInstance(readHost);
        clientIndex = elasticClient.getClient();
    	 
        mapper = _mapper;
    }
    
    

    public List<ProvinceDetailPO> bHXFindStoreProvinceById(Integer provinceId) throws IOException{
    	if(provinceId <= 0)
    		return null;
        var q = matchQuery("provinceId", provinceId);
        var sb = new SearchSourceBuilder().query(q);
        var sr = new SearchRequest().indices("bhx_storeprovinceinfo").source(sb);
       
        var data = clientIndex.search(sr, RequestOptions.DEFAULT);
        List<String> jsons = new ArrayList<>();
        Stream.of(data.getHits().getHits()).forEach(x -> {
            jsons.add(x.getSourceAsString());
        });
        List<ProvinceDetailPO> result = new ArrayList<>();
        if(jsons != null && jsons.size() != 0){
            Gson g = new Gson();
            jsons.forEach(item -> result.add(g.fromJson(item, ProvinceDetailPO.class)));
        }
        return result;        
    }
    
    public StoreDetailPO getStoreDetailByStore(int storeId) throws Throwable
    {
    	if(storeId <= 0)
    		return null;
    	listProvince = getAllProvince();
    	if(listProvince == null)
    		return null;
    	
    	var option = listProvince.stream().filter(
    			x->x.getStoreIds().stream().anyMatch(y->y.getStoreId() == storeId || Utils.toInt(y.getExpStore()) == storeId)).map(Optional::ofNullable)
				.findFirst().orElse(null);

    	if(option != null && !option.isEmpty())
    	{
    		var detail = option.get();
    		return detail.getStoreIds().stream().filter(x->x.getStoreId() == storeId || Utils.toInt(x.getExpStore()) == storeId).findFirst().get();
    	}
    	return null;
    }
    
    public int[] getAllStore() throws Throwable {
    	listProvince = getAllProvince();
        List<Integer> result = new ArrayList<Integer>();
        for(ProvinceDetailPO item : listProvince) {
        	if(item.getStoreIds() != null && item.getStoreIds().size() > 0) {
        		for(StoreDetailPO detail : item.getStoreIds()) {
        			if(detail.getStoreId() != null && !result.contains(detail.getStoreId())) {
        				if(detail.getStoreId() > 0) {
        					result.add(detail.getStoreId());
        				}
        			}
        			if(detail.getExpStore() != null && !result.contains(Integer.parseInt(detail.getExpStore()))) {
        				if(Integer.valueOf(detail.getExpStore()) > 0)
        					result.add(Integer.valueOf(detail.getExpStore()));
        			}
        		}
        	}
        }
        
        int[] array = new int[result.size()];
        for(int i = 0; i < result.size(); i++) array[i] = result.get(i);
        return array;
    }
  
    public List<Integer> getAllProvinceId() throws Throwable {
    	listProvince = getAllProvince();
    	List<Integer> result = new ArrayList<Integer>();
    	for(ProvinceDetailPO item : listProvince) {
    		result.add(item.getProvinceId());
    	}
    	return result;
    }
    
    public Integer getProvinceByStore(Integer storeId) throws Throwable {
    	if(storeId <= 0)
    		return 0;
    	listProvince = getAllProvince();
    	for(ProvinceDetailPO item : listProvince) {
    		List<StoreDetailPO> result = item.getStoreIds().stream().filter(
    	    			x -> x.getStoreId().equals(storeId) || x.getExpStore().equals(storeId.toString())).collect(Collectors.toList());
    		if(result != null && result.size() > 0) {
    			return item.getProvinceId();
    		}
    	}
    	return null;
    }
    
    public int[] getListNormalStore() throws Throwable {
    	listProvince = getAllProvince();
        List<Integer> result = new ArrayList<Integer>();
        for(ProvinceDetailPO item : listProvince) {
        	if(item.getStoreIds() != null && !item.getStoreIds().isEmpty()) {
        		for(StoreDetailPO detail : item.getStoreIds()) {
        			result.add(detail.getStoreId());
        		}
        	}
        }
        int[] array = new int[result.size()];
        for(int i = 0; i < result.size(); i++) array[i] = result.get(i);
        return array;
    }
    
    public int[] getListOutStockStore() throws Throwable
    {
    	listProvince = getAllProvince();
    	List<Integer> result = new ArrayList<Integer>();
        for(ProvinceDetailPO item : listProvince) {
        	if(item.getStoreIds() != null && !item.getStoreIds().isEmpty()) {
        		for(StoreDetailPO detail : item.getStoreIds()) {
        			result.add(Integer.valueOf(detail.getExpStore()));
        		}
        	}
        }
        int[] array = new int[result.size()];
        for(int i = 0; i < result.size(); i++) array[i] = result.get(i);
        return array;
    }
    
    public Integer getStoreIdByProvinceDistrictWard(Integer provinceId, Integer districtId, Integer wardId) throws Throwable
    {
    	listProvince = getAllProvince();
        for(ProvinceDetailPO item : listProvince) {
        	if(item.getProvinceId().equals(provinceId)) {
        		if(item.getStoreIds() != null && !item.getStoreIds().isEmpty()) {
        			for(StoreDetailPO store : item.getStoreIds()) {
        				if(store.getDistrictId().equals(districtId) && store.getWardId().equals(wardId)) {
        					return store.getStoreId();
        				}
        			}
        		}
        	}
        }
        return null;
    }
    
//    public List<Integer> convertData (SearchResponse data) {
//    	listProvinceId = (List<Integer>) Stream.of(data.getHits().getHits()).map(x -> {        	
//			JSONObject object = new JSONObject(x.getSourceAsString());
//			return Integer.valueOf(object.get("provinceId").toString());		
//        }).collect(Collectors.toList());
//    	return listProvinceId;
//	}
     
    public List<ProvinceDetailPO> getAllProvince () throws Throwable {
    	
    	@SuppressWarnings("unchecked")
		var listProvinceStore = (List<ProvinceDetailPO>) CacheStaticHelper.GetFromCache("listProvinceStore", 5);
    	if(listProvinceStore != null) {
    		return  listProvinceStore;
    		
    	}else {
    		SearchRequest searchRequest = new SearchRequest("bhx_storeprovinceinfo");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchRequest.source(searchSourceBuilder);        
           
    		var data = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
            listProvinceStore = (List<ProvinceDetailPO>) Stream.of(data.getHits().getHits()).map(x -> {
            	try {
    				return mapper.readValue(x.getSourceAsString(), ProvinceDetailPO.class);
    			} catch (Exception e) {
    				e.printStackTrace();
    				return null;
    			}
            }).collect(Collectors.toList());
            //có dữ liệu thì mới add cache
            if(listProvinceStore != null && !listProvinceStore.isEmpty()) {
            	CacheStaticHelper.AddToCache("listProvinceStore", listProvinceStore);
            }
            
            return listProvinceStore;
    	}
	}
}
