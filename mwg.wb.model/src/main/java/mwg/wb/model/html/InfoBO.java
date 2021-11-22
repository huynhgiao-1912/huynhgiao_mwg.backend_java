package mwg.wb.model.html;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT)
public class InfoBO {
	public InfoLangBO InfoLangBO;
	public int HTMLID;
	public String GroupID;
	public int IsDeleted;
	public Date DeletedDate;
	public String DeletedUser;
	public Date CreatedDate;
	public String CreatedUser;
	public boolean IsExist;
}
