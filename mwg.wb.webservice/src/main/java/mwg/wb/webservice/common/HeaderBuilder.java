package mwg.wb.webservice.common;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.http.HttpHeaders;

import mwg.wb.client.service.CodeTimer;

public class HeaderBuilder {
	public static HttpHeaders buildHeaders(CodeTimer... timers) {
		if (timers == null)
			return null;
		InetAddress server = null;
		String hostname = "";
		try {
			server = InetAddress.getLocalHost();
			hostname = server.getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		var headers = new HttpHeaders();
		for (var timer : timers)
			headers.set(timer.getName(), timer.getElapsedTime() + "ms");
		headers.set("code-server", server + " AND " + hostname);
		return headers;
	}
}
