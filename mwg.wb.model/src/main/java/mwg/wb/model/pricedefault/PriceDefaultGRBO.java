package mwg.wb.model.pricedefault;

import java.util.Collection;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;

public class PriceDefaultGRBO {
	public String recordID;
	public long productID;
	public String langID;
	public String data;
	public int siteID;
	public Date DidxUpdatedDate;

	private static ObjectMapper mapper;

	public static PriceDefaultGRBO fromListDefaultBO(long productID, int siteID, String langID,
			Collection<PriceDefaultBO> list) throws JsonProcessingException {
		if (mapper == null) {
			mapper = DidxHelper.generateJsonMapper(GConfig.DateFormatString);
		}
		var p = new PriceDefaultGRBO();
		p.siteID = siteID;
		p.langID = langID;
		p.data = mapper.writeValueAsString(list);
		p.recordID = productID + "_" + siteID + "_" + langID;
		p.productID = productID;
		p.DidxUpdatedDate = new Date();
		return p;
	}
}
