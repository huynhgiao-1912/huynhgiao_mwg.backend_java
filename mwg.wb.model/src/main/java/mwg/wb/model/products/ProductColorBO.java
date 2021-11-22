
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Vu Quy Khi 
/// Created date 	: 3/19/2012 
/// Tên tiếng Việt
/// </summary>	

import mwg.wb.common.Utils;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Date;

public class ProductColorBO {
	/// <summary>
	/// ColorID
	///
	/// </summary>
	public int ColorID;

	/// <summary>
	/// ColorName
	///
	/// </summary>
	public String ColorName;

	/// <summary>
	/// ColorCode
	///
	/// </summary>
	public String ColorCode;

	/// <summary>
	/// IsActived
	///
	/// </summary>
	public boolean IsActived;

	/// <summary>
	/// ActivedDate
	///
	/// </summary>
	public Date ActivedDate;

	/// <summary>
	/// ActivedUser
	///
	/// </summary>
	public String ActivedUser;

	/// <summary>
	/// IsDeleted
	///
	/// </summary>
	public boolean IsDeleted;

	/// <summary>
	/// DeletedDate
	///
	/// </summary>
	public Date DeletedDate;

	/// <summary>
	/// DeletedUser
	///
	/// </summary>
	public String DeletedUser;

	/// <summary>
	/// CreatedDate
	///
	/// </summary>
	public Date CreatedDate;

	/// <summary>
	/// CreatedUser
	///
	/// </summary>
	public String CreatedUser;

	/// <summary>
	/// UpdatedDate
	///
	/// </summary>
	public Date UpdatedDate;

	/// <summary>
	/// UpdatedUser
	///
	/// </summary>
	public String UpdatedUser;

	/// <summary>
	/// ICON
	///
	/// </summary>
	public String Icon;

	/// <summary>
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public int ProductID;

	public String ProductCode;

	public int CategoryID;

	public double Price;

	public String Picture;

	public double PreOrderPrice;
	
	public String Image;
	public String Bimage;
	public String Simage;
	public String Mimage;
	public int DisplayOrder;
	public int WebstatusID;

	@JsonIgnore
	public int GetImage(){
		return Utils.StringIsEmpty(Image) ? 999999 : DisplayOrder;
	}

}
