package mwg.wb.pkg.promotion.helper.bhx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

import mwg.wb.business.PriceHelper;
import mwg.wb.common.Utils;
import mwg.wb.model.promotion.GiftBHX;
import mwg.wb.model.promotion.PromotionBHX;


public class PromotionBHXHelper {
	public static double CalculateRealDiscountValue(ArrayList<PromotionBHX> promotions,
			 long productId, String productCode, int siteId, int provinceId, double price, int storeid,
			 PriceHelper priceHelper) throws Throwable
	{
		try
        {
            var pdate = Utils.GetCurrentDate();

            if (Utils.StringIsEmpty(productCode) || promotions == null || promotions.size() == 0 || price == 0)
                return 0;

//            ProductSvc.ProductErpPriceBO productPrice = null;
//
//            if (productCode.ToLower().StartsWith("cb") || productCode.ToLower().StartsWith("pos") || productCode.ToLower().StartsWith("web"))
//            {
//                var combo = GetCombo(productCode, siteId, iCached);
//                if (combo != null)
//                {
//                    productPrice = combo.Price;
//                }
//            }
//            else
//            {
//                string priceKey = string.Format(DataKey.ProductModule.PCI.PRICE_DETAIL_BYSITE, provinceId, productId,
//                    productCode, siteId);
//                productPrice = iCached.Get<ProductSvc.ProductErpPriceBO>(priceKey);
//            }
//
//            if (productPrice == null ||
//                productPrice.Price == 0 ||
//                productPrice.ProductId != productId ||
//                productPrice.ProductCode != productCode)
//                return 0;

            // Tổng giá trị KM tính theo %
            double promotionTotalValue = 0;
                        
            for (PromotionBHX promo : promotions) {
            	// Chỉ tính cho điều kiện áp dụng với SL 1               
				if (promo.QuantityCondition != 1)
                {
                    continue;
                }

                // Giá trị giảm tiền
                var valueByDiscount = promo.IsPercent ? promo.Value : (promo.Value / price) * 100;

                // Giá trị giảm bởi quà
                double valueByGifts = 0;
                
                //tính giá trị quà km
                if(promo.GiftBHX!= null && promo.GiftBHX.length > 0)
                {                	
            		// Tổng giá trị quà tặng khuyến mãi
            		//nếu km và thì lấy tất cả, ngược lại lấy 1 cái cao nhất
                	if(promo.GiftType) {
	            		for (GiftBHX giftBHX : promo.GiftBHX) {
	            			var priceGift = priceHelper.getDefaultPriceStrings(giftBHX.ProductID, 11, provinceId, 644, "vi-VN");
	            			if(priceGift!= null)
	            			{
	            				valueByGifts += priceGift.Price;
	            			}
						}
                	} else {
                		double maxprice = 0;
                		for (GiftBHX giftBHX : promo.GiftBHX) {
	            			var priceGift = priceHelper.getDefaultPriceStrings(giftBHX.ProductID, 11, provinceId, 644, "vi-VN");
	            			if(priceGift!= null && maxprice < priceGift.Price)
	            			{
	            				maxprice = priceGift.Price;
	            			}
						}
                		valueByGifts = maxprice;
                	}
                }     
                valueByGifts =  (100 * valueByGifts / price);

                switch (promo.Type)
                {
                    case Discount:
                        promotionTotalValue += valueByDiscount;
                        break;
                    case Gift:
                        promotionTotalValue += valueByGifts;
                        break;
                    case AndDiscountGift:
                        promotionTotalValue += valueByDiscount + valueByGifts;
                        break;
                    case OrDiscountGift:
                        promotionTotalValue += Math.max(valueByDiscount, valueByGifts);
                        break;
                }
            }

           // Console.WriteLine("TRACE: CalculateRealDiscountValue(" + productId + ") = (" + (DateTime.Now - pdate).TotalMilliseconds + ")");
            return Math.round(promotionTotalValue);
        }
        catch (Exception ex)
        {
            return 999;
        }
	}
	
//	public static Combo GetCombo(string comboId, int siteId, ICached iCached)
//    {
//        var comboKey = string.Format(DataKey.ProductModule.Product.COMBO_DETAIL, comboId, siteId);
//        var combo = iCached.Get<Combo>(comboKey);
//        return combo;
//    }
}
