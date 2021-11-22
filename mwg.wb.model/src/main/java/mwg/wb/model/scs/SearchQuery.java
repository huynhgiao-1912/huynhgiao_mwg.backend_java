package mwg.wb.model.scs;

public class SearchQuery {
	
	public String[] Ids;
	public String Keyword;
	public String[] Hashtags;
	public String[] Mentions;
	public int OrderType;
	public int ExtensionObject;
	public OrderType OrderValue;
	public int Type;
	public int ObjectId;

	public int PageIndex;
	public int PageSize;
	public enum OrderType {
		NORMAL, LARGEST, SMALLEST
	}
}
