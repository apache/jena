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
import org.apache.jena.sys.JenaSystem ;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory ;

public class TextDatasetFactory
{
    static { JenaSystem.init(); }

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
        return DatasetFactory.wrap(dsg) ;
    }

    /** Create a text-indexed dataset, optionally allowing the text index to be closed if the Dataset is */
    public static Dataset create(Dataset base, TextIndex textIndex, boolean closeIndexOnDSGClose, TextDocProducer producer)
    {
        DatasetGraph dsg = base.asDatasetGraph() ;
        dsg = create(dsg, textIndex, closeIndexOnDSGClose, producer) ;
        return DatasetFactory.wrap(dsg) ;
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
        TextIndexConfig config = new TextIndexConfig(def);
        config.setQueryAnalyzer(queryAnalyzer);
        return createLuceneIndex(directory, config);
    }

    /**
     * Create a Lucene TextIndex
     *
     * @param directory The Lucene Directory for the index
     * @param config The config definition for the index instantiation.
     */
    public static TextIndex createLuceneIndex(Directory directory, TextIndexConfig config)
    {
        return new TextIndexLucene(directory, config) ;
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
        TextIndexConfig config = new TextIndexConfig(def);
        config.setQueryAnalyzer(queryAnalyzer);
        return createLucene(base, directory, config);
    }

    /**
     * Create a text-indexed dataset, using Lucene
     *
     * @param base the base Dataset
     * @param directory The Lucene Directory for the index
     * @param config The config definition for the index instantiation.
     */
    public static Dataset createLucene(Dataset base, Directory directory, TextIndexConfig config)
    {
        TextIndex index = createLuceneIndex(directory, config) ;
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
        TextIndexConfig config = new TextIndexConfig(def);
        config.setQueryAnalyzer(queryAnalyzer);
        return createLucene(base, directory, config) ;
    }

    /**
     * Create a text-indexed dataset, using Lucene
     *
     * @param base the base DatasetGraph
     * @param directory The Lucene Directory for the index
     * @param config The config definition for the index instantiation.
     */
    public static DatasetGraph createLucene(DatasetGraph base, Directory directory, TextIndexConfig config)
    {
        TextIndex index = createLuceneIndex(directory, config) ;
        return create(base, index, true) ;
    }
}

