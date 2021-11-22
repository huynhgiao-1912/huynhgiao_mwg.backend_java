package mwg.wb.model.promotion;

public class NewGiftBHX {
	/// <summary>
    /// Mã sản phẩm trên web
    /// </summary>
    public int Id;

    /// <summary>
    /// Tên sản phẩm
    /// </summary>
    public String Name;

    /// <summary>
    /// Product-code của sản phẩm
    /// </summary>
    public String ProductCode;

    /// <summary>
    /// Số lượng quà tặng này khi được tặng. Mặc định là 1.
    /// </summary>
    public int Quantity;

    /// <summary>
    /// Nếu quà hết tồn kho, sẽ tặng tiền cho khách
    /// </summary>
    public double ReturnValue;

    /// <summary>
    /// Mức giảm % hoặc tiền dùng trong khuyến mãi bán kèm
    /// </summary>
    public String Discount;

    /// <summary>
    /// Giá bán kèm cố định
    /// </summary>
    public double FixedPrice;
}
