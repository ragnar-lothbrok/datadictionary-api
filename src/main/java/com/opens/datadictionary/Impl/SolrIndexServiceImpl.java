package com.opens.datadictionary.Impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
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
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

@Service
public class SolrIndexServiceImpl implements IndexService {

	private static final Logger logger = LoggerFactory.getLogger(SolrIndexServiceImpl.class);

	@Autowired
	private HttpSolrClient httpSolrClient;

	private final ObjectMapper mapper = new ObjectMapper();

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

		final Map<String, Map<String, String>> responseMap = responseMap(swagger);

		if (swagger.getPaths() != null && swagger.getPaths().size() > 0) {
			List<ApiResource> apisList = swagger.getPaths().entrySet().stream()
					.map(new Function<Entry<String, Path>, ApiResource>() {
						List<String> fieldsMetaData = new ArrayList<>();

						@Override
						public ApiResource apply(Entry<String, Path> t) {
							ApiResource apiResource = new ApiResource();
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
												apiResource.getPathFields().add(param.getName());
											}
											if (param instanceof QueryParameter) {
												apiResource.getQueryFields().add(param.getName());
											}
											if (param instanceof BodyParameter) {
												apiResource.getRequestFields().add(param.getName());
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
														apiResource.getRequestFields().addAll(responseMap
																.get(((BodyParameter) param).getSchema().getReference()
																		.substring(((BodyParameter) param).getSchema()
																				.getReference().lastIndexOf("/") + 1))
																.keySet());
														Collection<String> decriptions = responseMap
																.get(((BodyParameter) param).getSchema().getReference()
																		.substring(((BodyParameter) param).getSchema()
																				.getReference().lastIndexOf("/") + 1))
																.values();
														decriptions.removeAll(Collections.singleton(null));
														apiResource.getMeataData()
																.append(Joiner.on(" ").join(decriptions));
													}
												}
											}
										}
									}

								}
							}
							apiResource.getMeataData().append(Joiner.on(" ").join(fieldsMetaData));
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
						if (responseMap.get(responseDef) != null) {
							flatSolrDocumentDto.getResponseFields().addAll(responseMap.get(responseDef).keySet());
							Collection<String> descriptions = responseMap.get(responseDef).values();
							descriptions.removeAll(Collections.singleton(null));
							apiResource.getMeataData().append(Joiner.on(" ").join(descriptions));
						}
					}
				}
				flatSolrDocumentDto.setApiResource(flatApiResource);
				if (apiResource.getRequestFields() != null) {
					for (String requestF : apiResource.getRequestFields()) {
						if (responseMap.get(requestF) != null)
							flatSolrDocumentDto.getRequestFields().addAll(responseMap.get(requestF).keySet());
					}
				}
				flatSolrDocumentDto.setHeaderFields(apiResource.getHeaderFields());
				flatSolrDocumentDto.setQueryFields(apiResource.getQueryFields());
				flatSolrDocumentDto.setPathFields(apiResource.getPathFields());
				flatSolrDocumentDto.getAllFields().addAll(apiResource.getRequestFields());
				flatSolrDocumentDto.getAllFields().addAll(apiResource.getHeaderFields());
				flatSolrDocumentDto.getAllFields().addAll(apiResource.getQueryFields());
				flatSolrDocumentDto.getAllFields().addAll(apiResource.getPathFields());
				flatSolrDocumentDto.getAllFields().addAll(flatSolrDocumentDto.getResponseFields());
				solrDocumentDtos.add(flatSolrDocumentDto);

			}
		}
		logger.info("Transformed Solr Document = {} ", new Gson().toJson(solrDocumentDtos));
		return solrDocumentDtos;
	}

	private void getFields(Property property, Map<String, String> fields) {
		if (property instanceof ArrayProperty) {
			getFields(((ArrayProperty) property).getItems(), fields);
		} else if (property instanceof RefProperty) {
			fields.put(
					((RefProperty) property).get$ref()
							.substring(((RefProperty) property).get$ref().lastIndexOf("/") + 1),
					((RefProperty) property).getDescription());
		}
	}

	public Map<String, Map<String, String>> responseMap(Swagger swagger) {
		Map<String, Map<String, String>> responseMap = new HashMap<>();
		try {
			for (Entry<String, Model> responseDef : swagger.getDefinitions().entrySet()) {
				if (responseMap.get(responseDef.getKey()) == null) {
					responseMap.put(responseDef.getKey(), new HashMap<String, String>());
					responseMap.get(responseDef.getKey()).put(responseDef.getKey(), "");
				}
				if (responseDef.getValue() != null && responseDef.getValue().getProperties() != null) {
					for (Entry<String, Property> entry : responseDef.getValue().getProperties().entrySet()) {
						responseMap.get(responseDef.getKey()).put(entry.getKey(), entry.getValue().getDescription());
					}
					for (Entry<String, Property> entry : responseDef.getValue().getProperties().entrySet()) {
						getFields(entry.getValue(), responseMap.get(responseDef.getKey()));
					}
				}
			}
			logger.info("Fields Name description map = {} ", mapper.writeValueAsString(responseMap));
			DFS(responseMap);
			logger.info("Fields Name description map = {} ", mapper.writeValueAsString(responseMap));
		} catch (Exception e) {
			logger.error("Exception occured while creating response field map = {} ", e);
		}
		return responseMap;
	}

	// Adding all nested fields in a flat set
	private void DFS(Map<String, Map<String, String>> responseMap) throws JsonProcessingException {
		Set<String> visitedNodes = new HashSet<>();
		Map<String, String> newFields = new HashMap<>();
		for (Entry<String, Map<String, String>> entry : responseMap.entrySet()) {
			visitedNodes.clear();
			newFields.clear();
			Stack<Entry<String, String>> stack = new Stack<>();
			stack.addAll(entry.getValue().entrySet());
			while (!stack.isEmpty()) {
				Entry<String, String> fieldName = stack.pop();
				visitedNodes.add(fieldName.getKey());
				newFields.put(fieldName.getKey(), fieldName.getValue());
				if (responseMap.get(fieldName.getKey()) != null) {
					Set<Entry<String, String>> filteredData = responseMap.get(fieldName.getKey()).entrySet().stream()
							.filter(e -> !visitedNodes.contains(e.getKey())).collect(Collectors.toSet());
					stack.addAll(filteredData);
				}
			}
			responseMap.get(entry.getKey()).putAll(newFields);
		}
	}

	@Override
	public Boolean indexDocuments(List<SolrDocumentDto> dtos) {
		logger.info("Indexing started...");
		if (dtos != null) {
			for (SolrDocumentDto dto : dtos) {
				try {
					SolrInputDocument document = new SolrInputDocument();
					document.setField("id", dto.getUniqueId());
					document.setField("swaggerdocId", dto.getSwaggerDocId());
					if (dto.getTitle() != null)
						document.setField("title", dto.getTitle());
					if (dto.getDescription() != null)
						document.setField("description", dto.getDescription());
					if (dto.getBaseUrl() != null)
						document.setField("baseUrl", dto.getBaseUrl());
					if (dto.getHost() != null)
						document.setField("host", dto.getHost());
					if (dto.getResponseFields() != null) {
						document.setField("responseFields", dto.getResponseFields());
					}
					if (dto.getFileName() != null)
						document.setField("fileName", dto.getFileName());

					if (dto.getApiResource() != null)
						document.setField("apiResource.resourceUrl", dto.getApiResource().getResourceUrl());
					if (dto.getApiResource().getMethodName() != null)
						document.setField("apiResource.methodName", dto.getApiResource().getMethodName());

					if (dto.getApiResource().getTags() != null) {
						document.setField("apiResource.methodName_tags", dto.getApiResource().getTags());
					}

					if (dto.getApiResource().getSummary() != null) {
						document.setField("apiResource.summary", dto.getApiResource().getSummary());
					}
					if (dto.getApiResource().getMeataData() != null)
						document.setField("apiResource.metadata", dto.getApiResource().getMeataData().toString());

					if (dto.getApiResource().getOperationId() != null)
						document.setField("apiResource.operationId", dto.getApiResource().getOperationId());
					if (dto.getApiResource().getDescription() != null)
						document.setField("apiResource.description", dto.getApiResource().getDescription());

					if (dto.getRequestFields() != null) {
						document.setField("requestFields", dto.getRequestFields());
					}
					if (dto.getHeaderFields() != null) {
						document.setField("headerFields", dto.getHeaderFields());
					}
					if (dto.getQueryFields() != null) {
						document.setField("queryFields", dto.getQueryFields());
					}
					if (dto.getPathFields() != null) {
						document.setField("pathFields", dto.getPathFields());
					}
					if (dto.getAllFields() != null) {
						document.setField("allFields", dto.getAllFields());
					}
					document.setField("active", dto.isActive());
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
