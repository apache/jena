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

package org.openjena.riot.system;

import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.RiotException ;
import org.openjena.riot.checker.CheckerIRI ;
import org.openjena.riot.checker.CheckerLiterals ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.sparql.core.Quad ;

public class ParserProfileChecker extends ParserProfileBase //implements ParserProfile 
{
    private boolean checkLiterals = true ;
    private boolean resolveURIs = true ;
    
    public ParserProfileChecker(Prologue prologue, ErrorHandler errorHandler)
    {
        super(prologue, errorHandler) ;
    }

    @Override
    public String resolveIRI(String uriStr, long line, long col)
    {
        return makeIRI(uriStr, line, col).toString() ;
    }
    
    @Override
    public IRI makeIRI(String uriStr, long line, long col)
    {
        IRI iri = prologue.getResolver().resolveSilent(uriStr) ;
        CheckerIRI.iriViolations(iri, errorHandler, line, col) ;
        return iri ;
    }
    
    @Override
    public Triple createTriple(Node subject, Node predicate, Node object, long line, long col)
    {
        checkTriple(subject,predicate,object,line,col) ;
        return super.createTriple(subject, predicate, object, line, col) ;
    }
    
    private void checkTriple(Node subject, Node predicate, Node object, long line, long col)
    {
        if ( subject == null || ( ! subject.isURI() && ! subject.isBlank() ) )
        {
            errorHandler.error("Subject is not a URI or blank node", line, col) ;
            throw new RiotException("Bad subject: "+subject) ;
        }
        if ( predicate == null || ( ! predicate.isURI() ) )
        {
            errorHandler.error("Predicate not a URI", line, col) ;
            throw new RiotException("Bad predicate: "+predicate) ;
        }
        if ( object == null || ( ! object.isURI() && ! object.isBlank() && ! object.isLiteral() ) )
        {
            errorHandler.error("Object is not a URI, blank node or literal", line, col) ;
            throw new RiotException("Bad object: "+object) ;
        }
    }

    @Override
    public Quad createQuad(Node graph, Node subject, Node predicate, Node object, long line, long col)
    {
        checkQuad(graph,subject,predicate,object,line,col) ;
        return super.createQuad(graph,subject,predicate,object,line,col) ;
    }
    
    private void checkQuad(Node graph, Node subject, Node predicate, Node object, long line, long col)
    {
        if ( graph != null && ! graph.isURI() )
        {
            errorHandler.error("Graph name is not a URI", line, col) ;
            throw new RiotException("Bad graph name: "+graph) ;
        }        
        checkTriple(subject,predicate,object,line,col) ;
    }
    
    @Override
    public Node createURI(String x, long line, long col)
    { 
        try {
            String resolvedIRI = resolveIRI(x, line, col) ;
            return Node.createURI(resolvedIRI) ;
        } catch (RiotException ex)
        {
            // Error was handled.
            //errorHandler.error(ex.getMessage(), line, col) ;
            throw ex ;
        }
    }

//    @Override
//    public Node createTypedLiteral(String lexical, String datatype, long line, long col)
//    {
//        // Resolve datatype.
//        datatype = resolveIRI(datatype, line, col) ;
//        RDFDatatype dt = Node.getType(datatype) ;
//        return createTypedLiteral(lexical, dt, line, col) ;
//    }
    
    @Override
    public Node createTypedLiteral(String lexical, RDFDatatype datatype, long line, long col)
    {
        Node n = Node.createLiteral(lexical, null, datatype)  ;
        CheckerLiterals.checkLiteral(lexical, datatype, errorHandler, line, col) ;
        return n ;
    }

    @Override
    public Node createLangLiteral(String lexical, String langTag, long line, long col)
    {
        Node n = Node.createLiteral(lexical, langTag, null)  ;
        CheckerLiterals.checkLiteral(lexical, langTag, errorHandler, line, col) ;
        return n ;
    }
    
    @Override
    public Node createPlainLiteral(String lexical, long line, long col)
    {
        return Node.createLiteral(lexical) ;
    }

    @Override
    public Node createBlankNode(Node scope, String label, long line, long col)
    {
        return labelMapping.get(scope, label) ;
    }
    
}
