package mwg.wb.common.notify;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LineNotify {
	public String notifyName;

	public String token;

	public int DataCenter;
	public static Map<String, Long> notifyCache = null;

	public LineNotify() {

	}

	public LineNotify(String name, String token) {
		this.notifyName = name;
		this.token = token;
		if (notifyCache == null)
			notifyCache = new HashMap<String, Long>();
	}
	public void NotifyInfo(String message, int datacenter) {
// 		if (this.DataCenter >= 0) // tam bỏ
// 		return;

 		if (this.DataCenter == 3 || datacenter == 3 || this.DataCenter == 2 || datacenter == 2)
 			return;
		if (this.DataCenter == 3 || datacenter == 3  )
			return;
		message = "DC " + datacenter + " " + message; 
		NotifyMessage(message);

	}
	
	public void NotifyInfo(String message ) {
 		if (this.DataCenter >= 0) // tam bỏ
 		return;
 
		if (this.DataCenter == 3    )
			return;
		message = "DC " + message; 
		NotifyMessage(message);

	}
	public void NotifyError(String message, int datacenter) {
//		if (this.DataCenter >= 0) // tam bỏ
//			return;

//		if (this.DataCenter == 3 || datacenter == 3 || this.DataCenter == 2 || datacenter == 2)
//			return;
		if (this.DataCenter == 3 || datacenter == 3  )
			return;
		message = "DC " + datacenter + " " + message; 
		NotifyMessage(message);

	}
	public void NotifyError(String message ) {
//		if (this.DataCenter >= 0) // tam bỏ
//			return;

//		if (this.DataCenter == 3 || datacenter == 3 || this.DataCenter == 2 || datacenter == 2)
//			return;
		if (this.DataCenter == 3    )
			return;
		message = "DC  " + message; 
		NotifyMessage(message);

	}
	public void NotifyMessage(String message) {

		OkHttpClient client = new OkHttpClient();
		Response response = null;
		MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
		RequestBody body = RequestBody.create(mediaType, "message=" + message);
		Request request = new Request.Builder().url("https://notify-api.line.me/api/notify").post(body)
				.addHeader("authorization", "Bearer " + this.token)
				.addHeader("content-type", "application/x-www-form-urlencoded").addHeader("cache-control", "no-cache")
				.addHeader("postman-token", "d7a8a9e5-e03b-9f6f-eb50-0f372fac21f7").build();

		try {

			client.setWriteTimeout(500, TimeUnit.MILLISECONDS);
			client.setReadTimeout(500, TimeUnit.MILLISECONDS);
			response = client.newCall(request).execute();
		} catch (Throwable e) {
			Logs.LogException(e);
		} finally {
			if (response != null) {
				try {
					response.body().close();
				} catch (final Throwable th) {
					Logs.LogException(th);
				}
			}
		}

	}
//nay chỉ send info, ko phải ERROR, tam bỏ
	public void PushLineNotify(MessageQueue messageQueue, ResultMessage rsCode, String pkName,
			String processClassName) {

		if (this.DataCenter >= 0) // tam bỏ
			return;

		if (this.DataCenter == 3 || this.DataCenter == 2)
			return;

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		var dateString = dateFormat.format(cal.getTime());
		String key = "";
		String message = "";

		if (processClassName.equals("mwg.wb.pkg.product.Product")
				|| processClassName.equals("mwg.wb.pkg.upsert.Upsert")) {
			key = processClassName;
			message = "[SUCCESS] Refreshed " + pkName + " at " + dateString;
		} else {
			key = processClassName + "_" + messageQueue.Identify;
			message = "[SUCCESS] Refreshed " + pkName + " at " + dateString + ", identify: " + messageQueue.Identify;
		}

		var cacheValue = notifyCache.get(key);
		if (cacheValue == null) {
			cacheValue = (long) 0;
			notifyCache.put(key, System.currentTimeMillis());
		}

		if (messageQueue != null && !Utils.StringIsEmpty(messageQueue.Note) && messageQueue.Note.contains("DIDX_TOP")
				&& !Utils.StringIsEmpty(pkName)) {
			if (rsCode.Code == ResultCode.Success) {

				if (System.currentTimeMillis() - cacheValue > 3 * 60 * 1000) {// hon 3 phut
					NotifyMessage(message);
					notifyCache.put(key, System.currentTimeMillis());
				}

			} else {
				NotifyMessage("[FAILED] Refreshed " + pkName + " at " + dateString + ", identify: " + messageQueue.Identify);
				notifyCache.put(key, System.currentTimeMillis());
			}

		}

	}

}
