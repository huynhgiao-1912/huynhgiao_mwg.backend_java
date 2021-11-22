package mwg.wb.business;

import java.util.Date;
import java.util.List;

public class InStockBO {
	public int quantity;
	public float quantitynew;
	public int centerquantity;
	public int samplequantity;
	public int productcodequantity;
	public int webminstock;
	public String productcode;
	public String baseProductCode;
	public int provinceid;
	public int inventorystatusid;
	public List<Integer> storeidlist;
	public List<Integer> getStoreidlist() {
		return storeidlist;
	}
	//bhx field
	public double ExpireQuantity; 
	public Date ExpiredDate; //ngày hết hạn
	public Date ExpireddateInStore; //ngày hết hạn trong kho
	//nhận unit name từ crm
	public String quantityUnit;

	public int storechangequantity;
	public int storechangecenterquantity;
	
}
