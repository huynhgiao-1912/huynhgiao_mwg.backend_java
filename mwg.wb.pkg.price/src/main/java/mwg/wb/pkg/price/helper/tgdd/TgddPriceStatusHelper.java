package mwg.wb.pkg.price.helper.tgdd;

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

public class TgddPriceStatusHelper extends MasterHelper {

	@Override
	public ProductErpPriceBO ProcessProductStatus(ProductErpPriceBO objPrice) {
		return APIPriceHelper.getHelperBySite(1).ProcessProductStatus(objPrice);
	}
	public ProductErpPriceBO ProcessProductStatus2(ProductErpPriceBO objPrice, int quantityOLOLHub) {
		return APIPriceHelper.getHelperBySite(1).ProcessProductStatus2(objPrice, quantityOLOLHub);
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
		if (listPrice == null || listPrice.size() == 0) {
			listPrice = null;
			return null;
		}

		if (DidxHelper.isBeta() || DidxHelper.isLocal()){
			return MasterHelper.getHelperBySite(2).GetPriceDefault(listPrice);
		}
		if (true)
			return GetPriceDefaultV3(listPrice);

//		Date dts = Utils.GetCurrentDate();

		ProductErpPriceBO objPrice = null;
		if (listPrice.size() == 1) {
			objPrice = listPrice.stream().findFirst().get();
		} else {

			List<ProductErpPriceBO> tmp = listPrice.stream()
					.filter(c -> c != null && (c.WebStatusId == 4) && c.IsShowHome).collect(Collectors.toList());
			if (tmp != null && tmp.size() > 0) {
				objPrice = tmp.stream().sorted(Comparator.comparingDouble(x -> -x.Price)).findFirst().get();
			}
			tmp = null;
			// Tiếp theo tới còn hàng
			if (objPrice == null) {
				tmp = listPrice.stream().filter(c -> c != null && (c.WebStatusId == 4)).collect(Collectors.toList());
				if (tmp != null && tmp.size() > 0) {
					objPrice = tmp.stream().sorted(Comparator.comparingDouble(x -> -x.Price)).findFirst().get();
				}
				tmp = null;
			}
			// Nếu không có sản phẩm còn hàng, ưu tiên code có show web
			if (objPrice == null) {
				tmp = listPrice.stream().filter(c -> c != null && (c.WebStatusId != 1) && c.IsWebShow)
						.collect(Collectors.toList());
				if (tmp != null && tmp.size() > 0) {
					// Xếp theo giá giảm dần, ưu tiên 3 (giao 2-7), 5 (hết hàng tạm thời)...
					// objPrice = tmp.stream().sorted((o1,o2)->
					// o1.Price.compareTo(o2.Price)).ThenBy(x -> SiteID == 1 ? (x.WebStatusId == 3 ?
					// 3 : (x.WebStatusId == 5 ? 5 : 10)) : (x.WebStatusId == 3 ? 3 : (x.WebStatusId
					// == 5 ? 5 : 10))).FirstOrDefault();
					objPrice = tmp.stream()
							.sorted(Comparator.<ProductErpPriceBO>comparingDouble(x -> -x.Price)
									.thenComparing(x -> (x.WebStatusId == 3 ? 3 : (x.WebStatusId == 5 ? 5 : 10))))
							.findFirst().get();
//						users.sort(Comparator.comparingInt(User::getName).thenComparing(User::getAge));

				}
				tmp = null;
			}
			// Nếu không code nào được check show web, lấy giá cao nhất
			if (objPrice == null) {
				tmp = listPrice.stream().filter(c -> c != null && (c.WebStatusId != 1)).collect(Collectors.toList());
				if (tmp != null && tmp.size() > 0) {
					objPrice = tmp.stream().sorted(Comparator.comparingDouble(x -> -x.Price)).findFirst().get();
				}
				tmp = null;
			}

		}

		return objPrice;
	}

