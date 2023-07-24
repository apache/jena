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

package org.apache.jena.sparql.core.assembler;

import org.apache.jena.assembler.JA ;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.rdf.model.ResourceFactory ;

public class DatasetAssemblerVocab
{
    public static final String NS = JA.getURI() ;
    public static String getURI() { return NS ; }

    // General dataset
    public static final Resource tDataset            = ResourceFactory.createResource(NS+"RDFDataset") ;
    // Dataset to hold exactly one model.
    public static final Resource tDatasetOne         = ResourceFactory.createResource(NS+"RDFDatasetOne") ;

    // In-memory dataset
    public static final Resource tDatasetTxnMem      = ResourceFactory.createResource(NS+"DatasetTxnMem") ;

    // Specialised datasets
    public static final Resource tMemoryDataset      = ResourceFactory.createResource(NS+"MemoryDataset") ;
    public static final Resource tDatasetZero        = ResourceFactory.createResource(NS+"RDFDatasetZero") ;
    public static final Resource tDatasetSink        = ResourceFactory.createResource(NS+"RDFDatasetSink") ;
    public static final Resource tViewGraph          = ResourceFactory.createResource(NS+"ViewGraph") ;
    public static final Resource tDatasetView        = ResourceFactory.createResource(NS+"ViewDataset") ;

    public static final Property pDataset            = ResourceFactory.createProperty(NS, "dataset") ;
    // Dataset name - using a name means that the same dataset is return when assembled for shared resources.
    public static final Property pDatasetName        = ResourceFactory.createProperty(NS, "name") ;

    public static final Property pDefaultGraph       = ResourceFactory.createProperty(NS, "defaultGraph") ;

    public static final Property pNamedGraph         = ResourceFactory.createProperty(NS, "namedGraph") ;
    public static final Property pGraphName          = ResourceFactory.createProperty(NS, "graphName") ;

    public static final Property pGraph              = ResourceFactory.createProperty(NS, "graph") ;
    public static final Property pGraphAlt           = ResourceFactory.createProperty(NS, "graphData") ;

    public static final Property pIndex              = ResourceFactory.createProperty(NS, "textIndex") ;

    public static final Property pTransactional      = ResourceFactory.createProperty(NS, "transactional") ;

    public static final Property pContext            = ResourceFactory.createProperty(NS, "context") ;
    public static final Property pCxtName            = ResourceFactory.createProperty(NS, "cxtName") ;
    public static final Property pCxtValue           = ResourceFactory.createProperty(NS, "cxtValue") ;
}
