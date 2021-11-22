package mwg.wb.business;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.common.GConfig;
import mwg.wb.common.ProcessConfigSO;
import mwg.wb.common.ProcessInfoSO;
import mwg.wb.common.Utils;

public class JobHelper {
	public static String index_workers_process = "workers_process";
	public static String index_workers_config = "workers_config";
	private RestHighLevelClient clientIndex = ElasticClient
			.getInstance(WorkerHelper.GetWorkerClientConfig().SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
	ObjectMapper mapper = null;
//	public boolean UpdateProcessInfo(int State) {
//		// TODO: order
//		Map<String, Object> pricesMap = new HashMap<String, Object>();
//		String esKeyTerm = "";
//		try {
//
//			var update = new UpdateRequest(indexDB, esKeyTerm).doc("{\"State\":" + State + " }", XContentType.JSON)
//					.docAsUpsert(true).detectNoop(false);
//			var client = ElasticClient.getInstance().getClient();
//			var response = client.update(update, RequestOptions.DEFAULT);
//			if (response != null && response.getResult() == Result.UPDATED) {
//				Logs.WriteLine("ES");
//				return true;
//			} else {
//
//				Logs.WriteLine(
//						"-Index price status to ES FAILED: " + response.getResult().name() + "######################");
//				return false;
//			}
//		} catch (Exception e) {
//			Logs.WriteLine("Exception Index price status to ES FAILED: ");
//			Logs.WriteLine(e);
//			return false;
//		}
//
//	}

//	public boolean UpdateStatus(int State) {
//		// TODO: order
//		Map<String, Object> pricesMap = new HashMap<String, Object>();
//		String esKeyTerm = "";
//		try {
//
//			var update = new UpdateRequest(indexDB, esKeyTerm).doc("{\"State\":" + State + " }", XContentType.JSON)
//					.docAsUpsert(true).detectNoop(false);
//			var client = ElasticClient.getInstance().getClient();
//			var response = client.update(update, RequestOptions.DEFAULT);
//			if (response != null && response.getResult() == Result.UPDATED) {
//				Logs.WriteLine("ES");
//				return true;
//			} else {
//
//				Logs.WriteLine(
//						"-Index price status to ES FAILED: " + response.getResult().name() + "######################");
//				return false;
//			}
//		} catch (Exception e) {
//			Logs.WriteLine("Exception Index price status to ES FAILED: ");
//			Logs.WriteLine(e);
//			return false;
//		}
//
//	}

	public JobHelper() {

		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public ProcessConfigSO GetConfig() throws Throwable {

		try {
			ProcessConfigSO result = ElasticClient
					.getInstance(WorkerHelper.GetWorkerClientConfig().SERVER_ELASTICSEARCH_READ_HOST)
					.GetSingleObject(index_workers_config, "config", ProcessConfigSO.class);
			return result;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public List<ProcessInfoSO> GetListJob(long Thread, String className, int isActive) {
		SearchSourceBuilder sb = new SearchSourceBuilder();
//		sb.fetchSource(new String[] { "ProductID", "Prices.WebStatusId_" + qry.ProvinceId,
//				"Prices.IsShowHome_" + qry.ProvinceId, "Prices.Price_" + qry.ProvinceId,
//				"Prices.ProductCode_" + qry.ProvinceId }, null);

		var query = boolQuery();
		query.must(termQuery("className", className));

		SearchResponse queryResults = null;
		SearchRequest searchRequest = new SearchRequest(index_workers_process);

		searchRequest.source(sb.query(query));
		try {
			queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		var solist = new ArrayList<ProcessInfoSO>();

		queryResults.getHits().forEach(h -> {
			try {
				var lmao = h.getSourceAsString();
				var so = mapper.readValue(lmao, ProcessInfoSO.class);
				solist.add(so);
				System.out.println(h.getScore() + ": " + lmao);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		return solist;
	}

	public boolean JobUpdate(String IP, long ThreadID, String ClassName) {
		// TODO: order

		ProcessInfoSO document = new ProcessInfoSO();
		document.IP = IP;
		document.ThreadID = ThreadID;
		document.ActiveDate = Utils.GetCurrentDate();
		document.Term = IP + String.valueOf(ThreadID);

		try {
			//var result = ElasticClient.getInstance(WorkerHelper.GetWorkerClientConfig().SERVER_ELASTICSEARCH_WRITE_HOST)
			//		.IndexObject(index_workers_process, "process", document, document.Term);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}
}
