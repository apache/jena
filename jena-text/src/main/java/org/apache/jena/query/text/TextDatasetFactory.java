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

import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.query.text.assembler.TextVocab ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.assembler.AssemblerUtils ;
import org.apache.jena.sparql.util.Context ;
import org.apache.lucene.analysis.Analyzer ;
import org.apache.lucene.store.Directory ;
import org.apache.solr.client.solrj.SolrServer ;

import java.io.File;

public class TextDatasetFactory
{
    static { TextQuery.init(); }
    
    /** Use an assembler file to build a dataset with text search capabilities */ 
    public static Dataset create(String assemblerFile)
    {
        return (Dataset)AssemblerUtils.build(assemblerFile, TextVocab.textDataset) ;
    }

    /** Create a text-indexed dataset */ 
    public static Dataset create(Dataset base, TextIndex textIndex)
    {
        return create(base, textIndex, false);
    }
    
    /** Create a text-indexed dataset, optionally allowing the text index to be closed if the Dataset is */
    public static Dataset create(Dataset base, TextIndex textIndex, boolean closeIndexOnDSGClose)
    {
        DatasetGraph dsg = base.asDatasetGraph() ;
        dsg = create(dsg, textIndex, closeIndexOnDSGClose) ;
        return DatasetFactory.create(dsg) ;
    }
    
    /** Create a text-indexed dataset, optionally allowing the text index to be closed if the Dataset is */
    public static Dataset create(Dataset base, TextIndex textIndex, boolean closeIndexOnDSGClose, TextDocProducer producer)
    {
        DatasetGraph dsg = base.asDatasetGraph() ;
        dsg = create(dsg, textIndex, closeIndexOnDSGClose, producer) ;
        return DatasetFactory.create(dsg) ;
    }


    /** Create a text-indexed DatasetGraph */ 
    public static DatasetGraph create(DatasetGraph dsg, TextIndex textIndex)
    {
        return create(dsg, textIndex, false);
    }
    
    /** Create a text-indexed DatasetGraph, optionally allowing the text index to be closed if the DatasetGraph is */
    public static DatasetGraph create(DatasetGraph dsg, TextIndex textIndex, boolean closeIndexOnDSGClose)
    {
        return create(dsg, textIndex, closeIndexOnDSGClose, null);
    }
    
    /** Create a text-indexed DatasetGraph, optionally allowing the text index to be closed if the DatasetGraph is */
    public static DatasetGraph create(DatasetGraph dsg, TextIndex textIndex, boolean closeIndexOnDSGClose, TextDocProducer producer) {
        if (producer == null) producer = new TextDocProducerTriples(textIndex) ;
        DatasetGraph dsgt = new DatasetGraphText(dsg, textIndex, producer, closeIndexOnDSGClose) ;
        // Also set on dsg
        Context c = dsgt.getContext() ;
        c.set(TextQuery.textIndex, textIndex) ;
        
        return dsgt ;
    }

    /**
     * Create a Lucene TextIndex
     *
     * @param directory The Lucene Directory for the index
     * @param def The EntityDefinition that defines how entities are stored in the index
     * @param queryAnalyzer The analyzer to be used to find terms in the query text.  If null, then the analyzer defined by the EntityDefinition will be used.
     */
    public static TextIndex createLuceneIndex(Directory directory, EntityDefinition def, Analyzer queryAnalyzer)
    {
        TextIndex index = new TextIndexLucene(directory, def, queryAnalyzer) ;
        return index ;
    }

    /**
     * Create a Lucene TextIndex
     * 
     * @param directory The Lucene Directory for the index
     * @param def The EntityDefinition that defines how entities are stored in the index
     * @param analyzer The analyzer to be used to index literals. If null, then the standard analyzer will be used.
     * @param queryAnalyzer The analyzer to be used to find terms in the query text.  If null, then the analyzer defined by the EntityDefinition will be used.
     */ 
    public static TextIndex createLuceneIndex(Directory directory, EntityDefinition def, Analyzer analyzer, Analyzer queryAnalyzer)
    {
        TextIndex index = new TextIndexLucene(directory, def, analyzer, queryAnalyzer) ;
        return index ; 
    }

    /**
     * Create a localized Lucene TextIndex
     *
     * @param directory The Lucene Directory for the index
     * @param def The EntityDefinition that defines how entities are stored in the index
     * @param lang The language related with the analyzer.
     * @param queryAnalyzer The analyzer to be used to find terms in the query text.  If null, then the analyzer defined by the EntityDefinition will be used.
     */
    public static TextIndex createLuceneIndexFromLanguage(Directory directory, EntityDefinition def, String lang, Analyzer queryAnalyzer)
    {
        return createLuceneIndex(directory, def, LuceneUtil.createAnalyzer(lang, TextIndexLucene.VER), queryAnalyzer);
    }

    /**
     * Create a multilingual Lucene TextIndex
     *
     * @param directory The Lucene Directory for the index
     * @param def The EntityDefinition that defines how entities are stored in the index
     */
    public static TextIndex createLuceneIndexMultiLingual(File directory, EntityDefinition def)
    {
        TextIndex index = new TextIndexLuceneMultiLingual(directory, def) ;
        return index ;
    }

    /**
     * Create a text-indexed dataset, using Lucene
     *
     * @param base the base Dataset
     * @param directory The Lucene Directory for the index
     * @param def The EntityDefinition that defines how entities are stored in the index
     * @param queryAnalyzer The analyzer to be used to find terms in the query text.  If null, then the analyzer defined by the EntityDefinition will be used.
     */
    public static Dataset createLucene(Dataset base, Directory directory, EntityDefinition def, Analyzer queryAnalyzer)
    {
        TextIndex index = createLuceneIndex(directory, def, queryAnalyzer) ;
        return create(base, index, true) ;
    }

