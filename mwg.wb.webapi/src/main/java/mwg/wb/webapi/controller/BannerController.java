package mwg.wb.webapi.controller;

import java.util.Comparator;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import mwg.wb.business.LogHelper;
import mwg.wb.client.service.CodeTimer;
import mwg.wb.model.ad.BannerBO;
import mwg.wb.webapi.APIOrientClient;
import mwg.wb.webapi.service.HeaderBuilder;

@RestController
@Configuration
@RefreshScope
@RequestMapping("/apibanner")
public class BannerController {
	
	@RequestMapping(value = "/getbannerbysite", method = RequestMethod.GET)
	public ResponseEntity<BannerBO[]> getBannerBySite(int siteID, int categoryID, int placeID) {
		var timer = new CodeTimer("timer-all");
		BannerBO[] banner=null;
		try {
			banner = APIOrientClient.GetOrientClient().QueryFunctionCached("banners_GetInfo", BannerBO[].class, false,
					categoryID, placeID, siteID);
		
		banner = Stream.of(banner)
				.sorted(Comparator.comparingInt(BannerBO::orderValue)
						.thenComparing(Comparator.comparingInt(BannerBO::id).reversed()))
				.flatMap(b -> b.expandManu()).toArray(BannerBO[]::new);
		} catch (Throwable e) {
			LogHelper.WriteLog(e );
		}
		timer.end();
		return new ResponseEntity<BannerBO[]>(banner, HeaderBuilder.buildHeaders(timer), HttpStatus.OK);
	}
}
