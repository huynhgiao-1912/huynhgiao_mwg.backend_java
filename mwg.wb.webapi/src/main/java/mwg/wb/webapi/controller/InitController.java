package mwg.wb.webapi.controller;

import mwg.wb.business.LogHelper;
import mwg.wb.client.CachedGraphDBHelper;
import mwg.wb.client.queue.QueueHelper;
import mwg.wb.common.*;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.model.LogLevel;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.webapi.service.ConfigUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apiinit")
public class InitController {

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private Environment environment;

    public InitController() {

    }

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public ResponseEntity<String> Hello() {
        String port = environment.getProperty("local.server.port");

        String message = "Helloworld " + port;
        return new ResponseEntity<String>(message, HttpStatus.OK);
    }

    @RequestMapping(value = "/getversion", method = RequestMethod.GET)
    public String GetVersion() {

        ClientConfig config = ConfigUtils.GetOnlineClientConfig();
        return "XXXXX"+ config.API_VERSION;
    }
   
    @RequestMapping(value = "/getcachecount", method = RequestMethod.GET)
    public String GetCacheCount() {

    	try {
			return ""+ CachedGraphDBHelper.GetCount() ;
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return "";
    } 
    

    @RequestMapping(value = "/testlog", method = RequestMethod.GET)
    public void TestLogger(int status) {

        if (status == 500) {
            LogHelper.WriteLog("Test log error ", LogLevel.ERROR, request);

        }
    }

    @RequestMapping(value = "/testrevert", method = RequestMethod.GET)
    public void testrevert(int status) {

        if (status == 500) {
            LogHelper.WriteLog("Test log error ", LogLevel.ERROR, request);

        }
    }

//	@RequestMapping(value = "/getcentralconfig", method = RequestMethod.GET)
//	public ResponseEntity<ClientConfig> GetCentralConfig() {
////		String ra = ConfigUtils.GetOnlineClientConfigStr();
//		return new ResponseEntity<ClientConfig>(ConfigUtils.GetOnlineClientConfig(), HttpStatus.OK);
//	}

    @RequestMapping(value = "/pushpromotion", method = RequestMethod.POST)
    public ResponseEntity<String> PushInitPromotion(String productIDList, int SiteID, String Lang, int DataCenter) {
        String ra = "";
        if (DidxHelper.isStaging() || DidxHelper.isBeta() || DidxHelper.isLocal()) {
            String[] messIdentify = productIDList.split("\\,");

            String lang = "vi-VN";
            if (!Utils.StringIsEmpty(Lang)) {
                lang = Lang;
            }
            for (String productid : messIdentify) {
                MessageQueue message = new MessageQueue();

                message.Action = DataAction.Add;
                message.Note = "";
                message.Identify = productid;
                message.ClassName = "PROMOTION";
                message.CreatedDate = Utils.GetCurrentDate();
                message.ID = 0;
                message.SiteID = SiteID;
                message.Lang = lang;
                message.Version = 0;
                message.DataCenter = DataCenter;
                if (SiteID == 11) {
                    message.Note = "BHXTEST";
                }
                try {
                    QueueHelper.Current(ConfigUtils.GetOnlineClientConfig().SERVER_RABBITMQ_URL).Push("ms.init", message);
                    ra = ra + "," + productid;
                } catch (Exception e) {
                    Logs.LogException(e);
                    return new ResponseEntity<String>(e.getMessage(), HttpStatus.OK);
                }
            }

        } else {
            ra = "Push only on beta or staging !!!";
        }
        return new ResponseEntity<String>(ra, HttpStatus.OK);


    }

    @RequestMapping(value = "/pushprice", method = RequestMethod.POST)
    public ResponseEntity<String> PushInitPrice(String productIDList, int SiteID, String Lang, int DataCenter) {
        String ra = "";
        if (DidxHelper.isStaging() || DidxHelper.isBeta() || DidxHelper.isLocal()) {
            String[] messIdentify = productIDList.split("\\,");
            String lang = "vi-VN";
            if (!Utils.StringIsEmpty(Lang)) {
                lang = Lang;
            }
            for (String productid : messIdentify) {
                MessageQueue message = new MessageQueue();

                message.Action = DataAction.Add;
                message.Note = "";
                message.Identify = productid;
                message.ClassName = "PRICE";
                message.CreatedDate = Utils.GetCurrentDate();
                message.ID = 0;
                message.SiteID = SiteID;
                message.Lang = lang;
                message.Version = 0;
                message.DataCenter = DataCenter;
                if (SiteID == 11) {
                    message.Note = "BHXTEST";
                }
                try {
                    QueueHelper.Current(ConfigUtils.GetOnlineClientConfig().SERVER_RABBITMQ_URL).Push("ms.init", message);
                    ra = ra + "," + productid;
                } catch (Exception e) {
                	Logs.LogException(e);
                    return new ResponseEntity<String>(e.getMessage(), HttpStatus.OK);
                }
            }
        } else {
            ra = "Push only on beta or staging !!!";

        }

        return new ResponseEntity<String>(ra, HttpStatus.OK);
    }

    @RequestMapping(value = "/pushstatus", method = RequestMethod.POST)
    public ResponseEntity<String> PushStatus(String productIDList, SiteID site) {
		String ra = "";

		if (DidxHelper.isStaging() || DidxHelper.isBeta() || DidxHelper.isLocal()) {

			String[] messIdentify = productIDList.split("\\,");
        String lang = site != null ? site.getLangID() : "vi-VN";
        for (String productid : messIdentify) {
            MessageQueue message = new MessageQueue();

            message.Action = DataAction.Add;
            message.Note = "";
            message.Identify = productid;
            message.ClassName = "STATUS";
            message.CreatedDate = Utils.GetCurrentDate();
            message.ID = 0;
            message.SiteID = site.getValue();
            message.Lang = lang;
            message.Version = 0;
            message.DataCenter = DidxHelper.isBeta() ? 3 : 0;
            if (site != null && site.getValue() == 11) {
                message.Note = "BHXTEST";
            }
            try {
                QueueHelper.Current(ConfigUtils.GetOnlineClientConfig().SERVER_RABBITMQ_URL).Push("ms.init", message);
                ra = ra + "," + productid;
            } catch (Exception e) {
            	Logs.LogException(e);
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.OK);
            }
        }
		}else{
			ra = "Push only on beta or staging !!!";


		}


        return new ResponseEntity<String>(ra, HttpStatus.OK);
    }

