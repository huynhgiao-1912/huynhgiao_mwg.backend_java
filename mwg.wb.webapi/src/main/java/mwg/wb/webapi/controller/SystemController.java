package mwg.wb.webapi.controller;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import mwg.wb.model.general.ProvinceBO;
import mwg.wb.model.system.SystemConfigBO;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;

import mwg.wb.business.LogHelper;
import mwg.wb.business.ProductHelper;
import mwg.wb.business.SystemHelper;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.html.InfoBO;
import mwg.wb.model.system.KeyWordBO;
import mwg.wb.webapi.service.ConfigUtils;
import mwg.wb.webapi.service.HeaderBuilder;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apisystem")
public class SystemController {

    private static SystemHelper _systemHelper = null;
    private static ClientConfig config = null;
    private static ProductHelper productHelper = null;
    private static SystemHelper proHelper = null;
    private static ORThreadLocal oclient = null;

    // lockers
    private static ReentrantLock phelperLocker = new ReentrantLock(), configLocker = new ReentrantLock(),
            orientLocker = new ReentrantLock(), syshelperLocker = new ReentrantLock();

    private static ORThreadLocal getOrientClient() throws JsonParseException, IOException {
        try {
            orientLocker.lock();
            if (oclient == null) {
                oclient = new ORThreadLocal();
                oclient.initReadAPI(getConfig(), 0);
            }
            return oclient;
        } finally {
            orientLocker.unlock();
        }
    }

    private static ProductHelper getProductHelper() throws JsonParseException, IOException {
        try {
            phelperLocker.lock();
            if (productHelper == null) {
                productHelper = new ProductHelper(getOrientClient(), getConfig());
            }
            return productHelper;
        } finally {
            phelperLocker.unlock();
        }
    }


    private static ClientConfig getConfig() {
        try {
            configLocker.lock();
            if (config == null) {
                config = ConfigUtils.GetOnlineClientConfig();
            }
            return config;
        } finally {
            configLocker.unlock();
        }
    }

    public static SystemHelper GetSystemClient() {
        try {
            syshelperLocker.lock();
            if (_systemHelper == null) {
                ClientConfig config = getConfig();
                _systemHelper = new SystemHelper(config);
            }
            return _systemHelper;
        } finally {
            syshelperLocker.unlock();
        }
    }

    public SystemController() {
    }



    @RequestMapping(value = "/gethtmlinfo", method = RequestMethod.GET)
    public ResponseEntity<InfoBO> getHTMLInfo(int htmlID, int siteID, String langID) {
        var timer = new CodeTimer("timer-all");
        InfoBO info = null;
        var status = HttpStatus.OK;
        try {
            info = GetSystemClient().getHTMLInfo(htmlID, siteID, langID);
        } catch (Throwable e) {
            LogHelper.WriteLog(e);
            status = HttpStatus.INTERNAL_SERVER_ERROR;

        }
        timer.end();
        return new ResponseEntity<>(info, HeaderBuilder.buildHeaders(timer), status);
    }

    /**
     * GetKeyWordByCategory
     */
    @RequestMapping(value = "/getkeywordbycategory", method = RequestMethod.GET)
    public ResponseEntity<KeyWordBO[]> getKeyWordByCategory(int categoryID, int siteID) {
        var timer = new CodeTimer("timer-all");
        var status = HttpStatus.OK;
        KeyWordBO[] keys = null;
        try {
            keys = getProductHelper().GetListKeyWordByCateFromCache(categoryID, siteID);
        } catch (Throwable e) {
            LogHelper.WriteLog(e);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        timer.end();
        return new ResponseEntity<>(keys, HeaderBuilder.buildHeaders(timer), status);
    }
 
    @GetMapping(value = "/getallsystemconfig")
    public ResponseEntity<SystemConfigBO[]> getAllSystemConfig() {
        var timer = new CodeTimer("time-all");
        var status = HttpStatus.OK;
        SystemConfigBO[] result = null;
        try {
            result=  GetSystemClient().getListSystemConfig();
        } catch (Throwable throwable) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            LogHelper.WriteLog(throwable);
        }
        timer.end();
        return new ResponseEntity<>(result, HeaderBuilder.buildHeaders(timer), status);
    }

//	@RequestMapping(value = "/get", method = RequestMethod.GET)
//	public ResponseEntity<> get() {
//		 result = null;
//		var status = HttpStatus.OK;
//
//		try {
//		} catch (Throwable e) {
//			status = HttpStatus.INTERNAL_SERVER_ERROR;
//			LogHelper.WriteLog(e);
//		}
//		return new ResponseEntity<>(result, status);
//	}
}
