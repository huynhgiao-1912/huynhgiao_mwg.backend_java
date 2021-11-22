package mwg.wb.model.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import mwg.wb.model.products.ProductErpPriceBO;

@JsonInclude(Include.NON_DEFAULT)
public class ProductErpPriceBOApi extends ProductErpPriceBO {
	public ProductErpPriceBOApi clone() {
		return (ProductErpPriceBOApi) super.clone();
	}
}
