package mwg.wb.webservice.controller;

import java.util.List;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import mwg.wb.business.webservice.GameappSvcHelper;
import mwg.wb.business.webservice.NewsSvcHelper;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.webservice.NewsView;
import mwg.wb.webservice.common.ConfigUtils;
import mwg.wb.webservice.common.HeaderBuilder;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apigameappsvc")
public class GameappController {
	
	private static ClientConfig _config = null;

	private static GameappSvcHelper _tgddgameappServiceAppHelper = null;

	private static synchronized GameappSvcHelper GetConfig() {

		if (_config == null) {

			ClientConfig config = ConfigUtils.GetOnlineClientConfig();

			_config = config;
		}

		if (_tgddgameappServiceAppHelper == null) {
			return _tgddgameappServiceAppHelper = new GameappSvcHelper(_config);
		}
		return _tgddgameappServiceAppHelper;

	}

	@RequestMapping(value = "/getdatacenter", method = RequestMethod.GET)
	public int GetDatacenter() {

		var newsSvcHelper = GetConfig();
		return _config.DATACENTER;

	}

	@RequestMapping(value = "/gettopview7days", method = RequestMethod.GET)
	public ResponseEntity<List<NewsView>> GetTopView7Days() {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");

		List<NewsView> data = null;
		var newsSvcHelper = GetConfig();
		try {
			odbtimer.reset();
			data = newsSvcHelper.GetTopView7Days();

			odbtimer.end();

		} catch (Exception e) {

			e.printStackTrace();
			return new ResponseEntity<>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		var header = HeaderBuilder.buildHeaders(codetimer);
		return new ResponseEntity<>(data, header, HttpStatus.OK);

	}
}
