

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
public class SimBO {
	public String StoreIDList;
	public String SimGroupIDList;
	public double Price;
	public int AccountValue;
	public String SimNoDisplay;
	public int ElementSum;
	public int Discount;
	transient public int PromotionAccount;
	transient public String PromotionText;
	public int TaxRate;
	public boolean IsWebShow;
	public int SimNetworkID;
	public int SimServiceID;
	public int Status;
	public int ShowPrice;
	public boolean IsNew;
	public boolean IsHot;
	public boolean IsDeleted;
	transient public Date DeletedDate;
	transient public String DeletedUser;
	public Date CreatedDate;
	public String CreatedUser;
	transient public Date UpdatedDate;
	transient public String UpdatedUser;
	public String SimNo;
	public String ProductNo;
	public String Logo;
	public String SIMNetworkName;
	public int SubGroupID;
	public String SubGroupName;
	public String SimGroupName;
	public String ProductName;
	transient public int MainGoupID;
	transient public int BrandID;
}
