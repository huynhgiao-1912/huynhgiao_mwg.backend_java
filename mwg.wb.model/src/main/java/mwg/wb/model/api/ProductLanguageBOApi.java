package mwg.wb.model.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import mwg.wb.model.products.ProductLanguageBO;

@JsonInclude(Include.NON_DEFAULT)
public class ProductLanguageBOApi extends ProductLanguageBO {

}
