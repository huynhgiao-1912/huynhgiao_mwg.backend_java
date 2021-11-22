package mwg.wb.pkg.price;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.WorkerHelper;
import mwg.wb.business.webservice.WebserviceHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CrmServiceHelper;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.GConfig;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;

import java.text.SimpleDateFormat;

public class WebStatusUpdate implements Ididx {
    private ClientConfig clientConfig;
    
    @Override
    public void InitObject(ObjectTransfer objectTransfer) {
        clientConfig = (ClientConfig) objectTransfer.clientConfig;
    }

    @Override
    public ResultMessage Refresh(MessageQueue message) {
        ResultMessage r = new ResultMessage() {{
            Code = ResultCode.Success;
        }};
        String strNOTE = message.Note + "";
        boolean isLog = strNOTE.contains("DIDX_TOP_LOG") || strNOTE.contains("DIDX_LOG");
        Logs.getInstance().Log(isLog, strNOTE, "WebStatusUpdate", message);
        
        // Identify: productID|webStatusID|price
        var arr = message.Identify.split("\\|");
        if(arr.length < 3) {
            r.Code = ResultCode.InvalidRequest;
            return r;
        }
        try {
            int productID = Utils.toInt(arr[0]);
            int webStatusID = Utils.toInt(arr[1]);
            double price = Utils.toDouble(arr[2]);
            Integer c = WebserviceHelper.Call(clientConfig.DATACENTER).Get(
                    "apiproduct/updateproductstatus?productID=:?&statusID=:?&siteID=:?&languageID=:?&price=:?",
                    Integer.class, productID, webStatusID, message.SiteID, message.Lang, price);
            if (c != null && c >= 0) {
                Logs.Log(isLog, strNOTE, "WebserviceHelper updateproductstatus " + message.Identify + " success");
            } else {
                Logs.Log(isLog, strNOTE, "WebserviceHelper updateproductstatus " + message.Identify + " failed");
            }
        } catch (Throwable e) {
            Logs.LogException(e);
            r.StackTrace = Utils.stackTraceToString(e);
            r.Code = ResultCode.Retry;
        }
        return r;
    }

    @Override
    public ResultMessage RunScheduleTask() {
        return null;
    }

    public static void main(String[] args) throws Exception {
        test();
    }

    public static void test() throws Exception {
        var config = WorkerHelper.GetWorkerClientConfig();

        // dl live
        config.SERVER_RABBITMQ_URL = "amqp://tgdd:Tgdd2012@192.168.2.55:5672";
        config.SERVER_ORIENTDB_READ_URL1 = "remote:172.16.3.71:2424/web";
        config.ERP_SERVICES_URL = "http://erpwebsupportservices.thegioididong.com/Web/WSWeb.asmx";
        config.ERP_SERVICES_AUTHEN = "werwerewrw32423!@4#123";
        config.CRM_SERVIVES_URL = "http://crm-services.thegioididong.com/NEW-CRMTGDD/CRMTGDDService.asmx";
        config.SERVER_ELASTICSEARCH_WRITE_HOST = "172.16.3.23";

        var oread = new ORThreadLocal();
        oread.initRead(config, 0, 2);
        var priceHelper = new PriceHelper(oread, config);
        var phelper = new ProductHelper(oread, config);
        var erp = new ErpHelper(config.ERP_SERVICES_URL, config.ERP_SERVICES_AUTHEN);
        var mapper = new ObjectMapper();
        var df = new SimpleDateFormat(GConfig.DateFormatString);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).setDateFormat(df);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        var m = new MessageQueue() {
            {
                // X13|0|0160015000771|1
                Identify = "196963|1|0.0";//"229948";
                Note = "";
                SiteID = 1;
                Lang = "vi-VN";
                DataCenter = 3;
//				BrandID = 1;
//				Note = "VERSION2";
            }
        };
        var tools = new ObjectTransfer();
        tools.erpHelper = erp;
        tools.mapper = mapper;
        tools.factoryRead = oread;
        tools.clientConfig = config;
        tools.productHelper = phelper;
        tools.priceHelper = priceHelper;
        tools.crmHelper = new CrmServiceHelper(config);
        var status = new WebStatusUpdate();
        status.InitObject(tools);
        status.Refresh(m);
        System.out.print("hello");
    }
}
