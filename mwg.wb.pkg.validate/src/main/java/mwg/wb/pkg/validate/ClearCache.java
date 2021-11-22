package mwg.wb.pkg.validate;

import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.business.ProductHelper;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.Utils;
import mwg.wb.common.notify.LineNotify;
import mwg.wb.model.api.ClientConfig;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;

public class ClearCache implements Ididx {

	private ProductHelper productHelper = null;
	private ObjectMapper mapper = null;
	private ClientConfig clientConfig = null;
	LineNotify notifyHelperLog = null;

	public ClearCache() {

	}

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {

		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		productHelper = (ProductHelper) objectTransfer.productHelper;
		mapper = (ObjectMapper) objectTransfer.mapper;
		notifyHelperLog = (LineNotify) objectTransfer.notifyHelperLog;
	}

	public static String openConnection(String baseurl, String url) throws IOException {

		URL obj = new URL(url);
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) obj.openConnection();
			conn.setConnectTimeout(1000);
			conn.setReadTimeout(1000);
			conn.setInstanceFollowRedirects(false); // Make the logic below easier to detect redirections
			conn.setRequestProperty("User-Agent", "Mozilla/5.0...");

			switch (conn.getResponseCode()) {
			case HttpURLConnection.HTTP_MOVED_PERM:
			case HttpURLConnection.HTTP_MOVED_TEMP:
				String location = conn.getHeaderField("Location");
				if (!Utils.StringIsEmpty(location)) {
					location = baseurl + URLDecoder.decode(location, "UTF-8");
					return location;
				}
			}

		} catch (Throwable e) {
			Logs.LogException(e);
		} finally {
			conn.disconnect();
		}

		return "";

	}

	public ResultMessage Refresh(MessageQueue message) {
		// DataCenter = message.DataCenter;
		ResultMessage resultMessage = new ResultMessage();
		resultMessage.Code = ResultCode.Success;
		// clearcahe bên web
		// A Nam yêu cầu 20210412
		
		String strNOTE = message.Note + ""; 
		boolean isLog = Utils.IsMessageSaveLog(message.Note);
	 
		long pid = Utils.toLong(message.Identify);
		if ((message.SiteID == 1 || message.SiteID == 2) && pid > 0) {
			try {
				var strcode = "";
				if(!Utils.StringIsEmpty(message.RefIdentify)){
					strcode = "&Productcode=" + message.RefIdentify;
				}
				var url = new URL("https://www.thegioididong.com/aj/common/UpdateProduct?ProductId="
						+ pid + "&Proviceid=3&Siteid=" + message.SiteID + strcode).openConnection();
				
				
			 
				
				
				url.setReadTimeout(500);
				url.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
				url.connect();
				url.getInputStream().close();

//				Logs.LogFile("ClearCache.txt","https://www.thegioididong.com/aj/common/UpdateProduct?ProductId="
//						+ pid + "&Proviceid=3&Siteid=" + message.SiteID + strcode + " - message.RefIdentify: " + message.RefIdentify + " \n");
			} catch (SocketTimeoutException ignored) {
			} catch (Exception e) {
				Logs.LogException(e);
			}
		}
		return resultMessage;
		
//		String note = message.Note;
////
//		boolean isLog = Utils.IsMessageSaveLog(message.Note);
//		int siteid = message.SiteID;
//		long pid = Utils.toLong(message.Identify);
//		WebClient webClient = new WebClient();
//		if (message.Note.equals("DIDX_RENOTI")) {
//			return resultMessage;
//		}
//		if (!message.Note.contains("MSGBUS_ERPNOTIFY_RCV") && !message.Note.contains("DATA-REINIT")) {
//			return resultMessage;
//		}
//		try {
//			if (pid > 0) {
//				String msg = "";
//				String Url = "";
//				boolean sendOK = false;
//				if (siteid == 1) {
//					Url = openConnection("https://www.thegioididong.com", "https://www.thegioididong.com/sp-" + pid);// ?clearcache=1
//				}
//				if (siteid == 2) {
//					Url = openConnection("https://www.dienmayxanh.com", "https://www.dienmayxanh.com/sp-" + pid);// ?clearcache=1
//				}
//				if (siteid == 6) {
//					Url = openConnection("https://www.bluetronics.com", "https://www.bluetronics.com/sp-" + pid);// ?clearcache=1
//				}
//				if (!Utils.StringIsEmpty(Url)) {
//
//					boolean da = webClient.DownloadString2(Url + "?clearcache=1");
//					if (da) {
//						if (isLog) {
//							sendOK = true;
//							msg = "clearcache:" + pid + "|_" + Url + "?clearcache=1";
//						}
//					} else {
//						if (isLog) {
//							msg = "clearcache:" + pid + "|khong chay dc clear:" + Url;
//						}
//					}
//				} else {
//					if (isLog) {
//						msg = "clearcache:" + pid + "|khong co location";
//					}
//				}
//
//				if (sendOK) {
//					Logs.LogFactoryMessage("clearcache" + siteid,
//							Utils.GetCurrentDate() + "|" + pid + "|" + Url + "?clearcache=1|", siteid);
//					if (isLog) {
//						notifyHelperLog.Notify(msg, 0);
//
//					}
//				}
//			}
//		} catch (Throwable e) {
//			// resultMessage.Code = ResultCode.Retry;
//			// resultMessage.Message = e.getMessage();
//			// return resultMessage;
//			Logs.LogException(e);
//		}
//
//		return resultMessage;

	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}
}
