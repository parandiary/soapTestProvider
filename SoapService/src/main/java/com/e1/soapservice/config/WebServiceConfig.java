package com.e1.soapservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;


@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

	private final Logger log = LoggerFactory.getLogger(WebServiceConfig.class);

	/*
	 * @Bean public WebMvcConfigurer corsConfigurer() { return new
	 * WebMvcConfigurer() {
	 *
	 * @Override public void addCorsMappings(CorsRegistry registry) {
	 * //registry.addMapping("/greeting-javaconfig").allowedOrigins(
	 * "http://localhost:8080");
	 * //registry.addMapping("/ws").allowedOrigins("http://localhost:8080");
	 * //registry.addMapping("/**").allowedOrigins("*");
	 *
	 * registry.addMapping("/**") .allowedOriginPatterns("*") //.allowedOrigins("*")
	 * //.allowedOrigins("http://localhost:4200")
	 * //.allowedMethods(HttpMethod.POST.name()) .allowCredentials(false)
	 * .maxAge(3600); } }; }
	 */


	@Bean
	public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
		MessageDispatcherServlet servlet = new MessageDispatcherServlet();
		servlet.setApplicationContext(applicationContext);
		servlet.setTransformWsdlLocations(true);
		return new ServletRegistrationBean<>(servlet, "/ws/*");
	}

	@Bean(name = "countries")
	public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema countriesSchema) {
		DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
		wsdl11Definition.setPortTypeName("CountriesPort");
		wsdl11Definition.setLocationUri("/ws");
		wsdl11Definition.setTargetNamespace("http://spring.io/guides/gs-producing-web-service");
		wsdl11Definition.setSchema(countriesSchema);
		return wsdl11Definition;
	}

	@Bean
	public XsdSchema countriesSchema() {
		return new SimpleXsdSchema(new ClassPathResource("countries.xsd"));
	}

	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(false);
		//config.addAllowedOrigin("http://domain1.com");
		//config.addAllowedOrigin("*");
		config.addAllowedOriginPattern("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		source.registerCorsConfiguration("/**", config);
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<CorsFilter>(new CorsFilter(source));
		bean.setOrder(0);
		return bean;
	}




	/*
	 * @Bean public CorsFilter corsFilter() { UrlBasedCorsConfigurationSource source
	 * = new UrlBasedCorsConfigurationSource(); CorsConfiguration config =
	 * jHipsterProperties.getCors();
	 * log.info("=========== CORS FILTER ===============");
	 * log.info("config.getAllowedOrigins() : "+config.getAllowedOrigins());
	 * log.info("config.getAllowedMethods() : "+config.getAllowedMethods());
	 * log.info("config.getAllowedHeaders() : "+config.getAllowedHeaders());
	 * log.info("config.getAllowCredentials() : "+config.getAllowCredentials());
	 * log.debug("Registering CORS filter");
	 *
	 * if (config.getAllowedOrigins() != null &&
	 * !config.getAllowedOrigins().isEmpty()) {
	 * log.debug("Registering CORS filter");
	 * source.registerCorsConfiguration("/api/**", config);
	 * source.registerCorsConfiguration("/management/**", config);
	 * source.registerCorsConfiguration("/excel/**", config);
	 * source.registerCorsConfiguration("/v2/api-docs", config); } return new
	 * CorsFilter(source); }
	 */
}