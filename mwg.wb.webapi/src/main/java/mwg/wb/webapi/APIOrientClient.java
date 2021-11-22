package mwg.wb.webapi;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;

import mwg.wb.business.DataCenterHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.webapi.service.ConfigUtils;

public class APIOrientClient {
	private static ORThreadLocal oclient;

	public static synchronized ORThreadLocal GetOrientClient() {

		if (oclient == null) {

			ClientConfig config = ConfigUtils.GetOnlineClientConfig() ;
			try {
				oclient = new ORThreadLocal();
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			oclient.initReadAPI(config, 0);

		}
		return oclient;
	}
}
