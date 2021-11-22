package mwg.wb.common.bhx;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProvinceDetailPO {

    Integer provinceId;
    List<StoreDetailPO> storeIds;
    Integer defaultStore;
    String provinceFullName;
    String provinceName;
    String provinceShortName;
    List<Integer> districtApply;
    Date openDate;
    Date preOpeningDate;
    Map<Integer, List<Integer>> wardNotDeliveryByDistrict;
    Map<Integer, List<Integer>> wardDeliveryByDistrict;
    List<Integer> wardDeliverys;

    public ProvinceDetailPO(){
    }

    public Integer getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Integer provinceId) {
        this.provinceId = provinceId;
    }

	public List<StoreDetailPO> getStoreIds() {
        return storeIds;
    }

    public void setStoreIds(List<StoreDetailPO> storeIds) {
        this.storeIds = storeIds;
    }

    public Integer getDefaultStore() {
        return defaultStore;
    }

    public void setDefaultStore(Integer defaultStore) {
        this.defaultStore = defaultStore;
    }

    public String getProvinceFullName() {
        return provinceFullName;
    }

    public void setProvinceFullName(String provinceFullName) {
        this.provinceFullName = provinceFullName;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getProvinceShortName() {
        return provinceShortName;
    }

    public void setProvinceShortName(String provinceShortName) {
        this.provinceShortName = provinceShortName;
    }

    public List<Integer> getDistrictApply() {
        return districtApply;
    }

    public void setDistrictApply(List<Integer> districtApply) {
        this.districtApply = districtApply;
    }

    public Date getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }

    public Date getPreOpeningDate() {
        return preOpeningDate;
    }

    public void setPreOpeningDate(Date preOpeningDate) {
        this.preOpeningDate = preOpeningDate;
    }

	public Map<Integer, List<Integer>> getWardNotDeliveryByDistrict() {
		return wardNotDeliveryByDistrict;
	}

	public void setWardNotDeliveryByDistrict(Map<Integer, List<Integer>> wardNotDeliveryByDistrict) {
		this.wardNotDeliveryByDistrict = wardNotDeliveryByDistrict;
	}

	public Map<Integer, List<Integer>> getWardDeliveryByDistrict() {
		return wardDeliveryByDistrict;
	}

	public void setWardDeliveryByDistrict(Map<Integer, List<Integer>> wardDeliveryByDistrict) {
		this.wardDeliveryByDistrict = wardDeliveryByDistrict;
	}

	public List<Integer> getWardDeliverys() {
		return wardDeliverys;
	}

	public void setWardDeliverys(List<Integer> wardDeliverys) {
		this.wardDeliverys = wardDeliverys;
	}

	
}
