package com.opens.datadictionary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { MultipartAutoConfiguration.class })
@ComponentScan(basePackages = { "com.opens.datadictionary" })
@EnableTransactionManagement
public class DataDictionaryApp {

	public static void main(String[] args) {
		SpringApplication.run(DataDictionaryApp.class, args);
	}
}
