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

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphExtract;
import com.hp.hpl.jena.graph.TripleBoundary;

/**
     ModelExtract - a wrapper for GraphExtract, allowing rooted sub-models to be
     extracted from other models with some boundary condition.
*/
public class ModelExtract
    {
    /**
         The statement boundary used to bound the extraction.
    */
    protected StatementBoundary boundary;
    
    /**
         Initialise this ModelExtract with a boundary condition.
    */
    public ModelExtract( StatementBoundary b )
        { boundary = b; }
    
    /**
         Answer the rooted sub-model.
    */
    public Model extract( Resource r, Model s )
        { return extractInto( ModelFactory.createDefaultModel(), r, s ); }
    
    /**
         Answer <code>model</code> after updating it with the sub-graph of
         <code>s</code> rooted at <code>r</code>, bounded by this instances
         <code>boundary</code>.
    */
    public Model extractInto( Model model, Resource r, Model s )
        { TripleBoundary tb = boundary.asTripleBoundary( s );
        Graph g = getGraphExtract( tb ) .extractInto( model.getGraph(), r.asNode(), s.getGraph() );
        return ModelFactory.createModelForGraph( g ); }
    
    /**
         Answer a GraphExtract initialised with <code>tb</code>; extension point
         for sub-classes (specifically TestModelExtract's mocks).
    */
    protected GraphExtract getGraphExtract( TripleBoundary tb )
        { return new GraphExtract( tb ); }
    }
