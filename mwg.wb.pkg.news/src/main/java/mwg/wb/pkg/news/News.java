package mwg.wb.pkg.news;

import mwg.wb.business.NewsHelper;
import mwg.wb.business.webservice.WebserviceHelper;
import mwg.wb.client.cache.IgniteClient;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.webservice.NewsView;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class News implements Ididx {
	
	private ORThreadLocal factoryRead = null;
	private ObjectTransfer objectTransfer = null;
	private ClientConfig config = null;
	NewsHelper newsHelper = null;
	@Override
	public void InitObject(ObjectTransfer aobjectTransfer) {
		
		
		objectTransfer = aobjectTransfer;
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		config = (ClientConfig) objectTransfer.clientConfig;
		newsHelper = new NewsHelper(factoryRead, config);
	}

	public void InitConfig(ClientConfig clientConfig) {

		config = clientConfig;
	}

	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage resultMessage = new ResultMessage();
		resultMessage.Code = ResultCode.Success;
		if (message.ClassName.equals("mwg.wb.pkg.news.NewsEvent")) {
			NewsEvent exec = new NewsEvent();
			exec.InitObject(objectTransfer);
			return exec.Refresh(message);
		} else if (message.ClassName.equals("mwg.wb.pkg.news.NewSE")) {
			NewsSE exec = new NewsSE();
			exec.InitObject(objectTransfer);
			return exec.Refresh(message);
		} else if (message.ClassName.equals("mwg.wb.pkg.news.NewsTopic")) {
			NewsTopic exec = new NewsTopic();
			exec.InitObject(objectTransfer);
			return exec.Refresh(message);
		} else if(message.ClassName.equals("mwg.wb.pkg.news.NewsSuggestKeyword")){
			NewsSuggestKeyword exec = new NewsSuggestKeyword();
			exec.InitObject(objectTransfer);
			return exec.Refresh(message);
		}
		return resultMessage;

	}

	public static HashMap<String, Date> _LogUpdateView7Days = new HashMap<String, Date>();
	public static Date _LogLastUpdate;
	public static int _View7DayLastUpdate = -1;
	public static int _View7DayLastUpdateHour = 0;
	@SuppressWarnings("deprecation")
	@Override
	public ResultMessage RunScheduleTask() {

		////Phần này chỉ chạy 12h đêm.
		//var thisDate = new Date("07/22/2020 00:00:01");
		try {
		//var thisDate = new Date();
			Date dateC = new Date();
 			Calendar calendar = Calendar.getInstance(); // calendar instance
 			calendar.setTime(dateC);
 			int hour = calendar.get(Calendar.HOUR_OF_DAY);
 			int day = calendar.get(Calendar.DAY_OF_WEEK );
 			//hour = 0;
		if(hour == 0 && (_View7DayLastUpdate < day || _View7DayLastUpdate == 7)) {
			// _View7DayLastUpdate tức là ngày cuối tuần thì hôm nay this.getday == 0
			// get list news.
			_View7DayLastUpdate = day;
			var gg = "";
			if(!newsHelper.UpdateResetViewCount2()) {
				Logs.Log(true, "DIDX_LOG|UpdateResetViewCount",Utils.GetCurrentDate()+ ":UpdateResetViewCount false");
				
			}else {
				Logs.Log(true, "DIDX_LOG|UpdateResetViewCount",Utils.GetCurrentDate()+ ":UpdateResetViewCount true, _View7DayLastUpdate:" + _View7DayLastUpdate 
						+ " | day:" + day);
			}
			//5
		}
		
		 
		
			
		}catch (Exception e) {
			// TODO: handle exception
			String msg = Utils.stackTraceToString(e) + " Day:" + _View7DayLastUpdate + " CurrentDate: " + Utils.GetCurrentDate();
			Logs.LogException(msg);
		}
		/// end
		ResultMessage resultMessage = new ResultMessage();
			
		resultMessage.Code = ResultCode.Success;
		Calendar timeMoc = Calendar.getInstance();
		timeMoc.setTime(new Date());
		timeMoc.add(Calendar.HOUR_OF_DAY, -5); // 5 tiếng chạy 1 lần
		var a = timeMoc.getTime();
		
		
		if (_LogLastUpdate == null || _LogLastUpdate.getTime() < timeMoc.getTime().getTime()) { /// timeMoc.after(_LogLastUpdate))
																								/// { // timeMoc >
																								/// _LogLastUpdate
			_LogLastUpdate = new Date();

			try {
				var data = WebserviceHelper.Call(config.DATACENTER).Get("apinews/gettopview7days", NewsView[].class);
				// IgniteClient ignite = new IgniteClient(config);
				if (data != null && data.length > 0) {
					IgniteClient.GetClient(config).SetKey("MWG_News_TopView7D", data);
					Logs.Log(true, "DIDX_LOG|MWG_News_TopView7D", "SetKey(MWG_News_TopView7D:" + data.length);
				} else {
					IgniteClient.GetClient(config).RemoveKey("MWG_News_TopView7D");
					Logs.Log(true, "DIDX_LOG|MWG_News_TopView7D", "RemoveKey(MWG_News_TopView7D)");
				}

			} catch (Throwable e) {
				String msg = Utils.stackTraceToString(e) + "\r\nIGNITE_URL=" + config.IGNITE_URL;
				Logs.LogException(msg);
				resultMessage.Message = e.toString();

			}

		}else {
			//System.out.print("- Waiting for run data. 5 hours/time");
		}

		return resultMessage;
	}
}
