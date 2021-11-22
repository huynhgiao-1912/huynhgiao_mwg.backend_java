package mwg.wb.model.promotion;

import java.util.Date;

public class RelevantInfo {
	/// <summary>
    /// Có thể là  promotionId, comboId, billPromotionId
    /// </summary>
    public int Id;
    public Date BeginDate;
    public Date EndDate;
    public String StoreIds;
    /// <summary>
    /// ID sản phẩm có liên quan, trường hợp là KM theo bill thì nó chính là ID KM
    /// </summary>
    public int MainProductId;
    public String MainProductCode;
    /// <summary>
    /// 0: KM sản phẩm, 1: KM theo bill, 2: KM combo
    /// </summary>; { get; set; }
    public int Type;
}
