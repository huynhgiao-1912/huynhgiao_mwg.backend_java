package mwg.wb.model.search;

import java.util.Date;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Utils;
import mwg.wb.model.faq.FaqBO;
import mwg.wb.model.promotion.PromotionOldProductBO;

//class FAQStatSO
//{
//    public Date Date;
//
//    public int CategoryID;
//    public String CategoryCode ;
//    public int Count;
//    public int Type ;
//    public String DateKey;
//}

public class FaqSO {
	public int NewsID;
	public int CategoryID;
	public String Listcategoryid;
	public String ListTreeCategoryID;
	public String Title;
	public String SETitle;
	public String Content;
	public String Keyword;
	public String SEKeyword;
	public String Tags;
	public String SETags;
	public String SETagKey;
	public int IsActived;
	public int IsDeleted;
	public int Viewcounter;
	public int Likecounter;
	public int Dislikecount;
	public String Productidlist;
	public String Eventidlist;
	public String Hottopicidlist;
	public int Relateproductid;
	public long Createddate;
	public long Updateddate;
	public int Displayorder;
	public int Posttype;
	public int Userid;
	public int Storeid;
	public String Metatitle;
	// public String Metadescription;
	public String Metakeyword;
	// public int Labelid;
	// public String Image;
	// public String Url;
	// public String Detailimage;
	public int IsCommentallow;
	public int IsGallery;
	public int IsVideo;
	// public String Extenallink;
	public int IsFeature;
	public String IpCreated;
	public int IsSticked;
	public String Createduser;
	public String Listcategoryname;
	public int IsDraft;
	public int Siteid;
	// public String Videolink;
	public String Topicidlist;
	public String Topicnamelist;
	public Long ActivedDate;
	// public int Activeduser;
	// public String Thumbnailimage;
	// public String Shortdescription;
	public String Amountcomment;

	public int SEIsManual;
	public int IsAdminAnswer;
	public String BestCommentID;
	public String NewestAnswerID;
	public Boolean HasImage;
	public int IsResolved;
	public String ListParentThreadID;
	public String ListChildThreadID;
	public int PostType;
	public int IsPrivate;
	public String ProductIDList;

	public long setActivedDate() {
		return this.ActivedDate;
	}

	public static FaqSO fromBO(FaqBO bo) {
		return new FaqSO() {
			{
				NewsID = bo.NewsID;
				CategoryID = bo.CategoryID;
				Listcategoryid = bo.Listcategoryid;
				ListTreeCategoryID = bo.ListTreeCategoryID;
				Title = bo.Title;
				SETitle = !Utils.StringIsEmpty(bo.Title)
						? DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(bo.Title))
						: "";
				SEKeyword = !Utils.StringIsEmpty(bo.Keyword)
						? DidxHelper.FormatKeywordField(DidxHelper.FilterVietkey(bo.Keyword))
						: "";
				Keyword = bo.Keyword;
				// Tags = bo.Tags;
				SETags = !Utils.StringIsEmpty(bo.Tags) ? bo.Tags.replace("@", " PRO") : "";
				SETagKey = !Utils.StringIsEmpty(bo.Tags) ? ConvertToTagsTerm(bo.Tags) : "";
				HasImage = !Utils.StringIsEmpty(bo.Thumbnailimage) ? false : true;

				SEIsManual = bo.IsManual != null && bo.IsManual == true ? 1 : 0;
				IsResolved = bo.IsAdminAnswer == 1 || bo.IsUserAnswer == 1 ? 1 : 0;

				IsActived = bo.IsActived;
				IsDeleted = bo.IsDeleted; // quan trọng
				Viewcounter = bo.Viewcounter;
				Likecounter = bo.Likecounter;
				Dislikecount = bo.Dislikecount;
				Productidlist = bo.Productidlist;
				Createddate = bo.Createddate != null ? bo.Createddate.getTime() : Utils.GetDefaultDate().getTime();
				Updateddate = bo.Updateddate != null ? bo.Updateddate.getTime() : Utils.GetDefaultDate().getTime();
				Displayorder = bo.Displayorder;
				Posttype = bo.Posttype;
				Userid = bo.Userid;

				Siteid = bo.Siteid;
				ActivedDate = bo.ActivedDate != null ? bo.ActivedDate.getTime() : Utils.GetDefaultDate().getTime();
				Amountcomment = bo.Amountcomment;
				IsDraft = bo.IsDraft;
				ListParentThreadID = bo.ListParentThreadID != null ? bo.ListParentThreadID : "";
				ListChildThreadID = bo.ListChildThreadID != null ? bo.ListChildThreadID : "";
				// PostType = bo.Posttype;
				IsPrivate = bo.IsPrivate;
				ProductIDList = bo.Productidlist != null ? ProductIDList : "";
			}
		};
	}

	public String ConvertToTagsTerm(String tags) {
		if (Utils.StringIsEmpty(tags))
			return "";
		String res = "";
		String[] tagsArray = tags.split(",");
		// foreach (String itemtag in tagsArray)

		for (int i = 0; i < tagsArray.length; i++) {
			String TagName = tagsArray[i].trim();

			int productID = getProductID(TagName);
			if (productID <= 0) {
				String seoname = DidxHelper.GenTerm(TagName);
				if (!Utils.StringIsEmpty(seoname))
					res = res + " " + seoname;
			} else {
				res = res + " " + "pro" + productID;
			}
		}
		res = res.trim().toLowerCase();
		return res;
	}

	private int getProductID(String item) {
		try {
			String key = "@([0-9]+)";
			// Match M = Regex.Match(item, key, RegexOptions.Compiled |
			// RegexOptions.IgnoreCase | RegexOptions.Multiline);
			Pattern r = Pattern.compile(key);
			Matcher M = r.matcher(item);

			if (M.toMatchResult() != null)
				return Integer.parseInt(M.group(0));
		} catch (Exception e) {

		}
		return -1;
	}

//    public static boolean isNullOrEmpty(String str) {
//        if(str != null && !str.isEmpty())
//            return false;
//        return true;
//    }
}


