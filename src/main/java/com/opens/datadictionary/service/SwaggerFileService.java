package com.opens.datadictionary.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.opens.datadictionary.mongo.models.SwaggerDetail;
import com.opens.datadictionary.solr.dtos.SearchRequest;

import io.swagger.models.Swagger;

public interface SwaggerFileService {

	void store(MultipartFile file);

	List<Swagger> searchSwaggers(SearchRequest searchRequest);

	List<SwaggerDetail> uploadHistory();

	String getFilePath(String fileName);
}
