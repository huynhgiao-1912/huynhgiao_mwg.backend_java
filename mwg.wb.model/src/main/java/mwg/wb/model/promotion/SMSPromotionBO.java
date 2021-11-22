
package mwg.wb.model.promotion;
/// <summary>

/// Created by 		: Bạch Xuân Cường 
/// Created date 	: 4/4/2012 
/// Tên tiếng Việt
/// </summary>	

import java.util.Date;
import java.util.List;

public class SMSPromotionBO {

	public SMSPromotionBO() {
	}

	public String ProgramCode;
	public String CRMID;
	public int PromotionID;
	public Date StartDate;
	public Date EndDate;
	public String PromotionName;
	public String Description;
	public List<SMSGiftApportion> SMSGiftApportionList;
	public String sekeyword;

	public class SMSGiftApportion {

		public SMSGiftApportion() {
		}

		public String PRODUCTNAME;
		public String SMSPRODUCTCODE;
		public String PRODUCTIDREF;
		public int TOTALQUANTITY;
		public int QUANTITY;
		public String PRODUCTIDLIST;
	}
}
