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

package org.apache.jena.shacl.sys;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/** Constants */
public class C {
    public static Literal mTRUE = ResourceFactory.createTypedLiteral(true);
    public static Literal mFALSE = ResourceFactory.createTypedLiteral(false);

    public static Node rdfType = RDF.Nodes.type;
    public static Node rdfsClass = RDFS.Nodes.Class;
    public static Node rdfsSubclassOf = RDFS.subClassOf.asNode();
    public static Node TRUE = NodeConst.nodeTrue;
    public static Node FALSE = NodeConst.nodeFalse;

    public static final Node FIRST = RDF.first.asNode() ;
    public static final Node REST = RDF.rest.asNode() ;
    public static final Node NIL = RDF.nil.asNode() ;

}
