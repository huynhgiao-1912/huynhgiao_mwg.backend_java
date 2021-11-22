package mwg.wb.business.rcm;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.NullArgumentException;

import mwg.wb.business.rcm.model.RecommendationModel;
import mwg.wb.business.rcm.model.Rule6Object;
import mwg.wb.business.rcm.model.TimeTrackingBO;
import mwg.wb.business.rcm.model.TrackingBO;
 
public class RuleHolder {
	private Map<Integer, Double> resultMap;
	private static final Double RULE_1 = 1.0;
	private static final Double RULE_2 = 1.0;
	private static final Double RULE_3 = 1.0;
	private static final Double RULE_4 = 1.0;
	private static final Double RULE_5 = 1.0;
	private static final Double RULE_6 = 1.0;
	private static final Double RULE_7 = 1.0;
	private static final Double RULE_8 = 1.0;
	
	public RuleHolder(List<RecommendationModel> list) {

		if(list != null) this.resultMap = convertToMap(list);
		else throw new NullArgumentException("List is null");
	}
	
	public Map<Integer, Double> convertToMap(List<RecommendationModel> beginList){
		Map<Integer, Double> map = beginList
				.stream()
				.collect(Collectors.toMap(model -> Integer.valueOf(model.item), 
										  RecommendationModel::getScore));
		return map;
	}
	
	public RuleHolder applyRule_1(List<Integer> ruleList) {
		throw new UnsupportedOperationException("Rule one unavailable");
	}
	
	public RuleHolder applyRule_3(List<TimeTrackingBO> ruleList) {
		System.out.println("Apply rule 2---------------");
		if(ruleList == null || ruleList.isEmpty()) return this;
		List<Integer> list = ruleList.stream()
									 .map(TrackingBO::getId)
									 .collect(Collectors.toList());
		applyPointPolicy(list, RULE_2);
		
		return this;
	}
	
	public RuleHolder applyRule_4(List<Integer> ruleList) {
		throw new UnsupportedOperationException("Rule 4 unavailable");
	}
	
	public RuleHolder applyRule_5(List<Integer> ruleList) {
		throw new UnsupportedOperationException("Rule 5 unavailable");
	}
	
	public RuleHolder applyRule_6(Map<Integer, Rule6Object> ruleMap) {
		System.out.println("Apply rule 6---------------");
		if(ruleMap.isEmpty()) return this;
		resultMap.keySet().forEach(key -> {
			if(ruleMap.containsKey(key)) {
				Long weeks = ruleMap.get(key).getWeeks();
				List<Integer> listCount = ruleMap.get(key).getCount();
				int sum = listCount.stream().reduce(0, Integer::sum);
				float avg = sum/(float)listCount.size();
				
				//Sản phẩm có thời gian trung bình mua > 1 tháng/lần và sau 3 tuần
				if(avg > 0 && avg < 1 && weeks >= 3) {
					applyPointPolicy(key, RULE_6);
				}
				//Sản phẩm có thời gian trung bình 1 tháng/lần và sau 2 tuần
				else if(avg == 1 && weeks >= 2) {
					applyPointPolicy(key, RULE_6);
				}
				//Sản phẩm có thời gian mua trung bình 2, 3 lần/tháng và sau 2 tuần
				else if(avg > 2 && weeks >= 2) {
					applyPointPolicy(key, RULE_6);
				}
				
			}
		});
//		applyPointPolicy(temp, RULE_6);
		return this;
	}
	
	public RuleHolder applyRule_7(List<TimeTrackingBO> ruleList) {
		System.out.println("Apply rule 7---------------");
		if(ruleList == null || ruleList.isEmpty()) return this;
		List<Integer> products = ruleList.stream()
										 .map(TimeTrackingBO::getId)
										 .collect(Collectors.toList());
		applyPointPolicy(products, RULE_7);
		return this;
	}
	
	public RuleHolder applyRule_8(List<TimeTrackingBO> ruleList) {
		System.out.println("Apply rule 8---------------");
		if(ruleList == null || ruleList.isEmpty()) return this;
		List<Integer> products = ruleList.stream()
										 .map(TimeTrackingBO::getId)
										 .collect(Collectors.toList());
		applyPointPolicy(products, RULE_8);
		return this;
	}
	
	public void applyPointPolicy(List<Integer> ruleList, Double point) {
		ruleList.stream().forEach(value -> {
			if(resultMap.containsKey(value)) {
				Double p = resultMap.get(value);
				resultMap.replace(value, p + point);
			}
		});
	}
	
	public void applyPointPolicy(Integer key, Double point) {
		if(resultMap.containsKey(key)) {
			Double p = resultMap.get(key);
			resultMap.replace(key, p + point);
		}
	}
	
	public List<Integer> getResult(){
		System.out.println("Result---------------");
		List<Integer> lstResult  = resultMap.entrySet().stream().sorted((e1,e2)-> {
			if(e1.getValue() < e2.getValue()) return 1;
			return -1;
		})
		.map(entry -> entry.getKey()).collect(Collectors.toList());
		
		return lstResult;
	}
	
	public void printMap() {
		for(Map.Entry<Integer, Double> entry : resultMap.entrySet()) {
			System.out.println(String.format("%d -> %.1f", entry.getKey(), entry.getValue()));
		}
	}
	
	public void printMap(Map<Integer,Double> mapList) {
		for(Map.Entry<Integer, Double> entry : mapList.entrySet()) {
			System.out.println(String.format("%d -> %.1f", entry.getKey(), entry.getValue()));
		}
	}
}
