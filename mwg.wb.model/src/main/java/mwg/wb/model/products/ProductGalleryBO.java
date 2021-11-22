
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Vu Quy Khi 
/// Created date 	: 3/19/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class ProductGalleryBO {
	/// <summary>
	/// PictureID
	///
	/// </summary>
	public int PictureID;

	/// <summary>
	/// Picture
	///
	/// </summary>
	public String Picture;

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int ProductID;

	/// <summary>
	/// CategoryID
	///
	/// </summary>
	public int CategoryID;

	/// <summary>
	/// Description
	///
	/// </summary>
	public String Description;

	/// <summary>
	/// DisplayOrder
	///
	/// </summary>
	public int DisplayOrder;

	/// <summary>
	/// IsActived
	///
	/// </summary>
	public boolean IsActived;

	/// <summary>
	/// ActivedDate
	///
	/// </summary>
	public Date ActivedDate;

	/// <summary>
	/// ActivedUser
	///
	/// </summary>
	public String ActivedUser;

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
	/// DeletedUser
	///
	/// </summary>
	public String DeletedUser;

	/// <summary>
	/// CreatedDate
	///
	/// </summary>
	public Date CreatedDate;
	public long CreatedDateLongValue;

	/// <summary>
	/// CreatedUser
	///
	/// </summary>
	public String CreatedUser;

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
	/// ProductCode
	///
	/// </summary>
	public String ProductCode;

	/// <summary>
	/// ColorID
	///
	/// </summary>
	public int ColorID;

	/// <summary>
	/// ColorName
	/// </summary>
	public String ColorName;

	public String ColorIcon;
	public String ColorCode;

	/// <summary>
	/// MobilePicture
	///
	/// </summary>
	public String MobilePicture;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public String PictureLarge;

	public String PictureThumbnail;

	public String PictureOrg;

	public int ImageType;
	public String VideoUrl;

	public int LikeCount;
	public int ViewCount;
	public int PictureType;

	public int Width;
	public int WidthLarge;
	public int WidthOrg;

	public int Height;
	public int HeightLarge;
	public int HeightOrg;
}
