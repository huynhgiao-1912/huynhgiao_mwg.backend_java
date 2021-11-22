package mwg.wb.client.elasticsearch.dataquery;

import org.elasticsearch.script.Script;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder.ScriptSortType;
import org.elasticsearch.search.sort.SortOrder;

import static org.elasticsearch.search.sort.SortBuilders.scriptSort;

public enum SearchOrder {

    DISPLAYORDER_ASC {
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            sb.sort("DisplayOrder", SortOrder.ASC);
        }
    },
    SOLDCOUNT {
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            sb.sort("ProductSoldCount", SortOrder.DESC);
        }
    },
    FEATURE {

//		long now = System.currentTimeMillis(), start = 0, expire = 0;
//		long displayOrder = -1;
//		if(!doc["DisplayOrder"].empty && doc["DisplayOrder"].value != null) {
//			displayOrder = doc["DisplayOrder"].value;
//		}
//		if(!doc["FeatureStartDate"].empty && doc["FeatureStartDate"].value != null) {
//			start = doc["FeatureStartDate"].value.toInstant().toEpochMilli();
//		}
//		if(!doc["FeatureExpireDate"].empty && doc["FeatureExpireDate"].value != null) {
//			expire = doc["FeatureExpireDate"].value.toInstant().toEpochMilli();
//		}
//		if(displayOrder >= 2 && displayOrder <= 98 && start < now && start > 86400000L
//			&& (expire > start || expire < 86400000L)) {
//			return 500000 - displayOrder;
//		}
//		return 0;

        private ScriptSortBuilder featureSort = scriptSort(
                new Script("		long now = System.currentTimeMillis(), start = 0, expire = 0;\n"
                        + "		long displayOrder = -1;\n"
                        + "		if(!doc[\"DisplayOrder\"].empty && doc[\"DisplayOrder\"].value != null) {\n"
                        + "			displayOrder = doc[\"DisplayOrder\"].value;\n" + "		}\n"
                        + "		if(!doc[\"FeatureStartDate\"].empty && doc[\"FeatureStartDate\"].value != null) {\n"
                        + "			start = doc[\"FeatureStartDate\"].value.toInstant().toEpochMilli();\n" + "		}\n"
                        + "		if(!doc[\"FeatureExpireDate\"].empty && doc[\"FeatureExpireDate\"].value != null) {\n"
                        + "			expire = doc[\"FeatureExpireDate\"].value.toInstant().toEpochMilli();\n"
                        + "		}\n"
                        + "		if(displayOrder >= 2 && displayOrder <= 98 && start < now && start > 86400000L\n"
                        + "			&& (expire > start || expire < 86400000L)) {\n"
                        + "			return 500000 - displayOrder;\n" + "		}\n" + "		return 0;"),
                ScriptSortType.NUMBER).order(SortOrder.DESC);

        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            sb.sort(featureSort);
        }
    },
    PRICE_ASC {
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
//            sb.sort("Prices.Price_" + qry.ProvinceId + "", SortOrder.ASC);
            String price ="Prices.Price_"+qry.ProvinceId;
            ScriptSortBuilder priceASC = scriptSort(
                    new Script("if(!doc['"+price+"'].empty && doc['"+price+"'].value !=null " +
                            "&& doc['"+price+"'].value > 0){" +
                            "return doc['"+price+"'].value;" +
                            "} else { " +
                            "return Double.MAX_VALUE;"
                            +"}"), ScriptSortType.NUMBER
            ).order(SortOrder.ASC);
            sb.sort(priceASC);
        }
    },
    PRICE_DESC {
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            sb.sort("Prices.Price_" + qry.ProvinceId, SortOrder.DESC);


                 }
    },

    DISCOUNT_PERCENT_DESC {
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            sb.sort("Prices.PromotionDiscountPercent_" + qry.ProvinceId, SortOrder.DESC);
        }
    },

    DISCOUNT_PERCENT_ASC {
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            sb.sort("Prices.PromotionDiscountPercent_" + qry.ProvinceId, SortOrder.ASC);
        }
    },

    PRICE_DISCOUNT_ASC {
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
//            sb.sort("Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId + "", SortOrder.ASC);
            String price ="Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId;
            ScriptSortBuilder priceASC = scriptSort(
                new Script("if(!doc['"+price+"'].empty && doc['"+price+"'].value !=null " +
                        "&& doc['"+price+"'].value > 0){" +
                        "return doc['"+price+"'].value;" +
                        "} else { " +
                        "return Double.MAX_VALUE;"
                        +"}"), ScriptSortType.NUMBER
            ).order(SortOrder.ASC);
            sb.sort(priceASC);
        }
    },

    PRICE_DISCOUNT_DESC {
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            sb.sort("Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId + "", SortOrder.DESC);
        }
    },
