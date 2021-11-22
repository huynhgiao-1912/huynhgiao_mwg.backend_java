








package mwg.wb.model.products;
    /// <summary>
	/// Created by 		: Bạch Xuân Cường 
	/// Created date 	: 4/11/2012 
	/// Product
	/// </summary>	
	
	public class ProductPriceRangeMapBO
	{
		
		/// <summary>
		/// RangeID
		/// Miền giá của sản phẩm gốc
		/// </summary>
		public int RangeID;

		/// <summary>
		/// CategoryID
		/// Danh mục của sản phẩm liên quan
		/// </summary>
		public int CategoryID;

		/// <summary>
		/// PriceFrom
		/// Khoảng giá từ
		/// </summary>
		public int PriceFrom;

		/// <summary>
		/// PriceTo
		/// Khoảng giá đến
		/// </summary>
		public int PriceTo;

		/// <summary>
		/// Có tồn tại không?
		/// </summary>
		public boolean IsExist;


				
	}
