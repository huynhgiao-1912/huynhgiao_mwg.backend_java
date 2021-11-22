package mwg.wb.client.elasticsearch.dataquery;

import java.util.Date;

public class NewsQuery {

	public long UserID;
	public long CustomerID;
	public int CategoryId;
	public int HotTopicId;
	public int StoreID;
	public int RelatedNewsId;
	public String EventsIDList;
	public String ProductIDList;

	public boolean IsAlsoGetRelatedNews;
	public long ViewCounter;

	public boolean IsViewMore;
	public Date FromDate;
	public int FromHour;

	public int IsActive;
 
	public DateOrder Order;
	public Int64Order DisplayOrder;
	public Int64Order OrderByViewCounter;
	public Int64Order OrderByLikeCounter;
	public int OrderType;
	public Int64Order OrderValue;
	

	public enum DateOrder {
		NORMAL, LASTEST, OLDEST;
	}
	public enum Int64Order {
		NORMAL, LARGEST, SMALLEST;
	}

	public String Tag;
	public String Keyword;
	public int PageIndex;
	public int PageSize;
	public int MaxRecords;
	public boolean IsSearchLike;
	public int ExtensionObject;
	public int SiteID;
	
	public int NewsId;
	public int[] NewsIds;
	//ds bài tin đã được load
	public int[] LoadedNewsIds;
	public int[] ExcludeIdsCate;
	public int[] CateIds;
	public String Title; //api truyền vào, title trong query
	public String[] RelatedTags;
	public boolean IsHasYouTube;
	public DateOrder OrderByCreatedDate;
	public DateOrder OrderByUpdatedDate;
	public Int64Order OrderByCommentCounter;
		public Int64Order ScoreOrder;
 
	public boolean IsHasTagBrand;
	public boolean IsSuggestSearch;


}
