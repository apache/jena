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

import java.io.Reader ;
import java.util.List ;

import org.apache.jena.query.text.TextIndexException;
import org.apache.lucene.analysis.Analyzer ;
import org.apache.lucene.analysis.TokenFilter ;
import org.apache.lucene.analysis.Tokenizer ;
import org.apache.lucene.analysis.TokenStream ;
import org.apache.lucene.analysis.core.KeywordTokenizer ;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter ;
import org.apache.lucene.analysis.core.WhitespaceTokenizer ;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version ;


/** 
 * Lucene Analyzer implementation that can be configured with different
 * Tokenizer and (optionally) TokenFilter implementations.
 */

public class ConfigurableAnalyzer extends Analyzer {
        private final Version version;
        private final String tokenizer;
        private final List<String> filters;
        
        private Tokenizer getTokenizer(String tokenizerName, Reader reader) {
                switch(tokenizerName) {
                        case "KeywordTokenizer":
                                return new KeywordTokenizer(reader);
                        case "LetterTokenizer":
                                return new LetterTokenizer(version, reader);
                        case "StandardTokenizer":
                                return new StandardTokenizer(version, reader);
                        case "WhitespaceTokenizer":
                                return new WhitespaceTokenizer(version, reader);
                        default:
                                throw new TextIndexException("Unknown tokenizer : " + tokenizerName);
                }
        }
        
        private TokenFilter getTokenFilter(String filterName, TokenStream source) {
                switch(filterName) {
                        case "ASCIIFoldingFilter":
                                return new ASCIIFoldingFilter(source);
                        case "LowerCaseFilter":
                                return new LowerCaseFilter(version, source);
                        case "StandardFilter":
                                return new StandardFilter(version, source);
                        default:
                                throw new TextIndexException("Unknown filter : " + filterName);
                }
        }
        
        public ConfigurableAnalyzer(Version ver, String tokenizer, List<String> filters) {
                this.version = ver;
                this.tokenizer = tokenizer;
                this.filters = filters;
        }

        @Override
        protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
                Tokenizer source = getTokenizer(this.tokenizer, reader);
                TokenStream stream = source;
                for (String filter : this.filters) {
                        stream = getTokenFilter(filter, stream);
                }
                return new TokenStreamComponents(source, stream);
        }

}
