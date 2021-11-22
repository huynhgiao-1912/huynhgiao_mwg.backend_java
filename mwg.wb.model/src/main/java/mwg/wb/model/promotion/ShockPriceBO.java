package mwg.wb.model.promotion;

import java.util.Date;
import java.util.List;

public class ShockPriceBO {

	public int ProductID;

	public int PromotionID;

	public String Image;

	public String ShockName;

	public String CreatedUser;

	public Date CreatedDate;

	public String UpdatedUser;

	public Date UpdatedDate;

	public int IsDeleted;

	public String DeletedUser;

	public Date DeletedDate;

	public int IsExist;

	public int IsSelected;

	public int IsEdited;

	public int SaleProgramID;
	public Date OffTime;
	public Date StartDate;
	public Date EndDate;
	public String PromotionName;

	public int IsLimitPromotionTimes;

	public int Maxpromotiontimes;

	public int Currentpromotiontimes;

	public int Quantity;

	public int CurrentTime;

	public Date StartDateShow;

	public int IsPercentDiscount;

	public double DiscountValue;
	public int IspercentDiscountCMS;

	public double DiscountvalueCMS;

	public int ShockPriceID;

	public String FaceImage;

	public String FaceTitle;

	public String FaceDescription;

	public int IsApplyStore;

	public String PromotionContent;

	public Date TimeOn1;

	public Date TimeOn2;

	public Date TimeOn3;

	public Date TimeOff1;

	public Date TimeOff2;

	public Date TimeOff3;

	public int SaleForm;

	public Date ShowWebFromDate;

	public Date ShowWebToDate;

	public String SMSProductCode;

	public int ShockPriceType;

	public String ComingImage;

	public String EndImage;

	public String EmbedPara;

	public String InformText;
	public String ConfirmText;

	public String ProductCodeList;

	public String PolicyDescription;

	public int PaymentType;
	public String SMSDescription;
	public String PaymentDescription;
	public int IsOnlyDelivery;
	public int IsOnlyPayment;

	public int IsAutoTransferERP;
	public int IsSendSMS;
	public int ReceiveAfterDays;
	public int IsFlashSale;
	public int CategoryId;
	public List<ShockPriceDiscountBO> ListShockPriceDiscountBO;
	public int IsInstallment;
	public int isShowPrice;
}
