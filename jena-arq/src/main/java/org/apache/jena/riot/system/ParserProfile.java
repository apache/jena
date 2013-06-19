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


import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.riot.lang.LabelToNode ;
import org.apache.jena.riot.tokens.Token ;

import com.hp.hpl.jena.sparql.core.Quad ;

public interface ParserProfile
{
    public String resolveIRI(String uriStr, long line, long col) ;
    public IRI makeIRI(String uriStr, long line, long col) ;
    
    /** Create a triple */
    public Triple createTriple(Node subject, Node predicate, Node object, long line, long col) ;

    /** Create a Quad */
    public Quad createQuad(Node graph, Node subject, Node predicate, Node object, long line, long col) ;
    
    /** Create a URI Node */
    public Node createURI(String uriStr, long line, long col) ;
    
    /** Create a literal for a string+datatype */
    public Node createTypedLiteral(String lexical, RDFDatatype datatype, long line, long col) ;
    
    /** Create a literal for a string+language */
    public Node createLangLiteral(String lexical, String langTag, long line, long col) ;
    
    /** Create a literal for a string */ 
    public Node createStringLiteral(String lexical, long line, long col) ;
    
    /** Create a fresh blank node based on scope and label */ 
    public Node createBlankNode(Node scope, String label, long line, long col) ;
    /** Create a fresh blank node */ 
    public Node createBlankNode(Node scope, long line, long col) ;
    
    /** Make a node from a token - called after all else has been tried - return null for no such node */
    public Node createNodeFromToken(Node scope, Token token, long line, long col) ;
    
    /** Make any node from a token as appropriate */
    public Node create(Node currentGraph, Token token) ;
    
    public LabelToNode getLabelToNode() ;
    public void setLabelToNode(LabelToNode labelToNode) ;
    
    public ErrorHandler getHandler() ;
    public void setHandler(ErrorHandler handler) ;
    
    public Prologue getPrologue() ;
    public void setPrologue(Prologue prologue) ;
    
    public boolean isStrictMode() ;
    public void setStrictMode(boolean mode) ;
}
