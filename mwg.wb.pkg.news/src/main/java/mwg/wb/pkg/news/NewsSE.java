package mwg.wb.pkg.news;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.business.NewsHelper;
import mwg.wb.business.helper.news.TGDDNewsHelper;
import mwg.wb.client.OracleClient;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.MessageQueuePushType;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.gameapp.GameAppBO;
import mwg.wb.model.news.ListViewCount;
import mwg.wb.model.news.NewsBO;
import mwg.wb.model.news.NewsCategoryBO;
import mwg.wb.model.news.TagSO;
import mwg.wb.model.search.NewsSO;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NewsSE implements Ididx {

	private ORThreadLocal factoryRead = null;
	private String currentIndexDB = "ms_news";
	private String currentNewsTagIndex = "ms_newstags";
	private ClientConfig config = null;
	// public NotifyHelper lineNotify = null;
	
	private ObjectMapper mapper = null;

	
	OracleClient dbclient = null;
	Connection VHcondb = null;
	// public static String[] _LogUpdateView7Days;
	public static HashMap<String, Date> _LogUpdateView7Days = new HashMap<String, Date>();
	public static Date _LogLastUpdate;
	
	NewsHelper newsHelper = null;

	// Helper
	private static TGDDNewsHelper _tgddNewsHelper = null;

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		config = (ClientConfig) objectTransfer.clientConfig;
		// lineNotify = (LineNotify) objectTransfer.notifyHelper;

		newsHelper = new NewsHelper(factoryRead, config);
		
		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatStringNews);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		elasticClient = ElasticClient.getInstance(config.SERVER_ELASTICSEARCH_READ_HOST);
