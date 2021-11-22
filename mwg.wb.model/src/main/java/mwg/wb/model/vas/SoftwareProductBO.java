






package mwg.wb.model.vas;
    /// <summary>
	/// Created by 		: Bạch Xuân Cường 
	/// Created date 	: 2/28/2013 
	/// Tên tiếng Việt
	/// </summary>	
	
	public class SoftwareProductBO
	{
		public SoftwareProductBO()
		{
		}

		/// <summary>
		/// ProductIDSOfT
		/// Mã sản phẩm software
		/// </summary>
		public int ProductIDSoft ;

		/// <summary>
		/// ProductID
		/// Mã sản phẩm Thế giới di động
		/// </summary>
		public int ProductID ;

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
