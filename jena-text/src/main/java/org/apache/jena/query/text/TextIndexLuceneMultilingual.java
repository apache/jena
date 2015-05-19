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

package org.apache.jena.query.text;

import org.apache.jena.query.text.analyzer.Util;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;

import java.io.IOException;

public class TextIndexLuceneMultilingual extends TextIndexLucene {

    /**
     * Constructs a new TextIndexLuceneMultilingual.
     *
     * @param directory The Lucene Directory for the index
     * @param config The config definition for the index instantiation.
     */
    public TextIndexLuceneMultilingual(Directory directory, TextIndexConfig config) {
        super(directory, config) ;

        //multilingual index cannot work without lang field
        if (config.getEntDef().getLangField() == null)
            config.getEntDef().setLangField("lang");
    }

    @Override
    protected void updateDocument(Entity entity) throws IOException {
        Document doc = doc(entity);
        Term term = new Term(getDocDef().getEntityField(), entity.getId());
        Analyzer analyzer = Util.getLocalizedAnalyzer(entity.getLanguage());
        if (analyzer == null)
            analyzer = getAnalyzer();
        getIndexWriter().updateDocument(term, doc, analyzer) ;
    }

    @Override
    protected void addDocument(Entity entity) throws IOException {
        Document doc = doc(entity) ;
        Analyzer analyzer = Util.getLocalizedAnalyzer(entity.getLanguage());
        if (analyzer == null)
            analyzer = getAnalyzer();
        getIndexWriter().addDocument(doc, analyzer) ;
    }

    @Override
    protected Query preParseQuery(String queryString, String primaryField, Analyzer analyzer) throws ParseException {
        if (queryString.contains(getDocDef().getLangField() + ":")) {
            String lang = queryString.substring(queryString.lastIndexOf(":") + 1);
            if (!"*".equals(lang))
                analyzer = Util.getLocalizedAnalyzer(lang);
        }
        return super.preParseQuery(queryString, primaryField, analyzer);
    }
}
