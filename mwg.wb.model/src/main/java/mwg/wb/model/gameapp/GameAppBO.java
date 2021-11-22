package mwg.wb.model.gameapp;

import java.util.Date;
import java.util.List;

import org.apache.tinkerpop.shaded.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;

import mwg.wb.common.DidxHelper;
import mwg.wb.common.Utils;
import mwg.wb.model.products.ProductCategoryBO;
import mwg.wb.model.products.ProductCategoryLangBO;
import mwg.wb.model.products.ProductLanguageBO;
import mwg.wb.model.news.NewsBO;
public class GameAppBO {
	/**
	 * ProductID
	 *
	 **/
	public int ProductID;
	public String ProductCode;
	/**
	 * ParentID
	 *
	 **/
	public int ParentID;


	/**
	 * Số thứ tự Numbers
	 **/
	public int Numbers;

	/**
	 * MaxRow Numbers
	 **/
	public int MaxRow;

	/**
	 * Tỗng record 
	 **/
	public int TotalRecord;

	/**
	 * ProductName
	 *
	 **/
	public String ProductName;
	
	public int CategoryID;
	public String CategoryName;
//	
//	public ProductCategoryLangBO ProductCategoryLangBO;
//	public ProductCategoryBO ProductCategoryBO;

	public GameAppLanguageBO ProductLanguageBO;
	public ProductCategoryBO ProductCategoryBO;
	public ProductCategoryBO[] ProductCategorys;
	public NewsBO[] NewsBO;
	public PlatformGameAppBO[] PlatformBO;
	
	public int[] platformIDList;
	public String[] platformNameList;
	
	public String URL;
	
	public String featureimage;
	
	public String Bimage;
	public String Mimage;
	public boolean IsShowHome;
	public boolean IsFeature;
	public Date TimerFeatureFromDate;
	public String KeyWord;
	public String Advantage;
	public String HTMLDescription;
	public String MetaTitle;
	public String MetaDescription;
	public Date ActivedDate;
	public Date CreatedDate;
	public String Tag;
	public String DisplayOrder;
	public Date CreateDate;
	public int IsDeleted;
	public int SiteID;
	public String LanguageID;
	public int Price;
	public int IsOnlineOnly;
	public String Image;
	public String Strgallery;
	
	public int TotalReview;
	
	//@JsonProperty("totalreview")
	public int ViewCount;
	
	public int Quantity;
	
	@JsonProperty("mobile_simage")
	public String MobileSimage;
	@JsonProperty("mobile_mimage")
	public String MobileMimage;
	@JsonProperty("mobile_bimage")
	public String MobileBimage;
	
	public int IsWebShow;

	public int IsFree;
	public int IsActived;
	public int UrlDownload;
	public int Hasbimage;
	
	
	public String CreatedUser;
	
	public int CommentCount;
	
	public String FullName;
	public Date UpdatedDate;
//	public String UpdatedUser;
	
	public static GameAppSO fromBOToSO(GameAppBO gameapp) {
		GameAppSO gameso = new GameAppSO();
		gameso.ProductID = gameapp.ProductID;
		gameso.ParentID = gameapp.CategoryID;
		//gameso.CategoryID = Integer.toString(gameapp.ProductCategoryBO.CategoryID);
		String type = (gameapp.CategoryID == 8232) ? "ung dung app " : (gameapp.CategoryID == 8233) ? "tro choi game " : "";
		gameso.ProductName = type + (!Utils.StringIsEmpty(gameapp.ProductName) ? DidxHelper.FormatKeywordAZ(DidxHelper.FilterVietkey(gameapp.ProductName)).toLowerCase() : "");
		 
		gameso.Tag =  !Utils.StringIsEmpty(gameapp.KeyWord) ? DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(gameapp.KeyWord)) : "";
		gameso.IsDeleted  = gameapp.IsDeleted;
		gameso.CreateDate = gameapp.CreatedDate != null ?  gameapp.CreatedDate.getTime() : Utils.GetDefaultDate().getTime();
		gameso.ActivedDate = gameapp.ActivedDate != null ?  gameapp.ActivedDate.getTime() : Utils.GetDefaultDate().getTime();
		gameso.UpdatedDate = gameapp.UpdatedDate != null ?  gameapp.UpdatedDate.getTime() : Utils.GetDefaultDate().getTime();
		
		gameso.IsWebShow = gameapp.IsWebShow;
		gameso.IsFree = gameapp.IsFree;
		gameso.IsActived = gameapp.IsActived;
		gameso.ViewCount = gameapp.ViewCount;
		//gameso.IsFeature = gameapp.IsFeature ? 1 : 0;
		
		if(gameapp.ProductLanguageBO != null) {
			gameso.IsShowHome = gameapp.ProductLanguageBO.IsShowHome  ; 
			gameso.KeyWord = getValueValidateFormatFilter(gameapp.ProductLanguageBO.PlatformNameList) + " " + getValueValidateFormatFilter(gameapp.ProductLanguageBO.MetaDescription);
					//(!Utils.StringIsEmpty(gameapp.ProductLanguageBO.PlatformNameList) ? DidxHelper.FormatKeywordAZ(DidxHelper.FilterVietkey(gameapp.ProductLanguageBO.PlatformNameList)) : "") 
					//		+ "  " +
					//!Utils.StringIsEmpty(gameapp.ProductLanguageBO.MetaDescription) ? DidxHelper.FormatKeywordAZ(DidxHelper.FilterVietkey(gameapp.ProductLanguageBO.MetaDescription)).toLowerCase() : "";
					
			gameso.PlatformIDList = !Utils.StringIsEmpty(gameapp.ProductLanguageBO.PlatformIDList) ? String.join(" ", gameapp.ProductLanguageBO.PlatformIDList.split(",")) : ""; //;
			gameso.CategoryIDList =  !Utils.StringIsEmpty(gameapp.ProductLanguageBO.CategoryIDList) ? String.join(" ", gameapp.ProductLanguageBO.CategoryIDList.split(",")) : ""; //;
			gameso.SiteID =  (int)gameapp.ProductLanguageBO.SiteID;
			
			gameso.IsFeature = !Utils.StringIsEmpty(gameapp.ProductLanguageBO.bimage) ? 1 : 0;
			gameso.FeatureDate = gameapp.ProductLanguageBO.timerfeaturefromdate != null ? gameapp.ProductLanguageBO.timerfeaturefromdate.getTime() : 0;
			gameso.CategoryNameList = !Utils.StringIsEmpty(gameapp.ProductLanguageBO.CategoryNameList) ? getValueValidateFormatFilter(gameapp.ProductLanguageBO.CategoryNameList.replace(",", " ")) : "";
				
		}else {
			gameso.IsShowHome =   0 ; 
			gameso.KeyWord =   ""; 
			gameso.PlatformIDList =   "";  
			gameso.CategoryIDList =   ""; 
			gameso.SiteID = 1;
			gameso.IsFeature = 0;
			gameso.FeatureDate = 0;
		}
		
		return gameso;
		
	}
	
	@JsonIgnoreProperties
	public static String getValueValidateFormatFilter(String field) {
		try {
			if(!Utils.StringIsEmpty(field)) {
				return DidxHelper.FormatKeywordAZ(DidxHelper.FilterVietkey(field)).toLowerCase();
			}
			return "";
		}catch (Exception e) {
			// TODO: handle exception
			if(e.toString().equals("NullPointerException")) {
				return "";
			}			
		}
		return null;
	}
	
}


