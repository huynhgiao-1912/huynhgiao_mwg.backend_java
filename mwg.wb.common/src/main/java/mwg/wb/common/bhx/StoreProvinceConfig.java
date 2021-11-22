package mwg.wb.common.bhx;

import java.util.List;

import mwg.wb.common.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

//hiện bhx online chỉ chạy ở 1 số tỉnh/tp tương ứng với 1 số kho theo tỉnh/tp đó
//khi mở thêm tỉnh/tp thì thêm các config này
public class StoreProvinceConfig {
	private static List<StoreProvinceBO> source = null;
	
	private static int[] listAllStore = null;
	private static int[] listNomarlStore = null;
	private static int[] listOutdateStore = null;
	private static Map<Integer, StoreProvinceBO> provinceByStore = 
			new HashMap<Integer, StoreProvinceBO>();
	
	@SuppressWarnings("deprecation")
	public static List<StoreProvinceBO> GetAll()
	{
		source = new ArrayList<StoreProvinceBO>();
		var dicExpStore = new HashMap<Integer, Integer>();

		//tphcm
		dicExpStore.clear();
		dicExpStore.put(4100, 4722);
		dicExpStore.put(6463, 6613);
		source.add(new StoreProvinceBO
		        (
			            3,
			            new int[] { 4100 },
			            new int[] { 4472 },
			            4100,
			            dicExpStore,
			            new int[] { },
			            new Date(1),
			            "TP.Hồ Chí Minh",
			            "TP.HCM"
			        ));

		//dong nai
		dicExpStore.clear();
		dicExpStore.put(5771, 5971);
		source.add(new StoreProvinceBO
		        (
			            8,
			            new int[] { 5771 },
			            new int[] { 5971 },
			            5771,
			            dicExpStore,
			            new int[] { 723 },
			            new Date(1),
			            "TP. Biên Hoà",
			            "Biên Hoà"
			        ));
		//cần thơ
		dicExpStore.clear();
		dicExpStore.put(6677, 6678);
		source.add(new StoreProvinceBO
		        (
			            7,
			            new int[] { 6677 },
			            new int[] { 6678 },
			            6677,
			            dicExpStore,
			            new int[] { 54, 860, 863 },
			            new Date(1),
			            "TP. Cần Thơ",
			            "Cần Thơ"
			        ));

		//bình dương
		dicExpStore.clear();
		dicExpStore.put(6555, 6651);
		source.add(new StoreProvinceBO
		        (
			            109,
			            new int[] { 6555 },
			            new int[] { 6651 },
			            6555,
			            dicExpStore,
			            new int[] { 761, 860, 863 },
			            new Date(1),
			            "Bình Dương",
			            "Bình Dương"
			        ));

		//Tiền Giang
		dicExpStore.clear();
		dicExpStore.put(6515, 6653);
		source.add(new StoreProvinceBO
		        (
			            151,
			            new int[] { 6515 },
			            new int[] { 6653 },
			            6515,
			            dicExpStore,
			            new int[] { 1373 },
			            new Date(1),
			            "TP. Mỹ Tho",
			            "Mỹ Tho"
			        ));

		//Đắk Lắk
		dicExpStore.clear();
		dicExpStore.put(6444, 6661);
		source.add(new StoreProvinceBO
		        (
			            151,
			            new int[] { 6444 },
			            new int[] { 6661 },
			            6444,
			            dicExpStore,
			            new int[] { 833 },
			            new Date(1),
			            "TP. Buôn Ma Thuột",
			            "Buôn Ma Thuột"
			        ));

		//Bà Rịa Vũng Tàu
		dicExpStore.clear();
		dicExpStore.put(6548, 6663);
		source.add(new StoreProvinceBO
		        (
			            102,
			            new int[] { 6548 },
			            new int[] { 6663 },
			            6548,
			            dicExpStore,
			            new int[] { 891 },
			            new Date(2020,1,1,0,0,0),
			            "TP. Vũng Tàu",
			            "Vũng Tàu"
			        ));



		return source;
	}
	
	public static StoreProvinceBO Get(int provinceid)
    {
        if (provinceid == 0)
            provinceid = 3;
        
        GetAll();
        
        if (source == null)
            return null;
        
        for (StoreProvinceBO storeProvinceBO : source) {
			if(storeProvinceBO.ProvinceId == provinceid)
				return storeProvinceBO;
		}
        
       return null;
    }
	
