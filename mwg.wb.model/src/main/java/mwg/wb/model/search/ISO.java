package mwg.wb.model.search;

import java.util.Date;

public class ISO {
	public String Keyword;
	public String ID; // categoryID
	public String Lang; // term
	public int SiteID;
	public String Term; // = cateid_siteid_lang //term

	public Date CreatedDate;

	public Date IndexDate;
	public String IndexSource;
	public String IndexLog;
	public String IndexVersion;

	public Date didx_updateddate;
	public String didx_source;
	public int IsDeleted;
}
