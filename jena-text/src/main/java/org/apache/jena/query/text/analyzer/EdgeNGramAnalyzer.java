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

package org.apache.jena.query.text.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;

/**
 * Analyzer for prefix/typeahead indexing: treats the entire field value as a
 * single token (keyword), lowercases it, then produces edge n-grams from 1 to
 * {@code maxGram} characters.
 * <p>
 * Pair this with {@link LowerCaseKeywordAnalyzer} as the query analyzer
 * (via {@code idx:queryAnalyzer}) so that a user's prefix input is matched
 * against the indexed n-grams without being n-grammed itself.
 */
public class EdgeNGramAnalyzer extends Analyzer {

    private final int minGram;
    private final int maxGram;

    public EdgeNGramAnalyzer() {
        this(1, 20);
    }

    public EdgeNGramAnalyzer(int minGram, int maxGram) {
        this.minGram = minGram;
        this.maxGram = maxGram;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        KeywordTokenizer source = new KeywordTokenizer();
        TokenStream stream = new LowerCaseFilter(source);
        stream = new EdgeNGramTokenFilter(stream, minGram, maxGram, false);
        return new TokenStreamComponents(source, stream);
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        return new LowerCaseFilter(in);
    }
}