	public ProductErpPriceBO GetPriceDefaultV2(List<ProductErpPriceBO> listPrice) {
//		try {
		if (listPrice == null || listPrice.size() == 0) {
			listPrice = null;
			return null;
		}
//			Date dts = Utils.GetCurrentDate();

//			ProductErpPriceBO objPrice = null;

//			debug
//			if (listPrice.get(0).ProvinceId == 3) {
//				int a = 0;
//			}

		listPrice = listPrice.stream().sorted(Comparator.comparingInt(x -> -x.ProductCodeTotalQuantity))
				.collect(Collectors.toList()); // desending

//		printQuantity(listPrice);

		if (listPrice != null && listPrice.size() == 1) {
			return listPrice.stream().findFirst().orElse(null);
		}

		var PriceTmp1 = listPrice.stream().filter(x -> x.WebStatusId == 4
//					&& x.ProductCodeTotalQuantity > 0
		).collect(Collectors.toList());
		if (PriceTmp1 != null && PriceTmp1.size() == 1) {
			return PriceTmp1.stream().findFirst().orElse(null);
		} else if (PriceTmp1.size() > 1) {
			// nhiều code còn hàng
			var PriceTmp2 = PriceTmp1.stream().sorted(Comparator.comparing(x -> Strings.isNullOrEmpty(x.Image)))
					.collect(Collectors.toList());
			return PriceTmp2.stream().sorted(Comparator.comparingInt(x -> x.DisplayOrder)).findFirst().orElse(null);
		} else { // == 0
			var PriceTmp3 = listPrice.stream()
//						.filter(x -> x.WebStatusId == 11)
//						.filter(x -> x.ProductCodeTotalQuantity > 0)
					.filter(x -> !Strings.isNullOrEmpty(x.Image)).collect(Collectors.toList());
			if (PriceTmp3 != null && PriceTmp3.size() == 1)
				return PriceTmp3.stream().findFirst().orElse(null);
			if (PriceTmp3.size() > 1) {
				var def = PriceTmp3.stream().sorted(Comparator.comparingInt(x -> x.DisplayOrder)).findFirst()
						.orElse(null);
				return def;
			} else {
				var def = listPrice.stream().sorted(Comparator.comparingInt(x -> x.DisplayOrder)).findFirst()
						.orElse(null);
				return def;
			}
		}

//		} catch (Exception e) {
//			return new ProductErpPriceBO();
//		}

	} // end GetPriceDefaultV2

	public ProductErpPriceBO GetPriceDefaultV3(List<ProductErpPriceBO> listPrice) {
//		try {
		if (listPrice == null || listPrice.size() == 0) {
			listPrice = null;
			return null;
		}
//			Date dts = Utils.GetCurrentDate();

//			ProductErpPriceBO objPrice = null;

		listPrice = listPrice.stream().sorted(Comparator.<ProductErpPriceBO>comparingDouble(x -> x.DisplayOrder)
				.thenComparingInt(x -> -x.ProductCodeQuantity)).collect(Collectors.toList()); // desending

		if (listPrice != null && listPrice.size() == 1) {
			return listPrice.stream().findFirst().orElse(null);
		}

		// Mặc định lấy sản phẩm còn hàng & code co hinh mau truoc
		var PriceTmp1 = listPrice.stream().filter(x -> x.WebStatusId == 4)
				// .sorted(Comparator.<ProductErpPriceBO>comparingInt(x ->
				// Strings.isNullOrEmpty(x.Image) ? 1 : 0 ))
				.filter(x -> !Strings.isNullOrEmpty(x.Image)).collect(Collectors.toList());

		if (PriceTmp1 != null && PriceTmp1.size() > 0) {
			return PriceTmp1.stream().findFirst().orElse(null);
		} else {
			PriceTmp1 = listPrice.stream().filter(x -> x.WebStatusId == 4).filter(x -> x.IsShowHome)
					.collect(Collectors.toList());
			if (PriceTmp1 != null && PriceTmp1.size() > 0) {
				return PriceTmp1.stream().findFirst().orElse(null);
			} else {
				PriceTmp1 = listPrice.stream().filter(x -> x.WebStatusId == 4).collect(Collectors.toList());
				if (PriceTmp1 != null && PriceTmp1.size() > 0) {
					return PriceTmp1.stream().findFirst().orElse(null);
				}
			}

			PriceTmp1 = listPrice.stream().filter(x -> x.WebStatusId != 1).filter(x -> x.IsWebShow)
					.collect(Collectors.toList());
			if (PriceTmp1 != null && PriceTmp1.size() > 0) {
				var tmp = PriceTmp1.stream().sorted(Comparator.<ProductErpPriceBO>comparingDouble(
						x -> (x.WebStatusId == 3 || x.WebStatusId == 11 ? 3 : (x.WebStatusId == 5 ? 5 : 10) ))).findFirst().get();
				if (tmp != null) {
					return tmp;
				}
			}

			PriceTmp1 = listPrice.stream().filter(x -> x.WebStatusId != 1).collect(Collectors.toList());

			if (PriceTmp1 != null && PriceTmp1.size() > 0) {
				return PriceTmp1.stream().findFirst().orElse(null);
			}

			var PriceFirst = listPrice.stream().findFirst().orElse(null);
			if (PriceFirst != null) {
				PriceFirst.Price = 0;
				return PriceFirst;
			}

			return listPrice.stream().findFirst().orElse(null);
		}

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

	} // end GetPriceDefaultV3

	public void printQuantity(List<ProductErpPriceBO> listPrice) {
		for (var x : listPrice) {
			System.out.println(x.ProductCode + " productcodeTotalQuantity: " + x.ProductCodeTotalQuantity);
		}
	}
}
