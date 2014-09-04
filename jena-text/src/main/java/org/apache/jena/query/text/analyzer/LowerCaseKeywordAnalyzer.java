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

import org.apache.lucene.analysis.Analyzer ;
import org.apache.lucene.analysis.core.KeywordTokenizer ;
import org.apache.lucene.analysis.core.LowerCaseFilter ;
import org.apache.lucene.util.Version ;


/** 
 * Lucene Analyzer implementation that works like KeywordAnalyzer (i.e.
 * doesn't tokenize the input, keeps it as a single token), but forces text
 * to lowercase and is thus case-insensitive.
 */

public class LowerCaseKeywordAnalyzer extends Analyzer {
        private Version version;
        
        public LowerCaseKeywordAnalyzer(Version ver) {
                this.version = ver;
        }

        @Override
        protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
                KeywordTokenizer source = new KeywordTokenizer(reader);
                LowerCaseFilter filter = new LowerCaseFilter(version, source);
                return new TokenStreamComponents(source, filter);
        }

}
