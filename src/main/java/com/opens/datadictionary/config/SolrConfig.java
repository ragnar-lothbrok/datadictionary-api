package com.opens.datadictionary.config;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolrConfig {

	@Value("${solr.server.url}")
	private String solrServerUrl;

	@Bean
	public HttpSolrClient getSolrClient() {
		HttpSolrClient solr = new HttpSolrClient.Builder(solrServerUrl).build();
		solr.setParser(new XMLResponseParser());
		return solr;
	}

}
