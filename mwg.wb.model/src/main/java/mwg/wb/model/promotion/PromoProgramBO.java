
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Long Trịnh 
/// Created date 	: 11/18/2014 
/// Quản lý giao diện khuyến mãi
/// </summary>

import java.util.Date;
import java.util.List;

public class PromoProgramBO {

	public PromoProgramBO() {
	}

	/// <summary>
	/// PromoProgramID
	///
	/// </summary>
	public int PromoProgramID;

	/// <summary>
	/// BackgroundColorID
	///
	/// </summary>
	public String BackgroundColorID;

	/// <summary>
	/// TitleColorID
	///
	/// </summary>
	public String TitleColorID;

	/// <summary>
	/// DesktopHeaderBanner
	///
	/// </summary>
	public String DesktopHeaderBanner;

	/// <summary>
	/// MobileHeaderBanner
	///
	/// </summary>
	public String MobileHeaderBanner;

	/// <summary>
	/// HeaderBannerIsShowWeb
	///
	/// </summary>
	public boolean HeaderBannerIsShowWeb;

	/// <summary>
	/// BottomHTML
	///
	/// </summary>
	public String BottomHTML;

	/// <summary>
	/// BottomHTMLIsShowWeb
	///
	/// </summary>
	public boolean BottomHTMLIsShowWeb;

	/// <summary>
	/// CreatedDate
	///
	/// </summary>
	public Date CreatedDate;

	/// <summary>
	/// CreatedUser
	///
	/// </summary>
	public String CreatedUser;

	/// <summary>
	/// IsDeleted
	///
	/// </summary>
	public boolean IsDeleted;

	/// <summary>
	/// DeletedDate
	///
	/// </summary>
	public Date DeletedDate;

	/// <summary>
	/// UpdatedDate
	///
	/// </summary>
	public Date UpdatedDate;

	/// <summary>
	/// UpdatedUser
	///
	/// </summary>
	public String UpdatedUser;

	/// <summary>
	/// DeletedUser
	///
	/// </summary>
	public String DeletedUser;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public List<PromofeatureBoxBO> PromofeatureBoxLst;

	public List<PromofeatureProductBO> PromofeatureProductLst;

	public String BottomHTML2;

	public boolean IsShowWeb;

	public int DisPlayOrder;

	public String PromoProgramName;

	public String URL;

	public int BannerIDDesktop;

	public int BannerIDMobile;
}
