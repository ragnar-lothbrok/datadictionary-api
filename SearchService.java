package com.edureka.solr.searching;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.SpellingParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SearchService {

	/**
	 * This will return only title and id fields
	 *
	 * @throws IOException
	 * @throws SolrServerException
	 */

	public static void getSearchByText(String fieldName, String searchKey) throws SolrServerException, IOException {
		HttpSolrClient solr = SolrServer.getSolrClient();
		SolrQuery query = new SolrQuery();
		query.set("q", fieldName + ":" + searchKey);
		query.setRows(Integer.MAX_VALUE);
		QueryResponse response = solr.query(query);
		SolrDocumentList docList = response.getResults();
		Integer count = 0;
		List<String> movieTitles = new ArrayList<String>();
		for (SolrDocument doc : docList) {
			movieTitles.add(doc.get("title").toString());
			count++;
		}
		System.out.println("Records returned =  " + count);
		System.out.println("movieTitles returned =  " + movieTitles);
		System.out.println("===============================================");
	}

	public static void getSearchByText(String queryText) throws SolrServerException, IOException {
		HttpSolrClient solr = SolrServer.getSolrClient();
		SolrQuery query = new SolrQuery();
		query.set("q", queryText);
		QueryResponse response = solr.query(query);
		SolrDocumentList docList = response.getResults();
		Integer count = 0;
		List<String> movieTitles = new ArrayList<String>();
		for (SolrDocument doc : docList) {
			movieTitles.add(doc.get("title").toString() + "," + " run time = " + doc.get("runTime").toString());
			count++;
		}
		System.out.println("Records returned =  " + count);
		System.out.println("movieTitles returned =  " + movieTitles);
		System.out.println("===============================================");
	}

	public static void getSearchByTextAndSort(String fieldName, String searchKey, String sortFieldName)
			throws SolrServerException, IOException {
		HttpSolrClient solr = SolrServer.getSolrClient();
		SolrQuery query = new SolrQuery();
		query.set("q", fieldName + ":" + searchKey);
		query.addSort(sortFieldName, ORDER.asc);
		QueryResponse response = solr.query(query);
		SolrDocumentList docList = response.getResults();
		Integer count = 0;
		List<String> movieTitles = new ArrayList<String>();
		for (SolrDocument doc : docList) {
			movieTitles.add(doc.get("title").toString() + " - " + doc.get(sortFieldName).toString());
			count++;
		}
		System.out.println("Records returned =  " + count);
		System.out.println("movieTitles returned =  " + movieTitles);
		System.out.println("===============================================");
	}

	public static void getFacetCountsForField(String fieldName) throws SolrServerException, IOException {
		HttpSolrClient solr = SolrServer.getSolrClient();
		SolrQuery query = new SolrQuery();
		query.set("q", "*:*");
		query.setParam(FacetParams.FACET, true);
		query.setParam(FacetParams.FACET_FIELD, fieldName);
		QueryResponse response = solr.query(query);
		FacetField facetField = response.getFacetField(fieldName);
		if (facetField != null) {
			List<FacetField.Count> values = facetField.getValues();
			for (FacetField.Count facetCount : values) {
				String name = facetCount.getName();
				long count = facetCount.getCount();
				System.out.println("name: " + name + " docs count: " + count);
			}
		}
	}

	public static void getFacetQueryOutput(String fieldName) throws SolrServerException, IOException {
		HttpSolrClient solr = SolrServer.getSolrClient();
		SolrQuery query = new SolrQuery();
		query.set("q", "*:*");
		query.setParam(FacetParams.FACET, true);
		query.add(FacetParams.FACET_QUERY, fieldName + ":[* TO 1000]");
		query.add(FacetParams.FACET_QUERY, fieldName + ":[1001 TO 3000]");
		query.add(FacetParams.FACET_QUERY, fieldName + ":[3001 TO 5000]");
		query.add(FacetParams.FACET_QUERY, fieldName + ":[5001 TO *]");
		QueryResponse response = solr.query(query);
		Map<String, Integer> facetQuery = response.getFacetQuery();
		if (facetQuery != null) {
			for (Map.Entry<String, Integer> facetQueryCount : facetQuery.entrySet()) {
				System.out.println(
						"Facet query: " + facetQueryCount.getKey() + " docs count: " + facetQueryCount.getValue());
			}
		}
	}

	public static void getSpellCheckCorrections(String query) throws SolrServerException, IOException {
		HttpSolrClient solr = SolrServer.getSolrClient();
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setParam(CommonParams.QT, "/spell");
		solrQuery.setParam(CommonParams.Q, query);
		solrQuery.set(SpellingParams.SPELLCHECK_BUILD, true);
		solrQuery.set(SpellingParams.SPELLCHECK_COLLATE, false);
		solrQuery.set("spellcheck", true);
		QueryResponse response = solr.query(solrQuery);
		SpellCheckResponse spellCheckResponse = response.getSpellCheckResponse();
		if (spellCheckResponse != null) {
			List<SpellCheckResponse.Suggestion> suggestions = spellCheckResponse.getSuggestions();
			if (suggestions != null && suggestions.size() > 0) {
				SpellCheckResponse.Suggestion suggestion = suggestions.get(0);
				List<String> alternatives = suggestion.getAlternatives();
				if (alternatives != null && alternatives.size() > 0) {
					String s = alternatives.get(0);
					System.out.println("Spell corrected to : " + s);
				}
			}
		}
	}

	public static void getPaginatedResults(String fieldName, String searchQuery, int start, int rows)
			throws SolrServerException, IOException {
		HttpSolrClient solr = SolrServer.getSolrClient();
		SolrQuery query = new SolrQuery();
		query.set(CommonParams.Q, fieldName + ":" + searchQuery);
		query.set(CommonParams.START, start);
		query.set(CommonParams.ROWS, rows);
		QueryResponse response = solr.query(query);
		SolrDocumentList results = response.getResults();
		if (results != null) {
			int size = results.size();
			System.out.println("Docs returned: " + size + " num found: " + results.getNumFound() + " start: "
					+ results.getStart());
			Iterator<SolrDocument> iterator = results.iterator();
			while (iterator.hasNext()) {
				System.out.println("Doc ID: " + iterator.next().get("id"));
			}
		}
	}

	public static void main(String[] args) {
		try {
			getPaginatedResults("title", "space", 0, 5);
			getPaginatedResults("title", "space", 5, 10);
			getSpellCheckCorrections("ocaen");
			getSpellCheckCorrections("oceasn");
			getFacetCountsForField("originalLanguage");
			getFacetQueryOutput("voteCount");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
