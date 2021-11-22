package mwg.wb.model.gameapp;

import java.util.Date;
import java.util.List;

import mwg.wb.model.news.GameUserBO;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.news.NewsCategoryBO;
import mwg.wb.model.news.NewsEventBO;

public class gameAppNewsBO {
	public int NewsID;

	public String Title;

	public String ShortDescription;

	public String Description;

	public String Content;

	public String KeyWord;

	public String ThumbnailImage;

	public String DetailImage;

	public String MetaKeyWord;

	public String MetaTitle;

	public String MetaDescription;

	public String URL;

	public int DisplayOrder;

	public String CategoryName;
	public String CategoryUrl;
	public String ForumLink;
	public String CategoryNodeTree;

	public int IsActived;

	public int IsDeleted;

	public Date DeletedDate;

	public String DeletedUser;

	public Date CreatedDate;

	public String CreatedUser;

	public int ViewCounter;
	public boolean IsCommentAllow;

	public Date ActivedDate;

	public Date ValidFrom;

	public Date ValidTo;

	public int CategoryID;

	public int ProductId;

	public int Rownum;

	public boolean IsImage;

	public boolean IsVideo;

	public boolean IsGallery;

	public String Source;

	public String UpdatedUser;

	public Date UpdatedDate;

	public String ActivedUser;

	public int RelateProductID;

	public boolean IsExist;

	public String MobileImage;

	public int TotalRows;

	public boolean IsHot;

	public String MobileContent;

	public String EXTENAllink;

	public String ThumbnailImageMEDIUM;

	public String ThumbnailMobile;

	public String CoverImage;

	public boolean IsFeature;

	public int LabelID;

	public boolean IsSticked;

	public boolean IsFollowEmail;

	public int LikeCounter;

	public String IPCreated;
	public String IPUpdated;

	// public LabelBO LabelBO;
	public String Fullname;
	public String Email;
	public int Isadmin;
	public String Avatarimage;

	public int HotTopicID;
	public String ListCategoryName;
	public String ListCategoryID;
	public String ListTreeCategoryID;

	public String StatusNews;

	public int AmountAttachedFile;

	public int TotalFileSize;

	public boolean IsNewTab;

	public int IsRate;

	public int IsAllowRate;

	public boolean IsDraft;

	public String HotTopicName;

	public int AmountComment;

	public String SmallThumbnailImage;

	public String LastCommentUser;

	public String ListCustomerFullname;

	public int StatusID;

	public String CreatedEmail;

	public long CustomerID;

	public String CreatedCustomerID;

	public String UpdatedCustomerID;
	public String DeletedCustomerID;
	public String ActivedCustomerID;
	public String Tags;
	public int IsAdminAnswer;
	public int IsUserAnswer;
	public int ReportCount;
	public int ReportedCustomerID;
	public boolean IsReported;

	public String VoteID;
	public int IsHighLighted;
	public Date HighLightExprire;

	public String BestCommentID;
	public String NewestAnswerID;
	public int IsWasHighLighted;
	public String ProductIDList;
	public String PlatformIDList;
	public String PlatformNameList;
	public String ProvinceIDList;
	public int SiteID;
	public String CREATEDFULLNAME;
	public String CREATEDUSEREMAIL;
	public boolean IsAdvisory;
	public boolean IsManual;
	public int StoreID;
	public int PostType;
	public String LabelName;
	public String VideoLink;
	public String VideoDuration;
	public String LabelColor;
	public int IsTechNews;
	public int IsResolved;
	public boolean HasImage;
	public String SETagsText;
	public String SETagKey;
	public String SETitle;
	public String SETags;
	public String SEKeyword;
	public boolean IsBookMark;
	public int SEIsManual;
	public String GroupTags;
	public String DeviceToken;
	public int Device;
	public String ListRelateNewsID;
	public int IsPrivate;

	public boolean IsLiveBlog;

	public String ShortTitle;

	public int ManuID;

	public int ProductCategoryID;

	public int IsGame;

	public String PublicFullname;

	public String PublicPhone;

	public String PublicEmail;

	public String PublicFacebook;

	public int PublicScore;

	public int PublicEditorScore;

	public boolean IsReviewed;

	public String PublicEditorName;

	public String PublicReason;

	public String ListParentThreadID;

	public String ListChildThreadID;

	public String FacebookPostUrl;

	public int IsShowComment;

	public int PreNewsId;
	public int NextNewsId;

	public int DMCategoryID;
	public String DMCategoryName;
	public String Labelcolor;
	public String EventIDList;

	public int UserID;
	public String ListProductCategoryID;

	public GameUserBO GameUser;
	// cong-dong
	public int Stt;
	public int Totalrecord;
	public int ShareCounter;
	public boolean IsEditedCTV;
	public int DisLikeCount;
	public String TopicIDList;
	public String TopicNameList;
	public String HotTopicIDList;
	public String HotTopicNameList;
	public String ReviewedUser;
	public Date ReviewedDate;
	public int TotalCmt;
	public Date OpenPromotionFromDate;
	public Date OpenPromotionToDate;

	public int StatusId;
	public String NewsCategoryRelated;

	public String NewsRelatedNewsId;

	public String NewsHotTopicRelated;

	public List<NewsCategoryBO> ListCategories;
	public List<NewsEventBO> EventsList;
	public List<NewsBO> ListRelatedNews;
	
}
