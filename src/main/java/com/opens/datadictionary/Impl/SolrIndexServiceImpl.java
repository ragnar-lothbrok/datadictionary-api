package com.opens.datadictionary.Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import com.opens.datadictionary.service.IndexService;
import com.opens.datadictionary.solr.models.ApiResource;
import com.opens.datadictionary.solr.models.SolrDocumentDto;

import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

@Service
public class SolrIndexServiceImpl implements IndexService {

	private static final Logger logger = LoggerFactory.getLogger(SolrIndexServiceImpl.class);

	@Autowired
	private HttpSolrClient httpSolrClient;

	@Override
	public List<SolrDocumentDto> transform(Swagger swagger, String docId, String fileName) {
		logger.info("Swagger received for transformation.");
		List<SolrDocumentDto> solrDocumentDtos = new ArrayList<>();

		SolrDocumentDto solrDocumentDto = new SolrDocumentDto();

		// Setting up common fields
		solrDocumentDto.setSwaggerDocId(docId);
		solrDocumentDto.setFileName(fileName);
		solrDocumentDto.setBaseUrl(swagger.getBasePath());
		solrDocumentDto.setHost(swagger.getHost());
		if (swagger.getInfo() != null) {
			solrDocumentDto.setDescription(swagger.getInfo().getDescription());
			solrDocumentDto.setTitle(swagger.getInfo().getTitle());
		}

		final Map<String, Set<String>> responseMap = responseMap(swagger);

		if (swagger.getPaths() != null && swagger.getPaths().size() > 0) {
			List<ApiResource> apisList = swagger.getPaths().entrySet().stream()
					.map(new Function<Entry<String, Path>, ApiResource>() {
						@Override
						public ApiResource apply(Entry<String, Path> t) {
							ApiResource apiResource = new ApiResource();
							List<String> fieldsMetaData = new ArrayList<>();
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

								if (t.getValue().getPut() != null) {
									operation = t.getValue().getPut();
									apiResource.setMethodName(HttpMethod.PUT.name());
								}

								// Response
								for (Entry<String, Response> response : operation.getResponses().entrySet()) {
									if (response.getValue().getSchema() != null
											&& response.getValue().getSchema() instanceof RefProperty) {
										String splits[] = ((RefProperty) response.getValue().getSchema()).get$ref()
												.split("\\/");
										String responseDef = splits[splits.length - 1];
										apiResource.getResponseDef().add(responseDef);
									}
								}

								if (operation != null) {
									apiResource.setDescription(operation.getDescription());
									apiResource.setOperationId(operation.getOperationId());
									apiResource.setSummary(operation.getSummary());
									apiResource.setTags(operation.getTags());

									if (operation.getParameters() != null) {
										for (Parameter param : operation.getParameters()) {
											if (param.getDescription() != null) {
												fieldsMetaData.add(param.getDescription());
											}
											if (param instanceof HeaderParameter) {
												apiResource.getRequestFields().add(param.getName());
											}
											if (param instanceof PathParameter) {
												apiResource.getQueryFields().add(param.getName());
											}
											if (param instanceof QueryParameter) {
												apiResource.getQueryFields().add(param.getName());
											}
											if (param instanceof BodyParameter) {
												apiResource.getHeaderFields().add(param.getName());
												if (((BodyParameter) param).getSchema() != null) {
													if (((BodyParameter) param).getSchema().getProperties() != null) {
														apiResource.getRequestFields().addAll(((BodyParameter) param)
																.getSchema().getProperties().entrySet().stream()
																.map(new Function<Entry<String, Property>, String>() {
																	@Override
																	public String apply(Entry<String, Property> t) {
																		return t.getKey();
																	}
																}).collect(Collectors.toSet()));
													} else if (((BodyParameter) param).getSchema()
															.getReference() != null) {
														apiResource.getRequestFields()
																.addAll(responseMap.get(((BodyParameter) param)
																		.getSchema().getReference().substring(
																				((BodyParameter) param).getSchema()
																						.getReference().lastIndexOf("/")
																						+ 1)));
													}
												}
											}
										}
									}

								}
							}
							return apiResource;
						}
					}).collect(Collectors.toList());

			for (ApiResource apiResource : apisList) {
				SolrDocumentDto flatSolrDocumentDto = solrDocumentDto.clone();
				ApiResource flatApiResource = apiResource.clone();
				String id = flatSolrDocumentDto.getSwaggerDocId() + APIEndpoints.SEPERATOR
						+ flatApiResource.getResourceUrl() + APIEndpoints.SEPERATOR + flatApiResource.getMethodName();
				flatSolrDocumentDto.setUniqueId(id);
				if (apiResource.getResponseDef() != null) {
					for (String responseDef : apiResource.getResponseDef()) {
						flatSolrDocumentDto.setResponseFields(responseMap.get(responseDef));
					}
				}
				flatSolrDocumentDto.setApiResource(flatApiResource);
				flatSolrDocumentDto.setRequestFields(apiResource.getRequestFields());
				flatSolrDocumentDto.setHeaderFields(apiResource.getHeaderFields());
				flatSolrDocumentDto.setQueryFields(apiResource.getQueryFields());
				flatSolrDocumentDto.setPathFields(apiResource.getPathFields());
				flatSolrDocumentDto.getAllFields().addAll(apiResource.getRequestFields());
				flatSolrDocumentDto.getAllFields().addAll(apiResource.getHeaderFields());
				flatSolrDocumentDto.getAllFields().addAll(apiResource.getQueryFields());
				flatSolrDocumentDto.getAllFields().addAll(apiResource.getPathFields());
				solrDocumentDtos.add(flatSolrDocumentDto);

			}
		}
		logger.info("Transformed Solr Document = {} ", new Gson().toJson(solrDocumentDtos));
		return solrDocumentDtos;
	}

	public Map<String, Set<String>> responseMap(Swagger swagger) {
		Map<String, Set<String>> responseMap = new HashMap<>();
		for (Entry<String, Model> responseDef : swagger.getDefinitions().entrySet()) {
			if (responseMap.get(responseDef.getKey()) == null) {
				responseMap.put(responseDef.getKey(), new HashSet<String>());
			}
			responseMap.get(responseDef.getKey()).addAll(responseDef.getValue().getProperties().keySet());
			if (responseDef.getValue().getProperties().size() > 0) {
				for (Entry<String, Property> entry : responseDef.getValue().getProperties().entrySet()) {
					responseMap.get(responseDef.getKey()).add(entry.getKey());
					if (entry.getValue() instanceof RefProperty) {
						responseMap.get(responseDef.getKey()).addAll(responseMap.get(((RefProperty) entry.getValue())
								.get$ref().substring(((RefProperty) entry.getValue()).get$ref().lastIndexOf("/") + 1)));
					}
				}
			}
		}
		return responseMap;
	}

	@Override
	public Boolean indexDocuments(List<SolrDocumentDto> dtos) {
		logger.info("Indexing started...");
		if (dtos != null) {
			for (SolrDocumentDto dto : dtos) {
				try {
					SolrInputDocument document = new SolrInputDocument();
					document.setField("id", dto.getUniqueId());
					document.setField("swaggerdocId", dto.getSwaggerDocId().toLowerCase());
					if (dto.getTitle() != null)
						document.setField("title", dto.getTitle().toLowerCase());
					if (dto.getDescription() != null)
						document.setField("description", dto.getDescription().toLowerCase());
					if (dto.getBaseUrl() != null)
						document.setField("baseUrl", dto.getBaseUrl().toLowerCase());
					if (dto.getHost() != null)
						document.setField("host", dto.getHost().toLowerCase());
					if (dto.getResponseFields() != null) {
						document.setField("responseFields", dto.getResponseFields().stream().map(s -> s.toLowerCase())
								.collect(Collectors.toList()));
					}
					if (dto.getFileName() != null)
						document.setField("fileName", dto.getFileName().toLowerCase());

					if (dto.getApiResource() != null)
						document.setField("apiResource.resourceUrl", dto.getApiResource().getResourceUrl());
					if (dto.getApiResource().getMethodName() != null)
						document.setField("apiResource.methodName", dto.getApiResource().getMethodName().toLowerCase());
					if (dto.getResponseFields() != null) {
						document.setField("apiResource.methodName_tags", dto.getApiResource().getTags().stream()
								.map(s -> s.toLowerCase()).collect(Collectors.toList()));
					}
					if (dto.getApiResource().getSummary() != null)
						document.setField("apiResource.summary", dto.getApiResource().getSummary().toLowerCase());
					if (dto.getApiResource().getOperationId() != null)
						document.setField("apiResource.operationId",
								dto.getApiResource().getOperationId().toLowerCase());
					if (dto.getApiResource().getDescription() != null)
						document.setField("apiResource.description",
								dto.getApiResource().getDescription().toLowerCase());

					if (dto.getRequestFields() != null) {
						document.setField("requestFields", dto.getResponseFields().stream().map(s -> s.toLowerCase())
								.collect(Collectors.toList()));
					}
					if (dto.getHeaderFields() != null) {
						document.setField("headerFields",
								dto.getHeaderFields().stream().map(s -> s.toLowerCase()).collect(Collectors.toList()));
					}
					if (dto.getQueryFields() != null) {
						document.setField("queryFields",
								dto.getQueryFields().stream().map(s -> s.toLowerCase()).collect(Collectors.toList()));
					}
					if (dto.getPathFields() != null) {
						document.setField("pathFields",
								dto.getPathFields().stream().map(s -> s.toLowerCase()).collect(Collectors.toList()));
					}
					if (dto.getPathFields() != null) {
						document.setField("allFields",
								dto.getAllFields().stream().map(s -> s.toLowerCase()).collect(Collectors.toList()));
					}
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
