
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/11/2012 
/// Product
/// </summary>	

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Date;

@JsonInclude(Include.NON_DEFAULT)
public class ProductGallery360BO {
	/// <summary>
	/// PictureID
	///
	/// </summary>
	public int PictureID;

	/// <summary>
	/// Picture
	///
	/// </summary>
	public String Picture;

	/// <summary>
	/// ProductID
	///
	/// </summary>
	public int ProductID;

	/// <summary>
	/// Description
	///
	/// </summary>
	public String Description;

	/// <summary>
	/// DisplayOrder
	///
	/// </summary>
	public int DisplayOrder;

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
	/// Có tồn tại không?
	/// </summary>
	public boolean IsExist;

	public int CategoryID;

	@JsonIgnore
	public int number = -1;
}
