package mwg.wb.model.system;

import mwg.wb.model.products.ProductDetailBO;
import mwg.wb.model.products.PropList;

public class CachedDetails {
	public long lastUpdated;
	public ProductDetailBO[] list;
	 public PropList[] PropList;

	public CachedDetails(ProductDetailBO[] list, PropList[] PropList) {
		this.list = list;
		 this.PropList = PropList;

		lastUpdated = System.currentTimeMillis();
	}
}
