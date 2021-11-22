package mwg.wb.webapi.controller;


import mwg.wb.business.helper.FacebookHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimers;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.fb.FBinpuModel;
import mwg.wb.model.fb.FacebookResult;
import mwg.wb.model.products.ProductManuBO;
import mwg.wb.webapi.service.ConfigUtils;
import mwg.wb.webapi.service.HeaderBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apifb")
public class FacebookController {

    private static FacebookHelper _tgddfbHelper = null;
    private static ORThreadLocal factoryRead = null;
    private static ClientConfig _config = null;

    private static synchronized FacebookHelper GetFBClientBySiteID() {

        if (_config == null) {
            ClientConfig config = ConfigUtils.GetOnlineClientConfig();
            _config = config;
            _config.SERVER_ELASTICSEARCH_FB_READ_HOST = "172.16.3.105";
        }
        if (factoryRead == null) {
            try {
                factoryRead = new ORThreadLocal();
            } catch (Throwable e) {

                e.printStackTrace();
            }
            factoryRead.initReadAPI(_config, 0);
        }
        if (_tgddfbHelper == null) {

            _tgddfbHelper = new FacebookHelper( _config);
        }
        return _tgddfbHelper;

    }

    @RequestMapping(value = "/SendMessengerBySenderID", method = RequestMethod.POST)
    public ResponseEntity<FacebookResult> SendMessengerBySenderID(@RequestBody FBinpuModel fb) {
        var status = HttpStatus.OK;
        var timer = new CodeTimers();
        FacebookResult result = null;
        try{
            _tgddfbHelper = GetFBClientBySiteID();
            timer.start("backend-timer-all");
            // Backend_wewemPKz157845_Tgdd
            result = _tgddfbHelper.SendMessage(fb);
            timer.pause("backend-timer-all");

        }catch (Throwable e){
            status =  HttpStatus.INTERNAL_SERVER_ERROR;
            e.printStackTrace();
        }

        return new ResponseEntity<FacebookResult>(result, HeaderBuilder.buildHeaders(timer),
                status);
    }
}
