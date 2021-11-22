package mwg.wb.webapi.config;

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
				.apis(RequestHandlerSelectors.basePackage("mwg.wb.webapi.controller"))
				//.paths(PathSelectors.ant("/api*"))
				.paths(PathSelectors.regex("/api.*?"))
				.build().apiInfo(apiInfo());
				
	}



	public ApiInfo apiInfo() {
		return new ApiInfo("WebAPI - Java Restful API",
				"This is Restful API for Microservice", "V1.0",
				"Hiep Si Mat Na Diend",
				new Contact("Thanh Phi", "https://www.thegioididong.com", "nguyenthanhphi0401@gmail.com"),
				"License of API", "API license URL", java.util.Collections.emptyList());
	}
}