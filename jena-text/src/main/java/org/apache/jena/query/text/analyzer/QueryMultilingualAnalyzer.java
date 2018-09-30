/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.query.text.analyzer ;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.lucene.analysis.Analyzer ;
import org.apache.lucene.analysis.DelegatingAnalyzerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Lucene Analyzer implementation that delegates to a language-specific
 * Analyzer based on a field name suffix: e.g. field="label_en" will use
 * an EnglishAnalyzer.
 */

public class QueryMultilingualAnalyzer extends DelegatingAnalyzerWrapper {
    private static Logger log = LoggerFactory.getLogger(QueryMultilingualAnalyzer.class);
    private Analyzer defaultAnalyzer;
    private String langTag;

    public QueryMultilingualAnalyzer(Analyzer defaultAnalyzer) {
        super(PER_FIELD_REUSE_STRATEGY);
        this.defaultAnalyzer = defaultAnalyzer;
        this.langTag = null;
    }

    public QueryMultilingualAnalyzer(Analyzer defaultAnalyzer, String tag) {
        super(PER_FIELD_REUSE_STRATEGY);
        this.defaultAnalyzer = defaultAnalyzer;
        this.langTag = tag;
    }

    @Override
    /**
     * The analyzer corresponding to the langTag supplied at instantiation
     * is used to retrieve the analyzer to use regardless of the tag on the
     * fieldName. If no langTag is supplied then the tag on fieldName is
     * used to retrieve the analyzer as with the MultilingualAnalyzer
     * 
     * @param fieldName
     * @return the analyzer to use in the search
     */
    protected Analyzer getWrappedAnalyzer(String fieldName) {
        int idx = fieldName.lastIndexOf("_");
        if (idx == -1) { // not language-specific, e.g. "label"
            return defaultAnalyzer;
        }
        String lang = ObjectUtils.defaultIfNull(langTag, fieldName.substring(idx+1));
        Analyzer analyzer = Util.getLocalizedAnalyzer(lang);
        analyzer = ObjectUtils.defaultIfNull(analyzer, defaultAnalyzer);
        log.trace("getWrappedAnalyzer langTag: {}, fieldName: {}, analyzer: {}", langTag, fieldName, analyzer);
        return analyzer;
    }

    @Override
    public String toString() {
        return "QueryMultilingualAnalyzer(default=" + defaultAnalyzer + ")";
    }
}
