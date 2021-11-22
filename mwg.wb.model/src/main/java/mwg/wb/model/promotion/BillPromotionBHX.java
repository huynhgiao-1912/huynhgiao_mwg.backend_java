package mwg.wb.model.promotion;

import java.util.List;
import java.util.stream.Collectors;

import mwg.wb.common.Utils;

public class BillPromotionBHX extends NewPromotionBHX {
	/// <summary>
    /// <para>Giá trị mỗi bước nhảy theo giá trị đơn hàng</para>
    /// <para>Ví dụ: Giá trị đơn hàng mỗi 50.000đ sẽ được 1 lần áp dụng khuyến mãi</para>
    /// </summary>
    public double Step;

    /// <summary>
    /// <para>Số bước nhảy tối đa được phép</para>
    /// <para>Ví dụ: Mỗi 50.000đ sẽ được áp dụng KM 1 lần, nhưng tối đa không quá 5 lần</para>
    /// </summary>
    public double Ceiling;

    /// <summary>
    /// <para>Tặng quà hoặc giảm giá khi hết hàng: 0</para>
    /// <para>Bán kèm hoặc giảm giá khi hết hàng: 1</para>
    /// <para>Giảm giá: 3</para>
    /// <para>Tặng quà: 4</para>
    /// <para>Bán kèm: 5</para>
    /// </summary>
    public int Type;

    /// <summary>
    /// <para>Chỉ cần mua/nhận một trong các sản phẩm - Single-choose : 1</para>
    /// <para>Phải mua/nhận tất cả sản phẩm - All-choose : 3</para>
    /// <para>Lựa chọn ít hơn hoặc bằng X sản phẩm - Multi-choose : 2</para>
    /// </summary>
    public int ChooseType;

    /// <summary>
    /// Danh sách sản phẩm có thể áp dụng khuyến mãi
    /// </summary>
    public List<String> ProductCodes;


    /// <summary>
    /// Danh sách các nhóm sản phẩm (POS) có thể áp dụng khuyến mãi
    /// </summary>
    public List<String> ProductGroups;

    /// <summary>
    /// Khuyến mãi có được set random hay không
    /// </summary>
    public Boolean IsRandom;

    /// <summary>
    /// Danh sách quà tặng/bán kèm
    /// </summary>
    public List<NewGiftBHX> Gifts;

    /// <summary>
    /// Danh sách kho áp dụng
    /// </summary>
    public List<Integer> StoreIds;

    /// <summary>
    /// Kiểm tra xem khuyến mãi hợp lệ hay không
    /// </summary>
    /// <param name="parameters">
    /// <para>Tập hợp sản phẩm chính trong giỏ hàng</para>
    /// <para>Cấu trúc: ProductCode|POSGroup|Total</para>
    /// </param>
    /// <returns></returns>
    public Boolean IsValid(Object... parameters)
    {
        // Kiểm tra thời gian hiệu lực
        var isValid = IsValid(parameters);
        if (!isValid)
            return false;

        // Tính tổng tiền hàng
        var billTotal = CalculateBillTotal(parameters);
        if (billTotal < 1)
            return false;

        // Số lần khuyến mãi được áp dụng
        var stepCount = CountStep(billTotal);
        if (stepCount < 1)
            return false;

        return true;
    }

    /// <summary>
    /// Tính tổng tiền hàng hợp lệ
    /// </summary>
    /// <param name="parameters">
    /// <para>Tập hợp sản phẩm chính trong giỏ hàng</para>
    /// <para>Cấu trúc: ProductCode|POSGroup|Total</para>
    /// </param>
    /// <returns></returns>
    public double CalculateBillTotal(Object... parameters)
    {
        if (parameters == null || parameters.length < 1)
            return 0;

        // Tiền hàng của các sản phẩm có khuyến mãi
        double billTotal = 0;
        for (var p : parameters)
        {
            // ProductCode|POSGroup|Total
            var splited = Utils.toString(p).split("|");

            // Cộng dồn tiền hàng
            if (ProductCodes.contains(splited[0]) || ProductGroups.contains(splited[1]))
            {
                billTotal += Utils.toDouble(splited[2]);
            }

            // Nếu số tiền hàng vượt quá mức trần thì lấy theo mức trần
            if (billTotal >= Ceiling && Ceiling > 0)
            {
                billTotal = Ceiling;
                break;
            }
        }

        return billTotal;
    }

    /// <summary>
    /// Trả ra số lần khuyến mãi được áp dụng theo giá trị đơn hàng
    /// </summary>
    /// <param name="billTotal">Tổng tiền hàng hợp lệ</param>
    /// <returns></returns>
    private int CountStep(double billTotal)
    {
        // Nếu giá trị đơn hàng vượt quá mức trần thì lấy theo mức trần
        if (billTotal > Ceiling && Ceiling > 0)
            billTotal = Ceiling;

        // Số lần khuyến mãi được áp dụng
        return (int)(billTotal / Step);
    }

    /// <summary>
    /// Số lượng quà tặng/mua kèm tối đa
    /// </summary>
    /// <param name="productCode">ProductCode của quà tặng hoặc sản phẩm mua kèm</param>
    /// <param name="parameters">
    /// <para>Tập hợp sản phẩm chính trong giỏ hàng</para>
    /// <para>Cấu trúc: ProductCode|POSGroup|Total</para>
    /// </param>
    /// <returns></returns>
    public int MaxQtyOfGift(String productCode, Object[] parameters)
    {
        var gift = Gifts.stream().filter(g -> g.ProductCode == productCode).limit(1).collect(Collectors.toList());
        if (gift == null)
            return 0;

        // Tính tổng tiền hàng
        var billTotal = CalculateBillTotal(parameters);
        if (billTotal < 1)
            return 0;

        // Số lần khuyến mãi được áp dụng
        var stepCount = CountStep(billTotal);

        return gift.get(0).Quantity * stepCount;
    }
    /*
     * Lưu tạm Promotion Type
     */
    public int PromotionType;
}
