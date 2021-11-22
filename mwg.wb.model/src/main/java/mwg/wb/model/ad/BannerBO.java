package mwg.wb.model.ad;

import java.util.Comparator;
import java.util.Date;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT)
public class BannerBO implements Cloneable {
	public int BannerID;
	public String Image;
	public String HtmlSource;
	public String Content;
	public int Height;
	public int Width;
	public float Price;
	public Date DateStart;
	public Date DateEnd;
	public String CompanyName;
	public String Link;
	public String DmxLink;
	public String DmxContent;
	public String CompanyAddress;
	public Date DateRegister;
	public String UserNameUpdate;
	public Date LastUpdate;
	public String HTMLSource;
	public String Align;
	public int Type;
	public String BackgroundColorLeft;
	public String BackgroundColorRight;
	public String BackgroundImageLeft;
	public String BackgroundImageRight;
	public boolean Status;
	public String Description;
	public String CreatedUser;
	public Date CreatedDate;
	public String UpdatedUser;
	public Date UpdatedDate;
	public boolean IsDeleted;
	public String DeletedUser;
	public Date DeletedDate;
	public boolean IsExist;
	public boolean IsPromotionStatus;
	public int ManuID;
	public int ProvinceID;
	public boolean IsProvince;
	public boolean IsManu;
	public int PlaceId;
	public int orderdisplay;

	public BannerCateManu[] catemanu;
	
	public int[] ProvinceIDList;

	@JsonIgnore
	public int orderValue() {
		return orderdisplay;
	}

	@JsonIgnore
	public int id() {
		return BannerID;
	}

	public void getManuID() {
		if (catemanu != null) {
			ManuID = Stream.of(catemanu).sorted(Comparator.<BannerCateManu>comparingInt(x -> x.recordid).reversed())
					.mapToInt(x -> x.manuid).findFirst().orElse(0);
			catemanu = null;
		}
	}

	public Stream<BannerBO> expandManu() {
		if (catemanu != null && catemanu.length > 0)
			return Stream.of(catemanu).map(x -> clone(x.manuid));
		return Stream.of(this);
	}

	public BannerBO clone(int manuID) {
		try {
			var x = (BannerBO) super.clone();
			x.ManuID = manuID;
			x.catemanu = null;
			return x;
		} catch (Exception e) {
			return null;
		}
	}
}

class BannerCateManu {
	public int manuid;
	public int recordid;
}