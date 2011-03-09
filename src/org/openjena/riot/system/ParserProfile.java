/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.system;

import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.lang.LabelToNode ;
import org.openjena.riot.tokens.Token ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.sparql.core.Quad ;

public interface ParserProfile
{
//    public DatasetGraph createDatasetGraph(long line, long col) ;
//    public Graph createGraph(long line, long col) ;
    
    public String resolveIRI(String uriStr, long line, long col) ;
    public IRI makeIRI(String uriStr, long line, long col) ;
    
    public Triple createTriple(Node subject, Node predicate, Node object, long line, long col) ;
    public Quad createQuad(Node graph, Node subject, Node predicate, Node object, long line, long col) ;    
    public Node createURI(String uriStr, long line, long col) ;
    public Node createTypedLiteral(String lexical, RDFDatatype datatype, long line, long col) ;
    public Node createLangLiteral(String lexical, String langTag, long line, long col) ;
    public Node createPlainLiteral(String lexical, long line, long col) ;
    public Node createBlankNode(Node scope, String label, long line, long col) ;
    
    /** Make any node from a token as appropriate */
    public Node create(Node currentGraph, Token token) ;
    
    public LabelToNode getLabelToNode() ;
    public void setLabelToNode(LabelToNode labelToNode) ;
    
    public ErrorHandler getHandler() ;
    public void setHandler(ErrorHandler handler) ;
    
    public Prologue getPrologue() ;
    public void setPrologue(Prologue prologue) ;
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
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
