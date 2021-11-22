package mwg.wb.client.elasticsearch.dataquery;

import java.util.Date;
import java.util.List;

public class NewsEventQuery {
	public String keyword;
	public Date fromDate;
	public Date toDate;
	public int isFollow;

	public int parentEventID;
	public int orderType;
	public Int64Order orderValue;

	public enum Int64Order {
		NORMAL, LARGEST, SMALLEST;
	}

	public int pageIndex;
	public int pageSize;
	public int siteID;
	
	public List<String> lstEventID;

}