    /**
     * Create a text-indexed dataset, using Lucene
     * 
     * @param base the base Dataset
     * @param directory The Lucene Directory for the index
     * @param def The EntityDefinition that defines how entities are stored in the index
     * @param analyzer The analyzer to be used to index literals. If null, then the standard analyzer will be used.
     * @param queryAnalyzer The analyzer to be used to find terms in the query text.  If null, then the analyzer defined by the EntityDefinition will be used.
     */ 
    public static Dataset createLucene(Dataset base, Directory directory, EntityDefinition def, Analyzer analyzer, Analyzer queryAnalyzer)
    {
        TextIndex index = createLuceneIndex(directory, def, analyzer, queryAnalyzer) ;
        return create(base, index, true) ; 
    }

    /**
     * Create a localized text-indexed dataset, using Lucene
     *
     * @param base the base Dataset
     * @param directory The Lucene Directory for the index
     * @param def The EntityDefinition that defines how entities are stored in the index
     * @param lang The language related with the analyzer.
     * @param queryAnalyzer The analyzer to be used to find terms in the query text.  If null, then the analyzer defined by the EntityDefinition will be used.
     */
    public static Dataset createLuceneFromLanguage(Dataset base, Directory directory, EntityDefinition def, String lang, Analyzer queryAnalyzer)
    {
        TextIndex index = createLuceneIndexFromLanguage(directory, def, lang, queryAnalyzer) ;
        return create(base, index, true) ;
    }

    /**
     * Create a multilingual text-indexed dataset, using Lucene
     *
     * @param base the base Dataset
     * @param directory The Lucene Directory for the index
     * @param def The EntityDefinition that defines how entities are stored in the index
     */
    public static Dataset createLuceneMultilingual(Dataset base, File directory, EntityDefinition def)
    {
        TextIndex index = createLuceneIndexMultiLingual(directory, def) ;
        return create(base, index, true) ;
    }

    /**
     * Create a text-indexed dataset, using Lucene
     *
     * @param base the base DatasetGraph
     * @param directory The Lucene Directory for the index
     * @param def The EntityDefinition that defines how entities are stored in the index
     * @param queryAnalyzer The analyzer to be used to find terms in the query text.  If null, then the analyzer defined by the EntityDefinition will be used.
     */
    public static DatasetGraph createLucene(DatasetGraph base, Directory directory, EntityDefinition def, Analyzer queryAnalyzer)
    {
        TextIndex index = createLuceneIndex(directory, def, queryAnalyzer) ;
        return create(base, index, true) ;
    }

    /**
     * Create a text-indexed dataset, using Lucene
     * 
     * @param base the base DatasetGraph
     * @param directory The Lucene Directory for the index
     * @param def The EntityDefinition that defines how entities are stored in the index
     * @param analyzer The analyzer to be used to index literals. If null, then the standard analyzer will be used.
     * @param queryAnalyzer The analyzer to be used to find terms in the query text.  If null, then the analyzer defined by the EntityDefinition will be used.
     */ 
    public static DatasetGraph createLucene(DatasetGraph base, Directory directory, EntityDefinition def, Analyzer analyzer, Analyzer queryAnalyzer)
    {
        TextIndex index = createLuceneIndex(directory, def, analyzer, queryAnalyzer) ;
        return create(base, index, true) ; 
    }

    /**
     * Create a localized text-indexed dataset, using Lucene
     *
     * @param base the base DatasetGraph
     * @param directory The Lucene Directory for the index
     * @param def The EntityDefinition that defines how entities are stored in the index
     * @param lang The language related with the analyzer.
     * @param queryAnalyzer The analyzer to be used to find terms in the query text.  If null, then the analyzer defined by the EntityDefinition will be used.
     */
    public static DatasetGraph createLuceneFromLanguage(DatasetGraph base, Directory directory, EntityDefinition def, String lang, Analyzer queryAnalyzer)
    {
        TextIndex index = createLuceneIndexFromLanguage(directory, def, lang, queryAnalyzer) ;
        return create(base, index, true) ;
    }

    /**
     * Create a multilingual text-indexed dataset, using Lucene
     *
     * @param base the base DatasetGraph
     * @param directory The Lucene Directory for the index
     * @param def The EntityDefinition that defines how entities are stored in the index
     */
    public static DatasetGraph createLuceneMultilingual(DatasetGraph base, File directory, EntityDefinition def)
    {
        TextIndex index = createLuceneIndexMultiLingual(directory, def) ;
        return create(base, index, true) ;
    }


    /** Create a Solr TextIndex */
    public static TextIndex createSolrIndex(SolrServer server, EntityDefinition entMap)
    {
        TextIndex index = new TextIndexSolr(server, entMap) ;
        return index ; 
    }

    /** Create a text-indexed dataset, using Solr */ 
    public static Dataset createSolrIndex(Dataset base, SolrServer server, EntityDefinition entMap)
    {
        TextIndex index = createSolrIndex(server, entMap) ;
        return create(base, index, true) ; 
    }

    /** Create a text-indexed dataset, using Solr */ 
    public static DatasetGraph createSolrIndex(DatasetGraph base, SolrServer server, EntityDefinition entMap)
    {
        TextIndex index = createSolrIndex(server, entMap) ;
        return create(base, index, true) ; 
    }
}