//		clientIndex = elasticClient.getClient();
		
		
	}

	// User ID=tgdd_news;Password=44662288
	private final Lock queueLock = new ReentrantLock();

	@SuppressWarnings("unused")
	@Override
	public ResultMessage Refresh(MessageQueue message) {

		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;

		int NewsID = Integer.parseInt(message.Identify);

		if (message.Action == DataAction.Add || message.Action == DataAction.Update) {
			
			Map<String, Object> listView = new HashMap<>();
			listView.put("ViewCounter7Days1", 0);
			listView.put("ViewCounter7Days2", 0);
			listView.put("ViewCounter7Days3", 0);
			listView.put("ViewCounter7Days4", 0);
			listView.put("ViewCounter7Days5", 0);
			listView.put("ViewCounter7Days6", 0);
			listView.put("ViewCounter7Days7", 0);
			
			//Date dateC = new Date();
			Calendar calendar = Calendar.getInstance();
			//calendar.setTime(dateC);
			int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
			int indexDays = (dayOfYear % 7) + 1;
			
			
			if(message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_VIEWCOUNT_RESETVIEW_7DAY) {
				// chỗ này gọi lúc 0h đêm để reset lại view ngày đó.
				if(NewsID <= 0) {
					r.Code = ResultCode.Success;
					return r;
				}
				
				
				UpdateRequest updateRequest = new UpdateRequest(currentIndexDB, String.valueOf(NewsID))
						.script(new Script(ScriptType.INLINE,
				                "painless",
				                "if(ctx._source.containsKey(\"lstView7Day\") || ctx._source.lstView7Day != null || ctx._source.lstView7Day.length > 0){ \r\n" + 
				                
					               "ctx._source.ViewCounter7Days = ctx._source.lstView7Day.ViewCounter7Days1 + ctx._source.lstView7Day.ViewCounter7Days2 + ctx._source.lstView7Day.ViewCounter7Days3 + ctx._source.lstView7Day.ViewCounter7Days4 + ctx._source.lstView7Day.ViewCounter7Days5 + ctx._source.lstView7Day.ViewCounter7Days6 + ctx._source.lstView7Day.ViewCounter7Days7; " 
					               + "	ctx._source.lstView7Day.ViewCounter7Days"+indexDays+" = 0;" 
				               + "}else{"
				                + "ctx._source.ViewCounter7Days = 0;"
				                + "}"
				                ,Collections.emptyMap()))
						.scriptedUpsert(true).detectNoop(false);
				
				
				var client = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
				
				UpdateResponse response = null;
				try {
					queueLock.lock();
					
					response = client.update(updateRequest, RequestOptions.DEFAULT);
					
				} catch (Throwable e) {
					if(e.toString().contains("document_missing_exception")) {
						r.Code = ResultCode.Success;
						return r;
					}
					
					Logs.LogException("UpdateViewCount7Day0HDem",e);
					r.Message = Logs.GetStacktrace(e);
					r.Code = ResultCode.Retry;  // test nên dùng Success => thật dùng Retry
					
				} finally {
					queueLock.unlock();
				}
				if (response != null && response.getResult() == Result.UPDATED) {
					r.Code = ResultCode.Success;
				} else {
					
					r.Message = "-Index viewcounter to ES FAILED: " + NewsID + ", " + response
							+ "######################";
					r.Code = ResultCode.Retry; // test nên dùng Success => thật dùng Retry
				}
				
				
				
				r.Code = ResultCode.Success;
				return r; 
				
			}else if (message.Type == MessageQueuePushType.REQUIRE_PUSH_RUN_UPDATE_VIEWCOUNT) {
				
				if (config.IS_NOT_UPDATE_ES ==1) {
					r.Code = ResultCode.Success;
					return r;
				}
				long ViewCounter = Utils.toLong(message.Data);
				if (ViewCounter > 0) {

										
					HashMap<String, Object> scriptParams = new HashMap<>();
					try {
						scriptParams.put("ViewCounter", ViewCounter);
						scriptParams.put("lstView7Day", listView);//mapper.writeValueAsString()
					}catch (Throwable e) {
						// TODO: handle exception
						Logs.LogException("test_view7d_",e);
						//System.out.println("mapper.writeValueAsString");
					}
					UpdateRequest updateRequest = new UpdateRequest(currentIndexDB, String.valueOf(NewsID))
							.script(new Script(ScriptType.INLINE,
					                "painless",
					                //"if(ctx._source.RelatedCategory.contains(\"G2002G\")){"
					                "if(!ctx._source.containsKey(\"lastViewCount\") || ctx._source.lastViewCount == null || ctx._source.lastViewCount == 0){"
					                + "ctx._source.lastViewCount = params.ViewCounter;"
					                + "}"
					                + "if(!ctx._source.containsKey(\"lstView7Day\") || ctx._source.lstView7Day == null || ctx._source.lstView7Day.length == 0){ \r\n" + 
					                "	ctx._source.lstView7Day = params.lstView7Day;\r\n" + 
					                "}else{\r\n" + 
						                "if(ctx._source.lastViewCount == 0){"+
						                "	ctx._source.lstView7Day.ViewCounter7Days"+indexDays+" += 1;\r\n" + 
						                "}else{"
						                +"	ctx._source.lstView7Day.ViewCounter7Days"+indexDays+" += params.ViewCounter - ctx._source.lastViewCount;\r\n"
						                + "}" +
					                "}\r\n" +
					                //"}" + 
					                "ctx._source.ViewCounter = params.ViewCounter; "+
					                "ctx._source.lastViewCount = params.ViewCounter; "
					                //+ "ctx._source.ViewCounter7Days = ctx._source.lstView7Day.ViewCounter7Days1 + ctx._source.lstView7Day.ViewCounter7Days2 + ctx._source.lstView7Day.ViewCounter7Days3 + ctx._source.lstView7Day.ViewCounter7Days4 + ctx._source.lstView7Day.ViewCounter7Days5 + ctx._source.lstView7Day.ViewCounter7Days6 + ctx._source.lstView7Day.ViewCounter7Days7; " 
					                ,scriptParams))
							.scriptedUpsert(true).detectNoop(false);

					//ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).UpdateFieldOBject(indexDB, esKeyTerm, field, solist)

//					var update = new UpdateRequest(currentIndexDB, String.valueOf(NewsID))
//							.doc("{\"ViewCounter\":" + ViewCounter + " }", XContentType.JSON).docAsUpsert(true)
//							.detectNoop(false);
					var client = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
					
					UpdateResponse response = null;
					try {
						queueLock.lock();
						
						response = client.update(updateRequest, RequestOptions.DEFAULT);
						
						
					} catch (Throwable e) {
						if(e.toString().contains("document_missing_exception")) {
							r.Code = ResultCode.Success;
							return r;
						}
						if(e.toString().contains("version_conflict")) {
							r.Code = ResultCode.Success;
							return r;
						}
						try {
							Logs.LogException("UpdateViewCountAndViewOfDay newID " + mapper.writeValueAsString(message) ,e);
						} catch (JsonProcessingException e1) {
							Logs.LogException("UpdateViewCountAndViewOfDay newID " + message.Identify + " | " + message.SiteID ,e);
						}
						r.Message = Logs.GetStacktrace(e);
						r.Code = ResultCode.Retry;  // test nên dùng Success => thật dùng Retry
						
						if(r.Message.contains("version_conflict")) {
							r.Code = ResultCode.Success;
							
						}
					} finally {
						queueLock.unlock();
					}
					if (response != null && response.getResult() == Result.UPDATED) {
						r.Code = ResultCode.Success;
					} else {
						
						r.Message = "-Index viewcounter to ES FAILED: " + NewsID + ", " + response
								+ "######################";
						r.Code = ResultCode.Retry; // test nên dùng Success => thật dùng Retry
					}
				} else {

					return r;
				}
				
				
			}else if (message.Type == 111) {
				if (config.IS_NOT_UPDATE_ES == 1) {
					r.Code = ResultCode.Success;
					return r;
				}
				long ViewCounter = Utils.toLong(message.Data);
				if (ViewCounter > 0) {

					var update = new UpdateRequest(currentIndexDB, String.valueOf(NewsID))
							.doc("{\"ViewCounter\":" + ViewCounter + " }", XContentType.JSON).docAsUpsert(true)
							.detectNoop(false);
					var client = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
					UpdateResponse response = null;
					try {
						queueLock.lock();
						response = client.update(update,RequestOptions.DEFAULT);

					} catch (Throwable e) {
						Logs.LogException("updateViewCounter",e);
						r.Message = Logs.GetStacktrace(e);
						r.Code = ResultCode.Retry;
					} finally {
						queueLock.unlock();
					}
					if (response != null && response.getResult() == Result.UPDATED) {
						r.Code = ResultCode.Success;
					} else {

						r.Message = "-Index viewcounter to ES FAILED: " + NewsID + ", " + response
								+ "######################";
						r.Code = ResultCode.Retry;
					}
				} else {

					return r;
				}

			} else {
				NewsBO[] newsList = null;
				try {
					newsList = factoryRead.QueryFunction("news_GetByID", NewsBO[].class, false, NewsID);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					Logs.LogException(e);
				}
				if (newsList == null || newsList.length == 0) {
					r.Code = ResultCode.Success;
					r.Message = "News #" + NewsID + " does not exist";
					return r;
				}
				NewsBO news = newsList[0];
//				if (news.IsActived == 0 && news.CategoryID != 1123) {
//					DeleteNews(NewsID, r);
//					return r;
//				}

				if (!Utils.StringIsEmpty(news.ListCategoryID)) {
					if (news.ListCategoryID.contains("1708")) {
						if (news.IsActived == 0 && news.IsAdminAnswer == 0) {
							r.Code = ResultCode.Success;
							return r;
						}

					}
					// tien xu ly listcate
					if (news.ListCategoryID.contains("31")) // promotion
					{
						if (news.ListCategoryID.contains("999")) {
							news.ListCategoryID = news.ListCategoryID.replace("999", "");
						}
					} else {
						if (!Utils.StringIsEmpty(news.ListTreeCategoryID)) {
							if (!news.ListCategoryID.contains("1109") && news.ListTreeCategoryID.contains("1109")) {
								news.ListCategoryID += "*1109";
							}
							if (!news.ListCategoryID.contains("999")
									&& (news.ListTreeCategoryID + "").contains("999")) {
								news.ListCategoryID += "*999";
							}
						}
					}
				}

				try {
					IndexNews(news, r);

//					Logs.LogFile("vanhanhlognews", "newsid: " + news.NewsID + " - ");
				} catch (Throwable e) {
					Logs.LogException("IndexNews",e);
					e.printStackTrace();
					r.Code = ResultCode.Retry;
					return r;
				}

			}
		}

		return r;

	}



	private void IndexNews(NewsBO news, ResultMessage r) throws Throwable {
		var newsSO = new NewsSO();

		newsSO.NewsID = news.NewsID;

		if (!Utils.StringIsEmpty(news.ListCategoryID)) {
			List<String> cateKeys = new ArrayList<String>();
			var arr = news.ListCategoryID.split("\\*\\s");// remove space
			var listNewsCate = new ArrayList<NewsCategoryBO>();
			for (var item : arr) {
				if (Utils.StringIsEmpty(item))
					continue;
				// get newscate byid
				var newsCate = factoryRead.QueryFunction("news_GetNewsCateByID", NewsCategoryBO[].class, false,
						item.trim());
				if (newsCate != null && newsCate.length > 0) {
					listNewsCate.add(newsCate[0]);
				}

			}

			/* news gameapp */

			if (!Utils.StringIsEmpty(news.ProductIDList)) {
				var gameapp = factoryRead.QueryFunction("gameapp_getProductCateByID", GameAppBO[].class, false,
						news.ProductIDList.split(","));

				if (gameapp != null && gameapp.length > 0) {
					String catelist = Stream.of(gameapp).filter(x -> x.ProductLanguageBO != null)
							.filter(x -> x.ProductLanguageBO.CategoryIDList != null)
							.map(x -> x.ProductLanguageBO.CategoryIDList).collect(Collectors.joining(" "));
					newsSO.ProductCategoryIDList = !Utils.StringIsEmpty(catelist) ? catelist.replace(",", " ") : "";
				}

			}

			String gCate = "";
			if (listNewsCate != null && listNewsCate.size() > 0) {
				for (var item : listNewsCate) {
					if (item != null && item.CategoryID > 0 && !StringUtils.isEmpty(item.NodeTree)) {
						gCate += " G" + StringUtils.strip(item.NodeTree, ",").replace(",", "G G") + "G ";
					}

				}
			}
			newsSO.RelatedCategory = gCate;
			newsSO.IsGame = news.IsGameApp;

			String gListCate = "";
			if (!StringUtils.isEmpty(news.ListTreeCategoryID))
				gListCate = " G" + StringUtils.strip(news.ListTreeCategoryID, ",").replace(",", "G G") + "G ";
			newsSO.ListTreeCategory = gListCate;
		}

		String keyword = news.Title;
		if (!Utils.StringIsEmpty(news.ListCategoryID)) {
			if (news.ListCategoryID.contains("2002")) { // news game app

				if (!Utils.StringIsEmpty(news.KeyWord))
					keyword += " , " + news.KeyWord;
				keyword = DidxHelper.FormatKeywordField(keyword);
				newsSO.Keyword = DidxHelper.FilterVietkey(keyword);
				newsSO.SEContent = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(news.Content));
				newsSO.SETag = Utils.StringIsEmpty(news.KeyWord) ? ""
						: DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(news.KeyWord));
				newsSO.SETIitle = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(news.Title));
				newsSO.SETIitleVN = DidxHelper.FormatKeywordField(news.Title);

