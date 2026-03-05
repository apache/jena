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

import org.apache.jena.query.text.cql.CqlExpression;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.util.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared execution state between luc:query and luc:facet property functions.
 * <p>
 * When both PFs appear in the same query with matching parameters (same index ID,
 * properties, query string, and CQL filter), they share a single SearchExecution
 * instance stored in the ExecutionContext. This avoids executing the Lucene query twice.
 * <p>
 * Hits and facet counts are computed lazily on first access.
 */
public class SearchExecution {
    private static final Logger log = LoggerFactory.getLogger(SearchExecution.class);

    private final String indexId;
    private final List<Resource> props;
    private final String queryString;
    private final CqlExpression filter;
    private final List<SortSpec> sortSpecs;
    private final TextIndexLucene textIndex;
    private final String graphURI;
    private final String lang;

    // Lazy results
    private List<TextHit> hits;
    private Map<String, List<FacetValue>> facetCounts;
    private long totalHits = -1;
    private boolean hitsComputed = false;
    private boolean facetCountsComputed = false;

    public SearchExecution(String indexId, List<Resource> props, String queryString,
                           CqlExpression filter, List<SortSpec> sortSpecs,
                           TextIndexLucene textIndex, String graphURI, String lang) {
        this.indexId = indexId != null ? indexId : TextIndexRegistry.DEFAULT_ID;
        this.props = props != null ? new ArrayList<>(props) : new ArrayList<>();
        this.queryString = queryString;
        this.filter = filter;
        this.sortSpecs = sortSpecs != null ? List.copyOf(sortSpecs) : List.of();
        this.textIndex = textIndex;
        this.graphURI = graphURI;
        this.lang = lang;
    }

    /**
     * Get or create a SearchExecution in the given ExecutionContext.
     * If one already exists with the same key, it is reused.
     */
    public static SearchExecution getOrCreate(ExecutionContext execCxt,
                                              String indexId, List<Resource> props,
                                              String queryString, CqlExpression filter,
                                              List<SortSpec> sortSpecs,
                                              TextIndexLucene textIndex,
                                              String graphURI, String lang) {
        String key = buildKey(indexId, props, queryString, filter, sortSpecs);
        Symbol symbol = Symbol.create(TextQuery.NS + "searchExecution/" + key);

        Object existing = execCxt.getContext().get(symbol);
        if (existing instanceof SearchExecution) {
            log.trace("Reusing SearchExecution for key: {}", key);
            return (SearchExecution) existing;
        }

        SearchExecution se = new SearchExecution(indexId, props, queryString, filter,
            sortSpecs, textIndex, graphURI, lang);
        execCxt.getContext().put(symbol, se);
        log.trace("Created new SearchExecution for key: {}", key);
        return se;
    }

    /**
     * Build a cache key from index ID, properties, query string, CQL filter, and sort specs.
     */
    static String buildKey(String indexId, List<Resource> props, String queryString,
                           CqlExpression filter, List<SortSpec> sortSpecs) {
        StringBuilder sb = new StringBuilder();

        sb.append("idx=").append(indexId != null ? indexId : "default");

        if (props != null && !props.isEmpty()) {
            List<String> sortedProps = props.stream()
                .map(r -> r.getURI())
                .sorted()
                .collect(Collectors.toList());
            sb.append("|props=").append(String.join(",", sortedProps));
        }

        sb.append("|qs=").append(queryString != null ? queryString : "");

        if (filter != null) {
            sb.append("|cql=").append(filter.toCanonical());
        }

        if (sortSpecs != null && !sortSpecs.isEmpty()) {
            sb.append("|sort=");
            sb.append(sortSpecs.stream().map(SortSpec::toCanonical).collect(Collectors.joining(",")));
        }

        return sb.toString();
    }

    /**
     * Get search hits, computing them lazily on first access.
     */
    public synchronized List<TextHit> getHits(int limit, String highlight) {
        if (!hitsComputed) {
            try {
                if (filter == null && (sortSpecs == null || sortSpecs.isEmpty())) {
                    hits = textIndex.query(props, queryString, graphURI, lang, limit, highlight);
                } else {
                    hits = textIndex.queryWithCql(props, queryString, filter, sortSpecs,
                        graphURI, lang, limit, highlight);
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
     * Get facet counts with minCount threshold, computing them lazily on first access.
     */
    public synchronized Map<String, List<FacetValue>> getFacetCounts(
            List<String> facetFields, int maxValues, int minCount) {
        if (!facetCountsComputed) {
            try {
                if (filter == null) {
                    facetCounts = textIndex.getFacetCounts(queryString, facetFields, maxValues, minCount);
                } else {
                    facetCounts = textIndex.getFacetCountsWithCql(
                        queryString, facetFields, filter, maxValues, minCount);
                }
            } catch (Exception e) {
                log.error("Error computing facet counts: {}", e.getMessage());
                facetCounts = Collections.emptyMap();
            }
            facetCountsComputed = true;
        }
        return facetCounts;
    }

    /**
     * Get total hit count, computing lazily on first access.
     */
    public synchronized long getTotalHits() {
        if (totalHits < 0) {
            try {
                totalHits = textIndex.countQueryWithCql(queryString, filter);
            } catch (Exception e) {
                log.error("Error computing total hits: {}", e.getMessage());
                totalHits = 0;
            }
        }
        return totalHits;
    }

    public CqlExpression getFilter() {
        return filter;
    }

    public String getQueryString() {
        return queryString;
    }

    public List<Resource> getProps() {
        return props;
    }

    public String getIndexId() {
        return indexId;
    }

    public List<SortSpec> getSortSpecs() {
        return sortSpecs;
    }
}
