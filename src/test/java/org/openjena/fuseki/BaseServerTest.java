/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.logging.Log ;
import org.openjena.fuseki.Fuseki ;
import org.openjena.fuseki.rest.UpdateRemote ;
import org.openjena.fuseki.server.SPARQLServer ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class BaseServerTest extends BaseTest
{
    // Abstraction that runs one server.
    // Inherit from this class to add starting/stopping a server.  
    
    protected static final int port             = 3535 ;
    protected static final String datasetPath   = "/dataset" ;
    protected static final String serviceUpdate = "http://localhost:"+port+datasetPath+"/update" ; 
    protected static final String serviceQuery  = "http://localhost:"+port+datasetPath+"/query" ; 
    protected static final String serviceREST   = "http://localhost:"+port+datasetPath+"/data" ;
    
    protected static final String gn1       = "http://graph/1" ;
    protected static final String gn2       = "http://graph/2" ;
    protected static final String gn99      = "http://graph/99" ;
    protected static final Model graph1 = 
        ModelFactory.createModelForGraph(SSE.parseGraph("(base <http://example/> (graph (<x> <p> 1)))")) ;
    protected static final Model graph2 = 
        ModelFactory.createModelForGraph(SSE.parseGraph("(base <http://example/> (graph (<x> <p> 2)))")) ;

    
    private static int referenceCount = 0 ;
    private static SPARQLServer server = null ; 
    
    // Due to ordering issues, you must call these from your own @BeforeClass
    
    @BeforeClass static public void setupServer()
    { 
        if ( referenceCount == 0 )
            serverStart() ;
        referenceCount ++ ;
    }
    
    @AfterClass static public void clearupServer() 
    { 
        referenceCount -- ;
        if ( referenceCount == 0 )
            serverStop() ;
    }
    
    protected static void serverStart()
    {
        Log.logLevel(SPARQLServer.log.getName(), org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING) ;
        Log.logLevel(Fuseki.serverlog.getName(), org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING) ;
        Log.logLevel("org.eclipse.jetty", org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING) ;
        server = new SPARQLServer(DatasetGraphFactory.createMem(), datasetPath, port) ;
        server.start() ;
        
    }
    
    protected static void serverStop()
    {
        server.stop() ;
        Log.logLevel(SPARQLServer.log.getName(), org.apache.log4j.Level.INFO, java.util.logging.Level.INFO) ;
        Log.logLevel(Fuseki.serverlog.getName(), org.apache.log4j.Level.INFO, java.util.logging.Level.INFO) ;
        Log.logLevel("org.eclipse.jetty", org.apache.log4j.Level.INFO, java.util.logging.Level.INFO) ;
    }
    
    protected static void serverReset()
    {
        UpdateRemote.executeClear(serviceUpdate) ;
    }
    
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