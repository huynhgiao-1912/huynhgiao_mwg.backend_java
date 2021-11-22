








package mwg.wb.model.pm;
    /// <summary>
	/// Created by 		: Bạch Xuân Cường 
	/// Created date 	: 4/7/2012 
	/// Tên tiếng Việt
	/// </summary>	
    
    public class PriceSearchBO
	{
	
		public PriceSearchBO()
		{
		}
	 
		/// <summary>
		/// PriceSearchID
		/// 
		/// </summary>
		public int PriceSearchID ;

		/// <summary>
		/// CategoryID
		/// 
		/// </summary>
		public int CategoryID ;

		/// <summary>
		/// Type
		/// =0: FROM; =1: TO
		/// </summary>
		public boolean Type ;

        /// <summary>
        /// PriceText
		/// 
		/// </summary>
		public String PriceText ;

		/// <summary>
		/// Price
		/// 
		/// </summary>
		public double Price ;

		/// <summary>
		/// IsMax
		/// =1 GIỚI HẠN TÌM CAO NHẤT
		/// </summary>
		public boolean IsMax ;

		/// <summary>
		/// IsClassified
		/// Có phai tin rao vặt hay ko?
		/// </summary>
		public boolean IsClassified ;

		/// <summary>
		/// Có tồn tại không?
		/// </summary>
		public boolean IsExist ;


				
	}
