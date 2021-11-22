package mwg.wb.model.products;

import java.util.Date;
import java.util.List;

public class GroupCategoryBO {

	public int GroupID;
    public int SiteID;
    public int ParentCateID;
    public String ParentGroupName;
    public String GroupName;
    public Boolean IsActived;
    public Date ActivedDate;
    public String ActivedUser;
    public Date CreatedDate;
    public String CreatedUser;
    public Boolean IsDeleted;
    public Date DeletedDate; // datetime | long => timstamp
    public String DeletedUser;
    public int DisplayOrder;
    public Date UpdateDate; // datetime | long => timstamp
    public String UpdateUser;

    public String LangID;

    public String DesktopImage;
    public String MobileImage;

    public List<ProductCategoryBO> ListCategory;
	public List<RelationShipGroupCategoryBO> RelationShipGroupCategoryBO;
}
