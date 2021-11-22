
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Nguyễn Viết Hưng 
/// Created date 	: 27/9/2017 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;

public class PromotionImageBO {

	public PromotionImageBO() {
	}

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int PromotionID;
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
	/// LastUpdate
	///
	/// </summary>
	public Date UpdatedDate;

	/// <summary>
	/// UserUpdate
	///
	/// </summary>
	public String UpdatedUser;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsDelete;

	public Date DeletedDate;

	/// <summary>
	/// UserUpdate
	///
	/// </summary>
	public String DeletedUser;
	public String PromotionImageName;
	public String ThumbnailImage;
	public String DetailImage;
	public String ProductID;
	public String ProductCode;
	public String URL;

	public class PromotionImageInfoBO {

		public PromotionImageInfoBO() {
		}

		public int PromotionID;
		public String PromotionName;
		public String CreatedUser;
		public String UpdatedUser;

	}
}
