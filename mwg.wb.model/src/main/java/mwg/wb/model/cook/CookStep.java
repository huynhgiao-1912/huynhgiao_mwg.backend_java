package mwg.wb.model.cook;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CookStep {
	public CookStep() {
		ListGallery = new ArrayList<CookGallery>();
	}

	public int StepId;
	public int RecipeId;
	public String StepName;
	public String Directions;
	public String TipsNote;
	public int DisplayOrder;
	public Date CreatedDate;
	public String CreatedUser;
	public Date UpdatedDate;
	public String UpdatedUser;
	public int IsDeleted;
	public Date DeletedDate;
	public String DeletedUser;

	public List<CookGallery> ListGallery;

}
