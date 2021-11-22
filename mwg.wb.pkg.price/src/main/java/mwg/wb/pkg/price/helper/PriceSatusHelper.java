package mwg.wb.pkg.price.helper;

import mwg.wb.common.Logs;
import mwg.wb.common.Utils;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.model.products.SpecialSaleProgramBO;

import java.util.Date;
import java.util.List;

public class PriceSatusHelper {
	public static int GetOrder(int WebStatusId_3, Date dCreateDate, Date arrivalDateField, int siteid, String Lang) {
		return MasterHelper.getHelperBySite(siteid).GetOrder(WebStatusId_3, dCreateDate, arrivalDateField);
	}

	public static ProductErpPriceBO ProcessProductStatus(ProductErpPriceBO objPrice, int siteid, String Lang) {
		return MasterHelper.getHelperBySite(siteid).ProcessProductStatus(objPrice);
	}
	public static ProductErpPriceBO ProcessProductStatus2(ProductErpPriceBO objPrice, int siteid, String Lang, int quantityOLOLHub) {
		return MasterHelper.getHelperBySite(siteid).ProcessProductStatus2(objPrice, quantityOLOLHub);
	}

	public static boolean IsOnlineOnlyProduct(ProductBO productBO) {
		if (productBO != null && productBO.SpecialSaleProgram != null
				&& !Utils.StringIsEmpty(productBO.SpecialSaleProgram.SpecialSaleProgramName)
				&& productBO.SpecialSaleProgram.BeginDate.before(new Date())
				&& productBO.SpecialSaleProgram.EndDate.after(new Date()))
			return true;
		return false;
	}

	public static boolean isValidProgram(SpecialSaleProgramBO specialSaleProgramBO) {
		Date now = new Date();
		return specialSaleProgramBO != null && !Utils.StringIsEmpty(specialSaleProgramBO.SpecialSaleProgramName)
				&& specialSaleProgramBO.BeginDate.before(now) && specialSaleProgramBO.EndDate.after(now);
	}

	public static ProductErpPriceBO ProcessProductStatusDMX(ProductErpPriceBO objPrice, int siteId, int categoryid, int quantityOLOLHub) {
		return MasterHelper.getHelperBySite(siteId).ProcessProductStatusDMX(objPrice, siteId, categoryid,
				quantityOLOLHub);
	}

	public static Double GetPriceByCodeFromList(boolean isLog, String note, List<ProductErpPriceBO> lstPrices,
			String ProductCode, int ProvinceId) {
		double Price402 = 0;
		if (lstPrices != null) {
			var price402 = lstPrices.stream().filter(p -> p != null && ProductCode != null
					&& ProductCode.equals(p.ProductCode) && ProvinceId == p.ProvinceId).findFirst().orElse(null);
			if (price402 != null) {
				Price402 = price402.Price;
				Logs.Log(isLog, note, "GetPriceByCodeFromList:  " + ProductCode + "price  ==  " + Price402);
			} else {
				Price402 = 0;
				Logs.Log(isLog, note, "GetPriceByCodeFromList: " + ProductCode + " price   == null");
			}
		} else {
			Price402 = 0;
			Logs.Log(isLog, note, "GetPriceByCodeFromList:lstPrices  == null");
		}
		return Price402;
	}

	public static ProductErpPriceBO GetPriceDefault(List<ProductErpPriceBO> listPrice, int siteid, String Lang) {
		return MasterHelper.getHelperBySite(siteid).GetPriceDefault(listPrice);
	}

}
