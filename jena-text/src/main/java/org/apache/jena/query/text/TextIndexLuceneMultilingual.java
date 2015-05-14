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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.List;

public class TextIndexLuceneMultilingual extends TextIndexLucene {

    public TextIndexLuceneMultilingual(Directory directory, EntityDefinition def) {
        super(directory, def, null) ;
    }

    @Override
    protected void updateDocument(Entity entity) throws IOException {
        Document doc = doc(entity);
        Term term = new Term(getDocDef().getEntityField(), entity.getId());
        Analyzer analyzer = LuceneUtil.getLocalizedAnalyzer(entity.getLanguage());
        if (analyzer == null)
            analyzer = getAnalyzer();
        getIndexWriter().updateDocument(term, doc, analyzer) ;
    }

    @Override
    protected void addDocument(Entity entity) throws IOException {
        Document doc = doc(entity) ;
        Analyzer analyzer = LuceneUtil.getLocalizedAnalyzer(entity.getLanguage());
        if (analyzer == null)
            analyzer = getAnalyzer();
        getIndexWriter().addDocument(doc, analyzer) ;
    }

    @Override
    protected List<Field> buildContentFields(Entity entity) {
        List<Field> list = super.buildContentFields(entity);
        String lang =  entity.getLanguage();
        if (lang == null || "".equals(lang))
            lang = "undef";
        list.add( new Field("lang", lang, StringField.TYPE_STORED ) );
        return list;
    }

    @Override
    protected Query preParseQuery(String queryString, String primaryField, Analyzer analyzer) throws ParseException {
        String lang = queryString.substring( queryString.lastIndexOf(":") + 1);
        if (!"undef".equals(lang))
            analyzer = LuceneUtil.getLocalizedAnalyzer(lang);

        return super.preParseQuery(queryString, primaryField, analyzer);
    }
}
