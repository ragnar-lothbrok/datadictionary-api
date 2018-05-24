package com.opens.datadictionary.Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opens.datadictionary.service.SearchService;
import com.opens.datadictionary.solr.dtos.SearchRequest;

@Service
public class SolrSearchServiceImpl implements SearchService {

	private static final Logger logger = LoggerFactory.getLogger(SolrSearchServiceImpl.class);

	@Autowired
	private HttpSolrClient httpSolrClient;

	@Override
	public Map<String,List<String>> search(SearchRequest searchRequest) {
		logger.info("Search request is received in service = {} ", searchRequest);
		Map<String,List<String>> filenameIdMap = new HashMap<String,List<String>>();
		try {
			SolrQuery query = new SolrQuery();
			query.set(CommonParams.ROWS, Integer.MAX_VALUE);
			String queryText = "*";
			if (searchRequest != null) {
				if (!StringUtils.isEmpty(searchRequest.getSearchText())) {
					searchRequest.setSearchText(searchRequest.getSearchText().toLowerCase());
					queryText = "title:" + searchRequest.getSearchText();
					queryText += " OR description" + searchRequest.getSearchText();
					queryText += " OR apiResource.methodName_tags" + searchRequest.getSearchText();
					queryText += " OR apiResource.summary" + searchRequest.getSearchText();
					queryText += " OR apiResource.description" + searchRequest.getSearchText();
				}
			}
			if (searchRequest.getFilters() != null) {
				for (int i = 0; i < searchRequest.getFilters().size(); i++) {
					if (i == 0) {
						if (queryText.equals("*")) {
							queryText = searchRequest.getFilters().get(i).getFilter() + ":"
									+ searchRequest.getFilters().get(i).getFilterValue().toLowerCase();
						} else {
							queryText += "OR " + searchRequest.getFilters().get(i).getFilter() + ":"
									+ searchRequest.getFilters().get(i).getFilterValue().toLowerCase();
						}
					} else {
						queryText += " OR " + searchRequest.getFilters().get(i).getFilter() + ":"
								+ searchRequest.getFilters().get(i).getFilterValue().toLowerCase();
					}
				}
			}
			query.set("q", queryText);
			QueryResponse response = httpSolrClient.query(query);
			SolrDocumentList docList = response.getResults();
			Integer count = 0;

			for (SolrDocument doc : docList) {
				String fileName = doc.get("fileName").toString();
				if(fileName != null) {
					if(filenameIdMap.containsKey(fileName)) {
						filenameIdMap.get(fileName).add(doc.get("id").toString());
					}else {
						filenameIdMap.put(fileName,new ArrayList<>());
						filenameIdMap.get(fileName).add(doc.get("id").toString());
					}
				}
				count++;
			}
			logger.info("Total Records returned =  {} Records filenameIdMap = {}  ", count, filenameIdMap);
		} catch (Exception e) {
			logger.error("Exception occured while searching in Solr = {} ", e);
		}
		return filenameIdMap;
	}

}
