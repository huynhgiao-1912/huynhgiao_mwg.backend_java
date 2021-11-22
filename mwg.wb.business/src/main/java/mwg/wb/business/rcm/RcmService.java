package mwg.wb.business.rcm;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections.map.HashedMap;

import mwg.wb.business.rcm.helper.ElasticService;
import mwg.wb.business.rcm.model.ApiResultBO;
import mwg.wb.business.rcm.model.ApiTrackingBO;
import mwg.wb.business.rcm.model.RecommendationBO;
import mwg.wb.business.rcm.model.RecommendationModel;
import mwg.wb.business.rcm.model.Rule6Object;
import mwg.wb.business.rcm.model.TimeTrackingBO;

 

 
public class RcmService {

	private ElasticService elasticService;
	private EventHelper eventHelper;

//	@Autowired
//	private List<RecommendationModel> model;

	public RcmService(EventHelper eventHelper, ElasticService elasticService) {
		this.elasticService = elasticService;
		this.eventHelper = eventHelper;
	}
//
//	public List<Integer> getBestBuyProducts(String username, Integer productid)
//			throws IOException, ExecutionException, InterruptedException {
////		SearchSourceBuilder searchBuilder = new SearchSourceBuilder();
////		searchBuilder.query(QueryBuilders.matchQuery("id", username));
////		searchBuilder.docValueField("tk_buy_count_list");
//
////		List<Integer> predictList = List.of(1, 3, 5 ,8, 9, 100, 18, 10, 80, 17, 21);
////		List<RecommendationModel> predictList = eventHelper.GetRecommendationByItem(String.valueOf(productid)) ;
////		List<Integer> predictList = kq.stream().map(value -> {
////			return Integer.valueOf(value.item);
////		})
////		.collect(Collectors.toList());
//
////		List<RecommendationModel> predictList = model;
//		List<RecommendationModel> predictList = eventHelper.GetRecommendationByItem(String.valueOf(productid), 20);
//		if (predictList == null || predictList.isEmpty())
//			return List.of();
//		// RULE 2 lấy từ predictionIO
//		RuleHolder ruler = new RuleHolder(predictList);
//
//		RecommendationBO recommendation = elasticService.getSourceById(username, RcmAppConfig.RCM_INDEX,
//				RecommendationBO.class);
//		if (recommendation != null) {
//			/*
//			 * RULE 3 Từ danh sách có từ rule số 2 tiếp tục lọc ra các sản phẩm mua
//			 * "nhiều lần" trong 6 tháng
//			 */
//
//			List<TimeTrackingBO> rule_3 = recommendation.tk_buy_product_count_list;
//			ruler.applyRule_3(rule_3);
//
//			/*
//			 * RULE 6 Sản phẩm đã đã mua sắp tới thời điểm mua lại key yyyyMM_productid
//			 * id:productid, time : thoi gian mua gan nhat, count
//			 * 
//			 * MÔ TẢ Sản phẩm có thời gian trung bình mua > 1 tháng/lần => 3 tuần gần tới
//			 * ngày mua lại thì mới Recommendation Sản phẩm có thời gian trung bình 1
//			 * tháng/lần => 2 tuần gần tới ngày mua thì mới recommendation Sản phẩm có thời
//			 * gian mua trung bình 2, 3 lần/tháng thì recommendation 2 tuần trước ngày mua"
//			 **/
//
//			List<TimeTrackingBO> rule_6 = recommendation.tk_buy_product_inmonth_count_list;
//
//			// Rule6Object: count each months, productid, weeks from now
//			Map<Integer, Rule6Object> map_rule_6 = new HashMap<Integer, Rule6Object>();
//			LocalDateTime now = LocalDateTime.now();
//			rule_6.forEach(object -> {
//
//				Rule6Object rule6Object = map_rule_6.get(object.id);
//
//				if (object.timestamp != null) {
//					long weeks = object.timestamp.until(now, ChronoUnit.WEEKS);
//
//					if (rule6Object == null) {
//						rule6Object = new Rule6Object();
//						rule6Object.weeks = weeks;
//						rule6Object.count = new ArrayList<Integer>();
//						rule6Object.count.add(object.count);
//						map_rule_6.put(object.id, rule6Object);
//					} else {
//						if (weeks < rule6Object.weeks) {
//
//							rule6Object.weeks = weeks;
//						}
//						rule6Object.getCount().add(object.count);
//					}
//				}
//			});
//
////			for(Entry<Integer, Rule6Object> entry : map_rule_6.entrySet()) {
////				System.out.println("ProductId == " + entry.getKey());
////				System.out.println(entry.getValue());
////			}
//
//			ruler.applyRule_6(map_rule_6);
//
//			/*
//			 * RULE 7 Sản phẩm đã bỏ giỏ hàng ,Các sản phẩm đã bỏ giỏ hàng mà chưa mua
//			 */
//			List<TimeTrackingBO> rule_7 = recommendation.tk_product_addtocart_notbuy_count_list;
//			ruler.applyRule_7(rule_7);
//
//			/*
//			 * RULE 8 Sản phẩm xem nhiều và có thời gian xem nhiều
//			 */
//
//			// 8-1 Sản phẩm xem nhiều
//			List<TimeTrackingBO> rule_8_1 = recommendation.tk_buy_product_count_list;
//			ruler.applyRule_8(rule_8_1);
//
//			// 8-2 thời gian xem sản phẩm
//			// count chứa thời gian xem ms
//			List<TimeTrackingBO> rule_8_2 = recommendation.tk_viewed_product_time_list;
//			ruler.applyRule_8(rule_8_2);
//
//			// In kết quả
////			ruler.printMap();
//
//		}
//
//		return ruler.getResult();
//
//	}

