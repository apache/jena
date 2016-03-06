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

package org.apache.jena.riot.system;

import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;

/**
 * Create core RDF objects: {@link Node}s, {@link Triple}s, {@link Quad}s,
 * {@link Graph}, {@link DatasetGraph}s.
 * <p>
 */
public interface FactoryRDF {
    // ?? Are these too varied?
    public Graph createGraph() ;
    // ?? Are these too varied?
    public DatasetGraph createDatasetGraph() ;
    public Triple createTriple(Node subject, Node predicate, Node object) ;
    public Quad createQuad(Node graph, Node subject, Node predicate, Node object) ;
    public Node createURI(String uriStr) ;
    public Node createTypedLiteral(String lexical, RDFDatatype datatype) ;
    public Node createLangLiteral(String lexical, String langTag) ;
    public Node createStringLiteral(String lexical) ;
    /** Create a blank node */
    public Node createBlankNode() ;
    /** Create a blank node with the given string as internal system id */ 
    public Node createBlankNode(String label) ;
    /** Create a blank with the internal system id taken from 128 bit number provided.
     */
    public Node createBlankNode(long mostSigBits, long leastSigBits) ;

//    // Object for scope better?
//    public Node createBlankNode(Node scope, String label) ;
//    // Object for scope better?
//    public Node createBlankNode(Node scope) ;
}