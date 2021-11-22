






package mwg.wb.model.products;
    /// <summary>
	/// Created by 		: Phong Nguyễn 
	/// Created date 	: 5/9/2013 
	/// Tên tiếng Việt
	/// </summary>	
	
	public class ProductRelateTypeBO
	{
		public ProductRelateTypeBO()
		{
		}

		/// <summary>
		/// ProductID
		/// 
		/// </summary>
		public int ProductID;

		/// <summary>
		/// ProductIDRelate
		/// 
		/// </summary>
		public int ProductIDRelate;

		/// <summary>
		/// TypeID
		/// 
		/// </summary>
		public int TypeID;

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

	}
