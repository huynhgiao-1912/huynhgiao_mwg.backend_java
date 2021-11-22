








package mwg.wb.model.products;
    /// <summary>
	/// Created by 		: Vu Quy Khi 
	/// Created date 	: 4/17/2012 
	/// PRODUCT_COMMENT_USERVOTE
	/// </summary>	
	
	public class ProductCommentUserVoteBO
	{
		/// <summary>
		/// CommentID
		/// Mã comment
		/// </summary>
		public int CommentID;

		/// <summary>
		/// UserVote
		/// Người Vote cho comment nào đó
		/// </summary>
		public int UserVote;

		/// <summary>
		/// Có tồn tại không?
		/// </summary>
		public boolean IsExist;
	}
