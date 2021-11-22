package mwg.wb.business;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.GConfig;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.html.InfoBO;
import mwg.wb.model.system.SystemConfigBO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SystemHelper  {
	private ORThreadLocal oclient = null;
	private ObjectMapper mapper = null;
	ClientConfig config =null;
	public SystemHelper(ClientConfig aconfig) {

		oclient = APIOrientClient.GetOrientClient(aconfig ); 
		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
 
	public InfoBO getHTMLInfo(int htmlID, int siteID, String langID) throws Throwable {
		var info = oclient.QueryFunctionCached("html_info_GetByHtmlID", InfoBO[].class, false,
				htmlID, langID, siteID);
		return info != null && info.length > 0 ? info[0] : null;
	}

	public SystemConfigBO[] getListSystemConfig() throws Throwable {
		var result = oclient.QueryFunctionCached("system_config_GetAll",SystemConfigBO[].class,
				false);
		return result != null && result.length> 0 ? result : null;
//		var data = WebserviceHelper.Call(config.DATACENTER).Get("apisystem/getallsystemconfig", SystemConfigBO[].class);
//		return data;
	}
}
