package mwg.wb.business;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.GConfig;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.crm.CRMSpecialSale;
import mwg.wb.model.crm.CRMStoreDistance;

public class CrmHelper {
	private ORThreadLocal oclient = null;

	private ObjectMapper mapper = null;
	ClientConfig config = null;

	public CrmHelper(ClientConfig aconfig) {

		oclient = APIOrientClient.GetOrientClient(aconfig);
		mapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat(GConfig.DateFormatString);
		mapper.setDateFormat(df);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public CRMSpecialSale[] getSpecialSaleProgram(String productIDs, String provinceIDs) throws Throwable {
		return oclient.queryFunction("crm_getProductLstStoreByProductweb", CRMSpecialSale[].class,
				productIDs, provinceIDs);
	}

	public CRMSpecialSale[] getSpecialSaleProgramByCode(String productCode, String provinceIDs) throws Throwable {
		return oclient.queryFunction("crm_getProductLstStoreByProductCode", CRMSpecialSale[].class,
				productCode, provinceIDs);
	}

	public CRMStoreDistance[] getDistanceByMaingroupID(int maingroupID, int wardID) throws Throwable {
		return oclient.queryFunction("store_getDistanceByMaingroup", CRMStoreDistance[].class, maingroupID,
				wardID);
	}
}
