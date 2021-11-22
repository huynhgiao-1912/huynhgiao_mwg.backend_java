package mwg.wb.model.commonpackage;

import java.util.Date;

import mwg.wb.common.DidxHelper;
import mwg.wb.common.Utils;

public class KeyWordRedirectBO {
	public int KeywordID;
	
    public String UrlTGDD;
    public String UrlDMX;
    
    public String Keyword;
    public Date CreatedDate; 
    public String CreatedUser;

    public Date UpdateddDate;
    public String UpdatedUser;

    public Date DeletedDate;
    public String DeletedUser;
    public boolean IsDeleted;
    
    public static KeyWordRedirectSO toKeyWordRedirectSO(KeyWordRedirectBO data) {
    	
    	if(data == null) return null;
    	var tmp = new KeyWordRedirectSO();
    	tmp.KeywordID = data.KeywordID;
    	tmp.UrlTGDD = !Utils.StringIsEmpty(data.UrlTGDD) ? data.UrlTGDD : "";
    	tmp.UrlDMX = !Utils.StringIsEmpty(data.UrlDMX) ? data.UrlDMX : "";
    	
    	tmp.CreatedDate = data.CreatedDate != null ? data.CreatedDate : Utils.GetDefaultDate();
    	tmp.IsDeleted = data.IsDeleted;
    	tmp.type = 1;// 1 => keyword
    	tmp.SiteID = "1 2";
    	if(!Utils.StringIsEmpty(data.Keyword)) {
    		
    		tmp.Keyword = data.Keyword;
    		tmp.KeywordSE = DidxHelper.GenTermKeyWord(data.Keyword);
    	}else {
    		tmp.Keyword = "";
    		tmp.KeywordSE = "";
    	}
    	
    	return tmp;
    }
    
//    private static String GenTermKeyWord(String keyword)
//    {
//        var arrString = keyword.split(",");
//        String tmpString = "";
//        for (String item : arrString) {
//        	
//            tmpString += DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField( item.trim() )).replace(' ', '_') + " ";
//            
//        }
//
//        return tmpString.trim();
//    }
}
