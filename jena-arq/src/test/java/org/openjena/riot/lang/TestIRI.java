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

package org.openjena.riot.lang;

import com.hp.hpl.jena.graph.Node ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.junit.Test ;
import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.ErrorHandlerTestLib ;
import org.openjena.riot.ErrorHandlerTestLib.ExWarning ;
import org.openjena.riot.checker.CheckerIRI ;
import org.openjena.riot.system.Checker ;
import org.openjena.riot.system.RiotLib ;

public class TestIRI extends BaseTest
{
    static protected final ErrorHandler handler = new ErrorHandlerTestLib.ErrorHandlerEx() ;
    static protected final Checker checker = new Checker(new ErrorHandlerTestLib.ErrorHandlerEx()) ;
    
    static IRIFactory factory = IRIFactory.iriImplementation() ;
    
    @Test public void iri1()  { test("http://example/") ; }
    
    @Test(expected=ErrorHandlerTestLib.ExError.class)
    // No relative IRIs
    public void iri2()  { test("example") ; }
    
    @Test(expected=ExWarning.class) 
    public void iriErr1()  { test("http:") ; }

    @Test(expected=ExWarning.class) 
    public void iriErr2()  { test("http:///::") ; }

    @Test(expected=ExWarning.class) 
    public void iriErr3()  { test("http://example/.") ; }
    
    private void test(String uriStr)
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
    
}
