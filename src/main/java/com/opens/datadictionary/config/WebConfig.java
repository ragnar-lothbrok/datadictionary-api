package com.opens.datadictionary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver multipartResolver() {
		return new CommonsMultipartResolver();
	}

}