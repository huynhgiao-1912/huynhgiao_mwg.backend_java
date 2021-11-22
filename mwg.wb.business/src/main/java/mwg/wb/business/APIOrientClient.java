package mwg.wb.business;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;

import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.DidxHelper;
import mwg.wb.model.api.ClientConfig;
 

public class APIOrientClient {
	private static ORThreadLocal oclient;

	public static synchronized ORThreadLocal GetOrientClient(ClientConfig aconfig) {

		if (oclient == null) {

		
			try {
				oclient = new ORThreadLocal();
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			oclient.initReadAPI(aconfig, 0);

		}
		return oclient;
	}
}
