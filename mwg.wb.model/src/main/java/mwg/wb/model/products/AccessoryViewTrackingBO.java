






package mwg.wb.model.products;
    /// <summary>
	/// Created by 		: Bạch Xuân Cường 
	/// Created date 	: 04/15/13 
	/// Tên tiếng Việt
	/// </summary>	

import java.util.Date;

public class AccessoryViewTrackingBO
	{
		public AccessoryViewTrackingBO()
		{
		}

		/// <summary>
		/// TrackingID
		/// 
		/// </summary>
		public int TrackingID;

		/// <summary>
		/// ProductID
		/// Mã sản phẩm
		/// </summary>
		public int ProductID;

		/// <summary>
		/// ProductACID
		/// Mã sản phẩm phụ kiện
		/// </summary>
		public int ProductACID;

		/// <summary>
		/// FromDate
		/// Từ ngày
		/// </summary>
		public Date FromDate;

		/// <summary>
		/// ToDate
		/// Đến ngày
		/// </summary>
		public Date ToDate;

		public String strCreatedUser = "";

		/// <summary>
		/// CreatedUser
		/// 
		/// </summary>
		

		/// <summary>
		/// CreatedDate
		/// SYSDATE
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


        public String ProductACName;
    }