	public ApiResultBO GetRecommendationByItem(String username, int productid, int top)
			throws IOException, ExecutionException, InterruptedException {
//		SearchSourceBuilder searchBuilder = new SearchSourceBuilder();
//		searchBuilder.query(QueryBuilders.matchQuery("id", username));
		List<RecommendationModel> kq = eventHelper.GetRecommendationByItem(username,String.valueOf(productid), top);
		// List<RecommendationModel> kq=new ArrayList<RecommendationModel>();
//		List<RecommendationModel> kq = model;
//		System.out.println("model = " + model.size());
		// đây là rule 2
		ApiResultBO rsMessage=new ApiResultBO();
		Map<String, ApiTrackingBO> mapR = new HashMap<String, ApiTrackingBO>();
		Map<String, ApiTrackingBO> mapRS = new HashMap<String, ApiTrackingBO>();

		for (RecommendationModel i : kq) {
			ApiTrackingBO rd = new ApiTrackingBO();
			rd.id = i.item;
			rd.score = i.score;
			String[] datalist = (i.getData() + "").split("\\|");
			if (datalist.length >= 3) { 
				rd.productname = datalist[0];
				rd.manuid = datalist[1];
				rd.cateid = datalist[2];
				rd.buycount = datalist[3];
			}

			mapR.put(String.valueOf(i.item), rd);
			// System.out.println("sp" + rd.id + ":" + i.score);
		}

		if (mapR != null) {

			RecommendationBO recommendation = elasticService.getSourceById(username, "rcm_customer",
					RecommendationBO.class);
			if (recommendation != null) {

				Map<String, TimeTrackingBO> map_tk_buy_product_count_list = new HashMap<String, TimeTrackingBO>();
				if (recommendation.tk_buy_product_count_list != null) {
					for (TimeTrackingBO i : recommendation.tk_buy_product_count_list)
						map_tk_buy_product_count_list.put(String.valueOf(i.id), i);
				}
				Map<String, TimeTrackingBO> map_tk_product_addtocart_notbuy_count_list = new HashMap<String, TimeTrackingBO>();
				if (recommendation.tk_product_addtocart_notbuy_count_list != null) {
					for (TimeTrackingBO i : recommendation.tk_product_addtocart_notbuy_count_list)
						map_tk_product_addtocart_notbuy_count_list.put(String.valueOf(i.id), i);
				}

				Map<String, TimeTrackingBO> map_tk_viewed_product_time_list = new HashMap<String, TimeTrackingBO>();
				if (recommendation.tk_viewed_product_time_list != null) {
					for (TimeTrackingBO i : recommendation.tk_viewed_product_time_list)
						map_tk_viewed_product_time_list.put(String.valueOf(i.id), i);
				}
				Map<String, TimeTrackingBO> map_tk_viewed_product_count_list = new HashMap<String, TimeTrackingBO>();
				if (recommendation.tk_viewed_product_count_list != null) {
					for (TimeTrackingBO i : recommendation.tk_viewed_product_count_list)
						map_tk_viewed_product_count_list.put(String.valueOf(i.id), i);
				}
				List<TimeTrackingBO> rule_6 = recommendation.tk_buy_product_inmonth_count_list;
				rsMessage.tk_buy_manu_count_list=recommendation.tk_buy_manu_count_list;
				// Rule6Object: count each months, productid, weeks from now
				Map<Integer, Rule6Object> map_rule_6 = new HashMap<Integer, Rule6Object>();
				if (rule_6 != null && !rule_6.isEmpty()) {
					LocalDateTime now = LocalDateTime.now();
					rule_6.forEach(object -> {

						Rule6Object rule6Object = map_rule_6.get(object.id);

						if (object.timestamp != null) {
							long weeks = object.timestamp.until(now, ChronoUnit.WEEKS);

							if (rule6Object == null) {
								rule6Object = new Rule6Object();
								rule6Object.weeks = weeks;
								rule6Object.count = new ArrayList<Integer>();
								rule6Object.count.add(object.count);
								map_rule_6.put(object.id, rule6Object);
							} else {
								if (weeks < rule6Object.weeks) {

									rule6Object.weeks = weeks;
								}
								rule6Object.getCount().add(object.count);
							}
						}
					});

				}

				for (Entry<String, ApiTrackingBO> item : mapR.entrySet()) {
					String pid = item.getValue().id;
					String manuid = item.getValue().manuid;
					String productname = item.getValue().productname;
					String cateid = item.getValue().cateid;
					String buycount = item.getValue().buycount;	
					// double score = 1;// cho thanh 1 hết item.getValue().score;
					double sc=item.getValue().score;
					if(sc<=0) continue;
					double score = 1 + 0.0001 * item.getValue().score;
					String msg = "rcom:" + score;
					// rule 3 Từ danh sách có từ rule số 2 tiếp tục lọc ra các sản phẩm MUA "nhiều
					// lần" trong 6 tháng
					// bo rule nay, da co trogn rule recom
//					TimeTrackingBO i1 = map_tk_buy_product_count_list.get(pid);
//					if (i1 != null) {
//						score = score + 100 + 0.0 * i1.count;// score
//						msg=msg+",r3:"+score;
//					}

					// rule 6 Sản phẩm đã đã mua sắp tới thời điểm mua lại
					// key yyyyMM_productid id:productid, time : thoi gian mua gan nhat, count
					if (map_rule_6 != null && !map_rule_6.isEmpty()) {
						Long weeks = map_rule_6.get(pid).getWeeks();
						List<Integer> listCount = map_rule_6.get(pid).getCount();
						int sum = listCount.stream().reduce(0, Integer::sum);
						float avg = sum / (float) listCount.size();

						// Sản phẩm có thời gian trung bình mua > 1 tháng/lần và sau 3 tuần
						if (avg > 0 && avg < 1 && weeks >= 3) {
							score = score + 1;
							msg = msg + ",r6_1:" + score;

						}
						// Sản phẩm có thời gian trung bình 1 tháng/lần và sau 2 tuần
						else if (avg == 1 && weeks >= 2) {
							score = score + 1;
							msg = msg + ",r6_2:" + score;
						}
						// Sản phẩm có thời gian mua trung bình 2, 3 lần/tháng và sau 2 tuần
						else if (avg >= 2 && weeks >= 2) {
							score = score + 1;
							msg = msg + ",r6_3:" + score;
						}

					}
					/*
					 * "- Sản phẩm có thời gian trung bình mua > 1 tháng/lần => 3 tuần gần tới ngày
					 * mua lại thì mới Recommendation - Sản phẩm có thời gian trung bình 1 tháng/lần
					 * => 2 tuần gần tới ngày mua thì mới recommendation - Sản phẩm có thời gian mua
					 * trung bình 2, 3 lần/tháng thì recommendation 2 tuần trước ngày mua"
					 */
//chua lam

					// ruke 7 Sản phẩm đã bỏ giỏ hàng ,Các sản phẩm đã bỏ giỏ hàng mà chưa mua

					TimeTrackingBO i2 = map_tk_product_addtocart_notbuy_count_list.get(pid);
					if (i2 != null) {
						score = score + 1;// score
						msg = msg + ",r7:" + score;
					}

					// rule 8 Sản phẩm xem nhiều và có thời gian xem nhiều ,"Trong 3 tuần gần nhất.
					// Liệt kê các sản phẩm KH quan tâm nhất thể hiện ở:
					// Số lần click vào sản phẩm + thời gian xem sản phẩm"
					// 8-1 Sản phẩm xem nhiều
					TimeTrackingBO i3 = map_tk_viewed_product_count_list.get(pid);
					if (i3 != null) {
						score = score + 1;// score
						msg = msg + ",r81:" + score;
					}

					// 8-2 thời gian xem sản phẩm
					TimeTrackingBO i4 = map_tk_viewed_product_time_list.get(pid);
					if (i4 != null) {
						score = score + 1;// score
						msg = msg + ",r82:" + score;
					}
					ApiTrackingBO newitem = new ApiTrackingBO();
					newitem.id = pid;
					newitem.score = score;
					newitem.msg = msg;
					newitem.cateid=cateid;
					newitem.manuid=manuid;
					newitem.buycount=buycount;
					newitem.productname=productname;
					mapRS.put(pid, newitem);
				}

				// Thái giải
				// rule 9 Sản phẩm cùng thường hiệu thường mua
				// List<TimeTrackingBO> rule_9 = recommendation.tk_buy_manu_count_list; //
				// ruler.applyRule_2(rule_9);

//
//			List<TrackingBO> rule_3 = recommendation.tk_buy_count_list;
//			ruler.applyRule_3(rule_3);
//	
//			List<TimeTrackingBO> rule_8 = recommendation.tk_viewed_product_time_list;
//			ruler.applyRule_8(rule_8);
			}
		}
		ArrayList<ApiTrackingBO> valueList = new ArrayList<ApiTrackingBO>(mapRS.values());
		Collections.sort(valueList, Comparator.comparing(ApiTrackingBO::getScore));
		Collections.reverse(valueList);
		rsMessage.listproduct=valueList;
	
		// System.out.println("ket thuc :" + valueList);
		return rsMessage;

	}

}
