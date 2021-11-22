package mwg.wb.client.elasticsearch.dataquery;

public class ProductListing {
    public int[] listProductID;
    public int ManufactureId;
    public int ProvinceID;
    public int OrderType;// => 1.sort theo giá KM, sort giá giảm dần
    public int PageIndex;
    public int PageSize;
    public int SiteID;
    public String Lang;
}