	/// <summary>
    /// danh sách tỉnh thành
    /// </summary>
    /// <returns></returns>
    public static int[] GetAllProvince()
    {
    	
    	GetAll();
        if (source == null)
            return null;
        
        int[] result = new int[source.size()];
        for (int i = 0; i < source.size(); i++) {
			result[i] = source.get(i).ProvinceId;
			
		}
       return result;
    }
     
    public static StoreProvinceBO GetByStore(int store)
    {
    	if(provinceByStore.containsKey(store))
    		return provinceByStore.get(store);
    	
        if (store == 0)
            return null;
        GetAll();
        if (source == null)
            return null;            
        
        for (StoreProvinceBO storeProvinceBO : source) {
			if(contains(storeProvinceBO.StoreIds, store)
					|| contains(storeProvinceBO.ExpStoreIds, store))
			{
				provinceByStore.put(store, storeProvinceBO);
				return storeProvinceBO;
			}
				
		}        
        return null;
    }
    
    ///Lấy tỉnh theo store
    public static int GetProvinceByStore(int store)
    {
        if (store == 0)
            return 0;

        if (source == null)
            return -1;
        var tmp = GetByStore(store);
        if(tmp == null)
        	return -1;
        
        return tmp.ProvinceId;
    }
    
    ///Lấy danh sách kho mặc định
    public static int[] GetListDefaultStore()
    {
    	GetAll();
        if (source == null)
            return null;
        
        int[] result = new int[source.size()];
        for (int i = 0; i < source.size(); i++) {
			result[i] = source.get(i).DefaultStore;
			
		}
       return result;
    }
    
    ///Lấy danh sách kho cận date
    public static int[] GetListOutStockStore()
    {
    	if(listOutdateStore != null && listOutdateStore.length > 0)
    		return listOutdateStore;
    	
    	GetAll();
        if (source == null)
            return null;
        ArrayList<Integer> result = new ArrayList<Integer> ();
        for (int i = 0; i < source.size(); i++) {
        	var item =  source.get(i);
        	if(item.ExpStoreIds != null) {
				for (Integer integer : item.ExpStoreIds) {
					result.add(integer);
				}
        	}			
		}
        int[] arr = new int[result.size()];
        for(int i = 0; i < result.size(); i++) {
            arr[i] = result.get(i);
        }
        listOutdateStore = arr;
       return arr;
    }
    
    ///lấy danh sách kho thường
    public static int[] GetListNormalStore()
    {
    	if(listNomarlStore != null && listNomarlStore.length > 0)
    		return listNomarlStore;
    	
    	GetAll();
        if (source == null)
            return null;
        ArrayList<Integer> result = new ArrayList<Integer> ();
        for (int i = 0; i < source.size(); i++) {
        	var item =  source.get(i);
        	if(item.StoreIds != null) {
				for (Integer integer : item.StoreIds) {
					result.add(integer);
				}
        	}			
		}
        int[] arr = new int[result.size()];
        for(int i = 0; i < result.size(); i++) {
            arr[i] = result.get(i);
        }
        listNomarlStore = arr;
        return arr;
    }
    
   ///lấy danh sách tất cả các kho
    public static int[] GetListAllStore()
    {
    	if(listAllStore != null && listAllStore.length > 0)
    		return listAllStore;

    	GetAll();
        if (source == null)
            return null;
        ArrayList<Integer> result = new ArrayList<Integer> ();
        for (int i = 0; i < source.size(); i++) {
        	var item =  source.get(i);
        	if(item.StoreIds != null) {
				for (Integer integer : item.StoreIds) {
					result.add(integer);
				}
        	}
        	if(item.ExpStoreIds != null) {
				for (Integer integer : item.ExpStoreIds) {
					result.add(integer);
				}
        	}
		}
        int[] arr = new int[result.size()];
        for(int i = 0; i < result.size(); i++) {
            arr[i] = result.get(i);
        }
        listAllStore = arr;
        
        return arr;
    }

    private static boolean contains(final int[] arr, final int key) {    	
        return Arrays.stream(arr).anyMatch(i -> i == key);
    }
}