//				newsSO.ListGameApp = news.ListGameApp;
//				newsSO.ListPlatFormGameApp = news.ListPlatFormGameApp;

			} else if (news.ListCategoryID.contains("1822"))// thuoc
			{
				newsSO.SEContent = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(news.Content));
				newsSO.SETag = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(news.KeyWord));
				newsSO.SETIitle = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(news.Title));
				newsSO.SETIitleVN = DidxHelper.FormatKeywordField(news.Title);
				if (!Utils.StringIsEmpty(news.KeyWord))
					keyword += " , " + news.KeyWord;
				keyword = DidxHelper.FormatKeywordField(keyword);
				newsSO.Keyword = DidxHelper.FilterVietkey(keyword);
			} else if (news.ListCategoryID.contains("1733"))// BHX
			{
				newsSO.SEContent = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(news.Content));
				newsSO.SETag = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(news.KeyWord));
				newsSO.SETIitle = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(news.Title));
				newsSO.SETIitleVN = DidxHelper.FormatKeywordField(news.Title);
				if (!Utils.StringIsEmpty(news.KeyWord))
					keyword += " , " + news.KeyWord;
				keyword = DidxHelper.FormatKeywordField(keyword);
				newsSO.Keyword = DidxHelper.FilterVietkey(keyword);
			} else if (news.ListCategoryID.contains("1708"))// tincong dong cu
			{
				keyword = DidxHelper.FormatKeywordField(news.Content);
				newsSO.Keyword = DidxHelper.FilterVietkey(keyword);
				newsSO.SEContent = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(news.Content));
				newsSO.SETag = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(news.KeyWord));
				newsSO.SETIitle = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(news.Title));
				newsSO.SETIitleVN = DidxHelper.FormatKeywordField(news.Title);
			} else {
				if (!Utils.StringIsEmpty(news.KeyWord))
					keyword += " , " + news.KeyWord;
				keyword = DidxHelper.FormatKeywordField(keyword);
				newsSO.Keyword = DidxHelper.FilterVietkey(keyword);
				newsSO.SEContent = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(news.Content));
				newsSO.SETag = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(news.KeyWord));
				newsSO.SETIitle = DidxHelper.FilterVietkey(DidxHelper.FormatKeywordField(news.Title));
				newsSO.SETIitleVN = DidxHelper.FormatKeywordField(news.Title);
			}
		}

		// newsSO.ProductIDList = news.ProductIDList;
		newsSO.EventIDList = news.EventIDList;
