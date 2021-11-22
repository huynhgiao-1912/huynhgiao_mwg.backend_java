package mwg.wb.model.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mwg.wb.model.comment.RatingStaticBO;
import mwg.wb.model.cook.CookRecipe;

public class DishSO {
	public DishSO() {
		ListRecipe = new ArrayList<CookRecipe>();
		RatingStatic = new RatingStaticBO();
	}

	public int DishID;
	public String DishName;
	public String Title;
	public String Url;
	public String Keyword;
	public String Tag;
	public String ShortDescription;
	public String Tipsnote;
	public int IsDraft;
	public Date CreatedDate;
	public Date UpdatedDate;
	public int IsActived;
	public Date ActivedDate;
	public int IsDeleted;
	public Date DeletedDate;
	public int IsFeatured;

	public Date LastDidxUpdated;

	public String CategoryIdList;
	public String CategoryNameList;
	public String RecipeIdList;
	public String RecipeNameList;

	public String CreatedFullName;

	public int ViewCount;

	public int IsHasVideo;

	public List<CookRecipe> ListRecipe;
	public RatingStaticBO RatingStatic;

	public int IsMeoVatOnly;
}
