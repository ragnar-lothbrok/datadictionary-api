package com.opens.datadictionary.service;

import java.util.List;

import com.opens.datadictionary.solr.models.SolrDocumentDto;

import io.swagger.models.Swagger;

public interface SolrIndexService {

	List<SolrDocumentDto> transform(Swagger swagger, String docId);

	Boolean indexDocuments(List<SolrDocumentDto> dtos);
}
