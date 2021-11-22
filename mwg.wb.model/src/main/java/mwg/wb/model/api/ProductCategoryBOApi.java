package mwg.wb.model.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import mwg.wb.model.products.ProductCategoryBO;

@JsonInclude(Include.NON_DEFAULT)
public class ProductCategoryBOApi extends ProductCategoryBO {
		public String parentidlist;
	}
