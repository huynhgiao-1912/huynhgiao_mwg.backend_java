package mwg.wb.model.sim;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class NetworkBO {
	public int SIMNetworkID;
	public String SIMNetworkName;
	public String Logo;
	public String URLTarget;
	@JsonInclude(Include.NON_NULL)
	public int ProductCount;
	public int DisplayOrder;
	public boolean IsDeleted;
	public Date DeletedDate;
	public String DeletedUser;
	public Date CreatedDate;
	public String CreatedUser;
	public Date UpdatedDate;
	public String UpdatedUser;
	public String Description;
	public int CountSim;
	public boolean IsExist;
	public int SubGroupID;
	public String SubBroupName;
}
