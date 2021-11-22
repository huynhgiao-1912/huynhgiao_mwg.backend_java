






package mwg.wb.model.vas;
    /// <summary>
	/// Created by 		: Bạch Xuân Cường 
	/// Created date 	: 03/14/13 
	/// Tên tiếng Việt
	/// </summary>	
	
	public class SoftWareTopBO
	{
		public SoftWareTopBO()
		{
		}

		/// <summary>
		/// ProductID
		/// Mã sản phẩm soft
		/// </summary>
		public int ProductID ;

		/// <summary>
		/// ToPID
		/// Loại top
		/// </summary>
		public int ToPID ;

		/// <summary>
		/// DisplayOrder
		/// Thứ tự hiển thị
		/// </summary>
		public int DisplayOrder ;

		/// <summary>
		/// Có tồn tại không?
		/// </summary>
		public boolean IsExist ;

		/// <summary>
		/// Có đang chọn không?
		/// </summary>
		public boolean IsSelected ;

		/// <summary>
		/// Có chỉnh sữa không?
		/// </summary>
		public boolean IsEdited ;

	}
