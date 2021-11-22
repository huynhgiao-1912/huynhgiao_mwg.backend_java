 package mwg.wb.webapi;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.google.common.collect.Lists; 
import mwg.wb.webapi.service.ConfigUtils;

@SpringBootApplication
@EnableDiscoveryClient
@EnableEurekaClient
@EnableCircuitBreaker
@EnableHystrix
public class JavaRestAPI {

	public static void main(String[] args) {
		SpringApplication.run(JavaRestAPI.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
/*	 
	    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
//        FilterRegistrationBean filterBean = new FilterRegistrationBean();
//        filterBean.setFilter(new ShallowEtagHeaderFilter());
//        filterBean.setUrlPatterns(Arrays.asList("*"));
//        
//         
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(); 
        filterRegistrationBean.setFilter(new SecondFilter());
        List<String> urls = Lists.newArrayList();
        urls.add("/*");
        filterRegistrationBean.setUrlPatterns(urls);
       // filterRegistrationBean.setOrder(2);
        return filterRegistrationBean;
        
         
    }
*/	
	
//    @Bean
//    public FilterRegistrationBean filterRegistrationBean() {
//        FilterRegistrationBean filterBean = new FilterRegistrationBean();
//        filterBean.setFilter(new ShallowEtagHeaderFilter());
//        filterBean.setUrlPatterns(Arrays.asList("*"));
//        return filterBean;
//    }
//    @Component
//    public class CachingRequestBodyFilter extends GenericFilterBean {
//        @Override
//        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
//          throws IOException, ServletException {
//            HttpServletRequest currentRequest = (HttpServletRequest) servletRequest;
//            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(currentRequest);
//            chain.doFilter(wrappedRequest, servletResponse);
//        }
//    }
//    @Bean
//    public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
//    	 FilterRegistrationBean filterBean = new FilterRegistrationBean();
//         filterBean.setFilter(new ShallowEtagHeaderFilter());
//         filterBean.setUrlPatterns(Arrays.asList("*"));
//         return filterBean;
//      //  return new ShallowEtagHeaderFilter();
//    }
}
