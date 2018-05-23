package com.opens.datadictionary.service;

import com.opens.datadictionary.solr.models.SolrDocumentDto;

import io.swagger.models.Swagger;

public interface SolrIndexService {

	SolrDocumentDto transform(Swagger swagger);
}
