package mwg.wb.model.search;

import java.util.Date;
import java.util.List;

import org.apache.tinkerpop.shaded.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;

import mwg.wb.model.news.GameUserBO;
import mwg.wb.model.news.ListViewCount;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.news.NewsEventBO;

public class NewsSO {
	public int NewsID;
	public String RelatedCategory;
	public String ListTreeCategory;
	public String Keyword;
	public String KeywordTerm;
	public String TagTerm;
	public String KeywordTermVN;
	public int IsActived;
	public int IsDeleted;
	public int ViewCounter;
	public String RelatedNews;
	public String RelatedHotTopic;
	public String RelateProductID;
	public Date CreatedDate;
	public Date UpdatedDate;
	public int DisplayOrder;
	public Date ActivedDate;
	public String SeTags;
	public long CustomerID;
	public long UserID;
	public int CommentCount;
	public int PostType;
	public int LikeCounter;
	public int DisLikeCount;
	public int StoreID;
	public long ActionScore;
	public String TermEventsID;
	public String TermProductID;
	public String TermTopicID;
	public String SETIitle;
	public String SETIitleVN;
	public String SETag;
	public String SEContent;
	public int SiteID;
	public int IsGame;
	
	public String EventIDList;
	public String HotTopicIDList;
	public String CreatedCustomerID;
	public String CreatedUser;
	
//	public String ListGameApp;
//	public String ListPlatFormGameApp;

	public String ProductIDList;
	public int CountProductIDList;
	public String PlatformIDList;
	public int ViewCounter7Days;
	
	//init es for perfomance
	public List<NewsEventBO> EventsList;
	public List<NewsBO> ListRelatedNewsByTopic;
	public GameUserBO GameUser;
	
	public String SETagsFromPicture;
	
	public String ProductCategoryIDList;
	
//	@JsonIgnoreProperties
	public ListViewCount lstView7Day;
	
	public Date lastUpdateViewCount;
	public int lastViewCount;
	
	public int IsNewsVideo;
	public int IsFeature;
	@JsonIgnore
	@JsonIgnoreProperties(ignoreUnknown = true)
	public int getNewsID() {
		return NewsID;
	}
	
}
