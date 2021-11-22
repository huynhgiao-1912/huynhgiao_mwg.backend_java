package mwg.wb.common.bhx;

import java.util.Date;
import java.util.HashMap;

public class StoreProvinceBO {
	
	public StoreProvinceBO(int provinceId, 
			int[] storeId, 
			int[] expStoreId, 
			int defaultStore,
			HashMap<Integer, Integer> expiredStore,
			int[] districtApply,
		    Date openDate,
		    String provinceFullName,
		    String provinceShortName
    )
	{
		this.ProvinceId = provinceId;
		this.DefaultStore = defaultStore;
		this.StoreIds = storeId;
		this.ExpStoreIds = expStoreId;
		this.ExpiredStore = expiredStore;
		this.DistrictApply = districtApply;
		this.OpenDate = openDate;
		this.ProvinceFullName = provinceFullName;
		this.ProvinceShortName = provinceShortName;
	}
	
	/// <summary>
    /// mã tỉnh thành
    /// </summary>
    public int ProvinceId;

    /// <summary>
    /// mã kho tương ứng
    /// </summary>
    public int[] StoreIds;

    /// <summary>
    /// mã kho cận date
    /// </summary>
    public int[] ExpStoreIds;
    
    /// <summary>
    /// Kho mặc định của tỉnh/ thành
    /// </summary>
    public int DefaultStore;
    
    public HashMap<Integer, Integer> ExpiredStore; 
    
    public int[] DistrictApply;
    
    public Date OpenDate;
    
    public String ProvinceFullName;
    
    public String ProvinceShortName;
            
}
