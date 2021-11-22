package mwg.wb.model.promotion;

import mwg.wb.model.products.ProductBO;

public class GiftBHX {
	/// <summary>
    /// Thông tin về sản phẩm quà tặng
    /// </summary>
    public ProductBO ProductInfo;

    /// <summary>
    /// Số lượng quà tặng còn tồn kho
    /// </summary>
    private int StockQuantity;

    /// <summary>
    /// Số lượng quà sẽ tặng khi thỏa điều kiện quà tặng.
    /// <example>VD: Mua 3sp A tặng 2sp B --> AppliedQuantity = 2</example>
    /// </summary>
    public int AppliedQuantity;

    /// <summary>
    /// Điều kiện áp dụng quà tặng: Số lượng sp chính phải mua để nhận được món quà tương ứng
    /// </summary>
    private int QuantityCondition;

    public int ProductID;

    public String ProductCode;

    public String Name;

    public String Avatar;

    public String Unit;

    public String Url;

	public int getQuantityCondition() {
		return QuantityCondition;
	}

	public void setQuantityCondition(int quantityCondition) {
		QuantityCondition = quantityCondition;
	}

	public int getStockQuantity() {
		return StockQuantity;
	}

	public void setStockQuantity(int stockQuantity) {
		StockQuantity = stockQuantity;
	}
}
