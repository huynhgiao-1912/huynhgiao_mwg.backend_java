package mwg.wb.webapi.controller;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import mwg.wb.business.*;
import mwg.wb.client.elasticsearch.dataquery.NewSearch;
import mwg.wb.client.service.CodeTimers;
import mwg.wb.common.DidxHelper;
import mwg.wb.model.cook.CookDish;
import mwg.wb.model.scs.ResultBO;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sun.xml.ws.policy.privateutil.PolicyUtils.Collections;

import mwg.wb.business.helper.news.TGDDNewsHelper;
import mwg.wb.business.helper.news.BHXNewsHelper;
import mwg.wb.business.helper.news.DMXNewsHelper;
import mwg.wb.client.elasticsearch.dataquery.NewsEventQuery;
import mwg.wb.client.elasticsearch.dataquery.NewsQuery;
import mwg.wb.client.elasticsearch.dataquery.NewsQuery.Int64Order;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.LogLevel;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.news.CustomerUserBO;
//import mwg.wb.model.news.FaqBO;
import mwg.wb.model.news.GameUserBO;
import mwg.wb.model.news.HotTopicBO;
import mwg.wb.model.news.KeywordSuggest;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.news.NewsCategoryBO;
import mwg.wb.model.news.NewsCount;
import mwg.wb.model.news.NewsEventBO;
import mwg.wb.model.news.NewsGallery_NewsBO;
import mwg.wb.model.searchresult.FaqSR;
import mwg.wb.model.searchresult.NewsBOSR;
import mwg.wb.model.social.SocialNotifyBO;
import mwg.wb.model.social.SocialNotifyBOR;
import mwg.wb.model.social.SocialNotifySO;
import mwg.wb.webapi.service.ConfigUtils;
import mwg.wb.webapi.service.HeaderBuilder;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apinews")
public class NewsController {
	@Autowired
	private HttpServletRequest request;
	private static CommonHelper _commonHelper = null;
	private static TGDDNewsHelper _tgddNewsHelper = null;
	private static BHXNewsHelper _bhxNewsHelper = null;
	private static DMXNewsHelper _dmxNewsHelper = null;
	private static CookHelper _cookHelper = null;
	private static List<Integer> FaqCate = Arrays.asList(1053, 1222, 1226, 1060, -1, 1054, 1708);
	// private static ProductHelper _productHelper = null;

	private static ORThreadLocal factoryRead = null;
	private static ClientConfig _config = null;


	private static synchronized CookHelper GetCookClientBySiteID() {

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
		if (_cookHelper == null) {

			_cookHelper = new CookHelper(factoryRead, _config);
		}
		return _cookHelper;

	}


	private static synchronized NewsHelper GetNewsClientBySiteID(int siteID) {

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

//		if(_productHelper==null) {
//			_productHelper = new ProductHelper(factoryRead, _config);
//		}
		if (siteID == 1) {
			if (_tgddNewsHelper == null) {

				_tgddNewsHelper = new TGDDNewsHelper(factoryRead, _config);
			}
			return _tgddNewsHelper;
		} else if (siteID == 2) {
			if (_dmxNewsHelper == null) {

				_dmxNewsHelper = new DMXNewsHelper(factoryRead, _config);
			}
			return _dmxNewsHelper;
		} else if (siteID == 11) {
			if (_bhxNewsHelper == null) {

				_bhxNewsHelper = new BHXNewsHelper(factoryRead, _config);
			}
			return _bhxNewsHelper;
		} else {// mac dinh lay TGDD
			if (_tgddNewsHelper == null) {

				_tgddNewsHelper = new TGDDNewsHelper(factoryRead, _config);
			}
			return _tgddNewsHelper;
		}

	}

	public NewsController() {

	}

