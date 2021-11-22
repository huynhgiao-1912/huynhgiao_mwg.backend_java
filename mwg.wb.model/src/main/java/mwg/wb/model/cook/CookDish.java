package mwg.wb.model.cook;

import java.util.Date;
import java.util.List;

import mwg.wb.model.comment.RatingStaticBO;

public class CookDish {
	public int DishID;
	public String DishName;
	public String AvatarImage;
	public String VideoUrl;
	public String Title;
	public String Url;
	public String Keyword;
	public String Tag;
	public String ShortDescription;
	public String Tipsnote;
	public String MetaTitle;
	public String MetaDescription;
	public int IsDraft;
	public String ContentSRH;
	public Date CreatedDate;
	public String CreatedUser;
	public Date UpdatedDate;
	public String UpdatedUser;
	public int IsActived;
	public String ActivedUser;
	public Date ActivedDate;
	public int IsDeleted;
	public Date DeletedDate;
	public String DeletedUser;
	public int IsFeatured;

	public String ShortName;
	public String CategoryIdList;
	public String CategoryNameList;
	public String RecipeIdList;
	public String RecipeNameList;
	/// <summary>
	/// Tên d?y d? c?a ngu?i vi?t bài
	/// </summary>
	public String CreatedFullName;
	/// <summary>
	/// Lu?t xem món an
	/// </summary>
	public int ViewCount;
	public String ThumbImage;
	public List<CookRecipe> ListRecipe;
	public RatingStaticBO RatingStatic;
	
	public int TotalComment;
	public int TotalRatingComment;

	public int IsCanYouDoIt;
	
	
	
}
