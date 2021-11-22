package mwg.wb.model.promotion;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import mwg.wb.model.products.ProductErpPriceBO;

public class ComboBHX {
	public String Id;
    public int SubGroupId;
    public int PromotionId;
    public Date BeginDate;
    public Date EndDate;
    public List<ComboDetailBHX> Details;
    public ProductErpPriceBO Price;

    public ComboBHX()
    {
    }

    public ComboBHX(String id,
        int subGroupId,
        int promotionId,
        Date beginDate,
        Date endDate,
        List<ComboDetailBHX> details)
    {
        Id = id;
        SubGroupId = subGroupId;
        PromotionId = promotionId;
        BeginDate = beginDate;
        EndDate = endDate;
        Details = details;

        UpdatePrice();
    }

    public void UpdatePrice()
    {
        if (Details == null || Details.size() == 0)
        {
            return;
        }

        for (var d : Details)
        {
            switch (d.ComboType)
            {
                case 1:
                    // Giá cố định
                    d.ComboPrice = d.Value * (1 + d.Vat / 100);
                    break;
                case 2:
                    // Giảm theo %                                            
                    d.ComboPrice = d.Price * (1 - d.Value / 100);
                    break;
                default:
                    // Giảm theo số tiền                        
                    d.ComboPrice = d.Price - (d.Value * (1 + d.Vat / 100));
                    break;
            }
            d.ComboPrice = Math.round(d.ComboPrice);
            d.ComboPrice = d.ComboPrice == 0 ? 1 : d.ComboPrice;
        }

        double price = Math.round(Details.stream().map(d -> d.Quantity * d.ComboPrice).collect(Collectors.summingDouble(Double::doubleValue)));
        if (price % 10 == 9)
        {
            // Cộng lên 1đ cho nó tròn tiền những sản phẩm bị lẽ 9đ, 99đ, 999đ...
            price++;
        }
        double hisPrice = Math.round(Details.stream().map(d -> d.Quantity * (d.ComboPrice < 10 ? 0 : d.Price)).collect(Collectors.summingDouble(Double::doubleValue)));

        // Tồn kho
        Comparator<ComboDetailBHX> comparator = Comparator.comparing(ComboDetailBHX::getStockQuantity);
        int stockQuantity = Details.stream().min(comparator).get().getStockQuantity();

        // Nếu combo có giới hạn số suất, mà số suất bán ra nhiều hơn số quy định thì xem như tạm hết hàng
        if (Price != null && Price.IsLimit && Price.MinStock <= Price.CenterQuantity)
        {
            stockQuantity = 0;
        }

        Price = new ProductErpPriceBO();
        Price.ProductCode = Id;
        Price.WebStatusId = stockQuantity > 0 ? 4 : 5;
        Price.Price = price;
        Price.HisPrice = hisPrice;
        Price.ProductCodeTotalQuantity = stockQuantity;
        Price.Quantity = stockQuantity;
        Price.TotalQuantity = stockQuantity;
        Price.StoreID = PromotionId;
        Price.RowNumber = SubGroupId;
        Price.IsShowHome = true;
        Price.IsShowWeb = true;
        Price.IsLimit = Price.IsLimit;  // Có giới hạn
        Price.MinStock = Price.MinStock;    // Số lượng giới hạn
        Price.CenterQuantity = Price.CenterQuantity;     // Số suất đã bán
    }
}