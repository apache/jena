/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;

import org.openjena.riot.checker.CheckerIRI ;
import org.openjena.riot.checker.NodeChecker ;
import org.openjena.riot.lang.LabelToNode ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.sparql.core.Quad ;

public class MakerChecker extends MakerBase implements Maker 
{
    private final ErrorHandler errorHandler ;
    private final NodeChecker literalChecker ;
    
    public MakerChecker(ErrorHandler errorHandler, NodeChecker literalChecker)
    {
        this.errorHandler = errorHandler ;
        this.literalChecker = literalChecker ;
    }

    @Override
    public String resolveIRI(Prologue prologue, String uriStr, long line, long col)
    {
        return makeIRI(prologue, uriStr, line, col).toString() ;
    }
    
    @Override
    public IRI makeIRI(Prologue prologue, String uriStr, long line, long col)
    {
        IRI iri = prologue.getResolver().resolve(uriStr) ;
        CheckerIRI.violations(iri, errorHandler, false, false, line, col) ;
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
        if ( graph == null || ( ! graph.isURI() ) )
        {
            errorHandler.error("Graph name is not a URI", line, col) ;
            throw new RiotException("Bad graph name: "+graph) ;
        }        
        checkTriple(subject,predicate,object,line,col) ;
    }
    
    @Override
    public Node createURI(Prologue prologue, String x, long line, long col)
    { 
        try {
            String resolvedIRI = resolveIRI(prologue, x, line, col) ;
            return Node.createURI(resolvedIRI) ;
        } catch (RiotException ex)
        {
            errorHandler.error(ex.getMessage(), line, col) ;
            throw ex ;
        }
    }

    @Override
    public Node createTypedLiteral(String lexical, Prologue prologue, String datatype, long line, long col)
    {
        // Resolve datatype.
        datatype = resolveIRI(prologue, datatype, line, col) ;
        RDFDatatype dt = Node.getType(datatype) ;
        return createTypedLiteral(lexical, dt, line, col) ;
}
    
    @Override
    public Node createTypedLiteral(String lexical, RDFDatatype datatype, long line, long col)
    {
        Node n = Node.createLiteral(lexical, null, datatype)  ;
        literalChecker.check(n, line, col) ;
        return n ;
    }

    @Override
    public Node createLangLiteral(String lexical, String langTag, long line, long col)
    {
        Node n = Node.createLiteral(lexical, langTag, null)  ;
        literalChecker.check(n, line, col) ;
        return n ;
    }
    
    @Override
    public Node createPlainLiteral(String lexical, long line, long col)
    {
        return Node.createLiteral(lexical) ;
    }

    @Override
    public Node createBlankNode(LabelToNode map, Node scope, String label, long line, long col)
    {
        return map.get(scope, label) ;
    }
    
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */