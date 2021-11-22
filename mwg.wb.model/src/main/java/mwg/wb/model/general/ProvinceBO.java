package mwg.wb.model.general;

import java.util.Date;
import java.util.List;

import mwg.wb.model.pm.StoreBO;

public class ProvinceBO {

	public List<DistrictBO> DistrictBOList;

	public int ProvinceID;

	public int StoreCount;

	public int CountryID;

	public String ProvinceName;
	public String ProvinceNameURL;

	public boolean IsDelete;

	public String UserDelete;

	public Date DateDelete;

	public int PREPaidPercent;

	public boolean IsTransfer;

	public boolean IsPaidAtHome;

	public int DeliveryDaysForAtHome;

	public int ShippingCost;

	public int OrderIndex;

	public int DeliveryDaysForPREPaid;

	public int DeliveryDaysForTransfer;

	public boolean IsDefault;

	public boolean IsSystem;

	public int DisplayOrder;

	public int NumCenter;

	public boolean IsExist;

	public int CountClassified;

	public int CountCenterPro;

	public List<StoreBO> StoreBOList;
	public String Latitude;
	public String Longitude;
}
