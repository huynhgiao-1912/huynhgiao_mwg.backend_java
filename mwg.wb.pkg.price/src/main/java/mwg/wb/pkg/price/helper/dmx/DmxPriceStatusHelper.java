package mwg.wb.pkg.price.helper.dmx;

import com.google.common.base.Strings;
import mwg.wb.business.helper.APIPriceHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Utils;
import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.pkg.price.helper.MasterHelper;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DmxPriceStatusHelper extends MasterHelper {

	// for DMX
	@Override
	public ProductErpPriceBO ProcessProductStatusDMX(ProductErpPriceBO objPrice, int siteId, int categoryid,
			 int quantityOLOLHub) {
		return APIPriceHelper.getHelperBySite(2).ProcessProductStatusDMX(objPrice, siteId, categoryid,
				quantityOLOLHub);
	}

	@Override
	public int GetOrder(int WebStatusId_3, Date dCreateDate, Date arrivalDateField) {
		// int[] mainCat = new int[] { 42, 44, 522, 1882, 5698, 85, 7077, 7264, 5693
		// };//bo sung cate dong ho, máy in
		int result = 10;
		// if (Prices == null)
		// return 10;
		// var ListWebStatusId_3 = Prices
		// if (ListWebStatusId_3 != null && ListWebStatusId_3.Any())
		// {

		// var WebStatusId_3 =
		// DidxHelper.toInt(ListWebStatusId_3.FirstOrDefault().Value);

		switch (WebStatusId_3) {
		case 1:
			if (dCreateDate != null && dCreateDate != null
					&& (System.currentTimeMillis() - dCreateDate.getTime()) < 5184000000l)
				result = 39; // "Mới ra mắt";
			else
				result = 10;// "Không kinh doanh"
			break;
		case 2:
			if (arrivalDateField != null && arrivalDateField != null
					&& arrivalDateField.getTime() > System.currentTimeMillis()) {
				if (Utils.getDayOfMonth(arrivalDateField) <= 15)
					result = 39;// "Hàng sắp về, dự kiến về " + "đầu tháng " + obj.arrivalDateField.Month + "/"
								// + obj.arrivalDateField.Year;
				else
					result = 39;// "Hàng sắp về, dự kiến về " + "cuối tháng " + obj.arrivalDateField.Month + "/"
								// + obj.arrivalDateField.Year;
			} else
				result = 39;// "Hàng sắp về";
			break;
		case 3:
			result = 40;// "Giao hàng từ 2 - 7 ngày";
			break;
		case 4:
			// if (mainCat.Contains(iCat))
			// result = 40;// "Còn hàng - <a onclick='return ShowStore()'>Xem siêu thị còn
			// hàng</a>";
			// else
			result = 40;// "Còn hàng";
			break;
		case 5:
			result = 38;// "Hết hàng tạm thời";
			break;
		case 8:
			result = 40;// "Giao hàng từ 2 - 7 ngày";
			break;
		case 9:
			if (dCreateDate != null && dCreateDate != null
					&& (System.currentTimeMillis() - dCreateDate.getTime()) < 5184000000l)
				result = 39;// "Mới ra mắt";
			else
				result = 10;
			break;
		}
		// }
		// if (!mainCat.Contains(iCat) && result > 10) result = 31;
		return result;
	}

	@Override
	public ProductErpPriceBO GetPriceDefault(List<ProductErpPriceBO> listPrice) {
//		try {
		if (DidxHelper.isBeta() || DidxHelper.isLocal()){
			return GetPriceDefaultV3(listPrice);
		}

		if (true)
			return GetPriceDefaultV2(listPrice);

		if (listPrice == null || listPrice.size() == 0) {
			listPrice = null;
			return null;
		}
//			Date dts = Utils.GetCurrentDate();

		ProductErpPriceBO objPrice = null;
		listPrice = listPrice.stream().filter(p -> p != null).sorted(Comparator
				.<ProductErpPriceBO>comparingInt(x -> -x.ProductCodeQuantity).thenComparingDouble(x -> -x.Price))
				.collect(Collectors.toList());

		if (listPrice.size() == 1) {
			objPrice = listPrice.stream().findFirst().get();
		} else {

			objPrice = listPrice.stream().filter(p -> p.WebStatusId == 4 && p.ProductCodeTotalQuantity > 0).findFirst()
					.orElse(null);
			if (objPrice == null) {
				objPrice = listPrice.stream().filter(p -> p.WebStatusId == 11 && p.ProductCodeTotalQuantity > 0)
						.findFirst().orElse(null);
				if (objPrice == null) {
					objPrice = listPrice.stream().filter(p -> p.WebStatusId == 99 && p.ProductCodeTotalQuantity > 0)
							.findFirst().orElse(null);
					if (objPrice == null) {
						objPrice = listPrice.stream().filter(p -> p.WebStatusId == 2).findFirst().orElse(null);
						if (objPrice == null) {
							objPrice = listPrice.get(0);
						}
					}
				}
			}
		}

		return objPrice;
//		} catch (Exception e) {
//			return new ProductErpPriceBO();
//		}
	}

//	private boolean IsOnlineOnlyProduct(ProductBO productBO) {
//		Date now = new Date();
//		if (productBO != null && productBO.SpecialSaleProgram != null
//				&& !Strings.isNullOrEmpty(productBO.SpecialSaleProgram.SpecialSaleProgramName)
//				&& productBO.SpecialSaleProgram.BeginDate.before(now)
//				&& productBO.SpecialSaleProgram.EndDate.after(now))
//			return true;
//		return false;
//	}

	public ProductErpPriceBO GetPriceDefaultV2(List<ProductErpPriceBO> listPrice) {
//		try {
		if (listPrice == null || listPrice.size() == 0) {
			listPrice = null;
			return null;
		}
//			Date dts = Utils.GetCurrentDate();

//			ProductErpPriceBO objPrice = null;

		listPrice = listPrice.stream()
				.sorted(Comparator.<ProductErpPriceBO>comparingDouble(x -> -x.Price)
						.thenComparingInt(x -> -x.ProductCodeQuantity).thenComparingInt(x -> x.DisplayOrder))
				.collect(Collectors.toList()); // desending

		//listPrice = listPrice.stream().sorted(Comparator.<ProductErpPriceBO>comparingDouble(x -> x.DisplayOrder)).collect(Collectors.toList());
		// listPrice =
		// listPrice.stream().sorted(Comparator.<ProductErpPriceBO>comparingInt(x ->
		// x.DisplayOrder)).collect(Collectors.toList());

		if (listPrice != null && listPrice.size() == 1) {
			return listPrice.stream().findFirst().orElse(null);
		}

		var PriceTmp1 = listPrice.stream().filter(x -> x.WebStatusId == 4).filter(x -> x.ProductCodeTotalQuantity > 0)
				.sorted(Comparator.<ProductErpPriceBO>comparingInt(x -> Strings.isNullOrEmpty(x.Image) ? 1 : 0))
				.collect(Collectors.toList());

		if (PriceTmp1 != null && PriceTmp1.size() > 0) {
			return PriceTmp1.stream().sorted(Comparator.<ProductErpPriceBO>comparingInt(x -> x.DisplayOrder)).findFirst().orElse(null);
		}

		PriceTmp1 = listPrice.stream().filter(x -> x.WebStatusId == 11)// .filter(x -> !Strings.isNullOrEmpty(x.Image))
				.sorted(Comparator.<ProductErpPriceBO>comparingInt(x -> Strings.isNullOrEmpty(x.Image) ? 1 : 0))
				.filter(x -> x.ProductCodeTotalQuantity > 0).collect(Collectors.toList());
		if (PriceTmp1 != null && PriceTmp1.size() > 0) {
			return PriceTmp1.stream().sorted(Comparator.<ProductErpPriceBO>comparingInt(x -> x.DisplayOrder)).findFirst().orElse(null);
		}

		PriceTmp1 = listPrice.stream().filter(x -> x.WebStatusId == 99).filter(x -> x.ProductCodeTotalQuantity > 0)
				.collect(Collectors.toList());
		if (PriceTmp1 != null && PriceTmp1.size() > 0) {
			return PriceTmp1.stream().findFirst().orElse(null);
		}

		PriceTmp1 = listPrice.stream().filter(x -> x.WebStatusId == 2).collect(Collectors.toList());
		if (PriceTmp1 != null && PriceTmp1.size() > 0) {
			return PriceTmp1.stream().findFirst().orElse(null);
		}

		return listPrice.stream().sorted(Comparator.<ProductErpPriceBO>comparingInt(x -> x.DisplayOrder)).findFirst().orElse(null);

//		else if (PriceTmp1.size() > 1) {
//			// nhiều code còn hàng
////			var PriceTmp2 = PriceTmp1.stream().sorted(Comparator.comparing(x -> Strings.isNullOrEmpty(x.Image)))
////					.collect(Collectors.toList());
////			return PriceTmp2.stream().sorted(Comparator.comparingInt(x -> x.DisplayOrder)).findFirst().orElse(null);
//			
//			
//			return PriceTmp1.stream().sorted(Comparator.<ProductErpPriceBO>comparingInt(x -> Strings.isNullOrEmpty(x.Image) ? 1 : 0 )
//					.thenComparingInt(x -> x.DisplayOrder)).findFirst().orElse(null); //.collect(Collectors.toList())
//			
//			
//		} else { // == 0
//			var PriceTmp3 = listPrice.stream().filter(
//					x -> x.WebStatusId == 11 && x.ProductCodeTotalQuantity > 0 && !Strings.isNullOrEmpty(x.Image))
//					.collect(Collectors.toList());
//			if (PriceTmp3 != null && PriceTmp3.size() == 1)
//				return PriceTmp3.stream().findFirst().orElse(null);
//			if (PriceTmp3.size() > 1) {
//				var def = PriceTmp3.stream().sorted(Comparator.comparingInt(x -> x.DisplayOrder)).findFirst()
//						.orElse(null);
//				return def;
//			} else {
//				var def = listPrice.stream().sorted(Comparator.comparingInt(x -> x.DisplayOrder)).findFirst()
//						.orElse(null);
//				return def;
//			}
//		}

//		} catch (Exception e) {
//			return new ProductErpPriceBO();
//		}

	} // end GetPriceDefaultV2

	public ProductErpPriceBO GetPriceDefaultV3(List<ProductErpPriceBO> listPrice) {

		if (listPrice == null || listPrice.size() == 0) {
			return null;
		}

		listPrice = listPrice.stream().sorted(Comparator.<ProductErpPriceBO>comparingDouble(x -> x.DisplayOrder)
				.thenComparingInt(x -> -x.ProductCodeQuantity)).collect(Collectors.toList()); // desending

		if (listPrice != null && listPrice.size() == 1) {
			return listPrice.stream().findFirst().orElse(null);
		}

		/*
		* Ưu tiên 1: code có webstatus = 4 và sắp xếp theo
		Khai báo thumb màu thứ tự thấp nhất
		Giá thấp nhất và >0
		Ưu tiên 2: code có webstatus = 11 và sắp xếp theo
		Khai báo thumb màu thứ tự thấp nhất
		Giá thấp nhất và >0
		*/
		var priceDefault = listPrice.stream()
				.filter(x -> x.WebStatusId == 4 || x.WebStatusId == 11)
				.sorted(Comparator.<ProductErpPriceBO>comparingInt(x -> x.WebStatusId)
						.thenComparingInt(x -> x.WebStatusId == 4 ? 0 : 2)
						.thenComparingInt(x -> x.hasImage())
						//.thenComparingInt(x -> x.DisplayOrder)
						.thenComparingDouble(x -> x.Price)).collect(Collectors.toList());
		if (priceDefault != null && priceDefault.size() > 0) {
			return priceDefault.stream().findFirst().orElse(null);
		}else{
			/*
			* Ưu tiên 3: code có webstatus = 2,8 và sắp xếp theo
			Giá cao nhất
			Ngược lại lấy code đầu tiên và sắp xếp theo
			Giá cao nhất
			*/
			priceDefault = listPrice.stream()
					.sorted(Comparator.<ProductErpPriceBO>comparingDouble(x -> (x.WebStatusId == 2 || x.WebStatusId == 8) ? 0 : 2)
							.thenComparingDouble(x -> -x.Price))
					.collect(Collectors.toList());
			if (priceDefault != null && priceDefault.size() > 0) {
				return priceDefault.stream().findFirst().orElse(null);
			}
			return listPrice.stream().findFirst().orElse(null);
		}
	} // end GetPriceDefaultV3
}
