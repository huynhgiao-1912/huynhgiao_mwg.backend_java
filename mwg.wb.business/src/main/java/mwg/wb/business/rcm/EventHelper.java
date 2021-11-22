package mwg.wb.business.rcm;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.predictionio.sdk.java.EngineClient;
import org.apache.predictionio.sdk.java.EventClient;
import org.joda.time.DateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mwg.wb.business.rcm.model.RecommendationModel;
import mwg.wb.model.api.ClientConfig;
 

public class EventHelper {
	private EventClient eventClient;
	private EngineClient engineClient;
	ClientConfig config = null;

 
	public EventHelper(ClientConfig aconfig) {
		this.config = aconfig;
		 
		eventClient = new EventClient(config.RCM_PIO_ACCESS_KEY,config.RCM_PIO_APP_URL);
		engineClient = new EngineClient(config.RCM_PIO_ENGINE_URL);
	}
	
	public List<RecommendationModel> GetRecommendationByItem(String user,String item,int top) throws ExecutionException, InterruptedException, IOException {
  
		 
//		  Map<String, Object> fieldsArray = new HashMap<String, Object>();
//	        fieldsArray.put("name", "productname");
	      
//		JsonObject response = engineClient.sendQuery(ImmutableMap.<String, Object>of(
//           // "items", ImmutableList.of(item),
//    		
//    		
//            "fields", fieldsArray ,
//               "item", item ,  
//            
//            "num",  top));
//      
		
		
		
		
		
		
		
//		
//		JsonObject response = engineClient.sendQuery(ImmutableMap.<String,
//				Object>of(
//				"item",  item ,
//				"num", top,
//				"fields",  	ImmutableMap.<String, Object>of(
//				"name", "productname" 
//				  
//				))
//				   
//				  
//				 );
		JsonObject response = engineClient.sendQuery(ImmutableMap.<String,
				Object>of(
				"item",  item ,
				//"user",  user ,
				"num", top
				 
				   )
				  
				 );
		// System.out.println(response.getAsString());
    JsonArray recommendationsJsonArray = response.getAsJsonArray("itemScores");
  
    Iterator<JsonElement> iterator = recommendationsJsonArray.iterator();
    List<JsonElement> recommendationsJsonElements = new ArrayList<>();
    while (iterator.hasNext()) {
        recommendationsJsonElements.add(iterator.next());
    }
    Gson gson = new Gson();
    return recommendationsJsonElements.stream()
            .map(el -> gson.fromJson(el, RecommendationModel.class))
            .collect(Collectors.toList());
	}
	

    public List<RecommendationModel> GetRecommendationByUser(final String userId, final Integer numberOfRecommendations) {
          try {
            JsonObject response = engineClient.sendQuery(ImmutableMap.of("user", userId, "num", numberOfRecommendations));

            JsonArray recommendationsJsonArray = response.getAsJsonArray("itemScores");

            Iterator<JsonElement> iterator = recommendationsJsonArray.iterator();
            List<JsonElement> recommendationsJsonElements = new ArrayList<>();
            while (iterator.hasNext()) {
                recommendationsJsonElements.add(iterator.next());
            }
            Gson gson = new Gson();
            return recommendationsJsonElements.stream()
                    .map(el -> gson.fromJson(el, RecommendationModel.class))
                    .collect(Collectors.toList());
        } catch (ExecutionException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
	public String setUserEvent(String user, DateTime dt, Map<String, Object> userProps)
			throws ExecutionException, InterruptedException, IOException {
		String rs = eventClient.setUser(user, userProps, dt);
		return rs;
	}

	public String setItemEvent(String item, DateTime dt, Map<String, Object> itemProps)
			throws ExecutionException, InterruptedException, IOException {
		String rs = eventClient.setItem(item, itemProps, dt);
		return rs;
	}
/*
 * 16:27 61814_ThanhPhi_BEWeb {
  "event" : "purchase",
  "entityType" : "user",
  "entityId" : "2",
  "targetEntityType" : "item",
  "targetEntityId" : "114115",
  "eventTime" : "2020-09-09T12:34:56.123-08:00"
}
16:27 61814_ThanhPhi_BEWeb "eventNames": ["purchase", "view"]
 * */
	public String setActionEvent(String user, String item, String action, Map<String, Object> actionProps)
			throws ExecutionException, InterruptedException, IOException {
		String rs = eventClient.userActionItem(action, user, item, actionProps);
		return rs;
	}
}