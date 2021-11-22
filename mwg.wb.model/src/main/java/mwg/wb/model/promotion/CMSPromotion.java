package mwg.wb.model.promotion;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CMSPromotion {
	public int ProgramID;
	public String ProgramName;
	public String Description;
	public String Detaillink;
	public String DmDetaillink;
	public Date FromDate;
	public Date ToDate;
	
	@JsonIgnore
	public int getProgramID() {
		return ProgramID;
	}
	@JsonIgnore
	public Date getToDate() {
		return ToDate;
	}
}
