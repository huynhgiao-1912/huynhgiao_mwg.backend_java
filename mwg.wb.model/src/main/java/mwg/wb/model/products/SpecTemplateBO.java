
package mwg.wb.model.products;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT)
public class SpecTemplateBO {

	public int TemplateID;

	public String TemplateName;

	public String TemplateAlias;

	public int CategoryID;

	public String Content;

	public String CreatedUser;

	public Date CreatedDate;

	public String UpdatedUser;

	public Date UpdatedDate;

	public boolean IsDeleted;

	public String DeletedUser;

	public Date DeletedDate;

	public boolean IsActived;

	public Date ActivedDate;

	public String ActivedUser;

	public boolean IsExist;

	public boolean IsSelected;

	public boolean IsEdited;

	public String LanguageID;

}
