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

package com.hp.hpl.jena.sparql.graph;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.vocabulary.OWL ;
import com.hp.hpl.jena.vocabulary.RDF ;

public class NodeConst
{
    public static final Node nodeTrue       = NodeFactory.createLiteral("true", null,  XSDDatatype.XSDboolean) ; 
    public static final Node nodeFalse      = NodeFactory.createLiteral("false", null,  XSDDatatype.XSDboolean) ; 
    public static final Node nodeZero       = NodeFactory.createLiteral("0", null,  XSDDatatype.XSDinteger) ;
    public static final Node nodeOne        = NodeFactory.createLiteral("1", null,  XSDDatatype.XSDinteger) ;
    public static final Node nodeTwo        = NodeFactory.createLiteral("2", null,  XSDDatatype.XSDinteger) ;
    public static final Node nodeMinusOne   = NodeFactory.createLiteral("-1", null,  XSDDatatype.XSDinteger) ;
    public static final Node emptyString    = NodeFactory.createLiteral("") ;
    
    public static final Node nodeRDFType    = RDF.Nodes.type ;
    public static final Node nodeFirst      = RDF.Nodes.first ;
    public static final Node nodeRest       = RDF.Nodes.rest ;
    public static final Node nodeNil        = RDF.Nodes.nil ;
    public static final Node nodeANY        = Node.ANY ;
    
    public static final Node nodeOwlSameAs  = OWL.sameAs.asNode() ;
    public static final Node dtRDFlangString = NodeFactory.createURI(RDF.getURI()+"langString") ;

}