	@RequestMapping(value = "/getnewseventbyid", method = RequestMethod.GET)
	public ResponseEntity<NewsEventBO> GetNewsEventByID(int eventID, Integer siteID) {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsEventBO objEvent = null;
		var status = HttpStatus.OK;
		int _siteID = 1;
		if (siteID != null) {
			_siteID = siteID;
		}

		var newsHelper = GetNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objEvent = newsHelper.GetNewsEventByID(eventID);

			odbtimer.end();
			if (objEvent != null && objEvent.Isdeleted == 1) {
				objEvent = null;
			}

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objEvent, header, status);

	}

	@RequestMapping(value = "/getcategoriesbyparentid", method = RequestMethod.GET)
	public ResponseEntity<List<NewsCategoryBO>> GetCategoriesByParentID(int parentID, int siteID) {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		List<NewsCategoryBO> lstNewsCate = new ArrayList<NewsCategoryBO>();
		if (siteID <= 0)
			siteID = 1;
		var status = HttpStatus.OK;
		var newsHelper = GetNewsClientBySiteID(siteID);

		codetimer.reset();
		try {
			odbtimer.reset();
			lstNewsCate = newsHelper.GetCategoriesByParentID(parentID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(lstNewsCate, header, status);

	}

	@RequestMapping(value = "/getinfouserid", method = RequestMethod.GET)
	public ResponseEntity<GameUserBO> GetInfoUserID(int userID, int siteID) {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		GameUserBO gameUser = new GameUserBO();
		if (siteID <= 0)
			siteID = 1;
		var status = HttpStatus.OK;
		var newsHelper = GetNewsClientBySiteID(siteID);

		codetimer.reset();
		try {
			odbtimer.reset();
			gameUser = newsHelper.GetUserByID(userID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(gameUser, header, status);

	}

	@RequestMapping(value = "/getallhottopic", method = RequestMethod.GET)
	public ResponseEntity<List<HotTopicBO>> GetAllHotTopic(int siteID) {

		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		List<HotTopicBO> listHotTopic = new ArrayList<HotTopicBO>();
		if (siteID <= 0)
			siteID = 1;
		var newsHelper = GetNewsClientBySiteID(siteID);

		codetimer.reset();
		try {
			odbtimer.reset();
			listHotTopic = newsHelper.GetAllHotTopic();

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(listHotTopic, header, status);

	}

	@RequestMapping(value = "/getnewsdetailbynewsid", method = RequestMethod.GET)

	public ResponseEntity<NewsBO> GetNewsDetailByNewsID(int newsID, Integer siteID, Boolean isReview, Boolean isFaq) {

		long startTime = System.currentTimeMillis();
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsBO objNews = null;
		int _siteID = 1;
		if (siteID != null) {
			_siteID = siteID;
		}
		var status = HttpStatus.OK;
		var newsHelper = GetNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			codetimer.reset();
			var lstnews = newsHelper.GetNewsDetailByID(newsID);
			if (lstnews != null && lstnews.length > 0) {
				objNews = lstnews[0];
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				if (isReview == null)
					isReview = false;
				if(isFaq == null)
					isFaq = false;
				
				var isFaq2 = false;
				if(!isFaq) {
					if (!Utils.StringIsEmpty(objNews.ListCategoryID)) {
	
						var lstCate = objNews.ListCategoryID.split("\\*\\s");// remove space
						for (var item : lstCate) {
							if (FaqCate.contains(Integer.valueOf(item))) {
								isFaq2 = true;
								break;
							}
						}
	
					}
				}
				if (objNews.IsDeleted == 1 || (objNews.ListTreeCategoryID != null
						&& objNews.ListTreeCategoryID.contains("2002"))/* bai tin gameapp */
						|| (!isReview && objNews.ActivedDate != null && objNews.ActivedDate.after(new Date()))
						|| isFaq2/* bai tin hoi dap */) {
					return new ResponseEntity<>(new NewsBO(), HttpStatus.OK);
				}

				if (!Utils.StringIsEmpty(objNews.HotTopicIDList)) {
					String[] HotTopicIDArr = objNews.HotTopicIDList.split(",");
					if (HotTopicIDArr.length > 0) {

						var realtedNewsByTopic = newsHelper.GetNewsByListTopicID(HotTopicIDArr);

						objNews.ListRelatedNews = realtedNewsByTopic.stream()
								.filter(x -> x != null && x.ActivedDate.before(new Date()))
								.collect(Collectors.toList());

					}
				}
				if (objNews.UserID > 0) {
					var user = newsHelper.GetUserByIDFromCache(Integer.valueOf(objNews.UserID));
					objNews.GameUser = user;
				} else {

					GameUserBO gameUser = newsHelper.GetUserByUserIdOrCustomerIdFromCache(objNews.CreatedCustomerID+"",
							objNews.CreatedUser);
					if (gameUser == null) {// lay theo customerid

						var user = newsHelper.GetCustomerUserByIDFromCache(objNews.CreatedCustomerID+"");
						if (user != null) {
							gameUser = new GameUserBO();
							gameUser.FullName = user.CustomerName;

						}

					}
					objNews.GameUser = gameUser;

				}
				if (objNews.PostType == 4) {
					List<NewsGallery_NewsBO> lst = GetListPictureNewsGallery(objNews.NewsID, -1, -1);
					if (lst != null && lst.size() > 0) {
						String str = "";
						for (var item : lst) {
							str = str + "|" + item.Picture;
						}
						objNews.SETags = str;
					}

				}

			}

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);
		long endTime = System.currentTimeMillis();
		if (endTime - startTime > 2500) {
			var message = String.format("GetNewsDetailByNewsID - Total request time: %s , orient time %s",
					String.valueOf(endTime - startTime), String.valueOf(odbtimer.getElapsedTime()));

			LogHelper.WriteLogTimer(message, LogLevel.WARNING, request);

		}

		return new ResponseEntity<>(objNews, header, status);
		// return null;
	}

//	@RequestMapping(value = "/searchnews2020new", method = RequestMethod.POST)
//	public ResponseEntity<NewsBOSR> SearchNewsQuery2020New(@RequestBody NewsQuery newsQuery) {
//		var codetimer = new CodeTimer("timer-all");
//		var estimer = new CodeTimer("timer-es-all");
//		var odbtimer1 = new CodeTimer("timer-odb-getlistnewsbo");
//		var odbtimer2 = new CodeTimer("timer-odb-getgallery-user-hottopic-event");
//
//		var parsetimer = new CodeTimer("timer-es-parser");
//		var querytimer = new CodeTimer("timer-es-query");
//		var status = HttpStatus.OK;
//
//		codetimer.reset();
//
//		var newsHelper = GetNewsClientBySiteID(newsQuery.SiteID);
//
//		NewsBOSR result = new NewsBOSR();
//		try {
//
//			estimer.reset();
//			var solist = newsHelper.Ela_SearchNews2020(newsQuery, querytimer, parsetimer);
//			estimer.end();
//
//			result.message = solist.message;
//			result.total = solist.total;
//			result.facetList = solist.faceList;
//
//			if (solist.newsSOList != null && solist.newsSOList.size() > 0) {
//				@SuppressWarnings("unchecked")
//				List<Integer> lids = new ArrayList(solist.newsSOList.keySet());
//
//				odbtimer1.reset();
//				result.newsList = newsHelper.GetListNewsByListID(lids);
//				var g_newsEvent = newsHelper.GetNewsEventByListID(EventIDArr);
//				odbtimer1.end();
//List<String> ListNewsEventID=new ArrayList<String>();
//List<String> ListHotTopicID=new ArrayList<String>();
//List<Integer> ListGetUserByID=new ArrayList<Integer>();
//List<String> ListGetByCreatedUser=new ArrayList<String>();
//List<String> ListGetByCustomer=new ArrayList<String>();
//				odbtimer2.reset();
//				//build list
//				if (result.newsList != null && result.newsList.size() > 0) {
//					for (var objNews : result.newsList) {
//						if (objNews != null) {
//							if (!Utils.StringIsEmpty(objNews.EventIDList)) {
//								String[] EventIDArr = objNews.EventIDList.split(",");
//								if (EventIDArr.length > 0) { 
//									ListNewsEventID.addAll(Arrays.asList(EventIDArr)); 
//								}
//							}
//							if (!Utils.StringIsEmpty(objNews.HotTopicIDList)) {
//								String[] HotTopicIDArr = objNews.HotTopicIDList.split(",");
//								if (HotTopicIDArr.length > 0) {
//									ListHotTopicID.addAll(Arrays.asList(HotTopicIDArr));  
//								}
//							}
//							if (objNews.UserID > 0) {
//								ListGetUserByID.add (objNews.UserID); 
//								 
//							} else {
//
//								GameUserBO gameUser = newsHelper.GetUserByUserIdOrCustomerId(objNews.CreatedCustomerID,
//										objNews.CreatedUser);
//								if (gameUser == null) {// lay theo customerid
//
//									var user = newsHelper.GetCustomerUserByID(objNews.CreatedCustomerID);
//									if (user != null) {
//										gameUser = new GameUserBO();
//										gameUser.FullName = user.CustomerName;
//
//									}
//
//								}
//								objNews.GameUser = gameUser;
//
//							}
//
//							if (objNews.PostType == 4) {
//								List<NewsGallery_NewsBO> lst = GetListPictureNewsGallery(objNews.NewsID, -1, -1);
//								if (lst != null && lst.size() > 0) {
//									String str = "";
//									for (var item : lst) {
//										str = str + "|" + item.Picture;
//									}
//									objNews.SETags = str;
//								}
//
//							}
//
//						}
//					}
//
//				}
//				
//				
//				if (result.newsList != null && result.newsList.size() > 0) {
//					for (var objNews : result.newsList) {
//						if (objNews != null) {
//							if (!Utils.StringIsEmpty(objNews.EventIDList)) {
//								String[] EventIDArr = objNews.EventIDList.split(",");
//								if (EventIDArr.length > 0) {
//									
//
//									objNews.EventsList = newsEvent;
//								}
//							}
//							if (!Utils.StringIsEmpty(objNews.HotTopicIDList)) {
//								String[] HotTopicIDArr = objNews.HotTopicIDList.split(",");
//								if (HotTopicIDArr.length > 0) {
//
//									var realtedNewsByTopic = newsHelper.GetNewsByListTopicID(HotTopicIDArr);
//
//									objNews.ListRelatedNews = realtedNewsByTopic.stream()
//											.filter(x -> x != null && x.ActivedDate.before(new Date()))
//											.collect(Collectors.toList());
//
//								}
//							}
//							if (objNews.UserID > 0) {
//								var user = newsHelper.GetUserByID(Integer.valueOf(objNews.UserID));
//								objNews.GameUser = user;
//							} else {
//
//								GameUserBO gameUser = newsHelper.GetUserByUserIdOrCustomerId(objNews.CreatedCustomerID,
//										objNews.CreatedUser);
//								if (gameUser == null) {// lay theo customerid
//
//									var user = newsHelper.GetCustomerUserByID(objNews.CreatedCustomerID);
//									if (user != null) {
//										gameUser = new GameUserBO();
//										gameUser.FullName = user.CustomerName;
//
//									}
//
//								}
//								objNews.GameUser = gameUser;
//
//							}
//
//							if (objNews.PostType == 4) {
//								List<NewsGallery_NewsBO> lst = GetListPictureNewsGallery(objNews.NewsID, -1, -1);
//								if (lst != null && lst.size() > 0) {
//									String str = "";
//									for (var item : lst) {
//										str = str + "|" + item.Picture;
//									}
//									objNews.SETags = str;
//								}
//
//							}
//
//						}
//					}
//
//				}
//				odbtimer2.end();
//
//			}
//		} catch (Throwable e) {
//			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
//			result.message = "Failed: " + e.toString() + ": " + e.getMessage() + " - " + trace;
//			status = HttpStatus.INTERNAL_SERVER_ERROR;
//
//			LogHelper.WriteLog(e, LogLevel.ERROR, request);
//
//		}
//		codetimer.end();
//		var header = HeaderBuilder.buildHeaders(codetimer, estimer, odbtimer1, odbtimer2, parsetimer, querytimer);
//
//		return new ResponseEntity<NewsBOSR>(result, header, status);
//
//	}
	@RequestMapping(value = "/searchnews2020", method = RequestMethod.POST)
	public ResponseEntity<NewsBOSR> SearchNewsQuery2020(@RequestBody NewsQuery newsQuery) {
		long startTime = System.currentTimeMillis();
		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es-all");
		var odbtimer1 = new CodeTimer("timer-odb-getlistnewsbo");
		var odbtimer2 = new CodeTimer("timer-odb-getgallery-user-hottopic-event");

		var parsetimer = new CodeTimer("timer-es-parser");
		var querytimer = new CodeTimer("timer-es-query");
		var status = HttpStatus.OK;

		codetimer.reset();

		var newsHelper = GetNewsClientBySiteID(newsQuery.SiteID);

		NewsBOSR result = new NewsBOSR();
		try {

			estimer.reset();
			var solist = newsHelper.Ela_SearchNews2020(newsQuery, querytimer, parsetimer);
			estimer.end();

			result.message = solist.message;
			result.total = solist.total;
			result.facetList = solist.faceList;

			if (solist.newsSOList != null && solist.newsSOList.size() > 0) {
				@SuppressWarnings("unchecked")
				List<Integer> lids = new ArrayList(solist.newsSOList.keySet());

				odbtimer1.reset();
				result.newsList = newsHelper.GetListNewsByListID(lids);
				odbtimer1.end();

				if (result.newsList != null && result.newsList.size() > 0) {

					var listEventID = new ArrayList<String>();
					var listTopicID = new ArrayList<String>();
					var lstUserID = new ArrayList<Integer>();
					var lsCustomerID = new ArrayList<Double>();
					var lstCreatedUser = new ArrayList<String>();

					for (var objNews : result.newsList) {
						if (objNews != null) {
							if (!Utils.StringIsEmpty(objNews.EventIDList)) {
								String[] EventIDArr = objNews.EventIDList.split(",");
								if (EventIDArr.length > 0) {
									listEventID.addAll(Arrays.asList(EventIDArr));

								}
							}
							if (!Utils.StringIsEmpty(objNews.HotTopicIDList)) {
								String[] HotTopicIDArr = objNews.HotTopicIDList.split(",");
								if (HotTopicIDArr.length > 0) {
									listTopicID.addAll(Arrays.asList(HotTopicIDArr));

								}
							}
							if (objNews.UserID > 0) {

								lstUserID.add(objNews.UserID);
							} else {
								if (!Utils.StringIsEmpty(objNews.CreatedCustomerID+"")) {
									lsCustomerID.add(Double.valueOf(objNews.CreatedCustomerID));
								}
								if (!Utils.StringIsEmpty(objNews.CreatedUser)) {
									lstCreatedUser.add(objNews.CreatedUser);
								}

							}

//							if (objNews.PostType == 4) {
//								List<NewsGallery_NewsBO> lst = GetListPictureNewsGallery(objNews.NewsID, -1, -1);
//								if (lst != null && lst.size() > 0) {
//									String str = "";
//									for (var item : lst) {
//										str = str + "|" + item.Picture;
//									}
//									objNews.SETags = str;
//								}
//
//							}

						}
					}

					List<NewsEventBO> listNewsEvent = new ArrayList<NewsEventBO>();
					List<NewsBO> listRelatedNews = new ArrayList<NewsBO>();
					List<GameUserBO> listGameUserBO = new ArrayList<GameUserBO>();
					List<GameUserBO> listGameUserBOFromCustomer = new ArrayList<GameUserBO>();
					List<CustomerUserBO> listCustomerUserBO = new ArrayList<CustomerUserBO>();

					odbtimer2.reset();
					if (listEventID.size() > 0) {
						listNewsEvent = newsHelper.GetNewsEventByListID(
								listEventID.stream().distinct().collect(Collectors.toList()).toArray(new String[0]));

					}
					if (listTopicID.size() > 0) {
//						listRelatedNews = newsHelper
//								.GetNewsByListTopicID(listTopicID.stream().distinct().collect(Collectors.toList())
//										.toArray(new String[0]))
//								.stream().filter(x -> x != null && x.ActivedDate.before(new Date()))
//								.collect(Collectors.toList());

					}
					if (lstUserID.size() > 0) {
						listGameUserBO = newsHelper.GetUserByListID(
								lstUserID.stream().distinct().collect(Collectors.toList()).toArray(new Integer[0]));
					}
					if (lsCustomerID.size() > 0 || lstCreatedUser.size() > 0) {
						listGameUserBOFromCustomer = newsHelper.GetListUserByUserIdOrCustomerId(
								lsCustomerID.stream().distinct().collect(Collectors.toList()).toArray(new Double[0]),
								lstCreatedUser.stream().distinct().collect(Collectors.toList()).toArray(new String[0]));
					}

					if (lsCustomerID.size() > 0) {
						listCustomerUserBO = newsHelper.GetCustomerUserByListID(
								lsCustomerID.stream().distinct().collect(Collectors.toList()).toArray(new Double[0]));
					}

					odbtimer2.end();

					for (var objNews : result.newsList) {
						if (objNews != null) {
							if (!Utils.StringIsEmpty(objNews.EventIDList)) {
								String[] EventIDArr = objNews.EventIDList.split(",");
								if (EventIDArr.length > 0) {
									for (var id : EventIDArr) {
										objNews.EventsList.add(
												listNewsEvent.stream().filter(x -> x.Eventid == Integer.valueOf(id))
														.findFirst().orElse(null));
									}

								}
							}
//							if (!Utils.StringIsEmpty(objNews.HotTopicIDList)) {
//								String[] HotTopicIDArr = objNews.HotTopicIDList.split(",");
//								if (HotTopicIDArr.length > 0) {
//									for (var id : HotTopicIDArr) {
//										var lstRelated = listRelatedNews.stream()
//												.filter(x -> !Utils.StringIsEmpty(x.HotTopicIDList) && x.HotTopicIDList.contains(id))
//												.collect(Collectors.toList());
//										if (lstRelated != null && lstRelated.size() > 0) {
//
//											objNews.ListRelatedNews.addAll(lstRelated);
//										}
//									}
//
//								}
//							}

							if (objNews.UserID > 0) {
								var user = listGameUserBO.stream().filter(x -> x.UserID == objNews.UserID).findFirst()
										.orElse(null);
								objNews.GameUser = user;

							} else {

								GameUserBO gameUser = null;

								if (!Utils.StringIsEmpty(objNews.CreatedCustomerID+"")) {
									gameUser = listGameUserBOFromCustomer.stream()
											.filter(x -> !Utils.StringIsEmpty(x.CustomerId)
													&& x.CustomerId.equals(objNews.CreatedCustomerID))
											.findFirst().orElse(null);
									if (gameUser == null) {
										gameUser = listGameUserBOFromCustomer.stream()
												.filter(x -> !Utils.StringIsEmpty(x.Username)
														&& x.Username.equals(objNews.CreatedUser))
												.findFirst().orElse(null);

										if (gameUser == null) {
											var cUser = listCustomerUserBO.stream()
													.filter(x -> !Utils.StringIsEmpty(x.CustomerID)
															&& x.CustomerID.equals(objNews.CreatedCustomerID))
													.findFirst().orElse(null);
											if (cUser != null) {
												gameUser = new GameUserBO();
												gameUser.FullName = cUser.CustomerName;
											}
										}

									}
								}

								objNews.GameUser = gameUser;

							}

						}
					}

				}
				if (newsQuery.SiteID == 2) {
					result.result = result.newsList;
					result.newsList = null;
				}

			}
		} catch (Throwable e) {
			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.message = "Failed: " + e.toString() + ": " + e.getMessage() + " - " + trace;
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			var param = newsHelper.GetJsonFromObject(newsQuery);
			LogHelper.WriteLog(e, LogLevel.ERROR, param);

		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(codetimer, estimer, odbtimer1, odbtimer2, parsetimer, querytimer);
		long endTime = System.currentTimeMillis();
		if (endTime - startTime > 2500) {
			var message = String.format("searchnews2020 - Total request time: %s ,elastic time %s , orient time %s",
					String.valueOf(endTime - startTime), String.valueOf(estimer.getElapsedTime()),
					String.valueOf(odbtimer1.getElapsedTime() + odbtimer2.getElapsedTime()));

			LogHelper.WriteLogTimer(message, LogLevel.WARNING, request);

		}
		return new ResponseEntity<NewsBOSR>(result, header, status);

	}

	@RequestMapping(value = "/getrelatednews", method = RequestMethod.GET)

	public ResponseEntity<NewsBOSR> ElaSearchRelateNews(int newsID, int pageIndex, int pageSize, int rootNewsID,
			int siteID) {

		long startTime = System.currentTimeMillis();
		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es-all");
		var odbtimer = new CodeTimer("timer-odb-getlistnewsbo");
		var parsetimer = new CodeTimer("timer-es-parser");
		var querytimer = new CodeTimer("timer-es-query");
		var status = HttpStatus.OK;
		if (siteID <= 0)
			siteID = 1;

		var newsHelper = GetNewsClientBySiteID(siteID);

		NewsBOSR result = new NewsBOSR();
		try {
			var lstnews = newsHelper.GetNewsDetailByID(newsID);
			if (lstnews == null || lstnews.length == 0) {
				return null;
			} else {

				codetimer.reset();

				estimer.reset();
				var solist = newsHelper.ElaSearchRelateNews(lstnews[0], pageIndex, pageSize, rootNewsID, siteID,
						querytimer, parsetimer);
				estimer.end();

				result.total = solist.total;

				if (solist.newsSOList != null && solist.newsSOList.size() > 0) {
					@SuppressWarnings("unchecked")
					List<Integer> lids = new ArrayList(solist.newsSOList.keySet());

					odbtimer.reset();
					result.newsList = newsHelper.GetListNewsByListID(lids);
					odbtimer.end();

				}
			}
		} catch (Throwable e) {
			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.message = "Failed: " + e.toString() + ": " + e.getMessage() + " - " + trace;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(codetimer, estimer, parsetimer, querytimer);

		long endTime = System.currentTimeMillis();
		if (endTime - startTime > 2500) {
			var message = String.format("getrelatednews - Total request time: %s ,elastic time %s , orient time %s",
					String.valueOf(endTime - startTime), String.valueOf(estimer.getElapsedTime()),
					String.valueOf(odbtimer.getElapsedTime()));

			LogHelper.WriteLogTimer(message, LogLevel.WARNING, request);

		}

		return new ResponseEntity<NewsBOSR>(result, header, status);

	}

	@RequestMapping(value = "/getrelatednewsbyproductinfo", method = RequestMethod.GET)
	public ResponseEntity<NewsBOSR> GetRelatedNewsByProductInfo(int productID, int newsCount, int siteID) {

		long startTime = System.currentTimeMillis();
		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es-all");
		var odbtimer = new CodeTimer("timer-odb-getlistnewsbo");
		var parsetimer = new CodeTimer("timer-es-parser");
		var querytimer = new CodeTimer("timer-es-query");
		var status = HttpStatus.OK;
		if (siteID <= 0)
			siteID = 1;

		var newsHelper = GetNewsClientBySiteID(siteID);

		NewsBOSR result = new NewsBOSR();
		try {
			var productInfo = newsHelper.GetSimpleProductInfo(productID, siteID);

			if (productInfo == null) {
				return null;
			} else {

				codetimer.reset();

				estimer.reset();
				var solist = newsHelper.GetRelatedNewsByProductInfo(productInfo, newsCount, siteID, querytimer,
						parsetimer);
				estimer.end();

				result.total = solist.total;

				if (solist.newsSOList != null && solist.newsSOList.size() > 0) {
					@SuppressWarnings("unchecked")
					List<Integer> lids = new ArrayList(solist.newsSOList.keySet());

					odbtimer.reset();
					result.newsList = newsHelper.GetListNewsByListID(lids);
					odbtimer.end();

				}
			}
		} catch (Throwable e) {
			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.message = "Failed: " + e.toString() + ": " + e.getMessage() + " - " + trace;

			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(codetimer, estimer, parsetimer, querytimer);

		long endTime = System.currentTimeMillis();
		if (endTime - startTime > 2500) {
			var message = String.format(
					"GetRelatedNewsByProductInfo - Total request time: %s ,elastic time %s , orient time %s",
					String.valueOf(endTime - startTime), String.valueOf(estimer.getElapsedTime()),
					String.valueOf(odbtimer.getElapsedTime()));

			LogHelper.WriteLogTimer(message, LogLevel.WARNING, request);

		}
		return new ResponseEntity<NewsBOSR>(result, header, status);
	}

	@RequestMapping(value = "/searchnewsevent", method = RequestMethod.POST)
	public ResponseEntity<List<NewsEventBO>> SearchNewsEvent2020(@RequestBody NewsEventQuery newsEventQuery) {
		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es-all");
		var odbtimer1 = new CodeTimer("timer-odb-getlistnewsbo");
		var odbtimer2 = new CodeTimer("timer-odb-getlisteventtopicgalleryuser");

		var parsetimer = new CodeTimer("timer-es-parser");
		var querytimer = new CodeTimer("timer-es-query");
		if (newsEventQuery.siteID <= 0)
			newsEventQuery.siteID = 1;
		var status = HttpStatus.OK;
		var newsHelper = GetNewsClientBySiteID(newsEventQuery.siteID);

		List<NewsEventBO> finalResult = new ArrayList<NewsEventBO>();
		codetimer.reset();
		try {
			estimer.reset();
			var solist = newsHelper.Ela_SearchNewsEvent2020FromCache(newsEventQuery, querytimer, parsetimer);
			estimer.end();

			if (solist != null && solist.size() > 0) {
				@SuppressWarnings("unchecked")
				List<Integer> lids = new ArrayList(solist.stream().map(x -> x.eventID).collect(Collectors.toList()));

				odbtimer1.reset();
				finalResult = newsHelper.GetListNewsEventByListID(lids);
				odbtimer1.end();

			}
		} catch (Throwable e) {
			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));

			LogHelper.WriteLog(e, LogLevel.ERROR);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(codetimer, estimer, odbtimer1, odbtimer2, parsetimer, querytimer);

		return new ResponseEntity<List<NewsEventBO>>(finalResult, header, status);

	}

	@RequestMapping(value = "/gettoptrendingnews", method = RequestMethod.GET)
	public ResponseEntity<List<NewsBO>> GetTopTrendingNews(int SiteID, int CategoryID) {
		if (CategoryID <= 0)
			CategoryID = 0;

		if (SiteID <= 0)
			SiteID = 1;

		List<NewsBO> rsl = new ArrayList<NewsBO>();
		HttpHeaders header = null;
		var newsHelper = GetNewsClientBySiteID(SiteID);
		var status = HttpStatus.OK;
		try {
			NewsQuery qry = new NewsQuery();
			qry.CategoryId = CategoryID;
			qry.PageSize = 10;
			qry.PageIndex = 0;
			qry.OrderType = 3;
			qry.OrderValue = Int64Order.LARGEST;

			LocalDateTime localDateTime = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

			if (_config != null && _config.DATACENTER == 3) // beta beta lay nhieu de co du lieu test
			{
				localDateTime = localDateTime.plusDays(-45);

			} else {
				localDateTime = CategoryID == 999 ? localDateTime.plusDays(-1) : localDateTime.plusDays(-7);
			}

			qry.FromDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

			var resultSearch = SearchNewsQuery2020(qry);
			if (resultSearch.getBody() != null && resultSearch.getBody().newsList != null) {
				rsl = resultSearch.getBody().newsList;
			}
			header = resultSearch.getHeaders();

		} catch (Throwable objEx) {

			LogHelper.WriteLog(objEx, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}

		return new ResponseEntity<List<NewsBO>>(rsl, header, status);

	}

	@RequestMapping(value = "/getnewslistbycommentcount", method = RequestMethod.GET)
	public ResponseEntity<NewsBOSR> GetNewsListByCommentCount(int categoryID, int pagesize, int pageIndex, int siteID) {
		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es-all");
		var odbtimer1 = new CodeTimer("timer-odb-getlistnewsbo");

		var parsetimer = new CodeTimer("timer-es-parser");
		var querytimer = new CodeTimer("timer-es-query");
		var status = HttpStatus.OK;
		codetimer.reset();

		if (siteID <= 0)
			siteID = 1;

		var newsHelper = GetNewsClientBySiteID(siteID);

		NewsBOSR result = new NewsBOSR();
		try {

			estimer.reset();
			var solist = newsHelper.GetNewsListByCommentCount(categoryID, pagesize, pageIndex, siteID, querytimer,
					parsetimer);
			estimer.end();

			result.total = solist.total;
			result.facetList = solist.faceList;

			if (solist.newsSOList != null && solist.newsSOList.size() > 0) {
				@SuppressWarnings("unchecked")
				List<Integer> lids = new ArrayList(solist.newsSOList.keySet());

				odbtimer1.reset();
				result.newsList = newsHelper.GetListNewsByListID(lids);
				odbtimer1.end();

			}
		} catch (Throwable e) {
			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.message = "Failed: " + e.toString() + ": " + e.getMessage() + " - " + trace;
			LogHelper.WriteLog(e, LogLevel.ERROR, request);

			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(codetimer, estimer, odbtimer1, parsetimer, querytimer);

		return new ResponseEntity<NewsBOSR>(result, header, status);

	}

	public ResponseEntity<NewsBOSR> GetDefaultSearchNewsQuery(@RequestBody NewsQuery newsQuery) {
		var result = new NewsBOSR();
		result.message = "error on searchnews2020 with params:" + _commonHelper.ConvertObjectToJsonString(newsQuery);
		return new ResponseEntity<NewsBOSR>(result, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/getnewscatebyid", method = RequestMethod.GET)
	public ResponseEntity<NewsCategoryBO> GetNewsCateByID(int categoryId, int siteID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsCategoryBO category = null;
		var status = HttpStatus.OK;
		if (siteID < 1) {
			siteID = 1;
		}

		var newsHelper = GetNewsClientBySiteID(siteID);

		try {
			odbtimer.reset();
			category = newsHelper.GetNewsCateByID(categoryId);

			odbtimer.end();
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<NewsCategoryBO>(category, header, status);
	}

	@RequestMapping(value = "/getnewsrelativebyquery", method = RequestMethod.POST)
	public ResponseEntity<List<NewsBO>> GetNewsRelativeByQuery(@RequestBody NewsQuery newsQuery) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		List<NewsBO> result = new ArrayList<NewsBO>();
		var status = HttpStatus.OK;
		var newsHelper = GetNewsClientBySiteID(newsQuery.SiteID);

		try {
			odbtimer.reset();
			var arrNews = newsHelper.GetNewsRelativeByQuery(newsQuery, codetimer, odbtimer);

			odbtimer.end();

			if (arrNews != null && arrNews.size() > 0) {
				List<Integer> lids = new ArrayList<>();
				for (var news : arrNews) {
					lids.add(news.NewsID);
				}

				result = newsHelper.GetListNewsByListID(lids);
			}
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<List<NewsBO>>(result, header, status);
	}

	/* Get danh sách tin tức top view site BHX */
	@RequestMapping(value = "/gettopnews", method = RequestMethod.GET)
	public ResponseEntity<List<NewsBO>> GetTopNews() {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		List<NewsBO> result = new ArrayList<NewsBO>();
		var status = HttpStatus.OK;
		var newsHelper = GetNewsClientBySiteID(11);

		try {
			odbtimer.reset();

			List<NewsBO> arrNews = new ArrayList<>();
			arrNews = newsHelper.GetTopNews(codetimer, odbtimer);

			odbtimer.end();

			if (arrNews != null && arrNews.size() > 0) {
				List<Integer> lids = new ArrayList<>();
				for (var news : arrNews) {
					lids.add(news.NewsID);
				}

				result = newsHelper.GetListNewsByListID(lids);
			}
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<List<NewsBO>>(result, header, status);
	}

	private List<NewsGallery_NewsBO> GetListPictureNewsGallery(int newsID, int allbumID, int pictureID) {

		try {
			return _tgddNewsHelper.GetListPictureNewsGallery(newsID, allbumID, pictureID);
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR);
			return null;
		}
	}

	@RequestMapping(value = "/getlistnewsgallery", method = RequestMethod.GET)
	public ResponseEntity<List<NewsGallery_NewsBO>> GetListNewsGallery(int newsID, int allbumID, int pictureID,
			int siteID) {

		List<NewsGallery_NewsBO> result = new ArrayList<NewsGallery_NewsBO>();
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");

		var status = HttpStatus.OK;
		var newsHelper = GetNewsClientBySiteID(siteID);
		codetimer.reset();
		try {

			odbtimer.reset();
			result = newsHelper.GetListPictureNewsGallery(newsID, allbumID, pictureID);
			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		codetimer.end();
		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);
		return new ResponseEntity<>(result, header, status);
	}

	@RequestMapping(value = "/getrelatednewsbyproductid", method = RequestMethod.GET)
	public ResponseEntity<List<NewsBO>> GetRelatedNewsByProductID(int intProductID, int siteID) {
		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		List<NewsBO> result = new ArrayList<NewsBO>();

		var newsHelper = GetNewsClientBySiteID(siteID);

		try {
			odbtimer.reset();
			var tmpProduct = newsHelper.GetProductBO(intProductID);
			if (tmpProduct == null) {
				return new ResponseEntity<>(result, HttpStatus.OK);
			}
			var tmpdata = newsHelper.Ela_GetRelatedNewsByProductID(tmpProduct, siteID);

			odbtimer.end();

			if (tmpdata != null && tmpdata.newsSOList != null && tmpdata.newsSOList.size() > 0) {
				List<Integer> lids = new ArrayList<>();

				tmpdata.newsSOList.forEach((key, value) -> {
					if (value != null)
						lids.add(value.NewsID);
				});

				// var listid = tmpdata.newsSOList.entrySet().stream().filter(x -> x !=
				// null).filter(x -> x.getValue() !=
				// null).mapToInt(x->x.getValue().NewsID).toArray();;

				result = newsHelper.GetListNewsByListID(lids);
			}

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);

			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<List<NewsBO>>(result, header, status);
	}

	//////////////////
	//// Social Notify
	//////////////////
	@RequestMapping(value = "/addsocialnotify", method = RequestMethod.POST)
	public ResponseEntity<Long> AddSocialNotify(@RequestBody SocialNotifyBO Info) {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		long objEvent = -1;
		var status = HttpStatus.OK;
		int _siteID = 1;
		if (Info.SiteID > 0) {
			_siteID = Info.SiteID;
		}

		var newsHelper = GetNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objEvent = newsHelper.AddSocialNotify(Info);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objEvent, header, status);

	}

	@RequestMapping(value = "/getaggfollowbytopicid", method = RequestMethod.GET)
	public ResponseEntity<Long> GetAggFollowByTopicID(int TopicID, Integer SiteID) {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		long objEvent = -1;
		var status = HttpStatus.OK;
		int _siteID = 1;
		if (SiteID != null) {
			_siteID = SiteID;
		}

		var newsHelper = GetNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objEvent = newsHelper.GetAggFollowByTopicID(TopicID, SiteID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objEvent, header, status);

	}

	@RequestMapping(value = "/searchnotify", method = RequestMethod.POST)
	public ResponseEntity<SocialNotifyBOR> SearchNotify(@RequestBody SocialNotifySO qry) {
		var status = HttpStatus.OK;
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		SocialNotifyBOR result = null;

		var newsHelper = GetNewsClientBySiteID(qry.SiteID);

		try {
			odbtimer.reset();
			result = newsHelper.SearchNotify(qry);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);

			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<SocialNotifyBOR>(result, header, status);
	}

	// UserUnfollowUser
	@RequestMapping(value = "/userunfollowuser", method = RequestMethod.GET)
	public ResponseEntity<Boolean> UserUnfollowUser(int FromUserID, int ToUserID, Integer SiteID) {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		Boolean objEvent = false;
		var status = HttpStatus.OK;
		int _siteID = 1;
		if (SiteID != null) {
			_siteID = SiteID;
		}

		var newsHelper = GetNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objEvent = newsHelper.UserUnfollowUser(FromUserID, ToUserID, SiteID);

			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objEvent, header, status);

	}

	@RequestMapping(value = "/userunfollowalltopic", method = RequestMethod.GET)
	public ResponseEntity<Boolean> UserUnfollowAllTopic(int UserID, Integer SiteID) {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		Boolean objEvent = false;
		var status = HttpStatus.OK;
		int _siteID = 1;
		if (SiteID != null) {
			_siteID = SiteID;
		}

		var newsHelper = GetNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objEvent = newsHelper.UserUnfollowAllTopic(UserID, SiteID);
			odbtimer.end();
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objEvent, header, status);

	}

	@RequestMapping(value = "/tickisreadnotify", method = RequestMethod.GET)
	public ResponseEntity<Boolean> TickIsReadNotify(long NotifyID, Integer SiteID) {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		Boolean objEvent = false;
		var status = HttpStatus.OK;
		int _siteID = 1;
		if (SiteID != null) {
			_siteID = SiteID;
		}

		var newsHelper = GetNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			objEvent = newsHelper.TickIsReadNotify(NotifyID, SiteID);
			odbtimer.end();
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objEvent, header, status);

	}

	@RequestMapping(value = "/updateresetviewcount_test", method = RequestMethod.GET)
	public ResponseEntity<Boolean> UpdateResetViewCount_Test(int t) {

		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		Boolean objEvent = false;
		var status = HttpStatus.OK;
		int _siteID = 1;
//		if (SiteID != null) {
//			_siteID = SiteID;
//		}

		var newsHelper = GetNewsClientBySiteID(_siteID);

		try {
			odbtimer.reset();
			// objEvent = newsHelper.UserUnfollowAllTopic(UserID, SiteID);
			if (t == 1) {
				objEvent = newsHelper.UpdateResetViewCount_Test();
			} else {
				objEvent = newsHelper.UpdateResetViewCount2();
			}
			odbtimer.end();
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);

		return new ResponseEntity<>(objEvent, header, status);

	}

	@RequestMapping(value = "/getnewslistbycategoryid", method = RequestMethod.POST)
	public ResponseEntity<NewsBOSR> GetNewsListByCategoryID(@RequestBody NewsQuery newsQuery) {
		long startTime = System.currentTimeMillis();
		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es-all");
		var odbtimer1 = new CodeTimer("timer-odb-getlistnewsbo");
		var odbtimer2 = new CodeTimer("timer-odb-getgallery-user-hottopic-event");

		var parsetimer = new CodeTimer("timer-es-parser");
		var querytimer = new CodeTimer("timer-es-query");
		var status = HttpStatus.OK;

		codetimer.reset();

		var newsHelper = GetNewsClientBySiteID(newsQuery.SiteID);

		NewsBOSR result = new NewsBOSR();
		try {

			estimer.reset();
			var solist = newsHelper.GetNewsListByCategoryID(newsQuery, querytimer, parsetimer);
			estimer.end();

			result.message = solist.message;
			result.total = solist.total;
			result.facetList = solist.faceList;
			if (solist.newsSOList != null && solist.newsSOList.size() > 0) {
				@SuppressWarnings("unchecked")
				List<Integer> lids = new ArrayList(solist.newsSOList.keySet());

				odbtimer1.reset();
				result.newsList = newsHelper.GetListNewsByListID(lids);
				odbtimer1.end();

				if (result.newsList != null && result.newsList.size() > 0) {

					var listEventID = new ArrayList<String>();
					var listTopicID = new ArrayList<String>();
					var lstUserID = new ArrayList<Integer>();
					var lsCustomerID = new ArrayList<Double>();
					var lstCreatedUser = new ArrayList<String>();

					for (var objNews : result.newsList) {
						if (objNews != null) {
							if (!Utils.StringIsEmpty(objNews.EventIDList)) {
								String[] EventIDArr = objNews.EventIDList.split(",");
								if (EventIDArr.length > 0) {
									listEventID.addAll(Arrays.asList(EventIDArr));

								}
							}
							if (!Utils.StringIsEmpty(objNews.HotTopicIDList)) {
								String[] HotTopicIDArr = objNews.HotTopicIDList.split(",");
								if (HotTopicIDArr.length > 0) {
									listTopicID.addAll(Arrays.asList(HotTopicIDArr));

								}
							}
							if (objNews.UserID > 0) {

								lstUserID.add(objNews.UserID);
							} else {
								if (!Utils.StringIsEmpty(objNews.CreatedCustomerID+"")) {
									lsCustomerID.add(Double.valueOf(objNews.CreatedCustomerID));
								}
								if (!Utils.StringIsEmpty(objNews.CreatedUser)) {
									lstCreatedUser.add(objNews.CreatedUser);
								}

							}
						}
					}

					List<NewsEventBO> listNewsEvent = new ArrayList<NewsEventBO>();
					List<NewsBO> listRelatedNews = new ArrayList<NewsBO>();
					List<GameUserBO> listGameUserBO = new ArrayList<GameUserBO>();
					List<GameUserBO> listGameUserBOFromCustomer = new ArrayList<GameUserBO>();
					List<CustomerUserBO> listCustomerUserBO = new ArrayList<CustomerUserBO>();

					odbtimer2.reset();
					if (listEventID.size() > 0) {
						listNewsEvent = newsHelper.GetNewsEventByListID(
								listEventID.stream().distinct().collect(Collectors.toList()).toArray(new String[0]));

					}
					if (listTopicID.size() > 0) {
//						listRelatedNews = newsHelper
//								.GetNewsByListTopicID(listTopicID.stream().distinct().collect(Collectors.toList())
//										.toArray(new String[0]))
//								.stream().filter(x -> x != null && x.ActivedDate.before(new Date()))
//								.collect(Collectors.toList());

					}
					if (lstUserID.size() > 0) {
						listGameUserBO = newsHelper.GetUserByListID(
								lstUserID.stream().distinct().collect(Collectors.toList()).toArray(new Integer[0]));
					}
					if (lsCustomerID.size() > 0 || lstCreatedUser.size() > 0) {
						listGameUserBOFromCustomer = newsHelper.GetListUserByUserIdOrCustomerId(
								lsCustomerID.stream().distinct().collect(Collectors.toList()).toArray(new Double[0]),
								lstCreatedUser.stream().distinct().collect(Collectors.toList()).toArray(new String[0]));
					}

					if (lsCustomerID.size() > 0) {
						listCustomerUserBO = newsHelper.GetCustomerUserByListID(
								lsCustomerID.stream().distinct().collect(Collectors.toList()).toArray(new Double[0]));
					}

					odbtimer2.end();

					for (var objNews : result.newsList) {
						if (objNews != null) {
							if (!Utils.StringIsEmpty(objNews.EventIDList)) {
								String[] EventIDArr = objNews.EventIDList.split(",");
								if (EventIDArr.length > 0) {
									for (var id : EventIDArr) {
										objNews.EventsList.add(
												listNewsEvent.stream().filter(x -> x.Eventid == Integer.valueOf(id))
														.findFirst().orElse(null));
									}

								}
							}


							if (objNews.UserID > 0) {
								var user = listGameUserBO.stream().filter(x -> x.UserID == objNews.UserID).findFirst()
										.orElse(null);
								objNews.GameUser = user;

							} else {

								GameUserBO gameUser = null;

								if (!Utils.StringIsEmpty(objNews.CreatedCustomerID+"")) {
									gameUser = listGameUserBOFromCustomer.stream()
											.filter(x -> !Utils.StringIsEmpty(x.CustomerId)
													&& x.CustomerId.equals(objNews.CreatedCustomerID))
											.findFirst().orElse(null);
									if (gameUser == null) {
										gameUser = listGameUserBOFromCustomer.stream()
												.filter(x -> !Utils.StringIsEmpty(x.Username)
														&& x.Username.equals(objNews.CreatedUser))
												.findFirst().orElse(null);

										if (gameUser == null) {
											var cUser = listCustomerUserBO.stream()
													.filter(x -> !Utils.StringIsEmpty(x.CustomerID)
															&& x.CustomerID.equals(objNews.CreatedCustomerID))
													.findFirst().orElse(null);
											if (cUser != null) {
												gameUser = new GameUserBO();
												gameUser.FullName = cUser.CustomerName;
											}
										}

									}
								}

								objNews.GameUser = gameUser;

							}

						}
					}

				}
			}
			if (newsQuery.SiteID == 2) {
				result.result = result.newsList;
				result.newsList = null;
			}

		} catch (Throwable e) {
			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.message = "Failed: " + e.toString() + ": " + e.getMessage() + " - " + trace;
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			var param = newsHelper.GetJsonFromObject(newsQuery);
			LogHelper.WriteLog(e, LogLevel.ERROR, param);

		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(codetimer, estimer, odbtimer1, odbtimer2, parsetimer, querytimer);
		long endTime = System.currentTimeMillis();
		if (endTime - startTime > 2500) {
			var message = String.format(
					"getnewslistbycategoryid - Total request time: %s ,elastic time %s , orient time %s",
					String.valueOf(endTime - startTime), String.valueOf(estimer.getElapsedTime()),
					String.valueOf(odbtimer1.getElapsedTime() + odbtimer2.getElapsedTime()));

			LogHelper.WriteLogTimer(message, LogLevel.WARNING, request);

		}
		return new ResponseEntity<NewsBOSR>(result, header, status);

	}

	
	@RequestMapping(value = "/getnewsvideobycategoryid", method = RequestMethod.GET)
	public ResponseEntity<NewsBOSR> GetNewsVideoByCategoryID(int categoryID, int siteID, int pageIndex, int pageSize) {
		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es-all");
		var status = HttpStatus.OK;
		codetimer.reset();

		if (siteID <= 0)
			siteID = 1;

		var newsHelper = GetNewsClientBySiteID(siteID);

		NewsBOSR result = new NewsBOSR();
		try {

			estimer.reset();
			result = newsHelper.GetNewsVideoByCategoryID(categoryID, siteID, pageIndex, pageSize);
			estimer.end();
			
		} catch (Throwable e) {
			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.message = "Failed: " + e.toString() + ": " + e.getMessage() + " - " + trace;
			LogHelper.WriteLog(e, LogLevel.ERROR, request);

			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(codetimer, estimer);

		return new ResponseEntity<NewsBOSR>(result, header, status);

	}
	
	@RequestMapping(value = "/getnewsbystore", method = RequestMethod.GET)
	public ResponseEntity<NewsBOSR> GetNewsByStore(int storeID, int siteID, int pageIndex, int pageSize) {
		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es-all");
		var status = HttpStatus.OK;
		codetimer.reset();

//		if (siteID <= 0)
//			siteID = 1;

		var newsHelper = GetNewsClientBySiteID(siteID <= 0 ? 1 : siteID);

		NewsBOSR result = new NewsBOSR();
		try {

			estimer.reset();
			result = newsHelper.GetNewsByStore(storeID, siteID, pageIndex, pageSize);
			estimer.end();
			
		} catch (Throwable e) {
			String trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			result.message = "Failed: " + e.toString() + ": " + e.getMessage() + " - " + trace;
			LogHelper.WriteLog(e, LogLevel.ERROR, request);

			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(codetimer, estimer);

		return new ResponseEntity<NewsBOSR>(result, header, status);

	}

	@GetMapping(value = "/getnewsbycategoryidparent")
	public ResponseEntity<NewsBO[]> getNewsByCategoryidParent(NewSearch search, int siteID, int pageSize){
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsBO[] newsBOSR = null;
		var status = HttpStatus.OK;
		if (siteID < 1) {
			siteID = 2;
		}

		var newsHelper = GetNewsClientBySiteID(siteID);

		try {
			odbtimer.reset();
			if(search != null) {
				newsBOSR = newsHelper.getNewsByCategoryidParent(search, siteID, pageSize);
			}
			odbtimer.end();
			if(DidxHelper.isVu())
				System.out.println("Tong conng :" + newsBOSR.length);
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);
		return new ResponseEntity<NewsBO[]>(newsBOSR, header, status);
	}
	
	@RequestMapping(value = "/getkeywordsuggestnews", method = RequestMethod.GET)
	public ResponseEntity<KeywordSuggest[]> GetkeywordSuggestNews(String keyword, Integer clearcache) {
		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es-all");
		var status = HttpStatus.OK;
		codetimer.reset();
		var newsHelper = GetNewsClientBySiteID(1);
		KeywordSuggest[] result = null;
		try {

			estimer.reset();
			result = newsHelper.GetkeywordSuggestNewsFromCache(keyword,clearcache);
			estimer.end();
			
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(codetimer, estimer);
		return new ResponseEntity<KeywordSuggest[]>(result, header, status);

	}

	@RequestMapping(value = "/addkeywordsuggestfornews", method = RequestMethod.POST)
	public ResponseEntity<ResultBO> addKeywordSuggestForNews(String keyWordSuggest) {

		var codetimer = new CodeTimer("timer-all");
		var estimer = new CodeTimer("timer-es-all");
		var status = HttpStatus.OK;
		codetimer.reset();
		var newsHelper = GetNewsClientBySiteID(1);
		ResultBO result = null;
		try {

			estimer.reset();
			result = newsHelper.addKeywordSuggestForNews(keyWordSuggest);
			estimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();
		var header = HeaderBuilder.buildHeaders(codetimer, estimer);
		return new ResponseEntity<ResultBO>(result, header, status);



	}
	@GetMapping(value = "/getnewsquestionandanswer")
	public ResponseEntity<NewsBO[]> getNewsByQuestionAndAnswer(NewSearch search, int siteID, int pageSize){
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		NewsBO[] newsBOSR = null;
		var status = HttpStatus.OK;
		if (siteID < 1) {
			siteID = 2;
		}

		var newsHelper = GetNewsClientBySiteID(siteID);

		try {
			odbtimer.reset();
			if(search != null) {
				newsBOSR = newsHelper.getNewsByCategoryidParent(search, siteID, pageSize);
			}
			odbtimer.end();
			if(DidxHelper.isVu())
				System.out.println("Tong conng :" + newsBOSR.length);
		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		}
		codetimer.end();

		var header = HeaderBuilder.buildHeaders(odbtimer, codetimer);
		return new ResponseEntity<NewsBO[]>(newsBOSR, header, status);
	}
	
	@GetMapping("/getnewsviewscountbyidlist")
	public ResponseEntity<List<NewsCount>> GetNewsViewsCountByIdList(
			@RequestParam(name = "newsIdList", required = true) String IdList,
			@RequestParam(name = "siteId", defaultValue = "11", required = true) int siteID) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		HttpStatus status = HttpStatus.OK;

		if (Utils.StringIsEmpty(IdList)) {
			return new ResponseEntity<List<NewsCount>>(null, null, status);
		}
		List<NewsCount> result = new LinkedList<NewsCount>();
		var tmp = IdList.split(",");
		var NewsidList = Arrays.stream(tmp).mapToLong(x -> (Long)Long.parseLong(x)).toArray();//collect(Collectors.toList());
		// Check list idViews is null
		if (NewsidList == null) {
			return new ResponseEntity<List<NewsCount>>(null, null, status);
		}
		var newsHelper = GetNewsClientBySiteID(siteID);

		try {
			result = _bhxNewsHelper.GetNewsViewsCountByIdList(NewsidList);
			odbtimer.end();

		} catch (Throwable e) {
			LogHelper.WriteLog(e, LogLevel.ERROR, request);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		codetimer.end();
		HttpHeaders header = HeaderBuilder.buildHeaders(odbtimer, codetimer);
		return new ResponseEntity<List<NewsCount>>(result, header, status);
	}


	@GetMapping("/OffRedisNewcount")
	public ResponseEntity<List<NewsCount>> OffRedisNewcount(int onCP) {
		var codetimer = new CodeTimer("timer-all");
		var odbtimer = new CodeTimer("timer-odb");
		HttpStatus status = HttpStatus.OK;
		var newsHelper = GetNewsClientBySiteID(11);
		_bhxNewsHelper.OffRedisNewcount(onCP);
		HttpHeaders header = HeaderBuilder.buildHeaders(odbtimer, codetimer);
		return new ResponseEntity<List<NewsCount>>(null, header, status);
	}
	
	
}