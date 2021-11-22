package mwg.wb.model.cache;

import java.util.Map;

public class ProductCategoryCacheBO extends CacheObject {
	public ProductCategoryCacheBO(String Key, String Data ) {
		super("ProductCategoryCacheBO"+Key, Data ); 
	}

	public String Name; 
	 
}
