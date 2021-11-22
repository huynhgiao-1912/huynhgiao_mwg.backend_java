package mwg.wb.webapi.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mwg.wb.business.CommonHelper;
import mwg.wb.business.GameAppHelper;
import mwg.wb.business.GameAppNewsHelper;
import mwg.wb.business.LogHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Logs;
import mwg.wb.model.LogLevel;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.gameapp.GameAppBO;
import mwg.wb.model.gameapp.GameAppCategoryBO;
import mwg.wb.model.gameapp.GameAppDetailPlatformBO;
import mwg.wb.model.gameapp.GameAppFilter;
import mwg.wb.model.gameapp.GameAppSR;
import mwg.wb.model.gameapp.NewsGameAppFilter;
import mwg.wb.model.gameapp.PlatformGameAppBO;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.news.NewsSR;
import mwg.wb.model.products.ProductGalleryBO;
import mwg.wb.webapi.service.ConfigUtils;
import mwg.wb.webapi.service.HeaderBuilder;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apigameapp")
public class GameAppController {
	@Autowired
	private HttpServletRequest request;
	private static CommonHelper _commonHelper = null;
	private static GameAppHelper _tgddGameAppHelper = null;
	private static GameAppNewsHelper _tgddGameAppNewsHelper = null;
	private static ORThreadLocal factoryRead = null;
	private static ClientConfig _config = null;

	private static synchronized GameAppHelper GetGameappClientBySiteID(int siteID) {

		if (_config == null) {
			ClientConfig config = ConfigUtils.GetOnlineClientConfig();
			_config = config;
		}
		if (factoryRead == null) {
			try {
				factoryRead = new ORThreadLocal();
			} catch (Throwable e) {

				e.printStackTrace();
			}
			factoryRead.initReadAPI(_config, 0);
		}

		if (siteID == 1) {
			if (_tgddGameAppHelper == null) {

				_tgddGameAppHelper = new GameAppHelper(factoryRead, _config);
			}
			return _tgddGameAppHelper;
		} else {// mac dinh lay TGDD
			if (_tgddGameAppHelper == null) {

				_tgddGameAppHelper = new GameAppHelper(factoryRead, _config);
			}
			return _tgddGameAppHelper;
		}

	}

	private static synchronized GameAppNewsHelper GetGameappNewsClientBySiteID(int siteID) {

		if (_config == null) {
			ClientConfig config = ConfigUtils.GetOnlineClientConfig();
			_config = config;
		}
		if (factoryRead == null) {
			try {
				factoryRead = new ORThreadLocal();
			} catch (Throwable e) {

				e.printStackTrace();
			}
			factoryRead.initReadAPI(_config, 0);
		}

		if (siteID == 1) {
			if (_tgddGameAppNewsHelper == null) {

				_tgddGameAppNewsHelper = new GameAppNewsHelper(factoryRead, _config);
			}
			return _tgddGameAppNewsHelper;
		} else {// mac dinh lay TGDD
			if (_tgddGameAppNewsHelper == null) {

				_tgddGameAppNewsHelper = new GameAppNewsHelper(factoryRead, _config);
			}
			return _tgddGameAppNewsHelper;
		}

	}

