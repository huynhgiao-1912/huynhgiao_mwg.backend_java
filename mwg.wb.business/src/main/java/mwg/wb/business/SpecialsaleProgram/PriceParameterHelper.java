package mwg.wb.business.SpecialsaleProgram;

import mwg.wb.business.LogHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.PriceParamsBO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PriceParameterHelper {
	private static ORThreadLocal oclient = null;

	 	public static Map<Integer, PriceParamsBO> list_PriceParamsBO = new ConcurrentHashMap<Integer, PriceParamsBO>();

	private static PriceParameterHelper instance;
	private static long Expire=System.currentTimeMillis()+15*60*1000;//15 phut
	public static PriceParameterHelper getInstance(ORThreadLocal afactoryRead, ClientConfig aconfig) {

		if (instance == null) {
			synchronized (PriceParameterHelper.class) {
				instance = new PriceParameterHelper(afactoryRead, aconfig);
			}

		}

		return instance;
	}

	public PriceParameterHelper(ORThreadLocal afactoryRead, ClientConfig aconfig) {
		oclient = afactoryRead;
		try {
			LoadAll("");
		} catch (Throwable e) {
			LogHelper.WriteLog(e);
		}
	}

	public List<PriceParamsBO> GetPriceParams (int maingroupid  ) {
		long cDate = System.currentTimeMillis();
		
		if(Expire<cDate) {
			Expire=System.currentTimeMillis()+15*60*1000;
			try {
				 
					LoadAll("");
				 
			} catch (Throwable e) {
				LogHelper.WriteLog(e);
			}
		}
		
		 	 
//		List<PriceParamsBO> rsList = new ArrayList<PriceParamsBO>();

		
//		for (Entry<Integer, PriceParamsBO> entry : list_PriceParamsBO.entrySet()) {
//			var item = entry.getValue();
//			if (item.maingroupid == maingroupid ) { 
//				rsList.add(item ); 
//			}
//		} 
//		list_PriceParamsBO.values().
//		Iterator<PriceParamsBO> iterator = list_PriceParamsBO.values().iterator();
//		while(iterator.hasNext()) {
//			var item = iterator.next();
//			if (item.maingroupid != maingroupid ) {
////				rsList.add(item );
//				iterator.remove();
//			}
//
//		}
		return list_PriceParamsBO.values().stream().filter(x -> x.maingroupid == maingroupid)
				.collect(Collectors.toList());
	}

	public synchronized void LoadAll(String table) throws Throwable {
		 
		if (Utils.StringIsEmpty(table) || table.equals("pr_priceparameter")) {
			list_PriceParamsBO.clear();
			PriceParamsBO[] AllInvstt = getAllPriceParamsBO();
			for (PriceParamsBO pm_specialsaleprogram_invsttBO : AllInvstt) {
				list_PriceParamsBO.put(pm_specialsaleprogram_invsttBO.recordid,
						pm_specialsaleprogram_invsttBO);
			}
		}
	}

 
	public PriceParamsBO[] getAllPriceParamsBO() throws Throwable {

		return oclient.QueryFunction("priceparameter_getAll", PriceParamsBO[].class, false);

	}

	 
}
