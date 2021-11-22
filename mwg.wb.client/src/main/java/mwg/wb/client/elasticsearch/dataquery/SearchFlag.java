package mwg.wb.client.elasticsearch.dataquery;

import com.google.common.base.Strings;
import mwg.wb.common.Utils;
import org.apache.commons.lang3.ArrayUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.script.Script;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.*;

public enum SearchFlag {


    /**
     * Sản phẩm có scenario = 1 & tổng km giảm tiền > 10%
     */
    PROMOTION_DISCOUNTPERCENT_GT10 {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            query.must(boolQuery()//
                    .should(termQuery("Scenario", 1))//
                    .should(rangeQuery("PromotionDiscountPercent").gt(10))//
            );
        }
    },

    /**
     * ExtensionObject = 30 : Gía sau khuyến mãi
     */
    PRICE_AFTER_PROMOTION,
    /**
     * Phụ kiện có scenrario
     */
    ACCESSORY_SCENARIO,

    /**
     * Giam gia khuyến mãi : ExtentionObject = 49
     */
    PROMOTION_DISCOUNT {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            long nowepoch = System.currentTimeMillis();
            query.must(termQuery("PromotionSoList.discountvalue", 49))
                    .must(rangeQuery("PromotionSoList.begindate").lte(nowepoch))
                    .must(rangeQuery("PromotionSoList.enddate").gte(nowepoch));
        }
    },

    /**
     * - Không lấy đồng hồ cặp : ExtentionObject : 7264 : SITE 2
     */
    ISNOTCOUPLEWATCH {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            query.must(termQuery("IsCoupleWatch", false));
            query.mustNot(existsQuery("AddtionalField1"));
        }
    },

    COUPLEWATCH {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            query.must(termQuery("IsCoupleWatch", true));
//          query.must(existsQuery("AddtionalField1")); ?
        }
    },

    PROMOTION_DISCOUNT_PERCENT {

    },
    /**
     * sản  phẩm mới ra mắt : ExtentionObject = 1
     */
    PRODUCTS_NEW {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            query.must(rangeQuery("DateCreated").gte(new Date(System.currentTimeMillis() - (86400000l * 60))));
        }
    },
    /**
     * - sản phẩm trả góp : ExtentionObject = 4,  không hiện ra các sản phẩm sắp ra mắt, hàng sắp về, ngừng kinh doanh
     */
    PRODUCT_INSTALLMENT {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            Date now = new Date();
            query.must(termQuery("IsPayment", 1)).must(rangeQuery("PaymentFromDate").lte(now))
                    .must(rangeQuery("PaymentToDate").gte(now));
            query.mustNot(termsQuery("CmsProductStatus", new int[]{1,2}));

        }
    },

    /**
     * - Sản phẩm độc quyền : ExtentionObject = 3
     */
    PRODUCT_EXCLUSIVE {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            query.must(termsQuery("PropStr", "prop21375_153170"));
        }
    },

    /**
     * - Gía rẻ online : ExtentionObject = 2
     */
    ONLINE_REASONABLE_PRICE {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            Date now = new Date();
            query.must(termQuery("Scenario", 1)).must(rangeQuery("ScenarioFromDate").lte(now))
                    .must(rangeQuery("ScenarioToDate").gte(now));
        }
    },

    /**
     * - Lấy trạng thái webstatus : ExtentionObject = 60
     */
    GET_WEBSTATUS {
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            String webstatus = "Prices.WebStatusId_" + qry.ProvinceId;
            query.must(termsQuery(webstatus, new int[]{3, 4}));
        }
    },
    GOOD_EXPERIENCE_PAGE {

    },
    ISPAYMENT {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            //IsPayment
            query.must(termQuery("IsPayment", 1));
            query.must(rangeQuery("PaymentFromDate").lte(new Date()));
            query.must(rangeQuery("PaymentToDate").gte(new Date()));
        }
    },
    SEARCH_2020 {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
//            if (qry.SearchType == SearchType.SEARCH2020) {
            if (qry.CategoryId == 44) {
                query.must(boolQuery()
                        .should(termQuery("Scenario", 1))
                        .should(rangeQuery("Prices.PromotionDiscountPercent_" + qry.ProvinceId).gte(5))
                );
            }
            else {
                if (appliance2142Categories.contains(qry.CategoryId)) {
                    query.must(boolQuery()
                            .should(termQuery("Scenario", 1))
                            .should(rangeQuery("Prices.PromotionDiscountPercent_" + qry.ProvinceId).gte(30))
                    );
                } else if (listCateDungCu.contains(qry.CategoryId) || qry.CategoryId == 2162) {
                    query.must(boolQuery()
                            .should(termQuery("Scenario", 1))
                            .should(rangeQuery("Prices.PromotionDiscountPercent_" + qry.ProvinceId).gte(10))
                    );
                } else {

                    query.must(boolQuery()
                            .should(termQuery("Scenario", 1))
                            .should(rangeQuery("Prices.PromotionDiscountPercent_" + qry.ProvinceId).gte(10))
                            .should(boolQuery().must(termsQuery("CategoryID", newApplianceCategories))
                                    .must(termQuery("HasPromotion", 1)))
                    );
                }
                query.must(scriptQuery(new Script(" doc['Prices.Price_" + qry.ProvinceId + "'].size() > 0 " +
                        "&& doc['Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId + "'].size() > 0 " +
                        "&& (doc['Prices.Price_" + qry.ProvinceId + "'].value * 0.91) >= doc['Prices.PriceAfterPromotion_" +
                        qry.ProvinceId + "_" + qry.SiteId + "'].value ")));
            }
            // không  loại bỏ sp km > 60% nữa
//            query.mustNot(rangeQuery("Prices.PromotionDiscountPercent_" + qry.ProvinceId).gte(60));

//            }
        }

    },

    SCENARIO {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            if (accessoryCategory.contains(qry.CategoryId)) {
                var now = new Date();
                query.must(termQuery("Scenario", 1));
                query.must(rangeQuery("ScenarioFromDate").lte(now));
                query.must(rangeQuery("ScenarioToDate").gte(now));
            }

        }

    },
    MAINKEYWORD {

    },
    MAINKEYWORD_KEYWORD {

    },
    ISSEARCHLIKE {

    },
    ISNEW {

    },
    ISSEARCHONLINEONLY {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            query.must(termQuery("IsOnlineOnly", true));
        }
    },
    ISMONOPOLY {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            query.must(termQuery("IsMonopolyLabel", true));
        }
    },
    ISMONOPOLY_CMS {
        @Override
        //IsSmart
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            query.must(termQuery("IsMonopolyCms", true));
        }
    },
    ISFEATURE {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            query.must(termQuery("IsFeature", 1));

        }
    },
    PAYMENT_NOT_ONLINEONLY {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            query.must(scriptQuery(new Script("(doc['Prices.PriceAfterPromotion_'" + qry.ProductID + "_2].value)  > 1200000")));
            query.must(termQuery("IsPayment", 1));
            query.must(rangeQuery("PaymentFromDate").lte(new Date()));
            query.must(rangeQuery("PaymentToDate").gte(new Date()));
            query.must(termQuery("IsOnlineOnly", false));
        }
    },
    FEATURETYPE {

    },
    SEARCH {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, "4", "5", "1"));

        }
    },
    CATE {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            if (qry.CategoryId == 7264 && Strings.isNullOrEmpty(qry.Keyword))
                // bo het hang tam thoi:5 va het hang: 2 trang nganh hang
                query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, new int[]{3, 4}));
            else
                query.must(termsQuery("Prices.WebStatusId_" + qry.ProvinceId, new int[]{2, 3, 4}));
            query.must(termQuery("Prices.WebStatusId_" + qry.ProvinceId, 1));
        }
    },
    NEWPRODUCT_60D {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            if (qry.WebStatusIDList != null && !ArrayUtils.contains(qry.WebStatusIDList, 1)) {
                ArrayUtils.add(qry.WebStatusIDList, 1);
            }
            // ngày ra mắt trong vòng 60 ngày
            query.must(rangeQuery("DateCreated").gte("now-60d/d").lte("now/d"));
        }
    },
    NEWPRODUCT_THISYEAR {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            //năm ra mắt sp là năm hiện tại
            query.must(termQuery("AnnouncedYear", Calendar.getInstance().get(Calendar.YEAR)));
        }
    },
    NEWPRODUCT_LASTYEAR {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            // năm ra mắt  là năm trước hiện tại 1 năm
            query.must(termQuery("AnnouncedYear", Calendar.getInstance().get(Calendar.YEAR) - 1));
        }
    },
    EXCEPT_PRODUCT {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            query.must(termsQuery("ProductID", arrproductid));
        }
    },
    /**
     * Phụ kiện chính hãng
     */
    ACCESSORY_GENUINE {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            var listcate = new int[]{54, 55, 56, 57, 58, 75, 86, 1363, 1622, 1882, 1902, 2162, 3885,
                    4547, 4727, 4728, 6858, 6859, 6862, 6863, 7922, 7923, 7924, 7925, 9499, 9458, 9118};
            query.must(boolQuery()//
                    .should(termsQuery("CategoryID", listcate))//
                    .should(boolQuery().must(termQuery("CategoryID", 60))
                            .must(termQuery("PropStr", "prop14609_84641")))//
                    .should(boolQuery().must(termQuery("CategoryID", 1662))
                            .must(termQuery("PropStr", "prop24738_168651")))//
            );
        }
    },

    ACCESSORY_HIGHLIGHT { // phụ kiện nổi bật

        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            var listcate = new int[]{60, 3885, 1662, 56, 7922, 6858, 6862, 7924, 7925, 6863};
            Date now = new Date();
            query.must(boolQuery()
                    .should(boolQuery()
                            .must(rangeQuery("Prices.PromotionDiscountPercent_" + qry.ProvinceId).gte(30)))
                    .should(boolQuery()//
                            .must(termQuery("Scenario", 1))//
                            .must(rangeQuery("ScenarioFromDate").lte(now)) //
                            .must(rangeQuery("ScenarioToDate").gte(now)))
            );
            
            query.must(termsQuery("CategoryID", accessoryCategory));
            query.mustNot(termsQuery("CategoryID", listcate));
            if (qry.ProductIdEnumerable != null && qry.ProductIdEnumerable.length > 0) {
                query.mustNot(termsQuery("ProductID", qry.ProductIdEnumerable));
            }
        }
    },
    ACCESSORY_SHOCK_PRICE {// PHỤ KIỆN GIÁ SỐC
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            if (qry.CategoryId == 57 || qry.CategoryId == 58 || qry.CategoryId == 54 || qry.CategoryId == 2162) {

                query.must(
                        boolQuery()
                                .should(boolQuery()
                                        .must(rangeQuery("Prices.PromotionDiscountPercent_" + qry.ProvinceId).gte(20)))
                        .should(termQuery("IsPromotionSaving", true))//
                        .should(boolQuery()//
                                .must(termQuery("Scenario", 1))//
                                .must(rangeQuery("ScenarioFromDate").lte("now/d")) //
                                .must(rangeQuery("ScenarioToDate").gte("now/d")))
                );

                //query.must(scriptQuery( new Script(" (doc['Prices.Price_" + qry.ProvinceId + "'].value * 0.91) >= doc['Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId + "'].value ")));
            }
        }
    },
    ACCESSORY_ONLY{
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            query.must(termsQuery("CategoryID", AccessoryCategory));
        }
    },
    /*
    * Loại bỏ đồng hồ trẻ em
    * */
    ISNOTKIDWATCH{
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            //Đồng hồ thông minh
            if(qry.CategoryId == 7077){
                query.mustNot(termQuery("PropStr","prop26638_185103"));

            }else
                //Đồng hồ thời trang
                if(qry.CategoryId == 7264){
                query.mustNot(termQuery("PropStr","prop18799_135220"));

            }
        }
    },
    ISMULTIAIRCONDITIONER{
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry) {
            //multi máy lạnh
            if(qry.CategoryId == 2002){
//                query.must(
//                        boolQuery()
//                                .should(boolQuery()
//                                        .must(rangeQuery("PresentProductID").gte(0))
//                                        .mustNot(termQuery("Prices.WebStatusId_" + qry.ProvinceId,8)))
//                                .should(rangeQuery("PresentProductID").gte(0))
//                );
                query.must(scriptQuery(
                        new Script("(doc['PresentProductID'].value > 0 && doc['Prices.WebStatusId_"+qry.ProvinceId+"'].value != 8) || (doc['PresentProductID'].value == 0)")
                ));
            }else if(qry.CategoryId == 0){
//                query.must(
//                        boolQuery()
//                                .should(boolQuery()
//                                        .must(termQuery("CategoryID", 2002))
//                                        .must(rangeQuery("PresentProductID").gte(0))
//                                        .mustNot(termQuery("Prices.WebStatusId_" + qry.ProvinceId,8)))
//                                .should(rangeQuery("PresentProductID").gte(0))
//                                .should(boolQuery().mustNot(termQuery("CategoryID", 2002)))
//                );

                query.must(scriptQuery(
                        new Script("(doc['CategoryID'].value == 2002 && doc['PresentProductID'].value > 0 && doc['Prices.WebStatusId_"+qry.ProvinceId+"'].value != 8) || (doc['PresentProductID'].value == 0) || (doc['CategoryID'].value != 2002)")
                ));
            }
        }
    },

    COMBINEDPRODUCT {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry){
            if (qry.WebStatusIDList != null && !ArrayUtils.contains(qry.WebStatusIDList, 1)) {
                qry.WebStatusIDList = ArrayUtils.add(qry.WebStatusIDList, 1);
            }
            if(qry.CategoryId == 42) {
                var present = "doc['Prices.PresentStatus_"+qry.ProvinceId+"']";

                //query.must(termQuery("PresentProductID", 0)); => full cate 42

                //mở gộp sp cho tất cả hãng cate 42 manu 80
                query.must(boolQuery()//
                        .should(boolQuery()
                                .must(termQuery("PresentProductID", 0))
                                .must(termQuery("ManufactureID", 80))
                        )//
                        .should(boolQuery()//
                                .must(rangeQuery("PresentProductID").gte(0))
                                .mustNot(termQuery("ManufactureID", 80))
                        )//
                );

                //query.must(termQuery("PresentProductID", 0));
                query.must(scriptQuery(
                        //new Script("(doc['ManufactureID'].value > 0 && " + present + ".length > 0 && " + present + ".value > 0 ) || ( doc['Prices.WebStatusId_"+qry.ProvinceId+"'].length > 0 && doc['Prices.WebStatusId_"+qry.ProvinceId+"'].value != 1 && doc['Prices.WebStatusId_"+qry.ProvinceId+"'].value != 5 && doc['HasBimage'].value == 1 )")
                        new Script("(doc['ManufactureID'].value == 80 && " + present + ".length > 0 && " + present + ".value > 0 ) || ( doc['Prices.WebStatusId_"+qry.ProvinceId+"'].length > 0 && doc['Prices.WebStatusId_"+qry.ProvinceId+"'].value != 1 && doc['Prices.WebStatusId_"+qry.ProvinceId+"'].value != 5 && doc['HasBimage'].value == 1 )")
                ));
            }
        }
    },
    HAS_PROMOTION_GT1 {
        @Override
        public void query(BoolQueryBuilder query, ProductQuery qry){
            query.must(rangeQuery("Prices.PromotionDiscountPercent_" + qry.ProvinceId).gte(1));
        }
    }
    ;

    public void query(BoolQueryBuilder query, ProductQuery qry) {
    }

    private static final int[] AccessoryCategory = new int[]{482, 60, 57, 55, 58, 54, 1662, 1363, 1823, 56, 75, 86, 382, 346,
            2429, 2823, 2824, 2825, 3885, 1622, 5505, 5005, 5025, 4547, 5452, 85, 6858, 6599, 7186};
    private static final int[] arrproductid = new int[]
            {
                    178983, 137190, 155046, 155058, 155067, 156188, 164505, 156389, 164517, 156396, 152361, 152364,
                    158070, 159666, 158618, 156079, 156081, 159814, 158279, 70308, 158277, 70307, 158846, 157629,
                    158749, 72726, 163585, 72778, 158007, 163603, 157643, 70309, 75194, 75195, 167613, 152506, 153033,
                    152506, 152512, 153100, 153100, 75256, 97718, 75259, 156063, 156067, 165020, 165019, 153222, 156071,
                    153924, 153921, 154735, 154735, 153923, 156073, 153925, 156074, 153978, 92014, 156068, 153922,
                    153982, 97262, 148874, 148862, 158619, 148852, 148839, 148894, 148849, 92131, 148807, 71017, 92127,
                    148895, 148850, 148832, 92126, 148796, 149428, 73950, 148820, 148924, 149483, 149431, 156168,
                    149448, 114959, 148823, 99732, 149479, 72949, 75236, 75241, 85217, 72862, 71192, 88588, 71191,
                    153825, 153852, 91124, 153900, 153850, 91125, 91806, 153832, 153901, 133252, 153853, 153806, 91882,
                    153839, 91881, 75257, 154018, 92129, 73683, 75469, 153819, 73682, 92105, 75412, 92128, 92123, 98061,
                    92124, 92132, 92121, 91884, 91883, 91880, 100307, 75408, 92122, 91740, 92133, 154023, 91801, 71533,
                    153855, 153822, 153840, 75468, 151042, 91805, 91887, 162441, 153737, 153870, 100312, 72861, 71693,
                    75588, 75185, 91847, 88600, 91668, 71256, 75563, 71252, 92230, 133275, 154202, 114569, 97258, 114568,
                    153871, 154262, 154261, 88249, 148851, 85168, 88346, 154263, 76988, 92018, 75224, 114973, 114980,
                    154264, 75263, 154650, 154667, 173990, 153740, 153767, 167753, 153671, 153778, 91916, 67766, 167873,
                    92135, 88008, 101675, 91858, 74889, 91859, 91852, 92930, 154150, 109184, 109358, 109157, 109201,
                    109202, 73927, 74603, 74622, 74574, 74575, 71257, 74617, 91857, 91856, 92009, 153787, 145456, 106489,
                    93394, 145587, 96816, 106533, 114404, 114397, 148626, 145975, 93395, 156181, 108366, 156182, 75327,
                    88627, 158471, 100626, 100650, 156175, 156173, 106489, 158471, 111432, 106487, 108387, 103903,
                    148973, 104109, 158734, 136377, 143441, 158740, 103919, 92886, 154985, 154998, 110587, 136377,
                    155202, 92886, 154998, 154985, 92846, 155202, 77721, 154994, 71099, 71104, 149232, 149233, 149174,
                    154994, 109132, 109134, 149182, 154991, 155200
            };
    private static final List<Integer> accessoryCategory = new ArrayList<>(List.of(482, 60, 57, 55, 58, 54, 1662, 1363,
            1823, 56, 75, 86, 382, 346,
            2429, 2823, 2824, 2825, 3885, 1622, 5505, 5005, 5025, 4547, 5452, 85, 6858, 6599, 7186));
    private static final List<Integer> listCateDungCu = new ArrayList<>(List.of(
            7739, 4697, 7720, 4706, 8119, 8121, 8858, 8860, 8861, 8862, 8980, 8984, 8979, 9138
    ));
    private static final List<Integer> newApplianceCategories = new ArrayList<>(List.of(4645, 462, 1922, 1982, 1983,
            1984, 1985, 1986, 1987, 1988, 1989, 1990, 1991, 1992, 2062, 2063, 2064, 2084, 2222, 2262, 2302, 2322, 2342,
            2142, 3305, 5473, 2428, 3385, 5105, 7367, 5554, 7498, 7419, 7278, 7458, 7683, 7684, 7685, 7075, 346, 4366,
            4706, 366, 365, 8765, 8620, 7902, 8621, 9008, 324, 9003, 8967, 9000, 7858));
    private static final List<Integer> appliance2142Categories = new ArrayList<>(List.of(4928, 4931, 5205, 4930, 5228,
            4927, 5225, 5292, 5227, 5226, 5230, 5231, 2403, 2402, 5229, 4929, 4932, 5478, 5395, 5354, 6790, 6819, 6790,
            3187, 3729, 7075, 346, 6599, 7305, 6012, 7479, 2528, 7480, 3736, 4946, 7481, 7482, 6553));


}
