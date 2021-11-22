








package mwg.wb.model.products;
    /// <summary>
	/// Created by 		: Bạch Xuân Cường 
	/// Created date 	: 4/11/2012 
	/// Product
	/// </summary>	
	
	public class ProductCategoryRelateBO
	{
		/// <summary>
		/// NewsCATID
		/// Danh mục tin tức
		/// </summary>
		public int NewsCATID;

		/// <summary>
		/// ProductCATID
		/// Ngành hàng sản phẩm
		/// </summary>
		public int ProductCATID;

		/// <summary>
		/// OrderDisplay
		/// 
		/// </summary>
		public int OrderDisplay;

		/// <summary>
		/// Có tồn tại không?
		/// </summary>
		public boolean IsExist;

        public String NewsCategoryName;

        public String ProductCategoryName;
    }
