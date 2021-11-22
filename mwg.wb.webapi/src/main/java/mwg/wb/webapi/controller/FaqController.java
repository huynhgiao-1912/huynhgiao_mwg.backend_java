package mwg.wb.webapi.controller;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;
import static org.elasticsearch.search.aggregations.AggregationBuilders.topHits;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import mwg.wb.business.CommonHelper;
import mwg.wb.business.Faqhelper;
import mwg.wb.business.LogHelper;
import mwg.wb.business.NewsHelper;
import mwg.wb.business.helper.news.TGDDNewsHelper;
import mwg.wb.client.elasticsearch.dataquery.OrderType;
import mwg.wb.client.elasticsearch.dataquery.NewsQuery.Int64Order;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.faq.FaqBO;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.search.FaqCategorySO;
import mwg.wb.model.search.FaqSO;
import mwg.wb.model.searchresult.FaqSR;
import mwg.wb.model.system.ObjectSearch;
import mwg.wb.webapi.requests.FaqListCategoryRequest;
import mwg.wb.webapi.service.ConfigUtils;
import mwg.wb.webapi.service.HeaderBuilder;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apifaq")
public class FaqController {

	private static CommonHelper _commonHelper = null;
	private static Faqhelper _tgddFaqHelper = null;
	private static ORThreadLocal factoryRead = null;
	private static ClientConfig _config = null;

