package mwg.wb.webapi.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import mwg.wb.business.CommonHelper;
import mwg.wb.business.CookHelper;
import mwg.wb.business.LogHelper;
import mwg.wb.business.SCSHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.model.LogLevel;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.cook.CookCategory;
import mwg.wb.model.scs.HashtagBO;
import mwg.wb.model.scs.MentionBO;
import mwg.wb.model.scs.MentionSO;
import mwg.wb.model.scs.ResultBO;
import mwg.wb.model.scs.SearchQuery;
import mwg.wb.model.scs.TicketBO;
import mwg.wb.webapi.service.ConfigUtils;
import mwg.wb.webapi.service.HeaderBuilder;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apiscs")
//Smart Customer Service Controller
public class SCSController {
	@Autowired
	private HttpServletRequest request;
	private static CommonHelper _commonHelper = null;
	private static SCSHelper _scsHelper = null;

	private static ClientConfig _config = null;

	private static synchronized SCSHelper GetClientConfig() {

		if (_config == null) {
			ClientConfig config = ConfigUtils.GetOnlineClientConfig();
			_config = config;

		}

		if (_scsHelper == null) {

			_scsHelper = new SCSHelper(_config);
		}
		return _scsHelper;

	}

	public SCSController() {

	}

	@RequestMapping(value = "/hashtag", method = RequestMethod.PUT)
	public ResponseEntity<ResultBO> InsertHashTag(@RequestBody HashtagBO hastag) {

		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<Long>();
		var status = HttpStatus.OK;

		try {
			estimer.reset();
			result = scsHelper.InsertHashTag(hastag);

			estimer.end();

		} catch (Throwable e) {

			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();
			
			
			result.Result = (long) -1;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			
			if (result.Message.contains("version_conflict_engine_exception")) {
				result.StatusCode = 201;
				result.Message = "VERSION CONFLICT NEED RETRY";

				return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(estimer, codetimer), status);
			}

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

	@RequestMapping(value = "/hashtag/{id}", method = RequestMethod.POST)
	public ResponseEntity<ResultBO> UpdateHashTag(@RequestBody HashtagBO hastag, @PathVariable("id") long id) {

		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<Long>();
		var status = HttpStatus.OK;

		try {
			estimer.reset();
			result = scsHelper.UpdateHashTag(hastag, id);

			estimer.end();

		} catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();
			
			
			result.Result = (long) -1;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			
			if (result.Message.contains("version_conflict_engine_exception")) {
				result.StatusCode = 201;
				result.Message = "VERSION CONFLICT NEED RETRY";

				return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(estimer, codetimer), status);
			}

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

	@RequestMapping(value = "/hashtag/{id}", method = RequestMethod.GET)
	public ResponseEntity<ResultBO> GetHashTag(@PathVariable("id") long id) {
		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<HashtagBO>();
		var status = HttpStatus.OK;

		try {
			estimer.reset();
			result = scsHelper.GetHashTag(id);

			estimer.end();

		} catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();
			result.Result = null;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

	@RequestMapping(value = "/hashtag/searchhashtags", method = RequestMethod.POST)
	public ResponseEntity<ResultBO> SearchHashTags(@RequestBody SearchQuery query) {

		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<List<HashtagBO>>();
		var status = HttpStatus.OK;

		try {

			result = scsHelper.SearchHashTags(query, estimer);

		} catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();
			result.Result = null;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

	@RequestMapping(value = "/mention", method = RequestMethod.PUT)
	public ResponseEntity<ResultBO> InsertMention(@RequestBody MentionBO mention) {

		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<Long>();
		var status = HttpStatus.OK;

		try {
			estimer.reset();
			result = scsHelper.InsertMention(mention);

			estimer.end();

		} catch (Throwable e) {

			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();

			
			result.Result = (long) -1;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			
			if (result.Message.contains("version_conflict_engine_exception")) {
				result.StatusCode = 201;
				result.Message = "VERSION CONFLICT NEED RETRY";

				return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(estimer, codetimer), status);
			}


			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

	@RequestMapping(value = "/mention/{id}", method = RequestMethod.POST)
	public ResponseEntity<ResultBO> UpdateMention(@RequestBody MentionBO mention, @PathVariable("id") long id) {

		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<Long>();
		var status = HttpStatus.OK;

		try {
			estimer.reset();
			result = scsHelper.UpdateMention(mention, id);

			estimer.end();

		} catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();
			
			result.Result = (long) -1;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			
			if (result.Message.contains("version_conflict_engine_exception")) {
				result.StatusCode = 201;
				result.Message = "VERSION CONFLICT NEED RETRY";

				return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(estimer, codetimer), status);
			}

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

	// update hashtags
	@RequestMapping(value = "/mention/{id}/hashtags", method = RequestMethod.POST)
	public ResponseEntity<ResultBO> UpdateHashtagInMention(String[] hashtags, @PathVariable("id") long id) {

		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<Long>();
		var status = HttpStatus.OK;

		try {
			estimer.reset();
			result = scsHelper.UpdateHashtagInMention(hashtags, id);

			estimer.end();

		} catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();
			
			result.Result = (long) -1;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			
			if (result.Message.contains("version_conflict_engine_exception")) {
				result.StatusCode = 201;
				result.Message = "VERSION CONFLICT NEED RETRY";

				return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(estimer, codetimer), status);
			}

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

	@RequestMapping(value = "/mention/{id}", method = RequestMethod.GET)
	public ResponseEntity<ResultBO> GetMention(@PathVariable("id") long id) {
		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<MentionBO>();
		var status = HttpStatus.OK;

		try {
			estimer.reset();
			result = scsHelper.GetMention(id);

			estimer.end();

		} catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();
			result.Result = null;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

	@RequestMapping(value = "/mention/searchmentions", method = RequestMethod.POST)
	public ResponseEntity<ResultBO> SearchMentions(@RequestBody SearchQuery query) {

		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<List<MentionBO>>();
		var status = HttpStatus.OK;

		try {

			result = scsHelper.SearchMentions(query, estimer);

		} catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();
			result.Result = null;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			
			

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

	@ApiOperation(value = "insert new ticket. For simple, input id only in HastagBO and MentionBO")
	@RequestMapping(value = "/ticket", method = RequestMethod.PUT)
	public ResponseEntity<ResultBO> InsertTicket(@RequestBody TicketBO ticket) {

		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<Long>();
		var status = HttpStatus.OK;

		try {
			estimer.reset();
			result = scsHelper.InsertTicket(ticket);

			estimer.end();

		} catch (Throwable e) {

			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();
		
			
			result.Result = (long) -1;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			
			if (result.Message.contains("version_conflict_engine_exception")) {
				result.StatusCode = 201;
				result.Message = "VERSION CONFLICT NEED RETRY";

				return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(estimer, codetimer), status);
			}

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

	@RequestMapping(value = "/ticket/{id}", method = RequestMethod.GET)
	public ResponseEntity<ResultBO> GetTicket(@PathVariable("id") long id) {
		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<TicketBO>();
		var status = HttpStatus.OK;

		try {
			estimer.reset();
			result = scsHelper.GetTicket(id);

			estimer.end();

		} catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();
			result.Result = null;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

	@RequestMapping(value = "/ticket/{id}", method = RequestMethod.POST)
	public ResponseEntity<ResultBO> UpdateTicket(@RequestBody TicketBO ticket, @PathVariable("id") long id) {

		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<Long>();
		var status = HttpStatus.OK;

		try {
			estimer.reset();
			result = scsHelper.UpdateTicket(ticket, id);

			estimer.end();

		} catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();
			
			result.Result = (long) -1;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			
			if (result.Message.contains("version_conflict_engine_exception")) {
				result.StatusCode = 201;
				result.Message = "VERSION CONFLICT NEED RETRY";

				return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(estimer, codetimer), status);
			}

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

	@ApiOperation(value = "update hashtag in ticket. For simple, input id only")
	@RequestMapping(value = "/ticket/{id}/hashtags", method = RequestMethod.POST)
	public ResponseEntity<ResultBO> UpdateTicketHashtags(@RequestBody HashtagBO[] hashtag,
			@PathVariable("id") long id) {

		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<Long>();
		var status = HttpStatus.OK;

		try {
			estimer.reset();
			result = scsHelper.UpdateTicketHashtags(hashtag, id);

			estimer.end();

		} catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();
			result.Result = (long) -1;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			
			if (result.Message.contains("version_conflict_engine_exception")) {
				result.StatusCode = 201;
				result.Message = "VERSION CONFLICT NEED RETRY";

				return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(estimer, codetimer), status);
			}

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

	@ApiOperation(value = "update mention in ticket. For simple, input id only")
	@RequestMapping(value = "/ticket/{id}/mentions", method = RequestMethod.POST)
	public ResponseEntity<ResultBO> UpdateTicketMentions(@RequestBody MentionBO[] mentions,
			@PathVariable("id") long id) {

		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<Long>();
		var status = HttpStatus.OK;

		try {
			estimer.reset();
			result = scsHelper.UpdateTicketMentions(mentions, id);

			estimer.end();

		} catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();
			result.Result = (long) -1;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			
			if (result.Message.contains("version_conflict_engine_exception")) {
				result.StatusCode = 201;
				result.Message = "VERSION CONFLICT NEED RETRY";

				return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(estimer, codetimer), status);
			}

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

	@RequestMapping(value = "/ticket/searchtickets", method = RequestMethod.POST)
	public ResponseEntity<ResultBO> SearchTickets(@RequestBody SearchQuery query) {

		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<List<TicketBO>>();
		var status = HttpStatus.OK;

		try {

			result = scsHelper.SearchTickets(query, estimer);

		} catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = e.toString() + "-" + e.getMessage();
			result.Result = null;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

	@ApiOperation(value = "use to input nlu data for trainning")
	@RequestMapping(value = "/nlu", method = RequestMethod.POST)
	public ResponseEntity<ResultBO> InsertNLUData(String text, String intent) {

		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es");
		codetimer.reset();

		var scsHelper = GetClientConfig();
		var result = new ResultBO<Long>();
		var status = HttpStatus.OK;

		try {
			estimer.reset();
			result = scsHelper.InsertNLUData(text, intent);

			estimer.end();

		} catch (Throwable e) {
			result.StatusCode = 500;
			result.Message = (e.toString() + "-" + e.getMessage()).replace("172.16.3.123", "");
			result.Result = (long) -1;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(estimer, codetimer);

		return new ResponseEntity<>(result, header, status);

	}

}
