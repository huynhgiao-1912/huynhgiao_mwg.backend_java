package mwg.wb.webservice.controller;

import mwg.wb.business.LogHelper;
import mwg.wb.business.SystemHelper;
import mwg.wb.business.webservice.SystemSvcHelper;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.system.SystemConfigBO;
import mwg.wb.webservice.common.ConfigUtils;
import mwg.wb.webservice.common.HeaderBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apisystem") // newsService
public class SystemController {
    private static ClientConfig _config = null;
    private static SystemSvcHelper _systemsvcHelper = null;

    private static synchronized SystemSvcHelper getSystemSvcHelper() {
        if (_config == null) {
            ClientConfig config = ConfigUtils.GetOnlineClientConfig();
            _config = config;
        }
        if (_systemsvcHelper == null) {
            return _systemsvcHelper = new SystemSvcHelper(_config);
        }
        return _systemsvcHelper;
    }

    @GetMapping(value = "/getallsystemconfig")
    public ResponseEntity<SystemConfigBO[]> getAllSystemConfig() {
        var timer = new CodeTimer("timer-all");
        var odbtimer = new CodeTimer("timer-odb");
        var status = HttpStatus.OK;
        SystemConfigBO[] result = null;
        try {
            odbtimer.reset();
            result = getSystemSvcHelper().getListSystemConfig();
            odbtimer.end();
        } catch (SQLException throwables) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            LogHelper.WriteLog(throwables);
        }
        timer.end();
        return new ResponseEntity<SystemConfigBO[]>(result, HeaderBuilder.buildHeaders(timer), status);
    }


}
