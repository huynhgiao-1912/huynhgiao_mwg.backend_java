package mwg.wb.webservice.controller;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.primitives.Ints;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

//import jdk.vm.ci.code.site.Site;
import mwg.wb.business.CommonHelper;
import mwg.wb.business.GameAppHelper;
import mwg.wb.business.LogHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.webservice.NewsSvcHelper;
import mwg.wb.client.OracleClient;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.elasticsearch.dataquery.OrderType;
import mwg.wb.client.elasticsearch.dataquery.ProductQuery;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.api.ProductBOApi;
import mwg.wb.model.api.ProductGalleryBOApi;
import mwg.wb.model.common.ViewTrackingBO;
import mwg.wb.model.common.ViewTrackingBO.OBJECTTYPE;
import mwg.wb.model.pm.StoreBO;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductColorBO;
import mwg.wb.model.products.ProductDetailBO;
import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.model.products.ProductGallery360BO;
import mwg.wb.model.products.ProductGalleryBO;
import mwg.wb.model.products.ProductManuBO;
import mwg.wb.model.products.ProductSliderBO;
import mwg.wb.model.products.ProductVideoShowBO;
import mwg.wb.model.products.SpecTemplateBO;
import mwg.wb.model.products.StockBO;
import mwg.wb.model.products.TemplateRating;
import mwg.wb.model.promotion.CMSPromotion;
import mwg.wb.model.promotion.ShockPriceBO;
import mwg.wb.model.scs.ResultBO;
import mwg.wb.model.scs.TicketBO;
import mwg.wb.model.search.AccesoriesResult;
import mwg.wb.model.searchresult.FaceManuSR;
import mwg.wb.model.searchresult.GallerySR;
import mwg.wb.model.searchresult.ProductBOSR;
import mwg.wb.model.seo.ProductUrl;
import mwg.wb.model.system.DeliveryTime;
import mwg.wb.webservice.common.ConfigUtils;
import mwg.wb.webservice.common.HeaderBuilder;
import mwg.wb.model.webservice.NewsView;
import mwg.wb.model.webservice.ViewsKeywordSuggest;

//@PropertySource("classpath:/bootstrap.properties")
@RestController
@Configuration
@RefreshScope
@RequestMapping("/apinews") // newsService
public class NewsController {

	private static ClientConfig _config = null;

//	private static OracleClient dbClient = null;
//	private Connection connectDB = null;
	private static NewsSvcHelper _tgddNewsServiceAppHelper = null;

	private static synchronized NewsSvcHelper GetConfig() {

		if (_config == null) {

			ClientConfig config = ConfigUtils.GetOnlineClientConfig();

			_config = config;
		}

		if (_tgddNewsServiceAppHelper == null) {
			return _tgddNewsServiceAppHelper = new NewsSvcHelper(_config);
		}
		return _tgddNewsServiceAppHelper;

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
	@RequestMapping(value = "/gettopview7days_new", method = RequestMethod.GET)
	public ResponseEntity<List<NewsView>> GetTopView7Days_NEW() {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");

		List<NewsView> data = null;
		var newsSvcHelper = GetConfig();
		try {
			odbtimer.reset();
			data = newsSvcHelper.GetTopView7Days2();

			odbtimer.end();

		} catch (Exception e) {

			e.printStackTrace();
			return new ResponseEntity<>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		var header = HeaderBuilder.buildHeaders(codetimer);
		return new ResponseEntity<>(data, header, HttpStatus.OK);

	}

	@RequestMapping(value = "/gettopview15days", method = RequestMethod.GET)
	public ResponseEntity<List<NewsView>> GetTopView15Days(int cateID) {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");

		List<NewsView> data = null;
		var newsSvcHelper = GetConfig();
		try {
			odbtimer.reset();
			data = newsSvcHelper.GetTopView15Days(cateID);

			odbtimer.end();

		} catch (Exception e) {

			e.printStackTrace();
			return new ResponseEntity<>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		var header = HeaderBuilder.buildHeaders(codetimer);
		return new ResponseEntity<>(data, header, HttpStatus.OK);

	}

	@RequestMapping(value = "/trackingview", method = RequestMethod.PUT)
	public ResponseEntity<ResultBO> TrackingView(@RequestBody ViewTrackingBO viewTracking) {

		var codetimer = new CodeTimer("timer-all");
		codetimer.reset();
		var newsSvcHelper = GetConfig();
		var result = new ResultBO<Integer>();
		var status = HttpStatus.OK;
		try {

			if (viewTracking.Type == OBJECTTYPE.COOKDISH) {
				result = newsSvcHelper.TrackingViewDish(viewTracking);
			}
			if (viewTracking.Type == OBJECTTYPE.NEWS) {
				result = newsSvcHelper.TrackingViewNews(viewTracking);
			}

		} catch (Throwable e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();
			result.Result = -1;

			LogHelper.WriteLog(e);
		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(codetimer);
		return new ResponseEntity<>(result, header, status);

	}
	
	@RequestMapping(value = "/getmostviewbynewsid", method = RequestMethod.GET)
	public ResponseEntity<NewsView> GetMostviewbyNewsID(int NewID) {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-db");

		NewsView data = null;
		var newsSvcHelper = GetConfig();
		try {
			odbtimer.reset();
			data = newsSvcHelper.GetMostviewbyNewsID(NewID);

			odbtimer.end();

		} catch (Throwable e) {

			e.printStackTrace();
			return new ResponseEntity<>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		var header = HeaderBuilder.buildHeaders(odbtimer);
		return new ResponseEntity<>(data, header, HttpStatus.OK);

	}

	@GetMapping(value = "/addkeywordsuggestfornews")
	public ResponseEntity<ResultBO> addKeywordSuggestForNews(String keyWordSuggest) {

		var odbtimer = new CodeTimer("timer-db");

		ResultBO data = null;
		var newsSvcHelper = GetConfig();
		try {
			odbtimer.reset();
			data = newsSvcHelper.addKeywordSuggestForNews(keyWordSuggest);
			odbtimer.end();
		} catch (Throwable e) {
			e.printStackTrace();
			return new ResponseEntity<>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		var header = HeaderBuilder.buildHeaders(odbtimer);
		return new ResponseEntity<>(data, header, HttpStatus.OK);

	}

	@GetMapping(value = "/testoke")
	public ResponseEntity<ResultBO> testConectionString(String keyWordSuggest) {

		var odbtimer = new CodeTimer("timer-db");

		ResultBO data = null;
		var newsSvcHelper = GetConfig();
		try {
			odbtimer.reset();
			data = newsSvcHelper.addKeywordSuggestForNews2(keyWordSuggest);
			odbtimer.end();
		} catch (Throwable e) {
			e.printStackTrace();
			return new ResponseEntity<>(data, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		var header = HeaderBuilder.buildHeaders(odbtimer);
		return new ResponseEntity<>(data, header, HttpStatus.OK);

	}

}
