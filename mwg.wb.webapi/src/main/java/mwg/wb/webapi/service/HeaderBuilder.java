package mwg.wb.webapi.service;

import mwg.wb.client.service.CodeTimer;
import mwg.wb.client.service.CodeTimers;
import org.springframework.http.HttpHeaders;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HeaderBuilder {
	public static HttpHeaders buildHeaders(Throwable error, CodeTimer... timers) {
		InetAddress server = null;
		String hostname = "";
		try {
			server = InetAddress.getLocalHost();
			hostname = server.getHostName();
		} catch (UnknownHostException ignored) {
//			e.printStackTrace();
		}
		var headers = new HttpHeaders();
		if (timers != null) {
			for (var timer : timers)
				headers.set(timer.getName(), timer.getElapsedTime() + "ms");
		}
		if (error != null) {
			headers.set("apiIsError", "true");
			headers.set("apiErrorMsg", error.toString() + ": " + error.getMessage());
//			headers.set("apiErrorStack", DidxHelper.getStackTrace(error));
		}
		headers.set("code-server", server + " AND " + hostname);
		return headers;
	}

	public static HttpHeaders buildHeaders(CodeTimer... timers) {
		return buildHeaders(null, timers);
	}

	public static HttpHeaders buildHeaders(CodeTimers timers) {
		return buildHeaders(timers.getTimers());
	}

	public static HttpHeaders buildHeaders(Throwable error, CodeTimers timers) {
		return buildHeaders(error, timers.getTimers());
	}

	public static HttpHeaders buildHeaders(Throwable error) {
		return buildHeaders(error, (CodeTimer[]) null);
	}
}
