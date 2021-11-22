package mwg.wb.model.search;

public class FaqCategorySO
{

    public int CategoryID ;

    public String CategoryCode ; //P23131, F32434

    public String CategoryName ;

    public String Description ;

    public int Type ; //F=1, P=2

    public int NumTypeMV ;
    public int NumTypeCH ;
    public int NumTypeHD ;
    public int NumTypeAll ;
    public String KeyWord ;

    public String Term ;

    public int ProductID ;

    public int ParentID ;

    public int DisplayOrder ;

    public boolean IsActived ;

    public boolean IsDeleted ;

    public boolean IsShowHome ;

    public String NodeTree ;
    public int TotalFaq ;
    public int SiteID ;
}
