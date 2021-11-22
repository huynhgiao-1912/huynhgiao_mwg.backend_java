package mwg.wb.model.searchresult;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FaceCategorySR {
	public int categoryID;
	public String categoryName;
	public int productCount;
	public int productCountNoStock;
	public int score;
	public boolean hasProductInStock;

	public int getScore() {
		return (hasProductInStock ? 1000 : 0) + score;
	}

	public int getSort(){
		return productCount;
	}
	@JsonIgnore
	public int getID() {
		return categoryID;
	}
}
