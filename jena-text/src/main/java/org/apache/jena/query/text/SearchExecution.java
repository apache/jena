/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.query.text;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.util.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared execution state between text:query and text:facet property functions.
 * <p>
 * When both PFs appear in the same query with matching parameters (same properties,
 * query string, and filters), they share a single SearchExecution instance stored
 * in the ExecutionContext. This avoids executing the Lucene query twice.
 * <p>
 * Hits and facet counts are computed lazily on first access.
 */
public class SearchExecution {
    private static final Logger log = LoggerFactory.getLogger(SearchExecution.class);

    private final List<Resource> props;
    private final String queryString;
    private final Map<String, List<String>> filters;
    private final TextIndexLucene textIndex;
    private final String graphURI;
    private final String lang;

    // Lazy results
    private List<TextHit> hits;
    private Map<String, List<FacetValue>> facetCounts;
    private boolean hitsComputed = false;
    private boolean facetCountsComputed = false;

    public SearchExecution(List<Resource> props, String queryString,
                           Map<String, List<String>> filters, TextIndexLucene textIndex,
                           String graphURI, String lang) {
        this.props = props != null ? new ArrayList<>(props) : new ArrayList<>();
        this.queryString = queryString;
        this.filters = filters != null ? new LinkedHashMap<>(filters) : Collections.emptyMap();
        this.textIndex = textIndex;
        this.graphURI = graphURI;
        this.lang = lang;
    }

    /**
     * Get or create a SearchExecution in the given ExecutionContext.
     * If one already exists with the same key, it is reused.
     */
    public static SearchExecution getOrCreate(ExecutionContext execCxt,
                                              List<Resource> props, String queryString,
                                              Map<String, List<String>> filters,
                                              TextIndexLucene textIndex,
                                              String graphURI, String lang) {
        String key = buildKey(props, queryString, filters);
        Symbol symbol = Symbol.create(TextQuery.NS + "searchExecution/" + key);

        Object existing = execCxt.getContext().get(symbol);
        if (existing instanceof SearchExecution) {
            log.trace("Reusing SearchExecution for key: {}", key);
            return (SearchExecution) existing;
        }

        SearchExecution se = new SearchExecution(props, queryString, filters, textIndex, graphURI, lang);
        execCxt.getContext().put(symbol, se);
        log.trace("Created new SearchExecution for key: {}", key);
        return se;
    }

    /**
     * Build a cache key from properties, query string, and filters.
     * Sorted to ensure consistent keys regardless of input order.
     */
    static String buildKey(List<Resource> props, String queryString,
                           Map<String, List<String>> filters) {
        StringBuilder sb = new StringBuilder();

        // Sorted property URIs
        if (props != null && !props.isEmpty()) {
            List<String> sortedProps = props.stream()
                .map(r -> r.getURI())
                .sorted()
                .collect(Collectors.toList());
            sb.append("props=").append(String.join(",", sortedProps));
        }

        sb.append("|qs=").append(queryString != null ? queryString : "");

        // Sorted filter map
        if (filters != null && !filters.isEmpty()) {
            sb.append("|filters=");
            List<String> sortedKeys = new ArrayList<>(filters.keySet());
            Collections.sort(sortedKeys);
            for (String key : sortedKeys) {
                List<String> values = new ArrayList<>(filters.get(key));
                Collections.sort(values);
                sb.append(key).append("=").append(String.join(",", values)).append(";");
            }
        }

        return sb.toString();
    }

    /**
     * Get search hits, computing them lazily on first access.
     */
    public synchronized List<TextHit> getHits(int limit, String highlight) {
        if (!hitsComputed) {
            try {
                if (filters.isEmpty()) {
                    hits = textIndex.query(props, queryString, graphURI, lang, limit, highlight);
                } else {
                    hits = textIndex.queryWithFilters(props, queryString, filters, graphURI, lang, limit, highlight);
                }
            } catch (Exception e) {
                log.error("Error computing hits: {}", e.getMessage());
                hits = Collections.emptyList();
            }
            hitsComputed = true;
        }
        return hits;
    }

    /**
     * Get facet counts, computing them lazily on first access.
     */
    public synchronized Map<String, List<FacetValue>> getFacetCounts(
            List<String> facetFields, int maxValues) {
        if (!facetCountsComputed) {
            try {
                if (filters.isEmpty()) {
                    facetCounts = textIndex.getFacetCounts(queryString, facetFields, maxValues);
                } else {
                    facetCounts = textIndex.getFacetCountsWithFilters(
                        queryString, facetFields, filters, maxValues);
                }
            } catch (Exception e) {
                log.error("Error computing facet counts: {}", e.getMessage());
                facetCounts = Collections.emptyMap();
            }
            facetCountsComputed = true;
        }
        return facetCounts;
    }

    public Map<String, List<String>> getFilters() {
        return filters;
    }

    public String getQueryString() {
        return queryString;
    }

    public List<Resource> getProps() {
        return props;
    }
}
