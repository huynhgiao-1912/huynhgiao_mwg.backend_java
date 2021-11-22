package mwg.wb.common;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GConfig {
	// public static String DateFormatString="yyyy-MM-dd'T'HH:mm:ss";
	public static String DateFormatString = "yyyy-MM-dd HH:mm:ss";
	public static String DateFormatStringNews = "yyyy-MM-dd'T'HH:mm:ss";
	 
	public static Map<Long, Integer> ProductTaoLao = Map.of(
			       (long)9999 
			     ,   0
			     
				,  (long) 99999 
				,  0
				
				,  (long)999999 
				, 0
				
				,  (long)9999999 
				,   0
				
				,  (long)99999999  
				,  0
				,  (long)999  
				,  0
				,  (long)11111111 
				,  0
				
				,  (long)7777777 
				,   0
				, (long) 7777 
				,   0
				,  (long)9000000  
				,  0
			 
				
				
			);
	
	
	/**
	 * Removes from this list all of producID that are contained in the
     *  collection [999999, 9999999, 99999999, 999, 9000000, 11111111].
     * 
	 * @param productIDs
	 * @return
	 */
	public static List<Long> removeProductIDInvalid(List<Long> productIDs) {
		Set<Long> productIDsInvalid = ProductTaoLao.keySet();
		productIDs.removeAll(productIDsInvalid);
		return productIDs;
	}
	
	/**
	 * Returns {@code true} if this valid
	 * @param productID
	 * @return
	 */
	public static boolean validateProductID(Long productID) {
		return !ProductTaoLao.containsKey(productID);
	}
	
}
