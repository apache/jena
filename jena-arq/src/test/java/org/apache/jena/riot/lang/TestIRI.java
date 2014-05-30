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

package org.apache.jena.riot.lang;

import com.hp.hpl.jena.graph.Node ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.riot.ErrorHandlerTestLib ;
import org.apache.jena.riot.ErrorHandlerTestLib.ExWarning ;
import org.apache.jena.riot.checker.CheckerIRI ;
import org.apache.jena.riot.system.Checker ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.IRILib ;
import org.apache.jena.riot.system.RiotLib ;
import org.junit.Test ;

public class TestIRI extends BaseTest
{
    static protected final ErrorHandler handler = new ErrorHandlerTestLib.ErrorHandlerEx() ;
    static protected final Checker checker = new Checker(new ErrorHandlerTestLib.ErrorHandlerEx()) ;
    
    static IRIFactory factory = IRIFactory.iriImplementation() ;
    
    @Test public void iri1()  { testIRI("http://example/") ; }
    
    @Test(expected=ErrorHandlerTestLib.ExError.class)
    // No relative IRIs
    public void iri2()  { testIRI("example") ; }
    
    @Test(expected=ExWarning.class) 
    public void iriErr1()  
    { testIRI("http:") ; }

    @Test(expected=ExWarning.class) 
    public void iriErr2()  { testIRI("http:///::") ; }

    @Test(expected=ExWarning.class) 
    public void iriErr3()  { testIRI("http://example/.") ; }
    
    private void testIRI(String uriStr)
    {
        IRI iri = factory.create(uriStr) ;
        CheckerIRI.iriViolations(iri, handler) ;
    }
    
    @Test public void bNodeIRI_1()
    {
        Node n = RiotLib.createIRIorBNode("_:abc") ;
        assertTrue(n.isBlank()) ;
        assertEquals("abc", n.getBlankNodeLabel()) ;
    }
    
    @Test public void bNodeIRI_2()
    {
        Node n = RiotLib.createIRIorBNode("abc") ;
        assertTrue(n.isURI()) ;
        assertEquals("abc", n.getURI()) ;
    }
    
    @Test public void fileIRI_1()
    {
        String uri = testFileIRI("D.ttl") ; 
        assertTrue(uri.endsWith("D.ttl")) ;
    }
    
    @Test public void fileIRI_2()
    {
        String uri = testFileIRI("file:/D.ttl") ; 
        assertTrue(uri.endsWith("D.ttl")) ;
    }
    
    @Test public void fileIRI_3()
    {
        String uri = testFileIRI("file://D.ttl") ; 
        assertTrue(uri.endsWith("D.ttl")) ;
    }

    @Test public void fileIRI_4()
    {
        String iri = testFileIRI("file:///D.ttl") ;
        // Even on windows, this is used as-is so no drive letter. 
        assertEquals("file:///D.ttl", iri) ;
    }
    
    private static String testFileIRI(String fn)
    {
        String uri1 = IRILib.filenameToIRI(fn) ;
        assertTrue(uri1.startsWith("file:///")) ;
        String uri2 = IRILib.filenameToIRI(uri1) ;
        assertEquals(uri1, uri2) ;
        return uri1 ;
    }
}
