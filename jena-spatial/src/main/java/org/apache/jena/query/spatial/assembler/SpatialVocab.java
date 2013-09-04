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

package org.apache.jena.query.spatial.assembler;

import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.tdb.assembler.Vocab ;

public class SpatialVocab
{
    public static final String NS                   =  "http://jena.apache.org/spatial#" ;

    public static final Resource spatialDataset     = Vocab.resource(NS, "SpatialDataset") ;
    public static final Property pDataset           = Vocab.property(NS, "dataset") ;
    public static final Property pIndex             = Vocab.property(NS, "index") ;
    
    public static final Resource spatialIndex       = Vocab.resource(NS, "SpatialIndex") ;
    public static final Resource spatialIndexSolr   = Vocab.resource(NS, "SpatialIndexSolr") ;
    public static final Resource spatialIndexLucene = Vocab.resource(NS, "SpatialIndexLucene") ;
    public static final Property pServer            = Vocab.property(NS, "server") ;            // Solr
    public static final Property pSolrHome          = Vocab.property(NS, "solrHome") ;          // EmbeddedSolrServer 
    public static final Property pDirectory         = Vocab.property(NS, "directory") ;         // Lucene
    public static final Property pDefinition        = Vocab.property(NS, "definition") ;
    
    // Entity definition
    public static final Resource definition         = Vocab.resource(NS, "EntityDefinition") ;
    public static final Property pEntityField       = Vocab.property(NS, "entityField") ;
    public static final Property pGeoField          = Vocab.property(NS, "geoField") ;
    public static final Property pHasSpatialPredicatePairs = Vocab.property(NS, "hasSpatialPredicatePairs") ;
    public static final Property pHasWKTPredicates  = Vocab.property(NS, "hasWKTPredicates") ;
    public static final Property pLatitude             = Vocab.property(NS, "latitude") ;
    public static final Property pLongitude         = Vocab.property(NS, "longitude") ;
    public static final Property pSpatialContextFactory = Vocab.property(NS, "spatialContextFactory") ;
    // public static final Property pOptional          = Vocab.property(NS, "optional") ;

}

