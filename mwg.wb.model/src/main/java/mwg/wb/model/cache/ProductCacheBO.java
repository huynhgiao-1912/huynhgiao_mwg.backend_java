package mwg.wb.model.cache;

import java.util.Map;

public class ProductCacheBO extends CacheObject {
	public ProductCacheBO(String Key, String Data ) {
		super("ProductCacheBO"+Key, Data ); 
	}
	public String Name; 
	public ProductCategoryCacheBO Cate; 
	 
}
