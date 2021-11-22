package mwg.wb.test;

import java.io.IOException;

import mwg.wb.client.graph.OrientDBFactory;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.model.products.ProductErpPriceBO;

//import mwg.wb.model.promotion.PromotionInfoBO.Promotion;
public class price {
	private static OrientDBFactory factoryDB = null;

	public static void main(String[] args) throws IOException, IllegalArgumentException, IllegalAccessException {
		//ErpHelper a = new ErpHelper();
		// Promotion[] xyz = a.GetPromotionByPrdID("1000015004315", -1, 0, 0, 1);
		// String aaaa = a.GetPromotionByPrdIDStr("1000015004315", -1, 0, 0,
		// 1);1000015004315
//		ProductErpPriceBO[] abc = a.GetPriceByProductCode(13, "0130018000349", 0, 1);
		// abc

		// PriceUpsert ahaha = new PriceUpsert();
		// ahaha.Refresh()

//		OrientDBClient2 orientDB = factoryDB.GetConnect("priceupsert");
//		
//		for (final ProductErpPriceBO item : abc) {
//
//			for (int i = 0; i < 5; i++) {
//				
//				var rs = orientDB.UpsertODatabaseJson("product_price", "recordid", item.RecordID, item);
//			}
//		}

	}
}
