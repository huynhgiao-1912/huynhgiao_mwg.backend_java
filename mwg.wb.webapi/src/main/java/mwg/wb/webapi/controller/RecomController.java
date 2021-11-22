package mwg.wb.webapi.controller;

import java.io.IOException;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import mwg.wb.business.CrmHelper;
import mwg.wb.business.LogHelper;
import mwg.wb.business.rcm.EventHelper;
import mwg.wb.business.rcm.RcmService;
import mwg.wb.business.rcm.helper.ElasticService;
import mwg.wb.business.rcm.model.ApiResultBO;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.crm.CRMSpecialSale;
import mwg.wb.webapi.service.ConfigUtils;
import mwg.wb.webapi.service.HeaderBuilder;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apirecom")
public class RecomController {

	private static RcmService _crmHelper = null;

	private static synchronized RcmService GetRecomClient() {

		if (_crmHelper == null) {
			
			ClientConfig config = ConfigUtils.GetOnlineClientConfig();
			EventHelper eventHelper=new EventHelper(config);
			ElasticService elasticService=new  ElasticService( config.RCM_ES_HOST);
			_crmHelper = new RcmService(eventHelper,elasticService);
		}

		return _crmHelper;
	}

	public RecomController() {

	}

	@RequestMapping(value = "/getrecommendationbyitem", method = RequestMethod.GET)
	 
	public ResponseEntity<ApiResultBO>  GetRecommendationByItem(String username,int productid,int top) 
			 {
		ApiResultBO  recommendation = null ;
		var timer = new CodeTimer("timer-all");
		var status = HttpStatus.OK;
		try {
			recommendation = GetRecomClient().GetRecommendationByItem(username, productid, top) ;
			
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		timer.end();
		return new ResponseEntity<>(recommendation, HeaderBuilder.buildHeaders(timer), status);
		 
		//dsd
	}
}
