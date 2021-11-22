package mwg.wb.webapi.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.http.HttpHeaders;

import mwg.wb.model.common.HeaderBO;

public class FilterService {
	static InetAddress server;

	public FilterService() {
		// TODO Auto-generated constructor stub
		try {
			server = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			server = null;
		}
	}

	public HttpHeaders GetResponseHeader(HeaderBO header) {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("es-time", (header.ESEndTime - header.ESStartTime) + " ms");
		responseHeaders.set("odb-time", (header.OrientDBEndTime - header.OrientDBStartTime) + " ms");
		responseHeaders.set("code-time", (header.CodeEndTime - header.CodeStartTime) + " ms");
		var hostname = server.getHostName();
		String codeServer = "unknow";
		if (server != null) {
			try {
				var ip = server.toString().split("/")[1];
				var lastIP = ip.split("\\.")[3];
				codeServer = "sv" + lastIP;

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}

		// responseHeaders.set("code-server", codeServer);
		responseHeaders.set("code-server", server + " AND " + hostname);
		return responseHeaders;
	}

}
