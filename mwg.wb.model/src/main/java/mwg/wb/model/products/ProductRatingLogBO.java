
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Long Trịnh 
/// Created date 	: 10/24/2014 
/// Đánh giá nội dung chi tiết sản phẩm
/// </summary>

import java.util.Date;

public class ProductRatingLogBO {

	public ProductRatingLogBO() {
	}

	/// <summary>
	/// LogID
	///
	/// </summary>
	public int LogID;

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int ProductID;

	/// <summary>
	/// IPCreator
	///
	/// </summary>
	public String IPCreator;

	/// <summary>
	/// RatingType
	/// 1: Rất tốt, 2: Tạm được, 3: Chán
	/// </summary>
	public int RatingType;

	/// <summary>
	/// CreatedDate
	///
	/// </summary>
	public Date CreatedDate;

	/// <summary>
	/// CreatedDateKey
	///
	/// </summary>
	public int CreatedDateKey;

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
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

}
