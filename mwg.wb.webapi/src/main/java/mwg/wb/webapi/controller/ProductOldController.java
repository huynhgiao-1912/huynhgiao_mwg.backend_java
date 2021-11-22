package mwg.wb.webapi.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import mwg.wb.client.elasticsearch.dataquery.ProductOldModelQuery;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Logs;
import mwg.wb.common.SiteID;
import mwg.wb.model.searchresult.ProductOldModelSR;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import mwg.wb.business.LogHelper;
import mwg.wb.business.ProductOldHelper;
import mwg.wb.client.elasticsearch.dataquery.ProductOldImeiQuery;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.model.LogLevel;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductOldBO;
import mwg.wb.model.promotion.PromotionOldProductBO;
import mwg.wb.model.searchresult.ProductOldSR;
import mwg.wb.webapi.service.ConfigUtils;
import mwg.wb.webapi.service.HeaderBuilder;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apiproductold")
public class ProductOldController {
	private ProductOldHelper oldHelper;

	public ProductOldController() throws IOException {
		var config = ConfigUtils.GetOnlineClientConfig();
		var oclient = new ORThreadLocal();
		oclient.initReadAPI(config, 0);
		oldHelper = new ProductOldHelper(oclient, config);
	}

	@RequestMapping(value = "/getpromotion", method = RequestMethod.GET)
	public ResponseEntity<PromotionOldProductBO[]> getPromotion(int siteID, int storeID, int inventoryStatus,
			String productCode, String imei) {
		var timer = new CodeTimer("timer-all");
		PromotionOldProductBO[] list = null;
		try {
			list = oldHelper.getPromotion(siteID, storeID, inventoryStatus, productCode, imei);
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR);
		}
		timer.end();
		return new ResponseEntity<>(list, HeaderBuilder.buildHeaders(timer), HttpStatus.OK);
	}

	@RequestMapping(value = "/getdetail", method = RequestMethod.GET)
	public ResponseEntity<ProductOldBO> getDetail(int oldid) {
		ProductOldBO old = null;
		var timer = new CodeTimer("timer-all");
		try {

			old = oldHelper.getProductOldImei(oldid);
			timer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR);
		}

		return new ResponseEntity<>(old, HeaderBuilder.buildHeaders(timer), HttpStatus.OK);
	}

	@RequestMapping(value = "/searchimei", method = RequestMethod.POST)
	public ResponseEntity<ProductOldSR> searchImei(@RequestBody ProductOldImeiQuery query) {

		ProductOldSR result = null;
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;

		// Request must have parameter SITE is TGDD(1) or DMX(2)
		if (!Arrays.asList(1, 2).contains(query.siteID)) {
			return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
		}

		try {
			if (query.siteID == 1) {
				result = oldHelper.searchProductOldImei(query);
			} else {
				result = oldHelper.searchProductOldImeiBySiteDMX(query);
			}
			timer.end();
		} catch (Throwable e) {
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
	}
	
	// Nghia --
	@PostMapping(value = "/searchmodel")
	public ResponseEntity<ProductOldModelSR> searchProductOldModel(@RequestBody ProductOldModelQuery query) {
		ProductOldModelSR result = null;
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		int siteID = query.SiteID;


		// Request must have parameter SITE is TGDD(1) or DMX(2)
		if (!Arrays.asList(1, 2).contains(siteID)) {
			timer.end();
			return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
		}

		try {
			if (siteID== 1) {
				result = oldHelper.searchProductOldModel(query);
			} else {
				result = oldHelper.searchProductOldModeDMX(query);
			}
			timer.end();
		} catch (Throwable e) {
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<ProductOldModelSR>(result, HeaderBuilder.buildHeaders(timer), status);

	}
	
	@GetMapping(value = "/searchfeature")
	public ResponseEntity<List<ProductBO>> searchFeatureProductsOld(
			@RequestParam(name = "isHome", required = true) boolean isHome,
			@RequestParam(name = "categoryId", required = true) int categoryID,
			@RequestParam(name = "provinceId", required = true) int provinceID,
			@RequestParam(name = "siteID", required = true) int siteID) {

		List<ProductBO> result = null;
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;

		// Request must have parameter SITE is TGDD(1) or DMX(2)
		if (!Arrays.asList(1, 2).contains(siteID)) {
			timer.end();
			return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
		}

		try {
			result = oldHelper.getFeatureProductOld(isHome ? 1 : 0, categoryID, provinceID, siteID);
			timer.end();

		} catch (Throwable e) {
			Logs.LogException(
					"getFeatureProductOld(isHome: " + isHome + ", categoryID: " + categoryID + ", provinceID: "
							+ provinceID + ", siteID: " + siteID + " )" + " - Lỗi lấy sản phẩm feature may cu");
			Logs.LogException(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<List<ProductBO>>(result, HeaderBuilder.buildHeaders(timer), status);

	}


//	@RequestMapping(value = "/get", method = RequestMethod.GET)
//	public ResponseEntity<> get() {
//		return new ResponseEntity<>(null, HttpStatus.OK);
//	}
}
