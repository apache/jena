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

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

/**
    ModelGraphInterface - this interface mediates between the API Model level
    and the SPI Graph level. It may change if the SPI changes, is more fluid than
    Model and ModelCon, so don't use if it you don't *need* to.
*/
public interface ModelGraphInterface
    {
    /**
       Answer a Statement in this Model who's SPO is that of the triple <code>t</code>.
    */
    Statement asStatement( Triple t );
    
    /** 
        Answer the Graph which this Model is presenting.
    */
    Graph getGraph();

    /**
       Answer an RDF node wrapping <code>n</code> suitably; URI nodes
       become Resources with the same URI, blank nodes become Resources
       with URI null but the same AnonId, and literal nodes become Literals
       with <code>n</code> as their value carrier.
    */
    RDFNode asRDFNode( Node n );
    
    Resource wrapAsResource( Node n );
    }
