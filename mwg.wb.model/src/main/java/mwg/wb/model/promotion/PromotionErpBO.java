package mwg.wb.model.promotion;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import mwg.wb.common.DidxHelper;
import mwg.wb.model.search.PromotionSO;

public class PromotionErpBO {
	public String recordid;
	public int PromotionID;
	public String PromotionListGroupName;
	public boolean IsPercentDiscount;
	public double DiscountValue;
	public Date BeginDate;
	public Date EndDate;

	public String ProductCodes;
	public String ProductIds;
	public String ProductId;
	public String ProductName;
	public String GroupID;
	public String ExcludePromotion;
	public int PromotionListGroupID;
	public int ProvinceId;
	public double ToPrice;
	public double FromPrice;
	public int IsGiftPrdDiscount;
	public String QuantityList;
	public String QuantityLists;
	public String ProductIDref;// PRODUCTIDREF
	public String ProductIDRefList;
	public String ReturnValue;
	public String ReturnValues;
	public double ReturnValue2;// xu ly tu cot tren

	@JsonIgnore
	public double getReturnValue() {
		return ReturnValue2;
	}

	public double DiscountValueDisplay;

	public boolean IsPercentDiscountDisplay;
	public String MainGroupId;
	public boolean IsSpecialProgram;
	public String HomepageDescription;
	public int WebshowPriority;
	public String ISPERCENTDISCOUNTSALELIST;
	public String PROMOTIONPRICELIST;
	public String Quantities;

	public int IsCheapPrice;

	public int PromotionType;

	public int ISONLYFORSPECIALSALEPROGRAM;
	public int LimitPerProductType;
	public int SpecialOutputTypeCount;
	public int PromotionOutputTypeCount;

	///
	// input param
	// int siteid = 1;
	// int SalePrice = 1;
	// int OutputTypeID = 0;
	// int ProvinceID = -1;
	public int SiteID;
	public int SalePrice;
	public int OutputTypeID;
	public String ProductCode;
	public String recordidcommon;
	public String LangID;
	public String Codekey;

	public int BrandID;
	public int IsDeleted;
	public Date didxupdateddate;

	// bhx
	public int CorrespondingMonney;
	public String PromotionName;
	public int StockQuantity;
	public double VAT;
	public int DiscountType;
	public int Quantity;
	public int DefineQuantity;
	public int QuantityUnitID;
	public int RequestQuantity;
	public String QuantityUnit;
	public boolean IsDonateAll;
	public String Description;
	public String ApplyProductID;
	public int STOREID;
	public String ApplyProductIDRef;
	public int LimitQuantity;
	public int ProductApplyQuantity;
	public int PromotionGiftType;
	public int APPLYSUBGROUPID;
	public int DonateType;
	public int PromoStoreType;
	public boolean ISAPPLYALLSTORE;
	public int MaxQuantityOnBill;

	public String Gifts;
	public boolean GiftType;
	public String StoreIds;

	public int IsApplyByTimes;
	public int IsCheckSalePrice;
	public int SavingProgramOutputCount;

	public int IsPercentDiscountNotWeb;
	public double DiscountValueNotWeb;

	public int IsSpecialOutputTye; // => IsCheapPrice

	public int IsShowListGroupName;

	public int isApplyInPriceReport;

	// erp fields
	public double MAXTOTALPROMOTIONOFFERMONEY; // => ToValue;
	public double MINTOTALPROMOTIONOFFERMONEY; // => FromValue;
	public boolean IsNotApplyForInterestRate; // => NotApplyForInstallment;
	public String SALEPROGRAMIDLIST; // => ExcludeInstallmentProgramID;
	public int ISONLINEOUTPUTTYPE; // => IsOnline;
	public int StartTime; // => ApplyStartTime;
	public int EndTime; // => ApplyEndTime;
	public Integer LoaiChon; // => ProductApplyType;
	public int isAllowExReceipt; // giu nguyen
	public boolean iSSALEPROMOTIONLISTGROUP; // giu nguyen
	public int SPECIALOUTPUTTYECOUNT; // => SpecialOutputTypeCount
	public String EXCLUDEPROMOTIONID; // => ExcludePromotion
	public String ISREQUESTIMEI; // => IsRequestImeiWeb
	public String provinceIDList; // => provinceIDs
	public String createdPercentList; // giu nguyen
//	public boolean ISALLOWEXRECEIPT; // giu nguyen

	// from erp
	public double ToValue;
	public double FromValue;
	public boolean NotApplyForInstallment;
	public String ExcludeInstallmentProgramID;
	public boolean IsOnline;
	public int ApplyStartTime;
	public int ApplyEndTime;
	public int ProductApplyType;
	public boolean IsRequestImeiWeb;

	public int CategoryID;

	@JsonIgnore
	public double discountValue(double price) {
		var i = IsPercentDiscount ? DiscountValue * (price / 100f) : DiscountValue;
		if (i > 0 && MAXTOTALPROMOTIONOFFERMONEY <= 0) {
			ToValue = i;
		} else {
			ToValue = MAXTOTALPROMOTIONOFFERMONEY;
		}
		return i;
	}
	
	@JsonIgnore
	public double discountValueNonAssign(double price) {
		return IsPercentDiscount ? DiscountValue * (price / 100f) : DiscountValue;
	}

	@JsonIgnore
	public PromotionSO getSOObject() {
		var so = new PromotionSO();
		so.codekey = ProductCode + "_" + PromotionID;
		so.productcode = ProductCode;
		so.begindate = BeginDate.getTime();
		so.discountvalue = DiscountValue;
		so.enddate = EndDate.getTime();
		so.ispercentdiscount = IsPercentDiscount;
		so.promotiontype = PromotionType;
		so.strbegindate = BeginDate;
		so.strenddate = EndDate;
		so.provinceid = ProvinceId;
		so.groupid = GroupID;
		so.tovalue = ToValue;
		so.productcodes = ProductCodes;

		return so;
	}
@JsonIgnore
	public Promotion  toWebPromotion() {
		var so = new Promotion ();
		 return so;
	}
	public List<Integer> provinceIDs;
	public boolean checked;

	@JsonIgnore
	public boolean provinceIDApplied(int provinceID) {
		boolean beta = DidxHelper.isBeta();
		return (!beta && ProvinceId == provinceID) || (provinceIDs != null && provinceIDs.contains(provinceID));
	}
}
