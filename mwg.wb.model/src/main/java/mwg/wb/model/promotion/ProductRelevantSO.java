package mwg.wb.model.promotion;

public class ProductRelevantSO {
	/// <summary>
    /// Id sản phẩm khuyến mãi
    /// </summary>
    public int GiftId;
    /// <summary>
    /// Thông tin danh sách khuyến mãi liên quan
    /// </summary>
    public RelevantInfo[] PromotionInfos;
    /// <summary>
    /// Danh sách các sản phẩm combo có liên quan
    /// </summary>
    public RelevantInfo[] ComboInfos;
    /// <summary>
    /// Danh sách các khuyến mãi theo bill có liên quan đến sản phẩm
    /// </summary>
    public RelevantInfo[] BillPromotionInfos;
}