/**
 * - Tuan Vu :
 * */
    CREATE_DATE_DESC {
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            sb.sort("DateCreated", SortOrder.DESC);

        }
    },

    CREATE_DATE_ASC {
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            sb.sort("DateCreated", SortOrder.ASC);
        }
    },
// sort sản phẩm bán chạy
    SOLDCOUNT_DESC {
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            sb.sort("ProductSoldCount", SortOrder.DESC);
        }
    },
    SOLDCOUNT_ASC {
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            sb.sort("ProductSoldCount", SortOrder.ASC);
        }
    },
    STATUS_PRIORITY{
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            String title = "Prices.WebStatusId_" + qry.ProvinceId;
            ScriptSortBuilder statusPriority = scriptSort(
                    new Script("if(!doc['" + title +"'].empty && doc['" + title + "'].value!=null){"
                            + "if(doc['" + title + "'].value == 4 || doc['" + title + "'].value == 11) {return 2;}"
                            + "else if(doc['" + title + "'].value == 1 || doc['" + title + "'].value == 7) {return 0;}"
                            + "else{ return 1;}"
                            + "}"),
                    ScriptSortType.NUMBER).order(SortOrder.DESC);
           sb.sort(statusPriority);
        }
    },
    DISCOUNT_DESC {
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            String pricePromotion = "Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId;
            String price ="Prices.Price_"+qry.ProvinceId;
            ScriptSortBuilder discondSort = scriptSort(
                    new Script("if(!doc['"+pricePromotion+"'].empty " + "&& doc['"+pricePromotion+"'].value !=null" +
                            " && !doc['"+price+"'].empty && doc['"+price+"'].value !=null){" +
                            "return doc['"+pricePromotion+"'].value - doc['"+price+"'].value" +
                            "}"), ScriptSortType.NUMBER).order(SortOrder.DESC);
            sb.sort(discondSort);
        }
    },
    PROPERTY_VALUE_ASC {
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            if (qry.sortingPropertyID > 0) {
                sb.sort("rangeProp.prop_" + qry.sortingPropertyID, SortOrder.ASC);
            }
        }
    },
    PROPERTY_VALUE_DESC {
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            if (qry.sortingPropertyID > 0) {
                sb.sort("rangeProp.prop_" + qry.sortingPropertyID, SortOrder.DESC);
            }
        }
    },
    COMBINEDPRODUCT_PRICE_DESC{
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
            sb.sort("Prices.PresentPriceMin" + qry.ProvinceId, SortOrder.DESC);
        }
    },
    COMBINEDPRODUCT_PRICE_ASC{
        @Override
        public void sort(SearchSourceBuilder sb, ProductQuery qry) {
//            sb.sort("Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId + "", SortOrder.ASC);
            String price ="Prices.PresentPriceMin" + qry.ProvinceId;
            ScriptSortBuilder priceASC = scriptSort(
                    new Script("if(!doc['"+price+"'].empty && doc['"+price+"'].value !=null " +
                            "&& doc['"+price+"'].value > 0){" +
                            "return doc['"+price+"'].value;" +
                            "} else { " +
                            "return Double.MAX_VALUE;"
                            +"}"), ScriptSortType.NUMBER
            ).order(SortOrder.ASC);
            sb.sort(priceASC);
        }
    }
    ;

/***/
    public void sort(SearchSourceBuilder sb, ProductQuery qry) {

    }

}
