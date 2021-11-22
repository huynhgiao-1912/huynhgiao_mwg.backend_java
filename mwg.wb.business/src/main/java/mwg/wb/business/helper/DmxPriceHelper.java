package mwg.wb.business.helper;

import mwg.wb.business.ProductHelper;
import mwg.wb.common.Utils;
import mwg.wb.model.products.ProductErpPriceBO;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DmxPriceHelper extends APIPriceHelper {

	// for DMX
//	private static List<Integer> categoryAppliances = Arrays.asList(4645, 462, 1922, 1982, 1983, 1984, 1985, 1986, 1987,
//	1988, 1989, 1990, 1991, 1992, 2062, 2063, 2064, 2084, 2222, 2262, 2302, 2322, 2342, 2142, 3305, 5473, 2428,
//	3385, 5105, 7367, 5554, 7498, 7458);
//
//private static List<Integer> categoryTelecom = Arrays.asList(42, 44, 522, 1882, 482, 60, 57, 55, 58, 54, 1662, 1363,
//	1823, 56, 75, 86, 382, 346, 2429, 2823, 2824, 2825, 346);
//private static List<Integer> appliance2142 = Arrays.asList(4928, 4931, 5205, 4930, 5228, 4927, 5225, 5292, 5227,
//	5226, 5230, 5231, 2403, 2402, 5229, 4929, 4932, 5478, 5395, 5354, 6790, 6819, 3187, 3729, 7075);
//private static List<Integer> categoryelectric = Arrays.asList(1942, 1943, 1944, 2002, 2202);
//private static List<Integer> categoryICT = Arrays.asList(5698, 5697, 5693, 4727);

	private static List<Integer> categoryAtStore = Arrays.asList(1363, 7254);
	public static final List<Integer> categoryOlOlHub = Arrays.asList(8765, 366, 365, 1922, 7498, 3385, 1983, 3305, 1982,
			2063, 1984, 1986, 1985, 2062, 2322, 2262, 7684, 7685, 2063, 1987, 1989, 2064, 4645, 1992, 2428, 2222, 1988,
			1990, 5473, 7367, 2302, 7173, 7075, 1062, 7901, 7899, 4697, 5612, 7720, 4706, 49, 8898, 8889, 8899, 8901,
			8878, 8879, 8890, 8903, 8923, 8922, 8765, 8620, 7902, 8621, 9008, 324, 8967, 9000, 9099, 9100, 9098, 2529,
			4929, 9058, 4928, 6012, 9239, 9318, 9338, 9339, 9278, 2162, 8968, 9418, 9599, 9578, 9600, 9598, 9618, 9658,
			9659 ,9660, 4727, 7859,
			7264, 7077, /* đồng hồ thời trang và đồng hồ thông minh */
			54,55,56,57,58,60,75,85,86,1363,1662,1882,1902,2162,2823,2824,3885,4547,4727,4728,5005,5452,6858,6859,6862,
			7922,7923,7924,7925,9041,9118,9119,9262,9298,9320,9321,9341,9358,9386,9398,9458,9499,9518, 7458 /*AccessoryCategory*/
			);

	private static List<Integer> proidsmnn = Arrays.asList(71195, 70328, 68323, 71243, 68323, 68316, 71242, 68316,
			45850, 51795, 63429, 51795, 63515, 71244, 63502, 71245, 86386, 86387, 86150, 86252, 86251, 86385, 86253,
			86151, 75677, 75678, 75679, 75680, 78472, 88595, 78473, 174605, 225854, 174608, 225333, 174610, 225334,
			225335, 45676, 45678, 44635, 63516, 68330, 47581, 47582, 68336, 63493, 193793, 84647, 193794, 84646, 209306,
			75449, 63514, 91757, 63514, 75681, 63471, 72691, 63470, 136144, 136142, 74276, 73122, 71284, 71270, 63469,
			63475, 88654, 88656, 136147, 225713, 74277, 68558, 68557, 68555, 68556, 195397, 195398, 195399, 225323,
			71471, 72202, 49694, 49699, 49710, 49712, 49716, 49717, 49735, 49739, 211748, 211752, 192293, 192294,
			225325, 44758, 44728, 44720, 63425, 47459, 53090, 63428, 72209, 47463, 53105, 63416, 72210, 63422, 63423,
			63518, 63417, 135337, 135417, 47465, 63505, 63415, 72207, 72208, 164834, 225331, 225327, 225330);
	private static List<Integer> provids_notsale_mnn = Arrays.asList(139, 143, 150, 104, 112, 114, 117, 123, 128, 131,
			137, 145, 148, 153, 155, 156, 103, 106, 121, 101, 124, 130, 142, 147, 118, 120, 133, 134, 135, 147, 149, 5);
	private static List<Integer> categoryGiaDungOLOL = Arrays.asList(2222, 4645, 3385, 2063, 1987, 1983, 3305, 1982);
	private static List<Integer> provincesonsales = Arrays.asList(82, 102, 105, 107, 109, 110, 111, 81, 7, 6, 113, 8,
			115, 122, 3, 125, 126, 127, 129, 132, 136, 144, 146, 151, 152, 154);
	private static List<Integer> cate364 = Arrays.asList(5693, 5697, 5698);
	private static List<Integer> AccessoryOffBooking = List.of( 482, 60, 57, 55, 58, 54, 1662, 1363, 1823, 56, 75, 86,
			382, 2429, 2823, 2824, 2825, 3885, 1882, 5005, 5025, 4547, 5452, 85, 6858, 7186, 4728, 4727, 7921, 7922,
			7923, 6859, 6862, 7924, 7925, 6863, 1902, 1363, 6858, 9041, 9118, 9119, 9320, 9298, 9262, 9321, 9398, 9341,
			9386, 9358, 9458);

//private static List<Integer> hc1_NotSellProduct = Arrays.asList(229759, 229767, 229773, 229775);
//private static List<Integer> hc1_SpecialProvince = Arrays.asList(104, 112, 114, 117, 123, 128, 131, 137, 145, 148,
//	153, 155, 156, 103, 106, 121, 101, 124, 130, 142, 147, 118, 120, 133, 134, 135, 147, 149, 5);

	public boolean CheckIsOnlineOnly(ProductErpPriceBO objPrice) {
		var specialSaleProgram = objPrice.specialSale;
		var now = new Date();
		return specialSaleProgram != null && specialSaleProgram.BeginDate.before(now)
				&& specialSaleProgram.EndDate.after(now);
	}

	@Override
	public ProductErpPriceBO ProcessProductStatus(ProductErpPriceBO objPrice) {
		return ProcessProductStatusDMX(objPrice, 2, objPrice.CategoryId, objPrice.quantityOLOLHub);
	}

	@Override
	public ProductErpPriceBO ProcessProductStatusDMX(ProductErpPriceBO objPrice, int siteId, int categoryid,
													 int quantityOLOLHub) {
		if (objPrice == null)
			return null;

		Date now = new Date();
		if(objPrice.ProductCode.equals(("1274062001483"))){
			var codecandebug = 1;
		}
		// debug
//		if (objPrice.ProvinceId == 3) {
//			int a = 0;
//		}

		// Không hiển thị web
		if (!objPrice.IsWebShow && !objPrice.IsProductForthComing) {
			objPrice.WebStatusId = 1;
			return objPrice;
		}
		if(siteId == 1 || siteId == 2){
			objPrice.IsOnlineOnly = CheckIsOnlineOnly(objPrice);
		}

		if (objPrice.Price > 0) {

			if (Arrays.asList(42, 44, 5697).contains(categoryid)) {
				if (objPrice.IsOnlineOnly) {
					// //https://docs.google.com/document/d/1nROO-ZGTGnZqQpK1wrmcsPLCUinvsecYptm9Jcq6gI0/edit#
					if (categoryid == 44) {
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

			// gia dụng lắp đặt online only không quan tâm tồn kho
			if (categoryid == 5697 && objPrice.IsOnlineOnly) {
				if (objPrice.Quantity > 0)
					objPrice.WebStatusId = 4;
				else
					objPrice.WebStatusId = 11;
				return objPrice;
			}
			if (categoryGiaDungOLOL.contains(categoryid) && objPrice.IsOnlineOnly && objPrice.isSetupProduct) {
				if (objPrice.Quantity > 0)
					objPrice.WebStatusId = 4;
				else
					objPrice.WebStatusId = 11;
				return objPrice;
			}
			// yeu cau: https://bit.ly/2US9T6G
			if (categoryOlOlHub.contains(categoryid) && objPrice.IsOnlineOnly &&
					(quantityOLOLHub != -1 || categoryid == 7264))// -1
																										// :flag
																										// ignore
																										// processing
			{
				if(categoryid == 7264 ){
					// Đối vs cate 1274 nếu sp có khai báo OLOL thì auto còn hàng k quan tâm còn tồn kho hub hay không
					// chị Phương yêu cầu
					objPrice.WebStatusId = 4;
				} else {
					if (quantityOLOLHub > 0) {
						objPrice.WebStatusId = 4;
					} else {
						objPrice.WebStatusId = 5;
					}
				}
				return objPrice;
			}

//            #endregion
//            #region máy nước nóng trực tiếp chỉ kinh doanh 2 số tỉnh
			if (proidsmnn.contains((int) objPrice.ProductId) && provids_notsale_mnn.contains(objPrice.ProvinceId)) {
				objPrice.WebStatusId = 1;
				return objPrice;
			}
//            #endregion
//            #region may nuoc nong dai thanh chỉ kinh doanh 1 so tinh
			// https://docs.google.com/document/d/1kIb1jlJvaGTltLl1RY2yg-DVkr79VYbQ_LIybT4-M6E/edit

			if (categoryid == 1962 && objPrice.manufacturerID == 19778
					&& !provincesonsales.contains(objPrice.ProvinceId)) {
				objPrice.WebStatusId = 1;
				return objPrice;
			}
//            #endregion

//            #region phan mem con tồn kho hệ thống => còn hàng
			if (categoryid == 85 && objPrice.TotalQuantity > 0) {
				objPrice.WebStatusId = 4;
				return objPrice;
			}
//            #endregion

//			Anh Trùy yêu cầu 20201208
//			- Đối với NH 5697 - Màn hình, máy tính để bàn, 5698 - Máy tính nguyên bộ, 5693 - Máy in, Fax
//				và DeliveryVehicles = 1 (sản phẩm chỉ bán tại siêu thị)
			if (cate364.contains(categoryid) && objPrice.DeliveryVehicles == 1) {
				if (objPrice.is364province) { // Tỉnh thành có siêu thị kinh doanh sản phẩm
					if (objPrice.ProductCodeTotalQuantity > 0) {
						if (objPrice.ProductCodeQuantity > 0) { // Hệ thống còn hàng, tỉnh thành còn hàng => webstatus =
																// 4 (còn
							// hàng)
							objPrice.WebStatusId = 4;
						} else { // Hệ thống còn hàng, tỉnh thành hết hàng => webstatus=11 (chuyển hàng)
							objPrice.WebStatusId = 11;
						}
					} else {
						objPrice.WebStatusId = 1;
					}
				} else { // Tỉnh thành ko có siêu thị kinh doanh sản phẩm => Ngưng kinh doanh
					objPrice.WebStatusId = 1;
				}
				return objPrice;
			}

			// if (objPrice.IsBooking)
			// {
			// objPrice.WebStatusId = 8;
			// }
			// else
			{
				if (objPrice.TotalQuantity > objPrice.WebMinStock) {
					objPrice.WebStatusId = 4;
					if (objPrice.Quantity <= 0 && objPrice.Quantity + objPrice.CenterQuantity <= 0) {
						if (objPrice.TotalQuantityRelateProvince > 0
								|| (objPrice.TotalSampleQuantity > 0 && objPrice.StatusId == 8/* trạng thái đa dạng */))
							objPrice.WebStatusId = 11;
						else
							objPrice.WebStatusId = 5;
					}
				}
				// End of Line
				else if (objPrice.StatusId == 6) {
					objPrice.WebStatusId = 7;
				}
				// Trạng thái ERP là Không kinh doanh thì trạng thái web là "Không kinh doanh"
				else if (objPrice.StatusId == 5 || objPrice.StatusId == 0) {
					objPrice.WebStatusId = 1;
				} else if (objPrice.IsProductForthComing && objPrice.ProductArrivalDate != null
						&& objPrice.ProductArrivalDate.after(now)) // Hàng sắp về
				{
					objPrice.WebStatusId = 2;
					return objPrice;
					
				} else // Hết hàng tạm thời
				{
					objPrice.WebStatusId = 5;
				}
			}
			// hardcode
			// xử lý đặt trước

			if (!ProductHelper.itCategory.contains(categoryid)
					&& !AccessoryOffBooking.contains(categoryid)
					&& !ProductHelper.teleCategory.contains(categoryid)
					&& objPrice.IsBooking && objPrice.WebStatusId != 4) {
				objPrice.WebStatusId = 8;
			}
			if ((categoryid == 6552 || objPrice.ProductId == 206233) && objPrice.ProvinceId != 3) {
				objPrice.WebStatusId = 1;
			}
			if (objPrice.WebStatusId == 8 && objPrice.ProductId == 202922 && objPrice.ProvinceId != 3) {
				objPrice.WebStatusId = 1;
			}
			// Sản phẩm chỉ bán tại siêu thị nếu hết tồn tỉnh thành thì về tạm hết hàng
			if ((categoryAtStore.contains(categoryid) || objPrice.DeliveryVehicles == 1) && objPrice.Quantity == 0)
				objPrice.WebStatusId = 5;
		} else // Giá <= 0
		{
			// ngưng kinh doanh
			if (objPrice.StatusId == 6)// endoffline
			{
				objPrice.WebStatusId = 7;
			}
			// Hàng sắp về
			else if (objPrice.IsProductForthComing && objPrice.ProductArrivalDate != null
					&& objPrice.ProductArrivalDate.after(now)) {
				objPrice.WebStatusId = 2;
			} else {
				// Không kinh doanh
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
		try {
			if (listPrice == null || listPrice.size() == 0) {
				listPrice = null;
				return null;
			}
//			Date dts = Utils.GetCurrentDate();

			ProductErpPriceBO objPrice = null;
			listPrice = listPrice
					.stream().filter(p -> p != null).sorted(Comparator.comparing(ProductErpPriceBO::getQuantity)
							.reversed().thenComparing(ProductErpPriceBO::getPrice).reversed())
					.collect(Collectors.toList());

			if (listPrice.size() == 1) {
				objPrice = listPrice.stream().findFirst().get();
			} else {

				objPrice = listPrice.stream().filter(p -> p.WebStatusId == 4 && p.ProductCodeTotalQuantity > 0)
						.findFirst().orElse(null);
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
		} catch (Exception e) {
			return new ProductErpPriceBO();
		}

	}

}
