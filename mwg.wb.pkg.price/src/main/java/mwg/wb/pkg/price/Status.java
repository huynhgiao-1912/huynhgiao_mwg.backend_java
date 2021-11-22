package mwg.wb.pkg.price;

import com.fasterxml.jackson.databind.ObjectMapper;
import mwg.wb.business.PriceHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.ErpHelper;
import mwg.wb.common.DidxHelper;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.pkg.price.helper.bhx.StatusBHX;
import mwg.wb.pkg.price.helper.dmx.StatusDMX;

public class Status implements Ididx {

	private ORThreadLocal factoryWrite = null;
	private ORThreadLocal factoryRead = null;
	private PriceHelper priceHelper = null;
	private ObjectMapper mapper = null;
	private ErpHelper erpHelper = null;
	private int DataCenter = 0;
	ObjectTransfer objectTransfer = null;
	ClientConfig clientConfig = null;
 
	@Override
	public void InitObject(ObjectTransfer aobjectTransfer) {
		objectTransfer = aobjectTransfer;
		clientConfig = (ClientConfig) objectTransfer.clientConfig;
		 
			mapper = (ObjectMapper) objectTransfer.mapper;
		
			
			
	}

	public ResultMessage Refresh(MessageQueue message) {
		DataCenter = message.DataCenter;
		String note = message.Note + "";
		if (note.contains("Manh")) {

			ResultMessage msg = new ResultMessage();
			msg.Code = ResultCode.Success;
			return msg;
		}
		if (message.SiteID <= 0) {
			if (message.BrandID > 0) {

				message.Lang = DidxHelper.getLangByBrandID(message.BrandID);
				message.SiteID = DidxHelper.getSitebyBrandID(message.BrandID);
			} else {

				message.SiteID = message.SiteID;
				message.Lang = message.Lang;
			}

		}

		if (message.SiteID == 11) {
		 
				try {
					var price = new StatusBHX();
					price.InitObject(objectTransfer);
					price.Refresh(message);
				} catch (Throwable ex) {
					Logs.LogException(ex);
				}
				ResultMessage msg = new ResultMessage();
				msg.Code = ResultCode.Success;
				return msg;
			 
		} else if (message.SiteID == 2) {

			
			long s1=System.currentTimeMillis();
			 
		 
			
			
			
			try {
				var price = new StatusDMX();
				price.InitObject(objectTransfer);
				var rs= price.Refresh(message);
				
				long time=System.currentTimeMillis()-s1;
				if (time > 1000) { 
						try {
							Logs.LogFactorySlowMessage2("status-dmx","\r\n" + mapper.writeValueAsString(message) , time);
						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					 
				}
				
				return rs;
			} catch (Throwable ex) {
				Logs.LogException(ex);
			}
			ResultMessage msg = new ResultMessage();
			msg.Code = ResultCode.Success;
			return msg;

		} else {
			long s1=System.currentTimeMillis();
			
			Ididx price =
//					note.contains("VERSION2") ? new StatusV2() : 
					new StatusV1();
			price.InitObject(objectTransfer);
			var rs= price.Refresh(message);
			long time=System.currentTimeMillis()-s1;
			if (time > 1000) { 
					try {
						Logs.LogFactorySlowMessage2("status-1","\r\n" + mapper.writeValueAsString(message) , time);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 
			}
			
			return rs;
		}
	}

	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
