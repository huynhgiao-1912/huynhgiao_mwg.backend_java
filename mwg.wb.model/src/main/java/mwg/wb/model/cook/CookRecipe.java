package mwg.wb.model.cook;

import java.util.Date;
import java.util.List;

public class CookRecipe {
	public int Recipeid;
	public String AvatarImage;
	public String RecipeName;
	public String VideoUrl;
	public int DifficultLevel;
	public String Serves;
	public String PreparationTime;
	public String CookingTime;
	public String MetaTitle;
	public String Keyword;
	public String MetaDescription;
	public String UtensilandNotes;
	public String ContentSRH;
	public Date CreatedDate;
	public String CreatedUser;
	public Date UpdatedDate;
	public String UpdatedUser;
	public int IsDeleted;
	public Date DeletedDate;
	public String DeletedUser;
	public int IsDraft;
	public String CategoryIDlist;
	public String CategoryNamelist;
	public int DishCount;
	public int IsActived;
	public Date ActivedDate;
	public String ActivedUser;
	public int ViewCount;
	public List<CookStep> ListStep;
	public List<CookIngredient> ListIngredient;
	public List<Integer> DishIdList;
	

}
