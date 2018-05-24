package com.opens.datadictionary.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.opens.datadictionary.constants.APIEndpoints;
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

	@Autowired
	private HttpSolrClient httpSolrClient;

	@Override
	public List<SolrDocumentDto> transform(Swagger swagger, String docId) {
		logger.info("Swagger received for transformation.");
		List<SolrDocumentDto> solrDocumentDtos = new ArrayList<>();

		SolrDocumentDto solrDocumentDto = new SolrDocumentDto();

		// Setting up common fields
		solrDocumentDto.setId(docId);
		solrDocumentDto.setBaseUrl(swagger.getBasePath());
		solrDocumentDto.setHost(swagger.getHost());
		if (swagger.getInfo() != null) {
			solrDocumentDto.setDescription(swagger.getInfo().getDescription());
			solrDocumentDto.setTitle(swagger.getInfo().getTitle());
		}

		if (swagger.getPaths() != null && swagger.getPaths().size() > 0) {
			List<ApiResource> apisList = swagger.getPaths().entrySet().stream()
					.map(new Function<Entry<String, Path>, ApiResource>() {
						@Override
						public ApiResource apply(Entry<String, Path> t) {
							ApiResource apiResource = new ApiResource();
							List<ParamDetails> paramDetailsList = new ArrayList<ParamDetails>();
							apiResource.setResourceUrl(t.getKey());
							if (t.getValue() != null) {
								Operation operation = null;
								if (t.getValue().getGet() != null) {
									operation = t.getValue().getGet();
									apiResource.setMethodName(HttpMethod.GET.name());
								}
								if (t.getValue().getPost() != null) {
									operation = t.getValue().getPost();
									apiResource.setMethodName(HttpMethod.POST.name());
								}

								if (operation != null) {
									apiResource.setDescription(operation.getDescription());
									apiResource.setOperationId(operation.getOperationId());
									apiResource.setSummary(operation.getSummary());
									apiResource.setTags(operation.getTags());

									if (operation.getParameters() != null) {
										for (Parameter param : operation.getParameters()) {
											if (param instanceof HeaderParameter) {
												ParamDetails paramDetails = new ParamDetails();
												paramDetails.setParamHttpType(param.getIn());
												paramDetails.setParamjavaType(((HeaderParameter) param).getType());
												paramDetails.setName(param.getName());
												paramDetails.setRequired(param.getRequired());
												paramDetailsList.add(paramDetails);
											}
											if (param instanceof BodyParameter) {
												ParamDetails paramDetails = new ParamDetails();
												paramDetails.setParamHttpType(param.getIn());
												paramDetails.setName(param.getName());
												paramDetails.setRequired(param.getRequired());
												paramDetailsList.add(paramDetails);
												if (((BodyParameter) param).getSchema() != null
														&& ((BodyParameter) param).getSchema()
																.getProperties() != null) {
													List<ParamDetails> postParamDetails = ((BodyParameter) param)
															.getSchema().getProperties().entrySet().stream()
															.map(new Function<Entry<String, Property>, ParamDetails>() {
																@Override
																public ParamDetails apply(Entry<String, Property> t) {
																	ParamDetails paramDetails = new ParamDetails();
																	paramDetails
																			.setParamjavaType(t.getValue().getType());
																	paramDetails.setName(t.getKey());
																	paramDetails
																			.setRequired(t.getValue().getRequired());
																	paramDetails.setDescription(
																			t.getValue().getDescription());
																	return paramDetails;
																}
															}).collect(Collectors.toList());
													paramDetailsList.addAll(postParamDetails);
												}
											}
										}
									}

								}
							}
							apiResource.getParamDetails().addAll(paramDetailsList);
							return apiResource;
						}
					}).collect(Collectors.toList());

			for (ApiResource apiResource : apisList) {
				List<ParamDetails> paramDetails = apiResource.getParamDetails();
				for (ParamDetails paramDetail : paramDetails) {
					SolrDocumentDto flatSolrDocumentDto = solrDocumentDto.clone();
					ApiResource flatApiResource = apiResource.clone();
					String id = flatSolrDocumentDto.getId() + APIEndpoints.SEPERATOR + flatApiResource.getResourceUrl()
							+ APIEndpoints.SEPERATOR + flatApiResource.getMethodName() + APIEndpoints.SEPERATOR
							+ paramDetail.getParamHttpType() + APIEndpoints.SEPERATOR + paramDetail.getName();
					flatSolrDocumentDto.setId(id);
					flatSolrDocumentDto.setApiResource(flatApiResource);
					flatSolrDocumentDto.setParamDetails(paramDetail);
					solrDocumentDtos.add(flatSolrDocumentDto);
				}
			}
		}
		logger.info("Transformed Solr Document = {} ", new Gson().toJson(solrDocumentDtos));
		return solrDocumentDtos;
	}

	@Override
	public Boolean indexDocuments(List<SolrDocumentDto> dtos) {
		logger.info("Indexing started...");
		if (dtos != null) {
			for (SolrDocumentDto dto : dtos) {
				try {
					SolrInputDocument document = new SolrInputDocument();
					document.setField("id", dto.getId());
					document.setField("title", dto.getTitle());
					document.setField("description", dto.getDescription());
					document.setField("baseUrl", dto.getBaseUrl());
					document.setField("host", dto.getHost());

					document.setField("apiResource.resourceUrl", dto.getApiResource().getResourceUrl());
					document.setField("apiResource.methodName", dto.getApiResource().getMethodName());
					document.setField("apiResource.methodName_tags", dto.getApiResource().getTags());
					document.setField("apiResource.summary", dto.getApiResource().getSummary());
					document.setField("apiResource.operationId", dto.getApiResource().getOperationId());

					document.setField("paramDetails.name", dto.getParamDetails().getName());
					document.setField("paramDetails.paramHttpType", dto.getParamDetails().getParamHttpType());
					document.setField("paramDetails.description", dto.getParamDetails().getDescription());
					document.setField("paramDetails.required", dto.getParamDetails().isRequired());
					document.setField("paramDetails.paramjavaType", dto.getParamDetails().getParamjavaType());
					httpSolrClient.add(document);
					httpSolrClient.commit();
				} catch (Exception e) {
					logger.info("Not able to index Solr Documents = {} ", e);
				}
			}
			logger.info("Docuements indexed = {}", dtos.size());
		}
		return true;
	}

}
