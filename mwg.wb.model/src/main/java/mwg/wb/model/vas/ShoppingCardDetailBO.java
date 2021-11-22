






package mwg.wb.model.vas;
    /// <summary>
	/// Created by 		: Bạch Xuân Cường 
	/// Created date 	: 2/28/2013 
	/// Tên tiếng Việt
	/// </summary>	
	
	public class ShoppingCardDetailBO
	{
		public ShoppingCardDetailBO()
		{
		}

		/// <summary>
		/// SHOPPINGCardID
		/// Mã giỏ hàng
		/// </summary>
		public String ShoppingCardID ;

		/// <summary>
		/// ProductID
		/// Mã sản phẩm software
		/// </summary>
		public int ProductID ;

		/// <summary>
		/// Price
		/// Giá của từng software (nếu là sản phẩm tính phí >0) 
		/// </summary>
		public double Price ;

		/// <summary>
		/// Quantity
		/// Số lượng từng sản phẩm có trong giỏ hàng
		/// </summary>
		public int Quantity ;

		/// <summary>
		/// DownloadTimeS
		/// Số lần download
		/// </summary>
		public int DownloadTimes ;

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


        public String ProductName ;

        public int CategoryID ;

        public String CategoryName ;

        public String FileSize ;

        public String Url ;

        public String FileName ;

        public String Path ;
    }
