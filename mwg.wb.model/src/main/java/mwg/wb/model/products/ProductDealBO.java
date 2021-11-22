
package mwg.wb.model.products;
/// <summary>

/// Created by 		: Nguyen Duy Trung
/// Created date 	: 10/03/2014
/// Sản phẩm Deal
/// </summary>	

import java.util.Date;

public class ProductDealBO {
	public int ProductID;
	public int CategoryID;
	public int ManufacturerID;
	public String Image;
	public double DiscountValue;
	public Date FromDate;
	public Date ToDate;
	public int Quantity;
	public int BuyCount;
	public int RemainQuantity;
	public boolean IsHot;
	public boolean IsGift;
	public boolean IsMostDisCount;
	public int DisPlayOrder;
}
