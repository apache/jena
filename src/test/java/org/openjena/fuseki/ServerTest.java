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
import org.openjena.fuseki.http.UpdateRemote ;
import org.openjena.fuseki.server.SPARQLServer ;

import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;

/** Manage a server for testing.
 * <pre>
    \@BeforeClass public static void beforeClass() { ServerTest.allocServer() ; }
    \@AfterClass public static void afterClass() { ServerTest.freeServer() ; }
    </pre>
 */
public class ServerTest extends BaseTest
{
    // Abstraction that runs one server.
    // Inherit from this class to add starting/stopping a server.  
    
    public static final int port             = 3535 ;
    public static final String datasetPath   = "/dataset" ;
    public static final String serviceUpdate = "http://localhost:"+port+datasetPath+"/update" ; 
    public static final String serviceQuery  = "http://localhost:"+port+datasetPath+"/query" ; 
    public static final String serviceREST   = "http://localhost:"+port+datasetPath+"/data" ;
    
    private static int referenceCount = 0 ;
    private static SPARQLServer server = null ; 
    
    // If not inheriting from this class, call:
    
    @BeforeClass static public void allocServer()
    { 
        if ( referenceCount == 0 )
            serverStart() ;
        referenceCount ++ ;
    }
    
    @AfterClass static public void freeServer() 
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
        server = new SPARQLServer(DatasetGraphFactory.createMem(), datasetPath, port, true) ;
        server.start() ;
        
    }
    
    protected static void serverStop()
    {
        server.stop() ;
        Log.logLevel(SPARQLServer.log.getName(), org.apache.log4j.Level.INFO, java.util.logging.Level.INFO) ;
        Log.logLevel(Fuseki.serverlog.getName(), org.apache.log4j.Level.INFO, java.util.logging.Level.INFO) ;
        Log.logLevel("org.eclipse.jetty", org.apache.log4j.Level.INFO, java.util.logging.Level.INFO) ;
    }
    
    public static void resetServer()
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