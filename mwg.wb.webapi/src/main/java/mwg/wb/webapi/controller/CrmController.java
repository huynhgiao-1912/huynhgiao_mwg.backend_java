package mwg.wb.webapi.controller;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import mwg.wb.business.CrmHelper;
import mwg.wb.business.LogHelper;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.crm.CRMSpecialSale;
import mwg.wb.model.crm.CRMStoreDistance;
import mwg.wb.webapi.service.ConfigUtils;
import mwg.wb.webapi.service.HeaderBuilder;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apicrm")
public class CrmController {

	private static CrmHelper _crmHelper = null;

	private static synchronized CrmHelper GetCrmClient() {

		if (_crmHelper == null) {

			ClientConfig config = ConfigUtils.GetOnlineClientConfig();
			_crmHelper = new CrmHelper(config);
		}

		return _crmHelper;
	}

	public CrmController() {

	}

	@RequestMapping(value = "/getspecialsaleprogram", method = RequestMethod.GET)
	public ResponseEntity<CRMSpecialSale[]> getSpecialSaleProgram(String productIDs, String provinceIDs) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		CRMSpecialSale[] r = null;
		try {
			r = GetCrmClient().getSpecialSaleProgram(productIDs, provinceIDs);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<>(r, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getspecialsaleprogrambycode", method = RequestMethod.GET)
	public ResponseEntity<CRMSpecialSale[]> getSpecialSaleProgramByCode(String productCode, String provinceIDs) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		CRMSpecialSale[] r = null;
		try {
			r = GetCrmClient().getSpecialSaleProgramByCode(productCode, provinceIDs);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<>(r, HeaderBuilder.buildHeaders(timer), status);
	}

	@RequestMapping(value = "/getdistancebymaingroup", method = RequestMethod.GET)
	public ResponseEntity<CRMStoreDistance[]> getDistanceByMaingroupID(int maingroupID, int wardID) {
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		CRMStoreDistance[] r = null;
		try {
			r = GetCrmClient().getDistanceByMaingroupID(maingroupID, wardID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<>(r, HeaderBuilder.buildHeaders(timer), status);
	}
}
