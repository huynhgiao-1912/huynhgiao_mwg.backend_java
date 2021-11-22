package mwg.wb.common;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class MessageQueue implements IMessage {
	/// <summary>
	///
	/// </summary>
	public long ID;

	public DataAction Action;
	public int PushType;
	public boolean IsCreateEdge;
	/// <summary>
	/// Input data
	/// </summary>
	public String Identify;
	public String Data;
	public String Source;
	public String RefIdentify;
	public String Note;
 
	/// <summary>
	///
	/// </summary>
	public int SiteID;
	public boolean IsCheckHash;
	public int CategoryID;
	public int BrandID;
	public int ProvinceID;
		public int DistrictID;
	
	public int Storeid;
	public int Type;
	public int CachedType;
	public int DataCenter;
	public int RepushCount;
	public long Hash;	
	 public int CompanyID;
	/// <summary>
	///
	/// </summary>
	public String Lang;
	public String Term;

	// @JsonDeserialize(using= CustomerDateAndTimeDeserialize.class)
	public Date CreatedDate;
	/// <summary>
	///
	/// </summary>
	public long Version;
	public List<SqlInfo> SqlList;//
	public String ClassName;// ms.product.Product
	public String RepushClassName;
	public String RepushQueue;
	public String Processid;

	// @Override
//	public String ToString() {
//		// return JsonConvert.SerializeObject(this);
//		return null;
//	}
}