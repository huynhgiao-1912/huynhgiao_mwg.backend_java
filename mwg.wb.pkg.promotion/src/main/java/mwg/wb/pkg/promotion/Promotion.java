package mwg.wb.pkg.promotion;

import mwg.wb.business.ProductHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.common.Utils;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.search.ProductSO;
import mwg.wb.pkg.promotion.helper.bhx.PromotionBHXV1;

import java.util.Calendar;
import java.util.Date;

public class Promotion implements Ididx {

	int DataCenter = 0;
	ObjectTransfer objectTransfer = null;
	ClientConfig clientConfig = null;
	ProductHelper productHelper = null;
	
	ORThreadLocal factoryRead = null;
	@Override
	public void InitObject(ObjectTransfer aobjectTransfer) {

		objectTransfer = aobjectTransfer;
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		productHelper = new ProductHelper(factoryRead, clientConfig);
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
		DataCenter = message.DataCenter;
		String note = message.Note + "";
		if (message.SiteID == 11) {
			 
				try {
					var promotion = new PromotionBHXV1();
					promotion.InitObject(objectTransfer);
					promotion.Refresh(message);
				} catch (Throwable ex) {
					Logs.LogException(ex );
				}
				ResultMessage msg = new ResultMessage();
				msg.Code = ResultCode.Success;
				return msg;
			 

		} else {
			var promotion = new PromotionV1();
			promotion.InitObject(objectTransfer);
			return promotion.Refresh(message);
		}

	}
	private static int _DayLastUpdate = -1;
	@Override
	public ResultMessage RunScheduleTask() {
//		if(!(DidxHelper.isBeta() || DidxHelper.isHanh()) ) {
//			// chưa cho chạy live
//			return null;
//		}
		
		
		// TODO Auto-generated method stub
		Date dateC = new Date();
		Calendar calendar = Calendar.getInstance(); // calendar instance
		calendar.setTime(dateC);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int day = calendar.get(Calendar.DAY_OF_WEEK );
		
		if(DidxHelper.isHanh() ||  hour == 1 && (_DayLastUpdate < day || _DayLastUpdate == 7)) {
			// _View7DayLastUpdate tức là ngày cuối tuần thì hôm nay this.getday == 1
			_DayLastUpdate = day;
			// xử lý ở đây
			//getListProductPromotionExpired
			try {
				String queue = "";
				String queueBK = "";
				String queueDev = "";
				
				var listproductSO = productHelper.getListProductPromotionExpired();
				if(listproductSO != null && listproductSO.size() > 0) {
					for (ProductSO productSO : listproductSO) {
						
						int h = (int) (productSO.ProductID % 3);

						queue = "gr.dc4.didx.status" + h;
						queueDev = "gr.beta.didx.status" + h;
						queueBK = "gr.dc2.didx.status" + h;

						
						MessageQueue nwmsg = new MessageQueue();
						String ProductCode = productSO.Prices != null && productSO.Prices.size() > 0 ? productSO.Prices.get("ProductCode_3") + "" : null;
						if(!Utils.StringIsEmpty(ProductCode)) {
							
							nwmsg.Action = DataAction.Update;
							nwmsg.ClassName = "mwg.wb.pkg.price.Status";
							nwmsg.CreatedDate = Utils.GetCurrentDate();
							nwmsg.Lang = "vi-VN";
							nwmsg.SiteID = productSO.SiteID;
							nwmsg.Type = 0;
							nwmsg.Data = "";
							nwmsg.DataCenter = clientConfig.DATACENTER;// == 3 ? 3 : 4;
							//nwmsg.Identify = ProductCode + "|" + productSO.ProductID;
							nwmsg.Identify = productSO.ProductID + "";
							//Logs.Log(true, "", " ");
							Logs.LogFile("updatepromotionexpired.txt", nwmsg.Identify + "|" +  nwmsg.SiteID );
							nwmsg.Note = "Update Promotion Expired";
							QueueHelper.Current(clientConfig.SERVER_RABBITMQ_URL).PushAll(queue, queue, queueBK, queueDev, nwmsg,
									true, "", nwmsg.DataCenter);
							
						}
						
					}
				}
				
				
				
			} catch (Throwable e) {
				e.printStackTrace();
			}
			
			
		}
		
		return null;
	}

}
