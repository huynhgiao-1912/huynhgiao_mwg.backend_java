






package mwg.wb.model.vas;
    /// <summary>
	/// Created by 		: Bạch Xuân Cường 
	/// Created date 	: 2/28/2013 
	/// Tên tiếng Việt
	/// </summary>	
	
	public class CustomerGroupProductBO
	{
		public CustomerGroupProductBO()
		{
		}

		/// <summary>
		/// CustomerGroupID
		/// Mã nhóm khách hàng
		/// </summary>
		public int CustomerGroupID ;

		/// <summary>
		/// ProductID
		/// Mã sản phẩm Software (ProductID trong Product)
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
