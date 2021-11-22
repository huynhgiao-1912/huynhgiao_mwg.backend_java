package mwg.wb.business.helper;

import mwg.wb.model.products.ProductErpPriceBO;

import java.util.Date;
import java.util.List;

public class APIPriceHelper {

	private static APIPriceHelper dtsr, bigphone, tgdd, dmx, unknown;

	public synchronized static APIPriceHelper getHelperBySite(int siteID) {
		switch (siteID) {
		case 2:
			if (dmx == null)
				dmx = new DmxPriceHelper();
			return dmx;
		case 12:
			if (dtsr == null)
				dtsr = new DtsrPriceHelper();
			return dtsr;
		case 6:
			if (bigphone == null)
				bigphone = new BigPhonePriceHelper();
			return bigphone;
		case 1:
			if (tgdd == null)
				tgdd = new TgddPriceHelper();
			return tgdd;
		case 11:
			if (tgdd == null)
				tgdd = new BhxPriceHelper();
			return tgdd;
		default:
			if (unknown == null)
				unknown = new APIPriceHelper();
			return unknown;
		}
	}

	public ProductErpPriceBO ProcessProductStatusDMX(ProductErpPriceBO objPrice, int siteId, int categoryid,
			int quantityOLOLHub) {
		return null;
	}

	public ProductErpPriceBO ProcessProductStatus(ProductErpPriceBO objPrice) {
		return null;
	}
	public ProductErpPriceBO ProcessProductStatus2(ProductErpPriceBO objPrice, int quantityOLOLHub) {
		return null;
	}
	public int GetOrder(int WebStatusId_3, Date dCreateDate, Date arrivalDateField) {
		return 0;
	};

	public ProductErpPriceBO GetPriceDefault(List<ProductErpPriceBO> listPrice) {
		return null;
	}
}
