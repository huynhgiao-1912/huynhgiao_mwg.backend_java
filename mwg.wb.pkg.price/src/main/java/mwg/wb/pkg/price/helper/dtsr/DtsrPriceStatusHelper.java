package mwg.wb.pkg.price.helper.dtsr;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import mwg.wb.common.Utils;
import mwg.wb.model.products.ProductErpPriceBO;
import mwg.wb.pkg.price.helper.MasterHelper;

public class DtsrPriceStatusHelper extends MasterHelper {

	// https://docs.google.com/spreadsheets/d/1wUzCOE_aEHTY9TynMnXyAfT4UkgxcoBIpm8_wzOCF4g/edit#gid=964197835
	@Override
	public ProductErpPriceBO ProcessProductStatus(ProductErpPriceBO objPrice) {
		if (objPrice != null)

			// Không hiển thị web
			if (!objPrice.IsWebShow) {
				// Trực confirm: Bỏ hiển thị web => KKD
				objPrice.WebStatusId = 1;
			} else if (objPrice.Price > 0) {
				if (objPrice.Quantity > 0) {// - Có giá khu vực 678, còn tồn các siêu thị Siêu rẻ
					objPrice.WebStatusId = 4;
				} else {
					// - Có giá khu vực 678 >0 , không có tồn và trạng thái erp là 0,5(không
					// kd),6(end offline)
					if (objPrice.StatusId == 5 || objPrice.StatusId == 6 || objPrice.StatusId == 0) {
						objPrice.WebStatusId = 1;
					} else if (objPrice.IsForthcoming) // Hàng sắp về
					{
						objPrice.WebStatusId = 2;
					} else {
						objPrice.WebStatusId = 5;// tam het hàng
					}
				}
			} else // - Giá khu vực 678 = 0,Giá khu vực 678 (OFF cập nhật chính thức giá)
			{
				if (objPrice.IsForthcoming)// hang sap về
				{
					objPrice.WebStatusId = 2;
				} else {
					objPrice.WebStatusId = 1;
				}

			}

		return objPrice;
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
//		Date dts = Utils.GetCurrentDate();

		ProductErpPriceBO objPrice = null;
		if (listPrice.size() == 1) {
			objPrice = listPrice.stream().findFirst().get();
		} else {

			// Mặc định lấy sản phẩm còn hàng & được hiển thị trang chủ trước

			List<ProductErpPriceBO> tmp = listPrice.stream()
					.filter(c -> c != null && (c.WebStatusId == 4) && c.IsShowHome).collect(Collectors.toList());
			if (tmp != null && tmp.size() > 0) {
				objPrice = tmp.stream().sorted(Comparator.comparingDouble(ProductErpPriceBO::getPrice).reversed())
						.findFirst().get();
			}
			tmp = null;
			// Tiếp theo tới còn hàng
			if (objPrice == null) {
				tmp = listPrice.stream().filter(c -> c != null && (c.WebStatusId == 4)).collect(Collectors.toList());
				if (tmp != null && tmp.size() > 0) {
					objPrice = tmp.stream().sorted(Comparator.comparingDouble(ProductErpPriceBO::getPrice).reversed())
							.findFirst().get();
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
							.sorted(Comparator.comparingDouble(ProductErpPriceBO::getPrice).reversed()
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
					objPrice = tmp.stream().sorted(Comparator.comparingDouble(ProductErpPriceBO::getPrice).reversed())
							.findFirst().get();
				}
				tmp = null;
			}

		}

		return objPrice;
	}

}