	@RequestMapping(value = "/getallplatform", method = RequestMethod.GET)
	public ResponseEntity<PlatformGameAppBO[]> GetAllPlatformGameapp(Integer siteID, String langID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		var status = HttpStatus.OK;
		PlatformGameAppBO[] objgameapp = null;

		int _siteID = 1;
		if (siteID != null) {
			_siteID = siteID;
		}

		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappHelper.GetAllPlatformGameapp(siteID, "vi-VN");

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getgameapptopviewbyparentid", method = RequestMethod.GET)
	public ResponseEntity<GameAppSR> GetGameappTopviewByParentID(int parentID, int sortType, int pageIndex,
			int pageSize, Integer siteID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		var status = HttpStatus.OK;
		GameAppSR objgameapp = null;
		GameAppFilter filter = new GameAppFilter();
		filter.parentID = parentID;
		filter.sortType = sortType == 1 ? javax.swing.SortOrder.ASCENDING
				: (sortType == 2) ? javax.swing.SortOrder.DESCENDING : javax.swing.SortOrder.UNSORTED;
		filter.pageIndex = pageIndex;
		filter.pageSize = pageSize;
		filter.siteID = siteID;
		filter.platformID = 0;
		filter.isTopView = true;
		filter.langID = "vi-VN";

		int _siteID = 1;
		if (siteID != null) {
			_siteID = siteID;
		}

		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();

			objgameapp = gameappHelper.GameAppFilter(filter);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/searchgameapp2020", method = RequestMethod.POST)
	public ResponseEntity<GameAppSR> SearchGameApp2020(@RequestBody GameAppFilter filter) {

		long startTime = System.currentTimeMillis();
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		GameAppSR objgameapp = null;
		var status = HttpStatus.OK;

		filter.sortType = filter.intSortType == 1 ? javax.swing.SortOrder.ASCENDING
				: (filter.intSortType == 2) ? javax.swing.SortOrder.DESCENDING : javax.swing.SortOrder.UNSORTED;

		int _siteID = 1;
		if (filter.siteID > 0) {
			_siteID = filter.siteID;
		}

		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();

			objgameapp = gameappHelper.SearchGameApp2020(filter);

			odbtimer.end();

		} catch (Throwable e) {

			LogHelper.WriteLog(e, LogLevel.ERROR, request);

			LogHelper.WriteLog(gameappHelper.ObjectToString(request), "SearchGameApp2020");
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		long endTime = System.currentTimeMillis();
		if (endTime - startTime > 2500) {
			var message = String.format("SearchGameApp2020 - Total request time: %s ,elastic and orientdb time %s ",
					String.valueOf(endTime - startTime), String.valueOf(odbtimer.getElapsedTime()));

			LogHelper.WriteLogTimer(message, LogLevel.WARNING, request);

		}
		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getgameapptbyid", method = RequestMethod.GET)
	public ResponseEntity<GameAppBO> GetGameappByID(int gameAppID, Integer siteID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		var status = HttpStatus.OK;
		GameAppBO objgameapp = null;

		int _siteID = 1;
		if (siteID != null) {
			_siteID = siteID;
		}

		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappHelper.GetGameappByID(gameAppID, siteID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/reviewgameapptbyid", method = RequestMethod.GET)
	public ResponseEntity<GameAppBO> ReviewGameappByID(int gameAppID, Integer siteID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		GameAppBO objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;
		if (siteID != null) {
			_siteID = siteID;
		}

		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappHelper.ReviewGameappByID(gameAppID, siteID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getcategorybyplatformid", method = RequestMethod.GET)
	public ResponseEntity<GameAppCategoryBO[]> GetCateggoryByPlatformID(int ParentID, int PlatformID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		var status = HttpStatus.OK;
		GameAppCategoryBO[] objgameapp = null;

		int _siteID = 1;

		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappHelper.GetCateggoryByPlatformID(ParentID, PlatformID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getcategorybyparent", method = RequestMethod.GET)
	public ResponseEntity<GameAppCategoryBO[]> GetCateggoryByParent(int ParentID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		GameAppCategoryBO[] objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;

		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappHelper.GetCateggoryByParentID(ParentID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getcategorybyid", method = RequestMethod.GET)
	public ResponseEntity<GameAppCategoryBO> GetCateggoryByID(int categoryID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		GameAppCategoryBO objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;

		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappHelper.GetCateggoryByID(categoryID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getgameapprelatedbygameappid", method = RequestMethod.GET)
	public ResponseEntity<GameAppSR> GetGameAppRelatedByGameAppID(int gameAppID, int siteID, int pageIndex,
			int pageSize) {

		long startTime = System.currentTimeMillis();
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		GameAppSR objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = (Integer) siteID;

		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappHelper.GetGameAppRelatedByGameAppID(gameAppID, siteID, pageIndex, pageSize);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		long endTime = System.currentTimeMillis();
		if (endTime - startTime > 2500) {
			var message = String.format(
					"getgameapprelatedbygameappid - Total request time: %s ,elastic and orientdb time %s ",
					String.valueOf(endTime - startTime), String.valueOf(odbtimer.getElapsedTime()));

			LogHelper.WriteLogTimer(message, LogLevel.WARNING, request);

		}
		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getplatformbyid", method = RequestMethod.GET)
	public ResponseEntity<PlatformGameAppBO[]> GetPlatFormByID(int platformID, Integer siteID, String langID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		PlatformGameAppBO[] objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;
		if (siteID != null)
			_siteID = siteID;

		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappHelper.GetPlatFormByID(platformID, siteID, langID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getplatformbyurl", method = RequestMethod.GET)
	public ResponseEntity<PlatformGameAppBO> GetPlatFormByURL(String url, Integer siteID, String langID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		PlatformGameAppBO objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;
		if (siteID != null)
			_siteID = siteID;
		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappHelper.GetPlatFormByURL(url, siteID, langID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	// lấy thông tin của tền của của 1 game/app
	@RequestMapping(value = "/getgameappplatform", method = RequestMethod.GET)
	public ResponseEntity<GameAppDetailPlatformBO[]> GetGameappPlatform(String[] platformIdList, int gameAppID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		GameAppDetailPlatformBO[] objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;
//		if (siteID != null)
//			_siteID = siteID;
		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappHelper.GetGameappPlatform(platformIdList, gameAppID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getgameappplatformbyid", method = RequestMethod.GET)
	public ResponseEntity<GameAppDetailPlatformBO[]> GetGameappPlatformByID(int gameAppID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		GameAppDetailPlatformBO[] objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;
		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappHelper.GetGameappPlatformByID(gameAppID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getgallerybyproductid", method = RequestMethod.GET)
	public ResponseEntity<ProductGalleryBO[]> GetGalleryByProductID(int gameAppID, int platformID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		ProductGalleryBO[] objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;
		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappHelper.GetGalleryByProductID(gameAppID, platformID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getgameappfeatureinterlace", method = RequestMethod.GET)
	public ResponseEntity<GameAppBO[]> GetGameappFeatureInterlace() {

		long startTime = System.currentTimeMillis();
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		GameAppBO[] objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;
		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			String key = "GetGameappFeatureInterlace";
			objgameapp = gameappHelper.GetGameAppInterlaceFromCache(key);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		long endTime = System.currentTimeMillis();
		if (endTime - startTime > 2500) {
			var message = String.format(
					"getgameappfeatureinterlace - Total request time: %s ,elastic and orientdb time %s ",
					String.valueOf(endTime - startTime), String.valueOf(odbtimer.getElapsedTime()));

			LogHelper.WriteLogTimer(message, LogLevel.WARNING, request);

		}
		return new ResponseEntity<>(objgameapp, header, status);
	}
	
	
	@RequestMapping(value = "/getgameappbydate", method = RequestMethod.GET)
	public ResponseEntity<GameAppSR> GetGameappByDate(
			@RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date fromDate, 
			@RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date toDate, int categoryID, int pageSize,
			int pageIndex) {

		long startTime = System.currentTimeMillis();
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		GameAppSR objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;
		var gameappHelper = GetGameappClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappHelper.GetGameAppByDate(fromDate, toDate, categoryID, pageSize, pageIndex);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		long endTime = System.currentTimeMillis();
		if (endTime - startTime > 2500) {
			var message = String.format(
					"getgameappfeatureinterlace - Total request time: %s ,elastic and orientdb time %s ",
					String.valueOf(endTime - startTime), String.valueOf(odbtimer.getElapsedTime()));

			LogHelper.WriteLogTimer(message, LogLevel.WARNING, request);

		}
		return new ResponseEntity<>(objgameapp, header, status);
	}

	// VANHANH -- KETTHUCGAMEAPP - BATDAUNEWS

	@RequestMapping(value = "/getlistnews", method = RequestMethod.GET)
	public ResponseEntity<NewsSR> GetListNews(int isGame, int siteID, int pageIndex, int pageSize, int sortType) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsSR objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;

		var gameappNewsHelper = GetGameappNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappNewsHelper.GetListNews(isGame, siteID, pageIndex, pageSize, sortType);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getnewstopview7day", method = RequestMethod.GET)
	public ResponseEntity<NewsSR> GetNewsTopView7Day(int siteID, int pageIndex, int pageSize) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsSR objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;

		var gameappNewsHelper = GetGameappNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappNewsHelper.GetNewTopView7Day(siteID, pageIndex, pageSize);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getnewsrelatedbygameappid", method = RequestMethod.GET)
	public ResponseEntity<NewsSR> GetNewsRelatedByGameAppID(int gameAppID, int siteID, int pageIndex, int pageSize) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsSR objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;

		var gameappNewsHelper = GetGameappNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappNewsHelper.GetNewsRelatedByGameAppID(0, gameAppID, siteID, pageIndex, pageSize);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getnewsrelatedbygameappidv2", method = RequestMethod.GET)
	public ResponseEntity<NewsSR> GetNewsRelatedByGameAppIDV2(int newsId, int gameAppID, int siteID, int pageIndex,
			int pageSize) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsSR objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;

		var gameappNewsHelper = GetGameappNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappNewsHelper.GetNewsRelatedByGameAppID(newsId, gameAppID, siteID, pageIndex, pageSize);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/searchnews", method = RequestMethod.GET)
	public ResponseEntity<NewsSR> SearchNews(String keyWord, int siteID, int pageIndex, int pageSize) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsSR objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;

		var gameappNewsHelper = GetGameappNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappNewsHelper.SearchNews(keyWord, siteID, pageIndex, pageSize);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getnewsdetail", method = RequestMethod.GET)
	public ResponseEntity<NewsBO> GetNewsDetail(int newsID, int siteID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsBO objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;
		String message =null;
		var gameappNewsHelper = GetGameappNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappNewsHelper.GetNewsDetail(newsID, siteID);

			odbtimer.end();

		} catch (Throwable e) {
			message= e.toString();
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();
		if (objgameapp==null) {
			String params = newsID + "--" + siteID;
			message ="JAVA_API : /getnewsdetail - data-null:" + message;
			LogHelper.WriteLog(" DATA_NULL_LOG :" + message, LogLevel.LOGTRACE, params);
		}		
		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);
		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/reviewnewsdetail", method = RequestMethod.GET)
	public ResponseEntity<NewsBO> ReviewNewsDetail(int newsID, int siteID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsBO objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;

		var gameappNewsHelper = GetGameappNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappNewsHelper.ReviewNewsDetail(newsID, siteID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getnewsbyplatformid", method = RequestMethod.GET)
	public ResponseEntity<NewsSR> GetNewsByPlatformID(int platformID, int categoryID, int isGame, int isLatest,
			int isMostView, int pageIndex, int pageSize, int siteID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsSR objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;

		var gameappNewsHelper = GetGameappNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappNewsHelper.GetNewsByPlatformID(platformID, categoryID, isGame, isLatest, isMostView,
					pageIndex, pageSize, siteID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

//	@RequestMapping(value = "/searchgameapp2020", method = RequestMethod.POST)
//	public ResponseEntity<GameAppSR> SearchGameApp2020(@RequestBody GameAppFilter filter)
	@RequestMapping(value = "/getnewsrelatedbynewsid", method = RequestMethod.POST)
	public ResponseEntity<NewsSR> GetNewsRelatedByNewsID(@RequestBody NewsGameAppFilter filter) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsSR objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;

		var gameappNewsHelper = GetGameappNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappNewsHelper.GetNewsRelatedByNewsID(filter);

			odbtimer.end();

		} catch (Throwable e) {

			LogHelper.WriteLog(e, LogLevel.ERROR, request);

			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getnewstopview7daybyplatformorcategory", method = RequestMethod.GET)
	public ResponseEntity<NewsSR> GetNewsTopView7DayByPlatFormOrCategory(int platformID, int categoryID, int pageIndex,
			int pageSize, int siteID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsSR objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;

		var gameappNewsHelper = GetGameappNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappNewsHelper.GetNewsTopView7DayByPlatFormOrCategory(platformID, categoryID, pageIndex,
					pageSize, siteID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getnewsbydate", method = RequestMethod.GET)
	public ResponseEntity<NewsSR> GetNewsByDate(int platformID, int IsGame, int CategoryId, 
			@RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date fromDate, 
			@RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date toDate,
			int pageSize, int pageIndex) {

		long startTime = System.currentTimeMillis();
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsSR objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;
		var gameappNewsHelper = GetGameappNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappNewsHelper.GetGameAppNewsByDate(platformID, IsGame, CategoryId, fromDate, toDate, pageSize,
					pageIndex);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		long endTime = System.currentTimeMillis();
		if (endTime - startTime > 2500) {
			var message = String.format(
					"getgameappfeatureinterlace - Total request time: %s ,elastic and orientdb time %s ",
					String.valueOf(endTime - startTime), String.valueOf(odbtimer.getElapsedTime()));

			LogHelper.WriteLogTimer(message, LogLevel.WARNING, request);

		}
		return new ResponseEntity<>(objgameapp, header, status);
	}

	@RequestMapping(value = "/getlistnewsbycustomerid", method = RequestMethod.GET)
	public ResponseEntity<NewsSR> GetListNewsByCustomerID(String customerID, int pageIndex, int pageSize) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsSR objgameapp = null;
		var status = HttpStatus.OK;
		int _siteID = 1;

		var gameappNewsHelper = GetGameappNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objgameapp = gameappNewsHelper.GetListNewsByCustomerID(customerID, pageIndex, pageSize);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objgameapp, header, status);
	}
}
