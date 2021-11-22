package mwg.wb.model.news;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class NewsCategoryBO {

	public int CategoryID;

	public String CategoryName;

	public String Description;

	public String HotNewsIdList;

	public String KeyWord;

	public String MetaKeyWord;

	public String MetaTitle;

	public String MetaDescription;

	public String URL;

	public int ParentID;

	public int DisplayOrder;

	public int DisplayTemplate;

	public boolean IsActived;

	public boolean IsDeleted;

	public Date DeletedDate;

	public String DeletedUser;

	public Date CreatedDate;

	public String CreatedUser;

	public String UpdatedUser;

	public Date UpdatedDate;

	public boolean IsShowHome;

	public Date ActivedDate;

	public String ActivedUser;

	public boolean IsTGDDNews;

	public String NodeTree;

	public boolean IsExist;

	public int NewsCount;

	public List<NewsCategoryBO> SubNewsCategoryBOList;

	public int TotalRecord;

	public String Images;

	public int AmountNews;

	public int AmountHotNews;
	public String ListCategoryID;
	public String ListCategoryName;
	public int TotalSub;
	public int TotalNews;
	public int TotalComment;

	public String SmallImages;

	public String MobileTitle;

	public int SiteID;
	
//	public String ListGameApp;
//	public String ListPlatFormGameApp;
}
