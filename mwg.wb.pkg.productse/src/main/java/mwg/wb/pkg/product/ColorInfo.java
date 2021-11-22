package mwg.wb.pkg.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.business.ProductHelper;
import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.*;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.IMessage.DataAction;


import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.ProductColorBO;
import mwg.wb.model.search.ProductColorSO;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.Date;

public class ColorInfo  implements Ididx {
    private ORThreadLocal factoryRead = null;
    private String CurrentIndexDB = "colorindex";
    private ClientConfig config = null;
    private static ProductHelper productHelper = null;


    @Override
    public void InitObject(ObjectTransfer objectTransfer) {
        productHelper = (ProductHelper) objectTransfer.productHelper;
        factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
        config = (ClientConfig) objectTransfer.clientConfig;
        // lineNotify = (LineNotify) objectTransfer.notifyHelper;

    }

    @Override
    public ResultMessage Refresh(MessageQueue message) {
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.Code =ResultCode.Success;

        int ColorID = Integer.parseInt(message.Identify);
        var langID = message.Lang;

        String strNOTE = message.Note + "";
        boolean isLog = false;
        if (strNOTE.contains("LOG")) {
            isLog = true;
        }

        if (ColorID <= 0) {
            Logs.WriteLine("ProductID <= 0");
            return resultMessage;
        }

        String LangID = message.Lang.toLowerCase().replace("-", "_");

        if (Utils.StringIsEmpty(message.Lang)) {
            Logs.WriteLine("message.Lang == null");
            return resultMessage;
        }

        if(message.Action == DataAction.Add ||  message.Action == DataAction.Update){
            ProductColorBO[] colorInfo = null;
            try {
                colorInfo = productHelper.getColorInfor(ColorID+"",langID);
                if(colorInfo != null){

                    var rs = indexElastic(colorInfo[0]);

                    if(rs){
                        Logs.Log(isLog, strNOTE, ColorID + ":ESOK");
                        Logs.LogFile("colorse.txt", ColorID + ":ESOK");
                        resultMessage.Code = ResultCode.Success;
                    }else{
                        resultMessage.Message = "GameApp #" + ColorID + " init to ES FAILED";
                        resultMessage.Code = ResultCode.Retry;
                        Logs.Log(isLog, strNOTE, resultMessage.Message);
                        return resultMessage;
                    }


                }else{
                    Logs.LogFile("colorse.txt", ColorID + ":colorInfo == null");
                    resultMessage.Code = ResultCode.Success;
                    resultMessage.Message = "ColorSE #" + ColorID + " does not exist";
                    Logs.Log(isLog, strNOTE, resultMessage.Message);
                    return resultMessage;
                }
            } catch (Throwable e1) {
                Logs.LogException(e1);
                resultMessage.Message = "index fail";
                resultMessage.Code = ResultCode.Retry;
                return resultMessage;
            }


        }else if(message.Action == DataAction.Delete ){
          return  doDelete(message, resultMessage, isLog, strNOTE);

        }


        return resultMessage;
    }
    private ResultMessage doDelete(MessageQueue message, ResultMessage resultMessage, boolean islog, String strNode) {
        int colorID = Integer.parseInt(message.Identify);
        try {
            var update = new UpdateRequest(CurrentIndexDB, colorID + "").doc("{\"isdeleted\":1}", XContentType.JSON).docAsUpsert(true).detectNoop(false);
            var client = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
            var response = client.update(update, RequestOptions.DEFAULT);
            if (response == null || response.getResult() != DocWriteResponse.Result.UPDATED) {
                Logs.WriteLine("-Delete ColorInfo status to ES FAILED: " + colorID + ", " + response.getResult().name()
                        + "######################");
                resultMessage.Message = "delete fail";
                resultMessage.Code = ResultCode.Retry;
                return resultMessage;
            }
            resultMessage.Code = ResultCode.Success;
        } catch (Exception e) {
            Logs.Log(islog, strNode, "Exception delete ColorInfo status to ES FAILED: " + colorID);
            Logs.LogException(e);
            resultMessage.StackTrace = Logs.GetStacktrace(e);
            resultMessage.Message = "delete exception";
            resultMessage.Code = ResultCode.Retry;
        }
        return resultMessage;
    }

    private boolean indexElastic(ProductColorBO colorInfo) throws Exception {
        ResultMessage r = new ResultMessage();
        ProductColorSO color = new ProductColorSO();

        color.ColorID = colorInfo.ColorID;
        color.ColorCode = colorInfo.ColorCode;
        color.ColorName = colorInfo.ColorName;

        color.IsActived = colorInfo.IsActived;
        color.IsDeleted = colorInfo.IsDeleted;
        color.LastUpdated = new Date();

        if(!Strings.isNullOrEmpty(color.RepresentColor)){
            var representColor = color.RepresentColor.split(",");
            String keyword ="";
            for (var item: representColor) {
                keyword += DidxHelper.FilterVietkey(item).replace(" ","_");
            }
            color.RepresentColor = keyword + " ";
        }
        return ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).IndexObject(CurrentIndexDB,
                color, color.ColorID + "");
    }

    @Override
    public ResultMessage RunScheduleTask() {
        return null;
    }
}