	private static synchronized Faqhelper GetFaqClientBySiteID(int siteID) {

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
			if (_tgddFaqHelper == null) {

				_tgddFaqHelper = new Faqhelper(factoryRead, _config);
			}
			return _tgddFaqHelper;
		} else {// mac dinh lay TGDD
			if (_tgddFaqHelper == null) {

				_tgddFaqHelper = new Faqhelper(factoryRead, _config);
			}
			return _tgddFaqHelper;
		}

	}

	/* PHẦN NÀY LÀ DÙNG CHO FAQ */
	@RequestMapping(value = "/getmostview", method = RequestMethod.GET)
	public ResponseEntity<FaqSR> GetMostView(int categoryID, int pageIndex, int pageSize, Integer siteID) {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		FaqSR objFaq = null;

		int _siteID = 1;
		if (siteID != null) {
			_siteID = siteID;
		}

		var faqHelper = GetFaqClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objFaq = faqHelper.Ela_GetListActiveQuestion(-1, pageIndex, pageSize, siteID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e );
			return new ResponseEntity<>(objFaq, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<FaqSR>(objFaq, header, HttpStatus.OK);
	}

	@RequestMapping(value = "/getnewest", method = RequestMethod.GET)
	public ResponseEntity<FaqSR> GetNewest(int categoryID, int pageIndex, int pageSize, Integer siteID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		FaqSR objFaq = null;

		int _siteID = 1;
		if (siteID != null) {
			_siteID = siteID;
		}

		var faqHelper = GetFaqClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objFaq = faqHelper.Ela_GetListActiveQuestion(-1, pageIndex, pageSize, siteID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e );
			return new ResponseEntity<>(objFaq, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objFaq, header, HttpStatus.OK);
	}

	@RequestMapping(value = "/getlistquestionbylistquestionid", method = RequestMethod.GET)
	public ResponseEntity<FaqBO[]> GetListQuestionByListQuestionID(String[] listID, Integer siteID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		FaqBO[] objFaq = null;

		int _siteID = 1;
		if (siteID != null) {
			_siteID = siteID;
		}

		var faqHelper = GetFaqClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objFaq = faqHelper.GetListQuestionByListQuestionID(listID);

			odbtimer.end();

		} catch (Throwable e) {
			Logs.WriteLine(e);
			return new ResponseEntity<>(objFaq, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objFaq, header, HttpStatus.OK);
	}

	public ResponseEntity<FaqSR> Ela_GetListQuestionByTag(long UserID, String Tag, int pageIndex, int pageSize,
			Integer siteID) {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		FaqSR objFaq = null;

		int _siteID = 1;
		if (siteID != null) {
			_siteID = siteID;
		}

		var faqHelper = GetFaqClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objFaq = faqHelper.Ela_GetListActiveQuestion(-1, pageIndex, pageSize, siteID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e );
			return new ResponseEntity<>(objFaq, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objFaq, header, HttpStatus.OK);

	}

	@RequestMapping(value = "/getfaqcategorybyid", method = RequestMethod.GET)
	public ResponseEntity<FaqBO> GetFaqCategoryByID(int cateID, Integer siteID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		FaqBO objFaq = null;

		int _siteID = 1;
		if (siteID != null) {
			_siteID = siteID;
		}

		var faqHelper = GetFaqClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objFaq = faqHelper.GetFaqCategoryByID(cateID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e );
			return new ResponseEntity<>(objFaq, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objFaq, header, HttpStatus.OK);
	}

	@RequestMapping(value = "/getquestion", method = RequestMethod.GET)
	public ResponseEntity<FaqBO> GetQuestion(int faqID, Integer siteID) {// questionId
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		FaqBO objFaq = null;

		int _siteID = 1;
		if (siteID != null) {
			_siteID = siteID;
		}

		var faqHelper = GetFaqClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objFaq = faqHelper.GetQuestion(faqID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e );
			return new ResponseEntity<>(objFaq, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objFaq, header, HttpStatus.OK);
	}

	@RequestMapping(value = "/gethotquestion", method = RequestMethod.GET)
	public ResponseEntity<FaqBO[]> GetHotQuestion(String FaqListCategoryRequest, int countPerCat,
			Integer siteID) {
		//@RequestBody FaqListCategoryRequest faq
		
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		FaqBO[] objFaq = null;

		int _siteID = 1;
		if (siteID != null) {
			_siteID = siteID;
		}

		var faqHelper = GetFaqClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objFaq = faqHelper.GetHotQuestion(FaqListCategoryRequest.split(","), countPerCat, siteID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e ); 
			return new ResponseEntity<>(objFaq, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objFaq, header, HttpStatus.OK);
		
	}

	@RequestMapping(value = "/searchcategory", method = RequestMethod.GET)
	public ResponseEntity<FaqCategorySO[]> SearchCategory(int categoryID, Integer siteID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		FaqCategorySO[] objFaq = null;

		int _siteID = 1;
		if (siteID != null) {
			_siteID = siteID;
		}

		var faqHelper = GetFaqClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			
			objFaq = faqHelper.SearchCategory(categoryID,siteID);
			// objFaq.total = 0;
			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e );
			return new ResponseEntity<>(objFaq, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objFaq, header, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/addquestion", method = RequestMethod.POST)
	public ResponseEntity<NewsBO> AddQuestion(@RequestBody NewsBO newbo, Integer siteID){
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsBO objFaq = null;
		int _siteID = 1;
		if(siteID != null) {
			_siteID = siteID;
		}
		var faqHelper = GetFaqClientBySiteID(_siteID);
		try {
			odbtimer.reset();
			objFaq = faqHelper.AddQuestion(newbo,siteID);
			
			odbtimer.end();
		}catch(Throwable e) {
			LogHelper.WriteLog(e );
			return new ResponseEntity<>(objFaq,HttpStatus.OK);
		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(odbtimer,codetimer);
		return new ResponseEntity<>(objFaq,header,HttpStatus.OK);
	}
	
	@RequestMapping(value = "/getfaqfeature", method = RequestMethod.GET)
	public ResponseEntity<FaqBO[]> GetFAQFeature( Integer siteID){
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		FaqBO[] objFaq = null;
		int _siteID = 1;
		if(siteID != null) {
			_siteID = siteID;
		}
		var faqHelper = GetFaqClientBySiteID(_siteID);
		try {
			odbtimer.reset();
			objFaq = faqHelper.GetFAQFeature(siteID);
			
			odbtimer.end();
		}catch(Throwable e) {
			LogHelper.WriteLog(e );
			return new ResponseEntity<>(objFaq,HttpStatus.OK);
		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(odbtimer,codetimer);
		return new ResponseEntity<>(objFaq,header,HttpStatus.OK);
	}
	
	@RequestMapping(value = "/searchquestion", method = RequestMethod.POST)
	public ResponseEntity<NewsBO> SearchQuestion(Integer siteID, NewsBO qry, int orderType, Int64Order orderBy, int pageIndex, int pageSize){
		Date currentDay =  new Date();
		LocalDateTime localDateTime = currentDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsBO objFaq = null;
		int _siteID = 1;
		if(siteID != null) {
			_siteID = siteID;
		}
		var faqHelper = GetFaqClientBySiteID(_siteID);
		try {
			odbtimer.reset();
			//objFaq = faqHelper.FnSearchQuestion(-1, localDateTime.plusDays(-300), currentDay, qry, orderType, orderBy, pageIndex, pageSize);
			odbtimer.end();
		}catch(Throwable e) {
			LogHelper.WriteLog(e );
			return new ResponseEntity<>(objFaq, HttpStatus.OK);
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer,codetimer);
		return new ResponseEntity<>(objFaq,header,HttpStatus.OK);
	}
}
