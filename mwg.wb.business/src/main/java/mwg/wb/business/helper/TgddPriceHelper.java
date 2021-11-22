package mwg.wb.business.helper;

import mwg.wb.common.Utils;
import mwg.wb.model.products.ProductBO;
import mwg.wb.model.products.ProductErpPriceBO;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TgddPriceHelper extends APIPriceHelper {

	public static final List<Integer> categoryOlOlHub = Arrays.asList(
			7264, 7077, /* đồng hồ thời trang và đồng hồ thông minh */
			54,55,56,57,58,60,75,85,86,1363,1622,1662,1882,1902,2162,3885,4547,4727,4728,6858,6859,6862,6863,7922,7923,7924,7925,9041,9118,9119,9262,9298,9320,9321,9341,9358,9386,9398,9458,9499,9518 /*AccessoryCategory*/
			);
	private static List<Integer> cate364 = Arrays.asList(1262, 5693, 5697, 5698);

	public boolean CheckIsOnlineOnly(ProductErpPriceBO objPrice) {
		var specialSaleProgram = objPrice.specialSale;
		var now = new Date();
		return specialSaleProgram != null && !Utils.StringIsEmpty(specialSaleProgram.SpecialSaleProgramName) && specialSaleProgram.BeginDate.before(now)
				&& specialSaleProgram.EndDate.after(now);
	}

	@Override
	public ProductErpPriceBO ProcessProductStatus(ProductErpPriceBO objPrice) {
		return ProcessProductStatus2(objPrice, objPrice.quantityOLOLHub);
	}
	@Override
	public ProductErpPriceBO ProcessProductStatus2(ProductErpPriceBO objPrice, int quantityOLOLHub) {
		if (objPrice == null)
			return null;

		// lay trang thai dmx
		ProductBO product = new ProductBO() {{
			CategoryID = objPrice.CategoryId;
		}};
		APIPriceHelper.getHelperBySite(2).ProcessProductStatusDMX(objPrice, 1, objPrice.CategoryId,
				quantityOLOLHub);
		objPrice.WebStatusIdNew = objPrice.WebStatusId;
		objPrice.WebStatusId = 0;

		try {
			Date now = new Date();

			// debug
//		if (objPrice.ProvinceId == 3) {
//			int a = 0;
//		}

			// if (objPrice.IsProductForthComing)//bat buoc hang sap ve. yeu cau
			// https://docs.google.com/document/d/13yVLsYu3sC03RG_jpuGFAGe7ERKAc8KwqWkgCvM76dc/edit#heading=h.r3hlly9ug9je
			// {
			// objPrice.WebStatusId = 2;
			// return objPrice;
			// }

			// Không hiển thị web
			if (!objPrice.IsWebShow && !objPrice.IsProductForthComing
					&& (objPrice.ProductArrivalDate == null || objPrice.ProductArrivalDate.before(now))) {
				// Trực confirm: Bỏ hiển thị web => KKD
				objPrice.WebStatusId = 1;
				return objPrice;
			}

			// objPrice.IsOnlineOnly = CheckIsOnlineOnly(objPrice);

			
			if (categoryOlOlHub.contains(objPrice.CategoryId) && objPrice.codeIsOnlineOnly && quantityOLOLHub != -1)// -1
			{
				if (quantityOLOLHub > 0) {
					objPrice.WebStatusId = 4;
				} else {
					objPrice.WebStatusId = 5;
				}
				return objPrice;
			}
						
			if (objPrice.Price > 0) {
				// trạng thái cho sp online only:dien thoai, laptop, mhmt

				if (Arrays.asList(42, 44, 5697).contains(objPrice.CategoryId)) {
					if (objPrice.IsOnlineOnly) {

						// //https://docs.google.com/document/d/1nROO-ZGTGnZqQpK1wrmcsPLCUinvsecYptm9Jcq6gI0/edit#
						if (objPrice.CategoryId == 44) {
							if (objPrice.ProductCodeTotalQuantity <= 0)// ton kho code toan quoc
							{
								objPrice.WebStatusId = 1;
							} else {
								objPrice.WebStatusId = 4;
							}
						} else {
							if (objPrice.StatusId == 6 && objPrice.ProductCodeTotalQuantity <= 0)// ton kho code toan quoc
							{

								objPrice.WebStatusId = 1;
							} else {
								objPrice.WebStatusId = 4;
							}
						}

						return objPrice;
					}

				}

//            #region Phụ kiện Online-Only
				if (objPrice.codeIsOnlineOnly || (!Arrays.asList(42, 44, 522, 1882).contains(objPrice.CategoryId)
						&& objPrice.DeliveryType == 4)) {
					if (objPrice.CenterQuantity >= objPrice.WebMinStock && objPrice.CenterQuantity > 0) {
						// Còn tồn tại KTT => Còn hàng
						objPrice.WebStatusId = 4;
					} else {
						// Không còn tồn => Hết hàng tạm thời
						objPrice.WebStatusId = 5;
					}

					return objPrice;
				}
//            #endregion

//            #region Loa kéo
				if (objPrice.CategoryId == 2162 && objPrice.maingroupID == 304 && objPrice.subgroupID == 880) {

					if (objPrice.CenterQuantity + objPrice.Quantity >= objPrice.WebMinStock
							&& objPrice.CenterQuantity + objPrice.Quantity > 0) {
						// Còn tồn tại KTT => Còn hàng
						objPrice.WebStatusId = 4;
					} else {
						// Không còn tồn => Hết hàng tạm thời
						objPrice.WebStatusId = 5;
					}
					return objPrice;
				}
//            #endregion

//				Anh Trùy yêu cầu 20201208
//				- Đối với NH 5697 - Màn hình, máy tính để bàn, 5698 - Máy tính nguyên bộ, 5693 - Máy in, Fax
//					và DeliveryVehicles = 1 (sản phẩm chỉ bán tại siêu thị)
				if (cate364.contains(objPrice.CategoryId) && objPrice.DeliveryVehicles == 1) {
					if (objPrice.is364province) { // Tỉnh thành có siêu thị kinh doanh sản phẩm
						if (objPrice.ProductCodeTotalQuantity > 0) {
							objPrice.WebStatusId = 4;
						} else {
							objPrice.WebStatusId = 1;
						}
					} else { // Tỉnh thành ko có siêu thị kinh doanh sản phẩm => Ngưng kinh doanh
						objPrice.WebStatusId = 1;
					}
					return objPrice;
				}

				// Trạng thái là "Đặt trước (giao hàng từ 2 đến 7 ngày)"
				if (objPrice.WebStatusId == 9) {
					return objPrice;// Trường hợp giá dự kiến set trong CMS, return
				} else {
					// Ốp lưng EOF => Không kinh doanh
					if (objPrice.CategoryId == 60 || objPrice.CategoryId == 1662) {
						if (objPrice.ProductCodeTotalQuantity > objPrice.WebMinStock) {
							objPrice.WebStatusId = 4;
						} else {
							objPrice.WebStatusId = 1;
						}
						return objPrice;
					}

					// thay doi rule
					// https://docs.google.com/document/d/1lhdjLQ0ApetOG7m3xmZlQ2DDZvcE3sVR/edit
					// yeu cau Phuong Trinh 3/8/2020 17807
					// Số lượng tồn lớn hơn số lượng tồn hết hàng và dương, thì "kinh doanh bình
					// thường"
					if (objPrice.ProductCodeTotalQuantity >= objPrice.WebMinStock
							&& objPrice.ProductCodeTotalQuantity > 0) {
						objPrice.WebStatusId = 4;
					} else // < minstock
					{
						//// Tồn kho tỉnh hiện tại = 0, nhưng tổng tồn kho vẫn lớn hơn mức hết hàng thì
						//// "Chỉ bán online" tại kv này
						// if (objPrice.ProductCodeTotalQuantity > objPrice.WebMinStock)
						// {
						// objPrice.WebStatusId = 3;
						// }
						// Trạng thái ERP là Không kinh doanh hoặc End of Line, thì trạng thái web là
						//// "Không kinh doanh"
						if (objPrice.StatusId == 5 || objPrice.StatusId == 6 || objPrice.StatusId == 0) {
							// objPrice.Price = objPrice.OnlinePrice = 0;
							objPrice.WebStatusId = 1;
						} else if (objPrice.IsProductForthComing && objPrice.ProductArrivalDate != null
								&& objPrice.ProductArrivalDate.after(now)) // Hàng sắp về
						{
							objPrice.WebStatusId = 2;
						} else {
							// if (objProduct != null)
							// {
							// if (objProduct.WebStatusID == 1) //Ưu tiên trạng thái cms
							// {
							// objPrice.WebStatusId = 10;//"Hàng sắp về cms"

							// }
							// else if (objProduct.WebStatusID == 2) //Ưu tiên trạng thái cms
							// {
							// objPrice.WebStatusId = 11;//"Hết hàng tạm thời cms"
							// }
							// else
							// {
							// objPrice.WebStatusId = 5;//het hang tam thoi | giao hang tu 2-7 ngay
							// }

							// }

							objPrice.WebStatusId = 5;

						}
					}
				}
			} else // Giá <= 0
			{
				if (objPrice.IsProductForthComing && objPrice.ProductArrivalDate != null
						&& objPrice.ProductArrivalDate.after(now)) {
					// Cho đặt trước
					objPrice.WebStatusId = 2;
				} else {
					// Không kinh doanh
					objPrice.WebStatusId = 1;
				}
			}
			System.out.println("WebstatusID: "+objPrice.WebStatusId);
			return objPrice;
		} finally {
			objPrice.WebStatusIdOld = objPrice.WebStatusId;
			objPrice.WebStatusId = objPrice.WebStatusIdNew;
		}
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
