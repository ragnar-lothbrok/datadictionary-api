package com.opens.datadictionary.Impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opens.datadictionary.constants.APIEndpoints;
import com.opens.datadictionary.exceptions.GenericException;
import com.opens.datadictionary.exceptions.InvalidSwaggerFileException;
import com.opens.datadictionary.mongo.models.SwaggerDetail;
import com.opens.datadictionary.mongo.repository.CommonService;
import com.opens.datadictionary.service.IndexService;
import com.opens.datadictionary.service.SearchService;
import com.opens.datadictionary.service.SwaggerFileService;
import com.opens.datadictionary.solr.dtos.SearchRequest;
import com.opens.datadictionary.solr.models.SolrDocumentDto;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

@Service
public class SwaggerFileServiceImpl implements SwaggerFileService {

	private static final Logger logger = LoggerFactory.getLogger(SwaggerFileServiceImpl.class);

	@SuppressWarnings("unused")
	@Autowired
	private CommonService commonService;

	private final Path rootLocation = Paths.get("files");

	@Autowired
	private SwaggerParser swaggerParser;

	@Autowired
	private IndexService solrIndexService;

	@Autowired
	private SearchService solrSearchService;

	@Override
	public void store(MultipartFile file) {
		try {
			if (file != null) {
				String uuid = UUID.randomUUID().toString();
				String fileName = uuid + "-" + file.getOriginalFilename();
				Files.copy(file.getInputStream(), this.rootLocation.resolve(fileName));

				String swaggerContent = getDataFromFile(file.getInputStream());

				Swagger swagger = validateValidSwaggerFile(swaggerContent);

				// Saving file to mongo database
				SwaggerDetail swaggerDetail = new SwaggerDetail(UUID.randomUUID().toString(), fileName, swaggerContent);
				// commonService.save(swaggerDetail);

				List<SolrDocumentDto> solrDocumentDtos = solrIndexService.transform(swagger, swaggerDetail.getId(),
						fileName);

				solrIndexService.indexDocuments(solrDocumentDtos);

				logger.info("Content saved for file = {} ", fileName);
			}
		} catch (Exception e) {
			throw new GenericException("FAILED TO LOAD swagger file.");
		}

	}

	public Swagger validateValidSwaggerFile(String content) {
		Swagger swagger = null;
		try {
			swagger = swaggerParser.parse(content);
			if (swagger == null) {
				throw new InvalidSwaggerFileException("Invalid swagger file");
			}
		} catch (Exception e) {
			logger.error("Exception occured while parsing swagger file content = {} ", e);
			throw new InvalidSwaggerFileException("Invalid swagger file");
		}
		return swagger;
	}

	/**
	 * This method will read swagger file content and print it in logs
	 * 
	 * @param input
	 * @return
	 */
	public String getDataFromFile(InputStream input) {
		StringBuilder fileContent = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(input));
			String line;
			while ((line = reader.readLine()) != null) {
				fileContent.append(line);
			}
			logger.info("Swagger file content = {} ", fileContent.toString());
		} catch (Exception e) {
			logger.error("Exception occured while reading file = {} ", e);
			throw new InvalidSwaggerFileException("Invalid swagger file");
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("Exception occured while closing file = {} ", e);
				}
			}
		}
		return fileContent.toString();
	}

	@Override
	public List<String> search(SearchRequest searchRequest) {
		Map<String, List<String>> filenameIdMap = solrSearchService.search(searchRequest);
		return contructSwagger(filenameIdMap);
	}

	public Swagger getSearchedSwagger(Entry<String, List<String>> entry) {
		try {
			Resource resource = loadFile(entry.getKey());
			if (resource != null) {
				String content = getDataFromFile(resource.getInputStream());
				Swagger swagger = swaggerParser.parse(content);
				List<String> retainEndPoints = new ArrayList<>();
				for (String endPoints : entry.getValue()) {
					String splits[] = endPoints.split(APIEndpoints.SEPERATOR);
					if (swagger.getPaths() != null && swagger.getPaths().size() > 0) {
						if (swagger.getPaths().keySet().contains(splits[1])) {
							retainEndPoints.add(splits[1]);
						}
					}
				}
				swagger.getPaths().keySet().retainAll(retainEndPoints);
				return swagger;
			}
		} catch (Exception e) {
			logger.error("Exception occured while selecting search data form swagger = {}", e);
		}
		return null;
	}

	public List<String> contructSwagger(Map<String, List<String>> filenameIdMap) {
		List<String> swaggers = new ArrayList<String>();
		try {
			if (filenameIdMap != null) {
				for (Entry<String, List<String>> entry : filenameIdMap.entrySet()) {
					Swagger swagger = getSearchedSwagger(entry);
					if (swagger != null) {
						ObjectMapper mapper = new ObjectMapper();
						mapper.setSerializationInclusion(Include.NON_NULL);
						String parsedSwagger = mapper.writeValueAsString(swagger);
						logger.info("Swagger parsed = {}", parsedSwagger);
						swaggers.add(parsedSwagger);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception occured while creating swagger from file = {}", e);
		}
		return swaggers;
	}

	public Resource loadFile(String filename) {
		try {
			Path file = rootLocation.resolve(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new GenericException("FAILED TO LOAD SWAGGER FILE.");
			}
		} catch (MalformedURLException e) {
			throw new GenericException("FAILED TO LOAD SWAGGER FILE.");
		}
	}

	@PostConstruct
	public void init() {
		try {
			if (!Files.exists(rootLocation)) {
				Files.createDirectory(rootLocation);
			}
		} catch (IOException e) {
			logger.error("Directory already exists = {} ", e);
		}
	}

	@Override
	public String searchOneDoc(SearchRequest searchRequest) {
		try {
			Map<String, List<String>> filenameIdMap = solrSearchService.search(searchRequest);
			if (filenameIdMap != null) {
				for (Entry<String, List<String>> entry : filenameIdMap.entrySet()) {
					Swagger swagger = getSearchedSwagger(entry);
					if (swagger != null) {
						ObjectMapper mapper = new ObjectMapper();
						mapper.setSerializationInclusion(Include.NON_NULL);
						String parsedSwagger = mapper.writeValueAsString(swagger);
						logger.info("Swagger parsed = {}", parsedSwagger);
						return parsedSwagger;
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception occured while creating swagger from file = {}", e);
		}
		return null;
	}

	@Override
	public List<Swagger> searchSwaggers(SearchRequest searchRequest) {
		Map<String, List<String>> filenameIdMap = solrSearchService.search(searchRequest);
		List<Swagger> swaggers = new ArrayList<>();
		try {
			if (filenameIdMap != null) {
				for (Entry<String, List<String>> entry : filenameIdMap.entrySet()) {
					Swagger swagger = getSearchedSwagger(entry);
					if (swagger != null) {
						swaggers.add(swagger);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception occured while selecting search data form swagger = {}", e);
		}
		return swaggers;
	}

}
