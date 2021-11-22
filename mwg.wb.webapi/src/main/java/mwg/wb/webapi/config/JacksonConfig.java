package mwg.wb.webapi.config;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import mwg.wb.common.DidxHelper;
import mwg.wb.common.GConfig;

//@Configuration
public class JacksonConfig {
//	@Bean
	public ObjectMapper buildObjectMapper() {
		return DidxHelper.generateNonNullJsonMapper(GConfig.DateFormatString)
				.setSerializationInclusion(Include.NON_NULL);
	}
}
