package mwg.wb.common;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class MessageValidate   {
	 
 
	public String gTable;
	public String gKey; 
	public String gValue;
	public String gSelect; //
	
	
	public Map<String, SSObject> dbparams; 
	public String Note; 
	public Date CreatedDate;
	public String cCot; 
	public String cEdge; 
	public String cEdgeProp; 
}