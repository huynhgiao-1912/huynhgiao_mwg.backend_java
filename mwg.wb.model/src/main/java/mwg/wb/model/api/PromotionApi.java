package mwg.wb.model.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import mwg.wb.model.promotion.Promotion;

@JsonInclude(Include.NON_DEFAULT)
public class PromotionApi extends Promotion {

}
