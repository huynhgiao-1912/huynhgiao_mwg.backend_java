package mwg.wb.webapi.controller;

import java.util.List;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.graph.Network;

import mwg.wb.business.LogHelper;
import mwg.wb.business.SimHelper;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.common.DidxHelper;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.pm.StoreBO;
import mwg.wb.model.search.SimSO;
import mwg.wb.model.sim.GroupBO;
import mwg.wb.model.sim.NetworkBO;
import mwg.wb.model.sim.SimBO;
import mwg.wb.model.sim.SimBOListSR;
import mwg.wb.model.sim.SimPackageBO;
import mwg.wb.model.sim.SimPackageErpBO;
import mwg.wb.model.sim.SimQuery;
import mwg.wb.model.sim.SimStoreListSR;
import mwg.wb.webapi.service.ConfigUtils;
import mwg.wb.webapi.service.HeaderBuilder;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apisim")
public class SimController {

	private static SimHelper _simHelper = null;

	private static synchronized SimHelper GetSimClient() {

		if (_simHelper == null) {

			ClientConfig config = ConfigUtils.GetOnlineClientConfig();
			_simHelper = new SimHelper(config);
		}

		return _simHelper;
	}

	public SimController() {

	}

	@RequestMapping(value = "/getsimdetail", method = RequestMethod.GET)
	public ResponseEntity<SimBO> getSimDetail(String imei) {
		var timer = new CodeTimer("timer-all");
		SimBO sim = null;
		try {
			sim = GetSimClient().getSimDetail(imei);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
		}
		timer.end();
		return new ResponseEntity<SimBO>(sim, HeaderBuilder.buildHeaders(timer), HttpStatus.OK);
	}

	@RequestMapping(value = "/searchsim", method = RequestMethod.POST)
	public ResponseEntity<SimBOListSR> SearchSim(@RequestBody SimQuery simquery) {
		var timer = new CodeTimer("timer-all");
		var es = new CodeTimer("timer-es-query");
		var odb = new CodeTimer("timer-odb");
		SimBOListSR result = null;
		try {
			result = GetSimClient().SearchSim(simquery, timer, es, odb);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<SimBOListSR>(result, HeaderBuilder.buildHeaders(timer, es, odb), HttpStatus.OK);
	}

	@RequestMapping(value = "/getsimpackagebygroup", method = RequestMethod.GET)
	public ResponseEntity<SimPackageBO[]> GetSimPackageByGroup(int groupID) {
		CodeTimer timer = new CodeTimer("timer-all");
		SimPackageBO[] list = null;
		try {
			list = GetSimClient().GetSimPackage(groupID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
		}
		timer.end();
		return new ResponseEntity<>(list, HeaderBuilder.buildHeaders(timer), HttpStatus.OK);
	}

	@RequestMapping(value = "/getsimpackageerp", method = RequestMethod.GET)
	public ResponseEntity<SimPackageErpBO[]> GetSimPackageErp(int brandID) {
		CodeTimer timer = new CodeTimer("timer-all");
		SimPackageErpBO[] list = null;
		try {
			list = GetSimClient().GetSimPackageErp(brandID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
		}
		timer.end();
		return new ResponseEntity<>(list, HeaderBuilder.buildHeaders(timer), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/searchcam", method = RequestMethod.POST)
	public ResponseEntity<SimBOListSR> SearchCam(@RequestBody SimQuery simquery){
		var timer = new CodeTimer("timer-all");
		var es = new CodeTimer("timer-es-query");
		var odb = new CodeTimer("timer-odb");
		SimBOListSR result = null;
		try {
			result = GetSimClient().SearchCam(simquery, timer, es, odb);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<SimBOListSR>(result, HeaderBuilder.buildHeaders(timer, es, odb), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/getallsimnetwork", method = RequestMethod.GET)
	public ResponseEntity<NetworkBO[]> getAllSimNetwork(){
		var timer = new CodeTimer("timer-all");
		var odb = new CodeTimer("timer-odb");
		NetworkBO[] result = null;
		try {
			result = GetSimClient().getAllSimNetwork(odb);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<NetworkBO[]>(result, HeaderBuilder.buildHeaders(timer, null, odb), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/getallsimgroup", method = RequestMethod.GET)
	public ResponseEntity<GroupBO[]> getAllSimGroup(){
		var timer = new CodeTimer("timer-all");
		GroupBO[] result = null;
		try {
			result = GetSimClient().getAllSimGroup();
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<GroupBO[]>(result, HeaderBuilder.buildHeaders(timer, null, null), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/searchsimstore", method = RequestMethod.POST)
	public ResponseEntity<SimStoreListSR> SearchSimStore(@RequestBody SimQuery simquery){
		var timer = new CodeTimer("timer-all");
		var es = new CodeTimer("timer-es-query");
		var odb = new CodeTimer("timer-odb");
		SimStoreListSR result = null;
		try {
			result = GetSimClient().SearchSimStore(simquery,timer, es, odb);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<SimStoreListSR>(result, HeaderBuilder.buildHeaders(timer, es, odb), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/search2018", method = RequestMethod.POST)
	public ResponseEntity<SimBOListSR> Search2018(@RequestBody SimQuery simquery){
		var timer = new CodeTimer("timer-all");
		var es = new CodeTimer("timer-es-query");
		var odb = new CodeTimer("timer-odb");
		SimBOListSR result = null;
		try {
			result = GetSimClient().Search2018(simquery, timer, es, odb);
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
		}
		return new ResponseEntity<SimBOListSR>(result, HeaderBuilder.buildHeaders(timer, es, odb), HttpStatus.OK);
	}
	
}
