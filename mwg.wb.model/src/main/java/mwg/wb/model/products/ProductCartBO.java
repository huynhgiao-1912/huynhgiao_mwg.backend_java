
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Trịnh Văn Long
/// Created date 	: 9/13/2018 
/// Product
/// </summary>	

import java.util.Date;

public class ProductCartBO {
	public int TotalRecord;
	public long CartLogID;
	public String ProductIDList;
	public String Fullname;
	public String Phonenumber;
	public int Gender;
	public int ProvinceID;
	public String ProvinceName;
	public int DistrictID;
	public String DistrictName;
	public String Address;
	public String Note;
	public Date CreatedDate;
	public Date UpdatedDate;
	public boolean IsChangedCRM;
	public Date ChangedDate;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;
}
