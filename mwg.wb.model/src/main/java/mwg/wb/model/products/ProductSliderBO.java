
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Trịnh Văn Long 
/// Created date 	: 28/10/2013 
/// Slider Album
/// </summary>

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT)
public class ProductSliderBO {
	/// <summary>
	/// Ðôi tuong message
	/// </summary>
	/// <summary>
	/// SliderID
	///
	/// </summary>
	public int SliderID;

	/// <summary>
	/// SliderName
	/// Tên Slide
	/// </summary>
	public String SliderName;

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int ProductID;

	/// <summary>
	/// SliderImage
	/// Hình ảnh slide
	/// </summary>
	public String SliderImage;

	/// <summary>
	/// EffectID
	/// Mã hiệu ứng: 1: Hiệu ứng hình bên phải, chữ bên trái, 2:Hiệu ứng hình trái,
	/// chữ bên phải, 3: Hiệu ứng hình Full
	/// </summary>
	public int EffectID;

	/// <summary>
	/// Description
	/// Mô tả Slider
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
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public int TotalSlide;

	public int TotalAlbum;

	public int TotalVideo;

	public String ListVideoID;

	public String ListAlbumID;

	public String VideoIcon;

	public String AlbumIcon;

	public String MobileImage;
	public String URL;
	public Date Fromdate;
	public Date Todate;
	public String DMXURL;
	public String videoPath;

}
