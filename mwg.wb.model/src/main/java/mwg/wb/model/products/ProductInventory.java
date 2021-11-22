package mwg.wb.model.products;

import java.util.Date;

public class ProductInventory {
	 public String ProductID;
	 public double CurrentQuantity;
	 public boolean IsBase;
	 public int StatusID;
	 public int QuantityUnitID;
	 public String QuantityUnit;
	 public boolean IsOnlineOnly;
	 public double ExchangeQuantity;
	 public String BaseQuantityUnit;
	 public double ExpireQuantity; // số ngày được cho là cận date
	 public int SourceType;
	 public Date ExpiredDate; //ngày hết hạn
	 public Date ExpireddateInStore; //ngày hết hạn trong kho
	 public double Discount;
	 public int StoreID ;
}
