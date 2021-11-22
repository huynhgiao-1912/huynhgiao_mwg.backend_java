package mwg.wb.model.general;

import java.util.Date;
import java.util.List;

import mwg.wb.model.pm.StoreBO;
import mwg.wb.model.products.ProductWarrantyCenterBO;

public class DistrictBO {

	public List<StoreBO> StoreBOList;

	public List<ProductWarrantyCenterBO> ProductWarrantyCenterBOList;

	public List<WardBO> WardList;

	public int DistrictID;

	public int StoreCount;

	public int ProvinceID;

	public String ProvinceName;

	public String DistrictNameURL;

	public int CountClassifiedADS;

	public String DistrictName;

	public boolean IsDelete;

	public String UserDelete;

	public Date DateDelete;

	public int OrderIndex;

	public boolean IsSystem;

	public boolean IsExist;

	public int CountCenterDis;

	public String Latitude;
	public String Longitude;
}
