
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Trịnh Văn Long 
/// Created date 	: 28/10/2013 
/// Slider Album
/// </summary>

import java.util.Date;
import java.util.List;

public class ProductAlbumBO {

	public ProductAlbumBO() {
	}

	/// <summary>
	/// Ðôi tuong message
	/// </summary>
	/// <summary>
	/// AlbumID
	///
	/// </summary>
	public int AlbumID;

	/// <summary>
	/// AlbumName
	/// Tên Album
	/// </summary>
	public String AlbumName;

	/// <summary>
	/// Description
	/// Mô tả Album
	/// </summary>
	public String Description;

	/// <summary>
	/// DisplayOrder
	/// Thứ tự hiển thị
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
	/// ImageCount
	/// Tổng số hình có trong Album
	/// </summary>
	public int ImageCount;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public int ProductID;

	public List<ProductAlbumGalleryBO> ListProductAlbumGalleryBO;

	public String AlbumIcon;
}
