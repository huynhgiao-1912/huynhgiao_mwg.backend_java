
package mwg.wb.model.products;
/// <summary>

import java.util.Date;
import java.util.List;

public class PartnerUserBO {
	public PartnerUserBO() {
	}

	public int PartnerUserID;
	public String PartnerEmail;
	public String PartnerMobile;
	public String PartnerPassWord;
	public String PartnerFullName;
	public String PartnerMWGUserName;
	public int PartnerIsActived;
	public int PartnerIsDeleted;
	public String PartnerCreatedUser;
	public Date CreatedDate;
	public String PartnerUpdatedUser;
	public String PartnerDeletedUser;
	public Date UpdatedDate;
	public Date DeletedDate;
	public int IsAdmin;
	public boolean IsChangePrice;
	public boolean IsChangeStock;
	public boolean IsIMEIProd;
	public String PublicKey;
	public String RequiredApproveBefore;
	public int ID;
	public boolean IsPriceConfirmed;
	public boolean IsStockConfirmed;
	public String Manager;
	public int DetailID;

	public class PartnerPromotion {
		public PartnerPromotion() {
		}

		public int Partnerpromotionid;
		public String Promotionname;
		public Date Fromdate;
		public Date Todate;
		public boolean Ispercentdiscount;
		public int Discountvalue;
		public Date Createddate;
		public String Createdpartnerid;
		public Date Updateddate;
		public String Updatedpartnerid;
		public boolean Isdeleted;
		public Date Deleteddate;
		public String Deletedpartnerid;
		public boolean Isactived;
		public Date Activeddate;
		public String Activedpartnerid;
		public int ERPPromotionID;
		public int TotalRecord;
		public int PromotionType;
		public List<PartnerPromotionProduct> ListProduct;
	}

	public class PartnerPromotionGroup {
		public PartnerPromotionGroup() {
		}

		public int PromotionListGroupID;
		public String PromotionListGroupName;
		public int PromotionListGroupType;
		public int PartnerPromotionID;
		public boolean IsDiscount;
		public boolean IspercentDiscount;
		public int Discountvalue;
		public String CreatedPartnerID;
		public String UpdatedPartnerID;
	}

	public class PartnerPromotionGroupProduct {
		public PartnerPromotionGroupProduct() {
		}

		public int PromotionListGroupID;
		public String Productcode;
		public int Inventorystatusid;
		public int ProductID;
		public String ProductName;
		public int Quantity;
		public int ReturnValue;
		public int DebtPromotionPaid;
	}

	public class PartnerPromotionProduct {
		public PartnerPromotionProduct() {
		}

		public int Promotionproductid;
		public int Partnerpromotionid;
		public String Productcode;
		public int Productid;
		public Date Createddate;
		public String Createdpartnerid;
		public boolean Isdeleted;
		public Date Deleteddate;
		public String Deletedpartnerid;
		public String ProductName;
	}

	public class PartnerPromotionProductref {
		public PartnerPromotionProductref() {
		}

		public String Partnercode;
		public int Webcode;
		public String Erpcode;
		public int Partnerid;
		public int DetailID;
	}

	public class PartnerRefCode {
		public PartnerRefCode() {
		}

		public String Partnercode;
		public String Erpcode;
		public String Barcode;
		public int Webcode;
		public int Partnerid;
		public String Hrvvariantid;
		public int DetailID;
	}

	public class PartnerProductLog {
		public PartnerProductLog() {
		}

		public String Partnercode;
		public String Productname;
		public int Preprice;
		public int Curprice;
		public int Partnerid;
		public Date Createddate;
	}

	public class PartnerOrderReturn {
		public PartnerOrderReturn() {
		}

		public String OutputSaleorderID;
		public Date createddate;
		public int lat;
		public int lng;
		public String Saleorderid;
		public String inputvoucherid;
		public int customerid;
		public String ImageName;
	}

	public class PartnerImportProduct {
		public int LogID;
		public int CategoryID;
		public String CategoryName;
		public int SubGroupID;
		public String SubGroupName;
		public int PartnerID;
		public String ErpCode;
		public int ProductIDRef;
		public String C1;
		public String C2;
		public String C3;
		public String C4;
		public String C5;
		public String C6;
		public String C7;
		public String C8;
		public String C9;
		public String C10;
		public String C11;
		public String C12;
		public String C13;
		public String C14;
		public String C15;
		public String C16;
		public String C17;
		public String C18;
		public String C19;
		public String C20;
		public String C21;
		public String C22;
		public String C23;
		public String C24;
		public String C25;
		public String C26;
		public String C27;
		public String C28;
		public String C29;
		public String C30;
		public String C31;
		public String C32;
		public String C33;
		public String C34;
		public String C35;
		public String C36;
		public String C37;
		public String C38;
		public String C39;
		public String C40;
		public String C41;
		public String C42;
		public String C43;
		public String C44;
		public String C45;
		public String C46;
		public String C47;
		public String C48;
		public String C49;
		public String C50;
	}

	public class PartnerUserDetailBO {
		public PartnerUserDetailBO() {
		}

		public int DetailID;
		public int PartnerID;
		public String Email;
		public String Mobile;
		public String Password;
		public String FullName;
		public int IsAdmin;
		public String CreateUser;
		public String UpdateUser;
	}

	public class PartnerImportPriceStockLog {
		public PartnerImportPriceStockLog() {
		}

		public int LogID;
		public int PartnerID;
		public String SKU;
		public double Price;
		public int Quantity;
		public String StoreID;
		public Date CreateDate;
		public int LogType;
		public String CreatedUser;
	}

	public class PartnerProductInfo {
		public PartnerProductInfo() {
		}

		public String Partnercode;
		public int Webcode;
		public String Erpcode;
		public int Partnerid;
		public String ProductName;
	}

	public class PartnerStore {
		public PartnerStore() {
		}

		public int PartnerID;
		public int StoreID;
	}
}