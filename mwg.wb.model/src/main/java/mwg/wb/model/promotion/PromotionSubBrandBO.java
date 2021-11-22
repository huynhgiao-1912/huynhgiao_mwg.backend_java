package mwg.wb.model.promotion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PromotionSubBrandBO {
	public String recordid;
	public String data;

	@JsonIgnore
	public Promotion[] parsed;

	public void parse(ObjectMapper mapper) {
		try {
			parsed = mapper.readValue(data, Promotion[].class);
		} catch (Exception ignored) {
			parsed = new Promotion[0];
		}
	}
}
