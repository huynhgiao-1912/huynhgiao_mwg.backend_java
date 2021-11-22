package mwg.wb.webservice.controller;

import java.util.concurrent.locks.ReentrantLock;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import mwg.wb.business.webservice.ProductSvcHelper;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.webservice.common.ConfigUtils;
import mwg.wb.webservice.common.HeaderBuilder;

//@PropertySource("classpath:/bootstrap.properties")
@RestController
@Configuration
@RefreshScope
@RequestMapping("/apiproduct")
public class ProductController {

	private static ClientConfig _config = null;
	private static ProductSvcHelper productHelper = null;

	// lockers
	private static ReentrantLock configLocker = new ReentrantLock(), productLocker = new ReentrantLock();

	// helpers
	private static ClientConfig getConfig() {
		try {
			configLocker.lock();
			if (_config == null) {
				_config = ConfigUtils.GetOnlineClientConfig();
			}
		} finally {
			configLocker.unlock();
		}
		return _config;
	}

	private static ProductSvcHelper getHelper() {
		try {
			productLocker.lock();
			if (productHelper == null) {
				var config = getConfig();
				if (config != null) {
					productHelper = new ProductSvcHelper(config);
				}
			}
		} finally {
			productLocker.unlock();
		}
		return productHelper;
	}

	/////////

	@RequestMapping(value = "/getsoldproductquantity", method = RequestMethod.GET)
	public ResponseEntity<Integer> getSoldProductQuantity(int siteID, int productID, long fromDate, long toDate) {
		var timer = new CodeTimer("timer");
		int count = -1;
		try {
			count = getHelper().getSoldProductQuantity(siteID, productID, fromDate, toDate);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		timer.end();
		return new ResponseEntity<>(count, HeaderBuilder.buildHeaders(timer), HttpStatus.OK);
	}

	@RequestMapping(value = "/updateproductstatus", method = RequestMethod.GET)
	public ResponseEntity<Integer> updateProductStatus(int productID, int statusID, int siteID, String languageID,
			double price) {
		var timer = new CodeTimer("timer");
		int count = -1;
		try {
			count = getHelper().updateProductStatus(productID, statusID, siteID, languageID, price);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		timer.end();
		return new ResponseEntity<>(count, HeaderBuilder.buildHeaders(timer), HttpStatus.OK);
	}

	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public ResponseEntity<String> Welcome() {
		String welcome = "Hello world!";
		var codetimer = new CodeTimer("timer-all");
		ClientConfig config = ConfigUtils.GetOnlineClientConfig();
		_config = config;
		var header = HeaderBuilder.buildHeaders(codetimer);
		return new ResponseEntity<>(welcome, header, HttpStatus.OK);
	}
}
