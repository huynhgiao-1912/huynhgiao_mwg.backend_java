package mwg.wb.client.elasticsearch.dataquery;

import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.script.Script;
import mwg.wb.client.elasticsearch.dataquery.ProductQuery.SearchType;

import org.elasticsearch.script.ScriptType;

import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.scriptFunction;

public enum SearchNumberType {


    E_2020 {
        @Override
        public void search(BoolQueryBuilder query, ProductQuery qry) {
            if (qry.SearchType == SearchType.SEARCH2020) {
                if (Arrays.asList(appliance2142Categories).contains(qry.CategoryId)) {
                    query.must(boolQuery()
                            .should(termQuery("Scenario", 1))
                            .should(rangeQuery("PromotionDiscountPercent_" + qry.ProvinceId).gte(30))
                    );
                } else if (Arrays.asList(listCateDungCu).contains(qry.CategoryId) || qry.CategoryId == 2162) {
                    query.must(boolQuery()
                            .should(termQuery("Scenario", 1))
                            .should(rangeQuery("PromotionDiscountPercent_" + qry.ProvinceId).gte(10))
                    );
                } else {

                    query.must(boolQuery()
                            .should(termQuery("Scenario", 1))
                            .should(rangeQuery("PromotionDiscountPercent_" + qry.ProvinceId).gte(10))
                            .should(boolQuery().must(termQuery("CategoryID", newApplianceCategories))
                                    .must(termQuery("HasPromotion", 1)))
                    );
                }
                query.must(scriptQuery(new Script(" (doc['Prices.Price_" + qry.ProvinceId + "'].value * 0.91) >= doc['Prices.PriceAfterPromotion_" + qry.ProvinceId + "_" + qry.SiteId + "'].value ")));
                //loại bỏ sp km > 60%
                query.mustNot(rangeQuery("PromotionDiscountPercent_" + qry.ProvinceId).gte(60));

            }
        }

    },

    SCENARIO {
        @Override
        public void search(BoolQueryBuilder query, ProductQuery qry) {
            if (accessoryCategory.contains(qry.CategoryId) ){
                var now = new Date();
                query.must(termQuery("Scenario", 1));
                query.must(rangeQuery("ScenarioFromDate").lte(now));
                query.must(rangeQuery("ScenarioToDate").gte(now));
            }

        }

    },
    MAINKEYWORD{

    },
    MAINKEYWORD_KEYWORD{

    },


    ;


    public void search(BoolQueryBuilder query, ProductQuery qry) {
    }




    private static final List<Integer> accessoryCategory = new ArrayList<>(List.of(482, 60, 57, 55, 58, 54, 1662, 1363, 1823, 56, 75, 86, 382, 346,
            2429, 2823, 2824, 2825, 3885, 1622, 5505, 5005, 5025, 4547, 5452, 85, 6858, 6599, 7186));
    private static final List<Integer> listCateDungCu = new ArrayList<>(List.of(
            7739, 4697, 7720, 4706, 8119, 8121, 8858, 8860, 8861, 8862, 8980, 8984, 8979, 9138
    ));
    private static final List<Integer> newApplianceCategories = new ArrayList<>(List.of(4645, 462, 1922, 1982, 1983, 1984, 1985, 1986, 1987, 1988, 1989, 1990, 1991, 1992, 2062, 2063, 2064, 2084, 2222, 2262, 2302, 2322, 2342, 2142, 3305, 5473, 2428, 3385, 5105, 7367, 5554, 7498, 7419, 7278, 7458, 7683, 7684, 7685, 7075, 346, 4366, 4706, 366, 365, 8765, 8620, 7902, 8621, 9008, 324, 9003, 8967, 9000, 7858));
    private static final List<Integer> appliance2142Categories = new ArrayList<>(List.of(4928, 4931, 5205, 4930, 5228, 4927, 5225, 5292, 5227,
            5226, 5230, 5231, 2403, 2402, 5229, 4929, 4932, 5478, 5395, 5354, 6790, 6819, 6790, 3187, 3729, 7075, 346,
            6599, 7305, 6012, 7479, 2528, 7480, 3736, 4946, 7481, 7482, 6553));

}