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

import org.apache.jena.graph.Node;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TextIndexLuceneMultilingual implements TextIndex {

    Hashtable<String, TextIndex> indexes;
    private final EntityDefinition docDef;
    private final Directory        directory ;

    public TextIndexLuceneMultilingual(Directory directory, EntityDefinition def) {
        this.directory = directory ;
        this.docDef = def;
        indexes = new Hashtable<>();

        //default index created first. Localized index will be created on the fly.
        TextIndex index = new TextIndexLucene(directory, def, null, null);
        indexes.put("default", index);
    }

    public Collection<TextIndex> getIndexes() {
        return indexes.values();
    }

    TextIndex getIndex(String lang) {
        lang = LuceneUtil.getISO2Language(lang);
        if (lang == null)
            lang = "default";

        if (!indexes.containsKey(lang)) {
            //dynamic creation of localized index
            try {
                Analyzer analyzer = LuceneUtil.createAnalyzer(lang);
                if (analyzer != null) {
                    Directory langDir;
                    if (directory instanceof FSDirectory) {
                        File dir = ((FSDirectory) directory).getDirectory();
                        File indexDirLang = new File(dir, lang);
                        langDir = FSDirectory.open(indexDirLang);
                    }
                    else
                        langDir = new RAMDirectory();
                    TextIndex index = new TextIndexLucene(langDir, docDef, analyzer, null);
                    indexes.put(lang, index);
                }
                else
                    lang = "default";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return indexes.get(lang);
    }

    @Override
    public void prepareCommit() {
        for (TextIndex index : indexes.values())
            index.prepareCommit();
    }

    @Override
    public void commit() {
        for (TextIndex index : indexes.values())
            index.commit();
    }

    @Override
    public void rollback() {
        for (TextIndex index : indexes.values())
            index.rollback();
    }

    @Override
    public void addEntity(Entity entity) {
        String lang = entity.getLanguage();
        getIndex(lang).addEntity(entity);
    }

    @Override
    public void updateEntity(Entity entity) {
        String lang = entity.getLanguage();
        getIndex(lang).updateEntity(entity);
    }

    @Override
    public Map<String, Node> get(String uri) {
        return null;
    }

    @Override
    public List<Node> query(String qs, int limit) {
        return null;
    }

    @Override
    public List<Node> query(String qs) {
        return null;
    }

    @Override
    public EntityDefinition getDocDef() {
        return docDef;
    }

    @Override
    public void close() {
        for (TextIndex index : indexes.values())
            index.close();
    }

}
