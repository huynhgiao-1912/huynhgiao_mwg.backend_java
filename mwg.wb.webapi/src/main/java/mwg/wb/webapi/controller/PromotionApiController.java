package mwg.wb.webapi.controller;

import java.util.List;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import mwg.wb.business.LogHelper;
import mwg.wb.business.helper.BillPromotionHelper;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.model.LogLevel;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.promotion.BillPromotionBHX;
import mwg.wb.model.promotion.Promotion;
import mwg.wb.webapi.APIOrientClient;
import mwg.wb.webapi.service.ConfigUtils;
import mwg.wb.webapi.service.HeaderBuilder;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apipromotion")
public class PromotionApiController {
	private BillPromotionHelper bpHelper;
	public PromotionApiController() {
		ClientConfig config = ConfigUtils.GetOnlineClientConfig();
		this.bpHelper = new BillPromotionHelper(config);
	}

	@RequestMapping(value = "/ServiceGetPromotion", method = RequestMethod.POST)
	public ResponseEntity<String> ServiceGetPromotion(String prdCode, int ProvinceID, int OutputTypeID, int SalePrice,
			int siteid) {
		ErpHelper eprhelper = new ErpHelper("","");
		var timer = new CodeTimer("timer-all");
		String lstpromotion = "";
		try {
			lstpromotion = eprhelper.GetPromotionByPrdIDStr(prdCode, ProvinceID, OutputTypeID, SalePrice, siteid);
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR);
		}
		timer.end();
		return new ResponseEntity<String>(lstpromotion, HeaderBuilder.buildHeaders(timer), HttpStatus.OK);
	}

	@RequestMapping(value = "/getpromotionbyproductid", method = RequestMethod.GET)
	public ResponseEntity<Promotion[]> GetPromotionByProductID(int productID, int siteID, String langID) {
		 
		var timer = new CodeTimer("timer-all");
		Promotion[] result=null;
		try {
			result = APIOrientClient.GetOrientClient().QueryFunction("product_GetPromoByProductID", Promotion[].class,
					false, productID, siteID, langID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR);
		}
		timer.end();
		return new ResponseEntity<Promotion[]>(result, HeaderBuilder.buildHeaders(timer), HttpStatus.OK);
	}

	@RequestMapping(value = "/getpromotionbyproductcode", method = RequestMethod.GET)
	public ResponseEntity<Promotion[]> GetPromotionByProductCode(String productCode, int siteID, String langID) {
		var timer = new CodeTimer("timer-all");
		Promotion[] result = null;
		try {
			result = APIOrientClient.GetOrientClient().QueryFunction("product_GetPromobyProductCode", Promotion[].class,
					false, productCode, siteID, langID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR);
		}
		timer.end();
		return new ResponseEntity<Promotion[]>(result, HeaderBuilder.buildHeaders(timer), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/getBillPromotions", method = RequestMethod.GET)
	public ResponseEntity<List<BillPromotionBHX>> getPromotionBill(){
		var timer = new CodeTimer("timer-all");
		List<BillPromotionBHX> billBHX  = null;
		try {
			billBHX = bpHelper.getBillPromotionBHX();
		}catch(Throwable ex) {
			LogHelper.WriteLog(ex, LogLevel.ERROR);
		}
		timer.end();
		return new ResponseEntity<List<BillPromotionBHX>>(billBHX, HeaderBuilder.buildHeaders(timer), HttpStatus.OK);
	}
}
