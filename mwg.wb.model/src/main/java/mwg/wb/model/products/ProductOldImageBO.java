
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Nguyen Duc Hieu 
/// Created date 	: 10/18/2013 
/// Nguyen Duc Hieu
/// </summary>	

import java.util.Date;

public class ProductOldImageBO {
	public ProductOldImageBO() {

	}

	/// <summary>
	/// ImageID
	///
	/// </summary>
	public int ImageID;

	/// <summary>
	/// OldID
	/// Mã máy Model(Imei) máy cũ
	/// </summary>
	public int OldID;

	public String strImageDESKToP = "";// Hình hiển thị trên Desktop

	/// <summary>
	/// ImageDESKToP
	/// Hình hiển thị trên Desktop
	/// </summary>

	public String strImageMobile = "";// Hình trên Mobile

	/// <summary>
	/// ImageMobile
	/// Hình trên Mobile
	/// </summary>

	public String strIMAGetHUMNAIL = "";// Hình Thumnail

	/// <summary>
	/// IMAGetHUMNAIL
	/// Hình Thumnail
	/// </summary>

	public String strCaption = "";// Chú thích trên hình

	/// <summary>
	/// Caption
	/// Chú thích trên hình
	/// </summary>

	public String strCreatedUser = "";

	/// <summary>
	/// CreatedUser
	///
	/// </summary>

	/// <summary>
	/// CreatedDate
	/// Ngày tạo hình
	/// </summary>
	public Date CreatedDate;

	public String strUpdatedUser = "";

	/// <summary>
	/// UpdatedUser
	///
	/// </summary>

	/// <summary>
	/// UpdatedDate
	///
	/// </summary>
	public Date UpdatedDate;

	/// <summary>
	/// IsDeleted
	///
	/// </summary>
	public boolean IsDeleted;

	public String strDeletedUser = "";

	/// <summary>
	/// DeletedUser
	///
	/// </summary>

	/// <summary>
	/// DeletedDate
	///
	/// </summary>
	public Date DeletedDate;

	/// <summary>
	/// ActivedDate
	///
	/// </summary>
	public Date ActivedDate;

	/// <summary>
	/// IsActived
	///
	/// </summary>
	public boolean IsActived;

	public String strActivedUser = "";

	/// <summary>
	/// ActivedUser
	///
	/// </summary>

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	/// <summary>
	/// Có đang chọn không?
	/// </summary>
	public boolean IsSelected;

	/// <summary>
	/// Có chỉnh sữa không?
	/// </summary>
	public boolean IsEdited;

}
