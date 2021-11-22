
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 11/14/2012 
/// Đếm lượt xem sản phẩm
/// </summary>	

import java.util.Date;

public class ProductViewTrackingBO {

	public ProductViewTrackingBO() {
	}

	/// <summary>
	/// TrackingID
	///
	/// </summary>
	public int TrackingID;

	/// <summary>
	/// ProductID
	/// Mã sản phẩm tham chiếu
	/// </summary>
	public int ProductID;

	/// <summary>
	/// ViewCount
	/// Lượt view trong ngày
	/// </summary>
	public int ViewCount;

	/// <summary>
	/// TrackingDate
	/// Ngày tracking
	/// </summary>
	public Date TrackingDate;

	/// <summary>
	/// DOPING
	///
	/// </summary>
	public int DOPING;

	/// <summary>
	/// DateModified
	///
	/// </summary>
	public Date DateModified;

	/// <summary>
	/// UserModified
	///
	/// </summary>
	public String UserModified;

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

	public int TotalRecord;

	public int CategoryID;

	public String ProductName;

	public boolean IsUpdateViewTracking;

}
