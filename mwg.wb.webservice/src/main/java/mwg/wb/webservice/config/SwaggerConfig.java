package mwg.wb.webservice.config;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@ComponentScan
@Configuration
@RefreshScope
@EnableSwagger2
public class SwaggerConfig {
	@Bean
	public Docket productapi() {
		return new Docket(DocumentationType.SWAGGER_2)
				//.groupName("product")
				.select()
				.apis(RequestHandlerSelectors.basePackage("mwg.wb.webservice.controller"))
				//.paths(PathSelectors.ant("/api*"))
				.paths(PathSelectors.regex("/api.*?"))
				.build().apiInfo(apiInfo());
				
	}



	public ApiInfo apiInfo() {
		return new ApiInfo("Java Rest Webservice for TGDD",
				"This is Restful Webservice for Microservice", "V1.0",
				"Nguyễn Thanh Phi",
				new Contact("Nguyễn Thanh Phi", "https://www.thegioididong.com", "nguyenthanhphi0401@gmail.com"),
				"License of MWG", "https://www.thegioididong.com", java.util.Collections.emptyList());
	}
}