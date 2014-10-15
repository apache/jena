/*
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

package org.apache.jena.query.spatial;

import org.apache.jena.query.spatial.assembler.SpatialVocab;
import org.apache.lucene.store.Directory;
import org.apache.solr.client.solrj.SolrServer;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;
import com.hp.hpl.jena.sparql.util.Context;

public class SpatialDatasetFactory
{
    static { SpatialQuery.init(); }
    
    /** Use an assembler file to build a dataset with spatial search capabilities */ 
    public static Dataset create(String assemblerFile)
    {
        return (Dataset)AssemblerUtils.build(assemblerFile, SpatialVocab.spatialDataset) ;
    }

    /** Create a text-indexed dataset */ 
    public static Dataset create(Dataset base, SpatialIndex textIndex)
    {
        DatasetGraph dsg = base.asDatasetGraph() ;
        dsg = create(dsg, textIndex) ;
        return DatasetFactory.create(dsg) ;
    }


    /** Create a text-indexed dataset */ 
    public static DatasetGraph create(DatasetGraph dsg, SpatialIndex spatialIndex)
    {
        SpatialDocProducer producer = new SpatialDocProducerTriples(spatialIndex) ;
        DatasetGraph dsgt = new DatasetGraphSpatial(dsg, spatialIndex, producer) ;
        // Also set on dsg
        Context c = dsgt.getContext() ;
        
        dsgt.getContext().set(SpatialQuery.spatialIndex, spatialIndex) ;
        return dsgt ;

    }
    
    /** Create a Lucene TextIndex */ 
    public static SpatialIndex createLuceneIndex(Directory directory, EntityDefinition entMap)
    {
        SpatialIndex index = new SpatialIndexLucene(directory, entMap) ;
        return index ; 
    }

    /** Create a text-indexed dataset, using Lucene */ 
    public static Dataset createLucene(Dataset base, Directory directory, EntityDefinition entMap)
    {
        SpatialIndex index = createLuceneIndex(directory, entMap) ;
        return create(base, index) ; 
    }

    /** Create a text-indexed dataset, using Lucene */ 
    public static DatasetGraph createLucene(DatasetGraph base, Directory directory, EntityDefinition entMap)
    {
        SpatialIndex index = createLuceneIndex(directory, entMap) ;
        return create(base, index) ; 
    }

    /** Create a Solr TextIndex */ 
    public static SpatialIndex createSolrIndex(SolrServer server, EntityDefinition entMap)
    {
        SpatialIndex index = new SpatialIndexSolr(server, entMap) ;
        return index ; 
    }

    /** Create a text-indexed dataset, using Solr */ 
    public static Dataset createSolrIndex(Dataset base, SolrServer server, EntityDefinition entMap)
    {
        SpatialIndex index = createSolrIndex(server, entMap) ;
        return create(base, index) ; 
    }

    /** Create a text-indexed dataset, using Solr */ 
    public static DatasetGraph createSolrIndex(DatasetGraph base, SolrServer server, EntityDefinition entMap)
    {
        SpatialIndex index = createSolrIndex(server, entMap) ;
        return create(base, index) ; 
    }
}

