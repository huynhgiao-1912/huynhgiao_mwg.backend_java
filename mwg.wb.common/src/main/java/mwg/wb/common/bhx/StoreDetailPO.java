package mwg.wb.common.bhx;

import java.sql.Timestamp;

public class StoreDetailPO {

    Integer storeId;
    Integer wardId;
    Integer districtId;
    String expStore;
    String fullName;
    Timestamp openDate;
    String preClosingDate;
    Timestamp closeDate;

    public StoreDetailPO(){
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public String getExpStore() {
        return expStore;
    }

    public void setExpStore(String expStore) {
        this.expStore = expStore;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Timestamp getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Timestamp openDate) {
        this.openDate = openDate;
    }

    public String getPreClosingDate() {
        return preClosingDate;
    }

    public void setPreClosingDate(String preClosingDate) {
        this.preClosingDate = preClosingDate;
    }

    public Timestamp getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Timestamp closeDate) {
        this.closeDate = closeDate;
    }

	public Integer getWardId() {
		return wardId;
	}

	public void setWardId(Integer wardId) {
		this.wardId = wardId;
	}

	public Integer getDistrictId() {
		return districtId;
	}

	public void setDistrictId(Integer districtId) {
		this.districtId = districtId;
	}
    
    
}