    @RequestMapping(value = "/pushnews", method = RequestMethod.GET)
    public ResponseEntity<String> PushInitNews(String newsID, String Logs, int DataCenter) {
        String messIdentify = newsID;

			MessageQueue message = new MessageQueue();

			message.Action = DataAction.Add;
			message.Note = Logs;
			message.Identify = messIdentify;
			message.ClassName = "NEWS";
			message.CreatedDate = Utils.GetCurrentDate();
			message.ID = 0;
			message.SiteID = 1;
			message.Lang = "vi-VN";
			message.Version = 0;
			message.DataCenter = DataCenter;
			try {
				QueueHelper.Current(ConfigUtils.GetOnlineClientConfig().SERVER_RABBITMQ_URL).Push("ms.init", message);

			} catch (Exception e) {
				return new ResponseEntity<String>(e.getMessage(), HttpStatus.OK);
			}


        return new ResponseEntity<>("#Push news " + newsID + " success!", HttpStatus.OK);
    }

    @RequestMapping(value = "/push", method = RequestMethod.POST)
    public ResponseEntity<String> PushInitProduct(String productIDList, SiteID site) {
		String ra = "";
		if (DidxHelper.isStaging() || DidxHelper.isBeta() || DidxHelper.isLocal()) {

			String[] messIdentify = productIDList.split("\\,");

			String lang = site != null ? site.getLangID() : "vi-VN";

			int DataCenter = DidxHelper.isBeta() ? 3 : 0;
//		String lang = "vi-VN";
//		if (!Utils.StringIsEmpty(Lang)) {
//			lang = Lang;
//		}
			for (String productid : messIdentify) {
				MessageQueue message = new MessageQueue();

				message.Action = DataAction.Add;
				message.Note = "";
				message.Identify = productid;
				message.ClassName = "ALL";
				message.CreatedDate = Utils.GetCurrentDate();
				message.ID = 0;
				message.SiteID = site == null ? 1 : site.getValue();
				message.Lang = lang;
				message.Version = 0;
				message.DataCenter = DataCenter;
				try {
                    QueueHelper.Current(ConfigUtils.GetOnlineClientConfig().SERVER_RABBITMQ_URL).Push("ms.init", message);
                    ra = ra + "," + productid;
                } catch (Exception e) {
					return new ResponseEntity<String>(e.getMessage(), HttpStatus.OK);
				}
			}
		}else{
			ra = "Push only on beta or staging !!!";

		}

        return new ResponseEntity<String>(ra, HttpStatus.OK);
    }

    @RequestMapping(value = "/pushpromotionsubbrand", method = RequestMethod.POST)
    public ResponseEntity<String> pushPromotionSubBrand(int subgroupID, int brandID, int siteID, String langID) {
        String id = subgroupID + "|" + brandID;
        boolean beta = DidxHelper.isBeta() || DidxHelper.isLocal();
        String q = beta ? "gr.beta.didx.promotiongroup" : "gr.dc4.didx.promotiongroup";
        MessageQueue m = new MessageQueue() {
            {
                Action = DataAction.Add;
                Identify = id;
                Note = "";
                SiteID = siteID;
                DataCenter = beta ? 3 : 4;
                Lang = langID;
                CreatedDate = new Date();
                ClassName = "mwg.wb.pkg.promotion.PromotionGroup";
            }
        };
        try {
            QueueHelper.Current(ConfigUtils.GetOnlineClientConfig().SERVER_RABBITMQ_URL).Push(q, m);
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.OK);
        }
        return new ResponseEntity<String>(id, HttpStatus.OK);
    }

}
