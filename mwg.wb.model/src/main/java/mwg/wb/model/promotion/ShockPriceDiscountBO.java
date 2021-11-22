package mwg.wb.model.promotion;

public class ShockPriceDiscountBO {
	public int ShockPriceID ;

    /// <summary>
    /// Mức giảm ERP
    /// 
    /// </summary>
    public int DiscountLevelID ;

    /// <summary>
    /// Tỷ lệ phát sinh mã giảm này
    /// </summary>
    public double DiscountpercentRate ;

    /// <summary>
    /// Có tồn tại không?
    /// </summary>
    public int IsExist ;
}
