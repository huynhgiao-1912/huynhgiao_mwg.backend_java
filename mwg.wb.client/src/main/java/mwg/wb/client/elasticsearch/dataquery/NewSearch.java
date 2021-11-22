package mwg.wb.client.elasticsearch.dataquery;

import org.elasticsearch.index.query.BoolQueryBuilder;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;

public enum NewSearch {
    MOINHAT_KNH{ // trang mới nhất - kinh nghiệm hay
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",new_knh));
        }
    },
    DIENMAY_KNH{// điện máy - kinh nghiệm hay
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",dienMay_dmx));
        }
    },
    VIENTHONG_KNH{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",vienThong));
        }
    },
    GIADUNG_KNH{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",giaDung));
        }
    },
    MEOHAY_KNH{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",meoHay));
        }
    },
    KHUYENMAI_DMX{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",khuyenMai_DMX));
        }
    },
    VAOBEP_DMX{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",giaDung));
        }

    },
    ANDROID{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",android));
        }
    },
    IPHONE{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",iphone));
        }
    },
    MANGXAHOI{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",mangxahoi));
        }
    },
    THUTHUAT_VP{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",thuThuat_vanPhong));
        }
    },
    DONGHO{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",dongHo));
        }
    },
    DIENMAY_QANDA{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",dienMay_tgdd));
        }
    },
    MAYIN{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",mayIn));
        }
    },
    THUTHUAT_GAME{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",thuThuat_Game));
        }
    },
    PC_LAPTOP_TABLET{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",pc_laptop_tablet));
        }
    },
    PHUKIEN_TGDD{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",phuKien_tgdd));
        }
    },
    WIKI_THUATNGU{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",wiki_thuatNgu));
        }
    },
    THIETBITHONGMINH{
        @Override
        public void queryMust(BoolQueryBuilder query) {
            query.must(termsQuery("ListTreeCategory",tb_thongMinh));
        }
    };
    public void queryMust(BoolQueryBuilder query){
    }

     private static final  String[] new_knh = new String[]{"g1053g"};
     private static final String[] dienMay_dmx = new String[] {"g1043g","g1044g","g1046g","g1045g"};
     private static final String[] vienThong = new String[] {"g1908g","g1990g"};
     private static final String[] giaDung = new String[] {"g1047g"};
     private static final String[] meoHay = new String[] {"g1879g"};
     private static final String[] khuyenMai_DMX = new String[] {"g1061g"};
     private static final  String[] android = new String[]{"g1644g"};
    private static final  String[] iphone = new String[]{"g1637g"};
    private static final  String[] mangxahoi = new String[]{"g1637g"};
    private static final  String[] thuThuat_vanPhong = new String[]{"g2018g"};
    private static final  String[] dongHo = new String[]{"g1958g"};
    private static final  String[] dienMay_tgdd = new String[]{"g1984g"};
    private static final  String[] mayIn = new String[]{"g1963g"};
    private static final  String[] thuThuat_Game = new String[]{"g1949g"};
    private static final  String[] pc_laptop_tablet = new String[]{"g1656g"};
    private static final  String[] phuKien_tgdd = new String[]{"g1739g"};
    private static final  String[] wiki_thuatNgu = new String[]{"g1738g"};
    private static final  String[] tb_thongMinh = new String[]{"g2017g"};






}
