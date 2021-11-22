package mwg.wb.pkg.price.helper;

import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.pkg.price.helper.bhx.BhxPriceStatusHelper;
import mwg.wb.pkg.price.helper.bigphone.BigPhonePriceStatusHelper;
import mwg.wb.pkg.price.helper.dmx.DmxPriceStatusHelper;
import mwg.wb.pkg.price.helper.dtsr.DtsrPriceStatusHelper;
import mwg.wb.pkg.price.helper.tgdd.TgddPriceStatusHelper;

import java.util.Date;
import java.util.List;

public class MasterHelper {

	private static MasterHelper dtsr, bigphone, tgdd, dmx, unknown;
	
	public synchronized static MasterHelper getHelperBySite(int siteID) {
		switch (siteID) {
		case 2:
			if (dmx == null)
				dmx = new DmxPriceStatusHelper();
			return dmx;
		case 12:
			if (dtsr == null)
				dtsr = new DtsrPriceStatusHelper();
			return dtsr;
		case 6:
			if (bigphone == null)
				bigphone = new BigPhonePriceStatusHelper();
			return bigphone;
		case 1:
			if (tgdd == null)
				tgdd = new TgddPriceStatusHelper();
			return tgdd;
		case 11:
			if (tgdd == null)
				tgdd = new BhxPriceStatusHelper();
			return tgdd;
		default:
			if (unknown == null)
				unknown = new MasterHelper();
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
