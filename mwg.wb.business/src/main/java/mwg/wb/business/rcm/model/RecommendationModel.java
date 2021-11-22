package mwg.wb.business.rcm.model;

 

//import lombok.*;
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@ToString
 
public class RecommendationModel {
 	public String item;
    public Double score;
    public Double getScore() {
		return score;
	}
    public String getData() {
		return data;
	}
	private String data;
}
