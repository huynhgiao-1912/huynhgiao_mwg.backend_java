package mwg.wb.pkg.price;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.business.PriceHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.RefSql;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.SqlInfo;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.products.ProductErpPriceBO;
 
import mwg.wb.pkg.price.helper.bhx.PriceBHX;
import mwg.wb.pkg.price.helper.bhx.PriceBHXV2;

public class Price implements Ididx {
	private ORThreadLocal factoryWrite = null;
	private ORThreadLocal factoryRead = null;
	private PriceHelper priceHelper = null;
	private ProductHelper productHelper = null;
	private ObjectMapper mapper = null;
	private ErpHelper erpHelper = null;
	int DataCenter = 0;
	private static List<Integer> MainProductCate = List.of(42, 44, 522, 7077, 7264);
	ObjectTransfer objectTransfer = null;
	ClientConfig clientConfig = null;

	@Override
	public void InitObject(ObjectTransfer aobjectTransfer) {

		objectTransfer = aobjectTransfer;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
	}

	public int PushSysData(boolean isLog, String Note, String sql, Map<String, Object> params) {

		return 1;
	}

	public int PushSysData(boolean isLog, String Note, String sql) {

		return 1;
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		DataCenter = message.DataCenter;
		//site bhx xử lý giá từ POS
		String note = message.Note + "";
		 
		 

	 	if (message.SiteID == 11) {
	 		 
	 			try {
					 var price = new PriceBHXV2();
					 price.InitObject(objectTransfer);
					   price.Refresh(message);
				} catch (Throwable ex) {
					 Logs.LogException(ex );
				}
				ResultMessage msg = new ResultMessage();
				msg.Code = ResultCode.Success;
				return msg;
			 
	 		
	 		
	 	} else {
	 		 var price = new PriceV1();
			 price.InitObject(objectTransfer);
			 return price.Refresh(message); 
 		}

	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
