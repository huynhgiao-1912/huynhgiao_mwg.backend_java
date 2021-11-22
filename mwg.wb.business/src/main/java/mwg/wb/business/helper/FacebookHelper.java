package mwg.wb.business.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;
import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.fb.FBMessageType;
import mwg.wb.model.fb.FBSiteConfig;
import mwg.wb.model.fb.FBinpuModel;
import mwg.wb.model.fb.FacebookResult;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import mwg.wb.model.search.ProductSO;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class FacebookHelper {
    public String keyToken = "Backend_wewemPKz157845_Tgdd";
    private static ElasticClient elasticClient = null;
    private static final String INDEX_ELASTIC = "facebooknotify";
    public ObjectMapper mapper = null, esmapper = null;
    
    public FacebookHelper(ClientConfig config) {
        // http://172.16.3.105:9200/
        if(elasticClient == null){
            elasticClient  =ElasticClient.getInstance(config.SERVER_ELASTICSEARCH_FB_READ_HOST);
        }

        mapper = DidxHelper.generateJsonMapper(GConfig.DateFormatString);
        esmapper = DidxHelper.generateJsonMapper(GConfig.DateFormatStringNews);
    }

    public FacebookResult SendMessage(FBinpuModel fb) throws Exception {
        if(fb == null || fb.messagetype ==  null){
            return new FacebookResult(){{
                status = 400;
                message= "messageType không hợp lệ";
            }};
        }
        
        FBSiteConfig fbSiteConfig = getAccessTokenByPageID("1656824627935208");
        if(fbSiteConfig == null){
            return new FacebookResult(){{
                status = 500;
                message= "Token không hợp lệ";
            }};
        }
        
        if(!fb.token.equals(keyToken)){
            return new FacebookResult(){{
                status = 400;
                message= "Token không hợp lệ";
            }};
        }
        
        fb.message = fb.message.replace('"', '\"').replace("\n", "\\n");

        String postData = "";
        if (fb.messagetype == FBMessageType.CONFIRMORDER)
        {
            postData = "{\n  \"messaging_type\": \"RESPONSE\",\n  \"recipient\": {\n    \"id\": \"" + fb.senderID + "\"\n  },\n  \"message\": {\n    \"text\": \"" + fb.message + "\"\n  }\n}";
        }
        else
        if (fb.messagetype == FBMessageType.FEEDBACKORDER)
        {
            postData = "{\n  \"messaging_type\": \"MESSAGE_TAG\", \"tag\": \"CONFIRMED_EVENT_UPDATE\",\n  \"recipient\": {\n    \"id\": \"" + fb.senderID + "\"\n  },\n  \"message\": {\n    \"text\": \"" + fb.message + "\"\n  }\n}";
        }
        else
        if (fb.messagetype == FBMessageType.PROMOTION)
        {
            postData = "{\n  \"messaging_type\": \"MESSAGE_TAG\", \"tag\": \"CONFIRMED_EVENT_UPDATE\",\n  \"recipient\": {\n    \"id\": \"" + fb.senderID + "\"\n  },\n  \"message\": {\n    \"text\": \"" + fb.message + "\"\n  }\n}";
        }

//        var url = new URL("https://graph.facebook.com/v7.0/me/messages?access_token=" + fbSiteConfig.data);
//        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
//
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
//        connection.setConnectTimeout(5000); // 5s
//
//        connection.setDoOutput(true);// cho phép gửi body
//        try(OutputStream os = connection.getOutputStream()) {
//            byte[] input = postData.getBytes("utf-8");
//            os.write(input, 0, input.length);
//        }
//        connection.connect();
//
//        var r = new FacebookResult(){{
//            status = connection.getResponseCode();
//            message= "";
//            message_id = connection.getResponseMessage();
//        }};
//        try {
//            connection.disconnect();
//        }catch (Throwable e){ r.message = "disconnect fb thất bại"; }


        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(5000, TimeUnit.MILLISECONDS);
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, postData);
        Request request = new Request.Builder()
                .url("https://graph.facebook.com/v10.0/me/messages?access_token=" + fbSiteConfig.data)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();

        var r = new FacebookResult(){{
            status = response.code();
            message= response.message();
            message_id = response.body().string();
        }};

        try {
            //client.getCache().close();
            //client.getConnectionPool().evictAll();
            response.body().close();
            client.getDispatcher().getExecutorService().shutdown();
        }catch (Throwable e){ r.message = "disconnect fb thất bại"; }
        return r;
    }


	public FBSiteConfig getAccessTokenByPageID(String pageid) throws IOException {
        var q = boolQuery();
        q.must(termQuery("pageid", pageid));
        q.must(termQuery("userid", "602529473277685"));
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(q).from(0).size(2);
		var hit = elasticClient.searchObject(INDEX_ELASTIC, searchSourceBuilder);

		if(hit != null && hit.length > 0){
            var listResult = Arrays.stream(hit).map(h -> {
                try {
                    if(h.getType().equals("config")) // lọc trùng do con quỷ này sài es cũ còn dùng type
                        return esmapper.readValue(h.getSourceAsString(), FBSiteConfig.class);
                    return null;
                } catch (Exception e) {
                    return null;
                }
            }).filter(p -> p != null).toArray(FBSiteConfig[]::new);
            if(listResult != null && listResult.length > 0)
            {
                return listResult[0];
            }
        }


		return null;
	}
}
