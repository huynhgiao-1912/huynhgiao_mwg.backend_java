package mwg.wb.model.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import mwg.wb.model.products.ProductGalleryBO;

@JsonInclude(Include.NON_DEFAULT)
public class ProductGalleryBOApi extends ProductGalleryBO {
}
