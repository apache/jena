/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki;

import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class BaseServerTest extends BaseTest
{
    // Abstraction that runs one server.
    // Inherit from this class to add starting/stopping a server.  
    
    public static final String datasetPath   = "/dataset" ;
    public static final String serviceUpdate = "http://localhost:"+ServerTest.port+datasetPath+"/update" ; 
    public static final String serviceQuery  = "http://localhost:"+ServerTest.port+datasetPath+"/query" ; 
    public static final String serviceREST   = "http://localhost:"+ServerTest.port+datasetPath+"/data" ;
    
    protected static final String gn1       = "http://graph/1" ;
    protected static final String gn2       = "http://graph/2" ;
    protected static final String gn99      = "http://graph/99" ;
    
    protected static final Node n1          = Node.createURI("http://graph/1") ;
    protected static final Node n2          = Node.createURI("http://graph/2") ;
    protected static final Node n99         = Node.createURI("http://graph/99") ;
    
    protected static final Graph graph1     = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 1)))") ;
    protected static final Graph graph2     = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 2)))") ;
    
    protected static final Model model1     = ModelFactory.createModelForGraph(graph1) ;
    protected static final Model model2     = ModelFactory.createModelForGraph(graph2) ;
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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