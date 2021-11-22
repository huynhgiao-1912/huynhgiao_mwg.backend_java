
package mwg.wb.model.products;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT)
public class StockBO {
	public StockBO() {
	}

	public int ProvinceId;
	public String ProvinceName;
	public int DistrictId;
	public String DistrictName;
	public int StoreId;
	public String StoreName;
	public String StoreAddress;
	public String StoreFullName;
	public String ProductCode;
	public int Quantity;
	public int ProvinceSort;
	public int OrderIndex;
	public int DistrictSort;
	public int StoreSort;
	public int SiteId;
	public String WebAddress;
	public String Lat;
	public String Lng;
	public String StoreShortName;
	public int CenterQuantity;
	public int StoreTypeId;
	public Date OpeningDay;
	public boolean IsShowWeb;
	public Date ReOpenDate;
	public int SampleQuantity;
	public int ReplacePrdQuantity;
}
