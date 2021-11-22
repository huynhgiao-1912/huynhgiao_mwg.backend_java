package mwg.wb.model.search;

import java.util.Date;
import java.util.List;

import mwg.wb.model.promotion.PromotionOldProductBO;

public class ProductOldImeiSO extends ISO {
	public double ProductID;
	public int CategoryID;
	public int ProvinceID;
	public int DistrictID;

	public long ProductPrice;
	public long Price;
	public long Price2;
	public double PricePercent;

	public int ManufacturerID;
	public int OLDID;
	public String IMEI;
	public String ProductCode;
	public String SEIMEI;
	public String Keyword;

	public int StoreID;
	public Date CreatedDate;
	public int IsPromotion;
	public int IsPromotion2;

	public long DeltaPrice;
	public long RealPrice;

	public long DeltaPrice2;
	public long RealPrice2;
	public long PriceAfterPromotion;

public long NewDeltaPrice2;
	public int IsImei;
	public int NewIsPromotion;
	public double NewDiscountValuePercent;
	public double NewDiscountValue;

	public int DiscountValue;
	public int DiscountValueByCode;
	public int DiscountValueByImei;
	public int ReCheckDiscountValue;
	public double DiscountPercent;
	public int InventoryStatusID;
	public int Quantity;

	public int ColorID;
	public PromotionOldProductBO Promotion;
	public List<PromotionOldProductBO> PromotionListByCode;
	public List<PromotionOldProductBO> PromotionListByImei;

	public int WebStatusID;
	public String PropStr;
	public String IndexLog;
	public Date IndexDate;
	public String IndexSource;
	public int CompanyID;
	public int IsErpLock;
	public Date UnlockDate;

	public int BrandID;
	public String LanguageID;

}