//		newsSO.GameUser = news.GameUser;
		newsSO.PostType = news.PostType;

		if (!Utils.StringIsEmpty(news.ListCategoryID) && news.ListCategoryID.contains("1822")) {
			newsSO.KeywordTerm = DidxHelper.ConvertToTagsTerm(news.KeyWord + ", " + news.Tags);
		} else {
			newsSO.KeywordTerm = Utils.StringIsEmpty(news.KeyWord) ? "" : DidxHelper.ConvertToTagsTerm(news.KeyWord);
		}

		newsSO.TagTerm = DidxHelper.ConvertToTagsTerm(news.Tags);
		IndexDataTags(news.Tags);
		newsSO.KeywordTermVN = DidxHelper.ConvertToTagsTerm(news.KeyWord);
		if (!Utils.StringIsEmpty(news.KeyWord)) {
			try {
				String SeTags = DidxHelper.FormatKeywordField(news.KeyWord);
				newsSO.SeTags = DidxHelper.FilterVietkey(SeTags);
			} catch (Throwable ex) {
				Logs.LogException("#1 IndexNews set keyword: ", ex);
				Logs.WriteLine("Exception when analyze SeTags for news: " + ex);
			}

		}
		newsSO.IsActived = news.IsActived;
		newsSO.IsDeleted = news.IsDeleted;
		newsSO.ViewCounter = news.ViewCounter;
		newsSO.LikeCounter = news.LikeCounter;
		newsSO.DisLikeCount = news.DisLikeCount;
		String rltNews = Utils.StringIsEmpty(news.NewsRelatedNewsId) ? ""
				: ("G" + news.NewsRelatedNewsId.replace(",", "G G") + "G");
		newsSO.RelatedNews = rltNews;
		String rltHTP = Utils.StringIsEmpty(news.NewsHotTopicRelated) ? ""
				: ("G" + news.NewsHotTopicRelated.replace(",", "G G") + "G");
		newsSO.RelatedHotTopic = rltHTP;
		if (!Utils.StringIsEmpty(news.ProductIDList)) {
			newsSO.TermProductID = news.ProductIDList.replace(",", " ");
			var _productIDList = news.ProductIDList.split(" |,");
			for (var item : _productIDList) {
//                new NewProduct().Refresh(new MessageQueue()
//                {
//                    Action = DataAction.Update,
//                    ClassName = AsmClass.Didx_News_NewProduct,
//                    Identify = item,
//                    SiteID = 1
//                });
			}
		} else {
			newsSO.TermProductID = "";
		}

		if (!Utils.StringIsEmpty(news.EventIDList)) {
			newsSO.TermEventsID = news.EventIDList.replace(",", " ");

		} else {
			newsSO.TermEventsID = "";

		}
		newsSO.RelateProductID = String.valueOf(news.RelateProductID);

		if (!Utils.StringIsEmpty(news.HotTopicIDList)) {
			newsSO.TermTopicID = news.HotTopicIDList.replace(",", " ");

		} else {
			newsSO.TermTopicID = "";

		}

		if (news.CreatedDate != null) {
			newsSO.CreatedDate = news.CreatedDate;
		}
		if (news.UpdatedDate != null) {
			newsSO.UpdatedDate = news.UpdatedDate;
		}
		newsSO.DisplayOrder = news.DisplayOrder;
		Date activedDate = new Date();
		if (news.ActivedDate == null) {
			if (news.UpdatedDate == null)
				activedDate = news.CreatedDate;
			else
				activedDate = news.UpdatedDate;
		} else {
			activedDate = news.ActivedDate;
		}
		newsSO.PostType = news.PostType;
		newsSO.CustomerID = news.CustomerID;
		newsSO.UserID = news.UserID;
		newsSO.StoreID = news.StoreID;
		newsSO.CommentCount = news.AmountComment;
		newsSO.ActivedDate = activedDate;
		newsSO.SiteID = news.SiteID;
		newsSO.CreatedCustomerID = news.CreatedCustomerID + "";

		if (!Utils.StringIsEmpty(news.PlatformIDList)) {
			newsSO.PlatformIDList = String.join(" ", news.PlatformIDList.split(","));
		}
		if (!Utils.StringIsEmpty(news.ProductIDList)) {
			var s = news.ProductIDList.trim().split(",");
			newsSO.ProductIDList = String.join(" ", s);
			newsSO.CountProductIDList = s.length;
		}else {
			newsSO.CountProductIDList = 0;
		}
		
		//var newse = GetViewNews(news.NewsID);
		var newse = newsHelper.GetViewNewsES(news.NewsID);
		if(newse != null) {
			if(newse.lstView7Day == null) {
				newsSO.lstView7Day = new ListViewCount();
			}else {
				newsSO.lstView7Day = newse.lstView7Day;
			}
		}else {
			// newsSO.listView7Day = new ListViewCount();
		}
		
		
		//vh 01/01/2021
		if(news.PostType == 2 && news.IsFeature) {
			newsSO.IsNewsVideo = 1;
		}else {
			newsSO.IsNewsVideo = 0;
		}
		newsSO.IsFeature = news.IsFeature ? 1 : 0;
		
		
		// !Utils.StringIsEmpty(gameapp.ProductLanguageBO.PlatformIDList) ?
		// String.join(" ", gameapp.ProductLanguageBO.PlatformIDList.split(",")) : "";
		
		try {
			var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).IndexObject(currentIndexDB,
					newsSO, newsSO.NewsID + "");
			
			if (!rs) {
				r.Message = "-Index news to ES FAILED: " + news.NewsID + " ######################";
				r.Code = ResultCode.Retry;
			}

		} catch (Throwable e) {
			Logs.LogException("IndexNews ES failed",e);
			r.Message = "Exception index news to ES FAILED: " + news.NewsID;
			r.StackTrace = Logs.GetStacktrace(e);
			r.Code = ResultCode.Retry;
		}

	}
	
	int sum(int[] ar) {
		int sum = 0;

		for (int i : ar)
		    sum += i;
		return sum;
	}
	
	boolean IndexDataTags(String arraystr) {
		try {
			if (Utils.StringIsEmpty(arraystr))
				return true;
			String[] KeywordA = arraystr.split(",");
			for (var mKeyword : KeywordA) {
				if (Utils.StringIsEmpty(mKeyword))
					continue;
				String Keyword = mKeyword.trim();
				String keyword_kd = DidxHelper.FormatKeywordField(DidxHelper.FilterVietkey(Keyword));
				String term = DidxHelper.GenTermVN(Keyword);

				TagSO data = new TagSO();
				data.Term = term;
				data.Keyword = keyword_kd;
				data.Name = Keyword;

				ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).IndexObject(currentNewsTagIndex,
						data, term);
				
			}
		} catch (Throwable e) {
			Logs.LogException("IndexDataTags",e);
		
		}

		return true;
	}
	
//	NewsSO GetViewNews(int newsID) {
//		try {
//			var sb = new SearchSourceBuilder();
//			var q = boolQuery();
//			q.must(termQuery("NewsID", newsID));
//			sb.from(0).size(1).query(q);
//			
//			
//			var searchRequest = new SearchRequest(currentIndexDB);
//			searchRequest.source(sb);
//			var queryResults = clientIndex.search(searchRequest, RequestOptions.DEFAULT);
//			
//			List<NewsSO> lsnews = Stream.of(queryResults.getHits().getHits()).map(x -> {
//				try {
//					return mapper.readValue(x.getSourceAsString(), NewsSO.class);
//				} catch (IOException e1) {
//					return null;
//				}
//			}).filter(x -> x != null).collect(Collectors.toList());
//			
//			if(lsnews == null || lsnews.size() == 0) return null;
//			
//			return lsnews.stream().findFirst().orElse(null);
//		}catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
	@Override
	public ResultMessage RunScheduleTask() {
		// move qua class /news.java
		return null;
	}

}
