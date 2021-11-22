








package mwg.wb.model.products;
    /// <summary>
	/// Created by 		: Bạch Xuân Cường 
	/// Created date 	: 4/11/2012 
	/// Product
	/// </summary>	
	
	public class ProductPropValueMapBO
	{
		/// <summary>
		/// ValueID
		/// Mã giá trị sản phẩm gốc được liên kết
		/// </summary>
		public int ValueID;

		/// <summary>
		/// MapValueID
		/// Mã giá trị liên kết tới
		/// </summary>
		public int MapValueID;

		/// <summary>
		/// OPerAToR
		/// 
		/// </summary>
		public int OPerator;

		/// <summary>
		/// Có tồn tại không?
		/// </summary>
		public boolean IsExist;




        public String MapValue;

        public String Value;

        public int PropertyID;

        public int GroupID;

        public String PropertyName;

        public String GroupName;

        public int CategoryID;

        public String CategoryName;
    }
