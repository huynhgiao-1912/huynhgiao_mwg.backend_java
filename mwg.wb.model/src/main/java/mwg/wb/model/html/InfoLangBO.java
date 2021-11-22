package mwg.wb.model.html;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT)
public class InfoLangBO {
	public int HTMLID;
	public String LanguageID;
	public String Title;
	public String Content;
	public String URL;
	public String Description;
	public String KeyWord;
	public String MetaKeyWord;
	public String MetaDescription;
	public String MetaTitle;
	public int SiteID;
	public boolean IsExist;
}
