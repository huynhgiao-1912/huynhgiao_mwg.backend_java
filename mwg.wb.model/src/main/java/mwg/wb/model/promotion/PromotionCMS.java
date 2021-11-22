
package mwg.wb.model.promotion;

import java.util.Date;

public class PromotionCMS {
	public class PromotionCMSFeatureBoxBO {
		public int FeatureBoxId;
		public int PromoProgramId;
		public int FeatureBoxType;
		public int ProductId;
		public boolean IsShowWeb;
		public Date EndDate;
		public String BannerImage;
		public String BannerUrl;
		public String BackgroundColorId;
		public String TitleColorId;
		public String DesktopHeaderBanner;
		public String MobileHeaderBanner;
		public boolean HeaderBannerIsShowWeb;
		public String BottomHTML;
		public String BottomHTML2;
		public boolean BottomHTMLIsShowWeb;
	}

	public class PromotionCMSFeatureProductBO {
		public int FeatureProductId;
		public int PromoProgramId;
		public int ProductId;
		public String ProductImage;
		public int IsShowWeb;
		public int DisplayOrder;
		public String ProductName;
		public String BackgroundColorId;
		public String TitleColorId;
		public String DesktopHeaderBanner;
		public String MobileHeaderBanner;
		public boolean HeaderBannerIsShowWeb;
		public String BottomHTML;
		public boolean BottomHTMLIsShowWeb;
	}

	public class PromotionCMSProductGroupBO {
		public int PromoGroupId;
		public int ParentGroupId;
		public String GroupTitle;
		public String GroupListProductId;
		public String AnchorText;
		public String TextLink;
		public String UrlTextLink;
		public int ProductShowForm;
		public int DisplayOrder;
		public int PriceOrder;
		public int PromoProgramId;
		public String Icon;
		public String LabelImage;
		public String LabelText;
		public Date LabelDate;
		public int PromotionType;
		public String PromotionText;
		public Date PromotionDate;
		public boolean IsShowInventory;
		public boolean IsShowPromotion;
	}

	
}
