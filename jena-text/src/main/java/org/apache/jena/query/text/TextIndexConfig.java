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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;

public class TextIndexConfig {

    EntityDefinition entDef;
    Analyzer analyzer;
    Analyzer queryAnalyzer;
    String queryParser;
    boolean multilingualSupport;
    int maxBasicQueries = 1024;
    boolean valueStored;
    boolean ignoreIndexErrors;
    List<String> facetFields = new ArrayList<>();
    int maxFacetHits = 0; // 0 = unlimited
    ShaclIndexMapping shaclMapping = null;

    public TextIndexConfig(EntityDefinition entDef) {
        this.entDef = entDef;
    }

    public EntityDefinition getEntDef() {
        return entDef;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public Analyzer getQueryAnalyzer() {
        return queryAnalyzer;
    }

    public void setQueryAnalyzer(Analyzer queryAnalyzer) {
        this.queryAnalyzer = queryAnalyzer;
    }

    public String getQueryParser() {
        return ((queryParser != null) ? queryParser : "QueryParser");
    }

    public void setQueryParser(String queryParser) {
        this.queryParser = queryParser;
    }

    public boolean isMultilingualSupport() {
        return multilingualSupport;
    }

    public void setMultilingualSupport(boolean multilingualSupport) {
        this.multilingualSupport = multilingualSupport;
    }

    public int getMaxBasicQueries() {
        return maxBasicQueries;
    }

    public void setMaxBasicQueries(int maxBasicQueries) {
        this.maxBasicQueries = maxBasicQueries;
    }

    public boolean isValueStored() {
        return valueStored;
    }

    public void setValueStored(boolean valueStored) {
        this.valueStored = valueStored;
    }

    public boolean isIgnoreIndexErrors() {
        return ignoreIndexErrors;
    }

    public void setIgnoreIndexErrors(boolean ignore) {
        this.ignoreIndexErrors = ignore;
    }

    /**
     * Get the list of fields to enable native Lucene faceting on.
     * These fields will have SortedSetDocValues added during indexing.
     */
    public List<String> getFacetFields() {
        return Collections.unmodifiableList(facetFields);
    }

    /**
     * Set the list of fields to enable native Lucene faceting on.
     * @param facetFields list of field names that should support faceting
     */
    public void setFacetFields(List<String> facetFields) {
        this.facetFields = new ArrayList<>(facetFields);
    }

    /**
     * Add a field to the list of facetable fields.
     * @param fieldName the field name to enable faceting on
     */
    public void addFacetField(String fieldName) {
        this.facetFields.add(fieldName);
    }

    /**
     * Check if a field is configured for faceting.
     * @param fieldName the field name to check
     * @return true if the field is configured for faceting
     */
    public boolean isFacetField(String fieldName) {
        return facetFields.contains(fieldName);
    }

    public int getMaxFacetHits() {
        return maxFacetHits;
    }

    public void setMaxFacetHits(int maxFacetHits) {
        this.maxFacetHits = maxFacetHits;
    }

    public ShaclIndexMapping getShaclMapping() {
        return shaclMapping;
    }

    public void setShaclMapping(ShaclIndexMapping m) {
        this.shaclMapping = m;
    }

    public boolean isShaclMode() {
        return shaclMapping != null;
    }
}
