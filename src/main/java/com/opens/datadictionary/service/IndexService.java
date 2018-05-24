package com.opens.datadictionary.service;

import java.util.List;

import com.opens.datadictionary.solr.models.SolrDocumentDto;

import io.swagger.models.Swagger;

public interface IndexService {

	List<SolrDocumentDto> transform(Swagger swagger, String docId, String fileName);

	Boolean indexDocuments(List<SolrDocumentDto> dtos);
}
