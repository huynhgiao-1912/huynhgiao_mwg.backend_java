package mwg.wb.model.products;

import java.util.Date;

public class ProductInstallment {
	public int paymentID;
	public int ProductID;
	public int CategoryID;
	public int IsPayment;
	public Date FromDate;
	public Date ToDate;
	public double PercentInstallment;
	public double PaymentValue;
	public String ManuIDList;
}
