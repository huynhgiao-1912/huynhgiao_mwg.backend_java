package mwg.wb.common.bhx;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StoreProvinceInfoBO {
	@JsonProperty("STOREID")
    public Integer storeId;
	@JsonProperty("STORENAME")
    public String storeName;
	@JsonProperty("PROVINCEID")
    public Integer provinceId;
	@JsonProperty("WARDID")
    public Integer wardId;
	@JsonProperty("WARDNAME")
    public String wardName;
	@JsonProperty("DISTRICTID")
    public Integer districtId;
	@JsonProperty("DISTRICTNAME")
    public String districtName;
	@JsonProperty("PROVINCENAME")
    public String provinceName;
	@JsonProperty("PROVINCESHORTNAME")
    public String provinceShortName;
	@JsonProperty("OPENINGDAY")
    public Timestamp openingDay;
	@JsonProperty("CLOSINGDAY")
    public Timestamp closingDay;
	@JsonProperty("EXPIREDATESTORE")
    public String expireDateStore;
	
	public StoreProvinceInfoBO() {
		
	}
    

	public StoreProvinceInfoBO(Integer storeId, String storeName, Integer provinceId, Integer wardId, String wardName,
			Integer districtId, String districtName, String provinceName, String provinceShortName, Timestamp openingDay,
			Timestamp closingDay, String expireDateStore) {
		this.storeId = storeId;
		this.storeName = storeName;
		this.provinceId = provinceId;
		this.wardId = wardId;
		this.wardName = wardName;
		this.districtId = districtId;
		this.districtName = districtName;
		this.provinceName = provinceName;
		this.provinceShortName = provinceShortName;
		this.openingDay = openingDay;
		this.closingDay = closingDay;
		this.expireDateStore = expireDateStore;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public Integer getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(Integer provinceId) {
		this.provinceId = provinceId;
	}

	public Integer getWardId() {
		return wardId;
	}

	public void setWardId(Integer wardId) {
		this.wardId = wardId;
	}

	public String getWardName() {
		return wardName;
	}

	public void setWardName(String wardName) {
		this.wardName = wardName;
	}

	public Integer getDistrictId() {
		return districtId;
	}

	public void setDistrictId(Integer districtId) {
		this.districtId = districtId;
	}

	public String getDistrictName() {
		return districtName;
	}

	public void setDistrictName(String districtName) {
		this.districtName = districtName;
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

	public Timestamp getOpeningDay() {
		return openingDay;
	}

	public void setOpeningDay(Timestamp openingDay) {
		this.openingDay = openingDay;
	}

	public Timestamp getClosingDay() {
		return closingDay;
	}

	public void setClosingDay(Timestamp closingDay) {
		this.closingDay = closingDay;
	}

	public String getExpireDateStore() {
		return expireDateStore;
	}

	public void setExpireDateStore(String expireDateStore) {
		this.expireDateStore = expireDateStore;
	}

    
    
}
