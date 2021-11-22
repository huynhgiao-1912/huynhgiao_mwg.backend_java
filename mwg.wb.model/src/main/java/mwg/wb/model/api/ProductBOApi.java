package mwg.wb.model.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductErpPriceBO;

@JsonInclude(Include.NON_DEFAULT)
public class ProductBOApi extends ProductBO {
	public ProductErpPriceBOApi ProductErpPriceBO;

	public ProductErpPriceBOApi[] ProductErpPriceBOList;

	public ProductCategoryBOApi ProductCategoryBO;

	public List<PromotionApi> Promotion;

	public ProductLanguageBOApi ProductLanguageBO;

	@JsonIgnore
	@Override
	public ProductErpPriceBO getPrice() {
		return ProductErpPriceBO;
	}

	@JsonIgnore
	@Override
	public ProductErpPriceBO[] getPriceList() {
		return ProductErpPriceBOList;
	}
}
