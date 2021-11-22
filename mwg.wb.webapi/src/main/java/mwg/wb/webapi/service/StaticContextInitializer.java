package mwg.wb.webapi.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import mwg.wb.webapi.config.CentralConfig;

@Component
public class StaticContextInitializer {
	@Autowired
	private CentralConfig myConfig;
	@Autowired
	private ApplicationContext context;

	@PostConstruct
	public void init() {
		ConfigUtils.setMyConfig(myConfig);
	}
}