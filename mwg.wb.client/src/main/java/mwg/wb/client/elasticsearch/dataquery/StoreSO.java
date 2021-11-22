package mwg.wb.client.elasticsearch.dataquery;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.Date;

public class StoreSO {
	public Date ClosingDay;
	public Date ReOpenDate;
	public Date OpeningDay;
	public int StoreID;
//	public String RD_KEY;
	public int ProvinceID;
	public int DistrictID;
	public boolean IsShowweb;
	public boolean IsSaleStore;
	public String Keyword;
	public String Keyword_us;
	public int CompanyID;
	public int SiteID;
	public String UrlTerm;
	public double LAT;
	public double LNG;

	@GeoPointField
	public GeoPoint Location;
	public String PartnerinstallmentIDList;
	public int TypeOff;
	public Date OffBeginDate;
	public Date OffEndDate;

	public Date didx_updateddate;
}
