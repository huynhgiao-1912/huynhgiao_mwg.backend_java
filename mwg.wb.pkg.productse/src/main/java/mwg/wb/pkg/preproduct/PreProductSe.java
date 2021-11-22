package mwg.wb.pkg.preproduct;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.Utils;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.commonpackage.TimerSeoBO;
import mwg.wb.model.commonpackage.TimerSeoSO;
import mwg.wb.model.products.PrenextProduct;
import mwg.wb.model.products.PrenextProductSO;
import mwg.wb.model.products.ProductBO;

public class PreProductSe implements Ididx{

	private ORThreadLocal factoryRead = null;
	private String currentIndexDB = "ms_prenextproduct";
	private ClientConfig config = null;

	private ObjectMapper mapper = null;
	public void InitObject(ObjectTransfer objectTransfer) {
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		config = (ClientConfig) objectTransfer.clientConfig;
		mapper = (ObjectMapper) objectTransfer.mapper;
	}


	@Override
	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;
		// if(1==1)return r;
		String strNOTE = message.Note + "";
		boolean isLog = false;
		if (strNOTE.contains("LOG")) {

			isLog = true;
		}
		Logs.getInstance().Log(isLog, strNOTE, "TimerSeoSE", message);

		int PreProductID = Utils.toInt(message.Identify);
		if (PreProductID <= 0) {
			return r;
		}
		try {
			if (message.Action == DataAction.Add || message.Action == DataAction.Update) {
				var PreProduct = factoryRead.QueryFunction("product_PreNextGetbyID", PrenextProduct[].class, false,
						PreProductID);

				if (PreProduct == null || PreProduct.length == 0) {
					r.Code = ResultCode.Success;
					return r;  
				} else {
					/// //
					var PreProductList = "";
					var NextProductList = "";
					//int NextProductSize = 0;
					int isDeleted = 0;
					int ProductID = 0;
					for (PrenextProduct product : PreProduct) {
						if(product.productID == PreProductID) {
							// đây là sản phẩm đời trước
							// tìm xem đời sau là ai?
							PreProductList = product.listproductidpre + " ";
							PreProductList = PreProductList.replace(",", " ");
							isDeleted = product.isDeleted;
							ProductID = PreProductID;
							
						}else {
							// ngược lại sản phẩm đời sau.
							NextProductList += product.productID + " ";
							//NextProductSize++;
						}
					}
					if(ProductID > 0) {
						
						var Manu = factoryRead.QueryFunction("product_getCategoryByProductID", ProductBO[].class, false,
								PreProductID);
						
						
						PrenextProductSO PreSo = new PrenextProductSO();
						PreSo.previousProduct = PreProductList;
						PreSo.NextProduct = (NextProductList + " ");//.replace(",", " ");
						PreSo.isDeleted = isDeleted;
						PreSo.productID = ProductID;
						if(Manu != null && Manu.length > 0) {
							PreSo.ManuID = (int) Manu[0].ManufactureID;
						}
						
						
						var rs = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST)
								.IndexObject(currentIndexDB, PreSo, PreSo.productID + "");
					}
					r.Code = ResultCode.Success;					

				}
			} else if (message.Action == DataAction.Delete) {

				/// xoá
				r.Code = DeletePreProduct(Integer.toString(PreProductID),r);

			}

		} catch (Throwable e) {

			r.Code = ResultCode.Retry;
			Logs.WriteLine("prenextproduct_trycatch: " + e.toString());
			return r;
		}
		return r;
	}

	private ResultCode DeletePreProduct(String PreProductID, ResultMessage r) {

		try {
			var data = PreProductID;
	        if (data == null )
	        {
	        	r.Code = ResultCode.Success;
	        	return r.Code;
	        }

			var update = new UpdateRequest(currentIndexDB, String.valueOf(data))
					.doc("{\"IsDeleted\":1  }", XContentType.JSON).docAsUpsert(true).detectNoop(false);
			var client = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
			UpdateResponse response = null;
			try {
				response = client.update(update, RequestOptions.DEFAULT);
			} catch (Exception e) {
				r.Code = ResultCode.Retry;
				e.printStackTrace();
			}
			if (response != null && response.getResult() == Result.UPDATED) {
				r.Code = ResultCode.Success;
			} else {

				Logs.WriteLine("-Update isdeleted in PrenextProduct ES FAILED: PreProduct:#" + data + ", " + response
						+ "######################");

				r.Code = ResultCode.Retry;
			}
		} catch (Exception e) {
			Logs.WriteLine("Update isdeleted in ERRER, " + e);
			r.Code = ResultCode.Retry;

		}
		return r.Code;
	}
	
	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
class CateManu{
	public int ManufactureID;
	public int CategoryID;
}
