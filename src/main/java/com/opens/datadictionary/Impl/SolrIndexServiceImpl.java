package com.opens.datadictionary.Impl;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.opens.datadictionary.service.SolrIndexService;
import com.opens.datadictionary.solr.models.ApiResource;
import com.opens.datadictionary.solr.models.ParamDetails;
import com.opens.datadictionary.solr.models.SolrDocumentDto;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

@Service
public class SolrIndexServiceImpl implements SolrIndexService {

	private static final Logger logger = LoggerFactory.getLogger(SolrIndexServiceImpl.class);

	@Override
	public SolrDocumentDto transform(Swagger swagger) {
		logger.info("Swagger received for transformation.");
		SolrDocumentDto solrDocumentDto = new SolrDocumentDto();
		
		//Setting up common fields
		solrDocumentDto.setBaseUrl(swagger.getBasePath());
		solrDocumentDto.setHost(swagger.getHost());
		if(swagger.getInfo() != null) {
			solrDocumentDto.setDescription(swagger.getInfo().getDescription());
			solrDocumentDto.setTitle(swagger.getInfo().getTitle());
		}
		
		if(swagger.getPaths() != null && swagger.getPaths().size() > 0) {
			solrDocumentDto.getApiResources().addAll(swagger.getPaths().entrySet().stream().map(new Function<Entry<String,Path>, ApiResource>() {

				@Override
				public ApiResource apply(Entry<String, Path> t) {
					ApiResource apiResource = new ApiResource();
					apiResource.setResourceUrl(t.getKey());
					if(t.getValue() != null) {
						Operation operation = null;
						if(t.getValue().getGet() != null) {
							operation = t.getValue().getGet();
							apiResource.setMethodName(HttpMethod.GET.name());
						}
						if(t.getValue().getPost() != null) {
							operation = t.getValue().getPost();
							apiResource.setMethodName(HttpMethod.POST.name());
						}
						
						if(operation != null) {
							apiResource.setDescription(operation.getDescription());
							apiResource.setOperationId(operation.getOperationId());
							apiResource.setSummary(operation.getSummary());
							apiResource.setTags(operation.getTags());
							
							if(operation.getParameters() != null) {
								for(Parameter param : operation.getParameters()) {
									if(param instanceof HeaderParameter) {
										ParamDetails paramDetails = new ParamDetails();
										paramDetails.setParamHttpType(param.getIn());
										paramDetails.setParamjavaType(((HeaderParameter) param).getType());
										paramDetails.setName(param.getName());
										paramDetails.setRequired(param.getRequired());
										apiResource.getParams().add(paramDetails);
									}
									if(param instanceof BodyParameter) {
										ParamDetails paramDetails = new ParamDetails();
										paramDetails.setParamHttpType(param.getIn());
										paramDetails.setName(param.getName());
										paramDetails.setRequired(param.getRequired());
										apiResource.getParams().add(paramDetails);
										if(((BodyParameter) param).getSchema() != null && ((BodyParameter) param).getSchema().getProperties() != null) {
											paramDetails.getPostParamDetails().addAll(((BodyParameter) param).getSchema().getProperties().entrySet().stream().map(new Function<Entry<String, Property>,ParamDetails>() {

												@Override
												public ParamDetails apply(Entry<String, Property> t) {
													ParamDetails paramDetails = new ParamDetails();
													paramDetails.setParamjavaType(t.getValue().getType());
													paramDetails.setName(t.getKey());
													paramDetails.setRequired(t.getValue().getRequired());
													paramDetails.setDescription(t.getValue().getDescription());
													return paramDetails;
												}
											}).collect(Collectors.toList()));
										}
									}
								}
								
							}
						}
					}
					return apiResource;
				}
			}).collect(Collectors.toList()));
		}
		logger.info("Transformed Solr Document = {} ",new Gson().toJson(solrDocumentDto));
		return solrDocumentDto;
	}

}
