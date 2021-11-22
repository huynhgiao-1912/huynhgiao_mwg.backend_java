






package mwg.wb.model.vas;
    /// <summary>
    /// Created by 		: Nguyễn Viết Hưng
    /// Created date 	: 12/5/2016
    /// Music
    /// </summary>	
    
    public class MusicAuthorBO
    {
        public MusicAuthorBO()
		{
		}

		/// <summary>
		/// MusicID
		/// 
		/// </summary>
		public int MusicID ;

		/// <summary>
		/// AuthorID
		/// 
		/// </summary>
		public int AuthorID ;

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
