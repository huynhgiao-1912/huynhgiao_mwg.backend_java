package mwg.wb.business.helper;

import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.client.elasticsearch.ElasticClient;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.promotion.BillPromotionBHX;

public class BillPromotionHelper {
	 public ElasticClient elasticClient = null;
	 public ObjectMapper mapper = null;
	 
	 public BillPromotionHelper(ClientConfig config) {
		 elasticClient = ElasticClient.getInstance(config.SERVER_ELASTICSEARCH_READ_HOST);
	 }
	 
	 public List<BillPromotionBHX> getBillPromotionBHX() throws Throwable {

		 SearchSourceBuilder searchQuery = new SearchSourceBuilder();
		 searchQuery.query(QueryBuilders.matchAllQuery());
		 SearchResponse searchResponse = elasticClient.searchRequest(searchQuery, "bhx_new_promo_bill");
		 List<BillPromotionBHX> billPromotions = elasticClient.getSource(searchResponse, BillPromotionBHX.class);
		 if(billPromotions != null && !billPromotions.isEmpty()) {
			 billPromotions.stream()
					 .filter(BillPromotionHelper::validBillPromotion)
					 .collect(Collectors.toList());
		 }
		 return billPromotions;
	 }
	 
	 public static boolean validBillPromotion(BillPromotionBHX billpromotion) {
		 if(billpromotion.BeginDate.compareTo(Utils.GetCurrentDate()) <= 0
					&& billpromotion.EndDate.compareTo(Utils.GetCurrentDate()) >= 0) {
			return true;
		 }
		 return false;
	 }
}
