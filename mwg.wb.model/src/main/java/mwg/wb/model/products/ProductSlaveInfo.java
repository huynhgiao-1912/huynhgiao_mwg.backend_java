package mwg.wb.model.products;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSlaveInfo {
    public int Id ;
    public String Name ;
    public String ShortName ;
    public String FeatureInfo ;
    public String ShortDescription ;
    public String HtmlDescription ;
    public String StoreIdList ;
    public String Avatar ;
    public int LabelPosition ;
    public String LabelColor ;
    public Date BeginDate ;
    public Date EndDate ;
    public int IsOn ;
    public String YoutubeUrl ;
    public String YoutubeThumbnail ;
    public String AdvertiseImage;
    //public String AdvertiseImageUrl;

    public Date ActivedDefaultFormDate;
    public Date ActivedDefaultToDate;
    public int IsActivedDefault;
    public String OldReturnPolicy;
    public Date OldReturnPolicyDate;
    public String ReturnPolicy;
}
