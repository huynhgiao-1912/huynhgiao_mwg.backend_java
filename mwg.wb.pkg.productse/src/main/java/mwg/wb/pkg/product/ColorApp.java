package mwg.wb.pkg.product;

import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.IMessage;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.model.api.ClientConfig;

public class ColorApp {
    public static void main(String[] args) {

        System.out.println("BlackPink in your area");

        ObjectTransfer objTransfer = new ObjectTransfer();
        ORThreadLocal factoryRead = null;
        try {
            factoryRead = new ORThreadLocal();
            factoryRead.IsWorker = true;
        } catch (Exception e) {

            e.printStackTrace();
        }

        ClientConfig clientConfig = new ClientConfig();
        /// init config
        clientConfig.SERVER_ORIENTDB_READ_USER = "admin";
        clientConfig.SERVER_ORIENTDB_READ_PASS = "EnterW@graph!@#";
        clientConfig.SERVER_ORIENTDB_WRITE_USER = "admin";
        clientConfig.SERVER_ORIENTDB_WRITE_PASS = "EnterW@graph!@#";
        clientConfig.DATACENTER = 3;
        clientConfig.SERVER_ORIENTDB_WRITE_URL1 = "remote:10.1.5.126:2424/web";
        clientConfig.SERVER_ORIENTDB_READ_URL1 = "remote:10.1.5.126:2424/web";
        clientConfig.SERVER_ELASTICSEARCH_WRITE_HOST = "10.1.6.151";
        clientConfig.SERVER_ELASTICSEARCH_READ_HOST = "10.1.6.151";
        clientConfig.SERVER_RABBITMQ_URL = "amqp://beta:beta@10.1.6.139:5672";
        clientConfig.ERP_SERVICES_URL = "http://betaerpsupportservices.thegioididong.com/Web/WSWeb.asmx";
        clientConfig.ERP_SERVICES_AUTHEN = "ksdfswfrew3ttc!@4#123";

        factoryRead.initRead(clientConfig, 0,1);

        var productHelper = new ProductHelper(factoryRead,clientConfig);
        objTransfer.clientConfig = clientConfig;
        objTransfer.factoryRead = factoryRead;
        objTransfer.productHelper = productHelper;

        ColorInfo colorInfo = new ColorInfo();
        colorInfo.InitObject(objTransfer);

        MessageQueue message = new MessageQueue();
        message.Action = IMessage.DataAction.Add;

        message.Lang = "vi-VN";
        message.Note = "DIDX_NOTIFY_COLOR_INFOR";

        int [] ids = new int[]{1,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,36,38,40,39};

        for (int id:
            ids ) {
            message.Identify = id +"";
            colorInfo.Refresh(message);

        }


        System.out.println("complete");






    }
}
